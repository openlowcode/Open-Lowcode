/********************************************************************************
 * Copyright (c) 2019 [Open Lowcode SAS](https://openlowcode.com/)
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0 .
 *
 * SPDX-License-Identifier: EPL-2.0
 ********************************************************************************/
package org.openlowcode.tools.messages;

import java.io.IOException;
import java.io.Reader;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;

import java.util.logging.Logger;


/**
 * Standard Implementation of the  message
 * Reader.
 * @author Open Lowcode SAS
 *
 */
public class MessageSimpleReader extends MessageReader {
	private Reader br;

	private static Logger logger = Logger.getLogger(MessageSimpleReader.class.getName());
	private static Base64.Decoder base64decoder = Base64.getDecoder();

	private final static int MESSAGE_START = '{';
	private final static int MESSAGE_END = '}';
	private final static int STRUCTURE_START = '[';
	private final static int STRUCTURE_END = ']';
	private final static int ARRAY_START = '(';
	private final static int ARRAY_END = ')';
	private final static int STRUCTURE_CONTENT = ':';
	private final static int ATTRIBUTE_SEPARATOR = ',';
	private final static int ATTRIBUTE_VALUE = '=';
	private final static int STRING_DELIMITER = '"';
	private final static int DATE_START = 'D';
	private final static int DECIMAL_START = 'X';
	private final static int BINARY_START = 'B';
	private final static int BINARY_SEPARATOR = ':';
	// null markers
	private final static int NULL_MARKER = 'N';
	private final static int NULL_STRING = 'S';

	private final static int ERROR_MARKER = '#';
	private final static int ERROR_SEPARATOR = ':';

	private long charcounter = 0;

	public final static String firstcharStringToken = "AZERTYUIOPQSDFGHJKLMWXCVBNazertyuiopqsdfghjklmwxcvbn";
	public final static String followingcharStringToken = "AZERTYUIOPQSDFGHJKLMWXCVBNazertyuiopqsdfghjklmwxcvbn1234567890_-";

	private final static String firstcharNumber = "-1234567890.";
	private final static String followingcharNumber = "1234567890.-E";

	private final static String firstcharDecimal = "X";
	private final static String followingcharDecimal = "-1234567890.";

	private final static int boolean_true = 'T';
	private final static int boolean_false = 'F';
	private boolean recording = false;
	private StringBuffer recordedstring = null;

	private int currentcharacter = ' ';

	private boolean isUnsignificant(int thischar) {
		if (thischar == ' ')
			return true;
		if (thischar == '\n')
			return true;
		return false;
	}

	private void removeBlank() throws IOException {
		while (isUnsignificant(currentcharacter)) {
			if (currentcharacter == -1)
				return;
			currentcharacter = readOneCharacter();

		}
	}

	private String getNumberToken() throws IOException {
		StringBuffer token = new StringBuffer();
		if (firstcharNumber.indexOf(currentcharacter) == -1)
			throw new RuntimeException(String.format("invalid first character in number %s : %c at %s",
					token.toString(), currentcharacter, this.returnBufferTrace()));
		char character = (char) currentcharacter;
		token.append(character);
		currentcharacter = readOneCharacter();

		if (currentcharacter == -1)
			throw new RuntimeException(String.format("end of file while parsing number token at path %s at %s",
					this.getCurrentElementPath(), this.returnBufferTrace()));

		while (followingcharNumber.indexOf(currentcharacter) != -1) {
			character = (char) currentcharacter;
			token.append(character);
			currentcharacter = readOneCharacter();

			if (currentcharacter == -1)
				throw new RuntimeException(String.format("end of file while parsing number token at path %s at %s",
						this.getCurrentElementPath(), this.returnBufferTrace()));
		}
		return token.toString();
	}

	private String getDecimalToken() throws IOException {
		StringBuffer token = new StringBuffer();
		if (firstcharDecimal.indexOf(currentcharacter) == -1)
			throw new RuntimeException(String.format("invalid first character in number %d : %c at %s",
					token.toString(), currentcharacter, this.returnBufferTrace()));
		char character = (char) currentcharacter;
		token.append(character);
		currentcharacter = readOneCharacter();

		if (currentcharacter == -1)
			throw new RuntimeException(String.format("end of file while parsing number token at path %s at %s ",
					this.getCurrentElementPath(), this.returnBufferTrace()));

		while (followingcharDecimal.indexOf(currentcharacter) != -1) {
			character = (char) currentcharacter;
			token.append(character);
			currentcharacter = readOneCharacter();

			if (currentcharacter == -1)
				throw new RuntimeException(String.format("end of file while parsing number token at path %s at %s",
						this.getCurrentElementPath(), this.returnBufferTrace()));
		}
		return token.toString();
	}

