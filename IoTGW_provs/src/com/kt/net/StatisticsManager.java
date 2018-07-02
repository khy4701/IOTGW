package com.kt.net;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ProvifMsgType;
import com.kt.restful.model.StatisticsModel;


public class StatisticsManager implements Receiver{
	private static StatisticsManager statisticsManager;
	
	private static ConcurrentHashMap<String, StatisticsModel> statisticsHash;
	private static ConcurrentHashMap<String, StatisticsModel> statisticsHash_cubic;
	private static ConcurrentHashMap<String, StatisticsModel> statisticsHash_bssiot;

	
	private static int overLoadTps = 0; 
	
	public static void main(String[] args) {
		new StatisticsManager();
		
		int count = statisticsHash.size();
		int bodyLen = 4 + 4 + (statisticsHash.size() * 140);
				
		System.out.println("bodyLen : " + bodyLen);
		System.out.println("count : " + count);
		
		for(Entry<String, StatisticsModel> entry : statisticsHash.entrySet()) {
			System.out.println(entry.getValue().getIpAddress() + " : " + entry.getValue().getApiName() + " : " + entry.getValue().getTotal() + " : " + entry.getValue().getSucc() + " : " + entry.getValue().getFail()); 
		}
	}
	
	private StatisticsManager() {
		statisticsHash = new ConcurrentHashMap<String, StatisticsModel>();
		statisticsHash.clear();
		
		statisticsHash_cubic = new ConcurrentHashMap<String, StatisticsModel>();
		statisticsHash_cubic.clear();
		
		statisticsHash_bssiot = new ConcurrentHashMap<String, StatisticsModel>();
		statisticsHash_bssiot.clear();
		
//		for(int i = 0; i < 5; i++)
//		statisticsHash.put("kkkk"+i, new StatisticsModel("test"+i, "1.1.1.1"+i, 5, 4, 1));
	}
	
	public static StatisticsManager getInstance() {
		if(statisticsManager == null) {
			statisticsManager = new StatisticsManager();
		}
		
		return statisticsManager;
	}
	
	private int clientReqID = 0;
	
	public synchronized int getClientReqID(){
		clientReqID++;
		if(clientReqID > 2000000000){
			clientReqID = 0;
		}
		
		return clientReqID;
	}

	public synchronized static void sendCommand(ConcurrentHashMap<String, StatisticsModel> statistics, int type) {

		if(StatisticsConnector.getInstance().sendMessage(statistics, type)) {
			synchronized (statistics) {
				statistics.clear();
			}
		}
	}
			
	public synchronized void receiveMessage(String message, int rspCode, int cliReqId, ProvifMsgType pmt) {
		synchronized (statisticsHash) {
			sendCommand(statisticsHash, 1);
		}
		
		synchronized (statisticsHash_cubic) {
			sendCommand(statisticsHash_cubic, 2);
		}
		
		synchronized (statisticsHash_bssiot) {
			sendCommand(statisticsHash_bssiot, 3);
		}
	}
	
	public synchronized void sendStatitics() {
		
		
		String appName = IoTProperty.getPropPath("sys_name");
		
		if (appName.equals("provs1")) {
			synchronized (statisticsHash) {
				sendCommand(statisticsHash, 106);
			}
		}

		if (appName.equals("provs2")) {
			synchronized (statisticsHash_cubic) {
				sendCommand(statisticsHash_cubic, 110);
			}
		}

		if (appName.equals("provs3")) {
			synchronized (statisticsHash_bssiot) {
				sendCommand(statisticsHash_bssiot, 109);
			}
		}
	}
	
	public synchronized static ConcurrentHashMap<String, StatisticsModel> getStatisticsHash() {
		return statisticsHash;
	}
	
	public synchronized static ConcurrentHashMap<String, StatisticsModel> getStatisticsHash_CUBIC() {
		return statisticsHash_cubic;
	}
	
	public synchronized static ConcurrentHashMap<String, StatisticsModel> getStatisticsHash_BSSIOT() {
		return statisticsHash_bssiot;
	}


	public static void setStatisticsHash(ConcurrentHashMap<String, StatisticsModel> statisticsHash) {
		StatisticsManager.statisticsHash = statisticsHash;
	}
		
	
	public synchronized int getTps() {
		int tps = 0;
		
		synchronized (statisticsHash) {
			for(Entry<String, StatisticsModel> entry : statisticsHash.entrySet()) {
				tps += entry.getValue().getTotal();
			}
		}
		
		return tps;
	}
	
	public synchronized int getTps_CUBIC() {
		int tps = 0;
		
		synchronized (statisticsHash_cubic) {
			for(Entry<String, StatisticsModel> entry : statisticsHash_cubic.entrySet()) {
				tps += entry.getValue().getTotal();
			}
		}		
		return tps;
	}
	
	public synchronized int getTps_BSSIOT() {
		int tps = 0;
		
		synchronized (statisticsHash_bssiot) {
			for(Entry<String, StatisticsModel> entry : statisticsHash_bssiot.entrySet()) {
				tps += entry.getValue().getTotal();
			}
		}		
		return tps;
	}


	public synchronized int getOverLoadTps() {
		return overLoadTps;
	}

	public static void setOverLoadTps(int overLoadTps) {
		StatisticsManager.overLoadTps = overLoadTps;
	}

	
}

