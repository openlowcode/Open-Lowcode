/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.advanced;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.Field;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * the line grouping criteria on object will create grouping nodes for objects
 * for the current object in the object treee
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class LineGroupingCriteriaObject
		extends
		LineGroupingCriteria {
	private DataObjectDefinition object;
	private boolean shownumber;
	private boolean showname;
	private boolean backtoobject;
	private ArrayList<Field> fieldstoshowbeforemainvalue;
	private ArrayList<Field> fieldstoshowaftermainvalue;

	/**
	 * creates a line grouping criteria for the given object, with no back to object
	 * 
	 * @param object     data object
	 * @param shownumber true if the regrouping node shows the number
	 * @param showname   true if the regrouping node shows the name
	 */
	public LineGroupingCriteriaObject(DataObjectDefinition object, boolean shownumber, boolean showname) {
		this(object, shownumber, showname, false);
	}

	/**
	 * creates a line grouping criteria for the given object, with no back to object
	 * 
	 * @param object       data object
	 * @param shownumber   true if the regrouping node shows the number
	 * @param showname     true if the regrouping node shows the name
	 * @param backtoobject true if a back to object is put on the regrouping node
	 * @param fieldsbefore show fields before on the regrouping object
	 * @param fieldsafter  show fields after on the regrouping object
	 */
	public LineGroupingCriteriaObject(
			DataObjectDefinition object,
			boolean shownumber,
			boolean showname,
			boolean backtoobject,
			Field[] fieldsbefore,
			Field[] fieldsafter) {
		this(object, shownumber, showname, backtoobject);
		if (fieldsbefore != null)
			for (int i = 0; i < fieldsbefore.length; i++)
				this.addFieldToShowBeforeMainValue(fieldsbefore[i]);
		if (fieldsafter != null)
			for (int i = 0; i < fieldsafter.length; i++)
				this.addFieldToShowAfterMainValue(fieldsafter[i]);

	}

	/**
	 * creates a line grouping criteria for the given object
	 * 
	 * @param object       data object
	 * @param shownumber   true if the regrouping node shows the number
	 * @param showname     true if the regrouping node shows the name
	 * @param backtoobject true if a back to object is put on the regrouping node
	 */
	public LineGroupingCriteriaObject(
			DataObjectDefinition object,
			boolean shownumber,
			boolean showname,
			boolean backtoobject) {
		if (object == null)
			throw new RuntimeException("Object cannot be null");
		this.object = object;
		if (shownumber)
			if (!object.hasNumbered())
				throw new RuntimeException("Show number is not valid on " + object.getName()
						+ " as it does not have the Numbered property");
		if (showname)
			if (!object.hasNamed())
				throw new RuntimeException(
						"Show name is not valid on " + object.getName() + " as it does not have the Named property");

		this.shownumber = shownumber;
		this.showname = showname;
		this.backtoobject = backtoobject;
		this.fieldstoshowbeforemainvalue = new ArrayList<Field>();
		this.fieldstoshowaftermainvalue = new ArrayList<Field>();

	}

	/**
	 * adds a new field to show before the main value
	 * 
	 * @param field field to show before the main value on the object being used for
	 *              regrouping
	 */
	public void addFieldToShowBeforeMainValue(Field field) {
		if (field == null)
			throw new RuntimeException("Field cannot be null");
		if (field.getParentObject() == null)
			throw new RuntimeException(
					"Choice Field " + field.getName() + " does not have a parent DataObjectDefinition");
		if (field.getParentObject() != object)
			throw new RuntimeException("Adding inconsistent Field " + field.getName() + " to object " + object.getName()
					+ "Although field belongs to " + field.getParentObject().getName());
		this.fieldstoshowbeforemainvalue.add(field);
	}

	/**
	 * adds a new field to show after the main value
	 * 
	 * @param field field to show after the main value on the object being used for
	 *              regrouping
	 */
	public void addFieldToShowAfterMainValue(Field field) {
		if (field == null)
			throw new RuntimeException("Field cannot be null");
		if (field.getParentObject() == null)
			throw new RuntimeException(
					"Choice Field " + field.getName() + " does not have a parent DataObjectDefinition");
		if (field.getParentObject() != object)
			throw new RuntimeException("Adding inconsistent Field " + field.getName() + " to object " + object.getName()
					+ "Although field belongs to " + field.getParentObject().getName());
		this.fieldstoshowaftermainvalue.add(field);
	}

	@Override
	public DataObjectDefinition getObject() {
		return object;
	}

	@Override
	public boolean hasDataGathering() {
		return false;
	}

	@Override
	public void writeDataGathering(SourceGenerator sg, String objectprefix) {
		throw new RuntimeException("Not implemented");
	}

	@Override
	protected String getExtractorFromobject(String objectprefix) {
		return null;
	}

	@Override
	public String[] getImportStatements() {

		return null;
	}

	@Override
	protected void writeClassification(
			SourceGenerator sg,
			ObjectReportNode objectReportNode,
			String prefix,
			String extraindent) throws IOException {
		if (shownumber)
			if (showname)
				sg.wl(extraindent + "				step" + prefix + "classification.add(this"
						+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step"
						+ prefix + ".getNr()+\" - \"+this"
						+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step"
						+ prefix + ".getObjectname());");
		if (shownumber)
			if (!showname)
				sg.wl(extraindent + "				step" + prefix + "classification.add(this"
						+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step"
						+ prefix + ".getNr());");
		if (!shownumber)
			if (showname)
				sg.wl(extraindent + "				step" + prefix + "classification.add(this"
						+ StringFormatter.formatForAttribute(objectReportNode.getRelevantObject().getName()) + "step"
						+ prefix + ".getObjectname());");

	}

	@Override
	public boolean isbacktobject() {
		return this.backtoobject;
	}

	@Override
	protected void feedfields(ArrayList<Field> fieldlist, boolean before) {
		if (before) {
			for (int i = 0; i < this.fieldstoshowbeforemainvalue.size(); i++) {
				Field thisfield = this.fieldstoshowbeforemainvalue.get(i);
				Field copyfield = thisfield.copy();
				copyfield.setDisplayPriority(500 - i);
				fieldlist.add(copyfield);
			}
		} else {
			for (int i = 0; i < this.fieldstoshowaftermainvalue.size(); i++) {
				Field thisfield = this.fieldstoshowaftermainvalue.get(i);
				Field copyfield = thisfield.copy();
				copyfield.setDisplayPriority(50 - i);
				fieldlist.add(copyfield);
			}
		}

	}

	@Override
	protected void writeFields(SourceGenerator sg, String prefix) throws IOException {
		for (int i = 0; i < this.fieldstoshowbeforemainvalue.size(); i++) {
			Field thisfield = this.fieldstoshowbeforemainvalue.get(i);
			String fieldname = StringFormatter.formatForJavaClass(thisfield.getName());
			sg.wl("			newreportitem" + prefix + ".set" + fieldname + "(this"
					+ StringFormatter.formatForAttribute(object.getName()) + "step" + prefix + ".get" + fieldname
					+ "());");

		}
		for (int i = 0; i < this.fieldstoshowaftermainvalue.size(); i++) {
			Field thisfield = this.fieldstoshowaftermainvalue.get(i);
			String fieldname = StringFormatter.formatForJavaClass(thisfield.getName());
			sg.wl("			newreportitem" + prefix + ".set" + fieldname + "(this"
					+ StringFormatter.formatForAttribute(object.getName()) + "step" + prefix + ".get" + fieldname
					+ "());");
		}

	}

	@Override
	public boolean needArrayOfObjectId() {
		return false;
	}
}
