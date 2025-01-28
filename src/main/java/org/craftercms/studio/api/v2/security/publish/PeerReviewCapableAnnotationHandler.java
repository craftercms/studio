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

package org.craftercms.studio.api.v2.security.publish;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.craftercms.commons.aop.AopUtils;
import org.craftercms.studio.api.v1.service.configuration.ServicesConfig;
import org.craftercms.studio.api.v2.annotation.SiteId;
import org.craftercms.studio.api.v2.annotation.StudioAnnotationUtils;
import org.craftercms.studio.api.v2.annotation.publish.PackageId;
import org.craftercms.studio.api.v2.dal.User;
import org.craftercms.studio.api.v2.dal.publish.PublishDAO;
import org.craftercms.studio.api.v2.dal.publish.PublishPackage;
import org.craftercms.studio.api.v2.exception.security.PeerReviewCheckException;
import org.craftercms.studio.api.v2.service.security.SecurityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.annotation.Order;

import java.beans.ConstructorProperties;
import java.lang.reflect.Method;

/**
 * Aspect that handles {@link PeerReviewCapable} annotations.
 */
@Aspect
@Order(-20)
public class PeerReviewCapableAnnotationHandler {
	private static final Logger logger = LoggerFactory.getLogger(PeerReviewCapableAnnotationHandler.class);

	private final SecurityService securityService;
	private final ServicesConfig servicesConfig;
	private final PublishDAO publishDao;

	@ConstructorProperties({"securityService", "servicesConfig", "publishDao"})
	public PeerReviewCapableAnnotationHandler(SecurityService securityService, ServicesConfig servicesConfig,
											  PublishDAO publishDao) {
		this.securityService = securityService;
		this.servicesConfig = servicesConfig;
		this.publishDao = publishDao;
	}

	@Around("@within(PeerReviewCapable) || " +
		"@annotation(PeerReviewCapable)")
	@SuppressWarnings("unused")
	public Object checkPeerReview(ProceedingJoinPoint pjp) throws Throwable {
		Method method = AopUtils.getActualMethod(pjp);
		String siteId = StudioAnnotationUtils.getAnnotationValue(pjp, method, SiteId.class, String.class);

		if (!servicesConfig.isRequirePeerReview(siteId)) {
			logger.debug("Peer review is not required for site '{}'", siteId);
			return pjp.proceed();
		}

		Long packageId = StudioAnnotationUtils.getAnnotationValue(pjp, method, PackageId.class, Long.class);
		if (packageId == null) {
			// package id not being provided means is a package creation, so we must reject it
			logger.debug("Peer review is enabled for site '{}'. Users cannot publish directly. Method '{}.{}' is annotated with @PeerReviewCapable but does not have a @PackageId parameter. ",
				siteId, method.getDeclaringClass().getName(), method.getName());
			throw new PeerReviewCheckException("Peer review is enabled for site '%s''. Users cannot publish directly."
				.formatted(siteId));
		}
		PublishPackage publishPackage = publishDao.getByStringSiteId(siteId, packageId);
		User user = (User) securityService.getAuthentication().getPrincipal();

		if (user.getId() == publishPackage.getSubmitterId()) {
			String message = ("User '%s' is the submitter of the package '%s'. " +
				"Users are not allowed to approve their own packages when peer-review is enabled").formatted(user.getId(), packageId);
			logger.debug(message);
			throw new PeerReviewCheckException(message);
		}

		return pjp.proceed();
	}

}
