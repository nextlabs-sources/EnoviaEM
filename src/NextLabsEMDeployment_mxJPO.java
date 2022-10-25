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

import matrix.db.Context;
import matrix.util.MatrixException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.matrixone.apps.domain.util.FrameworkException;
import com.nextlabs.enovia.em.NextLabsDeploymentConfig;
import com.nextlabs.enovia.em.NextLabsLogger;
import com.nextlabs.enovia.em.NextLabsSystemConfig;
import com.nextlabs.enovia.common.NextLabsConstant;
import com.nextlabs.enovia.em.NextLabsAgentUtil;
import com.nextlabs.enovia.em.installer.ConfigValidationResult;
import com.nextlabs.enovia.em.installer.NextLabsEMInstaller;
import com.nextlabs.enovia.em.installer.State;
import com.nextlabs.enovia.em.installer.Trigger;


/**
 * @author klee
 * @version: $Id: //depot/EnoviaEnforcer/EntitlementManager/dev/FirstVersion_1206/src/NextLabsEntitlementManagerDeployment_mxJPO.java
 */
public class NextLabsEMDeployment_mxJPO implements NextLabsConstant {

	private static NextLabsEMInstaller emInstaller = null;
	private static NextLabsSystemConfig sysConfig = null;
	private static NextLabsLogger logger = null;
	private static String sConfigPath = null;

	static {
		emInstaller = NextLabsEMInstaller.getInstance();
		
		sConfigPath = NextLabsAgentUtil.getConfigPath();

		logger = 
			new NextLabsLogger(Logger.getLogger("nxlSystemConfig"));

		sysConfig = NextLabsSystemConfig.getInstance();
	}
	
	/**
	 * Constructor for the class
	 */
	public NextLabsEMDeployment_mxJPO() {
		PropertyConfigurator.configure(sConfigPath + LOG4J_CONFIG_FILE);
	}
	
	/**
	 * Function to validate the deployment file
	 * @param context
	 * @param args
	 * @throws FrameworkException
	 */
	public void validateDeploymentFile(Context context, String[] args) throws FrameworkException {
		try {
			if (args.length != 2 || !args[0].equals("-file")) {
				logger.info("Invalid command");
				logger.info("Usage: exec prog NextLabsEMDeployment -method validateDeploymentFile -file <filename>");
				
				return;
			}
			
			String fileName = args[1];
			
			ConfigValidationResult result = emInstaller.validateDeploymentConfig(fileName);
			
			if (result.isValid()) {
				logger.info("Valid deployment file");
			} else {
				logger.info("Invalid deployment file. Reason: %s", result.getMessage());
			}
		} catch (Exception ex) {
			logger.error("validateDeploymentFile() caught exception %s", ex, ex.getMessage());

			throw new FrameworkException(ex);
		}
	}
	
	/**
	 * Function to validate the runtime file
	 * @param context
	 * @param args
	 * @throws FrameworkException
	 */
	public void validateRuntimeFile(Context context, String[] args) throws FrameworkException {
		try {
			ConfigValidationResult result = emInstaller.validateRuntimeConfig();
			
			if (result.isValid()) {
				logger.info("Valid runtime file");
			} else {
				logger.info("Invalid runtime file. Reason: %s", result.getMessage());
			}
		} catch (Exception ex) {
			logger.error("validateRuntimeFile() caught exception %s", ex, ex.getMessage());

			throw new FrameworkException(ex);
		}
	}
	
	/**
	 * Function to validate the system file
	 * @param context
	 * @param args
	 * @throws FrameworkException
	 */
	public void validateSystemFile(Context context, String[] args) throws FrameworkException {
		try {
			ConfigValidationResult result = emInstaller.validateSystemConfig();
			
			if (result.isValid()) {
				logger.info("Valid system file");
			} else {
				logger.info("Invalid system file. Reason: %s", result.getMessage());
			}
		} catch (Exception ex) {
			logger.error("validateSystemFile() caught exception %s", ex, ex.getMessage());

			throw new FrameworkException(ex);
		}
	}
	
	/**
	 * Function to list out all the policies with NXL access check filter
	 * @param context Enovia Matrix context
	 * @param args Not in use
	 */
	public void listNXLPolicies(Context context, String[] args) throws FrameworkException {
		try {
			Map<String, ArrayList<State>> nxlPolicies = emInstaller.getAllNXLPolicies(context);
			
			Iterator<Entry<String, ArrayList<State>>> ite = nxlPolicies.entrySet().iterator();
			
			logger.info(nxlPolicies.entrySet().size() + " policies with NXL triggers");
			
			int index = 1;
			while (ite.hasNext()) {
				Map.Entry<String, ArrayList<State>> pairs = (Map.Entry<String, ArrayList<State>>) ite.next();

				logger.info("%s : %s:", index, pairs.getKey());
				logger.info(pairs.getValue().toString());
				logger.info("\n");
				
				index += 1;
			}
		} catch (MatrixException me) {
			logger.error("listNXLPolicies() caught exception %s", me, me.getMessage());
			
			throw new FrameworkException(me);
		}
	}
	
