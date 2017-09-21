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

import com.kt.restful.service.ApnsService;

public class LogFlagProperty {
	private static Map<String, String> propHandlerMap = new HashMap<String, String>();
	
	private static Logger logger = LogManager.getLogger(LogFlagProperty.class);

	static {
		Properties prop = new Properties();
		InputStream fis = null;
		try {
			fis =  new FileInputStream(IoTProperty.getPropPath("log_flag_file_path") + "/logFlag.properties");
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

	public static void setProperty(boolean overloadFlag) {
		Properties prop = new Properties();
		OutputStream output = null;

		try {
			output = new FileOutputStream(IoTProperty.getPropPath("log_flag_file_path") + "/logFlag.properties");

			if(overloadFlag)
				prop.setProperty("log_flag", "ON");
			else
				prop.setProperty("log_flag", "OFF");
			
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
	
	private LogFlagProperty(){}
	
	public static String getPropPath(String constName) {
		propHandlerMap = new HashMap<String, String>();
		propHandlerMap.clear();
		
		Properties prop = new Properties();
		InputStream fis = null;
		try {
			fis =  new FileInputStream(IoTProperty.getPropPath("log_flag_file_path") + "/logFlag.properties");
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
			String constName1 = (String) keyIter.next();
			String path = prop.getProperty(constName1);
			propHandlerMap.put(constName1, path);
		}  
		
		return propHandlerMap.get(constName);
	}
}
