/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import org.openlowcode.server.action.SNullActionInputDataRef;
import org.openlowcode.tools.structure.DataEltType;

/**
 * A null action data loc is used to specified that the argument of the action
 * should be set to null
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data element
 */
public class SNullActionDataloc<E extends DataEltType>
		extends
		SActionDataLoc<E> {

	public SNullActionDataloc(SNullActionInputDataRef<E> nullinputactiondataref) {
		super(null, nullinputactiondataref);

	}

}
