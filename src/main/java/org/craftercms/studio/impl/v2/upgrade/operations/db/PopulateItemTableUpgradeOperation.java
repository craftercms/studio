/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.entitlements.validator.DbIntegrityValidator;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.commons.upgrade.exception.UpgradeNotSupportedException;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
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
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import java.beans.ConstructorProperties;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

import static java.time.ZoneOffset.UTC;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.DB_SCHEMA;
import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.IGNORE_FILES;
import static org.eclipse.jgit.lib.Constants.HEAD;

public final class PopulateItemTableUpgradeOperation extends DbScriptUpgradeOperation {

    public static final Logger logger = LoggerFactory.getLogger(PopulateItemTableUpgradeOperation.class);

    public static final String CONFIG_KEY_CLEAR_EXISTING_DATA = "clearExistingData";
    public static final String CONFIG_KEY_STORED_PROCEDURE_NAME = "spName";
    public static final String QUERY_GET_ALL_SITES =
            "SELECT id, site_id FROM " + CRAFTER_SCHEMA_NAME + ".site WHERE system = 0 AND deleted = 0";
    public static final String STORED_PROCEDURE_NAME = "@spName";
    public static final String SP_PARAM_SITE = "@site";
    public static final String QUERY_PARAM_SITE_ID = "@siteId";
    public static final String QUERY_CHECK_DATA_EXISTS =
            "SELECT count(1) FROM " + CRAFTER_SCHEMA_NAME + ".item WHERE site_id = " + QUERY_PARAM_SITE_ID;
    public static final String QUERY_CALL_STORED_PROCEDURE =
            "call @spName(@site)";

    private boolean clearExistingData;
    private String crafterSchemaName;
    private String spName;
    private ItemServiceInternal itemServiceInternal;
    private ContentService contentService;
    private GitRepositoryHelper gitRepositoryHelper;

    @ConstructorProperties({"studioConfiguration", "scriptFolder", "integrityValidator", "itemServiceInternal",
            "contentService", "gitRepositoryHelper"})
    public PopulateItemTableUpgradeOperation(StudioConfiguration studioConfiguration,
                                             String scriptFolder,
                                             DbIntegrityValidator integrityValidator,
                                             ItemServiceInternal itemServiceInternal,
                                             ContentService contentService,
                                             GitRepositoryHelper gitRepositoryHelper) {
        super(studioConfiguration, scriptFolder, integrityValidator);
        this.itemServiceInternal = itemServiceInternal;
        this.contentService = contentService;
        this.gitRepositoryHelper = gitRepositoryHelper;
    }

    @Override
    public void doInit(HierarchicalConfiguration config) {
        super.doInit(config);
        clearExistingData = config.getBoolean(CONFIG_KEY_CLEAR_EXISTING_DATA, false);
        crafterSchemaName = studioConfiguration.getProperty(DB_SCHEMA);
        spName = config.getString(CONFIG_KEY_STORED_PROCEDURE_NAME);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doExecute(final StudioUpgradeContext context) throws UpgradeException {
        // create stored procedure from script
        super.doExecute(context);
        // get all sites from DB
        Map<Long, String> sites = new HashMap<Long, String>();
        try (Connection connection = context.getConnection()) {
            try(Statement statement = connection.createStatement(); ResultSet rs = statement.executeQuery(
                    QUERY_GET_ALL_SITES.replace(CRAFTER_SCHEMA_NAME, crafterSchemaName))) {
                while (rs.next()) {
                    sites.put(rs.getLong(1), rs.getString(2));
                }
            } catch (SQLException e) {
                logger.error("Error getting all sites from DB", e);
            }
            // loop over all sites
            for (Map.Entry<Long, String> site : sites.entrySet()) {
                processSite(context, site.getKey(), site.getValue());
            }
        } catch (SQLException e) {
            logger.error("Error getting DB connection", e);
        }
    }

    private void processSite(final StudioUpgradeContext context, long siteId, String site) {
        // check if data exists
        logger.error("Processing site: " + site);
        try (Connection connection = context.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(QUERY_CHECK_DATA_EXISTS.replace(CRAFTER_SCHEMA_NAME, crafterSchemaName)
                    .replace(QUERY_PARAM_SITE_ID, String.valueOf(siteId)))) {
                ResultSet rs = statement.executeQuery();
                if (!rs.next() || rs.getInt(1) < 1 || clearExistingData) {
                    populateDataFromDB(context, siteId);
                    populateDataFromRepo(site);
                    itemServiceInternal.updateParentIds(site, StringUtils.EMPTY);
                }

            } catch (SQLException e) {
                logger.error("Error while checking if item data already exists for site " + site);
            } catch (UpgradeException e) {
                logger.error("Error populating item table from DB for site " + site, e);
            } catch (IOException | CryptoException e) {
                logger.error("Error populating item table from repository for site " + site, e);
            }
        } catch (SQLException e) {
            logger.error("Error while getting db connection");
        }
    }

