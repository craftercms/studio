/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.commons.security.exception.ActionDeniedException;
import org.craftercms.commons.security.permissions.PermissionEvaluator;
import org.craftercms.commons.validation.ValidationException;
import org.craftercms.core.exception.PathNotFoundException;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.DmPageNavigationOrderService;
import org.craftercms.studio.api.v2.content.ContentLifeCycle;
import org.craftercms.studio.api.v2.content.LifeCycleContent;
import org.craftercms.studio.api.v2.content.LifeCycleContent.ContentLifeCycleItem;
import org.craftercms.studio.api.v2.content.LifeCycleContent.LifeCycleOperation;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.dal.item.ContentItem;
import org.craftercms.studio.api.v2.dal.item.LightItem;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.event.content.ContentEvent;
import org.craftercms.studio.api.v2.event.content.DeleteContentEvent;
import org.craftercms.studio.api.v2.event.lock.LockContentEvent;
import org.craftercms.studio.api.v2.exception.content.ContentInPublishQueueException;
import org.craftercms.studio.api.v2.exception.content.ContentLockedByAnotherUserException;
import org.craftercms.studio.api.v2.exception.content.EmptyChangesetException;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.security.SemanticsAvailableActionsResolver;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.content.ContentService;
import org.craftercms.studio.api.v2.service.dependency.DependencyService;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.api.v2.utils.function.ThrowingRunnable;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.craftercms.studio.model.AuthenticatedUser;
import org.craftercms.studio.model.history.ItemVersion;
import org.craftercms.studio.model.rest.Person;
import org.craftercms.studio.model.rest.content.GetChildrenBulkRequest.PathParams;
import org.craftercms.studio.model.rest.content.GetChildrenByPathsBulkResult;
import org.craftercms.studio.model.rest.content.GetChildrenByPathsBulkResult.ChildrenByPathResult;
import org.craftercms.studio.model.rest.content.GetChildrenResult;
import org.craftercms.studio.model.rest.content.WriteContentResult;
import org.craftercms.studio.model.rest.content.WriteContentResult.WriteContentResultItem;
import org.dom4j.Document;
import org.dom4j.DocumentException;
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
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Comparator.naturalOrder;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.*;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.collections4.ListUtils.union;
import static org.apache.commons.io.file.PathUtils.getBaseName;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.craftercms.studio.api.v1.constant.DmConstants.ROOT_PATTERN_PAGES;
import static org.craftercms.studio.api.v1.constant.DmConstants.SLASH_INDEX_FILE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.content.LifeCycleContent.LifeCycleOperation.NEW;
import static org.craftercms.studio.api.v2.content.LifeCycleContent.LifeCycleOperation.UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLog.createAuditLogEntry;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_CONTENT_ITEM;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SITE;
import static org.craftercms.studio.api.v2.utils.DalUtils.mapSortFields;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONTENT_ITEM_EDITABLE_TYPES;
import static org.craftercms.studio.api.v2.utils.StudioUtils.*;
import static org.craftercms.studio.impl.v1.util.ContentUtils.*;
import static org.craftercms.studio.impl.v2.utils.security.SecurityUtils.*;
import static org.craftercms.studio.permissions.CompositePermissionResolverImpl.PATH_LIST_RESOURCE_ID;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONTENT_WRITE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.SITE_ID_RESOURCE_ID;

public class ContentServiceInternalImpl implements ContentService, ApplicationEventPublisherAware {

	private static final Logger logger = LoggerFactory.getLogger(ContentServiceInternalImpl.class);
	private static final int FETCH_AUTHOR_FROM_COMMITS_BATCH_SIZE = 1000;

	private GitContentRepository contentRepository;
	private ItemDAO itemDao;
	private StudioConfiguration studioConfiguration;
	private SemanticsAvailableActionsResolver semanticsAvailableActionsResolver;
	private AuditService auditService;
	private DependencyService dependencyService;
	private SitesService siteService;
	private ItemService itemService;
	private GeneralLockService generalLockService;
	private ApplicationEventPublisher eventPublisher;
	private org.craftercms.studio.api.v1.service.content.ContentService contentServiceV1;
	private PublishService publishService;
	private ProcessedCommitsDAO processedCommitsDao;
	private ContentLifeCycle contentLifeCycle;
	private ContentLifeCycle assetLifeCycle;
	private PermissionEvaluator<String, Object> permissionEvaluator;
	private DmPageNavigationOrderService pageNavOrderService;

