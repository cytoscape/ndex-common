package org.ndexbio.common.models.dao;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.orientechnologies.orient.core.id.ORID;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;
import org.ndexbio.common.exceptions.DuplicateObjectException;
import org.ndexbio.common.exceptions.NdexException;
import org.ndexbio.common.exceptions.ObjectNotFoundException;
import org.ndexbio.common.models.object.NetworkQueryParameters;
import org.ndexbio.model.object.Membership;
import org.ndexbio.model.object.Permissions;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestNetworkDAO 

{
 //   private static final NetworkDAO dao = DAOFactorySupplier.INSTANCE.resolveDAOFactoryByType(CommonDAOValues.ORIENTDB_DAO_TYPE)
 //			.get().getNetworkDAO();
    
    
 /*   //@Test
    public void autoSuggest()
    {
        try
        {
            final ORID testNetworkRid = getRid("REACTOME.G1 Phase.485618");
            final Collection<String> suggestions = dao.autoSuggestTerms(this.testUserId,
            		IdConverter.toJid(testNetworkRid), "RBL");
            Assert.assertNotNull(suggestions);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void autoSuggestInvalidNetwork() throws IllegalArgumentException, NdexException
    {
        dao.autoSuggestTerms("","", "RBL");
    }

    @Test(expected = IllegalArgumentException.class)
    public void autoSuggestInvalidPartialTerm() throws IllegalArgumentException, NdexException
    {
        final ORID testNetworkRid = getRid("REACTOME TEST");
        dao.autoSuggestTerms(this.testUserId, IdConverter.toJid(testNetworkRid), "");
    }
*/
/*    @Test
    public void createNetwork()
    {
        Assert.assertTrue(createNewNetwork());
    }

    @Test(expected = DuplicateObjectException.class)
    public void createNetworkDuplicate() throws IllegalArgumentException, DuplicateObjectException, NdexException
    {
        Assert.assertTrue(createNewNetwork());
        
        final Network newNetwork = new Network();
        newNetwork.setDescription("This is a test network.");
        newNetwork.getMetadata().put("Copyright", "2013 Cytoscape Consortium");
        newNetwork.getMetadata().put("Format", "JDEX");
        newNetwork.setName("Test Network");
        
        dao.createNetwork(this.testUserId, newNetwork);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNetworkInvalid() throws IllegalArgumentException, DuplicateObjectException, NdexException
    {
        dao.createNetwork(this.testUserId, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void createNetworkInvalidTitle() throws IllegalArgumentException, DuplicateObjectException, NdexException
    {
        final Network newNetwork = new Network();
        newNetwork.setDescription("This is a test network.");
        newNetwork.getMetadata().put("Copyright", "2013 Cytoscape Consortium");
        newNetwork.getMetadata().put("Format", "JDEX");
        
        dao.createNetwork("",newNetwork);
    }

    @Test
    public void deleteNetwork()
    {
        Assert.assertTrue(createNewNetwork());

        final ORID testNetworkRid = getRid("Test Network");
        Assert.assertTrue(deleteTargetNetwork(IdConverter.toJid(testNetworkRid)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void deleteNetworkInvalid() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        dao.deleteNetwork(this.testUserId,null);
    }

    @Test(expected = ObjectNotFoundException.class)
    public void deleteNetworkNonexistant() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        dao.deleteNetwork(this.testUserId, "C999R999");
    }

    @Test
    public void findNetworks()
    {
        final SearchParameters searchParameters = new SearchParameters();
        searchParameters.setSearchString("reactome");
        searchParameters.setSkip(0);
        searchParameters.setTop(25);
        
        try
        {
            final List<Network> networksFound = dao.findNetworks(this.testUserId,
            		searchParameters, "starts-with");
            Assert.assertTrue(networksFound.size() > 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void findNetworksByMetadata()
    {
        final SearchParameters searchParameters = new SearchParameters();
        searchParameters.setSearchString("[company]=\"Cytoscape Consortium\"");
        searchParameters.setSkip(0);
        searchParameters.setTop(25);
        
        try
        {
            final List<Network> networksFound = dao.findNetworks(this.testUserId,
            		searchParameters, "starts-with");
            Assert.assertTrue(networksFound.size() == 2);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    public void findNetworksByTerm()
    {
        final SearchParameters searchParameters = new SearchParameters();
        searchParameters.setSearchString("term:{bel:pathology}");
        searchParameters.setSkip(0);
        searchParameters.setTop(25);
        
        try
        {
            final List<Network> networksFound = dao.findNetworks(this.testUserId, 
            		searchParameters, "starts-with");
            Assert.assertTrue(networksFound.size() > 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void findNetworksInvalid() throws IllegalArgumentException, NdexException
    {
        dao.findNetworks(null, null, null);
    }

    @Test
    public void findNetworksUsingContains()
    {
        final SearchParameters searchParameters = new SearchParameters();
        searchParameters.setSearchString("tome");
        searchParameters.setSkip(0);
        searchParameters.setTop(25);
        
        try
        {
            final List<Network> networksFound = dao.findNetworks(this.testUserId, searchParameters, "contains");
            Assert.assertTrue(networksFound.size() > 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void findNetworksUsingExactMatch()
    {
        final SearchParameters searchParameters = new SearchParameters();
        searchParameters.setSearchString("reactome test");
        searchParameters.setSkip(0);
        searchParameters.setTop(25);
        
        try
        {
            final List<Network> networksFound = dao.findNetworks(this.testUserId, 
            		searchParameters, "exact-match");
            Assert.assertTrue(networksFound.size() > 0);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void getNetwork()
    {
        try
        {
            final ORID networkRid = getRid("NCI_NATURE:FoxO family signaling");
            final Network testNetwork = dao.getNetwork(this.testUserId,
            		IdConverter.toJid(networkRid));
            Assert.assertNotNull(testNetwork);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getNetworkInvalid() throws IllegalArgumentException, NdexException
    {
        dao.getNetwork("","");
    }

    @Test
    public void getEdges()
    {
        try
        {
            final ORID networkRid = getRid("NCI_NATURE:FoxO family signaling");
            dao.getEdges(this.testUserId, IdConverter.toJid( networkRid), 0, 25);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Test
    public void getIntersectingTerms() throws IllegalArgumentException, NdexException
    {
        final ORID networkRid = getRid("NCI_NATURE:FoxO family signaling");
        final Iterable<BaseTerm> intersectingTerms = dao.getIntersectingTerms(this.testUserId, 
        		IdConverter.toJid(networkRid), new String[] { "GDP", "GTP", "RAL", "RFP" });
        
        int termCount = 0;
        final Iterator<BaseTerm> termIterator = intersectingTerms.iterator();
        while (termIterator.hasNext())
        {
            termCount++;
            termIterator.next();
        }
        
        Assert.assertEquals(3, termCount);
    }
    
    @Test
    public void getTermsInNamespace() throws IllegalArgumentException, NdexException
    {
        final ORID networkRid = getRid("Glucocorticoid_receptor_regulatory_network");
        final Iterable<BaseTerm> namespaceTerms = dao.getTermsInNamespaces(
        		this.testUserId,
        		IdConverter.toJid(networkRid), 
        		new String[] { "HGNC" });
        
        int termCount = 0;
        final Iterator<BaseTerm> termIterator = namespaceTerms.iterator();
        while (termIterator.hasNext())
        {
            termCount++;
            termIterator.next();
        }
        
        Assert.assertEquals(72, termCount);
    }
    
    @Test
    public void getNamespaces()
    {
        try
        {
            final ORID networkRid = getRid("NCI_NATURE:FoxO family signaling");
            dao.getNamespaces(IdConverter.toJid(networkRid), 0, 25);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test
    public void getTerms()
    {
        try
        {
            final ORID networkRid = getRid("NCI_NATURE:FoxO family signaling");
            dao.getTerms(IdConverter.toJid(networkRid), 0, 25);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void getEdgesInvalid() throws IllegalArgumentException, NdexException
    {
        dao.getEdges(this.testUserId, "", 0, 25);
    }

    @Test
    public void queryNetwork()
    {
        try
        {
            final ORID networkRid = getRid("NCI_NATURE:FoxO family signaling");
            
            final NetworkQueryParameters queryParameters = new NetworkQueryParameters();
            queryParameters.getStartingTermStrings().add("RBL_HUMAN");
            
            dao.queryNetwork(IdConverter.toJid(networkRid), queryParameters);
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void queryNetworkInvalid() throws IllegalArgumentException, NdexException
    {
        dao.queryNetwork("", null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void removeMemberInvalidNetwork() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID userId = getRid("dexterpratt");

        dao.removeMember("", IdConverter.toJid(userId));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void removeMemberInvalidUserId() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testGroupRid = getRid("triptychjs");

        dao.removeMember(IdConverter.toJid(testGroupRid), "");
    }
    
    @Test(expected = ObjectNotFoundException.class)
    public void removeMemberNonexistantNetwork() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID userId = getRid("dexterpratt");

        dao.removeMember("C999R999", IdConverter.toJid(userId));
    }
    
    @Test(expected = ObjectNotFoundException.class)
    public void removeMemberNonexistantUser() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testGroupRid = getRid("triptychjs");

        dao.removeMember(IdConverter.toJid(testGroupRid), "C999R999");
    }
    
    @Test(expected = SecurityException.class)
    public void removeMemberOnlyAdminMember() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testGroupRid = getRid("triptychjs");
        final ORID userId = getRid("dexterpratt");

        dao.removeMember(IdConverter.toJid(testGroupRid), IdConverter.toJid(userId));
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void updateMemberInvalidNetwork() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testUserId = getRid("dexterpratt");
        
        final Membership testMembership = new Membership();
        testMembership.setPermissions(Permissions.READ);
        testMembership.setResourceId(IdConverter.toJid(testUserId));
        testMembership.setResourceName("dexterpratt");

        dao.updateMember(this.testUserId,"", testMembership);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void updateMemberInvalidMembership() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testGroupRid = getRid("triptychjs");
        
        dao.updateMember(this.testUserId, IdConverter.toJid(testGroupRid), null);
    }
    
    @Test(expected = IllegalArgumentException.class)
    public void updateMemberInvalidUserId() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testGroupRid = getRid("triptychjs");
        
        final Membership testMembership = new Membership();
        testMembership.setPermissions(Permissions.READ);
        testMembership.setResourceId("C999R999");
        testMembership.setResourceName("dexterpratt");

        dao.updateMember("", IdConverter.toJid(testGroupRid), testMembership);
    }
    
    @Test(expected = ObjectNotFoundException.class)
    public void updateMemberNonexistantNetwork() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testUserId = getRid("dexterpratt");
        
        final Membership testMembership = new Membership();
        testMembership.setPermissions(Permissions.READ);
        testMembership.setResourceId(IdConverter.toJid(testUserId));
        testMembership.setResourceName("dexterpratt");

        dao.updateMember(this.testUserId, "C999R999", testMembership);
    }
    
    //@Test(expected = ObjectNotFoundException.class)
    public void updateMemberNonexistantUser() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testGroupRid = getRid("triptychjs");
        
        final Membership testMembership = new Membership();
        testMembership.setPermissions(Permissions.READ);
        testMembership.setResourceId("C999R999");
        testMembership.setResourceName("dexterpratt");

        dao.updateMember("",IdConverter.toJid(testGroupRid), testMembership);
    }
    
    //@Test(expected = SecurityException.class)
    public void updateMemberOnlyAdminMember() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        final ORID testGroupRid = getRid("triptychjs");
        final ORID testUserId = getRid("dexterpratt");
        
        final Membership testMembership = new Membership();
        testMembership.setPermissions(Permissions.READ);
        testMembership.setResourceId(IdConverter.toJid(testUserId));
        testMembership.setResourceName("dexterpratt");

        dao.updateMember(this.testUserId,IdConverter.toJid(testGroupRid), testMembership);
    }

    //@Test
    public void updateNetwork()
    {
        try
        {
            Assert.assertTrue(createNewNetwork());

            //Refresh the user or the system won't know they have access to
            //update the network
           // this.resetLoggedInUser();
           // this.setLoggedInUser();

            final ORID testNetworkRid = getRid("Test Network");
            final Network testNetwork = dao.getNetwork(this.testUserId,IdConverter.toJid(testNetworkRid));

            testNetwork.setName("Updated Test Network");
            dao.updateNetwork(this.testUserId,testNetwork);
            Assert.assertEquals(dao.getNetwork(this.testUserId, testNetwork.getId()).getName(), testNetwork.getName());

            Assert.assertTrue(deleteTargetNetwork(testNetwork.getId()));
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
    }

    //@Test(expected = IllegalArgumentException.class)
    public void updateNetworkInvalid() throws IllegalArgumentException, ObjectNotFoundException, SecurityException, NdexException
    {
        dao.updateNetwork(null,null);
    }
    
    
    
    private boolean createNewNetwork()
    {
        final Network newNetwork = new Network();
        newNetwork.setDescription("This is a test network.");
        newNetwork.setIsComplete(true);
        newNetwork.getMetadata().put("Copyright", "2013 Cytoscape Consortium");
        newNetwork.getMetadata().put("Format", "JDEX");
        newNetwork.setName("Test Network");
        
        try
        {
            final Network createdNetwork = dao.createNetwork(this.testUserId,newNetwork);
            Assert.assertNotNull(createdNetwork);
            
            return true;
        }
        catch (DuplicateObjectException doe)
        {
            return true;
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    private boolean deleteTargetNetwork(String networkId)
    {
        try
        {
            dao.deleteNetwork(this.testUserId,networkId);
            Assert.assertNull(dao.getNetwork(this.testUserId,networkId));
            
            return true;
        }
        catch (Exception e)
        {
            Assert.fail(e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    */
}
