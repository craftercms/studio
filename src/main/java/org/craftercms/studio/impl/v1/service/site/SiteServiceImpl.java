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

package org.craftercms.studio.impl.v1.service.site;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.mutable.MutableLong;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.craftercms.commons.entitlements.exception.EntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.plugin.model.PluginDescriptor;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.validation.annotations.param.ValidateNoTagsParam;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.constant.StudioConstants;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.*;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryCredentialsException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteRepositoryException;
import org.craftercms.studio.api.v1.exception.repository.RemoteRepositoryNotFoundException;
import org.craftercms.studio.api.v1.exception.security.GroupAlreadyExistsException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.annotation.RequireSiteExists;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.event.site.SiteReadyEvent;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.upgrade.StudioUpgradeManager;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.api.v2.utils.function.ThrowingRunnable;
import org.craftercms.studio.impl.v2.utils.DateUtils;
import org.craftercms.studio.impl.v2.utils.DependencyUtils;
import org.craftercms.studio.impl.v2.utils.TimeUtils;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.craftercms.studio.model.blobstore.BlobStoreDetails;
import org.craftercms.studio.model.site.SiteDetails;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.lang.NonNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.commons.file.blob.BlobStore.*;
import static org.craftercms.studio.api.v1.constant.DmConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.*;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_INITIALIZING;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;
import static org.craftercms.studio.api.v2.dal.AuditLog.createAuditLogEntry;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.DISABLED;
import static org.craftercms.studio.api.v2.dal.ItemState.NEW;
import static org.craftercms.studio.api.v2.dal.PublishStatus.READY;
import static org.craftercms.studio.api.v2.utils.DalUtils.MY_BATIS_QUERY_BATCH_SIZE;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.*;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.craftercms.studio.impl.v2.utils.PluginUtils.validatePluginParameters;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CREATE_SITE;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_DELETE_SITE;

/**
 * Note: consider renaming
 * A site in Crafter Studio is currently the name for a WEM project being managed.
 * This service provides access to site configuration
 *
 * @author russdanner
 */
public class SiteServiceImpl implements SiteService, ApplicationContextAware {

	private final static Logger logger = LoggerFactory.getLogger(SiteServiceImpl.class);

	protected Deployer deployer;
	protected ContentService contentService;
	protected GitContentRepository contentRepository;
	protected GroupService groupService;
	protected UserService userService;
	protected StudioUpgradeManager upgradeManager;
	protected StudioConfiguration studioConfiguration;
	protected SitesService sitesServiceInternal;
	protected AuditService auditService;
	protected ConfigurationService configurationService;
	protected ItemService itemService;
	protected ApplicationContext applicationContext;

	@Autowired
	protected SiteFeedMapper siteFeedMapper;

	protected EntitlementValidator entitlementValidator;

	protected org.craftercms.studio.api.v2.service.dependency.DependencyService dependencyServiceInternal;
	protected RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;

	protected UserDAO userDao;

	protected SqlSessionFactory sqlSessionFactory;

	@Override
	public Set<String> getAllAvailableSites() {
		List<SiteFeed> sites = siteFeedMapper.getSites();
		Set<String> toRet = new HashSet<>();
		for (SiteFeed site : sites) {
			toRet.add(site.getSiteId());
		}
		return toRet;
	}

	@Override
	public int countSites() {
		return siteFeedMapper.countSites();
	}

