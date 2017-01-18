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

package org.craftercms.studio.impl.v1.service.event;

import org.craftercms.studio.api.v1.deployment.PreviewDeployer;
import org.craftercms.studio.api.v1.ebus.PreviewEventContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.event.EventService;
import org.jgroups.JChannel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

public class EventServiceImpl implements EventService {

    private static final Logger logger = LoggerFactory.getLogger(EventServiceImpl.class);

    public EventServiceImpl() throws Exception {
        JChannel channel = new JChannel();
        rpcDispatcher = new RpcDispatcher(channel, this);
        channel.connect(CLUSTER_NAME);
    }

    @Override
    protected void finalize() throws Throwable {
        if (rpcDispatcher != null) {
            rpcDispatcher.close();
            rpcDispatcher = null;
        }
        super.finalize();
    }

    @Override
    public void firePreviewSyncEvent(String site) {
        try {
            PreviewEventContext context = new PreviewEventContext();
            context.setSite(site);
            MethodCall call = new MethodCall(getClass().getMethod(PREVIEW_SYNC_LISTENER_METHOD, PreviewEventContext.class));
            call.setArgs(context);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (NoSuchMethodException e) {
            logger.error("Could not find listener method for event, site: " + site, e);
        } catch (Exception e) {
            logger.error("Error invoking preview sync event for site" + site, e);
        }
    }

    @Override
    public void onPreviewSyncEvent(PreviewEventContext context) {
        previewDeployer.onEvent(context.getSite());
    }

    @Override
    public boolean firePreviewCreateTargetEvent(String site) {
        boolean toReturn = true;
        try {
            PreviewEventContext context = new PreviewEventContext();
            context.setSite(site);
            MethodCall call = new MethodCall(getClass().getMethod(PREVIEW_CREATE_TARGET_LISTENER_METHOD, PreviewEventContext.class));
            call.setArgs(context);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (NoSuchMethodException e) {
            logger.error("Could not find listener method for event, site: " + site, e);
            toReturn = false;
        } catch (Exception e) {
            logger.error("Error invoking preview sync event for site" + site, e);
            toReturn = false;
        }
        return toReturn;
    }

    @Override
    public void onCreateTargetEvent(PreviewEventContext context) {
        String site = context.getSite();
        previewDeployer.createTarget(site);
    }

    public PreviewDeployer getPreviewDeployer() { return previewDeployer; }
    public void setPreviewDeployer(PreviewDeployer previewDeployer) { this.previewDeployer = previewDeployer; }

    protected PreviewDeployer previewDeployer;

    protected RpcDispatcher rpcDispatcher;
}
