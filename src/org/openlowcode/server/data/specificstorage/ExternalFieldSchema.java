/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.specificstorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.richtext.RichText;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StoredTableSchema;
import org.openlowcode.server.data.storage.Field;
import org.openlowcode.server.data.DisplayProfile;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.storage.ExternalFieldSchemaTemplate;

/**
 * 
 * an external field content is a reference to the field of another object. It
 * is displayed as a read-only field of the main object. Typically, this is used
 * to display a field that is more meaningful than the technical id.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * 
 * @param <E> payload of the external field schema
 */
public class ExternalFieldSchema<E extends Object> extends ExternalFieldSchemaTemplate<E> {
	private Logger logger = Logger.getLogger(ExternalFieldSchema.class.getName());
	private ArrayList<StoredFieldSchema<E>> orderedexternalfield;
	private JoinQueryConditionDefinition<?> joinqueryconditiondefinition;
	@SuppressWarnings("unused")
	private StoredTableSchema parent;
	private String tooltip;
	private String displayname;
	private FieldChoiceDefinition<?> fieldchoice;
	private DisplayProfile<?> hideifprofileset;
	private boolean displayintitle = false;
	private boolean displayinbottomnotes = false;
	private int priority;
	private boolean orderedasinteger = false;
	private int numberoffset;
	private int displaycolumn;
	private HashMap<DisplayProfile<?>, String> displaynameforprofile;
	private boolean richtext = false;

	/**
	 * tells if the field should be ordered as integer (removing prefix)
	 * 
	 * @return true if the field should be ordered as integer
	 */
	public boolean isOrderedAsInteger() {
		return this.orderedasinteger;
	}

	/**
	 * gives the number offset (size of prefix) for ordering
	 * 
	 * @return the number of characters to offset as prefix before extracting
	 *         integer
	 */
	public int getNumberOffsetForOrdering() {
		return this.numberoffset;
	}

	/**
	 * if this method is called, the field is displayed in title
	 */
	public void setDisplayInTitle() {
		this.displayintitle = true;
	}

	/**
	 * if this method is called, the field is displayed in bottom notes
	 */
	public void setDisplayInBottomNotes() {
		this.displayinbottomnotes = true;
	}

	/**
	 * @return true if field is displayed in title
	 */
	public boolean isDisplayInTitle() {
		return this.displayintitle;
	}

	/**
	 * @return true if field is displayed in bottom notes
	 */
	public boolean isDiplayInBottomNotes() {
		return this.displayinbottomnotes;
	}

	/**
	 * @return get the priority of the field
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the size of the columns if displayed in the table
	 */
	public int getDisplaycolumn() {
		return displaycolumn;
	}

	/**
	 * @return true if the field content should be interpreted as rich text
	 */
	public boolean isRichText() {
		return this.richtext;
	}

	/**
	 * gets the display name depending on display profile
	 * 
	 * @param activeprofiles list of active profiles
	 * @return the display name
	 */
	public String getDisplayName(NamedList<DisplayProfile<?>> activeprofiles) {
		if (activeprofiles != null) {
			logger.finest("------------------- Temporary Debug for field " + this.getDisplayname() + " "
					+ this.getName() + " ----------------- ");
			logger.finest(" content of hashmap for field " + displayname);
			Iterator<DisplayProfile<?>> iterator = displaynameforprofile.keySet().iterator();
			while (iterator.hasNext()) {
				DisplayProfile<?> key = iterator.next();
				logger.finest(" - " + key + ", " + displaynameforprofile.get(key));
			}

			logger.finest("drop activeprofile name = " + activeprofiles.dropNameList());
			for (int i = 0; i < activeprofiles.getSize(); i++) {
				String potentialname = displaynameforprofile.get(activeprofiles.get(i));
				logger.finest("analyzing match " + potentialname + " - " + activeprofiles.get(i));
				if (potentialname != null)
					return potentialname;
			}
		} else {
			logger.finest("activeprofiles is null for field " + this.displayname);
		}

		return this.displayname;
	}

	/**
	 * gets the display profile representing the hidden fields
	 * 
	 * @return the display profile
	 */
	public DisplayProfile<?> getHideProfile() {
		return hideifprofileset;
	}

	/**
	 * gets the field choice definition if set on the field
	 * 
	 * @return the field choice definition
	 */
	public FieldChoiceDefinition<?> getFieldChoice() {
		return this.fieldchoice;
	}

	/**
	 * gets the join query condition to get from the main table to the appendix
	 * table
	 * 
	 * @return the join query condition
	 */
	public JoinQueryConditionDefinition<?> getJoinqueryconditiondefinition() {
		return joinqueryconditiondefinition;
	}

