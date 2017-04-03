package scripts.libs

import org.craftercms.studio.api.v1.log.*


class CommonLifecycleApi {
    static logger = LoggerFactory.getLogger(CommonLifecycleApi.class)
    def contentLifecycleParams

    CommonLifecycleApi(params) {
        contentLifecycleParams = params
    }

    def onCopy(site, path) {
        logger.debug("running copy operation event on " + site + ":" + path)
    }

    def onDelete(site, path) {
        logger.debug("running delete operation event on " + site + ":" + path)
    }

    def onDuplicate(site, path) {
        logger.debug("running duplicate operation event on " + site + ":" + path)
    }

    def onNew(site, path) {
        logger.debug("running new operation event on " + site + ":" + path)
    }

    def onRename(site, path) {
        logger.debug("running rename operation event on " + site + ":" + path)
    }

    def onRevert(site, path) {
        logger.debug("running revert operation event on " + site + ":" + path)
    }

    def onUpdate(site, path) {
        logger.debug("running update operation event on " + site + ":" + path)
    }

    def execute () {
        if (contentLifecycleParams.contentLifecycleOperation == "COPY") {
            onCopy(contentLifecycleParams.site, contentLifecycleParams.path)
        } else if (contentLifecycleParams.contentLifecycleOperation == "DELETE") {
            onDelete(contentLifecycleParams.site, contentLifecycleParams.path)
        } else if (contentLifecycleParams.contentLifecycleOperation == "DUPLICATE") {
            onDuplicate(contentLifecycleParams.site, contentLifecycleParams.path)
        } else if (contentLifecycleParams.contentLifecycleOperation == "NEW") {
            onNew(contentLifecycleParams.site, contentLifecycleParams.path)
        } else if (contentLifecycleParams.contentLifecycleOperation == "RENAME") {
            onRename(contentLifecycleParams.site, contentLifecycleParams.path)
        } else if (contentLifecycleParams.contentLifecycleOperation == "REVERT") {
            onRevert(contentLifecycleParams.site, contentLifecycleParams.path)
        } else if (contentLifecycleParams.contentLifecycleOperation == "UPDATE") {
            onUpdate(contentLifecycleParams.site, contentLifecycleParams.path)
        } else {
            logger.info("Unknown operation: " + contentLifecycleParams.contentLifecycleOperation + " for " +
                    contentLifecycleParams.site + ":" + contentLifecycleParams.path)
        }
    }
}
