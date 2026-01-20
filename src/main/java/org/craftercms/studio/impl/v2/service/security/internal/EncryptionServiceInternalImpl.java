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

package org.craftercms.studio.impl.v2.service.security.internal;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.craftercms.commons.crypto.CryptoException;
import org.craftercms.commons.crypto.TextEncryptor;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.Site;
import org.craftercms.studio.api.v2.dal.SiteDAO;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.security.EncryptionService;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;

import java.beans.ConstructorProperties;

import static org.craftercms.studio.api.v2.dal.AuditLog.createAuditLogEntry;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_CREATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_ENCRYPTION_TOKEN;
import static org.craftercms.studio.api.v2.utils.StudioConfiguration.CONFIGURATION_GLOBAL_SYSTEM_SITE;

/**
 * Internal implementation of {@link EncryptionService}.
 * @author joseross
 */
public class EncryptionServiceInternalImpl implements EncryptionService {

	private static final Logger logger = LoggerFactory.getLogger(EncryptionServiceInternalImpl.class);

	private final StudioConfiguration studioConfiguration;
	private final AuditService auditService;
	private final TextEncryptor textEncryptor;
	private final SiteDAO siteDAO;
	private final int maxLength;
	private final long delay;

	@ConstructorProperties({"studioConfiguration", "auditService", "textEncryptor", "siteDAO",
		"maxLength", "delay"})
	public EncryptionServiceInternalImpl(StudioConfiguration studioConfiguration, AuditService auditService,
					     TextEncryptor textEncryptor, SiteDAO siteDAO, int maxLength, long delay) {
		this.studioConfiguration = studioConfiguration;
		this.auditService = auditService;
		this.textEncryptor = textEncryptor;
		this.siteDAO = siteDAO;
		this.maxLength = maxLength;
		this.delay = delay;
	}

	@Override
	public String encrypt(final String siteId, final String text) throws ServiceLayerException {
		if (StringUtils.isEmpty(text) || text.length() > maxLength) {
			throw new InvalidParametersException("The provided text is invalid");
		}
		try {
			Thread.sleep(delay * 1000);
			String encryptedToken = textEncryptor.encrypt(text);

			var auth = SecurityContextHolder.getContext().getAuthentication();
			String resolvedSiteId = StringUtils.isNotEmpty(siteId) ? siteId
				: studioConfiguration.getProperty(CONFIGURATION_GLOBAL_SYSTEM_SITE);
			String targetId = DigestUtils.md5Hex(encryptedToken);
			createEncryptionAuditLog(resolvedSiteId, auth.getName(), targetId);
			logger.debug("Encryption token created for site '{}', target ID '{}'", resolvedSiteId, targetId);

			return encryptedToken;
		} catch (CryptoException | InterruptedException e) {
			throw new ServiceLayerException("Error encrypting text", e);
		}
	}

	/**
	 * Create encryption audit log
	 *
	 * @param siteId   site identifier
	 * @param actor    actor
	 * @param targetId encryption target id
	 */
	private void createEncryptionAuditLog(String siteId, String actor, String targetId) {
		Site site = siteDAO.getSite(siteId);
		AuditLog entry = createAuditLogEntry();
		entry.setOperation(OPERATION_CREATE);
		entry.setActorId(actor);
		entry.setSiteId(site.getId());
		entry.setPrimaryTargetId(targetId);
		entry.setPrimaryTargetType(TARGET_TYPE_ENCRYPTION_TOKEN);
		entry.setPrimaryTargetValue(targetId);
		auditService.insertAuditLog(entry);
	}
}
