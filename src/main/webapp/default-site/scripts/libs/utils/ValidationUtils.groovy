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
package scripts.libs.utils

import org.craftercms.core.exception.StoreException
import org.springframework.validation.Errors

import static java.lang.String.format

//import org.craftercms.commons.validation.ValidationException
//import org.craftercms.commons.validation.ValidationResult
//import org.craftercms.commons.validation.validators.Validator

class ValidationUtils {

    static void validateInput(String input, Validator<String> validator) {
        Errors errors = org.craftercms.commons.validation.ValidationUtils.validateValue(validator, input);
        if (errors.hasErrors()) {
//            throw new StoreException("Validation of path " + path + " failed. Errors: " + errors);
//            throw new ValidationException(format("Validation of path '%s' failed. Errors: %s", input, errors));
        }
//        def validationResult = new ValidationResult()
//        def valid = validator.validate(input, validationResult)
//
//        if (!valid) {
//            throw new ValidationException(validationResult)
//        }
    }

}
