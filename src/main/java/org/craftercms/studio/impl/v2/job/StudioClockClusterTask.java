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
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.repository.ContentRepository;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.RemoteRemoveCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryConstants.GIT_ROOT;

public abstract class StudioClockClusterTask extends StudioClockTask {

    private static final Logger logger = LoggerFactory.getLogger(StudioClockClusterTask.class);

    protected ContentRepository contentRepository;


    protected abstract Path buildRepoPath(String site);
    protected abstract List<String> getCreatedSites();

    public StudioClockClusterTask(int executeEveryNCycles,
                                  int offset,
                                  StudioConfiguration studioConfiguration,
                                  SiteService siteService,
                                  ContentRepository contentRepository) {
        super(executeEveryNCycles, offset, studioConfiguration, siteService);
        this.contentRepository = contentRepository;
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
