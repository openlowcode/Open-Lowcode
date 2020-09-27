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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.function.Function;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.action.SActionOutputDataRef;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.action.SInlineActionRef;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectFieldMarker;
import org.openlowcode.server.data.DisplayProfile;
import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.data.message.TObjectDataEltType;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.SimpleDataEltType;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;

/**
 * objects displayed in a table, with an edition mode that has the objective to
 * be as spreadsheet-like as possible
 * 
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object
 */
public class SObjectArray<E extends DataObject<E>>
		extends
		SPageNode
		implements
		SDefaultPath {
	private ArrayDataElt<TObjectDataElt<E>> inputdata;
	private String name;
	private DataObjectDefinition<E> objectmodel;
	private SActionRef defaultaction;
	private boolean defaultupdatemode;
	private boolean allowdataclear;
	/**
	 * helper text for read-write table
	 */
	public static String ARRAY_EDIT_HELPER = "Array edit feature available on table below:\n\n"
			+ " o To start array edit, hold Ctrl+ left click on the table\n"
			+ " o To finish array edit, hold Ctrl + left click on the table\n"
			+ " o Inside text, use shift+enter to insert carriage return, use enter to finish edit\n\n"
			+ "This table also allows paste to excel by using Ctrl+ double click.";

	/**
	 * helper for read-only table
	 */
	public static String ARRAY_RO_HELPER = "This table also allows paste to excel by using Ctrl+ double click.";

	// fields for inline action that only provides output to the table

	private boolean inline;
	private SInlineActionRef inlineaction;
	private SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineoutputdata;

	// fields for update action that takes input inside the table (updated rows)
	// output of the update action is put back into the table
	private SInlineActionRef inlineupdateaction;
	private SActionRef updateaction;
	private ArrayList<DataObjectFieldMarker<E>> updateactionfields;
	private SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineupdateactionoutputdata;

	private ArrayList<DataObjectFieldMarker<E>> hiddenfields;
	private NamedList<DisplayProfile<E>> activeprofiles;
	private int forcedrowheightinlines = 0;
	private int minfieldpriority = -1000;
	private HashMap<DataObjectFieldMarker<E>, TextDataElt> overridenlabels;

	/**
	 * details if multi-select is allowed in read-only mode
	 */
	private boolean allowmultiselect = false;
	private boolean unsaveddatawarning;
	private String unsavedwarningmessage;
	private String unsavedwarningcontinuemessage;
	private String unsavedwarningstopmessage;
	private boolean updatenote;
	private int rowstodisplay = 0;

	/**
	 * this method allows to create an array initiated with data from the action
	 * 
	 * @param name        unique name of the table in the scope of next parent
	 *                    significant widget
	 * @param inputdata   an array of objects as input attribute to the page
	 * @param objectmodel definition of the data object
	 * @param parent      parent page
	 */
	public SObjectArray(
			String name,
			ArrayDataElt<TObjectDataElt<E>> inputdata,
			DataObjectDefinition<E> objectmodel,
			SPage parent) {
		super(parent);
		this.name = name;
		this.inputdata = inputdata;
		this.objectmodel = objectmodel;
		this.defaultaction = null;
		this.inline = false;
		this.hiddenfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.activeprofiles = new NamedList<DisplayProfile<E>>();
		this.overridenlabels = new HashMap<DataObjectFieldMarker<E>, TextDataElt>();
		this.unsaveddatawarning = false;
		this.updatenote = false;
		this.forcedrowheightinlines = objectmodel.getPreferedTableRowHeight();
		this.allowdataclear = false;
	}

	/**
	 * This feature, if set, allows users to remove elements from the table. This
	 * has no consequence on persisted data. However, it probably mostly makes sense
	 * for arrays that represent a transient group (such as result of a search).
	 * 
	 */
	public void setAllowDataClear() {
		this.allowdataclear = true;
	}

	/**
	 * uses a display profile for the objects (allowing to hide some attributes)
	 * 
	 * @param profile display profile to add to the widget
	 */
	public void addDisplayProfile(DisplayProfile<E> profile) {
		this.activeprofiles.add(profile);
	}


	/**
	 * sets a default action that will be triggered in read-only mode when double
	 * clicking on a line
	 * 
	 * @param defaultaction default action reference
	 */
	public void addDefaultAction(SActionRef defaultaction) {
		this.defaultaction = defaultaction;
	}

	/**
	 * Overrides the label for a field
	 * 
	 * @param fieldmarker marker of the field
	 * @param newlabel    new label to show in the table
	 */
	public void addFieldLabelOverrides(DataObjectFieldMarker<E> fieldmarker, TextDataElt newlabel) {
		this.overridenlabels.put(fieldmarker, newlabel);
	}

	/**
	 * when this parameter is set, the client will not try to calculate smartly the
	 * number of lines, but will set it to the specified amount. This is especially
	 * useful in case rows of the table need to be more than 2 lines in height
	 * 
	 * @param numberoflines 1 to display 1 line, 2 to display 2 lines ...
	 */
	public void forceRowHeight(int numberoflines) {
		this.forcedrowheightinlines = numberoflines;
	}

	/**
	 * when this parameter is set, only the fields with a priority higher or equal
	 * than the specified field priority are displayed. This impacts both the column
	 * layout and the data sent.
	 * 
	 * @param minfieldpriority a value between 1000 and -1000
	 */
	public void setMinFieldPriority(int minfieldpriority) {
		if (this.minfieldpriority > 1000)
			throw new RuntimeException("minfieldpriority should be smaller or equal to 1000");
		if (this.minfieldpriority < -1000)
			throw new RuntimeException("minfieldpriority should be greater or equal than -1000");
		this.minfieldpriority = minfieldpriority;
	}

	/**
	 * defines the preferred number of rows to be shown on the table
	 * 
	 * @param rowstodisplay number of rows to display
	 */
	public void setRowsToDisplay(int rowstodisplay) {
		this.rowstodisplay = rowstodisplay;
	}

	/**
	 * this creates an array initiated at page load and updated by inline action
	 * 
	 * @param name             unique name of the widget
	 * @param inputdata        data to initiate the table at page loading
	 * @param inlineaction     inline action providing extra data
	 * @param inlineoutputdata reference to the ouput data of the inline action
	 *                         providing extra data
	 * @param objectmodel      definition of the data object to show
	 * @param parent           parent page
	 */
	public SObjectArray(
			String name,
			ArrayDataElt<TObjectDataElt<E>> inputdata,
			SInlineActionRef inlineaction,
			SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineoutputdata,
			DataObjectDefinition<E> objectmodel,
			SPage parent) {
		super(parent);
		this.name = name;
		this.inputdata = inputdata;
		this.inlineaction = inlineaction;
		this.inlineoutputdata = inlineoutputdata;
		this.objectmodel = objectmodel;
		this.defaultaction = null;
		this.inline = true;
		this.hiddenfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.unsaveddatawarning = false;
		this.activeprofiles = new NamedList<DisplayProfile<E>>();
		this.overridenlabels = new HashMap<DataObjectFieldMarker<E>, TextDataElt>();
		this.updatenote = false;
		this.forcedrowheightinlines = objectmodel.getPreferedTableRowHeight();
		this.allowdataclear = false;
	}

	/**
	 * This method creates an empty table that will be filled with inline action
	 * data
	 * 
	 * @param name             unique name of the widget
	 * @param inlineaction     inline action providing extra data
	 * @param inlineoutputdata reference to the ouput data of the inline action
	 *                         providing extra data
	 * @param objectmodel      definition of the data object to show
	 * @param parent           parent page
	 */
	public SObjectArray(
			String name,
			SInlineActionRef inlineaction,
			SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineoutputdata,
			DataObjectDefinition<E> objectmodel,
			SPage parent) {
		super(parent);
		this.name = name;
		this.inlineaction = inlineaction;
		this.inlineoutputdata = inlineoutputdata;
		this.objectmodel = objectmodel;
		this.defaultaction = null;
		this.inline = true;
		this.hiddenfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.activeprofiles = new NamedList<DisplayProfile<E>>();
		this.overridenlabels = new HashMap<DataObjectFieldMarker<E>, TextDataElt>();
		this.unsaveddatawarning = false;
		this.updatenote = false;
		this.forcedrowheightinlines = objectmodel.getPreferedTableRowHeight();
		this.allowdataclear = false;
	}



	/**
	 * if true, it is possible to select several rows
	 */
	public void setAllowMultiSelect() {
		this.allowmultiselect = true;
	}

	/**
	 * adds an inline update action with table starting in read-only mode
	 * 
	 * @param updateaction                 this action should be linked to the table
	 *                                     as input data. It will be called once for
	 *                                     all the lines that were updated
	 * @param relevantattributes           specify attributes to be updated. To take
	 *                                     all normal fields of object, enter null
	 * @param inlineupdateactionoutputdata the data to refresh the table with
	 */
	public void addUpdateAction(
			SInlineActionRef inlineupdateaction,
			ArrayList<DataObjectFieldMarker<E>> relevantattributes,
			SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineupdateactionoutputdata) {
		addUpdateAction(inlineupdateaction, relevantattributes, inlineupdateactionoutputdata, false);
	}

	/**
	 * adds an inline udate action with specified update mode at first table  display and update note mechanism
	 * 
	 * @param updateaction                 this action should be linked to the table
	 *                                     as input data. It will be called once for
	 *                                     all the lines that were updated
	 * @param relevantattributes           specify attributes to be updated. To take
	 *                                     all normal fields of object, enter null
	 * @param inlineupdateactionoutputdata the data to refresh the table with
	 * @param defaultupdatemode            if set to true, the table will start in
	 *                                     update mode
	 * @param updatenote if true, update note is requested
	 */
	public void addUpdateAction(
			SInlineActionRef updateaction,
			ArrayList<DataObjectFieldMarker<E>> relevantattributes,
			SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineupdateactionoutputdata,
			boolean defaultupdatemode,
			boolean updatenote) {
		addUpdateAction(inlineupdateaction, relevantattributes, inlineupdateactionoutputdata, defaultupdatemode);
		this.updatenote = updatenote;
	}

	/**
	 * adds an inline update action where after update the page is still shown
	 * 
	 * @param updateaction                 this action should be linked to the table
	 *                                     as input data. It will be called once for
	 *                                     all the lines that were updated
	 * @param relevantattributes           specify attributes to be updated. To take
	 *                                     all normal fields of object, enter null
	 * @param inlineupdateactionoutputdata the data to refresh the table with
	 * @param defaultupdatemode            if set to true, the table will start in
	 *                                     update mode
	 */
	public void addUpdateAction(
			SInlineActionRef inlineupdateaction,
			ArrayList<DataObjectFieldMarker<E>> relevantattributes,
			SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineupdateactionoutputdata,
			boolean defaultupdatemode) {
		if (this.updateaction != null)
			throw new RuntimeException("cannot have an inline update action and update action");

		if (relevantattributes == null) {
			this.inlineupdateaction = inlineupdateaction;
			this.updateactionfields = objectmodel.getAllFieldMarkersForObjectFields();
			this.inlineupdateactionoutputdata = inlineupdateactionoutputdata;
			this.defaultupdatemode = defaultupdatemode;
			return;
		}
		if (relevantattributes.size() == 0)
			throw new RuntimeException("an update action in table should have relevant attributes but table is empty");

		this.inlineupdateaction = inlineupdateaction;
		this.updateactionfields = relevantattributes;
		this.inlineupdateactionoutputdata = inlineupdateactionoutputdata;
		this.defaultupdatemode = defaultupdatemode;
	}

	/**
	 * adds an update action with table starting as read-only and no entry of update
	 * note after edition
	 * 
	 * @param updateaction       this action should be linked to the table as input
	 *                           data. It will be called once for all the lines that
	 *                           were updated
	 * @param relevantattributes specify attributes to be updated. To take all
	 *                           normal fields of object, enter null
	 */
	public void addUpdateAction(SActionRef updateaction, ArrayList<DataObjectFieldMarker<E>> relevantattributes) {

		addUpdateAction(updateaction, relevantattributes, false, false);
	}

	/**
	 * adds an update action where the table starts as read-only
	 * 
	 * @param updateaction       this action should be linked to the table as input
	 *                           data. It will be called once for all the lines that
	 *                           were updated
	 * @param relevantattributes specify attributes to be updated. To take all
	 *                           normal fields of object, enter null
	 * @param updatenote         forces the user to enter an update note after
	 *                           editing a series of rows
	 */
	public void addUpdateAction(
			SActionRef updateaction,
			ArrayList<DataObjectFieldMarker<E>> relevantattributes,
			boolean updatenote) {
		addUpdateAction(updateaction, relevantattributes, false, updatenote);
	}

	/**
	 * adds an update action to the object array table
	 * 
	 * @param updateaction       this action should be linked to the table as input
	 *                           data. It will be called once for all the lines that
	 *                           were updated
	 * @param relevantattributes specify attributes to be updated. To take all
	 *                           normal fields of object, enter null
	 * @param defaultupdatemode  if set to true, the table will start in update mode
	 * @param updatenote         forces the user to enter an update note after
	 *                           editing a series of rows
	 */
	public void addUpdateAction(
			SActionRef updateaction,
			ArrayList<DataObjectFieldMarker<E>> relevantattributes,
			boolean defaultupdatemode,
			boolean updatenote) {
		if (this.inlineupdateaction != null)
			throw new RuntimeException("cannot have an inline update action and update action");
		if (relevantattributes == null) {
			this.updateaction = updateaction;
			this.updateactionfields = objectmodel.getAllFieldMarkersForObjectFields();
			this.defaultupdatemode = defaultupdatemode;
			if (!this.updatenote)
				this.updatenote = updatenote;
			return;
		}
		if (relevantattributes.size() == 0)
			throw new RuntimeException("an update action in table should have relevant attributes but table is empty");
		this.updateaction = updateaction;
		this.updateactionfields = relevantattributes;
		this.defaultupdatemode = defaultupdatemode;
		if (!this.updatenote)
			this.updatenote = updatenote;
	}

	/**
	 * gets the content of the update note to be used as attribute for an action
	 * 
	 * @return the update note input
	 */
	public Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> getUpdateNoteInput() {
		// if using update note input, making sure it is turned on
		this.updatenote = true;
		return (a) -> (new SActionDataLoc<TextDataEltType>(this, a));
	}

	/**
	 * gets a specific attribute on the object selected
	 * 
	 * @param marker marker of the attribute
	 * @return a reference to the attribute of the object selected to use as
	 *         attribute for a page action
	 */
	public <F extends SimpleDataEltType> Function<SActionInputDataRef<F>, SActionDataLoc<F>> getAttributeInput(
			AttributeMarker<E, F> marker) {
		return (a) -> (marker.getDataLoc(this, a));
	}

	/**
	 * gets a reference to the array of attributes for all objects in this table
	 * 
	 * @param marker marker of the field
	 * @return a reference to the array of attributes for all objects in thetable
	 */
	public <F extends SimpleDataEltType> Function<
			SActionInputDataRef<ArrayDataEltType<F>>, SActionDataLoc<ArrayDataEltType<F>>> getAttributeArrayInput(
					AttributeMarker<E, F> marker) {
		return (a) -> (marker.getArrayDataLoc(this, a));
	}

	/**
	 * gets a reference to all active objects (modified) after an edition
	 * 
	 * @return a reference to all active objects (modified) after an edition
	 */
	public Function<
			SActionInputDataRef<ArrayDataEltType<TObjectDataEltType<E>>>,
			SActionDataLoc<ArrayDataEltType<TObjectDataEltType<E>>>> getActiveObjectArray() {
		return (a) -> (new SActionDataLoc<ArrayDataEltType<TObjectDataEltType<E>>>(this, a,
				a.getType().getObjectName()));
	}

	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("NAME", this.name);
		writer.addBooleanField("AMS", allowmultiselect);
		writer.addBooleanField("ADC", this.allowdataclear);
		writer.addIntegerField("RWH", this.forcedrowheightinlines);
		writer.addIntegerField("RTD", this.rowstodisplay);
		if (defaultaction != null)
			defaultaction.writeToCML(writer);
		writer.startStructure("ATTRS");

		objectmodel.writeFieldDefinition(writer, hiddenfields, activeprofiles, this.minfieldpriority, input, buffer);
		writer.endStructure("ATTRS");
		writer.addBooleanField("INL", this.inline);

		if (this.inline) {
			inlineaction.writeReferenceToOutputCLM(writer, inlineoutputdata);
		}
		boolean inputdataflag = false;
		if (inputdata != null)
			inputdataflag = true;
		writer.addBooleanField("IND", inputdataflag);
		if (inputdataflag)
			inputdata.writeReferenceToCML(writer);

		// ------------------------------ send update actions
		// -------------------------------
		if (this.inlineupdateaction == null) {
			writer.addBooleanField("INLUPD", false);
		}
		if (this.inlineupdateaction != null) {
			writer.addBooleanField("INLUPD", true);
			writer.startStructure("INLUPD");
			writer.addBooleanField("DUM", this.defaultupdatemode);

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
		if (this.updateaction == null) {
			writer.addBooleanField("UPD", false);
		}
		if (this.updateaction != null) {
			writer.addBooleanField("UPD", true);
			writer.startStructure("UPD");
			writer.addBooleanField("DUM", this.defaultupdatemode);

			updateaction.writeToCML(writer);
			writer.startStructure("FIELDS");

			for (int j = 0; j < updateactionfields.size(); j++) {
				DataObjectFieldMarker<E> field = updateactionfields.get(j);
				writer.startStructure("FIELD");
				writer.addStringField("NAM", field.toString());
				writer.endStructure("FIELD");
			}
			writer.endStructure("FIELDS");
			objectmodel.writeMultiFieldConstraints(writer);
			// Note: does not send multi field constraints
			writer.addBooleanField("UPDNOT", this.updatenote);
			writer.endStructure("UPD");

		}
		// ------------------------------ writing overwrite label
		writer.startStructure("OVWLBLS");
		Iterator<Entry<DataObjectFieldMarker<E>, TextDataElt>> labelsiterator = overridenlabels.entrySet().iterator();
		while (labelsiterator.hasNext()) {
			Entry<DataObjectFieldMarker<E>, TextDataElt> thisentry = labelsiterator.next();
			writer.startStructure("OVWLBL");
			writer.addStringField("FLD", thisentry.getKey().toString());
			thisentry.getValue().writeReferenceToCML(writer);
			writer.endStructure("OVWLBL");
		}
		writer.endStructure("OVWLBLS");
		writer.addBooleanField("UNSDATWAR", this.unsaveddatawarning);
		if (this.unsaveddatawarning) {
			writer.addStringField("UNSWARMES", this.unsavedwarningmessage);
			writer.addStringField("UNSWARCON", this.unsavedwarningcontinuemessage);
			writer.addStringField("UNSWARSTP", this.unsavedwarningstopmessage);

		}
	}

	@Override
	public String getWidgetCode() {

		return "OBJARR";
	}

	/**
	 * hides the specified attribute
	 * 
	 * @param marker marker of the attribute
	 */
	public void hideAttribute(DataObjectFieldMarker<E> marker) {
		hiddenfields.add(marker);
		inputdata.hideElement(marker);
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, newwidgetpathtoroot));
	}

	/**
	 * sets warning for unsaved edition when the page is left with default messages
	 */
	public void setWarningForUnsavedEdition() {
		this.unsaveddatawarning = true;
		this.unsavedwarningmessage = SPageNode.DEFAULT_UNSAVED_EDITION_WARNING_MESSAGE;
		this.unsavedwarningcontinuemessage = SPageNode.DEFAULT_UNSAVED_EDITION_CONTINUE_MESSAGE;
		this.unsavedwarningstopmessage = SPageNode.DEFAULT_UNSAVED_EDITION_STOP_MESSAGE;
	}

	/**
	 * sets warning for unsaved edition when the page is left with personalized
	 * messages
	 * 
	 * @param unsavedwarningmessage         unsaved warning top message
	 * @param unsavedwarningcontinuemessage label of the continue (discard update)
	 *                                      button
	 * @param unsavedwarningstopmessage     label of the stop (do not go do the
	 *                                      other page and proceed with update)
	 *                                      button
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
