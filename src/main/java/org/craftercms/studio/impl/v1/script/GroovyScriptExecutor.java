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

package org.craftercms.studio.impl.v1.script;

import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.craftercms.studio.api.v1.script.ScriptExecutor;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.List;
import java.util.Map;

public class GroovyScriptExecutor implements ScriptExecutor {

    @Override
    public void executeScriptString(String script, Map<String, Object> model) throws ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        factory.setBindings(new SimpleBindings(model));
        ScriptEngine engine = factory.getEngineByName("groovy");
        GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl)engine;
        for (String classPath : scriptsClassPath) {
            gse.getClassLoader().addClasspath(classPath);
        }
        engine.eval(script);
    }

    public List<String> getScriptsClassPath() { return scriptsClassPath; }
    public void setScriptsClassPath(List<String> scriptsClassPath) { this.scriptsClassPath = scriptsClassPath; }

    protected List<String> scriptsClassPath;
}
