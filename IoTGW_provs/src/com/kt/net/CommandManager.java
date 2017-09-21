package com.kt.net;

import java.util.*;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.AllowIpProperty;
import com.kt.restful.constants.LogFlagProperty;
import com.kt.restful.constants.OverloadControlProperty;
import com.kt.restful.model.StatisticsModel;

public class CommandManager implements CommandReceiver{
	private static Logger logger = LogManager.getLogger(CommandManager.class);
	
	private static CommandManager commandManager;
	
	private List<String> allowIpList = new ArrayList<String>();
	private List<String> traceImsiList = new ArrayList<String>();
	
	private boolean overloadControlFlag = false;
	private boolean logFlag = false;
	
	private int overloadTps = 0;
	
	public static void main(String[] args) {
		CommandConnector.getInstance();
	}
	
	private CommandManager() {
		
		try {
			for(String ip : AllowIpProperty.getPropPath("allow_ip_list").split(",")) {
				if(!ip.trim().equals("")) {
					allowIpList.add(ip);
				}
			}
			logger.info("allow_ip_list : " + AllowIpProperty.getPropPath("allow_ip_list"));
		} catch (Exception ex) {
			logger.error("AllowIpProperty Load Excetpion Occured : " + ex.getMessage());
		}
		
		try {
			
//			if(OverloadControlProperty.getPropPath("overloadControlFlag").equals("ON")) {
//				overloadControlFlag = true;
//			} else {
//				overloadControlFlag = false;
//			}
			overloadTps = Integer.parseInt(OverloadControlProperty.getPropPath("overloadControlTPS"));
			logger.info("overloadTps : " + overloadTps);
		} catch (Exception ex) {
			logger.error("OverloadControlProperty Load Excetpion Occured : " + ex.getMessage());
		}
		
		try {
			
			if(LogFlagProperty.getPropPath("log_flag").equals("ON")) {
				logFlag = true;
			} else {
				logFlag = false;
			}
			logger.info("logFlag : " + logFlag);
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
		switch (command.toUpperCase()) {
		case "REG-PROV-TRC" :
			StringBuffer regTrcSB  = new StringBuffer();
			if(traceImsiList.size() >= 40) {
				regTrcSB.append("FAIL REASON = TRACE LIST IS FULL");
				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
				break;
			}
			if(traceImsiList.contains(imsi)) {
				regTrcSB.append("FAIL REASON = ALREADY EXIST IMSI");
				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
				break;
			} else {
				traceImsiList.add(imsi);
				regTrcSB.append("IMSI = ");
				regTrcSB.append(imsi);
				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
				break;
			}
		case "CANC-PROV-TRC" :
			StringBuffer cancTrcSB  = new StringBuffer();
			if(traceImsiList.contains(imsi)) {
				traceImsiList.remove(imsi);
				cancTrcSB.append("IMSI = ");
				cancTrcSB.append(imsi);
				cancTrcSB.append(System.getProperty("line.separator"));
				cancTrcSB.append("DELETED");
				cancTrcSB.append(System.getProperty("line.separator"));
				result = cancTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
			} else {
				cancTrcSB.append("FAIL REASON = NOT EXIST IMSI");
				cancTrcSB.append(System.getProperty("line.separator"));
				result = cancTrcSB.toString();
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
			}
			break;
		case "DIS-PROV-TRC" :
			StringBuffer disTrcSB  = new StringBuffer();
			
			if(traceImsiList.size() == 0) {
				disTrcSB.append("FAIL REASON = NOT EXIST TRACE INFOMATION");
				disTrcSB.append(System.getProperty("line.separator"));
			} else {
				disTrcSB.append("TRACE NUM");
				disTrcSB.append(System.getProperty("line.separator"));
				disTrcSB.append("--------------------");
				disTrcSB.append(System.getProperty("line.separator"));
				for(String imsilist : traceImsiList){
					disTrcSB.append(imsilist);
					disTrcSB.append(System.getProperty("line.separator"));
				}
				disTrcSB.append("--------------------");
				disTrcSB.append(System.getProperty("line.separator"));
				disTrcSB.append("TOTAL COUNT = ");
				disTrcSB.append(traceImsiList.size());
				disTrcSB.append(System.getProperty("line.separator"));
			}
			result = disTrcSB.toString();
			CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
			break;
		case "CRTE-CC-INFO" :
			if(allowIpList.size() >= 40) {
				result = "ALREADY EXIST MAX ALLOW IP";
				break;
			}
			
			if(allowIpList.contains(ipAddress)) {
				result = "ALREADY EXIST IPADDRESS";
			} else {
				allowIpList.add(ipAddress);
				result = "ADD ALLOW IP : " + ipAddress;
			}
			
			try {
				AllowIpProperty.setProperty(allowIpList);
				allowIpList.clear();
				for(String ip : AllowIpProperty.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						allowIpList.add(ip);
					}
				}
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			break;
		case "DEL-CC-INFO" :
			if(allowIpList.contains(ipAddress)) {
				allowIpList.remove(ipAddress);
				result = "DELETE ALLOW IP : " + ipAddress;
			} else {
				result = "NOT EXIST IP ADDRESS";
			}
			try {
				AllowIpProperty.setProperty(allowIpList);
				allowIpList.clear();
				for(String ip : AllowIpProperty.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						allowIpList.add(ip);
					}
				}
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			break;
		case "DIS-PROV-ALLOW-IP" :
			StringBuffer disAllowIpSB  = new StringBuffer();
			disAllowIpSB.append("ALLOW IP");
			disAllowIpSB.append(System.getProperty("line.separator"));
			disAllowIpSB.append("--------------------");
			disAllowIpSB.append(System.getProperty("line.separator"));
			for(String iplist : allowIpList){
				disAllowIpSB.append(iplist);
				disAllowIpSB.append(System.getProperty("line.separator"));
			}
			disAllowIpSB.append("--------------------");
			disAllowIpSB.append(System.getProperty("line.separator"));
			disAllowIpSB.append("TOTAL COUNT = ");
			disAllowIpSB.append(allowIpList.size());
			disAllowIpSB.append(System.getProperty("line.separator"));
			result = disAllowIpSB.toString();
			CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
			break;
		case "OVERLOAD_MODE" :
			logger.info("OVERLOAD_MODE TPS : " + Integer.parseInt(imsi));
			result = "PROVS OverloadControl Flag : On";
			try {
				overloadTps = Integer.parseInt(imsi);
				overloadControlFlag = true;
				OverloadControlProperty.setProperty(overloadTps);
				logger.info("overloadTps : " + overloadTps);
			} catch (Exception ex) {
				logger.error("OverloadControlProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			break;
		case "NORMAL_MODE" :
			logger.info("NORMAL_MODE : " + Integer.parseInt(imsi));
			result = "PROVS OverloadControl Flag : Off";
			try {
				overloadTps = Integer.parseInt(imsi);
				overloadControlFlag = false;
				OverloadControlProperty.setProperty(overloadTps);
				logger.info("overloadTps : " + overloadTps);
			} catch (Exception ex) {
				logger.error("OverloadControlProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			break;
		case "RELOAD-CONFIG-DATA": 
			logger.info(command + "  "+ imsi);
			if(imsi.equals("PROVS")) {
				if(LogFlagProperty.getPropPath("log_flag").equals("ON")) {
					logFlag = true;
				} else {
					logFlag = false;
				}
				result = "";
				
				CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
			}
			break;
		case "DIS-CONFIG-DATA": 
			logger.info(command + "  "+ imsi);
			if(imsi.equals("PROVS")) {
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
	
	
	public synchronized List<String> getAllowIpList() {
		return allowIpList;
	}

	public void setAllowIpList(List<String> allowIpList) {
		this.allowIpList = allowIpList;
	}

	public synchronized List<String> getTraceImsiList() {
		return traceImsiList;
	}

	public void setTraceImsiList(List<String> traceImsiList) {
		this.traceImsiList = traceImsiList;
	}

	public synchronized boolean isOverloadControlFlag() {
		return overloadControlFlag;
	}

	public void setOverloadControlFlag(boolean overloadControlFlag) {
		this.overloadControlFlag = overloadControlFlag;
	}

	public synchronized int getOverloadTps() {
		return overloadTps;
	}

	public void setOverloadTps(int overloadTps) {
		this.overloadTps = overloadTps;
	}

	public synchronized boolean isLogFlag() {
		return logFlag;
	}

	public void setLogFlag(boolean logFlag) {
		this.logFlag = logFlag;
	}
	
}

