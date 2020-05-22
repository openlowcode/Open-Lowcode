/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.helpers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.NodeTree;
import org.openlowcode.tools.misc.StandardUtil;

/**
 * This class manages report trees,especially:
 * <ul>
 * <li>ensures that intermediate versions of the tree sum-up the leaves</li>
 * <li>create missing leaves if needed</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @param <E> class of the report object
 */
public class ReportTree<E extends DataObject<E>> {

	private class Node {

		private HashMap<String, Node> children;
		private E element;
		private boolean significant = true;

		public Node(E element) {
			this.element = element;
			children = new HashMap<String, Node>();
		}

		public E getElement() {
			return element;
		}

		public void addChild(String pathelement, Node child) {
			children.put(pathelement, child);
		}

		public Set<String> getKeySet() {
			return children.keySet();
		}

		public Node getChild(String pathelement) {
			return children.get(pathelement);
		}

		public void setSignificant(boolean significant) {
			this.significant = significant;
		}
	}

	private static Logger logger = Logger.getLogger(ReportTree.class.getName());

	/**
	 * function to set the name on the report object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> data object
	 */
	@FunctionalInterface
	public interface Namesetter<E extends DataObject<E>> {
		/**
		 * sets the name on the report object
		 * 
		 * @param object data object
		 * @param name   name
		 */
		public void name(E object, String name);
	}

	/**
	 * function to consolidate data from the child to the parent of the report node
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> report data object
	 */
	@FunctionalInterface
	public interface Consolidator<E extends DataObject<E>> {
		/**
		 * rolls-up data from child to parent
		 * 
		 * @param parent parent node
		 * @param child  child node
		 */
		public void consolidate(E parent, E child);
	}

	/**
	 * A complex consolidator will be called after all data has been entered, and
	 * allows to perform consolidation with all data known. Compared to
	 * consolidator, this should be used for algorithms that need all data present
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> report data object
	 */
	@FunctionalInterface
	public interface ComplexConsolidator<E extends DataObject<E>> {
		/**
		 * consolidates the parent on all children
		 * 
		 * @param parent      parent node
		 * @param allchildren all children of the parent node
		 */
		public void consolidateWithFullData(E parent, List<E> allchildren);
	}

	/**
	 * feature to initiate a new report object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> report data object
	 */
	@FunctionalInterface
	public interface Initiator<E extends DataObject<E>> {
		/**
		 * initiates a new object
		 * 
		 * @param newobject new blank object
		 */
		public void init(E newobject);
	}

	/**
	 * Extracts a value from the report object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> parent data object
	 * @param <F> class of the value to extract
	 */
	@FunctionalInterface
	public interface ValueExtractor<E extends DataObject<E>, F extends Object> {
		/**
		 * extracts the data from the given data object
		 * 
		 * @param dataobject data object
		 * @return extracted value
		 */
		public F extract(E dataobject);
	}

	/**
	 * a function to set a value to the report object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> parent data object
	 * @param <F> class of the value to extract
	 */
	@FunctionalInterface
	public interface ValueSetter<E extends DataObject<E>, F extends Object> {
		/**
		 * sets a value on the data object
		 * 
		 * @param dataobject data object
		 * @param value      value to set
		 */
		public void set(E dataobject, F value);
	}

	/**
	 * a robust sum utility (manages null the way it is expected in a spreadsheet)
	 * 
	 * @param first  first element
	 * @param second second element
	 * @return
	 *         <ul>
	 *         <li>the non null element if only one element is not null</li>
	 *         <li>null if both elements are null</li>
	 *         <li>the sum if both elements are not null</li>
	 *         </ul>
	 */
	public static BigDecimal sumIfNotNull(BigDecimal first, BigDecimal second) {
		if (first == null)
			return second;
		if (second == null)
			return first;
		return first.add(second);
	}

