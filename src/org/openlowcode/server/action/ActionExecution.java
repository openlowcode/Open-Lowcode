/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.action;

import java.util.function.Function;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.graphic.SPageData;
import org.openlowcode.server.runtime.SModule;
import org.openlowcode.server.security.ActionSecurityManager;
import org.openlowcode.server.security.SecurityBuffer;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.storage.QueryFilter;

/**
 * An ActionExecution class actually executes a request for an action coming
 * from a customer. For each action execution, a new instance of a class is
 * created. Actions
 * <ul>
 * <li>should not use any static attribute during execution as different threads
 * may mix-up</li>
 * <li>the Open Lowcode design studio will generate abstract action classes that
 * should be implemented by the user. No direct subclass of this clas should be
 * implemented directly by a developer</li>
 * </ul>
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class ActionExecution extends Named {

	private String message;
	private boolean popup = false;
	private SModule parent;

	/**
	 * @return the message to show in title
	 */
	public String getMessage() {
		return message;
	}

	/**
	 * @return get the parent module
	 * @since 1.10
	 */
	public SModule getParentModule() {
		return parent;
	}
	
	/**
	 * sets the message to show in title
	 * 
	 * @param message the message to show in title
	 */
	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return true if the action is triggered from a popup on the client screen
	 */
	public boolean isPopup() {
		return popup;
	}

	/**
	 * @param popup if the action is triggered from a popup on the client screen
	 */
	public void setPopup(boolean popup) {
		this.popup = popup;
	}

	/**
	 * @return the parent module
	 */
	public SModule getParent() {
		return this.parent;
	}

	/**
	 * creates a new action execution
	 * 
	 * @param name   unique name of the action for the parent module
	 * @param parent parent module
	 */
	public ActionExecution(String name, SModule parent) {
		super(name);
		this.parent = parent;

	}

	/**
	 * @return -1 if there is no input security argument, the index of the security
	 *         argument (0 or higher)
	 */
	protected abstract int getInputSecurityArgumentIndex();

	/**
	 * this creates an inline action reference that can be linked to a widget in
	 * order to launch an action to launch the page
	 * 
	 * @return the inline action reference
	 */
	protected SInlineActionRef getInlineActionReference() {
		return new SInlineActionRef(this.getName(), this.parent.getName(), getInputSecurityArgumentIndex());
	}

	/**
	 * this creates an action reference that can be linked to a widget in order to
	 * display a new page
	 * 
	 * @return the action reference
	 */
	protected SActionRef getActionReference() {
		return new SActionRef(this.getName(), this.parent.getName(), getInputSecurityArgumentIndex());
	}

	/**
	 * will check according to action manager and freeze objects that are not
	 * modified
	 * 
	 * @param dataarray the array of objects to check
	 */
	public void freezeUnauthorizedObjects(DataObject<?>[] dataarray) {
		ActionSecurityManager[] managers = this.getActionSecurityManager();
		SecurityBuffer securitybuffer = new SecurityBuffer();
		if (dataarray != null)
			for (int i = 0; i < dataarray.length; i++) {
				dataarray[i].setFrozen();
			}
		if (managers != null)
			for (int i = 0; i < managers.length; i++) {
				managers[i].freezeUnauthorizedObjects(dataarray, securitybuffer);
			}

	}

	/**
	 * The action is executed, and its result is shown in a page on the client
	 * 
	 * @param actionattributes the list of attributes
	 * @return the page displayed
	 */
	public abstract SPage executeActionFromGUI(SActionData actionattributes);

	/**
	 * The action is executed, and its result is shown in a page on the client, with
	 * a specified filter for data specified
	 * 
	 * @param actionattributes
	 * @param datafilter
	 * @return
	 */
	public abstract SPage executeActionFromGUI(SActionData actionattributes,
			Function<TableAlias, QueryFilter> datafilter);

	/**
	 * generates an ActionInputDataRef for the action
	 * 
	 * @param name  name of the data reference
	 * @param type  type of the data reference
	 * @param order order of the attribute
	 * @return the generated ActionInputDataRef
	 */
	protected static <T extends DataEltType> SActionInputDataRef<T> getActionInputDataRef(String name, T type,
			int order) {
		return new SActionInputDataRef<T>(name, type, order);

	}

	/**
	 * generates a null action input data ref for the
	 * 
	 * @param name  name of the attribute
	 * @param type  type type of the attribute
	 * @param order order of the attribute
	 * @return a null action input data ref
	 */
	protected static <T extends DataEltType> SNullActionInputDataRef<T> getNullActionInputDataRef(String name, T type,
			int order) {
		return new SNullActionInputDataRef<T>(name, type, order);
	}

	/**
	 * generates a reference to an output attribute
	 * 
	 * @param name  name of the attribute
	 * @param type  type type of the attribute
	 * @param order order of the attribute
	 * @return the generate output attribute
	 */
	protected static <T extends DataEltType> SActionOutputDataRef<T> getActionOutputDataRef(String name, T type,
			int order) {
		return new SActionOutputDataRef<T>(name, type, order);
	}

	/**
	 * executes the action as an inline action (enriching a page currently displayed
	 * on the client
	 * 
	 * @param actiondata inputaction data
	 * @return output action data
	 */
	public abstract SPageData executeInlineAction(SActionData actiondata);

	/**
	 * executes the action as an inline action (enriching a page currently displayed
	 * on the client) with a query filter
	 * 
	 * @param actiondata action input data
	 * @param datafilter data filter
	 * @return output action data
	 */
	public abstract SPageData executeInlineAction(SActionData actiondata, Function<TableAlias, QueryFilter> datafilter);

	/**
	 * this function is called by the server security manager to get the security
	 * manager for the action
	 * 
	 * @return the security managers for the action, or null if the action has no
	 *         security manager
	 */
	public abstract ActionSecurityManager[] getActionSecurityManager();

}
