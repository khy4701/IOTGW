package com.kt.net;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public abstract class Connector implements Runnable {
	private static Logger logger = LogManager.getLogger(Connector.class);
	
	protected static final int BUFFER_SIZE = 8192*4;//버퍼사이즈가 작으면 소켓이 끊어질수 잇어 omp는 512k
	 

	protected ConnectObserver	connectObserver;

	// private String ipAddr;
	// private int port;
	protected ServerSocket      socket;
	protected Thread			reader;
	protected Socket s;

	protected DataInputStream	dataIn;
	protected DataOutputStream	dataOut;

	protected Receiver			receiver;
	protected byte[]			buffer;
	protected Header			header;
	
	protected int port;
	protected String ipAddr;

	public Connector(String ipAddr, int port) {
		this.buffer = new byte[BUFFER_SIZE];
		this.header = new Header();
		
//		try {
//			if (connect(ipAddr, port)) {
//				reader = new Thread(this, "Reader");
//				reader.start();
//			}
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
	}
	
	public Connector(Receiver receiver, String ipAddr, int port) {

		this.receiver = receiver;

		this.buffer = new byte[BUFFER_SIZE];
		this.header = new Header();
		
		this.ipAddr = ipAddr;
		this.port = port;
		
//		connect(ipAddr, port);

		try {
			if (connect(ipAddr, port)) {
				reader = new Thread(this, "Reader");
				reader.start();
			}
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
	
/**
 * @param connectObserver 이 connection을 관리할 넘
 * @param receiver socket으로 부터 받은 메시지를 받을 넘
 * @param ipAddr 접속 할 IP address
 * @param port 당근 빠따 접속 할 port
 */
	public Connector(ConnectObserver connectObserver, Receiver receiver, String ipAddr, int port) {
		this.connectObserver = connectObserver;

		this.receiver = receiver;
		// this.ipAddr = ipAddr;
		// this.port = port;

		this.buffer = new byte[BUFFER_SIZE];
		this.header = new Header();

//		try {
//			if (connect(ipAddr, port)) {
//				logger.debug("Connector init=====================");
//				reader = new Thread(this, "Reader");
//				reader.start();
//			}
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
	}

/**
 * 연결이 자알 되어있는지 함 보자고~
 * 
 * @return 연결 됐으면 트루재
 */
	public boolean isConnected() {
		if (socket == null) {
			return false;
		}

//		return this.socket.isConnected();
		return true;
		
	}

	public void run() {
		Thread thisThread = Thread.currentThread();

		while (reader == thisThread) {
			try {
				readMessage();
			} catch (Exception e) {
				logger.error(socket.getInetAddress().getHostName() + ":" + socket.getLocalPort() + " exception occured");
				logger.error(e);
				logger.error("message : " + e.getMessage());
				logger.error("not connected...");
				logger.error(this.getClass().getName() + "-" + e.getMessage());
				e.printStackTrace();
				reader = null;
				
				try {
					socket.close();
					s.close();
					socket = null;
					s = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					logger.error(e1.getMessage());
				}
				
				if(connect(this.ipAddr, this.port)) {
					reader = new Thread(this, "Reader");
					reader.start();
				}
				
			}
		}

//		try {
//			socket.close();
//		} catch (IOException e) {
//			logger.error(e);
//		}
//
//		socket = null;
	}

	protected abstract void readMessage() throws IOException;

	protected abstract boolean sendMessage(String result);

//	private void connect(String ipAddr, int port) {
//		try {
//			socket = new ServerSocket(port);
//			socket.setReceiveBufferSize(BUFFER_SIZE);
////			socket = new ServerSocket(port);
////			socket.setReceiveBufferSize(BUFFER_SIZE);
//			while(true) {
//				s = socket.accept();
//				logger.info("Socket Accept=====================");
//
//				dataIn = new DataInputStream(new BufferedInputStream(s.getInputStream(), BUFFER_SIZE));
//				dataOut = new DataOutputStream(new BufferedOutputStream(s.getOutputStream(), BUFFER_SIZE));
//				
//				reader = new Thread(this, "Reader");
//				reader.start();
//			}
//		} catch (SocketException se) {
//			logger.error("SocketException" + se.getMessage());
//		} catch (IOException e) {
//			logger.error("IOException : " + e.getMessage());
//		}
//
//	}
	
	private boolean connect(String ipAddr, int port) {
		try {
			socket = new ServerSocket(port);
			socket.setReceiveBufferSize(BUFFER_SIZE);
//			Socket s = socket.accept();
			s = socket.accept();
			logger.info("Socket Accept=====================");
			
//			socket.connect(new InetSocketAddress(ipAddr, port));

			dataIn = new DataInputStream(new BufferedInputStream(s.getInputStream(), BUFFER_SIZE));
			dataOut = new DataOutputStream(new BufferedOutputStream(s.getOutputStream(), BUFFER_SIZE));
		} catch (SocketException se) {
//			socket = null;
			return false;
		} catch (IOException e) {
//			socket = null;
			return false;
		}

		return this.isConnected();
	}
	
	protected class Header {
		protected int	mapType	= 0;
		protected int	segFlag;
		protected int	seqNo;

		public void readHeader(DataInputStream dataIn) throws IOException {
			mapType = dataIn.readInt();
			segFlag = dataIn.readByte();
			seqNo = dataIn.readByte();
			
			dataIn.readByte();
			dataIn.readByte();
		}
	}
}
