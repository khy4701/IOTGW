package com.kt.net;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.restful.model.ProvifMsgType;

public class TraceManager {

	private static Logger logger = LogManager.getLogger(TraceManager.class);

	private static TraceManager trcManager;

	public static int reqId = 1000;

	public static TraceManager getInstance() {
		if(trcManager == null) {
			trcManager = new TraceManager();
		}
		return trcManager;
	}

	public TraceManager() {
	}
//	TraceManager.getInstance().printCubicTrace(sb, remoteAddr, apiName, req.getRequestURL().toString(), 
//	osysCode, tsysCode, msgId, msgType, resCode, resultDtlCode, resultMsg, jsonBody);

	public void printCubicTrace_req(StringBuffer sb, String remoteAddr, String apiName, String requestUrl, String osysCode,
			String tsysCode, String msgId, String msgType, String resCode, String resultDtlCode, String resultMsg, String jsonBody){
		
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
			sb.append("DIRECTION : CUBIC[" + remoteAddr + "] -> PROVS");
			sb.append(System.getProperty("line.separator"));
			sb.append(apiName + " REQUEST");
			sb.append(System.getProperty("line.separator"));
			sb.append("REQUEST URL : " + requestUrl);
			sb.append(System.getProperty("line.separator"));
			sb.append("O_SYS_CD : " + osysCode);
			sb.append(System.getProperty("line.separator"));
			sb.append("T_SYS_CD : " + tsysCode);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSG_ID   : " + msgId);
			sb.append(System.getProperty("line.separator"));
			sb.append("MSG_TYPE : " + msgType);
			sb.append(System.getProperty("line.separator"));
			sb.append("RESULT_CD : " + resCode);
			sb.append(System.getProperty("line.separator"));
			sb.append("RESULT_DTL_CD : " + resultDtlCode);
			sb.append(System.getProperty("line.separator"));
			sb.append("RESULT_MSG : " + resultMsg);
			sb.append(System.getProperty("line.separator"));
			sb.append("BODY : " + jsonBody);
			sb.append(System.getProperty("line.separator"));
			sb.append("=============================================");
			sb.append(System.getProperty("line.separator"));
	}
	
	public void printCubicTrace_res(StringBuffer sb, String remoteAddr, String apiName, int httpResCode, ProvifMsgType provMsg, String jsonBody){
		
		sb.append("=============================================");
		sb.append(System.getProperty("line.separator"));
		sb.append("DIRECTION : PROVS -> CUBIC[" + remoteAddr + "]");
		sb.append(System.getProperty("line.separator"));
		sb.append(apiName + " REPONSE");
		sb.append(System.getProperty("line.separator"));
		sb.append("STATUS : " + httpResCode);
		sb.append(System.getProperty("line.separator"));
		sb.append("O_SYS_CD : " + provMsg.getOsysCode());
		sb.append(System.getProperty("line.separator"));
		sb.append("T_SYS_CD : " + provMsg.getTsysCode());
		sb.append(System.getProperty("line.separator"));
		sb.append("MSG_ID   : " + provMsg.getMsgId());
		sb.append(System.getProperty("line.separator"));
		sb.append("MSG_TYPE : " + provMsg.getMsgType());
		sb.append(System.getProperty("line.separator"));
		sb.append("RESULT_CD : " + provMsg.getResultCode());
		sb.append(System.getProperty("line.separator"));
		sb.append("RESULT_DTL_CD : " + provMsg.getResultDtlCode());
		sb.append(System.getProperty("line.separator"));
		sb.append("RESULT_MSG : " + provMsg.getResultMsg());
		sb.append(System.getProperty("line.separator"));
		sb.append("BODY : " + jsonBody);
		sb.append(System.getProperty("line.separator"));
		sb.append("=============================================");
		sb.append(System.getProperty("line.separator"));
		
	}
	
	



}