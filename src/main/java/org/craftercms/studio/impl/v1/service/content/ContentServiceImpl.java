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
import java.util.List;

import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmPathTO;
import org.craftercms.studio.api.v1.to.VersionTO;
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

    @Override
    public boolean contentExists(String site, String path) {
        return this._contentRepository.contentExists(expandRelativeSitePath(site, path));
    }

    @Override
    public InputStream getContent(String path) {
       return this._contentRepository.getContent(path);
    }

    @Override
    public InputStream getContent(String site, String path) {
       return this._contentRepository.getContent(expandRelativeSitePath(site, path));
    }

    @Override
    public String getContentAsString(String path)  {
        String content = null;

        try {
            content = IOUtils.toString(_contentRepository.getContent(path));
        }
        catch(Exception err) {
            logger.error("Failed to get content as string for path '{0}'", err, path);
        }

        return content;
    }

    @Override
    public Document getContentAsDocument(String path)
    throws DocumentException {
        Document retDocument = null;
        InputStream is = this.getContent(path);

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
        ContentItemTO item = null;

        try {
            // this may be faster to get evenything from on of the other services?
            // the idea heare is that repo does not know enough to get an item,
            // this requires either a different servivice/subsystem or a combination of them
            if(path.endsWith(".xml")) {
                Document contentDoc = this.getContentAsDocument(expandRelativeSitePath(site, path));
                if(contentDoc != null) {  
                    item = new ContentItemTO();

                    Element rootElement = contentDoc.getRootElement();
                    item.internalName = rootElement.valueOf("internal-name");
                    item.contentType = rootElement.valueOf("content-type"); 
                    item.disabled = ( (rootElement.valueOf("disabled") != null) && rootElement.valueOf("disabled").equals("true") ); 
                    item.floating = ( (rootElement.valueOf("placeInNav") != null) && rootElement.valueOf("placeInNav").equals("true") ); 
                    item.hideInAuthoring = ( (rootElement.valueOf("hideInAuthoring") != null) && rootElement.valueOf("hideInAuthoring").equals("true") ); 

                    item.uri = path;
                    item.path = path.substring(0, path.lastIndexOf("/"));
                    item.name = path.substring(path.lastIndexOf("/")+1);
                    item.page = (item.contentType.indexOf("/page") != -1);
                    item.isContainer = false;
                    item.previewable = item.page;
                    item.component = (item.contentType.indexOf("/component") != -1);
                    item.document = false;
                    item.asset = (item.component == false && item.page == false);
                    item.browserUri = (item.page) ? path.replace("/site/website", "").replace("/index.xml", "") : null;

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
               }
                else {
                     logger.error("no xml document could be loaded for path '{0}'", path);    
                }
            }
            else {
                if (this.contentExists(site, path)) {
                    item = new ContentItemTO();
                    item.uri = path;
                    item.path = path.substring(0, path.lastIndexOf("/"));
                    item.name = path.substring(path.lastIndexOf("/")+1);
                    item.asset = true;
                    item.internalName = item.name;
                    item.contentType = "asset";
                    item.disabled = false;
                    item.floating = false; 
                    item.hideInAuthoring = false;

                    item.uri = path;
                    item.path = path.substring(0, path.lastIndexOf("/")-1);
                    item.name = path.substring(path.lastIndexOf("/")+1);
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
            }
        }
        catch(Exception err) {
            logger.error("error constructing item for object at path '{0}'", err, path);            
        }

        return item;
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
                if(depth > 0) contentItem.children = getContentItemTreeInternal(site, path, depth-1, isPages);
            }
            
            if(contentItem == null) {
                contentItem = getContentItem(site, relativePath);
                if(depth > 0) contentItem.children = getContentItemTreeInternal(site, path, depth-1, isPages);
            }

            children.add(contentItem);
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

    protected String getContentType(String site, String uri) {
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

    private ContentRepository _contentRepository;
    protected ServicesConfig servicesConfig;

    public ContentRepository getContentRepository() { return _contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this._contentRepository = contentRepository; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }
}
