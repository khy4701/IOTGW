package com.kt.net;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.IUDRHeartbeatCheckModel;
import com.kt.restful.model.ProvifMsgType;
import com.kt.restful.service.BSSProvRequestService;
import com.kt.restful.service.CubicNotiCallbackService;
import com.kt.restful.service.IUDRProvRequestService;


public class DBMManager implements Receiver{
	private static DBMManager dbmManager;
	private static Logger logger = LogManager.getLogger(DBMManager.class);
	
	private static ArrayList<SenderInfo> dbmMembers;
	
	private static int cliReqId = 1000;
	
	public static void main(String[] args) {
		dbmManager = new DBMManager();
		
//		dbmManager.receiveMessage("ADD_SUBS", "1", 0, "body");
//		receiveMessage(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body)
//		dbmManager.receiveMessage("ADD_SUBS", "1", 0, "123456789012345", "01012345678", "192.168.70.133", "imsi=123456789012345");
		
		StringBuffer bodySB = new StringBuffer();
		List<String[]> params = new ArrayList<String[]>();
		
		String[] param1 = {"hlrTemplate", URLDecoder.decode("%7B%22hlrTemplateId%22%3A%22222222222222222%22%2C%22masterTemplate%22%3A%22111111111111111%22%2C%22gprsStatus%22%3Atrue%2C%22smsMtStatus%22%3Atrue%2C%22smsMoStatus%22%3Atrue%2C%22smsIntStatus%22%3Afalse%2C%22blockSmsMoIntExHc%22%3Afalse%2C%22blockPremiumNumbers%22%3Afalse%2C%22csdProfile%22%3Afalse%2C%22voiceMtStatus%22%3Atrue%2C%22voiceMoStatus%22%3Atrue%2C%22csdMtOverride%22%3Atrue%2C%22csdMtStatus%22%3Afalse%2C%22csdMoStatus%22%3Afalse%2C%22voiceIntStatus%22%3Afalse%2C%22voiceMail%22%3Afalse%2C%22callForward%22%3Afalse%2C%22camel%22%3Afalse%2C%22callWaiting%22%3Afalse%2C%22callHold%22%3Afalse%2C%22multiParty%22%3Afalse%2C%22lte%22%3Atrue%2C%22defaultPdnId%22%3A1%2C%22operatorId%22%3A333333%2C%22hlrResId%22%3A1%2C%22lteResName%22%3A%22rr1%22%7D")};
		params.add(param1);
		String[] param2 = {"pdpIds", URLDecoder.decode("%5B1%2C2%5D")};
		params.add(param2);
		
		for(int i = 0; i < params.size(); i++) {
			if(i != 0) bodySB.append(";");
			bodySB.append(String.format("%s=%s",  params.get(i)[0],  params.get(i)[1]));
		}
		
		String[] params1 = bodySB.toString().split(";");
		
		System.out.println("params1.len : " + params1.length);
		
		
		System.out.println(bodySB.toString());
		
		StringBuffer postBody = new StringBuffer();
		for(int i = 0; i < params.size(); i++) {
			if(i != 0) postBody.append("&");
			postBody.append(params1[i].split("=")[0]);
			postBody.append("=");
			postBody.append(params1[i].split("=")[1]);
		}
		
		System.out.println("c : " + URLDecoder.decode("hlrTemplate%3D%7B%22hlrTemplateId%22%3A%22222222222222222%22%2C%22masterTemplate%22%3A%22111111111111111%22%2C%22gprsStatus%22%3Atrue%2C%22smsMtStatus%22%3Atrue%2C%22smsMoStatus%22%3Atrue%2C%22smsIntStatus%22%3Afalse%2C%22blockSmsMoIntExHc%22%3Afalse%2C%22blockPremiumNumbers%22%3Afalse%2C%22csdProfile%22%3Afalse%2C%22voiceMtStatus%22%3Atrue%2C%22voiceMoStatus%22%3Atrue%2C%22csdMtOverride%22%3Atrue%2C%22csdMtStatus%22%3Afalse%2C%22csdMoStatus%22%3Afalse%2C%22voiceIntStatus%22%3Afalse%2C%22voiceMail%22%3Afalse%2C%22callForward%22%3Afalse%2C%22camel%22%3Afalse%2C%22callWaiting%22%3Afalse%2C%22callHold%22%3Afalse%2C%22multiParty%22%3Afalse%2C%22lte%22%3Atrue%2C%22defaultPdnId%22%3A1%2C%22operatorId%22%3A333333%2C%22hlrResId%22%3A1%2C%22lteResName%22%3A%22rr1%22%7D%26pdpIds%3D%5B1%2C2%5D"));
		String a = URLDecoder.decode(postBody.toString());
		System.out.println("a : " + a);
		String b = URLEncoder.encode(a);
		System.out.println("b : " + b);
		
	}
	
	private DBMManager() {
		dbmMembers = new ArrayList<SenderInfo>();
	}
	
	public static DBMManager getInstance() {
		if(dbmManager == null) {
			dbmManager = new DBMManager();
		}
		
		return dbmManager;
	}
	
	private int clientReqID = 0;
	
	public synchronized int getClientReqID(){
		clientReqID++;
		if(clientReqID > 10000){
			clientReqID = 0;
		}
		return clientReqID;
	}

	@SuppressWarnings("static-access")
	public synchronized static void sendCommand(String apiName, String result, int resCode, ProvifMsgType pmt) {	

		DBMConnector.getInstance().sendMessage(apiName, result, resCode, pmt);
	}
	
