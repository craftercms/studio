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
package org.craftercms.studio.impl.v1.service.content;

import jakarta.validation.Valid;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.AuthenticationException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.*;
import org.craftercms.studio.api.v1.service.dependency.DependencyService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.ContentTypeConfigTO;
import org.craftercms.studio.api.v1.to.DmOrderTO;
import org.craftercms.studio.api.v1.to.RenderingTemplateTO;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.event.content.ContentEvent;
import org.craftercms.studio.api.v2.event.lock.LockContentEvent;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.repository.RepositoryItem;
import org.craftercms.studio.api.v2.service.audit.ActivityStreamService;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.util.ContentItemOrderComparator;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.impl.v2.utils.TimeUtils;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.craftercms.studio.impl.v2.utils.spring.ContentResource;
import org.craftercms.studio.model.rest.Person;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v1.constant.DmConstants.*;
import static org.craftercms.studio.api.v1.constant.DmXmlConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.INDEX_FILE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.DOCUMENT_ELM_CONTENT_TYPE;
import static org.craftercms.studio.api.v2.dal.AuditLog.createAuditLogEntry;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.impl.v2.utils.DateUtils.getCurrentTimeIso;
import static org.craftercms.studio.impl.v2.utils.security.SecurityUtils.getCurrentUser;

/**
 * Content Services that other services may use
 *
 * @author russdanner
 */
public class ContentServiceImpl implements ContentService, ApplicationContextAware {
	// TODO: SJ: Refactor in 2.7.x to leverage Crafter Core as this will automatically enable inheritance, caching and
	// TODO: SJ: make that feature available to end user.
	private static final Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

	private static final String ELM_ORDER_DEFAULT_SELECTOR = "//" + DmXmlConstants.ELM_ORDER_DEFAULT;

	private GitContentRepository contentRepository;
	protected ServicesConfig servicesConfig;
	protected DependencyService dependencyService;
	protected org.craftercms.studio.api.v2.service.dependency.DependencyService dependencyServiceV2;
	protected DmPageNavigationOrderService dmPageNavigationOrderService;
	protected DmContentLifeCycleService dmContentLifeCycleService;
	protected SitesService siteService;
	protected ContentItemIdGenerator contentItemIdGenerator;
	protected StudioConfiguration studioConfiguration;
	protected ContentTypeService contentTypeService;
	protected EntitlementValidator entitlementValidator;
	protected AuditService auditService;
	protected ItemService itemService;
	protected UserService userService;
	protected ApplicationContext applicationContext;
	protected ActivityStreamService activityStreamService;
	protected PublishService publishService;

	protected org.craftercms.studio.api.v2.service.content.ContentService contentServiceV2;

	public final static Pattern COPY_FILE_MODIFIER_PATTERN = Pattern.compile(".+(-copy-(\\d+))(.+)?(\\..*)?");
	public final static String COPY_FILE_MODIFIER_FORMAT = "%s-copy-%s%s";

	public final static String INTERNAL_NAME_MODIFIER_PATTERN = "\\s\\(Copy \\d+\\)";
	public final static String INTERNAL_NAME_MODIFIER_FORMAT = "%s (Copy %s)";

	@Deprecated
	@Override
	@Valid
	public boolean contentExists(@ValidSiteId String site,
				     @ValidateSecurePathParam String path) {
		// TODO: SJ: Refactor in 2.7.x as this might already exists in Crafter Core (which is part of the new Studio)
		return this.contentRepository.contentExists(site, path);
	}

	@Override
	@Valid
	public boolean shallowContentExists(String site,
					    @ValidateSecurePathParam String path) {
		return this.contentRepository.shallowContentExists(site, path);
	}

	@Override
	@Valid
	public InputStream getContent(String site,
				      @ValidateSecurePathParam String path)
		throws ContentNotFoundException {
		// TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
		if (StringUtils.equals(site, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
			return this.contentRepository.getContent(StringUtils.EMPTY, path);
		} else {
			return this.contentRepository.getContent(site, path);
		}
	}

	@Override
	@Valid
	public long getContentSize(@ValidateStringParam String site,
				   @ValidateStringParam String path) {
		return contentRepository.getContentSize(site, path);
	}

	@Override
	@Valid
	public String getContentAsString(String site,
					 @ValidateSecurePathParam String path) {
		return getContentAsString(site, path, null);
	}

	@Override
	@Valid
	public void checkWriteAssetPath(@ValidateStringParam String path) throws ServiceLayerException {
		if (path.startsWith(SLASH_SITE)) {
			throw new ServiceLayerException(format("Unable to write asset content to the path '%s'.", path));
		}
	}

	@Override
	@LogExecutionTime
	public String shallowGetContentAsString(String siteId, String path) {
		return getContentAsStringInternal(siteId, path, null, true);
	}

	@Override
	@Valid
	public String getContentAsString(@ValidSiteId String site,
					 @ValidateSecurePathParam String path,
					 String encoding) {
		return getContentAsStringInternal(site, path, encoding, false);
	}

	private String getContentAsStringInternal(String site, String path, String encoding, boolean shallow) {
		return TimeUtils.logExecutionTime(() -> {
			String content = null;
			try (InputStream is = contentRepository.getContent(site, path, shallow)) {
				if (is != null) {
					if (isEmpty(encoding)) {
						content = IOUtils.toString(is, UTF_8);
					} else {
						content = IOUtils.toString(is, encoding);
					}
				}
			} catch (Exception e) {
				logger.debug("Failed to get content as string from site '{}' path '{}'", site, path, e);
			}
			return content;
		}, logger, format("Method 'ContentServiceImpl.getContentAsStringInternal(..)' with parameters %s", Arrays.asList(site, path, encoding, shallow)));
	}

	@Override
	@Valid
	public Document getContentAsDocument(@ValidateStringParam String site,
					     @ValidateSecurePathParam String path)
		throws DocumentException {
		// TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
		Document retDocument = null;
		InputStream is = null;
		try {
			is = this.getContent(site, path);
		} catch (ContentNotFoundException e) {
			logger.debug("Content not found at site '{}' path '{}'", site, path, e);
		}

		if (is != null) {
			try {
				SAXReader saxReader = new SAXReader();
				try {
					saxReader.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
					saxReader.setFeature("http://xml.org/sax/features/external-general-entities", false);
					saxReader.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
				} catch (SAXException e) {
					logger.error("Unable to turn off external entity loading, This could be a security risk.", e);
				}
				retDocument = saxReader.read(is);
			} finally {
				try {
					is.close();
				} catch (IOException e) {
					logger.debug("Failed to close the stream for item at site '{}' path '{}'", site, path, e);
				}
			}
		}

		return retDocument;
	}

	@Override
	@Valid
	public Resource getContentAsResource(@ValidateStringParam String site,
					     @ValidateSecurePathParam String path)
		throws ContentNotFoundException {
		if (contentExists(site, path)) {
			return new ContentResource(this, site, path);
		} else {
			throw new ContentNotFoundException(path, site,
				format("File '%s' not found in site '%s'", path, site));
		}
	}

	/**
	 * Notify when there is a content update
	 *
	 * @param site site name
	 * @param path path name
	 */
	@Override
	@Valid
	public void notifyContentEvent(@ValidateStringParam String site, @ValidateSecurePathParam String path) {
		applicationContext.publishEvent(new ContentEvent(SecurityUtils.getAuthentication(), site, path));
	}

	protected void updateDatabaseOnMove(String site, String fromPath, String movePath, String commitId)
			throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		updateDatabaseOnMove(site, fromPath, movePath, null, null, null, commitId);
	}

