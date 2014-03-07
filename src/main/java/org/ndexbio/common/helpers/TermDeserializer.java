package org.ndexbio.common.helpers;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.ndexbio.common.models.object.BaseTerm;
import org.ndexbio.common.models.object.FunctionTerm;
import org.ndexbio.common.models.object.Namespace;
import org.ndexbio.common.models.object.ReifiedEdgeTerm;
import org.ndexbio.common.models.object.Term;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class TermDeserializer extends JsonDeserializer<Term>
{
    public TermDeserializer()
    {
        super();
    }
    
    
    
    @Override
    public Term deserialize(JsonParser jsonParser, DeserializationContext context) throws IOException, JsonProcessingException
    {
        final ObjectMapper jsonMapper = new ObjectMapper();
        final JsonNode serializedTerm = jsonMapper.readTree(jsonParser);
        final JsonNode termType = serializedTerm.get("termType");
        
        if (termType != null)
        {
            if (termType.asText().equals("Base"))
                return populateBaseTerm(serializedTerm);
            else if (termType.asText().equals("Function"))
                return populateFunctionTerm(serializedTerm);
            else if (termType.asText().equals("ReifiedEdgeTerm"))
                return populateReifiedEdgeTerm(serializedTerm);
        }
        else
        {
            final JsonNode nameProperty = serializedTerm.get("name");
            if (nameProperty != null)
                return populateBaseTerm(serializedTerm);
            
            final JsonNode functionProperty = serializedTerm.get("termFunction");
            if (functionProperty != null)
                return populateFunctionTerm(serializedTerm);
        }
        
        throw context.mappingException("Unsupported term type.");
    }
    
    
    
    private Term populateReifiedEdgeTerm(JsonNode serializedTerm) {
        final ReifiedEdgeTerm reifiedEdgeTerm = new ReifiedEdgeTerm();
        
        reifiedEdgeTerm.setTermEdge(serializedTerm.get("termEdge").asText());
                
        return reifiedEdgeTerm;
	}



	private BaseTerm populateBaseTerm(JsonNode serializedTerm)
    {
        final BaseTerm baseTerm = new BaseTerm();
        baseTerm.setName(serializedTerm.get("name").asText());
        
        if (serializedTerm.get("namespace") != null)
        {        
            baseTerm.setNamespace(serializedTerm.get("namespace").asText());
        }
        
        return baseTerm;
    }
    
    private FunctionTerm populateFunctionTerm(JsonNode serializedTerm)
    {
        final FunctionTerm functionTerm = new FunctionTerm();
        functionTerm.setTermFunction(serializedTerm.get("termFunction").asText());
        final Map<String, String> parameters = functionTerm.getParameters();
        Iterator<Entry<String, JsonNode>> fieldIterator = serializedTerm.get("parameters").fields();

        while (fieldIterator.hasNext()){
        	Entry<String, JsonNode> param = fieldIterator.next();
        	parameters.put(param.getKey(), param.getValue().asText());
        }
        functionTerm.setParameters(parameters);
        
        return functionTerm;
    }

}
