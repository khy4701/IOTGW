package com.kt.restful.service;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.net.CommandConnector;
import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.IUDRHeartbeatCheckModel;
import com.kt.util.SSLUtil;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

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
		
		iudrConnCheckList.add(new IUDRHeartbeatCheckModel("http://125.159.62.140:26789/ktadapter/api/v1/kt/apns/1", 0, 1, 0));
		for(int i = 1; i < 33; i++)
			iudrConnCheckList.add(new IUDRHeartbeatCheckModel("1.1.1."+i, 0,1, 0));
		
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
		
		// if provc2, provc3 -- return.
		if ( ! IoTProperty.getPropPath("sys_name").equals("provc1"))
			return ;
		
		synchronized (CommandConnector.getInstance().getIudrConnCheckList()) {
			ClientResponse response = null;

			for(IUDRHeartbeatCheckModel model : CommandConnector.getInstance().getIudrConnCheckList()) {
				for(int i = 0; i < Integer.parseInt(IoTProperty.getPropPath("iudr_conn_check_retry")) + 1; i++) {
					try {
						Client client = Client.create();
						WebResource webResource = null;

						// If it needs connection Type. 02.19
						// model.getConnType()
						
						// hykim add.02.19
						String url = String.format(IoTProperty.getPropPath("iudr.api.url.HEALTH_CHECK"),
								model.getIpAddress(), model.getPort(),
								IoTProperty.getPropPath("iudr_hlradapter"),
								IoTProperty.getPropPath("iudr_version"));

						client.setConnectTimeout(Integer.parseInt(IoTProperty.getPropPath("iudr_conn_check_time_out")));

						webResource = client.resource(url);
						response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);

						if(response.getStatus() == 200) {
							model.setStatus(1);	
							i = Integer.parseInt(IoTProperty.getPropPath("iudr_conn_check_retry")) + 1;
						}
						response.close();
					} catch (Exception ex) {
						model.setStatus(0);
						if (response != null)
							response.close();
					}
				}
			}
			
			// EXTCONN_TYPE_IUDR  2 
			CommandConnector.getInstance().sendConnCheckMessage(2);
		}
	}
	
	public void checkBSSIOTHeartbeat() {
		
		// if provc3 -- return.
		if ( !(IoTProperty.getPropPath("sys_name").equals("provc1") || IoTProperty.getPropPath("sys_name").equals("provc2"))  )
			return ;
		
		synchronized (CommandConnector.getInstance().getBssiotConnCheckList()) {

			ClientResponse response = null;
			for(IUDRHeartbeatCheckModel model : CommandConnector.getInstance().getBssiotConnCheckList()) {
				String url = "";				
				for(int i = 0; i < Integer.parseInt(IoTProperty.getPropPath("bssiot_conn_check_retry")) + 1; i++) {
					try {
						Client client = null;
						WebResource webResource = null;
						
						// hykim add.02.19
						url = String.format(IoTProperty.getPropPath("bssiot.api.url.HEALTH_CHECK"), model.getIpAddress(), model.getPort());
						
						// URL Routing--> HTTP ? HTTPS?			
						if (url.substring(0, 5).equals("https")){
													
					        ClientConfig config = new DefaultClientConfig();
					        try {
								config.getProperties()
								        .put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
								                new HTTPSProperties(
								                        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER,
								                        SSLUtil.getInsecureSSLContext()));
							} catch (KeyManagementException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (NoSuchAlgorithmException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}			
							client = Client.create(config);
						}

						else if (url.substring(0, 4).equals("http")){
						
							client = Client.create();
						}

						client.setConnectTimeout(Integer.parseInt(IoTProperty.getPropPath("bssiot_conn_check_time_out")));

						webResource = client.resource(url);
						response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);

						logger.info("CHECK : " + url + ", RES : " + response.getStatus()+ ", BODY : " + response.getEntity(String.class) );

						if(response.getStatus() == 200) {
							model.setStatus(1);	
							i = Integer.parseInt(IoTProperty.getPropPath("bssiot_conn_check_retry")) + 1;
						}
						
						response.close();
					} catch (Exception ex) {
						logger.info( "Exception : " +ex + url);

						if (response != null)
							response.close();
						model.setStatus(0);
						
					}
				}
			}
			
			// EXTCONN_TYPE_BSS_IOT  5
			CommandConnector.getInstance().sendConnCheckMessage(5);
		}
	}
	
	public void checkCubicHeartbeat() {
		
		if (!IoTProperty.getPropPath("sys_name").equals("provc3"))
			return ;
		
		synchronized (CommandConnector.getInstance().getCubicConnCheckList()) {
			ClientResponse response = null;

			for(IUDRHeartbeatCheckModel model : CommandConnector.getInstance().getCubicConnCheckList()) {
								
				for(int i = 0; i < Integer.parseInt(IoTProperty.getPropPath("cubic_conn_check_retry")) + 1; i++) {
					try {
						Client client = Client.create();
						WebResource webResource = null;

						// hykim add.02.19
						String url = String.format(IoTProperty.getPropPath("cubic.api.url.HEALTH_CHECK"), 
								model.getIpAddress(), model.getPort());

						client.setConnectTimeout(Integer.parseInt(IoTProperty.getPropPath("cubic_conn_check_time_out")));

						webResource = client.resource(url);
						response = webResource.type(MediaType.APPLICATION_FORM_URLENCODED).get(ClientResponse.class);

						if(response.getStatus() == 200) {
							model.setStatus(1);	
							i = Integer.parseInt(IoTProperty.getPropPath("cubic_conn_check_retry")) + 1;
						}
						response.close();

					} catch (Exception ex) {
						model.setStatus(0);
						if (response != null)
							response.close();
					}
				}
			}
			
			// EXTCONN_TYPE_CUBIC  6
			CommandConnector.getInstance().sendConnCheckMessage(6);
		}
	}

	
}

