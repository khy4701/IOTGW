package com.kt.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.StatisticsModel;

//typedef struct {
//int     bodyLen;
//int     mapType;    /* jumaeng 2011.04.11 for rmi */
//int     mtype;   /* 시스템에 따라 long이 4byte 또는 8byte로 다르다. */
//} SockLibHeadType;
//
//typedef struct {
//SockLibHeadType head;
//char            body[SOCKLIB_MAX_BODY_LEN];
//} SockLibMsgType;

//typedef struct {
//    unsigned int    num;
//    char            appName[32];
//    STM_CtrlCntStatMsg info[MAX_JASPER_OP*MAX_IPADDR_CNT];
//} STM_CtrlCntStatMsgType;

//typedef struct {
//    char            ipAddress[64];
//    int             port;
//    char            apiName[64];
//    int             statCnt;
//    int             stat[32];   // [0] attempt, [1] success, [2] fail, [3~19] err



//typedef struct {
//    char            ipAddress[64];
//    int             port;
//    char            apiName[64];
//    int             attempt;
//    int             success;
//    int             fail;
//    int             err400;
//    int             err403;
//    int             err409;
//    int             err410;
//    int             err500;
//    int             err501;
//    int             err503;
//} STM_CtrlCntStatMsg;


public class StatisticsConnector extends Connector {

	private static Logger logger = LogManager.getLogger(StatisticsConnector.class);

	private DataInputStream din;

	private boolean msgReadStarted;
	private int reservedMsgSize;
	private int totalReadSize, currentReadSize;
	private int[] msgSize;
	private static StatisticsConnector dbmConnector;

	public static int reqId = 1000;

	public static StatisticsConnector getInstance() {
		if(dbmConnector == null || !dbmConnector.isConnected()) {
			dbmConnector = new StatisticsConnector();
		}
		return dbmConnector;
	}

	public StatisticsConnector() {
		super(StatisticsManager.getInstance(), IoTProperty.getPropPath("iotgw_stat_ipaddress"), Integer.parseInt(IoTProperty.getPropPath("iotgw_stat_port")));
		msgSize = new int[4];
		for( int i=0; i<msgSize.length; i++ )
			msgSize[i] = -1;
	}


//	public boolean sendMessage(String command, List<String[]> params, int clientReqID) {
//		try {
//			StringBuffer bodySB = new StringBuffer();
//			for(int i = 0; i < params.size(); i++) {
//				if(i != 0) bodySB.append(",");
//				bodySB.append(String.format("%s=%s",  params.get(i)[0],  params.get(i)[1]));
//			}
//
//			//bodyLen
//			int bodyLen = bodySB.toString().length();
//
//			//dataLen
//			//			dataOut.write(toBytes(4+64+8+4+bodyLen));
//			dataOut.write(toBytes(bodyLen));
//			//			dataOut.writeInt(64+8+4+bodyLen);
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
//			//rspCode
//			dataOut.writeInt(0);
//			//bodyLen
//			//			dataOut.writeInt(bodyLen);
//			dataOut.write(toBytes(bodyLen));
//			//body
//			dataOut.write(bodySB.toString().getBytes());
//			dataOut.flush();
//
//			logger.info("=============================================");
//			logger.info("JAVA -> PROVIB TCP SEND");
//			logger.info("apiName : " + command);
//			logger.info("tid : " + clientReqID);
//			logger.info("bodyLen : " + bodyLen);
//			logger.info("==============BODY==================");
//			logger.info(bodySB.toString());
//			logger.info("====================================");
//			logger.info("=============================================");
//		} catch (Exception e) {
//			e.printStackTrace();
//			return false;
//		}
//
//		return true;
//	}

	private static byte[] toBytes(int i) {
		byte[] result = new byte[4];
		result[0] = (byte)(i>>24);
		result[1] = (byte)(i>>16);
		result[2] = (byte)(i>>8);
		result[3] = (byte)(i);
		return result;
	}   


