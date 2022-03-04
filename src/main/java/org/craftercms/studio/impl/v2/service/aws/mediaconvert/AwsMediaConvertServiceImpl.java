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

package org.craftercms.studio.impl.v2.service.aws.mediaconvert;

import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.commons.file.stores.S3Utils;
import org.craftercms.commons.security.permissions.DefaultPermission;
import org.craftercms.commons.security.permissions.annotations.HasPermission;
import org.craftercms.commons.security.permissions.annotations.ProtectedResourceId;
import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.aws.mediaconvert.MediaConvertProfile;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.aws.AbstractAwsService;
import org.craftercms.studio.api.v2.service.aws.mediaconvert.AwsMediaConvertService;
import org.craftercms.studio.impl.v1.service.aws.AwsUtils;
import org.craftercms.studio.model.aws.mediaconvert.MediaConvertResult;
import org.springframework.beans.factory.annotation.Required;
import com.amazonaws.services.mediaconvert.AWSMediaConvert;
import com.amazonaws.services.mediaconvert.AWSMediaConvertClientBuilder;
import com.amazonaws.services.mediaconvert.model.CmafGroupSettings;
import com.amazonaws.services.mediaconvert.model.CreateJobRequest;
import com.amazonaws.services.mediaconvert.model.CreateJobResult;
import com.amazonaws.services.mediaconvert.model.DashIsoGroupSettings;
import com.amazonaws.services.mediaconvert.model.FileGroupSettings;
import com.amazonaws.services.mediaconvert.model.GetJobTemplateRequest;
import com.amazonaws.services.mediaconvert.model.HlsGroupSettings;
import com.amazonaws.services.mediaconvert.model.Input;
import com.amazonaws.services.mediaconvert.model.JobSettings;
import com.amazonaws.services.mediaconvert.model.JobTemplate;
import com.amazonaws.services.mediaconvert.model.MsSmoothGroupSettings;
import com.amazonaws.services.mediaconvert.model.OutputGroupType;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3URI;

/**
 * Default implementation of {@link AwsMediaConvertService}
 *
 * @author joseross
 * @since 3.1.1
 */
