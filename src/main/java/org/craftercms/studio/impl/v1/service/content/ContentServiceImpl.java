/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2016 Crafter Software Corporation.
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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.dal.ItemState;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.executor.ProcessContentExecutor;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.*;
import org.craftercms.studio.api.v1.service.dependency.DmDependencyService;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.api.v1.util.DebugUtils;
import org.craftercms.studio.api.v1.util.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentItemOrderComparator;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;

import org.apache.commons.io.IOUtils;

import javax.activation.MimetypesFileTypeMap;

import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_TAXONOMY;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_TAXONOMY_REGEX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_UNKNOWN;
import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;

/**
 * Content Services that other services may use
 * @author russdanner
 * @author Sumer Jabri
 */
public class ContentServiceImpl implements ContentService {
    // TODO: SJ: Refactor in 2.7.x to leverage Crafter Core as this will automatically enable inheritance, caching and
    // TODO: SJ: make that feature available to end user.
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    /**
     * file and folder name patterns for copied files and folders
     */
    public final static Pattern COPY_FILE_PATTERN = Pattern.compile("(.+)-([0-9]+)\\.(.+)");
    public final static Pattern COPY_FOLDER_PATTERN = Pattern.compile("(.+)-([0-9]+)");

    @Override
    public boolean contentExists(String site, String path) {
        // TODO: SJ: Refactor in 2.7.x as this might already exists in Crafter Core (which is part of the new Studio)
        return this._contentRepository.contentExists(site, path);
    }

    @Override
    public InputStream getContent(String site, String path) throws ContentNotFoundException {
        // TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
        return this._contentRepository.getContent(site, path);
    }

    @Override
    public String getContentAsString(String site, String path)  {
        // TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
        String content = null;

        try {
            content = IOUtils.toString(_contentRepository.getContent(site, path));
        }
        catch(Exception err) {
            logger.debug("Failed to get content as string for path {0}", err, path);
        }

        return content;
    }

