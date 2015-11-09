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

package org.craftercms.studio.impl.v1.deployment;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.deployment.ContentNotFoundForPublishingException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.UploadFailedException;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SyncTargetDeployer implements Deployer {

    private final static Logger logger = LoggerFactory.getLogger(SyncTargetDeployer.class);

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
    public void deployFile(String site, String path) {

    }

    @Override
    public void deployFiles(String site, List<String> paths) {

    }

    @Override
    public void deleteFile(String site, String path) {

    }

    @Override
    public void deleteFiles(String site, List<String> paths) {

    }

    @Override
    public void deployFiles(String site, List<String> paths, List<String> deletedFiles) throws ContentNotFoundForPublishingException, UploadFailedException {
        logger.debug("Start deploying items for site \"{0}\", target \"{1}\", number of items \"{2}\"", site, endpointConfig.getName(), paths.size());
        URL requestUrl = null;
        try {
            requestUrl = new URL(endpointConfig.getServerUrl());
        } catch (MalformedURLException e) {
            logger.error("Invalid server URL for target {0}", endpointConfig.getName());
            throw new UploadFailedException(site, endpointConfig.getName(), endpointConfig.getServerUrl(), e);
        }

        ByteArrayPartSource baps = null;
        PartSource metadataPart = null;
        StringPart stringPart = null;
        FilePart filePart = null;

        // TODO: implement reactor version of deployment events
        int cntFiles = 0;
        StringBuilder sbDeletedFiles = new StringBuilder();
        List<Part> formParts = new ArrayList<Part>();

        formParts.add(new StringPart(PASSWORD_REQUEST_PARAMETER, endpointConfig.getPassword()));
        formParts.add(new StringPart(TARGET_REQUEST_PARAMETER, endpointConfig.getTarget()));
        String siteId = endpointConfig.getSiteId();
        if (StringUtils.isEmpty(siteId)) {
            siteId = site;
        }
        formParts.add(new StringPart(SITE_REQUEST_PARAMETER, siteId));

        logger.debug("Preparing deployment items for target {0}", endpointConfig.getName());
        for (String path : paths) {
            logger.debug("Parsing \"{0}\" , site \"{1}\"; for publishing on target \"{2}\"", path, site, endpointConfig.getName());
            logger.debug("Get content for \"{0}\" , site \"{1}\", environment \"{2}\"", path, site, environment);
            File file = new File(getDestinationPath(site, path, environment));
            InputStream input = null;
            try {
                input = FileUtils.openInputStream(file);
                if (input == null || input.available() < 0) {
                    if (file.exists() && !file.isDirectory()) {
                        baps = null;
                        stringPart = null;
                        filePart = null;
                        formParts = null;
                        throw new ContentNotFoundForPublishingException(site, endpointConfig.getName(), path);
                    } else {
                        // Content does not exist - skip deploying file
                        continue;
                    }
                }
            } catch (IOException err) {
                logger.error("Error reading input stream from envirnoment store for content at path: " + path + " site: " + site + " environment: " + environment);
                if (!file.exists()) {
                    logger.error("File expected, but does not exist at path: " + file.getAbsolutePath());
                }
                continue;
            }
            String fileName = file.getName();

            byte[] byteArray = null;

            try {
                byteArray = IOUtils.toByteArray(input);
            } catch (IOException e) {
                logger.error("Error while converting input stream to byte array", e);
                baps = null;
                stringPart = null;
                filePart = null;
                formParts = null;
            } finally {
                IOUtils.closeQuietly(input);
                input = null;
            }
            baps = new ByteArrayPartSource(fileName, byteArray);

            logger.debug("Create http request parameters for \"{0}\" , site \"{1}\"; publishing on target \"{2}\"", path, site, endpointConfig.getName());
            int idx = path.lastIndexOf("/");
            String relativePath = path.substring(0, idx + 1) + fileName;
            stringPart = new StringPart(CONTENT_LOCATION_REQUEST_PARAMETER + cntFiles, relativePath);
            formParts.add(stringPart);
            filePart = new FilePart(CONTENT_FILE_REQUEST_PARAMETER + cntFiles, baps);
            formParts.add(filePart);
            /*
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
            }*/
            cntFiles++;

// TODO: implement metadata transfer
        }

        for (int i = 0; i < deletedFiles.size(); i++) {
            if (i > 0) {
                sbDeletedFiles.append(FILES_SEPARATOR);
            }
            sbDeletedFiles.append(deletedFiles.get(i));
        }

        if (sbDeletedFiles.length() > 0) {
            formParts.add(new StringPart(DELETED_FILES_REQUEST_PARAMETER, sbDeletedFiles.toString()));
        }
        logger.debug("Create http request to deploy content for target {0} ({1})", endpointConfig.getName(), endpointConfig.getTarget());
        PostMethod postMethod = null;
        HttpClient client = null;
        try {
            logger.debug("Create HTTP Post Method");
            postMethod = new PostMethod(requestUrl.toString());
            postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
            Part[] parts = new Part[formParts.size()];
            for (int i = 0; i < formParts.size(); i++) parts[i] = formParts.get(i);
            postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
            client = new HttpClient();

            logger.debug("Execute HTTP POST request \"{0}\"", postMethod.getURI());
            int status = client.executeMethod(postMethod);
            if (status == HttpStatus.SC_OK) {
                logger.info("Successfully deployed on target {0}", endpointConfig.getName());
            } else {
                logger.error("Deployment failed for on target {1}. Deployment agent returned status {2}", endpointConfig.getName(), HttpStatus.getStatusText(status));
                throw new UploadFailedException(site, endpointConfig.getName(), endpointConfig.getServerUrl());
            }
        } catch (HttpException e) {
            logger.error("Publish failed for target {0} due to http protocol exception", endpointConfig.getName());
            throw new UploadFailedException(site, endpointConfig.getName(), endpointConfig.getServerUrl(), e);
        } catch (IOException e) {
            logger.error("Publish failed for target {0} due to I/O (transport) exception", endpointConfig.getName());
            throw new UploadFailedException(site, endpointConfig.getName(), endpointConfig.getServerUrl(), e);
        } finally {
            logger.debug("Release http connection and release resources");
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

        //LOGGER.debug("Publishing deployment event for target \"{0}\" with \"{1}\" items.", target.getName(), 0/*eventItems.size()*/);
        //contentRepository.publishDeployEvent(target.getName(), eventItems);

        logger.info("Deployment successful on target {0}", endpointConfig.getName());
        logger.debug("Finished deploying items for site \"{0}\", target \"{1}\", number of items \"{2}\"", site, endpointConfig.getName(), paths.size());
    }

    private String getDestinationPath(String site, String path, String environment) {
        return String.format("%s/%s/%s/%s", environmentsStoreRootPath, site, environment, path);
    }

    public String getEnvironmentsStoreRootPath() { return environmentsStoreRootPath; }
    public void setEnvironmentsStoreRootPath(String environmentsStoreRootPath) {
        this.environmentsStoreRootPath = environmentsStoreRootPath;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public DeploymentEndpointConfigTO getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(DeploymentEndpointConfigTO endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    protected String environmentsStoreRootPath;
    protected String environment;
    protected DeploymentEndpointConfigTO endpointConfig;
}
