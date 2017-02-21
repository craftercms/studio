package scripts.libs

import org.craftercms.studio.api.v1.log.*


class ExtractMetadataApi {
    static logger = LoggerFactory.getLogger(ExtractMetadataApi.class)
    def extractMetadataParams

    ExtractMetadataApi(params) {
        extractMetadataParams = params
    }

    def execute () {
        logger.info("running extract metadata on " + extractMetadataParams.site + ":" + extractMetadataParams.path)
    }
}
