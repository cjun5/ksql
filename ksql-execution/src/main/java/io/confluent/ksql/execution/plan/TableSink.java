/*
 * Copyright 2019 Confluent Inc.
 *
 * Licensed under the Confluent Community License; you may not use this file
 * except in compliance with the License.  You may obtain a copy of the License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.execution.plan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.errorprone.annotations.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Immutable
public class TableSink<K> implements ExecutionStep<KTableHolder<K>> {
  private final ExecutionStepPropertiesV1 properties;
  private final ExecutionStep<KTableHolder<K>> source;
  private final Formats formats;
  private final String topicName;

  public TableSink(
      @JsonProperty(value = "properties", required = true) final ExecutionStepPropertiesV1 props,
      @JsonProperty(value = "source", required = true) final ExecutionStep<KTableHolder<K>> source,
      @JsonProperty(value = "formats", required = true) final Formats formats,
      @JsonProperty(value = "topicName", required = true) final String topicName
  ) {
    this.properties = Objects.requireNonNull(props, "props");
    this.source = Objects.requireNonNull(source, "source");
    this.formats = Objects.requireNonNull(formats, "formats");
    this.topicName = Objects.requireNonNull(topicName, "topicName");
  }

  @Override
  public ExecutionStepPropertiesV1 getProperties() {
    return properties;
  }

  public String getTopicName() {
    return topicName;
  }

  @Override
  @JsonIgnore
  public List<ExecutionStep<?>> getSources() {
    return Collections.singletonList(source);
  }

  public Formats getFormats() {
    return formats;
  }

  public ExecutionStep<KTableHolder<K>> getSource() {
    return source;
  }

  @Override
  public KTableHolder<K> build(final PlanBuilder builder) {
    return builder.visitTableSink(this);
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final TableSink<?> tableSink = (TableSink<?>) o;
    return Objects.equals(properties, tableSink.properties)
        && Objects.equals(source, tableSink.source)
        && Objects.equals(formats, tableSink.formats)
        && Objects.equals(topicName, tableSink.topicName);
  }

  @Override
  public int hashCode() {

    return Objects.hash(properties, source, formats, topicName);
  }
}
