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
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.to.ResultTO;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

public class ExtractDependencyProcessor extends PathMatchProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExtractDependencyProcessor.class);

    public static final String NAME = "ExtractDependencyProcessor";

    protected DependencyService dependencyService;

    /**
     * default constructor
     */
    public ExtractDependencyProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public ExtractDependencyProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        String site = content.getProperty(DmConstants.KEY_SITE);
        String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String path = (folderPath.endsWith(FILE_SEPARATOR)) ? folderPath + fileName : folderPath + FILE_SEPARATOR + fileName;
        try {
            dependencyService.upsertDependencies(site, path);
        } catch (ServiceLayerException e) {
            throw new ContentProcessException(e);
        }
    }

    public DependencyService getDependencyService() { return dependencyService; }
    public void setDependencyService(DependencyService dependencyService) { this.dependencyService = dependencyService; }
}
