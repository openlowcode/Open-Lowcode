/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import org.openlowcode.module.system.data.Domain;
import org.openlowcode.module.system.data.DomainDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.specificstorage.ExternalField;
import org.openlowcode.server.data.storage.StoredField;

/**
 * Located property of an object. Location is uses a a criteria for user and
 * access rights
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object getting a location
 */
public class Located<E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>>
		extends DataObjectProperty<E> {

	private StoredField<String> locationdomainid;

	@SuppressWarnings("unused")
	private Storedobject<E> storedobject;
	@SuppressWarnings("unused")
	private Uniqueidentified<E> uniqueidentified;
	private ExternalField<String> locationnumber;
	private LocatedDefinition<E> locateddefinition;

	/**
	 * @param definition
	 * @param parentpayload
	 */
	@SuppressWarnings("unchecked")
	public Located(LocatedDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		locationdomainid = (StoredField<String>) this.field.lookupOnName("LOCATIONDOMAINID");
		locationnumber = (ExternalField<String>) this.field.lookupOnName("LOCATEDNUMBER");
		this.locateddefinition = definition;
	}

	/**
	 * @return
	 */
	public DataObjectId<Domain> getLocationdomainid() {
		return new DataObjectId<Domain>(this.locationdomainid.getPayload(), DomainDefinition.getDomainDefinition());
	}

	/**
	 * @param uniqueidentified
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * @param storedobject
	 */
	public void setDependentPropertyStoredobject(Storedobject<E> storedobject) {
		this.storedobject = storedobject;

	}

	/**
	 * @param object
	 * @param domainid
	 */
	public void setlocation(E object, DataObjectId<Domain> domainid) {
		this.locationdomainid.setPayload(domainid.getId());
		object.update();
	}

	/**
	 * @param object
	 */
	public void preprocStoredobjectInsert(E object) {
		this.locationdomainid.setPayload(this.locateddefinition.getLocationHelper().getObjectLocation(object).getId());
	}

	/**
	 * @param object
	 */
	public void preprocUniqueidentifiedUpdate(E object) {
		this.locationdomainid.setPayload(this.locateddefinition.getLocationHelper().getObjectLocation(object).getId());
	}

	/**
	 * @param object
	 * @param locatedbatch
	 */
	public static <E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>> void preprocStoredobjectInsert(
			E[] object, Located<E>[] locatedbatch) {
		for (int i = 0; i < object.length; i++) {
			locatedbatch[i].preprocStoredobjectInsert(object[i]);
		}
	}

	/**
	 * @param object
	 * @param locatedbatch
	 */
	public static <E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>> void preprocUniqueidentifiedUpdate(
			E[] object, Located<E>[] locatedbatch) {
		for (int i = 0; i < object.length; i++) {
			locatedbatch[i].preprocUniqueidentifiedUpdate(object[i]);
		}
	}

	/**
	 * @return
	 */
	public DataObjectId<Domain> getlocation() {
		return this.getLocationdomainid();
	}

	/**
	 * @return
	 */
	public String getLocatednr() {

		return this.locationnumber.getPayload();
	}

}
