/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.message;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.tools.structure.ObjectIdDataEltType;

/**
 * the type of ObjecIdDataElt with data object specified as generics to allow
 * easier manipulation on the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object
 */
public class TObjectIdDataEltType<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends
		ObjectIdDataEltType {

}
