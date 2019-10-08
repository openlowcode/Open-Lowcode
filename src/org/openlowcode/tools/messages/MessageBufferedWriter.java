/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.tools.messages;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * A buffered writer implementation of the OLc Message writer
 * 
 * @author Open Lowcode SAS
 *
 */
public class MessageBufferedWriter extends MessageWriter {
	private BufferedWriter writer;
	private StringBuffer messagebuffer;
	private static Logger logger = Logger.getLogger(MessageBufferedWriter.class.getName());
	// logic to put a closing structure in the same line to make message compacct
	private boolean samelinestructureclose;

	// logic to concatenate closing structures on the same time
	private String closestructurebuffer;
	private int closestructurestacklength;
	private boolean messageaudit;

	@Override
	public void checkConnection() {

	}

	public void close() throws IOException {
		writer.close();
		messagebuffer = null;
	}
	
	@Override
	public void sendMessageElement(MessageElement messageelement) throws IOException {
		
			String element = null;

			if ((messageelement instanceof MessageEndStructure) && (!samelinestructureclose)) {
				// start stack
				closestructurebuffer = messageelement.serialize(this.getCurrentPadding(),
						this.isIsfirstelementinstructure());
				for (int i = 0; i < closestructurestacklength; i++)
					closestructurebuffer = closestructurebuffer
							+ ((MessageEndStructure) messageelement).serializesameline();
				closestructurestacklength++;
			} else {
				// position to close stack is there is something
				if (closestructurebuffer != null) {
					// writer.write(closestructurebuffer);
					messagebuffer.append(closestructurebuffer);
					closestructurestacklength = 0;
					closestructurebuffer = null;
				}
			}
			// opportunity to close on the same line
			if ((messageelement instanceof MessageEndStructure) && (samelinestructureclose)) {
				element = ((MessageEndStructure) messageelement).serializesameline();
				samelinestructureclose = false;
			}

			// if buffer ongoing, do not print
			if (closestructurebuffer == null) {
				if (element == null)
					element = messageelement.serialize(this.getCurrentPadding(), this.isIsfirstelementinstructure());
				// writer.write(element);
				messagebuffer.append(element);
			}
			if (messageelement instanceof MessageStartStructure)
				samelinestructureclose = true;
			if (messageelement instanceof MessageEnd) {
				String message = messagebuffer.toString();
				writer.write(message);
				writer.flush();
				if (messageaudit) {
					logger.info("--------------------------- FULL MESSAGE AUDIT -----------------------");
					logger.info(message);
					logger.info("--------------------------- FULL MESSAGE AUDIT END -------------------");
				}
				messagebuffer = new StringBuffer();
			}
			if (messageelement instanceof MessageError) {
				String message = messagebuffer.toString();
				writer.write(message);
				writer.flush();
				if (messageaudit) {
					logger.info("--------------------------- FULL MESSAGE AUDIT #ERROR#----------------");
					logger.info(message);
					logger.info("--------------------------- FULL MESSAGE AUDIT END #ERROR# -------------------");
				}
				messagebuffer = new StringBuffer();
			}
		

	}

	/**
	 * Creates a new MessageBufferedWriter
	 * 
	 * @param writer       the buffered writer ot use
	 * @param messageaudit true if messages should be put in logs after being sent.
	 *                     This has significant impact on performance and log size,
	 *                     and should not be used in most production environments
	 */
	public MessageBufferedWriter(BufferedWriter writer, boolean messageaudit) {
		super();
		this.writer = writer;

		this.messageaudit = messageaudit;
		messagebuffer = new StringBuffer();
		this.samelinestructureclose = true;
	}

	@Override
	public void flushMessage() {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(
					String.format("Error in generating CML Message, original exception = %s at path %s", e.getMessage(),
							this.currentpath()));
		}
	}

}
