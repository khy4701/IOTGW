package com.kt.net;

public class SenderInfo {
	private int cliReqId;
	private DBMListener source;

	public SenderInfo(int cliReqId, DBMListener source) {
		this.cliReqId = cliReqId;
		this.source = source;
	}

	public int getCliReqId() {
		return this.cliReqId;
	}

	public DBMListener getSource() {
		return this.source;
	}

//	public void display() {
//	    System.out.println("cliReqId= " + cliReqId + "source= " + source 
//	            + "key= " + key + "index= " + index);
//	}
}