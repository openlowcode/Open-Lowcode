/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.module.system.action;

import java.util.function.Function;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.SFile;
import org.openlowcode.module.system.action.generated.AbsGetfileAction;
import org.openlowcode.module.system.data.Binaryfile;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;

import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.SModule;

/**
 * an action to get a file on the server giving a file id
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class GetfileAction
		extends
		AbsGetfileAction {
	private static Logger logger = Logger.getLogger(GetfileAction.class.getName());

	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public GetfileAction(SModule parent) {
		super(parent);

	}

	@Override
	public ActionOutputData executeActionLogic(
			DataObjectId<Binaryfile> fileid,
			Function<TableAlias, QueryFilter> datafilter) {
		logger.info("try to get file id = " + fileid.getId());
		Binaryfile file = Binaryfile.readone(fileid);
		SFile fileobject = new SFile(file.getFilename(), file.getFilecontent().getContent());
		logger.info(
				"found file  name = " + file.getFilename() + " length = " + file.getFilecontent().getContent().length);

		return new ActionOutputData(fileobject);
	}

	@Override
	public SPage choosePage(ActionOutputData logicoutput) {
		return null;
	}

}
