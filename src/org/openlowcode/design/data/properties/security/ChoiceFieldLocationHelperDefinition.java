/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.openlowcode.design.access.ModuleDomain;
import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A location helper that will put the object in a domain based on the content
 * of a choice field
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ChoiceFieldLocationHelperDefinition
		extends
		LocationHelperDefinition {

	private ModuleDomain defaultdomain;
	private ChoiceField choicefield;
	private HashMap<ChoiceValue, ModuleDomain> mappings;

	/**
	 * creates a choice field location helper configured with default domain
	 * 
	 * @param parent        parent data object
	 * @param defaultdomain the domain to choose if no mapping exists for this
	 *                      choice
	 * @param choicefield   the choice field to use on an object
	 */
	public ChoiceFieldLocationHelperDefinition(
			DataObjectDefinition parent,
			ModuleDomain defaultdomain,
			ChoiceField choicefield) {
		super(parent);
		this.defaultdomain = defaultdomain;
		this.choicefield = choicefield;
		mappings = new HashMap<ChoiceValue, ModuleDomain>();
		if (!parent.isFieldInObject(choicefield))
			throw new RuntimeException(
					"Field " + choicefield.getName() + " is not a field of object " + parent.getName());
	}

	/**
	 * adds a specific mapping between a choice value and a domain
	 * 
	 * @param choice choice value in the choice field for the object
	 * @param domain domain to map to when the field has this choice value
	 */
	public void addChoiceMapping(ChoiceValue choice, ModuleDomain domain) {
		if (mappings.get(choice) != null)
			throw new RuntimeException("Duplicate choice value for mapping " + choice.getName());
		mappings.put(choice, domain);
	}

	@Override
	public void generateLocationHelper(SourceGenerator sg, Module module) throws IOException {
		String objectclassname = StringFormatter.formatForJavaClass(this.getParent().getName());

		sg.wl("package " + module.getPath() + ".data;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.properties.security.ChoiceFieldLocationHelper;");
		sg.wl("import " + module.getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(choicefield.getChoice().getName()) + "ChoiceDefinition;");
		sg.wl("");
		sg.wl("public class " + objectclassname + "LocationHelper extends ChoiceFieldLocationHelper<" + objectclassname
				+ "," + StringFormatter.formatForJavaClass(choicefield.getChoice().getName()) + "ChoiceDefinition> {");
		sg.wl("	public " + objectclassname + "LocationHelper() {");
		sg.wl("		super(\"" + module.getCode() + "_" + defaultdomain.getName() + "\",");
		sg.wl("				(o -> o.get" + StringFormatter.formatForJavaClass(choicefield.getName()) + "()),");
		sg.wl("				new Function<String,String>() {");
		sg.wl("");
		sg.wl("					@Override");
		sg.wl("					public String apply(String t) {");
		Iterator<Entry<ChoiceValue, ModuleDomain>> mappingiterator = mappings.entrySet().iterator();
		while (mappingiterator.hasNext()) {
			Entry<ChoiceValue, ModuleDomain> thismapping = mappingiterator.next();

			sg.wl("						if (t.equals(\"" + thismapping.getKey().getName().toUpperCase()
					+ "\")) return \"" + module.getCode() + "_" + thismapping.getValue().getName().toUpperCase()
					+ "\";");
		}
		sg.wl("						return null;");
		sg.wl("					}");
		sg.wl("		});");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	private static " + objectclassname + "LocationHelper singleton;");
		sg.wl("	public static " + objectclassname + "LocationHelper get() {");
		sg.wl("		if (singleton==null) {");
		sg.wl("			" + objectclassname + "LocationHelper temp = new " + objectclassname + "LocationHelper();");
		sg.wl("			singleton = temp;");
		sg.wl("		}");
		sg.wl("		return singleton;");
		sg.wl("	}");
		sg.wl("}");
		sg.close();

	}

}