	/**
	 * Function to modify the specified policies and add in NXL access check filter
	 * @param context Enovia Matrix context
	 * @param args Not in use
	 */
	public void modifyEnoviaPolicies(Context context, String[] args) throws FrameworkException {
		try {
			if (args.length != 2 || !args[0].equals("-file")) {
				logger.info("Invalid command");
				logger.info("Usage: exec prog NextLabsEMDeployment -method modifyEnoviaPolicies -file <filename>");
				
				return;
			}
			
			String fileName = args[1];
			
			// Validate deployment file
			ConfigValidationResult result = emInstaller.validateDeploymentConfig(fileName);
			
			if (result.isValid()) {
				logger.info("Valid deployment file");
			} else {
				logger.info("Invalid deployment file. Reason: %s", result.getMessage());
				
				return;
			}
			
			NextLabsDeploymentConfig depConfig = new NextLabsDeploymentConfig(fileName);
			
			ArrayList<HashMap<String,Object>> arrList = depConfig.getPolicies();
			
			emInstaller.addNXLPolicies(context, arrList, sysConfig.getNextLabsFilterString());
		} catch (Exception ex) {
			logger.error("modifiyEnoviaPolicies() caught exception %s", ex, ex.getMessage());

			throw new FrameworkException(ex);
		}
	}
	
	/**
	 * Function to remove NXL access check filter from all modified policies
	 * @param context Enovia Matrix context
	 * @param args Not in use
	 */
	public void restoreEnoviaPolicies(Context context, String[] args) throws FrameworkException {
		try {
			if (args.length > 0) {
				logger.info("Invalid command");
				logger.info("Usage: exec prog NextLabsEMDeployment -method restoreEnoviaPolicy -policy <policyname>");
				logger.info("Usage: exec prog NextLabsEMDeployment -method restoreEnoviaPolicies");
				
				return;
			}
			
			// retrieve the policies with NXL access check filter
			Map<String, ArrayList<State>> policies = emInstaller.getAllNXLPolicies(context);
			
			emInstaller.remNXLPolicies(context, sysConfig.getNextLabsFilterString(), policies);
			
		} catch (Exception ex) {
			logger.error("restoreEnoviaPolicies() caught exception %s", ex, ex.getMessage());
			
			throw new FrameworkException(ex);
		}
	}
	
	/**
	 * Function to remove NXL access check filter from specified policy
	 * @param context Enovia Matrix context
	 * @param args
	 * @throws FrameworkException
	 */
	public void restoreEnoviaPolicy(Context context, String[] args) throws FrameworkException {
		try {
			
			if (args.length != 2 || !args[0].equals("-policy")) {
				logger.info("Invalid command");
				logger.info("Usage: exec prog NextLabsEMDeployment -method restoreEnoviaPolicy -policy <policyname>");
				
				return;
			}
			
			String policyName = args[1];
			
			// retrieve the policies with NXL access check filter
			Map<String, ArrayList<State>> policies = emInstaller.getNXLPolicy(context, policyName);
			
			emInstaller.remNXLPolicies(context, sysConfig.getNextLabsFilterString(), policies);
		} catch (Exception ex) {
			logger.error("restoreEnoviaPolicies() caught exception %s", ex, ex.getMessage());
			
			throw new FrameworkException(ex);
		}
	}
	
	/**
	 * Function to list out all NXL Triggers, both inherited and immediate
	 * @param context
	 * @param args
	 * @throws FrameworkException
	 */
	public void listAllNXLTriggers(Context context, String[] args) throws FrameworkException {
		try {
			Map<String, ArrayList<Trigger>> typeTriggers = emInstaller.getAllNXLTriggers(context);
			
			Iterator<Entry<String, ArrayList<Trigger>>> ite = typeTriggers.entrySet().iterator();
	
			logger.info(typeTriggers.entrySet().size() + " types with NXL triggers");
			
			int index = 1;
			while (ite.hasNext()) {
				Map.Entry<String, ArrayList<Trigger>> pairs = (Map.Entry<String, ArrayList<Trigger>>) ite.next();
				
				logger.info("%s: %s=%s", index, pairs.getKey(), pairs.getValue());
				logger.info("\n");
				
				index += 1;
			}
		} catch (MatrixException me) {
			logger.error("listAllNXLTriggers() caught exception %s", me, me.getMessage());
			
			throw new FrameworkException(me);
		}
	}
	
