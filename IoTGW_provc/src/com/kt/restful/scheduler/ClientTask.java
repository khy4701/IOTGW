package com.kt.restful.scheduler;

import java.util.TimerTask;

import com.kt.net.CommandConnector;
import com.kt.net.DBMConnector;
import com.kt.net.StatisticsConnector;
import com.kt.net.StatisticsManager;

public class ClientTask extends TimerTask {

//	private static Logger logger = LogManager.getLogger(ClientTask.class);
	
    @Override
    public void run() {
    	CommandConnector.getInstance();
    	DBMConnector.getInstance();
    	StatisticsConnector.getInstance();
    	StatisticsManager.getInstance().sendStatitics();
    	
    }
}
