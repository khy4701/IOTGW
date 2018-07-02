package com.kt.restful.constants;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class OverloadControlProperty {
	private static Map<String, String> propHandlerMap = new HashMap<String, String>();
	
	private static Logger logger = LogManager.getLogger(OverloadControlProperty.class);

	static {
		Properties prop = new Properties();
		InputStream fis = null;
		try {
			fis =  new FileInputStream(IoTProperty.getPropPath("overloadControl_file_path") + "/overloadControl.properties");
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

	public static void setProperty(int tps) {
		Properties prop = new Properties();
		OutputStream output = null;

		try {
			output = new FileOutputStream(IoTProperty.getPropPath("overloadControl_file_path") + "/overloadControl.properties");

			prop.setProperty("overloadControlTPS", String.valueOf(tps));
			
//			if(overloadFlag)
//				prop.setProperty("overloadControlFlag", "ON");
//			else
//				prop.setProperty("overloadControlFlag", "OFF");
			
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
	
//	public static void setProperty(boolean overloadFlag) {
//		Properties prop = new Properties();
//		OutputStream output = null;
//
//		try {
//			output = new FileOutputStream(IoTProperty.getPropPath("overloadControl_file_path") + "/overloadControl.properties");
//
//			if(overloadFlag)
//				prop.setProperty("overloadControlFlag", "ON");
//			else
//				prop.setProperty("overloadControlFlag", "OFF");
//			
//			prop.store(output, null);
//		} catch (IOException io) {
//			io.printStackTrace();
//		} finally {
//			if (output != null) {
//				try {
//					output.close();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}
	
	private OverloadControlProperty(){}
	
	public static String getPropPath(String constName) {
		return propHandlerMap.get(constName);
	}
}
