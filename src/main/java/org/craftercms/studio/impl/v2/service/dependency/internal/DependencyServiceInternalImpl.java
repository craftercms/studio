/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.Dependency;
import org.craftercms.studio.api.v2.dal.DependencyDAO;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.SOURCE_PATH_COLUMN_NAME;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.TARGET_PATH_COLUMN_NAME;
import static org.craftercms.studio.api.v2.dal.ItemState.MODIFIED_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.NEW_MASK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS;

public class DependencyServiceInternalImpl implements DependencyServiceInternal {

    private static final Logger logger = LoggerFactory.getLogger(DependencyServiceInternalImpl.class);

    private SiteService siteService;
    private StudioConfiguration studioConfiguration;
    private DependencyDAO dependencyDao;
    private ItemServiceInternal itemServiceInternal;
    private DependencyResolver dependencyResolver;
    private ServicesConfig servicesConfig;

    @Override
    public List<String> getSoftDependencies(String site, List<String> paths)
            throws ServiceLayerException {
        Map<String, String> toRet = calculateSoftDependencies(site, paths);
        return new ArrayList<>(toRet.keySet());
    }

    private Map<String, String> calculateSoftDependencies(String site, List<String> paths) {
        Set<String> toRet = new HashSet<>();

        logger.trace("Get all soft dependencies for site '{}' paths '{}'", site, paths);
        Set<String> pathsParams = new HashSet<>(paths);
        boolean exitCondition = false;
        Map<String, String> softDeps = new HashMap<>();
        for (String p : paths) {
            softDeps.put(p, p);
        }
        do {
            List<Map<String, String>> deps = getSoftDependenciesForListFromDB(site, pathsParams);
            List<String> targetPaths = new ArrayList<>();
            for (Map<String, String> d : deps) {
                String srcPath = d.get(SOURCE_PATH_COLUMN_NAME);
                String targetPath = d.get(TARGET_PATH_COLUMN_NAME);
                if (!softDeps.containsKey(targetPath) && !StringUtils.equals(targetPath, softDeps.get(srcPath))) {
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
        return dependencyDao.getSoftDependenciesForList(site, paths, getItemSpecificDependenciesPatterns(),
                MODIFIED_MASK, NEW_MASK);
    }

    protected List<String> getItemSpecificDependenciesPatterns() {
        StringTokenizer st = new StringTokenizer(
                studioConfiguration.getProperty(CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS), ",");
        List<String> itemSpecificDependenciesPatterns = new ArrayList<>(st.countTokens());
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
        logger.trace("Get hard dependencies for site '{}' path '{}'", site, path);
        List<String> paths = new ArrayList<>();
        paths.add(path);
        return getHardDependencies(site, paths);
    }

    @Override
    public List<String> getHardDependencies(String site, List<String> paths)
            throws ServiceLayerException {
        siteService.checkSiteExists(site);
        Map<String, String> dependencies = calculateHardDependencies(site, paths);
        return new ArrayList<>(dependencies.keySet());
    }

    private Map<String, String> calculateHardDependencies(String site, List<String> paths) {
        Set<String> toRet = new HashSet<>();

        logger.trace("Get all hard dependencies for site '{}' paths '{}'", site, paths);
        Set<String> pathsParams = new HashSet<>(paths);
        List<String> mandatoryParents = itemServiceInternal.getMandatoryParentsForPublishing(site, paths);
        List<String> mpAsList = new ArrayList<>(mandatoryParents);
        Map<String, String> ancestors = new HashMap<>();
        if (CollectionUtils.isNotEmpty(mandatoryParents)) {
            pathsParams.addAll(mandatoryParents);
            Set<String> existingRenamedChildrenOfMandatoryParents =
                    getExistingRenamedChildrenOfMandatoryParents(site, mpAsList);
            for (String p3: existingRenamedChildrenOfMandatoryParents) {
                    ancestors.put(p3, p3);
            }
            pathsParams.addAll(existingRenamedChildrenOfMandatoryParents);
        }
        boolean exitCondition = false;

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
            List<String> targetPaths = new ArrayList<>();
            for (Map<String, String> d : deps) {
                String srcPath = d.get(SOURCE_PATH_COLUMN_NAME);
                String targetPath = d.get(TARGET_PATH_COLUMN_NAME);
                if (!ancestors.containsKey(targetPath) &&
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

    private Set<String> getExistingRenamedChildrenOfMandatoryParents(String site, List<String> paths) {
        Set<String> toRet = new HashSet<>(itemServiceInternal.getExistingRenamedChildrenOfMandatoryParentsForPublishing(site, paths));
        return toRet;
    }

    private List<Map<String, String>> calculateHardDependenciesForListFromDB(String site, Set<String> paths) {
        return dependencyDao.getHardDependenciesForList(site, paths, getItemSpecificDependenciesPatterns(),
                MODIFIED_MASK, NEW_MASK);
    }

    @Override
    public List<String> getDependentItems(String siteId, List<String> paths) {
        if (CollectionUtils.isEmpty(paths)) {
            return new ArrayList<>();
        }
        List<String> result = dependencyDao.getDependentItems(siteId, paths);
        return result.stream().distinct().collect(Collectors.toList());
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
            return new ArrayList<>();
        }
    }

    @Override
    public Map<String, Set<String>> resolveDependencies(String siteId, String path) {
        Map<String, Set<String>> dependencies = null;
        boolean isXml = path.endsWith(DmConstants.XML_PATTERN);
        boolean isCss = path.endsWith(DmConstants.CSS_PATTERN);
        boolean isJs = path.endsWith(DmConstants.JS_PATTERN);
        boolean isTemplate = ContentUtils.matchesPatterns(path, servicesConfig.getRenderingTemplatePatterns(siteId));
        if (isXml || isCss || isJs || isTemplate) {
            dependencies = dependencyResolver.resolve(siteId, path);
        }
        return dependencies;
    }

    @Override
    public List<Dependency> getDependenciesByType(String siteId, String path, String dependencyType) {
        return dependencyDao.getDependenciesByType(siteId, path, dependencyType);
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

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public DependencyResolver getDependencyResolver() {
        return dependencyResolver;
    }

    public void setDependencyResolver(DependencyResolver dependencyResolver) {
        this.dependencyResolver = dependencyResolver;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }
}
