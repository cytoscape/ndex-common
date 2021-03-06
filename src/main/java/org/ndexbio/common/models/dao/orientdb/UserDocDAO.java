/**
 * Copyright (c) 2013, 2016, The Regents of the University of California, The Cytoscape Consortium
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors
 *    may be used to endorse or promote products derived from this software
 *    without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */
package org.ndexbio.common.models.dao.orientdb;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.Date;

import org.ndexbio.common.NdexClasses;
import org.ndexbio.common.access.NdexDatabase;
import org.ndexbio.common.util.NdexUUIDFactory;
import org.ndexbio.common.util.Security;
import org.ndexbio.model.exceptions.*;
import org.ndexbio.model.object.Membership;
import org.ndexbio.model.object.MembershipType;
import org.ndexbio.model.object.Permissions;
import org.ndexbio.model.object.Request;
import org.ndexbio.model.object.ResponseType;
import org.ndexbio.model.object.Status;
import org.ndexbio.model.object.Task;
import org.ndexbio.model.object.User;
import org.ndexbio.model.object.NewUser;
import org.ndexbio.model.object.SimpleUserQuery;
import org.ndexbio.task.Configuration;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.db.record.OIdentifiable;
import com.orientechnologies.orient.core.index.OIndex;
import com.orientechnologies.orient.core.sql.query.OSQLSynchQuery;
import com.tinkerpop.blueprints.Direction;

public class UserDocDAO extends OrientdbDAO {

	private static final Logger logger = Logger.getLogger(UserDAO.class
			.getName());

	/*
	 * User operations can be achieved with Orient Document API methods. The
	 * constructor will need to accept a OrientGraph object if we wish to use
	 * the Graph API.
	 */
	/**************************************************************************
	 * UserDAO
	 * 
	 * @param db
	 *            Database instance from the Connection pool, should be opened
	 * @param graph
	 *            OrientGraph instance for Graph API operations
	 **************************************************************************/
	public UserDocDAO(ODatabaseDocumentTx dbConnection) {
		super(dbConnection);
	}
	
	public UserDocDAO() throws NdexException {
		super(NdexDatabase.getInstance().getAConnection());
	}

	/**************************************************************************
	 * Authenticates a user trying to login.
	 * 
	 * @param accountName
	 *            The accountName.
	 * @param password
	 *            The password.
	 * @throws SecurityException
	 *             Invalid accountName or password.
	 * @throws NdexException
	 *             Can't authenticate users against the database.
	 * @return The user, from NDEx Object Model.
	 **************************************************************************/
	public User authenticateUser(String accountName, String password)
			throws UnauthorizedOperationException, NdexException,ObjectNotFoundException {

		if (Strings.isNullOrEmpty(accountName)
				|| Strings.isNullOrEmpty(password))
			throw new UnauthorizedOperationException("No accountName or password entered.");

		try {
			final ODocument OAuthUser = this.getRecordByAccountName(
					accountName, NdexClasses.User);
			if (!Security.authenticateUser(password, (String)OAuthUser.field("password"))) {
				throw new UnauthorizedOperationException("Invalid accountName or password.");
			}
			return UserDocDAO.getUserFromDocument(OAuthUser);
		} catch (UnauthorizedOperationException se) {
			logger.info("Authentication failed: " + se.getMessage());
			throw se;
		} catch (ObjectNotFoundException e) {
			throw e;
		} catch (Exception e) {
			throw new NdexException(
					"Ndex internal error when authenticate user. "+e.getMessage());
		}
	}

