package com.nextlabs.enovia.em;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.log4j.Logger;

import com.nextlabs.enovia.common.NextLabsConstant;

public class NextLabsDeploymentConfig implements NextLabsConstant {
		
	private XMLConfiguration xmlconfig = null;
	
	private static NextLabsLogger logger = null;
	
	private String fileName = null;
	
	static {		
		logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));
	}
	
	/**
	 *  Constructor for the class
	 */
	public NextLabsDeploymentConfig(String fileName) throws ConfigurationException {		
		this.fileName = fileName;
		
		logger.debug("Loading Deployment Configuration");
		
		parseConfigFile();
		
		logger.debug("Complete Loading Deployment Configuration");
	}
	
	private void parseConfigFile() throws ConfigurationException {
		logger.debug("Loading deployment configuration");
		
		xmlconfig = new XMLConfiguration(fileName);
		
		logger.debug("Loading complete file %s", fileName);
	}
	
	/**
	 * @return
	 */
	public ArrayList<HashMap<String, Object>> getAttributeList() {
		ArrayList<HashMap<String, Object>> arrList = new ArrayList<HashMap<String, Object>>();
		List<Object> types = xmlconfig.getList("custom-attributes.attribute.name");
		
		for (int i = 0; i < types.size(); i++) {
			HashMap <String,Object> attrHash = new HashMap<String, Object>();
			
			attrHash.put("name", (String) types.get(i));
			attrHash.put("type", xmlconfig.getString("custom-attributes.attribute(" + i + ").type", ""));
			attrHash.put("format", xmlconfig.getString("custom-attributes.attribute(" + i + ").format", ""));
			
			List<Object> values = xmlconfig.getList("custom-attributes.attribute(" + i + ").value");
			
			String[] stringArray = new String[values.size()];
			stringArray = values.toArray(stringArray);
			
			attrHash.put("value", stringArray);
			
			// Get the default values
			for (int k = 0; k < values.size(); k++) {
				if (null != xmlconfig.getString("custom-attributes.attribute(" + i + ").value(" + k + ")[@default]")) {
					attrHash.put("default", (String) values.get(k));
				}
			}
			
			arrList.add(attrHash);
		}
		
		logger.debug("Content values %s", arrList);
		
		return arrList;
	}
	
	/**
	 * Get the list if policy that need to be modify and apply to Enovia
	 * @param context Matrix Context
	 * @param args
	 * @return
	 */
	public ArrayList<HashMap<String, Object>> getPolicies() {
		ArrayList<HashMap<String, Object>> arrList = new ArrayList<HashMap<String, Object>>();
		List<Object> polcicyName = xmlconfig.getList("enovia-policies.policy.name");
		
		for (int i = 0; i < polcicyName.size(); i++) {
			HashMap <String,Object> attrHash = new HashMap<String, Object>();
			
			attrHash.put("name", (String) polcicyName.get(i));
			
			List<Object> values = xmlconfig.getList("enovia-policies.policy(" + i + ").role");
			String[] stringArray = new String[values.size()];
			stringArray = values.toArray(stringArray);
			
			attrHash.put("role", stringArray);
			
			values = xmlconfig.getList("enovia-policies.policy(" + i + ").state");
			
			String[] stringArrayState = new String[values.size()];
			stringArrayState = values.toArray(stringArrayState);
			
			attrHash.put("state", stringArrayState);
			
			arrList.add(attrHash);
		}
		
		logger.debug("Content values %s", arrList);
		
		return arrList;
	}
	
	/**
	 * Get the list of trigger that need to apply to Enovia from configuration file
	 * @return list of the trigger
	 */
	public ArrayList<HashMap<String, String>> getTriggers() {	
		ArrayList<HashMap<String, String>> arrList = new ArrayList<HashMap<String, String>>();
		List<Object> types = xmlconfig.getList("enovia-triggers.trigger.name");
		
		for (int i = 0; i < types.size(); i++) {
			HashMap <String,String> attrHash = new HashMap<String, String>();
			
			attrHash.put("name", (String) types.get(i));
			attrHash.put("action", xmlconfig.getString("enovia-triggers.trigger(" + i + ").action", ""));
			attrHash.put("type", xmlconfig.getString("enovia-triggers.trigger(" + i + ").type", ""));
			
			arrList.add(attrHash);
		}
		
		logger.debug("Content values %s", arrList);
		
		return arrList;
	}

}
