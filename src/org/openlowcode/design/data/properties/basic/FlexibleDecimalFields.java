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

import org.openlowcode.design.data.BigDecimalArgument;
import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * The property flexible decimal fields allows to define a list of decimal
 * fields at runtime. This property is only available on objects that are not
 * stored
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class FlexibleDecimalFields
		extends
		Property<FlexibleDecimalFields> {

	/**
	 * create a flexible decimal fields property on the data object
	 */
	public FlexibleDecimalFields() {
		super("FLEXIBLEDECIMALFIELDS");
		this.setDynamicDefinitionHelper();
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
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		return null;
	}

	@Override
	public void setFinalSettings() {
		if (this.getParent().getPropertyByName("STOREDOBJECT") != null)
			throw new RuntimeException("FlexibleDecimalFields property can only be set on a non-persisted object");

	}

	@Override
	public String getJavaType() {
		return "#ERROR#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
	}

	@Override
	public void controlAfterParentDefinition() {
		DataAccessMethod addflexiblevalue = new DataAccessMethod("ADDFLEXIBLEDECIMALVALUE", null, false, false);
		addflexiblevalue.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		addflexiblevalue.addInputArgument(new MethodArgument("NAME", new StringArgument("NAME", 64)));
		addflexiblevalue.addInputArgument(new MethodArgument("VALUE", new BigDecimalArgument("VALUE")));
		this.addDataAccessMethod(addflexiblevalue);

		DataAccessMethod getflexiblevalue = new DataAccessMethod("GETFLEXIBLEDECIMALVALUE",
				new BigDecimalArgument("VALUE"), false, false);
		getflexiblevalue.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		getflexiblevalue.addInputArgument(new MethodArgument("NAME", new StringArgument("NAME", 64)));
		this.addDataAccessMethod(getflexiblevalue);

	}

}
