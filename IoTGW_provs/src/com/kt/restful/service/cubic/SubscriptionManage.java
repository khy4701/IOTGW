package com.kt.restful.service.cubic;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;
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
import com.kt.net.ServiceManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.CommParam;
import com.kt.restful.model.MMCMsgType;
import com.kt.restful.model.ProvifMsgType;
import com.kt.restful.model.StatisticsModel;


// api/v1/order
// igate/mno/api/v1/concar
@Path("/concar")
@Produces("application/json;charset=UTF-8")
public class SubscriptionManage implements DBMListener{

	private static Logger logger = LogManager.getLogger(SubscriptionManage.class);
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("rqtOpn")
	@Produces("application/json;charset=UTF-8")
	
	public Response openSubscription(@Context HttpServletRequest req, @JsonProperty String jsonBody) {
		
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
		
		// [2-1] Common Param Data
		ServiceManager servManager = new ServiceManager();
		CommParam commParam = null;

		commParam = servManager.comm_reqParamParsing(jsonObj);
		if (commParam == null ){
			return Response.status(400).entity("Common Param Parameter Missing ").build();			
		}
		
		String osysCode = commParam.getOsysCode();
		String tsysCode = commParam.getTsysCode();
		String msgId = commParam.getMsgId();
		String msgType = commParam.getMsgType();
				
		// [2-2] 
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
			return Response.status(400).entity("Manadatory Paremter miss : IMSI").build();
		}
		
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
        // [3] Overload Check
        int ret = 0;
        if ( (ret = servManager.check_cubic_overloadTps(remoteAddr, apiName, osysCode)) < 0){
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
        }

        // [4] Allow Ip Check
        if ( (ret = servManager.check_cubic_allowIp(remoteAddr, apiName, osysCode)) < 0){
        	logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
    		return Response.status(403).entity("").build();
    	}
       
        
        // [5] Trace 
        boolean traceFlag = false;        
        ArrayList<String> trcKeyList = new ArrayList<String>();
        trcKeyList.add(imsi);
        trcKeyList.add(eid);
        trcKeyList.add(iccid);
        traceFlag = servManager.checkTrace_Available(trcKeyList);
    	
    	if(traceFlag)    		
    		servManager.handle_rcvTrace(remoteAddr, apiName, req.getRequestURL().toString(), commParam, jsonBody, trcKeyList);
    		
    	
    	// [6] Static Init
    	servManager.init_cubic_statistic(remoteAddr, apiName, osysCode);
    	    	
    	// [7] Sending
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
    	

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

    	// [8] Update Statistic
    	servManager.update_cubic_statistic(remoteAddr, apiName, osysCode, rspCode);
    	
    	if(CommandManager.getInstance().isLogFlag()) {
    		logger.info("=============================================");
    		logger.info(apiName + " REPONSE");
    		logger.info("STATUS : " + resultCode);
    		logger.info(this.msg);
    		logger.info("=============================================");
    	}
    	
