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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageEndStructure;
import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStringField;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.misc.NamedInterface;

/**
 * a data element that stores a unique value
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public abstract class SimpleDataElt extends DataElt {

	private String propertyname; // tells the parent property for a field if relevant;

	/**
	 * @return the name of the property the element is part of
	 */
	public String getPropertyname() {
		return propertyname;
	}

	/**
	 * sets the property name for the field (this is linked to how Open Lowcode
	 * stores data object
	 * 
	 * @param propertyname name of the property
	 */
	public void setPropertyname(String propertyname) {
		this.propertyname = propertyname;
	}

	/**
	 * Creates a simple data element
	 * 
	 * @param name         name of the property
	 * @param type         type of the element
	 * @param propertyname name of the property
	 */
	public SimpleDataElt(String name, SimpleDataEltType type, String propertyname) {
		this(name, type);
		this.propertyname = propertyname;

	}

	/**
	 * @param name name of the element
	 * @param type type of the element
	 */
	public SimpleDataElt(String name, SimpleDataEltType type) {
		super(name, type);
		this.propertyname = null;

	}

	/**
	 * makes a deep copy of the element
	 * 
	 * @return the deep copy
	 */
	public abstract SimpleDataElt cloneElt();

	/**
	 * writes the payload for network transport
	 * 
	 * @param writer writer
	 * @throws IOException if anything wrongs happens in the message sending
	 */
	public abstract void writePayload(MessageWriter writer) throws IOException;

	/**
	 * @return a simple text representation for audit and log purposes
	 */
	public abstract String defaultTextRepresentation();

	/**
	 * writes the element for network transmission
	 * 
	 * @param writer the message writer
	 * @throws IOException in case anything goes wrong in the network transmission
	 */
	public void writeToMessage(MessageWriter writer) throws IOException {
		writeToMessage(writer, null);
	}

	public void WriteSpecToMessage(MessageWriter writer) throws IOException {
		writer.startStructure("FLDSPEC");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.getType().printType());
		if (this.propertyname != null)
			writer.addStringField("PTN", this.propertyname);
		writer.endStructure("FLDSPEC");
	}

	@Override
	public void writeToMessage(MessageWriter writer, HashMap<String, NamedInterface> hiddenfields) throws IOException {
		if (hiddenfields != null)
			if (hiddenfields.size() > 0)
				throw new RuntimeException("Hidden fields not supported for this type " + this.getType().toString());

		writer.startStructure("DELT");
		writer.addStringField("NAM", this.getName());
		writer.addStringField("TYP", this.getType().printType());
		this.writePayload(writer);
		if (this.propertyname != null)
			writer.addStringField("PTN", this.propertyname);
		writer.endStructure("DELT");

	}

	/**
	 * @param constraintvalue force the content in payload. May not be implemented
	 *                        for all the types
	 */
	public abstract void forceContent(String constraintvalue);

	/**
	 * @return gets the Message Field Spec
	 */
	protected abstract MessageFieldSpec getMessageFieldSpec();

	/**
	 * @return the vale of an object in the message array
	 */
	protected abstract Object getMessageArrayValue();

	@SuppressWarnings("rawtypes")
	protected static SimpleDataElementCreator getEltCreator(MessageReader reader)
			throws OLcRemoteException, IOException {
		String name = reader.returnNextStringField("NAM");
		String type = reader.returnNextStringField("TYP");
		String temppropertyname = null;
		MessageElement nextelement = reader.getNextElement();
		if (nextelement instanceof MessageStringField) {
			MessageStringField field = (MessageStringField) nextelement;
			if (!field.getFieldName().equals("PTN"))
				throw new RuntimeException("Expecting a string field called PTN, got instead a string field called "
						+ field.getFieldName());
			temppropertyname = field.getFieldcontent();
			nextelement = reader.getNextElement();
		}
		final String propertyname = temppropertyname;
		if (!(nextelement instanceof MessageEndStructure))
			throw new RuntimeException(
					"Expected a MessageEndStructure, got " + nextelement.getClass() + " - " + nextelement.toString());
		MessageEndStructure endstructure = (MessageEndStructure) nextelement;
		if (!endstructure.getName().equals("FLDSPEC"))
			throw new RuntimeException(
					"Expected an end structure called FLDSPEC, instead it is called '" + endstructure.getName() + "'");
		switch (type) {
		case "DAT":
			return new SimpleDataElementCreator<DateDataElt, Date>() {

				@Override
				public DateDataElt getBlankDataElt() {
					DateDataElt blankelement = new DateDataElt(name);
					blankelement.setPropertyname(propertyname);
					return blankelement;
				}

				@Override
				public void setPayload(DateDataElt dataelt, Date payload) {
					dataelt.updatePayload(payload);
				}

			};
		case "MLC":
			return new SimpleDataElementCreator<MultipleChoiceDataElt,String>() {

				@Override
				public MultipleChoiceDataElt getBlankDataElt() {
					MultipleChoiceDataElt blankelement = new MultipleChoiceDataElt(name);
					blankelement.setPropertyname(propertyname);
					return blankelement;
				}

				@Override
				public void setPayload(MultipleChoiceDataElt dataelt, String payload) {
					dataelt.forceContent(payload);
					
				}
				
			};
		case "TXT":
			return new SimpleDataElementCreator<TextDataElt, String>() {

				@Override
				public TextDataElt getBlankDataElt() {
					TextDataElt blankelement = new TextDataElt(name);
					blankelement.setPropertyname(propertyname);
					return blankelement;
				}

				@Override
				public void setPayload(TextDataElt dataelt, String payload) {
					dataelt.changePayload(payload);
				}

			};
		case "FYT":
			return new SimpleDataElementCreator<FaultyTextDataElt, String>() {

				@Override
				public FaultyTextDataElt getBlankDataElt() {
					FaultyTextDataElt blankelement = new FaultyTextDataElt(name);
					blankelement.setPropertyname(propertyname);
					return blankelement;
				}

				@Override
				public void setPayload(FaultyTextDataElt dataelt, String payload) {
					dataelt.changePayload(payload);
				}

			};

		case "CHT":
			return new SimpleDataElementCreator<ChoiceDataElt, String>() {

				@Override
				public ChoiceDataElt getBlankDataElt() {
					ChoiceDataElt blankelement = new ChoiceDataElt(name);
					blankelement.setPropertyname(propertyname);
					return blankelement;
				}

				@Override
				public void setPayload(ChoiceDataElt dataelt, String payload) {
					dataelt.forceContent(payload);
				}

			};
		case "DEC":
			return new SimpleDataElementCreator<DecimalDataElt, BigDecimal>() {

				@Override
				public DecimalDataElt getBlankDataElt() {
					DecimalDataElt blankelement = new DecimalDataElt(name);
					blankelement.setPropertyname(propertyname);
					return blankelement;
				}

				@Override
				public void setPayload(DecimalDataElt dataelt, BigDecimal payload) {
					dataelt.updatePayload(payload);

				}

			};
		case "INT":
			return new SimpleDataElementCreator<IntegerDataElt, Integer>() {

				@Override
				public IntegerDataElt getBlankDataElt() {
					IntegerDataElt blankelement = new IntegerDataElt(name);
					blankelement.setPropertyname(propertyname);
					return blankelement;
				}

				@Override
				public void setPayload(IntegerDataElt dataelt, Integer payload) {
					dataelt.updateContent(payload);

				}

			};
		case "ETX":
			return new SimpleDataElementCreator<EncryptedTextDataElt, String>() {

				@Override
				public EncryptedTextDataElt getBlankDataElt() {
					EncryptedTextDataElt blankelement = new EncryptedTextDataElt(name);
					blankelement.setPropertyname(propertyname);
					return blankelement;
				}

				@Override
				public void setPayload(EncryptedTextDataElt dataelt, String payload) {
					dataelt.changePayload(payload);

				}

			};
		default:
			throw new RuntimeException(String.format(" Type %s not supported for data element %s at path %s ", type,
					name, reader.getCurrentElementPath()));

		}
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		// TODO Auto-generated method stub

	}

	@Override
	public String toString() {
		return "[" + this.type.printType() + ":" + this.getName() + "=" + this.defaultTextRepresentation() + "]";
	}

}
