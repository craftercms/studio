/*
 * Copyright (C) 2007-2018 Crafter Software Corporation. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
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
import org.craftercms.studio.api.v2.service.aws.s3.AwsS3Service;
import org.craftercms.studio.model.aws.s3.S3Item;
import org.craftercms.studio.model.rest.ApiResponse;
import org.craftercms.studio.model.rest.ResultList;
import org.craftercms.studio.model.rest.ResultOne;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static org.craftercms.studio.api.v1.aws.AwsConstants.PARAM_PATH;
import static org.craftercms.studio.api.v1.aws.AwsConstants.PARAM_PROFILE;
import static org.craftercms.studio.api.v1.aws.AwsConstants.PARAM_SITE;
import static org.craftercms.studio.api.v1.aws.AwsConstants.PARAM_TYPE;

/**
 * Rest controller for AWS S3 service.
 * @author joseross
 */
@RestController
@RequestMapping("/api/2/aws/s3")
public class AwsS3Controller {

    @Autowired
    protected AwsS3Service s3Service;

    /**
     * List items in an S3 bucket.
     * @param siteId the site id
     * @param profileId the profile id
     * @param path the path to list
     * @param type the type of file to list
     * @return the list of items
     * @throws AwsException if there is any error connecting to S3
     */
    @GetMapping("/list")
    public ResultList<S3Item> listItems(
        @RequestParam(PARAM_SITE) String siteId,
        @RequestParam(PARAM_PROFILE) String profileId,
        @RequestParam(value = PARAM_PATH, required = false, defaultValue = StringUtils.EMPTY) String path,
        @RequestParam(value = PARAM_TYPE, required = false, defaultValue = StringUtils.EMPTY) String type)
        throws AwsException {

        ResultList<S3Item> result = new ResultList<>();
        result.setEntities(s3Service.listItems(siteId, profileId, path, type));
        result.setResponse(ApiResponse.OK);

        return result;
    }

    /**
     * Upload a file to an S3 bucket.
     * @param request the request
     * @return the item uploaded
     * @throws IOException if there is any error reading the content of the file
     * @throws InvalidParametersException if there is any error parsing the request
     * @throws AwsException if there is any error connecting to S3
     */
    @PostMapping("/upload")
    public ResultOne<S3Item> uploadItem(HttpServletRequest request) throws IOException, InvalidParametersException,
        AwsException {
        if(ServletFileUpload.isMultipartContent(request)) {
            ResultOne<S3Item> result = new ResultOne<>();
            try {
                ServletFileUpload upload = new ServletFileUpload();
                FileItemIterator iterator = upload.getItemIterator(request);
                String siteId = null;
                String profileId = null;
                while (iterator.hasNext()) {
                    FileItemStream item = iterator.next();
                    String name = item.getFieldName();
                    try(InputStream stream = item.openStream()) {
                        if (item.isFormField()) {
                            switch (name) {
                                case PARAM_SITE:
                                    siteId = Streams.asString(stream);
                                    break;
                                case PARAM_PROFILE:
                                    profileId = Streams.asString(stream);
                                    break;
                            }
                        } else {
                            String filename = item.getName();
                            if (StringUtils.isNotEmpty(filename)) {
                                filename = FilenameUtils.getName(filename);
                            }
                            result.setEntity(s3Service.uploadItem(siteId, profileId, filename, stream));
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
