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

import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class NumberedFlatFileLoader<E extends DataObject<E> & NumberedInterface<E>> extends FlatFileLoaderColumn<E> {
	private DataObjectDefinition<E> objectdefinition;
	private NumberedDefinition<E> numberedproperty;
	private PropertyExtractor<E> propertyextractor;
	private boolean updateifexists;
	public NumberedFlatFileLoader(DataObjectDefinition<E> objectdefinition,NumberedDefinition<E> numberedproperty, boolean updateifexists,PropertyExtractor<E> propertyextractor) {
		this.objectdefinition = objectdefinition;
		this.updateifexists=updateifexists;
		this.propertyextractor = propertyextractor;
	}

	

	@Override
	public boolean isLinePreparator() {
		return true;
	}

	@Override
	public LinePreparation<E> LinePreparation(Object object,ArrayList<LinePreparationExtra<E>> linepreparatorextracriterias)
			 {
		String string = FlatFileLoader.parseObject(object,"LinePreparation for Numbered");
		QueryCondition extracondition = null;
		if (string==null) throw new RuntimeException("Number cannot be null on flat file loading.");
		if (string.trim().length()==0) throw new RuntimeException("Number cannot be an empty string on flat file loading. At least one significant character is expected.");
		
		if (objectdefinition.hasProperty("VERSIONED")) {
			extracondition = VersionedQueryHelper.getLatestVersionQueryCondition(objectdefinition.getAlias("SINGLEOBJECT"), objectdefinition);
		}
		for (int i=0;i<linepreparatorextracriterias.size();i++) {
			QueryCondition extracriteria = linepreparatorextracriterias.get(i).generateQueryCondition(objectdefinition, "SINGLEOBJECT");
			if (extracondition!=null) {
				extracondition = new AndQueryCondition(extracondition,extracriteria);
			} else {
				extracondition = extracriteria;
			}
		}
		E[] objectresult = NumberedQueryHelper.get().getobjectbynumber(string,extracondition, objectdefinition, numberedproperty);
		if (objectresult.length==1) {
			if (!updateifexists) throw new RuntimeException("Flat file loader is configured only to provide new elements, but element with nr = "+string+" already exists");
			return new LinePreparation<E>(objectresult[0],true);
		} else {
			return new LinePreparation<E>(objectdefinition.generateBlank(),false);
		}
		
	}

	@Override
	public boolean load(E object, Object value,PostUpdateProcessingStore<E> postupdateprocessingstore)  {
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property==null) throw new RuntimeException("could not find property in object");
		if (!( property instanceof Numbered)) throw new  RuntimeException("property does not have the correct type, expected Numbered, got "+property.getClass().getSimpleName());
		Numbered<E> numbered = (Numbered<E>) property;
		String oldnumber = numbered.getNr();
		String newnumber = FlatFileLoader.parseObject(value,"property 'Numbered'");
		
		if (FlatFileLoader.isTheSame(oldnumber,newnumber)) {
			return false;
		} else {
			// this will check again the number. It may return an exception in the context of load by parent
			object.setobjectnumber(newnumber);
			return true;
		}
	
	}



	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property==null) throw new RuntimeException("could not find property in object");
		if (!( property instanceof Numbered)) throw new RuntimeException("property does not have the correct type, expected Numbered, got "+property.getClass().getSimpleName());
		Numbered<E> numbered = (Numbered<E>) property;
		cell.setCellValue(numbered.getNr());
		return false;
	}

}
