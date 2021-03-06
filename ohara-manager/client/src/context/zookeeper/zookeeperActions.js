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

import { map } from 'lodash';

import { KIND } from 'const';
import * as routines from './zookeeperRoutines';
import * as action from 'utils/action';

export const createActions = context => {
  const { state, dispatch, zookeeperApi } = context;
  return {
    fetchZookeepers: async () => {
      const routine = routines.fetchZookeepersRoutine;
      if (state.isFetching || state.lastUpdated || state.error) return;
      try {
        dispatch(routine.request());
        const zookeepers = await zookeeperApi.fetchAll();
        const data = map(zookeepers, zookeeper => ({
          ...zookeeper,
          serviceType: KIND.zookeeper,
        }));
        dispatch(routine.success(data));
        return action.success(data);
      } catch (e) {
        dispatch(routine.failure(e.message));
        return action.failure(e.message);
      }
    },
    createZookeeper: async values => {
      const routine = routines.createZookeeperRoutine;
      if (state.isFetching) return;
      try {
        dispatch(routine.request());
        const createRes = await zookeeperApi.create(values);
        const startRes = await zookeeperApi.start(values.name);
        const data = { ...createRes, ...startRes };
        dispatch(routine.success(data));
        return action.success(data);
      } catch (e) {
        dispatch(routine.failure(e.message));
        return action.failure(e.message);
      }
    },
    updateZookeeper: async values => {
      const routine = routines.updateZookeeperRoutine;
      if (state.isFetching) return;
      try {
        dispatch(routine.request());
        const data = await zookeeperApi.update(values);
        dispatch(routine.success(data));
        return action.success(data);
      } catch (e) {
        dispatch(routine.failure(e.message));
        return action.failure(e.message);
      }
    },
    stageZookeeper: async values => {
      const routine = routines.stageZookeeperRoutine;
      if (state.isFetching) return;
      try {
        dispatch(routine.request());
        const data = await zookeeperApi.stage(values);
        dispatch(routine.success(data));
        return action.success(data);
      } catch (e) {
        dispatch(routine.failure(e.message));
        return action.failure(e.message);
      }
    },
    deleteZookeeper: async name => {
      const routine = routines.deleteZookeeperRoutine;
      if (state.isFetching) return;
      try {
        dispatch(routine.request());
        const data = await zookeeperApi.delete(name);
        dispatch(routine.success(data));
        return action.success(data);
      } catch (e) {
        dispatch(routine.failure(e.message));
        return action.failure(e.message);
      }
    },
    startZookeeper: async name => {
      const routine = routines.startZookeeperRoutine;
      if (state.isFetching) return;
      try {
        dispatch(routine.request());
        const data = await zookeeperApi.start(name);
        dispatch(routine.success(data));
        return action.success(data);
      } catch (e) {
        dispatch(routine.failure(e.message));
        return action.failure(e.message);
      }
    },
    stopZookeeper: async name => {
      const routine = routines.stopZookeeperRoutine;
      if (state.isFetching) return;
      try {
        dispatch(routine.request());
        const data = await zookeeperApi.stop(name);
        dispatch(routine.success(data));
        return action.success(data);
      } catch (e) {
        dispatch(routine.failure(e.message));
        return action.failure(e.message);
      }
    },
  };
};