	/**************************************************************************
	 * Create a new user
	 * 
	 * @param newUser
	 *            A User object, from the NDEx Object Model
	 * @throws NdexException
	 *             Attempting to save an ODocument to the database
	 * @throws IllegalArgumentException
	 *             The newUser does not contain proper fields
	 * @throws DuplicateObjectException
	 *             The account name and/or email already exist
	 * @returns User object, from the NDEx Object Model
	 **************************************************************************/
	public User createNewUser(NewUser newUser, String verificationCode ) throws NdexException,
			IllegalArgumentException, DuplicateObjectException {

		Preconditions.checkArgument(null != newUser,
				"A user object is required");
		Preconditions.checkArgument(
				!Strings.isNullOrEmpty(newUser.getAccountName()),
				"A accountName is required");
		
		Preconditions.checkArgument(
				!Strings.isNullOrEmpty(newUser.getPassword()),
				"A user password is required");
		Preconditions.checkArgument(
				!Strings.isNullOrEmpty(newUser.getEmailAddress()),
				"A user email address is required");

		this.checkForExistingUser(newUser);

		try {

			ODocument user = new ODocument(NdexClasses.User).
					fields("description", newUser.getDescription(),
			               "websiteURL", newUser.getWebsite(),
			               "imageURL", newUser.getImage(),
			               "emailAddress", newUser.getEmailAddress(),
			               "firstName", newUser.getFirstName(),
			               "lastName", newUser.getLastName(),
			               //, newUser.getAccountName(),
			               "password", Security.hashText(newUser.getPassword()),
			               NdexClasses.ExternalObj_ID, NdexUUIDFactory.INSTANCE.createNewNDExUUID(),
			               NdexClasses.ExternalObj_cTime, new Date(),
			               NdexClasses.ExternalObj_mTime, new Date()
			               //NdexClasses.ExternalObj_isDeleted, false
					      );

			if ( verificationCode !=null) {

//				String verCode= Security.generatePassword(10);
			
				user.fields(NdexClasses.User_verification_code, verificationCode,
							NdexClasses.ExternalObj_isDeleted, true,
							NdexClasses.account_P_oldAcctName, newUser.getAccountName());
			} else {
				user.fields(NdexClasses.ExternalObj_isDeleted, false,
						NdexClasses.account_P_accountName, newUser.getAccountName());
			}
			
			user = user.save();

			logger.info("A new user with accountName "
					+ newUser.getAccountName() + " has been created");

			User u = UserDocDAO.getUserFromDocument(user);
	//		if ( needEmailVerification)
	//			u.set
			
			return u; 
			
		} catch (Exception e) {
			logger.severe("Could not save new user to the database: " + e.getMessage());
			throw new NdexException(e.getMessage());
		}
	}

	
	public String verifyUser ( String userUUID, String verificationCode) throws ObjectNotFoundException, NdexException {
		ODocument userDoc = this.getRecordByUUIDStr(userUUID, NdexClasses.User);
		
		Boolean isDeleted = userDoc.field(NdexClasses.ExternalObj_isDeleted);
		if ( !isDeleted)
			throw new NdexException ( "User has already been verified.");
		
		String vCode = userDoc.field(NdexClasses.User_verification_code);
		
		String acc = userDoc.field(NdexClasses.account_P_oldAcctName);
		Date t = (Date)userDoc.field(NdexClasses.ExternalObj_cTime);
		
		long t2 = Calendar.getInstance().getTimeInMillis();
		boolean within = (t2 - t.getTime()) < 8 * 3600 * 1000;  // within 8 hours
		
		if ( vCode != null  && verificationCode.equals(vCode) && within) {
			userDoc.removeField(NdexClasses.account_P_oldAcctName);
			userDoc.removeField(NdexClasses.User_verification_code);
			userDoc.fields(NdexClasses.ExternalObj_isDeleted, false,
					NdexClasses.account_P_accountName, acc,
					NdexClasses.ExternalObj_mTime, t2).save();
			return acc;
		}
		
		throw new NdexException ( "Verification information not found");
		
	}

	/**************************************************************************
	 * Get a user
	 * 
	 * @param id
	 *            UUID for User
	 * @throws NdexException
	 *             Attempting to query the database
	 * @returns User object, from the NDEx Object Model
	 **************************************************************************/

	public User getUserById(UUID id) throws NdexException,
			IllegalArgumentException, ObjectNotFoundException {

		Preconditions.checkArgument(null != id, "UUID required");

		final ODocument user = this.getRecordByUUID(id, NdexClasses.User);
		return UserDocDAO.getUserFromDocument(user);

	}

	/**************************************************************************
	 * Get a user
	 * 
	 * @param accountName
	 *            accountName for User
	 * @throws NdexException
	 *             Attempting to query the database
	 * @returns User object, from the NDEx Object Model
	 **************************************************************************/
	public User getUserByAccountName(String accountName) throws NdexException,
			IllegalArgumentException, ObjectNotFoundException {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(accountName),
				"accountName required");

