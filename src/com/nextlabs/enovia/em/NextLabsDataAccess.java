package com.nextlabs.enovia.em;
/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import org.apache.log4j.Logger;

import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.nextlabs.enovia.common.NextLabsConstant;
import com.nextlabs.enovia.em.extension.NextLabsExtensionAgent;
import com.nextlabs.enovia.em.extension.NextLabsResourceExtension;
import com.nextlabs.enovia.em.extension.NextLabsSubjectExtension;

/**
 * @author klee
 * @version: $Id: //depot/ProfesionalServices/EnoviaEntitlement/NextLabsDataAccess.java
 */
public final class NextLabsDataAccess implements Serializable, NextLabsConstant {

	// Unique Serialization ID
	private static final long serialVersionUID = -2932799409322653191L;
	
	// Log4j logging initialization
	private static NextLabsLogger logger = null;
	
	// Configuration class for getting runtime configuration from XML file
	private static NextLabsRuntimeConfig config = null;
	
	// Extension Agent for retrieve extension details
	private static NextLabsExtensionAgent extensionAgent = null;
	
	// String prefix for custom attribute
	private static final String CUSTOM_ATTR_PREFIX = "attribute[";
	
	// String suffix for custom attribute
	private static final String CUSTOM_ATTR_SUFFIX = "].value";
	
	private static final String METHOD_GET_DATA = "getData";
	
	private static final boolean GET_TO = true;
	
	private static final boolean GET_FROM = false;
	
	private static final String OBJECT_WHERE = "";
	
	private static final String RELATIONSHIP_WHERE = "";
	
	private static final String INHERITANCE_RELATIONSHIP_NAME = "Latest Version";
		
	// DataAcess instance for singleton control
	private static NextLabsDataAccess nDataAccess = null;
	
