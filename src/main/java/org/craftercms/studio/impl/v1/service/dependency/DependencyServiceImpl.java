/*
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
 *
 */

package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.DependencyEntity;
import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.CalculateDependenciesEntityTO;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DeleteDependencyConfigTO;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import static org.craftercms.studio.api.v1.dal.DependencyMapper.EDITED_STATES_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.NEW_PATH_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.NEW_STATES_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.OLD_PATH_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.PATHS_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.PATH_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.REGEX_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.SITE_ID_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.SITE_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.SORUCE_PATH_COLUMN_NAME;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.TARGET_PATH_COLUMN_NAME;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.CONFIGURATION_DEPENDENCY_ITEM_SPECIFIC_PATTERNS;

public class DependencyServiceImpl implements DependencyService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyServiceImpl.class);

    @Autowired
    protected DependencyMapper dependencyMapper;
    protected StudioConfiguration studioConfiguration;
    protected SiteService siteService;
    protected ContentService contentService;
    protected DependencyResolver dependencyResolver;
    protected PlatformTransactionManager transactionManager;
    protected ObjectMetadataManager objectMetadataManager;
    protected ContentRepository contentRepository;
    protected ServicesConfig servicesConfig;

    @Override
    public Set<String> upsertDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        Set<String> toRet = new HashSet<String>();
        logger.debug("Resolving dependencies for content site: " + site + " path: " + path);
        Map<String, Set<String>> dependencies = dependencyResolver.resolve(site, path);
        List<DependencyEntity> dependencyEntities = new ArrayList<>();
        if (dependencies != null) {
            logger.debug("Found " + dependencies.size() + " dependencies. Create entities to insert into database.");
            for (String type : dependencies.keySet()) {
                dependencyEntities.addAll(createDependencyEntities(site, path, dependencies.get(type), type, toRet));
            }

            logger.debug("Preparing transaction for database updates.");
            DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
            defaultTransactionDefinition.setName("upsertDependencies");

            logger.debug("Starting transaction.");
            TransactionStatus txStatus = transactionManager.getTransaction(defaultTransactionDefinition);

            try {
                logger.debug("Delete all source dependencies for site: " + site + " path: " + path);
                deleteAllSourceDependencies(site, path);
                logger.debug("Insert all extracted dependencies entries for site: " + site + " path: " + path);
                insertDependenciesIntoDatabase(dependencyEntities);
                logger.debug("Committing transaction.");
                transactionManager.commit(txStatus);
            } catch (Exception e) {
                logger.debug("Rolling back transaction.");
                transactionManager.rollback(txStatus);
                throw new ServiceException("Failed to upsert dependencies for site: " + site + " path: " + path, e);
            }

        }
        return toRet;
    }

    @Override
    public Set<String> upsertDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        Set<String> toRet = new HashSet<String>();
        List<DependencyEntity> dependencyEntities = new ArrayList<>();
        StringBuilder sbPaths = new StringBuilder();
        logger.debug("Resolving dependencies for list of paths.");
        for (String path : paths) {
            sbPaths.append("\n").append(path);
            logger.debug("Resolving dependencies for content site: " + site + " path: " + path);
            Map<String, Set<String>> dependencies = dependencyResolver.resolve(site, path);
            if (dependencies != null) {
                logger.debug("Found " + dependencies.size() + " dependencies. " +
                        "Create entities to insert into database.");
                for (String type : dependencies.keySet()) {
                    dependencyEntities.addAll(createDependencyEntities(site, path, dependencies.get(type), type, toRet));
                }
            }
        }
        logger.debug("Preparing transaction for database updates.");
        DefaultTransactionDefinition defaultTransactionDefinition = new DefaultTransactionDefinition();
        defaultTransactionDefinition.setName("upsertDependencies");
        logger.debug("Starting transaction.");
        TransactionStatus txStatus = transactionManager.getTransaction(defaultTransactionDefinition);
        try {
            logger.debug("Delete all source dependencies for list of paths site: " + site);
            for (String path : paths) {
                deleteAllSourceDependencies(site, path);
            }
            logger.debug("Insert all extracted dependencies entries lof list of paths for site: " + site);
            insertDependenciesIntoDatabase(dependencyEntities);
            logger.debug("Committing transaction.");
            transactionManager.commit(txStatus);
        } catch (Exception e) {
            logger.debug("Rolling back transaction.");
            transactionManager.rollback(txStatus);
            throw new ServiceException("Failed to upsert dependencies for site: " + site + " paths: " +
                    sbPaths.toString(), e);
        }

        return toRet;
    }

    private void deleteAllSourceDependencies(String site, String path) {
        logger.debug("Delete all source dependencies for site: " + site + " path: " + path);
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_PARAM, site);
        params.put(PATH_PARAM, path);
        dependencyMapper.deleteAllSourceDependencies(params);
    }

    private List<DependencyEntity> createDependencyEntities(String site, String path, Set<String> dependencyPaths,
                                                            String dependencyType, Set<String> extractedPaths) {
        logger.debug("Create dependency entity TO for site: " + site + " path: " + path);
        List<DependencyEntity> dependencyEntities = new ArrayList<>();
        if (dependencyPaths != null && dependencyPaths.size() > 0) {
            for (String file : dependencyPaths) {
                DependencyEntity dependencyObj = new DependencyEntity();
                dependencyObj.setSite(site);
                dependencyObj.setSourcePath(getCleanPath(path));
                dependencyObj.setTargetPath(getCleanPath(file));
                dependencyObj.setType(dependencyType);
                dependencyEntities.add(dependencyObj);
                extractedPaths.add(file);
            }
        }
        return dependencyEntities;
    }

    private String getCleanPath(String path) {
        return path.replaceAll("//", "/");
    }

    private void insertDependenciesIntoDatabase(List<DependencyEntity> dependencyEntities) {
        logger.debug("Insert list of dependency entities into database");
        if (CollectionUtils.isNotEmpty(dependencyEntities)) {
            Map<String, Object> params = new HashMap<>();
            params.put(StudioConstants.JSON_PROPERTY_DEPENDENCIES, dependencyEntities);
            dependencyMapper.insertList(params);
        }
    }

    @Override
    public Set<String> getPublishingDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        logger.debug("Get publishing dependencies for site: " + site + " path:" + path);
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        return getPublishingDependencies(site, paths);
    }

    @Override
    public Set<String> getPublishingDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        Set<String> toRet = new HashSet<String>();
        Set<String> pathsParams = new HashSet<String>();

        logger.debug("Get all publishing dependencies");
        pathsParams.addAll(paths);
        boolean exitCondition = false;
        do {
            List<String> deps = getPublishingDependenciesForListFromDB(site, pathsParams);
            exitCondition = !toRet.addAll(deps);
            pathsParams.clear();
            pathsParams.addAll(deps);
        } while (!exitCondition);

        return toRet;
    }

    private List<String> getPublishingDependenciesForListFromDB(String site, Set<String> paths) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        params.put(REGEX_PARAM, getItemSpecificDependenciesPatterns());
        Collection<State> onlyEditStates = CollectionUtils.removeAll(State.CHANGE_SET_STATES, State.NEW_STATES);
        params.put(EDITED_STATES_PARAM, onlyEditStates);
        params.put(NEW_STATES_PARAM, State.NEW_STATES);
        return dependencyMapper.getPublishingDependenciesForList(params);
    }

    @Override
    public Set<String> getItemSpecificDependencies(String site, String path, int depth)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        Set<String> toRet = new HashSet<String>();
        Set<String> paths = new HashSet<String>();
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
            return new ArrayList<String>();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        params.put(REGEX_PARAM, getItemSpecificDependenciesPatterns());
        return dependencyMapper.getItemSpecificDependenciesForList(params);
    }

    @Override
    public Set<String> getItemDependencies(String site, String path, int depth)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        logger.debug("Get dependency items for content " + path + " for site " + site);

        Set<String> toRet = new HashSet<String>();
        Set<String> paths = new HashSet<String>();
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
            return new ArrayList<String>();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        return dependencyMapper.getDependenciesForList(params);
    }

    @Override
    public Set<String> getItemsDependingOn(String site, String path, int depth)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        logger.debug("Get items depending on content " + path + " for site " + site);
        Set<String> toRet = new HashSet<String>();
        Set<String> paths = new HashSet<String>();
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
            return new ArrayList<String>();
        }
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        return dependencyMapper.getItemsDependingOn(params);
    }

    @Override
    public Set<String> moveDependencies(String site, String oldPath, String newPath)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, newPath)) {
            throw new ContentNotFoundException();
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_ID_PARAM, site);
        params.put(OLD_PATH_PARAM, oldPath);
        params.put(NEW_PATH_PARAM, newPath);
        dependencyMapper.moveDependency(params);

        return getItemDependencies(site, newPath, 1);
    }

    @Override
    public void deleteItemDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        logger.debug("Delete dependencies for content site: " + site + " path: " + path);
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_PARAM, site);
        params.put(PATH_PARAM, path);
        dependencyMapper.deleteDependenciesForSiteAndPath(params);
    }

    @Override
    public void deleteSiteDependencies(String site) throws ServiceException {
        logger.debug("Delete all dependencies for site: " + site);
        Map<String, String> params = new HashMap<String, String>();
        params.put(SITE_PARAM, site);
        dependencyMapper.deleteDependenciesForSite(params);
    }

    @Override
    public Set<String> getDeleteDependencies(String site, String path)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        logger.debug("Get delete dependencies for content - site " + site + " path " + path);
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        Set<String> processedPaths = new HashSet<String>();
        return getDeleteDependenciesInternal(site, paths, processedPaths);
    }

    @Override
    public Set<String> getDeleteDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }
        StringBuilder sbPaths = new StringBuilder();
        for (String path : paths) {
            // Check if content exists
            if (!contentService.contentExists(site, path)) {
                throw new ContentNotFoundException();
            }
            sbPaths.append("\n").append(path);
        }

        logger.debug("Gete delete dependencies for content - site: " + site + " paths: " + sbPaths.toString());
        Set<String> processedPaths = new HashSet<String>();
        return getDeleteDependenciesInternal(site, paths, processedPaths);
    }

    private Set<String> getDeleteDependenciesInternal(String site, List<String> paths, Set<String> processedPaths) {
        Set<String> toRet = new HashSet<String>();
        // Step 1: collect all content from subtree
        logger.debug("Get all children from subtree");
        Set<String> children = getAllChildrenRecursively(site, paths);
        toRet.addAll(children);

        // Step 2: collect all dependencies from DB
        logger.debug("Get dependencies from DB for all paths in subtree(s) and filter them by item specific" +
                " and content type");
        Set<String> depsSource = new HashSet<String>();
        depsSource.addAll(paths);
        depsSource.addAll(children);
        Set<String> dependencies = getContentTypeFilteredDeleteDependencies(site, depsSource);
        toRet.addAll(dependencies);
        List<String> itemSpecificcDeps = getItemSpecificDependenciesFromDB(site, depsSource);
        toRet.addAll(itemSpecificcDeps);
        boolean doItAgain = false;

        // Step 3: recursion
        logger.debug("Repeat process for newly collected dependencies that have not been processed yet");
        doItAgain = doItAgain || processedPaths.addAll(children);
        doItAgain = doItAgain || processedPaths.addAll(dependencies);
        doItAgain = doItAgain || processedPaths.addAll(itemSpecificcDeps);
        if (doItAgain) {
            List<String> pathsToProcess = new ArrayList<String>();
            pathsToProcess.addAll(children);
            pathsToProcess.addAll(dependencies);
            toRet.addAll(getDeleteDependenciesInternal(site, pathsToProcess, processedPaths));
        }

        logger.debug("Return collected set of dependencies");
        return toRet;
    }

    private Set<String> getContentTypeFilteredDeleteDependencies(String site, Set<String> paths) {
        Set<String> toRet = new HashSet<String>();
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
        logger.debug("Get all content from subtree(s) for list of pats");
        Set<String> toRet = new HashSet<String>();

        for (String path : paths) {
            logger.debug("Get children from repository for content at site " + site + " path " + path);
            RepositoryItem[] children = contentRepository.getContentChildren(site, path);
            if (children != null) {
                List<String> childrenPaths = new ArrayList<String>();
                for (RepositoryItem child : children) {
                    String childPath = child.path + "/" + child.name;
                    childrenPaths.add(childPath);
                }
                logger.debug("Adding all collected children paths");
                toRet.addAll(childrenPaths);
                toRet.addAll(getAllChildrenRecursively(site, childrenPaths));
            }
        }
        return toRet;
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
    public Map<String, List<CalculateDependenciesEntityTO>> calculateDependencies(String site, List<String> paths)
            throws ServiceException {
        Map<String, List<CalculateDependenciesEntityTO>> toRet =
                new HashMap<String, List<CalculateDependenciesEntityTO>>();
        List<CalculateDependenciesEntityTO> entities = new ArrayList<CalculateDependenciesEntityTO>();
            Map<String, String> deps = calculatePublishingDependencies(site, paths);
            Map<String, List<Map<String, String>>> temp = new HashMap<String, List<Map<String,String>>>();
            for (String p : paths) {
                temp.put(p, new ArrayList<Map<String, String>>());
            }
            for (Map.Entry<String, String> d : deps.entrySet()) {
                if (d.getKey() != d.getValue()) {
                    List<Map<String, String>> ds = temp.get(d.getValue());
                    ds.add(new HashMap<String, String>() {{
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
    public Set<String> calculateDependenciesPaths(String site, List<String> paths) throws ServiceException {
        Map<String, String> dependencies = calculatePublishingDependencies(site, paths);
        return dependencies.keySet();
    }

    private Map<String, String> calculatePublishingDependencies(String site, List<String> paths)
            throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        Set<String> toRet = new HashSet<String>();
        Set<String> pathsParams = new HashSet<String>();

        logger.debug("Get all publishing dependencies");
        pathsParams.addAll(paths);
        boolean exitCondition = false;
        Map<String, String> ancestors = new HashMap<String, String>();
        for (String p : paths) {
            ancestors.put(p, p);
        }
        do {
            List<Map<String, String>> deps = calculatePublishingDependenciesForListFromDB(site, pathsParams);
            List<String> targetPaths = new ArrayList<String>();
            for (Map<String, String> d : deps) {
                String srcPath = d.get(SORUCE_PATH_COLUMN_NAME);
                String targetPath = d.get(TARGET_PATH_COLUMN_NAME);
                if (!ancestors.keySet().contains(targetPath)) {
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

    private List<Map<String, String>> calculatePublishingDependenciesForListFromDB(String site, Set<String> paths) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_PARAM, site);
        params.put(PATHS_PARAM, paths);
        params.put(REGEX_PARAM, getItemSpecificDependenciesPatterns());
        Collection<State> onlyEditStates = CollectionUtils.removeAll(State.CHANGE_SET_STATES, State.NEW_STATES);
        params.put(EDITED_STATES_PARAM, onlyEditStates);
        params.put(NEW_STATES_PARAM, State.NEW_STATES);
        return dependencyMapper.calculatePublishingDependenciesForList(params);
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ContentService getContentService() {
        return contentService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    public DependencyResolver getDependencyResolver() {
        return dependencyResolver;
    }

    public void setDependencyResolver(DependencyResolver dependencyResolver) {
        this.dependencyResolver = dependencyResolver;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public ObjectMetadataManager getObjectMetadataManager() {
        return objectMetadataManager;
    }

    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }

    public ContentRepository getContentRepository() {
        return contentRepository;
    }

    public void setContentRepository(ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }

    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }
}
