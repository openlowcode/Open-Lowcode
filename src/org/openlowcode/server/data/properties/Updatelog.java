/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.Date;

import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.module.system.data.AppuserDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.storage.StoredField;
import org.openlowcode.server.runtime.OLcServer;

/**
 * Update log property allows to record who last updated the object and when
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 */
public class Updatelog<E extends DataObject<E>> extends DataObjectProperty<E> {
	@SuppressWarnings("unused")
	private Storedobject<E> storedobject;
	@SuppressWarnings("unused")
	private Uniqueidentified<E> uniqueidentified;
	private StoredField<String> updateuserid;
	private StoredField<Date> updatetime;
	private boolean datesetbyscript = false;
	private boolean usersetbyscript = false;

	@SuppressWarnings("unchecked")
	public Updatelog(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		updateuserid = (StoredField<String>) this.field.lookupOnName("UPDATEUSERID");

		updatetime = (StoredField<Date>) this.field.lookupOnName("UPDATETIME");
	}

	/**
	 * put this to true to set date by script. This is needed during first creation
	 * of objects on the server
	 */
	public void setdateByscript() {
		datesetbyscript = true;
	}

	/**
	 * put this to true to set user by script. This is needed during first creation
	 * of objects on the server
	 */
	public void setuserByscript() {
		usersetbyscript = true;
	}

	/**
	 * @return the objectid of the user (note: this is Open Lowcode technical id,
	 *         not the business id (number)
	 */
	public DataObjectId<Appuser> getUpdateuserid() {
		return new DataObjectId<Appuser>(this.updateuserid.getPayload(), AppuserDefinition.getAppuserDefinition());
	}

	/**
	 * @return the date the object was updated
	 */
	public Date getUpdatetime() {

		return this.updatetime.getPayload();
	}

	/**
	 * @param updateuserid the objectid of the appuser that performed the last
	 *                     update (must be a valid id to ensure application correct
	 *                     working)
	 */
	protected void setUpdateuserid(String updateuserid) {
		this.updateuserid.setPayload(updateuserid);
	}

	/**
	 * @param createtime the time of the last update
	 */
	protected void setUpdatetime(Date createtime) {
		this.updatetime.setPayload(createtime);
	}

	/**
	 * @param uniqueidentified sets the dependent property UniqueIdentified (general
	 *                         loading mechanism)
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * @param storedobject sets the dependent property StoredObject (general loading
	 *                     mechanism)
	 */
	public void setDependentPropertyStoredobject(Storedobject<E> storedobject) {
		this.storedobject = storedobject;

	}

	/**
	 * performs the pre-processing before object insert
	 * 
	 * @param object object to process
	 */
	public void preprocStoredobjectInsert(E object) {
		preprocUniqueidentifiedUpdate(object);

	}

	/**
	 * performs the pre-processing before object update
	 * 
	 * @param object object to process
	 */
	public void preprocUniqueidentifiedUpdate(E object) {
		if (!datesetbyscript)
			this.setUpdatetime(new Date());
		DataObjectId<Appuser> userid = OLcServer.getServer().getCurrentUserId();
		if (usersetbyscript)
			userid = null;
		if (userid != null) {
			this.setUpdateuserid(userid.getId());
		} else {
			// creation outside of server context, putting admin
			Appuser[] admin = Appuser.getobjectbynumber("admin");
			if (admin.length == 1) {
				this.setUpdateuserid(admin[0].getId().getId());
			} else {
				// admin = null, this only happens when creating admin
				@SuppressWarnings("unchecked")
				Uniqueidentified<Appuser> uniqueidentified = (Uniqueidentified<Appuser>) this.parentpayload
						.lookupPropertyOnName("UNIQUEIDENTIFIED");
				this.setUpdateuserid(uniqueidentified.getId().getId());
			}
		}
	}

	/**
	 * performs treatment by batch before massive update. This does not require
	 * access to the database, so it is fast
	 * 
	 * @param objectbatch    the list of object
	 * @param updatelogbatch the list of their update log properties
	 */
	public static <E extends DataObject<E>> void preprocUniqueidentifiedUpdate(E[] objectbatch,
			Updatelog<E>[] updatelogbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (updatelogbatch == null)
			throw new RuntimeException("creationlog batch is null");
		if (objectbatch.length != updatelogbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with creationlog batch length " + updatelogbatch.length);
		// not optimizing for now as the code needs no access to database
		Date currentdate = new Date();
		DataObjectId<Appuser> userid = OLcServer.getServer().getCurrentUserId();
		if (userid == null) {
			Appuser[] admins = Appuser.getobjectbynumber("admin");
			if (admins.length == 1)
				userid = admins[0].getId();
			if (userid == null)
				throw new RuntimeException("Transaction does not have a userid and admin is not reachable");
		}
		for (int i = 0; i < updatelogbatch.length; i++) {
			Updatelog<E> updatelog = updatelogbatch[i];
			updatelog.setUpdatetime(currentdate);
			updatelog.setUpdateuserid(userid.getId());
		}
	}

	/**
	 * performs treatment by batch before massive insert. This does not require
	 * access to the database, so it is fast
	 * 
	 * @param objectbatch    the list of object
	 * @param updatelogbatch the list of their update log properties
	 */
	public static <E extends DataObject<E>> void preprocStoredobjectInsert(E[] objectbatch,
			Updatelog<E>[] updatelogbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (updatelogbatch == null)
			throw new RuntimeException("creationlog batch is null");
		if (objectbatch.length != updatelogbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with creationlog batch length " + updatelogbatch.length);
		// not optimizing for now as the code needs no access to database
		preprocUniqueidentifiedUpdate(objectbatch, updatelogbatch);
	}

}
