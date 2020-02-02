package org.openlowcode.design.data;

import org.openlowcode.design.generation.StringFormatter;

public class ObjectMasterIdStoredElement extends StoredElement {
	private DataObjectDefinition referencedobject;
	public ObjectMasterIdStoredElement(String suffix,DataObjectDefinition referencedobject) {
		super(suffix);
		this.referencedobject = referencedobject;
	}
	public DataObjectDefinition getReferencedObject() {
		return referencedobject;
	}
	@Override
	public String getJavaFieldName() {
		if (referencedobject!=null)
		return "DataObjectMasterId<"+StringFormatter.formatForJavaClass(referencedobject.getName())+">";
		return "DataObjectMasterId<DataObject>";
		
	}
}
