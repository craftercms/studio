package org.craftercms.studio.impl.v1.ebus;


import org.craftercms.core.service.CacheService;
import org.craftercms.core.util.cache.CacheTemplate;
import org.craftercms.studio.api.v1.constant.CStudioConstants;
import org.craftercms.studio.api.v1.ebus.ClearCacheEventMessage;
import org.craftercms.studio.api.v1.ebus.RepositoryEventContext;
import org.craftercms.studio.api.v1.log.Logger;
import org.craftercms.studio.api.v1.log.LoggerFactory;
import org.craftercms.studio.api.v1.service.GeneralLockService;
import org.craftercms.studio.api.v1.service.security.SecurityProvider;
import org.craftercms.studio.api.v1.service.site.SiteService;
import org.craftercms.studio.api.v1.util.DebugUtils;
import org.craftercms.studio.impl.v1.service.StudioCacheContext;
import org.jgroups.JChannel;
import org.jgroups.blocks.MethodCall;
import org.jgroups.blocks.RequestOptions;
import org.jgroups.blocks.RpcDispatcher;

public class ClearConfigurationCache {

    private final static Logger logger = LoggerFactory.getLogger(ClearConfigurationCache.class);

    public ClearConfigurationCache() throws Exception {
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

    public void onClearConfigurationCache(ClearCacheEventMessage message) {
        logger.info("ClearConfigurationCache event invoked for site" + message.getSite());
        String site = message.getSite();
        CacheService cacheService = cacheTemplate.getCacheService();
        StudioCacheContext cacheContext = new StudioCacheContext(site, true);
        cacheService.clearScope(cacheContext);
        cacheContext = new StudioCacheContext(CStudioConstants.CACHE_GLOBAL_SCOPE, true);
        cacheService.clearScope(cacheContext);
        cacheContext = new StudioCacheContext(CStudioConstants.CACHE_USERS_SCOPE, true);
        cacheService.clearScope(cacheContext);
        String ticket = securityProvider.authenticate(adminUser, adminPassword);
        RepositoryEventContext repositoryEventContext = new RepositoryEventContext(ticket);
        RepositoryEventContext.setCurrent(repositoryEventContext);
        siteService.reloadSiteConfiguration(site, false);
        RepositoryEventContext.setCurrent(null);
    }

    public void clearConfigurationCache(String site) {
        ClearCacheEventMessage message = new ClearCacheEventMessage(site);
        message.setSite(site);

        try {
            MethodCall call = new MethodCall(getClass().getMethod("onClearConfigurationCache", ClearCacheEventMessage.class));
            call.setArgs(message);
            rpcDispatcher.callRemoteMethods(null, call, RequestOptions.ASYNC());
        } catch (NoSuchMethodException e) {
            logger.error("Error invoking Clear Configuration Cache event", e);
        } catch (Exception e) {
            logger.error("Error invoking Clear Configuration Cache event", e);
        }
    }

    public RpcDispatcher getRpcDispatcher() { return rpcDispatcher; }
    public void setRpcDispatcher(RpcDispatcher rpcDispatcher) { this.rpcDispatcher = rpcDispatcher; }

    public SiteService getSiteService() { return siteService; }
    public void setSiteService(SiteService siteService) { this.siteService = siteService; }

    public CacheTemplate getCacheTemplate() { return cacheTemplate; }
    public void setCacheTemplate(CacheTemplate cacheTemplate) { this.cacheTemplate = cacheTemplate; }

    public SecurityProvider getSecurityProvider() { return securityProvider; }
    public void setSecurityProvider(SecurityProvider securityProvider) { this.securityProvider = securityProvider; }

    public String getAdminUser() { return adminUser; }
    public void setAdminUser(String adminUser) { this.adminUser = adminUser; }

    public String getAdminPassword() { return adminPassword; }
    public void setAdminPassword(String adminPassword) { this.adminPassword = adminPassword; }

    public GeneralLockService getGeneralLockService() { return generalLockService; }
    public void setGeneralLockService(GeneralLockService generalLockService) { this.generalLockService = generalLockService; }

    protected SecurityProvider securityProvider;
    protected RpcDispatcher rpcDispatcher;
    protected SiteService siteService;
    protected CacheTemplate cacheTemplate;
    protected String adminUser;
    protected String adminPassword;
    protected GeneralLockService generalLockService;
}