    @Override
    public Document getContentAsDocument(String site, String path)
            throws DocumentException {
        // TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
        Document retDocument = null;
        InputStream is = null;
        try {
            is = this.getContent(site, path);
        } catch (ContentNotFoundException e) {
            logger.debug("Content not found for path {0}", e, path);
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
                    logger.debug("Error closing stream for path {0}", err, path);
                }
            }
        }

        return retDocument;
    }

    @Override
    public void writeContent(String site, String path, String fileName, String contentType, InputStream input,
                             String createFolders, String edit, String unlock) throws ServiceException {
        // TODO: SJ: refactor for 2.7.x

        Map<String, String> params = new HashMap<String, String>();
        params.put(DmConstants.KEY_SITE, site);
        params.put(DmConstants.KEY_PATH, path);
        params.put(DmConstants.KEY_FILE_NAME, fileName);
        params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
        params.put(DmConstants.KEY_CREATE_FOLDERS, createFolders);
        params.put(DmConstants.KEY_EDIT, edit);
        params.put(DmConstants.KEY_UNLOCK, unlock);
        String id = site + ":" + path + ":" + fileName + ":" + contentType;
        String relativePath = path;
        boolean contentExists = contentExists(site, path);
        String lockKey = id;
        if (contentExists) {
            lockKey = site + ":" + path;
        }
        generalLockService.lock(lockKey);
        try {
            // Check if the user is saving and closing (releasing the lock) or just saving and will continue to edit
            // If "unlock" is empty, it means it's a save and close operation
            // if "unlock" is set to "false", it also means it's a save and continue operation
            boolean isSaveAndClose = (StringUtils.isNotEmpty(unlock) && !unlock.equalsIgnoreCase("false"));

            if (contentExists) {
                ItemState itemState = objectStateService.getObjectState(site, path);
                if (itemState == null) {
                    // This file is either new or someone created it outside of our system, we must create a state
                    // for it
                    ContentItemTO item = getContentItem(site, path, 0);
                    objectStateService.insertNewEntry(site, item);
                    itemState = objectStateService.getObjectState(site, path);
                }

                if (itemState != null) {

                    if (itemState.getSystemProcessing() != 0) {
                        // TODO: SJ: Review and refactor/redo
                        logger.error("Error Content {0} is being processed (Object State is system "
                                + "processing);", fileName);
                        throw new ServiceException("Content " + fileName + " is in system processing, we can't write "
                                + "it");
                    }

                    objectStateService.setSystemProcessing(site, path, true);
                }
                else {
                    logger.error("the object state is still null even after attempting to create it for site {0} "
                            + "path {1} fileName {2} contentType {3}"
                            + ".", site, path, fileName, contentType);
                }
            }

            // TODO: SJ: Item processing pipeline needs to be configurable without hardcoded paths
            // TODO: SJ: We need to consider various mechanics for pipeline choice other than path
            // TODO: SJ: Furthermore, we already have similar machinery in Crafter Core that might be a fit for some
            // TODO: SJ: of this work

            // default chain is asset type
            String chainID = DmConstants.CONTENT_CHAIN_ASSET;

            if(path.startsWith("/site")) {
                // anything inside site is a form based XML
                // example /site/website
                //         /site/components
                //         /site/books
                chainID = DmConstants.CONTENT_CHAIN_FORM;
            }

            // TODO: SJ: Content is being written here via the pipeline, this is not the best design and will be
            // TODO: SJ: refactored in 2.7.x
            processContent(id, input, true, params, chainID);

            // Item has been processed and persisted, set system processing state to off
            objectStateService.setSystemProcessing(site, path, false);

            // TODO: SJ: The path sent from the UI is inconsistent, hence the acrobatics below. Fix in 2.7.x
            String savedFileName = params.get(DmConstants.KEY_FILE_NAME);
            String savedPath = params.get(DmConstants.KEY_PATH);
            relativePath = savedPath;
            if (!savedPath.endsWith(savedFileName)) {
                relativePath = savedPath + "/" + savedFileName;
            }

            // TODO: SJ: Why is the item being loaded again? Why is the state being set to system not processing
            // TODO: SJ: again? Why would we insert the item into objectStateService again?
            // TODO: SJ: Refactor for 2.7.x
            ContentItemTO itemTo = getContentItem(site, relativePath, 0);
            if (itemTo != null) {
                if (isSaveAndClose) {
                    objectStateService.transition(site, itemTo, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE);
                } else {
                    objectStateService.transition(site, itemTo, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE_FOR_PREVIEW);
                }
                objectStateService.setSystemProcessing(site, itemTo.getUri(), false);
            } else {
                // TODO: SJ: the line below doesn't make any sense, itemTo == null => insert? Investigate and fix in
                // TODO: SJ: 2.7.x
                objectStateService.insertNewEntry(site, itemTo);
            }

            // Sync preview
            PreviewEventContext context = new PreviewEventContext();
            context.setSite(site);
            eventService.publish(EVENT_PREVIEW_SYNC, context);
        }  catch (RuntimeException e) {
            logger.error("error writing content", e);

            // TODO: SJ: Why setting two things? Are we guessing? Fix in 2.7.x
            objectStateService.setSystemProcessing(site, relativePath, false);
            objectStateService.setSystemProcessing(site, path, false);
            throw e;
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    public void writeContentAndRename(final String site, final String path, final String targetPath, final String fileName, final String contentType, final InputStream input,
                                      final String createFolders, final  String edit, final String unlock, final boolean createFolder) throws ServiceException {
        // TODO: SJ: The parameters need to be properly typed. Can't have Strings that actually mean boolean. Fix in
        // TODO: SJ: 2.7.x
        String id = site + ":" + path + ":" + fileName + ":" + contentType;

        // TODO: SJ: FIXME: Remove the log below after testing
        logger.debug("Write and rename for site '{}' path '{}' targetPath '{}' "
                + "fileName '{}' content type '{}'", site, path, targetPath, fileName, contentType);

        // TODO: SJ: The block below seems to be there to fix a stuck lock, refactor and remove in 2.7.x
        if (!generalLockService.tryLock(id)) {
            generalLockService.lock(id);
            generalLockService.unlock(id);
            return;
        }

        try {
            writeContent(site, path, fileName, contentType, input, createFolders, edit, unlock);
            moveContent(site, path, targetPath);
        } catch (ServiceException | RuntimeException e) {
            logger.error("Error while executing write and rename for site '{}' path '{}' targetPath '{}' "
                    + "fileName '{}' content type '{}'", e, site, path, targetPath, fileName, contentType);
        } finally {
            generalLockService.unlock(id);
        }
    }

    /**
     * write content asset
     *
     * @param site
     * @param path
     * @param assetName
     * @param in
     * @param isImage
     * 			is this asset an image?
     * @param allowedWidth
     * 			specifies the allowed image width in pixel if the asset is an image
     * @param allowedHeight
     * 			specifies the allowed image height in pixel if the asset is an image
     * @param unlock
     * 			unlock the content upon edit?
     * @return content asset info
     * @throws ServiceException
     */
    @Override
    public Map<String, Object> writeContentAsset(String site, String path, String assetName, InputStream in,
                                                 String isImage, String allowedWidth, String allowedHeight, String allowLessSize, String draft, String unlock, String systemAsset) throws ServiceException {
        boolean isSystemAsset = Boolean.valueOf(systemAsset);

        Map<String, String> params = new HashMap<String, String>();
        params.put(DmConstants.KEY_SITE, site);
        params.put(DmConstants.KEY_PATH, path);
        params.put(DmConstants.KEY_FILE_NAME, assetName);
        params.put(DmConstants.KEY_IS_IMAGE, isImage);
        params.put(DmConstants.KEY_ALLOW_LESS_SIZE, allowLessSize);
        params.put(DmConstants.KEY_ALLOWED_WIDTH, allowedWidth);
        params.put(DmConstants.KEY_ALLOWED_HEIGHT, allowedHeight);
        params.put(DmConstants.KEY_CONTENT_TYPE, "");
        params.put(DmConstants.KEY_CREATE_FOLDERS, "true");

        params.put(DmConstants.KEY_UNLOCK, unlock);
        params.put(DmConstants.KEY_SYSTEM_ASSET, String.valueOf(isSystemAsset));

        boolean exists = contentExists(site, path+ File.separator + assetName);
        params.put(DmConstants.KEY_ACTIVITY_TYPE, (exists ? ActivityService.ActivityType.UPDATED.toString() : ActivityService.ActivityType.CREATED.toString()));

        String id = site + ":" + path + ":" + assetName + ":" + "";
        // processContent will close the input stream
        ContentItemTO item = null;
        try {
            path = path + "/" + assetName;
            item = getContentItem(site, path);

            if (item != null) {
                ItemState itemState = objectStateService.getObjectState(site, path);
                if (itemState != null) {
                    if (itemState.getSystemProcessing() != 0) {
                        logger.error(String.format("Error Content %s is being processed (Object State is SYSTEM_PROCESSING);", assetName));
                        throw new RuntimeException(String.format("Content \"%s\" is being processed", assetName));
                    }
                    objectStateService.setSystemProcessing(site, path, true);
                }
            }
            ResultTO result = processContent(id, in, false, params, DmConstants.CONTENT_CHAIN_ASSET);
            ContentAssetInfoTO assetInfoTO = (ContentAssetInfoTO)result.getItem();
            if (isSystemAsset) {
                path = path.replace(assetName, assetInfoTO.getFileName());
            }
            item = getContentItem(site, path);
            item.setSize(assetInfoTO.getSize());
            item.setSizeUnit(assetInfoTO.getSizeUnit());
            if (item != null) {
                objectStateService.transition(site, item, TransitionEvent.SAVE);
            }

            PreviewEventContext context = new PreviewEventContext();
            context.setSite(site);
            eventService.publish(EVENT_PREVIEW_SYNC, context);

            Map<String, Object> toRet = new HashMap<String, Object>();
            toRet.put("success", true);
            toRet.put("message", item);
            return toRet;
        } catch (Exception e) {
            logger.error("Error processing content", e);
            Map<String, Object> toRet = new HashMap<String, Object>();
            toRet.put("success", true);
            toRet.put("message", e.getMessage());
            toRet.put("error", e);
            return toRet;
        } finally {
            if (item != null) {
                objectStateService.setSystemProcessing(site, path, false);
            }
        }
    }

    // This method is used for writing configuration files, this needs to be refactored in 3.1+
    @Override
    public boolean writeContent(String site, String path, InputStream content) throws ServiceException {
        boolean result;

        String commitId = _contentRepository.writeContent(site, path, content);

        result = StringUtils.isNotEmpty(commitId);

        if (result) {
            // Update database with commitId
            if (!objectMetadataManager.metadataExist(site, path)) {
                objectMetadataManager.insertNewObjectMetadata(site, path);
            }
            objectMetadataManager.updateCommitId(site, path, commitId);
            siteService.updateLastCommitId(site, commitId);
        }

        return result;
    }

    @Override
    public boolean createFolder(String site, String path, String name) {
        boolean toRet = false;
        String commitId = _contentRepository.createFolder(site, path, name);
        if (commitId != null) {
            // TODO: SJ: we're currently not keeping meta-data for folders and therefore nothing to update
            // TODO: SJ: rethink this for 3.1+
            toRet = true;
        }

        return toRet;
    }

    @Override
    public boolean deleteContent(String site, String path, String approver) {
        return deleteContent(site, path, true, approver);
    }

    @Override
    public boolean deleteContent(String site, String path, boolean generateActivity, String approver) {
        String commitId;
        boolean toReturn = false;
        if (generateActivity) {
            generateDeleteActivity(site, path, approver);
        }

        commitId = _contentRepository.deleteContent(site, path, approver);

        objectStateService.deleteObjectStateForPath(site, path);
        objectMetadataManager.deleteObjectMetadata(site, path);
        dependencyService.deleteDependenciesForSiteAndPath(site, path);

        PreviewEventContext context = new PreviewEventContext();
        context.setSite(site);
        eventService.publish(EVENT_PREVIEW_SYNC, context);


        // TODO: SJ: Add commitId to database for this item in version 2.7.x

        if (commitId != null) {
            toReturn = true;
        }

        return toReturn;
    }

    protected void generateDeleteActivity(String site, String path, String approver) {
        // This method creates a database record to show the activity of deleting a file
        // TODO: SJ: This type of thing needs to move to the audit service which handles all records related to
        // TODO: SJ: activities. Fix in 3.1+ by introducing the audit service and refactoring accordingly
        if (StringUtils.isEmpty(approver)) {
            approver = securityService.getCurrentUser();
        }
        boolean exists = contentExists(site, path);
        if (exists) {
            ItemMetadata properties = objectMetadataManager.getProperties(site, path);
            String user = (properties != null && !StringUtils.isEmpty(properties.getSubmittedBy()) ? properties
                .getSubmittedBy() : approver);
            Map<String, String> extraInfo = new HashMap<String, String>();
            if (path.endsWith(DmConstants.XML_PATTERN)) {
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, getContentTypeClass(site, path));
            }
            logger.debug("[DELETE] posting delete activity on " + path + " by " + user + " in " + site);

            activityService.postActivity(site, user, path, ActivityService.ActivityType.DELETED, ActivityService.ActivitySource.UI, extraInfo);
            // process content life cycle
            if (path.endsWith(DmConstants.XML_PATTERN)) {
                ContentItemTO item = getContentItem(site, path, 0);
                String contentType = item.getContentType();
                dmContentLifeCycleService.process(site, user, path,
                        contentType, DmContentLifeCycleService.ContentLifeCycleOperation.DELETE, null);
            }
        }
    }

    @Override
    public String copyContent(String site, String fromPath, String toPath) {
        return copyContent(site, fromPath, toPath, new HashSet<String>());
    }

    /**
     * internal method copy that handles
     * Get dependencies is already recursive
     */
    protected String copyContent(String site, String fromPath, String toPath, Set<String> processedPaths) {
        String retNewFileName = null;

        String lifecycleOp = DmContentLifeCycleService.ContentLifeCycleOperation.COPY.toString();
        String user = securityService.getCurrentUser();
        String copyPath = null;

        try {
            Map<String, String> copyPathMap = constructNewPathforCutCopy(site, fromPath, toPath, true);
            copyPath = copyPathMap.get("FILE_PATH");
            String copyPathModifier = copyPathMap.get("MODIFIER");
            String copyPathFileName = copyPathMap.get("FILE_NAME");
            String copyPathFolder = copyPathMap.get("FILE_FOLDER");

            String copyPathOnly = copyPath.substring(0, copyPath.lastIndexOf("/"));
            String copyFileName = copyPath.substring(copyPath.lastIndexOf("/")+1);

            if(!processedPaths.contains(copyPath)) {
                ContentItemTO fromItem = getContentItem(site, fromPath, 0);

                if (fromItem.isFolder()) {
                    createFolder(site, copyPathOnly, copyFileName);
                    // copy was successful, return the new name
                    retNewFileName = copyPath;
                } else {
                    InputStream copyContent = null;
                    try {
                        String contentType = fromItem.getContentType();
                        InputStream fromContent = getContent(site, fromPath);
                        Document fromDocument = ContentUtils.convertStreamToXml(fromContent);
                        Map<String, String> fromPageIds = getContentIds(fromDocument);

                        logger.debug("copying file for site {0} from {1} to {2}, new name is {3}", site, fromPath,
                                toPath, copyPath);

                        // come up with a new object ID and group ID for the object
                        Map<String, String> copyObjectIds = contentItemIdGenerator.getIds();

                        Map<String, String> copyDependencies = dependencyService.getCopyDependencies(site, fromPath,
                                fromPath);
                        copyDependencies = getItemSpecificDependencies(fromDocument, copyDependencies);
                        logger.debug("Calculated copy dependencies: {0}, {1}", fromPath, copyDependencies);

                        // Duplicate the children
                        for (String dependencyKey : copyDependencies.keySet()) {
                            String dependencyPath = copyDependencies.get(dependencyKey);
                            String copyDepPath = dependencyPath;

                            // try a simple substitution
                            copyDepPath = copyDepPath.replaceAll(
                                    fromPageIds.get(DmConstants.KEY_PAGE_ID),
                                    copyObjectIds.get(DmConstants.KEY_PAGE_ID));

                            copyDepPath = copyDepPath.replaceAll(
                                    fromPageIds.get(DmConstants.KEY_PAGE_GROUP_ID),
                                    copyObjectIds.get(DmConstants.KEY_PAGE_GROUP_ID));
                            logger.debug("Translated dependency path from {0} to {1}", dependencyPath, copyDepPath);

                            copyContent(site, dependencyPath, copyDepPath, processedPaths);
                        }

                        // update the file name / folder values
                        Document copyDocument = updateContentOnCopy(fromDocument, copyPathFileName, copyPathFolder,
                                copyObjectIds, copyPathModifier);

                        copyContent = ContentUtils.convertDocumentToStream(copyDocument, StudioConstants
                                .CONTENT_ENCODING);

                        // This code is very similar to what is in writeContent. Consolidate this code?
                        Map<String, String> params = new HashMap<String, String>();
                        params.put(DmConstants.KEY_SITE, site);
                        params.put(DmConstants.KEY_PATH, copyPathOnly);
                        params.put(DmConstants.KEY_FILE_NAME, copyFileName);
                        params.put(DmConstants.KEY_USER, user);
                        params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
                        params.put(DmConstants.KEY_CREATE_FOLDERS, "true");
                        params.put(DmConstants.KEY_EDIT, "true");
                        params.put(DmConstants.KEY_ACTIVITY_TYPE, "false");
                        params.put(DmConstants.KEY_SKIP_CLEAN_PREVIEW, "true");
                        params.put(DmConstants.KEY_COPIED_CONTENT, "true");
                        params.put(DmConstants.CONTENT_LIFECYCLE_OPERATION, lifecycleOp);

                        String id = site + ":" + copyPathOnly + ":" + copyFileName + ":" + contentType;

                        try {
                            generalLockService.lock(id);

                            // processContent will close the input stream
                            if (!StringUtils.isEmpty(contentType)) {
                                processContent(id, copyContent, true, params, DmConstants.CONTENT_CHAIN_FORM);
                            } else {
                                if (copyFileName.endsWith(DmConstants.XML_PATTERN)) {
                                    processContent(id, copyContent, true, params, DmConstants.CONTENT_CHAIN_FORM);
                                } else {
                                    processContent(id, fromContent, false, params, DmConstants.CONTENT_CHAIN_ASSET);
                                }
                            }

                            ItemState itemState = objectStateService.getObjectState(site, copyPath);

                            if (itemState == null) {
                                ContentItemTO copyItem = getContentItem(site, copyPath, 0);
                                objectStateService.insertNewEntry(site, copyItem);
                                objectStateService.setSystemProcessing(site, copyPath, false);
                            }

                            // copy was successful, return the new name
                            retNewFileName = copyPath;

                            // track that we already copied so we don't follow a circular dependency
                            processedPaths.add(copyPath);

                        } finally {
                            generalLockService.unlock(id);
                        }
                    } catch (ContentNotFoundException eContentNotFound) {
                        logger.debug("Content not found while copying content for site {0} from {1} to {2}, new name is {3}", eContentNotFound, site, fromPath, toPath, copyPath);
                    } catch (DocumentException eParseException) {
                        logger.error("General Error while copying content for site {0} from {1} to {2}, new name is {3}", eParseException, site, fromPath, toPath, copyPath);
                    } finally {
                        IOUtils.closeQuietly(copyContent);
                    }
                }
            } else {
                // no need to process
                retNewFileName = copyPath;
            }
        }
        catch(ServiceException eServiceException) {
            logger.info("General Error while copying content for site {0} from {1} to {2}, new name is {3}", eServiceException, site, fromPath, toPath, copyPath);
        }

        return retNewFileName;
    }

    @Override
    public String moveContent(String site, String fromPath, String toPath) {
        String retNewFileName = null;
        boolean opSuccess = false;
        String movePath = null;

        try {
            String sourcePath = (fromPath.indexOf("" + File.separator + DmConstants.INDEX_FILE) != -1) ? fromPath.substring(0, fromPath.lastIndexOf("/")) : fromPath;
            String sourcePathOnly = fromPath.substring(0, fromPath.lastIndexOf("/"));

            Map<String, String> movePathMap = constructNewPathforCutCopy(site, fromPath, toPath, true);
            movePath = movePathMap.get("FILE_PATH");
            String moveFileName = movePathMap.get("FILE_NAME");
            String movePathOnly = movePath.substring(0, movePath.lastIndexOf("/"));
            boolean moveAltFileName = "true".equals(movePathMap.get("ALT_NAME"));
            boolean targetIsIndex = DmConstants.INDEX_FILE.equals(moveFileName);
            boolean sourceIsIndex = DmConstants.INDEX_FILE.equals(fromPath);

            String targetPath = movePathOnly;
            if(movePathOnly.equals(sourcePathOnly)
                    || (moveAltFileName == true && !targetIsIndex)
                    ||  (!sourceIsIndex && !targetIsIndex)) {
                // we never send index.xml to the repo, we move folders (and the folder has the rename)
                // SO otherwise, this is a rename and we need to forward the full path
                targetPath = movePath;
            }

            logger.debug("Move file for site {0} from {1} to {2}, sourcePath {3} to target path {4}", site, fromPath, toPath, sourcePath, targetPath);

            // NOTE: IN WRITE SCENARIOS the repository OP IS PART of this PIPELINE, for some reason, historically with MOVE it is not
            Map<String, String> commitIds = _contentRepository.moveContent(site, sourcePath, targetPath);

            if (commitIds != null) {
                // Update the database with the commitId for the target item
                updateDatabaseOnMove(site, fromPath, movePath);
                updateChildrenOnMove(site, fromPath, movePath);
                for (Map.Entry<String, String> entry : commitIds.entrySet()) {
                    objectMetadataManager.updateCommitId(site, "/" + entry.getKey(), entry.getValue());
                }
                siteService.updateLastCommitId(site, _contentRepository.getRepoLastCommitId(site));
            }
            else {
                logger.error("Repository move failed site {0} from {1} to {2}", site, sourcePath, targetPath);
                movePath = fromPath;
            }

            PreviewEventContext context = new PreviewEventContext();
            context.setSite(site);
            eventService.publish(EVENT_PREVIEW_SYNC, context);
        }
        catch(ServiceException eMoveErr) {
            logger.error("Content not found while moving content for site {0} from {1} to {2}, new name is {3}", eMoveErr, site, fromPath, toPath, movePath);
        }

        return movePath;
    }

    protected void updateDatabaseOnMove(String site, String fromPath, String movePath) {
        logger.debug("updateDatabaseOnMove FROM {0} TO {1}  ", fromPath, movePath);

        String user = securityService.getCurrentUser();

        Map<String, String> params = new HashMap<>();
        params.put(DmConstants.KEY_SOURCE_PATH, fromPath);
        params.put(DmConstants.KEY_TARGET_PATH, movePath);
        // These do not exist in 3.0, note some extensions may be using it
        //params.put(DmConstants.KEY_SOURCE_FULL_PATH, expandRelativeSitePath(site, fromPath));
        //params.put(DmConstants.KEY_TARGET_FULL_PATH, expandRelativeSitePath(site, movePath));

        ContentItemTO renamedItem = getContentItem(site, movePath, 0);
        String contentType = renamedItem.getContentType();
        if (!renamedItem.isFolder()) {
            dmContentLifeCycleService.process(site, user, movePath, contentType, DmContentLifeCycleService
                    .ContentLifeCycleOperation.RENAME, params);

            // change the path of this object in the object state database
            objectStateService.updateObjectPath(site, fromPath, movePath);
            objectStateService.transition(site, renamedItem, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE);
        }
        // update metadata
        if (!objectMetadataManager.isRenamed(site, fromPath)) {
            // if an item was previously moved, we do not track intermediate moves because it will
            // ultimately orphan deployed content.  Old Path is always the OLDEST DEPLOYED PATH
            ItemMetadata metadata = objectMetadataManager.getProperties(site, fromPath);
            if (metadata == null && !renamedItem.isFolder()) {
                objectMetadataManager.insertNewObjectMetadata(site, fromPath);
                metadata = objectMetadataManager.getProperties(site, fromPath);
            }

            if (!renamedItem.isNew() && !renamedItem.isFolder()) {
                // if the item is not new, we need to track the old URL for deployment
                logger.debug("item is not new, and has not previously been moved. Track the old URL {0}", fromPath);
                Map<String, Object> objMetadataProps = new HashMap<String, Object>();
                objMetadataProps.put(ItemMetadata.PROP_RENAMED, 1);
                objMetadataProps.put(ItemMetadata.PROP_OLD_URL, fromPath);
                objectMetadataManager.setObjectMetadata(site, fromPath, objMetadataProps);
            }
        }

        if (!renamedItem.isFolder()) {
            objectMetadataManager.updateObjectPath(site, fromPath, movePath);
        }

        // write activity stream
        activityService.renameContentId(site, fromPath, movePath);

        Map<String, String> activityInfo = new HashMap<String, String>();
        String contentClass = getContentTypeClass(site, movePath);

        if(movePath.endsWith(DmConstants.XML_PATTERN)) {
            activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
        }

        if (!renamedItem.isFolder()) {
            activityService.postActivity(site, user, movePath, ActivityService.ActivityType.UPDATED, ActivityService.ActivitySource.UI, activityInfo);
        }

        updateDependenciesOnMove(site, fromPath, movePath);
    }

    protected void updateDependenciesOnMove(String site, String fromPath, String movePath) {
        dependencyService.deleteDependenciesForSiteAndPath(site, fromPath);
        if (movePath.endsWith(DmConstants.XML_PATTERN)) {
            try {
                Document document = getContentAsDocument(site, movePath);
                Map<String, Set<String>> globalDeps = new HashMap<>();
                dependencyService.extractDependencies(site, movePath, document, globalDeps);
            } catch (ServiceException | DocumentException e) {
                logger.error("Error while updating dependencies on move content site: " + site + " path: " + movePath, e);
            }
        } else {
            boolean isCss = movePath.endsWith(DmConstants.CSS_PATTERN);
            boolean isJs = movePath.endsWith(DmConstants.JS_PATTERN);
            List<String> templatePatterns = servicesConfig.getRenderingTemplatePatterns(site);
            boolean isTemplate = false;
            for (String templatePattern : templatePatterns) {
                Pattern pattern = Pattern.compile(templatePattern);
                Matcher matcher = pattern.matcher(movePath);
                if (matcher.matches()) {
                    isTemplate = true;
                    break;
                }
            }

            StringBuffer assetContent = new StringBuffer(getContentAsString(site, movePath));
            Map<String, Set<String>> globalDeps = new HashMap<String, Set<String>>();
            try {
                if (isCss) {
                    dependencyService.extractDependenciesStyle(site, movePath, assetContent, globalDeps);
                } else if (isJs) {
                    dependencyService.extractDependenciesJavascript(site, movePath, assetContent, globalDeps);
                } else if (isTemplate) {
                    dependencyService.extractDependenciesTemplate(site, movePath, assetContent, globalDeps);
                }
            } catch (ServiceException e) {
                logger.error("Error while updating dependencies on move content site: " + site + " path: " + movePath, e);
            }
        }
    }

    protected void updateChildrenOnMove(String site, String fromPath, String movePath) {
        logger.debug("updateChildrenOnMove from {0} to {1}", fromPath, movePath);

        // get the list of children
        ContentItemTO movedTO = getContentItem(site, movePath, 2);
        List<ContentItemTO> childrenTOs = movedTO.getChildren();

        for(ContentItemTO childTO : childrenTOs) {
            // calculate the child's from path by looking at it's parent's from path and the child new path
            // (parent move operation has already happened)
            String childToPath = childTO.getUri();

            String oldParentFolderPath = fromPath.replace("/index.xml", "");
            String parentFolderPath = movePath.replace("/index.xml", "");

            String childFromPath = childToPath.replace(parentFolderPath, oldParentFolderPath);

            logger.debug("updateChildrenOnMove handling child from: {0} to: {1}  ", childFromPath, childToPath);

            // update database, preview, cache etc
            updateDatabaseOnMove(site, childFromPath, childToPath);

            // handle this child's children
            updateChildrenOnMove(site, childFromPath, childToPath);
        }
    }

    protected Map<String, String> constructNewPathforCutCopy(String site, String fromPath, String toPath, boolean adjustOnCollide)
            throws ServiceException {
        Map<String, String> result = new HashMap<String, String>();

        // The following rules apply to content under the site folder
        String fromPathOnly = fromPath.substring(0, fromPath.lastIndexOf("/"));
        String fromFileNameOnly = fromPath.substring(fromPath.lastIndexOf("/")+1);
        boolean fromFileIsIndex = ("index.xml".equals(fromFileNameOnly));
        logger.debug("Cut/copy name rules from path: '{0}' name: '{1}'", fromPathOnly, fromFileNameOnly);

        if(fromFileIsIndex==true) {
            fromFileNameOnly = fromPathOnly.substring(fromPathOnly.lastIndexOf("/")+1);
            fromPathOnly = fromPathOnly.substring(0, fromPathOnly.lastIndexOf("/"));
            logger.debug("Cut/copy name rules index from path: '{0}' name: '{1}'", fromPathOnly, fromFileNameOnly);
        }

        String newPathOnly = (toPath.contains(".xml")) ? toPath.substring(0, toPath.lastIndexOf("/")) : toPath;
        String newFileNameOnly = (toPath.contains(".xml")) ? toPath.substring(toPath.lastIndexOf("/")+1) : fromFileNameOnly;
        boolean newFileIsIndex = ("index.xml".equals(newFileNameOnly));
        logger.debug("Cut/copy name rules to path: '{0}' name: '{1}'", newPathOnly, newFileNameOnly);

        if(newFileIsIndex==true) {
            newFileNameOnly = newPathOnly.substring(newPathOnly.lastIndexOf("/")+1);
            newPathOnly = newPathOnly.substring(0, newPathOnly.lastIndexOf("/"));
            logger.debug("Cut/copy name rules index to path: '{0}' name: '{1}'", newPathOnly, newFileNameOnly);
        }

        String proposedDestPath = null;
        String proposedDestPath_filename = null;
        String proposedDestPath_folder = null;
        boolean targetPathExistsPriorToOp = false;

        targetPathExistsPriorToOp = contentExists(site, toPath);

        if(fromFileIsIndex && newFileIsIndex) {
            // Example MOVE LOCATION, INDEX FILES
            // fromPath: "/site/website/search/index.xml"
            // toPath:   "/site/website/products/index.xml"
            // newPath:  "/site/website/products/search/index.xml"
            //
            // Example RENAME, INDEX FILES
            // fromPath: "/site/website/en/services/index.xml"
            // toPath:   "/site/website/en/services-updated/index.xml"
            // newPath:  "/site/website/en/services-updated/index.xml
            if(newPathOnly.equals(fromPathOnly) && !targetPathExistsPriorToOp) {
                // this is a rename
                proposedDestPath = newPathOnly + "/" + newFileNameOnly +  "/index.xml";
                proposedDestPath_filename = "index.xml";
                proposedDestPath_folder = newFileNameOnly;
            }
            else {
                // this is a location move
                proposedDestPath = newPathOnly + "/" + newFileNameOnly + "/" + fromFileNameOnly +  "/index.xml";
                proposedDestPath_filename = "index.xml";
                proposedDestPath_folder = fromFileNameOnly;
            }
        }
        else if(fromFileIsIndex && !newFileIsIndex) {
            // Example MOVE LOCATION, INDEX TO FOLDER
            // fromPath: "/site/website/search/index.xml"
            // toPath:   "/site/website/a-folder"
            // newPath:  "/site/website/a-folder/search/index.xml"
            proposedDestPath = newPathOnly + "/" + fromFileNameOnly +  "/index.xml";
            proposedDestPath_filename = "index.xml";
            proposedDestPath_folder = fromFileNameOnly;
        }
        else if(!fromFileIsIndex && newFileIsIndex) {
            proposedDestPath = newPathOnly + "/" + newFileNameOnly + "/" + fromFileNameOnly;
            proposedDestPath_filename = fromFileNameOnly;
            proposedDestPath_folder = newFileNameOnly;
        }
        else{
            // Example NON INDEX FILES MOVE TO FOLDER
            // fromPath: "/site/website/search.xml"
            // toPath:   "/site/website/a-folder"
            // newPath:  "/site/website/products/a-folder/search.xml"
            //
            // Example  INDEX FILES MOVE to FOLDER
            // fromPath: "/site/website/search.xml"
            // toPath:   "/site/website/products/search.xml"
            // newPath:  "/site/website/products/search.xml"
            if(fromFileNameOnly.equals(newFileNameOnly)) {
                // Move location
                proposedDestPath = newPathOnly + "/" + fromFileNameOnly;
                proposedDestPath_filename = fromFileNameOnly;
                proposedDestPath_folder = newPathOnly.substring(0, newPathOnly.lastIndexOf("/"));
            }
            else {
                // rename
                proposedDestPath = newPathOnly + "/" + newFileNameOnly;
                proposedDestPath_filename = newFileNameOnly;
                proposedDestPath_folder = newPathOnly.substring(0, newPathOnly.lastIndexOf("/"));
            }

        }

        logger.debug("Initial Proposed Path: {0} ", proposedDestPath);

        result.put("FILE_PATH", proposedDestPath);
        result.put("FILE_NAME", proposedDestPath_filename);
        result.put("FILE_FOLDER", proposedDestPath_folder);
        result.put("MODIFIER", "");
        result.put("ALT_NAME", "false");

        boolean contentExists = false;

        if(adjustOnCollide == true) {
            // if adjustOnCollide is true we need to check, otherwise we don't
            contentExists = contentExists(site, proposedDestPath);
        }

        if(adjustOnCollide && contentExists) {
            logger.debug("File already found at path {0}, creating new name", proposedDestPath);
            try {
                Map<String,String> ids = contentItemIdGenerator.getIds();
                String id = ids.get(DmConstants.KEY_PAGE_GROUP_ID);

                //proposedDestPath = getNextAvailableName(site, proposedDestPath);

                if(proposedDestPath.indexOf("/index.xml") == -1) {
                    int pdpli = proposedDestPath.lastIndexOf(".");
                    if (pdpli == -1) pdpli = proposedDestPath.length();
                    proposedDestPath =
                            proposedDestPath.substring(0, pdpli) + "-" + id +
                                    proposedDestPath.substring(pdpli);

                    // a regex would be better
                    proposedDestPath_filename = proposedDestPath.substring(proposedDestPath.lastIndexOf("/")+1);
                    proposedDestPath_folder = proposedDestPath.substring(0, proposedDestPath.lastIndexOf("/"));
                    proposedDestPath_folder = proposedDestPath_folder.substring(proposedDestPath_folder.lastIndexOf("/")+1);
                }
                else {
                    proposedDestPath =
                            proposedDestPath.substring(0, proposedDestPath.indexOf("/index.xml")) + "-" + id +
                                    proposedDestPath.substring(proposedDestPath.lastIndexOf("/index.xml"));

                    proposedDestPath_filename = "index.xml";
                    proposedDestPath_folder = proposedDestPath.replace("/index.xml","");
                    proposedDestPath_folder = proposedDestPath_folder.substring(proposedDestPath_folder.lastIndexOf("/")+1);
                }

                result.put("FILE_PATH", proposedDestPath);
                result.put("FILE_NAME", proposedDestPath_filename);
                result.put("FILE_FOLDER", proposedDestPath_folder);
                result.put("MODIFIER", id);
                result.put("ALT_NAME", "true");
            }
            catch(Exception altPathGenErr) {
                throw new ServiceException("Unable to generate an alternative path for name collision: " + proposedDestPath, altPathGenErr);
            }
        }

        logger.debug("Final proposed path from : '{0}' to: '{1}' final name '{2}'", fromPath, toPath,
            proposedDestPath);
        return result;
    }

    protected Map<String, String> getItemSpecificDependencies(Document document, Map<String, String> copyDependencies) {
        // TODO: RD: This should move to the dependency service to be fully computed there
        //update pageId and groupId with the new one
        Element root = document.getRootElement();

        List<Node> keys = root.selectNodes("//key");
        if (keys != null) {
            for(Node keyNode : keys) {
                String keyValue = ((Element)keyNode).getText();
                if(keyValue.contains("/page")) {
                    copyDependencies.put(keyValue, keyValue);
                }
            }
        }

        List<Node> includes = root.selectNodes("//include");
        if (includes != null) {
            for(Node includeNode : includes) {
                String includeValue = ((Element)includeNode).getText();
                if(includeValue.contains("/page")) {
                    copyDependencies.put(includeValue, includeValue);
                }
            }
        }

        return copyDependencies;
    }

    /**
     * Return the values for PageID and GroupID provided a Document
     * @param document DOM to search
     * @return Map of IDs
     */
    protected Map<String, String> getContentIds(Document document) {
        Map<String, String> ids = new HashMap<String, String>();

        Element root = document.getRootElement();
        Node pageIdNode = root.selectSingleNode("//" + DmXmlConstants.ELM_PAGE_ID);
        if (pageIdNode != null) {
            ids.put(DmConstants.KEY_PAGE_ID, ((Element)pageIdNode).getText());
        }

        Node groupIdNode = root.selectSingleNode("//" + DmXmlConstants.ELM_GROUP_ID);
        if (groupIdNode != null) {
            ids.put(DmConstants.KEY_PAGE_GROUP_ID, ((Element)groupIdNode).getText());
        }

        return ids;
    }

    protected Document updateContentOnCopy(Document document, String filename, String folder, Map<String,
        String> params, String modifier)
            throws ServiceException {

        //update pageId and groupId with the new one
        Element root = document.getRootElement();
        String originalPageId = null;
        String originalGroupId = null;

        Node filenameNode = root.selectSingleNode("//" + DmXmlConstants.ELM_FILE_NAME);
        if (filenameNode != null) {
            filenameNode.setText(filename);
        }

        Node folderNode = root.selectSingleNode("//" + DmXmlConstants.ELM_FOLDER_NAME);
        if (folderNode != null) {
            folderNode.setText(folder);
        }

        Node pageIdNode = root.selectSingleNode("//" + DmXmlConstants.ELM_PAGE_ID);
        if (pageIdNode != null) {
            originalPageId = pageIdNode.getText();
            pageIdNode.setText(params.get(DmConstants.KEY_PAGE_ID));
        }

        if(modifier != null) {
            Node internalNameNode = root.selectSingleNode("//" + DmXmlConstants.ELM_INTERNAL_NAME);
            if (internalNameNode != null) {
                String internalNameValue = internalNameNode.getText();
                internalNameNode.setText(internalNameValue + " " + modifier);
            }
        }

        Node groupIdNode = root.selectSingleNode("//" + DmXmlConstants.ELM_GROUP_ID);
        if (groupIdNode != null) {
            originalGroupId = groupIdNode.getText();
            groupIdNode.setText(params.get(DmConstants.KEY_PAGE_GROUP_ID));
        }

        List<Node> keys = root.selectNodes("//key");
        if (keys != null) {
            for(Node keyNode : keys) {
                String keyValue = keyNode.getText();
                keyValue = keyValue.replaceAll(originalPageId, params.get(DmConstants.KEY_PAGE_ID));
                keyValue = keyValue.replaceAll(originalGroupId, params.get(DmConstants.KEY_PAGE_GROUP_ID));

                if(keyValue.contains("/page")) {
                    keyNode.setText(keyValue);
                }
            }
        }

        List<Node> includes = root.selectNodes("//include");
        if (includes != null) {
            for(Node includeNode : includes) {
                String includeValue = includeNode.getText();
                includeValue = includeValue.replaceAll(originalPageId, params.get(DmConstants.KEY_PAGE_ID));
                includeValue = includeValue.replaceAll(originalGroupId, params.get(DmConstants.KEY_PAGE_GROUP_ID));

                if(includeValue.contains("/page")) {
                    includeNode.setText(includeValue);
                }
            }
        }

        return document;
    }

 /* ======================== */

    protected ContentItemTO createNewContentItemTO(String site, String contentPath) {
        ContentItemTO item = new ContentItemTO();
        contentPath = contentPath.replace("//", "/");   // FIXME: SJ: This is another workaround for UI issues

        item.uri = contentPath;
        item.path = contentPath.substring(0, contentPath.lastIndexOf("/"));
        item.name = contentPath.substring(contentPath.lastIndexOf("/") + 1);

        item.asset = true;
        item.site = site;
        item.internalName = item.name;
        item.contentType = CONTENT_TYPE_UNKNOWN;
        item.disabled = false;
        item.savedAsDraft = false;
        item.floating = false;
        item.hideInAuthoring = false;

        item.page = false;
        item.previewable = false;
        item.isPreviewable = false;
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
        item.folder = (item.name.contains(".")==false); // TODO: SJ: This seems hokey, fix in 3.1+

        return item;
    }

    protected ContentItemTO populateContentDrivenProperties(String site, ContentItemTO item)
            throws Exception {
        // This method load an XML content item and populates properties in the TO from the XML
        // TODO: SJ: Two problems here that need to be fixed in 3.1+
        // TODO: SJ: Use Crafter Core for some/all of this work
        // TODO: SJ: Much of this seems hardcoded and must be extensible/configurable via key:xpath in config
        String contentPath = item.uri;

        logger.debug("Populating page props '{}'", contentPath);
        item.setLevelDescriptor(item.name.equals(servicesConfig.getLevelDescriptorName(site)));
        item.page = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getPagePatterns(site));
        item.isPage = item.page;
        item.previewable = item.page;               // TODO: SJ: This and item below are duplicated due to UI issues
        item.isPreviewable = item.previewable;      // TODO: SJ: Fix this in 3.1+
        item.component = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)) || item.isLevelDescriptor();
        item.isComponent = item.component;
        item.asset = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getAssetPatterns(site));
        item.isAsset = item.asset;
        item.document = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site));
        item.isDocument = item.document;

        item.uri = contentPath;
        item.path = contentPath.substring(0, contentPath.lastIndexOf("/")); // TODO: SJ: This is hokey, fix in 3.1+
        item.name = contentPath.substring(contentPath.lastIndexOf("/") + 1);// TODO: SJ: This is hokey, fix in 3.1+
        item.browserUri = contentPath;

        if(item.page) {
            // TODO: SJ: This is hokey, fix in 4.x
            item.browserUri = contentPath.replace("/site/website", "").replace("/index.xml", "");
        }

        Document contentDoc = this.getContentAsDocument(site, contentPath);
        if(contentDoc != null) {
            Element rootElement = contentDoc.getRootElement();

            String internalName = rootElement.valueOf("internal-name");
            String contentType = rootElement.valueOf("content-type");
            String disabled = rootElement.valueOf("disabled");
            String savedAsDraft = rootElement.valueOf("savedAsDraft");
            String navigation = rootElement.valueOf("placeInNav");
            String hideInAuthoring = rootElement.valueOf("hideInAuthoring");
            String displayTemplate = rootElement.valueOf("display-template");

            item.internalName = (internalName!=null) ? internalName : null;
            item.contentType = (contentType!=null) ? contentType : null;
            item.disabled = (disabled!=null && "true".equalsIgnoreCase(disabled)) ? true : false;
            item.savedAsDraft = (savedAsDraft!=null && "true".equalsIgnoreCase(savedAsDraft)) ? true : false;
            item.hideInAuthoring = (hideInAuthoring!=null && "true".equalsIgnoreCase(hideInAuthoring)) ? true : false;

            item.navigation = (navigation!=null && "true".equalsIgnoreCase(navigation)) ? true : false;
            item.floating = !item.navigation;

            item.setOrders(getItemOrders(rootElement.selectNodes("//" + DmXmlConstants.ELM_ORDER_DEFAULT)));

            if(displayTemplate != null) {
                RenderingTemplateTO template = new RenderingTemplateTO();
                template.uri = displayTemplate;
                template.name = "DEFAULT";      // FIXME: SJ: 3.1+

                item.renderingTemplates.add(template);
            }
        }
        else {
            logger.debug("no xml document could be loaded for site '{}' path '{}'", site, contentPath);
        }

        Pattern taxonomyPattern = Pattern.compile(CONTENT_TYPE_TAXONOMY_REGEX);
        Matcher matcher = taxonomyPattern.matcher(contentPath);
        if (matcher.matches()) {
            item.contentType = CONTENT_TYPE_TAXONOMY;
        }

        return item;
    }

    /**
     * add order value to the list of orders
     *
     * @param orders
     * @param orderName
     * @param orderStr
     */
    protected void addOrderValue(List<DmOrderTO> orders, String orderName, String orderStr) {
        Double orderValue = null;
        try {
            orderValue = Double.parseDouble(orderStr);
        } catch (NumberFormatException e) {
            logger.debug(orderName + ", " + orderStr + " is not a valid order value pair.");
        }
        if (!StringUtils.isEmpty(orderName) && orderValue != null) {
            DmOrderTO order = new DmOrderTO();
            order.setId(orderName);
            order.setOrder(orderValue);
            orders.add(order);
        }
    }

    /**
     * get WCM content item order metadata
     *
     * @param nodes
     * @return
     */
    protected List<DmOrderTO> getItemOrders(List<Node> nodes) {
        // TODO: SJ: Rewrite this and the whole order/sort system; 3.1+
        if (nodes != null) {
            List<DmOrderTO> orders = new ArrayList<DmOrderTO>(nodes.size());
            for (Node node : nodes) {

                String orderName = DmConstants.JSON_KEY_ORDER_DEFAULT;
                String orderStr = node.getText();
                addOrderValue(orders, orderName, orderStr);
            }
            return orders;
        } else {
            return null;
        }
    }

    protected ContentItemTO populateItemChildren(ContentItemTO item, int depth) {
        // TODO: SJ: Refactor  in 3.1+
        String contentPath = item.uri;

        item.children = new ArrayList<ContentItemTO>();
        item.numOfChildren = 0;

        if(contentPath.indexOf("/index.xml") != -1
                || contentPath.indexOf(".") == -1 ) { // item.isFolder?

            if (contentPath.indexOf("/index.xml") != -1) {
                contentPath = contentPath.replace("/index.xml", "");
            }


            RepositoryItem[] childRepoItems = _contentRepository.getContentChildren(item.site, contentPath);
            boolean indexFound = false;

            if(childRepoItems != null) {
                item.numOfChildren = childRepoItems.length;
                if (item.numOfChildren != 0) {
                    item.isContainer = true;
                    item.container = true;
                }

                List<ContentItemTO> children = new ArrayList<>();
                logger.debug("Checking if {0} has index", contentPath);
                for (int j = 0; j < childRepoItems.length; j++) {
                    if ("index.xml".equals(childRepoItems[j].name)) {
                        if (!item.uri.contains("/index.xml")) {
                            item.path = item.uri;
                            item.uri = item.uri + "/index.xml";
                        }
                        item.numOfChildren--;
                        indexFound = true;
                    }
                    else {
                        if (depth > 1) {
                            String childPath = childRepoItems[j].path + "/" + childRepoItems[j].name;
                            if (childPath.startsWith("/site/website/") && childRepoItems[j].isFolder && contentExists(item.site, childPath + "/index.xml")) {
                                children.add(getContentItem(item.site, childPath + "/index.xml", depth - 1));
                            } else {
                                children.add(getContentItem(item.site, childPath, depth - 1));
                            }
                        }
                    }
                }

                if(!indexFound) {
                    // ITEM IS A FOLDER
                    item.folder = true;
                    item.isContainer = true;
                    item.container = true;

                    item.page = false;
                    item.asset = false;
                    item.component = false;
                    item.previewable = false;
                    item.isPreviewable = false;

                    item.internalName = item.name;
                    item.contentType = "folder";
                    item.path = item.uri;
                }

                // ORDER THE CHILDREN
                // level descriptors first
                // nav pages by order
                // floating pages via Alpha
                Comparator<ContentItemTO> comparator = new ContentItemOrderComparator("default", true, true, true);
                Collections.sort(children, comparator);
                item.children = children;

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

        if(item.internalName == null) item.internalName = item.name;

        return item;
    }

    @Override
    public ContentItemTO getContentItem(String site, String path) {
        return getContentItem(site, path, 2);
    }


    @Override
    public ContentItemTO getContentItem(String site, String path, int depth) {
        ContentItemTO item = null;
        logger.debug("Getting content item for site '{}' path '{}' depth '{}'", site, path, depth);

        DebugUtils.addDebugStack(logger);
        long startTime = System.currentTimeMillis();

        try {
            if (contentExists(site, path)) {
                // get item from cache
                item = loadContentItem(site, path);

                if (depth != 0) {
                    item = populateItemChildren(item, depth);
                }

                // POPULATE LOCK STATUS
                populateMetadata(site, item);

                // POPULATE WORKFLOW STATUS
                if (!item.isFolder() || item.isContainer()) {
                    populateWorkflowProperties(site, item);
                } else {
                    item.setNew(!objectStateService.isFolderLive(site, item.getUri()));
                    item.isNew = item.isNew();
                }
            } else {
                item = createDummyDmContentItemForDeletedNode(site, path);
            }
        }
        catch(Exception err) {
            logger.debug("error constructing item for object at site '{}' path '{}'", err, site, path);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        logger.debug("Content item from site '{}' path '{}' retrieved in '{}' milli-seconds", site, path, executionTime);
        return item;
    }

    protected ContentItemTO loadContentItem(String site, String path) {
        // TODO: SJ: Refactor such that the populate of non-XML is also a method in 3.1+
        ContentItemTO item = createNewContentItemTO(site, path);

        if (item.uri.endsWith(".xml")) {

            try {
                item = populateContentDrivenProperties(site, item);
            } catch (Exception err) {
                logger.debug("error constructing item for object at site '{}' path '{}'", err, site, path);
            }
        } else {
            item.setLevelDescriptor(item.name.equals(servicesConfig.getLevelDescriptorName(site)));
            item.page = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getPagePatterns(site));
            item.isPage = item.page;
            item.previewable = item.page;
            item.isPreviewable = item.previewable;
            item.asset = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getAssetPatterns(site)) || ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getRenderingTemplatePatterns(site)) || ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getScriptsPatterns(site));;
            item.isAsset = item.asset;
            item.component = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)) || item.isLevelDescriptor() || item.asset;
            item.isComponent = item.component;
            item.document = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site));
            item.isDocument = item.document;
            item.browserUri =item.getUri();
        }

        loadContentTypeProperties(site, item, item.contentType);

        MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
        String mimeType = mimeTypesMap.getContentType(item.getName());
        if (StringUtils.isNotEmpty(mimeType)) {
            item.setMimeType(mimeType);
        }
        return item;
    }

    protected void loadContentTypeProperties(String site, ContentItemTO item, String contentType) {
        // TODO: SJ: Refactor in 2.7.x
        if(contentType != null && !contentType.equals("folder") && !contentType.equals("asset") && !contentType.equals(CONTENT_TYPE_UNKNOWN)) {
            ContentTypeConfigTO config = servicesConfig.getContentTypeConfig(site, contentType);
            if (config != null) {
                item.setForm(config.getForm());
                item.setFormPagePath(config.getFormPath());
                item.setPreviewable(config.isPreviewable());
                item.isPreviewable = item.previewable;
            }
        } else {
            MimetypesFileTypeMap mimeTypesMap = new MimetypesFileTypeMap();
            String mimeType = mimeTypesMap.getContentType(item.getName());
            if (mimeType != null && !StringUtils.isEmpty(mimeType)) {
                item.setPreviewable(ContentUtils.matchesPatterns(mimeType, servicesConfig
                        .getPreviewableMimetypesPaterns(site)));
                item.isPreviewable = item.previewable;
            }
        }
        // TODO CodeRev:but what if the config is null?
    }

    protected void populateWorkflowProperties(String site, ContentItemTO item) {
        ItemState state = objectStateService.getObjectState(site, item.getUri(), false);
        if (state != null) {
            if (item.isFolder()) {
                boolean liveFolder = objectStateService.isFolderLive(site, item.getUri());
                item.setNew(!liveFolder);
                item.setLive(liveFolder);
            } else {
                item.setNew(org.craftercms.studio.api.v1.service.objectstate.State.isNew(org.craftercms.studio.api.v1.service.objectstate.State.valueOf(state.getState())));
                item.setLive(org.craftercms.studio.api.v1.service.objectstate.State.isLive(org.craftercms.studio.api.v1.service.objectstate.State.valueOf(state.getState())));
            }
            item.isNew = item.isNew();
            item.isLive = item.isLive();
            item.setInProgress(!item.isLive());
            item.isInProgress = item.isInProgress();
            item.setScheduled(org.craftercms.studio.api.v1.service.objectstate.State.isScheduled(org.craftercms.studio.api.v1.service.objectstate.State.valueOf(state.getState())));
            item.isScheduled = item.isScheduled();
            item.setSubmitted(org.craftercms.studio.api.v1.service.objectstate.State.isSubmitted(org.craftercms.studio.api.v1.service.objectstate.State.valueOf(state.getState())));
            item.isSubmitted = item.isSubmitted();
            item.setInFlight(state.getSystemProcessing() == 1);
            item.isInFlight = item.isInFlight();
        } else {
            if (item.isFolder()) {
                boolean liveFolder = objectStateService.isFolderLive(site, item.getUri());
                item.setNew(!liveFolder);
                item.setLive(liveFolder);
                item.isNew = item.isNew();
                item.isLive = item.isLive();
            }
        }
    }

    protected void populateMetadata(String site, ContentItemTO item) {
        // TODO: SJ: Refactor to return a ContentItemTO instead of changing the parameter
        // TODO: SJ: Change method name to be getContentItemMetadata or similar
        // TODO: SJ: 3.1+

        // TODO: SJ: Create a method String getValueIfNotNull(String) to use to return not null/empty string if null
        // TODO: SJ: Use that method to reduce redundant code here. 3.1+
        ItemMetadata metadata = objectMetadataManager.getProperties(site, item.getUri());
        if (metadata != null) {
            // Set the lock owner to empty string if we get a null to not confuse the UI, or set it to what's in the
            // database if it's not null
            if (StringUtils.isEmpty(metadata.getLockOwner())) {
                item.setLockOwner("");
            } else {
                item.setLockOwner(metadata.getLockOwner());
            }

            // Set the scheduled date
            if (metadata.getLaunchDate() != null) {
                item.scheduledDate = metadata.getLaunchDate();
                item.setScheduledDate(metadata.getLaunchDate());
            }

            // Set the modifier (user) if known
            if (StringUtils.isEmpty(metadata.getModifier())) {
                item.setUser("");
                item.setUserLastName("");
                item.setUserFirstName("");
            } else {
                item.user = metadata.getModifier();
                item.setUser(metadata.getModifier());
                if (StringUtils.isEmpty(metadata.getFirstName())) {
                    item.userFirstName = metadata.getModifier();
                    item.setUserFirstName(metadata.getModifier());
                } else {
                    item.userFirstName = metadata.getFirstName();
                    item.setUserFirstName(metadata.getFirstName());
                }
                if (StringUtils.isEmpty(metadata.getLastName())) {
                    item.userLastName = "";
                    item.setUserLastName("");
                } else {
                    item.userLastName = metadata.getLastName();
                    item.setUserLastName(metadata.getLastName());
                }
            }

            if (metadata.getModified() != null) {
                item.lastEditDate = metadata.getModified();
                item.eventDate = metadata.getModified();
                item.setLastEditDate(metadata.getModified());
                item.setEventDate(metadata.getModified());
            }
            if (StringUtils.isNotEmpty(metadata.getSubmissionComment())) {
                item.setSubmissionComment(metadata.getSubmissionComment());
            }
        } else {
            item.setLockOwner("");
        }
    }

    @Override
    public ContentItemTO getContentItemTree(String site, String path, int depth) {
        logger.debug("Getting content item  tree for '{}':'{}' depth '{}'", site, path, depth);
        DebugUtils.addDebugStack(logger);

        long startTime = System.currentTimeMillis();
        boolean isPages = (path.contains("/site/website"));
        ContentItemTO root = null;

        if (isPages && contentExists(site, path + "/index.xml")) {
            if (depth > 1) {
                root = getContentItem(site, path + "/index.xml", depth);
            } else {
                root = getContentItem(site, path + "/index.xml");
            }
        }
        else {
            if (depth > 1) {
                root = getContentItem(site, path, depth);
            } else {
                root = getContentItem(site, path);
            }
        }

        long executionTime = System.currentTimeMillis() - startTime;
        logger.debug("Content item tree ['{}':'{}' depth '{}'] retrieved in '{}' milli-seconds", site, path, depth,
                executionTime);

        return root;
    }

    @Override
    public VersionTO[] getContentItemVersionHistory(String site, String path) {
        // TODO: SJ: Switch this to return a collection and rely on Groovy to change it up for the UI
        return _contentRepository.getContentVersionHistory(site, path);
    }

    @Override
    public boolean revertContentItem(String site, String path, String version, boolean major, String comment) {
        boolean toReturn = false;
        String commitId = _contentRepository.revertContent(site, path, version, major, comment);

        if (commitId != null) {
            // Update the database with the commitId for the target item
            objectMetadataManager.updateCommitId(site, path, commitId);
            siteService.updateLastCommitId(site, commitId);
            toReturn = true;
        }

        if (toReturn) {
            PreviewEventContext context = new PreviewEventContext();
            context.setSite(site);
            eventService.publish(EVENT_PREVIEW_SYNC, context);
        }

        return toReturn;
    }

    @Override
    public InputStream getContentVersion(String site, String path, String version)
            throws ContentNotFoundException {
        return _contentRepository.getContentVersion(site, path, version);
    }

    @Override
    public String getContentVersionAsString(String site, String path, String version)
            throws ContentNotFoundException {
        String content = null;

        try {
            content = IOUtils.toString(getContentVersion(site, path, version));
        }
        catch(Exception err) {
            logger.debug("Failed to get content as string for path {0}, exception {1}", path, err);
        }

        return content;
    }

    @Override
    public ContentItemTO createDummyDmContentItemForDeletedNode(String site, String relativePath) {
        // TODO: SJ: Think of another way to do this in 3.1+

        ContentItemTO item = new ContentItemTO();
        String timeZone = servicesConfig.getDefaultTimezone(site);
        item.timezone = timeZone;
        String name = ContentUtils.getPageName(relativePath);
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
        item.isDisabled = false;
        item.isNavigation = false;
        item.name = name;
        item.uri = relativePath;

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
        item.deleted = true;
        item.isContainer = false;
        item.container = false;
        item.isNew = false;
        item.isInProgress = false;
        item.timezone = servicesConfig.getDefaultTimezone(site);
        item.isPreviewable = false;
        item.browserUri = getBrowserUri(item);

        return item;
    }

    protected String getBrowserUri(ContentItemTO item) {
        String replacePattern;

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
        // TODO: SJ: This reads: if can't guess what it is, it's a page. This is to be replaced in 3.1+
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
    public ResultTO processContent(String id, InputStream input, boolean isXml, Map<String, String> params, String contentChainForm) throws ServiceException {
        // TODO: SJ: Pipeline Processor is not defined right, we need to refactor in 3.1+
        // TODO: SJ: Pipeline should take input, and give you back output
        // TODO: SJ: Presently, this takes action and performs the action as a side effect of the processor chain
        // TODO: SJ: Furthermore, we have redundancy in the code of the processors
        // get sandbox if not provided
        long start = System.currentTimeMillis();
        try {

            String[] strings = id.split(":");
            long startTime = System.currentTimeMillis();
            ResultTO to = contentProcessor.processContent(id, input, isXml, params, contentChainForm);
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Write Duration: '{}'", duration);
            return to;
        } finally {
            long end = System.currentTimeMillis();
            logger.debug("Write complete for [" + id + "] in time [" + (end - start) + "]");
        }
    }

    @Override
    public String getNextAvailableName(String site, String path) {
        // TODO: SJ: Refactor to be faster, and make it work regardless (seems to fail above 10) in 3.1+
        String[] levels = path.split("/");
        int length = levels.length;
        if (length > 0) {
            ContentItemTO item = getContentItem(site, path, 0);
            if (item != null) {
                String name = ContentUtils.getPageName(path);
                String parentPath = ContentUtils.getParentUrl(path);
                ContentItemTO parentItem = getContentItemTree(site, parentPath, 1);
                if (parentItem != null) {
                    int lastIndex = name.lastIndexOf(".");
                    String ext = (item.isFolder()) ? "" : name.substring(lastIndex);
                    String originalName = (item.isFolder() || item.isContainer()) ? name : name.substring(0, lastIndex);
                    List<ContentItemTO> children = parentItem.getChildren();
                    // pattern matching doesn't work here
                    // String childNamePattern = originalName + "%" + ext;
                    int lastNumber = 0;
                    String namePattern = originalName + "-[0-9]+" + ext;
                    if (children != null && children.size() > 0) {
                        // since it is already sorted, we only care about the last matching item
                        for (ContentItemTO child : children) {
                            if (((item.isFolder() || item.isContainer()) == (child.isFolder() || child.isContainer()))) {
                                String childName = child.getName();
                                if ((child.isFolder() || child.isContainer())) {
                                    childName = ContentUtils.getPageName(child.getBrowserUri());
                                }
                                if (childName.matches(namePattern)) {
                                    Pattern pattern = (item.isFolder() || item.isContainer()) ? COPY_FOLDER_PATTERN : COPY_FILE_PATTERN;
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
        ContentItemTO contentItem = getContentItem(site, relativePath);
        GoLiveDeleteCandidates deletedItems = new GoLiveDeleteCandidates(site, this);
        if (contentItem != null) {
            childDeleteItems(site, contentItem, deletedItems);
            //update summary for all uri's delete
        }
        return deletedItems;
    }

    /**
     * Iterate over all paths inside the folder
     */
    protected void childDeleteItems(String site, ContentItemTO contentItem, GoLiveDeleteCandidates items) throws ServiceException {
        // TODO: SJ: Reconsider to be iterative instead of recursive in 3.1+
        // TODO: SJ: Reconsider having bulk operations in the underlying repository to speed things up and result
        // TODO: SJ: in less database writes and repo commits

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
        // TODO: SJ: Where is the object state update to indicate item is now locked?
        // TODO: SJ: Dejan to look into this
        _contentRepository.lockItem(site, path);
        objectMetadataManager.lockContent(site, path, securityService.getCurrentUser());
    }

    @Override
    public void unLockContent(String site, String path) {
        ContentItemTO item = getContentItem(site, path, 0);
        objectStateService.transition(site, item, TransitionEvent.CANCEL_EDIT); // this unlocks too
        _contentRepository.unLockItem(site, path);
        objectMetadataManager.unLockContent(site, path);
    }

    @Override
    public List<DmOrderTO> getItemOrders(String site, String path) throws ContentNotFoundException {
        List<DmOrderTO> dmOrderTOs = getOrders(site, path, "default", false);
        for (DmOrderTO dmOrderTO : dmOrderTOs) {
            dmOrderTO.setName(StringEscapeUtils.escapeJava(dmOrderTO.getName()));
        }
        return dmOrderTOs;
    }

    private List<DmOrderTO> getOrders(String site, String relativePath, String orderName, boolean includeFloating) {
        // TODO: SJ: Refactor this in 3.1+
        // TODO: SJ: Crafter Core already does some of this, refactor/redo
        // if the path ends with index.xml, remove index.xml and also remove the last folder
        // otherwise remove the file name only
        if (!StringUtils.isEmpty(relativePath)) {
            if (relativePath.endsWith(DmConstants.XML_PATTERN)) {
                int index = relativePath.lastIndexOf("/");
                if (index > 0) {
                    String fileName = relativePath.substring(index + 1);
                    String path = relativePath.substring(0, index);
                    if (DmConstants.INDEX_FILE.equals(fileName)) {
                        int secondIndex = path.lastIndexOf("/");
                        if (secondIndex > 0) {
                            path = path.substring(0, secondIndex);
                        }
                    }
                    relativePath = path;
                }
            }
        }
        // get the root item and its children
        ContentItemTO item = getContentItem(site, relativePath);
        if (item.getChildren() != null) {
            List<DmOrderTO> orders = new ArrayList<DmOrderTO>(item.getChildren().size());
            String pathIndex = relativePath + "/" + DmConstants.INDEX_FILE;
            for (ContentItemTO child : item.getChildren()) {
                // exclude index.xml, the level descriptor and floating pages at the path
                if (!(pathIndex.equals(child.getUri()) || child.isLevelDescriptor() || child.isDeleted()) && (!child.isFloating() || includeFloating)) {
                    DmOrderTO order = new DmOrderTO();
                    order.setId(child.getUri());
                    Double orderNumber = child.getOrder(orderName);
                    // add only if the page contains order information
                    if (orderNumber != null && orderNumber > 0) {
                        order.setOrder(child.getOrder(orderName));
                        order.setName(child.getInternalName());
                        if (child.isDisabled())
                            order.setDisabled("true");
                        else
                            order.setDisabled("false");

                        if (child.isNavigation())
                            order.setPlaceInNav("true");
                        else
                            order.setPlaceInNav("false");

                        orders.add(order);
                    }
                }
            }
            return orders;
        }
        return null;
    }

    @Override
    public double reorderItems(String site, String relativePath, String before, String after, String orderName) throws ServiceException {
        Double beforeOrder = null;
        Double afterOrder = null;
        DmOrderTO beforeOrderTO = null;
        DmOrderTO afterOrderTO = null;
        // get the order of the content before
        // if the path is not provided, the order is 0
        if (!StringUtils.isEmpty(before)) {
            ContentItemTO beforeItem = getContentItem(site, before, 0);
            beforeOrder = beforeItem.getOrder(orderName);
            beforeOrderTO = new DmOrderTO();
            beforeOrderTO.setId(before);
            if (beforeOrder != null && beforeOrder > 0) {
                beforeOrderTO.setOrder(beforeOrder);
            }
        }
        // get the order of the content after
        // if the path is not provided, the order is the order of before +
        // ORDER_INCREMENT
        if (!StringUtils.isEmpty(after)) {
            ContentItemTO afterItem = getContentItem(site, after, 0);
            afterOrder = afterItem.getOrder(orderName);
            afterOrderTO = new DmOrderTO();
            afterOrderTO.setId(after);
            if (afterOrder != null && afterOrder > 0) {
                afterOrderTO.setOrder(afterOrder);
            }
        }

        // if no after and before provided, the initial value is ORDER_INCREMENT
        if (afterOrder == null && beforeOrder == null) {
            return dmPageNavigationOrderService.getNewNavOrder(site, ContentUtils.getParentUrl(relativePath.replace("/" + DmConstants.INDEX_FILE, "")));
        } else if (beforeOrder == null) {
            return (0 + afterOrder) / 2;
        } else if (afterOrder == null) {
            logger.info("afterOrder == null");
            return dmPageNavigationOrderService.getNewNavOrder(site, ContentUtils.getParentUrl(relativePath.replace("/" + DmConstants.INDEX_FILE, "")), beforeOrder);
        } else {
            //return (beforeOrder + afterOrder) / 2;
            return computeReorder(site, relativePath, beforeOrderTO, afterOrderTO, orderName);
        }
    }

    /**
     * Will need to include the floating pages as well for orderValue computation
     * Since the beforeOrder and afterOrder in the UI does not include floating pages will need to do special processing
     */
    protected double computeReorder(String site, String relativePath, DmOrderTO beforeOrderTO, DmOrderTO afterOrderTO, String orderName) throws ContentNotFoundException {
        // TODO: SJ: This seems excessive, all we need is: double result = (getBefore + getAfter) / 2; return result;

        List<DmOrderTO> orderTO = getOrders(site, relativePath, orderName, true);
        Collections.sort(orderTO);

        int beforeIndex = orderTO.indexOf(beforeOrderTO);
        int afterIndex = orderTO.indexOf(afterOrderTO);

        if (!(beforeIndex + 1 == afterIndex)) {
            beforeOrderTO = orderTO.get(afterIndex - 1);
        }
        return (beforeOrderTO.getOrder() + afterOrderTO.getOrder()) / 2;
    }

    private ContentRepository _contentRepository;
    protected ServicesConfig servicesConfig;
    protected GeneralLockService generalLockService;
    protected ObjectStateService objectStateService;
    protected DmDependencyService dependencyService;
    protected ProcessContentExecutor contentProcessor;
    protected ObjectMetadataManager objectMetadataManager;
    protected SecurityService securityService;
    protected DmPageNavigationOrderService dmPageNavigationOrderService;
    protected SecurityProvider securityProvider;
    protected ActivityService activityService;
    protected DmContentLifeCycleService dmContentLifeCycleService;
    protected EventService eventService;
    protected SiteService siteService;
    protected ContentItemIdGenerator contentItemIdGenerator;
    protected StudioConfiguration studioConfiguration;

    public ContentRepository getContentRepository() { return _contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this._contentRepository = contentRepository; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public DmDependencyService getDependencyService() { return dependencyService; }
    public void setDependencyService(DmDependencyService dependencyService) { this.dependencyService = dependencyService; }

    public ProcessContentExecutor getContentProcessor() { return contentProcessor; }
    public void setContentProcessor(ProcessContentExecutor contentProcessor) { this.contentProcessor = contentProcessor; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public DmPageNavigationOrderService getDmPageNavigationOrderService() { return dmPageNavigationOrderService; }
    public void setDmPageNavigationOrderService(DmPageNavigationOrderService dmPageNavigationOrderService) { this.dmPageNavigationOrderService = dmPageNavigationOrderService; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public ActivityService getActivityService() { return activityService; }
    public void setActivityService(ActivityService activityService) { this.activityService = activityService; }

    public DmContentLifeCycleService getDmContentLifeCycleService() { return dmContentLifeCycleService; }
    public void setDmContentLifeCycleService(DmContentLifeCycleService dmContentLifeCycleService) { this.dmContentLifeCycleService = dmContentLifeCycleService; }

    public EventService getEventService() { return eventService; }
    public void setEventService(EventService eventService) { this.eventService = eventService; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public ContentItemIdGenerator getContentItemIdGenerator() { return contentItemIdGenerator; }
    public void setContentItemIdGenerator(ContentItemIdGenerator contentItemIdGenerator) { this.contentItemIdGenerator = contentItemIdGenerator; }

    public StudioConfiguration getStudioConfiguration() { return studioConfiguration; }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) { this.studioConfiguration = studioConfiguration; }
}