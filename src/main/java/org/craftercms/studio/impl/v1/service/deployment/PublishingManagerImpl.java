/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
package org.craftercms.studio.impl.v1.service.deployment;


import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.dal.CopyToEnvironmentMapper;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.dal.PublishToTarget;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.deployment.*;
import org.craftercms.studio.api.v1.service.notification.NotificationService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import org.craftercms.studio.impl.v1.deployment.DeployerFactory;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

public class PublishingManagerImpl implements PublishingManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishingManagerImpl.class);

    private static final String TARGET_REQUEST_PARAMETER = "target";
    private static final String VERSION_REQUEST_PARAMETER = "version";
    private static final String PASSWORD_REQUEST_PARAMETER = "password";
    private final static String SITE_REQUEST_PARAMETER = "siteId";
    private final static String DELETED_FILES_REQUEST_PARAMETER = "deletedFiles";
    private final static String CONTENT_LOCATION_REQUEST_PARAMETER = "contentLocation";
    private final static String CONTENT_FILE_REQUEST_PARAMETER = "contentFile";
    private final static String METADATA_FILE_REQUEST_PARAMETER = "metadataFile";

    private final static String FILES_SEPARATOR = ",";
    private final static String LIVE_ENVIRONMENT = "live";
    private final static String PRODUCTION_ENVIRONMENT = "Production";
    private final static String WORK_AREA_ENVIRONMENT = "work-area";

    @Override
    public boolean checkConnection(DeploymentEndpointConfigTO target) {
        boolean connOk = false;
        if (target.getStatusUrl() != null && !target.getStatusUrl().isEmpty()) {
            LOGGER.debug(String.format("Check deployment agent status for target ", target.getName()));
            URL statusUrl = null;
            try {
                statusUrl = new URL(target.getStatusUrl());
            } catch (MalformedURLException e) {
                LOGGER.error(String.format("Invalid endpoint status URL for publishing channel [%s]", target.getName()), e);
            }
            GetMethod getMethod = null;
            HttpClient client = null;
            try {
                getMethod = new GetMethod(target.getStatusUrl());
                client = new HttpClient();
                int status = client.executeMethod(getMethod);
                if (status == HttpStatus.SC_OK) {
                    connOk = true;
                }

            } catch (Exception e) {
                LOGGER.error(String.format("Target (%s) is not available. Status check failed for url %s", target.getName(), target.getStatusUrl()));
            } finally {
                if (client != null) {
                    HttpConnectionManager mgr = client.getHttpConnectionManager();
                    if (mgr instanceof SimpleHttpConnectionManager) {
                        ((SimpleHttpConnectionManager)mgr).shutdown();
                    }
                }
                if (getMethod != null) {
                    getMethod.releaseConnection();
                }
                getMethod = null;
                client = null;

            }
        }
        return connOk;
    }

    @Override
    public long getTargetVersion(DeploymentEndpointConfigTO target, String site) {
        long version = -1;
        if (target.getVersionUrl() != null && !target.getVersionUrl().isEmpty()) {
            LOGGER.debug(String.format("Get deployment agent version for target ", target.getName()));
            URL versionUrl = null;
            try {
                versionUrl = new URL(target.getVersionUrl());
            } catch (MalformedURLException e) {
                LOGGER.error(String.format("Invalid get version URL for target [%s]", target.getName()), e);
            }
            GetMethod getMethod = null;
            HttpClient client = null;
            try {
                getMethod = new GetMethod(target.getVersionUrl());
                String siteId = target.getSiteId();
                if (StringUtils.isEmpty(siteId)) {
                    siteId = site;
                }
                getMethod.setQueryString(new NameValuePair[] {new NameValuePair(TARGET_REQUEST_PARAMETER, target.getTarget()),
                        new NameValuePair(SITE_REQUEST_PARAMETER, siteId) });
                client = new HttpClient();
                int status = client.executeMethod(getMethod);
                if (status == HttpStatus.SC_OK) {
                    InputStream responseStream = getMethod.getResponseBodyAsStream();
                    String responseText = IOUtils.toString(responseStream);
                    if (responseText != null && !responseText.isEmpty()) {
                        version = Long.parseLong(responseText.trim());
                    } else {
                        version = 0;
                    }
                }

            } catch (Exception e) {
                //LOGGER.error(String.format("Target (%s) responded with error while checking target version. Get version failed for url %s", target.getName(), target.getVersionUrl()));

            } finally {
                if (client != null) {
                    HttpConnectionManager mgr = client.getHttpConnectionManager();
                    if (mgr instanceof SimpleHttpConnectionManager) {
                        ((SimpleHttpConnectionManager)mgr).shutdown();
                    }
                }
                if (getMethod != null) {
                    getMethod.releaseConnection();
                }
                getMethod = null;
                client = null;

            }
        }
        return version;
    }

    @Override
    public List<PublishToTarget> getItemsToSync(String site, long targetVersion, List<String> environments) {
        return deploymentService.getItemsToSync(site, targetVersion, environments);
    }

    @Override
    public void deployItemsToTarget(String site, List<PublishToTarget> filteredItems, DeploymentEndpointConfigTO target) throws ContentNotFoundForPublishingException, UploadFailedException {
        LOGGER.debug("Start deploying items for site \"{0}\", target \"{1}\", number of items \"{2}\"", site, target.getName(), filteredItems.size());

        int numberOfBuckets = filteredItems.size() / target.getBucketSize() + 1;
        String environment = filteredItems.get(0).getEnvironment();
        Iterator<PublishToTarget> iter = filteredItems.iterator();
        LOGGER.debug("Divide all deployment items into {0} bucket(s) for  target {1}", numberOfBuckets , target.getName());
        // TODO: implement reactor version of deployment events
        //List<DeploymentEventItem> eventItems = new ArrayList<DeploymentEventItem>();
        for (int bucketIndex = 0; bucketIndex < numberOfBuckets; bucketIndex++) {
            int cntFiles = 0;

            String siteId = target.getSiteId();
            if (StringUtils.isEmpty(siteId)) {
                siteId = site;
            }

            LOGGER.debug("Preparing deployment items (bucket {0}) for target {1}", bucketIndex + 1, target.getName());

            int loopSize = (filteredItems.size() - (bucketIndex * target.getBucketSize()) > target.getBucketSize()) ? target.getBucketSize() : filteredItems.size() - bucketIndex * target.getBucketSize();
            List<String> filesToDeploy = new ArrayList<String>();
            List<String> deletedFiles = new ArrayList<String>();
            for (int j = 0; j < loopSize; j++) {

                if (iter.hasNext()) {

                    PublishToTarget item = iter.next();
                    LOGGER.debug("Parsing \"{0}\" , site \"{1}\"; for publishing on target \"{2}\"", item.getPath(), item.getSite(), target.getName());
                    /*
                    DeploymentEventItem eventItem = new DeploymentEventItem();
                    eventItem.setSite(item.getSite());
                    eventItem.setPath(item.getPath());
                    eventItem.setUser(item.getUser());
                    eventItem.setDateTime(new Date());
                    */

                    if (StringUtils.equals(item.getAction(), PublishToTarget.Action.DELETE)) {
                        //eventItem.setState(DeploymentEventItem.STATE_DELETED);
                        deletedFiles.add(item.getPath());
                        if (item.getPath().endsWith("/" + indexFile)) {
                            String fullPath = contentService.expandRelativeSitePath(item.getSite(), item.getPath().replace("/" + DmConstants.INDEX_FILE, ""));
                            String folderPath = item.getPath().replace("/" + indexFile, "");
                            if (contentRepository.contentExists(fullPath)) {
                                RepositoryItem[] children = contentRepository.getContentChildren(fullPath);
                                if (children.length < 2) {
                                    deletedFiles.add(folderPath);
                                }
                            } else {
                                deletedFiles.add(folderPath);
                            }
                        }
                    } else {
                        filesToDeploy.add(item.getPath());
                        if (StringUtils.equals(item.getAction(), PublishToTarget.Action.MOVE)) {
                            if (item.getOldPath() != null && !item.getOldPath().equalsIgnoreCase(item.getPath())) {
                                LOGGER.debug("Add old path to be deleted for MOVE action (\"{0}\")", item.getOldPath());
                                deletedFiles.add(item.getOldPath());
                                if (item.getOldPath().endsWith("/" + indexFile)) {
                                    String fullPath = contentService.expandRelativeSitePath(item.getSite(), item.getOldPath().replace("/" + DmConstants.INDEX_FILE, ""));
                                    String folderPath = item.getOldPath().replace("/" + indexFile, "");
                                    if (contentRepository.contentExists(fullPath)) {
                                        RepositoryItem[] children = contentRepository.getContentChildren(fullPath);
                                        if (children.length < 2) {
                                            deletedFiles.add(folderPath);
                                        }
                                    } else {
                                        deletedFiles.add(folderPath);
                                    }
                                }
                            }
                        }
                    }
                    cntFiles++;
                    //eventItems.add(eventItem);
                }
            }
            Deployer deployer = deployerFactory.createSyncTargetDeployer(environment, target);
            try {
                deployer.deployFiles(site, filesToDeploy, deletedFiles);



            } catch (ContentNotFoundForPublishingException e) {
                LOGGER.error("Deployment failed for bucket number {0} on target {1}.", bucketIndex + 1, target.getName());
                throw e;
            } catch (UploadFailedException e) {
                LOGGER.error("Deployment failed for bucket number {0} on target {1}.", bucketIndex + 1, target.getName());
                throw e;
            }
        }
        LOGGER.debug("Publishing deployment event for target \"{0}\" with \"{1}\" items.", target.getName(), 0/*eventItems.size()*/);
        //contentRepository.publishDeployEvent(target.getName(), eventItems);

        LOGGER.info("Deployment successful on target {0}", target.getName());
        LOGGER.debug("Finished deploying items for site \"{0}\", target \"{1}\", number of items \"{2}\"", site, target.getName(), filteredItems.size());
    }

    private String getDestinationPath(String site, String path, String environment) {
        return String.format("%s/%s/%s/%s", environmentsStoreRootPath, site, environment, path);
    }

    @Override
    public long setTargetVersion(DeploymentEndpointConfigTO target, long newVersion, String site) {
        long resoponseVersion = -1;
        if (target.getVersionUrl() != null && !target.getVersionUrl().isEmpty()) {
            LOGGER.debug("Set deployment agent version for target {0}", target.getName());
            URL versionUrl = null;
            try {
                versionUrl = new URL(target.getVersionUrl());
            } catch (MalformedURLException e) {
                LOGGER.error("Invalid set version URL for target [%s]", target.getName());
                return resoponseVersion;
            }
            PostMethod postMethod = null;
            HttpClient client = null;
            try {
                postMethod = new PostMethod(target.getVersionUrl());
                postMethod.addParameter(TARGET_REQUEST_PARAMETER, target.getTarget());
                postMethod.addParameter(VERSION_REQUEST_PARAMETER, String.valueOf(newVersion));
                String siteId = target.getSiteId();
                if (StringUtils.isEmpty(siteId)) {
                    siteId = site;
                }
                postMethod.addParameter(SITE_REQUEST_PARAMETER, site);
                client = new HttpClient();
                int status = client.executeMethod(postMethod);
                if (status == HttpStatus.SC_OK) {
                    InputStream responseStream = postMethod.getResponseBodyAsStream();
                    String responseText = IOUtils.toString(responseStream);
                    if (responseText != null && !responseText.isEmpty()) {
                        resoponseVersion = Long.parseLong(responseText);
                    } else {
                        resoponseVersion = 0;
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Target {0} responded with error while setting target version. Set version failed for url {1}", target.getName(), target.getVersionUrl());

            } finally {
                if (client != null) {
                    HttpConnectionManager mgr = client.getHttpConnectionManager();
                    if (mgr instanceof SimpleHttpConnectionManager) {
                        ((SimpleHttpConnectionManager)mgr).shutdown();
                    }
                }
                if (postMethod != null) {
                    postMethod.releaseConnection();
                }
                postMethod = null;
                client = null;

            }
        }
        return resoponseVersion;
    }

    @Override
    public List<CopyToEnvironment> getItemsReadyForDeployment(String site, String environment) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("state", CopyToEnvironment.State.READY_FOR_LIVE);
        params.put("environment", environment);
        params.put("now", new Date());
        return copyToEnvironmentMapper.getItemsReadyForDeployment(params);
    }

    @Override
    public void processItem(CopyToEnvironment item) throws DeploymentException {

        String liveEnvironment = siteService.getLiveEnvironmentName(item.getSite());
        boolean isLive = false;
        if (StringUtils.isNotEmpty(liveEnvironment)) {
            if (liveEnvironment.equals(item.getEnvironment())) {
                isLive = true;
            }
        } else if (LIVE_ENVIRONMENT.equalsIgnoreCase(item.getEnvironment()) || PRODUCTION_ENVIRONMENT.equalsIgnoreCase(item.getEnvironment())) {
            isLive = true;
        }
        if (StringUtils.equals(item.getAction(), CopyToEnvironment.Action.DELETE)) {
            Deployer deployer = deployerFactory.createEnvironmentStoreDeployer(item.getEnvironment());
            if (item.getOldPath() != null && item.getOldPath().length() > 0) {
                contentService.deleteContent(item.getSite(), item.getOldPath(), item.getUser());
                boolean hasRenamedChildren = false;
                deployer.deleteFile(item.getSite(), item.getOldPath());
                if (item.getOldPath().endsWith("/" + DmConstants.INDEX_FILE)) {
                    String fullPath = contentService.expandRelativeSitePath(item.getSite(), item.getOldPath().replace("/" + DmConstants.INDEX_FILE, ""));
                    RepositoryItem[] children = contentRepository.getContentChildren(fullPath);
                    if (children.length < 2) {
                        deployer.deleteFile(item.getSite(), item.getOldPath().replace("/" + DmConstants.INDEX_FILE, ""));
                    } else {
                        hasRenamedChildren = true;
                    }
                    if (!hasRenamedChildren) {
                        contentService.deleteContent(item.getSite(), item.getOldPath().replace("/" + DmConstants.INDEX_FILE, ""), false, item.getUser());
                    }
                }

                objectMetadataManager.clearRenamed(item.getSite(), item.getPath());
            }
            boolean haschildren = false;
            deployer.deleteFile(item.getSite(), item.getPath());
            if (item.getPath().endsWith("/" + DmConstants.INDEX_FILE)) {
                String fullPath = contentService.expandRelativeSitePath(item.getSite(), item.getPath().replace("/" + DmConstants.INDEX_FILE, ""));
                RepositoryItem[] children = contentRepository.getContentChildren(fullPath);
                if (children.length < 2) {
                    deployer.deleteFile(item.getSite(), item.getPath().replace("/" + DmConstants.INDEX_FILE, ""));
                } else {
                    haschildren = true;
                }
            }

            if (isLive) {
                contentService.deleteContent(item.getSite(), item.getPath(), item.getUser());
                if (!haschildren) {
                    contentService.deleteContent(item.getSite(), item.getPath().replace("/" + DmConstants.INDEX_FILE, ""), false, item.getUser());
                }
            }
        } else {
            LOGGER.debug("Setting system processing for {0}:{1}", item.getSite(), item.getPath());
            objectStateService.setSystemProcessing(item.getSite(), item.getPath(), true);
            if (isLive) {
                if (!importModeEnabled) {
                    contentRepository.createVersion(contentService.expandRelativeSitePath(item.getSite(), item.getPath()), item.getSubmissionComment(), true);
                } else {
                    LOGGER.debug("Import mode is ON. Create new version is skipped for [{0}] site \"{1}\"", item.getPath(), item.getSite());
                }
            }
            if (StringUtils.equals(item.getAction(), CopyToEnvironment.Action.MOVE)) {
                if (item.getOldPath() != null && item.getOldPath().length() > 0) {
                    Deployer deployer = deployerFactory.createEnvironmentStoreDeployer(item.getEnvironment());
                    deployer.deleteFile(item.getSite(), item.getOldPath());
                    if (item.getOldPath().endsWith("/" + DmConstants.INDEX_FILE)) {
                        boolean hasRenamedChildren = false;
                        String fullPath = contentService.expandRelativeSitePath(item.getSite(), item.getOldPath().replace("/" + DmConstants.INDEX_FILE, ""));
                        RepositoryItem[] children = contentRepository.getContentChildren(fullPath);
                        if (children.length < 2) {
                            deployer.deleteFile(item.getSite(), item.getOldPath().replace("/" + DmConstants.INDEX_FILE, ""));
                        } else {
                            hasRenamedChildren = true;
                        }
                        if (!hasRenamedChildren) {
                            contentService.deleteContent(item.getSite(), item.getOldPath().replace("/" + DmConstants.INDEX_FILE, ""), false, item.getUser());
                        }
                    }
                    if (isLive) {
                        objectMetadataManager.clearRenamed(item.getSite(), item.getPath());
                    }
                }
            }
            LOGGER.debug("Getting deployer for environment store.");
            Deployer deployer = deployerFactory.createEnvironmentStoreDeployer(item.getEnvironment());
            deployer.deployFile(item.getSite(), item.getPath());


            ObjectMetadata objectMetadata = objectMetadataManager.getProperties(item.getSite(), item.getPath());
            if (objectMetadata == null) {
                objectMetadataManager.insertNewObjectMetadata(item.getSite(), item.getPath());
                objectMetadata = objectMetadataManager.getProperties(item.getSite(), item.getPath());
            }
            boolean sendEmail = objectMetadata.getSendEmail() == 1 ? true : false;
            if (sendEmail) {
                String submittedByValue = objectMetadata.getSubmittedBy();
                notificationService.sendApprovalNotification(item.getSite(), submittedByValue, item.getPath(), item.getUser());
            }

            if (isLive) {
                ContentItemTO contentItem = contentService.getContentItem(item.getSite(), item.getPath());
                objectStateService.transition(item.getSite(), contentItem, TransitionEvent.DEPLOYMENT);
            }
            LOGGER.debug("Resetting system processing for {0}:{1}", item.getSite(), item.getPath());
            objectStateService.setSystemProcessing(item.getSite(), item.getPath(), false);
        }
    }

    @Override
    public void setLockBehaviourEnabled(boolean enabled) {
        //contentRepository.setLockBehaviourEnabled(enabled);
    }

    @Override
    public void setupItemsForPublishingSync(String site, String environment, List<CopyToEnvironment> itemsToDeploy) throws DeploymentException {
        deploymentService.setupItemsForPublishingSync(site, environment, itemsToDeploy);
    }

    @Override
    public void insertDeploymentHistory(DeploymentEndpointConfigTO target, List<PublishToTarget> publishedItems, Date publishingDate) throws DeploymentException {
        deploymentService.insertDeploymentHistory(target, publishedItems, publishingDate);
    }

    @Override
    public void markItemsCompleted(String site, String environment, List<CopyToEnvironment> processedItems) throws DeploymentException {
        for (CopyToEnvironment item : processedItems) {
            item.setState(CopyToEnvironment.State.COMPLETED);
            copyToEnvironmentMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    public void markItemsProcessing(String site, String environment, List<CopyToEnvironment> itemsToDeploy) throws DeploymentException {
        for (CopyToEnvironment item : itemsToDeploy) {
            item.setState(CopyToEnvironment.State.PROCESSING);
            copyToEnvironmentMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    public void markItemsReady(String site, String environment, List<CopyToEnvironment> copyToEnvironmentItems) throws DeploymentException {
        for (CopyToEnvironment item : copyToEnvironmentItems) {
            item.setState(CopyToEnvironment.State.READY_FOR_LIVE);
            copyToEnvironmentMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    public List<CopyToEnvironment> processMandatoryDependencies(CopyToEnvironment item, List<String> pathsToDeploy, Set<String> missingDependenciesPaths) throws DeploymentException {
        List<CopyToEnvironment> mandatoryDependencies = new ArrayList<CopyToEnvironment>();
        String site = item.getSite();
        String path = item.getPath();

        if (StringUtils.equals(item.getAction(), CopyToEnvironment.Action.NEW) || StringUtils.equals(item.getAction(), CopyToEnvironment.Action.MOVE)) {
            if (ContentUtils.matchesPatterns(path, servicesConfig.getPagePatterns(site))) {
                String helpPath = path.replace("/" + indexFile, "");
                int idx = helpPath.lastIndexOf("/");
                String parentPath = helpPath.substring(0, idx) + "/" + indexFile;
                if (objectStateService.isNew(site, parentPath) /* TODO: check renamed || objectStateService.isRenamed(site, parentPath) */) {
                    String parentFullPath = contentService.expandRelativeSitePath(site, parentPath);
                    if (!missingDependenciesPaths.contains(parentFullPath) && !pathsToDeploy.contains(parentFullPath)) {
                        deploymentService.cancelWorkflow(site, parentPath);
                        missingDependenciesPaths.add(parentFullPath);
                        CopyToEnvironment parentItem = createMissingItem(site, parentPath, item);
                        processItem(parentItem);
                        mandatoryDependencies.add(parentItem);
                        mandatoryDependencies.addAll(processMandatoryDependencies(parentItem, pathsToDeploy, missingDependenciesPaths));
                    }
                }
            }

            List<String> dependentPaths = dmDependencyService.getDependencyPaths(site, path);
            for (String dependentPath : dependentPaths) {
                if (objectStateService.isNew(site, dependentPath) /* TODO: check renamed || contentRepository.isRenamed(site, dependentPath) */) {
                    String dependentFullPath = contentService.expandRelativeSitePath(site, dependentPath);
                    if (!missingDependenciesPaths.contains(dependentFullPath) && !pathsToDeploy.contains(dependentFullPath)) {
                        deploymentService.cancelWorkflow(site, dependentPath);
                        missingDependenciesPaths.add(dependentFullPath);
                        CopyToEnvironment dependentItem = createMissingItem(site, dependentPath, item);
                        processItem(dependentItem);
                        mandatoryDependencies.add(dependentItem);
                        mandatoryDependencies.addAll(processMandatoryDependencies(dependentItem, pathsToDeploy, missingDependenciesPaths));
                    }
                }
            }
        }

        return mandatoryDependencies;
    }

    private CopyToEnvironment createMissingItem(String site, String itemPath, CopyToEnvironment item) {
        CopyToEnvironment missingItem = new CopyToEnvironment();
        missingItem.setSite(site);
        missingItem.setEnvironment(item.getEnvironment());
        missingItem.setPath(itemPath);
        missingItem.setScheduledDate(item.getScheduledDate());
        missingItem.setState(item.getState());
        if (objectStateService.isNew(site, itemPath)) {
            missingItem.setAction(CopyToEnvironment.Action.NEW);
        }
        ObjectMetadata metadata = objectMetadataManager.getProperties(site, itemPath);
        if ((metadata != null) && (metadata.getRenamed() != 0)) {
            String oldPath = metadata.getOldUrl();
            missingItem.setOldPath(oldPath);
            missingItem.setAction(CopyToEnvironment.Action.MOVE);
        }
        String contentTypeClass = contentService.getContentTypeClass(site, itemPath);
        missingItem.setContentTypeClass(contentTypeClass);
        missingItem.setUser(item.getUser());
        missingItem.setSubmissionComment(item.getSubmissionComment());
        return missingItem;
    }

    public String getIndexFile() {  return indexFile; }
    public void setIndexFile(String indexFile) { this.indexFile = indexFile; }

    public boolean isImportModeEnabled() { return importModeEnabled; }
    public void setImportModeEnabled(boolean importModeEnabled) {
        this.importModeEnabled = importModeEnabled;
        LOGGER.info("Import mode is {0}. Creating new version when deploying content is {1}", importModeEnabled ? "ON" : "OFF", importModeEnabled ? "DISABLED" : "ENABLED");
    }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public org.craftercms.studio.api.v1.service.objectstate.ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    public DeploymentService getDeploymentService() { return deploymentService; }
    public void setDeploymentService(DeploymentService deploymentService) { this.deploymentService = deploymentService; }

    public String getEnvironmentsStoreRootPath() { return environmentsStoreRootPath; }
    public void setEnvironmentsStoreRootPath(String environmentsStoreRootPath) { this.environmentsStoreRootPath = environmentsStoreRootPath; }

    public DeployerFactory getDeployerFactory() { return deployerFactory; }
    public void setDeployerFactory(DeployerFactory deployerFactory) { this.deployerFactory = deployerFactory; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public NotificationService getNotificationService() { return notificationService; }
    public void setNotificationService(NotificationService notificationService) { this.notificationService = notificationService; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    protected String indexFile;
    protected boolean importModeEnabled;
    protected SiteService siteService;
    protected ObjectStateService objectStateService;
    protected ContentService contentService;
    protected DmDependencyService dmDependencyService;
    protected DeploymentService deploymentService;
    protected String environmentsStoreRootPath;
    protected DeployerFactory deployerFactory;
    protected ContentRepository contentRepository;
    protected ObjectMetadataManager objectMetadataManager;
    protected NotificationService notificationService;
    protected ServicesConfig servicesConfig;

    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;
}