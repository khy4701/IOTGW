package com.kt.restful.scheduler;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ClientScheduler implements ServletContextListener{
	
	private static Logger logger = LogManager.getLogger(ClientScheduler.class);

    private ClientThread clientThread = null;
 
    public void contextInitialized(ServletContextEvent sce) {
        if ((clientThread == null) || (!clientThread.isAlive())) {
        	logger.debug("Start");
            clientThread = new ClientThread(new ClientTask());
            clientThread.start();
        }
    }

    public void contextDestroyed(ServletContextEvent sce){
        if (clientThread != null && clientThread.isAlive()) {
        	logger.debug("End");
            clientThread.quit();
        }
    }
}