    	// [9] Handle Result Trace    	
    	if(traceFlag)    		
    		servManager.handle_sndTrace(remoteAddr, apiName, resultCode, this.provMsg, this.msg, trcKeyList);


		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8").build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("rqtTrmn")
	@Produces("application/json;charset=UTF-8")
	public Response terminateSubscription(@Context HttpServletRequest req, @JsonProperty String jsonBody) {
		
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
		
	    // [2-1] Common Param Data
	    ServiceManager servManager = new ServiceManager();
	    CommParam commParam = null;

	    commParam = servManager.comm_reqParamParsing(jsonObj);
	    if (commParam == null ){
	      return Response.status(400).entity("Common Param Parameter Missing ").build();      
	    }
	    
	    String osysCode = commParam.getOsysCode();
	    String tsysCode = commParam.getTsysCode();
	    String msgId = commParam.getMsgId();
	    String msgType = commParam.getMsgType();

	    // [2-2] 
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			//return Response.status(400).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
        // [3] Overload Check
        int ret = 0;
        if ( (ret = servManager.check_cubic_overloadTps(remoteAddr, apiName, osysCode)) < 0){
        logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
        return Response.status(503).entity("").build();
        }
    	
        // [4] Allow Ip Check
        if ( (ret = servManager.check_cubic_allowIp(remoteAddr, apiName, osysCode)) < 0){
          logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
        return Response.status(403).entity("").build();
      }
    	
        // [5] Trace 
        boolean traceFlag = false;        
        ArrayList<String> trcKeyList = new ArrayList<String>();
        trcKeyList.add(imsi);
        traceFlag = servManager.checkTrace_Available(trcKeyList);
      
        if(traceFlag)       
        	servManager.handle_rcvTrace(remoteAddr, apiName, req.getRequestURL().toString(), commParam, jsonBody, trcKeyList);
        
      
        // [6] Static Init
        servManager.init_cubic_statistic(remoteAddr, apiName, osysCode);
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		//pmt.setResultCode(resCode);
		//pmt.setResultDtlCode(resultDtlCode);
		//pmt.setResultMsg(resultMsg);

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

        // [8] Update Statistic
        servManager.update_cubic_statistic(remoteAddr, apiName, osysCode, rspCode);
        
        if(CommandManager.getInstance().isLogFlag()) {
          logger.info("=============================================");
          logger.info(apiName + " REPONSE");
          logger.info("STATUS : " + resultCode);
          logger.info(this.msg);
          logger.info("=============================================");
        }
        
        // [9] Handle Result Trace      
        if(traceFlag)       
          servManager.handle_sndTrace(remoteAddr, apiName, resultCode, this.provMsg, this.msg, trcKeyList);

        return Response.status(resultCode).entity(this.msg)
                .header("Content-Type", "application/json")
                .header("charset", "UTF-8").build();
	}

	
	@SuppressWarnings("static-access")
	@POST 
	@Path("chgContSttus")
	@Produces("application/json;charset=UTF-8")
	public Response changeSubscriptionSts(@Context HttpServletRequest req, @JsonProperty String jsonBody) {
		
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
		
	    // [2-1] Common Param Data
	    ServiceManager servManager = new ServiceManager();
	    CommParam commParam = null;

	    commParam = servManager.comm_reqParamParsing(jsonObj);
	    if (commParam == null ){
	      return Response.status(400).entity("Common Param Parameter Missing ").build();      
	    }
	    
	    String osysCode = commParam.getOsysCode();
	    String tsysCode = commParam.getTsysCode();
	    String msgId = commParam.getMsgId();
	    String msgType = commParam.getMsgType();
	
		
	    // [2-2] 
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			//return Response.status(400).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
        // [3] Overload Check
        int ret = 0;
        if ( (ret = servManager.check_cubic_overloadTps(remoteAddr, apiName, osysCode)) < 0){
        logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
        return Response.status(503).entity("").build();
        }

        // [4] Allow Ip Check
        if ( (ret = servManager.check_cubic_allowIp(remoteAddr, apiName, osysCode)) < 0){
          logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
        return Response.status(403).entity("").build();
      }
       
        
        // [5] Trace 
        boolean traceFlag = false;        
        ArrayList<String> trcKeyList = new ArrayList<String>();
        trcKeyList.add(imsi);
        traceFlag = servManager.checkTrace_Available(trcKeyList);
      
      if(traceFlag)       
        servManager.handle_rcvTrace(remoteAddr, apiName, req.getRequestURL().toString(), commParam, jsonBody, trcKeyList);
        
      
      // [6] Static Init
      servManager.init_cubic_statistic(remoteAddr, apiName, osysCode);
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);

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

        // [8] Update Statistic
        servManager.update_cubic_statistic(remoteAddr, apiName, osysCode, rspCode);
        
        if(CommandManager.getInstance().isLogFlag()) {
          logger.info("=============================================");
          logger.info(apiName + " REPONSE");
          logger.info("STATUS : " + resultCode);
          logger.info(this.msg);
          logger.info("=============================================");
        }
        
