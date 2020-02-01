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
import org.openlowcode.design.data.Field;
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.argument.IntegerArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.TimestampArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * The schedule property will treat the current object as a schedule item. It
 * will use another object to manage dependencies between schedule items
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Schedule
		extends
		Property<Schedule> {
	private DataObjectDefinition dependencyobject;
	private TimeSlot timeslot;
	private ScheduleDependency scheduledependencyproperty;
	private Field fieldforcolor;
	private Field fieldfordot;
	private int minwidthfordisplay = 0;

	/**
	 * allows to specify a field in the object that will be displayed on GANNT chart
	 * as a color dot
	 * 
	 * @param fieldfordot field to be used for color dot
	 */
	public void setFieldForDot(Field fieldfordot) {
		this.fieldfordot = fieldfordot;
	}

	/**
	 * @return the field used for color dot (or null if no dot on the GANNT chart)
	 */
	public Field getFieldForDot() {
		return this.fieldfordot;
	}

	/**
	 * @return the field used for the background color of tasks in the GANNT chart
	 */
	public Field getFieldForColor() {
		return this.fieldforcolor;
	}

	/**
	 * @return the data object used for dependencies between schedule items
	 *         (timeslots)
	 */
	public DataObjectDefinition getDependencyObject() {
		return this.dependencyobject;
	}

	/**
	 * create a schedule for current object as schedule item, and the object given
	 * as parameter used as dependency
	 * 
	 * @param dependencyobject object used as dependency
	 */
	public Schedule(DataObjectDefinition dependencyobject) {
		super("SCHEDULE");
		this.dependencyobject = dependencyobject;

	}

	/**
	 * create a schedule for current object as schedule item, and the object given
	 * as parameter used as dependency, with a field for background color
	 * 
	 * @param dependencyobject object used as dependency
	 * @param fieldforcolor    field used for background color of the task
	 */
	public Schedule(DataObjectDefinition dependencyobject, Field fieldforcolor) {
		super("SCHEDULE");
		this.dependencyobject = dependencyobject;
		this.fieldforcolor = fieldforcolor;
	}

	/**
	 * create a schedule for current object as schedule item, and the object given
	 * as parameter used as dependency, with a field for background color
	 * 
	 * @param dependencyobject   object used as dependency
	 * @param fieldforcolor      field used for background color of the task
	 * @param fieldfordot        field used to display a color dot
	 * @param minwidthfordisplay minimum width of the GANNT chart in pixel
	 */
	public Schedule(
			DataObjectDefinition dependencyobject,
			Field fieldforcolor,
			Field fieldfordot,
			int minwidthfordisplay) {
		super("SCHEDULE");
		this.dependencyobject = dependencyobject;
		this.fieldforcolor = fieldforcolor;
		this.fieldfordot = fieldfordot;
		this.minwidthfordisplay = minwidthfordisplay;

	}

	/**
	 * create a schedule for current object as schedule item, and the object given
	 * as parameter used as dependency
	 * 
	 * @param dependencyobject   object used as dependency
	 * @param minwidthfordisplay minimum width of the GANNT chart in pixel
	 */
	public Schedule(DataObjectDefinition dependencyobject, int minwidthfordisplay) {
		super("SCHEDULE");
		this.dependencyobject = dependencyobject;
		this.minwidthfordisplay = minwidthfordisplay;

	}

	/**
	 * @return the min width for the display of the GANNT chart in pixel
	 */
	public int getMinWidthForDisplay() {
		return this.minwidthfordisplay;
	}

	@Override
	public void controlAfterParentDefinition() {

		this.timeslot = (TimeSlot) parent.getPropertyByName("TIMESLOT");
		this.addDependentProperty(timeslot);
		if (this.fieldforcolor != null)
			if (parent.lookupFieldByName(fieldforcolor.getName()) == null)
				throw new RuntimeException("Field for color " + fieldforcolor.getName() + " does not exist in object");
		this.scheduledependencyproperty = new ScheduleDependency(parent, this);
		this.addExternalObjectProperty(dependencyobject, scheduledependencyproperty);

		this.addPropertyGenerics(new PropertyGenerics("SCHEDULE", dependencyobject, scheduledependencyproperty));

		DataAccessMethod rescheduleafter = new DataAccessMethod("RESCHEDULEAFTER", null, false);
		rescheduleafter.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(rescheduleafter);

		DataAccessMethod getcalendarstarthour = new DataAccessMethod("GETCALENDARSTARTHOUR",
				new IntegerArgument("CALENDARSTARTHOUR"), false);
		getcalendarstarthour.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getcalendarstarthour);

		DataAccessMethod getcalendarendhour = new DataAccessMethod("GETCALENDARENDHOUR",
				new IntegerArgument("CALENDARENDHOUR"), false);
		getcalendarendhour.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getcalendarendhour);

		DataAccessMethod getnextstarthour = new DataAccessMethod("GETNEXTSTARTHOUR",
				new TimestampArgument("NEXTSTARTHOUR"), false);
		getnextstarthour.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(getnextstarthour);

		DataAccessMethod insertafter = new DataAccessMethod("INSERTAFTER", null, false);
		insertafter.addInputArgument(new MethodArgument("OBJECTBEFORE", new ObjectArgument("OBJECTBEFORE", parent)));
		insertafter
				.addInputArgument(new MethodArgument("OBJECTTOINSERT", new ObjectArgument("OBJECTTOINSERT", parent)));

		this.addDataAccessMethod(insertafter);

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
		MethodAdditionalProcessing reschedulenext = new MethodAdditionalProcessing(false,
				timeslot.getDataAccessMethod("RESCHEDULE"));
		this.addMethodAdditionalProcessing(reschedulenext);
		MethodAdditionalProcessing repairscheddule = new MethodAdditionalProcessing(false,
				timeslot.getDataAccessMethod("REPAIRSCHEDULE"));
		this.addMethodAdditionalProcessing(repairscheddule);
	}

	@Override
	public String getJavaType() {
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
	}

	@Override
	public String getPropertyHelperName() {
		return "Abs" + StringFormatter.formatForJavaClass(this.getParent().getName()) + "ScheduleHelper";
	}

	@Override
	public void generatePropertyHelperToFile(SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(this.getParent().getName());

		sg.wl("package " + this.getParent().getOwnermodule().getPath() + ".data;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.data.properties.schedule.ScheduleHelper;");
		sg.wl("import " + this.getParent().getOwnermodule().getPath() + ".utility." + objectclass + "ScheduleHelper;");
		sg.wl("");
		sg.wl("public abstract class Abs" + objectclass + "ScheduleHelper extends ScheduleHelper<" + objectclass
				+ "> {");
		sg.wl("	public static Abs" + objectclass + "ScheduleHelper get() {");
		sg.wl("		return new " + objectclass + "ScheduleHelper();");
		sg.wl("	}");
		sg.wl("}");

		sg.close();
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}
}
