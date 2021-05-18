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
package org.craftercms.studio.impl.v2.service.scripting.internal;

import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.craftercms.commons.spring.context.RestrictedApplicationContext;
import org.craftercms.engine.util.spring.ApplicationContextAccessor;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.scripting.ScriptEngineManager;
import org.craftercms.studio.api.v2.service.scripting.internal.ScriptingServiceInternal;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SandboxInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.ConstructorProperties;

/**
 * Default implementation of {@link ScriptingServiceInternal}
 *
 * @author joseross
 * @since 4.0
 */
public class ScriptingServiceInternalImpl implements ScriptingServiceInternal, ApplicationContextAware,
        InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ScriptingServiceInternalImpl.class);

    public static final String KEY_REQUEST = "request";

    public static final String KEY_RESPONSE = "response";

    public static final String KEY_LOGGER = "logger";

    public static final String KEY_APP_CONTEXT = "applicationContext";

    protected ScriptEngineManager scriptEngineManager;

    protected SandboxInterceptor sandboxInterceptor;

    protected String scriptExtension;

    protected String scriptPathFormat;

    protected boolean enableVariableRestrictions;

    protected String[] allowedBeans;

    protected ApplicationContext applicationContext;

    protected ApplicationContextAccessor applicationContextAccessor;

    @ConstructorProperties({"scriptEngineManager", "sandboxInterceptor", "scriptExtension", "scriptPathFormat",
            "enableVariableRestrictions", "allowedBeans"})
    public ScriptingServiceInternalImpl(ScriptEngineManager scriptEngineManager, SandboxInterceptor sandboxInterceptor,
                                        String scriptExtension, String scriptPathFormat,
                                        boolean enableVariableRestrictions, String[] allowedBeans) {
        this.scriptEngineManager = scriptEngineManager;
        this.sandboxInterceptor = sandboxInterceptor;
        this.scriptExtension = scriptExtension;
        this.scriptPathFormat = scriptPathFormat;
        this.enableVariableRestrictions = enableVariableRestrictions;
        this.allowedBeans = allowedBeans;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (enableVariableRestrictions) {
            applicationContextAccessor = new ApplicationContextAccessor(
                    new RestrictedApplicationContext(applicationContext, allowedBeans));
        } else {
            applicationContextAccessor = new ApplicationContextAccessor(applicationContext);
        }
    }

    @Override
    public Object executeRestScript(String siteId, String scriptUrl, HttpServletRequest request,
                                    HttpServletResponse response) throws ResourceException, ScriptException {

        // Get the method of the request
        var requestMethod = request.getMethod().toLowerCase();

        var scriptPath = String.format(scriptPathFormat, scriptUrl, requestMethod, scriptExtension);

        // Get the script engine for this site
        var scriptEngine = scriptEngineManager.getScriptEngine(siteId);

        // Enable the sandbox if needed
        if (sandboxInterceptor != null) {
            sandboxInterceptor.register();
        }
        try {
            // Execute the script and return the result
            return scriptEngine.run(scriptPath, createBinding(request, response));
        } finally {
            if (sandboxInterceptor != null) {
                sandboxInterceptor.unregister();
            }
        }

    }

    protected Binding createBinding(HttpServletRequest request, HttpServletResponse response) {
        var binding = new Binding();
        binding.setVariable(KEY_REQUEST, request);
        binding.setVariable(KEY_RESPONSE, response);
        binding.setVariable(KEY_LOGGER, logger);
        binding.setVariable(KEY_APP_CONTEXT, applicationContextAccessor);
        return binding;
    }

    @Override
    public void reload(String siteId) {
        scriptEngineManager.reloadScriptEngine(siteId);
    }

}
