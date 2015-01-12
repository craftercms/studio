/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2015 Crafter Software Corporation.
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
import org.craftercms.profile.api.services.ProfileService;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmMetadataService;
import org.craftercms.studio.impl.v1.util.ValueConverter;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;

import java.util.Map;

public class DmMetadataServiceImpl extends AbstractRegistrableService implements DmMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(DmMetadataServiceImpl.class);

    public static final int VERSION_TO_LOOK_UP = -1;



    /**
     * value converter
     */
    protected ValueConverter converter = new ValueConverter();

    @Override
    public void register() {
        getServicesManager().registerService(DmMetadataService.class, this);
    }

    @Override
    public void extractMetadata(String site, String user, String path, String contentType, Document content) throws ServiceException {
        user = (StringUtils.isEmpty(user)) ? securityService.getCurrentUser() : user;
        String fullPath = contentService.expandRelativeSitePath(site, path);
        if (content == null) {
            try {
                contentService.getContentAsDocument(fullPath);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
        contentType = (StringUtils.isEmpty(contentType)) ? getContentType(content) : contentType;
        /*
        NodeRef actionedUponNodeRef = (nodeRef != null) ? nodeRef : persistenceManagerService.getNodeRef(dmContentService.getContentFullPath(site, path));

        // Find js location
        NodeRef scriptNodeRef = getScriptRef(site, contentType);
        if (scriptNodeRef != null) {
            // find all node references to creat base model
            
            NodeRef spaceRef = persistenceManagerService.getPrimaryParent(actionedUponNodeRef).getParentRef();
            ProfileService profileService = getService(ProfileService.class);
            NodeRef personRef = profileService.getUserRef(user);
            NodeRef homeSpaceRef = (NodeRef) persistenceManagerService.getProperty(personRef, ContentModel.PROP_HOMEFOLDER);
            
            Map<String, Object> model = persistenceManagerService.buildDefaultModel(personRef,
                    persistenceManagerService.getCompanyHomeNodeRef(), homeSpaceRef, scriptNodeRef,
                    actionedUponNodeRef, spaceRef);
            // put any script object needed
            for (String scriptObjectName : _scriptObjects.keySet()) {
                model.put(scriptObjectName, _scriptObjects.get(scriptObjectName));
            }
            if (!StringUtils.isEmpty(actionedUponNodeRef.getId())) {
                model.put(DmConstants.KEY_NODE_REF, actionedUponNodeRef);
            }
            model.put(DmConstants.KEY_SCRIPT_DOCUMENT, content.getDocument());
            model.put(DmConstants.KEY_SITE, site);
            model.put(DmConstants.KEY_PATH, path);
            model.put(DmConstants.KEY_USER, user);
            model.put(DmConstants.KEY_CONTENT_TYPE, contentType);
            model.put(DmConstants.KEY_SCRIPT_NODE, createScriptNode(actionedUponNodeRef));
            model.put(DmConstants.KEY_SCRIPT_CONVERTER, _converter);
            try {
                persistenceManagerService.executeScript(scriptNodeRef, ContentModel.PROP_CONTENT, model);
            } catch (Exception e) {
                throw new ServiceException(e);
            }
        } else {
            if (logger.isErrorEnabled()) {
                String scriptLocation = _scriptLocation.replaceAll(CStudioConstants.PATTERN_SITE, site)
                        .replaceAll(CStudioConstants.PATTERN_CONTENT_TYPE, contentType);
                logger.error("No script found at " + scriptLocation + ", contentType: " + contentType);
            }
        }*/
    }

    /**
     * get content type
     *
     * @param content
     * @return
     */
    protected String getContentType(Document content) {
        if (content != null) {
            Element root = content.getRootElement();
            return root.valueOf("//" + DmXmlConstants.ELM_CONTENT_TYPE);
        } else {
            return null;
        }
    }

    /**
     * get the content metadata extraction script
     *
     * @param contentType
     * @return nodeRef of the script
     *//*
    protected NodeRef getScriptRef(String site, String contentType) {
        String location = _scriptLocation.replaceAll(CStudioConstants.PATTERN_SITE, site)
                .replaceAll(CStudioConstants.PATTERN_CONTENT_TYPE, contentType);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef node = persistenceManagerService.getNodeRef(location);
        return node;
    }*/

    /**
     * create scriptNode for the given nodeRef
     *
     * @param nodeRef
     * @return scriptNode
     *//*
    protected ScriptNode createScriptNode(NodeRef nodeRef) {
        ServiceRegistry serviceRegistry = getService(ServiceRegistry.class);
        if (nodeRef.getStoreRef().getProtocol().equals(StoreRef.PROTOCOL_WORKSPACE)) {
            return new ScriptNode(nodeRef, serviceRegistry);
        } else {
        //PORT    return new AVMNode(nodeRef, serviceRegistry);
            return null;
        }
    }*/

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
    protected Map<String, Object> scriptObjects;
    public Map<String, Object> getScriptObjects() {
        return scriptObjects;
    }
    public void setScriptObjects(Map<String, Object> scriptObjects) {
        this.scriptObjects = scriptObjects;
    }

    protected SecurityService securityService;
    public SecurityService getSecurityService() {return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    protected ContentService contentService;
    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }
}
