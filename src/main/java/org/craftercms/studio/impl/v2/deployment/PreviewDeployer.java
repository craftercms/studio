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

import org.craftercms.commons.rest.RestServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.event.content.ContentEvent;
import org.craftercms.studio.api.v2.event.repository.RepositoryEvent;
import org.craftercms.studio.api.v2.event.site.SiteEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.MediaType;
import org.springframework.http.RequestEntity;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONFIG_SITEENV_VARIABLE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONFIG_SITENAME_VARIABLE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHORING_DISABLE_DEPLOY_CRON;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHORING_REPLACE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.AUTHORING_TEMPLATE_NAME;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PREVIEW_DEFAULT_CREATE_TARGET_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PREVIEW_DEFAULT_DELETE_TARGET_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PREVIEW_DISABLE_DEPLOY_CRON;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PREVIEW_REPLACE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PREVIEW_REPO_URL;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.PREVIEW_TEMPLATE_NAME;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.deployment.Deployer} that interacts with Authoring's
 * Preview Deployer. This {@code Deployer}:
 *
 * <ul>
 *     <li>Creates both and authoring and preview target on create</li>
 *     <li>Deletes the authoring and preview target on delete</li>
 *     <li>Issues deployments to the authoring and preview target on a preview sync event</li>
 * </ul>
 *
 * @author avasquez
 */
public class PreviewDeployer extends AbstractDeployer {

    private final static Logger logger = LoggerFactory.getLogger(PreviewDeployer.class);

    private final static String ENV_PREVIEW = "preview";
    private final static String ENV_AUTHORING = "authoring";


    @EventListener
    public void onSiteCreateComplete(SiteEvent event) {
        doPreviewSync(event.getSiteId(), true);
    }

    @EventListener
    public void onRepositorySyncComplete(RepositoryEvent event) {
        doPreviewSync(event.getSiteId(), false);
    }

    @EventListener
    public void onContentChange(ContentEvent event) {
        doPreviewSync(event.getSiteId(), event.isWaitForCompletion());
    }

    protected void doPreviewSync(String siteId, boolean waitTillDone) {
        doDeployment(siteId, ENV_AUTHORING, false);
        doDeployment(siteId, ENV_PREVIEW, waitTillDone);
    }

    @Override
    public void createTargets(String site) throws RestClientException {
        doCreateAuthTarget(site);
        doCreatePreviewTarget(site);
    }

    @Override
    public void deleteTargets(String site) throws RestClientException {
        doDeleteTarget(site, ENV_AUTHORING);
        doDeleteTarget(site, ENV_PREVIEW);
    }

    protected void doDeployment(String site, String environment, boolean waitTillDone) {
        String requestUrl = getDeployTargetUrl(site, environment);
        Map<String, Object> requestBody = Collections.singletonMap("wait_till_done", waitTillDone);

        try {
            RequestEntity<Map<String, Object>> requestEntity = RequestEntity.post(new URI(requestUrl))
                                                                            .contentType(MediaType.APPLICATION_JSON)
                                                                            .body(requestBody);

            logger.debug("Calling deployment API: {0}", requestEntity);

            restTemplate.exchange(requestEntity, Map.class);
        } catch (RestServiceException e) {
            logger.error("Preview sync request for site " + site + " returned error response: " + e);
        } catch (Exception e) {
            logger.error("Error while sending preview sync request for site " + site, e);
        }
    }

    protected void doCreateAuthTarget(String site) throws IllegalStateException, RestClientException {
        String repoUrl = getRepoUrl(PREVIEW_REPO_URL, site);
        String template = studioConfiguration.getProperty(AUTHORING_TEMPLATE_NAME);
        boolean replace = studioConfiguration.getProperty(AUTHORING_REPLACE, Boolean.class, false);
        boolean disableCron = studioConfiguration.getProperty(AUTHORING_DISABLE_DEPLOY_CRON, Boolean.class, false);

        doCreateTarget(site, ENV_AUTHORING, template, replace, disableCron,
                       null, repoUrl, null);
    }

    protected void doCreatePreviewTarget(String site) throws IllegalStateException,
                                                                                     RestClientException  {
        String repoUrl = getRepoUrl(PREVIEW_REPO_URL, site);
        String template = studioConfiguration.getProperty(PREVIEW_TEMPLATE_NAME);
        boolean replace = studioConfiguration.getProperty(PREVIEW_REPLACE, Boolean.class, false);
        boolean disableCron = studioConfiguration.getProperty(PREVIEW_DISABLE_DEPLOY_CRON, Boolean.class, false);

        doCreateTarget(site, ENV_PREVIEW, template, replace, disableCron, null, repoUrl, null);
    }

    protected String getDeployTargetUrl(String site, String environment) {
        // TODO: DB: implement deployer agent configuration for preview
        // TODO: SJ: Pseudo code: check if site configuration has a Preview Deployer URL, if so, return it, if not
        // TODO: SJ: return default from studioConfiguration.getProperty(PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL);
        return studioConfiguration.getProperty(PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL)
                                  .replaceAll(CONFIG_SITENAME_VARIABLE, site)
                                  .replaceAll(CONFIG_SITEENV_VARIABLE, environment);
    }

    @Override
    protected String getCreateTargetUrl() {
        // TODO: DB: implement deployer agent configuration for preview
        // TODO: SJ: Pseudo code: check if site configuration has a Preview Deployer URL, if so, return it, if not
        // TODO: SJ: return default from studioConfiguration.getProperty(PREVIEW_DEFAULT_CREATE_TARGET_URL);
        return studioConfiguration.getProperty(PREVIEW_DEFAULT_CREATE_TARGET_URL);
    }

    @Override
    protected String getDeleteTargetUrl(String site, String environment) {
        // TODO: DB: implement deployer agent configuration for preview
        // TODO: SJ: Pseudo code: check if site configuration has a Preview Deployer URL, if so, return it, if not
        // TODO: SJ: return default from studioConfiguration.getProperty(PREVIEW_DEFAULT_DELETE_TARGET_URL);
        return studioConfiguration.getProperty(PREVIEW_DEFAULT_DELETE_TARGET_URL)
                                  .replaceAll(CONFIG_SITENAME_VARIABLE, site)
                                  .replaceAll(CONFIG_SITEENV_VARIABLE, environment);
    }

}
