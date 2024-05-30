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

package org.craftercms.studio.impl.v1.aws.elastictranscoder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.file.stores.S3Utils;
import org.craftercms.studio.api.v1.aws.elastictranscoder.ElasticTranscoder;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderJob;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderOutput;
import org.craftercms.studio.api.v1.aws.elastictranscoder.TranscoderProfile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.impl.v1.service.aws.AwsUtils;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.elastictranscoder.ElasticTranscoderClient;
import software.amazon.awssdk.services.elastictranscoder.model.*;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Default implementation of {@link ElasticTranscoder}. Just as indicated by the interface, the video file is first uploaded to the
 * S3 input bucket of the AWS Elastic Transcoder pipeline, but before uploading, a unique bucket key is generated so that there's no
 * issue when a file with the same filename is uploaded several times. In this way all video file versions (original or transcoded) are
 * kept at all times and there's no downtime when a file is transcoded again.
 *
 * @author avasquez
 */
public class ElasticTranscoderImpl implements ElasticTranscoder {

    protected int partSize;

    public ElasticTranscoderImpl() {
        partSize = AwsUtils.MIN_PART_SIZE;
    }

    public void setPartSize(final int partSize) {
        this.partSize = partSize;
    }

    @Override
    public TranscoderJob startJob(String filename, InputStream content, TranscoderProfile profile) throws AwsException {
        try {
            S3Client s3Client = getS3Client(profile);
            ElasticTranscoderClient transcoderClient = getTranscoderClient(profile);
            Pipeline pipeline = getPipeline(profile.getPipelineId(), transcoderClient);
            String baseKey = FilenameUtils.removeExtension(filename) + "/" + UUID.randomUUID().toString();
            String inputKey = baseKey + "." + FilenameUtils.getExtension(filename);

            uploadInput(inputKey, filename, content, pipeline, s3Client);

            CreateJobResponse jobResult = createJob(inputKey, baseKey, profile, transcoderClient);

            return createResult(baseKey, jobResult, pipeline);
        } catch (Exception e) {
            throw new AwsException("Error while attempting to start an AWS Elastic Transcoder job for file " + filename, e);
        }
    }

    protected Pipeline getPipeline(String pipelineId, ElasticTranscoderClient client) {
        ReadPipelineRequest readPipelineRequest = ReadPipelineRequest.builder()
                .id(pipelineId)
                .build();

        ReadPipelineResponse response = client.readPipeline(readPipelineRequest);

        return response.pipeline();
    }

    protected void uploadInput(String inputKey, String filename, InputStream content, Pipeline pipeline,
                               S3Client s3Client) throws AwsException {
        String inputBucket = pipeline.inputBucket();

        AwsUtils.uploadStream(inputBucket, inputKey, s3Client, partSize, filename, content);
    }

    protected CreateJobResponse createJob(String inputKey, String baseKey, TranscoderProfile profile,
                                        ElasticTranscoderClient transcoderClient) {
        CreateJobRequest jobRequest = getCreateJobRequest(inputKey, baseKey, profile);
        CreateJobResponse jobResponse = transcoderClient.createJob(jobRequest);

        return jobResponse;
    }

    protected TranscoderJob createResult(String baseKey, CreateJobResponse jobResponse, Pipeline pipeline) {
        TranscoderJob job = new TranscoderJob();
        job.setId(jobResponse.job().id());
        job.setOutputBucket(pipeline.outputBucket());
        job.setBaseKey(baseKey);

        return job;
    }

    protected S3Client getS3Client(TranscoderProfile profile) {
        return S3Utils.createClient(profile);
    }

    protected ElasticTranscoderClient getTranscoderClient(TranscoderProfile profile) {
        return ElasticTranscoderClient.builder()
            .credentialsProvider(profile.getCredentialsProvider())
            .region(Region.of(profile.getRegion()))
            .build();
    }

    protected CreateJobRequest getCreateJobRequest(String inputKey, String baseKey, TranscoderProfile profile) {
        JobInput jobInput = JobInput.builder()
                .key(inputKey)
                .build();

        List<CreateJobOutput> jobOutputs = new ArrayList<>(profile.getOutputs().size());

        for (TranscoderOutput output : profile.getOutputs()) {
            jobOutputs.add(getCreateJobOutput(baseKey, output));
        }

        CreateJobRequest jobRequest = CreateJobRequest.builder()
                .pipelineId(profile.getPipelineId())
                .input(jobInput)
                .outputs(jobOutputs)
                .build();

        return jobRequest;
    }

    protected CreateJobOutput getCreateJobOutput(String baseKey, TranscoderOutput output) {
        CreateJobOutput.Builder builder = CreateJobOutput.builder()
                .presetId(output.getPresetId())
                .key(baseKey + output.getOutputKeySuffix());

        if (StringUtils.isNotEmpty(output.getThumbnailSuffixFormat())) {
            builder.thumbnailPattern(baseKey + output.getThumbnailSuffixFormat());
        }

        return builder.build();
    }

}

