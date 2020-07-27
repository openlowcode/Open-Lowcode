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
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of parent object
 */
public class MultiDimensionChild<E extends DataObjectDefinition>
		extends
		Property<MultiDimensionChild<E>> {

	private LinkedToParent<E> linkedtoparent;
	private Field firstaxisvalue;
	private Field[] secondaxisvalues;
	private Field[] payloadvalues;

	public Field getFirstAxisValue() {
		return firstaxisvalue;
	}
	
	public Field[] getSecondAxisValue() {
		return this.secondaxisvalues;
	}
	
	public LinkedToParent<E> getLinkedToParent() {
		return this.linkedtoparent;
	}
	
	public MultiDimensionChild(LinkedToParent<E> linkedtoparent, Field firstaxisvalue, Field[] secondaxisvalues,Field[] payloadvalues) {
		super("MULTIDIMENSIONCHILD");
		this.linkedtoparent = linkedtoparent;
		this.firstaxisvalue = firstaxisvalue;
		this.secondaxisvalues = secondaxisvalues;
		this.payloadvalues = payloadvalues;
	}

	@Override
	public void controlAfterParentDefinition() {
		if (this.getParent() != linkedtoparent.getParent())
			throw new RuntimeException("Inconsistent parent, LinkedToParent parent = " + linkedtoparent.getParent()
					+ ", Multidimensionchild parent = " + this.getParent());
		if (firstaxisvalue == null)
			throw new RuntimeException("First axis value cannot be null");
		checkField(firstaxisvalue);
		if (secondaxisvalues != null)
			for (int i = 0; i < secondaxisvalues.length; i++)
				checkField(secondaxisvalues[i]);
		this.addDependentProperty(linkedtoparent);
		this.addPropertyGenerics(new PropertyGenerics("PARENTOBJECTFORLINK", linkedtoparent.getParentObjectForLink(),
				new UniqueIdentified()));
		this.addExternalObjectProperty(linkedtoparent.getParentObjectForLink(), new HasMultiDimensionalChild(this.linkedtoparent.getInstancename() + "for" + parent.getName().toLowerCase(),this.getParent(), this));
		DataAccessMethod setparentwithoutupdate = new DataAccessMethod("SETMULTIDIMENSIONPARENTIDWITHOUTUPDATE", null, false);
		setparentwithoutupdate
				.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setparentwithoutupdate.addInputArgument(
				new MethodArgument("parent", new ObjectIdArgument("parentobject", this.linkedtoparent.getParentObjectForLink())));
		this.addDataAccessMethod(setparentwithoutupdate);
		if (secondaxisvalues!=null) if (secondaxisvalues.length>0)
			if (this.payloadvalues!=null) if (this.payloadvalues.length==1) {
				String[] linecriteria = new String[secondaxisvalues.length];
				for (int i=0;i<secondaxisvalues.length;i++) linecriteria[i] = secondaxisvalues[i].getName();
				this.linkedtoparent.setReverseTreeGrid(this.firstaxisvalue.getName(),linecriteria,this.payloadvalues[0].getName(),new String[0],new String[0]);
			
		}
	}

	private void checkField(Field field) {
		if (field == null)
			throw new RuntimeException("Field cannot be null");
		if (field.getParentObject() != this.getParent())
			throw new RuntimeException("Inconsistent parent, field " + field.getName() + " parent = "
					+ field.getParentObject() + ", multidimensionchild parent = " + this.getParent());
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
