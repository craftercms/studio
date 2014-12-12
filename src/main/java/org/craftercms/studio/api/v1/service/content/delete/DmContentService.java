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
package org.craftercms.studio.api.v1.service.content;

import org.craftercms.studio.api.v1.to.DmContentItemTO;
import org.craftercms.studio.api.v1.to.DmOrderTO;
import org.craftercms.studio.api.v1.to.GoLiveDeleteCandidates;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.to.ResultTO;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * Provides Content related services in WCM
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public interface DmContentService {


    /**
     * get content as an input stream
     *
     * @param site
     *            site to read the content from
     * @param path
     *            content path
     * @param edit
     *            getting content for edit? if true, lock the content
     * @param mergePrototype
     * @return InputStream
     * @throws org.alfresco.repo.security.permissions.AccessDeniedException
     * @throws ContentNotFoundException
     */
    //public InputStream getContent(String site, String path, boolean edit, boolean mergePrototype) throws AccessDeniedException, ContentNotFoundException;

    /**
     * TODO
     *
     * @param site
     * @param path
     * @return
     * @throws AccessDeniedException
     * @throws ContentNotFoundException
     */
    //public InputStream getContent(String site, String path) throws AccessDeniedException, ContentNotFoundException;

    /**
     * get content as an input stream from the draft/preview store
     *
     * @param site
     *            site to read the content from
     * @param path
     *            content path
     * @param edit
     *            getting content for edit? if true, lock the content
     * @param mergePrototype
     * @return InputStream
     * @throws AccessDeniedException
     * @throws ContentNotFoundException
     */
    //public InputStream getContentFromDraft(String site, String path, boolean edit, boolean mergePrototype) throws AccessDeniedException, ContentNotFoundException;


//    /**TODO
//     *
//     * @param site
//     * @param relativePath
//     * @return
//     * @throws AccessDeniedException
//     * @throws ContentNotFoundException
//     */
//    public Document getContentAsDocument(String site, String relativePath) throws AccessDeniedException, ContentNotFoundException;
//
    public DmContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath);

    /**
     * check whether the given content exists or not
     *
     * @param site
     * @param relativePath
     * @param originalPath
     *
     * @return true if the content exists at the given path
     */
    public boolean contentExists(final String site, final String relativePath,final String originalPath);

    /**
     * get content items from the given relativePath to the given depth
     *
     * @param site
     * @param sub
     * @param relativePath
     *            the path to start pages from
     * @param depth
     *            how many levels to walk down the tree
     * @param isPage
     * 			  are those content items pages?
     * @param orderName
     *            which order name to use to sort navigation pages
     *            for floating pages, it will sort them by their internal names alphabetically
     * @param checkChildren
     * 			 check each leaf has children or not. if so, # of children will be populate in numOfChildren property
     * @return root content item
     * @throws ContentNotFoundException
     */
    public DmContentItemTO getItems(final String site, final String sub, final String relativePath, final int depth,
                                     boolean isPage, final String orderName, final boolean checkChildren) throws ContentNotFoundException;

    public DmContentItemTO getItems(final String site, final String sub, final String relativePath, final int depth,
                                    boolean isPage, final String orderName, final boolean checkChildren, boolean populateDependencies) throws ContentNotFoundException;
    /**
     * get content items from the given relativePath to the given depth
     *
     * @param site
     * @param sub
     * @param relativePath
     *            the path to start pages from
     * @param depth
     *            how many levels to walk down the tree
     * @param isPage
     * 			  are those content items pages?
     * @param orderName
     *            which order name to use to sort navigation pages
     *            for floating pages, it will sort them by their internal names alphabetically
     * @param checkChildren
     * 			 check each leaf has children or not. if so, # of children will be populate in numOfChildren property
     * @return root content item
     * @throws ContentNotFoundException
     */
    public DmContentItemTO getItems(DmContentItemTO rootItem,final String site, final String sub, final String relativePath, final int depth,
                                     boolean isPage, final String orderName, final boolean checkChildren) throws ContentNotFoundException;

    public DmContentItemTO getItems(DmContentItemTO rootItem,final String site, final String sub, final String relativePath, final int depth,
                                    boolean isPage, final String orderName, final boolean checkChildren, boolean populateDependencies) throws ContentNotFoundException;
    /**
     * Return the order at the give long name path
     *
     * @param site
     *            ID of the site that the page belongs to
     * @param path
     *            the URI of the page. For a crafter URI this must be in the
     *            LONG form
     * @param sub
     *            the subordinate site key. This can be used for geo or any
     *            other type of subordination _EN _ASIA _DEPTX
     * @param orderName
     *            name of order
     * @return a list of orders
     * @throws ContentNotFoundException
     */
    public List<DmOrderTO> getOrders(String site, String path, String sub, String orderName,boolean includeFloating)
            throws ContentNotFoundException;

    /**
     * Return the order value given a re-order request if before and after are
     * not provided, the order will be ORDER_INCREMENT. if before is not
     * provided, the order will be (0 + after) / 2. if after is not provided,
     * the order will be before + ORDER_INCREMENT
     *
     * @param site
     *            ID of the site that the page belongs to
     * @param path
     *            the URI of the page. For a crafter URI this must be in the
     *            LONG form
     * @param sub
     *            the subordinate site key. This can be used for geo or any
     *            other type of subordination _EN _ASIA _DEPTX
     * @param before
     *            long name of the item (URI) to insert after. Not providing
     *            before will default the order of before to 0.
     * @param after
     *            long name of the item (URI) to insert ahead of. Not providing
     *            after will default the order of after to be the order of
     *            before * 2
     * @param orderName
     *            name of order
     * @return new order value
     * @throws ServiceException
     */
    public double reOrderContent(String site, String path, String sub, String before, String after,
                                 String orderName) throws ServiceException;

