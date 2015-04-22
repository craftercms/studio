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
package org.craftercms.studio.impl.v1.service.content;

import java.io.*;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.dal.ObjectState;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.executor.ProcessContentExecutor;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.DmRenameService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.DebugUtils;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;

import org.apache.commons.io.IOUtils;

/**
 * Content Services that other services may use
 * @author russdanner
 */
public class ContentServiceImpl implements ContentService {

    protected static final String MSG_ERROR_IO_CLOSE_FAILED = "err_io_closed_failed";

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    /**
     * file and foler name patterns for copied files and folders *
     */
    public final static Pattern COPY_FILE_PATTERN = Pattern.compile("(.+)-([0-9]+)\\.(.+)");
    public final static Pattern COPY_FOLDER_PATTERN = Pattern.compile("(.+)-([0-9]+)");

    @Override
    public boolean contentExists(String site, String path) {
        return this._contentRepository.contentExists(expandRelativeSitePath(site, path));
    }

    @Override
    public boolean contentExists(String fullPath) {
        return this._contentRepository.contentExists(fullPath);
    }

    @Override
    public InputStream getContent(String path) throws ContentNotFoundException {
       return this._contentRepository.getContent(path);
    }

    @Override
    public InputStream getContent(String site, String path) throws ContentNotFoundException {
       return this._contentRepository.getContent(expandRelativeSitePath(site, path));
    }

    @Override
    public String getContentAsString(String path)  {
        String content = null;

        try {
            content = IOUtils.toString(_contentRepository.getContent(path));
        }
        catch(Exception err) {
            logger.error("Failed to get content as string for path {0}", err, path);
        }

        return content;
    }

