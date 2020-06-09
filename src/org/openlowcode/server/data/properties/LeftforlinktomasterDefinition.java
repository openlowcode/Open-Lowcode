/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.util.ArrayList;
import java.util.logging.Logger;

import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DataObjectElement;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectPropertyDefinition;
import org.openlowcode.server.data.PropertyExtractor;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn;
import org.openlowcode.server.data.properties.constraints.ConstraintOnLinkToMaster;
import org.openlowcode.server.data.specificstorage.ExternalFieldSchema;
import org.openlowcode.server.data.storage.QueryCondition;
import org.openlowcode.tools.trace.ExceptionLogger;

/**
 * the property that is put on the left object for a link to master
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> current object (left for link)
 * @param <F> data object holding the link
 * @param <G> right data object for link
 */
public class LeftforlinktomasterDefinition <E extends DataObject<E> & UniqueidentifiedInterface<E>, F extends DataObject<F> & LinkobjecttomasterInterface<F, E, G>, G extends DataObject<G> & VersionedInterface<G>>
extends DataObjectPropertyDefinition<E> {
private static Logger logger = Logger.getLogger(LeftforlinkDefinition.class.getName());
private DataObjectDefinition<F> linkobjectdefinition;
private LinkobjecttomasterDefinition<F, E, G> linkobjecttomasterpropertydefinition;
private DataObjectDefinition<E> parentobject;
private DataObjectDefinition<G> rightobjectforlinkdefinition;
private UniqueidentifiedDefinition<E> uniqueidentifieddefinition;

@SuppressWarnings("unused")
private VersionedDefinition<G> versionedforrightobject;

@Override
public DataObjectDefinition<E> getParentObject() {
return this.parentobject;
}

/**
* creates the definition of the data object link
* 
* @param parentobject                 parent data object definition
* @param name                         name of the link
* @param linkobjectdefinition         definition of the data object for the
*                                     link
* @param rightobjectforlinkdefinition definition of the right data object for
*                                     the link
*/
public LeftforlinktomasterDefinition(DataObjectDefinition<E> parentobject, String name,
	DataObjectDefinition<F> linkobjectdefinition, DataObjectDefinition<G> rightobjectforlinkdefinition) {
super(parentobject, name);
this.linkobjectdefinition = linkobjectdefinition;
this.parentobject = parentobject;
this.rightobjectforlinkdefinition = rightobjectforlinkdefinition;
}

/**
* get the link object property definition for the linked object
* 
* @return the link object property
*/
public LinkobjecttomasterDefinition<F, E, G> getLinkObjectPropertyDefinition() {
return this.linkobjecttomasterpropertydefinition;
}

/**
* sets the link object property on the data object holding the link
* 
* @param linkobjecttomasterpropertydefinition
*/
public void setGenericsLinkobjecttomasterProperty(LinkobjecttomasterDefinition<F, E, G> linkobjecttomasterpropertydefinition) {
this.linkobjecttomasterpropertydefinition = linkobjecttomasterpropertydefinition;

}

/**
* gets the unique identified property definition on the data object on the left
* for the link
* 
* @return the unique identified property
*/
public UniqueidentifiedDefinition<E> getUniqueIdentifiedDefinition() {
return this.uniqueidentifieddefinition;
}

/**
* sets the dependent property UniqueIdentified
* 
* @param uniqueidentifieddefinition dependent property unique identified
*/
public void setDependentDefinitionUniqueidentified(UniqueidentifiedDefinition<E> uniqueidentifieddefinition) {
this.uniqueidentifieddefinition = uniqueidentifieddefinition;

}

/**
* sets the unique identified property for the link
* 
* @param versionedforrightobject versioned property for the
*                                       right object for the link
*/
public void setGenericsRightobjectforlinktomasterProperty(VersionedDefinition<G> versionedforrightobject) {
this.versionedforrightobject = versionedforrightobject;

}

@Override
public ArrayList<ExternalFieldSchema<?>> generateExternalSchema() {
return null;
}

@Override
public FieldSchemaForDisplay<E>[] setFieldSchemaToDisplay() {
return null;
}

@SuppressWarnings({ "unchecked", "rawtypes" })
@Override
public DataObjectElement initiateFieldInstance(DataObjectPayload parentpayload) {
return new Leftforlinktomaster<E, F, G>(this, parentpayload);
}

@Override
public QueryCondition getUniversalQueryCondition(String alias) {
return null;
}

@SuppressWarnings({ "rawtypes", "unchecked" })
@Override
public FlatFileLoaderColumn<E> getFlatFileLoaderColumn(DataObjectDefinition<E> objectdefinition,
	String[] columnattributes, PropertyExtractor<E> propertyextractor,
	ChoiceValue<ApplocaleChoiceDefinition> locale) {
boolean createmissing = false;
boolean deletelink = false;
String hardcodedvalue = null;
if (columnattributes != null)
	if (columnattributes.length > 0)
		for (int i = 0; i < columnattributes.length; i++) {
			if ("CREATE".equals(columnattributes[i]))
				createmissing = true;
			if ("DELETELINK".equals(columnattributes[i]))
				deletelink = true;
			if (columnattributes[i].startsWith("HARDCODEDVALUE=")) {
				String value = columnattributes[i].substring("HARDCODEDVALUE=".length());
				if (value.trim().length() > 0)
					hardcodedvalue = value.trim();
			}
		}

DataObjectPropertyDefinition<G> rightobjectnumbered = rightobjectforlinkdefinition.getProperty("NUMBERED");
ConstraintOnLinkToMaster<E, G>[] constraints = linkobjecttomasterpropertydefinition.getAllConstraints();
if (rightobjectnumbered == null)
	throw new RuntimeException(
			"Numbered is not existing for right object " + rightobjectforlinkdefinition.getName());
if (!(rightobjectnumbered instanceof NumberedDefinition))
	throw new RuntimeException("Numbered does not have the right class for right object "
			+ rightobjectforlinkdefinition.getName());
return new LeftforlinktomasterFlatFileLoader(objectdefinition, linkobjectdefinition, rightobjectforlinkdefinition,
		linkobjecttomasterpropertydefinition, (NumberedDefinition) rightobjectnumbered, createmissing, deletelink,
		hardcodedvalue, constraints);

}

@Override
public String[] getLoaderFieldList() {
try {

	if (rightobjectforlinkdefinition.getProperty("NUMBERED") != null) {
		return new String[] { "" };
	}
} catch (Exception e) {
	ExceptionLogger.setInLogs(e, logger);
}
return new String[] {};
}

@Override
public String[] getLoaderFieldSample(String name) {

String[] returntable = new String[4];
returntable[0] = this.getName();
returntable[1] = "OPTIONAL";
returntable[2] = "ID1234";
returntable[3] = "the number of the right object. It is possible to specify CREATE, which will create the right object "
		+ rightobjectforlinkdefinition.getName()
		+ " with the given number. \"DELETELINK\" option will delete links not mentioned in this line. It is possible to enter several values separated by '|'";
return returntable;
}
}
