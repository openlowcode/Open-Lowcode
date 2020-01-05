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
import org.openlowcode.server.data.TransitionFieldChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;

/**
 * Definition of the property giving the object an automatically generated
 * print-out when appropriate state is reached
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 * @param <F> transition choice definition for the lifecycle
 */
public class PrintoutDefinition<E extends DataObject<E> & FilecontentInterface<E> & LifecycleInterface<E, F>, F extends TransitionFieldChoiceDefinition<F>>
		extends DataObjectPropertyDefinition<E> {
	private String printoutlabel;
	private PrintOutGenerator<E> printoutgenerator;
	private ChoiceValue<F>[] triggerstates;

	public PrintoutDefinition(DataObjectDefinition<E> parentobject, String name, String printoutlabel,
			PrintOutGenerator<E> printoutgenerator, ChoiceValue<F>[] triggerstates) {
		super(parentobject, name);
		this.printoutlabel = printoutlabel;
		this.printoutgenerator = printoutgenerator;
		this.triggerstates = triggerstates;
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
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public String[] getLoaderFieldList() {
		return null;
	}

	@Override
	public String[] getLoaderFieldSample(String name) {
		return null;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new Printout<E, F>(this, parentpayload);
	}

	public PrintOutGenerator<E> getGenerator() {
		return this.printoutgenerator;
	}

	public String getPrintoutLabel() {
		return this.printoutlabel;
	}

	public ChoiceValue<F>[] getTriggerStates() {
		return this.triggerstates;
	}

}
