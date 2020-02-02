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

import java.util.Iterator;

import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * A Data Access Method is a method that allows access to data of the property.
 * It can be called from actions and utilities
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public class DataAccessMethod
		extends
		Named {

	private ArgumentContent output;
	private NamedList<MethodArgument> input;
	private MethodArgument implicitobjectmethod;
	private boolean acceptquerycondition;
	private boolean needpropertyextractor;
	private boolean isstaticexecuted = false;
	private boolean ismassive = false;

	/**
	 * @return true if the method accepts a query condition. This is mostly true for
	 *         data access methods performing queries
	 */
	public boolean isAcceptquerycondition() {
		return this.acceptquerycondition;
	}

	/**
	 * @return true if a property extractor is needed. typically, this is required
	 *         when the data access method uses a related property
	 */
	public boolean needPropertyExtractor() {
		return this.needpropertyextractor;
	}

	/**
	 * sets that the data access method needs a property extractor
	 */
	public void setNeedForPropertyExtractor() {
		this.needpropertyextractor = true;
	}

	/**
	 * Create a data access method specifying the output
	 * 
	 * @param name                 name of the method. Should be unique amongst all
	 *                             method properties of the application
	 * @param output               output of the method
	 * @param acceptquerycondition specifies if the method accepts an additional
	 *                             query condition method (example: a select does
	 *                             not "start" from an object)
	 */
	public DataAccessMethod(String name, ArgumentContent output, boolean acceptquerycondition) {
		super(name);
		this.output = output;
		this.input = new NamedList<MethodArgument>();
		this.implicitobjectmethod = null;
		this.acceptquerycondition = acceptquerycondition;
		this.needpropertyextractor = false;
	}

	/**
	 * Create a data access method
	 * 
	 * @param name                 name of the method. Should be unique amongst all
	 *                             method properties of the application
	 * @param output               output of the method
	 * @param acceptquerycondition specifies if the method accepts an additional
	 *                             query condition method (example: a select does
	 *                             not "start" from an object)
	 * @param masstreatment        true if mass treatment is implemented for this
	 *                             method. Mass treatment allows to treat
	 *                             efficiently a batch of objects
	 */
	public DataAccessMethod(String name, ArgumentContent output, boolean acceptquerycondition, boolean masstreatment) {
		this(name, output, acceptquerycondition);
		this.ismassive = masstreatment;

	}

	/**
	 * @return true if method implements massive treatment
	 */
	public boolean isMassive() {
		return this.ismassive;
	}

	/**
	 * @return the implicit argument for the method (if method is not static)
	 */
	public MethodArgument getImplicitobjectmethod() {
		if (!isstaticexecuted)
			isStatic();
		return this.implicitobjectmethod;
	}

	/**
	 * @param argument adds an input argument
	 */
	public void addInputArgument(MethodArgument argument) {
		input.add(argument);
	}

	/**
	 * A method should be static if it does not have a single object as an input
	 * attribute
	 * 
	 * @return true if the method should be implemented as static.
	 */
	public boolean isStatic() {
		isstaticexecuted = true;
		for (int i = 0; i < this.input.getSize(); i++) {
			ArgumentContent thisargumentcontent = input.get(i).getContent();
			if (thisargumentcontent instanceof ObjectArgument) {
				// note - may need to add a condition on the type of object
				this.implicitobjectmethod = input.get(i); // as argument is implicit, it will not be an attribute for
															// the user
				return false;

			}
		}
		return true;
	}

	/**
	 * @return get the output of the current method
	 */
	public ArgumentContent getoutputargument() {
		return output;
	}

	/**
	 * @return get the number of input attributes
	 */
	public int getInputAttributeNumber() {
		return input.getSize();
	}

	/**
	 * gets the input agument at the given index
	 * 
	 * @param index an integer between 0 (included) and getInputAttributeNumber
	 *              (excluded)
	 * @return the input method argument at the given index
	 */
	public MethodArgument getInputArgument(int index) {
		return input.get(index);
	}

	/**
	 * generates the method internal argument for massive
	 * 
	 * @param thisproperty   relevant property (parent)
	 * @param classname      class name of the data object
	 * @param startwithcomma if true, start with a comma (if there is another
	 *                       argument before)
	 * @return the method internal argument
	 */
	public String generateMethodInternalArgumentsForMassive(
			Property<?> thisproperty,
			String classname,
			boolean startwithcomma) {
		return generateMethodInternalArgumentsForMassive(thisproperty, classname, startwithcomma, true);
	}

	/**
	 * generates the method internal argument for massive to be used for code
	 * generation
	 * 
	 * @param thisproperty    relevant property (parent)
	 * @param classname       class name of the data object
	 * @param startwithcomma  if true, start with a comma (if there is another
	 *                        argument before)
	 * @param definitionstuff true if part of object definition, false if part of
	 *                        object run-time
	 * @return the method internal argument
	 */
	public String generateMethodInternalArgumentsForMassive(
			Property<?> thisproperty,
			String classname,
			boolean startwithcomma,
			boolean definitionstuff) {
		StringBuffer arguments = new StringBuffer();
		String thispropertyname = StringFormatter.formatForAttribute(thisproperty.getName());
		String thispropertyclassname = StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname());
		for (int k = 0; k < this.getInputAttributeNumber(); k++) {
			MethodArgument thisinputargument = this.getInputArgument(k);
			if ((k > 0) || (startwithcomma))
				arguments.append(",");
			arguments.append(StringFormatter.formatForAttribute(thisinputargument.getName()));
		}

		if (this.isAcceptquerycondition()) {
			arguments.append(",");
			arguments.append("additionalcondition");
		}

		if (this.isStatic())
			if (definitionstuff) {
				arguments.append(",");
				arguments.append("definition");

				for (int k = 0; k < thisproperty.getPropertyGenericsSize(); k++) {
					PropertyGenerics thisgenerics = thisproperty.getPropertyGenerics(k);
					String genericsproperty = StringFormatter
							.formatForJavaClass(thisgenerics.getOtherObject().getName());
					arguments.append("," + genericsproperty + "Definition.get" + genericsproperty + "Definition()");
				}
				arguments.append(",");
				arguments.append(
						"definition.get" + StringFormatter.formatForJavaClass(thisproperty.getName()) + "Definition()");

			}
		if (this.needPropertyExtractor()) {

			String generics = "<" + classname;
			for (int u = 0; u < thisproperty.getPropertyGenericsSize(); u++) {
				PropertyGenerics thisgenerics = thisproperty.getPropertyGenerics(u);
				generics = generics + "," + StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName());
			}
			// **** write choice categories for property

			Iterator<String> choicekey = thisproperty.getChoiceCategoryKeyNumber();
			while (choicekey.hasNext()) {
				ChoiceCategory propertychoice = thisproperty.getChoiceCategoryByKey(choicekey.next());
				generics = generics + "," + StringFormatter.formatForJavaClass(propertychoice.getName())
						+ "ChoiceDefinition";
			}

			// **** End write choice categories
			generics = generics + ">";

			arguments.append(",\n");

			arguments.append("				new ObjectExtractor<" + classname + "," + thispropertyclassname + generics
					+ ">() {");
			arguments.append("					@Override");
			arguments.append("					public " + thispropertyclassname + generics + " extract(");
			arguments.append("							" + classname + " object)  {");
			arguments.append("						return object." + thispropertyname + ";");
			arguments.append("					}}");
		}

		return arguments.toString();
	}

	/**
	 * generates the method internal argument for unitary processing to be used for
	 * code generation
	 * 
	 * @param thisproperty   relevant property (parent)
	 * @param classname      class name of the data object
	 * @param startwithcomma if true, start with a comma (if there is another
	 * @return the method arguments
	 */
	public String generateMethodInternalArguments(Property<?> thisproperty, String classname, boolean startwithcomma) {
		StringBuffer arguments = new StringBuffer();
		String thispropertyname = StringFormatter.formatForAttribute(thisproperty.getName());
		String thispropertyclassname = StringFormatter.formatForJavaClass(thisproperty.getPropertyclassname());
		int kbis2 = 0; // counter that does not count the self attribute
		if (startwithcomma)
			kbis2 = 1;
		for (int k = 0; k < this.getInputAttributeNumber(); k++) {
			MethodArgument thisinputargument = this.getInputArgument(k);
			if (!thisinputargument.equals(this.getImplicitobjectmethod())) {

				if (kbis2 > 0)
					arguments.append(",");
				arguments.append(StringFormatter.formatForAttribute(thisinputargument.getName()));
				kbis2++;
			} else {
				// update property methods to give object. This will help a property method call
				// other property methods
				// including
				if (kbis2 > 0)
					arguments.append(",");
				arguments.append("this");
				kbis2++;
				// arguments.append("/*discarded - "+thisinputargument.getName()+"*/");
			}

		}
		if (this.isAcceptquerycondition()) {
			if (kbis2 > 0) {
				arguments.append(",");
			}
			kbis2++;
			arguments.append("additionalcondition");
		}

		if (this.isStatic()) {
			if (kbis2 > 0)
				arguments.append(",");
			arguments.append("definition");
			kbis2++;
			for (int k = 0; k < thisproperty.getPropertyGenericsSize(); k++) {
				PropertyGenerics thisgenerics = thisproperty.getPropertyGenerics(k);
				String genericsproperty = StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName());
				arguments.append("," + genericsproperty + "Definition.get" + genericsproperty + "Definition()");
			}
			if (kbis2 > 0)
				arguments.append(",");
			arguments.append(
					"definition.get" + StringFormatter.formatForJavaClass(thisproperty.getName()) + "Definition()");

		}
		if (this.needPropertyExtractor()) {

			String generics = "<" + classname;
			for (int u = 0; u < thisproperty.getPropertyGenericsSize(); u++) {
				PropertyGenerics thisgenerics = thisproperty.getPropertyGenerics(u);
				generics = generics + "," + StringFormatter.formatForJavaClass(thisgenerics.getOtherObject().getName());
			}
			// **** write choice categories for property

			Iterator<String> choicekey = thisproperty.getChoiceCategoryKeyNumber();
			while (choicekey.hasNext()) {
				ChoiceCategory propertychoice = thisproperty.getChoiceCategoryByKey(choicekey.next());
				generics = generics + "," + StringFormatter.formatForJavaClass(propertychoice.getName())
						+ "ChoiceDefinition";
			}

			// **** End write choice categories
			generics = generics + ">";

			if (kbis2 > 0)
				arguments.append(",\n");
			kbis2++;
			arguments.append("				new ObjectExtractor<" + classname + "," + thispropertyclassname + generics
					+ ">() {");
			arguments.append("					@Override");
			arguments.append("					public " + thispropertyclassname + generics + " extract(");
			arguments.append("							" + classname + " object)  {");
			arguments.append("						return object." + thispropertyname + ";");
			arguments.append("					}}");
		}

		return arguments.toString();
	}

	/**
	 * generates the methods arguments for massive processing
	 * 
	 * @return methods arguments
	 */
	public String generateMethodArgumentsForMassive() {
		StringBuffer arguments = new StringBuffer();

		for (int k = 0; k < this.getInputAttributeNumber(); k++) {

			MethodArgument thisinputargument = this.getInputArgument(k);
			if (thisinputargument == null)
				throw new RuntimeException(
						" input argument index " + k + " is null for method " + this.getName() + " for object");
			if (k > 0)
				arguments.append(",");
			arguments.append(thisinputargument.getContent().getType() + "[] "
					+ StringFormatter.formatForAttribute(thisinputargument.getName()));

		}

		if (this.isAcceptquerycondition()) {
			arguments.append(",");
			arguments.append("QueryFilter additionalcondition");

		}

		return arguments.toString();
	}

	/**
	 * generates the arguments for arguments pass-through. This is used for methods
	 * with security
	 * 
	 * @return the arguments for method pass-through
	 */
	public String generateMethodPassThroughArguments() {
		StringBuffer arguments = new StringBuffer();

		int kbis = 0; // counter that does not count the self attribute
		for (int k = 0; k < this.getInputAttributeNumber(); k++) {
			MethodArgument thisinputargument = this.getInputArgument(k);
			if (thisinputargument == null)
				throw new RuntimeException(
						" input argument index " + k + " is null for method " + this.getName() + " for object");
			if (!thisinputargument.equals(this.getImplicitobjectmethod())) {

				if (kbis > 0)
					arguments.append(",");
				arguments.append(StringFormatter.formatForAttribute(thisinputargument.getName()));
				kbis++;
			} else {
				arguments.append("/*discarded - " + thisinputargument.getName() + "*/");
			}

		}
		if (this.isAcceptquerycondition()) {
			if (kbis > 0)
				arguments.append(",");
			arguments.append("additionalcondition");
			kbis++;
		}

		if (kbis > 0)
			arguments.append(",");
		kbis++;
		arguments.append("null,SecurityInDataMethod.NONE");

		return arguments.toString();
	}

	/**
	 * generates the method unitary arguments, with potentially security arguments
	 * 
	 * @param securityarguments if true, generates with security arguments
	 * @return method unitary arguments
	 */
	public String generateMethodArguments(boolean securityarguments) {
		StringBuffer arguments = new StringBuffer();

		int kbis = 0; // counter that does not count the self attribute
		for (int k = 0; k < this.getInputAttributeNumber(); k++) {
			MethodArgument thisinputargument = this.getInputArgument(k);
			if (thisinputargument == null)
				throw new RuntimeException(
						" input argument index " + k + " is null for method " + this.getName() + " for object");
			if (!thisinputargument.equals(this.getImplicitobjectmethod())) {

				if (kbis > 0)
					arguments.append(",");
				arguments.append(thisinputargument.getContent().getType() + " "
						+ StringFormatter.formatForAttribute(thisinputargument.getName()));
				kbis++;
			} else {
				arguments.append("/*discarded - " + thisinputargument.getName() + "*/");
			}

		}
		if (this.isAcceptquerycondition()) {
			if (kbis > 0)
				arguments.append(",");
			arguments.append("QueryFilter additionalcondition");
			kbis++;
		}
		if (securityarguments) {
			if (kbis > 0)
				arguments.append(",");
			kbis++;
			arguments.append("ActionExecution contextaction,SecurityInDataMethod method");
		}
		return arguments.toString();
	}
}
