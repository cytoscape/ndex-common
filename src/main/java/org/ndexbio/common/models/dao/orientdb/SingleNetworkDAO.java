package org.ndexbio.common.models.dao.orientdb;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import org.ndexbio.model.cx.CXSupport;
import org.ndexbio.model.cx.CitationElement;
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
    
	public SingleNetworkDAO ( String UUID) throws NdexException {
		db  = NdexDatabase.getInstance().getAConnection();	
		networkDoc = getRecordByUUIDStr(UUID);
    	btermIdIdx = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_bterm_id);
    	nsIdIdx = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_ns_id);

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
        cxwtr.start();
        
        Map<Long,String> citationIdMap = new TreeMap<> ();
        Map<Long,String> supportIdMap = new TreeMap<> ();
        
        List<AspectElement> aspect_elements = new ArrayList<AspectElement>(1);
        
        for ( ODocument doc : getNetworkElements(NdexClasses.Network_E_Edges)) {
        	writeEdgeInCX(doc,cxwtr, aspect_elements,citationIdMap, supportIdMap);
        }
        
        for ( ODocument doc : getNetworkElements(NdexClasses.Network_E_Nodes)) {
        	writeNodeInCX(doc, cxwtr, aspect_elements);
        }
        
        for ( CitationElement ci : getCXCitations()) {
        	aspect_elements.add(ci);
        	cxwtr.writeAspectElements(aspect_elements);
        	aspect_elements.remove(0);
        }
        
        cxwtr.end();

	}
	
	private void writeEdgeInCX(ODocument doc, CxWriter cxwtr, List<AspectElement> aspect_elements, Map<Long,String> citationIdMap,
			    Map<Long,String> supportIdMap) throws ObjectNotFoundException, IOException {
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
	  	aspect_elements.add(e);
    	cxwtr.writeAspectElements(aspect_elements);
    	aspect_elements.remove(0);
    	
    	// write properties
    	writeDocPropertiesAsCX(doc, cxwtr);

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
	
	private void writeNodeInCX(ODocument doc, CxWriter cxwtr, List<AspectElement> aspect_elements) throws ObjectNotFoundException, IOException {
		
		String SID = doc.field(NdexClasses.Element_SID);
		
		if ( SID ==null)  {
			Long id = doc.field(NdexClasses.Element_ID);
			SID = id.toString();
		}
		
		NodesElement e = new NodesElement(SID);
	  	aspect_elements.add(e);
    	cxwtr.writeAspectElements(aspect_elements);
    	aspect_elements.remove(0);
    	
    	// write properties
       	List<NdexPropertyValuePair> props = doc.field(NdexClasses.ndexProperties);
    	if ( props !=null) {
    		cxwtr.startAspectFragment(EdgeAttributesElement.NAME);
    		for ( NdexPropertyValuePair p : props ) {
    			NodeAttributesElement ep = new NodeAttributesElement ( null, p.getPredicateString(), p.getValue(), p.getDataType());
    			cxwtr.writeAspectElement(ep);
    		}
    		cxwtr.endAspectFragment();
    	}

	}
	
	private void writeCitationInCX(ODocument doc, CxWriter cxwtr, List<AspectElement> aspect_elements) throws ObjectNotFoundException, IOException {
	
		CitationElement result = new CitationElement();
		
		
		Long citationID = doc.field(NdexClasses.Element_ID);

	/*	String SID = doc.field(NdexClasses.Element_SID);   -- only useful if citations has ids.
		
		if ( SID ==null) {
			 SID = citationId.toString();
		} */

		result.setTitle((String)doc.field(NdexClasses.Citation_P_title));
		result.setCitationType((String)doc.field(NdexClasses.Citation_p_idType));
		result.setIdentifier((String)doc.field(NdexClasses.Citation_P_identifier));
		
		List<String> o = doc.field(NdexClasses.Citation_P_contributors);
		
		if ( o!=null && !o.isEmpty())
			result.setContributor(o);
		
    /*	List<NdexPropertyValuePair> props = doc.field(NdexClasses.ndexProperties);
    	if ( props !=null && props.size() > 0 )
    		result.setProperties(props);
*/
		// get the supports
		
		OIndex<?> citationIdx = db.getMetadata().getIndexManager().getIndex(NdexClasses.Index_support_citation);
		Collection<OIdentifiable> cIds =  (Collection<OIdentifiable>) citationIdx.get( citationID ); // account to traverse by
		
		if ( !cIds.isEmpty()) {
			Collection<CXSupport> ss = new ArrayList<> (cIds.size());
			for ( OIdentifiable od : cIds ) {
				ODocument sDoc = od.getRecord();
				CXSupport s = new CXSupport();
				s.setText((String)sDoc.field(NdexClasses.Support_P_text));
				ss.add(s);
			}
			result.setSupports(ss);
		}
		
	  	aspect_elements.add(result);
    	cxwtr.writeAspectElements(aspect_elements);
    	aspect_elements.remove(0);
    	
    	// write properties
    	writeDocPropertiesAsCX(doc, cxwtr);

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
    
    private String getBaseTermStringById(long id) throws ObjectNotFoundException {
    	ODocument doc = getBasetermDocById(id);
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
}