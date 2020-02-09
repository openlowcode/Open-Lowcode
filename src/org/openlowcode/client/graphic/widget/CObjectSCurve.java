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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.client.action.CInlineActionDataRef;
import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.graphic.widget.tools.TemporaryDateAxis;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.ObjectDataEltType;
import org.openlowcode.tools.structure.ObjectIdDataElt;
import org.openlowcode.tools.structure.ObjectIdDataEltType;
import org.openlowcode.tools.structure.SimpleDataElt;
import javafx.scene.layout.StackPane;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.scene.control.ListView;
import javafx.scene.control.TabPane;
import javafx.scene.paint.Color;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

/**
 * A S-Curve counting objects opened and closed in time
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CObjectSCurve
		extends
		CPageNode {

	private ArrayList<ObjectDataElt> objectarray;
	private ArrayList<ObjectDataElt> objectsbygrowingreddate;
	private ArrayList<ObjectDataElt> objectsbygrowinggreendate;
	private LineChart<Date, Number> lineChart;
	private XYChart.Series<Date, Number> redseries;
	private XYChart.Series<Date, Number> greenseries;
	private XYChart.Series<Date, Number> dottedgreenseries;
	private String greendatapointstyle;
	private String reddatapointstyle;
	private HashMap<XYChart.Data<?, ?>, ArrayList<ObjectDataElt>> objectsbydatapoint;
	private XYChart.Data<?, ?> currentdatapoint;
	private ArrayList<ObjectDataElt> objectsbygrowingdottedgreendate;
	@SuppressWarnings("unused")
	private String name;
	private boolean inputdata;
	private CPageDataRef datareference;
	private ArrayList<CBusinessField<?>> payloadlist;
	private String showred;
	private String showgreen;
	private String showgreendotted;
	private ArrayList<String> fieldstoshow;
	private CPageAction defaultaction;
	private boolean inline;
	private CInlineActionDataRef inlineactiondataref;
	private Date minimumdate;
	private Date maximumdate;
	private String redsmallpointstype;
	private String greensmallpointstyle;
	private Popup datapopup;

	private ListView<String> objectslistfordot;

	/**
	 * create an object S-Curve from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CObjectSCurve(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.name = reader.returnNextStringField("NAME");
		this.inputdata = reader.returnNextBooleanField("IND");
		if (this.inputdata) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
		}
		this.inline = reader.returnNextBooleanField("INL");

		if (inline) {
			this.inlineactiondataref = new CInlineActionDataRef(reader, this);

		}
		payloadlist = new ArrayList<CBusinessField<?>>();
		reader.returnNextStartStructure("ATTRS");
		while (reader.structureArrayHasNextElement("ATTR")) {
			@SuppressWarnings("rawtypes")
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);

			payloadlist.add(thisfield);
			reader.returnNextEndStructure("ATTR");
		}
		boolean isshowred = reader.returnNextBooleanField("ISR");
		if (isshowred) {
			this.showred = reader.returnNextStringField("SHR");
		}
		boolean isshowgreen = reader.returnNextBooleanField("ISG");
		if (isshowgreen) {
			this.showgreen = reader.returnNextStringField("SHG");
		}
		boolean isshowgreendotted = reader.returnNextBooleanField("ISGD");
		if (isshowgreendotted) {
			this.showgreendotted = reader.returnNextStringField("SHGD");
		}
		fieldstoshow = new ArrayList<String>();
		reader.returnNextStartStructure("FTSWS");
		while (reader.structureArrayHasNextElement("FTSW")) {
			fieldstoshow.add(reader.returnNextStringField("FLD"));
			reader.returnNextEndStructure("FTSW");
		}
		boolean isdefaultaction = reader.returnNextBooleanField("IDF");
		if (isdefaultaction) {
			reader.returnNextStartStructure("DFT");
			reader.returnNextStartStructure("ACTION");
			this.defaultaction = new CPageAction(reader);
			reader.returnNextEndStructure("DFT");
		}
		reader.returnNextEndStructure("SCURVE");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	public ArrayDataElt<ObjectDataElt> getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		// control not perfect
		if (!(thiselement instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> thiselementarray = (ArrayDataElt<ObjectDataElt>) thiselement;
		return thiselementarray;
	}

	private void setData(ArrayDataElt<ObjectDataElt> data) {
		objectarray = new ArrayList<ObjectDataElt>();
		this.minimumdate = new Date(System.currentTimeMillis() * 2);
		this.maximumdate = new Date(0);
		if (this.showred != null) {
			objectsbygrowingreddate = new ArrayList<ObjectDataElt>();
		}
		if (this.showgreen != null) {
			objectsbygrowinggreendate = new ArrayList<ObjectDataElt>();
		}
		if (this.showgreendotted != null) {
			objectsbygrowingdottedgreendate = new ArrayList<ObjectDataElt>();
		}

		for (int i = 0; i < data.getObjectNumber(); i++) {
			ObjectDataElt thisobject = data.getObjectAtIndex(i);
			objectarray.add(thisobject);
			if (this.showred != null) {

				SimpleDataElt thiselement = thisobject.lookupEltByName(showred);
				if (thiselement == null)
					throw new RuntimeException(" showred field does not exist " + showred);
				if (!(thiselement instanceof DateDataElt))
					throw new RuntimeException(
							"showred field " + showred + " should be date but is " + thiselement.getClass().getName());
				DateDataElt dateelt = (DateDataElt) thiselement;
				if (dateelt.getPayload() != null) {
					Date payload = dateelt.getPayload();
					if (payload.before(minimumdate))
						minimumdate = payload;
					if (payload.after(maximumdate))
						maximumdate = payload;
					objectsbygrowingreddate.add(thisobject);

				}
			}
			boolean isgreen = false;
			if (this.showgreen != null) {
				SimpleDataElt thiselement = thisobject.lookupEltByName(showgreen);

				if (thiselement == null)
					throw new RuntimeException(" showgreen field does not exist " + showgreen);
				if (!(thiselement instanceof DateDataElt))
					throw new RuntimeException("showgreen field " + showgreen + " should be date but is "
							+ thiselement.getClass().getName());
				DateDataElt dateelt = (DateDataElt) thiselement;
				if (dateelt.getPayload() != null) {
					Date payload = dateelt.getPayload();
					if (payload.before(minimumdate))
						minimumdate = payload;
					if (payload.after(maximumdate))
						maximumdate = payload;
					objectsbygrowinggreendate.add(thisobject);
					isgreen = true;
				}
			}
			if (this.showgreendotted != null) {
				SimpleDataElt thiselement = thisobject.lookupEltByName(showgreendotted);
				if (thiselement == null)
					throw new RuntimeException(" showgreendotted field does not exist " + showgreendotted);
				if (!(thiselement instanceof DateDataElt))
					throw new RuntimeException("showgreendotted field " + showgreendotted + " should be date but is "
							+ thiselement.getClass().getName());
				DateDataElt dateelt = (DateDataElt) thiselement;
				if (dateelt.getPayload() != null) {
					Date payload = dateelt.getPayload();
					if (payload.before(minimumdate))
						minimumdate = payload;
					if (payload.after(maximumdate))
						maximumdate = payload;
					if (!isgreen)
						objectsbygrowingdottedgreendate.add(thisobject);
				}
			}
		}
		if (maximumdate.before(new Date()))
			maximumdate = new Date();
		logger.fine("   *-*-*-* Found range of dates from " + minimumdate + " to " + maximumdate);
		Collections.sort(objectsbygrowingreddate, new DateFieldComparator(showred));
		Collections.sort(objectsbygrowinggreendate, new DateFieldComparator(showgreen));
		if (objectsbygrowingdottedgreendate != null) {
			Collections.sort(objectsbygrowingdottedgreendate, new DateFieldComparator(showgreendotted));
		}

		logger.fine("   *-*-*-* ordered list red " + objectsbygrowingreddate.size() + " ordered list green "
				+ objectsbygrowinggreendate.size() + " ordered list dotted green "
				+ (objectsbygrowingdottedgreendate != null ? objectsbygrowingdottedgreendate.size() : "null"));

	}

	/**
	 * sets the data in the chart
	 */
	public void setDataInChart() {
		lineChart.getXAxis().setAutoRanging(true);

		objectsbydatapoint = new HashMap<XYChart.Data<?, ?>, ArrayList<ObjectDataElt>>();
		ArrayList<ObjectDataElt> objectsforcurrentdot = new ArrayList<ObjectDataElt>();
		int datapoint = objectarray.size() / 2;
		if (datapoint < 40)
			datapoint = 40;
		if (datapoint > 80)
			datapoint = 80;
		long increment = ((maximumdate.getTime() - minimumdate.getTime()) / datapoint);
		int redseriesindex = 0;
		// ----------------------------
		// -- RED SERIES --------------

		if (redseries != null) {
			redseries.getData().clear();
			int incrementindex = 0;
			Date currentreddate = null;
			int currentredvalue = 0;
			while (incrementindex <= datapoint) {
				Date thisdatapointdate = new Date(minimumdate.getTime() + incrementindex * increment);
				if (redseriesindex < objectsbygrowingreddate.size())
					do {
						ObjectDataElt thisobject = objectsbygrowingreddate.get(redseriesindex);
						currentreddate = ((DateDataElt) (thisobject.lookupEltByName(showred))).getPayload();
						if (currentreddate.compareTo(thisdatapointdate) <= 0) {
							redseriesindex++;
							objectsforcurrentdot.add(thisobject);

						}
					} while ((redseriesindex < objectsbygrowingreddate.size())
							&& (currentreddate.compareTo(thisdatapointdate) <= 0));

				Data<Date, Number> thisdata = new XYChart.Data<Date, Number>(thisdatapointdate, redseriesindex);
				if (redseriesindex != currentredvalue) {
					thisdata.setNode(new DataPointStackPane(thisdata));
				}
				redseries.getData().add(thisdata);

				if (redseriesindex != currentredvalue) {

					thisdata.getNode().setStyle(reddatapointstyle);
					objectsbydatapoint.put(thisdata, objectsforcurrentdot);
					objectsforcurrentdot = new ArrayList<ObjectDataElt>();
					currentredvalue = redseriesindex;
				} else {
					thisdata.getNode().setStyle(this.redsmallpointstype);

				}
				incrementindex++;
			}
			logger.fine(" -*-*-*-* adding values for red diagram " + incrementindex);

		}
		// ----------------------------
		// -- GREEN SERIES ------------
		int greenseriesindex = 0;
		if (greenseries != null) {
			greenseries.getData().clear();
			int incrementindex = 0;
			Date currentgreendate = null;
			int currentgreenvalue = 0;
			while (incrementindex <= datapoint) {
				Date thisdatapointdate = new Date(minimumdate.getTime() + incrementindex * increment);
				if (thisdatapointdate.after(new Date()))
					break;
				if (greenseriesindex < objectsbygrowinggreendate.size())
					do {
						ObjectDataElt thisobject = objectsbygrowinggreendate.get(greenseriesindex);

						currentgreendate = ((DateDataElt) (thisobject.lookupEltByName(showgreen))).getPayload();
						if (currentgreendate.compareTo(thisdatapointdate) <= 0) {
							greenseriesindex++;
							objectsforcurrentdot.add(thisobject);
						}
					} while ((greenseriesindex < objectsbygrowinggreendate.size())
							&& (currentgreendate.compareTo(thisdatapointdate) <= 0));
				Data<Date, Number> thisdata = new XYChart.Data<Date, Number>(thisdatapointdate, greenseriesindex);
				if (greenseriesindex != currentgreenvalue) {
					thisdata.setNode(new DataPointStackPane(thisdata));
				}

				greenseries.getData().add(thisdata);
				if (greenseriesindex != currentgreenvalue) {

					thisdata.getNode().setStyle(greendatapointstyle);
					objectsbydatapoint.put(thisdata, objectsforcurrentdot);
					objectsforcurrentdot = new ArrayList<ObjectDataElt>();
					currentgreenvalue = greenseriesindex;
				} else {
					thisdata.getNode().setStyle(this.greensmallpointstyle);
				}
				incrementindex++;
			}
			Date now = new Date();
			Data<Date, Number> thisdata = new XYChart.Data<Date, Number>(now, objectsbygrowinggreendate.size());
			greenseries.getData().add(thisdata);
			thisdata.getNode().setStyle(greendatapointstyle);
			logger.fine(" -*-*-*-* adding values for green diagram " + incrementindex);

		}

		if (dottedgreenseries != null) {
			dottedgreenseries.getData().clear();
			int dottedgreenseriesindex = 0;
			Date currentdottedgreendate = null;
			Date now = new Date();
			int currentdottedgreen = 0;
			if (objectsbygrowingdottedgreendate != null)
				if (objectsbygrowingdottedgreendate.size() > 0)
					do {
						ObjectDataElt thisobject = objectsbygrowingdottedgreendate.get(dottedgreenseriesindex);

						currentdottedgreendate = ((DateDataElt) (thisobject.lookupEltByName(showgreendotted)))
								.getPayload();
						if (currentdottedgreendate.compareTo(now) <= 0) {
							dottedgreenseriesindex++;
							objectsforcurrentdot.add(thisobject);

						}
					} while ((dottedgreenseriesindex < objectsbygrowingdottedgreendate.size())
							&& (currentdottedgreendate.compareTo(now) <= 0));

			Data<Date, Number> thisdatafornow = new XYChart.Data<Date, Number>(now,
					dottedgreenseriesindex + objectsbygrowinggreendate.size());
			if (dottedgreenseriesindex != 0) {
				thisdatafornow.setNode(new DataPointStackPane(thisdatafornow));
			}

			dottedgreenseries.getData().add(thisdatafornow);

			if (dottedgreenseriesindex != 0) {
				thisdatafornow.getNode().setStyle(greendatapointstyle);
				currentdottedgreen = dottedgreenseriesindex;
				objectsbydatapoint.put(thisdatafornow, objectsforcurrentdot);
				objectsforcurrentdot = new ArrayList<ObjectDataElt>();
			} else {
				thisdatafornow.getNode().setStyle(greensmallpointstyle);

			}
			int incrementindex = 0;
			while (incrementindex <= datapoint) {
				Date thisdatapointdate = new Date(minimumdate.getTime() + incrementindex * increment);
				if (thisdatapointdate.after(now)) {
					if (objectsbygrowingdottedgreendate != null)
						if (dottedgreenseriesindex < objectsbygrowingdottedgreendate.size())
							do {
								ObjectDataElt thisobject = objectsbygrowingdottedgreendate.get(dottedgreenseriesindex);
								currentdottedgreendate = ((DateDataElt) (thisobject.lookupEltByName(showgreendotted)))
										.getPayload();
								if (currentdottedgreendate.compareTo(thisdatapointdate) <= 0) {
									dottedgreenseriesindex++;
									objectsforcurrentdot.add(thisobject);

								}
							} while ((dottedgreenseriesindex < objectsbygrowingdottedgreendate.size())
									&& (currentdottedgreendate.compareTo(thisdatapointdate) <= 0));
					Data<Date, Number> thisdata = new XYChart.Data<Date, Number>(thisdatapointdate,
							dottedgreenseriesindex + objectsbygrowinggreendate.size());
					if (dottedgreenseriesindex != currentdottedgreen) {
						thisdata.setNode(new DataPointStackPane(thisdata));
					}

					dottedgreenseries.getData().add(thisdata);
					if (dottedgreenseriesindex != currentdottedgreen) {
						thisdata.getNode().setStyle(greendatapointstyle);
						currentdottedgreen = dottedgreenseriesindex;
						objectsbydatapoint.put(thisdata, objectsforcurrentdot);
						objectsforcurrentdot = new ArrayList<ObjectDataElt>();
					} else {
						thisdata.getNode().setStyle(greensmallpointstyle);

					}
				}
				incrementindex++;
			}

		}

	}

	/**
	 * A comparator that will compare objects according to a date feidl
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class DateFieldComparator
			implements
			Comparator<ObjectDataElt> {
		private String fieldmarker;

		/**
		 * creates the comparator based on the given field name
		 * 
		 * @param fieldmarker field name
		 */
		public DateFieldComparator(String fieldmarker) {
			this.fieldmarker = fieldmarker;
		}

		@Override
		public int compare(ObjectDataElt object1, ObjectDataElt object2) {
			try {
				Date object1date = ((DateDataElt) (object1.lookupEltByName(fieldmarker))).getPayload();
				Date object2date = ((DateDataElt) (object2.lookupEltByName(fieldmarker))).getPayload();
				return (object1date.compareTo(object2date));
			} catch (Exception e) {
				logger.fine("Exception in comparison of SCurve : " + e.getMessage());
				e.printStackTrace(System.err);
				return 0;
			}

		}

	}

	@SuppressWarnings("unchecked")
	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes) {

		if (this.datareference != null) {
			ArrayDataElt<ObjectDataElt> data = getExternalContent(inputdata, datareference);
			setData(data);
		}
		if (this.inlineactiondataref != null) {

			inputdata.addInlineActionDataRef(this.inlineactiondataref);
		}
		TemporaryDateAxis xAxis = new TemporaryDateAxis(new Date(System.currentTimeMillis() - 86400l * 1000l * 60l),
				new Date(System.currentTimeMillis() + 86400l * 30l * 1000l));

		NumberAxis yAxis = new NumberAxis();
		xAxis.setLabel("time");
		yAxis.setLabel("cumulative tickets");
		lineChart = new LineChart<Date, Number>(xAxis, yAxis);
		lineChart.setMaxWidth(1300);
		lineChart.getYAxis().setAutoRanging(true);

		redseries = new XYChart.Series<Date, Number>();
		redseries.setName("Open");

		greenseries = new XYChart.Series<Date, Number>();

		dottedgreenseries = new XYChart.Series<Date, Number>();

		lineChart.getData().addAll(redseries, greenseries, dottedgreenseries);

		Color colorred = Color.RED;

		String rgbred = String.format("%d, %d, %d", (int) (colorred.getRed() * 255), (int) (colorred.getGreen() * 255),
				(int) (colorred.getBlue() * 255));

		// lineseries1.setStyle("-fx-stroke: rgba(" + rgbred + ", 1.0);");
		redseries.getNode().setStyle("-fx-stroke: rgba(" + rgbred
				+ ", 1.0); -fx-stroke-width: 2px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");

		this.reddatapointstyle = "-fx-background-color: rgba(" + rgbred
				+ ",1.0), white;  -fx-background-radius: 1px; -fx-padding: 3px;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);";
		this.redsmallpointstype = "-fx-background-color: rgba(" + rgbred
				+ ",1.0), white;  -fx-background-radius: 1px; -fx-padding: 0px;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);";
		
		Color colorgreen = Color.GREEN;

		String rgbgreen = String.format("%d, %d, %d", (int) (colorgreen.getRed() * 255),
				(int) (colorgreen.getGreen() * 255), (int) (colorgreen.getBlue() * 255));

		greenseries.getNode().setStyle("-fx-stroke: rgba(" + rgbgreen
				+ ", 1.0); -fx-stroke-width: 2px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
		dottedgreenseries.getNode().setStyle("-fx-stroke: rgba(" + rgbgreen
				+ ", 1.0); -fx-stroke-dash-array: 0.1 5.0; -fx-stroke-width: 2px; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);");
		// lineseries2.setStyle("-fx-stroke: rgba(" + rgbgreen + ", 1.0);");
		this.greendatapointstyle = "-fx-background-color: rgba(" + rgbgreen
				+ ",1.0), white;  -fx-background-radius: 1px; -fx-padding: 3px;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);";
		this.greensmallpointstyle = "-fx-background-color: rgba(" + rgbgreen
				+ ",1.0), white;  -fx-background-radius: 1px; -fx-padding: 0px;-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.8), 10, 0, 0, 0);";

		lineChart.setLegendVisible(false);
		objectslistfordot = new ListView<String>();
		objectslistfordot.setMaxHeight(120);
		objectslistfordot.setMaxWidth(40 + fieldstoshow.size() * 60);

		actionmanager.registerEvent(objectslistfordot, defaultaction);
		logger.fine("          *-*-*-*-* Registered in action manager object " + objectslistfordot + " for action "
				+ defaultaction);
		objectslistfordot.addEventHandler(ActionEvent.ACTION, actionmanager);
		objectslistfordot.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent arg0) {
				objectslistfordot.fireEvent(new ActionEvent());

				datapopup.hide();
			}
		});
		objectslistfordot.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent keyevent) {
				if (keyevent.getCode() == KeyCode.ESCAPE) {
					datapopup.hide();
				}

			}
		});
		objectslistfordot.setOnMouseExited(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				datapopup.hide();
			}

		});

		datapopup = new Popup();
		datapopup.getContent().add(objectslistfordot);

		if (this.objectarray != null)
			setDataInChart();
		return lineChart;

	}

	private class DataPointStackPane
			extends
			StackPane {
		DataPointStackPane(XYChart.Data<?, ?> datapoint) {
			super();
			DataPointStackPane own = this;
			this.setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {
					double wx = own.getScene().getX() + own.getScene().getWindow().getX();
					double wy = own.getScene().getY() + own.getScene().getWindow().getY();
					Point2D point = own.localToScene(0, 0);
					currentdatapoint = datapoint;
					ArrayList<ObjectDataElt> objectsfordot = objectsbydatapoint.get(datapoint);
					if (objectsfordot.size() > 0) {
						ArrayList<String> itemstoshow = new ArrayList<String>();
						for (int i = 0; i < objectsfordot.size(); i++) {
							ObjectDataElt thisobject = objectsfordot.get(i);
							StringBuffer display = new StringBuffer();
							for (int j = 0; j < fieldstoshow.size(); j++) {
								SimpleDataElt fieldtoshow = thisobject.lookupEltByName(fieldstoshow.get(j));
								if (fieldtoshow == null) {
									display.append("#ERROR:" + fieldstoshow.get(j) + "#");
								} else {
									if (display.length() > 0)
										display.append(' ');
									display.append(fieldtoshow.defaultTextRepresentation());
								}

							}
							itemstoshow.add(display.toString());
						}
						objectslistfordot.setItems(FXCollections.observableList(itemstoshow));
						double weight = 120;
						if (12 + itemstoshow.size() * 24 < 120)
							weight = 12 + itemstoshow.size() * 24;
						objectslistfordot.setMaxHeight(weight);
						objectslistfordot.setMinHeight(weight);
						datapopup.show(own, wx + point.getX() - 10, wy + point.getY() - 10);
					} else {
						logger.fine("Warning: datapoint with empty data");
					}
				}
			});
			own.setOnMouseExited(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent mouseEvent) {

					logger.fine("hovering exit");
				}
			});
		}
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		if (type instanceof ObjectIdDataEltType) {
			int index = objectslistfordot.getSelectionModel().getSelectedIndex();
			ObjectDataElt object = objectsbydatapoint.get(currentdatapoint).get(index);
			return new ObjectIdDataElt(eltname, object.lookupEltByName("ID").defaultTextRepresentation());

		}
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		if (!(dataelt instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("inline page data does not have expected %s type, actually found %s",
							dataelt.getName(), dataelt.getType()));
		ArrayDataElt<?> arraydataelt = (ArrayDataElt<?>) dataelt;
		if (!(arraydataelt.getArrayPayloadEltType() instanceof ObjectDataEltType))
			throw new RuntimeException("expecting object data element type");
		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> objectarraydataelt = (ArrayDataElt<ObjectDataElt>) arraydataelt;
		logger.fine("processing force update data");
		setData(objectarraydataelt);
		setDataInChart();
	}

	@Override
	public void mothball() {

	}

}
