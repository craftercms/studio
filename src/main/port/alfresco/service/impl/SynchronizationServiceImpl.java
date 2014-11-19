/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
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
package org.craftercms.cstudio.alfresco.service.impl;

import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.collections.CollectionUtils;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.preview.DeployedPreviewFile;
import org.craftercms.cstudio.alfresco.preview.PreviewDeployer;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.CStudioNodeService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.SiteService;
import org.craftercms.cstudio.alfresco.service.api.SynchronizationService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.util.PreviewDeployUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Sumer Jabri
 * @author Russ Danner
 * @author Alfonso Vásquez
 */
public class SynchronizationServiceImpl extends AbstractRegistrableService implements SynchronizationService {

    protected PreviewDeployer deployer;

    public void setDeployer(PreviewDeployer deployer) {
        this.deployer = deployer;
    }

    @Override
    public void register() {
        getServicesManager().registerService(SynchronizationService.class, this);
    }

    @Override
    public void synchronize(String path) throws ServiceException {
        try {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef nodeRef = persistenceManagerService.getNodeRef(path);
            DeployedPreviewFile deployedFile = deployer.getFile(path);
            if (nodeRef != null) {
                synchronize(path, getService(PersistenceManagerService.class).getFileInfo(nodeRef), deployedFile);
            } else {
                synchronize(path, null, deployedFile);
            }
        } catch (Exception e) {
            throw new ServiceException("Unable to execute sync for " + path, e);
        }
    }
    
    protected void synchronize(String path, FileInfo dmFileInfo, DeployedPreviewFile deployedFile) throws IOException {
        boolean nodeExists = dmFileInfo != null;
        boolean fileExists = deployedFile != null;

        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        SiteService siteService = getServicesManager().getService(SiteService.class);
        if (nodeExists && !fileExists) {
            if (!dmFileInfo.isFolder()) {
                DmPathTO dmPathTO = new DmPathTO(path);
                String site = dmPathTO.getSiteName();
                DeploymentEndpointConfigTO deploymentConfigTO = siteService.getPreviewDeploymentEndpoint(site);
                PreviewDeployUtils.deployFile(site, path, dmFileInfo, persistenceManagerService, deployer, deploymentConfigTO);
            } else {
                deployDir(path, dmFileInfo);
            }
        } else if (nodeExists && fileExists) {
            if (!dmFileInfo.isFolder() && deployedFile.isFile()) {
                if (PreviewDeployUtils.isUpdated(path, dmFileInfo, deployedFile)) {
                    DmPathTO dmPathTO = new DmPathTO(path);
                    String site = dmPathTO.getSiteName();
                    DeploymentEndpointConfigTO deploymentConfigTO = siteService.getPreviewDeploymentEndpoint(site);
                    PreviewDeployUtils.deployFile(site, path, dmFileInfo, persistenceManagerService, deployer, deploymentConfigTO);
                }
            } else if (dmFileInfo.isFolder() && deployedFile.isDirectory()) {
                synchronizeDir(path, dmFileInfo);
            } else {
                // This could mean that someone deleted a dir and created a file with the same name, or vice versa.
                DmPathTO dmPathTO = new DmPathTO(path);
                String site = dmPathTO.getSiteName();
                DeploymentEndpointConfigTO deploymentConfigTO = siteService.getPreviewDeploymentEndpoint(site);
                PreviewDeployUtils.deleteFileOrDir(site, path, deployer, deploymentConfigTO);
                if (!dmFileInfo.isFolder()) {
                    PreviewDeployUtils.deployFile(site, path, dmFileInfo, persistenceManagerService, deployer, deploymentConfigTO);
                } else {
                    deployDir(path, dmFileInfo);
                }
            }
        } else if (!nodeExists && fileExists) {
            DmPathTO dmPathTO = new DmPathTO(path);
            String site = dmPathTO.getSiteName();
            DeploymentEndpointConfigTO deploymentConfigTO = siteService.getPreviewDeploymentEndpoint(site);
            PreviewDeployUtils.deleteFileOrDir(site, path, deployer, deploymentConfigTO);
        }
    }

    protected void deployDir(String path, FileInfo dmFileInfo) throws IOException {
    	PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        SiteService siteService = getServicesManager().getService(SiteService.class);
        List<FileInfo> childrenFileInfo = persistenceManagerService.list(dmFileInfo.getNodeRef());
        if (CollectionUtils.isNotEmpty(childrenFileInfo)) {
            for (FileInfo childFileInfo : childrenFileInfo) {
                String childPath = path + "/" + childFileInfo.getName();
                if (!childFileInfo.isFolder()) {
                    DmPathTO dmPathTO = new DmPathTO(path);
                    String site = dmPathTO.getSiteName();
                    DeploymentEndpointConfigTO deploymentConfigTO = siteService.getPreviewDeploymentEndpoint(site);
                    PreviewDeployUtils.deployFile(site, childPath, childFileInfo, persistenceManagerService, deployer, deploymentConfigTO);
                } else {
                    deployDir(childPath, childFileInfo);
                }
            }
        }
    }

    protected void synchronizeDir(String path, FileInfo dmFileInfo) throws IOException {
        List<FileInfo> childrenFileInfo = getService(PersistenceManagerService.class).list(dmFileInfo.getNodeRef());
        List<DeployedPreviewFile> deployedChildren = new LinkedList<DeployedPreviewFile>(deployer.getChildren(path));
        if (CollectionUtils.isNotEmpty(childrenFileInfo)) {
            for (FileInfo childFileInfo : childrenFileInfo) {
                boolean deployedChildFound = false;

                for (Iterator<DeployedPreviewFile> i = deployedChildren.iterator(); i.hasNext() && !deployedChildFound;) {
                    DeployedPreviewFile deployedChild = i.next();
                    if (deployedChild.getName().equals(childFileInfo.getName())) {
                        synchronize(path + "/" + deployedChild.getName(), childFileInfo, deployedChild);

                        deployedChildFound = true;
                        i.remove();
                    }
                }

                if (!deployedChildFound) {
                    synchronize(path + "/" + childFileInfo.getName(), childFileInfo, null);
                }
            }
        }

        if (CollectionUtils.isNotEmpty(deployedChildren)) {
            for (DeployedPreviewFile deployedChild : deployedChildren) {
                synchronize(path + "/" + deployedChild.getName(), null, deployedChild);
            }
        }
    }

}
