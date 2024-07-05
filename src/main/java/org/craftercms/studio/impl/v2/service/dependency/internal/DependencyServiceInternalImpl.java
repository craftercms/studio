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
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver.ResolvedDependency;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.Dependency;
import org.craftercms.studio.api.v2.dal.DependencyDAO;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.model.rest.content.DependencyItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.craftercms.studio.api.v2.dal.DependencyDAO.TARGET_PATH_COLUMN_NAME;
import static org.craftercms.studio.api.v2.dal.ItemState.MODIFIED_MASK;
import static org.craftercms.studio.api.v2.dal.ItemState.NEW_MASK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS;

public class DependencyServiceInternalImpl implements DependencyService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyServiceInternalImpl.class);
    private static final String UPSERT_DEPENDENCIES_LOCK = ":upsertDependencies";

    private StudioConfiguration studioConfiguration;
    private DependencyDAO dependencyDao;
    private ItemServiceInternal itemServiceInternal;
    private DependencyResolver dependencyResolver;
    private ServicesConfig servicesConfig;
    private GeneralLockService generalLockService;
    private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    @LogExecutionTime
    public Collection<String> getSoftDependencies(String site, Collection<String> paths) {
        logger.trace("Get all soft dependencies for site '{}' paths '{}'", site, paths);
        Set<String> pathsParams = new HashSet<>(paths);
        Set<String> result = new HashSet<>();
        List<Map<String, String>> deps = dependencyDao.getSoftDependenciesForList(site, pathsParams, getItemSpecificDependenciesPatterns(),
                MODIFIED_MASK, NEW_MASK);
        for (Map<String, String> d : deps) {
            String targetPath = d.get(TARGET_PATH_COLUMN_NAME);
            if (!pathsParams.contains(targetPath)) {
                result.add(targetPath);
            }
        }
        return result;
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
    @RequireSiteExists
    public Collection<String> getHardDependencies(@SiteId String site, String publishingTarget, Collection<String> paths) {
        boolean isLiveTarget = StringUtils.equals(servicesConfig.getLiveEnvironment(site), publishingTarget);
        return dependencyDao.getHardDependenciesForList(site, publishingTarget, paths,
                getItemSpecificDependenciesPatterns(), isLiveTarget);
    }

    @Override
    public Collection<String> getHardDependencies(String site, Collection<String> paths) {
        String liveTarget = servicesConfig.getLiveEnvironment(site);
        // Default to live target for backwards compatibility
        return getHardDependencies(site, liveTarget, paths);
    }

    @Override
    public List<String> getDependentPaths(String siteId, List<String> paths) {
        if (CollectionUtils.isEmpty(paths)) {
            return new ArrayList<>();
        }
        List<String> result = dependencyDao.getDependentItems(siteId, paths);
        return result.stream().distinct().collect(Collectors.toList());
    }

    @Override
    public List<DependencyItem> getDependentItems(String siteId, String path) {
        List<String> dependentPaths = dependencyDao.getDependentItems(siteId, Collections.singletonList(path))
                .stream().distinct().toList();

        return dependentPaths.stream()
                .map(dep -> DependencyItem.getInstance(itemServiceInternal.getItem(siteId, dep)))
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getItemSpecificDependencies(String siteId, List<String> paths) {
        if (isNotEmpty(paths)) {
            return dependencyDao.getItemSpecificDependencies(siteId, paths, getItemSpecificDependenciesPatterns());
        } else {
            return new ArrayList<>();
        }
    }

    @Override
    @LogExecutionTime
    public Map<String, Set<ResolvedDependency>> resolveDependencies(String siteId, String path) {
        Map<String, Set<ResolvedDependency>> dependencies = null;
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
    @Transactional
    public void upsertDependencies(String site, String path) throws ServiceLayerException {
        Map<String, Set<ResolvedDependency>> resolveDependencies = dependencyResolver.resolve(site, path);
        List<Dependency> dependencies = new LinkedList<>();
        for (Map.Entry<String, Set<ResolvedDependency>> entry : resolveDependencies.entrySet()) {
            dependencies.addAll(
                    entry.getValue().stream()
                            .map(dep -> {
                                Dependency dependency = new Dependency();
                                dependency.setSite(site);
                                // Remove multiple slashes
                                dependency.setSourcePath(Path.of(path).toString());
                                dependency.setTargetPath(Path.of(dep.path()).toString());
                                dependency.setType(entry.getKey());
                                dependency.setValid(dep.valid());
                                return dependency;
                            }).toList());
        }
        String lock = site + UPSERT_DEPENDENCIES_LOCK;
        generalLockService.lock(lock);
        try {
            logger.debug("Upserting dependencies for site '{}' path '{}'", site, path);
            retryingDatabaseOperationFacade.retry(() -> dependencyDao.deleteItemDependencies(site, path));
            if (isNotEmpty(dependencies)) {
                retryingDatabaseOperationFacade.retry(() -> dependencyDao.insertItemDependencies(dependencies));
            }
        } catch (Exception e) {
            logger.error("Failed to upsert dependencies for site '{}' path '{}'", site, path, e);
            throw new ServiceLayerException(format("Failed to upsert dependencies for site '%s' path '%s'",
                    site, path), e);
        } finally {
            generalLockService.unlock(lock);
        }
    }

    @Override
    public void deleteItemDependencies(String site, String sourcePath) throws ServiceLayerException {
        try {
            retryingDatabaseOperationFacade.retry(() -> dependencyDao.deleteItemDependencies(site, sourcePath));
        } catch (Exception e) {
            logger.error("Failed to delete dependencies for site '{}' path '{}'", site, sourcePath, e);
            throw new ServiceLayerException(format("Failed to delete dependencies for site '%s' path '%s'",
                    site, sourcePath), e);
        }
    }

    @Override
    public void invalidateDependencies(String siteId, String targetPath) throws ServiceLayerException {
        try {
            retryingDatabaseOperationFacade.retry(() -> dependencyDao.invalidateDependencies(siteId, targetPath));
        } catch (Exception e) {
            logger.error("Failed to invalidate dependencies for site '{}' path '{}'", siteId, targetPath, e);
            throw new ServiceLayerException(format("Failed to invalidate dependencies for site '%s' path '%s'",
                    siteId, targetPath), e);
        }
    }

    @Override
    public void validateDependencies(String siteId, String targetPath) throws ServiceLayerException {
        try {
            retryingDatabaseOperationFacade.retry(() -> dependencyDao.validateDependencies(siteId, targetPath));
        } catch (Exception e) {
            logger.error("Failed to validate dependencies for site '{}' path '{}'", siteId, targetPath, e);
            throw new ServiceLayerException(format("Failed to validate dependencies for site '%s' path '%s'",
                    siteId, targetPath), e);
        }
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setDependencyDao(DependencyDAO dependencyDao) {
        this.dependencyDao = dependencyDao;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setDependencyResolver(DependencyResolver dependencyResolver) {
        this.dependencyResolver = dependencyResolver;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