	/**
	 * a robust product utility (manages null the way it is expected in a
	 * spreadsheet)
	 * 
	 * @param first  first element
	 * @param second second element
	 * @return
	 *         <ul>
	 *         <li>null if one element is null</li>
	 *         <li>the product if both elements are not null</li>
	 *         </ul>
	 */
	public static BigDecimal multiplyIfNotNull(BigDecimal first, BigDecimal second) {
		if (first == null)
			return null;
		if (second == null)
			return null;
		logger.finer("  multiplier " + first + " x " + second);
		return first.multiply(second);
	}

	/**
	 * a robust division utility (manages null the way it is expected in a
	 * spreadsheet)
	 * 
	 * @param first  first element
	 * @param second second element
	 * @return
	 *         <ul>
	 *         <li>null if one element is null</li>
	 *         <li>the division with rounding half_down if both elements are not
	 *         null</li>
	 *         </ul>
	 */
	public static BigDecimal divideIfNotNull(BigDecimal first, BigDecimal second) {
		if (first == null)
			return null;
		if (second == null)
			return null;
		return first.divide(second, first.scale(), RoundingMode.HALF_DOWN);

	}

	/**
	 * prints a big decimal
	 * 
	 * @param decimal       big decimal
	 * @param displayifnull what to show if Big Decimal is null
	 * @return a string with the representation of the big decimal
	 */
	public static String printBigDecimal(BigDecimal decimal, String displayifnull) {
		if (decimal == null)
			return displayifnull;
		DecimalFormat decimalformatter = StandardUtil.getOLcDecimalFormatter();
		return decimalformatter.format(decimal);
	}

	/**
	 * default print of a big decimal
	 * 
	 * @param decimal decimal
	 * @return print of a big decimal
	 */
	public static String printBigDecimal(BigDecimal decimal) {
		return printBigDecimal(decimal, "-");
	}

	/**
	 * prints a big decimal with the given scale (rounding half-down)
	 * 
	 * @param decimal       big decimal
	 * @param scale         scale
	 * @param displayifnull display for null
	 * @return string display of the big decimal
	 */
	public static String printBigDecimal(BigDecimal decimal, int scale, String displayifnull) {
		if (decimal == null)
			return displayifnull;
		DecimalFormat decimalformatter = StandardUtil.getOLcDecimalFormatter();
		BigDecimal decimalrounded = decimal.setScale(scale, RoundingMode.HALF_DOWN);
		return decimalformatter.format(decimalrounded);
	}

	/**
	 * prints the big decimal wih the given scale
	 * 
	 * @param decimal decimal
	 * @param scale   scale
	 * @return string representation
	 */
	public static String printBigDecimal(BigDecimal decimal, int scale) {
		return printBigDecimal(decimal, scale, "-");
	}

	/**
	 * Sums a valuein parent
	 * 
	 * @param parent    parent element of the report tree
	 * @param child     child element of the report tree
	 * @param extractor extractor of the value on the object
	 * @param setter    setter of the value on the object
	 */
	public static <E extends DataObject<E>> void sumInparent(
			E parent,
			E child,
			ValueExtractor<E, BigDecimal> extractor,
			ValueSetter<E, BigDecimal> setter) {
		BigDecimal parentvalue = extractor.extract(parent);
		BigDecimal childvalue = extractor.extract(child);
		setter.set(parent, sumIfNotNull(parentvalue, childvalue));
		logger.finer("summing " + childvalue + ", with  " + parentvalue + " (parent = " + parent.dropIdToString()
				+ " child = " + child.dropIdToString() + " )");
	}

	/**
	 * sums a hard coded value into the parent
	 * 
	 * @param parent     parent element
	 * @param childvalue child value to sum
	 * @param extractor  extractor of the value on the object
	 * @param setter     setter of the value on the object
	 */
	public static <E extends DataObject<E>> void sumInparent(
			E parent,
			BigDecimal childvalue,
			ValueExtractor<E, BigDecimal> extractor,
			ValueSetter<E, BigDecimal> setter) {
		BigDecimal parentvalue = extractor.extract(parent);
		setter.set(parent, sumIfNotNull(parentvalue, childvalue));
		logger.finer(
				"summing " + childvalue + ", with  " + parentvalue + " (parent = " + parent.dropIdToString() + " )");

	}