		final ODocument user = this.getRecordByAccountName(accountName,
				NdexClasses.User);
		return UserDocDAO.getUserFromDocument(user);

	}

	
	public String getUserUUIDByEmail(String userEmail) throws IllegalArgumentException {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(userEmail),
				"userEmail required");

		OIndex<?> Idx = this.db.getMetadata().getIndexManager().getIndex( NdexClasses.Index_userEmail );
		OIdentifiable user = (OIdentifiable) Idx.get(userEmail); 
		if(user == null)
				return null;
			
		ODocument doc = user.getRecord();
		return doc.field(NdexClasses.ExternalObj_ID);
			
	}

	
	/**************************************************************************
	 * Find users
	 * 
	 * @param id
	 *            UUID for User
	 * @param skip
	 *            amount of blocks to skip
	 * @param top
	 *            block size
	 * @throws NdexException
	 *             Attempting to query the database
	 * @returns User object, from the NDEx Object Model
	 **************************************************************************/
	public List<User> findUsers(SimpleUserQuery simpleQuery, int skip, int top)
			throws IllegalArgumentException, NdexException {
		Preconditions.checkArgument(simpleQuery != null,
				"Search parameters are required");

		String traversePermission;
		OSQLSynchQuery<ODocument> query;
		Iterable<ODocument> users;
		final List<User> foundUsers = new ArrayList<>();
		
		String searchStr = simpleQuery.getSearchString().toLowerCase();
		
		if (searchStr.equals("*") )
			searchStr = "";
		
		if (simpleQuery.getPermission() == null)
			traversePermission = "in_groupadmin, in_member";
		else
			traversePermission = "in_"
					+ simpleQuery.getPermission().name().toLowerCase();

		//StringEscapeUtils.escapeJava()
		searchStr = Helper.escapeOrientDBSQL(searchStr.toLowerCase().trim());
		final int startIndex = skip * top;

		try {

			if (!Strings.isNullOrEmpty(simpleQuery.getAccountName())) {
				ODocument nGroup = this.getRecordByAccountName(simpleQuery.getAccountName(), NdexClasses.Group);

				if (nGroup == null)
					throw new NdexException("Invalid accountName to filter by");

				String traverseRID = nGroup.getIdentity().toString();
				query = new OSQLSynchQuery<>("SELECT FROM"
						+ " (TRAVERSE "
						+ traversePermission
						+ " FROM"
						+ " "
						+ traverseRID
						+ " WHILE $depth <=1)"
						+ " WHERE @class = '"
						+ NdexClasses.User
						+ "' and ( " + NdexClasses.ExternalObj_isDeleted + "= false)"
						+ " AND (accountName.toLowerCase() LIKE '%"
						+ searchStr
						+ "%'"
						+ "  OR lastName.toLowerCase() LIKE '%"
						+ searchStr
						+ "%'"
						+ "  OR firstName.toLowerCase() LIKE '%"
						+ searchStr
						+ "%' )"
						+ " ORDER BY " + NdexClasses.ExternalObj_cTime + " DESC "
						+ " SKIP "
						+ startIndex + " LIMIT " + top);

				users = this.db.command(query).execute();

		/*		if (!users.iterator().hasNext()  && simpleQuery.getSearchString().equals("")) {

					query = new OSQLSynchQuery<>("SELECT FROM"
							+ " (TRAVERSE " + traversePermission + " FROM"
							+ " " + traverseRID + " WHILE $depth <=1)"
							+ " WHERE @class = '" + NdexClasses.User + "'"
							+ " ORDER BY " + NdexClasses.ExternalObj_cTime + " DESC " + " SKIP "
							+ startIndex + " LIMIT " + top);

					users = this.db.command(query).execute();

				} */

				for (final ODocument user : users) {
					foundUsers.add(UserDocDAO.getUserFromDocument(user));
				}
				return foundUsers;

			} 
				
			query = new OSQLSynchQuery<>("SELECT FROM "
						+ NdexClasses.User + " "
						+ "WHERE ( "+ NdexClasses.ExternalObj_isDeleted 
						+ " = false ) and ( accountName.toLowerCase() LIKE '%"
						+ searchStr + "%'"
						+ "  OR lastName.toLowerCase() LIKE '%"
						+ searchStr + "%'"
						+ "  OR firstName.toLowerCase() LIKE '%"
						+ searchStr + "%'"
						+ ")  ORDER BY " + NdexClasses.ExternalObj_cTime + " DESC " + " SKIP "
						+ startIndex + " LIMIT " + top);

			users = this.db.command(query).execute();

			for (final ODocument user : users) {
					foundUsers.add(UserDocDAO.getUserFromDocument(user));
			}
			return foundUsers;

		} catch (Exception e) {
			logger.severe("Unable to query the database");
			throw new NdexException("Failed to search for users.\n"
					+ e.getMessage());

		}

	}

	/**************************************************************************
	 * Email a new password
	 * 
	 * @param accountName
	 *            accountName for the User
	 * @throws NdexException
	 *             Attempting to query the database
	 * @throws IllegalArgumentException
	 *             accountName is required
	 * @throws ObjectNotFoundException
	 *             user with account name does not exist
	 * @returns response
	 **************************************************************************/
	public String setNewPassword(String accountName)
			throws IllegalArgumentException, ObjectNotFoundException,
			NdexException {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(accountName),
				"An accountName is required");

		try {

			ODocument userToSave = this.getRecordByAccountName(accountName,
					NdexClasses.User);

			final String newPassword = Security.generatePassword();
			final String password = Security.hashText(newPassword);
			userToSave.fields("password", password,
					NdexClasses.ExternalObj_mTime, new Date()).save();
            
			return newPassword;
			

		} catch (ObjectNotFoundException onfe) {

			throw onfe;

		} catch (Exception e) {

			throw new NdexException("Failed to recover your password: \n"
					+ e.getMessage());

		}
	}

	/**************************************************************************
	 * Change a user's password
	 * 
	 * @param id
	 *            UUID for user
	 * @param password
	 *            new password for user
	 * @throws NdexException
	 *             Attempting to access the database
	 * @throws IllegalArgumentException
	 *             new password and user id are required
	 * @throws ObjectNotFoundException
	 *             user does not exist
	 * @returns response
	 **************************************************************************/
	public void changePassword(String password, UUID id)
			throws IllegalArgumentException, NdexException,
			ObjectNotFoundException {

		Preconditions.checkNotNull(id, "A user id is required");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(password),
				"A password is required");

		ODocument user = this.getRecordByUUID(id, NdexClasses.User);

		try {
			// Remove quotes around the password
			if (password.startsWith("\"") && password.endsWith("\"") )
				password = password.substring(1, password.length() - 2);

			user.fields("password", Security.hashText(password.trim()),
					    NdexClasses.ExternalObj_mTime, new Date());

			user.save();

			logger.info("Changed password for user with UUID " + id);

		} catch (Exception e) {

			logger.severe("An error occured while saving password for user with UUID "
					+ id);
			throw new NdexException("Failed to change your password.\n"
					+ e.getMessage());

		}

	}

	/**************************************************************************
	 * Change a user's email Address
	 * 
	 * @param id
	 *            UUID for user
	 * @param emailAddress
	 *            new email address
	 * @throws NdexException
	 *             Attempting to access the database
	 * @throws IllegalArgumentException
	 *             new password and user id are required
	 * @throws ObjectNotFoundException
	 *             user does not exist
	 * @returns response
	 **************************************************************************/
