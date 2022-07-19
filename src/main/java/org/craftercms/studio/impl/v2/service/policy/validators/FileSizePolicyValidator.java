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
import org.apache.commons.io.FilenameUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.impl.v2.service.policy.PolicyValidator;
import org.craftercms.studio.model.policy.Action;
import org.craftercms.studio.model.policy.ValidationResult;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Implementation of {@link PolicyValidator} for file size aware actions
 *
 * @author joseross
 * @since 4.0.0
 */
public class FileSizePolicyValidator implements PolicyValidator {

    private static final Logger logger = LoggerFactory.getLogger(FileSizePolicyValidator.class);

    public static final String CONFIG_KEY_MIN_SIZE = "minimum-file-size";
    public static final String CONFIG_KEY_MAX_SIZE = "maximum-file-size";

    private void validatePermitted(HierarchicalConfiguration<?> config, Action action, ValidationResult result) {
        if (isEmpty(FilenameUtils.getExtension(action.getTarget()))) {
            logger.debug("Target is a folder, skipping action");
            return;
        }

        if (config.containsKey(CONFIG_KEY_MIN_SIZE) && action.containsMetadata(Action.METADATA_FILE_SIZE)) {
            long minSize = config.getLong(CONFIG_KEY_MIN_SIZE);
            Number value = action.getMetadata(Action.METADATA_FILE_SIZE);
            if (value.longValue() < minSize) {
                logger.error("File size should be at least {0}", minSize);
                result.setAllowed(false);
                return;
            }
        } else {
            logger.debug("No min size found, skipping action");
        }


        if (config.containsKey(CONFIG_KEY_MAX_SIZE) && action.containsMetadata(Action.METADATA_FILE_SIZE)) {
            long maxSize = config.getLong(CONFIG_KEY_MAX_SIZE);
            Number value = action.getMetadata(Action.METADATA_FILE_SIZE);
            if (value.longValue() > maxSize) {
                logger.error("File size should be less than {0}", maxSize);
                result.setAllowed(false);
            }
        } else {
            logger.debug("No max size found, skipping action");
        }
    }

    @Override
    public void validate(HierarchicalConfiguration<?> permittedConfig, HierarchicalConfiguration<?> deniedConfig, Action action, ValidationResult result) {
        if (permittedConfig != null) {
            validatePermitted(permittedConfig, action, result);
        }
    }

}
