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
package org.ndexbio.common.access;

import java.util.logging.Logger;

import org.ndexbio.model.exceptions.NdexException;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;

public class NdexAOrientDBConnectionPool {
	
//	private static NdexAOrientDBConnectionPool INSTANCE = null;

	
	private OrientGraphFactory pool;
	
	
	private static final Logger logger = Logger
			.getLogger(NdexAOrientDBConnectionPool.class.getName());

	private NdexAOrientDBConnectionPool(String dbURL, String dbUserName, String dbPassword, int size) {

		pool = new OrientGraphFactory(dbURL, dbUserName, dbPassword).setupPool(1,size);
		pool.setAutoScaleEdgeType(true);
		pool.setEdgeContainerEmbedded2TreeThreshold(40);
		pool.setUseLightweightEdges(true);
	    
	    logger.info("Connection pool to " + dbUserName + "@" + dbURL + " created.");
	}
	
/*	
	private static synchronized void createOrientDBConnectionPool (String dbURL, String dbUserName,
				String dbPassword, int size) {
	      if(INSTANCE == null) {
		         INSTANCE = new NdexAOrientDBConnectionPool(dbURL, dbUserName, dbPassword, size);
	      }
	}
	
	private static synchronized NdexAOrientDBConnectionPool getInstance() throws NdexException {
	      if(INSTANCE == null) {
	         throw new NdexException ("Connection pool is not created yet.");
	      }
	      return INSTANCE;
	}

	private ODatabaseDocumentTx acquire() {
		ODatabaseDocumentTx conn = pool.getDatabase();
		
	    return conn;
	}
   	
   private static synchronized void close() { 	
	 
       if ( INSTANCE != null) {	   
	     INSTANCE.pool.close();
	     INSTANCE=null;
         logger.info("Connection pool closed.");
       } else 
         logger.info("Connection pool already closed.");
   }
*/   
   
}
