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
package org.craftercms.studio.impl.v2.service.translation;

import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.to.TranslationConfigTo;
import org.craftercms.studio.api.v2.service.translation.TranslationService;
import org.craftercms.studio.api.v2.service.translation.internal.TranslationServiceInternal;

/**
 * Default implementation of {@link TranslationService}
 *
 * @author joseross
 * @since 3.2.0
 */
public class TranslationServiceImpl implements TranslationService {

    /**
     * The internal translation service
     */
    protected TranslationServiceInternal translationServiceInternal;

    public TranslationServiceImpl(TranslationServiceInternal translationServiceInternal) {
        this.translationServiceInternal = translationServiceInternal;
    }

    @Override
    @ValidateParams
    public TranslationConfigTo getConfig(@ValidateStringParam(name = "siteId") String siteId) {
        return translationServiceInternal.getConfig(siteId);
    }

}
