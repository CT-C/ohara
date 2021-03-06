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

package com.island.ohara.kafka.connector;

import com.island.ohara.common.annotations.Nullable;
import com.island.ohara.common.data.Row;
import com.island.ohara.common.util.CommonUtils;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/** A wrap to SourceRecord. Currently, only value columns and value are changed. */
public class RowSourceRecord {
  private final Map<String, ?> sourcePartition;
  private final Map<String, ?> sourceOffset;
  private final String topicName;

  @Nullable("thanks to kafka")
  private final Integer partition;

  private final Row row;

  @Nullable("thanks to kafka")
  private final Long timestamp;

  private RowSourceRecord(
      Map<String, ?> sourcePartition,
      Map<String, ?> sourceOffset,
      String topicName,
      Integer partition,
      Row row,
      Long timestamp) {
    this.sourcePartition = Collections.unmodifiableMap(Objects.requireNonNull(sourcePartition));
    this.sourceOffset = Collections.unmodifiableMap(Objects.requireNonNull(sourceOffset));
    this.topicName = topicName;
    this.partition = partition;
    this.row = row;
    this.timestamp = timestamp;
  }

  public Map<String, ?> sourcePartition() {
    return sourcePartition;
  }

  public Map<String, ?> sourceOffset() {
    return sourceOffset;
  }

  public String topicName() {
    return topicName;
  }

  public Optional<Integer> partition() {
    return Optional.ofNullable(partition);
  }

  public Row row() {
    return row;
  }

  public Optional<Long> timestamp() {
    return Optional.ofNullable(timestamp);
  }

  /**
   * a helper method to create a record with topic and row
   *
   * @param topic topic name
   * @param row row
   * @return connect record
   */
  public static RowSourceRecord of(String topic, Row row) {
    return builder().row(row).topicName(topic).build();
  }

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder implements com.island.ohara.common.pattern.Builder<RowSourceRecord> {
    private Builder() {
      // do nothing
    }

    private Map<String, ?> sourcePartition = Collections.emptyMap();
    private Map<String, ?> sourceOffset = Collections.emptyMap();
    private Integer partition = null;
    private Row row = null;
    private Long timestamp = null;
    private String topicName = null;

    @com.island.ohara.common.annotations.Optional("default is empty")
    public Builder sourcePartition(Map<String, ?> sourcePartition) {
      this.sourcePartition = Objects.requireNonNull(sourcePartition);
      return this;
    }

    @com.island.ohara.common.annotations.Optional("default is empty")
    public Builder sourceOffset(Map<String, ?> sourceOffset) {
      this.sourceOffset = Objects.requireNonNull(sourceOffset);
      return this;
    }

    @com.island.ohara.common.annotations.Optional(
        "default is empty. It means target partition is computed by hash")
    public Builder partition(int partition) {
      this.partition = partition;
      return this;
    }

    public Builder row(Row row) {
      this.row = Objects.requireNonNull(row);
      return this;
    }

    @com.island.ohara.common.annotations.Optional("default is current time")
    public Builder timestamp(long timestamp) {
      this.timestamp = timestamp;
      return this;
    }

    public Builder topicName(String topicName) {
      this.topicName = CommonUtils.requireNonEmpty(topicName);
      return this;
    }

    @Override
    public RowSourceRecord build() {
      return new RowSourceRecord(
          Objects.requireNonNull(sourcePartition),
          Objects.requireNonNull(sourceOffset),
          Objects.requireNonNull(topicName),
          partition,
          Objects.requireNonNull(row),
          timestamp);
    }
  }
}
