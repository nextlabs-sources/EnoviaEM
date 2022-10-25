package com.nextlabs.enovia.em.installer;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

public class ConfigValidationResult {
	
	private boolean valid;
	private String message;
	
	public ConfigValidationResult(boolean valid, String message) {
		this.valid = valid;
		this.message = message;
	}
	
	public boolean isValid() {
		return valid;
	}
	
	public String getMessage() {
		return message;
	}

}
