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

package org.craftercms.studio.impl.v1.entitlement;

import java.util.Arrays;
import java.util.List;

import org.craftercms.commons.entitlements.exception.UnsupportedEntitlementException;
import org.craftercms.commons.entitlements.model.EntitlementType;
import org.craftercms.commons.entitlements.model.Module;
import org.craftercms.commons.entitlements.usage.EntitlementUsageProvider;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.service.item.internal.ItemServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;

import static org.craftercms.commons.entitlements.model.Module.STUDIO;

/**
 * Implementation of {@link EntitlementUsageProvider} for Crafter Studio module.
 *
 * @author joseross
 */
public class StudioEntitlementUsageProvider implements EntitlementUsageProvider {

    /**
     * Current instance of {@link SiteService}.
     */
    protected SiteService siteService;
    protected UserServiceInternal userServiceInternal;
    protected ItemServiceInternal itemServiceInternal;

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
        switch (type) {
            case SITE:
                return countSites();
            case USER:
                return countUsers();
            case ITEM:
                return countItems();
            default:
                throw new UnsupportedEntitlementException(STUDIO, type);
        }
    }

    protected int countSites() {
        return siteService.countSites();
    }

    protected int countUsers() throws ServiceLayerException {
        return userServiceInternal.getAllUsersTotal(null);
    }

    protected int countItems() {
        return itemServiceInternal.countAllContentItems();
    }

    public SiteService getSiteService() {
        return siteService;
    }

    public void setSiteService(SiteService siteService) {
        this.siteService = siteService;
    }

    public UserServiceInternal getUserServiceInternal() {
        return userServiceInternal;
    }

    public void setUserServiceInternal(UserServiceInternal userServiceInternal) {
        this.userServiceInternal = userServiceInternal;
    }

    public ItemServiceInternal getItemServiceInternal() {
        return itemServiceInternal;
    }

    public void setItemServiceInternal(ItemServiceInternal itemServiceInternal) {
        this.itemServiceInternal = itemServiceInternal;
    }
}
