package org.ndexbio.common.cx.aspect;

import java.util.Collection;

import org.cxio.core.interfaces.AspectElement;
import org.ndexbio.model.cx.CXSupport;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)


public class CitationElement implements AspectElement {
    
	final public static String NAME           = "Citations";
	
	private String title;
	private Collection<String> contributor;
	private String identifier;
	private String citationType;
	
	private String description;
	private Collection<String> edges;
	private Collection<String> nodes;
	private Collection<CXSupport> supports;
	
	
	public CitationElement() {
	}

	@Override
	@JsonIgnore
	public  String getAspectName() {
		return NAME;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public Collection<String> getContributor() {
		return contributor;
	}

	public void setContributor(Collection<String> contributor) {
		this.contributor = contributor;
	}

	public String getIdentifier() {
		return identifier;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}

	public String getCitationType() {
		return citationType;
	}

	public void setCitationType(String citationType) {
		this.citationType = citationType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Collection<String> getEdges() {
		return edges;
	}

	public void setEdges(Collection<String> edges) {
		this.edges = edges;
	}

	public Collection<String> getNodes() {
		return nodes;
	}

	public void setNodes(Collection<String> nodes) {
		this.nodes = nodes;
	}

	public Collection<CXSupport> getSupports() {
		return supports;
	}

	public void setSupports(Collection<CXSupport> supports) {
		this.supports = supports;
	}

	@Override
	public long getSum() {
		// TODO Auto-generated method stub
		return 0;
	}

}
