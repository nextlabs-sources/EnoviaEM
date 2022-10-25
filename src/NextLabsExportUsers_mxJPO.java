/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Map;

import matrix.db.Context;
import matrix.db.MatrixWriter;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;


/**
 *
 **/
public class NextLabsExportUsers_mxJPO {

	// header;select_expression
	public static final String DELIMITER = ";";
	public static final String EXPORT_FILENAME = "C:/temp/personexport.csv";

	// Configuration of Person Export
	// Each line of exportItems represents on column to export
	// format: column header; column select expression for Person business object
	public static final String[][] EXPORT_ITEMS = { 
			{ "AccountName", "name" },
			{ "FirstName", "attribute[First Name]" },
			{ "LastName", "attribute[Last Name]" },
			{ "Company", "to[Employee].from.name" },
			{ "Country", "attribute[Country]" },
			{ "Department", "to[Employee].from.name" },
			{ "EmailAddress", "attribute[Email Address]" },
			{ "WebSite", "attribute[Web Site]" },
			{ "FaxNumber", "attribute[Fax Number]" },
			{ "PagerNumber", "attribute[Pager Number]" },
			{ "HomePhoneNumber", "attribute[Home Phone Number]" },
			{ "WorkPhoneNumber", "attribute[Work Phone Number]" },
			{ "MiddleName", "attribute[Middle Name]" },
			{ "Title", "attribute[Title]" },
			{ "MailCode", "attribute[Mail Code]" },
	};

	//public static MQLCommand mqlCommand = null;
	BufferedWriter writer = null;
	FileWriter fwriter = null;
	//FileWriter warningLog = null;
	//FileWriter convertedOidsLog = null;
	//FileWriter statusLog = null;

	/**
     *
     */
	public NextLabsExportUsers_mxJPO(Context context, 
			String[] args) throws Exception {
		writer = new BufferedWriter(new MatrixWriter(context));
		fwriter = new FileWriter(EXPORT_FILENAME, false);

		//mqlCommand = MqlUtil.getMQL(context);
		//super(context, args);
	}

	// intermediate test program
	public int exportPersonData(Context context, String[] args) {	
		println("START exportPersonData");

		// String result = "";
		try {
			// PersonList list = Person.getPersons(context);
			//
			// PersonItr itr = new PersonItr(list);
			// Person person = null;

			String typePattern = "Person";
			// String revisionPattern = "*";
			String vaultPattern = "*";
			String whereExpression = "";
			StringList objectSelects = new StringList();

			StringBuffer headerLine = new StringBuffer();
			
			for (int i = 0; i < EXPORT_ITEMS.length; i++) {
				headerLine.append(EXPORT_ITEMS[i][0]);
				headerLine.append(DELIMITER);

				objectSelects.addElement(EXPORT_ITEMS[i][1]);
			}

			fwrite(headerLine.toString());

			MapList mapList = DomainObject.findObjects(context, typePattern,
					vaultPattern, whereExpression, objectSelects);

			@SuppressWarnings("unchecked")
			Iterator<Map<String,String>> mapListIter = mapList.listIterator();

			while (mapListIter.hasNext()) {
				StringBuffer dataLine = new StringBuffer();
				Map<String,String> map = mapListIter.next();
				println("map: " + map);

				for (int i = 0; i < EXPORT_ITEMS.length; i++) {
					dataLine.append(map.get(EXPORT_ITEMS[i][1]));
					dataLine.append(DELIMITER);					
				}

				fwrite(dataLine.toString());
			}

			fwriter.close();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		println("END exportPersonData");
		return 0;
		//
	}

	public int mxMain(Context context, String[] args) {
		exportPersonData(context, args);
		
		return 0;
	}

	private void fwrite(String str) {
		// System.out.println(str);
		try {
			fwriter.write(str + "\n");
			fwriter.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	// print to MQL console
	private void println(String str) {
		// System.out.println(str);
		try {
			writer.write(str + "\n");
			writer.flush();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
