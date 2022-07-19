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
package org.craftercms.studio.impl.v2.service.policy.validators;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.craftercms.studio.impl.v2.service.policy.PolicyValidator;
import org.craftercms.studio.model.policy.Action;
import org.craftercms.studio.model.policy.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.ConstructorProperties;
import java.nio.file.Path;

/**
 * Implementation of {@link PolicyValidator} for system restrictions
 *
 * @author joseross
 * @since 4.0.0
 */
public class SystemPolicyValidator implements PolicyValidator {

    private static final Logger logger = LoggerFactory.getLogger(SystemPolicyValidator.class);

    protected int filenameMaxSize;

    protected int fullPathMaxSize;

    @ConstructorProperties({"filenameMaxSize", "fullPathMaxSize"})
    public SystemPolicyValidator(int filenameMaxSize, int fullPathMaxSize) {
        this.filenameMaxSize = filenameMaxSize;
        this.fullPathMaxSize = fullPathMaxSize;
    }

    private void validateSystem(Action action, ValidationResult result) {
        // Check if the full path exceeds the limit
        String fullPath = action.getTarget();
        if (fullPath.length() >= fullPathMaxSize) {
            logger.error("Full path should not exceed '{}'", fullPathMaxSize);
            result.setAllowed(false);
            return;
        }

        // Check if any folder in the path exceeds the limit
        Path path = Path.of(fullPath);
        while (path != null && path.getFileName() != null) {
            String filename = path.getFileName().toString();
            if (filename.length() >= filenameMaxSize) {
                logger.error("Folder names in path should not exceed '{}'", filenameMaxSize);
                result.setAllowed(false);
            }
            path = path.getParent();
        }
    }

    @Override
    public void validate(HierarchicalConfiguration<?> permittedConfig, HierarchicalConfiguration<?> deniedConfig, Action action, ValidationResult result) {
        validateSystem(action, result);
    }

}
