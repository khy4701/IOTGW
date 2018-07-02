package com.kt.net;

import com.kt.restful.model.ProvifMsgType;

public interface Receiver {

	//public void receiveMessage(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body);
	public void receiveMessage(String apiName, String seqNo, int rspCode, String imsi, String msisdn, String ipAddress, String body, ProvifMsgType pmt);
	
}