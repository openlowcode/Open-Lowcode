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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;

import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openlowcode.module.system.data.Csvloadererror;
import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;
import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;
import org.openlowcode.server.data.ChoiceValue;
import org.openlowcode.server.data.DataObject;
import org.openlowcode.server.data.DataObjectDefinition;
import org.openlowcode.server.data.DecimalDataObjectFieldFlatFileLoaderColumn;
import org.openlowcode.server.data.DecimalDataObjectFieldFlatFileLoaderColumn.DecimalParser;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn.LinePreparation;
import org.openlowcode.server.data.loader.FlatFileLoaderColumn.LinePreparationExtra;
import org.openlowcode.server.data.properties.UniqueidentifiedInterface;
import org.openlowcode.server.data.properties.CustomloaderDefinition.CustomloaderHelper;
import org.openlowcode.tools.file.CSVParser;
import org.openlowcode.tools.file.ExcelReader;
import org.openlowcode.tools.file.FileParser;
import org.openlowcode.tools.file.StringParser;
import org.openlowcode.tools.messages.SFile;

/**
 * A loader to get data from a flat file (CSV or Microsoft Excel)
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 * @param <E> a DataObject
 */
public class FlatFileLoader<E extends DataObject<E> & UniqueidentifiedInterface<E>> {
	private static Logger logger = Logger.getLogger(FlatFileLoader.class.getName());
	private DataObjectDefinition<E> objectdefinition;
	private ChoiceValue<ApplocaleChoiceDefinition> selectedlocale;
	private FlatFileLoaderSupplement<E> supplement;
	private ChoiceValue<PreferedfileencodingChoiceDefinition> preferedencoding;

	/**
	 * Creates a FlatFileLoader without supplement
	 * 
	 * @param objectdefinition definition of the object
	 * @param selectedlocale   locale for CSV Loading
	 * @param preferedencoding preferred encoding for CSV Loading
	 */
	public FlatFileLoader(
			DataObjectDefinition<E> objectdefinition,
			ChoiceValue<ApplocaleChoiceDefinition> selectedlocale,
			ChoiceValue<PreferedfileencodingChoiceDefinition> preferedencoding) {
		this.objectdefinition = objectdefinition;
		this.selectedlocale = selectedlocale;
		if (selectedlocale == null)
			this.selectedlocale = ApplocaleChoiceDefinition.get().US;
		this.preferedencoding = preferedencoding;
		if (this.preferedencoding == null)
			this.preferedencoding = PreferedfileencodingChoiceDefinition.get().CP1522;
	}

	/**
	 * Creates a FlatFileLoader with a loader supplement
	 * 
	 * @param objectdefinition definition of the object
	 * @param selectedlocale   locale for CSV Loading
	 * @param preferedencoding prefered encoding for CSV Loading
	 * @param supplement       supplement to the loader
	 */
	public FlatFileLoader(
			DataObjectDefinition<E> objectdefinition,
			ChoiceValue<ApplocaleChoiceDefinition> selectedlocale,
			ChoiceValue<PreferedfileencodingChoiceDefinition> preferedencoding,
			FlatFileLoaderSupplement<E> supplement) {
		this(objectdefinition, selectedlocale, preferedencoding);
		this.supplement = supplement;
	}

	/**
	 * This method returns true if the line has some data. A data is defined as a
	 * string of length greater than 0
	 * 
	 * @param parsedcsvline
	 * @return true of false depending on if the line has data.
	 */
	public static boolean hasdata(Object[] parsedcsvline) {
		if (parsedcsvline == null)
			return false;
		for (int i = 0; i < parsedcsvline.length; i++)
			if (parsedcsvline[i] != null) {
				if (parsedcsvline[i] instanceof String) {
					if (((String) (parsedcsvline[i])).length() > 0)
						return true;
					return false;
				}
				return true;
			}
		return false;
	}

	/**
	 * Returns the error message
	 * 
	 * @param t a throwable
	 * @return a String with the clearest possible message
	 */
	public static String buildExceptionMessage(Throwable t) {
		if (t == null)
			return "Null Exception (this is not normal)";
		return "Error: " + t.getClass().getName() + " - " + t.getMessage() + " @ " + t.getStackTrace()[0];
	}

