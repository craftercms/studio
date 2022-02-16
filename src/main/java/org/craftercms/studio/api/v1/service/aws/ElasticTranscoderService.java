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

package org.craftercms.studio.api.v1.service.aws;

import java.io.InputStream;

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
     * @param content          the video file itself
     *
     * @return the {@link TranscoderJob} that was started
     *
     * @throws AwsException if an error occurs
     */
    TranscoderJob transcodeFile(String site, String profileId, String filename, InputStream content) throws
        AwsException;

}
