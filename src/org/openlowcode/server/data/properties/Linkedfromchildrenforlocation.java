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

import org.openlowcode.module.system.data.Domain;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;

/**
 * This property indicates that this data object location should be replicated
 * on its children
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current data object (parent)
 * @param <F> child data object (used as basis for location)
 */
public class Linkedfromchildrenforlocation<
		E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>,
		F extends DataObject<F> & UniqueidentifiedInterface<F> & LocatedInterface<F>>
		extends
		DataObjectProperty<E> {

	@SuppressWarnings("unused")
	private Located<E> dependentpropertylocated;
	private Linkedfromchildren<E, F> dependentpropertylinkedfromchildren;
	private LinkedfromchildrenforlocationDefinition<E, F> parseddefinition;

	/**
	 * Creates an instance of the property
	 * 
	 * @param definition    data object definition
	 * @param parentpayload payload of the data object
	 */
	public Linkedfromchildrenforlocation(
			LinkedfromchildrenforlocationDefinition<E, F> definition,
			DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		this.parseddefinition = definition;

	}

	/**
	 * sets the dependent property located on the current object
	 * 
	 * @param dependentpropertylocated dependent property located on the current
	 *                                 object
	 */
	public void setDependentPropertyLocated(Located<E> dependentpropertylocated) {
		this.dependentpropertylocated = dependentpropertylocated;
	}

	/**
	 * set dependent property Linked From Children for the current object
	 * 
	 * @param dependentpropertylinkedfromchildren dependent property linked from
	 *                                            children
	 */
	public void setDependentPropertyLinkedfromchildren(Linkedfromchildren<E, F> dependentpropertylinkedfromchildren) {
		this.dependentpropertylinkedfromchildren = dependentpropertylinkedfromchildren;
	}

	/**
	 * when the parent changes location, the children will also change location
	 * 
	 * @param object   parent object
	 * @param domainid new domain id
	 */
	public void postprocLocatedSetlocation(E object, DataObjectId<Domain> domainid) {
		LinkedfromchildrenDefinition<
				E, F> linkedfromchildren = parseddefinition.getDependentLinkedFromChildrenDefinition();
		Uniqueidentified<E> uniqueidentified = dependentpropertylinkedfromchildren.getUniqueIdentified();
		F[] queryresult = LinkedtoparentQueryHelper
				.get(linkedfromchildren.getGenericsChildobjectforlinkProperty().getName())
				.<F, E>getallchildren(uniqueidentified.getRelatedHasid().getId(), null, parseddefinition.getChildObjectDefinition(),
						parseddefinition.getParentObject(), linkedfromchildren.getGenericsChildobjectforlinkProperty());
		for (int i = 0; i < queryresult.length; i++) {
			queryresult[i].setlocation(domainid);
		}
	}
}
