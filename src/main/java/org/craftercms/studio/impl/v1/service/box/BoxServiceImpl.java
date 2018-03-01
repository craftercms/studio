package org.craftercms.studio.impl.v1.service.box;

import java.io.InputStream;

import org.craftercms.studio.api.v1.box.BoxProfile;
import org.craftercms.studio.api.v1.box.BoxProfileReader;
import org.craftercms.studio.api.v1.exception.BoxException;
import org.craftercms.studio.api.v1.service.box.BoxService;
import org.craftercms.studio.api.v1.box.BoxUploadResult;
import org.springframework.beans.factory.annotation.Required;
import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIException;
import com.box.sdk.BoxConfig;
import com.box.sdk.BoxDeveloperEditionAPIConnection;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.EncryptionAlgorithm;
import com.box.sdk.JWTEncryptionPreferences;

/**
 * {@inheritDoc}
 */
public class BoxServiceImpl implements BoxService {

    /**
     * Box API file size limit, according to the documentation larger files should be uploaded in chunks.
     */
    public static final long MIN_SIZE = 20000000;

    protected BoxProfileReader profileReader;

    @Required
    public void setProfileReader(final BoxProfileReader profileReader) {
        this.profileReader = profileReader;
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

    protected BoxFolder getUploadFolder(String uploadFolder, BoxAPIConnection api) {
        BoxFolder root = BoxFolder.getRootFolder(api);
        Iterable<BoxItem.Info> items = root.getChildren("name", "id");
        for(BoxItem.Info info : items) {
            if(info instanceof BoxFolder.Info && info.getName().equals(uploadFolder)) {
                return (BoxFolder) info.getResource();
            }
        }
        return root.createFolder(uploadFolder).getResource();
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
    public BoxUploadResult uploadFile(final String site, final String profileId, final String filename,
                                      final InputStream content) throws BoxException {
        try {
            BoxProfile profile = getProfile(site, profileId);
            BoxAPIConnection api = getConnection(profile);
            String uploadFolder = profile.getUploadFolder();
            BoxFolder folder = getUploadFolder(uploadFolder, api);
            BoxFile.Info info = folder.uploadFile(content, filename);
            BoxUploadResult result = new BoxUploadResult();
            result.setId(info.getID());
            result.setName(info.getName());
            return result;
        } catch (BoxAPIException e) {
            throw new BoxException("Error during file upload", e);
        }
    }
}
