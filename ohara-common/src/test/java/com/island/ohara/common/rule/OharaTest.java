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

package com.island.ohara.common.rule;

import org.junit.AssumptionViolatedException;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.rules.Timeout;

/**
 * OharaTest carries the basic information to junit to set our tests. All ohara tests must extend
 * it. TestTestCases (in ohara-assembly) is able to pick up the invalidated classes for us :)
 */
public abstract class OharaTest {
  /**
   * the timeout to whole test class. Consider splitting test class if it is too large to be
   * completed before timeout.
   */
  @ClassRule public static final Timeout GLOBAL_TIMEOUT = Timeout.seconds(600);

  /** the timeout to test case. */
  @Rule public final Timeout timeout = Timeout.seconds(120);
  /**
   * check exception will throws
   *
   * @param c Exception class
   * @param r executed method
   */
  protected static <T extends Throwable> T assertException(Class<T> c, Runnable r) {
    try {
      r.run();
    } catch (Exception e) {
      if (c.isInstance(e)) return c.cast(e);
      else
        throw new AssertionError(
            String.format(
                "Assert ERROR: The %s throws ,but the expected exception is  %s ",
                e.getClass().getName(), c.getName()),
            e);
    }
    throw new AssertionError("Assert ERROR: No exception throws");
  }

  /**
   * Skip all remaining test cases after calling this method.
   *
   * @param message why you want to skip all test cases?
   */
  protected void skipTest(String message) {
    throw new AssumptionViolatedException(message);
  }
}
