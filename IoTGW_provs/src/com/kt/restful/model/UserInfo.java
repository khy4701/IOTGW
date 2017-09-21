package com.kt.restful.model;

public enum UserInfo {
	
	MDN("MDN"),                      
	CUST_SBSC_PROD("CUST_SBSC_PROD"),
	IMEI("IMEI"),                    
	IPADR_CTGRY("IPADR_CTGRY"),      
	IPADR("IPADR"),            
	ACCS_TKEN("ACCS_TKEN"),                
	DVC_ID("DVC_ID"),      
	MDN_ATHN_CD("MDN_ATHN_CD"),
	MDN_ATHN_STTUS("MDN_ATHN_STTUS"),        
	PUSH_ID("PUSH_ID"),  
	INOT_STTUS("INOT_STTUS"),  
	APN_UPLD_TIME("APN_UPLD_TIME"),      
	ERR_UPLD_TIME("ERR_UPLD_TIME"),      
	MBL_OS_TYPE("MBL_OS_TYPE"),  
	SPAP_TYPE("SPAP_TYPE"),
	SPAP_VER_NM("SPAP_VER_NM"),              
	USER_NM("USER_NM"),              
	DEPT_CD("DEPT_CD"),              
	REG_DT("REG_DT"),                
	LAST_DT("LAST_DT");
	
	final private String name;

	private UserInfo(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}
