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

package com.island.ohara.client.configurator.v0

import java.io.File

import com.island.ohara.common.rule.SmallTest
import com.island.ohara.common.util.CommonUtils
import org.junit.Test
import org.scalatest.Matchers

import scala.concurrent.ExecutionContext.Implicits.global
class TestFileApi extends SmallTest with Matchers {

  private[this] def access: FileApi.Access = FileApi.access.hostname(CommonUtils.hostname()).port(22)

  @Test
  def emptyNameInGet(): Unit =
    an[IllegalArgumentException] should be thrownBy access.get(CommonUtils.randomString(10), "")

  @Test
  def nullNameInGet(): Unit =
    an[NullPointerException] should be thrownBy access.get(CommonUtils.randomString(10), null)

  @Test
  def emptyGroupInGet(): Unit =
    an[IllegalArgumentException] should be thrownBy access.get("", CommonUtils.randomString(10))

  @Test
  def nullGroupInGet(): Unit =
    an[NullPointerException] should be thrownBy access.get(null, CommonUtils.randomString(10))

  @Test
  def emptyNameInDelete(): Unit =
    an[IllegalArgumentException] should be thrownBy access.delete(CommonUtils.randomString(10), "")

  @Test
  def nullNameInDelete(): Unit =
    an[NullPointerException] should be thrownBy access.delete(CommonUtils.randomString(10), null)

  @Test
  def emptyGroupInDelete(): Unit =
    an[IllegalArgumentException] should be thrownBy access.delete("", CommonUtils.randomString(10))

  @Test
  def nullGroupInDelete(): Unit =
    an[NullPointerException] should be thrownBy access.delete(null, CommonUtils.randomString(10))

  @Test
  def emptyName(): Unit = an[IllegalArgumentException] should be thrownBy access.request.name("")

  @Test
  def nullName(): Unit = an[NullPointerException] should be thrownBy access.request.name(null)

  @Test
  def emptyGroup(): Unit = an[IllegalArgumentException] should be thrownBy access.request.group("")

  @Test
  def nullGroup(): Unit = an[NullPointerException] should be thrownBy access.request.group(null)

  @Test
  def nullFile(): Unit = an[NullPointerException] should be thrownBy access.request.file(null)

  @Test
  def nonexistentFile(): Unit =
    an[IllegalArgumentException] should be thrownBy access.request.file(new File(CommonUtils.randomString(5)))

  @Test
  def nullTags(): Unit = an[NullPointerException] should be thrownBy access.request.tags(null)

  @Test
  def emptyTags(): Unit = access.request.tags(Map.empty)
}
