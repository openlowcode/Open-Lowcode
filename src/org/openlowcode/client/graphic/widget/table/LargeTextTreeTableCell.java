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

import java.util.logging.Logger;

import org.openlowcode.client.graphic.widget.ValueFormatter;
import org.openlowcode.client.graphic.widget.fields.FormatValidator;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTablePosition;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;

/**
 * A tree table cell displaying correctly a large text over several lines and
 * allowing edition too
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <S> type of the row
 * @param <T> type of the column
 */
public class LargeTextTreeTableCell<S, T>
		extends
		TreeTableCell<S, T> {
	private static Logger logger = Logger.getLogger(LargeTextTreeTableCell.class.getName());

	private TextInputControl textField;
	private FormatValidator entryrestrictions;
	private ValueFormatter<T> readonlyformatter;
	private boolean alignonright;
	private boolean largedisplay;

	/**
	 * Create a Large Text Tree Table Cell with content as text aligned on left
	 * 
	 * @param largedisplay true if several lines (large display)
	 */
	public LargeTextTreeTableCell(boolean largedisplay) {
		this(null, largedisplay);

	}

	/**
	 * Create a Large Text Tree Table Cell aligned on left
	 * 
	 * @param converter    converter to String
	 * @param largedisplay true if several lines (large display)
	 */
	public LargeTextTreeTableCell(StringConverter<T> converter, boolean largedisplay) {
		setConverter(converter);
		this.alignonright = false;
		this.largedisplay = largedisplay;
		if (this.largedisplay) {
			this.getStyleClass().add("text-area-table-cell");
		} else {
			this.getStyleClass().add("text-field-table-cell");
		}
	}

	/**
	 * Create a Large Text Tree Table Cell aligned on left
	 * 
	 * @param converter         converter to String
	 * @param entryrestrictions entry restrictions
	 * @param readonlyformatter foramtter for object as read-only
	 * @param largedisplay      true if several lines (large display)
	 */
	public LargeTextTreeTableCell(
			StringConverter<T> converter,
			FormatValidator entryrestrictions,
			ValueFormatter<T> readonlyformatter,
			boolean largedisplay) {

		setConverter(converter);
		this.entryrestrictions = entryrestrictions;
		this.readonlyformatter = readonlyformatter;
		this.alignonright = false;
		this.largedisplay = largedisplay;
		if (this.largedisplay) {
			this.getStyleClass().add("text-area-table-cell");
		} else {
			this.getStyleClass().add("text-field-table-cell");
		}
	}

	/**
	 * Create a Large Text Tree Table Cell
	 * 
	 * @param converter         converter to String
	 * @param entryrestrictions entry restrictions
	 * @param readonlyformatter foramtter for object as read-only
	 * @param largedisplay      true if several lines (large display)
	 * @param alignonright      align on right
	 */
	public LargeTextTreeTableCell(
			StringConverter<T> converter,
			FormatValidator entryrestrictions,
			ValueFormatter<T> readonlyformatter,
			boolean largedisplay,
			boolean alignonright) {

		setConverter(converter);
		this.entryrestrictions = entryrestrictions;
		this.readonlyformatter = readonlyformatter;
		this.alignonright = alignonright;
		this.largedisplay = largedisplay;
		if (this.largedisplay) {
			this.getStyleClass().add("text-area-table-cell");
		} else {
			this.getStyleClass().add("text-field-table-cell");
		}
		if (alignonright)
			this.setAlignment(Pos.TOP_RIGHT);
	}

	private ObjectProperty<
			StringConverter<T>> converter = new SimpleObjectProperty<StringConverter<T>>(this, "converter");
	private String oldtext;

	/**
	 * @return get the converter property
	 */
	public final ObjectProperty<StringConverter<T>> converterProperty() {
		return converter;
	}

	/**
	 * @param value sets the converter property
	 */
	public final void setConverter(StringConverter<T> value) {
		converterProperty().set(value);
	}

	/**
	 * @return get the converter
	 */
	public final StringConverter<T> getConverter() {
		return converterProperty().get();
	}

	@Override
	public void startEdit() {
		if (!isEditable() || !getTreeTableView().isEditable() || !getTableColumn().isEditable()) {
			return;
		}
		super.startEdit();

		if (isEditing()) {
			if (textField == null) {
				textField = createTextField(this, getConverter(), entryrestrictions, this.largedisplay,
						this.alignonright);
			}
			oldtext = getItemText(this, getConverter());
			startEdit(this, getConverter(), null, null, textField);
		}
	}

	@Override
	public void cancelEdit() {
		T item = null;

		if (textField.getText() != null)
			item = getConverter().fromString(textField.getText());
		logger.finer("cancel edit bypassed, new data = " + textField.getText() + " - " + item);
		updateItem(item, (item == null ? true : false));
		this.commitEdit(item);
	}

	@Override
	public void updateItem(T item, boolean empty) {
		super.updateItem(item, empty);
		logger.fine("edit " + item);
		updateItem(this, getConverter(), null, null, textField, readonlyformatter, item);
	}

	/**
	 * cancel the edit
	 * 
	 * @param cell      cell
	 * @param converter string converter
	 * @param graphic   node content
	 */
	static <T> void cancelEdit(Cell<T> cell, final StringConverter<T> converter, Node graphic) {
		cell.setText(getItemText(cell, converter));
		cell.setGraphic(graphic);
	}

	/**
	 * get the item text
	 * 
	 * @param cell      cell
	 * @param converter string converter
	 * @return item text
	 */
	private static <T> String getItemText(Cell<T> cell, StringConverter<T> converter) {
		return converter == null ? cell.getItem() == null ? "" : cell.getItem().toString()
				: converter.toString(cell.getItem());
	}

	/**
	 * update the item
	 * 
	 * @param cell              cell
	 * @param converter         converter between payload and string
	 * @param hbox              hbox inside the cell
	 * @param graphic           graphic node of the cell
	 * @param textField         text field
	 * @param readonlyformatter formatter for read-only
	 * @param item              payload item
	 */
	static <T> void updateItem(
			final Cell<T> cell,
			final StringConverter<T> converter,
			final HBox hbox,
			final Node graphic,
			final TextInputControl textField,
			ValueFormatter<T> readonlyformatter,
			T item) {
		if (cell.isEmpty()) {
			cell.setText(null);
			cell.setGraphic(null);
		} else {
			if (cell.isEditing()) {
				if (textField != null) {
					textField.setText(getItemText(cell, converter));
				}
				cell.setText(null);

				if (graphic != null) {
					hbox.getChildren().setAll(graphic, textField);
					cell.setGraphic(hbox);
				} else {
					cell.setGraphic(textField);
				}
			} else {
				if (readonlyformatter == null)
					cell.setText(getItemText(cell, converter));
				if (readonlyformatter != null)
					cell.setGraphic(readonlyformatter.getWidget(item));

				cell.setGraphic(graphic);
			}
		}
	}

	/**
	 * start edition to a cell
	 * 
	 * @param cell      cell
	 * @param converter converter between payload and string
	 * @param hbox      hbox inside the cell
	 * @param graphic   graphic node of the cell
	 * @param textField text field
	 */
	static <T> void startEdit(
			final Cell<T> cell,
			final StringConverter<T> converter,
			final HBox hbox,
			final Node graphic,
			final TextInputControl textField) {
		if (textField != null) {
			textField.setText(getItemText(cell, converter));
		}
		cell.setText(null);

		if (graphic != null) {
			hbox.getChildren().setAll(graphic, textField);
			cell.setGraphic(hbox);
		} else {
			cell.setGraphic(textField);
		}
		textField.selectAll();
		textField.requestFocus();
	}

	/**
	 * creates the text field
	 * 
	 * @param cell              cell
	 * @param converter         converter between payload and string
	 * @param entryrestrictions restrictions in entry
	 * @param largedisplay      if true, large display
	 * @param alignright        true to align on right
	 * @return
	 */
	static <S, T> TextInputControl createTextField(
			final LargeTextTreeTableCell<S, T> cell,
			final StringConverter<T> converter,
			FormatValidator entryrestrictions,
			boolean largedisplay,
			boolean alignright) {
		TextInputControl primaryinputcontrol = null;
		if (largedisplay) {
			TextArea textarea = new TextArea(getItemText(cell, converter));
			textarea.setWrapText(true);
			if (alignright)
				textarea.getStyleClass().add("rightTextArea");

			primaryinputcontrol = textarea;

		} else {
			TextField textField = new TextField(getItemText(cell, converter));
			if (alignright)
				textField.setAlignment(Pos.CENTER_RIGHT);
			primaryinputcontrol = textField;
		}

		final TextInputControl textic = primaryinputcontrol;
		if (entryrestrictions != null) {
			textic.textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldvalue, String newvalue) {
					String result = entryrestrictions.valid(newvalue);
					if (newvalue.length() == 0)
						result = "";
					final String finalresult = result;
					if (result == null) {
						int caretposition = textic.getCaretPosition();
						textic.setText(oldvalue);
						if (caretposition > oldvalue.length())
							caretposition = oldvalue.length();
						textic.positionCaret(caretposition);

					} else {
						int caretposition = textic.getCaretPosition();
						textic.setText(finalresult);
						caretposition = finalresult.length() - newvalue.length() + caretposition;
						if (caretposition > newvalue.length())
							caretposition = newvalue.length();

						textic.positionCaret(caretposition);
						// });
					}

				}

			});
		}
		textic.setOnKeyPressed(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent t) {
				if (t.getCode() == KeyCode.ESCAPE) {
					textic.setText(cell.oldtext);
					cell.cancelEdit();
					t.consume();
				}
			}
		});

		textic.setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent t) {
				if ((t.getCode() == KeyCode.ENTER) && (!t.isShiftDown())) {
					if (converter == null)
						throw new RuntimeException("StringConverter is null.");
					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();

					TreeTablePosition<S, ?> position = cell.getTreeTableView().getFocusModel().getFocusedCell();
					cell.getTreeTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());

				}

				if ((t.getCode() == KeyCode.ENTER) && (t.isShiftDown())) {
					int caretposition = textic.getCaretPosition();
					String textbefore = textic.getText().substring(0, caretposition);
					String textafter = textic.getText().substring(caretposition);
					t.consume();

					textic.setText(textbefore + "\n" + textafter);
					textic.selectPositionCaret(caretposition + 1);
					textic.deselect();

				}
				if ((t.getCode() == KeyCode.RIGHT) && (t.isShiftDown())) {
					logger.fine("in the code for shift right");
					if (converter == null)
						throw new RuntimeException("StringConverter is null.");
					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();
					cell.getTreeTableView().getFocusModel().focusRightCell();
					TreeTablePosition<S, ?> position = cell.getTreeTableView().getFocusModel().getFocusedCell();
					cell.getTreeTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTreeTableView().edit(position.getRow(), position.getTableColumn());
				}
				if ((t.getCode() == KeyCode.LEFT) && (t.isShiftDown())) {
					if (converter == null)
						throw new RuntimeException("StringConverter is null.");
					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();
					cell.getTreeTableView().getFocusModel().focusLeftCell();
					TreeTablePosition<S, ?> position = cell.getTreeTableView().getFocusModel().getFocusedCell();
					cell.getTreeTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTreeTableView().edit(position.getRow(), position.getTableColumn());

				}

				if ((t.getCode() == KeyCode.UP) && (t.isShiftDown())) {
					if (converter == null)
						throw new RuntimeException("StringConverter is null.");

					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();

					cell.getTreeTableView().getFocusModel().focusAboveCell();
					TreeTablePosition<S, ?> position = cell.getTreeTableView().getFocusModel().getFocusedCell();
					cell.getTreeTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTreeTableView().edit(position.getRow(), position.getTableColumn());

				}
				if ((t.getCode() == KeyCode.DOWN) && (t.isShiftDown())) {
					if (converter == null)
						throw new RuntimeException("StringConverter is null.");

					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();
					TreeTablePosition<S, ?> position = cell.getTreeTableView().getFocusModel().getFocusedCell();
					cell.getTreeTableView().getFocusModel().focusBelowCell();
					cell.getTreeTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTreeTableView().edit(position.getRow(), position.getTableColumn());

				}

			}
		});

		return textic;
	}
}
