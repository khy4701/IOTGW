//package com.kt.restful.model;
//
//import java.io.DataInputStream;
//import java.io.IOException;
//import java.lang.reflect.Field;
//import java.nio.ByteBuffer;
//import java.nio.ByteOrder;
//
//import org.apache.log4j.LogManager;
//import org.apache.log4j.Logger;
//
//import com.kt.util.Util;
//
//
//
////typedef struct {
////    char    apiName[64]; /* Get APN, Get Subscriber LTE Roaming Restrictions, 등 */
////    char    seq_no[8]; /* sequence number; 받은 값 그대로 반환 */             sample 에 Transaction-ID 6자리로 되어 있음.
////    char    res_code[8]; /* 요청 시에는 미 사용 */
////    char    data[8000]; /* 요청,응답 메시지 내용 */
////} ProvifMsgType;
//
//public class ProvifMsgType_CHG {
//	
//	private static Logger logger = LogManager.getLogger(ProvifMsgType_CHG.class);
//	
//	
//	public static void main(String[] args) {
//		
//		ProvStruct prov = new ProvStruct();
//		ProvStruct_Buffer provBuffer = new ProvStruct_Buffer();
//		
//		Class prov_class = prov.getClass();
//		Class provBuffer_class = provBuffer.getClass();
//		
//		try {			
//			// 모든 필드를 가져옴.
//			for ( Field field : prov_class.getDeclaredFields()){
//				
//				String clsType = field.getType().toString();
//				
//				// String 
//				if (clsType.equals("class java.lang.String")){
//					
//					String filedName = field.getName();
//					
//					try {
//						Field buffField = provBuffer_class.getDeclaredField(filedName+"Buffer");
//						
//						buffField.setAccessible(true);
//						byte [] tt = (byte []) buffField.get(provBuffer);
//						
//						System.out.println(filedName+"Buffer : " );
//												
//					} catch (NoSuchFieldException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//					
//					
//					field.setAccessible(true);
//					field.set(prov, "1");
//					
//					System.out.println(prov.getUrl());
//					
//				}
//				// Int
//				else if (clsType.equals("int"))
//					System.out.println("INT : " + field.getName());
//				else
//					System.out.println("??");;
//				
//			}
//			
//		    } catch (SecurityException e) {
//		      System.out.println(e);
//		    } catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		 }
//
//	
//	
//	public ProvifMsgType_CHG() {
//		url="";
//		appName ="";
//		apiName = "";
//		seqNo = "";
//		imsi = "";
//		mdn = "";
//		ipAddress = "";
//		resCode = 0;
//		data = "";
//		
//		
//		msgSize = new int[4];
//		for( int i=0; i<msgSize.length; i++ )
//		    msgSize[i] = -1;
//	}
//	
//	private int[] msgSize = new int[4];
//	private int reservedMsgSize;
//	public void setProvifMsgType(DataInputStream in) throws IOException {
//		
//		ProvStruct prov = new ProvStruct();
//		ProvStruct_Buffer provBuffer = new ProvStruct_Buffer();
//		
//		Class prov_class = prov.getClass();
//		Class provBuffer_class = provBuffer.getClass();
//		
//		try {			
//			// 모든 필드를 가져옴.
//			for ( Field field : prov_class.getDeclaredFields()){
//				
//				String clsType = field.getType().toString();
//				
//				// String 
//				if (clsType.equals("class java.lang.String")){
//					
//					String filedName = field.getName();
//					
//					try {
//						Field buffField = provBuffer_class.getDeclaredField(filedName+"Buffer");
//						
//						buffField.setAccessible(true);
//						byte [] buffer = (byte []) buffField.get(provBuffer);
//						
//						// Read String
//						in.read(buffer, 0, buffer.length);
//						field.set(prov, Util.nullTrim(new String(buffer)));
//						
//					} catch (NoSuchFieldException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}					
//				}
//				// Int
//				else if (clsType.equals("int")){
//					
//					field.set(prov, byteToInt(toBytes(in.readInt()), ByteOrder.BIG_ENDIAN));					
//				}
//				else
//					System.out.println("??");;
//				
//			}
//			
//		    } catch (SecurityException e) {
//		      System.out.println(e);
//		    } catch (IllegalArgumentException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IllegalAccessException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		
//		
//		reservedMsgSize = byteToInt(toBytes(in.readInt()), ByteOrder.BIG_ENDIAN);
//		byte[] dataBuffer;
//
//	    dataBuffer = new byte[reservedMsgSize];
//		
//		in.read(dataBuffer, 0, dataBuffer.length);
//		this.setData(Util.nullTrim(new String(dataBuffer)));
//	}
//	
//    private static byte[] toBytes(int i) {
//        byte[] result = new byte[4];
//        result[0] = (byte)(i>>24);
//        result[1] = (byte)(i>>16);
//        result[2] = (byte)(i>>8);
//        result[3] = (byte)(i);
//        return result;
//    }   
//	
//	public static int byteToInt(byte[] bytes, ByteOrder order) {
//		 
//		ByteBuffer buff = ByteBuffer.allocate(Integer.SIZE/8);
//		buff.order(order);
// 
//		// buff사이즈는 4인 상태임
//		// bytes를 put하면 position과 limit는 같은 위치가 됨.
//		buff.put(bytes);
//		// flip()가 실행 되면 position은 0에 위치 하게 됨.
//		buff.flip();
// 
//		return buff.getInt(); // position위치(0)에서 부터 4바이트를 int로 변경하여 반환
//	}
//
//	public String getApiName() {
//		return apiName;
//	}
//
//	public void setApiName(String apiName) {
//		this.apiName = apiName;
//	}
//
//	public String getSeqNo() {
//		return seqNo;
//	}
//
//	public void setSeqNo(String seqNo) {
//		this.seqNo = seqNo;
//	}
//
//	public String getImsi() {
//		return imsi;
//	}
//
//	public void setImsi(String imsi) {
//		this.imsi = imsi;
//	}
//
//	public String getMdn() {
//		return mdn;
//	}
//
//	public void setMdn(String mdn) {
//		this.mdn = mdn;
//	}
//	
//	public String getIpAddress() {
//		return ipAddress;
//	}
//
//	public void setIpAddress(String ipAddress) {
//		this.ipAddress = ipAddress;
//	}
//
//	public int getResCode() {
//		return resCode;
//	}
//
//	public void setResCode(int resCode) {
//		this.resCode = resCode;
//	}
//
//	public String getData() {
//		return data;
//	}
//
//	public void setData(String data) {
//		this.data = data;
//	}
//
//	public int getReservedMsgSize() {
//		return reservedMsgSize;
//	}
//
//	public void setReservedMsgSize(int reservedMsgSize) {
//		this.reservedMsgSize = reservedMsgSize;
//	}
//	
//
//	static class ProvStruct{
//		private String url;
//		private String appName; // provs1, provs2..
//		private String apiName;
//		private String seqNo;
//		private String imsi;
//		private String mdn;
//		private String ipAddress;
//		private int resCode;
//	
//		ProvStruct() {		
//		}
//		
//		String getUrl(){
//			return this.url;
//		}
//	}
//	
//	static class ProvStruct_Buffer{
//		
//		private byte[] urlBuffer;
//		private byte[] appNameBuffer;	
//		private byte[] apiNameBuffer;
//		private byte[] seqNoBuffer;
//		private byte[] imsiBuffer;
//		private byte[] mdnBuffer;
//		private byte[] ipAddressBuffer;
//	
//		ProvStruct_Buffer() {		
//			urlBuffer = new byte[160];
//			appNameBuffer = new byte[64];
//			apiNameBuffer = new byte[64];
//			seqNoBuffer = new byte[8];
//			imsiBuffer = new byte[16];
//			mdnBuffer = new byte[12];
//			ipAddressBuffer = new byte[64];
//
//		}
//		
//	}
//	
//
//}
