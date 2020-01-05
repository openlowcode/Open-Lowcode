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
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.TimestampStoredField;

/**
 * Definition of the target date for an object with lifecycle.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent object
 * @param <F> type of lifecycle of the related lifecycle property
 */
public class TargetdateDefinition<E extends DataObject<E> & UniqueidentifiedInterface<E> & LifecycleInterface<E,F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectPropertyDefinition<E> {
	private TimestampStoredField targetdate;
	@SuppressWarnings("unused")
	private LifecycleDefinition<E, F> lifecycledefinition;

	/**
	 * creates the targetdate definition
	 * 
	 * @param parentobject                    parent object
	 * @param transitionfieldchoicedefinition type of lifecycle of the related
	 *                                        lifecycle property
	 */
	public TargetdateDefinition(DataObjectDefinition<E> parentobject, F transitionfieldchoicedefinition) {
		super(parentobject, "TARGETDATE");
		targetdate = new TimestampStoredField("TARGETDATE", null, null);
		this.addFieldSchema(targetdate);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openlowcode.server.data.DataObjectPropertyDefinition#
	 * generateExternalSchema()
	 */
	@Override
	public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
		ArrayList<ExternalFieldSchema<?>> externalfieldlist = new ArrayList<ExternalFieldSchema<?>>();
		return externalfieldlist;
	}

	@Override
	public QueryCondition getUniversalQueryCondition(String alias) {
		return null;
	}

	@Override
	public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
		@SuppressWarnings("unchecked")
		FieldSchemaForDisplay<E>[] returnvalue = new FieldSchemaForDisplay[1];
		returnvalue[0] = new FieldSchemaForDisplay<E>("Target Date", "Target for closure of the issue", targetdate,
				false, false, 700, 20, this.parentobject);
		return returnvalue;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Targetdate<E, F>(this, parentpayload);
	}

	public void setDependentDefinitionLifecycle(LifecycleDefinition<E, F> lifecycledefinition) {
		this.lifecycledefinition = lifecycledefinition;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {

		String format = null;
		if (columnattributes.length > 0) {
			format = columnattributes[0];
		}
		return new TargetdateFlatFileLoader<E>(objectdefinition, format, propertyextractor);
	}

	@Override
	public String[] getLoaderFieldList() {
		return new String[] { "" };
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		returntable[2] = "2017.02.28";
		returntable[3] = "Optional parameter: specific java simpledateformat (e.g. \"yyyy.MM.dd G 'at' HH:mm:ss z\" for 2001.07.04 AD at 12:08:56 PDT ) , definition at https://docs.oracle.com/javase/7/docs/api/java/text/SimpleDateFormat.html";
		return returntable;
	}
}
