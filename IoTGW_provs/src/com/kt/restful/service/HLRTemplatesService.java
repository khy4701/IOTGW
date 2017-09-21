package com.kt.restful.service;


import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
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

@Path("/hlrTemplates")
public class HLRTemplatesService implements DBMListener  {

	private static Logger logger = LogManager.getLogger(HLRTemplatesService.class);

	@SuppressWarnings({ "deprecation", "static-access" })
	@POST
	@Produces("application/json;charset=UTF-8")
	public Response addHLRTemplates(@Context HttpServletRequest req, @FormParam("hlrTemplate") String hlrTemplate,
			@FormParam("pdpIds") String pdpIds,
			@FormParam("pdnIds") String pdnIds,
			@FormParam("defaultPdnId") String defaultPdnId,
			@FormParam("opId") String opId) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.ADD_HLR_TEMP.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("hlrTemplete : " + hlrTemplate );
			logger.info("pdpIds : " + pdpIds );
			logger.info("pdnIds : " + pdnIds );
			logger.info("defaultPdnId : " + defaultPdnId );
			logger.info("opId : " + opId );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.ADD_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.ADD_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError403();
				}
			}
			logger.info("Request Remote IP("+req.getRemoteAddr() +") Not Allow IP");
			return Response.status(403).entity("").build();
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName(), 
						new StatisticsModel(ApiDefine.ADD_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

//		String hlrTemplate = URLDecoder.decode(hlrTemplete);
		List<String[]> params = new ArrayList<String[]>();

		if(hlrTemplate != null) {
			String[] param1 = {"hlrTemplate", URLDecoder.decode(hlrTemplate)};
			params.add(param1);
		}
		if(pdpIds != null) {
			String[] param2 = {"pdpIds", URLDecoder.decode(pdpIds)};
			params.add(param2);
		}
		if(pdnIds != null) {
			String[] param3 = {"pdnIds", URLDecoder.decode(pdnIds)};
			params.add(param3);
		}
		if(defaultPdnId != null) {
			String[] param4 = {"defaultPdnId", URLDecoder.decode(defaultPdnId)};
			params.add(param4);
		}
		if(opId != null) {
			String[] param5 = {"opId", URLDecoder.decode(opId)};
			params.add(param5);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.ADD_HLR_TEMP.getName(), params, this, clientID, "", "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName(), 
								new StatisticsModel(ApiDefine.ADD_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.ADD_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.ADD_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError501();
				}
//				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName())) {
//					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusFail();
//
//					if( rspCode == 400)
//						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError400();
//					else if (rspCode == 500)
//						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError500();
//
//				} else {
//					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName(), 
//							new StatisticsModel(ApiDefine.ADD_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 0, 1));
//
//					if( rspCode == 400)
//						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError400();
//					else if (rspCode == 500)
//						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.ADD_HLR_TEMP.getName()).plusError500();
//				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.ADD_HLR_TEMP.getName() + " REPONSE");
			logger.info("Stauts : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		return Response.status(resultCode).entity(this.msg).build();
	}
	
	@SuppressWarnings({ "deprecation", "static-access" })
	@GET
	@Path("{hlrTemplateId}")
	@Produces("application/json;charset=UTF-8")
	public Response getHLRTemplates(@Context HttpServletRequest req, @PathParam("hlrTemplateId") String hlrTemplateId) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_HLR_TEMP.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("hlrTemplateId : " + hlrTemplateId );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.GET_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.GET_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError403();
				}
			}
			logger.info("Request Remote IP("+req.getRemoteAddr() +") Not Allow IP");
			return Response.status(403).entity("").build();
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName(), 
						new StatisticsModel(ApiDefine.GET_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

//		String hlrTemplate = URLDecoder.decode(hlrTemplete);
		List<String[]> params = new ArrayList<String[]>();

		if(hlrTemplateId!= null) {
			String[] param1 = {"hlrTemplateId", hlrTemplateId};
			params.add(param1);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.GET_HLR_TEMP.getName(), params, this, clientID, "", "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName(), 
								new StatisticsModel(ApiDefine.GET_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.GET_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.GET_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.GET_HLR_TEMP.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.GET_HLR_TEMP.getName() + " REPONSE");
			logger.info("Stauts : " + resultCode);
			logger.info(this.msg);
			logger.info("=============================================");
		}

		return Response.status(resultCode).entity(this.msg).build();
	}
	
	@SuppressWarnings({ "deprecation", "static-access" })
	@DELETE
	@Path("{hlrTemplateId}")
	@Produces("application/json;charset=UTF-8")
	public Response delHLRTemplates(@Context HttpServletRequest req, @PathParam("hlrTemplateId") String hlrTemplateId) {

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.DEL_HLR_TEMP.getName());
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("hlrTemplateId : " + hlrTemplateId );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.DEL_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError503();
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.DEL_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError403();
				}
			}
			logger.info("Request Remote IP("+req.getRemoteAddr() +") Not Allow IP");
			return Response.status(403).entity("").build();
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName())) {
				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName(), 
						new StatisticsModel(ApiDefine.DEL_HLR_TEMP.getName(), req.getRemoteAddr(), 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

//		String hlrTemplate = URLDecoder.decode(hlrTemplete);
		List<String[]> params = new ArrayList<String[]>();

		if(hlrTemplateId!= null) {
			String[] param1 = {"hlrTemplateId", hlrTemplateId};
			params.add(param1);
		}

		DBMManager.getInstance().sendCommand(ApiDefine.DEL_HLR_TEMP.getName(), params, this, clientID, "", "", req.getRemoteAddr());

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName())) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName(), 
								new StatisticsModel(ApiDefine.DEL_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.DEL_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName())) {
					StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError501();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName(), 
							new StatisticsModel(ApiDefine.DEL_HLR_TEMP.getName(), req.getRemoteAddr(), 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError400();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(req.getRemoteAddr()+ApiDefine.DEL_HLR_TEMP.getName()).plusError501();
				}
			}
		}

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(ApiDefine.DEL_HLR_TEMP.getName() + " REPONSE");
			logger.info("Stauts : " + resultCode);
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
