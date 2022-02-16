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

import java.util.regex.Matcher;

import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

/**
 * Processes an asset (input) and returns the transformed asset or a new asset (output).
 *
 * @author avasquez
 */
public interface AssetProcessor {

    /**
     * Processes the given asset.
     *
     * @param config            the configuration to use
     * @param inputPathMatcher  the Matcher object that resulted from path matching the asset against the input path pattern of the
     *                          pipeline
     * @param input             the asset to process
     *
     * @return the transformed asset or a new asset
     * @throws AssetProcessingException if an error occurs.
     */
    Asset processAsset(ProcessorConfiguration config, Matcher inputPathMatcher, Asset input) throws AssetProcessingException;

}
