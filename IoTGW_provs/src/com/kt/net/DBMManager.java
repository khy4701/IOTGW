package com.kt.net;

import java.util.*;


public class DBMManager implements Receiver{
	private static DBMManager dbmManager;
	
	private static ArrayList<SenderInfo> dbmMembers;
	
	private DBMManager() {
		dbmMembers = new ArrayList<SenderInfo>();
	}
	
	public static DBMManager getInstance() {
		if(dbmManager == null) {
			dbmManager = new DBMManager();
		}
		
		return dbmManager;
	}
	
	private int clientReqID = 0;
	
	public synchronized int getClientReqID(){
		clientReqID++;
		if(clientReqID > 2000000000){
			clientReqID = 0;
		}
		
		return clientReqID;
	}

	public synchronized static void sendCommand(String command, List<String[]> params, DBMListener source, int reqId, String imsi, String mdn, String ipAddress) {

		SenderInfo sender = new SenderInfo(reqId, source);
		dbmMembers.add(sender);

		if(!DBMConnector.getInstance().sendMessage(command, params, reqId, imsi, mdn, ipAddress)) {			
			dbmMembers.remove(sender);
		}
	}
	
	public synchronized static void sendCommand(String command, DBMListener source) {

//		SenderInfo sender = new SenderInfo(cliReqId, source);
//		dbmMembers.add(sender);
//
//		if(!DBMConnector.getInstance().sendMessage(cliReqId)) {
//			dbmMembers.remove(sender);
//		}
//		
//		if(cliReqId == 9999)     cliReqId = 1001;
//		else     cliReqId++;
	}
	
	public synchronized void receiveMessage(String message, int rspCode, int cliReqId) {
		SenderInfo sender = null;
		
		for(int i = 0 ; i < dbmMembers.size() ; i++) {
			sender = (SenderInfo)dbmMembers.get(i);
			if(sender != null) {
				if(sender.getCliReqId() == cliReqId) {
					DBMListener dbmListener = sender.getSource();
					dbmListener.setComplete(message, rspCode, cliReqId);
					dbmMembers.remove(i);
				}
			}
		}
	}
}
