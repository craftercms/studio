/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

import org.apache.commons.lang3.StringUtils;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.craftercms.studio.api.v1.script.ScriptExecutor;
import org.craftercms.studio.api.v2.utils.GitRepositoryHelper;

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

    public static final String SCRIPT_ENGINE_SHORT_NAME = "groovy";

    protected GitRepositoryHelper gitRepositoryHelper;
    protected List<String> scriptsClassPath;
    protected String pluginClassPath;

    @ConstructorProperties({"gitRepositoryHelper", "scriptsClassPath", "pluginClassPath"})
    GroovyScriptExecutor(final GitRepositoryHelper gitRepositoryHelper, final List<String> scriptsClassPath,
                         final String pluginClassPath) {
        this.gitRepositoryHelper = gitRepositoryHelper;
        this.scriptsClassPath = scriptsClassPath;
        this.pluginClassPath = pluginClassPath;
    }

    @Override
    public void executeScriptString(String siteId, String script, Map<String, Object> model) throws ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        factory.setBindings(new SimpleBindings(model));
        ScriptEngine engine = factory.getEngineByName(SCRIPT_ENGINE_SHORT_NAME);
        GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl) engine;

        for (String classPath : scriptsClassPath) {
            gse.getClassLoader().addClasspath(classPath);
        }

        gse.getClassLoader().addClasspath(getPluginClassFullPath(siteId));

        engine.eval(script);
    }

    /**
     * Get plugin Groovy classes full path for a given site
     * @param siteId the site identifier
     *
     * @return the plugin Groovy classes full path
     */
    private String getPluginClassFullPath(String siteId) {
        Path repoRootPath = gitRepositoryHelper.buildRepoPath(SANDBOX, siteId);
        return repoRootPath.resolve(StringUtils.removeStart(pluginClassPath, FILE_SEPARATOR)).toAbsolutePath().toString();
    }
}