	private String getStringToken(String context, boolean openfirstchar) throws IOException {
		StringBuffer token = new StringBuffer();

		if (!openfirstchar)
			if (firstcharStringToken.indexOf(currentcharacter) == -1)
				throw new RuntimeException(
						String.format("invalid first character in string %s : %c at %s, extracontext %s",
								token.toString(), currentcharacter, this.returnBufferTrace(), context));
		if (openfirstchar)
			if (followingcharStringToken.indexOf(currentcharacter) == -1)
				throw new RuntimeException(
						String.format("invalid open first character in string %s : %c at %s, extracontext %s",
								token.toString(), currentcharacter, this.returnBufferTrace(), context));
		char character = (char) currentcharacter;
		token.append(character);
		currentcharacter = readOneCharacter();

		if (currentcharacter == -1)
			throw new RuntimeException(
					String.format("end of file while parsing string token at path %s at %s, extracontext %s",
							this.getCurrentElementPath(), this.returnBufferTrace(), context));
		while (followingcharStringToken.indexOf(currentcharacter) != -1) {
			character = (char) currentcharacter;
			token.append(character);
			currentcharacter = readOneCharacter();

			if (currentcharacter == -1)
				throw new RuntimeException(
						String.format("end of file while parsing string token at path %s at %s, extracontext %s",
								this.getCurrentElementPath(), this.returnBufferTrace(), context));
		}
		return token.toString();
	}

	private String getStringToken() throws IOException {
		return getStringToken(null, false);
	}

	/**
	 * Creates a message simple reader that will send a remote exception
	 * when error is sent back from the remote party
	 * @param br reader to be used
	 */
	public MessageSimpleReader(Reader br) {
		super();
		this.br = br;
		logger.finest("Message Simple Reader created");

	}

	/**
	 * @param br reader to be used
	 * @param throwremoteexception 'true' if remote exception sent when
	 * getting an error from remote party, 'false' else
	 */
	public MessageSimpleReader(Reader br, boolean throwremoteexception) {
		super(throwremoteexception);
		this.br = br;
		logger.finest("Message Simple Reader created with throwremoteexception = "+throwremoteexception);
	}

