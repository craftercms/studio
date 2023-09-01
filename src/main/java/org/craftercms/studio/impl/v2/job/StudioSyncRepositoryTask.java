/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.job;

import org.apache.commons.collections4.CollectionUtils;
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
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.dependency.internal.DependencyServiceInternal;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;

import java.beans.ConstructorProperties;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.ZonedDateTime;
import java.util.*;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v1.constant.DmConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioConstants.*;
import static org.craftercms.studio.api.v1.constant.StudioXmlConstants.*;
import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.*;
import static org.craftercms.studio.api.v2.dal.ItemState.*;
import static org.craftercms.studio.api.v2.utils.SqlStatementGeneratorUtils.*;
import static org.craftercms.studio.api.v2.utils.StudioUtils.getStudioTemporaryFilesRoot;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_REPO_USER_USERNAME;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;

public class StudioSyncRepositoryTask extends StudioClockTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioSyncRepositoryTask.class);
    private final static String REPO_OPERATIONS_SCRIPT_PREFIX = "repoOperations_";
    private final static String UPDATE_PARENT_ID_SCRIPT_PREFIX = "updateParentId_";

    protected StudioDBScriptRunnerFactory studioDBScriptRunnerFactory;
    private static int threadCounter = 0;

    private final SitesService sitesService;
    private final GeneralLockService generalLockService;
    private final AuditServiceInternal auditServiceInternal;
    private final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
    private final DependencyServiceInternal dependencyServiceInternal;
    private final UserServiceInternal userServiceInternal;
    private final ItemServiceInternal itemServiceInternal;
    private final ContentService contentService;
    private final ConfigurationService configurationService;
    protected String[] configurationPatterns;

    public void init() {
        threadCounter++;
    }

    @ConstructorProperties({"sitesService", "generalLockService",
            "auditServiceInternal",
            "retryingDatabaseOperationFacade", "configurationPatterns",
            "studioDBScriptRunnerFactory", "dependencyServiceInternal",
            "userServiceInternal", "itemServiceInternal",
            "contentService", "configurationService"})
    public StudioSyncRepositoryTask(SitesService sitesService, GeneralLockService generalLockService,
                                    AuditServiceInternal auditServiceInternal,
                                    RetryingDatabaseOperationFacade retryingDatabaseOperationFacade, String[] configurationPatterns,
                                    StudioDBScriptRunnerFactory studioDBScriptRunnerFactory, DependencyServiceInternal dependencyServiceInternal,
                                    UserServiceInternal userServiceInternal, ItemServiceInternal itemServiceInternal
            , ContentService contentService, ConfigurationService configurationService) {
        this.sitesService = sitesService;
        this.generalLockService = generalLockService;
        this.auditServiceInternal = auditServiceInternal;
        this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
        this.studioDBScriptRunnerFactory = studioDBScriptRunnerFactory;
        this.configurationPatterns = configurationPatterns;
        this.dependencyServiceInternal = dependencyServiceInternal;
        this.userServiceInternal = userServiceInternal;
        this.itemServiceInternal = itemServiceInternal;
        this.contentService = contentService;
        this.configurationService = configurationService;
    }

    @Override
    protected void executeInternal(String site) {
        try {
            logger.debug("Execute sync repository thread counter '{}' ID '{}'", threadCounter,
                    Thread.currentThread().getId());
            String siteState = siteService.getSiteState(site);
            if (StringUtils.equals(siteState, STATE_READY)) {
                applicationContext.publishEvent(new SyncFromRepoEvent(site));
            }
        } catch (Exception e) {
            logger.error("Failed to sync the database from the repository in site '{}'", site, e);
        }
    }

    @EventListener
    protected void syncRepoListener(SyncFromRepoEvent event) throws ServiceLayerException {
        syncRepository(event.getSiteId());
    }

    private void syncRepository(final String siteId) throws ServiceLayerException {
        logger.debug("Sync the database with the repository in site '{}'", siteId);

        Site site = sitesService.getSite(siteId);
        if (!sitesService.checkSiteUuid(siteId, site.getSiteUuid())) {
            return;
        }
        String gitLockKey = StudioUtils.getSandboxRepoLockKey(siteId);
        generalLockService.lock(gitLockKey);
        try {
            // Get the last commit to be used along the sync process (instead of 'HEAD',
            // commits added after this point will be processed in subsequent executions of this method)
            final String lastCommitInRepo = contentRepository.getRepoLastCommitId(siteId);
            final String lastProcessedCommit = sitesService.getLastCommitId(siteId);
            if (StringUtils.equals(lastCommitInRepo, lastProcessedCommit)) {
                return;
            }
            // Some of these (the ones created by Studio APIs) will already be in the audit table
            List<String> unprocessedCommits;
            try {
                unprocessedCommits = contentRepository.getCommitIdsBetween(siteId, lastProcessedCommit, lastCommitInRepo);
            } catch (IOException e) {
                throw new ServiceLayerException(format("Failed to get unprocessed commits to sync repository for site '%s'", siteId), e);
            }

            String currentLastProcessedCommit = lastProcessedCommit;
            String lastUnprocessedCommit = null;
            for (String commitId : unprocessedCommits) {
                if (auditServiceInternal.isAudited(site.getId(), commitId)) {
                    if (lastUnprocessedCommit != null) {
                        ingestChanges(site, currentLastProcessedCommit, lastUnprocessedCommit);
                        lastUnprocessedCommit = null;
                    }
                    updateLastCommitId(siteId, commitId);
                    currentLastProcessedCommit = commitId;
                } else {
                    lastUnprocessedCommit = commitId;
                }
            }
            if (lastUnprocessedCommit != null) {
                ingestChanges(site, currentLastProcessedCommit, lastUnprocessedCommit);
                updateLastCommitId(siteId, lastUnprocessedCommit);
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    private void updateLastCommitId(String siteId, String commitId) {
        retryingDatabaseOperationFacade.retry(() -> sitesService.updateLastCommitId(siteId, commitId));
    }

    private void ingestChanges(final Site site, final String commitFrom, final String commitTo) {
        List<RepoOperation> operationsFromDelta = contentRepository.getOperationsFromDelta(site.getSiteId(), commitFrom, commitTo);
        syncDatabaseWithRepo(site, operationsFromDelta);
        auditChangesFromGit(site, commitFrom, commitTo);

        // Sync all preview deployers
        try {
            logger.debug("Sync preview for site '{}'", site);
            applicationContext.publishEvent(new RepositoryEvent(site.getSiteId()));
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
    private void auditChangesFromGit(final Site site, final String commitFrom, final String commitTo) {
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
        }

        auditServiceInternal.insertAuditLog(auditLogEntry);
    }

    private void syncDatabaseWithRepo(Site site, List<RepoOperation> repoOperationsDelta) {
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

    private void processRepoOperations(Site site, List<RepoOperation> repoOperations,
                                       Path repoOperationsScriptPath, Path updateParentIdScriptPath) throws IOException {
        User userObj = null;
        Map<String, User> cachedUsers = new HashMap<>();
        try {
            cachedUsers.put(GIT_REPO_USER_USERNAME, userServiceInternal.getUserByIdOrUsername(-1, GIT_REPO_USER_USERNAME));
        } catch (UserNotFoundException | ServiceLayerException e) {
            logger.error("Failed to process repo operations in site '{}'. git_repo_user should be in the the database",
                    site.getSiteId(), e);
        }

        String label;
        String contentTypeId;
        String previewUrl;
        boolean disabled;
        long state;
        long onStateBitMap;
        long offStateBitmap;
        for (RepoOperation repoOperation : repoOperations) {
            switch (repoOperation.getAction()) {
                case CREATE:
                case COPY:
                    if (cachedUsers.containsKey(repoOperation.getAuthor())) {
                        userObj = cachedUsers.get(repoOperation.getAuthor());
                    } else {
                        try {
                            userObj = userServiceInternal.getUserByIdOrUsername(-1, repoOperation.getAuthor());
                        } catch (UserNotFoundException | ServiceLayerException e) {
                            logger.debug("User '{}' not found while processing operations in site '{}'",
                                    repoOperation.getAuthor(), site.getSiteId(), e);
                        }
                    }
                    if (Objects.isNull(userObj)) {
                        userObj = cachedUsers.get(GIT_REPO_USER_USERNAME);
                    }
                    label = FilenameUtils.getName(repoOperation.getPath());
                    contentTypeId = StringUtils.EMPTY;
                    disabled = false;
                    if (StringUtils.endsWith(repoOperation.getPath(), XML_PATTERN)) {
                        try {
                            Document contentDoc = contentService.getContentAsDocument(site.getSiteId(), repoOperation.getPath());
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
                            logger.error("Failed to extract metadata from the XML site '{}' path '{}'",
                                    site.getSiteId(), repoOperation.getPath(), e);
                        }
                    }
                    previewUrl = null;
                    if (StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_PAGES) ||
                            StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_ASSETS)) {
                        previewUrl = itemServiceInternal.getBrowserUrl(site.getSiteId(), repoOperation.getPath());
                    }
                    processAncestors(site.getId(), repoOperation.getPath(), userObj.getId(),
                            repoOperation.getDateTime(), repoOperation.getCommitId(), repoOperationsScriptPath);
                    state = NEW.value;
                    if (disabled) {
                        state = state | DISABLED.value;
                    }

                    if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath()))) {
                        addUpdateParentIdScriptSnippets(site.getId(), repoOperation.getPath(),
                                updateParentIdScriptPath);
                    } else {
                        Files.write(repoOperationsScriptPath, insertItemRow(site.getId(),
                                repoOperation.getPath(), previewUrl, state, null, userObj.getId(),
                                repoOperation.getDateTime(), userObj.getId(), repoOperation.getDateTime(),
                                null, label, contentTypeId,
                                contentService.getContentTypeClass(site.getSiteId(), repoOperation.getPath()),
                                StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                                Locale.US.toString(), null,
                                contentRepository.getContentSize(site.getSiteId(), repoOperation.getPath()), null,
                                repoOperation.getCommitId(), null).getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        logger.debug("Extract dependencies from site '{}' path '{}'",
                                site.getSiteId(), repoOperation.getPath());
                        addUpdateParentIdScriptSnippets(site.getId(), repoOperation.getPath(),
                                updateParentIdScriptPath);
                        addDependenciesScriptSnippets(site.getSiteId(), repoOperation.getPath(), null,
                                repoOperationsScriptPath);
                    }
                    break;

                case UPDATE:
                    if (!ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath()))) {
                        if (cachedUsers.containsKey(repoOperation.getAuthor())) {
                            userObj = cachedUsers.get(repoOperation.getAuthor());
                        } else {
                            try {
                                userObj = userServiceInternal.getUserByIdOrUsername(-1, repoOperation.getAuthor());
                            } catch (UserNotFoundException | ServiceLayerException e) {
                                logger.debug("User '{}' not found while processing operations in site '{}'",
                                        repoOperation.getAuthor(), site.getSiteId(), e);
                            }
                        }
                        if (Objects.isNull(userObj)) {
                            userObj = cachedUsers.get(GIT_REPO_USER_USERNAME);
                        }
                        label = FilenameUtils.getName(repoOperation.getPath());
                        contentTypeId = StringUtils.EMPTY;
                        disabled = false;
                        if (StringUtils.endsWith(repoOperation.getPath(), XML_PATTERN)) {
                            try {
                                Document contentDoc = contentService.getContentAsDocument(site.getSiteId(), repoOperation.getPath());
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
                                logger.error("Failed to extract metadata from the XML site '{}' path '{}'",
                                        site.getSiteId(), repoOperation.getPath(), e);
                            }
                        }
                        previewUrl = null;
                        if (StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_PAGES) ||
                                StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_ASSETS)) {
                            previewUrl = itemServiceInternal.getBrowserUrl(site.getSiteId(), repoOperation.getPath());
                        }
                        onStateBitMap = SAVE_AND_CLOSE_ON_MASK;
                        offStateBitmap = SAVE_AND_CLOSE_OFF_MASK;
                        if (disabled) {
                            onStateBitMap = onStateBitMap | DISABLED.value;
                        } else {
                            offStateBitmap = offStateBitmap | DISABLED.value;
                        }

                        Files.write(repoOperationsScriptPath, updateItemRow(site.getId(),
                                repoOperation.getPath(), previewUrl, onStateBitMap, offStateBitmap, userObj.getId(),
                                repoOperation.getDateTime(), label, contentTypeId,
                                contentService.getContentTypeClass(site.getSiteId(), repoOperation.getPath()),
                                StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                                contentRepository.getContentSize(site.getSiteId(), repoOperation.getPath()),
                                repoOperation.getCommitId()).getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        logger.debug("Extract dependencies from site '{}' path '{}'",
                                site.getSiteId(), repoOperation.getPath());
                        addDependenciesScriptSnippets(site.getSiteId(), repoOperation.getPath(), null, repoOperationsScriptPath);
                    }
                    break;
                case DELETE:
                    String folder = FILE_SEPARATOR + FilenameUtils.getPathNoEndSeparator(repoOperation.getPath());
                    boolean folderExists = contentRepository.contentExists(site.getSiteId(), folder);

                    // If the folder exists and the deleted file is the index file, then we need to update the parent id for the children
                    if (folderExists && StringUtils.startsWith(repoOperation.getPath(), ROOT_PATTERN_PAGES) &&
                            StringUtils.endsWith(repoOperation.getPath(), SLASH_INDEX_FILE)) {
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
                    break;

                case MOVE:
                    if (cachedUsers.containsKey(repoOperation.getAuthor())) {
                        userObj = cachedUsers.get(repoOperation.getAuthor());
                    } else {
                        try {
                            userObj = userServiceInternal.getUserByIdOrUsername(-1, repoOperation.getAuthor());
                        } catch (UserNotFoundException | ServiceLayerException e) {
                            logger.debug("User '{}' not found while processing operations in site '{}'",
                                    repoOperation.getAuthor(), site.getSiteId(), e);
                        }
                    }
                    if (Objects.isNull(userObj)) {
                        userObj = cachedUsers.get(GIT_REPO_USER_USERNAME);
                    }
                    label = FilenameUtils.getName(repoOperation.getMoveToPath());
                    contentTypeId = StringUtils.EMPTY;
                    disabled = false;
                    if (StringUtils.endsWith(repoOperation.getMoveToPath(), XML_PATTERN)) {
                        try {
                            Document contentDoc = contentService.getContentAsDocument(site.getSiteId(), repoOperation.getMoveToPath());
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
                            logger.error("Failed to extact metadata from the XML site '{}' path '{}'",
                                    site.getSiteId(), repoOperation.getMoveToPath(), e);
                        }
                    }
                    previewUrl = null;
                    if (StringUtils.startsWith(repoOperation.getMoveToPath(), ROOT_PATTERN_PAGES) ||
                            StringUtils.startsWith(repoOperation.getMoveToPath(), ROOT_PATTERN_ASSETS)) {
                        previewUrl = itemServiceInternal.getBrowserUrl(site.getSiteId(), repoOperation.getMoveToPath());
                    }
                    processAncestors(site.getId(), repoOperation.getMoveToPath(), userObj.getId(),
                            repoOperation.getDateTime(), repoOperation.getCommitId(), repoOperationsScriptPath);
                    onStateBitMap = SAVE_AND_CLOSE_ON_MASK;
                    offStateBitmap = SAVE_AND_CLOSE_OFF_MASK;
                    if (disabled) {
                        onStateBitMap = onStateBitMap | DISABLED.value;
                    } else {
                        offStateBitmap = offStateBitmap | DISABLED.value;
                    }
                    if (ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getPath())) ||
                            ArrayUtils.contains(IGNORE_FILES, FilenameUtils.getName(repoOperation.getMoveToPath()))) {
                        addUpdateParentIdScriptSnippets(site.getId(), repoOperation.getMoveToPath(),
                                updateParentIdScriptPath);
                    } else {
                        Files.write(repoOperationsScriptPath, moveItemRow(site.getSiteId(), repoOperation.getPath(),
                                        repoOperation.getMoveToPath(), onStateBitMap, offStateBitmap).getBytes(UTF_8),
                                StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, updateItemRow(site.getId(),
                                repoOperation.getPath(), previewUrl, onStateBitMap, offStateBitmap, userObj.getId(),
                                repoOperation.getDateTime(), label, contentTypeId,
                                contentService.getContentTypeClass(site.getSiteId(), repoOperation.getPath()),
                                StudioUtils.getMimeType(FilenameUtils.getName(repoOperation.getPath())),
                                contentRepository.getContentSize(site.getSiteId(), repoOperation.getPath()),
                                repoOperation.getCommitId()).getBytes(UTF_8), StandardOpenOption.APPEND);
                        Files.write(repoOperationsScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                        addUpdateParentIdScriptSnippets(site.getId(), repoOperation.getMoveToPath(), updateParentIdScriptPath);
                        addDependenciesScriptSnippets(site.getSiteId(), repoOperation.getMoveToPath(),
                                repoOperation.getPath(), repoOperationsScriptPath);
                    }
                    invalidateConfigurationCacheIfRequired(site.getSiteId(), repoOperation.getMoveToPath());
                    break;

                default:
                    logger.error("Failed to process unknown repo operation '{}' in site '{}'",
                            site.getSiteId(), repoOperation.getAction());
                    break;
            }
            invalidateConfigurationCacheIfRequired(site.getSiteId(), repoOperation.getPath());
        }
    }

    protected void invalidateConfigurationCacheIfRequired(String siteId, String path) {
        if (RegexUtils.matchesAny(path, configurationPatterns)) {
            configurationService.invalidateConfiguration(siteId, path);
        }
    }

    private void processAncestors(long siteId, String path, long userId, ZonedDateTime now, String commitId,
                                  Path createFileScriptPath) throws IOException {
        Path p = Paths.get(path);
        List<Path> parts = new LinkedList<>();
        if (nonNull(p.getParent())) {
            p.getParent().iterator().forEachRemaining(parts::add);
        }
        String currentPath = StringUtils.EMPTY;
        if (CollectionUtils.isNotEmpty(parts)) {
            for (Path ancestor : parts) {
                if (isNotEmpty(ancestor.toString())) {
                    currentPath = currentPath + FILE_SEPARATOR + ancestor;
                    Files.write(createFileScriptPath, insertItemRow(siteId, currentPath, null, NEW.value, null, userId
                                    , now, userId, now, null, ancestor.toString(), null, CONTENT_TYPE_FOLDER, null,
                                    Locale.US.toString(), null, 0L, null, commitId, null).getBytes(UTF_8),
                            StandardOpenOption.APPEND);
                    Files.write(createFileScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
                }
            }
        }
    }

    private void addDependenciesScriptSnippets(String siteId, String path, String oldPath, Path file) throws IOException {
        long startDependencyResolver = logger.isDebugEnabled() ? System.currentTimeMillis() : 0L;
        Map<String, Set<String>> dependencies = dependencyServiceInternal.resolveDependencies(siteId, path);
        if (logger.isDebugEnabled()) {
            logger.debug("Dependency resolver for site '{}' path '{}' finished in '{}' milliseconds",
                    siteId, path, (System.currentTimeMillis() - startDependencyResolver));
        }
        if (StringUtils.isEmpty(oldPath)) {
            Files.write(file, deleteDependencySourcePathRows(siteId, path).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
            Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        } else {
            Files.write(file, deleteDependencySourcePathRows(siteId, oldPath).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);
            Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
        }
        if (nonNull(dependencies) && !dependencies.isEmpty()) {
            for (Map.Entry<String, Set<String>> entry : dependencies.entrySet()) {
                for (String targetPath : entry.getValue()) {
                    Files.write(file, insertDependencyRow(siteId, path, targetPath, entry.getKey())
                            .getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                    Files.write(file, "\n\n".getBytes(StandardCharsets.UTF_8), StandardOpenOption.APPEND);
                }
            }
        }
    }

    private void addUpdateParentIdScriptSnippets(long siteId, String path, Path updateParentIdScriptPath) throws IOException {
        String parentPath = FilenameUtils.getPrefix(path) +
                FilenameUtils.getPathNoEndSeparator(StringUtils.replace(path, SLASH_INDEX_FILE, ""));
        if (isEmpty(parentPath) || StringUtils.equals(parentPath, path)) {
            return;
        }
        addUpdateParentIdScriptSnippets(siteId, parentPath, updateParentIdScriptPath);
        if (StringUtils.endsWith(path, SLASH_INDEX_FILE)) {
            addUpdateParentIdScriptSnippets(siteId, StringUtils.replace(path,
                    "/index.xml", ""), updateParentIdScriptPath);
            if (StringUtils.startsWith(path, ROOT_PATTERN_PAGES)) {
                Files.write(updateParentIdScriptPath, updateNewPageChildren(siteId, path).getBytes(UTF_8),
                        StandardOpenOption.APPEND);
            }
        }
        Files.write(updateParentIdScriptPath, updateParentId(siteId, path, parentPath).getBytes(UTF_8),
                StandardOpenOption.APPEND);
        Files.write(updateParentIdScriptPath, "\n\n".getBytes(UTF_8), StandardOpenOption.APPEND);
    }

}
