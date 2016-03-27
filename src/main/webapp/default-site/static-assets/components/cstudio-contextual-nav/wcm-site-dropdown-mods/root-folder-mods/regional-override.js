CStudioAuthoring.ContextualNav.WcmRootFolder.RegionalOverride = CStudioAuthoring.ContextualNav.WcmRootFolder.RegionalOverride || {
	Self: this,
	region: "",

	init: function(moduleConfig) {
		Self.region = moduleConfig.config.region;
		Self.filterFolders = this.filterFolders;
	 
	},

	_renderContextMenu: function(tree, target, p_aArgs, component, menuItems, oCurrentTextNode, isWrite) {

		var data = { 
		 	tree: tree,
		  	currentTextNode: oCurrentTextNode,
		  	region: Self.region
		};

		p_aArgs.addItems([ menuItems.separator ]);
		p_aArgs.addItems([ { text: CMgs.format(siteDropdownLangBundle, "Add Regional Content"), onclick: 
			{ fn: CStudioAuthoring.ContextualNav.WcmRootFolder.RegionalOverride.createRegionalContent, obj:data 

			}}]);
	},

    createRegionalContent: function(action, evt, data) {

	    var createCb = {
	        success: function() {
	            this.callingWindow.location.reload(true);
	        },
	        failure: function() { },
	        callingWindow: window
	    };

	    CStudioAuthoring.Operations.createNewContent(
	            CStudioAuthoringContext.site,
	            oCurrentTextNode.data.path + "/" + data.region,
	            false,
	            createCb);
     },

	filterItem: function(treeItem) {
		var filterFolders =["lac", "usa", "euro"];

	    for (var i = 0; i < filterFolders.length; i++) {
    	    if (filterFolders[i] === treeItem.name) {
    	   		return true;
        	}	
    	}

		return false;
	},

	drawTreeItem: function(treeNodeTO, root, treeNode) {
		return treeNode;
	}
}            
            
CStudioAuthoring.Module.moduleLoaded("regional-override", CStudioAuthoring.ContextualNav.WcmRootFolder.RegionalOverride);
