/*
 * Copyright (C) 2007-2026 Crafter Software Corporation. All Rights Reserved.
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
import org.apache.ibatis.session.SqlSessionFactory;
import org.craftercms.commons.entitlements.validator.EntitlementValidator;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.dal.SiteFeedMapper;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.UserDAO;
import org.craftercms.studio.api.v2.deployment.Deployer;
import org.craftercms.studio.api.v2.repository.GitContentRepository;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.config.ConfigurationService;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.security.GroupService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.site.SitesService;
import org.craftercms.studio.api.v2.upgrade.StudioUpgradeManager;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@Valid
	public boolean exists(@ValidateStringParam String site) {
		return siteFeedMapper.exists(site) > 0;
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
