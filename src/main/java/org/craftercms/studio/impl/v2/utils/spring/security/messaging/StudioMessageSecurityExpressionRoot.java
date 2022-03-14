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
package org.craftercms.studio.impl.v2.utils.spring.security.messaging;

import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v2.dal.Group;
import org.craftercms.studio.api.v2.service.security.internal.GroupServiceInternal;
import org.craftercms.studio.api.v2.service.security.internal.UserServiceInternal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.expression.MessageSecurityExpressionRoot;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.function.Predicate.isEqual;
import static org.craftercms.studio.api.v1.constant.StudioConstants.SYSTEM_ADMIN_GROUP;

/**
 * Extension of {@link MessageSecurityExpressionRoot} that adds Studio specific security expressions.
 *
 * @author joseross
 * @since 4.0.0
 */
public class StudioMessageSecurityExpressionRoot extends MessageSecurityExpressionRoot {

    private static final Logger logger = LoggerFactory.getLogger(StudioMessageSecurityExpressionRoot.class);

    public static final Pattern SITE_DESTINATION_PATTERN = Pattern.compile("/topic/studio/([^/]+).*");

    protected UserServiceInternal userServiceInternal;

    protected GroupServiceInternal groupServiceInternal;

    public StudioMessageSecurityExpressionRoot(Authentication authentication, Message<?> message,
                                               UserServiceInternal userServiceInternal,
                                               GroupServiceInternal groupServiceInternal) {
        super(authentication, message);
        this.userServiceInternal = userServiceInternal;
        this.groupServiceInternal = groupServiceInternal;
    }

    protected boolean containsSystemAdminGroup(List<Group> groups) {
        return groups.stream()
                .map(Group::getGroupName)
                .anyMatch(isEqual(SYSTEM_ADMIN_GROUP));
    }

    /**
     * Checks if the current user belongs to the {@code system_admin} group
     */
    public boolean isSystemAdmin() {
        try {
            List<Group> userGroups = userServiceInternal.getUserGroups(-1, authentication.getName());
            return containsSystemAdminGroup(userGroups);
        } catch (ServiceLayerException | UserNotFoundException e) {
            logger.error("Error checking groups for user {}", authentication.getName(), e);
        }
        return false;
    }

    /**
     * Checks if the current user belongs to any group in the site from the message destination
     */
    public boolean isSiteMember() {
        String destination = SimpMessageHeaderAccessor.getDestination(message.getHeaders());

        if (StringUtils.isNotEmpty(destination)) {
            Matcher matcher = SITE_DESTINATION_PATTERN.matcher(destination);
            if (matcher.matches()) {
                String siteId = matcher.group(1);
                try {
                    List<Group> userGroups = userServiceInternal.getUserGroups(-1, authentication.getName());
                    if (containsSystemAdminGroup(userGroups)) {
                        return true;
                    } else {
                        List<String> siteGroups = groupServiceInternal.getSiteGroups(siteId);
                        return userGroups.stream()
                                .map(Group::getGroupName)
                                .anyMatch(siteGroups::contains);
                    }
                } catch (ServiceLayerException | UserNotFoundException e) {
                    logger.error("Error checking groups for user {} in site {}", authentication.getName(), siteId, e);
                }
            }
        }
        return false;
    }

}
