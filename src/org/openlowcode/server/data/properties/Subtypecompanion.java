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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.SimpleFieldChoiceDefinition;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 * @param <F>
 * @param <G>
 */
public class Subtypecompanion<
		E extends DataObject<E> & StoredobjectInterface<E>,
		G extends DataObject<G> & SubtypeInterface<G, F>,
		F extends SimpleFieldChoiceDefinition<F>> extends DataObjectProperty<E> {

	private Storedobject<E> storedobject;

	public Subtypecompanion(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);

	}

	public void setDependentPropertyStoredobject(Storedobject<E> storedobject) {
		this.storedobject=storedobject;
		
	}

	

}
