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

/**
 * an array of objects shown as a list of buttons displaying a specific
 * attribute (typically the number) of an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SObjectArrayField<E extends DataObject<E>>
		extends
		SPageNode
		implements
		SDefaultPath {
	private boolean inlinefeeding;
	private String label;
	private String helper;
	private ArrayDataElt<TObjectDataElt<E>> inputdata;
	private String name;
	private DataObjectDefinition<E> objectmodel;
	private SActionRef defaultaction;
	private SActionRef deleteaction;
	private ArrayList<DataObjectFieldMarker<E>> hiddenfields;
	// check what this is used for

	private SActionOutputDataRef<TObjectDataEltType<E>> inlineoutputdata;
	private DataObjectFieldMarker<E> fieldtoshow;
	private NamedList<DisplayProfile<E>> activeprofiles;
	private SPageNode nodeatendoffielddata;

	private int minfieldpriority = -1000;
	private SInlineActionRef feedinginlineaction;

	/**
	 * creates an object array field
	 * 
	 * @param name         unique name of the array field widget in the parent
	 *                     significant widget
	 * @param label        the label shown in the system (put null if you do not
	 *                     want the label to show)
	 * @param helper       helper that shows when rolling-over the field label
	 * @param inputdata    reference to the data to show
	 * @param objectmodel  definition of the object shown
	 * @param fieldstoshow the field of the object that will be shown on the field
	 * @param parent       parent page
	 */
	public SObjectArrayField(
			String name,
			String label,
			String helper,
			ArrayDataElt<TObjectDataElt<E>> inputdata,
			DataObjectDefinition<E> objectmodel,
			DataObjectFieldMarker<E> fieldtoshow,
			SPage parent) {
		super(parent);
		this.label = label;
		this.helper = helper;
		this.name = name;
		this.inputdata = inputdata;
		this.objectmodel = objectmodel;
		this.defaultaction = null;
		this.inlinefeeding = false;
		this.fieldtoshow = fieldtoshow;
		this.activeprofiles = new NamedList<DisplayProfile<E>>();
	}

	/**
	 * adds a display profile to hide some fields
	 * 
	 * @param profile profile
	 */
	public void addDisplayProfile(DisplayProfile<E> profile) {
		this.activeprofiles.add(profile);
	}

	/**
	 * allows to feed the context of this array field with the result of a page
	 * inline action. The element (one object) will be added to the list of objects
	 * already stored
	 * 
	 * @param feedinginlineaction inline action
	 * @param inlineoutputdata    output data of the inline action to use to feed
	 *                            the widget
	 */
	public void addFeedingInlineAction(
			SInlineActionRef feedinginlineaction,
			SActionOutputDataRef<TObjectDataEltType<E>> inlineoutputdata) {
		this.inlinefeeding = true;
		this.feedinginlineaction = feedinginlineaction;
		this.inlineoutputdata = inlineoutputdata;
	}

	/**
	 * @param defaultaction the action for double clink on the link
	 */
	public void addDefaultAction(SActionRef defaultaction) {
		this.defaultaction = defaultaction;
	}

	/**
	 * @param deleteaction if this action is set, then, there will be a small cross
	 *                     near each link to allow delete
	 */
	public void addDeleteAction(SActionRef deleteaction) {
		this.deleteaction = deleteaction;
	}

	/**
	 * @param nodeatendoffielddata adds a node at the end of the object array field.
	 *                             Typically, this can be a search (SFIeldSearcher)
	 *                             to add new objects
	 */
	public void addNodeAtEndOfFieldData(SPageNode nodeatendoffielddata) {
		this.nodeatendoffielddata = nodeatendoffielddata;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("LBL", this.label);
		writer.addStringField("HPR", this.helper);
		writer.addStringField("NAM", this.name);
		if (defaultaction != null) {
			writer.startStructure("DFT");
			defaultaction.writeToCML(writer);
			writer.endStructure("DFT");
		}
		if (deleteaction != null) {
			writer.startStructure("DLT");
			deleteaction.writeToCML(writer);
			writer.endStructure("DLT");
		}
		if (nodeatendoffielddata != null) {
			writer.startStructure("NAE");
			nodeatendoffielddata.WriteToCDL(writer, input, buffer);
			writer.endStructure("NAE");
		}
		writer.startStructure("ATTRS");
		objectmodel.writeFieldDefinition(writer, hiddenfields, activeprofiles, this.minfieldpriority, input, buffer);
		writer.endStructure("ATTRS");
		writer.addStringField("FTS", fieldtoshow.toString());
		writer.addBooleanField("HID", (inputdata!=null));
		if (inputdata!=null) inputdata.writeReferenceToCML(writer);
		if (this.inlinefeeding) {
			writer.addBooleanField("INF", true);
			writer.startStructure("INLACT");
			feedinginlineaction.writeToCML(writer);
			feedinginlineaction.writeReferenceToOutputCLM(writer, inlineoutputdata);
			writer.endStructure("INLACT");

		} else {
			writer.addBooleanField("INF", false);
		}
	}

	@Override
	public String getWidgetCode() {
		return "OBJARF";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	/**
	 * gets a reference to the attribute for the object selected to be used as
	 * attribute of an action (typically the object id)
	 * 
	 * @param marker marker of the attribute
	 * @return a reference to the attribute for the object selected
	 */
	public <F extends SimpleDataEltType> Function<SActionInputDataRef<F>, SActionDataLoc<F>> getAttributeInput(
			AttributeMarker<E, F> marker) {
		return (a) -> (marker.getDataLoc(this, a));
	}

	/**
	 * gets a reference to the array of objects being stored in the object array
	 * field
	 * 
	 * @return a reference to the array of objects stored
	 */
	public Function<
			SActionInputDataRef<ArrayDataEltType<TObjectDataEltType<E>>>,
			SActionDataLoc<ArrayDataEltType<TObjectDataEltType<E>>>> getObjectArrayInput() {
		return (a) -> (new SActionDataLoc<ArrayDataEltType<TObjectDataEltType<E>>>(this, a,
				a.getType().getObjectName()));
	}

	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);
		if (nodeatendoffielddata != null)
			nodeatendoffielddata.populateDown(parentpath, newwidgetpathtoroot);
	}
}
