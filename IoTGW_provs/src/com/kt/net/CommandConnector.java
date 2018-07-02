package com.kt.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.MMCMsgType;

public class CommandConnector extends Connector2 {

	private static Logger logger = LogManager.getLogger(CommandConnector.class);

	private DataInputStream din;

	private boolean msgReadStarted;
	private int reservedMsgSize;
	private int totalReadSize, currentReadSize;
	private int[] msgSize;
	private static CommandConnector dbmConnector;
	
	private int mapType = 0;

	public static int reqId = 1000;

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

	protected boolean sendMessage() {
		try {
			dataOut.writeInt(0);
			dataOut.flush();
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

//	typedef struct {
//	    int     bodyLen;
//	    int     mapType;    
//	    int     mtype;   
//	} SockLibHeadType;  
//
//	#define SOCKLIB_HEAD_LEN        sizeof(SockLibHeadType)
//	#define SOCKLIB_MAX_BODY_LEN    (16384)-(SOCKLIB_HEAD_LEN)
//	typedef struct {
//	    SockLibHeadType head;  
//	    char            body[SOCKLIB_MAX_BODY_LEN];
//	} SockLibMsgType;	
	
//	typedef struct {
//	    char appName[32];
//	    char command[32];
//	    char imsi[16];
//	    char ipAddress[64];
//	    char jobNumber[8];
//	} ProvMmcRequestHeadType;
	
//	typedef struct {
//	    ProvMmcRequestHeadType head;
//	    char body[1024*4];
//	} ProvMmcRequest;


	protected boolean sendMessage(MMCMsgType resMMCType, String sendMsg) {
		try {

			// SockLibMsgType's body len
			int bodyLen = MMCMsgType.getProvMmcHeaderSize() + sendMsg.length();
			String appname  = resMMCType.getAppName();
			String command  = resMMCType.getCommand();
			String imsi		= resMMCType.getImsi();
			String ipAddress= resMMCType.getIpAddress();
			String jobNo	= resMMCType.getJobNumber();
			String port		= resMMCType.getPort();
			String tcpMode	= resMMCType.getTcpMode();

			if(CommandManager.getInstance().isLogFlag()) {
				logger.info("===============================================");
				logger.info("PROVS -> MMIB");
				logger.info("bodyLen : " + bodyLen);
				logger.info("appname : " + appname);
				logger.info("command : " + command);
				logger.info("imsi : " + imsi);
				logger.info("ipAddress : " + ipAddress);
				logger.info("jobNo : " + jobNo);
				logger.info("port : " + port);
				logger.info("tcpMode : " + tcpMode);
				logger.info("sendMsg ");
				logger.info(sendMsg);
				logger.info("===============================================");
			}

			int mapType = 4;
			if (command.startsWith("TRACE_")) {
				mapType = 17;
				command = command.replace("TRACE_", "");
			}

			// bodylen
			//dataOut.writeInt(byteToInt(toBytes(bodyLen), ByteOrder.BIG_ENDIAN));
			dataOut.writeInt(bodyLen);
			
			// maptype
			dataOut.writeInt(mapType);
			// mtype
			dataOut.writeInt(0);

			//appname
			dataOut.write(appname.getBytes());
			for(int i = 0; i < MMCMsgType.getAppNameLen() - appname.length(); i++)
				dataOut.write("\0".getBytes());
			
			//command
			dataOut.write(command.getBytes());
			for(int i = 0; i < MMCMsgType.getCommandLen() - command.length(); i++)
				dataOut.write("\0".getBytes());

			//imsi
			dataOut.write(imsi.getBytes());
			for(int i = 0; i < MMCMsgType.getImsiLen() - imsi.length(); i++)
				dataOut.write("\0".getBytes());

			//ipAddress
			dataOut.write(ipAddress.getBytes());
			for(int i = 0; i < MMCMsgType.getIpLen() - ipAddress.length(); i++)
				dataOut.write("\0".getBytes());

			//jobNumber
			dataOut.write(jobNo.getBytes());
			for(int i = 0; i < MMCMsgType.getJobLen() - jobNo.length(); i++)
				dataOut.write("\0".getBytes());

			// port
			dataOut.write(port.getBytes());
			for(int i = 0; i < MMCMsgType.getPortLen() - port.length(); i++)
				dataOut.write("\0".getBytes());
			
			// tcpMode
			dataOut.write(tcpMode.getBytes());
			for(int i = 0; i < MMCMsgType.getTcpmodeLen() - tcpMode.length(); i++)
				dataOut.write("\0".getBytes());

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

		// buff사이즈는 4인 상태임
		// bytes를 put하면 position과 limit는 같은 위치가 됨.
		buff.put(bytes);
		// flip()가 실행 되면 position은 0에 위치 하게 됨.
		buff.flip();

		return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
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
				//Command 처리 mapType 46 
				if(mapType == 46) {

					MMCMsgType mmcMsg = new MMCMsgType(); 
					mmcMsg.setMMCMsgType(din);
					
					
					if(CommandManager.getInstance().isLogFlag()) {
						logger.info("===============================================");
						logger.info("MMIB -> PROVS");
						logger.info("bodyLen : " + reservedMsgSize);
						logger.info("appname : " + mmcMsg.getAppName());
						logger.info("command : " + mmcMsg.getCommand());
						logger.info("imsi : " + mmcMsg.getImsi());
						logger.info("ipAddress : " + mmcMsg.getIpAddress());
						logger.info("jobNo : " + mmcMsg.getJobNumber());
						logger.info("port : " + mmcMsg.getPort());
						logger.info("tcpMode : " + mmcMsg.getTcpMode());						
						logger.info("===============================================");
					}

					//				Thread.sleep(500);
					receiver.receiveMessage(mmcMsg);					
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
		// TODO Auto-generated method stub
		return false;
	}
}