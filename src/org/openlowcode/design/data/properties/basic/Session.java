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
import org.openlowcode.design.data.IntegerStoredElement;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.StringStoredElement;
import org.openlowcode.design.data.TimestampStoredElement;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.IntegerArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.TimestampArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * A session is as specific period of time that is part of a timeslot. As an
 * example, a 1-week English training will be from 1st to 5th of february, with
 * 2 sessions a day from 9am to 12am and from 1pm to 5pm. This is useful for
 * very precise scheduling for industrial planning or invitation sending
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class Session
		extends
		Property<Session> {
	private String[] returnvalues;
	private UniqueIdentified uniqueidentified;
	private DataObjectDefinition parenttimeslot;
	private LinkedToParent<?> linktotimeslot;

	/**
	 * A session is a child of the parent timeslot object indicated. The parent
	 * timeslot object should be an object with the timeslot property.
	 * 
	 * @param linktotimeslot the linked to parent property between the session and
	 *                       the timeslot
	 */
	public Session(LinkedToParent<?> linktotimeslot) {
		super("SESSION");

		this.linktotimeslot = linktotimeslot;

	}

	@Override
	public void controlAfterParentDefinition() {
		this.parenttimeslot = linktotimeslot.getParentObjectForLink();
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		this.addDependentProperty(linktotimeslot);
		StoredElement starttime = new TimestampStoredElement("STARTTIME");
		this.addElement(starttime);
		StoredElement endtime = new TimestampStoredElement("ENDTIME");
		this.addElement(endtime);
		StringStoredElement valid = new StringStoredElement("VALID", 16);
		this.addElement(valid);
		StoredElement sequence = new IntegerStoredElement("SEQUENCE");
		this.addElement(sequence);
		DataAccessMethod reschedule = new DataAccessMethod("SETSESSIONTIME", null, false);
		reschedule.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		reschedule.addInputArgument(new MethodArgument("STARTTIME", new TimestampArgument("STARTTIME")));
		reschedule.addInputArgument(new MethodArgument("ENDTIME", new TimestampArgument("ENDTIME")));
		reschedule.addInputArgument(new MethodArgument("SEQUENCE", new IntegerArgument("SEQUENCE")));
		reschedule.addInputArgument(new MethodArgument("VALID",
				new ChoiceArgument("VALID", SystemModule.getSystemModule().getBooleanChoice())));

		this.addDataAccessMethod(reschedule);

		DataAccessMethod setparenttimeslot = new DataAccessMethod("SETPARENTTIMESLOT", null, false);
		setparenttimeslot.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		setparenttimeslot.addInputArgument(
				new MethodArgument("PARENTTIMESLOTID", new ObjectIdArgument("PARENTTIMESLOTID", parenttimeslot)));

		this.addDataAccessMethod(setparenttimeslot);

		if (parenttimeslot == null)
			throw new RuntimeException("parenttimeslot is null for property with name = " + this.getName());
		TimeslotWithSessions timeslotwithsessions = new TimeslotWithSessions(this.parent);
		this.addExternalObjectProperty(parenttimeslot, timeslotwithsessions);
		this.addPropertyGenerics(new PropertyGenerics("PARENTTIMESLOT", parenttimeslot, new TimeSlot()));

	}

	@Override
	public String[] getPropertyInitMethod() {
		return returnvalues;
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
		returnvalues = new String[1];
		returnvalues[0] = ".setsessiontime(starttime,endtime,sequence,valid);";
		this.addDataInput(new TimestampArgument("STARTTIME", "Start Time", true));
		this.addDataInput(new TimestampArgument("ENDTIME", "End Time", true));
		this.addDataInput(new IntegerArgument("SEQUENCE", "Sequence"));
		this.addDataInput(new ChoiceArgument("VALID", SystemModule.getSystemModule().getBooleanChoice()));
	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.module.system.data.choice.BooleanChoiceDefinition;");

	}

}