	/**
	 * @param recursivebreaker
	 * @return null if reader data end reached, the next CSPElement else
	 * @throws IOException if communication if broken
	 */
	private MessageElement parseNextElement(int recursivebreaker) throws IOException {
		
			// remove insignificant stuff
			removeBlank();
			if (currentcharacter == -1)
				throw new RuntimeException(String.format("end of file reached while parsing a token at path %s at %s",
						this.getCurrentElementPath(), this.returnBufferTrace()));
			// detects key character

			if (currentcharacter == ERROR_MARKER) {
				logger.info("Start parsing error element");

				currentcharacter = readOneCharacter();

				String numberparsed = getNumberToken();
				logger.info("Got number info " + numberparsed);
				try {
					int errorcode = new Integer(numberparsed).intValue();
					removeBlank();
					logger.finest("parsed number ");
					if (currentcharacter != ERROR_SEPARATOR)
						throw new RuntimeException(
								"for error code " + errorcode + ", separator is not correct. expected "
										+ ERROR_SEPARATOR + ", got " + currentcharacter);
					currentcharacter = readOneCharacter();

					logger.finest("(1) ");
					if (currentcharacter != STRING_DELIMITER)
						throw new RuntimeException(String.format(
								" Expected string as second argument of error, got the following character as string delimiter "
										+ currentcharacter + " at %s",
										this.returnBufferTrace()));
					currentcharacter = readOneCharacter();

					logger.finest("(2) ");
					String errormessage = parseStringAttribute("ERROR:" + errorcode);
					logger.finest("(3) " + errormessage);

					if (currentcharacter != ERROR_MARKER) {
						StringBuffer error = new StringBuffer("for error code" + errorcode + ", error message "
								+ errormessage + ", got bad error end delimiter ");
						error.append(currentcharacter);
						throw new RuntimeException(error.toString());
					}
					logger.finest("Finished parsing error ");
					currentcharacter = readOneCharacter();

					return new MessageError(errorcode, errormessage);
				} catch (NumberFormatException e) {
					throw new RuntimeException(
							String.format("number format for errorcode could not be parsed to an Integer : %s at %s",
									numberparsed, this.returnBufferTrace()));
				}
			}

			if (currentcharacter == MESSAGE_START) {
				charcounter = 0;
				currentcharacter = readOneCharacter();

				return new MessageStart();
			}

			if (currentcharacter == MESSAGE_END) {
				currentcharacter = readOneCharacter();

				return new MessageEnd();
			}

			if (currentcharacter == STRUCTURE_END) {
				currentcharacter = readOneCharacter();

				return new MessageEndStructure();
			}

			if (currentcharacter == STRUCTURE_START) {
				currentcharacter = readOneCharacter();

				removeBlank();
				if (currentcharacter == -1)
					throw new RuntimeException(String.format(
							"Reached End of File while parsing structure start context %s ", this.returnBufferTrace()));
				String structurename = getStringToken();
				return new MessageStartStructure(structurename);
			}

			if (currentcharacter == ARRAY_END) {
				currentcharacter = readOneCharacter();

				if (currentcharacter == ARRAY_END) {
					currentcharacter = readOneCharacter();

					return new MessageArrayEnd();
				} else
					throw new RuntimeException("When closing an array, double ')' is compulsory");
			}

			if (currentcharacter == ARRAY_START) {
				currentcharacter = readOneCharacter();

				// ************************** ARRAY START HEADER ********************
				if (currentcharacter == ARRAY_START) {
					currentcharacter = readOneCharacter();

					removeBlank();
					String arrayname = getStringToken();
					removeBlank();
					if (currentcharacter == ARRAY_START) {

						ArrayList<MessageFieldSpec> fieldspecs = new ArrayList<MessageFieldSpec>();
						int fieldcounter = 0;
						do {
							fieldcounter++;
							removeBlank();
							currentcharacter = readOneCharacter();

							String fieldname = getStringToken(
									"Array Start arrayname = " + arrayname + ", field counter = " + fieldcounter, true);
							removeBlank();
							if (currentcharacter == ATTRIBUTE_VALUE) {
								currentcharacter = readOneCharacter();

								removeBlank();
								Object type = this.parseAttributeContent(arrayname + "/" + fieldname);
								removeBlank();
								if (!(type instanceof String))
									throw new RuntimeException("Type for field " + fieldname + " should be a string");
								String typestring = (String) type;
								MessageFieldSpec spec = new MessageFieldSpec(fieldname, typestring);
								fieldspecs.add(spec);
							} else
								throw new RuntimeException(
										"Expecting '=' after field name " + fieldname + " for array " + arrayname);
						} while (currentcharacter == ',');
						if (currentcharacter != ARRAY_END)
							throw new RuntimeException(
									"Expecting an ')' at the end of a line of array header definition");
						currentcharacter = readOneCharacter();

						return new MessageArrayStart(arrayname, fieldspecs);
					} else
						throw new RuntimeException("Expecting '(' after arrayname for array " + arrayname);
				} else {
					// ********************** ARRAY PAYLOAD HEADER ******************

					removeBlank();
					ArrayList<Object> onelinepayload = new ArrayList<Object>();
					int counter = 0;
					do {
						if (counter > 0) {
							currentcharacter = readOneCharacter();

							removeBlank();
						}
						Object payload = this.parseAttributeContent("payload column " + counter);
						onelinepayload.add(payload);
						counter++;
					} while (currentcharacter == ',');
					if (currentcharacter != ARRAY_END)
						throw new RuntimeException("Expecting an ')' at the end of a line of array payload definition");
					currentcharacter = readOneCharacter();

					return new MessageArrayLine(onelinepayload.toArray(new Object[0]));

				}
			}

			if ((currentcharacter == STRUCTURE_CONTENT) || // before first attribute of a structure
					(currentcharacter == ATTRIBUTE_SEPARATOR)) { // between attributes of a structure
				// loooking for an attribute
				currentcharacter = readOneCharacter();

				removeBlank();
				String attributename = getStringToken();
				removeBlank();
				if (currentcharacter != ATTRIBUTE_VALUE) {
					// no = found after attributename, invalid
					throw new RuntimeException(String.format(
							"After attribute %s, attribute separator '=' should be found, but character %c was found at %s",
							attributename, currentcharacter, this.returnBufferTrace()));
				} else { // if equals, parse content
					currentcharacter = readOneCharacter();

					removeBlank();
					/*
					 * --------- Treatment of strings. String are delimited by double quotes'"',
					 * with two consecutive double quotes to mean a double quote in string. Strings
					 * can have return carriage
					 */
					if (currentcharacter == STRING_DELIMITER) {
						currentcharacter = readOneCharacter();

						String attributecontent = parseStringAttribute(attributename);
						return new MessageStringField(attributename, attributecontent);
					}
					// treatment of integer or floats
					if (firstcharNumber.indexOf(currentcharacter) != -1) {
						String numberparsed = getNumberToken();

						// tries to parse it to integer
						try {
							int value = new Integer(numberparsed).intValue();

							return MessageIntegerField.getCSPMessageIntegerField(attributename, value);
						} catch (NumberFormatException e) {
							throw new RuntimeException(String.format(
									"number format for attribute %s could not be parsed to an Integer : %s at %s",
									attributename, numberparsed, this.returnBufferTrace()));
						}
					}
					// treatment of booleans

					if (currentcharacter == boolean_true) {
						currentcharacter = readOneCharacter();

						return new MessageBooleanField(attributename, true);
					}

					if (currentcharacter == boolean_false) {
						currentcharacter = readOneCharacter();

						return new MessageBooleanField(attributename, false);

					}

					if (currentcharacter == BINARY_START) {
						currentcharacter = readOneCharacter();

						String sizetext = getNumberToken();
						int size = new Integer(sizetext).intValue();
						logger.finer("file size  = " + size);
						if (currentcharacter != BINARY_SEPARATOR)
							throw new RuntimeException(String.format(
									"Expected to have a separator ':' after size %d in binary field, got '%c' at %s",
									size, currentcharacter, this.returnBufferTrace()));
						currentcharacter = readOneCharacter();

						if (size > 0) {

							if (currentcharacter != STRING_DELIMITER)
								throw new RuntimeException(
										String.format(" Expected string as filename in binary element %c at %s",
												currentcharacter, this.returnBufferTrace()));
							currentcharacter = readOneCharacter();

							String filename = this.parseStringAttribute("#BINARYPAYLOAD#");

							if (currentcharacter != BINARY_SEPARATOR)
								throw new RuntimeException(String.format(
										"Expected to have a separator ':' after filename '%s' in binary field, got '%c' at %s",
										filename, currentcharacter, this.returnBufferTrace()));
							char[] base64content = new char[size];
							int read = 0;
							int breaker = 0;
							while ((read < size) && (breaker < 100000)) {
								read += br.read(base64content, read, size - read);
								breaker++;
								logger.fine("total read = " + read);
								try {
									Thread.sleep(20);

								} catch (InterruptedException e) {
									logger.warning(
											"Error while waiting for file to be completly received " + e.getMessage());
								}
							}

							charcounter += size;
							String base64string = new String(base64content);
							// Correct code
							logger.fine("read = " + read + "generated ing length = " + base64string.length()
							+ " hascode=" + base64string.hashCode() + " first 3 char = '"
							+ base64string.substring(0, 3) + "' last 100 chars = '" + base64string.substring(
									(base64string.length() - 100 > 0 ? base64string.length() - 100 : 0)));

							byte[] binary = base64decoder.decode(base64string);
							currentcharacter = readOneCharacter();

							return new MessageBinaryField(attributename, binary, filename);
						}
						if (size == 0)
							return new MessageBinaryField(attributename);
					}

					if (currentcharacter == DECIMAL_START) {
						String token = getDecimalToken();

						BigDecimal decimal = null;
						if (token.length() > 1) {
							try {
								decimal = new BigDecimal(token.substring(1));

							} catch (NumberFormatException e) {
								throw new RuntimeException(String.format(
										"invalid decimal format for attribute %s for value %s, original exception : %s at %s",
										attributename, token, e.getMessage(), this.returnBufferTrace()));
							}
						}
						return new MessageDecimalField(attributename, decimal);
					}
					if (currentcharacter == DATE_START) {
						String token = getStringToken();
						Date date = null;
						if (token.length() > 1)
							try {
								date = MessageDateField.sdf.parse(token);
							} catch (ParseException e) {
								throw new RuntimeException(String.format(
										"invalid date format for attribute %s for value %s, original exception : %s at %s ",
										attributename, token, e.getMessage(), this.returnBufferTrace()));
							}
						return new MessageDateField(attributename, date);
					}
					if (currentcharacter == NULL_MARKER) {
						currentcharacter = readOneCharacter();

						if (currentcharacter == NULL_STRING) {
							currentcharacter = readOneCharacter();

							return new MessageStringField(attributename, null);
						}

					}
					throw new RuntimeException(
							String.format("parsing of attribute %s: content not supported at path %s at %s",
									attributename, this.getCurrentElementPath(), this.returnBufferTrace()));
				}
			}

			throw new RuntimeException(
					String.format("no element could be parsed, invalid character %c at path %s at %s", currentcharacter,
							this.getCurrentElementPath(), this.returnBufferTrace()));

		
	}

