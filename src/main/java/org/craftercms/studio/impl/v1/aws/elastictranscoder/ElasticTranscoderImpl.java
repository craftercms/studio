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

import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoder;
import com.amazonaws.services.elastictranscoder.AmazonElasticTranscoderClientBuilder;
import com.amazonaws.services.elastictranscoder.model.CreateJobOutput;
import com.amazonaws.services.elastictranscoder.model.CreateJobRequest;
import com.amazonaws.services.elastictranscoder.model.CreateJobResult;
import com.amazonaws.services.elastictranscoder.model.JobInput;
import com.amazonaws.services.elastictranscoder.model.Pipeline;
import com.amazonaws.services.elastictranscoder.model.ReadPipelineRequest;
import com.amazonaws.services.elastictranscoder.model.ReadPipelineResult;
import com.amazonaws.services.s3.AmazonS3;

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
            AmazonS3 s3Client = getS3Client(profile);
            AmazonElasticTranscoder transcoderClient = getTranscoderClient(profile);
            Pipeline pipeline = getPipeline(profile.getPipelineId(), transcoderClient);
            String baseKey = FilenameUtils.removeExtension(filename) + "/" + UUID.randomUUID().toString();
            String inputKey = baseKey + "." + FilenameUtils.getExtension(filename);

            uploadInput(inputKey, filename, content, pipeline, s3Client);

            CreateJobResult jobResult = createJob(inputKey, baseKey, profile, transcoderClient);

            return createResult(baseKey, jobResult, pipeline);
        } catch (Exception e) {
            throw new AwsException("Error while attempting to start an AWS Elastic Transcoder job for file " + filename, e);
        }
    }

    protected Pipeline getPipeline(String pipelineId, AmazonElasticTranscoder client) {
        ReadPipelineRequest readPipelineRequest = new ReadPipelineRequest();
        readPipelineRequest.setId(pipelineId);

        ReadPipelineResult result = client.readPipeline(readPipelineRequest);

        return result.getPipeline();
    }

    protected void uploadInput(String inputKey, String filename, InputStream content, Pipeline pipeline,
                               AmazonS3 s3Client) throws AwsException {
        String inputBucket = pipeline.getInputBucket();

        AwsUtils.uploadStream(inputBucket, inputKey, s3Client, partSize, filename, content);
    }

    protected CreateJobResult createJob(String inputKey, String baseKey, TranscoderProfile profile,
                                        AmazonElasticTranscoder transcoderClient) {
        CreateJobRequest jobRequest = getCreateJobRequest(inputKey, baseKey, profile);
        CreateJobResult jobResult = transcoderClient.createJob(jobRequest);

        return jobResult;
    }

    protected TranscoderJob createResult(String baseKey, CreateJobResult jobResult, Pipeline pipeline) {
        TranscoderJob job = new TranscoderJob();
        job.setId(jobResult.getJob().getId());
        job.setOutputBucket(pipeline.getOutputBucket());
        job.setBaseKey(baseKey);

        return job;
    }

    protected AmazonS3 getS3Client(TranscoderProfile profile) {
        return S3Utils.createClient(profile);
    }

    protected AmazonElasticTranscoder getTranscoderClient(TranscoderProfile profile) {
        return AmazonElasticTranscoderClientBuilder.standard()
            .withCredentials(profile.getCredentialsProvider())
            .withRegion(profile.getRegion())
            .build();
    }

    protected CreateJobRequest getCreateJobRequest(String inputKey, String baseKey, TranscoderProfile profile) {
        JobInput jobInput = new JobInput();
        jobInput.setKey(inputKey);

        List<CreateJobOutput> jobOutputs = new ArrayList<>(profile.getOutputs().size());

        for (TranscoderOutput output : profile.getOutputs()) {
            jobOutputs.add(getCreateJobOutput(baseKey, output));
        }

        CreateJobRequest jobRequest = new CreateJobRequest();
        jobRequest.setPipelineId(profile.getPipelineId());
        jobRequest.setInput(jobInput);
        jobRequest.setOutputs(jobOutputs);

        return jobRequest;
    }

    protected CreateJobOutput getCreateJobOutput(String baseKey, TranscoderOutput output) {
        CreateJobOutput jobOutput = new CreateJobOutput();
        jobOutput.setPresetId(output.getPresetId());
        jobOutput.setKey(baseKey + output.getOutputKeySuffix());

        if (StringUtils.isNotEmpty(output.getThumbnailSuffixFormat())) {
            jobOutput.setThumbnailPattern(baseKey + output.getThumbnailSuffixFormat());
        }

        return jobOutput;
    }

}

