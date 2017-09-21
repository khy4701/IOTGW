package com.kt.net;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.StatisticsModel;

public class StatisticsConnector extends Connector2 {

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


	public boolean sendMessage(String command, List<String[]> params, int clientReqID) {
		try {
			StringBuffer bodySB = new StringBuffer();
			for(int i = 0; i < params.size(); i++) {
				if(i != 0) bodySB.append(",");
				bodySB.append(String.format("%s=%s",  params.get(i)[0],  params.get(i)[1]));
			}

			//bodyLen
			int bodyLen = bodySB.toString().length();

			//dataLen
			//			dataOut.write(toBytes(4+64+8+4+bodyLen));
			dataOut.write(toBytes(bodyLen));
			//			dataOut.writeInt(64+8+4+bodyLen);
			//apiName
			dataOut.write(command.getBytes());
			for(int i = 0; i < 64 - command.length(); i++)
				dataOut.write("\0".getBytes());

			//seqNo
			dataOut.write((clientReqID+"").getBytes()); 
			for(int i = 0; i < 8 - (clientReqID+"").length(); i++)
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
				logger.info("JAVA -> PROVIB TCP SEND");
				logger.info("apiName : " + command);
				logger.info("tid : " + clientReqID);
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


	public synchronized boolean sendMessage(ConcurrentHashMap<String, StatisticsModel> statistics) {
		try {
			int count = statistics.size();
			int bodyLen = 4 + (statistics.size() * 160);

			if(CommandManager.getInstance().isLogFlag()) {
				for(Entry<String, StatisticsModel> entry : statistics.entrySet()) {
					logger.info(entry.getValue().getIpAddress() + " : " + entry.getValue().getApiName() + " : " + entry.getValue().getTotal() + " : " + entry.getValue().getSucc() + " : " + entry.getValue().getFail()+ " : " + entry.getValue().getError400() + " : " + entry.getValue().getError409() + " : " + entry.getValue().getError410()+ " : " + entry.getValue().getError500()+ " : " + entry.getValue().getError501()); 
				}
			}

			if(count == 0) {
				bodyLen = 4;
				//bodyLen
				dataOut.writeInt(bodyLen);
				//Statistics Count
				dataOut.writeInt(104);
				//Statistics Count
				for(int i = 0; i < 4; i++)
					dataOut.write("0".getBytes());

				dataOut.writeInt(byteToInt(toBytes((count)), ByteOrder.LITTLE_ENDIAN));

				dataOut.flush();
			} else {
				for(int loopCnt = 0; loopCnt < count/50 ;loopCnt++) {
					List<String> deleteList = new ArrayList<String>();
					bodyLen = 4 + (50 * 160);
					//bodyLen
					dataOut.writeInt(bodyLen);
					//Statistics Count
					dataOut.writeInt(104);
					//Statistics Count
					for(int i = 0; i < 4; i++)
						dataOut.write("0".getBytes());

					dataOut.writeInt(byteToInt(toBytes((50)), ByteOrder.LITTLE_ENDIAN));
					int idx = 0;
					for(Entry<String, StatisticsModel> entry : statistics.entrySet()) {
						if(idx >= 50) continue;

						//ipAddress
						dataOut.write(entry.getValue().getIpAddress().getBytes()); 
						for(int i = 0; i < 64 - entry.getValue().getIpAddress().length(); i++)
							dataOut.write("\0".getBytes());

						//apiName
						dataOut.write(entry.getValue().getApiName().getBytes()); 
						for(int i = 0; i < 64 - entry.getValue().getApiName().length(); i++)
							dataOut.write("\0".getBytes());


						//Total
						dataOut.writeInt(entry.getValue().getTotal());
						//Succ
						dataOut.writeInt(entry.getValue().getSucc());
						//Fail
						dataOut.writeInt(entry.getValue().getFail());
						//400 error
						dataOut.writeInt(entry.getValue().getError400());
						//409 error
						dataOut.writeInt(entry.getValue().getError409());
//						410 error
						dataOut.writeInt(entry.getValue().getError410());
						//500 error
						dataOut.writeInt(entry.getValue().getError500());
						//501 error
						dataOut.writeInt(entry.getValue().getError501());

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
				bodyLen = 4 + ((count%50) * 160);
				//bodyLen
				dataOut.writeInt(bodyLen);

				dataOut.writeInt(104);
				//Statistics Count
				for(int i = 0; i < 4; i++)
					dataOut.write("0".getBytes());

				//				dataOut.writeInt(count);
				dataOut.writeInt(byteToInt(toBytes((count%50)), ByteOrder.LITTLE_ENDIAN));

				for(Entry<String, StatisticsModel> entry : statistics.entrySet()) {
					//ipAddress
					dataOut.write(entry.getValue().getIpAddress().getBytes()); 
					for(int i = 0; i < 64 - entry.getValue().getIpAddress().length(); i++)
						dataOut.write("\0".getBytes());

					//apiName
					dataOut.write(entry.getValue().getApiName().getBytes()); 
					for(int i = 0; i < 64 - entry.getValue().getApiName().length(); i++)
						dataOut.write("\0".getBytes());

					//Total
					dataOut.writeInt(entry.getValue().getTotal());
					//Succ
					dataOut.writeInt(entry.getValue().getSucc());
					//Fail
					dataOut.writeInt(entry.getValue().getFail());
					//400 error
					dataOut.writeInt(entry.getValue().getError400());
					//409 error
					dataOut.writeInt(entry.getValue().getError409());
//					410 error
					dataOut.writeInt(entry.getValue().getError410());
					//500 error
					dataOut.writeInt(entry.getValue().getError500());
					//501 error
					dataOut.writeInt(entry.getValue().getError501());
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