	protected void updateDatabaseOnMove(String site, String fromPath, String movePath, Long parentId, String label, String folderLabel, String commitId)
			throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		logger.debug("updateDatabaseOnMove from '{}' to '{}'", fromPath, movePath);

		String user = SecurityUtils.getCurrentUsername();

		Map<String, String> params = new HashMap<>();
		params.put(DmConstants.KEY_SOURCE_PATH, fromPath);
		params.put(DmConstants.KEY_TARGET_PATH, movePath);

		ContentItemTO renamedItem = getContentItem(site, movePath, 0);
		String contentType = renamedItem.getContentType();
		if (!renamedItem.isFolder()) {
			dmContentLifeCycleService.process(site, user, movePath, contentType, DmContentLifeCycleService
				.ContentLifeCycleOperation.RENAME, params);
			renamedItem = getContentItem(site, movePath, 0);
		}
		long userId = getCurrentUser().getId();
		// Item update
		itemService.moveItem(site, fromPath, movePath, parentId, label, userId);
		// Update folder when we are moving a /index.xml
		if (fromPath.contains(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
			String sourcePath = fromPath.substring(0, fromPath.lastIndexOf(FILE_SEPARATOR));
			String targetPath = movePath.substring(0, movePath.lastIndexOf(FILE_SEPARATOR));
			itemService.moveItem(site, sourcePath, targetPath, parentId, folderLabel, userId);
		}

		// write activity stream
		Site siteFeed = siteService.getSite(site);
		AuditLog auditLog = createAuditLogEntry();
		auditLog.setOperation(OPERATION_MOVE);
		auditLog.setSiteId(siteFeed.getId());
		auditLog.setCommitId(commitId);
		auditLog.setActorId(user);
		auditLog.setPrimaryTargetId(site + ":" + movePath);
		if (renamedItem.isFolder()) {
			auditLog.setPrimaryTargetType(TARGET_TYPE_FOLDER);
		} else {
			auditLog.setPrimaryTargetType(TARGET_TYPE_CONTENT_ITEM);
		}
		auditLog.setPrimaryTargetValue(movePath);
		auditLog.setPrimaryTargetSubtype(getContentTypeClass(site, movePath));
		auditService.insertAuditLog(auditLog);

		Item item = itemService.getItem(site, movePath);
		// This is not required, the current user is already loaded in memory
		User u = userService.getUserByIdOrUsername(-1, user);
		activityStreamService.insertActivity(siteFeed.getId(), u.getId(), OPERATION_MOVE,
			DateUtils.getCurrentTime(), item, null);

		updateDependenciesOnMove(site, fromPath, movePath);
	}

	protected void updateDependenciesOnMove(String site, String fromPath, String movePath) {
		try {
			dependencyServiceV2.deleteItemDependencies(site, fromPath);
		} catch (ServiceLayerException e) {
			logger.error("Failed to delete dependencies at site '{}' path '{}", site, fromPath, e);
		}
		try {
			dependencyServiceV2.upsertDependencies(site, movePath);
		} catch (ServiceLayerException e) {
			logger.error("Failed to update dependencies on move content at site '{}' path '{}'", site, movePath, e);
		}
	}

	protected void updateChildrenOnMove(String site, String fromPath, String movePath, String commitId)
			throws ServiceLayerException, UserNotFoundException, AuthenticationException {
		logger.debug("updateChildrenOnMove for site '{}' from '{}' to '{}'", site, fromPath, movePath);

		// get the list of children
		ContentItemTO movedTO = getContentItem(site, movePath, 2);
		List<ContentItemTO> childrenTOs = movedTO.getChildren();

		for (ContentItemTO childTO : childrenTOs) {
			// calculate the child's from path by looking at it's parent's from path and the child new path
			// (parent move operation has already happened)
			String childToPath = childTO.getUri();

			String oldParentFolderPath = fromPath.replace(SLASH_INDEX_FILE, "");
			String parentFolderPath = movePath.replace(SLASH_INDEX_FILE, "");

			String childFromPath = childToPath.replace(parentFolderPath, oldParentFolderPath);

			logger.debug("updateChildrenOnMove handle child in site '{}' from '{}' to '{}'", site,
				childFromPath, childToPath);

			// update database, preview, cache etc
			updateDatabaseOnMove(site, childFromPath, childToPath, commitId);

			// handle this child's children
			updateChildrenOnMove(site, childFromPath, childToPath, commitId);
		}
	}

