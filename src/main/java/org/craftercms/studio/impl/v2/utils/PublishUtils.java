/*
 * Copyright (C) 2007-2024 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.utils;

import org.craftercms.core.util.ExceptionUtils;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v2.exception.publish.PublishException;
import org.craftercms.studio.model.rest.ApiResponse;
import org.springframework.lang.NonNull;
import software.amazon.awssdk.core.exception.SdkServiceException;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.net.ConnectException;

/**
 * Utility class for publish operations.
 */
public class PublishUtils {

    /**
     * Translates an exception during publishing to an error code.
     * This method will throw an exception if the error should be handled at package level.
     *
     * @param e the exception
     * @return the error code
     * @throws PublishException if the error should be handled at package level
     */
    public static int translateItemException(final Throwable e) throws PublishException {
        PublishErrorCode publishErrorCode = translateExceptionInternal(e);
        if (publishErrorCode.packageLevel) {
            throw new PublishException("Exception should be handled at package level", e);
        }
        return publishErrorCode.code();
    }

    /**
     * Translates an exception during publishing to an error code.
     *
     * @param e the exception
     * @return the error code
     */
    public static int translatePackageException(final Throwable e) {
        return translateExceptionInternal(e).code;
    }

    @NonNull
    private static PublishErrorCode translateExceptionInternal(final Throwable e) {
        // TODO: implement
        if (ExceptionUtils.getThrowableOfType(e, ConnectException.class) != null) {
            return new PublishErrorCode(ApiResponse.S3_UNREACHABLE.getCode(), true);
        }
        if (ExceptionUtils.getThrowableOfType(e, NoSuchBucketException.class) != null) {
            return new PublishErrorCode(ApiResponse.S3_BUCKET_NOT_FOUND.getCode(), true);
        }
        if (ExceptionUtils.getThrowableOfType(e, NoSuchKeyException.class) != null) {
            return new PublishErrorCode(ApiResponse.S3_KEY_NOT_FOUND.getCode(), false);
        }

        SdkServiceException sdkServiceException = ExceptionUtils.getThrowableOfType(e, SdkServiceException.class);
        if (sdkServiceException != null) {
            switch (sdkServiceException.statusCode()) {
                case 401:
                    return new PublishErrorCode(ApiResponse.S3_UNAUTHORIZED.getCode(), true);
                case 403:
                    return new PublishErrorCode(ApiResponse.S3_FORBIDDEN.getCode(), true);
            }
        }

        return new PublishErrorCode(ApiResponse.INTERNAL_SYSTEM_FAILURE.getCode(), false);
    }

    private record PublishErrorCode(int code, boolean packageLevel) {
    }
}
