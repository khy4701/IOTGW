package com.kt.net;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.ProvifMsgType;
import com.kt.restful.model.StatisticsModel;

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

	//	public synchronized static void sendCommand(String apiName, String seqNo, String imsi, String mdn, String ipAddress, String result, int resCode) {	
	@SuppressWarnings("static-access")
	public boolean sendMessage(String apiName, String seqNo, String imsi, String mdn, String ipAddress, String result, int resCode) {
		try {
			//bodyLen
			int bodyLen = result.length();

			//dataLen
			dataOut.write(toBytes(4+64+8+4+bodyLen+16+12+64));
			//			dataOut.writeInt(64+8+4+bodyLen);
			//apiName
			dataOut.write(apiName.getBytes());
			for(int i = 0; i < 64 - apiName.length(); i++)
				dataOut.write("\0".getBytes());

			//seqNo
			dataOut.write((seqNo+"").getBytes()); 
			for(int i = 0; i < 8 - (seqNo+"").length(); i++)
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
			dataOut.writeInt(resCode);
			//bodyLen
			//			dataOut.writeInt(bodyLen);
			dataOut.write(toBytes(bodyLen));
			//body
			dataOut.write(result.getBytes());
			dataOut.flush();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("=============================================");
				logger.info("PROVC -> RCB TCP SEND");
				logger.info("apiName : " + apiName);
				logger.info("tid : " + seqNo);
				logger.info("imsi : " + imsi);
				logger.info("msisdn : " + mdn);
				logger.info("ipAddress : " + ipAddress);
				logger.info("resCode : " + resCode);
				logger.info("bodyLen : " + bodyLen);
				logger.info("==============BODY==================");
				logger.info(result);
				logger.info("====================================");
				logger.info("=============================================");
			}
			
//			synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
//				String key = ipAddress.trim() + apiName.trim();
//				if(resCode == 200) {
//					if(StatisticsManager.getInstance().getStatisticsHash().containsKey(key)) {
//						StatisticsManager.getInstance().getStatisticsHash().get(key).plusSucc();
//					} else {
//						StatisticsManager.getInstance().getStatisticsHash().put(key, 
//								new StatisticsModel(apiName.trim(), ipAddress.trim(), 0, 1, 0));
//					}
//				} else {
//					if(StatisticsManager.getInstance().getStatisticsHash().containsKey(key)) {
//						if(resCode != -1) {
//							StatisticsManager.getInstance().getStatisticsHash().get(key).plusFail();
//
//							if (resCode == 412)
//								StatisticsManager.getInstance().getStatisticsHash().get(key).plusError412();
//							else if (resCode == 500)
//								StatisticsManager.getInstance().getStatisticsHash().get(key).plusError500();
//							else
//								StatisticsManager.getInstance().getStatisticsHash().get(key).plusError400();
//						}
//					} else {
//						if(resCode != -1) {
//							StatisticsManager.getInstance().getStatisticsHash().put(key, 
//									new StatisticsModel(apiName.trim(), ipAddress.trim(), 0, 0, 1));
//
//							if (resCode == 412)
//								StatisticsManager.getInstance().getStatisticsHash().get(key).plusError412();
//							else if (resCode == 500)
//								StatisticsManager.getInstance().getStatisticsHash().get(key).plusError500();
//							else
//								StatisticsManager.getInstance().getStatisticsHash().get(key).plusError400();
//						}
//					}
//				}
//			}
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

	@SuppressWarnings("static-access")
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

//				synchronized (StatisticsManager.getInstance().getStatisticsHash()) {
//					String key = pmt.getIpAddress().trim() + pmt.getApiName().trim();
//					
//					if(StatisticsManager.getInstance().getStatisticsHash().containsKey(key)) {
//						StatisticsManager.getInstance().getStatisticsHash().get(key).plusTotal();
//					} else {
//						StatisticsManager.getInstance().getStatisticsHash().put(key, 
//								new StatisticsModel(pmt.getApiName().trim(), pmt.getIpAddress().trim(), 1, 0, 0));
//					}
//				}
				
				if(CommandManager.getInstance().isLogFlag()) {
					logger.info("=============================================");
					logger.info("RCB -> PROVC TCP RECEIVE");
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


				receiver.receiveMessage(pmt.getApiName(), pmt.getSeqNo(), pmt.getResCode(), pmt.getImsi(), pmt.getMdn(), pmt.getIpAddress(), pmt.getData());

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

	@Override
	protected boolean sendMessage(String result) {
		// TODO Auto-generated method stub
		return false;
	}
}