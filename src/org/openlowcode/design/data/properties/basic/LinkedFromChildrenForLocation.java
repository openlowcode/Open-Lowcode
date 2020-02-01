package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * a linked from children for location ensures a child is located in the same
 * doman as its parent
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> type of the child object for link
 */
public class LinkedFromChildrenForLocation<E extends DataObjectDefinition>
		extends
		Property<LinkedFromChildrenForLocation<E>> {

	private LinkedFromChildren thisobjectlinkedfromchildren;
	@SuppressWarnings("unused")
	private E childobjectforlink;
	private LinkedToParent<?> originobjectproperty;
	private Located locatedforobject;

	/**
	 * creates a new linked from children for location property
	 * 
	 * @param name name of the property (should be a valid java name)
	 * @param childobjectforlink definition of the child object for link
	 * @param thisobjectlinkedfromchildren linked from  children for the current parent object
	 * @param originobjectproperty linked to parent property on the child object
	 * @param locatedforobject located property on the parent object
	 */
	public LinkedFromChildrenForLocation(
			String name,
			E childobjectforlink,
			LinkedFromChildren thisobjectlinkedfromchildren,
			LinkedToParent<?> originobjectproperty,
			Located locatedforobject) {
		super(name, "LINKEDFROMCHILDRENFORLOCATION");
		this.addPropertyGenerics(new PropertyGenerics("CHILDOBJECTFORLINK", childobjectforlink, originobjectproperty));
		this.thisobjectlinkedfromchildren = thisobjectlinkedfromchildren;
		this.childobjectforlink = childobjectforlink;
		this.originobjectproperty = originobjectproperty;
		this.locatedforobject = locatedforobject;
	}

	@Override
	public void controlAfterParentDefinition() {
		this.addDependentProperty(locatedforobject);
		this.addDependentProperty(thisobjectlinkedfromchildren);
		this.addMethodAdditionalProcessing(
				new MethodAdditionalProcessing(false, locatedforobject.getDataAccessMethod("SETLOCATION")));

	}

	@Override
	public String[] getPropertyInitMethod() {
		String[] returnvalues = new String[0];
		return returnvalues;
	}

	@Override
	public String[] getPropertyExtractMethod() {
		String[] returnvalues = new String[0];
		return returnvalues;
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(originobjectproperty.getParent());
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

}
