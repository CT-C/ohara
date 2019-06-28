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

import React from 'react';
import PropTypes from 'prop-types';
import styled from 'styled-components';
import Button from '@material-ui/core/Button';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogContentText from '@material-ui/core/DialogContentText';
import DialogTitle from '@material-ui/core/DialogTitle';
import CircularProgress from '@material-ui/core/CircularProgress';

const ButtonWrapper = styled.div`
  position: relative;
`;

const ConfirmButton = styled(Button)`
  &[disabled] {
    background-color: ${props => props.theme.palette.action.disabledBackground};
  }
`;

const Progress = styled(CircularProgress)`
  color: green;
  position: absolute;
  top: 50%;
  left: 50%;
  margin-top: -8px;
  margin-left: -8px;
`;

const AlertDialog = props => {
  const {
    title,
    content,
    open,
    handleConfirm,
    handleClose,
    cancelText = 'Cancel',
    confirmText = 'Delete',
    working = false,
  } = props;

  return (
    <>
      <Dialog open={open} onClose={handleClose}>
        <DialogTitle>{title}</DialogTitle>
        <DialogContent>
          <DialogContentText>{content}</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button disabled={working} onClick={handleClose}>
            {cancelText}
          </Button>
          <ButtonWrapper>
            <ConfirmButton
              disabled={working}
              onClick={handleConfirm}
              color="primary"
              autoFocus
            >
              {confirmText}
            </ConfirmButton>
            {working && <Progress size={14} />}
          </ButtonWrapper>
        </DialogActions>
      </Dialog>
    </>
  );
};

AlertDialog.propTypes = {
  title: PropTypes.string.isRequired,
  content: PropTypes.string.isRequired,
  open: PropTypes.bool.isRequired,
  handleClose: PropTypes.func.isRequired,
  handleConfirm: PropTypes.func.isRequired,
  cancelText: PropTypes.string,
  confirmText: PropTypes.string,
  working: PropTypes.bool,
};

export default AlertDialog;
