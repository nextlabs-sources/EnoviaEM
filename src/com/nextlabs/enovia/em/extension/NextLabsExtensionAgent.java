package com.nextlabs.enovia.em.extension;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import matrix.db.BusinessType;
import matrix.db.BusinessTypeList;
import matrix.db.Context;
import matrix.util.MatrixException;

import org.apache.log4j.Logger;

import com.nextlabs.enovia.em.NextLabsLogger;
import com.nextlabs.enovia.em.NextLabsRuntimeConfig;

public class NextLabsExtensionAgent {
	
	// Log4j logging initialization
	private static NextLabsLogger logger = null;

	private static final ConcurrentHashMap<String, NextLabsResourceExtension> resourceExtensionChildList;
	
	private static NextLabsExtensionAgent nExtensionAgent = null;
	private static NextLabsRuntimeConfig config = null;
	
	private static boolean reloadingRequired = true;
	
	static {		
		logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));

		config = NextLabsRuntimeConfig.getInstance();
				
		resourceExtensionChildList = new ConcurrentHashMap<String, NextLabsResourceExtension>();
	}
	
	private NextLabsExtensionAgent() {

	}
	
	/**
	 * Singleton control which return the instance
	 * @return nExtensionAgent
	 */
	public static synchronized NextLabsExtensionAgent getInstance() {
		if (nExtensionAgent == null) {
			nExtensionAgent = new NextLabsExtensionAgent();
		}
		
		return nExtensionAgent;
	}
	
	public NextLabsResourceExtension getResourceExtension(String sType, Context context) {
		NextLabsResourceExtension extension = config.getResourceExtension(sType);
		
		if (extension == null) {
			if (reloadingRequired)
				setResourceExtensionChildList(context);
			
			// search child list
			extension = resourceExtensionChildList.get(sType);
		}
		
		return extension;
	}
	
	public NextLabsSubjectExtension getSubjectExtension(String sType) {
		NextLabsSubjectExtension extension = config.getSubjectExtension(sType);
		
		return extension;
	}
	
	public void resetResourceExtensionChildList() {
		reloadingRequired = true;
	}
	
	public void setResourceExtensionChildList(Context context) {
		try {		
			ArrayList<NextLabsResourceExtension> extensions = config.getResourceExtensions();
			
			// clear all data in existing map
			resourceExtensionChildList.clear();
			
			for (NextLabsResourceExtension extension : extensions) {
				BusinessType bizType = new BusinessType(extension.getBoType(), context.getVault());
				int inheritanceLevel = 1;
				
				try {
					setupChildList(bizType, context, extension, inheritanceLevel);
				} catch (MatrixException me) {
					logger.error("%s while getting children for %s", me.getMessage(), bizType.getName());
				}
			}
			
			reloadingRequired = false;
			
			if (logger.isNXLDebugEnable)
				printChildren();
		} catch (Exception ex) {
			logger.error("setResourceExtensionChildList() caught exception: %s", ex, ex.getMessage());
		}
	}
	
	private void setupChildList(BusinessType bizType, Context context, 
			NextLabsResourceExtension root_extension, int inheritanceLevel) throws MatrixException {
		BusinessTypeList bizTypeList = bizType.getChildren(context);
		
		if (bizTypeList != null) {
			NextLabsResourceExtension child_extension = new NextLabsResourceExtension(
					root_extension.getImplClass(), root_extension.getBoType(), inheritanceLevel);
			
			for (Object obj : bizTypeList) {
				BusinessType bizTypeItem = (BusinessType) obj;
				
				if (resourceExtensionChildList.containsKey(bizTypeItem.getName())) {
					NextLabsResourceExtension extension = resourceExtensionChildList.get(bizTypeItem.getName());
					
					if (extension.getInheritanceLeve() > inheritanceLevel) {
						resourceExtensionChildList.put(bizTypeItem.getName(), child_extension);
						
						setupChildList(bizTypeItem, context, root_extension, inheritanceLevel + 1);
					}
				} else {
					resourceExtensionChildList.put(bizTypeItem.getName(), child_extension);
					
					setupChildList(bizTypeItem, context, root_extension, inheritanceLevel + 1);
				}
			}
		}
		
	}
	
	private void printChildren() {
		for(Map.Entry<String, NextLabsResourceExtension> entry : resourceExtensionChildList.entrySet()) {		
			logger.debug(entry.getKey() + ":" + entry.getValue().getImplClass() + ":" + entry.getValue().getInheritanceLeve());
		}
	}
		
}
