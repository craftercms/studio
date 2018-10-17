package org.craftercms.studio.api.v1.service.aws;

import java.io.InputStream;

import org.craftercms.studio.api.v1.aws.s3.S3Output;
import org.craftercms.studio.api.v1.exception.AwsException;

/**
 * Service that provides access to AWS S3 to upload files.
 *
 * @author joseross
 * @deprecated This service has been replaced with {@link org.craftercms.studio.api.v2.service.aws.s3.AwsS3Service}
 */
@Deprecated
public interface S3Service {

    /**
     * Requests the file upload using the specified {@link org.craftercms.studio.api.v1.aws.s3.S3Profile}.
     *
     * @param site       the site
     * @param profileId  the id of the {@link org.craftercms.studio.api.v1.aws.s3.S3Profile} to use.
     * @param filename   the name of the file to upload
     * @param content    the file itself
     * @return metadata of an AWS S3 upload
     * @throws AwsException if an error occurs
     */
    S3Output uploadFile(String site, String profileId, String filename, InputStream content) throws AwsException;

}
