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

package com.island.ohara.connector.jdbc.source

import com.island.ohara.client.kafka.WorkerClient
import com.island.ohara.common.setting.SettingDef.{Necessary, Permission, Reference}
import com.island.ohara.common.setting.{ConnectorKey, SettingDef, TopicKey}
import com.island.ohara.common.util.CommonUtils
import com.island.ohara.kafka.connector.json.ConnectorDefUtils
import com.island.ohara.testing.WithBrokerWorker
import org.junit.Test
import org.scalatest.Matchers._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

class TestJDBCSourceConnectorDefinition extends WithBrokerWorker {
  private[this] val jdbcSource                 = new JDBCSourceConnector
  private[this] val workerClient               = WorkerClient(testUtil().workersConnProps())
  private[this] def result[T](f: Future[T]): T = Await.result(f, 10 seconds)

  @Test
  def checkDbURL(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == DB_URL).get
    definition.necessary() shouldBe Necessary.REQUIRED
    definition.hasDefault shouldBe false
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.STRING
  }

  @Test
  def checkDbUserName(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == DB_USERNAME).get
    definition.necessary() shouldBe Necessary.REQUIRED
    definition.hasDefault shouldBe false
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.STRING
  }

  @Test
  def checkDbPassword(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == DB_PASSWORD).get
    definition.necessary() shouldBe Necessary.REQUIRED
    definition.hasDefault shouldBe false
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.PASSWORD
  }

  @Test
  def checkFetchDataSize(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == JDBC_FETCHDATA_SIZE).get
    definition.necessary() should not be Necessary.REQUIRED
    definition.defaultInt shouldBe JDBC_FETCHDATA_SIZE_DEFAULT
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.INT
  }

  @Test
  def checkFlushDataSize(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == JDBC_FLUSHDATA_SIZE).get
    definition.necessary() should not be Necessary.REQUIRED
    definition.defaultInt shouldBe JDBC_FLUSHDATA_SIZE_DEFAULT
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.INT
  }

  @Test
  def checkFrequenceTime(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == JDBC_FREQUENCE_TIME).get
    definition.necessary should not be Necessary.REQUIRED
    definition.defaultDuration() shouldBe java.time.Duration.ofMillis(JDBC_FREQUENCE_TIME_DEFAULT.toMillis)
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.DURATION
  }

  @Test
  def checkTableName(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == DB_TABLENAME).get
    definition.necessary() shouldBe Necessary.REQUIRED
    definition.hasDefault shouldBe false
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.JDBC_TABLE
  }

  @Test
  def checkCatalogPattern(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == DB_CATALOG_PATTERN).get
    definition.necessary() should not be Necessary.REQUIRED
    definition.hasDefault shouldBe false
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.STRING
  }

  @Test
  def checkSchemaPattern(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == DB_SCHEMA_PATTERN).get
    definition.necessary() should not be Necessary.REQUIRED
    definition.hasDefault shouldBe false
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.STRING
  }

  @Test
  def checkMode(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == MODE).get
    definition.necessary() should not be Necessary.REQUIRED
    definition.defaultString() shouldBe MODE_DEFAULT
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.STRING
  }

  @Test
  def checkTimeStampColumnName(): Unit = {
    val definition = jdbcSource.settingDefinitions().asScala.find(_.key() == TIMESTAMP_COLUMN_NAME).get
    definition.necessary() shouldBe Necessary.REQUIRED
    definition.hasDefault shouldBe false
    definition.permission() shouldBe Permission.EDITABLE
    definition.internal() shouldBe false
    definition.reference() shouldBe Reference.NONE
    definition.valueType() shouldBe SettingDef.Type.STRING
  }

  @Test
  def testSource(): Unit = {
    val url: String                 = "jdbc:postgresql://localhost:5432/postgres"
    val userName: String            = "user1"
    val password: String            = "123456"
    val tableName: String           = "table1"
    val timeStampColumnName: String = "COLUMN1"
    val topicKey                    = TopicKey.of(CommonUtils.randomString(5), CommonUtils.randomString(5))
    val connectorKey                = ConnectorKey.of(CommonUtils.randomString(5), CommonUtils.randomString(5))

    val response = result(
      workerClient
        .connectorValidator()
        .connectorKey(connectorKey)
        .numberOfTasks(1)
        .topicKey(topicKey)
        .settings(
          Map(
            DB_URL                -> url,
            DB_USERNAME           -> userName,
            DB_PASSWORD           -> password,
            DB_TABLENAME          -> tableName,
            TIMESTAMP_COLUMN_NAME -> timeStampColumnName,
            JDBC_FETCHDATA_SIZE   -> "1000",
            JDBC_FLUSHDATA_SIZE   -> "1000",
            JDBC_FREQUENCE_TIME   -> "1 second"
          )
        )
        .connectorClass(classOf[JDBCSourceConnector])
        .run()
    )
    response.settings().size should not be 0

    response
      .settings()
      .asScala
      .filter(_.definition().key() == ConnectorDefUtils.TOPIC_NAMES_DEFINITION.key())
      .head
      .definition()
      .necessary() shouldBe Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.definition().key() == ConnectorDefUtils.CONNECTOR_CLASS_DEFINITION.key())
      .head
      .definition()
      .necessary() shouldBe Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.definition().key() == ConnectorDefUtils.NUMBER_OF_TASKS_DEFINITION.key())
      .head
      .definition()
      .necessary() shouldBe Necessary.OPTIONAL_WITH_DEFAULT

    response
      .settings()
      .asScala
      .filter(_.definition().key() == ConnectorDefUtils.COLUMNS_DEFINITION.key())
      .head
      .definition()
      .necessary() should not be Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.definition().key() == ConnectorDefUtils.WORKER_CLUSTER_KEY_DEFINITION.key())
      .head
      .definition()
      .necessary() shouldBe Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.value().key() == DB_URL)
      .head
      .definition()
      .necessary() shouldBe Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.value().key() == DB_USERNAME)
      .head
      .definition()
      .necessary() shouldBe Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.value().key() == DB_PASSWORD)
      .head
      .definition()
      .necessary() shouldBe Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.value().key() == DB_TABLENAME)
      .head
      .definition()
      .necessary() shouldBe Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.value().key() == JDBC_FREQUENCE_TIME)
      .head
      .definition()
      .necessary() should not be Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.value().key() == TIMESTAMP_COLUMN_NAME)
      .head
      .definition()
      .necessary() shouldBe Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.value().key() == DB_CATALOG_PATTERN)
      .head
      .definition()
      .necessary() should not be Necessary.REQUIRED

    response
      .settings()
      .asScala
      .filter(_.value().key() == DB_SCHEMA_PATTERN)
      .head
      .definition()
      .necessary() should not be Necessary.REQUIRED

    response.errorCount() shouldBe 0
  }
}
