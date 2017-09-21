package com.kt.net;

public interface Receiver {

	public void receiveMessage(String message, int rspCode, int cliReqId);
	
}