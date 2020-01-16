/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.migrator;

import java.util.logging.Logger;

import org.openlowcode.module.system.data.Systemattribute;
import org.openlowcode.server.runtime.SModule;
import org.openlowcode.tools.misc.Named;

/**
 * A data migrator will execute a transformation of data following a change of
 * data model. It will typically transfer data from an obsolete property to
 * another property
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class SDataMigrator
		extends
		Named {
	private static Logger logger = Logger.getLogger(SDataMigrator.class.toString());
	public static final String MIGRATOR_INITIATED = "INITIATED";
	public static final String MIGRATOR_EXECUTING = "EXECUTING";
	public static final String MIGRATOR_FINISHED = "FINISHED";

	private SModule parent;

	/**
	 * Creates a data migration with the given name for the given module
	 * 
	 * @param name   name that is unique for the module
	 * @param parent parent module
	 */
	public SDataMigrator(String name, SModule parent) {
		super(name);
		this.parent = parent;
	}

	/**
	 * a unique string describing the migrator
	 * 
	 * @return
	 */
	public abstract String describeMigrator();

	/**
	 * executes normal migration. THe normal migration is typically faster, and will
	 * treat all data, assuming no data has been transfered yet
	 * 
	 * @return the number of rows modified
	 * @throws GalliumException
	 */
	public abstract long executeNormalMigration();

	/**
	 * executes recovery migration. The recovery migration is slower, but will be
	 * robust to the fact some data may have already been transfered
	 * 
	 * @return the number of rows modified
	 * @throws GalliumException
	 */
	public abstract long executeRecoveryMigration();

	/**
	 * executes the migration, logging the result in a system parameter
	 */
	public void executeMigration() {
		long starttime = System.currentTimeMillis();
		logger.warning(" --- *** --- Starting migrator " + describeMigrator());
		String migratorid = "#MIG." + parent.getName() + "." + this.getName();
		Systemattribute[] migratoridattributes = Systemattribute.getobjectbynumber(migratorid);
		Systemattribute migratoridattribute = null;
		if (migratoridattributes.length == 1)
			migratoridattribute = migratoridattributes[0];
		if (migratoridattribute == null) {
			migratoridattribute = new Systemattribute();
			migratoridattribute.setobjectnumber(migratorid);
			migratoridattribute.setValue(MIGRATOR_INITIATED);
			migratoridattribute.insert();
			logger.info("   Adding attribute " + migratoridattribute);
		}
		if (migratoridattribute.getValue().compareTo(MIGRATOR_FINISHED) == 0) {
			logger.warning(" --- *** --- migrator already executed - nothing to do");
			return;
		}
		long dataupdated = -1;

		if (migratoridattribute.getValue().compareTo(MIGRATOR_INITIATED) == 0) {
			migratoridattribute.setValue(MIGRATOR_EXECUTING);
			migratoridattribute.update();
			dataupdated = executeNormalMigration();
			migratoridattribute.setValue(MIGRATOR_FINISHED);
			migratoridattribute.update();
		}

		if (migratoridattribute.getValue().compareTo(MIGRATOR_EXECUTING) == 0) {
			logger.warning(" --- *** --- Launching migrator recovery for " + describeMigrator() + "");
			dataupdated = executeRecoveryMigration();
			migratoridattribute.setValue(MIGRATOR_FINISHED);
			migratoridattribute.update();
		}

		long endtime = System.currentTimeMillis();
		logger.warning(" --- *** --- Finished migrator " + describeMigrator() + " in " + ((endtime - starttime) / 1000)
				+ "s, dataupdated " + dataupdated + " rows");

	}

}
