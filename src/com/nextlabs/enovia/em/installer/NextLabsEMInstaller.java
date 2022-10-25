package com.nextlabs.enovia.em.installer;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;

import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;
import com.nextlabs.enovia.common.NextLabsConstant;
import com.nextlabs.enovia.em.NextLabsAgentUtil;
import com.nextlabs.enovia.em.NextLabsLogger;

import matrix.db.Context;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;

public class NextLabsEMInstaller {
	
	private static NextLabsEMInstaller instance;
	
	private static NextLabsLogger logger = null;
	
	private static String sConfigPath = null;
		
	private static final String TRIGGER_PROGRAM = "emxTriggerManager";
	
	private static final String TRIGGER_PROGRAM_PARAMS = "eService Trigger Program Parameters";
	private static final String TRIGGER_PROGRAM_PARAMS_REV = "NextLabsAuthentication";
	private static final String TRIGGER_PROGRAM_PARAMS_POLICY = "eService Trigger Program Policy";
	
	private static final String CUST_ATTR_DESC = "Attribute created by NextLabsEntitlementManagerDeployment tool";
	
	static {
		sConfigPath = NextLabsAgentUtil.getConfigPath();
		
		logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));
	}
	
	private NextLabsEMInstaller() {
		
	}
	
	public synchronized static NextLabsEMInstaller getInstance() {
		if (instance == null) {
			instance = new NextLabsEMInstaller();
		}
		
		return instance;
	}
	
	
	// Configuration Validate Section
	/**
	 * Function to validate deployment configuration file
	 * @param sXMLFile
	 * @return
	 */
	public ConfigValidationResult validateDeploymentConfig(String sXMLFile) {
		ConfigValidator configValidator = new ConfigValidator();
		
		String sXMLSchema = sConfigPath + NextLabsConstant.NXL_DEPLOYMENT_XSD_FILE;
		
		return configValidator.validate(sXMLSchema, sXMLFile);
	}
	
	/**
	 * Function to validate runtime configuration file
	 * @param sXMLFile
	 * @return
	 */
	public ConfigValidationResult validateRuntimeConfig() {
		ConfigValidator configValidator = new ConfigValidator();
		
		String sXMLSchema = sConfigPath + NextLabsConstant.NXL_RUNTIME_XSD_FILE;
		String sXMLFile = sConfigPath + NextLabsConstant.NXL_RUNTIME_FILE;
		
		return configValidator.validate(sXMLSchema, sXMLFile);
	}
	
	/**
	 * Function to validate system configuration file
	 * @param sXMLFile
	 * @return
	 */
	public ConfigValidationResult validateSystemConfig() {
		ConfigValidator configValidator = new ConfigValidator();
		
		String sXMLSchema = sConfigPath + NextLabsConstant.NXL_SYSTEM_XSD_FILE;
		String sXMLFile = sConfigPath + NextLabsConstant.NXL_SYSTEM_FILE;
		
		return configValidator.validate(sXMLSchema, sXMLFile);
	}
	
	
	
	// Policies Section
	/**
	 * Function to retrieve all policies with NXL access check filter 
	 * @param context
	 * @return policies in the expression of Map<String, ArrayList<State>>
	 * @throws MatrixException
	 */
	public Map<String, ArrayList<State>> getAllNXLPolicies(Context context)
			throws MatrixException {
		return getNXLPolicies(context, null);
	}
	
	/**
	 * Function to retrieve all policies with NXL access check filter 
	 * @param context
	 * @param sPolicyName Policy name
	 * @return policies in the expression of Map<String, ArrayList<State>>
	 * @throws MatrixException
	 */
	public Map<String, ArrayList<State>> getNXLPolicy(Context context, String sPolicyName) 
			throws MatrixException {
		return getNXLPolicies(context, sPolicyName);
	}
	
	/**
	 * Function to remove the policies with NXL access check filter
	 * @param context
	 * @param sNXLAccessFilter NextLabs access check filter program line
	 */
	public void remNXLPolicies(Context context, String sNXLAccessFilter, Map<String, ArrayList<State>> policies) {
		try {
			
			MQLCommand mql = new MQLCommand();
			int count = policies.entrySet().size();
			
			Iterator<Entry<String, ArrayList<State>>> ite = policies.entrySet().iterator();
			
			logger.info("Restoring %s policies with NXL access check filter", count);
			
			while (ite.hasNext()) {
				Map.Entry<String, ArrayList<State>> pairs = (Map.Entry<String, ArrayList<State>>) ite.next();
				
				boolean failedToRemFromPolicy = false;

				for (State state : pairs.getValue()) {
					for (User user : state.getUsers()) {
						String sFilterProgram = user.getFilterProgram();
												
						sFilterProgram = sFilterProgram.replace(sNXLAccessFilter + " &&", "");
						sFilterProgram = sFilterProgram.replace(sNXLAccessFilter, "");
						sFilterProgram = sFilterProgram.trim();
						
						String sCommand = "modify policy \"" + pairs.getKey() + 
								"\" state \"" + state.getName() + 
								"\" add user \"" + user.getName() + "\" " + user.getAccessItem() + 
								" filter \"" + sFilterProgram + "\"";
						
						try {
							logger.info("remNXLPolicies() cmd: %s", sCommand);
							mql.executeCommand(context, sCommand);
							
							logger.info("NXL filter is removed from policy:%s, state:%s, user:%s", 
									pairs.getKey(), state.getName(), user.getName());
						} catch (MatrixException me) {
							logger.error("remNXLPolicies() caught exception %s", me, me.getMessage());
							
							failedToRemFromPolicy = true;
						}
					}
				}
				
				if (failedToRemFromPolicy) {
					count -= 1;
					logger.info("Encounter exception when restoring %s, refer to log file for details", pairs.getKey());
				} else {
					logger.info("NXL filter is removed from %s", pairs.getKey());
				}
			}
			
			logger.info("%s policies are restored", count);
		} catch (Exception ex) {
			logger.error("remNXLPolicies() caught exception %s", ex, ex.getMessage());
		}
	}
	
	/**
	 * Function to modify the policies and add in NXL access check filter
	 * @param context
	 * @param arrList
	 * @param nxlFilterString NextLabs access filter program line
	 */
	public void addNXLPolicies(Context context, ArrayList <HashMap<String,Object>> arrList, String sNXLAccessFilter) {
		try {
			MQLCommand mql = new MQLCommand();
			int count = arrList.size();
			
			for (HashMap<String, Object> attrHashMap: arrList) {
				boolean failedToModPolicy = true;
				
				String sPolicyName = (String) attrHashMap.get("name");

				String[] sUserRoles = (String[]) attrHashMap.get("role");
				String[] sStates = (String[]) attrHashMap.get("state");
				
				mql.executeCommand(context, "print policy \"" + sPolicyName + "\" select state dump");
				
				String sState_line = mql.getResult().trim();
				String[] sPolicyStates = sState_line.split(",");
				
				// looping state
				for (String sPolicyState : sPolicyStates) {
					if (isStateIncluded(sPolicyState, sStates)) {
						mql.executeCommand(context, "print policy \"" + sPolicyName + "\" select state[" + sPolicyState + "].filter");
						
						State state = new State(sPolicyState);
						
						String sFilter_line = mql.getResult().trim();
						String[] sFilters = sFilter_line.split("\n");
												
						// looping filter
						for (String sFilter : sFilters) {
							sFilter = sFilter.trim();
							
							int equal_index = sFilter.indexOf("=");
							
							if (equal_index > 0) {
								String sFilter_user = sFilter.substring(sFilter.indexOf(".") + 8, equal_index - 2);
								String sFilter_program = "";						

								if ((equal_index + 2) >= sFilter.length()) {
									sFilter_program = "";
								} else {
									sFilter_program = sFilter.substring(equal_index + 2);
								}
															
								User user = new User(sFilter_user, sFilter_program);
								
								state.getUsers().add(user);
							}
						}
						
						// retrieve access items
						Map<String, String> mapUserAccessItems = getUserAccessItems(context, sPolicyName, sPolicyState);
						
						// setup user access items
						for (User user : state.getUsers()) {
							if (isRoleApplied(user.getName(), sUserRoles)) {
								user.setAccessItem(mapUserAccessItems.get(user.getName()));
								
								if (user.getAccessItem().indexOf("show") >= 0 || 
										user.getAccessItem().indexOf("all") >= 0) {
									
									StringBuffer cmdModPolicy = new StringBuffer("modify policy \"" 
										+ sPolicyName + "\" state \"" + sPolicyState + "\" ");
									
									// modify
									cmdModPolicy.append("add user \"" + user.getName() + "\" " + user.getAccessItem() + " ");
									
									if (user.getFilterProgram().trim().length() > 0) {
										if (user.getFilterProgram().indexOf(sNXLAccessFilter) >= 0) {
											logger.debug("Filter has already been added to policy: %s, state: %s, user: %s", 
													sPolicyName, sPolicyState, user.getName());
											
											continue;
										} else {
											cmdModPolicy.append("filter \"" + sNXLAccessFilter + " && " + user.getFilterProgram() + "\"");
										}
									} else {
										cmdModPolicy.append("filter \"" + sNXLAccessFilter + "\"");
									}
									
									try {
										logger.trace("addNXLPolicies() cmd: %s", cmdModPolicy.toString());
										mql.executeCommand(context, cmdModPolicy.toString());
										
										failedToModPolicy = false;
										
										logger.debug("Filter is added to policy: %s, state: %s, user: %s", 
												sPolicyName, sPolicyState, user.getName());
									} catch (MatrixException me) {
										logger.error("addNXLPolicies() caught exception %s", me, me.getMessage());
									}
								}
								
							}
						} // end of user looping
						
					}
				} // end of state looping
				
				if (failedToModPolicy) {
					count -= 1;
					logger.info("Failed to modify %s", sPolicyName);
				} else {
					logger.info("NXL Filter is added to %s", sPolicyName);
				}
				
			}

			logger.info("%s policies are modified", count);

		} catch (Exception ex) {
			logger.error("addNXLPolicies() caught exception %s", ex, ex.getMessage());
		}
	}
	
	/**
	 * Function to retrieve all policies with NXL access check filter 
	 * @param context
	 * @param sPolicyName null for all policies
	 * @return policies in the expression of Map<String, ArrayList<State>>
	 * @throws MatrixException
	 */
	private Map<String, ArrayList<State>> getNXLPolicies(Context context, String sPolicyName) 
			throws MatrixException {
		Map<String, ArrayList<State>> policies = new HashMap<String, ArrayList<State>>();
				
		MQLCommand mql = new MQLCommand();
		
		// list all the policy
		if (sPolicyName == null)
			mql.executeCommand(context, "list policy * select name dump");
		else
			mql.executeCommand(context, "list policy \"" + sPolicyName + "\" select name dump");
		
		String sPolicy_line = mql.getResult().trim();
		String[] sResults = sPolicy_line.split("\n");
		
		// loop through policies
		for (String sResult : sResults) {
			
			// select all states of each policy
			mql.executeCommand(context, "print policy \"" + sResult + "\" select state dump");
			
			String sState_line = mql.getResult().trim();
			String[] sStates = sState_line.split(",");
			
			// loop through states
			for (String sState : sStates) {
				
				mql.executeCommand(context, "print policy \"" + sResult + "\" select state[" + sState + "].filter");
				
				State state = new State(sState);
				
				String sFilter_line = mql.getResult().trim();
				String[] sFilters = sFilter_line.split("\n");
				
				boolean addToPolicyList = false;
				
				// loop through filters
				for (String sFilter : sFilters) {
					
					if (sFilter.contains(NextLabsConstant.NXL_POLICY_KEYWORD)) {							
						int equal_index = sFilter.indexOf("=");
						
						String sFilter_user = sFilter.substring(sFilter.indexOf(".") + 8, equal_index - 2);
						String sFilter_program = sFilter.substring(equal_index + 2);
													
						User user = new User(sFilter_user, sFilter_program);
						
						state.addUser(user);
						
						addToPolicyList = true;
					}
				} // end of loop through filters
				
				if (addToPolicyList) {
					ArrayList<State> state_list = policies.get(sResult);
					
					if (state_list == null)
						state_list = new ArrayList<State>();
					
					state_list.add(state);
					policies.put(sResult, state_list);
					
					// retrieve access items
					Map<String, String> mapUserAccessItems = getUserAccessItems(context, sResult, sState);
					
					// setup user access items
					for (User user : state.getUsers()) {
						user.setAccessItem(mapUserAccessItems.get(user.getName()));
					}
				}
			} // end of loop through states
		} // end of loop through policies
		
		return policies;
	}
	
	/**
	 * Function to retrieve user access items 
	 * @param context
	 * @param policy
	 * @param state
	 * @return user access items in the expression of Map<String, String>
	 * @throws MatrixException
	 */
	private Map<String, String> getUserAccessItems(Context context, String policy, String state) 
			throws MatrixException {
		Map<String, String> mapUserAccessItems = new HashMap<String, String>();
		
		MQLCommand mql = new MQLCommand();
		
		// retrieve access items
		mql.executeCommand(context, "print policy \"" + policy + "\" select state[" + state + "] dump");
		String sUserAccessItems_line = mql.getResult().trim();
		String[] sUserAccessItems = sUserAccessItems_line.split("\n");
		
		// setup key value pair for user and access items
		for (String sUserAccessItem : sUserAccessItems) {
			sUserAccessItem = sUserAccessItem.trim();
			
			if (sUserAccessItem != null && 
					sUserAccessItem.length() >= 5 && sUserAccessItem.substring(0, 5).equals("user ")) {
				String strUser = sUserAccessItem.split(":")[0].substring(5).trim();
				String accessItems = sUserAccessItem.split(":")[1].trim();
				
				mapUserAccessItems.put(strUser, accessItems);
			}
		}
		
		return mapUserAccessItems;
	}
	
	/**
	 * Checking for the role
	 * @param str Role for checking
	 * @param strArray List of the roles
	 * @return
	 */
	private boolean isRoleApplied(String str, String[] strArray) {
		for (int i = 0; i < strArray.length; i++) {	
			if (("all").equalsIgnoreCase(strArray[i])) {
				return true;
			}

			if (strArray[i].equals(str)) {
				return true;
			}
		}

		return false;
	}
	
	/**
	 * Checking for policy file state
	 * @param str State for checking
	 * @param strArray List of the state
	 * @return
	 */
	private boolean isStateIncluded(String str, String[] strArray) {
		boolean result = false;

		for (int i = 0; i < strArray.length; i++) {
			if (("all").equalsIgnoreCase(strArray[i])) {
				return true;
			}

			if (strArray[i].equals(str)) {
				result = true;
			}
		}

		return result;
	}
		
	
	
	// Trigger Section
	
	/**
	 * Function to retrieve all NXL triggers, both inherited and immediate 
	 * @param context
	 * @return triggers in the expression of Map<String, ArrayList<Trigger>>
	 * @throws MatrixException
	 */
	public Map<String, ArrayList<Trigger>> getAllNXLTriggers(Context context) 
			throws MatrixException{
		return getNXLTriggers(context, false, null);
	}
	
	/**
	 * Function to retrieve immediate NXL triggers
	 * @param context
	 * @return triggers in the expression of Map<String, ArrayList<Trigger>>
	 * @throws MatrixException
	 */
	public Map<String, ArrayList<Trigger>> getImmediateNXLTriggers(Context context) 
			throws MatrixException {
		return getNXLTriggers(context, true, null);
	}
	
	/**
	 * Function to retrieve immediate NXL triggers from specified type
	 * @param context
	 * @return triggers in the expression of Map<String, ArrayList<Trigger>>
	 * @throws MatrixException
	 */
	public Map<String, ArrayList<Trigger>> getImmediateNXLTriggers(Context context, String sTypeName) 
			throws MatrixException {
		return getNXLTriggers(context, true, sTypeName);
	}
		
	/**
	 * Function to remove the NXL triggers from BO type
	 * @param context
	 */
	public void remNXLTriggers(Context context, Map<String, ArrayList<Trigger>> typeTriggers) {
		try {
			// must set vault to eService Administration 
			// in order to insert eService Trigger Program Parameters 
			context.resetContext(context.getUser(), context.getPassword(), "eService Administration");
									
			MQLCommand mql = new MQLCommand();
			int count = typeTriggers.entrySet().size();
						
			Iterator<Entry<String, ArrayList<Trigger>>> ite = typeTriggers.entrySet().iterator();

			logger.info("Restoring %s types with NXL triggers", count);

			while (ite.hasNext()) {
				Map.Entry<String, ArrayList<Trigger>> pairs = (Map.Entry<String, ArrayList<Trigger>>) ite.next();
				ArrayList<Trigger> lstTrigger = pairs.getValue();
				
				boolean failedToRemFromType = false;
				
				for (Trigger trigger : lstTrigger) {
					String sEventType = trigger.getEventType(); 	// i.e. RemoveFile, Checkin, and etc
					String sTriggerType = trigger.getTriggerType();	// i.e. Check
					String sProgram = trigger.getProgram();			// i.e. emxTriggerProgram
					ArrayList<String> arguments = trigger.getArguments();
					
					StringBuffer cmdRemoveTrigger;
					
					// only one item (must be NextLabs)
					// remove the event trigger
					if (arguments.size() == 1) {
						cmdRemoveTrigger = new StringBuffer("modify type \"" + pairs.getKey() + 
								"\" remove trigger " + sEventType + " " + sTriggerType);
						
						deleteTriggerProgParams(context, arguments.get(0));
					} else {
						cmdRemoveTrigger = new StringBuffer("modify type \"" + pairs.getKey() + 
								"\" add trigger " + sEventType + " " + sTriggerType + " " + sProgram + " input \"");
						
						for (String argument : arguments) {
							if (argument.indexOf(NextLabsConstant.NXL_TRIGGER_KEYWORD) < 0) {
								cmdRemoveTrigger.append(argument + " ");
							} else {
								if (deleteTriggerProgParams(context, argument)) {
									logger.debug("Removed %s %s", TRIGGER_PROGRAM_PARAMS, argument);
								} else {
									logger.debug("Failed to remove %s %s", TRIGGER_PROGRAM_PARAMS, argument);
								}
							}
						}
						
						cmdRemoveTrigger.append("\"");
					}
					
					try {
						logger.trace("remNXLTriggers() cmd: %s", cmdRemoveTrigger.toString());
						mql.executeCommand(context, cmdRemoveTrigger.toString());
						
						logger.debug("NXL trigger is removed from %s %s", pairs.getKey(), sEventType);
					} catch (MatrixException me) {						
						logger.error("remNXLTriggers() caught exception %s", me, me.getMessage());
						
						failedToRemFromType = true;
					}
				}
				
				if (failedToRemFromType) {
					count -= 1;
					logger.info("Encounter exception when restoring %s, refer to log file for details", pairs.getKey());
				} else {
					logger.info("NXL triggers are removed from %s", pairs.getKey());
				}
				
			}
			
			logger.info("%s types are restored", count);
		} catch (Exception ex) {
			logger.error("remNXLTriggers() caught exception %s", ex, ex.getMessage());
		}
	}
	
	/**
	 * Function to add the NXL triggers to BO type
	 * @param context
	 * @param arrList List of trigger to be added
	 * @param sTriggerProgParams eService Trigger Program Parameters
	 */
	public void addNXLTriggers(Context context, ArrayList <HashMap<String,String>> arrList, String sTriggerProgParams) {
		try {
			// must set vault to eService Administration 
			// in order to insert eService Trigger Program Parameters 
			context.resetContext(context.getUser(), context.getPassword(), "eService Administration");
			
			MQLCommand mql = new MQLCommand();
			int count = arrList.size();
			
			for (HashMap<String,String> attrHashMap: arrList) {
				String sTriggerName = (String) attrHashMap.get("name");
				String sAction = (String) attrHashMap.get("action");
				String sType = (String) attrHashMap.get("type");
				
				// check is there any configured trigger input
				StringBuffer argument_line = getTriggerProgArgs(context, "immediatetrigger", sType, sAction);
				
				// if there is no immediate trigger for the action yet
				// search trigger (both inherited and immediate)
				if (argument_line.length() <= 0) {
					argument_line = getTriggerProgArgs(context, "trigger", sType, sAction);
				}
				
				// append NextLabs Trigger to configured input
				if (argument_line.indexOf(sTriggerName) >= 0) {
					logger.info("%s has already been added to %s %s", sTriggerName, sType, sAction);
					
					count -= 1;
					
					continue;
				} else {
					argument_line.append(sTriggerName);
				}
				
				String cmdAddTrigger = "modify type \"" + sType + "\" add trigger " + sAction + 
						" check " + TRIGGER_PROGRAM + " input \"" + argument_line.toString() + "\"";
				
				StringBuffer cmdAddTriggerProgParams = new StringBuffer("add businessobject \"" + 
						TRIGGER_PROGRAM_PARAMS + "\" \"" + sTriggerName + "\" \"" + TRIGGER_PROGRAM_PARAMS_REV + "\" ");
				
				cmdAddTriggerProgParams.append("policy \"" + TRIGGER_PROGRAM_PARAMS_POLICY + "\" ");
				
				String sData = sTriggerProgParams;
				sData = sData.replace("${ACTION}", sAction.toUpperCase());
				
				cmdAddTriggerProgParams.append(sData);
				
				String cmdPromoteTriggerProgParams = "promote businessobject \"" + TRIGGER_PROGRAM_PARAMS + "\" \"" + 
						sTriggerName + "\" \"" + TRIGGER_PROGRAM_PARAMS_REV + "\"";

				try {
					logger.trace("addNXLTriggers() cmd1: %s", cmdAddTrigger);
					mql.executeCommand(context, cmdAddTrigger);					
					
					logger.trace("addNXLTriggers() cmd2: %s", cmdAddTriggerProgParams.toString());
					mql.executeCommand(context, cmdAddTriggerProgParams.toString());
					
					logger.trace("addNXLTriggers() cmd3: %s", cmdPromoteTriggerProgParams);
					mql.executeCommand(context, cmdPromoteTriggerProgParams);
					
					logger.info("%s is added to %s %s", sTriggerName, sType, sAction);
				} catch (MatrixException me) {
					logger.error("addNXLTriggers() caught exception %s", me, me.getMessage());
				}
			}
			
			logger.info("%s triggers are added", count);
		} catch (Exception ex) {
			logger.error("addNXLTriggers() caught exception %s", ex, ex.getMessage());
		}
	}
	
	/**
	 * Function to retrieve NXL triggers, either both(inherited and immediate) or immediate only
	 * @param context
	 * @param immediateOnly
	 * @return trigger in the expression of May<String, ArrayList<Trigger>>
	 * @throws MatrixException
	 */
	private Map<String, ArrayList<Trigger>> getNXLTriggers(Context context, boolean immediateOnly, String sTypeName) 
			throws MatrixException {
		Map<String, ArrayList<Trigger>> typeTriggers = new HashMap<String, ArrayList<Trigger>>();
		
		MQLCommand mql = new MQLCommand();

		// list all the type in Enovia system
		if (sTypeName == null)
			mql.executeCommand(context, "list type * select name dump");
		else
			mql.executeCommand(context, "list type \"" + sTypeName + "\" select name dump");
		
		String sType_line = mql.getResult().trim();
		String[] sTypes = sType_line.split("\n");
		
		// loop through types
		for (String sType : sTypes) {
		
			if (immediateOnly)
				// only select immediate trigger
				mql.executeCommand(context, "print type \"" + sType + "\" select immediatetrigger dump");
			else
				// select all trigger (immediate and inherited)
				mql.executeCommand(context, "print type \"" + sType + "\" select trigger dump");
			
			String sTrigger_line = mql.getResult().trim();
			String[] sTriggers = sTrigger_line.split(",");
			
			// loop through triggers
			for (String sTrigger : sTriggers) {
				
				if (sTrigger.indexOf(NextLabsConstant.NXL_TRIGGER_KEYWORD) > 0) {
					
					String[] sEvent_prog = sTrigger.split(":");
					
					String sEvent = sEvent_prog[0];
					String sProg_argu = sEvent_prog[1];
					
					String sArgument_line = sProg_argu.substring(sProg_argu.indexOf("(") + 1, sProg_argu.lastIndexOf(")"));
					String[] sArguments = sArgument_line.split(" ");
					
					Trigger trigger = new Trigger(sEvent, TRIGGER_PROGRAM);
					for (String sArgument : sArguments) {						
						trigger.addArgument(sArgument.trim());
					}
					
					ArrayList<Trigger> type_trigger_list = typeTriggers.get(sType);
					
					if (type_trigger_list == null)
						type_trigger_list = new ArrayList<Trigger>();
					
					type_trigger_list.add(trigger);
					typeTriggers.put(sType, type_trigger_list);
				}
			} // end of loop through triggers
		} // end of loop through types
				
		return typeTriggers;
	}
	
	/**
	 * Function to retrieve Trigger Program (emxTriggerManager) arguments for specified type and action
	 * @param context
	 * @param sSelectStatement immediatetrigger or trigger
	 * @param sType BO Type
	 * @param sAction i.e. Checkin, Checkout, RemoveFile
	 * @return program arguments in the expression of StringBuffer
	 * @throws MatrixException
	 */
	private StringBuffer getTriggerProgArgs(Context context, String sSelectStatement, String sType, String sAction) 
			throws MatrixException {
		MQLCommand mql = new MQLCommand();
		
		mql.executeCommand(context, "print type \"" + sType + "\" select " + sSelectStatement + " dump");
		
		String trigger_line = mql.getResult().trim();
		String[] triggers = trigger_line.split(",");
		StringBuffer argument_line = new StringBuffer("");
		
		for (int j = 0; j < triggers.length; j++) {
			String triggerLine = triggers[j];
			
			// looking for check trigger type
			if (triggerLine.indexOf(sAction + "Check") >= 0) {
				String[] event_prog = triggerLine.split(":");
				
				// 0: event
				// 1: program
				String prog_argu = event_prog[1];
				
				if (prog_argu.indexOf(TRIGGER_PROGRAM) >= 0) {
					argument_line.append(prog_argu.substring(prog_argu.indexOf("(") + 1, 
							prog_argu.lastIndexOf(")")).trim() + " ");
				}
			}
		}
		
		return argument_line;
	}
	
	/**
	 * Check if a BO eService Trigger Program Parameters exist in Enovia
	 * @param context
	 * @param triggerName
	 * @return
	 * @throws Exception
	 */
	private boolean isTriggerProgParamsExist(Context context, String triggerName) throws Exception {
		StringBuffer cmdPrint = new StringBuffer();
		
		cmdPrint.append("print businessobject \"" + TRIGGER_PROGRAM_PARAMS + "\" \"")
			.append(triggerName).append("\" \"").append(TRIGGER_PROGRAM_PARAMS_REV).append("\"");
		
		logger.trace("isTriggerProgParamsExist() cmd: %s", cmdPrint);
		
		String result = null;
		
		try {
			result = MqlUtil.mqlCommand(context, cmdPrint.toString());
		} catch (FrameworkException fe) {
			logger.error("isTriggerProgParamsExist() caught exception %s", fe, fe.getMessage());
			
			return false;
		}
		
		if (null == result || result.contains("Error:")) {
			return false;
		}
		
		return true;
	}
	
	/**
	 * Method to delete BO eService Trigger Program Parameters from Enovia
	 * @param context
	 * @param triggerName
	 * @return
	 * @throws Exception
	 */
	private boolean deleteTriggerProgParams(Context context, String triggerName) throws Exception {
		if (isTriggerProgParamsExist(context, triggerName)) {
			StringBuffer cmdRemoveTriggerProgParams = new StringBuffer("delete businessobject \"" + 
					TRIGGER_PROGRAM_PARAMS + "\" \"" + triggerName + "\" \"" + TRIGGER_PROGRAM_PARAMS_REV + "\"");
			
			logger.trace("deleteTriggerProgParams() cmd: %s", cmdRemoveTriggerProgParams);
			
			String result = null;
			
			try {
				result = MqlUtil.mqlCommand(context, cmdRemoveTriggerProgParams.toString());
			} catch (FrameworkException fe) {
				logger.error("deleteTriggerProgParams() caught exception %s", fe, fe.getMessage());
				
				throw fe;
			}
			
			if ("".equals(result)) {
				return true;
			}
		}
		
		return false;
	}

	
	
	// Custom Attributes Section
	
	/**
	 * Function to retrieve all NXL custom attributes, by specified prefix (i.e. nxl_)
	 * @param context
	 * @return NXL custom attributes in the expression of ArrayList<String>
	 * @throws FrameworkException
	 */
	public ArrayList<String> getAllNXLCustomAttributes(Context context) 
			throws FrameworkException {		
		return getNXLCustomAttributes(context, NextLabsConstant.NXL_CUST_ATTR_PREFIX + "*");
	}
		
	/**
	 * Method to delete the attribute from Enovia system
	 * @param attrName Attribute name
	 * @return true if deleted, false if delete fail
	 * @throws Exception
	 */
	public boolean deleteCustomResourceAttribute(Context context, String attrName) throws Exception {
		try {
			if (attributeExists(context, attrName)) {
				if (removeAttribute(context, attrName)) {
					logger.info("Attribute %s is removed", attrName);

					return true;
				} else {
					logger.info("Failed ot remove attribute %s", attrName);
					
					return false;
				}
			} else {
				logger.info("Attribute %s does not exist, skip deleting", attrName);
				
				return false;
			}				
		} catch (Exception ex) {
			logger.error("deleteCustomResourceAttribute caught exception %s", ex, ex.getMessage());
			
			return false;
		}
	}
	
	/**
	 * Create the custom attribute and apply to system
	 * 
	 * @param attrName name of new attribute
	 * @param type type to associate the attribute
	 * @param format format of new attribute (should be String)
	 * @param range range of the attributes in string array (none,ITAR,BAFA)
	 * @param defaultValue a default value
	 * @return true is create successfully, false if create fail
	 * @throws Exception
	 */
	public boolean createCustomResourceAttribute(Context context, String sAttrName, String sType, 
			String format, String[] range, String defaultValue) throws Exception {
		try {
			if (!attributeExists(context, sAttrName)) {
				createAttribute(context, sAttrName, format, 
						range, defaultValue);
			}
			
			logger.debug("Start adding atttribute to object type");
			
			if (addAttributeToType(context, sAttrName, sType)) {
				logger.info("Attribute %s is added to object type %s", 
						sAttrName, sType);

				return true;
			} else {
				logger.info("Failed to add attribute %s to object type %s", 
						sAttrName, sType);

				return false;
			}			
		} catch (Exception ex) {
			logger.error("createCustomResourceAttribute() caught exception %s", ex, ex.getMessage());

			throw ex;
		}
	}
	
	/**
	 * Function to retrieve all NXL custom attributes, by specified prefix (i.e. nxl_)
	 * @param context
	 * @return NXL custom attributes in the expression of ArrayList<String>
	 * @throws FrameworkException
	 */
	private ArrayList<String> getNXLCustomAttributes(Context context, String sAttributeName) 
			throws FrameworkException {
		ArrayList<String> nxlCustAttrs = new ArrayList<String>();
		
		StringBuffer cmdListAttrs = new StringBuffer();
		
		cmdListAttrs.append("list attribute \"").append(sAttributeName).append("\"");
		
		logger.trace("getNXLCustomAttributes() cmd: %s", cmdListAttrs.toString());
		
		String result = null;
		
		try {
			result = MqlUtil.mqlCommand(context, cmdListAttrs.toString());
			
			String[] custAttrs = result.split("\n");
			
			for (String custAttr: custAttrs) {
				if (!custAttr.trim().equals(""))
					nxlCustAttrs.add(custAttr);
			}
		} catch (FrameworkException fe) {
			logger.error("getNXLCustomrAttributes() caught exception %s", fe, fe.getMessage());
			
			throw fe;
		}
		
		return nxlCustAttrs;
	}
	
	/**
	 * Remove the attribute of a business object type from Enovia
	 * @param context
	 * @param attrName
	 * @return
	 * @throws Exception
	 */
	private boolean removeAttribute(Context context, String attrName) 
			throws Exception {
		StringBuffer cmdRemAttr = new StringBuffer();
		
		cmdRemAttr.append("delete attribute \"").append(attrName).append("\"");
		
		logger.trace("removeAttribute() cmd: %s", cmdRemAttr);
		
		String result = null;
		
		try {
			result = MqlUtil.mqlCommand(context, cmdRemAttr.toString());
		} catch (FrameworkException fe) {
			logger.error("removeAttribute() caught exception %s", fe, fe.getMessage());
			
			throw fe;
		}
		
		if ("".equals(result)) {
			return true;
		}
		
		return false;
	}

	/**
	 * This function create the custom attributes based on the input
	 * 
	 * @param context
	 * @param attrName
	 * @param format
	 * @param ranges
	 * @param defaultValue
	 * @return
	 * @throws Exception
	 */
	private void createAttribute(Context context, String attrName, 
			String format, String[] ranges, String defaultValue) throws Exception {		
		StringBuffer rangeBuf = new StringBuffer();
		
		for (String sValue : ranges) {
			if (sValue != null && sValue.length() > 0) {
				rangeBuf.append(" range = \"").append(sValue).append("\" ");
			}
		}
		
		logger.debug("Range value %s", rangeBuf);
		
		StringBuffer cmdAddAttr = new StringBuffer();
		
		cmdAddAttr.append("add attribute \"").append(attrName).append("\" type \"")
			.append(format).append("\" description \"").append(CUST_ATTR_DESC)
			.append("\" default \"").append(defaultValue).append("\"")
			.append(rangeBuf.toString());
		
		logger.trace("createAttribute() cmd: %s", cmdAddAttr);
		
		try {
			MqlUtil.mqlCommand(context, cmdAddAttr.toString());
		} catch (FrameworkException fe) {
			logger.error("createAttribute() caught exception %s", fe, fe.getMessage());
			
			throw fe;
		}
	}

	/**
	 * Method use to determine is the
	 * 
	 * @param context
	 * @param attrName
	 * @return
	 * @throws FrameworkException
	 */
	private boolean attributeExists(Context context, String attrName) 
			throws FrameworkException {
		String cmd = "list attribute \"" + attrName + "\"";
		String result = null;
		
		try {
			result = MqlUtil.mqlCommand(context, cmd);
		} catch (FrameworkException fe) {
			logger.error("attributeExists() caught exception %s", fe, fe.getMessage());
			
			throw fe;
		}
		
		if ("".equals(result)) {
			return false;
		}
		
		logger.debug("Attribute %s already exist in system ", attrName);
		
		return true;
	}
		
	/**
	 * Function to add custom attribute to BO type
	 * 
	 * @param attrName Attribute name
	 * @param typeName BO type
	 * @return
	 * @throws Exception
	 */
	private boolean addAttributeToType(Context context, String attrName, 
			String typeName) throws FrameworkException {
		StringBuffer cmdModType = new StringBuffer();
		
		cmdModType.append("modify type \"").append(typeName)
			.append("\" add attribute \"").append(attrName).append("\"");
		
		logger.trace("addAttributeToType() cmd: %s", cmdModType);
		
		String result = null;
		
		try {
			result = MqlUtil.mqlCommand(context, cmdModType.toString());
		} catch (FrameworkException fe) {
			logger.error("addAttributeToType() caught exception %s", fe, fe.getMessage());
			
			throw fe;
		}
				
		if (("").equals(result)) {
			return true;
		}
		
		return false;
	}
	
}
