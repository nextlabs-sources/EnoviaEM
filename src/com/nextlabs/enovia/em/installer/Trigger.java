package com.nextlabs.enovia.em.installer;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.ArrayList;

import com.nextlabs.enovia.common.NextLabsConstant;

public class Trigger {
	
	private static final String TRIGGER_TYPE_CHECK = "Check";
	private static final String TRIGGER_TYPE_OVERRIDE = "Override";
	private static final String TRIGGER_TYPE_ACTION = "Action";

	private String event;
	private String eventType;				// any of the valid events, i.e. CheckOut, CheckIn
	private String triggerType;				// trigger type is Check, Override, Action
	private String program;					// name of the Program object that will execute when the event occurs, i.e. emxTriggerManager
	private ArrayList<String> arguments;	// arguments to be passed into the program, i.e. TypePartConnectNextLabs
		
	/**
	 * Constructor of Trigger
	 * @param event
	 * @param program
	 */
	public Trigger(String event, String program) {
		this.event = event;
		this.program = program;
		this.eventType = "";
		this.triggerType = "";
		
		this.arguments = new ArrayList<String>();
		
		if (this.event != null)
			setupTypes();
	}
	
	/**
	 * Function to assign value to triggerType and eventType
	 */
	private void setupTypes() {
		int index = 0;
		
		// trigger type is postfix of event, i.e. CreateAction, CreateCheck, and etc
		if ((index = event.lastIndexOf(TRIGGER_TYPE_CHECK)) > 0) {
			triggerType = TRIGGER_TYPE_CHECK;
		} else if ((index = event.lastIndexOf(TRIGGER_TYPE_OVERRIDE)) > 0) {
			triggerType = TRIGGER_TYPE_OVERRIDE;
		} else if ((index = event.lastIndexOf(TRIGGER_TYPE_ACTION)) > 0) {
			triggerType = TRIGGER_TYPE_ACTION;
		}
		
		if (index > 0) {
			eventType = event.substring(0, index);
		}
	}
	
	/**
	 * Function to retrieve the event's value
	 * @return event in the expression of String
	 */
	public String getEvent() {
		return event;
	}
	
	/**
	 * Function to retrieve the eventType's value
	 * @return eventType in the expression of String
	 */
	public String getEventType() {
		return eventType;
	}
	
	/**
	 * Function to retrieve the triggerType's value
	 * @return triggerType in the expression of String
	 */
	public String getTriggerType() {
		return triggerType;
	}
	
	/**
	 * Function to retrieve the program's value
	 * @return program in the expression of String
	 */
	public String getProgram() {
		return program;
	}
	
	/**
	 * Function to add argument to arguments list
	 * @param argument
	 */
	public void addArgument(String argument) {
		this.arguments.add(argument);
	}
	
	/**
	 * Function to retrieve arguments list 
	 * @return arguments in the expression of ArrayList<String>
	 */
	public ArrayList<String> getArguments() {
		return this.arguments;
	}
	
	/**
	 * Override the toString method
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer(event + ":" + program + "(");
		
		for (int i = 0; i < arguments.size(); i++) {
			if (arguments.get(i).contains(NextLabsConstant.NXL_TRIGGER_KEYWORD))
				strBuf.append(arguments.get(i) + " ");
		}
		
		strBuf.append(")");
		
		return strBuf.toString();
	}
	
}
