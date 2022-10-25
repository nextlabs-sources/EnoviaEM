package com.nextlabs.enovia.em;

/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

import com.nextlabs.enovia.common.NextLabsConstant;

/**
 * @author klee
 * @version: $Id: //depot/ProfesionalServices/EnoviaEntitlement/NextLabsSystemConfig.java
 */
public class NextLabsSystemConfig implements NextLabsConstant {
	
	private static NextLabsSystemConfig sysConfig;
	
	private static XMLConfiguration xmlconfig = null;
	
	private static NextLabsLogger logger = null;
	
	private static String sConfigPath = null;
	
	static {
		sConfigPath = NextLabsAgentUtil.getConfigPath();
		
		logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));
	}
	
	/**
	 *  Constructor for the class
	 */
	private NextLabsSystemConfig() {		
		
	}
		
	/**
	 * Get the filtering string that will be plug into policy files
	 * @return Filter string 
	 */
	public String getNextLabsFilterString() {
		return xmlconfig.getString("enovia-filter.value", "");
	}
	
	/**
	 * Get the trigger string in MxUpdate format.
	 * @return Trigger string
	 */
	public String getTriggerString() {
		return xmlconfig.getString("trigger", "");
	}
	
	/**
	 * Singleton control which return the instance
	 * @return nxlRuntimeConfig_mxJPO
	 */
	public static synchronized NextLabsSystemConfig getInstance() {
		if (sysConfig == null) {
			logger.debug("Loading System Configuration");
			
			sysConfig = new NextLabsSystemConfig();
			sysConfig.parseConfigFile();
			
			logger.debug("Complete Loading System Configuration");
		}
		
		return sysConfig;
	}
	
	public void parseConfigFile() {
		try {
			logger.debug("Loading system configuration");
			
			xmlconfig = new XMLConfiguration(sConfigPath + NXL_SYSTEM_FILE);
			
			logger.debug("Loading complete file %s%s", sConfigPath, NXL_SYSTEM_FILE);
			
			FileChangedReloadingStrategy reloadPolicy = new FileChangedReloadingStrategy();
			
			xmlconfig.setReloadingStrategy(reloadPolicy);
		} catch (Exception e) {
			logger.error("NextLabsSystemConfig() caught exception: %s", e.fillInStackTrace(), e.getMessage());
		}
	}
	
}
