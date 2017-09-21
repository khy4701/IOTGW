package com.kt.net;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.IUDRHeartbeatCheckModel;
import com.kt.util.Util;

public class CommandConnector extends Connector3 {

	private static Logger logger = LogManager.getLogger(CommandConnector.class);

	private DataInputStream din;

	private boolean msgReadStarted;
	private int reservedMsgSize;
	private int totalReadSize, currentReadSize;
	private int[] msgSize;
	private static CommandConnector dbmConnector;
	private int mapType = 0;

	public static int reqId = 1000;
	
	public List<IUDRHeartbeatCheckModel> iudrConnCheckList = new ArrayList<IUDRHeartbeatCheckModel>(); 
	
	public List<IUDRHeartbeatCheckModel> getIudrConnCheckList() {
		return iudrConnCheckList;
	}

	public void setIudrConnCheckList(List<IUDRHeartbeatCheckModel> iudrConnCheckList) {
		this.iudrConnCheckList = iudrConnCheckList;
	}

	public static CommandConnector getInstance() {
		if(dbmConnector == null || !dbmConnector.isConnected()) {
			dbmConnector = new CommandConnector();
		}
		return dbmConnector;
	}

	public CommandConnector() {
		super(CommandManager.getInstance(), IoTProperty.getPropPath("iotgw_command_ipaddress"), Integer.parseInt(IoTProperty.getPropPath("iotgw_command_port")));
		//		super(CommandManager.getInstance(), "192.168.70.237", 24834);
		msgSize = new int[4];
		for( int i=0; i<msgSize.length; i++ )
			msgSize[i] = -1;

		//		if (isConnected()) {
		//			try {
		//				dataOut.writeInt("INIT_COMPLETE".getBytes().length);	// length
		//				dataOut.writeInt(0);								                    // mapType					
		//				dataOut.writeInt(0);								                    // seqflag + seqNo
		//				dataOut.writeBytes("INIT_COMPLETE");					         // body
		//				dataOut.flush();
		//				
		//				
		//				System.out.println("XXXX");
		//			} catch (IOException e) {
		//			    e.printStackTrace();
		//			}			
		//		}
	}

