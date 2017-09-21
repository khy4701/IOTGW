package com.kt.restful.scheduler;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ConnectionCheckScheduler implements ServletContextListener{
	
	private static Logger logger = LogManager.getLogger(ConnectionCheckScheduler.class);

    private ConncetionCheckThread connectionCheckThread = null;
 
    public void contextInitialized(ServletContextEvent sce) {
        if ((connectionCheckThread == null) || (!connectionCheckThread.isAlive())) {
        	logger.debug("Start");
            connectionCheckThread = new ConncetionCheckThread(new ConnectionCheckTask());
            connectionCheckThread.start();
        }
    }

    public void contextDestroyed(ServletContextEvent sce){
        if (connectionCheckThread != null && connectionCheckThread.isAlive()) {
        	logger.debug("End");
            connectionCheckThread.quit();
        }
    }
}