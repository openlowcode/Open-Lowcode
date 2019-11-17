package org.openlowcode.tools.structure;

import java.io.IOException;
import java.util.logging.Logger;

import org.openlowcode.tools.data.TimePeriod;
import org.openlowcode.tools.messages.MessageFieldSpec;
import org.openlowcode.tools.messages.MessageFieldTypeString;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;


public class TimePeriodDataElt extends SimpleDataElt {
	private static Logger logger = Logger.getLogger(TimePeriodDataElt.class.getName());
	private TimePeriod payload;

	public TimePeriodDataElt(String name,TimePeriod payload) {
		super(name,new TimePeriodDataEltType());
		this.payload=payload;
		logger.finest("				Create new TimePeriodDataElt"+this.getName()+", parsed as "+payload);
	}
	
	public TimePeriodDataElt(String name) {
		super(name,new TimePeriodDataEltType());
	}

	@Override
	public SimpleDataElt cloneElt() {
		
		return new TimePeriodDataElt(this.getName(),(payload==null?null:payload.publicClone()));
	}

	@Override
	public void writePayload(MessageWriter writer) throws IOException {
		String stringpayload = (payload!=null?payload.encode():null);
		writer.addStringField("TMP",stringpayload);
		logger.finest("encoding field TimePeriod "+stringpayload);
	}

	@Override
	public String defaultTextRepresentation() {
		return payload.toString();
	}

	@Override
	public void forceContent(String constraintvalue) {
		this.payload = TimePeriod.generateFromString(constraintvalue);
		logger.finest("				Force Content for "+this.getName()+" "+constraintvalue+", parsed as "+payload);
		
	}

	@Override
	protected MessageFieldSpec getMessageFieldSpec() {
		return new MessageFieldSpec(this.getName().toUpperCase(),MessageFieldTypeString.singleton);
	}

	@Override
	protected Object getMessageArrayValue() {
		return payload.encode();
	}

	@Override
	public void addPayload(MessageReader reader) throws OLcRemoteException, IOException {
		this.payload = TimePeriod.generateFromString(reader.returnNextStringField("TMP"));
		
	}
	public TimePeriod getPayload() {
		return this.payload;
		
	}
}