	/**
	 * This method parses attribute contents. Supported:
	 * <ul>
	 * <li>Boolean</li>
	 * <li>BigDecimal</li>
	 * <li>Integer</li>
	 * <li>String</li>
	 * </ul>
	 * 
	 * @return a class of the given type. Wrapper classes are used any time
	 *         necessary
	 * @throws IOException if communication is broken
	 */
	private Object parseAttributeContent(String context) throws IOException {
		if (currentcharacter == STRING_DELIMITER) {
			currentcharacter = readOneCharacter();

			String attributecontent = parseStringAttribute(context);
			return attributecontent;
		}
		// treatment of integer or floats
		if (firstcharNumber.indexOf(currentcharacter) != -1) {
			String numberparsed = getNumberToken();

			// tries to parse it to integer
			try {
				int value = new Integer(numberparsed).intValue();

				return new Integer(value);
			} catch (NumberFormatException e) {
				throw new RuntimeException(
						String.format("number format for attribute %s could not be parsed to an Integer : %s at %s",
								context, numberparsed, this.returnBufferTrace()));
			}
		}
		if (currentcharacter == boolean_true) {
			currentcharacter = readOneCharacter();

			return new Boolean(true);
		}

		if (currentcharacter == boolean_false) {
			currentcharacter = readOneCharacter();

			return new Boolean(false);

		}

		if (currentcharacter == NULL_MARKER) {
			currentcharacter = readOneCharacter();

			if (currentcharacter == NULL_STRING) {
				currentcharacter = readOneCharacter();

				return null;
			}

		}
		if (currentcharacter == DECIMAL_START) {
			String token = getDecimalToken();

			BigDecimal decimal = null;
			if (token.length() > 1) {
				try {
					decimal = new BigDecimal(token.substring(1));

				} catch (NumberFormatException e) {
					throw new RuntimeException(String.format(
							"invalid decimal format for attribute %s for value %s, original exception : %s at %s",
							context, token, e.getMessage(), this.returnBufferTrace()));
				}
			}
			return decimal;
		}
		if (currentcharacter == DATE_START) {
			String token = getStringToken();
			Date date = null;
			if (token.length() > 1)
				try {
					date = MessageDateField.sdf.parse(token);
				} catch (ParseException e) {
					throw new RuntimeException(String.format(
							"invalid date format for attribute %s for value %s, original exception : %s at %s ",
							context, token, e.getMessage(), this.returnBufferTrace()));
				}
			return date;
		}
		throw new RuntimeException(String.format(
				"did not find a supported field type for " + context + " current char = '%c'", currentcharacter));
	}

