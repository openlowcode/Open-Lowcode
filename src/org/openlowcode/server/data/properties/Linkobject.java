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


import java.util.logging.Logger;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkObject;
import org.openlowcode.server.data.specificstorage.ExternalField;
import org.openlowcode.server.data.storage.StoredField;

/**
 * This property will make the parent data object a 'Link' between two other
 * data objects. The link has a left object and a right object. The link
 * generally belongs to the left object for all relevant purposes (access
 * right...)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the link object
 * @param <F> the "left" object of the link
 * @param <G> the "right" object of the link
 */
public class Linkobject<E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
		extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Linkobject.class.getName());
	private DataObjectDefinition<F> leftobjectdefinition;
	private DataObjectDefinition<G> rightobjectdefinition;

	private StoredField<String> lfid;
	private ExternalField<String> linkobjectleftobjectname;
	private ExternalField<String> linkobjectleftobjectnr;

	private StoredField<String> rgid;
	private ExternalField<String> linkobjectrightobjectname;
	private ExternalField<String> linkobjectrightobjectnr;

	private LinkobjectDefinition<E, F, G> linkobjectdefinition;
	Uniqueidentified<E> uniqueidentified;

	/**
	 * creates a link object property
	 * 
	 * @param definition            definition of the left object
	 * @param parentpayload         payload of the parent data object
	 * @param leftobjectdefinition  definition of the left object for the link
	 * @param rightobjectdefinition definition of the right object for the link
	 */
	@SuppressWarnings("unchecked")
	public Linkobject(LinkobjectDefinition<E, F, G> definition, DataObjectPayload parentpayload,
			DataObjectDefinition<F> leftobjectdefinition, DataObjectDefinition<G> rightobjectdefinition) {
		super(definition, parentpayload);
		this.leftobjectdefinition = leftobjectdefinition;
		this.rightobjectdefinition = rightobjectdefinition;
		this.linkobjectdefinition = definition;
		lfid = (StoredField<String>) this.field.lookupOnName("LFID");
		if (leftobjectdefinition.hasProperty("NAMED")) {
			linkobjectleftobjectname = (ExternalField<String>) this.field.lookupOnName("LINKOBJECTLEFTNAME");
		}
		if (leftobjectdefinition.hasProperty("NUMBERED")) {
			linkobjectleftobjectnr = (ExternalField<String>) this.field.lookupOnName("LINKOBJECTLEFTNR");
		}

		rgid = (StoredField<String>) this.field.lookupOnName("RGID");
		if (rightobjectdefinition.hasProperty("NAMED")) {
			linkobjectrightobjectname = (ExternalField<String>) this.field.lookupOnName("LINKOBJECTRIGHTNAME");
		}
		if (rightobjectdefinition.hasProperty("NUMBERED")) {
			linkobjectrightobjectnr = (ExternalField<String>) this.field.lookupOnName("LINKOBJECTRIGHTNR");
		}

	}

	/**
	 * sets the id of the left object (no control done)
	 * 
	 * @param lfid id of the left object
	 */
	protected void setLfid(String lfid) {
		this.lfid.setPayload(lfid);
	}

	/**
	 * gets the id of the left object
	 * 
	 * @return the id of the left object
	 */
	public DataObjectId<F> getLfid() {
		return new DataObjectId<F>(this.lfid.getPayload(), leftobjectdefinition);
	}

	/**
	 * sets the id of the right object (no control done)
	 * 
	 * @param rgid id of the right object
	 */
	protected void setRgid(String rgid) {
		this.rgid.setPayload(rgid);
	}

	/**
	 * gets the id of the right object
	 * 
	 * @return the id of the right object
	 */
	public DataObjectId<G> getRgid() {
		return new DataObjectId<G>(this.rgid.getPayload(), rightobjectdefinition);
	}

	/**
	 * gets the name of the right object (if the right object has the 'Named'
	 * property
	 * 
	 * @return the object name of the right object
	 */
	public String getLinkobjectrightobjectname() {
		return (String) this.linkobjectrightobjectname.getPayload();
	}

	/**
	 * gets the name of the left object (if the left object has the 'Named' property
	 * 
	 * @return the object name of the left object
	 */
	public String getLinkobjectleftobjectname() {
		return (String) this.linkobjectleftobjectname.getPayload();
	}

	/**
	 * gets the number of the right object (if the right object has the 'Numbered'
	 * property
	 * 
	 * @return the object number of the right object
	 */
	public String getLinkobjectrightnr() {
		return (String) this.linkobjectrightobjectnr.getPayload();
	}

	/**
	 * gets the number of the left object (if the left object has the 'Numbered'
	 * property
	 * 
	 * @return the object number of the left object
	 */
	public String getLinkobjectleftnr() {
		return (String) this.linkobjectleftobjectnr.getPayload();
	}

	/**
	 * checks that the link corresponds to the conditions
	 * 
	 * @param condition condition to check
	 * @return null if OK, error message if problem
	 */
	private String checkConditionOnLinkObject(ConstraintOnLinkObject<F, G> condition) {
		logger.fine("LinkObject --------------- Check condition on link ----------- condition " + condition);

		if (condition == null)
			return "Condition is Null";
		if (this.lfid.getPayload() == null)
			return null;
		if (this.lfid.getPayload().length() == 0)
			return null;
		if (this.rgid.getPayload() == null)
			return null;
		if (this.rgid.getPayload().length() == 0)
			return null;

		boolean checkok = condition.checklinkvalid(getLfid(), getRgid());
		if (!checkok) {
			String errormessage = condition.getInvalidLinkErrorMessage(getLfid().lookupObject(),
					getRgid().lookupObject());
			return errormessage;
		}
		return null;
	}

	/**
	 * checks that the link can be created, going through all conditions, and the
	 * 'Max One link from left' parameter)
	 * 
	 * @return null if OK, or the error message if problem
	 */
	public String checkConditionOnLinkObject() {

		for (int i = 0; i < linkobjectdefinition.getConstraintOnLinkObjectNumber(); i++) {
			ConstraintOnLinkObject<F, G> condition = linkobjectdefinition.getConstraintOnLinkObject(i);
			String checkcondition = checkConditionOnLinkObject(condition);
			if (checkcondition != null)
				return checkcondition;
		}
		if (linkobjectdefinition.isMaxOneLinkFromLeft())
			if (!linkobjectdefinition.isReplaceifmorethanonefromleft()) {
				DataObject<E>[] otherlinks = LinkobjectQueryHelper.get().getalllinksfromleftid(getLfid(), null,
						linkobjectdefinition.getParentObject(), leftobjectdefinition, rightobjectdefinition,
						linkobjectdefinition);
				if (otherlinks != null)
					if (otherlinks.length > 0)
						return "There are " + otherlinks.length + " other link(s) for the left object";
			}
		if (linkobjectdefinition.isUniqueForLeftAndRight())
			if (!linkobjectdefinition.isReplaceIfNotUniqueForLeftAndRight()) {
				DataObject<E>[] otherlinks = LinkobjectQueryHelper.get().getalllinksfromleftandrightid(getLfid(),
						getRgid(), null, linkobjectdefinition.getParentObject(), leftobjectdefinition,
						rightobjectdefinition, linkobjectdefinition);
				if (otherlinks != null)
					if (otherlinks.length > 0)
						return "There are " + otherlinks.length + " other link(s) with same left and right object";
			}
		return null;

	}

	/**
	 * checks if the link is valid and set the left object for this link
	 * 
	 * @param object       parent object of the link
	 * @param leftobjectid id of the left object for the link
	 */
	public void setleftobject(E object, DataObjectId<F> leftobjectid) {
		this.setLfid(leftobjectid.getId());
		String error = checkConditionOnLinkObject();
		if (error != null)
			throw new RuntimeException(error);
	}

	/**
	 * checks if the link is valid and set the right object for this link
	 * 
	 * @param object        parent object of the link
	 * @param rightobjectid id of the right object for the link
	 */
	public void setrightobject(E object, DataObjectId<G> rightobjectid) {
		this.setRgid(rightobjectid.getId());
		String error = checkConditionOnLinkObject();
		if (error != null)
			throw new RuntimeException(error);
	}

	/**
	 * sets the dependent property unique identified
	 * 
	 * @param uniqueidentified dependent property
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * performs post processing on the parent object insertion in persistence store.
	 * This may include replacing links that this link overrides
	 * 
	 * @param object object to process
	 */
	public void postprocStoredobjectInsert(E object) {
		if (linkobjectdefinition.isMaxOneLinkFromLeft())
			if (linkobjectdefinition.isReplaceifmorethanonefromleft()) {
				E[] otherlinks = LinkobjectQueryHelper.get().getalllinksfromleftid(getLfid(), null,
						linkobjectdefinition.getParentObject(), leftobjectdefinition, rightobjectdefinition,
						linkobjectdefinition);

				for (int i = 0; i < otherlinks.length; i++) {
					E otherlink = otherlinks[i];
					if (!otherlink.getId().equals(object.getId())) {
						otherlink.delete();
					}
				}
			}
		if (linkobjectdefinition.isUniqueForLeftAndRight())
			if (linkobjectdefinition.isReplaceIfNotUniqueForLeftAndRight()) {
				E[] otherlinks = LinkobjectQueryHelper.get().getalllinksfromleftandrightid(getLfid(), getRgid(), null,
						linkobjectdefinition.getParentObject(), leftobjectdefinition, rightobjectdefinition,
						linkobjectdefinition);

				for (int i = 0; i < otherlinks.length; i++) {
					E otherlink = otherlinks[i];
					if (!otherlink.getId().equals(object.getId())) {
						otherlink.delete();
					}
				}
			}
	}

	/**
	 * performs post processing on the parent object insertion in persistence store.
	 * This may include replacing links that this link overrides. This is not
	 * optimized for batch treatements yet.
	 * 
	 * @param objectbatch     the batch of objects to review
	 * @param linkobjectbatch the corresponding batch of link object properties
	 */
	public static <E extends DataObject<E> & LinkobjectInterface<E, F, G>, F extends DataObject<F> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> void postprocStoredobjectInsert(
			E[] objectbatch, Linkobject<E, F, G>[] linkobjectbatch) {
		logger.info("warning - massive postprocessing not optimized --");
		for (int i = 0; i < objectbatch.length; i++) {
			linkobjectbatch[i].postprocStoredobjectInsert(objectbatch[i]);
		}

	}

}
