/*
 * Copyright 2015-2018 Micro Focus or one of its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hpe.caf.boilerplate.web.setup;

import com.hpe.caf.boilerplate.api.hibernate.ExecutionContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * Created by Michael.McAlynn on 10/12/2015.
 */
public class ContextLoaderListener extends ContextLoader implements ServletContextListener {

    private org.springframework.web.context.ContextLoaderListener loader;
    private final Logger logger = LoggerFactory.getLogger(ContextLoaderListener.class);

    public ContextLoaderListener() {
        loader = new org.springframework.web.context.ContextLoaderListener();
    }

    public ContextLoaderListener(WebApplicationContext context) {

        loader = new org.springframework.web.context.ContextLoaderListener(context);
    }

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        logger.trace("ContextLoaderListener contextInitialized");

        loader.contextInitialized(servletContextEvent);
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

        logger.trace("ContextLoaderListener contextDestroyed");

        // ... First close any background tasks which may be using the DB ...
        // ... Then close any DB connection pools ...
        closeApplicationObjects();

        // Now deregister JDBC drivers in this context's ClassLoader, all of our classes,
        // should manually do this but this prevents issues in tomcat, where leaks can cause issues.
        deregisterJDBCDrivers();

        loader.contextDestroyed(servletContextEvent);

        loader = null;
    }

    private void closeApplicationObjects() {

        // get hold of the application context, to see if we can get hold of connection
        // provider and other objects.
        WebApplicationContext applicationContext = this.getCurrentWebApplicationContext();

        if (applicationContext.containsBean(ExecutionContextProvider.class.getName())) {

            ExecutionContextProvider executionContextProvider =
                    applicationContext.getBean(ExecutionContextProvider.class);

            // shutdown our connection provider, and hence the connection pool.
            try {
                executionContextProvider.closeExecutionContext();
            } catch (Exception e) {
                logger.error("Unexpected error in closeExecutionContext: ", e);
            }
        }

    }

    private void deregisterJDBCDrivers() {
        // Get the webapp's ClassLoader
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        // Loop through all drivers
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            if (driver.getClass().getClassLoader() == cl) {
                // This driver was registered by the webapp's ClassLoader, so deregister it:
                try {
                    logger.info("Deregistering JDBC driver {}", driver);
                    DriverManager.deregisterDriver(driver);
                } catch (SQLException ex) {
                    logger.error("Error deregistering JDBC driver {}", driver, ex);
                }
            } else {
                // driver was not registered by the webapp's ClassLoader and may be in use elsewhere
                logger.trace("Not deregistering JDBC driver {} as it does not belong to this webapp's ClassLoader", driver);
            }
        }
    }


}