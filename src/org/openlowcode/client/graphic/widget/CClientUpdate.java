/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.graphic.widget;

import java.util.logging.Logger;

import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.Callback;
import org.openlowcode.client.runtime.PageActionManager;

import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TabPane;
import javafx.scene.layout.Pane;
import javafx.stage.Window;

/**
 * A widget performing the update of the client
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CClientUpdate
		extends
		CPageNode {

	private static Logger logger = Logger.getLogger(CClientUpdate.class.getName());

	/**
	 * creates a client update for the special client update message
	 * 
	 * @param parentpath page of the parent widget
	 */
	public CClientUpdate(CPageSignifPath parentpath) {
		super(parentpath, "UPGRADEBUTTON");

	}

	@Override
	public CPageNode deepcopyWithCallback(Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public Node getNode(PageActionManager actionmanager, CPageData pagedata, Window window, TabPane[] parenttabpanes) {

		Pane thispane = CComponentBand.returnBandPane(CComponentBand.DIRECTION_RIGHT);

		Button button = new Button("Update Client");
		button.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		button.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				String fulllastaddress = actionmanager.getClientSession().getActiveClientDisplay().getConnectionBar()
						.getAddress().getText();
				String argumentcontent = actionmanager.getClientSession().getConnectionToServer().getServer() + ":"
						+ actionmanager.getClientSession().getConnectionToServer().getPort() + "/\" \""
						+ fulllastaddress;
				logger.severe("------------------ Launching client updater with attributes :");
				logger.severe(
						" updater address: " + actionmanager.getClientSession().getConnectionToServer().getServer()
								+ ":" + actionmanager.getClientSession().getConnectionToServer().getPort());
				logger.severe(" final address: " + fulllastaddress);
				logger.severe("---------------------------------------------------------------");
				try {
					Runtime.getRuntime()
							.exec("javaw -classpath ./lib/GalliumUpdater.jar gallium.updater.ClientUpdater \""
									+ argumentcontent + "\"");

					logger.warning("Successfully launched updater. Client will shutdown");
					System.exit(0);
				} catch (Throwable e) {
					logger.severe("Exception in launching updater " + e.getMessage());
					for (int i = 0; i < e.getStackTrace().length; i++)
						logger.severe(e.getStackTrace()[i].toString());

				}

			}

		});
		thispane.getChildren().add(button);
		Button stopbutton = new Button("No Thanks");
		stopbutton.setStyle("-fx-base: #ffffff; -fx-hover-base: #ddeeff;");
		thispane.getChildren().add(stopbutton);

		Thread autoupgrade = new Thread() {

			@Override
			public void run() {
				try {
					super.run();
					Thread.sleep(10000);
					button.fire();
				} catch (InterruptedException ie) {

				}
			}

		};
		autoupgrade.start();
		stopbutton.setOnAction(new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				autoupgrade.interrupt();
			}

		});
		return thispane;
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectdataloc) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public void mothball() {
		// do nohing
	}

}
