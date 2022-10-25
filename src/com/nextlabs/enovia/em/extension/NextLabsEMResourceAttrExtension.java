/**
 * NextLabs Enovia EM Resource Attribute Extension Interface
 */
package com.nextlabs.enovia.em.extension;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.DomainObject;
import matrix.db.Context;

public interface NextLabsEMResourceAttrExtension {
	
	MapList getData(Context context, DomainObject object);
	
}
