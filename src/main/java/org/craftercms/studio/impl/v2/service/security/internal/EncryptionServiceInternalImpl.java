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

package org.craftercms.studio.impl.v2.service.security.internal;

import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.annotation.IsActionAllowed;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.security.internal.EncryptionServiceInternal;

import static org.craftercms.studio.api.v2.security.AvailableActions.WRITE_CONFIGURATION_CONST_LONG;

/**
 * @author joseross
 */
public class EncryptionServiceInternalImpl implements EncryptionServiceInternal {

    protected long delay;

    protected int maxLength;

    protected TextEncryptor textEncryptor;

    public EncryptionServiceInternalImpl(final long delay, final int maxLength, final TextEncryptor textEncryptor) {
        this.delay = delay;
        this.maxLength = maxLength;
        this.textEncryptor = textEncryptor;
    }

    @Override
    @IsActionAllowed(allowedActionsMask = WRITE_CONFIGURATION_CONST_LONG)
    public String encrypt(final String text) throws ServiceLayerException {
        if (StringUtils.isEmpty(text) || text.length() > maxLength) {
            throw new InvalidParametersException("The provided text is invalid");
        }
        try {
            Thread.sleep(delay * 1000);
            return textEncryptor.encrypt(text);
        } catch (CryptoException | InterruptedException e) {
            throw new ServiceLayerException("Error encrypting text", e);
        }
    }

}
