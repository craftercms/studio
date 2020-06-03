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

package org.craftercms.studio.impl.v2.service.security;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.service.security.EncryptionService;
import org.craftercms.studio.api.v2.service.security.internal.EncryptionServiceInternal;

/**
 * @author joseross
 */
public class EncryptionServiceImpl implements EncryptionService {

    protected EncryptionServiceInternal encryptionServiceInternal;

    public EncryptionServiceImpl(final EncryptionServiceInternal encryptionServiceInternal) {
        this.encryptionServiceInternal = encryptionServiceInternal;
    }

    @Override
    @HasPermission(type = DefaultPermission.class, action = "encryption_tool")
    public String encrypt(@ValidateStringParam(name = "text") final String text) throws ServiceLayerException {
        return encryptionServiceInternal.encrypt(text);
    }

}
