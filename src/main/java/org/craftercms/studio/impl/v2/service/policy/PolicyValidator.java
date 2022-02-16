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
package org.craftercms.studio.impl.v2.service.policy;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.api.v2.exception.validation.ValidationException;
import org.craftercms.studio.model.policy.Action;

/**
 * Validates actions against the given configuration
 *
 * @author joseross
 * @since 4.0.0
 */
public interface PolicyValidator {

    /**
     * Performs the validation of an action
     *
     * @param config the policy configuration
     * @param action the action to validate
     * @throws ValidationException if the validation fails
     */
    void validate(HierarchicalConfiguration<?> config, Action action) throws ValidationException;

}
