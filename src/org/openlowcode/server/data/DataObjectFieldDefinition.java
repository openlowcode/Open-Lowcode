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
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.graphic.SPageNode;

/**
 * the definition of a field on a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
@SuppressWarnings("rawtypes")
public abstract class DataObjectFieldDefinition<E extends DataObject<E>>
		extends DataObjectElementDefinition<StoredFieldSchema, E> {

	private int priority;
	private int defaultcolumnintable;
	private DataObjectDefinition<E> definition;

	/**
	 * @return the parent object definition for this field
	 */
	public DataObjectDefinition<E> getObjectDefinition() {
		return this.definition;
	}

	/**
	 * @return the priority used to order the field for display
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the size of the column for display in table
	 */
	public int getDefaultcolumnintable() {
		return defaultcolumnintable;
	}

	/**
	 * Creates a data object field definition shown as a normal field
	 * 
	 * @param name        unique id of the field
	 * @param displayname display name of the field in the default language
	 * @param tooltip     tooltip for mouse rollover
	 * @param readonly    true if read-only, false if read-write
	 * @param definition  definition of the parent data object
	 */
	public DataObjectFieldDefinition(String name, String displayname, String tooltip, boolean readonly,
			DataObjectDefinition<E> definition) {
		this(name, displayname, tooltip, readonly, false, false, definition);

	}

	/**
	 * Creates a data object field definition with priority of zero
	 * 
	 * @param name             unique id of the field
	 * @param displayname      display name of the field in the default language
	 * @param tooltip          tooltip for mouse rollover
	 * @param readonly         true if read-only, false if read-write
	 * @param showintitle      true if field is shown also in title
	 * @param showinbottompage true if shown in bottom notes
	 * @param definition       definition of the parent object
	 */
	public DataObjectFieldDefinition(String name, String displayname, String tooltip, boolean readonly,
			boolean showintitle, boolean showinbottompage, DataObjectDefinition<E> definition) {
		super(name);
		this.displayname = displayname;
		this.tooltip = tooltip;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottonpage = showinbottompage;
		this.priority = 0;
		this.defaultcolumnintable = -1;
		this.definition = definition;
	}

	/**
	 * Crates a data object field with specified priority and size for table display
	 * 
	 * @param name                 unique id of the field
	 * @param displayname          display name of the field in the default language
	 * @param tooltip              tooltip for mouse rollover
	 * @param readonly             true if read-only, false if read-write
	 * @param showintitle          true if field is shown also in title
	 * @param priority             priority for display
	 * @param defaultcolumnintable size of the field when displayed in table
	 * @param definition           definition of the parent object
	 */
	public DataObjectFieldDefinition(String name, String displayname, String tooltip, boolean readonly,
			boolean showintitle, boolean showinbottompage, int priority, int defaultcolumnintable,
			DataObjectDefinition<E> definition) {
		super(name);
		this.displayname = displayname;
		this.tooltip = tooltip;
		this.readonly = readonly;
		this.showintitle = showintitle;
		this.showinbottonpage = showinbottompage;
		this.priority = priority;
		this.defaultcolumnintable = defaultcolumnintable;
		this.definition = definition;
	}

	/**
	 * Creates a data object field displayed as normal field (neither title nor
	 * bottom notes)
	 * 
	 * @param name                 unique id of the field
	 * @param displayname          display name of the field in the default language
	 * @param tooltip              tooltip for mouse rollover
	 * @param readonly             true if read-only, false if read-write
	 * @param priority             priority for display
	 * @param defaultcolumnintable size in table
	 * @param definition           definition of the parent object
	 */
	public DataObjectFieldDefinition(String name, String displayname, String tooltip, boolean readonly, int priority,
			int defaultcolumnintable, DataObjectDefinition<E> definition) {
		this(name, displayname, tooltip, readonly, false, false, priority, defaultcolumnintable, definition);
	}

	private String displayname;
	private String tooltip;
	private boolean readonly;
	private boolean showintitle;
	private boolean showinbottonpage;

	/**
	 * @return true if field is readonly
	 */
	public boolean isReadOnly() {
		return this.readonly;
	}

	/**
	 * @return the display name
	 */
	public String getDisplayname() {
		return displayname;
	}

	/**
	 * @return the tooltip for mouse rollover
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @return true if field is shown in title
	 */
	public boolean isShowintitle() {
		return showintitle;
	}

	/**
	 * @return true if field is shown in bottom notes
	 */
	public boolean isShowinbottonpage() {
		return showinbottonpage;
	}

	/**
	 * @return the widget to use for display in object
	 */
	public abstract SPageNode getDataFieldDefinition();

	/**
	 * generates the flat file loader for this property
	 * 
	 * @param objectdefinition definition of the object
	 * @param columnattributes column attributes
	 * @param locale           locale if loaded by CSV
	 * @return a flat file loader
	 */
	public abstract  FlatFileLoaderColumn<E> getFlatFileLoaderColumn(
			DataObjectDefinition<E> objectdefinition, String[] columnattributes,
			ChoiceValue<ApplocaleChoiceDefinition> locale);

	/**
	 * @param name name as returned by the function getLoaderFieldList
	 * @return an array of 4 strings
	 *         <ul>
	 *         <li>full name of the field</li>
	 *         <li>OPTIONAL or MANDATORY</li>
	 *         <li>Sample Values</li>
	 *         <li>Comments including options</li>
	 *         </ul>
	 */
	public abstract String[] getLoaderFieldSample();

	/**
	 * @return the default stored field schema used when searching on this field
	 */
	public abstract StoredFieldSchema getMainStoredField();
}
