/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * A utility class to load the configuration file of the server
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 * @since 1.10
 */
public class OLcServerConfig {
	private File pathfile;
	private HashMap<String, String> parametermap;

	/**
	 * creates the configuration file reader and checks that the file exists
	 * 
	 * @param path path of the config file
	 */
	public OLcServerConfig(String path) {
		this.pathfile = new File(path);
		if (!this.pathfile.exists())
			throw new RuntimeException("Server config file " + path + " does not exist");
	}

	/**
	 * Reads the parameter file. The file has to be the following format
	 * <ul>
	 * <li>either the first significant character is '#', which means the line is
	 * discarded</li>
	 * <li>Or the line is of type KEY=VALUE, with space at the beginning and end of
	 * both KEY and VALUE being trimmed</li>
	 * 
	 * @throws IOException
	 */
	public void parseConfigFile() throws IOException {
		@SuppressWarnings("resource")
		BufferedReader reader = new BufferedReader(new FileReader(pathfile));
		String line = reader.readLine();
		parametermap = new HashMap<String, String>();
		while (line != null) {
			if (line.trim().length() > 0) {
				if (line.trim().indexOf('#') != 0) {
					int indexofequal = line.indexOf('=');
					if (indexofequal == -1)
						throw new RuntimeException(
								"Incorrect line syntax for config file (should be KEY=VALUE), for line " + line);
					String key = line.substring(0, indexofequal).trim();
					String value = line.substring(indexofequal + 1).trim();
					parametermap.put(key, value);
				}
			}

			line = reader.readLine();
		}
		reader.close();

	}

	/**
	 * returns String value for the key, or throws an exception if the key does not
	 * exist
	 * 
	 * @param key key
	 * @return the string value
	 */
	public String getCompulsoryValue(String key) {
		String value = parametermap.get(key);
		if (value == null)
			throw new RuntimeException("Missing compulsory value for key '" + key + "' in configuration file");
		return value;
	}

	/**
	 * returns String value for the key, or null if the key does not exist
	 * 
	 * @param key key
	 * @return the string value
	 */
	public String getOptionalValue(String key) {
		return parametermap.get(key);
	}

	/**
	 * gets the integer for a key, or throws an exception if the key does not exist
	 * 
	 * @param key key in the config file
	 * @return an integer
	 */
	public int getCompulsoryIntegerValue(String key) {
		String valueunparsed = getCompulsoryValue(key);
		Integer parsedvalue = Integer.decode(valueunparsed);
		return parsedvalue.intValue();
	}

	/**
	 * @param key          key in the config file
	 * @param defaultvalue value if the key does not exist
	 * @return the value corresponding to the key, or the default value if key does
	 *         not exist
	 */
	public int getOptionalIntegerValue(String key, int defaultvalue) {
		String valueunparsed = getCompulsoryValue(key);
		if (valueunparsed == null)
			return defaultvalue;
		Integer parsedvalue = Integer.decode(valueunparsed);
		return parsedvalue.intValue();
	}

	/**
	 * @param key key in the config file
	 * @return the value corresponding to the key, or blows an exception if the key
	 *         does not exist
	 */
	public boolean getCompulsoryBooleanValue(String key) {
		String valueunparsed = getCompulsoryValue(key);
		return Boolean.parseBoolean(valueunparsed);

	}

	/**
	 * @param key          key in the config file
	 * @param defaultvalue the value to give back if the key does not exist
	 * @return the value corresponding to the key, or the default value if the key
	 *         does not exist
	 */
	public boolean getOptionalBooleanValue(String key, boolean defaultvalue) {
		String valueunparsed = getCompulsoryValue(key);
		if (valueunparsed == null)
			return defaultvalue;
		return Boolean.parseBoolean(valueunparsed);

	}
}
