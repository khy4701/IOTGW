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

import com.kt.net.CommandConnector;
import com.kt.net.CommandManager;
import com.kt.net.DBMListener;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.StatisticsModel;


@Path("/subscriberMsisdnByImsi")
public class SubscribersImsiByMsisdnService implements DBMListener  {

	private static Logger logger = LogManager.getLogger(SubscribersImsiByMsisdnService.class);

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}")
	@Produces("text/plain;charset=UTF-8")
	public Response getSubsMdnByImsi(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError403();
				}
			}
			logger.info("Request Remote IP("+req.getRemoteAddr() +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + req.getRemoteAddr() + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi );
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), 
						new StatisticsModel(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param1 = {"imsi", imsi};
			params.add(param1);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), 
								new StatisticsModel(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_MDN_BY_IMSI.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName() + " REPONSE");
			logger.info("Stauts : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_SUBS_MDN_BY_IMSI.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("Stauts : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_SUBS_MDN_BY_IMSI.getName(), imsi, "124", sb.toString(), 0);
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
