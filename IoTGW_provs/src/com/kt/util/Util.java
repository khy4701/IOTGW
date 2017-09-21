package com.kt.util;

public class Util {

	public static String nullTrim(String str) {
		if(str.length() == 0 || str.charAt(0) == 0) {
			return "";
		}

		String tempStr = str.trim();
		
		if(tempStr.indexOf(0) != -1) {
			return tempStr.substring(0, tempStr.indexOf(0));
		} else {
			return tempStr;
		}
	}
}
