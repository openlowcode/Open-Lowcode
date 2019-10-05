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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * This class stores the history of pages visited
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ClientData {

	private static Logger logger = Logger.getLogger(ClientData.class.getName());

	/**
	 * @return the list of addresses visited recently, ordered by date of visit
	 */
	public AddressLink[] getOrderedAddressLink() {
		AddressLink[] returnarray = new AddressLink[recentaddresses.size()];
		for (int i = 0; i < recentaddresses.size(); i++) {
			returnarray[i] = recentaddresses.get(i);
		}
		return returnarray;
	}

	private LinkedList<AddressLink> recentaddresses;
	private HashMap<String, AddressLink> addressesbyname;
	private int indexlastback;

	/**
	 * Creates an empty class
	 */
	public ClientData() {
		this.recentaddresses = new LinkedList<AddressLink>();
		this.addressesbyname = new HashMap<String, AddressLink>();
		indexlastback = 1;
	}

	/**
	 * Adds an address just visited to the client data holder
	 * 
	 * @param address the address of the page
	 * @param title   the title of the page
	 */
	public void addAddress(String address, String title) {
		AddressLink existinglink = addressesbyname.get(address);
		if (existinglink == null) {
			AddressLink link = new AddressLink(address, title);
			addressesbyname.put(address, link);
			recentaddresses.addFirst(link);
		} else {
			existinglink.visit();
			recentaddresses.remove(existinglink);
			recentaddresses.addFirst(existinglink);
		}
	}

	/**
	 * the last address visited
	 * 
	 * @return
	 */
	public String getLastAddress() {
		String address = recentaddresses.get(indexlastback).getClink();
		indexlastback++;
		return address;
	}

	/**
	 * @return true if we can still go back in history. This is used by the function
	 *         allowing to review
	 */
	public boolean isBackPossible() {
		logger.finer("is back possible = recent addresses size = " + recentaddresses.size() + ", index last back = "
				+ indexlastback);
		if (recentaddresses.size() > indexlastback)
			return true;
		return false;

	}

	/**
	 * signals that we cannot go back anymore
	 */
	public void setBackChainBroken() {
		indexlastback = 1;
	}
}
