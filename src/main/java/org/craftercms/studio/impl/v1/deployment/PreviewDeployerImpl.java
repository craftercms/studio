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

import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
import org.craftercms.studio.api.v1.ebus.EBusConstants;
import org.craftercms.studio.api.v1.ebus.EventListener;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.springframework.http.HttpStatus;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;

public class PreviewDeployerImpl implements PreviewDeployer {

    private final static Logger logger = LoggerFactory.getLogger(PreviewDeployerImpl.class);

    private final static String METHOD_PREVIEW_SYNC_LISTENER = "onPreviewSync";

    protected CloseableHttpClient httpClient;

    public PreviewDeployerImpl() {
        RequestConfig requestConfig = RequestConfig.custom().setExpectContinueEnabled(true).build();
        httpClient = HttpClientBuilder.create()
                        .setConnectionManager(new PoolingHttpClientConnectionManager())
                        .setDefaultRequestConfig(requestConfig)
                        .build();
    }

    public void subscribeToPreviewSyncEvents() {
        try {
            Method subscribeMethod = PreviewDeployerImpl.class.getMethod(METHOD_PREVIEW_SYNC_LISTENER, PreviewEventContext.class);
            this.eventService.subscribe(EBusConstants.EVENT_PREVIEW_SYNC, beanName, subscribeMethod);
        } catch (NoSuchMethodException e) {
            logger.error("Could not subscribe to preview sync events", e);
        }
    }

    @EventListener(EVENT_PREVIEW_SYNC)
    public void onPreviewSync(PreviewEventContext context) {
        String site = context.getSite();
        String requestUrl = getDeployTargetUrl(site);
        HttpPost postRequest = new HttpPost(requestUrl);

        if (context.isWaitTillDeploymentIsDone()) {
            String requestBody = getDeployTargetRequestBody(true);
            HttpEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
            postRequest.setEntity(requestEntity);
        }

        // TODO: DB: add all required params to post method
        try {
            CloseableHttpResponse response = httpClient.execute(postRequest);
            HttpStatus httpStatus = HttpStatus.valueOf(response.getStatusLine().getStatusCode());
            if (!httpStatus.is2xxSuccessful()) {
                logger.error("Preview sync request for site " + site + " returned status " + httpStatus + " (" +
                             httpStatus.getReasonPhrase() + ")");
            }
        } catch (IOException e) {
            logger.error("Error while sending preview sync request for site " + site, e);
        } finally {
            postRequest.releaseConnection();
        }
    }

    private String getDeployTargetUrl(String site) {
        // TODO: DB: implement deployer agent configuration for preview
        // TODO: SJ: Pseudo code: check if site configuration has a Preview Deployer URL, if so, return it, if not
        // TODO: SJ: return default from studioConfiguration.getProperty(PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL);
        String toRet = studioConfiguration.getProperty(PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL).replaceAll(StudioConstants
            .CONFIG_SITENAME_VARIABLE, site);
        return toRet;
    }

    private String getDeployTargetRequestBody(boolean waitTillDone) {
        DeployTargetRequestBody requestBody = new DeployTargetRequestBody();
        requestBody.setWaitTillDone(waitTillDone);

        return requestBody.toJson();
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean createTarget(String site) {
        boolean toReturn = true;
        String requestUrl = getCreateTargetUrl();

        HttpPost postRequest = new HttpPost(requestUrl);
        String requestBody = getCreateTargetRequestBody(site);
        HttpEntity requestEntity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        postRequest.setEntity(requestEntity);

        try {
            CloseableHttpResponse response = httpClient.execute(postRequest);
            if (HttpStatus.valueOf(response.getStatusLine().getStatusCode()) != HttpStatus.CREATED) {
                toReturn = false;
            }
        } catch (IOException e) {
            logger.error("Error while sending preview sync request for site " + site, e);
            toReturn = false;
        } finally {
            postRequest.releaseConnection();
        }
        return toReturn;
    }

    private String getCreateTargetUrl() {
        // TODO: DB: implement deployer agent configuration for preview
        // TODO: SJ: Pseudo code: check if site configuration has a Preview Deployer URL, if so, return it, if not
        // TODO: SJ: return default from studioConfiguration.getProperty(PREVIEW_DEFAULT_CREATE_TARGET_URL);
        String toReturn = studioConfiguration.getProperty(PREVIEW_DEFAULT_CREATE_TARGET_URL);
        return toReturn;
    }

    private String getCreateTargetRequestBody(String site) {
        CreateTargetRequestBody requestBody = new CreateTargetRequestBody();
        requestBody.setEnvironment("preview");
        requestBody.setSiteName(site);
        requestBody.setReplace(Boolean.parseBoolean(studioConfiguration.getProperty(PREVIEW_REPLACE)));
        requestBody.setDisableDeployCron(Boolean.parseBoolean(studioConfiguration.getProperty(PREVIEW_DISABLE_DEPLOY_CRON)));
        requestBody.setTemplateName(studioConfiguration.getProperty(PREVIEW_TEMPLATE_NAME));
        String repoUrl = studioConfiguration.getProperty(PREVIEW_REPO_URL).replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, site);
        Path repoUrlPath = Paths.get(repoUrl);
        repoUrl = repoUrlPath.normalize().toAbsolutePath().toString();
        requestBody.setRepoUrl(repoUrl);
        requestBody.setEngineUrl(studioConfiguration.getProperty(PREVIEW_ENGINE_URL));
        return requestBody.toJson();
    }

