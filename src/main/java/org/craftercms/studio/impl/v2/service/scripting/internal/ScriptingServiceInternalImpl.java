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
package org.craftercms.studio.impl.v2.service.scripting.internal;

import groovy.lang.Binding;
import groovy.util.ResourceException;
import groovy.util.ScriptException;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.RegExUtils;
import org.craftercms.commons.http.HttpUtils;
import org.craftercms.commons.spring.context.RestrictedApplicationContext;
import org.craftercms.engine.util.spring.ApplicationContextAccessor;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.craftercms.studio.api.v2.exception.configuration.ConfigurationException;
import org.craftercms.studio.api.v2.scripting.ScriptEngineManager;
import org.craftercms.studio.api.v2.service.marketplace.MarketplaceService;
import org.craftercms.studio.api.v2.service.scripting.internal.ScriptingServiceInternal;
import org.craftercms.studio.api.v2.utils.StudioConfiguration;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SandboxInterceptor;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.beans.ConstructorProperties;
import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.craftercms.studio.impl.v2.utils.PluginUtils.getPluginConfigurationPath;

/**
 * Default implementation of {@link ScriptingServiceInternal}
 *
 * @author joseross
 * @since 4.0
 */
public class ScriptingServiceInternalImpl implements ScriptingServiceInternal, ApplicationContextAware,
        InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(ScriptingServiceInternalImpl.class);

    public static final String KEY_SITE_ID = "siteId";

    public static final String KEY_PARAMS = "params";

    public static final String KEY_PLUGIN_ID = "pluginId";

    public static final String KEY_PLUGIN_CONFIG = "pluginConfig";

    public static final String KEY_REQUEST = "request";

    public static final String KEY_RESPONSE = "response";

    public static final String KEY_LOGGER = "logger";

    public static final String KEY_APP_CONTEXT = "applicationContext";

    protected Pattern pattern = Pattern.compile(".*plugins/(.+)");

    protected ScriptEngineManager scriptEngineManager;

    protected SandboxInterceptor sandboxInterceptor;

    protected String scriptExtension;

    protected String scriptPathFormat;

    protected boolean enableVariableRestrictions;

    protected String[] allowedBeans;

    protected ApplicationContext applicationContext;

    protected ApplicationContextAccessor applicationContextAccessor;

    protected MarketplaceService marketplaceService;

    protected ContentService contentService;

    protected StudioConfiguration studioConfiguration;

    @ConstructorProperties({"scriptEngineManager", "sandboxInterceptor", "scriptExtension", "scriptPathFormat",
            "enableVariableRestrictions", "allowedBeans", "marketplaceService", "contentService",
            "studioConfiguration"})
    public ScriptingServiceInternalImpl(ScriptEngineManager scriptEngineManager, SandboxInterceptor sandboxInterceptor,
                                        String scriptExtension, String scriptPathFormat,
                                        boolean enableVariableRestrictions, String[] allowedBeans,
                                        MarketplaceService marketplaceService, ContentService contentService,
                                        StudioConfiguration studioConfiguration) {
        this.scriptEngineManager = scriptEngineManager;
        this.sandboxInterceptor = sandboxInterceptor;
        this.scriptExtension = scriptExtension;
        this.scriptPathFormat = scriptPathFormat;
        this.enableVariableRestrictions = enableVariableRestrictions;
        this.allowedBeans = allowedBeans;
        this.marketplaceService = marketplaceService;
        this.contentService = contentService;
        this.studioConfiguration = studioConfiguration;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        if (enableVariableRestrictions) {
            applicationContextAccessor = new ApplicationContextAccessor(
                    new RestrictedApplicationContext(applicationContext, allowedBeans));
        } else {
            applicationContextAccessor = new ApplicationContextAccessor(applicationContext);
        }
    }

    @Override
    public Object executeRestScript(String siteId, String scriptUrl, HttpServletRequest request,
                                    HttpServletResponse response) throws ResourceException, ScriptException,
                                                                         ConfigurationException {

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
            return scriptEngine.run(scriptPath, createBinding(siteId, scriptPath, request, response));
        } finally {
            if (sandboxInterceptor != null) {
                sandboxInterceptor.unregister();
            }
        }

    }

    protected Binding createBinding(String siteId, String scriptUrl, HttpServletRequest request,
                                    HttpServletResponse response) throws ConfigurationException {
        Binding binding = new Binding();
        binding.setVariable(KEY_SITE_ID, siteId);
        binding.setVariable(KEY_PARAMS, HttpUtils.createRequestParamsMap(request));
        binding.setVariable(KEY_REQUEST, request);
        binding.setVariable(KEY_RESPONSE, response);
        binding.setVariable(KEY_LOGGER, logger);
        binding.setVariable(KEY_APP_CONTEXT, applicationContextAccessor);

        String pluginId = getPluginId(siteId, scriptUrl);
        binding.setVariable(KEY_PLUGIN_ID, pluginId);

        Configuration pluginConfig = getPluginConfiguration(siteId, pluginId);
        binding.setVariable(KEY_PLUGIN_CONFIG,  pluginConfig);

        return binding;
    }

    @Override
    public void reload(String siteId) {
        scriptEngineManager.reloadScriptEngine(siteId);
    }

    protected String getPluginId(String siteId, String scriptUrl) {
        Matcher matcher = pattern.matcher(scriptUrl);
        if (!matcher.matches()) {
            return null;
        }

        String pluginId = null;
        String path = matcher.group(1);
        boolean idFound = false;
        while (!idFound && StringUtils.isNotEmpty(path)) {
            path = FilenameUtils.getPathNoEndSeparator(path);
            pluginId = RegExUtils.replaceAll(path, File.separator, ".");
            idFound = contentService.contentExists(siteId, getPluginConfigurationPath(studioConfiguration, pluginId));
        }
        if (idFound) {
            return pluginId;
        }

        return null;
    }

    protected Configuration getPluginConfiguration(String siteId, String pluginId) throws ConfigurationException {
        return marketplaceService.getPluginConfiguration(siteId, pluginId);
    }

}
