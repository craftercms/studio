/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.service.content;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.validation.annotations.param.ValidateIntegerParam;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.dal.ItemMetadata;
import org.craftercms.studio.api.v1.dal.ItemState;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.executor.ProcessContentExecutor;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentItemIdGenerator;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.content.ContentTypeService;
import org.craftercms.studio.api.v1.service.content.DmContentLifeCycleService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v1.service.content.ObjectMetadataManager;
import org.craftercms.studio.api.v1.service.dependency.DependencyDiffService;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.craftercms.studio.api.v1.service.objectstate.ObjectStateService;
import org.craftercms.studio.api.v1.service.objectstate.State;
import org.craftercms.studio.api.v1.service.objectstate.TransitionEvent;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.to.ContentAssetInfoTO;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.CopyDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DeleteDependencyConfigTO;
import org.craftercms.studio.api.v1.to.DmOrderTO;
import org.craftercms.studio.api.v1.to.GoLiveDeleteCandidates;
import org.craftercms.studio.api.v1.to.RenderingTemplateTO;
import org.craftercms.studio.api.v1.to.ResultTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v1.util.DebugUtils;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.util.ContentFormatUtils;
import org.craftercms.studio.impl.v1.util.ContentItemOrderComparator;
import org.craftercms.studio.impl.v1.util.ContentUtils;

import org.craftercms.studio.impl.v2.utils.spring.ContentResource;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.DocumentException;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import javax.activation.MimetypesFileTypeMap;

import static org.craftercms.studio.api.v1.constant.DmConstants.KEY_PAGE_GROUP_ID;
import static org.craftercms.studio.api.v1.constant.DmConstants.KEY_PAGE_ID;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_CREATED_DATE;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_CREATED_DATE_DT;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_FILE_NAME;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_FOLDER_NAME;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_GROUP_ID;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_INTERNAL_NAME;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_LAST_MODIFIED_DATE;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_LAST_MODIFIED_DATE_DT;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.ELM_PAGE_ID;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_ENCODING;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_ASSET;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_COMPONENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_CONTENT_TYPE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_DOCUMENT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_FOLDER;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_PAGE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_RENDERING_TEMPLATE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_SCRIPT;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_TAXONOMY;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_TAXONOMY_REGEX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.CONTENT_TYPE_UNKNOWN;
import static org.craftercms.studio.api.v1.constant.StudioConstants.DATE_PATTERN_WORKFLOW_WITH_TZ;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v1.ebus.EBusConstants.EVENT_PREVIEW_SYNC;
import static org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.REVERT;
import static org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE;
import static org.craftercms.studio.api.v1.service.objectstate.TransitionEvent.SAVE_FOR_PREVIEW;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_DELETE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_MOVE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_PULL_FROM_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_PUSH_TO_REMOTE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_REVERT;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_FOLDER;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_REMOTE_REPOSITORY;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

/**
 * Content Services that other services may use
 * @author russdanner
 * @author Sumer Jabri
 */
public class ContentServiceImpl implements ContentService {
    // TODO: SJ: Refactor in 2.7.x to leverage Crafter Core as this will automatically enable inheritance, caching and
    // TODO: SJ: make that feature available to end user.
    private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

    private static final String COPY_DEP_XPATH = "//*/text()[normalize-space(.)='{copyDep}']/parent::*";
    private static final String COPY_DEP = "{copyDep}";

    private ContentRepository _contentRepository;
    private org.craftercms.studio.api.v2.repository.ContentRepository contentRepository;
    protected ServicesConfig servicesConfig;
    protected GeneralLockService generalLockService;
    protected ObjectStateService objectStateService;
    protected DependencyService dependencyService;
    protected ProcessContentExecutor contentProcessor;
    protected ObjectMetadataManager objectMetadataManager;
    protected SecurityService securityService;
    protected DmPageNavigationOrderService dmPageNavigationOrderService;
    protected DmContentLifeCycleService dmContentLifeCycleService;
    protected EventService eventService;
    protected SiteService siteService;
    protected ContentItemIdGenerator contentItemIdGenerator;
    protected StudioConfiguration studioConfiguration;
    protected DependencyDiffService dependencyDiffService;
    protected ContentTypeService contentTypeService;
    protected EntitlementValidator entitlementValidator;
    protected AuditServiceInternal auditServiceInternal;
    protected UserService userService;

    /**
     * file and folder name patterns for copied files and folders
     */
    public final static Pattern COPY_FILE_PATTERN = Pattern.compile("(.+)-([0-9]+)\\.(.+)");
    public final static Pattern COPY_FOLDER_PATTERN = Pattern.compile("(.+)-([0-9]+)");

