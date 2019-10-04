/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.action;

import java.io.IOException;

import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * An Inline Action Data Ref specifies the location on the page of the output of
 * an inline action. <br>
 * Note: an inline action is an action launched on a page that will add or
 * enrich data on the same page
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CInlineActionDataRef {
	private String name;
	private String module;
	private String fieldname;
	private int fieldorder;
	private String type;
	private CPageNode targetnode;

	/**
	 * @param reader     a message reader
	 * @param targetnode the page node on which the inline action data will be added
	 *                   after the inline action is launched
	 * @throws OLcRemoteException
	 * @throws IOException
	 */
	public CInlineActionDataRef(MessageReader reader, CPageNode targetnode) throws OLcRemoteException, IOException {
		reader.returnNextStartStructure("INLINEACTION");
		this.name = reader.returnNextStringField("NAME");
		this.module = reader.returnNextStringField("MODULE");
		reader.returnNextStartStructure("RELFLD");
		reader.returnNextStartStructure("INLOUTPUT");
		this.fieldname = reader.returnNextStringField("NAM");
		this.fieldorder = reader.returnNextIntegerField("ORD");
		this.type = reader.returnNextStringField("TYP");
		reader.returnNextEndStructure("INLOUTPUT");
		reader.returnNextEndStructure("RELFLD");
		reader.returnNextEndStructure("INLINEACTION");
		this.targetnode = targetnode;
	}

	/**
	 * @return action name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return module of the action to call
	 */
	public String getModule() {
		return module;
	}

	/**
	 * @return name  of the field
	 */
	public String getFieldname() {
		return fieldname;
	}

	/**
	 * @return order of the field (starting at 0)
	 */
	public int getFieldorder() {
		return fieldorder;
	}

	/**
	 * @return the type
	 */
	public String getType() {
		return type;
	}

	/**
	 * @return the node that this inline action data ref will 
	 */
	public CPageNode getTargetnode() {
		return targetnode;
	}

}
