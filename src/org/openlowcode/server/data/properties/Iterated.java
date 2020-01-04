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

import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.storage.StoredField;

/**
 * If an object is iterated, a copy of any update is stored. The latest
 * iteration is distinguished by a flag, and is used for most purposes. When
 * using the iterated property, data storage volume should be taken into
 * consideration
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class Iterated<E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedInterface<E>>
		extends DataObjectProperty<E> {
	private Uniqueidentified<E> uniqueidentified;
	private StoredField<Integer> iteration;
	private StoredField<String> latest;
	private StoredField<String> updatenote;
	private static Logger logger = Logger.getLogger(Iterated.class.getName());

	/**
	 * @param definition
	 * @param parentpayload
	 * @throws GalliumException
	 */
	@SuppressWarnings("unchecked")
	public Iterated(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		iteration = (StoredField<Integer>) this.field.lookupOnName("ITERATION");
		latest = (StoredField<String>) this.field.lookupOnName("LATEST");
		updatenote = (StoredField<String>) this.field.lookupOnName("UPDATENOTE");
	}

	/**
	 * sets the dependent property unique identified
	 * 
	 * @param uniqueidentified unique identified property
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

	/**
	 * gets the iteration number. The iteration number is incremented automatically
	 * 
	 * @return the iteration number
	 */
	public Integer getIteration() {
		return this.iteration.getPayload();
	}

	/**
	 * get the latest flag (value 'Y' is iteration is the latest, the one ot be used
	 * for common purpose)
	 * 
	 * @return the latest flag
	 */
	public String getLatest() {
		return this.latest.getPayload();
	}

	/**
	 * initiates the property for a new object, put iteration to 1, and latest to Y
	 * 
	 * @param object the object to process
	 */
	public void preprocStoredobjectInsert(E object) {
		iteration.setPayload(new Integer(1));
		latest.setPayload("Y");
	}

	/**
	 * massive treatement for initiating the property for a new object
	 * 
	 * @param object               batch of object
	 * @param preprociteratedbatch corresponding batch of interated properties
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedInterface<E>> void preprocStoredobjectInsert(
			E[] object, Iterated<E>[] preprociteratedbatch) {
		if (object == null)
			throw new RuntimeException("object array is null, this is not supported");
		if (preprociteratedbatch == null)
			throw new RuntimeException("preprocnamedbatch array is null, this is not supported");
		if (object.length != preprociteratedbatch.length)
			throw new RuntimeException("Object length " + object.length + " is different from iteratedlength"
					+ preprociteratedbatch.length);

		for (int i = 0; i < preprociteratedbatch.length; i++) {
			Iterated<E> thisiterated = preprociteratedbatch[i];
			thisiterated.iteration.setPayload(new Integer(1));
			thisiterated.latest.setPayload("Y");
		}
	}

	/**
	 * when updating, performs the following:<ul>
	 * <li>marks the current iteration as non-latest</li>
	 * <li>creates a new iteration (copy of object)</li>
	 * <li>puts the iteration as latest with new iteration number</li></ul>
	 * @param definition definition of the object
	 * @param object object to proceess
	 */
	public void preprocUniqueidentifiedUpdate(DataObjectDefinition<E> definition, E object) {
		DataObjectId<E> objectid = uniqueidentified.getId();
		E oldobject = UniqueidentifiedQueryHelper.get().readone(objectid, definition, uniqueidentified.getDefinition());
		logger.finer("drop all fields old object " + oldobject.dropToString());
		int nextiteration = (int) oldobject.archivethisiteration();

		latest.setPayload("Y");
		iteration.setPayload(new Integer(nextiteration));
		;
		logger.info("updated iteration to " + nextiteration + " for object id = " + uniqueidentified.getId().getId());

	}

	/**
	 * massive treatment of update for iteration. 
	 * @param definition definition of the object
	 * @param objectbatch batch of object
	 * @param iteratedbatch corresponding batch of iterated properties
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedInterface<E>> void preprocUniqueidentifiedUpdate(
			DataObjectDefinition<E> definition, E[] objectbatch, Iterated<E>[] iteratedbatch) {
		if (objectbatch == null)
			throw new RuntimeException("object array is null, this is not supported");
		if (iteratedbatch == null)
			throw new RuntimeException("preprocnamedbatch array is null, this is not supported");
		if (objectbatch.length != iteratedbatch.length)
			throw new RuntimeException(
					"Object length " + objectbatch.length + " is different from iteratedlength" + objectbatch.length);

		ArrayList<DataObjectId<E>> objectids = new ArrayList<DataObjectId<E>>();
		for (int i = 0; i < objectbatch.length; i++) {
			objectids.add(objectbatch[i].getId());
		}
		DataObjectId<E>[] objectidarray = objectids.toArray(definition.generateIdArrayTemplate());
		UniqueidentifiedDefinition<E> uidefinition = (UniqueidentifiedDefinition<E>) definition
				.getProperty("UNIQUEIDENTIFIED");
		E[] oldobjects = UniqueidentifiedQueryHelper.get().readseveral(objectidarray, definition, uidefinition);
		ArrayList<Iterated<E>> oldobjectsiterated = new ArrayList<Iterated<E>>();
		int[] newiterations = new int[iteratedbatch.length];
		for (int i = 0; i < oldobjects.length; i++) {
			Iterated<E> iteratedpropertyforoldobject = oldobjects[i].getPropertyForObject(iteratedbatch[0]);
			oldobjectsiterated.add(iteratedpropertyforoldobject);
			iteratedpropertyforoldobject.latest.setPayload("N");
			newiterations[i] = iteratedpropertyforoldobject.getIteration().intValue() + 1;
		}
		DataObjectPayload[] payloads = new DataObjectPayload[oldobjects.length];
		for (int i = 0; i < oldobjectsiterated.size(); i++)
			payloads[i] = oldobjectsiterated.get(i).parentpayload;
		DataObjectPayload.massiveinsert(payloads);
		for (int i = 0; i < iteratedbatch.length; i++)
			iteratedbatch[i].iteration.setPayload(new Integer(newiterations[i]));
	}

	/**
	 * archives the current iteration
	 * @param object object to archive
	 * @return the new value of iteration
	 */
	public Integer archivethisiteration(E object) {
		latest.setPayload("N");
		logger.finer("parentpayload before insert" + parentpayload);
		parentpayload.insert();
		logger.info("Archived iteration " + iteration.getPayload() + " for object id = "
				+ uniqueidentified.getId().getId());
		return new Integer(iteration.getPayload().intValue() + 1);
	}

	/**
	 * gets the update note for the iteration. This is entered by the user
	 * @return the update note
	 */
	public String getUpdatenote() {
		return updatenote.getPayload();
	}

	/**
	 * sets the update note for the iteration. This is entered by the user
	 * @param object object to process
	 * @param updatenote update note
	 */
	public void setupdatenote(E object, String updatenote) {
		this.updatenote.setPayload(updatenote);

	}

	/**
	 * gets a blank update note (zero-length string)
	 * @param object the object to process
	 * @return a zero-length string
	 */
	public String getblankupdatenote(E object) {
		return "";
	}

}
