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

package org.craftercms.studio.impl.v2.service.log;

import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.security.HasAnyPermissions;
import org.craftercms.studio.api.v2.service.log.LoggerService;
import org.craftercms.studio.model.rest.logging.LoggerConfig;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.beans.ConstructorProperties;
import java.util.List;

import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_CONFIGURE_LOG_LEVELS;
import static org.craftercms.studio.permissions.StudioPermissionsConstants.PERMISSION_VIEW_LOG_LEVELS;

/**
 * Default implementation for {@link LoggerService}.
 *
 * @author jmendeza
 * @since 4.0.2
 */
public class LoggerServiceImpl implements LoggerService {

    protected final LoggerService loggerServiceInternal;

    @ConstructorProperties({"loggerServiceInternal"})
    public LoggerServiceImpl(final LoggerService loggerServiceInternal) {
        this.loggerServiceInternal = loggerServiceInternal;
    }

    @Override
    @HasAnyPermissions(actions = {PERMISSION_VIEW_LOG_LEVELS, PERMISSION_CONFIGURE_LOG_LEVELS}, type = DefaultPermission.class)
    public List<LoggerConfig> getLoggerConfigs() throws ServiceLayerException {
        return loggerServiceInternal.getLoggerConfigs();
    }

    @Override
    @Valid
    @HasAnyPermissions(actions = {PERMISSION_VIEW_LOG_LEVELS, PERMISSION_CONFIGURE_LOG_LEVELS}, type = DefaultPermission.class)
    public LoggerConfig getLoggerConfig(@ValidateStringParam final String name, boolean createIfAbsent) throws ServiceLayerException {
        return loggerServiceInternal.getLoggerConfig(name, createIfAbsent);
    }

    @Override
    @Valid
    @HasPermission(action = PERMISSION_CONFIGURE_LOG_LEVELS, type = DefaultPermission.class)
    public void setLoggerLevel(@ValidateStringParam final String name,
                               @NotEmpty
                               @ValidateStringParam(
                                       whitelistedPatterns = {VALID_LEVEL_PATTERN}) final String level,
                               boolean createIfAbsent) throws ServiceLayerException {
        loggerServiceInternal.setLoggerLevel(name, level, createIfAbsent);
    }
}
