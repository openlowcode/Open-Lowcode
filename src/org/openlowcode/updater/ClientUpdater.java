/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.updater;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.MessageBufferedWriter;
import org.openlowcode.tools.messages.MessageSimpleReader;
import org.openlowcode.tools.messages.SFile;
import org.openlowcode.tools.trace.FileFormatter;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.concurrent.Task;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

/**
 * An small utility that downloads the latest client JAR when the client does
 * not have the good version of the client. After done, it relaunches the client
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ClientUpdater
		extends
		Application {
	private static Logger logger = Logger.getLogger(ClientUpdater.class.getName());
	private static String serverurl;

	private static Pane mainpane;
	private static String addresstoconnect;
	private static String logsetupwarning = null;
	private static FileHandler logfilehandler;

	/**
	 * tries to setup logs. Does not throw an exception even if there is an issue
	 * (typically log folder missing for logs), so that upgrade can continue even if
	 * issue with logs
	 */
	public static void attemptToSetupLogs() {
		try {
			Logger mainlogger = Logger.getLogger("");
			for (int i = 0; i < mainlogger.getHandlers().length; i++) {
				mainlogger.removeHandler(mainlogger.getHandlers()[i]);
			}
			logfilehandler = new FileHandler("./log/OpenLowcodeUpdater%g.log", 10000000, 1000, true);
			logfilehandler.setLevel(Level.FINEST);
			logfilehandler.setFormatter(new FileFormatter(true));

			Logger openlowcode = Logger.getLogger("org.openlowcode");
			openlowcode.addHandler(logfilehandler);
			openlowcode.setUseParentHandlers(false);
			openlowcode.setLevel(Level.ALL);

		} catch (Exception e) {
			logsetupwarning = "Exception while setting logs: " + e.getClass().getName() + " - " + e.getMessage();
		}
	}

	/**
	 * @param e put the exception in logs
	 */
	public static void treatInLogs(Throwable e) {
		logger.warning(" Exception recorded " + e.getClass().getName() + " " + e.getMessage());
		for (int i = 0; i < e.getStackTrace().length; i++)
			logger.warning("     " + e.getStackTrace()[i]);
	}

	/**
	 * drop current threads in logs for debugging purposes
	 */
	public static void dropThreadsInLogs() {
		logger.warning("   --------------------------------------------------------------- ");
		logger.warning("");
		logger.warning("   >>>             T H R E A D    D R O P P I N G              <<<");
		logger.warning("");
		logger.warning("   ---------------------------------------------------------------");
		Map<Thread, StackTraceElement[]> threads = Thread.getAllStackTraces();
		Iterator<Entry<Thread, StackTraceElement[]>> threaditerator = threads.entrySet().iterator();
		while (threaditerator.hasNext()) {
			Entry<Thread, StackTraceElement[]> set = threaditerator.next();
			Thread thread = set.getKey();
			logger.warning(
					" Thread --------- " + thread.getName() + " id " + thread.getId() + " - " + thread.getState());
			StackTraceElement[] element = set.getValue();
			for (int i = 0; i < element.length; i++)
				logger.warning("        * " + element[i]);
		}
		logger.warning("");
	}

	public static void main(String[] args) {
		attemptToSetupLogs();
		if (args.length == 2) {
			serverurl = args[0];
			addresstoconnect = args[1];
		} else {
			serverurl = null;
		}
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				logfilehandler.flush();
				logfilehandler.close();

			}
		});
		launch(new String[0]);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		primaryStage.setTitle("Welcome to Open Lowcode Client Updater");
		primaryStage.getIcons().add(new Image("css/OLC64-new.png"));
		primaryStage.getIcons().add(new Image("css/OLC32-new.png"));

		mainpane = new VBox(3);

		mainpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
		mainpane.setPadding(new Insets(5, 5, 5, 5));
		primaryStage.setScene(new Scene(mainpane, 400, 450));
		primaryStage.show();
		Label openlowcodeupdater = new Label("Open Lowcode Updater");
		openlowcodeupdater.setFont(Font.font(openlowcodeupdater.getFont().getName(), FontWeight.BOLD,
				openlowcodeupdater.getFont().getSize() * 1.4));
		mainpane.getChildren().add(openlowcodeupdater);
		if (logsetupwarning != null) {
			Label error = new Label("Please report this technical error:\n" + logsetupwarning);
			error.setTextFill(Color.RED);
		}

		Label waiting = new Label("Waiting 5 seconds for client to shut-down");
		waiting.setWrapText(true);
		mainpane.getChildren().add(waiting);
		ProgressBar pb = new ProgressBar();
		mainpane.getChildren().add(pb);
		Integer STARTTIME = 4;
		SimpleIntegerProperty timeSeconds = new SimpleIntegerProperty(STARTTIME * 100);
		pb.progressProperty().bind(timeSeconds.divide(STARTTIME * 100.0).subtract(1).multiply(-1));
		Timeline timeline = new Timeline();

		timeline.getKeyFrames().add(

				new KeyFrame(Duration.seconds(STARTTIME + 1), new KeyValue(timeSeconds, 0)));
		timeline.playFromStart();
		Task<Void> sleeper = new Task<Void>() {
			@Override
			protected Void call() throws Exception {
				try {

					Thread.sleep(5000);

				} catch (InterruptedException e) {
					treatInLogs(e);
				}
				return null;
			}
		};
		sleeper.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
			public void handle(WorkerStateEvent event) {
				try {

					int port = 8080;
					int columnindex = serverurl.indexOf(':');
					int slashindex = serverurl.indexOf('/');
					int serverend = slashindex; // by default, no port
					if ((columnindex >= 0) && (columnindex < slashindex)) {
						// there is a column before first slash : port is indicated
						serverend = columnindex;
						String portstring = serverurl.substring(columnindex + 1, slashindex);
						port = new Integer(portstring).intValue();
					}
					String server = serverurl.substring(0, serverend);
					logger.info("-------------------- Launched Updater -------------------");
					logger.info("   Server coordinates = " + server);
					logger.info("   port = " + port);
					Label serverlabel = new Label("Contacting Server at URL " + server + " and port " + port);
					serverlabel.setWrapText(true);
					mainpane.getChildren().add(serverlabel);
					Socket clientsocket = new Socket(server, port);
					logger.finer("  - Succesfully established socket");

					InputStreamReader inputstreamreader = new InputStreamReader(clientsocket.getInputStream(),
							Charset.forName("UTF-8"));
					logger.finer("  - Established input stream reader");
					BufferedReader bufferedreader = new BufferedReader(inputstreamreader, 9090);
					logger.finer("  - Established buffered reader");
					// dropThreadsInLogs();
					MessageSimpleReader reader = new MessageSimpleReader(bufferedreader);
					logger.info("  ** Established reader");
					OutputStreamWriter outputstreamwriter = new OutputStreamWriter(clientsocket.getOutputStream(),
							Charset.forName("UTF-8"));
					logger.finer("  - Established outputstreamwriter");
					BufferedWriter bufferedwriter = new BufferedWriter(outputstreamwriter);
					logger.finer("  - Established bufferedwriter");
					MessageBufferedWriter writer = new MessageBufferedWriter(bufferedwriter, true);
					logger.info("  ** Established writer");

					Label connectserver = new Label("Connected Server  " + serverurl);
					connectserver.setWrapText(true);
					mainpane.getChildren().add(connectserver);
					logger.info("  Starting message");
					writer.startNewMessage();
					logger.info("  Put first element in message");

					writer.startStructure("DOWNLOADCLIENT");
					writer.endStructure("DOWNLOADCLIENT");
					writer.endMessage();
					logger.info("  message ended");
					writer.flushMessage();
					logger.info("  message flushed");
					Label sendtdownloadrequest = new Label("Sent download request  ");
					mainpane.getChildren().add(sendtdownloadrequest);
					reader.returnNextMessageStart();
					logger.info("  started reading message");
					reader.returnNextStartStructure("NEWCLIENTJAR");
					SFile newjar = reader.returnNextLargeBinary("JAR");
					logger.info(
							"Download client " + newjar.getFileName() + " size: " + (newjar.getLength() / 1024) + "KB");
					Label downloadclient = new Label(
							"Download client " + newjar.getFileName() + " size: " + (newjar.getLength() / 1024) + "KB");
					downloadclient.setWrapText(true);
					mainpane.getChildren().add(downloadclient);
					reader.returnNextEndStructure("NEWCLIENTJAR");
					reader.returnNextEndMessage();
					logger.info("Finished reading message ");
					String clientpath = "." + File.separator + "lib" + File.separator + "OLcClient.jar";
					File existingclient = new File(clientpath);
					if (existingclient.exists()) {
						Date date = new Date();
						SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
						existingclient.renameTo(new File(clientpath + "." + sdf.format(date)));
						logger.info("renamed client file to " + clientpath + "." + sdf.format(date));
					}

					FileOutputStream fos = new FileOutputStream(new File(clientpath), false);
					logger.info("opening output streal to update client at " + clientpath);
					fos.write(newjar.getContent());
					fos.close();
					logger.info("finished updating client file");
					Label updatecompleted = new Label("Update is completed. Client will relaunch in 4 seconds.");
					updatecompleted.setWrapText(true);
					mainpane.getChildren().add(updatecompleted);
					clientsocket.close();
					Thread closeandlaunchclient = new Thread() {

						@Override
						public void run() {
							try {
								Thread.sleep(4);
								Runtime.getRuntime().exec(
										"javaw -classpath ./lib/OLcClient.jar -mx512m org.openlowcode.client.runtime.OLcClient  "
												+ addresstoconnect + " NOLOG");
								System.exit(0);
							} catch (Throwable e) {
								treatInLogs(e);
								mainpane.getChildren().add(new Label("Unexpected error launching client"));
								Label exceptionlabel = new Label("Exception " + e.getMessage());
								exceptionlabel.setWrapText(true);
								mainpane.getChildren().add(exceptionlabel);
								System.err.println("Exception " + e);
							}
						}

					};
					closeandlaunchclient.start();

				} catch (Throwable e) {
					treatInLogs(e);
					mainpane.getChildren().add(new Label("Unexpected error, please contact technical support"));
					Label exceptionlabel = new Label("Exception " + e.getMessage());
					exceptionlabel.setWrapText(true);
					mainpane.getChildren().add(exceptionlabel);

				}
				logger.fine("Main connection thread is finished !!!");
			}
		});
		new Thread(sleeper).start();

	}

}
