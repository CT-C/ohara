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

package com.island.ohara.streams.config;

import com.island.ohara.common.rule.OharaTest;
import com.island.ohara.common.setting.SettingDef;
import org.junit.Assert;
import org.junit.Test;

public class TestStreamDefUtils extends OharaTest {

  @Test
  public void testJarDefinition() {
    Assert.assertEquals(StreamDefUtils.JAR_KEY_DEFINITION.valueType(), SettingDef.Type.OBJECT_KEY);
    Assert.assertEquals(StreamDefUtils.JAR_KEY_DEFINITION.reference(), SettingDef.Reference.FILE);
  }
}
