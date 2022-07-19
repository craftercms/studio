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
import org.craftercms.studio.impl.v2.service.policy.PolicyValidator;
import org.craftercms.studio.model.policy.Action;

import org.craftercms.studio.model.policy.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;


/**
 * Implementation of {@link PolicyValidator} for content-type aware actions
 *
 * @author joseross
 * @since 4.0.0
 */
public class ContentTypePolicyValidator implements PolicyValidator {

    private static final Logger logger = LoggerFactory.getLogger(ContentTypePolicyValidator.class);

    public static final String CONFIG_KEY_CONTENT_TYPES = "content-types";

    @Override
    public void validate(HierarchicalConfiguration<?> permittedConfig, HierarchicalConfiguration<?> deniedConfig, Action action, ValidationResult result) {
        if (isEmpty(FilenameUtils.getExtension(action.getTarget()))) {
            logger.debug("Skipping folder '{}'", action.getTarget());
            return;
        }
        String contentType = action.getMetadata(Action.METADATA_CONTENT_TYPE);

        if (permittedConfig != null) {
            validatePermitted(permittedConfig, contentType, result);
        }

        if (deniedConfig != null) {
            validateDenied(deniedConfig, contentType, result);
        }
    }

    private void validatePermitted(HierarchicalConfiguration<?> permittedConfig, String contentType, ValidationResult result) {
        if (!permittedConfig.containsKey(CONFIG_KEY_CONTENT_TYPES)) {
            logger.debug("Skipping action because there are no content-type permitted restrictions");
            return;
        }

        if (isEmpty(contentType)) {
            logger.debug("Skipping action because there is no Content-Type from action metadata");
            result.setAllowed(false);
            return;
        }

        var allowedTypes = permittedConfig.getList(String.class, CONFIG_KEY_CONTENT_TYPES);

        if (!allowedTypes.contains(contentType)) {
            logger.error("Content-Type '{}' not allowed", contentType);
            result.setAllowed(false);
        }
    }

    private void validateDenied(HierarchicalConfiguration<?> deniedConfig, String contentType, ValidationResult result) {
        if (!deniedConfig.containsKey(CONFIG_KEY_CONTENT_TYPES)) {
            logger.debug("Skipping action because there are no content-types denied restrictions");
            return;
        }

        if (isEmpty(contentType)) {
            logger.debug("Skipping action because there is no Content-Type from action metadata");
            return;
        }

        var deniedTypes = deniedConfig.getList(String.class, CONFIG_KEY_CONTENT_TYPES);
        if (deniedTypes.contains(contentType)) {
            logger.error("Content-Type '{}' not allowed", contentType);
            result.setAllowed(false);
        }
    }
}
