/**
 * NextLabs Enovia EM Subject Attribute Extension Interface
 */
package com.nextlabs.enovia.em.extension;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.Map;

import matrix.db.Context;

public interface NextLabsEMSubjectAttrExtension {
	
	Map<String, Object> getData(Context context);

}
