package org.craftercms.studio.api.v1.service.box;

import java.io.InputStream;

import org.craftercms.studio.api.v1.box.BoxUploadResult;
import org.craftercms.studio.api.v1.exception.BoxException;

/**
 * Defines the operations available for handling files in Box
 */
public interface BoxService {

    /**
     * Gets an access token to allow direct access to the Box folder.
     * @param site the name of the site to search for the configuration file
     * @param profileId the name of the profile to search
     * @return the value of the access token
     * @throws BoxException
     */
    String getAccessToken(String site, String profileId) throws BoxException;

    /**
     * Uploads a file using the configuration specified by a profile
     * @param site the name of the site to search for the configuration file
     * @param profileId the name of the profile to search
     * @param filename the name of the file to upload
     * @param content the contents of the file to upload
     * @return an instance of {@link BoxUploadResult} holding the information of the file
     * @throws BoxException if the connection to the Box API can't be opened or the upload fails
     */
    BoxUploadResult uploadFile(String site, String profileId, String filename, InputStream content) throws
        BoxException;

}
