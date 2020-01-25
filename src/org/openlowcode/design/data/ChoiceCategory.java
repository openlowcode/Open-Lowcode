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

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.Named;

/**
 * A choice category defines a list of items that can be used as fields of
 * objects or to support properties. An example of choice category would be
 * "Size" consisting of elements S, M, L, XL.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ChoiceCategory
		extends
		Named {

	private int keystoragelength;

	/**
	 * @return the key storage length (maximum length of the code of each choice
	 *         value)
	 */
	public int getKeyStorageLength() {
		return this.keystoragelength;
	}

	private boolean pseudonumber;

	/**
	 * Pseudo-number choice values will have a function to give an integer value to
	 * each choice, and order them by number1
	 * 
	 * @return true if choice category is pseudo-number
	 */
	public boolean isPseudoNumber() {
		return pseudonumber;
	}

	/**
	 * This method checks what is the longest label in the choice values, and
	 * returns that value, or the value of label length if higher
	 * 
	 * @param labellength specifying the maximum of the labels of the choice values,
	 *                    and the input length provided (typically for column title)
	 * @return the length to use for column width
	 */
	public abstract int getDisplayLabelLength(int labellength);

	private Module parentmodule;

	/**
	 * @return the parent module for this choice.
	 */
	public Module getParentModule() {
		return this.parentmodule;
	}

	/**
	 * sets the parent module for this choice
	 * 
	 * @param parentmodule module the choice category is declared on1
	 */
	public void setParentModule(Module parentmodule) {
		this.parentmodule = parentmodule;
	}

	/**
	 * creates a choice category with the provided name and key storage length
	 * 
	 * @param name             a unique name for the module
	 * @param keystoragelength should be bigger than the length of the biggest
	 *                         storage key
	 */
	public ChoiceCategory(String name, int keystoragelength) {
		super(name);
		this.keystoragelength = keystoragelength;
		this.pseudonumber = false;
	}

	/**
	 * creates a choice category with the provided name and key storage length,
	 * precision if the choice category should have the pseudo-number option
	 * 
	 * @param name             a unique name for the module
	 * @param keystoragelength should be bigger than the length of the biggest
	 *                         storage key
	 * @param pseudonumber     true if there should be a way to transform each value
	 *                         in an integer
	 */
	public ChoiceCategory(String name, int keystoragelength, boolean pseudonumber) {
		this(name, keystoragelength);
		this.pseudonumber = pseudonumber;
	}

	public abstract String getDefinitionClass();

	/**
	 * * generates the source of the choice category to a file
	 * 
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything nasty happens while writing the file
	 */
	public abstract void generatetoFile(SourceGenerator sg, Module module) throws IOException;

	/**
	 * checks if a choice value with the given key is present
	 * 
	 * @param key the key
	 * @return true if the key is present
	 */
	public abstract boolean isKeyPresent(String key);
}
