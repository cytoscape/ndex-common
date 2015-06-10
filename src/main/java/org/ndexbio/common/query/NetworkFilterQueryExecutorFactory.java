/**
 *   Copyright (c) 2013, 2015
 *  	The Regents of the University of California
 *  	The Cytoscape Consortium
 *
 *   Permission to use, copy, modify, and distribute this software for any
 *   purpose with or without fee is hereby granted, provided that the above
 *   copyright notice and this permission notice appear in all copies.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.ndexbio.common.query;

import org.ndexbio.common.NdexClasses;
import org.ndexbio.common.models.dao.orientdb.Helper;
import org.ndexbio.common.models.dao.orientdb.NetworkDocDAO;
import org.ndexbio.common.query.filter.orientdb.EdgeByEdgePropertyFilterODB;
import org.ndexbio.common.query.filter.orientdb.EdgeByNodePropertyFilterODB;
import org.ndexbio.common.query.filter.orientdb.EdgeCollectionQueryODB;
import org.ndexbio.common.util.TermStringType;
import org.ndexbio.common.util.TermUtilities;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.network.query.EdgeByEdgePropertyFilter;
import org.ndexbio.model.network.query.EdgeByNodePropertyFilter;
import org.ndexbio.model.network.query.EdgeCollectionQuery;
import org.ndexbio.model.network.query.PropertySpecification;

import com.orientechnologies.orient.core.record.impl.ODocument;

public class NetworkFilterQueryExecutorFactory {
	
	private static final String edgePredicatePropertyName = "ndex:predicate";
	
	private static final String nodePropertyFunctionTermType = "ndex:functionTermType";
	private static final String nodePropertyNameORTerm = "ndex:nameOrTermName";
	private static final String nodePropertyNodeName = "ndex:nodeName";
	
	public static NetworkFilterQueryExecutor createODBExecutor(String networkIdStr, EdgeCollectionQuery query) throws NdexException {
		
		// check if the query is valid
		if ( query.getEdgeFilter() == null || query.getEdgeFilter().getPropertySpecifications().size() == 0) {
			if ( query.getNodeFilter() == null || query.getNodeFilter().getPropertySpecifications().size() == 0 )  {  //error
				throw new NdexException ("Invalid query object received. Both filters are empty.");
			}
		} 
		
		//TODO: optimize the case that when filter compiled to an empty list. Should just return empty collection without iteration.
		
		EdgeCollectionQueryODB edgeQuery = new EdgeCollectionQueryODB();
		edgeQuery.setQueryName(query.getQueryName());
		edgeQuery.setEdgeLimit(query.getEdgeLimit());
		
		try ( NetworkDocDAO networkDao = new NetworkDocDAO()) {
		
			ODocument networkDoc = networkDao.getNetworkDocByUUIDString(networkIdStr);
			
			EdgeByEdgePropertyFilterODB edgeFilter = preprocessEdgeByEdgePropertyFilter(
					   query.getEdgeFilter(), networkDoc)	;
			
			EdgeByNodePropertyFilterODB nodeFilter = preprocessEdgeByNodePropertyFilter(
					query.getNodeFilter(), networkDoc);
			
			edgeQuery.setEdgeFilter(edgeFilter);
			edgeQuery.setNodeFilter(nodeFilter);
		  NetworkFilterQueryExecutor executor = new NetworkFilterQueryExecutor(networkIdStr, edgeQuery);
		
		  return executor;
		}
	}

	
	private static EdgeByEdgePropertyFilterODB preprocessEdgeByEdgePropertyFilter(
			   EdgeByEdgePropertyFilter filter, ODocument networkDoc) throws NdexException {
		if ( filter == null) return null;
		
		EdgeByEdgePropertyFilterODB odbFilter = new EdgeByEdgePropertyFilterODB();

		for ( PropertySpecification spec : filter.getPropertySpecifications()) {
			String value = spec.getValue();
			String propName = spec.getName();
			if ( propName.equalsIgnoreCase(edgePredicatePropertyName) ) {
				Iterable<ODocument> bTerms = Helper.getNetworkElements(networkDoc, NdexClasses.Network_E_BaseTerms);
				if ( bTerms !=null) {
					for ( ODocument d : bTerms) {
						String name = d.field(NdexClasses.BTerm_P_name);
						if ( name !=null && name.equalsIgnoreCase(value)) {
							odbFilter.addPredicateId(d.getIdentity().toString());
						}
					}
				}
			} else {  // normal properties
				for ( ODocument baseTermDoc : Helper.getNetworkElements(networkDoc, NdexClasses.Network_E_BaseTerms)) {
					if (propertyNameMatchesBaseterm(propName, baseTermDoc) ) {
					   for ( ODocument prop : Helper.getDocumentLinks(baseTermDoc, "in_", NdexClasses.ndexProp_E_predicate)) {
							   String v = prop.field(NdexClasses.ndexProp_P_value);
							   if ( v.equalsIgnoreCase(value)) {
								   odbFilter.addPropertyId(prop.getIdentity().toString());
							   }
					   }
					}
				}
			}
		}
		
		return odbFilter;
	}

	private static EdgeByNodePropertyFilterODB preprocessEdgeByNodePropertyFilter(
			   EdgeByNodePropertyFilter filter, ODocument networkDoc) throws NdexException {
		if ( filter == null) return null;
		
		EdgeByNodePropertyFilterODB odbFilter = new EdgeByNodePropertyFilterODB();
		odbFilter.setMode(filter.getMode());
		
		for (PropertySpecification spec: filter.getPropertySpecifications()) {
			String value = spec.getValue();
			String propName = spec.getName();
			if ( propName.equalsIgnoreCase(nodePropertyNodeName) ) {
		       odbFilter.addNodeName(value);
			} else if (propName.equalsIgnoreCase(nodePropertyNameORTerm)) {
			       odbFilter.addNodeName(value);
					for ( ODocument d : Helper.getNetworkElements(networkDoc, NdexClasses.Network_E_BaseTerms)) {
							String name = d.field(NdexClasses.BTerm_P_name);
							if ( name !=null && name.equalsIgnoreCase(value)) {
								odbFilter.addRepresentTermID(d.getIdentity().toString());
							}
					}
			} else if (propName.equalsIgnoreCase(nodePropertyFunctionTermType)) {
					for ( ODocument funcTermDoc : Helper.getNetworkElements(networkDoc, NdexClasses.Network_E_FunctionTerms)) {
						ODocument fBTerm = funcTermDoc.field("out_"+NdexClasses.FunctionTerm_E_baseTerm);
						String name = fBTerm.field(NdexClasses.BTerm_P_name);
						if ( name !=null && name.equalsIgnoreCase(value)) {
							odbFilter.addRepresentTermID(funcTermDoc.getIdentity().toString());
						}
					}
			} else {  // normal property
				for ( ODocument baseTermDoc : Helper.getNetworkElements(networkDoc, NdexClasses.Network_E_BaseTerms)) {
					
					if (propertyNameMatchesBaseterm(propName, baseTermDoc)) {
					   for ( ODocument prop : Helper.getDocumentLinks(baseTermDoc, "in_", NdexClasses.ndexProp_E_predicate)) {
						   String v = prop.field(NdexClasses.ndexProp_P_value);
						   if ( v.equalsIgnoreCase(value)) {
								   odbFilter.addPropertyId(prop.getIdentity().toString());
						   }
					   }
					}
				}
			}
		}
		return odbFilter;
	}

	private static boolean propertyNameMatchesBaseterm(String propertyName, ODocument baseTermDoc) throws NdexException {

		String name = baseTermDoc.field(NdexClasses.BTerm_P_name);
		
		if ( name == null) return false;
		
		TermStringType termType = TermUtilities.getTermType(propertyName);
		
		switch (termType) {
		case URI:
			throw new NdexException("URI type baseterm search not implemented yet.");
	//		break;
		case CURIE:
			String[] qname =TermUtilities.getNdexQName(propertyName);
			if ( ! name.equalsIgnoreCase(qname[1]) ) return false;
			
			ODocument nsDoc = baseTermDoc.field("out_"+NdexClasses.BTerm_E_Namespace);
			String prefix = nsDoc.field(NdexClasses.ns_P_prefix);
			if (prefix !=null && prefix.equalsIgnoreCase(qname[0])) 
				  return true;	 
			break;
		case NAME:
			if ( name.equalsIgnoreCase(propertyName)) 
				return true;
			break;
		default:
			break;
		}

		
		return false;
		
	}
	
}
