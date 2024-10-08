package com.adobe.aio.cloudmanager.impl.pipeline;

/*-
 * #%L
 * Adobe Cloud Manager Client Library
 * %%
 * Copyright (C) 2020 - 2023 Adobe Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

import com.adobe.aio.cloudmanager.impl.generated.Pipeline;
import com.adobe.aio.ims.feign.AuthInterceptor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.adobe.aio.cloudmanager.ApiBuilder;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.PipelineApi;
import com.adobe.aio.cloudmanager.PipelineUpdate;
import com.adobe.aio.cloudmanager.Variable;
import com.adobe.aio.cloudmanager.impl.AbstractApiTest;
import com.adobe.aio.cloudmanager.impl.generated.EmbeddedProgram;
import com.adobe.aio.cloudmanager.impl.generated.PipelinePhase;
import com.adobe.aio.cloudmanager.impl.program.ProgramImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.MediaType;
import org.mockserver.verify.VerificationTimes;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

class PipelineTest extends AbstractApiTest {
  private static final JsonBody GET_BODY = loadBodyJson("pipeline/get.json");
  public static final JsonBody LIST_BODY = loadBodyJson("pipeline/list.json");
  public static final JsonBody LIST_VARIABLES_BODY = loadBodyJson("pipeline/list-variables.json");
  private PipelineApi underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = new ApiBuilder<>(PipelineApi.class).workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  private HttpResponse handleGoodPatch(HttpRequest request) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();

    try (InputStream is = AbstractApiTest.class.getClassLoader().getResourceAsStream("pipeline/get.json")) {
      Pipeline original = objectMapper.readValue(is, Pipeline.class);
      PipelinePhase oBuildPhase = original.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType()).findFirst().orElse(null);
      assert oBuildPhase != null;

      Pipeline pipeline = objectMapper.readValue(request.getBodyAsJsonOrXmlString(), Pipeline.class);
      PipelinePhase uBuildPhase = pipeline.getPhases().stream().filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType()).findFirst().orElse(null);
      assert uBuildPhase != null;
      if (uBuildPhase.getBranch() != null) {
        oBuildPhase.setBranch(uBuildPhase.getBranch());
      }
      if (uBuildPhase.getRepositoryId() != null) {
        oBuildPhase.setRepositoryId(uBuildPhase.getRepositoryId());
      }
      return response().withBody(objectMapper.writeValueAsString(original), MediaType.APPLICATION_JSON);
    }
  }

  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown.");
    assertEquals("Cannot retrieve pipelines: %s/api/program/1/pipelines (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_emptyBody() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody("{}"));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown.");
    assertEquals("Cannot find pipelines for program 1.", exception.getMessage(), "Message was correct");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_emptyPipelines() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(json("{ \"_embedded\": {} }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown.");
    assertEquals("Cannot find pipelines for program 1.", exception.getMessage(), "Message was correct");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success(@Mock EmbeddedProgram mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getId()).thenReturn("1");

    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withStatusCode(OK_200.code()).withBody(LIST_BODY));
    Collection<com.adobe.aio.cloudmanager.Pipeline> pipelines = underTest.list(new ProgramImpl(mock, null));
    assertEquals(4, pipelines.size(), "Correct pipelines list length");

    client.verify(list);
    client.clear(list);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1", "1"), "Exception thrown.");
    assertEquals("Cannot retrieve pipeline: %s/api/program/1/pipeline/1 (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));
    com.adobe.aio.cloudmanager.Pipeline pipeline = underTest.get("1", "1");
    assertEquals("1", pipeline.getId());

    client.verify(get);
    client.clear(get);
  }

  @Test
  void delete_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.delete("1", "1"), "Exception thrown");
    assertEquals("Cannot delete pipeline: %s/api/program/1/pipeline/1 (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct");

    client.verify(delete);
    client.clear(delete);
  }

  @Test
  void delete_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(NO_CONTENT_204.code()));
    underTest.delete("1", "1");
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void delete_success_via_pipeline(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(delete).respond(response().withStatusCode(NO_CONTENT_204.code()));
    new PipelineImpl(mock, underTest, null).delete();
    client.verify(delete, VerificationTimes.exactly(1));
    client.clear(delete);
  }

  @Test
  void update_failure_no_build_phase() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(loadBodyJson("pipeline/get-no-build.json")));

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.update("1", "1", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals("Pipeline %s does not appear to have a build phase.".formatted("1"), exception.getMessage(), "Message was correct");
    client.verify(get, VerificationTimes.once());
    client.clear(get);
  }

  @Test
  void update_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1").withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.update("1", "1", PipelineUpdate.builder().build()), "Exception thrown");
    assertEquals("Cannot update pipeline: %s/api/program/1/pipeline/1 (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct");

    client.verify(get, VerificationTimes.once());
    client.verify(patch);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_branch_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1").withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);
    PipelineUpdate updates = PipelineUpdate.builder().branch("newbranch").build();
    PipelineImpl result = (PipelineImpl) underTest.update("1", "1", updates);
    assertEquals("newbranch", result.getPhases().get(1).getBranch(), "Branch was set correctly.");
    client.verify(get, VerificationTimes.once());
    client.verify(patch);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_repository_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1").withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);
    PipelineUpdate updates = PipelineUpdate.builder().repositoryId("2").build();
    PipelineImpl result = (PipelineImpl) underTest.update("1", "1", updates);
    assertEquals("2", result.getPhases().get(1).getRepositoryId(), "Branch was set correctly.");
    client.verify(get, VerificationTimes.once());
    client.verify(patch);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void update_via_pipeline(@Mock Pipeline mock) throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1");
    client.when(get).respond(response().withBody(GET_BODY));

    HttpRequest patch = request().withPath("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/pipeline/1")
        .withContentType(MediaType.APPLICATION_JSON);
    client.when(patch).respond(this::handleGoodPatch);
    PipelineUpdate updates = PipelineUpdate.builder().branch("newbranch").repositoryId("2").build();
    PipelineImpl result = (PipelineImpl) new PipelineImpl(mock, underTest, null).update(updates);

    assertEquals("newbranch", result.getPhases().get(1).getBranch(), "Branch was set correctly.");
    assertEquals("2", result.getPhases().get(1).getRepositoryId(), "Branch was set correctly.");

    client.verify(get, VerificationTimes.once());
    client.verify(patch);
    client.clear(get);
    client.clear(patch);
  }

  @Test
  void invalidate_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/cache");
    client.when(delete).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.invalidateCache("1", "1"), "Exception thrown");
    assertEquals("Cannot invalidate pipeline cache: %s/api/program/1/pipeline/1/cache (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct");

    client.verify(delete);
    client.clear(delete);
  }

  @Test
  void invalidate_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/cache");
    client.when(delete).respond(response().withStatusCode(ACCEPTED_202.code()));
    underTest.invalidateCache("1", "1");
    client.verify(delete, VerificationTimes.once());
    client.clear(delete);
  }

  @Test
  void invalidate_success_via_pipeline(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");

    HttpRequest delete = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/cache");
    client.when(delete).respond(response().withStatusCode(ACCEPTED_202.code()));
    new PipelineImpl(mock, underTest, null).invalidateCache();
    client.verify(delete, VerificationTimes.once());
    client.clear(delete);
  }

  @Test
  void listVariables_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/variables");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getVariables("1", "1"), "Exception thrown.");
    assertEquals("Cannot list pipeline variables: %s/api/program/1/pipeline/1/variables (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listVariables_empty(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/variables");

    com.adobe.aio.cloudmanager.Pipeline pipeline = new PipelineImpl(mock, underTest, null);
    client.when(list).respond(response().withBody(json("{}")));
    assertTrue(pipeline.getVariables().isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(pipeline.getVariables().isEmpty());
    client.verify(list);
    client.clear(list);

    client.when(list).respond(response().withBody(json("{ \"_embedded\": { \"variables\": [] } }")));
    assertTrue(pipeline.getVariables().isEmpty());
    client.verify(list);
    client.clear(list);
  }

  @Test
  void listVariables_success(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipeline/1/variables");
    client.when(list).respond(response().withBody(LIST_VARIABLES_BODY));
    Set<Variable> variables = new PipelineImpl(mock, underTest, null).getVariables();
    assertEquals(2, variables.size(), "Correct response");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void setVariables_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest patch = request().withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withHeader("Content-Type", "application/json")
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\", \"type\": \"string\" }, { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" } ]"));
    client.when(patch).respond(
        response()
            .withStatusCode(BAD_REQUEST_400.code())
            .withHeader("Content-Type", "application/problem+json")
            .withBody(json("{ \"type\" : \"http://ns.adobe.com/adobecloud/validation-exception\", \"errors\": [ \"some error\" ] }"))
    );
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).build();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setVariables("1", "1", var1, var2), "Exception thrown.");
    assertEquals("Cannot set pipeline variables: %s/api/program/1/pipeline/1/variables (400 Bad Request) - Validation Error(s): some error.".formatted(baseUrl), exception.getMessage(), "Message was correct");
    client.verify(patch);
    client.clear(patch);
  }

  @Test
  void setVariables_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest patch = request().withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withHeader("Content-Type", "application/json")
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\", \"type\": \"string\" }, { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" } ]"));
    client.when(patch).respond(response().withStatusCode(NOT_FOUND_404.code()));
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).build();

    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.setVariables("1", "1", var1, var2), "Exception thrown.");
    assertEquals("Cannot set pipeline variables: %s/api/program/1/pipeline/1/variables (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct");
    client.verify(patch);
    client.clear(patch);
  }

  // I have no idea how this case would be possible, but it was in the original impl, so i'm keeping it, and therefore need a test for it.
  @Test
  void setVariables_empty(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest patch = request().withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withHeader("Content-Type", "application/json")
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\", \"type\": \"string\" }, { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" } ]"));
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).build();

    com.adobe.aio.cloudmanager.Pipeline pipeline = new PipelineImpl(mock, underTest, null);
    client.when(patch).respond(response().withBody(json("{}")));
    assertTrue(pipeline.setVariables(var1, var2).isEmpty());
    client.verify(patch);
    client.clear(patch);

    client.when(patch).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(pipeline.setVariables(var1, var2).isEmpty());
    client.verify(patch);
    client.clear(patch);

    client.when(patch).respond(response().withBody(json("{ \"_embedded\": { \"variables\": [] } }")));
    assertTrue(pipeline.setVariables(var1, var2).isEmpty());
    client.verify(patch);
    client.clear(patch);
  }

  @Test
  void setVariables_success(@Mock Pipeline mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest patch = request().withMethod("PATCH")
        .withHeader(API_KEY_HEADER, sessionId)
        .withHeader("Content-Type", "application/json")
        .withPath("/api/program/1/pipeline/1/variables")
        .withBody(json("[ { \"name\": \"foo\", \"value\": \"bar\", \"type\": \"string\" }, { \"name\": \"secretFoo\", \"value\": \"secretBar\", \"type\": \"secretString\" } ]"));
    client.when(patch).respond(response().withBody(LIST_VARIABLES_BODY));
    Variable var1 = Variable.builder().name("foo").value("bar").type(Variable.Type.STRING).build();
    Variable var2 = Variable.builder().name("secretFoo").value("secretBar").type(Variable.Type.SECRET).build();

    Set<Variable> variables = new PipelineImpl(mock, underTest, null).setVariables(var1, var2);
    assertEquals(2, variables.size(), "Correct response");
    client.verify(patch);
    client.clear(patch);
  }

  @Test
  void status() {
    assertEquals(com.adobe.aio.cloudmanager.Pipeline.Status.valueOf("WAITING"), com.adobe.aio.cloudmanager.Pipeline.Status.WAITING);
    assertEquals(com.adobe.aio.cloudmanager.Pipeline.Status.IDLE.name(), com.adobe.aio.cloudmanager.Pipeline.Status.IDLE.toString());
  }

  @Test
  void list_predicates() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/pipelines");
    client.when(list).respond(response().withBody(LIST_BODY));

    Collection<com.adobe.aio.cloudmanager.Pipeline> busy = underTest.list("1", com.adobe.aio.cloudmanager.Pipeline.IS_BUSY);
    assertEquals(2, busy.size());
    Collection<com.adobe.aio.cloudmanager.Pipeline> named = underTest.list("1", new com.adobe.aio.cloudmanager.Pipeline.NamePredicate("test1"));
    assertEquals(1, named.size());
    Collection<com.adobe.aio.cloudmanager.Pipeline> id = underTest.list("1", new com.adobe.aio.cloudmanager.Pipeline.IdPredicate("4"));
    assertEquals(1, id.size());

    client.verify(list, VerificationTimes.exactly(3));
    client.clear(list);
  }
}
