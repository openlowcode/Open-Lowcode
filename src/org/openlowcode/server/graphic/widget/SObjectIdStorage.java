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
import java.util.function.Function;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.message.TObjectIdDataEltType;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ObjectIdDataElt;

/**
 * a widget storing an id of a data object invisibly on a page. The id can then
 * be used as attribute to an action on a page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object which id is stored
 */
public class SObjectIdStorage<E extends DataObject<E> & UniqueidentifiedInterface<E>>
		extends
		SPageNode
		implements
		SDefaultPath {
	private String name;
	private ObjectIdDataElt inputdata;

	/**
	 * creates an object id storage
	 * 
	 * @param name       unique name of the widget inside the significant parent
	 *                   widget
	 * @param parentpage parent page
	 * @param inputdata  input data
	 */
	public SObjectIdStorage(String name, SPage parentpage, ObjectIdDataElt inputdata) {
		super(parentpage);
		this.name = name;
		this.inputdata = inputdata;
	}

	/**
	 * provides a reference to the data stored in the widget to be used in a page
	 * action
	 * 
	 * @return a reference to the object id stored in the widget
	 */
	public Function<
			SActionInputDataRef<TObjectIdDataEltType<E>>, SActionDataLoc<TObjectIdDataEltType<E>>> getObjectIdInput() {
		return (a) -> (new SActionDataLoc<TObjectIdDataEltType<E>>(this, a));
	}

	@Override
	public String getPathName() {
		return this.name;
	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		inputdata.writeReferenceToCML(writer);
	}

	@Override
	public String getWidgetCode() {

		return "OBJIDS";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {

		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, widgetpathtoroot));
	}
}
