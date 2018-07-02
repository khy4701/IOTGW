//package com.kt.restful.service.cubic;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.ws.rs.Encoded;
//import javax.ws.rs.HeaderParam;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.Context;
//import javax.ws.rs.core.Response;
//
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//import org.codehaus.jackson.annotate.JsonProperty;
//import org.json.JSONObject;
//
//import com.kt.net.CommandManager;
//import com.kt.net.DBMConnector;
//import com.kt.net.DBMListener;
//import com.kt.net.DBMManager;
//import com.kt.net.StatisticsManager;
//import com.kt.restful.constants.IoTProperty;
//import com.kt.restful.model.ApiDefine;
//import com.kt.restful.model.MMCMsgType;
//import com.kt.restful.model.ProvifMsgType;
//import com.kt.restful.model.StatisticsModel;
//
//// provs2
//@Path("/downProf")
//@Produces("application/json;charset=UTF-8")
//
//public class CallbackMasage implements DBMListener{
//
//	private static Logger logger = LogManager.getLogger(CallbackMasage.class);
//	
//	@SuppressWarnings("static-access")
//	@POST 	
//	@Path("/callback")
//	@Produces("application/json;charset=UTF-8")
//	public Response callBackManager(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
//			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
//			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
//		
//		String remoteAddr = req.getRemoteAddr();
//		String apiName = ApiDefine.DOWNLOAD_PROF_CALLBACK.getName();
//		
//		// 01. Read Json Parameter		
//		JSONObject jsonObj = null;
//		try{
//			jsonObj = new JSONObject(jsonBody);
//		}				
//		catch(Exception e){
//			logger.error("Json Parsing Error  : " + jsonBody);	
//			return Response.status(400).entity("Request Error").build();
//		}
//		
//		String eid = "";
//		String iccid = "";
//		
//		try{
//			eid = jsonObj.get("EID").toString();
//		}catch(Exception e){
//			eid = null;
//			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
//		}
//		
//		try{
//			iccid = jsonObj.get("ICCID").toString();
//		}catch(Exception e){
//			iccid = null;
//		}
//
//								
//        if(CommandManager.getInstance().isLogFlag()) {
//            logger.info("=============================================");
//            logger.info("BSS-IOT -> PROVS [" + apiName + "]");
//            logger.info("=============================================");
//            logger.info("REQUEST URL : " + req.getRequestURL().toString());
//            logger.info("HEADER -------------------------------------");
//            logger.info("BODY : " + jsonBody);
//            logger.info("=============================================");
//        }
//        
//    	if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
//    		synchronized (StatisticsManager.getInstance().getStatisticsHash_BSSIOT()) {
//    			if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusTotal();
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusFail();
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError503();
//    			} else {
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
//    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError503();
//    			}
//    		}
//    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
//    		return Response.status(503).entity("").build();
//    	}
//    	
//    	boolean allowIpFlag = false;
//    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
//    		if(allowIp.equals(remoteAddr))
//    		{
//    			allowIpFlag = true;
//    			break;
//    		}
//    	}
//
//    	if(!allowIpFlag) {
//    		if(CommandManager.getInstance().get_bssAllowIpList().contains(remoteAddr)){
//    			allowIpFlag = true;
//    		}
//    	}
//
//    	if(!allowIpFlag) {
//    		synchronized (StatisticsManager.getInstance().getStatisticsHash_BSSIOT()) {
//    			if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusTotal();
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusFail();
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError403();
//    			} else {
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
//    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError403();
//    			}
//    		}
//    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
//    		return Response.status(403).entity("").build();
//    	}
//    	
//    	boolean traceFlag = false;
//    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || 
//    			CommandManager.getInstance().getTraceImsiList().contains(iccid)){
//    		traceFlag = true;
//    	}
//
//    	if(traceFlag) {
//    		StringBuffer sb = new StringBuffer();
//    		sb.append("=============================================");
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append("DIRECTION : BSS-IOT[" + remoteAddr + "] -> PROVS");
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append(apiName + " REQUEST");
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append("REQUEST URL : " + req.getRequestURL().toString());
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append("EID : " + eid);
//    		sb.append("ICCID : " + iccid);
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append("=============================================");
//    		sb.append(System.getProperty("line.separator"));
//
//    		String appName = IoTProperty.getPropPath("sys_name");
//            String command = "TRACE_" + apiName;
//            
//    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
//                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, eid, "242", "0", "", ""), sb.toString());
//    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
//                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, iccid, "242", "0", "", ""), sb.toString());
//    	}
//
//    	synchronized (StatisticsManager.getInstance().getStatisticsHash_BSSIOT()) {
//    		if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
//    			StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusTotal();
//    		} else {
//    			StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
//    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
//    		}
//    	}
//
//    	int clientID = DBMManager.getInstance().getClientReqID();
//
//		ProvifMsgType pmt = new ProvifMsgType();
//		pmt.setUrl(req.getRequestURL().toString());
//		pmt.setIpAddress(req.getRemoteAddr());
//		
//		// Read Header Info 
//		pmt.setOsysCode(osysCode);
//		pmt.setTsysCode(tsysCode);
//		pmt.setMsgId(msgId);
//		pmt.setMsgType(msgType);
//		pmt.setResultCode(resCode);
//		pmt.setResultDtlCode(resultDtlCode);
//		pmt.setResultMsg(resultMsg);
//    	
//
//    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);
//
//		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
//		// 02. Waiting 
//		while(clientID != receiveReqID ){
//			try {
//				Thread.sleep(1);
//				if(timeOutTimeMillis < System.currentTimeMillis()) {
//					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
//					rspCode = 0;
//					break;
//				}
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		} 		  	
//
//    	int resultCode = rspCode;
//
//    	synchronized (StatisticsManager.getInstance().getStatisticsHash_BSSIOT()) {
//    		if(rspCode == 200) {
//    			if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
//    				//				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusSucc();
//    				if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName) == null)
//    					StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
//    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
//    				else
//    					StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusSucc();
//    			} else {
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
//    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
//    			}
//    		} else {
//    			if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusFail();
//
//					if( rspCode == 400)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError400();
//					else if (rspCode == 403)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError403();
//					else if (rspCode == 409)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError409();
//					else if (rspCode == 410)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError410();
//					else if (rspCode == 500)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError500();
//					else if (rspCode == 501)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError501();
//					else if (rspCode == 503)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError503();
//					else
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusErrorEtc();
//
//    			} else {
//    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
//    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));
//
//					if( rspCode == 400)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError400();
//					else if (rspCode == 403)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError403();
//					else if (rspCode == 409)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError409();
//					else if (rspCode == 410)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError410();
//					else if (rspCode == 500)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError500();
//					else if (rspCode == 501)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError501();
//					else if (rspCode == 503)
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError503();
//					else
//						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusErrorEtc();
//    			}
//    		}
//    	}
//
//    	if(CommandManager.getInstance().isLogFlag()) {
//    		logger.info("=============================================");
//    		logger.info(apiName + " REPONSE");
//    		logger.info("STATUS : " + resultCode);
//    		logger.info(this.msg);
//    		logger.info("=============================================");
//    	}
//
//    	if(traceFlag) {
//    		StringBuffer sb = new StringBuffer();
//    		sb.append("=============================================");
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append("DIRECTION : PROVS -> BSS-IOT[" + remoteAddr + "]");
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append(apiName + " REPONSE");
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append("STATUS : " + resultCode);
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append(this.msg);
//    		sb.append(System.getProperty("line.separator"));
//    		sb.append("=============================================");
//    		sb.append(System.getProperty("line.separator"));
//
//    		String appName = IoTProperty.getPropPath("sys_name");
//            String command = "TRACE_" + apiName;
//            
//    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
//                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, eid, "124", "0", "", ""), sb.toString());
//    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
//                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, iccid, "124", "0", "", ""), sb.toString());
//
//    	}
//
//		return Response.status(resultCode).entity(this.msg)
//				.header("Content-Type", "application/json")
//				.header("charset", "UTF-8")
//				.header("O_SYS_CD", this.provMsg.getOsysCode())
//				.header("T_SYS_CD", this.provMsg.getTsysCode())
//				.header("MSG_ID", this.provMsg.getMsgId())
//				.header("MSG_TYPE", this.provMsg.getMsgType())
//				.header("RESULT_CD", this.provMsg.getResultCode())
//				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
//				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();
//
//	}
//	
//	
//	
//	private int receiveReqID = -1;
//	private int rspCode = -1;
//	private String msg = "";
//	private ProvifMsgType provMsg = null;
//	@Override
//	public void setComplete(String msg, int rspCode, int reqId, ProvifMsgType pmt) {
//		this.msg = msg;
//		this.rspCode = rspCode;
//		this.receiveReqID = reqId;
//		this.provMsg = pmt;
//	}
//	
//}