    @Override
    public Document getContentAsDocument(String path)
    throws DocumentException {
        Document retDocument = null;
        InputStream is = null;
        try {
            is = this.getContent(path);
        } catch (ContentNotFoundException e) {
            logger.error("Content not found for path {0}", e, path);
        }

        if(is != null) {
            try {
                SAXReader saxReader = new SAXReader();          
                retDocument = saxReader.read(is);
            } 
            finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } 
                catch (IOException err) {
                    logger.error(MSG_ERROR_IO_CLOSE_FAILED, err, path);
                }
            }       
        }

        return retDocument;
    }


    @Override
    public boolean writeContent(String path, InputStream content) {

       boolean writeSuccess = false;

        writeSuccess = _contentRepository.writeContent(path, content);
        _contentRepository.createVersion(path, false);
 
       return writeSuccess;
    }

    @Override
    public void writeContent(String site, String path, String fileName, String contentType, InputStream input,
                             String createFolders, String edit, String unlock) throws ServiceException {
        Map<String, String> params = new HashMap<String, String>();
        params.put(DmConstants.KEY_SITE, site);
        params.put(DmConstants.KEY_PATH, path);
        params.put(DmConstants.KEY_FILE_NAME, fileName);
        params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
        params.put(DmConstants.KEY_CREATE_FOLDERS, createFolders);
        params.put(DmConstants.KEY_EDIT, edit);
        params.put(DmConstants.KEY_UNLOCK, unlock);
        String id = site + ":" + path + ":" + fileName + ":" + contentType;
        String fullPath = expandRelativeSitePath(site, path);
        ContentItemTO item = getContentItem(site, path);
        String lockKey = id;
        if (item != null) {
            lockKey = site + ":" + item.getUri();
        }
        generalLockService.lock(lockKey);
        try {
            boolean savaAndClose = (!StringUtils.isEmpty(unlock) && unlock.equalsIgnoreCase("false")) ? false : true;
            if (item != null) {
                ObjectState objectState = objectStateService.getObjectState(site, path);
                if (objectState == null) {
                    objectStateService.insertNewEntry(site, item);
                    objectState = objectStateService.getObjectState(site, path);
                }
                if (objectState.getSystemProcessing() != 0){
                    logger.error(String.format("Error Content %s is being processed (Object State is system processing);", fileName));
                    throw new RuntimeException(String.format("Content \"%s\" is being processed", fileName));
                }

                objectStateService.setSystemProcessing(site, path, true);
            }

            // default chain is asset type
            String chainID = DmConstants.CONTENT_CHAIN_ASSET;

            if(path.startsWith("/site")) {
                // anything inside site is a form based XML
                // example /site/website 
                //         /site/components  
                //         /site/books
                chainID = DmConstants.CONTENT_CHAIN_FORM;
            }

            processContent(id, input, true, params, chainID);
            objectStateService.setSystemProcessing(site, path, false);
            String savedFileName = params.get(DmConstants.KEY_FILE_NAME);
            String savedPath = params.get(DmConstants.KEY_PATH);
            fullPath = expandRelativeSitePath(site, savedPath);
            if (!savedPath.endsWith(savedFileName)) {
                fullPath = fullPath + "/" + savedFileName;
            }
            fullPath = fullPath.replace("//", "/");
            String relativePath = getRelativeSitePath(site, fullPath);
            ContentItemTO itemTo = getContentItem(site, relativePath);
            if (itemTo != null) {
                if (savaAndClose) {
                    objectStateService.transition(site, itemTo, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE);
                } else {
                    objectStateService.transition(site, itemTo, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE_FOR_PREVIEW);
                }
            } else {
                objectStateService.insertNewEntry(site, itemTo);
            }
            objectStateService.setSystemProcessing(site, relativePath, false);
        }  catch (RuntimeException e) {
            logger.error("error writing content",e);
            throw e;
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    public void writeContentAndRename(final String site, final String path, final String targetPath, final String fileName, final String contentType, final InputStream input,
                                      final String createFolders, final  String edit, final String unlock, final boolean createFolder) throws ServiceException {
        String id = site + ":" + path + ":" + fileName + ":" + contentType;
        if (!generalLockService.tryLock(id)) {
            generalLockService.lock(id);
            generalLockService.unlock(id);
            return;
        }
        try {
            writeContent(site, path, fileName, contentType, input, createFolders, edit, unlock);
            rename(site, path, targetPath, createFolder);
        } catch (Throwable t) {
            logger.error("Error while executing write and rename: ", t);
        } finally {
            generalLockService.unlock(id);
        }
    }

    @Override
    public boolean writeContent(String site, String path, InputStream content){
        return writeContent(expandRelativeSitePath(site, path), content);
    }

    @Override
    public boolean createFolder(String site, String path, String name) {
        return _contentRepository.createFolder(expandRelativeSitePath(site, path), name);
    }

    @Override
    public boolean deleteContent(String site, String path) {
        return _contentRepository.deleteContent(expandRelativeSitePath(site, path));
    }

    @Override
    public boolean copyContent(String site, String fromPath, String toPath) {
        return _contentRepository.copyContent(expandRelativeSitePath(site, fromPath),
                expandRelativeSitePath(site, toPath));
    }

    @Override
    public boolean moveContent(String site, String fromPath, String toPath) {
        return _contentRepository.moveContent(expandRelativeSitePath(site, fromPath),
                expandRelativeSitePath(site, toPath));
    }

    protected ContentItemTO createNewContentItemTO(String site, String contentPath) {
        ContentItemTO item = new ContentItemTO();

        item.asset = true;
        item.site = site;
        item.internalName = item.name;
        item.contentType = "asset";
        item.disabled = false;
        item.floating = false;
        item.hideInAuthoring = false;

        item.uri = contentPath;
        item.path = contentPath.substring(0, contentPath.lastIndexOf("/"));
        item.name = contentPath.substring(contentPath.lastIndexOf("/") + 1);
        
        item.page = false;
        item.previewable = false;
        item.component = false;
        item.document = false;
        item.asset = true;
        item.browserUri = "";

        // populate with workflow states and other metadata
        item.isNew = true;
        item.submitted = false;
        item.scheduled = false;
        item.deleted = false;
        item.submittedForDeletion = false;
        item.inProgress = true;
        item.live = false;
        item.folder = (item.name.contains(".")==false);

        return item;
    }

    protected ContentItemTO populateContentDrivenProperties(ContentItemTO item)
    throws Exception {
        
        String fullContentPath = expandRelativeSitePath(item.site, item.uri);
        String contentPath = item.uri;

        logger.info("Pupulating page props {0}", contentPath);
        boolean itemIsPage = false;

        if((contentPath.startsWith("/site/website"))
        && contentPath.indexOf(".level.xml") == -1) {
            itemIsPage = true;
        }
         
        item.page = itemIsPage;
        item.previewable = itemIsPage;
        item.component = !itemIsPage;
        item.asset = false;

        item.uri = contentPath;
        item.path = contentPath.substring(0, contentPath.lastIndexOf("/"));
        item.name = contentPath.substring(contentPath.lastIndexOf("/")+1);
        item.browserUri = (item.page) ? contentPath.replace("/site/website", "").replace("/index.xml", "") : null;

        Document contentDoc = this.getContentAsDocument(fullContentPath);
        if(contentDoc != null) {
            Element rootElement = contentDoc.getRootElement();
            item.internalName = rootElement.valueOf("internal-name");
            item.contentType = rootElement.valueOf("content-type");
            item.disabled = ( (rootElement.valueOf("disabled") != null) && rootElement.valueOf("disabled").equals("true") );
            item.floating = ( (rootElement.valueOf("placeInNav") != null) && rootElement.valueOf("placeInNav").equals("true") );
            item.hideInAuthoring = ( (rootElement.valueOf("hideInAuthoring") != null) && rootElement.valueOf("hideInAuthoring").equals("true") );
        }
        else {
             logger.error("no xml document could be loaded for path {0}", fullContentPath);
        }

        return item;
    }

    protected ContentItemTO populateItemChildren(ContentItemTO item, int depth) {
        String fullContentPath = expandRelativeSitePath(item.site, item.uri);
        String contentPath = item.uri;

        item.children = new ArrayList<ContentItemTO>();
        item.numOfChildren = 0;

        if (contentPath.indexOf("/index.xml") != -1 || contentPath.indexOf(".xml") == -1 ) { // item.isFolder?

            if (contentPath.indexOf("/index.xml") != -1) {
                fullContentPath = fullContentPath.replace("/index.xml", "");
            }


            RepositoryItem[] childRepoItems = _contentRepository.getContentChildren(fullContentPath);
            boolean indexFound = false;
                
            if(childRepoItems != null) {
                item.numOfChildren = childRepoItems.length;
                if (item.numOfChildren != 0) {
                    item.isContainer = true;
                    item.container = true;
                }

                logger.info("Checking if {0} has index", contentPath);
                for (int j = 0; j < childRepoItems.length; j++) {
                    if ("index.xml".equals(childRepoItems[j].name)) {
                        if (item.uri.indexOf("/index.xml") == -1) {
                            item.uri = item.uri + "/index.xml";
                        }
                        item.numOfChildren--;
                        indexFound = true;
                    }
                    else {
                        String childPath = getRelativeSitePath(item.site, childRepoItems[j].path+"/"+childRepoItems[j].name);
                        item.children.add(getContentItem(item.site, childPath, depth-1));
                    }
                }

                if(indexFound == false) {
                    // ITEM IS A FOLDER
                    item.folder = true;
                    item.isContainer = true;
                    item.container = true; 

                    item.page = false;
                    item.asset = false;
                    item.component = false;
                    item.previewable = false;
        
                    item.internalName = item.name;
                    item.contentType = "folder";
                    item.path = item.uri;
                }

                // ORDER THE CHILDREN
                    // level descriptors first
                    // nav pages by order
                    // floating pages via Alpha

            } else {
                // ITEM HAS NO CHILDREN
                item.isContainer = true;
                item.container = true;
            }
        }
        else {
            // ITEM IS A STAND-ALONE XML
            item.isContainer = false;
            item.container = false;   
        }

        return item;
    }

    @Override
    public ContentItemTO getContentItem(String site, String path) {
        return getContentItem(site, path, 2);
    }

    @Override
    public ContentItemTO getContentItem(String site, String path, int depth) {
        ContentItemTO item = null;
        String fullContentPath = expandRelativeSitePath(site, path);
        String contentPath = path;
        logger.info("Getting content item for {0}", contentPath);

        DebugUtils.addDebugStack(logger);
        long startTime = System.currentTimeMillis();

        try {
            item = createNewContentItemTO(site, contentPath);

            if(depth!=0) {
                item = populateItemChildren(item, depth);
            }

            if(item.uri.endsWith(".xml")) {
                item = populateContentDrivenProperties(item);
            }

            loadContentTypeProperties(site, item, item.contentType);

            // POPULATE LOCK STATUS
            populateMetadata(site, item);

            // POPULATE WORKFLOW STATUS
            populateWorkflowProperties(site, item);
            //item.setLockOwner("");
        }
        catch(Exception err) {
            logger.error("error constructing item for object at path '{0}'", err, fullContentPath);
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        logger.debug("Content item [{0}] retrieved in {1} milis", fullContentPath, executionTime);
        return item;
    }

    @Override
    public ContentItemTO getContentItem(String fullPath) {
        String site = getSiteFromFullPath(fullPath);
        String relativePath = getRelativeSitePath(site, fullPath);

        return getContentItem(site, fullPath);
    }

    protected void loadContentTypeProperties(String site, ContentItemTO item, String contentType) {
        ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, contentType);
        if (config != null) {
            item.setForm(config.getForm());
            item.setFormPagePath(config.getFormPath());
            item.setPreviewable(config.isPreviewable());
        }
        // TODO CodeRev:but what if the config is null?
    }

    protected void populateWorkflowProperties(String site, ContentItemTO item) {
        ObjectState state = objectStateService.getObjectState(site, item.getUri());
        if (state != null) {
            item.setLive(org.craftercms.studio.api.v1.service.objectstate.State.isLive(org.craftercms.studio.api.v1.service.objectstate.State.valueOf(state.getState())));
            item.isLive = item.isLive();
            item.setInProgress(!item.isLive());
            item.isInProgress = item.isInProgress();
            item.setScheduled(org.craftercms.studio.api.v1.service.objectstate.State.isScheduled(org.craftercms.studio.api.v1.service.objectstate.State.valueOf(state.getState())));
            item.isScheduled = item.isScheduled();
            item.setSubmitted(org.craftercms.studio.api.v1.service.objectstate.State.isSubmitted(org.craftercms.studio.api.v1.service.objectstate.State.valueOf(state.getState())));
            item.isSubmitted = item.isSubmitted();
        }
    }

    protected void populateMetadata(String site, ContentItemTO item) {
        ObjectMetadata metadata = objectMetadataManager.getProperties(site, item.getUri());
        if (metadata != null) {
            if (StringUtils.isEmpty(metadata.getLockOwner())) {
                item.setLockOwner("");
            } else {
                item.setLockOwner(metadata.getLockOwner());
            }
        } else {
            item.setLockOwner("");
        }
    }

    @Override
    public ContentItemTO getContentItemTree(String site, String path, int depth) {
        logger.debug("Getting content item  tree for {0}:{1} depth {2}", site, path, depth);
        DebugUtils.addDebugStack(logger);
        long startTime = System.currentTimeMillis();
        boolean isPages = (path.contains("/site/website"));
        ContentItemTO root = null;

        if(isPages && path.equals("/site/website")) {
            root = getContentItem(site, path+"/index.xml");
        }
        else {
            root = getContentItem(site, path);
        }

        // root.children = getContentItemTreeInternal(site, path, depth, isPages);
        // root.numOfChildren = root.children.size();
        // if(root.numOfChildren != 0)  root.isContainer = true;

        long executionTime = System.currentTimeMillis() - startTime;
        logger.debug("Content item tree [{0}:{1} depth {2}] retrieved in {3} milis", site, path, depth, executionTime);

        return root;
    }

    @Override
    public ContentItemTO getContentItemTree(String fullPath, int depth) {
        String site = getSiteFromFullPath(fullPath);
        String relativePath = getRelativeSitePath(site, fullPath);

        return getContentItemTree(site, relativePath, depth);
    }

    @Override
    public VersionTO[] getContentItemVersionHistory(String site, String path) {
        return _contentRepository.getContentVersionHistory(expandRelativeSitePath(site, path));
    }

    @Override
    public boolean revertContentItem(String site, String path, String version, boolean major, String comment) {
        boolean success = false;

        success = _contentRepository.revertContent(expandRelativeSitePath(site, path), version, major, comment);

        if(success) {
            // publish item udated event or push to preview
        }

        return success;
    }

    public ContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath){
        String absolutePath = expandRelativeSitePath(site, relativePath);
        DmPathTO path = new DmPathTO(absolutePath);
        ContentItemTO item = new ContentItemTO();
        String timeZone = servicesConfig.getDefaultTimezone(site);
        item.timezone = timeZone;
        String name = path.getName();
        //String relativePath = path.getRelativePath();
        String fullPath = path.toString();
        String folderPath = (name.equals(DmConstants.INDEX_FILE)) ? relativePath.replace("/" + name, "") : relativePath;
        item.path = folderPath;
        /**
         * Internal name should be just folder name
         */
        String internalName = folderPath;
        int index = folderPath.lastIndexOf('/');
        if (index != -1)
            internalName = folderPath.substring(index + 1);

        item.internalName = internalName;
        //item.title = internalName;
        item.isDisabled = false;
        item.isNavigation = false;
        item.name = name;
        item.uri = relativePath;

        //item.defaultWebApp = path.getDmSitePath();
        //set content type based on the relative Path
        String contentTypeClass = getContentTypeClass(site, relativePath);
        item.contentType = contentTypeClass;
        if (contentTypeClass.equals(DmConstants.CONTENT_TYPE_COMPONENT)) {
            item.component = true;
        } else if (contentTypeClass.equals(DmConstants.CONTENT_TYPE_DOCUMENT)) {
            item.document = true;
        }
        // set if the content is new
        item.isDeleted = true;
        item.isContainer = false;
        item.container = false;
        //item.isNewFile = false;
        item.isNew = false;
        item.isInProgress = false;
        item.timezone = servicesConfig.getDefaultTimezone(site);
        item.isPreviewable = false;
        item.browserUri = getBrowserUri(item);

        return item;
    }

    /**
     * get the tree of content items (metadata) beginning at a root
     *
     * @param site
     * @param path
     * @param depth
     * @param isPages
     * @return return an array of child nodes
     */
    protected List<ContentItemTO> getContentItemTreeInternal(String site, String path, int depth, boolean isPages) {

        List<ContentItemTO> children = new ArrayList<ContentItemTO>();

        RepositoryItem[] childRepoItems = _contentRepository.getContentChildren(expandRelativeSitePath(site, path));
        if (childRepoItems != null) {
            for (int i = 0; i < childRepoItems.length; i++) {
                RepositoryItem repoItem = childRepoItems[i];
                String relativePath = getRelativeSitePath(site, (repoItem.path + "/" + repoItem.name));

                ContentItemTO contentItem = null;
                if (repoItem.isFolder && isPages) {

                    contentItem = getContentItem(site, relativePath);  
                    // if (contentItem != null && depth > 0) {
                    //     contentItem.children = getContentItemTreeInternal(site, relativePath, depth - 1, isPages);
                    //     contentItem.numOfChildren = contentItem.children.size();
                    //     if(contentItem.numOfChildren != 0) contentItem.isContainer = true;
                    // }
                }

                if (contentItem == null) {
                    if (!StringUtils.endsWith(relativePath, "/index.xml")) {
                        contentItem = getContentItem(site, relativePath);
                        // if (depth > 0) {
                        //     contentItem.children = getContentItemTreeInternal(site, relativePath, depth - 1, isPages);
                        //     contentItem.numOfChildren = contentItem.children.size();
                        //     if(contentItem.numOfChildren != 0) contentItem.isContainer = true;
                        // }
                    }
                }

                if (contentItem != null) {
                    children.add(contentItem);
                }
            }
        }

        return children;
    }

    /**
     * get the tree of content items (metadata) beginning at a root
     *
     * @param site
     * @param path
     * @param depth
     * @param isPages
     * @return return an array of child nodes
     */
    protected List<ContentItemTO> getContentItemTreeInternal(String fullPath, int depth, boolean isPages) {
        String site = getSiteFromFullPath(fullPath);
        String relativePath = getRelativeSitePath(site, fullPath);
        return getContentItemTreeInternal(site, relativePath, depth, isPages);
    }

    /**
     * take a path like /sites/website/index.xml and root it properly with a fully expanded repo path
     *
     * @param site
     * @param relativePath
     * @return
     */
    @Override
    public String expandRelativeSitePath(String site, String relativePath) {
        return "/wem-projects/" + site + "/" + site + "/work-area" + relativePath;
    }

    /**
     * take a path like /wem-projects/SITE/SITE/work-area/sites/website/index.xml and
     * and return the releative path to the project
     *
     * @param site
     * @param fullPath
     * @return
     */
    @Override
    public String getRelativeSitePath(String site, String fullPath) {
        return fullPath.replace("/wem-projects/" + site + "/" + site + "/work-area", "");
    }

    protected String getRelativeSitePath(String fullPath) {
        String path = fullPath.replace("/wem-projects/", "");
        String site = path.substring(0, path.indexOf("/"));
        return getRelativeSitePath(site, fullPath);
    }

    protected String getSiteFromFullPath(String fullPath) {
        String path = fullPath.replace("/wem-projects/", "");
        String site = path.substring(0, path.indexOf("/"));
        return site;
    }

    protected String getBrowserUri(ContentItemTO item) {
        String replacePattern = "";
        //if (item.isLevelDescriptor) {
        //    replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        //} else if (item.isComponent()) {
        if (item.isComponent) {
            replacePattern = DmConstants.ROOT_PATTERN_COMPONENTS;
        } else if (item.isAsset) {
            replacePattern = DmConstants.ROOT_PATTERN_ASSETS;
        } else if (item.isDocument) {
            replacePattern = DmConstants.ROOT_PATTERN_DOCUMENTS;
        } else {
            replacePattern = DmConstants.ROOT_PATTERN_PAGES;
        }
        boolean isPage = !(item.isComponent || item.isAsset || item.isDocument);
        return getBrowserUri(item.uri, replacePattern, isPage);
    }

    protected static String getBrowserUri(String uri, String replacePattern, boolean isPage) {
        String browserUri = uri.replaceFirst(replacePattern, "");
        browserUri = browserUri.replaceFirst("/" + DmConstants.INDEX_FILE, "");
        if (browserUri.length() == 0) {
            browserUri = "/";
        }
        // TODO: come up with a better way of doing this.
        if (isPage) {
            browserUri = browserUri.replaceFirst("\\.xml", ".html");
        }
        return browserUri;
    }

    @Override
    public String getContentTypeClass(String site, String uri) {
        if (matchesPatterns(uri, servicesConfig.getComponentPatterns(site)) || uri.endsWith("/" + servicesConfig.getLevelDescriptorName(site))) {
            return DmConstants.CONTENT_TYPE_COMPONENT;
        } else if (matchesPatterns(uri, servicesConfig.getDocumentPatterns(site))) {
            return DmConstants.CONTENT_TYPE_DOCUMENT;
        } else if (matchesPatterns(uri, servicesConfig.getAssetPatterns(site))) {
            return DmConstants.CONTENT_TYPE_ASSET;

        } else if (matchesPatterns(uri, servicesConfig.getRenderingTemplatePatterns(site))) {
            return DmConstants.CONTENT_TYPE_RENDERING_TEMPLATE;
        }
        return DmConstants.CONTENT_TYPE_PAGE;
    }

    protected boolean matchesPatterns(String uri, List<String> patterns) {
        if (patterns != null) {
            for (String pattern : patterns) {
                if (uri.matches(pattern)) {
                    return true;
                }
            }
        }
        return false;
    }


    protected void rename(final String site, final String path, final String targetPath,final boolean createFolder) throws ServiceException {
        dmRenameService.rename(site, path,targetPath,createFolder);
    }

    @Override
    public void processContent(String id, InputStream input, boolean isXml, Map<String, String> params, String contentChainForm) throws ServiceException {
        // get sandbox if not provided
        long start = System.currentTimeMillis();
        try {

            String[] strings = id.split(":");
            final String path = strings[1];
            final String site = strings[0];
            ResultTO to = doSave(id, input, isXml, params, contentChainForm, path, site);

        } finally {
            long end = System.currentTimeMillis();
            logger.debug("Write complete for [" + id + "] in time [" + (end - start) + "]");
        }
    }

    protected ResultTO doSave(String id, InputStream input, boolean isXml, Map<String, String> params, String chainName, final String path, final String site) throws ServiceException {
        String fullPath = expandRelativeSitePath(site, path);
        if (fullPath.endsWith("/")) fullPath = fullPath.substring(0, fullPath.length() - 1);
        ResultTO to = contentProcessor.processContent(id, input, isXml, params, chainName);
        //GoLiveQueue queue = (GoLiveQueue) _cache.get(Scope.DM_SUBMITTED_ITEMS, CStudioConstants.DM_GO_LIVE_CACHE_KEY, site);
        //if (queue != null) {
        //    queue.remove(path);
        //}
        return to;
    }

    @Override
    public String getNextAvailableName(String site, String path) {
        String[] levels = path.split("/");
        int length = levels.length;
        if (length > 0) {
            ContentItemTO item = getContentItem(site, path);
            if (item != null) {
                String name = item.getName();
                String parentPath = ContentUtils.getParentUrl(path);
                ContentItemTO parentItem = getContentItemTree(site, parentPath, 1);
                if (parentItem != null) {
                    int lastIndex = name.lastIndexOf(".");
                    String ext = (item.isFolder()) ? "" : name.substring(lastIndex);
                    String originalName = (item.isFolder()) ? name : name.substring(0, lastIndex);
                    List<ContentItemTO> children = parentItem.getChildren();
                    // pattern matching doesn't work here
                    // String childNamePattern = originalName + "%" + ext;
                    int lastNumber = 0;
                    String namePattern = originalName + "\\-[0-9]+" + ext;
                    if (children != null && children.size() > 0) {
                        // since it is already sorted, we only care about the last matching item
                        for (ContentItemTO child : children) {
                            if ((item.isFolder() == child.isFolder())) {
                                String childName = child.getName();
                                if (childName.matches(namePattern)) {
                                    Pattern pattern = (item.isFolder()) ? COPY_FOLDER_PATTERN : COPY_FILE_PATTERN;
                                    Matcher matcher = pattern.matcher(childName);
                                    if (matcher.matches()) {
                                        int helper = ContentFormatUtils.getIntValue(matcher.group(2));
                                        lastNumber = (helper > lastNumber) ? helper : lastNumber;
                                    }
                                }
                            }
                        }
                    }
                    String nextName = originalName + "-" + ++lastNumber + ext;
                    return nextName;
                } else {
                    // if parent doesn't exist, it is new item so the current name is available one
                }
            }
        } else {
            // cannot generate a name
            return "";
        }
        // if not found the current name is available
        return levels[length - 1];
    }

    @Override
    public GoLiveDeleteCandidates getDeleteCandidates(String site, String relativePath) throws ServiceException {
        List<String> items = new ArrayList<>();
        ContentItemTO contentItem = getContentItem(site, relativePath);
        GoLiveDeleteCandidates deletedItems = new GoLiveDeleteCandidates(site, this);
        if (contentItem != null) {
            childDeleteItems(site, contentItem, deletedItems);
            //update summary for all uri's delete
        }
        //AuthenticationUtil.setFullyAuthenticatedUser(user);
        return deletedItems;
    }

    /**
     * Iterate over all paths inside the folder
     */
    protected void childDeleteItems(String site, ContentItemTO contentItem, GoLiveDeleteCandidates items) throws ServiceException {

        if (contentItem.isFolder()) {
            contentItem = getContentItemTree(site, contentItem.getUri(), 1);
            if (contentItem.getChildren() != null && contentItem.getNumOfChildren() > 0) {
                for (ContentItemTO child : contentItem.getChildren()) {
                    childDeleteItems(site, child, items);
                }
            }
        }
        //add the child path
        items.getPaths().add(contentItem.getUri());
    }

    @Override
    public void lockContent(String site, String path) {
        _contentRepository.lockItem(site, path);
        objectMetadataManager.lockContent(site, path, securityService.getCurrentUser());
    }

    @Override
    public void unLockContent(String site, String path) {
        _contentRepository.unLockItem(site, path);
        objectMetadataManager.unLockContent(site, path);
    }

    private ContentRepository _contentRepository;
    protected ServicesConfig servicesConfig;
    protected GeneralLockService generalLockService;
    protected org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService;
    protected DmDependencyService dependencyService;
    protected ProcessContentExecutor contentProcessor;
    protected DmRenameService dmRenameService;
    protected ObjectMetadataManager objectMetadataManager;
    protected SecurityService securityService;

    public ContentRepository getContentRepository() { return _contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this._contentRepository = contentRepository; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public org.craftercms.studio.api.v1.service.objectstate.ObjectStateService getObjectStateService() {
        return objectStateService;
    }

    public void setObjectStateService(org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService) {
        this.objectStateService = objectStateService;
    }

    public DmDependencyService getDependencyService() { return dependencyService; }
    public void setDependencyService(DmDependencyService dependencyService) { this.dependencyService = dependencyService; }

    public ProcessContentExecutor getContentProcessor() { return contentProcessor; }
    public void setContentProcessor(ProcessContentExecutor contentProcessor) { this.contentProcessor = contentProcessor; }

    public DmRenameService getDmRenameService() { return dmRenameService; }
    public void setDmRenameService(DmRenameService dmRenameService) { this.dmRenameService = dmRenameService; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }
}
