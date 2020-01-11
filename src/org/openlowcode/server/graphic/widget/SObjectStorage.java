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
import java.util.function.Function;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.data.message.TObjectDataEltType;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.SimpleDataEltType;

/**
 * An element storing the content on a data object on a page without showing it.
 * That can be useful for multi-step process
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object stored on the widget
 */
public class SObjectStorage<E extends DataObject<E>> extends SPageNode implements SDefaultPath {
	/**
	 * @param name
	 * @param inputdata
	 * @param objectmodel
	 * @param parentpage
	 */
	public SObjectStorage(String name, TObjectDataElt<E> inputdata, DataObjectDefinition<E> objectmodel,
			SPage parentpage) {

		super(parentpage);
		this.inputdata = inputdata;
		this.name = name;
		this.objectmodel = objectmodel;
	}

	private TObjectDataElt<E> inputdata;
	DataObjectDefinition<E> objectmodel;
	private String name; // a way to uniquely name an object display definition

	public <F extends SimpleDataEltType> Function<SActionInputDataRef<F>, SActionDataLoc<F>> getAttributeInput(
			AttributeMarker<E, F> marker) {
		return (a) -> (marker.getDataLoc(this, a));
	}

	public Function<SActionInputDataRef<TObjectDataEltType<E>>, SActionDataLoc<TObjectDataEltType<E>>> getObjectInput() {
		return (a) -> (new SActionDataLoc<TObjectDataEltType<E>>(this, a));
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("NAM", this.name);
		inputdata.writeReferenceToCML(writer);
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
	public String getWidgetCode() {
		return "OBJSTO";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {

		return false;
	}

}
