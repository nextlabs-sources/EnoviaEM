package com.nextlabs.enovia.em;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.apache.openaz.pepapi.Action;
import org.apache.openaz.pepapi.CategoryContainer;
import org.apache.openaz.pepapi.PepAgent;
import org.apache.openaz.pepapi.PepAgentFactory;
import org.apache.openaz.pepapi.PepResponse;
import org.apache.openaz.pepapi.Resource;
import org.apache.openaz.pepapi.Subject;
import org.apache.openaz.pepapi.std.StdPepAgentFactory;
import org.apache.openaz.xacml.api.AttributeAssignment;
import org.apache.openaz.xacml.api.Decision;
import org.apache.openaz.xacml.api.Result;

import com.matrixone.apps.domain.util.MapList;
import com.nextlabs.enovia.common.NextLabsConstant;
import com.nextlabs.enovia.common.PropertyLoader;
import com.nextlabs.openaz.pepapi.Application;
import com.nextlabs.openaz.utils.Constants;


/**
 * @author klee
 * @version: $Id: //depot/ProfesionalServices/EnoviaEntitlement/NextLabsAuthorisationAgentRest.java
 */
public final class NextLabsAuthorisationAgentRest  extends NextLabsAuthorisationAgentFactory implements NextLabsConstant{
	
	private String hostname = null;
	
	private static NextLabsLogger logger = null;
	
	private static NextLabsRuntimeConfig config = null;
	
	private static NextLabsAuthorisationAgentRest authAgent = null;
		
	private static Application application;
	
	private static PepAgent pepAgent;
			
	private static int iConnectCount = 0;
	
	private static long lLastConnect = 0L;
	
	private static boolean bConnected = false;
	
	private static String sConfigPath = null;
	
	private static boolean bUserNameToLower = true;
	
