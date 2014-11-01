var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;


/**
 * PreviewSync
 */
CStudioAuthoring.ContextualNav.PreviewSync = CStudioAuthoring.ContextualNav.PreviewSync || {

	/**
	 * initialize module
	 */
	initialize: function(config) {

		if(config.name == "previewsync") {			
            this.initialized = true;
            var dropdownInnerEl = config.containerEl;

            var parentFolderEl = document.createElement("div");
            parentFolderEl.style.paddingTop = "8px";
            var parentFolderLinkEl = document.createElement("a");
            parentFolderEl.appendChild(parentFolderLinkEl);
            YDom.addClass(parentFolderLinkEl, "acn-previewsync");

            parentFolderLinkEl.id = "previewsync";
            parentFolderLinkEl.innerHTML = "Preview Sync";
            parentFolderLinkEl.onclick = function() {
                CStudioAuthoring.Service.previewServerSyncAll(CStudioAuthoringContext.site, {
                   success: function() {
                      alert("Preview server synch-all initiated.");
                   },
                   failure: function() {}
                });
            };

            dropdownInnerEl.appendChild(parentFolderEl);
	    }
    }
}
CStudioAuthoring.Module.moduleLoaded("previewsync", CStudioAuthoring.ContextualNav.PreviewSync);
