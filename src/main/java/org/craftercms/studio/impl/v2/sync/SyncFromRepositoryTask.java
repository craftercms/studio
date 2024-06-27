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

package org.craftercms.studio.impl.v2.sync;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.lang.RegexUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.dal.*;
import org.craftercms.studio.api.v2.event.repository.RepositoryEvent;
import org.craftercms.studio.api.v2.event.site.SyncFromRepoEvent;
import org.craftercms.studio.api.v2.repository.ContentRepository;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v2.utils.DependencyUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.*;
import static org.craftercms.studio.api.v1.constant.DmConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.*;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.*;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_PATH_PATTERNS;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getStudioTemporaryFilesRoot;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;

/**
 * Listens to {@link SyncFromRepoEvent} events and performs the sync from repository.
 */
public class SyncFromRepositoryTask implements ApplicationEventPublisherAware {

    private static final Logger logger = LoggerFactory.getLogger(SyncFromRepositoryTask.class);
    private final static String REPO_OPERATIONS_SCRIPT_PREFIX = "repoOperations_";
    private final static String UPDATE_PARENT_ID_SCRIPT_PREFIX = "updateParentId_";

    protected StudioDBScriptRunnerFactory studioDBScriptRunnerFactory;

    private final SitesService sitesService;
    private final GeneralLockService generalLockService;
    private final AuditServiceInternal auditServiceInternal;
    private final DependencyServiceInternal dependencyServiceInternal;
    private final UserServiceInternal userServiceInternal;
    private final ItemServiceInternal itemServiceInternal;
    private final ContentService contentService;
    private final ConfigurationService configurationService;
    private final ContentRepository contentRepository;
    private final StudioConfiguration studioConfiguration;
    private ApplicationEventPublisher eventPublisher;

    @ConstructorProperties({"sitesService", "generalLockService",
            "auditServiceInternal",
            "studioDBScriptRunnerFactory", "dependencyServiceInternal",
            "userServiceInternal", "itemServiceInternal",
            "contentService", "configurationService",
            "contentRepository", "studioConfiguration"})
    public SyncFromRepositoryTask(SitesService sitesService, GeneralLockService generalLockService,
                                  AuditServiceInternal auditServiceInternal,
                                  StudioDBScriptRunnerFactory studioDBScriptRunnerFactory, DependencyServiceInternal dependencyServiceInternal,
                                  UserServiceInternal userServiceInternal, ItemServiceInternal itemServiceInternal,
                                  ContentService contentService, ConfigurationService configurationService,
                                  ContentRepository contentRepository, StudioConfiguration studioConfiguration) {
        this.sitesService = sitesService;
        this.generalLockService = generalLockService;
        this.auditServiceInternal = auditServiceInternal;
        this.studioDBScriptRunnerFactory = studioDBScriptRunnerFactory;
        this.dependencyServiceInternal = dependencyServiceInternal;
        this.userServiceInternal = userServiceInternal;
        this.itemServiceInternal = itemServiceInternal;
        this.contentService = contentService;
        this.configurationService = configurationService;
        this.contentRepository = contentRepository;
        this.studioConfiguration = studioConfiguration;
    }

    @Async
    @EventListener
    public void syncRepoListener(SyncFromRepoEvent event) throws ServiceLayerException {
        syncRepository(event.getSiteId());
    }

