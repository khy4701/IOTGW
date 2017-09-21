package com.kt.restful.model;

public enum UserSecureInfo {

	EVENT_TYPE("EVENT_TYPE"),                      
	MDN("MDN"),
	CUST_ID("CUST_ID"),                    
	IMEI("IMEI"),      
	NEW_MDN("NEW_MDN"),      
	USER_NM("USER_NM"),    
	DEPT_CD("DEPT_CD"),
	USST("USST"),                  
	TRM_YN("TRM_YN"),                
	REG_DT("REG_DT");

	final private String name;

	private UserSecureInfo(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}
}