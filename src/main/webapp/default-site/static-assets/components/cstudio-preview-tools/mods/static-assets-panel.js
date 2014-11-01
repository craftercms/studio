/**
 * editor tools
 */
CStudioAuthoring.StaticAssetsPanel = CStudioAuthoring.StaticAssetsPanel || {

	initialized: false,
	getContextIdUrl: "/api/1/site_context/id.json",
	getItemServiceUrl: "/api/1/content_store/descriptor.json",
	configurationUrl: "/preview-tools/assets-config.xml",
	
	/**
	 * initialize module
	 */
	initialize: function(config) {
		if(this.initialized == false) {
			this.initialized = true;
		}
	},
	
	render: function(containerEl, config) {
		CStudioAuthoring.Service.lookupConfigurtion(
				CStudioAuthoringContext.site, 
				CStudioAuthoring.StaticAssetsPanel.configurationUrl, {
					success: function(config) {

						var itemSelectEl = document.createElement("select");
						CStudioAuthoring.StaticAssetsPanel.createUI(containerEl, itemSelectEl);	
						
						// Load page model
						var getContextIdCallback = {
							success: function(context) {
								var id = eval(context.responseText);
								var url = CStudioAuthoring.StaticAssetsPanel.getPreviewPagePath(CStudioAuthoringContext.previewCurrentPath);
								var getItemServiceUrl = CStudioAuthoring.StaticAssetsPanel.getItemServiceUrl + "?url=" + url + "&contextId=" + id;
								var getItemCallBack = {
									success: function(response) {
										var json = response.responseText;
										var pageContent = eval("(" + json + ")");
								        CStudioAuthoring.StaticAssetsPanel.populateAssets(config, pageContent, itemSelectEl);
									}, // end of success
									failure: function(err) {
									} // end of failure
								}; // end of getItemCallBack
								YConnect.asyncRequest('GET', getItemServiceUrl, getItemCallBack);
							}, // end of success
							failure: function(err) {
							}
						}; // end of getContextIdCallback
		               
		               	// get the context id from preview store
						YConnect.asyncRequest('GET', CStudioAuthoring.StaticAssetsPanel.getContextIdUrl, getContextIdCallback);
		               
					}, // end of lookupConfigurtion cb success
					failure: function() {
					}
				});	// end of lookupConfigurtion
		
	}, // end of render
	
	/*
	* read static assets included in the page descriptor based on xpaths from configuration 
	* and list them in the dropdown for edit
	*/
	populateAssets: function(config, pageContent, itemSelectEl) {
		var selectIndex = 1;
		// only add those with write permission
		var checkPermissionsCb = function (cbResults, asset) {
			var isWrite = CStudioAuthoring.Service.isWrite(cbResults.permissions);
			if (isWrite == true) {
				itemSelectEl.options[selectIndex++] = new Option(asset, asset, false, false);
			} 
		};
		
		var assetList = new Array();
		var assetIndex = 0;
		// read asset lists based on path define in configuration
		if (config.assetNodes && config.assetNodes.length) {
			for (var assetNode in config.assetNodes) {
				var assetPath = config.assetNodes[assetNode];
				var elements = assetPath.split("/");
				// walk down the structure to find the last node
				var currentNode = pageContent.page;
				for (var element in elements) {
					var nodeName = elements[element];
					if (nodeName.length > 0) {
						var currentNode = currentNode[nodeName];
					}
				}
				if (currentNode != undefined) {
					// check for array
					if(currentNode instanceof Array) {
						for (var nodeIndex in currentNode) {
							assetList[assetIndex++] = currentNode[nodeIndex];
						}
					} else {
						assetList[assetIndex++] = currentNode;
					}
				}
			}
		}
		// look up dependencies	
		var dependencies = new Array();
		var depIndex = 0;
		var dependenciesCb = {
			success: function(result) {
				for (var itemIndex in result.items) {
					var item = result.items[itemIndex];
					if (item.assets) {
						for (var dependencyIndex in item.assets) {
							var assetUri = item.assets[dependencyIndex].uri;
							if (assetUri.match("\.(css|js|ftl)$")) {
								// add only if the original lis doesn't contain the same item 
								var dependencyUri = item.assets[dependencyIndex].uri;
								if (assetList.indexOf(dependencyUri) == -1) {
									assetList[assetList.length] = dependencyUri;
								}
							}
						}
					}
				}
				// check to make sure the user has write permissions
				for (var assetIndex in assetList) {
					CStudioAuthoring.Service.getUserPermissions(CStudioAuthoringContext.site, assetList[assetIndex], function() {
						var asset = assetList[assetIndex];
						return {
							success: function(results) {
								checkPermissionsCb(results, asset);
							},
								failure: function() {
							}
						}
					}() );
				}
				
			},
			failure: function() {
			}
		};
		this.lookupContentDependencies(CStudioAuthoringContext.site, assetList, dependenciesCb);
	},
	
	
	/**
	* given a list of items return the topdown dependencies
	 */
	lookupContentDependencies: function(site, contentItems, callback) {
		var serviceUri = "/proxy/alfresco/cstudio/wcm/dependency/get-dependencies?" + "site=" + site;
		var dependencyXml = this.createDependencyXml(contentItems);
		var serviceCallback = {
			success: function(oResponse) {
				var respJson = oResponse.responseText;
					try {
						var dependencies = eval("(" + respJson + ")");
						callback.success && callback.success(dependencies);
					} catch(err) {
						callback.failure && callback.failure(err);
					}
				},
			failure: callback.failure
		};
		YConnect.setDefaultPostHeader(false);
		YConnect.initHeader("Content-Type", "application/xml; charset=utf-8");
		YConnect.asyncRequest('POST', CStudioAuthoring.Service.createServiceUri(serviceUri), serviceCallback, dependencyXml);
	},

	
	createDependencyXml: function (items) {
		var xmlString = '<items>';
		for (var itemIndex in items) {
			xmlString = xmlString + '<item uri="' + items[itemIndex] + '"/>';
		}
		xmlString += '</items>';
		return xmlString;
	},
	
	/*
	* create the asset dropdown and edit button
	*/
	createUI: function(containerEl, itemSelectEl) {
		containerEl.appendChild(itemSelectEl);
		YAHOO.util.Dom.addClass(itemSelectEl, "acn-panel-dropdown");
		itemSelectEl.style.height = "20px";

		var editButtonEl = document.createElement("input");
		editButtonEl.type = "submit";
		editButtonEl.value = "Edit";
		YAHOO.util.Dom.addClass(editButtonEl, "cstudio-button");
		editButtonEl.style.backgroundColor = "#C0C0C0";
		editButtonEl.style.border = "1px solid #002185"; 
		editButtonEl.style.borderRadius = "3px 3px 3px 3px"; 
		editButtonEl.style.color ="#000000"; 
		editButtonEl.style.font = "12px Arial,Helvetica"; 
		editButtonEl.style.minWidth = "6em; padding: 5px 10px";
		editButtonEl.style.height = "27px";
		editButtonEl.style.left = "80px";
		editButtonEl.style.position = "relative";
		editButtonEl.style.right = "auto";
		containerEl.appendChild(editButtonEl);
						
		itemSelectEl.options[0] = new Option("Select Static Asset", "", true, false);

		// add onclick behavior to open up the edit dialog
		editButtonEl.onclick = function() {
			var selectedIndex = itemSelectEl.selectedIndex;
			if(selectedIndex != 0) {
				var itemUrl = itemSelectEl.options[selectedIndex].value;
				// check if the item exists before opening the edit dialog
				var existCb = {
					exists: function(result) {
						if (result == 'true') {
							CStudioAuthoring.Operations.openTemplateEditor(itemUrl, "default", onSaveCb);
						} else {
							alert(itemUrl + " does not exist.");
						}
					},
					failure: function() {
					}
				};
				CStudioAuthoring.Service.contentExists(itemUrl, existCb);
			}
		}; // end of onclick

		var onSaveCb = {
			success: function() {
				var cb = {
					moduleLoaded: function(moduleName, moduleClass, moduleConfig) {
						try { moduleClass.render(); } 
						catch (e) {}	
					},
				self: this
				};
			},
			failure: function() {}
		}; // end of onSaveCb

	},
	
	// this should be calculated in common api
	getPreviewPagePath: function (previewPath) {
		var pagePath = previewPath.replace(".html", ".xml");

		if (pagePath.indexOf(".xml") == -1) {
			if (pagePath.substring(pagePath.length - 1) != "/") {
				pagePath += "/";
			}
			pagePath += "index.xml";
		}
		return pagePath;
	},
	
}

CStudioAuthoring.Module.moduleLoaded("static-assets-panel", CStudioAuthoring.StaticAssetsPanel);	