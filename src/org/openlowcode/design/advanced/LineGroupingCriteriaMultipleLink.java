package org.openlowcode.design.advanced;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * This feature will group criteria by linked object. As an object may be linked
 * to several linked objects, classification is by the combination of linked
 * objects (e.g. : A - B - A,B - C - C,A)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public class LineGroupingCriteriaMultipleLink
		extends
		LineGroupingCriteria {

	DataObjectDefinition uniquelinkobject;
	LinkObject<?, ?> linkobject;

	/**
	 * create a line grouping criteria for a multiple link. Objects will be regrouped
	 * if they are linked to the same combination of right objects
	 * 
	 * @param uniquelinkobject the unique link having the node object as left object
	 */
	public LineGroupingCriteriaMultipleLink(DataObjectDefinition uniquelinkobject) {
		this.uniquelinkobject = uniquelinkobject;
		if (uniquelinkobject == null)
			throw new RuntimeException("Uniquelinkobject cannot be null");
		if (uniquelinkobject.getPropertyByName("LINKOBJECT") == null)
			throw new RuntimeException("Object " + this.uniquelinkobject.getName() + " is not a link object");
		linkobject = (LinkObject<?, ?>) uniquelinkobject.getPropertyByName("LINKOBJECT");
	}

	@Override
	public DataObjectDefinition getObject() {
		return linkobject.getLeftobjectforlink();
	}

	@Override
	public boolean hasDataGathering() {
		return true;
	}

	@Override
	public void writeDataGathering(SourceGenerator sg, String objectprefix) throws IOException {
		String linkobjectattribute = StringFormatter.formatForAttribute(linkobject.getParent().getName());
		String linkobjectclass = StringFormatter.formatForJavaClass(linkobject.getParent().getName());
		String leftobjectattribute = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		String leftobjectclass = StringFormatter.formatForJavaClass(linkobject.getLeftobjectforlink().getName());
		String rightobjectclass = StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName());
		sg.wl("		TwoDataObjects<" + linkobjectclass + "," + rightobjectclass + ">[] " + leftobjectattribute + "_step"
				+ objectprefix + "_linegrouping_" + linkobjectattribute + " = ");
		sg.wl("				" + linkobjectclass + ".getlinksandrightobject(" + leftobjectattribute + "_step"
				+ objectprefix + "_id, null);");
		sg.wl("		Function<DataObjectId<" + leftobjectclass + ">, String> " + leftobjectattribute + "_step"
				+ objectprefix + "_linegrouping_" + linkobjectattribute
				+ "_mapping = SmartReportUtility.getMultipleLinkNr(" + leftobjectattribute + "_step" + objectprefix
				+ "_linegrouping_" + linkobjectattribute + ");");

	}

	@Override
	protected String getExtractorFromobject(String objectprefix) {
		String linkobjectattribute = StringFormatter.formatForAttribute(linkobject.getParent().getName());
		String leftobjectattribute = StringFormatter.formatForAttribute(linkobject.getLeftobjectforlink().getName());
		return "(a)->(" + leftobjectattribute + "_step" + objectprefix + "_linegrouping_" + linkobjectattribute
				+ "_mapping.apply(a.getId()))";
	}

	@Override
	public String[] getImportStatements() {
		ArrayList<String> importstatements = new ArrayList<String>();
		importstatements.add("import " + linkobject.getParent().getOwnermodule().getPath() + ".data."
				+ StringFormatter.formatForJavaClass(linkobject.getParent().getName()) + ";");
		importstatements.add("import " + linkobject.getRightobjectforlink().getOwnermodule().getPath() + ".data."
				+ StringFormatter.formatForJavaClass(linkobject.getRightobjectforlink().getName()) + ";");
		importstatements.add("import org.openlowcode.server.data.TwoDataObjects;");
		return importstatements.toArray(new String[0]);
	}

	@Override
	protected void writeClassification(
			SourceGenerator sg,
			ObjectReportNode objectReportNode,
			String prefix,
			String extraindent) throws IOException {
		String linkname = StringFormatter.formatForAttribute(linkobject.getParent().getName());
		sg.wl(extraindent + "				String " + linkname + " = "
				+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "_step" + prefix
				+ "_linegrouping_" + linkname + "_mapping.apply(this"
				+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step" + prefix
				+ ".getId());");
		sg.wl(extraindent + "				if (" + linkname + "!=null) step" + prefix + "classification.add("
				+ linkname + ");");

	}

	@Override
	public boolean isbacktobject() {
		return false;
	}

	@Override
	protected void feedfields(ArrayList<Field> fieldlist, boolean before) {
		// donothin

	}

	@Override
	protected void writeFields(SourceGenerator sg, String prefix) throws IOException {
		// donothing

	}

	@Override
	public boolean needArrayOfObjectId() {
		return true;
	}

}
