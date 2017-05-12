# Boilerplate Worker Container FS

A pre-packaged boilerplate worker Docker image built using the worker-boilerplate library and a File System based data store.

## Container Configuration

### Environment Variable Configuration

The worker container supports reading its configuration solely from environment variables. To enable this mode do not pass the `CAF_APPNAME` and `CAF_CONFIG_PATH` environment variables to the worker. This will cause it to use the default configuration files embedded in the container which check for environment variables. A listing of the RabbitMQ and Storage properties is available [here](https://github.com/WorkerFramework/worker-framework/tree/develop/worker-default-configs).

The Boilerplate Worker specific configuration that can be controlled through the default configuration file is described below;

#### BoilerplateWorkerConfiguration

| Property | Checked Environment Variables | Default               |
|----------|-------------------------------|-----------------------|
| baseUrl  |  `CAF_BOILERPLATE_WORKER_BASE_URL`                              | http://boilerplate-api:8080/boilerplateapi  |
| cacheExpireTimePeriod      |   `CAF_BOILERPLATE_WORKER_CACHE_EXPIRE_TIME_PERIOD`                            |             PT5M         |
| outputQueue   |  `CAF_WORKER_OUTPUT_QUEUE`                                                      | worker-out  |
|              |   `CAF_WORKER_BASE_QUEUE_NAME` with '-out' appended to the value if present     |             |
|              |  `CAF_WORKER_NAME` with '-out' appended to the value if present                 |             |
|  threads   |   `CAF_BOILERPLATE_WORKER_THREADS`                                         |   1       |
|             |   `CAF_WORKER_THREADS`                                             |          |

## Feature Testing
The testing for the Boilerplate Worker is defined [here](testcases)