	private FlatFileLoaderReport load(
			String filename,
			FileParser firstparser,
			FileParser secondparser,
			FileParser thirdparser) {
		logger.info(" ------------- starting loader for object '" + objectdefinition.getName() + "' ---- "
				+ (supplement != null ? "supplement of class " + supplement.getClass().getName() + " is set"
						: "no supplement"));

		TransientPropertiesForLoader<E> transientproperties = objectdefinition.getTransientPropertiesForLoader();

		FileParser parser = firstparser;
		ArrayList<Csvloadererror> errors = new ArrayList<Csvloadererror>();
		long startloading = new Date().getTime();
		try {

			Object[] headline = parser.parseOneLine();
			ArrayList<FlatFileLoaderColumn<E>> loadercolumns = new ArrayList<FlatFileLoaderColumn<E>>();
			// custom loader helpers sorted by classname.
			HashMap<
					String,
					CustomloaderHelper<E>> activecustomloaderhelper = new HashMap<String, CustomloaderHelper<E>>();
			// line preparator is the column used to define if insert or update. There can
			// be zero or 1 column like that
			int linepreparatorindex = -1;
			ArrayList<Integer> linepreparatorextraindex = new ArrayList<Integer>();
			if (headline != null) {
				// parses headers
				for (int i = 0; i < headline.length; i++) {

					Object headlineobject = headline[i];
					String headlineelement = FlatFileLoader.parseObject(headlineobject, "Parse Title for column " + i);
					String alias = objectdefinition.getLoaderAlias(headlineelement);
					logger.fine(" -- Found alias = " + alias + " for headline element = " + headlineelement);
					if (alias == null)
						alias = headlineelement;

					// column definition exists
					if ((alias.trim().length() > 0) && (!alias.trim().equals("#DISCARDED#"))) {
						String[] headlinesplit = StringParser.splitwithdoubleescape(alias, '&');
						FlatFileLoaderColumn<E> column = objectdefinition.getFlatFileLoaderColumn(transientproperties,
								headlinesplit, selectedlocale);
						CustomloaderHelper<E> relevanthelper = objectdefinition
								.getCustomLoaderHelper(transientproperties, headlinesplit, selectedlocale);
						//
						if (relevanthelper != null)
							activecustomloaderhelper.put(relevanthelper.getClass().getName(), relevanthelper);
						if (column == null)
							throw new RuntimeException("Did not find any element for column '" + headlineelement + "' "
									+ (alias != null ? "(Alias '" + headline[i] + "')" : ""));
						if (column.isLinePreparator()) {
							if (linepreparatorindex != -1)
								throw new RuntimeException("double line preparator defined : oldindex "
										+ linepreparatorindex + ", new index " + i);
							linepreparatorindex = i;
						}

						loadercolumns.add(column);
						logger.fine("   * succesfully parsed column definition " + column);
						if (column.isLinePreparatorExtra()) {
							linepreparatorextraindex.add(new Integer(i));
						}
					} else {
						// column definition does not exist
						loadercolumns.add(new DiscardedColumn<E>());
					}

				}
				// remove discarded columns at the end
				int cleaningindex = loadercolumns.size() - 1;
				while (cleaningindex >= 0) {
					if (!loadercolumns.get(cleaningindex).isDiscarded())
						break;
					loadercolumns.remove(cleaningindex);
					cleaningindex--;
				}
				// checks if some columns require static pre-processing
				ArrayList<Integer> columnswithstaticprocessing = new ArrayList<Integer>();
				for (int i = 0; i < loadercolumns.size(); i++) {
					if (loadercolumns.get(i).isStaticPreProcessing())
						columnswithstaticprocessing.add(new Integer(i));
				}
				if (!columnswithstaticprocessing.isEmpty()) {
					logger.info(
							"  --- starting static preprocessing for object '" + objectdefinition.getName() + "' - ");
					ArrayList<
							HashMap<
									String,
									String>> uniquevaluesforstaticprocessing = new ArrayList<HashMap<String, String>>();
					for (int i = 0; i < columnswithstaticprocessing.size(); i++)
						uniquevaluesforstaticprocessing.add(new HashMap<String, String>());
					Object[] data = parser.parseOneLine();
					int firstpass = 0;
					while (data != null)
						if (!hasdata(data)) {
							data = parser.parseOneLine();
						} else {
							firstpass++;
							for (int i = 0; i < columnswithstaticprocessing.size(); i++) {
								int columnindex = columnswithstaticprocessing.get(i).intValue();
								if (columnindex < data.length) {
									Object relevantobject = data[columnindex];
									String relevantdata = FlatFileLoader.parseObject(relevantobject,
											"preprocessing column index = " + columnindex);
									if (relevantdata != null) {
										uniquevaluesforstaticprocessing.get(i).put(relevantdata, relevantdata);
									}
								}
							}

							data = parser.parseOneLine();
						}
					// closing parser, and opens a new one
					parser.close();
					parser = secondparser;
					parser.parseOneLine();

					for (int i = 0; i < columnswithstaticprocessing.size(); i++) {
						int columnindex = columnswithstaticprocessing.get(i).intValue();
						FlatFileLoaderColumn<E> thiscolumn = loadercolumns.get(columnindex);
						HashMap<String, String> uniquevalues = uniquevaluesforstaticprocessing.get(i);
						logger.info("   - managing preprocessing for column " + columnindex + ", processing "
								+ uniquevalues.size() + " elements");
						Iterator<String> valuesiterator = uniquevalues.keySet().iterator();
						while (valuesiterator.hasNext()) {
							thiscolumn.staticpreprocessor(valuesiterator.next());
						}
					}
					logger.info("  --- finished static preprocessing for for object '" + objectdefinition.getName()
							+ "' - , read " + firstpass + " lines in file");
				}
				Object[] dataforloading = parser.parseOneLine();
				// ---- loop on data
				int lineindex = 0;
				int error = 0;
				int insert = 0;
				int update = 0;
				int untouched = 0;
				int postprocerror = 0;
				ArrayList<E> objects = new ArrayList<E>(); // saving values for postprocessing
				while (dataforloading != null)
					if (!hasdata(dataforloading)) {
						dataforloading = parser.parseOneLine();
					} else {
						try {

							logger.info(" -- starting processing for loading line " + lineindex + " ------------- ");
							E objectforprocessing = null;
							boolean thislineupdate = false;
							boolean contentupdated = false;
							// adds criteria for selection
							ArrayList<
									FlatFileLoaderColumn.LinePreparationExtra<
											E>> linepreparatorextracriterias = new ArrayList<
													FlatFileLoaderColumn.LinePreparationExtra<E>>();
							for (int i = 0; i < linepreparatorextraindex.size(); i++) {
								FlatFileLoaderColumn<E> thiscolumn = loadercolumns.get(linepreparatorextraindex.get(i));
								linepreparatorextracriterias.add(thiscolumn
										.generateLinePreparatorExtra(dataforloading[linepreparatorextraindex.get(i)]));
							}
							// supplement can add also a criteria for selection
							if (this.supplement != null) {
								LinePreparationExtra<E> linepreparatorextra = this.supplement.getSupplement();
								if (linepreparatorextra != null)
									linepreparatorextracriterias.add(linepreparatorextra);
							}
							// tries to get an object
							if (linepreparatorindex != -1) {
								if (dataforloading.length <= linepreparatorindex)
									throw new RuntimeException(
											"loooking for line preparator index at index = " + linepreparatorindex
													+ " but line only has length = " + dataforloading.length);
								FlatFileLoaderColumn<E> thiscolumn = loadercolumns.get(linepreparatorindex);
								LinePreparation<E> thislineprep = thiscolumn.LinePreparation(
										dataforloading[linepreparatorindex], linepreparatorextracriterias);
								objectforprocessing = thislineprep.getPayload();
								thislineupdate = thislineprep.isUpdate();
								if (!thislineupdate) {
									if (this.supplement != null) {
										this.supplement.initializeNewObject(objectforprocessing);
									}
								}
								logger.info(" -- finished preparing line " + lineindex);
							}

							if (objectforprocessing == null) {
								objectforprocessing = objectdefinition.generateBlank();
								if (this.supplement != null) {
									this.supplement.initializeNewObject(objectforprocessing);
								}
							}
							PostUpdateProcessingStore<E> postupdateprocessingstore = new PostUpdateProcessingStore<E>();
							int columnnr = dataforloading.length;
							if (columnnr > loadercolumns.size())
								columnnr = loadercolumns.size();

							for (int i = 0; i < columnnr; i++) {

								FlatFileLoaderColumn<E> thiscolumn = loadercolumns.get(i);

								if (!thiscolumn.secondpass())
									if (!thiscolumn.processAfterLineInsertion()) {
										logger.info(" -- started processing column " + i + " as preprocessing for "
												+ lineindex + ", name =  " + thiscolumn.toString());

										Object value = dataforloading[i];
										boolean iscolumnupdated = thiscolumn.load(objectforprocessing, value,
												postupdateprocessingstore);
										if (iscolumnupdated) {
											logger.info("line " + lineindex + ", column " + i + " updated " + value);
											contentupdated = true;
										}
										logger.info(" -- finished processing column " + i + " as preprocessing for "
												+ lineindex + ", name =  " + thiscolumn.toString());
									}
							}

							for (int i = 0; i < columnnr; i++) {

								FlatFileLoaderColumn<E> thiscolumn = loadercolumns.get(i);

								if (thiscolumn.secondpass())
									if (!thiscolumn.processAfterLineInsertion()) {
										logger.info(" -- started processing column " + i + " as preprocessing for "
												+ lineindex + ", name =  " + thiscolumn.toString());

										Object value = dataforloading[i];
										boolean iscolumnupdated = thiscolumn.load(objectforprocessing, value,
												postupdateprocessingstore);
										if (iscolumnupdated) {
											logger.info("line " + lineindex + ", column " + i + " updated " + value);
											contentupdated = true;
										}
										logger.info(" -- finished processing column " + i + " as preprocessing for "
												+ lineindex + ", name =  " + thiscolumn.toString());
									}
							}

							// ---------- runs multi-field checks and constraints
							objectforprocessing.getDefinitionFromObject()
									.checkMultiFieldConstraints(objectforprocessing);

							if (thislineupdate)
								if (contentupdated) {
									objectforprocessing.update();
									update++;
									logger.info("    finished processing line " + lineindex + " with update ");

								}

							if (thislineupdate)
								if (!contentupdated) {
									untouched++;

									logger.info("    finished processing line " + lineindex
											+ " ignoring update as no change ");

								}

							if (!thislineupdate) {
								objectforprocessing.insert();
								insert++;
								logger.info("    finished processing line " + lineindex + " with insert ");

							}
							// specific try cast to classify the error as a postproc error.

							for (int i = 0; i < columnnr; i++) {
								try {
									FlatFileLoaderColumn<E> thiscolumn = loadercolumns.get(i);

									if (thiscolumn.processAfterLineInsertion()) {
										logger.info("    finished processing column " + i + " as preprocessing for "
												+ lineindex + ", name =  " + thiscolumn.toString());

										Object value = dataforloading[i];
										thiscolumn.load(objectforprocessing, value, postupdateprocessingstore);
										logger.info("    finished processing column " + i + " as preprocessing for "
												+ lineindex + ", name =  " + thiscolumn.toString());

									}
								} catch (Exception e) {
									postprocerror++;
									Csvloadererror errorreport = new Csvloadererror();
									errorreport.setError(buildExceptionMessage(e));
									errorreport.setLinenr(lineindex + 1);
									errorreport.setLinesource(dropOneLineAsText(dataforloading));
									errors.add(errorreport);
									logger.warning(" ---- Could not perform extra processing " + lineindex
											+ ", error = " + e.getMessage());
									for (int s = 0; s < e.getStackTrace().length; s++) {
										logger.warning("          " + e.getStackTrace()[s]);
									}
								}
							}

							ArrayList<Exception> exceptionsinpostupdateprocessingstore = postupdateprocessingstore
									.process(objectforprocessing);
							for (int i = 0; i < exceptionsinpostupdateprocessingstore.size(); i++) {
								Exception e = exceptionsinpostupdateprocessingstore.get(i);

								postprocerror++;
								Csvloadererror errorreport = new Csvloadererror();
								errorreport.setError(buildExceptionMessage(e));
								errorreport.setLinenr(lineindex + 1);
								errorreport.setLinesource(dropOneLineAsText(dataforloading));
								errors.add(errorreport);
								logger.warning(" ---- Could not perform extra processing " + lineindex + ", error = "
										+ e.getMessage());
								for (int s = 0; s < e.getStackTrace().length; s++) {
									logger.warning("          " + e.getStackTrace()[s]);
								}
							}

							Iterator<CustomloaderHelper<E>> loaderhelperiterator = activecustomloaderhelper.values()
									.iterator();
							while (loaderhelperiterator.hasNext()) {
								try {
									CustomloaderHelper<E> customloaderhelper = loaderhelperiterator.next();
									customloaderhelper.executeAtEndOfLine(objectforprocessing);
								} catch (Exception e) {
									postprocerror++;
									Csvloadererror errorreport = new Csvloadererror();
									errorreport.setError(buildExceptionMessage(e));
									errorreport.setLinenr(lineindex + 1);
									errorreport.setLinesource(dropOneLineAsText(dataforloading));
									errors.add(errorreport);
									logger.warning(" ---- Could not perform extra processing " + lineindex
											+ ", error = " + e.getMessage());
									for (int i = 0; i < e.getStackTrace().length; i++) {
										logger.warning("          " + e.getStackTrace()[i]);
									}
								}
							}

							// keeping that anyways even if error with postprocessing
							objects.add(objectforprocessing);
						} catch (Exception e) {
							error++;
							Csvloadererror errorreport = new Csvloadererror();
							errorreport.setError(buildExceptionMessage(e));
							errorreport.setLinenr(lineindex + 1);
							errorreport.setLinesource(dropOneLineAsText(dataforloading));
							errors.add(errorreport);
							logger.warning(" ---- Could not perform extra processing " + lineindex + ", error = "
									+ e.getMessage());
							for (int i = 0; i < e.getStackTrace().length; i++) {
								logger.warning("          " + e.getStackTrace()[i]);
							}
						}
						dataforloading = parser.parseOneLine();
						lineindex++;
					}

				logger.warning(" --- Loader First & second  passfinished, lines inserted = " + insert
						+ ", line updated = " + update + ", untouched = " + untouched + ", error = " + error);
				// ---- loop on data end
				parser.close();
				parser = thirdparser;
				// discards title
				parser.parseOneLine();
				dataforloading = parser.parseOneLine();
				int index = 0;
				while (dataforloading != null)
					if (!hasdata(dataforloading)) {
						dataforloading = parser.parseOneLine();
					} else {
						try {
							logger.info(
									" -- starting post-processing for loading line " + lineindex + " ------------- ");
							int columnnr = dataforloading.length;
							if (columnnr > loadercolumns.size())
								columnnr = loadercolumns.size();
							for (int i = 0; i < columnnr; i++) {

								FlatFileLoaderColumn<E> thiscolumn = loadercolumns.get(i);
								if (thiscolumn.finalpostprocessing()) {
									Object value = dataforloading[i];
									thiscolumn.postprocessLine((index < objects.size() ? objects.get(index) : null),
											value);
								}
							}

						} catch (Exception e) {
							postprocerror++;
							Csvloadererror errorreport = new Csvloadererror();
							errorreport.setError(buildExceptionMessage(e));
							errorreport.setLinenr(lineindex + 1);
							errorreport.setLinesource(dropOneLineAsText(dataforloading));
							errors.add(errorreport);
							logger.warning(" ---- Could not load line " + lineindex + ", error = " + e.getMessage());
							for (int i = 0; i < e.getStackTrace().length; i++) {
								logger.warning("          " + e.getStackTrace()[i]);
							}
						}
						index++;
						dataforloading = parser.parseOneLine();
						lineindex++;
					}
				long endloading = new Date().getTime();
				return new FlatFileLoaderReport(filename, insert, update, untouched, error, postprocerror,
						endloading - startloading, errors.toArray(new Csvloadererror[0]));

			}
			return new FlatFileLoaderReport(filename, 0, 0, 0, 0, 0, 0, errors.toArray(new Csvloadererror[0]));
		} catch (Exception e) {
			String exceptionstring = "error in flatfile loader " + e.getClass().getName() + " - " + e.getMessage();
			logger.warning(exceptionstring);
			for (int i = 0; i < e.getStackTrace().length; i++)
				logger.warning(e.getStackTrace()[i].toString());
			throw new RuntimeException(exceptionstring);
		} finally {
			try {
				if (parser != null)
					parser.close();
			} catch (Exception e) {
				logger.warning(" exception in cleanup for flat file loader " + e.toString());
				for (int i = 0; i < e.getStackTrace().length; i++)
					logger.warning(e.getStackTrace()[i].toString());

			}
		}

	}

