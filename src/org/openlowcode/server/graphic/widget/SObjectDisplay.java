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
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectFieldMarker;
import org.openlowcode.server.data.StringDataObjectFieldDefinition;
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
import org.openlowcode.tools.structure.TextDataElt;

/**
 * A widget showing an object. It has a title, normal fields, fields shown after
 * the more separator, and fields shown in bottom notes
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data objects shown
 */
public class SObjectDisplay<E extends DataObject<E>>
		extends
		SPageNode
		implements
		SDefaultPath {
	private TObjectDataElt<E> inputdata;
	DataObjectDefinition<E> objectmodel;
	private boolean readonly;
	private boolean hidereadonlyfield;
	private String name; // a way to uniquely name an object display definition
	private SActionRef defaultaction;
	private SPageNode noderightoftitle = null;
	private boolean hasnoderightoftitle = false;

	private SPageNode buttonbar = null;
	private boolean hasbuttonbar=false;
	
	private boolean showcontent;
	private boolean showtitle;
	private int morefieldpriority = 0;
	private int minfieldpriority = -1000;
	private ArrayList<DataObjectFieldMarker<E>> hiddenfields;
	private HashMap<DataObjectFieldMarker<E>, TextDataElt> overridenlabels;
	private HashMap<DataObjectFieldMarker<E>, ArrayDataElt<TextDataElt>> suggestionsfortextfields;

	/**
	 * creates an object display
	 * 
	 * @param name        unique name of the widget inside the significant parent
	 *                    widget
	 * @param inputdata   page attribute to show
	 * @param objectmodel definition of the data object
	 * @param parent      parent page
	 * @param readonly    if true, widget is readonly, if false, the widget is
	 *                    allowed
	 */
	public SObjectDisplay(
			String name,
			TObjectDataElt<E> inputdata,
			DataObjectDefinition<E> objectmodel,
			SPage parent,
			boolean readonly) {
		super(parent);
		this.name = name;
		this.inputdata = inputdata;
		this.objectmodel = objectmodel;
		this.readonly = readonly;
		this.defaultaction = null;
		hidereadonlyfield = false;
		this.showcontent = true;
		this.showtitle = true;
		this.hiddenfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.suggestionsfortextfields = new HashMap<DataObjectFieldMarker<E>, ArrayDataElt<TextDataElt>>();
		this.overridenlabels = new HashMap<DataObjectFieldMarker<E>, TextDataElt>();
	}

	/**
	 * creates an object display with a default action
	 * 
	 * @param name          unique name of the widget inside the significant parent
	 *                      widget
	 * @param inputdata     page attribute to show
	 * @param objectmodel   definition of the data object
	 * @param parent        parent page
	 * @param readonly      if true, widget is readonly, if false, the widget is
	 *                      allowed
	 * @param defaultaction action to trigger when clicking return on one of the
	 *                      fields
	 */
	public SObjectDisplay(
			String name,
			TObjectDataElt<E> inputdata,
			DataObjectDefinition<E> objectmodel,
			SPage parent,
			boolean readonly,
			SActionRef defaultaction) {
		super(parent);
		this.name = name;
		this.inputdata = inputdata;
		this.objectmodel = objectmodel;
		this.readonly = readonly;
		this.defaultaction = defaultaction;
		if (this.readonly)
			if (defaultaction != null)
				throw new RuntimeException(
						"it is not possible to set-up a defaultaction when widget is readonly, widget name = " + name);
		hidereadonlyfield = false;
		this.showcontent = true;
		this.showtitle = true;
		this.hiddenfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.suggestionsfortextfields = new HashMap<DataObjectFieldMarker<E>, ArrayDataElt<TextDataElt>>();
		this.overridenlabels = new HashMap<DataObjectFieldMarker<E>, TextDataElt>();
	}

	/**
	 * adds a new label for a field
	 * 
	 * @param fieldmarker field of the object to override
	 * @param newlabel    new label for the field
	 */
	public void addFieldLabelOverrides(DataObjectFieldMarker<E> fieldmarker, TextDataElt newlabel) {
		this.overridenlabels.put(fieldmarker, newlabel);
	}

	/**
	 * Adds a suggestion for a text field of the object
	 * 
	 * @param textfieldmarker
	 * @param suggestions
	 * @since 1.6
	 */
	public void addTextFieldSuggestion(
			DataObjectFieldMarker<E> textfieldmarker,
			ArrayDataElt<TextDataElt> suggestions) {
		if (objectmodel.getFieldMarkerClass(textfieldmarker) != StringDataObjectFieldDefinition.class)
			throw new RuntimeException("For object " + objectmodel.getName() + ", cannot provide suggestions for field "
					+ textfieldmarker.getName() + " as class is not text but "
					+ objectmodel.getFieldMarkerClass(textfieldmarker));
		this.suggestionsfortextfields.put(textfieldmarker, suggestions);
	}

	/**
	 * displays the object as summarized version (only the title)
	 * 
	 * @param title if true, show only the title
	 */
	public void setReducedDisplay(boolean title) {
		if (title) {
			this.showcontent = false;
			this.showtitle = true;
		} else {
			this.showcontent = true;
			this.showtitle = false;
		}
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
	 * when this parameter is set, only the fields with a priority higher or equal
	 * than the specified field priority are displayed above the more collapsible
	 * pane
	 * 
	 * @param morefieldpriority a value between 1000 and -1000
	 */
	public void setMoreFieldPriority(int morefieldpriority) {
		if (this.morefieldpriority > 1000)
			throw new RuntimeException("minfieldpriority should be smaller or equal to 1000");
		if (this.morefieldpriority < -1000)
			throw new RuntimeException("minfieldpriority should be greater or equal than -1000");
		this.morefieldpriority = morefieldpriority;
	}

	/**
	 * if true, read-only fields are hidden
	 */
	public void setHideReadOnly() {
		this.hidereadonlyfield = true;
	}

	/**
	 * hides the specified field
	 * 
	 * @param marker field marker for the field
	 */
	public void hideAttribute(DataObjectFieldMarker<E> marker) {
		hiddenfields.add(marker);

	}

	/**
	 * gets the attribute of the object to be used as an argument for an action on
	 * the page
	 * 
	 * @param marker marker of the field
	 * @return the reference to the content of one field of the object
	 */
	public <F extends SimpleDataEltType> Function<SActionInputDataRef<F>, SActionDataLoc<F>> getAttributeInput(
			AttributeMarker<E, F> marker) {
		return (a) -> (marker.getDataLoc(this, a));
	}

	/**
	 * gets the attribute of the object as a one element array to be used as
	 * argument for an action on the page
	 * 
	 * @param marker marker of the field
	 * @return the reference to the content of one field of the object as a one
	 *         element array
	 */
	public <F extends SimpleDataEltType> Function<
			SActionInputDataRef<ArrayDataEltType<F>>, SActionDataLoc<ArrayDataEltType<F>>> getOneElementArrayInput(
					AttributeMarker<E, F> marker) {
		return (a) -> (marker.getOneElementArrayDataLoc(this, a));
	}

	/**
	 * gets the full object as input for an action on the page
	 * 
	 * @return a reference to the content of the full object
	 */
	public Function<
			SActionInputDataRef<TObjectDataEltType<E>>, SActionDataLoc<TObjectDataEltType<E>>> getObjectInput() {
		return (a) -> (new SActionDataLoc<TObjectDataEltType<E>>(this, a));
	}

	/**
	 * adds a node right of title. This can be used to shown a special action button
	 * 
	 * @param node node to add right of the title
	 */
	public void addPageNodeRightOfTitle(SPageNode node) {
		this.hasnoderightoftitle = true;
		this.noderightoftitle = node;
	}

	/**
	 * 
	 * 
	 * @param node node to add as button bar under the title
	 */
	public void addButtonBarUnderTitle(SPageNode node) {
		this.hasbuttonbar = true;
		this.buttonbar = node;
	}
	
	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("NAME", this.name);
		writer.addStringField("LABEL", this.objectmodel.getLabel());
		writer.addBooleanField("SOT", this.showtitle);
		writer.addBooleanField("SOC", this.showcontent);

		if (this.defaultaction != null) {
			defaultaction.writeToCML(writer);
		}
		writer.addBooleanField("RO", this.readonly);
		writer.addBooleanField("HR", this.hidereadonlyfield);
		E object = inputdata.getContent();
		if (object == null)
			throw new RuntimeException("object is null, this is not correct");
		if (!readonly) {
			objectmodel.writeFieldDefinition(writer, hiddenfields, null, true, minfieldpriority, morefieldpriority,
					input, buffer);
		} else {
			objectmodel.writeFieldDefinition(writer, hiddenfields, null, false, minfieldpriority, morefieldpriority,
					input, buffer);
		}

		inputdata.writeReferenceToCML(writer);
		writer.addBooleanField("HNRT", this.hasnoderightoftitle);
		if (this.hasnoderightoftitle) {
			writer.startStructure("NRT");
			this.noderightoftitle.WriteToCDL(writer, input, buffer);
			writer.endStructure("NRT");

		}
		
		writer.addBooleanField("HBTBAR", this.hasbuttonbar);
		if (this.hasbuttonbar) {
			writer.startStructure("BBB");
			this.buttonbar.WriteToCDL(writer, input, buffer);
			writer.endStructure("BBB");
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
		writer.startStructure("TXTSUGS");
		Iterator<Entry<DataObjectFieldMarker<E>,
						ArrayDataElt<TextDataElt>>> suggestionsiterator 
		= this.suggestionsfortextfields.entrySet()
								.iterator();
		while (suggestionsiterator.hasNext()) {
			Entry<DataObjectFieldMarker<E>, ArrayDataElt<TextDataElt>> thisentry = suggestionsiterator.next();
			writer.startStructure("TXTSUG");
			writer.addStringField("FLD", thisentry.getKey().toString());
			thisentry.getValue().writeReferenceToCML(writer);
			writer.endStructure("TXTSUG");
		}
		writer.endStructure("TXTSUGS");
	}

	@Override
	public String getWidgetCode() {

		return "OBJDIS";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	/**
	 * get the data from this widget (used by security mechanisms)
	 * 
	 * @return the data from this widget
	 */
	public TObjectDataElt<E> getInputData() {
		return this.inputdata;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
		if (noderightoftitle != null)
			noderightoftitle.populateDown(parentpath, widgetpathtoroot);
		if (buttonbar!=null) 
			buttonbar.populateDown(parentpath, widgetpathtoroot);
	}
}
