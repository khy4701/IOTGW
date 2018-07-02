package com.kt.restful.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.kt.util.Util;


//typedef struct {
//    char    apiName[64]; /* Get APN, Get Subscriber LTE Roaming Restrictions, 등 */
//    char    seq_no[8]; /* sequence number; 받은 값 그대로 반환 */             sample 에 Transaction-ID 6자리로 되어 있음.
//    char    res_code[8]; /* 요청 시에는 미 사용 */
//    char    data[8000]; /* 요청,응답 메시지 내용 */
//} ProvifMsgType;

public class ProvifMsgType {
	
	private static Logger logger = LogManager.getLogger(ProvifMsgType.class);
	
	/* ProvLibHeadType */
	private String url;
	private String appName; // provs1, provs2..
	private String apiName;
	private String seqNo;
	private String imsi;
	private String mdn;
	private String ipAddress;
	private int resCode;

	private String data;
	
	private byte[] urlBuffer;
	private byte[] appNameBuffer;	
	private byte[] apiNameBuffer;
	private byte[] seqNoBuffer;
	private byte[] ipAddressBuffer;
	private byte[] imsiBuffer;
	private byte[] mdnBuffer;
	private byte[] dataBuffer;
	
	private static int URL_LEN = 160;
	private static int APP_NAME_LEN = 64;
	private static int API_NAME_LEN = 64;
	private static int SEQ_NO_LEN = 8;
	private static int IMSI_LEN = 16;
	private static int MDN_LEN = 12;
	private static int IP_ADDRESS_LEN = 64;
	
	/* OemHeadType */
	private String osysCode;
	private String tsysCode;
	private String msgId;
	private String msgType;
	private String resultCode;
	private String resultDtlCode;
	private String resultMsg;
	
	private static int OEM_O_SYS_CD_LEN = 8;
	private static int OEM_T_SYS_CD_LEN = 8;
	private static int OEM_MSG_ID_LEN = 24;
	private static int OEM_MSG_TYPE_LEN = 8;
	private static int OEM_RESULT_CD_LEN = 8;
	private static int OEM_RESULT_DTL_CD_LEN = 16;
	private static int OEM_RESULT_MSG_LEN = 208;
	
	private byte[] osysCodeBuffer;
	private byte[] tsysCodeBuffer;	
	private byte[] msgIdBuffer;
	private byte[] msgTypeBuffer;
	private byte[] resultCodeBuffer;
	private byte[] resultDtlCodeBuffer;
	private byte[] resultMsgBuffer;

	
	public static void main(String[] args) {
		
		ProvifMsgType g = new ProvifMsgType();
		
		Class c = g.getClass();
		try {
			
			for ( Field field : c.getDeclaredFields())
		      
		      System.out.println(field);
		    } catch (SecurityException e) {
		      System.out.println(e);
		    }
	}

	
	public ProvifMsgType() {
		url="";
		appName ="";
		apiName = "";
		seqNo = "";
		imsi = "";
		mdn = "";
		ipAddress = "";
		resCode = 0;
		data = "";

		osysCode = "";
		tsysCode = "";
		msgId = "";
		msgType = "";
		resultCode = "";
		resultDtlCode = "";
		
		urlBuffer = new byte[URL_LEN];
		appNameBuffer = new byte[APP_NAME_LEN];
		apiNameBuffer = new byte[API_NAME_LEN];
		seqNoBuffer = new byte[SEQ_NO_LEN];
		imsiBuffer = new byte[IMSI_LEN];
		mdnBuffer = new byte[MDN_LEN];
		ipAddressBuffer = new byte[IP_ADDRESS_LEN];
		
		osysCodeBuffer = new byte[OEM_O_SYS_CD_LEN];
		tsysCodeBuffer = new byte[OEM_T_SYS_CD_LEN];
		msgIdBuffer = new byte[OEM_MSG_ID_LEN];
		msgTypeBuffer = new byte[OEM_MSG_TYPE_LEN];
		resultCodeBuffer = new byte[OEM_RESULT_CD_LEN];
		resultDtlCodeBuffer = new byte[OEM_RESULT_DTL_CD_LEN];
		resultMsgBuffer = new byte[OEM_RESULT_MSG_LEN];

		
		msgSize = new int[4];
		for( int i=0; i<msgSize.length; i++ )
		    msgSize[i] = -1;
	}
	
