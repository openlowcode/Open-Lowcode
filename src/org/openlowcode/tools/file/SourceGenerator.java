/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.file;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * Source Generator. Light utility class over Buffered Writer
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SourceGenerator {
	@SuppressWarnings("unused")
	private File filepath;
	private BufferedWriter bw;

	/**
	 * Creates a source generator
	 * 
	 * @param filepath file path
	 * @param author   author of the module
	 * @param version  version of the module
	 * @throws IOException if any exception is encountered while writing the file
	 */
	public SourceGenerator(File filepath, String author, String version) throws IOException {
		this.filepath = filepath;
		filepath.getParentFile().mkdirs();
		bw = new BufferedWriter(new FileWriter(filepath));
		writeHeader(author, version);
	}

	/**
	 * writes the content and have a carriage return
	 * 
	 * @param content the content to write
	 * @throws IOException if any exception is encountered while writing the file
	 */
	public void wl(String content) throws IOException {
		bw.write(content + "\n");
	}

	/**
	 * @throws IOException if any exception is encountered while writing the file
	 */
	public void bl() throws IOException {
		bw.write("\n");
	}

	/**
	 * write the content without a carriage return at the end
	 * 
	 * @param content content to write
	 * @throws IOException if any exception is encountered while writing the file
	 */
	public void w(String content) throws IOException {
		bw.write(content);
	}

	/**
	 * close the file
	 * 
	 * @throws IOException if any exception is encountered while writing the file
	 */
	public void close() throws IOException {
		bw.close();
	}

	/**
	 * writes the header of auto-generated file
	 * 
	 * @param author  author of the module
	 * @param version version of the module
	 * @throws IOException if any exception is encountered while writing the file
	 */
	public void writeHeader(String author, String version) throws IOException {
		this.wl("/*-OLc--OLc--OLc--OLc--OLc--OLc--OLc--OLc--OLc--OLc--OLc--OLc--OLc-");
		this.wl(" * Open Lowcode generated file                                       ");
		this.wl(" * generated on : " + new Date());
		this.wl(" * author : " + author);
		this.wl(" * version : " + version);
		this.wl(" * Warning: the integrity of the system depends on this file. You must not ");
		this.wl(" *          modify it directly, and should instead run again the generation.");
		this.wl(" **************************************************************************/");

	}

}
