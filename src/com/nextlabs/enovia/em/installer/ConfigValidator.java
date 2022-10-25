package com.nextlabs.enovia.em.installer;

/*
 * Created on December 6, 2013
 *
 * All sources, binaries and HTML pages (C) copyright 2013 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.io.File;
import java.io.IOException;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;

public class ConfigValidator {
	
	public ConfigValidator() {
		
	}
	
	public ConfigValidationResult validate(String sXMLSchema, String sXMLFile) {
		ConfigValidationResult result;
		
		try { 
			Source xmlFile = new StreamSource(new File(sXMLFile));
			SchemaFactory schemaFactory = SchemaFactory
					.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = schemaFactory.newSchema(new File(sXMLSchema));
			Validator validator = schema.newValidator();
			
			try {
				validator.validate(xmlFile);
				
				result = new ConfigValidationResult(true, "");
			} catch (SAXException e) {
				result = new ConfigValidationResult(false, e.getLocalizedMessage());
			} catch (IOException ioe) {
				result = new ConfigValidationResult(false, ioe.getMessage());
			}
		} catch (SAXException sae) {
			result = new ConfigValidationResult(false, sae.getMessage());
		}
		
		return result;
	}

}
