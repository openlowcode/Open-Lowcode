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

import java.util.ArrayList;

import java.util.HashMap;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.widget.CBusinessField;
import org.openlowcode.client.graphic.widget.CObjectArray.UpdateMouseHandler;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.ObjectDataElt;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.TreeTableRow;

/**
 * creates a column model for an object array
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CObjectArrayColumnModel {
	private static Logger logger = Logger.getLogger(CObjectArrayColumnModel.class.getName());
	private ArrayList<CBusinessField<?>> arraycolumns;
	private ArrayList<CBusinessField<?>> columnstoshowintooltip;
	private HashMap<String, String> updateactionfields;
	private int finalheightinpixel = 12 + 15;
	private UpdateMouseHandler updatemousehandler;

	/**
	 * create a column model for the given business fields
	 * 
	 * @param arraycolumns a list of busines fields
	 */
	public CObjectArrayColumnModel(ArrayList<CBusinessField<?>> arraycolumns) {
		this.arraycolumns = arraycolumns;
		this.columnstoshowintooltip = new ArrayList<CBusinessField<?>>();
		this.updateactionfields = new HashMap<String, String>();

	}

	/**
	 * creates a column model
	 * 
	 * @param arraycolumns       a list of busines fields
	 * @param updateactionfields list of fields that should trigger an unique action
	 * @param uniqueupdatekey    key of the update inline action and action
	 * @param updatemousehandler mouse handler for update
	 */
	public CObjectArrayColumnModel(
			ArrayList<CBusinessField<?>> arraycolumns,
			ArrayList<String> updateactionfields,
			String uniqueupdatekey,
			UpdateMouseHandler updatemousehandler) {
		this.arraycolumns = arraycolumns;
		this.columnstoshowintooltip = new ArrayList<CBusinessField<?>>();
		this.updateactionfields = new HashMap<String, String>();
		if (updateactionfields != null) {
			for (int i = 0; i < updateactionfields.size(); i++)
				this.updateactionfields.put(updateactionfields.get(i), uniqueupdatekey);

		}
		this.updatemousehandler = updatemousehandler;
	}

	/**
	 * generate a tree table view
	 * 
	 * @param actionmanager action manager
	 * @return a tree table view model for the given column model
	 */
	public TreeTableView<ObjectDataElt> generateTreeTableViewModel(PageActionManager actionmanager) {
		// initializes the tree table
		TreeTableView<ObjectDataElt> treetable = new TreeTableView<ObjectDataElt>();
		double totalwidth = 0;
		boolean firsttreecolumn = true;
		for (int i = 0; i < arraycolumns.size(); i++) {
			CBusinessField<?> thisfield = arraycolumns.get(i);
			if (!thisfield.isShowinbottomnotes()) {

				String actionkeyforupdate = updateactionfields.get(thisfield.getFieldname());
				if (actionkeyforupdate != null) {
					logger.info("setting the column " + thisfield.getFieldname() + " to readwrite for action "
							+ actionkeyforupdate);
				}
				TreeTableColumn<
						ObjectDataElt, ?> thiscolumn = thisfield.getTreeTableColumn(actionmanager, actionkeyforupdate);
				if (firsttreecolumn) {
					thiscolumn.setMinWidth(thiscolumn.getMinWidth() + 50);
					firsttreecolumn = false;
				}
				totalwidth += thiscolumn.getMinWidth();
				treetable.getColumns().add(thiscolumn);
				thiscolumn.widthProperty().addListener(new ChangeListener<Number>() {

					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue,
							Number newValue) {
						double currentwidth = treetable.getWidth();
						double minwidth = treetable.getMinWidth();
						double extrawidth = newValue.doubleValue() - oldValue.doubleValue();

						treetable.setPrefWidth(currentwidth + extrawidth);
						treetable.setMinWidth(minwidth + extrawidth);

					}

				});
			} else {
				columnstoshowintooltip.add(thisfield);
			}
			treetable.setRowFactory(tv -> new TreeTableRow<ObjectDataElt>() {
				private Tooltip tooltip = new Tooltip();

				@Override
				public void updateItem(ObjectDataElt object, boolean empty) {
					super.updateItem(object, empty);
					if (object == null) {
						setTooltip(null);
					} else {
						tooltip.setText(generateTextForTooltip(columnstoshowintooltip, object));
						this.setTooltip(tooltip);
					}
				}

			});
		}
		// this is a hack to compensate for potential right scrollbar
		treetable.setMinWidth(totalwidth + 14);
		treetable.setPrefWidth(totalwidth + 14);
		return treetable;
	}

	/**
	 * generate a table view
	 * 
	 * @param actionmanager   page action manager
	 * @param forcedrowheight forced row heigh if needed (default 1)
	 * @return a table view
	 */
	public TableView<ObjectTableRow> generateTableViewModel(PageActionManager actionmanager, int forcedrowheight) {

		TableView<ObjectTableRow> returntable = new TableView<ObjectTableRow>();

		// -------------------- compute row height ---------------------

		int defaultrowheight = 1;

		if (forcedrowheight == 0) {
			// no row height specified on table, compute it according to column width
			for (int i = 0; i < arraycolumns.size(); i++) {
				CBusinessField<?> thisfield = arraycolumns.get(i);
				int thiscolumnrowheight = thisfield.getPreferredTableRowHeight();
				if (thiscolumnrowheight > defaultrowheight)
					defaultrowheight = thiscolumnrowheight;
			}
		} else {
			// forced row height specified on table, use it
			defaultrowheight = forcedrowheight;
		}
		final int finalpreferedrowheight = defaultrowheight;
		this.finalheightinpixel = finalpreferedrowheight * 15 + 14;

		// -------------------- generate columns ----------------------
		double totalwidth = 0;
		for (int i = 0; i < arraycolumns.size(); i++) {
			CBusinessField<?> thisfield = arraycolumns.get(i);

			if (!thisfield.isShowinbottomnotes()) {
				String actionkeyforupdate = updateactionfields.get(thisfield.getFieldname());
				TableColumn<ObjectTableRow, ?> thiscolumn = thisfield.getTableColumn(actionmanager,
						(finalpreferedrowheight > 1 ? true : false), finalpreferedrowheight, actionkeyforupdate);
				totalwidth += thiscolumn.getMinWidth();
				returntable.getColumns().add(thiscolumn);
				thiscolumn.widthProperty().addListener(new ChangeListener<Number>() {

					@Override
					public void changed(
							ObservableValue<? extends Number> observable,
							Number oldValue,
							Number newValue) {
						double currentwidth = returntable.getWidth();
						double minwidth = returntable.getMinWidth();
						double extrawidth = newValue.doubleValue() - oldValue.doubleValue();

						returntable.setPrefWidth(currentwidth + extrawidth);
						returntable.setMinWidth(minwidth + extrawidth);

					}

				});
			} else {
				columnstoshowintooltip.add(thisfield);
			}
		}

		returntable.setRowFactory(tv -> new EnhancedTableRow(updatemousehandler, finalheightinpixel));

		// this is a hack to compensate for potential right scrollbar
		returntable.setMinWidth(totalwidth + 14);
		returntable.setPrefWidth(totalwidth + 14);
		return returntable;

	}

	/**
	 * an utility class to manage table rows for table views with an update
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	class EnhancedTableRow
			extends
			TableRow<ObjectTableRow> {

		@SuppressWarnings("unused")
		private UpdateMouseHandler updatemousehandler;
		private double finalheightinpixel;

		/**
		 * creates an enhanced table row
		 * 
		 * @param updatemousehandler
		 * @param finalheightinpixel height of the row in pixel
		 */
		public EnhancedTableRow(UpdateMouseHandler updatemousehandler, int finalheightinpixel) {
			super();
			this.updatemousehandler = updatemousehandler;
			this.finalheightinpixel = finalheightinpixel;
			if (updatemousehandler != null)
				this.setOnMouseClicked(updatemousehandler);
		}

		@Override
		public void updateItem(ObjectTableRow object, boolean empty) {
			super.updateItem(object, empty);

			logger.finer("   --- final row height set to pixelheight =  " + finalheightinpixel + "---");
			this.setMaxHeight(finalheightinpixel);
			this.setMinHeight(finalheightinpixel);
			this.setPrefHeight(finalheightinpixel);
			this.setTextOverrun(OverrunStyle.ELLIPSIS);
			this.setEllipsisString("...");

		}
	}

	/**
	 * @return the final row height in pixel
	 */
	public int getFinalRowHeightInPixel() {
		return this.finalheightinpixel;
	}

	/**
	 * generates the text for tooltip
	 * 
	 * @param selectedfields list of busines fields
	 * @param object         object table row
	 * @return text for tooltip
	 */
	public static String generateTextForTooltip(ArrayList<CBusinessField<?>> selectedfields, ObjectTableRow object) {
		String answer = "";
		for (int i = 0; i < selectedfields.size(); i++) {
			CBusinessField<?> thisfield = selectedfields.get(i);
			if (i > 0)
				answer = answer + ",";
			answer = answer + thisfield.getLabel();
			answer = answer + ":";
			answer = answer + object.getFieldRepresentation(thisfield.getFieldname());
		}
		return answer;
	}

	/**
	 * generates the text for tooltip
	 * 
	 * @param selectedfields list of busines fields
	 * @param object         object data element
	 * @return text for tooltip
	 */
	public static String generateTextForTooltip(ArrayList<CBusinessField<?>> selectedfields, ObjectDataElt object) {
		String answer = "";
		for (int i = 0; i < selectedfields.size(); i++) {
			CBusinessField<?> thisfield = selectedfields.get(i);
			if (i > 0)
				answer = answer + ",";
			answer = answer + thisfield.getLabel();
			answer = answer + ":";
			answer = answer + object.lookupEltByName(thisfield.getFieldname()).defaultTextRepresentation();
		}
		return answer;
	}
}
