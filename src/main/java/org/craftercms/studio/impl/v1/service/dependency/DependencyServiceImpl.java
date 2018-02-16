/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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

package org.craftercms.studio.impl.v1.service.dependency;

import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DependencyServiceImpl implements DependencyService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyServiceImpl.class);

    @Autowired
    protected DependencyMapper dependencyMapper;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;
    protected ContentService contentService;

    @Override
    public Set<String> upsertDependencies(String site, String path) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    @Override
    public Set<String> upsertDependencies(String site, List<String> paths) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    private void deleteAllSourceDependencies(String site, String path) {
        logger.debug("Delete all source dependencies for site: " + site + " path: " + path);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", path);
        dependencyMapper.deleteAllSourceDependencies(params);
    }

    @Override
    public Set<String> getPublishingDepenencies(String site, String path) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    @Override
    public Set<String> getPublishingDepenencies(String site, List<String> paths) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    @Override
    public Set<String> getItemSpecificDependencies(String site, String path, int depth) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    @Override
    public Set<String> getItemDependencies(String site, String path, int depth) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    @Override
    public Set<String> getItemsDependingOn(String site, String path, int depth) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    @Override
    public Set<String> moveDependencies(String site, String oldPath, String newPath) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    @Override
    public void deleteItemDependencies(String site, String path) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        logger.debug("Delete dependencies for content site: " + site + " path: " + path);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", path);
        dependencyMapper.deleteDependenciesForSiteAndPath(params);
    }

    @Override
    public void deleteSiteDependencies(String site) throws SiteNotFoundException, ServiceException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        logger.debug("Delete all dependencies for site: " + site);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        dependencyMapper.deleteDependenciesForSite(params);
    }

    @Override
    public Set<String> getDeleteDependencies(String site, String path) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    @Override
    public Set<String> getDeleteDependencies(String site, List<String> paths) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        return null;
    }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }
}