    @Override
    @ValidateParams
    public boolean contentExists(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "path") String path) {
        // TODO: SJ: Refactor in 2.7.x as this might already exists in Crafter Core (which is part of the new Studio)
        return this._contentRepository.contentExists(site, path);
    }

    @Override
    @ValidateParams
    public InputStream getContent(@ValidateStringParam(name = "site") String site,
                                  @ValidateSecurePathParam(name = "path") String path)
            throws ContentNotFoundException, CryptoException {
        // TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
        if (StringUtils.equals(site, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
            return this._contentRepository.getContent(StringUtils.EMPTY, path);
        } else {
            return this._contentRepository.getContent(site, path);
        }
    }

    @Override
    @ValidateParams
    public long getContentSize(@ValidateStringParam(name = "site") String site,
                               @ValidateStringParam(name = "path") String path) {
        return _contentRepository.getContentSize(site, path);
    }

    @Override
    @ValidateParams
    public String getContentAsString(@ValidateStringParam(name = "site") String site,
                                     @ValidateSecurePathParam(name = "path") String path) {
        return getContentAsString(site, path, null);
    }

    @Override
    @ValidateParams
    public String getContentAsString(@ValidateStringParam(name = "site") String site,
                                     @ValidateSecurePathParam(name = "path") String path,
                                     @ValidateStringParam(name = "encoding") String encoding)  {
        // TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
        String content = null;

        try (InputStream is = _contentRepository.getContent(site, path)) {
            if (is != null) {
                if (StringUtils.isEmpty(encoding)) {
                    content = IOUtils.toString(is);
                } else {
                    content = IOUtils.toString(is, encoding);
                }
            }
        }
        catch(Exception err) {
            logger.debug("Failed to get content as string for path {0}", err, path);
        }

        return content;
    }

    @Override
    @ValidateParams
    public Document getContentAsDocument(@ValidateStringParam(name = "site") String site,
                                         @ValidateSecurePathParam(name = "path") String path)
            throws DocumentException {
        // TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
        Document retDocument = null;
        InputStream is = null;
        try {
            is = this.getContent(site, path);
        } catch (ContentNotFoundException | CryptoException e) {
            logger.debug("Content not found for path {0}", e, path);
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
                    logger.debug("Error closing stream for path {0}", err, path);
                }
            }
        }

        return retDocument;
    }

    @Override
    @ValidateParams
    public Resource getContentAsResource(@ValidateStringParam(name = "site") String site,
                                         @ValidateSecurePathParam(name = "path") String path)
        throws ContentNotFoundException {
        if (contentExists(site, path)) {
            return new ContentResource(this, site, path);
        } else {
            throw new ContentNotFoundException(path, site,
                String.format("File '%s' not found in site '%s'", path, site));
        }
    }

    @Override
    @ValidateParams
    public void writeContent(@ValidateStringParam(name = "site") String site,
                             @ValidateSecurePathParam(name = "path") String path,
                             @ValidateStringParam(name = "fileName") String fileName,
                             @ValidateStringParam(name = "contentType") String contentType, InputStream input,
                             @ValidateStringParam(name = "createFolders") String createFolders,
                             @ValidateStringParam(name = "edit") String edit,
                             @ValidateStringParam(name = "unlock") String unlock) throws ServiceLayerException {
        writeContent(site, path, fileName, contentType, input, createFolders, edit, unlock, false);
    }

    @Override
    @ValidateParams
    public void writeContent(@ValidateStringParam(name = "site") String site,
                             @ValidateSecurePathParam(name = "path") String path,
                             @ValidateStringParam(name = "fileName") String fileName,
                             @ValidateStringParam(name = "contentType") String contentType, InputStream input,
                             @ValidateStringParam(name = "createFolders") String createFolders,
                             @ValidateStringParam(name = "edit") String edit,
                             @ValidateStringParam(name = "unlock") String unlock,
                             boolean skipAuditLogInsert) throws ServiceLayerException {
        // TODO: SJ: refactor for 2.7.x

        try {
            entitlementValidator.validateEntitlement(EntitlementType.ITEM, 1);
        } catch (EntitlementException e) {
            throw new ServiceLayerException("Unable to complete request due to entitlement limits. Please contact your "
                + "system administrator.");
        }

        Map<String, String> params = new HashMap<String, String>();
        params.put(DmConstants.KEY_SITE, site);
        params.put(DmConstants.KEY_PATH, path);
        params.put(DmConstants.KEY_FILE_NAME, fileName);
        params.put(DmConstants.KEY_CONTENT_TYPE, contentType);
        params.put(DmConstants.KEY_CREATE_FOLDERS, createFolders);
        params.put(DmConstants.KEY_EDIT, edit);
        params.put(DmConstants.KEY_UNLOCK, unlock);
        params.put(DmConstants.KEY_SKIP_AUDIT_LOG_INSERT, String.valueOf(skipAuditLogInsert));
        String id = site + ":" + path + ":" + fileName + ":" + contentType;
        String relativePath = path;
        boolean contentExists = contentExists(site, path);
        String lockKey = id;
        if (contentExists) {
            lockKey = site + ":" + path;
        }
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
                                + "processing);", path);
                        throw new ServiceLayerException("Content " + path + " is in system processing, we can't write "
                                + "it");
                    }

                    objectStateService.setSystemProcessing(site, path, true);
                }
                else {
                    logger.error("the object state is still null even after attempting to create it for site {0} "
                            + "path {1} fileName {2} contentType {3}"
                            + ".", site, path, fileName, contentType);
                }
            } else {
                // Content does not exist; check for moved content and deleted content
                if (objectStateService.deletedPathExists(site, path) ||
                        objectMetadataManager.movedPathExists(site, path)) {
                    throw new ServiceLayerException("Content " + path + " for site " + site + ", cannot be created " +
                            "because this name/URL was in use by another content item that has been moved or" +
                            " deleted by not yet published.");
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
                relativePath = savedPath + FILE_SEPARATOR + savedFileName;
            }

            // TODO: SJ: Why is the item being loaded again? Why is the state being set to system not processing
            // TODO: SJ: again? Why would we insert the item into objectStateService again?
            // TODO: SJ: Refactor for 2.7.x
            ContentItemTO itemTo = getContentItem(site, relativePath, 0);
            if (itemTo != null) {
                if (isSaveAndClose) {
                    objectStateService.transition(site, itemTo, SAVE);
                } else {
                    objectStateService.transition(site, itemTo, SAVE_FOR_PREVIEW);
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
        }
    }

    @Override
    @ValidateParams
    public void writeContentAndRename(@ValidateStringParam(name = "site") final String site,
                                      @ValidateSecurePathParam(name = "path") final String path,
                                      @ValidateSecurePathParam(name = "targetPath") final String targetPath,
                                      @ValidateStringParam(name = "fileName") final String fileName,
                                      @ValidateStringParam(name = "contentType") final String contentType,
                                      final InputStream input,
                                      @ValidateStringParam(name = "createFolders") final String createFolders,
                                      @ValidateStringParam(name = "edit") final  String edit,
                                      @ValidateStringParam(name = "unlock") final String unlock,
                                      final boolean createFolder) throws ServiceLayerException {
        // TODO: SJ: The parameters need to be properly typed. Can't have Strings that actually mean boolean. Fix in
        // TODO: SJ: 2.7.x
        String id = site + ":" + path + ":" + fileName + ":" + contentType;

        // TODO: SJ: FIXME: Remove the log below after testing
        logger.debug("Write and rename for site '{}' path '{}' targetPath '{}' "
                + "fileName '{}' content type '{}'", site, path, targetPath, fileName, contentType);

        try {
            writeContent(site, path, fileName, contentType, input, createFolders, edit, unlock, true);
            moveContent(site, path, targetPath);
        } catch (ServiceLayerException | RuntimeException e) {
            logger.error("Error while executing write and rename for site '{}' path '{}' targetPath '{}' "
                    + "fileName '{}' content type '{}'", e, site, path, targetPath, fileName, contentType);
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
     * @throws ServiceLayerException
     */
    @Override
    @ValidateParams
    public Map<String, Object> writeContentAsset(@ValidateStringParam(name = "site") String site,
                                                 @ValidateSecurePathParam(name = "path") String path,
                                                 @ValidateStringParam(name = "assetName") String assetName,
                                                 InputStream in, String isImage, String allowedWidth,
                                                 String allowedHeight, String allowLessSize, String draft,
                                                 String unlock, String systemAsset) throws ServiceLayerException {

        try {
            entitlementValidator.validateEntitlement(EntitlementType.ITEM, 1);
        } catch (EntitlementException e) {
            throw new ServiceLayerException("Unable to complete request due to entitlement limits. Please contact your "
                + "system administrator.");
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

        boolean exists = contentExists(site, path+ FILE_SEPARATOR + assetName);
        params.put(DmConstants.KEY_ACTIVITY_TYPE, (exists ? OPERATION_UPDATE : OPERATION_CREATE));

        String id = site + ":" + path + ":" + assetName + ":" + "";
        // processContent will close the input stream
        ContentItemTO item = null;
        try {
            path = path + FILE_SEPARATOR + assetName;
            item = getContentItem(site, path);

            if (item != null) {
                ItemState itemState = objectStateService.getObjectState(site, path);
                if (itemState != null) {
                    if (itemState.getSystemProcessing() != 0) {
                        logger.error(String.format("Error Content %s is being processed " +
                                "(Object State is SYSTEM_PROCESSING);", path));
                        throw new RuntimeException(String.format("Content \"%s\" is being processed", path));
                    }
                    objectStateService.setSystemProcessing(site, path, true);
                }
            }

            if (objectStateService.deletedPathExists(site, path) ||
                    objectMetadataManager.movedPathExists(site, path)) {
                throw new ServiceLayerException("Content " + path + " for site " + site + ", cannot be created because"
                    + " this name/URL was in use by another content item that has been moved or deleted by "
                    + "not yet published.");
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
                if (result.getCommitId() != null) {
                    objectStateService.transition(site, item, SAVE);
                } else {
                    objectStateService.transition(site, item, TransitionEvent.CANCEL_EDIT);
                }
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
    @ValidateParams
    public boolean writeContent(@ValidateStringParam(name = "site") String site,
                                @ValidateSecurePathParam(name = "path") String path, InputStream content)
            throws ServiceLayerException {
        boolean result;

        String commitId = _contentRepository.writeContent(site, path, content);

        result = StringUtils.isNotEmpty(commitId);

        if (result) {
            // Update database with commitId
            if (!objectMetadataManager.metadataExist(site, path)) {
                objectMetadataManager.insertNewObjectMetadata(site, path);
            }
            objectMetadataManager.updateCommitId(site, path, commitId);
            contentRepository.insertGitLog(site, commitId, 1, 1);
            siteService.updateLastCommitId(site, commitId);
        }

        return result;
    }

    @Override
    @ValidateParams
    public boolean createFolder(@ValidateStringParam(name = "site") String site,
                                @ValidateSecurePathParam(name = "path") String path,
                                @ValidateStringParam(name = "name") String name) throws SiteNotFoundException {
        boolean toRet = false;
        String commitId = _contentRepository.createFolder(site, path, name);
        if (commitId != null) {
            SiteFeed siteFeed = siteService.getSite(site);
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_CREATE);
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setActorId(securityService.getCurrentUser());
            auditLog.setPrimaryTargetId(site + ":" + path + FILE_SEPARATOR + name);
            auditLog.setPrimaryTargetType(TARGET_TYPE_FOLDER);
            auditLog.setPrimaryTargetValue(path + FILE_SEPARATOR + name);
            auditServiceInternal.insertAuditLog(auditLog);

            contentRepository.insertGitLog(site, commitId, 1, 1);
            siteService.updateLastCommitId(site, commitId);
            toRet = true;
        }

        return toRet;
    }

    @Override
    @ValidateParams
    public boolean deleteContent(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "path") String path,
                                 @ValidateStringParam(name = "approver") String approver) throws SiteNotFoundException {
        return deleteContent(site, path, true, approver);
    }

    @Override
    @ValidateParams
    public boolean deleteContent(@ValidateStringParam(name = "site") String site,
                                 @ValidateSecurePathParam(name = "path") String path, boolean generateActivity,
                                 @ValidateStringParam(name = "approver") String approver) throws SiteNotFoundException {
        String commitId;
        boolean toReturn = false;
        if (generateActivity) {
            generateDeleteActivity(site, path, approver);
        }

        commitId = _contentRepository.deleteContent(site, path, approver);

        objectStateService.deleteObjectStateForPath(site, path);
        objectMetadataManager.deleteObjectMetadata(site, path);
        try {
            dependencyService.deleteItemDependencies(site, path);
        } catch (ServiceLayerException e) {
            logger.error("Error deleting dependencies for site " + site + " path " + path, e);
        }

        if (StringUtils.isNotEmpty(commitId)) {
            contentRepository.insertGitLog(site, commitId, 1);
        }

        PreviewEventContext context = new PreviewEventContext();
        context.setSite(site);
        eventService.publish(EVENT_PREVIEW_SYNC, context);


        // TODO: SJ: Add commitId to database for this item in version 2.7.x

        if (commitId != null) {
            toReturn = true;
        }

        return toReturn;
    }

    protected void generateDeleteActivity(String site, String path, String approver) throws SiteNotFoundException {
        // This method creates a database record to show the activity of deleting a file
        // TODO: SJ: This type of thing needs to move to the audit service which handles all records related to
        // TODO: SJ: activities. Fix in 3.1+ by introducing the audit service and refactoring accordingly
        if (StringUtils.isEmpty(approver)) {
            approver = securityService.getCurrentUser();
        }
        boolean exists = contentExists(site, path);
        if (exists) {
            ContentItemTO item = getContentItem(site, path, 0);
            ItemMetadata properties = objectMetadataManager.getProperties(site, path);
            String user = (properties != null && !StringUtils.isEmpty(properties.getSubmittedBy()) ? properties
                .getSubmittedBy() : approver);
            Map<String, String> extraInfo = new HashMap<String, String>();
            if (item.isFolder()) {
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, CONTENT_TYPE_FOLDER);
            } else {
                extraInfo.put(DmConstants.KEY_CONTENT_TYPE, getContentTypeClass(site, path));
            }
            logger.debug("[DELETE] posting delete activity on " + path + " by " + user + " in " + site);
            SiteFeed siteFeed = siteService.getSite(site);
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_DELETE);
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setActorId(user);
            auditLog.setPrimaryTargetId(site + ":" + path);
            auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLog.setPrimaryTargetValue(path);
            auditLog.setPrimaryTargetSubtype(getContentTypeClass(site, path));
            auditServiceInternal.insertAuditLog(auditLog);
            // process content life cycle
            if (path.endsWith(DmConstants.XML_PATTERN)) {

                String contentType = item.getContentType();
                dmContentLifeCycleService.process(site, user, path,
                        contentType, DmContentLifeCycleService.ContentLifeCycleOperation.DELETE, null);
            }
        }
    }

    @Override
    @ValidateParams
    public String copyContent(@ValidateStringParam(name = "site") String site,
                              @ValidateSecurePathParam(name = "fromPath") String fromPath,
                              @ValidateSecurePathParam(name = "toPath") String toPath) {
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

            String copyPathOnly = copyPath.substring(0, copyPath.lastIndexOf(FILE_SEPARATOR));
            String copyFileName = copyPath.substring(copyPath.lastIndexOf(FILE_SEPARATOR)+1);

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
                        if (fromPath.endsWith(DmConstants.XML_PATTERN)) {
                            Document fromDocument = ContentUtils.convertStreamToXml(fromContent);

                            Map<String, String> fromPageIds = getContentIds(fromDocument);

                            logger.debug("copying file for site {0} from {1} to {2}, new name is {3}",
                                    site, fromPath, toPath, copyPath);

                            // come up with a new object ID and group ID for the object
                            Map<String, String> copyObjectIds = contentItemIdGenerator.getIds();

                            Map<String, String> copyDependencies = getCopyDependencies(site, fromPath,
                                    fromPath);
                            copyDependencies = getItemSpecificDependencies(site, fromPath, fromDocument,
                                    copyDependencies);

                            logger.debug("Calculated copy dependencies: {0}, {1}", fromPath, copyDependencies);

                            // Duplicate the children
                            for (String dependencyKey : copyDependencies.keySet()) {
                                String dependencyPath = copyDependencies.get(dependencyKey);
                                String copyDepPath = dependencyPath;

                                // try a simple substitution
                                copyDepPath = copyDepPath.replaceAll(
                                        fromPageIds.get(KEY_PAGE_ID),
                                        copyObjectIds.get(KEY_PAGE_ID));

                                copyDepPath = copyDepPath.replaceAll(
                                        fromPageIds.get(KEY_PAGE_GROUP_ID),
                                        copyObjectIds.get(KEY_PAGE_GROUP_ID));

                                ContentItemTO targetPathItem = getContentItem(site, copyDepPath);
                                if (targetPathItem != null && targetPathItem.isFolder()) {
                                    copyDepPath = copyDepPath + FILE_SEPARATOR + FilenameUtils.getName(dependencyKey);
                                    copyDepPath = copyDepPath.replaceAll(FILE_SEPARATOR + FILE_SEPARATOR,
                                            FILE_SEPARATOR);
                                } else if (!copyDepPath.endsWith(DmConstants.XML_PATTERN)) {
                                    copyDepPath = ContentUtils.getParentUrl(copyDepPath);
                                }
                                
                                logger.debug("Translated dependency path from {0} to {1}",
                                        dependencyPath, copyDepPath);

                                String newCopyDepthPath = copyContent(site, dependencyKey, copyDepPath, processedPaths);
                                fromDocument = replaceCopyDependency(fromDocument, dependencyKey, newCopyDepthPath);
                            }

                            // update the file name / folder values
                            Document copyDocument = updateContentOnCopy(fromDocument, copyPathFileName, copyPathFolder,
                                    copyObjectIds, copyPathModifier);

                            copyContent = ContentUtils.convertDocumentToStream(copyDocument, CONTENT_ENCODING);
                        }

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

                        // processContent will close the input stream
                        if (copyFileName.endsWith(DmConstants.XML_PATTERN)) {
                            processContent(id, copyContent, true, params, DmConstants.CONTENT_CHAIN_FORM);
                        } else {
                            processContent(id, fromContent, false, params, DmConstants.CONTENT_CHAIN_ASSET);
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
                    } catch (ContentNotFoundException eContentNotFound) {
                        logger.debug("Content not found while copying content for site {0} from {1} to {2}," +
                                " new name is {3}", eContentNotFound, site, fromPath, toPath, copyPath);
                    } catch (DocumentException eParseException) {
                        logger.error("General Error while copying content for site {0} from {1} to {2}," +
                                " new name is {3}", eParseException, site, fromPath, toPath, copyPath);
                    } catch (CryptoException e) {
                        logger.error("Unexpected Error while copying content for site {0} from {1} to {2}," +
                                " new name is {3}", e, site, fromPath, toPath, copyPath);
                    } finally {
                        IOUtils.closeQuietly(copyContent);
                    }
                }
            } else {
                // no need to process
                retNewFileName = copyPath;
            }
        } catch(ServiceLayerException eServiceLayerException) {
            logger.info("General Error while copying content for site {0} from {1} to {2}, new name is {3}",
                eServiceLayerException, site, fromPath, toPath, copyPath);
        }

        return retNewFileName;
    }

    @SuppressWarnings("unchecked")
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

    private Map<String, String> getCopyDependencies(@ValidateStringParam(name = "site") String site,
                                                    @ValidateSecurePathParam(name = "sourceContentPath")
                                                            String sourceContentPath,
                                                    @ValidateSecurePathParam(name = "dependencyPath")
                                                            String dependencyPath)
            throws ServiceLayerException {
        Map<String,String> copyDependency = new HashMap<String,String>();
        if(sourceContentPath.endsWith(DmConstants.XML_PATTERN) && dependencyPath.endsWith(DmConstants.XML_PATTERN)){
            ContentItemTO dependencyItem = getContentItem(site, sourceContentPath);
            if (dependencyItem != null) {
                String contentType = dependencyItem.getContentType();
                List<CopyDependencyConfigTO> copyDependencyPatterns =
                        servicesConfig.getCopyDependencyPatterns(site, contentType);
                if (copyDependencyPatterns != null && copyDependencyPatterns.size() > 0) {
                    logger.debug("Copy Pattern provided for contentType" + contentType);
                    Set<String> dependencies = dependencyService.getItemDependencies(site, dependencyPath, 1);
                    if (CollectionUtils.isNotEmpty(dependencies)) {
                        for (String dependency : dependencies) {
                            for (CopyDependencyConfigTO copyConfig : copyDependencyPatterns) {
                                if (contentExists(site, dependency) &&
                                        StringUtils.isNotEmpty(copyConfig.getPattern()) &&
                                        StringUtils.isNotEmpty(copyConfig.getTarget()) &&
                                        dependency.matches(copyConfig.getPattern())) {
                                    copyDependency.put(dependency, copyConfig.getTarget());
                                }
                            }
                        }
                    }
                } else {
                    logger.debug("Copy Pattern is not provided for contentType" + contentType);
                }
            } else {
                logger.debug("Not found dependency item at site {0} path {!}", site, sourceContentPath);
            }
        }
        return copyDependency;
    }

    @Override
    @ValidateParams
    public String moveContent(@ValidateStringParam(name = "site") String site,
                              @ValidateSecurePathParam(name = "fromPath") String fromPath,
                              @ValidateSecurePathParam(name = "toPath") String toPath) {
        String retNewFileName = null;
        boolean opSuccess = false;
        String movePath = null;

        try {
            String sourcePath = (fromPath.indexOf("" + FILE_SEPARATOR + DmConstants.INDEX_FILE) != -1) ?
                    fromPath.substring(0, fromPath.lastIndexOf(FILE_SEPARATOR)) : fromPath;
            String sourcePathOnly = fromPath.substring(0, fromPath.lastIndexOf(FILE_SEPARATOR));

            Map<String, String> movePathMap = constructNewPathforCutCopy(site, fromPath, toPath, true);
            movePath = movePathMap.get("FILE_PATH");
            String moveFileName = movePathMap.get("FILE_NAME");
            String movePathOnly = movePath.substring(0, movePath.lastIndexOf(FILE_SEPARATOR));
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

            logger.debug("Move file for site {0} from {1} to {2}, sourcePath {3} to target path {4}", site,
                    fromPath, toPath, sourcePath, targetPath);

            // NOTE: IN WRITE SCENARIOS the repository OP IS PART of this PIPELINE, for some reason,
            // historically with MOVE it is not
            Map<String, String> commitIds = _contentRepository.moveContent(site, sourcePath, targetPath);

            if (commitIds != null) {
                // Update the database with the commitId for the target item
                updateDatabaseOnMove(site, fromPath, movePath);
                updateChildrenOnMove(site, fromPath, movePath);
                for (Map.Entry<String, String> entry : commitIds.entrySet()) {
                    objectMetadataManager.updateCommitId(site, FILE_SEPARATOR + entry.getKey(), entry.getValue());
                    contentRepository.insertGitLog(site, entry.getValue(), 1, 1);
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
        catch(ServiceLayerException eMoveErr) {
            logger.error("Content not found while moving content for site {0} from {1} to {2}, new name is {3}",
                    eMoveErr, site, fromPath, toPath, movePath);
        }

        return movePath;
    }

    protected void updateDatabaseOnMove(String site, String fromPath, String movePath) throws SiteNotFoundException {
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
            objectStateService.transition(site, renamedItem, SAVE);
            renamedItem = getContentItem(site, movePath, 0);
        }
        // update metadata
        if (!objectMetadataManager.isRenamed(site, fromPath)) {
            // if an item was previously moved, we do not track intermediate moves because it will
            // ultimately orphan deployed content.  Old Path is always the OLDEST DEPLOYED PATH
            ItemMetadata metadata = objectMetadataManager.getProperties(site, fromPath);
            if (metadata == null && !renamedItem.isFolder()) {
                if (!objectMetadataManager.metadataExist(site, fromPath)) {
                    objectMetadataManager.insertNewObjectMetadata(site, fromPath);
                }
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
            if (objectMetadataManager.metadataExist(site, movePath)) {
                if (!StringUtils.equalsIgnoreCase(fromPath, movePath)) {
                    objectMetadataManager.deleteObjectMetadata(site, movePath);
                }
            }
            objectMetadataManager.updateObjectPath(site, fromPath, movePath);
        }

        // write activity stream
        SiteFeed siteFeed = siteService.getSite(site);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_MOVE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(user);
        auditLog.setPrimaryTargetId(site + ":" + movePath);
        if (renamedItem.isFolder()) {
            auditLog.setPrimaryTargetType(TARGET_TYPE_FOLDER);
        } else {
            auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
        }
        auditLog.setPrimaryTargetValue(movePath);
        auditLog.setPrimaryTargetSubtype(getContentTypeClass(site, movePath));
        auditServiceInternal.insertAuditLog(auditLog);

        updateDependenciesOnMove(site, fromPath, movePath);
    }

    protected void updateDependenciesOnMove(String site, String fromPath, String movePath) {
        try {
            dependencyService.deleteItemDependencies(site, fromPath);
        } catch (ServiceLayerException e) {
            logger.error("Error while deleting dependencies for site " + site + " path " + fromPath, e);
        }
        try {
            dependencyService.upsertDependencies(site, movePath);
        } catch (ServiceLayerException e) {
            logger.error("Error while updating dependencies on move content site: " + site + " path: "
                    + movePath, e);
        }
    }

    protected void updateChildrenOnMove(String site, String fromPath, String movePath) throws SiteNotFoundException {
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

    protected Map<String, String> constructNewPathforCutCopy(String site, String fromPath, String toPath,
                                                             boolean adjustOnCollide) throws ServiceLayerException {
        Map<String, String> result = new HashMap<String, String>();

        // The following rules apply to content under the site folder
        String fromPathOnly = fromPath.substring(0, fromPath.lastIndexOf(FILE_SEPARATOR));
        String fromFileNameOnly = fromPath.substring(fromPath.lastIndexOf(FILE_SEPARATOR)+1);
        boolean fromFileIsIndex = ("index.xml".equals(fromFileNameOnly));
        logger.debug("Cut/copy name rules from path: '{0}' name: '{1}'", fromPathOnly, fromFileNameOnly);

        if(fromFileIsIndex) {
            fromFileNameOnly = fromPathOnly.substring(fromPathOnly.lastIndexOf(FILE_SEPARATOR)+1);
            fromPathOnly = fromPathOnly.substring(0, fromPathOnly.lastIndexOf(FILE_SEPARATOR));
            logger.debug("Cut/copy name rules index from path: '{0}' name: '{1}'", fromPathOnly, fromFileNameOnly);
        }

        String newPathOnly = (toPath.contains(".xml")) ?
                toPath.substring(0, toPath.lastIndexOf(FILE_SEPARATOR)) : toPath;
        String newFileNameOnly = (toPath.contains(".xml")) ?
                toPath.substring(toPath.lastIndexOf(FILE_SEPARATOR)+1) : fromFileNameOnly;
        boolean newFileIsIndex = ("index.xml".equals(newFileNameOnly));
        logger.debug("Cut/copy name rules to path: '{0}' name: '{1}'", newPathOnly, newFileNameOnly);

        if(newFileIsIndex) {
            newFileNameOnly = newPathOnly.substring(newPathOnly.lastIndexOf(FILE_SEPARATOR)+1);
            newPathOnly = newPathOnly.substring(0, newPathOnly.lastIndexOf(FILE_SEPARATOR));
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
                proposedDestPath = newPathOnly + FILE_SEPARATOR + newFileNameOnly + FILE_SEPARATOR +
                        DmConstants.INDEX_FILE;
                proposedDestPath_filename = DmConstants.INDEX_FILE;
                proposedDestPath_folder = newFileNameOnly;
            }
            else {
                // this is a location move
                proposedDestPath = newPathOnly + FILE_SEPARATOR + newFileNameOnly + FILE_SEPARATOR +
                        fromFileNameOnly + FILE_SEPARATOR + DmConstants.INDEX_FILE;
                proposedDestPath_filename = DmConstants.INDEX_FILE;
                proposedDestPath_folder = fromFileNameOnly;
            }
        }
        else if(fromFileIsIndex && !newFileIsIndex) {
            // Example MOVE LOCATION, INDEX TO FOLDER
            // fromPath: "/site/website/search/index.xml"
            // toPath:   "/site/website/a-folder"
            // newPath:  "/site/website/a-folder/search/index.xml"
            proposedDestPath = newPathOnly + FILE_SEPARATOR + fromFileNameOnly + FILE_SEPARATOR +
                    DmConstants.INDEX_FILE;
            proposedDestPath_filename = DmConstants.INDEX_FILE;
            proposedDestPath_folder = fromFileNameOnly;
        }
        else if(!fromFileIsIndex && newFileIsIndex) {
            proposedDestPath = newPathOnly + FILE_SEPARATOR + newFileNameOnly + FILE_SEPARATOR + fromFileNameOnly;
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
                if (!_contentRepository.contentExists(site, newPathOnly) ||
                        _contentRepository.isFolder(site, newPathOnly)) {
                    proposedDestPath = newPathOnly + FILE_SEPARATOR + fromFileNameOnly;
                } else {
                    proposedDestPath = newPathOnly;
                }
                proposedDestPath_filename = fromFileNameOnly;
                proposedDestPath_folder = newPathOnly.substring(0, newPathOnly.lastIndexOf(FILE_SEPARATOR));
            }
            else {
                // rename
                proposedDestPath = newPathOnly + FILE_SEPARATOR + newFileNameOnly;
                proposedDestPath_filename = newFileNameOnly;
                proposedDestPath_folder = newPathOnly.substring(0, newPathOnly.lastIndexOf(FILE_SEPARATOR));
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
                String id = ids.get(KEY_PAGE_GROUP_ID);

                if(proposedDestPath.indexOf(FILE_SEPARATOR + DmConstants.INDEX_FILE) == -1) {
                    int pdpli = proposedDestPath.lastIndexOf(".");
                    if (pdpli == -1) pdpli = proposedDestPath.length();
                    proposedDestPath =
                            proposedDestPath.substring(0, pdpli) + "-" + id +
                                    proposedDestPath.substring(pdpli);

                    // a regex would be better
                    proposedDestPath_filename =
                            proposedDestPath.substring(proposedDestPath.lastIndexOf(FILE_SEPARATOR)+1);
                    proposedDestPath_folder =
                            proposedDestPath.substring(0, proposedDestPath.lastIndexOf(FILE_SEPARATOR));
                    proposedDestPath_folder =
                            proposedDestPath_folder.substring(proposedDestPath_folder.lastIndexOf(FILE_SEPARATOR)+1);
                }
                else {
                    proposedDestPath =
                            proposedDestPath.substring(0,
                                    proposedDestPath.indexOf(FILE_SEPARATOR + DmConstants.INDEX_FILE)) + "-" + id +
                                    proposedDestPath.substring(
                                            proposedDestPath.lastIndexOf(FILE_SEPARATOR + DmConstants.INDEX_FILE));

                    proposedDestPath_filename = DmConstants.INDEX_FILE;
                    proposedDestPath_folder =
                            proposedDestPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE,"");
                    proposedDestPath_folder =
                            proposedDestPath_folder.substring(proposedDestPath_folder.lastIndexOf(FILE_SEPARATOR)+1);
                }

                result.put("FILE_PATH", proposedDestPath);
                result.put("FILE_NAME", proposedDestPath_filename);
                result.put("FILE_FOLDER", proposedDestPath_folder);
                result.put("MODIFIER", id);
                result.put("ALT_NAME", "true");
            }
            catch(Exception altPathGenErr) {
                throw new ServiceLayerException("Unable to generate an alternative path for name collision: " +
                        proposedDestPath, altPathGenErr);
            }
        }

        logger.debug("Final proposed path from : '{0}' to: '{1}' final name '{2}'", fromPath, toPath,
            proposedDestPath);
        return result;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, String> getItemSpecificDependencies(String site, String path, Document document,
                                                              Map<String, String> copyDependencies)
            throws ServiceLayerException {
        Set<String> deps = dependencyService.getItemSpecificDependencies(site, path, 1);
        for (String dep : deps) {
            copyDependencies.put(dep, dep);
        }

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
        if (document != null) {
            Element root = document.getRootElement();
            Node pageIdNode = root.selectSingleNode("//" + ELM_PAGE_ID);
            if (pageIdNode != null) {
                ids.put(KEY_PAGE_ID, ((Element) pageIdNode).getText());
            }

            Node groupIdNode = root.selectSingleNode("//" + ELM_GROUP_ID);
            if (groupIdNode != null) {
                ids.put(KEY_PAGE_GROUP_ID, ((Element) groupIdNode).getText());
            }
        }
        return ids;
    }

    @SuppressWarnings("unchecked")
    protected Document updateContentOnCopy(Document document, String filename, String folder, Map<String,
        String> params, String modifier)
            throws ServiceLayerException {

        //update pageId and groupId with the new one
        Element root = document.getRootElement();
        String originalPageId = null;
        String originalGroupId = null;

        Node filenameNode = root.selectSingleNode("//" + ELM_FILE_NAME);
        if (filenameNode != null) {
            filenameNode.setText(filename);
        }

        Node folderNode = root.selectSingleNode("//" + ELM_FOLDER_NAME);
        if (folderNode != null) {
            folderNode.setText(folder);
        }

        Node pageIdNode = root.selectSingleNode("//" + ELM_PAGE_ID);
        if (pageIdNode != null) {
            originalPageId = pageIdNode.getText();
            pageIdNode.setText(params.get(KEY_PAGE_ID));
        }

        if(modifier != null) {
            Node internalNameNode = root.selectSingleNode("//" + ELM_INTERNAL_NAME);
            if (internalNameNode != null) {
                String internalNameValue = internalNameNode.getText();
                internalNameNode.setText(internalNameValue + " " + modifier);
            }
        }

        Node groupIdNode = root.selectSingleNode("//" + ELM_GROUP_ID);
        if (groupIdNode != null) {
            originalGroupId = groupIdNode.getText();
            groupIdNode.setText(params.get(KEY_PAGE_GROUP_ID));
        }

        List<Node> keys = root.selectNodes("//key");
        if (keys != null) {
            for(Node keyNode : keys) {
                String keyValue = keyNode.getText();
                keyValue = keyValue.replaceAll(originalPageId, params.get(KEY_PAGE_ID));
                keyValue = keyValue.replaceAll(originalGroupId, params.get(KEY_PAGE_GROUP_ID));

                if(keyValue.contains("/page")) {
                    keyNode.setText(keyValue);
                }
            }
        }

        List<Node> includes = root.selectNodes("//include");
        if (includes != null) {
            for(Node includeNode : includes) {
                String includeValue = includeNode.getText();
                includeValue = includeValue.replaceAll(originalPageId, params.get(KEY_PAGE_ID));
                includeValue = includeValue.replaceAll(originalGroupId, params.get(KEY_PAGE_GROUP_ID));

                if(includeValue.contains("/page")) {
                    includeNode.setText(includeValue);
                }
            }
        }

        ZonedDateTime now = ZonedDateTime.now();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_PATTERN_WORKFLOW_WITH_TZ);
        String nowFormatted = now.format(DateTimeFormatter.ofPattern(sdf.toPattern()));

        Node createdDateNode = root.selectSingleNode("//" + ELM_CREATED_DATE);
        if (createdDateNode != null) {
            createdDateNode.setText(nowFormatted);
        }

        Node createdDateDtNode = root.selectSingleNode("//" + ELM_CREATED_DATE_DT);
        if (createdDateDtNode != null) {
            createdDateDtNode.setText(nowFormatted);
        }

        Node lastModifiedDateNode = root.selectSingleNode("//" + ELM_LAST_MODIFIED_DATE);
        if (lastModifiedDateNode != null) {
            lastModifiedDateNode.setText(nowFormatted);
        }

        Node lastModifiedDateDtNode = root.selectSingleNode("//" + ELM_LAST_MODIFIED_DATE_DT);
        if (lastModifiedDateDtNode != null) {
            lastModifiedDateDtNode.setText(nowFormatted);
        }

        return document;
    }

 /* ======================== */

    protected ContentItemTO createNewContentItemTO(String site, String contentPath) {
        ContentItemTO item = new ContentItemTO();
        // FIXME: SJ: This is another workaround for UI issues
        contentPath = FilenameUtils.normalize(contentPath, true);

        item.uri = contentPath;
        item.path = contentPath.substring(0, contentPath.lastIndexOf(FILE_SEPARATOR));
        item.name = contentPath.substring(contentPath.lastIndexOf(FILE_SEPARATOR) + 1);

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
        // TODO: DB: Review again in 3.1+
        item.folder = _contentRepository.isFolder(site, contentPath);

        return item;
    }

    @SuppressWarnings("unchecked")
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
        item.component = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)) ||
                item.isLevelDescriptor();
        item.isComponent = item.component;
        item.asset = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getAssetPatterns(site));
        item.isAsset = item.asset;
        item.document = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site));
        item.isDocument = item.document;

        item.uri = contentPath;
        // TODO: SJ: This is hokey, fix in 3.1+
        item.path = contentPath.substring(0, contentPath.lastIndexOf(FILE_SEPARATOR));
        // TODO: SJ: This is hokey, fix in 3.1+
        item.name = contentPath.substring(contentPath.lastIndexOf(FILE_SEPARATOR) + 1);
        item.browserUri = contentPath;

        if(item.page) {
            // TODO: SJ: This is hokey, fix in 4.x
            item.browserUri =
                    contentPath.replace(FILE_SEPARATOR + "site" + FILE_SEPARATOR + "website", "")
                            .replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
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
     * @return item orders metadata
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

        if(contentPath.indexOf(FILE_SEPARATOR + DmConstants.INDEX_FILE) != -1
                || contentPath.indexOf(".") == -1 ) { // item.isFolder?

            if (contentPath.indexOf(FILE_SEPARATOR + DmConstants.INDEX_FILE) != -1) {
                contentPath = contentPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
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
                        if (!item.uri.contains(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
                            item.path = item.uri;
                            item.uri = item.uri + FILE_SEPARATOR + DmConstants.INDEX_FILE;
                        }
                        item.numOfChildren--;
                        indexFound = true;
                    }
                    else {
                        if (depth > 1) {
                            String childPath = childRepoItems[j].path + FILE_SEPARATOR + childRepoItems[j].name;
                            if (childPath.startsWith(FILE_SEPARATOR + "site" + FILE_SEPARATOR + "website" +
                                    FILE_SEPARATOR) && childRepoItems[j].isFolder &&
                                    contentExists(item.site,childPath + FILE_SEPARATOR + DmConstants.INDEX_FILE )) {
                                children.add(getContentItem(item.site, childPath + FILE_SEPARATOR +
                                        DmConstants.INDEX_FILE, depth - 1));
                            } else {
                                children.add(getContentItem(item.site, childPath, depth - 1));
                            }
                        }
                    }
                }

                if(!indexFound && _contentRepository.isFolder(item.site, contentPath)) {
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
                Comparator<ContentItemTO> comparator =
                        new ContentItemOrderComparator("default", true, true, true);
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
    public ContentItemTO getContentItem(@ValidateStringParam(name = "site") String site,
                                        @ValidateSecurePathParam(name = "path") String path) {
        return getContentItem(site, path, 2);
    }

    @Override
    @ValidateParams
    public ContentItemTO getContentItem(@ValidateStringParam(name = "site") String site,
                                        @ValidateSecurePathParam(name = "path") String path,
                                        @ValidateIntegerParam(name = "depth") int depth) {
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
        logger.debug("Content item from site '{}' path '{}' retrieved in '{}' milli-seconds",
                site, path, executionTime);
        return item;
    }

    protected ContentItemTO loadContentItem(String site, String path) {
        // TODO: SJ: Refactor such that the populate of non-XML is also a method in 3.1+
        ContentItemTO item = createNewContentItemTO(site, path);

        if (item.uri.endsWith(".xml") && !item.uri.startsWith("/config/")) {

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
            item.asset = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getAssetPatterns(site)) ||
                    ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getRenderingTemplatePatterns(site)) ||
                    ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getScriptsPatterns(site));
            item.isAsset = item.asset;
            item.component = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)) ||
                    item.isLevelDescriptor() || item.asset;
            item.isComponent = item.component;
            item.document = ContentUtils.matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site));
            item.isDocument = item.document;
            item.browserUri =item.getUri();
            item.setContentType(getContentTypeClass(site, path));
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
        if (item.isFolder()) {
            item.setContentType(CONTENT_TYPE_FOLDER);
        } else {
            if (contentType != null && !contentType.equals(CONTENT_TYPE_FOLDER) && !contentType.equals("asset") &&
                    !contentType.equals(CONTENT_TYPE_UNKNOWN)) {
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
                item.setNew(State.isNew(State.valueOf(state.getState())));
                item.setLive(State.isLive(State.valueOf(state.getState())));
            }
            item.isNew = item.isNew();
            item.isLive = item.isLive();
            item.setInProgress(!item.isLive());
            item.isInProgress = item.isInProgress();
            item.setScheduled(State.isScheduled(State.valueOf(state.getState())));
            item.isScheduled = item.isScheduled();
            item.setSubmitted(State.isSubmitted(State.valueOf(state.getState())));
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
                item.setInProgress(!item.isLive());
                item.isInProgress = item.isInProgress();
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

            if (metadata.getPublishedDate() != null) {
                item.published = true;
                item.setPublished(true);
                item.publishedDate = metadata.getPublishedDate();
                item.setPublishedDate(metadata.getPublishedDate());
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
            if (StringUtils.isNotEmpty(metadata.getSubmittedToEnvironment())) {
                item.setSubmittedToEnvironment(metadata.getSubmittedToEnvironment());
            }
        } else {
            item.setLockOwner("");
        }
    }

    @Override
    @ValidateParams
    public ContentItemTO getContentItemTree(@ValidateStringParam(name = "site") String site,
                                            @ValidateSecurePathParam(name = "path") String path,
                                            @ValidateIntegerParam(name = "depth") int depth) {
        logger.debug("Getting content item  tree for '{}':'{}' depth '{}'", site, path, depth);
        DebugUtils.addDebugStack(logger);

        long startTime = System.currentTimeMillis();
        boolean isPages = (path.contains(FILE_SEPARATOR + "site" + FILE_SEPARATOR + "website"));
        ContentItemTO root = null;

        if (isPages && contentExists(site, path + FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
            if (depth > 1) {
                root = getContentItem(site, path + FILE_SEPARATOR + DmConstants.INDEX_FILE, depth);
            } else {
                root = getContentItem(site, path + FILE_SEPARATOR + DmConstants.INDEX_FILE);
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
    @ValidateParams
    public VersionTO[] getContentItemVersionHistory(@ValidateStringParam(name = "site") String site,
                                                    @ValidateSecurePathParam(name = "path") String path) {
        // TODO: SJ: Switch this to return a collection and rely on Groovy to change it up for the UI
        return _contentRepository.getContentVersionHistory(site, path);
    }

    @Override
    @ValidateParams
    public boolean revertContentItem(@ValidateStringParam(name = "site") String site,
                                     @ValidateSecurePathParam(name = "path") String path,
                                     @ValidateStringParam(name = "version") String version, boolean major,
                                     @ValidateStringParam(name = "comment") String comment) throws SiteNotFoundException {
        boolean toReturn = false;
        String commitId = _contentRepository.revertContent(site, path, version, major, comment);

        if (commitId != null) {
            try {
                dependencyService.upsertDependencies(site, path);
            } catch (ServiceLayerException e) {
                logger.error("Error while extracting dependencies for reverted content. Site: " + site + " path: " +
                        path + " version: " + version);
            }
            // Update the database with the commitId for the target item
            objectStateService.transition(site, path, REVERT);
            objectMetadataManager.updateCommitId(site, path, commitId);

            SiteFeed siteFeed = siteService.getSite(site);
            AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
            auditLog.setOperation(OPERATION_REVERT);
            auditLog.setSiteId(siteFeed.getId());
            auditLog.setActorId(securityService.getCurrentUser());
            auditLog.setPrimaryTargetId(site + ":" + path);
            auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLog.setPrimaryTargetValue(path);
            auditLog.setPrimaryTargetSubtype(getContentTypeClass(site, path));
            auditServiceInternal.insertAuditLog(auditLog);

            contentRepository.insertGitLog(site, commitId, 1, 1);
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
    @ValidateParams
    public InputStream getContentVersion(@ValidateStringParam(name = "site") String site,
                                         @ValidateSecurePathParam(name = "path") String path,
                                         @ValidateStringParam(name = "version") String version)
            throws ContentNotFoundException {
        return _contentRepository.getContentVersion(site, path, version);
    }

    @Override
    @ValidateParams
    public String getContentVersionAsString(@ValidateStringParam(name = "site") String site,
                                            @ValidateSecurePathParam(name = "path") String path,
                                            @ValidateStringParam(name = "version") String version)
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
    @ValidateParams
    public ContentItemTO createDummyDmContentItemForDeletedNode(@ValidateStringParam(name = "site") String site,
                                                                @ValidateSecurePathParam(name = "relativePath")
                                                                        String relativePath) {
        // TODO: SJ: Think of another way to do this in 3.1+

        ContentItemTO item = new ContentItemTO();
        String timeZone = servicesConfig.getDefaultTimezone(site);
        item.timezone = timeZone;
        String name = ContentUtils.getPageName(relativePath);
        String folderPath = (name.equals(DmConstants.INDEX_FILE)) ?
                relativePath.replace(FILE_SEPARATOR + name, "") : relativePath;
        item.path = folderPath;
        /**
         * Internal name should be just folder name
         */
        String internalName = folderPath;
        int index = folderPath.lastIndexOf(FILE_SEPARATOR);
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
        if (contentTypeClass.equals(CONTENT_TYPE_COMPONENT)) {
            item.component = true;
        } else if (contentTypeClass.equals(CONTENT_TYPE_DOCUMENT)) {
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
        browserUri = browserUri.replaceFirst(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
        if (browserUri.length() == 0) {
            browserUri = FILE_SEPARATOR;
        }
        // TODO: come up with a better way of doing this.
        if (isPage) {
            browserUri = browserUri.replaceFirst("\\.xml", ".html");
        }
        return browserUri;
    }

    @Override
    @ValidateParams
    public String getContentTypeClass(@ValidateStringParam(name = "site") String site, String uri) {
        // TODO: SJ: This reads: if can't guess what it is, it's a page. This is to be replaced in 3.1+
        if (matchesPatterns(uri, servicesConfig.getPagePatterns(site))) {
            return CONTENT_TYPE_PAGE;
        } else if (matchesPatterns(uri, servicesConfig.getComponentPatterns(site)) ||
                uri.endsWith(FILE_SEPARATOR + servicesConfig.getLevelDescriptorName(site))) {
            return CONTENT_TYPE_COMPONENT;
        } else if (matchesPatterns(uri, servicesConfig.getDocumentPatterns(site))) {
            return CONTENT_TYPE_DOCUMENT;
        } else if (matchesPatterns(uri, servicesConfig.getAssetPatterns(site))) {
            return CONTENT_TYPE_ASSET;
        } else if (matchesPatterns(uri, servicesConfig.getRenderingTemplatePatterns(site))) {
            return CONTENT_TYPE_RENDERING_TEMPLATE;
        } else if (StringUtils.startsWith(uri, contentTypeService.getConfigPath())) {
            return CONTENT_TYPE_CONTENT_TYPE;
        } else if (matchesPatterns(uri, Arrays.asList(CONTENT_TYPE_TAXONOMY_REGEX))) {
            return CONTENT_TYPE_TAXONOMY;
        } else if (matchesPatterns(uri, servicesConfig.getScriptsPatterns(site))) {
            return CONTENT_TYPE_SCRIPT;
        }

        return CONTENT_TYPE_UNKNOWN;
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
    @ValidateParams
    public ResultTO processContent(@ValidateStringParam(name = "id") String id, InputStream input, boolean isXml,
                                   Map<String, String> params,
                                   @ValidateStringParam(name = "contentChainForm") String contentChainForm)
            throws ServiceLayerException {
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
    @ValidateParams
    public String getNextAvailableName(@ValidateStringParam(name = "site") String site,
                                       @ValidateSecurePathParam(name = "path") String path) {
        // TODO: SJ: Refactor to be faster, and make it work regardless (seems to fail above 10) in 3.1+
        String[] levels = path.split(FILE_SEPARATOR);
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
                    String originalName = (item.isFolder() ||
                            item.isContainer()) ? name : name.substring(0, lastIndex);
                    List<ContentItemTO> children = parentItem.getChildren();
                    // pattern matching doesn't work here
                    // String childNamePattern = originalName + "%" + ext;
                    int lastNumber = 0;
                    String namePattern = originalName + "-[0-9]+" + ext;
                    if (children != null && children.size() > 0) {
                        // since it is already sorted, we only care about the last matching item
                        for (ContentItemTO child : children) {
                            if (((item.isFolder() || item.isContainer()) == (child.isFolder() ||
                                    child.isContainer()))) {
                                String childName = child.getName();
                                if ((child.isFolder() || child.isContainer())) {
                                    childName = ContentUtils.getPageName(child.getBrowserUri());
                                }
                                if (childName.matches(namePattern)) {
                                    Pattern pattern = (item.isFolder() ||
                                            item.isContainer()) ? COPY_FOLDER_PATTERN : COPY_FILE_PATTERN;
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
    public GoLiveDeleteCandidates getDeleteCandidates(@ValidateStringParam(name = "site") String site,
                                                      @ValidateSecurePathParam(name = "relativePath")
                                                              String relativePath) throws ServiceLayerException {
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
    protected void childDeleteItems(String site, ContentItemTO contentItem, GoLiveDeleteCandidates items)
            throws ServiceLayerException {
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
        } else {
            addDependenciesToDelete(site, contentItem.getUri(), contentItem.getUri(), items);
            addRemovedDependenicesToDelete(site, contentItem.getUri(), items);
        }
        //add the child path
        items.getPaths().add(contentItem.getUri());
    }

    protected void addDependenciesToDelete(String site, String sourceContentPath, String dependencyPath,
                                           GoLiveDeleteCandidates candidates) throws ServiceLayerException {
        Set<String> dependencyParentFolder = new HashSet<String>();
        //add dependencies as well
        Set<String> dependencies = dependencyService.getDeleteDependencies(site, sourceContentPath);
        for (String dependency : dependencies) {
            candidates.addDependency(dependency);
            logger.debug("Added to delete" + dependency);
        }

        //Find if any folder would get empty if remove the items and add just the folder
        for (String parentFolderToDelete : dependencyParentFolder) {
            RepositoryItem[] children = _contentRepository.getContentChildren(site, parentFolderToDelete);
            List<String> childItems = new ArrayList<String>();
            for (RepositoryItem child : children) {
                childItems.add(child.path + "/" + child.name);
            }
            if (candidates.getAllItems().containsAll(childItems)) {
                logger.debug("Added parentFolder for delete" + parentFolderToDelete);
                candidates.addDependencyParentFolder(parentFolderToDelete);
            }
        }
    }

    protected void addRemovedDependenicesToDelete(String site, String relativePath, GoLiveDeleteCandidates candidates)
            throws ServiceLayerException {
        if (relativePath.endsWith(DmConstants.XML_PATTERN) && !objectStateService.isNew(site, relativePath)) {
            DependencyDiffService.DiffRequest diffRequest = new DependencyDiffService.DiffRequest(site, relativePath,
                    null, null, site, true);
            List<String> deleted = getRemovedDependenices(diffRequest, true);
            logger.debug("Removed dependenices for path[" + relativePath + "] : " + deleted);
            for (String dependency : deleted) {
                candidates.getLiveDependencyItems().add(dependency);
            }
        }
    }

    protected List<String> getRemovedDependenices(DependencyDiffService.DiffRequest diffRequest,
                                                  boolean matchDeletePattern) throws ServiceLayerException {
        DependencyDiffService.DiffResponse diffResponse = dependencyDiffService.diff(diffRequest);
        List<String> removedDep = diffResponse.getRemovedDependencies();
        if(matchDeletePattern){
            removedDep = filterDependenicesMatchingDeletePattern(diffRequest.getSite(), diffRequest.getSourcePath(),
                    diffResponse.getRemovedDependencies());
        }
        return removedDep;
    }

    protected List<String> filterDependenicesMatchingDeletePattern(String site, String sourcePath,
                                                                   List<String> dependencies)
            throws ServiceLayerException {
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

    protected List<DeleteDependencyConfigTO> getDeletePatternConfig(String site, String relativePath,
                                                                    boolean isInLiveRepo)
            throws ServiceLayerException {
        List<DeleteDependencyConfigTO> deleteAssociations  = new ArrayList<DeleteDependencyConfigTO>();
        ContentItemTO dependencyItem = getContentItem(site, relativePath, 0);
        String contentType = dependencyItem.getContentType();
        deleteAssociations  = servicesConfig.getDeleteDependencyPatterns(site, contentType);
        return deleteAssociations;
    }

    protected List<DeleteDependencyConfigTO> getDeletePatternConfig(String site, String relativePath)
            throws ServiceLayerException {
        return getDeletePatternConfig(site,relativePath,false);
    }

    @Override
    @ValidateParams
    public void lockContent(@ValidateStringParam(name = "site") String site,
                            @ValidateSecurePathParam(name = "path") String path) {
        // TODO: SJ: Where is the object state update to indicate item is now locked?
        // TODO: SJ: Dejan to look into this
        _contentRepository.lockItem(site, path);
        objectMetadataManager.lockContent(site, path, securityService.getCurrentUser());
    }

    @Override
    @ValidateParams
    public void unLockContent(@ValidateStringParam(name = "site") String site,
                              @ValidateSecurePathParam(name = "path") String path) {
        ContentItemTO item = getContentItem(site, path, 0);
        objectStateService.transition(site, item, TransitionEvent.CANCEL_EDIT); // this unlocks too
        _contentRepository.unLockItem(site, path);
        objectMetadataManager.unLockContent(site, path);
    }

    @Override
    @ValidateParams
    public List<DmOrderTO> getItemOrders(@ValidateStringParam(name = "site") String site,
                                         @ValidateSecurePathParam(name = "path") String path)
            throws ContentNotFoundException {
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
                int index = relativePath.lastIndexOf(FILE_SEPARATOR);
                if (index > 0) {
                    String fileName = relativePath.substring(index + 1);
                    String path = relativePath.substring(0, index);
                    if (DmConstants.INDEX_FILE.equals(fileName)) {
                        int secondIndex = path.lastIndexOf(FILE_SEPARATOR);
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
            String pathIndex = relativePath + FILE_SEPARATOR + DmConstants.INDEX_FILE;
            for (ContentItemTO child : item.getChildren()) {
                // exclude index.xml, the level descriptor and floating pages at the path
                if (!(pathIndex.equals(child.getUri()) || child.isLevelDescriptor() || child.isDeleted()) &&
                        (!child.isFloating() || includeFloating)) {
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
    public double reorderItems(@ValidateStringParam(name = "site") String site,
                               @ValidateSecurePathParam(name = "relativePath") String relativePath,
                               @ValidateSecurePathParam(name = "before") String before,
                               @ValidateSecurePathParam(name = "after") String after,
                               @ValidateStringParam(name = "orderName") String orderName) throws ServiceLayerException {
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
            return dmPageNavigationOrderService.getNewNavOrder(site,
                    ContentUtils.getParentUrl(relativePath.replace(FILE_SEPARATOR +
                            DmConstants.INDEX_FILE, "")));
        } else if (beforeOrder == null) {
            return (0 + afterOrder) / 2;
        } else if (afterOrder == null) {
            logger.info("afterOrder == null");
            return dmPageNavigationOrderService.getNewNavOrder(site,
                    ContentUtils.getParentUrl(relativePath.replace(FILE_SEPARATOR +
                            DmConstants.INDEX_FILE, "")), beforeOrder);
        } else {
            //return (beforeOrder + afterOrder) / 2;
            return computeReorder(site, relativePath, beforeOrderTO, afterOrderTO, orderName);
        }
    }

    /**
     * Will need to include the floating pages as well for orderValue computation
     * Since the beforeOrder and afterOrder in the UI does not include floating pages will need to do special processing
     */
    protected double computeReorder(String site, String relativePath, DmOrderTO beforeOrderTO, DmOrderTO afterOrderTO,
                                    String orderName) throws ContentNotFoundException {
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

    @Override
    @ValidateParams
    public boolean renameFolder(@ValidateStringParam(name = "site") String site,
                                @ValidateSecurePathParam(name = "path") String path,
                                @ValidateStringParam(name = "name") String name) throws ServiceLayerException {
        boolean toRet = false;

        String parentPath = FILE_SEPARATOR + FilenameUtils.getPathNoEndSeparator(path);
        String targetPath = parentPath + FILE_SEPARATOR + name;

        if (contentExists(site, targetPath)) {
            Map<String,String> ids = contentItemIdGenerator.getIds();
            String id = ids.get(KEY_PAGE_GROUP_ID);
            targetPath += "-" + id;
        }

        logger.debug("Rename folder for site {0} sourcePath {3} to target path {4}", site, path, targetPath);
        // NOTE: IN WRITE SCENARIOS the repository OP IS PART of this PIPELINE, for some reason,
        // historically with MOVE it is not
        Map<String, String> commitIds = _contentRepository.moveContent(site, path, targetPath);

        if (commitIds != null) {
            // Update the database with the commitId for the target item
            updateDatabaseOnMove(site, path, targetPath);
            updateChildrenOnMove(site, path, targetPath);
            for (Map.Entry<String, String> entry : commitIds.entrySet()) {
                objectMetadataManager.updateCommitId(site, FILE_SEPARATOR + entry.getKey(), entry.getValue());
                contentRepository.insertGitLog(site, entry.getValue(), 1);
            }
            siteService.updateLastCommitId(site, _contentRepository.getRepoLastCommitId(site));

            PreviewEventContext context = new PreviewEventContext();
            context.setSite(site);
            eventService.publish(EVENT_PREVIEW_SYNC, context);
             toRet = true;

        } else {
            logger.error("Repository move failed site {0} from {1} to {2}", site, path, targetPath);
        }

        return toRet;
    }

    @Override
    public boolean pushToRemote(String siteId, String remoteName, String remoteBranch)
            throws ServiceLayerException, InvalidRemoteUrlException, AuthenticationException, CryptoException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException();
        }
        boolean toRet = _contentRepository.pushToRemote(siteId, remoteName, remoteBranch);
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_PUSH_TO_REMOTE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(userService.getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(remoteName + "/" + remoteBranch);
        auditLog.setPrimaryTargetType(TARGET_TYPE_REMOTE_REPOSITORY);
        auditLog.setPrimaryTargetValue(remoteName + "/" + remoteBranch);
        auditServiceInternal.insertAuditLog(auditLog);

        return toRet;
    }

    @Override
    public boolean pullFromRemote(String siteId, String remoteName, String remoteBranch)
            throws ServiceLayerException, InvalidRemoteUrlException, AuthenticationException, CryptoException {
        if (!siteService.exists(siteId)) {
            throw new SiteNotFoundException(siteId);
        }
        boolean toRet = _contentRepository.pullFromRemote(siteId, remoteName, remoteBranch);
        SiteFeed siteFeed = siteService.getSite(siteId);
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(OPERATION_PULL_FROM_REMOTE);
        auditLog.setSiteId(siteFeed.getId());
        auditLog.setActorId(userService.getCurrentUser().getUsername());
        auditLog.setPrimaryTargetId(remoteName + "/" + remoteBranch);
        auditLog.setPrimaryTargetType(TARGET_TYPE_REMOTE_REPOSITORY);
        auditLog.setPrimaryTargetValue(remoteName + "/" + remoteBranch);
        auditServiceInternal.insertAuditLog(auditLog);
        
        return toRet;
    }

    public ContentRepository getContentRepository() {
        return _contentRepository;
    }
    public void setContentRepository(ContentRepository contentRepository) {
        this._contentRepository = contentRepository;
    }

    public ServicesConfig getServicesConfig() {
        return servicesConfig;
    }
    public void setServicesConfig(ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }
    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public ObjectStateService getObjectStateService() {
        return objectStateService;
    }
    public void setObjectStateService(ObjectStateService objectStateService) {
        this.objectStateService = objectStateService;
    }

    public DependencyService getDependencyService() {
        return dependencyService;
    }
    public void setDependencyService(DependencyService dependencyService) {
        this.dependencyService = dependencyService;
    }

    public ProcessContentExecutor getContentProcessor() {
        return contentProcessor;
    }
    public void setContentProcessor(ProcessContentExecutor contentProcessor) {
        this.contentProcessor = contentProcessor;
    }

    public ObjectMetadataManager getObjectMetadataManager() {
        return objectMetadataManager;
    }
    public void setObjectMetadataManager(ObjectMetadataManager objectMetadataManager) {
        this.objectMetadataManager = objectMetadataManager;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }
    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public DmPageNavigationOrderService getDmPageNavigationOrderService() {
        return dmPageNavigationOrderService;
    }
    public void setDmPageNavigationOrderService(DmPageNavigationOrderService dmPageNavigationOrderService) {
        this.dmPageNavigationOrderService = dmPageNavigationOrderService;
    }

    public DmContentLifeCycleService getDmContentLifeCycleService() {
        return dmContentLifeCycleService;
    }
    public void setDmContentLifeCycleService(DmContentLifeCycleService dmContentLifeCycleService) {
        this.dmContentLifeCycleService = dmContentLifeCycleService;
    }

    public EventService getEventService() {
        return eventService;
    }
    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public SiteService getSiteService() {
        return siteService;
    }
    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public ContentItemIdGenerator getContentItemIdGenerator() {
        return contentItemIdGenerator;
    }
    public void setContentItemIdGenerator(ContentItemIdGenerator contentItemIdGenerator) {
        this.contentItemIdGenerator = contentItemIdGenerator;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }
    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public DependencyDiffService getDependencyDiffService() {
        return dependencyDiffService;
    }
    public void setDependencyDiffService(DependencyDiffService dependencyDiffService) {
        this.dependencyDiffService = dependencyDiffService;
    }

    public ContentTypeService getContentTypeService() {
        return contentTypeService;
    }
    public void setContentTypeService(ContentTypeService contentTypeService) {
        this.contentTypeService = contentTypeService;
    }

    public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
        this.entitlementValidator = entitlementValidator;
    }

    public AuditServiceInternal getAuditServiceInternal() {
        return auditServiceInternal;
    }

    public void setAuditServiceInternal(AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public org.craftercms.studio.api.v2.repository.ContentRepository getContentRepositoryV2() {
        return this.contentRepository;
    }

    public void setContentRepositoryV2(org.craftercms.studio.api.v2.repository.ContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }
}