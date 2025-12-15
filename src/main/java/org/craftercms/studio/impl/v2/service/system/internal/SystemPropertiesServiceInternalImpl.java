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

package org.craftercms.studio.impl.v2.service.system.internal;

import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.RetryingDatabaseOperationFacade;
import org.craftercms.studio.api.v2.dal.system.SystemPropertiesDAO;
import org.craftercms.studio.api.v2.dal.system.SystemProperty;
import org.craftercms.studio.api.v2.service.audit.AuditService;
import org.craftercms.studio.api.v2.service.system.SystemPropertiesService;
import org.craftercms.studio.impl.v2.utils.security.SecurityUtils;
import org.slf4j.Logger;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.craftercms.studio.api.v2.dal.AuditLogConstants.OPERATION_SYSTEM_PROPERTY_UPDATE;
import static org.craftercms.studio.api.v2.dal.AuditLogConstants.TARGET_TYPE_SYSTEM_PROPERTY;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * Internal implementation of {@link SystemPropertiesService}.
 */
public class SystemPropertiesServiceInternalImpl implements SystemPropertiesService {

	private final static Logger logger = getLogger(SystemPropertiesServiceInternalImpl.class);

	protected final SystemPropertiesDAO systemPropertiesDAO;
	protected final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade;
	protected final AuditService auditService;

	@ConstructorProperties({"retryingDatabaseOperationFacade", "systemPropertiesDAO", "auditService"})
	public SystemPropertiesServiceInternalImpl(final RetryingDatabaseOperationFacade retryingDatabaseOperationFacade,
											   final SystemPropertiesDAO systemPropertiesDAO,
											   final AuditService auditService) {
		this.retryingDatabaseOperationFacade = retryingDatabaseOperationFacade;
		this.systemPropertiesDAO = systemPropertiesDAO;
		this.auditService = auditService;
	}

	@Override
	public Map<String, String> getSystemProperties(final List<String> propertyNames) {
		return systemPropertiesDAO.getProperties(propertyNames).stream()
				.collect(Collectors.toMap(SystemProperty::name, SystemProperty::value));
	}

	@Override
	public void setSystemProperties(final Map<String, String> properties) {
		List<SystemProperty> propertyList = properties.entrySet().stream()
				.map(e -> new SystemProperty(e.getKey(), e.getValue()))
				.toList();
		retryingDatabaseOperationFacade.retry(() -> systemPropertiesDAO.updateProperties(propertyList));
		auditPropertiesUpdate(properties);
	}

	/**
	 * Log and audit system properties update
	 *
	 * @param properties updated properties
	 */
	private void auditPropertiesUpdate(final Map<String, String> properties) {
		String username = SecurityUtils.getCurrentUsername();
		properties.forEach((name, value) -> {
			logger.info("System property update: '{}'='{}' by user '{}'", name, value, username);
			AuditLog auditLogEntry = AuditLog.createAuditLogEntry();
			auditLogEntry.setOperation(OPERATION_SYSTEM_PROPERTY_UPDATE);
			auditLogEntry.setPrimaryTargetId(name);
			auditLogEntry.setPrimaryTargetType(TARGET_TYPE_SYSTEM_PROPERTY);
			auditLogEntry.setPrimaryTargetValue(value);
			auditLogEntry.setActorId(username);
			auditService.insertAuditLog(auditLogEntry);
		});
	}
}