	/**
	 * checks if the file is a supported workbook format
	 * 
	 * @param file binary file
	 * @return true if supported workbook format (all variants of MS Excel today
	 */
	public static boolean isWorkbookFormat(SFile file) {
		String filename = file.getFileName();
		if (filename == null)
			return false;
		if (filename.endsWith(".xlsx"))
			return true;
		if (filename.endsWith(".xls"))
			return true;
		if (filename.endsWith(".XLSX"))
			return true;
		if (filename.endsWith(".XLS"))
			return true;
		return false;
	}

	/**
	 * generates a CSVParser for the file with the correct charset corresponding to
	 * the locale
	 * 
	 * @param file         the file to generate the parser for
	 * @param charsettouse charset to use for the input stream
	 * @return a CSV Parser
	 */
	public CSVParser generateCSVParser(SFile file, Charset charsettouse) {
		BufferedReader br = new BufferedReader(
				new InputStreamReader(new ByteArrayInputStream(file.getContent()), charsettouse));
		CSVParser parser = null;
		if (selectedlocale.getStorageCode().equals(ApplocaleChoiceDefinition.get().US.getStorageCode()))
			parser = new CSVParser(',', '"', br);
		if (selectedlocale.getStorageCode().equals(ApplocaleChoiceDefinition.get().FR.getStorageCode()))
			parser = new CSVParser(';', '"', br);
		return parser;
	}

