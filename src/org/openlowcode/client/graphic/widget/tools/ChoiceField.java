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

import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.richtext.RichTextArea;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;

/**
 * A widget common to CChoiceField to CMultiChoiceField
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ChoiceField {
	private static Logger logger = Logger.getLogger(ChoiceField.class.getName());
	private boolean compactshow;
	private boolean twolines;
	private String label;
	private String helper;
	private boolean isactive;
	private boolean iseditable;

	private ObservableList<CChoiceFieldValue> values;
	private ChoiceBox<CChoiceFieldValue> choicebox;
	private GridPane thisboxarraypane;
	private ArrayList<CheckBox> checkboxpanel;
	private boolean singlevalueselection;
	private CChoiceFieldValue[] currentvalue;
	private ArrayList<String> storedauthorizedvalues;
	private boolean ischoicebox;
	private PageActionManager actionmanager;

	/**
	 * creates a Choice Field
	 * 
	 * @param compactshow            if true, choice field is shown as compact as
	 *                               possible, if false, displayed to be aligned to
	 *                               other business fields of a data object
	 * @param twolines               if true, show in two lines (for display in a
	 *                               menu)
	 * @param label                  label of the choice field
	 * @param helper                 multi-line helper explaining the business usage
	 *                               of the choice field
	 * @param isactive               true if active
	 * @param iseditable             true if editable
	 * @param singlevalueselection   if true, only single value selection is allowed
	 * @param values                 possible values
	 * @param currentvalue           current value(s) of the choice field (one value
	 *                               if single selection, several values if multiple
	 *                               selection)
	 * @param storedauthorizedvalues the list of non null fields with authorized
	 *                               selections. If values has null, the null value
	 *                               will always show.
	 */
	public ChoiceField(
			PageActionManager actionmanager,
			boolean compactshow,
			boolean twolines,
			String label,
			String helper,
			boolean isactive,
			boolean iseditable,
			boolean singlevalueselection,
			ObservableList<CChoiceFieldValue> values,
			CChoiceFieldValue[] currentvalue,
			ArrayList<String> storedauthorizedvalues) {
		this.actionmanager = actionmanager;
		this.compactshow = compactshow;
		this.twolines = twolines;
		this.label = label;
		this.helper = helper;
		this.isactive = isactive;
		this.iseditable = iseditable;
		this.values = values;
		this.singlevalueselection = singlevalueselection;
		this.currentvalue = currentvalue;
		if (this.singlevalueselection)
			if (currentvalue != null)
				if (currentvalue.length > 1)
					throw new RuntimeException("Several preselected values but single selection");
		this.storedauthorizedvalues = storedauthorizedvalues;
	}

	/**
	 * @return get hte javafx node for the choice field
	 */
	public Node getNode() {

		ObservableList<CChoiceFieldValue> valuesforhelper = values;
		Pane thispane;
		if (this.compactshow) {
			Pane thisboxpane;
			if (this.twolines) {
				thisboxpane = new VBox();

			} else {
				thisboxpane = new HBox();

			}
			thisboxpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			thisboxpane.setPadding(new Insets(0, 0, 0, 0));
			thispane = thisboxpane;
		} else {

			FlowPane thisflowpane = new FlowPane();
			thisflowpane.setRowValignment(VPos.TOP);
			thispane = thisflowpane;
		}

		Label thislabel = new Label(label);
		if (!this.compactshow) {
			thislabel.setMinWidth(120);
			thislabel.setMaxWidth(120);
			thislabel.setWrapText(true);
		} else {
			thislabel.setPadding(new Insets(4, 3, 0, 3));
			thislabel.setMinSize(Label.USE_PREF_SIZE, Label.USE_PREF_SIZE);
		}
		thislabel.setFont(Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
		thislabel.setWrapText(true);
		thispane.getChildren().add(thislabel);
		boolean readonly = false;
		if (!this.isactive)
			readonly = true;
		if (!this.iseditable)
			readonly = true;

		if (readonly) {
			StringBuffer text = new StringBuffer();
			if (this.currentvalue != null)
				for (int i = 0; i < currentvalue.length; i++) {
					if (i > 0)
						text.append(", ");
					text.append(currentvalue[i].getDisplayvalue());
				}

			logger.fine("CChoiceField " + label + ": generating readonlytextarea");
			thispane.getChildren().add(RichTextArea.getReadOnlyTextArea(actionmanager, text.toString(), 400).getNode());
		}

		if (!readonly) {
			// -------------------------------------- Single Value Selection
			// -----------------------------------
			if (this.singlevalueselection) {
				ischoicebox = true;
				if (this.storedauthorizedvalues != null) {
					ObservableList<CChoiceFieldValue> restrictedvalues = FXCollections.observableArrayList();
					for (int i = 0; i < storedauthorizedvalues.size(); i++) {
						String thisauthorizedvalue = storedauthorizedvalues.get(i);
						restrictedvalues.add(findChoiceFromStoredValue(thisauthorizedvalue));
					}
					this.choicebox = new ChoiceBox<CChoiceFieldValue>(restrictedvalues);
					valuesforhelper = restrictedvalues;
				} else {
					this.choicebox = new ChoiceBox<CChoiceFieldValue>(values);
				}
				if (currentvalue != null)
					if (currentvalue.length == 1) {
						this.choicebox.setValue(currentvalue[0]);
					}
			} else
			// --------------------------------------- Multi Value Selection
			// -----------------------------------
			{
				ischoicebox = false;
				thisboxarraypane = new GridPane();
				thisboxarraypane.setHgap(5);
				thisboxarraypane.setVgap(2);

				checkboxpanel = new ArrayList<CheckBox>();
				boolean seton3columns = false;
				if (values.size() > 2)
					seton3columns = true;

				for (int i = 0; i < values.size(); i++) {
					CChoiceFieldValue thisvalue = values.get(i);
					if (thisvalue != null) {
						CheckBox thischeckbox = new CheckBox(thisvalue.getDisplayvalue());
						if (currentvalue != null)
							for (int j = 0; j < currentvalue.length; j++)
								if (currentvalue[j].equals(thisvalue))
									thischeckbox.setSelected(true);
						checkboxpanel.add(thischeckbox);

						if (!seton3columns) {
							thisboxarraypane.add(thischeckbox, 0, i);
						} else {
							thisboxarraypane.add(thischeckbox, i % 3, i / 3);
						}
					}

				}
			}

			String helperstring = "";

			for (int i = 0; i < valuesforhelper.size(); i++) {
				CChoiceFieldValue value = valuesforhelper.get(i);
				if (value != null) {
					helperstring += value.getDisplayvalue() + " [" + value.getStorageCode() + "]";
					if (value.getValuehelper().length() > 0)
						helperstring += ": " + value.getValuehelper();
					helperstring += "\n";
				}
			}

			if (helper.length() > 0)
				helperstring = helper + "\n\n" + helperstring;
			if (this.choicebox != null) {
				this.choicebox.setTooltip(new Tooltip(helperstring));
				this.choicebox.setStyle(" -fx-opacity: 1; 	-fx-base: #ffffff;   ");
				if (!this.isactive)
					this.choicebox.setDisable(true);

				if (!this.iseditable)
					this.choicebox.setDisable(true);
				logger.fine("CChoiceField " + label + ": " + this.choicebox.getWidth());
				thispane.getChildren().add(this.choicebox);
				if (this.compactshow) {
					this.choicebox.autosize();

				}

			} else {
				Tooltip tooltip = new Tooltip(helperstring);
				for (int i = 0; i < this.checkboxpanel.size(); i++) {
					this.checkboxpanel.get(i).setTooltip(tooltip);
				}
				thispane.getChildren().add(thisboxarraypane);
			}

		}
		return thispane;
	}

	/**
	 * @return true if widget is a choicebox, false if widget is an array of
	 *         checkboxes
	 */
	public boolean isChoiceBox() {
		return this.ischoicebox;
	}

	/**
	 * Should only be called after getNode
	 * 
	 * @return all the checkboxes in the case of multi-selection
	 */
	public ArrayList<CheckBox> getCheckBoxList() {
		return this.checkboxpanel;
	}

	/**
	 * Should only be called after getNode
	 * 
	 * @return the choicebox in case of single selection
	 */
	public ChoiceBox<CChoiceFieldValue> getChoiceBox() {
		return this.choicebox;
	}

	private CChoiceFieldValue findChoiceFromStoredValue(String storedvalue) {
		if (storedvalue == null)
			return null;
		if (storedvalue.length() == 0)
			return null;
		for (int i = 0; i < this.values.size(); i++) {
			CChoiceFieldValue thisvalue = this.values.get(i);
			if (thisvalue.getStorageCode().compareTo(storedvalue) == 0)
				return thisvalue;
		}
		throw new RuntimeException(
				"display code not found for list of value, code = " + storedvalue + ", field name = " + this.label);
	}

	/**
	 * @return the list of selected values
	 */
	public ArrayList<CChoiceFieldValue> getSelectedValues() {
		ArrayList<CChoiceFieldValue> selectedvalues = new ArrayList<CChoiceFieldValue>();
		for (int i = 0; i < checkboxpanel.size(); i++) {
			if (checkboxpanel.get(i).isSelected()) {
				selectedvalues.add(values.get(i));
			}
		}
		return selectedvalues;
	}

}
