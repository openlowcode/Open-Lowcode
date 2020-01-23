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
import java.util.ArrayList;

import org.openlowcode.design.data.loader.LoaderElement;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;
import org.openlowcode.design.pages.SearchWidgetDefinition;
import org.openlowcode.tools.misc.Named;
import org.openlowcode.tools.misc.NamedList;

/**
 * An object element is a brick of an object. It will contain stored
 * information, binary search indexes, and potentially a list of search widgets
 * to display in search pages
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 */
public abstract class ObjectElement
		extends
		Named {
	private NamedList<LoaderElement> loaderelements;
	private NamedList<Element> elements;
	private NamedList<Index> indexes;
	private ArrayList<SearchWidgetDefinition> searchwidgetlist;
	private String displayname;
	private String tooltip;

	/**
	 * @return the list of search widgets for this object element
	 */
	public SearchWidgetDefinition[] getSearchWidgetList() {
		return searchwidgetlist.toArray(new SearchWidgetDefinition[0]);
	}

	/**
	 * @return label of the widget TODO should be moved to field, see issue #13
	 */
	public String getDisplayname() {
		return this.displayname;
	}

	/**
	 * @return tooltip roll-over tooltip for the element TODO should be moved to
	 *         field, see issue #13
	 */
	public String getTooltip() {
		return this.tooltip;
	}

	/**
	 * @param index adds an index to the object element
	 */
	public void addIndex(Index index) {
		this.indexes.add(index);
	}

	/**
	 * @param element adds a data element to the object element
	 */
	public void addElement(Element element) {
		this.elements.add(element);
	}

	/**
	 * adds an element with a search widget
	 * 
	 * @param element      data element (mostly stored element)
	 * @param searchwidget search widget to use for the element
	 */
	public void AddElementWithSearch(Element element, SearchWidgetDefinition searchwidget) {
		this.elements.add(element);
		searchwidget.setReferenceElement(element);
		this.searchwidgetlist.add(searchwidget);
	}

	/**
	 * creates an object element
	 * 
	 * @param name        name of the element
	 * @param displayname display name of the element (not used for property, see
	 *                    #13)
	 * @param tooltip     roll-over tooltip (not used for property, see #13)
	 */
	public ObjectElement(String name, String displayname, String tooltip) {
		super(name);
		this.displayname = displayname;
		this.tooltip = tooltip;
		elements = new NamedList<Element>();
		searchwidgetlist = new ArrayList<SearchWidgetDefinition>();
		indexes = new NamedList<Index>();
		this.loaderelements = new NamedList<LoaderElement>();

	}

	/**
	 * adds a loader element to the object element
	 * 
	 * @param loaderelement loader element
	 */
	public void addLoaderElement(LoaderElement loaderelement) {
		this.loaderelements.add(loaderelement);
	}

	/**
	 * gets all the loader elements for the object element
	 * 
	 * @return
	 */
	public LoaderElement[] getLoaderElements() {
		return loaderelements.getFullList().toArray(new LoaderElement[0]);
	}

	/**
	 * get all the data elements of the object element
	 * 
	 * @return an array with the elements
	 */
	public Element[] getElements() {
		return elements.getFullList().toArray(new Element[0]);
	}

	/**
	 * get all the binary search indexes for the object element
	 * 
	 * @return an array of all the indexes
	 */
	public Index[] getIndex() {
		return indexes.getFullList().toArray(new Index[0]);
	}

	/**
	 * @return the unique name of the field
	 */
	public abstract String getDataObjectFieldName();

	/**
	 * @return the attributes for the constructor to be used during code generation
	 */
	public abstract String getDataObjectConstructorAttributes();

	/**
	 * @return the java type of the object element in data object code
	 */
	public abstract String getJavaType();

	/**
	 * only send here classes that are NOT data objects. For data object
	 * dependencies for properties, use the property method
	 * getExternalObjectDependence
	 * 
	 * TODO: needs to replace this logic by the sending of list of strings to remove
	 * duplicates issue #12
	 * 
	 * @param sg
	 * @param module
	 * @throws IOException
	 */
	public abstract void writeDependentClass(SourceGenerator sg, Module module) throws IOException;

}
