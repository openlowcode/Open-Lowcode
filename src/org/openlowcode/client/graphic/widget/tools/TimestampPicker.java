/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.tools;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.DatePicker;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * An extended version of the date picker allowing to also edit hours and
 * minutes
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimestampPicker
		extends
		DatePicker {
	public static final String DEV_TIMESTAMP = "yyyy-MM-dd HH:mm";

	private DateTimeFormatter formatter;
	private ObjectProperty<LocalDateTime> datetimevalue = new SimpleObjectProperty<>(null);
	private ObjectProperty<String> format = new SimpleObjectProperty<String>() {
		public void set(String newValue) {
			super.set(newValue);
			formatter = DateTimeFormatter.ofPattern(newValue);
		}
	};

	/**
	 * creates a Timestamp picker
	 */
	public TimestampPicker() {
		getStyleClass().add("datetime-picker");
		setFormat(DEV_TIMESTAMP);
		setConverter(new DateTimeConverter());

		// Synchronizes changes to the underlying date value back to the dateTimeValue
		valueProperty().addListener((observable, oldValue, newValue) -> {
			if (newValue == null) {
				datetimevalue.set(null);
			} else {
				if (datetimevalue.get() == null) {
					datetimevalue.set(LocalDateTime.of(newValue, LocalTime.now()));
				} else {
					LocalTime time = datetimevalue.get().toLocalTime();
					datetimevalue.set(LocalDateTime.of(newValue, time));
				}
			}
		});

		// Synchronizes changes to dateTimeValue back to the underlying date value
		datetimevalue.addListener((observable, oldValue, newValue) -> {
			setValue(newValue == null ? null : newValue.toLocalDate());
		});

		// Persists changes
		getEditor().focusedProperty().addListener((observable, oldValue, newValue) -> {
			if (!newValue)
				simulateEnterPressed();
		});

	}

	private void simulateEnterPressed() {
		getEditor().fireEvent(new KeyEvent(getEditor(), getEditor(), KeyEvent.KEY_PRESSED, null, null, KeyCode.ENTER,
				false, false, false, false));
	}

	/**
	 * @return the date time value entered
	 */
	public LocalDateTime getDateTimeValue() {
		return datetimevalue.get();
	}

	/**
	 * @param dateTimeValue sets the payload to the given date time value
	 */
	public void setDateTimeValue(LocalDateTime datetimevalue) {
		this.datetimevalue.set(datetimevalue);
		this.getEditor().setText(datetimevalue.format(DateTimeFormatter.ofPattern(DEV_TIMESTAMP)));

	}

	/**
	 * @return the ObjectProperty containing the payload
	 */
	public ObjectProperty<LocalDateTime> dateTimeValueProperty() {
		return datetimevalue;
	}

	/**
	 * @return the format of the TimePicker
	 */
	public String getFormat() {
		return format.get();
	}

	/**
	 * @return the format of the TimePicker as an ObjctProperty
	 */
	public ObjectProperty<String> formatProperty() {
		return format;
	}

	/**
	 * sets the format
	 * 
	 * @param format format of the date time picker
	 */
	public void setFormat(String format) {
		this.format.set(format);
	}

	/**
	 * Date time converter
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	class DateTimeConverter
			extends
			StringConverter<LocalDate> {
		@Override
		public String toString(LocalDate object) {
			LocalDateTime value = getDateTimeValue();
			return (value != null) ? value.format(formatter) : "";
		}

		@Override
		public LocalDate fromString(String value) {
			if (value == null) {
				datetimevalue.set(null);
				return null;
			}
			
			if (value.trim().length()==0) {
				datetimevalue.set(null);
				return null;
			}

			datetimevalue.set(LocalDateTime.parse(value, formatter));
			return datetimevalue.get().toLocalDate();
		}
	}
}