	// TODO: Remove this now that we have it the clipboard service
	protected PastedPathMap constructNewPathForCutCopy(String site, String fromPath, String toPath,
							   boolean adjustOnCollide) throws ServiceLayerException {
		PastedPathMap result = new PastedPathMap();

		// The following rules apply to content under the site folder
		String fromPathOnly = fromPath.substring(0, fromPath.lastIndexOf(FILE_SEPARATOR));
		String fromFileNameOnly = fromPath.substring(fromPath.lastIndexOf(FILE_SEPARATOR) + 1);
		boolean fromFileIsIndex = (INDEX_FILE.equals(fromFileNameOnly));
		logger.debug("Cut/copy name rules for site '{}' from path '{}' name '{}'", site,
			fromPathOnly, fromFileNameOnly);

		if (fromFileIsIndex) {
			fromFileNameOnly = fromPathOnly.substring(fromPathOnly.lastIndexOf(FILE_SEPARATOR) + 1);
			fromPathOnly = fromPathOnly.substring(0, fromPathOnly.lastIndexOf(FILE_SEPARATOR));
			logger.debug("Cut/copy name rules index for site '{}' from path '{}' name '{}'", site,
				fromPathOnly, fromFileNameOnly);
		}

		String newPathOnly = (toPath.contains(".xml")) ?
			toPath.substring(0, toPath.lastIndexOf(FILE_SEPARATOR)) : toPath;
		String newFileNameOnly = (toPath.contains(".xml")) ?
			toPath.substring(toPath.lastIndexOf(FILE_SEPARATOR) + 1) : fromFileNameOnly;

		boolean newFileIsIndex = (INDEX_FILE.equals(newFileNameOnly));
		logger.debug("Cut/copy name rules for site '{}' to path '{}' name '{}'", site, newPathOnly, newFileNameOnly);
		if (newFileIsIndex) {
			newFileNameOnly = newPathOnly.substring(newPathOnly.lastIndexOf(FILE_SEPARATOR) + 1);
			newPathOnly = newPathOnly.substring(0, newPathOnly.lastIndexOf(FILE_SEPARATOR));
			logger.debug("Cut/copy name rules index for site '{}' to path '{}' name '{}'", site,
				newPathOnly, newFileNameOnly);
		}

		String proposedDestPath;
		String proposedDestPath_filename;
		String proposedDestPath_folder;
		boolean targetPathExistsPriorToOp = contentExists(site, toPath);

		if (fromFileIsIndex && newFileIsIndex) {
			// Example MOVE LOCATION, INDEX FILES
			// fromPath: "/site/website/search/index.xml"
			// toPath:   "/site/website/products/index.xml"
			// newPath:  "/site/website/products/search/index.xml"
			//
			// Example RENAME, INDEX FILES
			// fromPath: "/site/website/en/services/index.xml"
			// toPath:   "/site/website/en/services-updated/index.xml"
			// newPath:  "/site/website/en/services-updated/index.xml
			if (newPathOnly.equals(fromPathOnly) && !targetPathExistsPriorToOp) {
				// this is a rename
				proposedDestPath = newPathOnly + FILE_SEPARATOR + newFileNameOnly + FILE_SEPARATOR +
					DmConstants.INDEX_FILE;
				proposedDestPath_filename = DmConstants.INDEX_FILE;
				proposedDestPath_folder = newFileNameOnly;
			} else {
				// this is a location move
				proposedDestPath = newPathOnly + FILE_SEPARATOR + newFileNameOnly + FILE_SEPARATOR +
					fromFileNameOnly + FILE_SEPARATOR + DmConstants.INDEX_FILE;
				proposedDestPath_filename = DmConstants.INDEX_FILE;
				proposedDestPath_folder = newFileNameOnly;
			}
		} else if (fromFileIsIndex) {
			// Example MOVE LOCATION, INDEX TO FOLDER
			// fromPath: "/site/website/search/index.xml"
			// toPath:   "/site/website/a-folder"
			// newPath:  "/site/website/a-folder/search/index.xml"
			proposedDestPath = newPathOnly + FILE_SEPARATOR + fromFileNameOnly + FILE_SEPARATOR +
				DmConstants.INDEX_FILE;
			proposedDestPath_filename = DmConstants.INDEX_FILE;
			proposedDestPath_folder = fromFileNameOnly;
		} else if (newFileIsIndex) {
			proposedDestPath = newPathOnly + FILE_SEPARATOR + newFileNameOnly + FILE_SEPARATOR + fromFileNameOnly;
			proposedDestPath_filename = fromFileNameOnly;
			proposedDestPath_folder = newFileNameOnly;
		} else {
			// Example NON INDEX FILES MOVE TO FOLDER
			// fromPath: "/site/website/search.xml"
			// toPath:   "/site/website/a-folder"
			// newPath:  "/site/website/products/a-folder/search.xml"
			//
			// Example  INDEX FILES MOVE to FOLDER
			// fromPath: "/site/website/search.xml"
			// toPath:   "/site/website/products/search.xml"
			// newPath:  "/site/website/products/search.xml"
			if (fromFileNameOnly.equals(newFileNameOnly)) {
				// Move location
				if (!contentRepository.contentExists(site, newPathOnly) ||
					contentRepository.isFolder(site, newPathOnly)) {
					proposedDestPath = newPathOnly + FILE_SEPARATOR + fromFileNameOnly;
				} else {
					proposedDestPath = newPathOnly;
				}
				proposedDestPath_filename = fromFileNameOnly;
			} else {
				// rename
				proposedDestPath = newPathOnly + FILE_SEPARATOR + newFileNameOnly;
				proposedDestPath_filename = newFileNameOnly;
			}
			proposedDestPath_folder = newPathOnly.substring(0, newPathOnly.lastIndexOf(FILE_SEPARATOR));
		}

		logger.debug("Initial Proposed Path '{}' for site '{}' ", proposedDestPath, site);

		result.filePath = proposedDestPath;
		result.fileName = proposedDestPath_filename;
		result.fileFolder = proposedDestPath_folder;
		result.modifier = "";

		if (adjustOnCollide && contentExists(site, proposedDestPath)) {
			adjustOnCollide(site, result, fromFileIsIndex, newFileIsIndex, newPathOnly, proposedDestPath, proposedDestPath_filename, proposedDestPath_folder);
		}

		logger.debug("Final proposed path in site '{}' from '{}' to '{}' final name '{}'", site, fromPath, toPath,
			proposedDestPath);
		return result;
	}

