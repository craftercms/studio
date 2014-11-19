/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.cstudio.alfresco.action;

import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.craftercms.cstudio.alfresco.service.api.SiteService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReloadSiteServiceConfigActionExecutor extends ActionExecuterAbstractBase {

    protected Logger logger = LoggerFactory.getLogger(ReloadSiteServiceConfigActionExecutor.class);

    public static final String NAME = "reload-site-service-config";
    //public static final String PARAM_ASPECT_NAME = "aspect-name";

    protected SiteService _siteService;
    public SiteService getSiteService() {
        return _siteService;
    }
    public void setSiteService(SiteService siteService) {
        this._siteService = siteService;
    }


    protected void executeImpl(Action action, NodeRef actionedUponNodeRef) {
        this._siteService.reloadSiteConfigurations();
    }


    protected void addParameterDefinitions(List<ParameterDefinition> paramList) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
