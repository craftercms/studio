/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.impl.service.deployment;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEventItem;
import org.craftercms.cstudio.api.log.Logger;
import org.craftercms.cstudio.api.log.LoggerFactory;
import org.craftercms.cstudio.api.repository.ContentRepository;
import org.craftercms.cstudio.api.service.deployment.*;
import org.craftercms.cstudio.api.service.fsm.TransitionEvent;
import org.craftercms.cstudio.impl.service.deployment.dal.DeploymentDAL;
import org.craftercms.cstudio.impl.service.deployment.dal.DeploymentDALException;

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
    public Set<String> getAllAvailableSites() {
        return _contentRepository.getAllAvailableSites();
    }

    @Override
    public Set<PublishingTargetItem> getAllTargetsForSite(String site) {
        return _contentRepository.getAllTargetsForSite(site);
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
    public List<PublishingSyncItem> getItemsToSync(String site, long targetVersion, List<String> environments) {
        return _deploymentDAL.getItemsReadyForTargetSync(site, targetVersion, environments);
    }

    @Override
    public void deployItemsToTarget(String site, List<PublishingSyncItem> filteredItems, PublishingTargetItem target) throws ContentNotFoundForPublishingException, UploadFailedException {
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
        Iterator<PublishingSyncItem> iter = filteredItems.iterator();
        LOGGER.debug("Divide all deployment items into {0} bucket(s) for  target {1}", numberOfBuckets , target.getName());
        List<DeploymentEventItem> eventItems = new ArrayList<DeploymentEventItem>();
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

                    PublishingSyncItem item = iter.next();
                    LOGGER.debug("Parsing \"{0}\" , site \"{1}\"; for publishing on target \"{2}\"", item.getPath(), item.getSite(), target.getName());
                    DeploymentEventItem eventItem = new DeploymentEventItem();
                    eventItem.setSite(item.getSite());
                    eventItem.setPath(item.getPath());
                    eventItem.setUser(item.getUser());
                    eventItem.setDateTime(new Date());

                    if (item.getAction() == PublishingSyncItem.Action.DELETE) {
                        eventItem.setState(DeploymentEventItem.STATE_DELETED);
                        if (sbDeletedFiles.length() > 0) {
                            sbDeletedFiles.append(FILES_SEPARATOR).append(item.getPath());
                        } else {
                            sbDeletedFiles.append(item.getPath());
                        }
                        if (item.getPath().endsWith("/" + _indexFile)) {
                            sbDeletedFiles.append(FILES_SEPARATOR).append(item.getPath().replace("/" + _indexFile, ""));
                        }
                    } else {

                        if (item.getAction() == PublishingSyncItem.Action.NEW) {
                            eventItem.setState(DeploymentEventItem.STATE_NEW);
                        } else if (item.getAction() == PublishingSyncItem.Action.MOVE) {
                            eventItem.setState(DeploymentEventItem.STATE_MOVED);
                        } else {
                            eventItem.setState(DeploymentEventItem.STATE_UPDATED);
                        }

                        LOGGER.debug("Get content for \"{0}\" , site \"{1}\"", item.getPath(), item.getSite());
                        InputStream input = _contentRepository.getContent(site, null, item.getEnvironment(), item.getPath());
                        try {
                            if (input == null || input.available() < 0) {
                                if (!_contentRepository.isFolder(site, item.getPath()) && _contentRepository.contentExists(site, item.getPath())) {
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
                            if (_contentRepository.contentExists(site, item.getPath())) {
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
                        String fileName = _contentRepository.getFilename(site, item.getPath());

                        byte[] byteArray = null;

                        try {
                            byteArray = IOUtils.toByteArray(input);
                        } catch (IOException e) {
                            LOGGER.error("Error while converting input stream to byte array", e);
                            baps = null;
                            stringPart = null;
                            filePart = null;
                            formParts = null;
                            if (_contentRepository.contentExists(site, item.getPath())) {
                                throw new ContentNotFoundForPublishingException(site, target.getName(), item.getPath());
                            } else {
                                // Content does not exist - skip deploying file
                                continue;
                            }
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
                        if (item.getAction() == PublishingSyncItem.Action.MOVE) {
                            if (item.getOldPath() != null && !item.getOldPath().equalsIgnoreCase(item.getPath())) {
                                LOGGER.debug("Add old path to be deleted for MOVE action (\"{0}\")", item.getOldPath());
                                eventItem.setOldPath(item.getOldPath());
                                if (sbDeletedFiles.length() > 0) {
                                    sbDeletedFiles.append(",").append(item.getOldPath());
                                } else {
                                    sbDeletedFiles.append(item.getOldPath());
                                }
                                if (item.getOldPath().endsWith("/" + _indexFile)) {
                                    sbDeletedFiles.append(FILES_SEPARATOR).append(item.getOldPath().replace("/" + _indexFile, ""));
                                }
                            }
                        }

                        if (target.isSendMetadata()) {
                            LOGGER.debug("Adding meta data for content \"{0}\" site \"{0}\"", item.getPath(), item.getSite());
                            InputStream metadataStream = null;
                            try {
                                metadataStream = _contentRepository.getMetadataStream(site, item.getPath());
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
                    }
                    cntFiles++;
                    eventItems.add(eventItem);
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

        LOGGER.debug("Publishing deployment event for target \"{0}\" with \"{1}\" items.", target.getName(), eventItems.size());
        _contentRepository.publishDeployEvent(target.getName(), eventItems);

        LOGGER.info("Deployment successful on target {0}", target.getName());
        LOGGER.debug("Finished deploying items for site \"{0}\", target \"{1}\", number of items \"{2}\"", site, target.getName(), filteredItems.size());
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
    public List<CopyToEnvironmentItem> getItemsReadyForDeployment(String site, String environment) {
        return _deploymentDAL.getItemsReadyForDeployment(site, environment);
    }

    @Override
    public void processItem(CopyToEnvironmentItem item) throws DeploymentException {
        String liveEnvironment = _contentRepository.getLiveEnvironmentName(item.getSite());
        boolean isLive = false;
        if (StringUtils.isNotEmpty(liveEnvironment)) {
            if (liveEnvironment.equals(item.getEnvironment())) {
                isLive = true;
            }
        } else if (LIVE_ENVIRONMENT.equalsIgnoreCase(item.getEnvironment()) || PRODUCTION_ENVIRONMENT.equalsIgnoreCase(item.getEnvironment())) {
            isLive = true;
        }
        if (item.getAction() == CopyToEnvironmentItem.Action.DELETE) {
            if (item.getOldPath() != null && item.getOldPath().length() > 0) {
                _contentRepository.deleteContent(item.getSite(), item.getEnvironment(), item.getOldPath());
                _contentRepository.clearRenamed(item.getSite(), item.getPath());
            }
            _contentRepository.deleteContent(item.getSite(), item.getEnvironment(), item.getPath());
            if (isLive) {
                _contentRepository.deleteContent(item);
            }
        } else {
            _contentRepository.setSystemProcessing(item.getSite(), item.getPath(), true);
            if (isLive) {
                if (!_importModeEnabled) {
                    _contentRepository.createNewVersion(item.getSite(), item.getPath(), item.getSubmissionComment(), true);
                } else {
                    LOGGER.debug("Import mode is ON. Create new version is skipped for [{0}] site \"{1}\"", item.getPath(), item.getSite());
                }
            }
            if (item.getAction() == CopyToEnvironmentItem.Action.MOVE) {
                if (item.getOldPath() != null && item.getOldPath().length() > 0) {
                    _contentRepository.deleteContent(item.getSite(), item.getEnvironment(), item.getOldPath());
                    if (isLive) {
                        _contentRepository.clearRenamed(item.getSite(), item.getPath());
                    }
                }
            }
            _contentRepository.copyToEnvironment(item.getSite(), item.getEnvironment(), item.getPath());
            if (isLive) {
                _contentRepository.stateTransition(item.getSite(), item.getPath(), TransitionEvent.DEPLOYMENT);
            }
            _contentRepository.setSystemProcessing(item.getSite(), item.getPath(), false);
        }
    }

    @Override
    public void setLockBehaviourEnabled(boolean enabled) {
        _contentRepository.setLockBehaviourEnabled(enabled);
    }

    @Override
    public void setupItemsForPublishingSync(String site, String environment, List<CopyToEnvironmentItem> itemsToDeploy) throws DeploymentException {
        _deploymentDAL.setupItemsForPublishingSync(site, environment, itemsToDeploy);
    }

    @Override
    public void insertDeploymentHistory(PublishingTargetItem target, List<PublishingSyncItem> publishedItems, Date publishingDate) throws DeploymentException {
        _deploymentDAL.insertDeploymentHistory(target, publishedItems, publishingDate);
    }

    @Override
    public void markItemsCompleted(String site, String environment, List<CopyToEnvironmentItem> processedItems) throws DeploymentException {
        _deploymentDAL.markItemsCompleted(site, environment, processedItems);
    }

    @Override
    public void markItemsProcessing(String site, String environment, List<CopyToEnvironmentItem> itemsToDeploy) throws DeploymentException {
        _deploymentDAL.markItemsProcessing(site, environment, itemsToDeploy);
    }

    @Override
    public void markItemsReady(String site, String environment, List<CopyToEnvironmentItem> copyToEnvironmentItems) throws DeploymentException {
        _deploymentDAL.markItemsReady(site, environment, copyToEnvironmentItems);
    }

    @Override
    public List<CopyToEnvironmentItem> processMandatoryDependencies(CopyToEnvironmentItem item, List<String> pathsToDeploy, Set<String> missingDependenciesPaths) throws DeploymentException {
        List<CopyToEnvironmentItem> mandatoryDependencies = new ArrayList<CopyToEnvironmentItem>();
        String site = item.getSite();
        String path = item.getPath();
        if (item.getAction() == CopyToEnvironmentItem.Action.NEW || item.getAction() == CopyToEnvironmentItem.Action.MOVE) {
            String helpPath = path.replace("/" + _indexFile, "");
            int idx = helpPath.lastIndexOf("/");
            String parentPath = helpPath.substring(0, idx) + "/" + _indexFile;
            if (_contentRepository.isNew(site, parentPath) || _contentRepository.isRenamed(site, parentPath)) {
                String parentFullPath = _contentRepository.getFullPath(site, parentPath);
                if (!missingDependenciesPaths.contains(parentFullPath) && !pathsToDeploy.contains(parentFullPath)) {
                    try {
                        _deploymentDAL.cancelWorkflow(site, parentPath);
                    } catch (DeploymentDALException e) {
                        LOGGER.error("Error while canceling workflow for path {0}, site {1}", e, site, parentPath);
                    }
                    missingDependenciesPaths.add(parentFullPath);
                    CopyToEnvironmentItem parentItem = createMissingItem(site, parentPath, item);
                    processItem(parentItem);
                    mandatoryDependencies.add(parentItem);
                    mandatoryDependencies.addAll(processMandatoryDependencies(parentItem, pathsToDeploy, missingDependenciesPaths));
                }
            }

            List<String> dependentPaths = _contentRepository.getDependentPaths(site, path);
            for (String dependentPath : dependentPaths) {
                if (_contentRepository.isNew(site, dependentPath) || _contentRepository.isRenamed(site, dependentPath)) {
                    String dependentFullPath = _contentRepository.getFullPath(site, dependentPath);
                    if (!missingDependenciesPaths.contains(dependentFullPath) && !pathsToDeploy.contains(dependentFullPath)) {
                        try {
                            _deploymentDAL.cancelWorkflow(site, dependentPath);
                        } catch (DeploymentDALException e) {
                            LOGGER.error("Error while canceling workflow for path {0}, site {1}", e, site, dependentPath);
                        }
                        missingDependenciesPaths.add(dependentFullPath);
                        CopyToEnvironmentItem dependentItem = createMissingItem(site, dependentPath, item);
                        processItem(dependentItem);
                        mandatoryDependencies.add(dependentItem);
                        mandatoryDependencies.addAll(processMandatoryDependencies(dependentItem, pathsToDeploy, missingDependenciesPaths));
                    }
                }
            }
        }

        return mandatoryDependencies;
    }

    private CopyToEnvironmentItem createMissingItem(String site, String itemPath, CopyToEnvironmentItem item) {
        CopyToEnvironmentItem missingItem = new CopyToEnvironmentItem();
        missingItem.setSite(site);
        missingItem.setEnvironment(item.getEnvironment());
        missingItem.setPath(itemPath);
        missingItem.setScheduledDate(item.getScheduledDate());
        missingItem.setState(item.getState());
        if (_contentRepository.isNew(site, itemPath)) {
            missingItem.setAction(CopyToEnvironmentItem.Action.NEW);
        }
        if (_contentRepository.isRenamed(site, itemPath)) {
            String oldPath = _contentRepository.getOldPath(site, itemPath);
            missingItem.setOldPath(oldPath);
            missingItem.setAction(CopyToEnvironmentItem.Action.MOVE);
        }
        String contentTypeClass = _contentRepository.getContentTypeClass(site, itemPath);
        missingItem.setContentTypeClass(contentTypeClass);
        missingItem.setUser(item.getUser());
        missingItem.setSubmissionComment(item.getSubmissionComment());
        return missingItem;
    }

    public ContentRepository getContentRepository() { return _contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this._contentRepository = contentRepository; }

    public DeploymentDAL getDeploymentDal() { return _deploymentDAL; }
    public void setDeploymentDAL(DeploymentDAL deploymentDAL) { this._deploymentDAL = deploymentDAL; }

    public String getIndexFile() {  return _indexFile; }
    public void setIndexFile(String indexFile) { this._indexFile = indexFile; }

    public boolean isImportModeEnabled() { return _importModeEnabled; }
    public void setImportModeEnabled(boolean importModeEnabled) {
        this._importModeEnabled = importModeEnabled;
        LOGGER.info("Import mode is {0}. Creating new version when deploying content is {1}", _importModeEnabled ? "ON" : "OFF", _importModeEnabled ? "DISABLED" : "ENABLED");
    }

    protected ContentRepository _contentRepository;
    protected DeploymentDAL _deploymentDAL;
    protected String _indexFile;
    protected boolean _importModeEnabled;
}