	private void adjustOnCollide(final String site, final PastedPathMap result, final boolean fromFileIsIndex, final boolean newFileIsIndex,
				     final String newPathOnly, final String initialDestPath, final String initialDestFilename,
				     final String initialDestFolder) throws ServiceLayerException {
		logger.debug("File already found at path '{}' in site '{}', create a new name", initialDestPath, site);
		try {
			String adjustedDestPath = initialDestPath;
			String adjustedDestFilename = initialDestFilename;
			String adjustedDestFolder = initialDestFolder;
			String pasteTargetFolder = newFileIsIndex ?
				newPathOnly + File.separator + adjustedDestFolder :
				newPathOnly;
			var siblings = contentRepository.getContentChildren(site, pasteTargetFolder);
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
				if (!adjustedDestPath.contains(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
					int pdpli = adjustedDestPath.lastIndexOf(".");
					if (pdpli == -1) pdpli = adjustedDestPath.length();
					adjustedDestPath = format(COPY_FILE_MODIFIER_FORMAT,
						adjustedDestPath.substring(0, pdpli), modifier, adjustedDestPath.substring(pdpli));

					// a regex would be better
					adjustedDestFilename =
						adjustedDestPath.substring(adjustedDestPath.lastIndexOf(FILE_SEPARATOR) + 1);
					adjustedDestFolder =
						adjustedDestPath.substring(0, adjustedDestPath.lastIndexOf(FILE_SEPARATOR));
				} else {
					adjustedDestPath = format(COPY_FILE_MODIFIER_FORMAT,
						adjustedDestPath.substring(0,
							adjustedDestPath.indexOf(FILE_SEPARATOR + DmConstants.INDEX_FILE)),
						modifier,
						adjustedDestPath.substring(
							adjustedDestPath.lastIndexOf(FILE_SEPARATOR + DmConstants.INDEX_FILE)));

					adjustedDestFilename = DmConstants.INDEX_FILE;
					adjustedDestFolder =
						adjustedDestPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
				}
				adjustedDestFolder =
					adjustedDestFolder.substring(adjustedDestFolder.lastIndexOf(FILE_SEPARATOR) + 1);
				// for pages, we have to check the parent folder, in any other case the full path
				String newCollisionCheck = fromFileIsIndex ?
					pasteTargetFolder + File.separator + adjustedDestFolder :
					adjustedDestPath;
				collisionFound = siblings.stream()
					.map(item -> item.path() + File.separator + item.name())
					.anyMatch(newCollisionCheck::equals);
			}

			result.filePath = adjustedDestPath;
			result.fileName = adjustedDestFilename;
			result.fileFolder = adjustedDestFolder;
			result.modifier = Integer.toString(modifier);
			result.altName = true;
		} catch (Exception e) {
			throw new ServiceLayerException(format("Unable to generate an alternate path " +
				"for the name collision '%s' in site '%s'", initialDestPath, site), e);
		}
	}

	/**
	 * Helper method to update a single node element with the indicated value
	 *
	 * @param root     root element
	 * @param nodeName name of the node to update
	 * @param value    new text value of the node, if found
	 */
	private void updateSingleDocumentNode(final Element root, final String nodeName, final String value) {
		Node node = root.selectSingleNode(format("//%s", nodeName));
		if (node != null) {
			node.setText(value);
		}
	}

	/**
	 * Updates the XML after a move operation.
	 *
	 * @param root     root element
	 * @param filename new filename
	 * @param folder   new folder
	 * @param modifier numeric modifier used to update the internal-name. e.g.: for modifier 3, the internal-name
	 *                 will set to something like "Initial Name (Copy 3)"
	 */
	protected void updateContentOnMove(final Element root, final String filename, final String folder,
					   final String modifier) {
		updateSingleDocumentNode(root, ELM_FILE_NAME, filename);
		updateSingleDocumentNode(root, ELM_FOLDER_NAME, folder);

		if (StringUtils.isNotEmpty(modifier)) {
			Node internalNameNode = root.selectSingleNode("//" + ELM_INTERNAL_NAME);
			if (internalNameNode != null) {
				String internalNameValue = internalNameNode.getText().replaceFirst(INTERNAL_NAME_MODIFIER_PATTERN, "");
				internalNameNode.setText(format(INTERNAL_NAME_MODIFIER_FORMAT, internalNameValue, modifier));
			}
		}

		String nowFormatted = getCurrentTimeIso();
		updateSingleDocumentNode(root, ELM_LAST_MODIFIED_DATE, nowFormatted);
		updateSingleDocumentNode(root, ELM_LAST_MODIFIED_DATE_DT, nowFormatted);
	}
	/* ======================== */

	protected ContentItemTO createNewContentItemTO(String site, String contentPath) {
		ContentItemTO item = new ContentItemTO();
		// FIXME: SJ: This is another workaround for UI issues
		contentPath = FilenameUtils.normalize(contentPath, true);

		item.uri = contentPath;
		item.path = contentPath.substring(0, contentPath.lastIndexOf(FILE_SEPARATOR));
		item.name = contentPath.substring(contentPath.lastIndexOf(FILE_SEPARATOR) + 1);

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
		item.folder = contentRepository.isFolder(site, contentPath);

		return item;
	}

