package com.nextlabs.enovia.em;

/*
 * Created on September 20, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.io.Serializable;
import java.util.ArrayList;

import matrix.db.Context;

import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;
import com.nextlabs.enovia.common.NextLabsConstant;

public final class NextLabsNotificationCenter implements Serializable {
	
	// Unique Serialization ID
	private static final long serialVersionUID = 2165900077773883464L;
	
	// NextLabsNotificationCenter instance for singleton control
	private static NextLabsNotificationCenter nNotificationCenter = null;
	
	/**
	 * Constructor for the class.
	 */
	private NextLabsNotificationCenter() {
		
	}
	
	/**
	 * Singleton control for the data access.
	 * @return void
	 */
	public static synchronized NextLabsNotificationCenter getInstance() {
		if (nNotificationCenter == null) {
			nNotificationCenter = new NextLabsNotificationCenter();
		}
		
		return nNotificationCenter;
	}
	
	public void notify(Context context, String response, String message, 
			String notificationMethod) throws FrameworkException {
		if (!(NextLabsConstant.RESPONSE_ALLOW_VALUE).equalsIgnoreCase(response)) {
			ArrayList<String> messages = new ArrayList<String>();
			messages.add(message);
		
			notify(context, messages, notificationMethod);
		}
	}
	
	public void notify(Context context, String message, 
			String notificationMethod) throws FrameworkException {
		ArrayList<String> messages = new ArrayList<String>();
		messages.add(message);
		
		notify(context, messages, notificationMethod);
	}
	
	public void notify(Context context, ArrayList<String> messages, 
			String notificationMethod) throws FrameworkException {
		StringBuffer strBufMsg = new StringBuffer("");
		String delimeter = "\n";
		
		boolean isFrameworkException = notificationMethod.equalsIgnoreCase("frameworkexception");
		
		if (isFrameworkException)
			delimeter = " ";
		
		for (String message : messages){
			if (message != null && message.trim().length() > 0) {
				strBufMsg.append(message);
				strBufMsg.append(delimeter);
			}
		}
		
		if (strBufMsg.length() > 0) {
			if (isFrameworkException)
				throw new FrameworkException(strBufMsg.toString());
			else
				MqlUtil.mqlCommand(context, "notice \"" + strBufMsg.toString() + "\"",false, false);
		}
	}
	
	/**
	 * Over loading method for handling framework exception being throw and causing action is block, eventhough the response is allow
	 * @param context
	 * @param messages
	 * @param notificationMethod
	 * @param isAllow
	 * @throws FrameworkException
	 */
	public void notify(Context context, ArrayList<String> messages, 
			String notificationMethod, boolean isAllow) throws FrameworkException {
		StringBuffer strBufMsg = new StringBuffer("");
		String delimeter = "\n";
		
		boolean isFrameworkException = notificationMethod.equalsIgnoreCase("frameworkexception");
		
		if (isFrameworkException)
			delimeter = " ";
		
		for (String message : messages){
			if (message != null && message.trim().length() > 0) {
				strBufMsg.append(message);
				strBufMsg.append(delimeter);
			}
		}
		
		if (strBufMsg.length() > 0) {
			if (isFrameworkException && (!isAllow))
				throw new FrameworkException(strBufMsg.toString());
			else
				MqlUtil.mqlCommand(context, "notice \"" + strBufMsg.toString() + "\"", false, false);
		}
	}

}
