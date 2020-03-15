/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.server.data.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Logger;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.FieldChoiceDefinition;
import org.openlowcode.server.data.NodeTree;
import org.openlowcode.tools.file.StringParser;
import org.openlowcode.tools.messages.SFile;
import org.openlowcode.tools.misc.NamedList;
import org.openlowcode.tools.misc.StringDecoder;
import org.openlowcode.tools.misc.Triple;
import org.openlowcode.tools.structure.DateDataElt;
import org.openlowcode.tools.structure.DecimalDataElt;
import org.openlowcode.tools.structure.SimpleDataElt;

/**
 * an extractor to produce a spreadsheet file from a data object
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> data object this extractor applies to
 */
public class FlatFileExtractor<E extends DataObject<E>> {
	private static Logger logger = Logger.getLogger(FlatFileExtractor.class.getName());
	private DataObjectDefinition<E> definition;
	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	/**
	 * Creates a flat file extractor for the object
	 * 
	 * @param definition the parent object
	 */
	public FlatFileExtractor(DataObjectDefinition<E> definition) {
		this.definition = definition;
	}

	/**
	 * 
	 * @param objectarray
	 * @param specificaliaslist
	 * @return
	 */
	public SFile extractToExcel(E[] objectarray, String[] specificaliaslist) {
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Export Data");
			Sheet referencessheet = workbook.createSheet("Reference Values");
			loadWorkbook(sheet,referencessheet, objectarray, specificaliaslist);
			workbook.setActiveSheet(0); // setting active sheet to export data
			ByteArrayOutputStream documentinmemory = new ByteArrayOutputStream();
			workbook.write(documentinmemory);
			workbook.close();

			SFile returnresult = new SFile("OpenLowcodeExport-" + sdf.format(new Date()) + ".xlsx",
					documentinmemory.toByteArray());
			return returnresult;
		} catch (IOException e) {
			String exceptionstring = "Exception in extracting objects to array " + definition.getName()
					+ ", original IOException " + e.getMessage();
			logger.severe(exceptionstring);
			for (int i = 0; i < e.getStackTrace().length; i++) {
				logger.severe("    " + e.getStackTrace()[i]);
			}
			throw new RuntimeException(exceptionstring);
		}
	}

	/**
	 * Extracts to excel a tree of objects
	 * 
	 * @param objecttree object trees
	 * @return the binary file
	 */
	public SFile extractToExcel(NodeTree<E> objecttree) {
		try {
			Workbook workbook = new XSSFWorkbook();
			Sheet sheet = workbook.createSheet("Export Data");
			loadWorkbook(sheet, objecttree);
			ByteArrayOutputStream documentinmemory = new ByteArrayOutputStream();
			workbook.write(documentinmemory);
			workbook.close();
			SFile returnresult = new SFile("OpenLowcodeExport-" + sdf.format(new Date()) + ".xlsx",
					documentinmemory.toByteArray());
			return returnresult;
		} catch (IOException e) {
			String exceptionstring = "Exception in extracting objects to array " + definition.getName()
					+ ", original IOException " + e.getMessage();
			logger.severe(exceptionstring);
			for (int i = 0; i < e.getStackTrace().length; i++) {
				logger.severe("    " + e.getStackTrace()[i]);
			}
			throw new RuntimeException(exceptionstring);
		}
	}

	/**
	 * loads into a spreadsheet a tree of objects
	 * 
	 * @param sheet      active sheet
	 * @param objecttree object tree
	 */
	private void loadWorkbook(Sheet sheet, NodeTree<E> objecttree) {
		ArrayList<Triple<String, String, StringDecoder>> orderedkeylist = definition
				.getorderedFieldDefinition(objecttree.getRoot());
		CellStyle headerstyle = createBorderedStyle(sheet.getWorkbook());
		Font headerFont = sheet.getWorkbook().createFont();
		headerFont.setBold(true);
		headerFont.setItalic(true);
		headerFont.setFontHeightInPoints((short) (headerFont.getFontHeightInPoints() - (short) 2));
		headerstyle.setAlignment(HorizontalAlignment.CENTER);
		headerstyle.setFont(headerFont);
		Row headerrow = sheet.createRow(1);
		int[] columnmaxchar = new int[orderedkeylist.size()];
		for (int i = 0; i < orderedkeylist.size(); i++) {
			Cell cell = headerrow.createCell(i + 1);
			cell.setCellStyle(headerstyle);
			cell.setCellValue(orderedkeylist.get(i).getSecond());
			columnmaxchar[i] = orderedkeylist.get(i).getSecond().length();
		}
		CellStyle normalstyle = createBorderedStyle(sheet.getWorkbook());
		CellStyle summarystyle = createBorderedStyle(sheet.getWorkbook());
		Font summaryfont = sheet.getWorkbook().createFont();
		summaryfont.setBold(true);
		summarystyle.setFont(summaryfont);
		summarystyle.setFillForegroundColor(HSSFColor.HSSFColorPredefined.GREY_25_PERCENT.getIndex());
		int nblines = loadWorkbook(sheet, 2, objecttree, objecttree.getRoot(), orderedkeylist, normalstyle,
				summarystyle, columnmaxchar, 0);
		// at the end, size columns
		for (int i = 0; i < columnmaxchar.length; i++) {
			int width = (int) (columnmaxchar[i] * 1.14388 * 256);
			if (width > 80 * 256) {
				width = 80 * 256;
				for (int j = 2; j < nblines; j++) {
					Cell cell = sheet.getRow(j).getCell(i);
					if (cell!=null) if (cell.getCellStyle()!=null) cell.getCellStyle().setWrapText(true);

				}

			}

			sheet.setColumnWidth(i + 1, width);
		}
		sheet.setColumnWidth(0, (int) (1.14388 * 256));

	}

	/**
	 * extracts a tree structure into a nodebook
	 * 
	 * @param sheet            spreadsheet sheet
	 * @param line             line to start from
	 * @param objecttree       tree of objects to extract
	 * @param object           head object
	 * @param orderedkeylist   keys list
	 * @param normalstyle      the style to use for groupings
	 * @param summarystyle     the style to use for summary styles
	 * @param columnmaxchar    the size of colums
	 * @param recursivebreaker a recursive breaker to avoid server explosion in case
	 *                         of recursive structure
	 * @return the active line after loading
	 */
	private int loadWorkbook(Sheet sheet, int line, NodeTree<E> objecttree, E object,
			ArrayList<Triple<String, String, StringDecoder>> orderedkeylist, CellStyle normalstyle,
			CellStyle summarystyle, int[] columnmaxchar, int recursivebreaker) {
		if (recursivebreaker > 1024)
			throw new RuntimeException("Recursive breaker");
		boolean isleaf = false;
		if (objecttree.getChildrenNumber(object) == 0)
			isleaf = true;
		int lineafter = line;
		if (!isleaf) {

			for (int i = 0; i < objecttree.getChildrenNumber(object); i++) {
				lineafter = loadWorkbook(sheet, lineafter, objecttree, objecttree.getChild(object, i), orderedkeylist,
						normalstyle, summarystyle, columnmaxchar, recursivebreaker + 1);
			}
		}
		NamedList<SimpleDataElt> allobjectelements = object.getFieldList();
		Row datarow = sheet.createRow(lineafter);
		for (int i = 0; i < orderedkeylist.size(); i++) {
			String key = orderedkeylist.get(i).getFirst();
			SimpleDataElt elementtostore = allobjectelements.lookupOnName(key);
			Cell cell = datarow.createCell(i + 1);
			if (isleaf)
				cell.setCellStyle(normalstyle);
			if (!isleaf)
				cell.setCellStyle(summarystyle);
			boolean specialformatting = false;
			if (elementtostore == null) {
				cell.setCellValue("");
				specialformatting = true;
			}
			if (elementtostore instanceof DecimalDataElt) {
				BigDecimal payload = ((DecimalDataElt) elementtostore).getPayload();
				if (payload != null)
					cell.setCellValue(payload.doubleValue());
				if (columnmaxchar[i] < 10)
					columnmaxchar[i] = 10;
				specialformatting = true;
			}
			if (elementtostore instanceof DateDataElt) {
				cell.setCellValue(((DateDataElt) elementtostore).getPayload());
				if (columnmaxchar[i] < 15)
					columnmaxchar[i] = 15;
				specialformatting = true;
			}
			if (!specialformatting) {
				StringDecoder decoder = orderedkeylist.get(i).getTriple();
				String value = elementtostore.defaultTextRepresentation();
				if (decoder != null)
					value = decoder.decode(elementtostore.defaultTextRepresentation());
				cell.setCellValue(value);
				if (value != null)
					if (columnmaxchar[i] < value.length())
						columnmaxchar[i] = value.length();

			}
		}
		if (objecttree.getRoot() != object)
			sheet.groupRow(line, lineafter);
		lineafter++;
		return lineafter;

	}

	/**
	 * extracts an array of objects to excel
	 * 
	 * @param objectarray the object array
	 * @return the binary file
	 */
	public SFile extractToExcel(E[] objectarray) {
		return extractToExcel(objectarray, null);
	}

	/**
	 * loads into specified sheet the array of objects
	 * 
	 * @param sheet             active sheet
	 * @param referencessheet   sheet to store reference values
	 * @param objectarray       array of objects
	 * @param specificaliaslist the alias to put as column headers (also gives the
	 *                          order of fields)
	 */
	private void loadWorkbook(Sheet sheet,Sheet referencessheet, E[] objectarray, String[] specificaliaslist) {
		String[] aliaslisttoconsider = specificaliaslist;
		// if zero element, put it to null
		if (aliaslisttoconsider != null)
			if (aliaslisttoconsider.length == 0)
				aliaslisttoconsider = null;

		if (aliaslisttoconsider == null)
			if (definition.getAliasNumber() > 0) {
				aliaslisttoconsider = new String[definition.getAliasNumber()];
				for (int i = 0; i < definition.getAliasNumber(); i++)
					aliaslisttoconsider[i] = definition.getAliasat(i);
			}

		if (aliaslisttoconsider == null) {
			Row headerrow = sheet.createRow(0);
			Cell cell = headerrow.createCell(0);
			cell.setCellValue("Export is available only for objects with alias, objet " + definition.getName()
					+ " does not have declared aliases.");
		} else {
			Row headerrow = sheet.createRow(0);
			TransientPropertiesForLoader<E> transientproperties = definition.getTransientPropertiesForLoader();
			ArrayList<FlatFileLoaderColumn<E>> columns = new ArrayList<FlatFileLoaderColumn<E>>();
			FlatFileLoaderColumn<E> complexextractor = null;
			// ---------------------------------
			CellStyle headerstyle = createBorderedStyle(sheet.getWorkbook());
			Font headerFont = sheet.getWorkbook().createFont();
			headerFont.setBold(true);
			headerFont.setItalic(true);
			headerFont.setFontHeightInPoints((short) (headerFont.getFontHeightInPoints() - (short) 2));
			headerstyle.setAlignment(HorizontalAlignment.CENTER);
			headerstyle.setFont(headerFont);
			// ------------------------------------------- write header row
			int[] columnmaxchar = new int[aliaslisttoconsider.length];
			for (int i = 0; i < columnmaxchar.length; i++)
				columnmaxchar[i] = 0;
			ArrayList<String[]> restrictionsperalias = new ArrayList<String[]>();
			int maxrestrictionvalues=0;
			for (int i = 0; i < aliaslisttoconsider.length; i++) {
				Cell cell = headerrow.createCell(i);
				cell.setCellStyle(headerstyle);
				cell.setCellValue(aliaslisttoconsider[i]);
				columnmaxchar[i] = maxNumberCharacter(aliaslisttoconsider[i]);

				String loaderdef = definition.getLoaderAlias(aliaslisttoconsider[i]);
				String[] headlinesplit = StringParser.splitwithdoubleescape(loaderdef, '&');
				FlatFileLoaderColumn<E> column = definition.getFlatFileLoaderColumn(transientproperties, headlinesplit,
						null);
				restrictionsperalias.add(null);
				if (column!=null) {
					String[] valuesrestriction = column.getValueRestriction();
					if (valuesrestriction!=null) {
					restrictionsperalias.set(i,valuesrestriction);
					int valuesnr = valuesrestriction.length;
					if (valuesnr>maxrestrictionvalues) maxrestrictionvalues=valuesnr;
				}
				}
				columns.add(column);
				if (column != null)
					if (column.isComplexExtractor()) {
						if (complexextractor != null)
							throw new RuntimeException("Duplicate complex extractor not supported, " + complexextractor
									+ " and " + column);
						complexextractor = column;
						logger.info("Complex extractor defined as column " + column);
					}
			}
			
			
			// write restrictions in referencesheet
			for (int i=0;i<maxrestrictionvalues;i++) {
				Row currentrow = referencessheet.createRow(i);
				for (int j=0;j<restrictionsperalias.size();j++) {
					String[] restrictionsvaluesperalias = restrictionsperalias.get(j);
					if (restrictionsvaluesperalias!=null) if (i<restrictionsvaluesperalias.length) {
						Cell cell = currentrow.createCell(j);
						cell.setCellValue(restrictionsvaluesperalias[i]);
					}
			}
			}
			// by default, set height for two lines
			headerrow.setHeightInPoints(headerrow.getHeightInPoints() * 2);
			// parse objects
			int rowindex = 1;
			CellStyle normalstyle = createBorderedStyle(sheet.getWorkbook());
			logger.info("parsing objects in array, nr = " + objectarray.length);
			
			for (int i = 0; i < objectarray.length; i++) {
				E currentobject = objectarray[i];
				String[] context = new String[] { null };
				if (complexextractor != null)
					context = complexextractor.initComplexExtractorForObject(currentobject);
				logger.finer("for line " + i + ", context length = " + context.length);
				for (int j = 0; j < context.length; j++) {
					Row datarow = sheet.createRow(rowindex);
					rowindex++;
					for (int k = 0; k < aliaslisttoconsider.length; k++) {
						Cell cell = datarow.createCell(k);

						cell.setCellStyle(normalstyle);
						FlatFileLoaderColumn<E> column = columns.get(k);
						if (column != null) {

							boolean formattingapplied = column.putContentInCell(currentobject, cell, context[j]);
							columnmaxchar[k] = maxNumberCharacter(aliaslisttoconsider[k]);
							int cellsize = getCellNbChar(cell);
							if (cellsize > columnmaxchar[k])
								columnmaxchar[k] = cellsize;
							if (!formattingapplied)
								cell.setCellStyle(normalstyle);
						}
					}
				}

			}
			
			// Put restrictions on cells if exists
			for (int i=0;i<aliaslisttoconsider.length;i++) {
				String[] restrictions = restrictionsperalias.get(i);
				if (restrictions!=null) {
					setRestrictionsOnCell(sheet,referencessheet,i,restrictions.length,rowindex-2);
				}
			}
			
			// at the end, size columns
			for (int i = 0; i < columnmaxchar.length; i++) {
				int width = (int) (columnmaxchar[i] * 1.14388 * 256);
				if (width > 80 * 256) {
					width = 80 * 256;
					for (int j = 2; j < rowindex; j++) {
						Cell cell = sheet.getRow(j).getCell(i);
						cell.getCellStyle().setWrapText(true);

					}

				}
				sheet.setColumnWidth(i, (int) (columnmaxchar[i] * 1.14388 * 256));
			}

		}
	}

	/**
	 * provides the number of characters needed for a cell
	 * 
	 * @param cell the cell to analyze
	 * @return number of characters
	 */
	public static int getCellNbChar(Cell cell) {
		String cellcontentastext = "";
		if (cell.getCellType() == CellType.STRING)
			cellcontentastext = cell.getStringCellValue();
		if (cell.getCellType() == CellType.NUMERIC)
			cellcontentastext = "" + cell.getNumericCellValue();
		return maxNumberCharacter(cellcontentastext);
	}

	/**
	 * provides the maximum number of characters of any line in the text
	 * 
	 * @param multilinestring a multi-line string
	 * @return the maximum number of characters
	 */
	public static int maxNumberCharacter(String multilinestring) {
		String[] lines = multilinestring.split("[\\r\\n]+");

		int maxperline = 0;
		for (int i = 0; i < lines.length; i++) {
			if (maxperline < lines[i].length())
				maxperline = lines[i].length();
		}

		return maxperline;
	}

	/**
	 * a utility class to create style for a border around a cell
	 * 
	 * @param wb the workbook
	 * @return the style to use to put border around a cell
	 */
	public static CellStyle createBorderedStyle(Workbook wb) {
		CellStyle style = wb.createCellStyle();
		style.setBorderRight(BorderStyle.THIN);
		style.setRightBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderBottom(BorderStyle.THIN);
		style.setBottomBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderLeft(BorderStyle.THIN);
		style.setLeftBorderColor(IndexedColors.BLACK.getIndex());
		style.setBorderTop(BorderStyle.THIN);
		style.setTopBorderColor(IndexedColors.BLACK.getIndex());
		return style;
	}

	/**
	 * creates a style to show dates in the provided format
	 * 
	 * @param wb               workbook
	 * @param simpledateformat format of the date
	 * @return the style to use
	 */
	public static CellStyle createDateStyle(Workbook wb, String simpledateformat) {
		CellStyle style = createBorderedStyle(wb);
		CreationHelper createHelper = wb.getCreationHelper();
		style.setDataFormat(createHelper.createDataFormat().getFormat(simpledateformat));
		return style;
	}
	
	/**
	 * create restrictions on the data cells
	 * 
	 * @param mainsheet sheet with data
	 * @param restrictionsheet sheet with restriction values
	 * @param column index of column (starting with zero)
	 * @param nbofchoices number of choices (starting with zero)
	 * @param nbofrows number of rows (starting with zero)
	 */
	public static  void setRestrictionsOnCell(Sheet mainsheet,Sheet restrictionsheet,int column,int nbofchoices,int nbofrows) {
		DataValidationHelper validationHelper = new XSSFDataValidationHelper((XSSFSheet)mainsheet);
		String columnletter =  CellReference.convertNumToColString(column);
		String formula = "'"+restrictionsheet.getSheetName()+ "'!$"+columnletter+"$"+1+":$"+columnletter+"$"+nbofchoices;
		DataValidationConstraint constraint = validationHelper.createFormulaListConstraint(formula);
		CellRangeAddressList addressList = new CellRangeAddressList(1,nbofrows+1,column,column);
		
		DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
		dataValidation.setErrorStyle(DataValidation.ErrorStyle.STOP);
		dataValidation.setSuppressDropDownArrow(true);
		mainsheet.addValidationData(dataValidation);
	}
}