	protected ContentItemTO populateContentDrivenProperties(String site, ContentItemTO item)
		throws Exception {
		// This method load an XML content item and populates properties in the TO from the XML
		// TODO: SJ: Two problems here that need to be fixed in 3.1+
		// TODO: SJ: Use Crafter Core for some/all of this work
		// TODO: SJ: Much of this seems hardcoded and must be extensible/configurable via key:xpath in config
		String contentPath = item.uri;

		logger.debug("Populate the page props at site '{}' path '{}'", site, contentPath);
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

		if (item.page) {
			// TODO: SJ: This is hokey, fix in 4.x
			// TODO: Also fix to use File.separator
			/* Use to be:
			contentPath.replace(FILE_SEPARATOR + "site" + FILE_SEPARATOR + "website", "")
			.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, ""); */
			item.browserUri =
				contentPath.replace(SLASH_SITE_WEBSITE, "")
					.replace(SLASH_INDEX_FILE, "");
		}

		Document contentDoc = this.getContentAsDocument(site, contentPath);
		if (contentDoc != null) {
			Element rootElement = contentDoc.getRootElement();

			String internalName = rootElement.valueOf("internal-name");
			String contentType = rootElement.valueOf("content-type");
			String disabled = rootElement.valueOf("disabled");
			String savedAsDraft = rootElement.valueOf("savedAsDraft");
			String navigation = rootElement.valueOf("placeInNav");
			String hideInAuthoring = rootElement.valueOf("hideInAuthoring");
			String displayTemplate = rootElement.valueOf("display-template");

			item.internalName = internalName;
			item.contentType = contentType;
			item.disabled = "true".equalsIgnoreCase(disabled);
			item.savedAsDraft = "true".equalsIgnoreCase(savedAsDraft);
			item.hideInAuthoring = "true".equalsIgnoreCase(hideInAuthoring);

			item.navigation = "true".equalsIgnoreCase(navigation);
			item.floating = !item.navigation;

			item.setOrders(getItemOrders(rootElement.selectNodes(ELM_ORDER_DEFAULT_SELECTOR)));

			if (displayTemplate != null) {
				RenderingTemplateTO template = new RenderingTemplateTO();
				template.uri = displayTemplate;
				template.name = "DEFAULT";      // FIXME: SJ: 3.1+

				item.renderingTemplates.add(template);
			}
		} else {
			logger.debug("Failed to load the XML document at site '{}' path '{}'", site, contentPath);
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
	 */
	protected void addOrderValue(List<DmOrderTO> orders, String orderName, String orderStr) {
		Double orderValue = null;
		try {
			orderValue = Double.parseDouble(orderStr);
		} catch (NumberFormatException e) {
			logger.debug("'{}', '{}' is not a valid order value pair.", orderName, orderStr);
		}
		if (!isEmpty(orderName) && orderValue != null) {
			DmOrderTO order = new DmOrderTO();
			order.setId(orderName);
			order.setOrder(orderValue);
			orders.add(order);
		}
	}

	/**
	 * get WCM content item order metadata
	 *
	 * @return item orders metadata
	 */
	protected List<DmOrderTO> getItemOrders(List<Node> nodes) {
		// TODO: SJ: Rewrite this and the whole order/sort system; 3.1+
		if (nodes == null) {
			return null;
		}
		List<DmOrderTO> orders = new ArrayList<>(nodes.size());
		for (Node node : nodes) {

			String orderStr = node.getText();
			addOrderValue(orders, DmConstants.JSON_KEY_ORDER_DEFAULT, orderStr);
		}
		return orders;
	}

	protected ContentItemTO populateItemChildren(ContentItemTO item, int depth) throws ServiceLayerException {
		// TODO: SJ: Refactor  in 3.1+
		String contentPath = item.uri;

		item.children = new ArrayList<>();
		item.numOfChildren = 0;

		if (contentPath.contains(FILE_SEPARATOR + DmConstants.INDEX_FILE)
			|| !contentPath.contains(".")) { // item.isFolder?

			if (contentPath.contains(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
				contentPath = contentPath.replace(FILE_SEPARATOR + DmConstants.INDEX_FILE, "");
			}

			Collection<RepositoryItem> childRepoItems = contentRepository.getContentChildren(item.site, contentPath);
			boolean indexFound = false;

			if (childRepoItems != null) {
				item.numOfChildren = childRepoItems.size();
				if (item.numOfChildren != 0) {
					item.isContainer = true;
					item.container = true;
				}

				// TODO: Fix the mixed use of constants here and eliminate unnecessary string concatenation during
				// TODO: execution
				List<ContentItemTO> children = new ArrayList<>();
				logger.debug("Check if '{}' has an index.xml", contentPath);
				for (RepositoryItem childRepoItem : childRepoItems) {
					if (INDEX_FILE.equals(childRepoItem.name())) {
						if (!item.uri.contains(FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
							item.path = item.uri;
							item.uri = item.uri + DmConstants.SLASH_INDEX_FILE;
						}
						item.numOfChildren--;
						indexFound = true;
					} else {
						if (depth > 1) {
							String childPath = childRepoItem.path() + FILE_SEPARATOR + childRepoItem.name();
							// TODO: Consider using shallowContentExists
							if (childPath.startsWith(FILE_SEPARATOR + "site" + FILE_SEPARATOR + "website" +
								FILE_SEPARATOR) && childRepoItem.isFolder() &&
								contentExists(item.site, childPath + FILE_SEPARATOR + DmConstants.INDEX_FILE)) {
								children.add(getContentItem(item.site, childPath + FILE_SEPARATOR +
									DmConstants.INDEX_FILE, depth - 1));
							} else {
								children.add(getContentItem(item.site, childPath, depth - 1));
							}
						}
					}
				}

				if (!indexFound && contentRepository.isFolder(item.site, contentPath)) {
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
					// TODO: Switch to a proper constant
					item.contentType = "folder";
					item.path = item.uri;
				}

				// ORDER THE CHILDREN
				// level descriptors first
				// nav pages by order
				// floating pages via Alpha
				Comparator<ContentItemTO> comparator =
					new ContentItemOrderComparator("default", true, true, true);
				children.sort(comparator);
				item.children = children;

			} else {
				// ITEM HAS NO CHILDREN
				item.isContainer = true;
				item.container = true;
			}
		} else {
			// ITEM IS A STAND-ALONE XML
			item.isContainer = false;
			item.container = false;
		}

		if (item.internalName == null) item.internalName = item.name;

		return item;
	}

	@Override
	@Valid
	public ContentItemTO getContentItem(@ValidateStringParam String site,
					    @ValidateSecurePathParam String path) {
		return getContentItem(site, path, 2);
	}

	@Override
	@Valid
	@LogExecutionTime
	public ContentItemTO getContentItem(@ValidSiteId String site,
					    @ValidateSecurePathParam String path,
					    int depth) {
		ContentItemTO item = null;
		logger.debug("Get content item at site '{}' path '{}' depth '{}'", site, path, depth);

		try {
			if (contentExists(site, path)) {
				// get item from cache
				item = loadContentItem(site, path);

				if (depth != 0) {
					item = populateItemChildren(item, depth);
				}

				// POPULATE LOCK STATUS
				populateMetadata(site, item, path);

				// POPULATE WORKFLOW STATUS
				if (!item.isFolder() || item.isContainer()) {
					populateWorkflowProperties(site, item);
				} else {
					item.setNew(!itemService.isNew(site, item.getUri()));
					item.isNew = item.isNew();
				}
			} else {
				item = createDummyDmContentItemForDeletedNode(site, path);
			}
		} catch (Exception e) {
			logger.debug("Failed to construct the item at site '{}' path '{}'", site, path, e);
		}

		return item;
	}

	@Override
	@RequireSiteExists
	public String getItemContentType(@SiteId String site, String path) throws DocumentException, SiteNotFoundException {
		Item item = itemService.getItem(site, path);
		if (item == null) {
			return getContentTypeClass(site, path);
		}
		Pattern taxonomyPattern = Pattern.compile(CONTENT_TYPE_TAXONOMY_REGEX);
		Matcher matcher = taxonomyPattern.matcher(path);
		if (matcher.matches()) {
			return CONTENT_TYPE_TAXONOMY;
		}
		if (isNotEmpty(item.getContentTypeId())) {
			return item.getContentTypeId();
		}
		if (CONTENT_TYPE_FOLDER.equals(item.getSystemType())) {
			return CONTENT_TYPE_FOLDER;
		}
		if (path.endsWith(XML_PATTERN) && !path.startsWith(CONFIG_PATH_ROOT)) {
			Document contentDoc = this.getContentAsDocument(site, path);
			if (contentDoc != null) {
				Element rootElement = contentDoc.getRootElement();
				return rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
			}
		}
		return getContentTypeClass(site, path);
	}

	protected ContentItemTO loadContentItem(String site, String path) throws SiteNotFoundException {
		// TODO: SJ: Refactor such that the populate of non-XML is also a method in 3.1+
		ContentItemTO item = createNewContentItemTO(site, path);

		// TODO: Fix to use proper constants
		if (item.uri.endsWith(".xml") && !item.uri.startsWith("/config/")) {

			try {
				item = populateContentDrivenProperties(site, item);
			} catch (Exception e) {
				logger.debug("Failed to construct the item at site '{}' path '{}'", site, path, e);
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
			item.browserUri = item.getUri();
			item.setContentType(getContentTypeClass(site, path));
		}

		loadContentTypeProperties(site, item, item.contentType);

		String mimeType = StudioUtils.getMimeType(item.getName());
		if (StringUtils.isNotEmpty(mimeType)) {
			item.setMimeType(mimeType);
		}
		return item;
	}

	protected void loadContentTypeProperties(String site, ContentItemTO item, String contentType) throws SiteNotFoundException {
		// TODO: SJ: Refactor in 2.7.x
		if (item.isFolder()) {
			item.setContentType(CONTENT_TYPE_FOLDER);
		} else {
			// TODO: Use constants instead of string literals
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
				String mimeType = StudioUtils.getMimeType(item.getName());
				if (mimeType != null && !isEmpty(mimeType)) {
					item.setPreviewable(ContentUtils.matchesPatterns(mimeType, servicesConfig
						.getPreviewableMimetypesPaterns(site)));
					item.isPreviewable = item.previewable;
				}
			}
		}
		// TODO CodeRev:but what if the config is null?
	}

	protected void populateWorkflowProperties(String site, ContentItemTO item) {
		Item it = itemService.getItem(site, item.getUri());
		if (it != null) {
			if (item.isFolder()) {
				boolean liveFolder = isLive(it.getState());
				item.setNew(!liveFolder);
				item.setLive(liveFolder);
			} else {
				item.setNew(isNew(it.getState()));
				item.setLive(isLive(it.getState()));
				item.setStaged(isStaged(it.getState()));
			}
			item.isNew = item.isNew();
			item.isLive = item.isLive();
			item.isStaged = item.isStaged();
			item.setInProgress(!item.isLive());
			item.isInProgress = item.isInProgress();
			item.setScheduled(isScheduled(it.getState()));
			item.isScheduled = item.isScheduled();
			item.setSubmitted(isInWorkflow(it.getState()));
			item.isSubmitted = item.isSubmitted();
			item.setInFlight(isSystemProcessing(it.getState()));
			item.isInFlight = item.isInFlight();
		} else {
			if (item.isFolder()) {
				boolean liveFolder = isLive(it.getState());
				boolean stagedFolder = isStaged(it.getState());
				item.setNew(!liveFolder);
				item.setLive(liveFolder);
				item.setStaged(stagedFolder);
				item.isNew = item.isNew();
				item.isLive = item.isLive();
				item.isStaged = item.isStaged();
				item.setInProgress(!item.isLive());
				item.isInProgress = item.isInProgress();
			}
		}
	}

	protected void populateMetadata(final String site, final ContentItemTO item, final String path)
		throws ServiceLayerException, UserNotFoundException {
		// TODO: SJ: Refactor to return a ContentItemTO instead of changing the parameter
		// TODO: SJ: Change method name to be getContentItemMetadata or similar
		// TODO: SJ: 3.1+

		// TODO: SJ: Create a method String getValueIfNotNull(String) to use to return not null/empty string if null
		// TODO: SJ: Use that method to reduce redundant code here. 3.1+
		Item metadata = itemService.getItem(site, item.getUri());
		if (metadata == null) {
			item.setLockOwner("");
			return;
		}
		// Set the lock owner to empty string if we get a null to not confuse the UI, or set it to what's in the
		// database if it's not null
		if (isNull(metadata.getLockOwner())) {
			item.setLockOwner("");
		} else {
			item.setLockOwner(metadata.getLockOwner().getUsername());
		}

		Person modifier = metadata.getModifier();
		String modifierUsername = modifier != null ? modifier.getUsername() : null;
		// Set the modifier (user) if known
		if (isEmpty(modifierUsername)) {
			item.setUser("");
			item.setUserLastName("");
			item.setUserFirstName("");
		} else {
			User u = userService.getUserByIdOrUsername(-1, modifierUsername);
			item.user = modifierUsername;
			item.setUser(modifierUsername);
			item.userFirstName = u.getFirstName();
			item.setUserFirstName(u.getFirstName());
			item.userLastName = u.getLastName();
			item.setUserLastName(u.getLastName());
		}

		if (metadata.getLastModifiedOn() != null) {
			item.setLastEditDate(metadata.getLastModifiedOn());
			item.setEventDate(metadata.getLastModifiedOn());
		}

		if (metadata.getLastPublishedOn() != null) {
			item.setPublished(true);
			item.setPublishedDate(metadata.getLastPublishedOn());
		}

		PublishPackage publishPackage = publishService.getReadyPackageForItem(site, path, false);
		if (publishPackage != null) {
			if (publishPackage.getSchedule() != null) {
				item.setScheduledDate(publishPackage.getSchedule().atZone(ZoneOffset.UTC));
			}
			if (StringUtils.isNotEmpty(publishPackage.getSubmitterComment())) {
				item.setSubmissionComment(publishPackage.getSubmitterComment());
			}
			if (StringUtils.isNotEmpty(publishPackage.getTarget())) {
				item.setSubmittedToEnvironment(publishPackage.getTarget());
			}
			item.setSubmitted(true);
		}
	}

	@Override
	@Valid
	@LogExecutionTime
	public ContentItemTO getContentItemTree(@ValidateStringParam String site,
						@ValidateSecurePathParam String path,
						int depth) {
		logger.debug("Get the content item tree for item at site '{}' path '{}' with depth '{}'", site, path, depth);

		boolean isPages = (path.contains(DmConstants.SLASH_SITE_WEBSITE));
		ContentItemTO root;

		if (isPages && contentExists(site, path + DmConstants.SLASH_INDEX_FILE)) {
			if (depth > 1) {
				root = getContentItem(site, path + DmConstants.SLASH_INDEX_FILE, depth);
			} else {
				root = getContentItem(site, path + DmConstants.SLASH_INDEX_FILE);
			}
		} else {
			if (depth > 1) {
				root = getContentItem(site, path, depth);
			} else {
				root = getContentItem(site, path);
			}
		}

		return root;
	}

	@Override
	@Valid
	public Optional<Resource> getContentVersion(@ValidateStringParam String site,
						    @ValidateSecurePathParam String path,
						    @ValidateStringParam() String commitId) {
		return contentRepository.getContentByCommitId(site, path, commitId);
	}

	@Override
	@Valid
	public String getContentVersionAsString(@ValidateStringParam String site,
						@ValidateSecurePathParam String path,
						@ValidateStringParam() String version) {
		try {
			Optional<Resource> resource = getContentVersion(site, path, version);
			if (resource.isPresent()) {
				try (InputStream is = resource.get().getInputStream()) {
					return IOUtils.toString(is, UTF_8);
				}
			}
		} catch (Exception e) {
			logger.debug("Failed to get content as a string from site '{}' path '{}'", site, path, e);
		}

		return null;
	}

	@Override
	@Valid
	public ContentItemTO createDummyDmContentItemForDeletedNode(@ValidateStringParam String site,
								    @ValidateSecurePathParam()
								    String relativePath) throws SiteNotFoundException {
		// TODO: SJ: Think of another way to do this in 3.1+
		ContentItemTO item = new ContentItemTO();
		item.timezone = servicesConfig.getDefaultTimezone(site);
		String name = ContentUtils.getPageName(relativePath);
		// TODO: Use a helper method to remove the leading '/', consider StringUtils.removeStart or similar
		String folderPath = (name.equals(DmConstants.INDEX_FILE)) ?
			relativePath.replace(FILE_SEPARATOR + name, "") : relativePath;
		item.path = folderPath;
		/*
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
		browserUri = browserUri.replaceFirst(DmConstants.SLASH_INDEX_FILE, "");
		if (browserUri.isEmpty()) {
			browserUri = FILE_SEPARATOR;
		}
		// TODO: come up with a better way of doing this.
		if (isPage) {
			browserUri = browserUri.replaceFirst("\\.xml", ".html");
		}
		return browserUri;
	}

	@Override
	@Valid
	public String getContentTypeClass(@ValidateStringParam String site, String uri) throws SiteNotFoundException {
		// TODO: SJ: This reads: if can't guess what it is, it's a page. This is to be replaced in 3.1+
		if (uri.endsWith(FILE_SEPARATOR + servicesConfig.getLevelDescriptorName(site))) {
			return CONTENT_TYPE_LEVEL_DESCRIPTOR;
		} else if (matchesPatterns(uri, servicesConfig.getPagePatterns(site))) {
			return CONTENT_TYPE_PAGE;
		} else if (matchesPatterns(uri, servicesConfig.getComponentPatterns(site))) {
			return CONTENT_TYPE_COMPONENT;
		} else if (matchesPatterns(uri, servicesConfig.getDocumentPatterns(site))) {
			return CONTENT_TYPE_DOCUMENT;
		} else if (matchesPatterns(uri, servicesConfig.getAssetPatterns(site))) {
			return CONTENT_TYPE_ASSET;
		} else if (matchesPatterns(uri, servicesConfig.getRenderingTemplatePatterns(site))) {
			return CONTENT_TYPE_RENDERING_TEMPLATE;
		} else if (StringUtils.startsWith(uri, contentTypeService.getConfigPath())) {
			return CONTENT_TYPE_CONTENT_TYPE;
		} else if (matchesPatterns(uri, List.of(CONTENT_TYPE_TAXONOMY_REGEX))) {
			return CONTENT_TYPE_TAXONOMY;
		} else if (matchesPatterns(uri, servicesConfig.getScriptsPatterns(site))) {
			return CONTENT_TYPE_SCRIPT;
		} else if (matchesPatterns(uri, servicesConfig.getConfigurationPatterns(site))) {
			return CONTENT_TYPE_CONFIGURATION;
		}

		return CONTENT_TYPE_FILE;
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
	@Valid
	public void lockContent(@ValidateStringParam String site,
				@ValidateSecurePathParam String path)
		throws UserNotFoundException, ServiceLayerException {
		// TODO: SJ: Where is the object state update to indicate item is now locked?
		// TODO: SJ: Dejan to look into this
		contentRepository.lockItem(site, path);
		itemService.lockItemByPath(site, path, SecurityUtils.getCurrentUsername());
		applicationContext.publishEvent(new LockContentEvent(SecurityUtils.getAuthentication(), site, path, true));
	}

	@Override
	@Valid
	public List<DmOrderTO> getItemOrders(@ValidateStringParam String site,
					     @ValidateSecurePathParam String path) {
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
		if (!isEmpty(relativePath)) {
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
			List<DmOrderTO> orders = new ArrayList<>(item.getChildren().size());
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
	@Valid
	public double reorderItems(@ValidateStringParam String site,
				   @ValidateSecurePathParam() String relativePath,
				   @ValidateSecurePathParam() String before,
				   @ValidateSecurePathParam() String after,
				   @ValidateStringParam() String orderName) {
		Double beforeOrder = null;
		Double afterOrder = null;
		DmOrderTO beforeOrderTO = null;
		DmOrderTO afterOrderTO = null;
		// get the order of the content before
		// if the path is not provided, the order is 0
		if (!isEmpty(before)) {
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
		if (!isEmpty(after)) {
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
				ContentUtils.getParentUrl(relativePath));
		} else if (beforeOrder == null) {
			return (0 + afterOrder) / 2;
		} else if (afterOrder == null) {
			return dmPageNavigationOrderService.getNewNavOrder(site,
				ContentUtils.getParentUrl(relativePath), beforeOrder);
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
					String orderName) {
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
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setServicesConfig(ServicesConfig servicesConfig) {
		this.servicesConfig = servicesConfig;
	}

	public void setDependencyService(DependencyService dependencyService) {
		this.dependencyService = dependencyService;
	}

	@SuppressWarnings("unused")
	public void setDependencyServiceV2(org.craftercms.studio.api.v2.service.dependency.DependencyService dependencyServiceV2) {
		this.dependencyServiceV2 = dependencyServiceV2;
	}

	@SuppressWarnings("unused")
	public void setDmPageNavigationOrderService(DmPageNavigationOrderService dmPageNavigationOrderService) {
		this.dmPageNavigationOrderService = dmPageNavigationOrderService;
	}


	@SuppressWarnings("unused")
	public void setDmContentLifeCycleService(DmContentLifeCycleService dmContentLifeCycleService) {
		this.dmContentLifeCycleService = dmContentLifeCycleService;
	}

	public void setSiteService(final SitesService siteService) {
		this.siteService = siteService;
	}

	@SuppressWarnings("unused")
	public void setContentItemIdGenerator(ContentItemIdGenerator contentItemIdGenerator) {
		this.contentItemIdGenerator = contentItemIdGenerator;
	}

	public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
		this.studioConfiguration = studioConfiguration;
	}

	public void setContentTypeService(ContentTypeService contentTypeService) {
		this.contentTypeService = contentTypeService;
	}

	@SuppressWarnings("unused")
	public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
		this.entitlementValidator = entitlementValidator;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public void setContentRepository(GitContentRepository contentRepository) {
		this.contentRepository = contentRepository;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	public void setUserService(final UserService userService) {
		this.userService = userService;
	}

	@SuppressWarnings("unused")
	public void setActivityStreamService(ActivityStreamService activityStreamService) {
		this.activityStreamService = activityStreamService;
	}

	@SuppressWarnings("unused")
	public void setContentServiceV2(org.craftercms.studio.api.v2.service.content.ContentService contentServiceV2) {
		this.contentServiceV2 = contentServiceV2;
	}

	public void setPublishService(final PublishService publishService) {
		this.publishService = publishService;
	}

	/**
	 * Simple Object to hold result of calculating target paths for copy/cut and paste operation.
	 */
	// TODO: remove
	protected static class PastedPathMap {
		protected String filePath;
		protected String fileName;
		protected String fileFolder;
		protected String modifier;
		protected boolean altName;
	}

}
