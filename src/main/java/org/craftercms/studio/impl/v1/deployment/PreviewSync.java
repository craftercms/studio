package org.craftercms.studio.impl.v1.deployment;

import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.ebus.RepositoryEventMessage;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestHandler;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

public class PreviewSync implements RequestHandler {

    private final static Logger logger = LoggerFactory.getLogger(PreviewSync.class);

    public PreviewSync() throws Exception {
        JChannel channel = new JChannel();
        rpcDispatcher = new RpcDispatcher(channel, this);
        channel.connect("StudioCluster");
    }

    @Override
    protected void finalize() throws Throwable {
        if (rpcDispatcher != null) {
            rpcDispatcher.close();
            rpcDispatcher = null;
        }
        super.finalize();
    }

    public void onSyncPath(RepositoryEventMessage message) {
        logger.info("Received cluster message");
        String site = message.getSite();
        String path = message.getPath();
        RepositoryEventContext.setCurrent(message.getRepositoryEventContext());
        previewDeployer.deployFile(site, path);
        RepositoryEventContext.setCurrent(null);
    }

    public void syncPath(String site, String relativePath, RepositoryEventContext repositoryEventContext) {
        RepositoryEventMessage message = new RepositoryEventMessage();
        message.setSite(site);
        message.setPath(relativePath);
        message.setRepositoryEventContext(repositoryEventContext);

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onSyncPath", RepositoryEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public RpcDispatcher getRpcDispatcher() { return rpcDispatcher; }
    public void setRpcDispatcher(RpcDispatcher rpcDispatcher) { this.rpcDispatcher = rpcDispatcher; }

    public PreviewDeployer getPreviewDeployer() { return previewDeployer; }
    public void setPreviewDeployer(PreviewDeployer previewDeployer) { this.previewDeployer = previewDeployer; }

    protected RpcDispatcher rpcDispatcher;
    protected PreviewDeployer previewDeployer;

    @Override
    public Object handle(Message msg) throws Exception {
        return null;
    }
}
