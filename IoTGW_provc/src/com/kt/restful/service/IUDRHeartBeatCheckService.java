package com.kt.restful.service;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.net.CommandConnector;
import com.kt.net.CommandManager;
import com.kt.net.DBMManager;
import com.kt.net.StatisticsManager;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ApiDefine;
import com.kt.restful.model.IUDRHeartbeatCheckModel;
import com.kt.restful.model.StatisticsModel;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class IUDRHeartBeatCheckService extends Thread{

	private static Logger logger = LogManager.getLogger(IUDRHeartBeatCheckService.class);

	private String apiName = new String();
	private String seqNo = new String();
	private String imsi = new String();
	private String msisdn = new String();
	private String ipAddress = new String();
	private int rspCode = -1;
	private String body = new String();
	
	private static IUDRHeartBeatCheckService IUDRHearBeatCheckService;

	public static void main(String[] args ) {
//		System.out.println("main");
//		Client client = Client.create();
//		WebResource webResource = null;
//		ClientResponse response = null;
//		String url = "http://192.168.77.133";
//
//		client.setConnectTimeout(1000);
//
//		webResource = client.resource(url);
//		response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);
//
//		System.out.println("response.getStatus() : " + response.getStatus());
		
		List<IUDRHeartbeatCheckModel> iudrConnCheckList = new ArrayList<IUDRHeartbeatCheckModel>();
		
		iudrConnCheckList.add(new IUDRHeartbeatCheckModel("http://125.159.62.140:26789/ktadapter/api/v1/kt/apns/1", 1));
		for(int i = 1; i < 33; i++)
			iudrConnCheckList.add(new IUDRHeartbeatCheckModel("1.1.1."+i, 1));
		
		for(IUDRHeartbeatCheckModel model : iudrConnCheckList) {
			System.out.println("IP Address : " + model.getIpAddress() + " status : "+ model.getStatus() );
		}
		System.out.println("==========================================================");
		for(IUDRHeartbeatCheckModel model : iudrConnCheckList) {
			try {
				Client client = Client.create();
				WebResource webResource = null;
				ClientResponse response = null;

				String url = String.format("http://%s/ktadapter/api/vi/kt/heartbeat", model.getIpAddress());
				
				if(model.getIpAddress().indexOf("kt") > -1) {
					url = "http://125.159.62.140:26789/ktadapter/api/v1/kt/apns/1";
				}

				client.setConnectTimeout(100);

				webResource = client.resource(url);
				response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);
				
				if(response.getStatus() == 200) {
					model.setStatus(1);	
				}
			} catch (Exception ex) {
				System.out.println("Exception ex : " + model.getIpAddress());
//				ex.printStackTrace();
				model.setStatus(0);
			}
		}
		
		for(IUDRHeartbeatCheckModel model : iudrConnCheckList) {
			System.out.println("IP Address : " + model.getIpAddress() + " status : "+ model.getStatus() );
		}
	}


	public IUDRHeartBeatCheckService() {
	}
	
	public static IUDRHeartBeatCheckService getInstance() {
		if(IUDRHearBeatCheckService == null) {
			IUDRHearBeatCheckService = new IUDRHeartBeatCheckService();
		}
		return IUDRHearBeatCheckService;
	}

	
	public void checkIUDRHeartbeat() {
		
		synchronized (CommandConnector.getInstance().getIudrConnCheckList()) {

			for(IUDRHeartbeatCheckModel model : CommandConnector.getInstance().getIudrConnCheckList()) {
				for(int i = 0; i < Integer.parseInt(IoTProperty.getPropPath("iudr_conn_check_retry")) + 1; i++) {
					try {
						Client client = Client.create();
						WebResource webResource = null;
						ClientResponse response = null;

						String url = String.format("http://%s/%s/api/%s/kt/heartbeat", 
								model.getIpAddress(),
								IoTProperty.getPropPath("iudr_hlradapter"),
								IoTProperty.getPropPath("iudr_version"));

						client.setConnectTimeout(Integer.parseInt(IoTProperty.getPropPath("iudr_conn_check_time_out")));

						webResource = client.resource(url);
						response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);

						if(response.getStatus() == 200) {
							model.setStatus(1);	
							i = Integer.parseInt(IoTProperty.getPropPath("iudr_conn_check_retry")) + 1;
						}
					} catch (Exception ex) {
						model.setStatus(0);
					}
				}
			}
			
			CommandConnector.getInstance().sendIUDRConnCheckMessage();
		}
		
	}
}

