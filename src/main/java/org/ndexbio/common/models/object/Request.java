package org.ndexbio.common.models.object;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.ndexbio.common.helpers.IdConverter;
import org.ndexbio.common.models.data.IGroupInvitationRequest;
import org.ndexbio.common.models.data.IJoinGroupRequest;
import org.ndexbio.common.models.data.INetworkAccessRequest;
import org.ndexbio.common.models.data.IRequest;
import com.orientechnologies.orient.core.id.ORID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Request extends NdexObject
{
    private String _fromId;
    private String _fromName;
    private String _toId;
    private String _toName;
    private String _message;
    private String _requestType;
    private String _responder;
    private String _response;
    private String _responseMessage;

    

    /**************************************************************************
    * Default constructor.
    **************************************************************************/
    public Request()
    {
        super();
    }
    
    /**************************************************************************
    * Populates the class (from the database) and removes circular references.
    * 
    * @param request The Request with source data.
    **************************************************************************/
    public Request(IRequest request)
    {
        super(request);
        
        _message = request.getMessage();
        _responder = request.getResponder();
        _response = request.getResponse();
        _responseMessage = request.getResponseMessage();
        this.setCreatedDate(request.getRequestTime());

        if (request instanceof IGroupInvitationRequest)
        {
            IGroupInvitationRequest groupRequest = ((IGroupInvitationRequest)request); 
            _requestType = "Group Invitation";
            _fromId = IdConverter.toJid((ORID)groupRequest.getFromGroup().asVertex().getId());
            _fromName = groupRequest.getFromGroup().getName();
            _toId = IdConverter.toJid((ORID)groupRequest.getToUser().asVertex().getId());
            _toName = groupRequest.getToUser().getFirstName() + " " + groupRequest.getToUser().getLastName();
        }
        else if (request instanceof IJoinGroupRequest)
        {
            IJoinGroupRequest groupRequest = ((IJoinGroupRequest)request); 
            _requestType = "Join Group";
            _fromId = IdConverter.toJid((ORID)groupRequest.getFromUser().asVertex().getId());
            _fromName = groupRequest.getFromUser().getFirstName() + " " + groupRequest.getFromUser().getLastName();
            _toId = IdConverter.toJid((ORID)groupRequest.getToGroup().asVertex().getId());
            _toName = groupRequest.getToGroup().getName();
        }
        else if (request instanceof INetworkAccessRequest)
        {
            INetworkAccessRequest networkRequest = ((INetworkAccessRequest)request); 
            _requestType = "Network Access";
            _fromId = IdConverter.toJid((ORID)networkRequest.getFromUser().asVertex().getId());
            _fromName = networkRequest.getFromUser().getFirstName() + " " + networkRequest.getFromUser().getLastName();
            _toId = IdConverter.toJid((ORID)networkRequest.getToNetwork().asVertex().getId());
            _toName = networkRequest.getToNetwork().getTitle();
        }
    }

    
    
    
    public String getFrom()
    {
        return _fromName;
    }

    public void setFrom(String fromName)
    {
        _fromName = fromName;
    }

    public String getFromId()
    {
        return _fromId;
    }

    public void setFromId(String fromId)
    {
        _fromId = fromId;
    }

    public String getMessage()
    {
        return _message;
    }

    public void setMessage(String message)
    {
        _message = message;
    }

    public String getRequestType()
    {
        return _requestType;
    }

    public void setRequestType(String requestType)
    {
        _requestType = requestType;
    }
    
    public String getResponder()
    {
        return _responder;
    }
    
    public void setResponder(String responder)
    {
        _responder = responder;
    }
    
    public String getResponse()
    {
        return _response;
    }
    
    public void setResponse(String response)
    {
        _response = response;
    }
    
    public String getResponseMessage()
    {
        return _responseMessage;
    }
    
    public void setResponseMessage(String responseMessage)
    {
        _responseMessage = responseMessage;
    }

    public String getTo()
    {
        return _toName;
    }

    public void setTo(String toName)
    {
        _toName = toName;
    }

    public String getToId()
    {
        return _toId;
    }

    public void setToId(String toId)
    {
        _toId = toId;
    }
}
