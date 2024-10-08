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

import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.impl.exception.CloudManagerExceptionDecoder;
import feign.Response;
import lombok.Getter;

public class ExceptionDecoder extends CloudManagerExceptionDecoder {

  @Override
  public Exception decode(String methodKey, Response response) {
    ErrorType type = ErrorType.UNKNOWN;
    switch (methodKey) {
      case "FeignApi#list()": {
        type = ErrorType.LIST;
        break;
      }
      case "FeignApi#get(String)": {
        type = ErrorType.GET;
        break;
      }
    }
    return new CloudManagerApiException(type.message.formatted(getError(response)));
  }

  @Getter
  private enum ErrorType {
    LIST("Cannot retrieve tenants: %s."),
    GET("Cannot retrieve tenant: %s."),
    UNKNOWN("Tenant API Error: %s.");
    private final String message;

    ErrorType(String message) {
      this.message = message;
    }
  }
}
