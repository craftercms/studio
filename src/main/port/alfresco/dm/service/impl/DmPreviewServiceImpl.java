/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.service.impl;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import javolution.util.FastMap;


import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.NodeRef;


import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.executor.ProcessContentExecutor;
import org.craftercms.cstudio.alfresco.dm.service.api.DmContentService;
import org.craftercms.cstudio.alfresco.dm.service.api.DmPreviewService;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.CStudioNodeService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Dejan Brkic
 */
public class DmPreviewServiceImpl extends AbstractRegistrableService implements DmPreviewService{

    private static final Logger logger = LoggerFactory.getLogger(DmPreviewServiceImpl.class);
    
    private static final String PREVIEW_FOLDER = "temp";

    /**
     * Dm Content Processor
     */
    protected ProcessContentExecutor _contentProcessor;
    public ProcessContentExecutor getContentProcessor() {
        return _contentProcessor;
    }
    public void setContentProcessor(ProcessContentExecutor contentProcessor) {
        this._contentProcessor = contentProcessor;
    }

    @Override
    public void register() {
        getServicesManager().registerService(DmPreviewService.class, this);
    }

    @Override
    public void deleteContent(String site, String relativePath) throws ServiceException {
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, relativePath);
        DmPathTO dmPath = new DmPathTO(fullPath);
        String previewFullPath = dmPath.toString();
        getService(PersistenceManagerService.class).deleteNode(previewFullPath);
    }

    @Override
    public void writeContent(String site, String path, String fileName, String contentType, InputStream in) throws ServiceException {
        writeContent(site, path, fileName, contentType, in,false);
    }

    @Override
    public void writeContent(String site, String path, String fileName, String contentType, InputStream in, boolean duplicate) throws ServiceException {
        //String sandbox = _servicesConfig.getSandbox(site);

        /* Disable DRAFT repo Dejan 29.03.2012 */
    	//path = DmUtils.getPreviewPath(path);
        /***************************************/
        String id = site + ":" + path + ":" + fileName + ":" + contentType;
        Map<String, String> params = new FastMap<String, String>();
        params.put(DmConstants.KEY_SITE, site);
        //params.put(DmConstants.KEY_SANDBOX, sandbox);
        params.put(DmConstants.KEY_PATH, path);
        params.put(DmConstants.KEY_FILE_NAME, fileName);
        params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
        params.put(DmConstants.KEY_CREATE_FOLDERS, "true");
        params.put(DmConstants.KEY_OVERWRITE, "true");
        params.put(DmConstants.KEY_IS_PREVIEW, "true");
        String user = getService(PersistenceManagerService.class).getCurrentUserName();
        params.put(DmConstants.KEY_USER, user);
        if (logger.isDebugEnabled()) {
            logger.debug("Writing to preview "+path+"/"+fileName);
        }

        if (!StringUtils.isEmpty(contentType)) {
            _contentProcessor.processContent(id, in, true, params, DmConstants.CONTENT_CHAIN_FORM_PREVIEW);
        } else {
            if (fileName.endsWith(DmConstants.XML_PATTERN)) {
                _contentProcessor.processContent(id, in, true, params, DmConstants.CONTENT_CHAIN_PLAIN_PREVIEW);
            } else {
                _contentProcessor.processContent(id, in, false, params, DmConstants.CONTENT_CHAIN_ASSET_PREVIEW);
            }
        }
    }

    @Override
    public void cleanContent(String site, String path) throws ServiceException {
        DmContentService dmContentService = getService(DmContentService.class);
        String basePath = dmContentService.getContentFullPath(site, path, false);
        String previewPath = DmUtils.getPreviewPath(path);
        String fullPreviewPath = DmUtils.getAbsoluteDraftPath(path, basePath);
        revert(site, path, fullPreviewPath,previewPath);
    }

    protected void revert(String site, String path, String fullPreviewPath, String previewPath) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
    	NodeRef node = persistenceManagerService.getNodeRef(fullPreviewPath);
    	
        if (node != null) {
            // /site/webiste/parent1/parent2/index.xml
            // if new, check if we need to revert its parent
            //String[] levels = previewFullPath.split("/"); // [site, website, parent1, parent2, index.xml]
            String[] levels = previewPath.split("/");
            String childPath = ""; // /index.xml
            String currentPath = previewPath; // /site/website/parent1/parent2
            //String currentFullPath = previewFullPath;
            String currentFullPath = fullPreviewPath;
            //String revertPath = previewFullPath;
            String revertPath = fullPreviewPath;
            DmContentService dmContentService = getService(DmContentService.class);
            
            for (int index = levels.length - 1; index >= 0; index--) {
                // if the parent is not new, we don't need to revert the parent
                if (!dmContentService.isNew(site, currentPath)) {
                    revertPath = currentFullPath;
                    break;
                } else {
                    String currentLevel = levels[index];
                    childPath = "/" + currentLevel + childPath; // /index.xml
                    // if the parent is new but there is another child, we cannot revert the parent
                    String parentFullPath = DmUtils.getParentUrl(currentFullPath);
                    String parentPath = DmUtils.getParentUrl(currentPath);
                    if (!dmContentService.isNew(site, parentPath)) {
                        revertPath = currentFullPath;
                        break;
                    } else {
                        List<FileInfo> children = persistenceManagerService.list(parentFullPath);
                        if (children != null && children.size() > 1) {
                            revertPath = currentFullPath;
                            break;
                        } else {
                            currentPath = parentPath;
                            currentFullPath = parentFullPath;
                        }
                    }
                }
            }
            persistenceManagerService.deleteNode(revertPath);
        } else {
            if (logger.isDebugEnabled()) {
                //logger.debug("Reverting Deleted Node [" + previewFullPath + "]");
                logger.debug("Reverting Deleted Node [" + fullPreviewPath + "]");
            }
            NodeRef deletedNode = persistenceManagerService.getNodeRef(fullPreviewPath);
            if (deletedNode != null) {
                if (logger.isDebugEnabled()) {
                    //logger.debug("About to revert a deleted Node [" + previewFullPath + "]");
                    logger.debug("About to revert a deleted Node [" + fullPreviewPath + "]");
                }
                persistenceManagerService.deleteNode(fullPreviewPath);
            }
            if (logger.isDebugEnabled()) {
                logger.debug(fullPreviewPath + " does not exist. No need to delete.");
            }
        }
    }
}
