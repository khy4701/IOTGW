package com.kt.net;

import com.kt.restful.model.MMCMsgType;

public interface CommandReceiver {

	public void receiveMessage(MMCMsgType mmcMsg);
	
}