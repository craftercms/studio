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