	/**
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> data object in the report tree
	 */
	public interface Enricher<E extends DataObject<E>> {
		/**
		 * @param originobject
		 */
		public void enrich(E originobject);
	}

	/**
	 * This interface checks if the object has any significant information to reduce
	 * the report tree to only significant data
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 * @param <E> data object in the report tree
	 */

	@FunctionalInterface
	public interface SignificantChecker<E extends DataObject<E>> {
		/**
		 * @param object
		 * @return true if the object is significant
		 */
		public boolean isSignificant(E object);
	}

	private DataObjectDefinition<E> objectdefinition;
	private Node rootnode;

	private Namesetter<E> namesetter;
	private Consolidator<E>[] consolidators;
	private ComplexConsolidator<E> complexconsolidator;
	private SignificantChecker<E> significantchecker;
	private ValueExtractor<E, String> nameextractor;
	private Initiator<E> initiator;

	/**
	 * creates a new report tree
	 * 
	 * @param objectdefinition definition of the object in the report tree
	 * @param namesetter       utility to set name on object
	 * @param nameextractor    utility to get name on object
	 * @param consolidator     list of data consolidators
	 * @param initiator        initiator to set the root node
	 * @param rootname         root node name
	 */
	public ReportTree(
			DataObjectDefinition<E> objectdefinition,
			Namesetter<E> namesetter,
			ValueExtractor<E, String> nameextractor,
			Consolidator<E>[] consolidators,
			Initiator<E> initiator,
			String rootname) {
		this(objectdefinition, namesetter, nameextractor, consolidators, rootname);
		this.initiator = initiator;
		if (initiator != null)
			initiator.init(rootnode.element);
	}

	/**
	 * creates a new report tree
	 * 
	 * @param objectdefinition definition of the object in the report tree
	 * @param namesetter       utility to set name on object
	 * @param consolidator     data consolidators
	 * @param rootname         root node name
	 */
	public ReportTree(
			DataObjectDefinition<E> objectdefinition,
			Namesetter<E> namesetter,
			ValueExtractor<E, String> nameextractor,
			Consolidator<E>[] consolidators,
			String rootname) {
		this.objectdefinition = objectdefinition;
		this.namesetter = namesetter;
		this.consolidators = consolidators;
		this.nameextractor = nameextractor;
		E rootelement = objectdefinition.generateBlank();

		namesetter.name(rootelement, rootname);
		rootnode = new Node(rootelement);
		this.initiator = null;
	}

	/**
	 * creates a new report tree
	 * 
	 * @param objectdefinition   definition of the object in the report tree
	 * @param namesetter         utility to set name on object
	 * @param nameextractor      utility to get name on object
	 * @param uniqueconsolidator unique consolidator
	 * @param initiator          initiator to set the root node
	 * @param rootname           root node name
	 */
	public ReportTree(
			DataObjectDefinition<E> objectdefinition,
			Namesetter<E> namesetter,
			ValueExtractor<E, String> nameextractor,
			Consolidator<E> uniqueconsolidator,
			Initiator<E> initiator,
			String rootname) {
		this(objectdefinition, namesetter, nameextractor, uniqueconsolidator, rootname);
		this.initiator = initiator;
		if (initiator != null)
			initiator.init(rootnode.element);
	}

	/**
	 * creates a new report tree
	 * 
	 * @param objectdefinition definition of the object in the report tree
	 * @param namesetter       utility to set name on object
	 * @param consolidator     data consolidators
	 * @param rootname         root node name
	 */
	@SuppressWarnings("unchecked")
	public ReportTree(
			DataObjectDefinition<E> objectdefinition,
			Namesetter<E> namesetter,
			ValueExtractor<E, String> nameextractor,
			Consolidator<E> uniqueconsolidator,
			String rootname) {
		this.objectdefinition = objectdefinition;
		this.namesetter = namesetter;
		this.consolidators = new Consolidator[] { uniqueconsolidator };
		this.nameextractor = nameextractor;
		E rootelement = objectdefinition.generateBlank();

		namesetter.name(rootelement, rootname);
		rootnode = new Node(rootelement);
		this.initiator = null;
	}

