package com.kt.restful.model;

import java.io.DataInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.kt.util.Util;

//typedef struct {
//    char    apiName[64]; /* Get APN, Get Subscriber LTE Roaming Restrictions, 등 */
//    char    seq_no[8]; /* sequence number; 받은 값 그대로 반환 */             sample 에 Transaction-ID 6자리로 되어 있음.
//    char    res_code[8]; /* 요청 시에는 미 사용 */
//    char    data[8000]; /* 요청,응답 메시지 내용 */
//} ProvifMsgType;
public class ProvifMsgType {
	private String apiName;
	private String seqNo;
	private String imsi;
	private String mdn;
	private String ipAddress;
	private int resCode;
	private String data;
	
	private byte[] apiNameBuffer;
	private byte[] seqNoBuffer;
	private byte[] ipAddressBuffer;
	private byte[] imsiBuffer;
	private byte[] mdnBuffer;
	private byte[] dataBuffer;
	
	private static int API_NAME_LEN = 64;
	private static int SEQ_NO_LEN = 8;
	private static int IMSI_LEN = 16;
	private static int MDN_LEN = 12;
	private static int IP_ADDRESS_LEN = 64;
	
	public ProvifMsgType() {
		apiName = "";
		seqNo = "";
		imsi = "";
		mdn = "";
		ipAddress = "";
		resCode = 0;
		data = "";
		
		apiNameBuffer = new byte[API_NAME_LEN];
		seqNoBuffer = new byte[SEQ_NO_LEN];
		imsiBuffer = new byte[IMSI_LEN];
		mdnBuffer = new byte[MDN_LEN];
		ipAddressBuffer = new byte[IP_ADDRESS_LEN];
		
		msgSize = new int[4];
		for( int i=0; i<msgSize.length; i++ )
		    msgSize[i] = -1;
	}
	
	private int[] msgSize = new int[4];
	private int reservedMsgSize;
	public void setProvifMsgType(DataInputStream in) throws IOException {
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
		
//		this.setResCode(byteToInt(toBytes(in.readInt()), ByteOrder.LITTLE_ENDIAN));
//		
//		reservedMsgSize = byteToInt(toBytes(in.readInt()), ByteOrder.LITTLE_ENDIAN);
//	    dataBuffer = new byte[reservedMsgSize];
		
		this.setResCode(byteToInt(toBytes(in.readInt()), ByteOrder.BIG_ENDIAN));
		
		reservedMsgSize = byteToInt(toBytes(in.readInt()), ByteOrder.BIG_ENDIAN);
	    dataBuffer = new byte[reservedMsgSize];
	    
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
	
	
}
