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
import org.openlowcode.server.data.storage.QueryFilter;


/**
 * An object is linked from children if another data object is declared as
 * "LinkedToParent".
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent data object
 * @param <F> the child data object
 */
public class Linkedfromchildren<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F>>
		extends DataObjectProperty<E> {
	private DataObjectDefinition<F> childrendefinition;
	private DataObjectDefinition<E> referenceobjectdefinition;
	private LinkedfromchildrenDefinition<E, F> linkedfromchildrendefinition;
	private Uniqueidentified<E> uniqueidentified;

	/**
	 * gets the dependent property unique identified
	 * 
	 * @return the dependent property unique identified
	 */
	public Uniqueidentified<E> getUniqueIdentified() {
		return this.uniqueidentified;
	}

	/**
	 * creates the property linked from children
	 * 
	 * @param definition         definition of the linked from children property
	 * @param parentpayload      payload of the parent data object
	 * @param childrendefinition definition of the child data object
	 */
	public Linkedfromchildren(LinkedfromchildrenDefinition<E, F> definition, DataObjectPayload parentpayload,
			DataObjectDefinition<F> childrendefinition) {
		super(definition, parentpayload);
		this.linkedfromchildrendefinition = definition;
		this.referenceobjectdefinition = definition.getCurrentObjectDefinition();
		if (childrendefinition == null)
			throw new RuntimeException("childrendefinition is null");
		this.childrendefinition = childrendefinition;

	}

	/**
	 * gets all the children for the current data object
	 * 
	 * @param object                   current data object
	 * @param additionalquerycondition additional query condition (can be null if no
	 *                                 filter)
	 * @return all the children corresponding to the additional query condition (if
	 *         set)
	 */
	public F[] getallchildren(E object, QueryFilter additionalquerycondition) {

		return LinkedtoparentQueryHelper
				.get(linkedfromchildrendefinition.getGenericsChildobjectforlinkProperty().getName())
				.<F, E>getallchildren(uniqueidentified.getId(), additionalquerycondition, childrendefinition,
						referenceobjectdefinition,
						linkedfromchildrendefinition.getGenericsChildobjectforlinkProperty());
	}

	/**
	 * before deleting this object, checks if it has children. If the propery
	 * 'delete children' is set, children will be deleted, else, an exception will
	 * be thrown and the current object will not be deleted
	 * 
	 * @param object the current object
	 */
	@SuppressWarnings("unchecked")
	public void preprocUniqueidentifiedDelete(E object) {
		F[] children = getallchildren(object, null);
		if (children != null)
			if (children.length > 0) {
				if (this.linkedfromchildrendefinition.isDeleteChildren()) {
					for (int i = 0; i < children.length; i++) {
						F thischild = children[i];
						((UniqueidentifiedInterface<F>) thischild).delete();
					}
				} else {
					throw new RuntimeException(
							"Not possible to delete an object that has " + children.length + " children of type "
									+ childrendefinition.getName() + ", objectid =  " + uniqueidentified.getId());
				}
			}

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
	 * Massive version of check before deleting object. Before deleting this object,
	 * checks if it has children. If the propery 'delete children' is set, children
	 * will be deleted, else, an exception will be thrown and the current object
	 * will not be deleted
	 * 
	 * @param object             the list of objects
	 * @param linkedfromchildren the list of linked from children properties
	 */
	@SuppressWarnings("unchecked")
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & UniqueidentifiedInterface<F>> void preprocUniqueidentifiedDelete(
			E[] object, Linkedfromchildren<E, F>[] linkedfromchildren) {

		if (object == null)
			throw new RuntimeException("cannot treat null array");
		if (linkedfromchildren == null)
			throw new RuntimeException("cannot treat null array of linkedfromchildren");
		if (object.length != linkedfromchildren.length)
			throw new RuntimeException("LinkedfromChildren Array and Object Array do not have same size");
		if (object.length > 0) {
			LinkedfromchildrenDefinition<E, F> linkedfromchildrendefinition = linkedfromchildren[0].linkedfromchildrendefinition;
			F[] children = getallchildren(object, null, linkedfromchildren);
			if (children != null)
				if (children.length > 0) {
					if (linkedfromchildrendefinition.isDeleteChildren()) {
						// massive delete here
						ArrayList<Uniqueidentified<F>> uniqueidentifiedlistforchildren = new ArrayList<Uniqueidentified<F>>();
						for (int i = 0; i < children.length; i++) {
							F thischild = children[i];
							Uniqueidentified<F> thischildui = thischild
									.getUniqueidentiedFromLinkedFromChildren(linkedfromchildren[0]);
							uniqueidentifiedlistforchildren.add(thischildui);
						}
						Uniqueidentified.<F>delete(children,
								uniqueidentifiedlistforchildren.toArray(new Uniqueidentified[0]));

					} else {
						throw new RuntimeException("Not possible to delete a batch of objects that has "
								+ children.length + " children of type " + linkedfromchildrendefinition.getName());
					}
				}
		}
	}

	/**
	 * a massive version of get all children
	 * 
	 * @param object              array of objects
	 * @param additionalcondition additional query condition
	 * @param linkedfromchildren  array of linked from children properties (in the
	 *                            same order as the object array)
	 * @return the list of children
	 */
	@SuppressWarnings("unchecked")
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & UniqueidentifiedInterface<F>> F[] getallchildren(
			E[] object, QueryFilter additionalcondition, Linkedfromchildren<E, F>[] linkedfromchildren) {
		if (object == null)
			throw new RuntimeException("cannot treat null array");
		if (linkedfromchildren == null)
			throw new RuntimeException("cannot treat null array of linkedfromchildren");
		if (object.length != linkedfromchildren.length)
			throw new RuntimeException("LinkedfromChildren Array and Object Array do not have same size");
		if (object.length == 0)
			return (F[]) (new ArrayList<F>().toArray());
		ArrayList<DataObjectId<E>> parentidarray = new ArrayList<DataObjectId<E>>();
		LinkedfromchildrenDefinition<E, F> linkedfromchildrendefinition = linkedfromchildren[0].linkedfromchildrendefinition;
		for (int i = 0; i < object.length; i++) {
			parentidarray.add(object[i].getId());
		}
		DataObjectId<E>[] parentarrayid = parentidarray
				.toArray(object[0].getDefinitionFromObject().generateIdArrayTemplate());

		return LinkedtoparentQueryHelper
				.get(linkedfromchildrendefinition.getGenericsChildobjectforlinkProperty().getName())
				.<F, E>getallchildrenforseveralparents(parentarrayid, additionalcondition,
						linkedfromchildrendefinition.getChildObjectDefinition(),
						linkedfromchildrendefinition.getParentObject(),
						linkedfromchildrendefinition.getGenericsChildobjectforlinkProperty());
	}

}
