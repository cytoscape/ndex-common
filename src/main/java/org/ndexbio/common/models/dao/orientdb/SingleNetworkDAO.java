package org.ndexbio.common.models.dao.orientdb;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.cxio.aspects.datamodels.EdgeAttributesElement;
import org.cxio.aspects.datamodels.EdgesElement;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.aspects.datamodels.NodesElement;
import org.cxio.core.CxWriter;
import org.cxio.core.interfaces.AspectElement;
import org.cxio.core.interfaces.AspectFragmentWriter;
import org.cxio.util.Util;
import org.ndexbio.common.NdexClasses;
import org.ndexbio.common.access.NdexDatabase;
import org.ndexbio.common.cx.aspect.GeneralAspectFragmentWriter;
import org.ndexbio.model.cx.SupportElement;
import org.ndexbio.model.cx.FunctionTermsElement;
import org.ndexbio.model.cx.NamespacesElement;
import org.ndexbio.model.cx.NodeCitationLinksElement;
import org.ndexbio.model.cx.NodeSupportLinksElement;
import org.ndexbio.model.cx.CitationElement;
import org.ndexbio.model.cx.CitationLinksElement;
import org.ndexbio.model.cx.EdgeCitationLinksElement;
import org.ndexbio.model.cx.EdgeSupportLinksElement;
import org.ndexbio.model.cx.ReifiedEdgeElement;
import org.ndexbio.model.exceptions.NdexException;
import org.ndexbio.model.exceptions.ObjectNotFoundException;
import org.ndexbio.model.object.NdexPropertyValuePair;
import org.ndexbio.model.object.PropertiedObject;
import org.ndexbio.model.object.network.Namespace;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORecordId;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.record.impl.ODocument;

public class SingleNetworkDAO implements AutoCloseable {
		
	private ODatabaseDocumentTx db;
	private ODocument networkDoc;
	private OIndex<?> btermIdIdx;
    private OIndex<?> nsIdIdx;
    private OIndex<?> citationIdIdx;
    private OIndex<?> supportIdIdx;
    private OIndex<?> funcIdIdx;
    private OIndex<?> reifiedEdgeIdIdx;
    
    
    