	@Override
	@Valid
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_CREATE_SITE)
	public void createSiteFromBlueprint(
		@ValidateStringParam String blueprintId,
		@Size(max = 50) @ValidateStringParam(whitelistedPatterns = "[a-z0-9\\-]*") String siteId,
		@ValidateNoTagsParam String siteName,
		@ValidateStringParam String sandboxBranch,
		@ValidateNoTagsParam String desc,
		Map<String, String> params, boolean createAsOrphan)
		throws ServiceLayerException {
		if (exists(siteId) || existsByName(siteName)) {
			throw new SiteAlreadyExistsException();
		}

		logger.debug("Get the plugin descriptor for the blueprint '{}'", blueprintId);
		PluginDescriptor descriptor = sitesServiceInternal.getBlueprintDescriptor(blueprintId);
		if (Objects.isNull(descriptor)) {
			throw new BlueprintNotFoundException("Blueprint not found " + blueprintId);
		}

		logger.debug("Validate the parameters for blueprint '{}'", blueprintId);
		validatePluginParameters(descriptor.getPlugin(), params);

		String blueprintLocation = sitesServiceInternal.getBlueprintLocation(blueprintId);

		logger.debug("Validate the entitlements for the site '{}'", siteName);
		try {
			entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
		} catch (EntitlementException e) {
			throw new SiteCreationException("Unable to complete the request due to entitlement limits. " +
				"Please contact your system administrator.", e);
		}

		logger.info("Started the site creation process for site '{}' based on the blueprint '{}'",
			siteName, blueprintId);
		boolean success = true;

		// We must fail site creation if any of the site creations steps fail and rollback
		// For example: Create site => create Deployer Target (fail) = fail
		// and rollback the whole thing.
		// What we need to do for site creation and the order of execution:
		// 1) deployer target, 2) git repo, 3) database, 4) kick deployer
		String siteUuid = UUID.randomUUID().toString();

		String creator = SecurityUtils.getCurrentUsername();

		// Create the site in the preview deployer
		logger.info("Create the deployer targets for site '{}'", siteName);
		try {
			deployer.createTargets(siteId);
		} catch (Exception e) {
			logger.error("Failed to create site '{}' ID '{}' based on blueprint '{}'. The deployer targets " +
				"couldn't be created. Rolling back site creation.", siteName, siteId, blueprintId, e);
			throw new DeployerTargetException(format("Failed to create site '%s' ID '%s' based on blueprint '%s'. " +
					"The deployer targets couldn't be created. Rolling back site creation.",
				siteName, siteId, blueprintId), e);
		}

		try {
			logger.info("Initialize site '{}' with blueprint '{}'", siteName, blueprintId);
			success = createSiteFromBlueprintGit(blueprintLocation, siteId, sandboxBranch, params, creator);
			ZonedDateTime now = DateUtils.getCurrentTime();

			logger.debug("Add the site UUID to site '{}'", siteName);
			addSiteUuidFile(siteId, siteUuid);

			logger.debug("Add the site record to the database for site '{}' ID '{}'", siteName, siteId);
			// insert database records
			SiteFeed siteFeed = new SiteFeed();
			siteFeed.setName(siteName);
			siteFeed.setSiteId(siteId);
			siteFeed.setSiteUuid(siteUuid);
			siteFeed.setDescription(desc);
			siteFeed.setPublishingStatus(READY);
			siteFeed.setSandboxBranch(sandboxBranch);
			retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.createSite(siteFeed));

			logger.info("Upgrade the site '{}'", siteName);
			upgradeManager.upgrade(siteId);

			// Add default groups
			logger.debug("Add the default groups to site '{}'", siteName);
			addDefaultGroupsForNewSite();

			logger.debug("Add audit log to site '{}'", siteName);
			insertCreateSiteAuditLog(siteId, siteName, blueprintId, creator);

			TimeUtils.logExecutionTimeThrowing(() ->
					processCreatedFiles(siteId, creator, now),
				logger, format("Method 'SiteServiceImpl.processCreatedFiles(..)' with parameters %s", Arrays.asList(siteId, creator, now)));

			String lastCommitId = contentRepository.getRepoLastCommitId(siteId);
			updateLastCommitId(siteId, lastCommitId);

			logger.info("Reload the site configuration for site '{}'", siteName);
		} catch (Exception e) {
			logger.error("Failed to create site '{}' ID '{}' based on blueprint '{}'. Rolling back site creation.",
				siteName, siteId, blueprintId, e);

			deleteSite(siteId);

			throw new SiteCreationException(format("Failed to create site '%s' ID '%s' based on " +
					"blueprint '%s'. Rolling back site creation.",
				siteName, siteId, blueprintId), e);
		}

		if (!success) {
			throw new SiteCreationException(format("Site creation failed site '%s' ID '%s' based on " +
				"blueprint '%s'", siteName, siteId, blueprintId));
		}
		logger.info("Sync created site content to preview for site '{}'", siteName);
		setSiteState(siteId, STATE_READY);
		configureBlobStores(siteId);

		// Now that everything is created, we can sync the preview deployer with the new content
		try {
			applicationContext.publishEvent(new SiteReadyEvent(siteId, siteUuid));
		} catch (Exception e) {
			setSiteState(siteId, STATE_INITIALIZING);
			logger.warn("Failed to sync site content to preview for site '{}' ID '{}'. While site creation was " +
					"successful, the site won't be preview-able until the Preview Deployer is reachable " +
					"and has successfully synced.",
				siteName, siteId, e);
			throw new SiteCreationException(format("Failed to sync site content to preview for site '%s' " +
					"ID '%s'. While site creation was successful, the site won't be preview-able until " +
					"the Preview Deployer is reachable and has successfully synced.",
				siteName, siteId), e);
		}
		logger.info("Site '{}' ID '{}' created successfully", siteName, siteId);
	}

	/**
	 * When serverless mode is enabled, checks if the site has blob-stores-config.xml file and if not, creates it.
	 *
	 * @param siteId The site ID
	 */
	private void configureBlobStores(String siteId) {
		// TODO: JM: consider moving this kind of operations to a site-create pipeline
		if (!studioConfiguration.getProperty(SERVERLESS_DELIVERY_ENABLED, Boolean.class, false)) {
			logger.info("Serverless delivery is disabled, blob-stores configuration will not be updated for site '{}'", siteId);
			return;
		}

		try {
			String configLocation = studioConfiguration.getProperty(BLOB_STORES_CONFIG_PATH);
			HierarchicalConfiguration<?> xmlConfiguration = configurationService.getXmlConfiguration(siteId, MODULE_STUDIO, configLocation);
			if (xmlConfiguration == null) {
				logger.info("Serverless delivery is enabled, configuring default blob stores for site '{}'", siteId);
				String environment = studioConfiguration.getProperty(CONFIGURATION_ENVIRONMENT_ACTIVE);
				String defaultBlobConfigsLocation = studioConfiguration.getProperty(BLOB_STORES_SERVERLESS_DEFAULT_CONFIG_PATH);
				Resource resource = applicationContext.getResource(defaultBlobConfigsLocation);
				configurationService.writeConfiguration(siteId, MODULE_STUDIO, configLocation, environment, resource.getInputStream());
			}
		} catch (Exception e) {
			logger.error("Failed to configure blob stores for site '{}'", siteId, e);
		}
	}

	private void insertCreateSiteAuditLog(String siteId, String siteName, String blueprint, String creator) throws SiteNotFoundException {
		SiteFeed siteFeed = getSite(studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE));
		AuditLog auditLog = createAuditLogEntry();
		auditLog.setOperation(OPERATION_CREATE);
		auditLog.setSiteId(siteFeed.getId());
		auditLog.setActorId(creator);
		auditLog.setPrimaryTargetId(siteId);
		auditLog.setPrimaryTargetType(TARGET_TYPE_SITE);
		auditLog.setPrimaryTargetValue(siteName);
		List<AuditLogParameter> auditLogParameters = new ArrayList<>();
		AuditLogParameter auditLogParameter = new AuditLogParameter();
		auditLogParameter.setTargetId(siteId + ":" + blueprint);    // TODO: SJ: This feels hokey
		auditLogParameter.setTargetType(TARGET_TYPE_BLUEPRINT);
		auditLogParameter.setTargetValue(blueprint);
		auditLogParameters.add(auditLogParameter);

		auditLog.setParameters(auditLogParameters);
		auditService.insertAuditLog(auditLog);
	}

	private void processCreatedDirectory(ItemDAO itemDao, String siteId, String directory,
					     long userId, ZonedDateTime now) {
		String label = new File(directory).getName();
		Item item = itemService.instantiateItem(siteId, directory)
			.withPreviewUrl(null)
			.withState(NEW.value)
			.withLockedBy(null)
			.withCreatedBy(userId)
			.withCreatedOn(now)
			.withLastModifiedBy(userId)
			.withLastModifiedOn(now)
			.withLastPublishedOn(null)
			.withLabel(label)
			.withContentTypeId(null)
			.withSystemType(CONTENT_TYPE_FOLDER)
			.withMimeType(null)
			.withLocaleCode(Locale.US.toString())
			.withTranslationSourceId(null)
			.withSize(0L)
			.build();
		itemDao.upsertEntry(item);
	}

	private void processCreatedFile(ItemDAO itemDao, DependencyDAO dependencyDao, SqlSession sqlSession,
									Site site, String path, long userId, ZonedDateTime now) throws SiteNotFoundException {
		// Item
		String label = FilenameUtils.getName(path);
		String contentTypeId = EMPTY;
		boolean disabled = false;
		if (StringUtils.endsWith(path, XML_PATTERN)) {
			try {
				Document contentDoc = contentService.getContentAsDocument(site.getSiteId(), path);
				if (contentDoc != null) {
					Element rootElement = contentDoc.getRootElement();
					String internalName = rootElement.valueOf(DOCUMENT_ELM_INTERNAL_TITLE);
					if (isNotEmpty(internalName)) {
						label = internalName;
					}
					contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
					disabled = Boolean.parseBoolean(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
				}
			} catch (DocumentException e) {
				logger.error("Failed to extract metadata from XML file at site '{}' path '{}'",
					site.getSiteId(), path, e);
			}
		}
		String previewUrl = null;
		if (StringUtils.startsWith(path, ROOT_PATTERN_PAGES) ||
			StringUtils.startsWith(path, ROOT_PATTERN_ASSETS)) {
			previewUrl = itemService.getBrowserUrl(site.getSiteId(), path);
		}
		long state = NEW.value;
		if (disabled) {
			state = state | DISABLED.value;
		}

		if (!ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(path))) {
			Item item = itemService.instantiateItem(site.getSiteId(), path)
				.withPreviewUrl(previewUrl)
				.withState(state)
				.withLockedBy(null)
				.withCreatedBy(userId)
				.withCreatedOn(now)
				.withLastModifiedBy(userId)
				.withLastModifiedOn(now)
				.withLastPublishedOn(null)
				.withLabel(label)
				.withContentTypeId(contentTypeId)
				.withSystemType(contentService.getContentTypeClass(site.getSiteId(), path))
				.withMimeType(StudioUtils.getMimeType(FilenameUtils.getName(path)))
				.withLocaleCode(Locale.US.toString())
				.withTranslationSourceId(null)
				.withSize(contentRepository.getContentSize(site.getSiteId(), path))
				.build();
			itemDao.upsertEntry(item);

			DependencyUtils.updateDependencies(site.getSiteId(), path, null, dependencyServiceInternal, dependencyDao,
				sqlSession, false, false);
		}
	}

	/**
	 * Return a Runnable that will check if the counter has exceeded the batch size and if so, execute the queries
	 *
	 * @param sqlSession   sql session instance
	 * @param counter      The counter to check
	 * @return runnable
	 */
	private ThrowingRunnable getCheckCounterFunction(final SqlSession sqlSession, final MutableLong counter, final String siteId) {
		return () -> {
			counter.increment();
			if (counter.longValue() >= MY_BATIS_QUERY_BATCH_SIZE) {
				logger.debug("Executing batch of items for site '{}'", siteId);
				sqlSession.flushStatements();
				logger.debug("Executed batch of items for site '{}'", siteId);
				counter.setValue(0);
			}
		};
	}

	private void processCreatedFiles(String siteId, String creator,
					 ZonedDateTime now) throws Exception {
		logger.debug("Processing created files for site '{}'", siteId);
		Site site = sitesServiceInternal.getSite(siteId);
		User userObj = userService.getUserByGitName(creator);

		MutableLong itemCount = new MutableLong(0);
		try (SqlSession sqlSession = sqlSessionFactory.openSession(ExecutorType.BATCH)) {
			ItemDAO itemDao = sqlSession.getMapper(ItemDAO.class);
			DependencyDAO dependencyDao = sqlSession.getMapper(DependencyDAO.class);
			ThrowingRunnable checkCounter = getCheckCounterFunction(sqlSession, itemCount, siteId);
			contentRepository.forAllSitePaths(siteId,
				directory -> {
					processCreatedDirectory(itemDao, site.getSiteId(), directory, userObj.getId(), now);
					checkCounter.run();
				},
				file -> {
					processCreatedFile(itemDao, dependencyDao, sqlSession, site, file, userObj.getId(), now);
					checkCounter.run();
				}
			);
			sqlSession.commit();
			logger.debug("Update parent ID for created items for site '{}'", siteId);
			itemService.updateParentId(siteId);
			logger.debug("Validate dependencies for site '{}'", siteId);
			dependencyServiceInternal.validateDependencies(siteId);
		} catch (Exception e) {
			logger.error("Failed to update database for processingCreatedFiles in site '{}'", siteId, e);
			throw e;
		}
		logger.debug("Finished processing created files for site '{}'", siteId);
	}

	protected boolean createSiteFromBlueprintGit(String blueprintLocation, String siteId, String sandboxBranch,
						     Map<String, String> params, String creator)
		throws Exception {
		boolean success;

		// create site with git repo
		success = contentRepository.createSiteFromBlueprint(blueprintLocation, siteId, sandboxBranch, params, creator);

		String siteConfigFolder = FILE_SEPARATOR + "config" + FILE_SEPARATOR + "studio";
		replaceFileContentGit(siteId, siteConfigFolder + FILE_SEPARATOR + "site-config.xml", siteId);

		return success;
	}

	protected void replaceFileContentGit(String site, String path, String replace) throws Exception {
		InputStream content = contentRepository.getContent(site, path);
		String contentAsString = IOUtils.toString(content, UTF_8);

		contentAsString = contentAsString.replaceAll("SITENAME", replace);

		InputStream contentToWrite = IOUtils.toInputStream(contentAsString, UTF_8);

		contentRepository.writeContent(site, path, contentToWrite);
	}

	private void addDefaultGroupsForNewSite() {
		List<String> defaultGroups = getDefaultGroups();
		for (String group : defaultGroups) {
			String description = group + SITE_DEFAULT_GROUPS_DESCRIPTION;
			try {
				if (!groupService.groupExists(-1, group)) {
					try {
						groupService.createGroup(DEFAULT_ORGANIZATION_ID, group, description, false);
					} catch (GroupAlreadyExistsException e) {
						throw new IllegalStateException(e);
					}
				} else {
					logger.info("Default group '{}' was not created since it already exists", group);
				}
			} catch (ServiceLayerException e) {
				logger.error("Failed to create group '{}'", group, e);
			}
		}
	}

	@Override
	@Valid
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_CREATE_SITE)
	public void createSiteWithRemoteOption(
		@Size(max = 50) @ValidateStringParam(whitelistedPatterns = "[a-z0-9\\-]*") String siteId,
		@ValidateStringParam String siteName,
		@ValidateStringParam String sandboxBranch,
		@ValidateNoTagsParam String description,
		String blueprintName,
		@ValidateStringParam String remoteName,
		@ValidateStringParam String remoteUrl,
		String remoteBranch, boolean singleBranch, String authenticationType,
		String remoteUsername, String remotePassword, String remoteToken,
		String remotePrivateKey,
		@ValidateStringParam String createOption,
		Map<String, String> params, boolean createAsOrphan)
		throws ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
		RemoteRepositoryNotFoundException {
		if (exists(siteId) || existsByName(siteName)) {
			throw new SiteAlreadyExistsException();
		}

		logger.debug("Validate site entitlements for site '{}'", siteId);
		try {
			entitlementValidator.validateEntitlement(EntitlementType.SITE, 1);
		} catch (EntitlementException e) {
			throw new SiteCreationException("Unable to complete request due to entitlement limits. Please contact your "
				+ "system administrator.", e);
		}

		if (REMOTE_REPOSITORY_CREATE_OPTION_CLONE.equals(createOption)) {
			logger.info("Clone site from remote repository for site '{}', " +
					"remoteUrl '{}', remote branch '{}'",
				siteId, remoteUrl, remoteBranch);
			createSiteCloneRemote(siteId, siteName, sandboxBranch, description, remoteName, remoteUrl, remoteBranch,
				singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken, remotePrivateKey,
				params, createAsOrphan);
		} else {
			logger.error("Invalid site creation option '{}' for site '{}'", createOption, siteId);
			throw new SiteCreationException(format("Invalid site creation option '%s' for site '%s'",
				createOption, siteId));
		}
	}

	@SuppressWarnings("deprecation")
	private void createSiteCloneRemote(String siteId, String siteName, String sandboxBranch, String description,
					   String remoteName, String remoteUrl, String remoteBranch, boolean singleBranch,
					   String authenticationType, String remoteUsername, String remotePassword,
					   String remoteToken, String remotePrivateKey, Map<String, String> params,
					   boolean createAsOrphan)
		throws ServiceLayerException, InvalidRemoteRepositoryException, InvalidRemoteRepositoryCredentialsException,
		RemoteRepositoryNotFoundException {
		boolean success;

		// We must fail site creation if any of the site creations steps fail and rollback
		// For example: Create site => create Deployer Target (fail) = fail
		// and rollback the whole thing.
		// What we need to do for site creation and the order of execution:
		// 1) git repo, 2) deployer target, 3) database, 4) kick deployer
		String siteUuid = UUID.randomUUID().toString();

		String creator = SecurityUtils.getCurrentUsername();

		try {
			// create site by cloning remote git repo
			logger.info("Create site '{}' by cloning the remote '{}' url '{}' branch '{}'",
				siteId, remoteName, remoteUrl, remoteBranch);
			success = contentRepository.createSiteCloneRemote(siteId, sandboxBranch, remoteName, remoteUrl,
				remoteBranch, singleBranch, authenticationType, remoteUsername, remotePassword, remoteToken,
				remotePrivateKey, params, createAsOrphan, creator);

		} catch (InvalidRemoteRepositoryException | InvalidRemoteRepositoryCredentialsException |
			 RemoteRepositoryNotFoundException | ServiceLayerException e) {

			contentRepository.deleteSite(siteId);

			logger.error("Failed to create site '{}' by cloning '{}' url '{}' branch '{}'. Rolling back.",
				siteId, remoteName, remoteUrl, remoteBranch, e);

			throw e;
		}

		if (!success) {
//			contentRepository.removeRemoteRepositoriesForSite(siteId);
			contentRepository.deleteSite(siteId);
			throw new ServiceLayerException("Failed to create site: " + siteId + " ID: " + siteId + " as clone from " +
				"remote repository: " + remoteName + " (" + remoteUrl + ")");
		}

		// Create the site in the preview deployer
		try {
			logger.info("Create Deployer targets for site '{}'", siteId);
			deployer.createTargets(siteId);
		} catch (Exception e) {
			logger.error("Failed to create Deployer targets for site '{}' as a clone of '{}' url '{}' " +
				"branch '{}'. Site creation failed.", siteId, remoteName, remoteUrl, remoteBranch, e);

			contentRepository.removeRemote(siteId, remoteName);
			boolean deleted = contentRepository.deleteSite(siteId);

			if (!deleted) {
				logger.error("Failed to rollback site creation for site '{}'.", siteId);
			}

			throw new DeployerTargetException("Error while creating site: " + siteId + " ID: " + siteId +
				" as clone from remote repository: " + remoteName +
				" (" + remoteUrl + "). The required Deployer targets couldn't " +
				"be created", e);
		}

		ZonedDateTime now = DateUtils.getCurrentTime();
		try {
			logger.debug("Add site UUID to site '{}'", siteId);
			addSiteUuidFile(siteId, siteUuid);

			// insert database records
			logger.debug("Add site record to the database for site '{}'", siteId);
			SiteFeed siteFeed = new SiteFeed();
			siteFeed.setName(siteName);
			siteFeed.setSiteId(siteId);
			siteFeed.setSiteUuid(siteUuid);
			siteFeed.setDescription(description);
			siteFeed.setPublishingStatus(READY);
			siteFeed.setSandboxBranch(sandboxBranch);
			retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.createSite(siteFeed));

			upgradeManager.upgrade(siteId);


			// Add default groups
			logger.info("Add default groups to site '{}'", siteId);
			addDefaultGroupsForNewSite();


			insertCreateSiteAuditLog(siteId, siteId, remoteName + "/" + remoteBranch, creator);

			TimeUtils.logExecutionTimeThrowing(() ->
					processCreatedFiles(siteId, creator, now),
				logger, format("Method 'SiteServiceImpl.processCreatedFiles(..)' with parameters %s", Arrays.asList(siteId, creator, now)));

			String lastCommitId = contentRepository.getRepoLastCommitId(siteId);
			updateLastCommitId(siteId, lastCommitId);

			logger.info("Load the configuration for site '{}'", siteId);
		} catch (Exception e) {
			logger.error("Failed to create site '{}' by cloning '{}' url '{}' branch '{}'",
				siteId, remoteName, remoteUrl, remoteBranch, e);

			deleteSite(siteId);

			throw new SiteCreationException("Error while creating site: " + siteId + " ID: " + siteId +
				" as clone from remote repository: " + remoteName + " (" + remoteUrl + "). Rolling back.", e);
		}

		// Now that everything is created, we can sync the preview deployer with the new content
		logger.info("Sync site '{}' to preview", siteId);
		setSiteState(siteId, STATE_READY);
		configureBlobStores(siteId);
		try {
			applicationContext.publishEvent(new SiteReadyEvent(siteId, siteUuid));
		} catch (Exception e) {
			setSiteState(siteId, STATE_INITIALIZING);
			// TODO: SJ: This seems to leave the site in a bad state, review
			logger.error("Failed to sync site '{}' to preview. The site will become previewable once " +
				"the preview deployer is reachable.", siteId, e);

			throw new SiteCreationException(format("Failed to sync site '%s' to preview. The site will become " +
				"previewable once the preview deployer is reachable.", siteId), e);
		}
		logger.info("Site '{}' created successfully", siteId);
	}

	@Override
	@HasPermission(type = DefaultPermission.class, action = PERMISSION_DELETE_SITE)
	public boolean deleteSite(String siteId) {
		try {
			sitesServiceInternal.deleteSite(siteId);
			return true;
		} catch (Exception e) {
			logger.error("Failed to delete site '{}'", siteId, e);
			return false;
		}
	}

	@Override
	@Valid
	public void updateLastCommitId(@ValidateStringParam String site,
				       @ValidateStringParam String commitId) {
		Map<String, Object> params = new HashMap<>();
		params.put("siteId", site);
		params.put("lastCommitId", commitId);
		retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.updateLastCommitId(params));
	}

	@Override
	@Valid
	public boolean exists(@ValidateStringParam String site) {
		return siteFeedMapper.exists(site) > 0;
	}

	@Override
	@Valid
	public void checkSiteExists(@ValidateStringParam final String site) throws SiteNotFoundException {
		if (!exists(site)) {
			throw new SiteNotFoundException(format("Site '%s' not found.", site));
		}
	}

	@Override
	@Valid
	public boolean existsById(@ValidateStringParam String siteId) {
		return siteFeedMapper.existsById(siteId) > 0;
	}

	@Override
	@Valid
	public boolean existsByName(@ValidateStringParam String siteName) {
		return siteFeedMapper.existsByName(siteName) > 0;
	}

	@Override
	@Valid
	public int getSitesPerUserTotal()
		throws UserNotFoundException, ServiceLayerException {
		return getSitesPerUserTotal(SecurityUtils.getCurrentUsername());
	}

	@Override
	@Valid
	public int getSitesPerUserTotal(@ValidateStringParam String username)
		throws UserNotFoundException, ServiceLayerException {
		if (userService.userExists(username)) {
			Map<String, Object> params = new HashMap<>();
			params.put("username", username);
			return siteFeedMapper.getSitesPerUserQueryTotal(params);
		} else {
			throw new UserNotFoundException();
		}
	}

	@Override
	@Valid
	public List<SiteFeed> getSitesPerUser(int start,
					      int number)
		throws UserNotFoundException, ServiceLayerException {
		return getSitesPerUser(SecurityUtils.getCurrentUsername(), start, number);
	}

	@Override
	@Valid
	public List<SiteFeed> getSitesPerUser(@ValidateStringParam String username,
					      int start,
					      int number)
		throws UserNotFoundException, ServiceLayerException {
		if (userService.userExists(username)) {
			Map<String, Object> params = new HashMap<>();
			params.put("username", username);
			params.put("start", start);
			params.put("number", number);
			List<String> siteIds = siteFeedMapper.getSitesPerUserQuery(params);
			List<SiteFeed> toRet = new ArrayList<>();
			if (siteIds != null && !siteIds.isEmpty()) {
				params = new HashMap<>();
				params.put("siteids", siteIds);
				toRet = siteFeedMapper.getSitesPerUserData(params);
			}
			return toRet;
		} else {
			throw new UserNotFoundException();
		}
	}

	@Override
	@Valid
	public SiteFeed getSite(@ValidateStringParam String siteId) throws SiteNotFoundException {
		if (exists(siteId)) {
			Map<String, Object> params = new HashMap<>();
			params.put("siteId", siteId);
			return siteFeedMapper.getSite(params);
		} else {
			throw new SiteNotFoundException();
		}
	}

	@Override
	@RequireSiteExists
	public SiteDetails getSiteDetails(@SiteId String siteId) throws ServiceLayerException {
		Map<String, Object> params = new HashMap<>();
		params.put("siteId", siteId);

		List<BlobStoreDetails> storeDetails = Collections.emptyList();
		if (!studioConfiguration.getProperty(SERVERLESS_DELIVERY_ENABLED, Boolean.class, false)) {
			String configLocation = studioConfiguration.getProperty(BLOB_STORES_CONFIG_PATH);
			HierarchicalConfiguration<?> xmlConfiguration = configurationService.getXmlConfiguration(siteId, MODULE_STUDIO, configLocation);
			storeDetails = getBlobStoreDetails(xmlConfiguration);
		}
		return new SiteDetails(siteFeedMapper.getSite(params), storeDetails);
	}

	/**
	 * Reads blob-stores-config.xml and returns a list of BlobStoreDetails
	 *
	 * @param xmlConfiguration the blob-stores-config.xml configuration
	 * @return a list of BlobStoreDetails
	 */
	@NotNull
	private static List<BlobStoreDetails> getBlobStoreDetails(HierarchicalConfiguration<?> xmlConfiguration) {
		if (xmlConfiguration == null) {
			return Collections.emptyList();
		}

		List<? extends HierarchicalConfiguration<?>> blobStores = xmlConfiguration.configurationsAt(CONFIG_KEY_STORE);
		return blobStores.stream().map(store -> {
			String id = store.getString(CONFIG_KEY_ID);
			String type = store.getString(CONFIG_KEY_TYPE);
			String pattern = store.getString(CONFIG_KEY_PATTERN);
			boolean readOnly = store.getBoolean(CONFIG_KEY_READ_ONLY, false);

			List<BlobStoreDetails.Mapping> mappings = store.configurationsAt(CONFIG_KEY_MAPPING).stream()
				.map(mapping -> {
					String publishingTarget = mapping.getString(CONFIG_KEY_MAPPING_PUBLISHING_TARGET);
					String storeTarget = mapping.getString(CONFIG_KEY_MAPPING_STORE_TARGET);
					String prefix = mapping.getString(CONFIG_KEY_MAPPING_PREFIX);
					return new BlobStoreDetails.Mapping(publishingTarget, storeTarget, prefix);
				}).collect(Collectors.toList());

			BlobStoreDetails details = new BlobStoreDetails();
			details.setId(id);
			details.setPattern(pattern);
			details.setType(type);
			details.setReadOnly(readOnly);
			details.setMappings(mappings);

			return details;
		}).collect(Collectors.toList());
	}

	@Override
	public List<SiteFeed> getDeletedSites() {
		return siteFeedMapper.getDeletedSites();
	}

	private void addSiteUuidFile(String site, String siteUuid) throws IOException {
		Path path = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
			studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
			StudioConstants.SITE_UUID_FILENAME);
		String toWrite = StudioConstants.SITE_UUID_FILE_COMMENT + "\n" + siteUuid;
		Files.write(path, toWrite.getBytes());
	}

	@Override
	public List<String> getAllCreatedSites() {
		return siteFeedMapper.getAllCreatedSites(STATE_READY);
	}

	@Override
	public void setSiteState(String siteId, String state) {
		retryingDatabaseOperationFacade.retry(() -> siteFeedMapper.setSiteState(siteId, state));
	}

	@Override
	public String getSiteState(String siteId) {
		return siteFeedMapper.getSiteState(siteId);
	}

	public List<String> getDefaultGroups() {
		return Arrays.asList(studioConfiguration.getProperty(CONFIGURATION_DEFAULT_GROUPS).split(","));
	}

	@Override
	public void setApplicationContext(@NonNull ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setContentRepository(GitContentRepository repo) {
		contentRepository = repo;
	}

	public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
		this.studioConfiguration = studioConfiguration;
	}

	public void setDeployer(Deployer deployer) {
		this.deployer = deployer;
	}

	@SuppressWarnings("unused")
	public void setEntitlementValidator(final EntitlementValidator entitlementValidator) {
		this.entitlementValidator = entitlementValidator;
	}

	@SuppressWarnings("unused")
	public void setGroupService(GroupService groupService) {
		this.groupService = groupService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	@SuppressWarnings("unused")
	public void setUpgradeManager(final StudioUpgradeManager upgradeManager) {
		this.upgradeManager = upgradeManager;
	}

	@SuppressWarnings("unused")
	public void setSitesServiceInternal(SitesService sitesServiceInternal) {
		this.sitesServiceInternal = sitesServiceInternal;
	}

	public void setAuditService(AuditService auditService) {
		this.auditService = auditService;
	}

	public void setConfigurationService(ConfigurationService configurationService) {
		this.configurationService = configurationService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}

	@SuppressWarnings("unused")
	public void setDependencyServiceInternal(org.craftercms.studio.api.v2.service.dependency.DependencyService dependencyServiceInternal) {
		this.dependencyServiceInternal = dependencyServiceInternal;
	}

	public void setRetryingDatabaseOperationFacade(RetryingDatabaseOperationFacade retryingDatabaseOperationFacade) {
		this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
	}

	@SuppressWarnings("unused")
	public void setUserDao(UserDAO userDao) {
		this.userDao = userDao;
	}

	@SuppressWarnings("unused")
	public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
		this.sqlSessionFactory = sqlSessionFactory;
	}
}
