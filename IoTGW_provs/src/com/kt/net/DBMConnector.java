package com.kt.net;

import java.io.*;
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
	}


	public boolean sendMessage(String command, List<String[]> params, int clientReqID, String imsi, String mdn, String ipAddress) {
		try {
			StringBuffer bodySB = new StringBuffer();
			for(int i = 0; i < params.size(); i++) {
//				if(i != 0) bodySB.append(",");
				if(i != 0) bodySB.append(";");
				bodySB.append(String.format("%s=%s",  params.get(i)[0],  params.get(i)[1]));
			}
			
			//bodyLen
			int bodyLen = bodySB.toString().length();
			
			//dataLen
			dataOut.write(toBytes(4+64+8+4+bodyLen+16+12+64));
//			dataOut.writeInt(64+8+4+bodyLen);
			//apiName
			dataOut.write(command.getBytes());
			for(int i = 0; i < 64 - command.length(); i++)
				dataOut.write("\0".getBytes());
			
			//seqNo
			dataOut.write((clientReqID+"").getBytes()); 
			for(int i = 0; i < 8 - (clientReqID+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//imsi
			dataOut.write((imsi+"").getBytes()); 
			for(int i = 0; i < 16 - (imsi+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//mdn
			dataOut.write((mdn+"").getBytes()); 
			for(int i = 0; i < 12 - (mdn+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			//ipAddress
			dataOut.write((ipAddress+"").getBytes()); 
			for(int i = 0; i < 64 - (ipAddress+"").length(); i++)
				dataOut.write("\0".getBytes());
			
			
			//rspCode
			dataOut.writeInt(0);
			//bodyLen
//			dataOut.writeInt(bodyLen);
			dataOut.write(toBytes(bodyLen));
			//body
			dataOut.write(bodySB.toString().getBytes());
			dataOut.flush();
			
			if(CommandManager.getInstance().isLogFlag()) {
			logger.info("=============================================");
			logger.info("PROVS -> RCB TCP SEND");
			logger.info("apiName : " + command);
			logger.info("tid : " + clientReqID);
			logger.info("imsi : " + imsi);
			logger.info("msisdn : " + mdn);
			logger.info("ipAddress : " + ipAddress);
			logger.info("bodyLen : " + bodyLen);
			logger.info("==============BODY==================");
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
				logger.info("apiName : " + pmt.getApiName());
				logger.info("tid : " + pmt.getSeqNo());
				logger.info("imsi : " + pmt.getImsi());
				logger.info("msisdn : " + pmt.getMdn());
				logger.info("ipAddress : " + pmt.getIpAddress());
				logger.info("resCode : " + pmt.getResCode());
				logger.info("bodyLen : " + pmt.getReservedMsgSize());
				logger.info("==============BODY==================");
				logger.info(pmt.getData());
				logger.info("====================================");
				logger.info("=============================================");
				}
				
				
				receiver.receiveMessage(pmt.getData(), pmt.getResCode(), Integer.parseInt(pmt.getSeqNo()));
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