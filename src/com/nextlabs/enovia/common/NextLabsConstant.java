package com.nextlabs.enovia.common;
/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

/**
 * An interface for defining constant variable and configuration location
 * @author klee
 * @version: $Id: //depot/ProfesionalServices/EnoviaEntitlement/NextLabsConstant.java
 */
public interface NextLabsConstant {
	// Installation constants
	// NextLabs Trigger keyword
	public static final String NXL_TRIGGER_KEYWORD = "NextLabs";
	
	// NextLabs Policy keyword
	public static final String NXL_POLICY_KEYWORD = "NextLabsAccessCheck";
	
	// NextLabs Custom Attribute prefix
	public static final String NXL_CUST_ATTR_PREFIX = "nxl_";
	
	// Configuration file path	
	public static final String NXL_WIN_CONFIG_SUB_PATH = "\\nextlabs\\conf\\";
	
	public static final String NXL_SOL_CONFIG_SUB_PATH = "/nextlabs/conf/";
	
	// LOG4J configuration file
	public static final String LOG4J_CONFIG_FILE = "log4j.properties";
	
	// Ehcache configuration file
	public static final String EHCACHE_CONFIG_FILE = "ehcache.xml";
	
	// Runtime configuration file
	public static final String NXL_RUNTIME_FILE = "runtimeconfig.xml";
	
	// System configuration file
	public static final String NXL_SYSTEM_FILE = "systemconfig.xml";
	
	// Runtime configuration schema definition file
	public static final String NXL_RUNTIME_XSD_FILE = "runtimeconfig.xsd";
	
	// System configuration schema definition file
	public static final String NXL_SYSTEM_XSD_FILE = "systemconfig.xsd";
	
	// Deployment configuration schema definition file
	public static final String NXL_DEPLOYMENT_XSD_FILE = "deploymentconfig.xsd";
	
	// Default value for RMI port
	public static final int RMI_PORT = 1099;
	
	// Default value from connect timeout for policy control in milliseconds
	public static final int POLICY_CONTROLLER_TIMEOUT = 10000;
	
	// Runtime configuration reload interval in milliseconds
	public static final long CONFIG_RELOAD_INTERVAL = 360000;
	
	// Notification method
	public static final String NOTIFICATION_METHOD = "Notice";
	
	// Cache name for ehcache caching
	public static final String CACHE_NAME = "enovia-cache";
	
	// Response string key
	public static final String RESPONSE_KEY ="response";
	
	// Message string key
	public static final String RESPONSE_MESSAGE_KEY ="message";
	
	public static final String RESPONSE_USERMESSAGES_KEY ="UserMessages";
	
	public static final String OBLIGATION_MESSAGE_ID ="ENOVIAMSG";
	
	public static final String OBLIGATION_MESSAGE_KEY ="MessageText";
	
	// Response string value - allow
	public static final String RESPONSE_ALLOW_VALUE = "allow";
	
	// Response string value - deny
	public static final String RESPONSE_DENY_VALUE = "deny";
	
	// Response string value - error
	public static final String RESPONSE_ERROR_KEY = "error";
	
	// Unique attribute for an object in Enovia
	public static final String ATTRIBUTE_OBJECT_ID = "id";
	
	// type attribute for an object in Enovia
	public static final String ATTRIBUTE_OBJECT_TYPE = "type";
	
	// name attribute for an object in Enovia
	public static final String ATTRIBUTE_OBJECT_NAME = "name";
	
	// vault attribute for an object in Enovia
	public static final String ATTRIBUTE_OBJECT_VAULT = "vault";
	
	// revision attribute for an object in Enovia
	public static final String ATTRIBUTE_OBJECT_REV = "revision";
	
	public static final String ATTRIBUTE_IP_ADDRESS = "ip-address";
	
	public static final String BASE = "base";
	
	public static final String BASE0 = "base0";
	
	public static final String INHERITANCE_KEY = "inheritance";
	
	// Default value for connection retry count
	public static final int POLICY_CONTROLLER_RETRY_CNT = 10;
	
	// Default timer value for retry which is 600 seconds
	public static final long POLICY_CONTROLLER_RETRY_TIMER = 600000;
	
	public static final String VERSION_POLICY = "Version";
	
	// Base Attributes for all Business Object
	public static final String EM_ALL_TYPE = "Base BO";

	public static final String EM_ALL_RELTYPE = "Base Relationship";
	
	public static final String SUBJECT_EXTENSION_USER = "User";
	public static final String SUBJECT_EXTENSION_APP = "Application";
	
	public static final String RESOURCE_TYPE_ENOVIA = "enovia";
	
	public static final short RECURSIVE_LEVEL = 1;
}
