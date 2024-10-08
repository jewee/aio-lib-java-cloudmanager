package com.adobe.aio.cloudmanager.impl.content;

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

import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.adobe.aio.cloudmanager.impl.generated.ContentFlow;
import com.adobe.aio.cloudmanager.impl.generated.ContentSet;
import com.adobe.aio.ims.feign.AuthInterceptor;
import com.adobe.aio.cloudmanager.ApiBuilder;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.ContentSetApi;
import com.adobe.aio.cloudmanager.Environment;
import com.adobe.aio.cloudmanager.impl.AbstractApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.JsonBody;

import static com.adobe.aio.util.Constants.*;
import static com.adobe.aio.cloudmanager.ContentSet.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class ContentSetTest extends AbstractApiTest {

  private static final JsonBody GET_SET_BODY = loadBodyJson("content/set/get.json");
  public static final JsonBody LIST_SET_BODY = loadBodyJson("content/set/list.json");
  private static final JsonBody GET_FLOW_BODY = loadBodyJson("content/flow/get.json");
  public static final JsonBody LIST_FLOW_BODY = loadBodyJson("content/flow/list.json");

  private ContentSetApiImpl underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = (ContentSetApiImpl) new ApiBuilder<>(ContentSetApi.class).workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  @Test
  void list_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSets");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1"), "Exception thrown");
    assertEquals("Cannot list content sets: %s/api/program/1/contentSets (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSets");

    client.when(get).respond(response().withBody(json("{}")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{ \"_embedded\": { \"contentSets\": [] } }")));
    assertTrue(underTest.list("1").isEmpty());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSets");
    client.when(get).respond(response().withBody(LIST_SET_BODY));
    List<com.adobe.aio.cloudmanager.ContentSet> list = (List<com.adobe.aio.cloudmanager.ContentSet>) underTest.list("1");
    assertEquals(2, list.size(), "List correct.");
    com.adobe.aio.cloudmanager.ContentSet cs = list.get(0);
    assertEquals(1, cs.getPathDefinitions().size());
    List<PathDefinition> pds = (List<PathDefinition>) cs.getPathDefinitions();
    PathDefinition pd = pds.get(0);
    assertEquals("/content/foo", pd.getPath());
    assertEquals(1, pd.getExcluded().size());

    cs = list.get(1);
    assertEquals(1, cs.getPathDefinitions().size());
    pds = (List<PathDefinition>) cs.getPathDefinitions();
    pd = pds.get(0);
    assertEquals("/content/bar", pd.getPath());
    assertEquals(2, pd.getExcluded().size());

    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_limit_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1", 10), "Exception thrown");
    assertEquals("Cannot list content sets: %s/api/program/1/contentSets?start=0&limit=10 (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(LIST_SET_BODY));
    List<com.adobe.aio.cloudmanager.ContentSet> list = (List<com.adobe.aio.cloudmanager.ContentSet>) underTest.list("1", 10);
    assertEquals(2, list.size(), "List correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_start_limit_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.list("1", 10, 10), "Exception thrown");
    assertEquals("Cannot list content sets: %s/api/program/1/contentSets?start=10&limit=10 (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_start_limit_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(json("{}")));
    assertTrue(underTest.list("1", 10, 10).isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{ \"_embedded\": {} }")));
    assertTrue(underTest.list("1", 10, 10).isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{ \"_embedded\": { \"contentSets\": [] } }")));
    assertTrue(underTest.list("1", 10, 10).isEmpty());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void list_start_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(LIST_SET_BODY));
    List<com.adobe.aio.cloudmanager.ContentSet> list = (List<com.adobe.aio.cloudmanager.ContentSet>) underTest.list("1", 10, 10);
    assertEquals(2, list.size(), "List correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void create_failure_400() {
    List<PathDefinition> pds = new ArrayList<>();
    Set<String> exclusions = new HashSet<>();
    exclusions.add("/content/foo/bar");
    exclusions.add("/content/foo/foo");
    pds.add(new PathDefinition("/content/foo", exclusions));

    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POSt")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.create("1", "Test", "Description", pds), "Exception thrown.");
    assertEquals("Cannot create content set: %s/api/program/1/contentSets (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void create_success() throws CloudManagerApiException {
    List<PathDefinition> pds = new ArrayList<>();
    Set<String> exclusions = new HashSet<>();
    exclusions.add("/content/foo/bar");
    exclusions.add("/content/foo/foo");
    pds.add(new PathDefinition("/content/foo", exclusions));

    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSets")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(post).respond(response().withBody(GET_SET_BODY));
    com.adobe.aio.cloudmanager.ContentSet cs = underTest.create("1", "Test", "Description", pds);
    assertNotNull(cs);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(post);
    client.clear(post);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1", "1"), "Exception thrown.");
    assertEquals("Cannot get content set: %s/api/program/1/contentSet/1 (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));
    com.adobe.aio.cloudmanager.ContentSet cs = underTest.get("1", "1");
    assertNotNull(cs);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void update_failure_400() {
    List<PathDefinition> pds = new ArrayList<>();
    Set<String> exclusions = new HashSet<>();
    exclusions.add("/content/foo/bar");
    exclusions.add("/content/foo/foo");
    pds.add(new PathDefinition("/content/foo", exclusions));

    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.update("1", "1", "Test", "Description", pds), "Exception thrown.");
    assertEquals("Cannot update content set: %s/api/program/1/contentSet/1 (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void update_success() throws CloudManagerApiException {
    List<PathDefinition> pds = new ArrayList<>();
    Set<String> exclusions = new HashSet<>();
    exclusions.add("/content/foo/bar");
    exclusions.add("/content/foo/foo");
    pds.add(new PathDefinition("/content/foo", exclusions));

    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));
    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withBody(GET_SET_BODY));
    com.adobe.aio.cloudmanager.ContentSet updated = underTest.update("1", "1", "Test", "Description", pds);
    assertEquals(2, updated.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void update_success_name(@Mock ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));

    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Updated\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withBody(GET_SET_BODY));

    com.adobe.aio.cloudmanager.ContentSet cs = new ContentSetImpl(mock, underTest);
    cs.update("Updated", null, null);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void update_success_description(@Mock ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));

    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Updated\", \"paths\": [ { \"path\": \"/content/foo\", \"excluded\": [\"/content/foo/bar\", \"/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withBody(GET_SET_BODY));

    com.adobe.aio.cloudmanager.ContentSet cs = new ContentSetImpl(mock, underTest);
    cs.update(null, "Updated", null);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void update_success_paths(@Mock ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(get).respond(response().withBody(GET_SET_BODY));

    HttpRequest put = request()
        .withMethod("PUT")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentSet/1")
        .withBody(json("{ \"name\": \"Test\", \"description\":  \"Description\", \"paths\": [ { \"path\": \"/not/content/foo\", \"excluded\": [\"/not/content/foo/bar\", \"/not/content/foo/foo\"] } ] }"));
    client.when(put).respond(response().withBody(GET_SET_BODY));

    Set<String> exclusions = new HashSet<>();
    exclusions.add("/not/content/foo/bar");
    exclusions.add("/not/content/foo/foo");
    PathDefinition pd = new PathDefinition("/not/content/foo", exclusions);
    List<PathDefinition> paths = new ArrayList<>();
    paths.add(pd);

    com.adobe.aio.cloudmanager.ContentSet cs = new ContentSetImpl(mock, underTest);
    cs.update(null, null, paths);
    assertEquals(2, cs.getPathDefinitions().stream().findFirst().get().getExcluded().size());
    client.verify(get, put);
    client.clear(get);
    client.clear(put);
  }

  @Test
  void delete_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(del).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.delete("1", "1"), "Exception thrown.");
    assertEquals("Cannot delete content set: %s/api/program/1/contentSet/1 (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void delete_success(@Mock ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentSet/1");
    client.when(del).respond(response().withBody(GET_SET_BODY));
    new ContentSetImpl(mock, underTest).delete();
    client.verify(del);
    client.clear(del);
  }

  @Test
  void listFlows_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlows");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listFlows("1"), "Exception thrown");
    assertEquals("Cannot list content flows: %s/api/program/1/contentFlows (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlows");

    client.when(get).respond(response().withBody(json("{}")));
    assertTrue(underTest.listFlows("1").isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{\"_embedded\": {} }")));
    assertTrue(underTest.listFlows("1").isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{\"_embedded\": { \"contentFlows\": [] } }")));
    assertTrue(underTest.listFlows("1").isEmpty());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlows");
    client.when(get).respond(response().withBody(LIST_FLOW_BODY));
    List<com.adobe.aio.cloudmanager.ContentFlow> list = (List<com.adobe.aio.cloudmanager.ContentFlow>) underTest.listFlows("1");
    assertEquals(2, list.size(), "List correct.");
    com.adobe.aio.cloudmanager.ContentFlow cf = list.get(0);

    assertEquals(Environment.Tier.AUTHOR, cf.getEnvironmentTier());
    assertEquals(com.adobe.aio.cloudmanager.ContentFlow.Status.COMPLETED, cf.getFlowStatus());
    com.adobe.aio.cloudmanager.ContentFlow.Results exportResults = cf.getExportResults();
    assertEquals("0", exportResults.getErrorCode());
    assertEquals("Success", exportResults.getMessage());
    assertEquals("20 Exported", exportResults.getDetails().get(0));
    assertEquals(exportResults, cf.getExportResults());


    com.adobe.aio.cloudmanager.ContentFlow.Results importResults = cf.getImportResults();
    assertEquals("0", importResults.getErrorCode());
    assertEquals("Success", importResults.getMessage());
    assertEquals("20 Imported", importResults.getDetails().get(0));
    assertEquals(importResults, cf.getImportResults());

    cf = list.get(1);
    assertEquals(com.adobe.aio.cloudmanager.ContentFlow.Status.IN_PROGRESS, cf.getFlowStatus());
    exportResults = cf.getExportResults();
    assertEquals("0", exportResults.getErrorCode());
    assertEquals("Running", exportResults.getMessage());
    assertEquals("5 Exported", exportResults.getDetails().get(0));
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_limit_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listFlows("1", 10), "Exception thrown");
    assertEquals("Cannot list content flows: %s/api/program/1/contentFlows?start=0&limit=10 (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "0")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(LIST_FLOW_BODY));
    List<com.adobe.aio.cloudmanager.ContentFlow> list = (List<com.adobe.aio.cloudmanager.ContentFlow>) underTest.listFlows("1", 10);
    assertEquals(2, list.size(), "List correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_start_limit_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.listFlows("1", 10, 10), "Exception thrown");
    assertEquals("Cannot list content flows: %s/api/program/1/contentFlows?start=10&limit=10 (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_start_limit_empty() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");

    client.when(get).respond(response().withBody(json("{}")));
    assertTrue(underTest.listFlows("1", 10, 10).isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{\"_embedded\": {} }")));
    assertTrue(underTest.listFlows("1", 10, 10).isEmpty());
    client.verify(get);
    client.clear(get);

    client.when(get).respond(response().withBody(json("{\"_embedded\": { \"contentFlows\": [] } }")));
    assertTrue(underTest.listFlows("1", 10, 10).isEmpty());
    client.verify(get);
    client.clear(get);
  }

  @Test
  void listFlows_start_limit_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request()
        .withMethod("GET")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/contentFlows")
        .withQueryStringParameter("start", "10")
        .withQueryStringParameter("limit", "10");
    client.when(get).respond(response().withBody(LIST_FLOW_BODY));
    List<com.adobe.aio.cloudmanager.ContentFlow> list = (List<com.adobe.aio.cloudmanager.ContentFlow>) underTest.listFlows("1", 10, 10);
    assertEquals(2, list.size(), "List correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void startFlow_failure_400() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/contentFlow")
        .withBody(json("{ \"contentSetId\": \"1\", \"destEnvironmentId\": \"2\", \"tier\": \"author\", \"includeACL\": true, \"destProgramId\": \"1\" }"));
    client.when(post).respond(response().withStatusCode(BAD_REQUEST_400.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startFlow("1", "1", "1", "2", true), "Exception thrown.");
    assertEquals("Cannot start content flow: %s/api/program/1/environment/1/contentFlow (400 Bad Request).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void startFlow_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/contentFlow")
        .withBody(json("{ \"contentSetId\": \"1\", \"destEnvironmentId\": \"2\", \"tier\": \"author\", \"includeACL\": true, \"destProgramId\": \"1\" }"));
    client.when(post).respond(response().withStatusCode(FORBIDDEN_403.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.startFlow("1", "1", "1", "2", true), "Exception thrown.");
    assertEquals("Cannot start content flow: %s/api/program/1/environment/1/contentFlow (403 Forbidden).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(post);
    client.clear(post);
  }

  @Test
  void startFlow_success(@Mock ContentSet mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest post = request()
        .withMethod("POST")
        .withHeader(API_KEY_HEADER, sessionId)
        .withPath("/api/program/1/environment/1/contentFlow")
        .withBody(json("{ \"contentSetId\": \"1\", \"destEnvironmentId\": \"2\", \"tier\": \"author\", \"includeACL\": false, \"destProgramId\": \"1\" }"));
    client.when(post).respond(response().withBody(GET_FLOW_BODY));
    com.adobe.aio.cloudmanager.ContentFlow cf = new ContentSetImpl(mock, underTest).startFlow("1", "2", false);
    assertNotNull(cf);
    client.verify(post);
    client.clear(post);
  }

  @Test
  void getFlow_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlow/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.getFlow("1", "1"), "Exception thrown.");
    assertEquals("Cannot get content flow: %s/api/program/1/contentFlow/1 (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void getFlow_success() throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlow/1");
    client.when(get).respond(response().withBody(GET_FLOW_BODY));
    com.adobe.aio.cloudmanager.ContentFlow cf = underTest.getFlow("1", "1");
    assertNotNull(cf);
    client.verify(get);
    client.clear(get);
  }

  @Test
  void cancelFlow_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlow/1");
    client.when(del).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.cancelFlow("1", "1"), "Exception thrown.");
    assertEquals("Cannot cancel content flow: %s/api/program/1/contentFlow/1 (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct.");
    client.verify(del);
    client.clear(del);
  }

  @Test
  void cancelFlow_success(@Mock ContentFlow mock) throws CloudManagerApiException {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    when(mock.getDestProgramId()).thenReturn("1");
    when(mock.getId()).thenReturn("1");
    HttpRequest del = request().withMethod("DELETE").withHeader(API_KEY_HEADER, sessionId).withPath("/api/program/1/contentFlow/1");
    client.when(del).respond(response().withBody(GET_FLOW_BODY));
    new ContentFlowImpl(mock, underTest).cancel();
    client.verify(del);
    client.clear(del);
  }

}
