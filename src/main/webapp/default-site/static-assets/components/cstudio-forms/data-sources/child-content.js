CStudioForms.Datasources.ChildContent= CStudioForms.Datasources.ChildContent ||  
function(id, form, properties, constraints)  {
   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;
	this.selectItemsCount = -1;
	this.type = "";
   	
   	for(var i=0; i<properties.length; i++) {
   		if(properties[i].name == "repoPath") {
 			this.repoPath = properties[i].value;
   		}

		if(properties[i].name == "type"){
			this.type = (Array.isArray(properties[i].value))?"":properties[i].value;
		}
   	} 

	return this;
}

YAHOO.extend(CStudioForms.Datasources.ChildContent, CStudioForms.CStudioFormDatasource, {
	itemsAreContentReferences: true,
	
	add: function(control) {
		var _self = this;
		
		var addContainerEl = null;
		
		if(control.addContainerEl) {
			addContainerEl = control.addContainerEl;
			control.addContainerEl = null;
			control.containerEl.removeChild(addContainerEl);			
		}
		else {
			addContainerEl = document.createElement("div")
	        control.containerEl.appendChild(addContainerEl);
	        YAHOO.util.Dom.addClass(addContainerEl, 'cstudio-form-control-node-selector-add-container');
	        control.addContainerEl = addContainerEl;

//			YAHOO.util.Event.on(addContainerEl, 'mouseout', function() {
//				control.addContainerEl = null;
//				control.containerEl.removeChild(addContainerEl);
//			}, addContainerEl);
					
	       	addContainerEl.style.left = control.addButtonEl.offsetLeft + "px";
	       	addContainerEl.style.top = control.addButtonEl.offsetTop + 22 + "px";
		        		
			 var createEl = document.createElement("div");
			 YAHOO.util.Dom.addClass(createEl, 'cstudio-form-control-node-selector-add-container-item');
			 createEl.innerHTML = "Create New";
		   	 addContainerEl.appendChild(createEl);
		   	 
			YAHOO.util.Event.on(createEl, 'click', function() {
				control.addContainerEl = null;
				control.containerEl.removeChild(addContainerEl);
				if(_self.type == ""){
					CStudioAuthoring.Operations.createNewContent(
						CStudioAuthoringContext.site,
						_self.processPathsForMacros(_self.repoPath),
						false, { 
							success: function(formName, name, value) {
								control.insertItem(name, value);
								control._renderItems();

								var editorId = CStudioAuthoring.Utils.getQueryVariable(location.search, 'editorId');
								window.top.iceDialogs[editorId].close()
							},
							failure: function() {
							}	
					}, true);
				}else{
					CStudioAuthoring.Operations.openContentWebForm(
						_self.type,
						null,
						null,
						_self.processPathsForMacros(_self.repoPath),
						false,
						false,
						{ 
							success: function(contentTO, editorId, name, value) {
								control.insertItem(name, value);
								control._renderItems();
								window.top.iceDialogs[editorId].close()

							},
							failure: function() {
							}	
						},
						[{ name: "childForm", value: "true"}]);
				}
			}, createEl);		   	 
											   	 
	
			 var browseEl = document.createElement("div");
			 browseEl.innerHTML = "Browse for Existing";
			 YAHOO.util.Dom.addClass(browseEl, 'cstudio-form-control-node-selector-add-container-item');
		   	 addContainerEl.appendChild(browseEl);

			 YAHOO.util.Event.on(browseEl, 'click', function() {
				control.addContainerEl = null;
				control.containerEl.removeChild(addContainerEl);

				CStudioAuthoring.Operations.openBrowse("", _self.processPathsForMacros(_self.repoPath), _self.selectItemsCount, "select", true, { 
					success: function(searchId, selectedTOs) {

						for(var i=0; i<selectedTOs.length; i++) {
							var item = selectedTOs[i];
							var value = (item.internalName && item.internalName != "")?item.internalName:item.uri;
							control.insertItem(item.uri, value);
							control._renderItems();
							
							var editorId = CStudioAuthoring.Utils.getQueryVariable(location.search, 'editorId');
							window.top.iceDialogs[editorId].close()

						}					
					}, 
					failure: function() {
					}
				}); 
			}, browseEl);		   	 
		}
	},
	
	edit: function(key, control) {
		var getContentItemCb = {
			success: function(contentTO) {

				var editCallback = {
					success: function(contentTO, editorId, name, value) {
                        if(control){
                            control.updateEditedItem(value);
							window.top.iceDialogs[editorId].close();
                        }

					},
					failure: function() {
					}
				}
				
				CStudioAuthoring.Operations.editContent(
					contentTO.item.contentType,
					CStudioAuthoringContext.siteId,
					contentTO.item.uri,
					contentTO.item.nodeRef,
					contentTO.item.uri,
					false,
					editCallback);	
			},
			failure: function() {
			}
		};
		
		CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, key, getContentItemCb);
	},

    updateItem: function(item, control){
        if(item.key && item.key.match(/\.xml$/)){
            var getContentItemCb = {
                success: function(contentTO) {
                    item.value =  contentTO.item.internalName || item.value;
                    control._renderItems();
                },
                failure: function() {
                }
            }

            CStudioAuthoring.Service.lookupContentItem(CStudioAuthoringContext.site, item.key, getContentItemCb);
        }
    },

    getLabel: function() {
        return "Child content";
    },

   	getInterface: function() {
   		return "item";
   	},

	getName: function() {
		return "child-content";
	},
	
	getSupportedProperties: function() {
		return [
			{ label: "Repository Path", name: "repoPath", type: "string" },
			{ label: "Default Type", name: "type", type: "string" }
		];
	},

	getSupportedConstraints: function() {
		return [
		];
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-child-content", CStudioForms.Datasources.ChildContent);