        // [9] Handle Result Trace      
        if(traceFlag)       
          servManager.handle_sndTrace(remoteAddr, apiName, resultCode, this.provMsg, this.msg, trcKeyList);


      return Response.status(resultCode).entity(this.msg)
          .header("Content-Type", "application/json")
          .header("charset", "UTF-8").build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("chgIntm")
	@Produces("application/json;charset=UTF-8")
	public Response changeSubscriptionDevice(@Context HttpServletRequest req,  @JsonProperty String jsonBody) {
		
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
		
	    // [2-1] Common Param Data
	    ServiceManager servManager = new ServiceManager();
	    CommParam commParam = null;

	    commParam = servManager.comm_reqParamParsing(jsonObj);
	    if (commParam == null ){
	      return Response.status(400).entity("Common Param Parameter Missing ").build();      
	    }
	    
	    String osysCode = commParam.getOsysCode();
	    String tsysCode = commParam.getTsysCode();
	    String msgId = commParam.getMsgId();
	    String msgType = commParam.getMsgType();

		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			//return Response.status(400).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
        // [3] Overload Check
        int ret = 0;
        if ( (ret = servManager.check_cubic_overloadTps(remoteAddr, apiName, osysCode)) < 0){
        logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
        return Response.status(503).entity("").build();
        }

        // [4] Allow Ip Check
        if ( (ret = servManager.check_cubic_allowIp(remoteAddr, apiName, osysCode)) < 0){
          logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
        return Response.status(403).entity("").build();
      }
       
        
        // [5] Trace 
        boolean traceFlag = false;        
        ArrayList<String> trcKeyList = new ArrayList<String>();
        trcKeyList.add(imsi);
        traceFlag = servManager.checkTrace_Available(trcKeyList);
      
      if(traceFlag)       
        servManager.handle_rcvTrace(remoteAddr, apiName, req.getRequestURL().toString(), commParam, jsonBody, trcKeyList);
        
      
      // [6] Static Init
      servManager.init_cubic_statistic(remoteAddr, apiName, osysCode);
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		//pmt.setResultCode(resCode);
		//pmt.setResultDtlCode(resultDtlCode);
		//pmt.setResultMsg(resultMsg);

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

        // [8] Update Statistic
        servManager.update_cubic_statistic(remoteAddr, apiName, osysCode, rspCode);
        
        if(CommandManager.getInstance().isLogFlag()) {
          logger.info("=============================================");
          logger.info(apiName + " REPONSE");
          logger.info("STATUS : " + resultCode);
          logger.info(this.msg);
          logger.info("=============================================");
        }
        
        // [9] Handle Result Trace      
        if(traceFlag)       
          servManager.handle_sndTrace(remoteAddr, apiName, resultCode, this.provMsg, this.msg, trcKeyList);


      return Response.status(resultCode).entity(this.msg)
          .header("Content-Type", "application/json")
          .header("charset", "UTF-8").build();

	}
	
