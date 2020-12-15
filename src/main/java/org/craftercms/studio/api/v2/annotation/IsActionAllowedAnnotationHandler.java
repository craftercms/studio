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

package org.craftercms.studio.api.v2.annotation;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.craftercms.commons.aop.AopUtils;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.security.ActionNotAllowedException;
import org.craftercms.studio.api.v2.service.security.SecurityService;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.craftercms.studio.api.v2.annotation.IsActionAllowedParameter.PATH;
import static org.craftercms.studio.api.v2.annotation.IsActionAllowedParameter.SITE;
import static org.craftercms.studio.api.v2.security.AvailableActions.EVERYTHING_ALLOWED;

@Aspect
public class IsActionAllowedAnnotationHandler {

    private static final Logger logger = LoggerFactory.getLogger(IsActionAllowedAnnotationHandler.class);

    private org.craftercms.studio.api.v1.service.security.SecurityService securityServiceV1;
    private SecurityService securityServiceV2;

    public IsActionAllowedAnnotationHandler(
            SecurityService securityServiceV2,
            org.craftercms.studio.api.v1.service.security.SecurityService securityServiceV1) {
        this.securityServiceV2 = securityServiceV2;
        this.securityServiceV1 = securityServiceV1;
    }

    @Around("@within(org.craftercms.studio.api.v2.annotation.IsActionAllowed) || " +
            "@annotation(org.craftercms.studio.api.v2.annotation.IsActionAllowed)")
    public Object checkAllowedActions(ProceedingJoinPoint pjp) throws Throwable {
        Method method = AopUtils.getActualMethod(pjp);
        IsActionAllowed isActionAllowed = getIsActionAllowedAnnotation(method);
        if (isActionAllowed.allowedActionsMask() != EVERYTHING_ALLOWED) {
            Map<String, String> parameters = getAnnotatedParameters(method, pjp);
            String site = parameters.get(SITE);
            String path = parameters.get(PATH);
            String user = securityServiceV1.getCurrentUser();
            long userAvailableActions = securityServiceV2.getAvailableActions(site, path, user);
            long result = userAvailableActions & isActionAllowed.allowedActionsMask();
            if (result == 0) {
                throw new ActionNotAllowedException("User not authorized to execute " + method.getDeclaringClass() +
                        "." + method.getName());
            }
        }
        return pjp.proceed();
    }

    private IsActionAllowed getIsActionAllowedAnnotation(Method method) {
        return method.getAnnotation(IsActionAllowed.class);
    }

    private Map<String, String> getAnnotatedParameters(Method method, ProceedingJoinPoint pjp) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = pjp.getArgs();
        Map<String, String> annotatedParams = new HashMap<String, String>();

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation a : paramAnnotations[i]) {
                if (a instanceof IsActionAllowedParameter) {
                    String idName = ((IsActionAllowedParameter) a).value();
                    annotatedParams.put(idName, params[i].toString());
                }
            }
        }

        return annotatedParams;
    }
}
