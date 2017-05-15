# Boilerplate Service

Manifest of the components which make up the Boilerplate Service:
* [boilerplate-api](boilerplate-api)
* [boilerplate-api-audit](boilerplate-api-audit)
* [boilerplate-api-container](boilerplate-api-container)
* [boilerplate-api-contract](boilerplate-api-contract)
* [boilerplate-api-html](boilerplate-api-html)
* [boilerplate-api-ui](boilerplate-api-ui)
* [boilerplate-api-web](boilerplate-api-web)
* [boilerplate-creation-container](boilerplate-creation-container)
* [boilerplate-db](boilerplate-db)
* [boilerplate-db-container](boilerplate-db-container)
* [policy-boilerplate-fields](policy-boilerplate-fields)
* [util-boilerplate-creation](util-boilerplate-creation)
* [worker-boilerplate](worker-boilerplate)
* [worker-boilerplate-container](worker-boilerplate-container)
* [worker-boilerplate-shared](worker-boilerplate-shared)
* [worker-boilerplate-testing](worker-boilerplate-testing)

## Boilerplate Worker Modules

The Boilerplate Worker is used to identify and remove boilerplate language.

For more information on the functioning of the Boilerplate Worker visit [Boilerplate Removal Worker](worker-boilerplate/documentation/Boilerplate%20Removal%20Worker.md).

### worker-boilerplate-shared
This is the shared library defining public classes that constitute the worker interface to be used by consumers of the Boilerplate Worker. The project can be found in [worker-boilerplate-shared](worker-boilerplate-shared).

### worker-boilerplate
This project contains the actual implementation of the Boilerplate Worker. It can be found in [worker-boilerplate](worker-boilerplate).

### worker-boilerplate-container
This project builds a Docker image that packages the Boilerplate Worker for deployment. It can be found in [worker-boilerplate-container](worker-boilerplate-container).

### worker-boilerplate-testing
This contains implementations of the testing framework to allow for integration testing of the Boilerplate Worker. The project can be found in [worker-boilerplate-testing](worker-boilerplate-testing).

### boilerplate-api-container
This project builds a Docker image for the Boilerplate API web service. It consists of a Tomcat web server that connects to a database for performing Boilerplate Expressions and Tags CRUD operations. Auditing of Boilerplate API operations can be configured and enabled by following [boilerplate-api-auditing](boilerplate-api-container/documentation/boilerplate-api-auditing.md). This project can be found in [boilerplate-api-container](boilerplate-api-container).

## Feature Testing
The testing for the Boilerplate Service is defined in [testcases](worker-boilerplate-container-fs/testcases).
