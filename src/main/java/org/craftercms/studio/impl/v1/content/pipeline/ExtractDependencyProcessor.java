/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.dom4j.Document;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        } catch (ServiceException e) {
            throw new ContentProcessException(e);
        }
    }

    public DependencyService getDependencyService() { return dependencyService; }
    public void setDependencyService(DependencyService dependencyService) { this.dependencyService = dependencyService; }
}