//    /**
//     * add wcm properties to the given content specified by the path
//     *
//     * @param site
//     * @param path
//     * @param author
//     * @param contentType
//     * @throws ContentNotFoundException
//     */
//    public void addDmProperties(String site, String path, String author, String contentType) throws ContentNotFoundException;
//
    /**
     * is the file new?
     *
     * @param site
     * @param path
     * @return true if new
     */
    public boolean isNew(String site, String path);

    /**
     * is the file new or updated?
     *
     * @param site
     * @param path
     * @return true if new or updated
     */
    public boolean isUpdatedOrNew(String site, String path);

//    /**
//     * delete content from a specified path
//     *
//     * @param site
//     * @param relativePath
//     * @param recursive
//     * @param generateActivity
//     * 			generate delete activity?
//     * @param approver
//     * 			the user who approved the deletion (optional) it will be set to the current user if not provided
//     * @return a list of paths deleted
//     * @throws ContentNotFoundException
//     */
//    public GoLiveDeleteCandidates deleteContent(String site, String relativePath,boolean recursive, boolean generateActivity, String approver) throws ServiceException;
//
//    /**
//     * delete multiple items at the list of paths given
//     *
//     * @param site
//     * @param paths
//     * @param generateActivity
//     * 			generate delete activity?
//     * @param approver
//     * 			the user who approved the deletion (optional) it will be set to the current user if not provided
//     * @return a list of paths deleted
//     * @throws ServiceException
//     */
//    public List<String> deleteContents(String site, List<String> paths, boolean generateActivity, String approver) throws ServiceException;
//
//    /**
//     * revert content in the given site
//     *
//     * @param site
//     * @param relativePath
//     * @throws ContentNotFoundException
//     *
//     * use revert method instead
//     */
//    @Deprecated
//    public void revertContent(String site, String relativePath);


    /**
     * process content by running the content through a chain of content processors
     *
     * @param id
     * @param input
     * @param isXml
     * @param params
     * @param chainName
     * @throws ServiceException
     */
    public ResultTO processContent(String id, InputStream input, boolean isXml, Map<String, String> params, String chainName) throws ServiceException;

    /**
     * get content full path (avm path) for the given content relative path
     * Null is same as cstudio admin
     * @param site
     * @param relativePath
     * @return content avm path
     */
    public String getContentFullPath(String site, String relativePath);
    
    public String getContentFullPath(String site, String relativePath, boolean includeRelativePath);

    /**
     * cancel editing on dm content by removing lock
     *
     * @param site
     * @param path
     */
    public void cancelEditing(String site, String path);

//    /**
//     * get deleted item information
//     *
//     * @param site
//     * @param sub
//     * @param path
//     * @return deleted item
//     */
//    public DmContentItemTO getDeletedItem(String site, String sub, String path);

    /**
     * get content as XML
     *
     * @param site
     * @param sub
     * @param path
     * @return XML document
     * @throws ContentNotFoundException
     * @throws AccessDeniedException
     */
    //public Document getContentXml(String site, String sub, String path) throws AccessDeniedException, ContentNotFoundException;

    //public Document getContentXmlByVersion(String site, String sub, String path, String version) throws AccessDeniedException, ContentNotFoundException;

    public String getContentType(String site, String uri);

    /**
     * get the next available of the given content name at the given path (used for paste/duplicate)
     *
     * @param site
     * @param sub
     * @param path
     * @return next available name that avoids a name conflict
     */
    public String getNextAvailableName(String site, String sub, String path);

    public String getBrowserUri(String site,String relativeUri,String name);



    /**
     * Certain items are not displayed in the dashboard widgets
     *
     * @param site
     * @param uri
     * @return
     */
    public boolean matchesDisplayPattern(String site, String uri);

    /**
     * Given a relative path, it retrieves property value
     *
     * @param site
     * @param relativePath
     * @param propertyName
     * @return String value of the property or empty
     */
    public String getTextPropertyByRelativePath(String site, String relativePath, String propertyName);

//    void cleanDraftDependencies(String path, String site) throws ServiceException;
//
//    GoLiveDeleteCandidates getDeleteCandidates(String site, String relativePath) throws ServiceException;

    /**
     * create a folder with the name given at the give path
     *
     * @param site
     * 			the target site
     * @param path
     * 			the target location
     * @param name
     * 			folder name to create
     * @return
     * @throws ServiceException
     */
    public void createFolder(String site, String path, String name) throws ServiceException;

	public GoLiveDeleteCandidates getDeleteCandidates(String site, String uri) throws ServiceException;

	//public Document getContentAsDocument(String site, String relativePath) throws AccessDeniedException, ContentNotFoundException;

	public List<String> deleteContents(String site, List<String> itemsToDelete,
			boolean generateActivity, String approver) throws ServiceException;

	GoLiveDeleteCandidates deleteContent(String site, String relativePath,
			boolean recursive, boolean generateActivity, String approver)
			throws ServiceException;

	boolean isPathFullPath(String path);

    void generateDeleteActivity(String site, List<String> paths, String approver);
}
