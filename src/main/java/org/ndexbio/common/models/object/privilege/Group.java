package org.ndexbio.common.models.object.privilege;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import org.ndexbio.common.models.data.IGroup;
import org.ndexbio.common.models.data.IGroupMembership;
import org.ndexbio.common.models.data.INetworkMembership;
import org.ndexbio.common.models.data.IRequest;
import org.ndexbio.common.models.data.IUser;
import org.ndexbio.common.models.object.Request;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Group extends Account
{
    private String _name;
    private List<Membership> _members;
    private String _organizationName;
    private List<Membership> _networkMemberships;
    private List<Request> _requests;

    
    
    /**************************************************************************
    * Default constructor.
    **************************************************************************/
    public Group()
    {
        super();

        initCollections();
    }
    
    /**************************************************************************
    * Populates the class (from the database) and removes circular references.
    * Doesn't load owned Networks.
    * 
    * @param group The Group with source data.
    **************************************************************************/
    public Group(IGroup group)
    {
        this(group, false);
    }
    
    /**************************************************************************
    * Populates the class (from the database) and removes circular references.
    * 
    * @param group The Group with source data.
    * @param loadEverything True to load owned Networks, false to exclude
    *                       them.
    **************************************************************************/
    public Group(IGroup group, boolean loadEverything)
    {
        super(group);
        
        initCollections();

        this.setCreatedDate(group.getCreatedDate());
        
        _name = group.getName();
        _organizationName = group.getOrganizationName();

        if (loadEverything)
        {
            for (final IRequest request : group.getRequests())
                _requests.add(new Request(request));
            
            for (final IGroupMembership member : group.getMembers())
                _members.add(new Membership((IUser)member.getMember(), member.getPermissions()));
            
            for (final INetworkMembership network : group.getNetworks())
                _networkMemberships.add(new Membership(network.getNetwork(), network.getPermissions()));
        }
    }
    
    
    
    public List<Membership> getMembers()
    {
        return _members;
    }
    
    public void setMembers(List<Membership> members)
    {
        _members = members;
    }

    public String getName()
    {
        return _name;
    }
    
    public void setName(String name)
    {
        _name = name;
    }

    public List<Membership> getNetworks()
    {
        return _networkMemberships;
    }

    public void setNetworks(List<Membership> networkMemberships)
    {
        _networkMemberships = networkMemberships;
    }
    
    public String getOrganizationName()
    {
        return _organizationName;
    }
    
    public void setOrganizationName(String organizationName)
    {
        _organizationName = organizationName;
    }
    
    public List<Request> getRequests()
    {
        return _requests;
    }
    
    public void setRequests(List<Request> requests)
    {
        _requests = requests;
    }

    

    /**************************************************************************
    * Initializes the collections. 
    **************************************************************************/
    private void initCollections()
    {
        _members = new ArrayList<Membership>();
        _networkMemberships = new ArrayList<Membership>();
        _requests = new ArrayList<Request>();
    }
}
