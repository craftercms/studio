(function() {
    var YDom = YAHOO.util.Dom,
    	YEvent = YAHOO.util.Event,
    	YConnect = YAHOO.util.Connect;
		sutils = CStudioAuthoring.StringUtils,
		storage = CStudioAuthoring.Storage,		
		counter = 0, // Used to identify the contextmenu for each instances. May be used for any other purpose while numberic chronological order is maintained
		Self = null; // Local reference to CStudioAuthoring.ContextualNav.WcmRootFolder initialized by CStudioAuthoring.register call
        treeFlag = false;

		if(YAHOO.lang && !YAHOO.lang.escapeHTML) {
			// YUI version conflicts
			YAHOO.lang.escapeHTML = function(val) { return val; }
		}
		
    /**
     * WcmRootFolder
     * A root level folder is a configurable folder element that can be based at any
     * point along a wcm path.
     */
    Self = CStudioAuthoring.register({
        "ContextualNav.WcmRootFolder": {
			ROOT_OPENED: "root-folder",
            ROOT_OPEN: "open",
            ROOT_CLOSED: "closed",
            ROOT_TOGGLE: "toggle",
            CUT_STYLE_RGB: "rgb(159, 182, 205)",
            CUT_STYLE: "#9FB6CD",
			searchesToWire: [],
            myTree: null,
            myTreePages: null,
            myTreeComp: null,
            copiedItem: null,
            cutItem: null,
            instanceCount: 0,
            lastSelectedTextNode: null, //used for holding last selected node; to use it to hold hover effect on a text node when cotex menu is open.
            treePathOpenedEvt: new YAHOO.util.CustomEvent("wcmRootFolderTreePathLoaded", Self),
            /**
             * initialize module
             */
            initialize: function(config) {
                if (config.name == "wcm-root-folder") {
                    var instance = new CStudioAuthoring.ContextualNav.WcmRootFolderInstance(config);
                    instance.cannedSearchCache = [];
                    // cache the searches by name so they can be checked quickly when building the nav
                    if (config.params.cannedSearches) {
                    	// not an array
                    	if ( (typeof(config.params.cannedSearches) == "object") && (config.params.cannedSearches.cannedSearch.length == undefined)) {
                    		if (config.params.cannedSearches.cannedSearch != undefined) {
                    			var searchPath = config.params.cannedSearches.cannedSearch.path;
                    			if (!instance.cannedSearchCache[searchPath]) {
                    				instance.cannedSearchCache[searchPath] = [];
                    			}
                    			instance.cannedSearchCache[searchPath].push(config.params.cannedSearches.cannedSearch);
                    		}
                    	} else { 
                    		for (var i = 0; i < config.params.cannedSearches.cannedSearch.length; i++) {
                    			var searchPath = config.params.cannedSearches.cannedSearch[i].path;
                    			if (!instance.cannedSearchCache[searchPath]) {
                    				instance.cannedSearchCache[searchPath] = [];
                    			}
                    			instance.cannedSearchCache[searchPath].push(config.params.cannedSearches.cannedSearch[i]);
                    		}
                    	}
                    }
                    this.addContentTreeRootFolder(instance);
                    /**
                     * EMO-8478
                     */
                    var thisComponent = this;                    
                    if(YAHOO.util.Dom.getStyle("acn-dropdown-menu-wrapper", "display") != "none") {
                        window.firstClick = true;
                        thisComponent.openLatest(instance);
                    }
                    YEvent.on('acn-dropdown-toggler', 'click', function() {       
                    	if(!window.firstClick && YAHOO.util.Dom.getStyle("acn-dropdown-menu-wrapper", "display") != "none") {
                    		window.firstClick = true;
                    		thisComponent.openLatest(instance);
                    	}                    	 
                    });					
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
                parentFolderLinkEl.innerHTML = CMgs.format(siteDropdownLangBundle, instance.label);
                parentFolderLinkEl.onclick = Self.onRootFolderClick;
                parentFolderLinkEl.componentInstance = instance;

                var treeEl = document.createElement("div");
                folderListEl.appendChild(parentFolderEl);
                parentFolderEl.appendChild(parentFolderLinkEl);
                parentFolderEl.appendChild(treeEl);

                YDom.addClass(parentFolderLinkEl, "acn-parent-folder");
                parentFolderLinkEl.style.cursor = "pointer";
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

                var tree = instance.tree = new YAHOO.widget.TreeView(treeEl);
                tree.setDynamicLoad(this.onLoadNodeDataOnClick);
                tree.FOCUS_CLASS_NAME = null;

				var label = treeEl.previousElementSibling;
				YDom.addClass(label, "loading");

				// reduce call if not necessary
				if (this.pathOnlyHasCannedSearch(rootPath, instance)) {
					var dummy = new Object();
					dummy.path = rootPath;
					var items = new Array();
					items.push(dummy);
					Self.drawTree(items, tree, path, instance);
					YDom.removeClass(label, "loading");
					//add hover effect to nodes
					Self.nodeHoverEffects(this);
				} else {
	                CStudioAuthoring.Service.lookupSiteContent(site, rootPath, 1, "default", {
	                    openToPath: pathToOpen,
	                    success: function(treeData) { 
	                	
	                		if(rootPath == "/site/website")
	                			window.treeData = treeData;
	                	
	                        var items = treeData.item.children;
	                        if (instance.showRootItem) {
	                            items = new Array(treeData.item);
	                        }
	                        Self.drawTree(items, tree, path, instance);
							YDom.removeClass(label, "loading");
	                        //add hover effect to nodes
	                        Self.nodeHoverEffects(this);
	                    },
	
	                    failure: function() {
	                        YDom.removeClass(label, "loading");
	                    }
	                });
				}
            },
            /**
             * to check, if extra ajax call can be reduced
             */
			pathOnlyHasCannedSearch: function(path, instance) {
				if (instance.showRootItem == "false" && instance.cannedSearchCache[path]) 
					return true;
				return false;
			},
            /**
             * render function called on root level elements
             */
            drawTree: function(treeItems, tree, pathToOpenTo, instance) {

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

                for (var i = 0; i < treeItems.length; i++) {
                    var cannedSearches = instance.cannedSearchCache[treeItems[i].path];
                    var isSearch = false;

                    if (cannedSearches) {
                        for (var l = 0; l < cannedSearches.length; l++) {
                            if (cannedSearches[l].insertAs == "replace") {
                                this.drawCannedSearch(cannedSearches[l], tree.getRoot());
                                isSearch = true;
                            } else if (cannedSearches[l].insertAs == "append") {
                                this.drawCannedSearch(cannedSearches[l], tree.getRoot());
                                isSearch = false;
							}
                        }
                    }

                    if (!isSearch) {
                        var treeNodeTO = this.createTreeNodeTransferObject(treeItems[i]);

                        var treeNode = this.drawTreeItem(treeNodeTO, tree.getRoot());
                        treeNode.instance = instance;

                        if (pathToOpenTo != null && treeNode != null) {
                            if (treeNodeTO.pathSegment == "index.xml") {
                                if (CStudioAuthoring.Utils.endsWith(treeNodeTO.path, currentLevelPath)) {
                                    nodeToOpen = treeNode;
                                }
                            }
                        }

                        treeNodes.push(treeNode);
                        if (treeNode.labelElId) {
                            treeNodesLabels.push(treeNode.labelElId);
                        } else {
                            treeNodesLabels.push(tree.root.children[i].labelElId);
                        }

                    }
                }

                new YAHOO.widget.Tooltip("acn-context-tooltipWrapper", {
                    context: treeNodesLabels,
                    hidedelay:0,
                    showdelay:1000,
                    container: "acn-context-tooltip"
                });

                tree.subscribe('clickEvent', function(args) {
                    Self.onTreeNodeClick(args.node);

                    // Prevent the default behavior (i.e. expand/collapse) of links that should take the user to preview
                    if (args.node.data.linkToPreview) {
                        return false;
                    }
                });

                tree.subscribe("dblClickEvent", function(node) {
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

				instance.contextMenuId = ("ContextmenuWrapper" + (counter++))
                var oContextMenu = new YAHOO.widget.ContextMenu(instance.contextMenuId, {
                    container: "acn-context-menu",
                    trigger: YDom.get(instance.label.toLowerCase() + "-tree").parentNode,
                    shadow: true,
                    lazyload: true,
                    hidedelay: 700,
					showdelay: 0,
					classname: "wcm-root-folder-context-menu",
					zIndex: 100
                });

                oContextMenu.subscribe('beforeShow', function() {
                		Self.onTriggerContextMenu(tree, this);
                }, tree, false);

                tree.draw();
				Self.wireUpCannedSearches();

                if (nodeToOpen != null) {
                    // opening to a specific path
                    nodeToOpen.expand();
                    nodeToOpen.openToPath = remainingPath;
                }

				instance.firstDraw = true;

                if(treeFlag == false){
                    Self.myTreePages = tree;
                    instance.type = "Pages";
                    treeFlag = true;
                }else{
                    Self.myTreeComp = tree;
                    instance.type = "Components";
                }

            },
            /**
             * render method called on sub root level elements
             */
            drawSubtree: function(treeItems, root, pathToOpenTo, instance) {

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

                var parentCannedSearch = instance.cannedSearchCache[root.treeNodeTO.path];
                var replaceChildren = new Array();

                if (parentCannedSearch) {
                    for (var j = 0; j < parentCannedSearch.length; j++) {
                        if (parentCannedSearch[j].insertAs == "replaceAllChildFolders") {
                            replaceAllChildFolders = true;
                            break;
                        }
                    }
                }

                for (var i = 0, l = treeItems.length, treeNodeTO, renderChild; i < l; i++) {

                    treeNodeTO = this.createTreeNodeTransferObject(treeItems[i]);
                    if (treeNodeTO.isLevelDescriptor || treeNodeTO.isComponent ||
                        treeNodeTO.container == false || treeNodeTO.name == 'index.xml' ||
                        (treeNodeTO.isContainer == true && treeNodeTO.pathSegment != 'index.xml') ||
                        treeNodeTO.previewable == false) {
                        treeNodeTO.style += " no-preview";
                    }else{
                        treeNodeTO.style += " preview";
                    }
                    
                    renderChild = true;

                    if (replaceAllChildFolders && treeNodeTO.isContainer) {
                        renderChild = false;
                    }

                    if (renderChild) {
                        var itemCannedSearch = instance.cannedSearchCache[treeNodeTO.path];

                        if (itemCannedSearch && itemCannedSearch.length != 0 && itemCannedSearch[0].insertAs != "append") {
                            replaceChildren.push(treeNodeTO.path);
                        } else {
                            var treeNode = this.drawTreeItem(treeNodeTO, root);
                            //nodes will not have collapse/expand icon if they do not have any children
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
                }

                // done here so searches show up at the bottom
                if (replaceAllChildFolders) {
                    for (var k = 0; k < parentCannedSearch.length; k++) {
                        this.drawCannedSearch(parentCannedSearch[k], root);
                    }
                } else if (replaceChildren.length > 0) {
                    for (var i = 0; i < replaceChildren.length; i++) {
                        var itemCannedSearch = instance.cannedSearchCache[replaceChildren[i]];

                        for (var k = 0; k < itemCannedSearch.length; k++) {
                            if (itemCannedSearch[k].insertAs == "replace") {
                                this.drawCannedSearch(itemCannedSearch[k], root);
                            }
                        }
                    }
                }

                new YAHOO.widget.Tooltip("acn-context-tooltipWrapper", {
                    context: treeNodesLabels,
                    hidedelay:0,
                    showdelay:1000,
                    container: "acn-context-tooltip"
                });

                if (nodeToOpen) {
                    nodeToOpen.expand();
                    //nodeToOpen.openToPath = remainingPath;
                }
            },

            /**
             * draw a canned search element
             */
            drawCannedSearch: function(searchConfig, root) {

                var treeNode = null;
                var searchId = CStudioAuthoring.Utils.generateUUID();
                 var newId = CStudioAuthoring.Utils.generateUUID();
                 if (searchConfig.newPath && !window.pasteFlag) {
                    	var label = "<a style='display: inline; padding-right:5px;' id='ID' href='#' class='canned-search-el'>LABEL</a><a style='display: inline; border-left: 1px solid grey; padding-left: 5px;' id='NEWID' href='#'>+add</a>";
                            
                        label = label.replace("ID", searchId); 
                        label = label.replace("LABEL",searchConfig.label);
                        label = label.replace("NEWID", newId); 
                        
                        searchConfig.label = label;

                    treeNode = new YAHOO.widget.TextNode(searchConfig, root, false);
                    Self.searchesToWire.push(treeNode);
                } else {
                    searchConfig.label = "<a style='display: inline;' id='" + searchId + "' href='#'>" +
                            searchConfig.label + "</a>";

                    treeNode = new YAHOO.widget.TextNode(searchConfig, root, false);
                    
                    Self.searchesToWire.push(treeNode);
                }


                treeNode.nodeType = "SEARCH";
                treeNode.searchTO = searchConfig;
                treeNode.searchTO.newId = newId;
                treeNode.searchTO.searchId = searchId;
                treeNode.renderHidden = true;
                treeNode.nowrap = true;
                treeNode.isLeaf = true;
                treeNode.labelStyle = "acn-canned-search yui-resize-label";
				treeNode._yuiGetHtml = treeNode.getHtml();
				
treeNode.getHtml = function() { 
	var markup = treeNode._yuiGetHtml;   
	markup = markup.replace(/\&gt;/g, ">");
	markup = markup.replace(/\&lt;/g, "<");
	markup = markup.replace(/\&amp;/g, "&");
	markup = markup.replace(/\&#x27;/g, "'");
	markup = markup.replace(/\&#x2F;/g, "/");	

	return markup;
};
                return treeNode;
            },
            /**
             * render a tree item
             */
            drawTreeItem: function(treeNodeTO, root) {
                if (treeNodeTO.container == true || treeNodeTO.name != 'index.xml') {

                    if (!treeNodeTO.style.match(/\bfolder\b/)) {
                        treeNodeTO.linkToPreview = true;
                    }
                    
                    var treeNode = new YAHOO.widget.TextNode(treeNodeTO, root, false);

                    treeNode.labelStyle = treeNodeTO.style + " yui-resize-label treenode-label";
                    treeNode.nodeType = "CONTENT";
                    treeNode.treeNodeTO = treeNodeTO;
                    treeNode.renderHidden = true;
                    treeNode.nowrap = true;

                    if (!treeNodeTO.isContainer) {
                        treeNode.isLeaf = true;
                    }
                }

                return treeNode;
            },
            /**
             * method fired when user clicks on the root level folder
             */
            onRootFolderClick: function() {				
                Self.toggleFolderState(this.componentInstance, Self.ROOT_TOGGLE);
            },
            /**
             * toggle folder state
             */
            toggleFolderState: function(instance, forceState, path) {
                var WcmRootFolder = Self;
                if (forceState != null && forceState != WcmRootFolder.ROOT_TOGGLE) {
                    // force
                    if (forceState == WcmRootFolder.ROOT_OPEN) {
                        instance.rootFolderEl.style.display = 'block';
                        instance.state = WcmRootFolder.ROOT_OPEN;
						if (!instance.firstDraw) {
							Self.initializeContentTree(instance.rootFolderEl, path, instance);
                            Self.save(instance, Self.ROOT_OPENED);
						}
                    } else {
                        instance.rootFolderEl.style.display = 'none';
                        instance.state = WcmRootFolder.ROOT_CLOSED;
                        storage.eliminate( Self.getStoredPathKey(instance) );
                    }
                } else {
					// toggle
                    if (instance.state == WcmRootFolder.ROOT_OPEN) {
                        this.toggleFolderState(instance, WcmRootFolder.ROOT_CLOSED, path);
                    } else {
                        this.toggleFolderState(instance, WcmRootFolder.ROOT_OPEN, path);
                    }
                }
            },
            
            getStoredPathKey: function(instance) {
				return (instance.config.params.path + '-latest-opened-path');
			},
			
			openLatest: function(instance){

                var latestStored;
                try {
                    // TODO: revise this way to get the current page in preview
                    // CStudioAuthoringContext properties isPreview and previewCurrentPath are set on overlayhook.get.html.ftl
                    if (instance.label == "Pages" && CStudioAuthoringContext.isPreview === true) {
                        latestStored = CStudioAuthoringContext.previewCurrentPath;
                    }
                } catch (ex) { }
                if (typeof latestStored === "undefined") {
                    latestStored = storage.read( Self.getStoredPathKey(instance) );
                }

				if(latestStored){
					var treeEl = instance.rootFolderEl,
						site = treeEl.rootFolderSite,
						rootPath = treeEl.rootFolderPath,
						tree = instance.tree = new YAHOO.widget.TreeView(treeEl),
						paths = null,
						counter = 0,
						recursiveCalls = 0,
						pathTrace = rootPath,
						updatePathTrace = function(){ return (pathTrace = (pathTrace + "/" + paths[counter++])); },
						$ = YAHOO.util.Selector.query;
					(function(){
						var tmp = latestStored.replace(rootPath, "");
						paths = tmp.length ? (tmp.charAt(0) == "/" ? tmp.substr(1) : tmp).split("/") : null;
						recursiveCalls = tmp.length ? paths.length : 0;
					})();
					var label = instance.rootFolderEl.previousElementSibling;
					YDom.addClass(label, "loading");
					var doCall = function(n){
						Self.onLoadNodeDataOnClick(n, function(){
							n.loadComplete();
							if (counter < recursiveCalls) {
								updatePathTrace();
								var node = tree.getNodeByProperty("path", pathTrace);
								if (node != null) {
									var loadEl = $(".ygtvtp", node.getEl(), true);
									loadEl == null && (loadEl = $(".ygtvlp", node.getEl(), true));
									YDom.addClass(loadEl, "ygtvloading");
									doCall(node);
								} else {
									YDom.removeClass(label, "loading");
									YDom.removeClass($(".ygtvloading", treeEl), "ygtvloading");
									// Add hover effect to nodes
									Self.nodeHoverEffects(this);
                                    Self.firePathLoaded(instance);
								}
							} else {
								YDom.removeClass(label, "loading");
								YDom.removeClass($(".ygtvloading", treeEl), "ygtvloading");
								// Add hover effect to nodes
								Self.nodeHoverEffects(this);
                                Self.firePathLoaded(instance);
							}
						});
					}
					tree.setDynamicLoad(this.onLoadNodeDataOnClick);
					//reduce extra call, if applicable
					if (this.pathOnlyHasCannedSearch(rootPath, instance)) {
						var dummy = new Object();
						dummy.path = rootPath;
						var items = new Array();
						items.push(dummy);
						Self.drawTree(items, tree, null, instance);
						YDom.removeClass(label, "loading");
						Self.firePathLoaded(instance);
					} else {
						CStudioAuthoring.Service.lookupSiteContent(site, rootPath, 1, "default", {
							success: function(treeData) {
							
								if(rootPath == "/site/website")
									window.treeData = treeData;
							
								var items = treeData.item.children;
								if (instance.showRootItem) {
									items = new Array(treeData.item);
								}
								instance.state = Self.ROOT_OPEN;
								Self.drawTree(items, tree, null, instance);
								if (latestStored != Self.ROOT_OPENED) {
									var node, loadEl;
									node = tree.getNodeByProperty("path", rootPath);
									if(node == null) {
										node = tree.getNodeByProperty("path", updatePathTrace());
										loadEl = YAHOO.util.Selector.query(".ygtvtp", node.getEl(), true);
									} else {
										loadEl = YAHOO.util.Selector.query(".ygtvlp", node.getEl(), true);
									}
									YDom.addClass(loadEl, "ygtvloading");
									doCall(node);
								} else {
									YDom.removeClass(label, "loading");
	                                Self.firePathLoaded(instance);
								}
							},
							failure: function() { }
						});
					}
				} else {
                    Self.firePathLoaded(instance);
                }
			},
            firePathLoaded: function(instance) {
                ++(Self.treePathOpenedEvt.fireCount);
                Self.treePathOpenedEvt.fire(Self.instanceCount, Self.treePathOpenedEvt.fireCount);
            },
            /**
			 *  wire up new to search items 
			 */
			wireUpCannedSearches: function() {
				var searchesToWire = Self.searchesToWire;

				for (var i = 0; i < searchesToWire.length; i++) {
					var newId = searchesToWire[i].searchTO.newId;
					var searchId = searchesToWire[i].searchTO.searchId;

					var newEl = document.getElementById(newId);
					var searchEl = document.getElementById(searchId);

					if (newEl) {
						newEl.searchTO = searchesToWire[i].searchTO;
					}

					searchEl.searchTO = searchesToWire[i].searchTO;

					var createCb = {
						success: function() {
							this.callingWindow.location.reload(true);
						},

						failure: function() {
						},

						callingWindow: window
					};

					if (newEl) {
						newEl.onclick = function() {
							CStudioAuthoring.Operations.createNewContent(
									CStudioAuthoringContext.site,
									this.searchTO.newPath,
									false,
									createCb);

							return false;
						};
					}

					searchEl.onclick = function() {

						var url = CStudioAuthoringContext.authoringAppBaseUri + "/search?site=" + CStudioAuthoringContext.site +"&s=";

						var queryParams = this.searchTO.queryParams.queryParam;

						for (var i = 0; i < queryParams.length; i++) {
							url += "&" + encodeURIComponent(queryParams[i].name) +
									"=" + encodeURIComponent(queryParams[i].value);
						}

						window.location = url;
					}
				}
				/* free up once current ones registered */
				Self.searchesToWire = new Array();
			},
            /**
             * method fired when tree node is expanded for first time
             */
			
            onLoadNodeDataOnClick: function(node, fnLoadComplete) {
				// applicable for items under detail folder
				if (!node.treeNodeTO) { 
					fnLoadComplete();
					return ;
				}
					
				var plainpath = node.treeNodeTO.path,
					path = encodeURI(plainpath),
                	site = node.treeNodeTO.site,
                	pathToOpenTo = node.openToPath;

				Self.save(node.instance, plainpath);
				
				
				var serviceCb = {
	                	
	                    success: function(treeData, args) {
	                		
		                	/**
							 * nodes will not have collapse/expand icon if they do not have any children
							 * after clicking them.
							 */
	                		if(treeData.item.children.length == 0) {
	                			node.isLeaf = true;
	                		}
	            		
	                		Self.drawSubtree(treeData.item.children, args.node, args.pathToOpenTo, args.instance);
	
	            			args.fnLoadComplete();
	
	            			/* wire up new to search items */
	                		
	    					Self.wireUpCannedSearches();
	        		
	    					//add hover effect to nodes
	    					Self.nodeHoverEffects(this);

	    					//add blur effect for cut items
	    					Self.setChildrenStyles(args.node);
	            	    },
	
	                    failure: function(err, args) {
	                        args.fnLoadComplete();
	                    },
	
	                    argument: {
	                        "node": node,
	                        "instance": node.instance,
	                        "fnLoadComplete": fnLoadComplete,
	                        pathToOpenTo: pathToOpenTo
	                    }
	           	} 
				
	            CStudioAuthoring.Service.lookupSiteContent(site, path, 1, "default", serviceCb);
    },
    
    save: function(instance, path) {
        storage.write(Self.getStoredPathKey(instance), path, 360);
    },
    
    /**
	* methos that fires when new items added to tree.
	*/
	refreshNodes: function(treeNode,status) {
		var tree = Self.myTree;
		var copiedItemNode = Self.copiedItem;
		var node = tree.getNodeByProperty("path", treeNode.treeNodeTO.path);
		if (copiedItemNode != null && treeNode.data.path == copiedItemNode.data.path) {
                node = tree.getNodeByProperty("path", treeNode.parent.data.path);
                Self.copiedItem = null;
        }

        if(node) {
    		if (node.isLeaf) node.isLeaf = false;
        }
        else {
           node = treeNode;
        }
        
	   	tree.removeChildren(node);
		var loadEl = $(".ygtvtp", node.getEl(), true);
		loadEl == null && (loadEl = $(".ygtvlp", node.getEl(), true));
		YDom.addClass(loadEl, "ygtvloading");
		node.renderChildren();
		node.refresh();
		
		var treeInner = YDom.get('acn-dropdown-menu-inner');
		var previousCutEl = YDom.getElementsByClassName("status-icon", null, treeInner);
		
		for(var i=0; i<previousCutEl.length; i++){
			
			if(previousCutEl[i].style.color == Self.CUT_STYLE_RGB || previousCutEl[i].style.color == Self.CUT_STYLE ){
				
				if(status){
					var tempSplit = previousCutEl[i].id.split("labelel");
					var parentNode = YDom.get(tempSplit[0]+tempSplit[1]);
					parentNode.style.display = 'none';
					if (Self.cutItem != null) {
						var parNode = tree.getNodeByProperty("path", Self.cutItem.parent.data.path);
						//if parent have single child and we did cut and paste the child,
						//we should refresh the parent node to remove expand collapse icon 
						if (parNode && parNode.children && parNode.children.length == 1) {
							tree.removeChildren(parNode);
							var parLoadEl = $(".ygtvtp", parNode.getEl(), true);
							parLoadEl == null && (parLoadEl = $(".ygtvlp", parNode.getEl(), true));
							YDom.addClass(parLoadEl, "ygtvloading");
							parNode.renderChildren();
							parNode.refresh();
						} else if (parNode) {
							//remove the only item from parent node.
							tree.removeNode(Self.cutItem);
						}
						Self.cutItem = null;
					}
				}else{
					previousCutEl[i].removeAttribute("style");
				}
			}						
		}
		
		
	},
	
	/**
	* method fired when tree item is clicked
	 */
	onTreeNodeClick: function(node) {
	
		// lets remove ths case logic here and just invoke a callback
		if (node.nodeType == "CONTENT") {
			if (node.data.previewable == true && node.instance.onClickAction == "preview") {
			
				if(node.data.isContainer == true && node.data.pathSegment != 'index.xml') {
					// this is a false state coming from the back-end
				} else /*if (node.data.isLevelDescriptor == false)*/ {
                    CStudioAuthoring.Operations.openPreview(node.data, "", false, false);
				}
			}
		}  else if (node.nodeType == "SEARCH") {
			// wired on render
		}

		return false;

	},
            /**
             * create a transfer object for a node
             */
            createTreeNodeTransferObject: function(treeItem) {

                var retTransferObj = new Object();
                retTransferObj.site = CStudioAuthoringContext.site;
                retTransferObj.internalName = treeItem.internalName;
                retTransferObj.link = "UNSET";
                retTransferObj.path = treeItem.path;
                retTransferObj.uri = treeItem.uri;
                retTransferObj.browserUri = treeItem.browserUri;
                retTransferObj.nodeRef = treeItem.nodeRef;
                retTransferObj.formId = treeItem.form;
                retTransferObj.formPagePath = treeItem.formPagePath;
                retTransferObj.isContainer = treeItem.container || treeItem.isContainer;
                retTransferObj.isComponent = treeItem.component;
                retTransferObj.isLevelDescriptor = treeItem.levelDescriptor;
                retTransferObj.inFlight = treeItem.inFlight;
                retTransferObj.editedDate = "";
                retTransferObj.modifier = "";
                retTransferObj.pathSegment = treeItem.name;
                retTransferObj.lockOwner = treeItem.lockOwner;
                retTransferObj.inProgress = treeItem.inProgress;
                retTransferObj.previewable = treeItem.previewable;
                var itemNameLabel = "Page";
                

                retTransferObj.status = CStudioAuthoring.Utils.getContentItemStatus(treeItem);
                retTransferObj.style = CStudioAuthoring.Utils.getIconFWClasses(treeItem); //, treeItem.container

                //spilt status and made it as comma seperated items.
                var statusStr = retTransferObj.status;
                if (retTransferObj.status.indexOf(" and ") != -1) {
                    var statusArray = retTransferObj.status.split(" and ");
                    if (statusArray &&  statusArray.length >= 2) {
                        statusStr = "";
                        for (var statusIdx=0; statusIdx<statusArray.length; statusIdx++) {
                            if (statusIdx == (statusArray.length - 1)) {
                                statusStr += statusArray[statusIdx];
                            } else {
                                statusStr += statusArray[statusIdx] + ", ";
                            }
                        }
                    }
                }

                if (treeItem.component) {
                    itemNameLabel = "Component";
                } else if (treeItem.document) {
                    itemNameLabel = "Document";
                }

                if (retTransferObj.internalName == "") {
                    retTransferObj.internalName = treeItem.name;
                }

                if (retTransferObj.internalName == "crafter-level-descriptor.level.xml") {
                    retTransferObj.internalName = "Section Defaults";
                }

                if (treeItem.isNew) {
                    retTransferObj.label = retTransferObj.internalName + " *";
                } else {
                    retTransferObj.label = retTransferObj.internalName;
                }

                if (treeItem.previewable == false) {
                    retTransferObj.style += " no-preview";
                }else{
                    retTransferObj.style += " preview";
                }

                if (treeItem.disabled == true) {
                    retTransferObj.style += " disabled";
                }

                if (treeItem.container == true) {
                    retTransferObj.fileName = treeItem.name;
                } else {
                    retTransferObj.fileName = "";
                }

                if (treeItem.userFirstName != undefined && treeItem.userLastName != undefined) {
                    retTransferObj.modifier = treeItem.userFirstName + " " + treeItem.userLastName;
                }

                var ttFormattedEditDate = "";
                if (treeItem.eventDate != "" && treeItem.eventDate != undefined) {
                    var formattedEditDate = CStudioAuthoring.Utils.formatDateFromString(treeItem.eventDate);
                    retTransferObj.editedDate = formattedEditDate;
                    ttFormattedEditDate = CStudioAuthoring.Utils.formatDateFromString(treeItem.eventDate, "tooltipformat");
                }

                if (treeItem.scheduled == true) {

                    retTransferObj.scheduledDate = treeItem.scheduledDate;

                    formattedSchedDate = CStudioAuthoring.Utils.formatDateFromString(treeItem.scheduledDate);
                    retTransferObj.formattedScheduledDate = formattedSchedDate;
                    var ttFormattedSchedDate = CStudioAuthoring.Utils.formatDateFromString(treeItem.scheduledDate, "tooltipformat");

                    retTransferObj.title = this.buildToolTipScheduled(
                            retTransferObj.label,
                            retTransferObj.style,
                            statusStr,
                            ttFormattedEditDate,
                            retTransferObj.modifier,
                            ttFormattedSchedDate,
                            itemNameLabel);
                } else {
                    retTransferObj.title = this.buildToolTipRegular(
                            retTransferObj.label,
                            retTransferObj.style,
                            statusStr,
                            ttFormattedEditDate,
                            retTransferObj.modifier,
                            retTransferObj.lockOwner,
                            itemNameLabel);
                }
                return retTransferObj;
            },
            /**
             * build the HTML for the scheduled tool tip.
             *
             */
            buildToolTipRegular: function(label, style, status, editedDate, modifier, lockOwner, itemNameLabel) {
                if (!itemNameLabel) {
                    itemNameLabel = "Page";
                }

                // this API will replace double quotes with ASCII character
                // to resolve page display issue
                label = CStudioAuthoring.Utils.replaceWithASCIICharacter(label);

                return sutils.format(
					[
                        "<table class='width100 acn-tooltip'>",
                            "<tr>",
                                "<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, itemNameLabel.toLowerCase())+": </td>",
                                "<td class='acn-width200'>",
                                    "<div class='acn-width200' style='word-wrap: break-word;'>{1}</div>",
                                "</td>",
                            "</tr>",
                            "<tr>",
                                "<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, "status")+":</td>",
                                "<td class='acn-width200'>",
                                    "<span class='{2}'></span>",
                                    "<span style='padding-left:2px; position:relative; bottom:2px;'>{3}</span>",
                                "</td>",
                            "</tr>",
                            "<tr>",
                                "<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, "lastEdited")+": </td>",
                                "<td class='acn-width200'>{4}</td>",
                            "</tr>",
                            "<tr>",
                                "<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, "editedBy")+": </td>",
                                "<td class='acn-width200'>{5}</td>",
                            "</tr>",
                            "<tr>",
                                "<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, "lockedBy")+": </td>",
                                "<td class='acn-width200'>{6}</td>",
                            "</tr>",
                        "</table>"
                    ].join(''),
					itemNameLabel, label, style, status, editedDate, modifier, lockOwner);
            },
            /**
             * build the HTML for the scheduled tool tip.
             *
             */
            buildToolTipScheduled: function(label, style, status, editedDate, modifier, schedDate, itemNameLabel) {
                if (!itemNameLabel) {
                    itemNameLabel = "Page";
                }

                // this API will replace double quotes with ASCII character
                // to resolve page display issue
                label = CStudioAuthoring.Utils.replaceWithASCIICharacter(label);

                return sutils.format([
					"<table class='width100 acn-tooltip'>",
						"<tr>",
							"<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, itemNameLabel.toLowerCase())+":</td>",
							"<td class='acn-width200'><div class='acn-width200' style='word-wrap: break-word;'>{1}</div></td>",
						"</tr><tr>",
							"<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, "status")+":</td>",
							"<td class='acn-width200'><span class='{2}'></span>",
							"<span style='padding-left:2px;'>{3}</span></td>",
						"</tr><tr>",
							"<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, "lastEdited")+": </td>",
							"<td class='acn-width200'>{4}</td>",
						"</tr><tr>",
							"<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, "editedBy")+": </td>",
							"<td class='acn-width200'>{5}</td>",
						"</tr><tr>",
							"<td class='tooltip-title'>"+CMgs.format(siteDropdownLangBundle, "scheduled")+": </td>",
							"<td class='acn-width200'>{6}</td>",
						"</tr>",
					"</table>"
				].join(""), itemNameLabel, label, style, status, editedDate, modifier, schedDate);
            },

			/** 
			 * render the context menu
			 */
			_renderContextMenu: function(target, p_aArgs, component, menuItems, oCurrentTextNode, isWrite) {
				    var aMenuItems;
                	var menuWidth = "auto";

                	component.lastSelectedTextNode = target.parentNode.parentNode.parentNode.parentNode;
                    var menuId = YDom.get(p_aArgs.id);
                    p_aArgs.clearContent();
					var d = document.createElement("div");
					d.className = "bd context-menu-load-msg";
					d.innerHTML = CMgs.format(siteDropdownLangBundle, "loading");
					menuId.appendChild(d);

                    var formPath = oCurrentTextNode.data.formPagePath,
                        isContainer = oCurrentTextNode.data.isContainer,
                        isComponent = oCurrentTextNode.data.isComponent,
                        isLevelDescriptor = oCurrentTextNode.data.isLevelDescriptor,
                        isLocked = (oCurrentTextNode.data.lockOwner != "" && oCurrentTextNode.data.lockOwner != CStudioAuthoringContext.user),
                        isInProgress = oCurrentTextNode.data.inProgress,
                        isLevelDescriptor = oCurrentTextNode.data.isLevelDescriptor,
                        isFolder = (isContainer && oCurrentTextNode.data.fileName != 'index.xml') ? true : false,
                        isOpen = null;

 
                    //Get user permissions to get read write operations
					var checkPermissionsCb = {
                        success: function(results) {
                            var isCreateFolder = CStudioAuthoring.Service.isCreateFolder(results.permissions);
                            var isCreateContentAllowed = CStudioAuthoring.Service.isCreateContentAllowed(results.permissions);
                            var isChangeContentTypeAllowed = CStudioAuthoring.Service.isChangeContentTypeAllowed(results.permissions);
                            // check if the user is allowed to edit the content
                            var isUserAllowed = CStudioAuthoring.Service.isUserAllowed(results.permissions);
                            var isDeleteAllowed = CStudioAuthoring.Service.isDeleteAllowed(results.permissions) && !isOpen;

		                    if(isLocked == true && isWrite == true) {
		                    	p_aArgs.addItems([ menuItems.viewOption ]);

                        		if (isContainer == true) {
                                    if (isCreateContentAllowed) {
                                        p_aArgs.addItems([ menuItems.newContentOption ]);
                                    }
		                        	p_aArgs.addItems([ menuItems.newFolderOption ]);
		                        }
	                        	p_aArgs.addItems([ menuItems.separator ]);
	                        	//The item is locked
								//p_aArgs.addItems([ menuItems.cutOption ]);
	                        	p_aArgs.addItems([ menuItems.copyOption ]);

								var checkClipboardCb = {
			                        success: function(collection) {
										var contextMenuItems = [];
										contextMenuItems = this.menuItems;
			                            this.args.addItems(contextMenuItems);

                                        if(oCurrentTextNode.instance.type == "Pages"){
                                            Self.myTree = Self.myTreePages;
                                        }else{
                                            Self.myTree = Self.myTreeComp;
                                        }

                                        if ((collection.count > 0 && isContainer) && collection.item[0].uri.replace(/\/\//g,"/") != oCurrentTextNode.data.uri) {
                                            if(Self.myTree.getNodeByProperty("uri", collection.item[0].uri.replace(/\/\//g,"/"))){
                                                if(Self.myTree.getNodeByProperty("uri", collection.item[0].uri.replace(/\/\//g,"/")).parent.contentElId != oCurrentTextNode.contentElId){
                                                    this.args.addItems([ menuItems.pasteOption ]);
                                                }
                                            }
                                            Self.copiedItem = Self.myTree.getNodeByProperty("uri", collection.item[0].uri.replace(/\/\//g,"/"));
                                        }

			                            this.args.render();
										menuId.removeChild(d);
			                        },
			                        failure: function() { },
			                        args: p_aArgs,
			                        menuItems: aMenuItems,
			                        menuEl: menuId,
			                        menuWidth: menuWidth,
			                        itemInProgress: isInProgress,
			                        item: oCurrentTextNode.data
			                    };

			                    CStudioAuthoring.Clipboard.getClipboardContent(checkClipboardCb);

			                    p_aArgs.render();
								menuId.removeChild(d);
		                    }
		                   	else if(!isWrite) {
		                   		p_aArgs.addItems([ menuItems.viewOption ]);

                                if (isComponent == true || isLevelDescriptor == true) {
                                    if (formPath == "" || formPath == undefined) {
                                    } else {
                                        if (!isUserAllowed) {
                                            if (isCreateContentAllowed) {
                                                p_aArgs.addItems([ menuItems.newContentOption ]);
                                            }
                                        } else {
                                            if (!isFolder && isChangeContentTypeAllowed) {
                                                p_aArgs.addItems([ menuItems.separator ]);
                                                p_aArgs.addItems([ menuItems.changeTemplateOption ]);
                                            }
                                        }
                                    }
                                } else {
                                    if (formPath == "" || formPath == undefined) {
                                        if (isUserAllowed) {
                                            if (isContainer == true) {
                                                if (isCreateContentAllowed) {
                                                    p_aArgs.addItems([ menuItems.newContentOption ]);
                                                }
                                            }
                                            if (!isFolder && isChangeContentTypeAllowed) {
                                                p_aArgs.addItems([ menuItems.separator ]);
                                                p_aArgs.addItems([ menuItems.changeTemplateOption ]);
                                            }
                                        }
                                    } else {
                                        if (isContainer == true) {
                                            if (isCreateContentAllowed) {
                                                p_aArgs.addItems([ menuItems.newContentOption ]);
                                            }
                                            if (isUserAllowed) {
                                                if (!isFolder && isChangeContentTypeAllowed) {
                                                    p_aArgs.addItems([ menuItems.separator ]);
                                                    p_aArgs.addItems([ menuItems.changeTemplateOption ]);
                                                }
                                            }
                                        }
                                    }
                                }

		                   		p_aArgs.render();
								menuId.removeChild(d);
		                   	}
		                   	else {
			                    if (isComponent == true || isLevelDescriptor == true) {
			                        if (formPath == "" || formPath == undefined) {
			                        	p_aArgs.addItems([ menuItems.viewOption ]);
			                        	if (isUserAllowed) {

                                            if (isDeleteAllowed) {
				                        	    p_aArgs.addItems([ menuItems.separator ]);
			                        		    p_aArgs.addItems([ menuItems.deleteOption ]);
			                        		}
			                        	}
			                        } else {
			                        	if (isUserAllowed) {
				                        	p_aArgs.addItems([ menuItems.editOption ]);
				                        	p_aArgs.addItems([ menuItems.viewOption ]);

				                        	if(isDeleteAllowed ||!isFolder && isChangeContentTypeAllowed ){
                                                p_aArgs.addItems([ menuItems.separator ]);
                                            }
				                        	if (isDeleteAllowed) {
				                        	    p_aArgs.addItems([ menuItems.deleteOption ]);
				                        	}
                                            if (!isFolder && isChangeContentTypeAllowed) {
                                                p_aArgs.addItems([ menuItems.changeTemplateOption ]);
                                            }

				                        	p_aArgs.addItems([ menuItems.separator ]);
				                        	p_aArgs.addItems([ menuItems.cutOption ]);
				                        	p_aArgs.addItems([ menuItems.copyOption ]);
			                        	} else {
				                        	p_aArgs.addItems([ menuItems.viewOption ]);
                                            if (isCreateContentAllowed) {
                                                p_aArgs.addItems([ menuItems.newContentOption ]);
                                            }
			                        	}
			                        }
			                    } else {
			                        if (formPath == "" || formPath == undefined) {
			                        	if (isCreateFolder == true) {
			                        		if (isContainer == true) {
                                                if (isCreateContentAllowed) {
                                                    p_aArgs.addItems([ menuItems.newContentOption ]);
                                                }
					                        	p_aArgs.addItems([ menuItems.newFolderOption ]);
					                        }
				                        	if (isUserAllowed) {
				                        		p_aArgs.addItems([ menuItems.separator ]);
				                        		if (isDeleteAllowed) {
					                        	    p_aArgs.addItems([ menuItems.deleteOption ]);
					                        	}
                                                if (!isFolder && isChangeContentTypeAllowed) {
                                                    p_aArgs.addItems([ menuItems.changeTemplateOption ]);
                                                }

					                        	p_aArgs.addItems([ menuItems.separator ]);
					                        	p_aArgs.addItems([ menuItems.cutOption ]);
					                        	p_aArgs.addItems([ menuItems.copyOption ]);
					                        }
				                        }
			                        	else {
			                        		if (isContainer == true) {
                                                if (isCreateContentAllowed) {
                                                    p_aArgs.addItems([ menuItems.newContentOption ]);
                                                }
				                        	} else if (isUserAllowed) {
					                        	p_aArgs.addItems([ menuItems.separator ]);
				                        		p_aArgs.addItems([ menuItems.deleteOption ]);
				                        	}
			                        	}
			                        } else {
			                        	if (isContainer == true) {
				                        	if (isCreateFolder == true) {
				                        		if (isUserAllowed) {
						                        	p_aArgs.addItems([ menuItems.editOption ]);
						                        	p_aArgs.addItems([ menuItems.viewOption ]);
                                                    if (isCreateContentAllowed) {
                                                        p_aArgs.addItems([ menuItems.newContentOption ]);
                                                    }
						                        	p_aArgs.addItems([ menuItems.newFolderOption ]);

						                        	p_aArgs.addItems([ menuItems.separator ]);
						                        	if (isDeleteAllowed) {
						                        	    p_aArgs.addItems([ menuItems.deleteOption ]);
						                        	}
						                        	if (!isFolder && isChangeContentTypeAllowed) {
                                                        p_aArgs.addItems([ menuItems.changeTemplateOption ]);
                                                    }

						                        	p_aArgs.addItems([ menuItems.separator ]);
						                        	p_aArgs.addItems([ menuItems.cutOption ]);
						                        	p_aArgs.addItems([ menuItems.copyOption ]);
						                        } else {
						                        	p_aArgs.addItems([ menuItems.viewOption ]);
                                                    if (isCreateContentAllowed) {
                                                        p_aArgs.addItems([ menuItems.newContentOption ]);
                                                    }
						                        	p_aArgs.addItems([ menuItems.newFolderOption ]);
						                        }
					                        } else {
				                        		if (isUserAllowed) {
						                        	p_aArgs.addItems([ menuItems.editOption ]);
						                        	p_aArgs.addItems([ menuItems.viewOption ]);
                                                    if (isCreateContentAllowed) {
                                                        p_aArgs.addItems([ menuItems.newContentOption ]);
                                                    }

						                        	if (!isFolder && isChangeContentTypeAllowed) {
                                                        p_aArgs.addItems([ menuItems.separator ]);
                                                        p_aArgs.addItems([ menuItems.changeTemplateOption ]);
                                                    }
						                        } else {
						                        	p_aArgs.addItems([ menuItems.viewOption ]);
                                                    if (isCreateContentAllowed) {
                                                        p_aArgs.addItems([ menuItems.newContentOption ]);
                                                    }
						                        }
					                        }
					                    } else {
				                        	if (isUserAllowed) {
					                        	p_aArgs.addItems([ menuItems.editOption ]);
					                        	p_aArgs.addItems([ menuItems.viewOption ]);

					                        	p_aArgs.addItems([ menuItems.separator ]);
					                        	if (isDeleteAllowed) {
					                        	    p_aArgs.addItems([ menuItems.deleteOption ]);
					                        	}
					                        	if (!isFolder && isChangeContentTypeAllowed) {
                                                    p_aArgs.addItems([ menuItems.changeTemplateOption ]);
                                                }

					                        	p_aArgs.addItems([ menuItems.separator ]);
					                        	p_aArgs.addItems([ menuItems.cutOption ]);
					                        	p_aArgs.addItems([ menuItems.copyOption ]);
					                        } else {
					                        	p_aArgs.addItems([ menuItems.viewOption ]);
					                        }
					                    }
			                        }
			                    }
			                    
			                    var checkClipboardCb = {
			                        success: function(collection) {
										var contextMenuItems = [];
										contextMenuItems = this.menuItems;
			                            this.args.addItems(contextMenuItems);

                                        if(oCurrentTextNode.instance.type == "Pages"){
                                            Self.myTree = Self.myTreePages;
                                        }else{
                                            Self.myTree = Self.myTreeComp;
                                        }

                                        if ((collection.count > 0 && isContainer) && collection.item[0].uri.replace(/\/\//g,"/") != oCurrentTextNode.data.uri) {
			                            	if(Self.myTree.getNodeByProperty("uri", collection.item[0].uri.replace(/\/\//g,"/"))){
                                                if(Self.myTree.getNodeByProperty("uri", collection.item[0].uri.replace(/\/\//g,"/")).parent.contentElId != oCurrentTextNode.contentElId){
                                                    this.args.addItems([ menuItems.pasteOption ]);
                                                }
                                            }
                                            Self.copiedItem = Self.myTree.getNodeByProperty("uri", collection.item[0].uri.replace(/\/\//g,"/"));
			                            }
										
			                            if(isUserAllowed) {
			                            	this.args.addItems([ menuItems.separator ]);
			                            	this.args.addItems([ menuItems.revertOption ]);
		                   	            }                   	                   				
		
                                        menuId.removeChild(d);  // Remove the "Loading ..." message
			                            this.args.render();     // Render the site dropdown's context menu
			                        },
			                        failure: function() { },
			                        args: p_aArgs,
			                        menuItems: aMenuItems,
			                        menuEl: menuId,
			                        menuWidth: menuWidth,
			                        itemInProgress: isInProgress,
			                        item: oCurrentTextNode.data
			                    };
			                    
			                    CStudioAuthoring.Clipboard.getClipboardContent(checkClipboardCb);
		                   	
		                   	} // end of else

                            if((oCurrentTextNode.data.lockOwner != ""
                            && CStudioAuthoringContext.role === "admin") 
                            || oCurrentTextNode.data.lockOwner === CStudioAuthoringContext.user ) {
                               p_aArgs.addItems([ menuItems.separator ]);
                                p_aArgs.addItems([ menuItems.unlockOption ]);
                            }                                                       

		                   	
	                 	},
                        failure: function() { }
                    };
					
                    checkPermissionsCb.isComponent = isComponent;
                    checkPermissionsCb.isLevelDescriptor = isLevelDescriptor;
                    checkPermissionsCb.aMenuItems = aMenuItems;
                    checkPermissionsCb.menuItems = menuItems;
                    checkPermissionsCb.menuWidth = menuWidth;
                    checkPermissionsCb.menuId = menuId;
                    checkPermissionsCb.p_aArgs = p_aArgs;
                    checkPermissionsCb.formPath = formPath;
                    checkPermissionsCb.d = d;
                    checkPermissionsCb.oCurrentTextNode = oCurrentTextNode;

                    CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, oCurrentTextNode.data.uri, {
                        success: function (itemTO) {
                            isOpen = itemTO.item.lockOwner !== "";
                            CStudioAuthoring.Clipboard.getPermissions.call({}, (oCurrentTextNode.data.uri), checkPermissionsCb);
                        },
                        failure: function () {
                            alert("Unable to look up item. Please contact your system administrator.");
                        }
                    }, false, false);
			},
			
			/**
			 * load context menu
			 */
            onTriggerContextMenu: function(tree, p_aArgs) {
                var target = p_aArgs.contextEventTarget;
                
                /* Get the TextNode instance that that triggered the display of the ContextMenu instance. */
                oCurrentTextNode = tree.getNodeByElement(target);

                /* If item is being processed (inFlight) dont display right context menu */
                if (oCurrentTextNode != null &&
                    oCurrentTextNode.data &&
                    oCurrentTextNode.data.inFlight) {
                    oCurrentTextNode = null;
                }
                
                var menuItems = {
                	separator: { text: "<div>&nbsp;</div>", disabled:true, classname:"menu-separator" },

					newContentOption: { text: CMgs.format(siteDropdownLangBundle, "newContent"), onclick: { fn: Self.createContent } },

					newFolderOption: { text: CMgs.format(siteDropdownLangBundle, "newFolder"), onclick: { fn: Self.createContainer } },

					editOption: { text: CMgs.format(siteDropdownLangBundle, "edit"), onclick: { fn: Self.editContent } },
					
					viewOption: { text: CMgs.format(siteDropdownLangBundle, "view"), onclick: { fn: Self.viewContent } },
					
					changeTemplateOption: { text: CMgs.format(siteDropdownLangBundle, "changeTemplate"), onclick: { fn: Self.changeTemplate, obj:tree } },

					deleteOption: { text: CMgs.format(siteDropdownLangBundle, "delete"), onclick: { fn: Self.deleteContent, obj:tree } },

					cutOption: { text: CMgs.format(siteDropdownLangBundle, "cut"), onclick: { fn: Self.cutContent, obj:tree } },
					
					copyOption: { text: CMgs.format(siteDropdownLangBundle, "copy"), onclick: { fn: Self.copyTree, obj:tree } },
					
					pasteOption: { text: CMgs.format(siteDropdownLangBundle, "paste"), onclick: { fn: Self.pasteContent} },

					revertOption: { text: CMgs.format(siteDropdownLangBundle, "history"), onclick: { fn: Self.revertContent, obj:tree } },
					
					unlockOption: { text: CMgs.format(siteDropdownLangBundle, "Unlock"), onclick: { fn: Self.unlockContent } }
				};
                p_aArgs.clearContent();

				var permsCallback = {
					success: function(response) {
						var isWrite = CStudioAuthoring.Service.isWrite(response.permissions);
						
						if(isWrite) {
							this._self._renderContextMenu(
								target,
								p_aArgs, 
								this.component, 
								menuItems, 
								oCurrentTextNode, 
								true);
						}
						else {
							this._self._renderContextMenu(
								target,
								p_aArgs, 
								this.component, 
								menuItems, 
								oCurrentTextNode, 
								false);
						}
					},
					
					failure: function() {
						this._self._renderContextMenu(
							target,
							p_aArgs, 
							this.component, 
							menuItems, 
							oCurrentTextNode, 
							false);
					},
					
					_self: this,
					component: Self    
				};

                if (oCurrentTextNode != null) {
				
					CStudioAuthoring.Service.getUserPermissions(
						CStudioAuthoringContext.site, 
						oCurrentTextNode.data.uri, 
						permsCallback);
                
                }else{
                	p_aArgs.clearContent();
                	p_aArgs.cancel();
                }

            },

            /**
             * unlock a content item
             */
            unlockContent: function() {
                var unlockCb = {
                    success: function() {
                        this.callingWindow.location.reload(true);
                    },
                    failure: function() { },
                    callingWindow: window
                };
                CStudioAuthoring.Service.unlockContentItem(
                        CStudioAuthoringContext.site,
                        oCurrentTextNode.data.uri,
                        unlockCb);
            },            
            /**
             * Creates new content. Opens the form to create content
             */
            createContent: function() {
                var createCb = {
                    success: function() {
                        this.callingWindow.location.reload(true);
                    },
                    failure: function() { },
                    callingWindow: window
                };
                CStudioAuthoring.Operations.createNewContent(
                        CStudioAuthoringContext.site,
                        oCurrentTextNode.data.path,
                        false,
                        createCb);
            },
            /**
             * Edits the label of the TextNode that was the target of the
             * "contextmenu" event that triggered the display of the
             * ContextMenu instance.
             */

            editContent: function() {
                var path = (oCurrentTextNode.data.uri);

                var editCb = {
                    success: function() {
                        this.callingWindow.location.reload(true);
                    },

                    failure: function() {
                    },

                    callingWindow: window
                };
                CStudioAuthoring.Operations.editContent(
                    oCurrentTextNode.data.formId,
                    CStudioAuthoringContext.site,path,
                    oCurrentTextNode.data.nodeRef, path, false, editCb);
            },

            /**
             * View the label of the TextNode that was the target of the
             * "contextmenu" event that triggered the display of the
             * ContextMenu instance.
             */

            viewContent: function() {
                var path = (oCurrentTextNode.data.uri);

                var viewCb = {
                    success: function() {
                        this.callingWindow.location.reload(true);
                    },

                    failure: function() {
                    },

                    callingWindow: window
                };

                CStudioAuthoring.Operations.viewContent(
                    oCurrentTextNode.data.formId,
                    CStudioAuthoringContext.site,path,
                    oCurrentTextNode.data.nodeRef, path, false, viewCb);
            },
            
			/**
			 * Creates new container, Opens a dialog box to enter folder name
			 */
			createContainer: function() {
				var createCb = {
					success: function() {
                        Self.refreshNodes(this.currentNode,false);
					},
					failure: function() { },
					callingWindow: window,
					currentNode: oCurrentTextNode
				};
				
				CStudioAuthoring.Operations.createFolder(
							CStudioAuthoringContext.site,
							oCurrentTextNode.data.uri,
							window,
							createCb);
			},

            /**
             * Revert the content item
             */
            revertContent: function(p_sType, p_aArgs, tree) {
				CStudioAuthoring.Operations.viewContentHistory(oCurrentTextNode.data);
            },

            /**
             * Deletes the TextNode that was the target of the "contextmenu"
             * event that triggered the display of the ContextMenu instance.
             */
            deleteContent: function(p_sType, p_aArgs, tree) {
				var dropDownWrap = YDom.get('acn-dropdown-menu-wrapper');
				if(dropDownWrap){
					//dropDownWrap.style.display = 'none';
				}				
                CStudioAuthoring.Operations.deleteContent(
                        [oCurrentTextNode.data]);
            },
            /**
             * copy content
             */
            copyContent: function(sType, args, tree) {

                var copyCb = {
                    success: function() {
                    },

                    failure: function() {
                    }
                };

                CStudioAuthoring.Clipboard.copyContent(oCurrentTextNode.data, copyCb);
            },
            /**
             * cut content
             */
            cutContent: function(sType, args, tree) {
				
				var parentTreeNode = oCurrentTextNode.getEl();
				var getChildNodeClass = YDom.getElementsByClassName("ygtvlp", null, parentTreeNode); 
				var isExpandableNode = YDom.getElementsByClassName("ygtvtp", null, parentTreeNode); 				
				
				if(oCurrentTextNode.hasChildren() || getChildNodeClass.length > 0 || isExpandableNode.length > 0){
					// alert("The page and its child pages have been cut to the clipboard");
				}
				
                var uri = oCurrentTextNode.data.uri;
                Self.cutItem = oCurrentTextNode;

				if(uri.lastIndexOf("index.xml")==-1){
					var serviceUri = CStudioAuthoring.Service.getPagesServiceUrl + "?site=" + CStudioAuthoringContext.site + "&path=" + uri + "&depth=-1&order=default";
					
				}
				else {
	                var folderPath = uri.substring(0, uri.lastIndexOf("index.xml"));                

    	            var serviceUri = CStudioAuthoring.Service.getPagesServiceUrl + "?site=" + CStudioAuthoringContext.site + "&path=" + folderPath + "&depth=-1&order=default";
				}
				
                var getTreeItemReuest = CStudioAuthoring.Service.createServiceUri(serviceUri);

                try { 
					
					var treeInner = YDom.get('acn-dropdown-menu-inner');
					var previousCutEl = YDom.getElementsByClassName("status-icon", null, treeInner);
					for(var i=0; i<previousCutEl.length; i++){
						if(previousCutEl[i].style.color == Self.CUT_STYLE_RGB || previousCutEl[i].style.color == Self.CUT_STYLE ){
							previousCutEl[i].style.color = '';					
						}						
					}
					
					YDom.setStyle(oCurrentTextNode.labelElId, "color", Self.CUT_STYLE);
					if(oCurrentTextNode.hasChildren()){						
						var getTextNodes = YDom.getElementsByClassName("status-icon", null, parentTreeNode); 
						for(var i=0; i<getTextNodes.length; i++){
							getTextNodes[i].style.color = Self.CUT_STYLE;
						}						
					}
					
				} catch (ex) {  }

                //CStudioAuthoring.Operations.openCopyDialog(CStudioAuthoringContext.site, oCurrentTextNode.data.uri, assignTemplateCb, args);
                var cutCb = {
                    success: function(response) {

                        var content = YAHOO.lang.JSON.parse(response.responseText);

                        var item = content.item;
                        var jsonString= YAHOO.lang.JSON.stringify(item);
                        var jsonArray="{\"item\":["+jsonString+"]}";
                        var cutRequest = CStudioAuthoringContext.baseUri + "/api/1/services/api/1/clipboard/cut-item.json?site=" + CStudioAuthoringContext.site;

                        var onComplete = {
                            success:function(response) {
                                
                            },
                            failure: function() {
                            }

                        };

                        YAHOO.util.Connect.setDefaultPostHeader(false);
                        YAHOO.util.Connect.initHeader("Content-Type", "application/json; charset=utf-8");
                        YAHOO.util.Connect.asyncRequest('POST', cutRequest, onComplete, jsonArray);

                    },
                    failure:function(response) {

                    }

                };

               YConnect.asyncRequest('GET', getTreeItemReuest, cutCb);
            },
            /**
             * paste content to selected location
             */
            pasteContent: function(sType, args, tree) {
                //Check source and destination paths.
                if ((Self.cutItem != null && Self.cutItem.contentElId == oCurrentTextNode.contentElId) ||
                    (Self.copiedItem != null && (Self.copiedItem.contentElId == oCurrentTextNode.contentElId) || Self.copiedItem == oCurrentTextNode.data.uri) ||
                    (Self.copiedItem != null && Self.copiedItem.parent.contentElId == oCurrentTextNode.contentElId)){
                    alert("Source and destination path are same");
                    return false;
                }

                window.pasteFlag = true;
                var pasteCb = {
                    success: function(result) {
                        try {
                            var errorMsgExist=false;
                            var errorMsg='';
                            if(!result.success){
                                if(typeof result.message!= 'undefined' && typeof result.message.paths != 'undefined') {
                                    errorMsg = result.message.paths[0];
                                    if(errorMsg!='') {
                                        errorMsgExist=true;
                                    }
                                }
                            }

                            Self.refreshNodes(this.tree,!errorMsgExist);

                            if (typeof WcmDashboardWidgetCommon != 'undefined') {
                                var myRecentActivitiesInstace = WcmDashboardWidgetCommon.dashboards["MyRecentActivity"];
                                var filterByTypeEl = YDom.get('widget-filterBy-'+myRecentActivitiesInstace.widgetId);
                                var filterByTypeValue = 'all';
                                if(filterByTypeEl && filterByTypeEl.value != '') {
                                    filterByTypeValue = filterByTypeEl.value;
                                }

                                var searchNumberEl = YDom.get('widget-showitems-'+myRecentActivitiesInstace.widgetId);
                                var searchNumberValue =  myRecentActivitiesInstace.defaultSearchNumber;
                                if(searchNumberEl && searchNumberEl.value != '') {
                                    searchNumberValue = searchNumberEl.value;
                                }

                                WcmDashboardWidgetCommon.loadFilterTableData(
                                    myRecentActivitiesInstace.defaultSortBy,
                                    YDom.get(myRecentActivitiesInstace.widgetId),
                                    myRecentActivitiesInstace.widgetId,
                                    searchNumberValue,filterByTypeValue);
                            }

                            //code below to alert user if destination node url already exist during cut/paste
                            if (errorMsgExist && errorMsg=='DESTINATION_NODE_EXIST'){
                                alert("Page already exist at the destination");
                            }
                        } catch(e) { }
                    },

                    failure: function() {
                    },

                    tree: oCurrentTextNode
                };

                try{					
					YDom.addClass(oCurrentTextNode.getLabelEl().parentNode.previousSibling, "ygtvloading");
				}catch(e){}
				
				CStudioAuthoring.Clipboard.pasteContent(oCurrentTextNode.data, pasteCb);
            },


            copyTree:function(sType, args, tree) {

                var assignTemplateCb = {
                    success: function(selectedType) {

                    },

                    failure: function() {
                    },

                    activeNode: oCurrentTextNode
                };

                if(oCurrentTextNode.instance.label == "Pages"){
                    Self.myTree = Self.myTreePages;
                }else{
                    Self.myTree = Self.myTreeComp;
                }

                Self.copiedItem = Self.myTree.getNodeByProperty("path", oCurrentTextNode.data.path);
                Self.copiedItem ? null : Self.copiedItem = oCurrentTextNode;
                
                
                // if the tree does not have child do not open the copy dialoge
                // only call the copy content function
                if (oCurrentTextNode.isLeaf) {
                	
                	var copyContext = {
                    	"heading":"Copy",
                    	"description":"Please select any of the sub-pages you would like to batch copy.<br/> When pasting, any selected sub-pages and their positional heirarchy will be retained",
                    	"actionButton":"Copy"
                	};
                	
                	var site = CStudioAuthoringContext.site;
                	
                	var context = copyContext;
                	context.request = CStudioAuthoringContext.baseUri + CStudioAuthoring.Service.copyServiceUrl + "?site=" + site;
                	
                	var uri = oCurrentTextNode.data.uri; 
                	
                	var folderPath = uri;
                    if (uri.indexOf("index.xml") != -1) {
                        folderPath = uri.substring(0, uri.lastIndexOf("index.xml"));
                    }
                	
                	var openCopyDialog = {
        				success:function(response) {
        				   var copyTree= eval("(" + response.responseText + ")");
        				   this.copyTree = copyTree;
	        				var newItem = {};
	       	                newItem.uri = this.copyTree.item.uri;//Fixed for EMO-8742
	       	                var rootItem = newItem;
	       	                var pasteFormatItem = {};
	       	                pasteFormatItem.item = [];
	       	                pasteFormatItem.item.push(rootItem);
	       	                
	       	                var myJSON = YAHOO.lang.JSON.stringify(pasteFormatItem);
	       	                var oncomplete = {
	       	                    success:function() {
	       	                        CStudioAuthoring.ContextualNav.WcmRootFolder.resetNodeStyles();
	       	                    },
	       	                    failure:function() {
	       	  
	       	                    }
	       	                };  
	       	                var request = this.args['request'];
	       	                YAHOO.util.Connect.setDefaultPostHeader(false);
	       	                YAHOO.util.Connect.initHeader("Content-Type", "application/json; charset=utf-8");
	       	                YAHOO.util.Connect.asyncRequest('POST', request, oncomplete, myJSON);
        				},
        				failure:function() {

        				},
        				args : context
        			};
        			var serviceUri = CStudioAuthoring.Service.getPagesServiceUrl + "?site=" + site + "&path=" + folderPath + "&depth=-1&order=default";
        			var getCopyTreeItemReuest = CStudioAuthoring.Service.createServiceUri(serviceUri);
        			YConnect.asyncRequest('GET', getCopyTreeItemReuest, openCopyDialog);
                } else {
                	CStudioAuthoring.Operations.openCopyDialog(
                        CStudioAuthoringContext.site,
                        oCurrentTextNode.data.uri,
                        assignTemplateCb, args);
                }

            },
			cutTree:function(sType, args, tree){
				args.cut=true;
                var serviceUri = CStudioAuthoring.Service.getPagesServiceUrl + "?site=" + site + "&path=" + folderPath + "&depth=-1&order=default";
				var getCopyTreeItemReuest = CStudioAuthoring.Service.createServiceUri(serviceUri);
				YConnect.asyncRequest('GET', getCopyTreeItemReuest, openCopyDialog);
                CStudioAuthoring.Operations.openCopyDialog(sType,args,tree);
				
			},
            /**
             * change template for given item
             */
            changeTemplate: function(sType, args, tree) {

                var modalBody = YDom.get("cstudio-wcm-popup-div");
                if (modalBody === null) {
                    modalBody = document.createElement("div");
                    modalBody.setAttribute("id", "cstudio-wcm-popup-div");
                    document.body.appendChild(modalBody);
                }

                var continueFn = function continueFn (e) {
                    e.preventDefault();

                    var assignTemplateCb = {
                        success: function(selectedType) {
                            var path = (this.activeNode.data.uri);

                            var editCb = {
                                success: function() {
                                    this.callingWindow.location.reload(true);
                                },

                                failure: function() {
                                },

                                callingWindow: window
                            };
                            /* reload dashboard is heavy, to reflect changed content-type */
                            //window.location.reload(true);
                            //this.activeNode.data.formId = selectedType;
                            var auxParams = new Array();
                            var param1 = {};
                            param1['name'] = "draft";
                            param1['value'] = "true";
                            var param2 = {};
                            param2['name'] = "changeTemplate";
                            param2['value'] = selectedType;
                            auxParams.push(param1);
                            auxParams.push(param2);
                            CStudioAuthoring.Operations.editContent(
                                    selectedType,
                                    CStudioAuthoringContext.site,
                                    path,
                                    this.activeNode.data.nodeRef, path, false, editCb,auxParams);
                        },
                        failure: function() { },
                        activeNode: oCurrentTextNode
                    };
                    dialog.destroy();
                    CStudioAuthoring.Operations.assignContentTemplate(
                            CStudioAuthoringContext.site,
                            CStudioAuthoringContext.user,
                            oCurrentTextNode.data.uri,
                            assignTemplateCb,
                            oCurrentTextNode.data.formId);
                };

                var cancelFn = function cancelFn (e) {
                    e.preventDefault();
                    dialog.destroy();
                }

                modalBody.innerHTML = '<div class="contentTypePopupInner changeContent-type-dialog" style="width:460px;height:140px;">' +
                                        '<div class="contentTypePopupContent">' +
                                            '<form name="contentFromWCM">' +
                                            '<div class="contentTypePopupHeader">' + CMgs.format(formsLangBundle, "changeTemplateDialogTitle")+ '</div> ' +
                                            '<div class="contentTypeOuter">'+
                                                '<div>' + CMgs.format(formsLangBundle, "changeTemplateDialogBody")+ '</div>' +
                                            '</div>' +    
                                            '<div class="contentTypePopupBtn">' +
                                                '<input type="submit" class="btn btn-primary ok" id="acceptCTChange" value="' +CMgs.format(formsLangBundle, 'yes')+ '" />' +
                                                '<input type="submit" class="btn btn-default cancel" id="cancelCTChange" value="' +CMgs.format(formsLangBundle, 'no')+ '" />' +
                                            '</div>' +
                                            '</form> ' +
                                        '</div>' +
                                      '</div>';

                var dialog = new YAHOO.widget.Dialog("cstudio-wcm-popup-div", 
                                { fixedcenter : true,
                                  effect:{
                                    effect: YAHOO.widget.ContainerEffect.FADE,
                                    duration: 0.25
                                  }, 
                                  visible : false,
                                  modal:true,
                                  close:false,
                                  constraintoviewport : true,
                                  underlay:"none",
                                  zIndex: 100000
                                });

                dialog.render();

                YAHOO.util.Event.addListener("acceptCTChange", "click", continueFn);
                YAHOO.util.Event.addListener("cancelCTChange", "click", cancelFn);

                dialog.show();
            },
            /**
             * given a path, attempt to open the folder to that path
             */
            openTreeToPath: function(instance, path) {
                // open first level
                this.toggleFolderState(instance, this.ROOT_OPEN, path);
            },
            /**
             * add hover effect to context nav items.
             */
            nodeHoverEffects: function(e) {
                var YDom = YAHOO.util.Dom,
                        highlightWrpClass = "highlight-wrapper",
                        highlightColor = "#e2e2e2",
                        overSetClass = "over-effect-set", // class to identify elements that have their over/out effect initialized
                        spanNodes = YAHOO.util.Selector.query("span.yui-resize-label:not(." + overSetClass + ")", "acn-dropdown-menu-wrapper"),
                        moverFn = function(evt) {
                            var el = this,
                                    wrapEl = function(table) {
                                        var wrp = document.createElement('div');
                                        wrp.setAttribute('style', 'background-color:' + highlightColor);
                                        wrp.setAttribute('class', highlightWrpClass);
                                        YDom.insertBefore(wrp, table);
                                        wrp.appendChild(table);
                                        return wrp;
                                    };
                            if (YDom.hasClass(el, highlightWrpClass)) {
                                YDom.setStyle(el, 'background-color', highlightColor)
                            } else if (YDom.hasClass(el, 'ygtvitem')) {
                                var firstChild = YDom.getFirstChild(el);
                                YDom.hasClass(firstChild, highlightWrpClass)
                                        ? YDom.setStyle(firstChild, 'background-color', highlightColor)
                                        : wrapEl(firstChild)
                            } else {
                                var parent = el.parentNode;
                                YDom.hasClass(parent, highlightWrpClass)
                                        ? YDom.setStyle(parent, 'background-color', highlightColor)
                                        : wrapEl(el);
                            }
                            if(Self.lastSelectedTextNode != null) {
                            	var currentlySelectedTextNode = el
                            	if(currentlySelectedTextNode == Self.lastSelectedTextNode) return;
                            	(YDom.hasClass(Self.lastSelectedTextNode, highlightWrpClass)
                                        ? Self.lastSelectedTextNode
                                        : (YDom.hasClass(Self.lastSelectedTextNode, 'ygtvitem')
                                        ? YDom.getFirstChild(Self.lastSelectedTextNode)
                                        : Self.lastSelectedTextNode.parentNode))
                                        .style.backgroundColor = "";
                            	
                            	Self.lastSelectedTextNode = null;
                            }
                        },
                        moutFn = function(evt) {
                        	if(Self.lastSelectedTextNode != null) return;
                            var el = this;
                            (YDom.hasClass(el, highlightWrpClass)
                                    ? el
                                    : (YDom.hasClass(el, 'ygtvitem')
                                    ? YDom.getFirstChild(el)
                                    : el.parentNode))
                                    .style.backgroundColor = "";
                        };
                for (var i = 0,
                        l = spanNodes.length,
                        span = spanNodes[0],
                        barItem;
                     i < l;
                     i++,span = spanNodes[i]
                        ) {
                    // span -> td -> tr -> tbody -> table
                    barItem = span.parentNode.parentNode.parentNode.parentNode;
                    if (barItem) {
                        YEvent.addListener(barItem, "mouseover", moverFn);
                        YEvent.addListener(barItem, "mouseout", moutFn);
                        YDom.addClass(span, overSetClass);
                    }
                }
            },

            setChildrenStyles: function(treeNode) {
                var parentNode = treeNode.getContentEl();
                if (parentNode.children[0] &&
                    (parentNode.children[0].style.color == Self.CUT_STYLE_RGB ||
                     parentNode.children[0].style.color == Self.CUT_STYLE)) {
                    for (var chdIdx = 0; chdIdx < treeNode.children.length; chdIdx++) {
                        var chdEl = treeNode.children[chdIdx].getContentEl();
                        if (chdEl && chdEl.children[0]) {
                            chdEl.children[0].style.color = Self.CUT_STYLE
                        }
                    }
                }
            },

            resetNodeStyles: function() {
                var treeInner = YDom.get('acn-dropdown-menu-inner');
                var previousCutEl = YDom.getElementsByClassName("status-icon", null, treeInner);
                for(var i=0; i<previousCutEl.length; i++) {
                    if(previousCutEl[i].style.color == Self.CUT_STYLE_RGB || previousCutEl[i].style.color == Self.CUT_STYLE ) {
                        previousCutEl[i].removeAttribute("style");
                    }
                }
            }
        }
    });
    Self.treePathOpenedEvt.fireCount = 0;
    /**
     * instance object
     * CStudioAuthoring.ContextualNav.WcmRootFolder is static
     */
    CStudioAuthoring.ContextualNav.WcmRootFolderInstance = function(config) {

        ++(CStudioAuthoring.ContextualNav.WcmRootFolder.instanceCount);

        this._self = this;
        this._toggleState = CStudioAuthoring.ContextualNav.WcmRootFolder.ROOT_CLOSED;
        this.rootFolderEl = null;
        //this.instanceId = ++(CStudioAuthoring.ContextualNav.WcmRootFolder.instanceCount);

        this.type = config.name;
        this.label = config.params["label"];
        this.path = config.params["path"];
        this.showRootItem = (config.params["showRootItem"]) ? config.params["showRootItem"] : false;
        this.onClickAction = (config.params["onClick"]) ? config.params["onClick"] : "";
        this.config = config;

    }
    CStudioAuthoring.Module.moduleLoaded("wcm-root-folder", CStudioAuthoring.ContextualNav.WcmRootFolder);
})();