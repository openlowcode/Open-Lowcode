/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.Named;

/**
 * Definition of a sequence. A sequence will provide unique numbers in sequence.
 * It is used for example to provide an automatic number to objects
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SequenceDefinition
		extends
		Named {
	private Module parentmodule;

	/**
	 * creates a sequence with the given name
	 * 
	 * @param name a name that should be a valid java and sql field name
	 */
	public SequenceDefinition(String name) {
		super(name);

	}

	/**
	 * @param parentmodule parent module of the sequence
	 */
	public void setModule(Module parentmodule) {
		this.parentmodule = parentmodule;
	}

	/**
	 * @return get the parent module of the sequence
	 */
	public Module getParentModule() {
		return this.parentmodule;
	}

	/**
	 * generates a class allowing to get the next value of the sequence
	 * 
	 * @param sg     source generation
	 * @param module module
	 * @throws IOException if anything bad happens while writing the field
	 */
	public void generateLaunchSearchActionToFile(SourceGenerator sg, Module module) throws IOException {
		String idclass = StringFormatter.formatForJavaClass(this.getName());

		sg.wl("package " + module.getPath() + ".data.sequence;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.Sequence;");
		sg.wl("");
		sg.wl("public class " + idclass + "Sequence extends Sequence {");
		sg.wl("");
		sg.wl("	private static " + idclass + "Sequence singleton = new " + idclass + "Sequence();");
		sg.wl("	");
		sg.wl("	private " + idclass + "Sequence() {");
		sg.wl("		super(\"" + this.getName().toUpperCase() + "\");");
		sg.wl("	}");
		sg.wl("	public static " + idclass + "Sequence get() {");
		sg.wl("	return singleton;");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("}");

		sg.close();

	}
}