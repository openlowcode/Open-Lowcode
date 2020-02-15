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

import org.openlowcode.tools.messages.MessageBooleanField;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageIntegerField;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine;
import org.openlowcode.client.graphic.widget.table.ObjectTableRow;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.IntegerDataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataEltType;
import org.openlowcode.tools.trace.ExceptionLogger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * A widget to show and edit an integer field, either used as standalone or as
 * part of a data boject
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CIntegerField
		extends
		CBusinessField<IntegerDataElt> {
	private String helper;
	private String label;
	private String datafieldname;
	@SuppressWarnings("unused")
	private boolean businessparameter = false;
	private boolean iseditable = true;
	@SuppressWarnings("unused")
	private CPageAction action;

	private Integer inputvalue;
	@SuppressWarnings("unused")
	private Integer defaultvalue;
	private CPageDataRef datareference;
	private TextInputControl integerfield;
	@SuppressWarnings("unused")
	private int encryptiontype;
	@SuppressWarnings("unused")
	private int prefereddisplaysizeintable;

	/**
	 * creates the integer field from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CIntegerField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		label = reader.returnNextStringField("LBL");
		datafieldname = reader.returnNextStringField("DFN");
		helper = reader.returnNextStringField("HPR");

		defaultvalue = reader.returnNextIntegerField("DFV");
		this.inputvalue = null;
		businessparameter = reader.returnNextBooleanField("BSP");
		boolean externalreference = reader.returnNextBooleanField("EXR");
		if (externalreference) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
			if (!this.datareference.getType().equals(new IntegerDataEltType()))
				throw new RuntimeException(String.format(
						"Invalid external data reference named %s, excepted TextDataEltType, got %s in CPage ",
						datareference.getName(), datareference));
		}

		encryptiontype = reader.returnNextIntegerField("ECR");
		this.iseditable = !(reader.returnNextBooleanField("ROY"));
		MessageElement element = reader.getNextElement();

		if (element instanceof MessageStartStructure) { // CASE THERE IS ACTION
			MessageStartStructure actiontag = (MessageStartStructure) element;
			if (actiontag.getStructurename().compareTo("ACTION") == 0) {
				this.action = new CPageAction(reader);
				this.showintitle = reader.returnNextBooleanField("SIT");
			} else {
				throw new RuntimeException("only 'ACTION' structure available at this point, got "
						+ actiontag.getStructurename() + " at path " + reader.getCurrentElementPath());
			}
		} else { // CASE THERE IS NO ACTION
			if (element instanceof MessageBooleanField) {
				MessageBooleanField booleantag = (MessageBooleanField) element;
				if (((MessageBooleanField) element).getFieldName().compareTo("SIT") == 0) {
					this.showintitle = booleantag.getFieldContent();
				} else {
					throw new RuntimeException("expected a boolean 'SIT' tag, got " + element.toString() + " at path "
							+ reader.getCurrentElementPath());
				}

			} else {
				throw new RuntimeException("expected a boolean 'SIT' tag, got " + element.toString() + " at path "
						+ reader.getCurrentElementPath());
			}
		}
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
		reader.returnNextEndStructure("INF");
	}

	@Override
	public boolean isEditable() {
		return iseditable;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public String getFieldname() {
		return this.datafieldname;
	}

	@Override
	public Node getDisplayContent() {
		return this.integerfield;
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

		if (!(thiselement.getType() instanceof IntegerDataEltType))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							this.label, TextDataEltType.class, thiselement.getType().toString()));
		IntegerDataElt thistextelement = (IntegerDataElt) thiselement;
		this.inputvalue = thistextelement.getPayload();
		this.property = thistextelement.getPropertyname();

	}

	@Override
	public IntegerDataElt getFieldDataElt() {
		if (this.integerfield.getText() != null) {
			if (integerfield.getText().length() > 0) {
				return new IntegerDataElt(this.datafieldname, new Integer(integerfield.getText()));
			}
		}
		return new IntegerDataElt(this.datafieldname, null);
	}

	@Override
	public TableColumn<ObjectTableRow, Integer> getTableColumn(
			PageActionManager pageactionmanager,
			boolean largedisplay,
			int preferedrowheight,
			String actionkeyforupdate) {
		TableColumn<ObjectTableRow, Integer> thiscolumn = new TableColumn<ObjectTableRow, Integer>(this.getLabel());
		if (actionkeyforupdate != null) {
			thiscolumn.setEditable(true);
		} else {
			thiscolumn.setEditable(false);
		}
		int length = 110;
		thiscolumn.setMinWidth(length);
		CIntegerField thisintegerfield = this;

		thiscolumn.setCellFactory(column -> {
			return new TableCell<ObjectTableRow, Integer>() {
				@Override
				protected void updateItem(Integer integer, boolean empty) {
					super.updateItem(integer, empty);
					if (integer == null || empty) {
						setText("0");

					} else {
						setText(integer.toString());

					}

				}
			};
		});

		thiscolumn.setCellValueFactory(
				new Callback<CellDataFeatures<ObjectTableRow, Integer>, ObservableValue<Integer>>() {

					@Override
					public ObservableValue<Integer> call(CellDataFeatures<ObjectTableRow, Integer> p) {
						try {
							ObjectTableRow line = p.getValue();
							String fieldname = thisintegerfield.getFieldname();
							SimpleDataElt lineelement = line.getFieldDataEltClone(fieldname);

							if (lineelement == null)
								return new SimpleObjectProperty<Integer>(null);
							if (!(lineelement instanceof IntegerDataElt))
								return new SimpleObjectProperty<Integer>(null);
							IntegerDataElt linedataelt = (IntegerDataElt) lineelement;
							return new SimpleObjectProperty<Integer>(linedataelt.getPayload());
						} catch (RuntimeException e) {
							ExceptionLogger.setInLogs(e, logger);
							return null;
						}
					}

				});

		return thiscolumn;
	}

	@Override
	public void ForceAction(PageActionManager actionmanager, CPageAction action) {
		this.action = action;

	}

	@Override
	public TreeTableColumn<ObjectDataElt, Integer> getTreeTableColumn(
			PageActionManager pageactionmanager,
			String actionkeyforupdate) {
		TreeTableColumn<
				ObjectDataElt, Integer> thiscolumn = new TreeTableColumn<ObjectDataElt, Integer>(this.getLabel());
		if (actionkeyforupdate != null)
			thiscolumn.setEditable(true);
		int length = 110;
		thiscolumn.setMinWidth(length);
		CIntegerField thisdecimalfield = this;
		thiscolumn.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObjectDataElt, Integer>, ObservableValue<Integer>>() {

					@Override
					public ObservableValue<Integer> call(
							javafx.scene.control.TreeTableColumn.CellDataFeatures<ObjectDataElt, Integer> p) {
						ObjectDataElt line = p.getValue().getValue();
						String fieldname = thisdecimalfield.getFieldname();
						if (line == null)
							return new SimpleObjectProperty<Integer>(null);
						SimpleDataElt lineelement = line.lookupEltByName(fieldname);

						if (lineelement == null)
							return new SimpleObjectProperty<Integer>(null);
						if (!(lineelement instanceof IntegerDataElt))
							return new SimpleObjectProperty<Integer>(null);
						IntegerDataElt linedataelt = (IntegerDataElt) lineelement;
						return new SimpleObjectProperty<Integer>(linedataelt.getPayload());
					}

				});

		return thiscolumn;
	}

	@Override
	public int getPreferredTableRowHeight() {
		return 1;
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		if (this.datareference != null) {
			this.inputvalue = getExternalContent(inputdata, datareference);
		}

		FlowPane thispane = new FlowPane();
		Label thislabel = new Label(label);
		thislabel.setFont(Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
		thislabel.setMinWidth(120);
		thislabel.setWrapText(true);
		thislabel.setMaxWidth(120);
		thispane.setRowValignment(VPos.TOP);
		thispane.getChildren().add(thislabel);
		boolean readonly = false;
		if (!this.isactive)
			readonly = true;
		if (!this.iseditable)
			readonly = true;

		// ---------------------------- ACTIVE FIELD
		// ------------------------------------
		if (!readonly) {
			integerfield = new TextField();
			integerfield.textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldvalue, String newvalue) {
					if (newvalue.length() > 0) {
						try {
							new Integer(newvalue);
						} catch (NumberFormatException e) {
							integerfield.setText(oldvalue);
						}
					} else {
						integerfield.setText("0");
					}
				}

			});
			if (this.inputvalue != null)
				integerfield.setText(inputvalue.toString());
			thispane.getChildren().add(this.integerfield);
		} else {
			// ---------------------------- INACTIVE FIELD
			// ------------------------------------

			thispane.getChildren()
					.add(CTextField
							.getReadOnlyTextArea(actionmanager, (inputvalue != null ? inputvalue.toString() : ""), 15)
							.getNode());
		}

		return thispane;
	}

	private Integer getExternalContent(CPageData inputdata, CPageDataRef datareference) {
		DataElt thiselement = inputdata.lookupDataElementByName(datareference.getName());
		if (thiselement == null)
			throw new RuntimeException(
					String.format("could not find any page data with name = %s", datareference.getName()));
		if (!thiselement.getType().equals(datareference.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							datareference.getName(), datareference.getType(), thiselement.getType()));
		IntegerDataElt thisdecimalelement = (IntegerDataElt) thiselement;
		return thisdecimalelement.getPayload();
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {

		if (!(type instanceof IntegerDataEltType))
			throw new RuntimeException(String
					.format("Only IntegerDataEltType can be extracted from CIntegerField, but request was %s ", type));
		if (objectfieldname != null)
			throw new RuntimeException("indicated objectfieldname = '" + objectfieldname
					+ "', but the field is not supporting this parameter");

		return new IntegerDataElt(eltname, new Integer(this.integerfield.getText()));

	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}

	@Override
	public CPageNode deepcopyWithCallback(org.openlowcode.client.graphic.Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void mothball() {
	}

	@Override
	public boolean isRestrictionValid(String restriction) {
		// not implemented for integer
		return false;
	}

	@Override
	public String getValueForConstraint() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public boolean setConstraint(ArrayList<String> restrainedvalues, String selected) {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public void liftConstraint() {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public void pingValue() {
		throw new RuntimeException("Not yet implemented");

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
