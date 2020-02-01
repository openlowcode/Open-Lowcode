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
import org.openlowcode.design.data.Index;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.TimestampStoredElement;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.TimestampArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A timeslot is a property giving the object a start time and an end-time to
 * represent a portion of time
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimeSlot
		extends
		Property<TimeSlot> {
	private UniqueIdentified uniqueidentified;
	private String[] returnvalues;

	/**
	 * creates a timeslot property
	 */
	public TimeSlot() {
		super("TIMESLOT");
	}

	@Override
	public void controlAfterParentDefinition() {

		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		StoredElement starttime = new TimestampStoredElement("STARTTIME");
		this.addElement(starttime);
		this.addIndex(new Index("STARTTIME", starttime, false));
		StoredElement endtime = new TimestampStoredElement("ENDTIME");
		this.addElement(endtime);
		this.addIndex(new Index("ENDTIME", endtime, false));
		DataAccessMethod reschedule = new DataAccessMethod("RESCHEDULE", null, false);
		reschedule.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		reschedule.addInputArgument(new MethodArgument("STARTTIME", new TimestampArgument("STARTTIME")));
		reschedule.addInputArgument(new MethodArgument("ENDTIME", new TimestampArgument("ENDTIME")));
		this.addDataAccessMethod(reschedule);

		DataAccessMethod forcereschedule = new DataAccessMethod("FORCERESCHEDULE", null, false);
		forcereschedule.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		forcereschedule.addInputArgument(new MethodArgument("STARTTIME", new TimestampArgument("STARTTIME")));
		forcereschedule.addInputArgument(new MethodArgument("ENDTIME", new TimestampArgument("ENDTIME")));
		this.addDataAccessMethod(forcereschedule);

		DataAccessMethod repairschedule = new DataAccessMethod("REPAIRSCHEDULE", null, false);
		repairschedule.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(repairschedule);

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
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		return null;
	}

	@Override
	public void setFinalSettings() {
		returnvalues = new String[1];
		returnvalues[0] = ".forcereschedule(starttime,endtime);";
		this.addDataInput(new TimestampArgument("STARTTIME", "Start Time", true));
		this.addDataInput(new TimestampArgument("ENDTIME", "End Time", true));
	}

	@Override
	public String getJavaType() {
		return "#NOT IMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}
}
