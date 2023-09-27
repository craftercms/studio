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
package org.craftercms.studio.api.v2.security;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.craftercms.commons.aop.AopUtils;
import org.craftercms.commons.security.exception.PermissionException;
import org.craftercms.commons.security.permissions.PermissionEvaluator;
import org.craftercms.commons.security.permissions.annotations.AbstractPermissionAnnotationHandler;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.exception.security.ActionsDeniedException;
import org.springframework.core.annotation.Order;

import java.beans.ConstructorProperties;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Aspect that handles {@link org.craftercms.studio.api.v2.security.HasAllPermissions} annotations,
 * by doing appropriate permission checking.
 *
 * @author avasquez
 */
@Aspect
@Order(-1)
public class HasAllPermissionsAnnotationHandler extends AbstractPermissionAnnotationHandler {

    private static final String ERROR_KEY_EVALUATOR_NOT_FOUND = "security.permission.evaluatorNotFound";
    private static final String ERROR_KEY_EVALUATION_FAILED = "security.permission.evaluationFailed";

    protected final SecurityService securityService;

    @ConstructorProperties({"permissionEvaluators", "securityService"})
    public HasAllPermissionsAnnotationHandler(Map<Class<?>, PermissionEvaluator<?, ?>> permissionEvaluators, SecurityService securityService) {
        super(permissionEvaluators);
        this.securityService = securityService;
    }

    @Around("@within(org.craftercms.studio.api.v2.security.HasAllPermissions) || " +
            "@annotation(org.craftercms.studio.api.v2.security.HasAllPermissions)")
    public Object checkPermissions(ProceedingJoinPoint pjp) throws Throwable {
        boolean allowed = true;
        Method method = AopUtils.getActualMethod(pjp);
        HasAllPermissions hasAllPermissions = getHasPermissionAnnotation(method, pjp, HasAllPermissions.class);
        Class<?> type = hasAllPermissions.type();
        String[] actions = hasAllPermissions.actions();
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
                allowed = allowed && permissionEvaluator.isAllowed(securedResource, action);
            }
        } catch (PermissionException e) {
            throw new PermissionException(ERROR_KEY_EVALUATION_FAILED, e);
        }

        if (allowed) {
            return pjp.proceed();
        }
        StringBuilder sb = new StringBuilder();
        sb.append("User ").append(securityService.getCurrentUser())
                .append(" does not have all of the requested permissions ")
                .append(Stream.of(actions).collect(Collectors.joining(",","[","]")));

        throw new ActionsDeniedException(sb.toString());
    }
}
