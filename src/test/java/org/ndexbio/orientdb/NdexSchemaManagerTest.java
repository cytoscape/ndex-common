package org.ndexbio.orientdb;

import static org.junit.Assert.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.ndexbio.common.access.NdexAOrientDBConnectionPool;
import org.ndexbio.common.exceptions.NdexException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OSchema;

public class NdexSchemaManagerTest {

	 private static ODatabaseDocumentTx db;
	 private static String DB_URL = "memory:ndex";
	 
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		
//		db = new ODatabaseDocumentTx(DB_URL);
//		db.create();
		db = NdexAOrientDBConnectionPool.getInstance().acquire();
		long s = db.getDictionary().size();
		System.out.println(s);
		for ( Object o : db.getDictionary().keys())
			System.out.println(o.toString());
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		db.close();
	}

	@Test
	public void test() throws NdexException {
		NdexSchemaManager.INSTANCE.init(db);
		
		OSchema schema = db.getMetadata().getSchema();
		
		for ( OClass c :schema.getClasses()) 
		{
			System.out.println(c.getName());
		}
		
		assertEquals (schema.countClasses(), 19+10);  // 10 internal classes.
		
	}

}