	private int[] msgSize = new int[4];
	private int reservedMsgSize;
	public void setProvifMsgType(DataInputStream in) throws IOException {
		
		
		/* ProvLibHeadType */
		in.read(urlBuffer, 0, urlBuffer.length);
		this.setUrl(Util.nullTrim(new String(urlBuffer)));
		
		in.read(appNameBuffer, 0, appNameBuffer.length);
		this.setAppName(Util.nullTrim(new String(appNameBuffer)));
		
		in.read(apiNameBuffer, 0, apiNameBuffer.length);
		this.setApiName(Util.nullTrim(new String(apiNameBuffer)));

		in.read(seqNoBuffer, 0, seqNoBuffer.length);
		this.setSeqNo(Util.nullTrim(new String(seqNoBuffer)));
		
		in.read(imsiBuffer, 0, imsiBuffer.length);
		this.setImsi(Util.nullTrim(new String(imsiBuffer)));
		
		in.read(mdnBuffer, 0, mdnBuffer.length);
		this.setMdn(Util.nullTrim(new String(mdnBuffer)));
		
		in.read(ipAddressBuffer, 0, ipAddressBuffer.length);
		this.setIpAddress(Util.nullTrim(new String(ipAddressBuffer)));
		
		this.setResCode(byteToInt(toBytes(in.readInt()), ByteOrder.BIG_ENDIAN));
		
		
		/* OemHeadType */
		in.read(osysCodeBuffer, 0, osysCodeBuffer.length);
		this.setOsysCode(Util.nullTrim(new String(osysCodeBuffer)));

		in.read(tsysCodeBuffer, 0, tsysCodeBuffer.length);
		this.setTsysCode(Util.nullTrim(new String(tsysCodeBuffer)));

		in.read(msgIdBuffer, 0, msgIdBuffer.length);
		this.setMsgId(Util.nullTrim(new String(msgIdBuffer)));

		in.read(msgTypeBuffer, 0, msgTypeBuffer.length);
		this.setMsgType(Util.nullTrim(new String(msgTypeBuffer)));

		in.read(resultCodeBuffer, 0, resultCodeBuffer.length);
		this.setResultCode(Util.nullTrim(new String(resultCodeBuffer)));

		in.read(resultDtlCodeBuffer, 0, resultDtlCodeBuffer.length);
		this.setResultDtlCode(Util.nullTrim(new String(resultDtlCodeBuffer)));

		in.read(resultMsgBuffer, 0, resultMsgBuffer.length);
		this.setResultMsg(Util.nullTrim(new String(resultMsgBuffer)));
		
		// bodyLen
		reservedMsgSize = byteToInt(toBytes(in.readInt()), ByteOrder.BIG_ENDIAN);
	    dataBuffer = new byte[reservedMsgSize];
			    
	    // body
		in.read(dataBuffer, 0, dataBuffer.length);
		this.setData(Util.nullTrim(new String(dataBuffer)));
	}
	

	private static byte[] toBytes(int i) {
        byte[] result = new byte[4];
        result[0] = (byte)(i>>24);
        result[1] = (byte)(i>>16);
        result[2] = (byte)(i>>8);
        result[3] = (byte)(i);
        return result;
    }   
	
