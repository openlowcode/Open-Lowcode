/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.security;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.data.Domain;
import org.openlowcode.module.system.data.Groupmemberlink;
import org.openlowcode.module.system.data.Groupswithauthority;
import org.openlowcode.module.system.data.Systemattribute;
import org.openlowcode.module.system.data.Usergroup;
import org.openlowcode.server.data.ThreeDataObjects;
import org.openlowcode.server.data.properties.DataObjectId;



/**
 * This class aims at storing in the memory of the server all information related to users and groups
 * Requirement of memory is estimated at 1KB per user
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ServerSecurityBuffer {

	private static Logger logger = Logger.getLogger(ServerSecurityBuffer.class.getName());
	/**
	 * @return the singleton of this class
	 */
	public static ServerSecurityBuffer getUniqueInstance()  {
		if (uniqueinstance==null) {
			uniqueinstance = new ServerSecurityBuffer();
			uniqueinstance.refreshData();
		}
		return uniqueinstance;
	}
	private static ServerSecurityBuffer uniqueinstance;
	
	
	
	private HashMap<DataObjectId<Appuser>,ArrayList<Usergroup>> groupsperuser; // note identification of the user should be the same as in CSPServerSession, i.e. server userid
	private HashMap<DataObjectId<Usergroup>,ArrayList<Authority>> authoritiespergroup;
	private HashMap<DataObjectId<Authority>,ArrayList<Usergroup>> groupsperauthority;
	private HashMap<DataObjectId<Usergroup>,ArrayList<Appuser>> userspergroup;
	private HashMap<DataObjectId<Domain>,Domain> domainsperid;
	private HashMap<String,Domain> domainspernumber;
	private HashMap<DataObjectId<Domain>,ArrayList<Authority>> authoritiesperdomain;
	private HashMap<String,Systemattribute> attributesbuffer;


	private HashMap<DataObjectId<Appuser>, Appuser> usersperuserid;
	
	public Appuser getUserPerUserId(DataObjectId<Appuser> appuserid) {
		return this.usersperuserid.get(appuserid);
	}
	
	/**
	 * get all the user groups from this authority from the buffer.
	 * @param authorityid unique id of an authority
	 * @return all the user groups for this authority
	 */
	public Usergroup[] getGroupsForAuthority(DataObjectId<Authority> authorityid) {
		logger.finer(" -------------------- query for authorityid ---------------------");
		logger.finer(" authorityid = "+authorityid);
		
		ArrayList<Usergroup> groups = groupsperauthority.get(authorityid);
		if (groups!=null) {
			logger.info(" query on authorityid returned  "+groups.size()+" lines");
			return groups.toArray(new Usergroup[0]);
		}
		logger.finer(" query on authorityid, not found");
		return new Usergroup[0];
	}
	
	/**
	 * gets all the users inside a group from the buffer
	 * @param groupid uniqueid of the group
	 * @return all users declared from this group
	 */
	public Appuser[] getUsersForGroups(DataObjectId<Usergroup> groupid) {
		logger.finer(" -------------------- query for groupid ---------------------");
		logger.finer(" groupid = "+groupid);
		
		ArrayList<Appuser> users = userspergroup.get(groupid);
		if (users!=null) {
			logger.finer(" query on groupid returned  "+users.size()+" lines");
			return users.toArray(new Appuser[0]);
		}
		logger.finer(" query on groupid, not found");
		return new Appuser[0];
	}
	/**
	 * get all the authorities for a user
	 * @param userid id of a user of the application 
	 * @return all authorities for the user (with potential duplicates)
	 * @since 1.5
	 */
	
	public Authority[] getAuthoritiesForUser(DataObjectId<Appuser> userid) {
		ArrayList<Usergroup> groups = this.groupsperuser.get(userid);
		ArrayList<Authority> allauthorities = new ArrayList<Authority>();
		for (int i=0;i<groups.size();i++) {
			allauthorities.addAll(this.authoritiespergroup.get(groups.get(i).getId()));
		}
		return allauthorities.toArray(new Authority[0]);
	}
	
	/**
	 * gets all the authority for a group from the buffer
	 * @param groupid unique id of a group
	 * @return all authorities linked to this group
	 */
	public Authority[] getAuthoritiesForGroup(DataObjectId<Usergroup> groupid) {
		logger.finer(" -------------------- query for groupid ---------------------");
		logger.finer(" groupid = "+groupid);
		
		ArrayList<Authority> authorities = authoritiespergroup.get(groupid);
		if (authorities!=null) {
			logger.finer(" query on groupid returned  "+authorities.size()+" lines");
			return authorities.toArray(new Authority[0]);
		}
		logger.finer(" query on groupid, not found");
		return new Authority[0];
	}
	/**
	 * gets all the users that have this authority from the buffer (using group as an intermediate)
	 * @param authority unique id of the authority
	 * @return all users who hold this authority
	 */
	public Appuser[] getUsersForAuthority(DataObjectId<Authority> authority) {
		// this hashmap is used to manage unicity of answers
		HashMap<String,Appuser> usersbyid = new HashMap<String,Appuser>();
		ArrayList<Appuser> registeredusers = new ArrayList<Appuser>();
		Usergroup[] allgroupsforauthority = getGroupsForAuthority(authority);
		if (allgroupsforauthority!=null) for (int i=0;i<allgroupsforauthority.length;i++) {
			Usergroup currentusergroup=allgroupsforauthority[i];
			Appuser[] usersforcurrentgroup = this.getUsersForGroups(currentusergroup.getId());
			if (usersforcurrentgroup!=null) for (int j=0;j<usersforcurrentgroup.length;j++) {
				Appuser currentuser = usersforcurrentgroup[j];
				if (usersbyid.get(currentuser.getId().getId())==null) {
					usersbyid.put(currentuser.getId().getId(),currentuser);
					registeredusers.add(currentuser);
				}
			}
		}
		return registeredusers.toArray(new Appuser[0]);
	}
	
	/**
	 * gets all the groups of a user from the buffer
	 * @param userid unique id of the user
	 * @return all the groups this user is registered in
	 */
	public Usergroup[] getGroupsForUser(DataObjectId<Appuser> userid) {
		logger.finer(" -------------------- query for userid ---------------------");
		logger.finer(" userid = "+userid);
		ArrayList<Usergroup> grouplist = groupsperuser.get(userid);
		if (grouplist!=null) {
			logger.finer(" query on userid returned  "+grouplist.size()+" lines");
			return grouplist.toArray(new Usergroup[0]);
		}
		logger.finer(" query on userid returned, not found");
		return new Usergroup[0];
	}
	/**
	 * gets a domain from its unique id
	 * @param id the domain unique id
	 * @return the Domain object
	 */
	public Domain getDomainPerId(DataObjectId<Domain> id) {
		if (this.domainsperid==null) return null;
		return this.domainsperid.get(id);
	}
	/**
	 * get a domain from its business id (number)
	 * @param nr the unique business id (number) of the domain
	 * @return the domain object
	 */
	public Domain getDomainPerNr(String nr) {
		if (this.domainspernumber==null) return null;
		return this.domainspernumber.get(nr);
	}
	
	/**
	 * gets all authorities for the domain
	 * @param domainid the unique id of the domain
	 * @return all authorities for this domain
	 */
	public Authority[] getAuthoritiesForDomain(DataObjectId<Domain> domainid) {
		if (this.authoritiesperdomain==null) return new Authority[0];
		ArrayList<Authority> authorities = this.authoritiesperdomain.get(domainid);
		if (authorities==null) return new Authority[0];
		return authorities.toArray(new Authority[0]);
	}
	/**
	 * 
	 * @param number unique busines identifier (number) of the System Attribute
	 * @return the SystemAttrbute object
	 */
	public Systemattribute getSystemattribute(String number) {
		return this.attributesbuffer.get(number);
	}
	
	/**
	 * refreshes data in the buffer. It is managed so that the buffer can be queried even while refreshing
	 */
	public void refreshData()  {
		try {
		logger.info(" ------------- Refreshing security cache ---------------------");
		long starttimestamp = System.currentTimeMillis();
		HashMap<DataObjectId<Appuser>,Appuser> temporaryusersperuserid = new HashMap<DataObjectId<Appuser>,Appuser>();
		Appuser[] allusers = Appuser.getallactive(null);
		for (int i=0;i<allusers.length;i++) {
			temporaryusersperuserid.put(allusers[i].getId(),allusers[i]);
		
		}
		HashMap<DataObjectId<Appuser>,ArrayList<Usergroup>> temporarymapforusergroup = new HashMap<DataObjectId<Appuser>,ArrayList<Usergroup>>();
		HashMap<DataObjectId<Usergroup>,ArrayList<Appuser>> temporarymapforappuser = new HashMap<DataObjectId<Usergroup>,ArrayList<Appuser>>();
		ThreeDataObjects<Usergroup, Groupmemberlink, Appuser>[] queryresult = Groupmemberlink.getlinksandbothobjects(null);
		

		logger.info("found "+queryresult.length+" lines in query for user & group link");
		for (int i=0;i<queryresult.length;i++) {
			Usergroup group = queryresult[i].getObjectOne();
			Appuser user = queryresult[i].getObjectThree();
			logger.finer(" -- User id:"+user.getId()+", number:"+user.getNr()+" -- Group id:"+group.getId()+", number:"+group.getNr());
		
			if (temporarymapforusergroup.get(user.getId())==null) {
				ArrayList<Usergroup> grouparray = new ArrayList<Usergroup>();
				grouparray.add(group);
				temporarymapforusergroup.put(user.getId(),grouparray);
			} else {
				ArrayList<Usergroup> grouparray = temporarymapforusergroup.get(user.getId());
				grouparray.add(group);
			}
			if (temporarymapforappuser.get(group.getId())==null) {
				ArrayList<Appuser> appuserarray = new ArrayList<Appuser>();
				appuserarray.add(user);
				temporarymapforappuser.put(group.getId(),appuserarray);
			} else {
				ArrayList<Appuser> appuserarray = temporarymapforappuser.get(group.getId());
				appuserarray.add(user);
			}
		}
		HashMap<DataObjectId<Usergroup>,ArrayList<Authority>> temporarymapforauthorities = new HashMap<DataObjectId<Usergroup>,ArrayList<Authority>>();
		HashMap<DataObjectId<Authority>,ArrayList<Usergroup>> temporarymapforgroupsperauthority = new HashMap<DataObjectId<Authority>,ArrayList<Usergroup>>();
		
		ThreeDataObjects<Authority, Groupswithauthority, Usergroup>[] authorityqueryresult = Groupswithauthority.getlinksandbothobjects(null);
			logger.info("found "+authorityqueryresult.length+" lines in query for authority & user group link");
				
			for (int i=0;i<authorityqueryresult.length;i++) {
				Authority authority = authorityqueryresult[i].getObjectOne();
				Usergroup group = authorityqueryresult[i].getObjectThree();
				logger.finer(" -- Group id:"+group.getId()+", number:"+group.getNr()+" --- Authority: id:"+authority.getId()+", number:"+authority.getNr());
				if (temporarymapforauthorities.get(group.getId())==null) {
					ArrayList<Authority> authorityarray = new ArrayList<Authority>();
					authorityarray.add(authority);
					temporarymapforauthorities.put(group.getId(),authorityarray);
				} else {
					ArrayList<Authority> authorityarray = temporarymapforauthorities.get(group.getId());
					authorityarray.add(authority);
				}
				if (temporarymapforgroupsperauthority.get(authority.getId())==null) {
					ArrayList<Usergroup> grouparray = new ArrayList<Usergroup>();
					grouparray.add(group);
					temporarymapforgroupsperauthority.put(authority.getId(),grouparray);
				} else {
					ArrayList<Usergroup> grouparray = temporarymapforgroupsperauthority.get(authority.getId());
					grouparray.add(group);
				}
				
			}
		HashMap<DataObjectId<Domain>,Domain> temporarydomainsperid = new HashMap<DataObjectId<Domain>,Domain>();
		HashMap<String,Domain> temporarydomainspernumber = new HashMap<String,Domain>();
		HashMap<DataObjectId<Domain>,ArrayList<Authority>> temporaryauthoritiesperdomain = new HashMap<DataObjectId<Domain>,ArrayList<Authority>>();
		
		Domain[] domains = Domain.getallactive(null);
		for (int i=0;i<domains.length;i++) {
			temporarydomainsperid.put(domains[i].getId(),domains[i]);
			temporarydomainspernumber.put(domains[i].getNr(),domains[i]);
		}
		
		Authority[] allauthorities = Authority.getallactive(null);
	
		for (int i=0;i<allauthorities.length;i++) {
			Authority thisauthority = allauthorities[i];
			ArrayList<Authority> authoritiesfordomain=temporaryauthoritiesperdomain.get(thisauthority.getLinkedtoparentforscopeid());
			if (authoritiesfordomain==null) {
				authoritiesfordomain = new ArrayList<Authority>();
				temporaryauthoritiesperdomain.put(thisauthority.getLinkedtoparentforscopeid(),authoritiesfordomain);
			}
			authoritiesfordomain.add(thisauthority);
		}
		
		HashMap<String,Systemattribute> attributes = new HashMap<String,Systemattribute>();
		Systemattribute[] attributeslist = Systemattribute.getallactive(null);
		for (int i=0;i<attributeslist.length;i++) attributes.put(attributeslist[i].getNr(),attributeslist[i]);
		
		this.groupsperuser = temporarymapforusergroup;
		this.authoritiespergroup = temporarymapforauthorities;
		this.userspergroup = temporarymapforappuser;
		this.groupsperauthority = temporarymapforgroupsperauthority;
		this.domainsperid = temporarydomainsperid;
		this.domainspernumber = temporarydomainspernumber;
		this.authoritiesperdomain = temporaryauthoritiesperdomain;
		this.usersperuserid = temporaryusersperuserid;
		this.attributesbuffer=attributes;
		long endtimestamp = System.currentTimeMillis();
		logger.info(" ------------- Refreshing security cache end, loaded ("+
		(groupsperuser.size())+"/"+(authoritiespergroup.size())+") elements in "+(endtimestamp-starttimestamp)+"ms ---------------------");
		} catch (Exception  e) {
			logger.severe("------------- Error while refreshing security buffer ------------");
			logger.severe("    "+e.getMessage());
			for (int i=0;i<e.getStackTrace().length;i++) logger.severe("   "+e.getStackTrace()[i]);
		}
		}
	
}
