/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.stringpattern;

import org.openlowcode.design.data.SequenceDefinition;
import org.openlowcode.design.generation.StringFormatter;

/**
 * create a pattern element taking its content from a sequence definition of the
 * module
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SequenceElement
		extends
		PatternElement {
	private SequenceDefinition sequencedefinition;

	/**
	 * creates a sequence pattern element
	 * 
	 * @param sequencedefinition definition of the sequence
	 */
	public SequenceElement(SequenceDefinition sequencedefinition) {
		this.sequencedefinition = sequencedefinition;
	}

	@Override
	public String generateSource() {
		return StringFormatter.formatForJavaClass(sequencedefinition.getName()) + "Sequence.get().getNextValue()";
	}

	@Override
	public String[] generateImport() {
		String[] importlist = new String[1];
		importlist[0] = "import " + sequencedefinition.getParentModule().getPath() + ".data.sequence."
				+ StringFormatter.formatForJavaClass(sequencedefinition.getName()) + "Sequence;";
		return importlist;
	}

}
