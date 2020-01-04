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
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TableAlias;

/**
 * Definition of the iterated property. If an object is iterated, a copy of any
 * update is stored. The latest iteration is distinguished by a flag, and is
 * used for most purposes. When using the iterated property, data storage volume
 * should be taken into consideration
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class IteratedDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E> & IteratedInterface<E>>
		extends DataObjectPropertyDefinition<E> {
	private StringStoredField latest;
	private StringStoredField updatenote;
	private IntegerStoredField iteration;
	private UniqueidentifiedDefinition<E> uniqueidentifieddefinition;
	private DataObjectDefinition<E> parentobject;

	public UniqueidentifiedDefinition<E> getUniqueIdentifiedDefinition() {
		return uniqueidentifieddefinition;
	}

	public QueryCondition getUniversalQueryCondition(String aliasstring) {
		TableAlias alias = parentobject.getAlias(aliasstring);

		QueryCondition activeiterationcondition = new SimpleQueryCondition<String>(alias, latest,
				new QueryOperatorEqual<String>(), "Y");
		return activeiterationcondition;
	}

	public IteratedDefinition(DataObjectDefinition<E> parentobject) {
		super(parentobject, "ITERATED");
		this.iteration = new IntegerStoredField("ITERATION", null, new Integer(1));
		this.addFieldSchema(this.iteration);
		this.latest = new StringStoredField("LATEST", null, 1, "Y");
		this.addFieldSchema(this.latest);
		this.updatenote = new StringStoredField("UPDATENOTE", null, 200, "");
		this.addFieldSchema(this.updatenote);
		this.parentobject = parentobject;
	}

	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {

		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[3];
		returnvalue[0] = new FieldSchemaForDisplay<E>("Iteration", "History counter for the object", this.iteration,
				false, false, true, -50, 20, this.parentobject);
		returnvalue[1] = new FieldSchemaForDisplay<E>("Latest", "Indicates if this iteration is the latest",
				this.latest, false, false, true, -51, 20, this.parentobject);
		returnvalue[2] = new FieldSchemaForDisplay<E>("Update note", "Business context of the update", this.updatenote,
				false, false, true, -50, 20, this.parentobject);
		return returnvalue;

	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Iterated<E>(this, parentpayload);
	}

	public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentified) {
		this.uniqueidentifieddefinition = uniqueidentified;

	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
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
}
