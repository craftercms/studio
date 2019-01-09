/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v1.content.pipeline;

import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.to.ResultTO;

public class DmWorkflowProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(DmWorkflowProcessor.class);

    public static final String NAME = "DmWorkflowProcessor";


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
