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

package org.craftercms.studio.impl.v1.deployment;

import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.DeploymentEndpointConfigTO;

public class DeployerFactory {

    public PreviewDeployer createPreviewDeployer() {
        PreviewDeployer previewDeployer = new PreviewDeployer();
        previewDeployer.setContentService(contentService);
        previewDeployer.setContentRepository(contentRepository);
        previewDeployer.setSiteService(siteService);
        previewDeployer.setDefaultServer(defaultServer);
        previewDeployer.setDefaultPort(defaultPort);
        previewDeployer.setDefaultTarget(defaultTarget);
        previewDeployer.setDefaultPassword(defaultPassword);
        return previewDeployer;
    }

    public Deployer createEnvironmentStoreDeployer(String environment) {
        EnvironmentStoreDeployer deployer = new EnvironmentStoreDeployer();
        deployer.setContentService(contentService);
        deployer.setEnvironmentsStoreRootPath(environmentsStoreRootPath);
        deployer.setEnvironment(environment);
        return deployer;
    }

    public Deployer createEnvironmentStoreGitDeployer(String environment) {
        EnvironmentStoreGitDeployer deployer = new EnvironmentStoreGitDeployer();
        deployer.setContentService(contentService);
        deployer.setEnvironmentsStoreRootPath(environmentsStoreRootPath);
        deployer.setEnvironment(environment);
        return deployer;
    }

    public Deployer createSyncTargetDeployer(String environment, DeploymentEndpointConfigTO endpointConfigTO) {
        SyncTargetDeployer deployer = new SyncTargetDeployer();
        deployer.setEnvironmentsStoreRootPath(environmentsStoreRootPath);
        deployer.setEnvironment(environment);
        deployer.setEndpointConfig(endpointConfigTO);
        return deployer;
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

    public String getEnvironmentsStoreRootPath() { return environmentsStoreRootPath; }
    public void setEnvironmentsStoreRootPath(String environmentsStoreRootPath) { this.environmentsStoreRootPath = environmentsStoreRootPath; }

    public ContentRepository getContentRepository() { return contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this.contentRepository = contentRepository; }

    protected String environmentsStoreRootPath;
    protected String defaultServer;
    protected int defaultPort;
    protected String defaultPassword;
    protected String defaultTarget;
    protected SiteService siteService;
    protected ContentService contentService;
    protected ContentRepository contentRepository;
}
