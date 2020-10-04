/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
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

import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.IntegerStoredElement;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.IntegerArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * An iterated companion is a data object belonging to a main object. This
 * includes:
 * <ul>
 * <li>Link objets that belong to the left objet</li>
 * <li>Companion objects for sub-types</li>
 * </ul>
 * The property stores the iterations from the main object for which the
 * companion is valid. <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.HasId}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class IteratedCompanion
		extends
		Property<Iterated> {
	private String prefix;
	private String context;

	/**
	 * A property for an object with history managed with iteration of another
	 * 'master' object.
	 * 
	 * @param prefix  a short prefix for the fields (typically 'LF' for left, 'RG'
	 *                for right, 'PR' for parent
	 * @param context a text explaining the relation of this object with the object
	 *                having the master iteration. E.g. 'left object for link',
	 *                'parent'
	 * @since 1.13
	 */
	public IteratedCompanion(String prefix, String context) {
		super(prefix, "ITERATEDCOMPANION");
		this.prefix = prefix.toUpperCase();
		this.context = context;
	}

	@Override
	public void controlAfterParentDefinition() {
		StoredElement leftfirstiter = new IntegerStoredElement(prefix + "FIRSTITER");
		this.addElement(leftfirstiter, "Created on Iteration",
				"First iteration of " + context + " where this data is valid", FIELDDISPLAY_NORMAL, -50, 5);
		StoredElement leftlastiter = new IntegerStoredElement(prefix.toUpperCase() + "LASTITER");
		this.addElement(leftlastiter, "Removed on Iteration",
				"Last iteration of " + context + " where this data is valid", FIELDDISPLAY_NORMAL, -50, 5);
		DataAccessMethod archivecurrentiteration = new DataAccessMethod("ARCHIVETHISITERATION", null, false);
		archivecurrentiteration
				.addInputArgument(new MethodArgument("OBJECTTOARCHIVE", new ObjectArgument("OBJECTTOARCHIVE", parent)));
		archivecurrentiteration.addInputArgument(
				new MethodArgument(prefix + "OBJECTOLDITER", new IntegerArgument(prefix + "OBJECTOLDITER")));
		this.addDataAccessMethod(archivecurrentiteration);

	}

	@Override
	public String[] getPropertyInitMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFinalSettings() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getJavaType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		// TODO Auto-generated method stub

	}
}
