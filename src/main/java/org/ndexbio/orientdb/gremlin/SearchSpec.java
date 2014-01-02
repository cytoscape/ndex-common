package org.ndexbio.orientdb.gremlin;

import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.id.ORID;
import org.ndexbio.common.helpers.IdConverter;
import org.ndexbio.common.models.object.NetworkQueryParameters;

/**
 * @author Andrey Lomakin <a href="mailto:lomakin.andrey@gmail.com">Andrey
 *         Lomakin</a>
 * @since 11/14/13
 */
public class SearchSpec
{
    private OIdentifiable[] startingTerms;
    private String[] startingTermStrings;
    private RepresentationCriteria representationCriterion;
    private SearchType searchType;
    private OIdentifiable[] includedPredicates;
    private OIdentifiable[] excludedPredicates;
    private int searchDepth;

    public SearchSpec(NetworkQueryParameters parameters)
    {
        super();

        startingTermStrings = new String[parameters.getStartingTermStrings().size()];
        for (int index = 0; index < parameters.getStartingTermStrings().size(); index++)
            startingTermStrings[index] = parameters.getStartingTermStrings().get(index);

        representationCriterion = RepresentationCriteria.valueOf(parameters.getRepresentationCriterion());

        searchType = SearchType.valueOf(parameters.getSearchType());

        startingTerms = new OIdentifiable[parameters.getStartingTermIds().size()];
        for (int index = 0; index < parameters.getStartingTermIds().size(); index++)
        {
            String jid = parameters.getStartingTermIds().get(index);
            ORID rid = IdConverter.toRid(jid);
            startingTerms[index] = rid;
        }
        
        searchDepth = parameters.getSearchDepth();
    }

    
    
    public int getSearchDepth()
    {
        return searchDepth;
    }

    public void setSearchDepth(int searchDepth)
    {
        this.searchDepth = searchDepth;
    }

    public OIdentifiable[] getStartingTerms()
    {
        return startingTerms;
    }

    public void setStartingTerms(OIdentifiable[] startingTerms)
    {
        this.startingTerms = startingTerms;
    }

    public String[] getStartingTermStrings()
    {
        return startingTermStrings;
    }

    public void setStartingTermStrings(String[] startingTermStrings)
    {
        this.startingTermStrings = startingTermStrings;
    }

    public RepresentationCriteria getRepresentationCriterion()
    {
        return representationCriterion;
    }

    public void setRepresentationCriterion(RepresentationCriteria representationCriterion)
    {
        this.representationCriterion = representationCriterion;
    }

    public SearchType getSearchType()
    {
        return searchType;
    }

    public void setSearchType(SearchType searchType)
    {
        this.searchType = searchType;
    }

    public OIdentifiable[] getIncludedPredicates()
    {
        return includedPredicates;
    }

    public void setIncludedPredicates(OIdentifiable[] includedPredicates)
    {
        this.includedPredicates = includedPredicates;
    }

    public OIdentifiable[] getExcludedPredicates()
    {
        return excludedPredicates;
    }

    public void setExcludedPredicates(OIdentifiable[] excludedPredicates)
    {
        this.excludedPredicates = excludedPredicates;
    }
}
