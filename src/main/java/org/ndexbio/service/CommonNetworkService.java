package org.ndexbio.service;

import java.util.HashMap;

import org.ndexbio.common.exceptions.NdexException;
import org.ndexbio.common.models.data.*;
import org.ndexbio.common.models.object.SearchParameters;
import org.ndexbio.common.models.object.SearchResult;
import org.ndexbio.common.persistence.NDExPersistenceService;
import org.ndexbio.common.persistence.NDExPersistenceServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

// TODO: THIS DOES NOT BELONG HERE. THIS CLASS IS APPLICATION-SPECIFIC TO THE SERVICE
//       AND THEREFORE SHOULD RESIDE IN THAT PACKAGE. COMMON SHOULD ONLY CONTAIN
//       CLASSES AND FUNCTIONALITY THAT ARE COMMON TO MULTIPLE PROJECTS


/*
 * abstract class containing functionality common to all network service classes
 */

public abstract class CommonNetworkService {

	protected NDExPersistenceService persistenceService;
	protected final Logger logger;

	protected CommonNetworkService() {
		this.persistenceService = NDExPersistenceServiceFactory.INSTANCE
				.getNDExPersistenceService();
		logger = LoggerFactory.getLogger(this.getClass());
		logger.info("connection service instantiated");
	}
	
	/*
	 * A consistent method for creating a network, registering ownership, and
	 * assigning it a title
	 * throws NdexException if supplied owner name is not in the database
	 */
	public final INetwork createNewNetwork(String ownerName, String networkTitle) throws NdexException {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(ownerName),
				"A network owner name is required");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(networkTitle),
				"A network title is required");
		INetwork network = this.persistenceService.getCurrentNetwork();
		network.setMetadata(new HashMap<String, String>());
		network.setMetaterms(new HashMap<String, IBaseTerm>());
		// find the network owner in the database
		IUser networkOwner = resolveUserUserByUsername(ownerName);
		if( null == networkOwner){
			logger.error("User " +ownerName +" is not registered in the database/");
			throw new NdexException("User " +ownerName +" is not registered in the database");
		}
				
		INetworkMembership membership = createNewMember();
		membership.setMember(networkOwner);
		membership.setPermissions(Permissions.ADMIN);
		network.setIsPublic(true);
		network.setIsLocked(false);
		//networkOwner.addNetwork(membership);
		network.addMember(membership);
		network.setName(networkTitle);
		logger.info("A new NDex network titled: " +network.getName()
				+" owned by " +ownerName +" has been created");
		return network;
	}

	protected final IUser resolveUserUserByUsername(String userName) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(userName),
				"A username is required");
		SearchParameters searchParameters = new SearchParameters();
		searchParameters.setSearchString(userName);
		searchParameters.setSkip(0);
		searchParameters.setTop(1);

		try {
			SearchResult<IUser> result = findUsers(searchParameters);
			return (IUser) result.getResults().iterator().next();

		} catch (NdexException e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		
		return null;
	}

	public final SearchResult<IUser> findUsers(SearchParameters searchParameters)
			throws NdexException {
		return this.persistenceService.findUsers(searchParameters);
	}	
	
	public final INetwork getCurrentNetwork() {
		return this.persistenceService.getCurrentNetwork();
	}

	public final IUser createNewUser(String username) {
		Preconditions.checkArgument(!Strings.isNullOrEmpty(username));
		IUser user = this.persistenceService.getCurrentUser();
		user.setUsername(username);
		return user;
	}

	public final INetworkMembership createNewMember() {
		return this.persistenceService.createNetworkMembership();
	}

	public final void persistNewNetwork() {
		this.persistenceService.persistNetwork();
	}

	public final void rollbackCurrentTransaction() {
		this.persistenceService.abortTransaction();
	}
	
	/*
	 * expose persistence service to subclasses to provide access to database 
	 * operations defined in service interface
	 */
	protected final NDExPersistenceService getPersistenceService() {
		return this.persistenceService;
	}
	
	public void setFormat(String format) {
	    if (persistenceService.getCurrentNetwork().getMetadata() != null)
	        persistenceService.getCurrentNetwork().getMetadata().put("Format", format);	
	}
	
}
