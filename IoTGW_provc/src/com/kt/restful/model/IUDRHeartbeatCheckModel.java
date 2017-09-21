package com.kt.restful.model;

public class IUDRHeartbeatCheckModel {

	private final static int CONN_STS_ABNORMAL = 0;
	private final static int CONN_STS_NORMAL = 1;

	private String ipAddress = "";
	private int status = CONN_STS_NORMAL;
	
	public IUDRHeartbeatCheckModel(String ipAddress, int status) {
		this.ipAddress = ipAddress;
		this.status = status;
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
}