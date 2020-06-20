/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.runtime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageBufferedWriter;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageSimpleReader;
import org.openlowcode.tools.messages.MessageWriter;
import org.openlowcode.tools.messages.OLcRemoteException;

/**
 * Wraps the socket connection to the Open Lowcode server, and includes relaunch
 * mechanisms if a network connection is broken
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ConnectionToServer {

	private Socket clientsocket;
	private MessageSimpleReader reader;
	private MessageBufferedWriter writer;
	private String server = null;
	private int port = -1;
	private boolean relevant;

	/**
	 * Should include all actions to send a full command to the server. The
	 * ConnectionToServer may relaunch it several times in case of interruption of
	 * transmission.
	 * 
	 * @author <a href="https://openlowcode.com/">Open Lowcode SAS</a>
	 *
	 */
	@FunctionalInterface
	public interface WriterToServer {
		void apply(MessageWriter t) throws IOException;
	}

	private static final Logger logger = Logger.getLogger(ConnectionToServer.class.getName());

	/**
	 * Sends the message to the server, restarting the connection if necessary, and
	 * getting the first element, as in some cases of broken connection, the error
	 * appears only after trying to get the first element
	 * 
	 * @param writertoserver function to send the message to the server
	 * @return the first message element
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws OLcRemoteException 
	 */

	public MessageElement sendMessage(WriterToServer writertoserver) throws UnknownHostException, IOException, OLcRemoteException {
		int index = 0;
		boolean sent = false;
		while ((index < 2) && (!sent)) {
			try {
				if (clientsocket == null) {
					initConnection();
				}

				writertoserver.apply(writer);
				sent = true;
				return reader.getNextElement();
				
			} catch (IOException e) {
				logger.warning("Client disconnected");
				clientsocket = new Socket(server, new Integer(port).intValue());
				reader = new MessageSimpleReader(new BufferedReader(
						new InputStreamReader(clientsocket.getInputStream(), Charset.forName("UTF-8")), 9090));
				writer = new MessageBufferedWriter(
						new BufferedWriter(
								new OutputStreamWriter(clientsocket.getOutputStream(), Charset.forName("UTF-8"))),
						true);
				index++;
			}
		}
		throw new RuntimeException("Did not manage to send message after attempt "+(index+1));
	}

	/**
	 * @return the server (after it has been initiated by the method
	 *         connectToAddressAndGetApplication )
	 */
	public String getServer() {
		return server;
	}

	/**
	 * @return the port (after it has been initiated by the method
	 *         connectToAddressAndGetApplication )
	 */
	public int getPort() {
		return port;
	}

	/**
	 * creates the connection to server. It then needs to be initiated by the method
	 * connectToAddressAndGetApplication
	 */
	public ConnectionToServer() {
		this.relevant = true;
	}

	/**
	 * creates a connection to server initiated with parameters from the connection
	 * taken as parameter
	 * 
	 * @param originalconnection the original connection
	 */
	public ConnectionToServer(ConnectionToServer originalconnection) {
		this.server = originalconnection.server;
		this.port = originalconnection.port;
		this.relevant = true;
	}

	/**
	 * @param address the local address (action reference)
	 * @return the full address (with server URL)
	 * @throws IOException
	 */
	public String connectToAddressAndGetApplication(String address) throws IOException {
		int localport = 8080;
		int columnindex = address.indexOf(':');
		int slashindex = address.indexOf('/');
		int serverend = slashindex; // by default, no port
		if ((columnindex >= 0) && (columnindex < slashindex)) {
			// there is a column before first slash : port is indicated
			serverend = columnindex;
			String portstring = address.substring(columnindex + 1, slashindex);
			localport = new Integer(portstring).intValue();
		}
		String localserver = address.substring(0, serverend);
		String application = address.substring(slashindex + 1);
		if ((clientsocket == null) || ((localserver.compareTo(server) != 0) || (localport != port))) {
			if (clientsocket != null) {
				if (clientsocket.isConnected())
					clientsocket.close();
				if (reader != null)
					reader.close();
			}

			server = localserver;
			port = localport;
			initConnection();
		}
		return application;
	}

	private void initConnection() throws IOException {

		clientsocket = new Socket(server, new Integer(port).intValue());
		InputStreamReader streamreader = new InputStreamReader(clientsocket.getInputStream(), Charset.forName("UTF-8"));
		logger.fine("Input stream reader encoding " + streamreader.getEncoding());
		BufferedReader bufferedreader = new BufferedReader(streamreader, 9090);

		reader = new MessageSimpleReader(bufferedreader);
		OutputStreamWriter socketoutputstream = new OutputStreamWriter(clientsocket.getOutputStream(),
				Charset.forName("UTF-8"));
		logger.fine("OutputStream reader encoding" + socketoutputstream.getEncoding());
		BufferedWriter bufferedwriter = new BufferedWriter(socketoutputstream);
		writer = new MessageBufferedWriter(bufferedwriter, true);
	}

	/**
	 * @return the reader of this connection
	 */
	public MessageReader getReader() {
		return reader;
	}

	/**
	 * @param address the local address of an action
	 * @return the full address, including the server and port if necessary.
	 */
	public String completeAddress(String address) {
		StringBuffer fulladdress = new StringBuffer();
		fulladdress.append(server);
		if (port != 8080) {
			fulladdress.append(':');
			fulladdress.append(port);
		}
		fulladdress.append('/');
		fulladdress.append(address);
		return fulladdress.toString();
	}

	/**
	 * cancels and closes the connections
	 * 
	 * @throws IOException
	 */
	public void stopConnection() throws IOException {
		// dereferencing the socket first, whether the close happens well or not. If
		// there
		// is an exception in the close() socket method, anyways, socket is dead
		Socket sockettoclose = this.clientsocket;
		this.clientsocket = null;
		if (sockettoclose != null)
			sockettoclose.close();
		if (reader != null)
			reader.close();
		if (writer != null)
			writer.close();
	}

	public void resetSendingMessage() {
		try {
			if (writer != null)
				if (writer.isActive()) {
					logger.warning("Error during sending message, sending error to server to reset connection");
					writer.sendMessageError(1, "Error during sending of client data");
				}
		} catch (Exception e) {
			logger.warning("In case of error, did not manage to reset sending message");
			ClientSession.printException(e);
		}

	}

	/**
	 * precises that the connection has been overridden.
	 */
	public void markAsIrrelevant() {
		this.relevant = false;

	}

	/**
	 * @return true i the connection is relevant, meaning that the page should be
	 *         written.
	 */
	public boolean isRelevant() {
		return this.relevant;
	}

}
