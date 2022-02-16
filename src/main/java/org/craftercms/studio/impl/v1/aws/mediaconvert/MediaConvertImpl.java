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

import org.apache.commons.io.FilenameUtils;
import org.craftercms.commons.file.stores.S3Utils;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvert;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertJob;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertProfile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.impl.v1.service.aws.AwsUtils;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.mediaconvert.AWSMediaConvert;
import com.amazonaws.services.mediaconvert.AWSMediaConvertClientBuilder;
import com.amazonaws.services.mediaconvert.model.CreateJobRequest;
import com.amazonaws.services.mediaconvert.model.CreateJobResult;
import com.amazonaws.services.mediaconvert.model.GetJobTemplateRequest;
import com.amazonaws.services.mediaconvert.model.Input;
import com.amazonaws.services.mediaconvert.model.JobSettings;
import com.amazonaws.services.mediaconvert.model.JobTemplate;
import com.amazonaws.services.mediaconvert.model.OutputGroup;
import com.amazonaws.services.mediaconvert.model.OutputGroupType;
import com.amazonaws.services.s3.AmazonS3;

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
     * Creates an instance of {@link AmazonS3} to upload the files.
     * @param profile AWS profile
     * @return an S3 client
     */
    protected AmazonS3 getS3Client(MediaConvertProfile profile) {
        return S3Utils.createClient(profile);
    }

    /**
     * Creates an instance of {@link AWSMediaConvert} to start the transcoding jobs.
     * @param profile AWS profile
     * @return a MediaConvert client
     */
    protected AWSMediaConvert getMediaConvertClient(MediaConvertProfile profile) {
        return AWSMediaConvertClientBuilder.standard()
                .withCredentials(profile.getCredentialsProvider())
                .withEndpointConfiguration(
                    new AwsClientBuilder.EndpointConfiguration(profile.getEndpoint(), profile.getRegion()))
                .build();
    }

    /**
     * Creates an instance of {@link MediaConvertJob} with the result of the transcoding job creation.
     * @param createJobResult result from the MediaConvert client
     * @param destination output destination of the job
     * @param key base filename for the output
     * @return result of the transcoding job
     */
    protected MediaConvertJob createMediaConverJob(CreateJobResult createJobResult, String destination, String key) {
        MediaConvertJob job = new MediaConvertJob();
        job.setArn(createJobResult.getJob().getArn());
        job.setId(createJobResult.getJob().getId());
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
        AmazonS3 s3Client = getS3Client(profile);
        AWSMediaConvert mediaConvertClient = getMediaConvertClient(profile);

        AwsUtils.uploadStream(profile.getInputPath(), filename, s3Client, partSize, filename, content);

        String key = FilenameUtils.getBaseName(filename);

        JobTemplate jobTemplate = mediaConvertClient.getJobTemplate(new GetJobTemplateRequest()
            .withName(profile.getTemplate())).getJobTemplate();

        JobSettings jobSettings = new JobSettings()
                                    .withInputs(new Input().withFileInput(
                                        AwsUtils.getS3Url(profile.getInputPath(), filename)));

        CreateJobRequest createJobRequest = new CreateJobRequest()
                                                    .withJobTemplate(profile.getTemplate())
                                                    .withSettings(jobSettings)
                                                    .withRole(profile.getRole())
                                                    .withQueue(profile.getQueue());

        CreateJobResult createJobResult = mediaConvertClient.createJob(createJobRequest);

        return createMediaConverJob(createJobResult, getJobDestination(jobTemplate), key);
    }

    /**
     * Extracts the output destination from the transcoding job template settings.
     * @param jobTemplate the job template used
     * @return the output destination
     */
    protected String getJobDestination(final JobTemplate jobTemplate) {
        OutputGroup outputGroup = jobTemplate.getSettings().getOutputGroups().get(0);
        OutputGroupType type = OutputGroupType.valueOf(outputGroup.getOutputGroupSettings().getType());
        switch (type) {
            case FILE_GROUP_SETTINGS:
               return outputGroup.getOutputGroupSettings().getFileGroupSettings().getDestination();
            case HLS_GROUP_SETTINGS:
                return outputGroup.getOutputGroupSettings().getHlsGroupSettings().getDestination();
            case CMAF_GROUP_SETTINGS:
                return outputGroup.getOutputGroupSettings().getCmafGroupSettings().getDestination();
            case DASH_ISO_GROUP_SETTINGS:
                return outputGroup.getOutputGroupSettings().getDashIsoGroupSettings().getDestination();
            case MS_SMOOTH_GROUP_SETTINGS:
                return outputGroup.getOutputGroupSettings().getMsSmoothGroupSettings().getDestination();
            default:
                return null;
        }
    }

}
