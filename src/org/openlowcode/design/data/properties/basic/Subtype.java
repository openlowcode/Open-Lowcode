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

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * A data object with a subtype can:<ul>
 * <li>have specific rule per subtype</li>
 * <li>have a companion object for a group of subtypes, adding fields and properties</li>
 * <li></li></ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class Subtype<E extends DataObjectDefinition>
		extends
		Property<Subtype<E>> {

	private SimpleChoiceCategory subtypes;
	private UniqueIdentified uniqueidentified;

	public Subtype(SimpleChoiceCategory subtypes) {
		super("SUBTYPE");
		if (subtypes==null) throw new RuntimeException("Subtypes cannot be null");
		this.subtypes=subtypes;
		this.addChoiceCategoryHelper("LISTOFSUBTYPES", subtypes);
	}
	
	
	
	@Override
	public void controlAfterParentDefinition() {
		uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
	}



	public SimpleChoiceCategory getSubTypes() {
		return this.subtypes;
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
		sg.wl("import " + subtypes.getParentModule().getPath() + ".data.choice."
				+ StringFormatter.formatForJavaClass(subtypes.getName()) + "ChoiceDefinition;");
		
	}

}
