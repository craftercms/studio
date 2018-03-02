package org.craftercms.studio.api.v1.service.asset.processing;

import java.io.InputStream;

import org.craftercms.studio.api.v1.exception.AssetProcessingException;

public interface AssetProcessingService {

    void processAsset(String site, String path, String assetName, InputStream in, String isImage,
                      String allowedWidth, String allowedHeight, String allowLessSize, String draft,
                      String unlock, String systemAsset) throws AssetProcessingException;

}
