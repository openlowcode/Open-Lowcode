package org.openlowcode.samples.message;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;

import org.openlowcode.tools.messages.MessageArrayEnd;
import org.openlowcode.tools.messages.MessageArrayLine;
import org.openlowcode.tools.messages.MessageArrayStart;
import org.openlowcode.tools.messages.MessageBufferedWriter;
import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeBoolean;
import org.openlowcode.tools.messages.MessageFieldTypeDecimal;
import org.openlowcode.tools.messages.MessageFieldTypeInteger;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageSimpleReader;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * This sample shows how to use the OLc messaging.
 * It transmits the information through a local reader
 * and does not require network connection.
 * @author Open Lowcode SAS
 *
 */
public class OLcMessageSample {

	public static void main(String[] args) {
		try {
			System.out.println(" -----------------------------------------------------------");
			System.out.println(" Transmission of a simple message");
			transmitsSimpleMessage();
			transmitArray();
		} catch (Exception e) {
			System.err.println("An error happended "+e.getMessage());
			for (int i=0;i<e.getStackTrace().length;i++) {
				System.err.println("   - "+e.getStackTrace()[i]);
			}
	}
	}
	/**
	 * Transmits a compact array. This allows transfer of sets of identical elements
	 * with very limited overhead.
	 * @throws IOException 
	 * @throws OLcRemoteException
	 */
	private static void transmitArray() throws IOException, OLcRemoteException {
		System.out.println(" -----------------------------------------------------------");
		System.out.println(" Transmission of a message with compact array");
			
			StringWriter messageholder = new StringWriter();
			MessageBufferedWriter messagewriter = new MessageBufferedWriter(new BufferedWriter(messageholder), false);
			// -- write message writer here
			messagewriter.startNewMessage();
			ArrayList<MessageFieldSpec> fullspec = new ArrayList<MessageFieldSpec>();
			fullspec.add(new MessageFieldSpec("FIELD1",MessageFieldTypeBoolean.singleton));
			fullspec.add(new MessageFieldSpec("FIELD2",MessageFieldTypeInteger.singleton));
			fullspec.add(new MessageFieldSpec("FIELD3",MessageFieldTypeDecimal.singleton));
			fullspec.add(new MessageFieldSpec("FIELD4",MessageFieldTypeString.singleton));
			MessageArrayStart arraystart = new MessageArrayStart("TESTARRAY", fullspec);
			messagewriter.sendMessageElement(arraystart);
			messagewriter.sendMessageElement(new MessageArrayLine(arraystart, new Object[] {new Boolean(true),new Integer(123),new BigDecimal(12.5),"Line 1 "}));
			messagewriter.sendMessageElement(new MessageArrayLine(arraystart, new Object[] {new Boolean(false),new Integer(-50),new BigDecimal(-123.50),"Line 2  "}));
			messagewriter.sendMessageElement(new MessageArrayLine(arraystart, new Object[] {new Boolean(true),new Integer(45000),new BigDecimal(45.50),"Line 45 --<  "}));
			messagewriter.sendMessageElement(new MessageArrayEnd());				
			messagewriter.endMessage();
			
			String message = messageholder.toString();
			System.out.println("*** MESSAGE DROP ***");
			System.out.println(message);
			System.out.println("*** MESSAGE DROP END ***");
			
			MessageSimpleReader reader = new MessageSimpleReader(new BufferedReader(new StringReader(message)));
			// -- write message reader here
			reader.returnNextMessageStart();
			MessageArrayStart startarray = reader.returnNextMessageStartArray("TESTARRAY");
			System.out.println(" -- Array field definition ");
			for (int i=0;i<startarray.getFieldSpecNr();i++) {
				System.out.println(" Field "+ startarray.getFieldSpecAt(i).getName()+" type = "+startarray.getFieldSpecAt(i).getType());
			}
			while (reader.hasArrayNextLine()) {
				MessageArrayLine arrayline = reader.getArrayNextLine();
				System.out.println(">>> New Array Line");
				for (int i=0;i<arrayline.getObjectNumber();i++) {
					System.out.println("    - "+arrayline.getPayloadAt(i));
				}
			}
			reader.returnNextEndMessage();
			
			
		
		}
	private static void transmitsSimpleMessage() throws IOException, OLcRemoteException {
		StringWriter messageholder = new StringWriter();
		MessageBufferedWriter messagewriter = new MessageBufferedWriter(new BufferedWriter(messageholder), false);
		messagewriter.startNewMessage();
		messagewriter.startStructure("STR1");
		messagewriter.addStringField("FLD1","Content of field 1");
		messagewriter.startStructure("SUBSTR1_1");
		messagewriter.addStringField("FLD1_1","Content of field 1.1");
		messagewriter.addDateField("FLD1_2",new Date());
		
		messagewriter.endStructure("SUBSTR1_1");
		
		messagewriter.endStructure("STR1");
		
		messagewriter.endMessage();
		String message = messageholder.toString();
		System.out.println("*** MESSAGE DROP ***");
		System.out.println(message);
		System.out.println("*** MESSAGE DROP END ***");
		MessageSimpleReader reader = new MessageSimpleReader(new BufferedReader(new StringReader(message)));
		reader.returnNextMessageStart();
		reader.returnNextStartStructure("STR1");
		System.out.println("Content of Field 1 "+reader.returnNextStringField("FLD1"));
		reader.returnNextStartStructure("SUBSTR1_1");
		System.out.println("Content of Field 1.1 "+reader.returnNextStringField("FLD1_1"));
		System.out.println("Content of Field 1.2 "+reader.returnNextDateField("FLD1_2"));
		
		
		reader.returnNextEndStructure("SUBSTR1_1");
		
		reader.returnNextEndStructure("STR1");
		
		reader.returnNextEndMessage();
	}
}
