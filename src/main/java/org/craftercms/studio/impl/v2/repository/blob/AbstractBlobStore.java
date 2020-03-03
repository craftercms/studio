/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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
package org.craftercms.studio.impl.v2.repository.blob;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.craftercms.commons.config.ConfigurationException;
import org.craftercms.commons.config.ConfigurationMapper;
import org.craftercms.commons.config.profiles.ConfigurationProfile;
import org.craftercms.studio.api.v1.repository.RepositoryItem;
import org.craftercms.studio.api.v1.to.RemoteRepositoryInfoTO;
import org.craftercms.studio.api.v1.to.VersionTO;
import org.craftercms.studio.api.v2.dal.GitLog;
import org.craftercms.studio.api.v2.dal.RepoOperation;
import org.craftercms.studio.api.v2.repository.blob.BlobStore;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.commons.config.ConfigUtils.getRequiredStringProperty;

/**
 * Base class for all implementations of {@link BlobStore}
 *
 * @author joseross
 * @since 3.1.6
 */
public abstract class AbstractBlobStore<T extends ConfigurationProfile> implements BlobStore {

    /**
     * The id of the store
     */
    protected String id;

    /**
     * The regex to check for compatible paths
     */
    protected String pattern;

    /**
     * The mappings for the different environments
     */
    protected Map<String, Mapping> mappings;

    /**
     * The profile to connect to the remote store
     */
    protected T profile;

    /**
     * The mapper to load the profile configuration
     */
    protected ConfigurationMapper<T> profileMapper;

    public void setProfileMapper(ConfigurationMapper<T> profileMapper) {
        this.profileMapper = profileMapper;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Override
    public boolean isCompatible(String path) {
        return path != null && path.matches(pattern);
    }

    @Override
    public void init(HierarchicalConfiguration<ImmutableNode> config) throws ConfigurationException {
        id = getRequiredStringProperty(config, CONFIG_KEY_ID);
        pattern = getRequiredStringProperty(config, CONFIG_KEY_PATTERN);

        mappings = new LinkedHashMap<>();
        config.configurationsAt(CONFIG_KEY_MAPPING).forEach(bucketConfig -> {
            Mapping mapping = new Mapping();
            mapping.target = bucketConfig.getString(CONFIG_KEY_MAPPING_TARGET);
            mapping.prefix = bucketConfig.getString(CONFIG_KEY_MAPPING_PREFIX);
            mappings.put(bucketConfig.getString(CONFIG_KEY_MAPPING_ENVIRONMENT), mapping);
        });

        profile = profileMapper.processConfig(config.configurationAt(CONFIG_KEY_CONFIGURATION));
        profile.setProfileId(config.getString(CONFIG_KEY_ID));

        doInit(config);
    }

    protected abstract void doInit(HierarchicalConfiguration<ImmutableNode> config) throws ConfigurationException;

    protected Mapping getMapping(String environment) {
        return mappings.get(environment);
    }

    protected Mapping getPreviewMapping() {
        return getMapping(PREVIEW);
    }

    /**
     * Internal class used when loading the configuration
     */
    protected static class Mapping {

        /**
         * The target in the store
         */
        public String target;

        /**
         * The prefix to use in the store
         */
        public String prefix;

    }

    // Unsupported operations
    // TODO: Remove when the API is split

    // Start API 1
    @Override
    public RepositoryItem[] getContentChildren(String site, String path) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public VersionTO[] getContentVersionHistory(String site, String path) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public String createVersion(String site, String path, boolean majorVersion) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public String createVersion(String site, String path, String comment, boolean majorVersion) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public String revertContent(String site, String path, String version, boolean major, String comment) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getContentVersion(String site, String path, String version) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void lockItem(String site, String path) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void lockItemForPublishing(String site, String path) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void unLockItem(String site, String path) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void unLockItemForPublishing(String site, String path) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFolder(String siteId, String path) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean createSiteFromBlueprint(String blueprintLocation, String siteId, String sandboxBranch,
                                           Map<String, String> params) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteSite(String siteId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialPublish(String site, String sandboxBranch, String environment, String author, String comment) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRepoLastCommitId(String site) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRepoFirstCommitId(String site) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getEditCommitIds(String site, String path, String commitIdFrom, String commitIdTo) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean commitIdExists(String site, String commitId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertFullGitLog(String siteId, int processed) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteGitLogForSite(String siteId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean createSiteCloneRemote(String siteId, String sandboxBranch, String remoteName, String remoteUrl,
                                         String remoteBranch, boolean singleBranch, String authenticationType,
                                         String remoteUsername, String remotePassword, String remoteToken,
                                         String remotePrivateKey, Map<String, String> params, boolean createAsOrphan) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean createSitePushToRemote(String siteId, String remoteName, String remoteUrl,
                                          String authenticationType, String remoteUsername,
                                          String remotePassword, String remoteToken, String remotePrivateKey,
                                          boolean createAsOrphan) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addRemote(String siteId, String remoteName, String remoteUrl, String authenticationType,
                             String remoteUsername, String remotePassword, String remoteToken,
                             String remotePrivateKey) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeRemote(String siteId, String remoteName) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeRemoteRepositoriesForSite(String siteId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public List<RemoteRepositoryInfoTO> listRemote(String siteId, String sandboxBranch) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean pushToRemote(String siteId, String remoteName, String remoteBranch) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean pullFromRemote(String siteId, String remoteName, String remoteBranch) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetStagingRepository(String siteId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void reloadRepository(String siteId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void cleanupRepositories(String siteId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean repositoryExists(String site) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    // Start API 2

    @Override
    public List<String> getSubtreeItems(String site, String path) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public List<RepoOperation> getOperations(String site, String commitIdFrom, String commitIdTo) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public List<RepoOperation> getOperationsFromDelta(String site, String commitIdFrom, String commitIdTo) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public GitLog getGitLog(String siteId, String commitId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void markGitLogVerifiedProcessed(String siteId, String commitId) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

    @Override
    public void insertGitLog(String siteId, String commitId, int processed) {
        // This should be handled by the local repository
        throw new UnsupportedOperationException();
    }

}
