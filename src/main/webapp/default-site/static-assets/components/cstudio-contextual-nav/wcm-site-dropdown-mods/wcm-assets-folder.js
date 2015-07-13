var YDom = YAHOO.util.Dom;
var YEvent = YAHOO.util.Event;


/**
 * WcmAssetsFolder
 * A root level folder is a configurable folder element that can be based at any
 * point along a wcm path.
 */

CStudioAuthoring.ContextualNav.WcmAssetsFolder = CStudioAuthoring.ContextualNav.WcmAssetsFolder || {

    ROOT_OPEN: "open",
    ROOT_CLOSED: "closed",
    ROOT_TOGGLE: "toggle",
    Self: this,

    /**
     * initialize module
     */
    initialize: function(config) {

        if(config.name == "wcm-assets-folder") {

            var instance = new CStudioAuthoring.ContextualNav.WcmAssetsFolderInstance(config);

            this.addContentTreeRootFolder(instance);
        }
    },

    /**
     * add a root level folder to the content drop down
     */
    addContentTreeRootFolder: function(instance) {
        var folderListEl =  instance.config.containerEl;

        var parentFolderEl = document.createElement("div");

        var parentFolderLinkEl = document.createElement("a");
        parentFolderLinkEl.id = instance.label.toLowerCase() + "-tree";
        parentFolderLinkEl.innerHTML = instance.label;
        parentFolderLinkEl.onclick = CStudioAuthoring.ContextualNav.WcmAssetsFolder.onRootFolderClick;
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
        Self.myTree = tree;
        tree.setDynamicLoad(this.onLoadNodeDataOnClick);
        tree.FOCUS_CLASS_NAME = null;

        var label = treeEl.previousElementSibling;
        YDom.addClass(label, "loading");

        CStudioAuthoring.Service.lookupSiteContent(site, rootPath, 1, "default", {
            openToPath: pathToOpen,
            success: function(treeData) {

                var items = treeData.item.children;

                if(instance.showRootItem) {
                    items = new Array(treeData.item);
                }

                CStudioAuthoring.ContextualNav.WcmAssetsFolder.drawTree(items, tree, path, instance);

                //add hover effect to nodes
                CStudioAuthoring.ContextualNav.WcmAssetsFolder.nodeHoverEffects(this);

                YDom.removeClass(label, "loading");
            },
            failure: function() {
                YDom.removeClass(label, "loading");
            }
        });
    },

    /**
     * render function called on root level elements
     */
    drawTree: function(treeItems, tree, pathToOpenTo, instance) {

        var treeNodes = new Array();
        var treeNodesLabels = new Array();
        var currentLevelPath = null;
        var remainingPath = null;
        var nodeToOpen = null;
        var contextMenuPrefix = "ContextMenu-";
        var contextMenuId = contextMenuPrefix + tree.id;

        if(pathToOpenTo != null && pathToOpenTo != undefined) {
            var pathParts = pathToOpenTo.split("/");

            if(pathParts.length >= 2) {
                currentLevelPath = "/"+pathParts[1];
                remainingPath = pathToOpenTo.substring(currentLevelPath.length+1);
            }
        }

        for (var i=0; i<treeItems.length; i++) {
            var treeNodeTO = this.createTreeNodeTransferObject(treeItems[i]);

            if (treeNodeTO.isContainer == true) {
                treeNodeTO.style = "acn-parent-folder";
            }
            var treeNode = this.drawTreeItem(treeNodeTO, tree.getRoot());
            treeNode.instance = instance;

            if(pathToOpenTo != null && treeNode != null) {
                if(treeNodeTO.pathSegment == "index.xml") {
                    if(CStudioAuthoring.Utils.endsWith(treeNodeTO.path, currentLevelPath)) {
                        nodeToOpen = treeNode;
                    }
                }
            }

            treeNodes.push(treeNode);
            treeNodesLabels.push(tree.root.children[i].labelElId);

        }

        tree.subscribe('clickEvent', function(args) {
            CStudioAuthoring.ContextualNav.WcmAssetsFolder.onTreeNodeClick(args.node);
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
            contextMenuId,
            {
                container: "acn-context-menu",
                trigger: "acn-dropdown-menu-wrapper",
                shadow: false,
                lazyload: true,
                zIndex: 100000
            }
        );

        contextMenu.subscribe('beforeShow', function() {
            CStudioAuthoring.ContextualNav.WcmAssetsFolder.onTriggerContextMenu(tree, this, contextMenuId);
        }, tree, false);

        contextMenu.subscribe('show', function() {
            if (!YDom.isAncestor(tree.id, this.contextEventTarget)) {
                this.hide();
            }
        }, tree, false);

        tree.draw();

        if(nodeToOpen != null) {
            // opening to a specific path
            nodeToOpen.expand();
            nodeToOpen.openToPath = remainingPath;
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

        if(pathToOpenTo) {
            var pathParts = pathToOpenTo.split("/");

            if(pathParts.length >= 2) {
                currentLevelPath = "/"+pathParts[0];
                remainingPath = pathToOpenTo.substring(currentLevelPath.length);
            }
        }

        for (var i=0; i<treeItems.length; i++) {
            var treeNodeTO = this.createTreeNodeTransferObject(treeItems[i]);

            if(treeNodeTO.isContainer) {
                treeNodeTO.style = "acn-parent-folder no-preview";
            }

            var treeNode = this.drawTreeItem(treeNodeTO, root);
            treeNode.instance = instance;

            if(pathToOpenTo != null && treeNode != null) {
                if(CStudioAuthoring.Utils.endsWith(treeNodeTO.path, currentLevelPath)) {
                    nodeToOpen = treeNode;
                }
            }

            treeNodes.push(treeNode);
            if(root.children[i]) {
                treeNodesLabels.push(root.children[i].labelElId);
            } else {
                treeNodesLabels.push(treeNode.labelElId);
            }
        }

        if(nodeToOpen) {
            nodeToOpen.expand();
            //nodeToOpen.openToPath = remainingPath;
        }
    },

    /**
     * render a tree item
     */
    drawTreeItem: function(treeNodeTO, root) {
        if (treeNodeTO.container == true || treeNodeTO.name != 'index.xml') {

            var treeNode = new YAHOO.widget.TextNode(treeNodeTO, root, false);
            if(!Self.treeNodes)
                Self.treeNodes = [];

            Self.treeNodes[""+treeNode.labelElId] = treeNodeTO;
            treeNode.labelStyle = treeNodeTO.style;

            if(treeNodeTO.previewable == false) {
                treeNode.labelStyle += " no-preview";
            }
            treeNode.labelStyle+= "  yui-resize-label";
            treeNode.nodeType = "CONTENT";
            treeNode.treeNodeTO = treeNodeTO;
            treeNode.renderHidden = true;
            treeNode.nowrap = true;

            if(!treeNodeTO.isContainer) {
                treeNode.isLeaf = true;
            }
        }

        return treeNode;
    },

    /**
     * method fired when user clicks on the root level folder
     */
    onRootFolderClick: function() {
        var WcmAssetsFolder = CStudioAuthoring.ContextualNav.WcmAssetsFolder;

        WcmAssetsFolder.toggleFolderState(this.componentInstance, WcmAssetsFolder.ROOT_TOGGLE);
    },

    /**
     * toggle folder state
     */
    toggleFolderState: function(instance, forceState, path) {
        var WcmAssetsFolder = CStudioAuthoring.ContextualNav.WcmAssetsFolder;

        if(forceState != null && forceState != WcmAssetsFolder.ROOT_TOGGLE) {
            // force
            if(forceState == WcmAssetsFolder.ROOT_OPEN) {
                instance.rootFolderEl.style.display = 'block';
                instance.state = WcmAssetsFolder.ROOT_OPEN;
                this.initializeContentTree(instance.rootFolderEl, path, instance);
            }
            else {
                instance.rootFolderEl.style.display = 'none';
                instance.state = WcmAssetsFolder.ROOT_CLOSED;
            }
        }
        else {

            // toggle
            if(instance.state == WcmAssetsFolder.ROOT_OPEN) {
                this.toggleFolderState(instance,  WcmAssetsFolder.ROOT_CLOSED, path);
            }
            else {
                this.toggleFolderState(instance,  WcmAssetsFolder.ROOT_OPEN, path);
            }
        }
    },

    /**
     * method fired when tree node is expanded for first time
     */
    onLoadNodeDataOnClick: function(node, fnLoadComplete)  {

        var path = encodeURI(node.treeNodeTO.path);
        var site = node.treeNodeTO.site;
        var pathToOpenTo = node.openToPath;

        CStudioAuthoring.Service.lookupSiteContent(site, path, 1, "default", {
            success: function(treeData, args) {
                CStudioAuthoring.ContextualNav.WcmAssetsFolder.drawSubtree(treeData.item.children, node, args.pathToOpenTo, args.instance);

                args.fnLoadComplete();

                //add hove effect to nodes
                CStudioAuthoring.ContextualNav.WcmAssetsFolder.nodeHoverEffects(this);
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
        });
    },

    /**
     * method fired when tree item is clicked
     */
    onTreeNodeClick: function(node)	{
        if (!node.data.isLevelDescriptor && !node.data.isContainer) {
            CStudioAuthoring.Operations.openPreview(node.data, "", false, false);
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
        retTransferObj.sandboxId = treeItem.sandboxId;
        retTransferObj.link="/NOTSET";
        retTransferObj.path = treeItem.path;
        retTransferObj.uri = treeItem.uri;
        retTransferObj.browserUri = treeItem.browserUri;
        retTransferObj.nodeRef = treeItem.nodeRef;
        retTransferObj.formId = treeItem.form;
        retTransferObj.formPagePath = treeItem.formPagePath;
        retTransferObj.isContainer = treeItem.container;
        retTransferObj.isComponent = true;
        retTransferObj.isLevelDescriptor = treeItem.levelDescriptor;
        retTransferObj.editedDate = "";
        retTransferObj.modifier = "";
        retTransferObj.pathSegment = treeItem.name;
        retTransferObj.sandboxLockOwner = treeItem.sandboxLockOwner;
        retTransferObj.sandboxLockStore = treeItem.sandboxLockStore;
        retTransferObj.scheduledDate = treeItem.scheduledDate;
        retTransferObj.previewable = treeItem.previewable;

        treeItem.component = true;

        retTransferObj.status = CStudioAuthoring.Utils.getContentItemStatus(treeItem);
        retTransferObj.style = CStudioAuthoring.Utils.getContentItemClassName(treeItem);//, treeItem.container

        if(retTransferObj.internalName == "") {
            retTransferObj.internalName = treeItem.name;
        }

        if(retTransferObj.internalName == "crafter-level-descriptor.level.xml") {
            retTransferObj.internalName = "Section Defaults";
        }

        if(treeItem.newFile) {
            retTransferObj.label = retTransferObj.internalName + "*";
        }
        else {
            retTransferObj.label = retTransferObj.internalName;
        }

        if(treeItem.container == true) {
            retTransferObj.fileName = treeItem.name;
        }
        else {
            retTransferObj.fileName = "";
        }

        if (treeItem.userFirstName != undefined && treeItem.userLastName != undefined) {
            retTransferObj.modifier = treeItem.userFirstName + " " + treeItem.userLastName;
        }

        if(treeItem.eventDate != "" && treeItem.eventDate != undefined) {
            var formattedEditDate = CStudioAuthoring.Utils.formatDateFromString(treeItem.eventDate);
            retTransferObj.editedDate = formattedEditDate;
        }

        return retTransferObj;
    },

    onTriggerContextMenu: function(tree, p_aArgs, contextMenuId)	{

        target = p_aArgs.contextEventTarget;
        var aMenuItems;
        var menuWidth = "80px";
        var menuItems = {
            "assetsFolderMenu" : [
                { text: "Upload", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.uploadAsset, obj:tree } },
                { text: "Create&nbsp;Folder", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.createContainer, obj:tree } },
                { text: "Delete", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.deleteContainer, obj:tree } }
            ],
            "assetsFolderMenuNoDelete" : [
                { text: "Upload", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.uploadAsset, obj:tree } },
                { text: "Create&nbsp;Folder", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.createContainer, obj:tree } }
            ],
            "assetsFolderMenuNoCreateFolder" : [
                { text: "Upload", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.uploadAsset, obj:tree } },
                { text: "Delete", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.deleteContainer, obj:tree } }
            ],
            "assetsFolderMenuNoDeleteNoCreateFolder" : [
                { text: "Upload", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.uploadAsset, obj:tree } }
            ],
            "assetsMenu" : [
                { text: "Upload", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.overwriteAsset, obj:tree } },
                { text: "Delete", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.deleteContent, obj:tree } }
            ],
            "assetsMenuNoDelete" : [
                { text: "Upload", onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.overwriteAsset, obj:tree } }
            ],
            "assetsFolderMenuRead" : [
                { text: "No Actions&nbsp;Available", disabled: true, onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.uploadAsset, obj:tree } }
            ],

            "assetsFolderTemplate" : [
                { text: "Create&nbsp;Template", disabled: false, onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.createNewTemplate, obj:tree } }
            ],


            "assetsFolderScript" : [
                { text: "Create&nbsp;Controller", disabled: false, onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.createNewScript, obj:tree } }
            ],

            "assetsMenuRead" : [
                { text: "Upload", disabled: true, onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.overwriteAsset, obj:tree } },
                { text: "Delete", disabled: true, onclick: { fn: CStudioAuthoring.ContextualNav.WcmAssetsFolder.deleteContent, obj:tree } }
            ]
        };

        var targetNode = tree.getNodeByElement(target);

        if ( targetNode != null && YDom.isAncestor(tree.id, p_aArgs.contextEventTarget) ) {
            // Get the TextNode instance that that triggered the display of the ContextMenu instance.
            oCurrentTextNode = targetNode;

            var CSA = CStudioAuthoring;
            var formPath = oCurrentTextNode.data.formPagePath;
            var isContainer = oCurrentTextNode.data.isContainer;
            var isComponent = oCurrentTextNode.data.isComponent;
            var isLevelDescriptor = oCurrentTextNode.data.isLevelDescriptor;
            var menuId = YDom.get(contextMenuId);
            var isAssetsFolder = (oCurrentTextNode.instance.type == "wcm-assets-folder")? true : false;
            p_aArgs.clearContent();

            //Get user permissions to get read write operations
            var checkPermissionsCb = {
                success: function(results) {
                    var perms = results.permissions,
                        isWrite = CSA.Service.isWrite(perms),
                        isDeleteAllowed = CSA.Service.isDeleteAllowed(perms),
                        isCreateFolder = CSA.Service.isCreateFolder(perms);

                    if (isWrite == true) {
                        if (this.isContainer) {
                            this.menuWidth = "130px";
                            if (isDeleteAllowed) {
                                if (isCreateFolder) {
                                    this.aMenuItems = this.menuItems["assetsFolderMenu"].slice();
                                } else {
                                    this.aMenuItems = this.menuItems["assetsFolderMenuNoCreateFolder"].slice();
                                }
                            } else {
                                if (isCreateFolder) {
                                    this.aMenuItems = this.menuItems["assetsFolderMenuNoDelete"].slice();
                                } else {
                                    this.aMenuItems = this.menuItems["assetsFolderMenuNoDeleteNoCreateFolder"].slice();
                                }
                            }
                        } else {
                            this.menuWidth = "130px";
                            if (isDeleteAllowed) {
                                this.aMenuItems = this.menuItems["assetsMenu"].slice();
                            } else {
                                this.aMenuItems = this.menuItems["assetsMenuNoDelete"].slice();
                            }
                        }

                        if(oCurrentTextNode.data.uri.indexOf("/templates") != -1) {
                            this.aMenuItems.push(this.menuItems["assetsFolderTemplate"]);
                        }

                        if(oCurrentTextNode.data.uri.indexOf("/scripts") != -1) {
                            this.aMenuItems.push(this.menuItems["assetsFolderScript"]);
                        }

                        if(oCurrentTextNode.data.uri.indexOf(".ftl") != -1
                            ||  oCurrentTextNode.data.uri.indexOf(".js") != -1
                            ||  oCurrentTextNode.data.uri.indexOf(".css") != -1
                            ||  oCurrentTextNode.data.uri.indexOf(".groovy") != -1
                            ||  oCurrentTextNode.data.uri.indexOf(".html") != -1
                            ||  oCurrentTextNode.data.uri.indexOf(".hbs") != -1) {
                            // item is a template

                            this.aMenuItems.push(
                                { text: "Edit", disabled: false, onclick: { fn: CSA.ContextualNav.WcmAssetsFolder.editTemplate } });
                        }

                    } else {
                        if (this.isContainer) {
                            this.menuWidth = "130px";
                            this.aMenuItems = this.menuItems["assetsFolderMenuRead"].slice();
                        } else {
                            this.menuWidth = "100px";
                            this.aMenuItems = this.menuItems["assetsMenuRead"].slice();
                        }
                    }

                    if (CSA.Utils.hasPerm(CSA.Constants.PERMISSION_WRITE, perms)){
                        this.aMenuItems.push({
                            text: "Bulk Upload Assets",
                            onclick: { fn: CSA.ContextualNav.WcmAssetsFolder.bulkUpload }
                        });
                    }

                    var checkClipboardCb = {
                        success: function(collection) {

                            if(collection.count > 0) {
                                if (isWrite == true) {
                                    this.menuItems.push(
                                        { text: "Paste", onclick: { fn: CSA.ContextualNav.WcmAssetsFolder.pasteContent } });
                                } else {
                                    this.menuItems.push(
                                        { text: "Paste", disabled: true, onclick: { fn: CSA.ContextualNav.WcmAssetsFolder.pasteContent } });
                                }
                            }

                            this.args.addItems(this.menuItems);
                            this.menuEl.style.display = "block";
                            this.menuEl.style.width = this.menuWidth;
                            this.args.render();
                            this.args.show();
                        },

                        failure: function() {
                        },

                        args: this.p_aArgs,
                        menuItems: this.aMenuItems,
                        menuEl: this.menuId,
                        menuWidth: this.menuWidth
                    };

                    CSA.Clipboard.getClipboardContent(checkClipboardCb);

                },
                failure: function() { }
            };
            checkPermissionsCb.menuItems = menuItems;
            checkPermissionsCb.aMenuItems = aMenuItems;
            checkPermissionsCb.menuWidth = menuWidth;
            checkPermissionsCb.menuId = menuId;
            checkPermissionsCb.p_aArgs = p_aArgs;
            checkPermissionsCb.oCurrentTextNode = oCurrentTextNode;
            checkPermissionsCb.isContainer = isContainer;
            CSA.Service.getUserPermissions(CStudioAuthoringContext.site, oCurrentTextNode.data.uri, checkPermissionsCb);

        }

    },

    /**
     * Creates new container, Opens a dialog box to enter folder name
     */
    createContainer: function() {
        var createCb = {
            success: function() {
                Self.refreshNodes(this.tree,false);

            },

            failure: function() {
            },

            callingWindow: window,
            tree: oCurrentTextNode
        };

        CStudioAuthoring.Operations.createFolder(
            CStudioAuthoringContext.site,
            oCurrentTextNode.data.uri,
            window,
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
            oCurrentTextNode.data.formId, CStudioAuthoringContext.site,
            path, oCurrentTextNode.data.nodeRef, path, false, editCb);
    },

    editTemplate: function() {
        var path = (oCurrentTextNode.data.uri);

        var editCb = {
            success: function() {
                if(CStudioAuthoringContext.isPreview){
                     CStudioAuthoring.Operations.refreshPreview(); 
                }
                else {
                    this.callingWindow.location.reload(true);
                }
            },

            failure: function() {
            },

            callingWindow: window
        };

        CStudioAuthoring.Operations.openTemplateEditor(path, "default", editCb);
    },

    createNewTemplate: function() {
        CStudioAuthoring.Operations.createNewTemplate({ 
            success: function(templatePath) {
                this.callingWindow.location.reload(true);   
            }, 
            failure: function() {

            },

            callingWindow: window
        }); 
    },

    createNewScript: function() {
        CStudioAuthoring.Operations.createNewScript( oCurrentTextNode.data.uri, { 
            success: function(templatePath) {
                Self.refreshNodes(this.tree,false);  
            }, 
            failure: function() {

            },
            tree: oCurrentTextNode
        }); 
    },

    /**
     *	upload an asset to the target folder if it's a new asset
     */
    uploadAsset: function() {
        var uploadCb = {
            success: function() {
                Self.refreshNodes(this.tree,false);

                // Refresh the "My Recent Activity" section in the dashboard
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
    },

    bulkUpload: function () {

        var CSA = CStudioAuthoring,
            CSAC = CStudioAuthoringContext,

            fmt = CSA.StringUtils.format;

        CSA.Env.Loader.use('component-dropbox', 'dialog-bulkupload', function () {

            var view = new CSA.Dialogs.BulkUpload(),
                Dropbox = CSA.Component.Dropbox,
                treeNode = oCurrentTextNode;

            document.body.appendChild(view.element);
            var serviceUrl = CStudioAuthoring.Service.createServiceUri(
                CStudioAuthoring.Service.createWriteServiceUrl(
                    treeNode.data.uri, 
                    treeNode.data.filename, 
                    null,
                    treeNode.data.contentType, 
                    CSAC.site, 
                    true, 
                    false, 
                    false, 
                    true));

            var dropbox = new Dropbox({
                element: view.element,
                display: fmt(
                    '#{0} .file-display-container .pad',
                    view.id),
                progress: '.progress .bar',
                target: serviceUrl,
                uploadPostKey: 'file',
                formData: {
                    site: CSAC.site,
                    path: oCurrentTextNode.data.uri
                },
                template: fmt('template_{0}', view.id),
                newOnTop: true
            });

            dropbox.showUploadProgress = function (elem, progress) {
                elem.style.width = progress + '%';
            }

            dropbox.on(Dropbox.UPLOAD_SUCCESS_EVENT, function (data) {
                if (treeNode.expanded){
                    CSA.ContextualNav.WcmAssetsFolder.refreshNodes(treeNode);
                }
            });

        });
    },

    /**
     *	upload an asset to the target folder if it's a new asset
     */
    overwriteAsset: function() {
        var uploadCb = {
            success: function() {
                Self.refreshNodes(this.tree,false);

            },

            failure: function() {
            },

            callingWindow: window,
            tree: oCurrentTextNode
        };

        CStudioAuthoring.Operations.uploadAsset(
            CStudioAuthoringContext.site,
            oCurrentTextNode.data.uri,
            "overwrite",
            uploadCb);
    },

    /**
     * Deletes the TextNode that was the target of the "contextmenu"
     * event that triggered the display of the ContextMenu instance.
     */
    deleteContent: function(p_sType, p_aArgs, tree) {
        CStudioAuthoring.Operations.deleteContent([oCurrentTextNode.data]);
    },

    /**
     *	Deletes a folder and contents in the target folder
     */
    deleteContainer: function(p_sType, p_aArgs, tree) {
        CStudioAuthoring.ContextualNav.WcmAssetsFolder.deleteContent(p_sType, p_aArgs, tree);
    },

    nodeHoverEffects: function(e) {
        var YDom = YAHOO.util.Dom,
            highlightWrpClass = "highlight-wrapper",
            highlightColor = "#e2e2e2",
            overSetClass = "over-effect-set",
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

                var nodeId = (""+el.id).replace("table","label");
                var node = Self.treeNodes[nodeId];

                if( node.isContainer == false){

                    // remove this preview
                    // var reportContainerEl = document.getElementById("cstudioPreviewAnalyticsOverlay");

                    // if(reportContainerEl) {
                    //     document.body.removeChild(reportContainerEl);
                    // }

                    // var reportContainerEl = document.createElement("div");
                    // reportContainerEl.id = "cstudioPreviewAnalyticsOverlay";
                    // YAHOO.util.Dom.addClass(reportContainerEl, "cstudio-analytics-overlay");

                    // reportContainerEl.style.position = "fixed";
                    // reportContainerEl.style.width = "800px";
                    // reportContainerEl.style.top = "100px";

                    // var x = (window.innerWidth / 2) - (reportContainerEl.offsetWidth / 2) - 400;
                    // reportContainerEl.style.left = x+"px";


                    // document.body.appendChild(reportContainerEl);
                    // reportContainerEl.innerHTML =
                    //     "<div style='line-height: 111px; text-align: center;'><img src='"+CStudioAuthoringContext.baseUri + "/static-assets/themes/cstudioTheme/images/wait.gif'/></div>";


                    // var url = CStudioAuthoringContext.authoringAppBaseUri + "/page/site/" + CStudioAuthoringContext.site + "/cstudio-itempreview-overlay?nodeRef="+node.nodeRef;
                    // reportContainerEl.innerHTML = "<iframe id='cstudioPreviewAnalyticsOverlayFrame' style='border: none; margin-left: 100px; width: 600px; height:400px; margin-top:25px; margin-bottom: 25px;' src='"+ url + "' />";
                    // var iframe = document.getElementById("cstudioPreviewAnalyticsOverlayFrame");
                    // var iframeBody = iframe.contentDocument.getElementsByTagName('body')[0]
                    // iframeBody.style = "background: none transparent !important;";
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

                var reportContainerEl = document.getElementById("cstudioPreviewAnalyticsOverlay");

                if(reportContainerEl) {
                    document.body.removeChild(reportContainerEl);
                }

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
        if (node.isLeaf) node.isLeaf = false;
        tree.removeChildren(node);
        var loadEl = $(".ygtvtp", node.getEl(), true);
        loadEl == null && (loadEl = $(".ygtvlp", node.getEl(), true));
        YDom.addClass(loadEl, "ygtvloading");
        node.renderChildren();
        node.refresh();

        var treeInner = YDom.get('acn-dropdown-menu-inner');
        var previousCutEl = YDom.getElementsByClassName("status-icon", null, treeInner);

        for(var i=0; i<previousCutEl.length; i++){

            if(previousCutEl[i].style.color == 'rgb(159, 182, 205)' || previousCutEl[i].style.color == '#9FB6CD' ){

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
    }

};

/**
 * instance object
 * CStudioAuthoring.ContextualNav.WcmAssetsFolder is static
 */
CStudioAuthoring.ContextualNav.WcmAssetsFolderInstance = function(config) {

    this._self = this;
    this._toggleState = CStudioAuthoring.ContextualNav.WcmAssetsFolder.ROOT_CLOSED;
    this.rootFolderEl = null;

    this.type = config.name;
    this.label = config.params["label"];
    this.path = config.params["path"];
    this.showRootItem = (config.params["showRootItem"]) ? config.params["showRootItem"] : false;
    this.onClickAction = (config.params["onClick"]) ? config.params["onClick"] : "";
    this.config = config;

};

CStudioAuthoring.Module.moduleLoaded("wcm-assets-folder", CStudioAuthoring.ContextualNav.WcmAssetsFolder);
