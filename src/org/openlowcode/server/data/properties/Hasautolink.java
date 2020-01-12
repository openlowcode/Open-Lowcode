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
import org.openlowcode.server.data.properties.constraints.ConstraintOnAutolinkObject;
import org.openlowcode.server.data.storage.QueryFilter;

/**
 * The property of an object having an auto-link.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object (the object being the subject of the autolink)
 * @param <F> autolinkobject the object used as autolink
 */
public class Hasautolink<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & UniqueidentifiedInterface<F> & AutolinkobjectInterface<F, E>>
		extends DataObjectProperty<E> {
	private HasautolinkDefinition<E, F> definition;
	private Uniqueidentified<E> uniqueidentified;
	private static Logger logger = Logger.getLogger(Hasautolink.class.getName());

	/**
	 * Creates a new 'Has Autolink' property on the data obejct
	 * 
	 * @param definition    definiton
	 * @param parentpayload payload of the parent data object
	 */
	public Hasautolink(HasautolinkDefinition<E, F> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.definition = definition;

	}

	/**
	 * checks if the object can be deleted considering its autolink. Behavior is the
	 * following:
	 * <ul>
	 * <li>if autolink is symetric, then object can be deleted even if it has links
	 * (whether left or right)</li>
	 * <li>is autolink is not symetric, then general rule applies: links from left
	 * are deleted with object, if links from right exist, they forbid the deletion
	 * of object</li>
	 * 
	 */
	public void preprocUniqueidentifiedDelete(E object) {
		if (definition == null)
			throw new RuntimeException("Definition is null");
		if (definition.getAutolinkobjectPropertyDefinition() == null)
			throw new RuntimeException("get autolink is null");
		if (definition.getAutolinkobjectPropertyDefinition().isSymetricLink()) {
			// do nothing, one can always delete object with autolink
		} else {
			F[] linksfromright = AutolinkobjectQueryHelper.get().getalllinksfromrightid(uniqueidentified.getId(), null,
					definition.getAutolinkobjectPropertyDefinition().getLinkObjectDefinition(), definition.getParent(),
					definition.getAutolinkobjectPropertyDefinition());
			if (linksfromright != null)
				if (linksfromright.length > 0)
					throw new RuntimeException(" cannot delete object " + definition.getParent().getName() + "with id "
							+ uniqueidentified.getId() + "");
		}
	}

	/**
	 * after delete of the main object, all auto-links related to this object are
	 * deleted
	 * 
	 * @param object data object being deleted
	 */
	public void postprocUniqueidentifiedDelete(E object) {
		// this works without condition on autolink as in case of autolink, will provide
		// both links from left and right.
		F[] linksfromleft = AutolinkobjectQueryHelper.get().getalllinksfromleftid(uniqueidentified.getId(), null,
				definition.getAutolinkobjectPropertyDefinition().getLinkObjectDefinition(), definition.getParent(),
				definition.getAutolinkobjectPropertyDefinition());
		if (linksfromleft != null)
			if (linksfromleft.length > 0) {
				linksfromleft[0].getMassiveDelete().delete(linksfromleft);

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
	 * gets all the auto-links that have this object as left object
	 * 
	 * @param object                   data object (left object)
	 * @param additionalquerycondition additional filter condition
	 * @return an array of pairs of links and right objects
	 */
	public TwoDataObjects<F, E>[] getautolinksandchildren(E object, QueryFilter additionalquerycondition) {
		return AutolinkobjectQueryHelper.get().getlinksandrightobject(object.getId(), additionalquerycondition,
				definition.getAutolinkobjectPropertyDefinition().getLinkObjectDefinition(),
				definition.getParentObject(), definition.getAutolinkobjectPropertyDefinition());

	}

	/**
	 * when updating the object, it is checked if constraints on the link are still
	 * valid. Else, the update is refused.
	 * 
	 * @param object parent data object
	 */
	public void preprocUniqueidentifiedUpdate(E object) {

		// first search rightobjects (upstream for the link)
		TwoDataObjects<F, E>[] linkandrightobject = AutolinkobjectQueryHelper.get().getlinksandrightobject(
				uniqueidentified.getId(), null,
				definition.getAutolinkobjectPropertyDefinition().getLinkObjectDefinition(),
				definition.getParentObject(), definition.getAutolinkobjectPropertyDefinition());
		logger.severe(" --- autolink control for objects upstream, checking  " + linkandrightobject.length);
		for (int i = 0; i < linkandrightobject.length; i++) {
			E rightobject = linkandrightobject[i].getObjectTwo();
			for (int j = 0; j < definition.getAutolinkobjectPropertyDefinition()
					.getConstraintOnAutolinkObjectNumber(); j++) {
				ConstraintOnAutolinkObject<E> thisconstraint = definition.getAutolinkobjectPropertyDefinition()
						.getConstraintOnAutolinkObject(j);
				if (!(thisconstraint.checklinkvalid(object, rightobject)))
					throw new RuntimeException(
							"Constraint Error " + thisconstraint.getInvalidLinkErrorMessage(object, rightobject));
				logger.severe(" Constraint " + j + " for object " + i + " is OK");
			}
		}

		// then search left objects (downstream from the link)
		TwoDataObjects<E, F>[] linkandleftobject = AutolinkobjectQueryHelper.get().getlinksandleftobject(
				uniqueidentified.getId(), null,
				definition.getAutolinkobjectPropertyDefinition().getLinkObjectDefinition(),
				definition.getParentObject(), definition.getAutolinkobjectPropertyDefinition());
		logger.fine(" --- autolink control for objects downstream, checking  " + linkandleftobject.length);
		for (int i = 0; i < linkandleftobject.length; i++) {
			E rightobject = linkandleftobject[i].getObjectOne();
			for (int j = 0; j < definition.getAutolinkobjectPropertyDefinition()
					.getConstraintOnAutolinkObjectNumber(); j++) {
				ConstraintOnAutolinkObject<E> thisconstraint = definition.getAutolinkobjectPropertyDefinition()
						.getConstraintOnAutolinkObject(j);
				if (!(thisconstraint.checklinkvalid(object, rightobject)))
					throw new RuntimeException(
							"Constraint Error " + thisconstraint.getInvalidLinkErrorMessage(object, rightobject));
				logger.finer(" Constraint " + j + " for object " + i + " is OK");
			}
		}
	}

	/**
	 * massive version of the pre-processing of the update of the object
	 * 
	 * @param object      parent data object batch
	 * @param hasautolink corresponding parent HasAutolink property
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & UniqueidentifiedInterface<F> & AutolinkobjectInterface<F, E>> void preprocUniqueidentifiedUpdate(
			E[] object, Hasautolink<E, F>[] hasautolink) {
		for (int i = 0; i < hasautolink.length; i++) {
			hasautolink[i].preprocUniqueidentifiedUpdate(object[i]);
		}

	}

	/**
	 * massive version of the pre-treatment before delete
	 * 
	 * @param objectbatch   parent data object batch
	 * @param autolinkbatch corresponding hasautolink property batch
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & UniqueidentifiedInterface<F> & AutolinkobjectInterface<F, E>> void preprocUniqueidentifiedDelete(
			E[] objectbatch, Hasautolink<E, F>[] autolinkbatch) {
		if (objectbatch == null)
			throw new RuntimeException("cannot treat null array");
		if (autolinkbatch == null)
			throw new RuntimeException("cannot treat null array of linkedfromchildren");
		if (objectbatch.length != autolinkbatch.length)
			throw new RuntimeException("autolinkbatch Array and Object Array do not have same size");
		if (objectbatch.length > 0) {
			if (autolinkbatch[0].definition == null)
				throw new RuntimeException("Definition is null");
			if (autolinkbatch[0].definition.getAutolinkobjectPropertyDefinition() == null)
				throw new RuntimeException("get autolink is null");
			if (autolinkbatch[0].definition.getAutolinkobjectPropertyDefinition().isSymetricLink()) {
				// do nothing, one can always delete object with autolink
			} else {
				HashMap<DataObjectId<E>, E> objectsbyid = new HashMap<DataObjectId<E>, E>();
				ArrayList<DataObjectId<E>> objectid = new ArrayList<DataObjectId<E>>();
				for (int i = 0; i < objectbatch.length; i++) {
					objectsbyid.put(objectbatch[i].getId(), objectbatch[i]);
					objectid.add(objectbatch[i].getId());
					logger.severe("   -- before call from hell : " + objectbatch[i].getId());
				}
				DataObjectId<E>[] objectids = objectid
						.toArray(objectbatch[0].getDefinitionFromObject().generateIdArrayTemplate());
				F[] linksfromright = AutolinkobjectQueryHelper.get().getalllinksfromrightid(objectids, null,
						autolinkbatch[0].definition.getAutolinkobjectPropertyDefinition().getLinkObjectDefinition(),
						autolinkbatch[0].definition.getParent(),
						autolinkbatch[0].definition.getAutolinkobjectPropertyDefinition());
				if (linksfromright != null)
					if (linksfromright.length > 0) {
						StringBuffer dropids = new StringBuffer();

						for (int i = 0; i < linksfromright.length; i++)
							dropids.append(
									"LINKID" + linksfromright[i].getId() + "-OBJID:" + linksfromright[i].getRgid() + ":"
											+ objectsbyid.get(linksfromright[i].getRgid()) + "\n");
						throw new RuntimeException("Cannot delete object because there are " + linksfromright.length
								+ " link objects of type " + autolinkbatch[0].definition.getParentObject().getName()
								+ " for objects\n" + dropids.toString());
					}

			}
		}
	}

	/**
	 * massive version of the post-treatment after delete
	 * 
	 * @param objectbatch   parent data object batch
	 * @param autolinkbatch corresponding hasautolink property batch
	 */
	public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, 
	F extends DataObject<F> & UniqueidentifiedInterface<F> & AutolinkobjectInterface<F, E>> 
			void postprocUniqueidentifiedDelete(
			E[] objectbatch, Hasautolink<E, F>[] autolinkbatch) {
		if (objectbatch == null)
			throw new RuntimeException("cannot treat null array");
		if (autolinkbatch == null)
			throw new RuntimeException("cannot treat null array of linkedfromchildren");
		if (objectbatch.length != autolinkbatch.length)
			throw new RuntimeException("autolinkbatch Array and Object Array do not have same size");
		if (objectbatch.length > 0) {
			ArrayList<DataObjectId<E>> objectid = new ArrayList<DataObjectId<E>>();
			for (int i = 0; i < objectbatch.length; i++) {

				objectid.add(objectbatch[i].getId());
			}
			DataObjectId<E>[] objectids = objectid
					.toArray(objectbatch[0].getDefinitionFromObject().generateIdArrayTemplate());
			F[] linksfromleft = AutolinkobjectQueryHelper.get().getalllinksfromleftid(objectids, null,
					autolinkbatch[0].definition.getAutolinkobjectPropertyDefinition().getLinkObjectDefinition(),
					autolinkbatch[0].definition.getParent(),
					autolinkbatch[0].definition.getAutolinkobjectPropertyDefinition());
			if (linksfromleft != null)
				if (linksfromleft.length > 0) {
					linksfromleft[0].getMassiveDelete().delete(linksfromleft);

				}
		}
	}
}
