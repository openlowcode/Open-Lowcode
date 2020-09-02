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

import org.openlowcode.module.system.data.Domain;
import org.openlowcode.module.system.data.DomainDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.properties.constraints.LinkedToDefaultParent;
import org.openlowcode.server.data.specificstorage.ExternalField;
import org.openlowcode.server.data.storage.StoredField;

/**
 * The property to link an object to a parent. When this property is set, it is
 * compulsory to link the object, from creation to an existing parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> this data object
 * @param <F> the data object of the parent for linkedToParent relationship
 */
public class Linkedtoparent<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F>>
		extends DataObjectProperty<E> {
	private static Logger logger = Logger.getLogger(Linkedtoparent.class.getName());
	private DataObjectDefinition<F> parentdefinition;
	private StoredField<String> pridfield;
	private LinkedtoparentDefinition<E, F> linkedtoparentdefinition;
	@SuppressWarnings("unused")
	private Uniqueidentified<E> uniqueidentified;
	private ExternalField<String> prname;
	private ExternalField<String> prnumber;
	private ExternalField<String> prlocation;

	/**
	 * Creates a LinkedToParent property for this object
	 * 
	 * @param definition       definition of the linked to parent
	 * @param parentpayload    payload of the current data object
	 * @param parentdefinition definition of the parent data object for
	 *                         LinkedToParent relationship
	 */
	@SuppressWarnings("unchecked")
	public Linkedtoparent(LinkedtoparentDefinition<E, F> definition, DataObjectPayload parentpayload,
			DataObjectDefinition<F> parentdefinition) {
		super(definition, parentpayload);
		this.linkedtoparentdefinition = definition;
		this.parentdefinition = parentdefinition;
		pridfield = (StoredField<String>) this.field.lookupOnName(this.getName() + "ID");

		if (parentdefinition.hasProperty("NAMED")) {
			prname = (ExternalField<String>) this.field.lookupOnName(this.getName() + "NAME");
		}
		if (parentdefinition.hasProperty("NUMBERED")) {
			prnumber = (ExternalField<String>) this.field.lookupOnName(this.getName() + "NR");
		}
		if (parentdefinition.hasProperty("Located")) {
			prlocation = (ExternalField<String>) this.field.lookupOnName(this.getName() + "LOCATIONDOMAINID");
		}

	}

	/**
	 * performs pre-processing of the object before insert. if a default parent is
	 * defined, the object will get the default parent
	 * 
	 * @param object parent data object
	 */
	public void preprocStoredobjectInsert(E object) {
		boolean parentidpresent = false;
		if (this.pridfield.getPayload() != null)
			if (this.pridfield.getPayload().length() > 0)
				parentidpresent = true;
		if (!parentidpresent) {
			LinkedToDefaultParent<E, F> defaultlink = linkedtoparentdefinition.getLinkedToDefaultParentRule();
			if (defaultlink != null)
				defaultlink.processBeforeInsert(object);
		}

	}

	/**
	 * massive version of the pre-processing before insert
	 * 
	 * @param objectbatch         batch of object
	 * @param linkedtoparentbatch corresponding batch of linkedtoparent properties
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & UniqueidentifiedInterface<F>> void preprocStoredobjectInsert(
			E[] objectbatch, Linkedtoparent<E, F>[] linkedtoparentbatch) {
		if (objectbatch == null)
			throw new RuntimeException("null object array not supported");
		if (linkedtoparentbatch == null)
			throw new RuntimeException("null property array not supported");
		if (objectbatch.length != linkedtoparentbatch.length)
			throw new RuntimeException("object batch length = " + objectbatch.length
					+ " is different from linkedtoparent batch length = " + linkedtoparentbatch.length);
		for (int i = 0; i < objectbatch.length; i++) {
			E thisobject = objectbatch[i];
			Linkedtoparent<E, F> linkedtoparent = linkedtoparentbatch[i];
			boolean parentidpresent = false;
			if (linkedtoparent.pridfield.getPayload() != null)
				if (linkedtoparent.pridfield.getPayload().length() > 0)
					parentidpresent = true;
			if (!parentidpresent) {
				LinkedToDefaultParent<E, F> defaultlink = linkedtoparent.linkedtoparentdefinition
						.getLinkedToDefaultParentRule();
				if (defaultlink != null)
					defaultlink.processBeforeInsert(thisobject);
			}
		}
	}

	/**
	 * sets the id of the parent
	 * 
	 * @param prid id dof the parent
	 */
	protected void setPrid(DataObjectId<F> prid) {
		this.pridfield.setPayload(prid.getId());

	}

	/**
	 * gets the id of the parent object
	 * 
	 * @return id of the parent object
	 */
	public DataObjectId<F> getId() {
		return new DataObjectId<F>(this.pridfield.getPayload(), parentdefinition);
	}

	/**
	 * generates an update note when changing parent if object is iterated, with as
	 * much information from the parent as possible (i.e. number...)
	 * 
	 * @param object      current object
	 * @param oldparentid old parent id
	 * @param newparentid new parent id
	 * @return the update note.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String generateUpdateNote(E object, DataObjectId<F> oldparentid, DataObjectId<F> newparentid) {
		StringBuffer buffer = new StringBuffer("Changed parent ");
		if ((this.linkedtoparentdefinition.getReferenceObjectDefinition().hasProperty("NUMBERED"))
				|| (this.linkedtoparentdefinition.getReferenceObjectDefinition().hasProperty("NAMED"))) {
			if (oldparentid != null) {
				F oldparent = HasidQueryHelper.get().readone(oldparentid, parentdefinition,
						linkedtoparentdefinition.getGenericsParentobjectforlinkProperty().getDependentDefinitionHasid());
				if (oldparent != null) {
					buffer.append(" from");
					if (this.linkedtoparentdefinition.getReferenceObjectDefinition().hasProperty("NUMBERED")) {
						NumberedInterface<F> numbered = (NumberedInterface) oldparent;
						buffer.append(" '" + numbered.getNr() + "'");
					}
					if (this.linkedtoparentdefinition.getReferenceObjectDefinition().hasProperty("NAMED")) {

						NamedInterface<F> named = (NamedInterface) oldparent;
						buffer.append(" '" + named.getObjectname() + "'");
					}
				}
			}
			F newparent = HasidQueryHelper.get().readone(newparentid, parentdefinition,
					linkedtoparentdefinition.getGenericsParentobjectforlinkProperty().getDependentDefinitionHasid());
			buffer.append(" to");
			if (this.linkedtoparentdefinition.getReferenceObjectDefinition().hasProperty("NUMBERED")) {
				NumberedInterface numbered = (NumberedInterface) newparent;
				buffer.append(" '" + numbered.getNr() + "'");
			}
			if (this.linkedtoparentdefinition.getReferenceObjectDefinition().hasProperty("NAMED")) {
				NamedInterface named = (NamedInterface) newparent;
				buffer.append(" '" + named.getObjectname() + "'");
			}
		}

		return buffer.toString();
	}

	/**
	 * sets the parent for the object. This performs an update of the object
	 * 
	 * @param object   object
	 * @param parentid if of the parent to set.
	 */
	public void setparent(E object, DataObjectId<F> parentid) {
		DataObjectId<F> oldparentid = this.getId();
		this.pridfield.setPayload(parentid.getId());
		if (this.linkedtoparentdefinition.getParentObject().hasProperty("ITERATED")) {
			@SuppressWarnings("unchecked")
			IteratedInterface<F> iterated = (IteratedInterface<F>) object;
			iterated.setupdatenote(generateUpdateNote(object, oldparentid, parentid));
		}
		object.update();

	}

	/**
	 * gets the parent object if set
	 * 
	 * @param object current object
	 * @return parent object if it exists
	 */
	public F getparent(E object) {
		if (this.pridfield.getPayload() == null)
			return null;
		if (this.pridfield.getPayload().length() == 0)
			return null;
		return HasidQueryHelper.get().readone(
				new DataObjectId<F>(this.pridfield.getPayload(), parentdefinition), parentdefinition,
				linkedtoparentdefinition.getGenericsParentobjectforlinkProperty().getDependentDefinitionHasid());
	}

	/**
	 * gets the name of the parent (if it exists)
	 * 
	 * @return name of the parent
	 */
	public String getLinkedtoparentobjectname() {
		if (parentdefinition.hasProperty("NAMEDPROPERTY")) {

			for (int i = 0; i < this.field.getSize(); i++)
				logger.info("field = " + this.field.get(i).getName());
		}
		if (prname == null)
			return "#Name not present#";
		return prname.getPayload();
	}

	/**
	 * gest the number of the parent if it exists)
	 * 
	 * @return the number of parent
	 */
	public String getLinkedtoparentnr() {
		if (prnumber == null)
			return "#Number not present#";
		return prnumber.getPayload();
	}

	/**
	 * gets the domain id of the parent if it exists
	 * 
	 * @return the domain id of the parent
	 */
	public DataObjectId<Domain> getLinkedtoparentlocationdomainid() {
		if (prlocation == null)
			return null;
		return new DataObjectId<Domain>(prlocation.getPayload(), DomainDefinition.getDomainDefinition());
	}

	/**
	 * sets the dependent property unique identified
	 * 
	 * @param uniqueidentified dependent property unique identified
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * sets the parent without performing an update
	 * 
	 * @param object   data object
	 * @param parentid parent id
	 */
	public void setparentwithoutupdate(E object, DataObjectId<F> parentid) {
		this.pridfield.setPayload(parentid.getId());
	}

}
