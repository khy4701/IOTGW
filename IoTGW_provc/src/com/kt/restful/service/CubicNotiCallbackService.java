package com.kt.restful.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.ws.rs.core.MediaType;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kt.net.CommandManager;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.IoTProperty;
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

public class CubicNotiCallbackService extends Thread{

	private static Logger logger = LogManager.getLogger(CubicNotiCallbackService.class);

	private String apiName = new String();
	private String seqNo = new String();
	private String imsi = new String();
	private String msisdn = new String();
	private String ipAddress = new String();
	private int rspCode = -1;
	private String body = new String();
	
	private String oSysCode = new String();
	private String tSysCode = new String();
	private String msgId = new String();
	private String msgType = new String();
	private String resultCode = new String();
	private String resultDtlCode = new String();
	private String resultMsg = new String();
	
	private ProvifMsgType provMsg = null;

	public CubicNotiCallbackService(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body, ProvifMsgType pmt) {
		this.apiName = apiName;
		this.seqNo = seqNo;
		this.rspCode = rspCode;
		this.imsi = imsi;
		this.msisdn = msisdn;
		this.ipAddress = ipAddress;

		this.body = body;
		this.provMsg = pmt;
	}

	@SuppressWarnings({ "static-access", "deprecation" })
	public String postMethod() {		

		String output = "";
		try {
			Client client = null;
			WebResource webResource = null;
			ClientResponse response = null;
			String url = this.provMsg.getUrl();
			
			String postBodyStr = this.body;

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVC(POST) -> CUBIC["+ this.ipAddress +"]");
				logger.info("REQUEST URL : " + url );
				logger.info("=============================================");
//                logger.info("O_SYS_CD : " + this.provMsg.getOsysCode());
//                logger.info("T_SYS_CD : " + this.provMsg.getTsysCode());
//                logger.info("MSG_ID : " + this.provMsg.getMsgId());
//                logger.info("MSG_TYPE : " + this.provMsg.getMsgType());
//                logger.info("RESULT_CD : " + this.provMsg.getResultCode());
//                logger.info("RESULT_DTL_CD : " + this.provMsg.getResultDtlCode());
//                logger.info("RESULT_MSG : " + this.provMsg.getResultMsg());				
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

			String imsi = null;
			String eid = null;
			String iccid = null;
			
			try{
				imsi = jsonObj.get("IMSI").toString();
			}catch(Exception e){
				imsi = null;
			}
			
			
			try{
				eid = jsonObj.get("EID").toString();
			}catch(Exception e){
				eid = null;
			}
			
			try{
				iccid = jsonObj.get("ICCID").toString();
			}catch(Exception e){
				iccid = null;
			}
			
			
			// TRACE??
			boolean traceFlag = false;
			if(	CommandManager.getInstance().getTraceImsiList().contains(imsi) || 
					CommandManager.getInstance().getTraceImsiList().contains(eid) ||
					CommandManager.getInstance().getTraceImsiList().contains(iccid)){
				traceFlag = true;			
			}
			
        	String encData = "";
        	String keyVal = "";

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : PROVC(POST) -> CUBIC["+ this.ipAddress +"]");
				sb.append(System.getProperty("line.separator"));
				sb.append("REQUEST URL : " + url );
				sb.append(System.getProperty("line.separator"));
//				sb.append("O_SYS_CD : " + this.provMsg.getOsysCode());
//				sb.append(System.getProperty("line.separator"));
//				sb.append("T_SYS_CD : " + this.provMsg.getTsysCode());
//				sb.append(System.getProperty("line.separator"));
//				sb.append("MSG_ID   : " + this.provMsg.getMsgId());
//				sb.append(System.getProperty("line.separator"));
//				sb.append("MSG_TYPE : " + this.provMsg.getMsgType());
//				sb.append(System.getProperty("line.separator"));
//				sb.append("RESULT_CD : " + this.provMsg.getResCode());
//				sb.append(System.getProperty("line.separator"));
//				sb.append("RESULT_DTL_CD : " + this.provMsg.getResultDtlCode());
//				sb.append(System.getProperty("line.separator"));
//				sb.append("RESULT_MSG : " + this.provMsg.getResultMsg());
//				sb.append(System.getProperty("line.separator"));
				sb.append("BODY : " + postBodyStr );
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                if(CommandManager.getInstance().getTraceImsiList().contains(imsi))
                	CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "125", "0", "", ""), sb.toString());
                else if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                	CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, eid, "125", "0", "", ""), sb.toString());
                else if (CommandManager.getInstance().getTraceImsiList().contains(iccid)){
                	
					encData = AES256Util.getInstance().AES_Decode(iccid);					
					if (encData == null)
						keyVal = iccid;
					else
						keyVal = iccid+"("+encData+")";

                	CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, keyVal, "125", "0", "", ""), sb.toString());
                }

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
//					.header("O_SYS_CD", this.provMsg.getOsysCode())
//					.header("T_SYS_CD", this.provMsg.getTsysCode())
//					.header("MSG_ID", this.provMsg.getMsgId())
//					.header("MSG_TYPE", this.provMsg.getMsgType())
//					.header("RESULT_CD", this.provMsg.getResultCode())
//					.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
//					.header("RESULT_MSG", this.provMsg.getResultMsg())
					.post(ClientResponse.class, postBodyStr);

			if (response.getStatus() == 200) {
				rspCode = 1;
				output = response.getEntity(String.class);
			} else {
				rspCode = 0;
				output = "";
			}
			rspCode = response.getStatus();
						
