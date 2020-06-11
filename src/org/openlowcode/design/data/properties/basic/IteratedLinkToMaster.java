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
 * A link to master will be iterated if the left object is iterated (link belonging to the
 * left object)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class IteratedLinkToMaster extends Property<IteratedLink> {
	private LinkObjectToMaster<?, ?> linkobjecttomaster;

	/**
	 * creates an iterated link, specifying the link object
	 * 
	 * @param dependentlinkobject dependent link object
	 */
	public IteratedLinkToMaster(LinkObjectToMaster<?, ?> dependentlinkobject) {
		super("ITERATEDLINKTOMASTER");
		this.linkobjecttomaster = dependentlinkobject;
	}

	/**
	 * creates an iterated link, not specifying the link object
	 */
	public IteratedLinkToMaster() {
		super("ITERATEDLINKTOMASTER");

	}

	@Override
	public void controlAfterParentDefinition() {
		if (this.linkobjecttomaster == null)
			this.linkobjecttomaster = (LinkObjectToMaster<?, ?>) parent.getPropertyByName("LINKOBJECTTOMASTER");
		if (linkobjecttomaster == null)
			throw new RuntimeException("IteratedLink can only be added after object has LINKOBJECT property");
		this.addDependentProperty(linkobjecttomaster);
		commonConstructorStuff();
	}

	private void commonConstructorStuff() {
		StoredElement leftfirstiter = new IntegerStoredElement("LFFIRSTITER");
		this.addElement(leftfirstiter, "Created on Iteration", "The iteration of the left object this link was created",
				FIELDDISPLAY_NORMAL, -50, 5);
		StoredElement leftlastiter = new IntegerStoredElement("LFLASTITER");
		this.addElement(leftlastiter, "Removed on Iteration",
				"The iteration of the left object this link was removed or updated", FIELDDISPLAY_NORMAL, -50, 5);
		this.addPropertyGenerics(new PropertyGenerics("LEFTOBJECTFORLINK", linkobjecttomaster.getLeftobjectforlink(),
				linkobjecttomaster.getLeftobjectforlink().getPropertyByName("ITERATED")));
		this.addPropertyGenerics(new PropertyGenerics("RIGHTOBJECTFORLINK", linkobjecttomaster.getRightobjectforlink(),
				linkobjecttomaster.getRightobjectforlink().getPropertyByName("VERSIONED")));

		DataAccessMethod archivecurrentiteration = new DataAccessMethod("ARCHIVETHISITERATION", null, false);
		archivecurrentiteration
				.addInputArgument(new MethodArgument("OBJECTTOARCHIVE", new ObjectArgument("OBJECTTOARCHIVE", parent)));
		archivecurrentiteration
				.addInputArgument(new MethodArgument("LEFTOBJECTOLDITER", new IntegerArgument("LEFTOBJECTOLDITER")));
		this.addDataAccessMethod(archivecurrentiteration);
		DataAccessMethod getlinksfromleft = new DataAccessMethod("GETALLLINKSFROMLEFTITERATION",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromleft.addInputArgument(
				new MethodArgument("LEFTID", new ObjectIdArgument("leftid", this.linkobjecttomaster.getLeftobjectforlink())));
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
				linkobjecttomaster.getUniqueIdentified().getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(generateiterationatcreation);

		MethodAdditionalProcessing archiveandcreatenewiteration = new MethodAdditionalProcessing(true, true,
				linkobjecttomaster.getUniqueIdentified().getDataAccessMethod("UPDATE"));
		this.addMethodAdditionalProcessing(archiveandcreatenewiteration);

		MethodAdditionalProcessing archivebeforedelete = new MethodAdditionalProcessing(true, true,
				linkobjecttomaster.getUniqueIdentified().getDataAccessMethod("DELETE"));
		this.addMethodAdditionalProcessing(archivebeforedelete);

	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		// do nothing
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}
}



