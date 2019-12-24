/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/

package org.openlowcode.tools.file;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

/**
 * A parser reading a Microsoft Excel format file using Apache POI
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ExcelReader implements FileParser {
	private Workbook workbook;
	private Sheet activesheet;
	private Iterator<Row> rowIterator;

	/**
	 * Opens the specific file, and the active workbook,
	 * 
	 * @param data reader
	 * @throws IOException            if any problem reading the file
	 * @throws InvalidFormatException if any issue is encountered during parsing
	 */
	public ExcelReader(InputStream data) throws IOException, InvalidFormatException {
		workbook = WorkbookFactory.create(data);
		activesheet = workbook.getSheetAt(workbook.getActiveSheetIndex());
		rowIterator = activesheet.rowIterator();
	}

	/**
	 * @return the index of the active worksheet
	 */
	public int getActiveSheetIndex() {
		return workbook.getActiveSheetIndex();
	}

	/**
	 * get the list of sheet names
	 * 
	 * @return the list of sheet names
	 * @throws IOException            if any issue is encountered accessing the file
	 * @throws InvalidFormatException if file is not of the correct format
	 */
	public String[] getSheetNameList() throws IOException, InvalidFormatException {
		ArrayList<String> sheetnames = new ArrayList<String>();
		for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
			sheetnames.add(workbook.getSheetAt(i).getSheetName());
		}
		return sheetnames.toArray(new String[0]);
	}

	/**
	 * put as active sheet the sheet with the given name
	 * 
	 * @param sheetname name of the sheet
	 * @throws IOException            if any issue is encountered accessing the file
	 * @throws InvalidFormatException if file is not of the correct format
	 */
	public void GoToSheet(String sheetname) throws IOException, InvalidFormatException {
		Sheet potentialnewsheet = workbook.getSheet(sheetname);
		if (potentialnewsheet != null) {
			activesheet = potentialnewsheet;
			rowIterator = activesheet.rowIterator();
		}

	}

	/**
	 * Reads the giving cell, and provides the best object possible. Is able to read
	 * the value of fields that are formulas
	 * 
	 * @param cell
	 * @return the following objects
	 *         <ul>
	 *         <li>a string in case the cell is text</li>
	 *         <li>a date in case the cell is a date</li>
	 *         <li>A double in case it is a number</li>
	 *         <li>null in case it is another type</li>
	 *         </ul>
	 */
	public Object cellToObject(Cell cell) {
		switch (cell.getCellType()) {
		case BOOLEAN:
			return null;
		case STRING:
			return cell.getStringCellValue();

		case NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {

				return cell.getDateCellValue();
			} else {
				return cell.getNumericCellValue();
			}

		case FORMULA:
			switch (cell.getCachedFormulaResultType()) {
			case NUMERIC:
				if (DateUtil.isCellDateFormatted(cell)) {

					return cell.getDateCellValue();
				} else {
					return cell.getNumericCellValue();
				}
			case STRING:
				return cell.getRichStringCellValue().getString();
			default:
				return null;
			}
		case BLANK:
			return null;
		default:
			return null;
		}

	}

	@Override
	public Object[] parseOneLine() throws IOException {
		if (rowIterator.hasNext()) {
			Row rowtoread = rowIterator.next();
			ArrayList<Object> rowcontent = new ArrayList<Object> ();
			for (int i = 0; i < rowtoread.getLastCellNum(); i++) {
				Cell cell = rowtoread.getCell(i);
				if (cell == null)
					rowcontent.add(null);
				if (cell != null)
					rowcontent.add(cellToObject(cell));
			}
			return rowcontent.toArray();
		}
		return null;
	}

	@Override
	public void close() throws IOException {
		workbook.close();
	}

	@Override
	protected void finalize() throws Throwable {

		super.finalize();
		workbook.close();
	}

}
