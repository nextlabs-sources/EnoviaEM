package com.nextlabs.enovia.em.extension;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

public abstract class NextLabsExtension {
	
	private String implClass;
	
	public NextLabsExtension(String implClass) {
		this.implClass = implClass;
	}
	
	public String getImplClass() {
		return implClass;
	}

}