	/**
	 * parses a string attribute with current character set to immediately after the
	 * first '"'
	 * 
	 * @return
	 */
	private String parseStringAttribute(String attributename) throws IOException {
		StringBuffer content = new StringBuffer();
		boolean laststringdelimiter = false;
		while (currentcharacter != -1) {
			char character = (char) currentcharacter;
			// no '"' as last string -- normal process
			if (!laststringdelimiter)
				if (currentcharacter != STRING_DELIMITER)
					content.append(character);

			if (laststringdelimiter)
				if (currentcharacter != STRING_DELIMITER) {

					return content.toString();
				}
			if (currentcharacter == STRING_DELIMITER) {
				if (laststringdelimiter) {
					laststringdelimiter = false;
					content.append(character);
				} else {
					laststringdelimiter = true;
				}
			}

			currentcharacter = readOneCharacter();

		}

		throw new RuntimeException(String.format(
				"End of File reached while parsing string content for attribute at path %s at %s" + attributename,
				this.getCurrentElementPath(), this.returnBufferTrace()));
	}

	private int readOneCharacter() throws IOException {
		int thischar = br.read();
		charcounter++;
		if (this.recording) {
			this.recordedstring.append((char) thischar);
		}
		return thischar;
	}

	public void close() throws IOException {
		this.br.close();
	}

	@Override
	protected MessageElement parseNextElement() throws OLcRemoteException, IOException {
		// as algorithm is recursive, method is implemented with a circuit breaker
		return parseNextElement(0);
	}

	@Override
	public long charcountsinceStartMessage() {
		return charcounter;
	}

	@Override
	public void startrecord() {

		this.recording = true;
		this.recordedstring = new StringBuffer();
	}

	@Override
	public String endrecord() {
		if (!this.recording)
			return null;
		this.recording = false;
		return this.recordedstring.substring(0, recordedstring.length() - 1).toString();
	}

}
