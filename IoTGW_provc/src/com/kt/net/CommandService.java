package com.kt.net;

import java.net.URLEncoder;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.StatisticsModel;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class CommandService extends Thread{
	
	private static Logger logger = LogManager.getLogger(CommandService.class);

	private String command = new String();
	private int jobNo = 0;
	private String imsi = new String();
	private String msisdn = new String();
	private String ipAddress = new String();
	
	public static void main(String[] args ) {
		System.out.println("main");
		Client client = Client.create();
		WebResource webResource = null;
		ClientResponse response = null;
		String url = "http://192.168.77.133";
		
		client.setConnectTimeout(1000);
		
		webResource = client.resource(url);
		response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);
		
		System.out.println("response.getStatus() : " + response.getStatus());
	}
		
	public CommandService(String command, String imsi, String ipAddress, int jobNo) {
		this.command = command;
		this.imsi = imsi;
		this.jobNo = jobNo;
		this.ipAddress = ipAddress;
	}

	@Override
	public void run() {
		String result = "";
		switch (command.toUpperCase().replaceAll("_", "-")) {
		case "REG-PROV-TRC" :
			if(CommandManager.getInstance().getTraceImsiList().size() >= 40) {
				result = "ALREADY EXIST MAX TRC";
				break;
			}
			if(CommandManager.getInstance().getTraceImsiList().contains(imsi)) {
				result = "ALREADY EXIST IMSI";
			} else {
				CommandManager.getInstance().getTraceImsiList().add(imsi);
				result = "REG TRACE IMSI : " + imsi;
			}
			break;
		case "CANC-PROV-TRC" :
			if(CommandManager.getInstance().getTraceImsiList().contains(imsi)) {
				CommandManager.getInstance().getTraceImsiList().remove(imsi);
				result = "CANCEL TRACE IMSI : " + imsi;
			} else {
				result = "NOT EXIST IMSI";
			}
			break;
		case "DIS-PROV-TRC" :
			StringBuffer disTrcSB  = new StringBuffer();
			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			disTrcSB.append("REG TRACE IMSI LIST");
			disTrcSB.append(System.getProperty("line.separator"));
			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			for(String imsilist : CommandManager.getInstance().getTraceImsiList()){
				disTrcSB.append(imsilist);
				disTrcSB.append(System.getProperty("line.separator"));
			}
			disTrcSB.append("====================");
			disTrcSB.append(System.getProperty("line.separator"));
			result = disTrcSB.toString();
			break;
//		case "ADD-PROV-ALLOW-IP" :
//			if(CommandManager.getInstance().getTraceImsiList().size() >= 40) {
//				result = "ALREADY EXIST MAX ALLOW IP";
//				break;
//			}
//			
//			if(CommandManager.getInstance().getAllowIpList().contains(ipAddress)) {
//				result = "ALREADY EXIST IPADDRESS";
//			} else {
//				CommandManager.getInstance().getAllowIpList().add(ipAddress);
//				result = "ADD ALLOW IP : " + ipAddress;
//			}
//			AllowIpProperty.setProperty(CommandManager.getInstance().getAllowIpList());
//			break;
//		case "DEL-PROV-ALLOW-IP" :
//			if(CommandManager.getInstance().getAllowIpList().contains(ipAddress)) {
//				CommandManager.getInstance().getAllowIpList().remove(ipAddress);
//				result = "DELETE ALLOW IP : " + ipAddress;
//			} else {
//				result = "NOT EXIST IPADDRESS";
//			}
//			AllowIpProperty.setProperty(CommandManager.getInstance().getAllowIpList());
//			break;
//		case "DIS-PROV-ALLOW-IP" :
//			StringBuffer disAllowIpSB  = new StringBuffer();
//			disAllowIpSB.append("====================");
//			disAllowIpSB.append(System.getProperty("line.separator"));
//			disAllowIpSB.append("ALLOW IP LIST");
//			disAllowIpSB.append(System.getProperty("line.separator"));
//			disAllowIpSB.append("====================");
//			disAllowIpSB.append(System.getProperty("line.separator"));
//			for(String iplist : CommandManager.getInstance().getAllowIpList()){
//				disAllowIpSB.append(iplist);
//				disAllowIpSB.append(System.getProperty("line.separator"));
//			}
//			disAllowIpSB.append("====================");
//			disAllowIpSB.append(System.getProperty("line.separator"));
//			result = disAllowIpSB.toString();
//			break;
		}
		
		CommandConnector.getInstance().sendMessage(command, imsi, ipAddress, result, jobNo);
			
	}
}