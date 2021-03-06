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

import styled, { css } from 'styled-components';

export const Wrapper = styled.div(
  ({ theme }) => css`
    .shared {
      color: ${theme.palette.success.main};
      border: 1px solid ${theme.palette.success.main};
    }

    .private {
      color: ${theme.palette.warning.main};
      border: 1px solid ${theme.palette.warning.main};
    }

    .MuiChip-root {
      border-radius: ${theme.spacing(0.5)}px;

      .MuiSvgIcon-root {
        font-size: 1rem;
      }
    }
  `,
);
