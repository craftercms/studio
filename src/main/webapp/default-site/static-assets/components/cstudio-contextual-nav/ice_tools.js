/**
 * WCM Search Plugin
 */
CStudioAuthoring.ContextualNav.IceToolsMod = CStudioAuthoring.ContextualNav.IceToolsMod || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		this.definePlugin();
		CStudioAuthoring.ContextualNav.EditorsToolsNav.init();		
	},
	
	definePlugin: function() {
		var YDom = YAHOO.util.Dom,
			YEvent = YAHOO.util.Event;
		/**
		 * WCM editor tools Contextual Nav Widget
		 */
		CStudioAuthoring.register({
			"ContextualNav.EditorsToolsNav": {
				init: function() {
					if(CStudioAuthoringContext.isPreview == true) {
						if(CStudioAuthoring.IceTools) {
							this.render();

					       	CStudioAuthoring.IceTools.IceToolsOffEvent.subscribe(
					       			function() {
					       				var el = YDom.get("acn-ice-tools-container");
					       				el.children[0].src = CStudioAuthoringContext.authoringAppBaseUri + "/themes/cstudioTheme/images/edit_off.png";
					       			});

					       	CStudioAuthoring.IceTools.IceToolsOnEvent.subscribe(
					       			function() {
					       				var el = YDom.get("acn-ice-tools-container");
					       				el.children[0].src = CStudioAuthoringContext.authoringAppBaseUri + "/themes/cstudioTheme/images/edit.png";
					       			});

//						}
//						else {
							cb = {
								moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
							   		try {
						   					
									       	CStudioAuthoring.IceTools.initialize(moduleConfig);
									       	if(this.self.initialized == false) {
									       		this.self.render();
									       	}
									       	
									       	this.self.initialized = true;

									       	CStudioAuthoring.IceTools.IceToolsOffEvent.subscribe(
									       			function() {
									       				var el = YDom.get("acn-ice-tools-container");
									       				el.children[0].src = CStudioAuthoringContext.authoringAppBaseUri + "/themes/cstudioTheme/images/edit_off.png";
									       			});

									       	CStudioAuthoring.IceTools.IceToolsOnEvent.subscribe(
									       			function() {
									       				var el = YDom.get("acn-ice-tools-container");
									       				el.children[0].src = CStudioAuthoringContext.authoringAppBaseUri + "/themes/cstudioTheme/images/edit.png";
									       			});

											CStudioAuthoring.Module.requireModule(
								                    "preview-tools-controller",
								                    '/components/cstudio-preview-tools/preview-tools.js',
								                    0,
								                    {
								                    	moduleLoaded: function(moduleName, moduleClass, moduleConfig) {

													       	CStudioAuthoring.PreviewTools.PreviewToolsOffEvent.subscribe(
													       			function() {
													       				CStudioAuthoring.IceTools.turnEditOff();
													       			});
								                    	}
								                    });
							   		} 
								   	catch (e) {
									}
								},
								
								self: this
							};
							
							CStudioAuthoring.Module.requireModule(
			                    "ice-tools-controller",
			                    '/components/cstudio-preview-tools/ice-tools.js',
			                    0,
			                    cb
			                );
						}
					}					
				},
				
				render: function() {
				    var el, containerEl, imageEl, iceOn;

					
					el = YDom.get("acn-ice-tools");
					containerEl = document.createElement("div");
					containerEl.id = "acn-ice-tools-container";

					imageEl = document.createElement("img");
					imageEl.id = "acn-ice-tools-image";

					iceOn = !!(sessionStorage.getItem('ice-on'));   // cast string value to a boolean

                    imageEl.src = (iceOn) ? CStudioAuthoringContext.authoringAppBaseUri + "/themes/cstudioTheme/images/edit.png" :
                                        CStudioAuthoringContext.authoringAppBaseUri + "/themes/cstudioTheme/images/edit_off.png";

					containerEl.appendChild(imageEl);
					el.appendChild(containerEl);

					containerEl.onclick = function() {
					    var iceOn = !!(sessionStorage.getItem('ice-on'));   // cast string value to a boolean

						if(!iceOn) {
							CStudioAuthoring.IceTools.turnEditOn();
						}
						else {
							CStudioAuthoring.IceTools.turnEditOff();
						}
					}
				}
			}
		});
	}
}

CStudioAuthoring.Module.moduleLoaded("ice_tools", CStudioAuthoring.ContextualNav.IceToolsMod);
