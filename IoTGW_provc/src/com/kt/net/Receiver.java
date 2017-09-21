package com.kt.net;

public interface Receiver {

	public void receiveMessage(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body);
	
}