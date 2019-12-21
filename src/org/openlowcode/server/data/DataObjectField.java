/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * a DataObjectField is an element that stores information. The information can
 * be accessed and updated directly
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public abstract class DataObjectField<E extends DataObjectFieldDefinition<F>, F extends DataObject<F>>
		extends DataObjectElement<E, F> {
	public DataObjectField(E definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);

	}

	public abstract SimpleDataElt getDataElement();

}
