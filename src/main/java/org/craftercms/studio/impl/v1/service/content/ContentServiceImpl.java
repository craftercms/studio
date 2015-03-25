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
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.to.*;
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
       return _contentRepository.writeContent(path, content);
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

    @Override
    public ContentItemTO getContentItem(String site, String path) {
        ContentItemTO item = getContentItem(expandRelativeSitePath(site, path));
        if (item != null) {
            item.uri = path;
            item.path = path.substring(0, path.lastIndexOf("/"));
            item.name = path.substring(path.lastIndexOf("/") + 1);
            item.page = (item.contentType.indexOf("/page") != -1);
            item.isContainer = path.endsWith("/index.xml");
            item.previewable = item.page;
            item.component = (item.contentType.indexOf("/component") != -1);
            item.document = false;
            item.asset = (item.component == false && item.page == false);
            item.browserUri = (item.page) ? path.replace("/site/website", "").replace("/index.xml", "") : null;

            boolean studioContentType = (item.contentType.indexOf("/component") != -1) || (item.contentType.indexOf("/page") != -1);
            if (studioContentType) {
                loadContentTypeProperties(site, item, item.contentType);
            }
            loadWorkflowProperties(site, item);
        }
        return item;
    }
    @Override
    public ContentItemTO getContentItem(String fullPath) {
        ContentItemTO item = null;

        try {
            // this may be faster to get evenything from on of the other services?
            // the idea heare is that repo does not know enough to get an item,
            // this requires either a different servivice/subsystem or a combination of them
            if(fullPath.endsWith(".xml")) {
                Document contentDoc = this.getContentAsDocument(fullPath);
                if(contentDoc != null) {
                    item = new ContentItemTO();

                    Element rootElement = contentDoc.getRootElement();
                    item.internalName = rootElement.valueOf("internal-name");
                    item.contentType = rootElement.valueOf("content-type");
                    item.disabled = ( (rootElement.valueOf("disabled") != null) && rootElement.valueOf("disabled").equals("true") );
                    item.floating = ( (rootElement.valueOf("placeInNav") != null) && rootElement.valueOf("placeInNav").equals("true") );
                    item.hideInAuthoring = ( (rootElement.valueOf("hideInAuthoring") != null) && rootElement.valueOf("hideInAuthoring").equals("true") );

                    item.uri = fullPath;
                    item.path = fullPath.substring(0, fullPath.lastIndexOf("/"));
                    item.name = fullPath.substring(fullPath.lastIndexOf("/")+1);
                    item.page = (item.contentType.indexOf("/page") != -1);
                    item.isContainer = fullPath.endsWith("/index.xml");
                    item.previewable = item.page;
                    item.component = (item.contentType.indexOf("/component") != -1);
                    item.document = false;
                    item.asset = (item.component == false && item.page == false);
                    item.browserUri = (item.page) ? fullPath.replace("/site/website", "").replace("/index.xml", "") : null;

                    // populate with workflow states and other metadata

                    item.isNew = true;
                    item.submitted = false;
                    item.scheduled = false;
                    item.deleted = false;
                    item.submittedForDeletion = false;
                    item.inProgress = true;
                    item.live = false;

                    item.lockOwner = "";
                    item.user = "";
                    item.userFirstName = "";
                    item.userLastName = "";
                    item.nodeRef = "";
                    item.metaDescription = "";
                    item.folder = (item.name.contains(".")==false);


               }
                else {
                     logger.error("no xml document could be loaded for path {0}", fullPath);
                }
            }
            else {
                if (this.contentExists(fullPath)) {
                    item = new ContentItemTO();
                    item.uri = fullPath;
                    item.path = fullPath.substring(0, fullPath.lastIndexOf("/"));
                    item.name = fullPath.substring(fullPath.lastIndexOf("/")+1);
                    item.asset = true;
                    item.internalName = item.name;
                    item.contentType = "asset";
                    item.disabled = false;
                    item.floating = false;
                    item.hideInAuthoring = false;

                    item.uri = fullPath;
                    if (fullPath.contains("/") && fullPath.length() > 1) {
                        item.path = fullPath.substring(0, fullPath.lastIndexOf("/") - 1);
                        item.name = fullPath.substring(fullPath.lastIndexOf("/") + 1);
                    } else {
                        item.path = "";
                        item.name = fullPath;
                    }
                    item.page = false;
                    item.isContainer = (item.name.contains(".") == false);
                    item.previewable = false;
                    item.component = false;
                    item.document = false;
                    item.asset = (item.isContainer == false);
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
                }
            }

            if(item != null) {
                item.lockOwner = "";
                item.user = "";
                item.userFirstName = "";
                item.userLastName = "";
                item.nodeRef = "";
                item.metaDescription = "";

                // duplicate properties
                item.isDisabled = item.disabled;
                item.isInProgress = item.inProgress;
                item.isLive = item.live;
                item.isSubmittedForDeletion = item.submittedForDeletion;
                item.isScheduled = item.scheduled;
                item.isNavigation = item.navigation;
                item.isDeleted = item.deleted;
                item.isSubmitted = item.submitted;
                item.isFloating = item.floating;
                item.isPage = item.page;
                item.isPreviewable = item.previewable;
                item.isComponent = item.component;
                item.isDocument = item.document;
                item.isAsset = item.asset;
                item.folder = (item.name.contains(".")==false);
            }
        }
        catch(Exception err) {
            logger.error("error constructing item for object at path '{0}'", err, fullPath);
        }

        return item;
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

    protected void loadWorkflowProperties(String site, ContentItemTO item) {
        ObjectState state = objectStateService.getObjectState(site, item.getUri());
        if (state != null) {
            item.setLive(org.craftercms.studio.api.v1.service.objectstate.State.isLive(org.craftercms.studio.api.v1.service.objectstate.State.valueOf(state.getState())));
            item.isLive = item.isLive();
            item.setInProgress(!item.isLive());
        }
    }

    @Override
    public ContentItemTO getContentItemTree(String site, String path, int depth) {
        boolean isPages = (path.contains("/site/website"));
        ContentItemTO root = null;

        if(isPages && path.equals("/site/website")) {
            root = getContentItem(site, path+"/index.xml");
        }
        else {
            root = getContentItem(site, path);
        }

        root.children = getContentItemTreeInternal(site, path, depth, isPages);
        root.numOfChildren = root.children.size();

        return root;
    }

    @Override
    public ContentItemTO getContentItemTree(String fullPath, int depth) {
        ContentItemTO root = getContentItem(fullPath);

        root.children = getContentItemTreeInternal(fullPath, depth, false);
        root.numOfChildren = root.children.size();

        return root;
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
        String contentType = getContentType(site, relativePath);
        item.contentType = contentType;
        if (contentType.equals(DmConstants.CONTENT_TYPE_COMPONENT)) {
            item.component = true;
        } else if (contentType.equals(DmConstants.CONTENT_TYPE_DOCUMENT)) {
            item.document = true;
        }
        // set if the content is new
        item.isDeleted = true;
        item.isContainer = false;
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

        for(int i=0; i<childRepoItems.length; i++) {
            RepositoryItem repoItem = childRepoItems[i];
            String relativePath = getRelativeSitePath(site, (repoItem.path+ "/" + repoItem.name));

            ContentItemTO contentItem = null;
            
            if(repoItem.isFolder && isPages) {
                contentItem = getContentItem(site,  relativePath+"/index.xml");
                if (contentItem != null && depth > 0) {
                    contentItem.children = getContentItemTreeInternal(site, path, depth-1, isPages);
                    contentItem.numOfChildren = children.size();
                }
            }
            
            if(contentItem == null) {
                if (!StringUtils.endsWith(relativePath, "/index.xml")) {
                    contentItem = getContentItem(site, relativePath);
                    if (depth > 0) {
                        contentItem.children = getContentItemTreeInternal(site, path, depth - 1, isPages);
                        contentItem.numOfChildren = children.size();
                    }
                }
            }

            if(contentItem != null) {
                children.add(contentItem);
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

        List<ContentItemTO> children = new ArrayList<ContentItemTO>();

        RepositoryItem[] childRepoItems = _contentRepository.getContentChildren(fullPath);

        for(int i=0; i<childRepoItems.length; i++) {
            RepositoryItem repoItem = childRepoItems[i];
            //String relativePath = getRelativeSitePath(site, (repoItem.path+ "/" + repoItem.name));

            ContentItemTO contentItem = null;

            if(repoItem.isFolder && isPages) {
                contentItem = getContentItem(repoItem.path + "/" + repoItem.name + "/index.xml");
                if(depth > 0) {
                    contentItem.children = getContentItemTreeInternal(repoItem.path + "/" + repoItem.name, depth-1, isPages);
                    contentItem.numOfChildren = children.size();
                }
            }

            if(contentItem == null) {
                if (!StringUtils.endsWith(fullPath, "/index.xml")) {
                    contentItem = getContentItem(fullPath);
                    if (depth > 0) {
                        contentItem.children = getContentItemTreeInternal(fullPath, depth - 1, isPages);
                        contentItem.numOfChildren = children.size();
                    }
                }
            }
            if(contentItem == null) {
                children.add(contentItem);
            }
        }

        return children;
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
    public String getContentType(String site, String uri) {
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
            lockKey = item.getNodeRef();
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
            processContent(id, input, true, params, DmConstants.CONTENT_CHAIN_FORM);
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
        String fullPath = expandRelativeSitePath(site, path);
        String[] levels = path.split("/");
        int length = levels.length;
        if (length > 0) {
            ContentItemTO item = getContentItem(site, path);
            if (item != null) {
                String name = item.getName();
                //String parentPath = fullPath.replace("/" + name, "");
                String parentPath = ContentUtils.getParentUrl(path);
                ContentItemTO parentItem = getContentItemTree(site, parentPath, 1);
                if (parentItem != null) {
                    int lastIndex = name.lastIndexOf(".");
                    String ext = (/*item.isFolder()*/ false) ? "" : name.substring(lastIndex);
                    String originalName = (/*item.isFolder()*/ false) ? name : name.substring(0, lastIndex);
                    List<ContentItemTO> children = parentItem.getChildren();
                    // pattern matching doesn't work here
                    // String childNamePattern = originalName + "%" + ext;
                    int lastNumber = 0;
                    String namePattern = originalName + "\\-[0-9]+" + ext;
                    if (children != null && children.size() > 0) {
                        // since it is already sorted, we only care about the last matching item
                        for (ContentItemTO child : children) {
                            //if ((item.isFolder() == child.isFolder())) {
                                String childName = child.getName();
                                if (childName.matches(namePattern)) {
                                    Pattern pattern = (/*item.isFolder()*/false) ? COPY_FOLDER_PATTERN : COPY_FILE_PATTERN;
                                    Matcher matcher = pattern.matcher(childName);
                                    if (matcher.matches()) {
                                        int helper = ContentFormatUtils.getIntValue(matcher.group(2));
                                        lastNumber = (helper > lastNumber) ? helper : lastNumber;
                                    }
                                }
                            //}
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

    private ContentRepository _contentRepository;
    protected ServicesConfig servicesConfig;
    protected GeneralLockService generalLockService;
    protected org.craftercms.studio.api.v1.service.objectstate.ObjectStateService objectStateService;
    protected DmDependencyService dependencyService;
    protected ProcessContentExecutor contentProcessor;
    protected DmRenameService dmRenameService;

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
}
