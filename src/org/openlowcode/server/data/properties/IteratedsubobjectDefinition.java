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
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * Definition of an iterated subobject: a child of a main object that is
 * considered as part of the data set of the parent data object. Thus, any
 * change on the subobject will result in an iteration of the parent object
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> sub object
 * @param <F> parent of the subobject
 */
public class IteratedsubobjectDefinition<
		E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedsubobjectInterface<E, F>,
		F extends DataObject<F> & IteratedInterface<F>>
		extends
		DataObjectPropertyDefinition<E> {
	public static final int INFINITY = 999999999;
	private IntegerStoredField prfirstiter;
	private IntegerStoredField prlastiter;
	private DataObjectDefinition<F> parentiteratedobjectdef;

	private LinkedtoparentDefinition<E, F> linkedtoparent;
	private IteratedDefinition<F> iterateddefinition;

	/**
	 * creates the definition of an iterated subobject property
	 * 
	 * @param parentobject            data object definition of the sub object
	 * @param parentiteratedobjectdef data object definition of the iterated parent
	 *                                data object
	 */
	public IteratedsubobjectDefinition(
			DataObjectDefinition<E> parentobject,
			DataObjectDefinition<F> parentiteratedobjectdef) {
		super(parentobject, "ITERATEDSUBOBJECT");
		this.prfirstiter = new IntegerStoredField("PRFIRSTITER", null, new Integer(1));
		this.addFieldSchema(this.prfirstiter);
		this.prlastiter = new IntegerStoredField("PRLASTITER", null, new Integer(INFINITY));
		this.addFieldSchema(this.prlastiter);
		this.parentobject = parentobject;
		this.parentiteratedobjectdef = parentiteratedobjectdef;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {

		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String aliasstring) {
		TableAlias alias = parentobject.getAlias(aliasstring);
		QueryCondition activeiterationcondition = new SimpleQueryCondition<Integer>(alias, prlastiter,
				new QueryOperatorEqual<Integer>(), new Integer(INFINITY));
		return activeiterationcondition;
	}

	@Override
	public org.openlowcode.server.data.DataObjectPropertyDefinition.FieldSchemaForDisplay<
			E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[2];
		returnvalue[0] = new FieldSchemaForDisplay<E>("Created on iteration",
				"value of the history counter of the parent object at creation", this.prfirstiter, false, true, true,
				-200, 20, this.parentobject);
		returnvalue[1] = new FieldSchemaForDisplay<E>("Last active iteration ",
				"value of the history counter of the parent object before removal or update", this.prlastiter, false,
				true, true, -200, 20, this.parentobject);

		return returnvalue;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("Not yet implemented");
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
		return new Iteratedsubobject<E, F>(this, parentpayload);
	}

	/**
	 * definition of the related linked to parent property on this object (the
	 * subobject)
	 * 
	 * @param linkedtoparent linked to parent property)
	 */
	public void setDependentDefinitionLinkedtoparent(LinkedtoparentDefinition<E, F> linkedtoparent) {
		this.linkedtoparent = linkedtoparent;

	}

	/**
	 * definition of the parent object (for the parent-child link) iterated property
	 * 
	 * @param iterateddefinition parent object for link iterated property
	 */
	public void setGenericsParentProperty(IteratedDefinition<F> iterateddefinition) {
		this.iterateddefinition = iterateddefinition;

	}

	/**
	 * gets the definition of the parent data object for the parent child link
	 * 
	 * @return the definition of parent data object
	 */
	public DataObjectDefinition<F> getParentiteratedobjectdef() {
		return parentiteratedobjectdef;
	}

	/**
	 * gets the related linked to parent property of the current (subobject) data
	 * object property
	 * 
	 * @return the linked to parent property
	 */
	public LinkedtoparentDefinition<E, F> getLinkedtoparent() {
		return linkedtoparent;
	}

	/**
	 * gets the definition of the iterated property
	 * @return definition of the iteratd property
	 */
	public IteratedDefinition<F> getIterateddefinition() {
		return iterateddefinition;
	}

}
