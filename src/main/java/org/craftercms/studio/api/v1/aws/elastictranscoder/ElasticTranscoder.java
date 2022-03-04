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

import java.io.InputStream;

import org.craftercms.studio.api.v1.exception.AwsException;

/**
 * Proxy to AWS Elastic Transcoder (and S3) that enables the creation of transcoder jobs based on a {@link TranscoderProfile}s.
 *
 * @author avasquez
 */
public interface ElasticTranscoder {

    /**
     * Starts a transcoder job for the specified file, based on the given {@link TranscoderProfile}. It first uploads the file to
     * the input bucket in S3 before the job is created.
     *
     * @param filename  the video's file name
     * @param content      the file of the video
     * @param profile   the transcoding profile
     *
     * @return the metadata of the transcoder job. It's important to point out that returning the job info doesn't mean that the job
     * has been completed. To monitor the progress of the job the returned job ID can be used.
     *
     * @throws AwsException if an error occurred
     */
    TranscoderJob startJob(String filename, InputStream content, TranscoderProfile profile) throws AwsException;

}
