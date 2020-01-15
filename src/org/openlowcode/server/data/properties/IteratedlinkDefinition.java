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
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * Definition of the property specifying that a link is iterated. This is the
 * case when the left object is iterated. In that case, the link stored the
 * first iteration and last iteration of the left object for which the link is
 * valid. This is an efficient way to store link evolution. Any change in link
 * is also archived as an iteration on the left object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the link object
 * @param <F> the "left" object of the link
 * @param <G> the "right" object of the link
 */
public class IteratedlinkDefinition<
		E extends DataObject<E> & LinkobjectInterface<E, F, G> & IteratedlinkInterface<E, F, G>,
		F extends DataObject<F> & IteratedInterface<F>,
		G extends DataObject<G> & UniqueidentifiedInterface<G>>
		extends
		DataObjectPropertyDefinition<E> {
	public static final int INFINITY = 999999999;
	private IntegerStoredField lffirstiter;
	private IntegerStoredField lflastiter;
	private DataObjectDefinition<F> leftiteratedobjectdef;
	private DataObjectDefinition<G> rightobjectdef;

	private LinkobjectDefinition<E, F, G> linkobject;
	private IteratedDefinition<F> iterateddefinition;
	private UniqueidentifiedDefinition<G> uniqueidentifieddefinition;

	/**
	 * creates the definition of an iteation link property
	 * 
	 * @param parentobject          current link data object
	 * @param leftiteratedobjectdef definition of left object for link
	 * @param rightobjectdef        definition of right object
	 */
	public IteratedlinkDefinition(
			DataObjectDefinition<E> parentobject,
			DataObjectDefinition<F> leftiteratedobjectdef,
			DataObjectDefinition<G> rightobjectdef) {
		super(parentobject, "ITERATEDLINK");
		this.lffirstiter = new IntegerStoredField("LFFIRSTITER", null, new Integer(1));
		this.addFieldSchema(this.lffirstiter);
		this.lflastiter = new IntegerStoredField("LFLASTITER", null, new Integer(INFINITY));
		this.addFieldSchema(this.lflastiter);
		this.parentobject = parentobject;
		this.leftiteratedobjectdef = leftiteratedobjectdef;
		this.rightobjectdef = rightobjectdef;
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
		return new Iteratedlink<E, F, G>(this, parentpayload);
	}

	/**
	 * sets the dependent property link object
	 * 
	 * @param linkobject dependent property link object
	 */
	public void setDependentDefinitionLinkobject(LinkobjectDefinition<E, F, G> linkobject) {
		this.linkobject = linkobject;

	}

	/**
	 * sets the dependent property iterated on the left object for the link
	 * 
	 * @param iterateddefinition dependent property iterated on the left object for
	 *                           the link
	 */
	public void setGenericsLeftobjectforlinkProperty(IteratedDefinition<F> iterateddefinition) {
		this.iterateddefinition = iterateddefinition;

	}

	/**
	 * @return get the left object for link definition
	 */
	public DataObjectDefinition<F> getLeftiteratedobjectdef() {
		return leftiteratedobjectdef;
	}

	/**
	 * @return get the right object for link definition
	 */
	public DataObjectDefinition<G> getRightobjectdef() {
		return this.rightobjectdef;
	}

	/**
	 * @return get the link object property definition
	 */
	public LinkobjectDefinition<E, F, G> getLinkobject() {
		return linkobject;
	}

	/**
	 * @return get the iterated definition for the left object for the link
	 */
	public IteratedDefinition<F> getIterateddefinition() {
		return iterateddefinition;
	}

	/**
	 * sets the property unique identified on related right object for link
	 * 
	 * @param uniqueidentifiedDefinition unique identified property definition
	 */
	public void setGenericsRightobjectforlinkProperty(UniqueidentifiedDefinition<G> uniqueidentifiedDefinition) {
		this.uniqueidentifieddefinition = uniqueidentifiedDefinition;

	}

	/**
	 * get the property unique identified on related right object for link
	 * 
	 * @return unique identified property definition on the right object
	 */
	public UniqueidentifiedDefinition<G> getRightObjectUniqueidentified() {
		return this.uniqueidentifieddefinition;
	}
}
