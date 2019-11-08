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

import React, { useState, useEffect } from 'react';
import MenuItem from '@material-ui/core/MenuItem';
import ExpansionPanelSummary from '@material-ui/core/ExpansionPanelSummary';
import ExpansionPanelDetails from '@material-ui/core/ExpansionPanelDetails';
import ExpandMoreIcon from '@material-ui/icons/ExpandMore';
import AddIcon from '@material-ui/icons/Add';
import ShareIcon from '@material-ui/icons/Share';
import Menu from '@material-ui/core/Menu';
import { NavLink, useParams } from 'react-router-dom';
import { Form, Field } from 'react-final-form';

import * as pipelineApi from 'api/pipelineApi';
import { InputField } from 'components/common/Form';
import { Dialog } from 'components/common/Dialog';
import { useSnackbar } from 'context/SnackbarContext';
import { usePipeline } from 'context/PipelineContext';
import { useWorkspace } from 'context/WorkspaceContext';
import { useEditWorkspace } from 'context/EditWorkspaceContext';
import {
  required,
  validServiceName,
  minLength,
  maxLength,
  composeValidators,
} from 'utils/validate';
import {
  StyledNavigator,
  StyledButton,
  StyledExpansionPanel,
  StyledSubtitle1,
  PipelineList,
} from './Styles';

const Navigator = () => {
  const showMessage = useSnackbar();
  const [anchorEl, setAnchorEl] = useState(null);
  const [isOpen, setIsOpen] = useState(false);
  const { workspaceName } = useParams();
  const { findByWorkspaceName } = useWorkspace();
  const {
    setIsOpen: setIsEditWorkspaceOpen,
    setTab: setEditWorkspaceTab,
  } = useEditWorkspace();
  const { pipelines, doFetch: fetchPipelines } = usePipeline();

  const handleClick = event => {
    setAnchorEl(event.currentTarget);
  };

  const handleClose = () => {
    setAnchorEl(null);
  };

  const handleMenuItemClick = key => () => {
    setIsEditWorkspaceOpen(true);
    setEditWorkspaceTab(key);
    handleClose();
  };

  const onSubmit = async ({ pipelineName }, form) => {
    const response = await pipelineApi.create({
      name: pipelineName,
      group: workspaceName,
    });

    // TODO: this logic can be simplify once #3124 is done
    if (response && response.name === pipelineName) {
      showMessage(`Pipeline ${pipelineName} has been added`);
      await fetchPipelines(workspaceName);
      setTimeout(form.reset);
    }

    setIsOpen(false);
  };

  const validWorkspaceName = findByWorkspaceName(workspaceName);

  useEffect(() => {
    fetchPipelines(workspaceName);
  }, [fetchPipelines, workspaceName]);

  if (!validWorkspaceName) return null;

  return (
    <StyledNavigator>
      <StyledButton disableRipple onClick={handleClick}>
        <span className="menu-name">{workspaceName}</span>
        <ExpandMoreIcon />
      </StyledButton>
      <Menu
        anchorEl={anchorEl}
        keepMounted
        open={Boolean(anchorEl)}
        onClose={handleClose}
      >
        <MenuItem key="overview" onClick={handleMenuItemClick('overview')}>
          Overview
        </MenuItem>
        <MenuItem key="topics" onClick={handleMenuItemClick('topics')}>
          Topics
        </MenuItem>
        <MenuItem key="files" onClick={handleMenuItemClick('files')}>
          Files
        </MenuItem>
        <MenuItem key="settings" onClick={handleMenuItemClick('settings')}>
          Settings
        </MenuItem>
      </Menu>

      <Form
        onSubmit={onSubmit}
        initialValues={{}}
        render={({ handleSubmit, form, pristine, invalid }) => (
          <Dialog
            open={isOpen}
            title="Add a new pipeline"
            handleClose={() => {
              setIsOpen(false);
              form.reset();
            }}
            handleConfirm={handleSubmit}
            confirmDisabled={pristine || invalid}
          >
            <form onSubmit={handleSubmit}>
              <Field
                type="text"
                name="pipelineName"
                label="Pipeline name"
                placeholder="pipelinename"
                component={InputField}
                autoFocus
                required
                validate={composeValidators(
                  required,
                  minLength(2),
                  maxLength(20),
                  validServiceName,
                )}
              />
            </form>
          </Dialog>
        )}
      />

      <StyledExpansionPanel defaultExpanded={true}>
        <ExpansionPanelSummary disableRipple expandIcon={<ExpandMoreIcon />}>
          <StyledSubtitle1>Pipelines</StyledSubtitle1>
          <AddIcon
            className="new-pipeline-button"
            onClick={event => {
              event.stopPropagation();
              setIsOpen(true);
            }}
          />
        </ExpansionPanelSummary>
        <ExpansionPanelDetails>
          <PipelineList>
            {pipelines.map(pipeline => (
              <li key={pipeline.name}>
                <NavLink
                  activeClassName="active-link"
                  to={`/${workspaceName}/${pipeline.name}`}
                >
                  <ShareIcon className="link-icon" />
                  {pipeline.name}
                </NavLink>
              </li>
            ))}
          </PipelineList>
        </ExpansionPanelDetails>
      </StyledExpansionPanel>
    </StyledNavigator>
  );
};

export default Navigator;
