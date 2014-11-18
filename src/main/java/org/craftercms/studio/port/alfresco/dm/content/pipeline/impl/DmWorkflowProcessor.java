/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.content.pipeline.impl;

import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmWorkflowService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.ActivityService;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by IntelliJ IDEA.
 * User: dejan
 * Date: 12/30/11
 * Time: 9:31 AM
 * To change this template use File | Settings | File Templates.
 */
public class DmWorkflowProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DmWorkflowProcessor.class);

    public static final String NAME = "DmWorkflowProcessor";

    protected ServicesManager servicesManager;
    public ServicesManager getServicesManager() {
        return servicesManager;
    }
    public void setServicesManager(ServicesManager servicesManager) {
        this.servicesManager = servicesManager;
    }

    /**
     * default constructor
     */
    public DmWorkflowProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public DmWorkflowProcessor(String name) {
        super(name);
    }

    public boolean isProcessable(PipelineContent content) {
        return true;
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        String type = (String) content.getProperty(DmConstants.KEY_ACTIVITY_TYPE);
        boolean edit = (ActivityService.ActivityType.CREATED.toString().equals(type)) ? false : true;
        /*if (edit) {
              String path = (String) content.getProperty(WcmConstants.KEY_PATH);
              String site = (String) content.getProperty(WcmConstants.KEY_SITE);
              _wcmWorkflowService.updateWorkflowSandboxes(site, path);
          }*/

    }
}
