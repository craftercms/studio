/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.item.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.rest.parameters.SortField;
import org.craftercms.core.exception.XmlFileParseException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.dal.item.ContentItem;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static java.util.Collections.emptyMap;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.utils.DalUtils.mapSortFields;
import static org.craftercms.studio.api.v2.utils.StudioUtils.underDescriptorRoot;
import static org.craftercms.studio.impl.v1.util.ContentUtils.getContentTypeClass;
import static org.slf4j.LoggerFactory.getLogger;

public class ItemServiceInternalImpl implements ItemService {
	// TODO: SJ: Add logging to this class
	private static final Logger logger = getLogger(ItemServiceInternalImpl.class);

	public final static String CONTENT_TYPE = "/*[1]/content-type";
	public final static String DISABLED = "/*[1]/disabled";
	public final static String LOCALE_CODE = "/*[1]/locale-code";
	public final static String SAVED_AS_DRAFT = "/*[1]/savedAsDraft";

	private UserService userService;
	private SiteDAO siteDao;
	private ItemDAO itemDao;
	private ServicesConfig servicesConfig;
	private GeneralLockService generalLockService;
	private GitContentRepository contentRepository;
	private RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
	private StudioConfiguration studioConfiguration;

	protected void upsertEntry(Item item) {
		retryingDatabaseOperationFacade.retry(() -> itemDao.upsertEntry(item));
	}

	@Override
	public Item getItem(String siteId, String path) {
		return getItem(siteId, path, false);
	}

	@Override
	public Item getItem(String siteId, String path, boolean preferContent) {
		Site site = siteDao.getSite(siteId);
		if (Objects.isNull(site)) {
			return null;
		}
		return itemDao.getItemByPath(site.getId(), path, preferContent);
	}

	@Override
	public void deleteItem(long siteId, String path, boolean removePageParentFolder) {
		retryingDatabaseOperationFacade.retry(() -> itemDao.deleteBySiteAndPath(siteId, path, removePageParentFolder));
	}

	@Override
	public void setSystemProcessingBulk(String siteId, Collection<String> paths, boolean isSystemProcessing) {
		if (isSystemProcessing) {
			setStatesBySiteAndPathBulk(siteId, paths, ItemState.SYSTEM_PROCESSING.value);
		} else {
			resetStatesBySiteAndPathBulk(siteId, paths, ItemState.SYSTEM_PROCESSING.value);
		}
	}

	private void setStatesBySiteAndPathBulk(String siteId, Collection<String> paths, long statesBitMap) {
		if (CollectionUtils.isNotEmpty(paths)) {
			Site site = siteDao.getSite(siteId);
			retryingDatabaseOperationFacade.retry(() -> itemDao.setStatesBySiteAndPathBulk(site.getId(), paths, statesBitMap));
		}
	}

	private void resetStatesBySiteAndPathBulk(String siteId, Collection<String> paths, long statesBitMap) {
		if (CollectionUtils.isNotEmpty(paths)) {
			Site site = siteDao.getSite(siteId);
			retryingDatabaseOperationFacade.retry(() -> itemDao.resetStatesBySiteAndPathBulk(site.getId(), paths, statesBitMap));
		}
	}

	@Override
	public Item.Builder instantiateItem(String siteName, String path) {
		Item item = getItem(siteName, path);
		if (Objects.isNull(item)) {
			item = new Item();
			Site site = siteDao.getSite(siteName);
			item.setSiteId(site.getId());
			item.setSiteName(siteName);
			item.setPath(path);
			item.setState(NEW.value);
		}
		return Item.Builder.buildFromClone(item).withId(item.getId());
	}

	@Override
	public String getBrowserUrl(String site, String path) throws SiteNotFoundException {
		return ContentUtils.getPreviewUrl(servicesConfig, site, path);
	}

	@Override
	public void persistItemAfterCreate(String siteId, String path, Long parentId)
			throws ServiceLayerException, AuthenticationException {
		String lockKey = "persistItemAfterCreate:" + siteId;
		generalLockService.lock(lockKey);
		try {
			User userObj = SecurityUtils.getCurrentUser();
			Item item = buildItem(siteId, path, userObj);

			item.setParentId(parentId);
			item.setCreatedBy(userObj.getId());
			item.setCreatedOn(DateUtils.getCurrentTime());

			item.setLockedBy(userObj.getId());
			upsertEntry(item);
		} finally {
			generalLockService.unlock(lockKey);
		}
	}

	@Override
	public void persistItemAfterWrite(String siteId, String path)
		throws ServiceLayerException, AuthenticationException {
		User userObj = SecurityUtils.getCurrentUser();
		Item item = buildItem(siteId, path, userObj);
		upsertEntry(item);
	}

