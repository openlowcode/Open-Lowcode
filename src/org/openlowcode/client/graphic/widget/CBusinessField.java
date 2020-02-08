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

import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine;
import org.openlowcode.client.graphic.widget.table.ObjectTableRow;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TreeTableColumn;

/**
 * A field displaying and allowing to edit a simple elementary value (number,
 * date, text). It can be part of an object or standalone
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public abstract class CBusinessField<E extends SimpleDataElt>
		extends
		CPageNode {
	protected boolean isactive;
	protected String property;
	protected ArrayList<CMultiFieldConstraint> constraintsforcallback;
	/**
	 * this is kept for logging purposes in case an exception is thrown
	 */
	private CPageNode parentforfield;

	/**
	 * @return the parent node for the field
	 */
	public CPageNode getParentforfield() {
		return parentforfield;
	}

	/**
	 * @param parentforfield sets the parent node for this field
	 */
	public void setParentforfield(CPageNode parentforfield) {
		this.parentforfield = parentforfield;
	}

	/**
	 * @return true if the field is shown in title of an object when part of an
	 *         object
	 */
	public boolean isShowintitle() {
		return showintitle;
	}

	/**
	 * @return if the field is shown in the bottom notes of an object when part of a
	 *         data object
	 */
	public boolean isShowinbottomnotes() {
		return showinbottomnotes;
	}

	protected boolean showintitle;
	protected boolean showinbottomnotes;

	/**
	 * @return true if the field is editable
	 */
	public abstract boolean isEditable();

	/**
	 * creates a business field
	 * 
	 * @param reader     reader of information from the server
	 * @param parentpath path of the widget in the page
	 * @throws OLcRemoteException if anything bad happens during the transmission on
	 *                            the server
	 * @throws IOException        if any network error happens
	 */
	public CBusinessField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		isactive = true;
		constraintsforcallback = new ArrayList<CMultiFieldConstraint>();
	}

	/**
	 * @return the exact string displayed as label of the field
	 */
	public abstract String getLabel();

	/**
	 * @param newlabel set a new label to this field
	 */
	public abstract void overridesLabel(String newlabel);

	/**
	 * @return the field name
	 */
	public abstract String getFieldname();

	/**
	 * @return the node displaying the payload
	 */
	public abstract Node getDisplayContent();

	/**
	 * @return the string to display as helper during a mouse roll-up, or null if no
	 *         helper is available
	 */

	public abstract String getHelper();

	/**
	 * @param active note: by default, a component is active
	 */
	public void setActive(boolean active) {
		this.isactive = active;
	}

	/**
	 * @param objectdata get the content from an object data element
	 */
	public abstract void setContent(ObjectDataElt objectdata);

	/**
	 * @return the payload
	 */
	public abstract E getFieldDataElt();

	/**
	 * get the column representing this field for display in an object array table
	 * 
	 * @param pageactionmanager  page action manager
	 * @param largedisplay       true if large display
	 * @param preferedrowheight  the prefered row height, in number of rows
	 * @param actionkeyforupdate if this field has a specific action key for update.
	 *                           This is is case several actions can be triggered
	 *                           from data changed in the object (not fully
	 *                           operational)
	 * @return the table column
	 */
	public abstract TableColumn<ObjectTableRow, ?> getTableColumn(
			PageActionManager pageactionmanager,
			boolean largedisplay,
			int preferedrowheight,
			String actionkeyforupdate);

	/**
	 * @param preferedrowheight    prefered height for row (note: not used for big
	 *                             decimal, not implemented for other fields
	 * @param actionkeyforupdate   update action key
	 * @param maincolumnvalue      the main column value to consider for this field
	 *                             (i.e. display only if maincolumn value) of the
	 *                             object is equals to this main column value
	 * @param                      secondarycolumnvalue: the secondary value for
	 *                             column. THis will be the display of the title if
	 *                             used. Put null else
	 * @param maincolumnvaluetitle if true, title is the main column value (good for
	 *                             only one field display). if wrong, title is the
	 *                             current column title (good for several fields
	 *                             displays
	 * @return
	 */
	public abstract TableColumn<CObjectGridLine<?>, ?> getTableColumnForGrid(
			PageActionManager pageactionmanager,
			int preferedrowheight,
			String actionkeyforupdate,
			String maincolumnvalue,
			String secondarycolumnvalue,
			boolean maincolumnvaluetitle);

	/**
	 * adds an action from the upper node to this field
	 * 
	 * @param actionmanager page action manager
	 * @param action        action
	 */
	public abstract void ForceAction(PageActionManager actionmanager, CPageAction action);

	/**
	 * get the column representing this field for display in an object tree array
	 * table
	 * 
	 * @param pageactionmanager  page action manager
	 * @param actionkeyforupdate if this field has a specific action key for update.
	 *                           This is is case several actions can be triggered
	 *                           from data changed in the object (not fully
	 *                           operational)
	 * @return tree table column
	 */
	public abstract TreeTableColumn<ObjectDataElt, ?> getTreeTableColumn(
			PageActionManager pageactionmanager,
			String actionkeyforupdate);

	/**
	 * @return the prefered row height (in lines) when displayed in a table
	 */
	public abstract int getPreferredTableRowHeight();

	/**
	 * checks if a restriction is valid
	 * 
	 * @param restriction the string encoding the restriction
	 * @return true if the restriction isvalid for this field
	 */
	public abstract boolean isRestrictionValid(String restriction);

	/**
	 * @param thisconstraint adds a constraint to this field
	 */
	public void addConstraintCallBack(CMultiFieldConstraint thisconstraint) {
		constraintsforcallback.add(thisconstraint);

	}

	/**
	 * @return the value for constraint
	 */
	public abstract String getValueForConstraint();

	/**
	 * @param restrainedvalues the set of possible selections for this field. The
	 *                         following will be performed:
	 *                         <ul>
	 *                         <li>limit selection to provided values</li>
	 *                         <li>if value currently selected is not valid, empty
	 *                         value (unless a selected value exists</li>
	 *                         <li>if only one selection item is provided, it will
	 *                         be automatically selected, even if no prefered value
	 *                         is given</li>
	 *                         </ul>
	 * @param selected         the prefered value out of the restrained value. Will
	 *                         force the field to the prefered value if entered if
	 *                         provided with null, wil not select anyting, if
	 *                         provided wih empty string "", will force to blank
	 * @return true if the constraint resulted in a blank field
	 */
	public abstract boolean setConstraint(ArrayList<String> restrainedvalues, String selected);

	/**
	 * lift all constraints for this field
	 */
	public abstract void liftConstraint();

	@Override
	public String toString() {
		return this.getClass().getCanonicalName() + " - " + this.getFieldname() + " - " + this.getLabel();
	}

	/**
	 * runs controls on current value, especially related to multifield constraints
	 */
	public abstract void pingValue();

	/**
	 * parses the business field
	 * 
	 * @param reader     reader of information from the server
	 * @param parentpath path of the widget in the page
	 * @return the parsed business field
	 * @throws OLcRemoteException if anything bad happens during the transmission on
	 *                            the server
	 * @throws IOException        if any network error happens
	 */
	public static CBusinessField<?> parseBusinessField(MessageReader reader, CPageSignifPath parentpath)
			throws OLcRemoteException, IOException {
		CPageNode node = CPageNode.parseNode(reader, parentpath);
		if (!(node instanceof CBusinessField))
			throw new RuntimeException(
					"Expected a BusinessField, parsed an object of class " + node.getClass().getName());
		return (CBusinessField<?>) node;
	}
}
