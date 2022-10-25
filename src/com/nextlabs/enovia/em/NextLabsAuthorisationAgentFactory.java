package com.nextlabs.enovia.em;
/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */
import java.util.HashMap;import com.nextlabs.enovia.common.NextLabsConstant;
/**
 * @author klee
 * @version: $Id: //depot/ProfesionalServices/EnoviaEntitlement/NextLabsAuthorisationAgentFactory.java
 */
public class NextLabsAuthorisationAgentFactory implements NextLabsConstant {	
	public static final String ATTR_KEY_SERVER = "server";	
	public static final String ATTR_KEY_APPLICATION = "application";	
	public static final String ATTR_KEY_NOCACHE = "ce::nocache";	
	public static final String ATTR_VALUE_YES = "yes";	
	public static final String ATTR_KEY_URL = "url";	
	/**
	 * For determine whether user is granted access to specific type
	 * @param username Username for the user that will be allow/deny access
	 * @param action Action deformed by the user.
	 * @param attributes HashMap of attributes which contain all the object attributes that will be pass to Nextlabs CE SDK
	 * @return HashMap of the response, contain response string and also the obligation information
	 * @throws Exception
	 */
	public HashMap<String, Object> hasAccess(String username, String action, HashMap<String, Object> attributes) throws Exception {
		return null;
	}		/**	 * For determine whether user is granted access to specific type	 * @param username Username for the user that will be allow/deny access	 * @param action Action deformed by the user.	 * @param attributes HashMap of attributes which contain all the object attributes that will be pass to Nextlabs CE SDK	 * @return HashMap of the response, contain response string and also the obligation information	 * @throws Exception	 */	public HashMap<String, Object> hasAccess(String username, String action, 			HashMap<String, Object> attributes, HashMap<String, Object> userAttributes, 			HashMap<String, Object> appAttributes) throws Exception {		return null;	}	
	/**
	 * Calling to NextLabs CE SDK to validate the access
	 * @param username Username for the user that will be allow/deny access
	 * @param action Action performed by the user.
	 * @param attributes HashMap of attributes which contain all the object attributes that will be pass to Nextlabs CE SDK
	 * @return HashMap of the response, contain response string and also the obligation information
	 * @throws Exception
	 */
	public HashMap<String, Object> checkNxlPolicy(String username, String action, 			HashMap<String, Object> attributes, HashMap<String, Object> userAttributes, 			HashMap<String, Object> appAttributes) throws Exception {		return null;	}	
	/**	 * For input validation	 * @param username	 * @param action	 * @param attributes	 * @return	 */
	protected HashMap<String, Object> inputValidation(String username, String action, 			HashMap<String, Object> attributes, NextLabsRuntimeConfig config) {
		HashMap<String, Object> result = new HashMap<String, Object>();		
		if (username == null ) {
			result.put(RESPONSE_ERROR_KEY, "userName is null");
			result.put(RESPONSE_KEY, config.getDefaultAction());  			
			if (null != config.getDefaultMessage()) {				result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());			}						return result;
		} else if (action == null) {
			result.put(RESPONSE_ERROR_KEY, "action is null");
			result.put(RESPONSE_KEY, config.getDefaultAction());  			
			if (null != config.getDefaultMessage()) {
				result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
			}			
			return result;
		} else if (attributes == null) {
			result.put(RESPONSE_ERROR_KEY, "attributes is null");
			result.put(RESPONSE_KEY, config.getDefaultAction()); 			
			if (null != config.getDefaultMessage()) {
				result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
			}			
			return result;
		}		
		return result;
	}
	/**
	 * For input validation
	 * @param attributes
	 * @return
	 */
	protected HashMap<String, Object> inputValidation(HashMap<String, Object> attributes, NextLabsRuntimeConfig config) {		HashMap<String, Object> result = new HashMap<String, Object>();		
		if (attributes == null) {
			result.put(RESPONSE_ERROR_KEY, "attributes is null");
			result.put(RESPONSE_KEY, config.getDefaultAction()); 			
			if (null != config.getDefaultMessage()) {
				result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
			}			
			return result;
		}		
		return result;
	}
	protected String buildResourceString(String sHostName, String sAppName, String sVault, String sType, String sName) {
		if (null == sVault) {
			sVault = "*";
		}		
		if (null == sType) {
			sType = "*";
		}		
		if (null == sName) {
			sName = "*";
		}		
		StringBuffer sBuf = new StringBuffer();		
		sBuf.append("enovia://")			.append(sHostName).append("/")			.append(sAppName).append("/")			.append(sVault).append("/")			.append(sType).append("/")			.append(sName);		
		return sBuf.toString();
	}
}