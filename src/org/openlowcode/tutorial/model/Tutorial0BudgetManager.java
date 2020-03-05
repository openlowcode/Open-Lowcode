package org.openlowcode.tutorial.model;

import org.openlowcode.design.advanced.LineGroupingCriteriaObject;
import org.openlowcode.design.advanced.LinkedToChildrenNodeLink;
import org.openlowcode.design.advanced.MainReportValueDecimalField;
import org.openlowcode.design.advanced.ObjectReportNode;
import org.openlowcode.design.advanced.SmartReport;
import org.openlowcode.design.advanced.SumValueConsolidator;
import org.openlowcode.design.advanced.TimePeriodColumnCriteria;
import org.openlowcode.design.data.ChoiceField;
import org.openlowcode.design.data.ChoiceValue;
import org.openlowcode.design.data.DataObjectDefinition;
import org.openlowcode.design.data.DecimalField;
import org.openlowcode.design.data.SimpleChoiceCategory;
import org.openlowcode.design.data.TimePeriodField;
import org.openlowcode.design.data.properties.basic.CreationLog;
import org.openlowcode.design.data.properties.basic.Iterated;
import org.openlowcode.design.data.properties.basic.LinkedToParent;
import org.openlowcode.design.data.properties.basic.Named;
import org.openlowcode.design.data.properties.basic.Numbered;
import org.openlowcode.design.data.properties.basic.StoredObject;
import org.openlowcode.design.data.properties.basic.UniqueIdentified;
import org.openlowcode.design.data.properties.basic.UpdateLog;
import org.openlowcode.design.module.Module;
import org.openlowcode.tools.data.TimePeriod.PeriodType;

public class Tutorial0BudgetManager extends Module {

	private DataObjectDefinition product;
	private DataObjectDefinition workitem;
	private DataObjectDefinition budget;
	private LinkedToParent<?> workitemlinkedtoparent;
	private LinkedToParent<?> budgetlinkedtoparent;
	private DecimalField amount;
	private TimePeriodField budgetyear;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Tutorial0BudgetManager() {
		super("TUTORIAL","TT","Tutorial","openlowcode.tutorial.budgetmanager","Open Lowcode","v1.0","Welcome to Open Lowcode tutorial");
		
		SimpleChoiceCategory department = new SimpleChoiceCategory("DEPARTMENT", 20);
		department.addValue(new ChoiceValue("PROD", "Production", ""));
		department.addValue(new ChoiceValue("DO", "Design-Office", ""));
		this.addChoiceCategory(department);
		
		// ---- create objects ---
		product = new DataObjectDefinition("PRODUCT","Product",this);
		workitem = new DataObjectDefinition("WORKITEM","Work-Item",this);
		budget = new DataObjectDefinition("BUDGET","Budget",this);
		
		// ----- basic properties
		product.addProperties(new StoredObject(),new UniqueIdentified(),new Numbered(),new Named());
		workitem.addProperties(new StoredObject(),new UniqueIdentified(),new Numbered(),new Named());
		budget.addProperties(new StoredObject(),new UniqueIdentified(),new CreationLog(),new UpdateLog(),new Iterated());
		
		// ---- attributes
		budget.addField(amount = new DecimalField("AMOUNT", "Amount","", 12, 4, DecimalField.INDEXTYPE_NONE));
		budget.addField(budgetyear = new TimePeriodField("BUDGETYEAR", "Year","", PeriodType.STRICT_YEAR));
		budget.addField(new ChoiceField("DEPARTMENT","Department","", department));
		
		// ---- create links ---
		workitem.addProperty(workitemlinkedtoparent = new LinkedToParent("PRODUCT", product));
		budget.addProperty(budgetlinkedtoparent = new LinkedToParent("WORKITEM",workitem));
		
		// ---- smart report node
		ObjectReportNode productnode = new ObjectReportNode(product);
		ObjectReportNode workitemnode = new ObjectReportNode(workitem);
		ObjectReportNode budgetnode = new ObjectReportNode(budget);
		budgetnode.setMainReportValue(new MainReportValueDecimalField(amount, new SumValueConsolidator()));
		budgetnode.setColumnCriteria(new TimePeriodColumnCriteria(budgetnode, budgetyear));
		workitemnode.addGroupingCriteria(new LineGroupingCriteriaObject(workitem, true,true,true));
		productnode.addChildNode(new LinkedToChildrenNodeLink(workitemnode, workitemlinkedtoparent));
		workitemnode.addChildNode(new LinkedToChildrenNodeLink(budgetnode,budgetlinkedtoparent));
		this.addAdvancedFeature(new SmartReport("PRODUCTREPORT", "Product Budget Report", productnode));
	}

}
