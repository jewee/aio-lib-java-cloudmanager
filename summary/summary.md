<a id="top"></a>

<p style="font-size: 24px;"><img src="./qct-icons/transform-logo.svg" style="margin-right: 15px; vertical-align: middle;"></img><b>Code Transformation Summary by Amazon Q </b></p>
<p><img src="./qct-icons/transform-variables-dark.svg" style="margin-bottom: 1px; vertical-align: middle;"></img> Lines of code in your application: 23703 <p>
<p><img src="./qct-icons/transform-clock-dark.svg" style="margin-bottom: 1px; vertical-align: middle;"></img> Transformation duration: 39 min(s) <p>
<p><img src="./qct-icons/transform-dependencies-dark.svg" style="margin-bottom: 1px; vertical-align: middle;"></img> Planned dependencies replaced: 5 of 8 <p>
<p><img src="./qct-icons/transform-dependencyAnalyzer-dark.svg" style="margin-bottom: 1px; vertical-align: middle;"></img> Additional dependencies added: 12 <p>
<p><img src="./qct-icons/transform-smartStepInto-dark.svg" style="margin-bottom: 1px; vertical-align: middle;"></img> Planned deprecated code instances replaced: 0 of 0 <p>
<p><img src="./qct-icons/transform-listFiles-dark.svg" style="margin-bottom: 1px; vertical-align: middle;"></img> Files changed: 43 <p>
<p><img src="./qct-icons/transform-build-dark.svg" style="margin-bottom: 1px; vertical-align: middle;"></img> Build status in Java 17: <span style="color: #CCCC00">PARTIALLY_SUCCEEDED</span> <p>

### Table of Contents

1. <a href="#build-log-summary">Build log summary</a> 
1. <a href="#planned-dependencies-replaced">Planned dependencies replaced</a> 
1. <a href="#additional-dependencies-added">Additional dependencies added</a> 
1. <a href="#deprecated-code-replaced">Deprecated code replaced</a> 
1. <a href="#other-changes">Other changes</a> 
1. <a href="#all-files-changed">All files changed</a> 
1. <a href="#next-steps">Next steps</a> 


### Build log summary <a style="float:right; font-size: 14px;" href="#top">Scroll to top</a><a id="build-log-summary"></a>

Amazon Q could not build the upgraded code in Java 17. The following build log snippet that shows the errors Amazon Q encountered during the build log. To view the full build log, open [`buildCommandOutput.log`](./buildCommandOutput.log)

```
The Maven build failed due to compilation errors in generated source code related to missing Swagger annotations. The errors indicate that the Swagger/OpenAPI annotations like @Schema could not be found during compilation. This is likely due to a mismatch between the OpenAPI generator and runtime dependencies.
```


### Planned dependencies replaced <a style="float:right; font-size: 14px;" href="#top">Scroll to top</a><a id="planned-dependencies-replaced"></a>

Amazon Q updated the following dependencies that it identified in the transformation plan

| Dependency | Action | Previous version in Java 11 | Current version in Java 17 |
|--------------|--------|--------|--------|
| `io.swagger.core.v3:swagger-annotations` | Removed | 2.2.22 | - |
| `jakarta.validation:jakarta.validation-api` | Added | - | 3.1.0 |
| `javax.validation:validation-api` | Removed | 2.0.1.Final | - |
| `org.projectlombok:lombok` | Updated | 1.18.32 | 1.18.34 |
| `org.springframework.boot:spring-boot-starter-validation` | Added | - | 3.3.2 |

### Additional dependencies added <a style="float:right; font-size: 14px;" href="#top">Scroll to top</a><a id="additional-dependencies-added"></a>

Amazon Q updated the following additional dependencies during the upgrade

| Dependency | Action | Previous version in Java 11 | Current version in Java 17 |
|--------------|--------|--------|--------|
| `com.adobe.aio:aio-lib-java-core` | Updated | 1.1.26 | 1.1.28 |
| `com.adobe.aio:aio-lib-java-events-webhook` | Updated | 1.1.26 | 1.1.28 |
| `com.adobe.aio:aio-lib-java-ims` | Updated | 1.1.26 | 1.1.28 |
| `com.fasterxml.jackson.core:jackson-annotations` | Updated | 2.17.1 | 2.17.2 |
| `com.fasterxml.jackson.core:jackson-core` | Updated | 2.17.1 | 2.17.2 |
| `com.fasterxml.jackson.core:jackson-databind` | Updated | 2.17.1 | 2.17.2 |
| `com.fasterxml.jackson.datatype:jackson-datatype-jsr310` | Updated | 2.17.1 | 2.17.2 |
| `io.github.openfeign:feign-core` | Updated | 13.2.1 | 13.3 |
| `io.github.openfeign:feign-jackson` | Updated | 13.2.1 | 13.3 |
| `io.github.openfeign:feign-okhttp` | Updated | 13.2.1 | 13.3 |
| `io.github.openfeign:feign-slf4j` | Updated | 13.2.1 | 13.3 |
| `org.junit:junit-bom` | Updated | 5.10.0 | 5.10.3 |

### Deprecated code replaced <a style="float:right; font-size: 14px;" href="#top">Scroll to top</a><a id="deprecated-code-replaced"></a>

Amazon Q replaced the following instances of deprecated code. An instance with 0 files
changed indicates Amazon Q wasn't able to replace the deprecated code.

| Deprecated code | Files changed |
|----------------|----------------|


