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

            YDom.addClass(dropdownInnerEl, 'studio-view');
            YDom.addClass(siteSelectorEl, 'form-control');

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
				sites.push({name: "All Sites", siteId: "_ALL_SITES_"});

				for (var i=0; i < sites.length; i++) {
					var curSite = sites[i];
					
					if(curSite != undefined) {
						var option = document.createElement("option");
				
						option.text = curSite.name;
						option.value = curSite.siteId;
						sitesNavSelect.options.add(option);
				
						// Find out what dashboard we are on and select based on that.
						if (curSite.siteId == CStudioAuthoringContext.site){
							sitesNavSelect.selectedIndex = i;
						}
					}
				}
				
				sitesNavSelect.onchange = function() {
					var sitesNavSelect = document.getElementById("acn-site-dropdown");
					var selectedIndex = sitesNavSelect.selectedIndex;
					var shortName = sitesNavSelect.options[selectedIndex].value;

					// set the cookie for preview and then redirect
					if(shortName != "_ALL_SITES_") {
						CStudioAuthoring.Utils.Cookies.createCookie("crafterSite", shortName);
						window.location = CStudioAuthoringContext.authoringAppBaseUri + "/preview?site="+shortName;
					}
					else {
						CStudioAuthoring.Utils.Cookies.createCookie("crafterSite", shortName);
						window.location = CStudioAuthoringContext.authoringAppBaseUri + "/#/sites";
					}						
				};
			},
			failure: function() {
			}
		});
	}
}

CStudioAuthoring.Module.moduleLoaded("site-selector", CStudioAuthoring.ContextualNav.SiteSelector);
