/**
 * Copyright Okta, Inc. 2013
 */
package com.okta.scim.server.example;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.okta.scim.server.capabilities.UserManagementCapabilities;
import com.okta.scim.server.exception.DuplicateGroupException;
import com.okta.scim.server.exception.EntityNotFoundException;
import com.okta.scim.server.exception.OnPremUserManagementException;
import com.okta.scim.server.jdbc.NamedPreparedStatement;
import com.okta.scim.server.service.SCIMOktaConstants;
import com.okta.scim.server.service.SCIMService;
import com.okta.scim.util.exception.InvalidDataTypeException;
import com.okta.scim.util.model.Email;
import com.okta.scim.util.model.Membership;
import com.okta.scim.util.model.Name;
import com.okta.scim.util.model.PaginationProperties;
import com.okta.scim.util.model.PhoneNumber;
import com.okta.scim.util.model.PhoneNumber.PhoneNumberType;
import ch.qos.logback.core.net.SyslogOutputStream;

import com.okta.scim.util.model.SCIMFilter;
import com.okta.scim.util.model.SCIMFilterType;
import com.okta.scim.util.model.SCIMGroup;
import com.okta.scim.util.model.SCIMGroupQueryResponse;
import com.okta.scim.util.model.SCIMUser;
import com.okta.scim.util.model.SCIMUserQueryResponse;

/**
 * This provides a working SCIM connector example where users and groups are kept in an MySql database.
 * The sample database already has a large set of users, groups, and group memberships. The example shows how your connector can 
 * work with an existing user database to only implement a subset of all the UM capabilities that Okta supports.
 * <p>
 * This connector assumes it is integrated with an App named <strong>onprem_mysql_app</strong> that has three custom properties, 
 * <strong>birth_date</strong>, <strong>gender</strong>, and <strong>hire_date</strong>.
 * <p>
 * There is a separate <strong>mysql-data</strong> directory that ships with the tester that contains data files specific to this example
 * connector with the expected custom properties, group ids, and user ids that are already expected in the database.
 * <p>
 * Initially this example connector is hard coded to return only a maximum of 2,000 users, but you can edit the <code>MAX_USERS_TO_RETURN</code>
 * field if you want to return more users to test the connector.
 *
 * @author cbarbara
 */
public class MySqlSCIMServiceImpl implements SCIMService {

    //Since this is just an example we won't always want to download every single user from the database
    //Replace the number with Integer.MAX_VALUE if you want to download every user.
    private HashMap<String,String> fieldMapping=new HashMap<String,String>();
    private final static String SF_SOURCE="SuccessFactors";
	private static final int MAX_USERS_TO_RETURN = Integer.MAX_VALUE;

    private static final Logger LOGGER = LoggerFactory.getLogger(MySqlSCIMServiceImpl.class);
	public static final int SYNC_DONE=3;
	public static final int SYNC_PENDING_UPDATE=1;
	public static final int SYNC_PENDING_INSERT=2;

    //Our Okta AppName that this connector is going to be connected to
    private static final String APP_NAME = "upllimited_ugdntest_1";
    //Our Okta Universal Directory (UD) Schema Name that this connector is going to use for the custom properties
    private static final String UD_SCHEMA_NAME = "custom";
    //The custom SCIM extension where our App's custom properties will be found
    private static final String USER_CUSTOM_URN = SCIMOktaConstants.CUSTOM_URN_PREFIX + APP_NAME + SCIMOktaConstants.CUSTOM_URN_SUFFIX + UD_SCHEMA_NAME;

    //Using constants for the names of our App's custom properties
    private static final String CUSTOM_SCHEMA_PROPERTY_NAME_BIRTH_DATE = "birth_date";
    private static final String CUSTOM_SCHEMA_PROPERTY_NAME_GENDER = "gender";
    private static final String CUSTOM_SCHEMA_PROPERTY_NAME_HIRE_DATE = "hire_date";
    private static final Set<String> ALL_VALID_CUSTOM_SCHEMA_PROPERTY_NAMES = new HashSet<String>();

    //In this sample database there isn't an email column, we are going to make up a fake email address for all users
    private static final String DOMAIN_EMAIL_SUFFIX = "@upl-ltd.com";
    private static final String DOMAIN_EMAIL_SUFFIX1 = "@upl.com";
    //In this sample database the user is still a member of the group/department if the membership to_date is set to 9999-01-01
    private static final String CURRENT_GROUP_MEMBERSHIP_END_DATE = "9999-01-01";

    //MySql connection information, these properties are set via the Spring dispatcher-servlet.xml file
    private String serverName;
    private int serverPort;
    private String databaseName;
    private String userName;
    private String password;
    private String connectionString;

    /**
     * Builds and validates the MySql connection properties. It is called after Spring creates an instance of the class. 
     * Assigning static value into Map.
     * @throws Exception
     */
    @PostConstruct
    public void afterCreation() throws Exception {
       
    	
    	fieldMapping.put("uid_no", "upl_ugdn");
    	//There is no field USER_PRINCIPLE_NAME in ugdn api
    	fieldMapping.put("old_ecode", "old_ecode");
    	fieldMapping.put("bus_name", "bus_name");
    	fieldMapping.put("middle_name", "middleName");
    	fieldMapping.put("desig", "title");
    	fieldMapping.put("dept", "department");
    	fieldMapping.put("phone", "primaryPhone");
    	fieldMapping.put("blood_group", "blood_group");
    	fieldMapping.put("date_birth", "date_of_birth");
    	fieldMapping.put("date_join", "start_date");
    	fieldMapping.put("date_left", "end_date");
    	fieldMapping.put("loc_name", "addressCity");
    	fieldMapping.put("country_name", "country_name");
    	fieldMapping.put("company_code", "company_code");
    	fieldMapping.put("emp_catg", "employee_class");
    	fieldMapping.put("region_name", "region_name");
    	fieldMapping.put("sub_area", "cust_SubLocation");
    	fieldMapping.put("emp_function", "division");
    	fieldMapping.put("sub_function", "cust_SubDivision");
    	fieldMapping.put("cost_centre", "costCenter");
    	fieldMapping.put("hr_band", "hr_band");
    	fieldMapping.put("active_flag", "active_flag");
    	fieldMapping.put("hod_id", "hod_id");
    	fieldMapping.put("source", "source");
       	fieldMapping.put("business_unit", "business_unit");
    	fieldMapping.put("hr_represent", "hr_represent");
    	
    	
    	try {
            Class.forName("org.drizzle.jdbc.DrizzleDriver").newInstance();
        } catch (Exception ex) {
            LOGGER.error("Unable to find the org.drizzle.jdbc.DrizzleDriver class: " + ex.getMessage(), ex);
            throw ex;
        }

        connectionString = String.format("jdbc:mysql:thin://%s:%d/%s",
                this.serverName, this.serverPort, this.databaseName);

        //test that everything works
        Connection conn = getDatabaseConnection();
        cleanupConnection(null, null, conn);

        //we use this Set during our SCIM filter evaluation to make sure all filter queries that get created only
        //contains the known custom fields and not just any custom field name from the extension passed in through the query string
//        ALL_VALID_CUSTOM_SCHEMA_PROPERTY_NAMES.add(CUSTOM_SCHEMA_PROPERTY_NAME_BIRTH_DATE);
//        ALL_VALID_CUSTOM_SCHEMA_PROPERTY_NAMES.add(CUSTOM_SCHEMA_PROPERTY_NAME_GENDER);
//        ALL_VALID_CUSTOM_SCHEMA_PROPERTY_NAMES.add(CUSTOM_SCHEMA_PROPERTY_NAME_HIRE_DATE);
    }
    private String findPhoneNumber(Collection<PhoneNumber> mobiles) {
    	if(mobiles==null || mobiles.isEmpty()) {
    		return "";
    	}
    	return mobiles.iterator().next().getValue();
    }

