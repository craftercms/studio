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
     *
     * @return output key suffix
     */
    public String getOutputKeySuffix() {
        return outputKeySuffix;
    }

    /**
     * Sets the key suffix for files generated for this output (e.g. -small, -medium, -large)
     *
     * @param outputKeySuffix output key suffix
     */
    public void setOutputKeySuffix(String outputKeySuffix) {
        this.outputKeySuffix = outputKeySuffix;
    }

    /**
     * Returns the thumbnail suffix format for this output. If nothing is specified, no thumbnails will be generated. The format should
     * at least contain the macro {count} (required by AWS). {resolution} can also be specified. E.g. -{resolution}-{count}.jpg
     *
     * @return thumbnail suffix format
     */
    public String getThumbnailSuffixFormat() {
        return thumbnailSuffixFormat;
    }

    /**
     * Sets the thumbnail suffix format for this output. If nothing is specified, no thumbnails will be generated. The format should
     * at least contain the macro {count} (required by AWS). {resolution} can also be specified. E.g. -{resolution}-{count}.jpg
     *
     * @param thumbnailSuffixFormat thumbnail suffix format
     */
    public void setThumbnailSuffixFormat(String thumbnailSuffixFormat) {
        this.thumbnailSuffixFormat = thumbnailSuffixFormat;
    }

}
