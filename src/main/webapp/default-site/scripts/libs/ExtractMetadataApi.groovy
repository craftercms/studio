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