    private String findEmail(Collection<Email> emails) {
    	if(emails.isEmpty()) {
    		return "";
    	}
    	return emails.iterator().next().getValue();
    }
    /**
     * This method creates a user. All the standard attributes of the SCIM User can be retrieved by using the
     * getters on the SCIMStandardUser member of the SCIMUser object.
     * <p>
     * If there are custom schemas in the SCIMUser input, you can retrieve them by providing the name of the
     * custom property. 
     * <p><em>Example:</em> <code>SCIMUser.getStringCustomProperty("schemaName", "customFieldName")</code>, for a
     * string type property.</p>
     * <p>
     * This method is invoked when a POST is made to /Users with a SCIM payload representing a user
     * to create.
     * <p>
     * <strong>NOTE:</strong> While the user's group memberships are populated by Okta, according to the SCIM Spec
     * (http://www.simplecloud.info/specs/draft-scim-core-schema-01.html#anchor4), that information should be
     * considered read-only. Group memberships should only be updated through calls to createGroup or updateGroup.
     *
     * @param user A SCIMUser representation of the SCIM String payload sent by the SCIM client.
     * @return The created SCIMUser.
     * @throws OnPremUserManagementException
     */
    @Override
    public SCIMUser createUser(SCIMUser user) throws OnPremUserManagementException {
    	LOGGER.info("in Create User Method...");
    	NamedPreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;
        String id = getUserIdNo(user);
        SCIMUser existingUser = getUserById(getUserIdNo(user));
        if(existingUser!=null) {
        	return updateUser(id, existingUser);
        }
        
        try {
            //Start the INSERT query
            String query = "INSERT INTO employee (uid_no, old_ecode, bus_name, first_name, middle_name, last_name, display_name, desig, dept, phone, mobile, mail_id, blood_group, date_birth, date_join, date_left, loc_name, country_name, company_code, emp_catg, region_name, sub_area, emp_function, sub_function, cost_centre, hr_band, active_flag, hod_id, status, source, backwardsync,business_unit,hr_represent) VALUES "
            		+ "(:uid_no, :old_ecode, :bus_name, :first_name, :middle_name, :last_name, :display_name, :desig, :dept, :phone, :mobile, :mail_id, :blood_group, :date_birth, :date_join, :date_left, :loc_name, :country_name, :company_code, :emp_catg, :region_name, :sub_area, :emp_function, :sub_function, :cost_centre, :hr_band, :active_flag, :hod_id, :status, :source, :backwardsync,:business_unit,:hr_represent)";

            //Get the custom properties map (SchemaName -> JsonNode)
//            Map<String, JsonNode> customPropertiesMap = user.getCustomPropertiesMap();
//
//            String birthDate = null;
//            String gender = null;
//            String hireDate = null;
//
//            //in this example we expect our custom extension URN to be present
//            if (customPropertiesMap == null || !customPropertiesMap.containsKey(USER_CUSTOM_URN)) {
//                //you could decide to throw an exception if it is not there, and uncomment the following line
//                //throw new OnPremUserManagementException("MISSING_CUSTOM_PROPERTIES", "user missing the expected custom extension: " + USER_CUSTOM_URN);
//
//                //instead we will hard code some sample values
//                birthDate = "1972-05-12";
//                gender = "M";
//                hireDate = "2012-10-01";
//            } else {
//                //Get the JsonNode having all the custom properties for this schema
//                JsonNode customNode = customPropertiesMap.get(USER_CUSTOM_URN);
//
//                //getting the values directly from the customNode containing all of our custom schema extension properties.
//                // See the updateUser method below for a different way of getting to the values from your custom schema extension
//                birthDate = customNode.get(CUSTOM_SCHEMA_PROPERTY_NAME_BIRTH_DATE).asText();
//                gender = customNode.get(CUSTOM_SCHEMA_PROPERTY_NAME_GENDER).asText();
//                hireDate = customNode.get(CUSTOM_SCHEMA_PROPERTY_NAME_HIRE_DATE).asText();
//            }
            //get a new connection and start a new transaction
            
            
            Map<String, JsonNode> customPropertiesMap = user.getCustomPropertiesMap();
            if (customPropertiesMap == null || !customPropertiesMap.containsKey(USER_CUSTOM_URN)) { 
            	LOGGER.error("user missing the expected custom extension: " + USER_CUSTOM_URN + " user been pushed "+ getUserStr(user));
//            	throw new OnPremUserManagementException("MISSING_CUSTOM_PROPERTIES", "user missing the expected custom extension: " + USER_CUSTOM_URN + " user been pushed "+ getUserStr(user));
            }
            
            JsonNode customNode = customPropertiesMap.get(USER_CUSTOM_URN);
            
            String newUserId = getUserIdNo(user);
            LOGGER.info("new UserID"+newUserId);

            conn = getDatabaseConnection();
            conn.setAutoCommit(false);

            //create the statement and make sure our auto-incremented ID is returned
            stmt = NamedPreparedStatement.prepareStatement(conn, query);

            //populate our prepared statement with all the parameters
            int count=0;
            for (Entry<String, String> entry : fieldMapping.entrySet()) {
            	if(customNode == null) 
            	{
            		stmt.setString(entry.getKey(), null);
            	} else if(customNode.get(entry.getValue())!=null) {
            		stmt.setString(entry.getKey(), customNode.get(entry.getValue()).asText());
            	} else {
            		stmt.setString(entry.getKey(), null);
            	}
            	count++;
			}
            LOGGER.info("Count"+count);
            stmt.setString("first_name", user.getName().getFirstName());
            stmt.setString("last_name",user.getName().getLastName());
            stmt.setString("display_name", user.getName().getFormattedName());
            stmt.setString("mobile", findPhoneNumber(user.getPhoneNumbers())); //convert this to custom
            stmt.setString("mail_id", findEmail(user.getEmails())); //convert this to custom
            stmt.setString("uid_no", getUserIdNo(user));
            stmt.setString("active_flag",user.isActive()?"YES":"NO");
            stmt.setInt("backwardsync", SYNC_PENDING_INSERT);
            stmt.setInt("status", 1);
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new OnPremUserManagementException("CREATE_USER_INSERT_FAILED", "Creating user failed, expected 1 row affected but " + affectedRows + " rows affected.");
            }

            //get the new user id from the DB
            //NOTE: user.groupsGroups() is considered READ-ONLY according to the SCIM Spec
            // http://www.simplecloud.info/specs/draft-scim-core-schema-01.html#anchor4
            // Okta will serialize the user's group membership information for your reference but you should not
            // update the group membership from it. That should only happen through calls to createGroup or updateGroup

            //commit the transaction
            conn.commit();

            //return the most up to date copy of the user
            return getUser(newUserId);
        } catch (SQLException ex) {
            handleSQLException("createUser", ex, "CREATE_USER_INSERT_FAILED_EXCEPTION", conn);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }

