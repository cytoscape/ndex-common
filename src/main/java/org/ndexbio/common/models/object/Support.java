package org.ndexbio.common.models.object;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ndexbio.common.models.data.IEdge;
import org.ndexbio.common.models.data.ISupport;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Support extends NdexObject
{
    private String _jdexId;
    private String _text;
    private List<String> _edges;
    private String _citationId;



    /**************************************************************************
    * Default constructor.
    **************************************************************************/
    public Support()
    {
        super();
    }

    /**************************************************************************
    * Populates the class (from the database) and removes circular references.
    * 
    * @param support The Support with source data.
    **************************************************************************/
    public Support(ISupport support)
    {
        super(support);

        _jdexId = support.getJdexId();
        _text = support.getText();
        _citationId = support.getSupportCitation().getJdexId();
        _edges = new ArrayList<String>();
        
        for (final IEdge iEdge : support.getNdexEdges())
            _edges.add(iEdge.getJdexId());
    }

    public String getJdexId()
    {
        return _jdexId;
    }

    public void setJdexId(String jdexId)
    {
        _jdexId = jdexId;
    }

    public String getText()
    {
        return _text;
    }

    public void setText(String text)
    {
        _text = text;
    }

    public List<String> getEdges()
    {
        return _edges;
    }

    public void set_networks(List<String> edges)
    {
        _edges = edges;
    }

    public String getCitationId()
    {
        return _citationId;
    }

    public void setCitationId(String citationId)
    {
        _citationId = citationId;
    }
}