    /**
     * Sync the database with the repository in the given site.
     *
     * @param siteId The site ID.
     * @throws ServiceLayerException If an error occurs while syncing the database with the repository.
     */
    private void syncRepository(final String siteId) throws ServiceLayerException {
        logger.debug("Sync the database with the repository in site '{}'", siteId);

        Site site = sitesService.getSite(siteId);
        if (!sitesService.checkSiteUuid(siteId, site.getSiteUuid())) {
            logger.warn("Site '{}' has a different UUID than the one in the database. " +
                    "The site will not be synced with the repository.", siteId);
            return;
        }
        String syncFromRepoLockKey = StudioUtils.getSyncFromRepoLockKey(siteId);
        generalLockService.lock(syncFromRepoLockKey);
        try {
            // Get the last commit to be used along the sync process (instead of 'HEAD',
            // commits added after this point will be processed in subsequent executions of this method)
            final String lastCommitInRepo = contentRepository.getRepoLastCommitId(siteId);
            final String lastProcessedCommit = sitesService.getLastCommitId(siteId);
            if (StringUtils.equals(lastCommitInRepo, lastProcessedCommit)) {
                logger.debug("Site '{}' is already synced with the repository up to commit '{}'", siteId, lastCommitInRepo);
                return;
            }
            // Some of these (the ones created by Studio APIs) will already be in the audit table
            List<String> unprocessedCommits = contentRepository.getCommitIdsBetween(siteId, lastProcessedCommit, lastCommitInRepo);

            String currentLastProcessedCommit = lastProcessedCommit;
            String lastUnprocessedCommit = null;
            // This loop will iterate throw commits and find commit sequences that are not audited yet
            for (String commitId : unprocessedCommits) {
                if (auditServiceInternal.isAudited(site.getId(), commitId)) {
                    // If commit is already audited, ingest the changes in between, if any
                    if (lastUnprocessedCommit != null) {
                        ingestChanges(site, currentLastProcessedCommit, lastUnprocessedCommit);
                        lastUnprocessedCommit = null;
                    }
                    // Move site.commitId to the current commit
                    updateLastCommitId(siteId, commitId);
                    currentLastProcessedCommit = commitId;
                } else {
                    // Continue until we find a commit that is already in the audit table
                    lastUnprocessedCommit = commitId;
                }
            }
            if (lastUnprocessedCommit != null) {
                ingestChanges(site, currentLastProcessedCommit, lastUnprocessedCommit);
                updateLastCommitId(siteId, lastUnprocessedCommit);
            }
            logger.debug("Site '{}' is now synced with the repository up to commit '{}'", siteId, lastCommitInRepo);
        } catch (UserNotFoundException | GitAPIException | IOException e) {
            throw new ServiceLayerException(format("Failed to sync repository for site '%s'", siteId), e);
        } finally {
            generalLockService.unlock(syncFromRepoLockKey);
        }
    }

    private void updateLastCommitId(String siteId, String commitId) {
        sitesService.updateLastCommitId(siteId, commitId);
    }

    /**
     * Extracts the operations between the given commits and updates the database accordingly.
     *
     * @param site       the site to be updated.
     * @param commitFrom the commit to start from.
     * @param commitTo   the commit to end with.
     * @throws IOException           if an error occurs while reading the repository.
     * @throws GitAPIException       if an error occurs while reading the repository.
     * @throws UserNotFoundException if a user cannot be found for any of the operations.
     * @throws ServiceLayerException if an error occurs while updating the database.
     */
    private void ingestChanges(final Site site, final String commitFrom, final String commitTo) throws IOException, GitAPIException, UserNotFoundException, ServiceLayerException {
        List<RepoOperation> operationsFromDelta = contentRepository.getOperationsFromDelta(site.getSiteId(), commitFrom, commitTo);
        syncDatabaseWithRepo(site, operationsFromDelta.stream().sorted(comparing(RepoOperation::getAction)).toList());
        auditChangesFromGit(site, commitFrom, commitTo);

        // Sync all preview deployers
        try {
            logger.debug("Sync preview for site '{}'", site);
            eventPublisher.publishEvent(new RepositoryEvent(site.getSiteId()));
        } catch (Exception e) {
            logger.error("Failed to sync preview for site '{}'", site, e);
        }
    }

    /**
     * Creates an audit log entry indicating the sync of git history up from commitFrom up to commitTo <br/>
     * It will add as audit log parameters any commit between commitFrom and commitTo.
     *
     * @param site       The site being synced
     * @param commitFrom The last previously synced commit id
     * @param commitTo   The new synced commit id
     */
    private void auditChangesFromGit(final Site site, final String commitFrom, final String commitTo) throws GitAPIException, IOException {
        AuditLog auditLogEntry = auditServiceInternal.createAuditLogEntry();
        auditLogEntry.setSiteId(site.getId());
        auditLogEntry.setOperation(OPERATION_GIT_CHANGES);
        auditLogEntry.setOrigin(ORIGIN_GIT);
        auditLogEntry.setCommitId(commitTo);
        auditLogEntry.setActorId(ACTOR_ID_GIT);
        auditLogEntry.setActorDetails(ACTOR_ID_GIT);
        auditLogEntry.setPrimaryTargetId(site.getSiteId());
        auditLogEntry.setPrimaryTargetType(TARGET_TYPE_SITE);
        auditLogEntry.setPrimaryTargetValue(site.getName());

        try {
            List<String> commitIds = contentRepository.getIntroducedCommits(site.getSiteId(), commitFrom, commitTo);
            List<AuditLogParameter> auditParameters = new ArrayList<>();
            for (String commitId : commitIds) {
                AuditLogParameter auditParameter = new AuditLogParameter();
                auditParameter.setTargetId(commitId);
                auditParameter.setTargetType(TARGET_TYPE_SYNCED_COMMIT);
                auditParameter.setTargetValue(commitId);
                auditParameters.add(auditParameter);
            }
            auditLogEntry.setParameters(auditParameters);
        } catch (IOException | GitAPIException e) {
            logger.error("Failed to calculate introduced commits for site '{}' from commit '{}' to commit '{}'",
                    site.getSiteId(), commitFrom, commitTo, e);
            throw e;
        }

        auditServiceInternal.insertAuditLog(auditLogEntry);
    }

