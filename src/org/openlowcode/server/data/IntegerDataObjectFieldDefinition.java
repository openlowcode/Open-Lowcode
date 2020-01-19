/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SIntegerField;

/**
 * Definition of a field storing an integer value as payload.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class IntegerDataObjectFieldDefinition<E extends DataObject<E>>
		extends
		DataObjectFieldDefinition<E> {
	/**
	 * creates the definition of an integer data object field
	 * 
	 * @param name              unique name of the field for the data object
	 * @param displayname       label of the field in default language
	 * @param tooltip           a roll-over tip
	 * @param readonly          true if read-only
	 * @param showintitle       if true, show field in title and in main object
	 *                          section
	 * @param showinbottomnotes if true, show field in bottom notes only
	 * @param priority          priority of the field inside the object display
	 * @param objectdefinition  definition of the parent data object
	 */
	public IntegerDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			boolean readonly,
			boolean showintitle,
			boolean showinbottomnotes,
			int priority,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottomnotes, priority, -1, objectdefinition);
		this.addFieldSchema(new IntegerStoredField(this.getName(), objectdefinition.getTableschema()));
	}

	/**
	 * creates the definition of an integer data object field
	 * 
	 * @param name              unique name of the field for the data object
	 * @param displayname       label of the field in default language
	 * @param tooltip           a roll-over tip
	 * @param priority          priority of the field inside the object display
	 * @param objectdefinition  definition of the parent data object
	 */
	public IntegerDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			int priority,
			DataObjectDefinition<E> objectdefinition) {
		this(name, displayname, tooltip, false, false, false, priority, objectdefinition);
	}

	@Override
	public boolean isFormulaElement() {
		return true;
	}

	@Override
	public SPageNode getDataFieldDefinition() {
		SIntegerField integerfield = new SIntegerField(this.getDisplayname(), this.getName(), this.getTooltip(),
				new Integer(0), false, null, this.isReadOnly(), this.isShowintitle(), this.isShowinbottonpage(), null);
		return integerfield;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new IntegerDataObjectField<E>(this, parentpayload);
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return new IntegerDataObjectFieldFlatFileLoader<E>(objectdefinition, columnattributes, this.getName());
	}

	@Override
	public String[] getLoaderFieldSample() {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		returntable[2] = "-153";
		returntable[3] = "no additional parameter for this type";
		return returntable;
	}

	@Override
	public StoredFieldSchema<?> getMainStoredField() {
		throw new RuntimeException("Not yet implemented");
	}
}
