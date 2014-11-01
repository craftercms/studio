var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;


/**
 * WcmTaxonomiesFolder
 * A root level folder is a configurable folder element that can be based at any
 * point along a wcm path. 
 */
CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder || {
	
	ROOT_OPEN: "open",
	ROOT_CLOSED: "closed",
	ROOT_TOGGLE: "toggle",
	
	currentNode: null,
	
	/**
	 * initialize module
	 */
	initialize: function(config) {

		if(config.name == "wcm-taxonomy-folder") {
			var instance = new CStudioAuthoring.ContextualNav.WcmTaxonomyFolderInstance(config);
			var dropdownInnerEl = YDom.get("acn-dropdown-menu-inner");
			this.addContentTreeRootFolder(instance);
		}
	},

	/**
	 * add a root level folder to the content drop down
	 */
	addContentTreeRootFolder: function(instance) {
		var folderListEl = instance.config.containerEl;
		
		var parentFolderEl = document.createElement("div");

		var parentFolderLinkEl = document.createElement("a");
		parentFolderLinkEl.id = instance.label.toLowerCase() + "-tree"; // should be part of class no?
		parentFolderLinkEl.innerHTML = instance.label;
		parentFolderLinkEl.onclick = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.onRootFolderClick;
		parentFolderLinkEl.componentInstance = instance;

		var treeEl =  document.createElement("div");
        
		folderListEl.appendChild(parentFolderEl);
		parentFolderEl.appendChild(parentFolderLinkEl);
		parentFolderEl.appendChild(treeEl);
		
		YDom.addClass(parentFolderLinkEl, "acn-parent-folder");
		YDom.addClass(parentFolderEl, "acn-parent " + instance.label.toLowerCase() + "-tree");			
        
		parentFolderLinkEl.rootFolderEl = treeEl;
		parentFolderLinkEl.parentControl = this;
		treeEl.rootFolderSite = CStudioAuthoringContext.site;
		treeEl.rootFolderPath = instance.path;
		
		instance.rootFolderEl = treeEl;
	},
	
	/**
	 * initialize the content tree for the dropdown.  
	 * There are many methods involved, but it all starts here.
	 */
	initializeContentTree: function(treeEl, path, instance) {
		var site = treeEl.rootFolderSite;
		var rootPath = treeEl.rootFolderPath;
		var pathToOpen = (path != undefined) ? path : null;
		
 		var tree = new YAHOO.widget.TreeView(treeEl);
        // need to move this to instance
		CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.tree = tree;
        
        tree.setDynamicLoad(this.onLoadNodeDataOnClick);
	   	tree.FOCUS_CLASS_NAME = null;

        CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.drawTaxonomyRoot(tree, instance);				
	},
	
	/**
	 * render function called on root level elements
	 */
	drawTaxonomyRoot: function(tree, instance) {

	   	var treeNodes = new Array();
	   	var treeNodesLabels = new Array();
        var treeItems = instance.roots;

		for(var i=0; i<treeItems.length; i++) {
			var treeNodeTO = this.createTreeNodeTransferObject(treeItems[i]);
			var treeNode = this.drawTopLevelTreeItem(treeNodeTO, tree.getRoot());
			treeNode.instance = instance;
		}
		
		tree.subscribe('clickEvent', function(args) { 
			CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.onTreeNodeClick(args.node);
		});  
		
		tree.subscribe("clickEvent", function(node) { 
			return false; 
		});
		
		tree.subscribe("dblClickEvent", function(node) { 
			return false; 
		}); 
		
		tree.subscribe("expand", function(node) {
			var id = node.labelElId;
			var nodeId = YDom.get(id);
			
			if(nodeId != null) {
				var expandedNodeStyle = nodeId.className;
				expandedNodeStyle = expandedNodeStyle.replace(" acn-collapsed-tree-node-label","");
				nodeId.className = expandedNodeStyle + " acn-expanded-tree-node-label";
			}
			
			return true;
		});
		
		tree.subscribe("collapse", function(node) {
			var id = node.labelElId;
			var nodeId = YDom.get(id);  
			var collapsedNodeStyle = nodeId.className;
			collapsedNodeStyle = collapsedNodeStyle.replace(" acn-expanded-tree-node-label","");
			nodeId.className = collapsedNodeStyle + " acn-collapsed-tree-node-label";
			return true;			
		}); 

		var contextMenu = new YAHOO.widget.ContextMenu(
		    "ContextmenuWrapper",
		    {
		    	container: "acn-context-menu",
		        trigger: "acn-dropdown-menu-wrapper",
		        shadow: false,
		        lazyload: true
		    }
		);
			
		contextMenu.subscribe('beforeShow', function() { 
			CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.onTriggerContextMenu(tree, this);
		}, tree, false); 
		
		
        tree.draw();
 	},

	/**
	 * render a tree item
	 */
	drawTopLevelTreeItem: function(treeNodeTO, root) {

		treeNodeTO.level = 1;
		var treeNode = new YAHOO.widget.TextNode(treeNodeTO, root, false);
		
		treeNode.labelStyle = "acn-taxonomy yui-resize-label"; 
		treeNode.treeNodeTO = treeNodeTO;
		treeNode.renderHidden = true;
		treeNode.nowrap = true;

		return treeNode;
	},


	/**
	 * render a tree item
	 */
	drawCategoryTreeItem: function(treeNodeTO, root, instance) {

		var treeNode = new YAHOO.widget.TextNode(treeNodeTO, root, false);
		
		treeNode.labelStyle = "acn-taxonomy-category yui-resize-label"; 
		treeNode.treeNodeTO = treeNodeTO;
		treeNode.renderHidden = true;
		treeNode.nowrap = true;
		treeNode.treeNodeTO.level = root.treeNodeTO.level + 1;
		
		if(!treeNodeTO.isContainer) {
			treeNode.isLeaf = true;
		}; 

		return treeNode;
	},
		
	/**
	 * method fired when user clicks on the root level folder
	 */
	onRootFolderClick: function() {
		var WcmTaxonomiesFolder = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder;
		
		WcmTaxonomiesFolder.toggleFolderState(this.componentInstance, WcmTaxonomiesFolder.ROOT_TOGGLE);
	},

	/**
	 * method fired when tree node is expanded for first time
	 */
	onLoadNodeDataOnClick: function(node, fnLoadComplete)  {
		var site = node.treeNodeTO.site;
        var taxonomyName = node.treeNodeTO.taxonomyName;

		var dataCb = {
	    	success: function(treeData, args) {
                treeData = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.sort(treeData);

				var WcmTaxonomiesFolder = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder;
				var treeItems = treeData.modelData;
				var tree = this.argument.node.tree;
				var instance = this.argument.instance;
				
                if(treeItems) {
                    for(var i=0; i<treeItems.length; i++) {
                        var treeNodeTO = WcmTaxonomiesFolder.createTreeNodeTransferObjectFromCategory(treeItems[i], taxonomyName);
                        var treeNode = WcmTaxonomiesFolder.drawCategoryTreeItem(treeNodeTO, this.argument.node);
                        treeNode.instance = instance;
                    }
                }
                
	    		this.argument.fnLoadComplete();

				CStudioAuthoring.ContextualNav.WcmRootFolder.nodeHoverEffects(this);
	    	},
	    	
	    	failure: function(err, args) {
	    		this.argument.fnLoadComplete();
	    	},

	    	argument: {
            	"node": node,
            	"instance": node.instance,
                "fnLoadComplete": fnLoadComplete
            }
	    };

	    CStudioAuthoring.Service.getTaxonomy(site, node.treeNodeTO.taxonomyName, node.treeNodeTO.level, false, "item", dataCb);	
	},
		
	/**
	 * method fired when tree item is clicked
	 */
	onTreeNodeClick: function(node)	{

		return false;
	},	

	/**
	 * toggle folder state
	 */
	toggleFolderState: function(instance, forceState, path) {
		var WcmTaxonomiesFolder = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder;
		
		if(forceState != null && forceState != WcmTaxonomiesFolder.ROOT_TOGGLE) {
			// force
			if(forceState == WcmTaxonomiesFolder.ROOT_OPEN) {
				instance.rootFolderEl.style.display = 'block';
				instance.state = WcmTaxonomiesFolder.ROOT_OPEN;
				this.initializeContentTree(instance.rootFolderEl, path, instance);
			}
			else {
				instance.rootFolderEl.style.display = 'none';
				instance.state = WcmTaxonomiesFolder.ROOT_CLOSED;
			}
		}
		else {
			
			// toggle
			if(instance.state == WcmTaxonomiesFolder.ROOT_OPEN) {
				this.toggleFolderState(instance,  WcmTaxonomiesFolder.ROOT_CLOSED, path);
			}
			else {
				this.toggleFolderState(instance,  WcmTaxonomiesFolder.ROOT_OPEN, path);
			}
		}
	},
		
	/**
	 * create a transfer object for a node
	 */
	createTreeNodeTransferObject: function(treeItem) {

		var retTransferObj = new Object();
		
		retTransferObj.site = CStudioAuthoringContext.site;
		retTransferObj.label = treeItem.label;
		retTransferObj.name =  treeItem.type;
		retTransferObj.type =  treeItem.type;
        retTransferObj.taxonomyName = treeItem.type;

		return retTransferObj;
	},

	/**
	 * create a transfer object for a node
	 */
	createTreeNodeTransferObjectFromCategory: function(categoryItem, taxonomyName, level) {

		var retTransferObj = new Object();
		
		retTransferObj.site = CStudioAuthoringContext.site;
		retTransferObj.label = categoryItem.label;
		retTransferObj.type = categoryItem.type;
		retTransferObj.name = categoryItem.value;
		retTransferObj.isContainer = (categoryItem.children.length > 0);
        retTransferObj.taxonomyName = taxonomyName;

		return retTransferObj;
	},
	
	/**
	 * called on right click
	 */
	onTriggerContextMenu: function(tree, menu)	{
	   	var WcmTaxonomiesFolder = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder;
	    var target = menu.contextEventTarget;
	    var rightClickMenuEl = YDom.get("ContextmenuWrapper");	    
		
	    var menuOptions = {
			"default" : [ 
		    	{ text: "Order Children", onclick: { fn: CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.orderTaxonomy } },
				{ text: "New", onclick: { fn: CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.newTaxonomy } },		    	
		    	{ text: "Edit", onclick: { fn: CStudioAuthoring.ContextualNav.WcmRootFolder.editContent } },
		    	{ text: "Delete", onclick: { fn: CStudioAuthoring.ContextualNav.WcmRootFolder.deleteContent, obj:tree } }
			]};

		var curMenuOptions = menuOptions["default"];
	    WcmTaxonomiesFolder.currentNode = tree.getNodeByElement(target);

	    if(WcmTaxonomiesFolder.currentNode != null) {
	    	menu.clearContent();
			menu.addItems(curMenuOptions);
		 	
		 	rightClickMenuEl.style.display = "block";
		 	menu.render();
	    }
	    else {
			menu.cancel();
	        rightClickMenuEl.style.display = "none";
	    }
	},
    
    orderTaxonomy: function() {
        var curNode = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.currentNode.treeNodeTO;
    
        var orderedCb = {
            success: function() {
                CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.tree.removeChildren(this.currentNode);

                this.currentNode.renderChildren();
                this.currentNode.refresh();
            },
            
            failure: function() {
            },
            
            currentNode: CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.currentNode
        };
        
        CStudioAuthoring.Operations.orderTaxonomy(CStudioAuthoringContext.site, curNode.taxonomyName, curNode.level, orderedCb);
    },

    newTaxonomy: function() {
        var curNode = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.currentNode.treeNodeTO;    	 
        var newTaxonomyCb = {
            success: function() {
                
            },
            
            failure: function() {
            }
        };
        
        CStudioAuthoring.Operations.createNewTaxonomy(curNode.value,newTaxonomyCb);
    },

    /**
     * this function should go away -- sort in the service 
     */
    sort: function(treeData) {
        var modelData = treeData.modelData;
        
        for(var i=0; i<modelData.length; i++) {
            for(var j=0; j<modelData.length; j++) {
                if(modelData[i].order < modelData[j].order) {
                    var hold = modelData[i];
                    modelData[i] = modelData[j];
                    modelData[j] = hold;
                }
            }            
        }
        
        treeData.modelData = modelData;
        
        return treeData;
    }
}

/**
 * instance object
 * CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder is static
 */
CStudioAuthoring.ContextualNav.WcmTaxonomyFolderInstance = function(config) {

	this._self = this;
	this._toggleState = CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder.ROOT_CLOSED;
	this.rootFolderEl = null;

	this.type = config.name;	
	this.label = config.params["label"];
	this.path = config.params["path"];
    this.roots = config.params["roots"];
	this.config = config;
		
};


CStudioAuthoring.Module.moduleLoaded("wcm-taxonomy-folder", CStudioAuthoring.ContextualNav.WcmTaxonomiesFolder);
