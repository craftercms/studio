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

package org.craftercms.studio.impl.v1.service.aws;

import java.io.InputStream;

import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvert;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertJob;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertProfile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v1.service.aws.MediaConvertService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link MediaConvertService}.
 *
 * @author joseross
 * @deprecated This service has been replaced with
 *             {@link org.craftercms.studio.impl.v2.service.aws.mediaconvert.AwsMediaConvertServiceImpl}
 */
@Deprecated
public class MediaConvertServiceImpl extends AbstractAwsService<MediaConvertProfile> implements MediaConvertService {

    /**
     * Instance of {@link MediaConvert}.
     */
    protected MediaConvert mediaConvert;

    @Required
    public void setMediaConvert(final MediaConvert mediaConvert) {
        this.mediaConvert = mediaConvert;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaConvertJob startJob(final @ValidateStringParam(name = "site") String site,
                                    final @ValidateStringParam(name = "profile") String profileId,
                                    final @ValidateStringParam(name = "filename") String filename,
                                    final InputStream content) throws AwsException {

        MediaConvertProfile profile = getProfile(site, profileId);

        return mediaConvert.startJob(filename, content, profile);
    }

}
