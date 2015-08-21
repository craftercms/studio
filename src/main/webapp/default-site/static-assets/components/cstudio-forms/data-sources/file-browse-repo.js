CStudioForms.Datasources.FileBrowseRepo= CStudioForms.Datasources.FileBrowseRepo ||  
function(id, form, properties, constraints)  {
   	this.id = id;
   	this.form = form;
   	this.properties = properties;
   	this.constraints = constraints;
   	
   	for(var i=0; i<properties.length; i++) {
   		if(properties[i].name == "repoPath") {
 			this.repoPath = properties[i].value;
   		}
   	} 
	
	return this;
}

YAHOO.extend(CStudioForms.Datasources.FileBrowseRepo, CStudioForms.CStudioFormDatasource, {

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
					
	       	addContainerEl.style.left = control.addButtonEl.offsetLeft + "px";
	       	addContainerEl.style.top = control.addButtonEl.offsetTop + 22 + "px";
		        		
			 var createEl = document.createElement("div");
			 YAHOO.util.Dom.addClass(createEl, 'cstudio-form-control-node-selector-add-container-item');
			 createEl.innerHTML = "Create New";
		   	 addContainerEl.appendChild(createEl);
		   	 
			YAHOO.util.Event.on(createEl, 'click', function() {
				control.addContainerEl = null;
				control.containerEl.removeChild(addContainerEl);
				
			    CStudioAuthoring.Operations.uploadAsset(CStudioAuthoringContext.site, _self.processPathsForMacros(_self.repoPath), true, { 
					success: function(fileData) {
						var item = _self.processPathsForMacros(_self.repoPath) + "/" + fileData.fileName;
						control.insertItem(item, item, fileData.fileExtension, fileData.size);
						control._renderItems();
					}, 

					failure: function() {
					},

					context: this });	
			}, createEl);		   	 
			
			 var browseEl = document.createElement("div");
			 browseEl.innerHTML = "Browse for Existing";
			 YAHOO.util.Dom.addClass(browseEl, 'cstudio-form-control-node-selector-add-container-item');
		   	 addContainerEl.appendChild(browseEl);

			 YAHOO.util.Event.on(browseEl, 'click', function() {
				control.addContainerEl = null;
				control.containerEl.removeChild(addContainerEl);

				CStudioAuthoring.Operations.openBrowse("", _self.processPathsForMacros(_self.repoPath), "-1", "select", true, { 
					success: function(searchId, selectedTOs) {

						for(var i=0; i<selectedTOs.length; i++) {
							var item = selectedTOs[i];
							var fileName = item.name;
							var fileExtension = fileName.split('.').pop();
							control.insertItem(item.uri, item.uri, fileExtension);
							control._renderItems();
						}					
					}, 
					failure: function() {
					}
				}); 
			}, browseEl);		   	 
		}
	},
	
	edit: function(key) {
		var getContentItemCb = {
			success: function(contentTO) {

				var editCallback = {
					success: function() {
						// update label?
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
	
    getLabel: function() {
        return CMgs.format(langBundle, "fileBrowse");
    },

   	getInterface: function() {
   		return "item";
   	},

	getName: function() {
		return "file-browse-repo";
	},
	
	getSupportedProperties: function() {
		return [
			{ label: "Repository Path", name: "repoPath", type: "string" }
		];
	},

	getSupportedConstraints: function() {
		return [
		];
	}

});

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-file-browse-repo", CStudioForms.Datasources.FileBrowseRepo);