	public synchronized boolean sendMessage(ConcurrentHashMap<String, StatisticsModel> statistics, int mapType) {

		try {
			int count = statistics.size();
			int headerSize = 4 + 32;			        // header
			int indi_bodyLen = 8 + 64 + 4 + 64 + 4 + 32*4;  // body(STM_ProvStatMsg)
			int bodyLen = headerSize + (statistics.size() * indi_bodyLen);
			String appName = IoTProperty.getPropPath("sys_name");
			
			if(CommandManager.getInstance().isLogFlag()) {
				for(Entry<String, StatisticsModel> entry : statistics.entrySet()) {
					logger.info("CNT[" + count + "] " +entry.getValue().getOsysCode() + " : " + entry.getValue().getIpAddress() + " : " + entry.getValue().getApiName() + " : " + entry.getValue().getTotal() + " : " + entry.getValue().getSucc() + " : " + entry.getValue().getFail()
							+ " : " + entry.getValue().getError400() + " : " + entry.getValue().getError403()+ " : " + entry.getValue().getError409() + " : " + entry.getValue().getError410()
							+ " : " + entry.getValue().getError500() + " : " + entry.getValue().getError501()+ " : " + entry.getValue().getError503() + " : " + entry.getValue().getErrorEtc());
				}
			}
								
			if(count == 0) {
				bodyLen = headerSize;
				//bodyLen
				dataOut.writeInt(bodyLen);
				//mapType
				dataOut.writeInt(mapType);
				//mtype
				for(int i = 0; i < 4; i++)
					dataOut.write("0".getBytes());

				// num
				dataOut.writeInt(byteToInt(toBytes((count)), ByteOrder.LITTLE_ENDIAN));
				
				// appName
				
				dataOut.write(appName.getBytes());
				for(int i = 0; i < 32 - appName.length(); i++)
					dataOut.write("\0".getBytes());

				dataOut.flush();
			} else {
				
				for(int loopCnt = 0; loopCnt < count/50 ;loopCnt++) {
					List<String> deleteList = new ArrayList<String>();
					bodyLen = headerSize + (50 * indi_bodyLen);
					//bodyLen
					dataOut.writeInt(bodyLen);
					//mapType
					dataOut.writeInt(mapType);
					//mtype					
					for(int i = 0; i < 4; i++)
						dataOut.write("0".getBytes());
					
					// num(4)
					dataOut.writeInt(byteToInt(toBytes((50)), ByteOrder.LITTLE_ENDIAN));
					
					// appName(32)
					dataOut.write(appName.getBytes());
					for(int i = 0; i < 32 - appName.length(); i++)
						dataOut.write("\0".getBytes());
					
					int idx = 0;
					for(Entry<String, StatisticsModel> entry : statistics.entrySet()) {
						if(idx >= 50) continue;
						// osysCode 
						dataOut.write(entry.getValue().getOsysCode().getBytes()); 
						for(int i = 0; i < 8 - entry.getValue().getOsysCode().length(); i++)
							dataOut.write("\0".getBytes());
						
						//ipAddress(64)
						dataOut.write(entry.getValue().getIpAddress().getBytes()); 
						for(int i = 0; i < 64 - entry.getValue().getIpAddress().length(); i++)
							dataOut.write("\0".getBytes());

						//port(4)
						dataOut.writeInt(entry.getValue().getPort());
						
						//apiName(64)
						dataOut.write(entry.getValue().getApiName().getBytes()); 
						for(int i = 0; i < 64 - entry.getValue().getApiName().length(); i++)
							dataOut.write("\0".getBytes());

						
						// statCnt(4) ( total, success, fail = 3 )
						// errcode( 400, 403, 409, 410, 500, 501, 503, ETC = 8 )
						dataOut.writeInt(3+8);
					
						//Total(128)
						int statCnt = 0;
						dataOut.writeInt(entry.getValue().getTotal());
						statCnt++;
						//Succ
						dataOut.writeInt(entry.getValue().getSucc());
						statCnt++;
						//Fail
						dataOut.writeInt(entry.getValue().getFail());
						statCnt++;
						//400 error
						dataOut.writeInt(entry.getValue().getError400());
						statCnt++;
						//403 error
						dataOut.writeInt(entry.getValue().getError403());
						statCnt++;
						//409 error
						dataOut.writeInt(entry.getValue().getError409());
						statCnt++;
						//410 error
						dataOut.writeInt(entry.getValue().getError410());
						statCnt++;
						//500 error
						dataOut.writeInt(entry.getValue().getError500());
						statCnt++;
						//501 error
						dataOut.writeInt(entry.getValue().getError501());
						statCnt++;
						//503 error
						dataOut.writeInt(entry.getValue().getError503());
						statCnt++;
						//ETC error
						dataOut.writeInt(entry.getValue().getErrorEtc());
						statCnt++;					

						
						for (int i= 0 ; i < 32-statCnt; i++)
							dataOut.writeInt(0);				

						deleteList.add(entry.getKey());
						idx++;
					}

					for(String delKey : deleteList){
						statistics.remove(delKey);
					}

					dataOut.flush();
				}
			}

			if(count%50 > 0) {
				
				bodyLen = headerSize + ((count%50) * indi_bodyLen);
				//bodyLen
				dataOut.writeInt(bodyLen);
				//mapType
				dataOut.writeInt(mapType);
				//mtype
				for(int i = 0; i < 4; i++)
					dataOut.write("0".getBytes());

				// num
				dataOut.writeInt(byteToInt(toBytes((count%50)), ByteOrder.LITTLE_ENDIAN));
				
				// appName
				dataOut.write(appName.getBytes());
				for(int i = 0; i < 32 - appName.length(); i++)
					dataOut.write("\0".getBytes());


				for(Entry<String, StatisticsModel> entry : statistics.entrySet()) {
					// osysCode 
					dataOut.write(entry.getValue().getOsysCode().getBytes()); 
					for(int i = 0; i < 8 - entry.getValue().getOsysCode().length(); i++)
						dataOut.write("\0".getBytes());
					
					//ipAddress
					dataOut.write(entry.getValue().getIpAddress().getBytes()); 
					for(int i = 0; i < 64 - entry.getValue().getIpAddress().length(); i++)
						dataOut.write("\0".getBytes());

					//port
					dataOut.writeInt(entry.getValue().getPort());
					
					//apiName
					dataOut.write(entry.getValue().getApiName().getBytes()); 
					for(int i = 0; i < 64 - entry.getValue().getApiName().length(); i++)
						dataOut.write("\0".getBytes());
					
					// statCnt ( total, success, fail = 3 )
					// errcode( 400, 403, 409, 410, 500, 501, 503, etc = 8 )
					dataOut.writeInt(3+8);

					//Total
					int statCnt = 0;
					dataOut.writeInt(entry.getValue().getTotal());
					statCnt++;
					//Succ
					dataOut.writeInt(entry.getValue().getSucc());
					statCnt++;
					//Fail
					dataOut.writeInt(entry.getValue().getFail());
					statCnt++;
					//400 error
					dataOut.writeInt(entry.getValue().getError400());
					statCnt++;
					//403 error
					dataOut.writeInt(entry.getValue().getError403());
					statCnt++;
					//409 error
					dataOut.writeInt(entry.getValue().getError409());
					statCnt++;
					//410 error
					dataOut.writeInt(entry.getValue().getError410());
					statCnt++;
					//500 error
					dataOut.writeInt(entry.getValue().getError500());
					statCnt++;
					//501 error
					dataOut.writeInt(entry.getValue().getError501());
					statCnt++;
					//503 error
					dataOut.writeInt(entry.getValue().getError503());
					statCnt++;					
					//ETC error
					dataOut.writeInt(entry.getValue().getErrorEtc());
					statCnt++;					
					
					for (int i= 0 ; i < 32-statCnt; i++)
						dataOut.writeInt(0);				
					
					logger.info("appName: " + appName);
					logger.info("osysCode: " + entry.getValue().getOsysCode());
					logger.info("ipAddress: " + entry.getValue().getIpAddress());
					logger.info("port: " + entry.getValue().getPort());
					logger.info("apiName: " + entry.getValue().getApiName());
					logger.info("total: " + entry.getValue().getTotal());
					logger.info("success: " + entry.getValue().getSucc());
					logger.info("fail: " + entry.getValue().getFail());
					

				}

				dataOut.flush();
			}
		} catch (Exception e) {
			logger.error("Message Send Error Message - " + e );
			e.printStackTrace();
			return false;
		}

		return true;
	}

	public static int byteToInt(byte[] bytes, ByteOrder order) {

		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
		buff.order(order);

		buff.put(bytes);
		buff.flip();

		return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
	}

	protected void readMessage() throws IOException {
		if (msgReadStarted == false) {
			reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.BIG_ENDIAN);
			reservedMsgSize = 18;
			//		    reservedMsgSize = 14;
			//		    reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.BIG_ENDIAN);

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
				receiver.receiveMessage("", 0, 0, null);
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