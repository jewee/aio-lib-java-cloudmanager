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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import com.adobe.aio.cloudmanager.ApiBuilder;
import com.adobe.aio.cloudmanager.CloudManagerApiException;
import com.adobe.aio.cloudmanager.Constants;
import com.adobe.aio.cloudmanager.Pipeline;
import com.adobe.aio.cloudmanager.PipelineUpdate;
import com.adobe.aio.cloudmanager.Program;
import com.adobe.aio.cloudmanager.Variable;
import com.adobe.aio.cloudmanager.impl.FeignUtil;
import com.adobe.aio.workspace.Workspace;
import feign.Headers;
import feign.Param;
import feign.RequestLine;
import com.adobe.aio.cloudmanager.PipelineApi;
import com.adobe.aio.cloudmanager.PipelineExecutionApi;
import com.adobe.aio.cloudmanager.impl.VariableImpl;
import com.adobe.aio.cloudmanager.impl.generated.PipelineList;
import com.adobe.aio.cloudmanager.impl.generated.PipelinePhase;
import com.adobe.aio.cloudmanager.impl.generated.VariableList;

public class PipelineApiImpl implements PipelineApi {

  private final FeignApi api;
  private final PipelineExecutionApi executionApi;

  public PipelineApiImpl(Workspace workspace, URL url) {
    String baseUrl = url == null ? Constants.CLOUD_MANAGER_URL : url.toString();
    api = FeignUtil.getBuilder(workspace).errorDecoder(new ExceptionDecoder()).target(FeignApi.class, baseUrl);
    try {
      executionApi = new ApiBuilder<>(PipelineExecutionApi.class).workspace(workspace).url(new URL(baseUrl)).build();
    } catch (CloudManagerApiException | MalformedURLException e) {
      // This shouldn't be possible to reach.
      throw new RuntimeException(e);
    }
  }

  @Override
  public Collection<Pipeline> list(String programId) throws CloudManagerApiException {
    return list(programId, p -> true);
  }

  @Override
  public Collection<Pipeline> list(Program program) throws CloudManagerApiException {
    return list(program.getId());
  }

  @Override
  public PipelineImpl get(String programId, String pipelineId) throws CloudManagerApiException {
    return new PipelineImpl(api.get(programId, pipelineId), this, executionApi);
  }

  @Override
  public void delete(String programId, String pipelineId) throws CloudManagerApiException {
    api.delete(programId, pipelineId);
  }

  @Override
  public void delete(Pipeline pipeline) throws CloudManagerApiException {
    api.delete(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public Pipeline update(String programId, String pipelineId, PipelineUpdate updates) throws CloudManagerApiException {
    PipelineImpl original = get(programId, pipelineId);

    PipelinePhase buildPhase = original.getPhases().stream()
        .filter(p -> PipelinePhase.TypeEnum.BUILD == p.getType())
        .findFirst()
        .orElseThrow(() -> new CloudManagerApiException("Pipeline %s does not appear to have a build phase.".formatted(pipelineId)));

    if (updates.getBranch() != null) {
      buildPhase.setBranch(updates.getBranch());
    }

    if (updates.getRepositoryId() != null) {
      buildPhase.setRepositoryId(updates.getRepositoryId());
    }
    com.adobe.aio.cloudmanager.impl.generated.Pipeline toUpdate = new com.adobe.aio.cloudmanager.impl.generated.Pipeline();
    toUpdate.getPhases().add(buildPhase);
    return new PipelineImpl(api.update(programId, pipelineId, toUpdate), this, executionApi);
  }

  @Override
  public Pipeline update(Pipeline pipeline, PipelineUpdate updates) throws CloudManagerApiException {
    return update(pipeline.getProgramId(), pipeline.getId(), updates);
  }

  @Override
  public void invalidateCache(String programId, String pipelineId) throws CloudManagerApiException {
    api.invalidateCache(programId, pipelineId);
  }

  @Override
  public void invalidateCache(Pipeline pipeline) throws CloudManagerApiException {
    invalidateCache(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public Set<Variable> getVariables(String programId, String pipelineId) throws CloudManagerApiException {
    VariableList list = api.getVariables(programId, pipelineId);
    return list.getEmbedded() == null || list.getEmbedded().getVariables() == null ?
        Collections.emptySet() :
        list.getEmbedded().getVariables().stream().map(VariableImpl::new).collect(Collectors.toSet());
  }

  @Override
  public Set<Variable> getVariables(Pipeline pipeline) throws CloudManagerApiException {
    return getVariables(pipeline.getProgramId(), pipeline.getId());
  }

  @Override
  public Set<Variable> setVariables(String programId, String pipelineId, Variable... variables) throws CloudManagerApiException {
    List<com.adobe.aio.cloudmanager.impl.generated.Variable> toSet =
        Arrays.stream(variables).map((v) -> new com.adobe.aio.cloudmanager.impl.generated.Variable()
                .name(v.getName())
                .value(v.getValue())
                .type(com.adobe.aio.cloudmanager.impl.generated.Variable.TypeEnum.fromValue(v.getVarType().getValue())))
            .collect(Collectors.toList());
    VariableList list = api.setVariables(programId, pipelineId, toSet);
    return list.getEmbedded() == null || list.getEmbedded().getVariables() == null ?
        Collections.emptySet() :
        list.getEmbedded().getVariables().stream().map(VariableImpl::new).collect(Collectors.toSet());
  }

  @Override
  public Set<Variable> setVariables(Pipeline pipeline, Variable... variables) throws CloudManagerApiException {
    return setVariables(pipeline.getProgramId(), pipeline.getId(), variables);
  }

  @Override
  public Collection<Pipeline> list(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    return listDetails(programId, predicate);
  }

  private Collection<Pipeline> listDetails(String programId, Predicate<Pipeline> predicate) throws CloudManagerApiException {
    PipelineList list = api.list(programId);
    if (list.getEmbedded() == null || list.getEmbedded().getPipelines() == null) {
      throw new CloudManagerApiException("Cannot find pipelines for program %s.".formatted(programId));
    }

    return list.getEmbedded().getPipelines().stream().map(p -> new PipelineImpl(p, this, executionApi)).filter(predicate).collect(Collectors.toList());
  }

  private interface FeignApi {
    @RequestLine("GET /api/program/{programId}/pipelines")
    PipelineList list(@Param("programId") String programId) throws CloudManagerApiException;

    @RequestLine("GET /api/program/{programId}/pipeline/{id}")
    com.adobe.aio.cloudmanager.impl.generated.Pipeline get(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/pipeline/{id}")
    void delete(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PATCH /api/program/{programId}/pipeline/{id}")
    @Headers("Content-Type: application/json")
    com.adobe.aio.cloudmanager.impl.generated.Pipeline update(@Param("programId") String programId, @Param("id") String id, com.adobe.aio.cloudmanager.impl.generated.Pipeline update) throws CloudManagerApiException;

    @RequestLine("DELETE /api/program/{programId}/pipeline/{id}/cache")
    void invalidateCache(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("GET api/program/{programId}/pipeline/{id}/variables")
    VariableList getVariables(@Param("programId") String programId, @Param("id") String id) throws CloudManagerApiException;

    @RequestLine("PATCH api/program/{programId}/pipeline/{id}/variables")
    @Headers("Content-Type: application/json")
    VariableList setVariables(@Param("programId") String programId, @Param("id") String id, List<com.adobe.aio.cloudmanager.impl.generated.Variable> variables) throws CloudManagerApiException;
  }
}
