var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

/**
 * Site selector
 */
CStudioAuthoring.ContextualNav.SiteSelector = CStudioAuthoring.ContextualNav.SiteSelector || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
	    if(config.name == "site-selector") {
		if(!this.initialized) {
			this.initialized = true;
				
			var dropdownInnerEl = config.containerEl;
			
			var siteSelectorEl = document.createElement("select");
			siteSelectorEl.id = "acn-site-dropdown";
			siteSelectorEl.style.height = "20px";
			
			dropdownInnerEl.appendChild(siteSelectorEl);

			/* check first child of dropdown element,
			 * site select drop down should alway at top.
			 */
			if (dropdownInnerEl.firstChild != null) {
				dropdownInnerEl.insertBefore(siteSelectorEl, dropdownInnerEl.firstChild);
			} else {
				dropdownInnerEl.appendChild(siteSelectorEl);
			}
			
			this.populateSiteDropdownMenu(siteSelectorEl);
		}
	    }
	},

	/**
	 * populate the sites dropdown menu
	 */
	populateSiteDropdownMenu: function(dropdownInnerEl) {
		var sitesNavSelect = dropdownInnerEl;

		var sites = CStudioAuthoring.Service.retrieveSitesList({
			success: function(sites) {
				sites.push({label: "My Dashboard", link: ""});

				for (var i=0; i < sites.length; i++) {
					var curSite = sites[i];
					
					if(curSite != undefined) {
						var option = document.createElement("option");
				
						option.text = "View: " + curSite.label;
						option.value = CStudioAuthoringContext.authoringAppBaseUri + curSite.link;
						sitesNavSelect.options.add(option);
				
						// Find out what dashboard we are on and select based on that.
						if (curSite.link.indexOf(CStudioAuthoringContext.homeUri) != -1){
							sitesNavSelect.selectedIndex = i;
						}
					}
				}
				
				sitesNavSelect.onchange = function() {
					var sitesNavSelect = document.getElementById("acn-site-dropdown");
					var selectedIndex = sitesNavSelect.selectedIndex;
					var link = sitesNavSelect.options[selectedIndex].value;
					window.location =link;
				};
			},
			failure: function() {
			}
		});
	}
}

CStudioAuthoring.Module.moduleLoaded("site-selector", CStudioAuthoring.ContextualNav.SiteSelector);