	static {
		logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));

		config = NextLabsRuntimeConfig.getInstance();
		
		extensionAgent = NextLabsExtensionAgent.getInstance();
	}

	/**
	 * Constructor for the class.
	 * @throws Exception
	 */
	private NextLabsDataAccess() {                        
		        
	}
	
	/**
	 * Singleton control for the data access.
	 * @return void
	 */
	public static synchronized NextLabsDataAccess getInstance() {
		if (nDataAccess == null) {
			nDataAccess = new NextLabsDataAccess();
		}

		return nDataAccess;
	}

	/**
	 * Query the parent object attributes. Inheritance based information is obtain from here
	 * @param context Enovia Matrix Context
	 * @param childObjectId Object ID from Enovia for the object that will be query
	 * @param inheritanceRelationship Relationship name e.g "Part Specification"
	 * @param sType	Object type e.g "Part", "Documents"
	 * @return MapList with list of parent object related to the query object
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<MapList> getParentData(Context context, String childObjectId, 
			ArrayList<String> inheritanceRelationship, String sType) throws Exception {

		ArrayList<MapList> arrMapList = new ArrayList<MapList>();
		
		for (String sRelationShip : inheritanceRelationship) {
			logger.debug("Checking for relationshipPattern: %s", sRelationShip);

			String typePattern = "*";
			StringList selectList = new StringList();

			// Set to get the object id from enovia
			selectList.addElement(ATTRIBUTE_OBJECT_ID);
			
			String[] sData = sRelationShip.split(":");		
			logger.debug("Relationship name: %s | BO type: %s", sData[0], sData[1]);
			
			// Setup base attributes for all object type
			String[] configBaseAttributes_all = config.getBaseAttr(EM_ALL_TYPE);
			String[] configCustomAttributes_all = config.getCustomAttr(EM_ALL_TYPE);
			
			selectList = setupSelectList(selectList, configBaseAttributes_all, configCustomAttributes_all);
			// end of setup base attributes for all object type

			String[] configBaseAttributes = config.getBaseAttr(sData[1]);			
			String[] configCustomAttributes = config.getCustomAttr(sData[1]);
			
			selectList = setupSelectList(selectList, configBaseAttributes, configCustomAttributes);

			StringList relationshipSelects = new StringList();

			DomainObject dom = new DomainObject(childObjectId);

			MapList parentMaps = dom.getRelatedObjects(
					context,
					sData[0],
					typePattern,
					selectList,
					relationshipSelects,
					GET_TO,
					GET_FROM,
					NextLabsConstant.RECURSIVE_LEVEL,
					OBJECT_WHERE,
					RELATIONSHIP_WHERE, 0);

			logger.debug("parentMaps.size() is %d", parentMaps.size());
			
			
			if(parentMaps.size() > 0)
				arrMapList.add(parentMaps);

			// Just for debugging
			Map<String, Object> parentMap = null;

			for (Iterator<?> itrRouteNodes = parentMaps.iterator(); itrRouteNodes.hasNext();) {
				parentMap = (Map<String, Object>) itrRouteNodes.next();
				
				logger.debug("Before conversion parentMap: %s", parentMap);

				parentMap = convertToPolicyControllerFormat(parentMap, configCustomAttributes_all, 
						configBaseAttributes_all, configCustomAttributes, configBaseAttributes);

				logger.debug("After conversion parentMap: %s", parentMap);
			}
		}

		return arrMapList;
	}
	
	/**
	 * Query the parent object attributes. Inheritance based information is obtain from here
	 * This method is for getting the file item parent object
	 * @param context Enovia Matrix Context
	 * @param childObjectId Object ID from Enovia for the object that will be query
	 * @param sType	Object type e.g "Part", "Documents"
	 * @return MapList with list of parent object related to the query object
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public MapList getFixParentData(Context context, String childObjectId, String sType) 
			throws Exception {
		
		// Use relationship Latest Version to get the parent information
		logger.debug("relationshipPattern: %s", INHERITANCE_RELATIONSHIP_NAME);

		String typePattern = "*";
		StringList selectList = new StringList();
		
		// Set to get the object id from Enovia
		selectList.addElement(NextLabsConstant.ATTRIBUTE_OBJECT_ID);
		
		// Setup base attributes for all object type
		String[] configBaseAttributes_all = config.getBaseAttr(EM_ALL_TYPE);
		String[] configCustomAttributes_all = config.getCustomAttr(EM_ALL_TYPE);
		
		selectList = setupSelectList(selectList, configBaseAttributes_all, configCustomAttributes_all);
		// end of setup base attributes for all object type

		String[] configBaseAttributes = config.getBaseAttr(sType);
		String[] configCustomAttributes = config.getCustomAttr(sType);
		
		selectList = setupSelectList(selectList, configBaseAttributes, configCustomAttributes);
		
		StringList relationshipSelects = new StringList();
		DomainObject dom = new DomainObject(childObjectId);

		MapList parentMaps = dom.getRelatedObjects(
				context,
				INHERITANCE_RELATIONSHIP_NAME,
				typePattern,
				selectList,
				relationshipSelects,
				GET_TO,
				GET_FROM,
				NextLabsConstant.RECURSIVE_LEVEL,
				OBJECT_WHERE,
				RELATIONSHIP_WHERE, 0);

		logger.debug("parentMaps.size()" + parentMaps.size());
		
		// Just for debugging
		Map<String, Object> parentMap = null;

		for (Iterator<?> itrRouteNodes = parentMaps.iterator(); itrRouteNodes.hasNext();) {
			parentMap = (Map<String, Object>)itrRouteNodes.next();

			logger.debug("Before conversion directparentMap: %s", parentMap);
			
			parentMap = convertToPolicyControllerFormat(parentMap, 
					configCustomAttributes_all, configBaseAttributes_all, 
					configCustomAttributes, configBaseAttributes);
			
			logger.debug("After conversion directparentMap: %s", parentMap);
		}

		return parentMaps;
	}
	
	/**
	 * Get the attributes values needed for passing to NextLabs CE SDK
	 * R1.0 interface which is consuming by customer: RCI
	 * @param context Enovia Matrix Context
	 * @param sObjectID Enovia object id that use to query
	 * @param sUserId User ID that perform the access
	 * @return HaspMap that contain the object attribute and parent object list, if exist
	 * @throws Exception
	 */
	public Map<String, Object> getData(Context context, String sObjectID, String sUserId)
			throws Exception {
		return getData(context, sObjectID, sUserId, new HashMap<String, Object>());
	}

	/**
	 * Get the attributes values needed for passing to NextLabs CE SDK
	 * R1.1 interface
	 * @param context Enovia Matrix Context
	 * @param sObjectID Enovia object id that use to query
	 * @param sUserId User ID that perform the access
	 * @param macros contains hash map that holds the macros (key & value) from JPO
	 * @return HaspMap that contain the object attribute and parent object list, if exist
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getData(Context context, String sObjectID, String sUserId, 
			HashMap<String, Object> macros) throws FrameworkException, Exception {  	

		Map<String, Object> accessAttributes = null;
		Map<String, Object> dataSending = null;

		try {
			DomainObject dObject = new DomainObject(sObjectID);

			String sType = dObject.getInfo(context, DomainConstants.SELECT_TYPE);
			String name = dObject.getInfo(context, DomainConstants.SELECT_NAME);
			String rev = dObject.getInfo(context, DomainConstants.SELECT_REVISION);
			String sIPAddress = context.getIPAddress();
						
			// If no IP-address found then we hard coded to local, we need that
			if (sIPAddress == null) {
				sIPAddress = "127.0.0.1";
			}
			
			logger.debug("User is %s; Check object: %s | %s | %s", sUserId, sType, name, rev);
			logger.debug("Macros: %s", macros.toString());
			
			StringList selectList = new StringList();
			
			// Setup base attributes for all object type
			String[] configBaseAttributes_all = config.getBaseAttr(EM_ALL_TYPE);
			String[] configCustomAttributes_all = config.getCustomAttr(EM_ALL_TYPE);
			
			selectList = setupSelectList(selectList, configBaseAttributes_all, configCustomAttributes_all);
			// end of setup base attributes for all object type
						
			String[] configBaseAttributes = config.getBaseAttr(sType);
			String[] configCustomAttributes = config.getCustomAttr(sType);

			selectList = setupSelectList(selectList, configBaseAttributes, configCustomAttributes);
			
			accessAttributes = dObject.getInfo(context, selectList);
			
			// Performing conversion from key with name [attribute[IPC-Type].value] to IPC-Type
			accessAttributes = convertToPolicyControllerFormat(accessAttributes, 
					configCustomAttributes_all, configBaseAttributes_all, 
					configCustomAttributes, configBaseAttributes);
			
			// put macros into accessAttributes
			accessAttributes.putAll(macros);
						
			// put IP address into accessAttributes
			accessAttributes.put(ATTRIBUTE_IP_ADDRESS, sIPAddress);
			
			// EM Extension
			NextLabsResourceExtension extension = extensionAgent.getResourceExtension(sType, context);
			
			if (extension != null) {
				logger.debug("Loading EM Extension class %s with name %s", extension.getImplClass(), 
						extension.getBoType());
				
				MapList resultList = null;
				
				try {
					ClassLoader myClassLoader = NextLabsDataAccess.class.getClassLoader();
					Class myClass = myClassLoader.loadClass(extension.getImplClass());
					
					Object whatInstance = myClass.newInstance();
					
					// The second parameter is the parameter type,
					// There can be multiple parameters for the method we are trying to call
					Method myMethod = myClass.getMethod("getData", new Class[] {
							matrix.db.Context.class, DomainObject.class});
					
					// Calling the real method. Passing methodParameter as
					// parameter. You can pass multiple parameters based on
					// the signature of the method you are calling. Hence
					// there is an array.				
					resultList = (MapList) myMethod.invoke(whatInstance, new Object[] {
							context, dObject});
				} catch (Exception ex) {
					logger.error("getData() caught exception: %s", ex, ex.getMessage());
					
					throw new FrameworkException("Failed to load extension class: " + 
							extension.getImplClass() + " for " + extension.getBoType());
				}
				
				dataSending = new HashMap<String, Object>();
								
				if (resultList != null && resultList.size() > 0) {
					
					logger.debug("EM Extension resultList contains %d items", resultList.size());
					
					for (int i = 0; i < resultList.size(); i++) {
						
						Map<String, Object> map = (Map<String, Object>) resultList.get(i);
						
						// Performing conversion for extension data
						// adding prefix to the key
						map = convertExtensionDataToPolicyControllerFormat(map);
						
						// Make a copy of the accessAttributes
						Map<String, Object> accessAttributes_clone = new HashMap<String, Object>();
						accessAttributes_clone.putAll(accessAttributes);
						
						for (Map.Entry<String, Object> entry : map.entrySet()) {
							String key = entry.getKey();
							Object value = entry.getValue();
							
							accessAttributes_clone.put(key, value);
						}
						
						logger.debug("accessAttributes_clone %d: %s", i, accessAttributes_clone);

						// Attach the query result for the base object
						dataSending.put(NextLabsConstant.BASE + i, accessAttributes_clone);
						
					} // end of loop body for resultList
				} // end of if body for (resultList != null)
				// bug fix: when classification service is down and result list is null/empty
				else {
					// send basic information
					logger.debug("accessAttributes: %s", accessAttributes);
		
					dataSending = new HashMap<String, Object>();
					
					// Attach the query result for the base object
					dataSending.put(NextLabsConstant.BASE0, accessAttributes);
				}
				
			// End of Extension
			} else {
				logger.debug("accessAttributes: %s", accessAttributes);
				
				dataSending = new HashMap<String, Object>();
				
				// Attach the query result for the base object
				dataSending.put(NextLabsConstant.BASE0, accessAttributes);

				ArrayList<MapList> inheritanceParents = null;

				// direction is true, if Part is connected on the from side of the relationship
				if (config.getInheritanceDirectionFromParentToChild(sType)) {
					ArrayList<String> inheritanceRelationship = config.getInheritanceRelationship(sType);
					logger.debug("inheritanceRelationship: = true");
					inheritanceParents = getParentData(context, sObjectID, inheritanceRelationship, sType);
					if(inheritanceParents.size()>0)
						dataSending.put(INHERITANCE_KEY, inheritanceParents);
				}
			}
			
			logger.debug("Data sending: %s", dataSending);

		} catch (FrameworkException fe) {
			throw fe;
		} catch (Exception ex) {
			logger.error("getData() caught exception: %s", ex, ex.getMessage());
			
			throw ex;
		}

		logger.trace("END nxlDataAccess.");
		
		// Return dataSending hash map after attached the base and parent query result
		return dataSending;
	}
	
	/**
	 * Get the attributes values needed for passing to NextLabs CE SDK
	 * "create" action has no Business Object on which to operate during Check Trigger
	 * @param context Enovia Matrix Context
	 * @param sUserId User ID that perform the access
	 * @return HaspMap that contain the object attribute and parent object list, if exist
	 * @throws Exception
	 */
	public Map<String, Object> getCreateData(Context context, String sUserId, String sType)
			throws Exception {
		
		Map<String, Object> accessAttributes = null;
		Map<String, Object> dataSending = null;
		
		String emPrefix = config.getEMAttrPrefix();

		try {
			logger.debug("User is %s", sUserId);
			
			accessAttributes = new HashMap<String, Object>();

			String sIPAddress = context.getIPAddress();
						
			// If no IP-address found then we hard coded to local, we need that
			if (sIPAddress == null) {
				sIPAddress = "127.0.0.1";
			}
			
			// put IP address into accessAttributes
			accessAttributes.put(ATTRIBUTE_IP_ADDRESS, sIPAddress);
			accessAttributes.put(emPrefix + ATTRIBUTE_OBJECT_TYPE, sType);
						
			logger.debug("accessAttributes: %s", accessAttributes);

			dataSending = new HashMap<String, Object>();
			
			// Attach the query result for the base object
			dataSending.put(BASE0, accessAttributes);
		} catch (Exception ex) {
			logger.error("getData() caught exception: %s", ex, ex.getMessage());
			
			throw ex;
		}

		logger.trace("END nxlDataAccess.");
		
		// Return dataSending hash map after attach the base and parent query result
		return dataSending;
	}
		
	/**
	 * Get the attributes values needed for passing to NextLabs CE SDK
	 * This method is use to query the parent as base data based on relationship "Latest Version"
	 * @param context Enovia Matrix Context
	 * @param sObjectID Enovia object id that use to query
	 * @param sUserId User ID that perform the access
	 * @param sAction Action performed by user
	 * @return HaspMap that contain the object attribute and parent object list, if exist
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getIndirectData(Context context, String sObjectID, String sUserId, HashMap<String, Object> macros) 
			throws FrameworkException, Exception {    	

		Map<String, Object> accessAttributes = null;		
		Map<String, Object> dataSending = null;
		
		String emPrefix = config.getEMAttrPrefix();

		try {
			DomainObject dObject = new DomainObject(sObjectID);

			String sType = dObject.getInfo(context, DomainConstants.SELECT_TYPE);
			String name = dObject.getInfo(context, DomainConstants.SELECT_NAME);
			String rev = dObject.getInfo(context, DomainConstants.SELECT_REVISION);
			String sIPAddress = context.getIPAddress();
			
			// If no IP-address found then we hard coded to local, we need that
			if (sIPAddress == null) {
				sIPAddress = "127.0.0.1";
			}

			logger.debug("User is %s; Check object: %s | %s | %s", sUserId, sType, name, rev);
			logger.debug("Macros: %s", macros.toString());
			logger.debug("Getting direct parent");
			
			// the return data will have configured prefix.
			MapList mapList = getFixParentData(context, sObjectID, sType);
			
			if (mapList != null && mapList.size() > 0)
				accessAttributes = (Map<String, Object>) mapList.get(0);
			
			if (null != accessAttributes) {
				sObjectID = (String) accessAttributes.get(emPrefix + ATTRIBUTE_OBJECT_ID);
				
				// put IP address into accessAttributes
				accessAttributes.put(ATTRIBUTE_IP_ADDRESS, sIPAddress);
				
				// EM Extension get parent type
				String parentSType = (String) accessAttributes.get(emPrefix + ATTRIBUTE_OBJECT_TYPE);
				logger.debug("ID from direct parent is %s, direct parent type is %s", sObjectID, parentSType);
				
				DomainObject parentObject = new DomainObject(sObjectID);
				
				// EM Extension
				NextLabsResourceExtension extension = extensionAgent.getResourceExtension(parentSType, context);
				
				if (extension != null) {
					logger.debug("Loading EM Extension class %s with name %s", extension.getImplClass(), 
							extension.getBoType());
					
					MapList resultList = null;
					
					try {
						ClassLoader myClassLoader = NextLabsDataAccess.class.getClassLoader();
						Class myClass = myClassLoader.loadClass(extension.getImplClass());
						
						Object whatInstance = myClass.newInstance();
						
						// The second parameter is the parameter type,
						// There can be multiple parameters for the method we are trying to call
						Method myMethod = myClass.getMethod("getData", new Class[] {
								matrix.db.Context.class, DomainObject.class});
						
						// Calling the real method. Passing methodParameter as
						// parameter. You can pass multiple parameters based on
						// the signature of the method you are calling. Hence
						// there is an array.				
						resultList = (MapList) myMethod.invoke(whatInstance, new Object[] {
								context, parentObject});
					} catch (Exception ex) {
						logger.error("getIndirectData() caught exception: %s", ex, ex.getMessage());
						
						throw new FrameworkException("Failed to load extension class: " + 
								extension.getImplClass() + " for " + extension.getBoType());
					}
					
					dataSending = new HashMap<String, Object>();
					
					
					if (resultList != null && resultList.size() > 0) {
						
						logger.debug("EM Extension resultList contains %d items", resultList.size());
						
						for (int i = 0; i < resultList.size(); i++) {
							
							Map<String, Object> map = (Map<String, Object>) resultList.get(i);
							
							// Performing conversion for extension data
							// adding prefix to the key
							map = convertExtensionDataToPolicyControllerFormat(map);
							
							// Make a copy of the accessAttributes
							Map<String, Object> accessAttributes_clone = new HashMap<String, Object>();
							accessAttributes_clone.putAll(accessAttributes);
							
							for (Map.Entry<String, Object> entry : map.entrySet()) {
								String key = entry.getKey();
								Object value = entry.getValue();
								
								accessAttributes_clone.put(key, value);
							}
							
							accessAttributes_clone.putAll(macros);
							
							logger.debug("accessAttributes_clone %d: %s", i, accessAttributes_clone);	

							// Attach the query result for the base object
							dataSending.put(BASE + i, accessAttributes_clone);
						
						} // end of loop body for resultList
					} // end of if body for (resultList != null)
					// bug fix: when classification service is down and result list is null/empty
					else {
						// send basic information
						accessAttributes.putAll(macros);
						
						logger.debug("accessAttributes: %s", accessAttributes);
						
						dataSending = new HashMap<String, Object>();
						
						// Attach the query result for the base object
						dataSending.put(BASE0, accessAttributes);
					}
				
				} else {
					accessAttributes.putAll(macros);
					
					logger.debug("accessAttributes: %s", accessAttributes);
										
					dataSending = new HashMap<String, Object>();
					
					// Attach the query result for the base object
					dataSending.put(BASE0, accessAttributes);

					ArrayList<MapList> inheritanceParents = null;

					// direction is true, if Part is connected on the from side of the relationship
					if (config.getInheritanceDirectionFromParentToChild(sType)) {
						ArrayList<String> inheritanceRelationship = config.getInheritanceRelationship(sType);
						logger.debug("inheritanceRelationship: = true");
						inheritanceParents = getParentData(context, sObjectID, inheritanceRelationship, sType);
						dataSending.put(INHERITANCE_KEY, inheritanceParents);
					}
				}
				
				// End of EM Extension
				
				logger.debug("Data sending: %s", dataSending);
			} else {
				logger.error("Cannot get indirect parent, so will fall back to original data");
				return (getData(context, sObjectID, sUserId, macros));
			}
			
		} catch (FrameworkException fe) {
			throw fe;
		} catch (Exception ex) {
			logger.error("getIndirectData() caught exception: %s", ex, ex.getMessage());
			
			throw ex;
		}

		logger.trace("END nxlDataAccess.");

		// Return dataSending hash map after attach the base and parent query result
		return dataSending;
	}
	
	/**
	 * Get the attributes values needed for passing to NextLabs CE SDK
	 * @param context Enovia Matrix Context
	 * @param sObjectID Enovia object id that use to query
	 * @param sUserId User ID that perform the access
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Map<String, Object> getSubjectData(Context context, 
			String sUserId, String subjectExtensionType) throws Exception {
		Map<String, Object> dataSending = new HashMap<String, Object>();

		try {
			logger.debug("User is %s", sUserId);
					
			// EM Extension
			NextLabsSubjectExtension extension = extensionAgent.getSubjectExtension(subjectExtensionType);
			
			if (extension != null) {
				logger.debug("Loading EM Extension class %s with name %s", extension.getImplClass(), 
						extension.getSubjectType());
				
				ClassLoader myClassLoader = NextLabsDataAccess.class.getClassLoader();
				Class myClass = myClassLoader.loadClass(extension.getImplClass());
				
				Object whatInstance = myClass.newInstance();
				
				// The second parameter is the parameter type,
				// There can be multiple parameters for the method we are trying to call
				Method myMethod = myClass.getMethod(METHOD_GET_DATA, new Class[] {matrix.db.Context.class});
				
				// Calling the real method. Passing methodParameter as
				// parameter. You can pass multiple parameters based on
				// the signature of the method you are calling. Hence
				// there is an array.				
				Map<String, Object> result = (Map<String, Object>) myMethod.invoke(whatInstance, new Object[] {
						context});
				
				dataSending = new HashMap<String, Object>();
				
				if (result != null && result.size() > 0) {
					logger.debug("EM Extension resultList contains %d items", result.size());
					
					dataSending.putAll(result);
				} // end of if body for (resultList != null)
			// End of Extension
			} 
			
			logger.debug("Data sending: %s", dataSending);

		} catch (Exception ex) {
			logger.error("getSubjectData() caught exception: %s", ex, ex.getMessage());
			
			throw ex;
		}

		logger.trace("END nxlDataAccess.");
		
		// Return dataSending hash map after attach the base and parent query result
		return dataSending;
	}
	
	/**
	 * This function is needed because when we query from Enovia we need to covert the attribute name from "name"
	 * to "attribute[name].value" to get the value from Enovia. So for sending to policy controller we need to reverse this 
	 * process.
	 * @param accessAttributes Hashmap that contain all the key/value pair that need to convert
	 * @param configCustomAttributes Array of string contain the key of the custom attributes
	 * @return
	 */
	@SuppressWarnings("unused")
	private Map<String, Object> convertToPolicyControllerFormat(Map<String, Object> accessAttributes, 
			String[] configCustomAttributes, String[] sBaseAttrs) {
		return convertToPolicyControllerFormat(accessAttributes, new String[0], 
				new String[0], configCustomAttributes, sBaseAttrs);
	}
	
	/**
	 *  This function is added to support attribute for all object type
	 *  This function is needed because when we query from Enovia we need to covert the attribute name from "name"
	 * to "attribute[name].value" to get the value from Enovia. So for sending to policy controller we need to reverse this 
	 * process.
	 * @param accessAttributes Hashmap that contain all the key/value pair that need to convert
	 * @param configCustomAttributes Array of string contain the key of the custom attributes
	 * @return
	 */
	private Map<String, Object> convertToPolicyControllerFormat( Map<String, Object> accessAttributes, 
			String[] configCustomAttributesAll, String[] sBaseAttrsAll, 
			String[] configCustomAttributes, String[] sBaseAttrs) {
		
		String prefix = config.getEMAttrPrefix();
		
		// attributes for all object type
		for (String sCustomAttrAll : configCustomAttributesAll) {
			
			Object oValue = accessAttributes.get(CUSTOM_ATTR_PREFIX + sCustomAttrAll + CUSTOM_ATTR_SUFFIX);
			
			// Checking to avoid processing null and avoid
			// putting null to hashmap which will cause exception
			if (oValue == null)
				continue;
			
			// Remove from the map and put in the new format
			accessAttributes.remove(CUSTOM_ATTR_PREFIX + sCustomAttrAll + CUSTOM_ATTR_SUFFIX);
			
			// Special handling (name, type) is replaced by applying prefix to all to standardize the attribute naming
			// for easy to differentiate the data is from EM or extension
			// trade-off: size of data sent is increased
			sCustomAttrAll = prefix + sCustomAttrAll;
			
			accessAttributes.put(sCustomAttrAll, oValue);
		}
		
		for (String sBaseAttrAll : sBaseAttrsAll) {
			
			Object oValue = accessAttributes.get(sBaseAttrAll);
			
			// Checking to avoid processing null and avoid
			// putting null to hashmap which will cause exception
			if (oValue == null)
				continue;
			
			// Remove from the map and put in the new format
			accessAttributes.remove(sBaseAttrAll);
			
			// Special handling (name, type) is replaced by applying prefix to all to standardize the attribute naming
			// for easy to differentiate the data is from EM or extension
			// trade-off: size of data sent is increased
			sBaseAttrAll = prefix + sBaseAttrAll;

			accessAttributes.put(sBaseAttrAll, oValue);
		}
		// end of attributes for all object type
		
		// following codes are from original convertToPolicyControllerFormat(Map<String, Object> accessAttributes, 
		// String[] configCustomAttributes, String[] sBaseAttrs)
		for (String sCustomAttr : configCustomAttributes) {
			
			Object oValue = accessAttributes.get(CUSTOM_ATTR_PREFIX + sCustomAttr + CUSTOM_ATTR_SUFFIX);
			
			// Checking to avoid processing null and avoid
			// putting null to hashmap which will cause exception
			if (oValue == null)
				continue;
			
			// Remove from the map and put in the new format
			accessAttributes.remove(CUSTOM_ATTR_PREFIX + sCustomAttr + CUSTOM_ATTR_SUFFIX);
			
			// Special handling (name, type) is replaced by applying prefix to all to standardize the attribute naming
			// for easy to differentiate the data is from EM or extension
			// trade-off: size of data sent is increased
			sCustomAttr = prefix + sCustomAttr;

			accessAttributes.put(sCustomAttr, oValue);
		}
		
		// Handle reserved word in policy controller
		for (String sBaseAttr : sBaseAttrs) {
			
			Object oValue = accessAttributes.get(sBaseAttr);
			
			// Checking to avoid processing null and avoid
			// putting null to hashmap which will cause exception
			if (oValue == null)
				continue;
			
			// Remove from the map and put in the new format
			accessAttributes.remove(sBaseAttr);
			
			// Special handling (name, type) is replaced by applying prefix to all to standardize the attribute naming
			// for easy to differentiate the data is from EM or extension
			// trade-off: size of data sent is increased
			sBaseAttr = prefix + sBaseAttr;

			accessAttributes.put(sBaseAttr, oValue);
		}
		
		//Handling for not configure type
		if (accessAttributes.get(DomainConstants.SELECT_ID)!=null){
			Object oValue = accessAttributes.remove(DomainConstants.SELECT_ID);
			accessAttributes.put(prefix + DomainConstants.SELECT_ID, oValue);
		}
		
		if (accessAttributes.get(DomainConstants.SELECT_TYPE)!=null){
			Object oValue = accessAttributes.remove(DomainConstants.SELECT_TYPE);
			accessAttributes.put(prefix + DomainConstants.SELECT_TYPE, oValue);
		}
		
		return accessAttributes;
	}
	
	/**
	 *  This function is added to support translation for extension attribute
	 *  This function will handle PC keywords "name", "type"
	 * @param prefix
	 * @param accessAttributes Hashmap that contain all the key/value pair that need to convert
	 * @return
	 */
	private Map<String, Object> convertExtensionDataToPolicyControllerFormat(Map<String, Object> accessAttributes) {
		String prefix = config.getExtensionAttrPrefix();
		
		if (!prefix.trim().equals("")) {
			Map<String, Object> accessAttributes_prefixed = new HashMap<String, Object>();
			
			for (Map.Entry<String, Object> entry : accessAttributes.entrySet()) {
				String key = entry.getKey();
				Object value = entry.getValue();
								
				accessAttributes_prefixed.put(prefix + key, value);
			}
			
			return accessAttributes_prefixed;
		}
		
		return accessAttributes;
	}
	
	/**
	 * Setup the select list: query attributes
	 * To avoid all attribute to be selected
	 * If attribute[].value is pass in then all attributes will be returned
	 * @param selectList reference
	 * @param inputs reference
	 */
	private StringList setupSelectList(StringList selectList, 
			String[] baseInputs, String[] customInputs) {
		for (String baseInput : baseInputs) {
			if (baseInput.length() > 1)
				selectList.addElement(baseInput);
		}
		
		for (String customInput : customInputs) {
			if (customInput.length() > 1)
				selectList.addElement(CUSTOM_ATTR_PREFIX + customInput + CUSTOM_ATTR_SUFFIX);
		}
		
		return selectList;
	}

}
