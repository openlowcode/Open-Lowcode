/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;

/**
 * A no-op discarded column in the loading file
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the loading is on
 */
public class DiscardedColumn<E extends DataObject<E>> extends FlatFileLoaderColumn<E> {
	/**
	 * creates a discarded column
	 */
	public DiscardedColumn() {

	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		return false;
	}

	@Override
	public boolean isDiscarded() {
		return true;
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		return false;

	}

}
