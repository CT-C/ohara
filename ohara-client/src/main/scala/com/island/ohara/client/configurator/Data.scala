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

package com.island.ohara.client.configurator

import com.island.ohara.common.setting.ObjectKey
import spray.json.{JsValue, _}

/**
  * This is the basic type which can be stored by configurator.
  * All members are declared as "def" since not all subclasses intend to represent all members in restful APIs.
  */
trait Data {
  def group: String
  def name: String

  /**
    * a helper method used to generate the key of this data.
    * @return key
    */
  def key: ObjectKey = ObjectKey.of(group, name)

  def lastModified: Long
  def kind: String
  def tags: Map[String, JsValue]

  protected def raw: Map[String, JsValue]

  /**
    * by default, the query compare only "name", "group", "tags" and lastModified.
    * @param request query request
    * @return true if the query matches this object. otherwise, false
    */
  final def matched(request: QueryRequest): Boolean =
    request.raw.forall {
      case (key, value) => matchSetting(raw, key, value)
    }

  /**
    * there are many objects containing "settings", and it is filterable so we separate the related code for reusing.
    *
    * @param settings settings
    * @param key key
    * @param value string of json representation. Noted the string of json string is pure "string" (no quote)
    * @return true if the key-value is matched. Otherwise, false
    */
  private[this] def matchSetting(settings: Map[String, JsValue], key: String, value: String): Boolean =
    try if (value.toLowerCase == "none") !settings.contains(key)
    else
      settings.get(key).exists {
        // it is impossible to have JsNull since our json format does a great job :)
        case JsString(s)  => s == value
        case JsNumber(i)  => i == BigDecimal(value)
        case JsBoolean(b) => b == value.toBoolean
        case js: JsArray =>
          value.parseJson match {
            // include the part of elements => true
            // otherwise => false
            case other: JsArray => other.elements.forall(v => js.elements.contains(v))
            case _              => false
          }
        case js: JsObject =>
          value.parseJson match {
            case other: JsObject =>
              other.fields.forall {
                case (k, v) =>
                  matchSetting(js.fields, k, v match {
                    case JsString(s) => s
                    case _           => v.toString()
                  })
              }
            case _ => false
          }
        case JsNull => value.toLowerCase == "none"
        case _      => false
      } catch {
      case _: Throwable => false
    }
}
