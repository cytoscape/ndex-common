/**
 * Copyright (c) 2013, 2016, The Regents of the University of California, The Cytoscape Consortium
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.ndexbio.task.parsingengines;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.ndexbio.common.access.NdexDatabase;
import org.ndexbio.common.models.dao.orientdb.Helper;
import org.ndexbio.common.models.dao.orientdb.UserDocDAO;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.network.NetworkSourceFormat;
import org.ndexbio.task.Configuration;

@RunWith(Suite.class)
@SuiteClasses({
	ImportExportTest.class
	//BioPAXParserTest.class, SifParserTest.class,
	//	xbelParserTest.class, XgmmlParserTest.class 
		})
public class AllTests {

	
	static Configuration configuration ;
	static String propertyFilePath = 
			//"C/ndex/conf/ndex.properties";
			"/opt/ndex/conf/ndex.properties";
	static String testFileDirectory = 
			//"C:/Users/chenjing/Dropbox/Network_test_files/";
			"/Users/chenjing/Dropbox/Network_test_files/";
	public static Configuration confituration;
	public static NdexDatabase db ;
	public static String testUser = "cj2";
	public static List<TestMeasurement> testList;

	  @BeforeClass 
	    public static void setUpClass() throws NdexException, IOException {      
			setEnv();

	    	// read configuration
	    	configuration = Configuration.getInstance();
	    	
	    	//and initialize the db connections
			db = NdexDatabase.createNdexDatabase(configuration.getHostURI(),
					configuration.getDBURL(),
	    			configuration.getDBUser(),
	    			configuration.getDBPasswd(), 10);
			
			
			try (UserDocDAO dao = new UserDocDAO(db.getAConnection())) {
		    	
				Helper.createUserIfnotExist(dao, testUser,
					"blahsodl232@something.net", 
					"cj2");
			}	
			
			
	    	testList = new ArrayList<> (100);

	    	try (Reader in = new FileReader(testFileDirectory + "network_test_file_list_1.3.csv")) {
	    	  CSVParser parser = CSVFormat.EXCEL.parse(in);
	    	  for (CSVRecord record : parser) {
	    		if ( parser.getCurrentLineNumber() > 1 && Boolean.valueOf(record.get(16).toLowerCase()) ) {
	    			TestMeasurement t = new TestMeasurement();
	    			t.fileName = record.get(0);
	    			t.srcFormat = NetworkSourceFormat.valueOf(record.get(1));
	    			t.nameSpaceCnt = getIntValueFromRec(record,2);
	    			t.basetermCnt  = getIntValueFromRec(record,3);
	    			t.nodeCnt  	   = getIntValueFromRec(record,4);
	    			t.funcTermCnt  = getIntValueFromRec(record,5);
	    			t.reifiedEdgeCnt = getIntValueFromRec(record,6);
	    			t.edgeCnt      = getIntValueFromRec(record,7);
	    			t.citationCnt  = getIntValueFromRec(record,8);
	    			t.support      = getIntValueFromRec(record,9);
	    			t.netPropCnt   = getIntValueFromRec(record,10);
	    			t.netPresPropCnt = getIntValueFromRec(record,11);
	    			t.nodePropCnt   = getIntValueFromRec(record,12);
	    			t.edgePropCnt = getIntValueFromRec(record,13);
	    			t.networkName = record.get(14);
	    			String testCX = record.get(15); 
	    			if ( testCX !=null && testCX.toLowerCase().equals("false"))
	    				t.testCX = false;
	    			else 
	    				t.testCX = true;
	    			//t.runTest  = ;
	    			
	    			testList.add(t);
	    		}
	    	  }
	    	  parser.close();
	    	}
	    	
	    	
	    	

	    }

	  
	  @AfterClass 
	  public static void tearDownClass() { 
			db.close();
      }

	  
	  private static int getIntValueFromRec(CSVRecord r, int idx) {
		  try { 
			  return Integer.parseInt(r.get(idx));
		  } catch ( NumberFormatException e) {
			  return -1;
		  }
	  }
	  
	  
		private static void setEnv()
		{
		  try
		    {
		        Class<?> processEnvironmentClass = Class.forName("java.lang.ProcessEnvironment");
		        Field theEnvironmentField = processEnvironmentClass.getDeclaredField("theEnvironment");
		        theEnvironmentField.setAccessible(true);
		        Map<String, String> env = (Map<String, String>) theEnvironmentField.get(null);
		        env.put("ndexConfigurationPath", propertyFilePath);
		        //env.putAll(newenv);
		        Field theCaseInsensitiveEnvironmentField = processEnvironmentClass.getDeclaredField("theCaseInsensitiveEnvironment");
		        theCaseInsensitiveEnvironmentField.setAccessible(true);
		        Map<String, String> cienv = (Map<String, String>)     theCaseInsensitiveEnvironmentField.get(null);
		        //cienv.putAll(newenv);
		        env.put("ndexConfigurationPath", propertyFilePath);
		    }
		    catch (NoSuchFieldException e)
		    {
		      try {
		        Class[] classes = Collections.class.getDeclaredClasses();
		        Map<String, String> env = System.getenv();
		        for(Class cl : classes) {
		            if("java.util.Collections$UnmodifiableMap".equals(cl.getName())) {
		                Field field = cl.getDeclaredField("m");
		                field.setAccessible(true);
		                Object obj = field.get(env);
		                Map<String, String> map = (Map<String, String>) obj;
		                //map.clear();
		                //map.putAll(newenv);
		                map.put("ndexConfigurationPath", propertyFilePath);
		            }
		        }
		      } catch (Exception e2) {
		        e2.printStackTrace();
		      }
		    } catch (Exception e1) {
		        e1.printStackTrace();
		    } 
		}

}
