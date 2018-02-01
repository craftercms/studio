package org.craftercms.studio.impl.v1.service.aws;

import java.io.InputStream;

import org.craftercms.commons.validation.annotations.param.ValidateParams;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.aws.AwsProfileReader;
import org.craftercms.studio.api.v1.aws.elastictranscoder.ElasticTranscoder;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderJob;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v1.service.aws.ElasticTranscoderService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link ElasticTranscoderService}. It uses a {@link AwsProfileReader} to get the specified transcoder
 * profile and a {@link ElasticTranscoder} instance to start the transcoding job.
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
    @ValidateParams
    public TranscoderJob transcodeFile(@ValidateStringParam(name = "site") String site,
                                       @ValidateStringParam(name = "profileId") String profileId,
                                       @ValidateStringParam(name = "filename") String filename,
                                       InputStream content) throws AwsException {
        TranscoderProfile profile = getProfile(site, profileId);
        TranscoderJob job = transcoder.startJob(filename, content, profile);

        return job;
    }

}
