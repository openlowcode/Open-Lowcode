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
import org.openlowcode.server.data.properties.constraints.ConstraintOnAutolinkObject;
import org.openlowcode.server.data.specificstorage.ExternalField;
import org.openlowcode.server.data.storage.StoredField;

/**
 * An auto-link is a link between two data objects of the same class.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the data object used as autolink
 * @param <F> the data object being referenced by the autolink
 */
public class Autolinkobject<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & HasidInterface<F>> extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Autolinkobject.class.getName());
	private StoredField<String> lfid;
	private StoredField<String> rgid;
	@SuppressWarnings("unused")
	private Uniqueidentified<E> uniqueidentified;
	private AutolinkobjectDefinition<E, F> autolinkobjectdefinition;
	private DataObjectDefinition<F> linkedobjectdefinition;

	/**
	 * Creates an autolink object property
	 * 
	 * @param definition             definition of the autolink object
	 * @param parentpayload          parent payload of the data object
	 * @param linkedobjectdefinition definition of the object being referenced by
	 *                               the autolink
	 */
	@SuppressWarnings("unchecked")
	public Autolinkobject(AutolinkobjectDefinition<E, F> definition, DataObjectPayload parentpayload,
			DataObjectDefinition<F> linkedobjectdefinition) {
		super(definition, parentpayload);
		lfid = (StoredField<String>) this.field.lookupOnName("LFID");
		rgid = (StoredField<String>) this.field.lookupOnName("RGID");
		this.autolinkobjectdefinition = definition;
		this.linkedobjectdefinition = linkedobjectdefinition;
	}

	/**
	 * sets leftid (left object of the link, id as text)
	 * 
	 * @param lfid left id
	 */
	protected void setLfid(String lfid) {
		this.lfid.setPayload(lfid);
	}

	/**
	 * set rightid (right object of the link, id as text)
	 * 
	 * @param rgid right id
	 */
	protected void setRgid(String rgid) {
		this.rgid.setPayload(rgid);
	}

	/**
	 * specifies the left object for the link
	 * 
	 * @param object       link object
	 * @param leftobjectid id of the left object for the link
	 */
	public void setleftobject(E object, DataObjectId<F> leftobjectid) {
		this.setLfid(leftobjectid.getId());
		String error = checkConditionOnLinkObject();
		if (error != null)
			throw new RuntimeException(error);

	}

	/**
	 * specified the right object for the link
	 * 
	 * @param object        link object
	 * @param rightobjectid id of the right object for the link
	 */
	public void setrightobject(E object, DataObjectId<F> rightobjectid) {
		this.setRgid(rightobjectid.getId());
		String error = checkConditionOnLinkObject();
		if (error != null)
			throw new RuntimeException(error);

	}

	private String checkConditionOnLinkObject(ConstraintOnAutolinkObject<F> condition) {
		logger.info("LinkObject --------------- Check condition on autolink ----------- condition " + condition);

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
	 * return an error message if the link is not valid
	 * 
	 * @return null of link is valid, or an error if link is invalid
	 */
	public String checkConditionOnLinkObject() {

		for (int i = 0; i < autolinkobjectdefinition.getConstraintOnAutolinkObjectNumber(); i++) {
			ConstraintOnAutolinkObject<F> condition = autolinkobjectdefinition.getConstraintOnAutolinkObject(i);
			String checkcondition = checkConditionOnLinkObject(condition);
			if (checkcondition != null)
				return checkcondition;
		}
		return null;

	}

	/**
	 * sets dependent property unique identified
	 * 
	 * @param uniqueidentified dependent property unique identified
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * get right object id
	 * 
	 * @return data object id
	 */
	public DataObjectId<F> getRgid() {
		return new DataObjectId<F>(this.rgid.getPayload(), linkedobjectdefinition);
	}

	/**
	 * get left object id
	 * 
	 * @return data object id
	 */
	public DataObjectId<F> getLfid() {
		return new DataObjectId<F>(this.lfid.getPayload(), linkedobjectdefinition);
	}

	/**
	 * exchanges the left and right field for some specific logic when link is
	 * symetric
	 * 
	 * @param object parent data object
	 */
	@SuppressWarnings("unchecked")
	public void exchangeleftandrightfields(E object) {
		if (!this.autolinkobjectdefinition.isSymetricLink())
			throw new RuntimeException("can only exchange fields for symetric link");
		// exchange ids;
		String exchangeid = this.rgid.getPayload();
		this.rgid.setPayload(this.lfid.getPayload());
		this.lfid.setPayload(exchangeid);

		String linkname = this.definition.getName();
		if (linkedobjectdefinition.hasProperty("NAMED")) {
			String leftobjectname = linkname + "LEFTNAME";
			String rightobjectname = linkname + "RIGHTNAME";
			ExternalField<String> leftnamefield = (ExternalField<String>) this.field.lookupOnName(leftobjectname);
			ExternalField<String> rightnamefield = (ExternalField<String>) this.field.lookupOnName(rightobjectname);
			String exchangename = rightnamefield.getPayload();
			rightnamefield.setPayload(leftnamefield.getPayload());
			leftnamefield.setPayload(exchangename);

		}
		if (linkedobjectdefinition.hasProperty("NUMBERED")) {
			String leftobjectnumber = linkname + "LEFTNR";
			String rightobjectnumber = linkname + "RIGHTNR";
			ExternalField<String> leftnumberfield = (ExternalField<String>) this.field.lookupOnName(leftobjectnumber);
			ExternalField<String> rightnumberfield = (ExternalField<String>) this.field.lookupOnName(rightobjectnumber);
			String exchangenumber = rightnumberfield.getPayload();
			rightnumberfield.setPayload(leftnumberfield.getPayload());
			leftnumberfield.setPayload(exchangenumber);
		}
		if (linkedobjectdefinition.hasProperty("LIFECYCLE")) {
			String leftobjectstate = linkname + "LEFTSTATE";
			String rightobjectstate = linkname + "RIGHTSTATE";
			ExternalField<String> leftstatefield = (ExternalField<String>) this.field.lookupOnName(leftobjectstate);
			ExternalField<String> rightstatefield = (ExternalField<String>) this.field.lookupOnName(rightobjectstate);
			String exchangestate = rightstatefield.getPayload();
			rightstatefield.setPayload(leftstatefield.getPayload());
			leftstatefield.setPayload(exchangestate);
		}

	}

}
