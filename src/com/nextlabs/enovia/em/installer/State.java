package com.nextlabs.enovia.em.installer;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.ArrayList;

public class State {
	
	private String name;			// name of the state, i.e. Preliminary, Approved, ...
	private ArrayList<User> users;
	
	/**
	 * Constructor of State
	 * @param name
	 */
	public State(String name) {
		this.name = name;
		this.users = new ArrayList<User>();
	}
	
	/**
	 * Function to retrieve the value of name
	 * @return name in the expression of String
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Function to add user to users list 
	 * @param user
	 */
	public void addUser(User user) {
		users.add(user);
	}
	
	/**
	 * Function to retrieve the users list
	 * @return users in the expression of ArrayList<User>
	 */
	public ArrayList<User> getUsers() {
		return users;
	}
	
	/**
	 * Override the toString method
	 */
	public String toString() {
		StringBuffer strBuf = new StringBuffer(name + ":\n");
		
		if (users.size() > 0) {
			strBuf.append(users.get(0).getName());
			
			for (int i = 1; i < users.size(); i++) {
				strBuf.append(", " + users.get(i).getName());
			}
		}
		
		/*for (User user : users) {
			strBuf.append("   " + user.getName() + " = " + user.getFilterProgram() + "\n");
		}*/

		strBuf.append("\n");
		
		return strBuf.toString();
	}

}
