package com.kt.restful.scheduler;

import java.util.TimerTask;

import com.kt.restful.service.IUDRHeartBeatCheckService;

public class ConnectionCheckTask extends TimerTask {

//	private static Logger logger = LogManager.getLogger(ClientTask.class);
	
    @Override
    public void run() {
    	
    	IUDRHeartBeatCheckService.getInstance().checkIUDRHeartbeat();
    	IUDRHeartBeatCheckService.getInstance().checkBSSIOTHeartbeat();
    	IUDRHeartBeatCheckService.getInstance().checkCubicHeartbeat();
    }
}
