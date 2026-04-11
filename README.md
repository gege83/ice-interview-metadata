# Song metadata service

**note:** This is an interview exercise. [see the original task description](docs/01-TakeHomeTask-Candidate.pdf)

This project is a service that stores song metadata such as title, artist, length ...

The service uses REST apis to expose data and perform operations on it.

## How to run the project:

1. Clone the repository
2. Run `./gradlew bootRun` in the project directory
3. The service will be available at `http://localhost:8080` (or your configured port)

## How to run the tests:
tests are using testcontainers to run local instances of downstream services.

1. Run `./gradlew test` in the project directory
2. The tests will run and the results will be displayed in the console

## Plans:
- [x] Create a Spring Boot application and test setup
- [ ] Create a REST controller to handle song metadata
- [ ] Use schema versioning to manage data structure changes over time
- [ ] Configure security to protect the endpoints
- [ ] Create CI/CD pipeline to build and test the application
- [ ] Create a Dockerfile to containerize the application
- [ ] Docker compose file to run the application and it's dependencies locally
- [ ] Create diagrams to illustrate the architecture and design of the application
- [ ] Use structured logging and log correlation to improve observability
- [ ] Use OpenTelemetry to instrument the application and export traces and metrics to a monitoring system


