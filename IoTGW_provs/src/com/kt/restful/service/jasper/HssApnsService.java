package com.kt.restful.service.jasper;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.net.CommandManager;
import com.kt.net.DBMConnector;
import com.kt.net.DBMListener;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.ProvifMsgType;
import com.kt.restful.model.StatisticsModel;

@Path("/hssapns")
@Produces("application/json;charset=UTF-8")
public class HssApnsService implements DBMListener {

	private static Logger logger = LogManager.getLogger(ApnsService.class);

	@SuppressWarnings("static-access")
	@GET
	@Path("{pdnId}")
	@Produces("application/json;charset=UTF-8")
	public Response getApnByPdnId(@Context HttpServletRequest req, @PathParam("pdnId") String pdnId) {

		String ipAddress = req.getRemoteAddr();
		String apiName = ApiDefine.GET_APN_BY_PDN_ID.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("pdnId : " + pdnId );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
							new StatisticsModel("CC", apiName, ipAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(ipAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
							new StatisticsModel("CC", apiName, ipAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+ipAddress +") Not Allow IP");
			return Response.status(403).entity("").build();
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
						new StatisticsModel("CC", apiName, ipAddress, 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(pdnId != null) {
			String[] param1 = {"pdnId", pdnId};
			params.add(param1);
		}

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setIpAddress( ipAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID,  pmt);

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
		
		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(rspCode == 200) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
								new StatisticsModel("CC", apiName, ipAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
							new StatisticsModel("CC", apiName, ipAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
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

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
							new StatisticsModel("CC", apiName, ipAddress, 0, 0, 1));

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

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName + " REPONSE");
			logger.info("Stauts : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		return Response.status(resultCode).entity(this.msg).build();
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
