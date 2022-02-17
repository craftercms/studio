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
import org.apache.commons.configuration2.interpol.ConfigurationInterpolator;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.collections.MapUtils;
import org.craftercms.commons.rest.RestTemplate;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONFIG_SITENAME_VARIABLE;

/**
 * Base abstract class for {@link Deployer}s. Provides the commons methods that call the Deployer APIs.
 *
 * @author avasquez
 */
public abstract class AbstractDeployer implements Deployer {

    private final static Logger logger = LoggerFactory.getLogger(AbstractDeployer.class);

    protected RestTemplate restTemplate;
    protected StudioConfiguration studioConfiguration;

    public AbstractDeployer() {
        restTemplate = new RestTemplate();
        restTemplate.setErrorResponseType(Map.class);
        restTemplate.afterPropertiesSet();
    }

    @Required
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    protected void doCreateTarget(String site, String environment, String template,
                                  boolean replace, boolean disableDeployCron, String localRepoPath,
                                  String repoUrl, HierarchicalConfiguration<ImmutableNode> additionalParams)
            throws IllegalStateException, RestClientException {
        String requestUrl = getCreateTargetUrl();
        Map<String, Object> requestBody = getCreateTargetRequestBody(site, environment, template,
                                                                     replace, disableDeployCron, localRepoPath,
                                                                     repoUrl, additionalParams);
        try {
            RequestEntity<Map<String, Object>> requestEntity = RequestEntity.post(new URI(requestUrl))
                                                                            .contentType(MediaType.APPLICATION_JSON)
                                                                            .body(requestBody);

            logger.debug("Calling create target API: {0}", requestEntity);

            restTemplate.exchange(requestEntity, Map.class);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid format of create target URL: " + requestUrl, e);
        }
    }

    protected void doDeleteTarget(String site, String environment) {
        String requestUrl = getDeleteTargetUrl(site, environment);

        try {
            RequestEntity<Void> requestEntity = RequestEntity.post(new URI(requestUrl))
                                                             .contentType(MediaType.APPLICATION_JSON)
                                                             .build();

            logger.debug("Calling delete target API: {0}", requestEntity);

            restTemplate.exchange(requestEntity, Map.class);
        } catch (URISyntaxException e) {
            throw new IllegalStateException("Invalid format of create target URL: " + requestUrl, e);
        }
    }

    protected Map<String, Object> getCreateTargetRequestBody(String site, String environment,
                                                             String template, boolean replace, boolean disableDeployCron,
                                                             String localRepoPath, String repoUrl,
                                                             HierarchicalConfiguration<ImmutableNode> additionalParams) {

        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("env", environment);
        requestBody.put("site_name", site);
        requestBody.put("template_name", template);
        requestBody.put("replace", replace);
        requestBody.put("disable_deploy_cron", disableDeployCron);
        
        if (StringUtils.isNotEmpty(localRepoPath)) {
            requestBody.put("local_repo_path", localRepoPath);
        }
        if (StringUtils.isNotEmpty(repoUrl)) {
            requestBody.put("repo_url", repoUrl);
        }
        if (additionalParams != null) {
            addAdditionalParams(requestBody, additionalParams);
        }

        return requestBody;
    }

    protected void addAdditionalParams(Map<String, Object> params,
                                       HierarchicalConfiguration<ImmutableNode> additionalParams) {
        addChildParams(params, additionalParams.getNodeModel().getNodeHandler().getRootNode(),
                       additionalParams.getInterpolator());
    }

    protected void addChildParams(Map<String, Object> childParams, ImmutableNode parentNode,
                                  ConfigurationInterpolator interpolator) {
        for (ImmutableNode childParamNode : parentNode.getChildren()) {
            if (childParamNode.getChildren().isEmpty()) {
                Object value = interpolator.interpolate(childParamNode.getValue());
                MapUtils.add(childParams, childParamNode.getNodeName(), value);
            } else {
                Map<String, Object> params = new LinkedHashMap<>();
                addChildParams(params, childParamNode, interpolator);

                MapUtils.add(childParams, childParamNode.getNodeName(), params);
            }
        }
    }

    protected String getRepoUrl(String configKey, String site) {
        String repoUrl = studioConfiguration.getProperty(configKey);
        if (StringUtils.isNotEmpty(repoUrl)) {
            repoUrl = repoUrl.replaceAll(CONFIG_SITENAME_VARIABLE, site);

            try {
                return new URI(repoUrl).normalize().toString();
            } catch (URISyntaxException e) {
                throw new IllegalStateException(
                        "Invalid format of URL for config key '" + configKey + "': " + repoUrl, e);
            }
        } else {
            return "";
        }
    }

    protected abstract String getCreateTargetUrl();
    protected abstract String getDeleteTargetUrl(String site, String environment);

}
