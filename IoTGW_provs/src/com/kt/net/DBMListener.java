package com.kt.net;

import com.kt.restful.model.ProvifMsgType;

public interface DBMListener {

	public void setComplete(String msg, int rspCode, int reqId, ProvifMsgType pmt);
}
