/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.designer.design;

import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.data.StringField;
import org.openlowcode.design.data.properties.basic.Companion;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkMaxOneFromLeft;
import org.openlowcode.design.data.properties.basic.ConstraintOnLinkTypeRestrictionForLeft;
import org.openlowcode.design.data.properties.basic.DisplayLinkAsAttributeFromLeftObject;
import org.openlowcode.design.data.properties.basic.HasId;
import org.openlowcode.design.data.properties.basic.LinkObject;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.Named;
import org.openlowcode.design.data.properties.basic.Numbered;
import org.openlowcode.design.data.properties.basic.NumberedForParent;
import org.openlowcode.design.data.properties.basic.Stored;
import org.openlowcode.design.data.properties.basic.StoredObject;
import org.openlowcode.design.data.properties.basic.Typed;
import org.openlowcode.design.data.properties.basic.UniqueIdentified;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * Declaration of the designer module. The designer module is an interactive
 * tool to design an Open Lowcode module. This is an alternative to writing a
 * Module class by hand (such as this one ;-) ).
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DesignerModule
		extends
		Module {

	public DesignerModule() {
		super("DESIGNER", "D0", "Open Lowcode Designer", "org.openlowcode.module.designer", "Open Lowcode SAS", "0.1",
				"Welcome to Open Lowcode designer");

		// ---------------------------------------------------------------
		// Module object definition
		// ---------------------------------------------------------------

		DataObjectDefinition module = new DataObjectDefinition("MODULEDEF", "Application Module", this);
		module.addProperties(new StoredObject());
		UniqueIdentified moduleuniqueidentified = new UniqueIdentified();
		module.addProperties(moduleuniqueidentified);
		module.addProperties(new Numbered());
		module.addProperties(new Named());
		module.addField(new StringField("CODE", "Code",
				"A unique two letters code to use as prefix for database objects", 2, StringField.INDEXTYPE_NONE));
		module.addField(new StringField("PATH", "Path", "the java path of the module", 90, StringField.INDEXTYPE_NONE));
		module.addField(new StringField("AUTHOR", "Author", "Author of the module", 90, StringField.INDEXTYPE_NONE));
		module.addField(new StringField("VERSION", "Version", "Version of the module", 90, StringField.INDEXTYPE_NONE));
		module.addField(new StringField("WELCOMEM", "Welcome Message", "Version of the module", 2550,
				StringField.INDEXTYPE_NONE));

		// ---------------------------------------------------------------
		// Data Object definition
		// ---------------------------------------------------------------

		DataObjectDefinition object = new DataObjectDefinition("DATAOBJECTDEF", "Data Object", this);
		object.addProperty(new StoredObject());
		object.addProperty(new UniqueIdentified());
		object.addProperty(new Numbered());
		object.addProperty(new Named("Label"));
		LinkedToParent<DataObjectDefinition> objectlinkedtoparentmodule = new LinkedToParent<DataObjectDefinition>(
				"PARENT", module);
		object.addProperty(objectlinkedtoparentmodule);
		object.addProperty(new NumberedForParent(objectlinkedtoparentmodule));
		object.addField(new ChoiceField("FORCEHIDE", "Force Hide",
				"Even if the object is eligible for a search screen, the search screen is hidden from the module menu",
				SystemModule.getSystemModule().getBooleanChoice(), ChoiceField.INDEXTYPE_NONE));

		// ------------------------------------------------------------------
		// Property
		//-------------------------------------------------------------------
		
		// ------------ Definition of types of properties
		
		SimpleChoiceCategory propertytype = new SimpleChoiceCategory("PROPERTYTYPE",32);
		ChoiceValue PROPERTY_STOREDOBJECT = new ChoiceValue("STOREDOBJECT","Stored Object","stores the object on the database, valid for most objects except reports");
		propertytype.addValue(PROPERTY_STOREDOBJECT);
		ChoiceValue PROPERTY_UNIQUEIDENTIFIED = new ChoiceValue("UNIQUEIDENTIFIED","Unique Identified","generates a unique id. Needed for most stored objects, except maybe some logs");
		propertytype.addValue(PROPERTY_UNIQUEIDENTIFIED);
		ChoiceValue PROPERTY_LINKEDTOPARENT = new ChoiceValue("LINKEDTOPARENT","Linked to Parent","Links to exactly one object of the parent type.");
		propertytype.addValue(PROPERTY_LINKEDTOPARENT);
		ChoiceValue PROPERTY_LINKOBJECT = new ChoiceValue("LINKOBJECT","Link Object","A property to be added .");
		propertytype.addValue(PROPERTY_LINKOBJECT);
		
		this.addChoiceCategory(propertytype);
		
		// ----------- Definition of property object
		
		DataObjectDefinition property = new DataObjectDefinition("PROPERTYDEF","Property",this);
		property.addProperty(new StoredObject());
		property.addProperty(new UniqueIdentified());
		property.addProperty(new Numbered());
		property.addProperty(new Typed(propertytype));
		property.addProperty(new LinkedToParent("PARENTFORDATAOBJECT", object));
		
		DataObjectDefinition onelinkproperty = new DataObjectDefinition("ONELINKPROPERTY","One link property",this);
		onelinkproperty.addProperty(new Stored());
		onelinkproperty.addProperty(new HasId());
		onelinkproperty.addProperty(new Companion(property, new ChoiceValue[] {PROPERTY_LINKEDTOPARENT}));
		
		DataObjectDefinition mainlink = new DataObjectDefinition("MAINLINK","Main Link",this);
		mainlink.addProperty(new StoredObject());
		mainlink.addProperty(new UniqueIdentified());
		
		LinkObject linkobjectformainlink = new LinkObject(property,object,"Main Object","Referenced as main object by");
		linkobjectformainlink.addBusinessRule(new ConstraintOnLinkTypeRestrictionForLeft(new ChoiceValue[] {PROPERTY_LINKEDTOPARENT,PROPERTY_LINKOBJECT}));
		linkobjectformainlink.addBusinessRule(new ConstraintOnLinkMaxOneFromLeft(linkobjectformainlink, true));
		linkobjectformainlink.addBusinessRule(new DisplayLinkAsAttributeFromLeftObject(true));
		mainlink.addProperty(linkobjectformainlink);
		
		DataObjectDefinition twolinksproperty = new DataObjectDefinition("TWOLINKSPROPERTY","Two link property",this);
		twolinksproperty.addProperty(new Stored());
		twolinksproperty.addProperty(new HasId());
		twolinksproperty.addProperty(new Companion(property, new ChoiceValue[] {PROPERTY_LINKOBJECT}));
		
		DataObjectDefinition secondarylink = new DataObjectDefinition("SECONDLINK","Secondary Link",this);
		secondarylink.addProperty(new StoredObject());
		secondarylink.addProperty(new UniqueIdentified());
		
		LinkObject linkobjectforsecondlink = new LinkObject(property,object,"Second Object","Referenced as second object by");
		linkobjectforsecondlink.addBusinessRule(new ConstraintOnLinkTypeRestrictionForLeft(new ChoiceValue[] {PROPERTY_LINKOBJECT}));
		linkobjectforsecondlink.addBusinessRule(new ConstraintOnLinkMaxOneFromLeft(linkobjectforsecondlink, true));
		linkobjectforsecondlink.addBusinessRule(new DisplayLinkAsAttributeFromLeftObject(true));
		secondarylink.addProperty(linkobjectforsecondlink);
	}

}