	@Override
	public boolean contentExists(String siteId, String path) {
		return contentRepository.contentExists(siteId, path);
	}

	@Override
	public boolean shallowContentExists(String siteId, String path) {
		return contentRepository.shallowContentExists(siteId, path);
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
		List<ContentItem> resultSet = itemDao.getChildrenByPath(site.getId(), parentFolderPath,
			locale, keyword, systemTypes, List.of(CONTENT_TYPE_LEVEL_DESCRIPTOR),
			excludes,
			sortStrategy, order, offset, limit);

		processResultSet(siteId, resultSet);
		GetChildrenResult toRet = new GetChildrenResult();
		toRet.setChildren(resultSet);
		toRet.setLevelDescriptor(getLevelDescriptor(site, path, locale, keyword));
		toRet.setOffset(offset);
		toRet.setLimit(limit);
		toRet.setTotal(total);
		return toRet;
	}

	private ContentItem getLevelDescriptor(final Site site, final String path, final String locale, final String keyword) throws UserNotFoundException, ServiceLayerException {
		List<ContentItem> childItems = itemDao.getChildrenByPath(site.getId(), path,
			locale, keyword, List.of(CONTENT_TYPE_LEVEL_DESCRIPTOR), null, null,
			null, null, 0, 1);
		if (isEmpty(childItems)) {
			return null;
		}
		ContentItem levelDescriptorItem = childItems.getFirst();
		String user = getCurrentUsername();
		levelDescriptorItem.setAvailableActions(
			semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, site.getSiteId(), levelDescriptorItem));
		return levelDescriptorItem;
	}

	@Override
	public GetChildrenByPathsBulkResult getChildrenByPaths(String siteId, List<String> paths,
														   Map<String, PathParams> pathParams) throws UserNotFoundException, ServiceLayerException {
		List<ChildrenByPathResult> resultItems = new ArrayList<>(paths.size());

		Map<String, ContentItem> sandboxItemsByPath = getContentItemsByPath(siteId, paths, true).stream()
			.collect(toMap(ContentItem::getPath, identity()));
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

	private void processResultSet(String siteId, List<ContentItem> resultSet)
		throws ServiceLayerException, UserNotFoundException {
		if (isEmpty(resultSet)) {
			return;
		}
		String user = getCurrentUsername();
		for (ContentItem child : resultSet) {
			child.setAvailableActions(
				semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, child));
		}
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
	public List<ContentItem> getContentItemsByStates(String siteId, long statesBitMap, List<String> systemTypes, List<SortField> sortFields, int offset, int limit) throws UserNotFoundException, ServiceLayerException {
		List<ContentItem> items = itemDao.getContentItemsByStates(siteId, null, statesBitMap,
			systemTypes, mapSortFields(sortFields, ItemDAO.DETAILED_ITEM_SORT_FIELD_MAP), offset, limit);
		for (ContentItem item : items) {
			populateDetailedItemPropertiesFromRepository(siteId, item);
		}
		return items;
	}

	@Override
	public ContentItem getItemByPath(String siteId, String path, boolean preferContent)
		throws ServiceLayerException, UserNotFoundException {
		if (!contentRepository.contentExists(siteId, path)) {
			throw new ContentNotFoundException(path, siteId, format("Content not found at path '%s' site '%s'", path, siteId));
		}
		Site site = siteService.getSite(siteId);
		ContentItem item = null;
		if (preferContent) {
			item = itemDao.getContentItemByPathPreferContent(site.getId(), path);
		} else {
			item = itemDao.getContentItemByPath(site.getId(), path);
		}
		if (item == null) {
			throw new ContentNotFoundException(path, siteId, format("Content not found at path '%s' site '%s'", path, siteId));
		}
		populateDetailedItemPropertiesFromRepository(siteId, item);
		return item;
	}

	private void populateDetailedItemPropertiesFromRepository(String siteId, ContentItem item)
		throws ServiceLayerException, UserNotFoundException {
		if (Objects.nonNull(item)) {
			String user = getCurrentUsername();
			item.setAvailableActions(
				semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, item));
		}
	}

	@Override
	public List<ContentItem> getContentItemsByPath(String siteId, List<String> paths, boolean preferContent)
		throws ServiceLayerException, UserNotFoundException {
		Site site = siteService.getSite(siteId);
		List<ContentItem> items = itemDao.getContentItemsByPath(site.getId(), paths, preferContent);
		return calculatePossibleActions(siteId, items);
	}

	private List<ContentItem> calculatePossibleActions(String siteId, List<ContentItem> items)
		throws ServiceLayerException, UserNotFoundException {
		if (isEmpty(items)) {
			return emptyList();
		}
		List<ContentItem> toRet = new ArrayList<>();
		String user = getCurrentUsername();
		for (ContentItem item : items) {
			if (!contentRepository.contentExists(siteId, item.getPath())) {
				logger.warn("Content not found in site '{}' path '{}'", siteId, item.getPath());
			} else {
				item.setAvailableActions(
					semanticsAvailableActionsResolver.calculateContentItemAvailableActions(user, siteId, item));
				toRet.add(item);
			}
		}
		return toRet;
	}

	@Override
	public boolean isEditable(String itemPath, String mimeType) {
		List<String> editableMimeTypes =
			Arrays.asList(studioConfiguration.getArray(CONTENT_ITEM_EDITABLE_TYPES, String.class));

		MimeType itemMimeType;
		if (isEmpty(mimeType)) {
			itemMimeType = MimeType.valueOf(StudioUtils.getMimeType(itemPath));
		} else {
			itemMimeType = MimeType.valueOf(mimeType);
		}

		return editableMimeTypes.stream()
			.anyMatch(type -> (MimeType.valueOf(type)).isCompatibleWith(itemMimeType));
	}

	@Override
	public Optional<Resource> getContentByCommitId(String siteId, String path, String commitId)
		throws ContentNotFoundException {
		return contentRepository.getContentByCommitId(siteId, path, commitId);
	}

	@Override
	public List<ItemVersion> getContentVersionHistory(final String siteId, final String path) throws ServiceLayerException {
		try {
			Site site = siteService.getSite(siteId);

			List<ItemVersion> history = contentRepository.getContentItemHistory(siteId, path);

			for (List<ItemVersion> batch : Lists.partition(history, FETCH_AUTHOR_FROM_COMMITS_BATCH_SIZE)) {
				List<String> commitIds = batch.stream()
					.map(ItemVersion::getVersionNumber)
					.filter(Objects::nonNull)
					.collect(toList());
				List<CommitAuthor> commitAuthors = auditService.getCommitAuthors(site.getId(), commitIds, path);
				Map<String, Person> authorsMap = commitAuthors.stream()
					.collect(toMap(CommitAuthor::getCommitId, CommitAuthor::getAuthor));
				for (ItemVersion itemVersion : batch) {
					String versionNumber = itemVersion.getVersionNumber();
					if (authorsMap.containsKey(versionNumber)) {
						itemVersion.setAuthor(authorsMap.get(versionNumber));
					}
				}
			}
			return history;
		} catch (IOException | GitAPIException e) {
			throw new ServiceLayerException(format("Error getting content version history for site '%s' path '%s'", siteId, path), e);
		}
	}

	/**
	 * Run life cycle script (for content descriptors) or asset pipeline (for assets)
	 * Return the LifecycleContent object
	 */
	// TODO: Should we consider configuration files here?
	protected LifeCycleContent runLifeCycle(final String siteId, final String path, final InputStream content) throws ServiceLayerException {
		boolean contentExists = contentExists(siteId, path);
		LifeCycleOperation operation = contentExists ? UPDATE : NEW;
		ContentLifeCycle lifeCycle;
		LifeCycleContent lifeCycleContent;
		// Check if it is an asset
		if (isDescriptorPath(path)) {
			try {
				Document document = convertStreamToXml(content);
				String contentType = document.getRootElement().valueOf(CONTENT_TYPE);
				pageNavOrderService.updateNavOrder(siteId, path, document);
				Path tmpFile = createTempFile(path, document);
				lifeCycleContent = new LifeCycleContent(path, contentType, tmpFile, operation);
				lifeCycle = contentLifeCycle;
			} catch (DocumentException e) {
				throw new ServiceLayerException(format("Error converting stream to XML for site '%s' path '%s'", siteId, path), e);
			} catch (IOException e) {
				throw new ServiceLayerException(format("Error writing content to temporary file for site '%s' path '%s'", siteId, path), e);
			}
		} else {
			try {
				Path tmpFile = createTempFile(path, content);
				lifeCycleContent = new LifeCycleContent(path, null, tmpFile, operation);
				lifeCycle = assetLifeCycle;
			} catch (IOException e) {
				throw new ServiceLayerException(format("Error creating temporary file for content write site '%s' path '%s'", siteId, path), e);
			}
		}

		try {
			lifeCycle.execute(siteId, lifeCycleContent, this::loadContent);
		} catch (Exception e) {
			lifeCycleContent.close();
			throw e;
		}

		return lifeCycleContent;
	}

	@Override
	public WriteContentResult write(final String siteId, final String path, final InputStream content)
		throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		try (LifeCycleContent lifeCycleContent = runLifeCycle(siteId, path, content)) {
			Map<String, ContentLifeCycleItem> lifeCycleResultItems = lifeCycleContent.getItems();
			// Check list is not empty or throw exception  (can't write empty set)
			if (lifeCycleResultItems.isEmpty()) {
				throw new ServiceLayerException(format("Item list after life cycle processing is empty, nothing to write for site '%s' path '%s'", siteId, path));
			}

			List<String> paths = new ArrayList<>(lifeCycleResultItems.keySet());
			Map<String, Object> resource = Map.of(SITE_ID_RESOURCE_ID, siteId, PATH_LIST_RESOURCE_ID, paths);
			if (!permissionEvaluator.isAllowed(SecurityUtils.getCurrentUsername(), resource, PERMISSION_CONTENT_WRITE)) {
				throw new ActionDeniedException(PERMISSION_CONTENT_WRITE, paths);
			}

			// Fail to continue write operation if the item is in workflow
			assertNotInWorkflow(siteId, lifeCycleResultItems.keySet(), false);

			Map<String, LifeCycleOperation> operationsByPath = getOperationsByPath(siteId, lifeCycleContent);
			Set<String> missingFolders = getMissingFolders(siteId, operationsByPath);

			// Write to the repository and commit.
			String commitId = contentRepository.writeContent(siteId, lifeCycleResultItems.values(), missingFolders);
			if (isEmpty(commitId)) {
				throw new EmptyChangesetException(format("No changes were made to the repository for site '%s' path '%s'", siteId, path));
			}

			List<WriteContentResultItem> writeResultItems = persistToDB(siteId, lifeCycleResultItems.values(), missingFolders, operationsByPath);

			// Audit write operation
			insertWriteContentAudit(siteId, path, lifeCycleContent.getOperation(), writeResultItems, commitId);

			// Publish events
			eventPublisher.publishEvent(new ContentEvent(SecurityUtils.getAuthentication(), siteId, path));

			// Return the WriteContentResult
			return new WriteContentResult(writeResultItems);
		}
	}

	/**
	 * Extract the missing folders from a write operation
	 * Missing folders are the newly created paths that need empty file added to the repo
	 */
	protected Set<String> getMissingFolders(final String siteId, final Map<String, LifeCycleOperation> operationsByPath) {
		return operationsByPath.entrySet().stream()
			.filter(entry -> entry.getValue() == NEW)
			.map(Map.Entry::getKey)
			.map(p -> calculateMissingFolders(siteId, p))
			.flatMap(Collection::stream)
			.collect(toSet());
	}

	/**
	 * Creates a map out of the ContentLifecycleItems, where the key is the path and
	 * the value is the operation performed
	 */
	protected @NotNull Map<String, LifeCycleOperation> getOperationsByPath(String siteId, LifeCycleContent lifeCycleContent) {
		Map<String, ContentLifeCycleItem> resultItems = lifeCycleContent.getItems();
		String path = lifeCycleContent.getRepoPath();
		// Calculate the operation. This must be done before actually writing to the repository
		Map<String, LifeCycleOperation> operationsByPath = new HashMap<>(resultItems.size());
		for (ContentLifeCycleItem item : resultItems.values()) {
			LifeCycleOperation operation;
			if (path.equals(item.repoPath())) {
				// Preserve the operation for the main item
				operation = lifeCycleContent.getOperation();
			} else if (contentExists(siteId, item.repoPath())) {
				operation = UPDATE;
			} else {
				operation = NEW;
			}
			operationsByPath.put(item.repoPath(), operation);
		}
		return operationsByPath;
	}

	/**
	 * Provides a comparator to sort the paths so parents are created first
	 *
	 * @return a comparator to sort the paths
	 */
	protected Comparator<String> creationPathComparator() {
		// index.xml should go first
		// Otherwise sort by length so parents go first
		return Comparator.<String, Integer>comparing(s -> StringUtils.removeEnd(s, INDEX_FILE).length())
			// If they have the same length after removing index.xml, we are comparing folder and page for the same path:
			// 	/site/website/en/index.xml
			// 	/site/website/en
			// Natural order will give us the folder first
			.thenComparing(naturalOrder());
	}

	/**
	 * Persist a new folder to the database
	 *
	 * @param siteId    the site id
	 * @param newFolder the new folder path
	 */
	protected void persistNewFolder(final String siteId, final String newFolder) throws UserNotFoundException, AuthenticationException, ServiceLayerException {
		Item parentItem = itemService.getItem(siteId, getParentUrl(newFolder), true);
		itemService.persistItemAfterCreateFolder(siteId, newFolder, getBaseName(Path.of(newFolder)), parentItem.getId());
	}

	/**
	 * Persist an item to the database
	 *
	 * @param siteId    the site id
	 * @param item      the item to persist
	 * @param operation the content life cycle operation
	 */
	protected WriteContentResultItem persistItem(final String siteId, final ContentLifeCycleItem item,
												 LifeCycleOperation operation) throws UserNotFoundException, AuthenticationException, ServiceLayerException {
		String path = item.repoPath();
		if (NEW == operation) {
			boolean isPage = path.startsWith(ROOT_PATTERN_PAGES) && path.endsWith(FILE_SEPARATOR + INDEX_FILE);
			String parentItemPath;
			if (isPage) {
				parentItemPath = getParentUrl(removeEnd(path, SLASH_INDEX_FILE));
			} else {
				parentItemPath = getParentUrl(path);
			}
			Item parent = itemService.getItem(siteId, parentItemPath, isPage);
			itemService.persistItemAfterCreate(siteId, path, false, parent.getId());
		} else {
			itemService.persistItemAfterWrite(siteId, path, false);
		}
		dependencyService.upsertDependencies(siteId, path);
		dependencyService.validateDependencies(siteId, path);
		return new WriteContentResultItem(path, operation, item.amended());
	}

	/**
	 * Persist changes to the DB and gather the write result items from the ContentLifeCycleItems
	 */
	protected @NotNull List<WriteContentResultItem> persistToDB(String siteId, Collection<ContentLifeCycleItem> lifeCycleResultItems,
																Set<String> missingFolders, Map<String, LifeCycleOperation> operationsByPath)
		throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		List<WriteContentResultItem> writeResultItems = new ArrayList<>(lifeCycleResultItems.size());

		// Gather all the persist calls so we can sort them
		Map<String, ThrowingRunnable> persistItemCalls = new HashMap<>();
		for (String missingFolder : missingFolders) {
			persistItemCalls.put(missingFolder, () -> persistNewFolder(siteId, missingFolder));
		}
		for (ContentLifeCycleItem item : lifeCycleResultItems) {
			persistItemCalls.put(item.repoPath(), () -> writeResultItems.add(persistItem(siteId, item, operationsByPath.get(item.repoPath()))));
		}

		List<String> allPaths = persistItemCalls.keySet().stream().sorted(creationPathComparator()).toList();
		for (String path : allPaths) {
			try {
				persistItemCalls.get(path).run();
			} catch (ServiceLayerException | UserNotFoundException | AuthenticationException e) {
				throw e;
			} catch (Exception e) {
				throw new ServiceLayerException(e);
			}
		}
		return writeResultItems;
	}

	/**
	 * Calculate the missing folders for a given path.
	 * Missing folders are the ancestors of the path that do not exist in the repository.
	 */
	protected Collection<String> calculateMissingFolders(final String siteId, final String path) {
		List<String> missingFolders = new ArrayList<>();
		String parentItemPath = FilenameUtils.getFullPathNoEndSeparator(path);
		Path current = Path.of(parentItemPath);
		while (current != null && !contentExists(siteId, current.toString())) {
			missingFolders.add(current.toString());
			current = current.getParent();
		}
		return missingFolders;
	}

	/**
	 * Content loader method to support the content life cycle script
	 *
	 * @param siteId the site id
	 * @param path   the path
	 * @return InputStream to read the content
	 */
	protected InputStream loadContent(String siteId, String path) {
		if (!contentRepository.contentExists(siteId, path)) {
			return null;
		}
		try {
			return contentRepository.getContent(siteId, path);
		} catch (ContentNotFoundException e) {
			logger.error("Failed to load content for site '{}' path '{}'", siteId, path, e);
			return null;
		}
	}

	/**
	 * Insert an audit log entry for the write operation
	 *
	 * @param siteId           the site id
	 * @param path             the path
	 * @param operation        the operation performed
	 * @param writeResultItems the write result items
	 * @param commitId         the commit id
	 */
	protected void insertWriteContentAudit(String siteId, String path, LifeCycleOperation operation,
										   List<WriteContentResultItem> writeResultItems, String commitId) throws SiteNotFoundException {
		Site site = siteService.getSite(siteId);
		AuditLog auditLog = createAuditLogEntry();
		switch (operation) {
			case NEW -> auditLog.setOperation(AuditLogConstants.OPERATION_CREATE);
			case UPDATE -> auditLog.setOperation(AuditLogConstants.OPERATION_UPDATE);
			default -> auditLog.setOperation(operation.name());
		}
		auditLog.setActorId(getCurrentUsername());
		auditLog.setSiteId(site.getId());
		auditLog.setPrimaryTargetId(getContentItemId(site.getSiteId(), path));
		auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
		auditLog.setPrimaryTargetValue(path);
		auditLog.setCommitId(commitId);

		List<AuditLogParameter> auditLogParameters = new ArrayList<>(writeResultItems.size());
		for (WriteContentResultItem item : writeResultItems) {
			if (item.path().equals(path)) {
				continue;
			}
			AuditLogParameter auditLogParameter = new AuditLogParameter();
			auditLogParameter.setTargetId(getContentItemId(site.getSiteId(), item.path()));
			auditLogParameter.setTargetType(TARGET_TYPE_CONTENT_ITEM);
			auditLogParameter.setTargetValue(item.path());
			auditLogParameters.add(auditLogParameter);
		}
		auditLog.setParameters(auditLogParameters);
		auditService.insertAuditLog(auditLog);
	}

	@Override
	public List<LightItem> getChildItems(final String siteId, final List<String> paths) {
		Collection<LightItem> subtreeItems = itemDao.getSubtreeItems(siteId, paths);
		List<String> subtreePaths = subtreeItems.stream().map(LightItem::getPath).toList();
		List<LightItem> childItems = new ArrayList<>(subtreeItems);
		childItems.addAll(dependencyService.getItemSpecificDependencies(siteId, union(paths, subtreePaths)));
		return childItems;
	}

	@Override
	public void assertNotInWorkflow(final String siteId, final Collection<String> paths, final boolean includeChildren)
		throws ContentInPublishQueueException {
		// No need to check for children, as the paths collection already includes them
		Collection<PublishPackage> packagesForItems = publishService.getActivePackagesForItems(siteId, paths, includeChildren);
		if (isNotEmpty(packagesForItems)) {
			throw new ContentInPublishQueueException("Unable to edit content that is part of an active publish package", packagesForItems);
		}
	}

	@Override
	public long deleteContent(String siteId, List<String> paths, String publishTitle, String publishComment)
		throws ServiceLayerException, AuthenticationException, UserNotFoundException {
		// Lock the sandbox repository to prevent publish packages being submitted (delete operation might conflict with submitted packages)
		String sandboxRepoLockKey = getSandboxRepoLockKey(siteId);
		generalLockService.lock(sandboxRepoLockKey);
		Collection<String> allPaths = new ArrayList<>();
		try {
			AuthenticatedUser currentUser = getCurrentUser();
			if (itemService.isSystemProcessing(siteId, paths)) {
				throw new ServiceLayerException(format("Failed to delete content at site '%s' paths '%s' " +
						"because some items are being processed  (Object State is system processing)",
					siteId, paths));
			}
			itemService.setSystemProcessingBulk(siteId, paths, true);
			allPaths.addAll(paths);

			Optional<String> notFound = paths.stream().filter(path -> !contentRepository.contentExists(siteId, path)).findFirst();
			if (notFound.isPresent()) {
				throw new ContentNotFoundException(notFound.get(), siteId, "Content '%s' not found in site '%s'".formatted(notFound.get(), siteId));
			}

			Site site = siteService.getSite(siteId);
			List<String> children = itemDao.getSubtreeItems(siteId, paths).stream()
				.map(LightItem::getPath)
				.toList();
			itemService.setSystemProcessingBulk(siteId, children, true);
			allPaths.addAll(children);

			Collection<String> userRequested = CollectionUtils.union(paths, children);
			List<String> dependencies = dependencyService.getItemSpecificDependencies(siteId, paths).stream().map(LightItem::getPath).toList();
			itemService.setSystemProcessingBulk(siteId, dependencies, true);
			allPaths.addAll(dependencies);

			// check and fail if any of the items is part of a publish package
			assertNotInWorkflow(siteId, allPaths, false);
			String commitId = contentRepository.deleteContent(siteId, allPaths, currentUser.getUsername());
			processedCommitsDao.insertCommit(site.getId(), commitId);

			long publishPackageId = 0;
			if (contentRepository.publishedRepositoryExists(siteId)) {
				publishPackageId = publishService.publishDelete(siteId, userRequested,
					dependencies, publishTitle, publishComment);
			}

			for (String path : allPaths) {
				dependencyService.deleteItemDependencies(siteId, path);
				dependencyService.invalidateDependencies(siteId, path);
				itemService.deleteItem(site.getId(), path, true);
			}

			insertDeleteContentApprovedActivity(site, currentUser.getUsername(), allPaths);

			Authentication auth = getAuthentication();
			for (String path : paths) {
				eventPublisher.publishEvent(new DeleteContentEvent(auth, siteId, path));
			}
			return publishPackageId;
		} finally {
			if (!allPaths.isEmpty()) {
				itemService.setSystemProcessingBulk(siteId, allPaths, false);
			}
			generalLockService.unlock(sandboxRepoLockKey);
		}
	}

	private void insertDeleteContentApprovedActivity(Site site, String approver, Collection<String> paths) {
		AuditLog auditLog = createAuditLogEntry();
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
		auditService.insertAuditLog(auditLog);
	}

	@Override
	public Document getItemDescriptor(String siteId, String path, boolean flatten) throws ContentNotFoundException {
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
			var item = itemService.getItem(siteId, path);
			if (Objects.isNull(item)) {
				throw new ContentNotFoundException(path, siteId, format("Content not found in site '%s' at path '%s'",
					siteId, path));
			}
			var username = getCurrentUsername();
			boolean lockedByAnotherUser = ItemState.isUserLocked(item.getState()) &&
				Objects.nonNull(item.getLockOwner()) && !StringUtils.equals(item.getLockOwner().getUsername(), username);
			if (lockedByAnotherUser) {
				throw new ContentLockedByAnotherUserException(item.getLockOwner().getUsername());
			}

			contentRepository.lockItem(siteId, path);
			itemService.lockItemByPath(siteId, path, username);
			eventPublisher.publishEvent(
				new LockContentEvent(getAuthentication(), siteId, path, true));
		} finally {
			generalLockService.unlockContentItem(siteId, path);
		}
	}

	@Override
	public void unlockContent(String siteId, String path) throws ContentNotFoundException {
		logger.debug("Unlock item in site '{}' path '{}'", siteId, path);
		generalLockService.lockContentItem(siteId, path);
		try {
			var item = itemService.getItem(siteId, path);
			if (Objects.isNull(item)) {
				logger.debug("Item not found in site '{}' path '{}'", siteId, path);
				throw new ContentNotFoundException(path, siteId, format("Item not found in site '%s' path '%s'", siteId, path));
			}
			if (!ItemState.isUserLocked(item.getState()) && Objects.isNull(item.getLockOwner())) {
				logger.warn("Skipping unlock operation for item in site '{}' at path '{}': Item is already unlocked.", siteId, path);
				return;
			}
			contentRepository.unlockItem(siteId, path);
			itemService.unlockItemByPath(siteId, path);
			logger.debug("Item in site '{}' path '{}' successfully unlocked", siteId, path);
			eventPublisher.publishEvent(
				new LockContentEvent(getAuthentication(), siteId, path, false));
		} finally {
			generalLockService.unlockContentItem(siteId, path);
		}
	}

	@Override
	public boolean renameContent(String site, String path, String name) throws ServiceLayerException, UserNotFoundException, ValidationException, AuthenticationException {
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

	@SuppressWarnings("unused")
	public void setItemDao(final ItemDAO itemDao) {
		this.itemDao = itemDao;
	}

	public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
		this.studioConfiguration = studioConfiguration;
	}

	@SuppressWarnings("unused")
	public void setSemanticsAvailableActionsResolver(final SemanticsAvailableActionsResolver semanticsAvailableActionsResolver) {
		this.semanticsAvailableActionsResolver = semanticsAvailableActionsResolver;
	}

	public void setAuditService(final AuditService auditService) {
		this.auditService = auditService;
	}

	@Override
	public void setApplicationEventPublisher(final @NotNull ApplicationEventPublisher eventPublisher) {
		this.eventPublisher = eventPublisher;
	}

	@SuppressWarnings("unused")
	public void setDependencyService(final DependencyService dependencyService) {
		this.dependencyService = dependencyService;
	}

	public void setSiteService(final SitesService siteService) {
		this.siteService = siteService;
	}

	public void setItemService(final ItemService itemService) {
		this.itemService = itemService;
	}

	public void setGeneralLockService(final GeneralLockService generalLockService) {
		this.generalLockService = generalLockService;
	}

	@SuppressWarnings("unused")
	public void setContentServiceV1(final org.craftercms.studio.api.v1.service.content.ContentService contentService) {
		this.contentServiceV1 = contentService;
	}

	@SuppressWarnings("unused")
	public void setPublishService(final PublishService publishService) {
		this.publishService = publishService;
	}

	@SuppressWarnings("unused")
	public void setProcessedCommitsDao(final ProcessedCommitsDAO processedCommitsDao) {
		this.processedCommitsDao = processedCommitsDao;
	}

	@SuppressWarnings("unused")
	public void setContentLifeCycle(final ContentLifeCycle contentLifeCycle) {
		this.contentLifeCycle = contentLifeCycle;
	}

	@SuppressWarnings("unused")
	public void setAssetLifeCycle(final ContentLifeCycle assetLifeCycle) {
		this.assetLifeCycle = assetLifeCycle;
	}

	@SuppressWarnings("unused")
	public void setPermissionEvaluator(final PermissionEvaluator<String, Object> permissionEvaluator) {
		this.permissionEvaluator = permissionEvaluator;
	}

	@SuppressWarnings("unused")
	public void setPageNavOrderService(final DmPageNavigationOrderService pageNavOrderService) {
		this.pageNavOrderService = pageNavOrderService;
	}
}
