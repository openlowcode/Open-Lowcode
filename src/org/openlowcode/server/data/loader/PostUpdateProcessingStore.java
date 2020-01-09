/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.openlowcode.server.data.DataObject;

/**
 * a class to store processing to perform post update
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object the loader is about
 */
public class PostUpdateProcessingStore<E extends DataObject<E>> {

	private HashMap<String, PostUpdateProcessing<E>> processingstore;

	/**
	 * creates a blank post processing store
	 */
	public PostUpdateProcessingStore() {
		processingstore = new HashMap<String, PostUpdateProcessing<E>>();
	}

	/**
	 * adds a post processing element
	 * 
	 * @param name       name of the post processing (only one post processing will
	 *                   be stored per unique name
	 * @param processing post processing class
	 */
	public void addPostUpdateProcessing(String name, PostUpdateProcessing<E> processing) {
		processingstore.put(name, processing);
	}

	/**
	 * performs the post processing for the object given and gives back a list of
	 * exceptions for the errors encountered
	 * 
	 * @param object data object to post-process
	 * @return the list of exceptions encountered
	 */
	public ArrayList<Exception> process(E object) {
		Iterator<PostUpdateProcessing<E>> processestoexecute = processingstore.values().iterator();
		ArrayList<Exception> exceptions = new ArrayList<Exception>();
		while (processestoexecute.hasNext()) {
			PostUpdateProcessing<E> element = processestoexecute.next();
			try {
				element.postupdateprocess(object);
			} catch (Exception e) {
				exceptions.add(e);
			}
		}
		return exceptions;
	}

}
