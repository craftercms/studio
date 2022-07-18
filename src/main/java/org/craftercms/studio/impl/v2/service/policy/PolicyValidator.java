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
import org.craftercms.studio.model.policy.Action;
import org.craftercms.studio.model.policy.ValidationResult;

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
     * @param permittedConfig the permitted policy configuration
     * @param deniedConfig the denied policy configuration
     * @param action the action to validate
     * @param result result of the validation. Implementing methods should update status accordingly
     */
    void validate(HierarchicalConfiguration<?> permittedConfig, HierarchicalConfiguration<?> deniedConfig, Action action, ValidationResult result);
}
