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
package org.craftercms.cstudio.alfresco.dm.service.impl;

import org.alfresco.model.ContentModel;
import org.alfresco.scripts.ScriptException;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.model.FileNotFoundException;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.lang.StringUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentLifeCycleService;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.ServicesManager;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DmContentLifeCycleServiceImpl extends AbstractRegistrableService implements DmContentLifeCycleService{

    private static final Logger logger = LoggerFactory.getLogger(DmContentLifeCycleServiceImpl.class);

    /**
     * metadata extraction script location
     */
    protected String _scriptLocation;
    public String getScriptLocation() {
        return _scriptLocation;
    }
    public void setScriptLocation(String scriptLocation) {
        this._scriptLocation = scriptLocation;
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
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        if (operation == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("No lifecycle operation provided for " + site + ":" + path);
            }
            return;
        }
        if (StringUtils.isEmpty(contentType)) {
            if (logger.isWarnEnabled()) {
                logger.warn("Skipping content lifecycle script execution. no content type provided for " + site + ":" + path);
            }
            return;
        }
        // find the script ref based on content type
        NodeRef scriptNodeRef = getScriptRef(site, contentType);
        if (scriptNodeRef == null) {
            if (logger.isErrorEnabled()) {
                logger.error("No script found at " + _scriptLocation.replaceAll(CStudioConstants.PATTERN_SITE, site)
                        .replaceAll(CStudioConstants.PATTERN_CONTENT_TYPE, contentType) + ", contentType: " + contentType);
            }
            return;
        }
        ContentService contentService = getService(ContentService.class);
        ContentReader contentReader = contentService.getReader(scriptNodeRef, ContentModel.PROP_CONTENT);
        String script= contentReader.getContentString();
//        script += "if (controller){" +
//                     "controller.execute();" +
//                "};";
        Map<String, Object> model = buildModel(site, user, path, contentType, operation.toString(), params);
        try {
            persistenceManagerService.executeScriptString(script, model);
        } catch (ScriptException e) {
            if (logger.isErrorEnabled()) {
                logger.error("Error while executing content lifecycle script for " + site + ":" + path, e);
            }
        }
    }

    /**
     * get the content metadata extraction script
     *
     * @param contentType
     * @return nodeRef of the script
     */
    protected NodeRef getScriptRef(String site, String contentType) {
        String location = _scriptLocation.replaceAll(CStudioConstants.PATTERN_SITE, site)
                .replaceAll(CStudioConstants.PATTERN_CONTENT_TYPE, contentType);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return persistenceManagerService.getNodeRef(location);
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
        model.put(DmConstants.KEY_FULL_PATH, this.getContentFullPath(site, path));
        
        user = (StringUtils.isEmpty(user)) ? getService(PersistenceManagerService.class).getCurrentUserName() : user;
        model.put(DmConstants.KEY_USER, user);
        model.put(DmConstants.KEY_CONTENT_TYPE, contentType);
        //sandbox = _servicesConfig.getSandbox(site);
        //model.put(DmConstants.KEY_SCRIPT_SANDBOX, sandbox);
        model.put(DmConstants.CONTENT_LIFECYCLE_OPERATION, operation);
        model.put(DmConstants.KEY_CONTENT_LOADER, new XmlContentLoader(getServicesManager()));
        if (params != null) {
            for (String key : params.keySet()) {
                model.put(key, params.get(key));
            }
        }
        return model;
    }

    /**
     * get content full path
     *
     * @param site
     * @param path
     * @return
     */
    protected String getContentFullPath(String site, String path) {
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String nodePath = servicesConfig.getRepositoryRootPath(site) + path;
        return nodePath;
    }

    protected String _getNodePath(NodeRef node) {
        List<FileInfo> pathParts = null;
        try {
            pathParts = getService(PersistenceManagerService.class).getNamePath(_getCompanyHomeNodeRef(), node);
        } catch (FileNotFoundException e) {
            logger.error("ERROR: ", e);
        }
        String nodePath = "";
        for (FileInfo pathPart : pathParts) {
            nodePath = nodePath + "/" + pathPart.getName();
        }
        return nodePath;
    }

    protected NodeRef _getCompanyHomeNodeRef() {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        return persistenceManagerService.getCompanyHomeNodeRef();
    }

    /**
     * XmlContentLoader that provides XML document from the path provided
     *
     * @author hyanghee
     * @author Dejan Brkic
     *
     */
    public class XmlContentLoader implements Serializable {


        private static final long serialVersionUID = -6947731457966015604L;


        /**
         * default constructor
         */
        public XmlContentLoader() {};

        /**
         *
         * @param servicesManager
         */
        public XmlContentLoader(ServicesManager servicesManager) {
            this._servicesManager = servicesManager;
        }

        protected ServicesManager _servicesManager;
        public ServicesManager getServicesManager() {
            return _servicesManager;
        }
        public void setServicesManager(ServicesManager servicesManager) {
            this._servicesManager = servicesManager;
        }

        /**
         * return XML document
         *
         * @param fullPath
         * @return
         */
        public Document getContent(String fullPath) {
            PersistenceManagerService persistenceManagerService = getServicesManager().getService(PersistenceManagerService.class);
            InputStream is = null;
            try {
                NodeRef contentNode = persistenceManagerService.getNodeRef(fullPath);
                is = getService(PersistenceManagerService.class).getReader(contentNode).getContentInputStream();
                SAXReader saxReader = new SAXReader();
                Document content = saxReader.read(is);
                return content;
            } catch (DocumentException e) {
                if (logger.isErrorEnabled()) {
                    logger.error("Error while reading content from " + fullPath, e);
                }
                if (is != null) {
                    ContentUtils.release(is);
                }
                return null;
            }
        }

    }
}
