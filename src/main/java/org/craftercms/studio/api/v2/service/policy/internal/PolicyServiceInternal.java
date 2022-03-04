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
package org.craftercms.studio.api.v2.service.policy.internal;

import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.model.policy.Action;
import org.craftercms.studio.model.policy.ValidationResult;

import java.util.List;

/**
 * Validates changes on content
 *
 * @author joseross
 * @since 4.0.0
 */
public interface PolicyServiceInternal {

    /**
     * Performs the validation of one or more actions for a given site
     *
     * @param siteId the id of the site
     * @param actions the list of actions to validate
     * @return the validation results
     * @throws ConfigurationException if there any error parsing the configuration
     */
    List<ValidationResult> validate(String siteId, List<Action> actions)
            throws ConfigurationException;

}
