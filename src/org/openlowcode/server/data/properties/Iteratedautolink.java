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
 * an iterated auto-link will keep the history of the links on iterations of the
 * object being left of the auto-link
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object holding the auto-link
 * @param <F> data object referenced by the auto-link
 */
public class Iteratedautolink<
		E extends DataObject<E> & AutolinkobjectInterface<E, F> & IteratedautolinkInterface<E, F>,
		F extends DataObject<F> & IteratedInterface<F>>
		extends
		DataObjectProperty<E> {
	private StoredField<Integer> lffirstiter;
	private StoredField<Integer> lflastiter;
	private Autolinkobject<E, F> autolinkobject;
	private IteratedautolinkDefinition<E, F> casteddefinition;

	/**
	 * creates a property Iterated Auto-link
	 * 
	 * @param definition    definition of the iterated auto-link
	 * @param parentpayload payload of the parent object
	 */
	@SuppressWarnings("unchecked")
	public Iteratedautolink(IteratedautolinkDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		lffirstiter = (StoredField<Integer>) this.field.lookupOnName("LFFIRSTITER");
		lflastiter = (StoredField<Integer>) this.field.lookupOnName("LFLASTITER");
		this.casteddefinition = definition;
	}

	/**
	 * @return the first iteration of the left object for which the link is valid
	 */
	public Integer getLffirstiter() {
		return lffirstiter.getPayload();
	}

	/**
	 * @return the last iteration of the left object for which the link is valid
	 */
	public Integer getLflastiter() {
		return lflastiter.getPayload();
	}

	/**
	 * generates an update note when creating or updating a link
	 * 
	 * @param action context action
	 * @return the best update note possible, quoting right object number or name if
	 *         it exists
	 */
	public String generateUpdateNote(String action) {
		StringBuffer updatenote = new StringBuffer(action + " link " + casteddefinition.getParentObject().getLabel()
				+ " with " + casteddefinition.getAutolinkobject().getLinkedObjectDefinition().getLabel() + "");
		if ((this.casteddefinition.getAutolinkobject().getLinkedObjectDefinition().hasProperty("NUMBERED"))
				|| (this.casteddefinition.getAutolinkobject().getLinkedObjectDefinition().hasProperty("NAMED"))) {
			DataObjectId<F> rightobjectid = autolinkobject.getRgid();
			F rightobject = UniqueidentifiedQueryHelper.get().readone(rightobjectid,
					casteddefinition.getAutolinkobject().getLinkedObjectDefinition(),
					casteddefinition.getAutolinkobject().getLinkedObjectUniqueIdentifiedDefinition());
			if (rightobject == null) {
				updatenote.append(" flat file loader with object not yet loaded");
			} else {
				if (this.casteddefinition.getAutolinkobject().getLinkedObjectDefinition().hasProperty("NUMBERED")) {
					NumberedInterface<?> rightobjectnumbered = (NumberedInterface<?>) rightobject;
					updatenote.append(" number '" + rightobjectnumbered.getNr() + "'");
				}
				if (this.casteddefinition.getAutolinkobject().getLinkedObjectDefinition().hasProperty("NAMED")) {
					NamedInterface<?> rightobjectnamed = (NamedInterface<?>) rightobject;
					updatenote.append("  '" + rightobjectnamed.getObjectname() + "'");
				}
			}
		}
		return updatenote.toString();
	}

	/**
	 * massive version of the generate update note. Is optimized for batch treatment
	 * 
	 * @param preprociteratedlinkbatch batch of iterated link propertoes
	 * @param objectbatch              corresponding batch of data object
	 * @param action                   context action to mention in the update note
	 * @return the corresponding array of object notes
	 */
	public static <
			E extends DataObject<E> & AutolinkobjectInterface<E, F> & IteratedautolinkInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> String[] generateMassiveUpdateNote(
					Iteratedautolink<E, F>[] preprociteratedlinkbatch,
					E[] objectbatch,
					String action) {
		ArrayList<StringBuffer> updatenotes = new ArrayList<StringBuffer>();
		for (int i = 0; i < objectbatch.length; i++) {
			updatenotes.add(new StringBuffer(
					action + " link " + preprociteratedlinkbatch[0].casteddefinition.getParentObject().getLabel()
							+ " with " + preprociteratedlinkbatch[0].casteddefinition.getAutolinkobject()
									.getLinkedObjectDefinition().getLabel()
							+ ""));
		}
		if ((preprociteratedlinkbatch[0].casteddefinition.getLeftiteratedobjectdef().hasProperty("NUMBERED"))
				|| (preprociteratedlinkbatch[0].casteddefinition.getLeftiteratedobjectdef().hasProperty("NAMED"))) {
			ArrayList<DataObjectId<F>> rightobjectids = new ArrayList<DataObjectId<F>>();
			for (int i = 0; i < objectbatch.length; i++) {
				rightobjectids.add(preprociteratedlinkbatch[i].autolinkobject.getRgid());
			}
			DataObjectDefinition<
					F> rightobjectdefinition = preprociteratedlinkbatch[0].casteddefinition.getLeftiteratedobjectdef();
			DataObjectId<F>[] rightobjectidarray = rightobjectids
					.toArray(rightobjectdefinition.generateIdArrayTemplate());
			UniqueidentifiedDefinition<F> rightuidefinition = (UniqueidentifiedDefinition<F>) rightobjectdefinition
					.getProperty("UNIQUEIDENTIFIED");
			F[] rightobjects = UniqueidentifiedQueryHelper.get().readseveral(rightobjectidarray, rightobjectdefinition,
					rightuidefinition);
			for (int i = 0; i < rightobjects.length; i++) {
				F rightobject = rightobjects[i];
				StringBuffer updatenote = updatenotes.get(i);
				if (rightobject == null) {
					updatenote.append(" flat file loader with object not yet loaded");
				} else {
					if (preprociteratedlinkbatch[0].casteddefinition.getLeftiteratedobjectdef()
							.hasProperty("NUMBERED")) {
						NumberedInterface<?> rightobjectnumbered = (NumberedInterface<?>) rightobject;
						updatenote.append(" number '" + rightobjectnumbered.getNr() + "'");
					}
					if (preprociteratedlinkbatch[0].casteddefinition.getLeftiteratedobjectdef().hasProperty("NAMED")) {
						NamedInterface<?> rightobjectnamed = (NamedInterface<?>) rightobject;
						updatenote.append("  '" + rightobjectnamed.getObjectname() + "'");
					}
				}
			}
		}
		String[] updatenotereturnarray = new String[objectbatch.length];
		for (int i = 0; i < objectbatch.length; i++)
			updatenotereturnarray[i] = updatenotes.get(i).toString();
		return updatenotereturnarray;
	}

	/**
	 * pre-processing to a link insert. Generates an update note, and takes an
	 * iteration on the left object
	 * 
	 * @param object data object
	 */
	public void preprocStoredobjectInsert(E object) {
		DataObjectId<F> leftobjectid = object.getLfid();
		F leftobject = UniqueidentifiedQueryHelper.get().readone(leftobjectid,
				casteddefinition.getLeftiteratedobjectdef(),
				casteddefinition.getIterateddefinition().getUniqueIdentifiedDefinition());
		if (leftobject.getDefinitionFromObject().hasProperty("ITERATED")) {
			IteratedInterface<?> iteratedleft = (IteratedInterface<?>) leftobject;
			iteratedleft.setupdatenote(generateUpdateNote("Create"));
		}
		leftobject.update();
		int newiteration = leftobject.getIteration();
		lffirstiter.setPayload(new Integer(newiteration));
		lflastiter.setPayload(IteratedlinkDefinition.INFINITY);
	}

	/**
	 * common procedure for delete and update of links. Actually, update is treated
	 * as a delete and create
	 * 
	 * @param definition definition of the link data object
	 * @param object     linked data object
	 * @param updatenote update note
	 */
	public void commonpreprocForDeleteAndUpdate(DataObjectDefinition<E> definition, E object, String updatenote) {
		DataObjectId<F> leftobjectid = object.getLfid();
		F leftobject = UniqueidentifiedQueryHelper.get().readone(leftobjectid,
				casteddefinition.getLeftiteratedobjectdef(),
				casteddefinition.getIterateddefinition().getUniqueIdentifiedDefinition());
		// get old iteration of link and close it;
		DataObjectId<E> objectid = object.getId();
		E oldobject = UniqueidentifiedQueryHelper.get().readone(objectid, definition,
				casteddefinition.getAutolinkobject().getUniqueidentifiedDefinitionForLinkObject());
		oldobject.archivethisiteration(leftobject.getIteration());
		if (leftobject.getDefinitionFromObject().hasProperty("ITERATED")) {
			IteratedInterface<?> iteratedleft = (IteratedInterface<?>) leftobject;

			iteratedleft.setupdatenote(updatenote);
		}
		leftobject.update();
		int newiteration = leftobject.getIteration();
		this.lffirstiter.setPayload(newiteration);
	}

	/**
	 * pre-processing for a delete. Generates an update note and iterates the left
	 * object.
	 * 
	 * @param definition definition of the data object
	 * @param object     data object
	 */
	public void preprocUniqueidentifiedDelete(DataObjectDefinition<E> definition, E object) {
		String updatenote = null;
		if (casteddefinition.getLeftiteratedobjectdef().hasProperty("ITERATED")) {
			updatenote = generateUpdateNote("Delete");
		}
		commonpreprocForDeleteAndUpdate(definition, object, updatenote);
	}

	/**
	 * pre-processing for an update. Generates an update note and iterates the left
	 * object
	 * 
	 * @param definition definition of the data object
	 * @param object     data objet
	 */
	public void preprocUniqueidentifiedUpdate(DataObjectDefinition<E> definition, E object) {
		String updatenote = null;
		if (casteddefinition.getLeftiteratedobjectdef().hasProperty("ITERATED")) {
			updatenote = generateUpdateNote("Update");
		}
		commonpreprocForDeleteAndUpdate(definition, object, updatenote);

	}

	/**
	 * sets the dependent property link object
	 * 
	 * @param linkobject dependent property link object
	 */
	public void setDependentPropertyAutolinkobject(Autolinkobject<E, F> autolinkobject) {
		this.autolinkobject = autolinkobject;

	}

	/**
	 * archive this iteration
	 * 
	 * @param object            data object
	 * @param leftobjectolditer last iteration for which the link was valid
	 *                          (iteration of the left object before the link
	 *                          update)
	 */
	public void archivethisiteration(E object, long leftobjectolditer) {
		this.lflastiter.setPayload(new Integer((int) leftobjectolditer));
		parentpayload.insert();
	}

	/**
	 * massive pre-processign for an update
	 * 
	 * @param definition               definition of the auto-link object
	 * @param objectbatch              batch of auto-link objects
	 * @param preprociteratedlinkbatch corresponding batch of iterated autolink
	 *                                 properties
	 */
	public static <
			E extends DataObject<E> & AutolinkobjectInterface<E, F> & IteratedautolinkInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> void preprocUniqueidentifiedUpdate(
					DataObjectDefinition<E> definition,
					E[] objectbatch,
					Iteratedautolink<E, F>[] preprociteratedlinkbatch) {
		ArrayList<DataObjectId<F>> leftobjectids = new ArrayList<DataObjectId<F>>();
		for (int i = 0; i < objectbatch.length; i++) {
			leftobjectids.add(preprociteratedlinkbatch[i].autolinkobject.getLfid());
		}
		DataObjectDefinition<
				F> leftobjectdefinition = preprociteratedlinkbatch[0].casteddefinition.getLeftiteratedobjectdef();
		DataObjectId<F>[] leftobjectidarray = leftobjectids.toArray(leftobjectdefinition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<F> leftuidefinition = (UniqueidentifiedDefinition<F>) leftobjectdefinition
				.getProperty("UNIQUEIDENTIFIED");
		F[] leftobjects = UniqueidentifiedQueryHelper.get().readseveral(leftobjectidarray, leftobjectdefinition,
				leftuidefinition);
		// 1 - A put old parent iteration in the last iter
		ArrayList<DataObjectId<E>> objectids = new ArrayList<DataObjectId<E>>();
		DataObjectId<E>[] objectidarray = objectids.toArray(definition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<
				E> uidefinition = (UniqueidentifiedDefinition<E>) definition.getProperty("UNIQUEIDENTIFIED");
		E[] oldobjects = UniqueidentifiedQueryHelper.get().readseveral(objectidarray, definition, uidefinition);
		DataObjectPayload[] oldpayloads = new DataObjectPayload[oldobjects.length];
		for (int i = 0; i < oldobjects.length; i++) {
			Iteratedautolink<E, F> iteratedlinkforold = oldobjects[i].getPropertyForObject(preprociteratedlinkbatch[0]);
			iteratedlinkforold.lflastiter.setPayload(leftobjects[i].getIteration());
			oldpayloads[i] = iteratedlinkforold.parentpayload;
		}
		DataObjectPayload.massiveinsert(oldpayloads);
		// 2 - generate update note (with access on right object to massify
		if (leftobjectdefinition.hasProperty("ITERATED")) {
			String[] updatenotes = Iteratedautolink.generateMassiveUpdateNote(preprociteratedlinkbatch, objectbatch,
					"Update");
			for (int i = 0; i < leftobjects.length; i++) {
				IteratedInterface<F> oldobjectiteratedinterface = (IteratedInterface<F>) leftobjects[i];
				oldobjectiteratedinterface.setupdatenote(updatenotes[i]);
			}
		}
		@SuppressWarnings("unchecked")
		Uniqueidentified<F>[] leftobjectui = new Uniqueidentified[leftobjects.length];
		for (int i = 0; i < leftobjects.length; i++) {
			leftobjectui[i] = leftobjects[i]
					.getUniqueidentiedFromAutolinkObject(preprociteratedlinkbatch[0].autolinkobject);
		}
		// 3 - update left object (with id)
		Uniqueidentified.update(leftobjects, leftobjectui);
		for (int i = 0; i < objectbatch.length; i++) {
			int newiteration = leftobjects[i].getIteration();
			preprociteratedlinkbatch[i].lffirstiter.setPayload(new Integer(newiteration));

		}
	}

	/**
	 * massive version of the pre-proccessing or delete
	 * 
	 * @param definition               definition of the auto-link data object
	 * @param objectbatch              batch of auto-link objects
	 * @param preprociteratedlinkbatch corresponding batch of auto-link objects
	 */
	public static <
			E extends DataObject<E> & AutolinkobjectInterface<E, F> & IteratedautolinkInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> void preprocUniqueidentifiedDelete(
					DataObjectDefinition<E> definition,
					E[] objectbatch,
					Iteratedautolink<E, F>[] preprociteratedlinkbatch) {
		ArrayList<DataObjectId<F>> leftobjectids = new ArrayList<DataObjectId<F>>();
		for (int i = 0; i < objectbatch.length; i++) {
			leftobjectids.add(preprociteratedlinkbatch[i].autolinkobject.getLfid());
		}
		DataObjectDefinition<
				F> leftobjectdefinition = preprociteratedlinkbatch[0].casteddefinition.getLeftiteratedobjectdef();
		DataObjectId<F>[] leftobjectidarray = leftobjectids.toArray(leftobjectdefinition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<F> leftuidefinition = (UniqueidentifiedDefinition<F>) leftobjectdefinition
				.getProperty("UNIQUEIDENTIFIED");
		F[] leftobjects = UniqueidentifiedQueryHelper.get().readseveral(leftobjectidarray, leftobjectdefinition,
				leftuidefinition);
		// 1 - A put old parent iteration in the last iter
		ArrayList<DataObjectId<E>> objectids = new ArrayList<DataObjectId<E>>();
		DataObjectId<E>[] objectidarray = objectids.toArray(definition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<
				E> uidefinition = (UniqueidentifiedDefinition<E>) definition.getProperty("UNIQUEIDENTIFIED");
		E[] oldobjects = UniqueidentifiedQueryHelper.get().readseveral(objectidarray, definition, uidefinition);
		DataObjectPayload[] oldpayloads = new DataObjectPayload[oldobjects.length];
		for (int i = 0; i < oldobjects.length; i++) {
			Iteratedautolink<E, F> iteratedlinkforold = oldobjects[i].getPropertyForObject(preprociteratedlinkbatch[0]);
			iteratedlinkforold.lflastiter.setPayload(leftobjects[i].getIteration());
			oldpayloads[i] = iteratedlinkforold.parentpayload;
		}
		DataObjectPayload.massiveinsert(oldpayloads);

		// 2 - generate update note (with access on right object to massify
		if (leftobjectdefinition.hasProperty("ITERATED")) {
			String[] updatenotes = Iteratedautolink.generateMassiveUpdateNote(preprociteratedlinkbatch, objectbatch,
					"Update");
			for (int i = 0; i < leftobjects.length; i++) {
				IteratedInterface<F> oldobjectiteratedinterface = (IteratedInterface<F>) leftobjects[i];
				oldobjectiteratedinterface.setupdatenote(updatenotes[i]);
			}
		}
		@SuppressWarnings("unchecked")
		Uniqueidentified<F>[] leftobjectui = new Uniqueidentified[leftobjects.length];
		for (int i = 0; i < leftobjects.length; i++) {
			leftobjectui[i] = leftobjects[i]
					.getUniqueidentiedFromAutolinkObject(preprociteratedlinkbatch[0].autolinkobject);
		}
		// 3 - update left object (with id)
		Uniqueidentified.update(leftobjects, leftobjectui);
		for (int i = 0; i < objectbatch.length; i++) {
			int newiteration = leftobjects[i].getIteration();
			preprociteratedlinkbatch[i].lffirstiter.setPayload(new Integer(newiteration));

		}

	}

	/**
	 * massive version of the preprocessing for insert
	 * 
	 * @param objectbatch              batch of objects
	 * @param preprociteratedlinkbatch corresponding batch of iterated links
	 */
	public static <
			E extends DataObject<E> & AutolinkobjectInterface<E, F> & IteratedautolinkInterface<E, F>,
			F extends DataObject<F> & IteratedInterface<F>> void preprocStoredobjectInsert(
					E[] objectbatch,
					Iteratedautolink<E, F>[] preprociteratedlinkbatch) {
		// 1 - get left object (with id)
		ArrayList<DataObjectId<F>> leftobjectids = new ArrayList<DataObjectId<F>>();
		for (int i = 0; i < objectbatch.length; i++) {
			leftobjectids.add(preprociteratedlinkbatch[i].autolinkobject.getLfid());
		}
		DataObjectDefinition<
				F> leftobjectdefinition = preprociteratedlinkbatch[0].casteddefinition.getLeftiteratedobjectdef();
		DataObjectId<F>[] leftobjectidarray = leftobjectids.toArray(leftobjectdefinition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<F> leftuidefinition = (UniqueidentifiedDefinition<F>) leftobjectdefinition
				.getProperty("UNIQUEIDENTIFIED");
		F[] leftobjects = UniqueidentifiedQueryHelper.get().readseveral(leftobjectidarray, leftobjectdefinition,
				leftuidefinition);
		// 2 - generate update note (with access on right object to massify
		if (leftobjectdefinition.hasProperty("ITERATED")) {
			String[] updatenotes = Iteratedautolink.generateMassiveUpdateNote(preprociteratedlinkbatch, objectbatch,
					"Create");
			for (int i = 0; i < leftobjects.length; i++) {
				IteratedInterface<F> oldobjectiteratedinterface = (IteratedInterface<F>) leftobjects[i];
				oldobjectiteratedinterface.setupdatenote(updatenotes[i]);
			}
		}
		@SuppressWarnings("unchecked")
		Uniqueidentified<F>[] leftobjectui = new Uniqueidentified[leftobjects.length];
		for (int i = 0; i < leftobjects.length; i++) {
			leftobjectui[i] = leftobjects[i]
					.getUniqueidentiedFromAutolinkObject(preprociteratedlinkbatch[0].autolinkobject);
		}
		// 3 - update left object (with id)
		Uniqueidentified.update(leftobjects, leftobjectui);

		// 4 - set iteratedlink fields (no data access)
		for (int i = 0; i < objectbatch.length; i++) {
			int newiteration = leftobjects[i].getIteration();
			preprociteratedlinkbatch[i].lffirstiter.setPayload(new Integer(newiteration));
			preprociteratedlinkbatch[i].lflastiter.setPayload(IteratedlinkDefinition.INFINITY);
		}

	}

}