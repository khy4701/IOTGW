package com.kt.net;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.AllowIpProperty_BSS;
import com.kt.restful.constants.AllowIpProperty_CC;
import com.kt.restful.constants.AllowIpProperty_CUBIC;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.constants.LogFlagProperty;
import com.kt.restful.constants.OverloadControlProperty;
import com.kt.restful.model.MMCMsgType;
import com.kt.restful.model.StatisticsModel;
import com.kt.util.AES256Util;

public class CommandManager implements CommandReceiver{
	private static Logger logger = LogManager.getLogger(CommandManager.class);
	
	private static CommandManager commandManager;
	
	private List<String> cc_allowIpList = new ArrayList<String>();
	private List<String> bss_allowIpList = new ArrayList<String>();
	private List<String> cubic_allowIpList = new ArrayList<String>();

	private List<String> traceImsiList = new ArrayList<String>();
	
	private boolean overloadControlFlag = false;
	private boolean logFlag = false;
	
	private int overloadTps = 0;
	
	public static void main(String[] args) {
		CommandConnector.getInstance();
	}
	
	private CommandManager() {
		
		// cc-allow-ip init
		try {
			for (String ip : AllowIpProperty_CC.getPropPath("allow_ip_list").split(",")) {
				if (!ip.trim().equals("")) {
					cc_allowIpList.add(ip);
				}
			}
			logger.info("allow_ip_list : " + AllowIpProperty_CC.getPropPath("allow_ip_list"));
		} catch (Exception ex) {
			logger.error("AllowIpProperty Load Excetpion Occured : " + ex.getMessage());
		}

		// bss-allow-ip init
		try {
			for (String ip : AllowIpProperty_BSS.getPropPath("allow_ip_list").split(",")) {
				if (!ip.trim().equals("")) {
					bss_allowIpList.add(ip);
				}
			}
			logger.info("allow_ip_list : " + AllowIpProperty_BSS.getPropPath("allow_ip_list"));
		} catch (Exception ex) {
			logger.error("AllowIpProperty Load Excetpion Occured : " + ex.getMessage());
		}

		// cubic-allow-ip init
		try {
			for (String ip : AllowIpProperty_CUBIC.getPropPath("allow_ip_list").split(",")) {
				if (!ip.trim().equals("")) {
					cubic_allowIpList.add(ip);
				}
			}
			logger.info("allow_ip_list : " + AllowIpProperty_CUBIC.getPropPath("allow_ip_list"));
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
		
		switch (command.toUpperCase()) {
		case "REG-PROV-TRC" :
			StringBuffer regTrcSB  = new StringBuffer();
			if(traceImsiList.size() >= 80) {
				regTrcSB.append("FAIL REASON = TRACE LIST IS FULL");
				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

				break;
			}
			if(traceImsiList.contains(imsi)) {
				regTrcSB.append("FAIL REASON = ALREADY EXIST IMSI");
				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
				break;
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

				regTrcSB.append(System.getProperty("line.separator"));
				result = regTrcSB.toString();
				CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
				break;
			}
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

				cancTrcSB.append(System.getProperty("line.separator"));
				cancTrcSB.append("DELETED");
				cancTrcSB.append(System.getProperty("line.separator"));
				result = cancTrcSB.toString();
				CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
			} else {
				cancTrcSB.append("FAIL REASON = NOT EXIST IMSI");
				cancTrcSB.append(System.getProperty("line.separator"));
				result = cancTrcSB.toString();
				CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
			}
			break;
		case "DIS-PROV-TRC" :
			StringBuffer disTrcSB  = new StringBuffer();
			String encData = "";
			if(traceImsiList.size() == 0) {
				disTrcSB.append("FAIL REASON = NOT EXIST TRACE INFOMATION");
				disTrcSB.append(System.getProperty("line.separator"));
			} else {
				disTrcSB.append("TRACE NUM");
				disTrcSB.append(System.getProperty("line.separator"));
				disTrcSB.append("--------------------");
				disTrcSB.append(System.getProperty("line.separator"));
				for(String imsilist : traceImsiList){
										
					encData = AES256Util.getInstance().AES_Decode(imsilist);					
					if (encData == null)
						disTrcSB.append(imsilist);
					else
						disTrcSB.append(imsilist+"("+encData+")");
					
					disTrcSB.append(System.getProperty("line.separator"));
				}
				disTrcSB.append("--------------------");
				disTrcSB.append(System.getProperty("line.separator"));
				disTrcSB.append("TOTAL COUNT = ");
				disTrcSB.append(traceImsiList.size());
				disTrcSB.append(System.getProperty("line.separator"));
			}
			result = disTrcSB.toString();
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
			break;
		case "CRTE-CC-INFO" :
			
			if (tcpMode.equals("SERVER"))
				break;
			
			if(cc_allowIpList.size() >= 40) {
				result = "ALREADY EXIST MAX ALLOW IP";
				break;
			}
			
			if(cc_allowIpList.contains(ipAddress)) {
				result = "ALREADY EXIST IPADDRESS";
			} else {
				cc_allowIpList.add(ipAddress);
				result = "ADD ALLOW IP : " + ipAddress;
			}
			
			try {
				AllowIpProperty_CC.setProperty(cc_allowIpList);
				cc_allowIpList.clear();
				for(String ip : AllowIpProperty_CC.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						cc_allowIpList.add(ip);
					}
				}
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

			break;
		case "DEL-CC-INFO" :
			
			if (tcpMode.equals("SERVER"))
				break;

			if(cc_allowIpList.contains(ipAddress)) {
				cc_allowIpList.remove(ipAddress);
				result = "DELETE ALLOW IP : " + ipAddress;
			} else {
				result = "NOT EXIST IP ADDRESS";
			}
			try {
				AllowIpProperty_CC.setProperty(cc_allowIpList);
				cc_allowIpList.clear();
				for(String ip : AllowIpProperty_CC.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						cc_allowIpList.add(ip);
					}
				}
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

			break;
			
		case "CRTE-BSS-IOT" :
			
			if (tcpMode.equals("SERVER"))
				break;

			if(bss_allowIpList.size() >= 40) {
				result = "ALREADY EXIST MAX ALLOW IP";
				break;
			}
			
			if(bss_allowIpList.contains(ipAddress)) {
				result = "ALREADY EXIST IPADDRESS";
			} else {
				bss_allowIpList.add(ipAddress);
				result = "ADD ALLOW IP : " + ipAddress;
			}
			
			try {
				AllowIpProperty_BSS.setProperty(bss_allowIpList);
				bss_allowIpList.clear();
				for(String ip : AllowIpProperty_BSS.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						bss_allowIpList.add(ip);
					}
				}
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

			break;
		case "DEL-BSS-IOT" :
			
			if (tcpMode.equals("SERVER"))
				break;

			if(bss_allowIpList.contains(ipAddress)) {
				bss_allowIpList.remove(ipAddress);
				result = "DELETE ALLOW IP : " + ipAddress;
			} else {
				result = "NOT EXIST IP ADDRESS";
			}
			try {
				AllowIpProperty_BSS.setProperty(bss_allowIpList);
				bss_allowIpList.clear();
				for(String ip : AllowIpProperty_BSS.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						bss_allowIpList.add(ip);
					}
				}
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

			break;
			
		case "CRTE-OEMP-INFO" :
			
			if (tcpMode.equals("SERVER"))
				break;

			if(cubic_allowIpList.size() >= 40) {
				result = "ALREADY EXIST MAX ALLOW IP";
				break;
			}
			
			if(cubic_allowIpList.contains(ipAddress)) {
				result = "ALREADY EXIST IPADDRESS";
			} else {
				cubic_allowIpList.add(ipAddress);
				result = "ADD ALLOW IP : " + ipAddress;
			}
			
			try {
				AllowIpProperty_CUBIC.setProperty(cubic_allowIpList);
				cubic_allowIpList.clear();
				for(String ip : AllowIpProperty_CUBIC.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						cubic_allowIpList.add(ip);
					}
				}
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

			break;
		case "DEL-OEMP-INFO" :
			
			if (tcpMode.equals("SERVER"))
				break;

			if(cubic_allowIpList.contains(ipAddress)) {
				cubic_allowIpList.remove(ipAddress);
				result = "DELETE ALLOW IP : " + ipAddress;
			} else {
				result = "NOT EXIST IP ADDRESS";
			}
			try {
				AllowIpProperty_CUBIC.setProperty(cubic_allowIpList);
				cubic_allowIpList.clear();
				for(String ip : AllowIpProperty_CUBIC.getPropPath("allow_ip_list").split(",")) {
					if(!ip.trim().equals("")) {
						cubic_allowIpList.add(ip);
					}
				}
			} catch (Exception ex) {
				logger.error("AllowIpProperty Write/Load Excetpion Occured : " + ex.getMessage());
			}
			
			CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);

			break;
			
			
//		case "DIS-PROV-ALLOW-IP" :
//			StringBuffer disAllowIpSB  = new StringBuffer();
//			disAllowIpSB.append("ALLOW IP");
//			disAllowIpSB.append(System.getProperty("line.separator"));
//			disAllowIpSB.append("--------------------");
//			disAllowIpSB.append(System.getProperty("line.separator"));
//			for(String iplist : cc_allowIpList){
//				disAllowIpSB.append(iplist);
//				disAllowIpSB.append(System.getProperty("line.separator"));
//			}
//			disAllowIpSB.append("--------------------");
//			disAllowIpSB.append(System.getProperty("line.separator"));
//			disAllowIpSB.append("TOTAL COUNT = ");
//			disAllowIpSB.append(cc_allowIpList.size());
//			disAllowIpSB.append(System.getProperty("line.separator"));
//			result = disAllowIpSB.toString();
//			CommandConnector.getInstance().sendMessage(curr_appname, command, imsi, ipAddress, result, jobNo);
//			break;
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
				
				CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
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
				
				CommandConnector.getInstance().sendMessage( new MMCMsgType(curr_appname, command, imsi, ipAddress, jobNo, port, tcpMode) , result);
			}
			break;
		}
	}

	public void set_ccAllowIpList(List<String> ccAllowIpList) {
		this.cc_allowIpList = ccAllowIpList;
	}

	public void set_bssAllowIpList(List<String> bssAllowIpList) {
		this.bss_allowIpList = bssAllowIpList;
	}
	
	public void set_cubicAllowIpList(List<String> cubicAllowIpList) {
		this.cubic_allowIpList = cubicAllowIpList;
	}
	
	public synchronized List<String> get_ccAllowIpList() {
		return cc_allowIpList;
	}	
	
	public synchronized List<String> get_bssAllowIpList() {
		return bss_allowIpList;
	}
	
	public synchronized List<String> get_cubicAllowIpList() {
		return cubic_allowIpList;
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