/*	public Response changeEmailAddress(String emailAddress, UUID id)
			throws IllegalArgumentException, NdexException,
			ObjectNotFoundException, DuplicateObjectException {

		Preconditions.checkNotNull(id, "A user id is required");
		Preconditions.checkArgument(!Strings.isNullOrEmpty(emailAddress),
				"A password is required");

		ODocument user = this.getRecordById(id, NdexClasses.User);

		try {

			// check for unique emailAddress
			String query = "select emailAddress from " + NdexClasses.User
					+ " where emailAddress = ?";
			List<ODocument> existingUser = db.command(new OCommandSQL(query))
					.execute(emailAddress);

			if (!existingUser.isEmpty()) {
				logger.severe("Email address already exists in the database.");
				throw new NdexException("email address is taken");
			}

			final String oldEmail = (String) user.field("emailAddress");
			user.field("emailAddress", emailAddress);
			user.save();

			// send emails to new and old address
			final File ChangeEmailFile = new File(Configuration.getInstance()
					.getProperty("Change-Email-File"));

			if (!ChangeEmailFile.exists()) {
				logger.severe("Could not retrieve change email file");
				throw new java.io.FileNotFoundException(
						"File containing change email content doesn't exist.");
			}

			final BufferedReader fileReader = Files.newBufferedReader(
					ChangeEmailFile.toPath(), Charset.forName("US-ASCII"));

			final StringBuilder changeEmailText = new StringBuilder();

			String lineOfText = null;
			while ((lineOfText = fileReader.readLine()) != null)
				changeEmailText.append(lineOfText.replace("{oldEmail}",
						oldEmail).replace("{newEmail}", emailAddress));

			Email.sendEmail(
					Configuration.getInstance().getProperty(
							"Change-Email-Email"), oldEmail, "Email Change",
					changeEmailText.toString());
			Email.sendEmail(
					Configuration.getInstance().getProperty(
							"Change-Email-Email"), emailAddress,
					"Email Change", changeEmailText.toString());

			logger.info("Changed email address for user with UUID " + id);

			return Response.ok().build();

		} catch (Exception e) {

			logger.severe("An error occured while changing email for user with UUID "
					+ id);
			throw new NdexException("Failed to change your email.\n"
					+ e.getMessage());

		}

	}
*/
	/**************************************************************************
	 * Update a user
	 * 
	 * @param updatedUser
	 *            User with new information
	 * @param id
	 *            UUID for user
	 * @throws NdexException
	 *             Attempting to access the database
	 * @throws IllegalArgumentException
	 *             new password and user id are required
	 * @throws ObjectNotFoundException
	 *             user does not exist
	 * @return User object
	 **************************************************************************/
	public User updateUser(User updatedUser, UUID id)
			throws IllegalArgumentException, NdexException,
			ObjectNotFoundException {

		Preconditions.checkArgument(id != null, "A user id is required");
		Preconditions.checkArgument(updatedUser != null,
				"An updated user is required");

		ODocument user = this.getRecordByUUID(id, NdexClasses.User);

		try {
			// updatedUser.getDescription().isEmpty();
				user.fields("description", updatedUser.getDescription(),
						"websiteURL", updatedUser.getWebsite(),
						"imageURL", updatedUser.getImage(),
						"firstName", updatedUser.getFirstName(),
						"lastName", updatedUser.getLastName(),
						NdexClasses.ExternalObj_mTime, updatedUser.getModificationTime());

			user = user.save();
			logger.info("Updated user profile with UUID " + id);

			return getUserFromDocument(user);

		} catch (Exception e) {

			logger.severe("An error occured while updating user profile with UUID "
					+ id);
			throw new NdexException(e.getMessage());

		}

	}

	/**************************************************************************
	 * getUserNetworkMemberships
	 * 
	 * @param userId
	 *            UUID for associated user
	 * @param permission
	 *            Type of memberships to retrieve, ADMIN, WRITE, or READ
	 * @param skipBlocks
	 *            amount of blocks to skip
	 * @param blockSize
	 *            The size of blocks to be skipped and retrieved
	 * @throws NdexException
	 *             Invalid parameters or an error occurred while accessing the
	 *             database
	 * @throws ObjectNotFoundException
	 *             Invalid userId
	 **************************************************************************/

	public List<Membership> getUserNetworkMemberships(UUID userId,
			Permissions permission, int skipBlocks, int blockSize)
			throws ObjectNotFoundException, NdexException {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.toString()),
				"A user UUID is required");
		Preconditions.checkArgument((permission == Permissions.ADMIN)
				|| (permission == Permissions.READ)
				|| (permission == Permissions.WRITE),
				"Valid permissions required");

		ODocument user = this.getRecordByUUID(userId, NdexClasses.User);

		final int startIndex = skipBlocks * blockSize;

		try {
			List<Membership> memberships = new ArrayList<>();

			String userRID = user.getIdentity().toString();

			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
					"SELECT FROM" + " (TRAVERSE out_"
							+ Permissions.GROUPADMIN.name().toLowerCase()
							+ ", out_"
							+ permission.name().toString().toLowerCase()
							+ " FROM" + " " + userRID + "  WHILE $depth <=2)"
							+ " WHERE @class = '" + NdexClasses.Network + "' and ( " + NdexClasses.ExternalObj_isDeleted +" = false) "
							+ " ORDER BY " + NdexClasses.ExternalObj_cTime + " DESC " + " SKIP "
							+ startIndex + " LIMIT " + blockSize);

			List<ODocument> records = this.db.command(query).execute();
			for (ODocument network : records) {

				Membership membership = new Membership();
				membership.setMembershipType(MembershipType.NETWORK);
				membership.setMemberAccountName((String) user
						.field("accountName"));
				membership.setMemberUUID(userId);
				membership.setPermissions(permission);
				membership.setResourceName((String) network.field("name"));
				membership.setResourceUUID(UUID.fromString((String) network
						.field("UUID")));

				memberships.add(membership);
			}

			logger.info("Successfuly retrieved user-network memberships");
			return memberships;

		} catch (Exception e) {
			logger.severe("An unexpected error occured while retrieving user-network memberships");
			throw new NdexException(e.getMessage());
		}
	}

	/**************************************************************************
	 * getUsergroupMemberships
	 * 
	 * @param userId
	 *            UUID for associated user
	 * @param permission
	 *            Type of memberships to retrieve, ADMIN, WRITE, or READ
	 * @param skipBlocks
	 *            amount of blocks to skip
	 * @param blockSize
	 *            The size of blocks to be skipped and retrieved
	 * @throws NdexException
	 *             Invalid parameters or an error occurred while accessing the
	 *             database
	 * @throws ObjectNotFoundException
	 *             Invalid userId
	 **************************************************************************/

	public List<Membership> getUserGroupMemberships(UUID userId,
			Permissions permission, int skipBlocks, int blockSize)
			throws ObjectNotFoundException, NdexException {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(userId.toString()),
				"A user UUID is required");
		Preconditions.checkArgument((permission.equals(Permissions.GROUPADMIN))
				|| (permission.equals(Permissions.MEMBER)),
				"Valid permissions required");

		ODocument user = this.getRecordByUUID(userId, NdexClasses.User);

		final int startIndex = skipBlocks * blockSize;

		try {
			List<Membership> memberships = new ArrayList<>();

			String userRID = user.getIdentity().toString();
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
					"SELECT FROM" + " (TRAVERSE " + NdexClasses.User + ".out_"
							+ permission.name().toString().toLowerCase()
							+ " FROM" + " " + userRID + "  WHILE $depth <=1)"
							+ " WHERE @class = '" + NdexClasses.Group + "' and ( " + NdexClasses.ExternalObj_isDeleted +" = false) "
							+ " ORDER BY " + NdexClasses.ExternalObj_cTime + " DESC " + " SKIP "
							+ startIndex + " LIMIT " + blockSize);

			List<ODocument> records = this.db.command(query).execute();
			for (ODocument group : records) {

				Membership membership = new Membership();
				membership.setMembershipType(MembershipType.GROUP);
				membership.setMemberAccountName((String) user
						.field("accountName"));
				membership.setMemberUUID(userId);
				membership.setPermissions(permission);
				membership.setResourceName((String) group
						.field(NdexClasses.GRP_P_NAME));
				membership.setResourceUUID(UUID.fromString((String) group
						.field("UUID")));

				memberships.add(membership);
			}

			logger.info("Successfuly retrieved user-group memberships");
			return memberships;

		} catch (Exception e) {
			logger.severe("An unexpected error occured while retrieving user-group memberships");
			throw new NdexException(e.getMessage());
		}
	}

	
	/**
	 * Get all the group memberships this user belongs to.
	 * @param userId
	 * @return
	 * @throws ObjectNotFoundException
	 * @throws NdexException
	 */
	public List<String> getUserAllGroupMemberships(String userAccountName)
			throws ObjectNotFoundException, NdexException {

		Preconditions.checkArgument(!Strings.isNullOrEmpty(userAccountName),
				"A user UUID is required");
		
		ODocument user = this.getRecordByAccountName(userAccountName,NdexClasses.User);

		try {
			List<String> memberships = new ArrayList<>();

			String userRID = user.getIdentity().toString();
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
					"SELECT FROM" + " (TRAVERSE " + NdexClasses.User + ".out_"
							+ Permissions.GROUPADMIN.name().toString().toLowerCase()
							+ "," + NdexClasses.User + ".out_" 
							+ Permissions.MEMBER.name().toString().toLowerCase()
							+ " FROM" + " " + userRID + "  WHILE $depth <=1)"
							+ " WHERE @class = '" + NdexClasses.Group + "' and ( " + NdexClasses.ExternalObj_isDeleted +" = false) ");

			List<ODocument> records = this.db.command(query).execute();
			for (ODocument group : records) {
				memberships.add((String) group
						.field(NdexClasses.account_P_accountName));
			}

			logger.info("Successfuly retrieved user-group memberships");
			return memberships;

		} catch (Exception e) {
			logger.severe("An unexpected error occured while retrieving user-group memberships");
			throw new NdexException(e.getMessage());
		}
	}
	
	
	/**************************************************************************
	 * getMembership
	 * 
	 * @param account
	 *            UUID for user or group
	 * @param resource
	 *            UUID for resource
	 * @throws NdexException
	 *             Invalid parameters or an error occurred while accessing the
	 *             database
	 * @throws ObjectNotFoundException
	 *             Invalid userId
	 **************************************************************************/

	public Membership getMembership(UUID account, UUID resource, int depth)
			throws IllegalArgumentException, ObjectNotFoundException,
			NdexException {

		Preconditions.checkArgument(account != null, "Account UUID required");
		Preconditions.checkArgument(resource != null, "Resource UUID required");
		Preconditions.checkArgument(depth > 0 && depth < 3, "Depth range: [1,2]");

		ODocument OAccount = this.getRecordByUUID(account, null);
		ODocument OResource = this.getRecordByUUID(resource, null);

		Permissions permission = null;
		Membership membership = new Membership();

		if (OResource.getClassName().equals(NdexClasses.Group)) {
			if (checkPermission(OAccount.getIdentity(),
					OResource.getIdentity(), Direction.OUT, depth,
					Permissions.GROUPADMIN))
				permission = Permissions.GROUPADMIN;
			if (checkPermission(OAccount.getIdentity(),
					OResource.getIdentity(), Direction.OUT, depth,
					Permissions.MEMBER))
				permission = Permissions.MEMBER;

			membership.setMemberAccountName((String) OAccount
					.field(NdexClasses.account_P_accountName));
			membership.setMemberUUID(account);
			membership.setResourceName((String) OResource
					.field(NdexClasses.GRP_P_NAME));
			membership.setResourceUUID(resource);
			membership.setPermissions(permission);
			membership.setMembershipType(MembershipType.GROUP);

		} else {
			// order allows us to return most permissive permission
			if (checkPermission(OAccount.getIdentity(),
					OResource.getIdentity(), Direction.OUT, depth, 
					Permissions.READ, Permissions.GROUPADMIN, Permissions.MEMBER))
				permission = Permissions.READ;
			if (checkPermission(OAccount.getIdentity(),
					OResource.getIdentity(), Direction.OUT, depth,
					Permissions.WRITE, Permissions.GROUPADMIN, Permissions.MEMBER))
				permission = Permissions.WRITE;
			if (checkPermission(OAccount.getIdentity(),
					OResource.getIdentity(), Direction.OUT, depth,
					Permissions.ADMIN, Permissions.GROUPADMIN, Permissions.MEMBER))
				permission = Permissions.ADMIN;

			membership.setMemberAccountName((String) OAccount
					.field(NdexClasses.account_P_accountName));
			membership.setMemberUUID(account);
			membership.setResourceName((String) OResource.field("name"));
			membership.setResourceUUID(resource);
			membership.setPermissions(permission);
			membership.setMembershipType(MembershipType.NETWORK);

		}

		if (permission != null)
			return membership;

		return null;
	}

	/**************************************************************************
	 * getSentRequest
	 * 
	 * @param account
	 *            User object
	 * @param skipBlocks
	 *            amount of blocks to skip
	 * @param blockSize
	 *            The size of blocks to be skipped and retrieved
	 * @throws NdexException
	 *             An error occurred while accessing the database
	 * @throws ObjectNotFoundException
	 *             Invalid userId
	 **************************************************************************/

	public List<Request> getSentRequest(User account, int skipBlocks,
			int blockSize) throws ObjectNotFoundException, NdexException {
		Preconditions.checkArgument(account != null, "Must be logged in");
		// TODO May possibly add extra parameter to specify type of request to
		// return

		final List<Request> requests = new ArrayList<>();

		ODocument user = this.getRecordByUUID(account.getExternalId(),
				NdexClasses.User);
		final int startIndex = skipBlocks * blockSize;

			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
					"SELECT FROM" + " (TRAVERSE out_requests FROM" + " "
							+ user.getIdentity().toString()
							+ "  WHILE $depth <=1)" + " WHERE @class = '"
							+ NdexClasses.Request + "'"
							+ " ORDER BY " + NdexClasses.ExternalObj_cTime + " DESC " + " SKIP "
							+ startIndex + " LIMIT " + blockSize);

			List<ODocument> records = this.db.command(query).execute();

			for (ODocument request : records) {
				requests.add(RequestDAO.getRequestFromDocument(request));
			}

			return requests;
	}

	/**************************************************************************
	 * getPendingRequest
	 * 
	 * @param account
	 *            User object
	 * @param skipBlocks
	 *            amount of blocks to skip
	 * @param blockSize
	 *            The size of blocks to be skipped and retrieved
	 * @throws NdexException
	 *             An error occurred while accessing the database
	 * @throws ObjectNotFoundException
	 *             Invalid userId
	 **************************************************************************/
	public List<Request> getPendingRequest(User account, int skipBlocks,
			int blockSize) throws ObjectNotFoundException, NdexException {
		Preconditions.checkArgument(account != null, "Must be logged in");
		// TODO May possibly add extra parameter to specify type of request to
		// return

		final List<Request> requests = new ArrayList<>();

		ODocument user = this.getRecordByUUID(account.getExternalId(),
				NdexClasses.User);
		final int startIndex = skipBlocks * blockSize;

		try {
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
					"SELECT FROM" + " (TRAVERSE in_requests FROM" + " "
							+ user.getIdentity().toString()
							+ "  WHILE $depth <=1)" + " WHERE @class = '"
							+ NdexClasses.Request + "'" + " AND response = '"
							+ ResponseType.PENDING + "'"
							+ " ORDER BY " + NdexClasses.ExternalObj_cTime + " DESC " + " SKIP "
							+ startIndex + " LIMIT " + blockSize);

			List<ODocument> records = this.db.command(query).execute();

			for (ODocument request : records) {
				requests.add(RequestDAO.getRequestFromDocument(request));
			}

			return requests;
		} catch (Exception e) {
			throw new NdexException("Unable to retrieve sent requests");
		}
	}

	public List<Task> getTasks(User account, Status status,
			int skipBlocks, int blockSize) throws ObjectNotFoundException,
			NdexException {

		Preconditions.checkArgument(account != null, "Must be logged in");

		final List<Task> tasks = new ArrayList<>();

		ODocument user = this.getRecordByUUID(account.getExternalId(),
				NdexClasses.User);
		final int startIndex = skipBlocks * blockSize;
		
		String statusFilter = "";
		if (status != Status.ALL){
			statusFilter = " and status = '" + status + "'";
		}

		try {
			OSQLSynchQuery<ODocument> query = new OSQLSynchQuery<>(
					"SELECT FROM" + " (TRAVERSE in_ownedBy FROM" + " "
							+ user.getIdentity().toString()
							+ "  WHILE $depth <=1)"
							+ " WHERE @class = '" + NdexClasses.Task + "' and ( " + NdexClasses.ExternalObj_isDeleted +" = false) " 
							+ statusFilter
							+ " ORDER BY " + NdexClasses.ExternalObj_cTime + " DESC " 
							+ " SKIP " + startIndex 
							+ " LIMIT " + blockSize);

			List<ODocument> records = this.db.command(query).execute();

			for (ODocument task : records) {
				tasks.add(TaskDAO.getTaskFromDocument(task));
			}

			return tasks;

		} catch (Exception e) {
			throw new NdexException("Unable to retrieve user tasks:" + e.getMessage());
		}
	}
	
	/*
	 * Convert the database results into our object model 
	 */
	/*
	 * Convert the database results into our object model 
	 */
	public static User getUserFromDocument(ODocument n) {

		User result = new User();

		Helper.populateExternalObjectFromDoc (result, n);

		result.setFirstName((String) n.field("firstName"));
		result.setLastName((String) n.field("lastName"));
		result.setWebsite((String) n.field("websiteURL"));
		result.setDescription((String) n.field("description"));
		result.setImage((String) n.field("imageURL"));
		
		if ( result.getIsDeleted() ) {
			result.setAccountName ((String) n.field(NdexClasses.account_P_oldAcctName));
			result.setEmailAddress((String) n.field(NdexClasses.User_P_oldEmailAddress));
		} else {
			result.setAccountName((String) n.field(NdexClasses.account_P_accountName));
			result.setEmailAddress((String) n.field("emailAddress"));
		}

		return result;
	}

	/*
	 * Both a User's AccountName and emailAddress must be unique in the
	 * database. Throw a DuplicateObjectException if that is not the case
	 */

	protected void checkForExistingUser(final NewUser newUser)
			throws DuplicateObjectException, NdexException {
		try {
			OIndex<?> Idx = this.db.getMetadata().getIndexManager()
					.getIndex("index-user-username");
			OIdentifiable user = (OIdentifiable) Idx.get(newUser
					.getAccountName()); // account to traverse by

			if (user != null) {
				logger.info("User with accountName " + newUser.getAccountName()
						+ " already exists");
				throw new DuplicateObjectException(
						CommonDAOValues.DUPLICATED_ACCOUNT_FLAG);
			}
			OIndex<?> emailIdx = this.db.getMetadata().getIndexManager()
					.getIndex("index-user-emailAddress");
			user = (OIdentifiable) emailIdx.get(newUser.getEmailAddress()); // account
																			// to
																			// traverse
																			// by

			if (user != null) {
				logger.info("User with emailAddress "
						+ newUser.getEmailAddress() + " already exists");
				throw new DuplicateObjectException(
						CommonDAOValues.DUPLICATED_EMAIL_FLAG);
			}
		} catch (DuplicateObjectException e) {
			throw e;
		} catch (Exception e) {
			logger.info("Unexpected error on existing user check");
			throw new NdexException(e.getMessage());
		}

	}

}
