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

import javafx.scene.input.ContextMenuEvent;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.StringConverter;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.KeyCode;
import javafx.event.Event;

/**
 * A table cell displaying correctly a large text over several lines and
 * allowing edition too
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <S> type of the row
 * @param <T> type of the column
 */
public class LargeTextTableCell<S, T>
		extends
		TableCell<S, T> {
	private static Logger logger = Logger.getLogger(LargeTextTableCell.class.getName());

	private TextInputControl textField;
	private FormatValidator<?> entryrestrictions;
	private ValueFormatter<T> readonlyformatter;
	private boolean alignonright;
	private boolean largedisplay;
	private double lineheightfortable;

	/**
	 * creates a large text table cell with no restriction for data entry and
	 * aligned on left
	 * 
	 * @param converter    converter to generate the object from string entered
	 * @param largedisplay true if several lines (large display)
	 * @param lineheight   line height in pixel
	 */
	public LargeTextTableCell(StringConverter<T> converter, boolean largedisplay, double lineheight) {
		this.lineheightfortable = lineheight;
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
	 * create a large text table cell aligned on left
	 * 
	 * @param converter         converter to generate the object from strin entered
	 * @param entryrestrictions restrictions on text entry
	 * @param readonlyformatter foratter for read-only
	 * @param largedisplay      true if several lines (large display)
	 * @param lineheight        line height in pixel
	 */
	public LargeTextTableCell(
			StringConverter<T> converter,
			FormatValidator<?> entryrestrictions,
			ValueFormatter<T> readonlyformatter,
			boolean largedisplay,
			double lineheight) {
		this.lineheightfortable = lineheight;
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
	 * create a large text table cell
	 * 
	 * @param converter         converter to generate the object from strin entered
	 * @param entryrestrictions restrictions on text entry
	 * @param readonlyformatter foratter for read-only
	 * @param largedisplay      true if several lines (large display)
	 * @param alignonright      true to align on right
	 * @param lineheight        line height in pixel
	 */
	public LargeTextTableCell(
			StringConverter<T> converter,
			FormatValidator<?> entryrestrictions,
			ValueFormatter<T> readonlyformatter,
			boolean largedisplay,
			boolean alignonright,
			double lineheight) {
		this.lineheightfortable = lineheight;
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

	public final ObjectProperty<StringConverter<T>> converterProperty() {
		return converter;
	}

	public final void setConverter(StringConverter<T> value) {
		converterProperty().set(value);
	}

	public final StringConverter<T> getConverter() {
		return converterProperty().get();
	}

	@Override
	public void startEdit() {
		if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
			return;
		}
		super.startEdit();

		if (isEditing()) {
			if (textField == null) {
				textField = createTextField(this, getConverter(), entryrestrictions, this.largedisplay,
						this.alignonright, lineheightfortable);
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

	/***************************************************************************************
	 * H E L P E R . M E T H O D S (from Cell Utils)
	 ***************************************************************************************/

	static <T> void cancelEdit(Cell<T> cell, final StringConverter<T> converter, Node graphic) {
		cell.setText(getItemText(cell, converter));
		cell.setGraphic(graphic);
	}

	private static <T> String getItemText(Cell<T> cell, StringConverter<T> converter) {
		return converter == null ? cell.getItem() == null ? "" : cell.getItem().toString()
				: converter.toString(cell.getItem());
	}

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
	 * creates a text input control
	 * 
	 * @param cell              cell
	 * @param converter         text converter
	 * @param entryrestrictions restrictions on text entry
	 * @param largedisplay      true if large display (several lines)
	 * @param alignright        true if align on right, false if align on left
	 * @param lineheight        line height in pixel
	 * @return a text input control to be used a read-write component in the table
	 */
	static <S, T> TextInputControl createTextField(
			final LargeTextTableCell<S, T> cell,
			final StringConverter<T> converter,
			FormatValidator<?> entryrestrictions,
			boolean largedisplay,
			boolean alignright,
			double lineheight) {
		TextInputControl primaryinputcontrol = null;
		if (largedisplay) {
			TextArea textarea = new TextArea(getItemText(cell, converter));
			textarea.setWrapText(true);
			textarea.setMaxHeight(lineheight);
			logger.finest(" text are max height = " + lineheight);
			if (alignright)
				textarea.getStyleClass().add("rightTextArea");
			textarea.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, new EventHandler<Event>() {

				@Override
				public void handle(Event event) {
					event.consume();
					textarea.getParent().fireEvent(event);
					logger.finest("test");

				}

			});
			primaryinputcontrol = textarea;

		} else {
			TextField textField = new TextField(getItemText(cell, converter));
			if (alignright)
				textField.setAlignment(Pos.CENTER_RIGHT);
			textField.addEventFilter(ContextMenuEvent.CONTEXT_MENU_REQUESTED, new EventHandler<Event>() {

				@Override
				public void handle(Event event) {
					event.consume();
					textField.getParent().fireEvent(event);

				}

			});
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
					if (converter == null) {
						throw new IllegalStateException("Attempting to convert text input into Object, but provided "
								+ "StringConverter is null. Be sure to set a StringConverter "
								+ "in your cell factory.");
					}
					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();
					@SuppressWarnings("unchecked")
					TablePosition<S, T> position = cell.getTableView().getFocusModel().getFocusedCell();
					cell.getTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTableView().requestFocus();
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
					if (converter == null) {
						throw new IllegalStateException("Attempting to convert text input into Object, but provided "
								+ "StringConverter is null. Be sure to set a StringConverter "
								+ "in your cell factory.");
					}
					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();
					cell.getTableView().getFocusModel().focusRightCell();
					@SuppressWarnings("unchecked")
					TablePosition<S, T> position = cell.getTableView().getFocusModel().getFocusedCell();
					cell.getTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTableView().edit(position.getRow(), position.getTableColumn());
				}
				if ((t.getCode() == KeyCode.LEFT) && (t.isShiftDown())) {
					if (converter == null) {
						throw new IllegalStateException("Attempting to convert text input into Object, but provided "
								+ "StringConverter is null. Be sure to set a StringConverter "
								+ "in your cell factory.");
					}
					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();
					cell.getTableView().getFocusModel().focusLeftCell();
					@SuppressWarnings("unchecked")
					TablePosition<S, T> position = cell.getTableView().getFocusModel().getFocusedCell();
					cell.getTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTableView().edit(position.getRow(), position.getTableColumn());
					// });
				}

				if ((t.getCode() == KeyCode.UP) && (t.isShiftDown())) {
					if (converter == null) {
						throw new IllegalStateException("Attempting to convert text input into Object, but provided "
								+ "StringConverter is null. Be sure to set a StringConverter "
								+ "in your cell factory.");
					}
					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();
					cell.getTableView().getFocusModel().focusAboveCell();
					@SuppressWarnings("unchecked")
					TablePosition<S, T> position = cell.getTableView().getFocusModel().getFocusedCell();
					cell.getTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTableView().edit(position.getRow(), position.getTableColumn());
				}
				if ((t.getCode() == KeyCode.DOWN) && (t.isShiftDown())) {
					if (converter == null) {
						throw new IllegalStateException("Attempting to convert text input into Object, but provided "
								+ "StringConverter is null. Be sure to set a StringConverter "
								+ "in your cell factory.");
					}
					cell.commitEdit(converter.fromString(textic.getText()));
					t.consume();
					@SuppressWarnings("unchecked")
					TablePosition<S, T> position = cell.getTableView().getFocusModel().getFocusedCell();
					cell.getTableView().getFocusModel().focusBelowCell();
					cell.getTableView().getSelectionModel().clearAndSelect(position.getRow(),
							position.getTableColumn());
					cell.getTableView().edit(position.getRow(), position.getTableColumn());
				}

			}
		});

		return textic;
	}
}
