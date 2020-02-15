/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageBooleanField;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageIntegerField;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.MessageStringField;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine;
import org.openlowcode.client.graphic.widget.table.ObjectTableRow;
import org.openlowcode.client.graphic.widget.tools.CChoiceFieldValue;
import org.openlowcode.client.graphic.widget.tools.ChoiceField;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.tools.structure.ChoiceDataEltType;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.MultipleChoiceDataElt;
import org.openlowcode.tools.structure.MultipleChoiceDataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;
import org.openlowcode.tools.trace.ExceptionLogger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.scene.control.TableCell;

// put simpleDataElt to manage both cases with multiplechoicedataelt and choicedataelt
// it should probably be updated to have multiplechoicedataelt as a base class and
// choicedataelt as subclass. See Github issue #27
/**
 * a widget displaying a choice field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CChoiceField
		extends
		CBusinessField<SimpleDataElt> {
	private static Logger logger = Logger.getLogger(CChoiceField.class.getName());
	private String label;
	private String datafieldname;
	private String helper;
	private String defaultvaluecode;
	private String[] multipledefaultvaluecode;
	@SuppressWarnings("unused")
	private boolean businessparameter = false;
	private CPageDataRef datareference;
	ObservableList<CChoiceFieldValue> values;
	private HashMap<String, CChoiceFieldValue> valuesbycode;

	/**
	 * transforms a storage code into a CChoiceFieldValue object
	 * 
	 * @param code storage code of the value
	 * @return the CChoiceFieldValue object
	 */
	public CChoiceFieldValue getChoiceFieldValue(String code) {
		return valuesbycode.get(code);
	}

	private boolean iseditable;
	@SuppressWarnings("unused")
	private CPageAction action;
	@SuppressWarnings("unused")
	private CPageInlineAction inlineaction;
	private int maxcharlength;
	private int prefereddisplaysizeintable;
	private ChoiceBox<CChoiceFieldValue> choicebox;

	private boolean compactshow;
	private CChoiceFieldValue blankchoicefield;
	private boolean twolines;

	/**
	 * creates a new Choice Field widget from a message coming from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CChoiceField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		label = reader.returnNextStringField("LBL");
		compactshow = reader.returnNextBooleanField("CPS");
		this.twolines = reader.returnNextBooleanField("TWL");

		datafieldname = reader.returnNextStringField("DFN");
		helper = reader.returnNextStringField("HPR");
		MessageElement element = reader.getNextElement();

		if (element instanceof MessageStringField) {
			MessageStringField stringfieldelement = (MessageStringField) element;

			if (stringfieldelement.getFieldName().compareTo("DFC") != 0)
				throw new RuntimeException("expected a string 'DFC' tag, got " + element.toString() + " at path "
						+ reader.getCurrentElementPath());
			defaultvaluecode = stringfieldelement.getFieldcontent();
			// read the next element
			element = reader.getNextElement();
		}
		if (element instanceof MessageBooleanField) {
			MessageBooleanField booleanelement = (MessageBooleanField) element;
			if (booleanelement.getFieldName().compareTo("BSP") != 0)
				throw new RuntimeException("expected a boolean 'BSP' tag, got " + element.toString() + " at path "
						+ reader.getCurrentElementPath());
			businessparameter = booleanelement.getFieldContent();
		} else {
			throw new RuntimeException("expected a boolean 'BSP' tag, got " + element.toString() + " at path "
					+ reader.getCurrentElementPath());
		}
		// business reference
		boolean externalreference = reader.returnNextBooleanField("EXR");
		if (externalreference) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
			if (!this.datareference.getType().equals(new ChoiceDataEltType()))
				throw new RuntimeException(String.format(
						"Invalid external data reference named %s, excepted ChoiceDataEltType, got %s in CPage ",
						datareference.getName(), datareference.getType()));
		}
		// list of values
		values = FXCollections.observableArrayList();
		valuesbycode = new HashMap<String, CChoiceFieldValue>();
		reader.startStructureArray("CCL");
		this.maxcharlength = label.length();
		int sequence = 0;
		while (reader.structureArrayHasNextElement("CCL")) {
			CChoiceFieldValue thischoicevalue = new CChoiceFieldValue(reader.returnNextStringField("STV"),
					reader.returnNextStringField("DSV"), reader.returnNextStringField("HLP"), sequence);
			if (thischoicevalue.getDisplayvalue().length() > maxcharlength)
				maxcharlength = thischoicevalue.getDisplayvalue().length();
			values.add(thischoicevalue);
			valuesbycode.put(thischoicevalue.getStorageCode(), thischoicevalue);
			reader.returnNextEndStructure("CCL");
			sequence++;
		}

		this.iseditable = !(reader.returnNextBooleanField("ROY"));
		element = reader.getNextElement();
		if (element instanceof MessageStartStructure) { // CASE THERE IS ACTION
			MessageStartStructure actiontag = (MessageStartStructure) element;

			boolean treated = false;

			if (actiontag.getStructurename().compareTo("ACTION") == 0) {
				action = new CPageAction(reader);
				element = reader.getNextElement();
				treated = true;
			}
			if (actiontag.getStructurename().compareTo("INLINEACTION") == 0) {
				inlineaction = new CPageInlineAction(reader);
				element = reader.getNextElement();
				treated = true;
			}
			if (!treated)
				throw new RuntimeException(
						" was expecting either ACTION or INLINEACTION structure, got " + actiontag.getStructurename());

		}
		if (element instanceof MessageBooleanField) {
			MessageBooleanField booleanelement = (MessageBooleanField) element;
			if (booleanelement.getFieldName().compareTo("SIT") != 0)
				throw new RuntimeException("expecting boolean field 'SIT' , got " + element + " at path "
						+ reader.getCurrentElementPath());
			this.showintitle = booleanelement.getFieldContent();
			logger.fine("for field " + this.label + " show in title = " + this.showintitle);
		} else
			throw new RuntimeException(
					"expecting boolean field 'SIT' , got " + element + " at path " + reader.getCurrentElementPath());
		this.prefereddisplaysizeintable = -1;
		MessageElement nextelement = reader.getNextElement();
		if (nextelement instanceof MessageIntegerField) {
			MessageIntegerField prefereddisplaytable = (MessageIntegerField) nextelement;
			if (prefereddisplaytable.getFieldName().compareTo("PDT") == 0) {
				this.prefereddisplaysizeintable = prefereddisplaytable.getFieldContent();
			} else {
				throw new RuntimeException("expected an integer 'SIT' tag, got " + nextelement.toString() + " at path "
						+ reader.getCurrentElementPath());
			}
			nextelement = reader.getNextElement();
		}
		if (nextelement instanceof MessageBooleanField) {
			MessageBooleanField booleantag = (MessageBooleanField) nextelement;
			if (booleantag.getFieldName().compareTo("SBN") == 0) {
				this.showinbottomnotes = booleantag.getFieldContent();
			} else {
				throw new RuntimeException("expected a boolean 'SBN' tag, got " + nextelement.toString() + " at path "
						+ reader.getCurrentElementPath());
			}
		}

		reader.returnNextEndStructure("CTF");
		this.blankchoicefield = new CChoiceFieldValue();

	}

	private ChoiceDataElt<CChoiceFieldValue> getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		if (!(thiselement instanceof ChoiceDataElt))
			throw new RuntimeException("Expected a ChoiceDataElt element, got " + thiselement);
		@SuppressWarnings("unchecked")
		ChoiceDataElt<CChoiceFieldValue> thischoiceelement = (ChoiceDataElt<CChoiceFieldValue>) thiselement;
		return thischoiceelement;
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		CChoiceFieldValue currentchoice = null;
		ArrayList<String> restrictedvalues = null;
		if (this.datareference != null) {

			currentchoice = findChoiceFromStoredValue(getExternalContent(inputdata, datareference).getStoredValue());
			if (currentchoice != null) {
				String[] restrictedvaluesarray = getExternalContent(inputdata, datareference).getAuthorizedValues();
				if (restrictedvaluesarray != null) {
					restrictedvalues = new ArrayList<String>();
					for (int i = 0; i < restrictedvaluesarray.length; i++)
						restrictedvalues.add(restrictedvaluesarray[i]);
				}

			}
		} else {
			currentchoice = findChoiceFromStoredValue(defaultvaluecode);

		}
		logger.finest("--- Printing Choice field with label = " + label + ", current choice = " + currentchoice
				+ ",data referenece is " + (datareference == null ? "NULL" : "NOTNULL")
				+ ", restricted values length = " + (restrictedvalues != null ? restrictedvalues.size() : "NONE"));
		values.add(null);

		ChoiceField choicefield = new ChoiceField(actionmanager, compactshow, twolines, label, helper, isactive,
				iseditable, true, values,
				(currentchoice != null ? new CChoiceFieldValue[] { currentchoice } : new CChoiceFieldValue[0]),
				restrictedvalues);

		Node node = choicefield.getNode();

		if (choicefield.isChoiceBox()) {
			this.choicebox = choicefield.getChoiceBox();
			this.choicebox.valueProperty().addListener(new CChoiceFieldChangeListener(this));
		}

		return node;

	}

	@Override
	public Node getDisplayContent() {
		return this.choicebox;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (objectfieldname != null)
			throw new RuntimeException("indicated objectfieldname = '" + objectfieldname
					+ "', but the field is not supporting this parameter");

		if (!(type instanceof ChoiceDataEltType))
			throw new RuntimeException(String.format(
					"Only ChoiceDataEltType can be extracted from CChoiceField in single selection mode, but request was %s ",
					type));
		return new ChoiceDataElt<CChoiceFieldValue>(eltname, this.choicebox.getValue());

	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public boolean isEditable() {
		return this.iseditable;
	}

	@Override
	public String getLabel() {
		return this.label;

	}

	@Override
	public String getFieldname() {
		return this.datafieldname;
	}

	@Override
	public String getHelper() {
		return this.helper;
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
		throw new RuntimeException("display code not found for list of value, code = " + storedvalue + ", field name = "
				+ this.datafieldname);
	}

	@Override
	public void setContent(ObjectDataElt objectdata) {
		SimpleDataElt thiselement = objectdata.lookupEltByName(this.datafieldname);
		if (thiselement == null)
			throw new RuntimeException(String.format("could not find any page data with name = %s", this.label));

		boolean treated = false;

		if ((thiselement.getType() instanceof ChoiceDataEltType)) {
			@SuppressWarnings("unchecked")
			ChoiceDataElt<CChoiceFieldValue> thischoiceelement = (ChoiceDataElt<CChoiceFieldValue>) thiselement;
			String storedvalue = thischoiceelement.getStoredValue();
			logger.fine("logger info for field " + label + ": stored value = " + storedvalue);
			this.defaultvaluecode = storedvalue;
			this.property = thischoiceelement.getPropertyname();
			treated = true;
		}

		if ((thiselement.getType() instanceof MultipleChoiceDataEltType)) {
			@SuppressWarnings("unchecked")
			MultipleChoiceDataElt<
					CChoiceFieldValue> thischoiceelement = (MultipleChoiceDataElt<CChoiceFieldValue>) thiselement;
			this.multipledefaultvaluecode = new String[thischoiceelement.getSelectedChoicesNumber()];
			for (int i = 0; i < thischoiceelement.getSelectedChoicesNumber(); i++)
				this.multipledefaultvaluecode[i] = thischoiceelement.getSelectedChoiceAt(i);
			logger.fine("logger info for field " + label + ": Got multiple default value index = "
					+ thischoiceelement.getSelectedChoicesNumber());
			this.property = thischoiceelement.getPropertyname();
			treated = true;
		}

		// text data element happens when formatting a text element of a property. This
		// may have side
		// effect so should be reworked at some point
		if ((thiselement.getType() instanceof TextDataEltType)) {
			TextDataElt thistextelement = (TextDataElt) thiselement;
			this.defaultvaluecode = thistextelement.getPayload();
			this.property = thistextelement.getPropertyname();
			treated = true;
		}

		if (!treated)
			throw new RuntimeException(String.format(
					"page data with name = %s does not have expected %s  or %s type, actually found %s", this.label,
					ChoiceDataEltType.class, TextDataEltType.class, thiselement.getType().toString()));
	}

	@Override
	public ChoiceDataElt<?> getFieldDataElt() {

		if (this.choicebox == null)
			throw new RuntimeException("" + "Trying to get value from non-displayed choice field " + this.datafieldname
					+ " - path: " + this.getParentpath() + " parent -  "
					+ (this.getParentforfield() != null ? this.getParentforfield().getSignificantpath() : "unset"));
		CChoiceFieldValue value = this.choicebox.getValue();
		if (value == null)
			return new ChoiceDataElt<CChoiceFieldValue>(datafieldname, value);
		return new ChoiceDataElt<CChoiceFieldValue>(datafieldname, value);

	}

	@Override
	public TreeTableColumn<ObjectDataElt, String> getTreeTableColumn(
			PageActionManager pageactionmanager,
			String actionkeyforupdate) {

		TreeTableColumn<ObjectDataElt, String> thiscolumn = new TreeTableColumn<ObjectDataElt, String>(this.getLabel());
		thiscolumn.setEditable(true);
		int length = (this.maxcharlength * 7);
		if (length > 300)
			length = 300;
		if (this.prefereddisplaysizeintable >= 0) {
			length = this.prefereddisplaysizeintable * 7;

		}
		logger.fine(" --**-- length for field" + this.getLabel() + " maxcharlength:" + maxcharlength
				+ " pref display in table " + this.prefereddisplaysizeintable + " final length = " + length);

		thiscolumn.setMinWidth(length);
		thiscolumn.setPrefWidth(length);
		CChoiceField thischoicefield = this;
		thiscolumn.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObjectDataElt, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							javafx.scene.control.TreeTableColumn.CellDataFeatures<ObjectDataElt, String> p) {

						ObjectDataElt line = p.getValue().getValue();
						String fieldname = thischoicefield.getFieldname();
						if (line == null)
							return new SimpleStringProperty("");
						SimpleDataElt lineelement = line.lookupEltByName(fieldname);
						if (lineelement == null) {

							return new SimpleStringProperty("Field Not found !" + fieldname);
						}
						String code = lineelement.defaultTextRepresentation();
						String displaystring = "Invalid code: " + code; // by default code is invalid
						if (code.length() == 0)
							displaystring = ""; // or empty
						CChoiceFieldValue displayvalue = valuesbycode.get(code); // try to get display value
						if (displayvalue != null)
							displaystring = displayvalue.getDisplayvalue();
						return new SimpleStringProperty(displaystring);
					}

				});
		return thiscolumn;
	}

	private static class TableColumnOnEditCommit
			implements
			EventHandler<CellEditEvent<ObjectTableRow, CChoiceFieldValue>> {
		private CChoiceField thischoicefield;

		public TableColumnOnEditCommit(CChoiceField thischoicefield) {
			this.thischoicefield = thischoicefield;
		}

		@Override
		public void handle(CellEditEvent<ObjectTableRow, CChoiceFieldValue> event) {
			try {
				boolean changed = false;
				if (event.getOldValue() == null)
					if (event.getNewValue() != null)
						changed = true;
				if (event.getOldValue() != null)
					if (!(event.getOldValue().equals(event.getNewValue())))
						changed = true;
				if (changed) {
					ObjectTableRow objecttochange = event.getRowValue();
					objecttochange.updateField(thischoicefield.getFieldname(), event.getNewValue());
					logger.info("Updated choice value for field " + thischoicefield.getFieldname() + ", new value = "
							+ event.getNewValue());
				} else {
					logger.info("received edit event where old values and new values are the same");
				}
			} catch (RuntimeException e) {
				logger.severe("exception in updating ChoiceField " + thischoicefield.getFieldname() + " : "
						+ e.getClass().getName() + ": " + e.getMessage());
				for (int i = 0; i < e.getStackTrace().length; i++)
					if (e.getStackTrace()[i].toString().startsWith("gallium"))
						logger.severe("		- " + e.getStackTrace()[i].toString());
			}

		}

	}

	private static class TableCellValueFactory
			implements
			Callback<
					TableColumn.CellDataFeatures<ObjectTableRow, CChoiceFieldValue>,
					ObservableValue<CChoiceFieldValue>> {
		private CChoiceField thischoicefield;

		public TableCellValueFactory(CChoiceField thischoicefield) {
			this.thischoicefield = thischoicefield;
		}

		@Override
		public ObservableValue<CChoiceFieldValue> call(CellDataFeatures<ObjectTableRow, CChoiceFieldValue> p) {
			try {
				ObjectTableRow line = p.getValue();
				String fieldname = thischoicefield.getFieldname();
				SimpleDataElt lineelement = line.getFieldDataEltClone(fieldname);
				if (lineelement == null) {

					return null;
				}

				String code = lineelement.defaultTextRepresentation();

				CChoiceFieldValue displayvalue = thischoicefield.valuesbycode.get(code); // try to get display value
				ArrayList<String> restrictionsonupdate = line.hasFieldRestriction(fieldname);
				if (displayvalue == null)
					displayvalue = thischoicefield.getBlankChoiceField();
				if (displayvalue != null)
					displayvalue.setRestrictionsOnNextValues(restrictionsonupdate);
				if (line.isRowFrozen())
					displayvalue = displayvalue.duplicateAsFrozen();
				return new SimpleObjectProperty<CChoiceFieldValue>(displayvalue);

			} catch (RuntimeException e) {
				ExceptionLogger.setInLogs(e, logger);
				return null;
			}
		}

	}

	private static class CTableCell
			extends
			ChoiceBoxTableCell<ObjectTableRow, CChoiceFieldValue> {
		@SuppressWarnings("unused")
		private String helper;
		private ArrayList<CChoiceFieldValue> referencevalues;

		public CTableCell(String helper, ObservableList<CChoiceFieldValue> values) {
			super(values);
			referencevalues = new ArrayList<CChoiceFieldValue>();
			referencevalues.addAll(values);
			referencevalues.add(null);
			this.helper = helper;
		}

		@Override
		public void updateItem(CChoiceFieldValue choicefield, boolean empty) {
			logger.fine("update Item '" + (choicefield != null ? choicefield.getDisplayvalue() : "") + "' , empty="
					+ empty);
			ObservableList<CChoiceFieldValue> currentlist = this.getItems();
			boolean restrictionset = false;
			boolean currentauthorized = true;
			logger.finer("		----- authorized values --------------------------");
			for (int i = 0; i < currentlist.size(); i++)
				logger.finer("			- " + currentlist.get(i));
			logger.finer("		--------------------------------------------------");
			if (choicefield != null)
				if (choicefield.getRestrictionsOnNextValues() != null)
					if (choicefield.getRestrictionsOnNextValues().size() > 0) {
						ArrayList<String> restrictedvalues = choicefield.getRestrictionsOnNextValues();
						logger.finer("		----- restricted values provided at start --------");
						for (int i = 0; i < restrictedvalues.size(); i++)
							logger.finer("			- " + restrictedvalues.get(i));
						logger.finer("		--------------------------------------------------");

						ArrayList<CChoiceFieldValue> valuesforthis = new ArrayList<CChoiceFieldValue>();
						currentauthorized = false;

						for (int i = 0; i < referencevalues.size(); i++) {
							CChoiceFieldValue thisvalue = referencevalues.get(i);

							for (int j = 0; j < restrictedvalues.size(); j++)
								if (thisvalue != null)
									if (thisvalue.getStorageCode().equals(restrictedvalues.get(j))) {
										valuesforthis.add(thisvalue);
										if (thisvalue.getStorageCode()
												.equals((choicefield != null ? choicefield.getStorageCode() : null)))
											currentauthorized = true;
									}
						}
						valuesforthis.add(null);
						logger.finer("created values for this with nb of item = " + valuesforthis.size()
								+ " from restricted values index = " + restrictedvalues.size());
						logger.finer("update Item - intermediate inside 1");
						if (!currentauthorized)
							currentlist.clear();
						if (currentauthorized) {
							logger.finer("current is authorized, removing all except current selected item");
							for (int i = currentlist.size() - 1; i >= 0; i--) {
								boolean removei = false;
								if (currentlist.get(i) != null)
									if (!currentlist.get(i).getStorageCode().equals(choicefield.getStorageCode())) {
										currentlist.remove(i);
										removei = true;
									}
								if (!removei)
									if (currentlist.get(i) == null)
										currentlist.remove(i);
							}
						}
						logger.finer("update Item - intermediate inside 2");
						for (int i = 0; i < valuesforthis.size(); i++) {
							if (choicefield != null)
								if (valuesforthis.get(i) != null)
									if (!valuesforthis.get(i).getStorageCode().equals(choicefield.getStorageCode()))
										currentlist.add(valuesforthis.get(i));
							if (choicefield != null)
								if (valuesforthis.get(i) == null)
									currentlist.add(null);
						}
						restrictionset = true;
					}

			logger.finer("update Item - intermediate, restrictionset =  " + restrictionset + ", choice field = "
					+ (choicefield == null ? "null" : "not null") + " current list size = " + currentlist.size()
					+ ", reference values size = " + referencevalues.size());
			if (!restrictionset) {
				if (choicefield == null) {
					currentlist.clear();
					currentlist.addAll(referencevalues);
					logger.finer("clearing the list of all elements and adding all choices again");
				} else {
					logger.finer(
							"clearing the list of all elements except current selection and adding all choices again");
					for (int i = currentlist.size() - 1; i >= 0; i--) {
						boolean removei = false;
						if (currentlist.get(i) != null)
							if (!currentlist.get(i).getStorageCode().equals(choicefield.getStorageCode())) {
								currentlist.remove(i);
								removei = true;
							}

						if (!removei)
							if (currentlist.get(i) == null)
								currentlist.remove(i);
					}
					for (int i = 0; i < referencevalues.size(); i++)
						if (referencevalues.get(i) != null)
							if (!referencevalues.get(i).getStorageCode().equals((choicefield.getStorageCode())))
								currentlist.add(referencevalues.get(i));
				}

				// this.setItem(choicefield);
				// this.requestLayout();
			}
			logger.finer("update Item - intermediate 2");
			logger.finer("		----- authorized values at end --------------------------");
			for (int i = 0; i < currentlist.size(); i++)
				logger.finer("			- " + currentlist.get(i));
			logger.finer("		--------------------------------------------------");

			super.updateItem(choicefield, empty);

			super.setWrapText(true);
			super.setTextOverrun(OverrunStyle.ELLIPSIS);
			super.setEllipsisString("...");
			super.setMaxHeight(1 * 15 + 14);
			super.setPrefHeight(15 + 14);
			super.setMinHeight(15 + 14);

			if (choicefield == null || empty) {
				setText("");

			} else {
				setText((choicefield.getDisplayvalue() == null ? "" : choicefield.getDisplayvalue()));
				// if (helper!=null)
				// this.setTooltip(new Tooltip(helper));
			}
			if (choicefield != null)
				if (choicefield.isFrozen()) {
					this.setEditable(false);
				} else {
					this.setEditable(true);
				}
			logger.fine("update Item finished '" + (choicefield != null ? choicefield.getDisplayvalue() : "")
					+ "' , empty=" + empty);
		}
	}

	private static class TableCellFactory
			implements
			Callback<TableColumn<ObjectTableRow, CChoiceFieldValue>, TableCell<ObjectTableRow, CChoiceFieldValue>> {
		private String helper;
		private ObservableList<CChoiceFieldValue> values;

		public TableCellFactory(String helper, ObservableList<CChoiceFieldValue> values) {
			this.helper = helper;
			this.values = values;
		}

		@Override
		public TableCell<ObjectTableRow, CChoiceFieldValue> call(TableColumn<ObjectTableRow, CChoiceFieldValue> param) {
			ObservableList<CChoiceFieldValue> listclone = FXCollections.observableArrayList();
			listclone.addAll(values);
			return new CTableCell(helper, listclone);
		}

	}

	@Override
	public TableColumn<ObjectTableRow, CChoiceFieldValue> getTableColumn(
			PageActionManager pageactionmanager,
			boolean largedisplay,
			int rowheight,
			String actionkeyforupdate) {

		TableColumn<
				ObjectTableRow,
				CChoiceFieldValue> thiscolumn = new TableColumn<ObjectTableRow, CChoiceFieldValue>(this.getLabel());
		if (actionkeyforupdate != null) {
			thiscolumn.setEditable(true);
			thiscolumn.setOnEditCommit(new TableColumnOnEditCommit(this));
		} else {
			thiscolumn.setEditable(false);
		}

		int length = this.maxcharlength * 7;
		if (length > 300)
			length = 300;
		if (this.prefereddisplaysizeintable >= 0) {
			length = this.prefereddisplaysizeintable * 7;

		}

		double pixellength = ((new Text(this.label)).getBoundsInLocal().getWidth() + 10) * 1.05;
		for (int i = 0; i < this.values.size(); i++) {
			double valuelength = ((new Text(values.get(i).getDisplayvalue()).getBoundsInLocal().getWidth()) + 10)
					* 1.05;
			if (valuelength > pixellength)
				pixellength = valuelength;
		}
		int pixellengthi = (int) pixellength;
		thiscolumn.setMinWidth(pixellengthi);
		thiscolumn.setPrefWidth(pixellengthi);
		logger.fine(" --**-- length for field" + this.getLabel() + " maxcharlength:" + maxcharlength
				+ " pref display in table " + this.prefereddisplaysizeintable + " final length = " + length
				+ " - pixel length" + pixellengthi);
		thiscolumn.setCellValueFactory(new TableCellValueFactory(this));
		thiscolumn.setCellFactory(new TableCellFactory(helper, values));
		return thiscolumn;
	}

	/**
	 * @return
	 */
	public CChoiceFieldValue getBlankChoiceField() {
		return this.blankchoicefield;
	}

	@Override
	public void ForceAction(PageActionManager actionmanager, CPageAction action) {
		this.action = action;

	}

	@Override
	public CPageNode deepcopyWithCallback(org.openlowcode.client.graphic.Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int getPreferredTableRowHeight() {
		return 1;
	}

	@Override
	public void mothball() {

	}

	@Override
	public boolean isRestrictionValid(String restriction) {
		if (valuesbycode.get(restriction) != null)
			return true;
		return false;
	}

	/**
	 * Change Listener on a choice field. Checks if data is valid according to
	 * provided constraints
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class CChoiceFieldChangeListener
			implements
			ChangeListener<CChoiceFieldValue> {
		private CChoiceField choicefield;

		/**
		 * creates a change listener for the given choice field
		 * 
		 * @param choicefield choice field the listener will be applied on
		 */
		public CChoiceFieldChangeListener(CChoiceField choicefield) {
			this.choicefield = choicefield;
		}

		@Override
		public void changed(
				ObservableValue<? extends CChoiceFieldValue> observable,
				CChoiceFieldValue oldValue,
				CChoiceFieldValue newValue) {
			try {
				logger.info("evaluating " + (newValue != null ? newValue.getStorageCode() : "null") + " for field "
						+ this.choicefield.label);
				StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
				// checking if change as part of multifield constraint. If so, do not trigger
				// the multifield constraints again
				boolean changedaspartoffieldconstraint = false;
				for (int i = 0; i < stacktrace.length; i++)
					if (stacktrace[i].getClassName().equals(CMultiFieldConstraint.class.getName()))
						changedaspartoffieldconstraint = true;
				if (!changedaspartoffieldconstraint)
					if (newValue != null)
						for (int i = 0; i < choicefield.constraintsforcallback.size(); i++) {

							CMultiFieldConstraint thiscallback = choicefield.constraintsforcallback.get(i);
							thiscallback.checkFieldEntry(choicefield.getFieldname(), newValue.getStorageCode());
						}
			} catch (RuntimeException e) {
				try {
					logger.info("got exception in validation of new value " + e.toString());
					for (int i = 0; i < e.getStackTrace().length; i++) {
						logger.info(" at " + e.getStackTrace()[i]);
						if (i > 3)
							break;
					}
					if (oldValue != null)
						for (int i = 0; i < choicefield.constraintsforcallback.size(); i++) {
							CMultiFieldConstraint thiscallback = choicefield.constraintsforcallback.get(i);
							thiscallback.checkFieldEntry(choicefield.getFieldname(), oldValue.getStorageCode());
						}
					choicefield.choicebox.setValue(oldValue);
					logger.info(
							"set back old value " + oldValue.getStorageCode() + " for field " + this.choicefield.label);
				} catch (Throwable e2) {
					logger.info("got exception in validation of old value " + e2.toString());
					for (int i = 0; i < e2.getStackTrace().length; i++) {
						logger.info(" at " + e2.getStackTrace()[i]);
						if (i > 3)
							break;
					}
					logger.info("set back to null as old value does not work" + " for field " + this.choicefield.label);
					choicefield.choicebox.setValue(null);
				}

			}
		}

	}

	@Override
	public String getValueForConstraint() {
		CChoiceFieldValue value = this.choicebox.getValue();
		if (value == null)
			return null;
		return value.getStorageCode();
	}

	public CChoiceFieldValue lookupValueByStorage(String code) {
		for (int i = 0; i < values.size(); i++) {
			if (values.get(i).getStorageCode().equals(code))
				return values.get(i);
		}
		throw new RuntimeException("no value found for storage code " + code);
	}

	@Override
	public boolean setConstraint(ArrayList<String> restrainedvalues, String selected) throws RuntimeException {
		logger.finer("    -*-*- setting field " + this.datafieldname + " value size = "
				+ (restrainedvalues != null ? restrainedvalues.size() : "null") + " - selected = '" + selected + "'");
		if (selected == null)
			if (restrainedvalues != null)
				if (restrainedvalues.size() == 1)
					selected = restrainedvalues.get(0);
		boolean forcetoblank = false;
		if (selected != null)
			if (selected.length() == 0)
				forcetoblank = true;
		ObservableList<CChoiceFieldValue> restrictedvalues = FXCollections.observableArrayList();
		CChoiceFieldValue selectedvaluebeforerestriction = this.choicebox.getSelectionModel().getSelectedItem();
		boolean selectedvalid = false;
		HashMap<String, String> alreadyregistered = new HashMap<String, String>();
		for (int i = 0; i < restrainedvalues.size(); i++) {
			String value = restrainedvalues.get(i);
			if (value != null)
				if (alreadyregistered.get(value) == null) {
					alreadyregistered.put(value, value);
					if (selectedvaluebeforerestriction != null)
						if (value.equals(selectedvaluebeforerestriction.getStorageCode()))
							selectedvalid = true;
					restrictedvalues.add(lookupValueByStorage(value));
				}

		}
		boolean fulllisthasnull = false;
		for (int i = 0; i < values.size(); i++)
			if (values.get(i) == null)
				fulllisthasnull = true;
		if (fulllisthasnull)
			restrictedvalues.add(null);
		this.choicebox.getSelectionModel().select(null);
		this.choicebox.setItems(restrictedvalues);
		if (selectedvalid) {
			if (!forcetoblank) {
				this.choicebox.getSelectionModel().select(selectedvaluebeforerestriction);
				return false;
			} else {
				this.choicebox.getSelectionModel().select(null);
				return true;
			}
		}
		if (selected != null) {
			if (selected.length() > 0) {
				this.choicebox.getSelectionModel().select(lookupValueByStorage(selected));
				return false;
			} else {
				this.choicebox.getSelectionModel().select(null);
				return true;
			}
		}
		return true;
	}

	@Override
	public void liftConstraint() {
		this.choicebox.setItems(values);

	}

	@Override
	public void pingValue() {
		for (int i = 0; i < this.constraintsforcallback.size(); i++) {
			try {
				CMultiFieldConstraint thiscallback = this.constraintsforcallback.get(i);
				if (this.choicebox.getSelectionModel().getSelectedItem() != null)
					thiscallback.checkFieldEntry(this.getFieldname(),
							this.choicebox.getSelectionModel().getSelectedItem().getStorageCode());
			} catch (Exception e) {
				logger.info("got exception in validation of existing value " + e.toString());
				for (int j = 0; j < e.getStackTrace().length; j++) {
					logger.info(" at " + e.getStackTrace()[j]);
					if (j > 3)
						break;
				}
				choicebox.setValue(null);
			}
		}

	}

	@Override
	public TableColumn<CObjectGridLine<String>, ?> getTableColumnForGrid(
			PageActionManager pageactionmanager,
			int preferedrowheight,
			String actionkeyforupdate,
			String maincolumnvalue,
			String secondarycolumnvalue,
			boolean maincolumnvaluetitle) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void overridesLabel(String newlabel) {
		this.label = newlabel;

	}

}
