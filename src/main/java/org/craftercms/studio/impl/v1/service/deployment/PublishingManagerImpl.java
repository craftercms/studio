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
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.dal.CopyToEnvironment;
import org.craftercms.studio.api.v1.dal.CopyToEnvironmentMapper;
import org.craftercms.studio.api.v1.dal.PublishToTarget;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.deployment.*;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import org.craftercms.studio.api.v1.to.PublishingChannelConfigTO;
import org.craftercms.studio.api.v1.to.PublishingChannelGroupConfigTO;
import org.craftercms.studio.impl.v1.deployment.EnvironmentStoreDeployer;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
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
    public Set<PublishingTargetItem> getAllTargetsForSite(String site) {
        Map<String, PublishingChannelGroupConfigTO> groupConfigTOs = siteService.getPublishingChannelGroupConfigs(site);
        Set<PublishingTargetItem> targets = new HashSet<PublishingTargetItem>();
        Map<String, PublishingTargetItem> targetMap = new HashMap<String, PublishingTargetItem>();
        if (groupConfigTOs != null && groupConfigTOs.size() > 0) {
            for (PublishingChannelGroupConfigTO groupConfigTO : groupConfigTOs.values()) {
                List<PublishingChannelConfigTO> channelConfigTOs = groupConfigTO.getChannels();
                if (channelConfigTOs != null && channelConfigTOs.size() > 0) {
                    for (PublishingChannelConfigTO channelConfigTO : channelConfigTOs) {
                        DeploymentEndpointConfigTO endpoint = siteService.getDeploymentEndpoint(site, channelConfigTO.getName());
                        if (endpoint != null) {
                            PublishingTargetItem targetItem = targetMap.get(endpoint.getName());
                            if (targetItem == null) {
                                targetItem = new PublishingTargetItem();
                                targetItem.setId(endpoint.getName());
                                targetItem.setName(endpoint.getName());
                                targetItem.setTarget(endpoint.getTarget());
                                targetItem.setType(endpoint.getType());
                                targetItem.setServerUrl(endpoint.getServerUrl());
                                targetItem.setStatusUrl(endpoint.getStatusUrl());
                                targetItem.setVersionUrl(endpoint.getVersionUrl());
                                targetItem.setPassword(endpoint.getPassword());
                                targetItem.setExcludePattern(endpoint.getExcludePattern());
                                targetItem.setIncludePattern(endpoint.getIncludePattern());
                                targetItem.setBucketSize(endpoint.getBucketSize());
                                targetItem.setSiteId(endpoint.getSiteId());
                                targetItem.setSendMetadata(endpoint.isSendMetadata());
                                targets.add(targetItem);
                                targetMap.put(endpoint.getName(), targetItem);
                            }
                            targetItem.addEnvironment(groupConfigTO.getName());
                        }
                    }
                }
            }
        }
        return targets;
    }

    @Override
    public boolean checkConnection(PublishingTargetItem target) {
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
    public long getTargetVersion(PublishingTargetItem target, String site) {
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
                    String responseText = getMethod.getResponseBodyAsString();
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
    public void deployItemsToTarget(String site, List<PublishToTarget> filteredItems, PublishingTargetItem target) throws ContentNotFoundForPublishingException, UploadFailedException {
        LOGGER.debug("Start deploying items for site \"{0}\", target \"{1}\", number of items \"{2}\"", site, target.getName(), filteredItems.size());
        URL requestUrl = null;
        try {
            requestUrl = new URL(target.getServerUrl());
        } catch (MalformedURLException e) {
            LOGGER.error("Invalid server URL for target {0}", target.getName());
            throw new UploadFailedException(site, target.getName(), target.getServerUrl(), e);
        }

        ByteArrayPartSource baps = null;
        PartSource metadataPart = null;
        StringPart stringPart = null;
        FilePart filePart = null;

        int numberOfBuckets = filteredItems.size() / target.getBucketSize() + 1;
        Iterator<PublishToTarget> iter = filteredItems.iterator();
        LOGGER.debug("Divide all deployment items into {0} bucket(s) for  target {1}", numberOfBuckets , target.getName());
        // TODO: implement reactor version of deployment events
        //List<DeploymentEventItem> eventItems = new ArrayList<DeploymentEventItem>();
        for (int bucketIndex = 0; bucketIndex < numberOfBuckets; bucketIndex++) {
            int cntFiles = 0;
            StringBuilder sbDeletedFiles = new StringBuilder();
            List<Part> formParts = new ArrayList<Part>();

            formParts.add(new StringPart(PASSWORD_REQUEST_PARAMETER, target.getPassword()));
            formParts.add(new StringPart(TARGET_REQUEST_PARAMETER, target.getTarget()));
            String siteId = target.getSiteId();
            if (StringUtils.isEmpty(siteId)) {
                siteId = site;
            }
            formParts.add(new StringPart(SITE_REQUEST_PARAMETER, siteId));

            LOGGER.debug("Preparing deployment items (bucket {0}) for target {1}", bucketIndex + 1, target.getName());

            int loopSize = (filteredItems.size() - (bucketIndex * target.getBucketSize()) > target.getBucketSize()) ? target.getBucketSize() : filteredItems.size() - bucketIndex * target.getBucketSize();
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
                        if (sbDeletedFiles.length() > 0) {
                            sbDeletedFiles.append(FILES_SEPARATOR).append(item.getPath());
                        } else {
                            sbDeletedFiles.append(item.getPath());
                        }
                        if (item.getPath().endsWith("/" + indexFile)) {
                            String folderPath = item.getPath().replace("/" + indexFile, "");
                            /*
                            if (contentRepository.numberOfChildren(site, folderPath) < 1) {
                                sbDeletedFiles.append(FILES_SEPARATOR).append(folderPath);
                            }*/
                        }
                    } else {

                        if (StringUtils.equals(item.getAction(), PublishToTarget.Action.NEW)) {
                            //eventItem.setState(DeploymentEventItem.STATE_NEW);
                        } else if (StringUtils.equals(item.getAction(), PublishToTarget.Action.MOVE)) {
                            //eventItem.setState(DeploymentEventItem.STATE_MOVED);
                        } else {
                            //eventItem.setState(DeploymentEventItem.STATE_UPDATED);
                        }

                        LOGGER.debug("Get content for \"{0}\" , site \"{1}\"", item.getPath(), item.getSite());
                        File file = new File(getDestinationPath(site, item.getPath(), item.getEnvironment()));
                        InputStream input = null;
                        try {
                            input = FileUtils.openInputStream(file);
                            if (input == null || input.available() < 0) {
                                if (false /*!contentService.isFolder(site, item.getPath()) /*&& contentRepository.contentExists(site, item.getPath())*/) {
                                    baps = null;
                                    stringPart = null;
                                    filePart = null;
                                    formParts = null;
                                    throw new ContentNotFoundForPublishingException(site, target.getName(), item.getPath());
                                } else {
                                    // Content does not exist - skip deploying file
                                    continue;
                                }
                            }
                        } catch (IOException err) {
                            LOGGER.error("Error reading input stream for content at path: " + item.getPath() + " site: " + item.getSite());
                            /*
                            if (contentRepository.contentExists(site, item.getPath())) {
                                baps = null;
                                stringPart = null;
                                filePart = null;
                                formParts = null;
                                throw new ContentNotFoundForPublishingException(site, target.getName(), item.getPath());
                            } else {
                                // Content does not exist - skip deploying file
                                continue;
                            }*/
                        }
                        String fileName = file.getName();

                        byte[] byteArray = null;

                        try {
                            byteArray = IOUtils.toByteArray(input);
                        } catch (IOException e) {
                            LOGGER.error("Error while converting input stream to byte array", e);
                            baps = null;
                            stringPart = null;
                            filePart = null;
                            formParts = null;
                            /*
                            if (contentRepository.contentExists(site, item.getPath())) {
                                throw new ContentNotFoundForPublishingException(site, target.getName(), item.getPath());
                            } else {
                                // Content does not exist - skip deploying file
                                continue;
                            }*/
                        }
                        finally {
                            IOUtils.closeQuietly(input);
                            input = null;
                        }
                        baps = new ByteArrayPartSource(fileName, byteArray);

                        LOGGER.debug("Create http request parameters for \"{0}\" , site \"{1}\"; publishing on target \"{2}\"", item.getPath(), item.getSite(), target.getName());
                        int idx = item.getPath().lastIndexOf("/");
                        String relativePath = item.getPath().substring(0, idx + 1) + fileName;
                        stringPart = new StringPart(CONTENT_LOCATION_REQUEST_PARAMETER + cntFiles, relativePath);
                        formParts.add(stringPart);
                        filePart = new FilePart(CONTENT_FILE_REQUEST_PARAMETER + cntFiles, baps);
                        formParts.add(filePart);
                        if (StringUtils.equals(item.getAction(), PublishToTarget.Action.MOVE)) {
                            if (item.getOldPath() != null && !item.getOldPath().equalsIgnoreCase(item.getPath())) {
                                LOGGER.debug("Add old path to be deleted for MOVE action (\"{0}\")", item.getOldPath());
                                //eventItem.setOldPath(item.getOldPath());
                                if (sbDeletedFiles.length() > 0) {
                                    sbDeletedFiles.append(",").append(item.getOldPath());
                                } else {
                                    sbDeletedFiles.append(item.getOldPath());
                                }
                                if (item.getOldPath().endsWith("/" + indexFile)) {
                                    sbDeletedFiles.append(FILES_SEPARATOR).append(item.getOldPath().replace("/" + indexFile, ""));
                                }
                            }
                        }
/* TODO: implement metadata transfer
                        if (target.isSendMetadata()) {
                            LOGGER.debug("Adding meta data for content \"{0}\" site \"{0}\"", item.getPath(), item.getSite());
                            InputStream metadataStream = null;
                            try {
                                //metadataStream = contentRepository.getMetadataStream(site, item.getPath());
                                metadataPart = new ByteArrayPartSource(fileName + ".meta", IOUtils.toByteArray(metadataStream));
                                formParts.add(new FilePart(METADATA_FILE_REQUEST_PARAMETER + cntFiles, metadataPart));
                            } catch (IOException e) {
                                LOGGER.error("Error while creating input stream with content metadata", e);
                                baps = null;
                                stringPart = null;
                                filePart = null;
                                formParts = null;
                            }
                            finally {
                                IOUtils.closeQuietly(metadataStream);
                                metadataPart = null;
                            }
                        }
                        */
                    }
                    cntFiles++;
                    //eventItems.add(eventItem);
                }
            }


            if (sbDeletedFiles.length() > 0) {
                formParts.add(new StringPart(DELETED_FILES_REQUEST_PARAMETER, sbDeletedFiles.toString()));
            }
            LOGGER.debug("Create http request to deploy bucket {0} for target {1}", bucketIndex + 1, target.getName());

            PostMethod postMethod = null;
            HttpClient client = null;
            try {

                LOGGER.debug("Create HTTP Post Method");
                postMethod = new PostMethod(requestUrl.toString());
                postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
                Part[] parts = new Part[formParts.size()];
                for (int i = 0; i < formParts.size(); i++) parts[i] = formParts.get(i);
                postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
                client = new HttpClient();

                LOGGER.debug("Execute HTTP POST request \"{0}\"", postMethod.getURI());
                int status = client.executeMethod(postMethod);
                if (status == HttpStatus.SC_OK) {
                    LOGGER.info("Successfully deployed bucket number {0} on target {1}", bucketIndex + 1, target.getName());
                } else {
                    LOGGER.error("Deployment failed for bucket number {0} on target {1}. Deployment agent returned status {2}", bucketIndex + 1, target.getName(), HttpStatus.getStatusText(status));
                    throw new UploadFailedException(site, target.getName(), target.getServerUrl());
                }
            } catch (HttpException e) {
                LOGGER.error("Publish failed for target {0} due to http protocol exception", target.getName());
                throw new UploadFailedException(site, target.getName(), target.getServerUrl(), e);
            } catch (IOException e) {
                LOGGER.error("Publish failed for target {0} due to I/O (transport) exception", target.getName());
                throw new UploadFailedException(site, target.getName(), target.getServerUrl(), e);
            } finally {
                LOGGER.debug("Release http connection and release resources");
                if (client != null) {
                    HttpConnectionManager mgr = client.getHttpConnectionManager();
                    if (mgr instanceof SimpleHttpConnectionManager) {
                        ((SimpleHttpConnectionManager)mgr).shutdown();
                    }
                }
                if (postMethod != null) {
                    postMethod.releaseConnection();
                    postMethod = null;
                    client = null;
                }
                baps = null;
                stringPart = null;
                filePart = null;
                formParts = null;
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
    public long setTargetVersion(PublishingTargetItem target, long newVersion, String site) {
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
                    String responseText = postMethod.getResponseBodyAsString();
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
            if (item.getOldPath() != null && item.getOldPath().length() > 0) {
                //contentRepository.deleteContent(item.getSite(), item.getEnvironment(), item.getOldPath());
                //contentRepository.clearRenamed(item.getSite(), item.getPath());
            }
            //contentRepository.deleteContent(item.getSite(), item.getEnvironment(), item.getPath());
            if (isLive) {
                //contentRepository.deleteContent(item);
            }
        } else {
            //contentRepository.setSystemProcessing(item.getSite(), item.getPath(), true);
            if (isLive) {
                if (!importModeEnabled) {
                    //contentRepository.createNewVersion(item.getSite(), item.getPath(), item.getSubmissionComment(), true);
                } else {
                    LOGGER.debug("Import mode is ON. Create new version is skipped for [{0}] site \"{1}\"", item.getPath(), item.getSite());
                }
            }
            if (StringUtils.equals(item.getAction(), CopyToEnvironment.Action.MOVE)) {
                if (item.getOldPath() != null && item.getOldPath().length() > 0) {
                    //contentRepository.deleteContent(item.getSite(), item.getEnvironment(), item.getOldPath());
                    if (isLive) {
                        //contentRepository.clearRenamed(item.getSite(), item.getPath());
                    }
                }
            }
            //contentRepository.copyToEnvironment(item.getSite(), item.getEnvironment(), item.getPath());
            environmentStoreDeployer.deployFile(item.getSite(), item.getPath(), item.getEnvironment());
            if (isLive) {
                //contentRepository.stateTransition(item.getSite(), item.getPath(), org.craftercms.studio.api.v1.service.fsm.TransitionEvent.DEPLOYMENT);
            }
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
    public void insertDeploymentHistory(PublishingTargetItem target, List<PublishToTarget> publishedItems, Date publishingDate) throws DeploymentException {
        deploymentService.insertDeploymentHistory(target, publishedItems, publishingDate);
        //_deploymentDAL.insertDeploymentHistory(target, publishedItems, publishingDate);
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
            String helpPath = path.replace("/" + indexFile, "");
            int idx = helpPath.lastIndexOf("/");
            String parentPath = helpPath.substring(0, idx) + "/" + indexFile;
            if (objectStateService.isNew(site, parentPath) /* TODO: check renamed || objectStateService.isRenamed(site, parentPath) */ ) {
                String parentFullPath = contentService.expandRelativeSitePath(site, parentPath);
                if (!missingDependenciesPaths.contains(parentFullPath) && !pathsToDeploy.contains(parentFullPath)) {
                    cancelWorkflow(site, parentPath);
                    missingDependenciesPaths.add(parentFullPath);
                    CopyToEnvironment parentItem = createMissingItem(site, parentPath, item);
                    processItem(parentItem);
                    mandatoryDependencies.add(parentItem);
                    mandatoryDependencies.addAll(processMandatoryDependencies(parentItem, pathsToDeploy, missingDependenciesPaths));
                }
            }

            List<String> dependentPaths = dmDependencyService.getDependencyPaths(site, path);
            for (String dependentPath : dependentPaths) {
                if (objectStateService.isNew(site, dependentPath) /* TODO: check renamed || contentRepository.isRenamed(site, dependentPath) */) {
                    String dependentFullPath = contentService.expandRelativeSitePath(site, dependentPath);
                    if (!missingDependenciesPaths.contains(dependentFullPath) && !pathsToDeploy.contains(dependentFullPath)) {
                        /*
                        try {
                            _deploymentDAL.cancelWorkflow(site, dependentPath);
                        } catch (DeploymentDALException e) {
                            LOGGER.error("Error while canceling workflow for path {0}, site {1}", e, site, dependentPath);
                        }*/
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
        /* TODO: check for renamed
        if (contentRepository.isRenamed(site, itemPath)) {
            String oldPath = contentRepository.getOldPath(site, itemPath);
            missingItem.setOldPath(oldPath);
            missingItem.setAction(CopyToEnvironment.Action.MOVE);
        }*/
        String contentTypeClass = contentService.getContentType(site, itemPath);
        missingItem.setContentTypeClass(contentTypeClass);
        missingItem.setUser(item.getUser());
        missingItem.setSubmissionComment(item.getSubmissionComment());
        return missingItem;
    }

    protected void cancelWorkflow(String site, String path) throws DeploymentException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("site", site);
        params.put("path", path);
        params.put("state", CopyToEnvironmentItem.State.READY_FOR_LIVE);
        params.put("canceledState", CopyToEnvironmentItem.State.CANCELED);
        params.put("now", new Date());
        copyToEnvironmentMapper.cancelWorkflow(params);
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

    public EnvironmentStoreDeployer getEnvironmentStoreDeployer() { return environmentStoreDeployer; }
    public void setEnvironmentStoreDeployer(EnvironmentStoreDeployer environmentStoreDeployer) { this.environmentStoreDeployer = environmentStoreDeployer; }

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

    protected String indexFile;
    protected boolean importModeEnabled;
    protected SiteService siteService;
    protected EnvironmentStoreDeployer environmentStoreDeployer;
    protected org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService;
    protected ContentService contentService;
    protected DmDependencyService dmDependencyService;
    protected DeploymentService deploymentService;
    protected String environmentsStoreRootPath;

    @Autowired
    protected CopyToEnvironmentMapper copyToEnvironmentMapper;
}