### Other changes <a style="float:right; font-size: 14px;" href="#top">Scroll to top</a><a id="other-changes"></a>



### All files changed <a style="float:right; font-size: 14px;" href="#top">Scroll to top</a><a id="all-files-changed"></a>

| File | Action |
|----------------|--------|
| [pom.xml](../pom.xml) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/ApiBuilder.java](../src/main/java/com/adobe/aio/cloudmanager/ApiBuilder.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/ContentSet.java](../src/main/java/com/adobe/aio/cloudmanager/ContentSet.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/ContentSetApi.java](../src/main/java/com/adobe/aio/cloudmanager/ContentSetApi.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/Environment.java](../src/main/java/com/adobe/aio/cloudmanager/Environment.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/EnvironmentApi.java](../src/main/java/com/adobe/aio/cloudmanager/EnvironmentApi.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/LogOption.java](../src/main/java/com/adobe/aio/cloudmanager/LogOption.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineApi.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineApi.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineExecution.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineExecution.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionApi.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionApi.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionEndEvent.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionEndEvent.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionEvent.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionEvent.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionStartEvent.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionStartEvent.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionStepEndEvent.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionStepEndEvent.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionStepStartEvent.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionStepStartEvent.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionStepWaitingEvent.java](../src/main/java/com/adobe/aio/cloudmanager/PipelineExecutionStepWaitingEvent.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/ProgramApi.java](../src/main/java/com/adobe/aio/cloudmanager/ProgramApi.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/RepositoryApi.java](../src/main/java/com/adobe/aio/cloudmanager/RepositoryApi.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/TenantApi.java](../src/main/java/com/adobe/aio/cloudmanager/TenantApi.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/Variable.java](../src/main/java/com/adobe/aio/cloudmanager/Variable.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/content/ExceptionDecoder.java](../src/main/java/com/adobe/aio/cloudmanager/impl/content/ExceptionDecoder.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/environment/EnvironmentApiImpl.java](../src/main/java/com/adobe/aio/cloudmanager/impl/environment/EnvironmentApiImpl.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/environment/EnvironmentImpl.java](../src/main/java/com/adobe/aio/cloudmanager/impl/environment/EnvironmentImpl.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/environment/ExceptionDecoder.java](../src/main/java/com/adobe/aio/cloudmanager/impl/environment/ExceptionDecoder.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/exception/CloudManagerExceptionDecoder.java](../src/main/java/com/adobe/aio/cloudmanager/impl/exception/CloudManagerExceptionDecoder.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/ExceptionDecoder.java](../src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/ExceptionDecoder.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/PipelineApiImpl.java](../src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/PipelineApiImpl.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/ExceptionDecoder.java](../src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/ExceptionDecoder.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/PipelineExecutionApiImpl.java](../src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/PipelineExecutionApiImpl.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/PipelineExecutionImpl.java](../src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/PipelineExecutionImpl.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/PipelineExecutionStepStateImpl.java](../src/main/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/PipelineExecutionStepStateImpl.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/program/ExceptionDecoder.java](../src/main/java/com/adobe/aio/cloudmanager/impl/program/ExceptionDecoder.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/repository/ExceptionDecoder.java](../src/main/java/com/adobe/aio/cloudmanager/impl/repository/ExceptionDecoder.java) | Updated |
| [src/main/java/com/adobe/aio/cloudmanager/impl/tenant/ExceptionDecoder.java](../src/main/java/com/adobe/aio/cloudmanager/impl/tenant/ExceptionDecoder.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/ApiBuilderTest.java](../src/test/java/com/adobe/aio/cloudmanager/ApiBuilderTest.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/impl/AbstractApiTest.java](../src/test/java/com/adobe/aio/cloudmanager/impl/AbstractApiTest.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/impl/content/ContentSetTest.java](../src/test/java/com/adobe/aio/cloudmanager/impl/content/ContentSetTest.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/impl/environment/EnvironmentTest.java](../src/test/java/com/adobe/aio/cloudmanager/impl/environment/EnvironmentTest.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/impl/pipeline/PipelineTest.java](../src/test/java/com/adobe/aio/cloudmanager/impl/pipeline/PipelineTest.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/PipelineExecutionTest.java](../src/test/java/com/adobe/aio/cloudmanager/impl/pipeline/execution/PipelineExecutionTest.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/impl/program/ProgramTest.java](../src/test/java/com/adobe/aio/cloudmanager/impl/program/ProgramTest.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/impl/repository/RepositoryTest.java](../src/test/java/com/adobe/aio/cloudmanager/impl/repository/RepositoryTest.java) | Updated |
| [src/test/java/com/adobe/aio/cloudmanager/impl/tenant/TenantTest.java](../src/test/java/com/adobe/aio/cloudmanager/impl/tenant/TenantTest.java) | Updated |

### Next steps <a style="float:right; font-size: 14px;" href="#top">Scroll to top</a><a id="next-steps"></a>

1. Please review and accept the code changes using the diff viewer.If you are using a Private Repository, please ensure that updated dependencies are available.
1. 
1. In order to successfully verify these changes on your machine, you will need to change your project to Java 17. We verified the changes using [Amazon Corretto Java 17](https://docs.aws.amazon.com/corretto/latest/corretto-17-ug/what-is-corretto-17.html
) build environment.
1. If this project uses Maven CheckStyle, Enforcer, FindBugs or SpotBugs plugins, Q Code Transformation will disable those plugins when we build the project to verify proposed upgrades.