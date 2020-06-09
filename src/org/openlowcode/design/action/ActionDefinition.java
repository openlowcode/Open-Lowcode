/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.openlowcode.design.access.ActionGroup;
import org.openlowcode.design.access.Privilege;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ChoiceArgument;
import org.openlowcode.design.data.argument.IntegerArgument;
import org.openlowcode.design.data.argument.LargeBinaryArgument;
import org.openlowcode.design.data.argument.MultipleChoiceArgument;
import org.openlowcode.design.data.argument.NodeTreeArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.ObjectMasterIdArgument;
import org.openlowcode.design.data.argument.StringArgument;
import org.openlowcode.design.data.argument.TimePeriodArgument;
import org.openlowcode.design.data.argument.TimestampArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * An action is a business transaction that is triggered from user actions on
 * the client. Each action is checked for security. * @author
 * <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ActionDefinition
		extends
		Named
		implements
		ActionGroup {

	@Override
	public String toString() {
		return "(" + this.getClass().getSimpleName() + ";" + this.getName() + ")";
	}

	@Override
	public ActionDefinition[] getActionsInGroup() {
		return new ActionDefinition[] { this };
	}

	private Module module;

	/**
	 * @return the parent module
	 */
	public Module getModule() {
		return this.module;
	}

	private NamedList<ArgumentContent> outputarguments;
	private String buttonlabel = null;

	/**
	 * @return the button label shown automatically on automatically generated
	 *         pages. It should be a short description in the default language
	 */
	public String getButtonlabel() {
		return buttonlabel;
	}

	/**
	 * @return the number of output arguments
	 */
	public int getOutputArgumentNumber() {
		return outputarguments.getSize();
	}

	/**
	 * get the output argument at the given index
	 * 
	 * @param i index between 0 (included) and getOutputArgumentNumber (excluded)
	 * @return
	 */
	public ArgumentContent getOutputArgument(int i) {
		return outputarguments.get(i);
	}

	/**
	 * sets the button label (will be shown when buttons appear in automatic pages
	 * for this action)
	 * 
	 * @param buttonlabel button label to use
	 */
	public void setButtonlabel(String buttonlabel) {
		this.buttonlabel = buttonlabel;
	}

	/**
	 * @return get the index of the input argument that is access criteria
	 */
	public int getAccessCriteriaIndex() {
		return accesscriteriaindex;
	}

	protected int accesscriteriaindex;
	private String specification = null;
	private boolean noaddress = false;

	/**
	 * This will ensure the function will never have an address for the action. This
	 * is typically true for search actions.
	 */
	public void forceNoAddress() {
		this.noaddress = true;
	}

	private ArrayList<String> businessrules;

	/**
	 * @return the list of argument contents
	 */
	public abstract NamedList<ArgumentContent> getInputArguments();

	/**
	 * depending on the arguments, the action can have an address generated that is
	 * similar to an URL link. Only some type of arguments can be put in an address
	 * (object id, text...)
	 * 
	 * @param inputarguments list of input argumennts
	 * @return true if the address can be generated
	 */
	public boolean isAddressGenerated(NamedList<ArgumentContent> inputarguments) {
		if (this.noaddress)
			return false;
		for (int i = 0; i < inputarguments.getSize(); i++) {
			ArgumentContent content = inputarguments.get(i);
			if (content instanceof ObjectIdArgument) {
				// OK, supported, looks at the next one
			} else {
				return false;
			}
		}
		return true;
	}

	/**
	 * this method returns the object argument used for security if it exists
	 * 
	 * @return the object method argument used for security
	 */
	public abstract ObjectArgument getSecurityobjectargument();

	/**
	 * this method returns the object argument id used for security if it exists
	 * 
	 * @return the object method argument id used for security
	 */
	public abstract ObjectIdArgument getSecurityobjectidargument();

	private boolean autogenerated = false;

	/**
	 * @return true if the action is auto-generated by the framework
	 */
	public boolean isAutogenerated() {
		return autogenerated;
	}

	/**
	 * sets the parent module
	 * 
	 * @param module the module to add
	 */
	public void setModule(Module module) {
		if (this.module != null)
			throw new RuntimeException("Not possible to add a module " + module.getName()
					+ ", there is already one defined : " + this.module.getName());
		this.module = module;
	}

	/**
	 * creates a user defined (not autogenerated) action
	 * 
	 * @param name name of the action, should be unique in the module and a valid
	 *             java method name (starts by a letter...)
	 */
	public ActionDefinition(String name) {
		super(name);

		outputarguments = new NamedList<ArgumentContent>();
		this.businessrules = new ArrayList<String>();
		this.autogenerated = false;
	}

	/**
	 * creates an action defining if autogenerated or not
	 * 
	 * @param name          name of the action, should be unique in the module and a
	 *                      valid java method name (starts by a letter...)
	 * @param autogenerated true if autogenerated, false if user generated
	 */
	public ActionDefinition(String name, boolean autogenerated) {
		this(name);
		this.autogenerated = autogenerated;
	}

	/**
	 * creates an action (user-defined) with a specification to include in the
	 * javadoc of the action
	 * 
	 * @param name          name of the action, should be unique in the module and a
	 *                      valid java method name (starts by a letter...)
	 * @param specification a free text specification
	 */
	public ActionDefinition(String name, String specification) {
		this(name);
		this.specification = specification;
	}

	/**
	 * creates an action with a specification to include in the javadoc of the
	 * action
	 * 
	 * @param name          name of the action, should be unique in the module and a
	 *                      valid java method name (starts by a letter...)
	 * @param specification a free text specification
	 * @param autogenerated true if autogenerated, false if user generated
	 */
	public ActionDefinition(String name, String specification, boolean autogenerated) {
		this(name);
		this.specification = specification;
		this.autogenerated = autogenerated;
	}

	/**
	 * adds a business rule (in plain text) for the action rules to be added to the
	 * action javadoc
	 * 
	 * @param businessrule business rule to add
	 */
	public void addBusinessRule(String businessrule) {
		this.businessrules.add(businessrule);
	}

	/**
	 * adds an output argument to this action
	 * 
	 * @param thisargument argument to add
	 */
	public void addOutputArgument(ArgumentContent thisargument) {
		outputarguments.add(thisargument);
	}

	protected boolean accesscriteriaisinput;
	protected ArgumentContent accesscriteria;

	/**
	 * @return true if the access criteria is in input
	 */
	public boolean isAccessCriteriaInput() {
		return accesscriteriaisinput;
	}

	/**
	 * @return get the access criteria (input or output)
	 */
	public ArgumentContent getAccessCriteria() {
		return accesscriteria;
	}

	/**
	 * adds this output argument as security criteria. Typically, this requires the
	 * server to filter the data and only show it if user has visualization right on
	 * it
	 * 
	 * @param thisargument argument to add as output access criteria
	 */
	public void addOutputArgumentAsAccessCriteria(ArgumentContent thisargument) {
		accesscriteriaindex = outputarguments.getSize();
		outputarguments.add(thisargument);
		if (accesscriteria != null)
			throw new RuntimeException("there cannot be two access criteria for ActionDefinition " + this.getName());
		accesscriteriaisinput = false;
		accesscriteria = thisargument;
		if (thisargument.getMasterObject() != null) {
			DataObjectDefinition thisobject = thisargument.getMasterObject();
			thisobject.addActionToFullGroup(this);
		}
	}

	/**
	 * generates the abstract class of the action to file. This provides a framework
	 * for user to add their code to
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens while writing the file.
	 */
	public void generateToFile(SourceGenerator sg, Module module) throws IOException {
		String actionclass = StringFormatter.formatForJavaClass(this.getName());
		Privilege[] privilegesforaction = this.module.getPrivilegesForAction(this);
		boolean hasoptioninputargument = false;
		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			ArgumentContent inputargument = this.getInputArguments().get(i);
			if (inputargument.isOptional())
				hasoptioninputargument = true;
		}

		boolean isoutputobject = false;
		if (outputarguments.getSize() > 0)
			isoutputobject = true;
		if (outputarguments.getSize() == 0)
			if (this.isAddressGenerated(this.getInputArguments()))
				isoutputobject = true;
		boolean severaloutputobjects = false;
		if (outputarguments.getSize() > 1)
			severaloutputobjects = true;
		// address requires presence of
		if (this.isAddressGenerated(this.getInputArguments()))
			severaloutputobjects = true;
		// generateoutputinnerclass
		sg.bl();

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.bl();
		if (!this.isAutogenerated()) {
			sg.wl("/* <<<< Warning >>>> : You need to implement a concrete class in a separate file *!*! -----");
			sg.wl(" > This is an automatically generated abstract class providing the interface to ");
			sg.wl(" > the action you have to implement. Please create the class implementing ");
			sg.wl(" > this abstract class at path: ");
			sg.wl("    --> " + this.getModule().getPath() + ".action."
					+ StringFormatter.formatForJavaClass(this.getName()) + "Action");
			sg.wl(" > Please do NOT modify the current file, as it will be erased and generated again.");
			sg.wl(" ----------------------------------------------------------------------------------------- */");
		}
		sg.bl();
		if (this.isAddressGenerated(this.getInputArguments())) {
			sg.wl("import org.openlowcode.tools.messages.MessageStringField;");
		}

		sg.wl("import java.util.ArrayList;");
		sg.wl("import java.util.Date;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import java.util.function.BiFunction;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		for (int i = 0; i < privilegesforaction.length; i++) {
			privilegesforaction[i].writeImport(sg, this);
		}
		sg.wl("import org.openlowcode.server.action.ActionExecution;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.OLcServer;");
		sg.wl("import org.openlowcode.server.graphic.SPageData;");
		sg.wl("import org.openlowcode.server.action.SActionData;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.server.security.ActionSecurityManager;");
		sg.wl("import org.openlowcode.server.security.SecurityBuffer;");

		sg.wl("import org.openlowcode.server.action.SActionInputDataRef;");
		sg.wl("import org.openlowcode.server.graphic.widget.SActionDataLoc;");
		if (hasoptioninputargument)
			sg.wl("import org.openlowcode.server.action.SNullActionInputDataRef;");
		sg.wl("import org.openlowcode.server.action.SActionOutputDataRef;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.action.SActionRef;");
		sg.wl("import org.openlowcode.server.action.SInlineActionRef;");
		sg.wl("import org.openlowcode.server.data.message.*;");
		sg.wl("import org.openlowcode.tools.structure.*;");
		sg.wl("import " + module.getPath() + ".data.*;");
		for (int i = 0; i < this.getInputArguments().getSize(); i++)
			getInputArguments().get(i).writeImports(sg, module);
		for (int i = 0; i < this.outputarguments.getSize(); i++)
			this.outputarguments.get(i).writeImports(sg, module);
		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			ArgumentContent thisargument = this.getInputArguments().get(i);
			if (thisargument instanceof ArrayArgument) {
				ArrayArgument thisarray = (ArrayArgument) thisargument;
				thisargument = thisarray.getPayload();
			}
			if (thisargument instanceof ObjectArgument) {
				ObjectArgument objectargument = (ObjectArgument) thisargument;
				Module objectmodule = objectargument.getPayload().getOwnermodule();
				if (objectmodule.getPath().compareTo(module.getPath()) != 0)
					sg.wl("import " + objectmodule.getPath() + ".data."
							+ StringFormatter.formatForJavaClass(objectargument.getPayload().getName()) + ";");
			}
			if (thisargument instanceof ObjectIdArgument) {
				ObjectIdArgument objectidargument = (ObjectIdArgument) thisargument;
				if (objectidargument.getObject() != null) {
					Module objectmodule = objectidargument.getObject().getOwnermodule();
					if (objectmodule.getPath().compareTo(module.getPath()) != 0)
						sg.wl("import " + objectmodule.getPath() + ".data."
								+ StringFormatter.formatForJavaClass(objectidargument.getObject().getName()) + ";");
				}
			}
			if (thisargument instanceof ChoiceArgument) {
				ChoiceArgument thischoiceargument = (ChoiceArgument) thisargument;
				thischoiceargument.writeImports(sg, this.module);
			}
		}
		for (int i = 0; i < this.outputarguments.getSize(); i++) {
			ArgumentContent thisargument = this.outputarguments.get(i);
			if (thisargument instanceof ArrayArgument) {
				ArrayArgument thisarray = (ArrayArgument) thisargument;
				thisargument = thisarray.getPayload();
			}

			if (thisargument instanceof ObjectArgument) {
				ObjectArgument objectargument = (ObjectArgument) thisargument;
				Module objectmodule = objectargument.getPayload().getOwnermodule();
				if (objectmodule.getPath().compareTo(module.getPath()) != 0)
					sg.wl("import " + objectmodule.getPath() + ".data."
							+ StringFormatter.formatForJavaClass(objectargument.getPayload().getName()) + ";");
			}
			if (thisargument instanceof ObjectIdArgument) {
				ObjectIdArgument objectidargument = (ObjectIdArgument) thisargument;
				if (objectidargument.getObject() != null) {
					Module objectmodule = objectidargument.getObject().getOwnermodule();
					if (objectmodule.getPath().compareTo(module.getPath()) != 0)
						sg.wl("import " + objectmodule.getPath() + ".data."
								+ StringFormatter.formatForJavaClass(objectidargument.getObject().getName()) + ";");
				}
			}
		}

		sg.bl();
		sg.wl("	 /**");
		sg.wl("	  * @author " + module.getAuthor());
		sg.wl("	  * Action " + actionclass + " from Open Lowcode module " + module.getAuthor() + "<br>");
		if (this.specification != null) {
			sg.wl("	  * <br>");
			sg.wl("	  * Specifications<br>");
			sg.wl("	  * <br>");
			sg.wl("	  * " + this.specification + "<br>");
			if (this.businessrules.size() > 0) {
				sg.wl("	  * <br>");
				sg.wl("	  * Business rules<br><ul>");
				for (int i = 0; i < this.businessrules.size(); i++) {
					sg.wl("	  *<li>" + this.businessrules.get(i) + "</li>");
				}
				sg.wl("	  *</ul>");
			}

		}
		sg.wl("	  */");
		// *********************** defining interfaces
		String interfaces = "";
		boolean interfacepresent = false;
		if (this.getSecurityobjectargument() != null) {
			if (!interfacepresent) {
				interfaces += " implements ";
				interfacepresent = true;
			} else {
				interfaces += " , ";
			}
			interfaces += "ActionSecurityObjectArgument";
		}
		// *********************** defining interfaces end

		sg.wl("	public abstract class Abs" + actionclass + "Action extends ActionExecution " + interfaces + " {");
		sg.bl();
		// ************************* define formatted actions ******

		sg.wl("	public class ActionRef extends SActionRef {");
		sg.wl("		protected ActionRef() {");
		sg.wl("			super(Abs" + actionclass + "Action.this.getName(),");
		sg.wl("					Abs" + actionclass + "Action.this.getParent().getName(),");
		sg.wl("					" + this.accesscriteriaindex + ");");
		sg.wl("		}");
		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			ArgumentContent thisinputargument = this.getInputArguments().get(i);
			String attributeclass = StringFormatter.formatForJavaClass(thisinputargument.getName());
			String attributetype = thisinputargument.getPreciseDataEltTypeName();
			sg.wl("		public void set" + attributeclass + "(Function<SActionInputDataRef<" + attributetype
					+ ">,SActionDataLoc<" + attributetype + ">> input) {");
			sg.wl("			if (input==null) this.addActionBusinessData(SActionDataLoc.ground(Abs" + actionclass
					+ "Action.this.get" + attributeclass + "Ref()));");
			sg.wl("			if (input!=null) this.addActionBusinessData(input.apply(Abs" + actionclass
					+ "Action.this.get" + attributeclass + "Ref()));");
			sg.wl("		}");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	public class InlineActionRef extends SInlineActionRef {");
		sg.wl("		protected InlineActionRef() {");
		sg.wl("			super(Abs" + actionclass + "Action.this.getName(),");
		sg.wl("					Abs" + actionclass + "Action.this.getParent().getName(),");
		sg.wl("					" + this.accesscriteriaindex + ");");
		sg.wl("		}");
		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			ArgumentContent thisinputargument = this.getInputArguments().get(i);
			String attributeclass = StringFormatter.formatForJavaClass(thisinputargument.getName());
			String attributetype = thisinputargument.getPreciseDataEltTypeName();
			sg.wl("		public void set" + attributeclass + "(Function<SActionInputDataRef<" + attributetype
					+ ">,SActionDataLoc<" + attributetype + ">> input) {");
			sg.wl("			if (input==null) this.addActionBusinessData(SActionDataLoc.ground(Abs" + actionclass
					+ "Action.this.get" + attributeclass + "Ref()));");
			sg.wl("			if (input!=null) this.addActionBusinessData(input.apply(Abs" + actionclass
					+ "Action.this.get" + attributeclass + "Ref()));");
			sg.wl("		}");
		}
		sg.wl("	}");

		sg.wl("	public ActionRef getActionRef() {");
		sg.wl("		return new ActionRef();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	public InlineActionRef getInlineActionRef() {");
		sg.wl("		return new InlineActionRef();");
		sg.wl("	}");

		// *********************** define getters *************
		sg.wl("	public static Abs" + actionclass + "Action get() {");
		sg.wl("		return (Abs" + actionclass + "Action)");
		sg.wl("				OLcServer.getServer()");
		sg.wl("				.getModuleByName(\"" + this.getModule().getName().toUpperCase() + "\").getAction(\""
				+ this.getName().toUpperCase() + "\"); ");
		sg.wl("		}");
		sg.bl();
		sg.wl("		protected int getInputSecurityArgumentIndex() {");
		if (!this.isAccessCriteriaInput()) {
			sg.wl("			return -1;");
		} else {
			sg.wl("			return " + this.accesscriteriaindex + ";");
		}
		sg.wl("		}");
		if (this.isAccessCriteriaInput()) {
			ArgumentContent getaccesscriteria = this.getAccessCriteria();
			DataObjectDefinition accesscriteriaobject = this.accesscriteria.getMasterObject();
			String accesscriteriaobjectclass = StringFormatter.formatForJavaClass(accesscriteriaobject.getName());
			String accesscriteriaobjectvariable = StringFormatter.formatForAttribute(accesscriteriaobject.getName());
			sg.wl("	public static BiFunction<SActionData,SecurityBuffer," + accesscriteriaobjectclass
					+ "[]> getInputSecurityDataExtractor() {");

			sg.wl("			return new BiFunction<SActionData,SecurityBuffer," + accesscriteriaobjectclass + "[]>() {");
			sg.wl("				@Override");
			sg.wl("				public " + accesscriteriaobjectclass
					+ "[] apply(SActionData actiondata, SecurityBuffer buffer)  {");
			sg.wl("					if (actiondata==null) return new " + accesscriteriaobjectclass
					+ "[0]; // only happens for widget visibility");
			boolean implemented = false;

			if (getaccesscriteria instanceof ObjectIdArgument) {
				sg.wl("				DataObjectId<" + accesscriteriaobjectclass + "> objectid = null;");
				sg.wl("				if (actiondata.getAttribute(" + accesscriteriaindex
						+ ") instanceof ObjectIdDataElt) {");
				sg.wl("					ObjectIdDataElt attribute" + accesscriteriaindex
						+ " = (ObjectIdDataElt) actiondata.getAttribute(" + accesscriteriaindex + ");");
				sg.wl("					if (attribute" + accesscriteriaindex + ".getName().compareTo(\""
						+ getaccesscriteria.getName().toUpperCase()
						+ "\")==0) objectid = DataObjectId.generatefromDataObjectIdElt(attribute" + accesscriteriaindex
						+ "," + accesscriteriaobjectclass + ".getDefinition());");
				sg.wl("				} else {");
				sg.wl("					throw new RuntimeException(\"Expects to get an ObjectIdDataElt, got \"+actiondata.getAttribute("
						+ accesscriteriaindex + ").getType());");
				sg.wl("				}		");

				sg.wl("			if (objectid==null) {			");
				sg.wl("				String exceptionstring = \"action data = \";");
				sg.wl("				for (int i=0;i<actiondata.size();i++) exceptionstring+=(actiondata.getAttribute(i)!=null?\"\"+actiondata.getAttribute(i).getName()+\"/\"+actiondata.getAttribute(i).getType().printType()+\"|\":\"NULL\");");
				sg.wl("				throw new RuntimeException(\"Data Entry '"
						+ getaccesscriteria.getName().toUpperCase()
						+ "' not found for security \"+exceptionstring);	");
				sg.wl("				}");

				sg.wl("				" + accesscriteriaobjectclass + " " + accesscriteriaobjectvariable
						+ "inbuffer = buffer.getObject(objectid);");
				sg.wl("				return new " + accesscriteriaobjectclass + "[]{" + accesscriteriaobjectvariable
						+ "inbuffer};");
				sg.wl("			}");

				implemented = true;
			}
			if (getaccesscriteria instanceof ArrayArgument) {
				ArrayArgument arrayargument = (ArrayArgument) getaccesscriteria;
				if (arrayargument.getPayload() instanceof ObjectArgument) {

					sg.wl("				" + accesscriteriaobjectclass + "[] " + accesscriteriaobjectvariable
							+ " = null;");
					sg.wl("				if (actiondata.getAttribute(" + accesscriteriaindex
							+ ") instanceof ArrayDataElt) {");
					sg.wl("					ArrayDataElt attribute" + accesscriteriaindex
							+ " = (ArrayDataElt) actiondata.getAttribute(" + accesscriteriaindex + ");");
					sg.wl("					if (attribute" + accesscriteriaindex
							+ ".getArrayPayloadEltType() instanceof ObjectDataEltType) {");
					sg.wl("						ArrayList<" + accesscriteriaobjectclass
							+ "> inputlist = new  ArrayList<" + accesscriteriaobjectclass + ">();");
					sg.wl("						for (int i=0;i<attribute" + accesscriteriaindex
							+ ".getObjectNumber();i++) {");
					sg.wl("							ObjectDataElt objectinarray = (ObjectDataElt) attribute"
							+ accesscriteriaindex + ".getObjectAtIndex(i);");
					sg.wl("							if (objectinarray.getName().compareTo(\""
							+ getaccesscriteria.getName().toUpperCase() + "\")==0) {");
					sg.wl("								" + accesscriteriaobjectclass + " current"
							+ accesscriteriaobjectvariable + " = null;");
					sg.wl("								if (objectinarray.getUID().length()>0) current"
							+ accesscriteriaobjectvariable
							+ " =  buffer.getObject(DataObjectId.generatefromDataObjectElt(objectinarray,"
							+ accesscriteriaobjectclass + ".getDefinition()));");
					sg.wl("								if (objectinarray.getUID().length()==0) current"
							+ accesscriteriaobjectvariable + " = new " + accesscriteriaobjectclass + "();");
					sg.wl("								inputlist.add(current" + accesscriteriaobjectvariable + ");");
					sg.wl("							} else {");
					sg.wl("								throw new RuntimeException(\" " + accesscriteriaobjectvariable
							+ " attribute does not have the correct name, was expecting '"
							+ getaccesscriteria.getName().toUpperCase() + "', got \"+objectinarray.getName());");
					sg.wl("							}");
					sg.wl("						}");
					sg.wl("						if (inputlist.size()>0) " + accesscriteriaobjectvariable
							+ " = inputlist.toArray(new " + accesscriteriaobjectclass + "[0]);");
					sg.wl("					}");
					sg.wl("				}");
					sg.wl("			return " + accesscriteriaobjectvariable + ";");
					sg.wl("		}");

					implemented = true;

				}
				if (arrayargument.getPayload() instanceof ObjectIdArgument) {

					sg.wl("				" + accesscriteriaobjectclass + "[] " + accesscriteriaobjectvariable
							+ "s=null;");
					sg.wl("				if (actiondata.getAttribute(" + accesscriteriaindex
							+ ") instanceof ArrayDataElt) {");
					sg.wl("					ArrayList<" + accesscriteriaobjectclass + "> "
							+ accesscriteriaobjectvariable + "list = new ArrayList<" + accesscriteriaobjectclass
							+ ">();");
					sg.wl("					ArrayDataElt attribute" + accesscriteriaindex
							+ " = (ArrayDataElt) actiondata.getAttribute(" + accesscriteriaindex + ");");
					sg.wl("					if (attribute" + accesscriteriaindex
							+ ".getArrayPayloadEltType() instanceof ObjectIdDataEltType) {");
					sg.wl("						ArrayList<DataObjectId<" + accesscriteriaobjectclass + ">> listforleft"
							+ accesscriteriaobjectvariable + "id = new ArrayList<DataObjectId<"
							+ accesscriteriaobjectclass + ">>();");
					sg.wl("						for (int i=0;i<attribute" + accesscriteriaindex
							+ ".getObjectNumber();i++) {");
					sg.wl("							ObjectIdDataElt thisobjectid = (ObjectIdDataElt) attribute"
							+ accesscriteriaindex + ".getObjectAtIndex(i);");
					sg.wl("							if (thisobjectid.getName().compareTo(\""
							+ getaccesscriteria.getName().toUpperCase() + "\")==0) {");
					sg.wl("								" + accesscriteriaobjectvariable
							+ "list.add(buffer.getObject(DataObjectId.generatefromDataObjectIdElt(thisobjectid,"
							+ accesscriteriaobjectclass + ".getDefinition())));");
					sg.wl("							} else {");
					sg.wl("								throw new RuntimeException(String.format(\"was expecting an objectid attribute inside array called "
							+ getaccesscriteria.getName().toUpperCase()
							+ " as attribute 0, got %s \",thisobjectid.getName()));");
					sg.wl("							}");
					sg.wl("						}");
					sg.wl("						");
					sg.wl("					} else {");
					sg.wl("						throw new RuntimeException(String.format(\"was expecting an ObjectId inside array called "
							+ getaccesscriteria.getName().toUpperCase() + " as attribute 0, got %s \",attribute"
							+ accesscriteriaindex + ".getArrayPayloadEltType() ));");
					sg.wl("						");
					sg.wl("					}	");
					sg.wl("					" + accesscriteriaobjectvariable + "s = " + accesscriteriaobjectvariable
							+ "list.toArray(new " + accesscriteriaobjectclass + "[0]);");
					sg.wl("				}");
					sg.wl("				");
					sg.wl("				if (" + accesscriteriaobjectvariable
							+ "s == null) throw new RuntimeException(String.format( \" was expecting a DataObjectId<"
							+ accesscriteriaobjectclass + ">[] attribute called "
							+ getaccesscriteria.getName().toUpperCase()
							+ " as attribute 0, got %s \",actiondata.getAttribute(0)));");
					sg.wl("				return " + accesscriteriaobjectvariable + "s;");
					sg.wl("			}");

					implemented = true;
				}
			}
			if (getaccesscriteria instanceof ObjectArgument) {

				sg.wl("				" + accesscriteriaobjectclass + " " + accesscriteriaobjectvariable + " = null;");
				sg.wl("				if (actiondata.getAttribute(" + accesscriteriaindex
						+ ") instanceof ObjectDataElt) {");
				sg.wl("					ObjectDataElt attribute" + accesscriteriaindex
						+ " = (ObjectDataElt) actiondata.getAttribute(" + accesscriteriaindex + ");");
				sg.wl("				if (attribute" + accesscriteriaindex + ".getName().compareTo(\""
						+ getaccesscriteria.getName().toUpperCase() + "\")==0) {");
				sg.wl("					if (attribute" + accesscriteriaindex + ".getUID().length()>0) "
						+ accesscriteriaobjectvariable
						+ " = buffer.getObject(DataObjectId.generatefromDataObjectElt(attribute" + accesscriteriaindex
						+ "," + accesscriteriaobjectclass + ".getDefinition()));");
				sg.wl("					if (attribute" + accesscriteriaindex + ".getUID().length()==0) "
						+ accesscriteriaobjectvariable + " = new " + accesscriteriaobjectclass + "();");
				sg.wl("				}");
				sg.wl("				}");
				sg.wl("				");
				sg.wl("				if (" + accesscriteriaobjectvariable
						+ " == null) throw new RuntimeException(String.format( \" was expecting a " + accesscriteriaobjectclass
						+ " attribute called " + getaccesscriteria.getName().toUpperCase() + " as attribute "
						+ accesscriteriaindex + " of action , got %s \",actiondata.getAttribute(" + accesscriteriaindex
						+ ")));");
				sg.wl("				return new " + accesscriteriaobjectclass + "[]{" + accesscriteriaobjectvariable
						+ "};			");
				sg.wl("			}");
				sg.wl("			");
				implemented = true;
			}

			if (!implemented)
				sg.wl("  ### Type not supported yet in generation " + getaccesscriteria.getClass().getName());

			sg.wl("			};");

			sg.wl("		}");
		}
		sg.wl("	public Abs" + actionclass + "Action(SModule parent) {");
		sg.wl("		super(\"" + this.getName() + "\",parent);");
		sg.wl("	}");

		if (severaloutputobjects) {
			sg.wl("	public class ActionOutputData {");
			for (int i = 0; i < outputarguments.getSize(); i++) {
				ArgumentContent thisarg = outputarguments.get(i);
				sg.wl("		private " + thisarg.getType() + " " + StringFormatter.formatForAttribute(thisarg.getName())
						+ ";");
			}
			sg.wl("		private String address;");
			sg.w("		public ActionOutputData(");
			for (int i = 0; i < outputarguments.getSize(); i++) {
				ArgumentContent thisarg = outputarguments.get(i);
				if (i > 0)
					sg.w(",");
				sg.w("	" + thisarg.getType() + " " + StringFormatter.formatForAttribute(thisarg.getName()));

			}
			sg.wl(") {");
			for (int i = 0; i < outputarguments.getSize(); i++) {
				ArgumentContent thisarg = outputarguments.get(i);
				sg.wl("			this." + StringFormatter.formatForAttribute(thisarg.getName()) + " = "
						+ StringFormatter.formatForAttribute(thisarg.getName()) + ";");
			}
			sg.wl("		}");
			for (int i = 0; i < outputarguments.getSize(); i++) {
				ArgumentContent thisarg = outputarguments.get(i);
				sg.wl("		public " + thisarg.getType() + " get"
						+ StringFormatter.formatForJavaClass(thisarg.getName()) + "() {");
				sg.wl("			return this." + StringFormatter.formatForAttribute(thisarg.getName()) + ";");
				sg.wl("		}");
			}

			sg.wl("		private void setAddress(String address) {");
			sg.wl("			this.address = address;");
			sg.wl("		}");
			sg.wl("		private String getAddress() {");
			sg.wl("			return this.address;");
			sg.wl("		}			");

			sg.wl("	}");
		}
		String outputattribute = "void";
		String inputattribute = "";
		if (isoutputobject) {
			if (severaloutputobjects) {
				outputattribute = "Abs" + actionclass + "Action.ActionOutputData";
				inputattribute = outputattribute + " logicoutput";
			} else {
				outputattribute = this.outputarguments.get(0).getType();
				inputattribute = outputattribute + " "
						+ StringFormatter.formatForAttribute(this.outputarguments.get(0).getName());
			}
		}

		sg.w("	public abstract " + outputattribute + " executeActionLogic(");
		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			ArgumentContent thisarg = this.getInputArguments().get(i);
			if (i > 0)
				sg.w(",");
			sg.w("	" + thisarg.getType() + " " + StringFormatter.formatForAttribute(thisarg.getName()));

		}
		sg.wl((this.getInputArguments().getSize() > 0 ? "," : "") + "Function<TableAlias,QueryFilter> datafilter) ;");
		sg.bl();

		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			ArgumentContent thisarg = this.getInputArguments().get(i);
			sg.wl("	public SActionInputDataRef<" + thisarg.getPreciseDataEltTypeName() + "> get"
					+ StringFormatter.formatForJavaClass(thisarg.getName()) + "Ref() {");
			sg.wl("		return ActionExecution.getActionInputDataRef(\"" + thisarg.getName() + "\",new "
					+ thisarg.getPreciseDataEltTypeNameWithArgument() + "," + i + ");");
			sg.wl("	}");
			sg.bl();
			if (thisarg.isOptional()) {
				sg.wl("	public SNullActionInputDataRef<" + thisarg.getPreciseDataEltTypeName() + "> getNull"
						+ StringFormatter.formatForJavaClass(thisarg.getName()) + "Ref() {");
				sg.wl("		return ActionExecution.getNullActionInputDataRef(\"" + thisarg.getName() + "\",new "
						+ thisarg.getPreciseDataEltTypeNameWithArgument() + "," + i + ");");
				sg.wl("	}");
				sg.bl();
			}
		}
		for (int i = 0; i < outputarguments.getSize(); i++) {
			ArgumentContent thisarg = outputarguments.get(i);
			sg.wl("	public SActionOutputDataRef<" + thisarg.getPreciseDataEltTypeName() + "> get"
					+ StringFormatter.formatForJavaClass(thisarg.getName()) + "Ref() {");
			sg.wl("		return ActionExecution.getActionOutputDataRef(\"" + thisarg.getName() + "\",new "
					+ thisarg.getPreciseDataEltTypeNameWithArgument() + "," + i + ");");
			sg.wl("	}");
			sg.bl();
		}

		sg.bl();
		sg.wl("	public abstract SPage choosePage(" + inputattribute + ")  ;");

		sg.wl("	@Override");
		sg.wl("	public SPage executeActionFromGUI(SActionData actionattributes)");
		sg.wl("			 {");

		if (!isoutputobject) {
			// no output attribute
			sg.wl("		validateInputAndExecuteAction(actionattributes,null);");
			sg.wl("		OLcServer.getServer().executeTriggerList();");
			sg.wl("		return choosePage();");
		} else {
			if (severaloutputobjects) {
				// several attributes, use the output object

				sg.wl("		ActionOutputData outputdata = validateInputAndExecuteAction(actionattributes,null);");
				sg.wl("		OLcServer.getServer().executeTriggerList();");
				sg.wl("		SPage page = choosePage(outputdata);");
				if (this.isAddressGenerated(this.getInputArguments())) {
					sg.wl("		if (page!=null) page.setAddress(outputdata.getAddress());");
				}
				sg.wl("		return page;");

//				sg.wl("		return choosePage(validateInputAndExecutAction(actionattributes));");
			} else {
				// only one attribute, use the raw type
				sg.wl("		" + inputattribute + " = validateInputAndExecuteAction(actionattributes,null);");
				sg.wl("		OLcServer.getServer().executeTriggerList();");
				sg.wl("		return choosePage("
						+ StringFormatter.formatForAttribute(this.outputarguments.get(0).getName()) + ");");
			}
		}

		sg.wl("	}");

		sg.wl("");
		sg.wl("	public SPage executeActionFromGUI(SActionData actionattributes,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("			 {");
		if (!isoutputobject) {
			// no output attribute
			sg.wl("		validateInputAndExecuteAction(actionattributes,datafilter);");
			sg.wl("		OLcServer.getServer().executeTriggerList();");
			sg.wl("		 return choosePage();");
		} else {
			if (severaloutputobjects) {
				// several attributes, use the output object

				sg.wl("		ActionOutputData outputdata = validateInputAndExecuteAction(actionattributes,datafilter);");
				sg.wl("		OLcServer.getServer().executeTriggerList();");
				sg.wl("		SPage page = choosePage(outputdata);");

				if (this.isAddressGenerated(this.getInputArguments())) {
					sg.wl("		if (page!=null) page.setAddress(outputdata.getAddress());");
				}
				sg.wl("		return page;");

//				sg.wl("		return choosePage(validateInputAndExecutAction(actionattributes));");
			} else {
				// only one attribute, use the raw type
				sg.wl("		" + inputattribute + " = validateInputAndExecuteAction(actionattributes,datafilter);");
				sg.wl("		OLcServer.getServer().executeTriggerList();");
				sg.wl("		return choosePage("
						+ StringFormatter.formatForAttribute(this.outputarguments.get(0).getName()) + ");");

			}
		}

		sg.wl("	}");
		sg.wl("	");
		sg.wl("	@Override");
		sg.wl("	public SPageData executeInlineAction(SActionData actionattributes)");
		sg.wl("		 {");
		sg.wl("		SPageData returndata = executeInlineAction(actionattributes,null);");
		sg.wl("		if (!this.isPopup()) returndata.setMessage(this.getMessage());");
		sg.wl("		if (this.isPopup()) returndata.setPopupMessage(this.getMessage());");
		sg.wl("		return returndata;");
		sg.wl("	}");
		sg.wl("");

		String outputtype = "ActionOutputData";
		if (isoutputobject)
			if (!severaloutputobjects)
				outputtype = outputarguments.get(0).getType();
		if (!isoutputobject)
			outputtype = "void";
		sg.bl();

		sg.wl("	@Override");
		sg.wl("	public SPageData executeInlineAction(SActionData actionattributes,Function<TableAlias,QueryFilter> datafilter)");
		sg.wl("		 {");

		for (int i = 0; i < this.outputarguments.getSize(); i++) {
			sg.wl(" // drop outputargument " + i + " - " + this.outputarguments.get(i).getName());
		}
		if (isoutputobject) {
			sg.wl("		" + outputtype + " output = validateInputAndExecuteAction(actionattributes,datafilter);");
		} else {
			sg.wl("		validateInputAndExecuteAction(actionattributes,datafilter);");
		}
		sg.wl("		SPageData pagedata = new SPageData();");
		sg.wl("		if (!this.isPopup()) pagedata.setMessage(this.getMessage());");
		sg.wl("		if (this.isPopup()) pagedata.setPopupMessage(this.getMessage());");

		if (isoutputobject) {
			if (this.outputarguments.getSize() == 1) {
				ArgumentContent thisarg = outputarguments.get(0);
				if (thisarg instanceof ArrayArgument) {
					ArrayArgument arrayargument = (ArrayArgument) thisarg;
					sg.wl("		" + arrayargument.getGenericDataEltName() + " resultelement = new ArrayDataElt(\""
							+ arrayargument.getName() + "\",new "
							+ arrayargument.getPayload().getPreciseDataEltTypeNameWithArgument() + ");");
					if (severaloutputobjects) {
						sg.wl("		for (int i=0;i<output.get" + StringFormatter.formatForJavaClass(thisarg.getName())
								+ "().length;i++) {");
						sg.wl("			resultelement.addElement(new "
								+ arrayargument.getPayload().getPreciseDataEltName() + "(\"" + arrayargument.getName()
								+ "\",output.get" + StringFormatter.formatForJavaClass(thisarg.getName()) + "()[i]));");

					} else {
						sg.wl("		for (int i=0;i<output.length;i++) {");
						sg.wl("			resultelement.addElement(new "
								+ arrayargument.getPayload().getPreciseDataEltName() + "(\"" + arrayargument.getName()
								+ "\",output[i]));");
					}
					sg.wl("		}");
				}
				if (thisarg instanceof NodeTreeArgument) {
					NodeTreeArgument treeargument = (NodeTreeArgument) thisarg;
					if (severaloutputobjects) {
						sg.wl("		ObjectTreeDataElt<TObjectDataElt<" + treeargument.getPayload().getType()
								+ ">> resultelement = output.get"
								+ StringFormatter.formatForJavaClass(thisarg.getName())
								+ "().generateObjectTreeDataElt(\"" + thisarg.getName() + "\");");
					} else {
						sg.wl("		ObjectTreeDataElt<TObjectDataElt<" + treeargument.getPayload().getType()
								+ ">> resultelement = output.generateObjectTreeDataElt(\"" + thisarg.getName()
								+ "\");");
					}
				}
				if ((!(thisarg instanceof NodeTreeArgument)) && (!(thisarg instanceof ArrayArgument))) {
					if (severaloutputobjects) {
						sg.wl("		" + thisarg.getPreciseDataEltName() + " resultelement = new  "
								+ thisarg.getPreciseDataEltName() + "(\"" + thisarg.getName() + "\",output.get"
								+ StringFormatter.formatForJavaClass(thisarg.getName()) + "());");
					} else {
						sg.wl("		" + thisarg.getPreciseDataEltName() + " resultelement = new  "
								+ thisarg.getPreciseDataEltName() + "(\"" + thisarg.getName() + "\",output);");
					}
				}

				sg.wl("		pagedata.addDataElt(resultelement);");
			}

		}

		sg.wl("		return pagedata;");
		sg.wl("	}");

		sg.bl();
		sg.wl("	private " + outputtype
				+ " validateInputAndExecuteAction(SActionData actionattributes,Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		if (actionattributes.size()!=" + this.getInputArguments().getSize()
				+ ") throw new RuntimeException(String.format(\"action " + this.getName() + " is supposed to have "
				+ this.getInputArguments().getSize()
				+ " attributes, but %d attribute(s) was found\",actionattributes.size()));");

		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			boolean nullallowed = false;
			ArgumentContent thisarg = this.getInputArguments().get(i);
			sg.wl("		" + thisarg.getType() + " " + StringFormatter.formatForAttribute(thisarg.getName())
					+ " = null;");
			if (thisarg instanceof ArrayArgument) {
				sg.wl("		if (actionattributes.getAttribute(" + i + ") instanceof ArrayDataElt) {");
				sg.wl("			ArrayDataElt attribute" + i + " = (ArrayDataElt) actionattributes.getAttribute(" + i
						+ ");");

			} else {
				sg.wl("		if (actionattributes.getAttribute(" + i + ") instanceof " + thisarg.getGenericDataEltName()
						+ ") {");
				sg.wl("			" + thisarg.getGenericDataEltName() + " attribute" + i + " = ("
						+ thisarg.getGenericDataEltName() + ") actionattributes.getAttribute(" + i + ");");

			}
			boolean treated = false;
			if (thisarg instanceof StringArgument) {
				sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName() + "\")==0) "
						+ StringFormatter.formatForAttribute(thisarg.getName()) + " = attribute" + i
						+ ".getPayload();");
				treated = true;
			}
			if (thisarg instanceof TimestampArgument) {
				nullallowed = true;
				sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName() + "\")==0) "
						+ StringFormatter.formatForAttribute(thisarg.getName()) + " = attribute" + i
						+ ".getPayload();");
				treated = true;
			}
			if (thisarg instanceof ObjectArgument) {
				ObjectArgument thisobjectarg = (ObjectArgument) thisarg;
				sg.wl("		if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName() + "\")==0) {");
				if (thisobjectarg.getMasterObject().isUniqueIdentified()) {
					sg.wl("			if (attribute" + i + ".getUID().length()>0) "
							+ StringFormatter.formatForAttribute(thisarg.getName()) + " = " + thisarg.getType()
							+ ".readone(DataObjectId.generatefromDataObjectElt(attribute" + i + "," + thisarg.getType()
							+ ".getDefinition()));");
					sg.wl("			if (attribute" + i + ".getUID().length()==0) "
							+ StringFormatter.formatForAttribute(thisarg.getName()) + " = new " + thisarg.getType()
							+ "();");
				} else {
					sg.wl("			" + StringFormatter.formatForAttribute(thisarg.getName()) + " = new "
							+ thisarg.getType() + "();");
				}
				sg.wl("			" + StringFormatter.formatForAttribute(thisarg.getName())
						+ ".updateFromObjectContent(attribute" + i + ");");
				sg.wl("		}");
				treated = true;
			}
			if (thisarg instanceof ObjectIdArgument) {
				ObjectIdArgument thisobjectidarg = (ObjectIdArgument) thisarg;
				if (thisobjectidarg.getObject() != null) {
					sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName() + "\")==0) "
							+ StringFormatter.formatForAttribute(thisarg.getName())
							+ " = DataObjectId.generatefromDataObjectIdElt(attribute" + i + ","
							+ thisobjectidarg.getObjectType() + ".getDefinition());");
				} else {
					sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName() + "\")==0) "
							+ StringFormatter.formatForAttribute(thisarg.getName())
							+ " = DataObjectId.generatefromDataObjectIdElt(attribute" + i + ");");

				}
				treated = true;
			}
			if (thisarg instanceof ChoiceArgument) {
				// this argument has dedicated logic as it allows null value. Maybe this needs
				// to be extended to other fields.
				nullallowed = true;
				ChoiceArgument thischoicearg = (ChoiceArgument) thisarg;
				sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName().toUpperCase()
						+ "\")!=0) throw new RuntimeException(String.format( \" was expecting a " + thisarg.getType()
						+ " attribute called " + thisarg.getName() + " as attribute " + i + " of action "
						+ this.getName() + ", got %s \",actionattributes.getAttribute(" + i + ")));");
				sg.wl("			if (attribute" + i + ".getStoredValue()!=null) if (attribute" + i
						+ ".getStoredValue().length()>0) {");
				sg.wl("				" + thisarg.getName().toLowerCase() + " = " + thischoicearg.getChoiceCategoryClass()
						+ ".get().parseValueFromStorageCode(attribute" + i + ".getStoredValue());");
				sg.wl("				if (" + StringFormatter.formatForAttribute(thisarg.getName())
						+ " == null) throw new RuntimeException(String.format( \" was expecting a " + thisarg.getType()
						+ " attribute called " + thisarg.getName() + " as attribute " + i + " of action "
						+ this.getName() + ", got %s \",actionattributes.getAttribute(" + i + ")));");
				sg.wl("			}");
				treated = true;
			}

			if (thisarg instanceof MultipleChoiceArgument) {
				MultipleChoiceArgument thismultipliechoicearg = (MultipleChoiceArgument) thisarg;
				nullallowed = true;
				sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName().toUpperCase()
						+ "\")==0) " + thisarg.getName().toLowerCase() + " = "
						+ thismultipliechoicearg.getChoiceCategoryClass() + ".get().parseMultipleChoiceElt(attribute"
						+ i + ");");
				treated = true;
			}

			if (thisarg instanceof LargeBinaryArgument) {

				sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName().toUpperCase()
						+ "\")==0) {");
				sg.wl("				" + thisarg.getName().toLowerCase() + " = attribute" + i + ".getPayload();");
				sg.wl("			}");
				treated = true;
			}
			if (thisarg instanceof IntegerArgument) {
				sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName().toUpperCase()
						+ "\")==0) {");
				sg.wl("				" + thisarg.getName().toLowerCase() + " = attribute" + i + ".getPayload();");
				sg.wl("			}");
				treated = true;
			}
			if (thisarg instanceof TimePeriodArgument) {
				sg.wl("			if (attribute" + i + ".getName().compareTo(\"" + thisarg.getName().toUpperCase()
						+ "\")==0) {");
				sg.wl("				" + thisarg.getName().toLowerCase() + " = attribute" + i + ".getPayload();");
				sg.wl("			}");
				treated = true;
			}
			if (thisarg instanceof ArrayArgument) {
				ArrayArgument thisarrayarg = (ArrayArgument) thisarg;
				if (thisarrayarg.getPayload() instanceof ObjectArgument) {
					ObjectArgument thisobjectinarrayarg = (ObjectArgument) thisarrayarg.getPayload();
					String objectvariable = StringFormatter.formatForAttribute(thisobjectinarrayarg.getType());
					String objectclass = StringFormatter.formatForJavaClass(thisobjectinarrayarg.getType());

					sg.wl("if (attribute" + i + ".getArrayPayloadEltType() instanceof ObjectDataEltType) {");
					sg.wl("				ArrayList<" + objectclass + "> inputlist = new  ArrayList<" + objectclass
							+ ">();");
					sg.wl("				for (int i=0;i<attribute" + i + ".getObjectNumber();i++) {");
					sg.wl("					ObjectDataElt objectinarray = (ObjectDataElt) attribute" + i
							+ ".getObjectAtIndex(i);");
					sg.wl("					if (objectinarray.getName().compareTo(\""
							+ thisobjectinarrayarg.getName().toUpperCase() + "\")==0) {");
					sg.wl("						" + objectclass + " current" + objectvariable + " = null;");
					if (thisobjectinarrayarg.getMasterObject().isUniqueIdentified()) {
						sg.wl("						if (objectinarray.getUID().length()>0) current" + objectvariable
								+ " = " + objectclass + ".readone(DataObjectId.generatefromDataObjectElt(objectinarray,"
								+ objectclass + ".getDefinition()));");
						sg.wl("						if (objectinarray.getUID().length()==0) current" + objectvariable
								+ " = new " + objectclass + "();");
					} else {
						sg.wl("						current" + objectvariable + " = new " + objectclass + "();");
					}
					sg.wl("						current" + objectvariable + ".updateFromObjectContent(objectinarray);");
					sg.wl("						inputlist.add(current" + objectvariable + ");");
					sg.wl("					} else {");
					sg.wl("						throw new RuntimeException(\" " + objectvariable
							+ " attribute index \"+i+\"/\"+attribute" + i
							+ ".getObjectNumber()+\" does not have the correct name, was expecting '"
							+ thisobjectinarrayarg.getName().toUpperCase() + "', got \"+objectinarray.getName());");
					sg.wl("					}");
					sg.wl("				}");
					sg.wl("				if (inputlist.size()>0) "
							+ StringFormatter.formatForAttribute(thisarg.getName()) + " = inputlist.toArray(new "
							+ objectclass + "[0]);");
					sg.wl("				if (inputlist.size()==0) "
							+ StringFormatter.formatForAttribute(thisarg.getName()) + " = new " + objectclass + "[0];");
					sg.wl("			}");

					treated = true;
				}
				if (thisarrayarg.getPayload() instanceof ObjectIdArgument) {
					ObjectIdArgument thisobjectidinarray = (ObjectIdArgument) thisarrayarg.getPayload();
					String argumentname = thisobjectidinarray.getName().toLowerCase();

					sg.wl("		if (attribute" + i + ".getArrayPayloadEltType() instanceof ObjectIdDataEltType) {");
					sg.wl("				ArrayList<DataObjectId<"
							+ StringFormatter.formatForJavaClass(thisobjectidinarray.getObjectType()) + ">> listfor"
							+ argumentname + " = new ArrayList<DataObjectId<"
							+ StringFormatter.formatForJavaClass(thisobjectidinarray.getObjectType()) + ">>();");
					sg.wl("				for (int i=0;i<attribute" + i + ".getObjectNumber();i++) {");
					sg.wl("					ObjectIdDataElt thisobjectid = (ObjectIdDataElt) attribute" + i
							+ ".getObjectAtIndex(i);");
					sg.wl("					if (thisobjectid.getName().compareTo(\"" + argumentname.toUpperCase()
							+ "\")==0) {");
					sg.wl("					listfor" + argumentname
							+ ".add(DataObjectId.generatefromDataObjectIdElt(thisobjectid,"
							+ StringFormatter.formatForJavaClass(thisobjectidinarray.getObjectType())
							+ ".getDefinition()));");
					sg.wl("					} else {");
					sg.wl("						throw new RuntimeException(String.format(\"was expecting an objectid attribute inside array called "
							+ argumentname.toUpperCase() + " as attribute " + i + " of action " + this.getName()
							+ ", got %s \",thisobjectid.getName()));");
					sg.wl("					}");
					sg.wl("				}");
					sg.wl("				" + argumentname + " = (DataObjectId<"
							+ StringFormatter.formatForJavaClass(thisobjectidinarray.getObjectType()) + ">[]) listfor"
							+ argumentname + ".toArray(new DataObjectId[0]);");
					sg.wl("			} else {");
					sg.wl("				throw new RuntimeException(String.format(\"was expecting an ObjectId inside array called "
							+ argumentname.toUpperCase() + " as attribute " + i + " of action " + this.getName()
							+ ", got %s \",attribute" + i + ".getArrayPayloadEltType() ));");
					sg.wl("				");
					sg.wl("			}				");

					treated = true;
				}
				
				if (thisarrayarg.getPayload() instanceof ObjectMasterIdArgument) {
					ObjectMasterIdArgument thisobjectidinarray = (ObjectMasterIdArgument) thisarrayarg.getPayload();
					String argumentname = thisobjectidinarray.getName().toLowerCase();

					sg.wl("		if (attribute" + i + ".getArrayPayloadEltType() instanceof ObjectMasterIdDataEltType) {");
					sg.wl("				ArrayList<DataObjectMasterId<"
							+ StringFormatter.formatForJavaClass(thisobjectidinarray.getObjectType()) + ">> listfor"
							+ argumentname + " = new ArrayList<DataObjectMasterId<"
							+ StringFormatter.formatForJavaClass(thisobjectidinarray.getObjectType()) + ">>();");
					sg.wl("				for (int i=0;i<attribute" + i + ".getObjectNumber();i++) {");
					sg.wl("					ObjectMasterIdDataElt thisobjectid = (ObjectMasterIdDataElt) attribute" + i
							+ ".getObjectAtIndex(i);");
					sg.wl("					if (thisobjectid.getName().compareTo(\"" + argumentname.toUpperCase()
							+ "\")==0) {");
					sg.wl("					listfor" + argumentname
							+ ".add(DataObjectMasterId.generatefromDataObjectMasterIdElt(thisobjectid,"
							+ StringFormatter.formatForJavaClass(thisobjectidinarray.getObjectType())
							+ ".getDefinition()));");
					sg.wl("					} else {");
					sg.wl("						throw new RuntimeException(String.format(\"was expecting an objectid attribute inside array called "
							+ argumentname.toUpperCase() + " as attribute " + i + " of action " + this.getName()
							+ ", got %s \",thisobjectid.getName()));");
					sg.wl("					}");
					sg.wl("				}");
					sg.wl("				" + argumentname + " = (DataObjectMasterId<"
							+ StringFormatter.formatForJavaClass(thisobjectidinarray.getObjectType()) + ">[]) listfor"
							+ argumentname + ".toArray(new DataObjectMasterId[0]);");
					sg.wl("			} else {");
					sg.wl("				throw new RuntimeException(String.format(\"was expecting an ObjectMasterId inside array called "
							+ argumentname.toUpperCase() + " as attribute " + i + " of action " + this.getName()
							+ ", got %s \",attribute" + i + ".getArrayPayloadEltType() ));");
					sg.wl("				");
					sg.wl("			}				");

					treated = true;
				}				
				
				if (thisarrayarg.getPayload() instanceof ChoiceArgument) {
					ChoiceArgument thischoiceinarrayarg = (ChoiceArgument) thisarrayarg.getPayload();
					String argumentname = thischoiceinarrayarg.getName().toLowerCase();

					sg.wl("		if (attribute" + i + ".getArrayPayloadEltType() instanceof ChoiceDataEltType) {");
					sg.wl("				ArrayList<ChoiceValue<" + thischoiceinarrayarg.getChoiceCategoryClass()
							+ ">> listfor" + argumentname + " = new ArrayList<ChoiceValue<"
							+ thischoiceinarrayarg.getChoiceCategoryClass() + ">>();");
					sg.wl("				for (int i=0;i<attribute" + i + ".getObjectNumber();i++) {");
					sg.wl("					ChoiceDataElt thischoice = (ChoiceDataElt) attribute" + i
							+ ".getObjectAtIndex(i);");
					sg.wl("					if (thischoice.getName().compareTo(\"" + argumentname.toUpperCase()
							+ "\")==0) {");
					sg.wl("					listfor" + argumentname + ".add("
							+ thischoiceinarrayarg.getChoiceCategoryClass()
							+ ".get().parseValueFromStorageCode(thischoice.getStoredValue()));");
					sg.wl("					} else {");
					sg.wl("						throw new RuntimeException(String.format(\"was expecting a Choice Attribute inside array called "
							+ argumentname.toUpperCase() + " as attribute " + i + " of action " + this.getName()
							+ ", got %s \",thischoice.getName()));");
					sg.wl("					}");
					sg.wl("				}");
					sg.wl("				" + argumentname + " = listfor" + argumentname
							+ ".toArray(new ChoiceValue[0]);");
					sg.wl("			} else {");
					sg.wl("				throw new RuntimeException(String.format(\"was expecting a Choice Attribute inside array called "
							+ argumentname.toUpperCase() + " as attribute " + i + " of action " + this.getName()
							+ ", got %s \",attribute" + i + ".getArrayPayloadEltType() ));");
					sg.wl("				");
					sg.wl("			}				");

					treated = true;
				}
				
				if (thisarrayarg.getPayload() instanceof StringArgument) {
					StringArgument stringargument = (StringArgument) thisarrayarg.getPayload();
					String argumentname = stringargument.getName().toLowerCase();

					sg.wl("		if (attribute" + i + ".getArrayPayloadEltType() instanceof TextDataEltType) {");
					sg.wl("				ArrayList<String> listfor" + argumentname + " = new ArrayList<String>();");
					sg.wl("				for (int i=0;i<attribute" + i + ".getObjectNumber();i++) {");
					sg.wl("					TextDataElt thistextelt = (TextDataElt) attribute" + i
							+ ".getObjectAtIndex(i);");
					sg.wl("					if (thistextelt.getName().compareTo(\"" + argumentname.toUpperCase()
							+ "\")==0) {");
					sg.wl("					listfor" + argumentname + ".add(thistextelt.getPayload());");
					sg.wl("					} else {");
					sg.wl("						throw new RuntimeException(String.format(\"was expecting a TextData element inside array called "
							+ argumentname.toUpperCase() + " as attribute " + i + " of action " + this.getName()
							+ ", got %s \",thistextelt.getName()));");
					sg.wl("					}");
					sg.wl("				}");
					sg.wl("				" + argumentname + " = listfor" + argumentname
							+ ".toArray(new String[0]);");
					sg.wl("			} else {");
					sg.wl("				throw new RuntimeException(String.format(\"was expecting a Text Attribute inside array called "
							+ argumentname.toUpperCase() + " as attribute " + i + " of action " + this.getName()
							+ ", got %s \",attribute" + i + ".getArrayPayloadEltType() ));");
					sg.wl("				");
					sg.wl("			}				");
					
					treated = true;
				}
				
			}

			if (!treated)
				sg.wl(" #WARNING#, format not supported " + thisarg.getClass());

			sg.wl("		}");
			sg.wl("		");
			if (!nullallowed)
				sg.wl("		if (" + StringFormatter.formatForAttribute(thisarg.getName())
						+ " == null) throw new RuntimeException(String.format( \" was expecting a " + thisarg.getType()
						+ " attribute called " + thisarg.getName() + " as attribute " + i + " of action "
						+ this.getName() + ", got %s \",actionattributes.getAttribute(" + i + ")));");
			sg.wl("		");
		}
		String allinputarguments = "";
		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			ArgumentContent thisarg = this.getInputArguments().get(i);
			if (i > 0)
				allinputarguments += ",";
			allinputarguments += StringFormatter.formatForAttribute(thisarg.getName());
		}
		if (outputtype.compareTo("void") == 0) {
			sg.wl("		executeActionLogic(" + allinputarguments + (this.getInputArguments().getSize() > 0 ? "," : "")
					+ "datafilter);");
		} else {
			if (this.isAddressGenerated(this.getInputArguments())) {

				sg.wl("		ActionOutputData outputdata = executeActionLogic(" + allinputarguments
						+ (this.getInputArguments().getSize() > 0 ? "," : "") + "datafilter);");
				sg.wl("		outputdata.setAddress(generateAddress(" + allinputarguments + "));");
				sg.wl("		return outputdata;	");

			} else {
				sg.wl("		return executeActionLogic(" + allinputarguments
						+ (this.getInputArguments().getSize() > 0 ? "," : "") + "datafilter);");
			}
		}

		sg.wl("	}");

		sg.w("		public SPage executeAndShowPage(");
		for (int i = 0; i < this.getInputArguments().getSize(); i++) {
			ArgumentContent thisarg = this.getInputArguments().get(i);
			if (i > 0)
				sg.w(",");
			sg.w("	" + thisarg.getType() + " " + StringFormatter.formatForAttribute(thisarg.getName()));

		}
		sg.wl(")  {");
		if (this.isAddressGenerated(this.getInputArguments())) {

			sg.wl("		ActionOutputData outputdata = executeActionLogic(" + allinputarguments
					+ (allinputarguments.length() > 0 ? "," : "") + "null);");
			sg.wl("		outputdata.setAddress(generateAddress(" + allinputarguments + "));");

			sg.wl("		SPage page = choosePage(outputdata);");

			sg.wl("		page.setAddress(outputdata.getAddress());");

			sg.wl("		return page;");
		} else {
			if (isoutputobject) {
				sg.wl("		return choosePage(executeActionLogic(" + allinputarguments
						+ (allinputarguments.length() > 0 ? "," : "") + "null));");
			} else {
				sg.wl("		executeActionLogic(" + allinputarguments + (allinputarguments.length() > 0 ? "," : "")
						+ "null);");
				sg.wl("		return choosePage();");
			}
		}
		sg.wl("	}		");

		// ******************* Security manager *********************************

		// deduplicating the privileges
		HashMap<String, Privilege> uniqueprivileges = new HashMap<String, Privilege>();

		if (privilegesforaction != null)
			for (int i = 0; i < privilegesforaction.length; i++) {
				Privilege thisprivilege = privilegesforaction[i];
				if (uniqueprivileges.get(thisprivilege.getSecurityManagerName()) == null) {
					uniqueprivileges.put(thisprivilege.getSecurityManagerName(), thisprivilege);
				} else {
					if (!uniqueprivileges.get(thisprivilege.getSecurityManagerName()).equals(thisprivilege))
						throw new RuntimeException("Two privileges have the same name but different content "
								+ thisprivilege.getSecurityManagerName() + " - " + thisprivilege.getClass().toString());
				}
			}

		Privilege[] uniqueprivilegearray = uniqueprivileges.values().toArray(new Privilege[0]);

		if (uniqueprivilegearray != null)
			for (int i = 0; i < uniqueprivilegearray.length; i++) {
				Privilege thisprivilege = uniqueprivilegearray[i];
				thisprivilege.writeDefinition(sg, this);
			}

		sg.wl("		@Override");
		sg.wl("		public ActionSecurityManager[] getActionSecurityManager() {");
		sg.wl("			return new ActionSecurityManager[] {");

		if (uniqueprivilegearray != null)
			for (int i = 0; i < uniqueprivilegearray.length; i++) {
				Privilege thisprivilege = uniqueprivilegearray[i];
				sg.wl("			" + (i > 0 ? "," : "") + thisprivilege.getSecurityManagerName());
			}
		sg.wl("				};");
		sg.wl("		}");

		if (this.getSecurityobjectargument() != null) {
			sg.wl("	public String getObjectAttributeName() {");
			sg.wl("		return \"" + this.getSecurityobjectargument().getName() + "\";");
			sg.wl("	}");

		}
		// ***************** Address generation ******************
		if (this.isAddressGenerated(this.getInputArguments())) {

			sg.w("		public String generateAddress(");
			for (int i = 0; i < this.getInputArguments().getSize(); i++) {
				ArgumentContent thisarg = this.getInputArguments().get(i);
				if (i > 0)
					sg.w(",");
				sg.w("	" + thisarg.getType() + " " + StringFormatter.formatForAttribute(thisarg.getName()));

			}
			sg.wl(")  {");
			sg.wl("			StringBuffer address = new StringBuffer();");
			sg.wl("			address.append(\"" + this.getModule().getName().toUpperCase() + "\"); // module");
			sg.wl("			address.append('.');");
			sg.wl("			address.append(this.getName());");
			if (this.getInputArguments().getSize() > 0) {
				sg.wl("			address.append(':');");
			}
			for (int i = 0; i < this.getInputArguments().getSize(); i++) {
				if (i > 0)
					sg.wl("			address.append(',');");
				ArgumentContent thisargument = this.getInputArguments().get(i);
				sg.wl("			address.append(\"" + thisargument.getName().toUpperCase() + "\");");
				sg.wl("			address.append('=');");
				sg.wl("			address.append(MessageStringField.serializeStringPayload("
						+ StringFormatter.formatForAttribute(thisargument.getName()) + ".getId(),null));");

			}

			sg.wl("			return address.toString();");
			sg.wl("		}			");

		}

		sg.wl("}");
		sg.close();
	}
}
