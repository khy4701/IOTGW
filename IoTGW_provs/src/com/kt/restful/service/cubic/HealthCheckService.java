package com.kt.restful.service.cubic;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.kt.net.CommandManager;
import com.kt.net.DBMListener;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.ProvifMsgType;

//igate/mno/api/v1/echo

@Path("/echo")
public class HealthCheckService implements DBMListener{

	private static Logger logger = LogManager.getLogger(HealthCheckService.class);
	
	@SuppressWarnings("static-access")
	@GET	
	@Produces("application/json;charset=UTF-8")
	public Response openSubscription(@Context HttpServletRequest req) {
		
		String remoteAddr = req.getRemoteAddr();
		String apiName = ApiDefine.HEALTH_CHECK.getName();
		
        if(CommandManager.getInstance().isLogFlag()) {
            logger.info("=============================================");
            logger.info(apiName);
            logger.info("REQUEST URL : " + req.getRequestURL().toString());
            logger.info("=============================================");
        }
            	
    	boolean allowIpFlag = false;
    	if(!allowIpFlag) {
    		if(CommandManager.getInstance().get_bssAllowIpList().contains(remoteAddr) || CommandManager.getInstance().get_cubicAllowIpList().contains(remoteAddr)){
    			allowIpFlag = true;
    		}
    	}

//    	int clientID = DBMManager.getInstance().getClientReqID();
//
//		ProvifMsgType pmt = new ProvifMsgType();
//		pmt.setUrl(req.getRequestURL().toString());
//		pmt.setIpAddress(req.getRemoteAddr());
//		
//    	DBMManager.getInstance().sendCommand(apiName, "", this, clientID, remoteAddr, pmt);

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
    	
		int resultCode = 200;
		JSONObject responseJSONObject = new JSONObject();

		int indentLevel = 4;

		responseJSONObject.put("RESULT_CD", "0000");
		responseJSONObject.put("RESULT_MSG", "Success");

		if (CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		return Response.status(resultCode).entity(responseJSONObject.toString(indentLevel)).build();
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
