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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.PropertyDynamicDefinitionHelper;
import org.openlowcode.server.data.DataObjectPropertyDefinition.FieldSchemaForDisplay;
import org.openlowcode.server.data.helpers.ReportTree;
import org.openlowcode.server.data.specificstorage.TransientBigDecimal;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.misc.Named;

/**
 * the helper defines at runtime the list of fields that will be part of the
 * flexible decimal fields definition
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object
 */
public class FlexibledecimalfieldsDefinitionDynamicHelper<E extends DataObject<E> & FlexibledecimalfieldsInterface<E>>
		extends PropertyDynamicDefinitionHelper<E, Flexibledecimalfields<E>> implements ReportTree.Consolidator<E> {
	private NamedList<FlexibleDecimalField> fieldlist;
	private DataObjectDefinition<E> definition;

	/**
	 * creates an helper for the given definition
	 * 
	 * @param definition definition of the parent data object
	 */
	public FlexibledecimalfieldsDefinitionDynamicHelper(DataObjectDefinition<E> definition) {
		fieldlist = new NamedList<FlexibleDecimalField>();
		this.definition = definition;
	}

	/**
	 * gets the number of fields in this dynamic helper
	 * 
	 * @return the number of fields
	 */
	public int getFieldNumber() {
		return fieldlist.getSize();
	}

	/**
	 * gets the field at the given index
	 * 
	 * @param index a number between 0 (included) and getFieldNumber (excluded)
	 * @return the name of the field
	 */
	public String getFieldNameAtIndex(int index) {
		return fieldlist.get(index).getName();
	}

	/**
	 * the actual fields contained in the flexible decimal field helper
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	private class FlexibleDecimalField extends Named {
		private String label;
		private int priority;

		/**
		 * creates a flexible decimal field
		 * 
		 * @param name     unique short name
		 * @param label    label to display in default language
		 * @param priority priority of the field for display
		 */
		public FlexibleDecimalField(String name, String label, int priority) {
			super(name);
			this.label = label;
			this.priority = priority;
		}

		/**
		 * get the label
		 * 
		 * @return label
		 */
		public String getLabel() {
			return label;
		}

		/**
		 * gets the priority
		 * 
		 * @return priority
		 */
		public int getPriority() {
			return priority;
		}

	}

	/**
	 * checks if the field with the given name exists
	 * 
	 * @param name name of the field
	 * @return true if the field exists
	 */
	public boolean hasField(String name) {
		if (fieldlist.lookupOnName(name) != null)
			return true;
		return false;
	}

	/**
	 * gets the list of field names
	 * 
	 * @return the list of field names for logging purposes
	 */
	public String dropValidFieldNames() {
		return fieldlist.dropNameList();
	}

	/**
	 * adds a field to the dynamic helper
	 * 
	 * @param name     name of the field
	 * @param label    label of the field
	 * @param priority priority of the field
	 */
	public void addField(String name, String label, int priority) {
		fieldlist.add(new FlexibleDecimalField(name, label, priority));
	}

	@SuppressWarnings("unchecked")
	@Override
	public FieldSchemaForDisplay<E>[] getFieldsToDisplay() {
		ArrayList<FieldSchemaForDisplay<E>> fieldstodisplay = new ArrayList<FieldSchemaForDisplay<E>>();
		for (int i = 0; i < fieldlist.getSize(); i++) {
			FlexibleDecimalField thisfield = fieldlist.get(i);
			fieldstodisplay.add(
					new FieldSchemaForDisplay<E>(thisfield.getLabel(), "", new TransientBigDecimal(thisfield.getName()),
							false, false, thisfield.getPriority(), i, definition, 0));
		}
		return (FieldSchemaForDisplay<E>[]) fieldstodisplay.toArray(new FieldSchemaForDisplay[0]);
	}

	@Override
	public void consolidate(E parent, E child) {
		for (int i = 0; i < fieldlist.getSize(); i++) {
			FlexibledecimalfieldsDefinitionDynamicHelper<E>.FlexibleDecimalField field = fieldlist.get(i);
			String fieldname = field.getName();
			ReportTree.sumInparent(parent, child, a -> a.getflexibledecimalvalue(fieldname),
					(a, b) -> a.addflexibledecimalvalue(field.getName(), b));
		}

	}

}
