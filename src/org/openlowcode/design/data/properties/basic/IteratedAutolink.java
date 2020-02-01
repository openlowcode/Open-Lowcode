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
 * The iterated autolink property records the iteration of the left object the
 * link was created, and the iteration of the left object on which the link was
 * removed
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IteratedAutolink
		extends
		Property<IteratedAutolink> {
	private AutolinkObject<?> autolinkobject;

	/**
	 * creates an iterated auto-link with the autolink property of the link object
	 * given as reference
	 * 
	 * @param dependentlinkobject dependent property auto-link on the object holding
	 *                            the auto-link
	 */
	public IteratedAutolink(AutolinkObject<?> dependentlinkobject) {
		super("ITERATEDAUTOLINK");
		this.autolinkobject = dependentlinkobject;

	}

	/**
	 * 
	 */
	public IteratedAutolink() {
		super("ITERATEDAUTOLINK");

	}

	@Override
	public void controlAfterParentDefinition() {
		if (this.autolinkobject == null)
			this.autolinkobject = (AutolinkObject<?>) parent.getPropertyByName("LINKOBJECT");
		if (autolinkobject == null)
			throw new RuntimeException("IteratedLink can only be added after object has LINKOBJECT property");
		this.addDependentProperty(autolinkobject);
		commonConstructorStuff();
	}

	/**
	 * 
	 */
	public void commonConstructorStuff() {
		StoredElement leftfirstiter = new IntegerStoredElement("LFFIRSTITER");
		this.addElement(leftfirstiter, "Created on Iteration", "The iteration of the left object this link was created",
				FIELDDISPLAY_NORMAL, -50, 5);
		StoredElement leftlastiter = new IntegerStoredElement("LFLASTITER");
		this.addElement(leftlastiter, "Removed on Iteration",
				"The iteration of the left object this link was removed or updated", FIELDDISPLAY_NORMAL, -50, 5);
		this.addPropertyGenerics(new PropertyGenerics("LEFTOBJECTFORLINK", autolinkobject.getObjectforlink(),
				autolinkobject.getObjectforlink().getPropertyByName("ITERATED")));
		DataAccessMethod archivecurrentiteration = new DataAccessMethod("ARCHIVETHISITERATION", null, false);
		archivecurrentiteration
				.addInputArgument(new MethodArgument("OBJECTTOARCHIVE", new ObjectArgument("OBJECTTOARCHIVE", parent)));
		archivecurrentiteration
				.addInputArgument(new MethodArgument("LEFTOBJECTOLDITER", new IntegerArgument("LEFTOBJECTOLDITER")));
		this.addDataAccessMethod(archivecurrentiteration);
		DataAccessMethod getlinksfromleft = new DataAccessMethod("GETALLLINKSFROMLEFTITERATION",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromleft.addInputArgument(
				new MethodArgument("LEFTID", new ObjectIdArgument("leftid", this.autolinkobject.getObjectforlink())));
		getlinksfromleft.addInputArgument(new MethodArgument("LEFTITERATION", new IntegerArgument("ITERATION")));
		this.addDataAccessMethod(getlinksfromleft);

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
				autolinkobject.getUniqueIdentified().getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(generateiterationatcreation);

		MethodAdditionalProcessing archiveandcreatenewiteration = new MethodAdditionalProcessing(true, true,
				autolinkobject.getUniqueIdentified().getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(archiveandcreatenewiteration);

		MethodAdditionalProcessing archivebeforedelete = new MethodAdditionalProcessing(true, true,
				autolinkobject.getUniqueIdentified().getDataAccessMethod("DELETE"));
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