	/**
	 * Function to list out all immediate NXL triggers
	 * @param context
	 * @param args
	 * @throws FrameworkException
	 */
	public void listNXLTriggers(Context context, String[] args) throws FrameworkException {
		try {
			Map<String, ArrayList<Trigger>> typeTriggers = emInstaller.getImmediateNXLTriggers(context);
			
			Iterator<Entry<String, ArrayList<Trigger>>> ite = typeTriggers.entrySet().iterator();
	
			logger.info(typeTriggers.entrySet().size() + " types with NXL triggers");
			
			int index = 1;
			while (ite.hasNext()) {
				Map.Entry<String, ArrayList<Trigger>> pairs = (Map.Entry<String, ArrayList<Trigger>>) ite.next();
				
				logger.info("%s: %s=%s", index, pairs.getKey(), pairs.getValue());
				logger.info("\n");
				
				index += 1;
			}
		} catch (MatrixException me) {
			logger.error("listNXLTriggers() caught exception %s", me, me.getMessage());
			
			throw new FrameworkException(me);
		}
	}
	
	/**
	 * Method use to deploy the trigger to Enovia
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void modifyEnoviaTriggers(Context context, String[] args) throws FrameworkException {
		try {
			if (args.length != 2) {
				logger.info("Invalid command");
				logger.info("Usage: exec prog NextLabsEMDeployment -method modifyEnoviaTriggers -file <filename>");
				
				return;
			}
			
			String fileName = args[1];
			
			// Validate deployment file
			ConfigValidationResult result = emInstaller.validateDeploymentConfig(fileName);
			
			if (result.isValid()) {
				logger.info("Valid deployment file");
			} else {
				logger.info("Invalid deployment file. Reason: %s", result.getMessage());
				
				return;
			}
			
			NextLabsDeploymentConfig depConfig = new NextLabsDeploymentConfig(fileName);
			
			logger.debug("Getting trigger to update");
			ArrayList <HashMap<String,String>> arrList = depConfig.getTriggers();
			
			emInstaller.addNXLTriggers(context, arrList, sysConfig.getTriggerString());
		} catch (Exception ex) {
			logger.error("modifyEnoviaTriggers() caught exception %s", ex, ex.getMessage());
	
			throw new FrameworkException(ex);
		}
	}
	
	/**
	 * Method use to remove the trigger from Enovia
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void restoreEnoviaTriggers(Context context, String[] args) throws FrameworkException {
		try {
			Map<String, ArrayList<Trigger>> typeTriggers;
			
			if (args.length > 0) {
				if (args.length != 2 || !args[0].equals("-type")) {
					logger.info("Invalid command");
					logger.info("Usage: exec prog NextLabsEMDeployment -method restoreEnoviaTriggers -type <typename>");
					logger.info("Usage: exec prog NextLabsEMDeployment -method restoreEnoviaTriggers");
					
					return;
				}
				
				String typeName = args[1];
				
				typeTriggers = emInstaller.getImmediateNXLTriggers(context, typeName);
			} else {
				logger.debug("Getting trigger to restore");
				
				typeTriggers = emInstaller.getImmediateNXLTriggers(context);
			}
			
			emInstaller.remNXLTriggers(context, typeTriggers);
			
		} catch (Exception ex) {
			logger.error("restoreEnoviaTriggers() caught exception %s", ex, ex.getMessage());

			throw new FrameworkException(ex);
		}
	}
		
	public void listSecurityClassificationAttributes(Context context, String[] args) throws FrameworkException {
		try {
			ArrayList<String> attrs = emInstaller.getAllNXLCustomAttributes(context);
			
			logger.info("%s security classification attributes found", attrs.size());
			
			int index = 1;
			for (String attr: attrs) {
				logger.info("%s: %s", index, attr);
				
				index += 1;
			}
		} catch (Exception ex) {
			logger.error("listSecurityClassificationAttributes() caught exception %s", ex, ex.getMessage());
			
			throw new FrameworkException(ex);
		}
	}

	/**
	 * Create security classification based on configuration file
	 * @param context Enovia matrix context
	 * @param args
	 * @throws Exception
	 */
	public void createSecurityClassificationAttributes(Context context, String[] args) throws FrameworkException {
		try {
			if (args.length != 2 || !args[0].equals("-file")) {
				logger.info("Invalid command");
				logger.info("Usage: exec prog NextLabsEMDeployment -method createSecurityClassificationAttributes -file <filename>");
				
				return;
			}
			
			String fileName = args[1];
			
			// Validate deployment file
			ConfigValidationResult result = emInstaller.validateDeploymentConfig(fileName);
			
			if (result.isValid()) {
				logger.info("Valid deployment file");
			} else {
				logger.info("Invalid deployment file. Reason: %s", result.getMessage());
				
				return;
			}
						
			NextLabsDeploymentConfig depConfig = new NextLabsDeploymentConfig(fileName);
			
			context.start(true);

			logger.debug("Start adding attributes");
			ArrayList <HashMap<String,Object>> arrList = depConfig.getAttributeList();
			logger.debug("Create custom resource attributes...");
			
			for (HashMap<String,Object> attrHashMap: arrList) {
				logger.debug(attrHashMap.toString());

				String attrName = (String) attrHashMap.get("name");
				String type = (String) attrHashMap.get("type");
				String format = (String) attrHashMap.get("format");
				String[] values = (String[]) attrHashMap.get("value");
				String defaultValue = (String) attrHashMap.get("default");

				emInstaller.createCustomResourceAttribute(context, attrName, type, format, values, defaultValue);
			}

			context.commit();

			logger.debug("End Adding attributes");
		} catch (Exception ex) {
			try {
				if (context.isTransactionActive())
					context.abort();
			} catch (MatrixException me) {
				logger.error("createSecurityClassificationAttributes() caught exception %s", me, me.getMessage());
				throw new FrameworkException(me);
			}
			
			logger.error("createSecurityClassificationAttributes() caught exception %s", ex, ex.getMessage());
			throw new FrameworkException(ex);
		}
	}

