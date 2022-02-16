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

package org.craftercms.studio.api.v1.aws.mediaconvert;

import java.io.InputStream;

import org.craftercms.studio.api.v1.exception.AwsException;

/**
 * Defines the operations available for AWS MediaConvert.
 *
 * @author joseross
 */
public interface MediaConvert {

    /**
     * Uploads a file to AWS S3 and starts a transcoding job in AWS MediaConvert.
     * @param filename name of the file to upload
     * @param content stream providing the content of the file to upload
     * @param profile profile used to create the transcoding job
     * @return information of the transcoding job
     * @throws AwsException if the upload or transcoding job creation fails
     */
    MediaConvertJob startJob(String filename, InputStream content, MediaConvertProfile profile) throws AwsException;

}
