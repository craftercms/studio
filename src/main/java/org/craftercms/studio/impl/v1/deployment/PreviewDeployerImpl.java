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

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.entity.ContentType;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
import org.craftercms.studio.api.v1.ebus.EBusConstants;
import org.craftercms.studio.api.v1.ebus.EventListener;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.util.StudioConfiguration;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.*;

public class PreviewDeployerImpl implements PreviewDeployer {

    private final static Logger logger = LoggerFactory.getLogger(PreviewDeployerImpl.class);

    private final static String METHOD_PREVIEW_SYNC_LISTENER = "onPreviewSync";

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
        String requestUrl = getDeployerPreviewSyncUrl(site);
        PostMethod postMethod = new PostMethod(requestUrl);
        postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);

        // TODO: DB: add all required params to post method

        HttpClient client = new HttpClient();
        try {
            int status = client.executeMethod(postMethod);
        } catch (IOException e) {
            logger.error("Error while sending preview sync request for site " + site, e);
        } finally {
            postMethod.releaseConnection();
        }
    }

    private String getDeployerPreviewSyncUrl(String site) {
        // TODO: DB: implement deployer agent configuration for preview
        // TODO: SJ: Pseudo code: check if site configuration has a Preview Deployer URL, if so, return it, if not
        // TODO: SJ: return default from studioConfiguration.getProperty(PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL);
        String toRet = studioConfiguration.getProperty(PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL).replaceAll(StudioConstants
            .CONFIG_SITENAME_VARIABLE, site);
        return toRet;
    }

    @Override
    public boolean createTarget(String site) {
        boolean toReturn = true;
        String requestUrl = getDeployerCreatePreviewTargetUrl(site);

        PostMethod postMethod = new PostMethod(requestUrl);
        postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);
        String rqBody = getDeployerCreatePreviewTargetRequestBody(site);
        RequestEntity requestEntity = null;
        try {
            requestEntity = new StringRequestEntity(rqBody, ContentType.APPLICATION_JSON.toString(), StandardCharsets
                .UTF_8.displayName());
        } catch (UnsupportedEncodingException e) {
            logger.info("Unsupported encoding for request body. Using deprecated method instead.");
        }
        if (requestEntity != null) {
            postMethod.setRequestEntity(requestEntity);
        } else {
            postMethod.setRequestBody(rqBody);
        }

        HttpClient client = new HttpClient();
        try {
            int status = client.executeMethod(postMethod);
            if (status != HttpStatus.SC_CREATED) {
                toReturn = false;
            }
        } catch (IOException e) {
            logger.error("Error while sending preview sync request for site " + site, e);
            toReturn = false;
        } finally {
            postMethod.releaseConnection();
        }
        return toReturn;
    }

    private String getDeployerCreatePreviewTargetUrl(String site) {
        // TODO: DB: implement deployer agent configuration for preview
        // TODO: SJ: Pseudo code: check if site configuration has a Preview Deployer URL, if so, return it, if not
        // TODO: SJ: return default from studioConfiguration.getProperty(PREVIEW_DEFAULT_CREATE_TARGET_URL);
        String toReturn = studioConfiguration.getProperty(PREVIEW_DEFAULT_CREATE_TARGET_URL);
        return toReturn;
    }

    private String getDeployerCreatePreviewTargetRequestBody(String site) {
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
        String requestUrl = getDeployerDeletePreviewTargetUrl(site);

        PostMethod postMethod = new PostMethod(requestUrl);
        postMethod.getParams().setBooleanParameter(HttpMethodParams.USE_EXPECT_CONTINUE, true);

        HttpClient client = new HttpClient();
        try {
            int status = client.executeMethod(postMethod);
            if (status != 200) {
                toReturn = false;
            }
        } catch (IOException e) {
            logger.error("Error while sending delete preview target request for site " + site, e);
            toReturn = false;
        } finally {
            postMethod.releaseConnection();
        }
        return toReturn;
    }

    private String getDeployerDeletePreviewTargetUrl(String site) {
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

    class CreateTargetRequestBody {

        protected String environment;
        protected String siteName;
        protected boolean replace;
        protected boolean disableDeployCron;
        protected String templateName;
        protected String repoUrl;
        protected String engineUrl;

        public String toJson() {
            StringBuilder sb = new StringBuilder();
            sb.append("{ ");
            sb.append("\"env\":\"").append(this.environment).append("\", ");
            sb.append("\"site_name\":\"").append(this.siteName).append("\", ");
            sb.append("\"replace\":").append(this.replace).append(", ");
            sb.append("\"disable_deploy_cron\":").append(this.disableDeployCron).append(", ");
            sb.append("\"template_name\":\"").append(this.templateName).append("\", ");
            sb.append("\"repo_url\":\"").append(this.repoUrl).append("\", ");
            sb.append("\"engine_url\":\"").append(this.engineUrl).append("\"");
            sb.append(" }");
            return sb.toString();
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
}
