/*
 * Copyright (C) 2007-2020 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v1.web.security.access;

import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.springframework.security.access.*;
import org.springframework.security.access.vote.AbstractAccessDecisionManager;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;

import java.util.Collection;
import java.util.List;

public class StudioAccessDecisionManager extends AbstractAccessDecisionManager {

    private final static Logger loggerCrafter = LoggerFactory.getLogger(StudioAccessDecisionManager.class);

    protected StudioAccessDecisionManager(List<AccessDecisionVoter<?>> decisionVoters) {
        super(decisionVoters);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void decide(Authentication authentication, Object object, Collection<ConfigAttribute> configAttributes) throws AccessDeniedException, InsufficientAuthenticationException {
        int granted = 0;

        for (AccessDecisionVoter voter : getDecisionVoters()) {
            int result = voter.vote(authentication, object, configAttributes);
            loggerCrafter.debug("Voter: " + voter + ", returned: " + result);
            switch (result) {
                case AccessDecisionVoter.ACCESS_GRANTED:
                    granted++;
                    break;

                case AccessDecisionVoter.ACCESS_DENIED:
                    loggerCrafter.debug("Access denied by " + voter.getClass().getCanonicalName());
                    throw new AuthorizationServiceException(messages.getMessage(
                            "AbstractAccessDecisionManager.accessDenied", "Access is denied"));

                default:
                    break;
            }
        }

        if (granted > 0) {
            loggerCrafter.debug("Access granted");
            return;
        }

        if (authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            loggerCrafter.debug("User authenticated allow access");
            return;
        } else {
            // To get this far, every AccessDecisionVoter abstained
            loggerCrafter.debug("All voters abstained and user not authenticated");
            checkAllowIfAllAbstainDecisions();
        }
    }

}