	/**
	 * generates an excel parser for the file
	 * 
	 * @param file        binary file
	 * @param preferedtab name of the prefered tab to use
	 * @return the Excel Reader
	 */
	public ExcelReader generateExcelParser(SFile file, String preferedtab) {
		ByteArrayInputStream batch = new ByteArrayInputStream(file.getContent());
		try {
			ExcelReader parser = new ExcelReader(batch);
			if (preferedtab != null)
				parser.GoToSheet(preferedtab);
			return parser;
		} catch (InvalidFormatException | IOException e) {
			logger.warning("------------------ Exception in setting up Excel file ---- " + e.getMessage());
			for (int i = 0; i < e.getStackTrace().length; i++) {
				logger.warning("       " + e.getStackTrace()[i]);
			}
			throw new RuntimeException("Error in opening excel file " + file.getFileName() + " Original error "
					+ e.getClass() + " - " + e.getMessage());
		}
	}

	/**
	 * performs the 3 pass loading algorithm
	 * 
	 * @param file the file to use (loaded in memory)
	 * @return a report of the errors encountered
	 */
	public FlatFileLoaderReport load(SFile file) {
		if (!isWorkbookFormat(file)) {
			Charset charsettouse = Charset.forName("UTF-8");
			if (this.preferedencoding.equals(PreferedfileencodingChoiceDefinition.get().CP1522))
				charsettouse = Charset.forName("ISO-8859-1");
			CSVParser parser1 = generateCSVParser(file, charsettouse);
			CSVParser parser2 = generateCSVParser(file, charsettouse);
			CSVParser parser3 = generateCSVParser(file, charsettouse);

			return load(file.getFileName(), parser1, parser2, parser3);
		} else {
			ExcelReader reader1 = generateExcelParser(file, this.objectdefinition.getPreferedSpreadsheetTab());
			ExcelReader reader2 = generateExcelParser(file, this.objectdefinition.getPreferedSpreadsheetTab());
			ExcelReader reader3 = generateExcelParser(file, this.objectdefinition.getPreferedSpreadsheetTab());
			this.objectdefinition.getPreferedSpreadsheetTab();
			return load(file.getFileName(), reader1, reader2, reader3);
		}
	}

