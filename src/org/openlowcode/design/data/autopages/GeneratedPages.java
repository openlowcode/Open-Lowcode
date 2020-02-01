/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.autopages;

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * An interface for classes that generate pages
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public interface GeneratedPages {
	/**
	 * generates the file of the page
	 * 
	 * @param sg     source generator
	 * @param module parent module of the page
	 * @throws IOException if anything bad happens writing the code
	 */
	public void generateToFile(SourceGenerator sg, Module module) throws IOException;
}
