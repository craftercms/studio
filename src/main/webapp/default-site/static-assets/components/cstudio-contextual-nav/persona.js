/**
 * Preview Tools
 */
CStudioAuthoring.ContextualNav.PersonaNavMod = CStudioAuthoring.ContextualNav.PersonaNavMod || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		this.definePlugin();
		CStudioAuthoring.ContextualNav.PersonaNav.init();		
	},
	
	definePlugin: function() {
		var YDom = YAHOO.util.Dom,
			YEvent = YAHOO.util.Event;
		/**
		 * WCM preview tools Contextual Nav Widget
		 */
		CStudioAuthoring.register({
			"ContextualNav.PersonaNav": {
				init: function() {
					if(CStudioAuthoringContext.isPreview == true) {
						this.render();
					}					
				},
				
				render: function() {
					var el, containerEl, imageEl, ptoOn;

					el = YDom.get("acn-persona");
					containerEl = document.createElement("div");
					containerEl.id = "acn-persona-container";
					
					imageEl = document.createElement("img");
					imageEl.id = "acn-persona-image";

					var serviceUri = "/api/1/profile/get";
					
					var serviceCallback = {
						success: function(oResponse) {
							var json = oResponse.responseText;
	
							try {
								var currentProfile = eval("(" + json + ")");
								
									CStudioAuthoring.Service.lookupConfigurtion(
									CStudioAuthoringContext.site, 
									"/targeting/personas/personas-config.xml", {
										success: function(response) {

                                            var config;
                                            var persona;
											
											/*if(!config.length) {
												config = [ config.persona ];
											}*/

                                            config = response.persona;

                                            for (var i = 0; i < config.length; i++) {
                                                if (config[i].name.toLowerCase() == currentProfile.username.toLowerCase()) {
                                                    persona = config[i];
                                                    break;
                                                }
                                            }

                                            if (!persona) {
                                                for (var i = 0; i < config.length; i++) {
                                                    if (config[i].name.toLowerCase() == "anonymous") {
                                                        persona = config[i];
                                                        break;
                                                    }
                                                }
                                            }

                                            imageEl.title = persona.name;
                                            imageEl.src = CStudioAuthoringContext.baseUri + '/api/1/services/api/1/content/get-content-at-path.bin?path=/cstudio/config/sites/' + CStudioAuthoringContext.site + "/targeting/personas/thumbs/" + persona.thumb;

										},
										
										failure: function() {
										}
									});
							} catch(err) { }
						},
						failure: function(response) {}
					};
	
					YConnect.asyncRequest('GET', serviceUri, serviceCallback);

					containerEl.appendChild(imageEl);
					el.appendChild(containerEl);

				}
			}
		});
	}
}

CStudioAuthoring.Module.moduleLoaded("persona", CStudioAuthoring.ContextualNav.PersonaNavMod);
