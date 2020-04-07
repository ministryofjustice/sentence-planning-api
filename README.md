# Sentence Planning API

This service provides an API to manage HMPPS Setence Plans. 

## Getting Started

### Requirements

* ```Java 11```
* ```Docker```
* ```Postgres```


## Build and Run the Service

### Prerequisites
In order to run the service locally, a postgres database, the [Offender Assessment API](https://github.com/ministryofjustice/offender-assessments-api/) and [Nomis OAuth Service](https://github.com/ministryofjustice/nomis-oauth2-server/) are required. 

Add the following to your hosts file to enable the authentication integration to work:

```
 127.0.0.1    oauth
 ```


These can be run locally using the [docker-compose.yml](docker-compose.yml) file which will pull down the latest version.

```
 docker-compose up 
 ```

The service uses Lombok and so annotation processors must be [turned on within the IDE](https://www.baeldung.com/lombok-ide).

### Building and running 

This service is built using Gradle. In order to build the project from the command line, run

```
./gradlew build
```

To run the service, ensure there is an instance of Postgres running and then run

```
./gradlew bootRun
```

## Tests

The suite of tests includes unit tests for the resource and services classes, and integration tests. In order to run the integration tests, an instance of postgres must be running.


## Deployment

Builds and deployments are setup in [Circle CI](https://circleci.com/gh/ministryofjustice/sentence-planning-api) and configured in the [config file.](circleci.config.yml) 

Helm is used to deploy the service to a Kubernetes Cluster using templates in the helm_deploy folder. 

## Health
/ping: will respond pong to all requests.

/health: provides information about the application health and its dependencies.

/info: provides information about the version of deployed application.
