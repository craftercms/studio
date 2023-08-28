/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.upgrade.operations.db;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.commons.upgrade.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.dal.Item;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v2.upgrade.StudioUpgradeContext;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.*;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.time.ZoneOffset.UTC;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.dal.ItemState.DISABLED;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.eclipse.jgit.lib.Constants.HEAD;

public final class PopulateItemTableUpgradeOperation extends DbScriptUpgradeOperation {

    public static final Logger logger = LoggerFactory.getLogger(PopulateItemTableUpgradeOperation.class);

    public static final String CONFIG_KEY_CLEAR_EXISTING_DATA = "clearExistingData";
    public static final String CONFIG_KEY_STORED_PROCEDURE_NAME = "spName";
    public static final String CONFIG_KEY_STORED_PARENT_ID_PROCEDURE_NAME = "parentIdSpName";
    public static final String QUERY_GET_ALL_SITES =
            "SELECT id, site_id FROM " + CRAFTER_SCHEMA_NAME + ".site WHERE system = 0 AND deleted = 0";
    public static final String STORED_PROCEDURE_NAME = "@spName";
    public static final String SP_PARAM_SITE = "@site";
    public static final String QUERY_PARAM_SITE_ID = "@siteId";
    public static final String QUERY_CHECK_DATA_EXISTS =
            "SELECT count(1) FROM " + CRAFTER_SCHEMA_NAME + ".item WHERE site_id = " + QUERY_PARAM_SITE_ID;
    public static final String QUERY_CALL_STORED_PROCEDURE =
            "call @spName('@site')";

    private boolean clearExistingData;
    private String crafterSchemaName;
    private String spName;
    private String populateParentIdSpName;
    private final String blobExtension;
    private final ItemServiceInternal itemServiceInternal;
    private final ContentService contentService;
    private final GitRepositoryHelper gitRepositoryHelper;
    private final long executorTimeoutSeconds;
    private final int executorThreadCount;

    @ConstructorProperties({"studioConfiguration", "scriptFolder", "integrityValidator", "itemServiceInternal",
            "contentService", "gitRepositoryHelper", "blobExtension", "executorTimeoutSeconds", "executorThreadCount"})
    public PopulateItemTableUpgradeOperation(StudioConfiguration studioConfiguration,
                                             String scriptFolder,
                                             DbIntegrityValidator integrityValidator,
                                             ItemServiceInternal itemServiceInternal,
                                             ContentService contentService,
                                             GitRepositoryHelper gitRepositoryHelper,
                                             String blobExtension,
                                             long executorTimeoutSeconds,
                                             int executorThreadCount) {
        super(studioConfiguration, scriptFolder, integrityValidator);
        this.itemServiceInternal = itemServiceInternal;
        this.contentService = contentService;
        this.gitRepositoryHelper = gitRepositoryHelper;
        this.blobExtension = blobExtension;
        this.executorTimeoutSeconds = executorTimeoutSeconds;
        this.executorThreadCount = executorThreadCount;
    }

