/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

/**
 * This package provides a full utility to send structured messages as text
 * (serialized), typically over a network. Design objectives are:
 * <ul>
 * <li>format should be compact</li>
 * <li>parsing and serializing should be quick</li>
 * <li>the format does not manage writing and reading fields in different
 * orders</li>
 * </ul>
 */
package org.openlowcode.tools.messages;