package com.kt.net;

import com.kt.restful.model.MMCMsgType;

public interface CommandReceiver {

//	public void receiveMessage(String appname, String command, String imsi, String ipAddress, String jobNumber);
	public void receiveMessage(MMCMsgType mmcMsg);

}