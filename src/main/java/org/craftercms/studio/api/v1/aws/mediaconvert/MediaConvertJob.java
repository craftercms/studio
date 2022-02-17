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

/**
 * Holds the information about a transcoding job from AWS MediaConvert.
 *
 * @author joseross
 */
public class MediaConvertJob {

    /**
     * ARN of the transcoding job.
     */
    protected String arn;
    /**
     * Id of the transcoding job.
     */
    protected String id;

    /**
     * Destination of the transcoding job output.
     */
    protected String destination;
    /**
     * Base filename of the transcoding job output.
     */
    protected String baseKey;

    public String getArn() {
        return arn;
    }

    public void setArn(final String arn) {
        this.arn = arn;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(final String destination) {
        this.destination = destination;
    }

    public String getBaseKey() {
        return baseKey;
    }

    public void setBaseKey(final String baseKey) {
        this.baseKey = baseKey;
    }

}
