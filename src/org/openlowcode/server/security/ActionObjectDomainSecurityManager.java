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
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.Authority;
import org.openlowcode.module.system.data.Domain;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.properties.LocatedInterface;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.data.storage.OrQueryCondition;

import org.openlowcode.server.data.storage.QueryConditionNever;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.runtime.OLcServer;




/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public abstract class ActionObjectDomainSecurityManager<E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>> extends ActionObjectSecurityManager<E> {
	private static final Logger logger = Logger.getLogger(ActionObjectDomainSecurityManager.class.getName());
	
	private String authoritysuffix;

	public ActionObjectDomainSecurityManager(String authoritysuffix) {
		super();
		this.authoritysuffix = authoritysuffix;
		
	}
	
	@Override
	public String toString() {
		String returnstring = "GalliumActionObjectDomainSecurityManager:"+authoritysuffix;
		return returnstring;
	}

	@Override
	public void freezeUnauthorizedObjects(DataObject<?>[] dataarray, SecurityBuffer buffer)
			 {
		for (int h=0;h<dataarray.length;h++) {
			try { 
				@SuppressWarnings("unchecked")
				E object = (E) dataarray[h];
			
				DataObjectId<Domain> domainid = object.getLocationdomainid();
				Domain domain = ServerSecurityBuffer.getUniqueInstance().getDomainPerId(domainid);
			
				
				String authority = domain.getNr()+"_"+authoritysuffix;
				Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();
				if (userauthorities!=null) for (int i=0;i<userauthorities.length;i++) {
					String thisauthority = userauthorities[i].getNr();
					if (thisauthority.equals(authority)) 
						 object.setUnfrozen();
						
					
				}
				
			} catch (ClassCastException e) {
				logger.warning("Exception in security manager "+e.getMessage());
				for (int i=0;i<e.getStackTrace().length;i++) {
					StackTraceElement stacktrace = e.getStackTrace()[i];
					logger.warning("   + "+stacktrace.toString());
				}
			}
			
		}
 		

	}
	@Override
	public boolean isObjectAuthorized(E object)  {
		if (object==null) throw new RuntimeException("Trying to use SecurityManager but object is not initialized, security manager"+this);
		
		boolean isauthorized = false;
		DataObjectId<Domain> domainid = object.getLocationdomainid();
		
		Domain domain = ServerSecurityBuffer.getUniqueInstance().getDomainPerId(domainid);
			if (domain!=null) {
	
		Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();
		
		if (userauthorities==null) return false;
		for (int i=0;i<userauthorities.length;i++) {
			String thisauthority = userauthorities[i].getNr();
			if (thisauthority.equals(domain.getNr()+"_"+authoritysuffix)) {
				logger.info("evaluating authority for domain "+domain.getNr()+" for suffix = "+authoritysuffix+" for authority "+thisauthority+" -> OK");
				isauthorized=true;
			} else {
				logger.info("evaluating authority for domain "+domain.getNr()+" for suffix = "+authoritysuffix+" for authority "+thisauthority+" -> KO");			
			}
			
		}
		return isauthorized; 
		} else {
			return isMaybeAuthorized();
		}
		
	}	
	@Override
	public boolean isMaybeAuthorized() {
		boolean isauthorized=false;
		Authority[] userauthorities = OLcServer.getServer().getSecuritymanager().getAuthoritiesForCurrentUser();
		if (userauthorities==null) return false;
		for (int i=0;i<userauthorities.length;i++) {
			String thisauthority = userauthorities[i].getNr();
			if (thisauthority.endsWith(authoritysuffix)) {
				logger.info("evaluating authority for suffix = "+authoritysuffix+" for authority "+thisauthority+" for blank domain object-> OK");
				
				isauthorized=true;
			} else {
				logger.info("evaluating authority for suffix = "+authoritysuffix+" for authority "+thisauthority+" for blank domain object-> KO");
				
			}
		}
		return isauthorized;
	}
	

	@Override
	public Function<TableAlias, QueryFilter> getOutputFilterCondition()  {
		logger.fine(" ----****---- Temporary debug - requested output filter condition for action object domain");
		Function<TableAlias, QueryFilter> returnfunction = new Function<TableAlias, QueryFilter>() {
			@Override
			public String toString() {
				return "GalliumActionObjectDomainSecurityManager authoritysuffix = "+authoritysuffix;
			}
			@Override
			public QueryFilter apply(TableAlias alias) {
				try {
					StoredFieldSchema<String> domainid = new StringStoredField("LOCATIONDOMAINID", null,64);
					SecurityManager securitymanager = OLcServer.getServer().getSecuritymanager();
					Authority[] authorities = securitymanager.getAuthoritiesForCurrentUser();
					ArrayList<DataObjectId<Domain>> domains = new ArrayList<DataObjectId<Domain>>();
					for (int i=0;i<authorities.length;i++) {
						Authority thisauthority = authorities[i];
						String authorityname = thisauthority.getNr();
						// only considers authority names that end with  authority suffix but are not exactly authority suffix
						if (authorityname.endsWith(authoritysuffix)) if (!authorityname.equals(authoritysuffix)) {
							String domainnumber = authorityname.substring(0,authorityname.length()-authoritysuffix.length()-1);
							
							Domain domain = ServerSecurityBuffer.getUniqueInstance().getDomainPerNr(domainnumber);
							domains.add(domain.getId());
						}
					}
				if (domains.size()==0) return QueryFilter.get(
						new QueryConditionNever());
				if (domains.size()==1) 
					
				 return QueryFilter.get(new SimpleQueryCondition<String>(alias,domainid,
						new  QueryOperatorEqual<String>(),domains.get(0).getId()));
				
				if (domains.size()>1) {
					OrQueryCondition oroperator = new OrQueryCondition();
					for (int i=0;i<domains.size();i++) oroperator.addCondition(new SimpleQueryCondition<String>(alias,
							domainid,
						new  QueryOperatorEqual<String>(),
						domains.get(i).getId()));
					return QueryFilter.get(oroperator);
				}
				return null;
			
			} catch (RuntimeException e) {
				logger.info("Exception during filter. This may be a security breach "+e.getMessage());			
				for (int i=0;i<e.getStackTrace().length;i++) logger.info(" - "+e.getStackTrace()[i].toString());
				return null;
			}
				
			}
		};
		return returnfunction;
	}



	@Override
	public boolean isAuthorizedForCurrentUser(String context, E object) {
		return isObjectAuthorized(object);
	}
	
}
