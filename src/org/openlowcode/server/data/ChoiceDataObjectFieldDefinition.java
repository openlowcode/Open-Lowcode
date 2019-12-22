/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
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
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SChoiceTextField;

/**
 * The definition of a field storing one value in a list of choices
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of choice
 * @param <F> parent data object
 */
public class ChoiceDataObjectFieldDefinition<E extends FieldChoiceDefinition<E>, F extends DataObject<F>>
		extends DataObjectFieldDefinition<F> {
	private E fieldchoicedefinition;
	private StringStoredField mainfield;

	/**
	 * creates the definition of a choice DataObjectField with normal display
	 * 
	 * @param name                  unique name of the field for the object (not too
	 *                              long as used in the database)
	 * @param displayname           display name of the field (shown in the user
	 *                              interface)
	 * @param tooltip               tooltip for mouse rollover
	 * @param readonly              true if field is read-only
	 * @param fieldchoicedefinition type of choice used for the field
	 * @param objectdefinition      parent object definition
	 */
	public ChoiceDataObjectFieldDefinition(String name, String displayname, String tooltip, boolean readonly,
			E fieldchoicedefinition, DataObjectDefinition<F> objectdefinition) {
		super(name, displayname, tooltip, readonly, objectdefinition);
		this.setFieldchoicedefinition(fieldchoicedefinition);
		mainfield = new StringStoredField(this.getName(), null, fieldchoicedefinition.getStorageSize());
		this.addFieldSchema(mainfield);
	}

	/**
	 * creates the definition of a choice DataObjectField
	 * 
	 * @param name                  unique name of the field for the object (not too
	 *                              long as used in the database)
	 * @param displayname           display name of the field (shown in the user
	 *                              interface)
	 * @param tooltip               tooltip for mouse rollover
	 * @param readonly              true if field is read-only
	 * @param showintitle           true if field shown in title
	 * @param showinbottomnotes     true if field shown in bottom notes
	 * @param fieldchoicedefinition type of choice used for the field
	 * @param priority              priority for display
	 * @param defaultcolumnfortable size of the field when displayed in a table
	 * @param objectdefinition      parent object definition
	 */
	public ChoiceDataObjectFieldDefinition(String name, String displayname, String tooltip, boolean readonly,
			boolean showintitle, boolean showinbottomnotes, E fieldchoicedefinition, int priority,
			int defaultcolumnfortable, DataObjectDefinition<F> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottomnotes, priority, defaultcolumnfortable,
				objectdefinition);
		this.setFieldchoicedefinition(fieldchoicedefinition);
		mainfield = new StringStoredField(this.getName(), null, fieldchoicedefinition.getStorageSize());
		this.addFieldSchema(mainfield);

	}

	private void setFieldchoicedefinition(E fieldchoicedefinition) {
		this.fieldchoicedefinition = fieldchoicedefinition;

	}

	/**
	 * creates the definition of a choice DataObjectField with normal display
	 * 
	 * @param name                  unique name of the field for the object (not too
	 *                              long as used in the database)
	 * @param displayname           display name of the field (shown in the user
	 *                              interface)
	 * @param tooltip               tooltip for mouse rollover
	 * @param readonly              true if field is read-only
	 * @param fieldchoicedefinition type of choice used for the field
	 * @param priority              priority for display inside object
	 * @param defaultcolumnfortable width of column in table
	 * @param objectdefinition      parent object definition
	 */
	public ChoiceDataObjectFieldDefinition(String name, String displayname, String tooltip, boolean readonly,
			E fieldchoicedefinition, int priority, int defaultcolumnfortable,
			DataObjectDefinition<F> objectdefinition) {
		super(name, displayname, tooltip, readonly, priority, defaultcolumnfortable, objectdefinition);
		this.setFieldchoicedefinition(fieldchoicedefinition);
		mainfield = new StringStoredField(this.getName(), null, fieldchoicedefinition.getStorageSize());
		this.addFieldSchema(mainfield);

	}

	@Override
	public SPageNode getDataFieldDefinition() {
		SChoiceTextField<E> choicetextfield = new SChoiceTextField<E>(this.getDisplayname(), this.getName(),
				this.getTooltip(), getFieldchoicedefinition(), getFieldchoicedefinition().getDefaultChoice(), null,
				true, this.isReadOnly(), this.isShowintitle(), this.isShowinbottonpage(), null);
		if (this.getDefaultcolumnintable() >= 0)
			choicetextfield.setPreferedDisplayInTable(this.getDefaultcolumnintable());
		return choicetextfield;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {

		return new ChoiceDataObjectField<E, F>(this, parentpayload, 0, getFieldchoicedefinition());
	}

	@Override
	public FlatFileLoaderColumn<F> getFlatFileLoaderColumn(DataObjectDefinition<F> objectdefinition,
			String[] columnattributes, ChoiceValue<ApplocaleChoiceDefinition> locale) {
		boolean lenient = false;
		if (columnattributes != null)
			if (columnattributes.length > 0)
				if ("LENIENT".equals(columnattributes[0]))
					lenient = true;
		return new ChoiceDataObjectFieldFlatFileLoaderColumn<F>(objectdefinition, columnattributes, this.getName(),
				getFieldchoicedefinition(), lenient);
	}

	@Override
	public String[] getLoaderFieldSample() {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		ChoiceValue<E>[] values = getFieldchoicedefinition().getChoiceValue();
		returntable[2] = values[0].getStorageCode();
		StringBuffer allvalues = new StringBuffer("\n");
		for (int i = 0; i < values.length; i++) {
			if (i > 0)
				allvalues.append("\n");
			allvalues.append(values[i].getStorageCode());
		}
		allvalues.append("");
		returntable[3] = "Optional Parameter 'LENIENT' to insert object even if this field is wrong. One value amongst "
				+ allvalues.toString();
		return returntable;
	}

	@Override
	public StringStoredField getMainStoredField() {
		return mainfield;
	}

	/**
	 * @return
	 */
	public E getFieldchoicedefinition() {
		return fieldchoicedefinition;
	}

}
