/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.pages;

import org.openlowcode.design.data.ChoiceCategory;
import org.openlowcode.design.data.Element;
import org.openlowcode.tools.data.TimePeriod;
import org.openlowcode.tools.data.TimePeriod.PeriodType;

/**
 * A search widget definition can be added to the automatically generated search
 * page for an object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SearchWidgetDefinition {
	// shows as primary search criteria
	private boolean primary;
	// overrides name if needed
	private String displayname;
	private String fieldname;
	private int posttreatment;
	private Element element;
	/**
	 * performs a post treatment with no effect
	 */
	public static int POSTTREATMENT_NONE = 0;
	/**
	 * performs a post treatment for easy text search
	 */
	public static int POSTTREATMENT_EASYTEXTSEARCH = 1;

	/**
	 * search widget is text search
	 */
	public static int TYPE_TEXT = 100;
	/**
	 * search widget is date range
	 */
	public static int TYPE_DATE = 101;
	/**
	 * search widget is close choice list
	 */
	public static int TYPE_CHOICE = 102;
	/**
	 * search widget is time period
	 */
	public static int TYPE_TIMEPERIOD = 103;
	/**
	 * search widget is text choice
	 * @since 1.6
	 */
	public static int TYPE_TEXTCHOICE = 104;

	private ChoiceCategory multiplechoicecategory = null;

	/**
	 * a flag indicating if the storage code is in a choice field or in the string
	 * property
	 */
	private boolean choicesearchinstring;
	private PeriodType periodtype;
	private int type;

	/**
	 * @return the type of search widget (defined as a static int)
	 */
	public int getType() {
		return this.type;
	}

	/**
	 * @return true if the search is using a choice
	 */
	public boolean isChoiceSearchInString() {
		return this.choicesearchinstring;
	}

	/**
	 * Create a search widget definition for plain text field
	 * 
	 * @param primary     true if the element should be shown as primary, false if
	 *                    the field should be shown in the collapsible part of the
	 *                    search screen
	 * @param fieldname   name of the field (should be unique for the object)
	 * @param displayname the display name if the name of the element should be
	 *                    overridden, null else
	 */
	public SearchWidgetDefinition(boolean primary, String fieldname, String displayname) {
		super();
		this.primary = primary;
		this.fieldname = fieldname;
		this.displayname = displayname;
		this.posttreatment = POSTTREATMENT_NONE;
		this.type = TYPE_TEXT;
	}

	/**
	 * create a search widget definition with potential post-treatment
	 * 
	 * @param primary       true if the element should be shown as primary, false if
	 *                      the field should be shown in the collapsible part of the
	 *                      search screen
	 * @param fieldname     name of the field (should be unique for the object)
	 * @param displayname   the display name if the name of the element should be
	 *                      overridden, null else
	 * @param posttreatment post treatment as defined in a static int in this class
	 */
	public SearchWidgetDefinition(boolean primary, String fieldname, String displayname, int posttreatment) {
		super();
		this.primary = primary;
		this.fieldname = fieldname;
		this.displayname = displayname;
		this.posttreatment = posttreatment;
		this.type = TYPE_TEXT;
	}

	/**
	 * creates a search widget definition using a choice category. The widget will
	 * allow several selections
	 * 
	 * @param primary                true if the element should be shown as primary,
	 *                               false if the field should be shown in the
	 *                               collapsible part of the search screen
	 * @param fieldname              name of the field (should be unique for the
	 *                               object)
	 * @param displayname            the display name if the name of the element
	 *                               should be overridden, null else
	 * @param multiplechoicecategory choice category. In the case where choice
	 *                               should be determined by existing value in text
	 *                               field, just leave blank (new to version 1.6)
	 *                       
	 */
	public SearchWidgetDefinition(
			boolean primary,
			String fieldname,
			String displayname,
			ChoiceCategory multiplechoicecategory) {
		this(primary, fieldname, displayname, multiplechoicecategory, true);
	}

	/**
	 * creates a search widget definition for a time period
	 * 
	 * @param primary     true if the element should be shown as primary, false if
	 *                    the field should be shown in the collapsible part of the
	 *                    search screen
	 * @param fieldname   name of the field (should be unique for the object)
	 * @param displayname the display name if the name of the element should be
	 *                    overridden, null else
	 * @param periodtype  type of time period
	 */
	public SearchWidgetDefinition(
			boolean primary,
			String fieldname,
			String displayname,
			TimePeriod.PeriodType periodtype) {
		super();
		this.primary = primary;
		this.fieldname = fieldname;
		this.displayname = displayname;
		this.periodtype = periodtype;
		this.posttreatment = POSTTREATMENT_NONE;
	}

	/**
	 * Creates a search widget definition for a specified type (in practice text or
	 * date)
	 * 
	 * @param primary       true if the element should be shown as primary, false if
	 *                      the field should be shown in the collapsible part of the
	 *                      search screen
	 * @param fieldname     name of the field (should be unique for the object)
	 * @param displayname   the display name if the name of the element should be
	 *                      overridden, null else
	 * @param type          type of field as defined in a static int in this class
	 * @param posttreatment post treatment as defined in a static int in this class
	 * @since 1.6
	 */
	public SearchWidgetDefinition(boolean primary, String fieldname, String displayname, int type, int posttreatment) {
		super();
		this.primary = primary;
		this.fieldname = fieldname;
		this.displayname = displayname;
		this.type = type;
		this.posttreatment = posttreatment;

	}

	/**
	 * @return get the period type if the search widget is a time period
	 */
	public PeriodType getPeriodType() {
		return this.periodtype;
	}

	/**
	 * @param primary                true if the element should be shown as primary,
	 *                               false if the field should be shown in the
	 *                               collapsible part of the search screen
	 * @param fieldname              name of the field (should be unique for the
	 *                               object)
	 * @param displayname            the display name if the name of the element
	 *                               should be overridden, null else
	 * @param multiplechoicecategory choice category. In the case where choice
	 *                               should be determined by existing value in text
	 *                               field, just leave blank (new to version 1.6)
	 * @param searchasstring         if the search is using a choice
	 */
	public SearchWidgetDefinition(
			boolean primary,
			String fieldname,
			String displayname,
			ChoiceCategory multiplechoicecategory,
			boolean searchasstring) {
		super();
		this.primary = primary;
		this.fieldname = fieldname;
		this.displayname = displayname;
		this.posttreatment = POSTTREATMENT_NONE;
		this.multiplechoicecategory = multiplechoicecategory;
		this.choicesearchinstring = searchasstring;
		if (this.multiplechoicecategory!=null) this.type = TYPE_CHOICE;
		if (this.multiplechoicecategory==null) this.type = TYPE_TEXTCHOICE;
		
	}

	/**
	 * @return the choice category used for this search widget
	 */
	public ChoiceCategory getMultipleChoiceCategory() {
		return this.multiplechoicecategory;
	}

	/**
	 * creates a search widget definition
	 * 
	 * @param primary true if the element should be shown as primary, false if the
	 *                field should be shown in the collapsible part of the search
	 *                screen
	 */
	public SearchWidgetDefinition(boolean primary) {
		super();
		this.primary = primary;
		this.displayname = null;
		this.posttreatment = POSTTREATMENT_NONE;
		this.type = TYPE_TEXT;
	}

	/**
	 * @param element sets the element that is used for the search widget (data
	 *                element - mostly stored element)
	 */
	public void setReferenceElement(Element element) {
		this.element = element;
	}

	/**
	 * @return true if the search field is primary
	 */
	public boolean isPrimary() {
		return primary;
	}

	/**
	 * @return the field name (unique)
	 */
	public String getFieldname() {
		return this.fieldname;
	}

	/**
	 * @return the field description / label as shown in the screen
	 */
	public String getDisplayname() {
		return displayname;
	}

	/**
	 * @return the type of post-treatment ( static int in this class)
	 */
	public int getPosttreatment() {
		return posttreatment;
	}

	/**
	 * @return the element the search widget is refering to
	 */
	public Element getElement() {
		return element;
	}

}