	/**
	 * Instantiates an Item with current timestamps and user info, as well as metadata
	 * from the xml.
	 *
	 * @param siteId the site id
	 * @param path   the item path
	 * @return the Item
	 * @throws AuthenticationException if there is an error getting the current user
	 * @throws ServiceLayerException   if there is an error getting the content descriptor for the item
	 */
	protected Item buildItem(String siteId, String path, User userObj) throws AuthenticationException, ServiceLayerException {
		boolean disabled = false;
		String label = null;
		String contentType = null;
		String localeCode = null;
		boolean savedAsDraft = false;
		try {
			var descriptor = contentRepository.getItem(siteId, path, false);
			String disabledStr = descriptor.queryDescriptorValue(DISABLED);
			disabled = Boolean.parseBoolean(disabledStr);
			label = descriptor.queryDescriptorValue(INTERNAL_NAME_XPATH);
			contentType = descriptor.queryDescriptorValue(CONTENT_TYPE);
			localeCode = descriptor.queryDescriptorValue(LOCALE_CODE);
			savedAsDraft = Boolean.parseBoolean(descriptor.queryDescriptorValue(SAVED_AS_DRAFT));
		} catch (XmlFileParseException e) {
			logger.debug("Error getting content descriptor for path: '{}'", path, e);
			// If page, component, or other descriptor file, it must be a valid xml file
			if (underDescriptorRoot(path)) {
				throw new ServiceLayerException("Error getting content descriptor for path: " + path, e);
			}
		}
		if (StringUtils.isEmpty(label)) {
			label = FilenameUtils.getName(path);
		}
		Item item = instantiateItem(siteId, path)
				.withPreviewUrl(getBrowserUrl(siteId, path))
				.withLastModifiedBy(userObj.getId())
				.withLastModifiedOn(DateUtils.getCurrentTime())
				.withLabel(label)
				.withSystemType(getContentTypeClass(servicesConfig, studioConfiguration, siteId, path))
				.withContentTypeId(contentType)
				.withMimeType(StudioUtils.getMimeType(path))
				.withLocaleCode(localeCode)
				.withSize(contentRepository.getContentSize(siteId, path))
				.withSavedAsDraft(savedAsDraft)
				.build();

		item.setState(ItemState.savedAndNotClosed(item.getState()));
		if (disabled) {
			item.setState(item.getState() | ItemState.DISABLED.value);
		} else {
			item.setState(item.getState() & ~ItemState.DISABLED.value);
		}
		return item;
	}

	@Override
	public void persistItemAfterCreateFolder(String siteId, String folderPath, String folderName, Long parentId)
			throws AuthenticationException {
		User userObj = SecurityUtils.getCurrentUser();
		Item item = instantiateItem(siteId, folderPath)
				.withLastModifiedBy(userObj.getId())
				.withLastModifiedOn(DateUtils.getCurrentTime())
				.withLabel(folderName)
				.withParentId(parentId)
				.withState(0L)
				.build();
		item.setSystemType(CONTENT_TYPE_FOLDER);
		upsertEntry(item);
	}

	@Override
	public void moveItem(String siteId, String oldPath, String newPath, Long parentId, String label, long userId)
			throws SiteNotFoundException {
		String newPreviewUrl = getBrowserUrl(siteId, newPath);
		retryingDatabaseOperationFacade.retry(() ->
			itemDao.moveItem(siteId, oldPath, newPath, parentId, newPreviewUrl, label, userId));
	}

	@Override
	public void copyItem(String siteId, String sourcePath, String targetPath, long parentId, String label, long userId)
			throws SiteNotFoundException {
		String previewUrl = getBrowserUrl(siteId, targetPath);
		retryingDatabaseOperationFacade.retry(() ->
				itemDao.copyItem(siteId, sourcePath, targetPath, previewUrl, parentId, label, userId));
	}

	@Override
	public boolean isNew(String siteId, String path) {
		Item item = getItem(siteId, path);
		return ItemState.isNew(item.getState());
	}

	@Override
	public int countAllContentItems() {
		return itemDao.countAllContentItems(List.of(CONTENT_TYPE_FOLDER, CONTENT_TYPE_UNKNOWN));
	}

	@Override
	public Collection<String> getUnpublishedPaths(long siteId) {
		return itemDao.getUnpublishedPaths(siteId);
	}

	@Override
	public boolean isSystemProcessing(String siteId, Collection<String> paths) {
		if (isEmpty(paths)) {
			return false;
		}
		return itemDao.matchItemState(siteId, paths, SYSTEM_PROCESSING.value);
	}

	@Override
	public void lockItemByPath(String siteId, String path, String username)
		throws UserNotFoundException, ServiceLayerException {
		User user = userService.getUserByIdOrUsername(-1, username);
		retryingDatabaseOperationFacade.retry(() -> itemDao.lockItemByPath(siteId, path, user.getId(), USER_LOCKED.value, CONTENT_TYPE_FOLDER));
	}

