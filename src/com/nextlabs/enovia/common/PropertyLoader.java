package com.nextlabs.enovia.common;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PropertyLoader {

	private static final Log LOG = LogFactory.getLog(PropertyLoader.class);

	public static Properties loadPropertiesDirectly(String filePath) {
		Properties result = null;
		try {
			File file = new File(filePath);
			LOG.info("Properties File Path:: " + file.getAbsolutePath());
			if (file != null) {
				FileInputStream fis = new FileInputStream(file);
				result = new Properties();
				result.load(fis); // Can throw IOException
			}
		} catch (Exception e) {
			LOG.error("Error parsing properties file ", e);
			result = null;
		}
		return result;
	}
}
