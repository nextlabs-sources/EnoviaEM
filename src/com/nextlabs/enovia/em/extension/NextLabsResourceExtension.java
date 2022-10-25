package com.nextlabs.enovia.em.extension;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

public class NextLabsResourceExtension extends NextLabsExtension {

	private String boType;
	private int inheritanceLevel;
	
	public NextLabsResourceExtension(String implClass, String boType) {
		super(implClass);
		
		this.boType = boType;
		this.inheritanceLevel = 0;
	}
	
	public NextLabsResourceExtension(String implClass, String boType, int inheritanceLevel) {
		super(implClass);
		
		this.boType = boType;
		this.inheritanceLevel = inheritanceLevel;
	}
	
	public String getBoType() {
		return boType;
	}
	
	public int getInheritanceLeve() {
		return inheritanceLevel;
	}
	
}
