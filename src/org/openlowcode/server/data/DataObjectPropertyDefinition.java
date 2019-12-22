/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data;

import java.util.ArrayList;

import java.util.logging.Logger;

import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.CustomloaderDefinition.CustomloaderHelper;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.specificstorage.JoinQueryConditionDefinition;
import org.openlowcode.server.data.specificstorage.TransientBigDecimal;
import org.openlowcode.server.data.storage.DecimalStoredField;
import org.openlowcode.server.data.storage.FieldSchema;
import org.openlowcode.server.data.storage.IntegerStoredField;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.server.data.storage.StoredFieldSchema;
import org.openlowcode.server.data.storage.StringStoredField;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.data.storage.TimestampStoredField;
import org.openlowcode.server.graphic.widget.SDecimalFormatter;

/**
 * The definition of a DataObjectProperty. A property is a part of a DataObject
 * that includes value-added services.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public abstract class DataObjectPropertyDefinition<E extends DataObject<E>>
		extends DataObjectElementDefinition<FieldSchema, E> {
	private static Logger logger = Logger.getLogger(DataObjectPropertyDefinition.class.getName());
	protected DataObjectDefinition<E> parentobject;
	protected ArrayList<ExternalFieldByJoinQuery> externalfieldsbyjoinquery;
	protected NamedList<ExternalFieldSchema> allexternalfields;

	/**
	 * @return the parent DataObjectDefinition this property definition is a part of
	 */
	public DataObjectDefinition<E> getParentObject() {
		return parentobject;
	}

	/**
	 * @return the list of external fields for this property.
	 */
	public NamedList<ExternalFieldSchema> getAllExternalFields() {
		return allexternalfields;
	}

	/**
	 * adds a stored field schema to this property. This allows this property to use
	 * some persistence
	 * 
	 * @param field the field to add
	 */
	protected void addStoredFieldSchema(StoredFieldSchema field) {
		super.addFieldSchema(field);

	}

	/**
	 * This method is called once at the beginning of a file loading. It returns a
	 * class that will be used to generate columns. This class will guarantee to be
	 * unique to the current loading done by the user, so data can be stored on it
	 * for logic involving several columns.
	 * 
	 * @return the transient loader helper if it exists, or null if it does not.
	 */
	public CustomloaderHelper<E> getTransientLoaderHelper() {
		return null;
	}

	/**
	 * @return all external fields by join query
	 */
	public ExternalFieldByJoinQuery[] getExternalFieldsByJoinQueries() {
		return externalfieldsbyjoinquery.toArray(new ExternalFieldByJoinQuery[0]);
	}

	/**
	 * creates a property definition
	 * 
	 * @param parentobject parent object definition
	 * @param name         name of the property (should be unique for the object)
	 */
	public DataObjectPropertyDefinition(DataObjectDefinition<E> parentobject, String name) {
		super(name);
		this.parentobject = parentobject;
		externalfieldsbyjoinquery = new ArrayList<ExternalFieldByJoinQuery>();
		allexternalfields = new NamedList<ExternalFieldSchema>();
	}

	/**
	 * @return get all external fields
	 * 
	 */
	public abstract ArrayList<ExternalFieldSchema> generateExternalSchema();

	/**
	 * initiates and sorts proeperly all external schemas
	 */
	public void initiateExternalFieldSchema() {

		ArrayList<ExternalFieldSchema> external = generateExternalSchema();
		if (external != null)
			logger.finer("initiating external field schema, number of fields = " + external.size());
		if (external != null)
			for (int i = 0; i < external.size(); i++) {
				ExternalFieldSchema<E> externalfield = external.get(i);
				if (externalfield == null)
					throw new RuntimeException("external field null for index = " + i + " out of " + external.size()
							+ " for " + this.parentobject + " " + this.getName() + ", all fields = "
							+ this.dropfieldnamelist());
				JoinQueryConditionDefinition joindef = externalfield.getJoinqueryconditiondefinition();
				boolean doesjoindefexists = false;
				ExternalFieldByJoinQuery relevantfieldfamily = null;
				for (int j = 0; i < externalfieldsbyjoinquery.size(); j++) {
					ExternalFieldByJoinQuery currentfieldfamily = externalfieldsbyjoinquery.get(j);
					if (currentfieldfamily.relatedjoincondition == joindef) {
						doesjoindefexists = true;
						relevantfieldfamily = currentfieldfamily;
					}
				}
				if (!doesjoindefexists) {
					relevantfieldfamily = new ExternalFieldByJoinQuery(joindef);
					externalfieldsbyjoinquery.add(relevantfieldfamily);
				}
				relevantfieldfamily.addFieldSchema(externalfield);
				allexternalfields.add(externalfield);
			}

	}

	/**
	 * each property can define query conditions to be applied by all other
	 * properties on an object. THis is mostly for properties that archive copy of
	 * the object in the table.
	 * 
	 * @param alias alias for which the query condition should be created;
	 * @return the query condition to apply or null if this property does not
	 *         require one.
	 */
	public abstract QueryCondition getUniversalQueryCondition(String alias);

	/**
	 * each property should define the list of fields to display as read-only fields
	 * of the object. All FieldSchemas referenced here should be the property own
	 * field schemas, else an exception will be thrown
	 * 
	 * @return
	 */

	public abstract FieldSchemaForDisplay[] setFieldSchemaToDisplay();

	/**
	 * @return the dataobject field definition of all property fields to display
	 * 
	 */
	public <F extends DataObject<F>> DataObjectFieldDefinition<F>[] getDataObjectFieldDefinition(
			NamedList<DisplayProfile<F>> activeprofiles) {
		logger.fine("--------------- starting processing of fields in property " + this.getName() + "/"
				+ this.getClass() + ", for object " + this.parentobject.getName() + " ----------");
		logger.fine(" list of display profiles = " + (activeprofiles != null ? activeprofiles.dropNameList() : "null"));
		ArrayList<DataObjectFieldDefinition> returnedfields = new ArrayList<DataObjectFieldDefinition>();

		FieldSchemaForDisplay[] userdefinedfields = setFieldSchemaToDisplay();
		if (userdefinedfields != null)
			for (int i = 0; i < userdefinedfields.length; i++) {
				// control if present in the list of fields then add it
				FieldSchemaForDisplay currentselectedfield = userdefinedfields[i];
				if (currentselectedfield.getField() == null)
					throw new RuntimeException(String.format("current selected field is null"
							+ currentselectedfield.display + ", property " + this.getName()));
				boolean isvalid = false;
				for (int j = 0; j < this.getFieldSchemaNumber(); j++) {
					FieldSchema thisfieldoffulllist = this.getFieldSchema(j);
					if (thisfieldoffulllist == currentselectedfield.getField())
						isvalid = true;
				}
				if (!isvalid)
					throw new RuntimeException("The field to display is not a field of the property : "
							+ currentselectedfield.getField().getName());
				returnedfields.add(currentselectedfield.getDataObjectFieldDefinition());
			}
		// transform the external fields and show them all

		for (int i = 0; i < this.allexternalfields.getSize(); i++) {
			ExternalFieldSchema externalfield = this.allexternalfields.get(i);
			DisplayProfile fieldhideprofile = externalfield.getHideProfile();
			boolean showfield = false;
			if ((activeprofiles != null) && (fieldhideprofile != null)) { // both
																			// profile
																			// info
																			// exist
				if (activeprofiles.lookupOnName(fieldhideprofile.getName()) == null)
					showfield = true;

			} else {
				showfield = true;

			}
			if (showfield) {

				if (!externalfield.isOrderedAsInteger()) {
					// ------> treating properly choice field
					if (externalfield.getFieldChoice() != null) {

						FieldSchemaForDisplay externalfieldfordisplay = new FieldSchemaForDisplay(
								externalfield.getDisplayName(activeprofiles), externalfield.getTooltip(), externalfield,
								externalfield.isDisplayInTitle(), externalfield.isDiplayInBottomNotes(),
								externalfield.getPriority(), externalfield.getDisplaycolumn(), this.parentobject);
						externalfieldfordisplay.setChoiceForString(externalfield.getFieldChoice());
						returnedfields.add(externalfieldfordisplay.getDataObjectFieldDefinition());
					} else {
						// ------> Treating Rich text
						if (externalfield.isRichText()) {
							logger.fine("      ----> adding external field with normal order, normal tring "
									+ externalfield.getDisplayName(activeprofiles));
							FieldSchemaForDisplay externalfieldfordisplay = new FieldSchemaForDisplay(
									externalfield.getDisplayName(activeprofiles), externalfield.getTooltip(),
									externalfield, externalfield.isDisplayInTitle(),
									externalfield.isDiplayInBottomNotes(), externalfield.getPriority(),
									externalfield.getDisplaycolumn(), this.parentobject);
							externalfieldfordisplay.setRichText();
							returnedfields.add(externalfieldfordisplay.getDataObjectFieldDefinition());
						} else {
							// ------> Treat normal text
							logger.fine("      ----> adding external field with normal order, normal tring "
									+ externalfield.getDisplayName(activeprofiles));
							FieldSchemaForDisplay externalfieldfordisplay = new FieldSchemaForDisplay(
									externalfield.getDisplayName(activeprofiles), externalfield.getTooltip(),
									externalfield, externalfield.isDisplayInTitle(),
									externalfield.isDiplayInBottomNotes(), externalfield.getPriority(),
									externalfield.getDisplaycolumn(), this.parentobject);
							returnedfields.add(externalfieldfordisplay.getDataObjectFieldDefinition());
						}
					}

				} else {
					logger.fine("      ----> adding external field with integer order "
							+ externalfield.getDisplayName(activeprofiles));
					FieldSchemaForDisplay externalfieldfordisplay = new FieldSchemaForDisplay(
							externalfield.getDisplayName(activeprofiles), externalfield.getTooltip(), externalfield,
							externalfield.isDisplayInTitle(), externalfield.isDiplayInBottomNotes(),
							externalfield.getPriority(), externalfield.getDisplaycolumn(), this.parentobject,
							externalfield.getNumberOffsetForOrdering());
					returnedfields.add(externalfieldfordisplay.getDataObjectFieldDefinition());

				}
			} else {
				logger.fine("          ----> discarding field " + externalfield.getDisplayname());
			}
		}

		return returnedfields.toArray(new DataObjectFieldDefinition[0]);
	}

	/**
	 * @param objectdef definition of the object
	 * @param fieldname name of the field
	 * @return a field marker
	 */
	protected DataObjectFieldMarker getFieldMarker(DataObjectDefinition objectdef, String fieldname) {
		if (this.allexternalfields.lookupOnName(fieldname) != null)
			return new DataObjectFieldMarker(objectdef, this.allexternalfields.lookupOnName(fieldname).getName());
		if (this.getFieldSchemaByName(fieldname) != null)
			return new DataObjectFieldMarker(objectdef, this.getFieldSchemaByName(fieldname).getName());
		throw new RuntimeException("fieldname " + fieldname + " was not found, either as external field ("
				+ this.allexternalfields.dropNameList() + ") or as own object field (" + this.dropfieldnamelist()
				+ ")");
	}

	/**
	 * generates the flat file loader for this property
	 * 
	 * @param objectdefinition  definition of the object
	 * @param columnattributes  column attributes
	 * @param propertyextractor an extractor providing this property from the object
	 * @param locale            locale if loaded by CSV
	 * @return a flat file loader
	 */
	public abstract FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
			String[] columnattributes, PropertyExtractor<E> propertyextractor,
			ChoiceValue<ApplocaleChoiceDefinition> locale);

	/**
	 * @return a list of fields for the property
	 */
	public abstract String[] getLoaderFieldList();

	/**
	 * @param name name as returned by the function getLoaderFieldList
	 * @return an array of 4 strings
	 *         <ul>
	 *         <li>full name of the field</li>
	 *         <li>OPTIONAL or MANDATORY</li>
	 *         <li>Sample Values</li>
	 *         <li>Comments including options</li>
	 *         </ul>
	 */
	public abstract String[] getLoaderFieldSample(String name);

	/**
	 * This class regroups the list of all external fields, and the related join
	 * query condition to get the side data.
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class ExternalFieldByJoinQuery {
		private ArrayList<ExternalFieldSchema> fields;
		private JoinQueryConditionDefinition relatedjoincondition;

		/**
		 * @return the join query condition
		 */
		public JoinQueryConditionDefinition getJoinQueryConditionDefinition() {
			return relatedjoincondition;
		}

		/**
		 * creates an object with specified join query condition, and not yet any field
		 * defined
		 * 
		 * @param relatedjoincondition
		 */
		public ExternalFieldByJoinQuery(JoinQueryConditionDefinition relatedjoincondition) {
			this.relatedjoincondition = relatedjoincondition;
			this.fields = new ArrayList<ExternalFieldSchema>();
		}

		/**
		 * adds a new field schema
		 * 
		 * @param thisfieldschema field schema to add
		 */
		public void addFieldSchema(ExternalFieldSchema thisfieldschema) {
			fields.add(thisfieldschema);
		}

		/**
		 * @return the list of external fields
		 */
		public ExternalFieldByJoinQuery[] getFields() {
			return fields.toArray(new ExternalFieldByJoinQuery[0]);
		}

		/**
		 * checks that the fields defined are consistent, and adds a restriction on
		 * table alias to only get the specified fields
		 * 
		 * @param alias          side table alias
		 * @param maintablealias lain table alias
		 */
		public void restrainTableAlias(TableAlias alias, String maintablealias) {
			for (int i = 0; i < fields.size(); i++) {
				for (int j = 0; j < fields.get(i).getExternalTableField().size(); j++) {
					StoredFieldSchema externalfieldcomponent = (StoredFieldSchema) fields.get(i).getExternalTableField()
							.get(j);
					if (externalfieldcomponent == null)
						throw new RuntimeException("externalfieldcomponent is null for index " + j + " for field "
								+ fields.get(i).getName());
					alias.addFieldSelection(externalfieldcomponent, maintablealias + fields.get(i).getName() + "_" + j);
				}
			}
		}
	}

	/**
	 * This class allows to define a field to display for this property
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class FieldSchemaForDisplay<E extends DataObject<E>> {
		private String display;
		private String tooltip;
		private FieldSchema field;
		private boolean showintitle;
		private boolean showinbottomnotes;
		private boolean readonly;
		private SDecimalFormatter formatter;
		private boolean orderedasinteger = false;
		private int numberoffset;
		private boolean richtext = false;
		private FieldChoiceDefinition choiceforstring;
		private DataObjectDefinition<E> objectdefinition;

		/**
		 * defines this text as richtext
		 */
		public void setRichText() {
			this.richtext = true;
		}

		/**
		 * a figure between -1000 and 1000 to define order to show attributes
		 */
		private int priority;

		/**
		 * the size (in number of characters) between 0 and 200 defining the default
		 * width of a column
		 */
		private int defaultcolumn;

		/**
		 * @return the field priority in display for the object
		 */
		public int getPriority() {
			return this.priority;
		}

		/**
		 * @return true if read-only
		 */
		public boolean isReadonly() {
			return showintitle;
		}

		/**
		 * @return true if field to be shown in title
		 */
		public boolean isShowintitle() {
			return showintitle;
		}

		/**
		 * @return true if field to be shown in bottom notes
		 */
		public boolean isShowinbottomnotes() {
			return showinbottomnotes;
		}

		/**
		 * @return the display name of the field
		 */
		public String getDisplay() {
			return display;
		}

		/**
		 * @return the tooltip for rollover mouse
		 */
		public String getTooltip() {
			return tooltip;
		}

		/**
		 * @return the storedfield schema
		 */
		public FieldSchema getField() {
			return field;
		}

		/**
		 * Creates a standard read-only field for display
		 * 
		 * @param display           name of the field for display
		 * @param tooltip           tooltip to show for rollover
		 * @param field             field in the persistence layer
		 * @param showintitle       field to show in title
		 * @param showinbottomnotes field to show in bottom notes
		 * @param priority          priority for display of field in object
		 * @param defaultcolumn     number of columns to show in a table
		 * @param objectdefinition  the parent object of this field
		 */
		public FieldSchemaForDisplay(String display, String tooltip, FieldSchema field, boolean showintitle,
				boolean showinbottomnotes, int priority, int defaultcolumn, DataObjectDefinition<E> objectdefinition) {
			super();
			this.display = display;
			this.tooltip = tooltip;
			this.field = field;
			this.showintitle = showintitle;
			this.showinbottomnotes = showinbottomnotes;
			this.readonly = true;
			this.defaultcolumn = defaultcolumn;
			this.priority = priority;
			this.objectdefinition = objectdefinition;
		}

		/**
		 * Creates read-only field for display that can be sorted as a number
		 * 
		 * @param display           name of the field for display
		 * @param tooltip           tooltip to show for rollover
		 * @param field             field in the persistence layer
		 * @param showintitle       field to show in title
		 * @param showinbottomnotes field to show in bottom notes
		 * @param priority          priority for display of field in object
		 * @param defaultcolumn     number of columns to show in a table
		 * @param objectdefinition  the parent object of this field
		 * @param integeroffset     the offset to sort the field as a number
		 */
		public FieldSchemaForDisplay(String display, String tooltip, FieldSchema field, boolean showintitle,
				boolean showinbottomnotes, int priority, int defaultcolumn, DataObjectDefinition<E> objectdefinition,
				int integeroffset) {
			super();
			this.display = display;
			this.tooltip = tooltip;
			this.field = field;
			this.showintitle = showintitle;
			this.showinbottomnotes = showinbottomnotes;
			this.readonly = true;
			this.defaultcolumn = defaultcolumn;
			this.priority = priority;
			this.objectdefinition = objectdefinition;
			this.orderedasinteger = true;
			this.numberoffset = integeroffset;
		}

		/**
		 * Creates a field for display that can be sorted as a number that can be
		 * read-write
		 * 
		 * @param display           name of the field for display
		 * @param tooltip           tooltip to show for rollover
		 * @param field             field in the persistence layer
		 * @param showintitle       field to show in title
		 * @param showinbottomnotes field to show in bottom notes
		 * @param readonly          true if field is readonly, false else
		 * @param priority          priority for display of field in object
		 * @param defaultcolumn     number of columns to show in a table
		 * @param objectdefinition  the parent object of this field
		 * @param integeroffset
		 */
		public FieldSchemaForDisplay(String display, String tooltip, FieldSchema field, boolean showintitle,
				boolean showinbottomnotes, boolean readonly, int priority, int defaultcolumn,
				DataObjectDefinition<E> objectdefinition, int integeroffset) {
			super();
			this.display = display;
			this.tooltip = tooltip;
			this.field = field;
			this.showintitle = showintitle;
			this.showinbottomnotes = showinbottomnotes;
			this.readonly = readonly;
			this.defaultcolumn = defaultcolumn;
			this.priority = priority;
			this.objectdefinition = objectdefinition;
			this.orderedasinteger = true;
			this.numberoffset = integeroffset;
		}

		/**
		 * Creates a field that can be read-write
		 * 
		 * @param display           name of the field for display
		 * @param tooltip           tooltip to show for rollover
		 * @param field             field in the persistence layer
		 * @param showintitle       field to show in title
		 * @param showinbottomnotes field to show in bottom notes
		 * @param readonly          true if field is readonly, false else
		 * @param priority          priority for display of field in object
		 * @param defaultcolumn     number of columns to show in a table
		 * @param objectdefinition  the parent object of this field
		 */
		public FieldSchemaForDisplay(String display, String tooltip, FieldSchema field, boolean showintitle,
				boolean showinbottomnotes, boolean readonly, int priority, int defaultcolumn,
				DataObjectDefinition<E> objectdefinition) {
			super();
			this.display = display;
			this.tooltip = tooltip;
			this.field = field;
			this.showintitle = showintitle;
			this.showinbottomnotes = showinbottomnotes;
			this.readonly = readonly;
			this.defaultcolumn = defaultcolumn;
			this.priority = priority;
			this.objectdefinition = objectdefinition;

		}

		/**
		 * @param choiceforstring
		 */
		public void setChoiceForString(FieldChoiceDefinition choiceforstring) {
			this.choiceforstring = choiceforstring;
		}

		/**
		 * Creates a field that can be read-write formatted as a choice field
		 * 
		 * @param display           name of the field for display
		 * @param tooltip           tooltip to show for rollover
		 * @param field             field in the persistence layer
		 * @param showintitle       field to show in title
		 * @param showinbottomnotes field to show in bottom notes
		 * @param readonly          true if field is readonly, false else
		 * @param choiceforstring   choicefield to format field payload
		 * @param priority          priority for display of field in object
		 * @param defaultcolumn     number of columns to show in a table
		 * @param objectdefinition  the parent object of this field
		 */
		public FieldSchemaForDisplay(String display, String tooltip, FieldSchema field, boolean showintitle,
				boolean showinbottomnotes, boolean readonly, FieldChoiceDefinition choiceforstring, int priority,
				int defaultcolumn, DataObjectDefinition<E> objectdefinition) {
			super();
			this.display = display;
			this.tooltip = tooltip;
			this.field = field;
			this.showintitle = showintitle;
			this.showinbottomnotes = showinbottomnotes;
			this.readonly = readonly;
			this.choiceforstring = choiceforstring;
			this.priority = priority;
			this.defaultcolumn = defaultcolumn;
			this.objectdefinition = objectdefinition;
		}

		/**
		 * Creates a decimal read-only decimal field with a formatter
		 * 
		 * @param display           name of the field for display
		 * @param tooltip           tooltip to show for rollover
		 * @param field             field in the persistence layer
		 * @param formatter
		 * @param showintitle       field to show in title
		 * @param showinbottomnotes field to show in bottom notes
		 * @param priority          priority for display of field in object
		 * @param defaultcolumn     number of columns to show in a table
		 * @param objectdefinition  the parent object of this field
		 */
		public FieldSchemaForDisplay(String display, String tooltip, DecimalStoredField field,
				SDecimalFormatter formatter, boolean showintitle, boolean showinbottomnotes, int priority,
				int defaultcolumn, DataObjectDefinition<E> objectdefinition) {
			this(display, tooltip, field, showintitle, showinbottomnotes, priority, defaultcolumn, objectdefinition);
			this.formatter = formatter;
		}

		/**
		 * @return the field definition
		 */
		public DataObjectFieldDefinition<E> getDataObjectFieldDefinition() {
			// normal case
			FieldSchema fieldtoprocess = field;
			String name = field.getName();
			// external field: keeps the name but gets the referencedfield for format
			if (field instanceof ExternalFieldSchema) {
				ExternalFieldSchema externalfield = (ExternalFieldSchema) field;
				if (externalfield.getExternalTableField().size() == 1) {
					fieldtoprocess = (StoredFieldSchema) externalfield.getExternalTableField().get(0);
				} else {
					// several fields, this is necessarily a string field

					if (this.orderedasinteger) {
						StringDataObjectFieldDefinition fielddef = new StringDataObjectFieldDefinition(name, display,
								tooltip, 0, false, true, this.showintitle, this.showinbottomnotes, priority,
								defaultcolumn, false, objectdefinition);
						fielddef.setOrderedAsInteger(this.numberoffset);
						return fielddef;
					} else {
						if (this.choiceforstring == null) {

							return new StringDataObjectFieldDefinition(name, display, tooltip, 0, false, true,
									this.showintitle, this.showinbottomnotes, priority, defaultcolumn, this.richtext,
									objectdefinition);
						} else {
							return new ChoiceDataObjectFieldDefinition(name, display, tooltip, readonly,
									this.showintitle, this.showinbottomnotes, choiceforstring, this.priority,
									this.defaultcolumn, objectdefinition);
						}
					}

				}
			}
			if (fieldtoprocess instanceof StringStoredField) {
				StringStoredField stringfield = (StringStoredField) fieldtoprocess;
				if (choiceforstring == null) {
					if (this.orderedasinteger) {
						StringDataObjectFieldDefinition fielddef = new StringDataObjectFieldDefinition(name, display,
								tooltip, stringfield.getMaximumLength(), false, this.readonly, this.showintitle,
								this.showinbottomnotes, this.priority, this.defaultcolumn, false, objectdefinition);
						fielddef.setOrderedAsInteger(this.numberoffset);
						return fielddef;
					} else {
						return new StringDataObjectFieldDefinition(name, display, tooltip,
								stringfield.getMaximumLength(), false, this.readonly, this.showintitle,
								this.showinbottomnotes, this.priority, this.defaultcolumn, this.richtext,
								objectdefinition);

					}

				} else {
					return new ChoiceDataObjectFieldDefinition(name, display, tooltip, readonly, this.showintitle,
							this.showinbottomnotes, choiceforstring, this.priority, this.defaultcolumn,
							objectdefinition);
				}
			}
			if (fieldtoprocess instanceof TimestampStoredField) {

				return new DateDataObjectFieldDefinition(name, display, tooltip, true, this.readonly, this.showintitle,
						this.showinbottomnotes, this.priority, objectdefinition);
			}
			if (fieldtoprocess instanceof IntegerStoredField) {

				return new IntegerDataObjectFieldDefinition(name, display, tooltip, this.readonly, this.showintitle,
						this.showinbottomnotes, this.priority, objectdefinition);
			}
			if (fieldtoprocess instanceof DecimalStoredField) {
				DecimalStoredField decimalfield = (DecimalStoredField) fieldtoprocess;
				if (formatter == null)
					return new DecimalDataObjectFieldDefinition(name, display, tooltip, decimalfield.getPrecision(),
							decimalfield.getScale(), this.readonly, this.showintitle, this.showinbottomnotes,
							this.priority, objectdefinition);
				if (formatter != null)
					return new DecimalDataObjectFieldDefinition(name, display, tooltip, decimalfield.getPrecision(),
							decimalfield.getScale(), this.readonly, this.showintitle, this.showinbottomnotes, formatter,
							this.priority, objectdefinition);

			}
			if (fieldtoprocess instanceof TransientBigDecimal) {

				if (formatter == null)
					return new DecimalDataObjectFieldDefinition(name, display, tooltip, 1000, 100, this.readonly,
							this.showintitle, this.showinbottomnotes, this.priority, objectdefinition);
				if (formatter != null)
					return new DecimalDataObjectFieldDefinition(name, display, tooltip, 1000, 100, this.readonly,
							this.showintitle, this.showinbottomnotes, formatter, this.priority, objectdefinition);

			}

			throw new RuntimeException("field format '" + fieldtoprocess.getClass().getName() + "' is not supported "
					+ this.display + "," + this.tooltip + "," + this.field.getName());
		}
	}
}
