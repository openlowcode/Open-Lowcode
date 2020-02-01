/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.ChoiceCategory;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * This business rule will roll-up the lifecycle state of the children to the
 * parent. The parent is considered in the default working state is at least one
 * child is in the default working state. The parent will move to final state if
 * all children reach the final state
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a> *
 *
 */
public class RollupLifecycleStateToParent
		extends
		PropertyBusinessRule<Lifecycle> {
	private LinkedToParent<?> linkedtoparent;
	@SuppressWarnings("unused")
	private Lifecycle childlifecycle;

	/**
	 * create a roll-up lifecycle state to parent
	 * 
	 * @param linkedtoparent the linked to parent relationship used for the business
	 *                       rule
	 */
	public RollupLifecycleStateToParent(LinkedToParent<?> linkedtoparent) {
		super("ROLLUPTOPARENTFOR" + linkedtoparent.getInstancename(), false);
		this.linkedtoparent = linkedtoparent;
		this.childlifecycle = (Lifecycle) linkedtoparent.getParent().getPropertyByName("LIFECYCLE");

	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String childclass = StringFormatter.formatForJavaClass(linkedtoparent.getParent().getName());
		String childattribute = linkedtoparent.getParent().getName().toLowerCase();
		String parentclass = StringFormatter.formatForJavaClass(linkedtoparent.getParentObjectForLink().getName());
		String linktoparentinstance = linkedtoparent.getInstancename().toLowerCase();
		ChoiceCategory childlifecycle = ((Lifecycle) (linkedtoparent.getParent().getPropertyByName("LIFECYCLE")))
				.getTransitionChoiceCategory();
		String childlifecycleclass = StringFormatter.formatForJavaClass(childlifecycle.getName());

		sg.wl(" 		lifecycle.setLifecycleRollUpOnParent(new StandardRollupLifecycleOnParent(");
		sg.wl("				(objectid) -> " + childclass + ".readone(objectid).getparentfor" + linktoparentinstance
				+ "(), ");
		sg.wl("				(objectid) -> " + parentclass + ".readone(objectid).getallchildrenfor"
				+ linktoparentinstance + "for" + childattribute + "(null),");
		sg.wl("				" + childlifecycleclass + "ChoiceDefinition.get(), ");
		sg.wl("				" + childlifecycleclass + "ChoiceDefinition.get()));");

	}

	@Override
	public String[] getImportstatements() {
		ArrayList<String> imports = new ArrayList<String>();
		imports.add("import org.openlowcode.server.data.properties.constraints.StandardRollupLifecycleOnParent;");
		ChoiceCategory childlifecycle = ((Lifecycle) (linkedtoparent.getParent().getPropertyByName("LIFECYCLE")))
				.getTransitionChoiceCategory();
		ChoiceCategory parentlifecycle = ((Lifecycle) (linkedtoparent.getParentObjectForLink()
				.getPropertyByName("LIFECYCLE"))).getTransitionChoiceCategory();
		String childlifecycleclass = StringFormatter.formatForJavaClass(childlifecycle.getName());
		String parentlifecycleclass = StringFormatter.formatForJavaClass(parentlifecycle.getName());
		if (!childlifecycleclass.equals(parentlifecycleclass)) {
			// need to import parent lifecycle class
			imports.add("import " + parentlifecycle.getParentModule().getPath() + ".data.choice." + parentlifecycleclass
					+ "ChoiceDefinition;");
		}
		return imports.toArray(new String[0]);
	}

}