	static {
		
		sConfigPath = NextLabsAgentUtil.getConfigPath();
		
		config = NextLabsRuntimeConfig.getInstance();
			
		logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));
	}
	
	/**
	 * Default constructor, will initialize NextLabs Open AZ SDK
	 */
	private NextLabsAuthorisationAgentRest() {
		
	}
	
	/**
	 * Singleton control
	 * @return
	 */
	public synchronized static NextLabsAuthorisationAgentRest getInstance() {
		if (authAgent == null) {
			authAgent = new NextLabsAuthorisationAgentRest();
			
			authAgent.initialize();
		}
		
		return authAgent;
	}
	
	/**
	 * NextLabs CE SDK initialization
	 */
	private synchronized void initialize() {
		// Getting host name, does not serve any purpose name
		try {
			
			String sPropertiesFile = sConfigPath + config.getPDPConfigFileName();
			
			logger.info("Loading PDP setting from file %s", sPropertiesFile);
			
			Properties xacmlProperties = PropertyLoader.loadPropertiesDirectly(sPropertiesFile);
			
			//Added to handle encrypted password
			if((xacmlProperties.getProperty(Constants.PDP_REST_OAUTH2_CLIENT_SECRET)!=null)) {
					xacmlProperties.setProperty(Constants.PDP_REST_OAUTH2_CLIENT_SECRET, NextLabsAgentUtil.decryptPassword(xacmlProperties.getProperty(Constants.PDP_REST_OAUTH2_CLIENT_SECRET)));
			}

			PepAgentFactory pepAgentFactory = new StdPepAgentFactory(xacmlProperties);
			
			pepAgent = pepAgentFactory.getPepAgent();
			
			hostname = NextLabsAgentUtil.getHostName();
			
			application = Application.newInstance(getAppName());
			
			if (config.getUsernameToLowerCase().equalsIgnoreCase("false")) {
				logger.debug("Username remain as original cases, since setting is false for convert username to lowercase is false");
				bUserNameToLower = false;
			}
			else {
				logger.debug("Username will be convert to lowercase for all username");
			}
			
			// If connect count less than configure re-try count than will continue on reconnection
			if (iConnectCount < config.getPolicyControllerConnectRetryCnt()) {
				lLastConnect = System.currentTimeMillis();
				iConnectCount++;
					
				pepAgent = pepAgentFactory.getPepAgent();
				
				// Reset connect count since successfully connect
				iConnectCount = 0;
				if (sentDumpRequest()) {
					bConnected = true;
				}
				
			} else {
				logger.debug("Retry count > %s not retrying to connect now ", config.getPolicyControllerConnectRetryCnt());
				
				// If last re-try connection is more than certain setting time then reset re-try count to 0
				// so that reconnection can happen
				if ((System.currentTimeMillis() - lLastConnect) > config.getPolicyControllerConnectRetryTimer()) {
					logger.debug("Reset re-try count to 0 due to last retry time is expired ");
					iConnectCount = 1;
					lLastConnect = System.currentTimeMillis();
					
					pepAgent = pepAgentFactory.getPepAgent();
					
					if (sentDumpRequest()) {
						bConnected = true;
					}
				}
			}
		} catch (Exception e) {
			logger.error("initialize() caught exception: %s", e, e.getMessage());
			bConnected = false;
			
		}
	}
	
	/**
	 * 
	 * @return
	 */
	private boolean sentDumpRequest(){
		
		Subject user = Subject.newInstance("Test User");
		
		Action azAction = Action.newInstance("TEST");
		
		Application app = Application.newInstance("Enovia Connetion Test");
		
		Resource srcResource = Resource.newInstance("Dummy Resource");
		srcResource.addAttribute(Constants.ID_RESOURCE_RESOURCE_TYPE.stringValue(), RESOURCE_TYPE_ENOVIA);
		
		long lCurrentTime = System.nanoTime();
		
		PepResponse pepResponse = null;
		
		try {
		
			pepResponse = pepAgent.decide(user,azAction,srcResource, app);
		
		} catch (Exception ex) {
			logger.error("sentDumpRequest() caught exception: %s", ex, ex.getMessage());
			return false;
		}
		
		if (pepResponse == null) {
			logger.error("ERRORRESULT= %s", "Empty response from PDP");
			
			return false;
		}
		
		logger.debug("OpenAZ API response is %s with timing %s ms", pepResponse.allowed(), (System.nanoTime() - lCurrentTime) / 1000000.00);
		
		return true;
	
	}
	
	/**
	 * Getting the application name from configuration file
	 * @return Application name that will be used for NextLabs CE SDK calling
	 * @throws Exception
	 */
	private String getAppName() {
		return config.getAppName();
	}
	
	
	public HashMap<String, Object> hasAccess(String username, String action, 
			HashMap<String, Object> attributes) throws Exception {
		return hasAccess(username, action, attributes, new HashMap<String, Object>(), new HashMap<String, Object>());
	}
	
	/**
	 * For determine whether user is granted access to specific type
	 * @param username Username for the user that will be allow/deny access
	 * @param action Action performed by the user.
	 * @param attributes HashMap of attributes which contain all the object attributes that will be pass to OpenAZ API
	 * @return HashMap of the response, contain response string and also the obligation information
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	public HashMap<String, Object> hasAccess(String username, String action, 
			HashMap<String, Object> attributes, HashMap<String, Object> userAttributes,
			HashMap<String, Object> appAttributes) throws Exception {
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		// Checking if handler is 0 then need to reconnect.
		synchronized (this) {
			if (!bConnected) {
				initialize();
			}
		}
		
		if (!bConnected) {
			logger.error("Connection to PDP failed,  will return default action which is %s ", config.getDefaultAction());
			
			result.put(RESPONSE_ERROR_KEY, "OpenAZ API Exception.");
			result.put(RESPONSE_KEY, config.getDefaultAction());
			
			if (null != config.getDefaultMessage()) {
				result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
			}
			
			return result;
		}
		
		result = inputValidation(username, action, attributes, config);
		
		if (result.get(RESPONSE_ERROR_KEY) != null) {
			return result;
		}
		
		logger.debug("ATTRIBUTES: HASHMAP= %s", attributes);
		
		Map<String, Object> mData;
		if ((attributes.size() == 1) || (attributes.size() == 2 && attributes.containsKey(INHERITANCE_KEY))) {
			
			mData = (Map<String, Object>)attributes.get(NextLabsConstant.BASE0);
		
			HashMap<String, Object> hmData = new HashMap<String, Object>(mData);
		
			result = checkNxlPolicy(username, action, hmData, userAttributes, appAttributes);
		
			// If error happen then will direct return
			if (result.containsKey(RESPONSE_ERROR_KEY)) {
				return result;
			} else {
				// If this main object is being deny then will direct return
				if (!(RESPONSE_ALLOW_VALUE).equalsIgnoreCase((String) result.get(RESPONSE_KEY)))
					return result;
			}
			
			Subject user = buildUser(username, userAttributes);
			
			Action azAction = Action.newInstance(action);
			
			Application app = buildApplication(appAttributes);
		
			ArrayList<MapList> inherMapList = (ArrayList<MapList>) attributes.get(INHERITANCE_KEY);
		
			if (inherMapList != null && inherMapList.size()> 0) {
				
				logger.debug("INHERITANCE: size= %s", inherMapList.size());
				
				List<Resource> resourcesList = new ArrayList<Resource>();
			
				for (int i = 0; i < inherMapList.size(); i++) {
					
					MapList mList = (MapList) inherMapList.get(i);
					
					logger.debug("Size of the Maplist %s", mList.size());
							
					for (int k = 0; k < mList.size(); k++) {
						
						Map<String, String> map = (Map<String, String>) mList.get(k);
					
						HashMap<String, Object> hashmap = new HashMap<String, Object>(map);
					
						logger.debug("INHERITANCE: %s", hashmap);
						
						Resource srcResource = buildSource(hashmap);
												
						if(srcResource!=null){
							resourcesList.add(srcResource);
						}
						else{
							
							result.put(RESPONSE_ERROR_KEY, "request data is null");
							result.put(RESPONSE_KEY, config.getDefaultAction());
							
							if (null != config.getDefaultMessage()) {
								result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
							}
							
							logger.debug("ERRORRESULT= %s", result.get(RESPONSE_KEY));
							
							return result;
						}
					
					}
				}
				
				List<PepResponse> responses = null;
								
				try {
					responses = pepAgent.bulkDecide(resourcesList, user, azAction,app);
				} catch (Exception e) {

					logger.error("checkResources() caught exception: %s", e, e.getMessage());

					result.put(RESPONSE_ERROR_KEY, "OpenAZ API Exception.");
					result.put(RESPONSE_KEY, config.getDefaultAction());

					if (null != config.getDefaultMessage()) {
						result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
					}
				}
				
				//For null result handling
				if (responses==null){
					
					result.put(RESPONSE_ERROR_KEY, "result is null");
					result.put(RESPONSE_KEY, config.getDefaultAction());
					
					if (null != config.getDefaultMessage()) {
						result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
					}
					
					logger.debug("ERRORRESULT= %s", result.get(RESPONSE_KEY));
					
					return result;
				}
				
				
				ArrayList<String> userMessageListMaster = new ArrayList<>();
				
				for (PepResponse resp: responses) {
					
					if(!resp.allowed()){
						
						logger.debug("Received NON allow response for request");
						
						if (resp.getWrappedResult().getDecision().equals(Decision.DENY)) {
							
							result.put(RESPONSE_KEY, RESPONSE_DENY_VALUE);
							
							ArrayList<String> userMessageList = processRawObligation(resp.getWrappedResult());
							
							userMessageListMaster.addAll(userMessageList);
							
						}
						else if(resp.getWrappedResult().getDecision().equals(Decision.NOTAPPLICABLE)){
								result.put(RESPONSE_KEY, config.getDefaultNotApplicableAction());
								result.put(RESPONSE_MESSAGE_KEY, config.getDefaultNotApplicableMessage());
								result.put(RESPONSE_ERROR_KEY, "Not applicable resoponse from PDP");
						}
						else {
							result.put(RESPONSE_KEY, config.getDefaultIndeterminateAction());
							result.put(RESPONSE_MESSAGE_KEY, config.getDefaultIndeterminateMessage());
							result.put(RESPONSE_ERROR_KEY, "Indeterminate response from PDP");
						}

						break;
					}
					else{
						ArrayList<String> userMessageList = processRawObligation(resp.getWrappedResult());
						
						userMessageListMaster.addAll(userMessageList);
						
						result.put(RESPONSE_KEY, RESPONSE_ALLOW_VALUE);
					}
				}
				
				if (userMessageListMaster.size()>0){
					result.put(RESPONSE_USERMESSAGES_KEY, userMessageListMaster);
				}
				
			}
			
			return result;
		} else {
			
			for (int i = 0; i < attributes.size(); i++) {
				
				mData = (Map<String, Object>)attributes.get(NextLabsConstant.BASE + i);
				
				HashMap<String, Object> hmData = new HashMap<String, Object>(mData);

				result = checkNxlPolicy(username, action, hmData, userAttributes, appAttributes);
				
				// If error happen then will direct return
				if (result.containsKey(RESPONSE_ERROR_KEY))
					return result;
				else {		
					// If this main object is being deny then will direct return
					if (!(RESPONSE_ALLOW_VALUE).equalsIgnoreCase(
							(String)result.get(RESPONSE_KEY)))
						return result;
				}
			}
			
			return result;
		}
		
	}
	
	/**
	 * Calling to OpenAZ API to validate the access
	 * @param username Username for the user that will be allow/deny access
	 * @param action Action performed by the user.
	 * @param resourceAttributes HashMap of attributes which contain all the object attributes that will be pass to OpenAZ API
	 * @return HashMap of the response, contain response string and also the obligation information
	 * @throws Exception
	 */
	public HashMap<String, Object> checkNxlPolicy(String username, String action, 
			HashMap<String, Object> resourceAttributes, HashMap<String, Object> userAttributes,
			HashMap<String, Object> appAttributes) throws Exception {
		
		HashMap<String, Object> result = new HashMap<String, Object>();
		
		String emPrefix = config.getEMAttrPrefix();
		
		try {
			
			result = inputValidation(resourceAttributes, config);
			
			if (result.get(RESPONSE_ERROR_KEY) != null) {
				return result;
			}			
			
			try {
				String sId = (String) resourceAttributes.get(emPrefix + ATTRIBUTE_OBJECT_ID);
				
				logger.debug("Object ID is %s", sId);
				

				Resource source = getSourceResource(resourceAttributes);
				
				// Building the source attributes
				source.addAttribute(ATTR_KEY_SERVER, hostname);
				source.addAttribute(ATTR_KEY_APPLICATION, getAppName());
				source.addAttribute(ATTR_KEY_NOCACHE, ATTR_VALUE_YES);
				source.addAttribute(ATTR_KEY_URL, (String)source.getResourceIdValue());
				source.addAttribute(Constants.ID_RESOURCE_RESOURCE_TYPE.stringValue(), RESOURCE_TYPE_ENOVIA);
				
				haspmapToOpenAZAttrs(resourceAttributes, source);
				
				// Building the user attributes
				if (bUserNameToLower)
					username = username.toLowerCase();
				
				Subject user = Subject.newInstance(username);
				haspmapToOpenAZAttrs(userAttributes, user);

				logger.debug("Aplication username = %s", user.getSubjectIdValue());
				
				Application app = buildApplication(appAttributes);
				
				Map<String, Object[]> sAttr = source.getAttributeMap();

				if (logger.isNXLDebugEnable) {			
					for (Map.Entry<String, Object[]> entry : sAttr.entrySet())
					{
						logger.debug("Java calling  with |Data-->%s   Values-->%s|", entry.getKey(), entry.getValue()[0]);
					}
				}
				
				Action azAction = Action.newInstance(action);
				
				long lCurrentTime = System.nanoTime();
					
				PepResponse pepResponse = pepAgent.decide(user,azAction,source, app);
					
				
				// If not able to get enforcement result will read from configuration file
				if (pepResponse == null) {
					result.put(RESPONSE_ERROR_KEY, "result is null");
					result.put(RESPONSE_KEY, config.getDefaultAction());
					
					if (null != config.getDefaultMessage()) {
						result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
					}
					
					logger.debug("ERRORRESULT= %s", result.get(RESPONSE_KEY));
					
					return result;
				}
						
				logger.debug("OpenAZ API response is %s with timing %s ms", pepResponse.getWrappedResult().getDecision(), 
						(System.nanoTime() - lCurrentTime) / 1000000.00);
									
				ArrayList<String> userMessageList = processRawObligation(pepResponse.getWrappedResult());
				
				if (userMessageList.size()>0){
					result.put(RESPONSE_USERMESSAGES_KEY, userMessageList);
				}
				
				result.put(RESPONSE_KEY, pepResponse.allowed()?RESPONSE_ALLOW_VALUE:RESPONSE_DENY_VALUE);
				
				if(!pepResponse.allowed() && !pepResponse.getWrappedResult().getDecision().equals(Decision.DENY)) {
					//To handle indeterminate and not applicable response
					if(pepResponse.getWrappedResult().getDecision().equals(Decision.NOTAPPLICABLE)){
						result.put(RESPONSE_KEY, config.getDefaultNotApplicableAction());
						result.put(RESPONSE_MESSAGE_KEY, config.getDefaultNotApplicableMessage());
						result.put(RESPONSE_ERROR_KEY, "Not applicable resoponse from PDP");
					}
					else if (pepResponse.getWrappedResult().getDecision().equals(Decision.INDETERMINATE)){
						
						result.put(RESPONSE_KEY, config.getDefaultIndeterminateAction());
						result.put(RESPONSE_MESSAGE_KEY, config.getDefaultIndeterminateMessage());
						result.put(RESPONSE_ERROR_KEY, "Indeterminate response from PDP");
					}
				}
				
			} catch (Exception e) {
				logger.error("checkNxlPolicy() caught exception: %s", e, e.getMessage());
				
				result.put(RESPONSE_ERROR_KEY, "OpenAZ API Exception.");
				result.put(RESPONSE_KEY, config.getDefaultAction());
				
				if (null != config.getDefaultMessage()) {
					result.put(RESPONSE_MESSAGE_KEY, config.getDefaultMessage());
				}
			}
		} catch (Exception ex) {
			logger.error("checkNxlPolicy() caught exception: %s", ex, ex.getMessage());
			
			throw ex;
		}	
		
		logger.trace("END nxlAuthorisationAgent:hasAccess");
		logger.debug("RESULT= %s", result.get(RESPONSE_KEY));
		
		return result;
	}
	
	
	/**
	 * Process obligation and put in list
	 * @param resl Wrap results from PDP response
	 * @return List of display message to user
	 */
	private ArrayList<String> processRawObligation(Result resl){

		ArrayList<String> userMessageList = new ArrayList<>();
		// For obligation handling		
		Collection<org.apache.openaz.xacml.api.Obligation> obs= resl.getObligations();
		
		for (org.apache.openaz.xacml.api.Obligation ob: obs){

			logger.debug("Obligation ID is %s", ob.getId().toString());

			if(ob.getId().toString().equalsIgnoreCase(OBLIGATION_MESSAGE_ID)){

				Collection<AttributeAssignment> obAssigns = ob.getAttributeAssignments();

				for (AttributeAssignment obA: obAssigns){
					logger.debug("Value in obligation %s  with id %s", obA.getAttributeValue().getValue(), obA.getAttributeId().toString());
					if(obA.getAttributeId().toString().equalsIgnoreCase(OBLIGATION_MESSAGE_KEY)){
						userMessageList.add((String) obA.getAttributeValue().getValue());
					}
				}

			}
		}

		return userMessageList;

	}
	
	
	/**
	 * Convert the HashMap attribute to resource attributes in OpenAZ format
	 * @param attributes Source attribute in HashMap
	 * @return Resource attributes in OpenAz format
	 */
	private Resource buildSource(HashMap<String, Object> attributes) throws Exception {

		HashMap<String, Object> result = new HashMap<String, Object>();
		String emPrefix = config.getEMAttrPrefix();

		result = inputValidation(attributes, config);

		if (result.get(RESPONSE_ERROR_KEY) != null) {
			return null;
		}

		String sId = (String) attributes.get(emPrefix + ATTRIBUTE_OBJECT_ID);

		logger.debug("Object ID is %s", sId);

		Resource source = getSourceResource(attributes);

		// Building the source attributes
		source.addAttribute(ATTR_KEY_SERVER, hostname);
		source.addAttribute(ATTR_KEY_APPLICATION, getAppName());
		source.addAttribute(ATTR_KEY_NOCACHE, ATTR_VALUE_YES);
		source.addAttribute(ATTR_KEY_URL, (String)source.getResourceIdValue());
		source.addAttribute(Constants.ID_RESOURCE_RESOURCE_TYPE.stringValue(), RESOURCE_TYPE_ENOVIA);

		haspmapToOpenAZAttrs(attributes, source);

		Map<String, Object[]> sAttr = source.getAttributeMap();

		if (logger.isNXLDebugEnable) {			
			for (Map.Entry<String, Object[]> entry : sAttr.entrySet())
			{
				logger.debug("Java calling  with |Data-->%s   Values-->%s|", entry.getKey(), Arrays.toString(entry.getValue()));
			}
		}

		return source;

	}
	
	/**
	 * Convert the HashMap attribute to user attributes in OpenAZ format
	 * @param appAttributes User attribute in HashMap
	 * @return User attributes in OpenAz format
	 */
	private Subject buildUser(String username, HashMap<String, Object> userAttributes) {
		
		//Convert to lower case
		if (bUserNameToLower) {
			username = username.toLowerCase();
		}

		// Building the user attributes
		Subject user = Subject.newInstance(username);
		
		haspmapToOpenAZAttrs(userAttributes, user);

		logger.debug("CE username = %s", user.getSubjectIdValue());
		
		return user;
	}
	
	/**
	 * Convert the HashMap attribute to application attributes in OpenAZ format
	 * @param appAttributes Application attribute in HashMap
	 * @return Application attributes in OpenAz format
	 */
	private Application buildApplication(HashMap<String, Object> appAttributes){
		
		// Building the application attributes
		return application;
	}
	
	/**
	 * Convert the HashMap attribute to resource attributes in OpenAZ format
	 * @param attributes Source attribute in HashMap
	 * @return Resource attributes in OpenAz format
	 */
	private Resource getSourceResource(HashMap<String, Object> attributes){
		
		String emPrefix = config.getEMAttrPrefix();
		String extPrefix = config.getExtensionAttrPrefix();
		
		// Extension enhancement
		// use the Extension name and type when the keys are present
		String resource_type = (attributes.get(extPrefix + ATTRIBUTE_OBJECT_TYPE) != null) ?
				(String) attributes.get(extPrefix + ATTRIBUTE_OBJECT_TYPE) : (String) attributes.get(emPrefix + ATTRIBUTE_OBJECT_TYPE);  
		String resource_name = (attributes.get(extPrefix + ATTRIBUTE_OBJECT_NAME) != null) ? 
				(String) attributes.get(extPrefix + ATTRIBUTE_OBJECT_NAME) : (String) attributes.get(emPrefix + ATTRIBUTE_OBJECT_NAME);
		String resource_vault = (attributes.get(extPrefix + ATTRIBUTE_OBJECT_VAULT) != null) ?
				(String) attributes.get(extPrefix + ATTRIBUTE_OBJECT_VAULT) : (String) attributes.get(emPrefix + ATTRIBUTE_OBJECT_VAULT);
		
		// Building the resource string 
		String sResourceString = buildResourceString(hostname, getAppName(),
				resource_vault, resource_type, resource_name);
		
	
		return Resource.newInstance(sResourceString);
	
	}
	
	/**
	 * Convert data in HashMap to OpenAZ Format
	 * @param attributes Attributes values in HashMap 
	 * @param source OpenAZ container for attributes
	 */
	private void haspmapToOpenAZAttrs(HashMap<String, Object> attributes, CategoryContainer source) {
		
		for (Entry<String, Object> entry : attributes.entrySet()) {
			
			Object obj = entry.getValue();
			
			if (obj instanceof String) {
				String sValue = (String) obj;
				
				if (null != sValue && sValue.length() > 0) {
					source.addAttribute(entry.getKey(), sValue);
				}
			} else if (obj instanceof List<?>) {
				List<?> list = (List<?>) obj;
				
				for (Object listItem : list) {
					String sListItem = listItem.toString();
					
					if (null != sListItem && sListItem.length() > 0) {
						source.addAttribute(entry.getKey(), sListItem);
					}
				}
			} else {
				// for example, use may put other object or boolean into the map
				String sValue = obj.toString();
				
				if (null != sValue && sValue.length() > 0) {
					source.addAttribute(entry.getKey(), sValue);
				}
			}
		}
	}
	
	
}
