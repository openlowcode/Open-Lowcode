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

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * A loader/extractor class that extracts the master id in a column
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> A data object with the versioned property
 */
public class VersionedMasterIdFlatFileLoader<E extends DataObject<E> & VersionedInterface<E>>
		extends
		FlatFileLoaderColumn<E> {

	/**
	 * Creates a flatfile loader / extractor for the master id
	 * 
	 * @param dataobjectdefinition definition of the object
	 * @param versionedefinition   definition of the versioned property
	 * @param createifnotexists    if true, create the line of it does not exists
	 * @param propertyextractor    an extractor of property from the object
	 */
	public VersionedMasterIdFlatFileLoader(
			DataObjectDefinition<E> dataobjectdefinition,
			VersionedDefinition<E> versionedefinition,
			boolean createifnotexists,
			PropertyExtractor<E> propertyextractor) {
		super();
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		// does not load anything on the master id
		return false;
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		String masterid = currentobject.getMasterid().getId();
		cell.setCellValue(masterid);
		return false;
	}

}
