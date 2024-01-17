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

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.CalculateDependenciesEntityTO;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DeleteDependencyConfigTO;
import org.craftercms.studio.api.v2.dal.ItemDAO;
import org.craftercms.studio.api.v2.dal.ItemState;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static java.lang.String.format;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.*;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.MODIFIED_MASK;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.NEW_MASK;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS;

public class DependencyServiceImpl implements DependencyService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyServiceImpl.class);

    protected DependencyMapper dependencyMapper;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;
    protected ContentService contentService;
    protected ContentRepository contentRepository;
    protected ServicesConfig servicesConfig;
    protected org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal dependencyService;
    protected ItemDAO itemDao;
    protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

    @Override
    public List<String> getPublishingDependencies(String site, String path)
            throws ServiceLayerException {
        logger.debug("Get publishing dependencies for site '{}' path '{}'", site, path);
        List<String> paths = new ArrayList<>();
        paths.add(path);
        return getPublishingDependencies(site, paths);
    }

    @Override
    public List<String> getPublishingDependencies(String site, List<String> paths)
            throws ServiceLayerException {
        return dependencyService.getHardDependencies(site, paths);
    }

    @Override
    public Set<String> getItemSpecificDependencies(String site, String path, int depth)
            throws ServiceLayerException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        Set<String> toRet = new HashSet<>();
        Set<String> paths = new HashSet<>();
        boolean exitCondition = false;
        paths.add(path);
        if (depth < 0) {
            do {
                List<String> deps = getItemSpecificDependenciesFromDB(site, paths);
                exitCondition = !toRet.addAll(deps);
                paths.clear();
                paths.addAll(deps);
            } while (!exitCondition);
        } else {
            int d = depth;
            while (d-- > 0) {
                List<String> deps = getItemSpecificDependenciesFromDB(site, paths);
                exitCondition = !toRet.addAll(deps);
                paths.clear();
                paths.addAll(deps);
                if (exitCondition) break;
            }
        }
        return toRet;
    }

    private List<String> getItemSpecificDependenciesFromDB(String site, Set<String> paths) {
        if (CollectionUtils.isEmpty(paths)) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        params.put(REGEX_PARAM, getItemSpecificDependenciesPatterns());
        return dependencyMapper.getItemSpecificDependenciesForList(params);
    }

    @Override
    public Set<String> getItemDependencies(String site, String path, int depth)
            throws ServiceLayerException {
        // Check if site exists
        siteService.checkSiteExists(site);
        // Check if content exists
        contentService.checkContentExists(site, path);

        logger.debug("Get item dependencies for site '{}' path '{}'", site, path);

        Set<String> toRet = new HashSet<>();
        Set<String> paths = new HashSet<>();
        paths.add(path);
        boolean exitCondition;
        if (depth < 0) {
            do {
                List<String> deps = getItemDependenciesFromDB(site, paths);
                exitCondition = !toRet.addAll(deps);
                paths.clear();
                paths.addAll(deps);
            } while (!exitCondition);
        } else {
            int d = depth;
            while (d-- > 0) {
                List<String> deps = getItemDependenciesFromDB(site, paths);
                exitCondition = !toRet.addAll(deps);
                if (exitCondition) break;
            }
        }
        return toRet;
    }

    private List<String> getItemDependenciesFromDB(String site, Set<String> paths) {
        if (CollectionUtils.isEmpty(paths)) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        return dependencyMapper.getDependenciesForList(params);
    }

    @Override
    public Set<String> getItemsDependingOn(String site, String path, int depth)
            throws ServiceLayerException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        contentService.checkContentExists(site, path);

        logger.debug("Get items depending on item site '{}' path '{}'", site, path);
        Set<String> toRet = new HashSet<>();
        Set<String> paths = new HashSet<>();
        paths.add(path);
        if (depth < 0) {
            do {
                List<String> deps = getItemsDependingOnFromDB(site, paths);
                toRet.addAll(deps);
                paths.clear();
                paths.addAll(deps);
            } while (!CollectionUtils.isNotEmpty(paths));
        } else {
            int d = depth;
            while (d-- > 0) {
                List<String> deps = getItemsDependingOnFromDB(site, paths);
                toRet.addAll(deps);
                paths.clear();
                paths.addAll(deps);
            }
        }

        return toRet;
    }

    private List<String> getItemsDependingOnFromDB(String site, Set<String> paths) {
        if (CollectionUtils.isEmpty(paths)) {
            return new ArrayList<>();
        }
        Map<String, Object> params = new HashMap<>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        return dependencyMapper.getItemsDependingOn(params);
    }

    @Override
    public void deleteSiteDependencies(String site) throws ServiceLayerException {
        logger.debug("Delete all dependencies in site '{}'", site);
        Map<String, String> params = new HashMap<>();
        params.put(SITE_PARAM, site);
        retryingDatabaseOperationFacade.retry(() -> dependencyMapper.deleteDependenciesForSite(params));
    }

    @Override
    public Set<String> getDeleteDependencies(String site, String path)
            throws ServiceLayerException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException(format("Site '%s' not found", site));
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException(path, site, format("Content not found in site '%s' at path '%s'",
                    site, path));
        }

        logger.debug("Get delete dependencies for site '{}' path '{}'", site, path);
        List<String> paths = new ArrayList<>();
        paths.add(path);
        Set<String> processedPaths = new HashSet<>();
        return getDeleteDependenciesInternal(site, paths, processedPaths);
    }

    @Override
    public Set<String> getDeleteDependencies(String site, List<String> paths)
            throws ServiceLayerException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException(format("Site '%s' not found", site));
        }
        StringBuilder sbPaths = new StringBuilder();
        for (String path : paths) {
            // Check if content exists
            if (!contentService.contentExists(site, path)) {
                throw new ContentNotFoundException(path, site, format("Content not found in site '%s' at path '%s'",
                        site, path));
            }
            sbPaths.append("\n").append(path);
        }

        logger.debug("Get delete dependencies in site '{}' paths '{}'", site, sbPaths);
        Set<String> processedPaths = new HashSet<>();
        return getDeleteDependenciesInternal(site, paths, processedPaths);
    }

    private Set<String> getDeleteDependenciesInternal(String site, List<String> paths, Set<String> processedPaths) {
        // Step 1: collect all content from subtree
        logger.debug("Get all the children from the subtree in site '{}' paths '{}'", site, paths);
        Set<String> children = getAllChildrenRecursively(site, paths);
        Set<String> toRet = new HashSet<>(children);

        // Step 2: collect all dependencies from DB
        logger.debug("Get the dependencies from the database for all the paths in the subtree(s) and filter them " +
                "by item specificity and content type in site '{}'", site);
        Set<String> depsSource = new HashSet<>();
        depsSource.addAll(paths);
        depsSource.addAll(children);
        Set<String> dependencies = getContentTypeFilteredDeleteDependencies(site, depsSource);
        toRet.addAll(dependencies);
        List<String> itemSpecificcDeps = getItemSpecificDependenciesFromDB(site, depsSource);
        toRet.addAll(itemSpecificcDeps);

        // Step 3: recursion
        logger.debug("Repeat the process for newly collected dependencies that have not been processed yet in site" +
                "'{}'", site);
        boolean doItAgain = processedPaths.addAll(children);
        doItAgain = doItAgain || processedPaths.addAll(dependencies);
        doItAgain = doItAgain || processedPaths.addAll(itemSpecificcDeps);
        if (doItAgain) {
            List<String> pathsToProcess = new ArrayList<>();
            pathsToProcess.addAll(children);
            pathsToProcess.addAll(dependencies);
            toRet.addAll(getDeleteDependenciesInternal(site, pathsToProcess, processedPaths));
        }

        logger.debug("Return the collected set of dependencies in site '{}'", site);
        return toRet;
    }

    private Set<String> getContentTypeFilteredDeleteDependencies(String site, Set<String> paths) {
        Set<String> toRet = new HashSet<>();
        List<String> deps = getItemDependenciesFromDB(site, paths);
        for (String dep : deps) {
            ContentItemTO item = contentService.getContentItem(site, dep, 0);
            List<DeleteDependencyConfigTO> deleteDependencyConfigList =
                    servicesConfig.getDeleteDependencyPatterns(site, item.getContentType());
            if (CollectionUtils.isNotEmpty(deleteDependencyConfigList)) {
                for (DeleteDependencyConfigTO deleteDependencyConfig : deleteDependencyConfigList) {
                    if (dep.matches(deleteDependencyConfig.getPattern())) {
                        toRet.add(dep);
                        break;
                    }
                }
            }

        }
        return toRet;
    }

    private Set<String> getAllChildrenRecursively(String site, List<String> paths) {
        logger.debug("Get all the items from the subtree(s) in site '{}' paths '{}'", site, paths);
        Set<String> toRet = new HashSet<>();

        for (String path : paths) {
            logger.debug("Get the children from the repository for site '{}' path '{}'", site, path);
            RepositoryItem[] children = contentRepository.getContentChildren(site, path);
            if (children != null) {
                List<String> childrenPaths = new ArrayList<>();
                for (RepositoryItem child : children) {
                    String childPath = child.path + "/" + child.name;
                    childrenPaths.add(childPath);
                }
                logger.debug("Add all the collected children paths in site '{}'", site);
                toRet.addAll(childrenPaths);
                toRet.addAll(getAllChildrenRecursively(site, childrenPaths));
            }
        }
        return toRet;
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
    public Map<String, List<CalculateDependenciesEntityTO>> calculateDependencies(String site, List<String> paths)
            throws ServiceLayerException {
        Map<String, List<CalculateDependenciesEntityTO>> toRet =
                new HashMap<>();
        List<CalculateDependenciesEntityTO> entities = new ArrayList<>();
            Map<String, String> deps = calculatePublishingDependencies(site, paths);
            Map<String, List<Map<String, String>>> temp = new HashMap<>();
            for (String p : paths) {
                temp.put(p, new ArrayList<>());
            }
            for (Map.Entry<String, String> d : deps.entrySet()) {
                if (d.getKey() != d.getValue()) {
                    List<Map<String, String>> ds = temp.get(d.getValue());
                    ds.add(new HashMap<>() {{
                        put(StudioConstants.JSON_PROPERTY_ITEM, d.getKey());
                    }});
                }
            }
            for (Map.Entry<String, List<Map<String,String >>> t : temp.entrySet()) {
                CalculateDependenciesEntityTO calculateDependenciesEntityTO = new CalculateDependenciesEntityTO();
                calculateDependenciesEntityTO.setItem(t.getKey());
                calculateDependenciesEntityTO.setDependencies(t.getValue());
                entities.add(calculateDependenciesEntityTO);
            }

        toRet.put("entities", entities);
        return toRet;
    }

    @Override
    public Set<String> calculateDependenciesPaths(String site, List<String> paths) throws ServiceLayerException {
        Map<String, String> dependencies = calculatePublishingDependencies(site, paths);
        return dependencies.keySet();
    }

    private Map<String, String> calculatePublishingDependencies(String site, List<String> paths)
            throws ServiceLayerException {
        Set<String> toRet = new HashSet<>();

        logger.debug("Get all the publishing dependencies for site '{}' paths '{}'", site, paths);
        Set<String> pathsParams = new HashSet<>(paths);
        Set<String> mandatoryParents = getMandatoryParentsForPublishing(site, paths);
        Map<String, String> ancestors = new HashMap<>();
        for (String p : paths) {
            ancestors.put(p, p);
        }
        for (String p : mandatoryParents) {
            String prefix = p.replace(FILE_SEPARATOR + INDEX_FILE, "");
            for (String p2 : paths) {
                if (p2.startsWith(prefix)) {
                    ancestors.put(p, p2);
                    break;
                }
            }
        }
        boolean exitCondition = false;
        do {
            List<Map<String, String>> deps = calculatePublishingDependenciesForListFromDB(site, pathsParams);
            List<String> targetPaths = new ArrayList<>();
            for (Map<String, String> d : deps) {
                String srcPath = d.get(SORUCE_PATH_COLUMN_NAME);
                String targetPath = d.get(TARGET_PATH_COLUMN_NAME);
                if (!ancestors.containsKey(targetPath)) {
                    if (!StringUtils.equals(targetPath, ancestors.get(srcPath))) {
                        ancestors.put(targetPath, ancestors.get(srcPath));
                    }
                }
                targetPaths.add(targetPath);
            }
            exitCondition = !toRet.addAll(targetPaths);
            pathsParams.clear();
            pathsParams.addAll(targetPaths);
        } while (!exitCondition);

        return ancestors;
    }

    private Set<String> getMandatoryParentsForPublishing(String site, List<String> paths) {
        Set<String> toRet = new HashSet<>();
        Set<String> possibleParents = calculatePossibleParents(paths);
        if (!possibleParents.isEmpty()) {
            List<String> pp = new ArrayList<>(possibleParents);
            List<String> result = itemDao.getMandatoryParentsForPublishing(site, pp, ItemState.NEW_MASK,
                    ItemState.MODIFIED_MASK);
            toRet.addAll(result);
        }
        return toRet;
    }

    private Set<String> calculatePossibleParents(List<String> paths) {
        Set<String> possibleParents = new HashSet<>();
        for (String path : paths) {
            StringTokenizer stPath = new StringTokenizer(path.replace(FILE_SEPARATOR + INDEX_FILE, ""), FILE_SEPARATOR);
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

    private List<Map<String, String>> calculatePublishingDependenciesForListFromDB(String site, Set<String> paths) {
        Map<String, Object> params = new HashMap<>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        params.put(REGEX_PARAM, getItemSpecificDependenciesPatterns());
        params.put(MODIFIED_MASK, ItemState.MODIFIED_MASK);
        params.put(NEW_MASK, ItemState.NEW_MASK);
        return dependencyMapper.calculatePublishingDependenciesForList(params);
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setDependencyMapper(DependencyMapper dependencyMapper) {
        this.dependencyMapper = dependencyMapper;
    }

    public void setDependencyService(org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setItemDao(ItemDAO itemDao) {
        this.itemDao = itemDao;
    }

    public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
    }
}
