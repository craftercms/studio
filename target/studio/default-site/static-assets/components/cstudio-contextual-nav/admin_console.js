/**
 * PAdminConsoleMod
 */
CStudioAuthoring.ContextualNav.AdminConsoleMod = CStudioAuthoring.ContextualNav.AdminConsoleMod || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		this.definePlugin();
		CStudioAuthoring.ContextualNav.AdminConsoleNav.init();		
	},
	
	definePlugin: function() {
		var YDom = YAHOO.util.Dom,
			YEvent = YAHOO.util.Event;

		CStudioAuthoring.register({
			"ContextualNav.AdminConsoleNav": {
				init: function() {
					if(CStudioAuthoringContext.isAuthoringConsole == true) {
						this.render();
					}					
				},
				
				render: function() {
					document.getElementById('acn-dropdown').style.display = 'none';
					document.getElementById('acn-search').style.display = 'none';
				},
				
				clearActions: function() {
					document.getElementById('acn-admin-console').innerHTML = ''; 
				},
				
				initActions: function(actions) {
					this.clearActions();
					
					var containerEl = document.getElementById('acn-admin-console');
					
					for(var i=0; i<actions.length; i++) {
						var action = actions[i];						
                    	var linkContainerEl = document.createElement("div");
                    	var linkEl = document.createElement("a");
                                        
                    	YDom.addClass(linkContainerEl, "acn-link");
                    	linkEl.innerHTML = action.name;
                    	YDom.addClass(linkEl, "cursor");
                    	linkEl.style.cursor = 'pointer';
                    	
                    	linkContainerEl.appendChild(linkEl);
                    	containerEl.appendChild(linkContainerEl);
                    	
                    	YAHOO.util.Event.on(linkEl, 'click', function(evt, param) { param.method() }, { method: action.method, context: action.context });
                    	
                    	
					}
				}
			}
		});
	}
}

CStudioAuthoring.Module.moduleLoaded("admin_console", CStudioAuthoring.ContextualNav.AdminConsoleMod);
