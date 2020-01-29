/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.generation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

/**
 * a light class around a buffered writer providing some added features for
 * automatic code generation
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class SourceGenerator {
	private File filepath;
	private BufferedWriter bw;

	/**
	 * creates a source generator and generates the automatically generated code
	 * header to it
	 * 
	 * @param filepath path of the file (folders + file name)
	 * @param author   author of the module
	 * @param version  version of the module
	 * @throws IOException if anything bad happens creating the file
	 */
	public SourceGenerator(File filepath, String author, String version) throws IOException {
		this.filepath = filepath;
		this.filepath.getParentFile().mkdirs();
		bw = new BufferedWriter(new FileWriter(filepath));
		writeHeader(author, version);
	}

	/**
	 * writes content and put a return carriage at the end
	 * 
	 * @param content text without a carriage return at the end
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void wl(String content) throws IOException {
		bw.write(content + "\n");
	}

	/**
	 * breaks line (adds a carriage return)
	 * 
	 * @throws IOException
	 */
	public void bl() throws IOException {
		bw.write("\n");
	}

	/**
	 * write text without a carriage return at the end
	 * 
	 * @param content content
	 * @throws IOException if anything bad happens while writing the file
	 */
	public void w(String content) throws IOException {
		bw.write(content);
	}

	/**
	 * closes the file (if not done, the content may not be written in the file)
	 * 
	 * @throws IOException if anything bad happens while closing the file
	 */
	public void close() throws IOException {
		bw.close();
	}

	/**
	 * writes the auto-generated file header
	 * 
	 * @param author  author of the module
	 * @param version version of the module
	 * @throws IOException if anything bad happens while writing the header
	 */
	public void writeHeader(String author, String version) throws IOException {
		this.wl("/*-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-OLc-");
		this.wl(" * OPEN LOWCODE AUTOMATICALLY GENERATED FILE ( https://openlowcode.com/ ) ");
		this.wl(" * generated on : " + new Date());
		this.wl(" * author : " + author);
		this.wl(" * version : " + version);
		this.wl(" * Warning: the integrity of the system depends on this file. You must not ");
		this.wl(" *          modify it directly, and should instead run again the generation.");
		this.wl(" **************************************************************************/");

	}

}
