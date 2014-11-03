
CStudioForms.Controls.RTEManager = CStudioForms.Controls.RTEManager || {
	cachedConfig: new Array(),
	queuedConfigCallbacks: new Array(),
	inProcessCacheReqs: new Array(),
		
	cachedGenericConfig: new Array(),
	queuedConfigGenericCallbacks: new Array(),
	inProcessCacheGenericReqs: new Array(),
	rteConfigs: new Object(),
	rteConfigsInitialized: false,

	/**
	 * get cached configuration
	 */
	getRteConfiguration: function(setupId, context, callback) {
		var style = (setupId==null) ? "generic" : setupId;
		var cacheKey = "all";
		
		var cachedResponse = this.cachedConfig[cacheKey];
		
		if(!cachedResponse) {
			
			if(!this.inProcessCacheReqs[cacheKey]) {
			
				this.inProcessCacheReqs[cacheKey] = true;
			
				// queue the first req
				this.queuedConfigCallbacks[cacheKey] = new Array();
                //To Keep the correct style for each callback
				this.queuedConfigCallbacks[cacheKey].push({callback:callback, style: style});
				
				// create callback
				cacheCb = {
					context: context,
					configMgr: this,

					success: function(config) {

						this.configMgr.cachedConfig[cacheKey] = config;
						
						this.configMgr.inProcessCacheReqs[cacheKey] = false;
						
						var queuedCbs = this.configMgr.queuedConfigCallbacks[cacheKey];
						
						for(var i=0; i<queuedCbs.length; i++) {
                            var cb = queuedCbs[i].callback;
                            var style = queuedCbs[i].style;
							
							if(cb && cb.success) {
								var setup;
								
								// find the right form
								if(config.setup.length) { 
									for(var j=0; j<config.setup.length; j++) {
										if(config.setup[j].id == style) {
											setup = config.setup[j];
											break;
										}
									}
								}
								else {
									setup = config.setup;
								}

								cb.success(setup);
							}
						}
						
						this.configMgr.queuedConfigCallbacks[cacheKey] = new Array();
					},
					
					failure: function() {
                        var queuedCbs = this.configMgr.queuedConfigCallbacks[cacheKey];

                        for(var i=0; i<queuedCbs.length; i++) {
                            var cb = queuedCbs[i].callback;

                            if(cb && cb.failure) {
                                cb.failure();
                            }
                        }
					}					
				};
			
				CStudioAuthoring.Service.lookupConfigurtion(
					CStudioAuthoringContext.site, 
					"/form-control-config/rte/rte-setup.xml",
					cacheCb); 
			}
			else {
				if(!this.queuedConfigCallbacks[cacheKey]) {
					this.queuedConfigCallbacks[cacheKey] = new Array();
					this.queuedConfigCallbacks[cacheKey].push({callback:callback, style: style});
				}
				else {
					this.queuedConfigCallbacks[cacheKey].push({callback:callback, style: style});
				} 
			}
		}
		else {
			var setup;
			var config = cachedResponse;
			if(config.setup.length) { 
				for(var j=0; j<config.setup.length; j++) {
					if(config.setup[j].id == style) {
						setup = config.setup[j];
						break;
					}
				}
			}
			else {
				setup = config.setup;
			}
		
			callback.success(setup);
		}
	}
};

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-rte-config-manager", CStudioForms.Controls.RTEManager);