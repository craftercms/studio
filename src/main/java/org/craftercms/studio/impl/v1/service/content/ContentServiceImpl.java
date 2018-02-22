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
import org.craftercms.commons.lang.Callback;
import org.craftercms.commons.validation.annotations.param.ValidateIntegerParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.core.service.CacheService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.dal.ObjectMetadata;
import org.craftercms.studio.api.v1.dal.ObjectState;
import org.craftercms.studio.api.v1.ebus.EBusConstants;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.ebus.RepositoryEventMessage;
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
import org.craftercms.studio.api.v1.service.content.ContentItemIdGenerator;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.to.*;
import org.craftercms.studio.impl.v1.deployment.PreviewSync;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
import org.craftercms.studio.impl.v1.service.dependency.DmDependencyDiffService;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentItemOrderComparator;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;

import org.apache.commons.io.IOUtils;
import org.xml.sax.SAXException;
import reactor.core.Reactor;
import reactor.event.Event;

import javax.activation.MimetypesFileTypeMap;

/**
 * Content Services that other services may use
 * @author russdanner
 */
public class ContentServiceImpl implements ContentService {

    protected static final String MSG_ERROR_IO_CLOSE_FAILED = "err_io_closed_failed";

    private static final String COPY_DEP_XPATH = "//*/text()[normalize-space(.)='{copyDep}']/parent::*";
    private static final String COPY_DEP = "{copyDep}";

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    /**
     * file and foler name patterns for copied files and folders *
     */
    public final static Pattern COPY_FILE_PATTERN = Pattern.compile("(.+)-([0-9]+)\\.(.+)");
    public final static Pattern COPY_FOLDER_PATTERN = Pattern.compile("(.+)-([0-9]+)");

