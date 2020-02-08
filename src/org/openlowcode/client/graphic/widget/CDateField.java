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
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Date;

import org.openlowcode.tools.messages.MessageBooleanField;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.widget.format.DateFormatValidator;
import org.openlowcode.client.graphic.widget.format.NiceLockableDateStringConverter;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine;
import org.openlowcode.client.graphic.widget.table.LargeTextTableCell;
import org.openlowcode.client.graphic.widget.table.ObjectTableRow;
import org.openlowcode.client.graphic.widget.tools.DateField;
import org.openlowcode.client.graphic.widget.tools.TimestampPicker;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DateDataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.trace.ExceptionLogger;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TreeTableColumn;
import javafx.stage.Window;
import javafx.util.Callback;

/**
 * A widget holding a date field as payload. Can be used alone or as part of an
 * object display
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CDateField
		extends
		CBusinessField<DateDataElt> {
	private String label;

	private String datafieldname;
	private DatePicker datepickerfield; // to change
	private String helper;
	private int defaultvalue;
	private Date inputvalue;
	@SuppressWarnings("unused")
	private boolean businessparameter = false;
	private boolean readonly;
	private boolean timeedit;
	private CPageAction action;
	private CPageDataRef datareference;
	public static SimpleDateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	public static SimpleDateFormat fulldateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS z");
	private boolean compactshow;
	private boolean twolines;

	/**
	 * creates a CDateField from a message from the server
	 * 
	 * @param reader message reader
	 * @param parentpath parent path of the widget in the page
	 * @throws OLcRemoteException 
	 * @throws IOException
	 */
	public CDateField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		label = reader.returnNextStringField("LBL");
		datafieldname = reader.returnNextStringField("DFN");
		helper = reader.returnNextStringField("HPR");

		defaultvalue = reader.returnNextIntegerField("DFV");
		businessparameter = reader.returnNextBooleanField("BSP");
		timeedit = reader.returnNextBooleanField("TME");
		boolean externalreference = reader.returnNextBooleanField("EXR");
		if (externalreference) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
			if (!this.datareference.getType().equals(new DateDataEltType()))
				throw new RuntimeException(String.format(
						"Invalid external data reference named %s, excepted TextDataEltType, got %s in CPage ",
						datareference.getName(), datareference));
		}
		readonly = reader.returnNextBooleanField("ROY");
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

		this.showinbottomnotes = reader.returnNextBooleanField("SBN");
		this.compactshow = reader.returnNextBooleanField("CPS");
		this.twolines = reader.returnNextBooleanField("TWL");
		reader.returnNextEndStructure("DAT");
	}

	@Override
	public String getLabel() {
		return this.label;
	}

	@Override
	public Node getDisplayContent() {
		return null;
	}

	@Override
	public String getHelper() {
		return null;
	}

	
	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {

		DateField datefield = new DateField(actionmanager, compactshow, twolines, label, helper, readonly, isactive,
				timeedit, inputvalue, (defaultvalue == 1 ? true : false));

		Node node = datefield.generate();
		datepickerfield = datefield.getDatePicker();

		if ((!readonly) && (this.isactive)) {
			if (this.action != null) {
				actionmanager.registerEvent(datepickerfield, action);
				datepickerfield.setOnAction(actionmanager);

			}
		}

		return node;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (!(type instanceof DateDataEltType))
			throw new RuntimeException(
					String.format("Only DateDataEltType can be extracted from CDateField, but request was %s ", type));
		if (objectfieldname != null)
			throw new RuntimeException("indicated objectfieldname = '" + objectfieldname
					+ "', but the field is not supporting this parameter");
		if (datepickerfield.getValue() == null)
			return new DateDataElt(eltname, null);
		if (!timeedit)
			return new DateDataElt(eltname,
					Date.from(datepickerfield.getValue().atStartOfDay().toInstant(ZoneOffset.UTC)));
		return new DateDataElt(eltname, Date.from(
				((TimestampPicker) datepickerfield).getDateTimeValue().atZone(ZoneId.systemDefault()).toInstant()));
	}

	@Override
	public void setContent(ObjectDataElt objectdata) {
		SimpleDataElt thiselement = objectdata.lookupEltByName(this.datafieldname);
		if (thiselement == null)
			throw new RuntimeException(String.format("could not find any page data with name = %s", this.label));
		if (!(thiselement.getType() instanceof DateDataEltType)) {
			this.inputvalue = null;
			logger.severe("page data with name =" + this.label
					+ " does not have expected DateDataEltType type, actually found " + thiselement.getType());
		} else {
			DateDataElt thisdateelement = (DateDataElt) thiselement;
			this.inputvalue = thisdateelement.getPayload();
		}
		this.property = thiselement.getPropertyname();
	}

	@Override
	public DateDataElt getFieldDataElt() {
		if (this.datepickerfield == null)
			throw new RuntimeException("" + "Trying to get value from non-displayed choice field " + this.datafieldname
					+ " - path: " + this.getParentpath() + " parent -  "
					+ (this.getParentforfield() != null ? this.getParentforfield().getSignificantpath() : "unset"));
		DateDataElt answer = null;
		if (datepickerfield.getValue() == null) {
			answer = new DateDataElt(this.datafieldname, null);
		} else {
			if (!timeedit)
				answer = new DateDataElt(this.datafieldname,
						Date.from(datepickerfield.getValue().atStartOfDay().toInstant(ZoneOffset.UTC)));
			if (timeedit)
				answer = new DateDataElt(this.datafieldname, Date.from(((TimestampPicker) datepickerfield)
						.getDateTimeValue().atZone(ZoneId.systemDefault()).toInstant()));
		}
		if (this.property != null)
			answer.setPropertyname(this.property);
		return answer;
	}

	@Override
	public String getFieldname() {
		return this.datafieldname;
	}

	@Override
	public boolean isEditable() {

		return !readonly;
	}

	@Override
	public void ForceAction(PageActionManager actionmanager, CPageAction action) {
		this.action = action;

	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}

	@Override
	public TreeTableColumn<ObjectDataElt, Date> getTreeTableColumn(
			PageActionManager pageactionmanager,
			String actionkeyforupdate) {
		TreeTableColumn<ObjectDataElt, Date> thiscolumn = new TreeTableColumn<ObjectDataElt, Date>(this.getLabel());
		thiscolumn.setEditable(true);
		int length = 110;
		thiscolumn.setMinWidth(length);
		thiscolumn.setCellFactory(column -> {
			return new TreeTableCell<ObjectDataElt, Date>() {
				@Override
				protected void updateItem(Date date, boolean empty) {
					super.updateItem(date, empty);
					if (date == null || empty) {
						setText(null);

					} else {
						setText(dateformat.format(date));

						this.setTooltip(new Tooltip(fulldateformat.format(date)));
					}

				}
			};
		});
		CDateField thisdatefield = this;
		thiscolumn.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObjectDataElt, Date>, ObservableValue<Date>>() {

					@Override
					public ObservableValue<Date> call(
							javafx.scene.control.TreeTableColumn.CellDataFeatures<ObjectDataElt, Date> p) {
						ObjectDataElt line = p.getValue().getValue();
						String fieldname = thisdatefield.getFieldname();
						if (line == null)
							return new SimpleObjectProperty<Date>(null);
						SimpleDataElt lineelement = line.lookupEltByName(fieldname);

						if (lineelement == null)
							return new SimpleObjectProperty<Date>(null);
						if (!(lineelement instanceof DateDataElt))
							return new SimpleObjectProperty<Date>(null);
						DateDataElt linedataelt = (DateDataElt) lineelement;
						return new SimpleObjectProperty<Date>(linedataelt.getPayload());
					}

				});

		return thiscolumn;
	}

	/**
	 * A utility class holding a date that can be locked
	 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
	 *
	 */
	public static class LockableDate
			implements
			Comparable<LockableDate> {
		private boolean locked;
		private Date value;

		/**
		 * creates a lockable date
		 * 
		 * @param locked locked  if true
		 * @param value dae value
		 */
		public LockableDate(boolean locked, Date value) {
			super();
			this.locked = locked;
			this.value = value;
		}

		/**
		 * @return true if locked
		 */
		public boolean isLocked() {
			return locked;
		}

		/**
		 * @return date payload
		 */
		public Date getValue() {
			return value;
		}

		/**
		 * @param locked sets the date to locked if true, unlocked if false
		 */
		public void setLocked(boolean locked) {
			this.locked = locked;
		}

		/**
		 * @param value sets the payload date
		 */
		public void setValue(Date value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof LockableDate))
				return false;
			LockableDate otherlockabledate = (LockableDate) obj;
			if (otherlockabledate.value == null) {
				if (this.value == null)
					return true;
				if (this.value != null)
					return false;
			}
			// otherlockable has value
			if (this.value == null)
				return false;
			if (this.value.compareTo(otherlockabledate.value) != 0)
				return false;
			if (this.locked != otherlockabledate.locked)
				return false;
			return true;
		}

		@Override
		public String toString() {
			return "[LD value = " + value + ", locked = " + locked + " ]";
		}

		@Override
		public int compareTo(LockableDate object) {
			if (object instanceof LockableDate) {
				LockableDate otherdate = (LockableDate) object;
				if (value == null) {
					if (otherdate.value == null)
						return 0;
					return -1;
				}
				if (value != null) {
					if (otherdate.value == null)
						return 1;
					return value.compareTo(otherdate.value);
				}
			}
			return 1;

		}
	}

	@Override
	public TableColumn<ObjectTableRow, ?> getTableColumn(
			PageActionManager pageactionmanager,
			boolean largedisplay,
			int rowheight,
			String actionkeyforupdate) {
		TableColumn<
				ObjectTableRow,
				LockableDate> thiscolumn = new TableColumn<ObjectTableRow, LockableDate>(this.getLabel());

		if (actionkeyforupdate != null) {
			thiscolumn.setEditable(true);
			CDateField thisdatefield = this;
			thiscolumn.setOnEditCommit(new EventHandler<CellEditEvent<ObjectTableRow, LockableDate>>() {

				@Override
				public void handle(CellEditEvent<ObjectTableRow, LockableDate> event) {
					try {
						logger.finer("Triggering cell edit event ");
						boolean treated = false;
						if (event.getOldValue() == null)
							if (event.getNewValue() != null) {
								ObjectTableRow objecttochange = event.getRowValue();
								objecttochange.updateField(thisdatefield.getFieldname(), event.getNewValue());
								logger.finer("Updated Date value for string " + thisdatefield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}
						if (event.getNewValue() == null)
							if (event.getOldValue() != null) {
								ObjectTableRow objecttochange = event.getRowValue();
								objecttochange.updateField(thisdatefield.getFieldname(), event.getNewValue());
								logger.finer("Updated Date value for string " + thisdatefield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}

						if (!treated)
							if (!(event.getOldValue().equals(event.getNewValue()))) {
								ObjectTableRow objecttochange = event.getRowValue();
								objecttochange.updateField(thisdatefield.getFieldname(), event.getNewValue());
								logger.finer("Updated Date value for string " + thisdatefield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}
						if (!treated) {
							logger.fine("received edit event where old values and new values are the same");
						}
					} catch (RuntimeException e) {
						logger.severe("exception in updating Date " + thisdatefield.getFieldname() + " : "
								+ e.getClass() + ": " + e.getMessage());
						ExceptionLogger.setInLogs(e, logger);
					}

				}
			}

			);
		} else {
			thiscolumn.setEditable(false);
		}
		int length = 120;
		thiscolumn.setMinWidth(length);
		CDateField thisdatefield = this;

		DateFormatValidator validator = new DateFormatValidator();
		thiscolumn.setCellFactory(column -> {
			return new LargeTextTableCell<ObjectTableRow, LockableDate>(new NiceLockableDateStringConverter(),
					validator, null, largedisplay, rowheight) {
				@Override
				public void updateItem(LockableDate date, boolean empty) {
					logger.fine("Updating field for date = " + date + " empty = " + empty);
					super.updateItem(date, empty);

					super.setMaxHeight(12);
					super.setPrefHeight(12);
					super.setMinHeight(12);
					if (date != null) {
						if (date.isLocked()) {
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

		thiscolumn.setCellValueFactory(
				new Callback<CellDataFeatures<ObjectTableRow, LockableDate>, ObservableValue<LockableDate>>() {

					@Override
					public ObservableValue<LockableDate> call(CellDataFeatures<ObjectTableRow, LockableDate> p) {
						try {
							ObjectTableRow line = p.getValue();
							String fieldname = thisdatefield.getFieldname();
							SimpleDataElt lineelement = line.getFieldDataEltClone(fieldname);

							if (lineelement == null)
								return new SimpleObjectProperty<LockableDate>(null);
							if (!(lineelement instanceof DateDataElt))
								return new SimpleObjectProperty<LockableDate>(null);
							DateDataElt linedataelt = (DateDataElt) lineelement;
							return new SimpleObjectProperty<LockableDate>(
									new LockableDate(line.isRowFrozen(), linedataelt.getPayload()));
						} catch (RuntimeException e) {
							ExceptionLogger.setInLogs(e, logger);
							return null;
						}
					}

				});

		return thiscolumn;
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
	public TableColumn<CObjectGridLine<?>, ?> getTableColumnForGrid(
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
