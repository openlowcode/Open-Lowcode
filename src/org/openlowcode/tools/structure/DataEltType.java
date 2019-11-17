/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.structure;

/**
 * type of data element
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */

public abstract class DataEltType {
	public abstract String printType();

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof DataEltType))
			return false;
		DataEltType other = (DataEltType) obj;
		if (other.printType().compareTo(this.printType()) == 0)
			return true;
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static DataEltType getDataEltType(String printedtype) {
		if (printedtype.startsWith("ARR"))
			return new ArrayDataEltType(DataEltType.getDataEltType(printedtype.substring(4)));
		if (printedtype.compareTo("DAT") == 0)
			return new DateDataEltType();
		if (printedtype.compareTo("TXT") == 0)
			return new TextDataEltType();
		if (printedtype.compareTo("OBJ") == 0)
			return new ObjectDataEltType();
		if (printedtype.compareTo("OID") == 0)
			return new ObjectIdDataEltType();
		if (printedtype.compareTo("OMI") == 0)
			return new ObjectMasterIdDataEltType();
		if (printedtype.compareTo("BOL") == 0)
			return new BooleanDataEltType();
		if (printedtype.compareTo("CHT") == 0)
			return new ChoiceDataEltType();
		if (printedtype.compareTo("NDT") == 0)
			return new NodeTreeDataEltType();
		if (printedtype.startsWith("OBT"))
			return new ObjectTreeDataEltType(new ObjectDataEltType());
		if (printedtype.compareTo("LBN") == 0)
			return new LargeBinaryDataEltType();
		if (printedtype.compareTo("INT") == 0)
			return new IntegerDataEltType();
		if (printedtype.compareTo("TPE") == 0)
			return new TimePeriodDataEltType();
		throw new RuntimeException(String.format("Type not managed yet %s ", printedtype));
	}

	public static DataElt createNullDataElt(String name, String printedtype) {
		if (printedtype.compareTo("TXT") == 0)
			return new TextDataElt(name);
		if (printedtype.compareTo("OID") == 0)
			return new ObjectIdDataElt(name);
		if (printedtype.compareTo("OMI") == 0)
			return new ObjectIdDataElt(name);
		if (printedtype.compareTo("OBJ") == 0)
			return new ObjectDataElt(name);
		if (printedtype.compareTo("DAT") == 0)
			return new DateDataElt(name);
		if (printedtype.compareTo("TPE") == 0)
			return new TimePeriodDataElt(name);
		throw new RuntimeException(String.format("Type not managed yet %s ", printedtype));
	}
}