    /**
     * Syncs the database with the repository by applying the given repo operations
     *
     * @param site                The site being synced
     * @param repoOperationsDelta The repo operations to apply
     */
    private void syncDatabaseWithRepo(Site site, List<RepoOperation> repoOperationsDelta) throws IOException, UserNotFoundException, ServiceLayerException {
        StudioDBScriptRunner studioDBScriptRunner = studioDBScriptRunnerFactory.getDBScriptRunner();
        Path repoOperationsScriptPath = null;
        Path updateParentIdScriptPath = null;
        try {
            Path studioTempDir = getStudioTemporaryFilesRoot();
            String repoOperationsScriptFilename = REPO_OPERATIONS_SCRIPT_PREFIX + UUID.randomUUID();
            repoOperationsScriptPath = Files.createTempFile(studioTempDir, repoOperationsScriptFilename, SQL_SCRIPT_SUFFIX);
            String updateParentIdScriptFilename = UPDATE_PARENT_ID_SCRIPT_PREFIX + UUID.randomUUID();
            updateParentIdScriptPath = Files.createTempFile(studioTempDir, updateParentIdScriptFilename, SQL_SCRIPT_SUFFIX);
            processRepoOperations(site, repoOperationsDelta, repoOperationsScriptPath,
                    updateParentIdScriptPath);
            studioDBScriptRunner.execute(repoOperationsScriptPath.toFile());
            studioDBScriptRunner.execute(updateParentIdScriptPath.toFile());
        } catch (IOException e) {
            logger.error("Failed to create the database script for processing the created files in site '{}'", site);
            throw e;
        } finally {
            if (repoOperationsScriptPath != null) {
                logger.debug("Deleting temporary file '{}'", repoOperationsScriptPath);
                FileUtils.deleteQuietly(repoOperationsScriptPath.toFile());
            }
            if (updateParentIdScriptPath != null) {
                logger.debug("Deleting temporary file '{}'", updateParentIdScriptPath);
                FileUtils.deleteQuietly(updateParentIdScriptPath.toFile());
            }
        }
    }

    /**
     * This method will try to get a User object for the given operation author. If the user is not found, it will
     * return the {@value org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants#GIT_REPO_USER_USERNAME}
     * fallback user.
     *
     * @param operationAuthor The username of the operation author
     * @param cachedUsers     A map of already retrieved users, to avoid querying the database multiple times for the same values
     * @return The User object for the given operation author, or the fallback user if the author is not found
     * @throws UserNotFoundException if neither the author nor the fallback user are found
     * @throws ServiceLayerException if an error occurs while trying to retrieve the user
     */
    private User getRepoOperationUser(String operationAuthor, Map<String, User> cachedUsers)
            throws UserNotFoundException, ServiceLayerException {
        User result = cachedUsers.computeIfAbsent(operationAuthor, key -> {
            try {
                return userServiceInternal.getUserByIdOrUsername(-1, key);
            } catch (UserNotFoundException | ServiceLayerException e) {
                logger.debug("User '{}' not found while syncing operations from repository",
                        key, e);
                return null;
            }
        });
        if (result == null) {
            // Map the absent username to fallback, so we don't query the database again
            try {
                result = userServiceInternal.getUserByIdOrUsername(-1, GIT_REPO_USER_USERNAME);
                cachedUsers.put(operationAuthor, result);
            } catch (UserNotFoundException e) {
                logger.error("User '{}' not found while syncing operations from repository", GIT_REPO_USER_USERNAME, e);
                throw e;
            }
        }
        return result;
    }

