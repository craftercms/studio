/**
 * Preview Tools
 */
CStudioAuthoring.ContextualNav.PreviewToolsMod = CStudioAuthoring.ContextualNav.PreviewToolsMod || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		this.definePlugin();
		CStudioAuthoring.ContextualNav.PreviewToolsNav.init();		
	},
	
	definePlugin: function() {
		var YDom = YAHOO.util.Dom,
			YEvent = YAHOO.util.Event;
		/**
		 * WCM preview tools Contextual Nav Widget
		 */
		CStudioAuthoring.register({
			"ContextualNav.PreviewToolsNav": {
				init: function() {
					if(CStudioAuthoringContext.isPreview == true) {
						if(CStudioAuthoring.PreviewTools) {
							this.render();
						}
						else {
							cb = {
								moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
							   		try {
						   				if(!this.initialized) {
											CStudioAuthoring.PreviewTools.PreviewToolsOffEvent.subscribe(
									       			function() {
									       				var el = YDom.get("acn-preview-tools-container");
									       				el.children[0].src = CStudioAuthoringContext.authoringAppBaseUri + "/themes/cstudioTheme/images/tools_off_icon.png";
									       			});

											CStudioAuthoring.PreviewTools.PreviewToolsOnEvent.subscribe(
									       			function() {
									       				var el = YDom.get("acn-preview-tools-container");
									       				el.children[0].src = CStudioAuthoringContext.authoringAppBaseUri + "/themes/cstudioTheme/images/tools_icon.png";

									       			});

									       	CStudioAuthoring.PreviewTools.initialize(moduleConfig);
									       	this.self.render();
									       	this.self.initialized = true;
						   				}						   				
							   		} 
								   	catch (e) {
									}
								},
								
								self: this
							};
							
							CStudioAuthoring.Module.requireModule(
			                    "preview-tools-controller",
			                    '/static-assets/components/cstudio-preview-tools/preview-tools.js',
			                    0,
			                    cb
			                );
						}
					}					
				},
				
				render: function() {
					var el, containerEl, imageEl, ptoOn;

					el = YDom.get("acn-preview-tools");
					containerEl = document.createElement("div");
					containerEl.id = "acn-preview-tools-container";
					
					imageEl = document.createElement("img");
					imageEl.id = "acn-preview-tools-image";

                    ptoOn = !!(sessionStorage.getItem('pto-on'));   // cast string value to a boolean

                    imageEl.src = (ptoOn) ? CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/tools_icon.png" :
                                        CStudioAuthoringContext.authoringAppBaseUri + "/static-assets/themes/cstudioTheme/images/tools_off_icon.png";

					containerEl.appendChild(imageEl);
					el.appendChild(containerEl);

					containerEl.onclick = function() {
					    var ptoOn = !!(sessionStorage.getItem('pto-on'));

						if(!ptoOn) {
							CStudioAuthoring.PreviewTools.turnToolsOn();
						}
						else {
							CStudioAuthoring.PreviewTools.turnToolsOff();
						}
					}
					
					containerEl.onClick.containerEl = containerEl;
				}
			}
		});
	}
}

CStudioAuthoring.Module.moduleLoaded("preview_tools", CStudioAuthoring.ContextualNav.PreviewToolsMod);
