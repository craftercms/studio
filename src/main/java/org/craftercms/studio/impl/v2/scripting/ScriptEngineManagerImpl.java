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
package org.craftercms.studio.impl.v2.scripting;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyResourceLoader;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceConnector;
import groovy.util.ResourceException;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.craftercms.core.service.ContentStoreService;
import org.craftercms.engine.util.url.ContentStoreUrlConnection;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v2.core.ContextManager;
import org.craftercms.studio.api.v2.scripting.ScriptEngineManager;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.RejectASTTransformsCustomizer;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

import java.beans.ConstructorProperties;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.craftercms.studio.api.v2.utils.StudioUtils.getSiteId;

/**
 * Default implementation of {@link ScriptEngineManager}
 *
 * @author joseross
 * @since 4.0
 */
public class ScriptEngineManagerImpl implements ScriptEngineManager {

    private static final Logger logger = LoggerFactory.getLogger(ScriptEngineManagerImpl.class);

    protected Map<String, GroovyScriptEngine> scriptEngines = new ConcurrentHashMap<>();

    protected ContextManager contextManager;

    protected ContentStoreService contentStoreService;

    protected boolean sandboxEnabled;

    protected String classesBasePath;

    protected String restBasePath;

    protected String scriptExtension;

    @ConstructorProperties({"contextManager", "contentStoreService", "sandboxEnabled", "classesBasePath",
            "restBasePath", "scriptExtension"})
    public ScriptEngineManagerImpl(ContextManager contextManager, ContentStoreService contentStoreService,
                                   boolean sandboxEnabled, String classesBasePath, String restBasePath,
                                   String scriptExtension) {
        this.contextManager = contextManager;
        this.contentStoreService = contentStoreService;
        this.sandboxEnabled = sandboxEnabled;
        this.classesBasePath = classesBasePath;
        this.restBasePath = restBasePath;
        this.scriptExtension = scriptExtension;
    }

    @Override
    public GroovyScriptEngine getScriptEngine(String siteId) {
        return scriptEngines.computeIfAbsent(siteId, this::createScriptEngine);
    }

    protected GroovyScriptEngine createScriptEngine(String siteId) {
        logger.debug("Creating script engine for site {0}", siteId);
        var compilerConfig = new CompilerConfiguration();
        if (sandboxEnabled) {
            logger.debug("Enabling sandbox for site {0}", siteId);
            compilerConfig.addCompilationCustomizers(new RejectASTTransformsCustomizer(), new SandboxTransformer());
        }

        var groovyClassloader = new GroovyClassLoader(getClass().getClassLoader(), compilerConfig);
        groovyClassloader.setResourceLoader(new StudioResourceLoader(classesBasePath, new StudioUrlStreamHandler()));

        return new GroovyScriptEngine(new StudioResourceConnector(restBasePath), groovyClassloader);
    }

    @Override
    public void reloadScriptEngine(String siteId) {
        logger.debug("Reloading script engine for site {0}", siteId);
        scriptEngines.compute(siteId, (key, old) -> createScriptEngine(siteId));
    }

    // Internal classes used to load the scripts from the site

    protected class StudioUrlStreamHandler extends URLStreamHandler {

        @Override
        protected URLConnection openConnection(URL url) {
            var context = contextManager.getContext(getSiteId());
            return new ContentStoreUrlConnection(url, contentStoreService.getContent(context, url.getFile()));
        }

    }

    protected class StudioResourceConnector implements ResourceConnector {

        private final String basePath;

        public StudioResourceConnector(String basePath) {
            this.basePath = basePath;
        }
        @Override
        public URLConnection getResourceConnection(String name) throws ResourceException {
            try {
                var context = contextManager.getContext(getSiteId());
                var path = basePath + "/" + name;
                return new ContentStoreUrlConnection(new File(path).toURI().toURL(),
                                                        contentStoreService.getContent(context, path));
            } catch (Exception e) {
                throw new ResourceException(e);
            }
        }
    }

    protected class StudioResourceLoader implements GroovyResourceLoader {

        private final String basePath;

        private final URLStreamHandler urlStreamHandler;

        public StudioResourceLoader(String basePath, URLStreamHandler urlStreamHandler) {
            this.basePath = basePath;
            this.urlStreamHandler = urlStreamHandler;
        }

        @Override
        public URL loadGroovySource(String filename) throws MalformedURLException {
            if (filename.contains(".")) {
                filename = filename.replace('.', '/');
            }
            if (!filename.endsWith(scriptExtension)) {
                filename += "." + scriptExtension;
            }

            var context = contextManager.getContext(getSiteId());
            var path = basePath + "/" + filename;
            if (contentStoreService.exists(context, path)){
                return new URL(null, "site:" + path, urlStreamHandler);
            } else {
                return null;
            }
        }

    }

}