	public SingleNetworkDAO ( String UUID) throws NdexException {
		db  = NdexDatabase.getInstance().getAConnection();	
		networkDoc = getRecordByUUIDStr(UUID);
    	btermIdIdx = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_bterm_id);
    	nsIdIdx = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_ns_id);
    	citationIdIdx = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_citation_id);
    	supportIdIdx = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_support_id);
        funcIdIdx  = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_function_id);
        reifiedEdgeIdIdx = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_reifiededge_id);
	}

	private ODocument getRecordByUUIDStr(String id) 
			throws ObjectNotFoundException, NdexException {
		
			OIndex<?> Idx;
			OIdentifiable record = null;
			
			Idx = this.db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_UUID);
			OIdentifiable temp = (OIdentifiable) Idx.get(id);
			if((temp != null) )
				record = temp;
			else	
				throw new ObjectNotFoundException("Network with ID: " + id + " doesn't exist.");
			
			return (ODocument) record.getRecord();
	}
	
    private Iterable<ODocument> getNetworkElements(String elementEdgeString) {	
    	
    	Object f = networkDoc.field("out_"+ elementEdgeString);
    	
    	if ( f == null) return Helper.emptyDocs;
    	
    	if ( f instanceof ODocument)
    		 return new OrientDBIterableSingleLink((ODocument)f);
    	
    	Iterable<ODocument> iterable = (Iterable<ODocument>)f;
		return iterable;
    	     
    }
	
	public Iterator<Namespace> getNamespaces() {
		return new NamespaceIterator(getNetworkElements(NdexClasses.Network_E_Namespace));
	}
		
	public Iterable<CitationElement>  getCXCitations () {
		return new CXCitationCollection(getNetworkElements(NdexClasses.Network_E_Citations),db);
	}
	
	
	public void writeNetworkInCX(OutputStream out, final boolean use_default_pretty_printer) throws IOException, ObjectNotFoundException {
        CxWriter cxwtr = CxWriter.createInstance(out, use_default_pretty_printer);
        
        GeneralAspectFragmentWriter cfw = new GeneralAspectFragmentWriter(CitationElement.NAME);
        
        for (AspectFragmentWriter afw : Util.getAllAvailableAspectFragmentWriters() ) {
        	cxwtr.addAspectFragmentWriter(afw);
        }
        
        cxwtr.addAspectFragmentWriter(cfw);
        cxwtr.addAspectFragmentWriter(new GeneralAspectFragmentWriter(SupportElement.NAME));
        cxwtr.addAspectFragmentWriter(new GeneralAspectFragmentWriter(NodeCitationLinksElement.NAME));
        cxwtr.addAspectFragmentWriter(new GeneralAspectFragmentWriter(EdgeCitationLinksElement.NAME));
        cxwtr.addAspectFragmentWriter(new GeneralAspectFragmentWriter(EdgeSupportLinksElement.NAME));
        cxwtr.addAspectFragmentWriter(new GeneralAspectFragmentWriter(NodeSupportLinksElement.NAME));
        cxwtr.addAspectFragmentWriter(new GeneralAspectFragmentWriter(FunctionTermsElement.NAME));
        cxwtr.addAspectFragmentWriter(new GeneralAspectFragmentWriter(NamespacesElement.NAME));
        cxwtr.addAspectFragmentWriter(new GeneralAspectFragmentWriter(ReifiedEdgeElement.NAME));
        
        cxwtr.start();
        
        
        //write namespaces
        NamespacesElement prefixtab = new NamespacesElement();
        
        for ( ODocument doc : getNetworkElements(NdexClasses.Network_E_Namespace))  {
           String prefix = doc.field(NdexClasses.ns_P_prefix);
           if ( prefix !=null ) {
        	   String uri = doc.field(NdexClasses.ns_P_uri);
        	   prefixtab.put(prefix, uri);
           }
        }
         
        if ( prefixtab .size() >0) {
        	writeNdexAspectElementAsAspectFragment(cxwtr, prefixtab);
        }
        
        Map<Long,String> citationIdMap = new TreeMap<> ();
        Map<Long,String> supportIdMap = new TreeMap<> ();
        Set<Long> repIdSet = new TreeSet<> ();

        
        
        for ( ODocument doc : getNetworkElements(NdexClasses.Network_E_Citations)) {
        	Long citationId = doc.field(NdexClasses.Element_ID);
        	String SID = writeCitationInCX(doc, cxwtr);
        	citationIdMap.put(citationId, SID);
        }
        
        for ( ODocument doc: getNetworkElements (NdexClasses.Network_E_Supports)) {
        	Long supportId = doc.field(NdexClasses.Element_ID);
        	String SID = writeSupportInCX(doc, cxwtr);
        	supportIdMap.put(supportId, SID);
        }
           
        
        for ( ODocument doc : getNetworkElements(NdexClasses.Network_E_Nodes)) {
        	writeNodeInCX(doc, cxwtr, repIdSet, citationIdMap, supportIdMap);
        }        
        
        for ( ODocument doc : getNetworkElements(NdexClasses.Network_E_Edges)) {
        	writeEdgeInCX(doc,cxwtr, citationIdMap, supportIdMap);
        }
        
        cxwtr.end();

	}
	
	private void writeEdgeInCX(ODocument doc, CxWriter cxwtr, Map<Long,String> citationIdMap,
			    Map<Long,String> supportIdMap ) throws ObjectNotFoundException, IOException {
		String SID = doc.field(NdexClasses.Element_SID);
		
		if ( SID ==null) {
			 SID = ((Long)doc.field(NdexClasses.Element_ID)).toString();
		}
		
		ODocument srcDoc = doc.field("in_"+ NdexClasses.Edge_E_subject);
		ODocument tgtDoc = doc.field("out_"+NdexClasses.Edge_E_object);
		
		String srcId = srcDoc.field(NdexClasses.Element_SID);
		if ( srcId == null )
			srcId = ( (Long)srcDoc.field(NdexClasses.Element_ID)).toString();
		
		String tgtId = tgtDoc.field(NdexClasses.Element_SID);
		if ( tgtId == null)
			tgtId = ((Long)tgtDoc.field(NdexClasses.Element_ID)).toString();
		
		String relation = null;
		Long predicate= doc.field(NdexClasses.Edge_P_predicateId);
		
		if ( predicate !=null) {
			relation = this.getBaseTermStringById(predicate);
		}
		
		EdgesElement e = new EdgesElement(SID, srcId, tgtId,relation);
		
		writeNdexAspectElementAsAspectFragment(cxwtr,e);
	  
    	// write other properties
    	writeDocPropertiesAsCX(doc, cxwtr);
    	
    	//write citations
    	writeCitationsAndSupports(SID,  doc, cxwtr, citationIdMap,
			   supportIdMap , true );
       
	}
	
	private void writeCitationsAndSupports(String SID, ODocument doc,  CxWriter cxwtr, Map<Long,String> citationIdMap,
			    Map<Long,String> supportIdMap ,boolean isEdge ) throws ObjectNotFoundException, IOException {
	   	
		//write citations
    	Collection<Long> citations = doc.field(NdexClasses.Citation);
    	
    	if ( citations !=null) {
    		List<String> cids = new ArrayList<String> (citations.size());
    		
    		for ( Long citationId : citations) {
    			String csid = citationIdMap.get(citationId);
    			if ( csid == null) {
    				csid = writeCitationInCX(getCitationDocById(citationId), cxwtr);
    				citationIdMap.put(citationId, csid);
    			}
    			
    			cids.add(csid);
    		}
    		if (isEdge)
    		  writeNdexAspectElementAsAspectFragment(cxwtr, new EdgeCitationLinksElement(SID, cids));
    		else
    		  writeNdexAspectElementAsAspectFragment(cxwtr, new NodeCitationLinksElement(SID, cids));	
    	}
    	
    	//writeSupports
    	
    	Collection<Long> supports = doc.field(NdexClasses.Support);
    	
    	if ( supports !=null) {
    		List<String> supIds = new ArrayList<String> (supports.size());
    		
    		for ( Long supId : supports) {
    			String ssid = supportIdMap.get(supId);
    			if ( ssid == null) {
    				ssid = writeSupportInCX(getSupportDocById(supId), cxwtr);
    				supportIdMap.put(supId, ssid);
    			}
    			
    			supIds.add(ssid);
    		}
    		if ( isEdge)
    		  writeNdexAspectElementAsAspectFragment(cxwtr, new EdgeSupportLinksElement(SID, supIds));
    		else 
      		  writeNdexAspectElementAsAspectFragment(cxwtr, new NodeSupportLinksElement(SID, supIds));

    	}
	}
	
	
	private void writeDocPropertiesAsCX(ODocument doc, CxWriter cxwtr) throws IOException {
	   	List<NdexPropertyValuePair> props = doc.field(NdexClasses.ndexProperties);
    	if ( props !=null) {
    		cxwtr.startAspectFragment(EdgeAttributesElement.NAME);
    		for ( NdexPropertyValuePair p : props ) {
    			EdgeAttributesElement ep = new EdgeAttributesElement ( null, p.getPredicateString(), p.getValue(), p.getDataType());
    			cxwtr.writeAspectElement(ep);
    		}
    		cxwtr.endAspectFragment();
    	}
	}
	
	private void writeNodeInCX(ODocument doc, CxWriter cxwtr, Set<Long> repIdSet,
			 Map<Long,String> citationIdMap,  Map<Long,String> supportIdMap) 
			throws ObjectNotFoundException, IOException {
		
		String SID = doc.field(NdexClasses.Element_SID);
		
		if ( SID ==null)  {
			Long id = doc.field(NdexClasses.Element_ID);
			SID = id.toString();
		}
		
		writeNdexAspectElementAsAspectFragment(cxwtr, new NodesElement(SID));
    	
    	//write rep 
    	
    	Long repId = doc.field(NdexClasses.Node_P_represents);
    	
    	if ( repId != null && repId.longValue() > 0) {
    		try {
    			String repStr = this.getBaseTermStringById(repId);
    			writeNdexAspectElementAsAspectFragment(cxwtr, new NodeAttributesElement(null, SID, NdexClasses.Node_P_represents, repStr));
    		} catch ( ObjectNotFoundException e1) {
    			if ( !repIdSet.contains(repId) ) {
    			  try {
    				ODocument funcDoc = this.getFunctionDocById(repId);
    				writeNdexAspectElementAsAspectFragment(cxwtr, getFunctionTermsElementFromDoc(SID, funcDoc));
    				repIdSet.add(repId);
    			  } catch (ObjectNotFoundException e2) {
    				ODocument reifiedEdgeDoc = this.getReifiedEdgeDocById(repId);
    				writeReifiedEdgeTermInCX(SID, reifiedEdgeDoc, cxwtr);
    				repIdSet.add(repId);
    			  }
    			}  
    		}
    	}
    	 

		String name = doc.field(NdexClasses.Node_P_name);
    	    	
    	if (name !=null) {
    		writeNdexAspectElementAsAspectFragment(cxwtr, new NodeAttributesElement (null, SID,NdexClasses.Node_P_name,name ));
    	}
    	
    	Set<Long> aliases = doc.field(NdexClasses.Node_P_alias);
    	
    	if ( aliases !=null) {
        	List<String> terms = new ArrayList<> (aliases.size());
        	for ( Long id : aliases) {
        		terms.add(getBaseTermStringById(id));
        	}
        	writeNdexAspectElementAsAspectFragment(cxwtr, new NodeAttributesElement(null,SID,NdexClasses.Node_P_alias,terms));
    	}
    	    	
    	Set<Long> relatedTerms = doc.field(NdexClasses.Node_P_relateTo);
      	if ( relatedTerms !=null) {
        	List<String> terms = new ArrayList<> (relatedTerms.size());
        	for ( Long id : relatedTerms) {
        		terms.add(getBaseTermStringById(id));
        	}
        	writeNdexAspectElementAsAspectFragment(cxwtr, new NodeAttributesElement(null,SID,NdexClasses.Node_P_relateTo,terms));
    	}
      	
    	// write properties
       	List<NdexPropertyValuePair> props = doc.field(NdexClasses.ndexProperties);
    	if ( props !=null) {
    		cxwtr.startAspectFragment(NodeAttributesElement.NAME);
    		for ( NdexPropertyValuePair p : props ) {
    			NodeAttributesElement ep = new NodeAttributesElement ( null, p.getPredicateString(), p.getValue(), p.getDataType());
    			cxwtr.writeAspectElement(ep);
    		}
    		cxwtr.endAspectFragment();
    	}

    	//writeCitations and supports
    	writeCitationsAndSupports(SID,  doc, cxwtr, citationIdMap,
 			   supportIdMap , false );
 	}
	
	
	private FunctionTermsElement getFunctionTermsElementFromDoc(String nodeId, ODocument funcDoc) throws ObjectNotFoundException {
		Long btId = funcDoc.field(NdexClasses.BaseTerm);
		String bt = this.getBaseTermStringById(btId);
	
 	   	List<Object> args = new ArrayList<>();

 	    Object f = funcDoc.field("out_"+ NdexClasses.FunctionTerm_E_paramter);

 	    if ( f == null)   {   // function without parameters.
 	    	return new FunctionTermsElement(nodeId,bt, args);
 	    }

 	    Iterable<ODocument> iterable =  ( f instanceof ODocument) ?
    		 (new OrientDBIterableSingleLink((ODocument)f) ) :  (Iterable<ODocument>)f;
	    
    	for (ODocument para : iterable) {
	    	if (para.getClassName().equals(NdexClasses.BaseTerm)) {
	    		args.add(getBaseTermStringFromDoc(para));
	    	} else {  // add nested functionTerm
	    		FunctionTermsElement func = getFunctionTermsElementFromDoc ( null, para);
	    		args.add(func);
	    	}
	    }
	    return new FunctionTermsElement(nodeId, bt, args);
	}
	
	private void writeReifiedEdgeTermInCX(String nodeId, ODocument reifiedEdgeDoc, CxWriter cxwtr) throws ObjectNotFoundException, IOException {
		ODocument e = reifiedEdgeDoc.field("out_" + NdexClasses.ReifiedEdge_E_edge);
		String eid = e.field(NdexClasses.Element_SID);
		if ( eid == null) {
			eid = ((Long)e.field(NdexClasses.Element_ID)).toString();
		}
			
		writeNdexAspectElementAsAspectFragment(cxwtr, new ReifiedEdgeElement(nodeId, eid));
			
	}
		
	private String writeCitationInCX(ODocument doc, CxWriter cxwtr) throws ObjectNotFoundException, IOException {
	
	    CitationElement result = new CitationElement();
		
		Long citationID = doc.field(NdexClasses.Element_ID);

  	    String SID = doc.field(NdexClasses.Element_SID); 
		
		if ( SID ==null) {
			 SID = citationID.toString();
		} 
		result.setId(SID);
		result.setTitle((String)doc.field(NdexClasses.Citation_P_title));
		result.setCitationType((String)doc.field(NdexClasses.Citation_p_idType));
		result.setIdentifier((String)doc.field(NdexClasses.Citation_P_identifier));
		
		List<String> o = doc.field(NdexClasses.Citation_P_contributors);
		
		if ( o!=null && !o.isEmpty())
			result.setContributor(o);
		
	   	List<NdexPropertyValuePair> props = doc.field(NdexClasses.ndexProperties);
	   	
	   	result.setProps(props);
		
		writeNdexAspectElementAsAspectFragment(cxwtr,result);
	  	
    	return SID;
	}

	
	private String writeSupportInCX(ODocument doc, CxWriter cxwtr) throws ObjectNotFoundException, IOException {
		
		SupportElement result = new SupportElement();
		
		Long supportID = doc.field(NdexClasses.Element_ID);

 	    String SID = doc.field(NdexClasses.Element_SID); 
		
		if ( SID ==null) {
			 SID = supportID.toString();
		} 
		
		result.setId(SID);
		result.setText((String)doc.field(NdexClasses.Support_P_text));
		
		Long citationId = doc.field(NdexClasses.Citation);
		
		if ( citationId !=null) {
			ODocument cDoc = this.getCitationDocById(citationId);
			String cId = cDoc.field(NdexClasses.Element_SID);
			if ( cId == null)
				cId = ((Long)cDoc.field(NdexClasses.Element_ID)).toString();
			result.setCitationId(cId);
		}

	   	List<NdexPropertyValuePair> props = doc.field(NdexClasses.ndexProperties);

		result.setProps(props);
		
		writeNdexAspectElementAsAspectFragment(cxwtr,result);
		
    	// write properties
    //	writeDocPropertiesAsCX(doc, cxwtr);
    	return SID;
	}



	@Override
	public void close() throws Exception {
		db.commit();
		db.close();
	}
	
    
    protected static void getPropertiesFromDoc(ODocument doc, PropertiedObject obj) {
    	List<NdexPropertyValuePair> props = doc.field(NdexClasses.ndexProperties);
    	if (props != null && props.size()> 0) 
    		obj.setProperties(props);
    }

    private ODocument getBasetermDocById (long id) throws ObjectNotFoundException {
    	ORecordId rid =   (ORecordId)btermIdIdx.get( id ); 
        
    	if ( rid != null) {
    		return rid.getRecord();
    	}
  
    	throw new ObjectNotFoundException(NdexClasses.BaseTerm, id);
    }
    
    private ODocument getFunctionDocById (long id) throws ObjectNotFoundException {
    	ORecordId rid =   (ORecordId)funcIdIdx.get( id ); 
        
    	if ( rid != null) {
    		return rid.getRecord();
    	}
  
    	throw new ObjectNotFoundException(NdexClasses.FunctionTerm, id);
    }
    
    private ODocument getReifiedEdgeDocById (long id) throws ObjectNotFoundException {
    	ORecordId rid =   (ORecordId)reifiedEdgeIdIdx.get( id ); 
        
    	if ( rid != null) {
    		return rid.getRecord();
    	}
  
    	throw new ObjectNotFoundException(NdexClasses.ReifiedEdgeTerm, id);
    }
    
    private ODocument getCitationDocById (long id) throws ObjectNotFoundException {
    	ORecordId rid =   (ORecordId)citationIdIdx.get( id ); 
        
    	if ( rid != null) {
    		return rid.getRecord();
    	}
  
    	throw new ObjectNotFoundException(NdexClasses.Citation, id);
    }
    
    private ODocument getSupportDocById (long id) throws ObjectNotFoundException {
    	ORecordId rid =   (ORecordId)supportIdIdx.get( id ); 
        
    	if ( rid != null) {
    		return rid.getRecord();
    	}
  
    	throw new ObjectNotFoundException(NdexClasses.Citation, id);
    }
    
    
    private String getBaseTermStringById(long id) throws ObjectNotFoundException {
    	ODocument doc = getBasetermDocById(id);
    	return  getBaseTermStringFromDoc(doc);
    	
    }
    
    private String getBaseTermStringFromDoc(ODocument doc) throws ObjectNotFoundException {
	    String name = doc.field(NdexClasses.BTerm_P_name);
    	
    	Long nsId = doc.field(NdexClasses.BTerm_NS_ID); 
    	if ( nsId == null || nsId.longValue() <= 0) 
    		return name;
    	
    	ODocument nsdoc = getNamespaceDocById(nsId);
    	String prefix = nsdoc.field(NdexClasses.ns_P_prefix)	;
    	if ( prefix!=null)
    		return prefix + ":"+ name;
    	
    	return nsdoc.field(NdexClasses.ns_P_uri) + name;
    }
    
    private ODocument getNamespaceDocById(long id) throws ObjectNotFoundException {
    	ORecordId cIds =  (ORecordId) nsIdIdx.get( id ); 

    	if ( cIds !=null)
    		return cIds.getRecord();
    	
    	throw new ObjectNotFoundException(NdexClasses.Namespace, id);
    }
    
    private void writeNdexAspectElementAsAspectFragment (CxWriter cxwtr, AspectElement element ) throws IOException {
    	cxwtr.startAspectFragment(element.getAspectName());
		cxwtr.writeAspectElement(element);
		cxwtr.endAspectFragment();
    }
}
