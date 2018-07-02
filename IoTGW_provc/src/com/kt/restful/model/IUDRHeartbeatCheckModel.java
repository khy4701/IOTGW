package com.kt.restful.model;

public class IUDRHeartbeatCheckModel {

	private final static int CONN_STS_ABNORMAL = 0;
	private final static int CONN_STS_NORMAL = 1;

	private String ipAddress = "";
	private int status = CONN_STS_NORMAL;
	private int port = 0;
	private int connType = 0;
	
	public IUDRHeartbeatCheckModel(String ipAddress, int port, int status, int connType) {
		this.ipAddress = ipAddress;
		this.port = port;
		this.status = status;		
		this.connType = connType;
	}

	public int getConnType() {
		return connType;
	}

	public void setConnType(int connType) {
		this.connType = connType;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}
	
}