/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
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

import jakarta.validation.Valid;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.deployment.DeploymentException;
import org.craftercms.studio.api.v1.service.deployment.DeploymentService;
import org.craftercms.studio.api.v1.service.deployment.DmPublishService;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static java.lang.String.format;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

public class DmPublishServiceImpl implements DmPublishService {

    private static final Logger logger = LoggerFactory.getLogger(DmPublishServiceImpl.class);

    protected DeploymentService deploymentService;
    protected SecurityService securityService;
    protected DependencyService dependencyService;
    protected ItemServiceInternal itemServiceInternal;

    @Override
    @Valid
    public void bulkGoLive(@ValidateStringParam String site,
                           @ValidateStringParam String environment,
                           @ValidateSecurePathParam String path,
                           String comment) throws ServiceLayerException {
        logger.info("Start Bulk Publish in site '{}' path '{}' to target '{}'", site, path, environment);

        String queryPath = path;
        if (queryPath.startsWith(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
            queryPath = queryPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
        }

        logger.debug("Get a change-set from site '{}' root path '{}'", site, queryPath);
        List<String> childrenPaths = itemServiceInternal.getChangeSetForSubtree(site, queryPath);

        logger.debug("Collected '{}' items from site '{}' root path '{}'", childrenPaths.size(), site, queryPath);
        Set<String> processedPaths = new HashSet<>();
        ZonedDateTime launchDate = DateUtils.getCurrentTime();
        for (String childPath : childrenPaths) {
            String childHash = DigestUtils.md2Hex(childPath);
            logger.debug("Process bulk publish dependencies in site '{}' path '{}'", site, childPath);
            if (processedPaths.add(childHash)) {
                List<String> pathsToPublish = new ArrayList<>();
                pathsToPublish.add(childPath);
                for (String publishDependency : dependencyService.getPublishingDependencies(site, childPath)) {
                    String hash = DigestUtils.md2Hex(publishDependency);
                    if (processedPaths.add(hash)) {
                        pathsToPublish.add(publishDependency);
                    }
                }
                String aprover = securityService.getCurrentUser();
                if (StringUtils.isEmpty(comment)) {
                    comment = format("Bulk Publish invoked by '%s'", aprover);
                }
                logger.info("Publish a package of '{}' items in site '{}' path '{}' to target '{}'",
                        pathsToPublish.size(), site, childPath, environment);
                try {
                    deploymentService.deploy(site, environment, pathsToPublish, launchDate, aprover, comment, true);
                } catch (DeploymentException | UserNotFoundException e) {
                    logger.error("Failed to bulk publish site '{}' path '{}'", site, childPath, e);
                } finally {
                    logger.debug("Finished bulk publish processing of package in site '{}' path '{}'",
                            site, childPath);
                }
            }
        }
        logger.info("Finished Bulk Publish site '{}' path '{}' to target '{}'", site, path, environment);
    }

    public void setDeploymentService(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}
