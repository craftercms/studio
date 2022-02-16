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
import org.craftercms.studio.api.v2.exception.validation.FilenameTooLongException;
import org.craftercms.studio.api.v2.exception.validation.PathTooLongException;
import org.craftercms.studio.api.v2.exception.validation.ValidationException;
import org.craftercms.studio.impl.v2.service.policy.PolicyValidator;
import org.craftercms.studio.model.policy.Action;

import java.beans.ConstructorProperties;
import java.nio.file.Path;

/**
 * Implementation of {@link PolicyValidator} for system restrictions
 *
 * @author joseross
 * @since 4.0.0
 */
public class SystemPolicyValidator implements PolicyValidator {

    protected int filenameMaxSize;

    protected int fullPathMaxSize;

    @ConstructorProperties({"filenameMaxSize", "fullPathMaxSize"})
    public SystemPolicyValidator(int filenameMaxSize, int fullPathMaxSize) {
        this.filenameMaxSize = filenameMaxSize;
        this.fullPathMaxSize = fullPathMaxSize;
    }

    @Override
    public void validate(HierarchicalConfiguration<?> config, Action action) throws ValidationException {
        // Check if the full path exceeds the limit
        String fullPath = action.getTarget();
        if (fullPath.length() >= fullPathMaxSize) {
            throw new PathTooLongException(fullPath);
        }

        // Check if any folder in the path exceeds the limit
        Path path = Path.of(fullPath);
        while (path != null && path.getFileName() != null) {
            String filename = path.getFileName().toString();
            if (filename.length() >= filenameMaxSize) {
                throw new FilenameTooLongException(filename);
            }
            path = path.getParent();
        }
    }

}
