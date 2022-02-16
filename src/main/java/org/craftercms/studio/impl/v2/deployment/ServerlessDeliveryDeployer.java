/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.deployment;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.springframework.web.client.RestClientException;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONFIG_SITEENV_VARIABLE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONFIG_SITENAME_VARIABLE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_DEPLOYER_TARGET_CREATE_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_DEPLOYER_TARGET_DELETE_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_DEPLOYER_TARGET_LOCAL_REPO_PATH;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_DEPLOYER_TARGET_REMOTE_REPO_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_DEPLOYER_TARGET_REPLACE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_DEPLOYER_TARGET_TEMPLATE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_DEPLOYER_TARGET_TEMPLATE_PARAMS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.SERVERLESS_DELIVERY_ENABLED;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.deployment.Deployer} that interacts with the Serverless
 * Delivery {@link org.craftercms.studio.api.v2.deployment.Deployer}, only if serverless delivery is enabled.
 *
 * @author avasquez
 */
public class ServerlessDeliveryDeployer extends AbstractDeployer {

    private final static String ENV_SERVERLESS_DELIVERY = "serverless-delivery";

    @Override
    public void createTargets(String site) throws RestClientException {
        if (isServerlessDeliveryEnabled()) {
            String localRepoPath = getRepoUrl(SERVERLESS_DELIVERY_DEPLOYER_TARGET_LOCAL_REPO_PATH, site);
            String repoUrl = getRepoUrl(SERVERLESS_DELIVERY_DEPLOYER_TARGET_REMOTE_REPO_URL, site);
            String template = studioConfiguration.getProperty(SERVERLESS_DELIVERY_DEPLOYER_TARGET_TEMPLATE);
            boolean replace = studioConfiguration.getProperty(SERVERLESS_DELIVERY_DEPLOYER_TARGET_REPLACE,
                                                              Boolean.class, false);
            HierarchicalConfiguration<ImmutableNode> templateParams =
                    studioConfiguration.getSubConfig(SERVERLESS_DELIVERY_DEPLOYER_TARGET_TEMPLATE_PARAMS);

            doCreateTarget(site, ENV_SERVERLESS_DELIVERY, template, replace, false, localRepoPath,
                           repoUrl, templateParams);
        }
    }

    @Override
    public void deleteTargets(String site) throws RestClientException {
        if (isServerlessDeliveryEnabled()) {
            doDeleteTarget(site, ENV_SERVERLESS_DELIVERY);
        }
    }

    @Override
    protected String getCreateTargetUrl() {
        return studioConfiguration.getProperty(SERVERLESS_DELIVERY_DEPLOYER_TARGET_CREATE_URL);
    }

    @Override
    protected String getDeleteTargetUrl(String site, String environment) {
        return studioConfiguration.getProperty(SERVERLESS_DELIVERY_DEPLOYER_TARGET_DELETE_URL)
                                  .replaceAll(CONFIG_SITENAME_VARIABLE, site)
                                  .replaceAll(CONFIG_SITEENV_VARIABLE, environment);
    }

    protected boolean isServerlessDeliveryEnabled() {
        return studioConfiguration.getProperty(SERVERLESS_DELIVERY_ENABLED, Boolean.class, false);
    }

}
