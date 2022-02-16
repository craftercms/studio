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
package org.craftercms.studio.impl.v1.service.content;


import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.exception.ServiceLayerException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.AbstractRegistrableService;
import org.craftercms.studio.api.v1.service.content.ContentItemIdGenerator;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

public class ContentItemIdGeneratorImpl extends AbstractRegistrableService implements ContentItemIdGenerator {
    
    protected static final Logger logger = LoggerFactory.getLogger(ContentItemIdGeneratorImpl.class);

    @Override
    public void register() {
        this._servicesManager.registerService(ContentItemIdGenerator.class, this);
    }

    @Override
    public Map<String, String> getIds() throws ServiceLayerException {
        Map<String,String> params = new HashMap<String,String>();
        String pageId = UUID.randomUUID().toString();
        String groupId;
        StringTokenizer tokenizer = new StringTokenizer(pageId, "-");
        if (tokenizer.countTokens() > 0) {
            groupId = tokenizer.nextToken();
        } else {
            groupId = pageId.substring(0, 4);
        }
        params.put(DmConstants.KEY_PAGE_ID, pageId);
        params.put(DmConstants.KEY_PAGE_GROUP_ID, groupId);
        return params;
    }

}
