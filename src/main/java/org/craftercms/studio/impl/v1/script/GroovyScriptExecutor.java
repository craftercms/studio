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
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.craftercms.studio.api.v1.script.ScriptExecutor;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.RejectASTTransformsCustomizer;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SandboxInterceptor;
import org.kohsuke.groovy.sandbox.SandboxTransformer;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Map;

public class GroovyScriptExecutor implements ScriptExecutor {

	protected final static String GROOVY_ENGINE_NAME = "groovy";
	protected final SandboxInterceptor sandboxInterceptor;
	protected final boolean enableScriptSandbox;
	protected final List<String> scriptsClassPath;

	@ConstructorProperties({"sandboxInterceptor", "scriptsClassPath", "enableScriptSandbox"})
	public GroovyScriptExecutor(SandboxInterceptor sandboxInterceptor, List<String> scriptsClassPath, boolean enableScriptSandbox) {
		this.sandboxInterceptor = sandboxInterceptor;
		this.scriptsClassPath = scriptsClassPath;
		this.enableScriptSandbox = enableScriptSandbox;
	}

	protected ScriptEngine getScriptEngine(Map<String, Object> model) {
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
		return scriptEngine;
	}

	@Override
	public void executeScriptString(String script, Map<String, Object> model) throws ScriptException {
		if (sandboxInterceptor != null) {
			sandboxInterceptor.register();
		}
		try {
			ScriptEngine scriptEngine = getScriptEngine(model);
			scriptEngine.eval(script);
		} finally {
			if (sandboxInterceptor != null) {
				sandboxInterceptor.unregister();
			}
		}
	}

}
