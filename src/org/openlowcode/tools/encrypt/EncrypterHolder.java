/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.encrypt;

/**
 * a static holder for an encrypter
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode SAS</a>
 *
 */
public class EncrypterHolder {
	
	private static EncrypterHolder singleton;
	
	public static EncrypterHolder get() {
		if (singleton==null) throw new RuntimeException("Encrypter Holder not configured yet");
		return singleton;
		
	}
	public static void InitEncrypterHolder(Encrypter encrypter) {
		EncrypterHolder thisholder = new EncrypterHolder();
		thisholder.encrypter = encrypter;
		singleton = thisholder;
	}

	private Encrypter encrypter;
	
	public Encrypter getEncrypter() {
		return encrypter;
	}
}
