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

package org.craftercms.studio.controller.rest.v2;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.model.rest.AddGroupMembers;
import org.craftercms.studio.model.rest.EnableUsers;
import org.springframework.util.CollectionUtils;

import static java.lang.String.format;

/**
 * Utility class to perform parameter validations.
 * @author joseross
 */
public abstract class ValidationUtils {

    public static final String EMAIL_PATTERN = "([\\w\\d._\\-#])+@([\\w\\d._\\-#]+[.][\\w\\d._\\-#]+)+";

    /**
     * Validates that at least one of the lists is not null and is not empty.
     * @param lists lists to validate
     * @throws InvalidParametersException if all lists are null or empty
     */
    public static void validateAnyListNonEmpty(List<?>... lists) throws InvalidParametersException {
        long validLists = Arrays.stream(lists).filter(Objects::nonNull).filter(list -> !list.isEmpty()).count();
        if(validLists == 0) {
            throw new InvalidParametersException("All parameters are empty or null");
        }
    }

    /**
     * Validates a {@link AddGroupMembers} object.
     * @param addGroupMembers object to validate
     * @throws InvalidParametersException if the object is invalid
     */
    public static void validateAddGroupMembers(AddGroupMembers addGroupMembers) throws InvalidParametersException {
        if(CollectionUtils.isEmpty(addGroupMembers.getIds()) &&
            CollectionUtils.isEmpty(addGroupMembers.getUsernames())) {
            throw new InvalidParametersException("Both 'userIds' and 'usernames' are empty");
        }
    }

    /**
     * Validates a {@link EnableUsers} object.
     * @param enableUsers object to validate
     * @throws InvalidParametersException if the object is invalid
     */
    public static void validateEnableUsers(EnableUsers enableUsers) throws InvalidParametersException {
        if(CollectionUtils.isEmpty(enableUsers.getIds()) &&
            CollectionUtils.isEmpty(enableUsers.getUsernames())) {
            throw new InvalidParametersException("Both 'userIds' and 'usernames' are empty");
        }
    }

    /**
     * Validate email is valid.
     * @param email the email string to validate
     * @throws InvalidParametersException if the string is invalid email
     */
    public static void validateEmail(String email) throws InvalidParametersException {
        if (StringUtils.isEmpty(email) || !email.matches(EMAIL_PATTERN)) {
            throw new InvalidParametersException(format("Parameters 'email' has invalid value '%s'", email));
        }
    }

}
