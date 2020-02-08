/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import javafx.scene.control.TabPane;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.schedule.BusinessCalendar;
import org.openlowcode.client.graphic.widget.schedule.GanttDisplay;
import org.openlowcode.client.graphic.widget.schedule.GanttPlanning;
import org.openlowcode.client.graphic.widget.schedule.GanttTask;
import org.openlowcode.client.graphic.widget.schedule.GanttTaskMouseEventHandler;
import org.openlowcode.client.graphic.widget.tools.CChoiceFieldValue;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DateDataEltType;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.stage.Window;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.input.MouseButton;

/**
 * A widget showing a GANNT chart
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CGanntChart
		extends
		CPageNode {

	private static final Color[] colorarray = new Color[] { Color.rgb(93, 140, 174), Color.rgb(90, 79, 116),
			Color.rgb(135, 95, 154), Color.rgb(164, 52, 93), Color.rgb(185, 161, 147), Color.rgb(140, 156, 118),
			Color.rgb(185, 87, 84), Color.rgb(230, 131, 100), Color.rgb(243, 193, 58), Color.rgb(141, 178, 85),
			Color.rgb(75, 60, 57), Color.rgb(255, 221, 202) };

	private static final Color[] dotcolorarray = new Color[] { Color.RED, Color.YELLOW, Color.BLUE, Color.GREEN,
			Color.WHITE, Color.BLACK, Color.ORANGE, Color.FUCHSIA };

	private static final Color UNSETCOLOR = Color.rgb(119, 136, 153);

	private static final SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy HH:mm");
	@SuppressWarnings("unused")
	private String name;
	private int hourstart;
	private int hourend;
	private CPageDataRef datareference;
	private CPageDataRef linkdatareference;
	private String starttimefield;
	private String endtimefield;
	private String label1field;
	private boolean islabel2;
	private String label2field;
	@SuppressWarnings("unused")
	private Date mindate;

	/**
	 * As of T-747 / T-748, the data tree is made as a flat array until structures
	 * of tasks are added (this will be a next ticket
	 */
	private ArrayDataElt<ObjectDataElt> data;

	/**
	 * list of links. They will be shown as dependencies. The ids of objects are
	 * specified in the two attributes below.
	 */
	private ArrayDataElt<ObjectDataElt> link;

	/**
	 * the field giving the id of first element in the link object
	 */
	private String linkid1field;

	/**
	 * the field giving the id of second element in the link object
	 */
	private String linkid2field;
	private CPageAction action;
	private CPageAction rescheduleaction;
	private ObjectDataElt selectedelement;
	private CTimeslotField timeslotforreschedule;
	private CPageSignifPath parentpathexposed;
	private boolean hoursasdata;
	private CPageDataRef hourstartdataref;
	private CPageDataRef hourenddataref;
	private int showmode;
	private boolean haslinks;
	private boolean hascolorfield;
	private String colorfield;
	private int minchartwidth;
	private ArrayList<CBusinessField<?>> payloadlist;
	private HashMap<String, CBusinessField<?>> payloadlistbyname;
	private boolean hasdotmarkfield;
	private String dotmarkfield;

	@SuppressWarnings("unused")
	private CPageDataRef calendarstring;
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
	 * creates a GANNT chart from the message rom the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CGanntChart(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.parentpathexposed = parentpath;
		this.name = reader.returnNextStringField("NAME");
		boolean hasaction = reader.returnNextBooleanField("DFA");
		if (hasaction) {
			reader.returnNextStartStructure("ACTION");
			this.action = new CPageAction(reader);
		}
		boolean hasrescheduleaction = reader.returnNextBooleanField("RSA");
		if (hasrescheduleaction) {
			reader.returnNextStartStructure("ACTION");
			this.rescheduleaction = new CPageAction(reader);
		}
		this.showmode = reader.returnNextIntegerField("SHM");
		if (this.showmode == SHOWSIMPLEHOURS) {
			this.hoursasdata = reader.returnNextBooleanField("HAD");
			if (this.hoursasdata) {
				this.hourstartdataref = CPageDataRef.parseCPageDataRef(reader);
				this.hourenddataref = CPageDataRef.parseCPageDataRef(reader);

			} else {
				this.hourstart = reader.returnNextIntegerField("HRS");
				this.hourend = reader.returnNextIntegerField("HRE");
			}
		}
		if (this.showmode == SHOWHOURSINCALENDAR) {
			this.calendarstring = CPageDataRef.parseCPageDataRef(reader);

		}
		this.datareference = CPageDataRef.parseCPageDataRef(reader);
		this.starttimefield = reader.returnNextStringField("STM");
		this.endtimefield = reader.returnNextStringField("ETM");
		this.label1field = reader.returnNextStringField("LB1");
		this.islabel2 = reader.returnNextBooleanField("IL2");
		if (islabel2) {
			this.label2field = reader.returnNextStringField("LB2");
		}
		// ------------------------------- link section
		this.haslinks = reader.returnNextBooleanField("HLK");
		if (this.haslinks) {
			this.linkdatareference = CPageDataRef.parseCPageDataRef(reader);
			this.linkid1field = reader.returnNextStringField("LI1");
			this.linkid2field = reader.returnNextStringField("LI2");
		}
		this.hascolorfield = reader.returnNextBooleanField("HCF");
		if (this.hascolorfield) {
			this.colorfield = reader.returnNextStringField("CLF");
		}
		this.hasdotmarkfield = reader.returnNextBooleanField("HDM");
		if (this.hasdotmarkfield) {
			this.dotmarkfield = reader.returnNextStringField("DMF");
		}
		this.minchartwidth = reader.returnNextIntegerField("MCW");
		payloadlist = new ArrayList<CBusinessField<?>>();
		payloadlistbyname = new HashMap<String, CBusinessField<?>>();
		reader.startStructureArray("ATTR");
		while (reader.structureArrayHasNextElement("ATTR")) {
			@SuppressWarnings("rawtypes")
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);

			payloadlist.add(thisfield);

			payloadlistbyname.put(thisfield.getFieldname(), thisfield);
			reader.returnNextEndStructure("ATTR");
		}
		reader.returnNextEndStructure("GANNTC");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("not yet implemented");
	}

	/**
	 * get the start date of the the given Object data element
	 * 
	 * @param object data object
	 * @return start date
	 */
	public Date getStartDate(ObjectDataElt object) {
		SimpleDataElt startdate = object.lookupEltByName(this.starttimefield);
		if (startdate == null)
			return null;
		if (!(startdate instanceof DateDataElt))
			throw new RuntimeException("field " + this.starttimefield + " should be of class DateDataElt but is "
					+ startdate.getClass().toString());
		DateDataElt startdateparsed = (DateDataElt) startdate;
		return startdateparsed.getPayload();
	}

	/**
	 * get the end date of the given Object data element
	 * 
	 * @param object data object
	 * @return end date
	 */
	public Date getEndDate(ObjectDataElt object) {
		SimpleDataElt enddate = object.lookupEltByName(this.endtimefield);
		if (enddate == null)
			return null;
		if (!(enddate instanceof DateDataElt))
			throw new RuntimeException("field " + this.endtimefield + " should be of class DateDataElt but is "
					+ enddate.getClass().toString());
		DateDataElt enddateparsed = (DateDataElt) enddate;
		return enddateparsed.getPayload();
	}

	/**
	 * get the label to show on the GANNT from the data element
	 * 
	 * @param object data object
	 * @return the label of the data element
	 */
	public String getLabel(ObjectDataElt object) {
		SimpleDataElt field1 = object.lookupEltByName(this.label1field);
		if (!this.islabel2)
			return field1.defaultTextRepresentation();
		SimpleDataElt field2 = object.lookupEltByName(this.label2field);
		if (field2 != null)
			return field1.defaultTextRepresentation() + " " + field2.defaultTextRepresentation();
		return field1.defaultTextRepresentation();

	}

	/**
	 * get a field display value
	 * 
	 * @param fieldname unique java field name
	 * @param object    data object
	 * @return the display value of the field
	 */
	public String getFieldDisplayValue(String fieldname, ObjectDataElt object) {
		SimpleDataElt field = object.lookupEltByName(fieldname);
		CBusinessField<?> typefield = this.payloadlistbyname.get(fieldname);
		if (typefield != null)
			if (typefield instanceof CChoiceField) {
				CChoiceField choicefield = (CChoiceField) typefield;
				CChoiceFieldValue value = choicefield.getChoiceFieldValue(field.defaultTextRepresentation());
				if (value != null)
					return value.getDisplayvalue();

			}
		if (field instanceof DateDataElt) {
			DateDataElt datefield = (DateDataElt) field;
			Date date = datefield.getPayload();
			return sdf.format(date);
		}
		return field.defaultTextRepresentation();
	}

	/**
	 * get the start of day for the given date
	 * 
	 * @param date a given date
	 * @return the start of the day
	 */
	public Calendar getStartOfDay(Date date) {
		Calendar startday = new GregorianCalendar();
		startday.setTime(date);
		startday.set(Calendar.HOUR_OF_DAY, 0); // quick hack to manage Europe summer time
		startday.set(Calendar.MINUTE, 0);
		startday.set(Calendar.SECOND, 0);
		startday.set(Calendar.MILLISECOND, 0);
		return startday;
	}

	/**
	 * get the number of days necessary to cover both given dates
	 * 
	 * @param startdate start date
	 * @param enddate   end date
	 * @return the number of days necessary to include startdate and enddate. E.g.
	 *         from 23-MAY 23:00 to 25-MAY 01:00, number of days is 3. Note: there
	 *         are simple hacks to manage europe summer time that may have side
	 *         effects for activities from midnight to 2am, and it will not work for
	 *         other timezones.
	 */
	public int numberofdays(Date startdate, Date enddate) {
		Calendar startdatecal = getStartOfDay(startdate);

		int nbofday = 0;
		Calendar current = startdatecal;
		while (current.getTimeInMillis() <= enddate.getTime()) {
			startdatecal.add(Calendar.DAY_OF_MONTH, 1);
			nbofday++;
		}
		return nbofday;

	}

	private class CGanttTask
			extends
			GanttTask {

		private ObjectDataElt payload;

		public CGanttTask(Date starttime, Date endtime, ObjectDataElt payload) {
			super(starttime, endtime);
			this.payload = payload;
		}

	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {
		GanttPlanning<CGanttTask> planning = new GanttPlanning<CGanttTask>();

		planning.addAttribute("LABEL");
		boolean[] showfields = new boolean[this.payloadlist.size()];
		for (int i = 0; i < this.payloadlist.size(); i++) {
			CBusinessField<?> thisfield = this.payloadlist.get(i);
			String fieldlabel = thisfield.getLabel();
			boolean show = true;
			if (thisfield.isShowinbottomnotes())
				show = false;
			showfields[i] = show;
			if (show) {
				planning.addAttribute(fieldlabel);
			}
		}
		// ----- Read all tasks
		// ----------------------------------------------------------
		data = getObjectArrayExternalContent(inputdata, datareference);

		ArrayList<CGanttTask> tasks = new ArrayList<CGanttTask>();
		HashMap<String, CGanttTask> tasksbyid = new HashMap<String, CGanttTask>();
		HashMap<String, String> valuesforcolormapping = new HashMap<String, String>();
		HashMap<String, String> valuesfordotmapping = new HashMap<String, String>();

		if (this.haslinks)
			link = getObjectArrayExternalContent(inputdata, this.linkdatareference);
		for (int i = 0; i < data.getObjectNumber(); i++) {
			ObjectDataElt thistask = data.getObjectAtIndex(i);
			Date starttime = getStartDate(thistask);
			Date endtime = getEndDate(thistask);
			String label = getLabel(thistask);
			CGanttTask task = new CGanttTask(starttime, endtime, thistask);
			tasksbyid.put(thistask.getUID(), task);
			task.setAttribute("LABEL", label);
			logger.fine("Processing label " + label);
			tasks.add(task);
			if (this.hascolorfield) {
				String colorfieldvalue = getFieldDisplayValue(this.colorfield, thistask);
				if (colorfieldvalue.length() > 0)
					valuesforcolormapping.put(colorfieldvalue, colorfieldvalue);
			}
			if (this.hasdotmarkfield) {
				String dotfieldvalue = getFieldDisplayValue(this.dotmarkfield, thistask);
				if (dotfieldvalue.length() > 0)
					valuesfordotmapping.put(dotfieldvalue, dotfieldvalue);
			}
			for (int j = 0; j < this.payloadlist.size(); j++) {
				if (showfields[j]) {
					String attributename = this.payloadlist.get(j).getFieldname();
					String attributelabel = this.payloadlist.get(j).getLabel();
					String attributevalue = getFieldDisplayValue(attributename, thistask);
					task.setAttribute(attributelabel, attributevalue);
				}
			}
		}

		Collections.sort(tasks, new Comparator<CGanttTask>() {

			@Override
			public int compare(CGanttTask first, CGanttTask second) {
				return first.getStarttime().compareTo(second.getStarttime());
			}

		});

		planning.addTasks(tasks);

		if (this.showmode == SHOWSIMPLEHOURS)
			if (this.hoursasdata) {
				this.hourstart = getIntegerExternalContent(inputdata, this.hourstartdataref).getPayload().intValue();
				this.hourend = getIntegerExternalContent(inputdata, this.hourenddataref).getPayload().intValue();
			} else {
				throw new RuntimeException("Show mode not implemented");
			}

		BusinessCalendar calendar = new BusinessCalendar();
		calendar.setBusinessHoursWindow(hourstart, hourend);

		if (this.haslinks) {

			// step 2 - go through link list
			for (int j = 0; j < this.link.getObjectNumber(); j++) {
				ObjectDataElt thislink = this.link.getObjectAtIndex(j);
				String id1 = ((TextDataElt) (thislink.lookupEltByName(this.linkid1field))).getPayload();
				String id2 = ((TextDataElt) (thislink.lookupEltByName(this.linkid2field))).getPayload();
				CGanttTask task1 = tasksbyid.get(id1);
				CGanttTask task2 = tasksbyid.get(id2);
				logger.info("Create dependency " + task1.getAttribute("LABEL") + " - " + task2.getAttribute("LABEL"));
				if (task1.payload.getUID().equals(task2.payload.getUID()))
					logger.severe("Create auto-dependency " + task1.getAttribute("LABEL") + " - "
							+ task2.getAttribute("LABEL"));
				planning.addEndStartDependency(task1, task2);
			}

		}

		GanttDisplay<CGanttTask> display = new GanttDisplay<CGanttTask>(planning, calendar);
		if (this.minchartwidth > 0)
			display.seMinWidth(this.minchartwidth);
		display.setTitleAttribute("LABEL");
		if (this.hascolorfield) {
			String colorlabel = payloadlistbyname.get(this.colorfield).getLabel();
			display.SetAttributeColorMapping(colorlabel, UNSETCOLOR);
			String[] colorvaluearray = valuesforcolormapping.keySet().toArray(new String[0]);
			for (int i = 0; i < colorvaluearray.length; i++) {
				display.addAttributeColorValueMapping(colorvaluearray[i], colorarray[i % colorarray.length]);
			}
		}
		if (this.hasdotmarkfield) {
			String dotmarklabel = payloadlistbyname.get(this.dotmarkfield).getLabel();
			display.setDotFieldMapping(dotmarklabel);
			String[] dotmarkarray = valuesfordotmapping.keySet().toArray(new String[0]);
			for (int i = 0; i < dotmarkarray.length; i++) {
				display.addAttributeDotValueMapping(dotmarkarray[i], dotcolorarray[i % dotcolorarray.length]);
			}
		}
		Date startdisplaywindows = tasks.get(0).getStarttime();
		Date enddisplaywindows = new Date(startdisplaywindows.getTime() + 86400000 * 21);
		display.setDisplayWindow(startdisplaywindows, enddisplaywindows);
		Node displaynode = display.getNode(actionmanager, parentwindow);
		if (this.action != null)
			actionmanager.registerEvent(display, action);
		logger.severe("Registered action on object " + display);

		if ((this.action != null) || (this.rescheduleaction != null)) {
			display.setGanttTaskOnMouseClicked(new GanttTaskMouseEventHandler<CGanttTask>() {

				@Override
				public void handle(MouseEvent event, CGanttTask selectedgantttask) {

					logger.severe("Action found on " + event.getSource() + " - " + event.getClickCount() + " - "
							+ event.getButton());

					if (event.getClickCount() == 2)
						if (event.getButton().equals(MouseButton.PRIMARY))
							if (action != null) {

								selectedelement = selectedgantttask.payload;
								actionmanager.getMouseHandler().handle(event);

							}
					if (event.getClickCount() == 1)
						if (event.getButton().equals(MouseButton.SECONDARY))
							if (rescheduleaction != null) {
								selectedelement = selectedgantttask.payload;
								Date startdate = getStartDate(selectedelement);
								Date enddate = getEndDate(selectedelement);
								String label = getLabel(selectedelement);
								timeslotforreschedule = new CTimeslotField(startdate, enddate, parentpathexposed);
								Node popupnode = timeslotforreschedule.getNode(actionmanager, inputdata, parentwindow,
										parenttabpanes);
								Label title = new Label("Reschedule " + label);
								title.setFont(Font.font(title.getFont().getName(), FontWeight.BOLD,
										title.getFont().getSize() * 1.2));
								DropShadow ds = new DropShadow();
								ds.setRadius(1.);
								ds.setOffsetX(1.);
								ds.setOffsetY(1.);
								ds.setColor(Color.color(0.8, 0.8, 0.8));
								title.setEffect(ds);
								title.setTextFill(Color.web("#17184B"));
								title.setPadding(new Insets(5, 5, 10, 50));
								Pane popuppane = CComponentBand.returnBandPane(CComponentBand.DIRECTION_DOWN);
								Pane buttonpane = CComponentBand.returnBandPane(CComponentBand.DIRECTION_RIGHT);
								Button reschedule = new Button("Reschedule");
								reschedule.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
								buttonpane.getChildren().add(reschedule);
								popuppane.getChildren().add(title);
								popuppane.getChildren().add(popupnode);
								popuppane.getChildren().add(buttonpane);
								actionmanager.registerEvent(reschedule, rescheduleaction);
								reschedule.setOnMouseClicked(new CActionButton.ButtonHandler(actionmanager));

								CPopupButton.generateAndShowPopup(displaynode, popuppane, inputdata, parentwindow,
										false, false);

							}

				}
			});
		}

		return displaynode;

	}

	@Override
	public void mothball() {

	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("not yet implemented");

	}

	/**
	 * extracts an array of objects
	 * 
	 * @param inputdata page data
	 * @param dataref   data reference to use
	 * @return an array of data objects
	 */
	public ArrayDataElt<ObjectDataElt> getObjectArrayExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		// control not perfect
		if (!(thiselement instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> thisobjectarray = (ArrayDataElt<ObjectDataElt>) thiselement;
		return thisobjectarray;
	}

	/**
	 * get an integer element from page data
	 * 
	 * @param inputdata page data
	 * @param dataref   data reference to use
	 * @return an integer data element
	 */
	public IntegerDataElt getIntegerExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		if (!(thiselement instanceof IntegerDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		IntegerDataElt thisinteger = (IntegerDataElt) thiselement;
		return thisinteger;
	}

	/**
	 * get a text element from page data
	 * 
	 * @param inputdata page data
	 * @param dataref   data reference to use
	 * @return a text data element
	 */
	public TextDataElt getTextExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = %s" + dataref.getName());
		if (!(thiselement instanceof TextDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		TextDataElt thistext = (TextDataElt) thiselement;
		return thistext;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		if (type instanceof ObjectIdDataEltType) {
			if (objectfieldname.equals("SELECTEDTIMESLOT")) {
				ObjectDataElt object = this.selectedelement;
				SimpleDataElt field = object.lookupEltByName("ID");
				if (!(field instanceof TextDataElt))
					throw new RuntimeException("field for name = ID is not text");
				TextDataElt textfield = (TextDataElt) field;
				ObjectIdDataElt objectid = new ObjectIdDataElt(eltname, textfield.getPayload());
				return objectid;
			}

			throw new RuntimeException(
					"Only two parameters for getting object ID are 'ROOTTIMESLOT' and 'SELECTEDTIMESLOT', but got "
							+ objectfieldname);
		}
		if (type instanceof DateDataEltType) {
			if (objectfieldname.equals("RESCHEDULESTARTDATE")) {
				return timeslotforreschedule.getDataElt(type, eltname, "STARTDATE");
			}
			if (objectfieldname.equals("RESCHEDULEENDDATE")) {
				return timeslotforreschedule.getDataElt(type, eltname, "ENDDATE");
			}
			throw new RuntimeException(
					"Only two parameters for getting object ID are 'RESCHEDULESTARTDATE' and 'RESCHEDULEENDDATE', but got "
							+ objectfieldname);

		}

		throw new RuntimeException(String.format("Unsupported extraction type %s ", type));
	}

}
