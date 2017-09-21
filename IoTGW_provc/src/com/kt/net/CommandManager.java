package com.kt.net;

import java.util.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.LogFlagProperty;
import com.kt.restful.model.StatisticsModel;


public class CommandManager implements CommandReceiver{
	private static Logger logger = LogManager.getLogger(CommandManager.class);
	
	private static CommandManager commandManager;
	
	private List<String> allowIpList = new ArrayList<String>();
	private List<String> traceImsiList = new ArrayList<String>();
	
	private boolean logFlag = false;
 	
	public static void main(String[] args) {
		CommandConnector.getInstance();
	}
	
	private CommandManager() {
		try {
			
			if(LogFlagProperty.getPropPath("log_flag").equals("ON")) {
				logFlag = true;
			} else {
				logFlag = false;
			}
		} catch (Exception ex) {
			logger.error("LogFlagProperty Load Excetpion Occured : " + ex.getMessage());
		}
	}
	
	public static CommandManager getInstance() {
		if(commandManager == null) {
			commandManager = new CommandManager();
		}
		
		return commandManager;
	}
	
	private int clientReqID = 0;
	
	public synchronized int getClientReqID(){
		clientReqID++;
		if(clientReqID > 10000){
			clientReqID = 0;
		}
		
		return clientReqID;
	}

	public synchronized static void sendCommand(HashMap<String, StatisticsModel> statistics) {

	}
	
	public synchronized static void sendMessage(String command, String imsi, String ipAddress, String sendMsg, int jobNo) {
		CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, sendMsg, jobNo);
	}
	
	public synchronized void receiveMessage(String command, String imsi, String ipAddress, int jobNo) {

		String result = "";
		switch (command.toUpperCase().replaceAll("_", "-")) {
		case "REG-PROV-TRC" :
			StringBuffer regTrcSB  = new StringBuffer();
			if(traceImsiList.size() >= 40) {
				regTrcSB.append("REG TRACE IMSI : ");
				regTrcSB.append(imsi);
				break;
			}
			if(traceImsiList.contains(imsi)) {
				regTrcSB.append("REG TRACE IMSI : ");
				regTrcSB.append(imsi);
			} else {
				traceImsiList.add(imsi);
				regTrcSB.append("REG TRACE IMSI : ");
				regTrcSB.append(imsi);
			}
			regTrcSB.append(System.getProperty("line.separator"));
			regTrcSB.append("====================");
			regTrcSB.append(System.getProperty("line.separator"));
			regTrcSB.append("REG TRACE IMSI LIST");
			regTrcSB.append(System.getProperty("line.separator"));
			regTrcSB.append("====================");
			regTrcSB.append(System.getProperty("line.separator"));
			for(String imsilist : traceImsiList){
				regTrcSB.append(imsilist);
				regTrcSB.append(System.getProperty("line.separator"));
			}
			regTrcSB.append("====================");
			regTrcSB.append(System.getProperty("line.separator"));
			result = regTrcSB.toString();
			break;
		case "CANC-PROV-TRC" :
			StringBuffer cancTrcSB  = new StringBuffer();
			if(traceImsiList.contains(imsi)) {
				traceImsiList.remove(imsi);
				cancTrcSB.append("CANCEL TRACE IMSI : ");
				cancTrcSB.append(imsi);
			} else {
				cancTrcSB.append("CANCEL TRACE IMSI : ");
				cancTrcSB.append(imsi);
			}
			cancTrcSB.append(System.getProperty("line.separator"));
			cancTrcSB.append("====================");
			cancTrcSB.append(System.getProperty("line.separator"));
			cancTrcSB.append("REG TRACE IMSI LIST");
			cancTrcSB.append(System.getProperty("line.separator"));
			cancTrcSB.append("====================");
			cancTrcSB.append(System.getProperty("line.separator"));
			for(String imsilist : traceImsiList){
				cancTrcSB.append(imsilist);
				cancTrcSB.append(System.getProperty("line.separator"));
			}
			cancTrcSB.append("====================");
			cancTrcSB.append(System.getProperty("line.separator"));
			result = cancTrcSB.toString();
			break;
		case "DIS-PROV-TRC" :
			StringBuffer disTrcSB  = new StringBuffer();
			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			disTrcSB.append("REG TRACE IMSI LIST");
			disTrcSB.append(System.getProperty("line.separator"));
			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			for(String imsilist : traceImsiList){
				disTrcSB.append(imsilist);
				disTrcSB.append(System.getProperty("line.separator"));
			}
			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			result = disTrcSB.toString();
			break;
		case "RELOAD-CONFIG-DATA": 
			if(imsi.equals("PROVC")) {
				if(LogFlagProperty.getPropPath("log_flag").equals("ON")) {
					logFlag = true;
				} else {
					logFlag = false;
				}
			}
			
			result = "";
			CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
			break;
		case "DIS-CONFIG-DATA": 
			logger.info(command + "  "+ imsi);
			if(imsi.equals("PROVC")) {
				if(LogFlagProperty.getPropPath("log_flag").equals("ON")) {
					logFlag = true;
				} else {
					logFlag = false;
				}
				result = "LOG FLAG = " + logFlag;
				result = result.toUpperCase();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
			}
			break;
		}
	}
	
	public List<String> getAllowIpList() {
		return allowIpList;
	}

	public void setAllowIpList(List<String> allowIpList) {
		this.allowIpList = allowIpList;
	}

	public List<String> getTraceImsiList() {
		return traceImsiList;
	}

	public void setTraceImsiList(List<String> traceImsiList) {
		this.traceImsiList = traceImsiList;
	}
	
	public boolean isLogFlag() {
		return logFlag;
	}

	public void setLogFlag(boolean logFlag) {
		this.logFlag = logFlag;
	}
}

