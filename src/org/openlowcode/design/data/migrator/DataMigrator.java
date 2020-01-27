/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.migrator;

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.misc.Named;

/**
 * A data migrator will be executed once to transfer data from old properties
 * (legacy) to new properties
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public abstract class DataMigrator
		extends
		Named {
	/**
	 * @return the name of the migrator. A migrator of a given name is only executed
	 *         once on the server
	 */
	public abstract String getClassName();

	/**
	 * creates a data migrator with the given name
	 * 
	 * @param name unique name of the data migrator
	 */
	public DataMigrator(String name) {
		super(name);

	}

	/**
	 * generates the migrator code to a file
	 * 
	 * @param sg     source generation
	 * @param module module of the migrator
	 * @throws IOException if any issue happens while writing the files
	 */
	public abstract void generateMigratorToFile(SourceGenerator sg, Module module) throws IOException;
}
