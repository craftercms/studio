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
package org.craftercms.studio.impl.v2.service.scripting;

import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateSecurePathParam;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.service.scripting.ScriptingService;
import org.craftercms.studio.api.v2.service.scripting.internal.ScriptingServiceInternal;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.ConstructorProperties;

/**
 * Default implementation of {@link ScriptingService}
 *
 * @author joseross
 * @since 4.0
 */
public class ScriptingServiceImpl implements ScriptingService {

    protected ScriptingServiceInternal scriptingServiceInternal;

    @ConstructorProperties({"scriptingServiceInternal"})
    public ScriptingServiceImpl(ScriptingServiceInternal scriptingServiceInternal) {
        this.scriptingServiceInternal = scriptingServiceInternal;
    }

    @Override
    @ValidateParams
    public Object executeRestScript(String siteId, @ValidateSecurePathParam(name = "path") String path,
                                    HttpServletRequest request, HttpServletResponse response)
            throws ResourceException, ScriptException, ConfigurationException {
        return scriptingServiceInternal.executeRestScript(siteId, path, request, response);
    }

    @Override
    public void reload(String siteId) {
        scriptingServiceInternal.reload(siteId);
    }

}
