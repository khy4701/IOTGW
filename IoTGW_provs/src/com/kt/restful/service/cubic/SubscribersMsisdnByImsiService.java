package com.kt.restful.service;

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
import com.kt.net.DBMListener;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.StatisticsModel;

@Path("/subscriberImsiByMsisdn")
public class SubscribersMsisdnByImsiService implements DBMListener  {

	private static Logger logger = LogManager.getLogger(SubscribersMsisdnByImsiService.class);

	@SuppressWarnings("static-access")
	@GET
	@Path("{msisdn}")
	@Produces("text/plain;charset=UTF-8")
	public Response getSubsImsiByMdn(@Context HttpServletRequest req, @PathParam("msisdn") String msisdn) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("MSISDN : " + msisdn );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}
		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(req.getRemoteAddr()))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().getAllowIpList().contains(req.getRemoteAddr())){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError403();
				}
			}
			logger.info("Request Remote IP("+req.getRemoteAddr() +") Not Allow IP");
			return Response.status(403).entity("").build();
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), 
						new StatisticsModel(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(msisdn != null) {
			String[] param1 = {"msisdn", msisdn};
			params.add(param1);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), params, this, clientID, "", msisdn, req.getRemoteAddr());

		while(clientID != receiveReqID ){
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		int resultCode = rspCode;

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(rspCode == 200) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), 
								new StatisticsModel(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_IMSI_BY_MDN.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_SUBS_IMSI_BY_MDN.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	private int receiveReqID = -1;
	private int rspCode = -1;
	private String msg = "";
	@Override
	public void setComplete(String msg, int rspCode, int reqId) {
		this.msg = msg;
		this.rspCode = rspCode;
		this.receiveReqID = reqId;
	}
}
