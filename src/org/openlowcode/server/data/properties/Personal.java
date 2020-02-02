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

/**
 * The property Personal allowing a privilege to be granted to users directly
 * linked to the data obejct
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>data object the property applies to
 */
public class Personal<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends
		DataObjectProperty<E> {

	/**
	 * creates a Personal property
	 * 
	 * @param definition personal property definition
	 * @param parentpayload parent object payload
	 */
	public Personal(PersonalDefinition<E> definition, DataObjectPayload parentpayload)  {
		super(definition, parentpayload);
		
	}

}