	@Override
	public ArrayList<StoredFieldSchema<E>> getExternalTableField() {
		return this.orderedexternalfield;
	}

	/**
	 * @param name                         name of the field
	 * @param displayname                  display name of the field
	 * @param tooltip                      tooltip for mouse roll-over
	 * @param parent                       parent table (the external table from
	 *                                     which the field is extracted)
	 * @param externalfield                name of the field from the external
	 *                                     stored table
	 * @param joinqueryconditiondefinition join query condition to get from the main
	 *                                     object table to an appendix table
	 * @param priority                     a number from -1000 to 1000 (excluded)
	 * @param displaycolumn                size of the column when displayed in
	 *                                     table
	 */
	public ExternalFieldSchema(String name, String displayname, String tooltip, StoredTableSchema parent,
			StoredFieldSchema<E> externalfield, JoinQueryConditionDefinition<?> joinqueryconditiondefinition,
			int priority, int displaycolumn) {
		super(name);
		this.parent = parent;
		this.tooltip = tooltip;
		this.displayname = displayname;
		this.orderedexternalfield = new ArrayList<StoredFieldSchema<E>>();
		this.orderedexternalfield.add(externalfield);
		if (externalfield == null)
			throw new RuntimeException("external field is null for field " + this.displayname);
		this.joinqueryconditiondefinition = joinqueryconditiondefinition;
		this.fieldchoice = null;
		this.hideifprofileset = null;
		this.displaynameforprofile = new HashMap<DisplayProfile<?>, String>();
		this.priority = priority;
		this.displaycolumn = displaycolumn;
	}

	/**
	 * creates an external field schema with specified priority and display column
	 * 
	 * @param name                         name of the field
	 * @param displayname                  display name of the field
	 * @param tooltip                      tooltip for mouse roll-over
	 * @param parent                       parent table (the external table from
	 *                                     which the field is extracted
	 * @param joinqueryconditiondefinition join query condition to get from the main
	 *                                     object table to an appendix table
	 * @param priority                     a number from -1000 to 1000 (excluded)
	 * @param displaycolumn                size of the column when displayed in
	 *                                     table
	 */
	public ExternalFieldSchema(String name, String displayname, String tooltip, StoredTableSchema parent,
			JoinQueryConditionDefinition<?> joinqueryconditiondefinition, int priority, int displaycolumn) {
		super(name);
		this.parent = parent;
		this.tooltip = tooltip;
		this.displayname = displayname;
		this.orderedexternalfield = new ArrayList<StoredFieldSchema<E>>();

		this.joinqueryconditiondefinition = joinqueryconditiondefinition;
		this.fieldchoice = null;
		this.hideifprofileset = null;
		this.displaynameforprofile = new HashMap<DisplayProfile<?>, String>();
		this.priority = priority;
		this.displaycolumn = displaycolumn;
	}

	/**
	 * @param externalfield
	 */
	public void addStoredField(StoredFieldSchema<E> externalfield) {
		if (externalfield == null)
			throw new RuntimeException("external field is null for field " + this.displayname);
		this.orderedexternalfield.add(externalfield);
	}

	/**
	 * Creates an external field with a condition for profile set
	 * 
	 * @param name                         name of the field
	 * @param displayname                  display name of the field
	 * @param tooltip                      tooltip for mouse roll-over
	 * @param parent                       parent table (the external table from
	 *                                     which the field is extracted
	 * @param externalfield
	 * @param joinqueryconditiondefinition join query condition to get from the main
	 *                                     object table to an appendix table
	 * @param hideifprofileset             preset profile for which the field will
	 *                                     be hidden if profile active on the
	 *                                     project
	 * @param priority                     a number from -1000 to 1000 (excluded)
	 * @param displaycolumn                size of the column when displayed in
	 *                                     table
	 */
	public ExternalFieldSchema(String name, String displayname, String tooltip, StoredTableSchema parent,
			StoredFieldSchema<E> externalfield, JoinQueryConditionDefinition<?> joinqueryconditiondefinition,
			DisplayProfile<?> hideifprofileset, int priority, int displaycolumn) {
		this(name, displayname, tooltip, parent, externalfield, false, joinqueryconditiondefinition, hideifprofileset,
				priority, displaycolumn);
	}

