package com.adobe.aio.cloudmanager.impl.tenant;

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
import java.util.Collection;
import java.util.UUID;

import com.adobe.aio.ims.feign.AuthInterceptor;
import com.adobe.aio.cloudmanager.ApiBuilder;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Tenant;
import com.adobe.aio.cloudmanager.TenantApi;
import com.adobe.aio.cloudmanager.impl.AbstractApiTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockserver.model.HttpRequest;

import static com.adobe.aio.util.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;
import static org.mockserver.model.HttpRequest.*;
import static org.mockserver.model.HttpResponse.*;
import static org.mockserver.model.HttpStatusCode.*;
import static org.mockserver.model.JsonBody.*;

public class TenantTest extends AbstractApiTest {

  private TenantApi underTest;

  @BeforeEach
  void before() throws Exception {
    try (MockedConstruction<AuthInterceptor.Builder> ignored = mockConstruction(AuthInterceptor.Builder.class,
        (mock, mockContext) -> {
          when(mock.workspace(workspace)).thenReturn(mock);
          when(mock.build()).thenReturn(authInterceptor);
        }
    )) {
      underTest = new ApiBuilder<>(TenantApi.class).workspace(workspace).url(new URL(baseUrl)).build();
    }
  }

  @Test
  void list_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::list, "Exception thrown for 404");
    assertEquals("Cannot retrieve tenants: %s/api/tenants (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_403() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"error_code\":\"1234\", \"message\":\"some message\" }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::list, "Exception thrown for 403");
    assertEquals("Cannot retrieve tenants: %s/api/tenants (403 Forbidden) - Detail: some message (Code: 1234).".formatted(baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_403_errorMessageOnly() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"message\":\"some message\" }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::list, "Exception thrown for 403");
    assertEquals("Cannot retrieve tenants: %s/api/tenants (403 Forbidden) - Detail: some message.".formatted(baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_failure_403_errorCodeOnly() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withStatusCode(FORBIDDEN_403.code()).withBody(json("{ \"error_code\":\"1234\" }")));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, underTest::list, "Exception thrown for 403");
    assertEquals("Cannot retrieve tenants: %s/api/tenants (403 Forbidden).".formatted(baseUrl), exception.getMessage(), "Message was correct");
    client.verify(list);
    client.clear(list);
  }


  @Test
  void list_success_empty() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withBody(json("{}")));
    Collection<Tenant> tenants = underTest.list();
    assertEquals(0, tenants.size(), "Correct length of tenant list");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void list_success() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest list = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenants");
    client.when(list).respond(response().withBody(loadBodyJson("tenant/list.json")));
    Collection<Tenant> tenants = underTest.list();
    assertEquals(1, tenants.size(), "Correct length of tenant list");
    client.verify(list);
    client.clear(list);
  }

  @Test
  void get_failure_404() {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1");
    client.when(get).respond(response().withStatusCode(NOT_FOUND_404.code()));
    CloudManagerApiException exception = assertThrows(CloudManagerApiException.class, () -> underTest.get("1"), "Exception thrown for 404");
    assertEquals("Cannot retrieve tenant: %s/api/tenant/1 (404 Not Found).".formatted(baseUrl), exception.getMessage(), "Message was correct");
    client.verify(get);
    client.clear(get);
  }

  @Test
  void get_success() throws Exception {
    String sessionId = UUID.randomUUID().toString();
    when(workspace.getApiKey()).thenReturn(sessionId);
    HttpRequest get = request().withMethod("GET").withHeader(API_KEY_HEADER, sessionId).withPath("/api/tenant/1");
    client.when(get).respond(response().withBody(loadBodyJson("tenant/get.json")));
    assertNotNull(underTest.get("1"), "Tenant retrieved");
    client.verify(get);
    client.clear(get);
  }
}
