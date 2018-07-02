package com.kt.restful.scheduler;


import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ClientScheduler implements ServletContextListener{
	
	private static Logger logger = LogManager.getLogger(ClientScheduler.class);

    private ClientThread clientThread = null;
    private ConncetionCheckThread clientConnCheckThread = null;
 
    public void contextInitialized(ServletContextEvent sce) {
        if ((clientThread == null) || (!clientThread.isAlive())) {
        	logger.debug("Start");
            clientThread = new ClientThread(new ClientTask());
            clientThread.start();               
        }
        
        try {
			Thread.sleep(3000);
							 
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if ((clientConnCheckThread == null) || (!clientConnCheckThread.isAlive())) {
        	logger.debug("connection check start");
        	clientConnCheckThread = new ConncetionCheckThread(new ConnectionCheckTask());
        	clientConnCheckThread.start();               
        }        
    }

    public void contextDestroyed(ServletContextEvent sce){
        if (clientThread != null && clientThread.isAlive()) {
        	logger.debug("End");
            clientThread.quit();
        }
        
        if (clientConnCheckThread != null && clientConnCheckThread.isAlive()) {
        	logger.debug("End");
            clientThread.quit();
        }
    }
}