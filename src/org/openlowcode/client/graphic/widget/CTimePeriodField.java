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
import java.util.function.Function;

import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import javafx.util.Callback;

import org.openlowcode.client.graphic.widget.fields.TimePeriodField;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine;
import org.openlowcode.client.graphic.widget.table.ObjectDataElementKeyExtractor;
import org.openlowcode.client.graphic.widget.table.ObjectTableRow;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.data.TimePeriod;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.richtext.RichTextArea;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;
import org.openlowcode.tools.structure.TimePeriodDataElt;
import org.openlowcode.tools.structure.TimePeriodDataEltType;
import org.openlowcode.tools.trace.ExceptionLogger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.Cell;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.Window;

/**
 * A field to show and update a time period (in the sense of accounting time
 * period: year, quarter, month..., either to be used standalone or as part of
 * the data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CTimePeriodField
		extends
		CBusinessField<TimePeriodDataElt> implements ObjectDataElementKeyExtractor<ObjectDataElt,TimePeriod> {

	private String datafieldname;
	private String label;
	private String helper;
	@SuppressWarnings("unused")
	private boolean businessparameter;
	private CPageDataRef datareference;
	private boolean iseditable;
	@SuppressWarnings("unused")
	private CPageAction action;
	@SuppressWarnings("unused")
	private CPageInlineAction inlineaction;
	private int prefereddisplayintable;
	private TimePeriod payload;
	private TimePeriodField field;
	private TimePeriod.PeriodType periodtype;
	private boolean compactshow = false;
	private boolean twolines = false;

	/**
	 * create a time period field from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CTimePeriodField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		label = reader.returnNextStringField("LBL");
		datafieldname = reader.returnNextStringField("DFN");
		helper = reader.returnNextStringField("HPR");
		businessparameter = reader.returnNextBooleanField("BSP");
		boolean externalreference = reader.returnNextBooleanField("EXR");
		if (externalreference) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
			if (!this.datareference.getType().equals(new TimePeriodDataEltType()))
				throw new RuntimeException(String.format(
						"Invalid external data reference named %s, excepted TimePeriodDataEltType, got %s in CPage ",
						datareference.getName(), datareference));
		}
		this.iseditable = !(reader.returnNextBooleanField("ROY"));
		boolean hasaction = reader.returnNextBooleanField("ACT");
		if (hasaction) {
			String actiontag = reader.returnNextStartStructure();
			boolean treated = false;

			if (actiontag.compareTo("ACTION") == 0) {
				action = new CPageAction(reader);

				treated = true;
			}
			if (actiontag.compareTo("INLINEACTION") == 0) {
				inlineaction = new CPageInlineAction(reader);

				treated = true;
			}
			if (!treated)
				throw new RuntimeException(" was expecting either ACTION or INLINEACTION structure, got " + actiontag);

		}
		this.showintitle = reader.returnNextBooleanField("SIT");
		this.prefereddisplayintable = reader.returnNextIntegerField("PDT");
		this.showinbottomnotes = reader.returnNextBooleanField("SBN");
		String periodtypestring = reader.returnNextStringField("PDT");
		if (periodtypestring != null)
			this.periodtype = TimePeriod.PeriodType.valueOf(periodtypestring);
		reader.returnNextEndStructure("TPF");
		logger.finer("CTimePeriodField periodtype = " + this.periodtype + ", original field = " + periodtypestring);
	}

	/**
	 * @return get the payload of this field
	 */
	public TimePeriod getPayload() {
		return this.payload;
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
		return field;
	}

	@Override
	public String getHelper() {
		return this.getHelper();
	}

	@Override
	public void setContent(ObjectDataElt objectdata) {
		SimpleDataElt thiselement = objectdata.lookupEltByName(this.datafieldname);
		if (thiselement == null)
			throw new RuntimeException(String.format("could not find any page data with name = %s", this.label));

		boolean treated = false;

		if ((thiselement.getType() instanceof TimePeriodDataEltType)) {
			TimePeriodDataElt timeperiodelement = (TimePeriodDataElt) thiselement;
			this.payload = timeperiodelement.getPayload();
			if (field != null)
				field.setTimePeriod(payload);
			treated = true;

		}

		if ((thiselement.getType() instanceof TextDataEltType)) {
			TextDataElt timeperiodstring = (TextDataElt) thiselement;
			this.payload = TimePeriod.generateFromString(timeperiodstring.getPayload());
			if (field != null)
				field.setTimePeriod(payload);
			treated = true;
		}
		if (!treated)
			throw new RuntimeException(String.format(
					"page data with name = %s does not have expected %s  or %s type, actually found %s", this.label,
					TimePeriodDataEltType.class, TextDataEltType.class, thiselement.getType().toString()));

	}

	@Override
	public TimePeriodDataElt getFieldDataElt() {
		this.payload = field.getTimePeriod();
		logger.severe("Extracting timeperioddataelt " + payload);
		return new TimePeriodDataElt(datafieldname, payload);
	}

	@Override
	public TableColumn<ObjectTableRow, TimePeriod> getTableColumn(
			PageActionManager pageactionmanager,
			boolean largedisplay,
			int preferedrowheight,
			String actionkeyforupdate,boolean forcefieldupdatable) {
		TableColumn<
				ObjectTableRow, TimePeriod> thiscolumn = new TableColumn<ObjectTableRow, TimePeriod>(this.getLabel());
		if (actionkeyforupdate != null) {
			thiscolumn.setEditable(true);
			thiscolumn.setOnEditCommit(new TableColumnOnEditCommit(this));
		} else {
			thiscolumn.setEditable(false);
		}

		int length = 15 * 7;
		if (length > 300)
			length = 300;

		double pixellength = ((new Text(this.label)).getBoundsInLocal().getWidth() + 10) * 1.05;

		int pixellengthi = (int) pixellength;
		thiscolumn.setMinWidth(pixellengthi);
		thiscolumn.setPrefWidth(pixellengthi);
		thiscolumn.setCellValueFactory(new TableCellValueFactory(this));

		thiscolumn.setCellFactory(new TableCellFactory(helper, periodtype));

		return thiscolumn;
	}

	private static class TableCellFactory
			implements
			Callback<TableColumn<ObjectTableRow, TimePeriod>, TableCell<ObjectTableRow, TimePeriod>> {
		private String helper;
		private TimePeriod.PeriodType periodtype;

		public TableCellFactory(String helper, TimePeriod.PeriodType periodtype) {
			this.helper = helper;
			this.periodtype = periodtype;
		}

		@Override
		public TableCell<ObjectTableRow, TimePeriod> call(TableColumn<ObjectTableRow, TimePeriod> param) {

			return new CTimePeriodCell(helper, periodtype);
		}

	}

	private static class TableColumnOnEditCommit
			implements
			EventHandler<CellEditEvent<ObjectTableRow, TimePeriod>> {
		private CTimePeriodField thistimeperiodfield;

		public TableColumnOnEditCommit(CTimePeriodField thistimeperiodfield) {
			this.thistimeperiodfield = thistimeperiodfield;
		}

		@Override
		public void handle(CellEditEvent<ObjectTableRow, TimePeriod> event) {
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
					objecttochange.updateField(thistimeperiodfield.getFieldname(), event.getNewValue());
					logger.info("Updated timeperiod for field " + thistimeperiodfield.getFieldname() + ", new value = "
							+ event.getNewValue());
				} else {
					logger.info("received edit event where old values and new values are the same");
				}
			} catch (RuntimeException e) {
				logger.severe("exception in updating TimePeriod " + thistimeperiodfield.getFieldname() + " : "
						+ e.getClass() + ": " + e.getMessage());
				ExceptionLogger.setInLogs(e, logger);
			}

		}

	}

	private static class TableCellValueFactory
			implements
			Callback<TableColumn.CellDataFeatures<ObjectTableRow, TimePeriod>, ObservableValue<TimePeriod>> {
		private CTimePeriodField thistimeperiodfield;

		public TableCellValueFactory(CTimePeriodField thistimeperiodfield) {
			this.thistimeperiodfield = thistimeperiodfield;
		}

		@Override
		public ObservableValue<TimePeriod> call(CellDataFeatures<ObjectTableRow, TimePeriod> p) {
			try {
				ObjectTableRow line = p.getValue();
				String fieldname = thistimeperiodfield.getFieldname();
				SimpleDataElt lineelement = line.getFieldDataEltClone(fieldname);
				if (lineelement == null) {

					return null;
				}
				TimePeriod period = null;
				if (lineelement instanceof TextDataElt) {
					TextDataElt textelement = (TextDataElt) lineelement;
					period = TimePeriod.generateFromString(textelement.getPayload());
				}
				if (lineelement instanceof TimePeriodDataElt) {
					TimePeriodDataElt timeperiodelement = (TimePeriodDataElt) lineelement;
					period = timeperiodelement.getPayload();
				}
				return new SimpleObjectProperty<TimePeriod>(period);
			} catch (RuntimeException e) {
				ExceptionLogger.setInLogs(e, logger);
				return null;
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
	public void ForceAction(PageActionManager actionmanager, CPageAction action) {
	}

	@Override
	public TreeTableColumn<ObjectDataElt, ?> getTreeTableColumn(
			PageActionManager pageactionmanager,
			String actionkeyforupdate) {
		TreeTableColumn<ObjectDataElt, String> thiscolumn = new TreeTableColumn<ObjectDataElt, String>(this.getLabel());
		thiscolumn.setEditable(true);
		int length = 20 * 7;
		if (this.prefereddisplayintable >= 0) {
			length = this.prefereddisplayintable * 7;

		}

		thiscolumn.setMinWidth(length);
		thiscolumn.setPrefWidth(length);
		CTimePeriodField thistimeperiodfield = this;
		thiscolumn.setCellValueFactory(new javafx.util.Callback<
				TreeTableColumn.CellDataFeatures<ObjectDataElt, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(
					javafx.scene.control.TreeTableColumn.CellDataFeatures<ObjectDataElt, String> p) {

				ObjectDataElt line = p.getValue().getValue();
				String fieldname = thistimeperiodfield.getFieldname();
				if (line == null)
					return new SimpleStringProperty("");
				SimpleDataElt lineelement = line.lookupEltByName(fieldname);
				String displayasstring = "Field Not found or bad type: " + fieldname + "/"
						+ (lineelement != null ? lineelement.getClass() : "NULL");

				if (lineelement != null)
					if (lineelement instanceof TimePeriodDataElt) {
						TimePeriodDataElt tpelement = (TimePeriodDataElt) lineelement;
						return new SimpleStringProperty(
								tpelement.getPayload() != null ? tpelement.getPayload().toString() : "");
					}
				if (lineelement != null)
					if (lineelement instanceof TextDataElt) {
						TextDataElt text = (TextDataElt) lineelement;
						TimePeriod tp = TimePeriod.generateFromString(text.getPayload());
						return new SimpleStringProperty(tp != null ? tp.toString() : "");
					}

				return new SimpleStringProperty(displayasstring);
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
		// nothing to do

	}

	@Override
	public void pingValue() {
		// nothing to do

	}

	@Override
	public CPageNode deepcopyWithCallback(org.openlowcode.client.graphic.Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void forceUpdateData(DataElt arg0) {
		throw new RuntimeException("Not yet implemented");

	}

	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (objectfieldname != null)
			throw new RuntimeException("indicated objectfieldname = '" + objectfieldname
					+ "', but the field is not supporting this parameter");
		if (!(type instanceof TimePeriodDataEltType))
			throw new RuntimeException(String.format(
					"Only TimePeriodChoiceDataEltType can be extracted from CTimePeriodField, but request was %s ",
					type));
		this.payload = this.field.getTimePeriod();
		return new TimePeriodDataElt(eltname, payload);

	}

	public TimePeriod getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		TimePeriodDataElt thistimeperiodelement = (TimePeriodDataElt) thiselement;
		return thistimeperiodelement.getPayload();
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		if (this.datareference != null) {
			this.payload = getExternalContent(inputdata, datareference);
		}

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
		thislabel.setFont(Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
		thislabel.setMinWidth(120);
		thislabel.setWrapText(true);
		thislabel.setMaxWidth(120);

		thispane.getChildren().add(thislabel);
		boolean readonly = false;
		if (!this.isactive)
			readonly = true;
		if (!this.iseditable)
			readonly = true;
		if (!readonly) {
			field = new TimePeriodField(periodtype, payload);
			thispane.getChildren().add(field);

		} else {
			thispane.getChildren().add(RichTextArea
					.getReadOnlyTextArea(actionmanager, (this.payload != null ? this.payload.toString() : ""), 400)
					.getNode());
		}
		return thispane;
	}

	@Override
	public void mothball() {

	}

	private static class CTimePeriodCell
			extends
			TableCell<ObjectTableRow, TimePeriod> {
		@SuppressWarnings("unused")
		private String helper;
		private TimePeriod.PeriodType periodtype;
		private TimePeriodField timeperiodfield;

		public CTimePeriodCell(String helper, TimePeriod.PeriodType periodtype) {
			super();
			this.helper = helper;
			this.periodtype = periodtype;

		}

		@Override
		public void startEdit() {
			if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
				return;
			}

			if (timeperiodfield == null) {
				timeperiodfield = new TimePeriodField(periodtype);
			}
			timeperiodfield.setTimePeriod(getItem());

			super.startEdit();
			setText(null);
			setGraphic(timeperiodfield);
		}

		/** {@inheritDoc} */
		@Override
		public void cancelEdit() {
			super.cancelEdit();

			setText(getItem().toString());
			setGraphic(null);
		}

		/** {@inheritDoc} */
		@Override
		public void updateItem(TimePeriod item, boolean empty) {
			super.updateItem(item, empty);
			updateItem(this, null, null, timeperiodfield);
		}

		static void updateItem(
				final Cell<TimePeriod> cell,
				final HBox hbox,
				final Node graphic,
				final TimePeriodField timeperiodfield) {
			if (cell.isEmpty()) {
				cell.setText(null);
				cell.setGraphic(null);
			} else {
				if (cell.isEditing()) {
					if (timeperiodfield != null) {
						timeperiodfield.setTimePeriod(cell.getItem());
					}
					cell.setText(null);

					if (graphic != null) {
						hbox.getChildren().setAll(graphic, timeperiodfield);
						cell.setGraphic(hbox);
					} else {
						cell.setGraphic(timeperiodfield);
					}
				} else {
					cell.setText((cell.getItem() != null ? cell.getItem().toString() : ""));
					cell.setGraphic(graphic);
				}
			}
		}

	}

	// ---------------------------------------------------------------------------
	// key extractors
	// ---------------------------------------------------------------------------
	
	
	@Override
	public Function<ObjectDataElt, TimePeriod> fieldExtractor() {
		return (t) -> {				String fieldname = CTimePeriodField.this.getFieldname();
				
				SimpleDataElt lineelement = t.lookupEltByName(fieldname);
				if (lineelement==null) throw new RuntimeException("Element for "+fieldname+" does not exist on object "+t);
				if (lineelement != null)
					if (lineelement instanceof TimePeriodDataElt) {
						TimePeriodDataElt tpelement = (TimePeriodDataElt) lineelement;
						return tpelement.getPayload();
					}
				if (lineelement != null)
					if (lineelement instanceof TextDataElt) {
						TextDataElt text = (TextDataElt) lineelement;
						return TimePeriod.generateFromString(text.getPayload());
					}
				return null;
			};
		
	}

	@Override
	public Function<TimePeriod, String> keyExtractor() {
		return (t) -> (t.encode());

	}

	@Override
	public Function<TimePeriod, String> labelExtractor() {
		return (t) -> (t.toString());
	}

	@Override
	public Function<TimePeriod, Boolean> HorizontalSumException() {

		return (t) -> (new Boolean(!t.isFull()));
	}
	
	

}