	public boolean sendIUDRConnCheckMessage() {
		try {
			synchronized(iudrConnCheckList) {
				int bodyLen = 4 + (iudrConnCheckList.size() * 68);
				int mapType = 100;

				dataOut.writeInt(byteToInt(toBytes(bodyLen), ByteOrder.BIG_ENDIAN));
				dataOut.writeInt(mapType);
				dataOut.writeInt(0);

				//count
				dataOut.writeInt(iudrConnCheckList.size());

				for(IUDRHeartbeatCheckModel model : iudrConnCheckList) {
					if(CommandManager.getInstance().isLogFlag()) {
						logger.info("IUDR IP : " + model.getIpAddress() + " STATUS : " + model.getStatus());
					}
					//ipAddress
					dataOut.write(model.getIpAddress().getBytes());
					for(int i = 0; i < 64 - model.getIpAddress().length(); i++)
						dataOut.write("\0".getBytes());
					//status
					dataOut.writeInt(model.getStatus());

				}

				dataOut.flush();
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	protected boolean sendMessage(String command, String imsi, String ipAddress, String sendMsg, int jobNo) {
		try {

			int bodyLen = 32 + 16 + 64 + 4 + sendMsg.length();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("===============================================");
				logger.info("PROVS -> MMIB");
				logger.info("bodyLen : " + bodyLen);
				logger.info("command : " + command);
				logger.info("imsi : " + imsi);
				logger.info("ipAddress : " + ipAddress);
				logger.info("jobNo : " + jobNo);
				logger.info("sendMsg ");
				logger.info(sendMsg);
				logger.info("===============================================");
			}

			int mapType = 4;
			if (command.startsWith("TRACE_")) {
				mapType = 17;
				command = command.replace("TRACE_", ""); 
			}

			dataOut.writeInt(byteToInt(toBytes(bodyLen), ByteOrder.BIG_ENDIAN));
			//			dataOut.writeInt(bodyLen);
			//Statistics Count
			dataOut.writeInt(mapType);
			dataOut.writeInt(0);
			//			for(int i = 0; i < 4; i++)
			//				dataOut.write("0".getBytes());

			//command
			dataOut.write(command.getBytes());
			for(int i = 0; i < 32 - command.length(); i++)
				dataOut.write("\0".getBytes());

			//imsi
			dataOut.write(imsi.getBytes());
			for(int i = 0; i < 16 - imsi.length(); i++)
				dataOut.write("\0".getBytes());

			//ipAddress
			dataOut.write(ipAddress.getBytes());
			for(int i = 0; i < 64 - ipAddress.length(); i++)
				dataOut.write("\0".getBytes());

			dataOut.writeInt(jobNo);

			//sendMsg
			dataOut.write(sendMsg.getBytes());

			dataOut.flush();
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

		buff.put(bytes);
		buff.flip();

		return buff.getInt(); 
	}

	protected void readMessage() throws IOException {
		if (msgReadStarted == false) {
			//			reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.BIG_ENDIAN);
			//			reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.LITTLE_ENDIAN);
			reservedMsgSize = dataIn.readInt();
			//			reservedMsgSize = reservedMsgSize - 4;
			if (reservedMsgSize > BUFFER_SIZE) {
				logger.info(
						"(DBM) ReservedMsgSize is larger than "+ BUFFER_SIZE+ " : " + reservedMsgSize);
				//				throw new IOException("Larger than " + BUFFER_SIZE + " bytes");
				msgReadStarted = false;
				totalReadSize = 0;

				int avail = dataIn.available();
				dataIn.skipBytes(avail);
				return;
			}

			mapType = dataIn.readInt();
			dataIn.skipBytes(4);
//			dataIn.skipBytes(8);

			msgReadStarted = true;
			totalReadSize = 0;
		}

		currentReadSize = dataIn.read(buffer, totalReadSize, reservedMsgSize - totalReadSize);
		if (totalReadSize + currentReadSize == reservedMsgSize) {

			din = new DataInputStream(new ByteArrayInputStream(buffer));
			try {
				
				//Command Ã³¸® mapType 46 
				if(mapType == 46) {
					byte[] commandBuffer = new byte[32];
					byte[] imsiBuffer = new byte[16];
					byte[] ipAddressBuffer = new byte[64];

					din.read(commandBuffer, 0, commandBuffer.length);
					String command = Util.nullTrim(new String(commandBuffer));

					din.read(imsiBuffer, 0, imsiBuffer.length);
					String imsi = Util.nullTrim(new String(imsiBuffer));

					din.read(ipAddressBuffer, 0, ipAddressBuffer.length);
					String ipAddress = Util.nullTrim(new String(ipAddressBuffer));

					int jobNo = din.readInt();

					if(CommandManager.getInstance().isLogFlag()) {
						logger.info("===============================================");
						logger.info("MMIB -> PROVS");
						logger.info("bodyLen : " + reservedMsgSize);
						logger.info("command : " + command);
						logger.info("imsi : " + imsi);
						logger.info("ipAddress : " + ipAddress);
						logger.info("jobNo : " + jobNo);
						logger.info("===============================================");
					}

					receiver.receiveMessage(command, imsi, ipAddress, jobNo);
					
				//IUDR Connection Check mapType 100
				} else if (mapType == 100) {
					
					synchronized (iudrConnCheckList) {
						int count = din.readInt();
						iudrConnCheckList.clear();
						
						for(int i = 0; i < count; i++) {
							byte[] ipAddressBuffer = new byte[64];
							din.read(ipAddressBuffer, 0, ipAddressBuffer.length);
							String ipAddress = Util.nullTrim(new String(ipAddressBuffer));
							din.skipBytes(4);
							
							iudrConnCheckList.add(new IUDRHeartbeatCheckModel(ipAddress, 0));
						}
					}
				}
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
		return false;
	}
}