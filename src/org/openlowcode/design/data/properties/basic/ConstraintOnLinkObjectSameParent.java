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

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * a constraint that will only link left and right objects if they have the same
 * parent. This is an easy way to define working spaces in the application on
 * objects having the same parent, the parent acting as a 'working space'
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> left data object for the link
 * @param <F> right data object for the link
 */
public class ConstraintOnLinkObjectSameParent<E extends DataObjectDefinition, F extends DataObjectDefinition>
		extends
		PropertyBusinessRule<LinkObject<E, F>> {
	private LinkObject<E, F> linkproperty;
	private LinkedToParent<E> leftobjectparentproperty;
	private LinkedToParent<F> rightobjectparentproperty;

	/**
	 * provides the link object on which the constraint is placed
	 * 
	 * @return the link object
	 */
	public LinkObject<E, F> getLinkproperty() {
		return linkproperty;
	}

	/**
	 * provides the link to the common parent from the left object
	 * 
	 * @return link to parent for left object
	 */
	public LinkedToParent<E> getLeftobjectparentproperty() {
		return leftobjectparentproperty;
	}

	/**
	 * provides the link to the common parent from the right object
	 * 
	 * @return link to parent for the right object
	 */
	public LinkedToParent<F> getRightobjectparentproperty() {
		return rightobjectparentproperty;
	}

	/**
	 * creates a new constraint
	 * 
	 * @param linkproperty              link object the constraint is on
	 * @param leftobjectparentproperty  link to the common parent for left object
	 * @param rightobjectparentproperty link to the common parent for right object
	 */
	public ConstraintOnLinkObjectSameParent(
			LinkObject<E, F> linkproperty,
			LinkedToParent<E> leftobjectparentproperty,
			LinkedToParent<F> rightobjectparentproperty) {
		super("CONSTRAINTONLINKSAMEPARENT", false);
		if (linkproperty == null)
			throw new RuntimeException("LinkObject cannot be null");
		if (leftobjectparentproperty == null)
			throw new RuntimeException("Left object parent property cannot be null");
		if (rightobjectparentproperty == null)
			throw new RuntimeException("Right object parent property cannot be null");
		this.linkproperty = linkproperty;

		this.leftobjectparentproperty = leftobjectparentproperty;
		this.rightobjectparentproperty = rightobjectparentproperty;

	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String leftobjectclass = StringFormatter.formatForJavaClass(linkproperty.getLeftobjectforlink().getName());
		String rightobjectclass = StringFormatter.formatForJavaClass(linkproperty.getRightobjectforlink().getName());
		String sameparentobjectclass = StringFormatter
				.formatForJavaClass(leftobjectparentproperty.getParentObjectForLink().getName());
		String leftobjectparentpropertyclass = StringFormatter.formatForJavaClass(leftobjectparentproperty.getName());
		String rightobjectparentpropertyclass = StringFormatter.formatForJavaClass(rightobjectparentproperty.getName());

		sg.wl("		linkobject.setContraintOnLinkObject(new ConstraintOnLinkedObjectSimilarAttribute<" + leftobjectclass
				+ "," + rightobjectclass + ",DataObjectId<" + sameparentobjectclass + ">>(");
		sg.wl("				(objectid) -> " + leftobjectclass + ".readone(objectid).get" + leftobjectparentpropertyclass
				+ "id(),");
		sg.wl("				(objectid) -> " + rightobjectclass + ".readone(objectid).get"
				+ rightobjectparentpropertyclass + "id(),");
		sg.wl("				(object) -> object.get" + leftobjectparentpropertyclass + "id(),");
		sg.wl("				(object) -> object.get" + rightobjectparentpropertyclass + "id(),");
		sg.wl("				(object,id) -> object.setparentwithoutupdatefor"
				+ StringFormatter.formatForAttribute(leftobjectparentproperty.getInstancename()) + "(id),");
		sg.wl("				(object,id) -> object.setparentwithoutupdatefor"
				+ StringFormatter.formatForAttribute(rightobjectparentproperty.getInstancename()) + "(id),");
		sg.wl("				" + leftobjectclass + "Definition.get" + leftobjectclass + "Definition().get"
				+ leftobjectparentpropertyclass + "idFieldSchema(),");
		sg.wl("				" + rightobjectclass + "Definition.get" + rightobjectclass + "Definition().get"
				+ rightobjectparentpropertyclass + "idFieldSchema(),");
		sg.wl("				\"'" + leftobjectparentproperty.getParent().getLabel() + "' and '"
				+ rightobjectparentproperty.getParent().getLabel() + "' should have same parent '"
				+ leftobjectparentproperty.getParentObjectForLink().getLabel() + "' to be linked together\"));");

	}

	@Override
	public String[] getImportstatements() {
		ArrayList<String> returnimport = new ArrayList<String>();
		returnimport.add("import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkedObjectSimilarAttribute;");
		return returnimport.toArray(new String[0]);
	}

}
