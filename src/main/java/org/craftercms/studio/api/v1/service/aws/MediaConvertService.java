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

import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertJob;
import org.craftercms.studio.api.v1.exception.AwsException;

/**
 * Defines the operations available from AWS MediaConvert.
 *
 * @author joseross
 * @deprecated This service has been replaced with
 *              {@link org.craftercms.studio.api.v2.service.aws.mediaconvert.AwsMediaConvertService}
 */
@Deprecated
public interface MediaConvertService {

    /**
     * Uploads a file to S3 and creates a transcoding job using the specified profile.
     * @param site name of the site
     * @param profileId id of the profile
     * @param filename name of the file
     * @param content content of the file
     * @return information of the transcoding job
     * @throws AwsException if the upload or transcoding job creation fails
     */
    MediaConvertJob startJob(String site, String profileId, String filename, InputStream content) throws AwsException;

}
