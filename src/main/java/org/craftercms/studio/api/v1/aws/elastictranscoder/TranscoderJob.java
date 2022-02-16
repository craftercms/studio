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
 * Represents the metadata of an AWS Elastic Transcoder job.
 *
 * @author avasquez
 */
public class TranscoderJob {

    private String id;
    private String outputBucket;
    private String baseKey;

    /**
     * Returns the ID of the job.
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID of the job.
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the name of the S3 output bucket where the transcoder will put the result files.
     */
    public String getOutputBucket() {
        return outputBucket;
    }

    /**
     * Sets the name of the S3 output bucket where the transcoder will put the result files.
     */
    public void setOutputBucket(String outputBucket) {
        this.outputBucket = outputBucket;
    }

    /**
     * Returns the base key of the collection of transcoded files. The final filenames will be {@code baseKey + all output key suffixes}.
     */
    public String getBaseKey() {
        return baseKey;
    }

    /**
     * Sets the base key of the collection of transcoded files. The final filenames will be {@code baseKey + all output key suffixes}.
     */
    public void setBaseKey(String baseKey) {
        this.baseKey = baseKey;
    }

}
