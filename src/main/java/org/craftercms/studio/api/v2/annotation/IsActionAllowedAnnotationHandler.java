/*
 * Copyright (C) 2007-2021 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.craftercms.commons.aop.AopUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.exception.security.UserNotFoundException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.exception.security.ActionNotAllowedException;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.craftercms.studio.api.v2.annotation.IsActionAllowedParameter.PATH;
import static org.craftercms.studio.api.v2.annotation.IsActionAllowedParameter.PATHS;
import static org.craftercms.studio.api.v2.annotation.IsActionAllowedParameter.SITE;
import static org.craftercms.studio.api.v2.security.AvailableActions.ALL_PERMISSIONS;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

@Aspect
public class IsActionAllowedAnnotationHandler {

    private static final Logger logger = LoggerFactory.getLogger(IsActionAllowedAnnotationHandler.class);

    private org.craftercms.studio.api.v1.service.security.SecurityService securityServiceV1;
    private SecurityService securityServiceV2;
    private StudioConfiguration studioConfiguration;

    @Around("@within(org.craftercms.studio.api.v2.annotation.IsActionAllowed) || " +
            "@annotation(org.craftercms.studio.api.v2.annotation.IsActionAllowed)")
    public Object checkAllowedActions(ProceedingJoinPoint pjp) throws Throwable {
        Method method = AopUtils.getActualMethod(pjp);
        IsActionAllowed isActionAllowed = getIsActionAllowedAnnotation(method);
        if (isActionAllowed.allowedActionsMask() != ALL_PERMISSIONS) {
            Map<String, Object> parameters = getAnnotatedParameters(method, pjp);
            String site = (String) parameters.get(SITE);
            String path = (String) parameters.get(PATH);
            List<String> paths = (List<String>) parameters.get(PATHS);
            String user = securityServiceV1.getCurrentUser();
            if (StringUtils.isEmpty(site)) {
                site = studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE);
            }
            if (StringUtils.isEmpty(path) && Objects.isNull(paths)) {
                paths = new ArrayList<>();
                paths.add("/");
                paths.add("~DASHBOARD~");
            }
            long userAvailableActions = -1L;
            if (StringUtils.isNotEmpty(path)) {
                userAvailableActions &= securityServiceV2.getAvailableActions(user, site, path);
            }
            long result = userAvailableActions & isActionAllowed.allowedActionsMask();
            final String siteFinal = site;
            if (Objects.nonNull(paths)) {
                result &= paths.stream().mapToLong(p -> {
                    try {
                        return securityServiceV2.getAvailableActions(user, siteFinal, p);
                    } catch (ServiceLayerException | UserNotFoundException e) {
                        return ALL_PERMISSIONS;
                    }
                }).reduce(-1, Long::sum);
            }
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

    private Map<String, Object> getAnnotatedParameters(Method method, ProceedingJoinPoint pjp) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = pjp.getArgs();
        Map<String, Object> annotatedParams = new HashMap<String, Object>();

        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation a : paramAnnotations[i]) {
                if (a instanceof IsActionAllowedParameter) {
                    String idName = ((IsActionAllowedParameter) a).value();
                    annotatedParams.put(idName, params[i]);
                }
            }
        }

        return annotatedParams;
    }

    public org.craftercms.studio.api.v1.service.security.SecurityService getSecurityServiceV1() {
        return securityServiceV1;
    }

    public void setSecurityServiceV1(org.craftercms.studio.api.v1.service.security.SecurityService securityServiceV1) {
        this.securityServiceV1 = securityServiceV1;
    }

    public SecurityService getSecurityServiceV2() {
        return securityServiceV2;
    }

    public void setSecurityServiceV2(SecurityService securityServiceV2) {
        this.securityServiceV2 = securityServiceV2;
    }

    public StudioConfiguration getStudioConfiguration() {
        return studioConfiguration;
    }

    public void setStudioConfiguration(StudioConfiguration studioConfiguration) {
        this.studioConfiguration = studioConfiguration;
    }
}
