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

import org.openlowcode.server.data.storage.TimestampStoredField;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.widget.SDateField;

/**
 * Definition of a field having as payload a java date (with information on date
 * and time)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the parent data object
 */
public class DateDataObjectFieldDefinition<E extends DataObject<E>> extends DataObjectFieldDefinition<E> {
	private boolean timeedit;
	private TimestampStoredField mainfield;

	/**
	 * Creates a read-write date field definition
	 * 
	 * @param name             unique name of the field for the object (not too long
	 *                         as used in the database)
	 * @param displayname      display name of the field (shown in the user
	 *                         interface)
	 * @param tooltip          tooltip for mouse rollover
	 * @param timeedit         true if allows time edition, false if only date
	 * @param objectdefinition parent object definition
	 */
	public DateDataObjectFieldDefinition(String name, String displayname, String tooltip, boolean timeedit,
			DataObjectDefinition<E> objectdefinition) {
		this(name, displayname, tooltip, timeedit, false, objectdefinition);

	}

	/**
	 * 
	 * 
	 * @param name             unique name of the field for the object (not too long
	 *                         as used in the database)
	 * @param displayname      display name of the field (shown in the user
	 *                         interface)
	 * @param tooltip          tooltip for mouse rollover
	 * @param timeedit         true if allows time edition, false if only date
	 * @param readonly         true if read-only, false if read-write
	 * @param priority         priority for field display in user interface
	 * @param objectdefinition parent object definition
	 */
	public DateDataObjectFieldDefinition(String name, String displayname, String tooltip, boolean timeedit,
			boolean readonly, int priority, DataObjectDefinition<E> objectdefinition) {
		this(name, displayname, tooltip, timeedit, false, false, false, priority, objectdefinition);

	}

	/**
	 * Creates a read-write or read-only date field definition that can be shown in
	 * title or bottom notes
	 * 
	 * @param name              unique name of the field for the object (not too
	 *                          long as used in the database)
	 * @param displayname       display name of the field (shown in the user
	 *                          interface)
	 * @param tooltip           tooltip for mouse rollover
	 * @param timeedit          true if allows time edition, false if only date
	 * @param readonly          true if read-only, false if read-write
	 * @param showintitle       true if field shown in title
	 * @param showinbottomnotes true if field shown in bottom notes
	 * @param objectdefinition  parent object definition
	 */
	public DateDataObjectFieldDefinition(String name, String displayname, String tooltip, boolean timeedit,
			boolean readonly, boolean showintitle, boolean showinbottomnotes,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottomnotes, objectdefinition);
		mainfield = new TimestampStoredField(this.getName(), objectdefinition.getTableschema());
		this.addFieldSchema(mainfield);
		this.timeedit = timeedit;
	}

	/**
	 * Creates a read-write or read-only date field definition
	 * 
	 * @param name             unique name of the field for the object (not too long
	 *                         as used in the database)
	 * @param displayname      display name of the field (shown in the user
	 *                         interface)
	 * @param tooltip          tooltip for mouse rollover
	 * @param timeedit         true if allows time edition, false if only date
	 * @param readonly         true if read-only, false if read-write
	 * @param objectdefinition parent object definition
	 */
	public DateDataObjectFieldDefinition(String name, String displayname, String tooltip, boolean timeedit,
			boolean readonly, DataObjectDefinition<E> objectdefinition) {
		this(name, displayname, tooltip, timeedit, readonly, false, false, objectdefinition);

	}

	/**
	 * Creates a read-write or read-only date field definition that can be shown in
	 * title or bottom notes
	 * 
	 * @param name              unique name of the field for the object (not too
	 *                          long as used in the database)
	 * @param displayname       display name of the field (shown in the user
	 *                          interface)
	 * @param tooltip           tooltip for mouse rollover
	 * @param timeedit          true if allows time edition, false if only date
	 * @param readonly          true if read-only, false if read-write
	 * @param showintitle       true if field shown in title
	 * @param showinbottomnotes true if field shown in bottom notes
	 * @param priority          priority for field display in user interface
	 * @param objectdefinition  parent object definition
	 */
	public DateDataObjectFieldDefinition(String name, String displayname, String tooltip, boolean timeedit,
			boolean readonly, boolean showintitle, boolean showinbottomnotes, int priority,
			DataObjectDefinition<E> objectdefinition) {
		super(name, displayname, tooltip, readonly, showintitle, showinbottomnotes, priority, -1, objectdefinition);
		mainfield = new TimestampStoredField(this.getName(), objectdefinition.getTableschema());
		this.addFieldSchema(mainfield);
		this.timeedit = timeedit;
	}

	@Override
	public SPageNode getDataFieldDefinition() {
		SDateField datefield = new SDateField(this.getDisplayname(), this.getName(), this.getTooltip(),
				SDateField.DEFAULT_EMPTY, false, this.timeedit, null, this.isReadOnly(), this.isShowintitle(),
				this.isShowinbottonpage(), null);
		return datefield;
	}

	@Override
	public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, ChoiceValue<ApplocaleChoiceDefinition> locale) {

		return new DateDataObjectFieldFlatFileLoader<E>(objectdefinition, columnattributes, this.getName(), timeedit);
	}

	@Override
	public String[] getLoaderFieldSample() {
		String[] returntable = new String[4];
		returntable[0] = this.getName();
		returntable[1] = "OPTIONAL";
		returntable[2] = "2017.02.28";
		returntable[3] = "Optional parameter: specific java DateTimeFormatter \n (e.g. dd'/'MM'/'yyyy HH:mm for 03/01/2018 08:45  )\n definition at https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html";
		return returntable;
	}

	@Override
	public TimestampStoredField getMainStoredField() {
		return mainfield;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
		return new DateDataObjectField<E>(this, parentpayload);
	}

}
