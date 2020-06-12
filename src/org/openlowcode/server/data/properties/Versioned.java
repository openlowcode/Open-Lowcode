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

import java.util.ArrayList;
import java.util.logging.Logger;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.properties.version.AlphanumericVersionScheme;
import org.openlowcode.server.data.properties.version.VersionScheme;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.StoredField;

/**
 * This property gives objects the capacity to be versioned. Each version of the
 * object is stored, and versioned are maintained in a given order.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class Versioned<E extends DataObject<E> & UniqueidentifiedInterface<E> & VersionedInterface<E>>
		extends
		DataObjectProperty<E> {
	private StoredField<String> masteridfield;
	private StoredField<String> lastversionfield;
	private StoredField<String> versionfield;
	private Uniqueidentified<E> uniqueidentified;
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(Versioned.class.getName());
	private VersionScheme versionscheme;
	private VersionedDefinition<E> versioneddefinition;

	@SuppressWarnings("unchecked")
	public Versioned(VersionedDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.versioneddefinition = definition;
		masteridfield = (StoredField<String>) this.field.lookupOnName("MASTERID");
		lastversionfield = (StoredField<String>) this.field.lookupOnName("LASTVERSION");
		versionfield = (StoredField<String>) this.field.lookupOnName("VERSION");
		this.versionscheme = new AlphanumericVersionScheme();
	}

	/**
	 * @param objectbatch
	 * @param versionedarrayforbatch
	 */
	@SuppressWarnings("unchecked")
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E> & VersionedInterface<E>> void initversion(
			E[] objectbatch,
			Versioned<E>[] versionedarrayforbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (versionedarrayforbatch == null)
			throw new RuntimeException("versioned batch is null");
		if (objectbatch.length != versionedarrayforbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with versioned batch length " + objectbatch.length);
		if (objectbatch.length > 0) {
			// ----- batch control
			
			Uniqueidentified<E>[] uniqueidentifiedforobject = new Uniqueidentified[objectbatch.length];
			// ---- initiating data
			for (int i = 0; i < objectbatch.length; i++) {
				versionedarrayforbatch[i].preprocStoredobjectInsert(objectbatch[i]);
				uniqueidentifiedforobject[i] = versionedarrayforbatch[i].uniqueidentified;
			}
			Uniqueidentified.update(objectbatch, uniqueidentifiedforobject);
		}
	}

	/**
	 * @param object
	 */
	public void initversion(E object) {
		this.preprocStoredobjectInsert(object);
		object.update();
		
	}
	
	@SuppressWarnings("unchecked")
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E> & VersionedInterface<E>> E[] revise(
			E[] objectbatch,
			Versioned<E>[] versionedarrayforbatch) {
		// ------------------------------------- control of data size
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (versionedarrayforbatch == null)
			throw new RuntimeException("versioned batch is null");
		if (objectbatch.length != versionedarrayforbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with versioned batch length " + objectbatch.length);
		if (objectbatch.length > 0) {
			// ----- batch control
			for (int i = 0; i < objectbatch.length; i++)
				if (!("Y".equals(versionedarrayforbatch[i].lastversionfield.getPayload())))
					throw new RuntimeException("You can only create new version for last version");
			Uniqueidentified<E>[] uniqueidentifiedforobject = new Uniqueidentified[objectbatch.length];
			// ---- putting old version to latest = "N"
			for (int i = 0; i < objectbatch.length; i++) {
				versionedarrayforbatch[i].lastversionfield.setPayload("N");
				uniqueidentifiedforobject[i] = versionedarrayforbatch[i].uniqueidentified;
			}

			Uniqueidentified.update(objectbatch, uniqueidentifiedforobject);
			ArrayList<E> returnobjects = new ArrayList<E>();
			Versioned<E>[] returnobjectsversioned = (Versioned<E>[]) new Versioned[objectbatch.length];
			for (int i = 0; i < objectbatch.length; i++) {
				E object = objectbatch[i];
				E newobject = object.deepcopy();
				returnobjects.add(newobject);
				returnobjectsversioned[i] = newobject.getPropertyForObject(versionedarrayforbatch[i]);
				returnobjectsversioned[i].masteridfield.setPayload(object.getMasterid().getId());
				returnobjectsversioned[i].versionfield.setPayload(versionedarrayforbatch[i].versionscheme
						.getDefaultRevisionNextVersion(versionedarrayforbatch[i].versionfield.getPayload()));
				returnobjectsversioned[i].lastversionfield.setPayload("Y");
				if (object instanceof NumberedInterface) {
					NumberedInterface<E> objectnumbered = (NumberedInterface<E>) object;
					NumberedInterface<E> newversionobjectnumbered = (NumberedInterface<E>) newobject;
					newversionobjectnumbered.setobjectnumber(objectnumbered.getNr());
				}
				if (object instanceof NamedInterface) {
					NamedInterface<E> objectnamed = (NamedInterface<E>) object;
					NamedInterface<E> newversionobjectnamed = (NamedInterface<E>) newobject;
					newversionobjectnamed.setobjectname(objectnamed.getObjectname());
				}
			}

			E[] returnobjectarray = returnobjects
					.toArray(objectbatch[0].getDefinitionFromObject().generateArrayTemplate());
			objectbatch[0].getMassiveInsert().insert(returnobjectarray);
			return returnobjectarray;
		}
		// if length is zero, returning the same array, that ensures
		// no cast problem for array of zero element
		return objectbatch;
	}

	/**
	 * Forces this object as the latest version. This should be used carefully, and
	 * mostly to repair corrupted data. It does not change the order of versions
	 * between themselves (result of getPreviousVersion() ).
	 * 
	 * @param object provided object
	 */
	public void forceaslatestversion(E object) {
		E currentlastversion = VersionedQueryHelper.get().getlastversion(this.getMasterid(),
				object.getDefinitionFromObject(), this.versioneddefinition);
		if (currentlastversion != null) {
			if (!currentlastversion.getId().getId().equals(object.getId().getId())) {
				Versioned<E> currentlastversionproperty = currentlastversion.getPropertyForObject(this);
				currentlastversionproperty.lastversionfield.setPayload("N");
				currentlastversion.update();
				this.lastversionfield.setPayload("Y");
				object.update();
			}
		} else {
			this.lastversionfield.setPayload("Y");
			object.update();
		}
	}

	/**
	 * creates a new version of the object from the current latest version
	 * 
	 * @param object the object to create the new version for
	 * @return the just created version
	 */
	@SuppressWarnings("unchecked")
	public E revise(E object) {
		if (!("Y".equals(this.lastversionfield.getPayload())))
			throw new RuntimeException("You can only create new version for last version");
		this.lastversionfield.setPayload("N");
		object.update();
		E newversionobject = object.deepcopy();
		Versioned<E> versionedfornewobject = newversionobject.getPropertyForObject(this);
		versionedfornewobject.masteridfield.setPayload(object.getMasterid().getId());
		versionedfornewobject.versionfield
				.setPayload(versionscheme.getDefaultRevisionNextVersion(versionfield.getPayload()));
		versionedfornewobject.lastversionfield.setPayload("Y");
		if (object instanceof NumberedInterface) {
			NumberedInterface<E> objectnumbered = (NumberedInterface<E>) object;
			NumberedInterface<E> newversionobjectnumbered = (NumberedInterface<E>) newversionobject;
			newversionobjectnumbered.setobjectnumber(objectnumbered.getNr());
		}
		if (object instanceof NamedInterface) {
			NamedInterface<E> objectnamed = (NamedInterface<E>) object;
			NamedInterface<E> newversionobjectnamed = (NamedInterface<E>) newversionobject;
			newversionobjectnamed.setobjectname(objectnamed.getObjectname());
		}
		newversionobject.insert();
		return newversionobject;

	}

	/**
	 * massively processes an array of deleted objects
	 * 
	 * @param objectbatch            the batch of object
	 * @param versionedarrayforbatch the corresponding batch of versioned properties
	 */
	public static <
			E extends DataObject<E> & UniqueidentifiedInterface<E> & VersionedInterface<
					E>> void postprocUniqueidentifiedDelete(E[] objectbatch, Versioned<E>[] versionedarrayforbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object batch is null");
		if (versionedarrayforbatch == null)
			throw new RuntimeException("versioned batch is null");
		if (objectbatch.length != versionedarrayforbatch.length)
			throw new RuntimeException("Object batch length " + objectbatch.length
					+ " is not consistent with versioned batch length " + objectbatch.length);
		if (objectbatch.length > 0) {
			for (int i = 0; i < objectbatch.length; i++)
				versionedarrayforbatch[i].postprocUniqueidentifiedDelete(objectbatch[i]);
		}
	}

	/**
	 * processes deleted objects. Mostly, if the object deleted is the latest
	 * version, then previous version becomes the latest
	 * 
	 * @param object the object deleted
	 */
	public void postprocUniqueidentifiedDelete(E object) {
		String previousversion = versionscheme.getDefaultRevisionPreviousVersion(object.getVersion());
		if (previousversion != null)
			if (object.getLastversion().equals("Y")) {
				E[] allversions = VersionedQueryHelper.get().getallversions(object.getMasterid(),
						QueryFilter.get(VersionedQueryHelper.getVersionQueryCondition(
								definition.getParentObject().getAlias(VersionedQueryHelper.singleobjectalias),
								previousversion, definition.getParentObject())),
						definition.getParentObject(), versioneddefinition);
				if (allversions.length > 0) {
					if (allversions.length > 1)
						throw new RuntimeException("More than one version of object " + object.getName() + " "
								+ object.getMasterid() + " " + previousversion);
					E newlastversion = allversions[0];
					Versioned<E> versionedfonewlastversion = newlastversion.getPropertyForObject(this);
					versionedfonewlastversion.lastversionfield.setPayload("Y");
					newlastversion.update();
				}
			}
	}

	/**
	 * gets the previous version of an object
	 * 
	 * @param object the object to start the search on
	 * @return the previous version if present, null else
	 */
	public E getpreviousversion(E object) {
		String previousversion = versionscheme.getDefaultRevisionPreviousVersion(object.getVersion());
		if (previousversion == null)
			return null;
		E[] previousversionarray = VersionedQueryHelper.get().getallversions(object.getMasterid(),
				QueryFilter.get(VersionedQueryHelper.getVersionQueryCondition(
						versioneddefinition.getParentObject().getAlias(VersionedQueryHelper.singleobjectalias),
						previousversion, versioneddefinition.getParentObject())),
				versioneddefinition.getParentObject(), versioneddefinition);
		if (previousversionarray.length == 0)
			return null;
		if (previousversionarray.length == 1)
			return previousversionarray[0];
		throw new RuntimeException("several previous versions are present");
	}

	/**
	 * checks if the object can be revised
	 * 
	 * @param object the object to check
	 * @return true if object can be revised
	 */
	public boolean canberevised(E object) {
		return true;
	}

	/**
	 * pre-treatment of the object before insert
	 * 
	 * @param objectbatch    batch of object
	 * @param versionedbatch corresponding batch of versioned properties
	 */
	public static <
			E extends DataObject<E> & UniqueidentifiedInterface<E> & VersionedInterface<
					E>> void preprocStoredobjectInsert(E[] objectbatch, Versioned<E>[] versionedbatch) {
		// no need for specific batch algorithm as no persistence for the procedure when
		// using id generated with same algorith as UniqueIdentified id
		for (int i = 0; i < versionedbatch.length; i++)
			versionedbatch[i].preprocStoredobjectInsert(objectbatch[i]);
	}

	/**
	 * pre-process the object before inserting. Creates the first version of the
	 * version scheme, the master id, and of course, version just created for the
	 * new object is the latest
	 * 
	 * @param object the object to pre-process
	 */
	public void preprocStoredobjectInsert(E object) {
		// only treats if not filled
		boolean firstversion = true;
		if (versionfield.getPayload() != null)
			if (versionfield.getPayload().length() > 0)
				firstversion = false;
		if (firstversion) {
			lastversionfield.setPayload("Y");
			String id = "2" + Long.toHexString(Uniqueidentified.getNextId());

			// NOTE: due to path algorithm, it is assumed the id only contains letters and
			// figures (especially "[", "]" and "/" are forbidden

			masteridfield.setPayload(id);
			// default logic
			this.versionfield.setPayload(versionscheme.getFirstVersion());
		}
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return versionfield.getPayload();
	}

	/**
	 * @return a string indicator if this version if the latest ( values are 'Y' and
	 *         'N')
	 */
	public String getLastversion() {
		return lastversionfield.getPayload();
	}

	/**
	 * @return the id common to all versions of the object (each version will get
	 *         its own version id)
	 */
	public DataObjectMasterId<E> getMasterid() {
		return new DataObjectMasterId<E>(this.masteridfield.getPayload(), definition.getParentObject());
	}

	/**
	 * sets the dependent property unique identified
	 * 
	 * @param uniqueidentified the dependent property
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}
}
