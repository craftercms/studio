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
package org.craftercms.studio.impl.v2.service.clipboard.internal;

import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v2.annotation.ContentPath;
import org.craftercms.studio.api.v2.annotation.RequireContentExists;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.exception.content.ContentInPublishQueueException;
import org.craftercms.studio.api.v2.exception.content.ContentMoveInvalidLocation;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.service.clipboard.ClipboardService;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.model.clipboard.Operation;
import org.craftercms.studio.model.clipboard.PasteItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static org.apache.commons.collections4.CollectionUtils.isNotEmpty;
import static org.apache.commons.io.FilenameUtils.getFullPathNoEndSeparator;
import static org.apache.commons.lang3.StringUtils.removeEnd;
import static org.craftercms.studio.api.v1.constant.DmConstants.SLASH_INDEX_FILE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getSandboxRepoLockKey;
import static org.craftercms.studio.api.v2.utils.StudioUtils.isPageDescriptor;
import static org.craftercms.studio.model.clipboard.Operation.CUT;

/**
 * Default implementation of {@link ClipboardService}
 *
 * <p>Note: This class could be removed in the future if the logic is moved to the new content service</p>
 *
 * @author joseross
 * @since 3.2
 */
public class ClipboardServiceInternalImpl implements ClipboardService {

	private static final Logger logger = LoggerFactory.getLogger(ClipboardServiceInternalImpl.class);
	public final static Pattern COPY_FILE_MODIFIER_PATTERN = Pattern.compile(".+(-copy-(\\d+))(.+)?(\\..*)?");
	public final static String COPY_FILE_MODIFIER_FORMAT = "%s-copy-%s%s";

	// TODO: remove this dependency and migrate all service calls to the new content service
	protected final ContentService contentService;
	protected final PublishService publishService;
	protected final ItemService itemService;
	protected final GeneralLockService generalLockService;
	protected final org.craftercms.studio.api.v2.service.content.ContentService contentServiceV2;
	protected final GitContentRepository contentRepository;

	@ConstructorProperties({"contentRepository", "contentService",
		"publishService", "itemService",
		"generalLockService", "contentServiceV2"})
	public ClipboardServiceInternalImpl(GitContentRepository contentRepository, ContentService contentService,
										PublishService publishService, ItemService itemService,
										GeneralLockService generalLockService, org.craftercms.studio.api.v2.service.content.ContentService contentServiceV2) {
		this.contentRepository = contentRepository;
		this.contentService = contentService;
		this.publishService = publishService;
		this.itemService = itemService;
		this.generalLockService = generalLockService;
		this.contentServiceV2 = contentServiceV2;
	}

	protected void validatePasteItemsAction(final String siteId, Operation operation, final String sourcePath, final String targetPath)
		throws ServiceLayerException {
		ContentItemTO targetContentItem = contentService.getContentItem(siteId, targetPath);
		if (targetContentItem.isDeleted()) {
			throw new ContentNotFoundException(targetPath, siteId, format("Target path '%s' does not exist. " +
				"Unable to perform paste operation", targetPath));
		}
		if (!targetContentItem.isPage() && !targetContentItem.isFolder()) {
			throw new InvalidParametersException(format("Invalid paste target '%s' in site '%s'. " +
				"Only pages and folders can contain children", targetPath, siteId));
		}
		if (!contentServiceV2.contentExists(siteId, sourcePath)) {
			throw new ContentNotFoundException(sourcePath, siteId, format("No content found at path '%s' " +
				"Unable to perform paste operation", sourcePath));
		}
		String sourceTopLevel = StudioUtils.getTopLevelFolder(sourcePath);
		String targetTopLevel = StudioUtils.getTopLevelFolder(targetPath);

		if (!Objects.equals(sourceTopLevel, targetTopLevel)) {
			throw new InvalidParametersException(format("Cannot perform paste operation " +
					"from '%s' (%s) into '%s' (%s) for site '%s'. " +
					"Pasting across top level folders is not supported.",
				sourcePath, sourceTopLevel, targetPath, targetTopLevel, siteId));
		}

		if (CUT == operation) {
			String sourceDirectory = getParentUrl(sourcePath);
			String targetDirectory = removeEnd(targetPath, SLASH_INDEX_FILE);
			if (sourceDirectory.equals(targetDirectory)) {
				throw new ContentMoveInvalidLocation(format("Cannot perform cut-paste operation from '%s' to the same location '%s' for site '%s'",
					sourcePath, targetPath, siteId));
			}

			Collection<PublishPackage> packagesForItems = publishService.getActivePackagesForItems(siteId, List.of(sourcePath), true);
			if (isNotEmpty(packagesForItems)) {
				throw new ContentInPublishQueueException("Unable to cut content that is part of an active publish package", packagesForItems);
			}
		}

		if (itemService.isSystemProcessing(siteId, List.of(sourcePath, targetPath))) {
			throw new ServiceLayerException(format("Failed to paste items at site '%s' paths '%s' " +
					"because some items are being processed  (Object State is system processing)",
				siteId, List.of(sourcePath, targetPath)));
		}
	}

