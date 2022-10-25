/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.matrixone.apps.domain.util.FrameworkException;
import com.nextlabs.enovia.common.NextLabsConstant;
import com.nextlabs.enovia.em.NextLabsActionsSupport;
import com.nextlabs.enovia.em.NextLabsAgentUtil;
import com.nextlabs.enovia.em.NextLabsAuthorisationAgentFactory;
import com.nextlabs.enovia.em.NextLabsAuthorisationAgentRest;
import com.nextlabs.enovia.em.NextLabsDataAccess;
import com.nextlabs.enovia.em.NextLabsLogger;
import com.nextlabs.enovia.em.NextLabsNotificationCenter;
import com.nextlabs.enovia.em.NextLabsRuntimeConfig;


/**
 * @author klee
 * @version: $Id: //depot/ProfesionalServices/EnoviaEntitlement/NextLabsTriggerCheck_mxJPO.java
 */
public class NextLabsTriggerCheck_mxJPO implements NextLabsConstant {
	
	private static NextLabsLogger logger = null;
	
	// DataAccess class for query result from Enovia
	private static NextLabsDataAccess nDataAccess = null;
	
	// Notification center
	private static NextLabsNotificationCenter nNotificationCenter = null;
	
	// Authorization component which make a call to NextLabs CE SDK
	private static NextLabsAuthorisationAgentFactory agent = null;
	
	// NextLabs Runtime Config
	private static NextLabsRuntimeConfig nRuntimeConfig = null;
	
	private static String sConfigPath = null;
	
	private static boolean bInitSuccess = true;
	
	static {
		try {
			sConfigPath = NextLabsAgentUtil.getConfigPath();
			
			logger = new NextLabsLogger(Logger.getLogger("NextLabsTriggerCheck"));	
			
			nDataAccess = NextLabsDataAccess.getInstance();
			
			nNotificationCenter = NextLabsNotificationCenter.getInstance();
			
			nRuntimeConfig = NextLabsRuntimeConfig.getInstance();
			
			agent = NextLabsAuthorisationAgentRest.getInstance();
			
		} catch (Exception ex) {
			System.out.println(ex.toString());
			bInitSuccess = false;
		}
	}
	
	/**
	 * Default constructor
	 * @param context Enovia Matrix context
	 * @param args
	 * @throws Exception
	 */
	public NextLabsTriggerCheck_mxJPO (Context context, String[] params) 
			throws Exception {
		PropertyConfigurator.configure(sConfigPath + LOG4J_CONFIG_FILE);
	}
	