	/**
	 * Creates an external field, potentially formatting it as rich text
	 * 
	 * @param name                         name of the field
	 * @param displayname                  display name of the field
	 * @param tooltip                      tooltip for mouse roll-over
	 * @param parent                       parent table (the external table from
	 *                                     which the field is extracted
	 * @param externalfield                definition of the external field
	 * @param richtext                     true if field is rich text
	 * @param joinqueryconditiondefinition join query condition to get from the main
	 *                                     object table to an appendix table
	 * @param hideifprofileset             preset profile for which the field will
	 *                                     be hidden if profile active on the
	 *                                     project
	 * @param priority                     a number from -1000 to 1000 (excluded)
	 * @param displaycolumn                size of the column when displayed in
	 *                                     table
	 */
	public ExternalFieldSchema(String name, String displayname, String tooltip, StoredTableSchema parent,
			StoredFieldSchema<E> externalfield, boolean richtext,
			JoinQueryConditionDefinition<?> joinqueryconditiondefinition, DisplayProfile<?> hideifprofileset,
			int priority, int displaycolumn) {
		super(name);
		this.parent = parent;
		this.tooltip = tooltip;
		this.displayname = displayname;
		this.orderedexternalfield = new ArrayList<StoredFieldSchema<E>>();
		if (externalfield == null)
			throw new RuntimeException("external field is null for field " + this.displayname);
		this.orderedexternalfield.add(externalfield);
		this.joinqueryconditiondefinition = joinqueryconditiondefinition;
		this.fieldchoice = null;
		this.hideifprofileset = hideifprofileset;
		this.displaynameforprofile = new HashMap<DisplayProfile<?>, String>();
		this.priority = priority;
		this.displaycolumn = displaycolumn;
		this.richtext = richtext;
	}

	/**
	 * creates an external field ordered as a number (with prefix discarded)
	 * 
	 * @param name                         name of the field
	 * @param displayname                  display name of the field
	 * @param tooltip                      tooltip for mouse roll-over
	 * @param parent                       parent table (the external table from
	 *                                     which the field is extracted
	 * @param externalfield                definition of the external field
	 * @param joinqueryconditiondefinition join query condition to get from the main
	 *                                     object table to an appendix table
	 * @param hideifprofileset             preset profile for which the field will
	 *                                     be hidden if profile active on the
	 *                                     project
	 * @param priority                     a number from -1000 to 1000 (excluded)
	 * @param displaycolumn                size of the column when displayed in
	 *                                     table
	 * @param orderedasnumber              true if ordered as number
	 * @param numberoffset                 number offset
	 */
	public ExternalFieldSchema(String name, String displayname, String tooltip, StoredTableSchema parent,
			StoredFieldSchema<E> externalfield, JoinQueryConditionDefinition<?> joinqueryconditiondefinition,
			DisplayProfile<?> hideifprofileset, int priority, int displaycolumn, boolean orderedasnumber,
			int numberoffset) {
		super(name);
		this.parent = parent;
		this.tooltip = tooltip;
		this.displayname = displayname;
		this.orderedexternalfield = new ArrayList<StoredFieldSchema<E>>();
		if (externalfield == null)
			throw new RuntimeException("external field is null for field " + this.displayname);
		this.orderedexternalfield.add(externalfield);
		this.joinqueryconditiondefinition = joinqueryconditiondefinition;
		this.fieldchoice = null;
		this.hideifprofileset = hideifprofileset;
		this.displaynameforprofile = new HashMap<DisplayProfile<?>, String>();
		this.priority = priority;
		this.displaycolumn = displaycolumn;
		this.orderedasinteger = orderedasnumber;
		this.numberoffset = numberoffset;
	}

	/**
	 * Creates an external field with the specified field choice for formatting
	 * 
	 * @param name                         name of the field
	 * @param displayname                  display name of the field
	 * @param tooltip                      tooltip for mouse roll-over
	 * @param parent                       parent table (the external table from
	 *                                     which the field is extracted
	 * @param externalfield                definition of the external field
	 * @param fieldchoice                  choice definition
	 * @param joinqueryconditiondefinition join query condition to get from the main
	 *                                     object table to an appendix table
	 * @param priority                     a number from -1000 to 1000 (excluded)
	 * @param displaycolumn                size of the column when displayed in
	 *                                     table
	 */

	public ExternalFieldSchema(String name, String displayname, String tooltip, StoredTableSchema parent,
			StoredFieldSchema<E> externalfield, FieldChoiceDefinition<?> fieldchoice,
			JoinQueryConditionDefinition<?> joinqueryconditiondefinition, int priority, int displaycolumn) {
		super(name);
		this.parent = parent;
		this.tooltip = tooltip;
		this.displayname = displayname;
		this.orderedexternalfield = new ArrayList<StoredFieldSchema<E>>();
		this.orderedexternalfield.add(externalfield);
		this.joinqueryconditiondefinition = joinqueryconditiondefinition;
		this.fieldchoice = fieldchoice;
		this.hideifprofileset = null;
		this.displaynameforprofile = new HashMap<DisplayProfile<?>, String>();
		this.priority = priority;
		this.displaycolumn = displaycolumn;
	}

