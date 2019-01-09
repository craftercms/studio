/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v1.content.pipeline;

import org.craftercms.studio.api.v1.exception.ContentProcessException;
import org.craftercms.studio.api.v1.to.ResultTO;

/**
 * @author Dejan Brkic
 */
public interface ContentProcessorPipeline {

    /**
     * process the given content through the executors chain
     *
     * @param content
     * @param result
     */
    public void processContent(PipelineContent content, ResultTO result) throws ContentProcessException;
}
