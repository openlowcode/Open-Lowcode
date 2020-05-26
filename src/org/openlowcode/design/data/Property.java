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

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.openlowcode.design.data.properties.workflow.ObjectToAuthorityMapper;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * A property is a feature of an object that may include some stored
 * information, and some specific methods available inside business
 * transactions. Properties are identified by their name, unique across the
 * application. Different object types with the same properties can be treated
 * by the same code as all the objects will implement an interface corresponding
 * to the property.<br>
 * Property behaviour during object lifecycle<br>
 * The fields of a property are typically calculated by the property logic.
 * However, it is possible to define that some fields are entered by users
 * during the object creation or modification. In that case, a method will be
 * implemented by the property to perform the update, as it is assumed that some
 * pre-storage processing will be performed. <br>
 * Property dependencies<br>
 * Properties are typically dependent from each others. E.g. to link an object
 * to another object using a simple link, you need both objects to be uniquely
 * identified. <br>
 * <br>
 * Properties are identified by their name (which should be unique per object),
 * their classname (the same for the property whatever the context), and a
 * generics class value if the property uses a generic. This is useful for
 * properties that depend from other objects. <br>
 * e.g. ClassNameHelper<E extends generics1, F extends generics2> <br>
 * ClassNameHelper<Object1,Object2> propertynamehelper = .... <br>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public abstract class Property<E extends Property<E>>
		extends
		ObjectElement {

	private ArrayList<Property<?>> dependentproperties;
	private NamedList<DataAccessMethod> dataaccessmethods;
	private ArrayList<MethodAdditionalProcessing> methodadditionalprocessing;
	private NamedList<PropertyGenerics> dependentgenerics; // generics in the sense of java 1.5
	private NamedList<DisplayProfile> profilesforproperty;
	private ArrayList<PropertyBusinessRule<E>> businessrules;
	private ArrayList<FieldOverrideForProperty> fieldoverrides;
	private boolean setdatainputforupdate;
	private boolean hidedateinputforcreation;
	private boolean datainputatbottom;
	private boolean hasdynamicdefinitionhelper;

	/**
	 * @return true if data input is at bottom for property field
	 */
	public boolean isDatainputatbottom() {
		return datainputatbottom;
	}

	/**
	 * allows to set if data input for the property during creation page is at top
	 * or bottom of objects
	 * 
	 * @param datainputatbottom true if data input is at bottom
	 */
	public void setDatainputatbottom(boolean datainputatbottom) {
		this.datainputatbottom = datainputatbottom;
	}

	private ArrayList<DataObjectDefinition> externalobjectlist;
	private ArrayList<Property<?>> externalobjectpropertylist;

	private String extraattributes = null;

	/**
	 * This allows a property to add specific attributes to pass to the property
	 * definition when creating it. It should be rendered as a string, including
	 * preceding comma, e.g. ",2,3" for two additional integer attributes.
	 * 
	 * @param extraattributes extra attributes for property creation
	 */
	protected void setExtraAttributes(String extraattributes) {
		this.extraattributes = extraattributes;
	}

	/**
	 * adds an external object property to this property. Checks are done so that
	 * properties are only added once per type of external object
	 * 
	 * @param externalobject         other object
	 * @param externalobjectproperty property of the other object that this property
	 *                               is related to
	 */
	protected void addExternalObjectProperty(DataObjectDefinition externalobject, Property<?> externalobjectproperty) {
		boolean exists = false;
		for (int i = 0; i < externalobjectlist.size(); i++) {
			DataObjectDefinition previousobject = externalobjectlist.get(i);
			Property<?> previousproperty = externalobjectpropertylist.get(i);
			boolean thisissimilar = true;
			if (!previousobject.equals(externalobject))
				thisissimilar = false;
			if (!previousproperty.equals(externalobjectproperty))
				thisissimilar = false;
			if (thisissimilar)
				exists = true;
		}
		if (!exists) {
			externalobjectlist.add(externalobject);
			externalobjectpropertylist.add(externalobjectproperty);
		}
	}

	/**
	 * This method allows a property to add another property on the same object.
	 * This is authorized only when the property is on the same objet
	 * 
	 * @param propertyonsameobject the property to add on same object
	 */
	protected void addPropertyOnSameObject(Property<?> propertyonsameobject) {
		this.parent.addProperty(propertyonsameobject);
	}

	/**
	 * @return the number of external object properties
	 */
	public int getExternalObjectPropertySize() {
		return externalobjectlist.size();
	}

	/**
	 * returns an external object for which this property has reference
	 * 
	 * @param index an integer between 0 (included) and
	 *              getExternalObjectPropertySize() (excluded)
	 * @return the data object at the given index
	 */
	public DataObjectDefinition getExternalObject(int index) {
		return externalobjectlist.get(index);
	}

	/**
	 * the related property for the external object at the given index
	 * 
	 * @param index an integer between 0 (included) and
	 *              getExternalObjectPropertySize() (excluded)
	 * @return the external object property at the given index
	 */
	public Property<?> getExternalObjectProperty(int index) {
		return externalobjectpropertylist.get(index);
	}

	/**
	 * @return the number of business rules
	 */
	public int getBusinessRuleNumber() {
		return businessrules.size();
	}

	/**
	 * gets the business rule at the given index
	 * 
	 * @param index a number between 0 (included) and getBusinessRuleNumber
	 *              (excluded)
	 * @return the business rule at the given index
	 */
	public PropertyBusinessRule<E> getBusinessRule(int index) {
		return businessrules.get(index);
	}

	/**
	 * queries the business rule by name, return null if no corresponding business
	 * rule is found
	 * 
	 * @param name name of the business rule
	 * @return the property with the given name if exists, null else
	 */
	public PropertyBusinessRule<E> getBusinessRuleByName(String name) {

		for (int i = 0; i < businessrules.size(); i++) {
			PropertyBusinessRule<E> thisrule = businessrules.get(i);
			if (thisrule.getName().compareTo(name) == 0)
				return thisrule;
		}
		return null;
	}

	private NamedList<ArgumentContent> contextdataforcreation;

	private NamedList<ArgumentContent> datainputforpage;

	/**
	 * this method uses the context data for creation and the data-input for
	 * standard creation. This does not include automatically generated data, but
	 * data entered by user and context only.
	 * 
	 * @return a non-null array of size 0 to n with the method and arguments
	 *         required
	 */
	public abstract String[] getPropertyInitMethod();

	/**
	 * This method should return non null value only if
	 * setCreationDataInputForUpdate() is set
	 * 
	 * @return a non-null array of size 0 to n with the method and arguments
	 *         required
	 */
	public abstract String[] getPropertyExtractMethod();

	/**
	 * Returns the required deepcopy statement for the property, sends back null if
	 * nothing is required.
	 * 
	 * @return a number of lines of java code
	 */
	public abstract String[] getPropertyDeepCopyStatement();

	/**
	 * gets the number of context data for creation of property. Context data is
	 * provided by the creation page
	 * 
	 * @return the number of context data
	 */
	public int getContextDataForCreationSize() {
		return contextdataforcreation.getSize();
	}

	/**
	 * gets the type of context data for creation of an object with this property
	 * 
	 * @param index an integer between 0 (included) and getDataInputSize (excluded)
	 * @return the context data at the given index
	 */
	public ArgumentContent getContextDataForCreation(int index) {
		return contextdataforcreation.get(index);
	}

	/**
	 * @return the number of data input fields for object creation
	 */
	public int getDataInputSize() {
		return datainputforpage.getSize();
	}

	/**
	 * gets the type of data input (argument content) for creation of an object with
	 * this property
	 * 
	 * @param index an integer between 0 (included) and getDataInputSize (excluded)
	 * @return the argument content at the given index
	 */
	public ArgumentContent getDataInputForCreation(int index) {
		return datainputforpage.get(index);
	}

	/**
	 * context for data creation is provided by the context. This will be the data
	 * passed to the object creation page
	 * 
	 * @param contextdata
	 * @param optional    true if the context data is not obliged to be provided
	 */
	protected void addContextForDataCreation(ArgumentContent contextdata) {
		contextdataforcreation.add(contextdata);
		this.setdatainputforupdate = false;
		this.hidedateinputforcreation = false;
		this.datainputatbottom = false;
	}

	/**
	 * data input argument can be shown on create (default) but also on update. This
	 * argument tells if the data input should be used on update page also
	 * 
	 * @return true if data input argument(s) should be put on update page also
	 */
	public boolean isDataInputUsedForUpdate() {
		return this.setdatainputforupdate;
	}

	/**
	 * an indicator telling if data input should be hidden for creation (and so
	 * shown on update page only)
	 * 
	 * @return true if data input should be hidden for creation
	 */
	public boolean isDataInputHiddenForCreation() {
		return this.hidedateinputforcreation;
	}

	/**
	 * will also use the data input for creation for the update of object
	 */
	protected void setDataInputForUpdate() {
		this.setdatainputforupdate = true;
	}

	/**
	 * will use the data input only for update
	 */
	protected void hideDataInputForCreation() {
		this.hidedateinputforcreation = true;
	}

	/**
	 * sets data input (by default for creation). This is typically entered by the
	 * user during the prepare creation page.
	 * 
	 * @param datainput the data input.
	 */
	protected void addDataInput(ArgumentContent datainput) {
		datainputforpage.add(datainput);
	}

	/**
	 * list of choice categories that the property uses
	 */
	private HashMap<String, ChoiceCategory> relevantchoicecategories;
	private HashMap<String, String> relevantstringhelper = new HashMap<String, String>();

	/**
	 * @return a list of data object definitions that are linked to this property.
	 *         This helps making necessary imports in the data object definition
	 *         file
	 */
	public abstract ArrayList<DataObjectDefinition> getExternalObjectDependence();

	/**
	 * provides the number of choice categories used by this property. A choice
	 * category will 'type' the property with java generics
	 * 
	 * @return the number of choice categories
	 */
	public int getChoiceCategoryNumber() {
		return relevantchoicecategories.size();
	}

	/**
	 * An iterator on choice categories used by this property. A choice category
	 * will 'type' the property with java generics
	 * 
	 * @return iterator on choice categories
	 */
	public Iterator<String> getChoiceCategoryKeyNumber() {
		return relevantchoicecategories.keySet().iterator();
	}

	/**
	 * gets the choice category by the given key
	 * 
	 * @param key key provided by method getChoiceCategoryKeyNumber
	 * @return the corresponding choice category if exists, null else
	 */
	public ChoiceCategory getChoiceCategoryByKey(String key) {
		return relevantchoicecategories.get(key);
	}

	private String propertyclassname;
	private String instancename;

	/**
	 * @return instance name, null if no specific instance name
	 */
	public String getInstancename() {
		return instancename;
	}

	/**
	 * adds a choice category helper. THis means the property will have a generics
	 * linked to this choice category. E.g. The lifecycle property is defined by the
	 * choice of lifecycle states
	 * 
	 * @param name     name of the choice category
	 * @param category choice category.
	 */
	protected void addChoiceCategoryHelper(String name, ChoiceCategory category) {
		if (category == null)
			throw new RuntimeException("category should not be null for property = '" + this.getName()
					+ "', helper name = '" + name + "'");
		String cleanedkey = Named.cleanName(name);
		this.relevantchoicecategories.put(cleanedkey, category);
		this.relevantchoiceskeyset.add(cleanedkey);
	}

	/**
	 * A string helper is a string input defined when defining the property on a
	 * given object. It is used in Lifecycle to define a 'non-released' label that
	 * will be shown on a draft object
	 * 
	 * @param name    name of the string helper
	 * @param payload default payload in the string helper
	 */
	protected void addStringHelper(String name, String payload) {
		String cleanedkey = Named.cleanName(name);
		this.relevantstringhelper.put(cleanedkey, payload);
		this.relevantstringhelperkeyset.add(cleanedkey);
	}

	/**
	 * adds
	 * 
	 * @param name
	 * @param mapper
	 */
	protected void addObjectToAuthorityMapper(String name, ObjectToAuthorityMapper mapper) {
		if (mapper == null)
			throw new RuntimeException(
					"mapper should not be null for property = '" + this.getName() + ", helper name = '" + name + "'");
	}

	/**
	 * only properties that exist with only one instance can have an interface and
	 * so be referenced as significant property for the PropertyGenerics of
	 * anotherproperty
	 * 
	 * @return true if the property will have an interface
	 */
	public boolean hasInterface() {
		if (this.getName().compareTo(this.propertyclassname) == 0) {
			return true;
		}
		return false;
	}

	/**
	 * the property class name (different for the instance name in case the same
	 * property has several instances)
	 * 
	 * @return the property class name
	 */
	public String getPropertyclassname() {
		return propertyclassname;
	}

	/**
	 * This defines a relation between the current property and the dependent
	 * property. This authorizes the following:
	 * <ul>
	 * <li>the current property can complete a method of the dependent property by
	 * adding pre or post treatment, through declaring a MethodAdditionalProcessing
	 * </li>
	 * <li>the current property can access to data of the dependent property. This
	 * is offered through a reference to the dependent property inside the current
	 * property</li>
	 * <li>a method of the current property can call a method of the dependent
	 * property</li>
	 * </ul>
	 * 
	 * @param property the dependent property
	 */
	protected void addDependentProperty(Property<?> property) {
		dependentproperties.add(property);
	}

	/**
	 * a data access method is a method defined by the property that will be
	 * available to the object for a business action
	 * 
	 * @param dam data access method
	 */
	protected void addDataAccessMethod(DataAccessMethod dam) {
		this.dataaccessmethods.add(dam);
	}

	/**
	 * adds a method additional processing to a related property
	 * 
	 * @param map method additional processing
	 */
	protected void addMethodAdditionalProcessing(MethodAdditionalProcessing map) {
		this.methodadditionalprocessing.add(map);
	}

	protected DataObjectDefinition parent;
	private ArrayList<String> relevantchoiceskeyset;
	private ArrayList<String> relevantstringhelperkeyset;

	/**
	 * @return the number of data access methods
	 */
	public int getDataAccessMethodnumber() {
		return dataaccessmethods.getSize();
	}

	/**
	 * get the data access method at the provided index
	 * 
	 * @param index a number between 0 (included) and getDataAccessMethodNumber
	 *              (excluded)
	 * @return
	 */
	public DataAccessMethod getDataAccessMethod(int index) {
		return dataaccessmethods.get(index);
	}

	/**
	 * @return the number of method additional processings
	 */
	public int getMethodAdditionalProcessingnumber() {
		return methodadditionalprocessing.size();
	}

	/**
	 * get the method additional processing at the given index
	 * 
	 * @param index a number between 0 (included) and
	 *              getMethodAdditionalProcessingnumber (excluded)
	 * @return
	 */
	public MethodAdditionalProcessing getMethodAdditionalProcessing(int index) {
		return methodadditionalprocessing.get(index);
	}

	/**
	 * get data access method for the given name
	 * 
	 * @param name name of the data access method
	 * @return the data access method if it exists
	 */
	public DataAccessMethod getDataAccessMethod(String name) {
		return dataaccessmethods.lookupOnName(name);

	}

	/**
	 * @param name              the name of the instance of the property (e.g.
	 *                          Folder Structure, the specific instance of
	 *                          "linkedtoparent" property)
	 * 
	 * @param propertyclassname name of the class of the property (e.g.
	 *                          linkedtoparent). In case propertyclassname is
	 *                          provided, the propertyname is
	 *                          'propertclassname'+"for"+'name' (e.g.
	 *                          'linkedtoparentforfolderstructure' )
	 */
	public Property(String name, String propertyclassname) {
		super(propertyclassname + "for" + name, "", "");
		this.hasdynamicdefinitionhelper = false;

		this.propertyclassname = propertyclassname;
		this.instancename = name;
		this.dependentgenerics = new NamedList<PropertyGenerics>();
		dependentproperties = new ArrayList<Property<?>>();
		dataaccessmethods = new NamedList<DataAccessMethod>();
		methodadditionalprocessing = new ArrayList<MethodAdditionalProcessing>();
		relevantchoicecategories = new HashMap<String, ChoiceCategory>();
		relevantstringhelper = new HashMap<String, String>();
		relevantchoiceskeyset = new ArrayList<String>();
		relevantstringhelperkeyset = new ArrayList<String>();
		contextdataforcreation = new NamedList<ArgumentContent>();
		datainputforpage = new NamedList<ArgumentContent>();
		businessrules = new ArrayList<PropertyBusinessRule<E>>();
		externalobjectlist = new ArrayList<DataObjectDefinition>();
		externalobjectpropertylist = new ArrayList<Property<?>>();
		this.profilesforproperty = new NamedList<DisplayProfile>();
		fieldoverrides = new ArrayList<FieldOverrideForProperty>();
	}

	/**
	 * creates a property for the given name
	 * 
	 * @param name the name of the instance of the property on the object (if the
	 *             name depends from the instance, then the common classname should
	 *             be specified
	 */
	public Property(String name) {
		super(name, "", "");
		this.hasdynamicdefinitionhelper = false;
		this.propertyclassname = name;
		this.instancename = null;
		this.dependentgenerics = new NamedList<PropertyGenerics>();
		this.profilesforproperty = new NamedList<DisplayProfile>();
		dependentproperties = new ArrayList<Property<?>>();
		dataaccessmethods = new NamedList<DataAccessMethod>();
		methodadditionalprocessing = new ArrayList<MethodAdditionalProcessing>();
		relevantchoicecategories = new HashMap<String, ChoiceCategory>();
		relevantstringhelper = new HashMap<String, String>();
		relevantchoiceskeyset = new ArrayList<String>();
		relevantstringhelperkeyset = new ArrayList<String>();
		contextdataforcreation = new NamedList<ArgumentContent>();
		datainputforpage = new NamedList<ArgumentContent>();
		businessrules = new ArrayList<PropertyBusinessRule<E>>();
		externalobjectlist = new ArrayList<DataObjectDefinition>();
		externalobjectpropertylist = new ArrayList<Property<?>>();
		fieldoverrides = new ArrayList<FieldOverrideForProperty>();
		this.setdatainputforupdate = false;
	}

	/**
	 * sets the parent data object for the property. This is done automatically when
	 * the property is added to the DataObjectDefinition
	 * 
	 * @param parent
	 */
	void setParent(DataObjectDefinition parent) {
		if (this.parent != null)
			throw new RuntimeException("Requesting to add property " + this.getClass().getName() + " " + this.getName()
					+ " to " + parent.getName() + " but parent is already set: ");
		this.parent = parent;
		controlAfterParentDefinition();

	}

	/**
	 * get the parent DataObjectDefinition for the property
	 * 
	 * @return the parent DataObjectDefinition
	 */
	public DataObjectDefinition getParent() {
		return this.parent;
	}

	/**
	 * adds a display profile for this property. This allows, depending on context,
	 * to show different fields
	 * 
	 * @param displayprofile display profile to add
	 */
	public void addDisplayProfileForProperty(DisplayProfile displayprofile) {
		this.profilesforproperty.add(displayprofile);
	}

	/**
	 * gets the number of display profiles defined for this property
	 * 
	 * @return number of display profiles
	 */
	public int getDisplayProfileForPropertyNumber() {
		return this.profilesforproperty.getSize();
	}

	/**
	 * gets the display profile at the given index
	 * 
	 * @param index a number between 0 (included) and
	 *              getDisplayProfileForPropertyNumber (excluded)
	 * @return the diplsay profile at the given index
	 */
	public DisplayProfile getDisplayProfileForProperty(int index) {
		return this.profilesforproperty.get(index);
	}

	/**
	 * @param businessrule
	 */
	public void addBusinessRule(PropertyBusinessRule<E> businessrule) {
		businessrules.add(businessrule);
	}

	/**
	 * When a property has a dynamic definition helper, the definition helper has to
	 * be defined for each instance of the object.
	 */
	public void setDynamicDefinitionHelper() {
		this.hasdynamicdefinitionhelper = true;
	}

	/**
	 * @return true if the property has a dynamic definition helper
	 */
	public boolean hasDynamicDefinitionHelper() {
		return this.hasdynamicdefinitionhelper;
	}

	/**
	 * to be implemented if controls have to be performed after parent is added.
	 */
	public void controlAfterParentDefinition() {

	}

	/**
	 * get the number of field overrides defined for the object
	 * 
	 * @return number of field overrides
	 */
	public int getFieldOverridesNumber() {
		return fieldoverrides.size();
	}

	/**
	 * gets the field override at the given index
	 * 
	 * @param index a number between 0 (included) and getFieldOverridesNumber
	 *              (excluded)
	 * @return the field override for the given index
	 */
	public FieldOverrideForProperty getFieldOverridesat(int index) {
		return fieldoverrides.get(index);
	}

	/**
	 * adds a Field Overrides for the given property. It allows to change the label
	 * and priority of a field
	 * 
	 * @param override a field overrides to add
	 */
	protected void addFieldOverrides(FieldOverrideForProperty override) {
		fieldoverrides.add(override);
	}

	/**
	 * @return true if the property has a static query amongst its methods
	 */
	public boolean hasStaticQuery() {
		for (int i = 0; i < this.dataaccessmethods.getSize(); i++) {
			DataAccessMethod thismethod = this.dataaccessmethods.get(i);
			if (thismethod.isStatic())
				return true;
		}
		return false;
	}

	/**
	 * adds a generics to the property. A generics is a reference to another object
	 * type than the parent data object
	 * 
	 * @param generics the generics to add
	 */
	public void addPropertyGenerics(PropertyGenerics generics) {
		this.dependentgenerics.add(generics);
	}

	/**
	 * this method should returns all properties this property is dependent on.
	 * 
	 * @return
	 */
	public Property<?>[] getDependentProperties() {
		return dependentproperties.toArray(new Property[0]);
	}

	@Override
	public String getDataObjectFieldName() {
		return "NOT IMPLEMENTED";
	}

	@Override
	public String getDataObjectConstructorAttributes() {
		if (this.propertyclassname.compareTo(this.getName()) != 0) {
			// complex property
			StringBuffer returnvalue = new StringBuffer("this,");
			returnvalue.append('"');
			returnvalue.append(this.getName());
			returnvalue.append('"');
			for (int i = 0; i < this.getPropertyGenericsSize(); i++) {
				returnvalue.append(',');
				PropertyGenerics thisgenerics = this.getPropertyGenerics(i);

				returnvalue.append(StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName()));
				returnvalue.append("Definition.get");
				returnvalue.append(StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName()));
				returnvalue.append("Definition()");
			}
			if (this.getPropertyHelperName() != null)
				if (!this.isPropertyHelperTransient()) {
					returnvalue.append(",");
					returnvalue.append(this.getPropertyHelperName());
					returnvalue.append(".get()");
				}
			if (this.getPropertyHelperName() != null)
				if (this.isPropertyHelperTransient()) {
					returnvalue.append(",() ->");
					returnvalue.append(this.getPropertyHelperName());
					returnvalue.append(".get()");

				}

			if (this.extraattributes != null)
				returnvalue.append(this.extraattributes);
			return returnvalue.toString();
		} else {
			StringBuffer returnvalue = new StringBuffer("this");
			for (int i = 0; i < this.getPropertyGenericsSize(); i++) {
				returnvalue.append(',');
				PropertyGenerics thisgenerics = this.getPropertyGenerics(i);

				returnvalue.append(StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName()));
				returnvalue.append("Definition.get");
				returnvalue.append(StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName()));
				returnvalue.append("Definition()");
			}
			// ****** add choice categories *****
			for (int i = 0; i < this.relevantchoiceskeyset.size(); i++) {
				String key = relevantchoiceskeyset.get(i);
				ChoiceCategory choice = relevantchoicecategories.get(key);
				returnvalue.append(",");
				returnvalue.append(StringFormatter.formatForJavaClass(choice.getName()) + "ChoiceDefinition.get()");
			}

			for (int i = 0; i < this.relevantstringhelperkeyset.size(); i++) {
				String key = this.relevantstringhelperkeyset.get(i);
				String payload = this.relevantstringhelper.get(key);
				returnvalue.append(",");
				returnvalue
						.append(payload == null ? "null" : "\"" + StringFormatter.escapeforjavastring(payload) + "\"");
			}

			if (this.getPropertyHelperName() != null) {
				returnvalue.append(",");
				returnvalue.append(this.getPropertyHelperName());
				returnvalue.append(".get()");
			}
			if (this.extraattributes != null)
				returnvalue.append(this.extraattributes);
			return returnvalue.toString();
		}
	}

	/**
	 * @return true if the property has a transient property helper
	 */
	public boolean isPropertyHelperTransient() {

		return false;
	}

	/**
	 * @return the java class name of the property
	 */
	public String getJavaClassType() {
		return StringFormatter.formatForJavaClass(this.getName());
	}

	/**
	 * @return the number of generics of this objec
	 */
	public int getPropertyGenericsSize() {
		return this.dependentgenerics.getSize();
	}

	/**
	 * @return the list of generics for the class declaration
	 */
	public String getPropertyGenericsString() {
		StringBuffer genericsstring = new StringBuffer("");
		genericsstring.append(StringFormatter.formatForJavaClass(this.getParent().getName()));
		for (int j = 0; j < this.getPropertyGenericsSize(); j++) {
			PropertyGenerics thisgenerics = this.getPropertyGenerics(j);
			genericsstring.append(',');
			genericsstring.append(StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName()));
		}
		Iterator<String> choicekey = this.getChoiceCategoryKeyNumber();
		while (choicekey.hasNext()) {
			genericsstring.append(',');
			genericsstring
					.append(StringFormatter.formatForJavaClass(this.getChoiceCategoryByKey(choicekey.next()).getName())
							+ "ChoiceDefinition");
		}

		return genericsstring.toString();
	}

	/**
	 * gest the property generics at the given index
	 * 
	 * @param index a number between 0 (included) and getPropertyGenericsSize
	 *              (excluded)
	 * @return the property generics at the given index
	 */
	public PropertyGenerics getPropertyGenerics(int index) {
		return this.dependentgenerics.get(index);
	}

	/**
	 * adds a stored element to the property without any display or update setting
	 * 
	 * @param e stored element
	 */
	public void addElement(Element e) {
		super.addElement(e);
	}

	/**
	 * /** adds a stored element with display of the field specified
	 * 
	 * @param e                    element to store
	 * @param display              display label
	 * @param tooltip              tooltip for roll-over mouse
	 * @param fielddisplaymode     the type of display as defined in the class
	 *                             PropertyElementDisplayDefinition
	 * @param fielddisplaypriority priority display
	 * @param showcaractersintable number of characters of width of the column
	 */
	public void addElement(
			Element e,
			String display,
			String tooltip,
			int fielddisplaymode,
			int fielddisplaypriority,
			int showcaractersintable) {
		this.addElement(e);
		this.elementdisplayinformation.put(e, new PropertyElementDisplayDefinition(display, tooltip, fielddisplaymode,
				fielddisplaypriority, showcaractersintable));
	}

	/**
	 * 
	 * adds a stored element with display of the field specified and the field
	 * appearing in field searches
	 * 
	 * @param e                    element to store
	 * @param display              display label
	 * @param tooltip              tooltip for roll-over mouse
	 * @param fielddisplaymode     the type of display as defined in this class
	 * @param fielddisplaypriority priority display
	 * @param showcaractersintable number of characters of width of the column
	 * @param searchdef            definition of the search widget
	 */
	public void addElementasSearchElement(
			Element e,
			String display,
			String tooltip,
			int fielddisplaymode,
			int fielddisplaypriority,
			int showcaractersintable,
			SearchWidgetDefinition searchdef) {
		super.AddElementWithSearch(e, searchdef);
		this.elementdisplayinformation.put(e, new PropertyElementDisplayDefinition(display, tooltip, fielddisplaymode,
				fielddisplaypriority, showcaractersintable));
	}

	private HashMap<Element, PropertyElementDisplayDefinition> elementdisplayinformation = new HashMap<
			Element, PropertyElementDisplayDefinition>();

	/**
	 * gets the display definition for a given element of the property
	 * 
	 * @param e element
	 * @return the display information if it exists
	 */
	public PropertyElementDisplayDefinition getDisplayDefinitionForElement(Element e) {
		return elementdisplayinformation.get(e);
	}

	/**
	 * A simple classe to define how to display a property element definition
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class PropertyElementDisplayDefinition {
		private String tooltip;
		private String display;
		private int fielddisplayMode;
		private int fielddisplaypriority;
		private int showcaractersintable;

		/**
		 * @return the field display priority, an integer between 1000 and -1000
		 */
		public int getFielddisplaypriority() {
			return fielddisplaypriority;
		}

		/**
		 * @return the width of columns in table number of characters
		 */
		public int getShowcaractersintable() {
			return showcaractersintable;
		}

		/**
		 * @return the tooltup for mouse roll-over
		 */
		public String getTooltip() {
			return tooltip;
		}

		/**
		 * @return the display label of the field
		 */
		public String getDisplay() {
			return display;
		}

		/**
		 * @return the display mode of the field
		 */
		public int getFielddisplayMode() {
			return fielddisplayMode;
		}

		/**
		 * Defines the way a property element will be shown
		 * 
		 * @param tooltip              a tooltip that will display when the mouse roll
		 *                             overs the field
		 * @param display              the label this field will have
		 * @param fielddisplayMode     a field display mode (only values defined in this
		 *                             class are allowed):
		 *                             <ul>
		 *                             <li>FIELDDISPLAY_NONE: field is not shown</li>
		 *                             <li>FIELDDISPLAY_BOTTOMNOTES: Field is only shown
		 *                             in bottom notes</li>
		 *                             <li>FIELDDISPLAY_NORMAL: field is shown in main
		 *                             field display of object</li>
		 *                             <li>FIELDDISPLAY_NORMAL_MOD: field is shown in
		 *                             main field display of object and is modifiable
		 *                             </li>
		 *                             <li>FIELDDISPLAY_TITLE: field is shown in main
		 *                             field display of object and title</li>
		 *                             <li>FIELDDIPLSAY_TITLE_MOD: Field is shown in
		 *                             main field display of object and title and is
		 *                             modifiable</li>
		 *                             </ul>
		 * @param fielddisplaypriority an integer between -1000 and 1000.
		 * @param showcaractersintable an integer between 0 and 200 telling the prefered
		 *                             number of characters to show for the column @
		 */
		public PropertyElementDisplayDefinition(
				String tooltip,
				String display,
				int fielddisplayMode,
				int fielddisplaypriority,
				int showcaractersintable) {
			super();
			this.tooltip = tooltip;
			this.display = display;
			if (fielddisplaypriority > 1000)
				throw new RuntimeException("Field display priority should be less than or equal to 1000");
			if (fielddisplaypriority < -1000)
				throw new RuntimeException("Field display priority should be less than or equal to 1000");

			this.fielddisplaypriority = fielddisplaypriority;
			if (showcaractersintable < 0)
				throw new RuntimeException("showcaractersintable should be greater than 0");
			if (showcaractersintable > 200)
				throw new RuntimeException("showcaractersintable should be less than 200");

			this.showcaractersintable = showcaractersintable;
			boolean supportedfielddisplayMode = false;
			if (fielddisplayMode == FIELDDISPLAY_NONE)
				supportedfielddisplayMode = true;
			if (fielddisplayMode == FIELDDISPLAY_BOTTOMNOTES)
				supportedfielddisplayMode = true;
			if (fielddisplayMode == FIELDDISPLAY_NORMAL)
				supportedfielddisplayMode = true;
			if (fielddisplayMode == FIELDDISPLAY_NORMAL_MOD)
				supportedfielddisplayMode = true;
			if (fielddisplayMode == FIELDDISPLAY_TITLE)
				supportedfielddisplayMode = true;
			if (fielddisplayMode == FIELDDIPLSAY_TITLE_MOD)
				supportedfielddisplayMode = true;
			if (!supportedfielddisplayMode)
				throw new RuntimeException("invalid value of displaymode : " + fielddisplayMode
						+ ", for field with tooltip = " + tooltip + ", display = " + display);
			this.fielddisplayMode = fielddisplayMode;

		}

	}

	public static final int FIELDDISPLAY_NONE = 0; // field is not shown
	public static final int FIELDDISPLAY_BOTTOMNOTES = 1; // Field is only shown in bottom notes
	public static final int FIELDDISPLAY_NORMAL = 2; // field is shown in main field display of object
	public static final int FIELDDISPLAY_NORMAL_MOD = 3; // field is shown in main field display of object and is
															// modifiable
	public static final int FIELDDISPLAY_TITLE = 4; // field is shown in main field display of object and title;
	public static final int FIELDDIPLSAY_TITLE_MOD = 5; // Field is shown in main field display of object and title and
														// is modifiable

	/**
	 * an abstract method to be implemented by properties for finalizations to be
	 * performed after all data objects have been created. THis is especially useful
	 * for properties that refer to other data objects (e.g. LinkProperty )
	 */
	public abstract void setFinalSettings();

	/**
	 * @return true if the property is legacy, meaning it is still present to
	 *         perform a migration, but not shown. Business logic is deactivated,
	 *         but data is still accessible.
	 */
	public boolean isLegacy() {

		return legacy;
	}

	private boolean legacy = false;

	/**
	 * sets this property as legacy
	 */
	protected void setAsLegacy() {
		this.legacy = true;
	}

	/**
	 * A property can have a specific property helper. It is generated in the data
	 * folder, and is also called at the end of the property definition creator,
	 * with the singleton .get method
	 * 
	 * @return null if no specific helper, or the helper class name if helper exists
	 * 
	 */
	public String getPropertyHelperName() {
		return null;
	}

	/**
	 * Generates in the data folder the property helper.
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if any error is encountered writing the source file
	 */
	public void generatePropertyHelperToFile(SourceGenerator sg, Module module) throws IOException {
		throw new RuntimeException("Not implemented");
	}

	/**
	 * This method allows a property to define extra conditions when writing
	 * additional definition on the property
	 * 
	 * @param sg source generator
	 * @throws IOException if any error is encountered writing the source file
	 */
	public void writeAdditionalDefinition(SourceGenerator sg) throws IOException {

	}
}
