/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.content.internal;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.event.content.DeleteContentEvent;
import org.craftercms.studio.api.v2.event.lock.LockContentEvent;
import org.craftercms.studio.api.v2.exception.content.ContentAlreadyUnlockedException;
import org.craftercms.studio.api.v2.exception.content.ContentLockedByAnotherUserException;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.security.SemanticsAvailableActionsResolver;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentServiceInternal;
import org.craftercms.studio.api.v2.service.content.internal.ContentTypeServiceInternal;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.history.ItemVersion;
import org.craftercms.studio.model.rest.content.DetailedItem;
import org.craftercms.studio.model.rest.content.GetChildrenBulkRequest.PathParams;
import org.craftercms.studio.model.rest.content.GetChildrenByPathsBulkResult;
import org.craftercms.studio.model.rest.content.GetChildrenByPathsBulkResult.ChildrenByPathResult;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.SandboxItem;
import org.dom4j.Document;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.util.MimeType;

import java.io.IOException;
import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.collections4.CollectionUtils.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.api.v2.utils.DalUtils.mapSortFields;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONTENT_ITEM_EDITABLE_TYPES;

public class ContentServiceInternalImpl implements ContentServiceInternal, ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(ContentServiceInternalImpl.class);

    private GitContentRepository contentRepository;
    private ItemDAO itemDao;
    private ServicesConfig servicesConfig;
    private SecurityService securityService;
    private StudioConfiguration studioConfiguration;
    private SemanticsAvailableActionsResolver semanticsAvailableActionsResolver;
    private AuditServiceInternal auditServiceInternal;
    private DependencyService dependencyServiceInternal;
    private ContentTypeServiceInternal contentTypeServiceInternal;
    private UserServiceInternal userServiceInternal;
    private SitesService siteService;
    private ItemServiceInternal itemServiceInternal;
    private GeneralLockService generalLockService;
    private ApplicationEventPublisher eventPublisher;
    private org.craftercms.studio.api.v1.service.content.ContentService contentServiceV1;
    private PublishService publishServiceInternal;
    private ProcessedCommitsDAO processedCommitsDao;

    @Override
    public boolean contentExists(String siteId, String path) {
        return contentRepository.contentExists(siteId, path);
    }

    @Override
    public boolean shallowContentExists(String siteId, String path) {
        return contentRepository.shallowContentExists(siteId, path);
    }

    @Override
    public List<String> getSubtreeItems(String siteId, String path) {
        return contentRepository.getSubtreeItems(siteId, path);
    }

    @Override
    public List<String> getSubtreeItems(String siteId, List<String> paths) {
        List<String> subtreeItems = new ArrayList<>();
        for (String path : paths) {
            subtreeItems.addAll(contentRepository.getSubtreeItems(siteId, path));
        }
        return subtreeItems;
    }

    @Override
    public GetChildrenResult getChildrenByPath(String siteId, String path, String locale, String keyword,
                                               List<String> systemTypes, List<String> excludes, String sortStrategy,
                                               String order, int offset, int limit)
            throws ServiceLayerException, UserNotFoundException {
        if (!contentRepository.contentExists(siteId, path)) {
            throw new ContentNotFoundException(path, siteId, "Content not found at path " + path + " site " + siteId);
        }
        String parentFolderPath = StringUtils.replace(path, FILE_SEPARATOR + INDEX_FILE, "");
        Site site = siteService.getSite(siteId);
        int total = itemDao.getChildrenByPathTotal(site.getId(), parentFolderPath, locale, keyword, systemTypes,
                List.of(CONTENT_TYPE_LEVEL_DESCRIPTOR), excludes);
        List<Item> resultSet = itemDao.getChildrenByPath(site.getId(), parentFolderPath,
                locale, keyword, systemTypes, List.of(CONTENT_TYPE_LEVEL_DESCRIPTOR), excludes, sortStrategy, order, offset, limit);
        GetChildrenResult toRet = processResultSet(siteId, resultSet);
        toRet.setLevelDescriptor(getLevelDescriptor(site, path, locale, keyword));
        toRet.setOffset(offset);
        toRet.setLimit(limit);
        toRet.setTotal(total);
        return toRet;
    }

    private SandboxItem getLevelDescriptor(final Site site, final String path, final String locale, final String keyword) throws UserNotFoundException, ServiceLayerException {
        List<Item> sandboxItemsByPath = itemDao.getChildrenByPath(site.getId(), path,
                locale, keyword, List.of(CONTENT_TYPE_LEVEL_DESCRIPTOR), null, null, null, null, 0, 1);
        if (isEmpty(sandboxItemsByPath)) {
            return null;
        }
        Item levelDescriptorItem = sandboxItemsByPath.getFirst();
        String user = securityService.getCurrentUser();
        levelDescriptorItem.setAvailableActions(
                semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, site.getSiteId(), levelDescriptorItem));
        return SandboxItem.getInstance(levelDescriptorItem);
    }

    @Override
    public GetChildrenByPathsBulkResult getChildrenByPaths(String siteId, List<String> paths,
                                                           Map<String, PathParams> pathParams) throws UserNotFoundException, ServiceLayerException {
        List<ChildrenByPathResult> resultItems = new ArrayList<>(paths.size());

        Map<String, SandboxItem> sandboxItemsByPath = getSandboxItemsByPath(siteId, paths, true).stream()
                .collect(toMap(SandboxItem::getPath, identity()));
        List<String> missingItems = new LinkedList<>();
        for (PathParams params : pathParams.values()) {
            try {
                ChildrenByPathResult resultItem = new ChildrenByPathResult();
                resultItem.setPath(params.getPath());

                GetChildrenResult children = getChildrenByPath(siteId, params.getPath(), params.getLocaleCode(),
                        params.getKeyword(), params.getSystemTypes(), params.getExcludes(), params.getSortStrategy(),
                        params.getOrder(), params.getOffset(), params.getLimit());
                resultItem.setResult(children);
                resultItem.setItem(sandboxItemsByPath.get(params.getPath()));
                resultItems.add(resultItem);
            } catch (ContentNotFoundException e) {
                logger.error(format("Content not found at path %s site %s", params.getPath(), siteId), e);
                missingItems.add(params.getPath());
            }
        }
        return new GetChildrenByPathsBulkResult(resultItems, missingItems);
    }

    private GetChildrenResult processResultSet(String siteId, List<Item> resultSet)
            throws ServiceLayerException, UserNotFoundException {
        GetChildrenResult toRet = new GetChildrenResult();
        List<SandboxItem> children = new ArrayList<>(resultSet.size());
        toRet.setChildren(children);
        if (!isNotEmpty(resultSet)) {
            return toRet;
        }
        String user = securityService.getCurrentUser();
        for (Item child : resultSet) {
            child.setAvailableActions(
                    semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, child));
            children.add(SandboxItem.getInstance(child));
        }
        return toRet;
    }

    @Override
    public org.craftercms.core.service.Item getItem(String siteId, String path, boolean flatten) {
        return contentRepository.getItem(siteId, path, flatten);
    }

    @Override
    public long getContentSize(String siteId, String path) {
        return contentRepository.getContentSize(siteId, path);
    }

    @Override
    public List<DetailedItem> getItemsByStates(String siteId, long statesBitMap, List<String> systemTypes, List<SortField> sortFields, int offset, int limit) throws UserNotFoundException, ServiceLayerException {
        Site site = siteService.getSite(siteId);
        String stagingEnv = servicesConfig.getStagingEnvironment(siteId);
        String liveEnv = servicesConfig.getLiveEnvironment(siteId);
        List< org.craftercms.studio.api.v2.dal.DetailedItem> items = itemDao.getDetailedItemsByStates(site.getId(), statesBitMap,
                systemTypes, mapSortFields(sortFields, ItemDAO.DETAILED_ITEM_SORT_FIELD_MAP), stagingEnv, liveEnv, offset, limit);
        List<DetailedItem> result = new ArrayList<>();
        for (org.craftercms.studio.api.v2.dal.DetailedItem item : items) {
            DetailedItem detailedItem = DetailedItem.getInstance(item);
            populateDetailedItemPropertiesFromRepository(siteId, detailedItem);
            result.add(detailedItem);
        }
        return result;
    }

    @Override
    public DetailedItem getItemByPath(String siteId, String path, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        if (!contentRepository.contentExists(siteId, path)) {
            throw new ContentNotFoundException(path, siteId, format("Content not found at path '%s' site '%s'", path, siteId));
        }
        Site site = siteService.getSite(siteId);
        org.craftercms.studio.api.v2.dal.DetailedItem item = null;
        String stagingEnv = servicesConfig.getStagingEnvironment(siteId);
        String liveEnv = servicesConfig.getLiveEnvironment(siteId);
        if (preferContent) {
            item = itemDao.getItemBySiteIdAndPathPreferContent(site.getId(), path, stagingEnv, liveEnv);
        } else {
            item = itemDao.getItemBySiteIdAndPath(site.getId(), path, stagingEnv, liveEnv);
        }
        if (item == null) {
            throw new ContentNotFoundException(path, siteId, format("Content not found at path '%s' site '%s'", path, siteId));
        }
        DetailedItem detailedItem = DetailedItem.getInstance(item);
        populateDetailedItemPropertiesFromRepository(siteId, detailedItem);
        return detailedItem;
    }

    private void populateDetailedItemPropertiesFromRepository(String siteId, DetailedItem detailedItem)
            throws ServiceLayerException, UserNotFoundException {
        if (Objects.nonNull(detailedItem)) {
            String user = securityService.getCurrentUser();
            detailedItem.setAvailableActions(
                    semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, detailedItem));
        }
    }

    @Override
    public List<SandboxItem> getSandboxItemsByPath(String siteId, List<String> paths, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        Site site = siteService.getSite(siteId);
        List<Item> items = itemDao.getSandboxItemsByPath(site.getId(), paths, preferContent);
        return calculatePossibleActions(siteId, items);
    }

    @Override
    public List<SandboxItem> getSandboxItemsById(String siteId, List<Long> ids, List<SortField> sortFields, boolean preferContent)
            throws ServiceLayerException, UserNotFoundException {
        List<Item> items;
        if (preferContent) {
            items = itemDao.getSandboxItemsByIdPreferContent(ids, mapSortFields(sortFields, ItemDAO.SORT_FIELD_MAP));
        } else {
            items = itemDao.getSandboxItemsById(ids, mapSortFields(sortFields, ItemDAO.SORT_FIELD_MAP));
        }
        return calculatePossibleActions(siteId, items);
    }

    private List<SandboxItem> calculatePossibleActions(String siteId, List<Item> items)
            throws ServiceLayerException, UserNotFoundException {
        if (isEmpty(items)) {
            return emptyList();
        }
        List<SandboxItem> toRet = new ArrayList<>();
        String user = securityService.getCurrentUser();
        for (Item item : items) {
            if (!contentRepository.contentExists(siteId, item.getPath())) {
                logger.warn("Content not found in site '{}' path '{}'", siteId, item.getPath());
            } else {
                item.setAvailableActions(
                        semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, item));
                toRet.add(SandboxItem.getInstance(item));
            }
        }
        return toRet;
    }

    @Override
    public boolean isEditable(String itemPath, String mimeType) {
        List<String> editableMimeTypes =
                Arrays.asList(studioConfiguration.getArray(CONTENT_ITEM_EDITABLE_TYPES, String.class));

        MimeType itemMimeType;
        if (StringUtils.isEmpty(mimeType)) {
            itemMimeType = MimeType.valueOf(StudioUtils.getMimeType(itemPath));
        } else {
            itemMimeType = MimeType.valueOf(mimeType);
        }

        return editableMimeTypes.stream()
                .anyMatch(type -> (MimeType.valueOf(type)).isCompatibleWith(itemMimeType));
    }

    @Override
    public void itemLockByPath(String siteId, String path) {
        contentRepository.lockItem(siteId, path);
    }

    @Override
    public void itemUnlockByPath(String siteId, String path) {
        contentRepository.itemUnlock(siteId, path);
    }

    @Override
    public Optional<Resource> getContentByCommitId(String siteId, String path, String commitId)
            throws ContentNotFoundException {
        return contentRepository.getContentByCommitId(siteId, path, commitId);
    }

    @Override
    public List<ItemVersion> getContentVersionHistory(String siteId, String path) throws ServiceLayerException {
        try {
            List<ItemVersion> history = contentRepository.getContentItemHistory(siteId, path);
            for (ItemVersion itemVersion : history) {
                if (itemVersion.getVersionNumber() != null) {
                    itemVersion.setAuthor(auditServiceInternal.getAuthor(itemVersion.getVersionNumber()));
                }
            }
            return history;
        } catch (IOException | GitAPIException e) {
            throw new ServiceLayerException(format("Error getting content version history for site '%s' path '%s'", siteId, path), e);
        }
    }

    @Override
    public List<QuickCreateItem> getQuickCreatableContentTypes(String siteId) throws SiteNotFoundException {
        return contentTypeServiceInternal.getQuickCreatableContentTypes(siteId);
    }

    @Override
    public List<String> getChildItems(String siteId, List<String> paths) throws SiteNotFoundException {
        List<String> subtreeItems = getSubtreeItems(siteId, paths);
        List<String> childItems = new ArrayList<>();
        childItems.addAll(subtreeItems);
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, paths));
        childItems.addAll(dependencyServiceInternal.getItemSpecificDependencies(siteId, subtreeItems));
        return childItems;
    }

    @Override
    public long deleteContent(String siteId, List<String> paths, String submissionComment)
            throws ServiceLayerException, AuthenticationException, UserNotFoundException {
        String syncFromRepoLockKey = StudioUtils.getSyncFromRepoLockKey(siteId);
        generalLockService.lock(syncFromRepoLockKey);
        try {
            AuthenticatedUser currentUser = userServiceInternal.getCurrentUser();
            itemServiceInternal.setSystemProcessingBulk(siteId, paths, true);
            Site site = siteService.getSite(siteId);
            List<String> children = paths.stream()
                    .map(path -> contentRepository.getSubtreeItems(siteId, path))
                    .flatMap(List::stream)
                    .toList();
            itemServiceInternal.setSystemProcessingBulk(siteId, children, true);

            Collection<String> userRequested = union(paths, children);
            List<String> dependencies = dependencyServiceInternal.getItemSpecificDependencies(siteId, paths);
            itemServiceInternal.setSystemProcessingBulk(siteId, dependencies, true);

            Collection<String> allPaths = union(userRequested, dependencies);

            String commitId = contentRepository.deleteContent(siteId, allPaths, currentUser.getUsername());
            processedCommitsDao.insertCommit(site.getId(), commitId);

            long publishPackageId = 0;
            if (contentRepository.publishedRepositoryExists(siteId)) {
                // TODO: consider item_target previous path and send to delete the actual published path
                publishPackageId = publishServiceInternal.publishDelete(siteId, userRequested, dependencies, submissionComment);
            }

            for (String path : allPaths) {
                dependencyServiceInternal.deleteItemDependencies(siteId, path);
                dependencyServiceInternal.invalidateDependencies(siteId, path);
                itemServiceInternal.deleteItem(site.getId(), path);
            }

            insertDeleteContentApprovedActivity(site, currentUser.getUsername(), allPaths);

            Authentication auth = securityService.getAuthentication();
            for (String path : paths) {
                eventPublisher.publishEvent(new DeleteContentEvent(auth, siteId, path));
            }
            return publishPackageId;
        } finally {
            generalLockService.unlock(syncFromRepoLockKey);
        }
    }

    private void insertDeleteContentApprovedActivity(Site site, String approver, Collection<String> paths) {
        AuditLog auditLog = auditServiceInternal.createAuditLogEntry();
        auditLog.setOperation(AuditLogConstants.OPERATION_APPROVE);
        auditLog.setActorId(approver);
        auditLog.setSiteId(site.getId());
        auditLog.setPrimaryTargetId(site.getSiteId());
        auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLog.setPrimaryTargetValue(site.getSiteId());
        List<AuditLogParameter> auditLogParameters = new ArrayList<>();
        for (String itemToDelete : paths) {
            AuditLogParameter auditLogParameter = new AuditLogParameter();
            auditLogParameter.setTargetId(site.getSiteId() + ":" + itemToDelete);
            auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
            auditLogParameter.setTargetValue(itemToDelete);
            auditLogParameters.add(auditLogParameter);
        }
        auditLog.setParameters(auditLogParameters);
        auditServiceInternal.insertAuditLog(auditLog);
    }

    @Override
    public Document getItemDescriptor(String siteId, String path, boolean flatten) throws SiteNotFoundException, ContentNotFoundException {
        try {
            org.craftercms.core.service.Item item = getItem(siteId, path, flatten);
            Document descriptor = item.getDescriptorDom();
            if (descriptor == null) {
                throw new ContentNotFoundException(path, siteId, format("No descriptor found for '%s' in site '%s'", path, siteId));
            }
            return descriptor;
        } catch (PathNotFoundException e) {
            logger.error("Content not found for site '{}' at path '{}'", siteId, path, e);
            throw new ContentNotFoundException(path, siteId, format("Content not found in site '%s' at path '%s'", siteId, path));
        }
    }

    @Override
    public void lockContent(String siteId, String path) throws UserNotFoundException, ServiceLayerException {
        generalLockService.lockContentItem(siteId, path);
        try {
            var item = itemServiceInternal.getItem(siteId, path);
            if (Objects.isNull(item)) {
                throw new ContentNotFoundException(path, siteId, format("Content not found in site '%s' at path '%s'",
                        siteId, path));
            }
            var username = securityService.getCurrentUser();
            if (Objects.isNull(item.getLockOwner())) {
                itemLockByPath(siteId, path);
                itemServiceInternal.lockItemByPath(siteId, path, username);
                eventPublisher.publishEvent(
                        new LockContentEvent(securityService.getAuthentication(), siteId, path, true));
            } else {
                if (!StringUtils.equals(item.getLockOwner().getUsername(), username)) {
                    throw new ContentLockedByAnotherUserException(item.getLockOwner().getUsername());
                }
            }
        } finally {
            generalLockService.unlockContentItem(siteId, path);
        }
    }

    @Override
    public void unlockContent(String siteId, String path) throws ContentNotFoundException, ContentAlreadyUnlockedException, SiteNotFoundException {
        logger.debug("Unlock item in site '{}' path '{}'", siteId, path);
        generalLockService.lockContentItem(siteId, path);
        try {
            var item = itemServiceInternal.getItem(siteId, path);
            if (Objects.isNull(item)) {
                logger.debug("Item not found in site '{}' path '{}'", siteId, path);
                throw new ContentNotFoundException(path, siteId, format("Item not found in site '%s' path '%s'", siteId, path));
            }
            if (Objects.isNull(item.getLockOwner())) {
                logger.debug("Item in site '{}' path '{}' is already unlocked", siteId, path);
                throw new ContentAlreadyUnlockedException();
            }
            itemUnlockByPath(siteId, path);
            itemServiceInternal.unlockItemByPath(siteId, path);
            logger.debug("Item in site '{}' path '{}' successfully unlocked", siteId, path);
            eventPublisher.publishEvent(
                    new LockContentEvent(securityService.getAuthentication(), siteId, path, false));
        } finally {
            generalLockService.unlockContentItem(siteId, path);
        }
    }

    @Override
    public boolean renameContent(String site, String path, String name) throws ServiceLayerException, UserNotFoundException {
        logger.debug("rename path {} to new name {} for site {}", path, name, site);
        return contentServiceV1.renameContent(site, path, name);
    }

    @Override
    public Resource getContentAsResource(String site, String path) throws ContentNotFoundException {
        return contentServiceV1.getContentAsResource(site, path);
    }

    public void setContentRepository(final GitContentRepository contentRepository) {
        this.contentRepository = contentRepository;
    }

    public void setItemDao(final ItemDAO itemDao) {
        this.itemDao = itemDao;
    }

    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public void setSecurityService(final SecurityService securityService) {
        this.securityService = securityService;
    }

    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    public void setSemanticsAvailableActionsResolver(final SemanticsAvailableActionsResolver semanticsAvailableActionsResolver) {
        this.semanticsAvailableActionsResolver = semanticsAvailableActionsResolver;
    }

    public void setAuditServiceInternal(final AuditServiceInternal auditServiceInternal) {
        this.auditServiceInternal = auditServiceInternal;
    }

    @Override
    public void setApplicationEventPublisher(final @NotNull ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void setContentTypeServiceInternal(final ContentTypeServiceInternal contentTypeServiceInternal) {
        this.contentTypeServiceInternal = contentTypeServiceInternal;
    }

    public void setDependencyServiceInternal(final DependencyService dependencyServiceInternal) {
        this.dependencyServiceInternal = dependencyServiceInternal;
    }

    public void setUserServiceInternal(final UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public void setSiteService(final SitesService siteService) {
        this.siteService = siteService;
    }

    public void setItemServiceInternal(final ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }

    public void setGeneralLockService(final GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public void setContentServiceV1(final org.craftercms.studio.api.v1.service.content.ContentService contentService) {
        this.contentServiceV1 = contentService;
    }

    public void setPublishServiceInternal(final PublishService publishServiceInternal) {
        this.publishServiceInternal = publishServiceInternal;
    }

    public void setProcessedCommitsDao(final ProcessedCommitsDAO processedCommitsDao) {
        this.processedCommitsDao = processedCommitsDao;
    }
}
