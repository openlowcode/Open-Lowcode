/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget.table;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.client.graphic.widget.fields.FormatValidator;

import com.sun.javafx.scene.control.skin.TreeTableViewSkin;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableColumn.CellDataFeatures;
import javafx.scene.control.TreeTableView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.TreeTablePosition;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * A widget allowing to display data in a tree, with the payload objects being
 * regrouped depending on some values on the payload objects.<br>
 * Edition is also managed, with possibility to show sums or averages per
 * category
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> payload object managed in the editable tree table
 */
public class EditableTreeTable<E extends Object> {

	private static Logger logger = Logger.getLogger(EditableTreeTable.class.getName());

	private static Method columnToFitMethod;

	static {
		try {
			columnToFitMethod = TreeTableViewSkin.class.getDeclaredMethod("resizeColumnToFitContent",
					TreeTableColumn.class, int.class);
			columnToFitMethod.setAccessible(true);
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}

	private TreeTableView<EditableTreeTableLineItem<Wrapper<E>>> treetableview;
	private ArrayList<LineGrouping<E, ?>> linegroupings;
	private ArrayList<ColumnGroup<E>> columngroups;
	/**
	 * show nothing for grouping lines and columns
	 */
	public static final int GROUPING_NOTHING = 0;
	/**
	 * show the sum of numeric values (int, float and BigDecimal)
	 */
	public static final int GROUPING_SUM = 1;
	/**
	 * average for int, float, and BigDecimal
	 */
	public static final int GROUPING_AVERAGE = 2;
	/**
	 * shows value if all are the same, else show nothing
	 */
	public static final int GROUPING_SAME = 3;
	/**
	 * shows the first value in elements below
	 */
	public static final int GROUPING_FIRST = 4;

	private ArrayList<E> payload;
	private ArrayList<Wrapper<E>> wrappedpayload;
	private TreeItem<EditableTreeTableLineItem<Wrapper<E>>> treeroot;

	private boolean defaultisreadonly = false;

	private EventHandler<MouseEvent> readonlyactioneventhandler;

	private boolean fireonlyifoneitem;

	/**
	 * @return true if by default, the tree table is read-only
	 */
	public boolean defaultIsReadOnly() {
		return this.defaultisreadonly;
	}

	/**
	 * @param defaultisreadonly sets if by default, the tree table is read-only
	 */
	public void setDefaultIsReadOnly(boolean defaultisreadonly) {
		this.defaultisreadonly = defaultisreadonly;
	}

	/**
	 * Creates an editable tree table
	 * 
	 * @param payloadtable array of payload objects
	 */
	public EditableTreeTable(E[] payloadtable) {
		this.payload = new ArrayList<E>();
		this.wrappedpayload = new ArrayList<Wrapper<E>>();
		if (payloadtable != null)
			for (int i = 0; i < payloadtable.length; i++) {
				payload.add(payloadtable[i]);
				wrappedpayload.add(new Wrapper<E>(payloadtable[i]));
			}
		linegroupings = new ArrayList<LineGrouping<E, ?>>();
		columngroups = new ArrayList<ColumnGroup<E>>();
	}

	/**
	 * Creates an editable tree table
	 * 
	 * @param payload a list of payload objects. Note that the list will be cloned
	 */
	@SuppressWarnings("unchecked")
	public EditableTreeTable(ArrayList<E> payload) {
		this.payload = (ArrayList<E>) payload.clone();
		this.wrappedpayload = new ArrayList<Wrapper<E>>();
		if (payload != null)
			for (int i = 0; i < payload.size(); i++)
				wrappedpayload.add(new Wrapper<E>(payload.get(i)));

		linegroupings = new ArrayList<LineGrouping<E, ?>>();
		columngroups = new ArrayList<ColumnGroup<E>>();
	}

	/**
	 * @return the list of all updated items
	 */
	public List<E> getUpdatedItems() {
		ArrayList<E> updateditems = new ArrayList<E>();
		for (int i = 0; i < this.wrappedpayload.size(); i++) {
			Wrapper<E> item = this.wrappedpayload.get(i);
			if (item.isUpdated())
				updateditems.add(item.getPayload());

		}
		return updateditems;
	}
	
	public void unMarkUpdatedItems() {
		for (int i = 0; i < this.wrappedpayload.size(); i++) {
			Wrapper<E> item = this.wrappedpayload.get(i);
			item.setNotUpdated();
		}
	}

	/**
	 * defines a line grouping based on the value of a given field of the payload
	 * object. Key is used for classification, and label is used for display
	 * 
	 * @param extractfield  function to extra field
	 * @param generatekey   generates the key from the field
	 * @param generatelabel generated the label from the field
	 */
	public <F extends Object> void setLineGrouping(
			Function<E, F> extractfield,
			Function<F, String> generatekey,
			Function<F, String> generatelabel) {
		linegroupings.add(new LineGrouping<E, F>(extractfield, generatekey, generatelabel));
	}

	/**
	 * defines a line grouping based on the value of a given field of the payload
	 * object. Key is used for classification, and label is used for display
	 * 
	 * @param keyextractor extractor for the column grouping
	 */
	public <F extends Object> void setLineGrouping(ObjectDataElementKeyExtractor<E, F> keyextractor) {
		setLineGrouping(keyextractor.fieldExtractor(), keyextractor.keyExtractor(), keyextractor.labelExtractor());
	}

	/**
	 * defines one line grouping per object.
	 * 
	 * @param generatelabel function to generate label per object
	 */
	public void setLineObject(Function<E, String> generatelabel) {
		linegroupings.add(new LineGrouping<E, E>((a) -> (a), null, generatelabel));
	}

	/**
	 * generates a group of columns that allows to classify payload object per a
	 * title argument per column and uses another field as payload. This is similar
	 * to pivot table in spreadsheet
	 * 
	 * @param extracttitle        extract the title field of the payload object
	 * @param generatetitlekey    generates key from the payload object
	 * @param generatetitlelabel  generates label from the payload object
	 * @param payloadextractor    extracts the payload object
	 * @param payloadintegration  function to brings back a modified payload to the
	 *                            object
	 * @param columngroupinglabel label for the column providing a summary of all
	 *                            columns. If null, no summary column
	 * @param grouping            type of grouping (as a static int in this label
	 * @param operator            an operator for the payload field providing parse,
	 *                            sum, and divide function
	 * @param formatvalidator
	 * @since 1.8
	 */

	public <F, G> void setColumnGrouping(
			Function<E, F> extracttitle,
			Function<F, String> generatetitlekey,
			Function<F, String> generatetitlelabel,
			Function<F, Boolean> horizontalconsolidationexception,
			Function<E, G> payloadextractor,
			BiConsumer<E, G> payloadintegration,
			String columngroupinglabel,
			int grouping,
			Operator<G> operator,
			FormatValidator<G> formatvalidator) {
		ColumnGrouping<
				E, F,
				G> columngrouping = new ColumnGrouping<E, F, G>(extracttitle, generatetitlekey, generatetitlelabel,
						horizontalconsolidationexception, payloadextractor, payloadintegration, columngroupinglabel,
						grouping, operator, formatvalidator);
		this.columngroups.add(columngrouping);
	}

	/**
	 * generates a group of columns that allows to classify payload object per a
	 * title argument per column and uses another field as payload. This is similar
	 * to pivot table in spreadsheet
	 * 
	 * @param titleextractor      extractor for title column
	 * @param payloadvalueupdater updater for payload column
	 * @param columngroupinglabel label for grouping column (if it exists)
	 * @param grouping            grouping method (sum, average...)
	 */
	public <F, G> void setColumnGrouping(
			ObjectDataElementKeyExtractor<E, F> titleextractor,
			ObjectDataElementValueUpdater<E, G> payloadvalueupdater,
			String columngroupinglabel,
			int grouping) {
		setColumnGrouping(titleextractor.fieldExtractor(), titleextractor.keyExtractor(),
				titleextractor.labelExtractor(), titleextractor.HorizontalSumException(),
				payloadvalueupdater.fieldExtractor(), payloadvalueupdater.payloadIntegration(), columngroupinglabel,
				grouping, payloadvalueupdater.operator(), payloadvalueupdater.formatValidator());
	}

	/**
	 * A read-only column field
	 * 
	 * @param title        title of the column
	 * @param keyextractor extractor of the field
	 * @param grouping     grouping criteria as a static int in the class
	 */
	public <G> void setColumnReadOnlyField(
			String title,
			ObjectDataElementKeyExtractor<E, G> keyextractor,
			int grouping) {
		setColumnReadOnlyField(title, keyextractor.fieldExtractor(), keyextractor.labelExtractor(),
				keyextractor.keyExtractor(), grouping);
	}

	/**
	 * A read-only column field
	 * 
	 * @param title        title of the column
	 * @param keyextractor extractor of the field
	 * @param grouping     grouping criteria as a static int in the class
	 * @param keyexception values to discard in the consolidation
	 */
	public <G> void setColumnReadOnlyField(
			String title,
			ObjectDataElementKeyExtractor<E, G> keyextractor,
			int grouping,
			String keyexception) {
		setColumnReadOnlyField(title, keyextractor.fieldExtractor(), keyextractor.labelExtractor(),
				keyextractor.keyExtractor(), grouping, keyexception);
	}

	/**
	 * A read-only column field
	 * 
	 * @param title            title of the column
	 * @param payloadextractor extractor of the field from the payload
	 * @param displaygenerator generates the display value from the field
	 * @param keygenerator     generates the key value from the field
	 * @param grouping         grouping criteria as a static int in the class
	 */
	public <G> void setColumnReadOnlyField(
			String title,
			Function<E, G> payloadextractor,
			Function<G, String> displaygenerator,
			Function<G, String> keygenerator,
			int grouping) {
		ReadOnlyColumn<
				G> column = new ReadOnlyColumn<G>(title, payloadextractor, displaygenerator, keygenerator, grouping);
		this.columngroups.add(column);
	}

	/**
	 * A read-only column field
	 * 
	 * @param title            title of the column
	 * @param payloadextractor extractor of the field from the payload
	 * @param displaygenerator generates the display value from the field
	 * @param keygenerator     generates the key value from the field
	 * @param grouping         grouping criteria as a static int in the class
	 * @param keyexception     values to discard in the consolidation
	 */
	public <G> void setColumnReadOnlyField(
			String title,
			Function<E, G> payloadextractor,
			Function<G, String> displaygenerator,
			Function<G, String> keygenerator,
			int grouping,
			String keyexception) {
		ReadOnlyColumn<G> column = new ReadOnlyColumn<G>(title, payloadextractor, displaygenerator, keygenerator,
				grouping, keyexception);
		this.columngroups.add(column);
	}

	/**
	 * Fires the related event handler on double click on read-only mode of the
	 * widget
	 * 
	 * @param readonlyactioneventhandler event handler to fire
	 * @param fireonlyifoneitem          if true, the event if fired only if the
	 *                                   related cell links to only one element
	 */
	public void setDoubleClickReadOnlyEventHandler(
			EventHandler<MouseEvent> readonlyactioneventhandler,
			boolean fireonlyifoneitem) {
		if (treetableview != null)
			throw new RuntimeException("Method needs to be called before call to getNode()");
		this.readonlyactioneventhandler = readonlyactioneventhandler;
		this.fireonlyifoneitem = fireonlyifoneitem;
	}

	/**
	 * @return the elements corresponding to the active cell
	 */
	public List<E> getSelectedElements() {
		ObservableList<TreeTablePosition<EditableTreeTableLineItem<Wrapper<E>>, ?>> selectedcells = treetableview
				.getSelectionModel().getSelectedCells();
		if (selectedcells.size() == 1) {
			TreeTablePosition<EditableTreeTableLineItem<Wrapper<E>>, ?> selectedposition = selectedcells.get(0);
			TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?> tablecolumn = selectedposition.getTableColumn();
			if (tablecolumn instanceof EditableTreeTableValueColumn) {
				@SuppressWarnings({ "unchecked", "rawtypes" })
				EditableTreeTableValueColumn<E, ?, ?> tablecolumnparsed = (EditableTreeTableValueColumn) tablecolumn;
				TreeItem<EditableTreeTableLineItem<Wrapper<E>>> treeitem = selectedposition.getTreeItem();
				EditableTreeTableLineItem<Wrapper<E>> rowdata = treeitem.getValue();
				ArrayList<Wrapper<E>> filteredvalues = tablecolumnparsed.columngrouping.filterItems(rowdata,
						tablecolumnparsed.titlekey);
				ArrayList<E> returnedvalues = new ArrayList<E>();
				for (int i = 0; i < filteredvalues.size(); i++) {
					returnedvalues.add(filteredvalues.get(i).getPayload());
				}
				return returnedvalues;
			}
		}
		return new ArrayList<E>();
	}

	/**
	 * @return the tree table view
	 */
	public Node getNode() {

		treetableview = new TreeTableView<EditableTreeTableLineItem<Wrapper<E>>>();
		treetableview.setEditable(true);
		treetableview.getSelectionModel().setCellSelectionEnabled(true);
		generateTree();
		generateRootColumn();
		generateEditionListener();
		for (int i = 0; i < this.columngroups.size(); i++) {

			ColumnGroup<E> thiscolumngroup = this.columngroups.get(i);

			thiscolumngroup.preProcess(payload);
			List<TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?>> columnsforgroup = thiscolumngroup
					.generateColumns();
			logger.finest(" ---- Processing columngroup " + i + ", class = " + thiscolumngroup.getClass().getName()
					+ " columncount=" + (columnsforgroup != null ? columnsforgroup.size() : "null"));
			if (columnsforgroup != null)
				for (int j = 0; j < columnsforgroup.size(); j++) {
					TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?> column = columnsforgroup.get(j);
					logger.finest("   ----> processing column " + j + " inside columngroup, class = "
							+ column.getClass() + ", title = " + column.getText());
					treetableview.getColumns().add(column);
				}
		}
		expandall(treetableview.getRoot(), 0);
		resize();
		if (this.defaultisreadonly)
			treetableview.setEditable(false);
		treetableview.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				if (treetableview.isEditable()) {
					logger.fine("Sink event as editable mode");
				} else {
					if (event.getClickCount() > 1 && (event.getButton().equals(MouseButton.PRIMARY))) {
						logger.fine(" >>> Proper Event");
						if (readonlyactioneventhandler != null) {

							ObservableList<
									TreeTablePosition<
											EditableTreeTableLineItem<Wrapper<E>>,
											?>> selectedcells = treetableview.getSelectionModel().getSelectedCells();
							if (selectedcells.size() == 1) {
								TreeTablePosition<
										EditableTreeTableLineItem<Wrapper<E>>,
										?> selectedposition = selectedcells.get(0);
								TreeTableColumn<
										EditableTreeTableLineItem<Wrapper<E>>,
										?> tablecolumn = selectedposition.getTableColumn();
								if (tablecolumn instanceof EditableTreeTableValueColumn) {
									if (fireonlyifoneitem) {
										@SuppressWarnings({ "unchecked", "rawtypes" })
										EditableTreeTableValueColumn<
												E, ?, ?> tablecolumnparsed = (EditableTreeTableValueColumn) tablecolumn;
										TreeItem<EditableTreeTableLineItem<Wrapper<E>>> treeitem = selectedposition
												.getTreeItem();
										EditableTreeTableLineItem<Wrapper<E>> rowdata = treeitem.getValue();
										ArrayList<Wrapper<E>> filteredvalues = tablecolumnparsed.columngrouping
												.filterItems(rowdata, tablecolumnparsed.titlekey);
										if (filteredvalues.size() == 1)
											readonlyactioneventhandler.handle(event);

									} else {
										readonlyactioneventhandler.handle(event);
									}

								}
							}
						}
					}

				}
			}
		});
		return treetableview;
	}

	/**
	 * sets the field to editable after display
	 * 
	 * @param editable true to make the tree table editable, false to make the tree
	 *                 table not editable
	 */
	public void setEditable(boolean editable) {
		treetableview.setEditable(editable);
	}

	private void resize() {
		Platform.runLater(() -> {
			for (Object column : treetableview.getColumns()) {
				try {
					columnToFitMethod.invoke(treetableview.getSkin(), column, -1);
				} catch (Exception e) {
					logger.finest("Exception during autoresize of columns " + e.getClass() + " - " + e.getMessage());

				}
			}
		});
		
		ObservableList<TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?>> columns = treetableview.getColumns();
		for (int i=0;i<columns.size();i++) {
			columns.get(i).widthProperty().addListener(new  ChangeListener<Number>() {

				@Override
				public void changed(ObservableValue<? extends Number> arg0, Number arg1, Number arg2) {
					int totalwidth = 0;
					for (int j=0;j<columns.size();j++) {
						logger.severe("      column "+j+" width = "+columns.get(j).getWidth());
						totalwidth+= columns.get(j).getWidth();
					}
					totalwidth+=14;
					logger.severe("       ---------------------------> Total width setup to "+totalwidth+" points");
					treetableview.setMinWidth(totalwidth);
					treetableview.setPrefWidth(totalwidth);
				}
				
			});
			
		}
		
		
		
		
	}

	private void expandall(TreeItem<EditableTreeTableLineItem<Wrapper<E>>> thisitem, int circuitbreaker) {
		if (circuitbreaker > 1024)
			throw new RuntimeException("Circuit Breaker on ");
		thisitem.setExpanded(true);
		Iterator<TreeItem<EditableTreeTableLineItem<Wrapper<E>>>> childreniterator = thisitem.getChildren().iterator();
		while (childreniterator.hasNext()) {
			TreeItem<EditableTreeTableLineItem<Wrapper<E>>> childnode = childreniterator.next();
			expandall(childnode, circuitbreaker + 1);
		}
	}

	/**
	 * generates a listener to manage
	 * <ul>
	 * <li>on key pressed, edition starts</li>
	 * <li>delete removes content from the cell</li>
	 * </ul>
	 */
	private void generateEditionListener() {
		treetableview.setOnKeyPressed(new EventHandler<KeyEvent>() {

			@Override
			public void handle(KeyEvent event) {
				KeyCode keyCode = event.getCode();
				logger.finest("   > Key event on table " + keyCode);

				TreeTablePosition<
						EditableTreeTableLineItem<Wrapper<E>>,
						?> focusedcell = treetableview.getFocusModel().getFocusedCell();
				if (treetableview.getEditingCell() != null) {
					logger.finest("    >>Discard event as editing cell active");
				} else {

					if (keyCode == KeyCode.DELETE || keyCode == KeyCode.BACK_SPACE) {
						logger.finest("     > Delete detected ");
						TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?> column = focusedcell.getTableColumn();
						if (column instanceof EditableTreeTableValueColumn) {
							@SuppressWarnings("unchecked")
							EditableTreeTableValueColumn<
									E, ?, ?> parsedcolumn = (EditableTreeTableValueColumn<E, ?, ?>) column;
							TreeItem<EditableTreeTableLineItem<Wrapper<E>>> treeitem = focusedcell.getTreeItem();
							parsedcolumn.clear(treeitem.getValue());
						}

					}
					if (keyCode.isLetterKey() || keyCode.isDigitKey() || keyCode == KeyCode.DECIMAL
							|| keyCode == KeyCode.SPACE) {
						logger.finest("     > Detected real content");
						treetableview.edit(focusedcell.getRow(), focusedcell.getTableColumn());

					}
				}
			}

		});
	}

	/**
	 * generates the root column showing label of line elements
	 */
	private void generateRootColumn() {

		TreeTableColumn<
				EditableTreeTableLineItem<Wrapper<E>>,
				String> rootcolumn = new TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, String>("Item");
		rootcolumn.setCellValueFactory(new Callback<
				CellDataFeatures<EditableTreeTableLineItem<Wrapper<E>>, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<EditableTreeTableLineItem<Wrapper<E>>, String> param) {
				return new SimpleStringProperty(param.getValue().getValue().getLabel());
			}
		});

		treetableview.getColumns().add(rootcolumn);
	}

	/**
	 * generates the tree structure
	 */
	private void generateTree() {

		EditableTreeTableLineItem<
				Wrapper<E>> rootitem = new EditableTreeTableLineItem<Wrapper<E>>("Root", wrappedpayload);
		treeroot = new TreeItem<EditableTreeTableLineItem<Wrapper<E>>>(rootitem);
		generateSubTree(this.linegroupings.get(0), treeroot, 1);
		generateLeavesCount(treeroot, 0);
		consolidateTree(treeroot,0);
		treetableview.setRoot(this.treeroot);
	}

	private int generateLeavesCount(TreeItem<EditableTreeTableLineItem<Wrapper<E>>> item,int currentlevel) {
		if (currentlevel>1024) throw new RuntimeException("Recursive circuit breaker");
		if (item.getChildren().size()==0) {
			item.getValue().setNumberofleaves(1);
			return 1;
		}
		int count =0;
		for (int i=0;i<item.getChildren().size();i++) {
			count+=generateLeavesCount(item.getChildren().get(i),currentlevel+1);
		}
		item.getValue().setNumberofleaves(count);
		return count;
	}
	
	private void consolidateTree(TreeItem<EditableTreeTableLineItem<Wrapper<E>>> item,int currentlevel) {
		if (currentlevel>1024) throw new RuntimeException("Recursive circuit breaker");
		// if several items, do not simplidy at this level
		
		if (item.getValue().getNumberofleaves()>1) {
			logger.severe(" Item "+item.getValue().getLabel()+" has several children.");
			for (int i=0;i<item.getChildren().size();i++) {
				consolidateTree(item.getChildren().get(i),currentlevel+1);
			}
		}
		// one item, if children, cut them
		if (item.getValue().getNumberofleaves()==1) {
			if (item.getChildren().size()==1) {
				logger.severe(" Item "+item.getValue().getLabel()+"has only one data and one child, clear.");
				String extralabel = consolidatelowerlabels(item.getChildren().get(0),0);
				String newlabel = item.getValue().getLabel()+" "+extralabel;
				if (currentlevel==0) newlabel = extralabel;
				item.getValue().updateLabel(newlabel);
				item.getChildren().clear();
			} else {
				logger.severe(" Item "+item.getValue().getLabel()+"has only one data and no child, do nothing");
			}
		}
	}
	
	private String consolidatelowerlabels(TreeItem<EditableTreeTableLineItem<Wrapper<E>>> item,int recursivebreaker) {
		if (recursivebreaker>1024) throw new RuntimeException("Recursive circuit breaker");
		if (item.getChildren().size()==0) return item.getValue().getLabel();
		return item.getValue().getLabel()+" "+consolidatelowerlabels(item.getChildren().get(0),recursivebreaker+1);
	}
	
	/**
	 * Recursive structure to define the subtree
	 * 
	 * @param thislinegrouping      line grouping being processed
	 * @param subtree               subtree to process
	 * @param nextlinegroupingindex index in the list of line groupings
	 */
	private <F> void generateSubTree(
			LineGrouping<E, F> thislinegrouping,
			TreeItem<EditableTreeTableLineItem<Wrapper<E>>> subtree,
			int nextlinegroupingindex) {
		EditableTreeTableLineItem<Wrapper<E>> currentnode = subtree.getValue();
		logger.finest("Starting generate subtree for node " + subtree.getValue().getLabel() + " linegroupindex ="
				+ nextlinegroupingindex + " parent = "
				+ (subtree.getParent() != null ? subtree.getParent().getValue().getLabel() : ""));
		if (thislinegrouping.noGrouping()) {
			// generate one node per child
			for (int i = 0; i < currentnode.getItemsNumber(); i++) {
				Wrapper<E> item = currentnode.getItemAt(i);
				ArrayList<Wrapper<E>> singleitem = new ArrayList<Wrapper<E>>();
				singleitem.add(item);
				String label = thislinegrouping.generatelabel
						.apply(thislinegrouping.getExtractfield().apply(item.getPayload()));
				EditableTreeTableLineItem<
						Wrapper<E>> finalnode = new EditableTreeTableLineItem<Wrapper<E>>(label, singleitem);

				TreeItem<EditableTreeTableLineItem<Wrapper<E>>> singlenode = new TreeItem<
						EditableTreeTableLineItem<Wrapper<E>>>(finalnode);

				subtree.getChildren().add(singlenode);
			}
		} else {

			HashMap<String, ArrayList<Wrapper<E>>> groupedchildren = new HashMap<String, ArrayList<Wrapper<E>>>();
			HashMap<String, String> childrenlabel = new HashMap<String, String>();
			ArrayList<String> keys = new ArrayList<String>();

			// generates keys

			for (int i = 0; i < currentnode.getItemsNumber(); i++) {
				Wrapper<E> item = currentnode.getItemAt(i);
				F element = thislinegrouping.extractfield.apply(item.getPayload());
				String key = null;
				if (element != null)
					key = thislinegrouping.generatekey.apply(element);
				ArrayList<Wrapper<E>> childrenforkey = groupedchildren.get(key);
				if (childrenforkey == null) {
					childrenforkey = new ArrayList<Wrapper<E>>();
					groupedchildren.put(key, childrenforkey);
					keys.add(key);
					if (element == null) {
						childrenlabel.put(null, "Unclassified");
					} else {
						childrenlabel.put(key, thislinegrouping.getGeneratelabel().apply(element));
					}
				}
				childrenforkey.add(item);
			}
			for (int i = 0; i < keys.size(); i++) {
				String key = keys.get(i);
				ArrayList<Wrapper<E>> children = groupedchildren.get(key);
				EditableTreeTableLineItem<Wrapper<E>> nodelineitem = new EditableTreeTableLineItem<Wrapper<E>>(
						childrenlabel.get(key), children);
				TreeItem<EditableTreeTableLineItem<Wrapper<E>>> nodetreeitem = new TreeItem<
						EditableTreeTableLineItem<Wrapper<E>>>(nodelineitem);
				subtree.getChildren().add(nodetreeitem);
				if (nextlinegroupingindex < this.linegroupings.size()) {
					generateSubTree(this.linegroupings.get(nextlinegroupingindex), nodetreeitem,
							nextlinegroupingindex + 1);
				}
			}

		}

	}

	/**
	 * A read-write column used in the tree table
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> class of the payload object
	 * @param <F> class of the column grouping field in the payload object
	 * @param <G> class of the line grouping field in the payload object
	 */
	private static class EditableTreeTableValueColumn<E extends Object, F extends Object, G extends Object>
			extends
			TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, String> {
		private ColumnGrouping<E, F, G> columngrouping;
		private String titlekey;

		public boolean isEditable(EditableTreeTableLineItem<Wrapper<E>> lineitem) {
			ArrayList<Wrapper<E>> filteredvalues = columngrouping.filterItems(lineitem, titlekey);
			if (filteredvalues.size() == 1)
				return true;
			return false;
		}

		public void clear(EditableTreeTableLineItem<Wrapper<E>> lineitem) {
			ArrayList<Wrapper<E>> filteredvalues = columngrouping.filterItems(lineitem, titlekey);
			if (filteredvalues.size() == 1) {
				columngrouping.payloadintegration.accept(filteredvalues.get(0).getPayload(), null);
				filteredvalues.get(0).setUpdated();
				this.getTreeTableView().refresh();
			}
		}

		public EditableTreeTableValueColumn(
				ColumnGrouping<E, F, G> columngrouping,
				String titlekey,
				String titlelabel) {
			super(titlelabel);
			logger.finest("Creating column for key = " + titlekey);
			this.columngrouping = columngrouping;
			this.titlekey = titlekey;
			this.setOnEditCommit(new EventHandler<CellEditEvent<EditableTreeTableLineItem<Wrapper<E>>, String>>() {

				@Override
				public void handle(CellEditEvent<EditableTreeTableLineItem<Wrapper<E>>, String> item) {
					logger.finest(" got new update event " + item.getOldValue() + "-->" + item.getNewValue());
					EditableTreeTableLineItem<Wrapper<E>> row = item.getRowValue().getValue();

					ArrayList<Wrapper<E>> filteredvalues = columngrouping.filterItems(row, titlekey);

					if (filteredvalues.size() == 1) {
						if (item.getNewValue() == null) {
							if (item.getOldValue() != null) {
								columngrouping.payloadintegration.accept(filteredvalues.get(0).getPayload(), null);
								filteredvalues.get(0).setUpdated();
								item.getTableColumn().getTreeTableView().refresh();

								logger.finest("   >>> put item to null");
							}

						} else {
							if (!item.getNewValue().equals(item.getOldValue())) {
								G value = columngrouping.formatvalidator.parse(item.getNewValue());
								columngrouping.payloadintegration.accept(filteredvalues.get(0).getPayload(), value);
								filteredvalues.get(0).setUpdated();
								item.getTableColumn().getTreeTableView().refresh();
								logger.finest("   >>> put item to new value (" + value + ") -- "
										+ filteredvalues.get(0).toString());
							}
						}
					}

				}
			});
			this.setCellFactory(col -> {
				LargeTextTreeTableCell<
						EditableTreeTableLineItem<Wrapper<E>>,
						String> cell = new LargeTextTreeTableCell<EditableTreeTableLineItem<Wrapper<E>>, String>(
								IDENTICAL_CONVERTER, columngrouping.formatvalidator, null, (a) -> {
									logger.finest("Inside read-only criteria");
									if (EditableTreeTableValueColumn.this.isEditable(a)) return new Boolean(false);
									return new Boolean(true);
								}, false, true, 1);

				return cell;
			});

			this.setCellValueFactory(new Callback<
					CellDataFeatures<EditableTreeTableLineItem<Wrapper<E>>, String>, ObservableValue<String>>() {

				@Override
				public ObservableValue<String> call(
						CellDataFeatures<EditableTreeTableLineItem<Wrapper<E>>, String> param) {
					EditableTreeTableLineItem<Wrapper<E>> value = param.getValue().getValue();
					ArrayList<Wrapper<E>> filteredvalues = columngrouping.filterItems(value, titlekey);

					logger.finest("      -->filtered elements " + filteredvalues.size() + ", out of "
							+ value.getItemsNumber() + " for key " + titlekey);
					if (filteredvalues.size() == 1) {
						E object = filteredvalues.get(0).getPayload();
						G payload = columngrouping.payloadextractor.apply(object);
						if (payload == null)
							return null;
						return new SimpleStringProperty(columngrouping.formatvalidator.print(payload));
					}
					if (filteredvalues.size() > 1) {
						if (columngrouping.grouping == EditableTreeTable.GROUPING_FIRST) {
							E object = filteredvalues.get(0).getPayload();
							G payload = columngrouping.payloadextractor.apply(object);
							if (payload == null)
								return null;
							return new SimpleStringProperty(columngrouping.formatvalidator.print(payload));
						}
						if (columngrouping.grouping == EditableTreeTable.GROUPING_SUM) {
							G summedpayload = null;
							for (int i = 0; i < filteredvalues.size(); i++) {
								E object = filteredvalues.get(i).getPayload();
								G thispayload = columngrouping.payloadextractor.apply(object);
								summedpayload = columngrouping.operator.add(thispayload, summedpayload);

							}
							if (summedpayload == null)
								return null;
							return new SimpleStringProperty(columngrouping.formatvalidator.print(summedpayload));
						}
					}
					return null;
				}
			});
		}

	}

	private static StringConverter<String> IDENTICAL_CONVERTER = new StringConverter<String>() {

		@Override
		public String fromString(String string) {
			return string;
		}

		@Override
		public String toString(String object) {
			return object;
		}

	};

	/**
	 * A column grouping managing in the style of spreadsheet pivot table display
	 * and update of data
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> payload object
	 * @param <F> class of the payload object field used as column grouping criteria
	 * @param <G> class of the payload object field used as line grouping criteria
	 */
	private static class ColumnGrouping<E extends Object, F extends Object, G extends Object>
			implements
			ColumnGroup<E> {
		private Function<E, F> extracttitle;
		private Function<F, String> generatetitlekey;
		private Function<F, String> generatetitlelabel;
		private Function<E, G> payloadextractor;
		private BiConsumer<E, G> payloadintegration;
		@SuppressWarnings("unused")
		private String columngroupinglabel;
		private int grouping;
		private ArrayList<F> alltitles;
		private Operator<G> operator;
		private FormatValidator<G> formatvalidator;
		private Function<F, Boolean> horizontalconsolidationexception;

		public ColumnGrouping(
				Function<E, F> extracttitle,
				Function<F, String> generatetitlekey,
				Function<F, String> generatetitlelabel,
				Function<F, Boolean> horizontalconsolidationexception,
				Function<E, G> payloadextractor,
				BiConsumer<E, G> payloadintegration,
				String columngroupinglabel,
				int grouping,
				Operator<G> operator,
				FormatValidator<G> formatvalidator) {
			super();
			this.extracttitle = extracttitle;
			this.generatetitlekey = generatetitlekey;
			this.generatetitlelabel = generatetitlelabel;
			this.horizontalconsolidationexception = horizontalconsolidationexception;
			this.payloadextractor = payloadextractor;
			this.payloadintegration = payloadintegration;
			this.columngroupinglabel = columngroupinglabel;
			this.grouping = grouping;
			this.operator = operator;
			this.formatvalidator = formatvalidator;
		}

		private ArrayList<Wrapper<E>> filterItems(EditableTreeTableLineItem<Wrapper<E>> row, String titlekey) {
			ArrayList<Wrapper<E>> filteredvalues = new ArrayList<Wrapper<E>>();
			for (int i = 0; i < row.getItemsNumber(); i++) {
				Wrapper<E> wrappeditem = row.getItemAt(i);

				F titlepayload = extracttitle.apply(wrappeditem.getPayload());
				String key = null;
				if (titlepayload != null)
					key = generatetitlekey.apply(titlepayload);
				logger.finest("     object key = " + key);
				if (key == null) {
					if (titlekey == null)
						filteredvalues.add(wrappeditem);
				} else {
					if (key.equals(titlekey))
						filteredvalues.add(wrappeditem);
				}
			}
			return filteredvalues;

		}

		@Override
		public List<TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?>> generateColumns() {
			ArrayList<
					TreeTableColumn<
							EditableTreeTableLineItem<Wrapper<E>>,
							?>> columns = new ArrayList<TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?>>();
			for (int i = 0; i < alltitles.size(); i++) {
				F currenttitle = alltitles.get(i);
				String titlestring = "Unspecified";
				String titlekey = null;
				if (currenttitle != null) {
					titlestring = generatetitlelabel.apply(currenttitle);
					titlekey = generatetitlekey.apply(currenttitle);
				}

				final String titlekeyfinal = titlekey;
				logger.finest("Currently treating multicolumn key, value " + titlekey + "," + titlestring + ", final="
						+ titlekeyfinal);
				TreeTableColumn<
						EditableTreeTableLineItem<Wrapper<E>>,
						String> currentcolumn = new EditableTreeTableValueColumn<E, F, G>(this, titlekeyfinal,
								titlestring);
				columns.add(currentcolumn);
			}
			// ---------- Create Total Column
			if (grouping == EditableTreeTable.GROUPING_SUM) {
				TreeTableColumn<
						EditableTreeTableLineItem<Wrapper<E>>,
						String> currentcolumn = new TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, String>(
								"Total");
				currentcolumn.setCellValueFactory(new Callback<
						CellDataFeatures<EditableTreeTableLineItem<Wrapper<E>>, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(
							CellDataFeatures<EditableTreeTableLineItem<Wrapper<E>>, String> param) {
						EditableTreeTableLineItem<Wrapper<E>> values = param.getValue().getValue();
						G summedpayload = null;
						for (int i = 0; i < values.getItemsNumber(); i++) {
							E object = values.getItemAt(i).getPayload();
							F columncriteria = extracttitle.apply(object);
							boolean exclude = false;
							if (columncriteria != null)
								exclude = horizontalconsolidationexception.apply(columncriteria).booleanValue();
							if (!exclude) {
								G thispayload = payloadextractor.apply(object);
								summedpayload = operator.add(thispayload, summedpayload);
							}
						}
						if (summedpayload == null)
							return null;
						return new SimpleStringProperty(formatvalidator.print(summedpayload));
					}
				});

				currentcolumn.setCellFactory(col -> {
					LargeTextTreeTableCell<
							EditableTreeTableLineItem<Wrapper<E>>,
							String> cell = new LargeTextTreeTableCell<EditableTreeTableLineItem<Wrapper<E>>, String>(
									IDENTICAL_CONVERTER, formatvalidator, null, false, true, 1) {
								@Override
								public void startEdit() {

									return;

								}

							};

					return cell;
				});

				columns.add(currentcolumn);
			}
			return columns;
		}

		@Override
		public void preProcess(List<E> alldata) {
			alltitles = new ArrayList<F>();
			HashMap<String, F> keymap = new HashMap<String, F>();
			if (alldata != null)
				for (int i = 0; i < alldata.size(); i++) {
					E data = alldata.get(i);
					F titlepayload = extracttitle.apply(data);
					String key = null;
					if (titlepayload != null)
						key = this.generatetitlekey.apply(titlepayload);
					if (titlepayload != null)
						if (keymap.get(key) == null) {
							alltitles.add(titlepayload);
							keymap.put(key, titlepayload);
						}
				}
			// if comparable, orders the columns
			boolean comparable = false;
			if (alltitles.size() > 0) {
				comparable = true;
				for (int i = 0; i < alltitles.size(); i++)
					if (!(alltitles.get(i) instanceof Comparable<?>))
						comparable = false;
			}
			if (comparable) {
				Collections.sort(alltitles, new Comparator<F>() {

					@Override
					public int compare(F o1, F o2) {
						@SuppressWarnings("unchecked")
						Comparable<F> o1comp = (Comparable<F>) o1;
						return o1comp.compareTo(o2);

					}
				});
			}
		}

	}

	/**
	 * A line element made of groupings based on the value of a field of the payload
	 * object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> payload object
	 * @param <F> class of the field being used
	 */
	private static class LineGrouping<E extends Object, F extends Object> {
		private Function<E, F> extractfield;
		private Function<F, String> generatekey;
		private Function<F, String> generatelabel;

		public Function<E, F> getExtractfield() {
			return extractfield;
		}

		@SuppressWarnings("unused")
		public Function<F, String> getGeneratekey() {
			return generatekey;
		}

		public Function<F, String> getGeneratelabel() {
			return generatelabel;
		}

		public LineGrouping(
				Function<E, F> extractfield,
				Function<F, String> generatekey,
				Function<F, String> generatelabel) {
			super();
			this.extractfield = extractfield;
			this.generatekey = generatekey;
			this.generatelabel = generatelabel;
		}

		public boolean noGrouping() {
			if (generatekey == null)
				return true;
			return false;
		}
	}

	/**
	 * a read-only column showing the value of a field of the object, consolidated
	 * if appropriate
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <G> payload object
	 */
	private class ReadOnlyColumn<G extends Object>
			implements
			ColumnGroup<E> {

		private String title;
		private Function<E, G> payloadextractor;
		private Function<G, String> displaygenerator;
		private Function<G, String> keyextractor;
		private int grouping;
		private String keyexception;

		public ReadOnlyColumn(
				String title,
				Function<E, G> payloadextractor,
				Function<G, String> displaygenerator,
				Function<G, String> keyextractor,
				int grouping) {
			this.title = title;
			this.payloadextractor = payloadextractor;
			this.displaygenerator = displaygenerator;
			this.keyextractor = keyextractor;
			this.grouping = grouping;
			this.keyexception = null;
		}

		public ReadOnlyColumn(
				String title,
				Function<E, G> payloadextractor,
				Function<G, String> displaygenerator,
				Function<G, String> keyextractor,
				int grouping,
				String keyexception) {
			this.title = title;
			this.payloadextractor = payloadextractor;
			this.displaygenerator = displaygenerator;
			this.keyextractor = keyextractor;
			this.grouping = grouping;
			this.keyexception = keyexception;
		}

		@Override
		public List<TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?>> generateColumns() {
			TreeTableColumn<
					EditableTreeTableLineItem<Wrapper<E>>,
					String> readonlycolumn = new TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, String>(title);
			readonlycolumn.setCellValueFactory(new Callback<
					CellDataFeatures<EditableTreeTableLineItem<Wrapper<E>>, String>, ObservableValue<String>>() {

				@Override
				public ObservableValue<String> call(
						CellDataFeatures<EditableTreeTableLineItem<Wrapper<E>>, String> param) {
					EditableTreeTableLineItem<Wrapper<E>> listofobjects = param.getValue().getValue();
					String result = null;
					if (grouping == GROUPING_NOTHING) {
						if (listofobjects.getItemsNumber() == 1) {
							E object = listofobjects.getItemAt(0).getPayload();
							G payload = payloadextractor.apply(object);
							if (payload != null)
								result = displaygenerator.apply(payload);
						}
					}
					if (grouping == GROUPING_SAME) {
						boolean burnt = false;
						logger.fine("Starting analysis for a cell -----------------------------");
						for (int i = 0; i < listofobjects.getItemsNumber(); i++) {
							E object = listofobjects.getItemAt(i).getPayload();
							G payload = payloadextractor.apply(object);

							String value = null;
							String key = null;
							if (payload != null) {
								value = displaygenerator.apply(payload);
								key = keyextractor.apply(payload);
								logger.fine("     analyzing element value '" + value + "', key '" + key
										+ "', valueexception = '" + keyexception + "'");
							}
							if (result == null)
								if (!burnt)
									if (key != null)
										if (!key.equals(keyexception)) {
											result = value;
											logger.fine("         put in value " + result + " as '" + key + "' <> '"
													+ keyexception + "'");
											logger.finest("     > found value " + value + " for " + object.toString());
										}
							if (result != null)
								if (!result.equals(value))
									if (key != null)
										if (!key.equals(keyexception)) {
											logger.fine("         value " + value + " burning " + result);
											burnt = true;
											result = null;

										}

						}
						logger.fine("          --> " + result + ", burnt = " + burnt);
					}
					if (grouping == GROUPING_FIRST) {
						if (listofobjects.getItemsNumber() >= 1) {
							E object = listofobjects.getItemAt(0).getPayload();
							G payload = payloadextractor.apply(object);
							if (payload != null)
								result = displaygenerator.apply(payload);
						}

					}
					logger.finest(" ----> RO " + result);
					return new SimpleStringProperty(result);
				}

			});
			ArrayList<
					TreeTableColumn<
							EditableTreeTableLineItem<Wrapper<E>>,
							?>> returnlist = new ArrayList<TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?>>();
			returnlist.add(readonlycolumn);
			return returnlist;
		}

		@Override
		public void preProcess(List<E> alldata) {
			// do nothing

		}

	}

	/**
	 * A column group that will generate one or several columns
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> object payload
	 */
	private interface ColumnGroup<E extends Object> {

		List<TreeTableColumn<EditableTreeTableLineItem<Wrapper<E>>, ?>> generateColumns();

		public void preProcess(List<E> alldata);
	}

	/**
	 * an interface to manage numeric values inside the tree table
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <G> the numeric value class (e.g. BigDecimal)
	 */
	public interface Operator<G extends Object> {
		/**
		 * @param element1 first element to sum
		 * @param element2 second element to sum
		 * @return the sum of two elements, managing null as zero
		 */
		public G add(G element1, G element2);

		/**
		 * perform a division by an integer
		 * 
		 * @param element1 element to divide
		 * @param element2 element used for division
		 * @return the division of two elements, if two elements are not null, null else
		 */
		public G divide(BigDecimal element1, BigDecimal element2);

	}

	/**
	 * An operator managing big decimal
	 */
	public static Operator<BigDecimal> BIGDECIMAL_OPERATOR = new Operator<BigDecimal>() {

		@Override
		public BigDecimal add(BigDecimal element1, BigDecimal element2) {
			if (element1 == null)
				return element2;
			if (element2 == null)
				return element1;
			return element1.add(element2);
		}

		@Override
		public BigDecimal divide(BigDecimal element1, BigDecimal element2) {
			if (element1 == null)
				return null;
			if (element2 == null)
				return null;
			return element1.divide(element2);
		}

	};

	/**
	 * A wrapper around an object, managing stored data inside this widget per
	 * object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> payload of the object
	 */
	private static class Wrapper<E extends Object> {
		private E payload;
		private boolean updated;

		public Wrapper(E payload) {
			this.payload = payload;
		}

		public void setNotUpdated() {
			this.updated=false;
			
		}

		public E getPayload() {
			return payload;
		}

		public void setUpdated() {
			this.updated = true;
		}

		public boolean isUpdated() {
			return this.updated;
		}
	}
}
