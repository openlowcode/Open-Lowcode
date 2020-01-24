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

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.Named;

/**
 * An argument content describes the name and data type of an argument to an
 * action
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ArgumentContent
		extends
		Named {
	private String displaylabel; // necessary for property creation input argument

	/**
	 * @return the display label of the argument when used for creation of an object
	 */
	public String getDisplaylabel() {
		return displaylabel;
	}

	/**
	 * sets the display label for this argument content. This is only needed if the
	 * argument content is used to describe data to enter for property at object
	 * creation or update
	 * 
	 * @param displaylabel display label (plain English description)
	 */
	public void setDisplaylabel(String displaylabel) {
		this.displaylabel = displaylabel;
	}

	/**
	 * @return the java type of the argument
	 */
	public abstract String getType();

	private boolean securityrelevant;
	private boolean optional;

	/**
	 * A security relevant argument is tested against user privileges. The input
	 * object ids will be tested against privileges and potentially action access
	 * will be prevented. For output objects, they are filtered to give back only
	 * the objects the users have access to.
	 * 
	 * @return true if the argument content is security relevant for an action.
	 */
	public boolean isSecurityrelevant() {
		return securityrelevant;
	}

	/**
	 * generates a deep copy of the argument with a new name
	 * 
	 * @param newname new name
	 * @return deep copy of the argument
	 */
	public abstract ArgumentContent generateCopy(String newname);

	/**
	 * @return true is the argument is optional
	 */
	public boolean isOptional() {
		return optional;
	}

	/**
	 * sets the argument content as optional
	 * 
	 * @param optional true to set the argument content as optional
	 */
	public void setOptional(boolean optional) {
		this.optional = optional;
	}

	/**
	 * creates a new argument content
	 * 
	 * @param name             name of the argument, should be unique per function
	 * @param securityrelevant tells if the attribute should be used as a base for
	 *                         security access rights computation. This attribute
	 *                         does not make sense for all types of arguments. It is
	 *                         typically only relevant for objects and object ids
	 */
	public ArgumentContent(String name, boolean securityrelevant) {
		super(name);
		this.displaylabel = name.toLowerCase(); // until something specified better, take name as label
		this.securityrelevant = securityrelevant;
		this.optional = false;
	}

	/**
	 * @return a generic data element . For Objects, this means returning a simple
	 *         ObjectDataElt name.
	 */
	public abstract String getGenericDataEltName();

	/**
	 * @return a precise data element type. For objects, this means returning a
	 *         TobjectDataEltType<E> where E is a data object
	 */
	public abstract String getPreciseDataEltTypeName();

	/**
	 * @return a precise data element type with argument. For objects, this means
	 *         returning a TobjectDataEltType<E> where E is a data object
	 */
	public String getPreciseDataEltTypeNameWithArgument() {
		return getPreciseDataEltTypeName() + "()";
	}

	/**
	 * @return true if an objectdefinition needs to be provided to create the type
	 *         (typically an opject or an object array), false if the type can be
	 *         created without such oject
	 */
	public abstract boolean needDefinitionForInit();

	/**
	 * @return a precise data element . For objects, this means returning a
	 *         TobjectDataElt<E> where E is a data object
	 */
	public abstract String getPreciseDataEltName();

	/**
	 * writes to a java class the imports necessary for automatic generation of
	 * actions using this parameter
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if any error is encountered writing to a file
	 */
	public abstract void writeImports(SourceGenerator sg, Module module) throws IOException;

	/**
	 * @return all the java types needed for code generation when the argument
	 *         content is set
	 */
	public abstract ArrayList<String> getImports();

	/**
	 * @return a blank objet for this argument
	 */
	public abstract String initblank();

	/**
	 * This method is used to assess if a security argument is used in a manner
	 * compatible with security rules
	 * 
	 * @return null if the attribute refers to several objects or to no object (e.g.
	 *         a stringargument refers to no object).
	 */
	public abstract DataObjectDefinition getMasterObject();

}
