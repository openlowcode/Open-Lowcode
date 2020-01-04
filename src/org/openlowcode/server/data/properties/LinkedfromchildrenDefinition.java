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

import java.util.ArrayList;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * Definition of linked from children. An object is linked from children if
 * another data object is declared as "LinkedToParent".
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent data object
 * @param <F> the child data object
 */
public class LinkedfromchildrenDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & UniqueidentifiedInterface<F>>
		extends DataObjectPropertyDefinition<E> {
	private Logger logger = Logger.getLogger(LinkedfromchildrenDefinition.class.getName());
	private DataObjectDefinition<F> referenceobjectdefinition;
	private DataObjectDefinition<E> currentobjectdefinition;
	private LinkedtoparentDefinition<F, E> genericschildobjectforlinkproperty;
	@SuppressWarnings("unused")
	private UniqueidentifiedDefinition<E> uniqueidentifieddefinition;
	private boolean deletechildren;

	/**
	 * definition of linked from children property
	 * 
	 * @param parentobject              definition of the parent object
	 * @param name                      name of the linked from children (a data
	 *                                  object can have several 'LinkFromChildren'
	 *                                  properties, they are distinguished by name
	 *                                  that has to be unique for this data object
	 * @param referenceobjectdefinition the definition of the child object
	 */
	public LinkedfromchildrenDefinition(DataObjectDefinition<E> parentobject, String name,
			DataObjectDefinition<F> referenceobjectdefinition) {
		super(parentobject, name);
		if (referenceobjectdefinition == null) {
			logger.warning("Warniong: child definition is null at ");
			logger.log(Level.WARNING, "classpath", new Exception());
		} else {
			logger.fine("childdefinition is not null " + referenceobjectdefinition + " property address" + this);
		}
		this.referenceobjectdefinition = referenceobjectdefinition;
		this.currentobjectdefinition = parentobject;
		this.deletechildren = false;
	}

	/**
	 * gets the current data object definition (the parent)
	 * 
	 * @return current data object definition
	 */
	public DataObjectDefinition<E> getCurrentObjectDefinition() {
		return this.currentobjectdefinition;
	}

	/**
	 * gets child data object definition
	 * 
	 * @return child data object definition
	 */
	public DataObjectDefinition<F> getChildObjectDefinition() {
		return this.referenceobjectdefinition;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		logger.finest("current property address " + this + " for name " + this.getName() + " for parent object "
				+ parentobject.getName());
		if (referenceobjectdefinition == null)
			logger.info("child definition has become null");
		return new Linkedfromchildren<E, F>(this, parentpayload, referenceobjectdefinition);
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return null;
	}

	/**
	 * if set to true, when this object is deleted, the children will also be
	 * deleted. Else, an object with children cannot be deleted
	 */
	public void setDeleteChildren() {
		this.deletechildren = true;
	}

	/**
	 * @return true if delete children when deleting this object
	 */
	public boolean isDeleteChildren() {
		return this.deletechildren;
	}

	/**
	 * @param genericschildobjectforlinkproperty sets the related linkedtoparent
	 *                                           property on the child object
	 */
	public void setGenericsChildobjectforlinkProperty(
			LinkedtoparentDefinition<F, E> genericschildobjectforlinkproperty) {
		this.genericschildobjectforlinkproperty = genericschildobjectforlinkproperty;
	}

	/**
	 * @return gets the releated linkedtoparent property for the child object
	 */
	public LinkedtoparentDefinition<F, E> getGenericsChildobjectforlinkProperty() {
		return this.genericschildobjectforlinkproperty;
	}

	/**
	 * sets the dependent property unique identified for this data object
	 * 
	 * @param uniqueidentified unique identified property of this data object
	 */
	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		this.uniqueidentifieddefinition = uniqueidentified;

	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {

		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("Not yet Implemented");
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return null;
	}
}
