var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

/**
 * contextual nav
 */
CStudioAuthoring.ContextualNav = CStudioAuthoring.ContextualNav || {

	initialized: false,

	/**
	 * call out to the authoring environment for the nav content and overlay it
	 * on success.
	 */
	hookNavOverlayFromAuthoring: function() {
		if(!this.initialized) {
			this.initialized = true;
			this.updateContextualNavOverlay();
		}
	},

	/**
	 * Add the contextual navigation overlay / authoring support over 
	 * top of the existing page
	 * @param content to overlay
	 */
	updateContextualNavOverlay: function(context) {

		context = (context) ? context : CStudioAuthoringContext.navContext;
		CStudioAuthoring.Service.retrieveContextualNavContent(context, {
			success: function(navContent) {
				CStudioAuthoring.ContextualNav.addNavContent(navContent);
				YAHOO.util.Event.onAvailable("authoringContextNavHeader", function() {
                    document.domain = CStudioAuthoringContext.cookieDomain;
					CStudioAuthoring.Events.contextNavReady.fire();
				}, this);
			},
			failure: function() {
				YAHOO.log("Failed to hook context nav", "error", "authoring nav callback");
			}
		});
	},	

	/**
	 * add the contextual nav to the page - first time call
	 */
	addNavContent: function(navHtmlContent) {

		var bar = document.createElement("div");

		bar.id = "controls-overlay";
		bar.innerHTML = navHtmlContent;

		CStudioAuthoring.Service.retrieveContextNavConfiguration("default", {
			success: function(config) {
				var me = this;
				var $ = jQuery || function(fn) { fn() };
				$(function () {
					document.body.appendChild(bar);
					me.context.buildModules(config);
				});
			},
			failure: function() {},
			context: this
		});
	},

    /**
     * given a dropdown configuration, build the nav
     */
    buildModules: function(navConfig) {
		// TODO console.log(navConfig.modules.module)
		if(navConfig.modules.module.length) {
			for(var i=0; i<navConfig.modules.module.length; i++) {
				var module = navConfig.modules.module[i];
				 
				var cb = {
					moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
						try {
						    moduleClass.initialize(moduleConfig);
						} catch (e) {
						    // in preview, this function undefined raises error -- unlike dashboard.
						    // I agree, not a good solution!
						}
					}
				};
				
                CStudioAuthoring.Module.requireModule(
                    module.moduleName,
                    '/static-assets/components/cstudio-contextual-nav/' + module.moduleName + ".js",
                    0,
                    cb
                );
			}
		}
    }		
};

CStudioAuthoring.Events.contextNavLoaded.fire();
