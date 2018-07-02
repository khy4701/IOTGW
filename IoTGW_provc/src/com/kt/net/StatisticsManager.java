package com.kt.net;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ProvifMsgType;
import com.kt.restful.model.StatisticsModel;


public class StatisticsManager implements Receiver{
	private static StatisticsManager statisticsManager;
	
	private static ConcurrentHashMap<String, StatisticsModel> statisticsHash;
	private static ConcurrentHashMap<String, StatisticsModel> statisticsHash_BSS;
	private static ConcurrentHashMap<String, StatisticsModel> statisticsHash_CUBIC;

	
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
		
		statisticsHash_BSS = new ConcurrentHashMap<String, StatisticsModel>();
		statisticsHash_BSS.clear();
		
		statisticsHash_CUBIC = new ConcurrentHashMap<String, StatisticsModel>();
		statisticsHash_CUBIC.clear();

	}
	
	public static StatisticsManager getInstance() {
		if(statisticsManager == null) {
			statisticsManager = new StatisticsManager();
		}
		
		return statisticsManager;
	}
	
	public synchronized static void sendCommand(ConcurrentHashMap<String, StatisticsModel> statistics, int type) {

		if(StatisticsConnector.getInstance().sendMessage(statistics, type)) {
			synchronized (statistics) {
				statistics.clear();
			}
		}
	}
	
//	public synchronized void receiveMessage(String message, int rspCode, int cliReqId) {
//		synchronized (statisticsHash) {
//			sendCommand(statisticsHash);
//		}
//	}
	
	public synchronized void sendStatitics() {
		
		String appName = IoTProperty.getPropPath("sys_name");
		
		// PROVC1 -> IUDR : Jasper
		// PROVC2 -> IUDR : Cubic(inquiry)
		if (appName.equals("provc1") || appName.equals("provc2") ) {
			// IUDR
			// MSGID_IUDR_STATISTICS_REPORT : 104
			synchronized (statisticsHash) {
				sendCommand(statisticsHash, 104);
			}
		}

		// BSS-IOT
		// PROVC1 -> BSS  : Jasper
		// PROVC2 -> BSS  : Cubic
		// MSGID_BSS_IOT_STATISTICS_REPORT : 109
		if (appName.equals("provc1") || appName.equals("provc2")) {
			synchronized (statisticsHash_BSS) {
				sendCommand(statisticsHash_BSS, 109);
			}
		}
		
		// CUBIC
		// MSGID_BSS_IOT_STATISTICS_REPORT : 110
		if (appName.equals("provc3")) {
			synchronized (statisticsHash_CUBIC) {
				sendCommand(statisticsHash_CUBIC, 110);
			}
		}
	}
	
	public synchronized static ConcurrentHashMap<String, StatisticsModel> getStatisticsHash() {
		return statisticsHash;
	}
	
	public synchronized static ConcurrentHashMap<String, StatisticsModel> getStatistics_BSSHash() {
		return statisticsHash_BSS;
	}
	
	public synchronized static ConcurrentHashMap<String, StatisticsModel> getStatistics_CUBICHash() {
		return statisticsHash_CUBIC;
	}



	public static void setStatisticsHash(ConcurrentHashMap<String, StatisticsModel> statisticsHash, int type) {
		if (type == 0)
			StatisticsManager.statisticsHash = statisticsHash;
		
		if (type == 1)
			StatisticsManager.statisticsHash_BSS = statisticsHash;
		
		if (type == 2)
			StatisticsManager.statisticsHash_CUBIC = statisticsHash;
	}

	@Override
	public void receiveMessage(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress,
			String body, ProvifMsgType pmt) {
	}
}