	/**
	 * Delete the security classification that had been added to Enovia
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void deleteSecurityClassificationAttributes(Context context, String[] args) throws Exception {
		try {
			if (args.length > 0) {
				logger.info("Invalid command");
				logger.info("Usage: exec prog NextLabsEMDeployment -method deleteSecurityClassificationAttribute -attribute <attributename>");
				logger.info("Usage: exec prog NextLabsEMDeployment -method deleteSecurityClassificationAttributes");
				
				return;
			}
			
			context.start(true);

			logger.debug("Start delete attributes");
			ArrayList<String> arrList = emInstaller.getAllNXLCustomAttributes(context);
			logger.debug("Delete custom resource attributes...");

			for (String attr: arrList) {
				emInstaller.deleteCustomResourceAttribute(context, attr);
			}
			
			context.commit();
			logger.debug("End deleting attributes");
		} catch (Exception ex) {
			if (context.isTransactionActive())
				context.abort();
			
			logger.error("deleteSecurityClassificationAttributes() caught exception %s", ex, ex.getMessage());

			throw ex;
		}
	}
	
	/**
	 * Delete the security classification that had been added to Enovia
	 * @param context
	 * @param args
	 * @throws Exception
	 */
	public void deleteSecurityClassificationAttribute(Context context, String[] args) throws Exception {
		try {
			if (args.length != 2 || !args[0].equals("-attribute")) {
				logger.info("Invalid command");
				logger.info("Usage: exec prog NextLabsEMDeployment -method deleteSecurityClassificationAttribute -attribute <attributename>");
				
				return;
			}
			
			String attributeName = args[1];
			
			context.start(true);

			logger.debug("Start delete attributes");
			logger.debug("Delete custom resource attributes...");

			emInstaller.deleteCustomResourceAttribute(context, attributeName);
			
			context.commit();
			logger.debug("End deleting attributes");
		} catch (Exception ex) {
			if (context.isTransactionActive())
				context.abort();
			
			logger.error("deleteSecurityClassificationAttributes() caught exception %s", ex, ex.getMessage());

			throw ex;
		}
	}
	
	/**
	 * Main entry point for JPO
	 * @param context
	 * @param args
	 * @return
	 * @throws Exception
	 */
	public int mxMain(Context context, String[] args) throws Exception {
		logger.info("Usage: exec prog NextLabsEMDeployment -method <Method>");
		logger.info("Method:");
		logger.info("   - validateDeploymentFile -file <filename>");
		logger.info("   - validateRuntimeFile");
		logger.info("   - validateSystemFile");
		logger.info("   - listSecurityClassificationAttributes");
		logger.info("   - createSecurityClassificationAttributes -file <filename>");
		logger.info("   - deleteSecurityClassificationAttributes");
		logger.info("   - deleteSecurityClassificationAttribute -attribute <attributename>");
		logger.info("   - listNXLPolicies");
		logger.info("   - modifyEnoviaPolicies -file <filename>");
		logger.info("   - restoreEnoviaPolicies");
		logger.info("   - restoreEnoviaPolicy -policy <policyname>");
		logger.info("   - listAllNXLTriggers");
		logger.info("   - listNXLTriggers");
		logger.info("   - modifyEnoviaTriggers -file <filename>");
		logger.info("   - restoreEnoviaTriggers");
		logger.info("   - restoreEnoviaTriggers -type <typename>");
		
		return 0;
	}
	
}
