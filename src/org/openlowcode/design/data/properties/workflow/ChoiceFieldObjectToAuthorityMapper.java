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
import java.util.ArrayList;
import java.util.HashMap;

import org.openlowcode.design.access.TotalAuthority;
import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.NamedList;

/**
 * an authority mapper choosing an authority depending on the value of a choice
 * field on the object the workflow is working on.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ChoiceFieldObjectToAuthorityMapper
		extends
		ObjectToAuthorityMapper {
	private TotalAuthority defaultauthority;
	private ChoiceField choicefield;
	private ArrayList<ValueToAuthority> mappings;
	private NamedList<ChoiceValue> valueswithmapping;

	private class ValueToAuthority {
		private ChoiceValue value;
		private TotalAuthority authority;

		public ValueToAuthority(ChoiceValue value, TotalAuthority authority) {

			this.value = value;
			this.authority = authority;
		}

	}

	/**
	 * creates the choice field authority mapper
	 * 
	 * @param object           data object
	 * @param choicefield      choice field to use
	 * @param defaultauthority default authority to choose for choices who do not
	 *                         have a specific mapping
	 */
	public ChoiceFieldObjectToAuthorityMapper(
			DataObjectDefinition object,
			ChoiceField choicefield,
			TotalAuthority defaultauthority) {
		super(object);
		this.defaultauthority = defaultauthority;
		this.choicefield = choicefield;
		mappings = new ArrayList<ValueToAuthority>();
		valueswithmapping = new NamedList<ChoiceValue>();
	}

	/**
	 * adds a specific mapping
	 * 
	 * @param value     a valid choice value for the choice field
	 * @param authority the authority to use
	 */
	public void addMapping(ChoiceValue value, TotalAuthority authority) {
		if (valueswithmapping.lookupOnName(value.getName()) != null)
			throw new RuntimeException("value " + value + " already present");
		valueswithmapping.add(value);
		mappings.add(new ValueToAuthority(value, authority));
	}

	@Override
	public void generateSingleTaskPropertyHelperToFile(SourceGenerator sg, Module module, DataObjectDefinition parent)
			throws IOException {
		String objectclassname = StringFormatter.formatForJavaClass(parent.getName());
		String choicetype = StringFormatter.formatForJavaClass(this.choicefield.getChoice().getName());
		String choicefield = StringFormatter.formatForJavaClass(this.choicefield.getName());

		sg.wl("package " + parent.getOwnermodule().getPath() + ".data;");
		sg.wl("");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.workflowhelper.ObjectToAuthorityMapper;");
		sg.wl("import org.openlowcode.server.data.workflowhelper.SimpletaskWorkflowHelper;");
		sg.wl("import " + parent.getOwnermodule().getPath() + ".data.choice." + choicetype + "ChoiceDefinition;");
		sg.wl("import org.openlowcode.module.system.data.Authority;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.workflowhelper.ChoiceFieldExtractor;");
		sg.wl("import org.openlowcode.server.data.workflowhelper.ChoiceFieldObjectToAuthorityMapper;");
		sg.wl("import org.openlowcode.module.system.data.choice.DelaytypeChoiceDefinition;");

		sg.wl("");
		sg.wl("public class " + objectclassname + "SimpleTaskWorkflowHelper extends SimpletaskWorkflowHelper<"
				+ objectclassname + "> {");
		sg.wl("	private static " + objectclassname + "SimpleTaskWorkflowHelper singleton;");
		sg.wl("	private  ChoiceFieldObjectToAuthorityMapper<" + objectclassname + "," + choicetype
				+ "ChoiceDefinition> singleauthoritymapper;");
		sg.wl("	@Override");
		sg.wl("	public String getTaskMessage()  {");
		sg.wl("	return \"" + this.getMessage().replace("\"", "\\\"") + "\";");
		sg.wl("	}");
		sg.wl("	@Override");
		sg.wl("	public ObjectToAuthorityMapper<" + objectclassname + "> getSingleAuthorityMapper() {");
		sg.wl("		if (singleauthoritymapper==null) {");
		sg.wl("		Authority defaultauthority= Authority.getuniqueobjectbynumber(\""
				+ getObject().getOwnermodule().getCode() + "_" + defaultauthority.getName().toUpperCase()
				+ "\",null);");
		sg.wl("		ChoiceFieldObjectToAuthorityMapper<" + objectclassname + "," + choicetype
				+ "ChoiceDefinition> tempmapper = new  ChoiceFieldObjectToAuthorityMapper<" + objectclassname + ","
				+ choicetype + "ChoiceDefinition>(");
		sg.wl("				defaultauthority.getId(), ");
		sg.wl("				new " + objectclassname + "SimpletaskWorkflow());");
		sg.wl("		// creates specific mapping");

		HashMap<String, String> allauthorities = new HashMap<String, String>();
		for (int i = 0; i < this.mappings.size(); i++) {
			ValueToAuthority thismapping = this.mappings.get(i);
			String authority = getObject().getOwnermodule().getCode() + "_" + thismapping.authority.getName();
			if (allauthorities.get(authority) == null) {
				allauthorities.put(authority, authority);
				sg.wl("		Authority " + authority.toLowerCase() + " = Authority.getuniqueobjectbynumber(\""
						+ authority + "\",null);");

			}
		}

		for (int i = 0; i < this.mappings.size(); i++) {
			ValueToAuthority thismapping = this.mappings.get(i);
			String value = thismapping.value.getName();
			String authority = getObject().getOwnermodule().getCode() + "_" + thismapping.authority.getName();
			sg.wl("		ChoiceValue " + value.toLowerCase() + "value = " + choicetype + "ChoiceDefinition.get()."
					+ value + ";");
			sg.wl("		tempmapper.addSpecificMapping(" + value.toLowerCase() + "value," + authority.toLowerCase()
					+ ".getId());");
		}
		sg.wl("		singleauthoritymapper=tempmapper;");
		sg.wl("		}");
		sg.wl("		return singleauthoritymapper;");
		sg.wl("	}");
		sg.wl("	private " + objectclassname + "SimpleTaskWorkflowHelper()  {");
		sg.wl("		super(SimpletaskWorkflowHelper.EMAIL_NOW);");
		sg.wl("	}");
		sg.wl("	public static " + objectclassname + "SimpleTaskWorkflowHelper get()  {");
		sg.wl("		if (singleton==null) {");
		sg.wl("			" + objectclassname + "SimpleTaskWorkflowHelper temp = new " + objectclassname
				+ "SimpleTaskWorkflowHelper();");
		sg.wl("			singleton = temp;");
		sg.wl("		}");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("	private class " + objectclassname + "SimpletaskWorkflow implements ChoiceFieldExtractor<"
				+ objectclassname + "," + choicetype + "ChoiceDefinition> {");
		sg.wl("");
		sg.wl("		@Override");
		sg.wl("		public ChoiceValue<" + choicetype + "ChoiceDefinition> extractChoiceValue(" + objectclassname
				+ " object) {");
		sg.wl("			return object.get" + choicefield + "();");
		sg.wl("		}");

		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
	
		sg.wl("		@Override");
		sg.wl("		public int getDefaultDelay() {");
		sg.wl("			return " + this.getDefaultDelayForTask() + ";");
		sg.wl("		}");
		sg.wl("}");
		sg.close();

	}

}
