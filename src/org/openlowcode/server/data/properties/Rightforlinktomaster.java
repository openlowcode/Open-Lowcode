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
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkToMaster;

public class Rightforlinktomaster<E extends DataObject<E> & VersionedInterface<E>, F extends DataObject<F> & LinkobjecttomasterInterface<F, G, E> & UniqueidentifiedInterface<F>, G extends DataObject<G> & UniqueidentifiedInterface<G>>
extends DataObjectProperty<E> {
private Versioned<E> versioned;
private static Logger logger = Logger.getLogger(Rightforlink.class.getName());
private RightforlinktomasterDefinition<E, F, G> definition;

public Rightforlinktomaster(RightforlinktomasterDefinition<E, F, G> definition, DataObjectPayload parentpayload) {
super(definition, parentpayload);
this.definition = definition;
}

/**
* @param object
*/
public void preprocUniqueidentifiedDelete(E object) {
F[] links = LinkobjecttomasterQueryHelper.get().getalllinksfromrightmsid(versioned.getMasterid(), null,
		definition.getLinkObjectToMasterPropertyDefinition().getLinkObjectDefinition(),
		definition.getLinkObjectToMasterPropertyDefinition().getLeftObjectDefinition(),
		definition.getLinkObjectToMasterPropertyDefinition().getRightObjectDefinition(),
		definition.getLinkObjectToMasterPropertyDefinition());
if (links != null)
	if (links.length > 0)
		throw new RuntimeException("Cannot delete object because there are " + links.length
				+ " link objects of type " + definition.getParentObject().getName() + " for object with master id = "
				+ versioned.getMasterid());
}

/**
 * Sets the referenced versioned property on current (right for link) object
 * 
* @param versioned the versioned property
*/
public void setDependentPropertyVersioned(Versioned<E> versioned) {
this.versioned = versioned;
}

/**
* @param object
*/
public void preprocUniqueidentifiedUpdate(E object) {

TwoDataObjects<G, F>[] linkandleftobject = LinkobjecttomasterQueryHelper.get().getlinksandleftobject(
		versioned.getMasterid(), null, definition.getLinkObjectToMasterPropertyDefinition().getLinkObjectDefinition(),
		definition.getLinkObjectToMasterPropertyDefinition().getLeftObjectDefinition(), definition.getParentObject(),
		definition.getLinkObjectToMasterPropertyDefinition());
logger.finer(" --- right for Link Control on Link ");

for (int i = 0; i < linkandleftobject.length; i++) {
	G leftobject = linkandleftobject[i].getObjectOne();
	for (int j = 0; j < definition.getLinkObjectToMasterPropertyDefinition().getConstraintOnLinkToMasterNumber(); j++) {
		ConstraintOnLinkToMaster<G, E> thisconstraint = definition.getLinkObjectToMasterPropertyDefinition()
				.getConstraintOnLinkToMaster(j);
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
public static <E extends DataObject<E> & VersionedInterface<E>, 
F extends DataObject<F> & LinkobjecttomasterInterface<F, G, E> & UniqueidentifiedInterface<F>, 
G extends DataObject<G> & UniqueidentifiedInterface<G>> 
void preprocUniqueidentifiedUpdate(
	E[] object, Rightforlinktomaster<E, F, G>[] preprocrightforlinkforgroupmemberlinkbatch) {
for (int i = 0; i < preprocrightforlinkforgroupmemberlinkbatch.length; i++) {
	preprocrightforlinkforgroupmemberlinkbatch[i].preprocUniqueidentifiedUpdate(object[i]);
}
}

/**
* @param object
* @param preprocrightforlink
*/
public static <E extends DataObject<E> & VersionedInterface<E>, 
F extends DataObject<F> & LinkobjecttomasterInterface<F, G, E> & UniqueidentifiedInterface<F>, 
G extends DataObject<G> & UniqueidentifiedInterface<G>> 
void preprocUniqueidentifiedDelete(
	E[] object, Rightforlinktomaster<E, F, G>[] preprocrightforlink) {
if (object == null)
	throw new RuntimeException("cannot treat null array");
if (preprocrightforlink == null)
	throw new RuntimeException( "cannot treat null array of linkedfromchildren");
if (object.length != preprocrightforlink.length)
	throw new RuntimeException( "Rightforlink Array and Object Array do not have same size");

if (object.length > 0) {
	HashMap<DataObjectMasterId<E>, E> objectsbyid = new HashMap<DataObjectMasterId<E>, E>();
	ArrayList<DataObjectMasterId<E>> rightidlist = new ArrayList<DataObjectMasterId<E>>();
	for (int i = 0; i < object.length; i++) {
		rightidlist.add(object[i].getMasterid());
		objectsbyid.put(object[i].getMasterid(), object[i]);

	}
	DataObjectMasterId<E>[] rightidarray = (rightidlist
			.toArray(object[0].getDefinitionFromObject().generateMasterIdArrayTemplate()));
	F[] linksfromleft = LinkobjecttomasterQueryHelper.get().getalllinksfromrightmsid(rightidarray, null,
			preprocrightforlink[0].definition.getLinkObjectToMasterPropertyDefinition().getLinkObjectDefinition(),
			preprocrightforlink[0].definition.getLinkObjectToMasterPropertyDefinition().getLeftObjectDefinition(),
			preprocrightforlink[0].definition.getParentObject(),
			preprocrightforlink[0].definition.getLinkObjectToMasterPropertyDefinition());

	if (linksfromleft != null)
		if (linksfromleft.length > 0) {
			StringBuffer dropids = new StringBuffer();

			for (int i = 0; i < linksfromleft.length; i++)
				dropids.append("LINKID" + linksfromleft[i].getId() + "-OBJID:" + linksfromleft[i].getRgmsid()
						+ ":" + objectsbyid.get(linksfromleft[i].getRgmsid()).dropToString() + "\n");
			throw new RuntimeException(
					"Cannot delete object because there are " + linksfromleft.length + " link objects of type "
							+ preprocrightforlink[0].definition.getParentObject().getName() + " for objects\n"
							+ dropids.toString());
		}
}

}
}
