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

import * as node from './body/nodeBody';
import {
  getKey,
  requestUtil,
  responseUtil,
  axiosInstance,
} from './utils/apiUtils';
import * as URL from './utils/url';
import wait from './waitApi';
import * as waitUtil from './utils/waitUtils';

const url = URL.NODE_URL;

export const state = {
  available: 'AVAILABLE',
  unavailable: 'UNAVAILABLE',
};

export const create = async (params = {}) => {
  const requestBody = requestUtil(params, node);
  const res = await axiosInstance.post(url, requestBody);

  const result = responseUtil(res, node);
  result.title =
    `Create node ${getKey(params)} ` +
    (result.errors ? 'failed.' : 'successful.');
  return result;
};

export const update = async params => {
  const { hostname } = params;
  delete params[hostname];
  const body = params;
  const res = await axiosInstance.put(`${url}/${hostname}`, body);
  const result = responseUtil(res, node);
  result.title =
    `Update node ${getKey(params)} ` +
    (result.errors ? 'failed.' : 'successful.');
  return result;
};

export const remove = async params => {
  const { hostname } = params;
  const deletedRes = await axiosInstance.delete(`${url}/${hostname}`);

  let result = {};
  if (deletedRes.data.isSuccess) {
    const res = await wait({
      url,
      checkFn: waitUtil.waitForNodeNonexistent,
      paramRes: params,
    });
    result = responseUtil(res, node);
  } else {
    result = responseUtil(deletedRes, node);
  }

  result.title =
    `Remove node ${getKey(params)} ` +
    (result.errors ? 'failed.' : 'successful.');
  return result;
};

export const get = async params => {
  const { hostname } = params;
  const res = await axiosInstance.get(`${url}/${hostname}`);
  const result = responseUtil(res, node);
  result.title =
    `Get node ${getKey(params)} ` + (result.errors ? 'failed.' : 'successful.');
  return result;
};

export const getAll = async (params = {}) => {
  const res = await axiosInstance.get(url + URL.toQueryParameters(params));
  const result = responseUtil(res, node);
  result.title = `Get node list ` + (result.errors ? 'failed.' : 'successful.');
  return result;
};
