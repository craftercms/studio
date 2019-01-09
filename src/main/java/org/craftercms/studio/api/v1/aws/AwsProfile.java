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

package org.craftercms.studio.api.v1.aws;

import com.amazonaws.auth.AWSCredentials;

/**
 * Holds the basic information required by all services.
 *
 * @author joseross
 */
public abstract class AwsProfile {

    private AWSCredentials credentials;
    private String region;

    /**
     * Returns the AWS credentials used to authenticate to S3 and Elastic Transcoder.
     */
    public AWSCredentials getCredentials() {
        return credentials;
    }

    /**
     * Sets the AWS credentials used to authenticate to S3 and Elastic Transcoder.
     */
    public void setCredentials(AWSCredentials credentials) {
        this.credentials = credentials;
    }

    /**
     * Returns the region of the S3/Elastic Transcoder
     */
    public String getRegion() {
        return region;
    }

    /**
     * Sets the region of the S3/Elastic Transcoder
     */
    public void setRegion(String region) {
        this.region = region;
    }

}
