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
import java.util.List;
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
import org.openlowcode.server.data.DataObjectPropertyDefinition.FieldSchemaForDisplay;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.ObjectTreeDataElt;
import org.openlowcode.tools.structure.ObjectTreeDataEltType;
import org.openlowcode.tools.structure.SimpleDataEltType;
import org.openlowcode.tools.structure.TextDataElt;

/**
 * This widget is display an array tree of objects. This is the most powerful
 * widget to show complex structured data.<br>
 * 
 * The widget does not yet support edition mode.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 * @param <E> data object the tree array is displaying
 */
public class SObjectTreeArray<E extends DataObject<E>> extends SPageNode implements SDefaultPath {
	private boolean inline;
	private String name;
	private ObjectTreeDataElt<TObjectDataElt<E>> inputdata;
	private DataObjectDefinition<E> objectmodel;
	private SActionRef defaultaction;
	private ArrayList<DataObjectFieldMarker<E>> hiddenfields;
	private NamedList<DisplayProfile<E>> activeprofiles;
	private SInlineActionRef inlineaction;
	private SActionOutputDataRef<ObjectTreeDataEltType<TObjectDataEltType<E>>> inlineoutputdata;
	private int minfieldpriority = -1000;

	private HashMap<DataObjectFieldMarker<E>, TextDataElt> overridenlabels;
	private List<FieldSchemaForDisplay<E>> flexiblefieldsdefinition;

	/**
	 * overrides a field label by a label given as an input attribute of the page.
	 * This allows to give variable label to columns according to context
	 * 
	 * @param fieldmarker maker of the field
	 * @param newlabel    a text attribute of the page to use as label for the given
	 *                    label
	 */
	public void addFieldLabelOverrides(DataObjectFieldMarker<E> fieldmarker, TextDataElt newlabel) {
		this.overridenlabels.put(fieldmarker, newlabel);
	}

	/**
	 * sets the minimum priority of field to show
	 * 
	 * @param minfieldpriority a number between -1000 and 1000 excluded
	 */
	public void setMinFieldPriority(int minfieldpriority) {
		if (this.minfieldpriority > 1000)
			throw new RuntimeException("minfieldpriority should be smaller or equal to 1000");
		if (this.minfieldpriority < -1000)
			throw new RuntimeException("minfieldpriority should be greater or equal than -1000");
		this.minfieldpriority = minfieldpriority;
	}

	/**
	 * gets the attribute specified on the selected data object as input for an
	 * action in the page
	 * 
	 * @param marker field marker
	 * @return the attribute to be used as input of an action on a page
	 */
	public <F extends SimpleDataEltType> Function<SActionInputDataRef<F>, SActionDataLoc<F>> getAttributeInput(
			AttributeMarker<E, F> marker) {
		return (a) -> (marker.getDataLoc(this, a));
	}

	/**
	 * creates an object tree array without any special interaction inside the
	 * widget for the array
	 * 
	 * @param name        name of the object tree array
	 * @param inputdata   a tree of object data elements
	 * @param objectmodel the definition of the object
	 * @param parent      parent page
	 */
	public SObjectTreeArray(String name, ObjectTreeDataElt<TObjectDataElt<E>> inputdata,
			DataObjectDefinition<E> objectmodel, SPage parent) {
		super(parent);
		this.name = name;
		this.inputdata = inputdata;
		@SuppressWarnings("unchecked")
		TObjectDataElt<E> root = (TObjectDataElt<E>) inputdata.getObject(inputdata.getRootId());
		if (root != null) {
			flexiblefieldsdefinition = root.getContent().getFlexibleFieldsDefinition();
		}
		this.objectmodel = objectmodel;
		this.defaultaction = null;
		this.inline = false;
		this.hiddenfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.activeprofiles = new NamedList<DisplayProfile<E>>();
		this.overridenlabels = new HashMap<DataObjectFieldMarker<E>, TextDataElt>();
	}

	/**
	 * create an object tree array that will display the result of an inline action
	 * (action launched on the same page as the output is displayed)
	 * 
	 * @param name             name of the widget
	 * @param inlineaction     inline action
	 * @param inlineoutputdata output on the inline action
	 * @param objectmodel      definition of the object being displayed in the tree
	 *                         array widget
	 * @param parent           parent page of the widget
	 */
	public SObjectTreeArray(String name, SInlineActionRef inlineaction,
			SActionOutputDataRef<ObjectTreeDataEltType<TObjectDataEltType<E>>> inlineoutputdata,
			DataObjectDefinition<E> objectmodel, SPage parent) {
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
	}

	/**
	 * adds a display profile (that will hide some fields)
	 * 
	 * @param profile display profile
	 */
	public void addDisplayProfile(DisplayProfile<E> profile) {
		this.activeprofiles.add(profile);
	}

	/**
	 * adds a default action that is triggered when a user double clicks after
	 * selecting a line
	 * 
	 * @param defaultaction default action to trigger
	 */
	public void addDefaultAction(SActionRef defaultaction) {
		this.defaultaction = defaultaction;
	}

	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("NAME", this.name);
		if (defaultaction != null)
			defaultaction.writeToCML(writer);
		writer.startStructure("ATTRS");

		objectmodel.writeFieldDefinition(writer, hiddenfields, activeprofiles, this.minfieldpriority, input, buffer,
				flexiblefieldsdefinition);
		writer.endStructure("ATTRS");
		writer.addBooleanField("INL", this.inline);
		if (!this.inline)
			inputdata.writeReferenceToCML(writer);
		if (this.inline) {
			inlineaction.writeReferenceToOutputCLM(writer, inlineoutputdata);
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
	}

	@Override
	public String getWidgetCode() {
		return "OBJTRA";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {

		return false;
	}

	/**
	 * hide the given attribute
	 * 
	 * @param marker attribute marker of the attribute to hide
	 */
	public void hideAttribute(DataObjectFieldMarker<E> marker) {
		hiddenfields.add(marker);
		inputdata.hideElement(marker);
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
	}
}
