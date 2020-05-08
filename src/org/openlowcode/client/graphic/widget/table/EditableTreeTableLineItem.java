/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.table;

import java.util.ArrayList;

/**
 * A line item for the EditableTreeTable
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> payload object managed in the editable tree table
 */
public class EditableTreeTableLineItem<E extends Object> {
	private ArrayList<E> relevantitems;
	private String label;
	/**
	 * @param label label of the line item
	 * @param relevantitems number of items in this line-item
	 */
	@SuppressWarnings("unchecked")
	public EditableTreeTableLineItem(String label,ArrayList<E> relevantitems) {
		this.relevantitems = (ArrayList<E>) relevantitems.clone();
		this.label=label;
	}
	/**
	 * @return the number of items
	 */
	public int getItemsNumber() {
		return relevantitems.size();
	}
	
	/**
	 * @param index the index of element to send back
	 * @return the element at the given index in the list of items for this line
	 */
	public E getItemAt(int index) {
		return relevantitems.get(index);
	}
	/**
	 * @return the label for this line
	 */
	public String getLabel() {
		return this.label;
	}

}
