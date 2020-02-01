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
import org.openlowcode.design.data.ObjectIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.argument.LargeBinaryArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.module.system.design.SystemModule;

/**
 * Adds an image content to the object, that will be stored as PNG
 * 
 * * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ImageContent
		extends
		Property<ImageContent> {
	private UniqueIdentified uniqueidentified;

	/**
	 * creates an image content property on this object
	 * 
	 * @param name a unique name amongst image contents on this data object. Should
	 *             be a valid java attribute name
	 */
	public ImageContent(String name) {
		super(name, "IMAGECONTENT");

	}

	@Override
	public void controlAfterParentDefinition() {
		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		DataObjectDefinition binaryfiledef = SystemModule.getSystemModule().getBinaryFile();
		StoredElement imagefileid = new ObjectIdStoredElement(this.getName() + "IMGID", binaryfiledef);
		imagefileid.setGenericsName("IMGID");
		this.addElement(imagefileid);
		StoredElement thumbnailfileid = new ObjectIdStoredElement(this.getName() + "THBID", binaryfiledef);
		thumbnailfileid.setGenericsName("THBID");
		this.addElement(thumbnailfileid);
		DataAccessMethod setimage = new DataAccessMethod("SETIMAGE", null, false);
		setimage.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", this.parent)));
		setimage.addInputArgument(new MethodArgument("IMAGE", new LargeBinaryArgument("IMAGE", false)));
		setimage.addInputArgument(new MethodArgument("THUMBNAIL", new LargeBinaryArgument("THUMBNAIL", false)));
		this.addDataAccessMethod(setimage);
		DataAccessMethod getthumbnail = new DataAccessMethod("GETTHUMBNAIL",
				new LargeBinaryArgument("THUMBNAIL", false), false);
		getthumbnail.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", this.parent)));
		this.addDataAccessMethod(getthumbnail);
		DataAccessMethod getfullimage = new DataAccessMethod("GETFULLIMAGE",
				new LargeBinaryArgument("FULLIMAGE", false), false);
		getfullimage.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", this.parent)));
		this.addDataAccessMethod(getfullimage);
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
		return dependencies;
	}

	@Override
	public void setFinalSettings() {
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