	@Override
	public List<String> pasteItems(String siteId, Operation operation, String targetPath, PasteItem item)
		throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		// Lock the sandbox repository to prevent publish packages being submitted (cut-paste operations might conflict with submitted packages)
		String sandboxRepoLockKey = getSandboxRepoLockKey(siteId);
		generalLockService.lock(sandboxRepoLockKey);
		try {
			validatePasteItemsAction(siteId, operation, item.getPath(), targetPath);
			var pastedItems = new LinkedList<String>();

			switch (operation) {
				case COPY:
					copyPasteItemsInternal(siteId, targetPath, List.of(item), pastedItems);
					break;
				case CUT:
					pastedItems.add(cutPasteItems(siteId, targetPath, item));
					break;
			}
			logger.trace("'{}' items pasted in site '{}' from '{}' to '{}'",
				pastedItems.size(), siteId, item.getPath(), targetPath);
			return pastedItems;
		} finally {
			generalLockService.unlock(sandboxRepoLockKey);
		}
	}

	/**
	 * Performs a cut-paste operation.
	 *
	 * @param siteId     the site id
	 * @param targetPath the target path where the item will be pasted
	 * @param item       the item to be cut and pasted
	 * @return the new full path of the pasted item
	 * @throws ServiceLayerException   if an error occurs while performing the cut-paste operation
	 * @throws UserNotFoundException   if the user performing the operation is not found
	 * @throws AuthenticationException if the user is not authenticated
	 */
	protected String cutPasteItems(String siteId, String targetPath, PasteItem item)
		throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		String newTargetPath = constructNewPathForCutCopy(siteId, item.getPath(), targetPath);
		contentServiceV2.move(siteId, item.getPath(), newTargetPath);
		return newTargetPath;
	}

	/**
	 * Constructs a new path for cut/copy operations.
	 *
	 * @param site the site id
	 * @param from the source path of the content item to be cut/copy
	 * @param to   the target path where the content item will be pasted
	 * @return the full target path for the cut/copy operation, including the file name
	 * @throws ServiceLayerException if an error occurs while calculating the target path
	 */
	protected String constructNewPathForCutCopy(String site, String from, String to) throws ServiceLayerException {
		String sourcePath = from;
		String targetPath = to;
		if (isPageDescriptor(from) || isPageDescriptor(to)) {
			// Normalize the paths. If we're moving a page we need to move the folder anyway
			sourcePath = removeEnd(from, SLASH_INDEX_FILE);
			targetPath = removeEnd(to, SLASH_INDEX_FILE);
		}
		String result = constructNewPathForCutCopyInternal(site, sourcePath, targetPath);
		if (isPageDescriptor(from)) {
			result += SLASH_INDEX_FILE;
		}
		return result;
	}

	/**
	 * Constructs a new path for cut/copy operations.
	 * This will build the new path based on the source and target paths provided,
	 * and also check if the target path already exists, adjusting the name if necessary.
	 * <p>
	 * Notice that this method expects the fromPath and toPath NOT to contain the /index.xml portion
	 * of the path if they are page descriptors. For components and assets, they are expected to
	 * contain the full path
	 *
	 * @param site     the site id
	 * @param fromPath the source path of the content item to be cut/copy
	 * @param toPath   the target path where the content item will be pasted
	 * @return the full target path for the cut/copy operation, including the file name
	 * @throws ServiceLayerException if an error occurs while calculating the target path
	 */
	protected String constructNewPathForCutCopyInternal(String site, String fromPath, String toPath) throws ServiceLayerException {
		String result;

		// The following rules apply to content under the site folder
		String fromPathOnly = fromPath.substring(0, fromPath.lastIndexOf(FILE_SEPARATOR));
		String fromFileNameOnly = fromPath.substring(fromPath.lastIndexOf(FILE_SEPARATOR) + 1);
		logger.debug("Cut/copy name rules for site '{}' from path '{}' name '{}'", site,
			fromPathOnly, fromFileNameOnly);

		String newFileNameOnly = (toPath.contains(".xml")) ?
			toPath.substring(toPath.lastIndexOf(FILE_SEPARATOR) + 1) : fromFileNameOnly;

		logger.debug("Cut/copy name rules for site '{}' to path '{}' name '{}'", site, toPath, newFileNameOnly);

		String proposedDestPath;
		// Example NON INDEX FILES MOVE TO FOLDER
		// fromPath: "/site/website/search.xml"
		// toPath:   "/site/website/a-folder"
		// newPath:  "/site/website/products/a-folder/search.xml"
		//
		// Example  INDEX FILES MOVE to FOLDER
		// fromPath: "/site/website/search.xml"
		// toPath:   "/site/website/products/search.xml"
		// newPath:  "/site/website/products/search.xml"

		// Move location
		if (!contentRepository.contentExists(site, toPath) ||
			contentRepository.isFolder(site, toPath)) {
			proposedDestPath = toPath + FILE_SEPARATOR + fromFileNameOnly;
		} else {
			proposedDestPath = toPath;
		}

		logger.debug("Initial Proposed Path '{}' for site '{}' ", proposedDestPath, site);

		result = proposedDestPath;
		if (contentServiceV2.contentExists(site, proposedDestPath)) {
			result = adjustOnCollide(site, toPath, proposedDestPath);
		}

		logger.debug("Final proposed path in site '{}' from '{}' to '{}' final name '{}'", site, fromPath, toPath,
			proposedDestPath);
		return result;
	}

	/**
	 * Adjusts the destination path in case the target path already exists.
	 *
	 * @param site            the site id
	 * @param newPathOnly     the new path without the file name
	 * @param initialDestPath the initial destination path that was proposed
	 * @return the adjusted destination path
	 * @throws ServiceLayerException if an error occurs while calculating the target path
	 */
	private String adjustOnCollide(final String site,
								   final String newPathOnly, final String initialDestPath) throws ServiceLayerException {
		logger.debug("File already found at path '{}' in site '{}', create a new name", initialDestPath, site);
		try {
			String adjustedDestPath = initialDestPath;
			var siblings = contentRepository.getContentChildren(site, newPathOnly);
			var modifier = 1;
			var collisionFound = true;
			while (collisionFound) {
				var matcher = COPY_FILE_MODIFIER_PATTERN.matcher(adjustedDestPath);
				// check if the file already has a modifier (it is a copy of something)
				if (matcher.matches()) {
					// extract the values from the path
					var existingModifier = matcher.group(1); // the full modifier
					var modifierVersion = matcher.group(2); // the number of the modifier
					// remove the existing modifier
					adjustedDestPath = adjustedDestPath.replaceFirst(existingModifier, "");
					// calculate the new modifier
					modifier = Integer.parseInt(modifierVersion) + 1;
				}
				int pdpli = adjustedDestPath.lastIndexOf(".");
				if (pdpli == -1) pdpli = adjustedDestPath.length();
				adjustedDestPath = format(COPY_FILE_MODIFIER_FORMAT,
					adjustedDestPath.substring(0, pdpli), modifier, adjustedDestPath.substring(pdpli));

				// for pages, we have to check the parent folder, in any other case the full path
				String newCollisionCheck = adjustedDestPath;
				collisionFound = siblings.stream()
					.map(item -> item.path() + File.separator + item.name())
					.anyMatch(newCollisionCheck::equals);
			}

			return adjustedDestPath;
		} catch (Exception e) {
			throw new ServiceLayerException(format("Unable to generate an alternate path " +
				"for the name collision '%s' in site '%s'", initialDestPath, site), e);
		}
	}

	// Code based on the original clipboard service v1
	protected void copyPasteItemsInternal(String siteId, String targetPath, List<PasteItem> items,
										  List<String> pastedItems) throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		for (var item : items) {
			try {
				String newPath = contentService.copyContent(siteId, item.getPath(), targetPath);
				// recurse on copied children
				if (isNotEmpty(item.getChildren())) {
					copyPasteItemsInternal(siteId, newPath, item.getChildren(), pastedItems);
				}
				pastedItems.add(newPath);
			} catch (Exception e) {
				logger.error("Copy-Paste operation failed in site '{}' item '{}' to target path '{}'",
					siteId, item.getPath(), targetPath, e);
				throw e;
			}
		}
	}

	@RequireContentExists
	public String duplicateItem(@SiteId String siteId, @ContentPath String path) throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		String parentUrl = getParentUrl(path);
		var item = contentService.getContentItem(siteId, parentUrl, 0);
		return contentService.copyContent(siteId, path, item.uri);
	}

	/**
	 * Get the parent url: for folders and components it's just parent, for pages it's the parent of the parent.
	 * e.g.:
	 * /site/website/articles/page1/index.xml -> /site/website/articles
	 * /site/components/posts/january/clickbait.xml -> /site/components/posts/january
	 * /site/components/articles/health/ -> /site/components/articles
	 *
	 * @param path path of the content item
	 * @return path of the parent item
	 */
	protected String getParentUrl(String path) {
		return getFullPathNoEndSeparator(removeEnd(path, SLASH_INDEX_FILE));
	}

}
