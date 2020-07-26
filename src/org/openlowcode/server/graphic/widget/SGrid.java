/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.action.SActionOutputDataRef;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.action.SInlineActionRef;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectFieldMarker;
import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.data.message.TObjectDataEltType;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.SimpleDataEltType;
import org.openlowcode.tools.structure.TextDataEltType;

/**
 * A grid shows one or several values of an array of objects in a grid where
 * other fields of the object are used as column or line criteria. For the grid
 * to work property, there should be only one object with the given criteria for
 * lines and columns
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object shown in the grid
 */
public class SGrid<E extends DataObject<E>>
		extends
		SPageNode
		implements
		SDefaultPath {
	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(SGrid.class.getName());
	private String name;
	private DataObjectFieldMarker<E> linefield;
	private ArrayList<DataObjectFieldMarker<E>> columnsfields;

	private ArrayList<DataObjectFieldMarker<E>> valuefield;
	private ArrayList<DataObjectFieldMarker<E>> infofieldsforreversetree;
	private ArrayDataElt<TObjectDataElt<E>> objectarray;
	private DataObjectDefinition<E> objectmodel;
	private SInlineActionRef inlineupdateaction;
	private ArrayList<DataObjectFieldMarker<E>> updateactionfields;
	private SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineupdateactionoutputdata;
	private SActionRef defaultAction;
	private boolean unsaveddatawarning;
	private String unsavedwarningmessage;
	private String unsavedwarningcontinuemessage;
	private String unsavedwarningstopmessage;
	private boolean updatenote;
	private boolean reversetree = false;
	private ArrayList<String> exceptionskeysforeversetree;

	/**
	 * gives a reference to the updated objects in the grid
	 * 
	 * @return a reference to the updated objects in the grid
	 */
	public Function<
			SActionInputDataRef<ArrayDataEltType<TObjectDataEltType<E>>>,
			SActionDataLoc<ArrayDataEltType<TObjectDataEltType<E>>>> getUpdatedObjectArrayInput() {
		return (a) -> (new SActionDataLoc<ArrayDataEltType<TObjectDataEltType<E>>>(this, a));
	}

	/**
	 * gives a reference to the update note input
	 * 
	 * @return a reference to the update note input
	 */
	public Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> getUpdateNoteInput() {
		return (a) -> (new SActionDataLoc<TextDataEltType>(this, a));
	}

	/**
	 * adds an inline update action
	 * 
	 * @param inlineupdateaction           inline update action
	 * @param relevantattributes           relevant attributes for the inline update
	 *                                     (only those attributes are sent back to
	 *                                     the action); To take all normal fields of
	 *                                     object, enter null
	 * @param inlineupdateactionoutputdata output data of the inline action to put
	 *                                     back in the grid after the update
	 */
	public void addUpdateAction(
			SInlineActionRef inlineupdateaction,
			ArrayList<DataObjectFieldMarker<E>> relevantattributes,
			SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineupdateactionoutputdata) {
		addUpdateAction(inlineupdateaction, relevantattributes, inlineupdateactionoutputdata, false);
	}

	/**
	 * adds an inline update action
	 * 
	 * @param inlineupdateaction           inline update action
	 * @param relevantattributes           relevant attributes for the inline update
	 *                                     (only those attributes are sent back to
	 *                                     the action); To take all normal fields of
	 *                                     object, enter null
	 * @param inlineupdateactionoutputdata output data of the inline action to put
	 *                                     back in the grid after the update
	 * @param updatenote                   if true, request the user to enter an
	 *                                     update note
	 */
	public void addUpdateAction(
			SInlineActionRef inlineupdateaction,
			ArrayList<DataObjectFieldMarker<E>> relevantattributes,
			SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineupdateactionoutputdata,
			boolean updatenote) {
		this.inlineupdateaction = inlineupdateaction;
		this.inlineupdateactionoutputdata = inlineupdateactionoutputdata;
		this.updatenote = updatenote;
		if (relevantattributes == null) {
			this.updateactionfields = objectmodel.getAllFieldMarkersForObjectFields();
			return;
		}
		if (relevantattributes.size() == 0)
			throw new RuntimeException("an update action in table should have relevant attributes but table is empty");
		this.updateactionfields = relevantattributes;

	}

	/**
	 * Creates a grid with a single column criteria
	 * 
	 * @param name             unique name of the widget
	 * @param parentpage       page
	 * @param objectarray      the array of objects to show
	 * @param linefield        field to use as line
	 * @param columnfield      field to use as column
	 * @param uniquevaluefield field to show: this is a single value
	 * @param objectmodel      definition of the object being shown in the grid
	 */
	public SGrid(
			String name,
			SPage parentpage,
			ArrayDataElt<TObjectDataElt<E>> objectarray,
			DataObjectFieldMarker<E> linefield,
			DataObjectFieldMarker<E> columnfield,
			DataObjectFieldMarker<E> uniquevaluefield,
			DataObjectDefinition<E> objectmodel) {
		super(parentpage);
		this.name = name;
		this.objectarray = objectarray;
		this.linefield = linefield;
		this.columnsfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.columnsfields.add(columnfield);
		this.valuefield = new ArrayList<DataObjectFieldMarker<E>>();
		this.valuefield.add(uniquevaluefield);
		this.objectmodel = objectmodel;
		this.unsaveddatawarning = false;
		this.updatenote = false;
	}

	/**
	 * display as a reverse tree. The several columns criteria will actually be
	 * shown as lines for grouping in a tree
	 * 
	 * @param fieldmarkersforinfo             list of fields to show as column
	 * @param exceptionsforfieldconsolidation keys to excluded for the consolidation
	 */
	public void setReverseTree(
			DataObjectFieldMarker<E>[] fieldmarkersforinfo,
			String[] exceptionsforfieldconsolidation) {
		this.reversetree = true;
		infofieldsforreversetree = new ArrayList<DataObjectFieldMarker<E>>();
		if (fieldmarkersforinfo != null)
			for (int i = 0; i < fieldmarkersforinfo.length; i++)
				infofieldsforreversetree.add(fieldmarkersforinfo[i]);
		exceptionskeysforeversetree = new ArrayList<String>();
		if (exceptionsforfieldconsolidation!=null) 
			for (int i=0;i<exceptionsforfieldconsolidation.length;i++)
				exceptionskeysforeversetree.add(exceptionsforfieldconsolidation[i]);
		
	}

	/**
	 * creates a grid with two column criteria
	 * 
	 * @param name                 unique name of the widget
	 * @param parentpage           page
	 * @param objectarray          the array of objects to show
	 * @param linefield            field to use as line
	 * @param columnfield          field to use as column
	 * @param secondarycolumnfield the field to use as secondary column criteria
	 * @param uniquevaluefield     field to show: this is a single value
	 * @param objectmodel          definition of the object being shown in the grid
	 */
	public SGrid(
			String name,
			SPage parentpage,
			ArrayDataElt<TObjectDataElt<E>> objectarray,
			DataObjectFieldMarker<E> linefield,
			DataObjectFieldMarker<E> columnfield,
			DataObjectFieldMarker<E> secondarycolumnfield,
			DataObjectFieldMarker<E> uniquevaluefield,
			DataObjectDefinition<E> objectmodel) {
		this(name, parentpage, objectarray, linefield, columnfield, uniquevaluefield, objectmodel);
		
		this.columnsfields.add(secondarycolumnfield);
	}
	
	/**
	 * creates a grid with two column criteria
	 * 
	 * @param name                 unique name of the widget
	 * @param parentpage           page
	 * @param objectarray          the array of objects to show
	 * @param linefield            field to use as line
	 * @param columnfields          fields to use as column
	 * @param uniquevaluefield     field to show: this is a single value
	 * @param objectmodel          definition of the object being shown in the grid
	 * @since 1.11
	 */
	public SGrid(
			String name,
			SPage parentpage,
			ArrayDataElt<TObjectDataElt<E>> objectarray,
			DataObjectFieldMarker<E> linefield,
			DataObjectFieldMarker<E>[] columnfields,
			DataObjectFieldMarker<E> uniquevaluefield,
			DataObjectDefinition<E> objectmodel) {
		this(name, parentpage, objectarray, linefield, columnfields[0], uniquevaluefield, objectmodel);
		for (int i=1;i<columnfields.length;i++) this.columnsfields.add(columnfields[i]);
	}
	

	/**
	 * sets the default action after double click on a cell in the grid
	 * 
	 * @param defaultaction default action after double click on a cell in the grid
	 */
	public void setDefaultAction(SActionRef defaultaction) {
		this.defaultAction = defaultaction;
	}

	/**
	 * @param name        unique name of the widget
	 * @param parentpage  page
	 * @param objectarray the array of objects to show
	 * @param linefield   field to use as line
	 * @param columnfield field to use as column
	 * @param valuefield  field to show: this is an arraylist with at least one
	 *                    element. Practically, more than 3 values will likely
	 *                    result in an unreadable layout
	 * @param objectmodel definition of the data object shown in the grid
	 */
	public SGrid(
			String name,
			SPage parentpage,
			ArrayDataElt<TObjectDataElt<E>> objectarray,
			DataObjectFieldMarker<E> linefield,
			DataObjectFieldMarker<E> columnfield,
			ArrayList<DataObjectFieldMarker<E>> valuefield,
			DataObjectDefinition<E> objectmodel) {
		super(parentpage);
		this.name = name;
		this.objectarray = objectarray;
		this.linefield = linefield;
		this.columnsfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.columnsfields.add(columnfield);
		this.valuefield = valuefield;
		this.objectmodel = objectmodel;
		this.unsaveddatawarning = false;
		this.updatenote = false;

	}

	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, newwidgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {

		if (this.columnsfields.size()>1)
			if (valuefield.size() > 1)
				throw new RuntimeException(
						"Secondary column field is not authorized with several value sizes in current version.");

		writer.addStringField("NAME", this.name);
		writer.addStringField("LNF", linefield.toString());
		writer.addIntegerField("CLN", this.columnsfields.size());
		for (int i=0;i<this.columnsfields.size();i++)
			writer.addStringField("CLF", this.columnsfields.get(i).toString());
		writer.addIntegerField("VLN", valuefield.size());
		for (int i = 0; i < valuefield.size(); i++) {
			DataObjectFieldMarker<E> currentvalue = valuefield.get(i);
			writer.addStringField("VLF", currentvalue.toString());
		}
		objectarray.writeReferenceToCML(writer);
		writer.startStructure("ATTRS");
		// TODO potential optimization: sends only required fields + ID.
		objectmodel.writeFieldDefinition(writer, input, buffer);
		writer.endStructure("ATTRS");
		if (this.inlineupdateaction == null) {
			writer.addBooleanField("INLUPD", false);
		}
		if (this.inlineupdateaction != null) {
			writer.addBooleanField("INLUPD", true);
			writer.startStructure("INLUPD");

			inlineupdateaction.writeToCML(writer);

			writer.startStructure("FIELDS");

			for (int j = 0; j < updateactionfields.size(); j++) {
				DataObjectFieldMarker<E> field = updateactionfields.get(j);
				writer.startStructure("FIELD");
				writer.addStringField("NAM", field.toString());
				writer.endStructure("FIELD");
			}
			writer.endStructure("FIELDS");
			inlineupdateaction.writeReferenceToOutputCLM(writer, inlineupdateactionoutputdata);
			// send constraints here
			objectmodel.writeMultiFieldConstraints(writer);
			writer.addBooleanField("UPDNOT", this.updatenote);

			writer.endStructure("INLUPD");

		}
		if (this.defaultAction != null) {
			writer.addBooleanField("ISCELLACT", true);
			defaultAction.writeToCML(writer);
		} else {
			writer.addBooleanField("ISCELLACT", false);
		}
		writer.addBooleanField("UNSDATWAR", this.unsaveddatawarning);
		if (this.unsaveddatawarning) {
			writer.addStringField("UNSWARMES", this.unsavedwarningmessage);
			writer.addStringField("UNSWARCON", this.unsavedwarningcontinuemessage);
			writer.addStringField("UNSWARSTP", this.unsavedwarningstopmessage);

		}
		writer.addBooleanField("RVT", this.reversetree);
		if (this.reversetree) {
			writer.startStructure("INFFLDS");
			for (int i = 0; i < this.infofieldsforreversetree.size(); i++) {
				writer.startStructure("INFFLD");
				writer.addStringField("NAM", infofieldsforreversetree.get(i).toString());
				if (exceptionskeysforeversetree.size()>i) {
					writer.addStringField("EXC", exceptionskeysforeversetree.get(i)); 
				} else {
					writer.addStringField("EXC", null);
				}
				writer.endStructure("INFFLD");
			}
			writer.endStructure("INFFLDS");
		}
	}

	@Override
	public String getWidgetCode() {
		return "GRD";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	/**
	 * gets the data location of an attribute for the cell selected by double click
	 * 
	 * @param marker attribute marker
	 * @return the data location of an attribute on the selected cell
	 */
	public <F extends SimpleDataEltType> Function<SActionInputDataRef<F>, SActionDataLoc<F>> getAttributeInput(
			AttributeMarker<E, F> marker) {
		return (a) -> (marker.getDataLoc(this, a));
	}

	/**
	 * sets a warning for unsaved edition with default messages
	 */
	public void setWarningForUnsavedEdition() {
		this.unsaveddatawarning = true;
		this.unsavedwarningmessage = SPageNode.DEFAULT_UNSAVED_EDITION_WARNING_MESSAGE;
		this.unsavedwarningcontinuemessage = SPageNode.DEFAULT_UNSAVED_EDITION_CONTINUE_MESSAGE;
		this.unsavedwarningstopmessage = SPageNode.DEFAULT_UNSAVED_EDITION_STOP_MESSAGE;
	}

	/**
	 * sets a warning for unsaved edition with personalized messages
	 * 
	 * @param unsavedwarningmessage         message to shown when user is leaving
	 *                                      the page
	 * @param unsavedwarningcontinuemessage label for the continue (and discard
	 *                                      updates) button
	 * @param unsavedwarningstopmessage     label for the stop button
	 */
	public void setWarningForUnsavedEdition(
			String unsavedwarningmessage,
			String unsavedwarningcontinuemessage,
			String unsavedwarningstopmessage) {
		this.unsaveddatawarning = true;
		this.unsavedwarningmessage = unsavedwarningmessage;
		this.unsavedwarningcontinuemessage = unsavedwarningcontinuemessage;
		this.unsavedwarningstopmessage = unsavedwarningstopmessage;

	}
}
