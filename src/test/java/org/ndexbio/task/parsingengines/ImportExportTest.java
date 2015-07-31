/**
 * Copyright (c) 2013, 2015, The Regents of the University of California, The Cytoscape Consortium
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.TreeSet;
import java.util.UUID;
import java.util.logging.Logger;

import org.junit.Test;
import org.ndexbio.common.NetworkSourceFormat;
import org.ndexbio.common.exporter.BioPAXNetworkExporter;
import org.ndexbio.common.exporter.XGMMLNetworkExporter;
import org.ndexbio.common.exporter.XbelNetworkExporter;
import org.ndexbio.common.models.dao.orientdb.NetworkDAO;
import org.ndexbio.common.models.dao.orientdb.NetworkDocDAO;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.network.BaseTerm;
import org.ndexbio.model.object.network.Edge;
import org.ndexbio.model.object.network.Network;
import org.ndexbio.model.object.network.Node;
import org.ndexbio.task.event.NdexNetworkState;
import org.xml.sax.SAXException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.commons.io.FilenameUtils;



public class ImportExportTest {

//	private static final boolean BaseTerm = false;
	static Logger logger = Logger.getLogger(ImportExportTest.class.getName());

	@Test
	public void test() throws Exception {

		for ( TestMeasurement m : AllTests.testList) {
		  
			
		  logger.info("Testting " +m.fileName+ "\nFirst round import start.");
		  IParsingEngine parser = importFile(AllTests.testFileDirectory + m.fileName, m);
			  	
		 // get the UUID of the new test network
		 UUID networkID = parser.getUUIDOfUploadedNetwork();
			
		 logger.info("Verifying loaded content.");
		 assertEquivalence(networkID, m);
		
		 logger.info("First round import passed. Start exporting ...");

		 System.out.println("Working Directory = " + System.getProperty("user.dir"));
		 ODatabaseDocumentTx conn = AllTests.db.getAConnection();
		 exportNetwork(m, conn, networkID);

		 logger.info("First export done.");
		 if ( m.srcFormat == NetworkSourceFormat.SIF) { 
			 System.out.println("Ignore the rest of test for " + m.fileName);
			 continue;
		 }

		 String oldNetworkID = networkID.toString();
		  
		  logger.info("Started importing exported network.");
		  parser = importFile ( System.getProperty("user.dir") + "/"+ networkID.toString(), m);
		  
  		  

  		logger.info("Verifying network loaded from exported file.");
  		  networkID = parser.getUUIDOfUploadedNetwork();
 		  assertEquivalence(networkID, m);
          
 		  
 		 logger.info("Exporting the re-imported network.");
 		  exportNetwork(m, conn, networkID);
 

 		  logger.info("checking if the 2 exported files have similar sizes");
 		  File file1 = new File(oldNetworkID.toString());
		  File file2 = new File(networkID.toString());
 		  assertTrue( file2.exists());
 		  double sizeDiff = Math.abs(file1.length()-file2.length());
 		  assertTrue ( sizeDiff/file1.length() < 0.005 || sizeDiff <100);
 		  //assertEquals(file1.length(), file2.length()); 

  		 logger.info("Deleting first round test network " + oldNetworkID + " from db.");
 		  NetworkDAO dao = new NetworkDAO (conn);
 		  dao.deleteNetwork(oldNetworkID);

 		  conn.commit();
 		  
  		 logger.info("All tests on " + m.fileName + " passed. Deleteing test network " + networkID.toString()); 
  		  dao.deleteNetwork(networkID.toString());
  		  conn.commit();
  		  conn.close();
		  
		  logger.info("Deleting network document exported in first round.");
		  file1.delete();
 		  
 		 logger.info("Deleteing network document exported in second round " + networkID.toString());
 		  file2.delete();
 		  
 		 logger.info("All done for "+ m.fileName);
		}
		
		logger.info("All tests passed.");

	}

	private static void exportNetwork(TestMeasurement m, ODatabaseDocumentTx conn,
			UUID networkID) throws ParserConfigurationException, ClassCastException, NdexException, TransformerException, SAXException, IOException {
		  if ( m.srcFormat == NetworkSourceFormat.XGMML) {
			  XGMMLNetworkExporter exporter = new XGMMLNetworkExporter(conn);
			  FileOutputStream out = new FileOutputStream (networkID.toString());
			  exporter.exportNetwork(networkID,out);
			  out.close();
              
		  }	else if ( m.srcFormat == NetworkSourceFormat.BIOPAX) {
			  BioPAXNetworkExporter exporter = new BioPAXNetworkExporter(conn);
			  try (FileOutputStream out = new FileOutputStream (networkID.toString())) {
				  exporter.exportNetwork(networkID,out);
			  }	  
		  }
		  else if ( m.srcFormat == NetworkSourceFormat.BEL) {
				NetworkDocDAO  dao = new NetworkDocDAO(conn);

				// initiate the network state
				XbelNetworkExporter exporter = 
						new XbelNetworkExporter(AllTests.testUser, networkID.toString(),dao,networkID.toString());
			//
				exporter.exportNetwork();
			  
		  } 

	}
	
	private static IParsingEngine importFile (String fileName, TestMeasurement m) throws Exception {
		  IParsingEngine parser;	
		  String testFile = fileName;
		  if ( m.srcFormat == NetworkSourceFormat.XGMML) {
			  parser = new XgmmlParser(testFile, AllTests.testUser, 
			  			AllTests.db,m.fileName, "");
		  } else if ( m.srcFormat == NetworkSourceFormat.BEL) {
			  parser = new XbelParser(testFile,AllTests.testUser, AllTests.db, "");
		  } else if (m.srcFormat == NetworkSourceFormat.SIF) {
			  parser = new SifParser(testFile,AllTests.testUser, AllTests.db, FilenameUtils.getBaseName( m.fileName), "" );
		  } else if ( m.srcFormat == NetworkSourceFormat.BIOPAX) {
			  parser = new BioPAXParser ( testFile, AllTests.testUser, AllTests.db, FilenameUtils.getBaseName( m.fileName), "");
		  } else 
			  throw new Exception ("unsupported source format " + m.srcFormat);
		  
		  parser.parseFile();
		  
		  return parser;

	}	
    private static void assertEquivalence(UUID networkID, TestMeasurement m) throws NdexException {

    	// verify a uploaded network
		 try (ODatabaseDocumentTx conn = AllTests.db.getAConnection()) {
			 NetworkDAO dao = new NetworkDAO(conn);
			 Network n = dao.getNetworkById(networkID);
			 assertEquals(n.getName(), m.networkName);
			 assertNotNull(n.getDescription());
			 assertEquals(n.getNodeCount(), n.getNodes().size());
			 assertEquals(n.getNodeCount(), m.nodeCnt);
			 assertEquals(n.getEdgeCount(), m.edgeCnt);
			 assertEquals(n.getEdges().size(), m.edgeCnt);
			 if (m.basetermCnt >=0 ) {
/*				 TreeSet<String> s = new TreeSet<>();

				 for ( BaseTerm ss : n.getBaseTerms().values()) {
					 s.add(ss.getName());
					 
				 }
				 int i =0;
				 for(String si : s) { 
				   System.out.println(i + "\t" + si);
				   i++;
				 } */
				 assertEquals(n.getBaseTerms().size(), m.basetermCnt);
			 } 
			 if ( m.citationCnt >= 0 )
				 assertEquals(n.getCitations().size(), m.citationCnt);
	//		 if ( m.elmtPresPropCnt >= 0 )
	//			 assertEquals(n.getBaseTerms().size(), m.basetermCnt);
	//		 if ( m.elmtPropCnt >=0)
	//			 assertEquals(n.getBaseTerms().size(), m.basetermCnt);
			 if ( m.funcTermCnt >=0 )
				 assertEquals(n.getFunctionTerms().size(), m.funcTermCnt);
			 if ( m.nameSpaceCnt >=0 )
				 assertEquals(n.getNamespaces().size(), m.nameSpaceCnt);
		//	 if ( m.netPresPropCnt >=0 )
		//		 assertEquals(n.getPresentationProperties().size(), m.netPresPropCnt);
			 if ( m.netPropCnt >=0 )
				 assertEquals(n.getProperties().size(), m.netPropCnt+1);
			 if ( m.reifiedEdgeCnt >=0 )
				 assertEquals(n.getReifiedEdgeTerms().size(), m.reifiedEdgeCnt);
			 if ( m.support >=0 )
				 assertEquals(n.getSupports().size(), m.support);
			 if ( m.nodePropCnt >=0 ) {
				 int i = 0 ;
				 for ( Node node : n.getNodes().values() ) {
					 i += node.getProperties().size();
				 }
				 assertEquals(i, m.nodePropCnt);
			 }
			 if ( m.edgePropCnt >=0 ) {
				 int i = 0 ;
				 for ( Edge edge : n.getEdges().values() ) {
					 i += edge.getProperties().size();
				 }
				 assertEquals(i, m.edgePropCnt);
			 }
			 n = null;	 
		 }
   
    }
 

}
