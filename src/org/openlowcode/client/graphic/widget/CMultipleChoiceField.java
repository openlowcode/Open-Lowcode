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
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.MultipleChoiceDataElt;
import org.openlowcode.tools.structure.MultipleChoiceDataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableColumn;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * create a widget able to display and edit multi choices in a selection. This
 * can be standalone or part of a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CMultipleChoiceField
		extends
		CBusinessField<MultipleChoiceDataElt<?>> {

	private String label;
	private boolean compactshow;
	private boolean twolines;
	private String datafieldname;
	private String helper;

	@SuppressWarnings("unused")
	private boolean businessparameter;
	private boolean externalreference;
	private CPageDataRef datareference;
	private ObservableList<CChoiceFieldValue> values;
	private HashMap<String, CChoiceFieldValue> valuesbycode;
	private int maxcharlength;
	private boolean iseditable;
	@SuppressWarnings("unused")
	private CPageAction action;
	@SuppressWarnings("unused")
	private CPageInlineAction inlineaction;
	private int prefereddisplayintable;
	private String[] multipledefaultvaluecode;
	private ArrayList<CheckBox> checkboxpanel;
	private ArrayList<CChoiceFieldValue> preselectedvalues;
	private ChoiceField choicefield;

	/**
	 * creates the multiple choice field from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CMultipleChoiceField(MessageReader reader, CPageSignifPath parentpath)
			throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.preselectedvalues = new ArrayList<CChoiceFieldValue> ();
		label = reader.returnNextStringField("LBL");
		compactshow = reader.returnNextBooleanField("CPS");
		this.twolines = reader.returnNextBooleanField("TWL");

		datafieldname = reader.returnNextStringField("DFN");
		helper = reader.returnNextStringField("HPR");
		businessparameter = reader.returnNextBooleanField("BSP");
		externalreference = reader.returnNextBooleanField("EXR");
		if (externalreference) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
			if (!this.datareference.getType().equals(new MultipleChoiceDataEltType()))
				throw new RuntimeException(String.format(
						"Invalid external data reference named %s, excepted MultipleChoiceDataEltType, got %s in CPage ",
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
			boolean preselected=reader.returnNextBooleanField("PSL");
			if (preselected) preselectedvalues.add(thischoicevalue);
			if (thischoicevalue.getDisplayvalue().length() > maxcharlength)
				maxcharlength = thischoicevalue.getDisplayvalue().length();
			values.add(thischoicevalue);
			valuesbycode.put(thischoicevalue.getStorageCode(), thischoicevalue);
			reader.returnNextEndStructure("CCL");
			sequence++;
		}
		this.iseditable = !(reader.returnNextBooleanField("ROY"));
		boolean hasaction = reader.returnNextBooleanField("HAC");
		if (hasaction) {
			boolean treated = false;
			String structurestart = reader.returnNextStartStructure();
			if (structurestart.compareTo("ACTION") == 0) {
				action = new CPageAction(reader);

				treated = true;
			}
			if (structurestart.compareTo("INLINEACTION") == 0) {
				inlineaction = new CPageInlineAction(reader);

				treated = true;
			}
			if (!treated)
				throw new RuntimeException(
						" was expecting either ACTION or INLINEACTION structure, got " + structurestart);

		}
		this.showintitle = reader.returnNextBooleanField("SIT");
		this.prefereddisplayintable = reader.returnNextIntegerField("PDT");
		this.showinbottomnotes = reader.returnNextBooleanField("SBN");

		reader.returnNextEndStructure("MCF");
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
	public void overridesLabel(String newlabel) {
		this.label = newlabel;

	}

	@Override
	public String getFieldname() {
		return this.datafieldname;
	}

	@Override
	public Node getDisplayContent() {
		return this.choicefield.getNode();
	}

	@Override
	public String getHelper() {
		return this.helper;
	}

	@Override
	public void setContent(ObjectDataElt objectdata) {
		SimpleDataElt thiselement = objectdata.lookupEltByName(this.datafieldname);
		if (thiselement == null)
			throw new RuntimeException(String.format("could not find any page data with name = %s", this.label));
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

		} else {
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							this.label, MultipleChoiceDataEltType.class, thiselement.getType().toString()));
		}

	}

	public String displayMultiValue(MultipleChoiceDataElt<CChoiceFieldValue> multiplechoicedataelt) {
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < multiplechoicedataelt.getSelectedChoicesNumber(); i++) {
			String currentcode = multiplechoicedataelt.getSelectedChoiceAt(i);
			String displayvalue = valuesbycode.get(currentcode).getDisplayvalue();
			if (i > 0)
				buffer.append(", ");
			buffer.append(displayvalue);

		}
		return buffer.toString();
	}

	@Override
	public MultipleChoiceDataElt<CChoiceFieldValue> getFieldDataElt() {
		MultipleChoiceDataElt<
				CChoiceFieldValue> multipleelement = new MultipleChoiceDataElt<CChoiceFieldValue>(datafieldname);
		for (int i = 0; i < checkboxpanel.size(); i++) {
			CheckBox thischeckbox = checkboxpanel.get(i);
			if (thischeckbox.isSelected()) {
				multipleelement.addChoice(values.get(i));

			}
		}
		return multipleelement;
	}

	@Override
	public TableColumn<ObjectTableRow, ?> getTableColumn(
			PageActionManager pageactionmanager,
			boolean largedisplay,
			int preferedrowheight,
			String actionkeyforupdate) {
		TableColumn<ObjectTableRow, String> thiscolumn = new TableColumn<ObjectTableRow, String>(this.getLabel());
		thiscolumn.setEditable(false);
		int length = (this.maxcharlength * 7);
		if (length > 300)
			length = 300;
		if (this.prefereddisplayintable >= 0) {
			length = this.prefereddisplayintable * 7;

		}
		logger.fine(" --**-- length for field" + this.getLabel() + " maxcharlength:" + maxcharlength
				+ " pref display in table " + this.prefereddisplayintable + " final length = " + length);

		thiscolumn.setMinWidth(length);
		thiscolumn.setPrefWidth(length);
		CMultipleChoiceField thischoicefield = this;
		thiscolumn.setCellValueFactory(
				new Callback<TableColumn.CellDataFeatures<ObjectTableRow, String>, ObservableValue<String>>() {

					@SuppressWarnings("unchecked")
					@Override
					public ObservableValue<String> call(
							javafx.scene.control.TableColumn.CellDataFeatures<ObjectTableRow, String> p) {

						ObjectDataElt line = p.getValue().getObject();
						String fieldname = thischoicefield.getFieldname();
						if (line == null)
							return new SimpleStringProperty("");
						SimpleDataElt lineelement = line.lookupEltByName(fieldname);
						if (lineelement == null) {

							return new SimpleStringProperty("Field Not found !" + fieldname);
						}
						if (!(lineelement instanceof MultipleChoiceDataElt))
							return new SimpleStringProperty("Invalid type " + lineelement.getType());

						return new SimpleStringProperty(thischoicefield
								.displayMultiValue((MultipleChoiceDataElt<CChoiceFieldValue>) lineelement));
					}

				});
		return thiscolumn;
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
	public void ForceAction(PageActionManager actionmanager, CPageAction action) {
		this.action = action;

	}

	@Override
	public TreeTableColumn<ObjectDataElt, ?> getTreeTableColumn(
			PageActionManager pageactionmanager,
			String actionkeyforupdate) {
		TreeTableColumn<ObjectDataElt, String> thiscolumn = new TreeTableColumn<ObjectDataElt, String>(this.getLabel());
		thiscolumn.setEditable(true);
		int length = (this.maxcharlength * 7);
		if (length > 300)
			length = 300;
		if (this.prefereddisplayintable >= 0) {
			length = this.prefereddisplayintable * 7;

		}
		logger.fine(" --**-- length for field" + this.getLabel() + " maxcharlength:" + maxcharlength
				+ " pref display in table " + this.prefereddisplayintable + " final length = " + length);

		thiscolumn.setMinWidth(length);
		thiscolumn.setPrefWidth(length);
		CMultipleChoiceField thischoicefield = this;
		thiscolumn.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObjectDataElt, String>, ObservableValue<String>>() {

					@SuppressWarnings("unchecked")
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
						if (!(lineelement instanceof MultipleChoiceDataElt))
							return new SimpleStringProperty("Invalid type " + lineelement.getType());

						return new SimpleStringProperty(thischoicefield
								.displayMultiValue((MultipleChoiceDataElt<CChoiceFieldValue>) lineelement));
					}

				});
		return thiscolumn;
	}

	@Override
	public int getPreferredTableRowHeight() {
		return 1;
	}

	@Override
	public boolean isRestrictionValid(String restriction) {
		return false;
	}

	@Override
	public String getValueForConstraint() {
		return null;
	}

	@Override
	public boolean setConstraint(ArrayList<String> restrainedvalues, String selected) {
		return false;
	}

	@Override
	public void liftConstraint() {

	}

	@Override
	public void pingValue() {

	}

	@Override
	public CPageNode deepcopyWithCallback(org.openlowcode.client.graphic.Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void forceUpdateData(DataElt arg0) {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (!(type instanceof MultipleChoiceDataEltType))
			throw new RuntimeException(String.format(
					"Only MultipleChoiceDataEltType can be extracted from CMultiChoiceField, but request was %s",
					type));
		MultipleChoiceDataElt<
				CChoiceFieldValue> multipleelement = new MultipleChoiceDataElt<CChoiceFieldValue>(eltname);
		for (int i = 0; i < checkboxpanel.size(); i++) {
			CheckBox thischeckbox = checkboxpanel.get(i);
			if (thischeckbox.isSelected()) {
				multipleelement.addChoice(values.get(i));

			}
		}
		return multipleelement;
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
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		CChoiceFieldValue[] currentchoice = null;
		ArrayList<String> restrictedvalues = null;
		if (this.multipledefaultvaluecode != null) {
			currentchoice = new CChoiceFieldValue[multipledefaultvaluecode.length];
			for (int i = 0; i < this.multipledefaultvaluecode.length; i++) {
				currentchoice[i] = findChoiceFromStoredValue(multipledefaultvaluecode[i]);
			}
		}
		if (this.multipledefaultvaluecode==null) if (this.preselectedvalues.size()>0) {
			currentchoice = new CChoiceFieldValue[this.preselectedvalues.size()];
			for (int i=0;i<this.preselectedvalues.size();i++) currentchoice[i] = this.preselectedvalues.get(i);
		}
		choicefield = new ChoiceField(actionmanager, compactshow, twolines, label, helper, isactive, iseditable, false,
				values, currentchoice, restrictedvalues);
		Node node = choicefield.getNode();
		this.checkboxpanel = choicefield.getCheckBoxList();
		return node;
	}

	@Override
	public void mothball() {

	}

}