	/**
	 * A helper method comparing without explosion two objects, taking into account
	 * the possibility that objects are null
	 * 
	 * @param object1 first object (can be null)
	 * @param object2 second object (can be null)
	 * @return true if same, false if different
	 */
	public static <X extends Object> boolean isTheSame(X object1, X object2) {
		if (object1 == null) {
			if (object2 == null)
				return true;
			return false;
		} else {
			if (object1.equals(object2))
				return true;
			return false;
		}
	}

	/**
	 * parses an object as a string
	 * 
	 * @param object  the object
	 * @param context context for the parsing, used to give context in the potential
	 *                exception
	 * @return a string with the canonical representation of the object
	 */
	public static String parseObject(Object object, String context) {
		String value = "";
		if (object == null)
			return value;
		if (object instanceof String) {
			value = (String) object;
			return value;
		}
		if (object instanceof Date) {
			Date datevalue = (Date) object;
			value = datevalue.toString();
			return value;
		}
		if (object instanceof Double) {
			Double doublevalue = (Double) object;
			BigDecimal bigdecimal = new BigDecimal(doublevalue);
			value = bigdecimal.toString();
			return value;
		}
		throw new RuntimeException("For " + context + ", received an object of unsupported type = " + object.getClass()
				+ " value = " + object);

	}

