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
 * This constraint will allow creation of the link only if fields on both sides
 * have the same value in a choice field (the choice field will actually be
 * different on left and right objects but have to use the same choice category)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConstraintOnLinkObjectSameChoiceFieldValue
		extends
		PropertyBusinessRule<LinkObject<?, ?>> {

	private LinkObject<?, ?> linkproperty;
	private DataObjectDefinition leftobject;
	private DataObjectDefinition rightobject;
	private ChoiceField leftchoicefield;
	private ChoiceField rightchoicefield;
	private ChoiceCategory category;

	/**
	 * @return the left choice field
	 */
	public ChoiceField getLeftchoicefield() {
		return leftchoicefield;
	}

	/**
	 * @return the right choice field
	 */
	public ChoiceField getRightchoicefield() {
		return rightchoicefield;
	}

	/**
	 * creates a constraint allowing creation of links only if choice fields on both
	 * sides have the same value
	 * 
	 * @param linkproperty               a link property already linked to both
	 *                                   objects
	 * @param leftobjectchoicefieldname  the name in upper case of the left object
	 *                                   choice field (should already be created on
	 *                                   the object)
	 * @param rightobjectchoicefieldname the name in upper case of the right object
	 *                                   choice field (should already be created on
	 *                                   the object)
	 */
	public ConstraintOnLinkObjectSameChoiceFieldValue(
			LinkObject<?, ?> linkproperty,
			String leftobjectchoicefieldname,
			String rightobjectchoicefieldname) {
		super("CONSTRAINTONLINKSAMECHOICEFIELD" + leftobjectchoicefieldname, false);
		this.linkproperty = linkproperty;
		leftobject = linkproperty.getLeftobjectforlink();
		rightobject = linkproperty.getRightobjectforlink();
		Field leftfield = leftobject.lookupFieldByName(leftobjectchoicefieldname);
		Field rightfield = rightobject.lookupFieldByName(rightobjectchoicefieldname);
		if (leftfield == null)
			throw new RuntimeException("Field " + leftobjectchoicefieldname + " does not exist.");
		if (rightfield == null)
			throw new RuntimeException("Field " + rightobjectchoicefieldname + " does not exist.");
		if (!(leftfield instanceof ChoiceField))
			throw new RuntimeException("Field " + leftobjectchoicefieldname + " is not a ChoiceField but "
					+ leftfield.getClass().getName() + ".");
		if (!(rightfield instanceof ChoiceField))
			throw new RuntimeException("Field " + rightobjectchoicefieldname + " is not a ChoiceField but "
					+ rightfield.getClass().getName() + ".");
		leftchoicefield = (ChoiceField) leftfield;
		rightchoicefield = (ChoiceField) rightfield;
		ChoiceCategory templeftcategory = leftchoicefield.getChoice();
		ChoiceCategory temprightcategory = rightchoicefield.getChoice();
		if (!templeftcategory.equals(temprightcategory))
			throw new RuntimeException("Fields " + leftobjectchoicefieldname + " and " + rightobjectchoicefieldname
					+ " have inconsistent types " + templeftcategory + " and " + temprightcategory + ".");
		this.category = templeftcategory;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String leftobjectclass = StringFormatter.formatForJavaClass(linkproperty.getLeftobjectforlink().getName());
		String rightobjectclass = StringFormatter.formatForJavaClass(linkproperty.getRightobjectforlink().getName());
		String leftobjectfieldclass = StringFormatter.formatForJavaClass(leftchoicefield.getName());
		String rightobjectfieldclass = StringFormatter.formatForJavaClass(rightchoicefield.getName());

		sg.wl("		linkobject.setContraintOnLinkObject(new ConstraintOnLinkedObjectSimilarAttribute<" + leftobjectclass
				+ "," + rightobjectclass + ",ChoiceValue<" + StringFormatter.formatForJavaClass(category.getName())
				+ "ChoiceDefinition>>(");
		sg.wl("				(objectid) -> " + leftobjectclass + ".readone(objectid).get" + leftobjectfieldclass
				+ "(),");
		sg.wl("				(objectid) -> " + rightobjectclass + ".readone(objectid).get" + rightobjectfieldclass
				+ "(),");
		sg.wl("				(object) -> object.get" + leftobjectfieldclass + "(),");
		sg.wl("				(object) -> object.get" + rightobjectfieldclass + "(),");
		sg.wl("				(object,value) -> object.set" + leftobjectfieldclass + "(value),");
		sg.wl("				(object,value) -> object.set" + rightobjectfieldclass + "(value),");
		sg.wl("				" + leftobjectclass + "Definition.get" + leftobjectclass + "Definition().get"
				+ leftobjectfieldclass + "FieldSchema(),");
		sg.wl("				" + rightobjectclass + "Definition.get" + rightobjectclass + "Definition().get"
				+ rightobjectfieldclass + "FieldSchema(),");
		sg.wl("				\"'" + leftobject.getLabel() + "' and '" + rightobject + "' should have same value for '"
				+ leftchoicefield.getDisplayname() + "' and '" + rightchoicefield.getDisplayname()
				+ "' to be linked together\"));");

	}

	@Override
	public String[] getImportstatements() {
		ArrayList<String> returnimport = new ArrayList<String>();
		returnimport.add("import gallium.server.data.properties.constraints.ConstraintOnLinkedObjectSimilarAttribute;");
		returnimport.add("import " + category.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(category.getName()) + "ChoiceDefinition;");
		return returnimport.toArray(new String[0]);
	}

}
