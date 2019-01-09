/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
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

import org.craftercms.studio.api.v1.aws.s3.S3Output;
import org.craftercms.studio.api.v1.exception.AwsException;

/**
 * Service that provides access to AWS S3 to upload files.
 *
 * @author joseross
 */
public interface S3Service {

    /**
     * Requests the file upload using the specified {@link org.craftercms.studio.api.v1.aws.s3.S3Profile}.
     *
     * @param site       the site
     * @param profileId  the id of the {@link org.craftercms.studio.api.v1.aws.s3.S3Profile} to use.
     * @param filename   the name of the file to upload
     * @param content    the file itself
     * @return metadata of an AWS S3 upload
     * @throws AwsException if an error occurs
     */
    S3Output uploadFile(String site, String profileId, String filename, InputStream content) throws AwsException;

}
