package org.craftercms.studio.api.v1.service.aws;

import java.io.File;

import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderJob;
import org.craftercms.studio.api.v1.exception.AwsException;

/**
 * Service that provides access to the AWS Elastic Transcoder to sites for video transcoding.
 *
 * @author avasquez
 */
public interface ElasticTranscoderService {

    /**
     * Requests the transcoding of the specified file to the AWS Elastic Transcoder, using the specified
     * {@link org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile}.
     *
     * @param site          the site
     * @param profileId   the id of the {@link org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile} to use
     * @param filename      the name of the video file to transcode
     * @param file          the video file itself
     *
     * @return the {@link TranscoderJob} that was started
     *
     * @throws AwsException if an error occurs
     */
    TranscoderJob transcodeFile(String site, String profileId, String filename, File file) throws AwsException;

}
