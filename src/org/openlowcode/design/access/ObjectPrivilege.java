/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.access;

import java.io.IOException;

import org.openlowcode.design.action.ActionDefinition;
import org.openlowcode.design.data.ArgumentContent;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;

/**
 * an object privilege is a privilege given depending on the data on the object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ObjectPrivilege
		extends
		AuthorityPrivilege {
	private String classname;
	private String attributename;
	private DataObjectDefinition objectforprivilege;

	/**
	 * @return the attribute name for this privilege for code generation
	 */
	public abstract String generateAttributeName();

	@Override
	public String getSecurityManagerName() {
		return attributename;
	}

	/**
	 * @return get the object definition used for the privilege
	 */
	public DataObjectDefinition getObjectForPrivilege() {
		return objectforprivilege;
	}

	/**
	 * creates an object privilege for the given action group and authority
	 * 
	 * @param actiongroup action group the privilege grants access to
	 * @param authority   authority the privilege is granted to
	 */
	public ObjectPrivilege(ActionGroup actiongroup, Authority authority) {
		super(actiongroup, authority);

	}

	/**
	 * @return the class name for the privilege
	 */
	public abstract String generateClassName();

	/**
	 * @return the class name of the security manager
	 */
	public abstract String getSecurityManagerClassName();

	/**
	 * validates that the privilege is correct
	 */
	public void validate() {
		this.classname = generateClassName();
		this.attributename = generateAttributeName();
		ActionDefinition[] actions = this.getActiongroup().getActionsInGroup();
		if (actions != null)
			for (int i = 0; i < actions.length; i++) {
				ActionDefinition action = actions[i];
				if (objectforprivilege == null) {
					if (action.getAccessCriteria() != null) {
						DataObjectDefinition actionobject = action.getAccessCriteria().getMasterObject();
						if (actionobject == null)
							throw new RuntimeException("Cannot add object privilege for an action " + action.getName()
									+ " without object safety criteria , however argument "
									+ action.getAccessCriteria().getName() + " does not have a master object");
						objectforprivilege = actionobject;
					}
				}
				if (action.getAccessCriteria() != null)
					if (objectforprivilege != null)
						if (!objectforprivilege.equals(action.getAccessCriteria().getMasterObject()))
							throw new RuntimeException("inconsistent objects for action group "
									+ objectforprivilege.getName() + " and " + action.getAccessCriteria().getName());
			}
	}

	/**
	 * generates the super statement for the security manager
	 * 
	 * @param sg source generatio
	 * @throws IOException if anything bad happens while writing the field
	 */
	public abstract void generateSecurityManagerSuperStatement(SourceGenerator sg) throws IOException;

	@Override
	public void writeDefinition(SourceGenerator sg, ActionDefinition contextaction) throws IOException {
		ArgumentContent getaccesscriteria = contextaction.getAccessCriteria();

		String objectclass = null;
		String objectvariable = null;
		if (getaccesscriteria != null) {
			DataObjectDefinition object = getaccesscriteria.getMasterObject();
			objectclass = StringFormatter.formatForJavaClass(object.getName());
			objectvariable = StringFormatter.formatForAttribute(object.getName());
		}

		int accesscriteriaindex = contextaction.getAccessCriteriaIndex();
		if (contextaction.isAccessCriteriaInput()) {
			if (objectclass != null)
				sg.wl("		private static " + this.getSecurityManagerClassName() + "<" + objectclass + "> "
						+ attributename + " = new " + classname + "();");
			if (objectclass == null)
				sg.wl("		private static " + this.getSecurityManagerClassName() + " " + attributename + " = new "
						+ classname + "();");

			if (objectclass != null)
				sg.wl("		private static class " + classname + " extends " + this.getSecurityManagerClassName() + "<"
						+ objectclass + "> {");
			if (objectclass == null)
				sg.wl("		private static class " + classname + " extends " + this.getSecurityManagerClassName()
						+ " {");

			sg.wl("");
			sg.wl("			public " + classname + "() {");
			this.generateSecurityManagerSuperStatement(sg);

			sg.wl("			");
			sg.wl("			}");
			sg.wl("");
			sg.wl("");

			sg.wl("			@Override");
			sg.wl("			public boolean filterObjectData() {		");
			sg.wl("				return false;");
			sg.wl("			}");
			sg.wl("");

			if (getaccesscriteria == null) {
				sg.wl("			@Override");
				sg.wl("			public DataObject[] getInputObject(SActionData actiondata, SecurityBuffer buffer) {");
				sg.wl("				return null;");
				sg.wl("			}");
				sg.wl("			@Override");
				sg.wl("			public boolean queryObjectData() {	");
				sg.wl("				return false;");
				sg.wl("			}");
			} else

			{
				sg.wl("			@Override");
				sg.wl("			public boolean queryObjectData() {	");
				sg.wl("				return true;");
				sg.wl("			}");
				sg.wl("			@Override");
				sg.wl("			public " + objectclass
						+ "[] getInputObject(SActionData actiondata, SecurityBuffer buffer)  {");

				sg.wl("				if (actiondata==null) return new " + objectclass
						+ "[0]; // only happens for widget visibility");
				boolean implemented = false;

				if (getaccesscriteria instanceof ObjectIdArgument) {
					sg.wl("				DataObjectId<" + objectclass + "> objectid = null;");
					sg.wl("				if (actiondata.getAttribute(" + accesscriteriaindex
							+ ") instanceof ObjectIdDataElt) {");
					sg.wl("					ObjectIdDataElt attribute" + accesscriteriaindex
							+ " = (ObjectIdDataElt) actiondata.getAttribute(" + accesscriteriaindex + ");");
					sg.wl("					if (attribute" + accesscriteriaindex + ".getName().compareTo(\""
							+ getaccesscriteria.getName().toUpperCase()
							+ "\")==0) objectid = DataObjectId.generatefromDataObjectIdElt(attribute"
							+ accesscriteriaindex + "," + objectclass + ".getDefinition());");
					sg.wl("				} else {");
					sg.wl("					if (actiondata.getAttribute(" + accesscriteriaindex
							+ ")!=null) throw new RuntimeException(\"Expects to get an ObjectIdDataElt, got \"+actiondata.getAttribute("
							+ accesscriteriaindex + ").getType());");
					sg.wl("					throw new RuntimeException(\"Could not get an attribute for action \");");
					sg.wl("				}		");

					sg.wl("			if (objectid==null) {			");
					sg.wl("				String exceptionstring = \"action data = \";");
					sg.wl("				for (int i=0;i<actiondata.size();i++) exceptionstring+=(actiondata.getAttribute(i)!=null?\"\"+actiondata.getAttribute(i).getName()+\"/\"+actiondata.getAttribute(i).getType().printType()+\"|\":\"NULL\");");
					sg.wl("				throw new RuntimeException(\"Data Entry '"
							+ getaccesscriteria.getName().toUpperCase()
							+ "' not found for security \"+exceptionstring);	");
					sg.wl("				}");

					sg.wl("				" + objectclass + " " + objectvariable
							+ "inbuffer = buffer.getObject(objectid);");
					sg.wl("				return new " + objectclass + "[]{" + objectvariable + "inbuffer};");
					sg.wl("			}");
					sg.wl("			");
					sg.wl("		}		");
					implemented = true;
				}
				if (getaccesscriteria instanceof ArrayArgument) {
					ArrayArgument arrayargument = (ArrayArgument) getaccesscriteria;
					if (arrayargument.getPayload() instanceof ObjectArgument) {

						sg.wl("				" + objectclass + "[] " + objectvariable + " = null;");
						sg.wl("				if (actiondata.getAttribute(" + accesscriteriaindex
								+ ") instanceof ArrayDataElt) {");
						sg.wl("					ArrayDataElt attribute" + accesscriteriaindex
								+ " = (ArrayDataElt) actiondata.getAttribute(" + accesscriteriaindex + ");");
						sg.wl("					if (attribute" + accesscriteriaindex
								+ ".getArrayPayloadEltType() instanceof ObjectDataEltType) {");
						sg.wl("						ArrayList<" + objectclass + "> inputlist = new  ArrayList<"
								+ objectclass + ">();");
						sg.wl("						for (int i=0;i<attribute" + accesscriteriaindex
								+ ".getObjectNumber();i++) {");
						sg.wl("							ObjectDataElt objectinarray = (ObjectDataElt) attribute"
								+ accesscriteriaindex + ".getObjectAtIndex(i);");
						sg.wl("							if (objectinarray.getName().compareTo(\""
								+ getaccesscriteria.getName().toUpperCase() + "\")==0) {");
						sg.wl("								" + objectclass + " current" + objectvariable + " = null;");
						sg.wl("								if (objectinarray.getUID().length()>0) current"
								+ objectvariable
								+ " =  buffer.getObject(DataObjectId.generatefromDataObjectElt(objectinarray,"
								+ objectclass + ".getDefinition()));");
						sg.wl("								if (objectinarray.getUID().length()==0) current"
								+ objectvariable + " = new " + objectclass + "();");
						sg.wl("								inputlist.add(current" + objectvariable + ");");
						sg.wl("							} else {");
						sg.wl("								throw new RuntimeException(\" " + objectvariable
								+ " attribute does not have the correct name, was expecting '"
								+ getaccesscriteria.getName().toUpperCase() + "', got \"+objectinarray.getName());");
						sg.wl("							}");
						sg.wl("						}");
						sg.wl("						if (inputlist.size()>0) " + objectvariable
								+ " = inputlist.toArray(new " + objectclass + "[0]);");
						sg.wl("					}");
						sg.wl("				}");
						sg.wl("			return " + objectvariable + ";");
						sg.wl("		}");
						sg.wl("	}");
						implemented = true;

					}
					if (arrayargument.getPayload() instanceof ObjectIdArgument) {

						sg.wl("				" + objectclass + "[] " + objectvariable + "s=null;");
						sg.wl("				if (actiondata.getAttribute(" + accesscriteriaindex
								+ ") instanceof ArrayDataElt) {");
						sg.wl("					ArrayList<" + objectclass + "> " + objectvariable
								+ "list = new ArrayList<" + objectclass + ">();");
						sg.wl("					ArrayDataElt attribute" + accesscriteriaindex
								+ " = (ArrayDataElt) actiondata.getAttribute(" + accesscriteriaindex + ");");
						sg.wl("					if (attribute" + accesscriteriaindex
								+ ".getArrayPayloadEltType() instanceof ObjectIdDataEltType) {");
						sg.wl("						ArrayList<DataObjectId<" + objectclass + ">> listforleft"
								+ objectvariable + "id = new ArrayList<DataObjectId<" + objectclass + ">>();");
						sg.wl("						for (int i=0;i<attribute" + accesscriteriaindex
								+ ".getObjectNumber();i++) {");
						sg.wl("							ObjectIdDataElt thisobjectid = (ObjectIdDataElt) attribute"
								+ accesscriteriaindex + ".getObjectAtIndex(i);");
						sg.wl("							if (thisobjectid.getName().compareTo(\""
								+ getaccesscriteria.getName().toUpperCase() + "\")==0) {");
						sg.wl("								" + objectvariable
								+ "list.add(buffer.getObject(DataObjectId.generatefromDataObjectIdElt(thisobjectid,"
								+ objectclass + ".getDefinition())));");
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
								+ accesscriteriaindex + ".getArrayPayloadEltType()));");
						sg.wl("						");
						sg.wl("					}	");
						sg.wl("					" + objectvariable + "s = " + objectvariable + "list.toArray(new "
								+ objectclass + "[0]);");
						sg.wl("				}");
						sg.wl("				");
						sg.wl("				if (" + objectvariable
								+ "s == null) throw new RuntimeException(String.format( \" was expecting a DataObjectId<"
								+ objectclass + ">[] attribute called " + getaccesscriteria.getName().toUpperCase()
								+ " as attribute 0, got %s \",actiondata.getAttribute(0)));");
						sg.wl("				return " + objectvariable + "s;");
						sg.wl("			}");
						sg.wl("		}");

						implemented = true;
					}
				}
				if (getaccesscriteria instanceof ObjectArgument) {

					sg.wl("				" + objectclass + " " + objectvariable + " = null;");
					sg.wl("				if (actiondata.getAttribute(" + accesscriteriaindex
							+ ") instanceof ObjectDataElt) {");
					sg.wl("					ObjectDataElt attribute" + accesscriteriaindex
							+ " = (ObjectDataElt) actiondata.getAttribute(" + accesscriteriaindex + ");");
					sg.wl("				if (attribute" + accesscriteriaindex + ".getName().compareTo(\""
							+ getaccesscriteria.getName().toUpperCase() + "\")==0) {");
					sg.wl("					if (attribute" + accesscriteriaindex + ".getUID().length()>0) "
							+ objectvariable + " = buffer.getObject(DataObjectId.generatefromDataObjectElt(attribute"
							+ accesscriteriaindex + "," + objectclass + ".getDefinition()));");
					sg.wl("					if (attribute" + accesscriteriaindex + ".getUID().length()==0) "
							+ objectvariable + " = new " + objectclass + "();");
					sg.wl("				}");
					sg.wl("				}");
					sg.wl("				");
					sg.wl("				if (" + objectvariable
							+ " == null) throw new RuntimeException(String.format( \" was expecting a " + objectclass
							+ " attribute called " + getaccesscriteria.getName().toUpperCase() + " as attribute "
							+ accesscriteriaindex + " of action , got %s \",actiondata.getAttribute("
							+ accesscriteriaindex + ")));");
					sg.wl("				return new " + objectclass + "[]{" + objectvariable + "};			");
					sg.wl("			}");
					sg.wl("			");
					sg.wl("		}		");
					implemented = true;
				}

				if (!implemented)
					sg.wl("  ### Type not supported yet in generation " + getaccesscriteria.getClass().getName());
			}
		} else {
			sg.wl("		private static " + this.getSecurityManagerClassName() + " " + attributename + " = new "
					+ classname + "();");
			sg.wl("		private static class " + classname + " extends " + this.getSecurityManagerClassName() + " {");
			sg.wl("			public " + classname + "() {");
			this.generateSecurityManagerSuperStatement(sg);

			sg.wl("			}");
			sg.wl("			public DataObject[] getInputObject(SActionData actiondata,SecurityBuffer buffer)  {");
			sg.wl("				return null;");
			sg.wl("			}");
			sg.wl("			@Override");
			sg.wl("			public boolean filterObjectData() {		");
			sg.wl("				return true;");
			sg.wl("			}");
			sg.wl("			@Override");
			sg.wl("			public boolean queryObjectData() {	");
			sg.wl("				return false;");
			sg.wl("			}");
			sg.wl("		}");
		}

	}
}
