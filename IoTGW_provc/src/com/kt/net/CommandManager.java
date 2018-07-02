package com.kt.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.constants.LogFlagProperty;
import com.kt.restful.model.MMCMsgType;
import com.kt.restful.model.StatisticsModel;
import com.kt.util.AES256Util;


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
	
//	public synchronized static void sendMessage(String appname, String command, String imsi, String ipAddress, String sendMsg, String jobNo) {
//		CommandConnector.getInstance().sendMessage(appname, command, imsi, ipAddress, sendMsg, jobNo);
//	}
	public synchronized static void sendMessage(MMCMsgType mmcResMsg, String sendMsg) {
		CommandConnector.getInstance().sendMessage(mmcResMsg, sendMsg);
	}

	
	public synchronized void receiveMessage(MMCMsgType mmcMsg) {

		String result = "";
		String command   = mmcMsg.getCommand();
		String imsi      = mmcMsg.getImsi();
		String ipAddress = mmcMsg.getIpAddress();
		String jobNo	 = mmcMsg.getJobNumber();
		String port	     = mmcMsg.getPort();
		String tcpMode   = mmcMsg.getTcpMode();
		String curr_appname = IoTProperty.getPropPath("sys_name");
				
		switch (command.toUpperCase().replaceAll("_", "-")) {
		case "REG-PROV-TRC" :
			StringBuffer regTrcSB  = new StringBuffer();
			if(traceImsiList.size() >= 80) {
				regTrcSB.append("REG TRACE IMSI : ");
				regTrcSB.append(imsi+"\n");
				break;
			}
			if(traceImsiList.contains(imsi)) {
				regTrcSB.append("REG TRACE IMSI : ");
				regTrcSB.append(imsi);
			} else {
				
				regTrcSB.append("TRCKEY = ");
				
				regTrcSB.append(imsi+"\n");
				traceImsiList.add(imsi);
				String encTrckey = AES256Util.getInstance().AES_Encode(imsi); 
				if (encTrckey != null){
					traceImsiList.add(encTrckey);
					regTrcSB.append("         ");
					regTrcSB.append(encTrckey+"(" +imsi+")");

				}				
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
			
			// 02-13			
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

			break;
		case "CANC-PROV-TRC" :
			StringBuffer cancTrcSB  = new StringBuffer();
			if(traceImsiList.contains(imsi)) {
				cancTrcSB.append("TRCKEY = ");
				cancTrcSB.append(imsi+"\n");
				traceImsiList.remove(imsi);
				
				String encTrckey = AES256Util.getInstance().AES_Encode(imsi); 
				if (encTrckey != null){
					traceImsiList.remove(encTrckey);
					cancTrcSB.append("         ");
					cancTrcSB.append(encTrckey+"(" +imsi+")");
				}				
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
			
			// 02-13
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

			break;
		case "DIS-PROV-TRC" :
			StringBuffer disTrcSB  = new StringBuffer();
			String encData = "";

			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			disTrcSB.append("REG TRACE IMSI LIST");
			disTrcSB.append(System.getProperty("line.separator"));
			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			for(String imsilist : traceImsiList){
				
				encData = AES256Util.getInstance().AES_Decode(imsilist);					
				if (encData == null)
					disTrcSB.append(imsilist);
				else
					disTrcSB.append(imsilist+"("+encData+")");
				
				disTrcSB.append(System.getProperty("line.separator"));
			}
			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			result = disTrcSB.toString();
			
			// 02-13
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
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
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
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

				CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
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

