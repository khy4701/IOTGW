package com.kt.restful.service;


import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import com.kt.net.DBMListener;

@Path("/apns")
@Produces("application/json;charset=UTF-8")
public class ApnsService implements DBMListener {

	@GET
	@Path("{pdpId}")
	@Produces("application/json;charset=UTF-8")
	public Response getApnByPdpId(@Context HttpServletRequest req, @PathParam("pdpId") String pdpId) {
		return null;
	}

	@Override
	public void setComplete(String msg, int reqId) {
		// TODO Auto-generated method stub
		
	}
}
