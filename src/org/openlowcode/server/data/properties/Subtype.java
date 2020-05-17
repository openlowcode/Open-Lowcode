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

import org.openlowcode.module.designer.data.Propertydef;
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
 */
public class Subtype<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends SimpleFieldChoiceDefinition<F>>
		extends
		DataObjectProperty<E> {

	private Uniqueidentified<E> uniqueidentified;

	public Subtype(DataObjectPropertyDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
	}

	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

}
