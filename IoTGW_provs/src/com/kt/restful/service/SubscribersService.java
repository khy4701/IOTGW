package com.kt.restful.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
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

@Path("/subscribers")
public class SubscribersService implements DBMListener  {

	private static Logger logger = LogManager.getLogger(SubscribersService.class);

	@SuppressWarnings("static-access")
	@POST
	@Produces("application/json;charset=UTF-8")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response addSubs(@Context HttpServletRequest req,
			@FormParam("imsi") String imsi,
			@FormParam("hlrTemplate") String hlrTemplate,
			@FormParam("msisdn") String msisdn,
			@FormParam("eKi") String eKi,
			@FormParam("keyId") String keyId,
			@FormParam("subsType") String subsType,
			@FormParam("algoVersion") String algoVersion,
			@FormParam("opValue") String opValue,
			@FormParam("profileIndex") String profileIndex) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.ADD_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("HLRTEMPLATE : " + hlrTemplate);
			logger.info("MSISDN : " + msisdn);
			logger.info("EKI : " + eKi);
			logger.info("KEYID : " + keyId);
			logger.info("SUBSTYPE : " + subsType);
			logger.info("ALGOVERSION : " + algoVersion);
			logger.info("OPVALUE : " + opValue);
			logger.info("PROFILEINDEX : " + profileIndex);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName(), 
							new StatisticsModel(ApiDefine.ADD_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName(), 
							new StatisticsModel(ApiDefine.ADD_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.ADD_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("HLRTEMPLATE : " + hlrTemplate);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("EKI : " + eKi);
			sb.append(System.getProperty("line.separator"));
			sb.append("KEYID : " + keyId);
			sb.append(System.getProperty("line.separator"));
			sb.append("SUBSTYPE : " + subsType);
			sb.append(System.getProperty("line.separator"));
			sb.append("ALGOVERSION : " + algoVersion);
			sb.append(System.getProperty("line.separator"));
			sb.append("OPVALUE : " + opValue);
			sb.append(System.getProperty("line.separator"));
			sb.append("PROFILEINDEX : " + profileIndex);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.ADD_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName(), 
						new StatisticsModel(ApiDefine.ADD_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(hlrTemplate != null) {
			String[] param = {"hlrTemplate", hlrTemplate};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}
		if(eKi != null) {
			String[] param = {"eKi", eKi};
			params.add(param);
		}
		if(imsi != null) {
			String[] param = {"keyId", keyId};
			params.add(param);
		}
		if(subsType != null) {
			String[] param = {"subsType", subsType};
			params.add(param);
		}
		if(algoVersion != null) {
			String[] param = {"algoVersion", algoVersion};
			params.add(param);
		}
		if(opValue != null) {
			String[] param = {"opValue", opValue};
			params.add(param);
		}
		if(profileIndex != null) {
			String[] param = {"profileIndex", profileIndex};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.ADD_SUBS.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName(), 
								new StatisticsModel(ApiDefine.ADD_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName(), 
							new StatisticsModel(ApiDefine.ADD_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName(), 
							new StatisticsModel(ApiDefine.ADD_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.ADD_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.ADD_SUBS.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.ADD_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}


	@SuppressWarnings("static-access")
	@PUT
	@Path("{imsi}staticIpHss/{ipAddress}_{pdnId}")
	@Produces("application/json;charset=UTF-8")
	public Response modHssStaticIp(@Context HttpServletRequest req,
			@PathParam("imsi") String imsi,
			@PathParam("ipAddress") String ipAddress,
			@PathParam("pdnId") String pdnId) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.MOD_HSS_STATIC_IP.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi );
			logger.info("IPADDRESS : " + ipAddress );
			logger.info("PDNID : " + pdnId );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.MOD_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.MOD_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError403();
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
			sb.append(ApiDefine.MOD_HSS_STATIC_IP.getName() + " REQUEST");
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi );
			sb.append(System.getProperty("line.separator"));
			sb.append("IPADDRESS : " + ipAddress );
			sb.append(System.getProperty("line.separator"));
			sb.append("PDNID : " + pdnId );
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.MOD_HSS_STATIC_IP.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName(), 
						new StatisticsModel(ApiDefine.MOD_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		if(ipAddress != null) {
			String[] param = {"ipAddress", ipAddress};
			params.add(param);
		}

		if(pdnId != null) {
			String[] param = {"pdnId", pdnId};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.MOD_HSS_STATIC_IP.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName(), 
								new StatisticsModel(ApiDefine.MOD_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.MOD_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.MOD_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_HSS_STATIC_IP.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.MOD_HSS_STATIC_IP.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.MOD_HSS_STATIC_IP.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.MOD_HSS_STATIC_IP.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@PUT
	@Path("{imsi}_{msisdn}")
	@Produces("application/json;charset=UTF-8")
	public Response uptSubs(@Context HttpServletRequest req,
			@PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn,
			@FormParam("hlrTemplate") String hlrTemplate
			) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.UPT_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("HLRTEMPLATE : " + hlrTemplate);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName(), 
							new StatisticsModel(ApiDefine.UPT_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName(), 
							new StatisticsModel(ApiDefine.UPT_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.UPT_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("HLRTEMPLATE : " + hlrTemplate);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.UPT_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName(), 
						new StatisticsModel(ApiDefine.UPT_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}
		if(hlrTemplate != null) {
			String[] param = {"hlrTemplate", hlrTemplate};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.UPT_SUBS.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName(), 
								new StatisticsModel(ApiDefine.UPT_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName(), 
							new StatisticsModel(ApiDefine.UPT_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName(), 
							new StatisticsModel(ApiDefine.UPT_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.UPT_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.UPT_SUBS.getName());
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.UPT_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@DELETE
	@Path("{imsi}_{msisdn}")
	@Produces("application/json;charset=UTF-8")
	public Response delSubs(@Context HttpServletRequest req,
			@PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.DEL_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName(), 
							new StatisticsModel(ApiDefine.DEL_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName(), 
							new StatisticsModel(ApiDefine.DEL_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.DEL_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.DEL_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName(), 
						new StatisticsModel(ApiDefine.DEL_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.DEL_SUBS.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName(), 
								new StatisticsModel(ApiDefine.DEL_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName(), 
							new StatisticsModel(ApiDefine.DEL_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName(), 
							new StatisticsModel(ApiDefine.DEL_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.DEL_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.DEL_SUBS.getName());
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.DEL_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}_{msisdn}")
	@Produces("application/json;charset=UTF-8")
	public Response getSubsDetail(@Context HttpServletRequest req,
			@PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_SUBS_DETAIL.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_DETAIL.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_DETAIL.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError403();
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
			sb.append(ApiDefine.GET_SUBS_DETAIL.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_SUBS_DETAIL.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName(), 
						new StatisticsModel(ApiDefine.GET_SUBS_DETAIL.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_SUBS_DETAIL.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName(), 
								new StatisticsModel(ApiDefine.GET_SUBS_DETAIL.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_DETAIL.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName(), 
							new StatisticsModel(ApiDefine.GET_SUBS_DETAIL.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SUBS_DETAIL.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_SUBS_DETAIL.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_SUBS_DETAIL.getName());
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_SUBS_DETAIL.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/staticIpHss")
	@Produces("text/plain;charset=UTF-8")
	public Response getHssStaticIp(@Context HttpServletRequest req,
			@PathParam("imsi") String imsi) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_HSS_STATIC_IP.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.GET_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.GET_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError403();
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
			sb.append(ApiDefine.GET_HSS_STATIC_IP.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_HSS_STATIC_IP.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName(), 
						new StatisticsModel(ApiDefine.GET_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_HSS_STATIC_IP.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName(), 
								new StatisticsModel(ApiDefine.GET_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.GET_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.GET_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HSS_STATIC_IP.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_HSS_STATIC_IP.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_HSS_STATIC_IP.getName());
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_HSS_STATIC_IP.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@DELETE
	@Path("{imsi}_{msisdn}/staticIpHss/{ipAddress}_{pdnId}")
	@Produces("application/json;charset=UTF-8")
	public Response remHssStaticIp(@Context HttpServletRequest req,
			@PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn,
			@PathParam("ipAddress") String ipAddress,
			@PathParam("pdnId") String pdnId) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.REM_HSS_STATIC_IP.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("IPADDRESS : " + ipAddress);
			logger.info("PDNID : " + pdnId);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.REM_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.REM_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError403();
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
			sb.append(ApiDefine.REM_HSS_STATIC_IP.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("IPADDRESS : " + ipAddress);
			sb.append(System.getProperty("line.separator"));
			sb.append("PDNID : " + pdnId);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.REM_HSS_STATIC_IP.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName(), 
						new StatisticsModel(ApiDefine.REM_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}
		if(ipAddress != null) {
			String[] param = {"ipAddress", ipAddress};
			params.add(param);
		}
		if(pdnId != null) {
			String[] param = {"pdnId", pdnId};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.REM_HSS_STATIC_IP.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName(), 
								new StatisticsModel(ApiDefine.REM_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.REM_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.REM_HSS_STATIC_IP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_HSS_STATIC_IP.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.REM_HSS_STATIC_IP.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.REM_HSS_STATIC_IP.getName());
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.REM_HSS_STATIC_IP.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/apns")
	@Produces("application/json;charset=UTF-8")
	public Response getPdpForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_PDP_FOR_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_PDP_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_PDP_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.GET_PDP_FOR_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_PDP_FOR_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName(), 
						new StatisticsModel(ApiDefine.GET_PDP_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_PDP_FOR_SUBS.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName(), 
								new StatisticsModel(ApiDefine.GET_PDP_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_PDP_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_PDP_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDP_FOR_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_PDP_FOR_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_PDP_FOR_SUBS.getName());
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_PDP_FOR_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/hssapns")
	@Produces("application/json;charset=UTF-8")
	public Response getPdnForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_PDN_FOR_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_PDN_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_PDN_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.GET_PDN_FOR_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_PDN_FOR_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName(), 
						new StatisticsModel(ApiDefine.GET_PDN_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_PDN_FOR_SUBS.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName(), 
								new StatisticsModel(ApiDefine.GET_PDN_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_PDN_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_PDN_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_PDN_FOR_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_PDN_FOR_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.UPT_SUBS.getName());
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_PDN_FOR_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/roamres")
	@Produces("application/json;charset=UTF-8")
	public Response getRoamRestForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), 
						new StatisticsModel(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), 
								new StatisticsModel(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_ROAM_REST_FOR_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_ROAM_REST_FOR_SUBS.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_ROAM_REST_FOR_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/hssroamres")
	@Produces("application/json;charset=UTF-8")
	public Response getLteRoamRestForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), 
						new StatisticsModel(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), 
								new StatisticsModel(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/networkBar")
	@Produces("application/json;charset=UTF-8")
	public Response setNetworkBar(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SET_NETWORK_BAR.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName(), 
							new StatisticsModel(ApiDefine.SET_NETWORK_BAR.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName(), 
							new StatisticsModel(ApiDefine.SET_NETWORK_BAR.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError403();
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
			sb.append(ApiDefine.SET_NETWORK_BAR.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SET_NETWORK_BAR.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName(), 
						new StatisticsModel(ApiDefine.SET_NETWORK_BAR.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.SET_NETWORK_BAR.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName(), 
								new StatisticsModel(ApiDefine.SET_NETWORK_BAR.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName(), 
							new StatisticsModel(ApiDefine.SET_NETWORK_BAR.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName(), 
							new StatisticsModel(ApiDefine.SET_NETWORK_BAR.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_BAR.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SET_NETWORK_BAR.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.SET_NETWORK_BAR.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SET_NETWORK_BAR.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/networkUnbar")
	@Produces("application/json;charset=UTF-8")
	public Response setNetworkUnBar(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SET_NETWORK_UNBAR.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName(), 
							new StatisticsModel(ApiDefine.SET_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName(), 
							new StatisticsModel(ApiDefine.SET_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError403();
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
			sb.append(ApiDefine.SET_NETWORK_UNBAR.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SET_NETWORK_UNBAR.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName(), 
						new StatisticsModel(ApiDefine.SET_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.SET_NETWORK_UNBAR.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName(), 
								new StatisticsModel(ApiDefine.SET_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName(), 
							new StatisticsModel(ApiDefine.SET_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName(), 
							new StatisticsModel(ApiDefine.SET_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_NETWORK_UNBAR.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SET_NETWORK_UNBAR.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.SET_NETWORK_UNBAR.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SET_NETWORK_UNBAR.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/lteNetworkBar")
	@Produces("application/json;charset=UTF-8")
	public Response setLteNetworkBar(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SET_LTE_NETWORK_BAR.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName(), 
							new StatisticsModel(ApiDefine.SET_LTE_NETWORK_BAR.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName(), 
							new StatisticsModel(ApiDefine.SET_LTE_NETWORK_BAR.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError403();
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
			sb.append(ApiDefine.SET_LTE_NETWORK_BAR.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SET_LTE_NETWORK_BAR.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName(), 
						new StatisticsModel(ApiDefine.SET_LTE_NETWORK_BAR.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.SET_LTE_NETWORK_BAR.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName(), 
								new StatisticsModel(ApiDefine.SET_LTE_NETWORK_BAR.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName(), 
							new StatisticsModel(ApiDefine.SET_LTE_NETWORK_BAR.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName(), 
							new StatisticsModel(ApiDefine.SET_LTE_NETWORK_BAR.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_BAR.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SET_LTE_NETWORK_BAR.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.SET_LTE_NETWORK_BAR.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SET_LTE_NETWORK_BAR.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/lteNetworkUnbar")
	@Produces("application/json;charset=UTF-8")
	public Response setLteNetworkUnBar(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SET_LTE_NETWORK_UNBAR.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), 
							new StatisticsModel(ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), 
							new StatisticsModel(ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError403();
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
			sb.append(ApiDefine.SET_LTE_NETWORK_UNBAR.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), 
						new StatisticsModel(ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName())) {
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), 
								new StatisticsModel(ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), 
							new StatisticsModel(ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), 
							new StatisticsModel(ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SET_LTE_NETWORK_UNBAR.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SET_LTE_NETWORK_UNBAR.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.SET_LTE_NETWORK_UNBAR.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SET_LTE_NETWORK_UNBAR.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/cancelLocation")
	@Produces("application/json;charset=UTF-8")
	public Response sndCancelLoc(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SND_CANCEL_LOC.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName(), 
							new StatisticsModel(ApiDefine.SND_CANCEL_LOC.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName(), 
							new StatisticsModel(ApiDefine.SND_CANCEL_LOC.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError403();
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
			sb.append(ApiDefine.SND_CANCEL_LOC.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SND_CANCEL_LOC.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName(), 
						new StatisticsModel(ApiDefine.SND_CANCEL_LOC.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.SND_CANCEL_LOC.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName(), 
								new StatisticsModel(ApiDefine.SND_CANCEL_LOC.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName(), 
							new StatisticsModel(ApiDefine.SND_CANCEL_LOC.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName(), 
							new StatisticsModel(ApiDefine.SND_CANCEL_LOC.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_CANCEL_LOC.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SND_CANCEL_LOC.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.SND_CANCEL_LOC.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SND_CANCEL_LOC.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/cancelLteLocation")
	@Produces("application/json;charset=UTF-8")
	public Response sndLteCancelLoc(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SND_LTE_CANCEL_LOC.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName(), 
							new StatisticsModel(ApiDefine.SND_LTE_CANCEL_LOC.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName(), 
							new StatisticsModel(ApiDefine.SND_LTE_CANCEL_LOC.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError403();
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
			sb.append(ApiDefine.SND_LTE_CANCEL_LOC.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SND_LTE_CANCEL_LOC.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName(), 
						new StatisticsModel(ApiDefine.SND_LTE_CANCEL_LOC.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.SND_LTE_CANCEL_LOC.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName(), 
								new StatisticsModel(ApiDefine.SND_LTE_CANCEL_LOC.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName(), 
							new StatisticsModel(ApiDefine.SND_LTE_CANCEL_LOC.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName(), 
							new StatisticsModel(ApiDefine.SND_LTE_CANCEL_LOC.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.SND_LTE_CANCEL_LOC.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.SND_LTE_CANCEL_LOC.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.SND_LTE_CANCEL_LOC.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.SND_LTE_CANCEL_LOC.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/sgsnLocation")
	@Produces("text/plain;charset=UTF-8")
	public Response getSgsnLocForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), 
						new StatisticsModel(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), 
								new StatisticsModel(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/mscLocation")
	@Produces("text/plain;charset=UTF-8")
	public Response getMscLocForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), 
						new StatisticsModel(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

		while(clientID != receiveReqID ) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		int resultCode = rspCode;

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(rspCode == 200) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), 
								new StatisticsModel(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MSC_LOC_FOR_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_MSC_LOC_FOR_SUBS.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_MSC_LOC_FOR_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/mmeLocation")
	@Produces("text/plain;charset=UTF-8")
	public Response getMmeLocForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_MME_LOC_FOR_SUBS.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError403();
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
			sb.append(ApiDefine.GET_MME_LOC_FOR_SUBS.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), 
						new StatisticsModel(ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), 
								new StatisticsModel(ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), 
							new StatisticsModel(ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_MME_LOC_FOR_SUBS.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_MME_LOC_FOR_SUBS.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_MME_LOC_FOR_SUBS.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_MME_LOC_FOR_SUBS.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@PUT
	@Path("{imsi}_{msisdn}/updateSimAttributesOnHlr")
	@Produces("application/json;charset=UTF-8")
	public Response getUptSubsAtt(@Context HttpServletRequest req, @PathParam("imsi") String imsi, @PathParam("msisdn") String msisdn) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.UPT_SUBS_ATT.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName(), 
							new StatisticsModel(ApiDefine.UPT_SUBS_ATT.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName(), 
							new StatisticsModel(ApiDefine.UPT_SUBS_ATT.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError403();
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
			sb.append(ApiDefine.UPT_SUBS_ATT.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.UPT_SUBS_ATT.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName(), 
						new StatisticsModel(ApiDefine.UPT_SUBS_ATT.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(msisdn != null) {
			String[] param = {"msisdn", msisdn};
			params.add(param);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.UPT_SUBS_ATT.getName(), params, this, clientID, imsi, msisdn, req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName(), 
								new StatisticsModel(ApiDefine.UPT_SUBS_ATT.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName(), 
							new StatisticsModel(ApiDefine.UPT_SUBS_ATT.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName(), 
							new StatisticsModel(ApiDefine.UPT_SUBS_ATT.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.UPT_SUBS_ATT.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.UPT_SUBS_ATT.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.UPT_SUBS_ATT.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.UPT_SUBS_ATT.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@PUT
	@Path("{imsi}staticIp/{ipAddress}_{pdpId}")
	@Produces("application/json;charset=UTF-8")
	public Response modStaticIpAtt(@Context HttpServletRequest req, @PathParam("imsi") String imsi, @PathParam("ipAddress") String ipAddress, @PathParam("pdpId") String pdpId) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.MOD_STATIC_IP.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("IPADDRESS : " + ipAddress);
			logger.info("pdpId : " + pdpId);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.MOD_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.MOD_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError403();
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
			sb.append(ApiDefine.MOD_STATIC_IP.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("pdpId : " + pdpId);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.MOD_STATIC_IP.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName(), 
						new StatisticsModel(ApiDefine.MOD_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(ipAddress != null) {
			String[] param = {"ipAddress", ipAddress};
			params.add(param);
		}
		if(pdpId != null) {
			String[] param = {"pdpId", pdpId};
			params.add(param);
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		DBMManager.getInstance().sendCommand(ApiDefine.MOD_STATIC_IP.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName(), 
								new StatisticsModel(ApiDefine.MOD_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.MOD_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.MOD_STATIC_IP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.MOD_STATIC_IP.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.MOD_STATIC_IP.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.MOD_STATIC_IP.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.MOD_STATIC_IP.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/staticIp")
	@Produces("text/plain;charset=UTF-8")
	public Response getStaticIpAtt(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_STATIC_IP.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.GET_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.GET_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError403();
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
			sb.append(ApiDefine.GET_STATIC_IP.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_STATIC_IP.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName(), 
						new StatisticsModel(ApiDefine.GET_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		DBMManager.getInstance().sendCommand(ApiDefine.GET_STATIC_IP.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName(), 
								new StatisticsModel(ApiDefine.GET_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.GET_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.GET_STATIC_IP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_STATIC_IP.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_STATIC_IP.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.GET_STATIC_IP.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.GET_STATIC_IP.getName(), imsi, "124", sb.toString(), 0);
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@DELETE
	@Path("{imsi}/staticIp/{ipAddress}_{pdpId}")
	@Produces("application/json;charset=UTF-8")
	public Response remStaticIpAtt(@Context HttpServletRequest req, @PathParam("imsi") String imsi, @PathParam("ipAddress") String ipAddress, @PathParam("pdpId") String pdpId) {
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.REM_STATIC_IP.getName() + " REQUSET");
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("IPADDRESS : " + ipAddress);
			logger.info("pdpId : " + pdpId);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.REM_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.REM_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError403();
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
			sb.append(ApiDefine.REM_STATIC_IP.getName() + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("pdpId : " + pdpId);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.REM_STATIC_IP.getName(), imsi, "242", sb.toString(), 0);
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName(), 
						new StatisticsModel(ApiDefine.REM_STATIC_IP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		if(ipAddress != null) {
			String[] param = {"ipAddress", ipAddress};
			params.add(param);
		}
		if(pdpId != null) {
			String[] param = {"pdpId", pdpId};
			params.add(param);
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		DBMManager.getInstance().sendCommand(ApiDefine.REM_STATIC_IP.getName(), params, this, clientID, imsi, "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName(), 
								new StatisticsModel(ApiDefine.REM_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.REM_STATIC_IP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName(), 
							new StatisticsModel(ApiDefine.REM_STATIC_IP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.REM_STATIC_IP.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.REM_STATIC_IP.getName() + " REPONSE");
			logger.info("STATUS : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		if(traceFlag) {
			StringBuffer sb = new StringBuffer();
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + req.getRemoteAddr() + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(ApiDefine.REM_STATIC_IP.getName() + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage("TRACE_" + ApiDefine.REM_STATIC_IP.getName(), imsi, "124", sb.toString(), 0);
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
