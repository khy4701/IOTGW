package com.kt.net;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.constants.IoTProperty;
import com.kt.restful.model.IUDRHeartbeatCheckModel;
import com.kt.restful.model.MMCMsgType;
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
	public List<IUDRHeartbeatCheckModel> bssiotConnCheckList = new ArrayList<IUDRHeartbeatCheckModel>();
	public List<IUDRHeartbeatCheckModel> cubicConnCheckList = new ArrayList<IUDRHeartbeatCheckModel>();
	
	public List<IUDRHeartbeatCheckModel> getIudrConnCheckList() {
		return iudrConnCheckList;
	}
	
	public List<IUDRHeartbeatCheckModel> getBssiotConnCheckList() {
		return bssiotConnCheckList;
	}
	
	public List<IUDRHeartbeatCheckModel> getCubicConnCheckList() {
		return cubicConnCheckList;
	} 

	public void setIudrConnCheckList(List<IUDRHeartbeatCheckModel> iudrConnCheckList) {
		this.iudrConnCheckList = iudrConnCheckList;
	}
	
	public void setBssiotConnCheckList(List<IUDRHeartbeatCheckModel> bssiotConnCheckList) {
		this.bssiotConnCheckList = bssiotConnCheckList;
	}

	public void setCubicConnCheckList(List<IUDRHeartbeatCheckModel> cubicConnCheckList) {
		this.cubicConnCheckList = cubicConnCheckList;
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

	}

	public boolean sendConnCheckMessage(int connType) {
		
		List<IUDRHeartbeatCheckModel> connCheckList = null;
		
		String appName = IoTProperty.getPropPath("sys_name");
		
		// connType 
		// provc1 : IUDR(2), BSS-IOT(5)
		// provc2 : BSS-IOT(5)
		// provc3 : CUBIC(6)
		
		if (appName.equals("provc1") && connType == 2)
			connCheckList = iudrConnCheckList;
		
		else if ( (appName.equals("provc1") && connType == 5) || (appName.equals("provc2") && connType == 5))
			connCheckList = bssiotConnCheckList;
		
		else if (appName.equals("provc3") && connType == 6){
			connCheckList = cubicConnCheckList;
		}
		else
			return false;
			
		try {
			synchronized(connCheckList) {
				int bodyLen = 8 + (connCheckList.size() * 72 );
				int mapType = 100;

				//dataOut.writeInt(byteToInt(toBytes(bodyLen), ByteOrder.BIG_ENDIAN));
				dataOut.writeInt(bodyLen);
				dataOut.writeInt(mapType);
				dataOut.writeInt(0);

				//connType
				dataOut.writeInt(connType);
				
				//count
				dataOut.writeInt(connCheckList.size());

				for(IUDRHeartbeatCheckModel model : connCheckList) {
					if(CommandManager.getInstance().isLogFlag()) {
						logger.info("IUDR IP : " + model.getIpAddress()+ " PORT : "+ model.getPort() + " STATUS : " + model.getStatus() +" CONN TYPE : " + model.getConnType() );
					}
					
					//ipAddress
					dataOut.write(model.getIpAddress().getBytes());
					for(int i = 0; i < 64 - model.getIpAddress().length(); i++)
						dataOut.write("\0".getBytes());
					
					//port
					dataOut.writeInt(model.getPort());
					
					//status
					dataOut.writeInt(model.getStatus());
					
				}
				
//				if(CommandManager.getInstance().isLogFlag()) {
//					logger.info("[PROVS -> MMIBMMIB, H.B MSG]  MAP_TYPE[" + mapType +"] CONNTYPE [" + connType +"] COUNT [ " + connCheckList.size() );
//				}

				dataOut.flush();
			}
		} catch (Exception ex) {
			return false;
		}
		return true;
	}

	protected boolean sendMessage(MMCMsgType resMMCType, String sendMsg) {
		try {
			int bodyLen = MMCMsgType.getProvMmcHeaderSize() + sendMsg.length();
			
			String appname  = resMMCType.getAppName();
			String command  = resMMCType.getCommand();
			String imsi		= resMMCType.getImsi();
			String ipAddress= resMMCType.getIpAddress();
			String jobNo	= resMMCType.getJobNumber();
			String port		= resMMCType.getPort();
			String tcpMode	= resMMCType.getTcpMode();
			
			if(CommandManager.getInstance().isLogFlag()){
				logger.info("===============================================");
				logger.info("PROVC -> MMIB");
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

			dataOut.writeInt(byteToInt(toBytes(bodyLen), ByteOrder.BIG_ENDIAN));
			//			dataOut.writeInt(bodyLen);
			//Statistics Count
			dataOut.writeInt(mapType);
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

		buff.put(bytes);
		buff.flip();

		return buff.getInt(); 
	}

	protected void readMessage() throws IOException {
		if (msgReadStarted == false) {
			//			reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.BIG_ENDIAN);
			//			reservedMsgSize = byteToInt(toBytes(dataIn.readInt()), ByteOrder.LITTLE_ENDIAN);
			
			// Socket Header[bodylen]
			reservedMsgSize = dataIn.readInt();
			//			reservedMsgSize = reservedMsgSize - 4;
			if (reservedMsgSize > BUFFER_SIZE) {
				logger.info(
						"(DBM) ReservedMsgSize is larger than "+ BUFFER_SIZE+ " : " + reservedMsgSize);
				msgReadStarted = false;
				totalReadSize = 0;

				int avail = dataIn.available();
				dataIn.skipBytes(avail);
				return;
			}

			// Socket Header[mapType]
			mapType = dataIn.readInt();
			// Socket Header[mtype]
			dataIn.skipBytes(4);

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
					
					receiver.receiveMessage(mmcMsg);					
					
				// Heart Beat
				//IUDR Connection Check mapType 100
				} else if (mapType == 100) {
					
					List<IUDRHeartbeatCheckModel> ConnCheckList = null;
					
					// Connection Type
					int connType = din.readInt();
										
					List<IUDRHeartbeatCheckModel> notusedConnCheckList = new ArrayList<IUDRHeartbeatCheckModel>();
					// iudr
					if (connType == 2)
						ConnCheckList = iudrConnCheckList;
					// bss-iot
					else if (connType == 5)
						ConnCheckList = bssiotConnCheckList;
					// cubic-iot
					else if (connType == 6)
						ConnCheckList = cubicConnCheckList;					
					else
						// mmib->provc , provs 로 broadcasting 하는데, connType = 6(cubic-iot)는 provs에서 처리하는 메시지. 
						ConnCheckList = notusedConnCheckList;
					
					synchronized (ConnCheckList) {
						// Count
						int count = din.readInt();
						ConnCheckList.clear();

						for(int i = 0; i < 32; i++) {
							byte[] ipAddressBuffer = new byte[64];
							din.read(ipAddressBuffer, 0, ipAddressBuffer.length);
							
							// IpAddress
							String ipAddress = Util.nullTrim(new String(ipAddressBuffer));
							
							// port 
							int port = din.readInt();
							
							// status
							din.skipBytes(4);
														
							if (ipAddress.equals(""))
								continue;							
							
							ConnCheckList.add(new IUDRHeartbeatCheckModel(ipAddress, port, 0, connType));
						}
												
//						if(CommandManager.getInstance().isLogFlag()) {
//							logger.info("[MMIB -> PROVS, H.B MSG] BODY_LEN[" + reservedMsgSize +"] CONNTYPE [" + connType +"] COUNT [ " + count );
//						}
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