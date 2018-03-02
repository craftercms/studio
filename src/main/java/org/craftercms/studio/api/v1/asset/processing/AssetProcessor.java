package org.craftercms.studio.api.v1.asset.processing;

import java.util.regex.Matcher;

import org.craftercms.studio.api.v1.asset.Asset;
import org.craftercms.studio.api.v1.exception.AssetProcessingException;

public interface AssetProcessor {

    void init(ProcessorConfiguration config);

    Asset processAsset(Matcher inputPathMatcher, Asset input) throws AssetProcessingException;

}
