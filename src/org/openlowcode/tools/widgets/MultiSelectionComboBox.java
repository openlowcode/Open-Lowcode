/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.widgets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.tools.misc.Named;

import com.sun.javafx.scene.control.skin.ComboBoxListViewSkin;

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.Label;

/**
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> object managed in the multi-selection combo box.
 * @since 1.6
 */
public class MultiSelectionComboBox<E extends Object> {
	private static Logger logger = Logger.getLogger(MultiSelectionComboBox.class.getName());
	private HBox fullcontent;
	private HBox multiselection;
	private boolean systematicquery;
	private boolean localstored;
	private E[] presetobjects;
	private Function<String, E[]> interactiveobjectselection;
	private boolean allowsmultiselection;
	private Function<E, String> labelgeneration;
	private int lastsize = 0;
	private ComboBox<ObjectWithLabel<E>> comboBox;
	private RunWithSelection<E> runwithselection;
	private ArrayList<E> multiselectedobjects;
	private ObservableList<ObjectWithLabel<E>> fullchoices;
	private float minimumwidgetwidth = 0;
	private String initialeditorvalue;

	public E getSingleSelection() {
		if (allowsmultiselection)
			throw new RuntimeException("Cannot call Single Selection as combobox allows multi-selection");
		ObjectWithLabel<E> selecteditem = comboBox.getSelectionModel().getSelectedItem();
		if (selecteditem == null)
			return null;
		return selecteditem.getPayload();
	}


	/**
	 * @param allowsmultiselection allows selection of multiple value
	 * @param presetobjects        a list of preset objects amongst which the values
	 *                             will be selected
	 * @param initializeditorvalue initialize the editor with the given value
	 */
	public MultiSelectionComboBox(boolean allowsmultiselection, E[] presetobjects,String initialeditorvalue) {
		this.allowsmultiselection = allowsmultiselection;
		this.presetobjects = presetobjects;
		this.systematicquery = true;
		this.localstored = true;
		this.initialeditorvalue=initialeditorvalue;
	}
	
	/**
	 * @param allowsmultiselection allows selection of multiple value
	 * @param presetobjects        a list of preset objects amongst which the values
	 *                             will be selected
	 */
	public MultiSelectionComboBox(boolean allowsmultiselection, E[] presetobjects) {
		this.allowsmultiselection = allowsmultiselection;
		this.presetobjects = presetobjects;
		this.systematicquery = true;
		this.localstored = true;
	}

	/**
	 * @param allowsmultiselection allows selection of multiple value
	 * @param presetobjects        a list of preset objects amongst which the values
	 *                             will be selected
	 * @param labelgeneration      A function to generate the string label of an
	 *                             object if the toString function should not be
	 *                             used
	 */
	public MultiSelectionComboBox(
			boolean allowsmultiselection,
			E[] presetobjects,
			Function<E, String> labelgeneration) {
		this.allowsmultiselection = allowsmultiselection;
		if (this.presetobjects == null)
			throw new RuntimeException("preset objects cannot be null");
		this.presetobjects = presetobjects;
		this.systematicquery = true;
		this.localstored = true;
		this.labelgeneration = labelgeneration;
		this.lastsize = presetobjects.length;
	}

	/**
	 * @param allowsmultiselection       allows selection of multiple value
	 * @param interactiveobjectselection a function called every-time necessary to
	 *                                   query the relevant objects. Typically, this
	 *                                   function may query a remote resource with a
	 *                                   significant performance resource
	 * @param systematicquery            if true, everytime the text entered
	 *                                   changes, the function to get objecs is
	 *                                   called
	 */
	public MultiSelectionComboBox(
			boolean allowsmultiselection,
			Function<String, E[]> interactiveobjectselection,
			boolean systematicquery) {
		this.allowsmultiselection = allowsmultiselection;
		this.localstored = false;
		this.interactiveobjectselection = interactiveobjectselection;
		this.systematicquery = systematicquery;
	}

	/**
	 * @param allowsmultiselection       allows selection of multiple value
	 * @param interactiveobjectselection a function called every-time necessary to
	 *                                   query the relevant objects. Typically, this
	 *                                   function may query a remote resource with a
	 *                                   significant performance resource
	 * @param systematicquery            if true, everytime the text entered
	 *                                   changes, the function to get objecs is
	 *                                   called
	 * @param labelgeneration            A function to generate the string label of
	 *                                   an object if the toString function should
	 *                                   not be used
	 */
	public MultiSelectionComboBox(
			boolean allowsmultiselection,
			Function<String, E[]> interactiveobjectselection,
			boolean systematicquery,
			Function<E, String> labelgeneration) {
		this.allowsmultiselection = allowsmultiselection;
		this.localstored = false;
		this.interactiveobjectselection = interactiveobjectselection;
		this.systematicquery = systematicquery;
		this.labelgeneration = labelgeneration;
	}

