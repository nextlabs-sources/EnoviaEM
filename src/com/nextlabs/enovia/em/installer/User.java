package com.nextlabs.enovia.em.installer;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

public class User {
	
	private String name; 			// the name of user can be group name, role name, person name
	private String accessItem;		// i.e. read,modify,delete,...
	private String filterProgram;	// i.e. program[NextLabsAccessCheck -method ...
	
	/**
	 * Constructor of User
	 * @param name
	 * @param filterProgram
	 */
	public User(String name, String filterProgram) {
		this.name = name;
		this.filterProgram = filterProgram;
	}
	
	/**
	 * Function to retrieve the value of name
	 * @return name in the expression of String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Function to assign value to the name
	 * @param name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Function to retrieve the value of accessItem
	 * @return accessItem in the expression of String 
	 */
	public String getAccessItem() {
		return accessItem;
	}
	
	/**
	 * Function to assign value to the accessItem
	 * @param accessItem
	 */
	public void setAccessItem(String accessItem) {
		this.accessItem = accessItem;
	}
	
	/**
	 * Function to retrieve the value of filterProgram
	 * @return filterProgram in the expression of String 
	 */
	public String getFilterProgram() {
		return filterProgram;
	}
	
	/**
	 * Function to assign value to the filterProgram
	 * @param filterProgram
	 */
	public void setFilterProgram(String filterProgram) {
		this.filterProgram = filterProgram;
	}

}
