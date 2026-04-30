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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.craftercms.commons.validation.annotations.param.ValidSiteId;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.constant.DmXmlConstants;
import org.craftercms.studio.api.v1.exception.ContentNotFoundException;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.to.ContentItemTO;
import org.craftercms.studio.api.v1.to.DmOrderTO;
import org.craftercms.studio.api.v1.to.RenderingTemplateTO;
import org.craftercms.studio.api.v2.annotation.LogExecutionTime;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.repository.RepositoryItem;
import org.craftercms.studio.api.v2.service.content.ContentTypeService;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.publish.PublishService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v1.util.ContentItemOrderComparator;
import org.craftercms.studio.impl.v1.util.ContentUtils;
import org.craftercms.studio.model.contentType.ContentType;
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
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneOffset;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.craftercms.studio.api.v1.constant.DmConstants.SLASH_INDEX_FILE;
import static org.craftercms.studio.api.v1.constant.DmConstants.SLASH_SITE_WEBSITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_SITE_CONTENT_TYPES_CONFIG_BASE_PATH;
import static org.craftercms.studio.api.v2.utils.StudioUtils.matchesPatterns;

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
	protected StudioConfiguration studioConfiguration;
	protected ItemService itemService;
	protected UserService userService;
	protected ApplicationContext applicationContext;
	protected PublishService publishService;
	protected ContentTypeService contentTypeService;

	@Deprecated
	@Valid
	protected boolean contentExists(@ValidSiteId String site,
				     @ValidateSecurePathParam String path) {
		// TODO: SJ: Refactor in 2.7.x as this might already exists in Crafter Core (which is part of the new Studio)
		return this.contentRepository.contentExists(site, path);
	}

	@Valid
	protected InputStream getContent(String site,
				      @ValidateSecurePathParam String path)
		throws ContentNotFoundException {
		// TODO: SJ: Refactor in 4.x as this already exists in Crafter Core (which is part of the new Studio)
		if (StringUtils.equals(site, studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE))) {
			return this.contentRepository.getContent(StringUtils.EMPTY, path);
		} else {
			return this.contentRepository.getContent(site, path);
		}
	}

	@Valid
	protected Document getContentAsDocument(@ValidateStringParam String site,
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
		item.page = matchesPatterns(item.getUri(), servicesConfig.getPagePatterns(site));
		item.isPage = item.page;
		item.previewable = item.page;               // TODO: SJ: This and item below are duplicated due to UI issues
		item.isPreviewable = item.previewable;      // TODO: SJ: Fix this in 3.1+
		item.component = matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)) ||
			item.isLevelDescriptor();
		item.isComponent = item.component;
		item.asset = matchesPatterns(item.getUri(), servicesConfig.getAssetPatterns(site));
		item.isAsset = item.asset;
		item.document = matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site));
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

	@Valid
	@LogExecutionTime
	protected ContentItemTO getContentItem(@ValidSiteId String site,
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

	protected ContentItemTO loadContentItem(String site, String path) throws ServiceLayerException {
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
			item.page = matchesPatterns(item.getUri(), servicesConfig.getPagePatterns(site));
			item.isPage = item.page;
			item.previewable = item.page;
			item.isPreviewable = item.previewable;
			item.asset = matchesPatterns(item.getUri(), servicesConfig.getAssetPatterns(site)) ||
				matchesPatterns(item.getUri(), servicesConfig.getRenderingTemplatePatterns(site)) ||
				matchesPatterns(item.getUri(), servicesConfig.getScriptsPatterns(site));
			item.isAsset = item.asset;
			item.component = matchesPatterns(item.getUri(), servicesConfig.getComponentPatterns(site)) ||
				item.isLevelDescriptor() || item.asset;
			item.isComponent = item.component;
			item.document = matchesPatterns(item.getUri(), servicesConfig.getDocumentPatterns(site));
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

	protected void loadContentTypeProperties(String site, ContentItemTO item, String contentTypeId) throws ServiceLayerException {
		// TODO: SJ: Refactor in 2.7.x
		if (item.isFolder()) {
			item.setContentType(CONTENT_TYPE_FOLDER);
		} else {
			// TODO: Use constants instead of string literals
			if (contentTypeId != null && !contentTypeId.equals(CONTENT_TYPE_FOLDER) && !contentTypeId.equals("asset") &&
				!contentTypeId.equals(CONTENT_TYPE_UNKNOWN)) {
				ContentType contentType = contentTypeService.getContentType(site, contentTypeId);
				if (contentType != null) {
					item.setForm(contentType.getId());
					item.setPreviewable(contentType.isPreviewable());
					item.isPreviewable = item.previewable;
				}
			} else {
				String mimeType = StudioUtils.getMimeType(item.getName());
				if (mimeType != null && !isEmpty(mimeType)) {
					item.setPreviewable(matchesPatterns(mimeType, servicesConfig
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

	private ContentItemTO createDummyDmContentItemForDeletedNode(@ValidateStringParam String site,
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

	private String getContentTypeClass(@ValidateStringParam String site, String uri) throws SiteNotFoundException {
		return ContentUtils.getContentTypeClass(servicesConfig, studioConfiguration, site, uri);
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

	public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
		this.studioConfiguration = studioConfiguration;
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

	public void setPublishService(final PublishService publishService) {
		this.publishService = publishService;
	}

	public void setContentTypeService(ContentTypeService contentTypeService) {
		this.contentTypeService = contentTypeService;
	}
}
