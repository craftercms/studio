CStudioAdminConsole = {
	toolContainerEls: [],
	
	render: function(containerEl) {
		this.containerEl = containerEl;
		
		containerEl.innerHTML = 
				"<div class='categories-panel'></div>" +
				"<div id='cstudio-admin-console-workarea'></div>";

		CStudioAuthoring.Service.lookupConfigurtion(
			CStudioAuthoringContext.site, 
			"/administration/tools.xml", 
			{
				success: function(config) {
					var panelEl = YAHOO.util.Selector.query("#admin-console .categories-panel", null, true);
					this.context.toolbar = new CStudioAdminConsole.Toolbar(panelEl);

					this.context.buildModules(config, panelEl);
				},
				
				failure: function() {
				},
				
				context: this
			});	
	},

    buildModules: function(config, panelEl) {

    	amplify.subscribe("/content-type/loaded", function() {
    		var catEl = document.getElementById('admin-console');
    		catEl.className = "";	// Clear any classes
    		YDom.addClass(catEl, "work-area-active");
    	})

	    	if(!config.tools.tool.length) {
	    		config.tools.tool = [ config.tools.tool ];
	    	}
    	
		if(config.tools.tool.length) {
			
			for(var j=0; j<config.tools.tool.length; j++) {
				try {
					var toolContainerEl = document.createElement("div");
					this.toolContainerEls[this.toolContainerEls.length] = toolContainerEl;
 					panelEl.appendChild(toolContainerEl);
 					
 					if(j==0) {
	 					YDom.addClass(toolContainerEl, "cstudio-admin-console-item-first");
 					}
 					
			    	var cb = {
						moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
							try {
								var tool = new moduleClass(moduleConfig, this.toolContainerEl);
								tool.initialize(moduleConfig.config);
								this.context.toolbar.addToolbarItem(tool, this.toolContainerEl);
							} catch (e) {
							    // in preview, this function undefined raises error -- unlike dashboard.
						    	// I agree, not a good solution!
							}
						},
						
						context: this,
						toolContainerEl: toolContainerEl
					};

			
		    		CStudioAuthoring.Module.requireModule(
                		"cstudio-console-tools-" + config.tools.tool[j].name,
                    	'/static-assets/components/cstudio-admin/mods/' + config.tools.tool[j].name + ".js",
                    	{ config: config.tools.tool[j] },
                   	 	cb);
				}
				catch(err) { 
					//alert(err);
				}
			}
		}
    }
}

CStudioAdminConsole.Toolbar = function(containerEl) {
	this.containerEl = containerEl;
 	this.tools = [];
 	this.toolContainerEls = [];
 	return this;
}

CStudioAdminConsole.Toolbar.prototype = {
	addToolbarItem: function(tool, toolContainerEl) {
	   	toolContainerEl.innerHTML = CMgs.format(langBundle, tool.config.label);
	   	YDom.addClass(toolContainerEl, "cstudio-admin-console-item");
	   	
	   	var onRenderWorkAreaFn =  function(evt, params) {
	  		if(params.toolbar.selectedEl) {
		  		YDom.removeClass(params.toolbar.selectedEl, "cstudio-admin-console-item-selected");
	  		}
	  		
	  		params.toolbar.selectedEl = this;
		   	YDom.addClass(this, "cstudio-admin-console-item-selected");
			params.tool.renderWorkarea();
			var arrowEl = document.getElementById("cstudio-admin-console-item-selected-arrow");
			
			if(!arrowEl) {
				arrowEl =  document.createElement("div");
				arrowEl.id = "cstudio-admin-console-item-selected-arrow";
			}
			
			params.toolbar.selectedEl.appendChild(arrowEl);
		}

		onRenderWorkAreaFn.containerEl = toolContainerEl;
	   	YAHOO.util.Event.on(toolContainerEl, 'click', onRenderWorkAreaFn, {tool: tool, toolbar: this});

	   	this.tools[this.tools.length] = tool;
	}
}

CStudioAdminConsole.Tool = function() {

}

CStudioAdminConsole.Tool.prototype = {
	initialize: function(config) {
		this.config = config;		
	},

	renderWorkarea: function() {
	}
}


CStudioAdminConsole.CommandBar =  {
	
	render: function(actions) {
		if(!this.commandBarEl) {
			this.commandBarEl = document.createElement("div");
			this.commandBarEl.id = "cstudio-admin-console-command-bar";
			YDom.addClass(this.commandBarEl, "hidden");
						
			document.body.appendChild(this.commandBarEl);
		}

		this.hide();
		this.addActions(actions);
	},
	
	hide: function() {
		YDom.addClass(this.commandBarEl, "hidden");
	},

	show: function() {
		YDom.removeClass(this.commandBarEl, "hidden");
	}, 
	
	addActions: function(actions) {
		this.commandBarEl.innerHTML = "";
		
		if(actions.length > 0) {
			for(var i=0; i<actions.length; i++) {
				this.addAction(actions[i]);
			}
			
			this.show();
		}

	},
	
	addAction: function(action) {
		var buttonEl = document.createElement("input");
		YDom.addClass(buttonEl, "cstudio-button");
		buttonEl.type = "button";
		buttonEl.value = action.label;
		this.commandBarEl.appendChild(buttonEl);
		buttonEl.onclick = action.fn;
	}
}

YAHOO.util.Event.onAvailable('admin-console', function() {
   CStudioAdminConsole.render(document.getElementById('admin-console'));
});

