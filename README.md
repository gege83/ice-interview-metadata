# Song metadata service

**note:** This is an interview exercise. [see the original task description](docs/01-TakeHomeTask-Candidate.pdf)

This project is a service that stores song metadata such as title, artist, length ...

The service uses REST apis to expose data and perform operations on it.

## Application Details
The application will be available at `http://localhost:8080` after running the project. 
See the `How to run the project` section for more details.

Visit `/` to see a simple demo application
There are 3 endpoint groups:
- `/tracks` to manage track metadata
   - `GET /tracks?artistId={id}` to fetch all tracks for a given artist id
   - `POST /tracks` to create a new track metadata.
   - `PUT /tracks/{id}` to update a track metadata.
- `/artists` to manage artist alias
   - `GET /artists` to fetch the alias of the logged-in user.
   - `POST /artists` to create a new alias for logged-in user.
   - `PUT /artists/{id}` to update the alias of the logged-in user.
- `GET /public/artist-of-the-day` to get the artist of the day


## How to run the project

### Terminal
1. Run `docker compose up lgtm` to start observability tools.
2. setup agent environment variable to enable OpenTelemetry instrumentation (optional, but recommended for better observability)
   - `export JAVA_TOOL_OPTIONS="-javaagent:./agent/opentelemetry-javaagent.jar"`
3. Run `./gradlew bootRun` in the project directory

### Run with tracing in docker containers

1. Create local app image `./gradlew bootBuildImage`
2. Run docker with `docker compose up --force-recreate`

This will start 3 containers:
- the application (instrumented with OpenTelemetry) 8080
- Structurizer (to visualize the architecture diagrams) 8090
- Grafana
  - to visualize the metrics 3000
  - to accept the traces 4317
  - to accept the metrics 4318

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

| username | password | role   |
|----------| --- |--------|
| user     | password | USER   |
| artist1  | password | ARTIST |
| artist2  | password | ARTIST |


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
- [x] Create main page to use the app.
- [x] Handle missing artist id in track metadata creation and update 


