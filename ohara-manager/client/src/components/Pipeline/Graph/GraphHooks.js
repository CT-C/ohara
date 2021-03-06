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

export const useCenter = () => {
  const [isCentered, setIsCentered] = React.useState(false);

  const setCenter = ({ paper, currentCell, paperScale }) => {
    const contentLocalOrigin = paper.current.paperToLocalPoint(
      currentCell.current.bBox,
    );

    const currentTranslate = paper.current.translate();
    const computedSize = paper.current.getComputedSize();
    const fittingBbox = {
      x: currentTranslate.tx,
      y: currentTranslate.ty,
      width: computedSize.width,
      height: computedSize.height,
    };

    const origin = paper.current.options.origin;
    const newOx = fittingBbox.x - contentLocalOrigin.x * paperScale - origin.x;
    const newOy = fittingBbox.y - contentLocalOrigin.y * paperScale - origin.y;

    paper.current.translate(
      // divide by 2 so it's centered
      newOx + fittingBbox.width / 2,
      newOy + fittingBbox.height / 2,
    );
  };

  return {
    isCentered,
    setIsCentered,
    setCenter,
  };
};

export const useZoom = () => {
  const [paperScale, setPaperScale] = React.useState(1);
  const [isFitToContent, setIsFitToContent] = React.useState(false);

  const setZoom = (scale, instruction = 'fromDropdown') => {
    const fixedScale = Number((Math.floor(scale * 100) / 100).toFixed(2));
    const allowedScales = [0.01, 0.03, 0.06, 0.12, 0.25, 0.5, 1.0, 2.0];
    const isValidScale = allowedScales.includes(fixedScale);

    // Prevent graph from rescaling again
    setIsFitToContent(false);

    if (isValidScale) {
      // If the instruction is `fromDropdown`, we will use the scale it gives
      // and update the state right alway
      if (instruction === 'fromDropdown') return setPaperScale(scale);

      // By default, the scale is multiply and divide by `2`
      let newScale = 0;
      if (instruction === 'in') {
        // Manipulate two special values here, they're not valid
        // in our App:
        // 0.02 -> 0.03
        // 0.24 -> 0.25

        if (fixedScale * 2 === 0.02) {
          newScale = 0.03;
        } else if (fixedScale * 2 === 0.24) {
          newScale = 0.25;
        } else {
          // Handle other scale normally
          newScale = fixedScale * 2;
        }
      } else {
        newScale = fixedScale / 2;
      }

      return setPaperScale(newScale);
    }

    // Handle `none-valid` scales here
    const defaultScales = [0.5, 1.0, 2.0];
    const closest = defaultScales.reduce((prev, curr) => {
      return Math.abs(curr - fixedScale) < Math.abs(prev - fixedScale)
        ? curr
        : prev;
    });

    let outScale;
    let inScale;
    if (closest === 0.5) {
      // If the fixedScale is something like 0.46, we'd like the next `in` scale
      // to be `0.5` not `1`
      inScale = fixedScale <= 0.5 ? 0.5 : 1;
      outScale = 0.5;
    } else if (closest === 1) {
      inScale = 1;
      outScale = 0.5;
    } else {
      inScale = 2;
      outScale = 2;
    }

    const newScale = instruction === 'in' ? inScale : outScale;
    setPaperScale(newScale);
  };

  return {
    setZoom,
    paperScale,
    setPaperScale,
    isFitToContent,
    setIsFitToContent,
  };
};