        return null;
    }
    
    private String getUserStr(SCIMUser user) {
    	try {
			return new ObjectMapper().writeValueAsString(user.getCustomPropertiesMap());
		} catch (JsonGenerationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return "";
    }

    /**
     * This method updates a user.
     * <p>
     * This method is invoked when a PUT is made to <code>/Users/{id}</code> with the SCIM payload representing a user to
     * update.
     * <p>
     * <strong>NOTE:</strong> While the user's group memberships is populated by Okta, according to the SCIM Spec
     * (http://www.simplecloud.info/specs/draft-scim-core-schema-01.html#anchor4), that information should be
     * considered read-only. Group memberships should only be updated through calls to <code>createGroup</code> or <code>updateGroup</code>.
     *
     * @param id   The id of the SCIM user.
     * @param user A SCIMUser representation of the SCIM String payload sent by the SCIM client.
     * @return The updated SCIMUser.
     * @throws OnPremUserManagementException
     */
    @Override
    public SCIMUser updateUser(String id, SCIMUser user) throws OnPremUserManagementException, EntityNotFoundException {
        
    	LOGGER.info("In Update Users Method...");
    	//validate that the user already exists
        SCIMUser existingUser = getUserById(id);

        if(existingUser==null) {
        	return createUser(user);
        }
        
        //make sure the SCIM user record passed in has the same ID as the id passed in thought the URL
        if (!existingUser.getId().equalsIgnoreCase(user.getId())) {
            throw new OnPremUserManagementException("UPDATE_USER_ID_MISMATCH", "Modifying the user id is not allowed.");
        }
        
        NamedPreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            //UPDATE the user record with everything passed in
            String query = "UPDATE employee set old_ecode = :old_ecode, bus_name = :bus_name, first_name = :first_name, "
            		+ "middle_name = :middle_name, last_name = :last_name, display_name = :display_name, desig = :desig, "
            		+ "dept = :dept, phone = :phone, mobile = :mobile, mail_id = :mail_id, blood_group = :blood_group, "
            		+ "date_birth = :date_birth, date_join = :date_join, date_left = :date_left, loc_name = :loc_name, "
            		+ "country_name = :country_name, company_code = :company_code, emp_catg = :emp_catg, "
            		+ "region_name = :region_name, sub_area = :sub_area, emp_function = :emp_function, sub_function = :sub_function, "
            		+ "cost_centre = :cost_centre, hr_band = :hr_band, active_flag = :active_flag, hod_id = :hod_id, "
            		+ "status = :status, backwardsync = :backwardsync, source = IFNULL(:source, source)"
            		+ ",business_unit=:business_unit,hr_represent=:hr_represent WHERE uid_no = :uid_no"; 

            
            //get a new connection and start a new transaction
            conn = getDatabaseConnection();
            conn.setAutoCommit(false);

            //build statement
            stmt = NamedPreparedStatement.prepareStatement(conn, query);

//            Map<String, JsonNode> customPropertiesMap = user.getCustomPropertiesMap();
//            String birthDate = null;
//            String gender = null;
//            String hireDate = null;
//
//            //in this example we expect our custom extension URN to be present
//            if (customPropertiesMap == null || !customPropertiesMap.containsKey(USER_CUSTOM_URN)) {
//                //you could decide to throw an exception if it is not there, and uncomment the following line
//                //throw new OnPremUserManagementException("MISSING_CUSTOM_PROPERTIES", "user missing the expected custom extension: " + USER_CUSTOM_URN);
//
//                //instead we will just use the same values from the existing user in our database
//                birthDate = existingUser.getCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_BIRTH_DATE);
//                gender = existingUser.getCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_GENDER);
//                hireDate = existingUser.getCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_HIRE_DATE);
//            } else {
//                //getting the value by using the user.getCustomStringValue helper method to return our custom schema extension
//                // values. See the createUser method above for a different way of getting to the values from your custom schema extension
//                birthDate = user.getCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_BIRTH_DATE);
//                gender = user.getCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_GENDER);
//                hireDate = user.getCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_HIRE_DATE);
//            }

            
            Map<String, JsonNode> customPropertiesMap = user.getCustomPropertiesMap();
            LOGGER.info("Custom properties map"+customPropertiesMap);
            if (customPropertiesMap == null || !customPropertiesMap.containsKey(USER_CUSTOM_URN)) { 
            	LOGGER.error("user missing the expected custom extension: " + USER_CUSTOM_URN + " user been pushed "+ getUserStr(user));
//            	throw new OnPremUserManagementException("MISSING_CUSTOM_PROPERTIES", "user missing the expected custom extension: " + USER_CUSTOM_URN + " user been pushed "+ getUserStr(user));
            }
            
            JsonNode customNode = customPropertiesMap.get(USER_CUSTOM_URN);

            //populate our prepared statement with all the parameters
            for (Entry<String, String> entry : fieldMapping.entrySet()) {
//            	if("source".equals(entry.getValue()))
//            		continue;
            	
            	if(customNode == null) 
            	{
            		stmt.setString(entry.getKey(), null);
            	}
            	else if(customNode.get(entry.getValue())!=null) {
    				stmt.setString(entry.getKey(), customNode.get(entry.getValue()).asText());				            		
            	} else {
            		stmt.setString(entry.getKey(), null);
            	}
			}
            stmt.setString("first_name", user.getName().getFirstName());
            stmt.setString("last_name",user.getName().getLastName());
            stmt.setString("display_name", user.getName().getFormattedName());
            stmt.setString("mobile", findPhoneNumber(user.getPhoneNumbers())); //convert this to custom
            stmt.setString("mail_id", findEmail(user.getEmails())); //convert this to custom
            stmt.setString("uid_no", getUserIdNo(user));
            stmt.setString("active_flag",user.isActive()?"YES":"NO");
            if(getUserBackwardSyncStatus(getUserIdNo(user))==SYNC_PENDING_INSERT) {
                stmt.setInt("backwardsync", SYNC_PENDING_INSERT);
            } else {
                stmt.setInt("backwardsync", SYNC_PENDING_UPDATE);            	
            }
            stmt.setInt("status", 1);

            //execute our update
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new OnPremUserManagementException("UPDATE_USER_FAILED", "Updating user " + id + " failed, expected 1 row affected but " + affectedRows + " rows affected.");
            }

            //NOTE: user.groupsGroups() is considered READ-ONLY according to the SCIM Spec
            // http://www.simplecloud.info/specs/draft-scim-core-schema-01.html#anchor4
            // Okta will serialize the user's group membership information for your reference but you should not
            // update the group membership from it. That should only happen through calls to createGroup or updateGroup

            //commit and save
            conn.commit();
        } catch (SQLException ex) {
            handleSQLException("updateUser", ex, "UPDATE_USER_FAILED_EXCEPTION", conn);
        } catch (Exception e) {
            //LOGGER.error("updateUser" + " Failed - InvalidDataTypeException: " + e.getMessage(), e);
        	LOGGER.error("updateUser" + " Failed -: " + e.getMessage(), e);
            throw new OnPremUserManagementException("UPDATE_USER_FAILED_EXCEPTION_2", e.getMessage(), e);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }

        //return the most up to date user
        return getUserById(id);
    }

    /**
     * Get all the users.
     * <p>
     * This method is invoked when a GET is made to /Users.
     * To support pagination, so that the client and the server are not overwhelmed, this method supports querying based on a start index and the
     * maximum number of results expected by the client. The implementation is responsible for maintaining indices for the SCIM Users.
     *
     * @param pageProperties The pagination properties.
     * @param filter         The filter
     * @return The response from the server, which contains a list of  users along with the total number of results, the start index, and the items per page.
     * @throws com.okta.scim.server.exception.OnPremUserManagementException
     *
     */
    @Override
    public SCIMUserQueryResponse getUsers(PaginationProperties pageProperties, SCIMFilter filter) throws OnPremUserManagementException {
        if (filter != null) {
            return getUserByFilter(pageProperties, filter);
        } else {
            return getAllUsers(pageProperties);
        }
    }

    /**
     * Get a particular user.
     * <p>
     * This method is invoked when a GET is made to /Users/{id}
     *
     * @param id the Id of the SCIM User
     * @return the user corresponding to the id
     * @throws com.okta.scim.server.exception.OnPremUserManagementException
     *
     */
    @Override
    public SCIMUser getUser(String id) throws OnPremUserManagementException, EntityNotFoundException {
        SCIMUser user = getUserById(id);
        if (user == null) {
            //throw EntityNotFoundException when the id is invalid
            throw new EntityNotFoundException();
        }
        return user;
    }

    /**
     * Get all the groups.
     * <p>
     * This method is invoked when a GET is made to /Groups
     * In order to support pagination (So that the client and the server) are not overwhelmed, this method supports querying based on a start index and the
     * maximum number of results expected by the client. The implementation is responsible for maintaining indices for the SCIM groups.
     *
     * @param pageProperties @see com.okta.scim.util.model.PaginationProperties An object holding the properties needed for pagination - startindex and the count.
     * @return SCIMGroupQueryResponse the response from the server containing the total number of results, start index and the items per page along with a list of groups
     * @throws com.okta.scim.server.exception.OnPremUserManagementException
     *
     */
    @Override
    public SCIMGroupQueryResponse getGroups(PaginationProperties pageProperties) throws OnPremUserManagementException {
        SCIMGroupQueryResponse response = new SCIMGroupQueryResponse();

        response.setTotalResults(getTotalGroupCount());

        if (pageProperties == null) {
            pageProperties = new PaginationProperties(1, (int) (response.getTotalResults() + 1));
        }

        response.setStartIndex(pageProperties.getStartIndex());

        response.setScimGroups(getGroups(pageProperties.getStartIndex() - 1, pageProperties.getCount(), true));
        return response;
    }

    /**
     * Get a particular group.
     * <p>
     * This method is invoked when a GET is made to <code>/Groups/{id}</code>.
     *
     * @param id The id of the SCIM group.
     * @return The group corresponding to the id.
     * @throws com.okta.scim.server.exception.OnPremUserManagementException
     *
     */
    @Override
    public SCIMGroup getGroup(String id) throws OnPremUserManagementException {
        SCIMGroup group = getGroupById(id, true);
        if (group == null) {
            //throw EntityNotFoundException when the id is invalid
            throw new EntityNotFoundException();
        }
        return group;
    }

    /**
     * This method creates a group. All the standard attributes of the SCIM group can be retrieved by using the
     * getters on the SCIMStandardGroup member of the SCIMGroup object.
     * <p>
     * If there are custom schemas in the SCIMGroup input, you can retrieve them by providing the name of the
     * custom property. 
     * <p><em>Example:</em> <code>SCIMGroup.getCustomProperty("schemaName", "customFieldName"))</code>.</p>
     * <p>
     * This method is invoked when a POST is made to /Groups with a SCIM payload representing a group
     * to be created.
     *
     * @param group A SCIMGroup representation of the SCIM String payload sent by the SCIM client.
     * @return The created SCIMGroup
     * @throws com.okta.scim.server.exception.OnPremUserManagementException
     *
     */
    @Override
    public SCIMGroup createGroup(SCIMGroup group) throws OnPremUserManagementException, DuplicateGroupException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            String displayName = group.getDisplayName();

            String newGroupId = validateUniqueGroupNameAndGenerateNewId(displayName);

            //Okta groups will have an optional description with them, but in our sample database we will not be using the description
            //but this is an example of how you can read it out
            String groupDescription = group.getCustomStringValue(SCIMOktaConstants.APPGROUP_OKTA_CUSTOM_SCHEMA_URN, SCIMOktaConstants.OKTA_APPGROUP_DESCRIPTION, null);

            //get a new connection and start a new transaction
            conn = getDatabaseConnection();
            conn.setAutoCommit(false);

            //Start the INSERT query
            String query = "INSERT INTO departments (dept_no, dept_name) VALUES (?, ?)";

            //create the statement and make sure our auto-incremented ID is returned
            stmt = conn.prepareStatement(query);

            //populate our prepared statement with all the parameters
            stmt.setString(1, newGroupId);
            stmt.setString(2, displayName);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new OnPremUserManagementException("CREATE_GROUP_INSERT_FAILED", "Creating group failed, expected 1 row affected but " + affectedRows + " rows affected.");
            }

            group.setId(newGroupId);
            LOGGER.info("Created a new group with the id=" + newGroupId + ", name=" + displayName + ", description=" + groupDescription);

            //for every users group membership passed in, add the user as a member to that new group
            Collection<Membership> userMemberships = group.getMembers();
            if (userMemberships != null && !userMemberships.isEmpty()) {
                for (Membership userMembership : userMemberships) {
                    String userId = userMembership.getId();

                    addUserToGroup(conn, userId, newGroupId);
                }
            }

            //commit the transaction
            conn.commit();

            //return the group
            return group;
        } catch (SQLException ex) {
            handleSQLException("createGroup", ex, "CREATE_GROUP_INSERT_FAILED_EXCEPTION", conn);
        } catch (InvalidDataTypeException e) {
            LOGGER.error("createGroup" + " Failed - InvalidDataTypeException: " + e.getMessage(), e);
            throw new OnPremUserManagementException("CREATE_GROUP_INSERT_FAILED_EXCEPTION_2", e.getMessage(), e);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }
        return null;
    }

    /**
     * This method updates a group.
     * <p>
     * This method is invoked when a PUT is made to <code>/Groups/{id}</code> with the SCIM payload representing a group to
     * update.
     *
     * @param id The id of the SCIM group.
     * @param group SCIMGroup representation of the SCIM String payload sent by the SCIM client.
     * @return The updated SCIMGroup.
     * @throws com.okta.scim.server.exception.OnPremUserManagementException
     *
     */
    @Override
    public SCIMGroup updateGroup(String id, SCIMGroup group) throws OnPremUserManagementException {
        //make sure the group exists, getGroup throws EntityNotFoundException if it isn't found
        SCIMGroup existingGroup = getGroup(id);

        //make sure the SCIM group record passed in has the same ID as the id passed in thought the URL
        if (!existingGroup.getId().equalsIgnoreCase(group.getId())) {
            throw new OnPremUserManagementException("UPDATE_GROUP_ID_MISMATCH", "Modifying the group id is not allowed.");
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            //get a new connection and start a new transaction
            conn = getDatabaseConnection();
            conn.setAutoCommit(false);

            //UPDATE the group record with everything passed in
            String query = "UPDATE departments set dept_name = ? WHERE dept_no = ?";

            //build statement
            stmt = conn.prepareStatement(query);

            //populate our prepared statement with all the parameters
            stmt.setString(1, group.getDisplayName());
            stmt.setString(2, group.getId());

            //execute our update
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new OnPremUserManagementException("UPDATE_GROUP_FAILED", "Updating group " + id + " failed, expected 1 row affected but " + affectedRows + " rows affected.");
            }

            //create a Set of the existing group's user ids
            Set<String> previousUserMembershipIds = new HashSet<String>();
            Collection<Membership> memberships = existingGroup.getMembers();
            if (memberships != null && !memberships.isEmpty()) {
                for (Membership membership : memberships) {
                    previousUserMembershipIds.add(membership.getId().toLowerCase());
                }
            }

            //get the user membership records for the group
            memberships = group.getMembers();
            if (memberships != null && !memberships.isEmpty()) {
                for (Membership membership : memberships) {
                    String userId = membership.getId().toLowerCase();
                    //if the group id of the user doesn't already exist for the existing user
                    if (!previousUserMembershipIds.remove(userId)) {
                        //new user/group membership to add
                        addUserToGroup(conn, userId, id);
                    }
                }
            }

            //anything left in previousGroupMembershipIds needs to be removed from our group
            for (String userToRemove : previousUserMembershipIds) {
                query = "DELETE FROM dept_emp WHERE emp_no = ? AND dept_no = ?";

                stmt = conn.prepareStatement(query);
                stmt.setInt(1, Integer.parseInt(userToRemove));
                stmt.setString(2, id);

                affectedRows = stmt.executeUpdate();
                if (affectedRows == 0) {
                    //We wanted to remove the group membership record but the record was not there, it should be up to your connector to know if this
                    //is worthy of an exception or if logging is good enough.
                    LOGGER.warn("Attempted to remove the user " + userToRemove + " from the group " + id + ", but delete returned 0 rows affected.");
                } else {
                    LOGGER.info("Removed the user " + userToRemove + " from the group " + id);
                }
            }

            //commit and save
            conn.commit();
        } catch (SQLException ex) {
            handleSQLException("updateGroup", ex, "UPDATE_GROUP_FAILED_EXCEPTION", conn);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }

        //return the most up to date group
        return getGroup(id);
    }

    /**
     * Delete a particular group.
     * <p>
     * This method is invoked when a DELETE is made to <code>/Groups/{id}</code>.
     *
     * @param id id of the SCIM group.
     * @throws OnPremUserManagementException
     */
    @Override
    public void deleteGroup(String id) throws OnPremUserManagementException, EntityNotFoundException {

        //make sure the group exists, throw EntityNotFoundException if it isn't found
        SCIMGroup existingGroup = getGroupById(id, false);
        if (existingGroup == null) {
            throw new EntityNotFoundException();
        }

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            //get a new connection and start a new transaction
            conn = getDatabaseConnection();
            conn.setAutoCommit(false);

            String query = "DELETE FROM dept_emp WHERE dept_no = ?";

            //create the statement to delete the group/user memberships
            stmt = conn.prepareStatement(query);
            //populate our prepared statement with all the parameters
            stmt.setString(1, id);

            int affectedRows = stmt.executeUpdate();
            LOGGER.info("Deleted " + affectedRows + " existing group memberships from the group " + id);

            //Start the INSERT query
            query = "DELETE FROM departments WHERE dept_no = ?";

            //create the statement to delete the group
            stmt = conn.prepareStatement(query);
            //populate our prepared statement with all the parameters
            stmt.setString(1, id);

            stmt.executeUpdate();
            LOGGER.info("Deleted the group " + id);

            //commit the transaction
            conn.commit();
        } catch (SQLException ex) {
            handleSQLException("deleteGroup", ex, "DELETE_GROUP_FAILED_EXCEPTION", conn);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }
    }

    /**
     * Get all the Okta User Management capabilities that this SCIM Service has implemented.
     * <p>
     * This method is invoked when a GET is made to /ServiceProviderConfigs. It is called only when you are testing
     * or modifying your connector configuration from the Okta Application instance UM UI. If you change the return values
     * at a later time please retest and resave your connector settings to have your new return values respected.
     * <p>
     * These User Management capabilities help customize the UI features available to your app instance and tells Okta
     * all the possible commands that can be sent to your connector.
     *
     * @return all the implemented User Management capabilities.
     */
    @Override
    public UserManagementCapabilities[] getImplementedUserManagementCapabilities() {
        return new UserManagementCapabilities[]{
               // UserManagementCapabilities.GROUP_PUSH,
                UserManagementCapabilities.IMPORT_NEW_USERS,
                UserManagementCapabilities.IMPORT_PROFILE_UPDATES,
                UserManagementCapabilities.PUSH_NEW_USERS,
                UserManagementCapabilities.PUSH_PROFILE_UPDATES,
                UserManagementCapabilities.PUSH_USER_DEACTIVATION,
                UserManagementCapabilities.REACTIVATE_USERS
                //because of our sample database schema, we have no active/inactive or password columns
                //so we will tell Okta that we don't support the below capabilities since these capabilities all
                //relate to either the active/inactive state of a user, or updating the user's password
//                UserManagementCapabilities.PUSH_PASSWORD_UPDATES,
//                UserManagementCapabilities.PUSH_PENDING_USERS,
//                UserManagementCapabilities.PUSH_USER_DEACTIVATION,
//                UserManagementCapabilities.REACTIVATE_USERS
        };
    }

    /*
    Start private MySql implementation
     */

    /**
     * Get the server name.
     *
     * @return The server name. 
     */
    public String getServerName() {
        return serverName;
    }

    /**
     * Set the server name.
     *
     * @param serverName The server name to set.
     */
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    /**
     * Get the server port.
     *
     * @return The server port. 
     */
    public int getServerPort() {
        return serverPort;
    }

    /**
     * Set the server port.
     *
     * @param serverPort The integer value of the server port to set.
     */
    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    /**
     * Get the database name.
     *
     * @return The database name. 
     */
    public String getDatabaseName() {
        return databaseName;
    }

    /**
     * Set the database name.
     *
     * @param databaseName The database name to set.
     */
    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    /**
     * Get the user name.
     *
     * @return The user name. 
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Set the user name.
     *
     * @param userName The user name to set.
     */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /**
     * Get the password.
     *
     * @return The password. 
     */
    public String getPassword() {
        return password;
    }
    /**
     * Set the password.
     *
     * @param password The value of the password to set.
     */

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the user query response based on the pagination properties and SCIM filter.
     *
     * @param pageProperties The optional pagination properties.
     * @param filter         The SCIM filter.
     * @return The user query response.
     */
    private SCIMUserQueryResponse getUserByFilter(PaginationProperties pageProperties, SCIMFilter filter) {
        SCIMUserQueryResponse response = new SCIMUserQueryResponse();
        int maxUsers = MAX_USERS_TO_RETURN;
        long initialUserIndex = 1;

        //If the input has some pagination properties, update the start index and max count
        if (pageProperties != null) {
            initialUserIndex = pageProperties.getStartIndex();
            maxUsers = pageProperties.getCount();
        }

        response.setStartIndex(initialUserIndex);

        //Get users based on a filter
        //SCIM pagination is always 1 based, subtract 1 before making any SQL queries
        List<SCIMUser> users = getUserByFilter(filter, initialUserIndex - 1, maxUsers);

        //The total results in this case is set to the number of users. But it may be possible that
        //there are more results than what is being returned => totalResults > users.size();
        response.setTotalResults(users.size());
        //Actual results which need to be returned
        response.setScimUsers(users);
        return response;
    }

    /**
     * Get the user records based on a SCIM filter.
     *
     * @param filter           The SCIM filter.
     * @param initialUserIndex The first zero-based user record to return.
     * @param maxUsers         The maximum number of users to return.
     * @return the user records
     */
    private List<SCIMUser> getUserByFilter(SCIMFilter filter, long initialUserIndex, int maxUsers) {
        SCIMFilterType filterType = filter.getFilterType();
        Connection conn = getDatabaseConnection();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        List<SCIMUser> users = new ArrayList<SCIMUser>();
        try {
            if (filterType.equals(SCIMFilterType.EQUALS)) {
                //Example to show how to deal with an Equality filter
                stmt = getUsersQueryByEqualityFilter(conn, filter, initialUserIndex, maxUsers);
            } else if (filterType.equals(SCIMFilterType.OR)) {
                //Example to show how to deal with an OR filter containing multiple sub-filters.
                stmt = getUsersQueryByOrFilter(conn, filter, initialUserIndex, maxUsers);
            } else {
                LOGGER.error("The Filter " + filter + " contains a condition that is not supported");
                throw new OnPremUserManagementException("INVALID_SCIM_FILTER", "The Filter " + filter + " contains a condition that is not supported");
            }

            rs = stmt.executeQuery();

            //since our filter query just returns the employee ids, for each employee we get back we
            //need to fetch the full user record
            while (rs.next()) {
                SCIMUser user = getUserById(rs.getString(1));
                if (user != null) {
                    users.add(user);
                } else {
                    LOGGER.warn("No user found with the id " + rs.getString(1));
                }
            }
        } catch (SQLException ex) {
            handleSQLException("getUserByFilter", ex, "GET_BY_FILTER_EXCEPTION", null);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }

        return users;
    }

    /**
     * Generate the SQL query from the OR SCIM filter.
     * <p/>
     * Currently, Okta only sends an OR SCIM filter when the Okta user contains an array of values for the
     * <em>Unique user field name</em> that you selected when configuring your connector; for example, email or phoneNumber.
     *
     * @param conn             The SQL connection.
     * @param filter           The SCIM filter.
     * @param initialUserIndex The first zero-based user record to return.
     * @param maxUsers         The maximum number of users to return.
     * @return The SQL query for the users.
     */
    private PreparedStatement getUsersQueryByOrFilter(Connection conn, SCIMFilter filter, long initialUserIndex, int maxUsers) throws SQLException {
        //An OR filter would contain a list of filter expression. Each expression is a SCIMFilter by itself.
        //Ex : "email eq "abc@def.com" OR email eq "def@abc.com""
        List<SCIMFilter> subFilters = filter.getFilterExpressions();
        LOGGER.info("OR Filters : " + subFilters);

        List<Object> values = new ArrayList<Object>();
        String query = "SELECT uid_no FROM employee WHERE ";
        //Loop through the sub filters to evaluate each of them.
        for (int i = 0; i < subFilters.size(); i++) {
            if (i > 0) {
                query += " OR ";
            }
            query += fromScimFilterToWhereQueryClause(subFilters.get(i), values);
        }

        //for consistency, make sure we always return the users in the same order no matter what query
        query += " ORDER BY last_name, first_name, uid_no LIMIT " + initialUserIndex + ", " + maxUsers;

        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < values.size(); i++) {
            stmt.setObject(i + 1, values.get(i));
        }
        return stmt;
    }

    /**
     * Generate the SQL query from the EQUALITY SCIM filter
     *
     * @param conn             the sql connection
     * @param filter           SCIM filter
     * @param initialUserIndex first user (0 based) record to return
     * @param maxUsers         max number of users to return
     * @return sql query for the users
     */
    private PreparedStatement getUsersQueryByEqualityFilter(Connection conn, SCIMFilter filter, long initialUserIndex, int maxUsers) throws SQLException {
        //for consistency, make sure we always return the users in the same order no matter what query
        List<Object> values = new ArrayList<Object>();
        String query = "SELECT uid_no FROM employee WHERE " + fromScimFilterToWhereQueryClause(filter, values) + " ORDER BY last_name, first_name, uid_no LIMIT " + initialUserIndex + ", " + maxUsers;
        LOGGER.debug(query);
        LOGGER.debug("Values just before preparing statement. "+values);
        PreparedStatement stmt = conn.prepareStatement(query);
        for (int i = 0; i < values.size(); i++) {
            stmt.setObject(i + 1, values.get(i));
        }
        return stmt;
    }

    /**
     * Generate the SQL query where clause from our SCIM filter
     *
     * @param filter the SCIM filter
     * @return the where clause to use in the sql query
     */
    private String fromScimFilterToWhereQueryClause(SCIMFilter filter, List<Object> values) {
        String fieldName = filter.getFilterAttribute().getAttributeName();
        String value = filter.getFilterValue();
        LOGGER.info("Equality Filter : Field Name [ " + fieldName + " ]. Value [ " + value + " ]");

        //A basic example of how to return users that match the criteria
        //Ex : "userName eq "someUserName""
        if (fieldName.equalsIgnoreCase("userName") || fieldName.equalsIgnoreCase("email")) {
            values.add(value.split("@")[0]);
            LOGGER.info("Values pushed till now "+values);
            return "uid_no = ? ";
        } else if (fieldName.equalsIgnoreCase("upl_ugdn")) {
            //"id eq "someId""
            values.add(value);
            return "uid_no = ? ";
        } else if (fieldName.equalsIgnoreCase("last_name")) {
            String subFieldName = filter.getFilterAttribute().getSubAttributeName();
            if (subFieldName.equalsIgnoreCase("familyName")) {
                //"name.familyName eq "someFamilyName""
                values.add(value);
                return "last_name = ? ";
            } else if (subFieldName.equalsIgnoreCase("first_name")) {
                //"name.givenName eq "someGivenName""
                values.add(value);
                return "first_name = ? ";
            }
        } else if (filter.getFilterAttribute().getSchema().equalsIgnoreCase(USER_CUSTOM_URN)) { //Check that the Schema name is the Custom Schema name to process the filter for custom fields
            //On the query string the filter will look like this: urn:okta:onprem_mysql_app:1.0:user:birth_date eq "someValue"

            //to minimize the risk of a SQL injection, make sure that the field name is in the list of valid field names
            if (!ALL_VALID_CUSTOM_SCHEMA_PROPERTY_NAMES.contains(fieldName)) {
                throw new OnPremUserManagementException("UNKNOWN_CUSTOM_FIELD_NAME", "fieldName of " + fieldName + " is not in the list of valid custom field names.");
            }

            //the SCIMFilter class strips out the urn and filedName is left with only the last attribute name of: birth_date
            values.add(value);
            return fieldName + " = ? ";
        }
        LOGGER.error("fromScimFilterToWhereQueryClause - fieldName of " + fieldName + " is unknown!");
        throw new OnPremUserManagementException("UNKNOWN_FIELD_NAME", "fieldName of " + fieldName + " is unknown!");
    }

    /**
     * Get all users records
     *
     * @param paginationProperties optional pagination properties
     * @return the users query response
     */
    private SCIMUserQueryResponse getAllUsers(PaginationProperties paginationProperties) {
    	LOGGER.info("In Get All Uers Method...");
        SCIMUserQueryResponse response = new SCIMUserQueryResponse();

        //just for this example we are putting a limit on the number of users we return for testing purposes
        response.setTotalResults(Math.min(MAX_USERS_TO_RETURN, getTotalUserCount()));

        //pagination properties are not always present based on the SCIM query (ex: http://localhost:8080/Users)
        if (paginationProperties == null) {
            //so for our defaults we will return everyone
            paginationProperties = new PaginationProperties(1, (int) (response.getTotalResults() + 1));
        }

        response.setStartIndex(paginationProperties.getStartIndex());

        response.setScimUsers(getUsers(paginationProperties.getStartIndex() - 1, paginationProperties.getCount()));
        return response;
    }

    /**
     * Get a single user object
     *
     * @param id the user id
     * @return the user
     */
    private SCIMUser getUserById(String id) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = getDatabaseConnection();
        try {
            String query = "select e.* FROM employee AS e WHERE e.uid_no = ? AND active_flag='YES' OR source='"+SF_SOURCE+"' ORDER BY e.last_name, e.first_name, e.uid_no";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, id);

            rs = stmt.executeQuery();

            SCIMUser user = null;
            if (rs.next()) {
                user = toScimUser(rs, null);
            }
            return user;
        } catch (SQLException ex) {
            handleSQLException("getUserById", ex, "GET_USER_BY_ID_EXCEPTION", null);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }
        return null;
    }
    
    private int getUserBackwardSyncStatus(String id) {
    	PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = getDatabaseConnection();
        try {
            String query = "select backwardsync from employee where uid_no = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, id);

            rs = stmt.executeQuery();
            
            int userBackwardSync = 0;
            if (rs.next()) {
                userBackwardSync = rs.getInt("backwardsync");
            }
            return userBackwardSync;
        } catch (SQLException ex) {
            handleSQLException("getUserById", ex, "GET_USER_BY_ID_EXCEPTION", null);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }
        return 0;
    }

    /**
     * Convert the sql result to to a SCIMUser object
     *
     * @param rs        the sql result
     * @param allGroups optional list of all groups in our application
     * @return the SCIMUser object
     * @throws Exception 
     * @throws SQLException
     */
    private SCIMUser toScimUser(ResultSet rs, List<SCIMGroup> allGroups) throws SQLException  {
    	LOGGER.info("In TOSCIM User Method...");
    	SCIMUser user = new SCIMUser();
        String oktaAttribute;
        String ugdnAttribute;
    	try{
        
        //build the basic SCIM user object from the sql result
        user.setId(rs.getString("uid_no"));
        
        user.setName(new Name(rs.getString("display_name"), rs.getString("last_name"), rs.getString("first_name")));
        String emailAndUsername = user.getId() + DOMAIN_EMAIL_SUFFIX;
        user.setUserName(emailAndUsername);
        user.setPhoneNumbers(Arrays.asList(new PhoneNumber(rs.getString("mobile"),PhoneNumberType.MOBILE,true)));
        user.setEmails(Arrays.asList(new Email(rs.getString("mail_id"), "work", true)));
        


        //because of our sample database schema, there is no sense of active/inactive users so all users are active
        user.setActive("yes".equalsIgnoreCase(rs.getString("active_flag")));
        //because of our sample database schema, there is no password column for the users
        user.setPassword(null);

        //build this user's group membership
       // user.setGroups(buildUserGroupMembership(allGroups, user.getId()));

        //now set our custom extension properties from the sql result
 //       user.setCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_BIRTH_DATE, rs.getString(2));
//        user.setCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_GENDER, rs.getString(5));
//        user.setCustomStringValue(USER_CUSTOM_URN, CUSTOM_SCHEMA_PROPERTY_NAME_HIRE_DATE, rs.getString(6));
        
        Iterator fmIterator = fieldMapping.entrySet().iterator(); 
        
        // Iterate through the hashmap 
        // and add some bonus marks for every student 
        LOGGER.info("HashMap after adding bonus marks:"); 
        
        while (fmIterator.hasNext()) { 
            Map.Entry mapElement = (Map.Entry)fmIterator.next();  
            ugdnAttribute = (String) mapElement.getKey(); 
            oktaAttribute = (String) mapElement.getValue();
            
           	user.setCustomStringValue(USER_CUSTOM_URN, oktaAttribute, rs.getString(ugdnAttribute));
        } 
    	}catch (SQLException e) {
    		e.printStackTrace();
    		LOGGER.error(e.getMessage(),e);
			throw e;
		}
        user.setCustomStringValue("urn:scim:schemas:core:1.0", "externalId", user.getId());
        LOGGER.info("User serialized as "+user.getCustomPropertiesMap()+" with Status"+ user.isActive());
        LOGGER.info("User serialized as "+user.toString());
        return user;
    }

    /**
     * Get a list of users from the database
     *
     * @param startIndex first user (0 based) record to return
     * @param count      total number of records to return
     * @return a list of user records
     */
    private List<SCIMUser> getUsers(long startIndex, int count) {
        //Get a list of all known groups without their memberships in a single query.
        //This is used later when the user's group membership is built so that we won't need to make repeat queries
        //for every group to get its basic information
       // List<SCIMGroup> allGroups = getGroups(0, 100000, false);

        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = getDatabaseConnection();

        try {
            String query = "SELECT e.* FROM employee AS e where e.active_flag='YES' OR e.source='"+SF_SOURCE+"' ORDER BY e.last_name, e.first_name, e.uid_no LIMIT ?, ?";

            stmt = conn.prepareStatement(query);
            stmt.setLong(1, startIndex);
            stmt.setInt(2, count);

            //query to get all the users and their optional current group membership id
            rs = stmt.executeQuery();

            List<SCIMUser> users = new ArrayList<SCIMUser>();
            while (rs.next()) {
                SCIMUser user = toScimUser(rs, null);
                users.add(user);
            }
            return users;
        } catch (SQLException ex) {
            handleSQLException("getUsers", ex, "GET_USERS_EXCEPTION", null);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }
        return null;
    }

    /**
     * Get the user's group memberships
     *
     * @param allGroups optional - list of all groups in our application
     * @param userId    user id
     * @return group memberships for the user
     */
    private List<Membership> buildUserGroupMembership(List<SCIMGroup> allGroups, String userId) {
        LOGGER.info("Finding user group membership for " + userId);
        PreparedStatement stmt = null;
        ResultSet rs = null;
        Connection conn = getDatabaseConnection();

        try {
            String query = "SELECT dept_no FROM dept_emp WHERE emp_no = ? AND to_date = '" + CURRENT_GROUP_MEMBERSHIP_END_DATE + "'";

            stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userId));

            rs = stmt.executeQuery();

            List<Membership> groups = new ArrayList<Membership>();
            while (rs.next()) {
                String groupId = rs.getString(1);
                SCIMGroup matchedGroup = null;

                //if we were passed a list of all the groups in our application, we will iterate through that to
                //find the group information
                if (allGroups != null) {
                    for (SCIMGroup group : allGroups) {
                        if (group.getId().equalsIgnoreCase(groupId)) {
                            matchedGroup = group;
                            break;
                        }
                    }
                } else {
                    //otherwise we will need to query for the group individually
                    matchedGroup = getGroupById(groupId, false);
                }

                //if we found a group, create the membership object and add it to our return list
                if (matchedGroup != null) {
                    //the SCIM user's group membership object is made up of the group ID and the name of the group
                    groups.add(new Membership(groupId, matchedGroup.getDisplayName()));
                }
            }

            LOGGER.info("Found " + groups.size() + " groups for " + userId);
            return groups;
        } catch (SQLException ex) {
            handleSQLException("buildUserGroupMembership", ex, "GET_USER_GROUP_MEMBERSHIP", null);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }
        return null;
    }

    /**
     * Get the group
     *
     * @param id              group id
     * @param withMemberships to include all the user memberships for the groups or not
     * @return the group or null
     */
    private SCIMGroup getGroupById(String id, boolean withMemberships) {
        Connection conn = getDatabaseConnection();

        try {
            return getGroupById(conn, id, withMemberships);
        } catch (SQLException ex) {
            handleSQLException("getGroupById", ex, "GET_GROUP_BY_ID_EXCEPTION", null);
        } finally {
            cleanupConnection(null, null, conn);
        }
        return null;
    }

    /**
     * Get the group, using an existing connection
     *
     * @param conn            the sql connection
     * @param id              group is
     * @param withMemberships to include all the user memberships for the groups or not
     * @return the group or null
     * @throws SQLException
     */
    private SCIMGroup getGroupById(Connection conn, String id, boolean withMemberships) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        SCIMGroup group = null;

        try {
            stmt = conn.prepareStatement("SELECT * FROM departments where dept_no = ?");
            stmt.setString(1, id);

            rs = stmt.executeQuery();

            if (rs.next()) {
                group = toScimGroup(rs, withMemberships);
            }
        } finally {
            //don't cleanup the connection since it will be created in the method that created it
            cleanupConnection(stmt, rs, null);
        }

        return group;
    }

    /**
     * Convert the sql record to a group object
     *
     * @param rs              the sql record
     * @param withMemberships to include all the user memberships for the groups or not
     * @return the group
     * @throws SQLException
     */
    private SCIMGroup toScimGroup(ResultSet rs, boolean withMemberships) throws SQLException {
        SCIMGroup group = new SCIMGroup();
        group.setId(rs.getString(1));
        group.setDisplayName(rs.getString(2));

        if (withMemberships) {
            group.setMembers(getGroupMembership(group.getId()));
        }

        return group;
    }

    /**
     * Get all groups from our database
     *
     * @param startIndex      the first group record (0 based) to return
     * @param count           max number of group records to return
     * @param withMemberships include all the user memberships for the group
     * @return the groups from our database
     */
    private List<SCIMGroup> getGroups(long startIndex, int count, boolean withMemberships) {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = getDatabaseConnection();

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT * FROM departments ORDER BY dept_name LIMIT " + startIndex + ", " + count);

            List<SCIMGroup> groups = new ArrayList<SCIMGroup>();
            while (rs.next()) {
                SCIMGroup group = toScimGroup(rs, withMemberships);
                groups.add(group);
            }

            LOGGER.info("Found " + groups.size() + " groups");
            return groups;
        } catch (SQLException ex) {
            handleSQLException("getGroups", ex, "GET_GROUPS_EXCEPTION", null);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }
        return null;
    }

    /**
     * Get all the users that are members of the group
     *
     * @param groupId the group id
     * @return list of all the current members of the group
     */
    private Collection<Membership> getGroupMembership(String groupId) {
        LOGGER.info("Finding group membership for " + groupId);
//        PreparedStatement stmt = null;
//        ResultSet rs = null;
//        Connection conn = getDatabaseConnection();
//
//        try {
//            String query = "SELECT e.emp_no, e.first_name, e.last_name " +
//                    "FROM employee as e " +
//                    "   JOIN dept_emp AS de ON de.emp_no = e.emp_no " +
//                    "WHERE de.dept_no = ? AND de.to_date = '" + CURRENT_GROUP_MEMBERSHIP_END_DATE + "' " +
//                    "ORDER BY e.emp_no ";
//            stmt = conn.prepareStatement(query);
//            stmt.setString(1, groupId);
//
//            rs = stmt.executeQuery();

            //SCIM group membership objects are made up of the ID and the userName user
            Collection<Membership> groupMembers = new ArrayList<Membership>();
//            while (rs.next()) {
//                groupMembers.add(new Membership(rs.getString(1), buildUserName(rs.getString(3), rs.getString(2))));
//            }

            LOGGER.info("Found " + groupMembers.size() + " group memberships for " + groupId);
            return groupMembers;
//        } catch (SQLException ex) {
//            handleSQLException("getGroupMembership", ex, "GET_GROUP_MEMBERSHIP_EXCEPTION", null);
//        } finally {
//            cleanupConnection(stmt, rs, conn);
//        }
//        return null;
    }

    /**
     * Add a user group membership record
     *
     * @param conn    the sql connection to use
     * @param userId  user id
     * @param groupId group id
     * @throws SQLException
     */
    private void addUserToGroup(Connection conn, String userId, String groupId) throws SQLException {
        //make sure that the group exists
        SCIMGroup group = getGroupById(conn, groupId, false);
        if (group == null) {
            //up to your application to decide on this is exception worthy or not.
            LOGGER.warn("The user " + userId + " has a group membership to " + groupId + ", but that group does not exist in the system.");
            return;
        }

        PreparedStatement stmt = null;
        try {
            //build query to create a group membership record
            String query = "INSERT INTO dept_emp (emp_no, dept_no, from_date, to_date) VALUES (?, ?, '1985-01-01', '" + CURRENT_GROUP_MEMBERSHIP_END_DATE + "')";

            stmt = conn.prepareStatement(query);
            stmt.setInt(1, Integer.parseInt(userId));
            stmt.setString(2, groupId);

            //execute query
            int affectedRows = stmt.executeUpdate();
            if (affectedRows != 1) {
                throw new OnPremUserManagementException("USER_GROUP_MEMBERSHIP_INSERT_FAILED", "Creating user " + userId + " membership into group " + groupId + " failed, expected 1 row affected but " + affectedRows + " rows affected.");
            }
        } finally {
            //don't cleanup the connection since it will be created in the method that created it
            cleanupConnection(stmt, null, null);
        }
        LOGGER.info("Added user with the id " + userId + " to the group " + groupId);
    }

    /**
     * Here is one example of what might need to be done when your database does not generate the unique record ids for you.
     * It will throw a DuplicateGroupException when the group name is not going to be unique. Because of the schema we are
     * working with we cannot just create a UUID/GUID and use that as our unique value.
     * <p/>
     * This is NOT a perfect example, it is not thread safe or guaranteed to work if you have multiple connectors or applications
     * talking to the same database.
     *
     * @param displayName group name
     * @return new unique group id to use
     */
    private String validateUniqueGroupNameAndGenerateNewId(String displayName) {
        List<SCIMGroup> allGroups = getGroups(0, 100000, false);

        for (SCIMGroup group : allGroups) {
            if (group.getDisplayName().toLowerCase().equalsIgnoreCase(displayName.toLowerCase())) {
                LOGGER.warn("A group with the name " + displayName + " already exists, won't attempt to create a new one with the same name.");
                throw new DuplicateGroupException();
            }
        }

        String newGroupId = String.format("d%03d", allGroups.size() + 1);
        LOGGER.info("There are " + allGroups.size() + " existing groups, the new group will have the ID " + newGroupId);

        return newGroupId;
    }

    private String buildUserName(String familyName, String givenName) {
        return (familyName + givenName).replaceAll(" ", "") + DOMAIN_EMAIL_SUFFIX;
    }

    private long getTotalUserCount() {
        return getCount("getTotalUserCount", "SELECT count(*) FROM employee where active_flag='YES' OR source='"+SF_SOURCE+"'");
    }

    private long getTotalGroupCount() {
        return getCount("getTotalGroupCount", "SELECT count(*) FROM departments");
    }

    private long getCount(String methodName, String sql) {
        Statement stmt = null;
        ResultSet rs = null;
        Connection conn = getDatabaseConnection();

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            rs.next();

            return rs.getLong(1);
        } catch (SQLException ex) {
            handleSQLException(methodName, ex, "GET_COUNT_EXCEPTION", null);
        } finally {
            cleanupConnection(stmt, rs, conn);
        }
        return -1;
    }

    private Connection getDatabaseConnection() throws OnPremUserManagementException {
        Connection conn;
        try {
            conn = DriverManager.getConnection(connectionString, this.userName, this.password);
            if(!conn.equals(null))
            {
            	LOGGER.info("Connection Successfull..!!");
            }
        } catch (Exception ex) {
            LOGGER.error("Unable to connect to " + connectionString + " as " + this.userName + " - " + ex.getMessage(), ex);
            throw new OnPremUserManagementException("DB_CONNECTION_FAILED", ex.getMessage(), ex);
        }
        return conn;
    }

    private void cleanupConnection(Statement stmt, ResultSet rs, Connection conn) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException sqlEx) {
                LOGGER.error("Unable cleanup and close the result set", sqlEx);
            }
        }

        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException sqlEx) {
                LOGGER.error("Unable cleanup and close the statement", sqlEx);
            }
        }

        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException sqlEx) {
                LOGGER.error("Unable cleanup and close the db connection", sqlEx);
            }
        }
    }

    private void handleSQLException(String methodName, SQLException ex, String customErrorCode, Connection conn) throws OnPremUserManagementException {
        // log any errors
        LOGGER.error(methodName + " Failed - SQLException: " + ex.getMessage() +
                "\r\nSQLState: " + ex.getSQLState() +
                "\r\nVendorError: " + ex.getErrorCode(), ex);

        if (conn != null) {
            try {
                conn.rollback();
            } catch (SQLException e) {
                LOGGER.error("Rollback failed", e);
            }
        }

        throw new OnPremUserManagementException(customErrorCode, ex.getMessage(), ex);
    }
    
    public static String getUserCustomUrn() {
		return USER_CUSTOM_URN;
	}
    
    public static String getUserIdNo(SCIMUser user) {
    	String uid_no=null;
    	if(user.getCustomPropertiesMap()!=null) {
    		try {
				uid_no = user.getCustomStringValue(USER_CUSTOM_URN, "upl_ugdn");
			} catch (InvalidDataTypeException e) {
				e.printStackTrace();
			}
    	}
    	if(uid_no==null) {
	    	String username = user.getUserName();
	    	uid_no = username.split("@")[0];
    	}
    	return uid_no;
    }
}
