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
import org.openlowcode.design.data.Property;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * Defines a custom loader for the flat file / spreadsheet loader
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CustomLoader
		extends
		Property<CustomLoader> {

	/**
	 * @return if a local loader should be generated
	 * @since 1.15
	 */
	public boolean getLocalLoader() {
		return this.localloader;
	}
	
	private boolean localloader;
	
	/**
	 * Creates a custom loader for the data object
	 * 
	 * @param name a unique name amongst all the custom loaders of this data object
	 *             (should be a valid java attribute name)
	 */
	public CustomLoader(String name) {
		this(name, false);
	}

	/**
	 * creates a custom loader with the possibility to generate a local loader
	 * 
	 * @param name        a unique name amongst all the custom loaders of this data
	 *                    object (should be a valid java attribute name)
	 * @param localloader if true, generate a local loader for the object
	 * @since 1.15
	 */
	public CustomLoader(String name, boolean localloader) {
		super(name, "CUSTOMLOADER");
		this.localloader = localloader;
	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();

		return dependencies;
	}

	@Override
	public void setFinalSettings() {
	}

	@Override
	public String getJavaType() {
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public void generatePropertyHelperToFile(SourceGenerator sg, Module module) throws IOException {

		sg.wl("package " + this.getParent().getOwnermodule().getPath() + ".data;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.properties.CustomloaderDefinition.CustomloaderHelper;");
		sg.wl("");
		sg.wl("public abstract class Abs" + StringFormatter.formatForJavaClass(this.getParent().getName())
				+ StringFormatter.formatForJavaClass(this.getInstancename())
				+ "CustomloaderHelper implements CustomloaderHelper<"
				+ StringFormatter.formatForJavaClass(this.getParent().getName()) + "> {");
		sg.wl("");
		sg.wl("	public static Abs" + StringFormatter.formatForJavaClass(this.getParent().getName())
				+ StringFormatter.formatForJavaClass(this.getInstancename()) + "CustomloaderHelper get() {");
		sg.wl("		return new " + this.getParent().getOwnermodule().getPath() + ".utility."
				+ StringFormatter.formatForJavaClass(this.getParent().getName())
				+ StringFormatter.formatForJavaClass(this.getInstancename()) + "CustomloaderHelper();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	@Override
	public String getPropertyHelperName() {
		return "Abs" + StringFormatter.formatForJavaClass(this.getParent().getName())
				+ StringFormatter.formatForJavaClass(this.getInstancename()) + "CustomloaderHelper";
	}

	@Override
	public boolean isPropertyHelperTransient() {
		return true;
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