	/**
	 * Checks if user is allowed to access the object
	 * @param context the eMatrix <code>Context</code> object
	 * @param args 
	 * @returns 0 if context user has access, otherwise 1
	 */
	// NOPMD by klee on 6/5/12 6:09 PM
	public int EvaluateAccess(Context context, String[] args) throws FrameworkException, Exception {
		if (!bInitSuccess) {
			throw new FrameworkException("Failed to init NextLabs Enovia EM");
		}
		
		String sAction = "";
		String sObjectID;
		String sType;
		String sPolicy;
		String sFileName;
		HashMap<String, Object> macros = new HashMap<String, Object>();
		
		if (args.length > 0) {
			sAction = args[0];
			sObjectID = args[1];
			sType = args[2];
			sPolicy = args[3];
		} else {
			// If information is not pass in, then just return 0 to allow user download
			return 0;
		}
		
		// handle different action from trigger
		try {
			NextLabsActionsSupport nxlActionsSupport = NextLabsActionsSupport.valueOf(sAction);
			
			switch (nxlActionsSupport) {
				case CHECKOUT:			
					sFileName = args[6];
					sAction = "EXPORT";
					
					macros.put("filename", sFileName);
					
					break;
				case REMOVEFILE:
					sFileName = args[6];
					sAction = "DELETE";

					macros.put("filename", sFileName);
					
					break;
				case CHECKIN:
					sFileName = args[6];
					sAction = "UPLOAD";
					
					macros.put("filename", sFileName);
					
					break;
				case CREATE:
					sObjectID = "";
				case MODIFYDESCRIPTION:
				case MODIFYATTRIBUTE:
				case CHANGENAME:
				case CHANGEOWNER:
				case CHANGEPOLICY:
				case CHANGETYPE:
				case CHANGEVAULT:
					sAction = "EDIT";
					
					// Filter out the version object
					// Edit of version object will be triggered during upload,
					// where the version object(metadata of uploaded file) 
					// will be created and modified.
					
					// instantiate domain object and get isversion attribute will not works
					// due to when the version object is creating, there is no domain obj
					if (sPolicy.equals(VERSION_POLICY)) {
						return 0;
					}
					
					break;
				case CONNECT:
				case DISCONNECT:
					String sRelType = args[4];
					String sConnection = args[5];
					
					// Filter out the version object
					// Connect/Disconnect of version object will be triggered during upload/removefile,
					
					// instantiate domain object and get isversion attribute will not works
					// due to when the version object is creating, there is no domain obj
					if (sPolicy.equals(VERSION_POLICY)) {
						return 0;
					}
										
					if (!nRuntimeConfig.isProtectedRelationships(sType, sRelType, sConnection)) {
						logger.debug("BO not in protected relationship skip and return for action %s with relationship %s", nxlActionsSupport, sRelType);
						return 0;
					}
					
					macros.put("relationship", sRelType);
					macros.put("connection", sConnection);
					
					break;
				default:
					// Default value set to "EXPORT"
					// Passing null Action to NextLabs SDK will causing error
					sAction = "EXPORT";
			}
		} catch (Exception ex) {
			// IllegalArgumentException or NullPointerException
			throw new FrameworkException("NextLabs Enovia EM: Invalid action " + sAction);
		}
		
		StringBuffer sLog = new StringBuffer();
		sLog.append("Action is ").append(sAction).append(" from ").append(sObjectID);
		
		logger.debug(sLog.toString());
		
		try {
			String sUserId = context.getUser();
			
			if (sUserId.equalsIgnoreCase("User Agent") && nRuntimeConfig.getReplaceUserAgent().equalsIgnoreCase("true")){
				sUserId = readLoggedIUserFromMX(context);
				logger.debug("Convert user id to MX_LOGGED_IN_USER which is %s", sUserId);
			}
			
			Map<String, Object> accessAttributes;
			Map<String, Object> userAttributes;
			Map<String, Object> appAttributes;
			
			if (sAction.equals("DELETE")) {
				// Added to handle special case where delete will return the file item instead of parent item
				accessAttributes = nDataAccess.getIndirectData(context, sObjectID, sUserId, macros);
			} else if (sAction.equals("EDIT") && sObjectID.equals("")) {
				// Added to handle special case: Create Check Trigger
				// Object ID is temporary during Create Check and we cannot use it
				// to retrieve a BO
				// Object ID is only available to the Create Action Trigger
				accessAttributes = nDataAccess.getCreateData(context, sUserId, sType);
			} else {
				accessAttributes = nDataAccess.getData(context, sObjectID, sUserId, macros);
			}
			
			// get user data which retrieves data from extension (if any)
			userAttributes = nDataAccess.getSubjectData(context, sUserId, NextLabsConstant.SUBJECT_EXTENSION_USER);
			
			// get application data which retrieves data from extension (if any)
			appAttributes = nDataAccess.getSubjectData(context, sUserId, NextLabsConstant.SUBJECT_EXTENSION_APP);
			
			HashMap<String, Object> attrResource = new HashMap<String, Object>(accessAttributes);
			HashMap<String, Object> attrUser = new HashMap<String, Object>(userAttributes);
			HashMap<String, Object> attrApp = new HashMap<String, Object>(appAttributes);
			
			// Call to agent to get the response
			HashMap<String, Object> hasAccessResult = agent.hasAccess(
					sUserId, sAction, attrResource, attrUser, attrApp);
			
			String response = (String) hasAccessResult.get(RESPONSE_KEY);
			String sError = (String) hasAccessResult.get(RESPONSE_ERROR_KEY);
			
			// Checking to see if there is SDK error response
			if (null != sError) {
				String sDefaultMsg = (String) hasAccessResult.get(RESPONSE_MESSAGE_KEY);
				
				if (null != sDefaultMsg) {
					logger.error("SDK error: %s", sDefaultMsg);
					nNotificationCenter.notify(context, response, sDefaultMsg, nRuntimeConfig.getNotificationMethod());
				}
			}
			
			// Change to display message even if allow obligation	
			logger.info("Response is %s for request from user %s for objectId %s", response, sUserId, sObjectID);
			
			ArrayList<String> sMessage = getDisplayMessage(hasAccessResult);
			
			nNotificationCenter.notify(context, sMessage, nRuntimeConfig.getNotificationMethod(), RESPONSE_ALLOW_VALUE.equalsIgnoreCase(response)?true:false);
						
			if (!(RESPONSE_ALLOW_VALUE).equalsIgnoreCase(response)) {
				return 1;
			}
		} catch (FrameworkException fe) {
			throw fe;
		} catch (Exception ex) {
			logger.error("EvaluateAccess() caught exception: %s", ex, ex.getMessage());
		}
		
		logger.debug("END nxlTriggerCheck:hasAccessTrigger. returnValue.");
		
		return 0;
	}
		
	/**
	 * Extract the message to display to the user from HashMap, if found
	 * @param hashMap Hashmap of the obligation information
	 * @return Message for obligation CE::NOTIFY which use to display the user
	 */
	private ArrayList<String> getDisplayMessage(HashMap <String, Object> hashMap) {
		
		ArrayList<String> sMsgList = new ArrayList<String>();
		
		if(hashMap.get("UserMessages")!=null){
			sMsgList = (ArrayList<String>) hashMap.get(RESPONSE_USERMESSAGES_KEY);
		}
		Collections.sort(sMsgList);
		
		return sMsgList;
	}
	
	private String readLoggedIUserFromMX(Context context){
		
		String result = null;
		MQLCommand mql = new MQLCommand();
		String sCommand = "get env MX_LOGGED_IN_USER_NAME";
		try {

			result = mql.executeCommand(mql, context, sCommand);
			logger.debug("$$$ MX_LOGGED_IN_USER_NAME : %s", result);
			// sCommand = "get env USER";
			// result = mql.executeCommand(mql, context, sCommand);
			//logger.debug("$$$ USER : %s", result);
		} catch (Exception ex) {
			logger.error("readLoggedIUserFromMX() caught exception: %s", ex,
					ex.getMessage());
		}
		finally{
			try {
				mql.close(context);
			} catch (MatrixException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return result;
		
	}
	
	
	
	/**
	 * This is entry point for JPO
	 * @param context Enovia Matrix Context
	 * @param args
	 * @return 0
	 * @throws Exception
	 */
	public int mxMain(Context context, String[] args) {
		return 0;
	}
	
}
