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
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.impl.v2.service.policy.PolicyValidator;
import org.craftercms.studio.model.policy.Action;
import org.craftercms.studio.model.policy.ValidationResult;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Implementation of {@link PolicyValidator} for path aware actions
 *
 * @author joseross
 * @since 4.0.0
 */
public class PathPolicyValidator implements PolicyValidator {

    private static final Logger logger = LoggerFactory.getLogger(PathPolicyValidator.class);

    public static final String CONFIG_KEY_SOURCE_REGEX = "path.source-regex";
    public static final String CONFIG_KEY_TARGET_REGEX = "path.target-regex";
    public static final String CONFIG_KEY_CASE_TRANSFORM = "path.target-regex[@caseTransform]";

    private void validatePermitted(HierarchicalConfiguration<?> config, Action action, ValidationResult result) {
        if (!config.containsKey(CONFIG_KEY_SOURCE_REGEX)) {
            logger.debug("No path restrictions found, skipping action");
            return;
        }
        String target = result.getModifiedValue() != null ? result.getModifiedValue() : action.getTarget();
            var sourceRegex = config.getString(CONFIG_KEY_SOURCE_REGEX);
            if (!target.matches(sourceRegex)) {
                String modifiedValue = null;
                var targetRegex = config.getString(CONFIG_KEY_TARGET_REGEX);
                if (targetRegex != null) {
                    modifiedValue = target.replaceAll(sourceRegex, targetRegex);

                    var caseTransform = config.getString(CONFIG_KEY_CASE_TRANSFORM);
                    if (isNotEmpty(caseTransform)) {
                        switch (caseTransform.toLowerCase()) {
                            case "uppercase":
                                modifiedValue = modifiedValue.toUpperCase();
                                break;
                            case "lowercase":
                                modifiedValue = modifiedValue.toLowerCase();
                                break;
                            default:
                                logger.warn("Unsupported case transformation: {0}", caseTransform);
                        }
                    }

                    // special case when creating the folder used in the configuration
                    if (target.equals(modifiedValue)) {
                        return;
                    }
                }
                result.setAllowed(modifiedValue != null);
                result.setModifiedValue(modifiedValue);
                if (!result.isAllowed()) {
                    logger.error("Path {0} is invalid", action.getTarget());
                }
        }
    }

    @Override
    public void validate(HierarchicalConfiguration<?> permittedConfig, HierarchicalConfiguration<?> deniedConfig, Action action, ValidationResult validationResult) {
        if (permittedConfig != null) {
            validatePermitted(permittedConfig, action, validationResult);
        }
    }

}
