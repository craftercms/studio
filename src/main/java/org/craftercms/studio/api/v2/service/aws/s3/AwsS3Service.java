/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.api.v2.service.aws.s3;

import java.io.InputStream;
import java.util.List;

import org.craftercms.commons.config.profiles.ConfigurationProfileNotFoundException;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.model.aws.s3.S3Item;

/**
 * Service that provides access to AWS S3.
 *
 * @author joseross
 */
public interface AwsS3Service {

    /**
     * Uploads a file to an S3 bucket.
     * @param siteId the site id
     * @param profileId the profile id
     * @param path the path to upload the file (will be used as part of the S3 key)
     * @param filename the filename (will be used as part of the S3 key)
     * @param content a stream providing the content of the file
     * @return the uploaded item
     * @throws AwsException if there is any error connection to S3
     * @throws SiteNotFoundException if the site is not found
     * @throws ConfigurationProfileNotFoundException if the profile is not found
     */
    S3Item uploadItem(String siteId, String profileId, String path, String filename, InputStream content)
            throws AwsException, SiteNotFoundException, ConfigurationProfileNotFoundException;

    /**
     * Lists items in an S3 bucket.
     * @param siteId the site id
     * @param profileId the profile id
     * @param path the path to list
     * @param type the type of items to list
     * @param maxKeys the maximum number of keys
     * @return the list of items
     * @throws AwsException if there is any error connection to S3
     * @throws SiteNotFoundException if the site is not found
     * @throws ConfigurationProfileNotFoundException if the profile is not found
     */
    List<S3Item> listItems(String siteId, String profileId, String path, String type, int maxKeys)
            throws AwsException, SiteNotFoundException, ConfigurationProfileNotFoundException;

}
