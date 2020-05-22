/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import org.openlowcode.tools.file.SourceGenerator;

/**
 * This generator produces the final version class that is put in the Open
 * Lowcode framework. In this file, the current version, and the first
 * compatible version of the client are put manually before each build
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcVersionGenerator {
	public final static String version = "1.7.4";
	public final static String clientversion = "1.7.4";
	public final static boolean stable = true;

	public static void main(String[] args) {
		try {
			generateOLcVersion();
			System.out.println("Generated Open Lowcode version");
		} catch (Exception e) {
			System.err.println("Error in generating the Open Lowcode version file " + e.getMessage());
			e.printStackTrace(System.err);
			System.exit(1);
		}
	}

	/**
	 * writes the OLcVersion class
	 * 
	 * @throws IOException if any error is encountered writing the file
	 */
	public static void generateOLcVersion() throws IOException {
		SourceGenerator sg = new SourceGenerator(new File("./src/org/openlowcode/OLcVersion.java"),
				"Nicolas de Mauroy", OLcVersionGenerator.version);

		sg.wl("package org.openlowcode;");
		sg.wl("");
		sg.wl("import java.util.Date;");
		sg.wl("");
		sg.wl("/**");
		sg.wl(" * The Open Lowcode version specifies the version of framework being run.");
		sg.wl(" * It is used by the client and the server. The following rules are");
		sg.wl(" * implemented by the server:<ul>");
		sg.wl(" * <li>if version of the client is lower than version of the server,");
		sg.wl(" * an error is thrown, unless the module authorizes exceptions.</li>");
		sg.wl(" * <li>if version of the client is higher than or equal to");
		sg.wl(" * the version of the server, connection proceeds OK</li>");
		sg.wl(" * </ul>");
		sg.wl(" * @author <a href=\"https://openlowcode.com/\" rel=\"nofollow\">Open Lowcode SAS</a>");
		sg.wl(" *");
		sg.wl(" */");
		sg.wl("public class OLcVersion {");
		sg.wl("	");
		sg.wl("	/**");
		sg.wl("	 * version of the Open Lowcode framework (server and client side). Versions are ordered by");
		sg.wl("	 * lexicographic order");
		sg.wl("	 */");
		sg.wl("	public static String version=\"" + version + "\";");
		sg.wl("	");
		sg.wl("	/**");
		sg.wl("	 * reference version of the client for the server");
		sg.wl("	 */");
		sg.wl("	public static String referenceclientversion=\"" + clientversion + "\";");
		sg.wl("	");
		sg.wl("	/**");
		sg.wl("	 * date of the release (generated automatically at compile time)");
		sg.wl("	 */");
		sg.wl("	public static Date versiondate = new Date(" + (new Date()).getTime() + "L);");
		sg.wl("	");
		sg.wl("	/**");
		sg.wl("	 * indicates if the release is stable or not.");
		sg.wl("	 */");
		sg.wl("	public static boolean stable = " + stable + ";");
		sg.wl("	");
		sg.wl("	/**");
		sg.wl("	 * gives negative number if this object version is smaller than the other version,");
		sg.wl("	 * gives 0 if this object version is the same than the other version");
		sg.wl("	 * gives positive version if this object version is the same than other version");
		sg.wl("	 * @param connectedclientversion the other version (typically coming from client).");
		sg.wl("	 * @return");
		sg.wl("	 */");
		sg.wl("	public static int compareWithClientVersion(String connectedclientversion) {");
		sg.wl("		return OLcVersionTools.CompareTwoVersions(referenceclientversion,connectedclientversion);");
		sg.wl("	}");
		sg.wl("	");
		sg.wl("	");
		sg.wl("}");

		sg.close();
	}
}
