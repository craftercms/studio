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

import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.springframework.messaging.Message;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.expression.DefaultMessageSecurityExpressionHandler;

/**
 * Extension of {@link DefaultMessageSecurityExpressionHandler} that allows to integrate Studio security expressions
 *
 * @see  StudioMessageSecurityExpressionRoot
 *
 * @author joseross
 * @since 4.0.0
 */
public class StudioSecurityExpressionHandler<T> extends DefaultMessageSecurityExpressionHandler<T> {

    protected final SecurityService securityService;

    public StudioSecurityExpressionHandler(SecurityService securityService) {
        this.securityService = securityService;
    }

    @Override
    protected SecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
                                                                        Message<T> invocation) {
        StudioMessageSecurityExpressionRoot root = new StudioMessageSecurityExpressionRoot(authentication, invocation,
                                                        securityService);
        root.setPermissionEvaluator(getPermissionEvaluator());
        // A new instance needs to be created because it is private in the super class
        root.setTrustResolver(new AuthenticationTrustResolverImpl());
        root.setRoleHierarchy(getRoleHierarchy());
        return root;
    }

}
