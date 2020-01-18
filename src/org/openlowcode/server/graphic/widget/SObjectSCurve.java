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
import java.util.ArrayList;
import java.util.function.Function;

import org.openlowcode.server.action.SActionInputDataRef;
import org.openlowcode.server.action.SActionOutputDataRef;
import org.openlowcode.server.action.SActionRef;
import org.openlowcode.server.action.SInlineActionRef;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectFieldMarker;
import org.openlowcode.server.data.message.TObjectDataElt;
import org.openlowcode.server.data.message.TObjectDataEltType;
import org.openlowcode.server.graphic.SDefaultPath;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.SimpleDataEltType;

/**
 * A widget showing a S-curve with dates objects were created, and dates objects
 * were closed. If present, target date for open items is shown
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E>
 */
public class SObjectSCurve<E extends DataObject<E>>
		extends
		SPageNode
		implements
		SDefaultPath {
	private ArrayDataElt<TObjectDataElt<E>> inputdata;
	private String name;
	private DataObjectDefinition<E> objectmodel;
	private DataObjectFieldMarker<E> showred;
	private DataObjectFieldMarker<E> showgreen;
	private DataObjectFieldMarker<E> showgreendotted;
	private ArrayList<DataObjectFieldMarker<E>> fieldstoshow;
	private SActionRef defaultaction;
	private SInlineActionRef inlineaction;
	private SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineoutputdata;

	/**
	 * creates an object S-curve widget
	 * 
	 * @param name        unique name of the s-curve widget
	 * @param inputdata   input data for the S-Curve
	 * @param objectmodel definition of the parent data object
	 * @param parent      parent page for this widget
	 */
	public SObjectSCurve(
			String name,
			ArrayDataElt<TObjectDataElt<E>> inputdata,
			DataObjectDefinition<E> objectmodel,
			SPage parent) {
		super(parent);
		this.name = name;
		this.inputdata = inputdata;
		this.objectmodel = objectmodel;
		fieldstoshow = new ArrayList<DataObjectFieldMarker<E>>();
	}

	/**
	 * craetes an object S-Curve widget that dispalys the result of an inline action
	 * 
	 * @param name             unique name of the s-curve widget
	 * @param inlineaction     inline action for which the output will be put on the
	 *                         widget
	 * @param inlineoutputdata output data of the inline action to show (array of
	 *                         objects)
	 * @param objectmodel      definition of the parent data object
	 * @param parent           parent page for this widget
	 */
	public SObjectSCurve(
			String name,
			SInlineActionRef inlineaction,
			SActionOutputDataRef<ArrayDataEltType<TObjectDataEltType<E>>> inlineoutputdata,
			DataObjectDefinition<E> objectmodel,
			SPage parent) {
		super(parent);
		this.name = name;
		this.inlineaction = inlineaction;
		this.inlineoutputdata = inlineoutputdata;
		this.objectmodel = objectmodel;
		fieldstoshow = new ArrayList<DataObjectFieldMarker<E>>();
	}

	/**
	 * specifies the field to show when clicking on a dot (then, the objects in this
	 * dot on the diagram will display a short sentence
	 * 
	 * @param fieldtoshow field to show
	 */
	public void addFieldToShow(DataObjectFieldMarker<E> fieldtoshow) {
		fieldstoshow.add(fieldtoshow);
	}

	/**
	 * sets the field showing as red (typically the open date for object). Should be
	 * a date
	 * 
	 * @param redfield field to use as red field
	 */
	public void setRed(DataObjectFieldMarker<E> redfield) {
		this.showred = redfield;
	}

	/**
	 * sets the field showing as green (typically the close date for object). Should
	 * be a date
	 * 
	 * @param greenfield field to use as green field
	 */
	public void setGreen(DataObjectFieldMarker<E> greenfield) {
		this.showgreen = greenfield;
	}

	/**
	 * set the field showing as green dotted for objects that do not yet have a
	 * green date (typically, the target date for objects). Should be a date
	 * 
	 * @param greendottedfield field to use as dotted green field
	 */
	public void setGreenDotted(DataObjectFieldMarker<E> greendottedfield) {
		this.showgreendotted = greendottedfield;
	}

	/**
	 * get a reference to an attribute of the object being selected on the diagram
	 * (by clicking on one of the objects displaying on a dot)
	 * 
	 * @param marker attribute marker (typically, the object id)
	 * @return a reference to the selected data to be used on the default action
	 */
	public <F extends SimpleDataEltType> Function<SActionInputDataRef<F>, SActionDataLoc<F>> getAttributeInput(
			AttributeMarker<E, F> marker) {
		return (a) -> (marker.getDataLoc(this, a));

	}

	/**
	 * add a default action on a diagram that will be trigger when user select an
	 * object
	 * 
	 * @param defaultaction default action
	 */
	public void addDefaultAction(SActionRef defaultaction) {
		this.defaultaction = defaultaction;
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
		boolean inputdataflag = false;
		if (inputdata != null)
			inputdataflag = true;
		writer.addBooleanField("IND", inputdataflag);
		if (inputdataflag)
			inputdata.writeReferenceToCML(writer);
		if (this.inlineaction != null) {
			writer.addBooleanField("INL", true);
			inlineaction.writeReferenceToOutputCLM(writer, inlineoutputdata);
		} else {
			writer.addBooleanField("INL", false);
		}
		writer.startStructure("ATTRS");

		objectmodel.writeFieldDefinition(writer, input, buffer);
		writer.endStructure("ATTRS");

		if (this.showred != null) {
			writer.addBooleanField("ISR", true);
			writer.addStringField("SHR", showred.toString());
		} else {
			writer.addBooleanField("ISR", false);
		}

		if (this.showgreen != null) {
			writer.addBooleanField("ISG", true);
			writer.addStringField("SHG", showgreen.toString());
		} else {
			writer.addBooleanField("ISG", false);
		}
		if (this.showgreendotted != null) {
			writer.addBooleanField("ISGD", true);
			writer.addStringField("SHGD", showgreendotted.toString());
		} else {
			writer.addBooleanField("ISGD", false);
		}
		writer.startStructure("FTSWS");
		for (int i = 0; i < fieldstoshow.size(); i++) {
			writer.startStructure("FTSW");
			writer.addStringField("FLD", fieldstoshow.get(i).toString());
			writer.endStructure("FTSW");
		}
		writer.endStructure("FTSWS");
		if (defaultaction != null) {
			writer.addBooleanField("IDF", true);
			writer.startStructure("DFT");
			defaultaction.writeToCML(writer);
			writer.endStructure("DFT");
		} else {
			writer.addBooleanField("IDF", false);
		}
	}

	@Override
	public String getWidgetCode() {
		return "SCURVE";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

}
