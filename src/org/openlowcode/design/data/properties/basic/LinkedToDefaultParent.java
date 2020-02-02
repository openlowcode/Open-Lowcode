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
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of current object
 */
public class LinkedToDefaultParent<E extends DataObjectDefinition>
		extends
		PropertyBusinessRule<LinkedToParent<E>> {

	@SuppressWarnings("unused")
	private boolean createifnotexists;
	@SuppressWarnings("unused")
	private String defaultnumber;
	private LinkedToParent<E> linkedtoparent;

	/**
	 * creates a linked to default parent that will link to a default parent,
	 * potentially creating it if it does not exist
	 * 
	 * @param defaultnumber     default parent number
	 * @param createifnotexists if true, create missing parent, if false, throw an
	 *                          exception if the parent does not exist
	 * @param linkedtoparent    related linked to parent property
	 */
	public LinkedToDefaultParent(String defaultnumber, boolean createifnotexists, LinkedToParent<E> linkedtoparent) {
		super("LINKEDTODEFAULTPARENT", false);
		this.linkedtoparent = linkedtoparent;
		this.defaultnumber = defaultnumber;
		this.createifnotexists = createifnotexists;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		if (!linkedtoparent.getParentObjectForLink().hasNumbered())
			throw new RuntimeException("Parent object " + linkedtoparent.getParentObjectForLink().getName()
					+ " needs to have NUMBERED property for linked to default parent to work.");
		String mainobjectclass = StringFormatter.formatForJavaClass(linkedtoparent.getParent().getName());

		String parentobjectclass = StringFormatter
				.formatForJavaClass(linkedtoparent.getParentObjectForLink().getName());
		String linknamelowercase = linkedtoparent.getInstancename().toLowerCase();
		sg.wl("		LinkedToDefaultParent<" + mainobjectclass + "," + parentobjectclass + "> linkedtodefaultparentfor"
				+ linknamelowercase + " = new LinkedToDefaultParent<" + mainobjectclass + "," + parentobjectclass
				+ ">(\"DEFAULT\",true) {");
		sg.wl("");
		sg.wl("			@Override");
		sg.wl("			public void processBeforeInsert(" + mainobjectclass + " object)  {");
		sg.wl("				" + parentobjectclass + "[] parents = null;");
		sg.wl("				if (this.getDefaultparentstored()!=null) {");
		sg.wl("					parents = new " + parentobjectclass + "[] {this.getDefaultparentstored()};");
		sg.wl("				} else {");
		sg.wl("					parents = " + parentobjectclass + ".getobjectbynumber(this.getNumber());");
		sg.wl("				}");
		sg.wl("				" + parentobjectclass + " parent=null;");
		sg.wl("				if (parents.length==0) {");
		sg.wl("					if (this.isInsertifnotexists()) {");
		sg.wl("						parent = new " + parentobjectclass + "();");
		sg.wl("						parent.setobjectnumber(this.getNumber());");
		sg.wl("						parent.insert();");
		sg.wl("						this.setDefaultparentstored(parent);");
		sg.wl("					} else  {");
		sg.wl("						throw new RuntimeException(\"Default Parent Number '\"+this.getNumber()+\"' does not exists for object "
				+ parentobjectclass + "\");");
		sg.wl("					}");
		sg.wl("				} else {");
		sg.wl("					parent=parents[0];");
		sg.wl("					this.setDefaultparentstored(parent);");
		sg.wl("					}");

		sg.wl("				object.setparentwithoutupdatefor" + linknamelowercase + "(parent.getId());");
		sg.wl("");
		sg.wl("			}");
		sg.wl("");
		sg.wl("		};");
		sg.wl("		linkedtoparentfor" + linknamelowercase + ".addLinkedToDefaultParentRule(linkedtodefaultparentfor"
				+ linknamelowercase + ");");

	}

	@Override
	public String[] getImportstatements() {
		ArrayList<String> imports = new ArrayList<String>();
		imports.add("import org.openlowcode.server.data.properties.constraints.LinkedToDefaultParent;");
		return imports.toArray(new String[0]);
	}

}
