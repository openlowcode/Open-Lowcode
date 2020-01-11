/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.graphic.widget;

import java.io.IOException;
import java.util.function.Function;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectFieldMarker;
import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.data.message.TObjectIdDataEltType;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.DateDataEltType;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.TextDataElt;

/**
 * a GANNT chart displaying tasks and dependencies
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object storing tasks
 * @param <F> data object storing dependencies if they are found
 */
public class SGanntChart<E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & UniqueidentifiedInterface<F>>
		extends SPageNode implements SDefaultPath {
	private String name;
	private boolean hoursasdata;
	private int hourstart;
	private int hourend;
	private int showmode;
	private int minwidth = 0;
	private int morefieldpriority = 0;
	private ArrayDataElt<TObjectDataElt<E>> tasklist;
	private ArrayDataElt<TObjectDataElt<F>> linkarray;
	private DataObjectFieldMarker<F> linkobjectid1;
	private DataObjectFieldMarker<F> linkobjectid2;
	private DataObjectFieldMarker<E> taskstarttimefield;
	private DataObjectFieldMarker<E> taskendtimefield;
	private DataObjectFieldMarker<E> label1;
	private DataObjectFieldMarker<E> label2;
	private DataObjectFieldMarker<E> colorfield;
	private SActionRef defaultaction;
	private SActionRef rescheduleaction;
	private IntegerDataElt hourstartdata;
	private IntegerDataElt hourenddata;
	private TextDataElt calendarstring;
	private DataObjectDefinition<E> taskdefinition;
	private DataObjectFieldMarker<E> dotmarkfield;

	/**
	 * used for calendar with
	 * <ul>
	 * <li>bands for days</li>
	 * <li>show all days</li>
	 * </ul>
	 */
	public static final int SHOWDAYS = 0;
	/**
	 * used for calendar with
	 * <ul>
	 * <li>bands for days and sub-band for hours</li>
	 * <li>constant hour shown for everyday</li>
	 * </ul>
	 */
	public static final int SHOWSIMPLEHOURS = 1;
	/**
	 * used for calendar with
	 * <ul>
	 * <li>only days in calendar shown</li>
	 * <li>only working slots in calendar shown</li>
	 * </ul>
	 */
	public static final int SHOWHOURSINCALENDAR = 2;

	/**
	 * sets the default action when clicking on the task in the GANNT chart
	 * 
	 * @param defaultaction default action
	 */
	public void addDefaultAction(SActionRef defaultaction) {
		this.defaultaction = defaultaction;
	}

	/**
	 * adds a reschedule action that is triggered when dragging a task on the GANNT
	 * chart
	 * 
	 * @param rescheduleaction action to launch for reschedule
	 */
	public void addRescheduleAction(SActionRef rescheduleaction) {
		this.rescheduleaction = rescheduleaction;
	}

	/**
	 * gets the id of the selected timeslot as attribute for an action
	 * 
	 * @return the reference to put in the action
	 */
	public Function<SActionInputDataRef<TObjectIdDataEltType<E>>, SActionDataLoc<TObjectIdDataEltType<E>>> getSelectedTimeslotIdInput() {
		return (a) -> (new SActionDataLoc<TObjectIdDataEltType<E>>(this, a, "SELECTEDTIMESLOT"));
	}

	/**
	 * get the id of the root timeslot element (start of the GANNT chart)
	 * 
	 * @return the reference to put in the action
	 */
	public Function<SActionInputDataRef<TObjectIdDataEltType<E>>, SActionDataLoc<TObjectIdDataEltType<E>>> getRootTimeslotIdInput() {
		return (a) -> (new SActionDataLoc<TObjectIdDataEltType<E>>(this, a, "ROOTTIMESLOT"));
	}

	/**
	 * gets the new start date inside the reschedule popup to use it as attribute of
	 * an action
	 * 
	 * @obsolete
	 * @param inputdataref action input data ref
	 * @return the data location
	 */
	public SActionDataLoc<DateDataEltType> getReschedulePopupStartDate(
			SActionInputDataRef<DateDataEltType> inputdataref) {
		return new SActionDataLoc<DateDataEltType>(this, inputdataref, "RESCHEDULESTARTDATE");
	}

	/**
	 * gets the new start date in the reschedule popup
	 * 
	 * @return the attribute to use as input of an action
	 */
	public Function<SActionInputDataRef<DateDataEltType>, SActionDataLoc<DateDataEltType>> getReschedulePopupStartDateInput() {
		return (a) -> (new SActionDataLoc<DateDataEltType>(this, a, "RESCHEDULESTARTDATE"));
	}

	/**
	 * gets the new end date inside the reschedule popup to use it as attribute of
	 * an action
	 * 
	 * @obsolete
	 * @param inputdataref action input data ref
	 * @return the data location
	 */
	public SActionDataLoc<DateDataEltType> getReschedulePopupEndDate(
			SActionInputDataRef<DateDataEltType> inputdataref) {
		return new SActionDataLoc<DateDataEltType>(this, inputdataref, "RESCHEDULEENDDATE");
	}

	/**
	 * gets the new end date inside the reschedule popup to use it as attribute of
	 * an action
	 * 
	 * @return the attribute to use as input of an action
	 */
	public Function<SActionInputDataRef<DateDataEltType>, SActionDataLoc<DateDataEltType>> getReschedulePopupEndDateInput() {
		return (a) -> (new SActionDataLoc<DateDataEltType>(this, a, "RESCHEDULEENDDATE"));
	}

	/**
	 * sets the minimum width of the GANNT chart
	 * 
	 * @param minwidth min width of the GANNT chart
	 */
	public void setMinWidth(int minwidth) {
		this.minwidth = minwidth;
	}

	/**
	 * Creates a GANNT chart with variable start and end hour for displaying tasks
	 * per day
	 * 
	 * @param name               name of the widget
	 * @param hourstartdata      variable start hour
	 * @param hourenddata        variable end hour
	 * @param tasklist           list of tasks
	 * @param taskstarttimefield field to use as task start time
	 * @param taskendtimefield   field to use as task end time
	 * @param label1             first label on the task
	 * @param label2             second label on the task
	 * @param taskdefinition     definition of the task
	 * @param parent             page the widget is displayed on
	 */
	public SGanntChart(String name, IntegerDataElt hourstartdata, IntegerDataElt hourenddata,
			ArrayDataElt<TObjectDataElt<E>> tasklist, DataObjectFieldMarker<E> taskstarttimefield,
			DataObjectFieldMarker<E> taskendtimefield, DataObjectFieldMarker<E> label1, DataObjectFieldMarker<E> label2,
			DataObjectDefinition<E> taskdefinition, SPage parent) {
		super(parent);
		this.showmode = SHOWSIMPLEHOURS;
		this.name = name;
		this.hoursasdata = true;
		this.hourstartdata = hourstartdata;
		this.hourenddata = hourenddata;
		this.tasklist = tasklist;
		this.taskstarttimefield = taskstarttimefield;
		this.taskendtimefield = taskendtimefield;
		this.label1 = label1;
		this.label2 = label2;
		this.taskdefinition = taskdefinition;
	}

	/**
	 * * Creates a GANNT chart with default start hour end end hour for display
	 * 
	 * @param name               name of the widget
	 * @param tasklist           list of tasks
	 * @param taskstarttimefield field to use as task start time
	 * @param taskendtimefield   field to use as task end time
	 * @param label1             first label on the task
	 * @param label2             second label on the task
	 * @param taskdefinition     definition of the task
	 * @param parent             page the widget is displayed on
	 */
	public SGanntChart(String name, TextDataElt calendarcode, ArrayDataElt<TObjectDataElt<E>> tasklist,
			DataObjectFieldMarker<E> taskstarttimefield, DataObjectFieldMarker<E> taskendtimefield,
			DataObjectFieldMarker<E> label1, DataObjectFieldMarker<E> label2, DataObjectDefinition<E> taskdefinition,
			SPage parent) {
		super(parent);
		this.name = name;
		this.showmode = SHOWHOURSINCALENDAR;
		this.tasklist = tasklist;
		this.taskstarttimefield = taskstarttimefield;
		this.taskendtimefield = taskendtimefield;
		this.label1 = label1;
		this.label2 = label2;
		this.taskdefinition = taskdefinition;
	}

	/**
	 * Creates a GANNT chart with fixed start hour and end hour for displaying of
	 * day
	 * 
	 * @param name               name of the widget
	 * @param hourstart          set start hour
	 * @param hourend            set end hour
	 * @param tasklist           list of tasks
	 * @param taskstarttimefield field to use as task start time
	 * @param taskendtimefield   field to use as task end time
	 * @param label1             first label on the task
	 * @param label2             second label on the task
	 * @param taskdefinition     definition of the task
	 * @param parent             page the widget is displayed on
	 */
	public SGanntChart(String name, int hourstart, int hourend, ArrayDataElt<TObjectDataElt<E>> tasklist,
			DataObjectFieldMarker<E> taskstarttimefield, DataObjectFieldMarker<E> taskendtimefield,
			DataObjectFieldMarker<E> label1, DataObjectFieldMarker<E> label2, DataObjectDefinition<E> taskdefinition,
			SPage parent) {
		super(parent);
		this.name = name;
		this.showmode = SHOWSIMPLEHOURS;
		this.hoursasdata = false;
		this.hourstart = hourstart;
		this.hourend = hourend;
		if (this.hourstart <= 0)
			throw new RuntimeException("hour start should be 0 or more");
		if (this.hourend > 24)
			throw new RuntimeException("hour end should not be more than 24");
		if (this.hourstart >= hourend)
			throw new RuntimeException("hour end should be strictly greater than hour start");
		this.tasklist = tasklist;
		this.taskstarttimefield = taskstarttimefield;
		this.taskendtimefield = taskendtimefield;
		this.label1 = label1;
		this.label2 = label2;
		this.taskdefinition = taskdefinition;
	}

	/**
	 * adds a dependency array on the GANNT chart
	 * 
	 * @param linkarray     the list of link objects
	 * @param linkobjectid1 first id of object (of type E) of the dependency
	 * @param linkobjectid2 second id of object (of type E) of the dependency
	 */
	public void addDependencies(ArrayDataElt<TObjectDataElt<F>> linkarray, DataObjectFieldMarker<F> linkobjectid1,
			DataObjectFieldMarker<F> linkobjectid2) {
		this.linkarray = linkarray;
		this.linkobjectid1 = linkobjectid1;
		this.linkobjectid2 = linkobjectid2;
	}

	/**
	 * sets the attribute to be used
	 * 
	 * @param colorfield the attribute of the object to be used as background for
	 *                   the task (tasks can be displayed with a color for the task
	 *                   and also a dot)
	 */
	public void setColorField(DataObjectFieldMarker<E> colorfield) {
		this.colorfield = colorfield;
	}

	/**
	 * sets the attribute to be used for dot mark field (tasks can be displayed with
	 * a color for the task and also a dot)
	 * 
	 * @param dotmarkfield the attribute of the object to be used as color for dots
	 *                     on the task
	 */
	public void setDotMarkField(DataObjectFieldMarker<E> dotmarkfield) {
		this.dotmarkfield = dotmarkfield;
	}

	@Override
	public String getPathName() {

		return this.name;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);
		this.setSignifPath(new SPageSignifPath(this.getPathName(), this.getPage(), parentpath, newwidgetpathtoroot));

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.addStringField("NAME", this.name);
		if (this.defaultaction != null) {
			writer.addBooleanField("DFA", true);
			defaultaction.writeToCML(writer);
		} else {
			writer.addBooleanField("DFA", false);
		}
		if (this.rescheduleaction != null) {
			writer.addBooleanField("RSA", true);
			rescheduleaction.writeToCML(writer);
		} else {
			writer.addBooleanField("RSA", false);
		}
		writer.addIntegerField("SHM", this.showmode);
		if (this.showmode == SHOWSIMPLEHOURS) {
			if (this.hoursasdata) {
				writer.addBooleanField("HAD", true);
				this.hourstartdata.writeReferenceToCML(writer);
				this.hourenddata.writeReferenceToCML(writer);

			} else {
				writer.addBooleanField("HAD", false);
				writer.addIntegerField("HRS", this.hourstart);
				writer.addIntegerField("HRE", this.hourend);
			}
		}
		if (this.showmode == SHOWHOURSINCALENDAR) {
			this.calendarstring.writeReferenceToCML(writer);
		}
		tasklist.writeReferenceToCML(writer);
		// calendarstring.writeReferenceToCML(writer);
		writer.addStringField("STM", taskstarttimefield.toString());
		writer.addStringField("ETM", taskendtimefield.toString());
		writer.addStringField("LB1", label1.toString());
		writer.addBooleanField("IL2", (label2 != null));
		if (label2 != null)
			writer.addStringField("LB2", label2.toString());
		if (this.linkarray != null) {
			writer.addBooleanField("HLK", true);
			this.linkarray.writeReferenceToCML(writer);
			writer.addStringField("LI1", this.linkobjectid1.toString());
			writer.addStringField("LI2", this.linkobjectid2.toString());

		} else {
			writer.addBooleanField("HLK", false);
		}
		if (this.colorfield != null) {
			writer.addBooleanField("HCF", true);
			writer.addStringField("CLF", this.colorfield.toString());

		} else {
			writer.addBooleanField("HCF", false);
		}

		if (this.dotmarkfield != null) {
			writer.addBooleanField("HDM", true);
			writer.addStringField("DMF", this.dotmarkfield.toString());

		} else {
			writer.addBooleanField("HDM", false);
		}

		writer.addIntegerField("MCW", this.minwidth);

		this.taskdefinition.writeFieldDefinition(writer, null, null, false, this.morefieldpriority, input, buffer);

	}

	@Override
	public String getWidgetCode() {

		return "GANNTC";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}
}
