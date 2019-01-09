/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.io.FileUtils;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class EnvironmentStoreDeployer implements Deployer{

    private final static Logger logger = LoggerFactory.getLogger(EnvironmentStoreDeployer.class);

    @Override
    public void deployFile(String site, String path) {
        InputStream content = null;
        try {
            content = contentService.getContent(site, path);
            writeFile(site, path, environment, content);
        } catch (ContentNotFoundException e) {
            logger.error("Deployment to environment store failed [{0}]. Content not found for [{1}:{2}]", e, environment, site, path);
        }
    }
/*
    @Override
    public void deployFile(String site, String path, String environment) {

    }
*/
    private void writeFile(String site, String path, String environment, InputStream content) {

        try {
            if (content == null || content.available() < 0) {
                logger.warn("Not able to write empty content to environment store for site: {0}, path: {1}, environment: {2}", site, path, environment);
                return;
            }
            File file = new File(getDestinationPath(site, path, environment));
            logger.debug("Writing file: " + file.getAbsolutePath());
            FileUtils.copyInputStreamToFile(content, file);
        } catch (IOException e) {
            logger.error("Error while saving content to environment store [site: {0}] [path: {1}] [envirnonment: {2}]", e, site, path, environment);
        }
    }

    private String getDestinationPath(String site, String path, String environment) {
        return String.format("%s/%s/%s/%s", environmentsStoreRootPath, site, environment, path);
    }

    @Override
    public void deployFiles(String site, List<String> paths) {

    }
/*
    @Override
    public void deployFiles(String site, List<String> paths, String environment) {

    }
*/
    @Override
    public void deleteFile(String site, String path) {
        File file = new File(getDestinationPath(site, path, environment));
        file.delete();
    }
/*
    @Override
    public void deleteFile(String site, String path, String environment) {

    }
*/
    @Override
    public void deleteFiles(String site, List<String> paths) {

    }
/*
    @Override
    public void deleteFiles(String site, List<String> paths, String environment) {

    }
*/

    @Override
    public void deployFiles(String site, List<String> paths, List<String> deletedFiles) {

    }

    public String getEnvironmentsStoreRootPath() { return environmentsStoreRootPath; }
    public void setEnvironmentsStoreRootPath(String environmentsStoreRootPath) { this.environmentsStoreRootPath = environmentsStoreRootPath; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public String getEnvironment() { return environment; }
    public void setEnvironment(String environment) { this.environment = environment; }

    protected String environmentsStoreRootPath;
    protected ContentService contentService;
    protected String environment;
}
