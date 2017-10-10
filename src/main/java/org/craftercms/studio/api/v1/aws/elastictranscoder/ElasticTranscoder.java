package org.craftercms.studio.api.v1.aws.elastictranscoder;

import java.io.File;

import org.craftercms.studio.api.v1.exception.TranscoderException;

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
     * @param file      the file of the video
     * @param profile   the transcoding profile
     *
     * @return the metadata of the transcoder job. It's important to point out that returning the job info doesn't mean that the job
     * has been completed. To monitor the progress of the job the returned job ID can be used.
     *
     * @throws TranscoderException if an error occurred
     */
    TranscoderJob startJob(String filename, File file, TranscoderProfile profile) throws TranscoderException;

}
