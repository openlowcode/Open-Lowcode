/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;

/**
 * Creates a flat file loader for the related integer field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class IntegerDataObjectFieldFlatFileLoader<E extends DataObject<E>> extends FlatFileLoaderColumn<E> {
	private String name;
	public IntegerDataObjectFieldFlatFileLoader(DataObjectDefinition<E> definition,String[] arguments,String name) {
		this.name = name;
	}
	

	@SuppressWarnings("rawtypes")
	@Override
	public boolean load(E object, Object value,PostUpdateProcessingStore<E> postupdateprocessingstore)  {
		DataObjectField field = object.payload.lookupSimpleFieldOnName(name);
		if (field==null) throw new RuntimeException("field "+name+" could not be looked-up on "+object.getName());
		if (!(field instanceof IntegerDataObjectField)) throw new  RuntimeException("Expected field "+name+" would be of type IntegerDataObjectField but in reality, it is "+field.getClass().toString());
		IntegerDataObjectField integerfield = (IntegerDataObjectField) field;
		Integer oldinteger = integerfield.getValue();
		Integer newinteger = FlatFileLoader.parseInteger(object,"field "+name+" for object "+object.getName());
		if (FlatFileLoader.isTheSame(oldinteger,newinteger)) {
			return false;
		} else {
			integerfield.setValue(newinteger);
			return true;
		}

	}


	@SuppressWarnings("rawtypes")
	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context)  {
		DataObjectField field = currentobject.payload.lookupSimpleFieldOnName(name);
		if (field==null) throw new RuntimeException("field "+name+" could not be looked-up on "+currentobject.getName());
		if (!(field instanceof IntegerDataObjectField)) throw new  RuntimeException("Expected field "+name+" would be of type IntegerDataObjectField but in reality, it is "+field.getClass().toString());
		IntegerDataObjectField integerfield = (IntegerDataObjectField) field;
		Integer integer = integerfield.getValue();
		if (integer!=null) cell.setCellValue(integer.doubleValue());
		return false;
	}

}
