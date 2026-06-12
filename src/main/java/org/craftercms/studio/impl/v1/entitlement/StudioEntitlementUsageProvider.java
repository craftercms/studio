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

package org.craftercms.studio.impl.v1.entitlement;

import org.craftercms.commons.entitlements.exception.UnsupportedEntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.model.Module;
import org.craftercms.commons.entitlements.usage.EntitlementUsageProvider;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.item.ItemService;
import org.craftercms.studio.api.v2.service.security.UserService;
import org.craftercms.studio.api.v2.service.site.SitesService;

import java.util.Arrays;
import java.util.List;

import static org.craftercms.commons.entitlements.model.Module.STUDIO;

/**
 * Implementation of {@link EntitlementUsageProvider} for Crafter Studio module.
 *
 * @author joseross
 */
public class StudioEntitlementUsageProvider implements EntitlementUsageProvider {

	/**
	 * Current instance of {@link SitesService}.
	 */
	protected SitesService siteService;
	protected UserService userService;
	protected ItemService itemService;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Module getModule() {
		return STUDIO;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<EntitlementType> getSupportedEntitlements() {
		return Arrays.asList(EntitlementType.SITE, EntitlementType.USER, EntitlementType.ITEM);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int doGetEntitlementUsage(final EntitlementType type) throws UnsupportedEntitlementException,
		ServiceLayerException {
		return switch (type) {
			case SITE -> countSites();
			case USER -> countUsers();
			case ITEM -> countItems();
			default -> throw new UnsupportedEntitlementException(STUDIO, type);
		};
	}

	protected int countSites() {
		return siteService.getAllSites().size();
	}

	protected int countUsers() throws ServiceLayerException {
		return userService.getAllUsersTotal(null, false);
	}

	protected int countItems() {
		return itemService.countAllContentItems();
	}

	public void setSiteService(SitesService siteService) {
		this.siteService = siteService;
	}

	public void setUserService(UserService userService) {
		this.userService = userService;
	}

	public void setItemService(ItemService itemService) {
		this.itemService = itemService;
	}
}
