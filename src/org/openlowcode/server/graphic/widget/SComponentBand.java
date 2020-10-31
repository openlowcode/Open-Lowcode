/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
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

import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.graphic.SPageNode;
import org.openlowcode.server.graphic.SPageSignifPath;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.tools.messages.MessageStringField;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.structure.ChoiceDataElt;

/**
 * A band of components (page nodes) that are aligned.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SComponentBand
		extends
		SPageNode {
	private int direction;
	private ArrayList<SPageNode> elements;
	/**
	 * an horizontal band (typically used for button bands)
	 */
	public static final int DIRECTION_RIGHT = 1;
	/**
	 * a vertical band top to down (most common)
	 */
	public static final int DIRECTION_DOWN = 2;
	/**
	 * a vertical band bottom to top (rare)
	 */
	public static final int DIRECTION_UP = 3;
	/**
	 * an horizontal band right to left (rare)
	 */
	public static final int DIRECTION_LEFT = 4;
	/**
	 * a component band with a direction right and no upper line in display
	 */
	public static final int DIRECTION_RIGHT_NOLINE = 5;

	private String nameforpath;
	private String callstackatcreation;

	private class ConditionalBlock {
		private int insertbeforeindex;
		private ChoiceDataElt<?> conditionchoice;
		private ChoiceValue<?>[] validchoices;
		private SPageNode[] conditionalelements;

		public ConditionalBlock(
				int insertbeforeindex,
				ChoiceDataElt<?> conditionchoice,
				ChoiceValue<?>[] validchoices,
				SPageNode[] conditionalelements) {
			super();
			this.insertbeforeindex = insertbeforeindex;
			this.conditionchoice = conditionchoice;
			this.validchoices = validchoices;
			this.conditionalelements = conditionalelements;
		}
	}

	private ArrayList<ConditionalBlock> conditionalblocks;

	/**
	 * this creates a ComponentBand used as a significant element in the page path
	 * 
	 * @param direction   direction as defined in static int in this class
	 * @param parent      parent page
	 * @param nameforpath name for path if required (allows to have a unique
	 *                    namespace for parents.
	 */
	public SComponentBand(int direction, SPage parent, String nameforpath) {
		super(parent);
		this.direction = direction;
		this.elements = new ArrayList<SPageNode>();
		this.nameforpath = nameforpath;
		this.callstackatcreation = Thread.currentThread().getStackTrace()[2].toString();
		conditionalblocks = new ArrayList<ConditionalBlock>();
	}

	/**
	 * Creates a ComponentBand without a name with a direction = DOWN
	 * 
	 * @param parent parent page
	 */
	public SComponentBand(SPage parent) {
		this(DIRECTION_DOWN, parent);
	}

	/**
	 * this creates a ComponentBand without a name in the namespace of the page
	 * 
	 * @param direction direction as defined in static int in this class
	 * @param parent    parent page
	 */
	public SComponentBand(int direction, SPage parent) {
		super(parent);
		this.direction = direction;
		this.elements = new ArrayList<SPageNode>();
		this.nameforpath = null;
		this.callstackatcreation = Thread.currentThread().getStackTrace()[2].toString();
		conditionalblocks = new ArrayList<ConditionalBlock>();
	}

	/**
	 * @param element
	 */
	public void addElement(SPageNode element) {
		this.elements.add(element);

	}

	@Override
	public void WritePayloadToCDL(MessageWriter writer, SPageData input, SecurityBuffer buffer) throws IOException {
		writer.sendMessageElement(new MessageStringField("DIR", "" + direction));
		writer.startStructure("ELTS");
		for (int i = 0; i < this.elements.size(); i++) {
			SPageNode currentelement = this.elements.get(i);
			if (!currentelement.hideComponent(input, buffer)) {
				writer.startStructure("ELT");
				currentelement.WriteToCDL(writer, input, buffer);
				writer.endStructure("ELT");
			}
		}

		writer.endStructure("ELTS");

		writer.startStructure("CDNBLKS");
		for (int i = 0; i < this.conditionalblocks.size(); i++) {
			writer.startStructure("CDNBLK");
			ConditionalBlock thisblock = this.conditionalblocks.get(i);
			writer.addIntegerField("IND", thisblock.insertbeforeindex);
			thisblock.conditionchoice.writeReferenceToCML(writer);
			// values block
			writer.startStructure("VLDS");
			for (int j = 0; j < thisblock.validchoices.length; j++) {
				writer.startStructure("VLD");
				writer.addStringField("PLD", thisblock.validchoices[j].getStorageCode());
				writer.endStructure("VLD");
			}
			writer.endStructure("VLDS");
			// ------ Node block
			writer.startStructure("ELTS");
			for (int j = 0; j < thisblock.conditionalelements.length; j++) {
				writer.startStructure("ELT");
				thisblock.conditionalelements[j].WriteToCDL(writer, input, buffer);
				writer.endStructure("ELT");
			}
			writer.endStructure("ELTS");

			writer.endStructure("CDNBLK");
		}
		writer.endStructure("CDNBLKS");

	}

	@Override
	public String getWidgetCode() {
		return "COMPONENTBAND";
	}

	@Override
	public boolean hideComponent(SPageData input, SecurityBuffer buffer) {
		return false;
	}

	@Override
	public void populateDown(SPageSignifPath parentpath, SPageNode[] widgetpathtoroot) {
		SPageSignifPath pathforelements = parentpath;
		if (this.nameforpath != null) {
			pathforelements = new SPageSignifPath(nameforpath, this.getPage(), parentpath, widgetpathtoroot);
			this.setSignifPath(pathforelements);
		}
		SPageNode[] newwidgetpathtoroot = this.addCurrentWidgetToRoot(widgetpathtoroot);

		for (int i = 0; i < this.elements.size(); i++) {

			this.elements.get(i).populateDown(pathforelements, newwidgetpathtoroot);
		}
		for (int i = 0; i < this.conditionalblocks.size(); i++) {
			ConditionalBlock block = this.conditionalblocks.get(i);
			for (int j = 0; j < block.conditionalelements.length; j++)
				block.conditionalelements[j].populateDown(pathforelements, newwidgetpathtoroot);
		}
	}

	@Override
	public String toString() {
		return "SCOMPONENTBAND[" + callstackatcreation + "]";
	}

	/**
	 * adds a conditional block in the page band
	 * 
	 * @param conditionchoice     the data put as input to the page to be used at
	 *                            runtime
	 * @param validchoices        valid choices for the said choice data element
	 * @param conditionalelements nodes to add in the page band
	 * @since 1.14
	 */
	public <E extends FieldChoiceDefinition<E>> void addConditionalElements(
			ChoiceDataElt<?> conditionchoice,
			ChoiceValue<E>[] validchoices,
			SPageNode[] conditionalelements) {
		int currentrank = this.elements.size();
		ConditionalBlock thisblock = new ConditionalBlock(currentrank, conditionchoice, validchoices,
				conditionalelements);
		this.conditionalblocks.add(thisblock);
	}

}