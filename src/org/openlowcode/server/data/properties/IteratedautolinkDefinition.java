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
 * Definition of an iterated auto-link that will keep the history of the links
 * on iterations of the object being left of the auto-link
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object holding the auto-link
 * @param <F> data object referenced by the auto-link
 */
public class IteratedautolinkDefinition<
		E extends DataObject<E> & AutolinkobjectInterface<E, F> & IteratedautolinkInterface<E, F>,
		F extends DataObject<F> & IteratedInterface<F>>
		extends
		DataObjectPropertyDefinition<E> {
	public static final int INFINITY = 999999999;
	private IntegerStoredField lffirstiter;
	private IntegerStoredField lflastiter;
	private DataObjectDefinition<F> leftiteratedobjectdef;
	private AutolinkobjectDefinition<E, F> autolinkobject;
	private IteratedDefinition<F> iterateddefinition;

	/**
	 * definition of the iterated auto-link definition
	 * 
	 * @param parentobject          parent data object definition
	 * @param leftiteratedobjectdef data object definition of the object being
	 *                              referenced by the auto-link
	 */
	public IteratedautolinkDefinition(
			DataObjectDefinition<E> parentobject,
			DataObjectDefinition<F> leftiteratedobjectdef) {
		super(parentobject, "ITERATEDAUTOLINK");
		this.lffirstiter = new IntegerStoredField("LFFIRSTITER", null, new Integer(1));
		this.addFieldSchema(this.lffirstiter);
		this.lflastiter = new IntegerStoredField("LFLASTITER", null, new Integer(INFINITY));
		this.addFieldSchema(this.lflastiter);
		this.parentobject = parentobject;
		this.leftiteratedobjectdef = leftiteratedobjectdef;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String aliasstring) {
		TableAlias alias = parentobject.getAlias(aliasstring);
		QueryCondition activeiterationcondition = new SimpleQueryCondition<Integer>(alias, lflastiter,
				new QueryOperatorEqual<Integer>(), new Integer(INFINITY));
		return activeiterationcondition;
	}

	@Override
	public org.openlowcode.server.data.DataObjectPropertyDefinition.FieldSchemaForDisplay<
			E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[2];
		returnvalue[0] = new FieldSchemaForDisplay<E>("Created on iteration",
				"value of the history counter of the left object of the link at creation", this.lffirstiter, false,
				true, true, -200, 20, this.parentobject);
		returnvalue[1] = new FieldSchemaForDisplay<E>("Last active iteration ",
				"value of the history counter of the left obeject of the link before removal or update",
				this.lflastiter, false, true, true, -200, 20, this.parentobject);

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
		return new Iteratedautolink<E, F>(this, parentpayload);
	}

	/**
	 * sets the dependent auto-link object
	 * 
	 * @param autolinkobject dependent auto-link object
	 */
	public void setDependentDefinitionAutolinkobject(AutolinkobjectDefinition<E, F> autolinkobject) {
		this.autolinkobject = autolinkobject;

	}

	/**
	 * sets the iterated property definition on the object being referenced by the
	 * auto-link
	 * 
	 * @param iterateddefinition definition of the iterated property
	 */
	public void setGenericsLeftobjectforlinkProperty(IteratedDefinition<F> iterateddefinition) {
		this.iterateddefinition = iterateddefinition;

	}

	/**
	 * @return gets the definition of the object being referenced by the auto-link
	 */
	public DataObjectDefinition<F> getLeftiteratedobjectdef() {
		return leftiteratedobjectdef;
	}

	/**
	 * @return gets the definition of the dependent property Autolink
	 */
	public AutolinkobjectDefinition<E, F> getAutolinkobject() {
		return autolinkobject;
	}

	/**
	 * @return gets the definition of the dependent property iterated on the object
	 *         being referenced by the auto-link
	 */
	public IteratedDefinition<F> getIterateddefinition() {
		return iterateddefinition;
	}

}
