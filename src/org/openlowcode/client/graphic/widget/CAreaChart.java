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
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

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
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.stage.Window;
import javafx.scene.chart.StackedAreaChart;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.control.TabPane;

/**
 * A chart showing several series of number evolution with time
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CAreaChart
		extends
		CPageNode {
	@SuppressWarnings("unused")
	private String name;
	private CPageDataRef datareference;
	private ArrayList<CBusinessField<?>> payloadlist;
	private String xaxisattribute;
	private String yattributecategory;
	private String yattributevalue;
	private ArrayList<ObjectDataElt> objectarray;
	private Date minimumdate;
	private Date maximumdate;
	private HashMap<Date, HashMap<String, ObjectDataElt>> objectsbydateandcategory;
	private ArrayDataElt<ObjectDataElt> currentdata;
	private ArrayList<String> orderedcategories;
	private HashMap<String, String> allcategories;

	/**
	 * Create a CAreaChart
	 * 
	 * @param reader     reader
	 * @param parentpath parent path of the widget
	 * @throws OLcRemoteException if something bad happens on the server
	 * @throws IOException        if something bad happens during the transmission
	 *                            between the client and the server
	 */
	public CAreaChart(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		this.name = reader.returnNextStringField("NAME");
		this.datareference = CPageDataRef.parseCPageDataRef(reader);

		payloadlist = new ArrayList<CBusinessField<?>>();
		reader.returnNextStartStructure("ATTRS");
		while (reader.structureArrayHasNextElement("ATTR")) {
			@SuppressWarnings("rawtypes")
			CBusinessField thisfield = CBusinessField.parseBusinessField(reader, parentpath);
			thisfield.setParentforfield(this);

			payloadlist.add(thisfield);
			reader.returnNextEndStructure("ATTR");
		}
		this.xaxisattribute = reader.returnNextStringField("XAXIS");
		this.yattributecategory = reader.returnNextStringField("YAXISCATEGORY");
		this.yattributevalue = reader.returnNextStringField("YAXISVALUE");

		reader.returnNextEndStructure("ACHART");
	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		currentdata = getExternalContent(inputdata, datareference);
		setData(currentdata);
		TemporaryDateAxis xaxis = new TemporaryDateAxis();
		NumberAxis yaxis = new NumberAxis();
		HashMap<
				String,
				XYChart.Series<Date, Number>> seriesbycategory = new HashMap<String, XYChart.Series<Date, Number>>();
		StackedAreaChart<Date, Number> sac = new StackedAreaChart<Date, Number>(xaxis, yaxis);
		for (int i = 0; i < this.orderedcategories.size(); i++) {
			XYChart.Series<Date, Number> series = new XYChart.Series<Date, Number>();
			series.setName(this.orderedcategories.get(i));
			sac.getData().add(series);
			seriesbycategory.put(this.orderedcategories.get(i), series);

		}

		List<Date> datelist = new ArrayList<Date>();
		datelist.addAll(objectsbydateandcategory.keySet());
		Collections.sort(datelist);
		for (int i = 0; i < datelist.size(); i++) {
			Date currentdate = datelist.get(i);
			HashMap<String, ObjectDataElt> valuesfordate = objectsbydateandcategory.get(currentdate);
			for (int j = 0; j < this.orderedcategories.size(); j++) {
				XYChart.Series<Date, Number> relevantseries = seriesbycategory.get(orderedcategories.get(j));
				ObjectDataElt relevantobject = valuesfordate.get(orderedcategories.get(j));
				if (relevantobject != null) {
					double value = getValueFromObject(relevantobject);
					relevantseries.getData().add(new Data<Date, Number>(currentdate, value));
				} else {
					double value = 0f;
					relevantseries.getData().add(new Data<Date, Number>(currentdate, value));
				}
			}
		}
		return sac;
	}

	private double getValueFromObject(ObjectDataElt data) {
		SimpleDataElt thiselement = data.lookupEltByName(this.yattributevalue);
		if (thiselement == null)
			throw new RuntimeException(" xaxisattribute field does not exist " + xaxisattribute);
		Double value = null;
		boolean treated = false;
		if (thiselement instanceof IntegerDataElt) {
			IntegerDataElt integerdataelt = (IntegerDataElt) thiselement;
			value = new Double(integerdataelt.getPayload().intValue());
			treated = true;
		}
		if (thiselement instanceof DecimalDataElt) {
			DecimalDataElt decimaldataelt = (DecimalDataElt) thiselement;
			value = new Double(decimaldataelt.getPayload().doubleValue());
			treated = true;
		}
		if (!treated)
			throw new RuntimeException("Type not supported " + thiselement.getType().toString());
		if (value == null)
			return 0;
		return value.doubleValue();

	}

	private void setData(ArrayDataElt<ObjectDataElt> data) {
		objectarray = new ArrayList<ObjectDataElt>();

		orderedcategories = new ArrayList<String>();
		allcategories = new HashMap<String, String>();

		this.minimumdate = new Date(System.currentTimeMillis() * 2);
		this.maximumdate = new Date(0);
		objectsbydateandcategory = new HashMap<Date, HashMap<String, ObjectDataElt>>();
		for (int i = 0; i < data.getObjectNumber(); i++) {
			ObjectDataElt thisobject = data.getObjectAtIndex(i);
			// ------------------------- processing xaxis date
			objectarray.add(thisobject);
			SimpleDataElt thiselement = thisobject.lookupEltByName(xaxisattribute);
			if (thiselement == null)
				throw new RuntimeException(" xaxisattribute field does not exist " + xaxisattribute);
			if (!(thiselement instanceof DateDataElt))
				throw new RuntimeException("xaxisattribute field " + xaxisattribute + " should be date but is "
						+ thiselement.getClass().getName());
			DateDataElt dateelt = (DateDataElt) thiselement;
			if (dateelt.getPayload() != null) {
				Date xaxisdate = dateelt.getPayload();
				if (xaxisdate.before(minimumdate))
					minimumdate = xaxisdate;
				if (xaxisdate.after(maximumdate))
					maximumdate = xaxisdate;
				HashMap<String, ObjectDataElt> objectsbydate = objectsbydateandcategory.get(xaxisdate);
				if (objectsbydate == null) {
					objectsbydate = new HashMap<String, ObjectDataElt>();
					objectsbydateandcategory.put(xaxisdate, objectsbydate);

				}
				// processing yaxiscategory
				SimpleDataElt yaxiscategoryse = thisobject.lookupEltByName(this.yattributecategory);
				if (yaxiscategoryse == null)
					throw new RuntimeException(" xaxisattribute field does not exist " + xaxisattribute);
				String defaultstringvalue = yaxiscategoryse.defaultTextRepresentation();
				if (defaultstringvalue == null)
					defaultstringvalue = "";
				if (objectsbydate.containsKey(defaultstringvalue))
					throw new RuntimeException("duplicate value for date " + xaxisdate + " and category "
							+ defaultstringvalue + " for object index = " + i);
				objectsbydate.put(defaultstringvalue, thisobject);
				if (allcategories.get(defaultstringvalue) == null) {
					allcategories.put(defaultstringvalue, defaultstringvalue);
					orderedcategories.add(defaultstringvalue);
				}

			}

		}
	}

	/**
	 * gets the content in the chart from the page data
	 * 
	 * @param inputdata input data
	 * @param dataref   reference of data
	 * @return an array of objects
	 */
	public ArrayDataElt<ObjectDataElt> getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException(
					String.format("could not find any page data with name = %s" + dataref.getName()));
		// control not perfect
		if (!(thiselement instanceof ArrayDataElt))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		@SuppressWarnings("unchecked")
		ArrayDataElt<ObjectDataElt> thiselementarray = (ArrayDataElt<ObjectDataElt>) thiselement;
		return thiselementarray;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		return null;
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public void mothball() {
	}

}
