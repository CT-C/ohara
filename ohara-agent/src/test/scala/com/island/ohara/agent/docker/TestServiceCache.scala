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

package com.island.ohara.agent.docker

import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

import com.island.ohara.agent.docker.ServiceCache.RequestKey
import com.island.ohara.client.configurator.v0.ClusterStatus
import com.island.ohara.client.configurator.v0.ContainerApi.ContainerInfo
import com.island.ohara.common.rule.OharaTest
import com.island.ohara.common.setting.ObjectKey
import com.island.ohara.common.util.CommonUtils
import org.junit.Test
import org.scalatest.Matchers._

import scala.concurrent.duration._
class TestServiceCache extends OharaTest {
  private[this] def status(name: String): ClusterStatus = ClusterStatus(
    group = "default",
    name = name,
    kind = ClusterStatus.Kind.ZOOKEEPER,
    containers = Seq(fakeContainerInfo),
    state = Some("RUNNING"),
    error = None
  )

  private[this] def fakeContainerInfo: ContainerInfo = ContainerInfo(
    nodeName = CommonUtils.randomString(),
    id = CommonUtils.randomString(),
    imageName = CommonUtils.randomString(),
    state = CommonUtils.randomString(),
    kind = CommonUtils.randomString(),
    name = CommonUtils.randomString(),
    size = CommonUtils.current(),
    portMappings = Seq.empty,
    environments = Map.empty,
    hostname = CommonUtils.randomString()
  )

  @Test
  def testRequestKey(): Unit = {
    val key = RequestKey(
      key = ObjectKey.of(CommonUtils.randomString(), CommonUtils.randomString()),
      kind = ClusterStatus.Kind.ZOOKEEPER,
      createdTime = CommonUtils.current()
    )

    key shouldBe key
    key should not be key.copy(key = ObjectKey.of(CommonUtils.randomString(), CommonUtils.randomString()))
    key should not be key.copy(kind = ClusterStatus.Kind.WORKER)
  }

  @Test
  def testAutoRefresh(): Unit = {
    val clusterInfo0 = status(CommonUtils.randomString())
    val clusterInfo1 = status(CommonUtils.randomString())
    val fetchCount   = new AtomicInteger(0)
    val refreshCount = new AtomicInteger(0)
    val cache = ServiceCache.builder
      .supplier(() => {
        refreshCount.incrementAndGet()
        Seq(clusterInfo0, clusterInfo1)
      })
      .frequency(2 seconds)
      .build
    try {
      refreshCount.get() shouldBe 0
      fetchCount.get() shouldBe 0
      TimeUnit.SECONDS.sleep(3)
      refreshCount.get() shouldBe 1
      cache.snapshot.map(_.key).toSet shouldBe Seq(clusterInfo0, clusterInfo1).map(_.key).toSet
      fetchCount.get() shouldBe 0
    } finally cache.close()
  }

  @Test
  def testRequestUpdate(): Unit = {
    val clusterInfo0 = status(CommonUtils.randomString())
    val clusterInfo1 = status(CommonUtils.randomString())
    val fetchCount   = new AtomicInteger(0)
    val refreshCount = new AtomicInteger(0)
    val cache = ServiceCache.builder
      .supplier(() => {
        refreshCount.incrementAndGet()
        Seq(clusterInfo0, clusterInfo1)
      })
      .frequency(1000 seconds)
      .build
    try {
      refreshCount.get() shouldBe 0
      cache.requestUpdate()
      TimeUnit.SECONDS.sleep(3)
      refreshCount.get() shouldBe 1
      cache.snapshot.map(_.key).toSet shouldBe Seq(clusterInfo0, clusterInfo1).map(_.key).toSet
      fetchCount.get() shouldBe 0
      refreshCount.get() shouldBe 1
    } finally cache.close()
  }

  @Test
  def failToOperateAfterClose(): Unit = {
    val cache = ServiceCache.builder.supplier(() => Seq.empty).frequency(1000 seconds).build
    cache.close()

    an[IllegalStateException] should be thrownBy cache.snapshot
    an[IllegalStateException] should be thrownBy cache.requestUpdate()
  }

  @Test
  def testGet(): Unit = {
    val clusterInfo0 = status(CommonUtils.randomString())
    val cache = ServiceCache.builder
      .supplier(() => {
        Seq(clusterInfo0)
      })
      .frequency(1000 seconds)
      .build
    try {
      cache.snapshot shouldBe Seq.empty
      cache.put(clusterInfo0)
      cache.snapshot shouldBe Seq(clusterInfo0)
      cache.remove(clusterInfo0)
      cache.snapshot shouldBe Seq.empty
    } finally cache.close()
  }

  @Test
  def testLazyRemove(): Unit = {
    val count = new AtomicInteger(0)
    val cache = ServiceCache.builder
      .supplier(() => {
        count.incrementAndGet()
        Seq.empty
      })
      .frequency(1 seconds)
      .lazyRemove(5 seconds)
      .build
    try {
      val clusterInfo = status(CommonUtils.randomString())
      cache.put(clusterInfo)
      TimeUnit.SECONDS.sleep(2)
      val currentCount = count.get()
      currentCount should not be 0
      cache.snapshot shouldBe Seq(clusterInfo)
      TimeUnit.SECONDS.sleep(5)
      count.get() should not be currentCount
      cache.snapshot shouldBe Seq.empty
    } finally cache.close()
  }
}
