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
import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * this constraint will allow creation of an auto-link object only if indicated
 * choice field has the same value on both side of the newly created auto-link
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConstraintOnAutolinkObjectSameChoiceFieldValue
		extends
		PropertyBusinessRule<AutolinkObject<?>> {

	private AutolinkObject<?> autolinkproperty;
	private DataObjectDefinition linkedobject;
	private ChoiceField choicefield;
	private ChoiceCategory category;

	public ChoiceField getChoiceField() {
		return this.choicefield;
	}

	/**
	 * creates a constraint on auto-link object for same choice field value
	 * 
	 * @param autolinkproperty      a link property already linked to both objects
	 * @param objectchoicefieldname the name in upper case of the linked object
	 *                              choice field (should already be created on the
	 *                              object)
	 * @throws GalliumException
	 */
	public ConstraintOnAutolinkObjectSameChoiceFieldValue(
			AutolinkObject<?> autolinkproperty,
			String objectchoicefieldname) {
		super("CONSTRAINTONAUTOLINKSAMECHOICEFIELD" + objectchoicefieldname, false);
		this.autolinkproperty = autolinkproperty;
		linkedobject = autolinkproperty.getObjectforlink();
		Field relevantfield = linkedobject.lookupFieldByName(objectchoicefieldname);

		if (relevantfield == null)
			throw new RuntimeException("Field " + objectchoicefieldname + " does not exist.");

		if (!(relevantfield instanceof ChoiceField))
			throw new RuntimeException("Field " + objectchoicefieldname + " is not a ChoiceField but "
					+ relevantfield.getClass().getName() + ".");
		choicefield = (ChoiceField) relevantfield;

		this.category = choicefield.getChoice();
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String linkedobjectclass = StringFormatter.formatForJavaClass(linkedobject.getName());
		String fieldclass = StringFormatter.formatForJavaClass(choicefield.getName());

		sg.wl("		autolinkobject.setContraintOnAutolinkObject(new ConstraintOnAutolinkObjectSimilarAttribute<"
				+ linkedobjectclass + ",ChoiceValue<" + StringFormatter.formatForJavaClass(category.getName())
				+ "ChoiceDefinition>>(");
		sg.wl("				(objectid) -> " + linkedobjectclass + ".readone(objectid).get" + fieldclass + "(),");
		sg.wl("				(object) -> object.get" + fieldclass + "(),");
		sg.wl("				" + linkedobjectclass + "Definition.get" + linkedobjectclass + "Definition().get"
				+ fieldclass + "FieldSchema(),");
		sg.wl("				\"'" + autolinkproperty.getParent().getLabel() + "' should have same value on field '"
				+ choicefield.getDisplayname() + "' to be linked together\"));");

	}

	@Override
	public String[] getImportstatements() {
		ArrayList<String> returnimport = new ArrayList<String>();
		returnimport
				.add("import gallium.server.data.properties.constraints.ConstraintOnAutolinkObjectSimilarAttribute;");
		returnimport.add("import " + category.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(category.getName()) + "ChoiceDefinition;");
		return returnimport.toArray(new String[0]);
	}

}
