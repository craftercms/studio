package org.craftercms.studio.impl.v1.service.box;

import org.craftercms.studio.api.v1.box.BoxProfile;
import org.craftercms.studio.api.v1.box.BoxProfileReader;
import org.craftercms.studio.api.v1.exception.BoxException;
import org.craftercms.studio.api.v1.service.box.BoxService;
import org.springframework.beans.factory.annotation.Required;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.EncryptionAlgorithm;
import com.box.sdk.JWTEncryptionPreferences;

/**
 * {@inheritDoc}
 */
public class BoxServiceImpl implements BoxService {

    protected static final String DEFAULT_URL_FORMAT = "/remote-assets/box/%s/%s";

    protected String urlFormat = DEFAULT_URL_FORMAT;

    protected BoxProfileReader profileReader;

    @Required
    public void setProfileReader(final BoxProfileReader profileReader) {
        this.profileReader = profileReader;
    }

    public void setUrlFormat(final String urlFormat) {
        this.urlFormat = urlFormat;
    }

    protected BoxProfile getProfile(String site, String profileId) throws BoxException  {
        return profileReader.getProfile(site, profileId);
    }

    protected BoxAPIConnection getConnection(BoxProfile profile) {
        JWTEncryptionPreferences jwtPrefs = new JWTEncryptionPreferences();
        jwtPrefs.setPublicKeyID(profile.getPublicKeyId());
        jwtPrefs.setPrivateKey(profile.getPrivateKey());
        jwtPrefs.setPrivateKeyPassword(profile.getPrivateKeyPassword());
        jwtPrefs.setEncryptionAlgorithm(EncryptionAlgorithm.RSA_SHA_256);
        BoxConfig config = new BoxConfig(
            profile.getClientId(), profile.getClientSecret(), profile.getEnterpriseId(), jwtPrefs);

        return BoxDeveloperEditionAPIConnection.getAppEnterpriseConnection(config);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getAccessToken(final String site, final String profileId) throws BoxException {
        BoxProfile profile = getProfile(site, profileId);
        BoxAPIConnection api = getConnection(profile);
        return api.getAccessToken();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUrl(final String site, final String profileId, final String fileId) throws BoxException {
        getProfile(site, profileId); // validate that the profileId exists in the site
        return String.format(urlFormat, profileId, fileId);
    }

}
