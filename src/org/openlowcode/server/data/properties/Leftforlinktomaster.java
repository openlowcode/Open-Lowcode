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
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.TwoDataObjects;
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkToMaster;

/**
 * the property that is put on the left object for a link to master
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current object (left for link)
 * @param <F> data object holding the link
 * @param <G> right data object for link (linked for master)
 */
public class Leftforlinktomaster<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjecttomasterInterface<F, E, G>, G extends DataObject<G> & VersionedInterface<G>>
extends DataObjectProperty<E> {
private Uniqueidentified<E> uniqueidentified;
private LeftforlinktomasterDefinition<E, F, G> definition;
private static Logger logger = Logger.getLogger(Leftforlink.class.getName());

/**
* creates the property LeftForLinktomaster
* 
* @param definition    definition of the LeftForLink
* @param parentpayload payload for the parent data object
*/
public Leftforlinktomaster(LeftforlinktomasterDefinition<E, F, G> definition, DataObjectPayload parentpayload) {
super(definition, parentpayload);
this.definition = definition;
}

/**
* post-processing for the deletion of an object. If the object is deleted, all
* links are also deleted
* 
* @param object object being deleted
*/
public void postprocUniqueidentifiedDelete(E object) {
F[] linksfromleft = LinkobjecttomasterQueryHelper.get().getalllinksfromleftid(uniqueidentified.getRelatedHasid().getId(), null,
		definition.getLinkObjectPropertyDefinition().getLinkObjectDefinition(), definition.getParentObject(),
		definition.getLinkObjectPropertyDefinition().getRightObjectDefinition(),
		definition.getLinkObjectPropertyDefinition());

if (linksfromleft != null)
	for (int i = 0; i < linksfromleft.length; i++) {
		F thislink = linksfromleft[i];
		thislink.delete();
	}
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
* Pre-processing for the update of an object. Constraints are checked, and
* updates are refused if the links are no more valid
* 
* @param object parent data object
*/
public void preprocUniqueidentifiedUpdate(E object) {
TwoDataObjects<F, G>[] linkandrightobject = LinkobjecttomasterQueryHelper.get().getlinksandrightobject(
		uniqueidentified.getRelatedHasid().getId(), null, definition.getLinkObjectPropertyDefinition().getLinkObjectDefinition(),
		definition.getParentObject(), definition.getLinkObjectPropertyDefinition().getRightObjectDefinition(),
		definition.getLinkObjectPropertyDefinition());
logger.fine(" --- left for Link Control on Link ");
for (int i = 0; i < linkandrightobject.length; i++) {
	G rightobject = linkandrightobject[i].getObjectTwo();
	for (int j = 0; j < definition.getLinkObjectPropertyDefinition().getConstraintOnLinkToMasterNumber(); j++) {
		ConstraintOnLinkToMaster<E, G> thisconstraint = definition.getLinkObjectPropertyDefinition()
				 .getConstraintOnLinkToMaster(j);
		if (!(thisconstraint.checklinkvalid(object, rightobject)))
			throw new RuntimeException(
					"Constraint Error " + thisconstraint.getInvalidLinkErrorMessage(object, rightobject));
		logger.finer(" Constraint " + j + " for object " + i + " is OK");
	}
}

}

/**
* massive version of the pre-processing for update
* 
* @param object                                        batch of data objects
* @param preprocleftforlinkforgroupswithauthoritybatch corresponding batch of
*                                                      left for link
*/
public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjecttomasterInterface<F, E, G>, G extends DataObject<G> & VersionedInterface<G>> void preprocUniqueidentifiedUpdate(
	E[] object, Leftforlinktomaster<E, F, G>[] preprocleftforlinkforgroupswithauthoritybatch) {
for (int i = 0; i < preprocleftforlinkforgroupswithauthoritybatch.length; i++) {
	preprocleftforlinkforgroupswithauthoritybatch[i].preprocUniqueidentifiedUpdate(object[i]);
}

}

/**
* massive version of the post-processing for the update
* 
* @param object              batch of data objects
* @param postprocleftforlink corresponding batch of left for links
*/
public static <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjecttomasterInterface<F, E, G>, G extends DataObject<G> & VersionedInterface<G>> void postprocUniqueidentifiedDelete(
	E[] object, Leftforlinktomaster<E, F, G>[] postprocleftforlink) {
if (object == null)
	throw new RuntimeException("cannot treat null array");
if (postprocleftforlink == null)
	throw new RuntimeException("cannot treat null array of linkedfromchildren");
if (object.length != postprocleftforlink.length)
	throw new RuntimeException("LinkedfromChildren Array and Object Array do not have same size");
if (object.length > 0) {
	ArrayList<DataObjectId<E>> leftidlist = new ArrayList<DataObjectId<E>>();
	for (int i = 0; i < object.length; i++)
		leftidlist.add(object[i].getId());
	DataObjectId<E>[] leftidarray = leftidlist
			.toArray(object[0].getDefinitionFromObject().generateIdArrayTemplate());
	F[] linksfromleft = LinkobjecttomasterQueryHelper.get().getalllinksfromleftid(leftidarray, null,
			postprocleftforlink[0].definition.getLinkObjectPropertyDefinition().getLinkObjectDefinition(),
			postprocleftforlink[0].definition.getParentObject(),
			postprocleftforlink[0].definition.getLinkObjectPropertyDefinition().getRightObjectDefinition(),
			postprocleftforlink[0].definition.getLinkObjectPropertyDefinition());
	if (linksfromleft != null)
		if (linksfromleft.length > 0) {
			linksfromleft[0].getMassiveDelete().delete(linksfromleft);
		}
}
}
}
