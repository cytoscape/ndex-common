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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.ndexbio.common.access.NdexDatabase;
import org.ndexbio.common.models.dao.orientdb.Helper;
import org.ndexbio.common.models.dao.orientdb.UserDocDAO;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.object.NdexPropertyValuePair;
import org.ndexbio.model.object.NdexProvenanceEventType;
import org.ndexbio.model.object.ProvenanceEntity;
import org.ndexbio.model.object.SimplePropertyValuePair;
import org.ndexbio.common.persistence.orientdb.NdexPersistenceService;
import org.ndexbio.common.util.TermStringType;
import org.ndexbio.common.util.TermUtilities;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.network.NetworkSourceFormat;
import org.ndexbio.model.object.network.NetworkSummary;
import org.ndexbio.model.tools.ProvenanceHelpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.io.Files;


/*
 * Lines in the SIF file specify a source node, a relationship type
 * (or edge type), and one or more target nodes.
 * 
 * see: http://wiki.cytoscape.org/Cytoscape_User_Manual/Network_Formats
 */

public class SifParser implements IParsingEngine {
	private final File sifFile;
	private final String sifURI;
	private final String extendedBinarySIFEdgeHeader = "PARTICIPANT_A	INTERACTION_TYPE	PARTICIPANT_B	INTERACTION_DATA_SOURCE	INTERACTION_PUBMED_ID";
	private final String extendedBinarySIFAliasHeader = "PARTICIPANT	PARTICIPANT_TYPE	PARTICIPANT_NAME	UNIFICATION_XREF	RELATIONSHIP_XREF";
														 
	private final String extendedBinarySIFPropertiesHeader = "NAME	ORGANISM	URI	DATASOURCE";
	private final List<String> msgBuffer;
	
	private static Logger logger = Logger.getLogger("SifParser");

	
	private NdexPersistenceService persistenceService;

    private String taskDescription;
    private User loggedInUser;
	
//	private TreeSet<String> pubmedIdSet;

	public SifParser(String fn, String ownerName, NdexDatabase db, String networkName, String taskDescription) throws Exception {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(fn),
				"A filename is required");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(ownerName),
				"A network owner name is required");
		this.msgBuffer = Lists.newArrayList();
		if ( fn.startsWith("/") || fn.matches("^[a-zA-Z]:.*")) 
			this.sifFile = new File(fn);
		else
		    this.sifFile = new File(getClass().getClassLoader().getResource(fn).toURI());
		this.sifURI = sifFile.toURI().toString();
		this.persistenceService = new NdexPersistenceService(db);
		
		String title = networkName;
		if ( title == null) 
			title = Files.getNameWithoutExtension(this.sifFile.getName());

		persistenceService.createNewNetwork(ownerName, title, null);
        this.taskDescription = taskDescription;

        try (UserDocDAO userDocDAO = new UserDocDAO(db.getAConnection())) {
        	loggedInUser = userDocDAO.getUserByAccountName(ownerName);
        }

