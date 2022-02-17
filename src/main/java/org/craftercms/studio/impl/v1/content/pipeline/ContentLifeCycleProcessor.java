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

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.DmContentLifeCycleService;
import org.craftercms.studio.api.v1.to.ResultTO;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;

/**
 * 
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class ContentLifeCycleProcessor extends PathMatchProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ContentLifeCycleProcessor.class);


    @Override
    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
    	String preview = content.getProperty(DmConstants.KEY_IS_PREVIEW);
    	// do not run on preview write
    	if (StringUtils.isEmpty(preview) || !preview.equalsIgnoreCase("true")) {
			String site = content.getProperty(DmConstants.KEY_SITE);
			String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
			String contentType = content.getProperty(DmConstants.KEY_CONTENT_TYPE);
			String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
			String path = (folderPath.endsWith(FILE_SEPARATOR)) ? folderPath + fileName : folderPath + FILE_SEPARATOR + fileName;
			String user = content.getProperty(DmConstants.KEY_USER);
			String operValue = content.getProperty(DmConstants.CONTENT_LIFECYCLE_OPERATION);
			DmContentLifeCycleService.ContentLifeCycleOperation operation = (DmContentLifeCycleService.ContentLifeCycleOperation.getOperation(operValue));
			if (operation == null) {
				String type = content.getProperty(DmConstants.KEY_ACTIVITY_TYPE);
				operation = (OPERATION_CREATE.equals(type)) ? DmContentLifeCycleService.ContentLifeCycleOperation.NEW :
                        DmContentLifeCycleService.ContentLifeCycleOperation.UPDATE;
			}
	    	dmContentLifeCycleService.process(site, user, path, contentType, operation, null);
    	}
    }

	public DmContentLifeCycleService getDmContentLifeCycleService() {return dmContentLifeCycleService; }
	public void setDmContentLifeCycleService(DmContentLifeCycleService dmContentLifeCycleService) { this.dmContentLifeCycleService = dmContentLifeCycleService; }

	protected DmContentLifeCycleService dmContentLifeCycleService;
}
