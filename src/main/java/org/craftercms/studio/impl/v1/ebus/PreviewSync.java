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

import org.craftercms.commons.http.RequestContext;
import org.craftercms.studio.api.v1.deployment.Deployer;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.ebus.RepositoryEventMessage;
import org.craftercms.studio.api.v1.exception.ServiceException;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.impl.v1.deployment.PreviewDeployer;
import org.jgroups.JChannel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

import javax.servlet.http.HttpSession;

public class PreviewSync extends BaseEBusEvent {

    private final static Logger logger = LoggerFactory.getLogger(PreviewSync.class);

    public PreviewSync() throws Exception {
        super();
    }

    public void syncAllContentToPreview(String site) {
        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);
        String sessionTicket = securityProvider.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
        message.setRepositoryEventContext(repositoryEventContext);

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onSyncAllContentToPreview", RepositoryEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (Exception e) {
            logger.error("Error invoking Sync All Content To Preview event", e);
        }
    }

    public void onSyncAllContentToPreview(RepositoryEventMessage message) {
        try {
            previewDeployer.syncAllContentToPreview(message);
        } catch (ServiceException e) {
            logger.error("Site '" + message.getSite() + "' synchronization failed", e);
        }
    }

    public void syncPath(String site, String path) {
        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);
        message.setPath(path);
        String sessionTicket = securityProvider.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
        message.setRepositoryEventContext(repositoryEventContext);

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onSyncPath", RepositoryEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (Exception e) {
            logger.error("Error invoking Sync All Content To Preview event", e);
        }
    }

    public void onSyncPath(RepositoryEventMessage message) {
        logger.info("Received cluster message");
        String site = message.getSite();
        String path = message.getPath();
        RepositoryEventContext.setCurrent(message.getRepositoryEventContext());
        previewDeployer.deployFile(site, path);
        RepositoryEventContext.setCurrent(null);
    }

    public void notifyUpdateContent(String site, String path) {
        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);
        message.setPath(path);
        String sessionTicket = securityProvider.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
        message.setRepositoryEventContext(repositoryEventContext);

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onUpdateContent", RepositoryEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (Exception e) {
            logger.error("Error invoking Update Content event", e);
        }
    }

    public void onUpdateContent(RepositoryEventMessage message) {
        try {
            previewDeployer.onUpdateContent(message);
        } catch (Exception t) {
            logger.error("Error while deploying preview content for: " + message.getSite() + " - " + message.getPath(), t);
        } finally {
            RepositoryEventContext.setCurrent(null);
        }
    }

    public  void notifyDeleteContent(String site, String path) {
        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);
        message.setPath(path);
        String sessionTicket = securityProvider.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
        message.setRepositoryEventContext(repositoryEventContext);

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onDeleteContent", RepositoryEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (Exception e) {
            logger.error("Error invoking Update Content event", e);
        }
    }

    public void onDeleteContent(RepositoryEventMessage message) throws ServiceException {
        try {
            previewDeployer.onDeleteContent(message);
        } catch (Exception t) {
            logger.error("Error while deleting content from: " + message.getSite() + " - " + message.getPath(), t);
        }
    }

    public  void notifyMoveContent(String site, String path, String oldPath) {
        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);
        message.setPath(path);
        message.setOldPath(oldPath);
        String sessionTicket = securityProvider.getCurrentToken();
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(sessionTicket);
        message.setRepositoryEventContext(repositoryEventContext);

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onMoveContent", RepositoryEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (Exception e) {
            logger.error("Error invoking Move Content event", e);
        }
    }

    public void onMoveContent(RepositoryEventMessage message) throws ServiceException {
        try {
            previewDeployer.onMoveContent(message);
        } catch (Exception t) {
            logger.error("Error while responding to moving content event for: " + message.getSite() + " - " + message.getPath(), t);
        }
    }

    public Deployer getPreviewDeployer() { return previewDeployer; }
    public void setPreviewDeployer(PreviewDeployer previewDeployer) { this.previewDeployer = previewDeployer; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    protected PreviewDeployer previewDeployer;
    protected SecurityProvider securityProvider;
}
