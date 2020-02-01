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

import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * Creates a constraint on auto-link object that allows to link two objects only
 * if they have the same parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConstraintOnAutolinkObjectSameParent
		extends
		PropertyBusinessRule<AutolinkObject<?>> {
	private AutolinkObject<?> autolinkproperty;
	private LinkedToParent<?> sameparent;

	/**
	 * creates a constraint on auto-link object that allows to link two objects only
	 * if they have the same parent
	 * 
	 * @param autolinkproperty the linked property this is added to
	 * @param sameparent       the linked to parent property to consider for the
	 *                         linked object
	 * @throws GalliumException
	 */
	public ConstraintOnAutolinkObjectSameParent(AutolinkObject<?> autolinkproperty, LinkedToParent<?> sameparent) {
		super("CONSTRAINTONAUTOLINKSAMEPARENT", false);
		this.autolinkproperty = autolinkproperty;
		this.sameparent = sameparent;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String linkedobjectclass = StringFormatter.formatForJavaClass(autolinkproperty.getObjectforlink().getName());

		String parentobjectclass = StringFormatter.formatForJavaClass(sameparent.getParentObjectForLink().getName());
		String sameparentclass = StringFormatter.formatForJavaClass(sameparent.getName());

		sg.wl("		autolinkobject.setContraintOnAutolinkObject(new ConstraintOnAutolinkObjectSimilarAttribute<"
				+ linkedobjectclass + ",DataObjectId<" + parentobjectclass + ">>(");
		sg.wl("				(objectid) -> " + linkedobjectclass + ".readone(objectid).get" + sameparentclass + "id(),");
		sg.wl("				(object) -> object.get" + sameparentclass + "id(),");

		sg.wl("				" + linkedobjectclass + "Definition.get" + linkedobjectclass + "Definition().get"
				+ sameparentclass + "idFieldSchema(),");
		sg.wl("				\"'" + autolinkproperty.getParent().getLabel() + "' should have same parent '"
				+ sameparent.getParentObjectForLink().getLabel() + "' to be linked together\"));");

	}

	@Override
	public String[] getImportstatements() {
		ArrayList<String> returnimport = new ArrayList<String>();
		returnimport
				.add("import gallium.server.data.properties.constraints.ConstraintOnAutolinkObjectSimilarAttribute;");
		return returnimport.toArray(new String[0]);
	}
}
