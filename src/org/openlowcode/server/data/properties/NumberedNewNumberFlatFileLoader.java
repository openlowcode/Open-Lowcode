package org.openlowcode.server.data.properties;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;


public class NumberedNewNumberFlatFileLoader<E extends DataObject<E> & NumberedInterface<E>>
		extends
		FlatFileLoaderColumn<E> {

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		String newnumber = (String) value;
		if (newnumber!=null) if (newnumber.length()>0) {
			object.setobjectnumber(newnumber);
		}
		// returns always false as the new number is already persisted
		return false;
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {

		return false;
	}

}
