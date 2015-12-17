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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.*;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.ebus.annotations.EListener;
import org.craftercms.commons.ebus.annotations.EventHandler;
import org.craftercms.commons.ebus.annotations.EventSelectorType;
import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.ebus.EBusConstants;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.ebus.RepositoryEventMessage;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;
import reactor.event.Event;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@EListener
public class PreviewDeployer implements Deployer {

    private final static Logger logger = LoggerFactory.getLogger(PreviewDeployer.class);

    public static final String METADATA_EXTENSION = ".depmeta";
    public static final String DEPLOYER_SERVLET_URL = "/publish";
    public static final String DEPLOYER_PASSWORD_PARAM = "password";
    public static final String DEPLOYER_TARGET_PARAM = "target";
    public static final String DEPLOYER_SITE_PARAM = "siteId";
    public static final String DEPLOYER_DELETED_FILES_PARAM = "deletedFiles";
    public static final String DEPLOYER_CONTENT_LOCATION_PARAM = "contentLocation";
    public static final String DEPLOYER_CONTENT_FILE_PARAM = "contentFile";
    public static final String DEPLOYER_METADATA_FILE_PARAM = "metadataFile";
    public static final String FILES_SEPARATOR = ",";

    @Override
    public void deployFile(String site, String path) {
        DeploymentEndpointConfigTO deploymentEndpointConfigTO = siteService.getPreviewDeploymentEndpoint(site);
        URL requestUrl = null;

        try {
            String url = DEPLOYER_SERVLET_URL;
            List<Part> formParts = new ArrayList<>();
            if (deploymentEndpointConfigTO != null) {
                requestUrl = new URL(deploymentEndpointConfigTO.getServerUrl());
                formParts.add(new StringPart(DEPLOYER_PASSWORD_PARAM, deploymentEndpointConfigTO.getPassword()));
                formParts.add(new StringPart(DEPLOYER_TARGET_PARAM, deploymentEndpointConfigTO.getTarget()));
            } else {
                requestUrl = new URL("http", defaultServer, defaultPort, url);
                formParts.add(new StringPart(DEPLOYER_PASSWORD_PARAM, defaultPassword));
                formParts.add(new StringPart(DEPLOYER_TARGET_PARAM, defaultTarget));
            }

            InputStream content = contentService.getContent(site, path);
            if(content != null) {
                ByteArrayPartSource baps = null;
                byte[] byteArray = null;
                byteArray = IOUtils.toByteArray(content);

                baps = new ByteArrayPartSource(path, byteArray);
                formParts.add(new FilePart(DEPLOYER_CONTENT_FILE_PARAM, baps));
            }

            formParts.add(new StringPart(DEPLOYER_CONTENT_LOCATION_PARAM, path));
            formParts.add(new StringPart(DEPLOYER_SITE_PARAM, site));

            PostMethod postMethod = new PostMethod(requestUrl.toString());
            postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);

            Part[] parts = new Part[formParts.size()];

            for (int i = 0; i < formParts.size(); i++) parts[i] = formParts.get(i);
            postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
            HttpClient client = new HttpClient();
            int status = client.executeMethod(postMethod);
            postMethod.releaseConnection();
        }
        catch(Exception err) {
            logger.error("error while preview deploying '" + site + ":" + path + "'", err);
        }
    }

    @Override
    public void deployFiles(String site, List<String> paths) {

    }

    @Override
    public void deleteFile(String site, String path) {

        DeploymentEndpointConfigTO deploymentEndpointConfigTO = siteService.getPreviewDeploymentEndpoint(site);
        URL requestUrl = null;

        try {
            String url = DEPLOYER_SERVLET_URL;
            List<Part> formParts = new ArrayList<>();
            if (deploymentEndpointConfigTO != null) {
                requestUrl = new URL(deploymentEndpointConfigTO.getServerUrl());
                formParts.add(new StringPart(DEPLOYER_PASSWORD_PARAM, deploymentEndpointConfigTO.getPassword()));
                formParts.add(new StringPart(DEPLOYER_TARGET_PARAM, deploymentEndpointConfigTO.getTarget()));
            } else {
                requestUrl = new URL("http", defaultServer, defaultPort, url);
                formParts.add(new StringPart(DEPLOYER_PASSWORD_PARAM, defaultPassword));
                formParts.add(new StringPart(DEPLOYER_TARGET_PARAM, defaultTarget));
            }

            StringBuilder sbDeletedFiles = new StringBuilder(path);
            if (path.endsWith("/index.xml")) {
                RepositoryItem[] children = contentRepository.getContentChildren(contentService.expandRelativeSitePath(site, path.replace("/index.xml", "")));
                if (!(children != null && children.length > 1)) {
                    sbDeletedFiles.append(FILES_SEPARATOR).append(path.replace("/index.xml", ""));

                }
            }
            formParts.add(new StringPart(DEPLOYER_DELETED_FILES_PARAM, sbDeletedFiles.toString()));
            formParts.add(new StringPart(DEPLOYER_SITE_PARAM, site));

            PostMethod postMethod = new PostMethod(requestUrl.toString());
            postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);

            Part[] parts = new Part[formParts.size()];

            for (int i = 0; i < formParts.size(); i++) parts[i] = formParts.get(i);
            postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
            HttpClient client = new HttpClient();
            int status = client.executeMethod(postMethod);
            postMethod.releaseConnection();
        }
        catch(Exception err) {
            logger.error("error while preview deploying '" + site + ":" + path + "'", err);
        }
    }

    @Override
    public void deleteFiles(String site, List<String> paths) {

    }

    @EventHandler(
            event = EBusConstants.REPOSITORY_CREATE_EVENT,
            ebus = EBusConstants.REPOSITORY_REACTOR,
            type = EventSelectorType.REGEX)
    public void onCreateContent(final Event<RepositoryEventMessage> event) {

        RepositoryEventMessage message = event.getData();
        String site = message.getSite();
        String path = message.getPath();
        deployFile(site, path);
    }

    @EventHandler(
            event = EBusConstants.REPOSITORY_UPDATE_EVENT,
            ebus = EBusConstants.REPOSITORY_REACTOR,
            type = EventSelectorType.REGEX)
    public void onUpdateContent(final Event<RepositoryEventMessage> event) {
        RepositoryEventMessage message = event.getData();
        try {
            String site = message.getSite();
            String path = message.getPath();
            RepositoryEventContext.setCurrent(message.getRepositoryEventContext());
            deployFile(site, path);
        } catch (Exception t) {
            logger.error("Error while deploying preview content for: " + message.getSite() + " - " + message.getPath(), t);
        } finally {
            RepositoryEventContext.setCurrent(null);
        }
    }

    @Override
    public void deployFiles(String site, List<String> paths, List<String> deletedFiles) {

    }

    @EventHandler(
            event = EBusConstants.REPOSITORY_PREVIEW_SYNC_EVENT,
            ebus = EBusConstants.REPOSITORY_REACTOR,
            type = EventSelectorType.REGEX)
    public void syncAllContentToPreview(final Event<RepositoryEventMessage> event) throws ServiceException {

        RepositoryEventMessage message = event.getData();
        String site = message.getSite();
        logger.info("Received preview sync event for site: " + site);
        RepositoryEventContext.setCurrent(message.getRepositoryEventContext());
        String siteRootPath = contentService.expandRelativeSitePath(site, "/");
        if (StringUtils.isNotEmpty(siteRootPath)) {
            try {
                syncFolder(site, siteRootPath);
            } catch (Exception e) {
                logger.error("Site '" + site + "' synchronization failed", e);
                throw new ServiceException("Unable to execute sync for " + site, e);
            }
            logger.info("Synchronization of site '" + site + "' completed successfully");
        } else {
            logger.error("Site '" + site + "' synchronization failed. Repository root path empty");
        }
    }

    protected void syncFolder(String site, String path) {
        RepositoryItem[] children = contentRepository.getContentChildren(path);

        for (RepositoryItem item : children) {
            if (item.isFolder) {
                syncFolder(site, item.path + "/" + item.name);
            } else {
                deployFile(site, contentService.getRelativeSitePath(site, item.path + "/" + item.name));
            }
        }
    }

    @EventHandler(
            event = EBusConstants.REPOSITORY_MOVE_EVENT,
            ebus = EBusConstants.REPOSITORY_REACTOR,
            type = EventSelectorType.REGEX
    )
    public void onMoveContent(final Event<RepositoryEventMessage> event) throws ServiceException {
        RepositoryEventMessage message = event.getData();
        try {
            String site = message.getSite();
            String path = message.getPath();
            String oldPath = message.getOldPath();
            RepositoryEventContext.setCurrent(message.getRepositoryEventContext());
            deleteFile(site, oldPath);
            syncFolder(site, contentService.expandRelativeSitePath(site, path));
        } catch (Exception t) {
            logger.error("Error while deploying moving content from: " + message.getSite() + " - " + message.getOldPath() + " to: " + message.getSite() + " - " + message.getPath(), t);
        } finally {
            RepositoryEventContext.setCurrent(null);
        }
    }

    @EventHandler(
            event = EBusConstants.REPOSITORY_DELETE_EVENT,
            ebus = EBusConstants.REPOSITORY_REACTOR,
            type = EventSelectorType.REGEX
    )
    public void onDeleteContent(final Event<RepositoryEventMessage> event) throws ServiceException {
        RepositoryEventMessage message = event.getData();
        try {
            String site = message.getSite();
            String path = message.getPath();
            RepositoryEventContext.setCurrent(message.getRepositoryEventContext());
            deleteFile(site, path);
        } catch (Exception t) {
            logger.error("Error while deleting content from: " + message.getSite() + " - " + message.getPath(), t);
        } finally {
            RepositoryEventContext.setCurrent(null);
        }
    }

    @EventHandler(
            event = EBusConstants.REPOSITORY_DELETE_SITE_EVENT,
            ebus = EBusConstants.REPOSITORY_REACTOR,
            type = EventSelectorType.REGEX
    )
    public void onDeleteSite(final Event<RepositoryEventMessage> event) throws ServiceException {
        RepositoryEventMessage message = event.getData();
        try {
            String site = message.getSite();
            RepositoryEventContext.setCurrent(message.getRepositoryEventContext());
            deleteSite(site);
        } catch (Exception t) {
            logger.error("Error while deleting site content from preview: " + message.getSite(), t);
        } finally {
            RepositoryEventContext.setCurrent(null);
        }
    }

    protected void deleteSite(String site) {

        DeploymentEndpointConfigTO deploymentEndpointConfigTO = siteService.getPreviewDeploymentEndpoint(site);
        URL requestUrl = null;

        try {
            String url = DEPLOYER_SERVLET_URL;
            List<Part> formParts = new ArrayList<>();
            if (deploymentEndpointConfigTO != null) {
                requestUrl = new URL(deploymentEndpointConfigTO.getServerUrl());
                formParts.add(new StringPart(DEPLOYER_PASSWORD_PARAM, deploymentEndpointConfigTO.getPassword()));
                formParts.add(new StringPart(DEPLOYER_TARGET_PARAM, deploymentEndpointConfigTO.getTarget()));
            } else {
                requestUrl = new URL("http", defaultServer, defaultPort, url);
                formParts.add(new StringPart(DEPLOYER_PASSWORD_PARAM, defaultPassword));
                formParts.add(new StringPart(DEPLOYER_TARGET_PARAM, defaultTarget));
            }

            StringBuilder sbDeletedFiles = new StringBuilder("/");

            formParts.add(new StringPart(DEPLOYER_DELETED_FILES_PARAM, sbDeletedFiles.toString()));
            formParts.add(new StringPart(DEPLOYER_SITE_PARAM, site));

            PostMethod postMethod = new PostMethod(requestUrl.toString());
            postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);

            Part[] parts = new Part[formParts.size()];

            for (int i = 0; i < formParts.size(); i++) parts[i] = formParts.get(i);
            postMethod.setRequestEntity(new MultipartRequestEntity(parts, postMethod.getParams()));
            HttpClient client = new HttpClient();
            int status = client.executeMethod(postMethod);
            postMethod.releaseConnection();
        }
        catch(Exception err) {
            logger.error("error while deleting site from preview: '" + site + "'", err);
        }
    }

    public String getDefaultServer() { return defaultServer; }
    public void setDefaultServer(String defaultServer) { this.defaultServer = defaultServer; }

    public int getDefaultPort() { return defaultPort; }
    public void setDefaultPort(int defaultPort) { this.defaultPort = defaultPort; }

    public String getDefaultPassword() { return defaultPassword; }
    public void setDefaultPassword(String defaultPassword) { this.defaultPassword = defaultPassword; }

    public String getDefaultTarget() { return defaultTarget; }
    public void setDefaultTarget(String defaultTarget) { this.defaultTarget = defaultTarget; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    protected String defaultServer;
    protected int defaultPort;
    protected String defaultPassword;
    protected String defaultTarget;
    protected SiteService siteService;
    protected ContentService contentService;
    protected ContentRepository contentRepository;
}
