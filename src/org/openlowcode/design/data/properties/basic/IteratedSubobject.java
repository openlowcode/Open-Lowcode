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
import org.openlowcode.design.data.IntegerStoredElement;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.IntegerArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * This property ensured children objects that are subobjects are iterated when
 * the parent is iterated
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IteratedSubobject
		extends
		Property<IteratedSubobject> {
	private LinkedToParent<?> linkedtoparent;

	/**
	 * creates an iterated subobject for the property
	 * 
	 * @param dependentlinkobject linked to parent linked to the iterated parent
	 */
	public IteratedSubobject(LinkedToParent<?> dependentlinkobject) {
		super("ITERATEDSUBOBJECT");
		this.linkedtoparent = dependentlinkobject;

	}

	/**
	 * creates an iterated subobject property without linked to parent
	 */
	public IteratedSubobject() {
		super("ITERATEDSUBOBJECT");

	}

	@Override
	public void controlAfterParentDefinition() {
		if (linkedtoparent == null)
			this.linkedtoparent = (LinkedToParent<?>) parent.getPropertyByName("LINKEDTOPARENT");
		if (linkedtoparent == null)
			throw new RuntimeException("IteratedLink can only be added after object has LINKOBJECT property");
		this.addDependentProperty(linkedtoparent);
		commonConstructorStuff();
	}

	public void commonConstructorStuff() {
		StoredElement parentfirstiter = new IntegerStoredElement("PRFIRSTITER");
		this.addElement(parentfirstiter, "Created on Iteration",
				"The iteration of the left object this link was created", FIELDDISPLAY_NORMAL, -50, 5);
		StoredElement parentlastiter = new IntegerStoredElement("PRLASTITER");
		this.addElement(parentlastiter, "Removed on Iteration",
				"The iteration of the left object this link was removed or updated", FIELDDISPLAY_NORMAL, -50, 5);
		this.addPropertyGenerics(new PropertyGenerics("PARENT", linkedtoparent.getParentObjectForLink(),
				linkedtoparent.getParentObjectForLink().getPropertyByName("ITERATED")));
		DataAccessMethod archivecurrentiteration = new DataAccessMethod("ARCHIVETHISITERATION", null, false);
		archivecurrentiteration
				.addInputArgument(new MethodArgument("OBJECTTOARCHIVE", new ObjectArgument("OBJECTTOARCHIVE", parent)));
		archivecurrentiteration
				.addInputArgument(new MethodArgument("PARENTOLDITER", new IntegerArgument("PARENTOLDITER")));
		this.addDataAccessMethod(archivecurrentiteration);
		DataAccessMethod getlinksfromparentiteration = new DataAccessMethod("GETALLLINKSFROMPARENTITERATION",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromparentiteration.addInputArgument(new MethodArgument("PARENTID",
				new ObjectIdArgument("leftid", this.linkedtoparent.getParentObjectForLink())));
		getlinksfromparentiteration
				.addInputArgument(new MethodArgument("PARENTITERATION", new IntegerArgument("ITERATION")));
		this.addDataAccessMethod(getlinksfromparentiteration);

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
	public void setFinalSettings() {
		MethodAdditionalProcessing generateiterationatcreation = new MethodAdditionalProcessing(true,
				linkedtoparent.getUniqueIdentified().getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(generateiterationatcreation);

		MethodAdditionalProcessing archiveandcreatenewiteration = new MethodAdditionalProcessing(true, true,
				linkedtoparent.getUniqueIdentified().getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(archiveandcreatenewiteration);

		MethodAdditionalProcessing archivebeforedelete = new MethodAdditionalProcessing(true, true,
				linkedtoparent.getUniqueIdentified().getDataAccessMethod("DELETE"));
		this.addMethodAdditionalProcessing(archivebeforedelete);

	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}
}
