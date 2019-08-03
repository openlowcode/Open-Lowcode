/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.tools.messages;

/**
 * A message can be made of structures, structures themselves holding
 * elements, such as fields, other structures or compact arrays
 * @author Open Lowcode SAS
 *
 */
public class MessageStartStructure extends MessageElement {
	private String structurename;
	private static String START_STRUCTURE = "[";
	public String getStructurename() {
		return structurename;
	}

	
	/**
	 * creates a new message structure with the specified structure name.
	 * @param structurename name of the structure
	 * (this is very recommended to be checked  at 
	 * parsing time.
	 */
	public MessageStartStructure(String structurename) {
		super();
		this.structurename = structurename;
	}

	@Override
	public String serialize(String padding,boolean firstattribute) {
		return "\n"+padding+START_STRUCTURE+structurename;
	}

	@Override
	public String toString() {
	
		return serialize("",false);
	}
	
}