	@SuppressWarnings("static-access")
	@POST 
	@Path("chgSvcNo")
	@Produces("application/json;charset=UTF-8")
	public Response changeServiceNumber(@Context HttpServletRequest req,  @JsonProperty String jsonBody) {
		
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
		
	    // [2-1] Common Param Data
	    ServiceManager servManager = new ServiceManager();
	    CommParam commParam = null;

	    commParam = servManager.comm_reqParamParsing(jsonObj);
	    if (commParam == null ){
	      return Response.status(400).entity("Common Param Parameter Missing ").build();      
	    }
	    
	    String osysCode = commParam.getOsysCode();
	    String tsysCode = commParam.getTsysCode();
	    String msgId = commParam.getMsgId();
	    String msgType = commParam.getMsgType();

		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(400).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
        // [3] Overload Check
        int ret = 0;
        if ( (ret = servManager.check_cubic_overloadTps(remoteAddr, apiName, osysCode)) < 0){
        logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
        return Response.status(503).entity("").build();
        }

        // [4] Allow Ip Check
        if ( (ret = servManager.check_cubic_allowIp(remoteAddr, apiName, osysCode)) < 0){
          logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
        return Response.status(403).entity("").build();
      }
       
        
        // [5] Trace 
        boolean traceFlag = false;        
        ArrayList<String> trcKeyList = new ArrayList<String>();
        trcKeyList.add(imsi);
        traceFlag = servManager.checkTrace_Available(trcKeyList);
      
      if(traceFlag)       
        servManager.handle_rcvTrace(remoteAddr, apiName, req.getRequestURL().toString(), commParam, jsonBody, trcKeyList);
        
      
      // [6] Static Init
      servManager.init_cubic_statistic(remoteAddr, apiName, osysCode);
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		//pmt.setResultCode(resCode);
		//pmt.setResultDtlCode(resultDtlCode);
		//pmt.setResultMsg(resultMsg);

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

        // [8] Update Statistic
        servManager.update_cubic_statistic(remoteAddr, apiName, osysCode, rspCode);
        
        if(CommandManager.getInstance().isLogFlag()) {
          logger.info("=============================================");
          logger.info(apiName + " REPONSE");
          logger.info("STATUS : " + resultCode);
          logger.info(this.msg);
          logger.info("=============================================");
        }
        
        // [9] Handle Result Trace      
        if(traceFlag)       
          servManager.handle_sndTrace(remoteAddr, apiName, resultCode, this.provMsg, this.msg, trcKeyList);


      return Response.status(resultCode).entity(this.msg)
          .header("Content-Type", "application/json")
          .header("charset", "UTF-8").build();

	}

	@SuppressWarnings("static-access")
	@POST 
	@Path("sndOTA")
	@Produces("application/json;charset=UTF-8")
	public Response otaRequest(@Context HttpServletRequest req, @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.OTA_REQ.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
	    // [2-1] Common Param Data
	    ServiceManager servManager = new ServiceManager();
	    CommParam commParam = null;

	    commParam = servManager.comm_reqParamParsing(jsonObj);
	    if (commParam == null ){
	      return Response.status(400).entity("Common Param Parameter Missing ").build();      
	    }
	    
	    String osysCode = commParam.getOsysCode();
	    String tsysCode = commParam.getTsysCode();
	    String msgId = commParam.getMsgId();
	    String msgType = commParam.getMsgType();

		
		String imsi = null;
						
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
        // [3] Overload Check
        int ret = 0;
        if ( (ret = servManager.check_cubic_overloadTps(remoteAddr, apiName, osysCode)) < 0){
        logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
        return Response.status(503).entity("").build();
        }

        // [4] Allow Ip Check
        if ( (ret = servManager.check_cubic_allowIp(remoteAddr, apiName, osysCode)) < 0){
          logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
        return Response.status(403).entity("").build();
      }
       
        
        // [5] Trace 
        boolean traceFlag = false;        
        ArrayList<String> trcKeyList = new ArrayList<String>();
        trcKeyList.add(imsi);
        traceFlag = servManager.checkTrace_Available(trcKeyList);
      
      if(traceFlag)       
        servManager.handle_rcvTrace(remoteAddr, apiName, req.getRequestURL().toString(), commParam, jsonBody, trcKeyList);
        
      
      // [6] Static Init
      servManager.init_cubic_statistic(remoteAddr, apiName, osysCode);
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		//pmt.setResultCode(resCode);
		//pmt.setResultDtlCode(resultDtlCode);
		//pmt.setResultMsg(resultMsg);

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

        // [8] Update Statistic
        servManager.update_cubic_statistic(remoteAddr, apiName, osysCode, rspCode);
        
        if(CommandManager.getInstance().isLogFlag()) {
          logger.info("=============================================");
          logger.info(apiName + " REPONSE");
          logger.info("STATUS : " + resultCode);
          logger.info(this.msg);
          logger.info("=============================================");
        }
        
        // [9] Handle Result Trace      
        if(traceFlag)       
          servManager.handle_sndTrace(remoteAddr, apiName, resultCode, this.provMsg, this.msg, trcKeyList);


      return Response.status(resultCode).entity(this.msg)
          .header("Content-Type", "application/json")
          .header("charset", "UTF-8").build();
	}
	

