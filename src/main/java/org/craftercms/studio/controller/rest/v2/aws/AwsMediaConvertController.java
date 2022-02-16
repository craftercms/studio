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

package org.craftercms.studio.controller.rest.v2.aws;

import java.io.IOException;
import java.io.InputStream;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.AwsException;
import org.craftercms.studio.api.v2.exception.InvalidParametersException;
import org.craftercms.studio.api.v2.service.aws.mediaconvert.AwsMediaConvertService;
import org.craftercms.studio.model.aws.mediaconvert.MediaConvertResult;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.studio.controller.rest.v2.RequestConstants.REQUEST_PARAM_SITEID;
import static org.craftercms.studio.controller.rest.v2.ResultConstants.RESULT_KEY_ITEM;

/**
 * Rest controller for AWS MediaConvert
 *
 * @author joseross
 * @since 3.1.1
 */
@RestController
@RequestMapping("/api/2/aws/mediaconvert")
public class AwsMediaConvertController {

    public static final String INPUT_PROFILE_PARAM = "inputProfileId";
    public static final String OUTPUT_PROFILE_PARAM = "outputProfileId";

    @Autowired
    protected AwsMediaConvertService mediaConvertService;

    /**
     * Uploads a file to S3 and triggers a MediaConvert job
     *
     * @param request the request
     * @return the result of triggering the job
     * @throws IOException if there is any error reading the content of the file
     * @throws AwsException if there is any error uploading the file or triggering the job
     * @throws InvalidParametersException if there is any error parsing the request
     */
    @PostMapping("/upload")
    public ResultOne<MediaConvertResult> uploadVideo(HttpServletRequest request)
        throws IOException, AwsException, InvalidParametersException {
        if (ServletFileUpload.isMultipartContent(request)) {
            ResultOne<MediaConvertResult> result = new ResultOne<>();
            try {
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator iterator = upload.getItemIterator(request);
                String siteId = null;
                String inputProfileId = null;
                String outputProfileId = null;
                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    String name = item.getFieldName();
                    try (InputStream stream = item.openStream()) {
                        if (item.isFormField()) {
                            switch (name) {
                                case REQUEST_PARAM_SITEID:
                                    siteId = Streams.asString(stream);
                                    break;
                                case INPUT_PROFILE_PARAM:
                                    inputProfileId = Streams.asString(stream);
                                    break;
                                case OUTPUT_PROFILE_PARAM:
                                    outputProfileId = Streams.asString(stream);
                                    break;
                                default:
                                    // Unknown parameter, just skip it...
                            }
                        } else {
                            if (StringUtils.isAnyEmpty(siteId, inputProfileId, outputProfileId)) {
                                throw new InvalidParametersException("Missing one or more required parameters: "
                                    + "siteId, inputProfileId or outputProfileId");
                            }
                            String filename = item.getName();
                            if (StringUtils.isNotEmpty(filename)) {
                                filename = FilenameUtils.getName(filename);
                            }
                            result.setEntity(RESULT_KEY_ITEM,
                                mediaConvertService
                                    .uploadVideo(siteId, inputProfileId, outputProfileId, filename, stream));
                            result.setResponse(ApiResponse.OK);
                        }
                    }
                }
                return result;
            } catch (FileUploadException e) {
                throw new InvalidParametersException("The request body is invalid");
            }
        } else {
            throw new InvalidParametersException("The request is not multipart");
        }
    }

}
