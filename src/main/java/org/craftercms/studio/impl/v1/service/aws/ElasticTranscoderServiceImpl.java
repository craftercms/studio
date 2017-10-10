package org.craftercms.studio.impl.v1.service.aws;

import java.io.File;
import java.io.InputStream;

import org.craftercms.studio.api.v1.aws.elastictranscoder.ElasticTranscoder;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderJob;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfileReader;
import org.craftercms.studio.api.v1.exception.TranscoderException;
import org.craftercms.studio.api.v1.service.aws.ElasticTranscoderService;
import org.craftercms.studio.api.v1.service.content.ContentService;
import org.springframework.beans.factory.annotation.Required;

/**
 * Default implementation of {@link ElasticTranscoderService}. It uses a {@link TranscoderProfileReader} to get the specified transcoder
 * profile and a {@link ElasticTranscoder} instance to start the transcoding job.
 *
 * @author avasquez
 */
public class ElasticTranscoderServiceImpl implements ElasticTranscoderService {

    private ContentService contentService;
    private ElasticTranscoder transcoder;
    private TranscoderProfileReader profileReader;

    @Required
    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }

    @Required
    public void setTranscoder(ElasticTranscoder transcoder) {
        this.transcoder = transcoder;
    }

    @Required
    public void setProfileReader(TranscoderProfileReader profileReader) {
        this.profileReader = profileReader;
    }

    @Override
    public TranscoderJob transcodeFile(String site, String profilePath, String filename, File file) throws TranscoderException {
        TranscoderProfile profile = getTranscoderProfile(site, profilePath);
        TranscoderJob job = transcoder.startJob(filename, file, profile);

        return job;
    }

    protected TranscoderProfile getTranscoderProfile(String site, String path) throws TranscoderException {
        try {
            InputStream content = contentService.getContent(site, path);

            return profileReader.readProfile(content);
        } catch (Exception e) {
            throw new TranscoderException("Unable to retrieve transcoder profile at " + path, e);
        }
    }

}