	/**
	 * adds a complex consolidator
	 * 
	 * @param complexconsolidator complex consolidator to be applied after all data
	 *                            entered
	 */
	public void addComplexConsolidator(ComplexConsolidator<E> complexconsolidator) {
		this.complexconsolidator = complexconsolidator;
	}

	public void addsignificantchecker(SignificantChecker<E> significantchecker) {
		this.significantchecker = significantchecker;
	}

	/**
	 * This method will add the given object as a node if it has a label. If it does
	 * not have a label, the value will be rolled-up into the parent of the last
	 * element of parent labels. This simplifies the algorithm for the smart reports
	 * 
	 * @param parentlabels the parents, excluding root node
	 * @param node         the node to add as child to the last parent in the parent
	 *                     labels array
	 */
	public void addNode(String[] parentlabels, E node) {
		String ownlabel = this.nameextractor.extract(node);
		String[] parentlabelstouse = parentlabels;
		boolean haslabel = true;
		if (ownlabel == null)
			haslabel = false;
		if (ownlabel != null)
			if (ownlabel.length() == 0)
				haslabel = false;
		if (!haslabel) {
			if (parentlabels.length == 0)
				throw new RuntimeException("Cannot add a node without label and no parent label");
			String ownparentlabel = parentlabels[parentlabels.length - 1];
			if (ownparentlabel == null)
				throw new RuntimeException("Cannot add a node without label if parent label is null");
			ownlabel = ownparentlabel;
			parentlabelstouse = new String[parentlabels.length - 1];
			for (int i = 0; i < parentlabelstouse.length; i++)
				parentlabelstouse[i] = parentlabels[i];
			logger.fine("                * managing null label of element as element on parent " + ownlabel);
			this.namesetter.name(node, ownlabel);
		} else if (ownlabel.length() == 0)
			logger.fine("         * label with zero length");
		StringBuffer droperror = new StringBuffer("Path [");
		for (int i = 0; i < parentlabelstouse.length; i++) {
			if (i > 0)
				droperror.append('|');
			droperror.append(parentlabelstouse[i]);

		}
		droperror.append("]");
		for (int i = 0; i < parentlabelstouse.length; i++)
			if (parentlabelstouse[i] == null) {
				logger.fine(" ---- Error null label for object " + node.dropToString());
				throw new RuntimeException("Null element " + i + " in parent labels " + droperror.toString());
			}
		Node currentnode = rootnode;
		ArrayList<Node> pathnode = new ArrayList<Node>();
		pathnode.add(rootnode);

		for (int i = 0; i < parentlabelstouse.length; i++) {
			String label = parentlabelstouse[i];
			Node childnode = currentnode.getChild(label);
			if (childnode == null) {
				logger.fine("   * addition child " + label + " for parent label " + i);
				E child = objectdefinition.generateBlank();
				if (initiator != null)
					initiator.init(child);
				namesetter.name(child, label);
				childnode = new Node(child);
				currentnode.addChild(label, childnode);
			}
			currentnode = childnode;
			pathnode.add(currentnode);
		}

		Node oldleaf = currentnode.getChild(nameextractor.extract(node));
		E oldnode = null;
		if (oldleaf != null)
			oldnode = oldleaf.element;
		if (oldnode == null) {
			logger.fine("* Adding node "
					+ (node == null ? "NULL NODE (not normal)" : "Node " + nameextractor.extract(node) + " - " + node)
					+ "at path " + droperror.toString());

			Node leaf = new Node(node);
			currentnode.addChild(nameextractor.extract(node), leaf);
			pathnode.add(leaf);
		} else {
			logger.finer("* Enriching node "
					+ (node == null ? "NULL NODE (not normal)" : "Node " + nameextractor.extract(node) + " - " + node)
					+ "at path " + droperror.toString());

			// do not create a new node, but sum in the old node
			for (int i = 0; i < this.consolidators.length; i++)
				this.consolidators[i].consolidate(oldnode, node);
		}

	}

