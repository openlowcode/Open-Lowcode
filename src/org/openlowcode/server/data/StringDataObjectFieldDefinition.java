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
import org.openlowcode.server.data.storage.FieldSchema;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StoredTableIndex;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.STextField;

/**
 * The definition of a persisted string field on a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class StringDataObjectFieldDefinition<E extends DataObject<E>>
		extends
		DataObjectFieldDefinition<E> {
	private int maxlength;
	private boolean easysearch;
	private boolean richtextedit;
	private boolean orderedasinteger = false;
	private int integeroffset;
	private StringStoredField mainfield;
	private StringStoredField easysearchfield;

	/**
	 * creates a read-write field with default display on a data object
	 * 
	 * @param name             unique name of the field (only characters, short)
	 * @param displayname      display label on the user interface for the field
	 * @param tooltip          tooltip for roll-over mouse
	 * @param maxlength        maximum length of the field in characters
	 * @param easysearch       if true, provides easy search feature
	 * @param objectdefinition definition of the parent data object
	 */
	public StringDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			int maxlength,
			boolean easysearch,
			DataObjectDefinition<E> objectdefinition) {
		this(name, displayname, tooltip, maxlength, easysearch, false, objectdefinition);
	}

	/**
	 * this feature can be used to order the field as an integer, if the string
	 * field is in the shape of a prefix and then numbers (e.g. T-1, T-11 ...). This
	 * feature is important, as in this case, alphanumeric ordering will not work
	 * (you get 1, 11, 2, 3, 31, 4)
	 * 
	 * @param offset size of the text prefix before the number sequence.
	 */
	public void setOrderedAsInteger(int offset) {
		this.orderedasinteger = true;
		this.integeroffset = offset;
	}

	/**
	 * creates a string data object field that can be read-only, shown in title or
	 * shown in bottom notes
	 * 
	 * @param name             unique name of the field (only characters, short)
	 * @param displayname      display label on the user interface for the field
	 * @param tooltip          tooltip for roll-over mouse
	 * @param maxlength        maximum length of the field in characters
	 * @param easysearch       if true, provides easy search feature
	 * @param readonly         if true, field is read-only
	 * @param showintitle      if true, field is shown both in title and main object
	 *                         display
	 * @param showinbottompage if true, field is shown only in bottom notes
	 * @param objectdefinition parent data object definition
	 */
	public StringDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			int maxlength,
			boolean easysearch,
			boolean readonly,
			boolean showintitle,
			boolean showinbottompage,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottompage, objectdefinition);
		this.maxlength = maxlength;
		this.easysearch = easysearch;
		mainfield = new StringStoredField(this.getName(), null, maxlength);
		this.addFieldSchema(mainfield);
		if (easysearch) {
			easysearchfield = new StringStoredField(this.getName() + FieldSchema.EASY_SEARCH_FIELD_SUFFIX, null, maxlength);
			this.addFieldSchema(easysearchfield);
			StoredTableIndex easysearchindex = new StoredTableIndex(this.getName() + FieldSchema.EASY_SEARCH_FIELD_SUFFIX);
			easysearchindex.addStoredFieldSchame(easysearchfield);
			this.addIndex(easysearchindex);
		}
	}

	/**
	 * @param name             unique name of the field (only characters, short)
	 * @param displayname      display label on the user interface for the field
	 * @param tooltip          tooltip for roll-over mouse
	 * @param maxlength        maximum length of the field in characters
	 * @param easysearch       if true, provides easy search feature
	 * @param readonly         if true, field is read-only
	 * @param objectdefinition parent data object definition
	 */
	public StringDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			int maxlength,
			boolean easysearch,
			boolean readonly,
			DataObjectDefinition<E> objectdefinition) {
		this(name, displayname, tooltip, maxlength, easysearch, readonly, false, false, objectdefinition);
	}

	/**
	 * @param name             unique name of the field (only characters, short)
	 * @param displayname      display label on the user interface for the field
	 * @param tooltip          tooltip for roll-over mouse
	 * @param maxlength        maximum length of the field in characters
	 * @param easysearch       if true, provides easy search feature
	 * @param readonly         if true, field is read-only
	 * @param showintitle      if true, field is shown both in title and main object
	 *                         display
	 * @param showinbottompage if true, field is shown only in bottom notes
	 * @param priority         field priority,a number between -1000 and 1000
	 * @param defaultcolumn    size of the field in characters when displayed in
	 *                         table
	 * @param richtextedit     if true, field is rich text (allows some limited
	 *                         formatting)
	 * @param objectdefinition parent data object definition
	 */
	public StringDataObjectFieldDefinition(
			String name,
			String displayname,
			String tooltip,
			int maxlength,
			boolean easysearch,
			boolean readonly,
			boolean showintitle,
			boolean showinbottompage,
			int priority,
			int defaultcolumn,
			boolean richtextedit,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottompage, priority, defaultcolumn,
				objectdefinition);
		this.maxlength = maxlength;
		this.easysearch = easysearch;
		this.richtextedit = richtextedit;
		mainfield = new StringStoredField(this.getName(), null, maxlength);
		this.addFieldSchema(mainfield);
		if (easysearch) {
			easysearchfield = new StringStoredField(this.getName() + FieldSchema.EASY_SEARCH_FIELD_SUFFIX, null, maxlength);
			this.addFieldSchema(easysearchfield);
			StoredTableIndex easysearchindex = new StoredTableIndex(this.getName() + FieldSchema.EASY_SEARCH_FIELD_SUFFIX);
			easysearchindex.addStoredFieldSchame(easysearchfield);
			this.addIndex(easysearchindex);
		}
	}

	/**
	 * @return
	 */
	public int getMaxlength() {
		return maxlength;
	}

	/**
	 * @return
	 */
	public boolean isEasysearch() {
		return easysearch;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectField initiateFieldInstance(DataObjectPayload parentpayload) {
		return new StringDataObjectField<E>(this, parentpayload);
	}

	@Override
	public SPageNode getDataFieldDefinition() {
		STextField textfield = new STextField(this.getDisplayname(), this.getName(), this.getTooltip(),
				this.getMaxlength(), null, true, null, this.isReadOnly(), this.isShowintitle(),
				this.isShowinbottonpage(), null, this.richtextedit, true);
		if (this.orderedasinteger)
			textfield.setOrderAsInteger(this.integeroffset);
		if (this.getDefaultcolumnintable() >= 0)
			textfield.setPreferedDisplayInTable(this.getDefaultcolumnintable());
		return textfield;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public  FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition,
			String[] columnattributes,
			ChoiceValue<ApplocaleChoiceDefinition> locale) {
		boolean truncate = true;
		if (columnattributes != null)
			for (int i = 0; i < columnattributes.length; i++) {
				if ("NOTRUNCATE".equals(columnattributes[i]))
					truncate = false;
			}
		return new StringDataObjectFieldFlatFileLoaderColumn(objectdefinition, columnattributes, this.getName(),
				maxlength, richtextedit, truncate);
	}

	@Override
	public String[] getLoaderFieldSample() {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		returntable[2] = "any text";
		returntable[3] = "you can specify the NOTRUNCATE tag, in that case, an error is filled if text is too long, else text is truncated. This column has maxlength of "
				+ this.maxlength + ". To escape texte, put it between double quotes '\"', doubling double quote inside";
		return returntable;
	}

	@Override
	public StoredFieldSchema<?> getMainStoredField() {

		return this.mainfield;
	}

	/**
	 * @return true if field is rich text
	 */
	public boolean isRichtextedit() {
		return richtextedit;
	}

}
