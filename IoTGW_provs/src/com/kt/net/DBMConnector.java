package com.kt.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ProvifMsgType;

public class DBMConnector extends Connector {
	
	private static Logger logger = LogManager.getLogger(DBMConnector.class);
	
	private DataInputStream din;

	private static int timeOut;
	private boolean msgReadStarted;
	private int reservedMsgSize;
	private int totalReadSize, currentReadSize;
	private int[] msgSize;
	private static DBMConnector dbmConnector;
	
	public static int reqId = 1000;
	
	public static DBMConnector getInstance() {
		if(dbmConnector == null || !dbmConnector.isConnected()) {
			dbmConnector = new DBMConnector();
		}
		return dbmConnector;
	}
	
	public DBMConnector() {
		super(DBMManager.getInstance(), IoTProperty.getPropPath("iotgw_rcb_ipaddress"), Integer.parseInt(IoTProperty.getPropPath("iotgw_rcb_port")));
		msgSize = new int[4];
		for( int i=0; i<msgSize.length; i++ )
		    msgSize[i] = -1;
		
		timeOut = Integer.parseInt(IoTProperty.getPropPath("req_timeout"));
	}

	public int getTimeOut(){
		return DBMConnector.timeOut;
	}

	// SEND TO IUDR -- form parameter type 
//	public boolean sendMessage(String command, List<String[]> params, int clientReqID, String imsi, String mdn, String ipAddress) {
//		try {
//			StringBuffer bodySB = new StringBuffer();
//			for(int i = 0; i < params.size(); i++) {
////				if(i != 0) bodySB.append(",");
//				if(i != 0) bodySB.append(";");
//				bodySB.append(String.format("%s=%s",  params.get(i)[0],  params.get(i)[1]));
//			}
//			
//			//bodyLen
//			int bodyLen = bodySB.toString().length();
//			
//			//dataLen
//			dataOut.write(toBytes(4+64+8+4+bodyLen+16+12+64));
////			dataOut.writeInt(64+8+4+bodyLen);
//			//apiName
//			dataOut.write(command.getBytes());
//			for(int i = 0; i < 64 - command.length(); i++)
//				dataOut.write("\0".getBytes());
//			
//			//seqNo
//			dataOut.write((clientReqID+"").getBytes()); 
//			for(int i = 0; i < 8 - (clientReqID+"").length(); i++)
//				dataOut.write("\0".getBytes());
//			
//			//imsi
//			dataOut.write((imsi+"").getBytes()); 
//			for(int i = 0; i < 16 - (imsi+"").length(); i++)
//				dataOut.write("\0".getBytes());
//			
//			//mdn
//			dataOut.write((mdn+"").getBytes()); 
//			for(int i = 0; i < 12 - (mdn+"").length(); i++)
//				dataOut.write("\0".getBytes());
//			
//			//ipAddress
//			dataOut.write((ipAddress+"").getBytes()); 
//			for(int i = 0; i < 64 - (ipAddress+"").length(); i++)
//				dataOut.write("\0".getBytes());
//						
//			//rspCode
//			dataOut.writeInt(0);
//			//bodyLen
////			dataOut.writeInt(bodyLen);
//			dataOut.write(toBytes(bodyLen));
//			//body
//			dataOut.write(bodySB.toString().getBytes());
//			dataOut.flush();
//			
//			if(CommandManager.getInstance().isLogFlag()) {
//			logger.info("=============================================");
//			logger.info("PROVS -> RCB TCP SEND[IUDR]");
//			logger.info("apiName : " + command);
//			logger.info("tid : " + clientReqID);
//			logger.info("imsi : " + imsi);
//			logger.info("msisdn : " + mdn);
//			logger.info("ipAddress : " + ipAddress);
//			logger.info("bodyLen : " + bodyLen);
//			logger.info("==============BODY==================");
//			logger.info(bodySB.toString());
//			logger.info("====================================");
//			logger.info("=============================================");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//		
//		return true;
//	}
	
	
//	/* GenRestMsgType */
//	#define MAX_RESTMSG_LEN         32768-sizeof(int)
//	typedef struct {
//	    int     dataLen;
//	    char    data[MAX_RESTMSG_LEN];
//	} GenRestMsgType;

