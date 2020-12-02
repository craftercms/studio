/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.dependency.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.dal.ItemStateMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.DependencyDAO;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.SORUCE_PATH_COLUMN_NAME;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.TARGET_PATH_COLUMN_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS;

public class DependencyServiceInternalImpl implements DependencyServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(DependencyServiceInternalImpl.class);

    private SiteService siteService;
    private StudioConfiguration studioConfiguration;
    private DependencyDAO dependencyDao;
    private ItemStateMapper itemStateMapper;

    @Override
    public List<String> getSoftDependencies(String site, String path) throws ServiceLayerException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException(site);
        }
        logger.debug("Get soft dependencies for site: " + site + " path:" + path);
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        return getSoftDependencies(site, paths);
    }

    @Override
    public List<String> getSoftDependencies(String site, List<String> paths) throws ServiceLayerException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException(site);
        }
        Map<String, String> toRet = calculateSoftDependencies(site, paths);

        return new ArrayList<String>(toRet.keySet());
    }

    private Map<String, String> calculateSoftDependencies(String site, List<String> paths) {
        Set<String> toRet = new HashSet<String>();
        Set<String> pathsParams = new HashSet<String>();

        logger.debug("Get all soft dependencies");
        pathsParams.addAll(paths);
        boolean exitCondition = false;
        Map<String, String> softDeps = new HashMap<String, String>();
        for (String p : paths) {
            softDeps.put(p, p);
        }
        do {
            List<Map<String, String>> deps = getSoftDependenciesForListFromDB(site, pathsParams);
            List<String> targetPaths = new ArrayList<String>();
            for (Map<String, String> d : deps) {
                String srcPath = d.get(SORUCE_PATH_COLUMN_NAME);
                String targetPath = d.get(TARGET_PATH_COLUMN_NAME);
                if (!softDeps.keySet().contains(targetPath) && !StringUtils.equals(targetPath, softDeps.get(srcPath))) {
                    softDeps.put(targetPath, softDeps.get(srcPath));
                }
                targetPaths.add(targetPath);
            }
            exitCondition = !toRet.addAll(targetPaths);
            pathsParams.clear();
            pathsParams.addAll(targetPaths);
        } while (!exitCondition);

        return softDeps;
    }

    private List<Map<String, String>> getSoftDependenciesForListFromDB(String site, Set<String> paths) {
        Collection<State> onlyEditStates = CollectionUtils.removeAll(State.CHANGE_SET_STATES, State.NEW_STATES);
        return dependencyDao.getSoftDependenciesForList(site, paths, getItemSpecificDependenciesPatterns(),
                onlyEditStates);
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

    @Override
    public List<String> getHardDependencies(String site, String path) throws ServiceLayerException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException(site);
        }
        logger.debug("Get hard dependencies for site: " + site + " path:" + path);
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        return getHardDependencies(site, paths);
    }

    @Override
    public List<String> getHardDependencies(String site, List<String> paths) throws ServiceLayerException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException(site);
        }
        Map<String, String> dependencies = calculateHardDependencies(site, paths);
        return new ArrayList<>(dependencies.keySet());
    }

    private Map<String, String> calculateHardDependencies(String site, List<String> paths) {
        Set<String> toRet = new HashSet<String>();
        Set<String> pathsParams = new HashSet<String>();

        logger.debug("Get all hard dependencies");
        pathsParams.addAll(paths);
        Set<String> mandatoryParents = getMandatoryParents(site, paths);
        boolean exitCondition = false;
        Map<String, String> ancestors = new HashMap<String, String>();
        for (String p : mandatoryParents) {
            String prefix = p.replace(FILE_SEPARATOR + INDEX_FILE, "");
            for (String p2 : paths) {
                if (p2.startsWith(prefix)) {
                    ancestors.put(p, p2);
                    break;
                }
            }
        }
        do {
            List<Map<String, String>> deps = calculateHardDependenciesForListFromDB(site, pathsParams);
            List<String> targetPaths = new ArrayList<String>();
            for (Map<String, String> d : deps) {
                String srcPath = d.get(SORUCE_PATH_COLUMN_NAME);
                String targetPath = d.get(TARGET_PATH_COLUMN_NAME);
                if (!ancestors.keySet().contains(targetPath) &&
                        !StringUtils.equals(targetPath, ancestors.get(srcPath))) {
                    ancestors.put(targetPath, ancestors.get(srcPath));
                }
                targetPaths.add(targetPath);
            }
            exitCondition = !toRet.addAll(targetPaths);
            pathsParams.clear();
            pathsParams.addAll(targetPaths);
        } while (!exitCondition);

        return ancestors;
    }

    private Set<String> getMandatoryParents(String site, List<String> paths) {
        Set<String> toRet = new HashSet<String>();
        Set<String> possibleParents = calculatePossibleParents(paths);
        if (!possibleParents.isEmpty()) {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put(ItemStateMapper.SITE_PARAM, site);
            params.put(ItemStateMapper.POSSIBLE_PARENTS_PARAM, possibleParents);
            Collection<State> onlyEditStates = CollectionUtils.removeAll(State.CHANGE_SET_STATES, State.NEW_STATES);
            params.put(ItemStateMapper.EDITED_STATES_PARAM, onlyEditStates);
            params.put(ItemStateMapper.NEW_STATES_PARAM, State.NEW_STATES);
            List<String> result = itemStateMapper.getMandatoryParentsForPublishing(params);
            toRet.addAll(result);
        }
        return toRet;
    }

    private Set<String> calculatePossibleParents(List<String> paths) {
        Set<String> possibleParents = new HashSet<String>();
        for (String path : paths) {
            StringTokenizer stPath =
                    new StringTokenizer(path.replace(FILE_SEPARATOR + INDEX_FILE, ""), FILE_SEPARATOR);
            StringBuilder candidate = new StringBuilder(FILE_SEPARATOR);
            if (stPath.countTokens() > 0) {
                do {
                    String token = stPath.nextToken();
                    if (stPath.hasMoreTokens()) {
                        candidate.append(token).append(FILE_SEPARATOR);
                        possibleParents.add(candidate.toString() + INDEX_FILE);
                    }
                } while (stPath.hasMoreTokens());
            }
        }
        return possibleParents;
    }

    private List<Map<String, String>> calculateHardDependenciesForListFromDB(String site, Set<String> paths) {
        Collection<State> onlyEditStates = CollectionUtils.removeAll(State.CHANGE_SET_STATES, State.NEW_STATES);
        return dependencyDao.getHardDependenciesForList(site, paths, getItemSpecificDependenciesPatterns(),
                onlyEditStates, State.NEW_STATES);
    }

    @Override
    public List<String> getDependentItems(String siteId, String path) {
        List<String> paths = new ArrayList<String>(1);
        paths.add(path);
        return getDependentItems(siteId, paths);
    }

    @Override
    public List<String> getDependentItems(String siteId, List<String> paths) {
        if (CollectionUtils.isEmpty(paths)) {
            return new ArrayList<String>();
        }
        return dependencyDao.getDependentItems(siteId, paths);
    }

    @Override
    public List<String> getItemSpecificDependencies(String siteId, String path) {
        List<String> paths = new ArrayList<>(1);
        paths.add(path);
        return getItemSpecificDependencies(siteId, paths);
    }

    @Override
    public List<String> getItemSpecificDependencies(String siteId, List<String> paths) {
        if (CollectionUtils.isNotEmpty(paths)) {
            return dependencyDao.getItemSpecificDependencies(siteId, paths, getItemSpecificDependenciesPatterns());
        } else {
            return new ArrayList<String>();
        }
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

    public ItemStateMapper getItemStateMapper() {
        return itemStateMapper;
    }

    public void setItemStateMapper(ItemStateMapper itemStateMapper) {
        this.itemStateMapper = itemStateMapper;
    }
}
