package com.nextlabs.enovia.em;

/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.configuration.reloading.FileChangedReloadingStrategy;
import org.apache.log4j.Logger;

import com.nextlabs.enovia.common.NextLabsConstant;
import com.nextlabs.enovia.em.extension.NextLabsResourceExtension;
import com.nextlabs.enovia.em.extension.NextLabsSubjectExtension;

/**
 * @author klee
 * @version: $Id: //depot/ProfesionalServices/EnoviaEntitlement/NextLabsRuntimeConfig.java
 */
public class NextLabsRuntimeConfig implements NextLabsConstant {
	
	private static NextLabsRuntimeConfig runtimeConfig;
	private static XMLConfiguration xmlconfig = null;
	private static NextLabsLogger logger = null;
	private static String sConfigPath = null;
	
	static {
		sConfigPath = NextLabsAgentUtil.getConfigPath();
		
		logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));
	}
	
	/*
	<inheritance-list>
		<inheritance>
			<name>Part Specification</name>
			<from-type>Part</from-type>
			<to-type>CAD Drawing</to-type>
			<relationship>to</relationship>
		</inheritance>
	</inheritance-list>
	 */
	/**
	 * @param sChildType Object type for the relationship
	 * @return true if the relationship is set in configuration file, false if not found
	 */
	public boolean getInheritanceDirectionFromParentToChild(String sChildType) {
		List<Object> types = xmlconfig.getList("inheritance-list.inheritance.to-type");
		
		if (types.contains(sChildType)) {
			return true;
		}
		
		return false;
	}
	
	/*
	<type-protected-relationships>
		<type>
			<name>Part</name>
			<relationships-from>
				<name>Part Specification</name>
				<name>EBOM</name>
			</relationships-from>
			<relationships-to>
			</relationships-to>
		</type>
	</type-protected-relationships>
	 */
	/**
	 * @param sType Object type for the Business Object
	 * @param sRelType Name of the protected relationship
	 * @param sConnection Connection type of the protected relationship, From/Or
	 * @return true if the relationship is protected, otherwise false
	 */
	public boolean isProtectedRelationships(String sType, String sRelType, String sConnection) {
		List<Object> types = xmlconfig.getList("type-protected-relationships.type.name");
		boolean result = false;
		
		if (types.contains(sType)) {
			int i = types.indexOf(sType);
			List<Object> rels = xmlconfig.getList("type-protected-relationships.type(" + i + ").relationships-" + 
					sConnection.toLowerCase() + ".name");
			
			if (rels.contains(sRelType) || rels.contains(EM_ALL_RELTYPE)) {
				result = true;
			}
		}
		
		return result;
	}
	
	/*
	<inheritance-list>
		<inheritance>
			<name>Part Specification</name>
			<from-type>Part</from-type>
			<to-type>CAD Drawing</to-type>
			<relationship>to</relationship>
		</inheritance>
	</inheritance-list>
	 */
	/**
	 * @param sChildType Object type for the relationship
	 * @return Name of the relationship with from type e.g "Part Specification:Part"
	 */
	public ArrayList<String> getInheritanceRelationship(String sChildType) {
		List<Object> types = xmlconfig.getList("inheritance-list.inheritance.to-type");
		ArrayList<String> arrResult = new ArrayList<String>();
		int k = 0;
		
		for (Object obj: types) {
			//logger.debug("Object data is %s", obj.toString());
			
			if (obj.toString().equals(sChildType)) {
				arrResult.add(
						xmlconfig.getString("inheritance-list.inheritance("+k+").name", "") + ":" +
						xmlconfig.getString("inheritance-list.inheritance("+k+").from-type", "")
					);
			}
			
			k++;
		}
		
		return arrResult;
	}
	
	/*
	<inheritance-list>
		<inheritance>
			<name>Part Specification</name>
			<from-type>Part</from-type>
			<to-type>CAD Drawing</to-type>
			<relationship>to</relationship>
		</inheritance>
	</inheritance-list>
	 */
	/**
	 * @param sChildType Object type for the relationship
	 * @return Parent object type which related to the child object
	 */
	public ArrayList<String> getInheritanceParentType(String sChildType) {
		List<Object> types = xmlconfig.getList("inheritance-list.inheritance.to-type");
		ArrayList<String> arrResult = new ArrayList<String>();
		int k = 0;
		
		for (Object obj: types) {
			//logger.debug("Object data is %s", obj.toString());
			
			if (obj.toString().equals(sChildType)) {
				arrResult.add(xmlconfig.getString("inheritance-list.inheritance("+k+").from-type", ""));
			}
			
			k++;
		}
		
		return arrResult;
	}
	
	/*
	<policy-controller-attributes>
		<data-format>
			<em-prefix>enovia-</em-prefix>
			<extension-prefix>enoviaext-</extension-prefix>
		</data-format>
		<attribute>
			<type>Part</type>
			<baseattr>type</baseattr>
			<baseattr>name</baseattr>
			<baseattr>vault</baseattr>
			<customattr>IPC-Type</customattr>
			<customattr>IPC-Data</customattr>
			<customattr>revision</customattr>
			<customattr>nxl_Access_Classification</customattr>
		</attribute>
	</policy-controller-attributes>
	 */
	/**
	 * @param sType Enovia object type
	 * @return String array which contain base attribute for the object type
	 */
	public String[] getBaseAttr(String sType) {
		List<Object> types = xmlconfig.getList("policy-controller-attributes.attribute.type");
		
		if (types.contains(sType)) {
			int i = types.indexOf(sType);
			List<Object> attr = xmlconfig.getList("policy-controller-attributes.attribute("+i+").baseattr");
			
			logger.debug("Return baseattr -->%s", attr);			
			String[] stringArray = new String[attr.size()];
			stringArray = attr.toArray(stringArray);
			return (stringArray);
		}
		
		return (new String[0]);
	}
	
	/*
	<policy-controller-attributes>
		<data-format>
			<em-prefix>enovia-</em-prefix>
			<extension-prefix>enoviaext-</extension-prefix>
		</data-format>
		<attribute>
			<type>Part</type>
			<baseattr>type</baseattr>
			<baseattr>name</baseattr>
			<baseattr>vault</baseattr>
			<customattr>IPC-Type</customattr>
			<customattr>IPC-Data</customattr>s
			<customattr>revision</customattr>
			<customattr>nxl_Access_Classification</customattr>
		</attribute>
	</policy-controller-attributes>
	 */
	/**
	 * @param sType Enovia object type
	 * @return String array which contain base attribute for the object type
	 */
	public String[] getCustomAttr(String sType) {
		List<Object> types = xmlconfig.getList("policy-controller-attributes.attribute.type");
		
		if (types.contains(sType)) {
			int i = types.indexOf(sType);
			List<Object> attr = xmlconfig.getList("policy-controller-attributes.attribute("+i+").customattr");
			
			logger.debug("Return customattr -->%s", attr);			
			String[] stringArray = new String[attr.size()];
			stringArray = attr.toArray(stringArray);			 
			return (stringArray);
		}
		
		return (new String[0]);
	}
	
	/*
	<policy-controller-attributes>
		<data-format>
			<em-prefix>enovia-</em-prefix>
			<extension-prefix>enoviaext-</extension-prefix>
		</data-format>
		<attribute>
			<type>Part</type>
			<baseattr>type</baseattr>
			<baseattr>name</baseattr>
			<baseattr>vault</baseattr>
			<customattr>IPC-Type</customattr>
			<customattr>IPC-Data</customattr>s
			<customattr>revision</customattr>
			<customattr>nxl_Access_Classification</customattr>
		</attribute>
	</policy-controller-attributes>
	 */
	/**
	 * Function to return prefix for em data
	 * @return String array which contain base attribute for the object type
	 */
	public String getEMAttrPrefix() {
		try {
			return xmlconfig.getString("policy-controller-attributes.data-format.em-prefix", "");
		} catch (Exception ex) {}
		
		return "";
	}
	
	/*
	<policy-controller-attributes>
		<data-format>
			<em-prefix>enovia-</em-prefix>
			<extension-prefix>enoviaext-</extension-prefix>
		</data-format>
		<attribute>
			<type>Part</type>
			<baseattr>type</baseattr>
			<baseattr>name</baseattr>
			<baseattr>vault</baseattr>
			<customattr>IPC-Type</customattr>
			<customattr>IPC-Data</customattr>s
			<customattr>revision</customattr>
			<customattr>nxl_Access_Classification</customattr>
		</attribute>
	</policy-controller-attributes>
	 */
	/**
	 * Function to return prefix for em data
	 * @return String array which contain base attribute for the object type
	 */
	public String getExtensionAttrPrefix() {
		try {
			return xmlconfig.getString("policy-controller-attributes.data-format.extension-prefix", "");
		} catch (Exception ex) {}
		
		return "";
	}
	
	/*
	<policy-controller>
		<host>127.0.0.1</host>
		<appname>Engineering Central</appname>
		<timeout>10000</timeout>
		<type>webjax</type>
		<web-service-url>http://10.23.57.90/gsoap/mod_gsoap.dll?wssdkserver</web-service-url>
	</policy-controller>
	 */
	/**
	 * @return Application name 
	 */
	public String getAppName() {
		return xmlconfig.getString("policy-controller.appname", "");
	}
	
	/*
	<policy-controller>
        <config-file>openaz-pep-on-prem.properties</config-file>
        <appname>Engineering Central</appname>
        <default-action>allow</default-action>
        <default-message>PDP connection timeout</default-message>
        <connect-retry-count>5</connect-retry-count>
        <connect-retry-timer>100000</connect-retry-timer>
    </policy-controller>
	 */
	public String getPDPConfigFileName() {
		return xmlconfig.getString("policy-controller.config-file", "openaz-pep-on-prem.properties");
	}
		
	
	/*
	<policy-controller>
        <config-file>openaz-pep-on-prem.properties</config-file>
        <appname>Engineering Central</appname>
        <default-action>allow</default-action>
        <default-message>PDP connection timeout</default-message>
        <connect-retry-count>5</connect-retry-count>
        <connect-retry-timer>100000</connect-retry-timer>
    </policy-controller>
	 */
	/**
	 * @return Policy Controller Default action
	 */
	public String getDefaultAction() {
		return xmlconfig.getString("policy-controller.default-action", "deny");
	}
	
	/**
	 * @return Policy Controller Default action when decision is NotApplicable
	 */
	public String getDefaultNotApplicableAction() {
		return xmlconfig.getString("policy-controller.default-na-action", "deny");
	}
	
	/**
	 * @return Policy Controller Default action when decision is Indeterminate
	 */
	public String getDefaultIndeterminateAction() {
		return xmlconfig.getString("policy-controller.default-indeterminate-action", "deny");
	}
	
	
	/*
	<policy-controller>
        <config-file>openaz-pep-on-prem.properties</config-file>
        <appname>Engineering Central</appname>
        <default-action>allow</default-action>
        <default-message>PDP connection timeout</default-message>
        <connect-retry-count>5</connect-retry-count>
        <connect-retry-timer>100000</connect-retry-timer>
    </policy-controller>
	 */
	/**
	 * @return Policy Controller Default message
	 */
	public String getDefaultMessage() {
		return xmlconfig.getString("policy-controller.default-message", null);
	}
	
	/**
	 * @return Policy Controller Default message when decision is NotApplicable
	 */
	public String getDefaultNotApplicableMessage() {
		return xmlconfig.getString("policy-controller.default-na-message", null);
	}
	
	/**
	 * @return Policy Controller Default message when decision is Indeterminate
	 */
	public String getDefaultIndeterminateMessage() {
		return xmlconfig.getString("policy-controller.default-indeterminate-message", null);
	}
	
	
	
	/*
	<policy-controller>
        <config-file>openaz-pep-on-prem.properties</config-file>
        <appname>Engineering Central</appname>
        <default-action>allow</default-action>
        <default-message>PDP connection timeout</default-message>
        <connect-retry-count>5</connect-retry-count>
        <connect-retry-timer>100000</connect-retry-timer>
    </policy-controller>
	 */
	/**
	 * @return POlicy Controller time out value
	 */
	public int getPolicyControllerTimeOut() {
		try {
			return xmlconfig.getInt("policy-controller.timeout", POLICY_CONTROLLER_TIMEOUT);
		} catch (Exception ex) {}
		
		return POLICY_CONTROLLER_TIMEOUT;
	}
	
	/*
	<policy-controller>
        <config-file>openaz-pep-on-prem.properties</config-file>
        <appname>Engineering Central</appname>
        <default-action>allow</default-action>
        <default-message>PDP connection timeout</default-message>
        <connect-retry-count>5</connect-retry-count>
        <connect-retry-timer>100000</connect-retry-timer>
    </policy-controller>
	 */
	/**
	 * @return Policy controller connect retry count
	 */
	public int getPolicyControllerConnectRetryCnt() {
		try {
			return xmlconfig.getInt("policy-controller.connect-retry-count", POLICY_CONTROLLER_RETRY_CNT);
		} catch (Exception ex) {}
		
		return POLICY_CONTROLLER_RETRY_CNT;
	}
	
	/*
	<policy-controller>
        <config-file>openaz-pep-on-prem.properties</config-file>
        <appname>Engineering Central</appname>
        <default-action>allow</default-action>
        <default-message>PDP connection timeout</default-message>
        <connect-retry-count>5</connect-retry-count>
        <connect-retry-timer>100000</connect-retry-timer>
    </policy-controller>
	 */
	/**
	 * @return Policy controller connect retry timer
	 */
	public long getPolicyControllerConnectRetryTimer() {
		try {
			return xmlconfig.getLong("policy-controller.connect-retry-timer", POLICY_CONTROLLER_RETRY_TIMER);
		} catch (Exception ex) {}
		
		return POLICY_CONTROLLER_RETRY_TIMER;
	}
	
	
	/*
	<system>
		<config-reload-interval>36000</config-reload-interval>
	</system>
	 */
	/**
	 * @return Configuration file reload interval
	 */
	public long getConfigReloadInterval() {
		try {
			return xmlconfig.getLong("system.config-reload-interval", CONFIG_RELOAD_INTERVAL);
		} catch (Exception ex) {}
		
		return CONFIG_RELOAD_INTERVAL;
	}
	
	/*
	<system>
		<notification-method>Notice</notification-method>
	</system>
	 */
	/**
	 * @return notification display way, either Notice or FrameworkException
	 */
	public String getNotificationMethod() {
		return xmlconfig.getString("system.notification-method", NOTIFICATION_METHOD);
	}
	
	/*
	<system>
		<replace-user-agent>Notice</replace-user-agent>
	</system>
	 */
	/**
	 * @return true/false, true if replace user-agent to real login id, false will skip checking
	 */
	public String getReplaceUserAgent() {
		return xmlconfig.getString("system.replace-user-agent", "false");
	}
	
	/*
	<system>
		<username-to-lower>false</username-to-lower>
	</system>
	 */
	/**
	 * @return true/false, true if username will be convert to lowercase
	 */
	public String getUsernameToLowerCase() {
		return xmlconfig.getString("system.username-to-lower", "true");
	}
		
	/*
	<extensions>
		<extension type="resource">
			<class>com.nextlabs.enovia.extension.impl.CADModelExtension</class>
			<business-object-type>CAD Model</business-object-type>
		</extension>
	</extensions>
	 */
	/**
	 * @param sType Enovia Object Type
	 * @return Extension which contains the extension details of the object type
	 */
	public NextLabsResourceExtension getResourceExtension(String sType) {
		List<Object> objects = xmlconfig.getList("extensions.extension[@type]");
		NextLabsResourceExtension extension = null;
		
		int k = 0;
		for (Object obj : objects) {
			if (obj.toString().equalsIgnoreCase("resource")) {
				String boType = xmlconfig.getString("extensions.extension(" + k + ").business-object-type", "");
				
				if (boType.equals(sType)) {
					String extClass = xmlconfig.getString("extensions.extension(" + k + ").class", "");
					
					extension = new NextLabsResourceExtension(extClass, boType);
					break;
				}
			}
			
			k++;
		}
		
		return extension;
	}
	
	/**
	 * @return Array List which contains the extension details of all the object type
	 */
	public ArrayList<NextLabsResourceExtension> getResourceExtensions() {
		List<Object> objects = xmlconfig.getList("extensions.extension[@type]");
		ArrayList<NextLabsResourceExtension> extensions = new ArrayList<NextLabsResourceExtension>();
		
		int k = 0;
		for (Object obj : objects) {
			if (obj.toString().equalsIgnoreCase("resource")) {
				String boType = xmlconfig.getString("extensions.extension(" + k + ").business-object-type", "");
				String extClass = xmlconfig.getString("extensions.extension(" + k + ").class", "");
				
				NextLabsResourceExtension extension = new NextLabsResourceExtension(extClass, boType);
				extensions.add(extension);
			}
			
			k++;
		}
		
		return extensions;
	}
	
	/*
	<extensions>
		<extension type="subject">
			<class>com.nextlabs.enovia.extension.impl.UserExtension</class>
			<subject-type>User</subject-type>
		</extension>
	</extensions>
	 */
	/**
	 * @param sType Subject Type, either User or Application
	 * 
	 */
	public NextLabsSubjectExtension getSubjectExtension(String sType) {
		List<Object> objects = xmlconfig.getList("extensions.extension[@type]");
		NextLabsSubjectExtension result = null;
		
		int k = 0;
		for (Object obj : objects) {
			if (obj.toString().equalsIgnoreCase("subject")) {
				String subjectType = xmlconfig.getString("extensions.extension(" + k + ").subject-type", "");
				
				if (subjectType.equals(sType)) {
					String extClass = xmlconfig.getString("extensions.extension(" + k + ").class", "");
					
					result = new NextLabsSubjectExtension(extClass, subjectType);
					break;
				}
			}
			
			k++;
		}
		
		return result;
	}
	
	/**
	 *  Constructor for the class
	 */
	private NextLabsRuntimeConfig() {		
		
	}
	
	/**
	 * Singleton control which return the instance
	 * @return nxlRuntimeConfig_mxJPO
	 */
	public static synchronized NextLabsRuntimeConfig getInstance() {
		if (runtimeConfig == null) {
			logger.debug("Loading Runtime Configuration");
			
			runtimeConfig = new NextLabsRuntimeConfig();
			runtimeConfig.parseConfigFile();
			
			logger.debug("FINISHED Loading Runtime Configuration");
		}
		
		return runtimeConfig;
	}
	
	/**
	 * Loading xml config files and setting the config file reloading policy.
	 * The reloading is based on configuration in the config file
	 */
	public void parseConfigFile() {
		try {
			logger.debug("Loading new configuration");
			
			xmlconfig = new XMLConfiguration(sConfigPath + NXL_RUNTIME_FILE);
			FileChangedReloadingStrategy reloadPolicy = new FileChangedReloadingStrategy();
			reloadPolicy.setRefreshDelay(getConfigReloadInterval());
			xmlconfig.setReloadingStrategy(reloadPolicy);
			
			// register configuration listener
			NextLabsConfigListener configListener = new NextLabsConfigListener();
			xmlconfig.addConfigurationListener(configListener);
		} catch (Exception e) {
			logger.error("parseConfigFile() caught exception: %s", e.fillInStackTrace(), e.getMessage());
		}
	}
	
}
