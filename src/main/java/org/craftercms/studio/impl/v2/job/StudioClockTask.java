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

package org.craftercms.studio.impl.v2.job;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.repository.InvalidRemoteUrlException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.ClusterDAO;
import org.craftercms.studio.api.v2.dal.RemoteRepository;
import org.craftercms.studio.api.v2.job.SiteJob;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.service.cluster.StudioClusterUtils;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.RemoteAddCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.URIish;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

public abstract class StudioClockTask implements SiteJob {

    private static final Logger logger = LoggerFactory.getLogger(StudioClockTask.class);

    private int executeEveryNCycles;
    protected int counter;
    protected StudioClusterUtils studioClusterUtils;
    protected StudioConfiguration studioConfiguration;
    protected ContentRepository contentRepository;
    protected SiteService siteService;
    protected ClusterDAO clusterDao;

    public StudioClockTask(int executeEveryNCycles,
                           StudioClusterUtils studioClusterUtils,
                           StudioConfiguration studioConfiguration,
                           ContentRepository contentRepository,
                           SiteService siteService, ClusterDAO clusterDao) {
        this.executeEveryNCycles = executeEveryNCycles;
        this.counter = executeEveryNCycles;
        this.studioClusterUtils = studioClusterUtils;
        this.studioConfiguration = studioConfiguration;
        this.contentRepository = contentRepository;
        this.siteService = siteService;
        this.clusterDao = clusterDao;
    }

    protected synchronized boolean checkCycleCounter() {
        return !(--counter > 0);
    }

    protected abstract void executeInternal(String site);
    protected abstract boolean lockSiteInternal(String site);
    protected abstract void unlockSiteInternal(String site);
    protected abstract Path buildRepoPath(String site);
    protected abstract List<String> getCreatedSites();

    @Override
    public final void execute(String site) {
        if (checkCycleCounter()) {
            if (lockSiteInternal(site)) {
                try {
                    executeInternal(site);
                    counter = executeEveryNCycles;
                } finally {
                    unlockSiteInternal(site);
                }
            }
        }
    }

    protected boolean checkIfSiteRepoExists(String siteId) {
        boolean toRet = false;
        if (getCreatedSites().contains(siteId)) {
            toRet = true;
        } else {
            String firstCommitId = contentRepository.getRepoFirstCommitId(siteId);
            if (!StringUtils.isEmpty(firstCommitId)) {
                toRet = true;
                getCreatedSites().add(siteId);
            } else {
                Repository repo = null;
                FileRepositoryBuilder builder = new FileRepositoryBuilder();
                try {
                    repo = builder
                            .setMustExist(true)
                            .setGitDir(buildRepoPath(siteId).resolve(GIT_ROOT).toFile())
                            .readEnvironment()
                            .findGitDir()
                            .build();
                } catch (IOException e) {
                    logger.info("Failed to open PUBLISHED repo for site " + siteId);
                }
                toRet = Objects.nonNull(repo) && repo.getObjectDatabase().exists();
            }
        }
        return toRet;
    }

    protected void removeRemote(Git git, String remoteName) throws GitAPIException {
        RemoteRemoveCommand remoteRemoveCommand = git.remoteRemove();
        remoteRemoveCommand.setRemoteName(remoteName);
        remoteRemoveCommand.call();

        List<Ref> resultRemoteBranches = git.branchList()
                .setListMode(ListBranchCommand.ListMode.REMOTE)
                .call();

        List<String> branchesToDelete = new ArrayList<String>();
        for (Ref remoteBranchRef : resultRemoteBranches) {
            if (remoteBranchRef.getName().startsWith(Constants.R_REMOTES + remoteName)) {
                branchesToDelete.add(remoteBranchRef.getName());
            }
        }
        if (CollectionUtils.isNotEmpty(branchesToDelete)) {
            DeleteBranchCommand delBranch = git.branchDelete();
            String[] array = new String[branchesToDelete.size()];
            delBranch.setBranchNames(branchesToDelete.toArray(array));
            delBranch.setForce(true);
            delBranch.call();
        }
    }
}
