/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data.properties.basic;

import java.io.IOException;
import java.util.ArrayList;

import org.openlowcode.design.data.DataAccessMethod;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.LargeBinaryArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * This property adds a table of attachment (files with a name) to the object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class FileContent
		extends
		Property<FileContent> {
	private UniqueIdentified uniqueidentified;

	/**
	 * 
	 * the widget showing the attachments on an object
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public class AttachmentTableWidget
			extends
			Widget {
		private FileContent filecontent;

		/**
		 * creates the widget showing the attachments of an object
		 * 
		 * @param filecontent related file content property
		 */
		AttachmentTableWidget(FileContent filecontent) {
			super("ATTACHMENTTABLE");
			this.filecontent = filecontent;
		}

		@Override
		public String[] getImportStatements() {
			Module module = filecontent.getParent().getOwnermodule();
			ArrayList<String> importstatements = new ArrayList<String>();
			String objectvariable = StringFormatter.formatForAttribute(filecontent.getParent().getName());
			importstatements.add("import org.openlowcode.module.system.data.Objattachment;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SFileDownloader;");
			importstatements.add("import org.openlowcode.server.graphic.widget.SPopupButton;");
			importstatements.add("import " + module.getPath() + ".action.generated.AtgAddnewattachmentfor"
					+ objectvariable + "Action;");
			importstatements.add("import " + module.getPath() + ".action.generated.AtgDownloadattachmentfor"
					+ objectvariable + "Action;");
			importstatements.add("import " + module.getPath() + ".action.generated.AtgDeleteattachmentfor"
					+ objectvariable + "Action;");
			return importstatements.toArray(new String[0]);
		}

		@Override
		public void generateWidgetCode(SourceGenerator sg, Module module, String locationname) throws IOException {
			String objectvariable = StringFormatter.formatForAttribute(filecontent.getParent().getName());
			String objectclass = StringFormatter.formatForJavaClass(filecontent.getParent().getName());

			sg.wl("		// -----------------------------------------------------------------------------------------");
			sg.wl("		// Display all attachments");
			sg.wl("		// -----------------------------------------------------------------------------------------");
			sg.wl("		" + locationname
					+ ".addElement(new SPageText(\"Attachment Files\",SPageText.TYPE_TITLE,this));");
			sg.wl("		SObjectArray attachmentsarray = new SObjectArray(\"ATTACHMENTS\", this.getAttachments(),Objattachment.getDefinition(),this);");
			sg.wl("		attachmentsarray.forceRowHeight(1);");


			sg.wl("		SComponentBand addattachmentpopup = new SComponentBand(SComponentBand.DIRECTION_DOWN,this);");
			sg.wl("		SFileChooser addattachmentfilechooser = new SFileChooser(this,\"ADDATTACHMENT\",\"Select file to attach\");");
			sg.wl("		addattachmentpopup.addElement(addattachmentfilechooser);");
			sg.wl("		AtgAddnewattachmentfor" + objectvariable
					+ "Action.ActionRef addattachmentref = AtgAddnewattachmentfor" + objectvariable
					+ "Action.get().getActionRef();");
			sg.wl("		STextField comment = new STextField(\"Comment\",\"ATTACHMENTCOMMENT\",\"Context of the attachment\",");
			sg.wl("				800, \"\", false,this, false, false, false, addattachmentref);");
			sg.wl("		addattachmentpopup.addElement(comment);");
			sg.wl("		addattachmentref.set" + objectclass + "(objectdisplaydefinition.getAttributeInput("
					+ objectclass + ".getDefinition().getIdMarker())); ");
			sg.wl("		addattachmentref.setFile(addattachmentfilechooser.getLargeBinaryInput());");
			sg.wl("		addattachmentref.setComment(comment.getTextInput());");
			sg.wl("		SActionButton addattachmentbutton = new SActionButton(\"Add\", addattachmentref, this);");
			sg.wl("		addattachmentpopup.addElement(addattachmentbutton);");

			sg.wl("		// download file");
			sg.wl("		AtgDownloadattachmentfor" + objectvariable
					+ "Action.InlineActionRef attachmentdownloadaction = AtgDownloadattachmentfor" + objectvariable
					+ "Action.get().getInlineActionRef();");
			sg.wl("		attachmentdownloadaction.set" + objectclass + "(objectdisplaydefinition.getAttributeInput("
					+ objectclass + ".getDefinition().getIdMarker()));");
			sg.wl("		attachmentdownloadaction.setAttachmentid(attachmentsarray.getAttributeInput(Objattachment.getIdMarker())); ");
			sg.wl("		attachmentsarray.addDefaultAction(attachmentdownloadaction);");
			sg.wl("		SFileDownloader attachmentdownload = new SFileDownloader(\"DOWNLOADATTACHMENT\",");
			sg.wl("				this, ");
			sg.wl("				attachmentdownloadaction, ");
			sg.wl("				AtgDownloadattachmentfor" + objectvariable + "Action.get().getFileRef());");
			sg.wl("		" + locationname + ".addElement(attachmentdownload);");

			sg.wl("		SPopupButton addattachmentpopupbutton = new SPopupButton(this, addattachmentpopup,\"New Attachment\",\"allows to add file from folder\",addattachmentref);");

			sg.wl("		SComponentBand attachmentbuttonband = new SComponentBand(SComponentBand.DIRECTION_RIGHT, this);");
			sg.wl("		attachmentbuttonband.addElement(addattachmentpopupbutton);");
			sg.wl("		AtgDeleteattachmentfor" + objectvariable
					+ "Action.ActionRef deleteattachmentref = AtgDeleteattachmentfor" + objectvariable
					+ "Action.get().getActionRef();");
			sg.wl("		deleteattachmentref.setObjectid(objectdisplaydefinition.getAttributeInput(" + objectclass
					+ ".getDefinition().getIdMarker())); ");
			sg.wl("		deleteattachmentref.setObjattachmentid(attachmentsarray.getAttributeInput(Objattachment.getIdMarker())); ");
			sg.wl("		SActionButton deleteattachmentbutton = new SActionButton(\"Delete\", deleteattachmentref, this);");
			sg.wl("		attachmentbuttonband.addElement(deleteattachmentbutton);");
			sg.wl("		" + locationname + ".addElement(attachmentbuttonband);");
			sg.wl("		" + locationname + ".addElement(attachmentsarray);		");			
			sg.wl("		// --------------- end display attachment -------------");

		}

		@Override
		public WidgetDisplayPriority getWidgetPriority() {
			return filecontent.attachmenttablepriority;
		}

	}

	/**
	 * @return the widget showing attachment files (to give it a priority)
	 */
	public Widget generateAttachmentTableWidget() {
		return new AttachmentTableWidget(this);
	}

	private WidgetDisplayPriority attachmenttablepriority;

	/**
	 * file content property to be added to the object to have attachments enabled.
	 * This is a prerequisite to the print-out method.
	 * 
	 */
	public FileContent() {
		super("FILECONTENT");

	}

	/**
	 * file content property to be added to the object to have attachments enabled.
	 * This is a prerequisite to the print-out method. This method allows to create
	 * a FileContent with a specific widget display priority to change layout of the
	 * display page of the object
	 * 
	 * @param attachmenttablepriority
	 */
	public FileContent(WidgetDisplayPriority attachmenttablepriority) {
		this();
		this.attachmenttablepriority = attachmenttablepriority;

	}

	@Override
	public void controlAfterParentDefinition() {
		if (this.attachmenttablepriority != null)
			this.attachmenttablepriority.checkIfValidForObject(this.parent);
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		DataObjectDefinition objectattachment = SystemModule.getSystemModule().getObjectAttachment();

		DataAccessMethod getallattachments = new DataAccessMethod("GETATTACHMENTS",
				new ArrayArgument(new ObjectArgument("attachments", objectattachment)), true);
		getallattachments.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		this.addDataAccessMethod(getallattachments);

		DataAccessMethod addattachment = new DataAccessMethod("ADDATTACHMENT", null, false);
		addattachment.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		addattachment
				.addInputArgument(new MethodArgument("attachment", new ObjectArgument("attachment", objectattachment)));
		addattachment.addInputArgument(new MethodArgument("file", new LargeBinaryArgument("file", false)));
		this.addDataAccessMethod(addattachment);

		DataAccessMethod deleteattachment = new DataAccessMethod("DELETEATTACHMENT", null, false);
		deleteattachment.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		deleteattachment.addInputArgument(
				new MethodArgument("attachmentid", new ObjectIdArgument("attachment", objectattachment)));
		this.addDataAccessMethod(deleteattachment);

		DataAccessMethod updateattachment = new DataAccessMethod("UPDATEATTACHMENT", null, false);
		updateattachment.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		updateattachment
				.addInputArgument(new MethodArgument("attachment", new ObjectArgument("attachment", objectattachment)));
		updateattachment.addInputArgument(new MethodArgument("newfile", new LargeBinaryArgument("file", false)));
		this.addDataAccessMethod(updateattachment);

	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(SystemModule.getSystemModule().getBinaryFile());
		dependencies.add(SystemModule.getSystemModule().getObjectAttachment());
		return dependencies;
	}

	@Override
	public void setFinalSettings() {
		// Nothing to do
	}

	@Override
	public String getJavaType() {
		return "#ERROR#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.tools.messages.SFile;");

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {
		return null;
	}

}
