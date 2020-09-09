/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.client.runtime;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Date;

import org.openlowcode.OLcVersion;
import org.openlowcode.client.graphic.CPage;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageNodeCatalog;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.pages.ClientUpgradePage;
import org.openlowcode.client.graphic.widget.*;
import org.openlowcode.client.graphic.widget.schedule.GanttDisplay;
import org.openlowcode.client.graphic.widget.schedule.GanttTaskCell;
import org.openlowcode.client.runtime.ClientMainFrame;
import org.openlowcode.client.runtime.ClientMainFrame.ClientUpgradePageGenerator;
import org.openlowcode.client.runtime.PageActionManager.ActionSourceTransformer;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.OLcRemoteException;

import javafx.scene.control.TableView;
import javafx.scene.control.TableRow;

/**
 * The main class of the javafx client for Open Lowcode
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class OLcClient
		extends
		Application {
	
	public static void setUrlToConnect(String urltoconnect) {
		OLcClient.urltoconnect = urltoconnect;
	}
	public static void setNoLog(boolean nolog) {
		OLcClient.nolog = nolog;
	}
	private static String urltoconnect;
	private static boolean nolog = false;

	public static void main(String[] args) {
		if (args.length >= 1)
			urltoconnect = args[0];

		if (args.length >= 2)
			if (args[1].equals("NOLOG"))
				nolog = true;
		launch(args);

	}
	
	/**
	 * @return
	 */
	public String getUpdaterJar() {
		return "OLcUpdater.jar";
	}
	/**
	 * @return
	 */
	public String getUpdaterClass() {
		return "org.openlowcode.updater.ClientUpdater";
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		new ClientMainFrame(stage, OLcVersion.version, // clientversion
				OLcVersion.versiondate, // clientversiondate
				getClientUpdateGenerator(), // clientupgradepage
				getActionSourceTransformer(), // actionsourcetransformer
				getPageCatalog(), // page catalog
				urltoconnect, // urltoconnect
				nolog, // nolog
				"css/OLC64-new.png", // icon
				"css/OLC32-new.png", // bigicon
				"css/QM64.png", // Question Mark
				"css/openlowcode.css" // CSS
		);

	}

	private ClientUpgradePageGenerator getClientUpdateGenerator() {
		return new ClientUpgradePageGenerator() {

			@Override
			public CPage generateClientUpgradePage(String clientversion, String serverversion, Date serverupdatedate) {
				return new ClientUpgradePage(clientversion, serverversion, serverupdatedate,getUpdaterJar(),getUpdaterClass());
			}

		};
	}

	/**
	 * @return the catalog of widgets that can be put into a page on the Open
	 *         lowcode client
	 */
	public CPageNodeCatalog getPageCatalog() {
		return new CPageNodeCatalog() {

			@Override
			public CPageNode getNodeFromCode(String structure, MessageReader reader, CPageSignifPath path)
					throws IOException, OLcRemoteException {
				CPageNode answer;
				switch (structure) {
				case "COMPONENTBAND":
					answer = new CComponentBand(reader, path);
					break;

				case "ACTIONBUTTON":
					answer = new CActionButton(reader, path);
					break;
				case "TXF":
					answer = new CTextField(reader, path);
					break;
				case "DAT":
					answer = new CDateField(reader, path);
					break;
				case "CTF":
					answer = new CChoiceField(reader, path);
					break;
				case "MCF":
					answer = new CMultipleChoiceField(reader, path);
					break;
				case "DCF":
					answer = new CDecimalField(reader, path);
					break;
				case "INF":
					answer = new CIntegerField(reader, path);
					break;
				case "TPF":
					answer = new CTimePeriodField(reader, path);
					break;

				case "PAGETEXT":
					answer = new CPageText(reader, path);
					break;
				case "OBJDIS":
					answer = new CObjectDisplay(reader, path);
					break;
				case "OBJIDS":
					answer = new CObjectIdStorage(reader, path);
					break;
				case "TEXTST":
					answer = new CTextStorage(reader, path);
					break;
				case "OBJARR":
					answer = new CObjectArray(reader, path);
					break;
				case "POPUPBTN":
					answer = new CPopupButton(reader, path);
					break;
				case "OBJTRA":
					answer = new CObjectTreeArray(reader, path);
					break;
				case "OBJBND":
					answer = new CObjectBand(reader, path);
					break;
				case "SPR":
					answer = new CSeparator(reader, path);
					break;
				case "OBJARF":
					answer = new CObjectArrayField(reader, path);
					break;
				case "OBJSTO":
					answer = new CObjectStorage(reader, path);
					break;
				case "FLDSRC":
					answer = new CFieldSearcher(reader, path);
					break;
				case "MNUBAR":
					answer = new CMenuBar(reader, path);
					break;
				case "SCURVE":
					answer = new CObjectSCurve(reader, path);
					break;
				case "TABPANE":
					answer = new CTabPane(reader, path);
					break;
				case "IMC":
					answer = new CImageChooser(reader, path);
					break;
				case "IMD":
					answer = new CImageDisplay(reader, path);
					break;
				case "FLC":
					answer = new CFileChooser(reader, path);
					break;
				case "FLD":
					answer = new CFileDownload(reader, path);
					break;
				case "GANNTC":
					answer = new CGanntChart(reader, path);
					break;
				case "TSF":
					answer = new CTimeslotField(reader, path);
					break;
				case "GRD":
					answer = new CGrid(reader, path);
					break;
				case "CLB":
					answer = new CCollapsibleBand(reader, path);
					break;
				case "ACHART":
					answer = new CAreaChart(reader, path);
					break;
				case "TRE":
					answer = new CTimeRangeEntry(reader,path);
					break;
				case "WDP":
					answer = new  CWidthProtector(reader,path);
					break;
				default:
					throw new RuntimeException(String.format(
							"no valid component found for CSP Node parsing, node = '" + structure + "' %s ",
							reader.getCurrentElementPath()));
				}
				// note: no need to close structure: every subclass will close its structure
				return answer;
			}

		};
	}

	private ActionSourceTransformer getActionSourceTransformer() {
		return new ActionSourceTransformer() {

			@Override
			public Object getParentWidget(Object originwidget) {
				if (originwidget instanceof GanttTaskCell) {
					GanttTaskCell<?> gantttaskcell = (GanttTaskCell<?>) originwidget;
					GanttDisplay<?> parentdisplay = gantttaskcell.getParentGanttDisplay();
					return parentdisplay;
				}
				if (originwidget instanceof TableRow) {
					TableRow<?> tablerow = (TableRow<?>) originwidget;
					TableView<?> table = tablerow.getTableView();
					return table;
				}
				return originwidget;
			}
		};
	}
}
