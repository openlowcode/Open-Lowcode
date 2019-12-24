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
import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.StoredFieldSchema;

import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SDecimalField;
import org.openlowcode.server.graphic.widget.SDecimalFormatter;

/**
 * Definition of a decimal field in a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the type of data object
 */
public class DecimalDataObjectFieldDefinition<E extends DataObject<E>> extends DataObjectFieldDefinition<E> {
	private int scale;
	private int precision;
	private SDecimalFormatter formatter;
	private DecimalStoredField mainfield;

	@Override
	public boolean isFormulaElement() {
		return true;
	}

	/**
	 * creates a new field with default priority (0)
	 * 
	 * @param name             unique name of the field
	 * @param displayname      label to show in the main language of the application
	 * @param tooltip          long explanation for mouse roll-over
	 * @param precision        number of significant digits authorized
	 * @param scale            number of digits authorized after comma
	 * @param readonly         true if field is read-only
	 * @param showintitle      show the field in title of the obkect
	 * @param showinbottompage show the field in bottom notes of the object
	 * @param formatter        a decimal formatter
	 * @param objectdefinition parent objet
	 */
	public DecimalDataObjectFieldDefinition(String name, String displayname, String tooltip, int precision, int scale,
			boolean readonly, boolean showintitle, boolean showinbottompage, SDecimalFormatter formatter,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottompage, objectdefinition);
		this.scale = scale;
		this.precision = precision;
		this.addFieldSchema(new DecimalStoredField(this.getName(), null, precision, scale));
		this.formatter = formatter;
	}

	/**
	 * creates a new field specifying the priority and formatter
	 * 
	 * @param name             unique name of the field
	 * @param displayname      label to show in the main language of the application
	 * @param tooltip          long explanation for mouse roll-over
	 * @param precision        number of significant digits authorized
	 * @param scale            number of digits authorized after comma
	 * @param readonly         true if field is read-only
	 * @param showintitle      show the field in title of the obkect
	 * @param showinbottompage show the field in bottom notes of the object
	 * @param formatter        a decimal formatter
	 * @param priority         priority of the field
	 * @param objectdefinition parent object definition
	 */
	public DecimalDataObjectFieldDefinition(String name, String displayname, String tooltip, int precision, int scale,
			boolean readonly, boolean showintitle, boolean showinbottompage, SDecimalFormatter formatter, int priority,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottompage, priority, -1, objectdefinition);
		this.scale = scale;
		this.precision = precision;
		mainfield = new DecimalStoredField(this.getName(), null, precision, scale);
		this.addFieldSchema(mainfield);
		this.formatter = formatter;
	}

	/**
	 * Creates a new field specifying the priority but keeping the default formatter
	 * 
	 * @param name             unique name of the field
	 * @param displayname      label to show in the main language of the application
	 * @param tooltip          long explanation for mouse roll-over
	 * @param precision        number of significant digits authorized
	 * @param scale            number of digits authorized after comma
	 * @param readonly         true if field is read-only
	 * @param showintitle      show the field in title of the obkect
	 * @param showinbottompage show the field in bottom notes of the object
	 * @param priority         priority of the field
	 * @param objectdefinition parent object definition
	 */
	public DecimalDataObjectFieldDefinition(String name, String displayname, String tooltip, int precision, int scale,
			boolean readonly, boolean showintitle, boolean showinbottompage, int priority,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottompage, priority, -1, objectdefinition);
		this.scale = scale;
		this.precision = precision;
		this.addFieldSchema(new DecimalStoredField(this.getName(), null, precision, scale));

	}

	/**
	 * @return the scale of the field (number of digits authorized after the comma
	 */
	public int getScale() {
		return scale;
	}

	/**
	 * @return the precision of the field (total number of digits)
	 */
	public int getPrecision() {
		return precision;
	}

	@Override
	public SPageNode getDataFieldDefinition() {
		SDecimalField decimalfield = new SDecimalField(this.getDisplayname(), this.getName(), this.getTooltip(),
				this.precision, this.scale, null, true, null, this.isReadOnly(), this.isShowintitle(),
				this.isShowinbottonpage(), formatter, null);
		if (this.getDefaultcolumnintable() >= 0)
			decimalfield.setPreferedDisplayInTable(this.getDefaultcolumnintable());
		return decimalfield;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new DecimalDataObjectField<E>(this, parentpayload);
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, ChoiceValue<ApplocaleChoiceDefinition> locale) {
		return new DecimalDataObjectFieldFlatFileLoaderColumn<E>(objectdefinition, columnattributes, this.getName(),
				scale, precision, locale);
	}

	@Override
	public String[] getLoaderFieldSample() {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		returntable[2] = "-10004.54 (US Locale) or -10.004,54 (French locale) or -10004,54 (French locale)";
		returntable[3] = "no additional parameter for this type,but should respect scale = " + scale + ", precision = "
				+ precision;
		return returntable;
	}

	@Override
	public StoredFieldSchema<?> getMainStoredField() {
		return mainfield;
	}

}
