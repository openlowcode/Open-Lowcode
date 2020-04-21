/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

/**
 * A field stored a piece of information on a data object. The data is typically
 * entered by the user with no complex logic as long as the user has creation or
 * update privileges on the data object.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class Field
		extends
		ObjectElement {
	private int displaypriority;
	private boolean showintitle;
	private boolean showinbottomnotes;
	private boolean nouseredition = false;
	private boolean hasfieldvaluesquery = false;

	/**
	 * In case the field has a values query, a specific method will be created on
	 * the object to query all existing values
	 * 
	 * @since 1.6
	 */
	protected void setHasFieldValuesQuery() {
		this.hasfieldvaluesquery=true;
	}

	/**
	 * @return if the field has a values query (a specific method will be created on 
	 * the object to query all existing values for the field
	 * @since 1.6
	 */
	public boolean hasFieldValuesQuery() {
		return this.hasfieldvaluesquery;
	}
	
	/**
	 * If this is set, the only way to update this field is through actions and
	 * triggers.
	 */
	public void setNoUserEdition() {
		this.nouseredition = true;
	}

	/**
	 * @return true if the field is only updated by automatic processing
	 */
	public boolean isNoUserEdition() {
		return this.nouseredition;
	}

	private DataObjectDefinition parentobject;

	/**
	 * @return the parent data object
	 */
	public DataObjectDefinition getParentObject() {
		return parentobject;
	}

	/**
	 * set the parent data object on this field
	 * 
	 * @param parentobject the parent data object
	 */
	protected void setDataObjectDefinition(DataObjectDefinition parentobject) {
		this.parentobject = parentobject;
	}

	/**
	 * creates a new field
	 * 
	 * @param name        unique identification that should be valid in java and sql
	 * @param displayname label of the field that is shown to the users in the
	 *                    default language
	 * @param tooltip     roll-over tooltip explaining the field
	 */
	public Field(String name, String displayname, String tooltip) {
		super(name, displayname, tooltip);
		this.displaypriority = 0;
		this.showinbottomnotes = false;
		this.showinbottomnotes = false;
	}

	/**
	 * if true, the field is shown in title and in main object field on the object
	 * page
	 */
	public void setShowInTitle() {
		this.showintitle = true;
	}

	/**
	 * if true, the field is shown in bottom notes
	 */
	public void setShowinbottomnotes() {
		this.showinbottomnotes = true;
	}

	/**
	 * @return true if field is shown in title
	 */
	public boolean isShowintitle() {
		return showintitle;
	}

	/**
	 * @return true if field is shown only in bottom notes
	 */
	public boolean isShowinbottomnotes() {
		return showinbottomnotes;
	}

	/**
	 * creates a new field with the given priority
	 * 
	 * @param name            unique identification that should be valid in java and
	 *                        sql
	 * @param displayname     label of the field that is shown to the users in the
	 *                        default language
	 * @param tooltip         roll-over tooltip explaining the field
	 * @param displaypriority display priority for the field
	 */
	public Field(String name, String displayname, String tooltip, int displaypriority) {
		super(name, displayname, tooltip);
		this.setDisplayPriority(displaypriority);
	}

	/**
	 * set the display priority of the field
	 * 
	 * @param displaypriority display priority
	 */
	public void setDisplayPriority(int displaypriority) {
		if (displaypriority > 1000)
			throw new RuntimeException("display priority cannot be more than 1000 for field " + this.getName());
		if (displaypriority < -1000)
			throw new RuntimeException("display priority cannot be more less than -1000 for field " + this.getName());

		this.displaypriority = displaypriority;
	}

	/**
	 * @return get hte display priority of the field
	 */
	public int getDisplayPriority() {
		return this.displaypriority;
	}

	/**
	 * Returns the main stored element of a field to be used in composite index
	 * 
	 * @return a stored element
	 */
	public abstract StoredElement getMainStoredElementForCompositeIndex();

	/**
	 * performs a copy of the field
	 * 
	 * @return a copy of this field with the same name
	 */
	public Field copy() {
		return copy(null, null);
	}

	/**
	 * performs a copy of the field with new name and new display label
	 * 
	 * @param newname         newname for the field (null if keep originalname)
	 * @param newdisplaylabel new display label (null to keep original display name)
	 * @return
	 */
	public abstract Field copy(String newname, String newdisplaylabel);
}
