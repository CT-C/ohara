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

package com.island.ohara.shabondi.sink

import java.time.{Duration => JDuration}
import java.util.concurrent.{ExecutorService, TimeUnit}

import com.island.ohara.common.util.Releasable
import com.island.ohara.shabondi.{BasicShabondiTest, KafkaSupport}
import org.junit.{After, Test}

import scala.compat.java8.DurationConverters
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}

final class TestSinkDataGroups extends BasicShabondiTest {
  @After
  override def tearDown(): Unit = {
    super.tearDown()
  }

  @Test
  def testDefaultGroup(): Unit = {
    val threadPool: ExecutorService = newThreadPool()
    implicit val ec                 = ExecutionContext.fromExecutorService(threadPool)
    val topicKey1                   = createTopicKey
    val rowCount                    = 999
    val dataGroups                  = new SinkDataGroups(brokerProps, Seq(topicKey1.name), DurationConverters.toJava(10 seconds))
    try {
      KafkaSupport.prepareBulkOfRow(brokerProps, topicKey1.name, rowCount)

      val queue  = dataGroups.defaultGroup.queue
      val queue1 = dataGroups.defaultGroup.queue
      queue should ===(queue1)
      dataGroups.size should ===(1)

      val rows = countRows(queue, 10 * 1000, ec)

      Await.result(rows, 30 seconds) should ===(rowCount)
    } finally {
      Releasable.close(dataGroups)
      threadPool.shutdown()
    }
  }

  @Test
  def testMultipleGroup(): Unit = {
    val threadPool: ExecutorService = newThreadPool()
    implicit val ec                 = ExecutionContext.fromExecutorService(threadPool)
    val topicKey1                   = createTopicKey
    val rowCount                    = 999
    val dataGroups                  = new SinkDataGroups(brokerProps, Seq(topicKey1.name), DurationConverters.toJava(10 seconds))
    try {
      KafkaSupport.prepareBulkOfRow(brokerProps, topicKey1.name, rowCount)

      val queue  = dataGroups.defaultGroup.queue
      val queue1 = dataGroups.createIfAbsent("group1").queue
      val queue2 = dataGroups.createIfAbsent("group1").queue
      queue1 should ===(queue2)
      dataGroups.size should ===(2)

      val rows  = countRows(queue, 10 * 1000, ec)
      val rows1 = countRows(queue1, 10 * 1000, ec)

      Await.result(rows, 30 seconds) should ===(rowCount)
      Await.result(rows1, 30 seconds) should ===(rowCount)
    } finally {
      Releasable.close(dataGroups)
      threadPool.shutdown()
    }
  }

  @Test
  def testRowQueueIsIdle(): Unit = {
    val idleTime = JDuration.ofSeconds(2)
    val rowQueue = new RowQueue("group1")
    multipleRows(100).foreach(rowQueue.add)
    rowQueue.poll() should !==(null)
    rowQueue.isIdle(idleTime) should ===(false)
    TimeUnit.SECONDS.sleep(1)

    rowQueue.poll() should !==(null)
    rowQueue.isIdle(idleTime) should ===(false)
    TimeUnit.SECONDS.sleep(3)

    rowQueue.isIdle(idleTime) should ===(true)
  }

  @Test
  def testFreeIdleGroup(): Unit = {
    val threadPool: ExecutorService = newThreadPool()
    implicit val ec                 = ExecutionContext.fromExecutorService(threadPool)
    val topicKey1                   = createTopicKey
    val idleTime                    = JDuration.ofSeconds(3)
    val dataGroups                  = new SinkDataGroups(brokerProps, Seq(topicKey1.name), DurationConverters.toJava(10 seconds))
    try {
      val group1Name   = "group1"
      val defaultGroup = dataGroups.defaultGroup
      val group1       = dataGroups.createIfAbsent(group1Name)
      dataGroups.size should ===(2)

      // test for not over idle time
      countRows(defaultGroup.queue, 1 * 1000, ec)
      countRows(group1.queue, 1 * 1000, ec)
      TimeUnit.SECONDS.sleep(idleTime.getSeconds - 1)
      dataGroups.freeIdleGroup(idleTime)
      dataGroups.groupExist(dataGroups.defaultGroupName) should ===(true)
      dataGroups.groupExist(group1Name) should ===(true)

      // test for idle time has passed
      countRows(defaultGroup.queue, 2 * 1000, ec)
      TimeUnit.SECONDS.sleep(idleTime.getSeconds)
      dataGroups.freeIdleGroup(idleTime)
      dataGroups.size should ===(1)
      dataGroups.groupExist(dataGroups.defaultGroupName) should ===(true)
      dataGroups.groupExist(group1Name) should ===(false)
    } finally {
      Releasable.close(dataGroups)
      threadPool.shutdown()
    }
  }
}
