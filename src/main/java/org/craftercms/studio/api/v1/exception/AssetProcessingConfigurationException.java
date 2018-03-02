package org.craftercms.studio.api.v1.exception;

public class AssetProcessingConfigurationException extends AssetProcessingException {

    public AssetProcessingConfigurationException(String message) {
        super(message);
    }

    public AssetProcessingConfigurationException(String message, Exception e) {
        super(message, e);
    }

}
