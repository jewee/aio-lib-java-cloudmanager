package com.adobe.aio.cloudmanager.impl.environment;

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

import java.io.File;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import com.adobe.aio.cloudmanager.Region;
import com.adobe.aio.cloudmanager.impl.generated.Environment;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.EnvironmentApi;
import com.adobe.aio.cloudmanager.EnvironmentLog;
import com.adobe.aio.cloudmanager.LogOption;
import com.adobe.aio.cloudmanager.RegionDeployment;
import com.adobe.aio.cloudmanager.Variable;
import com.adobe.aio.cloudmanager.impl.generated.HalLink;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Delegate;

@ToString
@EqualsAndHashCode(callSuper = false)
public class EnvironmentImpl extends Environment implements com.adobe.aio.cloudmanager.Environment {
  private static final long serialVersionUID = 1L;

  @Delegate
  private final Environment delegate;
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private final EnvironmentApi client;

  public EnvironmentImpl(Environment delegate, EnvironmentApi client) {
    this.delegate = delegate;
    this.client = client;
  }

  @Override
  public Type getEnvType() {
    return Type.valueOf(getType().name());
  }

  @Override
  public Collection<LogOption> getLogOptions() {
    return getAvailableLogOptions().stream().map(LogOptionImpl::new).collect(Collectors.toList());
  }

  @Override
  public void delete() throws CloudManagerApiException {
    delete(false);
  }

  @Override
  public void delete(boolean ignoreFailure) throws CloudManagerApiException {
    client.delete(this, ignoreFailure);
  }

  @Override
  public Collection<EnvironmentLog> listLogs(LogOption option, int days) throws CloudManagerApiException {
    return client.listLogs(this, option, days);
  }

  @Override
  public String getLogDownloadUrl(LogOption option, LocalDate date) throws CloudManagerApiException {
    return client.getLogDownloadUrl(this, option, date);
  }

  @Override
  public Collection<RegionDeployment> listRegionDeployments() throws CloudManagerApiException {
    return client.listRegionDeployments(this);
  }

  @Override
  public void addRegionDeployment(Region region) throws CloudManagerApiException {
    client.createRegionDeployments(this, region);
  }

  @Override
  public void removeRegionDeployment(Region region) throws CloudManagerApiException {
    client.removeRegionDeployments(this, region);
  }

  @Override
  public Set<Variable> getVariables() throws CloudManagerApiException {
    return client.getVariables(this);
  }

  @Override
  public Set<Variable> setVariables(Variable... variables) throws CloudManagerApiException {
    return client.setVariables(this, variables);
  }

  @Override
  public void reset() throws CloudManagerApiException {
    if (getEnvType() == Type.RDE) {
      client.resetRde(this);
    }
  }

  @Override
  public String getDeveloperConsoleUrl() throws CloudManagerApiException {
    HalLink link = delegate.getLinks().getHttpnsAdobeComadobecloudreldeveloperConsole();
    if (link == null) {
      throw new CloudManagerApiException("Environment %s [%s] does not appear to support Developer Console.".formatted(getId(), getName()));
    } else {
      return link.getHref();
    }
  }

  @Override
  public Collection<EnvironmentLog> downloadLogs(LogOption logOption, int days, File dir) throws CloudManagerApiException {
    return client.downloadLogs(this, logOption, days, dir);
  }
}
