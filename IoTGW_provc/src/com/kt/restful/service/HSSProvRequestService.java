package com.kt.restful.service;

import java.net.URLEncoder;
import java.util.HashMap;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.net.CommandManager;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.StatisticsModel;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class HSSProvRequestService extends Thread{

	private static Logger logger = LogManager.getLogger(HSSProvRequestService.class);

	private String apiName = new String();
	private String seqNo = new String();
	private String imsi = new String();
	private String msisdn = new String();
	private String ipAddress = new String();
	private int rspCode = -1;
	private String body = new String();

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


	public HSSProvRequestService(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body) {
		this.apiName = apiName;
		this.seqNo = seqNo;
		this.rspCode = rspCode;
		this.imsi = imsi;
		this.msisdn = msisdn;
		this.ipAddress = ipAddress;

		this.body = body;

	}

	@SuppressWarnings("static-access")
	public String getMethod() {		
		String output = "";
		try {
			Client client = Client.create();
			WebResource webResource = null;
			ClientResponse response = null;
			String url = "";

			HashMap<String, String> paramHash = new HashMap<String, String>();

			String[] params = this.body.split(";");
			for(String param : params) {
				paramHash.put(param.split("=")[0], param.split("=")[1]);
			}

			if(ApiDefine.valueOf(apiName).getParamCount() == 1) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0])
						);
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 2) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
						);
			} 


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

				CommandManager.getInstance().sendMessage("TRACE_" + apiName, imsi, "125", sb.toString(), 0);
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

				CommandManager.getInstance().sendMessage("TRACE_" + apiName, imsi, "252", sb.toString(), 0);
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
			String url = "";

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

				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version")
						);

			} else if (apiName.equals("ADD_SUBS")) {
				String[] params1 = this.body.split(";");
				for(int i = 0; i < params1.length; i++) {
					if(i != 0) postBody.append("&");
					postBody.append(params1[i].split("=")[0]);
					postBody.append("=");
					postBody.append(params1[i].split("=")[1]);
				}

				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version")
						);
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 1) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0])
						);
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 2) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
						);
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

				CommandManager.getInstance().sendMessage("TRACE_" + apiName, imsi, "125", sb.toString(), 0);
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

				CommandManager.getInstance().sendMessage("TRACE_" + apiName, imsi, "252", sb.toString(), 0);
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
			String url = "";

			HashMap<String, String> paramHash = new HashMap<String, String>();

			String[] params = this.body.split(";");
			for(String param : params) {
				paramHash.put(param.split("=")[0], param.split("=")[1]);
			}

			String param = "";
			if(apiName.equals("UPT_SUBS")) {
				param = IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2] + "=" +paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2]);
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
						);

				webResource = client.resource(url);
				response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).put(ClientResponse.class, param);
			} else if(ApiDefine.valueOf(apiName).getParamCount() == 1) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0])
						);

			} else if (ApiDefine.valueOf(apiName).getParamCount() == 2) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
						);
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 3) {
				//				String param = paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2]);
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2])
						);
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

				CommandManager.getInstance().sendMessage("TRACE_" + apiName, imsi, "125", sb.toString(), 0);
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

				CommandManager.getInstance().sendMessage("TRACE_" + apiName, imsi, "252", sb.toString(), 0);
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
			String url = "";

			HashMap<String, String> paramHash = new HashMap<String, String>();

			String[] params = this.body.split(";");
			for(String param : params) {
				paramHash.put(param.split("=")[0], param.split("=")[1]);
			}

			if(ApiDefine.valueOf(apiName).getParamCount() == 1) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0])
						);
			} else if(ApiDefine.valueOf(apiName).getParamCount() == 2) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1])
						);
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 3) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2])
						);
			} else if (ApiDefine.valueOf(apiName).getParamCount() == 4) {
				url = String.format(IoTProperty.getPropPath("iudr.api.url." + apiName), 
						this.ipAddress,
						IoTProperty.getPropPath("iudr_hlradapter"),
						IoTProperty.getPropPath("iudr_version"),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[0]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[1]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[2]),
						paramHash.get(IoTProperty.getPropPath("iudr.api.param." + apiName).split(",")[3])
						);
			} 

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

				CommandManager.getInstance().sendMessage("TRACE_" + apiName, imsi, "125", sb.toString(), 0);
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

				CommandManager.getInstance().sendMessage("TRACE_" + apiName, imsi, "252", sb.toString(), 0);
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

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
						new StatisticsModel(apiName, ipAddress, 1, 0, 0));
			}
		}

		String result = ""; 
		switch(apiName) {
		//GET METHOD
		case "GET_APN_BY_PDP_ID" :
		case "GET_APN_BY_PDN_ID" :
		case "GET_ROAM_REST_BY_ID" :
		case "GET_LTE_ROAM_REST_BY_ID" :
		case "GET_SUBS_DETAIL" :
		case "GET_SUBS_MDN_BY_IMSI" :
		case "GET_SUBS_IMSI_BY_MDN" :
		case "GET_HSS_STATIC_IP" :
		case "GET_PDP_FOR_SUBS" :
		case "GET_PDN_FOR_SUBS" :
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

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(rspCode == 200) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
							new StatisticsModel(apiName, ipAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
					if(rspCode != -1) {
						StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusFail();

						if( rspCode == 400)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError400();
						else if (rspCode == 409)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError409();
						else if (rspCode == 410)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError410();
						else if (rspCode == 500)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError500();
						else if (rspCode == 510)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError501();
					}
				} else {
					if(rspCode != -1) {
						StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
								new StatisticsModel(apiName, ipAddress, 0, 0, 1));

						if( rspCode == 400)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError400();
						else if (rspCode == 409)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError409();
						else if (rspCode == 410)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError410();
						else if (rspCode == 500)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError500();
						else if (rspCode == 510)
							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError501();
					}
				}
			}
		}

		DBMManager.getInstance().sendCommand(apiName, seqNo, imsi, msisdn, ipAddress, result, rspCode);
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

