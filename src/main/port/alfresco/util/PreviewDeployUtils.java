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
package org.craftercms.cstudio.alfresco.util;

import org.alfresco.service.cmr.model.FileInfo;
import org.craftercms.cstudio.alfresco.deployment.DeploymentEndpointConfigTO;
import org.craftercms.cstudio.alfresco.preview.DeployedPreviewFile;
import org.craftercms.cstudio.alfresco.preview.PreviewDeployer;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * @author Alfonso Vásquez
 */
public class PreviewDeployUtils {

    public static final String MOD_DATE_META_PROPERTY = "dm.modDate";

    private static final Logger logger = LoggerFactory.getLogger(PreviewDeployUtils.class);

    public static void createDir(String path, PreviewDeployer deployer)
            throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating deployment dir at " + path);
        }

        deployer.createDirectory(path);
    }

    public static void deployFile(String site, String path, FileInfo dmFileInfo, PersistenceManagerService persistenceManagerService,
                                  PreviewDeployer deployer, DeploymentEndpointConfigTO deploymentEndpointConfigTO) throws IOException {
        Properties metaData = new Properties();
        metaData.setProperty(MOD_DATE_META_PROPERTY, Long.toString(dmFileInfo.getModifiedDate().getTime()));

        if (logger.isDebugEnabled()) {
            logger.debug("Deploying file to " + path + " (deployment metadata " + metaData + ")");
        }

        InputStream content = persistenceManagerService.getReader(dmFileInfo.getNodeRef()).getContentInputStream();
        try {
            deployer.deploy(site, path, content, metaData, deploymentEndpointConfigTO);
        } finally {
            try {
                content.close();
            } catch (IOException e) {
            }
        }
    }

    public static void deleteFileOrDir(String site, String path, PreviewDeployer deployer, DeploymentEndpointConfigTO deploymentEndpointConfigTO) throws IOException {
        if (logger.isDebugEnabled()) {
            logger.debug("Deleting deployed file or dir at " + path);
        }
        try {
            deployer.delete(site, path, deploymentEndpointConfigTO);
        } catch (FileNotFoundException fnfe) {
            if (logger.isDebugEnabled()) {
                logger.debug("File/Folder not found " + path);
            }
        } catch (Exception e) {
            logger.error("Error while deleting path " + path, e);
        }
    }

    public static boolean isUpdated(String path, FileInfo dmFileInfo, DeployedPreviewFile deployedFile) throws IOException {
        Properties metaData = deployedFile.getMetaData();
        if (metaData != null) {
            long dmModDate = dmFileInfo.getModifiedDate().getTime();
            long deployedFileModDate = Long.parseLong(metaData.getProperty(MOD_DATE_META_PROPERTY));
            boolean updated = dmModDate != deployedFileModDate;

            if (updated && logger.isDebugEnabled()) {
                logger.debug("DM file " + path + " updated (AVM mod date = '" + dmFileInfo.getModifiedDate() + "' vs deployed " +
                        "file mod date = '" + new Date(deployedFileModDate) + "')");
            }

            return updated;
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("No deployment metadata found for AVM file " + path + ". Re-deployment required");
            }

            return true;
        }
    }

}
