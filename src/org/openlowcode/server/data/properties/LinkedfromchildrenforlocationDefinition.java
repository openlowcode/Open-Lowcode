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
 * Defition of the LinkedFromchildrenforlocation. This property indicates that
 * this data object location should be replicated on its children
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current data object (parent)
 * @param <F> child data object (used as basis for location)
 */
public class LinkedfromchildrenforlocationDefinition<
		E extends DataObject<E> & LocatedInterface<E> & UniqueidentifiedInterface<E>,
		F extends DataObject<F> & UniqueidentifiedInterface<F> & LocatedInterface<F>>
		extends
		DataObjectPropertyDefinition<E> {
	/**
	 * gets the dependent property definition linked from children
	 * 
	 * @return dependent property definition linked from children
	 */
	public LinkedfromchildrenDefinition<E, F> getDependentLinkedFromChildrenDefinition() {
		return this.dependentlinkfromchildrendefinition;
	}

	/**
	 * gets the definition of the child object to replicate the location to
	 * 
	 * @return definition of the child object
	 */
	public DataObjectDefinition<F> getChildObjectDefinition() {
		return this.referenceobjectdefinition;
	}

	@SuppressWarnings("unused")
	private LocatedDefinition<E> dependentlocateddefinition;
	private LinkedfromchildrenDefinition<E, F> dependentlinkfromchildrendefinition;
	private DataObjectDefinition<F> referenceobjectdefinition;
	@SuppressWarnings("unused")
	private LinkedtoparentDefinition<F, E> genericschildobjectforlinkproperty;

	/**
	 * creates the definition of the linkedfromchildrenforlocation property
	 * 
	 * @param parentobject              data object the property is on
	 * @param name                      name of the linkedtoparent related property
	 * @param referenceobjectdefinition definition of the child object to replicate
	 *                                  location to
	 */
	public LinkedfromchildrenforlocationDefinition(
			DataObjectDefinition<E> parentobject,
			String name,
			DataObjectDefinition<F> referenceobjectdefinition) {
		super(parentobject, name);
		this.referenceobjectdefinition = referenceobjectdefinition;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return null;
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[0];
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Linkedfromchildrenforlocation<E, F>(this, parentpayload);
	}

	/**
	 * sets a reference to the dependent definition located on the same object
	 * 
	 * @param dependentlocateddefinition dependent definition located
	 */
	public void setDependentDefinitionLocated(LocatedDefinition<E> dependentlocateddefinition) {
		this.dependentlocateddefinition = dependentlocateddefinition;
	}

	/**
	 * sets a reference to the property definition linkedtochildren on the current
	 * (parent) object
	 * 
	 * @param dependentlinkfromchildrendefinition dependent property linked from
	 *                                            children
	 */
	public void setDependentDefinitionLinkedfromchildren(
			LinkedfromchildrenDefinition<E, F> dependentlinkfromchildrendefinition) {
		this.dependentlinkfromchildrendefinition = dependentlinkfromchildrendefinition;
	}

	/**
	 * sets a referencethe property definition linkedtoparent from the child object
	 * 
	 * @param genericschildobjectforlinkproperty related property definition
	 *                                           linkedtoparent from the child
	 *                                           object
	 */
	public void setGenericsChildobjectforlinkProperty(
			LinkedtoparentDefinition<F, E> genericschildobjectforlinkproperty) {
		this.genericschildobjectforlinkproperty = genericschildobjectforlinkproperty;
	}

}
