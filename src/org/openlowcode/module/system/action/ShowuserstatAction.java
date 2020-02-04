package org.openlowcode.module.system.action;

import java.util.function.Function;

import org.openlowcode.module.system.action.generated.AbsShowuserstatAction;
import org.openlowcode.module.system.action.generated.AtgShowappuserAction;
import org.openlowcode.module.system.data.Appuser;
import org.openlowcode.server.data.properties.DataObjectId;
import org.openlowcode.server.data.storage.QueryFilter;
import org.openlowcode.server.data.storage.TableAlias;
import org.openlowcode.server.graphic.SPage;
import org.openlowcode.server.runtime.OLcServer;
import org.openlowcode.server.runtime.SModule;
/**
 * action to show the current user data
 * 
 * @author <a href="https://openlowcode.com/" rel="nofollow">Open Lowcode
 *         SAS</a>
 *
 */
public class ShowuserstatAction
		extends
		AbsShowuserstatAction {
	private SModule localparent;
	/**
	 * Creates the action
	 * 
	 * @param parent parent module
	 */
	public ShowuserstatAction(SModule parent) {
		super(parent);
		this.localparent = parent;
	}

	@Override
	public ActionOutputData executeActionLogic(Function<TableAlias, QueryFilter> datafilter) {
		DataObjectId<Appuser> userid = OLcServer.getServer().getCurrentUserId();
		return new ActionOutputData(userid);
	}

	@Override
	public SPage choosePage(ActionOutputData outputdata) {
		// call action show user
		AtgShowappuserAction showuseraction = new AtgShowappuserAction(localparent);
		// sends page
		return showuseraction.choosePage(showuseraction.executeActionLogic(outputdata.getUserid(), null));

	}

}
