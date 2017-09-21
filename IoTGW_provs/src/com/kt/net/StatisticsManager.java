package com.kt.net;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.kt.restful.model.StatisticsModel;


public class StatisticsManager implements Receiver{
	private static StatisticsManager statisticsManager;
	
	private static ConcurrentHashMap<String, StatisticsModel> statisticsHash;
	
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

	public synchronized static void sendCommand(ConcurrentHashMap<String, StatisticsModel> statistics) {

		if(StatisticsConnector.getInstance().sendMessage(statistics)) {
			synchronized (statisticsHash) {
				statisticsHash.clear();
			}
		}
	}
	
	
	
	public synchronized void receiveMessage(String message, int rspCode, int cliReqId) {
		synchronized (statisticsHash) {
			sendCommand(statisticsHash);
		}
	}
	
	public synchronized void sendStatitics() {
		synchronized (statisticsHash) {
			sendCommand(statisticsHash);
		}
	}
	
	public synchronized static ConcurrentHashMap<String, StatisticsModel> getStatisticsHash() {
		return statisticsHash;
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

	public synchronized int getOverLoadTps() {
		return overLoadTps;
	}

	public static void setOverLoadTps(int overLoadTps) {
		StatisticsManager.overLoadTps = overLoadTps;
	}
	
	
}

