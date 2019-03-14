/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
import org.craftercms.commons.validation.annotations.param.ValidateLongParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.dal.CopyToEnvironmentMapper;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.dal.PublishToTarget;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.ebus.DeploymentEventItem;
import org.craftercms.studio.api.v1.ebus.DeploymentEventMessage;
import org.craftercms.studio.api.v1.ebus.DeploymentEventService;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.*;
import org.craftercms.studio.api.v1.service.notification.NotificationService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
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
    @ValidateParams
    public long getTargetVersion(DeploymentEndpointConfigTO target, @ValidateStringParam(name = "site") String site) {
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
                LOGGER.error(String.format("Target (%s) responded with error while checking target version. Get version failed for url %s", target.getName(), target.getVersionUrl()));

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
    @ValidateParams
    public List<PublishToTarget> getItemsToSync(@ValidateStringParam(name = "site") String site, @ValidateLongParam(name = "targetVersion") long targetVersion, List<String> environments) {
        return deploymentService.getItemsToSync(site, targetVersion, environments);
    }

    @Override
    @ValidateParams
    public void deployItemsToTarget(@ValidateStringParam(name = "site") String site, List<PublishToTarget> filteredItems, DeploymentEndpointConfigTO target) throws ContentNotFoundForPublishingException, UploadFailedException {
        LOGGER.debug("Start deploying items for site \"{0}\", target \"{1}\", number of items \"{2}\"", site, target.getName(), filteredItems.size());

        int numberOfBuckets = filteredItems.size() / target.getBucketSize() + 1;
        String environment = filteredItems.get(0).getEnvironment();
        Iterator<PublishToTarget> iter = filteredItems.iterator();
        LOGGER.debug("Divide all deployment items into {0} bucket(s) for  target {1}", numberOfBuckets , target.getName());

        List<DeploymentEventItem> eventItems = new ArrayList<DeploymentEventItem>();
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

                    DeploymentEventItem eventItem = new DeploymentEventItem(item.getSite(), item.getPath(), item.getOldPath(), item.getUsername(), new Date(), null);

                    if (StringUtils.equals(item.getAction(), PublishToTarget.Action.DELETE)) {
                        eventItem.setState(DeploymentEventItem.STATE_DELETED);
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

                        if (StringUtils.equals(item.getAction(), PublishToTarget.Action.NEW)) {
                            eventItem.setState(DeploymentEventItem.STATE_NEW);
                        } else if (StringUtils.equals(item.getAction(), PublishToTarget.Action.MOVE)) {
                            eventItem.setState(DeploymentEventItem.STATE_MOVED);
                        } else {
                            eventItem.setState(DeploymentEventItem.STATE_UPDATED);
                        }

                        filesToDeploy.add(item.getPath());
                    }
                    // populate old URL into the deleted files if this is MOVE or DELETE
                    if (StringUtils.equals(item.getAction(), PublishToTarget.Action.MOVE) ||
                            StringUtils.equals(item.getAction(), PublishToTarget.Action.DELETE)) {
                        if (item.getOldPath() != null && !item.getOldPath().equalsIgnoreCase(item.getPath())) {
                            LOGGER.debug("Add old path to be deleted for {0} action (\"{1}\")", item.getAction(), item.getOldPath());
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

                    cntFiles++;
                    eventItems.add(eventItem);
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
        LOGGER.debug("Publishing deployment event for target \"{0}\" with \"{1}\" items.", target.getName(), eventItems.size());
        String sessionTicket = securityProvider.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
        DeploymentEventMessage message = new DeploymentEventMessage(site, target.getName(), eventItems, repositoryEventContext);
        deploymentEventService.deploymentEvent(message);

        LOGGER.info("Deployment successful on target {0}", target.getName());
        LOGGER.debug("Finished deploying items for site \"{0}\", target \"{1}\", number of items \"{2}\"", site, target.getName(), filteredItems.size());
    }

    private String getDestinationPath(String site, String path, String environment) {
        return String.format("%s/%s/%s/%s", environmentsStoreRootPath, site, environment, path);
    }

    @Override
    @ValidateParams
    public long setTargetVersion(DeploymentEndpointConfigTO target, @ValidateLongParam(name = "newVersion") long newVersion, @ValidateStringParam(name = "site") String site) {
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
    @ValidateParams
    public List<CopyToEnvironment> getItemsReadyForDeployment(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment) {
        Map<String, Object> params = new HashMap<>();
        params.put("site", site);
        params.put("state", CopyToEnvironment.State.READY_FOR_LIVE);
        params.put("environment", environment);
        params.put("now", new Date());
        return copyToEnvironmentMapper.getItemsReadyForDeployment(params);
    }

    @Override
    public void processItem(CopyToEnvironment item) throws DeploymentException {
        if(item == null) {
            throw new DeploymentException("Cannot processItem. Item is null");
        }

        String site = item.getSite();
        String path = item.getPath();
        String oldPath = item.getOldPath();
        String environment = item.getEnvironment();
        String action = item.getAction();
        String user = item.getUser();
        String submissionComment = item.getSubmissionComment();
        

        String liveEnvironment = siteService.getLiveEnvironmentName(site);
        boolean isLive = false;

 
        if (StringUtils.isNotEmpty(liveEnvironment)) {
            if (liveEnvironment.equals(environment)) {
                isLive = true;
            }
        }
        else if (LIVE_ENVIRONMENT.equalsIgnoreCase(item.getEnvironment()) 
        || PRODUCTION_ENVIRONMENT.equalsIgnoreCase(environment)) {
            isLive = true;
        }

        LOGGER.debug("Processing Item, {0}, {1}, {2}, isLive: {3}, {4} ", site, path, environment, isLive, action);        

        if (StringUtils.equals(action, CopyToEnvironment.Action.DELETE)) {
            Deployer deployer = deployerFactory.createEnvironmentStoreDeployer(environment);

            if (oldPath != null && oldPath.length() > 0) {
                LOGGER.debug("Send DELETE for old item at path: {0}", oldPath);

                contentService.deleteContent(site, oldPath, user);
                boolean hasRenamedChildren = false;
                deployer.deleteFile(site, path);

                if (oldPath.endsWith("/" + DmConstants.INDEX_FILE)) {
                    String fullPath = contentService.expandRelativeSitePath(site, oldPath.replace("/" + DmConstants.INDEX_FILE, ""));
                    if (contentService.contentExists(fullPath)) {
                        RepositoryItem[] children = contentRepository.getContentChildren(fullPath);

                        if (children.length < 2) {
                            deployer.deleteFile(site, oldPath.replace("/" + DmConstants.INDEX_FILE, ""));
                        } else {
                            hasRenamedChildren = true;
                        }

                    }
                    if (!hasRenamedChildren) {
                        deleteFolder(site, oldPath.replace("/" + DmConstants.INDEX_FILE, ""), user, deployer);
                    }
                }

                LOGGER.debug("Clear renamed values for item at path: {0}", oldPath);
                LOGGER.warn("Clearing these before LIVE deploy may/will orphan {0}", oldPath);
                objectMetadataManager.clearRenamed(site, path);
            }


            boolean haschildren = false;
            deployer.deleteFile(site, path);


            if (item.getPath().endsWith("/" + DmConstants.INDEX_FILE)) {
                String fullPath = contentService.expandRelativeSitePath(site, path.replace("/" + DmConstants.INDEX_FILE, ""));
                if (contentService.contentExists(fullPath)) {
                    RepositoryItem[] children = contentRepository.getContentChildren(fullPath);

                    if (children.length < 2) {
                        deployer.deleteFile(site, path.replace("/" + DmConstants.INDEX_FILE, ""));
                    } else {
                        haschildren = true;
                    }
                }
            }

            if (contentService.contentExists(site, path)) {
                contentService.deleteContent(site, path, user);

                if (!haschildren) {
                    deleteFolder(site, path.replace("/" + DmConstants.INDEX_FILE, ""), user, deployer);
                }
            }
        }
        else {
            LOGGER.debug("Send {0} at path: {1}", action, path);

            LOGGER.debug("Setting system processing for {0}:{1}", site, path);
            objectStateService.setSystemProcessing(site, path, true);
            boolean contentExists = contentService.contentExists(site, path);
            
            if (isLive) {
                if (!importModeEnabled) {
                    String pubVersionComment = "Submitted by:" + user + ", " + submissionComment;
                    if (contentExists) {
                        contentRepository.createVersion(contentService.expandRelativeSitePath(site, path), pubVersionComment, true);
                    }
                }
                else {
                    LOGGER.debug("Import mode is ON. Create new version is skipped for [{0}] site \"{1}\"", path, site);
                }
            }
            
            
            
            if (StringUtils.equals(action, CopyToEnvironment.Action.MOVE)) {
                
                if (oldPath != null && oldPath.length() > 0) {
                    LOGGER.debug("Send MOVE for old item at path: {0}", oldPath);
                    Deployer deployer = deployerFactory.createEnvironmentStoreDeployer(environment);
                    deployer.deleteFile(site, oldPath);
                    
                    
                    if (oldPath.endsWith("/" + DmConstants.INDEX_FILE)) {
                        deleteFolder(site, oldPath.replace("/" + DmConstants.INDEX_FILE, ""), user, deployer);
                    }
                    
                    if (isLive) {
                        LOGGER.debug("Clear Renamed {0} on Live ENV", path);
                        objectMetadataManager.clearRenamed(site, path);
                    }
                    else {
                        LOGGER.debug("DONT Clear Renamed, ENV is not live {0}", path);
                    }
                }
            }
            
            if (contentExists) {
                LOGGER.debug("Getting deployer for environment store.");
                Deployer deployer = deployerFactory.createEnvironmentStoreDeployer(environment);
                deployer.deployFile(site, path);
            }


            ObjectMetadata objectMetadata = objectMetadataManager.getProperties(site, path);
            
            
            if (objectMetadata == null) {
                LOGGER.debug("No object state found for {0}:{1}, create it", site, path);
                objectMetadataManager.insertNewObjectMetadata(site, path);
                objectMetadata = objectMetadataManager.getProperties(site, path);
            }
            
            
            if(objectMetadata != null) {
                boolean sendEmail = objectMetadata.getSendEmail() == 1 ? true : false;
            
                if (sendEmail) {
                    String submittedByValue = objectMetadata.getSubmittedBy();
                
                    try {
                        LOGGER.debug("Sending approval notification for item site:{0} path:{1} user:{2}", site, path, user);
                        notificationService.sendApprovalNotification(site, submittedByValue, path, user);
                        LOGGER.debug("Sending approval notification SENT site:{0} path:{1} user:{2}", site, path, user);
                    }
                    catch(Exception eNotifyError) {
                        LOGGER.debug("Error sending approval notification site:{0} path:{1} user:{2}", site, path, user);
                    }
                }
            }
            else {
                LOGGER.error("Unable to get item metadata for {0}:{1}, can't notify", site, path);
            }

            if (isLive) {
                // should consider what should be done if this does not work. Currently the method will bail and the item is stuck in processing.
                LOGGER.debug("Environment is live, transition item to LIVE state {0}:{1}", site, path);
                ContentItemTO contentItem = contentService.getContentItem(site, path);
                objectStateService.transition(site, contentItem, TransitionEvent.DEPLOYMENT);
                if (objectMetadata != null) {
                    Map<String, Object> props = new HashMap<String, Object>();
                    props.put(ObjectMetadata.PROP_SUBMITTED_BY, StringUtils.EMPTY);
                    props.put(ObjectMetadata.PROP_SEND_EMAIL, 0);
                    props.put(ObjectMetadata.PROP_SUBMITTED_FOR_DELETION, 0);
                    props.put(ObjectMetadata.PROP_SUBMISSION_COMMENT, StringUtils.EMPTY);
                    objectMetadataManager.setObjectMetadata(site, path, props);
                }
            }
            
            LOGGER.debug("Resetting system processing for {0}:{1}", site, path);
            objectStateService.setSystemProcessing(site, path, false);
        }
    }


    private void deleteFolder(String site, String path, String user, Deployer deployer) {
        String fullPath = contentService.expandRelativeSitePath(site, path);
        if (contentService.contentExists(fullPath)) {
            RepositoryItem[] children = contentRepository.getContentChildren(fullPath);

            if (children.length < 1) {
                contentService.deleteContent(site, path, false, user);
                deployer.deleteFile(site, path);
                String parentPath = ContentUtils.getParentUrl(path);
                deleteFolder(site, parentPath, user, deployer);
            }
        } else {
            deployer.deleteFile(site, path);
            String parentPath = ContentUtils.getParentUrl(path);
            deleteFolder(site, parentPath, user, deployer);
        }
    }

    @Override
    @ValidateParams
    public void setupItemsForPublishingSync(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment, List<CopyToEnvironment> itemsToDeploy) throws DeploymentException {
        deploymentService.setupItemsForPublishingSync(site, environment, itemsToDeploy);
    }

    @Override
    public void insertDeploymentHistory(DeploymentEndpointConfigTO target, List<PublishToTarget> publishedItems, Date publishingDate) throws DeploymentException {
        deploymentService.insertDeploymentHistory(target, publishedItems, publishingDate);
    }

    @Override
    @ValidateParams
    public void markItemsCompleted(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment, List<CopyToEnvironment> processedItems) throws DeploymentException {
        for (CopyToEnvironment item : processedItems) {
            item.setState(CopyToEnvironment.State.COMPLETED);
            copyToEnvironmentMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    @ValidateParams
    public void markItemsProcessing(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment, List<CopyToEnvironment> itemsToDeploy) throws DeploymentException {
        for (CopyToEnvironment item : itemsToDeploy) {
            item.setState(CopyToEnvironment.State.PROCESSING);
            copyToEnvironmentMapper.updateItemDeploymentState(item);
        }
    }

    @Override
    @ValidateParams
    public void markItemsReady(@ValidateStringParam(name = "site") String site, @ValidateStringParam(name = "environment") String environment, List<CopyToEnvironment> copyToEnvironmentItems) throws DeploymentException {
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
                if (objectStateService.isNew(site, parentPath) || objectMetadataManager.isRenamed(site, parentPath)) {
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

            if (!enablePublishingWithoutDependencies) {
                Set<String> dependentPaths = null;
                try {
                    dependentPaths = dependencyService.getPublishingDepenencies(site, path);
                } catch (ServiceException e) {
                    throw new DeploymentException(e);
                }
                for (String dependentPath : dependentPaths) {
                    if (objectStateService.isNew(site, dependentPath) || objectMetadataManager.isRenamed(site, dependentPath) ) {
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

    public void setNotificationService2(final org.craftercms.studio.api.v2.service.notification.NotificationService
                                            notificationService2) {
        this.notificationService2 = notificationService2;
    }

    public boolean isEnablePublishingWithoutDependencies() { return enablePublishingWithoutDependencies; }
    public void setEnablePublishingWithoutDependencies(boolean enablePublishingWithoutDependencies) { this.enablePublishingWithoutDependencies = enablePublishingWithoutDependencies; }

    public DeploymentEventService getDeploymentEventService() { return deploymentEventService; }
    public void setDeploymentEventService(DeploymentEventService deploymentEventService) { this.deploymentEventService = deploymentEventService; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public DependencyService getDependencyService() { return dependencyService; }
    public void setDependencyService(DependencyService dependencyService) { this.dependencyService = dependencyService; }

    protected String indexFile;
    protected boolean importModeEnabled;
    protected SiteService siteService;
    protected ObjectStateService objectStateService;
    protected ContentService contentService;
    protected DeploymentService deploymentService;
    protected String environmentsStoreRootPath;
    protected DeployerFactory deployerFactory;
    protected ContentRepository contentRepository;
    protected ObjectMetadataManager objectMetadataManager;
    protected NotificationService notificationService;
    protected ServicesConfig servicesConfig;
    protected org.craftercms.studio.api.v2.service.notification.NotificationService notificationService2;
    protected boolean enablePublishingWithoutDependencies = false;
    protected DeploymentEventService deploymentEventService;
    protected SecurityProvider securityProvider;
    protected DependencyService dependencyService;

    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;
}
