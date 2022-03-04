/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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
import org.craftercms.studio.api.v1.to.ResultTO;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;

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
        boolean edit = (OPERATION_CREATE.equals(type)) ? false : true;
    }
}