    /**
     * Processes the given repo operations and generates the database script to apply them
     *
     * @param site                     The site being synced
     * @param repoOperations           The repo operations to apply
     * @param repoOperationsScriptPath The path to the generated database script
     * @param updateParentIdScriptPath The path to the generated database script
     * @throws IOException if an error occurs while generating the database script
     */
    private void processRepoOperations(Site site, List<RepoOperation> repoOperations,
                                       Path repoOperationsScriptPath, Path updateParentIdScriptPath) throws IOException, UserNotFoundException, ServiceLayerException {
        Map<String, User> cachedUsers = new HashMap<>();
        for (RepoOperation repoOperation : repoOperations) {
            User user = getRepoOperationUser(repoOperation.getAuthor(), cachedUsers);
            switch (repoOperation.getAction()) {
                case CREATE, COPY ->
                        processCreate(site, repoOperation, user, repoOperationsScriptPath, updateParentIdScriptPath);
                case UPDATE -> processUpdate(site, repoOperation, user, repoOperationsScriptPath);
                case DELETE -> processDelete(site, repoOperation, repoOperationsScriptPath);
                case MOVE -> processMove(site, repoOperation, user, repoOperationsScriptPath, updateParentIdScriptPath);
                default -> logger.error("Failed to process unknown repo operation '{}' in site '{}'",
                        site.getSiteId(), repoOperation.getAction());
            }
            invalidateConfigurationCacheIfRequired(site.getSiteId(), repoOperation.getPath());
        }
    }

    /**
     * Gets the item metadata for the given site and path when the item is an XML file.
     * When the file is not an XML, metadata is not extracted from the item file
     * and result will contain default values.
     *
     * @param siteId The site id
     * @param path   The path to the item
     * @return The item metadata
     */
    private ItemMetadata getItemMetadata(String siteId, String path) {
        ItemMetadata result = new ItemMetadata(path);
        if (startsWith(path, ROOT_PATTERN_PAGES) ||
                startsWith(path, ROOT_PATTERN_ASSETS)) {
            result.previewUrl = itemServiceInternal.getBrowserUrl(siteId, path);
        }
        if (!endsWith(path, XML_PATTERN)) {
            return result;
        }
        try {
            Document contentDoc = contentService.getContentAsDocument(siteId, path);
            if (contentDoc != null) {
                Element rootElement = contentDoc.getRootElement();
                String internalName = rootElement.valueOf(DOCUMENT_ELM_INTERNAL_TITLE);
                if (isNotEmpty(internalName)) {
                    result.label = internalName;
                }
                result.contentTypeId = rootElement.valueOf(DOCUMENT_ELM_CONTENT_TYPE);
                result.disabled = Boolean.parseBoolean(rootElement.valueOf(DOCUMENT_ELM_DISABLED));
            }
        } catch (DocumentException e) {
            logger.error("Failed to extract metadata from the XML site '{}' path '{}'",
                    siteId, path, e);
        }
        return result;
    }

