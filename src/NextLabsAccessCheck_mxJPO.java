/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */
 
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import org.apache.log4j.Logger;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.nextlabs.enovia.common.NextLabsConstant;
import com.nextlabs.enovia.em.NextLabsAgentUtil;
import com.nextlabs.enovia.em.NextLabsAuthorisationAgentFactory;
import com.nextlabs.enovia.em.NextLabsAuthorisationAgentRest;
import com.nextlabs.enovia.em.NextLabsDataAccess;
import com.nextlabs.enovia.em.NextLabsLogger;


/**
 * @author klee
 * @version: $Id: //depot/ProfesionalServices/EnoviaEntitlement/NextLabsAccessCheck_mxJPO.java
 */
public class NextLabsAccessCheck_mxJPO extends DomainObject implements Serializable, NextLabsConstant {
	/**
	 * Serialization Unique ID
	 */
	private static final long serialVersionUID = -8424719566325299371L;
	
	// Cachemanager from Ehcache
	private static CacheManager cacheManager = null;
	
	// Cache from Ehcache for holding
	private static Cache memoryOnlyCache = null;
	
	// Log4J logger for logging purpose
	private static NextLabsLogger logger = null;
	
	// DataAccess class for query result from Enovia
	private static NextLabsDataAccess nDataAccess = null;
	
	// Authorization component which make a call to NextLabs CE SDK
	private static NextLabsAuthorisationAgentFactory agent = null;
	
	// Action variable to pass to NextLabs CE SDK when perform the calling
	private static final String ACTION = "OPEN";
	
	// String represent string value "false"
	private static final String VALUE_FALSE = "false";

	// String represent string value "true"
	private static final String VALUE_TRUE = "true";
	
	private static String sConfigPath = null;
	
	private static boolean bInitSuccess = true;
	
	/**
	 * Static portion for loading of classes that only need to initialize one time
	 */
	static {
		try {
			sConfigPath = NextLabsAgentUtil.getConfigPath();
			
			cacheManager = CacheManager.create(sConfigPath + EHCACHE_CONFIG_FILE);
			memoryOnlyCache = (Cache) cacheManager.addCacheIfAbsent(CACHE_NAME);
			
			logger = new NextLabsLogger(Logger.getLogger("NextLabsAccessCheck"));
			nDataAccess = NextLabsDataAccess.getInstance();
			agent = NextLabsAuthorisationAgentRest.getInstance();
		} catch (Exception ex) {
			System.out.println(ex.toString());
			
			bInitSuccess = false;
		}
	}
	
	/**
	 * Constructor for the class. Initialization of log4j and DataAcess class.
	 * @param context Enovia Matrix Context
	 * @param args
	 * @throws Exception
	 */
	public NextLabsAccessCheck_mxJPO(Context context, String[] args) 
			throws Exception {
		super();
		
    	if (args != null && args.length > 0) {
      		setId(args[0]);
    	}
	}
	
	/**
	 * Checks if user is allowed to view the object.
	 * return "true" string is allow, "false" if deny.
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 
	 * @returns "true" if context user has access, otherwise "false"
	 * @throws Exception if the operation fails
	 */
	public String EvaluateAccess(Context context, String[] args) throws FrameworkException {
		if (!bInitSuccess) {
			throw new FrameworkException("Failed to init NextLabs Enovia EM");
		}
		
		long lStartTimeNano = System.nanoTime();
		String returnValue = VALUE_FALSE;
		
		try {
			// Object ID is unique in Enovia
			String sObjectId = getInfo(context, SELECT_ID);
			String sUserId = context.getUser();
			String sCacheKey = sObjectId + sUserId;
			
			// Try to retrieve the previous result from cache
			returnValue = getValueInCache(sCacheKey);
			
			// If the key is in cache then just return, no need to proceed for further.
			if (returnValue != null) {
				return returnValue;
			}
			
			// Getting the data from Enovia via the DataAccess class.
			Map<String, Object> resourceAttributes = nDataAccess.getData(
					context, sObjectId, sUserId);
			Map<String, Object> userAttributes = nDataAccess.getSubjectData(
					context, sUserId, NextLabsConstant.SUBJECT_EXTENSION_USER);
			Map<String, Object> appAttributes = nDataAccess.getSubjectData(
					context, sUserId, NextLabsConstant.SUBJECT_EXTENSION_APP);
						
			HashMap<String, Object> attrResource = new HashMap<String, Object>(resourceAttributes);
			HashMap<String, Object> attrUser = new HashMap<String, Object>(userAttributes);
			HashMap<String, Object> attrApp = new HashMap<String, Object>(appAttributes);
			
			// Invoke calling to authorization agent which will call to Nextlabs CE SDK
			HashMap<String, Object> hasAccessResult = agent.hasAccess(
					context.getUser(), ACTION, attrResource, attrUser, attrApp);
			
			// Getting response for result hashmap
			String response = (String) hasAccessResult.get(RESPONSE_KEY);
			
			if ((RESPONSE_ALLOW_VALUE).equalsIgnoreCase(response)) {
				returnValue = VALUE_TRUE;
			} else {
				returnValue = VALUE_FALSE;
				logger.info ("Response is denied for request from user %s for objectId %s", sUserId, sObjectId);
			}
			
			// Put the latest retrieval result to cache
			putValueIntoCache(sCacheKey, returnValue);
		} catch (Exception ex) {
			logger.error("EvaluateAccess() caught exception: %s", ex, ex.getMessage());
		}
		
		logger.debug("NextLabsAccessCheck:EvaluateAccess. returnValue: %s with response timing %s", returnValue, 
				(System.nanoTime() - lStartTimeNano) / 1000000.00);
				
		return returnValue;
	}
	
	/**
	 * Getting the key/value pair from Ehcache memory
	 * @param sKey Key in ehcache
	 * @return Value for the key, null if key not found in cache
	 */
	private String getValueInCache(String sKey) {
		if (null != memoryOnlyCache) {
			Element eleCache = memoryOnlyCache.get(sKey);
			
			if (null != eleCache) {
				String sValue = (String) eleCache.getObjectValue();
				
				logger.debug("Element found for key |%s| with value |%s|", sKey, sValue);
				
				return sValue;
			}
		}
		
		return null;
	}
	
	/**
	 * Putting the key/value pair to Ehcache
	 * @param sKey Key for cache element storage
	 * @param sValue Value for storage with the key pair
	 */
	private void putValueIntoCache(final String sKey, final String sValue) {
		if (memoryOnlyCache != null) {
			Element element = new Element(sKey, sValue);
			
			memoryOnlyCache.put(element);
		}
	}
	
	/**
	 * This is entry point for JPO, always return 0, we no need that
	 * @param context Enovia Matrix Context
	 * @param args
	 * @return 0
	 * @throws Exception
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		return 0;
	}
	
}
