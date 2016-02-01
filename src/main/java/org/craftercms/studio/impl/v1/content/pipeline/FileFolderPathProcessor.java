/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.studio.impl.v1.content.pipeline;


import org.apache.commons.lang.StringUtils;
import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.to.ResultTO;

import java.util.Map;

/**
 * set the file folder path in params
 *
 * @author hyanghee
 * @author Dejan Brkic
 *
 */
public class FileFolderPathProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(FileFolderPathProcessor.class);

    public static final String NAME = "FileFolderPathProcessor";

    /**
     * default constructor
     */
    public FileFolderPathProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public FileFolderPathProcessor(String name) {
        super(name);
    }

    /*
      * (non-Javadoc)
      * @see org.craftercms.cstudio.alfresco.content.pipeline.impl.BaseContentProcessor#process(org.craftercms.cstudio.alfresco.content.pipeline.api.PipelineContent, org.craftercms.cstudio.alfresco.to.ResultTO)
      */
    public void process(PipelineContent content, ResultTO result) throws ContentProcessException {
        Map<String, String> params = content.getProperties();
        String path = params.get(DmConstants.KEY_PATH);
        String fileName = params.get(DmConstants.KEY_FILE_NAME);
        String folderPath = path;
        if (!StringUtils.isEmpty(fileName)) {
            if (path.endsWith(fileName)) {
                folderPath = path.replace("/" + fileName, "");
            } else {
                if (path.endsWith(DmConstants.INDEX_FILE)) {
                    folderPath = path.replace("/" + DmConstants.INDEX_FILE, "");
                } else {
                    // path could be a file path to indicate creating a leaf underneath a leaf node
                    if (path.endsWith(DmConstants.XML_PATTERN)) {
                        folderPath = path.replace(DmConstants.XML_PATTERN, "");
                    }
                }
            }
        }
        params.put(DmConstants.KEY_FOLDER_PATH, folderPath);
    }
}
