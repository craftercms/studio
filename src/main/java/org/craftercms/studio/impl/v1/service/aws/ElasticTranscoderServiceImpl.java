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

import org.craftercms.commons.config.profiles.ConfigurationProfileNotFoundException;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.aws.elastictranscoder.ElasticTranscoder;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderJob;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v1.service.aws.ElasticTranscoderService;
import org.springframework.beans.factory.annotation.Required;

import javax.validation.Valid;
import java.io.InputStream;

/**
 * Default implementation of {@link ElasticTranscoderService}.
 *
 * @author avasquez
 */
public class ElasticTranscoderServiceImpl extends AbstractAwsService<TranscoderProfile> implements ElasticTranscoderService {

    private ElasticTranscoder transcoder;

    @Required
    public void setTranscoder(ElasticTranscoder transcoder) {
        this.transcoder = transcoder;
    }

    @Override
    @Valid
    public TranscoderJob transcodeFile(@ValidateStringParam String site,
                                       @ValidateStringParam String profileId,
                                       @ValidateStringParam String filename,
                                       InputStream content) throws AwsException, ConfigurationProfileNotFoundException {
        TranscoderProfile profile = getProfile(site, profileId);
        TranscoderJob job = transcoder.startJob(filename, content, profile);

        return job;
    }

}
