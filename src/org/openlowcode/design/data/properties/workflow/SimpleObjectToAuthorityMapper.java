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

import org.openlowcode.design.access.TotalAuthority;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A simple object to authority mapper will map an object to a predefined total
 * authority
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SimpleObjectToAuthorityMapper
		extends
		ObjectToAuthorityMapper {
	private TotalAuthority authority;

	/**
	 * creates a simple object to authority mapper
	 * 
	 * @param object    parent data object definition
	 * @param authority the authority that will be returned by the mapper
	 */
	public SimpleObjectToAuthorityMapper(DataObjectDefinition object, TotalAuthority authority) {
		super(object);
		this.authority = authority;

	}

	@Override
	public void generateSingleTaskPropertyHelperToFile(SourceGenerator sg, Module module, DataObjectDefinition parent)
			throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(this.getObject().getName());
		sg.wl("package " + module.getPath() + ".data;");
		sg.wl("");
		sg.wl("import org.openlowcode.module.system.data.Authority;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.workflowhelper.ObjectToAuthorityMapper;");
		sg.wl("import org.openlowcode.server.data.workflowhelper.SimpleObjectToAuthorityMapper;");
		sg.wl("import org.openlowcode.server.data.workflowhelper.SimpletaskWorkflowHelper;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.module.system.data.choice.DelaytypeChoiceDefinition;");
		sg.wl("");
		sg.wl("public class " + objectclass + "SimpleTaskWorkflowHelper extends SimpletaskWorkflowHelper {");
		sg.wl("	private static " + objectclass + "SimpleTaskWorkflowHelper singleton;");
		sg.wl("	private static DataObjectId<Authority> authority;");
		sg.wl("	@Override");
		sg.wl("	public int getDefaultDelay(int arg0)  {");
		sg.wl("		return " + this.getDefaultDelayForTask() + ";");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ObjectToAuthorityMapper<" + objectclass + "> getSingleAuthorityMapper()  {");
		sg.wl("		if (authority==null) authority = Authority.getuniqueobjectbynumber(\"" + authority.getName()
				+ "\",null).getId();");
		sg.wl("		return new SimpleObjectToAuthorityMapper<" + objectclass + ">(authority);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("");
		sg.wl("		@Override");
		sg.wl("		public ChoiceValue<DelaytypeChoiceDefinition> sendMailOnTask()  {");
		sg.wl("			return DelaytypeChoiceDefinition.get()." + this.getEmailDelayType() + ";");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public String getTaskMessage()  {");
		sg.wl("		return \"" + this.getMessage() + "\";");
		sg.wl("	}");
		sg.wl("	public static " + objectclass + "SimpleTaskWorkflowHelper get()  {");
		sg.wl("		if (singleton==null) {");
		sg.wl("			" + objectclass + "SimpleTaskWorkflowHelper temp = new " + objectclass
				+ "SimpleTaskWorkflowHelper();");
		sg.wl("			singleton = temp;");
		sg.wl("		}");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("	private " + objectclass + "SimpleTaskWorkflowHelper()  {");
		sg.wl("		super(SimpletaskWorkflowHelper.EMAIL_NOW);");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");
		sg.close();
	}

}
