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

import java.util.HashMap;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoader;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.loader.PostUpdateProcessingStore;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * the flat file loader to link a parent to the object being loaded
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the object itself
 * @param <F> the parent of the object
 */
public class LinkedToParentFlatFileLoader<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & NumberedInterface<F>>
		extends FlatFileLoaderColumn<E> {
	private static Logger logger = Logger.getLogger(LinkedToParentFlatFileLoader.class.getName());
	private HashMap<String, F> parentobjectsbykey;
	private DataObjectDefinition<E> objectdefinition;
	private LinkedtoparentDefinition<E, F> linkedtoparent;
	private DataObjectDefinition<F> parentdefinition;
	private PropertyExtractor<E> propertyextractor;
	private boolean createmissing;

	/**
	 * creates a flat file loader to link the data object being loaded to a parent
	 * having the numbered property
	 * 
	 * @param objectdefinition  data object definition
	 * @param linkedtoparent    linked to parent property
	 * @param parentdefinition  definition of the parent object
	 * @param propertyextractor an extractor to get the property
	 * @param createmissing     if true, the missing parents will be
	 */
	public LinkedToParentFlatFileLoader(DataObjectDefinition<E> objectdefinition,
			LinkedtoparentDefinition<E, F> linkedtoparent, DataObjectDefinition<F> parentdefinition,
			PropertyExtractor<E> propertyextractor, boolean createmissing) {
		this.objectdefinition = objectdefinition;
		this.linkedtoparent = linkedtoparent;
		this.parentdefinition = parentdefinition;
		this.propertyextractor = propertyextractor;
		this.parentobjectsbykey = new HashMap<String, F>();
		this.createmissing = createmissing;
		if (!parentdefinition.hasProperty("NUMBERED"))
			throw new RuntimeException("Only linked to parent where parent object is numbered can be loaded, but "
					+ parentdefinition.getName() + " is not.");

	}

	@Override
	public boolean isStaticPreProcessing() {
		return true;
	}

	@SuppressWarnings("static-access")
	@Override
	public void staticpreprocessor(String next) {
		if (!parentobjectsbykey.containsKey(next)) {
			QueryCondition extracondition = null;
			if (parentdefinition.hasProperty("VERSIONED")) {
				extracondition = VersionedQueryHelper.get()
						.getLatestVersionQueryCondition(parentdefinition.getAlias("SINGLEOBJECT"), parentdefinition);
			}
			F[] object = NumberedQueryHelper.get().getobjectbynumber(next, extracondition, parentdefinition,
					(NumberedDefinition<F>) parentdefinition.getProperty("NUMBERED"));
			if (object.length == 1) {
				parentobjectsbykey.put(next, object[0]);
			} else {
				if (createmissing) {
					F newparent = parentdefinition.generateBlank();
					newparent.setobjectnumber(next);
					newparent.insert();
					parentobjectsbykey.put(next, newparent);
					logger.fine("Inserted new parent object " + parentdefinition.getName() + " with nr = " + next);
				}
			}
		}
	}

	@Override
	public boolean load(E object, Object value, PostUpdateProcessingStore<E> postupdateprocessingstore) {
		String stringvalue = FlatFileLoader.parseObject(value, "Linked To Parent for " + objectdefinition.getName());
		DataObjectProperty<E> property = propertyextractor.extract(object);
		if (property == null)
			throw new RuntimeException("could not find property in object");
		if (!(property instanceof Linkedtoparent))
			throw new RuntimeException("property does not have the correct type, expected Linkedtoparent, got "
					+ property.getClass().getSimpleName());
		@SuppressWarnings("unchecked")
		Linkedtoparent<E, F> linkedtoparent = (Linkedtoparent<E, F>) property;
		F parentobject = parentobjectsbykey.get(stringvalue);
		if (parentobject == null)
			throw new RuntimeException("Parent is not valid for object " + value);
		DataObjectId<F> oldid = linkedtoparent.getId();
		if (FlatFileLoader.isTheSame(oldid, parentobject.getId())) {
			return false;
		} else {
			linkedtoparent.setPrid(parentobject.getId());
			return true;

		}
	}

	@Override
	public boolean isLinePreparatorExtra() {
		if (objectdefinition.hasProperty("NUMBEREDFORPARENT")) {
			@SuppressWarnings("rawtypes")
			NumberedforparentDefinition numberedforparent = (NumberedforparentDefinition) (objectdefinition
					.getProperty("NUMBEREDFORPARENT"));
			if (numberedforparent.getRelatedLinkedToParentDefinition().getName().equals(linkedtoparent.getName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public FlatFileLoaderColumn.LinePreparationExtra<E> generateLinePreparatorExtra(Object data) {
		String parseddata = FlatFileLoader.parseObject(data, "LinkedToParent as " + objectdefinition.getName());
		F object = parentobjectsbykey.get(parseddata);
		DataObjectId<F> parentid = (object != null ? object.getId() : null);
		if (parentid == null)
			throw new RuntimeException("Did not find parentid for number = '" + data + "'");
		return new FlatFileLoaderColumn.LinePreparationExtra<E>() {

			@Override
			public QueryCondition generateQueryCondition(DataObjectDefinition<E> definition, String alias) {

				return LinkedtoparentQueryHelper.get(linkedtoparent.getName()).getParentIdQueryCondition(
						definition.getAlias(alias), parentid, objectdefinition, parentdefinition);
			}

		};
	}

	@Override
	protected boolean putContentInCell(E currentobject, Cell cell, String context) {
		DataObjectProperty<E> property = propertyextractor.extract(currentobject);
		if (property == null)
			throw new RuntimeException("could not find property in object");
		if (!(property instanceof Linkedtoparent))
			throw new RuntimeException("property does not have the correct type, expected Linkedtoparent, got "
					+ property.getClass().getSimpleName());
		@SuppressWarnings("unchecked")
		Linkedtoparent<E, F> linkedtoparent = (Linkedtoparent<E, F>) property;
		F parent = linkedtoparent.getparent(currentobject);
		if (parent != null)
			cell.setCellValue(parent.getNr());
		return false;
	}

}