    private void processCreate(Site site, RepoOperation repoOperation, User user,
                               Path repoOperationsScriptPath, Path updateParentIdScriptPath) throws IOException, ServiceLayerException {

        ItemMetadata metadata = getItemMetadata(site.getSiteId(), repoOperation.getPath());
        processAncestors(site.getId(), repoOperation.getPath(), user.getId(),
                repoOperation.getDateTime(), repoOperationsScriptPath);
        long state = NEW.value;
        if (metadata.disabled) {
            state = state | DISABLED.value;
        }

        if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath()))) {
            addUpdateParentIdScriptSnippets(site.getId(), repoOperation.getPath(),
                    updateParentIdScriptPath);
        } else {
            Files.write(repoOperationsScriptPath, insertItemRow(site.getId(),
                    repoOperation.getPath(), metadata.previewUrl, state, null, user.getId(),
                    repoOperation.getDateTime(), user.getId(), repoOperation.getDateTime(),
                    null, metadata.label, metadata.contentTypeId,
                    contentService.getContentTypeClass(site.getSiteId(), repoOperation.getPath()),
                    StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                    Locale.US.toString(), null,
                    contentRepository.getContentSize(site.getSiteId(), repoOperation.getPath()), null,
                    null).getBytes(UTF_8), StandardOpenOption.APPEND);
            Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
            logger.debug("Extract dependencies from site '{}' path '{}'",
                    site.getSiteId(), repoOperation.getPath());
            addUpdateParentIdScriptSnippets(site.getId(), repoOperation.getPath(),
                    updateParentIdScriptPath);
            DependencyUtils.addDependenciesScriptSnippets(site.getSiteId(), repoOperation.getPath(), null,
                    repoOperationsScriptPath, dependencyServiceInternal);
        }
    }

    private void processUpdate(Site site, RepoOperation repoOperation, User user,
                               Path repoOperationsScriptPath) throws IOException, ServiceLayerException {
        if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath()))) {
            return;
        }
        ItemMetadata metadata = getItemMetadata(site.getSiteId(), repoOperation.getPath());
        long onStateBitMap = SAVE_AND_CLOSE_ON_MASK;
        long offStateBitmap = SAVE_AND_CLOSE_OFF_MASK;
        if (metadata.disabled) {
            onStateBitMap = onStateBitMap | DISABLED.value;
        } else {
            offStateBitmap = offStateBitmap | DISABLED.value;
        }

        Files.write(repoOperationsScriptPath, updateItemRow(site.getId(),
                repoOperation.getPath(), metadata.previewUrl, onStateBitMap, offStateBitmap, user.getId(),
                repoOperation.getDateTime(), metadata.label, metadata.contentTypeId,
                contentService.getContentTypeClass(site.getSiteId(), repoOperation.getPath()),
                StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                contentRepository.getContentSize(site.getSiteId(), repoOperation.getPath())).getBytes(UTF_8), StandardOpenOption.APPEND);
        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
        logger.debug("Extract dependencies from site '{}' path '{}'",
                site.getSiteId(), repoOperation.getPath());
        DependencyUtils.addDependenciesScriptSnippets(site.getSiteId(), repoOperation.getPath(), null,
                repoOperationsScriptPath, dependencyServiceInternal);
    }

    private void processMove(Site site, RepoOperation repoOperation, User user,
                             Path repoOperationsScriptPath, Path updateParentIdScriptPath) throws IOException, ServiceLayerException {

        ItemMetadata metadata = getItemMetadata(site.getSiteId(), repoOperation.getMoveToPath());
        processAncestors(site.getId(), repoOperation.getMoveToPath(), user.getId(),
                repoOperation.getDateTime(), repoOperationsScriptPath);
        long onStateBitMap = SAVE_AND_CLOSE_ON_MASK;
        long offStateBitmap = SAVE_AND_CLOSE_OFF_MASK;
        if (metadata.disabled) {
            onStateBitMap = onStateBitMap | DISABLED.value;
        } else {
            offStateBitmap = offStateBitmap | DISABLED.value;
        }
        if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath())) ||
                ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getMoveToPath()))) {
            addUpdateParentIdScriptSnippets(site.getId(), repoOperation.getMoveToPath(),
                    updateParentIdScriptPath);
        } else {
            Files.write(repoOperationsScriptPath, moveItemRow(site.getId(), repoOperation.getPath(),
                            repoOperation.getMoveToPath(), onStateBitMap, offStateBitmap).getBytes(UTF_8),
                    StandardOpenOption.APPEND);
            Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
            Files.write(repoOperationsScriptPath, updateItemRow(site.getId(),
                    repoOperation.getPath(), metadata.previewUrl, onStateBitMap, offStateBitmap, user.getId(),
                    repoOperation.getDateTime(), metadata.label, metadata.contentTypeId,
                    contentService.getContentTypeClass(site.getSiteId(), repoOperation.getPath()),
                    StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                    contentRepository.getContentSize(site.getSiteId(), repoOperation.getPath()))
                    .getBytes(UTF_8), StandardOpenOption.APPEND);
            Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
            addUpdateParentIdScriptSnippets(site.getId(), repoOperation.getMoveToPath(), updateParentIdScriptPath);
            DependencyUtils.addDependenciesScriptSnippets(site.getSiteId(), repoOperation.getMoveToPath(),
                    repoOperation.getPath(), repoOperationsScriptPath, dependencyServiceInternal);
        }
        invalidateConfigurationCacheIfRequired(site.getSiteId(), repoOperation.getMoveToPath());
    }

    private void processDelete(Site site, RepoOperation repoOperation, Path repoOperationsScriptPath) throws IOException {
        String folder = FILE_SEPARATOR + FilenameUtils.getPathNoEndSeparator(repoOperation.getPath());
        boolean folderExists = contentRepository.contentExists(site.getSiteId(), folder);

        // If the folder exists and the deleted file is the index file, then we need to update the parent id for the children
        if (folderExists && startsWith(repoOperation.getPath(), ROOT_PATTERN_PAGES) &&
                endsWith(repoOperation.getPath(), SLASH_INDEX_FILE)) {
            Files.write(repoOperationsScriptPath,
                    updateDeletedPageChildren(site.getId(), folder).getBytes(UTF_8), StandardOpenOption.APPEND);
        }

        Files.write(repoOperationsScriptPath,
                deleteItemRow(site.getId(), repoOperation.getPath()).getBytes(UTF_8),
                StandardOpenOption.APPEND);
        if (!folderExists) {
            Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
            Files.write(repoOperationsScriptPath,
                    deleteItemRow(site.getId(), folder).getBytes(UTF_8), StandardOpenOption.APPEND);
        }
        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
        Files.write(repoOperationsScriptPath,
                deleteDependencyRows(site.getSiteId(), repoOperation.getPath()).getBytes(UTF_8),
                StandardOpenOption.APPEND);
        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
    }

    protected void invalidateConfigurationCacheIfRequired(String siteId, String path) {
        String[] configurationPatterns = studioConfiguration.getArray(CONFIGURATION_PATH_PATTERNS, String.class);
        if (RegexUtils.matchesAny(path, configurationPatterns)) {
            configurationService.invalidateConfiguration(siteId, path);
        }
    }

    /**
     * Add the script snippets to insert the parents of the given path.
     *
     * @param siteId               The site id
     * @param path                 The path
     * @param userId               The user id
     * @param now                  The current date time
     * @param createFileScriptPath The path to the script file
     * @throws IOException If an error occurs
     */
    private void processAncestors(long siteId, String path, long userId, ZonedDateTime now,
                                  Path createFileScriptPath) throws IOException {
        Path p = Paths.get(path);
        if (!nonNull(p.getParent())) {
            return;
        }

        List<Path> parts = new LinkedList<>();
        p.getParent().iterator().forEachRemaining(parts::add);
        String currentPath = EMPTY;
        for (Path ancestor : parts) {
            if (isNotEmpty(ancestor.toString())) {
                currentPath = currentPath + FILE_SEPARATOR + ancestor;
                Files.write(createFileScriptPath, insertItemRow(siteId, currentPath, null, NEW.value, null, userId
                                , now, userId, now, null, ancestor.toString(), null, CONTENT_TYPE_FOLDER, null,
                                Locale.US.toString(), null, 0L, null, null).getBytes(UTF_8),
                        StandardOpenOption.APPEND);
                Files.write(createFileScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
            }
        }
    }

    /**
     * Adds a path to the 'update parent id' script.
     * It will also add the parent paths recursively.
     * TODO: JM: try to remove recursion
     *
     * @param siteId                   the site id
     * @param path                     the path
     * @param updateParentIdScriptPath the update parent id script path
     * @throws IOException if an error occurs while updating the script
     */
    private void addUpdateParentIdScriptSnippets(long siteId, String path, Path updateParentIdScriptPath) throws IOException {
        String parentPath = FilenameUtils.getPrefix(path) +
                FilenameUtils.getPathNoEndSeparator(replace(path, SLASH_INDEX_FILE, ""));
        if (isEmpty(parentPath) || StringUtils.equals(parentPath, path)) {
            return;
        }
        addUpdateParentIdScriptSnippets(siteId, parentPath, updateParentIdScriptPath);
        if (endsWith(path, SLASH_INDEX_FILE)) {
            addUpdateParentIdScriptSnippets(siteId, replace(path,
                    "/index.xml", ""), updateParentIdScriptPath);
            if (startsWith(path, ROOT_PATTERN_PAGES)) {
                Files.write(updateParentIdScriptPath, updateNewPageChildren(siteId, path).getBytes(UTF_8),
                        StandardOpenOption.APPEND);
            }
        }
        Files.write(updateParentIdScriptPath, updateParentId(siteId, path, parentPath).getBytes(UTF_8),
                StandardOpenOption.APPEND);
        Files.write(updateParentIdScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
    }

    @Override
    public void setApplicationEventPublisher(@NotNull final ApplicationEventPublisher applicationEventPublisher) {
        this.eventPublisher = applicationEventPublisher;
    }

    /**
     * Convenience class to store item metadata, so we can load the content item only once.
     */
    private static class ItemMetadata {
        String previewUrl = null;
        String label;
        String contentTypeId = EMPTY;
        boolean disabled = false;

        public ItemMetadata(final String path) {
            label = FilenameUtils.getName(path);
        }
    }
}
