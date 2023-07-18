/*
 * Copyright (C) 2007-2023 Crafter Software Corporation. All Rights Reserved.
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
import org.craftercms.studio.api.v1.dal.SiteFeed;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v2.exception.SiteNotReadyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.craftercms.studio.api.v1.dal.SiteFeed.STATE_READY;

/**
 * Handles the {@link RequireSiteReady} annotation.
 * Checks if the site is ready before executing the annotated method.
 */
@Aspect
@Order(10)
public class RequireSiteReadyAnnotationHandler {

    private static final Logger logger = LoggerFactory.getLogger(RequireSiteReadyAnnotationHandler.class);

    private final SiteService siteService;

    public RequireSiteReadyAnnotationHandler(final SiteService sitesService) {
        this.siteService = sitesService;
    }

    @Around("@within(org.craftercms.studio.api.v2.annotation.RequireSiteReady) || " +
            "@annotation(org.craftercms.studio.api.v2.annotation.RequireSiteReady)")
    public Object checkSiteReadiness(ProceedingJoinPoint pjp) throws Throwable {
        Method method = AopUtils.getActualMethod(pjp);
        String siteId = getSiteId(pjp, method);

        if (siteId != null) {
            SiteFeed site = siteService.getSite(siteId);
            if (site == null || !STATE_READY.equals(site.getState())) {
                throw new SiteNotReadyException(siteId);
            }
        } else {
            logger.debug("Method '{}.{}' is annotated with @RequireSiteReady but does not have a @SiteId parameter. " +
                    "This annotation will be ignored.", method.getDeclaringClass().getName(), method.getName());
        }
        return pjp.proceed();
    }

    private String getSiteId(final ProceedingJoinPoint pjp, final Method method) {
        Annotation[][] paramAnnotations = method.getParameterAnnotations();
        Object[] params = pjp.getArgs();
        for (int i = 0; i < paramAnnotations.length; i++) {
            for (Annotation a : paramAnnotations[i]) {
                if (a instanceof SiteId) {
                    return (String) params[i];
                }
            }
        }
        return null;
    }
}
