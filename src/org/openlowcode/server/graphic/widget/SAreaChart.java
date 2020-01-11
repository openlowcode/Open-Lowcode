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

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectFieldMarker;
import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ArrayDataElt;

/**
 * A chart showing values according to x and y axis per category
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object holding the data shown in the chart
 */
public class SAreaChart<E extends DataObject<E>> extends SPageNode implements SDefaultPath {

	private String name;
	private ArrayDataElt<TObjectDataElt<E>> inputdata;
	private DataObjectDefinition<E> objectmodel;
	private DataObjectFieldMarker<E> xaxis;
	private DataObjectFieldMarker<E> yaxiscategory;
	private DataObjectFieldMarker<E> yaxisvalue;

	/**
	 * Creates the area chart
	 * 
	 * @param name          name of the chart
	 * @param inputdata     input data
	 * @param objectmodel   defnition of the input data
	 * @param xaxis         marker of a date field
	 * @param yaxiscategory marker providing category for yaxis
	 * @param yaxisvalue    marker providing value to calculate for y axis
	 * @param parentpage    parent page of the widget
	 */
	public SAreaChart(String name, ArrayDataElt<TObjectDataElt<E>> inputdata, DataObjectDefinition<E> objectmodel,
			DataObjectFieldMarker<E> xaxis, DataObjectFieldMarker<E> yaxiscategory, DataObjectFieldMarker<E> yaxisvalue,
			SPage parentpage) {
		super(parentpage);
		this.name = name;
		this.inputdata = inputdata;
		this.objectmodel = objectmodel;
		this.xaxis = xaxis;
		this.yaxiscategory = yaxiscategory;
		this.yaxisvalue = yaxisvalue;
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
		// name and input data reference
		writer.addStringField("NAME", this.name);
		inputdata.writeReferenceToCML(writer);
		// object attributes
		writer.startStructure("ATTRS");
		objectmodel.writeFieldDefinition(writer, input, buffer);
		// fields to show in the diagram
		writer.endStructure("ATTRS");
		writer.addStringField("XAXIS", this.xaxis.toString());
		writer.addStringField("YAXISCATEGORY", this.yaxiscategory.toString());
		writer.addStringField("YAXISVALUE", this.yaxisvalue.toString());

	}

	@Override
	public String getWidgetCode() {
		return "ACHART";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
