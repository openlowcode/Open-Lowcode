/********************************************************************************
 * Copyright (c) 2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.properties;

import java.math.BigDecimal;
import java.util.logging.Logger;

import org.openlowcode.tools.messages.SFile;
import org.openlowcode.module.system.data.Binaryfile;
import org.openlowcode.module.system.data.BinaryfileDefinition;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectPayload;
import org.openlowcode.server.data.DataObjectProperty;
import org.openlowcode.server.data.storage.StoredField;

/**
 * a data object with an image content will hold a main image. Both a thumbnail
 * and a full image are stored for the object. By default,the thumbnail is
 * shown, and when clicking on it, the full image is shown
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> parent data object of the image content property
 */
public class Imagecontent<E extends DataObject<E> & UniqueidentifiedInterface<E>> extends DataObjectProperty<E> {
	private StoredField<String> imgid;
	private StoredField<String> thbid;
	@SuppressWarnings("unused")
	private Uniqueidentified<E> uniqueidentified;
	private ImagecontentDefinition<E> imagecontentdefinition;
	private static Logger logger = Logger.getLogger(Imagecontent.class.getName());

	/**
	 * creates a property specifying the data object has an object content
	 * 
	 * @param definition    definition of the property
	 * @param parentpayload parent object payload
	 */
	@SuppressWarnings("unchecked")
	public Imagecontent(ImagecontentDefinition<E> definition, DataObjectPayload parentpayload) {
		super(definition, parentpayload);
		imgid = (StoredField<String>) this.field.lookupOnName(this.getName() + "IMGID");
		thbid = (StoredField<String>) this.field.lookupOnName(this.getName() + "THBID");
		this.imagecontentdefinition = definition;

	}

	/**
	 * gets the thumbnail for the object
	 * 
	 * @param object data object
	 * @return the thumbnail file
	 */
	public SFile getthumbnail(E object) {
		DataObjectId<Binaryfile> thumbnailid = new DataObjectId<Binaryfile>(thbid.getPayload(),
				BinaryfileDefinition.getBinaryfileDefinition());
		Binaryfile binaryfile = Binaryfile.readone(thumbnailid);
		if (binaryfile != null)
			return binaryfile.getFilecontent();
		return new SFile();
	}

	/**
	 * gets the full image for the object
	 * 
	 * @param object data object
	 * @return the full image
	 */
	public SFile getfullimage(E object) {
		DataObjectId<Binaryfile> imageid = new DataObjectId<Binaryfile>(imgid.getPayload(),
				BinaryfileDefinition.getBinaryfileDefinition());
		Binaryfile binaryfile = Binaryfile.readone(imageid);
		if (binaryfile != null)
			return binaryfile.getFilecontent();
		return new SFile();
	}

	/**
	 * sets the image for the data object. Persistence is done both on the data
	 * object and on the two files stored on the server
	 * 
	 * @param object    data object
	 * @param image     full image
	 * @param thumbnail thumbnail image
	 */
	public void setimage(E object, SFile image, SFile thumbnail) {
		boolean somethingdone = false;
		if (image != null)
			if (!image.isEmpty()) {
				logger.info("Treating image as it is not void, filename = " + image.getFileName() + ", size = "
						+ image.getLength() + "B");
				Binaryfile imagefile = new Binaryfile();
				imagefile.setFilecontent(image);
				imagefile.setFilename(image.getFileName());
				imagefile.setFilesize(new BigDecimal(image.getLength()));
				imagefile.insert();
				DataObjectId<Binaryfile> imageid = imagefile.getId();
				imgid.setPayload(imageid.getId());
				somethingdone = true;
			}
		if (thumbnail != null)
			if (!thumbnail.isEmpty()) {
				Binaryfile thumbnailfile = new Binaryfile();
				thumbnailfile.setFilecontent(thumbnail);
				thumbnailfile.setFilename(thumbnail.getFileName());
				thumbnailfile.setFilesize(new BigDecimal(thumbnail.getLength()));

				thumbnailfile.insert();
				DataObjectId<Binaryfile> thumbnailid = thumbnailfile.getId();
				thbid.setPayload(thumbnailid.getId());
				somethingdone = true;
			}
		if (somethingdone) {
			if (imagecontentdefinition.getParentObject().hasProperty("ITERATED")) {
				@SuppressWarnings({ "rawtypes", "unchecked" })
				IteratedInterface<E> iterated = (IteratedInterface) object;
				iterated.setupdatenote("Changed image content");
			}
			object.update();
		}

	}

	/**
	 * gets the id of the binary file object storing the full image
	 * 
	 * @return the id of the binary file object storing the full image
	 */
	public DataObjectId<Binaryfile> getImgid() {
		return new DataObjectId<Binaryfile>(imgid.getPayload(), BinaryfileDefinition.getBinaryfileDefinition());
	}

	/**
	 * gets the id of the binary file object storing the thumbnail image
	 * 
	 * @return the id of the binary file object storing the thumbnail image
	 */
	public DataObjectId<Binaryfile> getThbid() {
		return new DataObjectId<Binaryfile>(thbid.getPayload(), BinaryfileDefinition.getBinaryfileDefinition());
	}

	/**
	 * @param uniqueidentified
	 */
	public void setDependentPropertyUniqueidentified(Uniqueidentified<E> uniqueidentified) {
		this.uniqueidentified = uniqueidentified;

	}

}
