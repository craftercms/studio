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

package org.craftercms.studio.api.v1.script;

import javax.script.ScriptException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public interface ScriptExecutor {

    void executeScriptString(String script, Map<String, Object> model) throws ScriptException;

    /**
     * Invode a script method
     * @param script script content
     * @param scriptPath script path
     * @param methodName method name
     * @param args method arguments
     * @return result of invoked method
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws IOException
     */
    Object invokeScriptMethod(String script, String scriptPath, String methodName, Object[] args)
            throws InstantiationException, IllegalAccessException, IOException;
}
