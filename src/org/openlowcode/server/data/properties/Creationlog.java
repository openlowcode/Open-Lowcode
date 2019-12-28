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
 * A property that adds a log of the user who created the object and the date of
 * creation
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> object the creation log is declared on
 */
public class Creationlog<E extends DataObject<E>> extends DataObjectProperty<E> {

	private StoredField<String> createuserid;
	private StoredField<Date> createtime;

	@SuppressWarnings("unchecked")
	public Creationlog(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		createuserid = (StoredField<String>) this.field.lookupOnName("CREATEUSERID");
		createtime = (StoredField<Date>) this.field.lookupOnName("CREATETIME");
	}

	/**
	 * @return the object id of the application user (Appuser) that created the
	 *         object
	 */
	public DataObjectId<Appuser> getCreateuserid() {
		return new DataObjectId<Appuser>(createuserid.getPayload(), AppuserDefinition.getAppuserDefinition());
	}

	/**
	 * @param createuserid sets as a raw string the userid that created the object
	 */
	void SetCreateuserid(String createuserid) {
		this.createuserid.setPayload(createuserid);

	}

	/**
	 * @return the creation time of the object
	 */
	public Date getCreatetime() {
		return this.createtime.getPayload();
	}

	/**
	 * @param createtime sets the creation time of the object
	 */
	void SetCreatetime(Date createtime) {
		this.createtime.setPayload(createtime);
	}

	/**
	 * @param storedobject gets the dependent property (general Open Lowcode
	 *                     mechanism, but useless in that case)
	 */
	public void setDependentPropertyStoredobject(Storedobject<E> storedobject) {
		// do nothing, stored object is not used

	}

	/**
	 * performs preprocessing of the object before insertion (gets the userid from
	 * the server context and the creation time)
	 * 
	 * @param object data object
	 */
	public void preprocStoredobjectInsert(E object) {
		if (this.createtime.getPayload() == null)
			this.SetCreatetime(new Date());

		DataObjectId<Appuser> userid = OLcServer.getServer().getCurrentUserId();

		if (userid != null) {
			this.SetCreateuserid(userid.getId());
		} else {
			// creation outside of server context, putting admin
			Appuser[] admins = Appuser.getobjectbynumber("admin");
			if (admins.length == 1) {
				this.SetCreateuserid(admins[0].getId().getId());
			} else {
				// admin = null, this only happens when creating admin
				@SuppressWarnings("unchecked")
				Uniqueidentified<Appuser> uniqueidentified = (Uniqueidentified<Appuser>) this.parentpayload
						.lookupPropertyOnName("UNIQUEIDENTIFIED");
				this.SetCreateuserid(uniqueidentified.getId().getId());
			}
		}
	}

	/**
	 * Massive preprocessing before insertion. This does not use a database access
	 * and should be very performant.
	 * 
	 * @param objectbatch
	 * @param creationlogbatch
	 */
	public static <E extends DataObject<E>> void preprocStoredobjectInsert(E[] objectbatch,
			Creationlog<E>[] creationlogbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (creationlogbatch == null)
			throw new RuntimeException("creationlog batch is null");
		if (objectbatch.length != creationlogbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with creationlog batch length " + creationlogbatch.length);

		Date currentdate = new Date();
		DataObjectId<Appuser> userid = OLcServer.getServer().getCurrentUserId();
		if (userid == null) {
			Appuser[] admins = Appuser.getobjectbynumber("admin");
			if (admins.length == 1)
				userid = admins[0].getId();
			if (userid == null)
				throw new RuntimeException("Transaction does not have a userid and admin is not reachable");
		}

		for (int i = 0; i < creationlogbatch.length; i++) {
			Creationlog<E> creationlog = creationlogbatch[i];
			creationlog.SetCreatetime(currentdate);
			creationlog.SetCreateuserid(userid.getId());
		}
	}
}
