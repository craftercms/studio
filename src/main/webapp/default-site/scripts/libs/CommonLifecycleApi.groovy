/*
 * Copyright (C) 2007-2022 Crafter Software Corporation. All Rights Reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scripts.libs

import org.slf4j.LoggerFactory

class CommonLifecycleApi {
    static logger = LoggerFactory.getLogger(CommonLifecycleApi.class)
    def contentLifecycleParams

    CommonLifecycleApi(params) {
        contentLifecycleParams = params
    }

    def onCopy(site, path) {
        logger.debug("Run copy operation event on site '{}' path '{}'", site, path)
    }

    def onDelete(site, path) {
        logger.debug("Run delete operation event on site '{}' path '{}'", site, path)
    }

    def onDuplicate(site, path) {
        logger.debug("Run duplicate operation event on site '{}' path '{}'", site, path)
    }

    def onNew(site, path) {
        logger.debug("Run new operation event on site '{}' path '{}'", site, path)
    }

    def onRename(site, path) {
        logger.debug("Run rename operation event on site '{}' path '{}'", site, path)
    }

    def onRevert(site, path) {
        logger.debug("Run revert operation event on site '{}' path '{}'", site, path)
    }

    def onUpdate(site, path) {
        logger.debug("Run update operation event on site '{}' path '{}'", site, path)
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
            logger.info("Unknown operation '{}' on site '{}' path '{}'",
                    contentLifecycleParams.contentLifecycleOperation,
                    contentLifecycleParams.site,
                    contentLifecycleParams.path)
        }
    }
}
