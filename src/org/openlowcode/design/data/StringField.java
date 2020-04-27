/********************************************************************************
 * Copyright (c) 2019-2020 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.design.data;

import java.io.IOException;

import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;

/**
 * A field holding text string on a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class StringField
		extends
		Field {

	private int length;
	private int indextype;
	private boolean richtextdisplay;
	/**
	 * No index
	 */
	public static int INDEXTYPE_NONE = 0;
	/**
	 * An easy search index where the text field is also stored in another field in
	 * lower case and without special characters, ensuring similar strings are found
	 * (e.g. Search for newyork will find also 'New-York'). Field is also added to
	 * search page as a search criteria
	 */
	public static int INDEXTYPE_EASYSEARCH = 1;
	/**
	 * A normal index on the field. field is also added to the search page as a
	 * search criteria
	 */
	public static int INDEXTYPE_RAWINDEX = 2;
	/**
	 * no index, but the field is also added to the search page as a search criteria
	 */
	public static int INDEXTYPE_SEARCHWITHNOINDEX = 3;

	/**
	 * field is free but expected to be part of a list. In search, a list of values
	 * is provided, and the field is a search criteria (not yet implemented)
	 * 
	 * @since 1.6
	 */
	public static int INDEXTYPE_LISTOFVALUESWITHSEARCH = 4;

	/**
	 * field is free but expected to be part of a list. The field does not appear in
	 * search screens by default, but is is possible to get the list of existing
	 * values efficiently on big tables thanks to index
	 * 
	 * @since 1.6
	 */
	public static int INDEXTYPE_LISTOFVALUESWITHINDEX = 5;

	/**
	 * field is free but expected to be part of a list. The field does not appear in
	 * search screens by default, but is is possible to get the list of existing
	 * values (though with bad performance on big tables) as there is no index
	 * 
	 * @since 1.6
	 */
	public static int INDEXTYPE_LISTOFVALUESWITHNOINDEX = 6;

	private StoredElement plainfield;
	private StoredElement cleantext;

	/**
	 * @return true if the field has a list of values helper
	 * @since 1.6
	 */
	public boolean hasListOfValuesHelper() {
		if (indextype == INDEXTYPE_LISTOFVALUESWITHSEARCH) return true;
		if (indextype == INDEXTYPE_LISTOFVALUESWITHINDEX) return true;
		if (indextype == INDEXTYPE_LISTOFVALUESWITHNOINDEX) return true;
		return false;
	}
	
	
	public void setRichTextDisplay() {
		this.richtextdisplay = true;
	}

	/**
	 * create a text field with standard priority
	 * 
	 * @param name        short name of the field (valid java and sql name)
	 * @param displayname name for display in plain language in the default language
	 * @param tooltip     long mouse roll-over tooltip
	 * @param length      storage length of the field in the database
	 * @param indextype   index type as defined in the static string in this class
	 * @param richtext    if true, text is using Open Lowcode RichText
	 */
	public StringField(String name, String displayname, String tooltip, int length, int indextype, boolean richtext) {
		this(name, displayname, tooltip, length, indextype);
		if (richtext)
			this.setRichTextDisplay();
	}

	/**
	 * create a text field that is not rich text with standard priority
	 * 
	 * @param name        short name of the field (valid java and sql name)
	 * @param displayname name for display in plain language in the default language
	 * @param tooltip     long mouse roll-over tooltip
	 * @param length      storage length of the field in the database
	 * @param indextype   index type as defined in the static string in this class
	 */
	public StringField(String name, String displayname, String tooltip, int length, int indextype) {

		super(name, displayname, tooltip);
		this.length = length;
		this.indextype = indextype;
		this.richtextdisplay = false;
		plainfield = new StringStoredElement("", length);
		if ((this.indextype == StringField.INDEXTYPE_LISTOFVALUESWITHINDEX)
				|| (this.indextype == StringField.INDEXTYPE_LISTOFVALUESWITHNOINDEX)
				|| (this.indextype == StringField.INDEXTYPE_LISTOFVALUESWITHSEARCH))
			this.setHasFieldValuesQuery();
			if (this.indextype == INDEXTYPE_RAWINDEX) {
				this.AddElementWithSearch(plainfield, new SearchWidgetDefinition(true, name, displayname));

			}
		if (this.indextype == INDEXTYPE_SEARCHWITHNOINDEX) {
			this.AddElementWithSearch(plainfield, new SearchWidgetDefinition(false, name, displayname));

		}

		if (this.indextype == StringField.INDEXTYPE_LISTOFVALUESWITHSEARCH) {
			this.AddElementWithSearch(plainfield, new SearchWidgetDefinition(true, name, displayname,
					SearchWidgetDefinition.TYPE_TEXTCHOICE, SearchWidgetDefinition.POSTTREATMENT_NONE));
		}

		if (this.indextype == StringField.INDEXTYPE_EASYSEARCH) {

			cleantext = new StringStoredElement("CLEAN", length);
			this.AddElementWithSearch(cleantext, new SearchWidgetDefinition(true, name, displayname,
					SearchWidgetDefinition.POSTTREATMENT_EASYTEXTSEARCH));

			this.addIndex(new Index("EASYSEARCH", cleantext, false));
		}

		if ((this.indextype == StringField.INDEXTYPE_RAWINDEX)
				|| (this.indextype == StringField.INDEXTYPE_LISTOFVALUESWITHSEARCH)
				|| (this.indextype == StringField.INDEXTYPE_LISTOFVALUESWITHINDEX)) {
			this.addIndex(new Index("RAWSEARCH", plainfield, false));

		}
	}

	/**
	 * @param name            short name of the field (valid java and sql name)
	 * @param displayname     name for display in plain language in the default
	 *                        language
	 * @param tooltip         long mouse roll-over tooltip
	 * @param length          storage length of the field in the database
	 * @param indextype       index type as defined in the static string in this
	 *                        class
	 * @param displaypriority display priority between -1000 (low priority) and 1000
	 *                        (high priority)
	 */
	public StringField(
			String name,
			String displayname,
			String tooltip,
			int length,
			int indextype,
			int displaypriority) {
		this(name, displayname, tooltip, length, indextype);
		this.setDisplayPriority(displaypriority);
	}

	/**
	 * @param name            short name of the field (valid java and sql name)
	 * @param displayname     name for display in plain language in the default
	 *                        language
	 * @param tooltip         long mouse roll-over tooltip
	 * @param length          storage length of the field in the database
	 * @param indextype       index type as defined in the static string in this
	 *                        class
	 * @param displaypriority display priority between -1000 (low priority) and 1000
	 *                        (high priority)
	 * @param richtext        if true, text is using Open Lowcode RichText
	 */
	public StringField(
			String name,
			String displayname,
			String tooltip,
			int length,
			int indextype,
			int displaypriority,
			boolean richtext) {
		this(name, displayname, tooltip, length, indextype, displaypriority);
		if (richtext)
			this.setRichTextDisplay();
	}

	@Override
	public String getDataObjectFieldName() {
		return "StringDataObjectField";
	}

	@Override
	public String getDataObjectConstructorAttributes() {
		boolean easysearch = false;
		if (this.indextype == INDEXTYPE_EASYSEARCH)
			easysearch = true;
		return "\"" + this.getName() + "\",\"" + this.getDisplayname() + "\",\"" + this.getTooltip() + "\","
				+ this.length + "," + easysearch + "," + this.isNoUserEdition() + "," + this.isShowintitle() + ","
				+ this.isShowinbottomnotes() + "," + this.getDisplayPriority() + ",-1," + this.richtextdisplay;
	}

	@Override
	public String getJavaType() {
		return "String";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public StoredElement getMainStoredElementForCompositeIndex() {
		if (this.indextype == StringField.INDEXTYPE_EASYSEARCH)
			return cleantext;
		return plainfield;
	}

	@Override
	public Field copy(String newname, String newdisplaylabel) {
		return new StringField((newname != null ? newname : this.getName()),
				(newdisplaylabel != null ? newdisplaylabel : this.getDisplayname()), this.getTooltip(), length,
				indextype, this.getDisplayPriority(), richtextdisplay);
	}
	/**
	 * @return the length of the field
	 * @since 1.6
	 */
	public int getLength() {
		return this.length;
	}
}
