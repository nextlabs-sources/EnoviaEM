package com.nextlabs.enovia.em;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import org.apache.commons.configuration.event.ConfigurationEvent;
import org.apache.commons.configuration.event.ConfigurationListener;
import org.apache.log4j.Logger;

import com.nextlabs.enovia.em.extension.NextLabsExtensionAgent;

public class NextLabsConfigListener implements ConfigurationListener {
	
	// Log4j logging initialization
	private static NextLabsLogger logger = null;
	
	// Extension Agent for retrieve extension details
	private static NextLabsExtensionAgent extensionAgent = null;
	
	static {
		logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));
		
		extensionAgent = NextLabsExtensionAgent.getInstance();
	}
	
	public void configurationChanged(ConfigurationEvent event) {
		if (!event.isBeforeUpdate()) {
			logger.debug("Runtimeconfig is modified, reset resource extension child list.");
			
			extensionAgent.resetResourceExtensionChildList();
		}
	}

}
