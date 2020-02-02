package org.openlowcode.design.data;

import org.openlowcode.design.generation.StringFormatter;

public class ObjectIdStoredElement extends StoredElement {
	private DataObjectDefinition referencedobject;
	public ObjectIdStoredElement(String suffix,DataObjectDefinition referencedobject) {
		super(suffix);
		this.referencedobject = referencedobject;
	}
	public DataObjectDefinition getReferencedObject() {
		return referencedobject;
	}
	@Override
	public String getJavaFieldName() {
		if (referencedobject!=null)
		return "DataObjectId<"+StringFormatter.formatForJavaClass(referencedobject.getName())+">";
		return "DataObjectId";
		
	}
}