	/**
	 * Creates an external field with the specified field choice for formatting
	 * 
	 * @param name                         name of the field
	 * @param displayname                  display name of the field
	 * @param tooltip                      tooltip for mouse roll-over
	 * @param parent                       parent table (the external table from
	 *                                     which the field is extracted
	 * @param externalfield                definition of the external field
	 * @param fieldchoice                  choice definition
	 * @param joinqueryconditiondefinition join query condition to get from the main
	 *                                     object table to an appendix table
	 * @param hideifprofileset             preset profile for which the field will
	 *                                     be hidden if profile active on the
	 *                                     project
	 * @param priority                     specific priority from -1000 to 1000
	 *                                     (excluded)
	 * @param displaycolumn                size of the column when displayed in
	 *                                     table
	 */
	public ExternalFieldSchema(String name, String displayname, String tooltip, StoredTableSchema parent,
			StoredFieldSchema<E> externalfield, FieldChoiceDefinition<?> fieldchoice,
			JoinQueryConditionDefinition<?> joinqueryconditiondefinition, DisplayProfile<?> hideifprofileset,
			int priority, int displaycolumn) {
		super(name);
		this.parent = parent;
		this.tooltip = tooltip;
		this.displayname = displayname;
		this.orderedexternalfield = new ArrayList<StoredFieldSchema<E>>();
		this.orderedexternalfield.add(externalfield);
		this.joinqueryconditiondefinition = joinqueryconditiondefinition;
		this.fieldchoice = fieldchoice;
		this.hideifprofileset = hideifprofileset;
		this.displaynameforprofile = new HashMap<DisplayProfile<?>, String>();
		this.priority = priority;
		this.displaycolumn = displaycolumn;
	}

	/**
	 * changes the display is a profile is active. THis is used for example to
	 * remove reference to left or right for objects related to the links
	 * 
	 * @param profile            profile for which the display is special
	 * @param specialdisplayname special display name
	 */
	public void addSpecialdisplaynamewhenprofileactive(DisplayProfile<?> profile, String specialdisplayname) {
		this.displaynameforprofile.put(profile, specialdisplayname);
	}

	@SuppressWarnings("unchecked")
	@Override
	public E castToType(Object o) {
		logger.severe("starting cast to type on object " + o + " fieldsize = " + this.orderedexternalfield.size() + " "
				+ this.getName() + " RT=" + this.richtext + ", FC=" + this.fieldchoice);
		if (fieldchoice == null) {

			if (this.orderedexternalfield.size() == 1) {
				logger.info("processed field = " + this.getName() + " from field "
						+ this.orderedexternalfield.get(0).getName());
				if (this.richtext == false)
					return orderedexternalfield.get(0).castToType(o);
				// problem here
				return (E) (new RichText((String) (orderedexternalfield.get(0).castToType(o)))).generatePlainString();
			} else {
				String compactstring = "";
				for (int i = 0; i < this.orderedexternalfield.size(); i++) {
					compactstring += this.orderedexternalfield.get(i).castToType(o).toString();
					if (i > 0)
						compactstring += " ";
				}
				// crappy code but E is string (see a pattern here ?)
				return (E) (compactstring);
			}
		} else {
			// crappy code but E is String
			if (this.orderedexternalfield.size() == 1) {
				logger.info("processed field = " + this.getName() + " from field "
						+ this.orderedexternalfield.get(0).getName() + " with processing on external field");
				return (E) (fieldchoice.showDisplay((String) (orderedexternalfield.get(0).castToType(o))));
			} else {
				throw new RuntimeException("Not yet implemented");
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public E defaultValue() {
		if (orderedexternalfield.size() == 0) {
			return orderedexternalfield.get(0).defaultValue();
		} else {
			String compactstring = "";
			for (int i = 0; i < this.orderedexternalfield.size(); i++) {
				if (orderedexternalfield.get(i).defaultValue() != null) {
					compactstring += this.orderedexternalfield.get(i).defaultValue().toString();
				} else {
					compactstring += "Not Set";
				}
				if (i > 0)
					compactstring += " ";
			}
			return (E) (compactstring);
		}
	}

	@Override
	public Field<E> initBlankField() {
		ExternalField<E> field = new ExternalField<E>(this);
		return field;
	}

	/**
	 * @return gets the tooltip
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @return gets the display name
	 */
	public String getDisplayname() {
		return displayname;
	}

}
