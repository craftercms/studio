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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.craftercms.commons.aop.AopUtils;
import org.craftercms.commons.security.exception.PermissionException;
import org.craftercms.commons.security.permissions.PermissionEvaluator;
import org.craftercms.commons.security.permissions.annotations.ProtectedResource;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.studio.api.v1.service.security.SecurityService;
import org.craftercms.studio.api.v2.exception.security.ActionsDeniedException;
import org.springframework.core.annotation.Order;

/**
 * Aspect that handles {@link org.craftercms.studio.api.v2.security.HasAllPermissions} annotations,
 * by doing appropriate permission checking.
 *
 * @author avasquez
 */
@Aspect
@Order(-1)
public class HasAllPermissionsAnnotationHandler {

    private static final String ERROR_KEY_EVALUATOR_NOT_FOUND = "security.permission.evaluatorNotFound";
    private static final String ERROR_KEY_EVALUATION_FAILED = "security.permission.evaluationFailed";

    protected Map<Class<?>, PermissionEvaluator<?, ?>> permissionEvaluators;
    protected SecurityService securityService;

    @Around("@within(org.craftercms.studio.api.v2.security.HasAllPermissions) || " +
            "@annotation(org.craftercms.studio.api.v2.security.HasAllPermissions)")
    public Object checkPermissions(ProceedingJoinPoint pjp) throws Throwable {
        boolean allowed = true;
        Method method = AopUtils.getActualMethod(pjp);
        HasAllPermissions hasAllPermissions = getHasAllPermissionsAnnotation(method, pjp);
        Class<?> type = hasAllPermissions.type();
        String[] actions = hasAllPermissions.actions();
        PermissionEvaluator permissionEvaluator = permissionEvaluators.get(type);

        Object securedResource = getAnnotatedProtectedResource(method, pjp);
        if (securedResource == null) {
            securedResource = getAnnotatedProtectedResourceIds(method, pjp);
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
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("User ").append(securityService.getCurrentUser())
                    .append(" does not have all of the requested permissions ")
                    .append(Stream.of(actions).collect(Collectors.joining(",","[","]")));

            throw new ActionsDeniedException(sb.toString());
        }
    }

    protected HasAllPermissions getHasAllPermissionsAnnotation(Method method, ProceedingJoinPoint pjp) {
        HasAllPermissions hasAllPermissions = method.getAnnotation(HasAllPermissions.class);

        if (hasAllPermissions == null) {
            Class<?> targetClass = pjp.getTarget().getClass();
            hasAllPermissions = targetClass.getAnnotation(HasAllPermissions.class);
        }

        return hasAllPermissions;
    }

    protected Object getAnnotatedProtectedResource(Method method, ProceedingJoinPoint pjp) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = pjp.getArgs();

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation a : paramAnnotations[i]) {
                if (a instanceof ProtectedResource) {
                    return params[i];
                }
            }
        }

        return null;
    }

    protected Map<String, Object> getAnnotatedProtectedResourceIds(Method method, ProceedingJoinPoint pjp) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = pjp.getArgs();
        Map<String, Object> resourceIds = null;

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation a : paramAnnotations[i]) {
                if (a instanceof ProtectedResourceId) {
                    String idName = ((ProtectedResourceId) a).value();

                    if (resourceIds == null) {
                        resourceIds = new HashMap<>();
                    }

                    resourceIds.put(idName, params[i]);
                }
            }
        }

        return resourceIds;
    }

    public Map<Class<?>, PermissionEvaluator<?, ?>> getPermissionEvaluators() {
        return permissionEvaluators;
    }

    public void setPermissionEvaluators(Map<Class<?>, PermissionEvaluator<?, ?>> permissionEvaluators) {
        this.permissionEvaluators = permissionEvaluators;
    }

    public SecurityService getSecurityService() {
        return securityService;
    }

    public void setSecurityService(SecurityService securityService) {
        this.securityService = securityService;
    }
}
