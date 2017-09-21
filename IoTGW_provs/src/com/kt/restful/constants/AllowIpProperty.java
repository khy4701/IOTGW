package com.kt.restful.constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class AllowIpProperty {
	private static Map<String, String> propHandlerMap = new HashMap<String, String>();
	
	private static Logger logger = LogManager.getLogger(AllowIpProperty.class);

	static {
		Properties prop = new Properties();
		InputStream fis = null;
		try {
			fis =  new FileInputStream(IoTProperty.getPropPath("allow_ip_file_path") + "/allowIp.properties");
			prop.load(fis);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (fis != null) {try { fis.close(); } catch (IOException ex) { } }
		}

		Iterator<Object> keyIter = prop.keySet().iterator();
		while (keyIter.hasNext()) {
			String constName = (String) keyIter.next();
			String path = prop.getProperty(constName);
			propHandlerMap.put(constName, path);
		}  
	}

	public static void setProperty(List<String> allowIpList) {
		Properties prop = new Properties();
		OutputStream output = null;

		try {
			output = new FileOutputStream(IoTProperty.getPropPath("allow_ip_file_path") + "/allowIp.properties");

			StringBuffer allowIpListSB = new StringBuffer();
			for( String ip : allowIpList ){
				allowIpListSB.append(ip);
				allowIpListSB.append(",");
			}

			prop.setProperty("allow_ip_list", allowIpListSB.toString());
			prop.store(output, null);
		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private AllowIpProperty(){}
	
	public static String getPropPath(String constName) {
		
		propHandlerMap = new HashMap<String, String>();
		propHandlerMap.clear();
		
		Properties prop1 = new Properties();
		InputStream fis = null;
		try {
			fis =  new FileInputStream(IoTProperty.getPropPath("allow_ip_file_path") + "/allowIp.properties");
			prop1.load(fis);
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		} finally {
			if (fis != null) {try { fis.close(); } catch (IOException ex) { } }
		}

		Iterator<Object> keyIter = prop1.keySet().iterator();
		while (keyIter.hasNext()) {
			String constName1 = (String) keyIter.next();
			String path1 = prop1.getProperty(constName1);
			propHandlerMap.put(constName1, path1);
		}  
		
		return propHandlerMap.get(constName);
	}
}
