package com.kt.restful.model;

public class StatisticsModel {
	private String apiName; 
	private String ipAddress;
	private int total;
	private int succ;
	private int fail;
	private int error400;
	private int error409;
	private int error410;
	private int error500;
	private int error501;
	private int error403;
	private int error503;
	
	public StatisticsModel() {
		this.apiName = ""; 
		this.ipAddress = "";
		this.total = 0;
		this.succ = 0;
		this.fail = 0;	
		this.error400 = 0;
		this.error409 = 0;
		this.error410 = 0;
		this.error500 = 0;
		this.error501 = 0;
		this.error403 = 0;
		this.error503 = 0;
	}
	
	public StatisticsModel(String apiName, String ipAddress, int total, int succ, int fail) {
		this.apiName = apiName; 
		this.ipAddress = ipAddress;
		this.total = total;
		this.succ = succ;
		this.fail = fail;	
		this.error400 = 0;
		this.error409 = 0;
		this.error410 = 0;
		this.error500 = 0;
		this.error501 = 0;
		this.error403 = 0;
		this.error503 = 0;
	}
	
	public StatisticsModel(String apiName, String ipAddress, int total, int succ, int fail, int error400, int error500) {
		this.apiName = apiName; 
		this.ipAddress = ipAddress;
		this.total = total;
		this.succ = succ;
		this.fail = fail;	
		this.error400 = error400;
		this.error500 = error500;
	}

	public String getApiName() {
		return apiName;
	}

	public void setApiName(String apiName) {
		this.apiName = apiName;
	}

	public String getIpAddress() {
		return ipAddress;
	}

	public void setIpAddress(String ipAddress) {
		this.ipAddress = ipAddress;
	}

	public int getTotal() {
		return total;
	}

	public void setTotal(int total) {
		this.total = total;
	}

	public int getSucc() {
		return succ;
	}

	public void setSucc(int succ) {
		this.succ = succ;
	}

	public int getFail() {
		return fail;
	}

	public void setFail(int fail) {
		this.fail = fail;
	}
	
	public int getError400() {
		return error400;
	}

	public void setError400(int error400) {
		this.error400 = error400;
	}

	public int getError409() {
		return error409;
	}

	public void setError409(int error409) {
		this.error409 = error409;
	}

	public int getError410() {
		return error410;
	}

	public void setError410(int error410) {
		this.error410 = error410;
	}

	public int getError500() {
		return error500;
	}

	public void setError500(int error500) {
		this.error500 = error500;
	}

	public int getError501() {
		return error501;
	}

	public void setError501(int error501) {
		this.error501 = error501;
	}

	public int getError403() {
		return error403;
	}

	public void setError403(int error403) {
		this.error403 = error403;
	}

	public int getError503() {
		return error503;
	}

	public void setError503(int error503) {
		this.error503 = error503;
	}

	public int plusTotal() {
		return total++;
	}
	
	public int plusSucc() {
		return succ++;
	}
	
	public int plusFail() {
		return fail++;
	}
	
	public int plusError400() {
		return error400++;
	}
	
	public int plusError409() {
		return error409++;
	}
	
	public int plusError410() {
		return error410++;
	}
	
	public int plusError500() {
		return error500++;
	}
	
	public int plusError501() {
		return error501++;
	}
	
	public int plusError403() {
		return error403++;
	}
	
	public int plusError503() {
		return error503++;
	}
}
