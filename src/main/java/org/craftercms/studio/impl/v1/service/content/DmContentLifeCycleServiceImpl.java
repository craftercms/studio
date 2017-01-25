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
package org.craftercms.studio.impl.v1.service.content;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.script.ScriptExecutor;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmContentLifeCycleService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.impl.v1.util.spring.context.ApplicationContextProvider;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class DmContentLifeCycleServiceImpl extends AbstractRegistrableService implements DmContentLifeCycleService {

    private static final Logger logger = LoggerFactory.getLogger(DmContentLifeCycleServiceImpl.class);

    /**
     * metadata extraction script location
     */
    protected String scriptLocation;
    public String getScriptLocation() {
        return scriptLocation;
    }
    public void setScriptLocation(String scriptLocation) {
        this.scriptLocation = scriptLocation;
    }

    /**
     * mapping of beans and services to map in to the scripting environment during metadata extraction
     */
    protected Map<String, Object> _scriptObjects;
    public Map<String, Object> getScriptObjects() {
        return _scriptObjects;
    }
    public void setScriptObjects(Map<String, Object> scriptObjects) {
        this._scriptObjects = scriptObjects;
    }

    @Override
    public void register() {
        getServicesManager().registerService(DmContentLifeCycleService.class, this);
    }

    @Override
    public void process(String site, String user, String path, String contentType, ContentLifeCycleOperation operation, Map<String, String> params) {
        if (operation == null) {
            logger.warn("No lifecycle operation provided for " + site + ":" + path);
            return;
        }
        if (StringUtils.isEmpty(contentType)) {
            logger.warn("Skipping content lifecycle script execution. no content type provided for " + site + ":" + path);
            return;
        }
        // find the script ref based on content type
        String scriptPath = getScriptPath(site, contentType);
        if (!contentService.contentExists(scriptPath)) {
            // it's ok not to have a script
            logger.debug("No script found at " + scriptPath + ", contentType: " + contentType);

            return;
        }
        String script = contentService.getContentAsString(scriptPath);

        Map<String, Object> model = buildModel(site, user, path, contentType, operation.toString(), params);
        try {
            scriptExecutor.executeScriptString(script, model);
        } catch (Exception e) {
            logger.error("Error while executing content lifecycle script for " + site + ":" + path, e);
        }
    }

    /**
     * get the content metadata extraction script
     *
     * @param contentType
     * @return path of the script
     */
    protected String getScriptPath(String site, String contentType) {
        String location = scriptLocation.replaceAll(CStudioConstants.PATTERN_SITE, site)
                .replaceAll(CStudioConstants.PATTERN_CONTENT_TYPE, contentType);
        return location;
    }

    /**
     * build script model
     *
     * @param site
     * @param user
     * @param path
     * @param contentType
     * @param operation
     * @param params
     * @return
     */
    protected Map<String, Object> buildModel(String site, String user, String path, String contentType, String operation, Map<String, String> params) {
        Map<String, Object> model = new HashMap<String,Object>();
        for (String scriptObjectName : _scriptObjects.keySet()) {
            model.put(scriptObjectName, _scriptObjects.get(scriptObjectName));
        }
        model.put(DmConstants.KEY_SITE, site);
        model.put(DmConstants.KEY_PATH, path);
        model.put(DmConstants.KEY_FULL_PATH, contentService.expandRelativeSitePath(site, path));
        
        user = (StringUtils.isEmpty(user)) ? securityService.getCurrentUser() : user;
        model.put(DmConstants.KEY_USER, user);
        model.put(DmConstants.KEY_CONTENT_TYPE, contentType);
        model.put(DmConstants.CONTENT_LIFECYCLE_OPERATION, operation);
        model.put(DmConstants.KEY_CONTENT_LOADER, new XmlContentLoader());
        model.put(DmConstants.KEY_APPLICATION_CONTEXT, ApplicationContextProvider.getApplicationContext());
        if (params != null) {
            for (String key : params.keySet()) {
                model.put(key, params.get(key));
            }
        }
        return model;
    }

    /**
     * XmlContentLoader that provides XML document from the path provided
     *
     * @author hyanghee
     * @author Dejan Brkic
     *
     */
    public class XmlContentLoader implements Serializable {
        private static final long serialVersionUID = -7848136703282922101L;

        /**
         * default constructor
         */
        public XmlContentLoader() {};

        /**
         *
         * @param servicesManager
         */

        /**
         * return XML document
         *
         * @param fullPath
         * @return
         */
        public Document getContent(String fullPath) {
            InputStream is = null;
            try {
                is = contentService.getContent(fullPath);
                SAXReader saxReader = new SAXReader();
                Document content = saxReader.read(is);
                return content;
            } catch (DocumentException e) {
                logger.error("Error while reading content from " + fullPath, e);
                if (is != null) {
                    ContentUtils.release(is);
                }
                return null;
            } catch (ContentNotFoundException e) {
                logger.error("Error while reading content from " + fullPath, e);
                if (is != null) {
                    ContentUtils.release(is);
                }
                return null;
            }
        }
    }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public ScriptExecutor getScriptExecutor() { return scriptExecutor; }
    public void setScriptExecutor(ScriptExecutor scriptExecutor) { this.scriptExecutor = scriptExecutor; }

    protected ContentService contentService;
    protected SecurityService securityService;
    protected ScriptExecutor scriptExecutor;
}