	@SuppressWarnings("static-access")
	public synchronized void receiveMessage(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body, ProvifMsgType pmt) {
		
//		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
//			String key = ipAddress.trim() + apiName.trim();
//			
//			if(StatisticsManager.getStatisticsHash().containsKey(key)) {
//				StatisticsManager.getInstance().getStatisticsHash().get(key).plusTotal();
//			} else {
//				StatisticsManager.getInstance().getStatisticsHash().put(key, 
//						new StatisticsModel(apiName.trim(), ipAddress.trim(), 1, 0, 0));
//			}
//		}
				
		List<IUDRHeartbeatCheckModel> iudrConnlist = CommandConnector.getInstance().getIudrConnCheckList();
		String appName = IoTProperty.getPropPath("sys_name");
		
		if (appName.equals("provc1") || appName.equals("provc2")) {
			synchronized (iudrConnlist) {
				for (IUDRHeartbeatCheckModel conn : iudrConnlist) {
					String ip = conn.getIpAddress();
					int port = conn.getPort();
					String checkStr = ip + ":" + port;
					// logger.info("IOT CHECK: " + checkStr + ", IP : " +
					// ipAddress );
					if (checkStr.equals(ipAddress)) {
						IUDRProvRequestService svc = new IUDRProvRequestService(apiName, seqNo, rspCode, imsi, msisdn,
								ipAddress, body, pmt);
						svc.start();
						return;
					}
				}
			}
		}
		
		// ipAddress -> destination ����
		List<IUDRHeartbeatCheckModel> bssiotConnlist = CommandConnector.getInstance().getBssiotConnCheckList();
		
		if (appName.equals("provc1") || appName.equals("provc2")) {
			synchronized (bssiotConnlist) {
				for (IUDRHeartbeatCheckModel conn : bssiotConnlist) {
					String ip = conn.getIpAddress();
					int port = conn.getPort();
					String checkStr = ip + ":" + port;

					// logger.info("BSS CHECK: " + checkStr + ", IP : " +
					// ipAddress);
					if (checkStr.equals(ipAddress)) {
						BSSProvRequestService svc = new BSSProvRequestService(apiName, seqNo, rspCode, imsi, msisdn,
								ipAddress, body, pmt);
						svc.start();
						return;
					}
				}
			}
		}		
		
		// ipAddress -> destination ����
		List<IUDRHeartbeatCheckModel> cubicConnlist = CommandConnector.getInstance().getCubicConnCheckList();
		if (appName.equals("provc3")) {
			synchronized (cubicConnlist) {
				for (IUDRHeartbeatCheckModel conn : cubicConnlist) {
					String ip = conn.getIpAddress();
					int port = conn.getPort();
					String checkStr = ip + ":" + port;

					// logger.info("BSS CHECK: " + checkStr + ", IP : " +
					// ipAddress);

					if (checkStr.equals(ipAddress)) {
						CubicNotiCallbackService svc = new CubicNotiCallbackService(apiName, seqNo, rspCode, imsi,
								msisdn, ipAddress, body, pmt);
						svc.start();
						return;
					}
				}
			}
		}
		
		logger.info("Not defined connection info: " + ipAddress + ", API NAME : " + apiName );
		logger.info("[1] IUDR List: ");
		for (IUDRHeartbeatCheckModel conn : iudrConnlist) {
			logger.info("IP ADDRESS: " + conn.getIpAddress() +":" + conn.getPort());
		}
		
		logger.info("[2] BSS List: ");
		for (IUDRHeartbeatCheckModel conn : bssiotConnlist) {
			logger.info("IP ADDRESS: " + conn.getIpAddress() +":" + conn.getPort());
		}
		
		logger.info("[3] CUBIC List: ");
		for (IUDRHeartbeatCheckModel conn : cubicConnlist) {
			logger.info("IP ADDRESS: " + conn.getIpAddress() +":" + conn.getPort());
		}

	}	
	
	
//	@SuppressWarnings("static-access")
//	public synchronized static void sendCommand(String apiName, String seqNo, String imsi, String mdn, String ipAddress, String result, int resCode) {	
//
//		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
//			if(resCode == 200) {
//				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
//					StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusSucc();
//				} else {
//					StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
//							new StatisticsModel(apiName, ipAddress, 0, 1, 0));
//				}
//			} else {
//				if(StatisticsManager.getInstance().getStatisticsHash().containsKey(ipAddress+apiName)) {
//					if(resCode != -1) {
//						StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusFail();
//
//						if (resCode == 412)
//							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError412();
//						else if (resCode == 500)
//							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError500();
//						else
//							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError400();
//					}
//				} else {
//					if(resCode != -1) {
//						StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
//								new StatisticsModel(apiName, ipAddress, 0, 0, 1));
//
//						if (resCode == 412)
//							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError412();
//						else if (resCode == 500)
//							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError500();
//						else
//							StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusError400();
//					}
//				}
//			}
//		}
//
//		DBMConnector.getInstance().sendMessage(apiName, seqNo, imsi, mdn, ipAddress, result, resCode);
//	}
//	
//	@SuppressWarnings("static-access")
//	public synchronized void receiveMessage(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body) {
//		
//		synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
//			if(StatisticsManager.getStatisticsHash().containsKey(ipAddress+apiName)) {
//				StatisticsManager.getInstance().getStatisticsHash().get(ipAddress+apiName).plusTotal();
//			} else {
//				StatisticsManager.getInstance().getStatisticsHash().put(ipAddress+apiName, 
//						new StatisticsModel(apiName, ipAddress, 1, 0, 0));
//			}
//		}
//		
//		HSSProvRequestService svc = new HSSProvRequestService(apiName, seqNo, rspCode, imsi, msisdn, ipAddress, body);
//		svc.start();
//	}
}
