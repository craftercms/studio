/**
 * editor tools
 */
CStudioAuthoring.MediumPanel = CStudioAuthoring.MediumPanel || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		if(this.initialized == false) {
			
			this.initialized = true;
		}
	},
	
	render: function(containerEl, config) {

		var channels = (config.config.channels.length) ? config.config.channels : [ config.config.channels.channel ];
		
		var channelSelectEl = document.createElement("select");
		YAHOO.util.Dom.addClass(channelSelectEl, "acn-panel-dropdown");
		channelSelectEl.style.height = "20px";
		
		containerEl.appendChild(channelSelectEl);
		
		for(var i=0; i<channels.length; i++) {
			var label = channels[i].title;
			channelSelectEl.options[i] = new Option(label, ""+i, false, false);
		}

		channelSelectEl.onchange = function() {
			var selectedIndex = this.selectedIndex;
			var channel = channels[selectedIndex];
			CStudioAuthoringContext.channel = channel.value;
			
			var cb = {
					moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
				   		try {
				   			moduleClass.render();
				   		} 
					   	catch (e) {
						}
					},
					
					self: this
				};
				
				CStudioAuthoring.Module.requireModule(
                    "medium-panel-"+channel.value,
                    '/components/cstudio-preview-tools/mods/agent-plugins/'+channel.value+'/'+channel.value+'.js',
                    0,
                    cb
                );
			
		};
	}
}

CStudioAuthoring.Module.moduleLoaded("medium-panel", CStudioAuthoring.MediumPanel);