# Boilerplate Database Installer

Creates a database installer that will create and/or update the Boilerplate database schema in the target database.

## Running the Installer

The installer is run via command line. The following arguments can be specified:

*   db.user  :  Specifies the username to access the database.
*   db.pass  :  Specifies the password to access the database.
*   db.connection  : Specifies the jbdc connection string to the database service. This does not include the database name.  e.g. jdbc:postgresql:/localhost:3307/
*   db.name  :  Specifies the name of the database to be created or updated.
*   fd  :  Enables the deletion of the existing database for a fresh install, rather than updating the database.
*   log : Specifies the logging level of the installer. Valid options are: [debug, info, warning, severe, off]

You can specify these options in a database.properties file. This can be specified with the system property `-DDATABASE_CONFIG=<DIRECTORY OF PROPERTY FILE>`   
The command to run the jar will be in the following format:  

    java -jar [system properties] installer.jar [arguemnts]  

For example:   

    java -jar -DDATABASE_CONFIG=C:\var myInstaller.jar -fd -db.user Admin  
    
Note that passing a property as a command line argument will override the properties file.  