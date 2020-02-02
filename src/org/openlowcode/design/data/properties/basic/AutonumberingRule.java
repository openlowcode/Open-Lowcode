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
import org.openlowcode.design.data.stringpattern.ConstantElement;
import org.openlowcode.design.data.stringpattern.PatternElement;
import org.openlowcode.design.data.stringpattern.SequenceElement;
import org.openlowcode.design.data.stringpattern.StringPattern;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * /** If this business rule is added to the
 * {@link org.openlowcode.design.data.properties.basic.Numbered} property, the
 * name is automatically generated at object creation and update.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class AutonumberingRule
		extends
		PropertyBusinessRule<Numbered> {
	private StringPattern pattern;
	private Numbered numbered;

	/**
	 * Creates an auto-numbering rule
	 * 
	 * @param name     name of the auto-numbering rule
	 * @param pattern  string pattern
	 * @param numbered numbered property on the object
	 */
	public AutonumberingRule(String name, StringPattern pattern, Numbered numbered) {
		super(name, false);
		this.pattern = pattern;
		this.numbered = numbered;
	}

	@Override
	public void writeInitialization(SourceGenerator sg) throws IOException {
		String objectname = StringFormatter.formatForJavaClass(numbered.getParent().getName());
		boolean isorderedasinteger = true;
		int offset = 0;
		for (int i = 0; i < pattern.getElementNumber() - 1; i++) {
			PatternElement thispattern = pattern.getElement(i);
			if (thispattern instanceof ConstantElement) {
				ConstantElement element = (ConstantElement) thispattern;
				offset += element.getLength();
			} else {
				isorderedasinteger = false;
			}
		}
		PatternElement lastelement = pattern.getElement(pattern.getElementNumber() - 1);
		if (lastelement instanceof SequenceElement) {

		} else {
			isorderedasinteger = false;
		}

		sg.wl("		AutonumberingRule<" + objectname + "> rule = new AutonumberingRule<" + objectname + ">() {");
		sg.wl("");
		sg.wl("			@Override");
		sg.wl("			public boolean orderedAsNumber() {");
		sg.wl("					return " + isorderedasinteger + ";");
		sg.wl("				}");
		sg.wl("");
		sg.wl("			@Override");
		sg.wl("			public int getNumberOffset() {");
		sg.wl("					return " + offset + ";");
		sg.wl("				}");
		sg.wl("");

		sg.wl("			@Override");

		sg.wl("			public String generateNumber(" + objectname + " object)  {");
		sg.wl("				StringBuffer sequence = new StringBuffer();");
		for (int i = 0; i < pattern.getElementNumber(); i++) {
			sg.wl("				sequence.append(" + pattern.getElement(i).generateSource() + ");");
		}
		sg.wl("				return sequence.toString();");
		sg.wl("			}");

		sg.wl("		};");
		sg.wl("		numbered.addAutonumberingrule(rule);");

	}

	@Override
	public String[] getImportstatements() {
		ArrayList<String> importstatements = new ArrayList<String>();
		importstatements.add("import org.openlowcode.server.data.properties.constraints.AutonumberingRule;");
		for (int i = 0; i < pattern.getElementNumber(); i++) {
			PatternElement thiselement = pattern.getElement(i);
			for (int j = 0; j < thiselement.generateImport().length; j++) {
				importstatements.add(thiselement.generateImport()[j]);
			}
		}
		return importstatements.toArray(new String[0]);
	}

}