    @Override
    public boolean deleteTarget(String site) {
        boolean toReturn = true;
        String requestUrl = getDeleteTargetUrl(site);

        HttpPost postRequest = new HttpPost(requestUrl);

        try {
            CloseableHttpResponse response = httpClient.execute(postRequest);
            if (!HttpStatus.valueOf(response.getStatusLine().getStatusCode()).is2xxSuccessful()) {
                toReturn = false;
            }
        } catch (IOException e) {
            logger.error("Error while sending delete preview target request for site " + site, e);
            toReturn = false;
        } finally {
            postRequest.releaseConnection();
        }
        return toReturn;
    }

    private String getDeleteTargetUrl(String site) {
        // TODO: DB: implement deployer agent configuration for preview
        // TODO: SJ: Pseudo code: check if site configuration has a Preview Deployer URL, if so, return it, if not
        // TODO: SJ: return default from studioConfiguration.getProperty(PREVIEW_DEFAULT_DELETE_TARGET_URL);
        String url = new String(studioConfiguration.getProperty(PREVIEW_DEFAULT_DELETE_TARGET_URL));
        url = url.replaceAll(StudioConstants.CONFIG_SITENAME_VARIABLE, site);
        url = url.replaceAll(StudioConstants.CONFIG_SITEENV_VARIABLE, "preview");
        return url;
    }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    public EventService getEventService() { return eventService; }
    public void setEventService(EventService eventService) { this.eventService = eventService; }

    public String getBeanName() { return beanName; }
    public void setBeanName(String beanName) { this.beanName = beanName; }

    protected StudioConfiguration studioConfiguration;
    protected EventService eventService;
    protected String beanName;

    protected class CreateTargetRequestBody {

        protected String environment;
        protected String siteName;
        protected boolean replace;
        protected boolean disableDeployCron;
        protected String templateName;
        protected String repoUrl;
        protected String engineUrl;

        public String toJson() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("env", this.environment);
            jsonObject.put("site_name", this.siteName);
            jsonObject.put("replace", this.replace);
            jsonObject.put("disable_deploy_cron", this.disableDeployCron);
            jsonObject.put("template_name", this.templateName);
            jsonObject.put("repo_url", this.repoUrl);
            jsonObject.put("engine_url", this.engineUrl);
            return  jsonObject.toString();
        }

        public String getEnvironment() { return environment; }
        public void setEnvironment(String environment) { this.environment = environment; }

        public String getSiteName() { return siteName; }
        public void setSiteName(String siteName) { this.siteName = siteName; }

        public boolean isReplace() { return replace; }
        public void setReplace(boolean replace) { this.replace = replace; }

        public boolean isDisableDeployCron() { return disableDeployCron; }
        public void setDisableDeployCron(boolean disableDeployCron) { this.disableDeployCron = disableDeployCron; }

        public String getTemplateName() { return templateName; }
        public void setTemplateName(String templateName) { this.templateName = templateName; }

        public String getRepoUrl() { return repoUrl; }
        public void setRepoUrl(String repoUrl) { this.repoUrl = repoUrl; }

        public String getEngineUrl() { return engineUrl; }
        public void setEngineUrl(String engineUrl) { this.engineUrl = engineUrl; }
    }

    protected class DeployTargetRequestBody {

        protected boolean waitTillDone;

        public boolean isWaitTillDone() {
            return waitTillDone;
        }

        public void setWaitTillDone(boolean waitTillDone) {
            this.waitTillDone = waitTillDone;
        }

        public String toJson() {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("wait_till_done", this.waitTillDone);
            return  jsonObject.toString();
        }

    }

}
