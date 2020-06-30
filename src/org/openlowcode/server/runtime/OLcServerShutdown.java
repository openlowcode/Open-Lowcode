/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.runtime;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.logging.ConsoleHandler;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.tools.enc.AESCommunicator;
import org.openlowcode.tools.messages.MessageBufferedWriter;
import org.openlowcode.tools.messages.MessageSimpleReader;
import org.openlowcode.tools.trace.ConsoleFormatter;
import org.openlowcode.tools.trace.FileFormatter;

/**
 * program to be launched to shutdown the server (to be launched on the machine
 * running the server)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcServerShutdown {
	private static Logger logger = Logger.getLogger(OLcServerShutdown.class.getName());

	/**
	 * @param port
	 */
	public OLcServerShutdown(int port) {
		Logger mainlogger = Logger.getLogger("");
		try {
			for (int i = 0; i < mainlogger.getHandlers().length; i++) {
				mainlogger.removeHandler(mainlogger.getHandlers()[i]);
			}
			ConsoleHandler consolehandler = new ConsoleHandler();
			consolehandler.setFormatter(new ConsoleFormatter());
			consolehandler.setLevel(Level.FINER);
			mainlogger.addHandler(consolehandler);
			File file = new File("./log/");
			System.err.println("log folder = " + file.getAbsolutePath());
			FileHandler logfilehandler = new FileHandler("./log/OpenLowcodeClient%g.log", 10000000, 1000, true);
			logfilehandler.setLevel(Level.FINEST);
			logfilehandler.setFormatter(new FileFormatter(true));
			mainlogger.addHandler(logfilehandler);
			mainlogger.setUseParentHandlers(false);
			// ------------------------------------------------------------------------------
			logger.severe("Connection to Open-Lowcode Server on localhost port " + port + " to send shutdown message");
			Socket clientsocket = new Socket("localhost", port);
			MessageSimpleReader reader = new MessageSimpleReader(
					new BufferedReader(new InputStreamReader(clientsocket.getInputStream()), 9090));
			MessageBufferedWriter writer = new MessageBufferedWriter(
					new BufferedWriter(new OutputStreamWriter(clientsocket.getOutputStream())), false);
			@SuppressWarnings("unused")
			AESCommunicator aescommunicator = AESCommunicator.performServerHandshake(reader, writer);
			writer.startNewMessage();
			writer.startStructure("SHUTDOWN");
			writer.endStructure("SHUTDOWN");
			writer.endMessage();
			writer.flushMessage();
			logger.severe("Succesfully sent shutdown message to server on port " + port);
			reader.returnNextMessageStart();
			reader.returnNextStartStructure("SHUTDOWNOK");
			reader.returnNextEndStructure("SHUTDOWNOK");
			reader.returnNextEndMessage();

			logger.severe(
					"Got last message from server before shutdown, all connections are stopped. Server will stop in less than 50ms");
			reader.close();
			writer.close();
			clientsocket.close();
		} catch (Exception e) {
			logger.severe("could not execute correctly shutdown script: " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				logger.severe(e.getStackTrace()[i].toString());
			}
		}
	}

	public static void main(String[] args) {
		if (args.length != 1) {
			System.err.println("error: syntax java OLcServerShutdown [port]");
			System.err.println("        where");
			System.err.println("        port is the socket port of the server to shutdown");

			System.exit(1);
		}
		int port = new Integer(args[0]).intValue();
		new OLcServerShutdown(port);

	}

}
