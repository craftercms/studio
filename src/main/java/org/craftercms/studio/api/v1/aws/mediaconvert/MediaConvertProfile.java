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

import org.craftercms.commons.config.profiles.aws.AbstractAwsProfile;

/**
 * Holds the necessary information to request a transcoding job for AWS MediaConvert.
 *
 * @author joseross
 */
public class MediaConvertProfile extends AbstractAwsProfile {

    /**
     * ARN of the AWS Role used to create the transcoding jobs.
     */
    protected String role;
    /**
     * ARN of the Queue used to create the trancoding jobs.
     */
    protected String queue;
    /**
     * Name of the job template to use.
     */
    protected String template;

    /**
     * S3 URL to upload the files.
     */
    protected String inputPath;

    public String getRole() {
        return role;
    }

    public void setRole(final String role) {
        this.role = role;
    }

    public String getQueue() {
        return queue;
    }

    public void setQueue(final String queue) {
        this.queue = queue;
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(final String template) {
        this.template = template;
    }

    public String getInputPath() {
        return inputPath;
    }

    public void setInputPath(final String inputPath) {
        this.inputPath = inputPath;
    }

}
