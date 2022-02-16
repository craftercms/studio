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

package org.craftercms.studio.api.v2.service.aws.mediaconvert;

import java.io.InputStream;

import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.model.aws.mediaconvert.MediaConvertResult;

/**
 * Service that provides access to AWS MediaConvert.
 *
 * @author joseross
 * @since 3.1.1
 */
public interface AwsMediaConvertService {

    /**
     * Uploads a file to S3 and creates a transcoding job using the specified profile.
     * @param site name of the site
     * @param inputProfileId id of the profile for uploads
     * @param outputProfileId id of the s3 profile for downloads
     * @param filename name of the file
     * @param content content of the file
     * @throws AwsException if the upload or transcoding job creation fails
     * @return the result of the transcoding job
     */
    MediaConvertResult uploadVideo(final String site, final String inputProfileId, final String outputProfileId,
                                   final String filename, final InputStream content) throws AwsException;

}
