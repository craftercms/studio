CStudioForms.Datasources.FileDesktopUpload = CStudioForms.Datasources.FileDesktopUpload ||  
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

YAHOO.extend(CStudioForms.Datasources.FileDesktopUpload, CStudioForms.CStudioFormDatasource, {
	itemsAreContentReferences: true,

	/**
	 * action called when user clicks insert file
	 */
	add: function(control) {
		this._self = this;

		var site = CStudioAuthoringContext.site;
		var path = this._self.repoPath;
		var isUploadOverwrite = true;

		for(var i=0; i<this.properties.length; i++) {
			if(this.properties[i].name == "repoPath") {
				path = this.properties[i].value;
			
				path = this.processPathsForMacros(path);
			}
		}

		var callback = { 
			success: function(fileData) {
				if (control) {
					control.insertItem(path + "/" + fileData.fileName, path + "/" + fileData.fileName, fileData.fileExtension, fileData.size);
					control._renderItems();
				}
			},

			failure: function() {
				if (control) {
					control.failure("An error occurred while uploading the file."); 
				}
			},

			context: this 
		};

		CStudioAuthoring.Operations.uploadAsset(site, path, isUploadOverwrite, callback);
	},

	edit: function(key, control) {
		this._self = this;

		var site = CStudioAuthoringContext.site;
		var path = this._self.repoPath;
		var isUploadOverwrite = true;

		for(var i=0; i<this.properties.length; i++) {
			if(this.properties[i].name == "repoPath") {
				path = this.properties[i].value;

				path = this.processPathsForMacros(path);
			}
		}

		var callback = { 
			success: function(fileData) {
				if (control) {
					control.deleteItem(key);
					control.insertItem(path + "/" + fileData.fileName, path + "/" + fileData.fileName, fileData.fileExtension, fileData.size);
					control._renderItems();
				}
			},

			failure: function() {
				if (control) {
					control.failure("An error occurred while uploading the file."); 
				}
			},

			context: this 
		};

		CStudioAuthoring.Operations.uploadAsset(site, path, isUploadOverwrite, callback);
	},

    getLabel: function() {
        return "File uploaded from desktop";
    },

   	getInterface: function() {
   		return "item";
   	},

	getName: function() {
		return "file-desktop-upload";
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

CStudioAuthoring.Module.moduleLoaded("cstudio-forms-controls-file-desktop-upload", CStudioForms.Datasources.FileDesktopUpload);