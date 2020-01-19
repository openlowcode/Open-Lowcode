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
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SMultipleChoiceTextField;

/**
 * Definition of a field of a data object holding several choice values. A list
 * of codes is persisted in the databases separated by "|"
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> choice definition
 * @param <F> parent data object
 */
public class MultipleChoiceDataObjectFieldDefinition<E extends FieldChoiceDefinition<E>, F extends DataObject<F>>
		extends
		DataObjectFieldDefinition<F> {
	private E fieldchoicedefinition;
	private StringStoredField mainfield;
	private int storagelength;

	/**
	 * creates the definition of a multiple choice data object field definition
	 * 
	 * @param name                  unique name of the field
	 * @param displayname           label of the field
	 * @param tooltip               roll-over tooltip
	 * @param readonly              true if read-only
	 * @param fieldchoicedefinition definition of the field choice
	 * @param storagelength         storage length of the field. Should be able to
	 *                              hold enough storage codes and separators
	 * @param objectdefinition      definition of the parent data object
	 */
	public MultipleChoiceDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			boolean readonly,
			E fieldchoicedefinition,
			int storagelength,
			DataObjectDefinition<F> objectdefinition) {
		super(name, displayname, tooltip, readonly, objectdefinition);
		this.setFieldchoicedefinition(fieldchoicedefinition);
		this.storagelength = storagelength;
		this.mainfield = new StringStoredField(this.getName(), null, this.storagelength);
		this.addFieldSchema(mainfield);
	}

	/**
	 * creates the definition of a multiple choice data object field definition
	 * 
	 * @param name                  unique name of the field
	 * @param displayname           label of the field
	 * @param tooltip               roll-over tooltip
	 * @param readonly              true if read-only
	 * @param showintitle           true if field is shown in title and main object
	 *                              band
	 * @param showinbottomnotes     true if field is shown only in bottom notes
	 * @param fieldchoicedefinition definition of the field choice
	 * @param storagelength         storage length of the field. Should be able to
	 *                              hold enough storage codes and separators
	 * @param priority              priority of the field in the object
	 * @param defaultcolumn         size of column in table expressed in characters
	 * @param objectdefinition      definition of the parent data object
	 */
	public MultipleChoiceDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			boolean readonly,
			boolean showintitle,
			boolean showinbottomnotes,
			E fieldchoicedefinition,
			int storagelength,
			int priority,
			int defaultcolumn,
			DataObjectDefinition<F> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottomnotes, priority, defaultcolumn,
				objectdefinition);
		this.setFieldchoicedefinition(fieldchoicedefinition);
		this.storagelength = storagelength;
		this.mainfield = new StringStoredField(this.getName(), null, this.storagelength);
		this.addFieldSchema(mainfield);

	}

	/**
	 * creates the definition of a multiple choice data object field definition
	 * 
	 * @param name                  unique name of the field
	 * @param displayname           label of the field
	 * @param tooltip               roll-over tooltip
	 * @param readonly              true if read-only
	 * @param fieldchoicedefinition definition of the field choice
	 * @param storagelength         storage length of the field. Should be able to
	 *                              hold enough storage codes and separators
	 * @param priority              priority of the field in the object
	 * @param defaultcolumn         size of column in table expressed in characters
	 * @param objectdefinition      definition of the parent data object
	 */
	public MultipleChoiceDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			boolean readonly,
			E fieldchoicedefinition,
			int storagelength,
			int priority,
			int defaultcolumn,
			DataObjectDefinition<F> objectdefinition) {
		super(name, displayname, tooltip, readonly, priority, defaultcolumn, objectdefinition);
		this.setFieldchoicedefinition(fieldchoicedefinition);
		this.storagelength = storagelength;
		this.mainfield = new StringStoredField(this.getName(), null, this.storagelength);
		this.addFieldSchema(mainfield);

	}

	/**
	 * @return the definition of the choice for this field
	 */
	public E getFieldchoicedefinition() {
		return fieldchoicedefinition;
	}

	/**
	 * sets the field choice for this field
	 * 
	 * @param fieldchoicedefinition field choice for the field
	 */
	private void setFieldchoicedefinition(E fieldchoicedefinition) {
		this.fieldchoicedefinition = fieldchoicedefinition;
	}

	@Override
	public SPageNode getDataFieldDefinition() {
		SMultipleChoiceTextField<E> choicetextfield = new SMultipleChoiceTextField<E>(this.getDisplayname(),
				this.getName(), this.getTooltip(), getFieldchoicedefinition(), null, true, this.isReadOnly(),
				this.isShowintitle(), this.isShowinbottonpage(), null);
		if (this.getDefaultcolumnintable() >= 0)
			choicetextfield.setPreferedDisplayInTable(this.getDefaultcolumnintable());
		return choicetextfield;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {

		return new MultipleChoiceDataObjectField<E, F>(this, parentpayload, getFieldchoicedefinition());
	}

	@Override
	public FlatFileLoaderColumn<F> getFlatFileLoaderColumn(
			DataObjectDefinition<F> objectdefinition,
			String[] columnattributes,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		boolean lenient = false;
		if (columnattributes != null)
			if (columnattributes.length > 0)
				if ("LENIENT".equals(columnattributes[0]))
					lenient = true;
		return new MultipleChoiceDataObjectFieldFlatFileLoaderColumn<F>(objectdefinition, columnattributes,
				this.getName(), getFieldchoicedefinition(), lenient);
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
	public StoredFieldSchema<?> getMainStoredField() {
		return this.mainfield;
	}

}
