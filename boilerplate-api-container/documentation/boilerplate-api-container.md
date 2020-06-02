## Boilerplate Web API Container

This is a docker container for the Boilerplate web service. It consists of a Tomcat web server that connects to the database specified in the Marathon JSON files.
It uses the [openSUSE Tomcat with Java 11 base image](https://github.com/CAFapi/opensuse-tomcat-jre11-image).

### Configuration

#### Environment Variables

##### hibernate.connectionstring
A string that specifies the JDBC connection to the Boilerplate database the web service will save to.

##### hibernate.user
The username to connect to the Boilerplate database.

##### hibernate.password
The password to use to connect to the Boilerplate database.

