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
import java.util.function.Supplier;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.CustomloaderDefinition.CustomloaderHelper;
import org.openlowcode.server.data.properties.multichild.MultidimensionchildHelper;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent object type
 * @param <F> child object type
 */
public class HasmultidimensionalchildDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E>,
F extends DataObject<F> & UniqueidentifiedInterface<F> & MultidimensionchildInterface<F,E>>
extends
DataObjectPropertyDefinition<E> {
	private LinkedfromchildrenDefinition<E,F> relateddefinitionlinkedfromchildren;
	@SuppressWarnings("unused")
	private DataObjectDefinition<F> childobjectdefinition;
	@SuppressWarnings("unused")
	private MultidimensionchildDefinition<F, E> childobjectmuldidimensionchildpropertydefinition;
	private Supplier<MultidimensionchildHelper<F,E>> multidimensionchildhelpergenerator;
	public MultidimensionchildHelper<F,E> getHelper() {
		return this.multidimensionchildhelpergenerator.get();
	}
	
	public F[] getChildren(DataObjectId<E> parentid) {
		return LinkedtoparentQueryHelper
				.get(this.getRelatedDefinitionLinkedFromChildren()
						.getGenericsChildobjectforlinkProperty().getName())
				.getallchildren(parentid, null,
						this.getRelatedDefinitionLinkedFromChildren().getChildObjectDefinition(),
						this.getRelatedDefinitionLinkedFromChildren().getParentObject(),
						this.getRelatedDefinitionLinkedFromChildren()
								.getGenericsChildobjectforlinkProperty());
	}
	
	public void setHelperGenerator(Supplier<MultidimensionchildHelper<F,E>> multidimensionchildhelpergenerator) {
		this.multidimensionchildhelpergenerator = multidimensionchildhelpergenerator;
	}
	
	public LinkedfromchildrenDefinition<E,F> getRelatedDefinitionLinkedFromChildren() {
		return this.relateddefinitionlinkedfromchildren;
	}
	public HasmultidimensionalchildDefinition(DataObjectDefinition<E> parentobject, String name,DataObjectDefinition<F> childobjectdefinition) {
		super(parentobject, name);
		this.childobjectdefinition = childobjectdefinition;
	}
	public void setDependentDefinitionLinkedfromchildren(LinkedfromchildrenDefinition<E,F> relateddefinitionlinkedfromchildren) {
		this.relateddefinitionlinkedfromchildren = relateddefinitionlinkedfromchildren;
	}
	
	public void setGenericsChildobjectforlinkProperty(MultidimensionchildDefinition<F,E> childobjectmuldidimensionchildpropertydefinition) {
		this.childobjectmuldidimensionchildpropertydefinition = childobjectmuldidimensionchildpropertydefinition;
	}
	
	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public FieldSchemaForDisplay[] setFieldSchemaToDisplay() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getLoaderFieldList() {
		return null;
	} 

	@Override
	public String[] getLoaderFieldSample(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(
			DataObjectPayload parentpayload) {
		return new  Hasmultidimensionalchild<E,F>(this, parentpayload);
	}
	@Override
	public CustomloaderHelper<E> getTransientLoaderHelper() {
		return new HasmultidimensionalchildFlatFileLoaderHelper<E,F>(this);
	}
}
