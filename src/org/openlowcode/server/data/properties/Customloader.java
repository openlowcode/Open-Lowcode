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
 * This property will add a custom loader for the object when performing a flat file loader
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class Customloader<E extends DataObject<E> & UniqueidentifiedInterface<E>> extends DataObjectProperty<E> {

	/**
	 * creates a custom  loader property
	 * 
	 * @param definition definition of the property
	 * @param parentpayload payload of the parent data object
	 */
	public Customloader(CustomloaderDefinition<E> definition,DataObjectPayload parentpayload)
			 {
		super(definition, parentpayload);
		
	}

}
