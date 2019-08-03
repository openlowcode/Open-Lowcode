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

import java.io.ByteArrayInputStream;

import java.io.InputStream;

/**
 * A light wrapper around a file, including
 * binary content and a filename
 * @author Open Lowcode SAS
 *
 */
public class SFile {
	private boolean empty;
	private byte[] content;
	private String filename;
	/**
	 * @return binary content
	 */
	public byte[] getContent() {
		return content;
	}
	/**
	 * Creates a new file with no filename or content.
	 */
	public SFile() {
		this.empty=true;
	}
	
	/**
	 * Creates a new file with no filename or content.
	 * it will check whether the content is null or not
	 * @param filename name of the file (it should not include the path)
	 * @param content binary content
	 */
	public SFile(String filename,byte[] content) {
		this.content = content;
		this.filename = filename;
		if (content!=null) {
		this.empty=false;
		} else {
			this.empty=true;
		}
		
	}
	
	
	/**
	 * @return the file name
	 */
	public String getFileName() {
		return this.filename;
	}
	
	/**
	 * @return an input stream with the content
	 */
	public InputStream getStream()  {
		 return new ByteArrayInputStream(content);
	
	}
	
	/**
	 * @return the length of the file
	 */
	public long getLength() {
		return content.length;
	}
	/**
	 * @return true if the file payload is 
	 * empty
	 */
	public boolean isEmpty() {
		return empty;
	}

@Override
public boolean equals(Object other) {
	if (other==null) return false;
	if (!(other instanceof SFile)) return false;
	SFile othersfile = (SFile) other;
	if (this.empty) if (!othersfile.empty) return false;
	if (!this.filename.equals(othersfile.filename)) return false;
	return java.util.Arrays.equals(this.content,othersfile.content);
}
}
