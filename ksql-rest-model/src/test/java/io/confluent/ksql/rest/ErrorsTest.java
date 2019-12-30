/*
 * Copyright 2019 Confluent Inc.
 *
 * Licensed under the Confluent Community License (the "License"); you may not use
 * this file except in compliance with the License.  You may obtain a copy of the
 * License at
 *
 * http://www.confluent.io/confluent-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.confluent.ksql.rest;

import static javax.ws.rs.core.Response.Status.FORBIDDEN;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import io.confluent.ksql.rest.entity.KsqlErrorMessage;
import io.confluent.ksql.util.KsqlException;
import javax.ws.rs.core.Response;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class ErrorsTest {
  
  private static final String SOME_ERROR = "error string";
  private static final Response KAFKA_DENIED_ERROR = Response
      .status(FORBIDDEN)
      .entity(new KsqlErrorMessage(Errors.ERROR_CODE_FORBIDDEN_KAFKA_ACCESS, SOME_ERROR))
      .build();
  
  @Mock
  private ErrorMessages errorMessages;
  @Mock
  private Exception exception;
  
  private Errors errorHandler;

  @Before
  public void setUp() {
    when(errorMessages.kafkaAuthorizationErrorMessage(any(Exception.class)))
        .thenReturn(SOME_ERROR);
    errorHandler = new Errors(errorMessages);
  }

  @Test
  public void shouldReturnForbiddenKafkaResponse() {
    final Response response = errorHandler.accessDeniedFromKafkaResponse(exception);
    assertThat(response.getStatus(), is(403));
    assertThat(response.getEntity(), is(instanceOf(KsqlErrorMessage.class)));
    assertThat(((KsqlErrorMessage) response.getEntity()).getMessage(), is(SOME_ERROR));
  }

  @Test
  public void shouldReturnForbiddenKafkaErrorMessageString() {
    final String error = errorHandler.kafkaAuthorizationErrorMessage(exception);
    assertThat(error, is(SOME_ERROR));
  }

  @Test
  public void shouldReturnForbiddenKafkaResponseIfRootCauseTopicAuthorizationException() {
    final Response response = errorHandler.generateResponse(new KsqlException(
        new TopicAuthorizationException("error")), Errors.badRequest("bad"));
    assertThat(response.getStatus(), is(403));
    assertThat(response.getEntity(), is(instanceOf(KsqlErrorMessage.class)));
    assertThat(((KsqlErrorMessage) response.getEntity()).getMessage(), is(SOME_ERROR));
  }

  @Test
  public void shouldReturnResponseIfRootCauseNotTopicAuthorizationException() {
    final Response response = errorHandler.generateResponse(new KsqlException(
        new RuntimeException("error")), Errors.badRequest("bad"));
    assertThat(response.getStatus(), is(400));
  }
}