	@SuppressWarnings("static-access")
	@POST 
	@Path("retvContSttus")
	@Produces("application/json;charset=UTF-8")
	public Response retrieveSubsStatus(@Context HttpServletRequest req, @JsonProperty String jsonBody) {
		
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
		
	    // [2-1] Common Param Data
	    ServiceManager servManager = new ServiceManager();
	    CommParam commParam = null;

	    commParam = servManager.comm_reqParamParsing(jsonObj);
	    if (commParam == null ){
	      return Response.status(400).entity("Common Param Parameter Missing ").build();      
	    }
	    
	    String osysCode = commParam.getOsysCode();
	    String tsysCode = commParam.getTsysCode();
	    String msgId = commParam.getMsgId();
	    String msgType = commParam.getMsgType();

		
		String imsi = null;
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			//return Response.status(400).entity("Manadatory Paremter miss : IMSI").build();
		}
				
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
        // [3] Overload Check
        int ret = 0;
        if ( (ret = servManager.check_cubic_overloadTps(remoteAddr, apiName, osysCode)) < 0){
        logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps_CUBIC() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
        return Response.status(503).entity("").build();
        }

        // [4] Allow Ip Check
        if ( (ret = servManager.check_cubic_allowIp(remoteAddr, apiName, osysCode)) < 0){
          logger.info("Request Remote IP("+remoteAddr +") Not Allow IP");
        return Response.status(403).entity("").build();
      }
       
        
        // [5] Trace 
        boolean traceFlag = false;        
        ArrayList<String> trcKeyList = new ArrayList<String>();
        trcKeyList.add(imsi);
        traceFlag = servManager.checkTrace_Available(trcKeyList);
      
      if(traceFlag)       
        servManager.handle_rcvTrace(remoteAddr, apiName, req.getRequestURL().toString(), commParam, jsonBody, trcKeyList);
        
      
      // [6] Static Init
      servManager.init_cubic_statistic(remoteAddr, apiName, osysCode);
    	
    	int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress(req.getRemoteAddr());
		
		// Read Header Info 
		pmt.setOsysCode(osysCode);
		pmt.setTsysCode(tsysCode);
		pmt.setMsgId(msgId);
		pmt.setMsgType(msgType);
		//pmt.setResultCode(resCode);
		//pmt.setResultDtlCode(resultDtlCode);
		//pmt.setResultMsg(resultMsg);

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

        // [8] Update Statistic
        servManager.update_cubic_statistic(remoteAddr, apiName, osysCode, rspCode);
        
        if(CommandManager.getInstance().isLogFlag()) {
          logger.info("=============================================");
          logger.info(apiName + " REPONSE");
          logger.info("STATUS : " + resultCode);
          logger.info(this.msg);
          logger.info("=============================================");
        }
        
        // [9] Handle Result Trace      
        if(traceFlag)       
          servManager.handle_sndTrace(remoteAddr, apiName, resultCode, this.provMsg, this.msg, trcKeyList);


