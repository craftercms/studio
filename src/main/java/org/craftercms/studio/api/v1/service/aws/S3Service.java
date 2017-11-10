package org.craftercms.studio.api.v1.service.aws;

import java.io.File;

import org.craftercms.commons.validation.annotations.param.ValidateStringParam;
import org.craftercms.studio.api.v1.aws.s3.S3Output;
import org.craftercms.studio.api.v1.exception.AwsException;

/**
 * Service that provides access to AWS S3 to upload files.
 *
 * @author joseross
 */
public interface S3Service {

    /**
     * Requests the file upload using the specified {@link org.craftercms.studio.api.v1.aws.s3.S3Profile}.
     *
     * @param site       the site
     * @param profileId  the id of the {@link org.craftercms.studio.api.v1.aws.s3.S3Profile} to use.
     * @param filename   the name of the file to upload
     * @param file       the file itself
     * @return
     * @throws AwsException if an error occurs
     */
    S3Output uploadFile(String site, String profileId, String filename, File file) throws AwsException;

}
