/*
 * Created on April 30, 2012
 *
 * All sources, binaries and HTML pages (C) copyright 2012 by NextLabs Inc.,
 * San Mateo CA, Ownership remains with NextLabs Inc, All rights reserved
 * worldwide.
 */

import java.util.Map;

import matrix.db.Context;
import matrix.util.StringList;

import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.engineering.CADDrawing;
import com.matrixone.apps.engineering.Part;


/**
 *
*/
public class NextLabsTestData_mxJPO {
	// configure here:

	public static final String partNamePrefix = "nxlTestPart_";
	public static final String docNamePrefix = "nxlTestDoc_";
	public static final String owner = "Test Everything";
	public static final String vault = "eService Production";
	// each iteration creates part and document. Eg. createNumber 10
	// will create 10 part with 10 related objects
	public static final Integer createNumber = 500;
	public static final String nxlClassificationAttrName = "nxl_Access_Classification";
	
	public NextLabsTestData_mxJPO(Context _context, String[] args) {

	}

	public void createAll(Context context, String[] args) throws Exception {
		println("START method createAll");
		println("Creating " + createNumber*2 + " objects...");

		MqlUtil.mqlCommand(context, "trigger off;");

		try {
			context.start(true);
			context.setVault(vault);
			Part testPart = new Part();

			String partType = "Part";
			String partRevision = "0";
			String partPolicy = "Development Part";
			String partDesc = "Test Part for nextLabs Control Center Integration";
			String partECOName = "";
			boolean useAutoNamer = false;

			CADDrawing testDoc = new CADDrawing();

			String docType = "CAD Drawing";
			String docRevision = "A";
			String docPolicy = "CAD Drawing";
			String docDesc = "Test Document for nextLabs Control Center Integration";
			String docECOName = "";
			Map<?, ?> docAttributesMap = null;

			String customrevision = null;

			for (int i = 0; i < createNumber; i++) {
				String partName = partNamePrefix + (new Integer(i)).toString();
				testPart.create(context, partType, partName, partRevision,
						partPolicy, vault, partDesc, docAttributesMap, partECOName, partName, partName, partName, partName, useAutoNamer);
				//testPart.create(context, partType, partName, partRevision,
				//		partPolicy, vault, partDesc, partECOName, useAutoNamer);
				testPart.setOwner(context, owner);

				// set classification of some objects to ITAR
				if (i % 10 == 0) {
					testPart.setAttributeValue(context, nxlClassificationAttrName, "ITAR");
				}

				String docName = docNamePrefix + (new Integer(i)).toString();
				String docPartId = testPart.getObjectId();
				
				testDoc.create(context, docType, docName, docRevision, null,
						useAutoNamer, docPolicy, vault, docDesc,
						docAttributesMap, docPartId, docECOName, customrevision);
				testDoc.setOwner(context, owner);

				// set classification of some objects to ITAR
				if (i % 10 == 0) {
					testDoc.setAttributeValue(context, nxlClassificationAttrName, "ITAR");
				}
			}

			context.commit();
		} catch (Exception e) {
			e.printStackTrace();
			context.abort();
		}

		MqlUtil.mqlCommand(context, "trigger on;");
		println("END method createAll");
	}

	public void deleteAll(Context context, String[] args) throws Exception {
		println("START method deleteAll");
		MqlUtil.mqlCommand(context, "trigger off;");

		try {
			String strType = "*";
			String strName = "*TypeCADDrawingCheckO*";
			String strRevision = "*";
			String strOwner = "*";
			String sbWhereExp = "";

			StringList slSelect = new StringList(1);
			slSelect.addElement("id");

			short sQueryLimit = -1;

			MapList mapList = DomainObject.findObjects(context, strType,
					strName, strRevision, strOwner, vault,
					sbWhereExp.toString(), "", true, (StringList) slSelect,
					sQueryLimit);

			int number = mapList.size();

			println("Deleting " + number + " objects...");

			String[] ids = new String[number];

		    for (int i=0; i < mapList.size(); i++) {
		         Map<?, ?> objectMap = (Map<?, ?>)mapList.get(i);
		         String id  = (String)objectMap.get("id");                     
		         ids[i] = id;
		    }

			//DomainObject.deleteObjects(context, ids);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		MqlUtil.mqlCommand(context, "trigger on;");
		println("END method deleteAll");
	}
	
	/*public void testExtGetRelatedParts(Context context, String[] args) throws Exception {
		DomainObject dObject = new DomainObject(args[0]);
		
		NextLabsExtensionHelper extensionHelper = NextLabsExtensionHelper.getInstance();
		MapList result = extensionHelper.getRelatedParts(context, dObject);
		
		printExtResult(result);
	}
	
	public void testExtGetRelatedECO(Context context, String[] args) throws Exception {
		DomainObject dObject = new DomainObject(args[0]);
		
		NextLabsExtensionHelper extensionHelper = NextLabsExtensionHelper.getInstance();
		MapList result = extensionHelper.getRelatedECO(context, dObject);
		
		printExtResult(result);
	}
	
	@SuppressWarnings("unchecked")
	private void printExtResult(MapList resultList) throws Exception {
		NextLabsLogger logger = new NextLabsLogger(Logger.getLogger("EMLOGGER"));
		
		for (int i = 0; i < resultList.size(); i++) {
			
			Map<String, Object> map = (Map<String, Object>) resultList.get(i);
			
			for (Map.Entry<String, Object> entry : map.entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue().toString();
				
				logger.info(i + " : " + key + ":" + value);
			}			
		} // end of loop body for resultList
	}*/
	
	public int mxMain(Context context, String[] args) throws Exception {
		//createAll(context);
		deleteAll(context, args);

		return 0;
	}
	
	// print to MQL console
	private void println(String str) {
		System.out.println(str);
	}
	
}
