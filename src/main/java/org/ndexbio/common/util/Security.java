package org.ndexbio.common.util;

import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.MultivaluedMap;

import org.jboss.resteasy.util.Base64;
import org.ndexbio.common.NdexClasses;
import org.ndexbio.common.access.NdexAOrientDBConnectionPool;
import org.ndexbio.common.models.dao.orientdb.UserDAO;
import org.ndexbio.common.models.dao.orientdb.UserOrientdbDAO;
import org.ndexbio.model.object.User;

import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
import com.orientechnologies.orient.core.record.impl.ODocument;
import com.orientechnologies.orient.core.sql.OCommandSQL;

public class Security
{
	/**************************************************************************
	    * Authenticates the user against the OrientDB database. (1.0)
	    * 
	    * @param authInfo
	    *            A string array containing the username/password.
	    * @throws Exception
	    *            Accessing the database failed.
	    * @returns True if the user is authenticated, false otherwise.
	    **************************************************************************/
	    public static User authenticateUser(final String userName, final String password, ODatabaseDocumentTx db) 
	    		throws Exception {
	    	
	        try {
	        	
	        	//replace this with method from UserDAO
	            List<ODocument> usersFound = db
	                .command(new OCommandSQL("select from " + NdexClasses.User + " where accountName = ?"))
	                .execute(userName);
	            
	            if (usersFound.size() < 1)
	                return null;

	            ODocument OUserDoc = usersFound.get(0);
	            String hashedPassword = Security.hashText(password);
	            
	            if ( ((String)OUserDoc.field("password")).equals(hashedPassword)) {
	            	return UserDAO._getUserFromDocument(OUserDoc); 
	            }
	            
	            return null;
	            
	        } catch (Exception e) {
	        	
	        	throw e;
	        	
	        }
	        
	    }
    /**************************************************************************
    * Authenticates the user against the OrientDB database.
    * 
    * @param authInfo
    *            A string array containing the username/password.
    * @throws Exception
    *            Accessing the database failed.
    * @returns True if the user is authenticated, false otherwise.
    **************************************************************************/
	    @Deprecated
    public static User authenticateUser(final String userName, final String password ) throws Exception
    {
    	ODatabaseDocumentTx ndexDatabase = null;
        try
        {
        	ndexDatabase = NdexAOrientDBConnectionPool.getInstance().acquire();

            List<ODocument> usersFound = ndexDatabase
                .command(new OCommandSQL("select from User where accountName = ?"))
                .execute(userName);
            
            if (usersFound.size() < 1)
                return null;

            ODocument OUserDoc = usersFound.get(0);
            String hashedPassword = Security.hashText(password);
            if ( ((String)OUserDoc.field("password")).equals(hashedPassword)) {
            	return UserOrientdbDAO.getUserFromDocument(OUserDoc);
            }
            
            return null;
        }
        finally
        {
            if (ndexDatabase != null)
                ndexDatabase.close();
        }
	    
    }

    /**************************************************************************
    * Converts bytes into hexadecimal text.
    * 
    * @param data
    *            The byte data.
    * @return A String containing the byte data as hexadecimal text.
    **************************************************************************/
    public static String convertByteToHex(byte data[])
    {
        StringBuffer hexData = new StringBuffer();
        for (int byteIndex = 0; byteIndex < data.length; byteIndex++)
            hexData.append(Integer.toString((data[byteIndex] & 0xff) + 0x100, 16).substring(1));
        
        return hexData.toString();
    }

    /**************************************************************************
    * Generates a password of 10 random characters.
    * 
    * @return A String containing the random password.
    **************************************************************************/
    public static String generatePassword()
    {
        return generatePassword(10);
    }
    
    /**************************************************************************
    * Generates a password of random characters.
    * 
    * @param passwordLength
    *            The length of the password.
    * @return A String containing the random password.
    **************************************************************************/
    public static String generatePassword(int passwordLength)
    {
        final String alphaCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String numericCharacters = "0123456789";
        final String symbolCharacters = "`-=;~!@#$%^&*_+|:?";
        
        StringBuilder randomPassword = new StringBuilder();
        for (int passwordIndex = 0; passwordIndex < passwordLength; passwordIndex++)
        {
            //Determine if the character will be alpha, numeric, or a symbol
            final int charType = randomNumber(1, 3);
            
            if (charType == 1)
                randomPassword.append(alphaCharacters.charAt(randomNumber(0, alphaCharacters.length() - 1)));
            else if (charType == 2)
                randomPassword.append(numericCharacters.charAt(randomNumber(0, numericCharacters.length() - 1)));
            else
                randomPassword.append(symbolCharacters.charAt(randomNumber(0, symbolCharacters.length() - 1)));
        }
        
        return randomPassword.toString();
    }
    
    /**************************************************************************
    * Computes a SHA-512 hash against the supplied text.
    * 
    * @param textToHash
    *            The text to compute the hash against.
    * @return A String containing the SHA-512 hash in hexadecimal format.
    **************************************************************************/
    public static String hashText(String textToHash) throws Exception
    {
        final MessageDigest sha512 = MessageDigest.getInstance("SHA-512");
        sha512.update(textToHash.getBytes());
        
        return convertByteToHex(sha512.digest());
    }

    /**************************************************************************
    * Base64-decodes and parses the Authorization header to get the username
    * and password.
    * 
    * @param requestContext
    *            The servlet HTTP request context.
    * @throws IOException
    *            Decoding the Authorization header failed.
    * @return a String array containing the username and password.
    **************************************************************************/
    public static String[] parseCredentials(ContainerRequestContext requestContext) throws IOException
    {
        final MultivaluedMap<String, String> headers = requestContext.getHeaders();
        final List<String> authHeader = headers.get("Authorization");
        
        if (authHeader == null || authHeader.isEmpty())
            return null;

        final String encodedAuthInfo = authHeader.get(0).replaceFirst("Basic" + " ", "");
        final String decodedAuthInfo = new String(Base64.decode(encodedAuthInfo));
        
        return decodedAuthInfo.split(":");
    }
    
    /**************************************************************************
    * Generates a random number between the two values.
    * 
    * @param minValue
    *            The minimum range of values.
    * @param maxValue
    *            The maximum range of values.
    * @return A random number between the range.
    **************************************************************************/
    public static int randomNumber(int minValue, int maxValue)
    {
        return minValue + (int)(Math.random() * ((maxValue - minValue) + 1));
    }
}
