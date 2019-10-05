/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.runtime;

/**
 * Tools used by classes in the client
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class ClientTools {
	/**
	 * @return a text summarized memory statement of the client, it is used to detect memory links
	 */
	public static String memoryStatement() {
		StringBuffer memorystatement = new StringBuffer();
		long usedmemoryinmb=((Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory())/(1024*1024));
		long pcused = (100*usedmemoryinmb/(Runtime.getRuntime().maxMemory()/(1024*1024)));
		memorystatement.append(", memory: ");
		memorystatement.append(usedmemoryinmb);
		memorystatement.append("mb (");
		memorystatement.append(pcused);
		memorystatement.append("%) ");
	
		return memorystatement.toString();
	}
}