//		addSystemDefaultNamespaces();
		
	}

	public List<String> getMsgBuffer() {
		return this.msgBuffer;
	}

	public String getSIFURI() {
		return sifURI;
	}

	public File getSifFile() {
		return sifFile;
	}

	/**************************************************************************
	 * Whitespace (space or tab) is used to delimit the names in the simple
	 * interaction file format. However, in some cases spaces are desired in a
	 * node name or edge type. The standard is that, if the file contains any
	 * tab characters, then tabs are used to delimit the fields and spaces are
	 * considered part of the name. If the file contains no tabs, then any
	 * spaces are delimiters that separate names (and names cannot contain
	 * spaces).
	 * @throws JsonProcessingException 
	 * @throws NdexException 
	 **************************************************************************/
	@Override
	public void parseFile() throws  NdexException {

		try (BufferedReader bufferedReader = 
				new BufferedReader(new FileReader(this.getSifFile()))){

			this.getMsgBuffer().add("Parsing lines from " + this.getSIFURI());

			boolean extendedBinarySIF = checkForExtendedFormat();
			if (extendedBinarySIF) {
				this.processExtendedBinarySIF(bufferedReader);
//				this.networkService.setFormat("EXTENDED_BINARY_SIF");
			} else {
				boolean tabDelimited = scanForTabs();
				this.processSimpleSIFLines(tabDelimited, bufferedReader);
//				this.networkService.setFormat("BINARY_SIF");
			}

			//add provenance to network
			NetworkSummary currentNetwork = this.persistenceService.getCurrentNetwork();
			
			// set the source format
			this.persistenceService.setNetworkSourceFormat(NetworkSourceFormat.SIF);
			
			String uri = NdexDatabase.getURIPrefix();
			
			// close database connection
			this.persistenceService.persistNetwork();

            ProvenanceEntity provEntity = ProvenanceHelpers.createProvenanceHistory(currentNetwork,
                    uri, NdexProvenanceEventType.FILE_UPLOAD, currentNetwork.getCreationTime(), 
                    (ProvenanceEntity)null);
            Helper.populateProvenanceEntity(provEntity, currentNetwork);
            provEntity.getCreationEvent().setEndedAtTime(currentNetwork.getModificationTime());

            List<SimplePropertyValuePair> l = provEntity.getCreationEvent().getProperties();
            Helper.addUserInfoToProvenanceEventProperties( l, loggedInUser);
            l.add(	new SimplePropertyValuePair ( "filename",taskDescription) );

            this.persistenceService.setNetworkProvenance(provEntity);

            persistenceService.commit();
			
		} catch (Exception e) {
			// delete network and close the database connection
			e.printStackTrace();
			this.persistenceService.abortTransaction();
			throw new NdexException("Error occurred when loading file " +
					this.sifFile.getName() + ". " + e.getMessage() );
		}
        finally
        {
            persistenceService.close();
        }
	}

	private boolean checkForExtendedFormat() throws IOException {
		try (BufferedReader bufferedReader = 
				new BufferedReader(new FileReader(this.getSifFile()))){
			String line = bufferedReader.readLine();
			// Check the first line for the EBS header
			if ( line.startsWith(extendedBinarySIFEdgeHeader)) {
				bufferedReader.close();
				return true;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IOException(e);
		} 
		return false;
	}

	private boolean scanForTabs() throws IOException {
		try (BufferedReader bufferedReader
					= new BufferedReader(new FileReader(this.getSifFile()))){
			String line;
			int counter = 0;
			// Check the first 20 lines for tabs
			while ((line = bufferedReader.readLine()) != null) {
				if (line.indexOf("\t") != -1)
					return true;
				if (counter++ > 20)
					return false;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new IOException(e);
		} 

		return false;
	}

	private void processSimpleSIFLines(boolean tabDelimited,
			BufferedReader bufferedReader) throws IOException, ExecutionException, NdexException {

		try {

			String line;
			while ((line = bufferedReader.readLine()) != null) {
				String[] tokens = null;
				if (tabDelimited) {
					tokens = line.split("\t");
				} else {
					tokens = line.split("\\s+");
				}

				if (tokens.length == 1)
					addNode(tokens[0]); 
//				if (tokens.length == 3)
//					addEdge(tokens[0], tokens[1], tokens[2]);  
				else if ( tokens.length ==2 ) 
					throw new NdexException ("Invalid data format found in line: " + line);
				else {
				  for ( int i = 2 ; i < tokens.length; i++ )
					  addEdge ( tokens[0], tokens[1],tokens[i]);
				}
			}
		} catch (IOException e) {
			this.getMsgBuffer().add(e.getMessage());
			throw e;
		} finally {
			bufferedReader.close();
		}
	}

	/*
	 * Standard Extended Binary SIF has two sections, one for edges and one to
	 * define aliases for terms used in the edges. NDEx Extended Binary SIF has
	 * an additional section that captures some additional network meta-data
	 * 
	 * Each section is preceded by its header, so the parsing mode switches as
	 * each header is encountered. We already know that line 0 is the edge
	 * header so we start processing edges on the next line.
	 */
	private void processExtendedBinarySIF(BufferedReader bufferedReader)
			throws IOException, ExecutionException, NdexException {
		try {
			// skip the header line
			bufferedReader.readLine();

			String line;
			int counter = 0;
			while ((line = bufferedReader.readLine()) != null) {
				if (line.indexOf(extendedBinarySIFAliasHeader) != -1) {
					processExtendedBinarySIFAliases(bufferedReader);  
					break;
				}
				String[] tokens = null;
				tokens = line.split("\t");
				if (tokens.length > 2) {
					// "PARTICIPANT_A	INTERACTION_TYPE	PARTICIPANT_B	INTERACTION_DATA_SOURCE	INTERACTION_PUBMED_ID";
					String subject = tokens[0];
					String predicate = tokens[1];
					String object = tokens[2];
					// String dataSource = null; // ignored for now
					String[] pubMedIds = null;
					if (tokens.length > 4 && tokens[4] != null && tokens[4].length()>0) {
						pubMedIds = tokens[4].split(";");
					}

					Long edgeId = addEdge(subject, predicate, object);
					counter ++;
					if ( counter % 2000 == 0 ) {
						logger.info("processed " + counter + " lines so far. commit this batch.");
						this.persistenceService.commit();
					}
					
					if (pubMedIds != null) {
						List<Long> citationIds = new ArrayList<> (pubMedIds.length);
						
						for (String pubMedId : pubMedIds) {
							String[] pubmedIdTokens = pubMedId.split(":");
							if ( pubmedIdTokens.length ==2 ) {
								if ( pubmedIdTokens[0].equals("Pubmed")) {
									Long citationId = this.persistenceService.getCitationId(
										"", NdexPersistenceService.URICitationType,
										NdexPersistenceService.pmidPrefix + pubmedIdTokens[1], null);
									citationIds.add(citationId);
								
								} else if ( pubmedIdTokens[0].equals("ISBN")){
									Long citationId = this.persistenceService.getCitationId(
											"", NdexPersistenceService.URICitationType, pubMedId, null);
									citationIds.add(citationId);
								} else {	
								  logger.warning("Unsupported Pubmed id format: " + 
							       pubMedId + " found in file.\n line:\n " + line +"\n Ignore this pubmedId.\n" ); 
								}
							} else if (pubmedIdTokens.length == 1 ) {
								String pubmedId = pubmedIdTokens[0];
								if ( pubmedId.length() > 0 ) {
									Long citationId = this.persistenceService.getCitationId(
										"", NdexPersistenceService.URICitationType,
										NdexPersistenceService.pmidPrefix + pubmedIdTokens[0], null);
									citationIds.add(citationId);
								}
							} else 
								throw new NdexException("Invalid Pubmed format in line: " + line);
							
							this.persistenceService.addCitationsToElement(edgeId, citationIds);
						}
					}

				}
			}
		} catch (IOException e) {
			this.getMsgBuffer().add(e.getMessage());
			throw e;
		} finally {
			bufferedReader.close();
		}
	}


	private void processExtendedBinarySIFAliases(BufferedReader bufferedReader)
			throws IOException, ExecutionException, NdexException {

		// "PARTICIPANT	PARTICIPANT_TYPE	PARTICIPANT_NAME	UNIFICATION_XREF	RELATIONSHIP_XREF";
		System.out.println("Processing Aliases");
		String line;
		int counter = 0;
		while ((line = bufferedReader.readLine()) != null) {
		//	System.out.println("-- " + line);
			if (line.indexOf(extendedBinarySIFPropertiesHeader) != -1) {
				System.out.println("found properties header");
				processExtendedBinarySIFProperties(bufferedReader);
				break;
			} else if ("".equals(line)) {
				// skip blank lines.
			} else {
				// System.out.println("aliases: " + line);
				// Process one line of aliases
				String[] tokens = null;
				tokens = line.split("\t");
				counter ++;
				if ( counter % 2000 == 0 ) {
					logger.info("Aliases processed " + counter + " lines. commit batch.");
					this.persistenceService.commit();
				}
				if (tokens.length > 2) {
					String participantIdentifier = tokens[0];
					// find the node that represents the term specified by the
					// participantIdentifier
					Long participantNodeId = addNode(participantIdentifier);
					if (participantNodeId == null)
						break;
					//String type = tokens[1];
					List<String> aliasList = new LinkedList<>();
					aliasList.add(tokens[2]);
					//String name = tokens[2];
					// special case processing for "_HUMAN" suffix # this logic is removed from 1.3.2
				/*	int humanSuffixIndex = name.indexOf("_HUMAN");
					if (humanSuffixIndex != -1){
						name = name.substring(0, humanSuffixIndex);
					} 
					//participant.setName(name);
					this.persistenceService.setNodeName(participantNodeId, name); */
					
					if (tokens.length > 3) {
						String[] unificationAliases = tokens[3].split(";");
						if ( unificationAliases !=null ) {
							for (String a : unificationAliases) 
								aliasList.add(a);
						}
						this.persistenceService.addAliasToNode(participantNodeId,aliasList);
						if (tokens.length > 4) {
							String[] relationshipAliases = tokens[4].split(";");
							this.persistenceService.setRelatedTermsOnNode(participantNodeId, relationshipAliases);
						}
					}
				}
			}
		}
	}

	private void processExtendedBinarySIFProperties(
			BufferedReader bufferedReader) throws IOException, NdexException, ExecutionException {

		// NAME\tORGANISM\tURI\tDATASOURCE";
		// this is currently one line of properties, but perhaps it would be
		// better to have one property per line.
		System.out.println("Processing one line of Network Properties");
		String line = bufferedReader.readLine();
		if (line != null) {
			String[] values = line.split("\t");
			if (values.length > 0 && values[0] != null) {
				this.persistenceService.setNetworkTitleAndDescription(
						values[0], null);
			}
			
			List<NdexPropertyValuePair> props = new ArrayList<>();
			
			if (values.length > 1 && values[1] != null) {
                NdexPropertyValuePair p = new NdexPropertyValuePair ("ORGANISM", values[1]);
                props.add(p);
			}
			
			if (values.length > 2 && values[2] != null) {
                NdexPropertyValuePair p = new NdexPropertyValuePair ("URI", values[2]);
                props.add(p);
			}

			if (values.length > 3 && values[3] != null) {
			//	System.out.println("Source: " + values[3]);
				String source = values[3];
				if (source.equals("http://purl.org/pc2/4/pid")){
					source = "PID";
				}
				props.add(new NdexPropertyValuePair("Source" , source));
			}
			this.persistenceService.setNetworkProperties(props, null);
		}
	}
	
	

	private Long addNode(String name) throws ExecutionException, NdexException {
		TermStringType stype = TermUtilities.getTermType(name);
		if ( stype == TermStringType.NAME) {
			return persistenceService.getNodeIdByName(name);
		} 
		return persistenceService.getNodeIdByBaseTerm(name);
		
	}


	private Long addEdge(String subject, String predicate, String object)
			throws ExecutionException, NdexException {
		Long subjectNodeId = addNode(subject);
		Long objectNodeId = addNode(object);
		Long predicateTermId = persistenceService.getBaseTermId(predicate);
		return persistenceService.getEdge(subjectNodeId, objectNodeId,
				predicateTermId, null,null,null);

	}
	
/*	
	private void addSystemDefaultNamespaces() throws NdexException {
		this.persistenceService.createNamespace2("UniProt", 	"http://identifiers.org/uniprot/");
		this.persistenceService.createNamespace2("Ensembl", 	"http://ndex.org/Ensembl/");
		this.persistenceService.createNamespace2("Pubmed",	"http://www.ncbi.nlm.nih.gov/pubmed/");

		this.persistenceService.createNamespace2("CHEBI",	"http://identifiers.org/chebi/");
		this.persistenceService.createNamespace2("Reactome",	"http://identifiers.org/reactome/");
		this.persistenceService.createNamespace2("RefSeq",	"http://identifiers.org/refseq/");
		this.persistenceService.createNamespace2("HGNC Symbol","http://identifiers.org/hgnc.symbol/");
		this.persistenceService.createNamespace2("HGNC",		"http://identifiers.org/hgnc/");
		this.persistenceService.createNamespace2("NCBI Gene","http://identifiers.org/ncbigene/");
		this.persistenceService.createNamespace2("InChIKey",	"http://identifiers.org/inchikey/");
		this.persistenceService.createNamespace2("pubchem-substance","http://identifiers.org/pubchem.substance/");
		this.persistenceService.createNamespace2("pubchem",	"http://identifiers.org/pubchem.compound/");
		this.persistenceService.createNamespace2("omim",		"http://identifiers.org/omim/");
		this.persistenceService.createNamespace2("PROTEIN DATA BANK","http://identifiers.org/pdb/");
		this.persistenceService.createNamespace2("Panther Family","http://identifiers.org/panther.family/");
		this.persistenceService.createNamespace2("CAS",		"http://identifiers.org/cas/");
		
	}
*/	
	@Override
	public UUID getUUIDOfUploadedNetwork() {
		try { 
			return persistenceService.getCurrentNetwork().getExternalId();
		} catch ( Exception e) {
			e.printStackTrace();
			return null;
		}
	}

}
