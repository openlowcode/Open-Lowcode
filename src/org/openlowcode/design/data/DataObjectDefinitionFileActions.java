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

import org.openlowcode.design.data.properties.basic.LinkedFromChildren;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.generation.SourceGenerator;
import org.openlowcode.design.generation.StringFormatter;
import org.openlowcode.design.module.Module;

/**
 * This utility class gathers all actions related to file import and export
 * inside the Open Lowcode framework
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class DataObjectDefinitionFileActions {
	/**
	 * generates the export children to file action code
	 * 
	 * @param name               data object name
	 * @param sg                 source generator
	 * @param module             parent module
	 * @param linkedfromchildren linked from children property relevant for children
	 *                           export
	 * @throws IOException if anything bad happens during code generation
	 */
	public static void generateExportChildrenToFile(
			String name,
			SourceGenerator sg,
			Module module,
			LinkedFromChildren linkedfromchildren) throws IOException {
		String linkedfromchildrenvariable = StringFormatter.formatForAttribute(linkedfromchildren.getInstancename());
		String objectvariable = StringFormatter.formatForAttribute(name);
		String objectclass = StringFormatter.formatForJavaClass(name);

		String childclass = StringFormatter.formatForJavaClass(linkedfromchildren.getChildObject().getName());
		String childvariable = StringFormatter.formatForAttribute(linkedfromchildren.getChildObject().getName());
		boolean hasextractorchoice = false;
		ChoiceCategory categoryforextractor = null;
		String returntype = "ActionOutputData";
		if (linkedfromchildren.getChildObject().getCategoryForExtractor() != null) {
			hasextractorchoice = true;
			categoryforextractor = linkedfromchildren.getChildObject().getCategoryForExtractor();
			returntype = "SFile";
		}

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + linkedfromchildren.getChildObject().getOwnermodule().getPath() + ".data." + childclass + ";");
		sg.wl("import org.openlowcode.server.data.loader.FlatFileExtractor;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		if (hasextractorchoice) {
			sg.wl("import org.openlowcode.server.data.ChoiceValue;");
			;
			sg.wl("import " + categoryforextractor.getParentModule().getPath() + ".data.choice."
					+ StringFormatter.formatForJavaClass(categoryforextractor.getName()) + "ChoiceDefinition;");
		}
		sg.wl("");
		sg.wl("public class AtgExportchildren" + linkedfromchildrenvariable + "for" + objectvariable + "Action");
		sg.wl("		extends AbsExportchildren" + linkedfromchildrenvariable + "for" + objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgExportchildren" + linkedfromchildrenvariable + "for" + objectvariable
				+ "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public " + returntype + " executeActionLogic(DataObjectId<" + objectclass + "> parentid,");
		if (hasextractorchoice) {
			sg.wl("			ChoiceValue<" + StringFormatter.formatForJavaClass(categoryforextractor.getName())
					+ "ChoiceDefinition>  exporttype,");
		}
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + childclass + "[] " + childvariable + " = " + childclass + ".getallchildrenfor"
				+ linkedfromchildren.getRelatedLinkedToParent().getInstancename().toLowerCase() + "(parentid,null);");
		sg.wl("		FlatFileExtractor<" + childclass + "> extractor = new FlatFileExtractor<" + childclass + ">("
				+ childclass + ".getDefinition());");
		String extraparameter = "";
		if (hasextractorchoice) {
			boolean processed = false;
			if (isObjectDynamicHelper(linkedfromchildren.getChildObject(),name)) {
					sg.wl("		String[] specificalias = " + childclass
							+ ".getDefinition().getSpecificAliasList(exporttype,parentid);");
					processed = true;
				}
			
			if (!processed) {
				sg.wl("		String[] specificalias = " + childclass
						+ ".getDefinition().getSpecificAliasList(exporttype);");

			}
			extraparameter = ",specificalias";
		} else {
			if (isObjectDynamicHelper(linkedfromchildren.getChildObject(),name)) {
				sg.wl("		String[] specificalias = " + childclass
						+ ".getDefinition().getSpecificAliasList(parentid);");
				extraparameter = ",specificalias";
			}
		}
		
		sg.wl("		SFile returnfile = extractor.extractToExcel(" + childvariable + extraparameter + ");");
		if (hasextractorchoice) {
			sg.wl("		return returnfile;");
		} else {
			sg.wl("		return new ActionOutputData(returnfile);");
		}
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(" + returntype + " logicoutput)  {");
		sg.wl("		return null;");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * Check if the object has a filter on parent (for dynamic or normal aliases)
	 * @param object
	 * @param name
	 * @return
	 */
	public static boolean isObjectDynamicHelper(DataObjectDefinition object,String name) {
		if (object.getAliasFilterOnParent()!=null) if (object.getAliasFilterOnParent().getName().equals(name)) return true;
		if (object.getDynamicAliasFilterOnParent()!=null) if (object.getDynamicAliasFilterOnParent().getName().equals(name)) return true;
		return false;
	}
	
	/**
	 * generates the load children to file action code
	 * 
	 * @param name               data object name
	 * @param sg                 source generator
	 * @param module             parent module
	 * @param linkedfromchildren linked from children property relevant for children
	 *                           export
	 * @throws IOException if anything bad happens during code generation
	 */

	public static void generateLoadChildrenToFile(
			String name,
			SourceGenerator sg,
			Module module,
			LinkedFromChildren linkedfromchildren) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String childclass = StringFormatter.formatForJavaClass(linkedfromchildren.getChildObject().getName());
		String childattribute = StringFormatter.formatForAttribute(linkedfromchildren.getChildObject().getName());
		String filename = StringFormatter
				.formatForJavaClass("LOADCHILDREN" + linkedfromchildren.getInstancename() + "FOR" + name);
		;
		LinkedToParent<?> linkedtoparent = linkedfromchildren.getOriginObjectProperty();

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("import java.util.function.Function;");
		sg.wl("import java.util.logging.Logger;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("");

		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".data." + childclass + ";");
		sg.wl("import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;");
		sg.wl("import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;");
		sg.wl("import org.openlowcode.module.system.page.ShowloadingreportforchildrenPage;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.server.data.DataObjectDefinition;");
		sg.wl("import org.openlowcode.server.data.loader.FlatFileLoader;");
		sg.wl("import org.openlowcode.server.data.loader.FlatFileLoaderColumn.LinePreparationExtra;");
		sg.wl("import org.openlowcode.server.data.loader.FlatFileLoaderReport;");
		sg.wl("import org.openlowcode.server.data.loader.FlatFileLoaderSupplement;");
		sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
		sg.wl("import org.openlowcode.server.data.properties.LinkedtoparentQueryHelper;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		sg.wl("");
		sg.wl("public class Atg" + filename + "Action extends Abs" + filename + "Action {");
		sg.wl("	private static Logger logger = Logger.getLogger(Atg" + filename + "Action.class.getName());");
		sg.wl("	public Atg" + filename + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(DataObjectId<" + objectclass + "> parentid,");
		sg.wl("			ChoiceValue<ApplocaleChoiceDefinition> locale,ChoiceValue<PreferedfileencodingChoiceDefinition> fileencoding, SFile flatfile,");
		sg.wl("			Function<TableAlias, QueryFilter> datafilter)  {");
		sg.wl("		" + objectclass + " thisparent = " + objectclass + ".readone(parentid);");
		sg.wl("		FlatFileLoaderSupplement<" + childclass + "> loadersupplement = new FlatFileLoaderSupplement<"
				+ childclass + ">() {");
		sg.wl("");
		sg.wl("			@Override");
		sg.wl("			public LinePreparationExtra<" + childclass + "> getSupplement()  {");
		sg.wl("				// TODO Auto-generated method stub");
		sg.wl("				return new LinePreparationExtra<" + childclass + ">() {");
		sg.wl("");
		sg.wl("					@Override");
		sg.wl("					public QueryCondition generateQueryCondition(DataObjectDefinition<" + childclass + "> "
				+ childattribute + "definition, String alias)");
		sg.wl("							 {");
		sg.wl("						return LinkedtoparentQueryHelper.get(\"LINKEDTOPARENTFOR"
				+ linkedtoparent.getInstancename().toUpperCase() + "\").getParentIdQueryCondition(" + childclass
				+ ".getDefinition().getAlias(alias), parentid, " + childclass + ".getDefinition()," + objectclass
				+ ".getDefinition());");
		sg.wl("					}");
		sg.wl("");
		sg.wl("				};");
		sg.wl("			}");
		sg.wl("");
		sg.wl("			@Override");
		sg.wl("			protected void initializeNewObject(" + childclass + " " + childattribute + ")  {");
		sg.wl("				" + childattribute + ".setparentwithoutupdatefor"
				+ linkedfromchildren.getOriginObjectProperty().getInstancename().toLowerCase() + "(parentid);");
		sg.wl("				");
		sg.wl("			}");
		sg.wl("");
		sg.wl("		};");
		sg.wl("		FlatFileLoader<" + childclass + "> loader = new FlatFileLoader<" + childclass + ">(" + childclass
				+ ".getDefinition(),locale,fileencoding,loadersupplement);");
		sg.wl("		FlatFileLoaderReport returnmessage = loader.load(flatfile);");
		sg.wl("		return new ActionOutputData(returnmessage.getContext(),");
		sg.wl("				returnmessage.getInserted(),");
		sg.wl("				returnmessage.getUpdated(),");
		sg.wl("				returnmessage.getError(),");
		sg.wl("				returnmessage.getPostprocError(),");
		sg.wl("				(int)(returnmessage.getLoadingtimems()/1000),");
		sg.wl("				returnmessage.getErrordetails(),");
		sg.wl("				parentid);");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return new ShowloadingreportforchildrenPage(logicoutput.getLoadingcontext(),");
		sg.wl("				logicoutput.getInserted(),");
		sg.wl("				logicoutput.getUpdated(),");
		sg.wl("				logicoutput.getErrors(), ");
		sg.wl("				logicoutput.getPostprocerrors(),");
		sg.wl("				logicoutput.getLoadingtime(),");
		sg.wl("				logicoutput.getErrordetail(),");
		sg.wl("				logicoutput.getParentid_thru());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}	");

		sg.close();
	}

	/**
	 * generates the code for the action that generates a flat file sample for
	 * import in CSV
	 * 
	 * @param name   data object name
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens during code generation
	 */
	public static void generateFlatFileSampleToFile(String name, SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("import java.io.BufferedWriter;");
		sg.wl("import java.io.ByteArrayOutputStream;");
		sg.wl("import java.io.IOException;");
		sg.wl("import java.io.OutputStreamWriter;");
		sg.wl("import java.util.ArrayList;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import " + module.getPath() + ".page.generated.AtgSearch" + objectvariable + "Page;");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.module.system.data.Appuser;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.runtime.OLcServer;");
		sg.wl("import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;");
		sg.wl("");
		sg.wl("public class AtgGenerateflatfilesamplefor" + objectvariable
				+ "Action extends AbsGenerateflatfilesamplefor" + objectvariable + "Action {");
		sg.wl("");
		sg.wl("	public AtgGenerateflatfilesamplefor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(Function<TableAlias,QueryFilter> datafilter)  {");
		sg.wl("		try {");

		sg.wl("			Appuser currentuser = OLcServer.getServer().getCurrentUser();");
		sg.wl("			String csvseparator = \",\";");
		sg.wl("			if (currentuser.getPreflang()!=null) if (currentuser.getPreflang().equals(ApplocaleChoiceDefinition.get().FR)) csvseparator = \";\";");
		sg.wl("			ArrayList<String[]> sample = " + objectclass
				+ ".getDefinition().getFlatFileLoaderDescription();");
		sg.wl("			ByteArrayOutputStream baos = new ByteArrayOutputStream();");
		sg.wl("			BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(baos));");
		sg.wl("			");
		sg.wl("			writer.write(\"\\\"This sample file shows specifies column title in first line,\\n optional / mandatory in second line,\\n a sample value in third line,\\n and comments in fourth line.\\nA loading file should have titles for chosen column, followed by '&' and optional tags in same cell as first line,\\nwith subsequent lines having the values.\\\"\\n\");");
		sg.wl("			for (int i=0;i<4;i++) for  (int j=0;j<sample.size();j++) {");
		sg.wl("				writer.write(\"\\\"\"+sample.get(j)[i].replaceAll(\"\\\"\",\"\\\"\\\"\")+\"\\\"\"+csvseparator);");
		sg.wl("				if (j==sample.size()-1) writer.write(\"\\n\");");
		sg.wl("			}");
		sg.wl("				writer.close();");
		sg.wl("				ActionOutputData outputdata = new  ActionOutputData(new SFile(\"sample" + objectvariable
				+ ".csv\",baos.toByteArray()));");
		sg.wl("				return outputdata;");
		sg.wl("		} catch (IOException e) {");
		sg.wl("			throw new RuntimeException(\"error in generating sample file \"+e.getMessage());");
		sg.wl("		}");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");
		sg.wl("		return AtgLaunchsearch" + objectvariable + "Action.get().executeAndShowPage();");
		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}

	/**
	 * generates the code for a local flat file loader action
	 * 
	 * @param name   data object name
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens during code generation
	 * @since 1.15
	 */
	public static void generateFlatFileLocalLoaderToFile(String name, SourceGenerator sg, Module module,DataObjectDefinition object) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(object.getName());
		String objectvariable = StringFormatter.formatForAttribute(object.getName());
		String loadername = StringFormatter.formatForJavaClass(name);
		
sg.wl("package " + module.getPath() + ".action.generated;");
sg.wl("");
sg.wl("import java.util.function.Function;");
sg.wl("");
sg.wl("import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;");
sg.wl("import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;");
sg.wl("import org.openlowcode.module.system.page.ShowloadingreportforchildrenPage;");
sg.wl("import org.openlowcode.server.data.ChoiceValue;");
sg.wl("import org.openlowcode.server.data.loader.FlatFileLoader;");
sg.wl("import org.openlowcode.server.data.loader.FlatFileLoaderReport;");
sg.wl("import org.openlowcode.server.data.properties.DataObjectId;");
sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
sg.wl("import org.openlowcode.server.graphic.SPage;");
sg.wl("import org.openlowcode.server.runtime.SModule;");
sg.wl("import org.openlowcode.tools.messages.SFile;");
sg.wl("");
sg.wl("");
sg.wl("import " + module.getPath() + ".data."+objectclass+";");
sg.wl("");
sg.wl("public class Atg"+loadername+"Action");
sg.wl("		extends");
sg.wl("		Abs"+loadername+"Action {");
sg.wl("");
sg.wl("	public Atg"+loadername+"Action(SModule parent) {");
sg.wl("		super(parent);");
sg.wl("");
sg.wl("	}");
sg.wl("");
sg.wl("	@Override");
sg.wl("	public ActionOutputData executeActionLogic(");
sg.wl("			DataObjectId<"+objectclass+"> "+objectvariable+"id,");
sg.wl("			ChoiceValue<ApplocaleChoiceDefinition> locale,");
sg.wl("			ChoiceValue<PreferedfileencodingChoiceDefinition> encoding,");
sg.wl("			SFile file,");
sg.wl("			Function<TableAlias, QueryFilter> datafilter) {");
sg.wl("		"+objectclass+" "+objectvariable+" = "+objectclass+".readone("+objectvariable+"id);");
sg.wl("		FlatFileLoader<"+objectclass+"> loader = new FlatFileLoader<"+objectclass+">("+objectclass+".getDefinition(),locale,encoding);");
sg.wl("		loader.setHardObject("+objectvariable+");");
sg.wl("		FlatFileLoaderReport returnmessage = loader.load(file);");
sg.wl("		return new ActionOutputData(returnmessage.getContext(),");
sg.wl("				returnmessage.getInserted(),");
sg.wl("				returnmessage.getUpdated(),");
sg.wl("				returnmessage.getError(),");
sg.wl("				returnmessage.getPostprocError(),");
sg.wl("				(int)(returnmessage.getLoadingtimems()/1000),");
sg.wl("				returnmessage.getErrordetails(),");
sg.wl("				"+objectvariable+"id);");
sg.wl("	}");
sg.wl("");
sg.wl("	@Override");
sg.wl("	public SPage choosePage(ActionOutputData logicoutput) {");
sg.wl("		return new ShowloadingreportforchildrenPage(logicoutput.getLoadingcontext(),");
sg.wl("				logicoutput.getInserted(),");
sg.wl("				logicoutput.getUpdated(),");
sg.wl("				logicoutput.getErrors(),"); 
sg.wl("				logicoutput.getPostprocerrors(),");
sg.wl("				logicoutput.getLoadingtime(),");
sg.wl("				logicoutput.getErrordetail(),");
sg.wl("				logicoutput.get"+objectclass+"id_thru());");
sg.wl("	}");
sg.wl("");
sg.wl("}");
		

		
		

		
		sg.close();
	}
	
	/**
	 * generates the code for the flat file loader action
	 * 
	 * @param name   data object name
	 * @param sg     source generator
	 * @param module parent module
	 * @throws IOException if anything bad happens during code generation
	 */
	public static void generateFlatFileLoaderToFile(String name, SourceGenerator sg, Module module) throws IOException {
		String objectclass = StringFormatter.formatForJavaClass(name);
		String objectvariable = StringFormatter.formatForAttribute(name);

		sg.wl("package " + module.getPath() + ".action.generated;");
		sg.wl("");
		sg.wl("");
		sg.wl("import java.util.logging.Logger;");
		sg.wl("");
		sg.wl("import " + module.getPath() + ".action.generated.AbsFlatfileloaderfor" + objectvariable + "Action;");
		sg.wl("import " + module.getPath() + ".page.generated.AtgSearch" + objectvariable + "Page;");
		sg.wl("import " + module.getPath() + ".data." + objectclass + ";");
		sg.wl("import org.openlowcode.module.system.data.choice.ApplocaleChoiceDefinition;");
		sg.wl("import org.openlowcode.module.system.data.choice.PreferedfileencodingChoiceDefinition;");
		sg.wl("import org.openlowcode.server.data.loader.FlatFileLoader;");
		sg.wl("import org.openlowcode.server.data.loader.FlatFileLoaderReport;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("import org.openlowcode.module.system.page.ShowloadingreportPage;");
		sg.wl("");
		sg.wl("import org.openlowcode.server.graphic.SPage;");
		sg.wl("import org.openlowcode.server.runtime.SModule;");
		sg.wl("import org.openlowcode.tools.messages.SFile;");
		sg.wl("import java.util.function.Function;");
		sg.wl("import org.openlowcode.server.data.storage.QueryFilter;");
		sg.wl("import org.openlowcode.server.data.storage.QueryCondition;");
		sg.wl("import org.openlowcode.server.data.storage.TableAlias;");
		sg.wl("import org.openlowcode.server.data.ChoiceValue;");
		sg.wl("");
		sg.wl("public class AtgFlatfileloaderfor" + objectvariable + "Action extends AbsFlatfileloaderfor"
				+ objectvariable + "Action {");
		sg.wl("	private static Logger logger = Logger.getLogger(AtgFlatfileloaderfor" + objectvariable
				+ "Action.class.getName());");
		sg.wl("	public AtgFlatfileloaderfor" + objectvariable + "Action(SModule parent) {");
		sg.wl("		super(parent);");
		sg.wl("		");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public ActionOutputData executeActionLogic(ChoiceValue<ApplocaleChoiceDefinition> locale,ChoiceValue<PreferedfileencodingChoiceDefinition> fileencoding,SFile flatfile,Function<TableAlias,QueryFilter> datafilter)  {");

		sg.wl("		FlatFileLoader loader = new FlatFileLoader(" + objectclass
				+ ".getDefinition(),locale,fileencoding);");
		sg.wl("		FlatFileLoaderReport returnmessage = loader.load(flatfile);");
		sg.wl("		return new ActionOutputData(returnmessage.getContext(),");
		sg.wl("				returnmessage.getInserted(),");
		sg.wl("				returnmessage.getUpdated(),");
		sg.wl("				returnmessage.getError(),");
		sg.wl("				returnmessage.getPostprocError(),");
		sg.wl("				(int)(returnmessage.getLoadingtimems()/1000),");
		sg.wl("				returnmessage.getErrordetails());");
		sg.wl("	}");
		sg.wl("");
		sg.wl("	@Override");
		sg.wl("	public SPage choosePage(ActionOutputData logicoutput)  {");

		sg.wl("		return new ShowloadingreportPage(logicoutput.getLoadingcontext(),");
		sg.wl("				logicoutput.getInserted(),");
		sg.wl("				logicoutput.getUpdated(),");
		sg.wl("				logicoutput.getErrors(), ");
		sg.wl("				logicoutput.getPostprocerrors(),");
		sg.wl("				logicoutput.getLoadingtime(),");
		sg.wl("				logicoutput.getErrordetail());");

		sg.wl("	}");
		sg.wl("");
		sg.wl("}");

		sg.close();
	}
}
