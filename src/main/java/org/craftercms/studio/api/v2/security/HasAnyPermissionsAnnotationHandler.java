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
package org.craftercms.studio.api.v2.security;


import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.craftercms.commons.aop.AopUtils;
import org.craftercms.commons.security.exception.PermissionException;
import org.craftercms.commons.security.permissions.PermissionEvaluator;
import org.craftercms.commons.security.permissions.annotations.AbstractPermissionAnnotationHandler;
import org.craftercms.studio.api.v2.exception.security.ActionsDeniedException;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.springframework.core.annotation.Order;

import java.beans.ConstructorProperties;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Aspect that handles {@link org.craftercms.studio.api.v2.security.HasAnyPermissions} annotations,
 * by doing appropriate permission checking.
 */
@Aspect
@Order(-1)
public class HasAnyPermissionsAnnotationHandler extends AbstractPermissionAnnotationHandler {

	private static final String ERROR_KEY_EVALUATOR_NOT_FOUND = "security.permission.evaluatorNotFound";
	private static final String ERROR_KEY_EVALUATION_FAILED = "security.permission.evaluationFailed";

	@ConstructorProperties({"permissionEvaluators"})
	public HasAnyPermissionsAnnotationHandler(Map<Class<?>, PermissionEvaluator<?, ?>> permissionEvaluators) {
		super(permissionEvaluators);
	}

	@Around("@within(org.craftercms.studio.api.v2.security.HasAnyPermissions) || " +
		"@annotation(org.craftercms.studio.api.v2.security.HasAnyPermissions)")
	public Object checkPermissions(ProceedingJoinPoint pjp) throws Throwable {
		boolean allowed = false;
		Method method = AopUtils.getActualMethod(pjp);
		HasAnyPermissions hasAnyPermissions = getHasPermissionAnnotation(method, pjp, HasAnyPermissions.class);
		Class<?> type = hasAnyPermissions.type();
		String[] actions = hasAnyPermissions.actions();
		PermissionEvaluator permissionEvaluator = permissionEvaluators.get(type);

		Object securedResource = getAnnotatedProtectedResource(method, pjp.getArgs());
		if (securedResource == null) {
			securedResource = getAnnotatedProtectedResourceIds(method, pjp.getArgs());
		}

		if (permissionEvaluator == null) {
			throw new PermissionException(ERROR_KEY_EVALUATOR_NOT_FOUND, type);
		}

		try {
			for (String action : actions) {
				allowed = allowed || permissionEvaluator.isAllowed(securedResource, action);
			}
		} catch (PermissionException e) {
			throw new PermissionException(ERROR_KEY_EVALUATION_FAILED, e);
		}

		if (allowed) {
			return pjp.proceed();
		}
		String message = "User " + SecurityUtils.getCurrentUsername() +
			" does not have any of the requested permissions [" +
			StringUtils.join(actions, ",") +
			"]";

		throw new ActionsDeniedException(message);
	}
}
