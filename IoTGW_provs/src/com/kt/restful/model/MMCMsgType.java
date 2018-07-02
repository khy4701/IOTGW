package com.kt.restful.model;

import java.io.DataInputStream;
import java.io.IOException;

import com.kt.util.Util;

//typedef struct {
//    int     bodyLen;
//    int     mapType;    
//    int     mtype;   
//} SockLibHeadType;  
//
//#define SOCKLIB_HEAD_LEN        sizeof(SockLibHeadType)
//#define SOCKLIB_MAX_BODY_LEN    (16384)-(SOCKLIB_HEAD_LEN)
//typedef struct {
//    SockLibHeadType head;  
//    char            body[SOCKLIB_MAX_BODY_LEN];
//} SockLibMsgType;	
//
//typedef struct {
//    char appName[32];
//    char command[32];
//    char imsi[32];
//    char ipAddress[64];
//    char jobNumber[8];
//} ProvMmcRequestHeadType;
//
//typedef struct {
//    ProvMmcRequestHeadType head;
//    char body[1024*4];
//} ProvMmcRequest;

public class MMCMsgType {
	/* ProvLibHeadType */
	
	private String appName; // provs1, provs2..
	private String command;
	private String imsi;
	private String ipAddress;
	private String jobNumber;
	private String port;
	private String tcpMode;
	
	private static int APPNAME_LEN = 32;
	private static int COMMAND_LEN = 32;
	private static int IMSI_LEN = 40;
	private static int IP_LEN = 64;
	private static int JOB_LEN = 8;
	private static int PORT_LEN = 8;
	private static int TCPMODE_LEN = 8;
	
	public MMCMsgType(){
		
	}
	
	public MMCMsgType(String appName, String command, String imsi, String ipAddress, String jobNumber, String port, String tcpMode){
		this.appName = appName;
		this.command = command;
		this.imsi = imsi;
		this.ipAddress = ipAddress;
		this.jobNumber = jobNumber;
		this.port = port;
		this.tcpMode = tcpMode;		
	}
	
	public static int getProvMmcHeaderSize(){
		
		int bodyTotalLen = APPNAME_LEN + COMMAND_LEN + IMSI_LEN + IP_LEN + JOB_LEN + PORT_LEN + TCPMODE_LEN;
		return bodyTotalLen;		
	}
	
	public static int getAppNameLen(){
		return APPNAME_LEN;
	}
	
	public static int getCommandLen(){
		return COMMAND_LEN;
	}
	
	public static int getImsiLen(){
		return IMSI_LEN;
	}
	
	public static int getIpLen(){
		return IP_LEN;
	}
	
	public static int getJobLen(){
		return JOB_LEN;
	}
	
	public static int getPortLen(){
		return PORT_LEN;
	}
	
	public static int getTcpmodeLen(){
		return TCPMODE_LEN;
	}
	
	public void setMMCMsgType(DataInputStream din) throws IOException {
				
		byte[] appnameBuffer = new byte[APPNAME_LEN];
		byte[] commandBuffer = new byte[COMMAND_LEN];
		byte[] imsiBuffer = new byte[IMSI_LEN];
		byte[] ipAddressBuffer = new byte[IP_LEN];
		byte[] jobNumberBuffer = new byte[JOB_LEN];
		byte[] portBuffer = new byte[PORT_LEN];
		byte[] tcpmodeBuffer = new byte[TCPMODE_LEN];		

		din.read(appnameBuffer, 0, appnameBuffer.length);
		this.appName = Util.nullTrim(new String(appnameBuffer));
		
		din.read(commandBuffer, 0, commandBuffer.length);
		this.command = Util.nullTrim(new String(commandBuffer));

		din.read(imsiBuffer, 0, imsiBuffer.length);
		this.imsi = Util.nullTrim(new String(imsiBuffer));

		din.read(ipAddressBuffer, 0, ipAddressBuffer.length);
		this.ipAddress = Util.nullTrim(new String(ipAddressBuffer));
		
		din.read(jobNumberBuffer, 0, jobNumberBuffer.length);
		this.jobNumber = Util.nullTrim(new String(jobNumberBuffer));
		
		din.read(portBuffer, 0, portBuffer.length);
		this.port = Util.nullTrim(new String(portBuffer));

		din.read(tcpmodeBuffer, 0, tcpmodeBuffer.length);
		this.tcpMode = Util.nullTrim(new String(tcpmodeBuffer));

	}

	public String getAppName() {
		return appName;
	}

	public String getCommand() {
		return command;
	}

	public String getImsi() {
		return imsi;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public String getJobNumber() {
		return jobNumber;
	}

	public String getPort() {
		return port;
	}

	public String getTcpMode() {
		return tcpMode;
	}
	
	
}