    private void populateDataFromDB(final StudioUpgradeContext context, long siteId) throws UpgradeException {
        try (Connection connection = context.getConnection()) {
            integrityValidator.validate(connection);
        } catch (SQLException e) {
            // for backwards compatibility
            logger.warn("Could not validate database integrity", e);
        } catch (Exception e) {
            throw new UpgradeNotSupportedException("The current database version can't be upgraded", e);
        }

        try (Connection connection = context.getConnection()) {
            CallableStatement callableStatement = connection.prepareCall(
                    QUERY_CALL_STORED_PROCEDURE.replace(STORED_PROCEDURE_NAME, spName)
                            .replace(SP_PARAM_SITE, String.valueOf(siteId)));
            callableStatement.execute();
        } catch (SQLException e) {
            logger.error("Error populating data from DB", e);
        }
    }

    private void populateDataFromRepo(final String site)
            throws IOException, CryptoException {
        try (Repository repo = getRepository(site)) {
            Ref head = repo.findRef(HEAD);
            ObjectId objCommitId = repo.resolve(HEAD);
            String commitId = head.getName();
            try (RevWalk walk = new RevWalk(repo)) {
                RevCommit commit = walk.parseCommit(objCommitId);
                RevTree tree = commit.getTree();
                TreeWalk treeWalk = new TreeWalk(repo);
                treeWalk.addTree(tree);
                treeWalk.setRecursive(false);
                while (treeWalk.next()) {
                    try {
                        if (treeWalk.isSubtree()) {
                            processFolder(site, FILE_SEPARATOR + treeWalk.getPathString(), commitId,
                                    treeWalk.getNameString());
                            treeWalk.enterSubtree();
                        } else {
                            try {
                                processFile(site, FILE_SEPARATOR + treeWalk.getPathString(), commitId,
                                        treeWalk.getNameString());
                            } catch (DocumentException e) {
                                logger.error("Unexpected error processing file" + treeWalk.getPathString() +
                                        " for site " + site, e);
                            }
                        }
                    } catch (IOException e) {
                        logger.error("Unexpected error processing " + treeWalk.getPathString() + " for site " +
                                site, e);
                    }
                }
            }
        }
    }

    private Repository getRepository(String site) {
        return gitRepositoryHelper.getRepository(site, GitRepositories.SANDBOX);
    }

    private void processFolder(String site, String path, String commitId,
                               String name) {
        File folder = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH)).toFile();
        Item item = itemServiceInternal.instantiateItem(site, path).withCommitId(commitId).withPreviewUrl(path)
                .withLastModifiedOn(folder.lastModified() > 0 ?
                        ZonedDateTime.from(Instant.ofEpochMilli(folder.lastModified()).atZone(UTC)) : null)
                .withLabel(name).withSystemType("folder").build();
        itemServiceInternal.upsertEntry(site, item);
    }

    private void processFile(String site, String path, String commitId,
                             String name) throws DocumentException {
        File file = Paths.get(studioConfiguration.getProperty(StudioConfiguration.REPO_BASE_PATH),
                studioConfiguration.getProperty(StudioConfiguration.SITES_REPOS_PATH), site,
                studioConfiguration.getProperty(StudioConfiguration.SANDBOX_PATH)).toFile();
        Item item = itemServiceInternal.instantiateItem(site, path).withCommitId(commitId).withPreviewUrl(path)
                .withLastModifiedOn(file.lastModified() > 0 ?
                        ZonedDateTime.from(Instant.ofEpochMilli(file.lastModified()).atZone(UTC)) : null)
                .withLabel(name).withSystemType(contentService.getContentTypeClass(site, path))
                .withMimeType(StudioUtils.getMimeType(name))
                .withSize(file.length())
                .withIgnored(ArrayUtils.contains(IGNORE_FILES, name))
                .build();
        if (StringUtils.endsWith(name, ".xml")) {
            populateDescriptorProperties(site, path, item);
        }
        itemServiceInternal.upsertEntry(site, item);
    }

    private void populateDescriptorProperties(String site, String path, Item item) throws DocumentException {
        Document document = contentService.getContentAsDocument(site, path);
        if(document != null) {
            Element rootElement = document.getRootElement();

            String internalName = rootElement.valueOf("internal-name");
            String contentType = rootElement.valueOf("content-type");
            String disabled = rootElement.valueOf("disabled");

            if (StringUtils.isNotEmpty(internalName)) {
                item.setLabel(internalName);
            }
            item.setContentTypeId(StringUtils.isNotEmpty(contentType) ? contentType : null);
            item.setDisabled(StringUtils.isNotEmpty(disabled) && "true".equalsIgnoreCase(disabled));
        }
    }
}