	/**
	 * @param objectdefinition
	 * @return
	 */
	public NodeTree<E> generateNodeTree(DataObjectDefinition<E> objectdefinition) {
		rollupamount(rootnode, 0);

		NodeTree<E> nodetree = new NodeTree<E>(rootnode.getElement());
		
		if (this.significantchecker!=null) checksignificant(rootnode,0);
		addChildren(nodetree, rootnode, 0);
		return nodetree;
	}

	/**
	 * @param node           node to check for significance
	 * @param circuitbreaker recursive circuit breaker
	 */
	private boolean checksignificant(ReportTree<E>.Node node, int circuitbreaker) {
		if (circuitbreaker > 50)
			throw new RuntimeException(
					"Recursive path error for element " + nameextractor.extract(node.getElement()));
		boolean childrensignificant = false;
		Iterator<String> keyiterator = node.getKeySet().iterator();
		while (keyiterator.hasNext()) {
			boolean childsignificant = this.checksignificant(node.getChild(keyiterator.next()), circuitbreaker+1);
			if (childsignificant) childrensignificant=true;
		}
		boolean owndatasignificant = this.significantchecker.isSignificant(node.getElement());
		boolean significanttotal = childrensignificant | owndatasignificant;
		node.setSignificant(significanttotal);
		logger.fine("Set significant for node "+nameextractor.extract(node.getElement())+" total="+significanttotal+", own="+owndatasignificant+", children = "+childrensignificant);
		return significanttotal;
	}

	/**
	 * Just before the node generation, the roll-up is performed. This avoids having
	 * issue with revursivity, a circuit breaker is used
	 * 
	 * @param parentnode node to process
	 * @param circuitbreaker recursive level
	 */
	private void rollupamount(Node parentnode, int circuitbreaker) {
		if (circuitbreaker > 50)
			throw new RuntimeException(
					"Recursive path error for element " + nameextractor.extract(parentnode.getElement()));
		ArrayList<String> orderedchildrenkeys = new ArrayList<String>();
		orderedchildrenkeys.addAll(parentnode.getKeySet());
		for (int i = 0; i < orderedchildrenkeys.size(); i++) {
			Node childnode = parentnode.getChild(orderedchildrenkeys.get(i));
			rollupamount(childnode, circuitbreaker + 1);
			for (int j = 0; j < this.consolidators.length; j++)
				consolidators[j].consolidate(parentnode.element, childnode.element);

		}
		if (complexconsolidator != null) {
			ArrayList<E> allchildren = new ArrayList<E>();
			for (int i = 0; i < orderedchildrenkeys.size(); i++)
				allchildren.add(parentnode.getChild(orderedchildrenkeys.get(i)).getElement());
			complexconsolidator.consolidateWithFullData(parentnode.element, allchildren);
		}

	}

	private void addChildren(NodeTree<E> nodetree, Node parentnode, int circuitbreaker) {
		if (circuitbreaker > 50)
			throw new RuntimeException(
					"Recursive path error for element " + nameextractor.extract(parentnode.getElement()));
		ArrayList<String> orderedchildrenkeys = new ArrayList<String>();
		orderedchildrenkeys.addAll(parentnode.getKeySet());
		Collections.sort(orderedchildrenkeys);
		for (int i = 0; i < orderedchildrenkeys.size(); i++) {
			Node childnode = parentnode.getChild(orderedchildrenkeys.get(i));
			if (childnode.significant) {
				nodetree.addChild(parentnode.getElement(), childnode.getElement());
				addChildren(nodetree, childnode, circuitbreaker + 1);
			} else {
				logger.fine("Node dropped "+childnode.getElement());
			}
		}
	}

}
