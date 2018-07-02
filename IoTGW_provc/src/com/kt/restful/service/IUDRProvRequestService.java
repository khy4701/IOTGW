package com.kt.restful.service;

import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kt.net.CommandManager;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.MMCMsgType;
import com.kt.restful.model.ProvifMsgType;
import com.kt.restful.model.StatisticsModel;
import com.kt.util.AES256Util;
import com.kt.util.SSLUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public class IUDRProvRequestService extends Thread{

	private static Logger logger = LogManager.getLogger(IUDRProvRequestService.class);

	private String apiName = new String();
	private String seqNo = new String();
	private String imsi = new String();
	private String msisdn = new String();
	private String ipAddress = new String();
	private int rspCode = -1;
	private String body = new String();
	
	String oSysCode = new String();
	String tSysCode = new String();
	String msgId = new String();
	String msgType = new String();
	String resultCode = new String();
	String resultDtlCode = new String();
	String resultMsg = new String();			

	private ProvifMsgType provMsg = null;

	public static void main(String[] args ) {
		System.out.println("main");
		Client client = Client.create();
		WebResource webResource = null;
		ClientResponse response = null;
		String url = "http://192.168.77.133";

		client.setConnectTimeout(1000);

		webResource = client.resource(url);
		response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED)
				.get(ClientResponse.class);

		System.out.println("response.getStatus() : " + response.getStatus());
	}


	public IUDRProvRequestService(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body, ProvifMsgType pmt) {
		this.apiName = apiName;
		this.seqNo = seqNo;
		this.rspCode = rspCode;
		this.imsi = imsi;
		this.msisdn = msisdn;
		this.ipAddress = ipAddress;

		this.body = body;
		this.provMsg = pmt;

	}

	@SuppressWarnings("static-access")
	public String getMethod() {		
		String output = "";
		try {
			Client client = Client.create();
			WebResource webResource = null;
			ClientResponse response = null;
			String url = this.provMsg.getUrl();

			HashMap<String, String> paramHash = new HashMap<String, String>();

			String[] params = this.body.split(";");
			for(String param : params) {
				paramHash.put(param.split("=")[0], param.split("=")[1]);
			}

//			if(ApiDefine.valueOf(apiName).getParamCount() == 1) {
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0])
//						);
//			} else if (ApiDefine.valueOf(apiName).getParamCount() == 2) {
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
//						);
//			} 


			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVC(GET) -> IUDR["+ this.ipAddress +"]");
				logger.info("REQUEST URL : " + url );
				logger.info("=============================================");
			}

			boolean traceFlag = false;
			if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
				traceFlag = true;
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : PROVC(GET) -> IUDR["+ this.ipAddress +"]");
				sb.append(System.getProperty("line.separator"));
				sb.append("REQUEST URL : " + url );
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "125", "0", "", ""), sb.toString());
			}

			webResource = client.resource(url);
			response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);

			if (response.getStatus() == 200) {
				rspCode = 1;
				output = response.getEntity(String.class);
			} else {
				rspCode = 0;
				output = "";
			}

			rspCode = response.getStatus();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("IUDR["+ this.ipAddress +"] -> PROVC(GET)");
				logger.info("STATUS : " + response.getStatus() );
				logger.info(output);
				logger.info("=============================================");
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : IUDR["+ this.ipAddress +"] -> PROVC(GET)");
				sb.append(System.getProperty("line.separator"));
				sb.append("STATUS : " + response.getStatus());
				sb.append(System.getProperty("line.separator"));
				sb.append("BODY");
				sb.append(System.getProperty("line.separator"));
				sb.append(output);
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                
                
                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "252", "0", "", ""), sb.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			rspCode = -1;
			return "";
		}

		return output;
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	public String postMethod() {		

		String output = "";
		try {
			Client client = Client.create();
			WebResource webResource = null;
			ClientResponse response = null;
			String url = this.provMsg.getUrl();

			HashMap<String, String> paramHash = new HashMap<String, String>();

			String[] params = this.body.split(";");
			for(String param : params) {
				paramHash.put(param.split("=")[0], param.split("=")[1]);
			}

			StringBuffer postBody = new StringBuffer(); 
			if(apiName.equals("ADD_HLR_TEMP")) {
				String[] params1 = this.body.split(";");

				for(int i = 0; i < params1.length; i++) {
					if(i != 0) postBody.append("&");
					postBody.append(params1[i].split("=")[0]);
					postBody.append("=");
					postBody.append(URLEncoder.encode(params1[i].split("=")[1]));
				}

//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version")
//						);

			} else if (apiName.equals("ADD_SUBS")) {
				String[] params1 = this.body.split(";");
				for(int i = 0; i < params1.length; i++) {
					if(i != 0) postBody.append("&");
					postBody.append(params1[i].split("=")[0]);
					postBody.append("=");
					postBody.append(params1[i].split("=")[1]);
				}

//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version")
//						);
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 1) {
				;
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0])
//						);
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 2) {
				;
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
//						);
			}

			String postBodyStr = postBody.toString();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVC(POST) -> IUDR["+ this.ipAddress +"]");
				logger.info("REQUEST URL : " + url );
				logger.info("BODY : " + postBodyStr );
				logger.info("=============================================");
			}

			boolean traceFlag = false;
			if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
				traceFlag = true;
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : PROVC(POST) -> IUDR["+ this.ipAddress +"]");
				sb.append(System.getProperty("line.separator"));
				sb.append("REQUEST URL : " + url );
				sb.append(System.getProperty("line.separator"));
				sb.append("BODY : " + postBodyStr );
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "125", "0", "", ""), sb.toString());
			}

			webResource = client.resource(url);
			response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).post(ClientResponse.class, postBodyStr);

			if (response.getStatus() == 200) {
				rspCode = 1;
				output = response.getEntity(String.class);
			} else {
				rspCode = 0;
				output = "";
			}
			rspCode = response.getStatus();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("IUDR["+ this.ipAddress +"] -> PROVC(POST)");
				logger.info("STATUS : " + response.getStatus() );
				logger.info(output);
				logger.info("=============================================");
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : IUDR["+ this.ipAddress +"] -> PROVC(POST)");
				sb.append(System.getProperty("line.separator"));
				sb.append("STATUS : " + response.getStatus());
				sb.append(System.getProperty("line.separator"));
				sb.append("BODY");
				sb.append(System.getProperty("line.separator"));
				sb.append(output);
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                    CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "252", "0", "", ""), sb.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			rspCode = -1;
			return "";
		}

		return output;
	}

	@SuppressWarnings("static-access")
	public String putMethod() {		

		String output = "";
		try {
			Client client = Client.create();
			WebResource webResource = null;
			ClientResponse response = null;
			String url = this.provMsg.getUrl();

			HashMap<String, String> paramHash = new HashMap<String, String>();

			String[] params = this.body.split(";");
			for(String param : params) {
				paramHash.put(param.split("=")[0], param.split("=")[1]);
			}

			String param = "";
			if(apiName.equals("UPT_SUBS")) {
				param = IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2] + "=" +paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2]);
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
//						);

				webResource = client.resource(url);
				response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).put(ClientResponse.class, param);
			} else if(ApiDefine.valueOf(apiName).getParamCount() == 1) {
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0])
//						);
				;

			} else if (ApiDefine.valueOf(apiName).getParamCount() == 2) {
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
//						);
				;
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 3) {
				//				String param = paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2]);
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2])
//						);
				;
			} 

			if(!apiName.equals("UPT_SUBS")) {
				webResource = client.resource(url);
				response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).put(ClientResponse.class);
			}

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVC(PUT) -> IUDR["+ this.ipAddress +"]");
				logger.info("REQUEST URL : " + url );
				logger.info("=============================================");
			}

			boolean traceFlag = false;
			if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
				traceFlag = true;
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : PROVC(PUT) -> IUDR["+ this.ipAddress +"]");
				sb.append(System.getProperty("line.separator"));
				sb.append("REQUEST URL : " + url );
				sb.append(System.getProperty("line.separator"));
				sb.append("BODY : " + param );
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "125", "0", "", ""), sb.toString());
			}

			if (response.getStatus() == 200) {
				rspCode = 1;
				output = response.getEntity(String.class);
			} else {
				rspCode = 0;
				output = "";
			}
			rspCode = response.getStatus();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("IUDR["+ this.ipAddress +"] -> PROVC(PUT)");
				logger.info("STATUS : " + response.getStatus() );
				logger.info(output);
				logger.info("=============================================");
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : IUDR["+ this.ipAddress +"] -> PROVC(PUT)");
				sb.append(System.getProperty("line.separator"));
				sb.append("STATUS : " + response.getStatus());
				sb.append(System.getProperty("line.separator"));
				sb.append("BODY");
				sb.append(System.getProperty("line.separator"));
				sb.append(output);
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                    CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "252", "0", "", ""), sb.toString());
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			rspCode = -1;
			return "";
		}

		return output;
	}

	@SuppressWarnings("static-access")
	public String deleteMethod() {		

		String output = "";
		try {
			Client client = Client.create();
			WebResource webResource = null;
			ClientResponse response = null;
			String url = this.provMsg.getUrl();

			HashMap<String, String> paramHash = new HashMap<String, String>();

			String[] params = this.body.split(";");
			for(String param : params) {
				paramHash.put(param.split("=")[0], param.split("=")[1]);
			}

//			if(ApiDefine.valueOf(apiName).getParamCount() == 1) {
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0])
//						);
//			} else if(ApiDefine.valueOf(apiName).getParamCount() == 2) {
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
//						);
//			} else if (ApiDefine.valueOf(apiName).getParamCount() == 3) {
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2])
//						);
//			} else if (ApiDefine.valueOf(apiName).getParamCount() == 4) {
//				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
//						this.ipAddress,
//						IoTProperty.getPropPath("iudr_hlradapter"),
//						IoTProperty.getPropPath("iudr_version"),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2]),
//						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[3])
//						);
//			} 

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVC(DEL) -> IUDR["+ this.ipAddress +"]");
				logger.info("REQUEST URL : " + url );
				logger.info("=============================================");
			}

			boolean traceFlag = false;
			if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
				traceFlag = true;
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : PROVC(DEL) -> IUDR["+ this.ipAddress +"]");
				sb.append(System.getProperty("line.separator"));
				sb.append("REQUEST URL : " + url );
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "125", "0", "", ""), sb.toString());
			}

			webResource = client.resource(url);
			response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).delete(ClientResponse.class);

			if (response.getStatus() == 200) {
				rspCode = 1;
				output = response.getEntity(String.class);
			} else {
				rspCode = 0;
				output = "";
			}
			rspCode = response.getStatus();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("IUDR["+ this.ipAddress +"] -> PROVC(DEL)");
				logger.info("STATUS : " + response.getStatus() );
				logger.info(output);
				logger.info("=============================================");
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : IUDR["+ this.ipAddress +"] -> PROVC(DEL)");
				sb.append(System.getProperty("line.separator"));
				sb.append("STATUS : " + response.getStatus());
				sb.append(System.getProperty("line.separator"));
				sb.append("BODY");
				sb.append(System.getProperty("line.separator"));
				sb.append(output);
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                    CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "252", "0", "", ""), sb.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			rspCode = -1;
			return "";
		}

		return output;
	}

	@SuppressWarnings("static-access")
	public String cubicPostMethod() {		

		String output = "";
		try {
			Client client = null;
			WebResource webResource = null;
			ClientResponse response = null;
			String url = this.provMsg.getUrl();
			
			String postBodyStr = this.body;

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVC(POST) -> IUDR["+ this.ipAddress +"]");
				logger.info("REQUEST URL : " + url );
				logger.info("=============================================");
                logger.info("O_SYS_CD : " + this.provMsg.getOsysCode());
                logger.info("T_SYS_CD : " + this.provMsg.getTsysCode());
                logger.info("MSG_ID : " + this.provMsg.getMsgId());
                logger.info("MSG_TYPE : " + this.provMsg.getMsgType());
                logger.info("RESULT_CD : " + this.provMsg.getResultCode());
                logger.info("RESULT_DTL_CD : " + this.provMsg.getResultDtlCode());
                logger.info("RESULT_MSG : " + this.provMsg.getResultMsg());				
				logger.info("BODY : " + postBodyStr );
				logger.info("=============================================");
			}

			
			JSONObject jsonObj = null;
			try{
				jsonObj = new JSONObject(postBodyStr);
			}				
			catch(Exception e){
				logger.error("Json Parsing Error  : " + postBodyStr);	
			}
			
			String imsiData ="";			
			try{
				imsiData = jsonObj.get("IMSI").toString();
			}catch(Exception e){
				imsiData = null;
			}			
			
			boolean traceFlag = false;
			if(CommandManager.getInstance().getTraceImsiList().contains(imsiData)){
				traceFlag = true;
			}
			
            String encData = "";
            String keyValue = "";
			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : PROVC(POST) -> BSSIOT["+ this.ipAddress +"]");
				sb.append(System.getProperty("line.separator"));
				sb.append("REQUEST URL : " + url );
				sb.append(System.getProperty("line.separator"));
				sb.append("BODY : " + postBodyStr );
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;
                                
				encData = AES256Util.getInstance().AES_Decode(imsiData);			
				if (encData == null)
					keyValue = imsiData;
				else
					keyValue = imsiData+"("+encData+")";


                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, keyValue, "125", "0", "", ""), sb.toString());
			}

			// URL Routing--> HTTP ? HTTPS?			
			if (url.substring(0, 5).equals("https")){
				System.out.println("HTTPS");
				
		        ClientConfig config = new DefaultClientConfig();
		        try {
					config.getProperties()
					        .put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
					                new HTTPSProperties(
					                        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER,
					                        SSLUtil.getInsecureSSLContext()));
				} catch (KeyManagementException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
				client = Client.create(config);
			}

			else if (url.substring(0, 4).equals("http")){
				System.out.println("HTTP");
				client = Client.create();
			}
			
			webResource = client.resource(url);
			response = webResource.type(MediaType.APPLICATION_JSON_TYPE)
					.header("charset", "UTF-8")
					.header("O_SYS_CD", this.provMsg.getOsysCode())
					.header("T_SYS_CD", this.provMsg.getTsysCode())
					.header("MSG_ID", this.provMsg.getMsgId())
					.header("MSG_TYPE", this.provMsg.getMsgType())
					.header("RESULT_CD", this.provMsg.getResultCode())
					.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
					.header("RESULT_MSG", this.provMsg.getResultMsg())
					.post(ClientResponse.class, postBodyStr);

			output = response.getEntity(String.class);
			if (response.getStatus() == 200) {
				rspCode = 1;
				//output = response.getEntity(String.class);
			} else {
				rspCode = 0;
				//output = "";
			}
			rspCode = response.getStatus();
						
			oSysCode = response.getHeaders().getFirst("O_SYS_CD");
			tSysCode = response.getHeaders().getFirst("T_SYS_CD");
			msgId = response.getHeaders().getFirst("MSG_ID");
			msgType = response.getHeaders().getFirst("MSG_TYPE");
			resultCode = response.getHeaders().getFirst("RESULT_CD");
			resultDtlCode = response.getHeaders().getFirst("RESULT_DTL_CD");
			resultMsg = response.getHeaders().getFirst("RESULT_MSG");			

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("IUDR["+ this.ipAddress +"] -> PROVC(POST)");
				logger.info("STATUS : " + response.getStatus() );
				logger.info("BODY: " + output);
				logger.info("=============================================");
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : IUDR["+ this.ipAddress +"] -> PROVC(POST)");
				sb.append(System.getProperty("line.separator"));
				sb.append("STATUS : " + response.getStatus());
				sb.append(System.getProperty("line.separator"));
				sb.append("BODY : ");
				sb.append(System.getProperty("line.separator"));
				sb.append(output);
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

				encData = AES256Util.getInstance().AES_Decode(imsiData);			
				if (encData == null)
					keyValue = imsiData;
				else
					keyValue = imsiData+"("+encData+")";

                
                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, keyValue, "252", "0", "", ""), sb.toString());
			}

		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e.getMessage());
			rspCode = -1;
			return "";
		}

		return output;
	}
	
	@SuppressWarnings("static-access")
	@Override
	public synchronized void run() {

		String osysCode = "";
		
		// CUBIC -> IUDR API( INQUIRY )
		if (apiName.equals("INQUIRY"))
			osysCode = this.provMsg.getOsysCode();
		else
		// CC    -> IUDR API
			osysCode = new String("CC");
		
		logger.info("OSYSCODE : " + osysCode + ", STATIC_NAME " + ipAddress+apiName);
		
		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
						new StatisticsModel(osysCode , apiName, ipAddress, 1, 0, 0));
			}
		}

		String result = ""; 
		switch(apiName) {
		
		case "INQUIRY":
			result = cubicPostMethod();
			break;
		
		//GET METHOD
		case "GET_APN_BY_PDP_ID" :
		case "GET_APN_BY_PDN_ID" :
		case "GET_ROAM_REST_BY_ID" :
		case "GET_LTE_ROAM_REST_BY_ID" :
		case "GET_SUBS_DETAIL" :
		case "GET_SUBS_MDN_BY_IMSI" :
		case "GET_SUBS_IMSI_BY_MDN" :
		case "GET_HSS_STATIC_IP" :
		case "GET_APN_FOR_SUBS" :
		case "GET_HSS_APN_FOR_SUBS" :
		case "GET_ROAM_REST_FOR_SUBS" :
		case "GET_LTE_ROAM_REST_FOR_SUBS" :
		case "GET_SGSN_LOC_FOR_SUBS" :
		case "GET_MSC_LOC_FOR_SUBS" :
		case "GET_MME_LOC_FOR_SUBS" :
		case "GET_STATIC_IP" :
		case "GET_HLR_TEMP" :
			result = getMethod();
			break;
			//POST METHOD
		case "ADD_HLR_TEMP" :
		case "ADD_SUBS" :
		case "SET_NETWORK_BAR" :
		case "SET_NETWORK_UNBAR" :
		case "SET_LTE_NETWORK_BAR" :
		case "SET_LTE_NETWORK_UNBAR" :
		case "SND_CANCEL_LOC" :
		case "SND_LTE_CANCEL_LOC" :
			result = postMethod();
			break;
			//PUT METHOD
		case "MOD_HSS_STATIC_IP" :
		case "UPT_SUBS" :
		case "UPT_SUBS_ATT" :
		case "MOD_STATIC_IP" :
			result = putMethod();
			break;
			
			//DELETE
		case "DEL_SUBS" :
		case "REM_HSS_STATIC_IP" :
		case "REM_STATIC_IP" :
		case "DEL_HLR_TEMP" :
			result = deleteMethod();
			break;
		}

		logger.info("OSYSCODE : " + osysCode + ", STATIC_NAME " + ipAddress+apiName);

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(rspCode == 200) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
							new StatisticsModel(osysCode, apiName, ipAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
					if(rspCode != -1) {
						StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusFail();

						if( rspCode == 400)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError400();
						else if (rspCode == 403)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError403();
						else if (rspCode == 409)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError409();
						else if (rspCode == 410)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError410();
						else if (rspCode == 500)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError500();
						else if (rspCode == 501)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError501();
						else if (rspCode == 503)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError503();
						else
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusErrorEtc();
					}
				} else {
					if(rspCode != -1) {
						StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
								new StatisticsModel(osysCode, apiName, ipAddress, 0, 0, 1));

						if( rspCode == 400)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError400();
						else if (rspCode == 403)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError403();
						else if (rspCode == 409)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError409();
						else if (rspCode == 410)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError410();
						else if (rspCode == 500)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError500();
						else if (rspCode == 501)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError501();
						else if (rspCode == 503)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError503();
						else
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusErrorEtc();
					}
				}
			}
		}

		
		ProvifMsgType pmt = new ProvifMsgType();
		
		pmt.setSeqNo(seqNo);
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(ipAddress);		
		
		if (apiName.equals("INQUIRY")){
			pmt.setOsysCode(oSysCode);
			pmt.setTsysCode(tSysCode);
			pmt.setMsgId(msgId);
			pmt.setMsgType(msgType);
			pmt.setResultCode(resultCode);
			pmt.setResultDtlCode(resultDtlCode);
			pmt.setResultMsg(resultMsg);
		}

		
		DBMManager.getInstance().sendCommand(apiName, result, rspCode, pmt);
		//		if(result.trim().equals("4000")) {
		//			DBMManager.getInstance().sendCommand(apiName, seqNo, imsi, msisdn, ipAddress, result, 4000);
		//		} else if (result.trim().equals("4002")) {
		//			DBMManager.getInstance().sendCommand(apiName, seqNo, imsi, msisdn, ipAddress, result, 4002);
		//		} else if (result.trim().equals("2300")) {
		//			DBMManager.getInstance().sendCommand(apiName, seqNo, imsi, msisdn, ipAddress, result, 2300);
		//		} else if(rspCode != -1) {
		//			DBMManager.getInstance().sendCommand(apiName, seqNo, imsi, msisdn, ipAddress, result, rspCode);
		//		} 

	}
}

