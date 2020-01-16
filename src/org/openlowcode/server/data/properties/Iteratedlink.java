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

import java.util.ArrayList;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.storage.StoredField;

/**
 * The property specifying that a link is iterated. This is the case when the
 * left object is iterated. In that case, the link stored the first iteration
 * and last iteration of the left object for which the link is valid. This is an
 * efficient way to store link evolution. Any change in link is also archived as
 * an iteration on the left object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the link object
 * @param <F> the "left" object of the link
 * @param <G> the "right" object of the link
 */
public class Iteratedlink<E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>, F extends DataObject<F> & IteratedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
		extends DataObjectProperty<E> {
	private StoredField<Integer> lffirstiter;
	private StoredField<Integer> lflastiter;
	private Linkobject<E, F, G> linkobject;
	private IteratedlinkDefinition<E, F, G> casteddefinition;

	/**
	 * creates an iterated link property on this link data object
	 * 
	 * @param definition    definition of the iterated link property
	 * @param parentpayload parent data object payload
	 */
	@SuppressWarnings("unchecked")
	public Iteratedlink(IteratedlinkDefinition<E, F, G> definition, DataObjectPayload parentpayload) {
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
				+ " with " + casteddefinition.getLinkobject().getRightObjectDefinition().getLabel() + "");
		if ((this.casteddefinition.getRightobjectdef().hasProperty("NUMBERED"))
				|| (this.casteddefinition.getRightobjectdef().hasProperty("NAMED"))) {
			DataObjectId<G> rightobjectid = linkobject.getRgid();
			G rightobject = UniqueidentifiedQueryHelper.get().readone(rightobjectid,
					casteddefinition.getRightobjectdef(), casteddefinition.getRightObjectUniqueidentified());
			if (rightobject == null) {
				updatenote.append(" flat file loader with object not yet loaded");
			} else {
				if (this.casteddefinition.getRightobjectdef().hasProperty("NUMBERED")) {
					@SuppressWarnings("rawtypes")
					NumberedInterface rightobjectnumbered = (NumberedInterface) rightobject;
					updatenote.append(" number '" + rightobjectnumbered.getNr() + "'");
				}
				if (this.casteddefinition.getRightobjectdef().hasProperty("NAMED")) {
					@SuppressWarnings("rawtypes")
					NamedInterface rightobjectnamed = (NamedInterface) rightobject;
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
	public static <E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>, F extends DataObject<F> & IteratedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>>

			String[] generateMassiveUpdateNote(Iteratedlink<E, F, G>[] preprociteratedlinkbatch, E[] objectbatch,
					String action) {
		ArrayList<StringBuffer> updatenotes = new ArrayList<StringBuffer>();
		for (int i = 0; i < objectbatch.length; i++) {
			updatenotes.add(new StringBuffer(action + " link "
					+ preprociteratedlinkbatch[0].casteddefinition.getParentObject().getLabel() + " with "
					+ preprociteratedlinkbatch[0].casteddefinition.getLinkobject().getRightObjectDefinition().getLabel()
					+ ""));
		}
		if ((preprociteratedlinkbatch[0].casteddefinition.getRightobjectdef().hasProperty("NUMBERED"))
				|| (preprociteratedlinkbatch[0].casteddefinition.getRightobjectdef().hasProperty("NAMED"))) {
			ArrayList<DataObjectId<G>> rightobjectids = new ArrayList<DataObjectId<G>>();
			for (int i = 0; i < objectbatch.length; i++) {
				rightobjectids.add(preprociteratedlinkbatch[i].linkobject.getRgid());
			}
			DataObjectDefinition<G> rightobjectdefinition = preprociteratedlinkbatch[0].casteddefinition
					.getRightobjectdef();
			DataObjectId<G>[] rightobjectidarray = rightobjectids
					.toArray(rightobjectdefinition.generateIdArrayTemplate());
			UniqueidentifiedDefinition<G> rightuidefinition = (UniqueidentifiedDefinition<G>) rightobjectdefinition
					.getProperty("UNIQUEIDENTIFIED");
			G[] rightobjects = UniqueidentifiedQueryHelper.get().readseveral(rightobjectidarray, rightobjectdefinition,
					rightuidefinition);
			for (int i = 0; i < rightobjects.length; i++) {
				G rightobject = rightobjects[i];
				StringBuffer updatenote = updatenotes.get(i);
				if (rightobject == null) {
					updatenote.append(" flat file loader with object not yet loaded");
				} else {
					if (preprociteratedlinkbatch[0].casteddefinition.getRightobjectdef().hasProperty("NUMBERED")) {
						@SuppressWarnings("rawtypes")
						NumberedInterface rightobjectnumbered = (NumberedInterface) rightobject;
						updatenote.append(" number '" + rightobjectnumbered.getNr() + "'");
					}
					if (preprociteratedlinkbatch[0].casteddefinition.getRightobjectdef().hasProperty("NAMED")) {
						@SuppressWarnings("rawtypes")
						NamedInterface rightobjectnamed = (NamedInterface) rightobject;
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
			@SuppressWarnings("rawtypes")
			IteratedInterface iteratedleft = (IteratedInterface) leftobject;
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
				casteddefinition.getLinkobject().getUniqueidentifiedDefinitionForLinkObject());
		oldobject.archivethisiteration(leftobject.getIteration());
		if (leftobject.getDefinitionFromObject().hasProperty("ITERATED")) {
			@SuppressWarnings("rawtypes")
			IteratedInterface iteratedleft = (IteratedInterface) leftobject;
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
			updatenote = generateUpdateNote("Delete");
		}
		commonpreprocForDeleteAndUpdate(definition, object, updatenote);

	}

	/**
	 * sets the dependent property link object
	 * 
	 * @param linkobject dependent property link object
	 */
	public void setDependentPropertyLinkobject(Linkobject<E, F, G> linkobject) {
		this.linkobject = linkobject;

	}

	/**
	 * archive this iteration
	 * 
	 * @param object            data object
	 * @param leftobjectolditer last iteration for which the link was valid
	 *                          (iteration of the left object before the link
	 *                          update)
	 */
	public void archivethisiteration(E object, Integer leftobjectolditer) {
		this.lflastiter.setPayload(leftobjectolditer);
		parentpayload.insert();
	}

	/**
	 * massive version of the archive iteration. It is optimized for massive
	 * treatment
	 * 
	 * @param objectbatch       batch of data objects
	 * @param iteratedlinkbatch corresponding batch of iterated link property
	 * @param leftobjectolditer old iterations of the left object for each data
	 *                          object provided
	 */
	public static <E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>, F extends DataObject<F> & IteratedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> void archivethisiteration(
			E[] objectbatch, Iteratedlink<E, F, G>[] iteratedlinkbatch, Integer[] leftobjectolditer) {
		for (int i = 0; i < objectbatch.length; i++) {
			iteratedlinkbatch[i].lflastiter.setPayload(leftobjectolditer[i]);
		}
		DataObjectPayload[] payloads = new DataObjectPayload[objectbatch.length];
		for (int i = 0; i < iteratedlinkbatch.length; i++) {
			payloads[i] = iteratedlinkbatch[i].parentpayload;
		}
		DataObjectPayload.massiveinsert(payloads);
	}

	/**
	 * massive version of the pre-processing to update link.
	 * 
	 * @param definition               definition of the data object of the link
	 * @param objectbatch              batch of objects
	 * @param preprociteratedlinkbatch batch of iterated link properties
	 */
	public static <E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>, F extends DataObject<F> & IteratedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> void preprocUniqueidentifiedUpdate(
			DataObjectDefinition<E> definition, E[] objectbatch, Iteratedlink<E, F, G>[] preprociteratedlinkbatch) {

		// 0 ---------- archive old iteration

		// 1 - -------- get old parents
		ArrayList<DataObjectId<F>> leftobjectids = new ArrayList<DataObjectId<F>>();
		for (int i = 0; i < objectbatch.length; i++) {
			leftobjectids.add(preprociteratedlinkbatch[i].linkobject.getLfid());
		}
		DataObjectDefinition<F> leftobjectdefinition = preprociteratedlinkbatch[0].casteddefinition
				.getLeftiteratedobjectdef();
		DataObjectId<F>[] leftobjectidarray = leftobjectids.toArray(leftobjectdefinition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<F> leftuidefinition = (UniqueidentifiedDefinition<F>) leftobjectdefinition
				.getProperty("UNIQUEIDENTIFIED");
		F[] leftobjects = UniqueidentifiedQueryHelper.get().readseveral(leftobjectidarray, leftobjectdefinition,
				leftuidefinition);

		// 1 - A put old parent iteration in the last iter
		ArrayList<DataObjectId<E>> objectids = new ArrayList<DataObjectId<E>>();
		DataObjectId<E>[] objectidarray = objectids.toArray(definition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<E> uidefinition = (UniqueidentifiedDefinition<E>) definition
				.getProperty("UNIQUEIDENTIFIED");
		E[] oldobjects = UniqueidentifiedQueryHelper.get().readseveral(objectidarray, definition, uidefinition);
		DataObjectPayload[] oldpayloads = new DataObjectPayload[oldobjects.length];
		for (int i = 0; i < oldobjects.length; i++) {
			Iteratedlink<E, F, G> iteratedlinkforold = oldobjects[i].getPropertyForObject(preprociteratedlinkbatch[0]);
			iteratedlinkforold.lflastiter.setPayload(leftobjects[i].getIteration());
			oldpayloads[i] = iteratedlinkforold.parentpayload;
		}
		DataObjectPayload.massiveinsert(oldpayloads);
		// 2 - generate update note (with access on right object to massify
		if (leftobjectdefinition.hasProperty("ITERATED")) {
			String[] updatenotes = Iteratedlink.generateMassiveUpdateNote(preprociteratedlinkbatch, objectbatch,
					"Update");
			for (int i = 0; i < leftobjects.length; i++) {
				IteratedInterface<F> oldobjectiteratedinterface = (IteratedInterface<F>) leftobjects[i];
				oldobjectiteratedinterface.setupdatenote(updatenotes[i]);
			}
		}
		@SuppressWarnings("unchecked")
		Uniqueidentified<F>[] leftobjectui = new Uniqueidentified[leftobjects.length];
		for (int i = 0; i < leftobjects.length; i++) {
			leftobjectui[i] = leftobjects[i].getUniqueidentiedFromLinkObject(preprociteratedlinkbatch[0].linkobject);
		}
		// 3 - update left object (with id)
		Uniqueidentified.update(leftobjects, leftobjectui);
		for (int i = 0; i < objectbatch.length; i++) {
			int newiteration = leftobjects[i].getIteration();
			preprociteratedlinkbatch[i].lffirstiter.setPayload(new Integer(newiteration));

		}

	}

	/**
	 * batch version of the preprocessing on deleting the link
	 * 
	 * @param definition               definition of the link object
	 * @param objectbatch              batch of objects
	 * @param preprociteratedlinkbatch corresponding batches of iterated link
	 *                                 properties
	 */
	public static <E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>, F extends DataObject<F> & IteratedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> void preprocUniqueidentifiedDelete(
			DataObjectDefinition<E> definition, E[] objectbatch, Iteratedlink<E, F, G>[] preprociteratedlinkbatch) {
		ArrayList<DataObjectId<F>> leftobjectids = new ArrayList<DataObjectId<F>>();
		for (int i = 0; i < objectbatch.length; i++) {
			leftobjectids.add(preprociteratedlinkbatch[i].linkobject.getLfid());
		}
		DataObjectDefinition<F> leftobjectdefinition = preprociteratedlinkbatch[0].casteddefinition
				.getLeftiteratedobjectdef();
		DataObjectId<F>[] leftobjectidarray = leftobjectids.toArray(leftobjectdefinition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<F> leftuidefinition = (UniqueidentifiedDefinition<F>) leftobjectdefinition
				.getProperty("UNIQUEIDENTIFIED");

		F[] leftobjects = UniqueidentifiedQueryHelper.get().readseveralpotentialexisting(leftobjectidarray,
				leftobjectdefinition, leftuidefinition);
		boolean hasobjects = false;
		// this may need to be improved as it manages correctly
		// only the fact the left objects are either all present
		// or all absent
		for (int i = 0; i < leftobjects.length; i++)
			if (leftobjects[i] != null)
				hasobjects = true;

		if (hasobjects) {
			// 1 - A put old parent iteration in the last iter
			ArrayList<DataObjectId<E>> objectids = new ArrayList<DataObjectId<E>>();
			DataObjectId<E>[] objectidarray = objectids.toArray(definition.generateIdArrayTemplate());
			UniqueidentifiedDefinition<E> uidefinition = (UniqueidentifiedDefinition<E>) definition
					.getProperty("UNIQUEIDENTIFIED");
			E[] oldobjects = UniqueidentifiedQueryHelper.get().readseveral(objectidarray, definition, uidefinition);
			DataObjectPayload[] oldpayloads = new DataObjectPayload[oldobjects.length];
			for (int i = 0; i < oldobjects.length; i++) {
				Iteratedlink<E, F, G> iteratedlinkforold = oldobjects[i]
						.getPropertyForObject(preprociteratedlinkbatch[0]);
				iteratedlinkforold.lflastiter.setPayload(leftobjects[i].getIteration());
				oldpayloads[i] = iteratedlinkforold.parentpayload;
			}
			DataObjectPayload.massiveinsert(oldpayloads);
			// 2 - generate update note (with access on right object to massify
			if (leftobjectdefinition.hasProperty("ITERATED")) {
				String[] updatenotes = Iteratedlink.generateMassiveUpdateNote(preprociteratedlinkbatch, objectbatch,
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
						.getUniqueidentiedFromLinkObject(preprociteratedlinkbatch[0].linkobject);
			}
			// 3 - update left object (with id)
			Uniqueidentified.update(leftobjects, leftobjectui);
			for (int i = 0; i < objectbatch.length; i++) {
				int newiteration = leftobjects[i].getIteration();
				preprociteratedlinkbatch[i].lffirstiter.setPayload(new Integer(newiteration));

			}
		}
	}

	/**
	 * massive version of insert links
	 * 
	 * @param objectbatch              batch of link objects
	 * @param preprociteratedlinkbatch corresponding batch of iterated properties
	 */
	public static <E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>, F extends DataObject<F> & IteratedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>> void preprocStoredobjectInsert(
			E[] objectbatch, Iteratedlink<E, F, G>[] preprociteratedlinkbatch) {
		// 1 - get left object (with id)
		if (objectbatch != null)
			if (objectbatch.length > 0) {
				ArrayList<DataObjectId<F>> leftobjectids = new ArrayList<DataObjectId<F>>();
				for (int i = 0; i < objectbatch.length; i++) {
					leftobjectids.add(preprociteratedlinkbatch[i].linkobject.getLfid());
				}
				DataObjectDefinition<F> leftobjectdefinition = preprociteratedlinkbatch[0].casteddefinition
						.getLeftiteratedobjectdef();
				DataObjectId<F>[] leftobjectidarray = leftobjectids
						.toArray(leftobjectdefinition.generateIdArrayTemplate());
				UniqueidentifiedDefinition<F> leftuidefinition = (UniqueidentifiedDefinition<F>) leftobjectdefinition
						.getProperty("UNIQUEIDENTIFIED");
				F[] leftobjects = UniqueidentifiedQueryHelper.get().readseveral(leftobjectidarray, leftobjectdefinition,
						leftuidefinition);
				// 2 - generate update note (with access on right object to massify
				if (leftobjectdefinition.hasProperty("ITERATED")) {
					String[] updatenotes = Iteratedlink.generateMassiveUpdateNote(preprociteratedlinkbatch, objectbatch,
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
							.getUniqueidentiedFromLinkObject(preprociteratedlinkbatch[0].linkobject);
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

}