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

package org.craftercms.studio.api.v1.aws.elastictranscoder;

/**
 * Holds the information for a specific output a transcoder should generate.
 *
 * @author avasquez
 */
public class TranscoderOutput {

    private String presetId;
    private String outputKeySuffix;
    private String thumbnailSuffixFormat;

    /**
     * Returns the ID of the preset with the transcoding configuration.
     */
    public String getPresetId() {
        return presetId;
    }

    /**
     * Sets the ID of the preset with the transcoding configuration.
     */
    public void setPresetId(String presetId) {
        this.presetId = presetId;
    }

    /**
     * Returns the key suffix for files generated for this output (e.g. -small, -medium, -large)
     */
    public String getOutputKeySuffix() {
        return outputKeySuffix;
    }

    /**
     * Sets the key suffix for files generated for this output (e.g. -small, -medium, -large)
     */
    public void setOutputKeySuffix(String outputKeySuffix) {
        this.outputKeySuffix = outputKeySuffix;
    }

    /**
     * Returns the thumbnail suffix format for this output. If nothing is specified, no thumbnails will be generated. The format should
     * at least contain the macro {count} (required by AWS). {resolution} can also be specified. E.g. -{resolution}-{count}.jpg
     */
    public String getThumbnailSuffixFormat() {
        return thumbnailSuffixFormat;
    }

    /**
     * Sets the thumbnail suffix format for this output. If nothing is specified, no thumbnails will be generated. The format should
     * at least contain the macro {count} (required by AWS). {resolution} can also be specified. E.g. -{resolution}-{count}.jpg
     */
    public void setThumbnailSuffixFormat(String thumbnailSuffixFormat) {
        this.thumbnailSuffixFormat = thumbnailSuffixFormat;
    }

}
