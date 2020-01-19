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
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectFieldMarker;
import org.openlowcode.server.data.DisplayProfile;
import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.SimpleDataEltType;

/**
 * a band of objects displayed one below the other. This is used for a
 * presentation similar to comments in a web page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E> data object being shown in the object band
 */
public class SObjectBand<E extends DataObject<E>>
		extends
		SPageNode
		implements
		SDefaultPath {
	private String name;
	private ArrayDataElt<TObjectDataElt<E>> inputdata;
	private DataObjectDefinition<E> objectmodel;
	private SPageNode actiongroup;
	private ArrayList<DataObjectFieldMarker<E>> hiddenfields;
	private NamedList<DisplayProfile<E>> activeprofiles;

	/**
	 * creates an object band widget inside a page declaration
	 * 
	 * @param name        unique name in the context of parent significant widget
	 * @param inputdata   input array of objecs to show
	 * @param objectmodel definition of the data object
	 * @param parent      parent page
	 */
	public SObjectBand(
			String name,
			ArrayDataElt<TObjectDataElt<E>> inputdata,
			DataObjectDefinition<E> objectmodel,
			SPage parent) {
		super(parent);
		this.name = name;
		this.inputdata = inputdata;
		this.objectmodel = objectmodel;

		this.hiddenfields = new ArrayList<DataObjectFieldMarker<E>>();
		this.activeprofiles = new NamedList<DisplayProfile<E>>();
	}

	/**
	 * sets a node as action group. It will be repeated below each object
	 * 
	 * @param actiongroup action group.
	 */
	public void setActionGroup(SPageNode actiongroup) {
		this.actiongroup = actiongroup;
	}

	/**
	 * adds a display profile to hide some specific fields
	 * 
	 * @param profile display profile to hide some specific fields
	 */
	public void addDisplayProfile(DisplayProfile<E> profile) {
		this.activeprofiles.add(profile);
	}

	/**
	 * gets a reference of the attribute for the object selected in the object band.
	 * 
	 * @param marker marker of the attribute to use as action attribute
	 * @return reference to the attribute of the object selected in the object band
	 */
	public <F extends SimpleDataEltType> Function<SActionInputDataRef<F>, SActionDataLoc<F>> getAttributeInput(
			AttributeMarker<E, F> marker) {
		return (a) -> (marker.getDataLoc(this, a));
	}

	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("NAME", this.name);
		if (actiongroup != null) {
			writer.startStructure("ACNGRP");
			actiongroup.WriteToCDL(writer, input, buffer);
			writer.endStructure("ACNGRP");
		}

		objectmodel.writeFieldDefinition(writer, hiddenfields, activeprofiles, false, -1000, input, buffer);

		inputdata.writeReferenceToCML(writer);

	}

	@Override
	public String getWidgetCode() {
		return "OBJBND";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {

		return false;
	}

	/**
	 * allows to hide a specific attribute
	 * 
	 * @param marker marker of the attribute to add
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