	/**
	 * 
	 * @param object  object to parse (should be a String, else exception)
	 * @param context context for the exception being potentially thrown
	 * @return the object parsed as a string, or an exception
	 */
	public static String parseStringStrict(Object object, String context) {
		String value = "";
		if (object == null)
			return value;
		if (object instanceof String) {
			value = (String) object;
			return value;
		}
		throw new RuntimeException("For " + context + ", received an object of unsupported type = " + value.getClass()
				+ " value = " + value);

	}

	/**
	 * parses an object as a date
	 * 
	 * @param object   the object to parse
	 * @param context  context for potential exception being thrown
	 * @param timeedit true if time is stored in the date
	 * @param format   format to parse the date if string
	 * @return the date if could be parsed as a valid date, else throws an exception
	 */
	public static Date parseDate(Object object, String context, boolean timeedit, DateTimeFormatter format) {
		if (object == null)
			return null;
		if (object instanceof Date) {
			Date datevalue = (Date) object;
			// no time edit, date is set to noon to avoid issues linked to summer time
			if (!timeedit) {
				Calendar calendar = Calendar.getInstance();
				calendar.setTime(datevalue);
				calendar.set(Calendar.HOUR_OF_DAY, 12);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
				datevalue = calendar.getTime();

			}
			return datevalue;
		}
		if (object instanceof String) {
			String stringvalue = (String) object;
			Date date = null;
			if (stringvalue.length() > 0)
				try {
					date = Date
							.from(LocalDateTime.parse(stringvalue, format).atZone(ZoneId.systemDefault()).toInstant());
				} catch (Exception e) {
					throw new RuntimeException("Exception " + e.getClass() + " : " + e.getMessage() + " for "
							+ stringvalue + " context = " + context);
				}
			logger.info(" date parsing datestring=" + stringvalue + ", dateparsed=" + date + ", format = "
					+ format.toString());
			return date;

		}
		throw new RuntimeException("For " + context + ", received an object of unsupported type = " + object.getClass()
				+ " value = " + object);

	}

