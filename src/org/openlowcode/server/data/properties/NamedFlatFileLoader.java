/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * A flat file loader to load the name of the objects. It performs a check of
 * the name length, and depending on the 'truncate' settings, will truncate
 * names that are too long or throw an exception
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class NamedFlatFileLoader<E extends DataObject<E> & NamedInterface<E>> extends FlatFileLoaderColumn<E> {
	@SuppressWarnings("unused")
	private DataObjectDefinition<E> dataobjectdefinition;
	private boolean truncate;

	public NamedFlatFileLoader(DataObjectDefinition<E> dataobjectdefinition, boolean truncate) {
		this.dataobjectdefinition = dataobjectdefinition;
		this.truncate = truncate;
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		String toinsert = FlatFileLoader.parseObject(value, "property 'Named'");

		if (truncate)
			if (toinsert.length() > NamedDefinition.NAMED_LENGTH) {
				toinsert = toinsert.substring(0, NamedDefinition.NAMED_LENGTH - 3) + "...";
			}
		String oldname = object.getName();
		if (FlatFileLoader.isTheSame(oldname, toinsert)) {
			return false;
		} else {
			object.setobjectname(toinsert);
			return true;
		}
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		cell.setCellValue(currentobject.getObjectname());
		return false;
	}

}
