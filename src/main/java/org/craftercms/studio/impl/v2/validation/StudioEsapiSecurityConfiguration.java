/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.validation;

import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.owasp.esapi.SecurityConfiguration;
import org.owasp.esapi.reference.DefaultSecurityConfiguration;

import java.util.regex.Pattern;

import static java.lang.String.format;

/**
 * ESAPI {@link DefaultSecurityConfiguration} extension to allow overriding
 * validation regex patterns.
 */
public class StudioEsapiSecurityConfiguration extends DefaultSecurityConfiguration {

    public static final String STUDIO_VALIDATION_REGEX_OVERRIDE_FORMAT = "studio.validation.regex.%s";

    private static volatile SecurityConfiguration singletonInstance = null;

    private final StudioConfiguration studioConfiguration;

    public StudioEsapiSecurityConfiguration(final StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }

    static void setInstance(final StudioEsapiSecurityConfiguration instance) {
        singletonInstance = instance;
    }

    public static SecurityConfiguration getInstance() {
        return singletonInstance;
    }

    /**
     * Returns a single pattern based upon key.
     * It first tries to get the value from studio configuration, using the key
     * prefix 'studio.validation.regex.%s'. If value is not found in studio configuration,
     * it invokes the super class (DefaultSecurityConfiguration) method to get default configured
     * value.
     *
     * @param key validation pattern name you'd like
     * @return if key exists, the associated validation pattern, null otherwise
     */
    @Override
    public Pattern getValidationPattern(String key) {
        String studioConfigurationKey = format(STUDIO_VALIDATION_REGEX_OVERRIDE_FORMAT, key);
        String patternOverride = studioConfiguration.getProperty(studioConfigurationKey);
        if (patternOverride == null) {
            return super.getValidationPattern(key);
        }
        return Pattern.compile(patternOverride);
    }
}
