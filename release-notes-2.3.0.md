#### Version Number
${version-number}

#### New Features
 - [CAF-3746](https://jira.autonomy.com/browse/CAF-3746): Logging to standard output streams  
    The Tomcat configuration has been updated so that all logs are sent to the standard output streams. The access logs are prefixed with `access_log> ` so that they can be easily filtered.

 - [CAF-2349](https://jira.autonomy.com/browse/CAF-2349): Docker Health Checks  
    Docker health checks have now been added to the worker to allow the workers to recover when running in swarm mode.

#### Known Issues
