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
package org.craftercms.studio.impl.v1.content.pipeline;

import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.content.pipeline.PipelineContent;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;

import java.util.List;

import static org.craftercms.studio.api.v1.constant.StudioConstants.FILE_SEPARATOR;

public class PathMatchProcessor extends BaseContentProcessor {

    private static final Logger logger = LoggerFactory.getLogger(PathMatchProcessor.class);

    public static final String NAME = "PathMatchProcessor";

    /** a list of paths to match on **/
    protected List<String> _matchPaths;

    /** a list of paths not to match on **/
    protected List<String> _unmatchPaths;

    /**
     * @param matchPaths the matchPaths to set
     */
    public void setMatchPaths(List<String> matchPaths) {
        this._matchPaths = matchPaths;
    }

    /**
     * @param unmatchPaths the unmatchPaths to set
     */
    public void setUnmatchPaths(List<String> unmatchPaths) {
        this._unmatchPaths = unmatchPaths;
    }

    /**
     * default constructor
     */
    public PathMatchProcessor() {
        super(NAME);
    }

    /**
     * constructor that sets the process name
     *
     * @param name
     */
    public PathMatchProcessor(String name) {
        super(name);
    }

    public boolean isProcessable(PipelineContent content) {
        String folderPath = content.getProperty(DmConstants.KEY_FOLDER_PATH);
        String fileName = content.getProperty(DmConstants.KEY_FILE_NAME);
        String path = folderPath + FILE_SEPARATOR + fileName;
        boolean result = false;
        // if at least one match path provided, and it does not match, not processable
        if ((_matchPaths != null && _matchPaths.size() > 0)) {
            // if match is true and one of paths is matching the content path, it is processable
            for (String pathPattern : _matchPaths) {
                if (path.matches(pathPattern)) {
                    result = true;
                    break;
                }
            }
        } else {
            // if no match paths provided, match all
            result =  true;
        }
        if (result) {
            // if path matches on any unmathPath, it is not processable
            if ((_unmatchPaths != null && _unmatchPaths.size() > 0)) {
                // if match is true and one of paths is matching the content path, it is processable
                for (String pathPattern : _unmatchPaths) {
                    if (path.matches(pathPattern)) {
                        result = false;
                        break;
                    }
                }
            }
        }
        return result;
    }
}
