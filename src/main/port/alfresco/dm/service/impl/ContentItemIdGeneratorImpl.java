/*
 * Copyright (C) 2007-2013 Crafter Software Corporation.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.craftercms.cstudio.alfresco.dm.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.UUID;

import org.craftercms.cstudio.alfresco.dm.constant.DmConstants;
import org.craftercms.cstudio.alfresco.dm.service.api.ContentItemIdGenerator;
import org.craftercms.cstudio.alfresco.service.AbstractRegistrableService;
import org.craftercms.cstudio.alfresco.service.exception.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ContentItemIdGeneratorImpl extends AbstractRegistrableService implements ContentItemIdGenerator {
    
    protected static final Logger logger = LoggerFactory.getLogger(ContentItemIdGeneratorImpl.class);

    @Override
    public void register() {
        this._servicesManager.registerService(ContentItemIdGenerator.class, this);
    }

    @Override
    public Map<String, String> getIds() throws ServiceException {
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
