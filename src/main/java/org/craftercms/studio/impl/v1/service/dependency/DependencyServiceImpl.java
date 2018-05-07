package org.craftercms.studio.impl.v1.service.dependency;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.DependencyEntity;
import org.craftercms.studio.api.v1.dal.DependencyMapper;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyResolver;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.CalculateDependenciesEntityTO;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.studio.api.v1.dal.DependencyMapper.EDITED_STATES_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.NEW_STATES_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.PATHS_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.REGEX_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.SITE_PARAM;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.SORUCE_PATH_COLUMN_NAME;
import static org.craftercms.studio.api.v1.dal.DependencyMapper.TARGET_PATH_COLUMN_NAME;

public class DependencyServiceImpl implements DependencyService {

    private static final Logger logger = LoggerFactory.getLogger(DependencyServiceImpl.class);

    @Autowired
    protected DependencyMapper dependencyMapper;

    protected SiteService siteService;
    protected ContentService contentService;
    protected DependencyResolver dependencyResolver;
    protected PlatformTransactionManager transactionManager;
    protected List<String> itemSpecificDependencies;
    protected ObjectMetadataManager objectMetadataManager;
    protected ContentRepository contentRepository;


    @Override
    public Set<String> upsertDependencies(String site, String path) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
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
                insertDependencies(dependencyEntities);
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

    private void deleteAllSourceDependencies(String site, String path) {
        logger.debug("Delete all source dependencies for site: " + site + " path: " + path);
        Map<String, String> params = new HashMap<String, String>();
        params.put("site", site);
        params.put("path", path);
        dependencyMapper.deleteAllSourceDependencies(params);
    }

    private List<DependencyEntity> createDependencyEntities(String site, String path, Set<String> dependencyPaths, String dependencyType, Set<String> extractedPaths) {
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
        String cleanPath = path.replaceAll("//", "/");
        return cleanPath;
    }

    private void insertDependencies(List<DependencyEntity> dependencyEntities) {
        logger.debug("Insert list of dependency entities into database");
        if (CollectionUtils.isNotEmpty(dependencyEntities)) {
            Map<String, Object> params = new HashMap<>();
            params.put("dependencies", dependencyEntities);
            dependencyMapper.insertList(params);
        }
    }

    @Override
    public Set<String> upsertDependencies(String site, List<String> paths) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        Set<String> toRet = new HashSet<String>();
        List<DependencyEntity> dependencyEntities = new ArrayList<>();
        StringBuilder sbPaths = new StringBuilder();
        logger.debug("Resolving dependencies for list of paths.");
        for (String path : paths) {
            sbPaths.append("\n").append(path);
            logger.debug("Resolving dependencies for content site: " + site + " path: " + path);
            Map<String, Set<String>> dependencies = dependencyResolver.resolve(site, path);
            if (dependencies != null) {
                logger.debug("Found " + dependencies.size() + " dependencies. Create entities to insert into database.");
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
            deleteAllSourceDependencies(site, paths);
            logger.debug("Insert all extracted dependencies entries lof list of paths for site: " + site);
            insertDependencies(dependencyEntities);
            logger.debug("Committing transaction.");
            transactionManager.commit(txStatus);
        } catch (Exception e) {
            logger.debug("Rolling back transaction.");
            transactionManager.rollback(txStatus);
            throw new ServiceException("Failed to upsert dependencies for site: " + site + " paths: " + sbPaths.toString(), e);
        }

        return toRet;
    }

    private void deleteAllSourceDependencies(String site, List<String> paths) {
        for (String path : paths) {
            deleteAllSourceDependencies(site, path);
        }
    }