	public boolean sendMessage(String command, List<String[]> params, int clientReqID, ProvifMsgType pmt) {
		try {
			StringBuffer bodySB = new StringBuffer();
			for(int i = 0; i < params.size(); i++) {
				if(i != 0) bodySB.append(";");
				bodySB.append(String.format("%s=%s",  params.get(i)[0],  params.get(i)[1]));
			}
						
			int provLibHeadSize =  ProvifMsgType.getProvLibHeadSize();
			int oemHeadSize = ProvifMsgType.getOemHeadSize();
			
			//bodyLen
			int bodyLen = bodySB.toString().length();
			
			//dataLen
			dataOut.write(toBytes(provLibHeadSize+oemHeadSize + 4 + bodyLen));
			
			
			/* [1] provLibHeadMessage */
			// url 
			String urlStr = pmt.getUrl();
			if (urlStr == null)
				urlStr = "";
			dataOut.write(urlStr.getBytes());
			for(int i = 0; i < ProvifMsgType.getURL_LEN() - urlStr.length(); i++)
				dataOut.write("\0".getBytes());
			
			// appName
			String appName = IoTProperty.getPropPath("sys_name");
			dataOut.write(appName.getBytes());
			for(int i = 0; i < ProvifMsgType.getAPP_NAME_LEN() - appName.length(); i++)
				dataOut.write("\0".getBytes());

			//apiName
			dataOut.write(command.getBytes());
			for(int i = 0; i < ProvifMsgType.getAPI_NAME_LEN() - command.length(); i++)
				dataOut.write("\0".getBytes());

			//seqNo
			dataOut.write((clientReqID+"").getBytes()); 
			for(int i = 0; i < ProvifMsgType.getSEQ_NO_LEN() - (clientReqID+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//imsi
			String imsi = pmt.getImsi();
			if (imsi == null)
				imsi = "";
			dataOut.write((imsi+"").getBytes()); 
			for(int i = 0; i < ProvifMsgType.getIMSI_LEN() - (imsi+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//mdn
			String mdn = pmt.getMdn();
			if (mdn == null)
				mdn = "";
			dataOut.write((mdn+"").getBytes()); 
			for(int i = 0; i < ProvifMsgType.getMDN_LEN() - (mdn+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//ipAddress
			String ipAddress = pmt.getIpAddress();
			if (ipAddress == null)
				ipAddress = "";
			dataOut.write((ipAddress+"").getBytes()); 
			for(int i = 0; i < ProvifMsgType.getIP_ADDRESS_LEN() - (ipAddress+"").length(); i++)
				dataOut.write("\0".getBytes());
						
			//rspCode
			dataOut.writeInt(0);
			
			/* [2] OemHeadType */
			// o_sys_code 
			String osysCode = pmt.getOsysCode();
			if (osysCode == null)
				osysCode = "";
			dataOut.write(osysCode.getBytes());
			for(int i = 0; i < ProvifMsgType.getOEM_O_SYS_CD_LEN() - osysCode.length(); i++)
				dataOut.write("\0".getBytes());
			
			// t_sys_code 
			String tsysCode = pmt.getTsysCode();
			if (tsysCode == null)
				tsysCode = "";
			dataOut.write(tsysCode.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_T_SYS_CD_LEN() - tsysCode.length(); i++)
				dataOut.write("\0".getBytes());		
			
			// msgid
			String msgId = pmt.getMsgId();
			if (msgId == null)
				msgId = "";
			dataOut.write(msgId.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_MSG_ID_LEN() - msgId.length(); i++)
				dataOut.write("\0".getBytes());		

			// msgType
			String msgType = pmt.getMsgType();
			if (msgType == null)
				msgType = "";
			dataOut.write(msgType.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_MSG_TYPE_LEN() - msgType.length(); i++)
				dataOut.write("\0".getBytes());
			
			// resultCode
			String resultCode = pmt.getResultCode();
			if (resultCode == null)
				resultCode = "";			
			dataOut.write(resultCode.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_RESULT_CD_LEN() - resultCode.length(); i++)
				dataOut.write("\0".getBytes());

			// resultCode
			String result_dtlCode = pmt.getResultDtlCode();
			if (result_dtlCode == null)
				result_dtlCode = "";
			dataOut.write(result_dtlCode.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_RESULT_DTL_CD_LEN() - result_dtlCode.length(); i++)
				dataOut.write("\0".getBytes());
			
			// resultMsg
			String resultMsg = pmt.getResultMsg();
			if (resultMsg == null)
				resultMsg = "";
			dataOut.write(resultMsg.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_RESULT_MSG_LEN() - resultMsg.length(); i++)
				dataOut.write("\0".getBytes());
			
			//bodyLen
//			dataOut.writeInt(bodyLen);
			dataOut.write(toBytes(bodyLen));
			//body
			dataOut.write(bodySB.toString().getBytes());
			dataOut.flush();
			
			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVS -> RCB TCP SEND[IUDR]");
				logger.info("PROV_SIZE : " + provLibHeadSize );
				logger.info("OEM_SIZE : " + oemHeadSize );
				int totLen = provLibHeadSize+oemHeadSize + 4 + bodyLen;
				logger.info("TOT_SIZE : " + totLen);				
				logger.info("============ProvLibHeadType================");
				logger.info("URL : " + urlStr);
				logger.info("appName : " + appName);
				logger.info("apiName : " + command);
				logger.info("tid : " + clientReqID);
				logger.info("imsi : " + imsi);
				logger.info("msisdn : " + mdn);
				logger.info("ipAddress : " + ipAddress);
				logger.info("resCode : " + 0);
				logger.info("==============OemHeadType==================");
				logger.info("o_sys_cd : " + osysCode);
				logger.info("t_sys_cd : " + tsysCode);
				logger.info("msg_id : " + msgId);
				logger.info("msg_type : " + msgType);
				logger.info("result_cd : " + resultCode);
				logger.info("result_dtl_cd : " + result_dtlCode);
				logger.info("result_msg : " + resultMsg);
				logger.info("==============BODY==================");
				logger.info("bodyLen : " + bodyLen);
				logger.info(bodySB.toString());
				logger.info("====================================");
				logger.info("=============================================");
			}		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	
	// SEND TO BSSIOT -- json type 
	public boolean sendMessage(String command, String jsonBody, int clientReqID, String ipAddress, ProvifMsgType pmt) {
		try {
			StringBuffer bodySB = new StringBuffer();
			bodySB.append(jsonBody);
			
			int provLibHeadSize =  ProvifMsgType.getProvLibHeadSize();
			int oemHeadSize = ProvifMsgType.getOemHeadSize();

			//bodyLen
			int bodyLen = bodySB.toString().length();
			
			//dataLen
			dataOut.write(toBytes(provLibHeadSize+oemHeadSize + 4 + bodyLen));
			
			
			/* [1] provLibHeadMessage */
			// url 
			String urlStr = pmt.getUrl();
			dataOut.write(urlStr.getBytes());
			for(int i = 0; i < ProvifMsgType.getURL_LEN() - urlStr.length(); i++)
				dataOut.write("\0".getBytes());
			
			// appName
			String appName = IoTProperty.getPropPath("sys_name");
			dataOut.write(appName.getBytes());
			for(int i = 0; i < ProvifMsgType.getAPP_NAME_LEN() - appName.length(); i++)
				dataOut.write("\0".getBytes());

			//apiName
			dataOut.write(command.getBytes());
			for(int i = 0; i < ProvifMsgType.getAPI_NAME_LEN() - command.length(); i++)
				dataOut.write("\0".getBytes());

			//seqNo
			dataOut.write((clientReqID+"").getBytes()); 
			for(int i = 0; i < ProvifMsgType.getSEQ_NO_LEN() - (clientReqID+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//imsi
			String imsi = pmt.getImsi();
			dataOut.write((imsi+"").getBytes()); 
			for(int i = 0; i < ProvifMsgType.getIMSI_LEN() - (imsi+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//mdn
			String mdn = pmt.getMdn();
			dataOut.write((mdn+"").getBytes()); 
			for(int i = 0; i < ProvifMsgType.getMDN_LEN() - (mdn+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//ipAddress
			dataOut.write((ipAddress+"").getBytes()); 
			for(int i = 0; i < ProvifMsgType.getIP_ADDRESS_LEN() - (ipAddress+"").length(); i++)
				dataOut.write("\0".getBytes());
						
			//rspCode
			dataOut.writeInt(0);

			/* [2] OemHeadType */
			// o_sys_code 
			String osysCode = pmt.getOsysCode();
			if (osysCode == null)
				osysCode = "";			
			dataOut.write(osysCode.getBytes());
			for(int i = 0; i < ProvifMsgType.getOEM_O_SYS_CD_LEN() - osysCode.length(); i++)
				dataOut.write("\0".getBytes());
			
			// t_sys_code 
			String tsysCode = pmt.getTsysCode();
			if (tsysCode == null)
				tsysCode = "";			
			dataOut.write(tsysCode.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_T_SYS_CD_LEN() - tsysCode.length(); i++)
				dataOut.write("\0".getBytes());		
			
			// msgid
			String msgId = pmt.getMsgId();
			if (msgId == null)
				msgId = "";			
			dataOut.write(msgId.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_MSG_ID_LEN() - msgId.length(); i++)
				dataOut.write("\0".getBytes());		

			// msgType
			String msgType = pmt.getMsgType();
			if (msgType == null)
				msgType = "";
			dataOut.write(msgType.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_MSG_TYPE_LEN() - msgType.length(); i++)
				dataOut.write("\0".getBytes());
			
			// resultCode
			String resultCode = pmt.getResultCode();
			if (resultCode == null)
				resultCode = "";
			dataOut.write(resultCode.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_RESULT_CD_LEN() - resultCode.length(); i++)
				dataOut.write("\0".getBytes());

			// resultCode
			String result_dtlCode = pmt.getResultDtlCode();
			if (result_dtlCode == null)
				result_dtlCode = "";

			dataOut.write(result_dtlCode.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_RESULT_DTL_CD_LEN() - result_dtlCode.length(); i++)
				dataOut.write("\0".getBytes());
			
			// resultMsg
			String resultMsg = pmt.getResultMsg();
			if (resultMsg == null)
				resultMsg = "";
			
			dataOut.write(resultMsg.getBytes());
			for (int i = 0; i < ProvifMsgType.getOEM_RESULT_MSG_LEN() - resultMsg.length(); i++)
				dataOut.write("\0".getBytes());

			
			//bodyLen
//			dataOut.writeInt(bodyLen);
			dataOut.write(toBytes(bodyLen));
			//body
			dataOut.write(bodySB.toString().getBytes());
			dataOut.flush();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVS -> RCB TCP SEND[BSS-IOT]");
				logger.info("PROV_SIZE : " + provLibHeadSize );
				logger.info("OEM_SIZE : " + oemHeadSize );
				int totLen = provLibHeadSize+oemHeadSize + 4 + bodyLen;
				logger.info("TOT_SIZE : " + totLen);				
				logger.info("============ProvLibHeadType================");
				logger.info("URL : " + urlStr);
				logger.info("appName : " + appName);
				logger.info("apiName : " + command);
				logger.info("tid : " + clientReqID);
				logger.info("imsi : " + imsi);
				logger.info("msisdn : " + mdn);
				logger.info("ipAddress : " + ipAddress);
				logger.info("resCode : " + 0);
				logger.info("==============OemHeadType==================");
				logger.info("o_sys_cd : " + osysCode);
				logger.info("t_sys_cd : " + tsysCode);
				logger.info("msg_id : " + msgId);
				logger.info("msg_type : " + msgType);
				logger.info("result_cd : " + resultCode);
				logger.info("result_dtl_cd : " + result_dtlCode);
				logger.info("result_msg : " + resultMsg);
				logger.info("==============BODY==================");
				logger.info("bodyLen : " + bodyLen);
				logger.info(bodySB.toString());
				logger.info("====================================");
				logger.info("=============================================");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	
    private static byte[] toBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)(i>>24);
        result[1] = (byte)(i>>16);
        result[2] = (byte)(i>>8);
        result[3] = (byte)(i);
        return result;
    }   
	
	
	public boolean sendMessage(String req) {
		
		try {
			dataOut.write(req.getBytes());
			
			dataOut.flush();
		} catch (Exception e) {
			logger.error("Message Send Error Message - " + e );
			return false;
		}
		
		return true;
	}
	
	public static int byteToInt(byte[] bytes, ByteOrder order) {
		 
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
		buff.order(order);
 
		// buff사이즈는 4인 상태임
		// bytes를 put하면 position과 limit는 같은 위치가 됨.
		buff.put(bytes);
		// flip()가 실행 되면 position은 0에 위치 하게 됨.
		buff.flip();
 
		return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
	}

	protected void readMessage() throws IOException {
		if (msgReadStarted == false) {
//		    reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.LITTLE_ENDIAN);
		    reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.BIG_ENDIAN);
		    
			if (reservedMsgSize > BUFFER_SIZE) {
				logger.info(
					"(DBM) ReservedMsgSize is larger than "+ BUFFER_SIZE+ " : " + reservedMsgSize);
				throw new IOException("Larger than " + BUFFER_SIZE + " bytes");
			}

			msgReadStarted = true;
			totalReadSize = 0;
		}

		currentReadSize = dataIn.read(buffer, totalReadSize, reservedMsgSize - totalReadSize);
		
		if (totalReadSize + currentReadSize == reservedMsgSize) {
			din = new DataInputStream(new ByteArrayInputStream(buffer));
			try {
				
				ProvifMsgType pmt = new ProvifMsgType();
				pmt.setProvifMsgType(din);

				if(CommandManager.getInstance().isLogFlag()) {
					logger.info("=============================================");
					logger.info("RCB -> PROVS TCP RECEIVE");
					logger.info("============ProvLibHeadType================");
					logger.info("url : " + pmt.getUrl());
					logger.info("appName : " + pmt.getAppName());
					logger.info("apiName : " + pmt.getApiName());
					logger.info("tid : " + pmt.getSeqNo());
					logger.info("imsi : " + pmt.getImsi());
					logger.info("msisdn : " + pmt.getMdn());
					logger.info("ipAddress : " + pmt.getIpAddress());
					logger.info("resCode : " + pmt.getResCode());
					logger.info("bodyLen : " + pmt.getReservedMsgSize());
					logger.info("==============OemHeadType==================");
					logger.info("o_sys_cd : " + pmt.getOsysCode());
					logger.info("t_sys_cd : " + pmt.getTsysCode());
					logger.info("msg_id : " + pmt.getMsgId());
					logger.info("msg_type : " + pmt.getMsgType());
					logger.info("result_cd : " + pmt.getResultCode());
					logger.info("result_dtl_cd : " + pmt.getResultDtlCode());
					logger.info("result_msg : " + pmt.getResultMsg());
					logger.info("==============BODY==================");
					logger.info(pmt.getData());
					logger.info("====================================");
					logger.info("=============================================");
				}
				receiver.receiveMessage(pmt.getData(), pmt.getResCode(), Integer.parseInt(pmt.getSeqNo()), pmt );
			} catch (Exception e) {
				e.printStackTrace();
			}

			msgReadStarted = false;
		} else if (totalReadSize + currentReadSize > reservedMsgSize) {
			throw new IOException("It is never occured, but...");
		} else {
			totalReadSize += currentReadSize;
		}
	}
}