	/**
	 * parses an object as Integer
	 * 
	 * @param object  an object (can be string or double)
	 * @param context context for exception throwing
	 * @return the integer parsed if operation could be succesfully achieved, else
	 *         throws an exception
	 */
	public static Integer parseInteger(Object object, String context) {
		// --------------------- Process null ------------------
		if (object == null)
			return null;
		// --------------------- Process String ------------------
		if (object instanceof String) {
			String stringvalue = (String) object;
			Integer integer = new Integer(stringvalue);
			return integer;
		}
		// --------------------- Process double ------------------
		if (object instanceof Double) {
			Double doublevalue = (Double) object;
			Integer integer = doublevalue.intValue();
			return integer;
		}
		// --------------------- Else return error
		throw new RuntimeException("For " + context + ", received an object of unsupported type = " + object.getClass()
				+ " value = " + object);
	}

	/**
	 * parses a big decimal if possible, else throws an exception
	 * 
	 * @param object        the object to parse
	 * @param precision     precision in the sense of BigDecimal
	 * @param scale         scale in the sense of Big Decimal
	 * @param context       context for exception throwing
	 * @param decimalparser a parser for the big decimal
	 * @return the Big Decimal if could be parsed, else throws an exception
	 */
	public static BigDecimal parseDecimal(
			Object object,
			int precision,
			int scale,
			String context,
			DecimalParser decimalparser) {
		if (object == null)
			return null;
		if (object instanceof Double) {
			Double doublevalue = (Double) object;
			BigDecimal parsedbigdecimal = BigDecimal.valueOf(doublevalue.doubleValue());
			int specialtreatment = decimalparser.getSpecialTreatment();
			if (specialtreatment == DecimalDataObjectFieldFlatFileLoaderColumn.DecimalParser.SPECIAL_TREATMENT_MULTIPLY_BY_1000)
				parsedbigdecimal = parsedbigdecimal.multiply(new BigDecimal(1000));
			if (specialtreatment == DecimalDataObjectFieldFlatFileLoaderColumn.DecimalParser.SPECIAL_TREATMENT_MULTIPLY_BY_100)
				parsedbigdecimal = parsedbigdecimal.multiply(new BigDecimal(100));

			if (specialtreatment == DecimalDataObjectFieldFlatFileLoaderColumn.DecimalParser.SPECIAL_TREATMENT_DIVIDE_BY_1000)
				parsedbigdecimal = parsedbigdecimal.divide(new BigDecimal(10000), scale, BigDecimal.ROUND_HALF_DOWN);

			if (parsedbigdecimal != null)
				if (parsedbigdecimal.scale() > scale) {
					parsedbigdecimal = parsedbigdecimal.setScale(scale, RoundingMode.HALF_DOWN);
				}
			if (precision >= 0)
				if (parsedbigdecimal.precision() > precision)
					throw new RuntimeException(
							"Trying to load a number with a precision too big " + parsedbigdecimal.precision()
									+ ", authorized precision = " + precision + ", number = " + parsedbigdecimal);
			return parsedbigdecimal;
		}
		if (object instanceof String) {
			String stringvalue = (String) object;
			BigDecimal parsedbigdecimal = decimalparser.parse(stringvalue);
			if (scale >= 0)
				if (parsedbigdecimal.scale() > scale)
					throw new RuntimeException("Trying to load a number with a scale too big, authorized scale = "
							+ scale + ", number = " + parsedbigdecimal);
			if (precision >= 0)
				if (parsedbigdecimal.precision() > precision)
					throw new RuntimeException("Trying to load a number with a scale too big, authorized precision = "
							+ precision + ", number = " + parsedbigdecimal);
			return parsedbigdecimal;
		}
		throw new RuntimeException("For " + context + ", received an object of unsupported type = " + object.getClass()
				+ " value = " + object);
	}

