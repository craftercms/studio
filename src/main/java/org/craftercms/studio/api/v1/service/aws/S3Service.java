/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
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
 *
 */

package org.craftercms.studio.api.v1.service.aws;

import java.io.InputStream;
import java.util.List;

import org.craftercms.studio.api.v1.aws.s3.S3Item;
import org.craftercms.studio.api.v1.aws.s3.S3Output;
import org.craftercms.studio.api.v1.exception.AwsException;

/**
 * Service that provides access to AWS S3.
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
    @Deprecated
    S3Output uploadFile(String site, String profileId, String filename, InputStream content) throws AwsException;

    /**
     * Uploads a file to an S3 bucket.
     * @param siteId the site id
     * @param profileId the profile id
     * @param filename the filename (will be used as the key)
     * @param content a stream providing the content of the file
     * @return the uploaded item
     * @throws AwsException if there is any error connection to S3
     */
    S3Item uploadItem(String siteId, String profileId, String filename, InputStream content) throws AwsException;

    /**
     * Lists items in an S3 bucket.
     * @param siteId the site id
     * @param profileId the profile id
     * @param path the path to list
     * @param type the type of items to list
     * @return the list of items
     * @throws AwsException if there is any error connection to S3
     */
    List<S3Item> listItems(String siteId, String profileId, String path, String type) throws AwsException;

}
