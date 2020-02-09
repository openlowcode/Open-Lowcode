/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.table;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

import javafx.beans.property.SimpleObjectProperty;

/**
 * The head column inside an object grid
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of object used as payload
 */
public class CObjectGridLineColumn<E extends Comparable<E>>
		extends
		TableColumn<CObjectGridLine<E>, E> {

	@SuppressWarnings("unused")
	private boolean linetitle;
	@SuppressWarnings("unused")
	private String columnname;

	/**
	 * creates the head column
	 * 
	 * @param text label  of the column
	 */
	public CObjectGridLineColumn(String text) {
		super(text);
		this.linetitle = true;
		setEditable(false);
		this.columnname = text;
		setValueFactory();

	}

	private void setValueFactory() {
		setCellValueFactory(new Callback<CellDataFeatures<CObjectGridLine<E>, E>, ObservableValue<E>>() {

			@Override
			public ObservableValue<E> call(javafx.scene.control.TableColumn.CellDataFeatures<CObjectGridLine<E>, E> p) {
				CObjectGridLine<E> line = p.getValue();
				return new SimpleObjectProperty<E>(line.getLabelObject());

			}

		});
	}

}