	/**
	 * generates a DateTimeFormatter from a string input format
	 * 
	 * @param inputformat the input format to parse (if null, default format
	 *                    'YYYY.MM.dd' is used)
	 * @param context     context for exception handling
	 * @return the DateTimeFormatter
	 */
	public static DateTimeFormatter generateFormat(String inputformat, String context) throws RuntimeException {
		String formatasstring = "yyyy.MM.dd";
		if (inputformat != null)
			if (inputformat.length() > 0)
				formatasstring = inputformat;
		try {

			logger.fine("Generating format for pattern " + formatasstring);
			DateTimeFormatter format = new DateTimeFormatterBuilder().appendPattern(formatasstring)
					.parseDefaulting(ChronoField.HOUR_OF_DAY, 12).parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)
					.parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0).toFormatter();
			return format;
		} catch (IllegalArgumentException e) {
			throw new RuntimeException("format " + formatasstring + " invalid for DateTimeFormatter for " + context);

		}
	}

	/**
	 * @param data
	 * @return
	 * @throws RuntimeException
	 */
	public String dropOneLineAsText(Object[] data) throws RuntimeException {
		if (data == null)
			return "null lign";
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < data.length; i++) {
			if (i > 0)
				buffer.append('|');
			buffer.append(FlatFileLoader.parseObject(data[i], "Drop one Line " + i));
		}
		return buffer.toString();
	}
}
