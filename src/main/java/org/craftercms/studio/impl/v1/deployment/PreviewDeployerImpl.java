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
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.http.entity.ContentType;
import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.util.StudioConfiguration;

import static org.craftercms.studio.api.v1.util.StudioConfiguration.PREVIEW_DEFAULT_CREATE_TARGET_URL;
import static org.craftercms.studio.api.v1.util.StudioConfiguration.PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PreviewDeployerImpl implements PreviewDeployer {

    private final static Logger logger = LoggerFactory.getLogger(PreviewDeployerImpl.class);

    public void onEvent(String site) {
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
        String toRet = studioConfiguration.getProperty(PREVIEW_DEFAULT_PREVIEW_DEPLOYER_URL) + "/preview_" + site;
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
            requestEntity = new StringRequestEntity(rqBody, ContentType.APPLICATION_JSON.toString(), StandardCharsets.UTF_8.displayName());
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
            if (status != 200) {
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
        toReturn = toReturn.replace("{siteId}", site);
        return toReturn;
    }

    private String getDeployerCreatePreviewTargetRequestBody(String site) {
        String toRet = "{\n" +
                "  \"target_id\": \"preview_SITENAME\",\n" +
                "  \"replace\": true,\n" +
                "  \"template_name\" : \"preview\",\n" +
                "  \"site_name\" : \"SITENAME\",\n" +
                "  \"remote_repo_url\" : \"REPO_URL\",\n" +
                "  \"remote_repo_branch\" : \"master\"\n" +
                "}";
        toRet = toRet.replaceAll("SITENAME", site);
        Path repoPath = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH), studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site, studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH));
        toRet = toRet.replace("REPO_URL",repoPath.toAbsolutePath().toString());
        return toRet;
    }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }

    protected StudioConfiguration studioConfiguration;
}
