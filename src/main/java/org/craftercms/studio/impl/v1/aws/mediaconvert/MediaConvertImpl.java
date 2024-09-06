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

package org.craftercms.studio.impl.v1.aws.mediaconvert;

import java.io.InputStream;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.file.stores.S3Utils;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvert;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertJob;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertProfile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.impl.v1.service.aws.AwsUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.mediaconvert.MediaConvertClient;
import software.amazon.awssdk.services.mediaconvert.model.*;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Default implementation of {@link MediaConvert}.
 *
 * @author joseross
 */
public class MediaConvertImpl implements MediaConvert {

    protected int partSize = AwsUtils.MIN_PART_SIZE;

    public void setPartSize(final int partSize) {
        this.partSize = partSize;
    }

    /**
     * Creates an instance of {@link S3Client} to upload the files.
     * @param profile AWS profile
     * @return an S3 client
     */
    protected S3Client getS3Client(MediaConvertProfile profile) {
        return S3Utils.createClient(profile);
    }

    /**
     * Creates an instance of {@link MediaConvertClient} to start the transcoding jobs.
     * @param profile AWS profile
     * @return a MediaConvert client
     */
    protected MediaConvertClient getMediaConvertClient(MediaConvertProfile profile) {
        return MediaConvertClient.builder()
                .credentialsProvider(profile.getCredentialsProvider())
                .region(Region.of(profile.getRegion()))
                .endpointOverride(URI.create(profile.getEndpoint()))
                .build();
    }

    /**
     * Creates an instance of {@link MediaConvertJob} with the result of the transcoding job creation.
     * @param createJobResponse result from the MediaConvert client
     * @param destination output destination of the job
     * @param key base filename for the output
     * @return result of the transcoding job
     */
    protected MediaConvertJob createMediaConverJob(CreateJobResponse createJobResponse, String destination, String key) {
        MediaConvertJob job = new MediaConvertJob();
        job.setArn(createJobResponse.job().arn());
        job.setId(createJobResponse.job().id());
        job.setDestination(destination);
        job.setBaseKey(key);
        return job;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaConvertJob startJob(final String filename, final InputStream content, final MediaConvertProfile profile)
        throws AwsException {
        S3Client s3Client = getS3Client(profile);
        MediaConvertClient mediaConvertClient = getMediaConvertClient(profile);

        AwsUtils.uploadStream(profile.getInputPath(), filename, s3Client, partSize, filename, content);

        String key = FilenameUtils.getBaseName(filename);

        JobTemplate jobTemplate = mediaConvertClient.getJobTemplate(
                GetJobTemplateRequest.builder()
                        .name(profile.getTemplate())
                        .build()).jobTemplate();

        JobSettings jobSettings = JobSettings.builder()
                .inputs(Input.builder()
                        .fileInput(AwsUtils.getS3Url(profile.getInputPath(), filename))
                        .build())
                .build();

        CreateJobRequest createJobRequest = CreateJobRequest.builder()
                .jobTemplate(profile.getTemplate())
                .settings(jobSettings)
                .role(profile.getRole())
                .queue(profile.getQueue())
                .build();

        CreateJobResponse createJobResult = mediaConvertClient.createJob(createJobRequest);

        return createMediaConverJob(createJobResult, getJobDestination(jobTemplate), key);
    }

    /**
     * Extracts the output destination from the transcoding job template settings.
     * @param jobTemplate the job template used
     * @return the output destination
     */
    protected String getJobDestination(final JobTemplate jobTemplate) {
        OutputGroup outputGroup = jobTemplate.settings().outputGroups().get(0);
        OutputGroupType type = outputGroup.outputGroupSettings().type();
        switch (type) {
            case FILE_GROUP_SETTINGS:
               return outputGroup.outputGroupSettings().fileGroupSettings().destination();
            case HLS_GROUP_SETTINGS:
                return outputGroup.outputGroupSettings().hlsGroupSettings().destination();
            case CMAF_GROUP_SETTINGS:
                return outputGroup.outputGroupSettings().cmafGroupSettings().destination();
            case DASH_ISO_GROUP_SETTINGS:
                return outputGroup.outputGroupSettings().dashIsoGroupSettings().destination();
            case MS_SMOOTH_GROUP_SETTINGS:
                return outputGroup.outputGroupSettings().msSmoothGroupSettings().destination();
            default:
                return null;
        }
    }

}
