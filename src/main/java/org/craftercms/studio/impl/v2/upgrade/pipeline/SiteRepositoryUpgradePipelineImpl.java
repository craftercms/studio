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

package org.craftercms.studio.impl.v2.upgrade.pipeline;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.upgrade.UpgradeOperation;
import org.craftercms.commons.upgrade.exception.UpgradeException;
import org.craftercms.commons.upgrade.impl.UpgradeContext;
import org.craftercms.commons.upgrade.impl.pipeline.DefaultUpgradePipelineImpl;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.repository.RetryingRepositoryOperationFacade;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.DeleteBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;

import static org.craftercms.studio.api.v1.constant.StudioConstants.PATTERN_SITE;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SITE_SANDBOX_REPOSITORY_GIT_LOCK;

/**
 * Implementation of {@link org.craftercms.commons.upgrade.UpgradePipeline} that handles a git repository
 * to work on a temporary branch for upgrades.
 * @author joseross
 */
public class SiteRepositoryUpgradePipelineImpl extends DefaultUpgradePipelineImpl<String> {

    private static final Logger logger = LoggerFactory.getLogger(SiteRepositoryUpgradePipelineImpl.class);

    /**
     * The name of the sandbox branch.
     */
    protected String siteSandboxBranch;

    /**
     * The name of the temporary branch used for upgrades.
     */
    protected String siteUpgradeBranch;

    /**
     * Message for the merge commit after upgrading.
     */
    protected String commitMessage;
    protected SiteService siteService;
    protected GeneralLockService generalLockService;
    protected GitRepositoryHelper gitRepositoryHelper;
    protected RetryingRepositoryOperationFacade retryingRepositoryOperationFacade;

    public SiteRepositoryUpgradePipelineImpl(String name, List<UpgradeOperation<String>> upgradeOperations) {
        super(name, upgradeOperations);
    }

    protected void createTemporaryBranch(String site, Git git) throws GitAPIException {
        ListBranchCommand listBranchCommand = git.branchList();
        List<Ref> branches = retryingRepositoryOperationFacade.call(listBranchCommand);
        if(branches.stream().anyMatch(b -> b.getName().contains(siteUpgradeBranch))) {
            logger.debug("Temporary branch already exists, changes will be discarded");
            deleteTemporaryBranch(git);
        }
        logger.debug("Creating temporary branch {0} for site {1}", siteUpgradeBranch, site);
        CreateBranchCommand createBranchCommand = git.branchCreate().setName(siteUpgradeBranch);
        retryingRepositoryOperationFacade.call(createBranchCommand);
    }

    protected void checkoutBranch(String branch, Git git) throws GitAPIException {
        logger.debug("Checking out {0} branch", branch);
        CheckoutCommand checkoutCommand = git.checkout().setName(branch);
        retryingRepositoryOperationFacade.call(checkoutCommand);
    }

    protected void mergeTemporaryBranch(Repository repository, Git git) throws IOException, GitAPIException {
        logger.debug("Merging changes from upgrade branch");
        MergeCommand mergeCommand = git.merge()
            .include(repository.findRef(siteUpgradeBranch))
            .setMessage(commitMessage)
            .setCommit(true);
        retryingRepositoryOperationFacade.call(mergeCommand);
    }

    protected void deleteTemporaryBranch(Git git) throws GitAPIException {
        logger.debug("Removing temporary branch");
        DeleteBranchCommand deleteBranchCommand = git.branchDelete().setBranchNames(siteUpgradeBranch);
        retryingRepositoryOperationFacade.call(deleteBranchCommand);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final UpgradeContext<String> context) throws UpgradeException {
        var site = context.getTarget();
        String gitLockKey = SITE_SANDBOX_REPOSITORY_GIT_LOCK.replaceAll(PATTERN_SITE, site);
        generalLockService.lock(gitLockKey);
        try {
            Repository repository = gitRepositoryHelper.getRepository(site, GitRepositories.SANDBOX);
            String sandboxBranch = siteSandboxBranch;
            if (repository != null) {
                Git git = new Git(repository);
                try {
                    if (!isEmpty()) {
                        SiteFeed siteFeed = siteService.getSite(site);
                        if (!StringUtils.isEmpty(siteFeed.getSandboxBranch())) {
                            sandboxBranch = siteFeed.getSandboxBranch();
                        }
                        createTemporaryBranch(site, git);
                        checkoutBranch(siteUpgradeBranch, git);
                        super.execute(context);
                        checkoutBranch(sandboxBranch, git);
                        mergeTemporaryBranch(repository, git);
                        deleteTemporaryBranch(git);

                    }
                } catch (GitAPIException | IOException | SiteNotFoundException e) {
                    throw new UpgradeException("Error branching or merging upgrade branch for site " + site, e);
                } finally {
                    if (!isEmpty()) {
                        try {
                            checkoutBranch(sandboxBranch, git);
                        } catch (GitAPIException e) {
                            logger.error("Error cleaning up repo for site " + site, e);
                        }
                    }
                    git.close();
                }
            }
        } finally {
            generalLockService.unlock(gitLockKey);
        }
    }

    public void setSiteSandboxBranch(final String siteSandboxBranch) {
        this.siteSandboxBranch = siteSandboxBranch;
    }

    public void setSiteUpgradeBranch(final String siteUpgradeBranch) {
        this.siteUpgradeBranch = siteUpgradeBranch;
    }

    public void setCommitMessage(final String commitMessage) {
        this.commitMessage = commitMessage;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public GeneralLockService getGeneralLockService() {
        return generalLockService;
    }

    public void setGeneralLockService(GeneralLockService generalLockService) {
        this.generalLockService = generalLockService;
    }

    public GitRepositoryHelper getGitRepositoryHelper() {
        return gitRepositoryHelper;
    }

    public void setGitRepositoryHelper(GitRepositoryHelper gitRepositoryHelper) {
        this.gitRepositoryHelper = gitRepositoryHelper;
    }

    public RetryingRepositoryOperationFacade getRetryingRepositoryOperationFacade() {
        return retryingRepositoryOperationFacade;
    }

    public void setRetryingRepositoryOperationFacade(RetryingRepositoryOperationFacade retryingRepositoryOperationFacade) {
        this.retryingRepositoryOperationFacade = retryingRepositoryOperationFacade;
    }
}
