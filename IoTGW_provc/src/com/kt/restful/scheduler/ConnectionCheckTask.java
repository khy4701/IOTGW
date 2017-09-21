package com.kt.restful.scheduler;

import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.net.CommandConnector;
import com.kt.net.DBMConnector;
import com.kt.net.StatisticsConnector;
import com.kt.net.StatisticsManager;
import com.kt.restful.service.IUDRHeartBeatCheckService;

public class ConnectionCheckTask extends TimerTask {

//	private static Logger logger = LogManager.getLogger(ClientTask.class);
	
    @Override
    public void run() {
    	IUDRHeartBeatCheckService.getInstance().checkIUDRHeartbeat();
    }
}
