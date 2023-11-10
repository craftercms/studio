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

package org.craftercms.studio.controller.rest;

import org.craftercms.commons.validation.ValidationException;
import org.craftercms.commons.validation.ValidationResult;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.model.rest.AddGroupMembers;
import org.craftercms.studio.model.rest.EnableUsers;
import org.springframework.validation.Validator;

import java.util.*;

import static org.springframework.util.CollectionUtils.isEmpty;

/**
 * Utility class to perform parameter validations.
 *
 * @author joseross
 */
public abstract class ValidationUtils {

    /**
     * Validates that at least one of the lists is not null and is not empty.
     *
     * @param lists lists to validate
     * @throws InvalidParametersException if all lists are null or empty
     */
    public static void validateAnyListNonEmpty(List<?>... lists) throws InvalidParametersException {
        long validLists = Arrays.stream(lists).filter(Objects::nonNull).filter(list -> !list.isEmpty()).count();
        if (validLists == 0) {
            throw new InvalidParametersException("All parameters are empty or null");
        }
    }

    /**
     * Validates a {@link AddGroupMembers} object.
     *
     * @param addGroupMembers object to validate
     * @throws InvalidParametersException if the object is invalid
     */
    public static void validateAddGroupMembers(AddGroupMembers addGroupMembers) throws InvalidParametersException {
        if (isEmpty(addGroupMembers.getIds()) &&
                isEmpty(addGroupMembers.getUsernames())) {
            throw new InvalidParametersException("Both 'userIds' and 'usernames' are empty");
        }
    }

    /**
     * Validates a {@link EnableUsers} object.
     *
     * @param enableUsers object to validate
     * @throws InvalidParametersException if the object is invalid
     */
    public static void validateEnableUsers(EnableUsers enableUsers) throws InvalidParametersException {
        if (isEmpty(enableUsers.getIds()) &&
                isEmpty(enableUsers.getUsernames())) {
            throw new InvalidParametersException("Both 'userIds' and 'usernames' are empty");
        }
    }

    /**
     * Validates a value against a given validator and throws a ValidationException if validation fails
     *
     * @param validator {@link Validator}
     * @param value     value to be validated
     * @param key       validation error key to be used for the value
     * @throws ValidationException if validation fails
     */
    public static void validateValue(final Validator validator, final Object value, final String key) throws ValidationException {
        ValidationResult validationResult = org.craftercms.commons.validation.ValidationUtils.validateValue(validator, value, key);
        if (validationResult.hasErrors()) {
            throw new ValidationException(validationResult);
        }
    }

    public static void validateValue(final Validator validator, final Object value, final String key, Map<String, String> errors) {
        ValidationResult filenameResult = org.craftercms.commons.validation.ValidationUtils.validateValue(validator, value, key);
        if (filenameResult.hasErrors()) {
            errors.putAll(filenameResult.getErrors());
        }
    }

    private static void throwExceptionIfErrorsFound(Map<String, String> errors) throws ValidationException {
        if (!errors.isEmpty()) {
            ValidationResult result = new ValidationResult();
            errors.forEach(result::addError);
            throw new ValidationException(result);
        }
    }

}
