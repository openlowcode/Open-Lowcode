/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.workflow;

import java.io.IOException;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * A user selector that will use a parent link from the object to a user
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ObjectParentUserSelector
		extends
		TaskUserSelector {
	private DataObjectDefinition object;
	private LinkedToParent<?> userlinkedtoparent;

	/**
	 * creates an object parent user selector
	 * 
	 * @param object             object that the workflow is running on
	 * @param userlinkedtoparent a linked to parent with
	 *                           <ul>
	 *                           <li>the parent being the object the workflow is
	 *                           running on</li>
	 *                           <li>the child being an app user</li>
	 *                           </ul>
	 */
	public ObjectParentUserSelector(DataObjectDefinition object, LinkedToParent<?> userlinkedtoparent) {
		this.object = object;
		this.userlinkedtoparent = userlinkedtoparent;
		if (userlinkedtoparent.getParent() != object)
			throw new RuntimeException(
					"Linked to Parent property should refer to the object provided in attribute, object = "
							+ object.getName() + ", parent = " + userlinkedtoparent.getParent().getName());
		if (userlinkedtoparent.getParentObjectForLink() != SystemModule.getSystemModule().getAppuser())
			throw new RuntimeException("Linked to parent property should be have as parent APPUSER, currently "
					+ userlinkedtoparent.getParentObjectForLink().getName());
	}

	@Override
	public DataObjectDefinition getObjectForQuery() {
		return object;
	}

	@Override
	public void writeSelectorDeclaration(
			SourceGenerator sg,
			Module module,
			String parentobjectclass,
			String lifecycleclass,
			String taskcodelowercase) throws IOException {
		String parentname = StringFormatter.formatForJavaClass(userlinkedtoparent.getName());
		sg.wl("		ObjectParentUserSelectionForTask<" + parentobjectclass + "," + lifecycleclass + "ChoiceDefinition> "
				+ taskcodelowercase + "userselection");
		sg.wl("		= new ObjectParentUserSelectionForTask<" + parentobjectclass + "," + lifecycleclass
				+ "ChoiceDefinition>(( a -> a.get" + parentname + "id()));");
	}
}
