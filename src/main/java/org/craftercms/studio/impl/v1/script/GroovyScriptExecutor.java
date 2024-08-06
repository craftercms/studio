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

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.codehaus.groovy.jsr223.GroovyScriptEngineImpl;
import org.craftercms.studio.api.v1.script.ScriptExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GroovyScriptExecutor implements ScriptExecutor {

    private static final Logger logger = LoggerFactory.getLogger(GroovyScriptExecutor.class);

    @Override
    public void executeScriptString(String script, Map<String, Object> model) throws ScriptException {
        ScriptEngineManager factory = new ScriptEngineManager();
        factory.setBindings(new SimpleBindings(model));
        // TODO: SJ: Avoid string literals
        ScriptEngine engine = factory.getEngineByName("groovy");
        GroovyScriptEngineImpl gse = (GroovyScriptEngineImpl)engine;
        for (String classPath : scriptsClassPath) {
            gse.getClassLoader().addClasspath(classPath);
        }
        engine.eval(script);
    }

    @Override
    public Object invokeScriptMethod(String script, String scriptPath, String methodName, Object[] args)
            throws InstantiationException, IllegalAccessException, IOException {
        List<URL> urls= new ArrayList<>();
        for (String classPath : scriptsClassPath) {
            File additionalClassPath = new File(classPath);
            urls.add(additionalClassPath.toURI().toURL());
        }
        URLClassLoader additionalClassLoader = new URLClassLoader(urls.toArray(new URL[0]), new GroovyClassLoader());
        GroovyClassLoader classLoader = new GroovyClassLoader(additionalClassLoader);
        Class<?> groovyClass = classLoader.parseClass(script);

        boolean methodExists = false;
        for (Method method : groovyClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                methodExists = true;
                break;
            }
        }

        if (methodExists) {
            GroovyObject groovyObject = (GroovyObject) groovyClass.newInstance();
            Object result = groovyObject.invokeMethod(methodName, args);
            logger.debug("Executed method '{}' in the script path '{}' with result '{}'", methodName, scriptPath, result);
            return result;
        } else {
            logger.debug("Method '{}' does not exist in the script path '{}'", methodName, scriptPath);
            return null;
        }
    }

    public List<String> getScriptsClassPath() { return scriptsClassPath; }
    public void setScriptsClassPath(List<String> scriptsClassPath) { this.scriptsClassPath = scriptsClassPath; }

    protected List<String> scriptsClassPath;
}