    @Override
    @ValidateParams
    public boolean contentExists(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, false);
        if (cacheService.hasScope(cacheContext)) {
            Object cacheKey = cacheTemplate.getKey(site, path);
            boolean cached = cacheService.hasKey(cacheContext, cacheKey);
            if (cached) {
                return cached;
            }
        }
        return this._contentRepository.contentExists(expandRelativeSitePath(site, path));
    }

    @Override
    @ValidateParams
    public boolean contentExists(@ValidateSecurePathParam(name = "fullPath") String fullPath) {
        return this._contentRepository.contentExists(fullPath);
    }

    @Override
    @ValidateParams
    public InputStream getContent(@ValidateSecurePathParam(name = "path") String path) throws ContentNotFoundException {
       return this._contentRepository.getContent(path);
    }

    @Override
    @ValidateParams
    public InputStream getContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) throws ContentNotFoundException {
       return this._contentRepository.getContent(expandRelativeSitePath(site, path));
    }

    @Override
    @ValidateParams
    public String getContentAsString(@ValidateSecurePathParam(name = "path") String path)  {
        String content = null;

        try {
            content = IOUtils.toString(_contentRepository.getContent(path));
        }
        catch(Exception err) {
            logger.error("Failed to get content as string for path {0}", path);
            logger.debug("Failed to get content as string for path {0}", err, path);
        }

        return content;
    }

    @Override
    @ValidateParams
    public Document getContentAsDocument(@ValidateSecurePathParam(name = "path") String path)
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
                try {
                    saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
                    saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
                    saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
                }catch (SAXException ex){
                    logger.error("Unable to turn off external entity loading, This could be a security risk.", ex);
                }
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
    @ValidateParams
    public boolean writeContent(@ValidateSecurePathParam(name = "path") String path, InputStream content) throws ServiceException {

       boolean writeSuccess = false;

        writeSuccess = _contentRepository.writeContent(path, content);

        try {
            _contentRepository.createVersion(path, false);
        }
        catch(Exception err) {
            // configurable weather or not to blow up the entire write?
            logger.error("Failed to create version for object at path: " + path, err);
        }
        removeItemFromCache(getSiteFromFullPath(path),getRelativeSitePath(path));
       return writeSuccess;
    }

    @Override
    @ValidateParams
    public void writeContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "fileName") String fileName, @ValidateStringParam(name = "contentType") String contentType, InputStream input,
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
        String relativePath = path;
        boolean contentExists = contentExists(site, path);
        String lockKey = id;
        if (contentExists) {
            lockKey = site + ":" + path;
        }
        generalLockService.lock(lockKey);
        try {
            boolean savaAndClose = (!StringUtils.isEmpty(unlock) && unlock.equalsIgnoreCase("false")) ? false : true;
            if (contentExists) {
                ObjectState objectState = objectStateService.getObjectState(site, path);
                if (objectState == null) {
                    ContentItemTO item = getContentItem(site, path, 0);
                    objectStateService.insertNewEntry(site, item);
                    objectState = objectStateService.getObjectState(site, path);
                }

                if(objectState != null) {

                    if (objectState.getSystemProcessing() != 0){
                        logger.error(String.format("Error Content %s is being processed (Object State is system processing);", fileName));
                        throw new RuntimeException(String.format("Content \"%s\" is being processed", fileName));
                    }

                    objectStateService.setSystemProcessing(site, path, true);
                }
                else {
                    logger.error("the object state is still null.");
                }
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
            if (contentExists) {
                objectStateService.setSystemProcessing(site, path, false);
            } else {
                objectStateService.setSystemProcessing(site, path, false);
            }
            String savedFileName = params.get(DmConstants.KEY_FILE_NAME);
            String savedPath = params.get(DmConstants.KEY_PATH);
            fullPath = expandRelativeSitePath(site, savedPath);
            if (!savedPath.endsWith(savedFileName)) {
                fullPath = fullPath + "/" + savedFileName;
            }
            fullPath = fullPath.replace("//", "/");
            relativePath = getRelativeSitePath(site, fullPath);
            ContentItemTO itemTo = getContentItem(site, relativePath, 0);
            if (itemTo != null) {
                if (savaAndClose) {
                    objectStateService.transition(site, itemTo, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE);
                } else {
                    objectStateService.transition(site, itemTo, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE_FOR_PREVIEW);
                }
                objectStateService.setSystemProcessing(site, itemTo.getUri(), false);
            } else {
                objectStateService.insertNewEntry(site, itemTo);
            }

            removeItemFromCache(site, relativePath);

            RepositoryEventMessage message = new RepositoryEventMessage();
            message.setSite(site);
            message.setPath(relativePath);
            String sessionTicket = securityProvider.getCurrentToken();
            RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
            message.setRepositoryEventContext(repositoryEventContext);
            previewSync.syncPath(site, relativePath, repositoryEventContext);
        }  catch (RuntimeException e) {
            logger.error("error writing content",e);
            objectStateService.setSystemProcessing(site, relativePath, false);
            objectStateService.setSystemProcessing(site, path, false);
            throw e;
        } finally {
            generalLockService.unlock(lockKey);
        }
    }

    @Override
    @ValidateParams
    public void writeContentAndRename(@ValidateStringParam(name = "site") final String site, @ValidateSecurePathParam(name = "path") final String path, @ValidateSecurePathParam(name = "targetPath") final String targetPath, @ValidateStringParam(name = "fileName") final String fileName, @ValidateStringParam(name = "contentType") final String contentType, final InputStream input,
                                      final String createFolders, final  String edit, final String unlock, final boolean createFolder) throws ServiceException {
        String id = site + ":" + path + ":" + fileName + ":" + contentType;
        if (!generalLockService.tryLock(id)) {
            generalLockService.lock(id);
            generalLockService.unlock(id);
            return;
        }
        try {
            writeContent(site, path, fileName, contentType, input, createFolders, edit, unlock);
            moveContent(site, path, targetPath);
        } catch (Throwable t) {
            logger.error("Error while executing write and rename: ", t);
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
    @ValidateParams
    public Map<String, Object> writeContentAsset(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "assetName") String assetName, InputStream in,
                                                 String isImage, String allowedWidth, String allowedHeight, String allowLessSize, String draft, String unlock, String systemAsset) throws ServiceException {
        if(assetName != null) {
            assetName = assetName.replace(" ","_");
        }

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

        String id = site + ":" + path + ":" + assetName + ":" + "";
        // processContent will close the input stream
        String fullPath = null;
        ContentItemTO item = null;
        try {
            fullPath = expandRelativeSitePath(site, path + "/" + assetName);
            item = getContentItem(site, path + "/" + assetName);

            if (item != null) {
                ObjectState itemState = objectStateService.getObjectState(site, path + "/" + assetName);
                if (itemState != null) {
                    if (itemState.getSystemProcessing() != 0) {
                        logger.error(String.format("Error Content %s is being processed (Object State is SYSTEM_PROCESSING);", assetName));
                        throw new RuntimeException(String.format("Content \"%s\" is being processed", assetName));
                    }
                    objectStateService.setSystemProcessing(site, path + "/" + assetName, true);
                }
            }
            ResultTO result = processContent(id, in, false, params, DmConstants.CONTENT_CHAIN_ASSET);
            ContentAssetInfoTO assetInfoTO = (ContentAssetInfoTO)result.getItem();
            if (isSystemAsset) {
                fullPath = fullPath.replace(assetName, assetInfoTO.getFileName());
            }
            item = getContentItem(site, getRelativeSitePath(site, fullPath));
            item.setSize(assetInfoTO.getSize());
            item.setSizeUnit(assetInfoTO.getSizeUnit());
            if (item != null) {
                objectStateService.transition(site, item, TransitionEvent.SAVE);
            }

            String relativePath = getRelativeSitePath(site, fullPath);
            removeItemFromCache(site, relativePath);
            RepositoryEventMessage message = new RepositoryEventMessage();
            message.setSite(site);
            message.setPath(relativePath);
            String sessionTicket = securityProvider.getCurrentToken();
            RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
            message.setRepositoryEventContext(repositoryEventContext);
            repositoryReactor.notify(EBusConstants.REPOSITORY_UPDATE_EVENT, Event.wrap(message));

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
                objectStateService.setSystemProcessing(site, getRelativeSitePath(site, fullPath), false);
            }
        }
    }

    @Override
    @ValidateParams
    public boolean writeContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, InputStream content) throws ServiceException {
        return writeContent(expandRelativeSitePath(site, path), content);
    }

    @Override
    @ValidateParams
    public boolean createFolder(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "name") String name) {
        boolean toRet = _contentRepository.createFolder(expandRelativeSitePath(site, path), name);
        removeItemFromCache(site, path + "/" + name);
        return toRet;
    }

    @Override
    @ValidateParams
    public boolean deleteContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "approver") String approver) {
        return deleteContent(site, path, true, approver);
    }

    @Override
    @ValidateParams
    public boolean deleteContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, boolean generateActivity, @ValidateStringParam(name = "approver") String approver) {
        if (generateActivity) {
            generateDeleteActivity(site, path, approver);
        }
        boolean toRet = _contentRepository.deleteContent(expandRelativeSitePath(site, path));
        objectStateService.deleteObjectStateForPath(site, path);
        objectMetadataManager.deleteObjectMetadata(site, path);
        try {
            dependencyService.deleteItemDependencies(site, path);
        } catch (ServiceException e) {
            logger.error("Error while deleting dependencies during delete content operation for site " + site, " path " + path, e);
        }

        removeItemFromCache(site, path);

        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);
        message.setPath(path);
        String sessionTicket = securityProvider.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
        message.setRepositoryEventContext(repositoryEventContext);
        repositoryReactor.notify(EBusConstants.REPOSITORY_DELETE_EVENT, Event.wrap(message));
        return toRet;
    }

    protected void generateDeleteActivity(String site, String path, String approver) {
        if (StringUtils.isEmpty(approver)) {
            approver = securityService.getCurrentUser();
        }
        boolean exists = contentExists(site, path);
        if (exists) {
            ObjectMetadata properties = objectMetadataManager.getProperties(site, path);
            String user = (properties != null && !StringUtils.isEmpty(properties.getSubmittedBy()) ? properties.getSubmittedBy() : approver);
            Map<String, String> extraInfo = new HashMap<String, String>();
            if (path.endsWith(DmConstants.XML_PATTERN)) {
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, getContentTypeClass(site, path));
            }
            logger.debug("[DELETE] posting delete activity on " + path + " by " + user + " in " + site);

            activityService.postActivity(site, user, path, ActivityService.ActivityType.DELETED, extraInfo);
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
    @ValidateParams
    public String copyContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "fromPath") String fromPath, @ValidateSecurePathParam(name = "toPath") String toPath) {
        return copyContent(site, fromPath, toPath, new HashSet<String>());
    }

    /** 
     * internal method copy that handles
     * Get dependencies is already recursive 
     */
    protected String copyContent(String site, String fromPath, String toPath, Set<String> processedPaths) {
        String retNewFileName = null;
        boolean opSuccess = false;

        String lifecycleOp = DmContentLifeCycleService.ContentLifeCycleOperation.COPY.toString();
        String user = securityService.getCurrentUser();
        String sessionTicket = securityProvider.getCurrentToken();
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
                    createFolder(site, toPath, copyFileName);
                    // copy was successful, return the new name
                    retNewFileName = copyPath;
                } else {
                    InputStream copyContent = null;
                    try {
                        String contentType = fromItem.getContentType();
                        InputStream fromContent = getContent(site, fromPath);
                        if (fromPath.endsWith(DmConstants.XML_PATTERN)) {
                            Document fromDocument = ContentUtils.convertStreamToXml(fromContent);
                            Map<String, String> fromPageIds = getContentIds(fromDocument);

                            logger.debug("copying file for site {0} from {1} to {2}, new name is {3}", site, fromPath, toPath, copyPath);

                            // come up with a new object ID and group ID for the object
                            Map<String, String> copyObjectIds = contentItemIdGenerator.getIds();

                            Map<String, String> copyDependencies = getCopyDependencies(site, fromPath, fromPath);
                            copyDependencies = getItemSpecificDependencies(fromDocument, copyDependencies);
                            logger.debug("Calculated copy dependencies: {0}, {1}", fromPath, copyDependencies);

                            // Duplicate the children
                            for (String dependecyKey : copyDependencies.keySet()) {
                                String dependecyPath = copyDependencies.get(dependecyKey);
                                String copyDepPath = dependecyPath;

                                // Does not seem to work (leaving it here because it's suspposed to do the work below)
                                //PathMacrosTransaltor.resolvePath(dependecyPath, copyObjectIds);
                                //copyDepPath = copyDepPath + "/" +  dependecyPath.substring(dependecyPath.lastIndexOf("/")+1);

                                // try a simple substitution
                                copyDepPath = copyDepPath.replaceAll(
                                        fromPageIds.get(DmConstants.KEY_PAGE_ID),
                                        copyObjectIds.get(DmConstants.KEY_PAGE_ID));

                                copyDepPath = copyDepPath.replaceAll(
                                        fromPageIds.get(DmConstants.KEY_PAGE_GROUP_ID),
                                        copyObjectIds.get(DmConstants.KEY_PAGE_GROUP_ID));

                                if (!copyDepPath.endsWith(DmConstants.XML_PATTERN)) {
                                    copyDepPath = ContentUtils.getParentUrl(copyDepPath);
                                }
                                logger.debug("TRANSLATED DEP PATH {0} to {1}", dependecyPath, copyDepPath);

                                String newCopyDepthPath = copyContent(site, dependecyPath, copyDepPath, processedPaths);
                                fromDocument = replaceCopyDependency(fromDocument, dependecyPath, newCopyDepthPath);
                            }

                            // update the file name / folder values
                            Document copyDocument = updateContentForCopy(site, fromDocument, copyPathFileName, copyPathFolder, copyObjectIds, copyPathModifier);

                            copyContent = ContentUtils.convertDocumentToStream(copyDocument, CStudioConstants.CONTENT_ENCODING);
                        }
                        // This code is very similar to what is in WRTIE CONTENT. Consolidate this code?
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
                                if (copyFileName.endsWith(DmConstants.XML_PATTERN)) {
                                    // when do you get here?
                                    processContent(id, copyContent, true, params, DmConstants.CONTENT_CHAIN_FORM);
                                } else {
                                    processContent(id, fromContent, false, params, DmConstants.CONTENT_CHAIN_ASSET);
                                }


                            // I THINK this will always return null
                            ObjectState objectState = objectStateService.getObjectState(site, copyPath);

                            if (objectState == null) {
                                ContentItemTO copyItem = getContentItem(site, copyPath, 0);
                                objectStateService.insertNewEntry(site, copyItem);
                                objectState = objectStateService.getObjectState(site, copyPath);

                                // Do I need to do this?
                                objectStateService.setSystemProcessing(site, copyPath, false);
                            }

                            // copy was successful, return the new name
                            retNewFileName = copyPath;

                            // Fire update events and preview sync
                            RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
                            RepositoryEventMessage message = new RepositoryEventMessage();

                            message.setSite(site);
                            message.setPath(copyPath);
                            message.setRepositoryEventContext(repositoryEventContext);

                            previewSync.syncPath(site, copyPath, repositoryEventContext);

                            // track that we already copied so we don't follow a circular dependency
                            processedPaths.add(copyPath);

                            // not sure why item would be in cache (its a new name)
                            removeItemFromCache(site, copyPath);
                        } finally {
                            generalLockService.unlock(id);
                        }
                    } catch (ContentNotFoundException eContentNotFound) {
                        logger.error("Content not found while copying content for site {0} from {1} to {2}, new name is {3}", eContentNotFound, site, fromPath, toPath, copyPath);
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
            logger.error("General Error while copying content for site {0} from {1} to {2}, new name is {3}", eServiceException, site, fromPath, toPath, copyPath);
        }

        return retNewFileName;
    }

    protected Document replaceCopyDependency(Document document, String depPath, String copyDepPath) {
        Element root = document.getRootElement();
        List<Node> includes = root.selectNodes(COPY_DEP_XPATH.replace(COPY_DEP, depPath));
        if (includes != null) {
            for(Node includeNode : includes) {
                includeNode.setText(includeNode.getText().replace(depPath, copyDepPath));
            }
        }
        return document;
    }

    protected Map<String, String> getCopyDependencies(String site, String sourceContentPath, String dependencyPath) throws ServiceException {
        Map<String, String> copyDependency = new HashMap<String, String>();
        if (sourceContentPath.endsWith(DmConstants.XML_PATTERN) && dependencyPath.endsWith(DmConstants.XML_PATTERN)) {
            String fullPath = expandRelativeSitePath(site, sourceContentPath);
            ContentItemTO dependencyItem = getContentItem(site, sourceContentPath);
            if (dependencyItem != null) {
                String contentType = dependencyItem.getContentType();
                List<CopyDependencyConfigTO> copyDependencyPatterns = servicesConfig.getCopyDependencyPatterns(site, contentType);
                if (copyDependencyPatterns != null && copyDependencyPatterns.size() > 0) {
                    logger.debug("Copy Pattern provided for contentType" + contentType);
                    Set<String> dependencies = dependencyService.getItemDependencies(site, dependencyPath, 1);
                    for (String dependency : dependencies) {
                        String assocFilePath = dependency;
                        for (CopyDependencyConfigTO copyConfig : copyDependencyPatterns) {
                            if (StringUtils.isNotEmpty(copyConfig.getPattern()) &&
                                    StringUtils.isNotEmpty(copyConfig.getTarget()) && assocFilePath.matches(copyConfig.getPattern())) {
                                ContentItemTO assocItem = getContentItem(site, assocFilePath);
                                if (assocItem != null) {
                                    copyDependency.put(dependency, dependency);
                                }
                            }
                        }
                    }

                } else {
                    logger.debug("Copy Pattern is not provided for contentType" + contentType);
                }
                Set<String> deps = dependencyService.getItemSpecificDependencies(site, dependencyPath, 1);
                for (String dep : deps) {
                    copyDependency.put(dep, dep);
                }
            } else {
                logger.debug("Not found dependency item at {0}", fullPath);
            }
        }
        return copyDependency;
    }

    @Override
    @ValidateParams
    public String moveContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "fromPath") String fromPath, @ValidateSecurePathParam(name = "toPath") String toPath) {
        String retNewFileName = null;
        boolean opSuccess = false;
        String movePath = null;

        try {
            String sourcePath = (fromPath.indexOf("index.xml") != -1) ? fromPath.substring(0, fromPath.lastIndexOf("/")) : fromPath;
            String sourcePathOnly = fromPath.substring(0, fromPath.lastIndexOf("/"));

            Map<String, String> movePathMap = constructNewPathforCutCopy(site, fromPath, toPath, true);
            movePath = movePathMap.get("FILE_PATH");
            String moveFileName = movePathMap.get("FILE_NAME");
            String movePathOnly = movePath.substring(0, movePath.lastIndexOf("/"));
            boolean moveAltFileName = "true".equals(movePathMap.get("ALT_NAME"));
            boolean targetIsIndex = "index.xml".equals(moveFileName);

            String targetPath = movePathOnly;
            if(movePathOnly.equals(sourcePathOnly)
            || (moveAltFileName == true && !targetIsIndex)) {
                // we never send index.xml to the repo, we move folders (and the folder has the rename)
                // SO otherwise, this is a rename and we need to forward the full path
                targetPath = movePath;  
            }
            
            logger.debug("Move file for site {0} from {1} to {2}, sourcePath {3} to target path {4}", site, fromPath, toPath, sourcePath, targetPath);

            // NOTE: IN WRITE SCENARIOS the repository OP IS PART of this PIPELINE, for some reason, historically with MOVE it is not
            opSuccess = _contentRepository.moveContent(
                expandRelativeSitePath(site, sourcePath),
                expandRelativeSitePath(site, targetPath));

            if(opSuccess) {
                // update database, preview, cache etc
                updateDatabaseCachePreviewForMove(site, fromPath, movePath, true);

                updateChildrenForMove(site, fromPath, movePath);
            }
            else {
                logger.error("Repository move failed site {0} from {1} to {2}", site, sourcePath, targetPath);
                movePath = fromPath;
            }
        }
        catch(ServiceException eMoveErr) {
            logger.error("Content not found while moving content for site {0} from {1} to {2}, new name is {3}", eMoveErr, site, fromPath, toPath, movePath);
        }

        return movePath;
    }

    @Override
    public String moveContent(String site, String fromPath, String toPath, String newName) {
        // Not sure why we need this method. Just a helper for a special case?
        return moveContent(site, fromPath, toPath+"/"+ newName);
    }

    protected void updateDatabaseCachePreviewForMove(String site, String fromPath, String movePath, boolean isMoveRoot) throws ServiceException {
        logger.debug("updateDatabaseCachePreviewForMove FROM {0} TO {1}  ", fromPath, movePath);

        String user = securityService.getCurrentUser();
        String sessionTicket = securityProvider.getCurrentToken();

        Map<String, String> params = new HashMap<>();
        params.put(DmConstants.KEY_SOURCE_PATH, fromPath);
        params.put(DmConstants.KEY_TARGET_PATH, movePath);
        params.put(DmConstants.KEY_SOURCE_FULL_PATH, expandRelativeSitePath(site, fromPath));
        params.put(DmConstants.KEY_TARGET_FULL_PATH, expandRelativeSitePath(site, movePath));

        // preRenameCleanWorkFlow(site, sourcePath);

        ContentItemTO renamedItem = getContentItem(site, movePath, 0);
        String contentType = renamedItem.getContentType();

        dmContentLifeCycleService.process(site, user, movePath, contentType,  DmContentLifeCycleService.ContentLifeCycleOperation.RENAME, params);

        //update nav order
        //updateContentWithNewNavOrder

        // remove old paths from cache
        removeItemFromCache(site, fromPath);

        // change the path of this object in the object state database
        objectStateService.updateObjectPath(site, fromPath, movePath);
        ContentItemTO movedTO = getContentItem(site, movePath, 0);  
        objectStateService.transition(site, movedTO, org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE);

        // update metadata
        if (!objectMetadataManager.isRenamed(site, fromPath)) {
            // if an item was previously moved, we do not track intermediate moves because it will
            // ultimately orphan deployed content.  Old Path is always the OLDEST DEPLOYED PATH
            ObjectMetadata metadata = objectMetadataManager.getProperties(site, fromPath);
            if(metadata == null) {
                objectMetadataManager.insertNewObjectMetadata(site, fromPath);
                metadata = objectMetadataManager.getProperties(site, fromPath);
            }

            if (!movedTO.isNew()) {
                // if the item is not new, we need to track the old URL for deployment
                logger.debug("item is not new, and has not previously been moved. Track the old URL {0}", fromPath);
                Map<String, Object> objMetadataProps = new HashMap<String, Object>();
                objMetadataProps.put(ObjectMetadata.PROP_RENAMED, 1);
                objMetadataProps.put(ObjectMetadata.PROP_OLD_URL, fromPath);
                objectMetadataManager.setObjectMetadata(site, fromPath, objMetadataProps);
            }
        }

        objectMetadataManager.updateObjectPath(site, fromPath, movePath);

        // WHAT IS THIS PROPERTY USED FOR?
        //objMetadataProps = new HashMap<String, Object>();
        //objMetadataProps.put(ObjectMetadata.PROP_DELETE_URL, true);
        //objectMetadataManager.setObjectMetadata(site, movePath, objMetadataProps);

        // write activity stream
        activityService.renameContentId(site, fromPath, movePath);

        Map<String, String> activityInfo = new HashMap<String, String>();
        String contentClass = getContentTypeClass(site, movePath);
        
        if(movePath.endsWith(DmConstants.XML_PATTERN)) {
            activityInfo.put(DmConstants.KEY_CONTENT_TYPE, contentClass);
        }

        activityService.postActivity(
            site, 
            user, 
            movePath, 
            ActivityService.ActivityType.UPDATED, 
            activityInfo);

        updateDependenciesOnMove(site, fromPath, movePath);

        // we added this check because deployer is asking content service for old content
        // which of course will NOT be there, becasue it has been moved in the repo
        if(isMoveRoot) {
            // fire events and sync preview if this is the top level object
            // child objects are moved in deployment as a sideffect of moving the parent
            RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
            RepositoryEventMessage message = new RepositoryEventMessage();

            // sync ops work on folder paths 
            message.setSite(site);
            message.setPath(movePath.replace("/index.xml", ""));
            message.setOldPath(fromPath.replace("/index.xml", ""));
            message.setRepositoryEventContext(repositoryEventContext);

            repositoryReactor.notify(EBusConstants.REPOSITORY_MOVE_EVENT, Event.wrap(message));
        }
    }

    protected void updateDependenciesOnMove(String site, String fromPath, String movePath) throws ServiceException {
        dependencyService.moveDependencies(site, fromPath, movePath);
        try {
            dependencyService.upsertDependencies(site, movePath);
        } catch (ServiceException  e) {
            logger.error("Error while updating dependencies on move content site: " + site + " path: " + movePath, e);
        }
    }

    protected void updateChildrenForMove(String site, String fromPath, String movePath) throws ServiceException {
        logger.debug("updateChildObjectStateForMove HANDLING {0}, {1}", fromPath, movePath);


        // get the list of children
        ContentItemTO movedTO = getContentItem(site, movePath, 2);
        List<ContentItemTO> childrenTOs = movedTO.getChildren();

        for(ContentItemTO childTO : childrenTOs) {
            // calculate the childs from path by looking at it's parent's from path and the child new path
            // (parent move operation has already happened)
            String childToPath = childTO.getUri();
            
            String oldParentFolderPath = fromPath.replace("/index.xml", "");
            String parentFolderPath = movePath.replace("/index.xml", "");

            String childFromPath = childToPath.replace(parentFolderPath, oldParentFolderPath);

            logger.debug("updateChildObjectStateForMove HANDLING CHILD FROM: {0} TO: {1}  ", childFromPath, childToPath);

            // update database, preview, cache etc
            updateDatabaseCachePreviewForMove(site, childFromPath, childToPath, false);
                
            // handle this child's children
            updateChildrenForMove(site, childFromPath, childToPath);
        }
    }    

    protected Map<String, String> constructNewPathforCutCopy(String site, String fromPath, String toPath, boolean adjustOnCollide) 
    throws ServiceException {
         Map<String, String> result = new HashMap<String, String>(); 
        
        // The following rules apply to content under the site folder
        String fromPathOnly = fromPath.substring(0, fromPath.lastIndexOf("/"));
        String fromFileNameOnly = fromPath.substring(fromPath.lastIndexOf("/")+1);
        boolean fromFileIsIndex = ("index.xml".equals(fromFileNameOnly));
        logger.debug("cut/copy name rules FROM: {0}, {1}", fromPathOnly, fromFileNameOnly);

        if(fromFileIsIndex==true) {
            fromFileNameOnly = fromPathOnly.substring(fromPathOnly.lastIndexOf("/")+1);
            fromPathOnly = fromPathOnly.substring(0, fromPathOnly.lastIndexOf("/"));
            logger.debug("cut/copy name rules INDEX FROM: {0}, {1}", fromPathOnly, fromFileNameOnly);
        }

        String newPathOnly = (toPath.contains(".xml")) ? toPath.substring(0, toPath.lastIndexOf("/")) : toPath;
        String newFileNameOnly = (toPath.contains(".xml")) ? toPath.substring(toPath.lastIndexOf("/")+1) : fromFileNameOnly;
        boolean newFileIsIndex = ("index.xml".equals(newFileNameOnly));
        logger.debug("cut/copy name rules TO: {0}, {1}", newPathOnly, newFileNameOnly);

        if(newFileIsIndex==true) {
            newFileNameOnly = newPathOnly.substring(newPathOnly.lastIndexOf("/")+1);
            newPathOnly = newPathOnly.substring(0, newPathOnly.lastIndexOf("/"));
            logger.debug("cut/copy name rules INDEX TO: {0}, {1}", newPathOnly, newFileNameOnly);
        }

        String proposedDestPath = null;
        String proposedDestPath_filename = null;
        String proposedDestPath_folder = null;
        boolean targetPathExistsPriorToOp = false;

        try {
           targetPathExistsPriorToOp = contentExists(site, toPath);
        }
        catch(Exception contentExistsErr) {
            // what can cause this error?  
            // can't talk to the repository?
            // swallow it for now, the error will come when we try a write
        }

        if(fromFileIsIndex && newFileIsIndex) {
            // Example MOVE LOCATION, INDEX FILES
            // fromPath: "/site/website/search/index.xml"
            // toPath:   "/site/website/products/index.xml"
            // newPath:  "/site/website/products/search/index.xml"             
            //
            // Example RENAME, INDEX FILES
            // fromPath: "/site/website/en/services/index.xml"
            // toPath:   "site/website/en/services-updated/index.xml" 
            // newPath:  "site/website/en/services-updated/index.xml 
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

        try {
           if(adjustOnCollide == true) {
               // if adjustOnCollide is true we need to check, otherwise we dont
               contentExists = contentExists(site, proposedDestPath);
           }
        }
        catch(Exception contentExistsErr) {
            // what can cause this error?  
            // can't talk to the repository?
            // swallow it for now, the error will come when we try a write
        }

        if(adjustOnCollide && contentExists) {
            logger.debug("File already found at path {0}, creating new name", proposedDestPath);
            try {
                Map<String,String> ids = contentItemIdGenerator.getIds(); 
                String id = ids.get(DmConstants.KEY_PAGE_GROUP_ID);

                //proposedDestPath = getNextAvailableName(site, proposedDestPath);

                if(proposedDestPath.indexOf("/index.xml") == -1) {
                    proposedDestPath = 
                        proposedDestPath.substring(0, proposedDestPath.lastIndexOf(".")) + "-" + id +
                        proposedDestPath.substring(proposedDestPath.lastIndexOf("."));

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

        logger.debug("FINAL PROPOSED PATH from {0} to {1} FINAL {2}", fromPath, toPath, proposedDestPath);
        return result;
    }

    protected Map<String, String> getItemSpecificDependencies(Document document, Map<String, String> copyDependencies) {
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

    protected Document updateContentForCopy(String site, Document document, String filename, String folder, Map<String, String> params, String modifier) 
    throws ServiceException {
        
        //update pageId and groupId with the new one
        Element root = document.getRootElement();
        String originalPageId = null;
        String originalGroupId = null;

        Node filenameNode = root.selectSingleNode("//" + DmXmlConstants.ELM_FILE_NAME);
        if (filenameNode != null) {
            ((Element)filenameNode).setText(filename);
        }

        Node folderNode = root.selectSingleNode("//" + DmXmlConstants.ELM_FOLDER_NAME);
        if (folderNode != null) {
            ((Element)folderNode).setText(folder);
        }


        Node pageIdNode = root.selectSingleNode("//" + DmXmlConstants.ELM_PAGE_ID);
        if (pageIdNode != null) {
            originalPageId = ((Element)pageIdNode).getText();
            ((Element)pageIdNode).setText(params.get(DmConstants.KEY_PAGE_ID));
        }

        if(modifier != null) {
            Node internalNameNode = root.selectSingleNode("//" + DmXmlConstants.ELM_INTERNAL_NAME);
            if (internalNameNode != null) {
                String internalNameValue = ((Element)internalNameNode).getText();
                ((Element)internalNameNode).setText(internalNameValue + " " + modifier);
            }
        }

        Node groupIdNode = root.selectSingleNode("//" + DmXmlConstants.ELM_GROUP_ID);
        if (groupIdNode != null) {
            originalGroupId = ((Element)groupIdNode).getText();
            ((Element)groupIdNode).setText(params.get(DmConstants.KEY_PAGE_GROUP_ID));
        }

        List<Node> keys = root.selectNodes("//key");
        if (keys != null) {
            for(Node keyNode : keys) {
                String keyValue = ((Element)keyNode).getText();
                keyValue = keyValue.replaceAll(originalPageId, params.get(DmConstants.KEY_PAGE_ID));
                keyValue = keyValue.replaceAll(originalGroupId, params.get(DmConstants.KEY_PAGE_GROUP_ID));

                if(keyValue.contains("/page")) {
                    ((Element)keyNode).setText(keyValue);
                }
            }
        }

        List<Node> includes = root.selectNodes("//include");
        if (includes != null) {
            for(Node includeNode : includes) {
                String includeValue = ((Element)includeNode).getText();
                includeValue = includeValue.replaceAll(originalPageId, params.get(DmConstants.KEY_PAGE_ID));
                includeValue = includeValue.replaceAll(originalGroupId, params.get(DmConstants.KEY_PAGE_GROUP_ID));

                if(includeValue.contains("/page")) {
                    ((Element)includeNode).setText(includeValue);
                }
            }
        }

        return document;
    }


    protected ContentItemTO createNewContentItemTO(String site, String contentPath) {
        ContentItemTO item = new ContentItemTO();
        contentPath = contentPath.replace("//", "/");

        item.uri = contentPath;
        item.path = contentPath.substring(0, contentPath.lastIndexOf("/"));
        item.name = contentPath.substring(contentPath.lastIndexOf("/") + 1);

        item.asset = true;
        item.site = site;
        item.internalName = item.name;
        item.contentType = "asset";
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
        item.folder = (item.name.contains(".")==false);

        return item;
    }

    protected ContentItemTO populateContentDrivenProperties(String site,ContentItemTO item)
    throws Exception {

        String fullContentPath = expandRelativeSitePath(item.site, item.uri);
        String contentPath = item.uri;

        logger.debug("Pupulating page props {0}", contentPath);
        item.setLevelDescriptor(item.name.equals(servicesConfig.getLevelDescriptorName(site)));
        item.page = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getPagePatterns(site));
        item.isPage = item.page;
        item.previewable = item.page;
        item.isPreviewable = item.previewable;
        item.component = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)) || item.isLevelDescriptor();
        item.isComponent = item.component;
        item.asset = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getAssetPatterns(site));
        item.isAsset = item.asset;
        item.document = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site));
        item.isDocument = item.document;

        item.uri = contentPath;
        item.path = contentPath.substring(0, contentPath.lastIndexOf("/"));
        item.name = contentPath.substring(contentPath.lastIndexOf("/")+1);
        item.browserUri = contentPath;
        
        if(item.page) {
            item.browserUri = contentPath.replace("/site/website", "").replace("/index.xml", "");
        }

        Document contentDoc = this.getContentAsDocument(fullContentPath);
        if(contentDoc != null) {
            Element rootElement = contentDoc.getRootElement();
            
            String internalName = rootElement.valueOf("internal-name");
            String contentType = rootElement.valueOf("content-type");
            String disabled = rootElement.valueOf("disabled");
            String savedAsDraft = rootElement.valueOf("savedAsDraft");
            String floating = rootElement.valueOf("placeInNav");
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
                template.name = "DEFAULT";

                item.renderingTemplates.add(template);
            }
        }
        else {
             logger.error("no xml document could be loaded for path {0}", fullContentPath);
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
        String fullContentPath = expandRelativeSitePath(item.site, item.uri);
        String contentPath = item.uri;

        item.children = new ArrayList<ContentItemTO>();
        item.numOfChildren = 0;

        if(contentPath.indexOf("/index.xml") != -1 
        || contentPath.indexOf(".") == -1 ) { // item.isFolder?

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
                            String childPath = getRelativeSitePath(item.site, childRepoItems[j].path + "/" + childRepoItems[j].name);
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
    @ValidateParams
    public ContentItemTO getContentItem(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        return getContentItem(site, path, 2);
    }


    @Override
    @ValidateParams
    public ContentItemTO getContentItem(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateIntegerParam(name = "depth", minValue = 0) int depth) {
        logger.debug("Loading content item ... should be cached {0}, {1}, {2}", site, path, depth);
        ContentItemTO item = null;
        String fullContentPath = expandRelativeSitePath(site, path);
        String contentPath = path;
        logger.debug("Getting content item for {0}", contentPath);

        long startTime = System.currentTimeMillis();

        try {
            if (contentExists(site, path)) {
                // get item from cache
                item = getCachedContentItem(site, path);

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
            logger.error("error constructing item for object at path '{0}'", err, fullContentPath);
        }
        
        long executionTime = System.currentTimeMillis() - startTime;
        logger.debug("Content item [{0}] retrieved in {1} milis", fullContentPath, executionTime);
        return item;
    }

    protected ContentItemTO getCachedContentItem(final String site, final String path) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, false);
        generalLockService.lock(cacheContext.getId());
        try {
            if (!cacheService.hasScope(cacheContext)) {
                cacheService.addScope(cacheContext);
            }
        } finally {
            generalLockService.unlock(cacheContext.getId());
        }
        ContentItemTO item = cacheTemplate.getObject(cacheContext, new Callback<ContentItemTO>() {
            @Override
            public ContentItemTO execute() {
                return loadContentItem(site, path);
            }
        }, site, path);
        ContentItemTO toRet = new ContentItemTO(item);
        return toRet;
    }

    protected void removeItemFromCache(String site, String path) {
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, false);
        Object cacheKey = cacheTemplate.getKey(site, path);
        generalLockService.lock(cacheContext.getId());
        try {
            if (!cacheService.hasScope(cacheContext)) {
                cacheService.addScope(cacheContext);
            }
        } finally {
            generalLockService.unlock(cacheContext.getId());
        }
        cacheService.remove(cacheContext, cacheKey);
    }

    protected ContentItemTO loadContentItem(String site, String path) {
        ContentItemTO item = createNewContentItemTO(site, path);

        if (item.uri.endsWith(".xml")) {

            try {
                item = populateContentDrivenProperties(site, item);
            } catch (Exception err) {
                logger.error("error constructing item for object at path '{0}'", err, expandRelativeSitePath(site, path));
            }
        } else {
            item.setLevelDescriptor(item.name.equals(servicesConfig.getLevelDescriptorName(site)));
            item.page = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getPagePatterns(site));
            item.isPage = item.page;
            item.previewable = item.page;
            item.isPreviewable = item.previewable;
            item.asset = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getAssetPatterns(site));
            item.isAsset = item.asset;
            item.component = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)) || item.isLevelDescriptor() || item.asset || ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getRenderingTemplatePatterns(site));
            item.isComponent = item.component;
            item.document = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site));
            item.isDocument = item.document;
            item.browserUri =item.getUri();
        }

        loadContentTypeProperties(site, item, item.contentType);
        return item;
    }

    @Override
    @ValidateParams
    public ContentItemTO getContentItem(@ValidateSecurePathParam(name = "fullPath") String fullPath) {
        String site = getSiteFromFullPath(fullPath);
        String relativePath = getRelativeSitePath(site, fullPath);

        return getContentItem(site, fullPath);
    }

    protected void loadContentTypeProperties(String site, ContentItemTO item, String contentType) {
        if(contentType != null && !contentType.equals("folder") && !contentType.equals("asset")) {
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
                item.setPreviewable(ContentUtils.matchesPatterns(mimeType, servicesConfig.getPreviewableMimetypesPaterns(site)));
                item.isPreviewable = item.previewable;
            }
        }
        // TODO CodeRev:but what if the config is null?
    }

    protected void populateWorkflowProperties(String site, ContentItemTO item) {
        ObjectState state = objectStateService.getObjectState(site, item.getUri(), false);
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
        ObjectMetadata metadata = objectMetadataManager.getProperties(site, item.getUri());
        if (metadata != null) {
            if (StringUtils.isEmpty(metadata.getLockOwner())) {
                item.setLockOwner("");
            } else {
                item.setLockOwner(metadata.getLockOwner());
            }
            if (metadata.getLaunchDate() != null) {
                item.scheduledDate = metadata.getLaunchDate();
                item.setScheduledDate(metadata.getLaunchDate());
            }
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
    @ValidateParams
    public ContentItemTO getContentItemTree(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateIntegerParam(name = "depth") int depth) {
        logger.debug("Getting content item  tree for {0}:{1} depth {2}", site, path, depth);

        long startTime = System.currentTimeMillis();
        boolean isPages = (path.contains("/site/website"));
        ContentItemTO root = null;

        if (isPages && contentExists(site, path + "/index.xml")) {
            root = getContentItem(site, path+"/index.xml",depth);
        }
        else {
            root = getContentItem(site, path,depth);
        }

        long executionTime = System.currentTimeMillis() - startTime;
        logger.debug("Content item tree [{0}:{1} depth {2}] retrieved in {3} milis", site, path, depth, executionTime);

        return root;
    }

    @Override
    @ValidateParams
    public ContentItemTO getContentItemTree(@ValidateSecurePathParam(name = "fullPath") String fullPath, @ValidateIntegerParam(name = "depth") int depth) {
        String site = getSiteFromFullPath(fullPath);
        String relativePath = getRelativeSitePath(site, fullPath);

        return getContentItemTree(site, relativePath, depth);
    }

    @Override
    @ValidateParams
    public VersionTO[] getContentItemVersionHistory(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        return _contentRepository.getContentVersionHistory(expandRelativeSitePath(site, path));
    }

    @Override
    @ValidateParams
    public boolean revertContentItem(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "version") String version, boolean major, @ValidateStringParam(name = "comment") String comment) {
        boolean success = false;

        if(path.startsWith("/site")) {
            success = revertXmlContentItem(site, path, version, major, comment);
        }
        else {
            success = _contentRepository.revertContent(expandRelativeSitePath(site, path), version, major, comment);

            ContentItemTO item = getContentItem(site, path);
            objectStateService.transition(site, item, TransitionEvent.REVERT);

            removeItemFromCache(site, path);

            RepositoryEventMessage message = new RepositoryEventMessage();
            message.setSite(site);
            message.setPath(path);
            String sessionTicket = securityProvider.getCurrentToken();
            RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
            message.setRepositoryEventContext(repositoryEventContext);
            repositoryReactor.notify(EBusConstants.REPOSITORY_UPDATE_EVENT, Event.wrap(message));
        }
        return success;
    }

    protected boolean revertXmlContentItem(String site, String path, String version, boolean major, String comment) {
        boolean success = false;

        try {
            String lifecycleOp = DmContentLifeCycleService.ContentLifeCycleOperation.REVERT.toString();
            String user = securityService.getCurrentUser();
            String sessionTicket = securityProvider.getCurrentToken();

            String contentType = null;
            String fileName = path.substring(path.lastIndexOf("/")+1);
            String pathOnly = path.substring(0, path.lastIndexOf("/"));
            String folderOnly = pathOnly.substring(pathOnly.lastIndexOf("/")+1);
            boolean isIndex = ("index.xml".equals(fileName));

            InputStream olderContentStream = getContentVersion(site, path, version);

            // get older values and modify document (folder name may have changed due to a move)
            Document olderContentDocument = ContentUtils.convertStreamToXml(olderContentStream);
            Element root = olderContentDocument.getRootElement();

            Node contentTypeNode = root.selectSingleNode("//" + DmXmlConstants.ELM_CONTENT_TYPE);
            if (contentTypeNode != null) {
                contentType = ((Element)contentTypeNode).getText();
            }

            Node folderNode = root.selectSingleNode("//" + DmXmlConstants.ELM_FOLDER_NAME);
            if (folderNode != null) {
                ((Element)folderNode).setText(folderOnly);
            }

            InputStream newVersionContent = ContentUtils.convertDocumentToStream(olderContentDocument, CStudioConstants.CONTENT_ENCODING);

            // This code is very similar to what is in WRTIE CONTENT. Consolidate this code?
            Map<String, String> params = new HashMap<String, String>();
            params.put(DmConstants.KEY_SITE, site);
            params.put(DmConstants.KEY_PATH, pathOnly);
            params.put(DmConstants.KEY_FILE_NAME, fileName);
            params.put(DmConstants.KEY_USER, user);
            params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
            params.put(DmConstants.KEY_CREATE_FOLDERS, "true");
            params.put(DmConstants.KEY_EDIT, "true");
            params.put(DmConstants.KEY_ACTIVITY_TYPE, "false");
            params.put(DmConstants.KEY_SKIP_CLEAN_PREVIEW, "true");
            params.put(DmConstants.KEY_COPIED_CONTENT, "false");
            params.put(DmConstants.CONTENT_LIFECYCLE_OPERATION, lifecycleOp);

            String id = site + ":" + pathOnly + ":" + fileName + ":" + contentType;

            try {
                generalLockService.lock(id);

                processContent(id, newVersionContent, true, params, DmConstants.CONTENT_CHAIN_FORM);

                String versionComment = "Reverted to content from version "+ version;
                
                String fullPath =expandRelativeSitePath(site, path);
                _contentRepository.createVersion(fullPath, versionComment, major);

                ObjectState objectState = objectStateService.getObjectState(site, path);

                ContentItemTO versionItem = getContentItem(site, path, 0);
                if (objectState == null) {
                    objectStateService.insertNewEntry(site, versionItem);
                    objectState = objectStateService.getObjectState(site, path);
                }

                objectStateService.transition(site, versionItem, TransitionEvent.REVERT);
                objectStateService.setSystemProcessing(site, path, false);

                // Fire update events and preview sync
                RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
                RepositoryEventMessage message = new RepositoryEventMessage();

                message.setSite(site);
                message.setPath(path);
                message.setRepositoryEventContext(repositoryEventContext);

                previewSync.syncPath(site, path, repositoryEventContext);

                // not sure why item would be in cache (its a new name)
                removeItemFromCache(site, path);

                success = true;
            }
            finally {
                generalLockService.unlock(id);
            }
        }
        catch(DocumentException parseErr) {
           logger.error("Unable to revert content, parse error on document for item at path {0} with version id {1}.", parseErr, path, version); 
        }
        catch(ContentNotFoundException verNotFoundErr) {
           logger.error("Unable to revert content, item at path {0} with version id {1} not found.", path, version);
        }
        catch(ServiceException err) {
           logger.error("Exception to revert content, write failed for item at path {0} with version id {1}", err, path, version);            
        }

        return success;
    }

	/**
     * return the content for a given version
     *
     * @param site    - the project ID
     * @param path    - the path item
     * @param version - version
     */
	@Override
	@ValidateParams
 	public InputStream getContentVersion(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "version") String version)
 	throws ContentNotFoundException {
 		String repositoryPath = expandRelativeSitePath(site, path);
 		
 		return _contentRepository.getContentVersion(repositoryPath, version);
 	}

	/**
     * return the content for a given version
     *
     * @param site    - the project ID
     * @param path    - the path item
     * @param version - version
     */
	@Override
	@ValidateParams
 	public String getContentVersionAsString(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateStringParam(name = "path") String version)
	throws ContentNotFoundException { 		
 		String content = null;

        try {
            content = IOUtils.toString(getContentVersion(site, path, version));
        }
        catch(Exception err) {
            logger.error("Failed to get content as string for path {0}", path);
            logger.debug("Failed to get content as string for path {0}", err, path);
        }

        return content;
 	}

 	@Override
    @ValidateParams
    public ContentItemTO createDummyDmContentItemForDeletedNode(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "relativePath") String relativePath){
        String absolutePath = expandRelativeSitePath(site, relativePath);
        DmPathTO path = new DmPathTO(absolutePath);
        ContentItemTO item = new ContentItemTO();
        String timeZone = servicesConfig.getDefaultTimezone(site);
        item.timezone = timeZone;
        String name = path.getName();
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


    /**
     * take a path like /sites/website/index.xml and root it properly with a fully expanded repo path
     *
     * @param site
     * @param relativePath
     * @return
     */
    @Override
    @ValidateParams
    public String expandRelativeSitePath(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "relativePath") String relativePath) {
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
    @ValidateParams
    public String getRelativeSitePath(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "fullPath") String fullPath) {
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
    @ValidateParams
    public String getContentTypeClass(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "uri") String uri) {
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
        moveContent(site, path, targetPath);
    }

    @Override
    @ValidateParams
    public ResultTO processContent(@ValidateStringParam(name = "id") String id, InputStream input, boolean isXml, Map<String, String> params, String contentChainForm) throws ServiceException {
        // get sandbox if not provided
        long start = System.currentTimeMillis();
        try {

            String[] strings = id.split(":");
            final String path = strings[1];
            final String site = strings[0];
            long startTime = System.currentTimeMillis();
            ResultTO to = contentProcessor.processContent(id, input, isXml, params, contentChainForm);
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("Write Duration: {0}", duration);
            return to;
        } finally {
            long end = System.currentTimeMillis();
            logger.debug("Write complete for [" + id + "] in time [" + (end - start) + "]");
        }
    }

    @Override
    @ValidateParams
    public String getNextAvailableName(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
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
    @ValidateParams
    public GoLiveDeleteCandidates getDeleteCandidates(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "relativePath") String relativePath) throws ServiceException {
        List<String> items = new ArrayList<>();
        ContentItemTO contentItem = getContentItem(site, relativePath);
        GoLiveDeleteCandidates deletedItems = new GoLiveDeleteCandidates(site, this, objectStateService);
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

        if (contentItem.isFolder()) {
            contentItem = getContentItemTree(site, contentItem.getUri(), 2);
            if (contentItem.getChildren() != null && contentItem.getNumOfChildren() > 0) {
                for (ContentItemTO child : contentItem.getChildren()) {
                    childDeleteItems(site, child, items);
                }
            }
        } else {
            addDependenciesToDelete(site, contentItem.getUri(), contentItem.getUri(), items,false);
            addRemovedDependenicesToDelete(site, contentItem.getUri(), items);
        }
        //add the child path
        items.getPaths().add(contentItem.getUri());
    }

    protected void addDependenciesToDelete(String site, String sourceContentPath, String dependencyPath, GoLiveDeleteCandidates candidates,boolean isLiveRepo) throws ServiceException {
        Set<String> dependencyParentFolder = new HashSet<String>();
        //add dependencies as well
        Set<DmDependencyTO> dmDependencyTOs = getDeleteDependencies(site, sourceContentPath, dependencyPath,isLiveRepo);
        for (DmDependencyTO dependency : dmDependencyTOs) {
            if (candidates.addDependency(dependency.getUri())) {
                logger.debug("Added to delete" + dependency.getUri());
                if (dependency.isDeleteEmptyParentFolder()) {
                    dependencyParentFolder.add(ContentUtils.getParentUrl(dependency.getUri()));
                }
            }
            addDependenciesToDelete(site, sourceContentPath, dependency.getUri(), candidates,false); //recursively add dependencies of the dependency
        }

        //Find if any folder would get empty if remove the items and add just the folder
        for (String parentFolderToDelete : dependencyParentFolder) {
            RepositoryItem[] children = _contentRepository.getContentChildren(expandRelativeSitePath(site, parentFolderToDelete));
            List<String> childItems = new ArrayList<String>();
            for (RepositoryItem child : children) {
                childItems.add(getRelativeSitePath(site, child.path + "/" + child.name));
            }
            if (candidates.getAllItems().containsAll(childItems)) {
                logger.debug("Added parentFolder for delete" + parentFolderToDelete);
                candidates.addDependencyParentFolder(parentFolderToDelete);
            }
        }
    }

    protected Set<DmDependencyTO> getDeleteDependencies(String site, String sourceContentPath, String dependencyPath, boolean isLiveRepo) throws ServiceException {
        Set<DmDependencyTO> toRet = new HashSet<DmDependencyTO>();
        if(sourceContentPath.endsWith(DmConstants.XML_PATTERN) && dependencyPath.endsWith(DmConstants.XML_PATTERN)){
            List<DeleteDependencyConfigTO> deleteAssociations = getDeletePatternConfig(site, sourceContentPath,isLiveRepo);
            Set<String> dependencies = dependencyService.getItemDependencies(site, dependencyPath, 1);
            for (String dependency : dependencies) {
                for (DeleteDependencyConfigTO deleteAssoc : deleteAssociations) {
                    if (dependency.matches(deleteAssoc.getPattern())) {
                        if (contentExists(site, dependencyPath)) {
                            DmDependencyTO depTo = new DmDependencyTO();
                            depTo.setUri(dependency);
                            depTo.setDeleteEmptyParentFolder(deleteAssoc.isRemoveEmptyFolder());
                            toRet.add(depTo);
                        }
                    }
                }
            }
        }

        return toRet;
    }

    protected Set<DmDependencyTO> getDeleteDependencies(String site, String sourceContentPath,String dependencyPath) throws ServiceException {
        return getDeleteDependencies(site, sourceContentPath, dependencyPath, false);
    }

    protected void addRemovedDependenicesToDelete(String site, String relativePath, GoLiveDeleteCandidates candidates) throws ServiceException {
        if (relativePath.endsWith(DmConstants.XML_PATTERN) && !objectStateService.isNew(site, relativePath)) {
            DmDependencyDiffService.DiffRequest diffRequest = new DmDependencyDiffService.DiffRequest(site, relativePath, null, null, site, true);
            List<String> deleted = getRemovedDependenices(diffRequest, true);
            logger.debug("Removed dependenices for path[" + relativePath + "] : " + deleted);
            for (String dependency : deleted) {
                String dependencyFullPath = expandRelativeSitePath(site, dependency);
                candidates.getLiveDependencyItems().add(dependencyFullPath);
            }
        }
    }

    protected List<String> getRemovedDependenices(DmDependencyDiffService.DiffRequest diffRequest,
                                        boolean matchDeletePattern) throws ServiceException {
        DmDependencyDiffService.DiffResponse diffResponse = dmDependencyDiffService.diff(diffRequest);
        List<String> removedDep = diffResponse.getRemovedDependenices();
        if(matchDeletePattern){
            removedDep = filterDependenicesMatchingDeletePattern(diffRequest.getSite(), diffRequest.getSourcePath(),diffResponse.getRemovedDependenices());
        }
        return removedDep;
    }

    protected List<String> filterDependenicesMatchingDeletePattern(String site, String sourcePath, List<String> dependencies) throws ServiceException{
        List<String> matchingDep = new ArrayList<String>();
        if(sourcePath.endsWith(DmConstants.XML_PATTERN) && sourcePath.endsWith(DmConstants.XML_PATTERN)){
            List<DeleteDependencyConfigTO> deleteAssociations = getDeletePatternConfig(site,sourcePath);
            if (deleteAssociations != null && deleteAssociations.size() > 0) {
                for(String dependency:dependencies){
                    for (DeleteDependencyConfigTO deleteAssoc : deleteAssociations) {
                        if (dependency.matches(deleteAssoc.getPattern())) {
                            matchingDep.add(dependency);
                        }
                    }
                }
            }
        }
        return matchingDep;
    }

    protected List<DeleteDependencyConfigTO> getDeletePatternConfig(String site, String relativePath,boolean isInLiveRepo) throws ServiceException{
        List<DeleteDependencyConfigTO> deleteAssociations  = new ArrayList<DeleteDependencyConfigTO>();
        ContentItemTO dependencyItem = getContentItem(site, relativePath, 0);
        String contentType = dependencyItem.getContentType();
        deleteAssociations  = servicesConfig.getDeleteDependencyPatterns(site, contentType);
        return deleteAssociations;
    }

    protected List<DeleteDependencyConfigTO> getDeletePatternConfig(String site, String relativePath) throws ServiceException{
        return getDeletePatternConfig(site,relativePath,false);
    }

    @Override
    @ValidateParams
    public void lockContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        _contentRepository.lockItem(site, path);
        objectMetadataManager.lockContent(site, path, securityService.getCurrentUser());
    }

    @Override
    @ValidateParams
    public void unLockContent(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) {
        ContentItemTO item = getContentItem(site, path, 0);
        objectStateService.transition(site, item, TransitionEvent.CANCEL_EDIT);
        _contentRepository.unLockItem(site, path);
        objectMetadataManager.unLockContent(site, path);
    }

    @Override
    @ValidateParams
    public List<DmOrderTO> getItemOrders(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path) throws ContentNotFoundException {
        List<DmOrderTO> dmOrderTOs = getOrders(site, path, "default", false);
        for (DmOrderTO dmOrderTO : dmOrderTOs) {
            dmOrderTO.setName(StringEscapeUtils.escapeJava(dmOrderTO.getName()));
        }
        return dmOrderTOs;
    }

    private List<DmOrderTO> getOrders(String site, String relativePath, String orderName, boolean includeFloating) {
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
    @ValidateParams
    public double reorderItems(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "relativePath") String relativePath, @ValidateSecurePathParam(name = "before") String before, @ValidateSecurePathParam(name = "after") String after, @ValidateStringParam(name = "orderName") String orderName) throws ServiceException {
        Double beforeOrder = null;
        Double afterOrder = null;
        DmOrderTO beforeOrderTO = null;
        DmOrderTO afterOrderTO = null;
        String fullPath = expandRelativeSitePath(site, relativePath);
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
            logger.debug("afterOrder == null");
            return dmPageNavigationOrderService.getNewNavOrder(site, ContentUtils.getParentUrl(relativePath.replace("/" + DmConstants.INDEX_FILE, "")));
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

        List<DmOrderTO> orderTO = getOrders(site, relativePath, orderName, true);
        Collections.sort(orderTO);

        int beforeIndex = orderTO.indexOf(beforeOrderTO);
        int afterIndex = orderTO.indexOf(afterOrderTO);

        if (!(beforeIndex + 1 == afterIndex)) {
            beforeOrderTO = orderTO.get(afterIndex - 1);
        }
        return (beforeOrderTO.getOrder() + afterOrderTO.getOrder()) / 2;
    }

    @Override
    @ValidateParams
    public boolean renameBulk(@ValidateStringParam(name = "site") String site, @ValidateSecurePathParam(name = "path") String path, @ValidateSecurePathParam(name = "targetPath") String targetPath, boolean createFolder) {
        generalLockService.lock(site + ":" + path);
        boolean result = false;
        try {
            moveContent(site, path, targetPath);
            result = true;
        } 
        finally {
            generalLockService.unlock(site + ":" + path);
        }

        return result; 
    }

    private ContentRepository _contentRepository;
    protected ServicesConfig servicesConfig;
    protected GeneralLockService generalLockService;
    protected ObjectStateService objectStateService;
    protected DependencyService dependencyService;
    protected ProcessContentExecutor contentProcessor;
    protected ObjectMetadataManager objectMetadataManager;
    protected SecurityService securityService;
    protected Reactor repositoryReactor;
    protected DmPageNavigationOrderService dmPageNavigationOrderService;
    protected SecurityProvider securityProvider;
    protected ActivityService activityService;
    protected DmContentLifeCycleService dmContentLifeCycleService;
    protected PreviewSync previewSync;
    protected CacheTemplate cacheTemplate;
    protected ContentItemIdGenerator contentItemIdGenerator;
    protected DmDependencyDiffService dmDependencyDiffService;


    public ContentRepository getContentRepository() { return _contentRepository; }
    public void setContentRepository(ContentRepository contentRepository) { this._contentRepository = contentRepository; }

    public ServicesConfig getServicesConfig() { return servicesConfig; }
    public void setServicesConfig(ServicesConfig servicesConfig) { this.servicesConfig = servicesConfig; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    public ObjectStateService getObjectStateService() { return objectStateService; }
    public void setObjectStateService(ObjectStateService objectStateService) { this.objectStateService = objectStateService; }

    public DependencyService getDependencyService() { return dependencyService; }
    public void setDependencyService(DependencyService dependencyService) { this.dependencyService = dependencyService; }

    public ProcessContentExecutor getContentProcessor() { return contentProcessor; }
    public void setContentProcessor(ProcessContentExecutor contentProcessor) { this.contentProcessor = contentProcessor; }

    public ObjectMetadataManager getObjectMetadataManager() { return objectMetadataManager; }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) { this.objectMetadataManager = objectMetadataManager; }

    public SecurityService getSecurityService() { return securityService; }
    public void setSecurityService(SecurityService securityService) { this.securityService = securityService; }

    public Reactor getRepositoryReactor() { return repositoryReactor; }
    public void setRepositoryReactor(Reactor repositoryReactor) { this.repositoryReactor = repositoryReactor; }

    public DmPageNavigationOrderService getDmPageNavigationOrderService() { return dmPageNavigationOrderService; }
    public void setDmPageNavigationOrderService(DmPageNavigationOrderService dmPageNavigationOrderService) { this.dmPageNavigationOrderService = dmPageNavigationOrderService; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public ActivityService getActivityService() { return activityService; }
    public void setActivityService(ActivityService activityService) { this.activityService = activityService; }

    public DmContentLifeCycleService getDmContentLifeCycleService() { return dmContentLifeCycleService; }
    public void setDmContentLifeCycleService(DmContentLifeCycleService dmContentLifeCycleService) { this.dmContentLifeCycleService = dmContentLifeCycleService; }

    public PreviewSync getPreviewSync() { return previewSync; }
    public void setPreviewSync(PreviewSync previewSync) { this.previewSync = previewSync; }

    public CacheTemplate getCacheTemplate() { return cacheTemplate; }
    public void setCacheTemplate(CacheTemplate cacheTemplate) { this.cacheTemplate = cacheTemplate; }

    public ContentItemIdGenerator getContentItemIdGenerator() { return contentItemIdGenerator; }
    public void setContentItemIdGenerator(ContentItemIdGenerator contentItemIdGenerator) { this.contentItemIdGenerator = contentItemIdGenerator; }

    public DmDependencyDiffService getDmDependencyDiffService() { return dmDependencyDiffService; }
    public void setDmDependencyDiffService(DmDependencyDiffService dmDependencyDiffService) { this.dmDependencyDiffService = dmDependencyDiffService; }
}
