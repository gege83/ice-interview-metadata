# Song metadata service

**note:** This is an interview exercise. [see the original task description](docs/01-TakeHomeTask-Candidate.pdf)

This project is a service that stores song metadata such as title, artist, length ...

The service uses REST apis to expose data and perform operations on it.

## How to run the project:

### Terminal
1. Run `docker compose up lgtm` to start observability tools.
2. setup agent environment variable to enable OpenTelemetry instrumentation (optional, but recommended for better observability)
   - `export JAVA_TOOL_OPTIONS="-javaagent:./agent/opentelemetry-javaagent.jar"`
3. Run `./gradlew bootRun` in the project directory
4. The service will be available at `http://localhost:8080` (or your configured port)

### Run with tracing in docker containers

1. Create local app image `./gradlew bootBuildImage`
2. Run docker with `docker compose up --force-recreate`

This will start 3 containers:
- the application (instrumented with OpenTelemetry)
- open telementry collector (configured to receive traces from the application and export them to Jaeger)
- Jaeger (to visualize the traces)

### IntelliJ setup

In the run options you can add the following environment variable to enable OpenTelemetry instrumentation (optional, but recommended for better observability):
- `JAVA_TOOL_OPTIONS=-javaagent:./agent/opentelemetry-javaagent.jar`

OR

You can add the following VM options to enable OpenTelemetry instrumentation (optional, but recommended for better observability):
- `-javaagent:./agent/opentelemetry-javaagent.jar`

## How to run the tests:
tests are using testcontainers to run local instances of downstream services.

1. Run `./gradlew test` in the project directory
2. The tests will run and the results will be displayed in the console

# Security

To avoid the complexity of an authenticator service this application is using preconfigured test users. 
**To push the application to production this will need to change.**
you can check the `SecurityConfig` class for the user details configuration.
At the moment we have configured only one user with no special permissions, but we can easily add more users with different roles and permissions if needed.

| username | password | role              |
|----------| --- |-------------------|
| user     | password | unregistered user |
| artist1  | password | registered artist |
| artist2  | password | reigstered artist |


## Extra docs

- [architecture documentation](docs/architecture/architecture.md)
- [domain](docs/domain.md)
- [artist of the day solution](docs/artistOfTheDay.md)

## Plans:
- [x] Create a Spring Boot application and test setup
- [x] Create REST endpoints to handle song metadata
- [x] Create REST endpoints to handle artist alias
- [x] Create REST endpoint to handle artist of the day
- [x] Use schema versioning to manage data structure changes over time
- [x] Configure security to protect the endpoints
- [x] Create CI/CD pipeline to build and test the application
- [x] Create a Dockerfile to containerize the application (build image does this for us)
- [x] Docker compose file to run the application and it's dependencies locally
- [x] Create diagrams to illustrate the architecture and design of the application
- [x] Use structured logging and log correlation to improve observability
- [x] Use OpenTelemetry to instrument the application and export traces and metrics to a monitoring system
- [ ] Create main page to use the app.
- [ ] Handle missing artist id in track metadata creation and update 


