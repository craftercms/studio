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
package org.craftercms.cstudio.alfresco.dm.content.pipeline.impl;

import org.craftercms.cstudio.alfresco.cache.Scope;
import org.craftercms.cstudio.alfresco.cache.cstudioCacheManager;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Invalidate cache of the content being processed
 * TODO: currently this is only being used for assets. should refactor the code to use for form contents as well
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class InvalidateCacheProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(InvalidateCacheProcessor.class);

    public static final String NAME = "InvalidateCacheProcessor";

    protected cstudioCacheManager _cache;

    protected DmContentService _dmContentService;

    public cstudioCacheManager getCache() {
        return _cache;
    }

    public void setCache(cstudioCacheManager cache) {
        this._cache = cache;
    }

    /**
     * @return the dmContentService
     */
    public DmContentService getDmContentService() {
        return _dmContentService;
    }

    /**
     * @param dmContentService the dmContentService to set
     */
    public void setDmContentService(DmContentService dmContentService) {
        this._dmContentService = dmContentService;
    }

    /**
     * default constructor
     */
    public InvalidateCacheProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public InvalidateCacheProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        result.setInvalidateCache(true);
        /** Disabled **/
        /*
        String site = content.getProperty(DmConstants.KEY_SITE);
        String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String path = (folderPath.endsWith("/")) ? folderPath + fileName : folderPath + "/" + fileName;
        String fullPath = _dmContentService.getContentFullPath(site, path);
        _cache.invalidate(Scope.DM_CONTENT_ITEM, fullPath);
        */
    }
}
