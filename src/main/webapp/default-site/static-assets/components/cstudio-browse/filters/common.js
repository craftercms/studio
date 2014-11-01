
/**
 * Most of CStudio's Filters are similar, this class encapsulates that similarity and
 * allows us to subclass it for specializaitons rather than duplicating code
 */
CStudioSearch.FilterRenderer.Common = function() { 
	var object = {
		title: "TITLE NOT SET",
		appliesToContentTypes: [ "" ],
		filterCols: [ ],

		/**
		 * return the title for the filter
		 */		
		getFilterTitle: function() {
			return this.self.title;
		},

		/*
		 * returns an array of content types to be target
		 */
		getAppliesToContentTypes: function() {
			return this.self.appliesToContentTypes;
		},

		/**
		 * returns an array of sort option objects
		 */
		getSortOptions: function() {
			return [];
		},

		/**
		 * render the filter body
		 * @returns HTML
		 */
		renderFilterBody: function(filterControlsEl, renderCb) {
		
			var filters = this.self.filterCols;
			
			var filterBodyRenderCb = {
				success: function() {
					this.callbackRecieved++;

					if(this.callbackRecieved >= this.filters.length) {
						this.renderCb.success();
					}
				},
				
				failure: function() {
				},
				
				renderCb: renderCb,
				filters: filters,
				callbackRecieved: 0
			};
			
			if(filters.length && filters.length > 0) {
				for(var i=0; i<filters.length; i++) {
					filters[i].render(filterControlsEl, filterBodyRenderCb);
				}
			}
			else {
				filterBodyRenderCb.success();
			}
		},

		/**
	 	 * populate search context with filter values from url
	 	 * returns context
	  	 */
		prepareSearchFilterFromUrl: function() {
			var queryString = document.location.search;

			var filters = this.self.filterCols;
			
			for(var i=0; i<filters.length; i++) {
				filters[i].prepareColFromUrl(queryString);
			}
   		},

		/**
		 * populate search context with filter values
		 * returns context
		 */
		augmentContextWithActiveFilter: function(searchContext) {

			searchContext.contentTypes = this.self.contentTypes;
			searchContext.includeAspects = this.self.includeAspects;
			searchContext.excludeAspects = this.self.excludeAspects;
			
			var filters = this.self.filterCols;
			
			for(var i=0; i<filters.length; i++) {
				searchContext = filters[i].augmentContextWithCol(searchContext);
			}

			return searchContext;
		}
		
	};
	
	object.self = object;
	return object;
}




/** 
 * common base for all colums
 */
CStudioSearch.FilterRenderer.Common.BaseCol = function() {
	return {

		/** 
		 * render col, base does nothing 
		 */
		render: function() {
		},
		
		/**
		 * translate url parms in to set col values
		 */
		prepareColFromUrl: function(queryString) {
		},
		
		/**
		 * add filters etc for the given column
		 */
		augmentContextWithCol: function(searchContext) {
			return searchContext;
		},
		
		/**
		 * create a column for controls in the filter
		 */
		createFilterColEl: function(label) {
			var colEl = document.createElement("div");
			YDom.addClass(colEl, "cstudio-wcm-searchfilter-col");
			
			var colLabelEl = document.createElement("div");
			if (label && label.trim() != "") 
				colLabelEl.innerHTML = label + ": ";
			else 
				colLabelEl.innerHTML = "&nbsp;";
			
			
			colEl.appendChild(colLabelEl);
			
			return colEl;
		},

		/**
		 * create a checkbox 
		 */
		createCheckboxEl: function(label, family, id, defaultChecked) {
			var containerEl  = document.createElement("span");
			var labelEl = document.createElement("span");
			YDom.addClass(labelEl, "cstudio-wcm-searchfilter-chkbox-label");

			var checkboxEl = document.createElement("input");
			checkboxEl.type = "checkbox";
			checkboxEl.id = id;
			checkboxEl.checked = defaultChecked;
			checkboxEl.name = family;
			
			labelEl.innerHTML = label;
			
			containerEl.inputEl = checkboxEl;
			containerEl.appendChild(checkboxEl);
			containerEl.appendChild(labelEl);
			
			return containerEl;
		}

	};
}