	/**
	 * adds code to run after a value has been selected. This is triggered when the
	 * user selects a value actively in the drop-down list.
	 * 
	 * When the user types a text, and only an item is remaining, the selection hook
	 * is NOT run, as it is not clear that the typing is finished. However, the
	 * value is selected, and can be accessed from the getSingleSectionMethod.
	 * 
	 * @param runwithselection the logic to run at item selection
	 */
	public void setOnSelection(RunWithSelection<E> runwithselection) {
		this.runwithselection = runwithselection;
	}

	/**
	 * @return the typed value in the entry field. This is only relevant in certain
	 *         usages, such as the field being used to enter a String text
	 */
	public String getTypedValue() {
		return comboBox.getEditor().getText();
	}

	/**
	 * @return the selected value. A value is selected if it has been typed up to be
	 *         the only selection available or if it was selected through the combos
	 *         directly.
	 */
	public E getUniqueSelectedValue() {
		if (this.allowsmultiselection)
			throw new RuntimeException("Method only available in single selection mode");
		ObjectWithLabel<E> selecteditem = comboBox.getValue();
		if (selecteditem == null)
			return null;
		return selecteditem.getPayload();
	}

	/**
	 * @return the list of all selected items stored on the widget (they appear as a
	 *         list of labels on the right)
	 */
	@SuppressWarnings("unchecked")
	public ArrayList<E> getMultipleSelectedValue() {
		if (!this.allowsmultiselection)
			throw new RuntimeException("Method only available in multipel selection mode");
		return (ArrayList<E>) multiselectedobjects.clone();
	}

