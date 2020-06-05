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
import org.openlowcode.design.data.ExternalElement;
import org.openlowcode.design.data.MethodAdditionalProcessing;
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
 * This property should be added to an object that will be used as a multiple
 * link between two objects.
 * 
 * * <br>
 * Dependent property :
 * {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> left object for link (must have property
 *        {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 *        )
 * @param <F> right object for link (must have property
 *        {@link org.openlowcode.design.data.properties.basic.UniqueIdentified}
 *        )
 */
public class LinkObject<E extends DataObjectDefinition, F extends DataObjectDefinition>
		extends
		Property<LinkObject<E, F>> {
	private E leftobjectforlink;
	private F rightobjectforlink;
	private UniqueIdentified uniqueidentified;

	private boolean hasfieldshown = false;
	private int priorityforleft;
	private int priorityforright;
	private boolean setidsmodifiable = false;

	/**
	 * @param priorityforleft
	 * @param priorityforright
	 */
	public void setFieldsShown(int priorityforleft, int priorityforright) {
		this.hasfieldshown = true;
		this.priorityforleft = priorityforleft;
		this.priorityforright = priorityforright;
	}

	/**
	 * @return
	 */
	public UniqueIdentified getUniqueIdentified() {
		return uniqueidentified;
	}

	private String labelfromleft;
	private String labelfromright;
	private WidgetDisplayPriority displaypriorityfromleft;
	private WidgetDisplayPriority displaypriorityfromright;

	/**
	 * makes the ids of left and right object modifiable from client client. This
	 * can be a security risk, with the possibility for data to be tampered if
	 * someone uses a non-standard client
	 */
	public void setIdsmodifiable() {
		this.setidsmodifiable = true;
	}

	/**
	 * @return
	 */
	public String getLabelFromLeft() {
		return this.labelfromleft;
	}

	/**
	 * @return
	 */
	public String getLabelFromRight() {
		return this.labelfromright;
	}

	@Override
	public void addBusinessRule(PropertyBusinessRule<LinkObject<E, F>> rule) {
		super.addBusinessRule(rule);
	}

	/**
	 * creates a link object with default parameter
	 * 
	 * @param leftobjectforlink  the left object of the link
	 * @param rightobjectforlink the right object of the link
	 */
	public LinkObject(E leftobjectforlink, F rightobjectforlink) {
		this(leftobjectforlink, rightobjectforlink, null, null);
	}

	/**
	 * creates a link object with specified label from left object, and label from
	 * right object for the link (e.g. for a wedding link between a man and a woman,
	 * you could display 'husband' and 'wife' as specific label)
	 * 
	 * @param leftobjectforlink  the left object of the link
	 * @param rightobjectforlink the right object of the link
	 * @param labelfromleft      label of the link table seen from left object
	 * @param labelfromright     label of the link table seen from right object
	 */
	public LinkObject(E leftobjectforlink, F rightobjectforlink, String labelfromleft, String labelfromright) {
		super("LINKOBJECT");
		this.labelfromleft = labelfromleft;
		this.labelfromright = labelfromright;
		this.leftobjectforlink = leftobjectforlink;
		this.rightobjectforlink = rightobjectforlink;

	}

	/**
	 * creates a link object with specified label from left object, and label from
	 * right object for the link (e.g. for a wedding link between a man and a woman,
	 * you could display 'husband' and 'wife' as specific label). Priority for the
	 * widgets on left and right objects is also specified
	 * 
	 * @param leftobjectforlink        the left object of the link
	 * @param rightobjectforlink       the right object of the link
	 * @param labelfromleft            label of the link table seen from left object
	 * @param labelfromright           label of the link table seen from right
	 *                                 object
	 * @param displaypriorityfromleft  priority display from the left object
	 * @param displaypriorityfromright priority display from the right object
	 */
	public LinkObject(
			E leftobjectforlink,
			F rightobjectforlink,
			String labelfromleft,
			String labelfromright,
			WidgetDisplayPriority displaypriorityfromleft,
			WidgetDisplayPriority displaypriorityfromright) {
		this(leftobjectforlink, rightobjectforlink, labelfromleft, labelfromright, displaypriorityfromleft,
				displaypriorityfromright, false);
	}

	/**
	 * creates a link object with specified label from left object, and label from
	 * right object for the link (e.g. for a wedding link between a man and a woman,
	 * you could display 'husband' and 'wife' as specific label). Priority for the
	 * widgets on left and right objects is also specified
	 * 
	 * @param leftobjectforlink        the left object of the link
	 * @param rightobjectforlink       the right object of the link
	 * @param labelfromleft            label of the link table seen from left object
	 * @param labelfromright           label of the link table seen from right
	 *                                 object
	 * @param displaypriorityfromleft  priority display from the left object
	 * @param displaypriorityfromright priority display from the right object
	 * @param setidsmodifiable         put true if ids should be modifiable from
	 *                                 client. This can be a security risk, with the
	 *                                 possibility for data to be tampered if
	 *                                 someone uses a non-standard client
	 */
	public LinkObject(
			E leftobjectforlink,
			F rightobjectforlink,
			String labelfromleft,
			String labelfromright,
			WidgetDisplayPriority displaypriorityfromleft,
			WidgetDisplayPriority displaypriorityfromright,
			boolean setidsmodifiable) {
		this(leftobjectforlink, rightobjectforlink, labelfromleft, labelfromright);
		this.setidsmodifiable = setidsmodifiable;
		this.displaypriorityfromleft = displaypriorityfromleft;
		this.displaypriorityfromright = displaypriorityfromright;
		if (this.displaypriorityfromleft != null)
			this.displaypriorityfromleft.checkIfValidForObject(this.leftobjectforlink);
		if (this.displaypriorityfromright != null)
			this.displaypriorityfromright.checkIfValidForObject(this.rightobjectforlink);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public void controlAfterParentDefinition() {
		if (!leftobjectforlink.getOwnermodule().equals(parent.getOwnermodule()))
			throw new RuntimeException("Link " + parent.getName() + " (module " + parent.getOwnermodule().getName()
					+ ") should be in same module than left object: " + leftobjectforlink.getName() + " (module "
					+ leftobjectforlink.getOwnermodule().getName() + ")");
		if (this.labelfromleft == null)
			this.labelfromleft = parent.getLabel();
		if (this.labelfromright == null)
			this.labelfromright = parent.getLabel();

		this.uniqueidentified = (UniqueIdentified) parent.getPropertyByName("UNIQUEIDENTIFIED");
		if (uniqueidentified == null)
			throw new RuntimeException("linkobject property needs the object to have property uniqueidentified");
		this.addDependentProperty(uniqueidentified);

		this.addPropertyGenerics(new PropertyGenerics("LEFTOBJECTFORLINK", leftobjectforlink, new UniqueIdentified()));
		this.addPropertyGenerics(
				new PropertyGenerics("RIGHTOBJECTFORLINK", rightobjectforlink, new UniqueIdentified()));

		if (parent == null)
			throw new RuntimeException("parent is null for property with name = " + this.getName());

		if (this.leftobjectforlink.getPropertyByName("ITERATED") != null) {
			// adding property in case left object already has iterated property at time
			// link is created
			this.addExternalObjectProperty(parent, new IteratedLink(this));
		}

		if (leftobjectforlink == null)
			throw new RuntimeException("leftobjectforlink is null for property with name = " + this.getName());
		if (rightobjectforlink == null)
			throw new RuntimeException("rightobjectforlink is null for property with name = " + this.getName());

		// add a marker to the left and right object
		this.addExternalObjectProperty(leftobjectforlink,
				new LeftForLink(this.getParent(), rightobjectforlink, this, displaypriorityfromleft));
		this.addExternalObjectProperty(rightobjectforlink,
				new RightForLink(this.getParent(), leftobjectforlink, this, displaypriorityfromright));

		DataAccessMethod getlinksfromleft = new DataAccessMethod("GETALLLINKSFROMLEFTID",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromleft
				.addInputArgument(new MethodArgument("leftid", new ObjectIdArgument("leftid", this.leftobjectforlink)));
		this.addDataAccessMethod(getlinksfromleft);

		DataAccessMethod getlinksfromright = new DataAccessMethod("GETALLLINKSFROMRIGHTID",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromright.addInputArgument(
				new MethodArgument("rightid", new ObjectIdArgument("rightid", this.rightobjectforlink)));
		this.addDataAccessMethod(getlinksfromright);

		DataAccessMethod getlinksfromleftandright = new DataAccessMethod("GETALLLINKSFROMLEFTANDRIGHTID",
				new ArrayArgument(new ObjectArgument("links", parent)), true);
		getlinksfromleftandright.addInputArgument(
				new MethodArgument("left id", new ObjectIdArgument("leftid", this.leftobjectforlink)));
		getlinksfromleftandright.addInputArgument(
				new MethodArgument("rightid", new ObjectIdArgument("rightid", this.rightobjectforlink)));
		this.addDataAccessMethod(getlinksfromleftandright);

		DataAccessMethod getlinkandbothobjects = new DataAccessMethod("GETLINKSANDBOTHOBJECTS",
				new ArrayArgument(new ThreeObjectsArgument("linkandobject",
						new ObjectArgument("leftobject", this.leftobjectforlink), new ObjectArgument("links", parent),
						new ObjectArgument("rightobject", this.rightobjectforlink))),
				true);

		this.addDataAccessMethod(getlinkandbothobjects);

		DataAccessMethod getlinksandrightobject = new DataAccessMethod("GETLINKSANDRIGHTOBJECT",
				new ArrayArgument(new TwoObjectsArgument("LINKANDRIGHT", new ObjectArgument("links", parent),
						new ObjectArgument("rightobject", this.rightobjectforlink))),
				true, true);

		getlinksandrightobject.addInputArgument(
				new MethodArgument("LEFTID", new ObjectIdArgument("LEFTOBJECTID", this.leftobjectforlink)));
		this.addDataAccessMethod(getlinksandrightobject);

		DataAccessMethod getlinksandleftobject = new DataAccessMethod("GETLINKSANDLEFTOBJECT",
				new ArrayArgument(new TwoObjectsArgument("LINKANDLEFT",
						new ObjectArgument("leftobject", this.leftobjectforlink), new ObjectArgument("links", parent))),
				true,true);
		getlinksandleftobject.addInputArgument(
				new MethodArgument("RIGHTID", new ObjectIdArgument("RIGHTOBJECTID", this.rightobjectforlink)));
		this.addDataAccessMethod(getlinksandleftobject);

		DataAccessMethod getleftobjectsfromright = new DataAccessMethod("GETLEFTOBJECTSFROMRIGHT",
				new ArrayArgument(new ObjectArgument("leftobject", this.leftobjectforlink)), true);
		getleftobjectsfromright.addInputArgument(
				new MethodArgument("RIGHTID", new ObjectIdArgument("RIGHTOBJECTID", this.rightobjectforlink)));
		this.addDataAccessMethod(getleftobjectsfromright);

		DataAccessMethod getpotentialrightobjects = new DataAccessMethod("GETPOTENTIALRIGHTOBJECT",
				new ArrayArgument(new ObjectArgument("RIGHTOBJECT", this.rightobjectforlink)), true);
		getpotentialrightobjects.addInputArgument(
				new MethodArgument("leftobjectid", new ObjectIdArgument("leftobject", this.leftobjectforlink)));
		this.addDataAccessMethod(getpotentialrightobjects);

		DataAccessMethod getpotentialleftobjects = new DataAccessMethod("GETPOTENTIALLEFTOBJECT",
				new ArrayArgument(new ObjectArgument("LEFTOBJECT", this.leftobjectforlink)), true);
		getpotentialleftobjects.addInputArgument(
				new MethodArgument("rightobjectid", new ObjectIdArgument("rightobject", this.rightobjectforlink)));
		this.addDataAccessMethod(getpotentialleftobjects);

		DataAccessMethod setleftobject = new DataAccessMethod("SETLEFTOBJECT", null, false);
		setleftobject.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setleftobject.addInputArgument(
				new MethodArgument("leftobject", new ObjectIdArgument("leftobject", this.leftobjectforlink)));
		this.addDataAccessMethod(setleftobject);

		DataAccessMethod setrightobject = new DataAccessMethod("SETRIGHTOBJECT", null, false);
		setrightobject.addInputArgument(new MethodArgument("object", new ObjectArgument("object", this.parent)));
		setrightobject.addInputArgument(
				new MethodArgument("rightobject", new ObjectIdArgument("rightobject", this.rightobjectforlink)));
		this.addDataAccessMethod(setrightobject);

		StoredElement leftid = new ObjectIdStoredElement("LFID", leftobjectforlink);
		StoredElement rightid = new ObjectIdStoredElement("RGID", rightobjectforlink);
		this.addElement(leftid);
		this.addElement(rightid);

		this.addDisplayProfileForProperty(new DisplayProfile("HIDELEFTOBJECTFIELDS"));
		this.addDisplayProfileForProperty(new DisplayProfile("HIDERIGHTOBJECTFIELDS"));

		// ---------------------------------------------------------
		// TREAT NAME

		// TODO never specifies a name, this is incorrect
		if (leftobjectforlink.getPropertyByName("NAMED") != null) {

			Named namedproperty = (Named) leftobjectforlink.getPropertyByName("NAMED");

			ExternalElement leftnameelement = new ExternalElement(this, leftobjectforlink, namedproperty, false,
					(StoredElement) namedproperty.getElements()[0], "LEFT");

			this.addElement(leftnameelement);
		}
		if (rightobjectforlink.getPropertyByName("NAMED") != null) {

			Named namedproperty = (Named) rightobjectforlink.getPropertyByName("NAMED");

			ExternalElement rightnameelement = new ExternalElement(this, rightobjectforlink, namedproperty, false,
					(StoredElement) namedproperty.getElements()[0], "RIGHT");
			this.addElement(rightnameelement);
		}
		// ---------------------------------------------------------
		// TREAT NUMBER

		if (leftobjectforlink.getPropertyByName("NUMBERED") != null) {

			Numbered numberedproperty = (Numbered) leftobjectforlink.getPropertyByName("NUMBERED");

			ExternalElement leftnumberelement = new ExternalElement(this, leftobjectforlink, numberedproperty, false,
					(StoredElement) numberedproperty.getElements()[0], "LEFT");
			this.addElement(leftnumberelement);
		}
		if (rightobjectforlink.getPropertyByName("NUMBERED") != null) {

			Numbered numberedproperty = (Numbered) rightobjectforlink.getPropertyByName("NUMBERED");

			ExternalElement rightnumberelement = new ExternalElement(this, rightobjectforlink, numberedproperty, false,
					(StoredElement) numberedproperty.getElements()[0], "RIGHT");
			this.addElement(rightnumberelement);
		}
		// ----

		MethodAdditionalProcessing deleteotherlinksifrequired = new MethodAdditionalProcessing(false,
				this.uniqueidentified.getStoredObject().getDataAccessMethod("INSERT"));
		this.addMethodAdditionalProcessing(deleteotherlinksifrequired);

		// -- process arguments
		if (this.hasfieldshown) {
			this.setExtraAttributes("," + this.priorityforleft + "," + this.priorityforright+","+this.setidsmodifiable);
		} else {
			this.setExtraAttributes(","+this.setidsmodifiable);
		}
		

	}

	@Override
	public String getJavaType() {
		return "#NOTIMPLEMENTED#";
	}

	@Override
	public void writeDependentClass(SourceGenerator sg, Module module) throws IOException {
		sg.wl("import org.openlowcode.server.data.formula.LinkNavigator;");
		sg.wl("import org.openlowcode.server.data.formula.LinkReverseNavigator;");
		sg.wl("import org.openlowcode.server.data.formula.LinkToLeftReverseNavigator;");

	}

	@Override
	public ArrayList<DataObjectDefinition> getExternalObjectDependence() {
		ArrayList<DataObjectDefinition> dependencies = new ArrayList<DataObjectDefinition>();
		dependencies.add(leftobjectforlink);
		dependencies.add(rightobjectforlink);
		return dependencies;
	}

	/**
	 * @return
	 */
	public E getLeftobjectforlink() {
		return leftobjectforlink;
	}

	/**
	 * @return
	 */
	public F getRightobjectforlink() {
		return rightobjectforlink;
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
	public void setFinalSettings() {
	}

	@Override
	public String[] getPropertyDeepCopyStatement() {

		return null;
	}
}
