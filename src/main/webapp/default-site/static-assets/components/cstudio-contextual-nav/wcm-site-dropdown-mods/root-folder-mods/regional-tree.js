CStudioAuthoring.ContextualNav.WcmRootFolder.RegionalTree = CStudioAuthoring.ContextualNav.WcmRootFolder.RegionalTree || {
	Self: this,
	readyToLoad: false,
	region: "",

	init: function(moduleConfig) {
		Self.region = moduleConfig.config.region;
		Self.filterPaths = [];
		Self.filterFolders = this.filterFolders;

		var cb = { 
			success: function(results) {
				for(var i=0; i<results.objectList.length; i++) {
					var item = results.objectList[i].item;
					var pathOfItem = item.localId.replace("/index.xml","");
					
					if(this._self.filterPaths.indexOf(pathOfItem) == -1) {
						// this path is new, add it
						this._self.filterPaths[this._self.filterPaths.length] = pathOfItem;
					}
				}

				this._self.RegionalTree.readyToLoad = true;
			},
			failure: function(err) {
				console.log("error getting paths for region. err:"+err);
			},
			_self: Self
		};

		CStudioAuthoring.Service.search(CStudioAuthoringContext.site, {
                searchType: "",
                sortBy: "",
                page: 1,
                sortAscending: true,
                pageSize: 1000000,
                includeAspects: new Array(),
                excludeAspects: new Array(),
                contentTypes:new Array(),
                filters: [ {qname: "localId", value: "*/" + Self.region + "/*"} ]
			}, cb);
	},

	_renderContextMenu: function(tree, target, p_aArgs, component, menuItems, oCurrentTextNode, isWrite) {
	},

	sortTreeItems: function(treeItems) {
		treeItems.sort(function(a, b){
			var itemPathA = a.uri;
			var itemPathB = b.uri;

			if(itemPathA.indexOf("/"+Self.region)) {
				return 1;
			}
			else {
				return -1;
			}
		});

		return treeItems;
	},

	filterItem: function(treeItem) {
		var filterItem = false;
		var itemPath = treeItem.uri.replace("/index.xml","");

		if(itemPath == "/site/website/wip" || itemPath.indexOf("/wip/"+Self.region) != -1) {
                	filterItem = false;
		}
		else if(itemPath.indexOf("/"+Self.region) != -1) {
			filterItem = false;
		}
        else if(Self.filterPaths.length != 0) {
		    // assume it's filtered
		    filterItem  = true;

		    for (var i = 0; i < Self.filterPaths.length; i++) {
		    	var filterPath = Self.filterPaths[i];

		    	if(filterPath.indexOf(itemPath) != -1) {
		    		filterItem = false;
		    		break;
		    	}
	    	}

		}
		
		return filterItem;
	},

	drawTreeItem: function(treeNodeTO, root, treeNode) {
		if(treeNodeTO.uri == "/site/website/wip") {
			treeNode.label = "Work in Progress";
		}
		else if(treeNodeTO.uri.indexOf("/site/website/wip/"+Self.region) != -1) {
			treeNode.label = Self.region.toUpperCase();
		}
		else if(treeNodeTO.fileName == Self.region) { 
			var label = (treeNode.parent.label) ? treeNode.parent.label.replace("*", "") : "ROOT"; 
			treeNode.label = Self.region.toUpperCase() + " " + label +  "Regional Content"
		}

		return treeNode;
	}
}            
      