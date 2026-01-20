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

package org.craftercms.studio.impl.v1.script;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.craftercms.studio.api.v1.script.ScriptExecutor;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.RejectASTTransformsCustomizer;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SandboxInterceptor;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.beans.ConstructorProperties;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v1.constant.GitRepositories.SANDBOX;
import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

public class GroovyScriptExecutor implements ScriptExecutor {

	protected final static String GROOVY_ENGINE_NAME = "groovy";

	protected GitRepositoryHelper gitRepositoryHelper;
	protected final SandboxInterceptor sandboxInterceptor;
	protected final boolean enableScriptSandbox;
	protected final List<String> scriptsClassPath;
	protected String pluginClassPath;

	@ConstructorProperties({"gitRepositoryHelper", "sandboxInterceptor", "scriptsClassPath", "pluginClassPath", "enableScriptSandbox"})
	public GroovyScriptExecutor(final GitRepositoryHelper gitRepositoryHelper, SandboxInterceptor sandboxInterceptor,
								List<String> scriptsClassPath, String pluginClassPath, boolean enableScriptSandbox) {
		this.gitRepositoryHelper = gitRepositoryHelper;
		this.sandboxInterceptor = sandboxInterceptor;
		this.scriptsClassPath = scriptsClassPath;
		this.pluginClassPath = pluginClassPath;
		this.enableScriptSandbox = enableScriptSandbox;
	}

	protected ScriptEngine getScriptEngine(String siteId, Map<String, Object> model) {
		ScriptEngineManager factory = new ScriptEngineManager();
		factory.setBindings(new SimpleBindings(model));
		GroovyScriptEngineImpl scriptEngine = (GroovyScriptEngineImpl) factory.getEngineByName(GROOVY_ENGINE_NAME);
		CompilerConfiguration config = new CompilerConfiguration();
		if (enableScriptSandbox) {
			config.addCompilationCustomizers(new RejectASTTransformsCustomizer(), new SandboxTransformer());
		}
		scriptEngine.setClassLoader(new GroovyClassLoader(scriptEngine.getClassLoader(), config));
		for (String classPath : scriptsClassPath) {
			scriptEngine.getClassLoader().addClasspath(classPath);
		}

		scriptEngine.getClassLoader().addClasspath(getPluginClassFullPath(siteId));
		return scriptEngine;
	}

	@Override
	public void executeScriptString(String siteId, String script, Map<String, Object> model) throws ScriptException {
		if (enableScriptSandbox && sandboxInterceptor != null) {
			sandboxInterceptor.register();
		}
		try {
			ScriptEngine scriptEngine = getScriptEngine(siteId, model);
			scriptEngine.eval(script);
		} finally {
			if (enableScriptSandbox && sandboxInterceptor != null) {
				sandboxInterceptor.unregister();
			}
		}
	}

	/**
	 * Get plugin Groovy classes full path for a given site
	 *
	 * @param siteId the site identifier
	 * @return the plugin Groovy classes full path
	 */
	private String getPluginClassFullPath(String siteId) {
		Path repoRootPath = gitRepositoryHelper.buildRepoPath(SANDBOX, siteId);
		return repoRootPath.resolve(StringUtils.removeStart(pluginClassPath, FILE_SEPARATOR)).toAbsolutePath().toString();
	}
}
