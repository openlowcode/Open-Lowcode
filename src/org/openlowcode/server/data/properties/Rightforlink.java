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
import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkObject;

/**
 * A property that an object being linked as right object implements. There will
 * be one property per link going to the object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the right object (current object the property is on)
 * @param <F>the link object
 * @param <G> the left object for the link
 */
public class Rightforlink<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjectInterface<F, G, E> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
		extends DataObjectProperty<E> {
	private Uniqueidentified<E> uniqueidentified;
	private static Logger logger = Logger.getLogger(Rightforlink.class.getName());
	private RightforlinkDefinition<E, F, G> definition;

	public Rightforlink(RightforlinkDefinition<E, F, G> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.definition = definition;
	}

	/**
	 * @param object
	 */
	public void preprocUniqueidentifiedDelete(E object) {
		F[] links = LinkobjectQueryHelper.get().getalllinksfromrightid(uniqueidentified.getRelatedHasid().getId(), null,
				definition.getLinkObjectPropertyDefinition().getLinkObjectDefinition(),
				definition.getLinkObjectPropertyDefinition().getLeftObjectDefinition(),
				definition.getLinkObjectPropertyDefinition().getRightObjectDefinition(),
				definition.getLinkObjectPropertyDefinition());
		if (links != null)
			if (links.length > 0)
				throw new RuntimeException("Cannot delete object because there are " + links.length
						+ " link objects of type " + definition.getParentObject().getName() + " for object with id = "
						+ uniqueidentified.getRelatedHasid().getId());
	}

	/**
	 * @param uniqueidentified
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;
	}

	/**
	 * @param object
	 */
	public void preprocUniqueidentifiedUpdate(E object) {

		TwoDataObjects<G, F>[] linkandleftobject = LinkobjectQueryHelper.get().getlinksandleftobject(
				uniqueidentified.getRelatedHasid().getId(), null, definition.getLinkObjectPropertyDefinition().getLinkObjectDefinition(),
				definition.getLinkObjectPropertyDefinition().getLeftObjectDefinition(), definition.getParentObject(),
				definition.getLinkObjectPropertyDefinition());
		logger.finer(" --- right for Link Control on Link ");

		for (int i = 0; i < linkandleftobject.length; i++) {
			G leftobject = linkandleftobject[i].getObjectOne();
			for (int j = 0; j < definition.getLinkObjectPropertyDefinition().getConstraintOnLinkObjectNumber(); j++) {
				ConstraintOnLinkObject<G, E> thisconstraint = definition.getLinkObjectPropertyDefinition()
						.getConstraintOnLinkObject(j);
				if (!(thisconstraint.checklinkvalid(leftobject, object)))
					throw new RuntimeException(
							"Constraint Error " + thisconstraint.getInvalidLinkErrorMessage(leftobject, object));
				logger.finer(" Constraint " + j + " for object " + i + " is OK");
			}
		}

	}

	/**
	 * @param object
	 * @param preprocrightforlinkforgroupmemberlinkbatch
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, 
	F extends DataObject<F> & LinkobjectInterface<F, G, E> & UniqueidentifiedInterface<F>, 
	G extends DataObject<G> & UniqueidentifiedInterface<G>> 
	void preprocUniqueidentifiedUpdate(
			E[] object, Rightforlink<E, F, G>[] preprocrightforlinkforgroupmemberlinkbatch) {
		for (int i = 0; i < preprocrightforlinkforgroupmemberlinkbatch.length; i++) {
			preprocrightforlinkforgroupmemberlinkbatch[i].preprocUniqueidentifiedUpdate(object[i]);
		}
	}

	/**
	 * @param object
	 * @param preprocrightforlink
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, 
	F extends DataObject<F> & LinkobjectInterface<F, G, E> & UniqueidentifiedInterface<F>, 
	G extends DataObject<G> & UniqueidentifiedInterface<G>> 
	void preprocUniqueidentifiedDelete(
			E[] object, Rightforlink<E, F, G>[] preprocrightforlink) {
		if (object == null)
			throw new RuntimeException("cannot treat null array");
		if (preprocrightforlink == null)
			throw new RuntimeException( "cannot treat null array of linkedfromchildren");
		if (object.length != preprocrightforlink.length)
			throw new RuntimeException( "Rightforlink Array and Object Array do not have same size");

		if (object.length > 0) {
			HashMap<DataObjectId<E>, E> objectsbyid = new HashMap<DataObjectId<E>, E>();
			ArrayList<DataObjectId<E>> rightidlist = new ArrayList<DataObjectId<E>>();
			for (int i = 0; i < object.length; i++) {
				rightidlist.add(object[i].getId());
				objectsbyid.put(object[i].getId(), object[i]);

			}
			DataObjectId<E>[] rightidarray = (rightidlist
					.toArray(object[0].getDefinitionFromObject().generateIdArrayTemplate()));
			F[] linksfromleft = LinkobjectQueryHelper.get().getalllinksfromrightid(rightidarray, null,
					preprocrightforlink[0].definition.getLinkObjectPropertyDefinition().getLinkObjectDefinition(),
					preprocrightforlink[0].definition.getLinkObjectPropertyDefinition().getLeftObjectDefinition(),
					preprocrightforlink[0].definition.getParentObject(),
					preprocrightforlink[0].definition.getLinkObjectPropertyDefinition());

			if (linksfromleft != null)
				if (linksfromleft.length > 0) {
					StringBuffer dropids = new StringBuffer();

					for (int i = 0; i < linksfromleft.length; i++)
						dropids.append("LINKID" + linksfromleft[i].getId() + "-OBJID:" + linksfromleft[i].getRgid()
								+ ":" + objectsbyid.get(linksfromleft[i].getRgid()).dropToString() + "\n");
					throw new RuntimeException(
							"Cannot delete object because there are " + linksfromleft.length + " link objects of type "
									+ preprocrightforlink[0].definition.getParentObject().getName() + " for objects\n"
									+ dropids.toString());
				}
		}

	}
}
