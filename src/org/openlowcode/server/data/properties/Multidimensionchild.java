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

import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of parent object
 */
public class Multidimensionchild<E extends DataObject<E> & UniqueidentifiedInterface<E>,F extends DataObject<F>>
		extends
		DataObjectProperty<E> {

	private Linkedtoparent<E, F> dependentpropertylinkedtoparent;

	public Multidimensionchild(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
	
	}
	
	public void setDependentPropertyLinkedtoparent(Linkedtoparent<E,F> dependentpropertylinkedtoparent) {
		this.dependentpropertylinkedtoparent =dependentpropertylinkedtoparent;
	}
	public void setmultidimensionparentidwithoutupdate(E object,DataObjectId<F> parentid) {
		this.dependentpropertylinkedtoparent.setparentwithoutupdate(object, parentid);
	}
	
}
