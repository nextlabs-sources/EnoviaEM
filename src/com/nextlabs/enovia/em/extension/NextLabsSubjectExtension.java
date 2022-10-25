package com.nextlabs.enovia.em.extension;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

public class NextLabsSubjectExtension extends NextLabsExtension {
	
	private String subjectType;
	
	public NextLabsSubjectExtension(String implClass, String subjectType) {
		super(implClass);
		
		this.subjectType = subjectType;
	}
	
	public String getSubjectType() {
		return subjectType;
	}

}
