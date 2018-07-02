package com.kt.net;

import com.kt.restful.model.ProvifMsgType;

public interface Receiver {

	public void receiveMessage(String message, int rspCode, int cliReqId, ProvifMsgType pmt);
	
}