CStudioSearch.FilterRenderer.Common.PathSelect = function(rootPath) {

	var column = new CStudioSearch.FilterRenderer.Common.BaseCol();
	column.self = column;
	column.rootPath = rootPath;
	column.myTree = null;
	column.counter = 0;
	
	/**
	 * render filter
	 */
	column.render = function(filtersContainerEl, renderCb) {
		var pathColEl = this.self.createFilterColEl("");
		filtersContainerEl.appendChild(pathColEl);

		var buildFilterCallback = {
			success: function(controlEl) {
				this.containerEl.appendChild(controlEl);
				this.parent.pathEl = controlEl;
				this.renderCb.success();
			},
			
			failure: function() {
			},
			
			parent: this.self,
	
			containerEl: pathColEl,
			
			renderCb: renderCb
		};

		this.self._renderPathControl(buildFilterCallback);	
	};

	/**
	 * prepare from url 
	 */
	column.prepareColFromUrl = function(queryString) {

		var value = CStudioAuthoring.Utils.getQueryVariable(queryString, "PATH");
	
		if((!value) || value == "") {
			this.self.inputEl.value = "";
		} 
		else {
			this.self.inputEl.value = new String(value);
		}
	}
	
	/**
	 * add filters etc for the given column
	 */
	column.augmentContextWithCol = function(searchContext) {
		
		var value = this.self.inputEl.value;
		
		if (value != null && value != "") {
			searchContext.filters.push({ qname: "PATH",  value: value });
		}
		
		return searchContext;
	}

	/**
	 * build the language drop down
	 */
	column._renderPathControl = function(buildCallback) {
		var fieldEl = document.createElement("div");
		var inputEl = document.createElement("input");
		var dropdownEl = document.createElement("div");
		var foldersEl = document.createElement("div");
		var contextMenuEl = document.createElement("div");
		contextMenuEl.id = "ContextmenuWrapper0";
		
		fieldEl.inputEl = inputEl;
		fieldEl.appendChild(inputEl);		
		fieldEl.dropdownEl = dropdownEl;
		fieldEl.appendChild(dropdownEl);
		dropdownEl.appendChild(foldersEl);
		dropdownEl.appendChild(contextMenuEl);

		dropdownEl.className = "cs-folder-tree";
		inputEl.style.display = "none";
	
		var self = this.self;
		self.inputEl = inputEl;
		self.dropdownEl = dropdownEl;
		self.initializeContentTree(foldersEl, self);	
		
		buildCallback.success(fieldEl);
	};

	/**
	* initialize the content tree for the dropdown.
	* There are many methods involved, but it all starts here.
	*/
	column.initializeContentTree = function(treeEl, instance) {
		var queryString = document.location.search;
		var site = CStudioAuthoringContext.site;
		var rootPath = CStudioAuthoring.Utils.getQueryVariable(queryString, "PATH");
		var pathToOpen = (this.self.defaultPath != undefined) ? this.self.defaultPath : null;
		
		var tree = instance.tree = new YAHOO.widget.TreeView(treeEl);
		tree.setDynamicLoad(this.onLoadNodeDataOnClick);
		tree.FOCUS_CLASS_NAME = null;
		
		var label = treeEl.previousElementSibling;
		YDom.addClass(label, "loading");

		CStudioAuthoring.Service.lookupSiteFolders(site, rootPath, 1, "default", {
			openToPath: pathToOpen,
			self: this.self,

			success: function(treeData) { 
			   var items = treeData.item.children;
			   items = new Array(treeData.item);
		
			   this.self._drawTree(items, tree, this.self.defaultPath, instance);
				YDom.removeClass(label, "loading");
			},
		
			failure: function() {
			   YDom.removeClass(label, "loading");
			}			
		}, false);
	},

	/**
	 * method fired when tree node is expanded for first time
	 */
	column.onLoadNodeDataOnClick = function(node, fnLoadComplete) {
		if (!node.treeNodeTO) { 
			fnLoadComplete();
			return;
		}

		var plainpath = node.treeNodeTO.path,
		path = encodeURI(plainpath),
		site = node.treeNodeTO.site,
		pathToOpenTo = node.openToPath;

		CStudioAuthoring.Service.lookupSiteFolders(site, path, 1, "default", {
			success: function(treeData, args) {
				if(treeData.item.children.length == 0) {
					node.isLeaf = true;
				}

				args.instance._drawSubtree(treeData.item.children, args.node, args.pathToOpenTo, args.instance);
				args.fnLoadComplete();
			},

			failure: function(err, args) {
				args.fnLoadComplete();
			},

			argument: {
				"self": this,
				"node": node,
				"instance": node.instance,
				"fnLoadComplete": fnLoadComplete,
				pathToOpenTo: pathToOpenTo
			}
		}, false);
    },

   /**
    * create a transfer object for a node
    */
   column._createTreeNodeTransferObject = function(treeItem) {

       var retTransferObj = new Object();
       retTransferObj.site = CStudioAuthoringContext.site;
       retTransferObj.internalName = treeItem.internalName;
       retTransferObj.path = treeItem.path;
       retTransferObj.uri = treeItem.uri;
       retTransferObj.browserUri = treeItem.browserUri;
       retTransferObj.isContainer = treeItem.container;

       retTransferObj.status = CStudioAuthoring.Utils.getContentItemStatus(treeItem);
       retTransferObj.style = CStudioAuthoring.Utils.getIconFWClasses(treeItem); //, treeItem.container

       if (retTransferObj.internalName == "") {
           retTransferObj.internalName = treeItem.name;
       }

       if (treeItem.newFile) {
           retTransferObj.label = retTransferObj.internalName + "*";
       } 
       else {
           retTransferObj.label = retTransferObj.internalName;
       }

       return retTransferObj;
   },
            
	/**
	 * draw the tree
	 */
    column._drawTree = function(treeItems, tree, pathToOpenTo, instance) {
		var treeNodes = [];
		var treeNodesLabels = new Array();
		var currentLevelPath = null;
		var remainingPath = null;
		var nodeToOpen = null;
		
		if (pathToOpenTo != null && pathToOpenTo != undefined) {
		    var pathParts = pathToOpenTo.split("/");
		
		    if (pathParts.length >= 2) {
		        currentLevelPath = "/" + pathParts[1];
		        remainingPath = pathToOpenTo.substring(currentLevelPath.length + 1);
		    }
		}

		for(var i = 0; i < treeItems.length; i++) {
			var treeNodeTO = this._createTreeNodeTransferObject(treeItems[i]);
			
			if(treeNodeTO.isContainer) {	
				var treeNode = this._drawTreeItem(treeNodeTO, tree.getRoot());
				treeNode.instance = instance;
			
				treeNodes.push(treeNode);
		
				if (treeNode.labelElId) {
					treeNodesLabels.push(treeNode.labelElId);
				} 
				else {
					treeNodesLabels.push(tree.root.children[i].labelElId);
				}
			}
		}

			
		tree.subscribe('clickEvent', function(args) {
		    args.node.instance.inputEl.value = args.node.data.path;
			CStudioSearch.searchContext.path = args.node.data.path;
			CStudioSearch.executeSearch();
			return false;
		});


		tree.subscribe("expand", function(node) {
		    var id = node.labelElId;
		    var nodeId = YDom.get(id);
		
		    if (nodeId != null) {
		        var expandedNodeStyle = nodeId.className;
		        expandedNodeStyle = expandedNodeStyle.replace(" acn-collapsed-tree-node-label", "");
		        nodeId.className = expandedNodeStyle + " acn-expanded-tree-node-label";
		    }
		
		    return true;
		});
		
		tree.subscribe("collapse", function(node) {
		    var id = node.labelElId;
		    var nodeId = YDom.get(id);
		    var collapsedNodeStyle = nodeId.className;
		    collapsedNodeStyle = collapsedNodeStyle.replace(" acn-expanded-tree-node-label", "");
		    nodeId.className = collapsedNodeStyle + " acn-collapsed-tree-node-label";
		    return true;
		});

		instance.contextMenuId = ("ContextmenuWrapper" + (this.counter++))

		var oContextMenu = new YAHOO.widget.ContextMenu(
			instance.contextMenuId, {
				container: ".cstudio-context-menuWrapper",
				trigger: instance.dropdownEl,
				shadow: true,
				lazyload: true,
				hidedelay: 700,
				showdelay: 0,
				classname: "cstudio-context-menu"
		});
		
		oContextMenu.contextOwner = this;
		oContextMenu.subscribe('beforeShow', function() {
			this.contextOwner.onTriggerContextMenu(tree, this);
		}, tree, false);

//try{
		Lang = YAHOO.lang.escapeHTML = function(x) { return x; };
		tree.draw();
//}catch(err) {
//	alert(err);
//}	
		if (nodeToOpen != null) {
		    // opening to a specific path
		    nodeToOpen.expand();
		    nodeToOpen.openToPath = remainingPath;
		}
		
		instance.firstDraw = true;
		this.self.myTree = tree;
    };

    /**
     * render a tree item
     */
    column._drawTreeItem = function(treeNodeTO, root) {
        if (treeNodeTO.container == true || treeNodeTO.name != 'index.xml') {

            var treeNode = new YAHOO.widget.TextNode(treeNodeTO, root, false);

            treeNode.labelStyle = treeNodeTO.style + " yui-resize-label";
            treeNode.treeNodeTO = treeNodeTO;
            treeNode.renderHidden = true;
            treeNode.nowrap = true;

            if (!treeNodeTO.isContainer) {
                treeNode.isLeaf = true;
            }
        }

        return treeNode;
    };

    /**
     * render method called on sub root level elements
     */
    column._drawSubtree = function(treeItems, root, pathToOpenTo, instance) {

	     var treeNodes = new Array();
	     var treeNodesLabels = new Array();
	     var nodeToOpen = null;
	     var currentLevelPath = null;
	     var remainingPath = null;
	     var replaceAllChildFolders = false;
	
	     if (pathToOpenTo) {
	         var pathParts = pathToOpenTo.split("/");
	
	         if (pathParts.length >= 2) {
	             currentLevelPath = "/" + pathParts[0];
	             remainingPath = pathToOpenTo.substring(currentLevelPath.length);
	         }
	     }

	     for(var i = 0, l = treeItems.length, treeNodeTO; i < l; i++) {
	
	         treeNodeTO = this._createTreeNodeTransferObject(treeItems[i]);
	
	         if(treeNodeTO.isContainer) {
	            var treeNode = this._drawTreeItem(treeNodeTO, root);
	            if (treeItems[i].numOfChildren == 0) {
	                treeNode.isLeaf = true;
	            }
	            treeNode.instance = instance;
	
	            if (pathToOpenTo != null && treeNode != null) {
	                if (CStudioAuthoring.Utils.endsWith(treeNodeTO.path, currentLevelPath)) {
	                    nodeToOpen = treeNode;
	                }
	            }
	
	            treeNodes.push(treeNode);
	
	            if (root.children[i]) {
	                treeNodesLabels.push(root.children[i].labelElId);
	            } else {
	                treeNodesLabels.push(treeNode.labelElId);
	            }
	         }
	     }
	
	     if (nodeToOpen) {
	         nodeToOpen.expand();
	         nodeToOpen.openToPath = remainingPath;
	     }
    };

	/**
	 * open the context menu
	 */	
	column.onTriggerContextMenu = function(tree, contexMenu) {
		var target = contexMenu.contextEventTarget;
		var Self = this;
		
		oCurrentTextNode = tree.getNodeByElement(target);
		
		var menuItems = {
			write:[
				{ text: "Upload", onclick: { fn: Self.uploadContent, obj: tree } }
			]
		};
		
		var permsCallback = {
			success: function(response) {
				var isWrite = CStudioAuthoring.Service.isWrite(response.permissions);
		
				if(isWrite != false) {
					this._self._renderContextMenu(
						target,
						contexMenu, 
						this.component, 
						menuItems.write, 
						oCurrentTextNode, 
						true);
					
					contexMenu.show();
				}
				else {
					contexMenu.clearContent();
					contexMenu.hide();
					contexMenu.cancel();
				}
			},
		
			failure: function() {
			},
		
			_self: this,
			component: Self    
		};
		
		if(oCurrentTextNode != null) {
		
			CStudioAuthoring.Service.getUserPermissions(
				CStudioAuthoringContext.site, 
				oCurrentTextNode.data.uri, 
				permsCallback);
		
		}
		else {
			contexMenu.clearContent();
			contexMenu.cancel();
		}	
	};

	/** 
	 * render the context menu
	 */
	column._renderContextMenu = function(target, contextMenu, component, menuItems, oCurrentTextNode, isWrite) { 
		var menuWidth = "auto";

		component.lastSelectedTextNode = target.parentNode.parentNode.parentNode.parentNode;
        var menuEl = component.dropdownEl;
        contextMenu.clearContent();

		if(isWrite) {
			var d = document.createElement("div");
			d.className = "cstudio-context-menu context-menu-load-msg";
			d.innerHTML = 'Loading&hellip;';
			menuEl.appendChild(d);
						
			contextMenu.addItems(menuItems);			
	        contextMenu.render(menuEl);
			menuEl.removeChild(d);
		}		
	};

	/**
	 * upload content
	 */
	column.uploadContent = function(evtName, evtParams, data) {
		var uploadCb = {
			success: function() {
			    this.tree.instance.inputEl.value = oCurrentTextNode.data.path;
				CStudioSearch.searchContext.path = oCurrentTextNode.data.path;
				CStudioSearch.executeSearch();
				return false;
			},
			
			failure: function() {
			},
			
			callingWindow: window,
			tree: oCurrentTextNode
		};
		
		CStudioAuthoring.Operations.uploadAsset(
					CStudioAuthoringContext.site,
					oCurrentTextNode.data.uri,
					"upload",
					uploadCb);
	};

	return column;
}