	@Override
	public void unlockItemByPath(String siteId, String path) {
		retryingDatabaseOperationFacade.retry(() -> itemDao.unlockItemByPath(siteId, path, ~USER_LOCKED.value));
	}

	@Override
	public int getItemByStatesTotal(String siteId, String path, Long states, List<String> systemTypes) {
		return itemDao.getItemByStatesTotal(siteId, path, states, systemTypes);
	}

	@Override
	public Map<String, ItemPathAndState> getItemStates(final String siteId, final Collection<String> paths) {
		if (isEmpty(paths)) {
			return emptyMap();
		}
		return itemDao.getItemStates(siteId, paths);
	}

	@Override
	public List<ContentItem> getItemsByStates(String siteId, String path, Long states, List<String> systemTypes, List<SortField> sortFields, int offset, int limit) {
		return itemDao.getContentItemsByStates(siteId, path, states, systemTypes,
			mapSortFields(sortFields, ItemDAO.SORT_FIELD_MAP), offset, limit);
	}

	@Override
	public void updateItemStates(String siteId, List<String> paths, boolean clearSystemProcessing,
								 boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
		if (CollectionUtils.isNotEmpty(paths)) {
			long setStatesMask = getSetStatesMask(live, staged, isNew, modified);
			long resetStatesMask = getResetStatesMask(clearSystemProcessing, clearUserLocked, live, staged, isNew, modified);

			Site site = siteDao.getSite(siteId);
			retryingDatabaseOperationFacade.retry(() -> itemDao.updateStatesBySiteAndPathBulk(site.getId(), paths, setStatesMask,
				resetStatesMask));
		}
	}

	protected long getSetStatesMask(Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
		long setStatesMask = 0L;
		if (BooleanUtils.isTrue(live)) {
			setStatesMask |= LIVE.value;
		}
		if (BooleanUtils.isTrue(staged)) {
			setStatesMask |= STAGED.value;
		}
		if (BooleanUtils.isTrue(isNew)) {
			setStatesMask |= NEW.value;
		}
		if (BooleanUtils.isTrue(modified)) {
			setStatesMask |= MODIFIED.value;
		}
		return setStatesMask;
	}

	protected long getResetStatesMask(boolean clearSystemProcessing, boolean clearUserLocked, Boolean live,
									  Boolean staged, Boolean isNew, Boolean modified) {
		long resetStatesMask = 0L;

		if (clearSystemProcessing) {
			resetStatesMask |= SYSTEM_PROCESSING.value;
		}
		if (clearUserLocked) {
			resetStatesMask |= USER_LOCKED.value;
		}
		if (BooleanUtils.isFalse(live)) {
			resetStatesMask |= LIVE.value;
		}
		if (BooleanUtils.isFalse(staged)) {
			resetStatesMask |= STAGED.value;
		}
		if (BooleanUtils.isFalse(isNew)) {
			resetStatesMask |= NEW.value;
		}
		if (BooleanUtils.isFalse(modified)) {
			resetStatesMask |= MODIFIED.value;
		}
		return resetStatesMask;
	}

	@Override
	public void updateItemStatesByQuery(String siteId, String path, Long states, boolean clearSystemProcessing,
										boolean clearUserLocked, Boolean live, Boolean staged, Boolean isNew, Boolean modified) {
		long setStatesMask = getSetStatesMask(live, staged, isNew, modified);
		long resetStatesMask = getResetStatesMask(clearSystemProcessing, clearUserLocked, live, staged, isNew, modified);

		retryingDatabaseOperationFacade.retry(() -> itemDao.updateStatesByQuery(siteId, path, states, setStatesMask, resetStatesMask));
	}

	@Override
	public void updateNewPageChildren(final String siteId, final String folderPath) {
		retryingDatabaseOperationFacade.retry(() -> itemDao.updateNewPageChildren(siteId, folderPath));
	}

	@Override
	public Collection<String> getChildrenPaths(long siteId, String path) {
		return itemDao.getChildrenPaths(siteId, path);
	}

	@Override
	public void updateParentId(final long siteId, final Collection<String> paths) {
		retryingDatabaseOperationFacade.retry(() -> itemDao.updateParentId(siteId, paths));
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@SuppressWarnings("unused")
	public void setSiteDao(final SiteDAO siteDao) {
		this.siteDao = siteDao;
	}

	@SuppressWarnings("unused")
	public void setItemDao(ItemDAO itemDao) {
		this.itemDao = itemDao;
	}

	public void setServicesConfig(ServicesConfig servicesConfig) {
		this.servicesConfig = servicesConfig;
	}

	public void setContentRepository(GitContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

	public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
		this.studioConfiguration = studioConfiguration;
	}

	public void setGeneralLockService(GeneralLockService generalLockService) {
		this.generalLockService = generalLockService;
	}

	public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
		this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
	}
}
