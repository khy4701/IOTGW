package com.kt.restful.scheduler;

import java.util.TimerTask;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.net.CommandConnector;
import com.kt.net.DBMConnector;
import com.kt.net.StatisticsConnector;
import com.kt.net.StatisticsManager;

public class ClientTask extends TimerTask {

	private static Logger logger = LogManager.getLogger(ClientTask.class);
	
    @Override
    public void run() {
    	CommandConnector.getInstance();
    	StatisticsConnector.getInstance();
    	StatisticsManager.getInstance().sendStatitics();

//		Client client = Client.create();
//		WebResource webResource = null;
//		ClientResponse response = null;
//		
//		JSONObject object = null;
//
//		webResource = client.resource("http://127.0.0.1:8080/ktadapter/api/v1/kt/apns/1");
//		response = webResource.get(ClientResponse.class);
//		webResource = client.resource("http://127.0.0.1:8080/ktadapter/api/v1/kt/apns/1");
//		response = webResource.get(ClientResponse.class);
//		webResource = client.resource("http://127.0.0.1:8080/ktadapter/api/v1/kt/apns/1");
//		response = webResource.get(ClientResponse.class);
//		
//		if (response.getStatus() != 200) {
//			logger.error("Failed : HTTP error code : " + response.getStatus());
//			logger.info(object.toString());
//		}
//		
//		String output = response.getEntity(String.class);
//		logger.info(" ============RESULT============");
//		logger.info(output);
		
//        try {
//        	logger.debug("START");
//        	
//        	Context ctx = new InitialContext();
//        	Context environmentContext = (Context) ctx.lookup("java:/comp/env");
//        	String connectionURL = (String) environmentContext.lookup("USER_INFO_SEND_URL");
//        	logger.debug("connectionURL : " + connectionURL);
//        	
//    		Statement stmt = null;
//    		PreparedStatement delStmt = null;
//    		ResultSet rs = null;
//    		int count = 0;
//    		
//    		try {
//    			stmt = DBConnector.getInstance().getConnect().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_UPDATABLE);
//    			delStmt = DBConnector.getInstance().getConnect().prepareStatement(DEL_USER_INFO_QRY);
//    			
//    			rs = stmt.executeQuery(GET_USER_INFO_QRY);
//    			logger.debug(GET_USER_INFO_QRY);
//    			
//    			while(rs.next()){
//    				
//    				Client client = Client.create();
//    				WebResource webResource = null;
//    				ClientResponse response = null;
//    				
//    				JSONObject object = null;
//    				
//    				switch(rs.getInt("event_type")){
//    					//CREATE
//    					case CREATE_EVENT : 
//	    					
//    						object = new JSONObject();
//    						
//	    					object.put("mdn" , rs.getString(UserSecureInfo.MDN.getName()));
//	    					object.put("userName", rs.getString(UserSecureInfo.USER_NM.getName()));
//	    					object.put("companyCode", rs.getString(UserSecureInfo.CUST_ID.getName()));
//	    					object.put("departmentCode", rs.getString(UserSecureInfo.DEPT_CD.getName()));
//	    					object.put("imei", rs.getString(UserSecureInfo.IMEI.getName()));
//	    					object.put("newMdn", rs.getString(UserSecureInfo.NEW_MDN.getName()));
//	    					object.put("lostDeviceYn", rs.getString(UserSecureInfo.USST.getName()));
//	    					object.put("delYn", "N");
//	    					
//	    					logger.debug("[CREATE EVENT]");
//	    					logger.debug(object.toString());
//
//	    					webResource = client.resource(connectionURL);
//	    					response = webResource.type("application/json;charset=UTF-8").header("akey", "pl@sm#897&82160").header("channel", "plism").post(ClientResponse.class, object.toString());
//	    					break;
//	    					
//    					//UPDATE
//	    				case UPDATE_EVENT :
//	    					
//	    					object = new JSONObject();
//	    					
//	    					object.put("mdn" , rs.getString(UserSecureInfo.MDN.getName()));
//	    					object.put("userName", rs.getString(UserSecureInfo.USER_NM.getName()));
//	    					object.put("companyCode", rs.getString(UserSecureInfo.CUST_ID.getName()));
//	    					object.put("departmentCode", rs.getString(UserSecureInfo.DEPT_CD.getName()));
//	    					object.put("imei", rs.getString(UserSecureInfo.IMEI.getName()));
//	    					object.put("newMdn", rs.getString(UserSecureInfo.NEW_MDN.getName()));
//	    					object.put("lostDeviceYn", rs.getString(UserSecureInfo.USST.getName()));
//	    					object.put("delYn", "N");
//	    					
//	    					logger.debug("[UPDATE EVENT]");
//	    					logger.debug(object.toString());
//	    					
//	    					webResource = client.resource(connectionURL);
//	    					response = webResource.type("application/json;charset=UTF-8").header("akey", "pl@sm#897&82160").header("channel", "plism").put(ClientResponse.class, object.toString());
//	    					break;
//	    					
//    					//DELETE
//	    				case DELETE_EVENT:
//	    					
//	    					object = new JSONObject();
//	    					
//	    					object.put("mdn" , rs.getString(UserSecureInfo.MDN.getName()));
//	    					object.put("userName", rs.getString(UserSecureInfo.USER_NM.getName()));
//	    					object.put("companyCode", rs.getString(UserSecureInfo.CUST_ID.getName()));
//	    					object.put("departmentCode", rs.getString(UserSecureInfo.DEPT_CD.getName()));
//	    					object.put("imei", rs.getString(UserSecureInfo.IMEI.getName()));
//	    					object.put("newMdn", rs.getString(UserSecureInfo.NEW_MDN.getName()));
//	    					object.put("lostDeviceYn", rs.getString(UserSecureInfo.USST.getName()));
//	    					object.put("delYn", "Y");
//	    					
//	    					logger.debug("[DELETE EVENT]");
//	    					logger.debug(object.toString());
//	    					
//	    					webResource = client.resource(connectionURL);
//	    					response = webResource.type("application/json;charset=UTF-8").header("akey", "pl@sm#897&82160").header("channel", "plism").put(ClientResponse.class, object.toString());
//	    					break;
//    						//UNKNOWN
//	    					default : 
//	    						logger.info("Unknown Event Type[" +rs.getInt("event_type")+"]");
//	    						continue;
//    				};
//					
//    				if (response.getStatus() != 200) {
//    					logger.error("Failed : HTTP error code : " + response.getStatus());
//        				switch(rs.getInt("event_type")){
//	    					case CREATE_EVENT : 
//		    					logger.info("[CREATE EVENT]");
//	    					case UPDATE_EVENT : 
//		    					logger.info("[UPDATE EVENT]");
//	    					case DELETE_EVENT : 
//		    					logger.info("[DELETE EVENT]");
//	        				}
//    					logger.info(object.toString());
//    					continue;
//    				}
//    				
//    				String output = response.getEntity(String.class);
//    				logger.info("["+ count++ + "] ============RESULT============");
//    				logger.info(output);
//    				
//    				
//    				JSONObject result = new JSONObject(output.toString());
//    				
//    				if(!result.get("resultCode").equals("RPE99999"))
//    				{
//    					delStmt.setInt(1, rs.getInt(UserSecureInfo.EVENT_TYPE.getName()));
//    					delStmt.setString(2, rs.getString(UserSecureInfo.MDN.getName()));
//    					delStmt.setTimestamp(3, rs.getTimestamp(UserSecureInfo.REG_DT.getName()));
//    					delStmt.execute();
//    					
//	        			logger.debug(String.format(DEL_USER_INFO_QRY, rs.getString(UserSecureInfo.EVENT_TYPE.getName()), rs.getString(UserSecureInfo.MDN.getName()), rs.getString(UserSecureInfo.REG_DT.getName())));
////	        			logger.debug(DEL_USER_INFO_QRY + " WHERE event_type = '" + rs.getString(UserSecureInfo.EVENT_TYPE.getName()) + "' AND mdn = '" + rs.getString(UserSecureInfo.MDN.getName()) + "' AND reg_dt = '" + rs.getString(UserSecureInfo.REG_DT.getName())+"'" );
//    				} 
//    			}
//    			
//    			logger.debug("END");
//    			
//    		} catch (SQLException e) {
//    			e.printStackTrace();
//    			logger.error(e.getMessage());
//    		} finally {
//    			try {
//        			stmt.close();
//        			delStmt.close();
//        			rs.close();
//    			} catch (SQLException e) {
//    				e.printStackTrace();
//    				logger.error(e.getMessage());
//    			} finally {
//        			stmt = null;
//        			delStmt = null;
//        			rs = null;
//    			}
//    		}
//    		 
//        } catch (Exception e) {
//            e.printStackTrace();
//            logger.error(e.getMessage());
//        } 
    }
}
