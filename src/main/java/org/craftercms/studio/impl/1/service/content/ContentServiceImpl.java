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
package org.craftercms.cstudio.impl.service.content;

import java.net.*;
import java.io.*;
import java.io.InputStream;
import org.dom4j.io.SAXReader;
import java.lang.reflect.Method;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;

import org.apache.commons.io.IOUtils;

import org.craftercms.cstudio.api.log.*;
import org.craftercms.cstudio.api.service.content.*;
import org.craftercms.cstudio.api.to.ContentItemTO;
import org.craftercms.cstudio.api.repository.RepositoryItem;
import org.craftercms.cstudio.api.repository.ContentRepository;
import org.craftercms.cstudio.api.to.VersionTO;

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

    /**
     * get the tree of content items (metadata) beginning at a root
     *
     * @param site
     * @param path
     * @param depth
     * @param isPages
     * @return return an array of child nodes
     */
    protected ContentItemTO[] getContentItemTreeInternal(String site, String path, int depth, boolean isPages) {

        ContentItemTO[] children = new ContentItemTO[0];

        RepositoryItem[] childRepoItems = _contentRepository.getContentChildren(expandRelativeSitePath(site, path));

        children = new ContentItemTO[childRepoItems.length];
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

            children[i] = contentItem;
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
    protected String expandRelativeSitePath(String site, String relativePath) {
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
    protected String getRelativeSitePath(String site, String fullPath) {
        return fullPath.replace("/wem-projects/" + site + "/" + site + "/work-area", "");
    }


    private ContentRepository _contentRepository;
    public ContentRepository getContentRepository() { return _contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this._contentRepository = contentRepository; }
}
