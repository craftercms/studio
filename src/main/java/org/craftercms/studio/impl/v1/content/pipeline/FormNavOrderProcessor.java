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
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.to.ResultTO;

public class FormNavOrderProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FormNavOrderProcessor.class);

    public static final String NAME = "FormNavOrderProcessor";



    /**
     * default constructor
     */
    public FormNavOrderProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public FormNavOrderProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        boolean copiedContent = Boolean.valueOf(content.getProperty(DmConstants.KEY_COPIED_CONTENT));
        String site = String.valueOf(content.getProperty(DmConstants.KEY_SITE));
        String path = String.valueOf(content.getProperty(DmConstants.KEY_PATH));
        if(copiedContent){
            pageNavOrderService.addNavOrder(site, path, content.getDocument());
        }else{
            pageNavOrderService.updateNavOrder(site, path, content.getDocument());
        }
    }


    public DmPageNavigationOrderService getPageNavOrderService() {
        return this.pageNavOrderService;
    }
    public void setPageNavOrderService (DmPageNavigationOrderService pageNavigationOrderService) {
        this.pageNavOrderService = pageNavigationOrderService;
    }

    protected DmPageNavigationOrderService pageNavOrderService;
}