    @Override
    public void doInit(HierarchicalConfiguration config) {
        super.doInit(config);
        clearExistingData = config.getBoolean(CONFIG_KEY_CLEAR_EXISTING_DATA, false);
        crafterSchemaName = studioConfiguration.getProperty(DB_SCHEMA);
        spName = config.getString(CONFIG_KEY_STORED_PROCEDURE_NAME);
        populateParentIdSpName = config.getString(CONFIG_KEY_STORED_PARENT_ID_PROCEDURE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        // create stored procedure from script (if needed)
        if (isNotEmpty(fileName)) {
            super.doExecute(context);
        }
        // get all sites from DB
        Map<Long, String> sites = new HashMap<>();
        try (Connection connection = context.getConnection()) {
            try(Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery(
                    QUERY_GET_ALL_SITES.replace(CRAFTER_SCHEMA_NAME, crafterSchemaName))) {
                while (rs.next()) {
                    sites.put(rs.getLong(1), rs.getString(2));
                }
            } catch (SQLException e) {
                logger.error("Failed to get all sites from the database", e);
            }
        } catch (SQLException e) {
            logger.error("Failed to get a database connection", e);
        }

        // loop over all sites
        for (Map.Entry<Long, String> site : sites.entrySet()) {
            processSite(context, site.getKey(), site.getValue());
        }
    }

    private void processSite(final StudioUpgradeContext context, long siteId, String site) {
        // check if data exists
        logger.info("Process site '{}'", site);
        boolean shouldProcess = false;
        try (Connection connection = context.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_CHECK_DATA_EXISTS
                    .replace(CRAFTER_SCHEMA_NAME, crafterSchemaName)
                    .replace(QUERY_PARAM_SITE_ID, String.valueOf(siteId)))) {
                ResultSet rs = statement.executeQuery();
                shouldProcess = !rs.next() || rs.getInt(1) < 1 || clearExistingData;
            } catch (SQLException e) {
                logger.error("Failed to check if item data already exists in the database for site '{}'", site);
            }
        } catch (SQLException e) {
            logger.error("Failed to get a database connection");
        }

        try {
            if (shouldProcess) {
                populateDataFromDB(context, site);
            }
        } catch (UpgradeException e) {
            logger.error("Failed to populate item table for site '{}'", site, e);
        }

