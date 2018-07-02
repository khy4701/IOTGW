package com.kt.restful.service.cubic;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Encoded;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.codehaus.jackson.annotate.JsonProperty;
import org.json.JSONObject;

import com.kt.net.CommandManager;
import com.kt.net.DBMConnector;
import com.kt.net.DBMListener;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.net.TraceManager;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.MMCMsgType;
import com.kt.restful.model.ProvifMsgType;
import com.kt.restful.model.StatisticsModel;

@Path("/order")
@Produces("application/json;charset=UTF-8")
public class SubscriptionManage implements DBMListener{

	private static Logger logger = LogManager.getLogger(SubscriptionManage.class);
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("rqtOpn")
	@Produces("application/json;charset=UTF-8")

	public Response openSubscription(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.OPEN_SUBS.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(503).entity("Manadatory Paremter miss : IMSI").build();
		}
						
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
    		traceFlag = true;
    	}
    	
    	logger.info("Trace Flag : " + traceFlag + ", imsi :" + imsi);

    	if(traceFlag) {    		
    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
    	}
    	
    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}

    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);
    	

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();
    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("rqtTrmn")
	@Produces("application/json;charset=UTF-8")
	public Response terminateSubscription(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.TERMINATE_SUBS.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(503).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
//    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
//    		if(allowIp.equals(remoteAddr))
//    		{
//    			allowIpFlag = true;
//    			break;
//    		}
//    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();
    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}

	
	@SuppressWarnings("static-access")
	@POST 
	@Path("chgContSttus")
	@Produces("application/json;charset=UTF-8")
	public Response changeSubscriptionSts(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.CHG_SUBS_STS.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(503).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();
    				

    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("chgIntm")
	@Produces("application/json;charset=UTF-8")
	public Response changeSubscriptionDevice(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.CHG_SUBS_DEV.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(503).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("chgSvcNo")
	@Produces("application/json;charset=UTF-8")
	public Response changeServiceNumber(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.CHG_SERV_NUMBER.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(503).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}

	@SuppressWarnings("static-access")
	@POST 
	@Path("sndOTA")
	@Produces("application/json;charset=UTF-8")
	public Response otaRequest(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.OTA_REQ.getName();
		
		// 01. Read Json Parameter		
//		JSONObject jsonObj = null;
//		try{
//			jsonObj = new JSONObject(jsonBody);
//		}				
//		catch(Exception e){
//			logger.error("Json Parsing Error  : " + jsonBody);	
//			return Response.status(400).entity("Request Error").build();
//		}
		
		String imsi = null;
						
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr,pmt );

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();    					
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();    					
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("retvContSttus")
	@Produces("application/json;charset=UTF-8")
	public Response retrieveSubsStatus(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.RETREIVE_SUBS_STS.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(503).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();    				
    				else if (rspCode == 403)
        					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();    
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}

	

	// SIM Management 
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("downProf")
	@Produces("application/json;charset=UTF-8")
	public Response downloadProfReq(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.DOWNLOAD_PROF_REQ.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("es2EnblProf")
	@Produces("application/json;charset=UTF-8")
	public Response enableProfReqES2(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.ENABLE_ES2_PROF_REQ.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();
	}

		
	@SuppressWarnings("static-access")
	@POST 
	@Path("es2DsblProf")
	@Produces("application/json;charset=UTF-8")
	public Response disableProfreqES2(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.DISABLE_ES2_PROF_REQ.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("es2DelProf")
	@Produces("application/json;charset=UTF-8")
	public Response deleteProfreqES2(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.DELETE_ES2_PROF_REQ.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();
	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("es2UpdSubsc")
	@Produces("application/json;charset=UTF-8")
	public Response updateSubsAddressES2(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.UPT_ES2_SUBS_ADDRESS.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr,pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("es2getEis")
	@Produces("application/json;charset=UTF-8")
	public Response getEIS_ES2(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.GET_ES2_EIS.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
						
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    	}
		
		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}

	@SuppressWarnings("static-access")
	@POST 
	@Path("es4EnblProf")
	@Produces("application/json;charset=UTF-8")
	public Response enableProfreqES4(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.ENABLE_ES4_PROF_REQ.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}
    	
    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

       	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}
       	
		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("es4DsblProf")
	@Produces("application/json;charset=UTF-8")
	public Response disableProfreqES4(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.DISABLE_ES4_PROF_REQ.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("es4DelProf")
	@Produces("application/json;charset=UTF-8")
	public Response deleteProfreqES4(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.DELETE_ES4_PROF_REQ.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
       	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}


	@SuppressWarnings("static-access")
	@POST 
	@Path("es4UpdSubsc")
	@Produces("application/json;charset=UTF-8")
	public Response updateProfreqES4(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.UPT_ES4_SUBS_ADDRESS.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
       	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}
       	
    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();
 
	}
	
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("es4getEis")
	@Produces("application/json;charset=UTF-8")
	public Response getEisES4(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.GET_ES4_EIS.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String eid = "";
		String iccid = "";
		
		try{
			eid = jsonObj.get("EID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : EID").build();
		}
		
		try{
			iccid = jsonObj.get("ICCID").toString();
		}catch(Exception e){
			eid = null;
			return Response.status(503).entity("Manadatory Paremter miss : ICCID").build();
		}

				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(eid) || CommandManager.getInstance().getTraceImsiList().contains(iccid)){
    		traceFlag = true;
    	}
    	
       	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "242", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "242", "0", "", ""), sb.toString());
    	}
       	
    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);

    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

    	if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

    		if (CommandManager.getInstance().getTraceImsiList().contains(eid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, eid, "124", "0", "", ""), sb.toString());
    		else if (CommandManager.getInstance().getTraceImsiList().contains(iccid))
                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, iccid, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();

	}
	
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("inquiry")
	@Produces("application/json;charset=UTF-8")

	public Response inQuery(@Context HttpServletRequest req, @HeaderParam("O_SYS_CD") @Encoded String osysCode, @HeaderParam("T_SYS_CD") @Encoded String tsysCode,
			@HeaderParam("MSG_ID") @Encoded String msgId, @HeaderParam("MSG_TYPE") @Encoded String msgType, @HeaderParam("RESULT_CD") @Encoded String resCode,
			@HeaderParam("RESULT_DTL_CD") @Encoded String resultDtlCode, @HeaderParam("RESULT_MSG") @Encoded String resultMsg, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.INQUIRY.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(503).entity("Manadatory Paremter miss : IMSI").build();
		}
						
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("O_SYS_CD : " + osysCode);
            logger.info("T_SYS_CD : " + tsysCode);
            logger.info("MSG_ID   : " + msgId);
            logger.info("MSG_TYPE : " + msgType);
            logger.info("RESULT_CD : " + resCode);
            logger.info("RESULT_DTL_CD : " + resultDtlCode);
            logger.info("RESULT_MSG : " + resultMsg);
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps_CUBIC() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
    		if(allowIp.equals(remoteAddr))
    		{
    			allowIpFlag = true;
    			break;
    		}
    	}

    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    			}
    		}
    		logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
    	
    	boolean traceFlag = false;
    	if(CommandManager.getInstance().getTraceImsiList().contains(imsi)){
    		traceFlag = true;
    	}

    	if(traceFlag) {    		
    		StringBuffer sb = new StringBuffer();    		
    		TraceManager.getInstance().printCubicTrace_req(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
    				osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
    	}
    	
    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    					new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 0));
    		}
    	}

    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		pmt.setResultCode(resCode);
		pmt.setResultDtlCode(resultDtlCode);
		pmt.setResultMsg(resultMsg);
    	
    	DBMManager.getInstance().sendCommand(apiName, jsonBody, this, clientID, remoteAddr, pmt);

		long timeOutTimeMillis = System.currentTimeMillis() + DBMConnector.getInstance().getTimeOut()*1000;
		// 02. Waiting 
		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
				if(timeOutTimeMillis < System.currentTimeMillis()) {
					logger.error("Response Timeout("+timeOutTimeMillis +":" + System.currentTimeMillis() + ")");
					rspCode = 0;
					break;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} 		  	

    	int resultCode = rspCode;

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_CUBIC()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_CUBIC().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusFail();

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();
    				
    				


    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_CUBIC().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

    				if( rspCode == 400)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError400();
    				else if (rspCode == 403)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError403();
    				else if (rspCode == 409)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError409();
    				else if (rspCode == 410)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError410();
    				else if (rspCode == 500)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError500();
    				else if (rspCode == 501)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError501();
    				else if (rspCode == 503)
    					StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_CUBIC().get(remoteAddr+apiName).plusErrorEtc();

    			}
    		}
    	}

    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}

		if(traceFlag) {
    		StringBuffer sb = new StringBuffer();
    		TraceManager.getInstance().printCubicTrace_res(sb, remoteAddr, apiName, resultCode , this.provMsg, this.msg);

            CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());

    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8")
				.header("O_SYS_CD", this.provMsg.getOsysCode())
				.header("T_SYS_CD", this.provMsg.getTsysCode())
				.header("MSG_ID", this.provMsg.getMsgId())
				.header("MSG_TYPE", this.provMsg.getMsgType())
				.header("RESULT_CD", this.provMsg.getResultCode())
				.header("RESULT_DTL_CD", this.provMsg.getResultDtlCode())
				.header("RESULT_MSG", this.provMsg.getResultMsg()).build();
 
	}
	
	private int receiveReqID = -1;
	private int rspCode = -1;
	private String msg = "";
	private ProvifMsgType provMsg = null;
	@Override
	public void setComplete(String msg, int rspCode, int reqId, ProvifMsgType pmt) {
		this.msg = msg;
		this.rspCode = rspCode;
		this.receiveReqID = reqId;
		this.provMsg = pmt;
	}
	
}