      return Response.status(resultCode).entity(this.msg)
          .header("Content-Type", "application/json")
          .header("charset", "UTF-8").build();
	}


	@SuppressWarnings("static-access")
	@POST 	
	@Path("/subStatusChgNoti")
	@Produces("application/json;charset=UTF-8")
	public Response subsStatusChangeNoti(@Context HttpServletRequest req,  @JsonProperty String jsonBody) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.SUBS_STS_CHG_NOTI.getName();
		
		// 01. Read Json Parameter		
		JSONObject jsonObj = null;
		try{
			jsonObj = new JSONObject(jsonBody);
		}				
		catch(Exception e){
			logger.error("Json Parsing Error  : " + jsonBody);	
			return Response.status(400).entity("Request Error").build();
		}
		
	    // [2-1] Common Param Data
	    ServiceManager servManager = new ServiceManager();
	    CommParam commParam = null;

	    commParam = servManager.comm_reqParamParsing(jsonObj);
	    if (commParam == null ){
	      return Response.status(400).entity("Common Param Parameter Missing ").build();      
	    }
	    
	    String osysCode = commParam.getOsysCode();
	    String tsysCode = commParam.getTsysCode();
	    String msgId = commParam.getMsgId();
	    String msgType = commParam.getMsgType();
		
		
		String imsi = "";
		
		try{
			imsi = jsonObj.get("IMSI").toString();
		}catch(Exception e){
			imsi = null;
			return Response.status(400).entity("Manadatory Paremter miss : IMSI").build();
		}
								
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info("BSS-IOT -> PROVS [" + apiName + "]");
            logger.info("=============================================");
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("HEADER -------------------------------------");
            logger.info("BODY : " + jsonBody);
            logger.info("=============================================");
        }
        
    	if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_BSSIOT()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError503();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError503();
    			}
    		}
    		logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
    		return Response.status(503).entity("").build();
    	}
    	
    	boolean allowIpFlag = false;
    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_bssAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

    	if(!allowIpFlag) {
    		synchronized (StatisticsManager.getInstance().getStatisticsHash_BSSIOT()) {
    			if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusTotal();
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusFail();
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError403();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 1, 0, 1));
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError403();
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
    		sb.append("=============================================");
    		sb.append(System.getProperty("line.separator"));
    		sb.append("DIRECTION : BSS-IOT[" + remoteAddr + "] -> PROVS");
    		sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("BODY : " + jsonBody);
			sb.append(System.getProperty("line.separator"));
    		sb.append("=============================================");
    		sb.append(System.getProperty("line.separator"));

    		String appName = IoTProperty.getPropPath("sys_name");
            String command = "TRACE_" + apiName;
            
    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "242", "0", "", ""), sb.toString());
    	}

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_BSSIOT()) {
    		if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
    			StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusTotal();
    		} else {
    			StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
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

    	synchronized (StatisticsManager.getInstance().getStatisticsHash_BSSIOT()) {
    		if(rspCode == 200) {
    			if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
    				//				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusSucc();
    				if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName) == null)
    					StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
    							new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    				else
    					StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusSucc();
    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 1, 0));
    			}
    		} else {
    			if(StatisticsManager.getInstance().getStatisticsHash_BSSIOT().containsKey(remoteAddr+apiName)) {
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusErrorEtc();

    			} else {
    				StatisticsManager.getInstance().getStatisticsHash_BSSIOT().put(remoteAddr+apiName, 
    						new StatisticsModel(osysCode, apiName, remoteAddr, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash_BSSIOT().get(remoteAddr+apiName).plusErrorEtc();
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
    		sb.append("=============================================");
    		sb.append(System.getProperty("line.separator"));
    		sb.append("DIRECTION : PROVS -> BSS-IOT[" + remoteAddr + "]");
    		sb.append(System.getProperty("line.separator"));
    		sb.append(apiName + " REPONSE");
    		sb.append(System.getProperty("line.separator"));
    		sb.append("STATUS : " + resultCode);
    		sb.append(System.getProperty("line.separator"));
    		sb.append("BODY : " + this.msg);
    		sb.append(System.getProperty("line.separator"));
    		sb.append("=============================================");
    		sb.append(System.getProperty("line.separator"));

    		String appName = IoTProperty.getPropPath("sys_name");
            String command = "TRACE_" + apiName;
            
    		if (CommandManager.getInstance().getTraceImsiList().contains(imsi))
                CommandManager.getInstance().sendMessage(new MMCMsgType(appName, command, imsi, "124", "0", "", ""), sb.toString());
    	}

		return Response.status(resultCode).entity(this.msg)
				.header("Content-Type", "application/json")
				.header("charset", "UTF-8").build();

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
