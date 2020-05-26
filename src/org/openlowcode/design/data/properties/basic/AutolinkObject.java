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
import org.openlowcode.design.data.DisplayProfile;
import org.openlowcode.design.data.MethodArgument;
import org.openlowcode.design.data.ObjectIdStoredElement;
import org.openlowcode.design.data.Property;
import org.openlowcode.design.data.PropertyBusinessRule;
import org.openlowcode.design.data.PropertyGenerics;
import org.openlowcode.design.data.StoredElement;
import org.openlowcode.design.data.argument.ArrayArgument;
import org.openlowcode.design.data.argument.ObjectArgument;
import org.openlowcode.design.data.argument.ObjectIdArgument;
import org.openlowcode.design.data.argument.ThreeObjectsArgument;
import org.openlowcode.design.data.argument.TwoObjectsArgument;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.module.Module;

/**
 * This property creates a symetric link between objects of the same type. A
 * symetric link is a link that does not precise left or right object.
 * 
 * objects that are subject of an autolink get the associated property HasAuto
 * {@link org.openlowcode.design.data.properties.basic.HasAutolink}
 * 
 * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> the object owning the property
 */
public class AutolinkObject<E extends DataObjectDefinition>
		extends
		Property<AutolinkObject<E>> {
	private E objectforlink;
	private UniqueIdentified uniqueidentified;
	private String labelfromleft;
	private String labelfromright;
	private WidgetDisplayPriority priorityfromleft;
	private WidgetDisplayPriority priorityfromright;
	private boolean showlinktree = true;

	/**
	 * @return label for the widget showing auto-links from left
	 */
	public String getLabelFromLeft() {
		return this.labelfromleft;
	}

	/**
	 * @return label for the widget showing auto-links from right
	 */
	public String getLabelFromRight() {
		return this.labelfromright;
	}

	/**
	 * @return the unique identified property from the link object
	 */
	public UniqueIdentified getUniqueIdentified() {
		return this.uniqueidentified;
	}

	/**
	 * create an auto-link
	 * 
	 * @param objectforlink               object to link
	 * @param uniquelabelfromleftandright unique label shown both on the left and
	 *                                    right widget (or the single widget if
	 *                                    symetric)
	 */
	public AutolinkObject(E objectforlink, String uniquelabelfromleftandright) {
		this(objectforlink, uniquelabelfromleftandright, uniquelabelfromleftandright);
	}

	/**
	 * creates an auto-link property with specific label from left or right
	 * 
	 * @param objectforlink  object to link
	 * @param labelfromleft  label when seeing the link from left
	 * @param labelfromright label when seeing the link from right
	 */
	public AutolinkObject(E objectforlink, String labelfromleft, String labelfromright) {

		super("AUTOLINKOBJECT");
		this.labelfromleft = labelfromleft;
		this.labelfromright = labelfromright;
		this.objectforlink = objectforlink;
	}

	/**
	 * creates an auto-link property with specific label from left or right
	 * specifying tree display
	 * 
	 * @param objectforlink  object to link
	 * @param labelfromleft  label when seeing the link from left
	 * @param labelfromright label when seeing the link from right
	 * @param showlinktree   true if link should be shown as multi-level tree from
	 *                       left
	 */
	public AutolinkObject(E objectforlink, String labelfromleft, String labelfromright, boolean showlinktree) {

		super("AUTOLINKOBJECT");
		this.labelfromleft = labelfromleft;
		this.labelfromright = labelfromright;
		this.objectforlink = objectforlink;
		this.showlinktree = showlinktree;
	}

	/**
	 * creates an auto-link property with specific label from left or right and
	 * specific priority
	 * 
	 * @param objectforlink     object to link
	 * @param labelfromleft     label when seeing the link from left
	 * @param labelfromright    label when seeing the link from right
	 * @param priorityfromleft  priority for the left widget
	 * @param priorityfromright priority for the right widget
	 */
	public AutolinkObject(
			E objectforlink,
			String labelfromleft,
			String labelfromright,
			WidgetDisplayPriority priorityfromleft,
			WidgetDisplayPriority priorityfromright) {
		this(objectforlink, labelfromleft, labelfromright);
		this.priorityfromleft = priorityfromleft;
		this.priorityfromright = priorityfromright;
	}

	/**
	 * @return true if the widget for the display of link from the left should be a
	 *         tree
	 */
	public boolean isShowLinkTree() {
		return this.showlinktree;
	}

	/**
	 * @param showlinktree sets the display of link from left as a tree
	 */
	public void setShowLinkTree(boolean showlinktree) {
		this.showlinktree = showlinktree;
	}

	@Override
	public String[] getPropertyInitMethod() {
		return new String[0];
	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(objectforlink);
		return dependencies;
	}

	/**
	 * @return the object being linked by the auto-link
	 */
	public E getObjectforlink() {
		return objectforlink;
	}

	@Override
	public void setFinalSettings() {
		// if no symetric link, and linked object is iterated, adds the property
		if (!isSymetricLink())
			if (this.objectforlink.getPropertyByName("ITERATED") != null) {
				parent.addProperty(new IteratedAutolink(this));
			}

	}

	/**
	 * @return true if the link is symetric
	 */
	public boolean isSymetricLink() {
		for (int i = 0; i < this.getBusinessRuleNumber(); i++) {
			PropertyBusinessRule<?> businessrule = this.getBusinessRule(i);
			if (businessrule instanceof SymetricLink)
				return true;
		}
		return false;
	}

	@Override
	public String getJavaType() {
		return "#NOTIMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {

	}

	@Override
	public String[] getPropertyExtractMethod() {
		return new String[0];
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void controlAfterParentDefinition() {

		if (parent == null)
			throw new RuntimeException("parent is null for property with name = " + this.getName());
		if (objectforlink == null)
			throw new RuntimeException("objectforlink is null for property with name = " + this.getName());

		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		this.addDependentProperty(uniqueidentified);
		this.addPropertyGenerics(new PropertyGenerics("OBJECTFORLINK", objectforlink, new HasId()));
		this.addExternalObjectProperty(objectforlink,
				new HasAutolink(this.getParent(), this, priorityfromleft, priorityfromright));

		DataAccessMethod getlinksfromleft = new DataAccessMethod("GETALLLINKSFROMLEFTID",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromleft
				.addInputArgument(new MethodArgument("leftid", new ObjectIdArgument("leftid", this.objectforlink)));

		this.addDataAccessMethod(getlinksfromleft);

		DataAccessMethod getlinksfromright = new DataAccessMethod("GETALLLINKSFROMRIGHTID",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromright
				.addInputArgument(new MethodArgument("rightid", new ObjectIdArgument("rightid", this.objectforlink)));
		this.addDataAccessMethod(getlinksfromright);

		DataAccessMethod getlinksfromleftandright = new DataAccessMethod("GETALLLINKSFROMLEFTANDRIGHTID",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromleftandright
				.addInputArgument(new MethodArgument("left id", new ObjectIdArgument("leftid", this.objectforlink)));
		getlinksfromleftandright
				.addInputArgument(new MethodArgument("rightid", new ObjectIdArgument("rightid", this.objectforlink)));
		this.addDataAccessMethod(getlinksfromleftandright);

		DataAccessMethod getlinkandbothobjects = new DataAccessMethod("GETLINKSANDBOTHOBJECTS",
				new ArrayArgument(new ThreeObjectsArgument("linkandobject",
						new ObjectArgument("leftobject", this.objectforlink), new ObjectArgument("links", parent),
						new ObjectArgument("rightobject", this.objectforlink))),
				true);

		this.addDataAccessMethod(getlinkandbothobjects);

		DataAccessMethod getpotentialrightobjects = new DataAccessMethod("GETPOTENTIALRIGHTOBJECT",
				new ArrayArgument(new ObjectArgument("RIGHTOBJECT", this.objectforlink)), true);
		getpotentialrightobjects.addInputArgument(
				new MethodArgument("leftobjectid", new ObjectIdArgument("leftobject", this.objectforlink)));
		this.addDataAccessMethod(getpotentialrightobjects);

		DataAccessMethod setleftobject = new DataAccessMethod("SETLEFTOBJECT", null, false);
		setleftobject.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setleftobject.addInputArgument(
				new MethodArgument("leftobject", new ObjectIdArgument("leftobject", this.objectforlink)));
		this.addDataAccessMethod(setleftobject);

		DataAccessMethod setrightobject = new DataAccessMethod("SETRIGHTOBJECT", null, false);
		setrightobject.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setrightobject.addInputArgument(
				new MethodArgument("rightobject", new ObjectIdArgument("rightobject", this.objectforlink)));
		this.addDataAccessMethod(setrightobject);

		StoredElement leftid = new ObjectIdStoredElement("LFID", objectforlink);
		StoredElement rightid = new ObjectIdStoredElement("RGID", objectforlink);
		this.addElement(leftid);
		this.addElement(rightid);

		DataAccessMethod exchangeleftandrightfields = new DataAccessMethod("EXCHANGELEFTANDRIGHTFIELDS", null, false);
		exchangeleftandrightfields.addInputArgument(new MethodArgument("OBJECT", new ObjectArgument("OBJECT", parent)));
		this.addDataAccessMethod(exchangeleftandrightfields);

		DataAccessMethod getlinksandrightobject = new DataAccessMethod(
				"GETLINKSANDRIGHTOBJECT", new ArrayArgument(new TwoObjectsArgument("LINKANDRIGHT",
						new ObjectArgument("links", parent), new ObjectArgument("rightobject", this.objectforlink))),
				true);
		getlinksandrightobject.addInputArgument(
				new MethodArgument("LEFTID", new ObjectIdArgument("LEFTOBJECTID", this.objectforlink)));
		this.addDataAccessMethod(getlinksandrightobject);

		DataAccessMethod getlinksandleftobject = new DataAccessMethod("GETLINKSANDLEFTOBJECT",
				new ArrayArgument(new TwoObjectsArgument("LINKANDLEFT",
						new ObjectArgument("leftobject", this.objectforlink), new ObjectArgument("links", parent))),
				true);
		getlinksandleftobject.addInputArgument(
				new MethodArgument("RIGHTID", new ObjectIdArgument("RIGHTOBJECTID", this.objectforlink)));
		this.addDataAccessMethod(getlinksandleftobject);

		this.addDisplayProfileForProperty(new DisplayProfile("HIDELEFTOBJECTFIELDS"));
		this.addDisplayProfileForProperty(new DisplayProfile("HIDERIGHTOBJECTFIELDS"));

	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}

}
