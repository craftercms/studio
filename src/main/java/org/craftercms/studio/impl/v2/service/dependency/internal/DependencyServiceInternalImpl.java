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

package org.craftercms.studio.impl.v2.service.dependency.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.api.v2.dal.DependencyDAO;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.EDITED_STATES_PARAM;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.PATHS_PARAM;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.REGEX_PARAM;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.SITE_PARAM;

public class DependencyServiceInternalImpl implements DependencyServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(DependencyServiceInternalImpl.class);

    private SiteService siteService;
    private StudioConfiguration studioConfiguration;
    private DependencyDAO dependencyDao;

    @Override
    public List<String> getSoftDependencies(String site, String path) throws SiteNotFoundException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException(site);
        }
        logger.debug("Get soft dependencies for site: " + site + " path:" + path);
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        return getSoftDependencies(site, paths);
    }

    @Override
    public List<String> getSoftDependencies(String site, List<String> paths) throws SiteNotFoundException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException(site);
        }
        Set<String> toRet = new HashSet<String>();
        Set<String> pathsParams = new HashSet<String>();

        logger.debug("Get all soft dependencies");
        pathsParams.addAll(paths);
        boolean exitCondition = false;
        do {
            List<String> deps = getSoftDependenciesForListFromDB(site, pathsParams);
            exitCondition = !toRet.addAll(deps);
            pathsParams.clear();
            pathsParams.addAll(deps);
        } while (!exitCondition);

        return new ArrayList<>(toRet);
    }

    private List<String> getSoftDependenciesForListFromDB(String site, Set<String> paths) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        params.put(REGEX_PARAM, getItemSpecificDependenciesPatterns());
        Collection<State> onlyEditStates = CollectionUtils.removeAll(State.CHANGE_SET_STATES, State.NEW_STATES);
        params.put(EDITED_STATES_PARAM, onlyEditStates);
        return dependencyDao.getSoftDependenciesForList(params);
    }

    protected List<String> getItemSpecificDependenciesPatterns() {
        StringTokenizer st = new StringTokenizer(
                studioConfiguration.getProperty(CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS), ",");
        List<String> itemSpecificDependenciesPatterns = new ArrayList<String>(st.countTokens());
        while (st.hasMoreTokens()) {
            itemSpecificDependenciesPatterns.add(st.nextToken().trim());
        }
        return itemSpecificDependenciesPatterns;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public DependencyDAO getDependencyDao() {
        return dependencyDao;
    }

    public void setDependencyDao(DependencyDAO dependencyDao) {
        this.dependencyDao = dependencyDao;
    }
}
