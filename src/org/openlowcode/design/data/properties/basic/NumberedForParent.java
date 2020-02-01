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

import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * The Numbered For Parent property modified the 'Numbered' property and
 * performs the unicity check of the number only amongst the children of the
 * same parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class NumberedForParent
		extends
		Property<NumberedForParent> {
	private LinkedToParent<?> linkedtoparent;
	private Numbered numbered;

	/**
	 * creates a NumberedForParent property
	 * 
	 * @param linkedtoparent related linked to parent property
	 */
	public NumberedForParent(LinkedToParent<?> linkedtoparent) {
		super("NUMBEREDFORPARENT");
		this.linkedtoparent = linkedtoparent;
	}

	@Override
	public String[] getPropertyInitMethod() {
		return null;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		return null;
	}

	@Override
	public void controlAfterParentDefinition() {
		if (linkedtoparent.getParent() != this.getParent())
			throw new RuntimeException(
					" Parent object " + (this.getParent() != null ? this.getParent().getName() : "NULL")
							+ " for NUMBEREDFORPARENT and parent for LINKEDTOPARENT  "
							+ (linkedtoparent.getParent() != null ? linkedtoparent.getParent().getName() : "NULL")
							+ " are not consistent.");
		this.numbered = (Numbered) parent.getPropertyByName("NUMBERED");
		this.addDependentProperty(numbered);
		this.addDependentProperty(linkedtoparent);
		this.addPropertyGenerics(new PropertyGenerics("PARENTOBJECTFORLINK", linkedtoparent.getParentObjectForLink(),
				new UniqueIdentified()));

		DataAccessMethod getnumberforparent = new DataAccessMethod("GETOBJECTBYNUMBERFORPARENT",
				new ArrayArgument(new ObjectArgument("OBJECT", parent)), false);
		getnumberforparent.addInputArgument(new MethodArgument("NR", new StringArgument("NR", 64)));
		getnumberforparent.addInputArgument(new MethodArgument("PARENTID",
				new ObjectIdArgument("PARENTID", this.linkedtoparent.getParentObjectForLink())));
		this.addDataAccessMethod(getnumberforparent);

		DataAccessMethod getparentfornumber = new DataAccessMethod("GETPARENTIDFORNUMBER",
				new ObjectIdArgument("PARENTID", this.linkedtoparent.getParentObjectForLink()), false);
		getparentfornumber.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getparentfornumber);
	}

	@Override
	public void setFinalSettings() {
		this.addMethodAdditionalProcessing(new MethodAdditionalProcessing(true, false,
				linkedtoparent.getDataAccessMethod("SETPARENTWITHOUTUPDATE")));
		this.addMethodAdditionalProcessing(
				new MethodAdditionalProcessing(true, false, linkedtoparent.getDataAccessMethod("SETPARENT")));

	}

	@Override
	public String getJavaType() {
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}
}
