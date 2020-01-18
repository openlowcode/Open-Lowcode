/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
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
import org.openlowcode.server.data.message.TObjectDataEltType;
import org.openlowcode.server.data.message.TObjectIdDataEltType;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.TextDataEltType;

/**
 * A searcher widget that typically allows to enter number and have a list of
 * objects in a popup to choose from. This is often used with the
 * SObjectArrayField widget (for example to show links to objects in a compact
 * way)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of data object being searched
 */
public class SFieldSearcher<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends
		SPageNode
		implements
		SDefaultPath {
	private String name;
	private String addlabel;
	private String closelabel;
	private String rollovertip;
	private SInlineActionRef search;
	private SActionRef action;
	private SInlineActionRef inlineaction;
	private DataObjectDefinition<E> objectmodel;
	private DataObjectFieldMarker<E> fieldtoshow;
	private int minfieldpriority = -1000;
	private NamedList<DisplayProfile<E>> activeprofiles;
	private ArrayList<DataObjectFieldMarker<E>> hiddenfields;
	private SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineoutputdata;
	private boolean hasactionatbottom = false;
	private SActionRef actionatbottom;
	private boolean showonlyifemptysearch = false;
	private String bottomactionlabel;

	/**
	 * The action at bottom displays after the search button is pressed. It
	 * typically allows to create an object if no one is found.
	 * 
	 * @param actionatbottom
	 * @param bottomactionlabel
	 * @param showonlyifemptysearch
	 */

	public void addBottomAction(SActionRef actionatbottom, String bottomactionlabel, boolean showonlyifemptysearch) {
		this.hasactionatbottom = true;
		this.actionatbottom = actionatbottom;
		this.bottomactionlabel = bottomactionlabel;
		this.showonlyifemptysearch = showonlyifemptysearch;
	}

	/**
	 * @param name        name of the widget (should be unique in parent widget with
	 *                    a significant path)
	 * @param addlabel    label showing on the button
	 * @param closelabel  label when the field is closed (for example 'add' to
	 *                    explain people should add a new reference to link here)
	 * @param rollovertip tip that will show on the button
	 * @param search      action launched for search. This action should have one
	 *                    string argument and gives back an array of objects
	 * 
	 * @param action      action to trigger after the searched object has been
	 *                    selected
	 * @param objectmodel definition of the data object
	 * @param fieldtoshow field to show in searcher. Typically, this is the object
	 *                    number
	 * @param parent      parent page
	 */
	public SFieldSearcher(
			String name,
			String addlabel,
			String closelabel,
			String rollovertip,
			SInlineActionRef search,
			SActionRef action,
			DataObjectDefinition<E> objectmodel,
			DataObjectFieldMarker<E> fieldtoshow,
			SPage parent) {
		super(parent);
		this.name = name;
		this.addlabel = addlabel;
		this.closelabel = closelabel;
		this.rollovertip = rollovertip;
		this.search = search;
		this.action = action;
		this.objectmodel = objectmodel;
		this.fieldtoshow = fieldtoshow;
		this.activeprofiles = new NamedList<DisplayProfile<E>>();

	}

	/**
	 * @param name         name of the widget (should be unique in parent widget
	 *                     with a significant path)
	 * @param addlabel     label showing on the button
	 * @param closelabel   label when the field is closed (for example 'add' to
	 *                     explain people should add a new reference to link here)
	 * @param rollovertip  tip that will show on the button
	 * @param search       action launched for search. This action should have one
	 *                     string argument and gives back an array of objects
	 * @param inlineaction inline action triggered when object is selected
	 * @param objectmodel  definition of the data object shown in this widget
	 * @param fieldtoshow  field to show (typically the number)
	 * @param parent       parent page for the widget
	 */
	public SFieldSearcher(
			String name,
			String addlabel,
			String closelabel,
			String rollovertip,
			SInlineActionRef search,
			SInlineActionRef inlineaction,
			DataObjectDefinition<E> objectmodel,
			DataObjectFieldMarker<E> fieldtoshow,
			SPage parent) {
		super(parent);
		this.name = name;
		this.addlabel = addlabel;
		this.closelabel = closelabel;
		this.rollovertip = rollovertip;
		this.search = search;
		this.inlineaction = inlineaction;
		this.objectmodel = objectmodel;
		this.fieldtoshow = fieldtoshow;
		this.activeprofiles = new NamedList<DisplayProfile<E>>();

	}

	/**
	 * gets the data object id of the object selected by the searcher to be used in
	 * the action
	 * 
	 * @return data object id of the object selected
	 */
	public Function<
			SActionInputDataRef<TObjectIdDataEltType<E>>, SActionDataLoc<TObjectIdDataEltType<E>>> getObjectIdInput() {
		return (a) -> (new SActionDataLoc<TObjectIdDataEltType<E>>(this, a));
	}

	public SActionDataLoc<TObjectDataEltType<E>> getObject(SActionInputDataRef<TObjectDataEltType<E>> objectargument) {
		return new SActionDataLoc<TObjectDataEltType<E>>(this, objectargument);

	}

	/**
	 * gets the full data object of the object selected in the searcher, to be used
	 * in the action
	 * 
	 * @return reference to the data object
	 */
	public Function<
			SActionInputDataRef<TObjectDataEltType<E>>, SActionDataLoc<TObjectDataEltType<E>>> getObjectInput() {
		return (a) -> (new SActionDataLoc<TObjectDataEltType<E>>(this, a));
	}

	/**
	 * gets the data object id of the object selected in the searcher, to be used in
	 * the action as a one element array
	 * 
	 * @return a reference to a one element object id array
	 */
	public Function<
			SActionInputDataRef<ArrayDataEltType<TObjectIdDataEltType<E>>>,
			SActionDataLoc<ArrayDataEltType<TObjectIdDataEltType<E>>>> getObjectIdArrayInput() {
		return (a) -> (new SActionDataLoc<ArrayDataEltType<TObjectIdDataEltType<E>>>(this, a));
	}

	/**
	 * gets the text entered in the search widget as action attribute
	 * 
	 * @return reference to the text entered in the search widget
	 */
	public Function<SActionInputDataRef<TextDataEltType>, SActionDataLoc<TextDataEltType>> getSearchTextInput() {
		return (a) -> (new SActionDataLoc<TextDataEltType>(this, a));
	}

	/**
	 * sets the result of the search inside the widget
	 * 
	 * @param inlineoutputdata result of the search (array of objects)
	 */
	public void setSearchInlineOutput(SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineoutputdata) {
		this.inlineoutputdata = inlineoutputdata;
	}

	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		if (this.inlineoutputdata == null)
			throw new RuntimeException(
					"Inline output data cannot be null for searcher " + this.getPathName() + " - " + this.name);
		writer.addStringField("NAM", this.name);
		writer.addStringField("ADL", this.addlabel);
		writer.addStringField("CSL", this.closelabel);
		writer.addStringField("RLT", this.rollovertip);

		writer.startStructure("SRA");
		search.writeToCML(writer);
		writer.endStructure("SRA");
		if (this.action != null) {
			writer.addBooleanField("ATP", true);
			writer.startStructure("ACT");
			action.writeToCML(writer);
			writer.endStructure("ACT");
		} else {
			writer.addBooleanField("ATP", false);
			writer.startStructure("INLACT");
			inlineaction.writeToCML(writer);
			writer.endStructure("INLACT");

		}
		writer.startStructure("ATTRS");
		objectmodel.writeFieldDefinition(writer, hiddenfields, activeprofiles, this.minfieldpriority, input, buffer);
		writer.endStructure("ATTRS");
		writer.addStringField("FTS", fieldtoshow.toString());
		search.writeReferenceToOutputCLM(writer, inlineoutputdata);
		writer.addBooleanField("HAB", this.hasactionatbottom);
		if (this.hasactionatbottom) {
			this.actionatbottom.writeToCML(writer);
			writer.addStringField("BAL", this.bottomactionlabel);
			writer.addBooleanField("SOE", this.showonlyifemptysearch);
		}
	}

	@Override
	public String getWidgetCode() {

		return "FLDSRC";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
