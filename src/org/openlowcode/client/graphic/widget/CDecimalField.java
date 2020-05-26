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
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.openlowcode.tools.messages.MessageBooleanField;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageIntegerField;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.StandardUtil;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.widget.fields.BigDecimalFormatValidator;
import org.openlowcode.client.graphic.widget.fields.FormatValidator;
import org.openlowcode.client.graphic.widget.format.NiceLockableBigDecimalStringConverter;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine;
import org.openlowcode.client.graphic.widget.table.LargeTextTableCell;
import org.openlowcode.client.graphic.widget.table.LargeTextTreeTableCell;
import org.openlowcode.client.graphic.widget.table.ObjectDataElementValueUpdater;
import org.openlowcode.client.graphic.widget.table.ObjectTableRow;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine.ObjectInGrid;
import org.openlowcode.client.graphic.widget.table.EditableTreeTable;
import org.openlowcode.client.graphic.widget.table.EditableTreeTable.Operator;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.DecimalDataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataEltType;
import org.openlowcode.tools.trace.ExceptionLogger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * A field widget to show and modify a decimal field. This can be used alone or
 * as part of data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CDecimalField
		extends
		CBusinessField<DecimalDataElt> implements ObjectDataElementValueUpdater<ObjectDataElt,BigDecimal>{
	private String helper;
	private String label;
	private String datafieldname;
	@SuppressWarnings("unused")
	private boolean businessparameter = false;
	private boolean iseditable = true;
	@SuppressWarnings("unused")
	private CPageAction action;
	private int precision;
	private int scale;
	private BigDecimal inputvalue;
	@SuppressWarnings("unused")
	private BigDecimal defaultvalue;
	private CPageDataRef datareference;
	private TextInputControl decimalfield;
	@SuppressWarnings("unused")
	private int encryptiontype;
	@SuppressWarnings("unused")
	private int prefereddisplaysizeintable;
	private DecimalFormat formatfordecimal;
	private Node alternatefielddisplay;

	/**
	 * A utility class storing a big decimal payload with a boolean storing if the
	 * value is locked or not
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class LockableBigDecimal
			implements
			Comparable<LockableBigDecimal> {
		private boolean locked;
		private BigDecimal value;

		/**
		 * create a lockable big decimal
		 * 
		 * @param locked locked flag
		 * @param value  payload value
		 */
		public LockableBigDecimal(boolean locked, BigDecimal value) {
			super();
			this.locked = locked;
			this.value = value;
		}

		/**
		 * @return the locked flag
		 */
		public boolean isLocked() {
			return locked;
		}

		/**
		 * @return the big decimal value
		 */
		public BigDecimal getValue() {
			return value;
		}

		/**
		 * sets the locked flag
		 * 
		 * @param locked locked flag
		 */
		public void setLocked(boolean locked) {
			this.locked = locked;
		}

		/**
		 * set big decimal value
		 * 
		 * @param value big decimal value
		 */
		public void setValue(BigDecimal value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof LockableBigDecimal))
				return false;
			LockableBigDecimal otherlockabledecimal = (LockableBigDecimal) obj;
			if (otherlockabledecimal.value == null) {
				if (this.value == null)
					return true;
				if (this.value != null)
					return false;
			}
			// otherlockable has value
			if (this.value == null)
				return false;
			if (this.value.compareTo(otherlockabledecimal.value) != 0)
				return false;
			if (this.locked != otherlockabledecimal.locked)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "" + (value != null ? value : "");
		}

		@Override
		public int compareTo(LockableBigDecimal o) {
			if (o == null)
				return 1;
			if (o.value == null)
				return 1;
			if (value == null)
				return -1;
			return value.compareTo(o.value);
		}

	}

	/**
	 * sets the default format for dcimal for this decimal field
	 */
	public void setFormatForDecimal() {
		formatfordecimal = StandardUtil.getOLcDecimalFormatter();
	}

	/**
	 * creates a CDecimalField from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CDecimalField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		setFormatForDecimal();
		label = reader.returnNextStringField("LBL");
		datafieldname = reader.returnNextStringField("DFN");
		helper = reader.returnNextStringField("HPR");
		precision = reader.returnNextIntegerField("PRC");
		scale = reader.returnNextIntegerField("SCL");
		defaultvalue = reader.returnNextDecimalField("DFV");
		this.inputvalue = null;
		businessparameter = reader.returnNextBooleanField("BSP");
		boolean externalreference = reader.returnNextBooleanField("EXR");
		if (externalreference) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
			if (!this.datareference.getType().equals(new DecimalDataEltType()))
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
				throw new RuntimeException(String.format("only 'ACTION' structure available at this point, got "
						+ actiontag.getStructurename() + " at path " + reader.getCurrentElementPath()));
			}
		} else { // CASE THERE IS NO ACTION
			if (element instanceof MessageBooleanField) {
				MessageBooleanField booleantag = (MessageBooleanField) element;
				if (((MessageBooleanField) element).getFieldName().compareTo("SIT") == 0) {
					this.showintitle = booleantag.getFieldContent();
				} else {
					throw new RuntimeException(String.format("expected a boolean 'SIT' tag, got " + element.toString()
							+ " at path " + reader.getCurrentElementPath()));
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
		boolean hasDecimalFormatter = reader.returnNextBooleanField("HDF");
		if (hasDecimalFormatter) {
			this.decimalformatter = new CDecimalFormatter(reader);
		}
		reader.returnNextEndStructure("DCF");
		this.formatvalidator = new BigDecimalFormatValidator(precision, scale);
	}

	private CDecimalFormatter decimalformatter;
	private BigDecimalFormatValidator formatvalidator;

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
		if (this.alternatefielddisplay == null)
			return this.decimalfield;
		return this.alternatefielddisplay;
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

		if (!(thiselement.getType() instanceof DecimalDataEltType))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							this.label, TextDataEltType.class, thiselement.getType().toString()));
		DecimalDataElt thistextelement = (DecimalDataElt) thiselement;
		this.inputvalue = thistextelement.getPayload();
		this.property = thistextelement.getPropertyname();

	}

	@Override
	public DecimalDataElt getFieldDataElt() {
		try {
			if (this.decimalfield.getText() != null) {
				if (decimalfield.getText().length() > 0) {
					if (decimalfield.getText().equals("-")) {
						DecimalDataElt answer = new DecimalDataElt(this.datafieldname, null);
						if (this.property != null)
							answer.setPropertyname(this.property);
						return answer;
					} else {
						DecimalDataElt answer = new DecimalDataElt(this.datafieldname,
								(BigDecimal) (formatfordecimal.parse(decimalfield.getText())));
						if (this.property != null)
							answer.setPropertyname(this.property);
						return answer;
					}
				} else {
					DecimalDataElt answer = new DecimalDataElt(this.datafieldname, null);
					if (this.property != null)
						answer.setPropertyname(this.property);
				}

			}
			return new DecimalDataElt(this.datafieldname, null);
		} catch (ParseException e) {
			throw new RuntimeException("Error in parsing text into decimal " + e.getMessage());
		}
	}

	@Override
	public TreeTableColumn<ObjectDataElt, LockableBigDecimal> getTreeTableColumn(
			PageActionManager pageactionmanager,
			String actionkeyforupdate) {
		TreeTableColumn<
				ObjectDataElt, LockableBigDecimal> thiscolumn = new TreeTableColumn<ObjectDataElt, LockableBigDecimal>(
						this.getLabel());
		thiscolumn.setEditable(true);
		thiscolumn.setStyle("-fx-alignment: CENTER-RIGHT;");
		int length = 110;
		thiscolumn.setMinWidth(length);
		CDecimalField thisdecimalfield = this;

		BigDecimalFormatValidator validator = new BigDecimalFormatValidator(precision, scale);
		// big display disabled as hardcoded
		thiscolumn.setCellFactory(column -> {
			return new LargeTextTreeTableCell<ObjectDataElt, LockableBigDecimal>(
					new NiceLockableBigDecimalStringConverter(precision, scale), validator, this.decimalformatter,
					false, true,1) {
				@Override
				public void updateItem(LockableBigDecimal decimal, boolean empty) {
					logger.fine("Updating field for decimal = " + decimal + " empty = " + empty);
					super.updateItem(decimal, empty);

					super.setMaxHeight(12);
					super.setPrefHeight(12);
					super.setMinHeight(12);
					if (decimal != null) {
						if (decimal.isLocked()) {
							logger.fine("set to lock");

							super.setEditable(false);
						} else {
							logger.fine("set to unlock");

							super.setEditable(true);
						}
					}

				}

			};
		});

		thiscolumn.setCellValueFactory(new Callback<
				TreeTableColumn.CellDataFeatures<ObjectDataElt, LockableBigDecimal>,
				ObservableValue<LockableBigDecimal>>() {

			@Override
			public ObservableValue<LockableBigDecimal> call(
					TreeTableColumn.CellDataFeatures<ObjectDataElt, LockableBigDecimal> p) {

				SimpleDataElt lineelement = p.getValue().getValue().lookupEltByName(thisdecimalfield.getFieldname());
				if (lineelement == null)
					return new SimpleObjectProperty<LockableBigDecimal>(null);
				if (!(lineelement instanceof DecimalDataElt))
					return new SimpleObjectProperty<LockableBigDecimal>(null);
				DecimalDataElt linedataelt = (DecimalDataElt) lineelement;
				boolean locked = true;

				logger.finest("      *-*-*-* processing DecimalDataElt " + Integer.toHexString(linedataelt.hashCode())
						+ " locked = " + linedataelt.islocked() + ", payload = " + linedataelt.getPayload() + " name = "
						+ linedataelt.getName());
				return new SimpleObjectProperty<LockableBigDecimal>(
						new LockableBigDecimal(locked, linedataelt.getPayload()));

			}

		});

		return thiscolumn;
	}

	@Override
	public TableColumn<ObjectTableRow, LockableBigDecimal> getTableColumn(
			PageActionManager pageactionmanager,
			boolean largedisplay,
			int rowheight,
			String actionkeyforupdate) {
		TableColumn<
				ObjectTableRow,
				LockableBigDecimal> thiscolumn = new TableColumn<ObjectTableRow, LockableBigDecimal>(this.getLabel());
		thiscolumn.setStyle("-fx-alignment: TOP-RIGHT;");
		if ((actionkeyforupdate != null) && (this.isEditable()))  {
			thiscolumn.setEditable(true);
			CDecimalField thisdecimalfield = this;
			thiscolumn.setOnEditCommit(new EventHandler<CellEditEvent<ObjectTableRow, LockableBigDecimal>>() {

				@Override
				public void handle(CellEditEvent<ObjectTableRow, LockableBigDecimal> event) {
					try {
						logger.finer("Triggering cell edit event ");
						boolean treated = false;
						if (event.getOldValue() == null)
							if (event.getNewValue() != null) {
								ObjectTableRow objecttochange = event.getRowValue();
								objecttochange.updateField(thisdecimalfield.getFieldname(), event.getNewValue());
								logger.finer("Updated Decimal value for string " + thisdecimalfield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}
						if (event.getNewValue() == null)
							if (event.getOldValue() != null) {
								ObjectTableRow objecttochange = event.getRowValue();
								objecttochange.updateField(thisdecimalfield.getFieldname(), event.getNewValue());
								logger.finer("Updated Decimal value for string " + thisdecimalfield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}

						if (!treated)
							if (!(event.getOldValue().equals(event.getNewValue()))) {
								ObjectTableRow objecttochange = event.getRowValue();
								objecttochange.updateField(thisdecimalfield.getFieldname(), event.getNewValue());
								logger.finer("Updated Decimal value for string " + thisdecimalfield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}
						if (!treated) {
							logger.fine("received edit event where old values and new values are the same");
						}
					} catch (RuntimeException e) {
						logger.severe("exception in updating CDecimalField " + thisdecimalfield.getFieldname() + " : "
								+ e.getClass() + ": " + e.getMessage());
						ExceptionLogger.setInLogs(e, logger);
					}

				}
			}

			);
		} else {
			thiscolumn.setEditable(false);
		}
		double length = 80;
		double pixellength = (new Text(this.label)).getBoundsInLocal().getWidth() + 10;
		if (pixellength > length)
			length = pixellength;
		thiscolumn.setMinWidth(length);
		CDecimalField thisdecimalfield = this;
		BigDecimalFormatValidator validator = new BigDecimalFormatValidator(precision, scale);
		thiscolumn.setCellFactory(column -> {
			return new LargeTextTableCell<ObjectTableRow, LockableBigDecimal>(
					new NiceLockableBigDecimalStringConverter(precision, scale), validator, this.decimalformatter,
					largedisplay, true, (double) rowheight) {
				@Override
				public void updateItem(LockableBigDecimal decimal, boolean empty) {
					logger.fine("Updating field for decimal = " + decimal + " empty = " + empty);
					super.updateItem(decimal, empty);

					super.setMaxHeight(12);
					super.setPrefHeight(12);
					super.setMinHeight(12);
					if (decimal != null) {
						if (decimal.isLocked()) {
							logger.fine("set to lock");

							super.setEditable(false);
						} else {
							logger.fine("set to unlock");

							super.setEditable(true);
						}
					}

				}

			};
		});

		thiscolumn.setCellValueFactory(new Callback<
				CellDataFeatures<ObjectTableRow, LockableBigDecimal>, ObservableValue<LockableBigDecimal>>() {

			@Override
			public ObservableValue<LockableBigDecimal> call(CellDataFeatures<ObjectTableRow, LockableBigDecimal> p) {
				try {
					ObjectTableRow line = p.getValue();
					String fieldname = thisdecimalfield.getFieldname();
					SimpleDataElt lineelement = line.getFieldDataEltClone(fieldname);

					if (lineelement == null)
						return new SimpleObjectProperty<LockableBigDecimal>(null);
					if (!(lineelement instanceof DecimalDataElt))
						return new SimpleObjectProperty<LockableBigDecimal>(null);
					DecimalDataElt linedataelt = (DecimalDataElt) lineelement;
					boolean locked = linedataelt.islocked();
					if (line.isRowFrozen())
						locked = true;
					logger.finest("      *-*-*-* processing DecimalDataElt "
							+ Integer.toHexString(linedataelt.hashCode()) + " locked = " + linedataelt.islocked()
							+ ", payload = " + linedataelt.getPayload() + " name = " + linedataelt.getName());
					return new SimpleObjectProperty<LockableBigDecimal>(
							new LockableBigDecimal(locked, linedataelt.getPayload()));
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
	public int getPreferredTableRowHeight() {
		return 1;
	}

	/**
	 * extracts the BigDecimal payload from the data
	 * 
	 * @param inputdata input data
	 * @param dataref reference of the data
	 * @return the big decimal payload
	 */
	public BigDecimal getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		DecimalDataElt thisdecimalelement = (DecimalDataElt) thiselement;
		return thisdecimalelement.getPayload();
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
			decimalfield = new TextField();
			this.formatvalidator = new BigDecimalFormatValidator(precision, scale);
			decimalfield.textProperty().addListener(new ChangeListener<String>() {

				@Override
				public void changed(ObservableValue<? extends String> observable, String oldvalue, String newvalue) {
					String valueafterformatting = formatvalidator.valid(newvalue);
					if (valueafterformatting == null)
						decimalfield.setText(oldvalue);
					if (valueafterformatting != null)
						decimalfield.setText(valueafterformatting);

				}

			});

			if (this.inputvalue != null)
				decimalfield.setText(formatfordecimal.format(inputvalue));
			// if (this.inputvalue!=null) decimalfield.setText(formatfordecimal.format(new
			// LockableBigDecimal(false,inputvalue)));
			if (alternatefielddisplay == null) {
				thispane.getChildren().add(this.decimalfield);
			} else {
				thispane.getChildren().add(alternatefielddisplay);
			}

		} else {
			// ---------------------------- INACTIVE FIELD
			// ------------------------------------

			if (this.decimalformatter != null) {
				alternatefielddisplay = this.decimalformatter.getWidget(new LockableBigDecimal(false, inputvalue));
				thispane.getChildren().add(alternatefielddisplay);
			} else {
				thispane.getChildren()
						.add(CTextField
								.getReadOnlyTextArea(actionmanager,
										(inputvalue != null ? formatfordecimal.format(inputvalue) : ""), precision + 2)
								.getNode());
			}
		}

		return thispane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (!(type instanceof DecimalDataEltType))
			throw new RuntimeException(String
					.format("Only DecimalDataEltType can be extracted from CDecimalField, but request was %s ", type));
		if (objectfieldname != null)
			throw new RuntimeException("indicated objectfieldname = '" + objectfieldname
					+ "', but the field is not supporting this parameter");

		return new DecimalDataElt(eltname, new BigDecimal(formatfordecimal.format(this.decimalfield.getText())));

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
		if (restriction == null)
			return true;
		try {
			BigDecimal bigdecimal = new BigDecimal(restriction);
			int entryprecision = bigdecimal.precision();
			int entryscale = bigdecimal.scale();
			if ((entryprecision - entryscale) > (precision - scale))
				return false;
			if (entryscale > scale)
				return false;
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	@Override
	public String getValueForConstraint() {
		return decimalfield.getText();
	}

	@Override
	public boolean setConstraint(ArrayList<String> restrainedvalues, String selected) {
		if (restrainedvalues.size() == 1)
			if (restrainedvalues.get(0) != null) {
				logger.finer("set restricted value to " + restrainedvalues.get(0) + " for decimal field "
						+ this.datafieldname);
				decimalfield.setText(restrainedvalues.get(0));
				decimalfield.editableProperty().set(false);
				return false;
			}

		liftConstraint();
		return false;

	}

	@Override
	public void liftConstraint() {
		logger.finer("lifted constraint in field " + this.datafieldname);
		decimalfield.editableProperty().set(true);

	}

	@Override
	public void pingValue() {
		throw new RuntimeException("not implemented");

	}

	@Override
	public TableColumn<CObjectGridLine<String>, LockableBigDecimal> getTableColumnForGrid(
			PageActionManager pageactionmanager,
			int preferedrowheight,
			String actionkeyforupdate,
			String maincolumnvalue,
			String secondarycolumnvalue,
			boolean maincolumnvaluetitle) {
		TableColumn<
				CObjectGridLine<String>,
				LockableBigDecimal> thiscolumn = new TableColumn<CObjectGridLine<String>, LockableBigDecimal>(
						(secondarycolumnvalue != null ? secondarycolumnvalue
								: (maincolumnvaluetitle ? maincolumnvalue : this.getLabel())));
		thiscolumn.setStyle("-fx-alignment: CENTER-RIGHT;");
		CDecimalField thisdecimalfield = this;
		String thismaincolumnvalue = maincolumnvalue;
		String thissecondarycolumnvalue = secondarycolumnvalue;

		if (actionkeyforupdate != null) {
			thiscolumn.setEditable(true);

			thiscolumn.setOnEditCommit(new EventHandler<CellEditEvent<CObjectGridLine<String>, LockableBigDecimal>>() {
				public ObjectInGrid findObjectInGrid(CObjectGridLine<?> objectingrid) {
					if (thissecondarycolumnvalue == null)
						return objectingrid.getObjectInGrid(thismaincolumnvalue);
					return objectingrid.getObjectInGrid(thismaincolumnvalue, thissecondarycolumnvalue);
				}

				@Override
				public void handle(CellEditEvent<CObjectGridLine<String>, LockableBigDecimal> event) {
					try {
						logger.finer("Triggering cell edit event ");
						boolean treated = false;
						if (event.getOldValue() == null)
							if (event.getNewValue() != null) {
								CObjectGridLine<?> gridline = event.getRowValue();
								ObjectInGrid objectingrid = findObjectInGrid(gridline);
								objectingrid.updateField(thisdecimalfield.getFieldname(), event.getNewValue());
								logger.finer("Updated Decimal value for string " + thisdecimalfield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}
						if (event.getNewValue() == null)
							if (event.getOldValue() != null) {
								CObjectGridLine<?> gridline = event.getRowValue();
								ObjectInGrid objectingrid = findObjectInGrid(gridline);
								objectingrid.updateField(thisdecimalfield.getFieldname(), event.getNewValue());
								logger.finer("Updated Decimal value for string " + thisdecimalfield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}

						if (!treated)
							if (!(event.getOldValue().equals(event.getNewValue()))) {
								CObjectGridLine<?> gridline = event.getRowValue();
								ObjectInGrid objectingrid = findObjectInGrid(gridline);
								objectingrid.updateField(thisdecimalfield.getFieldname(), event.getNewValue());
								logger.finer("Updated Decimal value for string " + thisdecimalfield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}
						if (!treated) {
							logger.fine("received edit event where old values and new values are the same");
						}
					} catch (RuntimeException e) {
						logger.severe("exception in updating CDecimalField " + thisdecimalfield.getFieldname() + " : "
								+ e.getClass() + ": " + e.getMessage());
						ExceptionLogger.setInLogs(e, logger);
					}

				}
			}

			);
		} else {
			thiscolumn.setEditable(false);
		}
		double length = 80;
		String columntitle = (maincolumnvaluetitle ? maincolumnvalue : this.getLabel());
		double pixellength = (new Text(columntitle)).getBoundsInLocal().getWidth() + 10;
		if (pixellength > length) {
			length = pixellength;
			logger.finer(" ** -- ** setting grid column for label = '" + columntitle + "' to " + pixellength + " pts");
		} else {
			logger.finer(" ** -- ** setting grid column for label = '" + columntitle + "' to 80 pts > " + pixellength
					+ " pts");
		}
		thiscolumn.setMinWidth(length);

		BigDecimalFormatValidator validator = new BigDecimalFormatValidator(precision, scale);
		thiscolumn.setCellFactory(column -> {
			return new LargeTextTableCell<CObjectGridLine<String>, LockableBigDecimal>(
					new NiceLockableBigDecimalStringConverter(precision, scale), validator, this.decimalformatter,
					false, true, preferedrowheight) {
				@Override
				public void updateItem(LockableBigDecimal decimal, boolean empty) {
					logger.fine("Updating field for decimal = " + decimal + " empty = " + empty);
					super.updateItem(decimal, empty);

					super.setMaxHeight(12);
					super.setPrefHeight(12);
					super.setMinHeight(12);
					if (decimal != null) {
						if (decimal.isLocked()) {
							logger.fine("set to lock");

							super.setEditable(false);
						} else {
							logger.fine("set to unlock");

							super.setEditable(true);
						}
					}

				}

			};
		});

		thiscolumn.setCellValueFactory(new Callback<
				CellDataFeatures<CObjectGridLine<String>, LockableBigDecimal>, ObservableValue<LockableBigDecimal>>() {
			public ObjectInGrid findObjectInGrid(CObjectGridLine<?> objectingrid) {
				if (thissecondarycolumnvalue == null)
					return objectingrid.getObjectInGrid(thismaincolumnvalue);
				return objectingrid.getObjectInGrid(thismaincolumnvalue, thissecondarycolumnvalue);
			}

			@Override
			public ObservableValue<LockableBigDecimal> call(
					CellDataFeatures<CObjectGridLine<String>, LockableBigDecimal> p) {
				try {
					CObjectGridLine<?> gridline = p.getValue();
					ObjectInGrid objectingrid = findObjectInGrid(gridline);
					String fieldname = thisdecimalfield.getFieldname();
					if (objectingrid != null) {
						SimpleDataElt lineelement = objectingrid.getFieldDataEltClone(fieldname);

						if (lineelement == null)
							return new SimpleObjectProperty<LockableBigDecimal>(null);
						if (!(lineelement instanceof DecimalDataElt))
							return new SimpleObjectProperty<LockableBigDecimal>(null);
						DecimalDataElt linedataelt = (DecimalDataElt) lineelement;
						boolean locked = linedataelt.islocked();
						if (objectingrid.isRowFrozen())
							locked = true;
						logger.finest("      *-*-*-* processing DecimalDataElt "
								+ Integer.toHexString(linedataelt.hashCode()) + " locked = " + linedataelt.islocked()
								+ ", payload = " + linedataelt.getPayload() + " name = " + linedataelt.getName());
						return new SimpleObjectProperty<LockableBigDecimal>(
								new LockableBigDecimal(locked, linedataelt.getPayload()));
					} else {
						return new SimpleObjectProperty<LockableBigDecimal>(new LockableBigDecimal(true, null));
					}

				} catch (RuntimeException e) {
					ExceptionLogger.setInLogs(e, logger);
					return null;
				}
			}

		});

		return thiscolumn;
	}

	@Override
	public void overridesLabel(String newlabel) {
		this.label = newlabel;

	}

	// -------------------------------------------------------------------------
	// Extractor and integrator for management in tables
	// --------------------------------------------------------------------------
	
	private DecimalDataElt extractDataElement(ObjectDataElt object) {
		SimpleDataElt simpledataelt = object.lookupEltByName(this.datafieldname);
		if (simpledataelt == null) throw new RuntimeException("Could not find "+this.datafieldname+" for object "+object);
		if (!(simpledataelt instanceof DecimalDataElt)) throw new RuntimeException("field "+this.datafieldname+" is not DecimalDataElt, but "+simpledataelt.getClass().getName());
		return (DecimalDataElt) simpledataelt;
	}
	
	
	@Override
	public Function<ObjectDataElt, BigDecimal> fieldExtractor() {
		return (t) -> (extractDataElement(t).getPayload());
	}

	@Override
	public Function<BigDecimal, String> keyExtractor() {
		return (t) -> (t.toString());
	}

	@Override
	public Function<BigDecimal, String> labelExtractor() {
		return (t) -> (formatvalidator.print(t));
	}

	@Override
	public BiConsumer<ObjectDataElt, BigDecimal> payloadIntegration() {
		return (t,u) -> {extractDataElement(t).updatePayload(u);};
	}

	@Override
	public Operator<BigDecimal> operator() {
		return EditableTreeTable.BIGDECIMAL_OPERATOR;
	}

	@Override
	public FormatValidator<BigDecimal> formatValidator() {
		return formatvalidator;
	}
}
