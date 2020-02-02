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
import org.openlowcode.design.data.stringpattern.PatternElement;
import org.openlowcode.design.data.stringpattern.StringPattern;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * If this business rule is added to the
 * {@link org.openlowcode.design.data.properties.basic.Named} property, the name
 * is automatically generated at object creation and update.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AutoNamingRule
		extends
		PropertyBusinessRule<Named> {
	private Named named;
	private StringPattern pattern;

	/**
	 * creates an automatic naming rule for the named property
	 * 
	 * @param pattern String pattern
	 * @param named   named property of the object
	 */
	public AutoNamingRule(StringPattern pattern, Named named) {
		super("AUTONAMINGRULE", false);
		this.pattern = pattern;
		this.named = named;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String objectname = StringFormatter.formatForJavaClass(named.getParent().getName());
		sg.wl("		AutonamingRule<" + objectname + "> rule = new AutonamingRule<" + objectname + ">() {");
		sg.wl("");
		sg.wl("			@Override");
		sg.wl("			public String generateName(" + objectname + " object)  {");
		sg.wl("				StringBuffer sequence = new StringBuffer();");
		for (int i = 0; i < pattern.getElementNumber(); i++) {
			sg.wl("				sequence.append(" + pattern.getElement(i).generateSource() + ");");
		}
		sg.wl("				return sequence.toString();");
		sg.wl("			}");

		sg.wl("		};");
		sg.wl("		named.addAutonamingrule(rule);");

	}

	@Override
	public String[] getImportstatements() {
		ArrayList<String> importstatements = new ArrayList<String>();
		importstatements.add("import org.openlowcode.server.data.properties.constraints.AutonamingRule;");
		for (int i = 0; i < pattern.getElementNumber(); i++) {
			PatternElement thiselement = pattern.getElement(i);
			for (int j = 0; j < thiselement.generateImport().length; j++) {
				importstatements.add(thiselement.generateImport()[j]);
			}
		}
		return importstatements.toArray(new String[0]);
	}

}
