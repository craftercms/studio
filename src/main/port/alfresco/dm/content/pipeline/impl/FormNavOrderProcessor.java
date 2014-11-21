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
import org.craftercms.cstudio.alfresco.dm.service.api.DmPageNavigationOrderService;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FormNavOrderProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FormNavOrderProcessor.class);

    public static final String NAME = "FormNavOrderProcessor";

    protected DmPageNavigationOrderService _pageNavOrderService;
    public DmPageNavigationOrderService getPageNavOrderService() {
        return this._pageNavOrderService;
    }
    public void setPageNavOrderService (DmPageNavigationOrderService pageNavigationOrderService) {
        this._pageNavOrderService = pageNavigationOrderService;
    }

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
            _pageNavOrderService.addNavOrder(site, path, content.getDocument());
        }else{
            _pageNavOrderService.updateNavOrder(site, path, content.getDocument());
        }
    }
}
