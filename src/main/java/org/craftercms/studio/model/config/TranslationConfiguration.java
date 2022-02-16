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
package org.craftercms.studio.model.config;

import java.util.List;

/**
 * Holds the configuration for translations
 *
 * @author joseross
 * @since 4.0.0
 */
public class TranslationConfiguration {

    /**
     * The code of the default locale for all new content
     */
    protected String defaultLocaleCode;

    /**
     * The list of codes for the supported locales
     */
    protected List<String> localeCodes;

    public String getDefaultLocaleCode() {
        return defaultLocaleCode;
    }

    public void setDefaultLocaleCode(String defaultLocaleCode) {
        this.defaultLocaleCode = defaultLocaleCode;
    }

    public List<String> getLocaleCodes() {
        return localeCodes;
    }

    public void setLocaleCodes(List<String> localeCodes) {
        this.localeCodes = localeCodes;
    }

}
