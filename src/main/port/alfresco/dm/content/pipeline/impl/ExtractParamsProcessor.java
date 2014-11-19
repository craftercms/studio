/*******************************************************************************
 * Crafter Studio Web-content authoring solution
 *     Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.craftercms.cstudio.alfresco.dm.content.pipeline.impl;

import org.dom4j.Document;
import org.dom4j.Element;
import org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent;
import org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor;
import org.craftercms.cstudio.alfresco.service.exception.ContentProcessException;
import org.craftercms.cstudio.alfresco.to.ResultTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * extracts parameters such as file name from the content being processed
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class ExtractParamsProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(ExtractParamsProcessor.class);

    public static final String NAME = "ExtractParamsProcessor";

    protected Map<String, String> _params = null;
    public Map<String, String> getParams() {
        return _params;
    }
    public void setParams(Map<String, String> params) {
        this._params = params;
    }

    /**
     * default constructor
     */
    public ExtractParamsProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public ExtractParamsProcessor(String name) {
        super(name);
    }

    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        // extract file name from XML if not provided
        if (_params != null) {
            Element root = null;
            // check for each param
            for (String key : _params.keySet()) {
                // if the content does not contain the param, get it from the document
                String value = content.getProperty(key);
                if (value == null) {
                    if (root == null) {
                        Document document = content.getDocument();
                        root = document.getRootElement();
                    }
                    value = root.valueOf(_params.get(key));
                    content.addProperty(key, value);
                }
            }
        }
    }
}
