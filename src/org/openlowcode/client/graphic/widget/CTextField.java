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

import java.io.IOException;
import java.util.ArrayList;
import java.util.function.UnaryOperator;
import java.util.logging.Logger;
import javafx.beans.property.SimpleObjectProperty;

import org.openlowcode.client.action.CPageAction;
import org.openlowcode.client.action.CPageInlineAction;
import org.openlowcode.client.graphic.CPageData;
import org.openlowcode.client.graphic.CPageDataRef;
import org.openlowcode.client.graphic.CPageNode;
import org.openlowcode.client.graphic.CPageSignifPath;
import org.openlowcode.client.graphic.widget.table.CObjectGridLine;
import org.openlowcode.client.graphic.widget.table.LargeTextTableCell;
import org.openlowcode.client.graphic.widget.table.ObjectTableRow;
import org.openlowcode.client.runtime.PageActionManager;
import org.openlowcode.tools.structure.ArrayDataElt;
import org.openlowcode.tools.structure.ArrayDataEltType;
import org.openlowcode.tools.structure.DataElt;
import org.openlowcode.tools.structure.DataEltType;
import org.openlowcode.tools.structure.EncryptedTextDataElt;
import org.openlowcode.tools.structure.EncryptedTextDataEltType;
import org.openlowcode.tools.structure.ObjectDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;
import org.openlowcode.tools.structure.TextDataElt;
import org.openlowcode.tools.structure.TextDataEltType;
import org.openlowcode.tools.trace.ExceptionLogger;
import org.openlowcode.tools.widgets.MultiSelectionComboBox;
import org.openlowcode.tools.messages.MessageBooleanField;
import org.openlowcode.tools.messages.MessageElement;
import org.openlowcode.tools.messages.MessageIntegerField;
import org.openlowcode.tools.messages.MessageReader;
import org.openlowcode.tools.messages.MessageStartStructure;
import org.openlowcode.tools.messages.OLcRemoteException;
import org.openlowcode.tools.richtext.RichText;
import org.openlowcode.tools.richtext.RichTextArea;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableColumn.CellEditEvent;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextFormatter.Change;
import javafx.scene.control.TextInputControl;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.stage.Window;
import javafx.util.Callback;
import javafx.util.StringConverter;

