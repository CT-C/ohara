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

package com.island.ohara.client.kafka

import java.util
import java.util.Collections

import com.island.ohara.common.setting.SettingDef
import com.island.ohara.common.setting.SettingDef.Type
import com.island.ohara.kafka.connector.{RowSourceConnector, RowSourceTask, TaskSetting}

import scala.collection.JavaConverters._

class MyConnector extends RowSourceConnector {
  private[this] var settings: TaskSetting = _

  override protected def _taskClass(): Class[_ <: RowSourceTask] = classOf[MyConnectorTask]

  override protected def _taskSettings(maxTasks: Int): util.List[TaskSetting] =
    new util.ArrayList[TaskSetting](Seq.fill(maxTasks)(settings).asJavaCollection)

  override protected def _start(settings: TaskSetting): Unit = this.settings = settings

  override protected def _stop(): Unit = {
    // do nothing
  }

  override protected def _definitions(): util.List[SettingDef] = Collections.singletonList(
    // used by TestWorkerClient.passIncorrectDuration
    SettingDef.builder().key(MyConnector.DURATION_KEY).optional(Type.DURATION).build()
  )
}

object MyConnector {
  val DURATION_KEY: String = "myconnector.duration"
}
