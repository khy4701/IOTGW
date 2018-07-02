package com.kt.net;

import java.util.ArrayList;
import java.util.List;

import com.kt.restful.model.ProvifMsgType;


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

	public synchronized static void sendCommand(String command, List<String[]> params, DBMListener source, int reqId, ProvifMsgType pmt) {

		SenderInfo sender = new SenderInfo(reqId, source);
		dbmMembers.add(sender);

		if(!DBMConnector.getInstance().sendMessage(command, params, reqId, pmt)) {			
			dbmMembers.remove(sender);
		}
	}
	
	public synchronized static void sendCommand(String command, String jsonBody, DBMListener source, int reqId, String ipAddress, ProvifMsgType pmt) {

		SenderInfo sender = new SenderInfo(reqId, source);
		dbmMembers.add(sender);

		if(!DBMConnector.getInstance().sendMessage(command, jsonBody, reqId, ipAddress, pmt)) {			
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
	
	public synchronized void receiveMessage(String message, int rspCode, int cliReqId, ProvifMsgType pmt) {
		SenderInfo sender = null;
		
		for(int i = 0 ; i < dbmMembers.size() ; i++) {
			sender = (SenderInfo)dbmMembers.get(i);
			if(sender != null) {
				if(sender.getCliReqId() == cliReqId) {
					DBMListener dbmListener = sender.getSource();
					dbmListener.setComplete(message, rspCode, cliReqId, pmt);
					dbmMembers.remove(i);
				}
			}
		}
	}
}
