var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;

/**
 * Site selector
 */
CStudioAuthoring.ContextualNav.CannedSearch = CStudioAuthoring.ContextualNav.CannedSearch || {

	initialized: false,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		
		
		if(!this.initialized) {
			this.initialized = true;
				
			var folderListEl = YDom.get("acn-dropdown-menu-inner");
			
			var parentFolderEl = document.createElement("div");   
	
			var parentFolderLinkEl = document.createElement("a");
			parentFolderLinkEl.innerHTML = "<br/>&nbsp;&nbsp;+&nbsp;&nbsp;&nbsp;Downloads Canned Search Example";
			parentFolderLinkEl.onclick = function() { 
				var url = CStudioAuthoringContext.authoringAppBaseUri + "/page" +
					"/site/" + CStudioAuthoringContext.site +
					"/cstudio-wcm-search?context=download&selection=-1&mode=act";
		     
					window.location = url;	
				 }; 
			
			folderListEl.appendChild(parentFolderEl);
			parentFolderEl.appendChild(parentFolderLinkEl);
		}
	}
}

CStudioAuthoring.Module.moduleLoaded("cannedSearch", CStudioAuthoring.ContextualNav.CannedSearch);
