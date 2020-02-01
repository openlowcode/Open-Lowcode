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
import org.openlowcode.design.data.MethodAdditionalProcessing;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * A timeslot with session is a property added to the timeslot object to
 * indicate the timeslots are subdivided in children sessions. <br>
 * As an example, a 1-week English training timeslot will be from 1st to 5th of
 * february, with 2 sessions a day from 9am to 12am and from 1pm to 5pm. This is
 * useful for very precise scheduling for industrial planning or invitation
 * sending
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class TimeslotWithSessions
		extends
		Property<TimeslotWithSessions> {

	private DataObjectDefinition sessionsubobject;
	private Schedule schedule;
	private UniqueIdentified uniqueidentified;

	/**
	 * This constructor is only supposed to be called by the timeslot constructor.
	 * 
	 * @param sessionsubobject the subobject storing session
	 */
	protected TimeslotWithSessions(DataObjectDefinition sessionsubobject) {
		super("TIMESLOTWITHSESSIONS");
		this.sessionsubobject = sessionsubobject;
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
		Property<?> timeslot = parent.getPropertyByName("TIMESLOT");
		MethodAdditionalProcessing reworksessionaftereschedule = new MethodAdditionalProcessing(false,
				timeslot.getDataAccessMethod("RESCHEDULE"));
		this.addMethodAdditionalProcessing(reworksessionaftereschedule);
		// Note: nothing will happen when this method is called during object creation
		MethodAdditionalProcessing reworksessionafteforcereschedule = new MethodAdditionalProcessing(false,
				timeslot.getDataAccessMethod("FORCERESCHEDULE"));
		this.addMethodAdditionalProcessing(reworksessionafteforcereschedule);
		MethodAdditionalProcessing reworksessionafterrepairschedule = new MethodAdditionalProcessing(false,
				timeslot.getDataAccessMethod("REPAIRSCHEDULE"));
		this.addMethodAdditionalProcessing(reworksessionafterrepairschedule);
		Property<?> storedobject = parent.getPropertyByName("STOREDOBJECT");
		MethodAdditionalProcessing reworksessionaftercreation = new MethodAdditionalProcessing(false,
				storedobject.getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(reworksessionaftercreation);

	}

	@Override
	public String getJavaType() {
		return null;
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public void controlAfterParentDefinition() {
		this.schedule = (Schedule) parent.getPropertyByName("SCHEDULE");
		this.addDependentProperty(schedule);
		this.addPropertyGenerics(new PropertyGenerics("CHILDRENSESSIONS", sessionsubobject,
				sessionsubobject.getPropertyByName("SESSION")));
		DataAccessMethod getallsessions = new DataAccessMethod("GETALLSESSIONS",
				new ArrayArgument(new ObjectArgument("sessionsintimeslot", this.sessionsubobject)), true);
		getallsessions.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		this.addDataAccessMethod(getallsessions);
		uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
	}

}
