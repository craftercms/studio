/*
 * Copyright (C) 2007-2019 Crafter Software Corporation. All Rights Reserved.
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

package org.craftercms.studio.impl.v2.service.audit.internal;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.craftercms.studio.api.v1.exception.SiteNotFoundException;
import org.craftercms.studio.api.v2.dal.AuditDAO;
import org.craftercms.studio.api.v2.dal.AuditLog;
import org.craftercms.studio.api.v2.dal.QueryParameterNames;
import org.craftercms.studio.api.v2.service.audit.internal.AuditServiceInternal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.craftercms.studio.api.v2.dal.QueryParameterNames.ACTIONS;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.LIMIT;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.OFFSET;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.SITE_ID;
import static org.craftercms.studio.api.v2.dal.QueryParameterNames.USERNAME;

public class AuditServiceInternalImpl implements AuditServiceInternal {

    private AuditDAO auditDao;

    @Override
    public List<AuditLog> getAuditLogForSite(String site, int offset, int limit, String user, List<String> actions)
            throws SiteNotFoundException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_ID, site);
        params.put(OFFSET, offset);
        params.put(LIMIT, limit);
        if (StringUtils.isNotEmpty(user)) {
            params.put(USERNAME, user);
        }
        if (CollectionUtils.isNotEmpty(actions)) {
            params.put(ACTIONS, actions);
        }
        return auditDao.getAuditLogForSite(params);
    }

    @Override
    public int getAuditLogForSiteTotal(String site, String user, List<String> actions) throws SiteNotFoundException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(SITE_ID, site);
        if (StringUtils.isNotEmpty(user)) {
            params.put(USERNAME, user);
        }
        if (CollectionUtils.isNotEmpty(actions)) {
            params.put(ACTIONS, actions);
        }
        return auditDao.getAuditLogForSiteTotal(params);
    }


    @Override
    public List<AuditLog> getAuditLog() {
        Map<String, Object> params = new HashMap<String, Object>();
        return auditDao.getAuditLog(params);
    }

    @Override
    public AuditLog getAuditLogEntry(long auditLogId) {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(QueryParameterNames.ID, auditLogId);
        return auditDao.getAuditLogEntry(params);
    }

    @Override
    public boolean insertAuditLog(AuditLog auditLog) {
        int result = auditDao.insertAuditLog(auditLog);
        return result > 0;
    }

    public AuditDAO getAuditDao() {
        return auditDao;
    }

    public void setAuditDao(AuditDAO auditDao) {
        this.auditDao = auditDao;
    }
}