    @Override
    public Set<String> getPublishingDepenencies(String site, String path) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        logger.debug("Get publishing dependencies for site: " + site + " path:" + path);
        List<String> paths = new ArrayList<String>();
        paths.add(path);
        return getPublishingDepenencies(site, paths);
    }

    @Override
    public Set<String> getPublishingDepenencies(String site, List<String> paths) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        Map<String, Object> params = new HashMap<String, Object>();

        Set<String> toRet = new HashSet<String>();
        Set<String> pathsParams = new HashSet<String>();


        // NEW
        logger.debug("Get all dependencies that are NEW");
        pathsParams.addAll(paths);
        boolean exitCondition = false;
        do {
            params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("paths", pathsParams);
            params.put("states", State.NEW_STATES);
            List<String> deps = dependencyMapper.getPublishingDependenciesForList(params);
            exitCondition = !toRet.addAll(deps);
            pathsParams.clear();
            pathsParams.addAll(deps);
        } while (!exitCondition);

        logger.debug("Get all item specific dependencies that are edited");
        Collection<State> onlyEditStates = CollectionUtils.removeAll(State.CHANGE_SET_STATES, State.NEW_STATES);
        pathsParams.clear();
        pathsParams.addAll(paths);
        do {
            params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("paths", pathsParams);
            params.put("states", onlyEditStates);
            List<String> deps = dependencyMapper.getPublishingDependenciesForList(params);
            Set<String> filtered = new HashSet<String>();
            filtered.addAll(deps);
            filtered = filterItemSpecificDependencies(filtered);
            exitCondition = !toRet.addAll(filtered);
            pathsParams.clear();
            pathsParams.addAll(filtered);
        } while (!exitCondition);

        return toRet;
    }

    private Set<String> filterItemSpecificDependencies(Set<String> paths) {
        Set<String> toRet = new HashSet<String>();
        for (String path : paths) {
            for (String itemSpecificDependency : itemSpecificDependencies) {
                Pattern p = Pattern.compile(itemSpecificDependency);
                Matcher m = p.matcher(path);
                if (m.matches()) {
                    toRet.add(path);
                    break;
                }
            }
        }
        return toRet;
    }

    @Override
    public Set<String> getItemSpecificDependencies(String site, String path, int depth) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        Map<String, Object> params = new HashMap<String, Object>();
        Set<String> toRet = new HashSet<String>();
        Set<String> paths = new HashSet<String>();
        boolean exitCondition = false;
        paths.add(path);
        if (depth < 0) {
            do {
                params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("paths", paths);
                List<String> deps = dependencyMapper.getDependenciesForList(params);
                Set<String> filtered = new HashSet<String>();
                filtered.addAll(deps);
                filtered = filterItemSpecificDependencies(filtered);
                exitCondition = !toRet.addAll(filtered);
                paths.clear();
                paths.addAll(filtered);
            } while (!exitCondition);
        } else {
            int d = depth;
            while (d-- > 0) {
                params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("paths", paths);
                List<String> deps = dependencyMapper.getDependenciesForList(params);
                Set<String> filtered = new HashSet<String>();
                filtered.addAll(deps);
                filtered = filterItemSpecificDependencies(filtered);
                exitCondition = !toRet.addAll(filtered);
                paths.clear();
                paths.addAll(filtered);
                if (exitCondition) break;
            }
        }
        return toRet;
    }

    @Override
    public Set<String> getItemDependencies(String site, String path, int depth) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        logger.debug("Get dependency items for content " + path + " for site " + site);

        Map<String, Object> params = new HashMap<String, Object>();
        Set<String> toRet = new HashSet<String>();
        Set<String> paths = new HashSet<String>();
        paths.add(path);
        boolean exitCondition;
        if (depth < 0) {
            do {
                params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("paths", paths);
                List<String> deps = dependencyMapper.getDependenciesForList(params);
                exitCondition = !toRet.addAll(deps);
                paths.clear();
                paths.addAll(deps);
            } while (!exitCondition);
        } else {
            int d = depth;
            while (d-- > 0) {
                params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("paths", paths);
                List<String> deps = dependencyMapper.getDependenciesForList(params);
                exitCondition = !toRet.addAll(deps);
                if (exitCondition) break;
            }
        }
        return toRet;
    }

    @Override
    public Set<String> getItemsDependingOn(String site, String path, int depth) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
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
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("targetPath", path);
        if (depth < 0) {
            paths.add(path);
            do {
                params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("paths", paths);
                List<String> deps = dependencyMapper.getItemsDependingOn(params);
                toRet.addAll(deps);
                paths.clear();
                paths.addAll(deps);
            } while (paths.size() > 0);
        } else {
            int d = depth;
            paths.add(path);
            while (d-- > 0) {
                params = new HashMap<String, Object>();
                params.put("site", site);
                params.put("paths", paths);
                List<String> deps = dependencyMapper.getItemsDependingOn(params);
                toRet.addAll(deps);
                paths.clear();
                paths.addAll(deps);
            }
        }

        return toRet;
    }

    @Override
    public Set<String> moveDependencies(String site, String oldPath, String newPath) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, newPath)) {
            throw new ContentNotFoundException();
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put("siteId", site);
        params.put("oldPath", oldPath);
        params.put("newPath", newPath);
        dependencyMapper.moveDependency(params);

        return getItemDependencies(site, newPath, 1);
    }

    @Override
    public void deleteItemDependencies(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        // Check if content exists
        if (!contentService.contentExists(site, path)) {
            throw new ContentNotFoundException();
        }

        logger.debug("Delete dependencies for item - site: " + site + " path: " + path);
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        params.put("path", path);
        dependencyMapper.deleteDependenciesForSiteAndPath(params);
    }

    @Override
    public void deleteItemDependencies(String site, List<String> paths) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
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

        logger.debug("Delete dependencies for item - site: " + site + " paths: " + sbPaths.toString());
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("path", paths);
        dependencyMapper.deleteDependenciesForSiteAndListOfPaths(params);
    }

    @Override
    public void deleteSiteDependencies(@ValidateStringParam(name = "site") String site) throws SiteNotFoundException, ServiceException {
        // Check if site exists
        if (!siteService.exists(site)) {
            throw new SiteNotFoundException();
        }

        logger.debug("Delete all dependencies for site " + site);
        Map<String, String> params = new HashMap<>();
        params.put("site", site);
        dependencyMapper.deleteDependenciesForSite(params);
    }

    @Override
    public Set<String> getDeleteDepenencies(String site, String path) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
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
    public Set<String> getDeleteDepenencies(String site, List<String> paths) throws SiteNotFoundException, ContentNotFoundException, ServiceException {
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
        logger.debug("Get dependencies from DB for all paths in subtree(s) and filter them by item specific and content type");
        List<String> depsSource = new ArrayList<String>();
        depsSource.addAll(paths);
        depsSource.addAll(children);
        Set<String> dependencies = getContentTypeFilteredDeleteDependencies(site, depsSource);
        toRet.addAll(dependencies);
        boolean doItAgain = false;

        // Step 3: recursion
        logger.debug("Repeat process for newly collected dependencies that have not been processed yet");
        doItAgain = doItAgain || processedPaths.addAll(children);
        doItAgain = doItAgain || processedPaths.addAll(dependencies);
        if (doItAgain) {
            List<String> pathsToProcess = new ArrayList<String>();
            pathsToProcess.addAll(children);
            pathsToProcess.addAll(dependencies);
            toRet.addAll(getDeleteDependenciesInternal(site, pathsToProcess, processedPaths));
        }

        logger.debug("Return collected set of dependencies");
        return toRet;
    }

    private Set<String> getContentTypeFilteredDeleteDependencies(String site, List<String> paths) {
        Map<String, Object> params = new HashMap<String, Object>();
        Set<String> toRet = new HashSet<String>();
        boolean exitCondition;
        do {
            logger.debug("Get dependencies from database");
            params = new HashMap<String, Object>();
            params.put("site", site);
            params.put("paths", paths);
            List<String> deps = dependencyMapper.getDependenciesForList(params);
            Set<String> filtered = new HashSet<String>();
            filtered.addAll(deps);
            logger.debug("Filter collected dependencies");
            filtered = filterItemSpecificDependencies(filtered);
            exitCondition = !toRet.addAll(filtered);
            paths.clear();
            paths.addAll(deps);
        } while (!exitCondition);
        return toRet;
    }

    private Set<String> getAllChildrenRecursively(String site, List<String> paths) {
        logger.debug("Get all content from subtree(s) for list of pats");
        Set<String> toRet = new HashSet<String>();

        for (String path : paths) {
            logger.debug("Get children from repository for content at site " + site + " path " + path);
            RepositoryItem[] children = contentRepository.getContentChildren(contentService.expandRelativeSitePath(site, path), true);
            if (children != null) {
                List<String> childrenPaths = new ArrayList<String>();
                for (RepositoryItem child : children) {
                    String childPath = child.path + "/" + child.name;
                    String relativePath = contentService.getRelativeSitePath(site, childPath);
                    childrenPaths.add(relativePath);
                }
                logger.debug("Adding all collected children paths");
                toRet.addAll(childrenPaths);
                toRet.addAll(getAllChildrenRecursively(site, childrenPaths));
            }
        }
        return toRet;
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
        for (final Map.Entry<String, String> d : deps.entrySet()) {
            if (d.getKey() != d.getValue()) {
                List<Map<String, String>> ds = temp.get(d.getValue());
                ds.add(new HashMap<String, String>() {{
                    put(CStudioConstants.JSON_PROPERTY_ITEM, d.getKey());
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
        params.put(REGEX_PARAM, itemSpecificDependencies);
        Collection<State> onlyEditStates = CollectionUtils.removeAll(State.CHANGE_SET_STATES, State.NEW_STATES);
        params.put(EDITED_STATES_PARAM, onlyEditStates);
        params.put(NEW_STATES_PARAM, State.NEW_STATES);
        return dependencyMapper.calculatePublishingDependenciesForList(params);
    }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public DependencyResolver getDependencyResolver() { return dependencyResolver; }
    public void setDependencyResolver(DependencyResolver dependencyResolver) { this.dependencyResolver = dependencyResolver; }

    public PlatformTransactionManager getTransactionManager() { return transactionManager; }
    public void setTransactionManager(PlatformTransactionManager transactionManager) { this.transactionManager = transactionManager; }

    public List<String> getItemSpecificDependencies() { return itemSpecificDependencies; }
    public void setItemSpecificDependencies(List<String> itemSpecificDependencies) { this.itemSpecificDependencies = itemSpecificDependencies; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }
}