        try {
            populateDataFromRepo(site);
            logger.debug("Update the parent IDs in site '{}'", site);
            populateParentId(context, site, siteId);
        } catch (Exception e) {
            logger.error("Failed to populate the item table from the repository for site '{}'", site, e);
        }
    }

    /**
     * Calls the 'populate parent id' stored procedure. It takes the name from
     * the property 'parentIdSpName'.
     * The SP is meant to set the parent Id for each item based on the path.
     * @param context the upgrade context
     * @param site the site id
     * @param siteId the numeric site id
     */
    private void populateParentId(final StudioUpgradeContext context, String site, long siteId) {
        logger.debug("Execute the stored procedure '{}' in site '{}'", populateParentIdSpName, site);
        try (Connection connection = context.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall(
                    QUERY_CALL_STORED_PROCEDURE.replace(STORED_PROCEDURE_NAME, populateParentIdSpName)
                            .replace(SP_PARAM_SITE, String.valueOf(siteId)));
            callableStatement.execute();
        } catch (SQLException e) {
            logger.error("Failed to populate item table parent ID for site '{}'", site, e);
        }
    }

    private void populateDataFromDB(final StudioUpgradeContext context, String siteId) throws UpgradeException {
        try (Connection connection = context.getConnection()) {
            integrityValidator.validate(connection);
        } catch (SQLException e) {
            // for backward compatibility
            logger.warn("Failed to validate database integrity", e);
        } catch (Exception e) {
            throw new UpgradeNotSupportedException("The current database version can't be upgraded", e);
        }

        logger.debug("Execute the stored procedure '{}' in site '{}'", spName, siteId);
        try (Connection connection = context.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall(
                    QUERY_CALL_STORED_PROCEDURE.replace(STORED_PROCEDURE_NAME, spName)
                            .replace(SP_PARAM_SITE, siteId));
            callableStatement.execute();
        } catch (SQLException e) {
            logger.error("Failed to populate data from the database for site '{}'", siteId, e);
        }
    }

    private void populateDataFromRepo(final String siteName)
            throws Exception {
        logger.debug("Populate data from the repository for site '{}'", siteName);
        try (Repository repo = getRepository(siteName)) {
            ObjectId objCommitId = repo.resolve(HEAD);
            try (RevWalk walk = new RevWalk(repo)) {
                RevCommit commit = walk.parseCommit(objCommitId);
                RevTree tree = commit.getTree();
                TreeWalk treeWalk = new TreeWalk(repo);
                treeWalk.addTree(tree);
                treeWalk.setRecursive(false);
                ExecutorService taskExecutor = Executors.newFixedThreadPool(executorThreadCount);
                while (treeWalk.next()) {
                    String pathString = treeWalk.getPathString();
                    String nameString = treeWalk.getNameString();
                    if (treeWalk.isSubtree()) {
                        taskExecutor.execute(() -> {
                                    try {
                                        processFolder(siteName, FILE_SEPARATOR + pathString,
                                                ObjectId.toString(commit), nameString);
                                    } catch (IOException e) {
                                        logger.error("Failed to process file '{}' in site '{}'", pathString,
                                                siteName, e);
                                    }
                                }
                        );
                        treeWalk.enterSubtree();
                    } else {
                        if (StringUtils.containsAny(getName(nameString), IGNORE_FILES)) {
                            logger.debug("Skip ignored file '{}' in site '{}'", pathString,
                                    siteName);
                        } else {
                            taskExecutor.execute(() -> {
                                try {
                                    processFile(siteName, FILE_SEPARATOR + pathString,
                                            ObjectId.toString(commit), nameString);
                                } catch (DocumentException | IOException e) {
                                    logger.error("Failed to process file '{}' in site '{}'", pathString,
                                            siteName, e);
                                }
                            });
                        }
                    }
                }
                taskExecutor.shutdown();
                taskExecutor.awaitTermination(executorTimeoutSeconds, TimeUnit.SECONDS);
            }
        }
    }

    private Repository getRepository(String site) {
        return gitRepositoryHelper.getRepository(site, GitRepositories.SANDBOX);
    }

    private void processFolder(String site, String path, String commitId,
                               String name) throws IOException {
        logger.debug("Process the folder '{}' in site '{}'", path, site);
        File folder = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH)).toFile();
        Item item = itemServiceInternal.instantiateItem(site, path)
                .withLastModifiedOn(folder.lastModified() > 0 ?
                        ZonedDateTime.from(Instant.ofEpochMilli(folder.lastModified()).atZone(UTC)) : null)
                .withLabel(name)
                .withSystemType("folder")
                .build();
        itemServiceInternal.upsertEntry(item);
    }

    private void processFile(String site, String path, String commitId,
                             String name) throws DocumentException, IOException {
        logger.debug("Process the file '{}' in site '{}'", path, site);
        File file = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH)).toFile();

        if (StringUtils.endsWith(path, blobExtension)) {
            path = StringUtils.removeEnd(path, "." + blobExtension);
            name = StringUtils.removeEnd(name, "." + blobExtension);
        }

        Item item = itemServiceInternal.instantiateItem(site, path)
                .withPreviewUrl(itemServiceInternal.getBrowserUrl(site, path))
                .withLastModifiedOn(file.lastModified() > 0 ?
                        ZonedDateTime.from(Instant.ofEpochMilli(file.lastModified()).atZone(UTC)) : null)
                .withLabel(name)
                .withSystemType(contentService.getContentTypeClass(site, path))
                .withMimeType(StudioUtils.getMimeType(name))
                .withSize(contentService.getContentSize(site, path))
                .withIgnored(ArrayUtils.contains(IGNORE_FILES, name))
                .build();

        if (StringUtils.endsWith(name, ".xml")) {
            populateDescriptorProperties(site, path, item);
        }

        itemServiceInternal.upsertEntry(item);
    }

    private void populateDescriptorProperties(String site, String path, Item item) throws DocumentException {
        logger.debug("Extract the descriptor properties from file '{}' in site '{}'", path, site);
        Document document = contentService.getContentAsDocument(site, path);
        if(document != null) {
            Element rootElement = document.getRootElement();

            String internalName = rootElement.valueOf("internal-name");
            String contentType = rootElement.valueOf("content-type");
            String disabled = rootElement.valueOf("disabled");

            if (isNotEmpty(internalName)) {
                item.setLabel(internalName);
            }
            item.setContentTypeId(isNotEmpty(contentType) ? contentType : null);
            if (isNotEmpty(disabled) && "true".equalsIgnoreCase(disabled)) {
                item.setState(item.getState() | DISABLED.value);
            }
        }
    }

}