//			oSysCode = response.getHeaders().getFirst("O_SYS_CD");
//			tSysCode = response.getHeaders().getFirst("T_SYS_CD");
//			msgId = response.getHeaders().getFirst("MSG_ID");
//			msgType = response.getHeaders().getFirst("MSG_TYPE");
//			resultCode = response.getHeaders().getFirst("RESULT_CD");
//			resultDtlCode = response.getHeaders().getFirst("RESULT_DTL_CD");
//			resultMsg = response.getHeaders().getFirst("RESULT_MSG");

						
			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("CUBIC["+ this.ipAddress +"] -> PROVC(POST)");
				logger.info("STATUS : " + response.getStatus() );
				logger.info(output);
				logger.info("=============================================");
			}

			if(traceFlag) {
				StringBuffer sb = new StringBuffer();
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));
				sb.append("DIRECTION : CUBIC["+ this.ipAddress +"] -> PROVC(POST)");
				sb.append(System.getProperty("line.separator"));
				sb.append("STATUS : " + response.getStatus());
				sb.append(System.getProperty("line.separator"));
//				sb.append("O_SYS_CD : " + oSysCode);
//				sb.append(System.getProperty("line.separator"));
//				sb.append("T_SYS_CD : " + tSysCode);
//				sb.append(System.getProperty("line.separator"));
//				sb.append("MSG_ID   : " + msgId);
//				sb.append(System.getProperty("line.separator"));
//				sb.append("MSG_TYPE : " + msgType);
//				sb.append(System.getProperty("line.separator"));
//				sb.append("RESULT_CD : " + resultCode);
//				sb.append(System.getProperty("line.separator"));
//				sb.append("RESULT_DTL_CD : " + resultDtlCode);
//				sb.append(System.getProperty("line.separator"));
//				sb.append("RESULT_MSG : " + resultMsg);
//				sb.append(System.getProperty("line.separator"));
				sb.append("BODY : " + output);
				sb.append(System.getProperty("line.separator"));
				sb.append("=============================================");
				sb.append(System.getProperty("line.separator"));

                String appName = IoTProperty.getPropPath("sys_name");
                String command = "TRACE_" + apiName;

                if(CommandManager.getInstance().getTraceImsiList().contains(imsi))
                	CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "252", "0", "", ""), sb.toString());
                else if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                	CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, eid, "252", "0", "", ""), sb.toString());
                else if (CommandManager.getInstance().getTraceImsiList().contains(iccid)){
					encData = AES256Util.getInstance().AES_Decode(iccid);					
					if (encData == null)
						keyVal = iccid;
					else
						keyVal = iccid+"("+encData+")";

                	
                	CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, keyVal, "252", "0", "", ""), sb.toString());
                }
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
		
		oSysCode = this.provMsg.getOsysCode(); 
		
		synchronized (StatisticsManager.getInstance().getStatistics_CUBICHash()) {
			if(StatisticsManager.getInstance().getStatistics_CUBICHash().containsKey(ipAddress+apiName)) {
				StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatistics_CUBICHash().put(ipAddress+apiName, 
						new StatisticsModel(oSysCode, apiName, ipAddress, 1, 0, 0));
			}
		}

		String result = ""; 
		
		if ( apiName.equals("SUBS_STS_CHG_NOTI") ||  apiName.equals("DOWN_PROF_CLBK") || 
				apiName.equals("PROF_STS_CHG_CLBK") || apiName.equals("PROF_STS_CHG_NOTI"))
			result = postMethod();
		else{
			logger.info("NOT ALLOWED API NAME : " + apiName);
			return ;
		}
			

		synchronized (StatisticsManager.getInstance().getStatistics_CUBICHash()) {
			if(rspCode == 200) {
				if(StatisticsManager.getInstance().getStatistics_CUBICHash().containsKey(ipAddress+apiName)) {
					StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatistics_CUBICHash().put(ipAddress+apiName, 
							new StatisticsModel(oSysCode, apiName, ipAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatistics_CUBICHash().containsKey(ipAddress+apiName)) {
					if(rspCode != -1) {
						StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusFail();

						if( rspCode == 400)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError400();
						else if (rspCode == 403)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError403();
						else if (rspCode == 409)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError409();
						else if (rspCode == 410)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError410();
						else if (rspCode == 500)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError500();
						else if (rspCode == 501)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError501();
						else if (rspCode == 503)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError503();
						else
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusErrorEtc();
					}
				} else {
					if(rspCode != -1) {
						StatisticsManager.getInstance().getStatistics_CUBICHash().put(ipAddress+apiName, 
								new StatisticsModel(oSysCode, apiName, ipAddress, 0, 0, 1));

						if( rspCode == 400)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError400();
						else if (rspCode == 403)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError403();
						else if (rspCode == 409)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError409();
						else if (rspCode == 410)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError410();
						else if (rspCode == 500)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError500();
						else if (rspCode == 501)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError501();
						else if (rspCode == 503)
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusError503();
						else
							StatisticsManager.getInstance().getStatistics_CUBICHash().get(ipAddress+apiName).plusErrorEtc();
					}
				}
			}
		}
		
		ProvifMsgType pmt = new ProvifMsgType();
		
		pmt.setSeqNo(seqNo);
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(ipAddress);
		
		pmt.setOsysCode(oSysCode);
		pmt.setTsysCode(tSysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resultCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);
			
		// HSS-IOT 에서 온 Response Body값은 Header에 실어서 전송.
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

