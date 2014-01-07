package org.ndexbio.common.models.data;

import com.tinkerpop.frames.Adjacency;
import com.tinkerpop.frames.Property;

public interface ISupport extends IMetadataObject
{
    @Property("jdexId")
    public void setJdexId(String jdexId);

    @Property("jdexId")
    public String getJdexId();

    @Property("text")
    public void setText(String text);

    @Property("text")
    public String getText();

    @Adjacency(label = "supportCitation")
    public void setSupportCitation(ICitation citation);

    @Adjacency(label = "supportCitation")
    public ICitation getSupportCitation();
    
    @Adjacency(label = "supportEdges")
    public void addNdexEdge(IEdge edge);

    @Adjacency(label = "supportEdges")
    public Iterable<IEdge> getNdexEdges();
    
    @Adjacency(label = "supportEdges")
    public void removeNdexEdge(IEdge edge);
}
