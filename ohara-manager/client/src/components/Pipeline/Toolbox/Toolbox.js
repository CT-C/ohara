/*
 * Copyright 2019 is-land
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import React, { useState, useEffect, useRef } from 'react';
import PropTypes from 'prop-types';
import Draggable from 'react-draggable';
import Typography from '@material-ui/core/Typography';
import IconButton from '@material-ui/core/IconButton';
import List from '@material-ui/core/List';
import ExpansionPanel from '@material-ui/core/ExpansionPanel';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import AddIcon from '@material-ui/icons/Add';
import CloseIcon from '@material-ui/icons/Close';
import * as joint from 'jointjs';

import ToolboxAddGraphDialog from './ToolboxAddGraphDialog';
import ToolboxSearch from './ToolboxSearch';
import ConnectorGraph from '../Graph/Connector/ConnectorGraph';
import ToolboxUploadButton from './ToolboxUploadButton';
import * as utils from './toolboxUtils';
import * as context from 'context';
import { KIND } from 'const';
import { StyledToolbox } from './ToolboxStyles';
import { AddTopicDialog } from 'components/Topic';
import { useFiles, useToolboxHeight, useTopics } from './ToolboxHooks';
import { getKey } from 'utils/object';

const Toolbox = props => {
  const {
    isOpen: isToolboxOpen,
    expanded,
    handleClose,
    handleClick,
    paper,
    graph,
    toolboxKey,
    setToolboxExpanded,
    initToolboxList,
  } = props;

  const {
    currentWorker,
    currentWorkspace,
    currentBroker,
    currentPipeline,
  } = context.useWorkspace();
  const { updatePipeline } = context.usePipelineActions();
  const { createStream } = context.useStreamActions();
  const { createFile } = context.useFileActions();
  const {
    startConnector,
    stopConnector,
    deleteConnector,
  } = context.useConnectorActions();

  const { open: openAddTopicDialog } = context.useAddTopicDialog();
  const { open: openSettingDialog, setData } = context.useGraphSettingDialog();
  const { createTopic, stopTopic, deleteTopic } = context.useTopicActions();
  const { createConnector } = context.useConnectorActions();
  const showMessage = context.useSnackbar();

  const [isOpen, setIsOpen] = useState(false);
  const [zIndex, setZIndex] = useState(2);
  const [searchResults, setSearchResults] = useState(null);
  const [cellInfo, setCellInfo] = useState({
    classType: '',
    className: '',
    displayedClassName: '',
    icon: '',
    position: {
      x: 0,
      y: 0,
    },
  });
  const { streams, files: streamFiles } = useFiles();
  const [sources, sinks] = utils.getConnectorInfo(currentWorker);
  const [topics, topicsData] = useTopics(currentWorkspace);

  const connectors = {
    sources,
    topics,
    streams,
    sinks,
  };

  const {
    toolboxHeight,
    toolboxRef,
    toolboxHeaderRef,
    panelSummaryRef,
    panelAddButtonRef,
  } = useToolboxHeight({
    expanded,
    paper,
    searchResults,
    connectors,
  });

  const handleFileSelect = async event => {
    const [file] = event.target.files;

    if (!file) return;
    const { error } = await createFile(file);
    if (error) return showMessage(error);
  };

  const removeTempCell = () => {
    // Remove temporary cells
    const tempCells = graph.current
      .getCells()
      .filter(cell => Boolean(cell.attributes.isTemporary));
    tempCells.forEach(cell => cell.remove());
  };

  const handleAddGraph = async newGraphName => {
    setZIndex(zIndex + 1);

    const sharedParams = {
      title: newGraphName,
      graph,
      paper,
      cellInfo,
    };

    switch (cellInfo.classType) {
      case KIND.stream:
        const [targetStream] = streamFiles
          .filter(streamFile =>
            streamFile.classInfos.find(
              classInfo => classInfo.className === cellInfo.className,
            ),
          )
          .map(streamFile => {
            const stream = {
              ...streamFile,
              classInfos: streamFile.classInfos.filter(
                classInfo => classInfo.className === cellInfo.className,
              ),
            };
            return stream;
          });
        const requestParams = {
          name: newGraphName,
          jarKey: { name: targetStream.name, group: targetStream.group },
          brokerClusterKey: getKey(currentBroker),
          connector__class: cellInfo.className,
        };
        const definition = targetStream.classInfos[0];

        createStream(requestParams, definition);
        await updatePipeline({
          name: currentPipeline.name,
          endpoints: [
            ...currentPipeline.endpoints,
            {
              name: requestParams.name,
              kind: KIND.stream,
            },
          ],
        });

        graph.current.addCell(
          ConnectorGraph({
            ...sharedParams,
          }),
        );

        break;

      case KIND.source:
      case KIND.sink:
        const { classInfos } = currentWorker;

        const connectorRes = await createConnector({
          name: newGraphName,
          connector__class: cellInfo.className,
        });

        await updatePipeline({
          name: currentPipeline.name,
          endpoints: [
            ...currentPipeline.endpoints,
            {
              name: connectorRes.data.name,
              kind: connectorRes.data.kind,
            },
          ],
        });

        const [targetConnector] = classInfos.filter(
          classInfo => classInfo.className === cellInfo.className,
        );

        graph.current.addCell(
          ConnectorGraph({
            ...sharedParams,
            openSettingDialog,
            setData,
            classInfo: targetConnector,
            name: connectorRes.data.name,
            startConnector,
            stopConnector,
            deleteConnector,
            updatePipeline,
            currentPipeline,
          }),
        );
        break;

      default:
        break;
    }
    removeTempCell();
    showMessage(`${newGraphName} has been added`);
    setIsOpen(false);
  };

  let sourceGraph = useRef(null);
  let sinkGraph = useRef(null);
  let topicGraph = useRef(null);
  let streamGraph = useRef(null);

  useEffect(() => {
    if (!connectors.sources || !connectors.sinks) return;

    const renderToolbox = () => {
      const sharedProps = {
        width: 'auto',
        height: 'auto',
        interactive: false,
        // this fixes JointJs cannot properly render these html elements in es6 modules: https://github.com/clientIO/joint/issues/1134
        cellViewNamespace: joint.shapes,
      };

      sourceGraph.current = new joint.dia.Graph();
      topicGraph.current = new joint.dia.Graph();
      streamGraph.current = new joint.dia.Graph();
      sinkGraph.current = new joint.dia.Graph();

      const sourcePaper = new joint.dia.Paper({
        el: document.getElementById('source-list'),
        model: sourceGraph.current,
        ...sharedProps,
      });

      const topicPaper = new joint.dia.Paper({
        el: document.getElementById('topic-list'),
        model: topicGraph.current,
        ...sharedProps,
      });

      const streamPaper = new joint.dia.Paper({
        el: document.getElementById('stream-list'),
        model: streamGraph.current,
        ...sharedProps,
      });

      const sinkPaper = new joint.dia.Paper({
        el: document.getElementById('sink-list'),
        model: sinkGraph.current,
        ...sharedProps,
      });

      utils.createToolboxList({
        connectors,
        streamGraph,
        sourceGraph,
        sinkGraph,
        topicGraph,
        searchResults,
      });

      // Add the ability to drag and drop connectors/streams/topics
      utils.enableDragAndDrop({
        toolPapers: [sourcePaper, sinkPaper, topicPaper, streamPaper],
        paper,
        setCellInfo,
        setIsOpen,
        graph,
        currentPipeline,
        topicsData,
        createTopic,
        stopTopic,
        deleteTopic,
        updatePipeline,
      });
    };

    renderToolbox();
  }, [
    createTopic,
    connectors,
    currentBroker,
    currentPipeline,
    graph,
    paper,
    searchResults,
    topicsData,
    updatePipeline,
    initToolboxList,
    topics,
    stopTopic,
    deleteTopic,
  ]);

  return (
    <Draggable
      bounds="parent"
      handle=".toolbox-title"
      ref={toolboxRef}
      key={toolboxKey}
    >
      <StyledToolbox className={`toolbox ${isToolboxOpen ? 'is-open' : ''}`}>
        <div className="toolbox-header" ref={toolboxHeaderRef}>
          <div className="title toolbox-title">
            <Typography variant="subtitle1">Toolbox</Typography>
            <IconButton onClick={handleClose}>
              <CloseIcon />
            </IconButton>
          </div>

          <ToolboxSearch
            searchData={Object.values(connectors).flat()}
            setSearchResults={setSearchResults}
            setToolboxExpanded={setToolboxExpanded}
          />
        </div>

        <div
          className="toolbox-body"
          style={{ height: toolboxHeight ? toolboxHeight : 'auto' }}
        >
          <ExpansionPanel square expanded={expanded.source}>
            <ExpansionPanelSummary
              ref={panelSummaryRef}
              expandIcon={<ExpandMoreIcon />}
              onClick={() => handleClick('source')}
            >
              <Typography variant="subtitle1">Source</Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className="detail">
              <List disablePadding>
                <div id="source-list" className="toolbox-list"></div>
              </List>

              <ToolboxUploadButton
                buttonText="Add source connectors"
                onChange={handleFileSelect}
                ref={panelAddButtonRef}
              />
            </ExpansionPanelDetails>
          </ExpansionPanel>

          <ExpansionPanel square expanded={expanded.topic}>
            <ExpansionPanelSummary
              className="panel-title"
              expandIcon={<ExpandMoreIcon />}
              onClick={() => handleClick('topic')}
            >
              <Typography variant="subtitle1">Topic</Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className="detail">
              <List disablePadding>
                <div id="topic-list" className="toolbox-list"></div>
              </List>

              <div className="add-button">
                <IconButton onClick={openAddTopicDialog}>
                  <AddIcon />
                </IconButton>
                <Typography variant="subtitle2">Add topics</Typography>
              </div>
            </ExpansionPanelDetails>
          </ExpansionPanel>

          <AddTopicDialog />

          <ExpansionPanel square expanded={expanded.stream}>
            <ExpansionPanelSummary
              className="panel-title"
              expandIcon={<ExpandMoreIcon />}
              onClick={() => handleClick('stream')}
            >
              <Typography variant="subtitle1">Stream</Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className="detail">
              <List disablePadding>
                <div id="stream-list" className="toolbox-list"></div>
              </List>

              <ToolboxUploadButton
                buttonText="Add streams"
                onChange={handleFileSelect}
                ref={panelAddButtonRef}
              />
            </ExpansionPanelDetails>
          </ExpansionPanel>

          <ExpansionPanel square expanded={expanded.sink}>
            <ExpansionPanelSummary
              className="panel-title"
              expandIcon={<ExpandMoreIcon />}
              onClick={() => handleClick('sink')}
            >
              <Typography variant="subtitle1">Sink</Typography>
            </ExpansionPanelSummary>
            <ExpansionPanelDetails className="detail">
              <List disablePadding>
                <div id="sink-list" className="toolbox-list"></div>
              </List>

              <ToolboxUploadButton
                buttonText="Add sink connectors"
                onChange={handleFileSelect}
                ref={panelAddButtonRef}
              />
            </ExpansionPanelDetails>
          </ExpansionPanel>
        </div>

        <ToolboxAddGraphDialog
          isOpen={isOpen}
          classType={cellInfo.classType}
          handleConfirm={handleAddGraph}
          handleClose={() => {
            setIsOpen(false);
            removeTempCell();
          }}
        />
      </StyledToolbox>
    </Draggable>
  );
};

Toolbox.propTypes = {
  isOpen: PropTypes.bool.isRequired,
  handleClose: PropTypes.func.isRequired,
  handleClick: PropTypes.func.isRequired,
  expanded: PropTypes.shape({
    topic: PropTypes.bool.isRequired,
    source: PropTypes.bool.isRequired,
    sink: PropTypes.bool.isRequired,
    stream: PropTypes.bool.isRequired,
  }).isRequired,
  toolboxKey: PropTypes.number.isRequired,
  setToolboxExpanded: PropTypes.func.isRequired,
  initToolboxList: PropTypes.number.isRequired,
  paper: PropTypes.any,
  graph: PropTypes.any,
};

export default Toolbox;
