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

import javolution.util.FastList;
import javolution.util.FastMap;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.permissions.AccessDeniedException;
import org.alfresco.repo.transaction.RetryingTransactionHelper;
import org.alfresco.service.cmr.model.FileInfo;
import org.alfresco.service.cmr.repository.DuplicateChildNodeNameException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.cstudio.alfresco.constant.CStudioConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.constant.DmXmlConstants;
import org.craftercms.cstudio.alfresco.dm.content.pipeline.api.DmContentProcessor;
import org.craftercms.cstudio.alfresco.dm.service.api.*;
import org.craftercms.cstudio.alfresco.dm.to.DmPasteItemTO;
import org.craftercms.cstudio.alfresco.dm.to.DmPathTO;
import org.craftercms.cstudio.alfresco.dm.util.DmUtils;
import org.craftercms.cstudio.alfresco.dm.util.impl.PathMacrosTransaltor;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.api.ObjectStateService;
import org.craftercms.cstudio.alfresco.service.api.PersistenceManagerService;
import org.craftercms.cstudio.alfresco.service.api.ServicesConfig;
import org.craftercms.cstudio.alfresco.service.exception.ContentNotFoundException;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.craftercms.cstudio.alfresco.to.ContentTypeConfigTO;
import org.craftercms.cstudio.alfresco.util.ContentFormatUtils;
import org.craftercms.cstudio.alfresco.util.ContentUtils;
import org.craftercms.cstudio.alfresco.util.TransactionHelper;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DmClipboardServiceImpl extends AbstractRegistrableService implements DmClipboardService{

    protected static final Logger logger = LoggerFactory.getLogger(DmClipboardServiceImpl.class);

    public final static Pattern FILE_PATTERN = Pattern.compile("(.+)-([0-9]+)\\.(.+)");
    public final static Pattern FOLDER_PATTERN = Pattern.compile("(.+)-([0-9]+)");

    /**
     * CStudio DmContentProcessor
     */
    protected DmContentProcessor _writeProcessor;
    public DmContentProcessor getWriteProcessor() {
        return _writeProcessor;
    }
    public void setWriteProcessor(DmContentProcessor writeProcessor) {
        this._writeProcessor = writeProcessor;
    }

    @Override
    public void register() {
        getServicesManager().registerService(DmClipboardService.class, this);
    }

    /**
     * Entry point for all past operation
     *
     * @param pasteItems - items that needs to be pasted in the target location
     * @param destination - target location where the items have to be pasted
     * @cut - is it a cut/paste or a copy/paste operation
     *
     */
    public List<String> paste(final String site, List<DmPasteItemTO> pasteItems, final String destination, boolean cut) throws ServiceException {

        if (cut) {
            return cutPaste(site, pasteItems, destination);
        }
        return copyPaste(site, pasteItems, destination);
    }

    @Override
    public String duplicateToDraft(String site, String sub, String path) throws ServiceException {
        return _duplicate(site, sub, path, true,path);
    }

    /**
     * Duplicate the content (i.e create a copy of a content at the same location by providing auto-generated uri)
     *
     */
	public String duplicate(String site, String sub, String path, String source) throws ServiceException {
		return _duplicate(site, sub, path, false, source);
    }

	protected String _duplicate(String site, String sub, String path, boolean toDraft, String source) throws ServiceException {
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        DmContentService dmContentService = getService(DmContentService.class);
        String fullPath = dmContentService.getContentFullPath(site, path);
        try {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef node = persistenceManagerService.getNodeRef(fullPath);
            if (node != null) {
                ContentTypeConfigTO config = getContentTypeConfig(site, sub, path);
                boolean contentAsFolder = (config != null) ? config.isContentAsFolder() : false;
                if (!contentAsFolder && path.endsWith(DmConstants.INDEX_FILE)) {
                    contentAsFolder = true;
                }
                String namePath = path;
                // if the content is being saved as folder, the name path will be the folder name
                if (contentAsFolder && path.endsWith(DmConstants.INDEX_FILE)) {
                    namePath = namePath.replace("/" + DmConstants.INDEX_FILE, "");
                }
                String name = dmContentService.getNextAvailableName(site, sub, namePath);
				InputStream content = getDuplicatedContent(site, user, sub, source, name, contentAsFolder);
                String fileName = (contentAsFolder) ? DmConstants.INDEX_FILE : name;
                String newPath = namePath.substring(0, namePath.lastIndexOf("/"));
                newPath = (contentAsFolder) ? newPath + "/" + name : newPath;
                DmPathTO dmPath = new DmPathTO(fullPath);
                String destPath = dmContentService.getContentFullPath(site, newPath);
                DmPathTO destDmPath = new DmPathTO(destPath);
                String copiedPath = copy(site, user, null,dmPath, destDmPath, fileName, content,toDraft, DmContentLifeCycleService.ContentLifeCycleOperation.DUPLICATE);
                ServicesConfig servicesConfig = getService(ServicesConfig.class);
                String newFullPath = servicesConfig.getRepositoryRootPath(site) + newPath + "/" + fileName;
                persistenceManagerService.insertNewObjectEntry(newFullPath);
                return newPath + "/" + fileName;
            } else {
                throw new ContentNotFoundException(path + " does not exist.");
            }
        }finally{
            AuthenticationUtil.setFullyAuthenticatedUser(user);
        }
    }

    /**
     * Copy the given node to the destination node
     * Provide new pageId/groupId to XMLs
     * Make copies of dependencies if required
     *
     */
    protected String copy(String site, String user, SourcePageInfo sourcePageInfo, DmPathTO path, DmPathTO destination, String destinationFileName, InputStream content, boolean isDraft, DmContentLifeCycleService.ContentLifeCycleOperation writeOperation) throws ServiceException {

        if(sourcePageInfo == null){
            sourcePageInfo = new SourcePageInfo(path.getRelativePath());
        }

        String relativePath = path.getRelativePath();
        ContentTypeConfigTO config = getContentTypeConfig(site, null, relativePath);

        if(content == null){
            DmContentService dmContentService = getService(DmContentService.class);
            content = dmContentService.getContent(site, relativePath, false, false);
        }
        if(relativePath.endsWith(DmConstants.XML_PATTERN)){
            if(sourcePageInfo.getParams() == null){
                ContentItemIdGenerator contentItemIdGenerator = getService(ContentItemIdGenerator.class);
                Map<String,String> params = contentItemIdGenerator.getIds();//page and groud id
                sourcePageInfo.setParams(params);
            }
            Map<String,String> dependencies = copyDependencies(site, user, sourcePageInfo, path.getRelativePath(),isDraft);
            //update content with all params
            content = updateContent(site,relativePath,content,sourcePageInfo.getParams(),dependencies);
        }

        String contentType = config==null?null:config.getName();
        String fileName = (StringUtils.isEmpty(destinationFileName)) ? path.getName() : destinationFileName;
        if(!isDraft){
            writeContent(site, null, destination.getRelativePath(), fileName, user, content, contentType, false, writeOperation);
        }else{
            DmPreviewService dmPreviewService = getService(DmPreviewService.class);
            dmPreviewService.writeContent(site, destination.getRelativePath(), null, contentType, content);
        }
        return destination.getRelativePath() + "/" + fileName;
    }

    /**
     * @param site
     * @param sub
     * @param path

     * @param content
     * @param writeOperation
     * @throws ServiceException
     */
    protected void writeContent(String site, String sub, String path, String fileName, String user,
                                InputStream content, String contentType, boolean edit, DmContentLifeCycleService.ContentLifeCycleOperation writeOperation) throws ServiceException {
        AuthenticationUtil.setFullyAuthenticatedUser(user);
        Map<String, String> params = new FastMap<String, String>();
        params.put(DmConstants.KEY_SITE, site);
        params.put(DmConstants.KEY_PATH, path);
        params.put(DmConstants.KEY_FILE_NAME, fileName);
        params.put(DmConstants.KEY_USER, user);
        params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
        params.put(DmConstants.KEY_CREATE_FOLDERS, "true");
        params.put(DmConstants.KEY_EDIT, Boolean.valueOf(edit).toString());
        params.put(DmConstants.KEY_ACTIVITY_TYPE, "false");
        params.put(DmConstants.KEY_SKIP_CLEAN_PREVIEW, "true");
        params.put(DmConstants.KEY_COPIED_CONTENT, "true");
        params.put(DmConstants.CONTENT_LIFECYCLE_OPERATION, writeOperation.toString());
        String id = site + ":" + path + ":" + fileName + ":" + contentType;
        // processContent will close the input stream
        DmContentService dmContentService = getService(DmContentService.class);
        if (!StringUtils.isEmpty(contentType)) {
            dmContentService.processContent(id, content, true, params, DmConstants.CONTENT_CHAIN_FORM);
        } else {
            if (fileName.endsWith(DmConstants.XML_PATTERN)) {
                dmContentService.processContent(id, content, true, params, DmConstants.CONTENT_CHAIN_FORM);
            } else {
                dmContentService.processContent(id, content, false, params, DmConstants.CONTENT_CHAIN_ASSET);
            }
        }
    }

    /**
     *
     * Looks for dependencies in the source file and optionally (if mentioned in the config) create copies
     *
     *
     * @param site
     * @param user

     */
    protected Map<String,String> copyDependencies(String site, String user, SourcePageInfo sourcePageInfo,String dependencyPath,boolean isPreview) throws ServiceException{

        //get the dependencies path that are not shared
        DmDependencyService dmDependencyService = getService(DmDependencyService.class);
        Map<String, String> copyDependencies = dmDependencyService.getCopyDependencies(site, sourcePageInfo.getPath(),dependencyPath);
        Map<String,String> copiedDependenices = new HashMap<String,String>();
        DmContentService dmContentService = getService(DmContentService.class);
        
        for (String path: copyDependencies.keySet()) {
            String target = copyDependencies.get(path);
            String destinationPath = PathMacrosTransaltor.resolvePath(target, sourcePageInfo.getParams()); //get the new target  location
            String destinationFullPath = dmContentService.getContentFullPath(site, destinationPath);

            //make copy of each dependency to the new location
            String srcFullPath = dmContentService.getContentFullPath(site, path);
            
            DmPathTO destinationDMPath = new DmPathTO(destinationFullPath);
            DmPathTO dmPath = new DmPathTO(srcFullPath);
            String copiedPath = copy(site, user, sourcePageInfo, dmPath, destinationDMPath, null, null, isPreview, DmContentLifeCycleService.ContentLifeCycleOperation.COPY);
            copiedDependenices.put(path, copiedPath);
        }
        return copiedDependenices;
    }

    /**
     * Update content only for Index pages
     *
     * @param path
     * @param is
     * @param params
     * @param dependencies
     * @return
     * @throws ServiceException
     */
    protected InputStream updateContent(String site,String path, InputStream is, Map<String, String> params, Map<String, String> dependencies) throws ServiceException {

        if (path.endsWith(DmConstants.XML_PATTERN)) {

            Document document;
            try {
                document = ContentUtils.convertStreamToXml(is);
            } catch (DocumentException e) {
                throw new ServiceException("Error while updating content",e);
            }
            if(document!=null){
                //update pageId and groupId with the new one
                Element root = document.getRootElement();
                Node pageIdNode = root.selectSingleNode("//" + DmXmlConstants.ELM_PAGE_ID);
                if (pageIdNode != null) {
                    ((Element) pageIdNode).setText(params.get(DmConstants.KEY_PAGE_ID));
                }
                Node groupIdNode = root.selectSingleNode("//" + DmXmlConstants.ELM_GROUP_ID);
                if (pageIdNode != null) {
                    ((Element) groupIdNode).setText(params.get(DmConstants.KEY_PAGE_GROUP_ID));
                }
                DmDependencyService dmDependencyService = getService(DmDependencyService.class);
                is = dmDependencyService.replaceDependencies(site, document, dependencies);

            }
        }
        return is;
    }

    /**
     * get the duplicated content
     *
     * @param site
     * @param sub
     * @param path
     * @param name
     * @param contentAsFolder
     * @return duplicated content
     * @throws org.alfresco.repo.security.permissions.AccessDeniedException
     * @throws ContentNotFoundException
     */
    protected InputStream getDuplicatedContent(String site, String user, String sub, String path, String name, boolean contentAsFolder) throws AccessDeniedException, ContentNotFoundException {
        // if it is XML, change the internal name with the next number
        DmContentService dmContentService = getService(DmContentService.class);
        if (path.endsWith(DmConstants.XML_PATTERN)) {
            Document document = dmContentService.getContentXml(site, sub, path);
            Pattern pattern = (contentAsFolder) ? FOLDER_PATTERN : FILE_PATTERN;
            final Matcher m = pattern.matcher(name);
            if (m.matches()) {
                String number = m.group(2);
                Element root = document.getRootElement();
                // set internal name to be the same as duplicate content
                Node nameNode = root.selectSingleNode("//" + DmXmlConstants.ELM_INTERNAL_NAME);
                if (nameNode != null) {
                    String nameValue = nameNode.getText();
                    ((Element) nameNode).setText(nameValue + " " + number);
                }
                Node fileNameNode = root.selectSingleNode("//" + DmXmlConstants.ELM_FILE_NAME);
                if (fileNameNode != null) {
                    String fileName = (contentAsFolder) ? DmConstants.INDEX_FILE : name;
                    ((Element) fileNameNode).setText(fileName);
                }
                // set content history - modifier
                Node modifierNode = root.selectSingleNode("//" + DmXmlConstants.ELM_LAST_MODIFIED_BY);
                if (modifierNode != null) {
                    ((Element) modifierNode).setText(user);
                }
                // set content history - modified date
                Node modifiedDateNode = root.selectSingleNode("//" + DmXmlConstants.ELM_LAST_MODIFIED_DATE);
                if (modifiedDateNode != null) {
                    SimpleDateFormat format = new SimpleDateFormat(CStudioConstants.DATE_PATTERN_MODEL);
                    String date = ContentFormatUtils.formatDate(format, new Date());
                    String formDate = ContentFormatUtils.convertToFormDate(date);
                    ((Element) modifiedDateNode).setText(formDate);
                }
            }
            return ContentUtils.convertDocumentToStream(document, CStudioConstants.CONTENT_ENCODING);
        } else {
            // otherwise, return the content as is
            return dmContentService.getContent(site, path, false, false);
        }
    }

    /**
     * get content type configuration of the content at the given path
     *
     * @param site
     * @param sub
     * @param path
     * @return content type configuration
     */
    protected ContentTypeConfigTO getContentTypeConfig(String site, String sub, String path) {
        try {
            DmContentTypeService dmContentTypeService = getService(DmContentTypeService.class);
            return dmContentTypeService.getContentTypeByRelativePath(site, sub, path);
        } catch (ServiceException e) {
            // ignore the error
            if (logger.isWarnEnabled()) {
                logger.warn("No content type found for " + path);
            }
        }
        return null;
    }

    /**
     * Cut/Paste calls the Rename service <link>WcmRenameService</link>
     */
    protected List<String> cutPaste(final String site, List<DmPasteItemTO> pasteItems, final String destination) throws ContentNotFoundException, DuplicateChildNodeNameException {
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        ObjectStateService objectStateService = getService(ObjectStateService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        for (final DmPasteItemTO item : pasteItems) {
            String path = item.getUri();
            String fullPath = servicesConfig.getRepositoryRootPath(site) + path;
            persistenceManagerService.setSystemProcessing(fullPath, true);
            String destinationUri = destination;
            if(destination.endsWith(DmConstants.INDEX_FILE)){
                destinationUri = DmUtils.getParentUrl(destinationUri);
            }

            destinationUri = destinationUri+ "/" + DmUtils.getPageName(path); //we will need to provide the destination folder name to the rename service

            if(dmContentService.contentExists(site, destinationUri, path)){
                NodeRef parentNode = persistenceManagerService.getNodeRef(destinationUri);
                throw new DuplicateChildNodeNameException(parentNode, ContentModel.ASSOC_CONTAINS, DmUtils.getPageName(path), null);
            }else{
                DmRenameService dmRenameService = getService(DmRenameService.class);
                dmRenameService.rename(site, null, path, destinationUri, false);
                updateFileWithNewNavOrder(site, null, destinationUri);//only for cut/paste will need to provide new navorder value right here since it doesnt go through FormContentProcessor
                fullPath = servicesConfig.getRepositoryRootPath(site) + destinationUri;
                objectStateService.transition(fullPath, ObjectStateService.TransitionEvent.SAVE);
            }
            persistenceManagerService.setSystemProcessing(fullPath, false);
        }
        return null;
    }

    /**
     * Update the XML content with new Page Nav Order during copy/paste
     *
     * @param site
     * @param sub
     * @param path
     */
    protected void updateFileWithNewNavOrder(String site, String sub,String path){
        if(!path.endsWith(DmConstants.XML_PATTERN)) {
            path = path + "/" + DmConstants.INDEX_FILE;
        }
        String user = AuthenticationUtil.getFullyAuthenticatedUser();
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        try{
            NodeRef nodeRef = persistenceManagerService.getNodeRef(path);
            if (nodeRef != null) {
                Document document = dmContentService.getContentXml(site, null, path);
                DmPageNavigationOrderService dmPageNavigationOrderService = getService(DmPageNavigationOrderService.class);
                dmPageNavigationOrderService.addNavOrder(site, path, document);
                InputStream content = ContentUtils.convertDocumentToStream(document, CStudioConstants.CONTENT_ENCODING);
                updateFileDirect(site, path, content);
            }
        }catch(Exception e){
            logger.warn("Error while update file with new Nav order "+ path,e);
        }finally{
            AuthenticationUtil.setFullyAuthenticatedUser(user);
        }
    }

    /**
     * Update file directly rather than going through the pipeline processor
     * TODO - remove this to use the pipeline processor
     */
    protected void updateFileDirect(String site, String relativePath,InputStream input){
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String fullPath = dmContentService.getContentFullPath(site, relativePath);// write content
        OutputStream output = null;
        try {
            output = persistenceManagerService.getWriter(fullPath).getContentOutputStream();
            IOUtils.copy(input, output);
        } catch (IOException e) {
            logger.error("Failed to write content to " + relativePath, e);
        } finally {
            ContentUtils.release(output);
            ContentUtils.release(input);
        }
    }

    /**
     * Copy/Paste called with items to be pasted at the destination
     */
    protected List<String> copyPaste(final String site, List<DmPasteItemTO> pasteItems, final String destination) throws ServiceException {

        final String user = AuthenticationUtil.getFullyAuthenticatedUser();
        List<String> copiedItems = new FastList<String>();

        DmContentService dmContentService = getService(DmContentService.class);
        //ObjectStateService objectStateService = getService(ObjectStateService.class);
        if (pasteItems != null && pasteItems.size() > 0) {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            String destFullpath = dmContentService.getContentFullPath(site, destination);
            NodeRef destNode = persistenceManagerService.getNodeRef(destFullpath);
            if (destNode == null) {
                throw new ServiceException("Error while pasting content. " + destination + " does not exist.");
            }
            FileInfo destNodeInfo = getService(PersistenceManagerService.class).getFileInfo(destNode);
            String destNodePath = destFullpath;
            if (!destNodeInfo.isFolder()) {
                if (destNodeInfo.getName().equals(DmConstants.INDEX_FILE)) {
                    destNode = persistenceManagerService.getNodeRef(destNodePath.replace("/" + destNodeInfo.getName(), ""));
                    destNodePath = persistenceManagerService.getNodePath(destNode);
                } else {
                    String folderPath = _writeProcessor.fileToFolder(destNode);
                    destNode = persistenceManagerService.getNodeRef(folderPath);
                    destNodePath = folderPath;
                }
            }
            for (final DmPasteItemTO item : pasteItems) {

                if (hasSameChild(item.getUri(), destNode, site)) {
                    //if the parent folder has the same content then duplicate the content
                    duplicateContent(site, destination, user, destNode, item);
                } else {
                    copyContent(site, user, item, destNodePath, copiedItems);
                }
            }
        }
        return copiedItems;
    }

    /**
     * CopyContent creates copy of the item at the destination
     * If there are item.getChildren it recursively gets called to create copy of them as well
     *
     * @throws ServiceException
     */
    protected void copyContent(String site, String user, DmPasteItemTO item, String destination, List<String> copiedItems) throws ServiceException {
        String path = item.getUri();
        boolean deep = item.isDeep();
        DmContentService dmContentService = getService(DmContentService.class);
        ServicesConfig servicesConfig = getService(ServicesConfig.class);
        String fullPath = dmContentService.getContentFullPath(site, path);
        try {
            PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
            NodeRef node = persistenceManagerService.getNodeRef(fullPath);
            if (node == null) {
                throw new ServiceException("Error while pasting content. " + path + " does not exist.");
            }

            // check if the original content is DM
            //String nodeRef = getNodeRef(fullPath);
            //if (NodeRef.isNodeRef(nodeRef)) {
            //    String copiedPath = copyDmContent(site, user, nodeRef, fullPath, false);
            //    if (!StringUtils.isEmpty(copiedPath)) copiedItems.add(copiedPath);
            //} else {
            FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
            String nodePath = fullPath;
            if (nodeInfo.isFolder()) {
                copyChildren(site, user, path, node, destination + "/" + nodeInfo.getName(), item, deep, true, copiedItems);
                if (item.getChildren() == null) {
                    NodeRef destinationParentNodeRef = persistenceManagerService.getNodeRef(destination);
                    QName assocQName = QName.createQName(ContentModel.TYPE_CONTENT.getNamespaceURI(), QName.createValidLocalName(nodeInfo.getName()));
                    NodeRef copyNode = persistenceManagerService.copy(node, destinationParentNodeRef, ContentModel.ASSOC_CONTAINS, assocQName);
                    Map<QName, Serializable> copyNodeProps = persistenceManagerService.getProperties(copyNode);
                    copyNodeProps.put(ContentModel.PROP_NAME, nodeInfo.getName());
                    persistenceManagerService.setProperties(copyNode, copyNodeProps);
                }
            } else if (nodeInfo.getName().equals(DmConstants.INDEX_FILE)) {

                DmPathTO destinationPath = new DmPathTO(destination);
                
                NodeRef parentNode = persistenceManagerService.getPrimaryParent(node).getParentRef();
                path = path.replace("/" + DmConstants.INDEX_FILE, "");

                FileInfo parentNodeInfo = persistenceManagerService.getFileInfo(parentNode);
                copyChildren(site, user, path, parentNode, destination + "/" + parentNodeInfo.getName(), item, deep, false, copiedItems);
                destinationPath = new DmPathTO(destination + "/" + parentNodeInfo.getName());

                // copy the index file
                DmPathTO dmPath = new DmPathTO(nodePath);
                String copiedPath = copy(site, user, null, dmPath, destinationPath, null, null,false, DmContentLifeCycleService.ContentLifeCycleOperation.COPY);
                String copiedFullPath = servicesConfig.getRepositoryRootPath(site) + "/" + copiedPath;
                persistenceManagerService.insertNewObjectEntry(copiedFullPath);
                copiedItems.add(copiedPath);
            } else {
                DmPathTO destinationPath = new DmPathTO(destination);
                // copy the current file
                DmPathTO avmPath = new DmPathTO(nodePath);

                String copiedPath = copy(site, user, null,avmPath, destinationPath, null, null, false, DmContentLifeCycleService.ContentLifeCycleOperation.COPY);
                String copiedFullPath = servicesConfig.getRepositoryRootPath(site) + "/" + copiedPath;
                persistenceManagerService.insertNewObjectEntry(copiedFullPath);
                copiedItems.add(copiedPath);
            }
            //}
        } catch (Exception e) {
            throw new ServiceException("Error while pasting " + path, e);
        }
    }

    /**
     * Make copy of the child items
     *
     */
    protected void copyChildren(String site, String user, String parentPath, NodeRef parentNode,
                                String destination, DmPasteItemTO parentItem, boolean deep, boolean copyIndex, List<String> copiedItems) throws ServiceException {
        if (deep) {
            List<FileInfo> children = getService(PersistenceManagerService.class).list(parentNode);
            NodeRef indexDescp = null;
            /**
             * first paste all the children and then at last paste current node.
             */
            for (FileInfo child : children) {
                // using it to paste at last.
                if(child.getName().equals(DmConstants.INDEX_FILE)) {
                    indexDescp = child.getNodeRef();
                    continue;
                }
                if (copyIndex || !child.getName().equals(DmConstants.INDEX_FILE)) {
                    copyItem(site, user, parentPath, destination, deep, copiedItems, child.getNodeRef());
                }
            }
            if(copyIndex && indexDescp != null) {
                copyItem(site, user, parentPath,destination,deep, copiedItems, indexDescp);
            }
        } else {
            if (parentItem.getChildren() != null && parentItem.getChildren().size() > 0) {
                for (DmPasteItemTO childPasteItem : parentItem.getChildren()) {
                    copyContent(site, user, childPasteItem, destination, copiedItems);
                }
            }
        }
    }

    /**
     * Ultimately calls <code>copy</code> with required parameters
     */
    protected void copyItem(String site, String user, String parentPath, String destination, boolean deep,
                            List<String> copiedItems,NodeRef childNode) throws ServiceException {
        DmPasteItemTO childPasteItem = new DmPasteItemTO();
        FileInfo childNodeInfo = getService(PersistenceManagerService.class).getFileInfo(childNode);
        String childUri = parentPath + "/" + childNodeInfo.getName();
        childPasteItem.setUri(childUri);
        childPasteItem.setDeep(deep);
        copyContent(site, user, childPasteItem, destination, copiedItems);
    }

    /**
     * check if pasting to the same destination
     * 1) path = destination
     * 2) path's directory path = destination (either folder path or index.xml)
     *
     * @param srcPath
     * @param destination
     * @param site
     * @return true if the same destination
     */
    protected boolean hasSameChild(String srcPath, NodeRef destination, String site) {
        DmContentService dmContentService = getService(DmContentService.class);
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        String originalDirectory = DmUtils.getParentUrl(srcPath);
        List<FileInfo> listingArray = getService(PersistenceManagerService.class).list(destination);
        String originalDirectoryFullPath = dmContentService.getContentFullPath(site, originalDirectory);
        String srcFullPath = dmContentService.getContentFullPath(site, srcPath);
        FileInfo srcFileInfo = persistenceManagerService.getFileInfo(srcFullPath);
        if (!srcFileInfo.isFolder() && !srcPath.endsWith(DmConstants.INDEX_FILE)) {
            originalDirectoryFullPath = srcFullPath;
        }
        for (FileInfo child : listingArray) {
            if(originalDirectoryFullPath.endsWith("/" + child.getName())) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * If the pasted parent page already exist then duplicate the page and create copies of all the children 
     *
     * @param site
     * @param destination
     * @param user
     * @param destNode
     * @param item
     * @throws ServiceException
     */
    protected void duplicateContent(final String site, final String destination, final String user, NodeRef destNode, final DmPasteItemTO item)throws ServiceException {
        DmContentService dmContentService = getService(DmContentService.class);
        DmTransactionService dmTransactionService = getService(DmTransactionService.class);
        String srcFullPath = dmContentService.getContentFullPath(site, item.getUri());
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        NodeRef srcNode = persistenceManagerService.getNodeRef(srcFullPath);
        String destNodePath = persistenceManagerService.getNodePath(destNode);
        final String destRelLoc = getDestPath(destNodePath, item, srcNode);

        //duplicate the item since it already exist in the destination
        TransactionHelper txnHelper = dmTransactionService.getTransactionHelper();
        RetryingTransactionHelper.RetryingTransactionCallback<String> processContentCallBack = new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
            public String execute() throws Throwable {
                AuthenticationUtil.setFullyAuthenticatedUser(user);
				return duplicate(site, null, destRelLoc, item.getUri());
            }
        };

        //Create copies of the child items
        final String newPath = txnHelper.doInTransaction(processContentCallBack);
        TransactionHelper txnHelper2 = dmTransactionService.getTransactionHelper();
        RetryingTransactionHelper.RetryingTransactionCallback<String> processContentCallBack2 = new RetryingTransactionHelper.RetryingTransactionCallback<String>() {
            public String execute() throws Throwable {
                List<DmPasteItemTO> pasteItemTOList = item.getChildren();
                if (pasteItemTOList != null && !pasteItemTOList.isEmpty()) {
                    copyPaste(site, pasteItemTOList, newPath);
                }
                return null;
            }
        };
        txnHelper2.doInTransaction(processContentCallBack2);

    }

    protected String getDestPath(String destination, DmPasteItemTO item, NodeRef srcNode) {
        PersistenceManagerService persistenceManagerService = getService(PersistenceManagerService.class);
        FileInfo srcNodeInfo = persistenceManagerService.getFileInfo(srcNode);
        String srcNodePath = persistenceManagerService.getNodePath(srcNode);
		String destRelFullLoc = destination + '/' + srcNodeInfo.getName();
        if (!srcNodeInfo.isFolder()) {
            if (srcNodeInfo.getName().equals(DmConstants.INDEX_FILE)) {
                srcNode = persistenceManagerService.getNodeRef(srcNodePath.replace("/" + srcNodeInfo.getName(), ""));
                srcNodeInfo = persistenceManagerService.getFileInfo(srcNode);
				destRelFullLoc = destination + "/" + srcNodeInfo.getName() + "/" + DmConstants.INDEX_FILE;
            }
        }
		DmPathTO path = new DmPathTO(destRelFullLoc);
		return path.getRelativePath();
    }

    public class SourcePageInfo {

        protected String path; //parent Page path incase of dependencies

        protected Map<String,String> params; //keep track of source page Ids required by the dependencies

        public SourcePageInfo(String path) {
            super();
            this.path = path;
        }
        public String getPath() {
            return path;
        }
        public void setPath(String path) {
            this.path = path;
        }
        public Map<String, String> getParams() {
            return params;
        }
        public void setParams(Map<String, String> params) {
            this.params = params;
        }

    }
}
