/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.tools.messages;

/**
 * @author Open Lowcode SAS
 * @param <E> the object to store in the circular buffer
 */
public class CircularBuffer<E extends Object> {
	private int buffersize;
	private E[] buffer;
	private int nextindex;
	private boolean bufferfull;

	public void reset() {
		this.bufferfull = false;
		nextindex = 0;
	}

	/**
	 * Creates a circular buffer able to hold at maximum a set number of elements of
	 * a given class.
	 * 
	 * @param buffersize the number of elements to store
	 */
	@SuppressWarnings("unchecked")
	public CircularBuffer(int buffersize) {
		this.buffersize = buffersize;
		buffer = (E[]) new Object[buffersize];
		bufferfull = false;
		nextindex = 0;
	}

	/**
	 * Adds an element. If the buffer is full, will add the element given by
	 * removing the older one.
	 * 
	 * @param element the element to add
	 */
	public void addElement(E element) {
		buffer[nextindex] = element;
		nextindex++;
		if (nextindex == buffersize) {
			nextindex = 0;
			bufferfull = true;
		}
	}

	/**
	 * This is a specific method used to display the content of the buffer,
	 * highlighting the last element, and replacing all carriage returns by space so
	 * that it displays in a compact manner in the log.
	 * 
	 * @return a String with the representation of the objects
	 */
	public String getCompactBufferTrace() {
		E[] answer = getBufferContent();
		StringBuffer compactanswer = new StringBuffer();
		for (int i = 0; i < answer.length; i++) {
			if (i == answer.length - 1)
				compactanswer.append(" ->");
			String element = answer[i].toString().replace('\n', ' ');
			compactanswer.append(element);
			if (i == answer.length - 1)
				compactanswer.append("<- ");
		}
		return compactanswer.toString();
	}

	/**
	 * This method drops the content of the circular buffer,
	 * as an array of objects by order of creation
	 * @return the content of the circular buffer, as an array of objects
	 * of type E
	 */
	public E[] getBufferContent() {
		if (!bufferfull) {
			// simple case: just send the first elements of the array
			@SuppressWarnings("unchecked")
			E[] answer = (E[]) new Object[nextindex];
			System.arraycopy(buffer, 0, answer, 0, nextindex);
			return answer;
		} else {
			// complex case, buffer is full, there will be two portions to copy, before and
			// after the index.
			@SuppressWarnings("unchecked")
			E[] answer = (E[]) new Object[buffersize];
			System.arraycopy(buffer, nextindex, answer, 0, buffersize - nextindex);
			if (nextindex != 0)
				System.arraycopy(buffer, 0, answer, buffersize - nextindex, nextindex);
			return answer;
		}
	}

}
