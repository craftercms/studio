/*
 * Crafter Studio Web-content authoring solution
 * Copyright (C) 2007-2016 Crafter Software Corporation.
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
 *
 */

package org.craftercms.studio.impl.v1.ebus;

import org.craftercms.studio.api.v1.constant.DmConstants;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.ebus.RepositoryEventMessage;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.activity.ActivityService;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;

import java.util.HashMap;
import java.util.Map;

public class ContentTypeUpdated extends BaseEBusEvent {

    private final static Logger logger = LoggerFactory.getLogger(ContentTypeUpdated.class);

    public ContentTypeUpdated() throws Exception {
        super();
    }

    public void contentTypeUpdated(String site, String contentType) {
        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);
        message.setContentType(contentType);
        String sessionTicket = securityProvider.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket, securityProvider.getCurrentUser());
        message.setRepositoryEventContext(repositoryEventContext);

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onContentTypeUpdated", RepositoryEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (Exception e) {
            logger.error("Error invoking Content Type Updated event", e);
        }
    }

    public void onContentTypeUpdated(RepositoryEventMessage message) {
        String site =  message.getSite();
        String contentType = message.getContentType();
        RepositoryEventContext.setCurrent(message.getRepositoryEventContext());
        String user = securityProvider.getCurrentUser();
        Map<String,String> extraInfo = new HashMap<String,String>();
        extraInfo.put(DmConstants.KEY_CONTENT_TYPE, DmConstants.CONTENT_TYPE_FORM_DEFINITION);
        activityService.postActivity(site, user, contentType, ActivityService.ActivityType.UPDATED, ActivityService.ActivitySource.UI, extraInfo);
    }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public ActivityService getActivityService() { return activityService; }
    public void setActivityService(ActivityService activityService) { this.activityService = activityService; }

    protected ActivityService activityService;
    protected SecurityProvider securityProvider;
}
