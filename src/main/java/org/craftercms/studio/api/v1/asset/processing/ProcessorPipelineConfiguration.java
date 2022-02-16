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

import java.util.List;

/**
 * Represents the configuration of an asset processor pipeline.
 *
 * @author avasquez
 */
public class ProcessorPipelineConfiguration {

    private String inputPathPattern;
    private boolean keepOriginal;
    private List<ProcessorConfiguration> processorsConfig;

    /**
     * Returns the input path pattern. This pattern will be used to check in an asset needs to be processed by the pipeline.
     */
    public String getInputPathPattern() {
        return inputPathPattern;
    }

    /**
     * Sets the input path pattern.
     */
    public void setInputPathPattern(String inputPathPattern) {
        this.inputPathPattern = inputPathPattern;
    }

    /**
     * Returns true if the original asset (without being processed) should be saved.
     */
    public boolean isKeepOriginal() {
        return keepOriginal;
    }

    /**
     * Sets if the original asset (without being processed) should be saved.
     */
    public void setKeepOriginal(boolean keepOriginal) {
        this.keepOriginal = keepOriginal;
    }

    /**
     * Returns the list of configurations for the processors of this pipeline.
     */
    public List<ProcessorConfiguration> getProcessorsConfig() {
        return processorsConfig;
    }

    /**
     * Sets the list of configurations for the processors of this pipeline.
     */
    public void setProcessorsConfig(List<ProcessorConfiguration> processorsConfig) {
        this.processorsConfig = processorsConfig;
    }

}
