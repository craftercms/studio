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
package org.craftercms.studio.impl.v1.service.clipboard;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.content.pipeline.DmContentProcessor;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.clipboard.ClipboardService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.*;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.DmPasteItemTO;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v1.util.PathMacrosTransaltor;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;

import javax.swing.text.html.parser.ContentModel;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClipboardServiceImpl extends AbstractRegistrableService implements ClipboardService {

    protected static final Logger logger = LoggerFactory.getLogger(ClipboardServiceImpl.class);

    public final static Pattern FILE_PATTERN = Pattern.compile("(.+)-([0-9]+)\\.(.+)");
    public final static Pattern FOLDER_PATTERN = Pattern.compile("(.+)-([0-9]+)");

    @Override
    public void register() {
        getServicesManager().registerService(ClipboardService.class, this);
    }

    /**
     * Entry point for all past operation
     *
     * @param pasteItems - items that needs to be pasted in the target location
     * @param destination - target location where the items have to be pasted
     * @cut - is it a cut/paste or a copy/paste operation
     *
     */
    public List<String> paste(final String site, List<Map<String, String>> pasteItems, final String destination, boolean cut) throws ServiceException {

        if (cut) {
            return cutPaste(site, pasteItems, destination);
        }
        return copyPaste(site, pasteItems, destination);
    }
/*
    @Override
    public String duplicateToDraft(String site, String sub, String path) throws ServiceException {
        return _duplicate(site, sub, path, true,path);
    }

    /**
     * Duplicate the content (i.e create a copy of a content at the same location by providing auto-generated uri)
     *
     */
    @Override
	public String duplicate(String site, String path, String source) throws ServiceException {
		return _duplicate(site, path, false, source);
    }

	protected String _duplicate(String site, String path, boolean toDraft, String source) throws ServiceException {
        String user = securityService.getCurrentUser();
        String fullPath = contentService.expandRelativeSitePath(site, path);
        try {
            ContentItemTO item = contentService.getContentItem(site, path);
            if (item != null) {
                ContentTypeConfigTO config = getContentTypeConfig(site, path);
                boolean contentAsFolder = (config != null) ? config.isContentAsFolder() : false;
                if (!contentAsFolder && path.endsWith(DmConstants.INDEX_FILE)) {
                    contentAsFolder = true;
                }
                String namePath = path;
                // if the content is being saved as folder, the name path will be the folder name
                if (contentAsFolder && path.endsWith(DmConstants.INDEX_FILE)) {
                    namePath = namePath.replace("/" + DmConstants.INDEX_FILE, "");
                }
                String name = contentService.getNextAvailableName(site, namePath);
				InputStream content = getDuplicatedContent(site, user, source, name, contentAsFolder);
                String fileName = (contentAsFolder) ? DmConstants.INDEX_FILE : name;
                String newPath = namePath.substring(0, namePath.lastIndexOf("/"));
                newPath = (contentAsFolder) ? newPath + "/" + name : newPath;
                //DmPathTO dmPath = new DmPathTO(fullPath);
                String destPath = contentService.expandRelativeSitePath(site, newPath);
                //DmPathTO destDmPath = new DmPathTO(destPath);
                String copiedPath = copy(site, user, null,path, newPath, fileName, content,toDraft, DmContentLifeCycleService.ContentLifeCycleOperation.DUPLICATE);
                ContentItemTO itemTo = contentService.getContentItem(site, newPath + "/" + fileName);
                objectStateService.insertNewEntry(site, newPath + "/" + fileName);
                return newPath + "/" + fileName;
            } else {
                throw new ContentNotFoundException(path + " does not exist.");
            }
        }finally{
            //AuthenticationUtil.setFullyAuthenticatedUser(user);
        }
    }

    /**
     * Copy the given node to the destination node
     * Provide new pageId/groupId to XMLs
     * Make copies of dependencies if required
     *
     */
    protected String copy(String site, String user, SourcePageInfo sourcePageInfo, String path, String destination, String destinationFileName, InputStream content, boolean isDraft, DmContentLifeCycleService.ContentLifeCycleOperation writeOperation) throws ServiceException {

        if(sourcePageInfo == null){
            sourcePageInfo = new SourcePageInfo(path);
        }

        String relativePath = contentService.getRelativeSitePath(site, path);
        ContentTypeConfigTO config = getContentTypeConfig(site, relativePath);

        if(content == null){
            content = contentService.getContent(site, relativePath);
        }
        if(relativePath.endsWith(DmConstants.XML_PATTERN)){
            if(sourcePageInfo.getParams() == null){
                Map<String,String> params = contentItemIdGenerator.getIds();//page and groud id
                sourcePageInfo.setParams(params);
            }
            Map<String,String> dependencies = copyDependencies(site, user, sourcePageInfo, path,isDraft);
            //update content with all params
            content = updateContent(site,relativePath,content,sourcePageInfo.getParams(),dependencies);
        }

        String contentType = config==null?null:config.getName();
        String fileName = (StringUtils.isEmpty(destinationFileName)) ? ContentUtils.getPageName(path) : destinationFileName;
        //if(!isDraft){
            writeContent(site, destination, fileName, user, content, contentType, false, writeOperation);
        //}else{
        //    DmPreviewService dmPreviewService = getService(DmPreviewService.class);
        //    dmPreviewService.writeContent(site, destination.getRelativePath(), null, contentType, content);
        //}
        return destination + "/" + fileName;
    }

    /**
     * @param site
     * @param path

     * @param content
     * @param writeOperation
     * @throws ServiceException
     */
    protected void writeContent(String site, String path, String fileName, String user,
                                InputStream content, String contentType, boolean edit, DmContentLifeCycleService.ContentLifeCycleOperation writeOperation) throws ServiceException {
        //AuthenticationUtil.setFullyAuthenticatedUser(user);
        Map<String, String> params = new HashMap<String, String>();
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
        if (!StringUtils.isEmpty(contentType)) {
            contentService.processContent(id, content, true, params, DmConstants.CONTENT_CHAIN_FORM);
        } else {
            if (fileName.endsWith(DmConstants.XML_PATTERN)) {
                contentService.processContent(id, content, true, params, DmConstants.CONTENT_CHAIN_FORM);
            } else {
                contentService.processContent(id, content, false, params, DmConstants.CONTENT_CHAIN_ASSET);
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
        Map<String, String> copyDependencies = dmDependencyService.getCopyDependencies(site, sourcePageInfo.getPath(), dependencyPath);
        Map<String,String> copiedDependenices = new HashMap<String,String>();
        
        for (String path: copyDependencies.keySet()) {
            String target = copyDependencies.get(path);
            String destinationPath = PathMacrosTransaltor.resolvePath(target, sourcePageInfo.getParams()); //get the new target  location
            String destinationFullPath = contentService.expandRelativeSitePath(site, destinationPath);

            //make copy of each dependency to the new location
            String srcFullPath = contentService.expandRelativeSitePath(site, path);
            String copiedPath = copy(site, user, sourcePageInfo, path, destinationPath, null, null, isPreview, DmContentLifeCycleService.ContentLifeCycleOperation.COPY);
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
                is = dmDependencyService.replaceDependencies(site, document, dependencies);

            }
        }
        return is;
    }

    /**
     * get the duplicated content
     *
     * @param site
     * @param path
     * @param name
     * @param contentAsFolder
     * @return duplicated content
     * @throws ContentNotFoundException
     */
    protected InputStream getDuplicatedContent(String site, String user, String path, String name, boolean contentAsFolder) throws ContentNotFoundException {
        // if it is XML, change the internal name with the next number
        if (path.endsWith(DmConstants.XML_PATTERN)) {
            Document document = null;
            try {
                document = contentService.getContentAsDocument(contentService.expandRelativeSitePath(site, path));
            } catch (DocumentException e) {
                logger.error("Error getting xml document for following path: " + contentService.expandRelativeSitePath(site, path));
            }
            if (document != null) {
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
                return null;
            }
        } else {
            // otherwise, return the content as is
            return contentService.getContent(site, path);
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
    protected ContentTypeConfigTO getContentTypeConfig(String site,  String path) {
        try {
            return contentTypeService.getContentTypeByRelativePath(site, path);
        } catch (ServiceException e) {
            // ignore the error
            logger.warn("No content type found for " + path);
        }
        return null;
    }

    /**
     * Cut/Paste calls the Rename service <link>WcmRenameService</link>
     */
    protected List<String> cutPaste(final String site, List<Map<String, String>> pasteItems, final String destination) throws ServiceException {
        for (final Map<String, String> pasteItem : pasteItems) {
            String path = pasteItem.get("uri");
            String fullPath = contentService.expandRelativeSitePath(site, path);
            objectStateService.setSystemProcessing(site, path, true);
            String destinationUri = destination;
            if(destination.endsWith(DmConstants.INDEX_FILE)){
                destinationUri = ContentUtils.getParentUrl(destinationUri);
            }

            destinationUri = destinationUri+ "/" + ContentUtils.getPageName(path); //we will need to provide the destination folder name to the rename service

            if(contentService.contentExists(site, destinationUri)){
                throw new ServiceException("Content already exists [" + site + ":" + path +"]");
            }else{
                dmRenameService.rename(site, path, destinationUri, false);
                updateFileWithNewNavOrder(site, destinationUri);//only for cut/paste will need to provide new navorder value right here since it doesnt go through FormContentProcessor
                //fullPath = servicesConfig.getRepositoryRootPath(site) + destinationUri;
                ContentItemTO itemTO = contentService.getContentItem(destinationUri);
                objectStateService.transition(site, itemTO, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE);
            }
            objectStateService.setSystemProcessing(site, destinationUri, false);
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
    protected void updateFileWithNewNavOrder(String site, String path){
        if(!path.endsWith(DmConstants.XML_PATTERN)) {
            path = path + "/" + DmConstants.INDEX_FILE;
        }
        String user = securityService.getCurrentUser();
        try{
            ContentItemTO item = contentService.getContentItem(site, path);
            if (item != null) {
                Document document = contentService.getContentAsDocument(contentService.expandRelativeSitePath(site, path));
                DmPageNavigationOrderService dmPageNavigationOrderService = getService(DmPageNavigationOrderService.class);
                dmPageNavigationOrderService.addNavOrder(site, path, document);
                InputStream content = ContentUtils.convertDocumentToStream(document, CStudioConstants.CONTENT_ENCODING);
                updateFileDirect(site, path, content);
            }
        }catch(Exception e){
            logger.warn("Error while update file with new Nav order "+ path,e);
        }finally{
            //AuthenticationUtil.setFullyAuthenticatedUser(user);
        }
    }

    /**
     * Update file directly rather than going through the pipeline processor
     * TODO - remove this to use the pipeline processor
     */
    protected void updateFileDirect(String site, String relativePath,InputStream input){
        try {
            contentService.writeContent(site, relativePath, input);
        } finally {
            ContentUtils.release(input);
        }
    }

    /**
     * Copy/Paste called with items to be pasted at the destination
     */
    protected List<String> copyPaste(final String site, List<Map<String, String>> pasteItems, final String destination) throws ServiceException {

        final String user = securityService.getCurrentUser();
        List<String> copiedItems = new ArrayList<>();
        if (pasteItems != null && pasteItems.size() > 0) {
            String destFullpath = contentService.expandRelativeSitePath(site, destination);
            ContentItemTO destItem = contentService.getContentItem(site, destination);
            if (destItem == null) {
                throw new ServiceException("Error while pasting content. " + destination + " does not exist.");
            }
            String destNodePath = destFullpath;
            if (!destItem.isFolder()) {
                if (destItem.getName().equals(DmConstants.INDEX_FILE)) {
                    destItem = contentService.getContentItem(site, destination.replace("/" + destItem.getName(), ""));
                    destNodePath = destItem.getUri();
                } else {
                    String folderPath = writeProcessor.fileToFolder(site, destNodePath);
                    destItem = contentService.getContentItem(site, folderPath);
                    destNodePath = folderPath;
                }
            }
            for (final Map<String, String> pasteItem : pasteItems) {
                DmPasteItemTO dmPasteItem = new DmPasteItemTO();
                String pasteItemUri = pasteItem.get("uri");
                dmPasteItem.setUri(pasteItemUri);
                dmPasteItem.setDeep(Boolean.parseBoolean(pasteItem.get("deep")));
                if (hasSameChild(pasteItemUri, destItem, site)) {
                    //if the parent folder has the same content then duplicate the content
                    duplicateContent(site, destination, user, destItem, pasteItemUri);
                } else {
                    copyContent(site, user, dmPasteItem, destNodePath, copiedItems);
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
    protected void copyContent(String site, String user, DmPasteItemTO pasteItem, String destination, List<String> copiedItems) throws ServiceException {
        String path = pasteItem.getUri();
        boolean deep = true; //item.isDeep();
        String fullPath = contentService.expandRelativeSitePath(site, path);
        try {
            ContentItemTO contentItem = contentService.getContentItemTree(site, path, 1);
            if (contentItem == null) {
                throw new ServiceException("Error while pasting content. " + path + " does not exist.");
            }

            // check if the original content is DM
            //String nodeRef = getNodeRef(fullPath);
            //if (NodeRef.isNodeRef(nodeRef)) {
            //    String copiedPath = copyDmContent(site, user, nodeRef, fullPath, false);
            //    if (!StringUtils.isEmpty(copiedPath)) copiedItems.add(copiedPath);
            //} else {
            //FileInfo nodeInfo = persistenceManagerService.getFileInfo(node);
            String nodePath = fullPath;
            if (contentItem.isFolder()) {
                copyChildren(site, user, path, contentItem, destination + contentItem.getName(), pasteItem, deep, true, copiedItems);
                if (contentItem.getChildren() == null || contentItem.getChildren().size() == 0) {
                    ContentItemTO destinationParentItem = contentService.getContentItem(site, destination);
                    //writeContent(site, );
                    //copy(site, user, null, nodePath, destination, null, null, false, DmContentLifeCycleService.ContentLifeCycleOperation.COPY);
                    contentService.copyContent(site, path, destination);
                }

            } else if (contentItem.getName().equals(DmConstants.INDEX_FILE)) {

                ContentItemTO parentItem = contentService.getContentItem(site, contentItem.getPath());
                path = path.replace("/" + DmConstants.INDEX_FILE, "");

                //FileInfo parentNodeInfo = persistenceManagerService.getFileInfo(parentNode);
                copyChildren(site, user, path, parentItem, destination + "/" + parentItem.getName(), pasteItem, deep, false, copiedItems);
                //destinationPath = new DmPathTO(destination + "/" + parentItem.getName());

                // copy the index file
                //DmPathTO dmPath = new DmPathTO(nodePath);
                String copiedPath = copy(site, user, null, path, destination + "/" + parentItem.getName(), null, null,false, DmContentLifeCycleService.ContentLifeCycleOperation.COPY);
                //ContentItemTO copiedItem = contentService.getContentItem(site, copiedPath);
                objectStateService.insertNewEntry(site, copiedPath);
                copiedItems.add(copiedPath);
            } else {
                String copiedPath = copy(site, user, null,nodePath, destination, null, null, false, DmContentLifeCycleService.ContentLifeCycleOperation.COPY);
                //ContentItemTO copiedItem = contentService.getContentItem(site, copiedPath);
                objectStateService.insertNewEntry(site, copiedPath);
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
    protected void copyChildren(String site, String user, String parentPath, ContentItemTO parentContentItem,
                                String destination, DmPasteItemTO parentItem, boolean deep, boolean copyIndex, List<String> copiedItems) throws ServiceException {
        if (deep) {
            ContentItemTO parentItemTree = contentService.getContentItemTree(site, parentContentItem.getUri(), 1);
            ContentItemTO indexDescp = null;
            /**
             * first paste all the children and then at last paste current node.
             */
            for (ContentItemTO child : parentItemTree.getChildren()) {
                // using it to paste at last.
                if(child.getName().equals(DmConstants.INDEX_FILE)) {
                    indexDescp = child;
                    continue;
                }
                if (copyIndex || !child.getName().equals(DmConstants.INDEX_FILE)) {
                    copyItem(site, user, parentPath, destination, deep, copiedItems, child);
                }
            }
            if(copyIndex && indexDescp != null) {
                copyItem(site, user, parentPath,destination,deep, copiedItems, indexDescp);
            }
        } else {
            if (parentContentItem.getChildren() != null && parentContentItem.getChildren().size() > 0) {
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
                            List<String> copiedItems, ContentItemTO childItem) throws ServiceException {

        DmPasteItemTO childPasteItem = new DmPasteItemTO();
        String childUri = parentPath + "/" + childItem.getName();
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
    protected boolean hasSameChild(String srcPath, ContentItemTO destination, String site) {
        String originalDirectory = ContentUtils.getParentUrl(srcPath);
        ContentItemTO itemTree = contentService.getContentItemTree(site, destination.getUri(), 1);
        String originalDirectoryFullPath = contentService.expandRelativeSitePath(site, originalDirectory);
        String srcFullPath = contentService.expandRelativeSitePath(site, srcPath);
        //FileInfo srcFileInfo = persistenceManagerService.getFileInfo(srcFullPath);
        if (/*!srcFileInfo.isFolder() && */!srcPath.endsWith(DmConstants.INDEX_FILE)) {
            originalDirectoryFullPath = srcFullPath;
        }
        for (ContentItemTO child : itemTree.getChildren()) {
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
    protected void duplicateContent(final String site, final String destination, final String user, ContentItemTO destItem, final String pasteItem)throws ServiceException {
        String srcFullPath = contentService.expandRelativeSitePath(site, pasteItem);
        ContentItemTO srcItem = contentService.getContentItem(site, pasteItem);
        String destNodePath = contentService.expandRelativeSitePath(site, destItem.getUri());
        final String destRelLoc = getDestPath(site, destItem.getUri(), pasteItem, srcItem);

        //AuthenticationUtil.setFullyAuthenticatedUser(user);


        //Create copies of the child items
        final String newPath = duplicate(site, destRelLoc, pasteItem);
                /*;
        List<DmPasteItemTO> pasteItemTOList = item.getChildren();
        if (pasteItemTOList != null && !pasteItemTOList.isEmpty()) {
            copyPaste(site, pasteItemTOList, newPath);
        }*/
    }

    protected String getDestPath(String site, String destination, String pasteItem, ContentItemTO srcItem) {
        String srcNodePath = contentService.expandRelativeSitePath(site, pasteItem);
		String destRelLoc = destination + '/' + srcItem.getName();
        //if (!srcItem.isFolder()) {
            if (srcItem.getName().equals(DmConstants.INDEX_FILE)) {
                srcItem = contentService.getContentItem(srcNodePath.replace("/" + srcItem.getName(), ""));
				destRelLoc = destination + "/" + srcItem.getName() + "/" + DmConstants.INDEX_FILE;
            }
        //}
		return destRelLoc;
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

    public DmContentProcessor getWriteProcessor() { return writeProcessor; }
    public void setWriteProcessor(DmContentProcessor writeProcessor) { this.writeProcessor = writeProcessor; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public ContentService getContentService() { return contentService; }
    public void setContentService(ContentService contentService) { this.contentService = contentService; }

    public org.craftercms.studio.api.v1.service.objectstate.ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public DmRenameService getDmRenameService() { return dmRenameService; }
    public void setDmRenameService(DmRenameService dmRenameService) { this.dmRenameService = dmRenameService; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public ContentTypeService getContentTypeService() { return contentTypeService; }
    public void setContentTypeService(ContentTypeService contentTypeService) { this.contentTypeService = contentTypeService; }

    public ContentItemIdGenerator getContentItemIdGenerator() { return contentItemIdGenerator; }
    public void setContentItemIdGenerator(ContentItemIdGenerator contentItemIdGenerator) { this.contentItemIdGenerator = contentItemIdGenerator; }

    public DmDependencyService getDmDependencyService() { return dmDependencyService; }
    public void setDmDependencyService(DmDependencyService dmDependencyService) { this.dmDependencyService = dmDependencyService; }

    protected DmContentProcessor writeProcessor;
    protected ServicesConfig servicesConfig;
    protected ContentService contentService;
    protected org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService;
    protected DmRenameService dmRenameService;
    protected SecurityService securityService;
    protected ContentTypeService contentTypeService;
    protected ContentItemIdGenerator contentItemIdGenerator;
    protected DmDependencyService dmDependencyService;
}