/**
 * A text business field can be used either as part of an object display (for
 * which it implements the Business Field interface) and as a standalone
 * businessfield.
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class CTextField
		extends
		CBusinessField<TextDataElt> {
	private static final Logger LOGGER = Logger.getLogger(CTextField.class.getName());
	private static final int MAXROWWIDTH = 250;
	private String label;
	private String datafieldname;
	private TextInputControl textfield;
	private RichTextArea richtextarea;
	private String helper;
	private String defaultvalue;
	private String inputvalue;
	@SuppressWarnings("unused")
	private boolean businessparameter = false;
	private boolean iseditable = true;
	private CPageAction action;
	private CPageInlineAction inlineaction;
	@SuppressWarnings("unused")
	private int encryptiontype;
	private int prefereddisplaysizeintable;
	private String[] suggestionsarray;
	private boolean orderasinteger;
	private int integeroffset;

	private boolean hidedisplay = false;
	private CPageDataRef datareference;
	private int maxlength;
	private boolean richtextedit;
	private boolean compactshow;
	private boolean twolines;
	private boolean nosmallfield;
	private boolean hassuggestions;
	private CPageDataRef suggestions;
	private MultiSelectionComboBox<String> multiselectioncombobox;
	private boolean showhelperbefore;

	public boolean isRichText() {
		return richtextedit;
	}

	/**
	 * creates the text field widget from a message from the server
	 * 
	 * @param reader     message reader from the server
	 * @param parentpath parent path
	 * @throws OLcRemoteException if anything bad happens on the server
	 * @throws IOException        if any transmission error appears while reading
	 *                            the message
	 */
	public CTextField(MessageReader reader, CPageSignifPath parentpath) throws OLcRemoteException, IOException {
		super(reader, parentpath);
		label = reader.returnNextStringField("LBL");
		datafieldname = reader.returnNextStringField("DFN");
		helper = reader.returnNextStringField("HPR");
		maxlength = reader.returnNextIntegerField("MXL");
		this.orderasinteger = reader.returnNextBooleanField("OAI");
		if (this.orderasinteger)
			this.integeroffset = reader.returnNextIntegerField("INO");
		defaultvalue = reader.returnNextStringField("DFV");
		this.inputvalue = null;
		businessparameter = reader.returnNextBooleanField("BSP");
		boolean externalreference = reader.returnNextBooleanField("EXR");
		if (externalreference) {
			this.datareference = CPageDataRef.parseCPageDataRef(reader);
			if (!this.datareference.getType().equals(new TextDataEltType()))
				throw new RuntimeException(String.format(
						"Invalid external data reference named %s, excepted TextDataEltType, got %s in CPage ",
						datareference.getName(), datareference));
		}
		hidedisplay = reader.returnNextBooleanField("HID");
		encryptiontype = reader.returnNextIntegerField("ECR");
		this.iseditable = !(reader.returnNextBooleanField("ROY"));
		MessageElement element = reader.getNextElement();

		if (element instanceof MessageStartStructure) { // CASE THERE IS ACTION
			MessageStartStructure actiontag = (MessageStartStructure) element;

			boolean treated = false;

			if (actiontag.getStructurename().compareTo("ACTION") == 0) {
				action = new CPageAction(reader);
				this.showintitle = reader.returnNextBooleanField("SIT");
				treated = true;
			}
			if (actiontag.getStructurename().compareTo("INLINEACTION") == 0) {
				inlineaction = new CPageInlineAction(reader);
				this.showintitle = reader.returnNextBooleanField("SIT");
				treated = true;
			}
			if (!treated)
				throw new RuntimeException(
						" was expecting either ACTION or INLINEACTION structure, got " + actiontag.getStructurename());

		} else { // CASE THERE IS NO ACTION
			if (element instanceof MessageBooleanField) {
				MessageBooleanField booleantag = (MessageBooleanField) element;
				if (((MessageBooleanField) element).getFieldName().compareTo("SIT") == 0) {
					this.showintitle = booleantag.getFieldContent();
				} else {
					throw new RuntimeException("expected a boolean 'SIT' tag, got " + element.toString() + " at path "
							+ reader.getCurrentElementPath());
				}

			} else {
				throw new RuntimeException("expected a boolean 'SIT' tag, got " + element.toString() + " at path "
						+ reader.getCurrentElementPath());
			}
		}
		this.prefereddisplaysizeintable = -1;
		MessageElement nextelement = reader.getNextElement();
		if (nextelement instanceof MessageIntegerField) {
			MessageIntegerField prefereddisplaytable = (MessageIntegerField) nextelement;
			if (prefereddisplaytable.getFieldName().compareTo("PDT") == 0) {
				this.prefereddisplaysizeintable = prefereddisplaytable.getFieldContent();
			} else {
				throw new RuntimeException("expected an integer 'SIT' tag, got " + nextelement.toString() + " at path "
						+ reader.getCurrentElementPath());
			}
			nextelement = reader.getNextElement();
		}
		if (nextelement instanceof MessageBooleanField) {
			MessageBooleanField booleantag = (MessageBooleanField) nextelement;
			if (booleantag.getFieldName().compareTo("SBN") == 0) {
				this.showinbottomnotes = booleantag.getFieldContent();
			} else {
				throw new RuntimeException("expected a boolean 'SBN' tag, got " + nextelement.toString() + " at path "
						+ reader.getCurrentElementPath());
			}
		}
		this.richtextedit = reader.returnNextBooleanField("RCH");
		this.compactshow = reader.returnNextBooleanField("CPS");
		this.twolines = reader.returnNextBooleanField("TWL");
		this.nosmallfield = reader.returnNextBooleanField("NSF");
		this.hassuggestions = reader.returnNextBooleanField("HSG");
		if (hassuggestions) {
			this.suggestions = CPageDataRef.parseCPageDataRef(reader);
			if (!this.suggestions.getType().equals(new ArrayDataEltType<TextDataEltType>(new TextDataEltType())))
				throw new RuntimeException(String.format(
						"Invalid suggestion reference named %s, expected ArrayDataEltType<TextDataEltType>, got %s in CPage ",
						suggestions.getName(), suggestions));
		}
		this.showhelperbefore=reader.returnNextBooleanField("SHB");
		reader.returnNextEndStructure("TXF");
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public Node getDisplayContent() {

		return textfield;
	}

	@Override
	public String getHelper() {

		return this.helper;
	}

	/**
	 * get text field payload from the page data
	 * 
	 * @param inputdata page input data
	 * @param dataref   reference to the data element that holds the text
	 * @return the string payload
	 */
	public String getExternalContent(CPageData inputdata, CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		TextDataElt thistextelement = (TextDataElt) thiselement;
		return thistextelement.getPayload();
	}

	/**
	 * get the text field suggestions from the page data
	 * @param inputdata all input data
	 * @param dataref reference to the suggestions (must be an array of fields)
	 * @return an array of string with the content
	 */
	public String[] getSuggestions(CPageData inputdata,CPageDataRef dataref) {
		DataElt thiselement = inputdata.lookupDataElementByName(dataref.getName());
		if (thiselement == null)
			throw new RuntimeException("could not find any page data with name = " + dataref.getName());
		if (!thiselement.getType().equals(dataref.getType()))
			throw new RuntimeException(
					String.format("page data with name = %s does not have expected %s type, actually found %s",
							dataref.getName(), dataref.getType(), thiselement.getType()));
		@SuppressWarnings("unchecked")
		ArrayDataElt<TextDataElt> thistextarray = (ArrayDataElt<TextDataElt>) thiselement;
		String[] suggestions = new String[thistextarray.getObjectNumber()];
		for (int i=0;i<thistextarray.getObjectNumber();i++) suggestions[i] = thistextarray.getObjectAtIndex(i).getPayload();
		return suggestions;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Node getNode(
			PageActionManager actionmanager,
			CPageData inputdata,
			Window parentwindow,
			TabPane[] parenttabpanes,
			CollapsibleNode nodetocollapsewhenactiontriggered) {
		if (this.datareference != null) {
			this.inputvalue = getExternalContent(inputdata, datareference);
		}
		Pane externalpane=null;
		Pane thispane;
		if (this.compactshow) {
			Pane thisboxpane;
			if (this.twolines) {
				thisboxpane = new VBox();
			} else {
				thisboxpane = new HBox();
			}
			thisboxpane.setBackground(new Background(new BackgroundFill(Color.WHITE, null, null)));
			thisboxpane.setPadding(new Insets(0, 0, 0, 0));
			thispane = thisboxpane;
		} else {
			FlowPane thisflowpane = new FlowPane();
			thisflowpane.setRowValignment(VPos.TOP);
			thispane = thisflowpane;
			if (this.showhelperbefore) {
				VBox overpane = new VBox(1);
				externalpane=overpane;
				Label visiblehelper = new Label(this.helper);
				visiblehelper.setFont(Font.font(visiblehelper.getFont().getName(), FontPosture.REGULAR, visiblehelper.getFont().getSize()*0.9));
				overpane.getChildren().add(visiblehelper);
				overpane.getChildren().add(thisflowpane);
			}
		}
		Label thislabel = new Label(label);
		thislabel.setFont(Font.font(thislabel.getFont().getName(), FontPosture.ITALIC, thislabel.getFont().getSize()));
		thislabel.setMinWidth(120);
		thislabel.setWrapText(true);
		thislabel.setMaxWidth(120);

		thispane.getChildren().add(thislabel);
		boolean readonly = false;
		if (!this.isactive)
			readonly = true;
		if (!this.iseditable)
			readonly = true;
		// ******************* MODIFIABLE *******************

		if (!readonly) {
			if (!hidedisplay) {
				if (this.maxlength < 100) {
					if (this.richtextedit)
						throw new RuntimeException(
								"rich text edit only supported for fields with more than 100 characters");
					if (this.hassuggestions) {
						suggestionsarray = this.getSuggestions(inputdata,this.suggestions);
						
						multiselectioncombobox = new MultiSelectionComboBox(false, suggestionsarray,inputvalue);
						thispane.getChildren().add(multiselectioncombobox.getNode());
						
					} else {
						textfield = new TextField();
	
					}
					
					if (this.nosmallfield) if (!this.hassuggestions)
						textfield.setMinWidth(400);
					if (this.nosmallfield) if (this.hassuggestions)
						multiselectioncombobox.setMinimumWidgetWidth(400);
				} else {
					if (!this.richtextedit) {
						richtextarea = new RichTextArea(actionmanager, false, true, 400);

					} else {
						richtextarea = new RichTextArea(actionmanager, true, true, 400);

					}
				}
			} else {
				if (this.richtextedit)
					throw new RuntimeException("rich text edit only supported for fields without hidden display");
				textfield = new PasswordField();
				if (this.nosmallfield)
					textfield.setMinWidth(400);
			}
			// ********************* LIMITER ****************************

			UnaryOperator<Change> rejectChange = c -> {
				if (c.isContentChange()) {
					if (c.getControlNewText().length() > this.maxlength) {
						LOGGER.info("control format, rejected text as longer than " + this.maxlength + " ("
								+ c.getControlNewText().length() + ") : " + c.getControlNewText());
						return null;
					}
				}
				return c;
			};
			
			
			
			if (textfield != null) { // normal text edition
				textfield.setTextFormatter(new TextFormatter(rejectChange));
				if (!this.showhelperbefore) if (helper.length() > 0)
					textfield.setTooltip(new Tooltip(helper));
				// ** Init text field content
				if (this.inputvalue != null) {
					textfield.setText(inputvalue);

				} else {
					if (this.defaultvalue != null)
						textfield.setText(defaultvalue);
				}
				thispane.getChildren().add(this.textfield);
			} 
			if (richtextarea!=null) {
				richtextarea.setMaxTextLength(this.maxlength);
				thispane.getChildren().add(this.richtextarea.getNode());
				String input = "";
				if (inputvalue != null) {
					input = inputvalue;
				} else {
					if (defaultvalue != null)
						input = defaultvalue;
				}
				richtextarea.setTextInput(input);
			}

		}
		// ******************* READ-ONLY *******************
		if (readonly) {
			LOGGER.fine("setting up text for label " + label);
			if (!this.richtextedit) {
				richtextarea = new RichTextArea(actionmanager, false, false, 400);
				richtextarea.setTextInput(inputvalue);

				thispane.getChildren().add(richtextarea.getNode());
			} else {
				richtextarea = new RichTextArea(actionmanager, true, false, 400);
				richtextarea.setTextInput(inputvalue);

				thispane.getChildren().add(richtextarea.getNode());

			}
		}
		if (this.action != null) {
			actionmanager.registerEvent(textfield, action);
			if (textfield instanceof TextField)
				((TextField) textfield).setOnAction(actionmanager);
		}
		if (this.inlineaction != null) {
			inlineaction.setNodeToCollapse(nodetocollapsewhenactiontriggered);
			actionmanager.registerInlineAction(textfield, inlineaction);
			if (textfield instanceof TextField)
				((TextField) textfield).setOnAction(actionmanager);
		}
		if (externalpane!=null) return externalpane;
		return thispane;

	}

	/**
	 * compute the height of the text with the current font
	 * 
	 * @param text  text payload
	 * @param width width of the widget in pixel
	 * @return the height in pixel
	 */
	public static double computeTextHeight(String text, double width) {
		Text textfield = new Text(text);
		textfield.setWrappingWidth(width - 8);
		new Scene(new Group(textfield));
		textfield.applyCss();
		textfield.setStyle("");
		double height = textfield.getLayoutBounds().getHeight();
		return height * 1.07 + 2;
	}

	/**
	 * generates a RichTextArea for the given input value and width
	 * 
	 * @param actionmanager page action manager
	 * @param inputvalue    input text value
	 * @param width         width of the pixel
	 * @return a rich text area
	 */
	public static RichTextArea getReadOnlyTextArea(PageActionManager actionmanager, String inputvalue, int width) {
		return RichTextArea.getReadOnlyTextArea(actionmanager, inputvalue, width);
	}

	@Override
	public DataElt getDataElt(DataEltType type, String eltname, String objectfieldname) {
		
		if (type instanceof ArrayDataEltType) {
			ArrayDataEltType<?> arraytype = (ArrayDataEltType<?>)type;
			if (arraytype.getPayloadType() instanceof TextDataEltType) {
				if (this.hassuggestions) {
					if (objectfieldname.equals("FULL")) {
						
						ArrayDataElt<TextDataElt> fullsuggestions = new ArrayDataElt<TextDataElt>(eltname, new TextDataEltType());
						if (suggestionsarray!=null) for (int i=0;i<suggestionsarray.length;i++) {
							fullsuggestions.addElement(new TextDataElt(eltname, suggestionsarray[i]));
						}
						return fullsuggestions;
						
					}
				}
			}
		}
		
		if (!(type instanceof TextDataEltType))
			throw new RuntimeException(
					String.format("Only TextDataEltType can be extracted from CTextField, but request was %s ", type));
		if (objectfieldname != null)
			throw new RuntimeException("indicated objectfieldname = '" + objectfieldname
					+ "', but the field is not supporting this parameter");

		if (!this.hidedisplay) {
			if (this.textfield != null)
				return new TextDataElt(eltname, this.textfield.getText());
			if (this.richtextarea!=null)
			return new TextDataElt(eltname, this.richtextarea.generateText());
			if (this.multiselectioncombobox!=null) return new TextDataElt(eltname, this.multiselectioncombobox.getTypedValue());
		}
		LOGGER.finer("Creating an encrypted text field : " + this.datafieldname + ";" + this.textfield.getText());
		return new EncryptedTextDataElt(eltname, this.textfield.getText());

	}

	@Override
	public void setContent(ObjectDataElt objectdata) {
		SimpleDataElt thiselement = objectdata.lookupEltByName(this.datafieldname);
		if (thiselement == null)
			throw new RuntimeException(String.format("could not find any page data with name = %s", this.label));
		if (!this.hidedisplay) {
			if (!(thiselement.getType() instanceof TextDataEltType))
				throw new RuntimeException(
						String.format("page data with name = %s does not have expected %s type, actually found %s",
								this.label, TextDataEltType.class, thiselement.getType().toString()));
			TextDataElt thistextelement = (TextDataElt) thiselement;
			this.inputvalue = thistextelement.getPayload();
			this.property = thistextelement.getPropertyname();
		} else {
			//
			if (!(thiselement.getType() instanceof EncryptedTextDataEltType))
				throw new RuntimeException(
						String.format("page data with name = %s does not have expected %s type, actually found %s",
								this.label, TextDataEltType.class, thiselement.getType().toString()));
			EncryptedTextDataElt thistextelement = (EncryptedTextDataElt) thiselement;
			this.inputvalue = thistextelement.getPayload();
			this.property = thistextelement.getPropertyname();
		}
	}

	@Override
	public TextDataElt getFieldDataElt() {

		if (this.textfield != null) {
			TextDataElt answer = new TextDataElt(this.datafieldname, this.textfield.getText());
			if (this.property != null)
				answer.setPropertyname(this.property);
			return answer;
		}
		
		if (this.multiselectioncombobox!=null) {
			TextDataElt answer = new TextDataElt(this.datafieldname,this.multiselectioncombobox.getTypedValue());
			if (this.property != null)
				answer.setPropertyname(this.property);
			return answer;
		}
		
		if (this.richtextarea != null) {
			String text = this.richtextarea.generateText();
			logger.fine("----------- extreme text debugging -------------");
			for (int i = 0; i < text.length(); i++) {
				logger.fine("" + text.charAt(i) + "(" + (0 + text.charAt(i)) + ")");
			}
			logger.fine("----------- extreme text debugging -------------");

			TextDataElt answer = new TextDataElt(this.datafieldname, this.richtextarea.generateText());
			if (this.property != null)
				answer.setPropertyname(this.property);
			return answer;
		}

		LOGGER.finest("------------------- AUDIT CTEXTFIELD -------------------");
		LOGGER.finest("name=" + this.datafieldname);
		LOGGER.finest("label=" + this.label);
		LOGGER.finest("default value=" + this.defaultvalue);
		LOGGER.finest("input value=" + this.inputvalue);

		if (!this.hidedisplay)
			return new TextDataElt(this.datafieldname, this.inputvalue);
		return new EncryptedTextDataElt(this.datafieldname, this.inputvalue);
	}

	@Override
	public String getFieldname() {
		return this.datafieldname;
	}

	@Override
	public TreeTableColumn<ObjectDataElt, ?> getTreeTableColumn(
			PageActionManager pageactionmanager,
			String actionkeyforupdate) {
		TreeTableColumn<ObjectDataElt, String> thiscolumn = new TreeTableColumn<ObjectDataElt, String>(this.getLabel());

		if (actionkeyforupdate != null)
			thiscolumn.setEditable(true);
		int length = 20 + this.maxlength * 6;

		if (length > 150)
			length = 150;
		if (this.prefereddisplaysizeintable >= 0) {
			logger.severe(
					"dirty log: prefereddisplayintable " + this.prefereddisplaysizeintable + "," + this.getLabel());
			length = this.prefereddisplaysizeintable * 5;
		}

		if (length > MAXROWWIDTH) {
			length = MAXROWWIDTH;
			LOGGER.finer("for column " + this.getFieldname() + ", reduced max row width to " + length);

		}

		thiscolumn.setMinWidth(length);
		thiscolumn.setPrefWidth(length);
		CTextField thistextfield = this;
		thiscolumn.setCellValueFactory(
				new Callback<TreeTableColumn.CellDataFeatures<ObjectDataElt, String>, ObservableValue<String>>() {

					@Override
					public ObservableValue<String> call(TreeTableColumn.CellDataFeatures<ObjectDataElt, String> p) {
						try {
							ObjectDataElt line = p.getValue().getValue();
							String fieldname = thistextfield.getFieldname();
							if (line == null)
								return new SimpleStringProperty("");
							SimpleDataElt lineelement = line.lookupEltByName(fieldname);
							if (lineelement == null)
								return new SimpleStringProperty("Field Not found !" + fieldname);
							if (!richtextedit)
								return new SimpleStringProperty(lineelement.defaultTextRepresentation());
							String text = lineelement.defaultTextRepresentation();
							RichText richtext = new RichText(text);
							return new SimpleStringProperty(richtext.generatePlainString());
						} catch (Exception e) {
							logger.warning("Exception while building observable value " + e.getMessage());
							for (int i = 0; i < e.getStackTrace().length; i++)
								logger.warning("    " + e.getStackTrace()[i]);
							pageactionmanager.getClientSession().getActiveClientDisplay()
									.updateStatusBar("Error in building cell value " + e.getMessage(), true);
							return new SimpleStringProperty("ERROR");
						}
					}

				});

		return thiscolumn;
	}

	/**
	 * A utility class ordering a string as integer (potentially with an offset.
	 * E.g. It would order T-1, T-2, T-11 as 1, 2, and 11)
	 * 
	 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
	 *         SAS</a>
	 *
	 */
	public static class OrderableString
			implements
			Comparable<OrderableString> {
		private String value;
		private boolean orderasinteger;
		private int integerstartsat;
		private boolean frozen;

		/**
		 * creates an orderable string
		 * 
		 * @param value           value
		 * @param orderasinteger  order as integer
		 * @param integerstartsat number of characters of the offset
		 * @param frozen          true if frozen
		 */
		public OrderableString(String value, boolean orderasinteger, int integerstartsat, boolean frozen) {
			this.value = value;
			this.orderasinteger = orderasinteger;
			if (this.orderasinteger)
				this.integerstartsat = integerstartsat;
			this.frozen = frozen;
		}

		/**
		 * * creates an orderable string ordered as simple string
		 * 
		 * @param value  value
		 * @param frozen true if frozen
		 */
		public OrderableString(String value, boolean frozen) {
			this.value = value;
			this.orderasinteger = false;
			this.frozen = frozen;
		}

		/**
		 * create an orderable string
		 * 
		 * @param rawstring
		 * @param integerstartsat number of characters of the offset
		 * @param frozen          true if frozen
		 */
		public OrderableString(String rawstring, int integerstartsat, boolean frozen) {
			this.orderasinteger = true;
			this.integerstartsat = integerstartsat;
			this.frozen = frozen;
		}

		/**
		 * @return true if field is frozen
		 */
		public boolean isFrozen() {
			return this.frozen;
		}

		/**
		 * @param value sets the payload
		 */
		public void setValue(String value) {
			this.value = value;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof OrderableString))
				return false;
			OrderableString otherorderablestring = (OrderableString) obj;
			if (otherorderablestring.value == null) {
				if (this.value == null)
					return true;
				if (this.value != null)
					return false;
			}
			// otherlockable has value
			if (this.value == null)
				return false;
			if (this.value.compareTo(otherorderablestring.value) != 0)
				return false;
			if (this.orderasinteger != otherorderablestring.orderasinteger)
				return false;
			if (this.orderasinteger)
				if (this.integerstartsat != otherorderablestring.integerstartsat)
					return false;
			return true;
		}

		@Override
		public int compareTo(OrderableString object) {
			if (object instanceof OrderableString) {
				OrderableString otherstring = (OrderableString) object;
				if (value == null) {
					if (otherstring.value == null)
						return 0;
					return -1;
				}
				if (value != null) {
					if (otherstring.value == null)
						return 1;
					// ---------------- real comparison

					if (this.orderasinteger)
						if (!otherstring.orderasinteger)
							return 1;
					if (!this.orderasinteger)
						if (otherstring.orderasinteger)
							return -1;

					if (!this.orderasinteger) {
						// logger.severe(" --- "+value+"-"+otherstring.value+" :
						// "+value.compareTo(otherstring.value));
						return this.value.compareTo(otherstring.value);
					}
					if (this.orderasinteger) {
						String thissubstring = this.value.substring(this.integerstartsat);
						String othersubstring = otherstring.value.substring(otherstring.integerstartsat);

						int thisinteger = 0;
						int otherinteger = 0;
						try {
							thisinteger = new Integer(thissubstring).intValue();
						} catch (NumberFormatException e) {
							logger.severe("Error in parsing String in integer, value = " + this.value + " offset="
									+ this.integerstartsat);
						}
						try {

							otherinteger = new Integer(othersubstring).intValue();
						} catch (NumberFormatException e) {
							logger.severe("Error in parsing String in integer, value = " + otherstring.value
									+ " offset=" + otherstring.integerstartsat);

						}

						if (thisinteger > otherinteger) {
							return 1;
						}
						if (thisinteger < otherinteger) {
							return -1;
						}
						if (thisinteger == otherinteger)
							return 0;
					}

					// ----------------- realcomparison
				}
			}
			return 1;
		}

		/**
		 * @return get the value
		 */
		public String getValue() {
			return this.value;
		}

	}

	@Override
	public TableColumn<ObjectTableRow, OrderableString> getTableColumn(
			PageActionManager pageactionmanager,
			boolean largedisplay,
			int rowheight,
			String actionkeyforupdate) {
		TableColumn<
				ObjectTableRow,
				OrderableString> thiscolumn = new TableColumn<ObjectTableRow, OrderableString>(this.getLabel());
		if ((actionkeyforupdate != null) && (this.isEditable()))  {
			thiscolumn.setEditable(true);
			CTextField thistextfield = this;
			thiscolumn.setOnEditCommit(new EventHandler<CellEditEvent<ObjectTableRow, OrderableString>>() {

				@Override
				public void handle(CellEditEvent<ObjectTableRow, OrderableString> event) {
					try {
						boolean treated = false;
						if (event.getOldValue() == null)
							if (event.getNewValue() != null) {
								ObjectTableRow objecttochange = event.getRowValue();
								objecttochange.updateField(thistextfield.getFieldname(), event.getNewValue());
								logger.info("Updated String value for string " + thistextfield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;
							}

						if (!treated)
							if (event.getOldValue() != null)
								if (event.getNewValue() == null) {
									ObjectTableRow objecttochange = event.getRowValue();
									objecttochange.updateField(thistextfield.getFieldname(), event.getNewValue());
									logger.info("Updated String value for string " + thistextfield.getFieldname()
											+ ", new value = " + event.getNewValue());
									treated = true;
								}

						if (!treated)
							if (event.getOldValue().compareTo(event.getNewValue()) != 0) {
								ObjectTableRow objecttochange = event.getRowValue();
								objecttochange.updateField(thistextfield.getFieldname(), event.getNewValue());
								logger.info("Updated String value for string " + thistextfield.getFieldname()
										+ ", new value = " + event.getNewValue());
								treated = true;

							}
						if (!treated) {
							logger.fine("received edit event where old values and new values are the same");
						}
					} catch (RuntimeException e) {
						logger.severe("exception in updating CTextField " + thistextfield.getFieldname() + " : "
								+ e.getClass() + ": " + e.getMessage());
						ExceptionLogger.setInLogs(e, logger);
					}

				}

			});
		} else {
			thiscolumn.setEditable(false);
		}
		LOGGER.finer(
				" ------------------ Start processing " + this.getFieldname() + ", rowheight " + rowheight + " ----- ");
		int length = 20 + this.maxlength * 6;

		LOGGER.finer("for column " + this.getFieldname() + ", column default size = " + length);

		if (rowheight > 1) {
			length = 50 + (length - 50) / rowheight;
			LOGGER.finer(
					"for column " + this.getFieldname() + ", column default size changed due to row height " + length);

		}
		if (this.prefereddisplaysizeintable >= 0) {
			length = this.prefereddisplaysizeintable * 5;
			LOGGER.finer("for column " + this.getFieldname() + ", override length to " + length);

			if (rowheight > 1) {
				length = 50 + (length - 50) / rowheight;
				LOGGER.finer(
						"for column " + this.getFieldname() + ", overriden length change due to row height " + length);

			}
		}

		if (length > MAXROWWIDTH) {
			length = MAXROWWIDTH;
			LOGGER.finer("for column " + this.getFieldname() + ", reduced max row width to " + length);

		}

		int titlelength = this.getLabel().length() * 8;
		if (titlelength > length) {
			length = titlelength;
			LOGGER.finer("for column " + this.getFieldname() + ", extended max row width due to title to " + length);
		}

		if (rowheight > 1) {
			thiscolumn.setMinWidth(length);
			thiscolumn.setPrefWidth(length);
		} else {
			thiscolumn.setMinWidth(length);
			// do nothing as everything is supposed to be sized automatically
		}
		CTextField thistextfield = this;
		thiscolumn.setCellValueFactory(
				new Callback<CellDataFeatures<ObjectTableRow, OrderableString>, ObservableValue<OrderableString>>() {

					@Override
					public ObservableValue<OrderableString> call(CellDataFeatures<ObjectTableRow, OrderableString> p) {
						try {
							ObjectTableRow line = p.getValue();
							String fieldname = thistextfield.getFieldname();
							String lineelement = line.getFieldRepresentation(fieldname);

							if (!richtextedit) {

								return new SimpleObjectProperty<OrderableString>(new OrderableString(lineelement,
										orderasinteger, integeroffset, line.isRowFrozen()));
							}

							RichText richtext = new RichText(lineelement);
							return new SimpleObjectProperty<OrderableString>(new OrderableString(
									richtext.generatePlainString(), orderasinteger, integeroffset, line.isRowFrozen()));
						} catch (Exception e) {
							logger.warning("Exception while building observable value " + e.getMessage());
							for (int i = 0; i < e.getStackTrace().length; i++)
								logger.warning("    " + e.getStackTrace()[i]);
							pageactionmanager.getClientSession().getActiveClientDisplay()
									.updateStatusBar("Error in getting String value " + e.getMessage(), true);
							return new SimpleObjectProperty<OrderableString>(
									new OrderableString("ERROR", orderasinteger, integeroffset, true));
						}
					}

				});

		thiscolumn.setCellFactory(column -> {
			return new LargeTextTableCell<ObjectTableRow, OrderableString>(new StringConverter<OrderableString>() {

				@Override
				public OrderableString fromString(String arg0) {
					// normally, only called when field is already updatable
					return new OrderableString(arg0, orderasinteger, integeroffset, false);
				}

				@Override
				public String toString(OrderableString arg0) {
					return arg0.value;
				}

			}, largedisplay, rowheight) {

				@Override
				public void updateItem(OrderableString string, boolean empty) {
					super.updateItem(string, empty);

					super.setWrapText(true);
					super.setTextOverrun(OverrunStyle.ELLIPSIS);
					super.setEllipsisString("...");
					super.setMaxHeight(1 * 15 + 14);
					super.setPrefHeight(15 + 14);
					super.setMinHeight(15 + 14);

					if (string == null || empty) {
						setText(null);

					} else {
						setText(string.value);
						// if (helper!=null)
						// this.setTooltip(new Tooltip(helper));
					}
					if (string != null)
						if (string.isFrozen()) {
							this.setEditable(false);
						} else {
							this.setEditable(true);
						}
				}
			};
		});
		return thiscolumn;
	}

	@Override
	public boolean isEditable() {

		return iseditable;
	}

	@Override
	public void ForceAction(PageActionManager actionmanager, CPageAction action) {
		this.action = action;

	}

	@Override
	public void forceUpdateData(DataElt dataelt) {
		throw new RuntimeException("Inline data force update not supported by the widget");

	}

	@Override
	public CPageNode deepcopyWithCallback(org.openlowcode.client.graphic.Callback callback) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public int getPreferredTableRowHeight() {
		if (this.maxlength > MAXROWWIDTH)
			return 2;
		return 1;
	}

	@Override
	public void mothball() {
	}

	@Override
	public boolean isRestrictionValid(String restriction) {
		return false;
	}

	@Override
	public String getValueForConstraint() {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public boolean setConstraint(ArrayList<String> restrainedvalues, String selected) {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public void liftConstraint() {
		throw new RuntimeException("Not yet implemented");

	}

	@Override
	public void pingValue() {
		throw new RuntimeException("Not yet implemented");

	}

	
	@Override
	public TableColumn<CObjectGridLine<String>, ?> getTableColumnForGrid(
			PageActionManager pageactionmanager,
			int preferedrowheight,
			String actionkeyforupdate,
			String maincolumnvalue,
			String secondarycolumnvalue,
			boolean maincolumnvaluetitle) {
		throw new RuntimeException("Not yet implemented");
	}

	@Override
	public void overridesLabel(String newlabel) {
		this.label = newlabel;

	}

	/**
	 * 
	 * @param suggestions suggestions to add
	 * @since 1.6
	 */
	public void addSuggestions(CPageDataRef suggestions) {
		this.hassuggestions=true;
		this.suggestions = suggestions;
		if (!this.suggestions.getType().equals(new ArrayDataEltType<TextDataEltType>(new TextDataEltType())))
			throw new RuntimeException(String.format(
					"Invalid suggestion reference named %s, expected ArrayDataEltType<TextDataEltType>, got %s in CPage ",
					suggestions.getName(), suggestions));
		
		
	}
}