	/**
	 * @return a javafx node for the given input. This function should only be
	 *         called once
	 */
	public Node getNode() {
		if (fullcontent == null) {
			fullcontent = new HBox();

			comboBox = new ComboBox<ObjectWithLabel<E>>();
			if (this.minimumwidgetwidth > 0)
				comboBox.getEditor().setMinWidth(this.minimumwidgetwidth);
			fullcontent.getChildren().add(comboBox);
			if (allowsmultiselection) {
				multiselection = new HBox(6);
				multiselection.setPadding(new Insets(4, 0, 0, 10));
				fullcontent.getChildren().add(multiselection);
				// dirty trick to add space

				multiselectedobjects = new ArrayList<E>();

			}
			if (localstored) {
				ArrayList<ObjectWithLabel<E>> originalselection = new ArrayList<ObjectWithLabel<E>>();
				for (int i = 0; i < this.presetobjects.length; i++) {
					originalselection.add(new ObjectWithLabel<E>(this.presetobjects[i], labelgeneration));
				}
				fullchoices = FXCollections.observableArrayList(originalselection);
				comboBox.setItems(fullchoices);
				comboBox.setEditable(true);
			}
			if (this.initialeditorvalue!=null) comboBox.getEditor().setText(initialeditorvalue);
			
			ComboBoxListViewSkin<
					ObjectWithLabel<E>> comboBoxListViewSkin = new ComboBoxListViewSkin<ObjectWithLabel<E>>(comboBox);
			comboBoxListViewSkin.getPopupContent().addEventFilter(KeyEvent.ANY, (event) -> {
				if (event.getCode() == KeyCode.SPACE) {
					event.consume();
				}
			});
			comboBox.setSkin(comboBoxListViewSkin);
			
			comboBox.setOnAction(new EventHandler<ActionEvent>() {

				@Override
				public void handle(ActionEvent event) {
					// determine selected object payload
					Object objecttoget = comboBox.getValue();
					if (objecttoget instanceof String) {
						logger.finest("Event discarded as got String");
						return;
					}
					ObjectWithLabel<E> selectedobjectwrapper = comboBox.getValue();
					E selectedobject = null;
					if (selectedobjectwrapper != null)
						selectedobject = selectedobjectwrapper.getPayload();
					// run the selection hook if appropriate
					if (runwithselection != null)
						runwithselection.run(selectedobject);
					// store the object if multi-selection
					if (selectedobject != null)
						if (allowsmultiselection) {
							// check if selection already exists
							boolean objectexists = multiselectedobjects.contains(selectedobject);
							// if not, adds it
							if (!objectexists) {
								Label newlabel = new Label(selectedobjectwrapper.toString());
								multiselection.getChildren().add(newlabel);
								multiselectedobjects.add(selectedobject);
								final E objectforlabel = selectedobject;
								newlabel.setOnMouseClicked(new EventHandler<MouseEvent>() {

									@Override
									public void handle(MouseEvent arg0) {
										multiselection.getChildren().remove(newlabel);
										multiselectedobjects.remove(objectforlabel);
									}

								});
							}
							logger.finest(
									" Cleaning Editor after edition, current text = " + comboBox.getEditor().getText());
							Platform.runLater(new Runnable() {

								@Override
								public void run() {
									comboBox.selectionModelProperty().get().clearSelection();
									comboBox.setItems(fullchoices);
									comboBox.getEditor().clear();
									comboBox.getEditor().requestLayout();
								}

							});

						}

				}

			});

			comboBox.getEditor().setOnKeyReleased(new EventHandler<KeyEvent>() {

				@Override
				public void handle(KeyEvent keyevent) {
					boolean treat = false;
					if (systematicquery)
						treat = true;
					if (!systematicquery) {
						if (keyevent.getCode() == KeyCode.TAB)
							treat = true;
						if (keyevent.getCode() == KeyCode.CONTROL)
							treat = true;
					}
					if (treat) {
						Platform.runLater(new Runnable() {
							@Override
							public void run() {
								String selectionstring = comboBox.getEditor().getText();
								int caretposition = comboBox.getEditor().getCaretPosition();
								logger.severe("---------------- Starting selection " + selectionstring
										+ " -------------------------");
								List<E> selection = null;
								if (localstored) {
									selection = filterObjects(selectionstring);
									logger.severe("   > Selection " + selection.size());
								} else {
									selection = Arrays.asList(interactiveobjectselection.apply(selectionstring));
								}
								ArrayList<ObjectWithLabel<E>> selectionresult = new ArrayList<ObjectWithLabel<E>>();
								for (int i = 0; i < selection.size(); i++) {
									selectionresult.add(new ObjectWithLabel<E>(selection.get(i), labelgeneration));
								}
								ObservableList<ObjectWithLabel<E>> choices = FXCollections
										.observableArrayList(selectionresult);
								comboBox.setItems(choices);
								// somehow, when setting items, in some situations, editor content is removed
								comboBox.getEditor().setText(selectionstring);
								comboBox.getEditor().positionCaret(caretposition);

								logger.severe("    > Reset values");
								boolean shownfirst = false;
								if (!comboBox.isShowing()) {
									logger.severe("    > combobox first show");
									comboBox.show();
									shownfirst = true;
								}
								if (choices.size() != lastsize)
									if (!shownfirst) {
										logger.severe("    > combobox hide and show");
										comboBox.hide();
										comboBox.show();
										lastsize = choices.size();
									}

								if (!allowsmultiselection) {
									if (choices.size() != 1) {
										logger.severe("   >>> Clear multi-selection.");
										// if not one choice available, unselect any item selected
										comboBox.getSelectionModel().clearSelection();
									}
								}
							}

						});
					}

				}

			});

		}
		return fullcontent;
	}

	private List<E> filterObjects(String selectionstring) {
		String filteredselection = Named.cleanName(selectionstring);
		ArrayList<E> answers = new ArrayList<E>();
		for (int i = 0; i < this.presetobjects.length; i++) {
			E currentobject = this.presetobjects[i];
			String thislabel = "";
			if (this.labelgeneration == null)
				thislabel = Named.cleanName(currentobject.toString());
			if (this.labelgeneration != null)
				thislabel = Named.cleanName(this.labelgeneration.apply(currentobject));
			if (thislabel.indexOf(filteredselection) >= 0) {
				answers.add(currentobject);
			}
		}
		return answers;
	}

	/**
	 * A wrapper class around an object with potentially an ad-hoc method to
	 * generate a label
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	private static class ObjectWithLabel<E> {
		private E object;
		private Function<E, String> labelgenerator;

		/**
		 * @param object
		 * @param labelgenerator
		 */
		public ObjectWithLabel(E object, Function<E, String> labelgenerator) {
			this.object = object;
			this.labelgenerator = labelgenerator;
		}

		@Override
		public String toString() {
			if (object == null)
				return null;
			if (labelgenerator == null)
				return object.toString();
			return labelgenerator.apply(object);
		}

		/**
		 * @return the object payload of this wrapper object
		 */
		public E getPayload() {
			return object;
		}

	}

	/**
	 * A simple hook to be executed after value is selected. This should be used
	 * mostly in single selection
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> the object managed in the widget
	 * @since 1.6
	 */
	public interface RunWithSelection<E extends Object> {
		/**
		 * @param selectedobject
		 */
		public abstract void run(E selectedobject);
	}

	public void setMinimumWidgetWidth(float width) {
		this.minimumwidgetwidth = width;

	}
}
