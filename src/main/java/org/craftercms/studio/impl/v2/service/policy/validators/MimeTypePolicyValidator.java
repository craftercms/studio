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
import org.craftercms.studio.api.v2.exception.validation.ValidationException;
import org.craftercms.studio.api.v2.utils.StudioUtils;
import org.craftercms.studio.impl.v2.service.policy.PolicyValidator;
import org.craftercms.studio.model.policy.Action;
import org.springframework.util.MimeType;

import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Implementation of {@link PolicyValidator} for MIME type aware actions
 *
 * @author joseross
 * @since 4.0.0
 */
public class MimeTypePolicyValidator implements PolicyValidator {

    private static final Logger logger = LoggerFactory.getLogger(MimeTypePolicyValidator.class);

    public static final String CONFIG_KEY_MIME_TYPES = "mime-types";

    @Override
    public void validate(HierarchicalConfiguration<?> config, Action action) throws ValidationException {
        if (config.containsKey(CONFIG_KEY_MIME_TYPES) && !isEmpty(FilenameUtils.getExtension(action.getTarget()))) {
            var actionMimeType = MimeType.valueOf(StudioUtils.getMimeType(action.getTarget()));
            if (config.getList(String.class, CONFIG_KEY_MIME_TYPES).stream()
                    .map(MimeType::valueOf)
                    .noneMatch(actionMimeType::isCompatibleWith)) {
                throw new ValidationException("MIME type " + actionMimeType + " not allowed");
            }
        } else {
            logger.debug("No MIME type restrictions found, skipping action");
        }
    }

}
