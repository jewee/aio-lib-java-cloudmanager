package com.adobe.aio.cloudmanager;

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

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import jakarta.validation.constraints.NotNull;

import com.adobe.aio.cloudmanager.exception.PipelineRunningException;

/**
 * Pipeline Execution API
 * <p>
 * See the <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#tag/Pipeline-Execution">Pipeline Execution API documentation</a>.
 * See the <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/api/#tag/Execution-Artifacts">Execution Artifact API documentation</a>.
 * See the <a href="https://developer.adobe.com/experience-cloud/cloud-manager/reference/events/">Event Definition documentation</a>
 * 
 */
public interface PipelineExecutionApi {

  /**
   * Get the current execution of the specified pipeline, if one exists.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id of to find the execution
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Optional<PipelineExecution> getCurrent(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Get the current execution of the specified pipeline, if one exists.
   *
   * @param pipeline the pipeline reference
   * @return An optional containing the execution details of the pipeline
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Optional<PipelineExecution> getCurrent(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * Start the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrent(String, String)} may not return a result.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the id of the pipeline
   * @return the new execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution start(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * Start the specified pipeline.
   * <p>
   * Note: This API call may return before the requested action takes effect. i.e. The Pipelines are <i>scheduled</i> to start once called. However, an immediate subsequent call to {@link #getCurrent(String, String)} may not return a result.
   *
   * @param pipeline the {@link Pipeline} to start
   * @return the new execution
   * @throws PipelineRunningException if the pipeline is already running
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution start(@NotNull Pipeline pipeline) throws PipelineRunningException, CloudManagerApiException;

  /**
   * Get the specified execution of the pipeline.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the pipeline id
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution get(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Returns the specified execution of the pipeline.
   *
   * @param pipeline    the pipeline context for the execution
   * @param executionId the id of the execution to retrieve
   * @return the execution details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecution get(@NotNull Pipeline pipeline, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Get the specified action step for the pipeline execution.
   *
   * @param execution the execution context
   * @param action    the step state action (see {@link StepAction})
   * @return the step state details
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionStepState getStepState(@NotNull PipelineExecution execution, @NotNull StepAction action) throws CloudManagerApiException;

  /**
   * Advance the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be advanced
   * @throws CloudManagerApiException when any error occurs
   */
  void advance(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Advance the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be advanced
   * @throws CloudManagerApiException when any error occurs
   */
  void advance(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Cancel the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param programId   the program id context of the pipeline
   * @param pipelineId  the id of the pipeline to cancel
   * @param executionId the execution id to be canceled
   * @throws CloudManagerApiException when any error occurs
   */
  void cancel(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId) throws CloudManagerApiException;

  /**
   * Cancel the execution of the specified pipeline execution, if in an appropriate state.
   *
   * @param execution the execution to be canceled
   * @throws CloudManagerApiException when any error occurs
   */
  void cancel(@NotNull PipelineExecution execution) throws CloudManagerApiException;

  /**
   * Get the fully qualified URL to the specified step's log file.
   *
   * @param programId   the program id of the pipeline context
   * @param pipelineId  the pipeline id for the execution context
   * @param executionId the execution id
   * @param action      the execution step action for the log
   * @return the log file URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getStepLogDownloadUrl(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId, @NotNull StepAction action) throws CloudManagerApiException;

  /**
   * Get the fully qualified URL to the specified step's log file.
   *
   * @param programId   the program id of the pipeline context
   * @param pipelineId  the pipeline id for the execution context
   * @param executionId the execution id
   * @param action      the execution step action context
   * @param name        custom log file name
   * @return the log file URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getStepLogDownloadUrl(@NotNull String programId, @NotNull String pipelineId, @NotNull String executionId, @NotNull StepAction action, @NotNull String name) throws CloudManagerApiException;

  /**
   * Get the fully qualified URL to the specified step's log file.
   *
   * @param execution the execution context
   * @param action    the execution step action context
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getStepLogDownloadUrl(@NotNull PipelineExecution execution, @NotNull StepAction action) throws CloudManagerApiException;

  /**
   * Get the fully qualified URL to the specified log file referenced by name, within the step.
   *
   * @param execution the execution context
   * @param action    the execution step action context
   * @param name      custom log file name
   * @return the log file download URL
   * @throws CloudManagerApiException when any error occurs
   */
  String getStepLogDownloadUrl(@NotNull PipelineExecution execution, @NotNull StepAction action, @NotNull String name) throws CloudManagerApiException;

  /**
   * Get the metrics for the specified execution and step, if any.
   *
   * @param execution the execution context
   * @param action    the action step context
   * @return the metrics for the execution
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Metric> getQualityGateResults(@NotNull PipelineExecution execution, @NotNull StepAction action) throws CloudManagerApiException;

  /**
   * List executions of the specified pipeline, using the default limit and starting at 0.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull String programId, @NotNull String pipelineId) throws CloudManagerApiException;

  /**
   * List executions of the specified pipeline, using the default limit and starting at 0.
   *
   * @param pipeline the pipeline context
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull Pipeline pipeline) throws CloudManagerApiException;

  /**
   * List executions of the specified pipeline, using the specified limit and starting at 0.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id
   * @param limit      the number of executions to return
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull String programId, @NotNull String pipelineId, int limit) throws CloudManagerApiException;

  /**
   * List executions of the specified pipeline, using the specified limit and starting at 0.
   *
   * @param pipeline the pipeline context
   * @param limit    the number of executions to return
   * @return list of executions, if any
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull Pipeline pipeline, int limit) throws CloudManagerApiException;

  /**
   * List executions of the specified pipeline, using the specified limit and starting at the specified position.
   *
   * @param programId  the program id context of the pipeline
   * @param pipelineId the pipeline id
   * @param start      the starting position of the results
   * @param limit      the number of executions to return
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull String programId, @NotNull String pipelineId, int start, int limit) throws CloudManagerApiException;

  /**
   * List executions of the specified pipeline, using the specified limit and starting at the specified position.
   *
   * @param pipeline the pipeline context
   * @param start    the starting position of the results
   * @param limit    the number of executions to return
   * @return list of executions
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<PipelineExecution> list(@NotNull Pipeline pipeline, int start, int limit) throws CloudManagerApiException;

  /**
   * List all artifacts associated with the specified step.
   *
   * @param step the pipeline execution step context
   * @return list of artifacts
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  Collection<Artifact> listArtifacts(@NotNull PipelineExecutionStepState step) throws CloudManagerApiException;

  /**
   * Get the fully qualified URL to the artifact file.
   *
   * @param step       the pipeline execution step context
   * @param artifactId the id of the artifact
   * @return the artifact file download url
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  String getArtifactDownloadUrl(@NotNull PipelineExecutionStepState step, String artifactId) throws CloudManagerApiException;


  // Event handling

  /**
   * Parse the provided string into an Event instance. Use this API when polling the journal events.
   *
   * @param eventBody the body to parse into the event
   * @return an instance of a {@link PipelineExecutionEvent}
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionEvent parseEvent(@NotNull String eventBody) throws CloudManagerApiException;

  /**
   * Parse the provided body into an Event instance, validating the event as part of the parsing process. Use this API when receiving webhook events.
   *
   * @param eventBody     the body to parse into the event
   * @param requestHeader the request headers sent with the Event
   * @return an instance of a {@link PipelineExecutionEvent}
   * @throws CloudManagerApiException when any error occurs
   */
  @NotNull
  PipelineExecutionEvent parseEvent(@NotNull String eventBody, @NotNull Map<String, String> requestHeader) throws CloudManagerApiException;
}
