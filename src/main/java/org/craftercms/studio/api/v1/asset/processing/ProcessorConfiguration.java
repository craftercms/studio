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
package org.craftercms.studio.api.v1.asset.processing;

import java.util.Map;

/**
 * Represents the configuration of a processor.
 *
 * @author avasquez
 */
public class ProcessorConfiguration {

    private String type;
    private Map<String, String> params;
    private String outputPathFormat;

    /**
     * Returns the processor type.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the processor type.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns the processor parameters.
     */
    public Map<String, String> getParams() {
        return params;
    }

    /**
     * Sets the processor parameters.
     */
    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    /**
     * Returns the output path format. Variables that have a dollar sign ($) and an index are later replaced by groups that resulted
     * during input path matching, to form the final output path. E.g for path /static-assets/image/logo.jpg, if pipeline input path
     * pattern = /static-assets/image/(.+)\.jpg and output path format = /static-assets/image/processed/$1.jpg, then the final output
     * path of the processed asset will be /static-assets/image/processed/logo.jpg
     *
     * @return output path format
     */
    public String getOutputPathFormat() {
        return outputPathFormat;
    }

    /**
     * Sets the output path format.
     */
    public void setOutputPathFormat(String outputPathFormat) {
        this.outputPathFormat = outputPathFormat;
    }

}
