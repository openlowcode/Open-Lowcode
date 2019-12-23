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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedInterface;
import org.openlowcode.tools.misc.NamedList;

import org.openlowcode.server.data.DataObjectPropertyDefinition.FieldSchemaForDisplay;
import org.openlowcode.server.data.formula.DataUpdateTrigger;
import org.openlowcode.server.data.properties.Autolinkobject;
import org.openlowcode.server.data.properties.Computeddecimal;
import org.openlowcode.server.data.properties.HasFlexibleDefinition;
import org.openlowcode.server.data.properties.Linkedfromchildren;
import org.openlowcode.server.data.properties.Linkedtoparent;
import org.openlowcode.server.data.properties.Linkobject;
import org.openlowcode.server.data.properties.Numbered;
import org.openlowcode.server.data.properties.Uniqueidentified;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.data.properties.Versioned;
import org.openlowcode.server.data.storage.Field;
import org.openlowcode.server.data.storage.Row;
import org.openlowcode.server.data.storage.StoredField;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.tools.structure.ChoiceDataElt;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.IntegerDataElt;
import org.openlowcode.tools.structure.MultipleChoiceDataElt;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TimePeriodDataElt;

/**
 * A data object is the basic unit of management in Open Lowcode. It wraps a
 * number of fields storing elementary information and properties adding
 * features. <b>In Open Lowcode, subclasses of dataobject are auto-generated.
 * Creating manually a subclass of data-object can create unforecasted issues,
 * and is discouraged</b>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class DataObject<E extends DataObject<E>> extends Named {
	private DataObjectDefinition<E> definition;
	private boolean frozen;

	/**
	 * Triggers used in the UniqueIdentified refresh method
	 * 
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public NamedList<DataUpdateTrigger<E>> getDataRefreshTriggers() {
		NamedList<DataUpdateTrigger<E>> triggers = new NamedList<DataUpdateTrigger<E>>();
		for (int i = 0; i < this.payload.getPropertyNumber(); i++) {
			DataObjectProperty<E> property = this.payload.getPropertyAtIndex(i);

			if (property instanceof Computeddecimal) {
				Computeddecimal<E> computeddecimal = (Computeddecimal<E>) property;
				DataUpdateTrigger<E> trigger = computeddecimal.getFieldTrigger();
				triggers.add(trigger);
			}
		}
		return triggers;
	}

	/**
	 * Normally, fields of the object are determined by the object type, and are
	 * identical for all instances of the same data object class. The flexible
	 * fields are determined for each instance depending on the program logic. This
	 * method returns the list of flexible fields
	 * 
	 * @return the list of flexible fields for the object
	 */
	@SuppressWarnings("unchecked")
	public List<FieldSchemaForDisplay<E>> getFlexibleFieldsDefinition() {
		ArrayList<FieldSchemaForDisplay<E>> flexiblefields = new ArrayList<FieldSchemaForDisplay<E>>();
		for (int i = 0; i < this.payload.getPropertyNumber(); i++) {
			DataObjectProperty<E> thisproperty = this.payload.getPropertyAtIndex(i);
			if (thisproperty instanceof HasFlexibleDefinition) {
				HasFlexibleDefinition<E> propertycasted = (HasFlexibleDefinition<E>) thisproperty;
				PropertyDynamicDefinitionHelper<E, ?> propertyhelper = propertycasted.getFlexibleDefinition();

				flexiblefields.addAll(Arrays.asList(propertyhelper.getFieldsToDisplay()));
			}
		}
		return flexiblefields;
	}

	/**
	 * A deep copy will return a fresh object that has all fields copied and all
	 * properties initiatilized like a new object. This is the prefered method for
	 * creating new iterations or versions of object for the Gallium framework. It
	 * is also a useful framework for the home-made methods that have to perform
	 * copies of objects.
	 * 
	 * @return
	 */
	public abstract E deepcopy();

	/**
	 * This method is intented only to be used on properties internal code. It will
	 * allow complex algorithms where the same properties on different objects
	 * interact. E.g. it is used to perform deep copy of objects during iteration
	 * and versions to make a smart deep copy adapted to the context
	 * 
	 * @param otherobjectproperty similar property from another object
	 * @return the casted property
	 */
	public abstract <Z extends DataObjectProperty<E>> Z getPropertyForObject(Z otherobjectproperty);

	/**
	 * This is a dirty trick to get left object uniqueidentified property from a
	 * link object.
	 * 
	 * @param linkobjectproperty the link object property
	 * @return the casted property
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Uniqueidentified<E> getUniqueidentiedFromLinkObject(Linkobject linkobjectproperty) {
		if (linkobjectproperty == null)
			throw new RuntimeException(
					"To get uniqueidentified on object " + this.getName() + ", non null linkobject is required");
		DataObjectProperty<E> uniqueidentifieduncast = this.payload.lookupPropertyOnName("UNIQUEIDENTIFIED");
		return (Uniqueidentified<E>) uniqueidentifieduncast;
	}

	/**
	 * This is a dirty trick to get left object uniqueidentified property from an
	 * auto link
	 * 
	 * @param autolinkobjectproperty the autolink object property
	 * @return the casted property
	 */
	@SuppressWarnings("unchecked")
	public <Z extends Autolinkobject<?, E>> Uniqueidentified<E> getUniqueidentiedFromAutolinkObject(
			Z autolinkobjectproperty) {
		if (autolinkobjectproperty == null)
			throw new RuntimeException(
					"To get uniqueidentified on object " + this.getName() + ", non null autolinkobject is required");
		DataObjectProperty<E> uniqueidentifieduncast = this.payload.lookupPropertyOnName("UNIQUEIDENTIFIED");
		return (Uniqueidentified<E>) uniqueidentifieduncast;
	}

	/**
	 * This is a dirty trick to get parent object uniqueidentified property from an
	 * parent link
	 * 
	 * @param linkedtoparentobjectproperty the linkedtoparent property
	 * @return the casted property
	 */
	@SuppressWarnings("unchecked")
	public <Z extends Linkedtoparent<?, E>> Uniqueidentified<E> getUniqueidentiedFromLinkedToParent(
			Z linkedtoparentobjectproperty) {
		if (linkedtoparentobjectproperty == null)
			throw new RuntimeException(
					"To get uniqueidentified on object " + this.getName() + ", non null linked to parent is required");
		DataObjectProperty<E> uniqueidentifieduncast = this.payload.lookupPropertyOnName("UNIQUEIDENTIFIED");
		return (Uniqueidentified<E>) uniqueidentifieduncast;
	}

	/**
	 * This is a dirty trick to get parent object uniqueidentified property from a
	 * linked from children link
	 * 
	 * @param linkedfromchildrenproperty the linkedfromchildren property
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public <Z extends Linkedfromchildren<?, E>> Uniqueidentified<E> getUniqueidentiedFromLinkedFromChildren(
			Z linkedfromchildrenproperty) {
		if (linkedfromchildrenproperty == null)
			throw new RuntimeException(
					"To get uniqueidentified on object " + this.getName() + ", non null linked to parent is required");
		DataObjectProperty<E> uniqueidentifieduncast = this.payload.lookupPropertyOnName("UNIQUEIDENTIFIED");
		return (Uniqueidentified<E>) uniqueidentifieduncast;
	}

	/**
	 * when the object is updated, it can fire triggers. This method returns all the
	 * triggers. This should be only called by the framework
	 * 
	 * @return all the triggers
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public NamedList<DataUpdateTrigger<E>> getDataUpdateTriggers() {
		NamedList<DataUpdateTrigger<E>> triggers = new NamedList<DataUpdateTrigger<E>>();
		for (int i = 0; i < this.payload.getFieldNumber(); i++) {
			DataObjectField field = this.payload.getFieldAtIndex(i);
			triggers.mergeWithNamedListIfNotExist(field.getTriggersForThisUpdate());
		}
		for (int i = 0; i < this.payload.getPropertyNumber(); i++) {
			DataObjectProperty property = this.payload.getPropertyAtIndex(i);
			triggers.mergeWithNamedListIfNotExist(property.getTriggersForThisUpdate());
		}
		return triggers;
	}

	/**
	 * a long text drop of the object including all fields content
	 * 
	 * @return the text
	 */
	@SuppressWarnings("unchecked")
	public String dropToString() {
		String dropstring = "DATAOBJECT:" + this.getName();
		for (int i = 0; i < this.payload.getFieldNumber(); i++) {
			dropstring += ";";
			DataObjectField<?, E> field = this.payload.getFieldAtIndex(i);
			dropstring += field.getName() + ":" + field.getDataElement().defaultTextRepresentation();
		}
		for (int i = 0; i < this.payload.getPropertyNumber(); i++) {
			DataObjectProperty<E> property = this.payload.getPropertyAtIndex(i);
			DataElt[] propertydisplaydata = property.getDisplayDataElt();
			for (int j = 0; j < propertydisplaydata.length; j++) {
				DataElt thispropertydisplay = propertydisplaydata[j];
				dropstring += ";";
				if (thispropertydisplay instanceof SimpleDataElt) {
					SimpleDataElt simplepropertydisplay = (SimpleDataElt) thispropertydisplay;
					dropstring += thispropertydisplay.getName() + ":"
							+ simplepropertydisplay.defaultTextRepresentation();
				} else {
					dropstring += thispropertydisplay.getName() + ":" + thispropertydisplay.getType().printType();
				}
			}
		}
		return dropstring;
	}

	/**
	 * This method returns the id string for an object:
	 * <ul>
	 * <li>if object is numbered, bring back the number</li>
	 * <li>if object has version, bring back the version</li>
	 * <li>if object has named, bring back the name</li>
	 * <li>if object has id, but not number, bring back the number</li>
	 * </ul>
	 * 
	 * @return the ID as defined
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String dropIdToString() {
		StringBuffer id = new StringBuffer();
		boolean numbered = false;
		// ------------------------ Check Number
		for (int i = 0; i < payload.getPropertyNumber(); i++) {
			DataObjectProperty<E> property = this.payload.getPropertyAtIndex(i);
			if (property instanceof Numbered) {
				Numbered numberedproperty = (Numbered) property;
				numbered = true;
				id.append(numberedproperty.getNr());
			}
		}
		// ------------------------- Check Version
		for (int i = 0; i < payload.getPropertyNumber(); i++) {
			DataObjectProperty<E> property = this.payload.getPropertyAtIndex(i);
			if (property instanceof Versioned) {
				Versioned versionedproperty = (Versioned) property;
				if (id.length() > 0)
					id.append(" ");
				id.append(versionedproperty.getVersion());
			}
		}

		// ------------------------- Check Version
		for (int i = 0; i < payload.getPropertyNumber(); i++) {
			DataObjectProperty<E> property = this.payload.getPropertyAtIndex(i);
			if (property instanceof org.openlowcode.server.data.properties.Named) {
				org.openlowcode.server.data.properties.Named<E> namedproperty = (org.openlowcode.server.data.properties.Named) property;
				if (id.length() > 0)
					id.append(" ");
				id.append(namedproperty.getObjectname());
			}
		}
		if (!numbered)
			for (int i = 0; i < payload.getPropertyNumber(); i++) {
				DataObjectProperty<E> property = this.payload.getPropertyAtIndex(i);
				if (property instanceof Uniqueidentified) {
					Uniqueidentified<E> uiproperty = (Uniqueidentified) property;
					if (id.length() > 0)
						id.append(" ");
					id.append(uiproperty.getId().getId());
				}
			}
		return id.toString();
	}

	protected DataObjectPayload payload;

	/**
	 * creates a new Data Object with a blank payload.
	 * 
	 * @param definition object definition
	 * 
	 */
	public DataObject(DataObjectDefinition<E> definition) {
		super(definition.getName());
		this.definition = definition;
		this.payload = definition.initiateBlankPayload();
		this.frozen = false;

	}

	/**
	 * freezes the object so that it cannot be modified. This information is sent to
	 * the client
	 */
	public void setFrozen() {
		this.frozen = true;
	}

	/**
	 * unfreezes the object so that it can be modified again
	 */
	public void setUnfrozen() {
		this.frozen = false;
	}

	/**
	 * Generates a data object from the database
	 * 
	 * @param definition definition of the data object
	 * @param row        the row coming from the database
	 * @param alias      the alias to look at in the query
	 */
	public DataObject(DataObjectDefinition<E> definition, Row row, TableAlias alias) {
		this(definition);
		payload.initFromDB(row, alias);
	}

	/**
	 * updates the objects from data (only fields)
	 * 
	 * @param object a Data structure with information about fields (note: this
	 *               method does not update property information because property
	 *               linked data is updated through actions
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void updateFromObjectContent(ObjectDataElt object) {

		for (int i = 0; i < object.fieldnumber(); i++) {
			SimpleDataElt thisfield = object.getField(i);
			String fieldname = thisfield.getName();

			boolean treated = false;

			// ---------------------------- OBJECT FIELDS ----------------------------------
			if (thisfield.getPropertyname() == null) {
				if (thisfield instanceof TextDataElt) {
					TextDataElt thistextfield = (TextDataElt) thisfield;
					// TODO get also fields inside properties
					DataObjectElement field = this.payload.lookupSimpleFieldOnName(fieldname);
					if (field == null)
						throw new RuntimeException(
								String.format("Try to update data in field %s  but it does not exist in object %s",
										fieldname, this.getName()));
					if ((!(field instanceof StringDataObjectField))
							&& (!(field instanceof EncryptedStringDataObjectField)))
						throw new RuntimeException(
								String.format("Field  %s should be text , but is actually %s", fieldname, field));

					if (field instanceof StringDataObjectField) {
						StringDataObjectField thisstringfield = (StringDataObjectField) field;
						int length = ((StringDataObjectFieldDefinition) (thisstringfield.definition)).getMaxlength();
						if (thistextfield.getPayload() != null)
							if (thistextfield.getPayload().length() >= length)
								throw new RuntimeException(String.format(
										"field too long, fieldname = %s , length = %d, maximum length = %d ", fieldname,
										thistextfield.getPayload().length(), length));
						thisstringfield.setValue(thistextfield.getPayload());
						treated = true;
					}

					if (field instanceof EncryptedStringDataObjectField) {
						EncryptedStringDataObjectField thisencryptedstringfield = (EncryptedStringDataObjectField) field;
						int length = ((EncryptedStringDataObjectFieldDefinition) (thisencryptedstringfield.definition))
								.getLength();
						if (thistextfield.getPayload().length() >= length)
							throw new RuntimeException(
									String.format("field too long, fieldname = %s , length = %d, maximum length = %d ",
											fieldname, thistextfield.getPayload().length(), length));
						boolean update = true;
						// encrypted fields one way are not exported with the object (the encrypted
						// value does not make any sense
						// so when receiving such field as empty, it is not updated into the object
						if (thisencryptedstringfield.isEncryptedOneWay())
							if (thistextfield.getPayload().length() == 0)
								update = false;
						if (update)
							thisencryptedstringfield.setValue(thistextfield.getPayload());
						treated = true;
					}
				}
				if (thisfield instanceof DateDataElt) {
					DateDataElt thisdatefield = (DateDataElt) thisfield;
					DataObjectElement field = this.payload.lookupSimpleFieldOnName(fieldname);
					if (field == null)
						throw new RuntimeException(
								String.format("Try to update data in field %s  but it does not exist in object %s",
										fieldname, this.getName()));
					if (!(field instanceof DateDataObjectField))
						throw new RuntimeException(
								String.format("Field  %s should be date, but is actually %s", fieldname, field));
					DateDataObjectField thisdateobjectfield = (DateDataObjectField) field;
					thisdateobjectfield.setValue(thisdatefield.getPayload());
					treated = true;
				}
				// treats time period field
				if (thisfield instanceof TimePeriodDataElt) {
					TimePeriodDataElt timeperiodfield = (TimePeriodDataElt) thisfield;
					DataObjectElement field = this.payload.lookupSimpleFieldOnName(fieldname);
					if (field == null)
						throw new RuntimeException(
								String.format("Try to update data in field %s  but it does not exist in object %s",
										fieldname, this.getName()));
					if (!(field instanceof TimePeriodDataObjectField))
						throw new RuntimeException(
								String.format("Field  %s should be TimePeriod, but is actually %s", fieldname, field));
					TimePeriodDataObjectField thistimeperiodobjectfield = (TimePeriodDataObjectField) field;
					thistimeperiodobjectfield.setValue(timeperiodfield.getPayload());
					treated = true;

				}
				if (thisfield instanceof DecimalDataElt) {
					DecimalDataElt thisdecimalfield = (DecimalDataElt) thisfield;
					DataObjectElement field = this.payload.lookupSimpleFieldOnName(fieldname);
					if (field == null)
						throw new RuntimeException(
								String.format("Try to update data in field %s  but it does not exist in object %s",
										fieldname, this.getName()));
					if (!(field instanceof DecimalDataObjectField))
						throw new RuntimeException(
								String.format("Field  %s should be decimal, but is actually %s", fieldname, field));
					DecimalDataObjectField thisdecimalobjectfield = (DecimalDataObjectField) field;
					thisdecimalobjectfield.setValue(thisdecimalfield.getPayload());
					treated = true;
				}

				if (thisfield instanceof IntegerDataElt) {
					IntegerDataElt thisinteger = (IntegerDataElt) thisfield;
					DataObjectElement field = this.payload.lookupSimpleFieldOnName(fieldname);
					if (field == null)
						throw new RuntimeException(
								String.format("Try to update data in field %s  but it does not exist in object %s",
										fieldname, this.getName()));
					if (!(field instanceof IntegerDataObjectField))
						throw new RuntimeException(
								String.format("Field  %s should be decimal, but is actually %s", fieldname, field));
					IntegerDataObjectField thisintegerobjectfield = (IntegerDataObjectField) field;
					thisintegerobjectfield.setValue(thisinteger.getPayload());
					treated = true;
				}

				// treats CChoiceField

				if (thisfield instanceof ChoiceDataElt) {
					ChoiceDataElt thischoicefield = (ChoiceDataElt) thisfield;
					DataObjectElement field = this.payload.lookupSimpleFieldOnName(fieldname);
					if (field == null)
						throw new RuntimeException(
								String.format("Try to update data in field %s  but it does not exist in object %s",
										fieldname, this.getName()));
					if (!(field instanceof ChoiceDataObjectField))
						throw new RuntimeException(
								String.format("Field  %s should be choice, but is actually %s", fieldname, field));
					ChoiceDataObjectField thischoiceobjectfield = (ChoiceDataObjectField) field;
					thischoiceobjectfield.setValue(thischoicefield);
					treated = true;
				}

				// treats CChoiceField

				if (thisfield instanceof MultipleChoiceDataElt) {
					MultipleChoiceDataElt thischoicefield = (MultipleChoiceDataElt) thisfield;
					DataObjectElement field = this.payload.lookupSimpleFieldOnName(fieldname);
					if (field == null)
						throw new RuntimeException(
								String.format("Try to update data in field %s  but it does not exist in object %s",
										fieldname, this.getName()));
					if (!(field instanceof MultipleChoiceDataObjectField))
						throw new RuntimeException(String
								.format("Field  %s should be multiple choice, but is actually %s", fieldname, field));
					MultipleChoiceDataObjectField thischoiceobjectfield = (MultipleChoiceDataObjectField) field;
					thischoiceobjectfield.setValue(thischoicefield);
					treated = true;
				}

			} else {
				// ------------------------ OBJECT PROPERTY FIELDS
				// ------------------------------------
				String propertyname = thisfield.getPropertyname();
				DataObjectProperty property = this.payload.lookupPropertyOnName(propertyname);
				if (property == null)
					throw new RuntimeException(
							String.format("Field %s is referencing a property %s that is not existing in object ",
									fieldname, propertyname));
				Field field = property.getFieldBufferForGUI(thisfield.getName());
				if (field == null) {
					// field received is not in updatable from GUI
					if (property.getFieldFromName(thisfield.getName()) == null)
						throw new RuntimeException(
								String.format("Field %s does not exist in property %s ", fieldname, propertyname));
					treated = true;
				} else {

					// treats text
					if (thisfield instanceof TextDataElt) {
						TextDataElt thistextfield = (TextDataElt) thisfield;
						try {
							StoredField<String> castedstringfield = (StoredField<String>) field;
							castedstringfield.setPayload(thistextfield.getPayload());
							treated = true;
						} catch (ClassCastException e) {
							throw new RuntimeException(String.format("Excepting a text field %s in property %s, got %s",
									thistextfield.getName(), propertyname, field));
						}
					}

					// treats date

					if (thisfield instanceof DateDataElt) {
						DateDataElt thisdatefield = (DateDataElt) thisfield;

						try {
							StoredField<Date> casteddatefield = (StoredField<Date>) field;
							casteddatefield.setPayload(thisdatefield.getPayload());
							treated = true;

						} catch (ClassCastException e) {
							throw new RuntimeException(String.format("Excepting a date field %s in property %s, got %s",
									thisdatefield.getName(), propertyname, field));
						}
					}

					// treats decimal

					if (thisfield instanceof DecimalDataElt) {
						DecimalDataElt thisdecimalfield = (DecimalDataElt) thisfield;

						try {
							StoredField<BigDecimal> casteddecimalfield = (StoredField<BigDecimal>) field;
							casteddecimalfield.setPayload(thisdecimalfield.getPayload());
							treated = true;

						} catch (ClassCastException e) {
							throw new RuntimeException(
									String.format("Excepting a decimal field %s in property %s, got %s",
											thisdecimalfield.getName(), propertyname, field));
						}
					}

					// treats integer

					if (thisfield instanceof IntegerDataElt) {
						IntegerDataElt thisintegerfield = (IntegerDataElt) thisfield;

						try {
							StoredField<Integer> castedintegerfield = (StoredField<Integer>) field;
							castedintegerfield.setPayload(thisintegerfield.getPayload());
							treated = true;

						} catch (ClassCastException e) {
							throw new RuntimeException(
									String.format("Excepting an integer field %s in property %s, got %s",
											thisintegerfield.getName(), propertyname, field));
						}
					}

				}
			}
			// --------------------------------------------------------------------------------------------

			if (!treated) {
				throw new RuntimeException(String.format("Support of field %s type %s not yet implemented ", fieldname,
						thisfield.getType().printType()));
			}

		}

	}

	/**
	 * gets the list of fields as data structure
	 * 
	 * @return the list of all elements (fields and visible information from
	 *         property)
	 */
	@SuppressWarnings("unchecked")
	public NamedList<SimpleDataElt> getFieldList() {
		NamedList<SimpleDataElt> fieldlist = new NamedList<SimpleDataElt>();
		for (int i = 0; i < this.payload.getFieldNumber(); i++) {
			fieldlist.add(this.payload.getFieldAtIndex(i).getDataElement());
		}

		// write additional fields
		for (int i = 0; i < this.payload.getPropertyNumber(); i++) {
			DataObjectProperty<E> property = this.payload.getPropertyAtIndex(i);
			DataElt[] propertydisplaydata = property.getDisplayDataElt();
			for (int j = 0; j < propertydisplaydata.length; j++) {

				if (propertydisplaydata[j] instanceof SimpleDataElt)
					fieldlist.add((SimpleDataElt) (propertydisplaydata[j]));

			}
		}
		return fieldlist;
	}

	/**
	 * writes object as a message
	 * 
	 * @param writer       message writer
	 * @param hiddenfields a list of hidden fields not to show
	 * @param uid          unique id of the object
	 * @throws IOException if a problem is encountered sending the data
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void writeObjectContent(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields, String uid)
			throws IOException {
		// TODO this is not so clean. See if we should generalise and add it as a field
		if (hiddenfields == null)
			hiddenfields = new HashMap<String, NamedInterface>();
		if (uid != null) {
			writer.addStringField("UID", uid);
		} else {
			if (this instanceof UniqueidentifiedInterface) {
				UniqueidentifiedInterface uipthis = (UniqueidentifiedInterface) this;
				writer.addStringField("UID", uipthis.getId().getId());
			} else {
				writer.addStringField("UID", this.transientid);
			}
		}
		writer.addBooleanField("FRZ", this.frozen);
		writer.startStructure("FLDS");
		// write normal field
		for (int i = 0; i < this.payload.getFieldNumber(); i++) {
			if (hiddenfields.get(this.payload.getFieldAtIndex(i).getName()) == null) {
				writer.startStructure("FLD");
				DataObjectField field = this.payload.getFieldAtIndex(i);
				field.getDataElement().writeToMessage(writer);
				writer.endStructure("FLD");
			}
		}
		// write additional fields
		for (int i = 0; i < this.payload.getPropertyNumber(); i++) {
			DataObjectProperty<E> property = this.payload.getPropertyAtIndex(i);
			DataElt[] propertydisplaydata = property.getDisplayDataElt();
			for (int j = 0; j < propertydisplaydata.length; j++) {
				if (hiddenfields.get(propertydisplaydata[j].getName()) == null) {
					writer.startStructure("FLD");
					propertydisplaydata[j].writeToMessage(writer, null);
					writer.endStructure("FLD");
				}
			}
		}
		writer.endStructure("FLDS");
	}

	/**
	 * gets the object definition
	 * 
	 * @return the object definition
	 */
	public DataObjectDefinition<E> getDefinitionFromObject() {
		return this.definition;
	}

	private String transientid = null;

	/**
	 * the transient id can be used by an algorithm to store a unique transient id
	 * on an object;
	 * 
	 * @return the transient id if generated before
	 */
	public String getTransientid() {
		return this.transientid;
	}

	/**
	 * For objects that do not have a persisted id, it is possible to specify a
	 * transient id.
	 * 
	 * @param transientid a transient id guaranteed as unique in the scope of the
	 *                    algorithm using it
	 */
	public void setTransientid(String transientid) {
		this.transientid = transientid;
	}

}
