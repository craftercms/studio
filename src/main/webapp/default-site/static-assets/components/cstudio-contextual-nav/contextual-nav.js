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
		
		if(!this.intitialized) {
			this.intitialized = true;
			
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
			success: function(content) {

				CStudioAuthoring.ContextualNav.addNavContent(navContent); 
				
				YAHOO.util.Event.onAvailable("authoringContextNavHeader", function() {
                    document.domain=CStudioAuthoringContext.cookieDomain;
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
		
		var _body = document.getElementsByTagName('body')[0];

		var hdrDiv = document.createElement("div");
		hdrDiv.id = "controls-overlay";

		hdrDiv.innerHTML = navHtmlContent;

		var br = document.createElement("br");
		YDom.insertBefore(br, YDom.getFirstChild(_body));
		YDom.insertBefore(document.createElement("br"), br);

		YDom.insertBefore(hdrDiv, YDom.getFirstChild(_body));

		CStudioAuthoring.Service.retrieveContextNavConfiguration("default", {
			success: function(config) {
				this.context.buildModules(config)
			},
			
			failure: function() {
			},
			
			context: this
		});
	},

    /**
     * given a dropdown configuration, build the nav
     */
    buildModules: function(navConfig) {
		
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
