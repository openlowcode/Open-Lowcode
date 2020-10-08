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
import org.openlowcode.server.data.storage.AndQueryCondition;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.QueryOperatorEqual;
import org.openlowcode.server.data.storage.QueryOperatorGreaterOrEqualTo;
import org.openlowcode.server.data.storage.QueryOperatorSmallerOrEqualTo;
import org.openlowcode.server.data.storage.SimpleQueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 * @since 1.13
 */
public class IteratedcompanionDefinition<E extends DataObject<E> & HasidInterface<E>>
		extends
		DataObjectPropertyDefinition<E> {
	public static final int INFINITY = 999999999;
	private IntegerStoredField mnfirstiter;
	private IntegerStoredField mnlastiter;

	public IteratedcompanionDefinition(DataObjectDefinition<E> parentobject, String name) {
		super(parentobject, name);
		this.mnfirstiter = new IntegerStoredField( "MNFIRSTITER", null, new Integer(1));
		this.addFieldSchema(this.mnfirstiter);
		this.mnlastiter = new IntegerStoredField ("MNLASTITER", null, new Integer(INFINITY));
		this.addFieldSchema(this.mnlastiter);

	}


	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		return null;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String aliasstring) {
		TableAlias alias = parentobject.getAlias(aliasstring);
		QueryCondition activeiterationcondition = new SimpleQueryCondition<Integer>(alias, mnlastiter,
				new QueryOperatorEqual<Integer>(), new Integer(INFINITY));
		return activeiterationcondition;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[2];
		returnvalue[0] = new FieldSchemaForDisplay<E>("Created on iteration",
				"First iteration of main object where this data is valid", this.mnfirstiter, false, true,
				true, -200, 20, this.parentobject);
		returnvalue[1] = new FieldSchemaForDisplay<E>("Last active iteration ",
				"Last iteration of main object where this data is valid", this.mnlastiter,
				false, true, true, -200, 20, this.parentobject);

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
		return new Iteratedcompanion<E>(this, parentpayload);
	}

	/**
	 * generates the iteration query condition for filtering autolinks that are
	 * iterated
	 * 
	 * @param alias            parent table alias
	 * @param iteration        iteration number
	 * @param parentdefinition definition of the object holding the auto-link
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public QueryCondition getIterationQueryCondition(TableAlias alias, Integer iteration) {

		StoredFieldSchema<Integer> leftfromiteration = (StoredFieldSchema<Integer>) this.getDefinition()
				.lookupOnName("MNFIRSTITER");
		StoredFieldSchema<Integer> lefttoiteration = (StoredFieldSchema<Integer>) this.getDefinition()
				.lookupOnName("MNLASTITER");
		SimpleQueryCondition<Integer> fromiterationcondition = new SimpleQueryCondition<Integer>(alias,
				leftfromiteration, new QueryOperatorSmallerOrEqualTo<Integer>(), iteration);
		SimpleQueryCondition<Integer> toiterationcondition = new SimpleQueryCondition<Integer>(alias, lefttoiteration,
				new QueryOperatorGreaterOrEqualTo<Integer>(), iteration);
		return new AndQueryCondition(fromiterationcondition, toiterationcondition);
	}
}
