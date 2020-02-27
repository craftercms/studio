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

package org.craftercms.studio.impl.v2.upgrade.pipeline;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.constant.GitRepositories;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.exception.UpgradeException;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v1.repository.git.GitContentRepositoryHelper;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.springframework.beans.factory.annotation.Required;

/**
 * Implementation of {@link org.craftercms.studio.api.v2.upgrade.UpgradePipeline} that handles a git repository
 * to work on a temporary branch for upgrades.
 * @author joseross
 */
public class SiteRepositoryUpgradePipelineImpl extends DefaultUpgradePipelineImpl {

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

    protected StudioConfiguration studioConfiguration;
    protected ServicesConfig servicesConfig;
    protected SecurityService securityService;
    protected UserServiceInternal userServiceInternal;
    protected SiteService siteService;

    protected void createTemporaryBranch(String site, Git git) throws GitAPIException {
        List<Ref> branches = git.branchList().call();
        if(branches.stream().anyMatch(b -> b.getName().contains(siteUpgradeBranch))) {
            logger.debug("Temporary branch already exists, changes will be discarded");
            deleteTemporaryBranch(git);
        }
        logger.debug("Creating temporary branch {0} for site {1}", siteUpgradeBranch, site);
        git.branchCreate().setName(siteUpgradeBranch).call();
    }

    protected void checkoutBranch(String branch, Git git) throws GitAPIException {
        logger.debug("Checking out {0} branch", branch);
        git.checkout().setName(branch).call();
    }

    protected void mergeTemporaryBranch(Repository repository, Git git) throws IOException, GitAPIException {
        logger.debug("Merging changes from upgrade branch");
        git.merge()
            .include(repository.findRef(siteUpgradeBranch))
            .setMessage(commitMessage)
            .setCommit(true)
            .call();
    }

    protected void deleteTemporaryBranch(Git git) throws GitAPIException {
        logger.debug("Removing temporary branch");
        git.branchDelete().setBranchNames(siteUpgradeBranch).call();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(final String site) throws UpgradeException {
        GitContentRepositoryHelper helper = new GitContentRepositoryHelper(studioConfiguration, servicesConfig,
                userServiceInternal, securityService);

        Repository repository = helper.getRepository(site, GitRepositories.SANDBOX);
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
                    super.execute(site);
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

    }

    @Required
    public void setSiteSandboxBranch(final String siteSandboxBranch) {
        this.siteSandboxBranch = siteSandboxBranch;
    }

    @Required
    public void setSiteUpgradeBranch(final String siteUpgradeBranch) {
        this.siteUpgradeBranch = siteUpgradeBranch;
    }

    @Required
    public void setCommitMessage(final String commitMessage) {
        this.commitMessage = commitMessage;
    }


    @Required
    public void setStudioConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    @Required
    public void setServicesConfig(final ServicesConfig servicesConfig) {
        this.servicesConfig = servicesConfig;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }
}
