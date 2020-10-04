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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.storage.StoredField;

/**
 * An iterated subobject is a child of a main object that is considered as part
 * of the data set of the parent data object. Thus, any change on the subobject
 * will result in an iteration of the parent object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> sub object
 * @param <F> parent of the subobject
 */
public class Iteratedsubobject<
		E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedsubobjectInterface<E, F>,
		F extends DataObject<F> & IteratedInterface<F>>
		extends
		DataObjectProperty<E> {
	private StoredField<Integer> prfirstiter;
	private StoredField<Integer> prlastiter;
	private Linkedtoparent<E, F> linkedtoparent;
	private IteratedsubobjectDefinition<E, F> casteddefinition;

	/**
	 * create the iterated sub-object property
	 * 
	 * @param definition    definition of the property
	 * @param parentpayload payload of the parent data object
	 */
	@SuppressWarnings("unchecked")
	public Iteratedsubobject(IteratedsubobjectDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		prfirstiter = (StoredField<Integer>) this.field.lookupOnName("PRFIRSTITER");
		prlastiter = (StoredField<Integer>) this.field.lookupOnName("PRLASTITER");
		this.casteddefinition = definition;
	}

	/**
	 * get the first iteration of the parent on which the current data object
	 * (subobject) is valid
	 * 
	 * @return first iteration of the parent for which the subobject is valid
	 */
	public Integer getPrfirstiter() {
		return prfirstiter.getPayload();
	}

	/**
	 * get the last iteration of the parent on which the current data object
	 * (subobject) is valid
	 * 
	 * @return the last iteration on the parent on which the current data object is
	 *         valid
	 */
	public Integer getPrlastiter() {
		return prlastiter.getPayload();
	}

	/**
	 * generates the best update note possible for the left object when subobject is
	 * created, updated or deleted
	 * 
	 * @param object data object
	 * @param action type of action (will be put in the comment)
	 * @return
	 */
	public String generateUpdateNote(E object, String action) {
		StringBuffer updatenote = new StringBuffer(
				action + " sub-object " + casteddefinition.getParentObject().getLabel() + " ");
		if ((this.casteddefinition.getParentObject().hasProperty("NUMBERED"))
				|| (this.casteddefinition.getParentObject().hasProperty("NAMED"))) {

			if (this.casteddefinition.getParentObject().hasProperty("NUMBERED")) {
				NumberedInterface<?> objectnumbered = (NumberedInterface<?>) object;
				updatenote.append(" number '" + objectnumbered.getNr() + "'");
			}
			if (this.casteddefinition.getParentObject().hasProperty("NAMED")) {
				NamedInterface<?> objectnamed = (NamedInterface<?>) object;
				updatenote.append("  '" + objectnamed.getObjectname() + "'");
			}
		}
		return updatenote.toString();
	}

	/**
	 * pre-processing for the data object insertion. Will create an iteration of the
	 * parent object
	 * 
	 * @param object data object being inserted
	 */
	public void preprocStoredobjectInsert(E object) {

		DataObjectId<F> parentobjectid = linkedtoparent.getId();
		F leftobject = HasidQueryHelper.get().readone(parentobjectid,
				casteddefinition.getParentiteratedobjectdef(),
				casteddefinition.getIterateddefinition().getUniqueIdentifiedDefinition().getDependentDefinitionHasid());
		leftobject.setupdatenote(generateUpdateNote(object, "Create"));
		leftobject.update();
		int newiteration = leftobject.getIteration();
		prfirstiter.setPayload(new Integer(newiteration));
		prlastiter.setPayload(IteratedlinkDefinition.INFINITY);
	}

	/**
	 * common procedure for delete and update of the data-object
	 * 
	 * @param definition definition of the data object
	 * @param object     data object
	 * @param updatetype type of update (update or delete): plain text for logging
	 */
	public void commonpreprocForDeleteAndUpdate(DataObjectDefinition<E> definition, E object, String updatetype) {
		DataObjectId<F> parentobjectid = linkedtoparent.getId();
		F leftobject = HasidQueryHelper.get().readone(parentobjectid,
				casteddefinition.getParentiteratedobjectdef(),
				casteddefinition.getIterateddefinition().getUniqueIdentifiedDefinition().getDependentDefinitionHasid());
		// get old iteration of link and close it;
		DataObjectId<E> objectid = object.getId();
		E oldobject = HasidQueryHelper.get().readone(objectid, definition,
				casteddefinition.getLinkedtoparent().getUniqueidentifiedForThisObject().getDependentDefinitionHasid());
		oldobject.archivethisiteration(leftobject.getIteration());
		leftobject.setupdatenote(generateUpdateNote(object, updatetype));
		leftobject.update();
		int newiteration = leftobject.getIteration();
		this.prfirstiter.setPayload(newiteration);
	}

	/**
	 * pre-processing for delete object. Takes an iteration on the parent object and
	 * archives the old object
	 * 
	 * @param definition definition of the data object
	 * @param object     data object
	 */
	public void preprocUniqueidentifiedDelete(DataObjectDefinition<E> definition, E object) {
		commonpreprocForDeleteAndUpdate(definition, object, "Delete");
	}

	/**
	 * massive version of the pre-processing for delete object
	 * 
	 * @param definition             definition of the data object
	 * @param objectbatch            batch of data object
	 * @param iteratedsubobjectbatch corresponding batch of the iterated sub object
	 *                               property
	 */
	public static <
			E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedsubobjectInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> void preprocUniqueidentifiedDelete(
					DataObjectDefinition<E> definition,
					E[] objectbatch,
					Iteratedsubobject<E, F>[] iteratedsubobjectbatch) {

		if (objectbatch == null)
			throw new RuntimeException("cannot treat null array");
		if (iteratedsubobjectbatch == null)
			throw new RuntimeException("cannot treat null array of linkedfromchildren");
		if (objectbatch.length != iteratedsubobjectbatch.length)
			throw new RuntimeException("iteratedsubobjectbatch Array and Object Array do not have same size");
		if (objectbatch.length > 0) {

			for (int i = 0; i < objectbatch.length; i++) {
				iteratedsubobjectbatch[i].preprocUniqueidentifiedDelete(definition, objectbatch[i]);
			}

		}
	}

	/**
	 * pre-processing for the update object. Takes an iteration on the parent object
	 * and archives the old object, link the updated object to the new iteration
	 * 
	 * @param definition definition of the data object
	 * @param object     data object
	 */
	public void preprocUniqueidentifiedUpdate(DataObjectDefinition<E> definition, E object) {
		commonpreprocForDeleteAndUpdate(definition, object, "Update");

	}

	/**
	 * sets the dependent property linked to parent
	 * 
	 * @param linkedtoparent linked to parent property
	 */
	public void setDependentPropertyLinkedtoparent(Linkedtoparent<E, F> linkedtoparent) {
		this.linkedtoparent = linkedtoparent;

	}

	/**
	 * archive the current sub object
	 * 
	 * @param object            data object
	 * @param leftobjectolditer last iteration of the parent data object for which
	 *                          this subobject is valid
	 */
	public void archivethisiteration(E object, Integer leftobjectolditer) {
		this.prlastiter.setPayload(leftobjectolditer);
		parentpayload.insert();
	}

	/**
	 * massive version of the pre-processing for update
	 * 
	 * @param definition               definition of the data object
	 * @param objectbatch              batch of data object
	 * @param preprociteratedlinkbatch corresponding batch of iterated subobject
	 *                                 properties
	 */
	public static <
			E extends DataObject<E> & IteratedsubobjectInterface<E, F> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & IteratedInterface<F>> void preprocUniqueidentifiedUpdate(
					DataObjectDefinition<E> definition,
					E[] objectbatch,
					Iteratedsubobject<E, F>[] preprociteratedlinkbatch) {
		ArrayList<DataObjectId<F>> parentobjectids = new ArrayList<DataObjectId<F>>();
		for (int i = 0; i < objectbatch.length; i++) {
			parentobjectids.add(preprociteratedlinkbatch[i].linkedtoparent.getId());
		}
		DataObjectDefinition<
				F> parentdefinition = preprociteratedlinkbatch[0].casteddefinition.getParentiteratedobjectdef();
		DataObjectId<F>[] leftobjectidarray = parentobjectids.toArray(parentdefinition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<F> parentuidefinition = (UniqueidentifiedDefinition<F>) parentdefinition
				.getProperty("UNIQUEIDENTIFIED");
		F[] parentobjects = HasidQueryHelper.get().readseveral(leftobjectidarray, parentdefinition,
				parentuidefinition.getDependentDefinitionHasid());
		// 1 - A put old parent iteration in the last iter
		ArrayList<DataObjectId<E>> objectids = new ArrayList<DataObjectId<E>>();
		DataObjectId<E>[] objectidarray = objectids.toArray(definition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<
				E> uidefinition = (UniqueidentifiedDefinition<E>) definition.getProperty("UNIQUEIDENTIFIED");
		E[] oldobjects = HasidQueryHelper.get().readseveral(objectidarray, definition, uidefinition.getDependentDefinitionHasid());
		DataObjectPayload[] oldpayloads = new DataObjectPayload[oldobjects.length];
		for (int i = 0; i < oldobjects.length; i++) {
			Iteratedsubobject<
					E, F> iteratedlinkforold = oldobjects[i].getPropertyForObject(preprociteratedlinkbatch[0]);
			iteratedlinkforold.prlastiter.setPayload(parentobjects[i].getIteration());
			oldpayloads[i] = iteratedlinkforold.parentpayload;
		}
		DataObjectPayload.massiveinsert(oldpayloads);
		// 2 - generate update note (with access on right object to massify
		if (parentdefinition.hasProperty("ITERATED")) {
			String[] updatenotes = Iteratedsubobject.generateMassiveUpdateNote(preprociteratedlinkbatch, objectbatch,
					"Update");
			for (int i = 0; i < parentobjects.length; i++) {
				IteratedInterface<F> oldobjectiteratedinterface = (IteratedInterface<F>) parentobjects[i];
				oldobjectiteratedinterface.setupdatenote(updatenotes[i]);
			}
		}
		@SuppressWarnings("unchecked")
		Uniqueidentified<F>[] leftobjectui = new Uniqueidentified[parentobjects.length];
		for (int i = 0; i < parentobjects.length; i++) {
			leftobjectui[i] = parentobjects[i]
					.getUniqueidentiedFromLinkedToParent(preprociteratedlinkbatch[0].linkedtoparent);
		}
		// 3 - update left object (with id)
		Uniqueidentified.update(parentobjects, leftobjectui);

		// 4 - set iteratedlink fields (no data access)
		for (int i = 0; i < objectbatch.length; i++) {
			int newiteration = parentobjects[i].getIteration();
			preprociteratedlinkbatch[i].prfirstiter.setPayload(new Integer(newiteration));
			preprociteratedlinkbatch[i].prlastiter.setPayload(IteratedlinkDefinition.INFINITY);
		}

	}

	/**
	 * massive version of the pre-processing for insert
	 * 
	 * @param objectbatch              batch of objects
	 * @param preprociteratedlinkbatch corresponding batch of iterated subobject
	 *                                 properties
	 */
	public static <
			E extends DataObject<E> & IteratedsubobjectInterface<E, F> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & IteratedInterface<F>> void preprocStoredobjectInsert(
					E[] objectbatch,
					Iteratedsubobject<E, F>[] preprociteratedlinkbatch) {
		ArrayList<DataObjectId<F>> parentobjectids = new ArrayList<DataObjectId<F>>();
		for (int i = 0; i < objectbatch.length; i++) {
			parentobjectids.add(preprociteratedlinkbatch[i].linkedtoparent.getId());
		}
		DataObjectDefinition<
				F> parentdefinition = preprociteratedlinkbatch[0].casteddefinition.getParentiteratedobjectdef();
		DataObjectId<F>[] leftobjectidarray = parentobjectids.toArray(parentdefinition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<F> parentuidefinition = (UniqueidentifiedDefinition<F>) parentdefinition
				.getProperty("UNIQUEIDENTIFIED");
		F[] parentobjects = HasidQueryHelper.get().readseveral(leftobjectidarray, parentdefinition,
				parentuidefinition.getDependentDefinitionHasid());
		// 2 - generate update note (with access on right object to massify
		if (parentdefinition.hasProperty("ITERATED")) {
			String[] updatenotes = Iteratedsubobject.generateMassiveUpdateNote(preprociteratedlinkbatch, objectbatch,
					"Create");
			for (int i = 0; i < parentobjects.length; i++) {
				IteratedInterface<F> oldobjectiteratedinterface = (IteratedInterface<F>) parentobjects[i];
				oldobjectiteratedinterface.setupdatenote(updatenotes[i]);
			}
		}
		@SuppressWarnings("unchecked")
		Uniqueidentified<F>[] leftobjectui = new Uniqueidentified[parentobjects.length];
		for (int i = 0; i < parentobjects.length; i++) {
			leftobjectui[i] = parentobjects[i]
					.getUniqueidentiedFromLinkedToParent(preprociteratedlinkbatch[0].linkedtoparent);
		}
		// 3 - update left object (with id)
		Uniqueidentified.update(parentobjects, leftobjectui);

		// 4 - set iteratedlink fields (no data access)
		for (int i = 0; i < objectbatch.length; i++) {
			int newiteration = parentobjects[i].getIteration();
			preprociteratedlinkbatch[i].prfirstiter.setPayload(new Integer(newiteration));
			preprociteratedlinkbatch[i].prlastiter.setPayload(IteratedlinkDefinition.INFINITY);
		}

	}

	/**
	 * generates update note in a way optimized for batch treatment
	 * 
	 * @param preprociteratedlinkbatch batch of iterated subobject properties
	 * @param objectbatch              corresponding batch of data objects
	 * @param action                   the action to reference in the update note
	 * @return the best update note possible
	 */
	public static <
			E extends DataObject<E> & IteratedsubobjectInterface<E, F> & UniqueidentifiedInterface<E>,
			F extends DataObject<F> & IteratedInterface<F>>

			String[] generateMassiveUpdateNote(
					Iteratedsubobject<E, F>[] preprociteratedlinkbatch,
					E[] objectbatch,
					String action) {
		// no specific algorithm as there is no persistence
		String[] returnupdatenote = new String[objectbatch.length];
		for (int i = 0; i < objectbatch.length; i++) {
			preprociteratedlinkbatch[i].generateUpdateNote(objectbatch[i], action);
		}
		return returnupdatenote;
	}

}