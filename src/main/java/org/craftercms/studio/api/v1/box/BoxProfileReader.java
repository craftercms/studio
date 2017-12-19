package org.craftercms.studio.api.v1.box;

import org.craftercms.studio.api.v1.exception.BoxException;

/**
 * Defines the operations to handle Box Profiles for a site.
 *
 * @author joseross
 */
public interface BoxProfileReader {

    /**
     * Reads the specified profile from the XML configuration file.
     * @param site the name of the site to read the configuration file
     * @param profileId the name of the profile to search
     * @return the @{link {@link BoxProfile} instance holding the configuration
     * @throws BoxException if the configuration can't be read or the profile is not found
     */
    BoxProfile getProfile(String site, String profileId) throws BoxException;

}
