/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.argument;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * an argument that holds a timestamp
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimestampArgument
		extends
		ArgumentContent {
	private boolean defaultdisplayusehour;

	/**
	 * create a TimestampArgument for the given name
	 * 
	 * @param name name of the argument, should be unique amongst input and output
	 *             argument, should be a valid java field name
	 */
	public TimestampArgument(String name) {
		super(name, false);
		imports = new ArrayList<String>();
		this.defaultdisplayusehour = true;
	}

	/**
	 * @param name                  name of the argument, should be unique amongst
	 *                              input and output argument, should be a valid
	 *                              java field name
	 * @param label                 plain default language description of the
	 *                              argument
	 * @param defaultdisplayusehour if true, the hour and minutes are shown on the
	 *                              GUI
	 */
	public TimestampArgument(String name, String label, boolean defaultdisplayusehour) {
		this(name);
		this.setDisplaylabel(label);
		this.defaultdisplayusehour = defaultdisplayusehour;
	}

	/**
	 * @return if true, by default, hours and minutes are shown on the GUI
	 */
	public boolean isDefaultDisplayUseHour() {
		return this.defaultdisplayusehour;
	}

	private ArrayList<String> imports;

	@Override
	public String getType() {
		return "Date";
	}

	@Override
	public ArgumentContent generateCopy(String newname) {
		return null;
	}

	@Override
	public String getGenericDataEltName() {

		return "DateDataElt";
	}

	@Override
	public String getPreciseDataEltTypeName() {

		return "DateDataEltType";
	}

	@Override
	public boolean needDefinitionForInit() {
		return false;
	}

	@Override
	public String getPreciseDataEltName() {
		return "DateDataElt";
	}

	@Override
	public void writeImports(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import java.util.Date;");
	}

	@Override
	public ArrayList<String> getImports() {
		return imports;
	}

	@Override
	public String initblank() {
		return "null";
		// return "new Date()";
	}

	@Override
	public DataObjectDefinition getMasterObject() {

		return null;
	}

}
