/*
 * Copyright (C) 2007-2025 Crafter Software Corporation. All Rights Reserved.
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

import org.craftercms.studio.api.v2.service.security.UserService;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.Nullable;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.messaging.Message;
import org.springframework.security.access.expression.SecurityExpressionOperations;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.messaging.access.expression.DefaultMessageSecurityExpressionHandler;
import org.springframework.security.messaging.access.expression.MessageSecurityExpressionRoot;

import java.util.function.Supplier;

/**
 * Extension of {@link DefaultMessageSecurityExpressionHandler} that allows to integrate Studio security expressions
 *
 * @author joseross
 * @see StudioMessageSecurityExpressionRoot
 * @since 4.0.0
 */
public class StudioMessageSecurityExpressionHandler<T> extends DefaultMessageSecurityExpressionHandler<T> {

	protected final UserService userService;

	public StudioMessageSecurityExpressionHandler(UserService userService) {
		this.userService = userService;
	}

	@NotNull
	@Override
	protected SecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
																		@NotNull Message<T> invocation) {
		StudioMessageSecurityExpressionRoot<T> root = createSecurityExpressionRoot(() -> authentication, invocation);
		root.setPermissionEvaluator(getPermissionEvaluator());
		// A new instance needs to be created because it is private in the super class
		root.setTrustResolver(new AuthenticationTrustResolverImpl());
		root.setRoleHierarchy(getRoleHierarchy());
		return root;
	}

	@NotNull
	@Override
	public EvaluationContext createEvaluationContext(@NotNull Supplier<? extends @Nullable Authentication> authentication, @NotNull Message<T> invocation) {
		MessageSecurityExpressionRoot<T> root = createSecurityExpressionRoot(authentication, invocation);
		StandardEvaluationContext ctx = new StandardEvaluationContext(root);
		BeanResolver beanResolver = getBeanResolver();
		if (beanResolver != null) {
			// https://github.com/spring-projects/spring-framework/issues/35371
			ctx.setBeanResolver(beanResolver);
		}
		return ctx;
	}

	private StudioMessageSecurityExpressionRoot<T> createSecurityExpressionRoot(Supplier<? extends Authentication> authentication, Message<T> invocation) {
		StudioMessageSecurityExpressionRoot<T> root = new StudioMessageSecurityExpressionRoot<T>(authentication, invocation, userService);
		root.setAuthorizationManagerFactory(getAuthorizationManagerFactory());
		root.setPermissionEvaluator(getPermissionEvaluator());
		return root;
	}
}