	public static int byteToInt(byte[] bytes, ByteOrder order) {
		 
		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
		buff.order(order);
 
		// buff사이즈는 4인 상태임
		// bytes를 put하면 position과 limit는 같은 위치가 됨.
		buff.put(bytes);
		// flip()가 실행 되면 position은 0에 위치 하게 됨.
		buff.flip();
 
		return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getSeqNo() {
		return seqNo;
	}

	public void setSeqNo(String seqNo) {
		this.seqNo = seqNo;
	}

	public String getImsi() {
		return imsi;
	}

	public void setImsi(String imsi) {
		this.imsi = imsi;
	}

	public String getMdn() {
		return mdn;
	}

	public void setMdn(String mdn) {
		this.mdn = mdn;
	}
	
	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getResCode() {
		return resCode;
	}

	public void setResCode(int resCode) {
		this.resCode = resCode;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public int getReservedMsgSize() {
		return reservedMsgSize;
	}

	public void setReservedMsgSize(int reservedMsgSize) {
		this.reservedMsgSize = reservedMsgSize;
	}
	public String getUrl() {
		return url;
	}


	public void setUrl(String url) {
		this.url = url;
	}


	public String getAppName() {
		return appName;
	}


	public void setAppName(String appName) {
		this.appName = appName;
	}

    public String getOsysCode() {
		return osysCode;
	}


	public void setOsysCode(String osysCode) {
		this.osysCode = osysCode;
	}


	public String getTsysCode() {
		return tsysCode;
	}


	public void setTsysCode(String tsysCode) {
		this.tsysCode = tsysCode;
	}


	public String getMsgId() {
		return msgId;
	}


	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}


	public String getMsgType() {
		return msgType;
	}


	public void setMsgType(String msgType) {
		this.msgType = msgType;
	}


	public String getResultCode() {
		return resultCode;
	}


	public void setResultCode(String resultCode) {
		this.resultCode = resultCode;
	}


	public String getResultDtlCode() {
		return resultDtlCode;
	}


	public void setResultDtlCode(String resultDtlCode) {
		this.resultDtlCode = resultDtlCode;
	}


	public String getResultMsg() {
		return resultMsg;
	}


	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public static int getURL_LEN() {
		return URL_LEN;
	}

	public static int getAPP_NAME_LEN() {
		return APP_NAME_LEN;
	}

	public static int getAPI_NAME_LEN() {
		return API_NAME_LEN;
	}

	public static int getSEQ_NO_LEN() {
		return SEQ_NO_LEN;
	}

	public static int getIMSI_LEN() {
		return IMSI_LEN;
	}

	public static int getMDN_LEN() {
		return MDN_LEN;
	}

	public static int getIP_ADDRESS_LEN() {
		return IP_ADDRESS_LEN;
	}

	public static int getOEM_O_SYS_CD_LEN() {
		return OEM_O_SYS_CD_LEN;
	}

	public static int getOEM_T_SYS_CD_LEN() {
		return OEM_T_SYS_CD_LEN;
	}

	public static int getOEM_MSG_ID_LEN() {
		return OEM_MSG_ID_LEN;
	}

	public static int getOEM_MSG_TYPE_LEN() {
		return OEM_MSG_TYPE_LEN;
	}

	public static int getOEM_RESULT_CD_LEN() {
		return OEM_RESULT_CD_LEN;
	}

	public static int getOEM_RESULT_DTL_CD_LEN() {
		return OEM_RESULT_DTL_CD_LEN;
	}

	public static int getOEM_RESULT_MSG_LEN() {
		return OEM_RESULT_MSG_LEN;
	}
	
	public static int getProvLibHeadSize() {
				
		// 4 : resCode
		return URL_LEN + APP_NAME_LEN + API_NAME_LEN + 
				SEQ_NO_LEN + IMSI_LEN + MDN_LEN + 
				IP_ADDRESS_LEN + 4 ;
	}
	
	public static int getOemHeadSize() {
		
		return OEM_O_SYS_CD_LEN + OEM_T_SYS_CD_LEN + OEM_MSG_ID_LEN + 
				OEM_MSG_TYPE_LEN + OEM_RESULT_CD_LEN + OEM_RESULT_DTL_CD_LEN + 
				OEM_RESULT_MSG_LEN;
	}

	
	
}
