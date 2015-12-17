var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;


/**
 * GaAnalytics
 * A link to the GA Analyics Page
 */
CStudioAuthoring.ContextualNav.GaAnalytics = CStudioAuthoring.ContextualNav.GaAnalytics || {

	/**
	 * initialize module
	 */
	initialize: function(config) {

		if(config.name == "analytics-dashboard") {

			this.initialized = true;	
			var dropdownInnerEl = config.containerEl;

			var parentFolderEl = document.createElement("div");
			parentFolderEl.style.paddingTop = "8px";
			var parentFolderLinkEl = document.createElement("a");
			parentFolderEl.appendChild(parentFolderLinkEl);
			YDom.addClass(parentFolderLinkEl, "acn-analytics");

			parentFolderLinkEl.id = "analytics-dashboard";
			parentFolderLinkEl.innerHTML = CMgs.format(siteDropdownLangBundle, "analyticsDashboard");
			parentFolderLinkEl.onclick = function() {
				document.location = CStudioAuthoringContext.authoringAppBaseUri +
				"/analytics-dashboard?site=" + CStudioAuthoringContext.site;
			};
			
			dropdownInnerEl.appendChild(parentFolderEl);
		}
	}
}

CStudioAuthoring.Module.moduleLoaded("analytics-dashboard", CStudioAuthoring.ContextualNav.GaAnalytics);