public class AwsMediaConvertServiceImpl extends AbstractAwsService<MediaConvertProfile>
    implements AwsMediaConvertService {

    private static final Logger logger = LoggerFactory.getLogger(AwsMediaConvertServiceImpl.class);

    /**
     * The part size used for S3 uploads
     */
    protected int partSize = AwsUtils.MIN_PART_SIZE;

    /**
     * The delimiter for S3 paths
     */
    protected String delimiter;

    /**
     * The URL pattern for the generated files
     */
    protected String urlPattern;

    /**
     * The extension used by Apple HLS files
     */
    protected String hlsExtension;

    /**
     * The extension used by DASH ISO files
     */
    protected String dashExtension;

    /**
     * The extension used by MS Smooth files
     */
    protected String smoothExtension;

    public void setPartSize(final int partSize) {
        this.partSize = partSize;
    }

    @Required
    public void setDelimiter(final String delimiter) {
        this.delimiter = delimiter;
    }

    @Required
    public void setUrlPattern(final String urlPattern) {
        this.urlPattern = urlPattern;
    }

    @Required
    public void setHlsExtension(final String hlsExtension) {
        this.hlsExtension = hlsExtension;
    }

    @Required
    public void setDashExtension(final String dashExtension) {
        this.dashExtension = dashExtension;
    }

    @Required
    public void setSmoothExtension(final String smoothExtension) {
        this.smoothExtension = smoothExtension;
    }

    /**
     * Creates an instance of {@link AmazonS3} to upload the files.
     * @param profile AWS profile
     * @return an S3 client
     */
    protected AmazonS3 getS3Client(MediaConvertProfile profile) {
        return S3Utils.createClient(profile, false);
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
                new AWSMediaConvertClientBuilder.EndpointConfiguration(profile.getEndpoint(), profile.getRegion()))
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @HasPermission(type = DefaultPermission.class, action = "s3 write")
    public MediaConvertResult uploadVideo(@ValidateStringParam @ProtectedResourceId("siteId") final String site,
                                          @ValidateStringParam final String inputProfileId,
                                          @ValidateStringParam final String outputProfileId,
                                          @ValidateStringParam final String filename,
                                          final InputStream content) throws AwsException {
        MediaConvertProfile profile = getProfile(site, inputProfileId);
        AmazonS3 s3Client = getS3Client(profile);
        AWSMediaConvert mediaConvertClient = getMediaConvertClient(profile);

        logger.info("Starting upload of file {0} for site {1}", filename, site);
        AwsUtils.uploadStream(profile.getInputPath(), filename, s3Client, partSize, filename, content);
        logger.info("Upload of file {0} for site {1} complete", filename, site);

        String originalName = FilenameUtils.getBaseName(filename);

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

        logger.info("Starting transcode job of file {0} for site {1}", filename, site);
        CreateJobResult createJobResult = mediaConvertClient.createJob(createJobRequest);
        logger.debug("Job {0} started", createJobResult.getJob().getArn());

        return buildResult(jobTemplate, createJobResult, outputProfileId, originalName);
    }


    protected MediaConvertResult buildResult(JobTemplate jobTemplate, CreateJobResult createJobResult,
                                             String outputProfileId, String originalName) {
        List<String> urls = new LinkedList<>();
        jobTemplate.getSettings().getOutputGroups().forEach(outputGroup -> {
            logger.debug("Adding urls from group {0}", outputGroup.getName());
            OutputGroupType type = OutputGroupType.valueOf(outputGroup.getOutputGroupSettings().getType());
            switch (type) {
                case FILE_GROUP_SETTINGS:
                    FileGroupSettings fileSettings = outputGroup.getOutputGroupSettings().getFileGroupSettings();
                    outputGroup.getOutputs().forEach(output -> {
                        addUrl(urls, outputProfileId, fileSettings.getDestination(), originalName,
                            output.getNameModifier(), output.getExtension());
                    });
                    break;
                case HLS_GROUP_SETTINGS:
                    HlsGroupSettings hlsSettings = outputGroup.getOutputGroupSettings().getHlsGroupSettings();
                    addUrl(urls, outputProfileId, hlsSettings.getDestination(), originalName,
                            StringUtils.EMPTY, hlsExtension);
                    break;
                case DASH_ISO_GROUP_SETTINGS:
                    DashIsoGroupSettings dashSettings = outputGroup.getOutputGroupSettings().getDashIsoGroupSettings();
                    addUrl(urls, outputProfileId, dashSettings.getDestination(), originalName,
                        StringUtils.EMPTY, dashExtension);
                    break;
                case MS_SMOOTH_GROUP_SETTINGS:
                    MsSmoothGroupSettings smoothSettings = outputGroup.getOutputGroupSettings().getMsSmoothGroupSettings();
                    addUrl(urls, outputProfileId, smoothSettings.getDestination(), originalName,
                        StringUtils.EMPTY, smoothExtension);
                    break;
                case CMAF_GROUP_SETTINGS:
                    CmafGroupSettings cmafSettings = outputGroup.getOutputGroupSettings().getCmafGroupSettings();
                    addUrl(urls, outputProfileId, cmafSettings.getDestination(), originalName,
                        StringUtils.EMPTY, hlsExtension);
                    addUrl(urls, outputProfileId, cmafSettings.getDestination(), originalName,
                        StringUtils.EMPTY, dashExtension);
                    break;
                default:
                    // unknown settings ... do nothing
            }
        });

        MediaConvertResult result = new MediaConvertResult();
        result.setJobId(createJobResult.getJob().getId());
        result.setJobArn(createJobResult.getJob().getArn());
        result.setUrls(urls);

        return result;
    }

    protected void addUrl(List<String> urls, String outputProfileId, String destination, String originalName,
                            String modifier, String extension) {
        String url = StringUtils.appendIfMissing(destination, delimiter) + originalName + modifier + "." + extension;
        url = createUrl(outputProfileId, url);
        urls.add(url);
        logger.debug("Added url {0}", url);
    }

    /**
     * Builds a remote-asset url using the given profile and S3 URI
     */
    protected String createUrl(String profileId, String fullUri) {
        AmazonS3URI uri = new AmazonS3URI(fullUri);
        return String.format(urlPattern, profileId, uri.getKey());
    }

}
