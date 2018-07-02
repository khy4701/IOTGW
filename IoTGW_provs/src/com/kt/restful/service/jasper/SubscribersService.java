package com.kt.restful.service.jasper;

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
import com.kt.net.DBMConnector;
import com.kt.net.DBMListener;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.MMCMsgType;
import com.kt.restful.model.ProvifMsgType;
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

		String reqIpAddress = req.getRemoteAddr();
		String apiName = ApiDefine.ADD_SUBS.getName();
		
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
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

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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
		
		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);
		
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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					
					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
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
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName = ApiDefine.MOD_HSS_STATIC_IP.getName();


		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi );
			logger.info("IPADDRESS : " + ipAddress );
			logger.info("PDNID : " + pdnId );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi );
			sb.append(System.getProperty("line.separator"));
			sb.append("reqIpAddress : " + reqIpAddress );
			sb.append(System.getProperty("line.separator"));
			sb.append("PDNID : " + pdnId );
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
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
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName = ApiDefine.UPT_SUBS.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("HLRTEMPLATE : " + hlrTemplate);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
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

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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
		
		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName);
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
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

		String reqIpAddress = req.getRemoteAddr();
		String apiName = ApiDefine.DEL_SUBS.getName();
		
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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
		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName);
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
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

		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_SUBS_DETAIL.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}
		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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
		
		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName);
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/staticIpHss")
	@Produces("text/plain;charset=UTF-8")
	public Response getHssStaticIp(@Context HttpServletRequest req,
			@PathParam("imsi") String imsi) {

		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_HSS_STATIC_IP.getName();

		
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}
		
		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName);
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
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

		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_HSS_STATIC_IP.getName();

		
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("IPADDRESS : " + ipAddress);
			logger.info("PDNID : " + pdnId);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("reqIpAddress : " + reqIpAddress);
			sb.append(System.getProperty("line.separator"));
			sb.append("PDNID : " + pdnId);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

            CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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
		
		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName);
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

            CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/apns")
	@Produces("application/json;charset=UTF-8")
	public Response getPdpForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_APN_FOR_SUBS.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName);
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/hssapns")
	@Produces("application/json;charset=UTF-8")
	public Response getPdnForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_HSS_APN_FOR_SUBS.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName);
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/roamres")
	@Produces("application/json;charset=UTF-8")
	public Response getRoamRestForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {

		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_ROAM_REST_FOR_SUBS.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/hssroamres")
	@Produces("application/json;charset=UTF-8")
	public Response getLteRoamRestForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {

		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_LTE_ROAM_REST_FOR_SUBS.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/networkBar")
	@Produces("application/json;charset=UTF-8")
	public Response setNetworkBar(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.SET_NETWORK_BAR.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/networkUnbar")
	@Produces("application/json;charset=UTF-8")
	public Response setNetworkUnBar(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.SET_NETWORK_UNBAR.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/lteNetworkBar")
	@Produces("application/json;charset=UTF-8")
	public Response setLteNetworkBar(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {

		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.SET_LTE_NETWORK_BAR.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/lteNetworkUnbar")
	@Produces("application/json;charset=UTF-8")
	public Response setLteNetworkUnBar(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.SET_LTE_NETWORK_UNBAR.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/cancelLocation")
	@Produces("application/json;charset=UTF-8")
	public Response sndCancelLoc(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {

		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.SND_CANCEL_LOC.getName();

		
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@POST
	@Path("{imsi}_{msisdn}/cancelLteLocation")
	@Produces("application/json;charset=UTF-8")
	public Response sndLteCancelLoc(@Context HttpServletRequest req, @PathParam("imsi") String imsi,
			@PathParam("msisdn") String msisdn) {

		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.SND_LTE_CANCEL_LOC.getName();

		
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/sgsnLocation")
	@Produces("text/plain;charset=UTF-8")
	public Response getSgsnLocForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_SGSN_LOC_FOR_SUBS.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/mscLocation")
	@Produces("text/plain;charset=UTF-8")
	public Response getMscLocForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {

		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_MSC_LOC_FOR_SUBS.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/mmeLocation")
	@Produces("text/plain;charset=UTF-8")
	public Response getMmeLocForSubs(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {

		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_MME_LOC_FOR_SUBS.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}

			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}


		int clientID = DBMManager.getInstance().getClientReqID();

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@PUT
	@Path("{imsi}_{msisdn}/updateSimAttributesOnHlr")
	@Produces("application/json;charset=UTF-8")
	public Response getUptSubsAtt(@Context HttpServletRequest req, @PathParam("imsi") String imsi, @PathParam("msisdn") String msisdn) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.UPT_SUBS_ATT.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("MSISDN : " + msisdn);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSISDN : " + msisdn);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setMdn(msisdn);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@PUT
	@Path("{imsi}staticIp/{ipAddress}_{pdpId}")
	@Produces("application/json;charset=UTF-8")
	public Response modStaticIpAtt(@Context HttpServletRequest req, @PathParam("imsi") String imsi, @PathParam("ipAddress") String ipAddress, @PathParam("pdpId") String pdpId) {
		
		String reqIpAddress =  req.getRemoteAddr();
		String apiName = ApiDefine.MOD_STATIC_IP.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("IPADDRESS : " + ipAddress);
			logger.info("pdpId : " + pdpId);
			logger.info("=============================================");
		}		

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("pdpId : " + pdpId);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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
		
		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

			CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@GET
	@Path("{imsi}/staticIp")
	@Produces("text/plain;charset=UTF-8")
	public Response getStaticIpAtt(@Context HttpServletRequest req, @PathParam("imsi") String imsi) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName =  ApiDefine.GET_STATIC_IP.getName();

		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName);
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi );
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}

			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
			}
		}

		List<String[]> params = new ArrayList<String[]>();

		if(imsi != null) {
			String[] param = {"imsi", imsi};
			params.add(param);
		}

		int clientID = DBMManager.getInstance().getClientReqID();

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();


				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
		}

		return Response.status(resultCode).entity(this.msg).build();
	}

	@SuppressWarnings("static-access")
	@DELETE
	@Path("{imsi}/staticIp/{ipAddress}_{pdpId}")
	@Produces("application/json;charset=UTF-8")
	public Response remStaticIpAtt(@Context HttpServletRequest req, @PathParam("imsi") String imsi, @PathParam("ipAddress") String ipAddress, @PathParam("pdpId") String pdpId) {
		
		String reqIpAddress = req.getRemoteAddr();
		String apiName = ApiDefine.REM_STATIC_IP.getName();
				
		if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info(apiName + " REQUSET");
			logger.info("REQUEST URL : " + req.getRequestURL().toString());
			logger.info("IMSI : " + imsi);
			logger.info("IPADDRESS : " + ipAddress);
			logger.info("pdpId : " + pdpId);
			logger.info("=============================================");
		}

		if(StatisticsManager.getInstance().getTps() > CommandManager.getInstance().getOverloadTps()){
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
				}
			}
			logger.info("Overload Control Flag : " + CommandManager.getInstance().isOverloadControlFlag() + ", TPS : " + StatisticsManager.getInstance().getTps() + " Overload TPS : " + CommandManager.getInstance().getOverloadTps() + " Return 503(The service is unavailable)");
			return Response.status(503).entity("").build();
		}

		boolean allowIpFlag = false;
		for(String allowIp : IoTProperty.getPropPath("allow_ip_list").split(",")) {
			if(allowIp.equals(reqIpAddress))
			{
				allowIpFlag = true;
				break;
			}
		}

		if(!allowIpFlag) {
			if(CommandManager.getInstance().get_ccAllowIpList().contains(reqIpAddress)){
				allowIpFlag = true;
			}
		}

		if(!allowIpFlag) {
			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 1));
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
				}
			}
			logger.info("Request Remote IP("+reqIpAddress +") Not Allow IP");
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
			sb.append("DIRECTION : CTRLCENTER[" + reqIpAddress + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + req.getRequestURL().toString());
			sb.append(System.getProperty("line.separator"));
			sb.append("IMSI : " + imsi);
			sb.append(System.getProperty("line.separator"));
			sb.append("pdpId : " + pdpId);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "242", "0", "", ""), sb.toString());
		}

		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
			if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusTotal();
			} else {
				StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
						new StatisticsModel("CC", apiName, reqIpAddress, 1, 0, 0));
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

		ProvifMsgType pmt = new ProvifMsgType();
		pmt.setUrl(req.getRequestURL().toString());
		pmt.setImsi(imsi);
		pmt.setIpAddress(reqIpAddress);

		DBMManager.getInstance().sendCommand(apiName, params, this, clientID, pmt);

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
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					//				StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
					if(StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName) == null)
						StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
								new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusSucc();
				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 1, 0));
				}
			} else {
				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(reqIpAddress+apiName)) {
					StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusFail();

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();

				} else {
					StatisticsManager.getInstance().getStatisticsHash().put(reqIpAddress+apiName, 
							new StatisticsModel("CC", apiName, reqIpAddress, 0, 0, 1));

					if( rspCode == 400)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError400();
					else if (rspCode == 403)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError403();
					else if (rspCode == 409)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError409();
					else if (rspCode == 410)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError410();
					else if (rspCode == 500)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError500();
					else if (rspCode == 501)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError501();
					else if (rspCode == 503)
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusError503();
					else
						StatisticsManager.getInstance().getStatisticsHash().get(reqIpAddress+apiName).plusErrorEtc();
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
			sb.append("DIRECTION : PROVS -> CTRLCENTER[" + reqIpAddress + "]");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REPONSE");
			sb.append(System.getProperty("line.separator"));
			sb.append("STATUS : " + resultCode);
			sb.append(System.getProperty("line.separator"));
			sb.append(this.msg);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));

                CommandManager.getInstance().sendMessage(new MMCMsgType(IoTProperty.getPropPath("sys_name"), "TRACE_" + apiName, imsi, "124", "0", "", ""), sb.toString());
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
